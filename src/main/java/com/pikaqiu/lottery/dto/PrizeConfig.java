package com.pikaqiu.lottery.dto;

import lombok.Data;

/**
 * 奖品配置。
 *
 * @author xiaoye
 */
@Data
public class PrizeConfig {

    /** 奖品ID */
    private Long prizeId;

    /** 奖品名称 */
    private String prizeName;

    /** 独立中奖概率(百万分之),如 5000 表示 0.5% */
    private Integer probPpm;

    /** 奖品总数量 */
    private Long totalCount;

    /** 优先级,越大越先掷(高价值奖品在前) */
    private Integer priority;
}
