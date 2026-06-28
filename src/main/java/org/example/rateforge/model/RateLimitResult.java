package org.example.rateforge.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Result of a single rate limit check.
 */
@Getter
@AllArgsConstructor
public class RateLimitResult {
    private final boolean allowed;
    private final long remaining;
}
