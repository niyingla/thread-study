package com.pikaqiu.demo;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @program: text
 * @description:
 * @author: xiaoye
 * @create: 2018-11-08 11:24
 **/
public class StopThread implements Runnable {
    @Override
    public void run() {

        //isInterrupted 只是一个标志 可以跨线程调用
        while ( !Thread.currentThread().isInterrupted()) {
            // 线程执行具体逻辑
            System.out.println(Thread.currentThread().getName() + "运行中。。");
        }

        System.out.println(Thread.currentThread().getName() + "退出。。");

    }

    public static void main(String[] args) throws Exception {

        /*Thread thread = new Thread(new StopThread(), "thread A");
        thread.start();

        System.out.println("main 线程正在运行") ;

        TimeUnit.MILLISECONDS.sleep(10) ;
        thread.interrupt();
        */

        executorService();

    }



    private static void executorService() throws Exception{

        //由于LinkedBlockingQueue实现是线程安全的，实现了先进先出等特性，是作为生产者消费者的首选，LinkedBlockingQueue 可以指定容量，
        // 也可以不指定，不指定的话，默认最大是Integer.MAX_VALUE，其中主要用到put和take方法，put方法在队列满的时候会阻塞直到有队列成员被消费，
        // take方法在队列空的时候会阻塞，直到有队列成员被放进来。
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(10) ;

        // corePoolSize
        //
        //核心线程数，默认情况下核心线程会一直存活，即使处于闲置状态也不会受存keepAliveTime限制。除非将allowCoreThreadTimeOut设置为true。
        //
        //maximumPoolSize
        //
        //线程池所能容纳的最大线程数。超过这个数的线程将被阻塞。当任务队列为没有设置大小的LinkedBlockingDeque时，这个值无效。
        //
        //keepAliveTime
        //
        //非核心线程的闲置超时时间，超过这个时间就会被回收。
        //
        //unit
        //
        //指定keepAliveTime的单位，如TimeUnit.SECONDS。当将allowCoreThreadTimeOut设置为true时对corePoolSize生效。
        //
        //workQueue
        //
        //线程池中的任务队列.
        //
        //常用的有三种队列，SynchronousQueue,LinkedBlockingDeque,ArrayBlockingQueue。

        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(5,10,1, TimeUnit.MILLISECONDS,queue) ;

        poolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                    System.out.println("running");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        poolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    System.out.println("running2");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        //shutDown 不在接受新的线程，并且等待之前提交的线程都执行完在关闭，
        //shutDownNow 直接关闭活跃状态的所有的线程 ， 并返回等待中的线程
        poolExecutor.shutdown();
        //awaitTermination()是阻塞的，返回结果是线程池是否已停止（true/false）（可以传入阻塞时间）；shutdown()不阻塞。
        while (!poolExecutor.awaitTermination(1,TimeUnit.SECONDS)){
            System.out.println("线程还在执行。。。");
        }
        System.out.println("main over");
    }

}