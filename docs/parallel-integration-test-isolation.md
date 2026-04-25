# Parallel Integration Test Isolation

When running `mvn --threads 2 verify`, `foo` and `bar` build and run integration tests concurrently. Three layers of isolation prevent conflicts:

## 1. Separate Docker Containers

Each test class starts its own `adobe/s3mock` container via `@Container static`. `foo`'s IT and `bar`'s IT never share a container.

## 2. Random Host Ports

S3Mock listens inside the container on port `9090`, but Docker maps it to a random ephemeral host port (`getMappedPort(9090)`). Even if both containers start simultaneously, they bind to different OS ports and cannot collide.

## 3. `@DynamicPropertySource`

Each test injects its own container's mapped port into `app.s3.endpoint-override` before the Spring context boots. This means the `S3Client` bean in each module points at that module's private container, not a shared one.

## Result

`foo`'s IT talks to `localhost:PORT_A` and `bar`'s IT talks to `localhost:PORT_B`, fully independently, even when running concurrently under `--threads 2`.
