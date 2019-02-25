package com.example.text.demo.timer;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.util.StopWatch;


/**
 * @program: text
 * @description:
 * @author: xiaoye
 * @create: 2019-02-21 20:15
 **/
@Slf4j
public abstract class TimeTestTemplate {

    /**
     * 计时方法
     */
    @Test
    public void countTime() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        doSomeThing();
        stopWatch.stop();
        log.info(" total cost : " + stopWatch.getTotalTimeMillis());
    }

    /**
     * 实际做的事情 需要子类实现
     */
    public abstract void doSomeThing();

}
