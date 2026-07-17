package com.pikaqiu.lottery.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.List;

/**
 * 抽奖 Lua 脚本 Bean。启动时预加载,execute 走 EVALSHA 缓存,减少脚本传输。
 *
 * @author xiaoye
 */
@Configuration
public class LotteryScriptConfig {

    @SuppressWarnings("rawtypes")
    @Bean
    public DefaultRedisScript<List> lotteryDrawScript() {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        // 从 classpath 加载脚本文件
        script.setLocation(new ClassPathResource("lua/lottery_draw.lua"));
        // 返回类型 List 对应 Lua 的 {status, prizeId} 数组
        script.setResultType(List.class);
        return script;
    }
}
