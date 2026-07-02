-- deduct.lua —— 方案三真实环境用的原子扣减脚本（在 Redis 内原子执行）
--
-- 用法（Jedis 示例）：
--   String sha = jedis.scriptLoad(luaText);
--   Object r = jedis.evalsha(sha, 2,
--       "balance:" + accountId, "dedup:" + requestId,   -- KEYS
--       String.valueOf(amountCents), String.valueOf(ttlSeconds)); -- ARGV
--
-- KEYS[1] = balance:{accountId}   账户余额（以"分"存整数）
-- KEYS[2] = dedup:{requestId}     幂等标记
-- ARGV[1] = amount                扣减金额（分，整数，避免浮点）
-- ARGV[2] = dedupTtlSeconds       幂等键过期时间
--
-- 返回: {code, data}
--   {2, status}   DUPLICATE   幂等命中，data 为历史结果
--   {-1,'NO_ACCOUNT'}         账户不存在
--   {0, 'INSUFFICIENT'}       余额不足
--   {1, newBalance}           扣减成功，data 为扣后余额

-- 1) 幂等：该 requestId 已处理过，直接返回历史结果
if redis.call('EXISTS', KEYS[2]) == 1 then
    return {2, redis.call('GET', KEYS[2])}
end

-- 2) 取余额
local balance = tonumber(redis.call('GET', KEYS[1]))
if balance == nil then
    return {-1, 'NO_ACCOUNT'}
end

-- 3) 余额校验
local amount = tonumber(ARGV[1])
if balance < amount then
    redis.call('SETEX', KEYS[2], tonumber(ARGV[2]), 'REJECTED')
    return {0, 'INSUFFICIENT'}
end

-- 4) 扣减 + 写幂等标记（同一脚本内原子完成，绝不超扣）
local newBalance = redis.call('DECRBY', KEYS[1], amount)
redis.call('SETEX', KEYS[2], tonumber(ARGV[2]), 'SUCCESS')
return {1, tostring(newBalance)}

-- 注意（脚本之外的工程要点）：
--   扣减成功后，调用方需同步把这笔操作追加到可靠日志（Kafka / Redis Stream /
--   DB 流水表，带 request_id 唯一），先落盘再对外返回成功。
--   Redis 开 AOF appendfsync everysec（最多丢 ~1s），异步消费日志落 MySQL 权威
--   流水并定期对账：DB初始余额 - Σ已确认扣减 == Redis 当前余额。
