package com.example.text.demo.timer;

import org.junit.Test;

/**
 * @program: thread-study
 * @description:
 * @author: xiaoye
 * @create: 2019-02-25 10:51
 **/
public class 测试 extends TimeTestTemplateWithSpring{

    /**
     * 实现需要测试计时的方法
     */
    @Override
    public void doSomeThing() {
        try {
            Thread.sleep(5000);
        }catch (Exception e){

        }
    }

    /**
     * 调用测试方法
     */
    @Test
    public void zuo(){
        super.countTime();
    }
}
