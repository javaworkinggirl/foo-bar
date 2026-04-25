package com.example.bar;

import com.example.common.GreetingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BarController {

    private final GreetingService greetingService;

    public BarController(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    @GetMapping("/bar")
    public String hello() {
        return greetingService.greet("bar");
    }
}
