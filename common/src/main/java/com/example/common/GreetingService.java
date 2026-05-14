package com.example.common;

import org.springframework.stereotype.Service;

@SuppressWarnings("PMD.AtLeastOneConstructor")
@Service
public class GreetingService {

    public String greet(final String name) {
        return "Hello from common, " + name + "!";
    }
}
