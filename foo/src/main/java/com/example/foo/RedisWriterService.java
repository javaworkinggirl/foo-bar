package com.example.foo;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisWriterService {

    private final StringRedisTemplate redis;

    public RedisWriterService(final StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void write(final String key, final String value) {
        redis.opsForValue().set(key, value);
    }
}
