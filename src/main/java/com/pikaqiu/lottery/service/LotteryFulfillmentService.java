package com.pikaqiu.lottery.service;

import com.pikaqiu.lottery.common.LotteryConstants;
import com.pikaqiu.lottery.dao.LotteryRecordDao;
import com.pikaqiu.lottery.dto.WinRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 发奖履约:调发放接口 + 回写发奖状态。
 * 由消费者(首次)和对账任务(补偿)共用,保证两条链路行为一致。
 *
 * @author xiaoye
 */
@Slf4j
@Service
public class LotteryFulfillmentService {

    @Resource
    private PrizeGrantService prizeGrantService;

    @Resource
    private LotteryRecordDao lotteryRecordDao;

    /**
     * 发奖并回写状态:成功置已发放,失败置发放失败。
     * 失败只记录不抛出,交由对账任务重试,避免消费链路 poison 循环。
     *
     * @param record 中奖记录
     * @return 是否发放成功
     */
    public boolean fulfill(WinRecord record) {
        try {
            prizeGrantService.grant(record);
            lotteryRecordDao.updateSendStatus(record.getActId(), record.getRequestId(),
                    LotteryConstants.SEND_SUCCESS);
            return true;
        } catch (Exception e) {
            lotteryRecordDao.updateSendStatus(record.getActId(), record.getRequestId(),
                    LotteryConstants.SEND_FAIL);
            log.error("[lottery-fulfill] 发奖失败, actId={}, requestId={}",
                    record.getActId(), record.getRequestId(), e);
            return false;
        }
    }
}
