package com.example.text.demo;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

/**
 * @program: text
 * @description:因为 Java 是采用共享内存的方式进行线程通信的，所以可以采用以下方式用主线程关闭 A 线程:
 * Java语言提供了一种稍弱的同步机制，即volatile变量，用来确保将变量的更新操作通知到其他线程。
 * 当把变量声明为volatile类型后，编译器与运行时都会注意到这个变量是共享的，因此不会将该变量上的操作与其他内存操作一起重排序。
 * volatile变量不会被缓存在寄存器或者对其他处理器不可见的地方，因此在读取volatile类型的变量时总会返回最新写入的值。
 * 在访问volatile变量时不会执行加锁操作，因此也就不会使执行线程阻塞，因此volatile变量是一种比sychronized关键字更轻量级的同步机制。
 * @author: xiaoye
 * @create: 2018-11-08 10:40
 **/
@Slf4j
public class Volatile implements Runnable {

    /**
     * 加不加结果一样
     * 但是加了之后内存响应更稳定迅速
     */
    private static volatile boolean flag = true;

    @Override
    public void run() {
        while (flag) {
            System.out.println(Thread.currentThread().getName() + "正在运行。。。");
        }
        System.out.println(Thread.currentThread().getName() + "执行完毕");
    }

    public static void main(String[] args) throws Exception {
        /*Volatile aVolatile = new Volatile();

        new Thread(aVolatile, "thread A").start();

        System.out.println("main 线程正在运行");

        TimeUnit.MILLISECONDS.sleep(1);

        aVolatile.stopThread();*/


//        countDownLatch();

        cyclicBarrier();
    }

//----------------------------------------countDownLatch-----------------------------------------------
    /**
     * 利用它可以实现类似计数器的功能。比如有一个任务A，它要等待其他4个任务执行完毕之后才能执行，此时就可以利用CountDownLatch来实现这种功能了。
     *
     * @throws Exception
     */
    private static void countDownLatch() throws Exception {
        int thread = 3;
        long start = System.currentTimeMillis();
        final CountDownLatch countDown = new CountDownLatch(thread);
        for (int i = 0; i < thread; i++) {
            new Thread(() -> {
                System.out.println("thread run");
                try {
                    Thread.sleep(2000);
                    countDown.countDown();

                    System.out.println("thread end");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
        //等待计数完毕才会继续执行
        countDown.await();
        long stop = System.currentTimeMillis();
        log.info("main over total time={}", stop - start);
    }

    private void stopThread() {
        flag = false;
    }

//---------------------------------cyclicBarrier--------------------------------------


    /**
     *
     * 功能描述: cyclicBarrier设置的人
     *到所有参与者都调用了 await() 后，所有线程从 await() 返回继续后续逻辑。
     * @param:
     * @return:
     * @auther: xiaoye
     * @date: 2018/11/8 13:52
     */
    private static void cyclicBarrier() throws Exception {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(3) ;

        new Thread(new Runnable() {
            @Override
            public void run() {
                log.info("thread run");
                try {
                    cyclicBarrier.await() ;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                log.info("thread end do something");
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                log.info("thread run");
                try {
                    cyclicBarrier.await() ;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                log.info("thread end do something");
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                log.info("thread run");
                try {
                    Thread.sleep(5000);
                    cyclicBarrier.await() ;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                log.info("thread end do something");
            }
        }).start();

        log.info("main thread");
    }
}