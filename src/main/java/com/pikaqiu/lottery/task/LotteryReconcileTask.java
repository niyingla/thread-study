package com.pikaqiu.lottery.task;

import com.pikaqiu.lottery.dao.LotteryRecordDao;
import com.pikaqiu.lottery.dto.WinRecord;
import com.pikaqiu.lottery.service.LotteryFulfillmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 发奖对账/补偿任务:定时扫描 send_status ≠ 已发放(待发放/失败)的中奖记录并重新发放,
 * 兜住"消费者插库后未发奖就宕机""发奖接口临时故障"等漏发场景。
 * 依赖 Kafka 落库链路,用 lottery.record.sender=kafka 启用。
 *
 * <p>集群注意:多实例都会跑本任务。fulfill 已按 requestId 幂等,重复补偿安全;
 * 生产建议再加分布式锁(如 Redis SETNX)或交 XXL-JOB 单实例调度,减少无谓重复。
 *
 * @author xiaoye
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "lottery.record.sender", havingValue = "kafka")
public class LotteryReconcileTask {

    /** 单批处理的最大记录数 */
    private static final int BATCH_SIZE = 500;

    /** 只补偿创建超过该时长的记录,避开正在被消费者处理的在途消息 */
    private static final long SETTLE_DELAY_MS = TimeUnit.MINUTES.toMillis(5);

    @Resource
    private LotteryRecordDao lotteryRecordDao;

    @Resource
    private LotteryFulfillmentService lotteryFulfillmentService;

    /**
     * 定时补偿。默认每分钟一次,可用 lottery.reconcile.interval-ms 覆盖。
     */
    @Scheduled(fixedDelayString = "${lottery.reconcile.interval-ms:60000}")
    public void reconcile() {
        Timestamp createdBefore = new Timestamp(System.currentTimeMillis() - SETTLE_DELAY_MS);
        List<WinRecord> unfinished = lotteryRecordDao.listUnfinished(createdBefore, BATCH_SIZE);
        if (CollectionUtils.isEmpty(unfinished)) {
            return;
        }
        int success = 0;
        for (WinRecord record : unfinished) {
            if (lotteryFulfillmentService.fulfill(record)) {
                success++;
            }
        }
        log.info("[lottery-reconcile] 补偿完成, total={}, success={}", unfinished.size(), success);
    }
}
