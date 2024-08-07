package com.pikaqiu.java8;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class FutureTaskTest {
    public static void main(String[] args) throws Exception {
        // 创建任务 T2 的 FutureTask
        FutureTask<String> ft2 = new FutureTask<>(new T2Task());
        // 创建任务 T1 的 FutureTask
        FutureTask<String> ft1 = new FutureTask<>(new T1Task(ft2));
        // 线程 T1 执行任务 ft1
        Thread T1 = new Thread(ft1);
        T1.start();
        // 线程 T2 执行任务 ft2
        Thread T2 = new Thread(ft2);
        T2.start();
        // 等待线程 T1 执行结果
        System.out.println(ft1.get());
    }
}

 class T1Task implements Callable<String> {
    FutureTask<String> ft2;

    public T1Task(FutureTask<String> ft2) {
        this.ft2 = ft2;
    }

    @Override
    public String call() throws Exception {
        System.out.println("T1: 洗水壶...");
        Thread.sleep(1000);
        System.out.println("T1: 烧开水...");
        Thread.sleep(15000);
        // 获取 T2 线程的茶叶
        String tf = ft2.get();
        System.out.println("T1: 拿到茶叶:" + tf);

        System.out.println("T1: 泡茶...");
        return " 上茶:" + tf;
    }


}

class T2Task implements Callable<String> {
    @Override
    public String call() throws Exception {
        System.out.println("T2: 洗茶壶...");
        Thread.sleep(1000);
        System.out.println("T2: 洗茶杯...");
        Thread.sleep(2000);
        System.out.println("T2: 拿茶叶...");
        Thread.sleep(1000);
        return " 龙井 ";
    }
}