# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

**Build and test everything (including integration tests):**
```
mvn verify
```

**Build in parallel (mirrors CI):**
```
mvn --threads 2 verify
```

**Build a specific module and its dependencies:**
```
mvn verify -pl foo --also-make
mvn verify -pl carnival --also-make
```

**Run only unit tests (skip integration tests):**
```
mvn test
```

**Run a single integration test class:**
```
mvn verify -pl foo -Dit.test=S3WriterServiceIT
mvn verify -pl integration-test -Dit.test=CarnivalBarIntegrationIT
```

**Build a Docker image (no Dockerfile needed — uses Buildpacks):**
```
mvn spring-boot:build-image -pl foo --also-make -DskipTests
```

## Architecture

This is a multi-module Maven project (Java 21, Spring Boot 3.5.14, AWS SDK v2).

**Module dependency graph:**
```
common  ←──  foo  ←──  carnival  ←──┐
common  ←──  bar                    integration-test
                   carnival, bar  ←─┘
```

- **`common`** — shared library: `S3Config` (beans), `S3Properties` (config record), `GreetingService`
- **`foo`** — Spring Boot app (port 8080); writes objects to S3 via `S3WriterService`
- **`bar`** — Spring Boot app; reads objects from S3 via `S3ReaderService`
- **`carnival`** — Spring Boot app; depends on `foo` (not `common` directly); publishes events to S3 under the `carnival/events/` key prefix using `S3WriterService`
- **`integration-test`** — no main code; cross-module IT that wires `CarnivalService` + `S3ReaderService` against a shared S3Mock container

## Integration Tests

All integration tests are named `*IT.java` and run via maven-failsafe-plugin in the `verify` phase. Each test class spins up its own `adobe/s3mock:latest` Testcontainers container on a random host port and uses `@DynamicPropertySource` to inject `app.s3.endpoint-override`. The bucket is created in `@BeforeAll` using a short-lived `S3Client`.

Docker must be running locally for integration tests to work.

## Dual-JAR Strategy

`foo`, `bar`, and `carnival` each produce two JARs via `<classifier>exec</classifier>` in the spring-boot-maven-plugin:
- `module-1.0.0-SNAPSHOT.jar` — thin JAR, Maven-importable (used by `integration-test` as a dependency)
- `module-1.0.0-SNAPSHOT-exec.jar` — fat JAR, self-contained (used for Docker images)

Without this classifier, Spring Boot would replace the thin JAR with the fat JAR, breaking the `integration-test` module's ability to import classes from `foo`, `bar`, or `carnival`.

## S3 Configuration

S3 is configured via `app.s3.*` properties (bound to `S3Properties`):
- `app.s3.bucket-name` — required; set via `S3_BUCKET_NAME` env var in production
- `app.s3.region` — defaults to `us-east-1` via `${AWS_REGION:us-east-1}`
- `app.s3.endpoint-override` — optional; when set, `S3Config` switches to path-style access with static `test/test` credentials (for S3Mock)

Production credentials use the default AWS credential chain (env vars `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY`).
