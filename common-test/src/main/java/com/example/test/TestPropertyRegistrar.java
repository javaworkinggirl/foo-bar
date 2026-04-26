package com.example.test;

import org.testcontainers.containers.GenericContainer;
import org.springframework.test.context.DynamicPropertyRegistry;

public class TestPropertyRegistrar {

    public static void registerS3(DynamicPropertyRegistry registry, GenericContainer<?> s3Mock, String bucket) {
        registry.add("app.s3.bucket-name",       () -> bucket);
        registry.add("app.s3.region",            () -> "us-east-1");
        registry.add("app.s3.endpoint-override",
                () -> "http://localhost:" + s3Mock.getMappedPort(9090));
    }

    public static void registerRedis(DynamicPropertyRegistry registry, GenericContainer<?> redis) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }
}
