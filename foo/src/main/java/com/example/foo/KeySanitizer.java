package com.example.foo;

@SuppressWarnings("PMD.AtLeastOneConstructor")
public class KeySanitizer {

    public String sanitize(final String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key must not be blank");
        }
        return key
                .replaceAll("[^a-zA-Z0-9/_\\-.]", "")
                .replaceAll("/+", "/")
                .replaceAll("^/+|/+$", "");
    }
}
