package com.example.carnival;

import com.example.common.StockAlert;
import com.example.test.ContainerFactory;
import com.example.test.S3TestClient;
import com.example.test.TestPropertyRegistrar;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class StockAlertListenerIT {

    static final String BUCKET = "carnival-kafka-it-bucket";

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
    }

    @BeforeAll
    static void createBucket() {
        try (S3Client client = S3TestClient.forPort(S3_MOCK.getMappedPort(9090))) {
            client.createBucket(b -> b.bucket(BUCKET));
        }
    }

    @Autowired StockAlertListener listener;
    @Autowired KafkaTemplate<String, StockAlert> kafkaTemplate;

    @Test
    void listenerStoresAlertWhenMessageArrivesOnTopic() {
        kafkaTemplate.send(StockAlert.TOPIC, "popcorn", new StockAlert("popcorn", 3, "LOW"));

        await().atMost(10, SECONDS)
                .until(() -> listener.getLatest("popcorn") != null);

        assertThat(listener.getLatest("popcorn").severity()).isEqualTo("LOW");
        assertThat(listener.getLatest("popcorn").quantity()).isEqualTo(3);
    }

    @Test
    void listenerTracksLatestAlertPerItem() {
        kafkaTemplate.send(StockAlert.TOPIC, "lemonade", new StockAlert("lemonade", 10, "LOW"));
        kafkaTemplate.send(StockAlert.TOPIC, "lemonade", new StockAlert("lemonade", 1, "CRITICAL"));

        await().atMost(10, SECONDS)
                .until(() -> {
                    StockAlert latest = listener.getLatest("lemonade");
                    return latest != null && "CRITICAL".equals(latest.severity());
                });

        assertThat(listener.getLatest("lemonade").severity()).isEqualTo("CRITICAL");
    }
}
