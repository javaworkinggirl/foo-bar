package com.example.foo;

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
class RedisWriterServiceIT {

    @Container
    static final GenericContainer<?> REDIS = ContainerFactory.redis();

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        TestPropertyRegistrar.registerRedis(registry, REDIS);
        registry.add("app.s3.bucket-name", () -> "test-bucket");
        registry.add("app.s3.region",      () -> "us-east-1");
    }

    @Autowired RedisWriterService writerService;
    @Autowired StringRedisTemplate redis;

    @Test
    void writesValueToRedis() {
        writerService.write("foo:test:key", "hello from foo");

        assertThat(redis.opsForValue().get("foo:test:key")).isEqualTo("hello from foo");
    }

    @Test
    void overwritingKeyReplacesValue() {
        writerService.write("foo:test:overwrite", "first");
        writerService.write("foo:test:overwrite", "second");

        assertThat(redis.opsForValue().get("foo:test:overwrite")).isEqualTo("second");
    }
}
