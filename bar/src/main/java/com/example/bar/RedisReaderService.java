package com.example.bar;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisReaderService {

    private final StringRedisTemplate redis;

    public RedisReaderService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public String read(String key) {
        return redis.opsForValue().get(key);
    }
}
