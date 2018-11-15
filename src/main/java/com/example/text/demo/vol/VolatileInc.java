package com.example.text.demo.vol;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @program: text
 * @description:
 * @author: xiaoye
 * @create: 2018-11-12 14:51
 **/
public class VolatileInc implements Runnable {

    //   private static volatile int count = 0; //使用 volatile 修饰基本数据内存不能保证原子性

    /**
     * 使结果最终一致性
     */
    private static volatile AtomicInteger count = new AtomicInteger();

    @Override
    public void run() {
        for (int i = 0; i < 10000; i++) {
//            count++;
            count.incrementAndGet();
        }
        System.out.println(count);

    }

    public static void main(String[] args) throws InterruptedException {
        VolatileInc volatileInc = new VolatileInc();
        Thread t1 = new Thread(volatileInc, "t1");
        Thread t2 = new Thread(volatileInc, "t2");
        t1.start();
        //t1.join();

        t2.start();
        //t2.join();
        for (int i = 0; i < 10000; i++) {
//            count++;
            count.incrementAndGet();
        }

        System.out.println(count);

//        System.out.println("最终Count="+count);

    }
}