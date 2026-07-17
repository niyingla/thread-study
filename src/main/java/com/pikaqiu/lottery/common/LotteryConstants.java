package com.pikaqiu.lottery.common;

/**
 * 抽奖常量。
 *
 * @author xiaoye
 */
public final class LotteryConstants {

    private LotteryConstants() {
    }

    /** 中奖记录 topic */
    public static final String TOPIC_WIN_RECORD = "lottery-win-record";

    /** 中奖记录消费组 */
    public static final String RECORD_CONSUMER_GROUP = "lottery-record-consumer";

    /** 发奖状态:待发放 */
    public static final int SEND_PENDING = 0;

    /** 发奖状态:已发放 */
    public static final int SEND_SUCCESS = 1;

    /** 发奖状态:发放失败 */
    public static final int SEND_FAIL = 2;
}
