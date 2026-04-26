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
        String version = System.getProperty("docker.s3mock.version", "5.0.0");
        return new GenericContainer<>(DockerImageName.parse("adobe/s3mock:" + version))
                .withExposedPorts(9090);
    }

    public static GenericContainer<?> redis() {
        String version = System.getProperty("docker.redis.version", "7.4.8-alpine");
        return new GenericContainer<>(DockerImageName.parse("redis:" + version))
                .withExposedPorts(6379);
    }
}
