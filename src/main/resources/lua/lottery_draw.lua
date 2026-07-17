-- =============================================================
-- 抽奖核心脚本
-- 一次调用完成:幂等 -> 时间窗校验 -> 限次 -> 独立概率抽奖 -> 懒惰释放库存 -> 扣减。
--
-- 为什么全放一个脚本:Redis 单线程,一段脚本内的所有命令原子执行、期间不会被其他请求打断。
-- 因此"抽概率命中某奖品 -> 扣它的库存"这两步之间不存在竞态,天然防超卖,也不会出现
-- "抽中了却扣不到库存"或"扣了库存却没记幂等"的中间态。相比"先查库存再扣"的两次往返,
-- 这里零竞态、零分布式锁。
--
-- ---- 入参约定(KEYS / ARGV 顺序必须与 Java LotteryService 严格一致)----
-- KEYS[1] = 幂等键   lottery:req:{actId}:{reqId}   (string)  值=首次结果的 prizeId
-- KEYS[2] = 用户限次 lottery:user:{actId}          (hash: field=userId  -> 已抽次数)
-- KEYS[3] = 可用库存 lottery:stock:{actId}         (hash: field=prizeId -> 已释放未领取数)
-- KEYS[4] = 已释放量 lottery:released:{actId}      (hash: field=prizeId -> 懒惰释放游标)
-- 说明:4 个 key 都带 {actId} hash tag,保证落在同一 slot,集群下多 key 脚本才能执行。
--
-- ARGV[1]  = now(毫秒)        当前时间,由应用层传入(脚本内不取时钟,保证主从/回放一致)
-- ARGV[2]  = userId
-- ARGV[3]  = userLimit         每人限抽次数(总)
-- ARGV[4]  = startMs           活动开始
-- ARGV[5]  = endMs             活动结束
-- ARGV[6]  = sliceMs           时间片长度(毫秒)
-- ARGV[7]  = totalSlices       总时间片数 = ceil((endMs-startMs)/sliceMs)
-- ARGV[8]  = ttlMs             key 过期(活动结束 + buffer)
-- ARGV[9]  = prizeCount        奖品数 N
-- ARGV[10..] 每个奖品 4 元组(按优先级从高到低排列):
--            prizeId, probPpm(百万分之), total(总量), rand(0~999999,应用层生成)
--
-- ---- 返回 {status, prizeId} ----
-- status  1 = 正常处理(prizeId>0 中奖, =0 未中奖)
-- status  2 = 幂等重放(返回首次的 prizeId)
-- status -1 = 活动未开始
-- status -2 = 活动已结束
-- status -3 = 抽奖次数用尽
-- =============================================================

-- 解析定长入参(变长的奖品数组从 ARGV[PRIZE_BASE] 开始)
local nowMs      = tonumber(ARGV[1])
local userId     = ARGV[2]
local userLimit  = tonumber(ARGV[3])
local startMs    = tonumber(ARGV[4])
local endMs      = tonumber(ARGV[5])
local sliceMs    = tonumber(ARGV[6])
local totalSlice = tonumber(ARGV[7])
local ttlMs      = ARGV[8]              -- 保持字符串,直接传给 PEXPIRE/SET PX,避免数字转换歧义
local prizeCount = tonumber(ARGV[9])
local PRIZE_BASE = 10                    -- 第 1 个奖品四元组的起始下标
local PPM        = 1000000               -- 概率分母:百万分之

-- 0. 幂等:同一 reqId 直接返回首次结果。
--    只要抽奖真正处理过(无论中奖与否)就会写 KEYS[1],故重试/重复提交只认首次结论,
--    不会重复扣次数、不会重复中奖。GET 命中即使值是 "0"(未中奖)也算处理过。
local cached = redis.call('GET', KEYS[1])
if cached then
    return {2, tonumber(cached)}
end

-- 1. 活动时间窗校验:未开始 / 已结束都直接短路返回,不消耗次数、不写幂等。
if nowMs < startMs then
    return {-1, 0}
end
if nowMs > endMs then
    return {-2, 0}
end

-- 2. 限次:先自增再判断,超限则回滚自增值。
--    先加后判能避免"读-改-写"竞态;这里未中奖也会占用一次(cnt 已 +1 且不回滚),防刷。
local cnt = redis.call('HINCRBY', KEYS[2], userId, 1)
if cnt > userLimit then
    redis.call('HINCRBY', KEYS[2], userId, -1)   -- 超限,撤销本次自增
    return {-3, 0}
end
redis.call('PEXPIRE', KEYS[2], ttlMs)

-- 3. 独立概率抽奖:奖品已按优先级从高到低排好,逐个用各自的随机数独立掷。
--    "独立"指每档用自己的概率判定,互不影响;第一个命中的奖品即为本次结果(高价值优先)。
local won = 0
for i = 0, prizeCount - 1 do
    local off = PRIZE_BASE + i * 4          -- 第 i 个奖品四元组的起始下标
    local prizeId = ARGV[off]
    local prob    = tonumber(ARGV[off + 1]) -- 该档概率(百万分之)
    local total   = tonumber(ARGV[off + 2]) -- 该档总量
    local rand    = tonumber(ARGV[off + 3]) -- 该档随机数 0~999999

    if rand < prob then
        -- ==== 命中该档,先按时间片"懒惰释放"补齐当前应放库存,再扣减 ====
        -- 均匀发放思路:总量 total 摊到 totalSlice 个时间片,到当前时刻累计应释放
        --   target = floor(total * 已过片数 / 总片数)
        -- released 记录"已释放游标",只增不减;target 与 released 的差值就是本次要补进可用池的量。
        -- 未被领取的配额会留在 stock 里自然顺延累积,所以总量最终一定发完,不会因某片没人抽而丢失。
        local elapsed = math.floor((nowMs - startMs) / sliceMs) + 1   -- 含当前片的已过片数(从1计)
        if elapsed > totalSlice then
            elapsed = totalSlice                                       -- 封顶,末片之后不再增长
        end
        local target = math.floor(total * elapsed / totalSlice)
        if target > total then
            target = total                                            -- 双保险,累计不超过总量
        end
        local released = tonumber(redis.call('HGET', KEYS[4], prizeId) or '0')
        if target > released then
            redis.call('HINCRBY', KEYS[3], prizeId, target - released) -- 把新增配额补进可用库存
            redis.call('HSET', KEYS[4], prizeId, target)               -- 前移已释放游标
        end

        -- ==== 扣减可用库存 ====
        -- 先扣再判:HINCRBY 原子返回扣后余量。>=0 说明扣成功(中奖);
        -- <0 说明这一刻该档可用池已空(被别人抢完或本片尚未释放到),回滚 +1 并按业务约定
        -- 降级为"未中奖"——注意:按需求命中档无货不顺延到其他档,直接 break 结束。
        local left = redis.call('HINCRBY', KEYS[3], prizeId, -1)
        if left >= 0 then
            won = tonumber(prizeId)          -- 命中且有货
        else
            redis.call('HINCRBY', KEYS[3], prizeId, 1)   -- 回滚,避免可用池被扣成负数
            won = 0                                       -- 命中但无货 -> 降级为未中奖
        end
        break   -- 第一个命中的档已决定结果,后面的档不再判定
    end
end

-- 刷新库存/游标 key 的过期时间,与活动生命周期对齐,活动结束后自动回收
redis.call('PEXPIRE', KEYS[3], ttlMs)
redis.call('PEXPIRE', KEYS[4], ttlMs)

-- 4. 记录幂等结果(中奖存 prizeId,未中奖存 0),供后续同 reqId 重放直接返回
redis.call('SET', KEYS[1], won, 'PX', ttlMs)

return {1, won}
