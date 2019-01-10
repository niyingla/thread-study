package com.example.text.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @program: text
 * @description: 线程池配置
 * @author: xiaoye
 * @create: 2019-01-10 16:31
 **/
@EnableAsync
@Configuration
class TaskPoolConfig {

    /**
     * 核心线程数10：线程池创建时候初始化的线程数
     * 最大线程数20：线程池最大的线程数，只有在缓冲队列满了之后才会申请超过核心线程数的线程
     * 缓冲队列200：用来缓冲执行任务的队列
     * 允许线程的空闲时间60秒：当超过了核心线程出之外的线程在空闲时间到达之后会被销毁
     * 线程池名的前缀：设置好了之后可以方便我们定位处理任务所在的线程池
     * 线程池对拒绝任务的处理策略：这里采用了CallerRunsPolicy策略，当线程池没有处理能力的时候，
     * 该策略会直接在 execute 方法的调用线程中运行被拒绝的任务；如果执行程序已关闭，则会丢弃该任务
     * @return
     *
     * 调用时指定对应的 Bean名称
     * @Async("taskExecutor")
     */
    @Bean("taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("taskExecutor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }
    /**
     * Future接口的五个方法
     *
     * cancel方法用来取消任务，如果取消任务成功则返回true，如果取消任务失败则返回false。
     * 参数mayInterruptIfRunning表示是否允许取消正在执行却没有执行完毕的任务，如果设置true，
     * 则表示可以取消正在执行过程中的任务。如果任务已经完成，则无论mayInterruptIfRunning为true还是false，
     * 此方法肯定返回false，即如果取消已经完成的任务会返回false；如果任务正在执行，若mayInterruptIfRunning设置为true，
     * 则返回true，若mayInterruptIfRunning设置为false，则返回false；
     * 如果任务还没有执行，则无论mayInterruptIfRunning为true还是false，肯定返回true。
     *
     * isCancelled方法表示任务是否被取消成功，如果在任务正常完成前被取消成功，则返回 true。
     *
     * isDone方法表示任务是否已经完成，若任务完成，则返回true；
     *
     * get()方法用来获取执行结果，这个方法会产生阻塞，会一直等到任务执行完毕才返回；
     *
     * get(long timeout, TimeUnit unit)用来获取执行结果，如果在指定时间内，还没获取到结果，就直接返回null。
     */
}