package com.pikaqiu.demo.vol;

import lombok.Cleanup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @date 2019-10-12
 */
@SuppressWarnings("NullableProblems")
public class RedisLock implements Lock {
    private static final String LOCK_SUCCESS = "OK";
    private static final String SET_IF_NOT_EXIST = "NX";
    private static final String SET_WITH_EXPIRE_TIME = "PX";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String PREFIX = "apec_lock_redis:";

    /**
     * 最长持锁时间10000ms
     */
    private static final long EXPIRY = 10000;

    private static final String RELEASE_LOCK_LUA_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    private static JedisPool jedisPool;

    static {
        jedisPool = ApplicationContextUtil.getBean("jedisPool", JedisPool.class);
    }

    private final String key;

    private final String value;

    public RedisLock(String name) {
        this.key = PREFIX + name;
        this.value = UUID.randomUUID().toString();
    }

    @Override
    public void lock() {
        while (!tryLock()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                this.logger.error("获取锁失败:{}", e.getMessage());
                throw new RuntimeException("获取锁失败", e);
            }
        }
        logger.info("正确获取锁:" + key);
    }


    /**
     * 分布式锁无法实现此功能
     * <p>
     * 原功能如下:
     * <p>
     * 获取锁，除非线程被打断。
     * 如果锁不被其他线程持有，则获取锁，并立即返回，将锁持有计数设置为1。
     * 如果当前线程已经持有此锁，那么持有计数将增加1，方法立即返回。
     * 如果锁被另一个线程持有，那么当前线程将出于线程调度的目的被禁用，
     * 并处于休眠状态，直到发生以下两种情况之一:
     * 1.锁由当前线程获取。
     * 2.其他线程打断当前线程。
     * 如果锁被当前线程获取，那么锁持有计数被设置为1。
     * <p>
     * 如果当前线程:进入此方法时，其中断状态是否已设置
     * 或者当获取锁时被打断
     * 则会抛出InterruptedException，并清除interrupted status。
     * <p>
     * 在这个实现中，由于这个方法是一个显式的中断点，
     * 所以优先响应中断而不是正常的或可重入的锁获取。
     */
    @Override
    public void lockInterruptibly() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean tryLock() {
        @Cleanup Jedis resource = jedisPool.getResource();
        String result = resource.set(key, value, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, EXPIRY);
        return LOCK_SUCCESS.equals(result);

    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        long start = System.currentTimeMillis();
        long end = start + TimeUnit.MILLISECONDS.convert(time, unit);
        while (!tryLock()) {
            if (System.currentTimeMillis() > end) {
                logger.error("获取锁超时:" + key);
                return false;
            }
            Thread.sleep(100);
        }
        logger.info("正确获取锁:" + key);
        return true;
    }

    @Override
    public void unlock() {
        try {
            @Cleanup Jedis resource = jedisPool.getResource();
            resource.eval(RELEASE_LOCK_LUA_SCRIPT, Collections.singletonList(key), Collections.singletonList(value));
        } catch (Exception e) {
            logger.error("解锁异常:" + key, e);
        }
    }

    /**
     * 分布式锁无法实现此功能
     */
    @SuppressWarnings("NullableProblems")
    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException();
    }
//
//    private JedisCluster redis() {
//        if (jedisPool == null) {
//            jedisPool = ApplicationContextUtil.getBean("jedisCluster", JedisCluster.class);
//            if (jedisPool == null) {
//                throw new CannotAcquireLockException("redis未配置");
//            }
//        }
//        return jedisPool;
//    }


}
