package com.example.bar;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class WordFrequencyCounterTest {

    private final WordFrequencyCounter counter = new WordFrequencyCounter();

    @Test
    void returnsTopNWordsByFrequency() {
        List<Map.Entry<String, Long>> result =
                counter.topN("the cat sat on the mat the cat", 2);
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getKey()).isEqualTo("the");
        assertThat(result.get(0).getValue()).isEqualTo(3L);
        assertThat(result.get(1).getKey()).isEqualTo("cat");
        assertThat(result.get(1).getValue()).isEqualTo(2L);
    }

    @Test
    void isCaseInsensitive() {
        List<Map.Entry<String, Long>> result = counter.topN("Hello hello HELLO", 1);
        assertThat(result.get(0).getKey()).isEqualTo("hello");
        assertThat(result.get(0).getValue()).isEqualTo(3L);
    }

    @Test
    void returnsEmptyListForBlankInput() {
        assertThat(counter.topN("   ", 5)).isEmpty();
        assertThat(counter.topN(null, 5)).isEmpty();
    }

    @Test
    void limitsResultsToN() {
        List<Map.Entry<String, Long>> result = counter.topN("a b c d e", 3);
        assertThat(result).hasSize(3);
    }
}
