package com.pikaqiu.common;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RedisLock extends AbstractLock {

    String key = "lock";


    public StringRedisTemplate stringRedisTemplate;

    private final long defaultExpireTime = 300000;
    private final String uuid;

    public RedisLock(StringRedisTemplate stringRedisTemplate, String lockName) {
        this.lockName = lockName;
        this.stringRedisTemplate = stringRedisTemplate;
        this.uuid = UUID.randomUUID().toString();
    }

    @Override
    public void lock() {
        lock(TimeUnit.MILLISECONDS, defaultExpireTime);
    }

    @Override
    public void lock(TimeUnit timeUnit, long expireTime) {
        // 1.使用setnx指令进行加锁
        while (true) {
            tryLockInternal(defaultExpireTime, timeUnit);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException();
            }
        }

    }

    @Override
    public boolean tryLock(long time, long expireTime, TimeUnit unit) {
        long startTime = System.currentTimeMillis();
        long currentTime = System.currentTimeMillis();
        while (currentTime - startTime < expireTime) {
            boolean result = tryLockInternal(time, unit);
            if (result) {
                return true;
            }
            currentTime = System.currentTimeMillis();
        }
        return false;
    }

    private boolean tryLockInternal(long time, TimeUnit timeUnit) {
        // 2.使用lua脚本进行加锁
        String luaScript = "if redis.call('exist', KEYS[1]) == 0 " +
                "then redis.call('hincrby', KEYS[1], 'ARGV[1]', 1) " +
                "redis.call('pexpire', KEYS[1], ARGV[2]) " +
                "return 1 else return 0 end";
        final Long result = stringRedisTemplate.execute(
                new DefaultRedisScript<>(luaScript, Long.class),
                Collections.singletonList(key),
                UUID.randomUUID().toString(),
                String.valueOf(timeUnit.toSeconds(time)));
        if (result != null && result == 1) {
            new Thread(() -> {
                while (true) {
                    // 3.使用lua脚本进行续期
                    String expireScript = "if redis.call('hexists', KEYS[1], 'ARGV[1]') == 0 " +
                            "then return 0; else redis.call('pexpire', KEYS[1], ARGV[2]) return 1 end";
                    Long expireResult = stringRedisTemplate.execute(new DefaultRedisScript<>(expireScript, Long.class), Collections.singletonList(key), UUID.randomUUID().toString(), String.valueOf(timeUnit.toSeconds(time)));
                    if (expireResult != null && expireResult == 0) {
                        break;
                    }
                    try {
                        //转成毫秒
                        TimeUnit.MILLISECONDS.sleep(timeUnit.toMillis(time) / 2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            return true;
        }
        return false;

    }


    @Override
    public void unlock() {
        // 4.使用lua脚本进行解锁
        String luaScript = "if redis.call('hexists', KEYS[1], 'ARGV[1]') == 0 " +
                "then return 0 ;" +
                "end " +
                "local count = redis.call('hincrby', KEYS[1], 'ARGV[1]', -1) " +
                "if count > 0 then return 1 else redis.call('del', KEYS[1]) return 0 end";
        stringRedisTemplate.execute(new DefaultRedisScript<>(luaScript, Long.class),
                Collections.singletonList(key), uuid);
    }
}
