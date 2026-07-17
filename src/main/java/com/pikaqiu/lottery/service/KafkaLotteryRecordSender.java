package com.pikaqiu.lottery.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pikaqiu.lottery.common.LotteryConstants;
import com.pikaqiu.lottery.dto.WinRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 中奖记录投递的 Kafka 实现:抽奖命中后把中奖事件发到 MQ,消费者异步落库+发奖。
 * 用 lottery.record.sender=kafka 启用。
 *
 * @author xiaoye
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "lottery.record.sender", havingValue = "kafka")
public class KafkaLotteryRecordSender implements LotteryRecordSender {

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void send(WinRecord record) {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(record);
        } catch (JsonProcessingException e) {
            log.error("[lottery-record] 中奖消息序列化失败, record={}", record, e);
            return;
        }
        // key = userId:同一用户的记录进同一分区、保序;热点活动也能打散到多分区并行消费
        kafkaTemplate.send(LotteryConstants.TOPIC_WIN_RECORD, record.getUserId(), payload)
                .addCallback(
                        result -> {
                        },
                        ex -> log.error("[lottery-record] 中奖消息发送失败, record={}", record, ex));
    }
}
