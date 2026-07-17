package com.pikaqiu.lottery.enums;

/**
 * 抽奖结果状态。
 *
 * @author xiaoye
 */
public enum DrawStatus {

    /** 中奖 */
    WIN("中奖"),

    /** 未中奖 */
    NOT_WIN("未中奖"),

    /** 活动未开始 */
    NOT_STARTED("活动未开始"),

    /** 活动已结束 */
    ENDED("活动已结束"),

    /** 抽奖次数已用完 */
    LIMIT_EXCEEDED("抽奖次数已用完"),

    /** 活动不存在 */
    ACTIVITY_NOT_FOUND("活动不存在");

    private final String desc;

    DrawStatus(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
