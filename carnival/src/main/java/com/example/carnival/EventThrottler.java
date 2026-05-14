package com.example.carnival;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sliding-window rate limiter per event type. Allows at most {@code maxPerWindow}
 * events of a given type within a rolling {@code windowMillis} period.
 */
public class EventThrottler {

    private final int maxPerWindow;
    private final long windowMillis;
    private final Map<String, Deque<Instant>> windows = new ConcurrentHashMap<>();

    public EventThrottler(final int maxPerWindow, final long windowMillis) {
        this.maxPerWindow = maxPerWindow;
        this.windowMillis = windowMillis;
    }

    public boolean tryAcquire(final String eventType) {
        final Instant now = Instant.now();
        final Deque<Instant> timestamps = windows.computeIfAbsent(eventType, k -> new ArrayDeque<>());
        final Instant cutoff = now.minusMillis(windowMillis);
        while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(cutoff)) {
            timestamps.pollFirst();
        }
        final boolean acquired = timestamps.size() < maxPerWindow;
        if (acquired) {
            timestamps.addLast(now);
        }
        return acquired;
    }
}
