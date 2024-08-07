-- hash key: KEYS[1] = lockKey
-- hash field: ARGV[1] = lockValue
-- hash value: 1 加锁次数
-- 可重入锁的实现
-- 1. 如果key不存在，说明是第一次加锁，直接设置key的值为1，设置过期时间
if(redis.call('exists', KEYS[1]) == 0) then
  redis.call('hincrby', KEYS[1], ARG[1], 1)
    redis.call('pexpire', KEYS[1], ARGV[1])
    return 1
end
-- 2. 如果key存在，判断field是否存在，如果存在，说明是重入锁，直接加1
if(redis.call('hexists', KEYS[1], ARGV[1]) == 1) then
    redis.call('hincrby', KEYS[1], ARGV[1], 1)
    redis.call('pexpire', KEYS[1], ARGV[1])
    return 1
-- 3. 如果field不存在，说明是其他线程加的锁，直接返回0
else
    return 0
end

-- 删除可重入锁
-- hash key: KEYS[1] = lockKey
-- hash field: ARGV[1] = lockValue
-- hash value: 1 加锁次数
-- 1. 判断key是否存在，如果不存在，直接返回0
if(redis.call('exists', KEYS[1]) == 0) then
    return 0
end
-- 2. 判断field是否存在，如果不存在，直接返回0
if(redis.call('hexists', KEYS[1], ARGV[1]) == 0) then
    return 0
end
-- 3. 如果field存在，判断加锁次数，如果大于1，直接减1
local count = redis.call('hincrby', KEYS[1], ARGV[1], -1)
if(count > 0) then
-- 4. 如果加锁次数等于1，直接删除key
    redis.call('pexpire', KEYS[1], 30000)
    return 1
else
-- 5. 如果加锁次数等于0，直接删除key
    redis.call('del', KEYS[1])
    return 1
end
```