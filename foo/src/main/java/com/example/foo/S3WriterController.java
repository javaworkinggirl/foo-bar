package com.example.foo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/foo/s3")
public class S3WriterController {

    private final S3WriterService writerService;

    public S3WriterController(S3WriterService writerService) {
        this.writerService = writerService;
    }

    @PostMapping("/write")
    public ResponseEntity<String> write(
            @RequestParam String key,
            @RequestParam String content) {
        writerService.write(key, content);
        return ResponseEntity.ok("Written to S3: " + key);
    }
}
