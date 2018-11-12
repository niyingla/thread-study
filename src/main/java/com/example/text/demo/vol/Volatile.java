package com.example.text.demo.vol;

import java.util.Scanner;

/**
 * @program:
 * 由于 `Java` 内存模型(`JMM`)规定，所有的变量都存放在主内存中，而每个线程都有着自己的工作内存(高速缓存)。
 * 线程在工作时，需要将主内存中的数据拷贝到工作内存中。这样对数据的任何操作都是基于工作内存(效率提高)，
 * 并且不能直接操作主内存以及其他线程工作内存中的数据，之后再将更新之后的数据刷新到主内存中。
 * 普通操作 1 复制变量到线程工作内存 2 线程进行工作 3 工作完成 数据刷新到主内存中
 * volatile操作 1 复制变量到线程工作内存 2 线程进行工作 3 更新变量时 试试更新每个线程中的变量
 * @description:
 * @author: xiaoye
 * @create: 2018-11-12 14:25
 **/



public class Volatile implements Runnable {

    //volatile关键字可以让线程变量同步 变量被修改时 会同步更新其他线程中的

    //不加只是会复制变量到从线程

    private static volatile boolean flag = true;

    @Override
    public void run() {
        while (flag) {
        }
        System.out.println(Thread.currentThread().getName() + "执行完毕");
    }

    public static void main(String[] args) throws InterruptedException {

        Volatile aVolatile = new Volatile();

        new Thread(aVolatile, "thread A").start();

        System.out.println("main 线程正在运行");

        Scanner sc = new Scanner(System.in);

        while (sc.hasNext()) {
            String value = sc.next();
            if (value.equals("1")) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        aVolatile.stopThread();
                    }
                }).start();

                break;
            }
        }

        System.out.println("主线程退出了！");

    }

    private void stopThread() {
        flag = false;
    }

}
