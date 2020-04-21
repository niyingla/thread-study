package com.pikaqiu;

import com.pikaqiu.demo.vol.Lock;
import com.pikaqiu.demo.vol.RedisLock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StopWatch;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TextApplication.class)
public class TextApplicationTests {

    @Autowired
    private Lock lock;

    /**
     * 获取容器中同一类型对象
     */
    @Autowired
    private Map<String, Lock> lockMap;

    /**
     * 测试超时删锁
     */
    @Test
    public void ti() {
        //获取锁
        boolean tryLock = this.lock.tryLock("11", "222");
        System.out.println(tryLock);
        //重置锁
        try {
            boolean lock = this.lock.lock("11", "2222", 100000);
            System.out.println(lock);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //删除第一个锁
        boolean unlock = lock.unlock("11", "222");
        System.out.println(unlock);
    }

    /**
     * 每秒大概50个 上锁删除锁
     *
     *  配置正常大概能有500-600个/s
     */
    @Test
    public void t2() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (int i = 0; i < 1000; i++) {
            //获取锁
            boolean tryLock = lock.tryLock("11", "222");
            System.out.println(tryLock);
            //删除第一个锁
            boolean unlock = lock.unlock("11", "222");
            System.out.println(unlock);
        }
        stopWatch.stop();
        System.out.println("总共费时：" + stopWatch.getTotalTimeMillis()+" 毫秒");
    }

    /**
     * 测试map类型注入
     */
    @Test
    public void t3(){
        int size = lockMap.size();
        System.out.println(size);
    }


    @Test
    public void ti111() throws InterruptedException {
        for (int i = 0; i < 5000; i++) {
            ti222();
        }
    }


    public void ti222() throws InterruptedException {
        RedisLock redisLock1 = new RedisLock("lock");
        //获取锁
        boolean tryLock = redisLock1.tryLock(1000, TimeUnit.MILLISECONDS);
        System.out.println(tryLock);
        //重置锁
        RedisLock redisLock2 = new RedisLock("lock");
        boolean lock = redisLock2.tryLock();
        System.out.println(lock);

        //删除第一个锁
        redisLock1.unlock();

        //获取锁
        RedisLock redisLock4 = new RedisLock("lock");
        boolean tryLock4 = redisLock4.tryLock(1000,TimeUnit.MILLISECONDS);
        System.out.println(tryLock4);
    }
    @Test
    public void contextLoads() {
        this.t2();
    }
}
