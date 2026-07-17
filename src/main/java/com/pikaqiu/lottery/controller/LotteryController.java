package com.pikaqiu.lottery.controller;

import com.pikaqiu.lottery.dto.ActivityConfig;
import com.pikaqiu.lottery.dto.DrawRequest;
import com.pikaqiu.lottery.dto.DrawResult;
import com.pikaqiu.lottery.service.LotteryConfigService;
import com.pikaqiu.lottery.service.LotteryService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 抽奖接口。
 *
 * @author xiaoye
 */
@RestController
@RequestMapping("/lottery")
public class LotteryController {

    @Resource
    private LotteryService lotteryService;

    @Resource
    private LotteryConfigService lotteryConfigService;

    /**
     * 抽奖。
     * curl -X POST http://localhost:8080/lottery/draw -H "Content-Type: application/json" \
     *      -d '{"actId":1,"userId":"u001","requestId":"req-001"}'
     */
    @PostMapping("/draw")
    public DrawResult draw(@RequestBody DrawRequest request) {
        return lotteryService.draw(request);
    }

    /**
     * 预热/刷新活动配置到本地缓存,活动上线或改配置后调用。
     * curl -X POST "http://localhost:8080/lottery/warmup?actId=1"
     */
    @PostMapping("/warmup")
    public ActivityConfig warmup(@RequestParam Long actId) {
        return lotteryConfigService.refresh(actId);
    }
}
