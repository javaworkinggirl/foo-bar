package com.example.bar;

import com.example.common.StockAlert;
import com.example.test.ContainerFactory;
import com.example.test.S3TestClient;
import com.example.test.TestPropertyRegistrar;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.redpanda.RedpandaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.services.s3.S3Client;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class StockAlertPublisherIT {

    static final String BUCKET = "bar-kafka-it-bucket";

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

    @Autowired StockAlertPublisher publisher;

    @Test
    void publishedAlertArrivesOnTopic() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                KAFKA.getBootstrapServers(), "pub-test", "true");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        try (Consumer<String, String> consumer =
                     new org.springframework.kafka.core.DefaultKafkaConsumerFactory<String, String>(consumerProps)
                             .createConsumer()) {
            consumer.subscribe(List.of(StockAlert.TOPIC));
            seekToEnd(consumer);

            publisher.publish(new StockAlert("cotton-candy", 5, "LOW"));

            ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer);
            assertThat(records).hasSize(1);
            var record = records.iterator().next();
            assertThat(record.key()).isEqualTo("cotton-candy");
            assertThat(record.value()).contains("LOW");
        }
    }

    @Test
    void publishedAlertKeyMatchesItemName() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                KAFKA.getBootstrapServers(), "key-test", "true");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        try (Consumer<String, String> consumer =
                     new org.springframework.kafka.core.DefaultKafkaConsumerFactory<String, String>(consumerProps)
                             .createConsumer()) {
            consumer.subscribe(List.of(StockAlert.TOPIC));
            seekToEnd(consumer);

            publisher.publish(new StockAlert("funnel-cake", 2, "CRITICAL"));

            ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer);
            assertThat(records.iterator().next().key()).isEqualTo("funnel-cake");
        }
    }

    private static void seekToEnd(Consumer<String, String> consumer) {
        // Poll until Kafka assigns partitions, then seek to end so tests only see messages they publish.
        while (consumer.assignment().isEmpty()) {
            consumer.poll(Duration.ofMillis(100));
        }
        consumer.seekToEnd(consumer.assignment());
        // seekToEnd is lazy — force immediate resolution before we publish so the position
        // is captured before the new message arrives (otherwise we can land past it).
        consumer.assignment().forEach(consumer::position);
    }
}
