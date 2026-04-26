package com.example.it;

import com.example.bar.RedisReaderService;
import com.example.bar.S3ReaderService;
import com.example.carnival.CarnivalRedisService;
import com.example.carnival.CarnivalService;
import com.example.test.ContainerFactory;
import com.example.test.S3TestClient;
import com.example.test.TestPropertyRegistrar;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.services.s3.S3Client;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = IntegrationTestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class WholeSystemIntegrationTest {

    static final String BUCKET = "carnival-bar-bucket";

    @Container
    static final GenericContainer<?> S3_MOCK = ContainerFactory.s3Mock();

    @Container
    static final GenericContainer<?> REDIS = ContainerFactory.redis();

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        TestPropertyRegistrar.registerS3(registry, S3_MOCK, BUCKET);
        TestPropertyRegistrar.registerRedis(registry, REDIS);
    }

    @BeforeAll
    static void createBucket() {
        try (S3Client client = S3TestClient.forPort(S3_MOCK.getMappedPort(9090))) {
            client.createBucket(b -> b.bucket(BUCKET));
        }
    }

    @Autowired CarnivalService carnivalService;
    @Autowired S3ReaderService s3ReaderService;
    @Autowired CarnivalRedisService carnivalRedisService;
    @Autowired RedisReaderService redisReaderService;

    // ── S3 tests ──────────────────────────────────────────────────────────────

    @Test
    void carnivalPublishesEventAndBarReadsItFromS3() {
        carnivalService.publishEvent("evt-001", "ride the ferris wheel");

        assertThat(s3ReaderService.read(carnivalService.keyFor("evt-001")))
                .isEqualTo("ride the ferris wheel");
    }

    @Test
    void carnivalOverwriteIsReflectedInBarS3Read() {
        carnivalService.publishEvent("evt-002", "first");
        carnivalService.publishEvent("evt-002", "revised");

        assertThat(s3ReaderService.read(carnivalService.keyFor("evt-002")))
                .isEqualTo("revised");
    }

    // ── Redis tests ───────────────────────────────────────────────────────────

    @Test
    void carnivalPublishesEventAndBarReadsItFromRedis() {
        carnivalRedisService.publishEvent("evt-r01", "spin the teacups");

        assertThat(redisReaderService.read(carnivalRedisService.keyFor("evt-r01")))
                .isEqualTo("spin the teacups");
    }

    @Test
    void carnivalRedisOverwriteIsReflectedInBarRead() {
        carnivalRedisService.publishEvent("evt-r02", "first");
        carnivalRedisService.publishEvent("evt-r02", "updated");

        assertThat(redisReaderService.read(carnivalRedisService.keyFor("evt-r02")))
                .isEqualTo("updated");
    }
}
