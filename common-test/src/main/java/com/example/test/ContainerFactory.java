package com.example.test;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class ContainerFactory {

    public static void simulateSlowSetup(String appName) throws InterruptedException {
        for (int i = 0; i < 250; i++) {
            System.out.println("[" + appName + "] line " + i);
            Thread.sleep(50);
        }
    }

    public static GenericContainer<?> s3Mock() {
        return new GenericContainer<>(DockerImageName.parse("adobe/s3mock:latest"))
                .withExposedPorts(9090);
    }

    public static GenericContainer<?> redis() {
        return new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                .withExposedPorts(6379);
    }
}
