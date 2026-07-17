package com.pikaqiu.lottery.service;

import com.pikaqiu.lottery.dto.ActivityConfig;
import com.pikaqiu.lottery.dto.DrawRequest;
import com.pikaqiu.lottery.dto.DrawResult;
import com.pikaqiu.lottery.dto.PrizeConfig;
import com.pikaqiu.lottery.dto.WinRecord;
import com.pikaqiu.lottery.enums.DrawStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 抽奖核心服务。
 *
 * <p>职责:把一次抽奖翻译成对 Redis 抽奖脚本({@code lottery_draw.lua})的一次原子调用,并把脚本
 * 结果翻译回业务结果。真正的并发控制(防超卖、限次、概率、时间片释放)全部下沉到脚本里靠 Redis
 * 单线程串行保证,本类只做"参数组装 + 结果解析 + 命中后异步发奖",本身无状态,可水平扩展。
 *
 * <p>一次抽奖流程:取配置(本地缓存) -> 每个奖品生成一个随机数 -> 执行原子 Lua -> 解析
 * {@code {status, prizeId}} -> 命中且非重放时把中奖事件投递给 {@link LotteryRecordSender} 异步落库发奖。
 *
 * <p>设计要点:
 * <ul>
 *   <li>随机数在应用层生成后传入脚本,而非在脚本内取——规避 Redis Lua 内 math.random 的主从复制语义问题;</li>
 *   <li>时间戳也由应用层传入,保证脚本在主从/回放下结果一致;</li>
 *   <li>KEYS 全部带 {@code {actId}} hash tag,保证多 key 脚本在 Redis Cluster 下同 slot 可执行。</li>
 * </ul>
 *
 * @author xiaoye
 */
@Slf4j
@Service
public class LotteryService {

    /** 概率精度:百万分之 */
    private static final int PPM = 1_000_000;

    /** key 过期在活动结束后再多留的 buffer,便于对账 */
    private static final long TTL_BUFFER_MS = TimeUnit.DAYS.toMillis(1);

    /** Lua 返回状态码,需与 lottery_draw.lua 保持一致 */
    private static final long STATUS_OK = 1L;            // 正常处理(prizeId>0 中奖, =0 未中奖)
    private static final long STATUS_REPLAY = 2L;        // 幂等重放,返回首次结果
    private static final long STATUS_NOT_STARTED = -1L;  // 活动未开始
    private static final long STATUS_ENDED = -2L;        // 活动已结束
    private static final long STATUS_LIMIT = -3L;        // 抽奖次数用尽

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    @SuppressWarnings("rawtypes")
    private DefaultRedisScript<List> lotteryDrawScript;

    @Resource
    private LotteryConfigService lotteryConfigService;

    @Resource
    private LotteryRecordSender lotteryRecordSender;

    /**
     * 抽奖。整个方法无锁、无本地状态,所有并发安全性由 Redis 脚本保证。
     *
     * @param request 抽奖请求(actId、userId 必填;requestId 建议客户端自带以获得幂等)
     * @return 抽奖结果(中奖/未中奖/未开始/已结束/次数用尽/活动不存在)
     */
    public DrawResult draw(DrawRequest request) {
        // 1. 取活动配置(本地 Caffeine 缓存,未命中回源加载);活动不存在直接短路
        ActivityConfig config = lotteryConfigService.getConfig(request.getActId());
        if (config == null) {
            return DrawResult.of(DrawStatus.ACTIVITY_NOT_FOUND);
        }

        // 2. requestId 是幂等的唯一依据:客户端自带同一值时,重试/重复提交只认首次结果。
        //    为空则生成随机 UUID —— 每次都不同,等于放弃幂等,故仅作兜底,生产应要求客户端传入。
        String requestId = StringUtils.isBlank(request.getRequestId())
                ? UUID.randomUUID().toString()
                : request.getRequestId();
        // now 与随机数都在应用层生成后传入脚本,保证脚本在主从复制/回放下行为确定
        long now = System.currentTimeMillis();

        // 3. 组装 KEYS/ARGV,一次原子 Lua 完成:幂等 -> 时间窗 -> 限次 -> 独立概率 -> 懒惰释放 -> 扣库存
        List<String> keys = buildKeys(config.getActId(), requestId);
        Object[] args = buildArgs(config, request.getUserId(), now);
        List<?> raw = stringRedisTemplate.execute(lotteryDrawScript, keys, args);

        // 4. 解析脚本返回;命中则异步投递发奖
        return parseResult(config, request, requestId, now, raw);
    }

    /**
     * 组装脚本 KEYS。{actId} 作为 hash tag,保证同一活动的 4 个 key 落在同一 slot;
     * Redis Cluster 要求多 key 的 Lua 脚本所有 key 同 slot,否则直接报错。
     */
    private List<String> buildKeys(Long actId, String requestId) {
        String tag = "{" + actId + "}";
        return Arrays.asList(
                "lottery:req:" + tag + ":" + requestId,   // KEYS[1] 幂等键(string)
                "lottery:user:" + tag,                     // KEYS[2] 用户限次(hash: field=userId)
                "lottery:stock:" + tag,                    // KEYS[3] 可用库存(hash: field=prizeId)
                "lottery:released:" + tag);                // KEYS[4] 已释放游标(hash: field=prizeId)
    }

    /**
     * 组装脚本 ARGV。注意:顺序必须与 lottery_draw.lua 头部注释严格一致,否则脚本取参错位。
     */
    private Object[] buildArgs(ActivityConfig config, String userId, long now) {
        // key 过期时间 = 活动结束后再留 1 天 buffer,便于结束后对账
        long ttlMs = config.getEndTime() - now + TTL_BUFFER_MS;
        List<PrizeConfig> prizes = config.getPrizes();

        List<String> args = new ArrayList<>(9 + prizes.size() * 4);
        args.add(String.valueOf(now));                     // ARGV[1] 当前时间(ms)
        args.add(userId);                                  // ARGV[2] 用户ID
        args.add(String.valueOf(config.getUserLimit()));   // ARGV[3] 每人限抽次数
        args.add(String.valueOf(config.getStartTime()));   // ARGV[4] 活动开始(ms)
        args.add(String.valueOf(config.getEndTime()));     // ARGV[5] 活动结束(ms)
        args.add(String.valueOf(config.getSliceMs()));     // ARGV[6] 时间片长度(ms)
        args.add(String.valueOf(config.getTotalSlices())); // ARGV[7] 总时间片数
        args.add(String.valueOf(ttlMs));                   // ARGV[8] key 过期(ms)
        args.add(String.valueOf(prizes.size()));           // ARGV[9] 奖品数 N
        // ARGV[10..] 每个奖品 4 元组:prizeId, probPpm, total, rand;prizes 已按优先级倒序
        for (PrizeConfig prize : prizes) {
            args.add(String.valueOf(prize.getPrizeId()));
            args.add(String.valueOf(prize.getProbPpm()));
            args.add(String.valueOf(prize.getTotalCount()));
            // 随机数(0~999999)由应用层 ThreadLocalRandom 生成,规避 Redis Lua 内 math.random 的主从复制语义坑
            args.add(String.valueOf(ThreadLocalRandom.current().nextInt(PPM)));
        }
        return args.toArray();
    }

    /**
     * 解析 Lua 返回的 {status, prizeId},映射成业务结果;命中且非重放时触发异步发奖。
     */
    private DrawResult parseResult(ActivityConfig config, DrawRequest request, String requestId,
                                   long now, List<?> raw) {
        // 防御式判空:正常情况下脚本一定返回长度为 2 的数组
        if (raw == null || raw.size() < 2) {
            log.error("[lottery] 抽奖脚本返回异常, actId={}, userId={}, raw={}",
                    request.getActId(), request.getUserId(), raw);
            return DrawResult.of(DrawStatus.NOT_WIN);
        }
        long status = toLong(raw.get(0));   // 处理状态码
        long prizeId = toLong(raw.get(1));  // 中奖奖品ID,0 表示未中奖

        // 各类"未处理成功"的状态直接映射返回
        if (status == STATUS_NOT_STARTED) {
            return DrawResult.of(DrawStatus.NOT_STARTED);
        }
        if (status == STATUS_ENDED) {
            return DrawResult.of(DrawStatus.ENDED);
        }
        if (status == STATUS_LIMIT) {
            return DrawResult.of(DrawStatus.LIMIT_EXCEEDED);
        }

        // status=1 正常处理、status=2 幂等重放,二者都算"已处理",继续判断中奖与否
        boolean replay = status == STATUS_REPLAY;
        if (status != STATUS_OK && !replay) {
            log.error("[lottery] 未知状态码, status={}, actId={}, userId={}",
                    status, request.getActId(), request.getUserId());
            return DrawResult.of(DrawStatus.NOT_WIN);
        }

        // prizeId<=0:未中奖(含"命中但库存不足被降级"的情况)
        if (prizeId <= 0) {
            DrawResult result = DrawResult.of(DrawStatus.NOT_WIN);
            result.setReplay(replay);
            return result;
        }

        // 命中:从配置回填奖品名
        PrizeConfig prize = findPrize(config, prizeId);
        DrawResult result = DrawResult.of(DrawStatus.WIN);
        result.setPrizeId(prizeId);
        result.setPrizeName(prize == null ? null : prize.getPrizeName());
        result.setReplay(replay);

        // 只在首次中奖时投递发奖;幂等重放(status=2)说明首次已投递过,不重复发
        if (!replay) {
            lotteryRecordSender.send(buildWinRecord(request, requestId, now, result));
        }
        return result;
    }

    /**
     * 组装投递给发奖链路的中奖事件。requestId 会随消息带到消费者,作为落库/发奖的幂等键。
     */
    private WinRecord buildWinRecord(DrawRequest request, String requestId, long now, DrawResult result) {
        WinRecord record = new WinRecord();
        record.setActId(request.getActId());
        record.setUserId(request.getUserId());
        record.setPrizeId(result.getPrizeId());
        record.setPrizeName(result.getPrizeName());
        record.setRequestId(requestId);
        record.setDrawTime(now);
        return record;
    }

    /**
     * 按奖品ID在配置里查奖品,用于回填奖品名。
     */
    private PrizeConfig findPrize(ActivityConfig config, long prizeId) {
        for (PrizeConfig prize : config.getPrizes()) {
            if (Long.valueOf(prizeId).equals(prize.getPrizeId())) {
                return prize;
            }
        }
        return null;
    }

    /**
     * Redis 脚本整型返回一般是 Long,这里做兼容转换,避免个别序列化返回 String 时抛异常。
     */
    private long toLong(Object value) {
        return value instanceof Number ? ((Number) value).longValue() : Long.parseLong(String.valueOf(value));
    }
}
