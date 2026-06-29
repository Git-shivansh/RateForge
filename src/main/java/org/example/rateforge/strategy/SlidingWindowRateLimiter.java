package org.example.rateforge.strategy;


import org.example.rateforge.config.RateLimiterProperties;
import org.example.rateforge.model.RateLimitResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Sliding window: stores every request timestamp in a Redis sorted set
 * and counts how many fall inside the current rolling window. Most
 * accurate of the three (no boundary burst), but uses more memory since
 * every timestamp in the window is stored individually.
 */
@Component("sliding-window")
public class SlidingWindowRateLimiter implements RateLimiterStrategy {

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<List> script;
    private final RateLimiterProperties properties;

    public SlidingWindowRateLimiter(StringRedisTemplate redisTemplate,
                                    @Qualifier("slidingWindowScript") DefaultRedisScript<List> script,
                                    RateLimiterProperties properties) {
        this.redisTemplate = redisTemplate;
        this.script = script;
        this.properties = properties;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RateLimitResult isAllowed(String userId) {
        String key = "rl:window:" + userId;
        long now = System.currentTimeMillis();
        long windowSizeMs = properties.getWindowSizeSeconds() * 1000;

        List<Object> result = redisTemplate.execute(
                script,
                Collections.singletonList(key),
                String.valueOf(properties.getCapacity()),
                String.valueOf(windowSizeMs),
                String.valueOf(now)
        );

        Long allowedFlag = (Long) result.get(0);
        Long remaining = (Long) result.get(1);

        return new RateLimitResult(
                allowedFlag != null && allowedFlag == 1L,
                remaining == null ? 0 : Math.max(0, remaining)
        );
    }
}
