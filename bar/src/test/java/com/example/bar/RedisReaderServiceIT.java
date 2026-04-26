package com.example.bar;

import com.example.test.ContainerFactory;
import com.example.test.TestPropertyRegistrar;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class RedisReaderServiceIT {

    @Container
    static final GenericContainer<?> REDIS = ContainerFactory.redis();

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        TestPropertyRegistrar.registerRedis(registry, REDIS);
        registry.add("app.s3.bucket-name", () -> "RedisReaderServiceIT-test-bucket");
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
