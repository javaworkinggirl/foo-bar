package com.example.carnival;

import java.time.Instant;

public record CarnivalEvent(String eventId, String type, String payload, Instant occurredAt) {

    public CarnivalEvent {
        if (eventId == null || eventId.isBlank()) throw new IllegalArgumentException("eventId must not be blank");
        if (type == null || type.isBlank()) throw new IllegalArgumentException("type must not be blank");
    }
}
