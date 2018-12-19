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
        boolean tryLock = lock.tryLock("111", "222");
        System.out.println(tryLock);
        boolean unlock = lock.unlock("111", "222");
        System.out.println(unlock);
    }

}
