package org.example.rateforge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.List;

/**
 * Wires up the Redis connection and loads each algorithm's Lua script
 * once at startup. Loading scripts as beans (instead of building the
 * script string in Java) keeps the atomic logic readable and lets Redis
 * cache the compiled script server-side.
 */
@Configuration
public class RedisConfig {

    @Bean
    public StringRedisTemplate redisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    @Bean
    public DefaultRedisScript<List> tokenBucketScript() {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("scripts/token_bucket.lua"));
        script.setResultType(List.class);
        return script;
    }

    @Bean
    public DefaultRedisScript<List> slidingWindowScript() {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("scripts/sliding_window.lua"));
        script.setResultType(List.class);
        return script;
    }

    @Bean
    public DefaultRedisScript<List> leakyBucketScript() {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("scripts/leaky_bucket.lua"));
        script.setResultType(List.class);
        return script;
    }
}
