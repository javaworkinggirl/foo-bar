package com.example.carnival;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SuppressWarnings("PMD.UseUtilityClass")
@SpringBootApplication(scanBasePackages = {
        "com.example.carnival",
        "com.example.foo",
        "com.example.common"
})
public class CarnivalApplication {
    public static void main(final String[] args) {
        SpringApplication.run(CarnivalApplication.class, args);
    }
}
