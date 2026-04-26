# Parallel Integration Test Isolation

When running `mvn --threads 2 verify`, `foo` and `bar` build and run integration tests concurrently. Three layers of isolation prevent conflicts:

## 1. Separate Docker Containers

Each test class starts its own `adobe/s3mock` container via `@Container static`. `foo`'s IT and `bar`'s IT never share a container. Containers are created via `ContainerFactory.s3Mock()` and `ContainerFactory.redis()` from `common-test`.

## 2. Random Host Ports

S3Mock listens inside the container on port `9090`, but Docker maps it to a random ephemeral host port (`getMappedPort(9090)`). Even if both containers start simultaneously, they bind to different OS ports and cannot collide. The same applies to Redis on port `6379`.

## 3. `@DynamicPropertySource`

Each test injects its own container's mapped port into `app.s3.endpoint-override` (and `spring.data.redis.host`/`port`) before the Spring context boots. This is done via `TestPropertyRegistrar.registerS3()` and `TestPropertyRegistrar.registerRedis()` from `common-test`, so the `S3Client` and `StringRedisTemplate` beans in each module point at that module's private containers, not shared ones.

## Result

`foo`'s IT talks to `localhost:PORT_A` (S3) and `localhost:PORT_B` (Redis), while `bar`'s IT talks to its own isolated `localhost:PORT_C` and `localhost:PORT_D`, fully independently, even when running concurrently under `--threads 2`.
