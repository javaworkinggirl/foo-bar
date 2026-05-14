package com.example.test;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"PMD.TestClassWithoutTestCases", "PMD.AtLeastOneConstructor"})
public class TestLifecycleLogger implements BeforeEachCallback, AfterEachCallback {

    private static final Logger LOG = LoggerFactory.getLogger(TestLifecycleLogger.class);

    @Override
    public void beforeEach(final ExtensionContext context) {
        if (LOG.isInfoEnabled()) {
            LOG.info("[START] {} > {}",
                    context.getRequiredTestClass().getSimpleName(),
                    context.getDisplayName());
        }
    }

    @Override
    public void afterEach(final ExtensionContext context) {
        if (LOG.isInfoEnabled()) {
            LOG.info("[END]   {} > {}",
                    context.getRequiredTestClass().getSimpleName(),
                    context.getDisplayName());
        }
    }
}
