package com.example.test;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.redpanda.RedpandaContainer;
import org.testcontainers.utility.DockerImageName;

public final class ContainerFactory {

    private ContainerFactory() {
    }

    public static GenericContainer<?> s3Mock() {
        final String version = System.getProperty("docker.s3mock.version", "5.0.0");
        return new GenericContainer<>(DockerImageName.parse("adobe/s3mock:" + version))
                .withExposedPorts(9090);
    }

    public static GenericContainer<?> redis() {
        final String version = System.getProperty("docker.redis.version", "7.4.8-alpine");
        return new GenericContainer<>(DockerImageName.parse("redis:" + version))
                .withExposedPorts(6379);
    }

    public static RedpandaContainer kafka() {
        final String version = System.getProperty("docker.redpanda.version", "v24.1.2");
        return new RedpandaContainer(
                DockerImageName.parse("docker.redpanda.com/redpandadata/redpanda:" + version));
    }
}
