package com.example.test;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class TestLifecycleLogger implements BeforeEachCallback, AfterEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        System.out.printf("[START] %s > %s%n",
                context.getRequiredTestClass().getSimpleName(),
                context.getDisplayName());
    }

    @Override
    public void afterEach(ExtensionContext context) {
        System.out.printf("[END]   %s > %s%n",
                context.getRequiredTestClass().getSimpleName(),
                context.getDisplayName());
    }
}
