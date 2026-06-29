package org.example.rateforge.strategy;


import com.example.rateforge.config.RateLimiterProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Strategy pattern in action: Spring injects every RateLimiterStrategy
 * bean into this map, keyed by its bean name ("token-bucket",
 * "sliding-window", "leaky-bucket"). Whichever value is set under
 * rate-limiter.strategy in application.yml is picked at request time —
 * no code change or recompilation needed to switch algorithms.
 */
@Component
public class RateLimiterFactory {

    private final Map<String, RateLimiterStrategy> strategies;
    private final RateLimiterProperties properties;

    public RateLimiterFactory(Map<String, RateLimiterStrategy> strategies,
                              RateLimiterProperties properties) {
        this.strategies = strategies;
        this.properties = properties;
    }

    public RateLimiterStrategy getStrategy() {
        RateLimiterStrategy strategy = strategies.get(properties.getStrategy());
        if (strategy == null) {
            throw new IllegalStateException(
                    "Unknown rate-limiter.strategy: '" + properties.getStrategy() +
                            "'. Valid options are: " + strategies.keySet());
        }
        return strategy;
    }
}
