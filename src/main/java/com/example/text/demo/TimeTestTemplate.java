package com.example.text.demo;

import com.example.text.TextApplication;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StopWatch;


/**
 * @program: text
 * @description:
 * @author: xiaoye
 * @create: 2019-02-21 20:15
 **/
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TextApplication.class)
public abstract class TimeTestTemplate {

    /**
     * 计时方法
     */
    public void countTime() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        doSomeThing();
        stopWatch.stop();
    }

    /**
     * 实际做的事情
     */
    public abstract void doSomeThing();
}
