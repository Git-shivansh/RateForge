-- token_bucket.lua
--
-- Token bucket algorithm: each user has a bucket of tokens that refills
-- continuously at a fixed rate, capped at a maximum capacity. Every
-- request consumes one token. If no token is available, the request
-- is rejected. This whole check-and-decrement happens atomically inside
-- Redis so concurrent requests from the same user can never both succeed
-- when only one token is left (no race condition).
--
-- KEYS[1] = bucket key, e.g. "rl:token:user-1"
-- ARGV[1] = capacity        (max tokens the bucket can hold)
-- ARGV[2] = refill_rate     (tokens added per second)
-- ARGV[3] = now             (current timestamp in milliseconds)
-- ARGV[4] = requested       (tokens this request needs, normally 1)
--
-- returns {allowed, remaining_tokens}
--   allowed = 1 (request allowed) or 0 (rejected)

local key = KEYS[1]
local capacity = tonumber(ARGV[1])
local refill_rate = tonumber(ARGV[2])
local now = tonumber(ARGV[3])
local requested = tonumber(ARGV[4])

local bucket = redis.call('HMGET', key, 'tokens', 'ts')
local tokens = tonumber(bucket[1])
local last_ts = tonumber(bucket[2])

-- first request ever for this user: start with a full bucket
if tokens == nil then
    tokens = capacity
    last_ts = now
end

-- refill tokens based on how much time has passed since the last request
local elapsed_seconds = (now - last_ts) / 1000
if elapsed_seconds > 0 then
    local refill = elapsed_seconds * refill_rate
    tokens = math.min(capacity, tokens + refill)
    last_ts = now
end

local allowed = 0
if tokens >= requested then
    tokens = tokens - requested
    allowed = 1
end

redis.call('HMSET', key, 'tokens', tokens, 'ts', last_ts)
redis.call('EXPIRE', key, 3600)

return {allowed, math.floor(tokens)}
