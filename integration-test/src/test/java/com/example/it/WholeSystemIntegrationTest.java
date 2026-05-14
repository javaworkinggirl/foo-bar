package com.example.it;

import com.example.bar.RedisReaderService;
import com.example.bar.S3ReaderService;
import com.example.bar.StockAlertPublisher;
import com.example.carnival.CarnivalRedisService;
import com.example.carnival.CarnivalService;
import com.example.carnival.StockAlertListener;
import com.example.common.StockAlert;
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
import org.testcontainers.redpanda.RedpandaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.services.s3.S3Client;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

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
    @Container
    static final RedpandaContainer KAFKA = ContainerFactory.kafka();

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        TestPropertyRegistrar.registerS3(registry, S3_MOCK, BUCKET);
        TestPropertyRegistrar.registerRedis(registry, REDIS);
        TestPropertyRegistrar.registerKafka(registry, KAFKA.getBootstrapServers());
        registry.add("spring.kafka.consumer.group-id",        () -> "integration-test");
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
        registry.add("spring.kafka.consumer.key-deserializer",
                () -> "org.apache.kafka.common.serialization.StringDeserializer");
        registry.add("spring.kafka.consumer.value-deserializer",
                () -> "org.springframework.kafka.support.serializer.JsonDeserializer");
        registry.add("spring.kafka.consumer.properties.spring.json.trusted.packages",
                () -> "com.example.common");
        registry.add("spring.kafka.consumer.properties.spring.json.value.default.type",
                () -> "com.example.common.StockAlert");
        registry.add("spring.kafka.producer.key-serializer",
                () -> "org.apache.kafka.common.serialization.StringSerializer");
        registry.add("spring.kafka.producer.value-serializer",
                () -> "org.springframework.kafka.support.serializer.JsonSerializer");
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
    @Autowired StockAlertPublisher stockAlertPublisher;
    @Autowired StockAlertListener stockAlertListener;

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

    // ── Kafka tests ───────────────────────────────────────────────────────────

    @Test
    void barPublishesStockAlertAndCarnivalReceivesIt() {
        stockAlertPublisher.publish(new StockAlert("corn-dog", 4, "LOW"));

        await().atMost(10, SECONDS)
                .until(() -> stockAlertListener.getLatest("corn-dog") != null);

        assertThat(stockAlertListener.getLatest("corn-dog").severity()).isEqualTo("LOW");
    }

    @Test
    void carnivalTracksLatestAlertWhenBarPublishesMultipleTimes() {
        stockAlertPublisher.publish(new StockAlert("hot-dog", 8, "LOW"));
        stockAlertPublisher.publish(new StockAlert("hot-dog", 1, "CRITICAL"));

        await().atMost(10, SECONDS)
                .until(() -> {
                    StockAlert latest = stockAlertListener.getLatest("hot-dog");
                    return latest != null && "CRITICAL".equals(latest.severity());
                });

        assertThat(stockAlertListener.getLatest("hot-dog").severity()).isEqualTo("CRITICAL");
    }
}
