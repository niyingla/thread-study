package com.pikaqiu.lottery.service;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.pikaqiu.lottery.dto.WinRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 中奖记录投递的本地实现(学习/无外部依赖场景):线程池异步打日志代替"落库 + 发奖"。
 * 生产用 {@link KafkaLotteryRecordSender},切换开关 lottery.record.sender=kafka。
 *
 * @author xiaoye
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "lottery.record.sender", havingValue = "log", matchIfMissing = true)
public class LogLotteryRecordSender implements LotteryRecordSender {

    /**
     * 异步处理线程池。按阿里规约手动构造,拒绝策略用 CallerRuns 兜底,
     * 队列满时退化为调用线程同步执行,保证不丢中奖记录。
     */
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            2, 4, 60L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(2048),
            new ThreadFactoryBuilder().setNameFormat("lottery-record-%d").setDaemon(true).build(),
            new ThreadPoolExecutor.CallerRunsPolicy());

    @Override
    public void send(WinRecord record) {
        executor.execute(() -> doSend(record));
    }

    private void doSend(WinRecord record) {
        // TODO 生产:1) INSERT t_lottery_record(uk_act_request 幂等) 2) 调发券/加积分 3) 更新 send_status
        log.info("[lottery-record] 中奖落库&发奖, actId={}, userId={}, prizeId={}, prizeName={}, requestId={}",
                record.getActId(), record.getUserId(), record.getPrizeId(),
                record.getPrizeName(), record.getRequestId());
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }
}
