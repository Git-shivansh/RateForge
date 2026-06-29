-- sliding_window.lua
--
-- Sliding window algorithm: every request's timestamp is stored in a
-- Redis sorted set, scored by the time it arrived. On each new request,
-- timestamps older than the window are dropped, then the remaining
-- count is compared against the limit. More accurate than token bucket
-- (no burst at window boundaries) but costs more memory since every
-- timestamp inside the window is stored individually.
--
-- KEYS[1] = window key, e.g. "rl:window:user-1"
-- ARGV[1] = capacity        (max requests allowed inside the window)
-- ARGV[2] = window_size_ms  (size of the rolling window in milliseconds)
-- ARGV[3] = now             (current timestamp in milliseconds)
--
-- returns {allowed, remaining_slots}

local key = KEYS[1]
local capacity = tonumber(ARGV[1])
local window_size = tonumber(ARGV[2])
local now = tonumber(ARGV[3])

local window_start = now - window_size

-- drop every timestamp that has fallen outside the current window
redis.call('ZREMRANGEBYSCORE', key, 0, window_start)

local current_count = redis.call('ZCARD', key)

local allowed = 0
if current_count < capacity then
    -- unique member per request: timestamp + random suffix avoids collisions
    -- when two requests land in the same millisecond
    local member = now .. '-' .. math.random(1, 1000000)
    redis.call('ZADD', key, now, member)
    allowed = 1
    current_count = current_count + 1
end

redis.call('EXPIRE', key, math.ceil(window_size / 1000) + 1)

return {allowed, capacity - current_count}
