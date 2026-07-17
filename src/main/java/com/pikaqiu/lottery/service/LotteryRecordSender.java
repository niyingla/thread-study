package com.pikaqiu.lottery.service;

import com.pikaqiu.lottery.dto.WinRecord;

/**
 * 中奖记录投递。抽奖主流程只把中奖事件交给它就立即返回,落库/发奖异步完成。
 * 生产实现:发 Kafka(actId 做 key 保证同用户有序),消费者按 request_id 幂等落库 + 调发券接口。
 *
 * @author xiaoye
 */
public interface LotteryRecordSender {

    /**
     * 投递一条中奖记录(异步、不阻塞抽奖主流程)。
     *
     * @param record 中奖记录
     */
    void send(WinRecord record);
}
