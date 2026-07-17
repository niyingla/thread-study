package com.pikaqiu.lottery.dto;

import lombok.Data;

import java.util.List;

/**
 * 活动配置(应用层缓存对象)。
 * 由 {@link com.pikaqiu.lottery.loader.ActivityConfigLoader} 从 DB 加载后缓存,抽奖时随脚本一起下发,
 * 使 Lua 脚本只负责运行态(库存/限次)的原子变更。
 *
 * @author xiaoye
 */
@Data
public class ActivityConfig {

    /** 活动ID */
    private Long actId;

    /** 活动开始时间(毫秒时间戳) */
    private Long startTime;

    /** 活动结束时间(毫秒时间戳) */
    private Long endTime;

    /** 时间片长度(毫秒),用于奖品均匀发放 */
    private Long sliceMs;

    /** 总时间片数,= ceil((endTime - startTime) / sliceMs) */
    private Integer totalSlices;

    /** 每人限抽次数(总) */
    private Integer userLimit;

    /** 奖品列表,已按 priority 从大到小排序 */
    private List<PrizeConfig> prizes;
}
