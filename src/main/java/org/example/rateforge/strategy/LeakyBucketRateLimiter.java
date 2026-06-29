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
 * Leaky bucket: every request raises the "water level" by one unit,
 * while the level continuously drains at a fixed rate. Smooths bursty
 * traffic into a steady outflow instead of allowing a burst up to
 * capacity the way token bucket does.
 */
@Component("leaky-bucket")
public class LeakyBucketRateLimiter implements RateLimiterStrategy {

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<List> script;
    private final RateLimiterProperties properties;

    public LeakyBucketRateLimiter(StringRedisTemplate redisTemplate,
                                  @Qualifier("leakyBucketScript") DefaultRedisScript<List> script,
                                  RateLimiterProperties properties) {
        this.redisTemplate = redisTemplate;
        this.script = script;
        this.properties = properties;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RateLimitResult isAllowed(String userId) {
        String key = "rl:leaky:" + userId;
        long now = System.currentTimeMillis();

        List<Object> result = redisTemplate.execute(
                script,
                Collections.singletonList(key),
                String.valueOf(properties.getCapacity()),
                String.valueOf(properties.getLeakRate()),
                String.valueOf(now)
        );

        Long allowedFlag = (Long) result.get(0);
        Long remaining = (Long) result.get(1);

        return new RateLimitResult(
                allowedFlag != null && allowedFlag == 1L,
                remaining == null ? 0 : remaining
        );
    }
}
