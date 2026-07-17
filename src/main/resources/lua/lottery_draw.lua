-- =============================================================
-- 抽奖核心脚本:一次调用完成 幂等 -> 时间窗校验 -> 限次 -> 独立概率抽奖 -> 懒惰释放库存 -> 扣减
-- Redis 单线程串行执行,整段脚本原子,天然防超卖、无竞态。
--
-- KEYS[1] = 幂等键   lottery:req:{actId}:{reqId}   (string)
-- KEYS[2] = 用户限次 lottery:user:{actId}          (hash: field=userId)
-- KEYS[3] = 可用库存 lottery:stock:{actId}         (hash: field=prizeId, 已释放未领取)
-- KEYS[4] = 已释放量 lottery:released:{actId}      (hash: field=prizeId, 懒惰释放游标)
--
-- ARGV[1]  = now(毫秒)
-- ARGV[2]  = userId
-- ARGV[3]  = userLimit(每人限抽次数)
-- ARGV[4]  = startMs(活动开始)
-- ARGV[5]  = endMs(活动结束)
-- ARGV[6]  = sliceMs(时间片长度,毫秒)
-- ARGV[7]  = totalSlices(总时间片数)
-- ARGV[8]  = ttlMs(key 过期,活动结束+buffer)
-- ARGV[9]  = prizeCount(奖品数 N)
-- ARGV[10..] 每个奖品 4 元组(按优先级从高到低排列):
--            prizeId, probPpm(百万分之), total(总量), rand(0~999999 由应用层生成)
--
-- 返回 {status, prizeId}:
--   status  1 = 正常处理(prizeId>0 中奖, =0 未中奖)
--   status  2 = 幂等重放(返回首次结果)
--   status -1 = 活动未开始
--   status -2 = 活动已结束
--   status -3 = 抽奖次数用尽
-- =============================================================

local nowMs      = tonumber(ARGV[1])
local userId     = ARGV[2]
local userLimit  = tonumber(ARGV[3])
local startMs    = tonumber(ARGV[4])
local endMs      = tonumber(ARGV[5])
local sliceMs    = tonumber(ARGV[6])
local totalSlice = tonumber(ARGV[7])
local ttlMs      = ARGV[8]
local prizeCount = tonumber(ARGV[9])
local PRIZE_BASE = 10
local PPM        = 1000000

-- 0. 幂等:同一 reqId 直接返回首次结果
local cached = redis.call('GET', KEYS[1])
if cached then
    return {2, tonumber(cached)}
end

-- 1. 活动时间窗校验
if nowMs < startMs then
    return {-1, 0}
end
if nowMs > endMs then
    return {-2, 0}
end

-- 2. 限次:未中奖也算一次,超限回滚
local cnt = redis.call('HINCRBY', KEYS[2], userId, 1)
if cnt > userLimit then
    redis.call('HINCRBY', KEYS[2], userId, -1)
    return {-3, 0}
end
redis.call('PEXPIRE', KEYS[2], ttlMs)

-- 3. 独立概率:按优先级顺序逐个掷,第一个命中的奖品决定结果
local won = 0
for i = 0, prizeCount - 1 do
    local off = PRIZE_BASE + i * 4
    local prizeId = ARGV[off]
    local prob    = tonumber(ARGV[off + 1])
    local total   = tonumber(ARGV[off + 2])
    local rand    = tonumber(ARGV[off + 3])
    if rand < prob then
        -- 命中该档:先按时间片懒惰释放库存(顺延累积,不丢配额)
        local elapsed = math.floor((nowMs - startMs) / sliceMs) + 1
        if elapsed > totalSlice then
            elapsed = totalSlice
        end
        local target = math.floor(total * elapsed / totalSlice)
        if target > total then
            target = total
        end
        local released = tonumber(redis.call('HGET', KEYS[4], prizeId) or '0')
        if target > released then
            redis.call('HINCRBY', KEYS[3], prizeId, target - released)
            redis.call('HSET', KEYS[4], prizeId, target)
        end
        -- 扣减可用库存
        local left = redis.call('HINCRBY', KEYS[3], prizeId, -1)
        if left >= 0 then
            won = tonumber(prizeId)
        else
            redis.call('HINCRBY', KEYS[3], prizeId, 1)   -- 回滚
            won = 0                                       -- 命中但无货 -> 降级为未中奖
        end
        break
    end
end

redis.call('PEXPIRE', KEYS[3], ttlMs)
redis.call('PEXPIRE', KEYS[4], ttlMs)

-- 4. 记录幂等结果
redis.call('SET', KEYS[1], won, 'PX', ttlMs)

return {1, won}
