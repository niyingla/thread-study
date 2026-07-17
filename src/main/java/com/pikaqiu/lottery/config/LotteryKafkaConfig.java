package com.pikaqiu.lottery.config;

import com.pikaqiu.lottery.common.LotteryConstants;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 抽奖 Kafka 链路配置:启动时自动建 topic(多分区支撑并发消费),并开启定时能力供对账任务使用。
 * 仅在 lottery.record.sender=kafka 时生效,避免无外部依赖场景加载多余基础设施。
 *
 * @author xiaoye
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "lottery.record.sender", havingValue = "kafka")
public class LotteryKafkaConfig {

    /** 中奖记录 topic:6 分区,单副本(生产按集群规模调副本数) */
    @Bean
    public NewTopic lotteryWinRecordTopic() {
        return TopicBuilder.name(LotteryConstants.TOPIC_WIN_RECORD)
                .partitions(6)
                .replicas(1)
                .build();
    }
}
