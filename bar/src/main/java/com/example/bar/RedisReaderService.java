package com.example.bar;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisReaderService {

    private final StringRedisTemplate redis;

    public RedisReaderService(final StringRedisTemplate redis) {
        this.redis = redis;
    }

    public String read(final String key) {
        return redis.opsForValue().get(key);
    }
}
