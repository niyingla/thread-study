package com.pikaqiu.lottery.loader;

import com.pikaqiu.lottery.dto.ActivityConfig;

/**
 * 活动配置加载器。生产实现从 DB 读取 t_lottery_activity + t_lottery_prize。
 *
 * @author xiaoye
 */
public interface ActivityConfigLoader {

    /**
     * 加载活动配置。
     *
     * @param actId 活动ID
     * @return 活动配置,不存在返回 null
     */
    ActivityConfig load(Long actId);
}
