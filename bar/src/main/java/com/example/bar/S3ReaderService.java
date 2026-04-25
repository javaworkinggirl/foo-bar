package com.example.bar;

import com.example.common.S3Properties;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

@Service
public class S3ReaderService {

    private final S3Client s3Client;
    private final S3Properties props;

    public S3ReaderService(S3Client s3Client, S3Properties props) {
        this.s3Client = s3Client;
        this.props = props;
    }

    public String read(String key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(props.bucketName())
                .key(key)
                .build();

        try (ResponseInputStream<GetObjectResponse> response = s3Client.getObject(request)) {
            return new String(response.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read S3 object: " + key, e);
        }
    }
}
