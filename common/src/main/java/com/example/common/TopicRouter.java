package com.example.common;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Routes a {@link Message} to a topic by scanning its body for the first
 * matching keyword from an ordered rule list. Falls back to a default topic
 * when no rule matches.
 */
public class TopicRouter {

    private final List<Map.Entry<String, String>> rules; // keyword -> topic
    private final String defaultTopic;

    public TopicRouter(final List<Map.Entry<String, String>> rules, final String defaultTopic) {
        this.rules = List.copyOf(rules);
        this.defaultTopic = defaultTopic;
    }

    public String route(final Message message) {
        final String lowerBody = message.body().toLowerCase(Locale.ROOT);
        String result = defaultTopic;
        for (final Map.Entry<String, String> rule : rules) {
            if (lowerBody.contains(rule.getKey().toLowerCase(Locale.ROOT))) {
                result = rule.getValue();
                break;
            }
        }
        return result;
    }
}
