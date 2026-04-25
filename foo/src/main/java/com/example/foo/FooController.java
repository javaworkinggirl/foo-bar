package com.example.foo;

import com.example.common.GreetingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FooController {

    private final GreetingService greetingService;

    public FooController(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    @GetMapping("/foo")
    public String hello() {
        return greetingService.greet("foo");
    }
}
