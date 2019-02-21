package com.example.text;

import com.example.text.demo.vol.Lock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StopWatch;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    public void contextLoads() {
        this.t2();
    }
}