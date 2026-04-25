package com.example.carnival;

import com.example.foo.S3WriterService;
import org.springframework.stereotype.Service;

@Service
public class CarnivalService {

    static final String KEY_PREFIX = "carnival/events/";

    private final S3WriterService writerService;

    public CarnivalService(S3WriterService writerService) {
        this.writerService = writerService;
    }

    public void publishEvent(String eventId, String payload) {
        writerService.write(KEY_PREFIX + eventId, payload);
    }

    public String keyFor(String eventId) {
        return KEY_PREFIX + eventId;
    }
}
