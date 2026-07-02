package com.pikaqiu.concurrentpay;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 方案三：Redis 内存扣减（纯 Java 模拟版，免依赖可直接跑）
 *
 * 真实环境：余额放 Redis，用 Lua 脚本（见同目录 deduct.lua）原子完成
 * "幂等 + 判断 + DECRBY"。Redis 单线程执行命令，对单 key 的 Lua 脚本天然
 * 原子且串行，所以不超扣、无需额外加锁。
 *
 * 本 demo 用 synchronized 模拟"Redis 单线程原子段"——等价于 Redis 对单 key
 * 的串行化执行。deduct() 内每一步都标注了对应的真实 Redis 命令。
 *
 * 还演示了 opLog（操作日志）：每笔成功扣减先落可靠日志再对外返回，
 * 这是方案三的命门（持久化 / 对账 / Redis 丢数据后重建余额）。
 *
 * 金额统一用 long（分），规避浮点误差。
 */
public class RedisStyleDeductionDemo {

    enum Code { SUCCESS, INSUFFICIENT, DUPLICATE, NO_ACCOUNT }

    /** 模拟单个 Redis 实例（单线程原子语义） */
    static final class RedisSim {
        private final Map<Long, Long> balance = new HashMap<>();   // balance:{accountId}
        private final Map<String, Code> dedup = new HashMap<>();   // dedup:{requestId}
        // 可靠操作日志：模拟 Kafka / Redis Stream，"先落盘再对外返回成功"
        private final Queue<String> opLog = new ConcurrentLinkedQueue<>();

        void initBalance(long accountId, long cents) {
            balance.put(accountId, cents);
        }

        /**
         * 对应 deduct.lua 的整段原子执行。
         * synchronized == Redis 单线程对该实例命令的串行化。
         */
        synchronized Code deduct(long accountId, String requestId, long amountCents) {
            // 1) 幂等：EXISTS dedup:{requestId}
            Code prev = dedup.get(requestId);
            if (prev != null) {
                return Code.DUPLICATE;
            }
            // 2) GET balance:{accountId}
            Long bal = balance.get(accountId);
            if (bal == null) {
                return Code.NO_ACCOUNT;
            }
            // 3) 余额校验
            if (bal < amountCents) {
                dedup.put(requestId, Code.INSUFFICIENT);            // SETEX dedup REJECTED
                return Code.INSUFFICIENT;
            }
            // 4) DECRBY balance:{accountId} amount
            long newBal = bal - amountCents;
            balance.put(accountId, newBal);
            dedup.put(requestId, Code.SUCCESS);                    // SETEX dedup SUCCESS

            // 5) 同步追加操作日志（必须先于"对外返回成功"可靠落盘）
            opLog.add(String.format("acct=%d req=%s amt=%d newBal=%d",
                    accountId, requestId, amountCents, newBal));
            return Code.SUCCESS;
        }

        long balanceOf(long accountId) {
            return balance.getOrDefault(accountId, 0L);
        }

        int opLogSize() {
            return opLog.size();
        }

        /** Redis 丢数据后，按操作日志 + DB 初始余额重建（这里只演示思路） */
        long rebuildFromLog(long accountId, long dbBaseBalance) {
            long deducted = 0;
            for (String line : opLog) {
                if (line.startsWith("acct=" + accountId + " ")) {
                    int s = line.indexOf("amt=") + 4;
                    int e = line.indexOf(' ', s);
                    deducted += Long.parseLong(line.substring(s, e));
                }
            }
            return dbBaseBalance - deducted;
        }
    }

    // ====================== 演示 ======================
    public static void main(String[] args) throws InterruptedException {
        final long accountId = 2001L;
        final long initial = 1_000_00;                     // 初始 1000.00 元

        RedisSim redis = new RedisSim();
        redis.initBalance(accountId, initial);

        final int threads = 100;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger insufficient = new AtomicInteger();
        AtomicLong successAmount = new AtomicLong();

        for (int i = 0; i < threads; i++) {
            final int idx = i;
            pool.submit(() -> {
                try {
                    start.await();                          // 等统一发令，制造瞬时突发
                    long amount = 15_00 + (idx % 5) * 5_00; // 15~35 元，总需求 ~2500 元 >> 1000 元
                    Code code = redis.deduct(accountId, "REQ-" + idx, amount);
                    if (code == Code.SUCCESS) {
                        success.incrementAndGet();
                        successAmount.addAndGet(amount);
                    } else if (code == Code.INSUFFICIENT) {
                        insufficient.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                finally {
                    done.countDown();
                }
            });
        }

        start.countDown();                                  // 100 个线程同时冲
        done.await();
        pool.shutdown();
        pool.awaitTermination(2, TimeUnit.SECONDS);

        long finalBalance = redis.balanceOf(accountId);
        System.out.println("------------------ 结果 ------------------");
        System.out.printf("初始余额      : %.2f%n", initial / 100.0);
        System.out.printf("成功笔数      : %d%n", success.get());
        System.out.printf("成功总额      : %.2f%n", successAmount.get() / 100.0);
        System.out.printf("余额不足被拒  : %d%n", insufficient.get());
        System.out.printf("最终余额      : %.2f%n", finalBalance / 100.0);
        System.out.printf("opLog 条数(应==成功笔数)          : %d%n", redis.opLogSize());
        System.out.printf("账实校验(初始-成功总额==最终余额) : %b%n",
                initial - successAmount.get() == finalBalance);
        System.out.printf("无超扣(最终余额>=0)               : %b%n", finalBalance >= 0);
        System.out.printf("按 opLog 重建余额 == 当前余额      : %b%n",
                redis.rebuildFromLog(accountId, initial) == finalBalance);
    }
}
