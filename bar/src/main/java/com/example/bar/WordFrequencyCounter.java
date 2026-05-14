package com.example.bar;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("PMD.AtLeastOneConstructor")
public class WordFrequencyCounter {

    public List<Map.Entry<String, Long>> topN(final String text, final int count) {
        final List<Map.Entry<String, Long>> result;
        if (text == null || text.isBlank()) {
            result = List.of();
        } else {
            result = Arrays.stream(text.toLowerCase(Locale.ROOT).split("\\W+"))
                    .filter(w -> !w.isBlank())
                    .collect(Collectors.groupingBy(w -> w, Collectors.counting()))
                    .entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(count)
                    .collect(Collectors.toList());
        }
        return result;
    }
}
