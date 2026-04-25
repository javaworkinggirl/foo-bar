package com.example.carnival;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class CarnivalServiceIT {

    static final String BUCKET = "carnival-it-bucket";

    @Container
    static final GenericContainer<?> S3_MOCK =
            new GenericContainer<>(DockerImageName.parse("adobe/s3mock:latest"))
                    .withExposedPorts(9090);

    @DynamicPropertySource
    static void s3Properties(DynamicPropertyRegistry registry) {
        registry.add("app.s3.bucket-name",       () -> BUCKET);
        registry.add("app.s3.region",            () -> "us-east-1");
        registry.add("app.s3.endpoint-override",
                () -> "http://localhost:" + S3_MOCK.getMappedPort(9090));
    }

    @BeforeAll
    static void createBucket() throws InterruptedException{
        try (S3Client client = s3ClientForPort(S3_MOCK.getMappedPort(9090))) {
            client.createBucket(b -> b.bucket(BUCKET));
        }
            for (int i = 0; i < 250; i++) {
                System.out.println("[CARNIVAL] line " + i);
                Thread.sleep(50);
            }

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
