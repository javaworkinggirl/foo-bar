package com.example.test;

import org.testcontainers.containers.GenericContainer;
import org.springframework.test.context.DynamicPropertyRegistry;

@SuppressWarnings("PMD.TestClassWithoutTestCases")
public final class TestPropertyRegistrar {

    private TestPropertyRegistrar() {
    }

    public static void registerS3(final DynamicPropertyRegistry registry, final GenericContainer<?> s3Mock, final String bucket) {
        registry.add("app.s3.bucket-name",       () -> bucket);
        registry.add("app.s3.region",            () -> "us-east-1");
        registry.add("app.s3.endpoint-override",
                () -> "http://localhost:" + s3Mock.getMappedPort(9090));
    }

    public static void registerRedis(final DynamicPropertyRegistry registry, final GenericContainer<?> redis) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    public static void registerKafka(final DynamicPropertyRegistry registry, final String bootstrapServers) {
        registry.add("spring.kafka.bootstrap-servers", () -> bootstrapServers);
    }
}
