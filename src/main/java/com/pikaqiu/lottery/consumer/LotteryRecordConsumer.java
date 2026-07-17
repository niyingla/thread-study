package com.pikaqiu.lottery.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pikaqiu.lottery.common.LotteryConstants;
import com.pikaqiu.lottery.dao.LotteryRecordDao;
import com.pikaqiu.lottery.dto.WinRecord;
import com.pikaqiu.lottery.service.LotteryFulfillmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 中奖记录消费者:幂等落库 + 触发发奖。
 * 只负责"落库 + 幂等判断",发奖履约交给 {@link LotteryFulfillmentService}。
 *
 * @author xiaoye
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "lottery.record.sender", havingValue = "kafka")
public class LotteryRecordConsumer {

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private LotteryRecordDao lotteryRecordDao;

    @Resource
    private LotteryFulfillmentService lotteryFulfillmentService;

    @KafkaListener(topics = LotteryConstants.TOPIC_WIN_RECORD, groupId = LotteryConstants.RECORD_CONSUMER_GROUP)
    public void onMessage(String message) {
        WinRecord record = parse(message);
        if (record == null) {
            return;
        }
        try {
            int inserted = lotteryRecordDao.insertIgnore(record);
            // 已存在且已发放 -> 幂等跳过;新插入 或 已存在但未发放 -> 继续发奖
            if (inserted == 0 && lotteryRecordDao.isGranted(record.getActId(), record.getRequestId())) {
                return;
            }
            lotteryFulfillmentService.fulfill(record);
        } catch (Exception e) {
            log.error("[lottery-record] 消费中奖消息异常, record={}", record, e);
        }
    }

    /**
     * 反序列化消息;失败返回 null(丢弃,避免 poison 消息阻塞分区)。
     */
    private WinRecord parse(String message) {
        try {
            return objectMapper.readValue(message, WinRecord.class);
        } catch (Exception e) {
            log.error("[lottery-record] 消息反序列化失败,丢弃, message={}", message, e);
            return null;
        }
    }
}
