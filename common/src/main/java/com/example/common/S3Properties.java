package com.example.common;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.s3")
public record S3Properties(
        String bucketName,
        String region,
        // null in production — set only in tests to redirect to S3Mock
        String endpointOverride) {}
