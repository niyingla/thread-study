package com.example.text.demo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @program: pikaqiu
 * @description: 多线程批量抓取数据
 * @author: xiaoye
 * @create: 2018-11-24 13:57
 **/
public class batchGet {

    public static void main(String[] args) {

        AtomicInteger atomicInteger = new AtomicInteger(0);

        //总记录数
        int count = 370;

        //默认线程数
        int threads = 7;

//        //建立线程集合
//        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(10);
//        //创建线程池  当任务个数超过queue时会抛异常
//        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(5, 100, 1, TimeUnit.MILLISECONDS, queue);

        //不限定等待长度的线程组
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        //判断是否还有数据没有取完
        while (count > 0) {

            //每次五个线程取10个数据
            count = count - threads * 10;

            //判断是否减多了
            if (count < 0) {
                threads = threads - Math.abs(Math.round(count / 10));
            }

            for (int i = 0; i < threads; i++) {
                //循环体检线程
                executorService.execute(() -> {
                    try {
                        Thread.sleep(3000);
                        int i1 = atomicInteger.addAndGet(1);
                        System.out.println("第" + i1 + "次取十条");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
        //等待结束
        executorService.shutdown();
    }
}
