package com.example.bar;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class RedisReaderServiceIT {

    @Container
    static final GenericContainer<?> REDIS =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
        registry.add("app.s3.bucket-name", () -> "test-bucket");
        registry.add("app.s3.region",      () -> "us-east-1");
    }

    @Autowired RedisReaderService readerService;
    @Autowired StringRedisTemplate redis;

    @Test
    void readsValueSeededDirectlyIntoRedis() {
        redis.opsForValue().set("bar:test:key", "seeded value");

        assertThat(readerService.read("bar:test:key")).isEqualTo("seeded value");
    }

    @Test
    void returnsNullForMissingKey() {
        assertThat(readerService.read("bar:test:does-not-exist")).isNull();
    }
}
