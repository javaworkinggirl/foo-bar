package com.example.bar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SuppressWarnings("PMD.UseUtilityClass")
@SpringBootApplication(scanBasePackages = {"com.example.bar", "com.example.common"})
public class BarApplication {
    public static void main(final String[] args) {
        SpringApplication.run(BarApplication.class, args);
    }
}
