package com.example.bar;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bar/s3")
public class S3ReaderController {

    private final S3ReaderService readerService;

    public S3ReaderController(S3ReaderService readerService) {
        this.readerService = readerService;
    }

    @GetMapping("/read")
    public ResponseEntity<String> read(@RequestParam String key) {
        String content = readerService.read(key);
        return ResponseEntity.ok(content);
    }
}
