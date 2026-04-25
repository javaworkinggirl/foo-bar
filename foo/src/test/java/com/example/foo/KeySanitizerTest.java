package com.example.foo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KeySanitizerTest {

    private final KeySanitizer sanitizer = new KeySanitizer();

    @Test
    void passesCleanKeyUnchanged() {
        assertThat(sanitizer.sanitize("events/2024/file.txt")).isEqualTo("events/2024/file.txt");
    }

    @Test
    void stripsIllegalCharacters() {
        assertThat(sanitizer.sanitize("events/my-file(1).txt")).isEqualTo("events/my-file1.txt");
    }

    @Test
    void collapsesDoubleSlashes() {
        assertThat(sanitizer.sanitize("events//archive///old.txt")).isEqualTo("events/archive/old.txt");
    }

    @Test
    void stripsLeadingAndTrailingSlashes() {
        assertThat(sanitizer.sanitize("/events/file/")).isEqualTo("events/file");
    }

    @Test
    void throwsOnBlankKey() {
        assertThatThrownBy(() -> sanitizer.sanitize("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
