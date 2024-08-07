package com.pikaqiu.java8;

import java.util.concurrent.CompletableFuture;

public class CompletableFutureTest {
    public static void main(String[] args) {
        // 创建任务 T2 的 FutureTask
        java.util.concurrent.CompletableFuture<String> ft2 = CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("T2: 洗茶壶...");
                Thread.sleep(1000);
                System.out.println("T2: 洗茶杯...");
                Thread.sleep(2000);
                System.out.println("T2: 拿茶叶...");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return " 龙井 ";
        });
        // 创建任务 T1 的 FutureTask
        java.util.concurrent.CompletableFuture<String> ft1 = CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("T1: 洗水壶...");
                Thread.sleep(1000);
                System.out.println("T1: 烧开水...");
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return " 烧开水 ";
        }).thenCombine(ft2, (t, tf) -> {
            System.out.println("T1: 拿到茶叶:" + tf);
            System.out.println("T1: 泡茶...");
            return " 上茶:" + tf;
        });
        // 等待线程 T1 执行结果
        System.out.println(ft1.join());


        //任务1 洗水壶 烧开水
        CompletableFuture<Void> f1 = CompletableFuture.runAsync(() -> {
            try {
                System.out.println("T1: 洗水壶...");
                Thread.sleep(1000);
                System.out.println("T1: 烧开水...");
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        //任务2 洗茶壶 洗茶杯 拿茶叶
        CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("T2: 洗茶壶...");
                Thread.sleep(1000);
                System.out.println("T2: 洗茶杯...");
                Thread.sleep(2000);
                System.out.println("T2: 拿茶叶...");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return " 龙井 ";
        });
        //任务3 拿到茶叶 泡茶
        CompletableFuture<String> f3 = f1.thenCombine(f2, (__, tf) -> {
            System.out.println("T1: 拿到茶叶:" + tf);
            System.out.println("T1: 泡茶...");
            return " 上茶:" + tf;
        });
        //等待任务3执行结果
        System.out.println(f3.join());
    }
}
