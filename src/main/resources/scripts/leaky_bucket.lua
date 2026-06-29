-- leaky_bucket.lua
--
-- Leaky bucket algorithm: each user has a "water level" that rises by 1
-- with every incoming request and continuously leaks (drains) at a fixed
-- rate over time. If the level would exceed capacity, the request is
-- rejected. This smooths out bursty traffic into a steady outflow rate,
-- unlike token bucket which allows short bursts up to full capacity.
--
-- KEYS[1] = bucket key, e.g. "rl:leaky:user-1"
-- ARGV[1] = capacity   (max level the bucket can hold)
-- ARGV[2] = leak_rate  (units drained per second)
-- ARGV[3] = now        (current timestamp in milliseconds)
--
-- returns {allowed, remaining_capacity}

local key = KEYS[1]
local capacity = tonumber(ARGV[1])
local leak_rate = tonumber(ARGV[2])
local now = tonumber(ARGV[3])

local bucket = redis.call('HMGET', key, 'level', 'ts')
local level = tonumber(bucket[1])
local last_ts = tonumber(bucket[2])

if level == nil then
    level = 0
    last_ts = now
end

-- drain the bucket based on elapsed time since the last request
local elapsed_seconds = (now - last_ts) / 1000
if elapsed_seconds > 0 then
    local leaked = elapsed_seconds * leak_rate
    level = math.max(0, level - leaked)
    last_ts = now
end

local allowed = 0
if level < capacity then
    level = level + 1
    allowed = 1
end

redis.call('HMSET', key, 'level', level, 'ts', last_ts)
redis.call('EXPIRE', key, 3600)

return {allowed, math.floor(capacity - level)}
