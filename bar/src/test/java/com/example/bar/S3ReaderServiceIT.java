package com.example.bar;

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
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class S3ReaderServiceIT {

    static final String BUCKET  = "bar-it-bucket";
    static final String KEY     = "reader-test/message.txt";
    static final String CONTENT = "seeded directly into s3mock for bar test";

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
            System.out.println("[BAR] line " + i);
            Thread.sleep(50);
        }
    }

    @Autowired S3ReaderService readerService;
    @Autowired S3Client s3Client;

    @BeforeEach
    void seedS3Mock() {
        s3Client.putObject(
                PutObjectRequest.builder().bucket(BUCKET).key(KEY).build(),
                RequestBody.fromString(CONTENT));
    }

    @Test
    void readsExistingObjectFromS3() {
        String actual = readerService.read(KEY);
        assertThat(actual).isEqualTo(CONTENT);
    }

    @Test
    void throwsWhenKeyDoesNotExist() {
        assertThatThrownBy(() -> readerService.read("does-not-exist/missing.txt"))
                .isInstanceOf(Exception.class);
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
