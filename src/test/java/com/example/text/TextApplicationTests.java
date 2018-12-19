package com.example.text;

import com.example.text.demo.vol.Lock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TextApplication.class)
public class TextApplicationTests {

    @Autowired
    private Lock lock;

    @Test
    public void contextLoads() {
        //获取锁
        boolean tryLock = lock.tryLock("11", "222");
        System.out.println(tryLock);
        //重置锁
        try {
            lock.lock("11", "2222",100000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //删除第一个锁
        boolean unlock = lock.unlock("11", "222");
        System.out.println(unlock);
    }

}
