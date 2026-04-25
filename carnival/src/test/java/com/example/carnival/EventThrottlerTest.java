package com.example.carnival;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EventThrottlerTest {

    @Test
    void allowsEventsUpToLimit() {
        EventThrottler throttler = new EventThrottler(3, 1000);
        assertThat(throttler.tryAcquire("purchase")).isTrue();
        assertThat(throttler.tryAcquire("purchase")).isTrue();
        assertThat(throttler.tryAcquire("purchase")).isTrue();
    }

    @Test
    void rejectsEventOnceLimitReached() {
        EventThrottler throttler = new EventThrottler(2, 1000);
        throttler.tryAcquire("signup");
        throttler.tryAcquire("signup");
        assertThat(throttler.tryAcquire("signup")).isFalse();
    }

    @Test
    void tracksLimitsIndependentlyPerType() {
        EventThrottler throttler = new EventThrottler(1, 1000);
        throttler.tryAcquire("purchase");
        assertThat(throttler.tryAcquire("purchase")).isFalse();
        assertThat(throttler.tryAcquire("refund")).isTrue();
    }

    @Test
    void allowsEventsAgainAfterWindowExpires() throws InterruptedException {
        EventThrottler throttler = new EventThrottler(1, 50);
        throttler.tryAcquire("click");
        Thread.sleep(60);
        assertThat(throttler.tryAcquire("click")).isTrue();
    }
}
