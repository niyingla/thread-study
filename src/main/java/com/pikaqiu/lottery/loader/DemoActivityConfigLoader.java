package com.pikaqiu.lottery.loader;

import com.pikaqiu.lottery.dto.ActivityConfig;
import com.pikaqiu.lottery.dto.PrizeConfig;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 演示用配置加载器:actId=1 返回一个"当前正在进行、时长1小时"的活动,方便本地 curl 验证。
 * 生产环境请替换为读 DB 的实现(见类内注释)。
 *
 * @author xiaoye
 */
@Component
public class DemoActivityConfigLoader implements ActivityConfigLoader {

    private static final long DEMO_ACT_ID = 1L;

    @Override
    public ActivityConfig load(Long actId) {
        if (!Long.valueOf(DEMO_ACT_ID).equals(actId)) {
            return null;
        }

        // ===== 生产实现示意(用 JdbcTemplate / MyBatis 从库里读)=====
        // TLotteryActivity act = activityMapper.selectById(actId);
        // List<TLotteryPrize> prizes = prizeMapper.selectByActId(actId);
        // 然后组装成 ActivityConfig 并按 priority 倒序排列即可。
        // ==========================================================

        long now = System.currentTimeMillis();
        ActivityConfig config = new ActivityConfig();
        config.setActId(actId);
        // 演示:开始时间设为 1 分钟前,保证已开始;结束时间 1 小时后
        config.setStartTime(now - TimeUnit.MINUTES.toMillis(1));
        config.setEndTime(now + TimeUnit.HOURS.toMillis(1));
        config.setSliceMs(TimeUnit.SECONDS.toMillis(60));
        config.setUserLimit(3);

        List<PrizeConfig> prizes = new ArrayList<>();
        prizes.add(buildPrize(1001L, "一等奖-100积分", 10000, 50L, 100));
        prizes.add(buildPrize(1002L, "二等奖-10元券", 50000, 500L, 50));
        prizes.add(buildPrize(1003L, "三等奖-1元券", 200000, 5000L, 10));
        config.setPrizes(prizes);

        return config;
    }

    private PrizeConfig buildPrize(Long prizeId, String name, int probPpm, long total, int priority) {
        PrizeConfig prize = new PrizeConfig();
        prize.setPrizeId(prizeId);
        prize.setPrizeName(name);
        prize.setProbPpm(probPpm);
        prize.setTotalCount(total);
        prize.setPriority(priority);
        return prize;
    }
}
