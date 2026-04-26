package com.example.carnival;

import com.example.foo.RedisWriterService;
import org.springframework.stereotype.Service;

@Service
public class CarnivalRedisService {

    static final String KEY_PREFIX = "carnival:events:";

    private final RedisWriterService redisWriterService;

    public CarnivalRedisService(RedisWriterService redisWriterService) {
        this.redisWriterService = redisWriterService;
    }

    public void publishEvent(String eventId, String payload) {
        redisWriterService.write(KEY_PREFIX + eventId, payload);
    }

    public String keyFor(String eventId) {
        return KEY_PREFIX + eventId;
    }
}
