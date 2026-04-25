package com.example.carnival;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Sliding-window rate limiter per event type. Allows at most {@code maxPerWindow}
 * events of a given type within a rolling {@code windowMillis} period.
 */
public class EventThrottler {

    private final int maxPerWindow;
    private final long windowMillis;
    private final Map<String, Deque<Instant>> windows = new HashMap<>();

    public EventThrottler(int maxPerWindow, long windowMillis) {
        this.maxPerWindow = maxPerWindow;
        this.windowMillis = windowMillis;
    }

    public boolean tryAcquire(String eventType) {
        Instant now = Instant.now();
        Deque<Instant> timestamps = windows.computeIfAbsent(eventType, k -> new ArrayDeque<>());
        Instant cutoff = now.minusMillis(windowMillis);
        while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(cutoff)) {
            timestamps.pollFirst();
        }
        if (timestamps.size() < maxPerWindow) {
            timestamps.addLast(now);
            return true;
        }
        return false;
    }
}
