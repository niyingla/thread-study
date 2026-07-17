package com.pikaqiu.lottery.service;

import com.pikaqiu.lottery.dto.WinRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 发奖服务(纯虚拟奖品:积分/优惠券)。
 * 生产:按奖品类型调积分服务/券服务的发放接口,且发放接口需以 requestId 做幂等,防止重复到账。
 *
 * @author xiaoye
 */
@Slf4j
@Service
public class PrizeGrantService {

    /**
     * 发放奖品。抛异常表示发放失败,由调用方置为失败状态、交对账补偿。
     *
     * @param record 中奖记录
     */
    public void grant(WinRecord record) {
        // TODO 生产:根据 prize_type 调 积分/券 服务发放接口(幂等键 = requestId)
        log.info("[lottery-grant] 发放虚拟奖品成功, userId={}, prizeId={}, prizeName={}, requestId={}",
                record.getUserId(), record.getPrizeId(), record.getPrizeName(), record.getRequestId());
    }
}
