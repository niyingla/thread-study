package com.pikaqiu.lottery.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.pikaqiu.lottery.dto.ActivityConfig;
import com.pikaqiu.lottery.dto.PrizeConfig;
import com.pikaqiu.lottery.loader.ActivityConfigLoader;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 活动配置服务:本地 Caffeine 缓存 + 定期回源,避免每次抽奖读 DB。
 * 集群下每个实例各自缓存,靠短 TTL(默认5分钟)达到最终一致,足以支撑抽奖场景。
 *
 * @author xiaoye
 */
@Service
public class LotteryConfigService {

    /** 配置缓存,key=actId */
    private final Cache<Long, ActivityConfig> configCache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    @Resource
    private ActivityConfigLoader activityConfigLoader;

    /**
     * 获取活动配置,命中缓存直接返回,未命中回源加载。
     *
     * @param actId 活动ID
     * @return 活动配置,不存在返回 null
     */
    public ActivityConfig getConfig(Long actId) {
        return configCache.get(actId, this::loadAndBuild);
    }

    /**
     * 预热/刷新:强制回源并写入缓存,活动上线或配置变更后调用。
     *
     * @param actId 活动ID
     * @return 活动配置,不存在返回 null
     */
    public ActivityConfig refresh(Long actId) {
        ActivityConfig config = loadAndBuild(actId);
        if (config != null) {
            configCache.put(actId, config);
        } else {
            configCache.invalidate(actId);
        }
        return config;
    }

    private ActivityConfig loadAndBuild(Long actId) {
        ActivityConfig config = activityConfigLoader.load(actId);
        if (config == null) {
            return null;
        }
        validateAndFill(config);
        return config;
    }

    /**
     * 校验配置合法性并补齐派生字段(总时间片数、奖品优先级排序)。
     */
    private void validateAndFill(ActivityConfig config) {
        Long start = config.getStartTime();
        Long end = config.getEndTime();
        Long sliceMs = config.getSliceMs();
        if (start == null || end == null || end <= start) {
            throw new IllegalArgumentException("活动时间配置非法, actId=" + config.getActId());
        }
        if (sliceMs == null || sliceMs <= 0) {
            throw new IllegalArgumentException("时间片长度配置非法, actId=" + config.getActId());
        }
        List<PrizeConfig> prizes = config.getPrizes();
        if (prizes == null || prizes.isEmpty()) {
            throw new IllegalArgumentException("活动未配置奖品, actId=" + config.getActId());
        }

        long duration = end - start;
        int totalSlices = (int) ((duration + sliceMs - 1) / sliceMs);
        config.setTotalSlices(Math.max(totalSlices, 1));

        // 按优先级从大到小排序,独立概率下先掷的奖品优先级更高
        prizes.sort(Comparator.comparingInt(PrizeConfig::getPriority).reversed());
    }
}
