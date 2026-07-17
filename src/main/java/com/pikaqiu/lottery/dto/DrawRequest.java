package com.pikaqiu.lottery.dto;

import lombok.Data;

/**
 * 抽奖请求。
 *
 * @author xiaoye
 */
@Data
public class DrawRequest {

    /** 活动ID */
    private Long actId;

    /** 用户ID */
    private String userId;

    /**
     * 请求ID,幂等用。同一逻辑抽奖必须用同一 requestId(客户端生成),
     * 重试时可保证不重复扣次数/不重复中奖。为空时服务端会生成随机值(即不做去重)。
     */
    private String requestId;
}
