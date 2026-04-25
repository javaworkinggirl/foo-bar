package com.example.common;

import java.time.Instant;

public record Message(String id, String topic, String body, Instant sentAt) {

    public Message {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id must not be blank");
        if (body == null) throw new IllegalArgumentException("body must not be null");
    }
}
