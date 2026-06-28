package org.example.rateforge.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Binds the "rate-limiter.*" section of application.yml so the active
 * strategy and its tuning parameters can be changed in config, with no
 * code changes or recompilation needed.
 */
@Component
@ConfigurationProperties(prefix = "rate-limiter")
@Data
public class RateLimiterProperties {

    /** token-bucket | sliding-window | leaky-bucket */
    private String strategy = "token-bucket";

    private long capacity = 10;
    private long refillRate = 2;
    private long windowSizeSeconds = 10;
    private long leakRate = 2;
}

