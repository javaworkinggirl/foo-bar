package com.example.it;

import com.example.bar.RedisReaderService;
import com.example.bar.S3ReaderService;
import com.example.carnival.CarnivalRedisService;
import com.example.carnival.CarnivalService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = IntegrationTestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class CarnivalBarIntegrationIT {

    static final String BUCKET = "carnival-bar-bucket";

    @Container
    static final GenericContainer<?> S3_MOCK =
            new GenericContainer<>(DockerImageName.parse("adobe/s3mock:latest"))
                    .withExposedPorts(9090);

    @Container
    static final GenericContainer<?> REDIS =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("app.s3.bucket-name",       () -> BUCKET);
        registry.add("app.s3.region",            () -> "us-east-1");
        registry.add("app.s3.endpoint-override",
                () -> "http://localhost:" + S3_MOCK.getMappedPort(9090));
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
    }

    @BeforeAll
    static void createBucket() {
        try (S3Client client = s3ClientForPort(S3_MOCK.getMappedPort(9090))) {
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

    static S3Client s3ClientForPort(int port) {
        return S3Client.builder()
                .endpointOverride(URI.create("http://localhost:" + port))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test", "test")))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }
}
