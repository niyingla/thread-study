package com.pikaqiu.demo;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * <p> CompletableFutureTest </p>
 *
 * @author xiaoye
 * @version 1.0
 * @date 2023/5/10 9:25
 */
public class CompletableFutureTest {

    @Test
    public void apply() throws Exception {
        CompletableFuture<Integer> cf = CompletableFuture
                .supplyAsync(() -> 10)
                //apply 有入参和返回值参，为前置任务的输出
                .thenApplyAsync((e) -> {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    return e * 10;
                }).thenApplyAsync(e -> e - 1);

        //当一个方法同时没有join和get时,会在主线程执行完毕后直接结束
        cf.join();
        System.out.println(cf.get());
    }

    @Test
    public void accept() {
        CompletableFuture
                .supplyAsync(() -> 10)
                //accept 有入参无返回值，会返回 CompletableFuture
                .thenAccept((e) -> {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    System.out.println(e);
                });
        System.out.println(1);
    }

    /**
     * 当完成的时候执行whenComplete
     * @throws Exception
     */
    @Test
    public void whenComplete()throws Exception{
        CompletableFuture<Integer> cf = CompletableFuture
                .supplyAsync(() -> 10)
                .thenApplyAsync((e) -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    return e * 10;
                }).thenApplyAsync(e -> e - 1)
                //当完成的时候执行whenComplete
                .whenComplete((r, e) -> System.out.println("done"))
                //当异常的时候执行exceptionally
                .exceptionally(ex -> {
                    System.out.println(ex);
                    return -1;
                });
        cf.join();
        System.out.println(cf.get());
    }

    @Test
    public void allOfTask() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        //创建空的任务数组
        List<Integer> requests = Lists.newArrayList(1, 2, 3);
        List<CompletableFuture<Integer>> futureList = requests
                .stream()
                //构造任务,并将每个任务放入线程池
                .map(request -> CompletableFuture.supplyAsync(() -> request * 2, executor)
                        .whenComplete((r, e) -> System.out.println(r)))
                .collect(Collectors.toList());
        //返回值必须为空
        CompletableFuture<Void> allCF = CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]));
        //等待所有任务完成
        allCF.join();
    }

    /**
     * 两个任务都完成后执行
     * @throws Exception
     */
    @Test
    public void thenAcceptBoth() throws Exception {
        //处理两个任务的情况，有两个任务结果入参，无返回值
        CompletableFuture.supplyAsync(() -> 10)
                .thenAcceptBoth(CompletableFuture.supplyAsync(() -> 20), (a, b) -> System.out.println(a + b));
        //处理两个任务的情况，有两个任务结果入参，有返回值
        Integer result = CompletableFuture.supplyAsync(() -> 10)
                .thenCombine(CompletableFuture.supplyAsync(() -> 20), (a, b) -> a + b).get();
        System.out.println(result);
        //处理两个任务的情况，无入参，无返回值
        CompletableFuture.supplyAsync(() -> 10)
                .runAfterBoth(CompletableFuture.supplyAsync(() -> 20), () -> System.out.println("done"));
    }
}
