package com.pikaqiu.lottery.dto;

import lombok.Data;

/**
 * 中奖记录(异步落库/发奖的消息体)。
 *
 * @author xiaoye
 */
@Data
public class WinRecord {

    /** 活动ID */
    private Long actId;

    /** 用户ID */
    private String userId;

    /** 中奖奖品ID */
    private Long prizeId;

    /** 奖品名称 */
    private String prizeName;

    /** 抽奖请求ID,落库幂等用 */
    private String requestId;

    /** 中奖时间(毫秒时间戳) */
    private Long drawTime;
}
