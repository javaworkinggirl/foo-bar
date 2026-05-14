package com.example.carnival;

import com.example.foo.RedisWriterService;
import org.springframework.stereotype.Service;

@Service
public class CarnivalRedisService {

    /* default */ static final String KEY_PREFIX = "carnival:events:";

    private final RedisWriterService redisWriter;

    public CarnivalRedisService(final RedisWriterService redisWriter) {
        this.redisWriter = redisWriter;
    }

    public void publishEvent(final String eventId, final String payload) {
        redisWriter.write(KEY_PREFIX + eventId, payload);
    }

    public String keyFor(final String eventId) {
        return KEY_PREFIX + eventId;
    }
}
