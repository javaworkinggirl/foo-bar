package com.example.bar;

import com.example.test.ContainerFactory;
import com.example.test.S3TestClient;
import com.example.test.TestPropertyRegistrar;
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
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class S3ReaderServiceIT {

    static final String BUCKET  = "bar-it-bucket";
    static final String KEY     = "reader-test/message.txt";
    static final String CONTENT = "seeded directly into s3mock for bar test";

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
        ContainerFactory.simulateSlowSetup("BAR");
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
}
