package com.pikaqiu.concurrentpay;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 方案一：合并扣减（单写者 single-writer + 批处理）
 *
 * 思想：每个账户配一个独占的写者线程。调用方不直接改余额，而是把扣减请求投进
 * 该账户的队列后阻塞等结果。写者在一个极短的窗口内把多笔请求攒成一批，
 * 合并成"一次"扣减落库。N 把行锁 -> 1 把，吞吐被摊薄，DB（这里用内存字段模拟）
 * 仍是唯一权威源，强一致不变。
 *
 * 注意：余额只被单一 writer 线程修改 -> 该字段无需加锁。
 * 金额统一用 long（分）避免浮点；生产对应 DECIMAL / BigDecimal。
 *
 * 直接运行 main 即可看到 100 并发下"不超扣 + 账实平"。
 */
public class BatchMergeDeductionDemo {

    /** 结果码 */
    enum Code { SUCCESS, INSUFFICIENT, DUPLICATE, TIMEOUT }

    /** 一笔扣减请求 */
    static final class DeductRequest {
        final String requestId;
        final long amountCents;
        final CompletableFuture<Code> future = new CompletableFuture<>();

        DeductRequest(String requestId, long amountCents) {
            this.requestId = requestId;
            this.amountCents = amountCents;
        }
    }

    /** 单账户扣减引擎：持有该账户的余额 + 队列 + 单写者线程 */
    static final class AccountEngine {
        // ===== 可调旋钮：延迟 vs 合并率 =====
        private static final long BATCH_WINDOW_MS = 5;    // 凑批窗口，越大合并率越高、单笔延迟越高
        private static final int  MAX_BATCH_SIZE  = 200;  // 单批上限保护

        private final long accountId;
        private long balanceCents;                        // 模拟 DB 中的权威余额行（仅 writer 改）
        private final BlockingQueue<DeductRequest> queue = new LinkedBlockingQueue<>();
        private final Map<String, Code> dedup = new ConcurrentHashMap<>(); // 模拟 request_id 唯一索引（幂等）
        private final Thread writer;
        private volatile boolean running = true;

        AccountEngine(long accountId, long initialBalanceCents) {
            this.accountId = accountId;
            this.balanceCents = initialBalanceCents;
            this.writer = new Thread(this::loop, "writer-acct-" + accountId);
            this.writer.start();
        }

        /** 调用方入口：投递并同步等待结果（带超时兜底，防写者卡死） */
        Code pay(String requestId, long amountCents) {
            Code prev = dedup.get(requestId);             // 幂等前置：重复请求直接返回
            if (prev != null) {
                return Code.DUPLICATE;
            }
            DeductRequest req = new DeductRequest(requestId, amountCents);
            queue.offer(req);
            try {
                return req.future.get(500, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                return Code.TIMEOUT;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        /** 单写者主循环：取首笔 -> 凑批窗口 -> 一次取走全部 -> 结算 */
        private void loop() {
            while (running) {
                try {
                    DeductRequest first = queue.poll(100, TimeUnit.MILLISECONDS);
                    if (first == null) {
                        continue;                          // 空闲轮询，便于优雅关闭
                    }
                    List<DeductRequest> batch = new ArrayList<>();
                    batch.add(first);

                    Thread.sleep(BATCH_WINDOW_MS);         // 凑批窗口：让排队的请求堆进来
                    queue.drainTo(batch, MAX_BATCH_SIZE - 1);

                    settle(batch);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        /** 整批结算：足额则一次扣全部；不足则 FIFO 贪心部分结算 */
        private void settle(List<DeductRequest> batch) {
            long total = 0;
            for (DeductRequest r : batch) {
                total += r.amountCents;
            }
            System.out.printf("[writer] 合并一批 %3d 笔, 总额 %8.2f, 扣前余额 %8.2f%n",
                    batch.size(), total / 100.0, balanceCents / 100.0);

            if (balanceCents >= total) {
                balanceCents -= total;                     // 整批一次扣减（对应一次 UPDATE，一把行锁）
                for (DeductRequest r : batch) {
                    dedup.put(r.requestId, Code.SUCCESS);
                    r.future.complete(Code.SUCCESS);
                }
                return;
            }

            // 总额超余额 -> 按到达顺序贪心接纳一个前缀，拒绝其余
            long accepted = 0;
            List<DeductRequest> ok = new ArrayList<>();
            List<DeductRequest> rejected = new ArrayList<>();
            for (DeductRequest r : batch) {
                if (accepted + r.amountCents <= balanceCents) {
                    accepted += r.amountCents;
                    ok.add(r);
                } else {
                    rejected.add(r);                       // 也可改为"跳过大额、继续接纳小额"策略
                }
            }
            balanceCents -= accepted;                      // 仅扣被接纳的总额（一次 UPDATE）
            for (DeductRequest r : ok) {
                dedup.put(r.requestId, Code.SUCCESS);
                r.future.complete(Code.SUCCESS);
            }
            for (DeductRequest r : rejected) {
                dedup.put(r.requestId, Code.INSUFFICIENT);
                r.future.complete(Code.INSUFFICIENT);
            }
        }

        void shutdown() {
            running = false;
            writer.interrupt();
        }

        long balance() {
            return balanceCents;
        }
    }

    // ====================== 演示 ======================
    public static void main(String[] args) throws InterruptedException {
        final long initial = 1_000_00;                     // 初始 1000.00 元
        AccountEngine engine = new AccountEngine(1001L, initial);

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
                    long amount = 15_00 + (idx % 5) * 5_00; // 15~35 元，100 笔总需求 ~2500 元 >> 1000 元
                    Code code = engine.pay("REQ-" + idx, amount);
                    if (code == Code.SUCCESS) {
                        success.incrementAndGet();
                        successAmount.addAndGet(amount);
                    } else if (code == Code.INSUFFICIENT) {
                        insufficient.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();                                  // 100 个线程同时冲
        done.await();
        pool.shutdown();
        Thread.sleep(50);
        engine.shutdown();

        long finalBalance = engine.balance();
        System.out.println("------------------ 结果 ------------------");
        System.out.printf("初始余额      : %.2f%n", initial / 100.0);
        System.out.printf("成功笔数      : %d%n", success.get());
        System.out.printf("成功总额      : %.2f%n", successAmount.get() / 100.0);
        System.out.printf("余额不足被拒  : %d%n", insufficient.get());
        System.out.printf("最终余额      : %.2f%n", finalBalance / 100.0);
        System.out.printf("账实校验(初始-成功总额==最终余额) : %b%n",
                initial - successAmount.get() == finalBalance);
        System.out.printf("无超扣(最终余额>=0)               : %b%n", finalBalance >= 0);
    }
}
