package com.example.foo;

import com.example.common.S3Properties;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3WriterService {

    private final S3Client s3Client;
    private final S3Properties props;

    public S3WriterService(S3Client s3Client, S3Properties props) {
        this.s3Client = s3Client;
        this.props = props;
    }

    public void write(String key, String content) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(props.bucketName())
                .key(key)
                .contentType("text/plain")
                .build();

        s3Client.putObject(request, RequestBody.fromString(content));
    }
}
