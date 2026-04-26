package com.example.carnival;

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
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class CarnivalServiceIT {

    static final String BUCKET = "carnival-it-bucket";

    @Container
    static final GenericContainer<?> S3_MOCK = ContainerFactory.s3Mock();

    @DynamicPropertySource
    static void s3Properties(DynamicPropertyRegistry registry) {
        TestPropertyRegistrar.registerS3(registry, S3_MOCK, BUCKET);
    }

    @BeforeAll
    static void createBucket() throws InterruptedException {
        try (S3Client client = S3TestClient.forPort(S3_MOCK.getMappedPort(9090))) {
            client.createBucket(b -> b.bucket(BUCKET));
        }
        ContainerFactory.simulateSlowSetup("CARNIVAL");
    }

    @Autowired CarnivalService carnivalService;
    @Autowired S3Client s3Client;

    @Test
    void publishEventWritesPayloadAtExpectedKey() throws IOException {
        carnivalService.publishEvent("evt-100", "test payload");

        String key = carnivalService.keyFor("evt-100");
        try (var stream = s3Client.getObject(
                GetObjectRequest.builder().bucket(BUCKET).key(key).build())) {
            String actual = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertThat(actual).isEqualTo("test payload");
        }
    }

    @Test
    void publishEventOverwriteReplacesPayload() throws IOException {
        carnivalService.publishEvent("evt-101", "original");
        carnivalService.publishEvent("evt-101", "overwritten");

        String key = carnivalService.keyFor("evt-101");
        try (var stream = s3Client.getObject(
                GetObjectRequest.builder().bucket(BUCKET).key(key).build())) {
            String actual = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertThat(actual).isEqualTo("overwritten");
        }
    }
}
