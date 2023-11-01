package com.pikaqiu.utils;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;

import java.util.concurrent.TimeUnit;

/**
 * <p> sss </p>
 *
 * @author xiaoye
 * @version 1.0
 * @date 2023/10/17 9:35
 */
public class HashedWheelTimerTest {
    static class MyTimerTask implements TimerTask {

       volatile boolean flag;

        public MyTimerTask(boolean flag) {
            this.flag = flag;
        }


        @Override
        public void run(Timeout timeout) throws Exception {
            // TODO Auto-generated method stub
            System.out.println("要去数据库删除订单了。。。。");
            this.flag = false;
        }
    }

    public static void main(String[] argv) {
        MyTimerTask timerTask = new MyTimerTask(true);
        Timer timer = new HashedWheelTimer();
        timer.newTimeout(timerTask, 5, TimeUnit.SECONDS);
        timer.newTimeout(timerTask, 10, TimeUnit.SECONDS);
        int i = 1;
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.println(i + "秒过去了");
            i++;
        }
    }
}
