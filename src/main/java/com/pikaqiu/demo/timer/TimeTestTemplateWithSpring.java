package com.pikaqiu.demo.timer;

import com.pikaqiu.TextApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
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
@Slf4j
public abstract class TimeTestTemplateWithSpring {

    /**
     * 计时方法
     */
    @Test
    public void countTime() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        doSomeThing();
        stopWatch.stop();
        log.info(" total cost: " + stopWatch.getTotalTimeMillis());
    }

    /**
     * 实际做的事情 需要子类实现
     */
    public abstract void doSomeThing();

}
