package com.pikaqiu.log;

import java.util.concurrent.CompletableFuture;

/**
 * <p> ParallelExecutionExample </p>
 *
 * @author xiaoye
 * @version 1.0
 * @date 2023/11/2 20:09
 */
public class ParallelExecutionExample {
    public static void main(String[] args) {
        // 创建第一个任务
        CompletableFuture<String> task1 = CompletableFuture.supplyAsync(() -> {
            // 执行任务1的逻辑
            // 这里只是一个示例，可以根据实际需求编写具体的任务逻辑
            return "任务1执行结果";
        });

        // 创建第二个任务
        CompletableFuture<Object> task2 = CompletableFuture.supplyAsync(() -> {
            // 执行任务2的逻辑
            // 这里只是一个示例，可以根据实际需求编写具体的任务逻辑
            throw new RuntimeException("任务2执行出错");
        }).exceptionally(e -> {
            // 处理任务2的异常
            System.out.println("任务2执行出错：" + e.getMessage());
            return "任务2执行出错";
        });

        // 等待两个任务都执行完毕
        CompletableFuture<Void> allTasks = CompletableFuture.allOf(task1, task2);

        // 在所有任务执行完毕后获取结果并处理异常
        allTasks.thenRun(() -> {
            try {
                // 获取任务1的执行结果
                String result1 = task1.get();
                System.out.println("任务1执行结果：" + result1);

                // 获取任务2的执行结果
                Object result2 = task2.get();
                System.out.println("任务2执行结果：" + result2);
            } catch (Exception e) {
                // 处理异常
                System.out.println("任务执行出错：" + e.getMessage());
            }
        });

        // 等待所有任务执行完毕
        allTasks.join();
    }
}
