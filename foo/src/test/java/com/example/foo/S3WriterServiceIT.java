package com.example.foo;

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
class S3WriterServiceIT {

    static final String BUCKET = "foo-it-bucket";

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
    static void createBucket() throws InterruptedException {
        try (S3Client client = S3TestClient.forPort(S3_MOCK.getMappedPort(9090))) {
            client.createBucket(b -> b.bucket(BUCKET));
        }
        ContainerFactory.simulateSlowSetup("FOO");
    }

    @Autowired S3WriterService writerService;
    @Autowired S3Client s3Client;

    static final String KEY     = "writer-test/message.txt";
    static final String CONTENT = "written by foo integration test";

    @Test
    void writesToS3AndContentIsVerifiableDirectly() throws IOException {
        writerService.write(KEY, CONTENT);

        try (var stream = s3Client.getObject(
                GetObjectRequest.builder().bucket(BUCKET).key(KEY).build())) {
            assertThat(new String(stream.readAllBytes(), StandardCharsets.UTF_8)).isEqualTo(CONTENT);
        }
    }

    @Test
    void overwritingKeyReplacesContent() throws IOException {
        writerService.write(KEY, "first");
        writerService.write(KEY, "second");

        try (var stream = s3Client.getObject(
                GetObjectRequest.builder().bucket(BUCKET).key(KEY).build())) {
            assertThat(new String(stream.readAllBytes(), StandardCharsets.UTF_8)).isEqualTo("second");
        }
    }
}
