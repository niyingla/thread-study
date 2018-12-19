package com.example.text.demo.vol;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.PostConstruct;
import java.util.Collections;

/**
 * @program: text
 * @description:
 * @author: xiaoye
 * @create: 2018-11-27 16:48
 **/
@Component
public class Lock {


    @Autowired
    private JedisPool jedisPool;


    private static final Long DEFAULT_SLEEP_TIME = 100L;
    private static final Long TIME = 1000L;
    private static final String LOCK_MSG = "OK";
    private static final String SET_IF_NOT_EXIST = "NX";
    private static final String UNLOCK_MSG = "OK";
    private static final String SET_WITH_EXPIRE_TIME = "PX";
    private static final String LOCK_PREFIX = "LOCK_PREFIX";

    private Jedis jedis;

    @PostConstruct
    public void initJedis() {
        jedis = jedisPool.getResource();
        jedis.setex("11", 10000, "22");

    }


    public boolean tryLock(String key, String request) {
        //                               String key,     String value,   String nxxx,      String expx,    int time
        String result = this.jedis.set(LOCK_PREFIX + key, request, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, 1000 * TIME);

        if (LOCK_MSG.equals(result)) {
            return true;
        } else {
            return false;
        }
    }


    public void lock(String key, String request) throws InterruptedException {

        for (; ; ) {
            String result = this.jedis.set(LOCK_PREFIX + key, request, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, 1000 * TIME);
            if (LOCK_MSG.equals(result)) {
                break;
            }

            //防止一直消耗 CPU
            Thread.sleep(DEFAULT_SLEEP_TIME);
        }

    }

    //自定义阻塞时间
    public boolean lock(String key, String request, int blockTime) throws InterruptedException {

        while (blockTime >= 0) {

            String result = this.jedis.set(LOCK_PREFIX + key, request, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, 10 * TIME);
            if (LOCK_MSG.equals(result)) {
                return true;
            }
            blockTime -= DEFAULT_SLEEP_TIME;

            Thread.sleep(DEFAULT_SLEEP_TIME);
        }
        return false;
    }

    public boolean unlock(String key, String request) {
        //lua script                           key集合     参数集合所以下面时两个    Collections
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        //上面的语句含义就是获取第一个key的值 如果等于第一个参数 就执行删除第一个key 不然什么都不做
        Object result = null;
        if (jedis instanceof Jedis) {
            result = ((Jedis) this.jedis).eval(script, Collections.singletonList(LOCK_PREFIX + key), Collections.singletonList(request));
//        }else if (jedis instanceof JedisCluster){
//            result = ((JedisCluster)this.jedis).eval(script, Collections.singletonList(LOCK_PREFIX + key), Collections.singletonList(request));
        } else {
            //throw new RuntimeException("instance is error") ;
            return false;
        }

        if (UNLOCK_MSG.equals(result)) {
            return true;
        } else {
            return false;
        }
    }

}
