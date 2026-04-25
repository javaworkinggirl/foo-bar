package com.example.bar;

import java.time.Instant;

public record FetchResult(String key, String content, Instant fetchedAt) {}
