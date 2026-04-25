package com.example.common;

import java.util.List;
import java.util.Map;

/**
 * Routes a {@link Message} to a topic by scanning its body for the first
 * matching keyword from an ordered rule list. Falls back to a default topic
 * when no rule matches.
 */
public class TopicRouter {

    private final List<Map.Entry<String, String>> rules; // keyword -> topic
    private final String defaultTopic;

    public TopicRouter(List<Map.Entry<String, String>> rules, String defaultTopic) {
        this.rules = List.copyOf(rules);
        this.defaultTopic = defaultTopic;
    }

    public String route(Message message) {
        String lowerBody = message.body().toLowerCase();
        for (var rule : rules) {
            if (lowerBody.contains(rule.getKey().toLowerCase())) {
                return rule.getValue();
            }
        }
        return defaultTopic;
    }
}
