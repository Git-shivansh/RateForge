package org.example.rateforge.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Plain endpoint used only to demonstrate the rate limiter. The filter
 * runs in front of this (and every other) controller automatically.
 */
@RestController
public class DemoController {

    @GetMapping("/api/ping")
    public Map<String, Object> ping() {
        return Map.of(
                "status", "ok",
                "timestamp", Instant.now().toString()
        );
    }
}
