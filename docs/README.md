# Documentation

## CI

- [CI Build Logging](ci-build-logging.md) — Why Maven output goes to a file in CI and how to access it after a build

## Architecture

- [Module Dependencies](module-dependencies.md) — Mermaid diagram of the Maven module dependency graph

## Testing
- [Redis Testing Approach](redis-testing-approach.md) — Why real Redis via Testcontainers instead of mocking
- [Pinned Docker Versions](pinned-docker-versions.md) — Why image versions are pinned in `pom.xml` and how to upgrade them
- [Common Test Module](common-test-module.md) — Shared test utilities in `common-test`: `ContainerFactory`, `TestPropertyRegistrar`, `S3TestClient`, `TestLifecycleLogger`
- [Parallel Integration Test Isolation](parallel-integration-test-isolation.md) — How `foo` and `bar` run integration tests concurrently without conflicts (separate containers, random ports, `@DynamicPropertySource`)
- [JUnit 5 Parallel Execution](junit-parallel-execution.md) — Class-level parallelism config, method sequencing, and key isolation rules
