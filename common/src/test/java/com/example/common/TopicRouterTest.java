package com.example.common;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TopicRouterTest {

    private final TopicRouter router = new TopicRouter(
            List.of(
                    Map.entry("error", "alerts"),
                    Map.entry("payment", "billing"),
                    Map.entry("login", "auth")
            ),
            "general"
    );

    private Message msg(String body) {
        return new Message("id-1", null, body, Instant.now());
    }

    @Test
    void routesToFirstMatchingKeyword() {
        assertThat(router.route(msg("payment gateway error"))).isEqualTo("alerts");
    }

    @Test
    void routesWhenOnlySecondRuleMatches() {
        assertThat(router.route(msg("payment received successfully"))).isEqualTo("billing");
    }

    @Test
    void routesToDefaultWhenNoRuleMatches() {
        assertThat(router.route(msg("user updated their profile"))).isEqualTo("general");
    }

    @Test
    void matchingIsCaseInsensitive() {
        assertThat(router.route(msg("User LOGIN attempt"))).isEqualTo("auth");
    }
}
