package com.example.foo;

import java.time.Instant;

public record WriteReceipt(String key, int byteCount, Instant writtenAt) {}
