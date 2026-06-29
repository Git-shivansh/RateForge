package org.example.rateforge.strategy;


import com.ratelimiter.model.RateLimitResult;

/**
 * Common contract for every rate limiting algorithm. Each implementation
 * decides, atomically inside Redis, whether a given user is allowed to
 * make another request right now.
 */
public interface RateLimiterStrategy {
    RateLimitResult isAllowed(String userId);
}
