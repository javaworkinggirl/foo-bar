# Redis Integration Testing Approach

## Why Testcontainers + real Redis (not a mock)

Integration tests use `redis:7.4.8-alpine` via Testcontainers rather than mocking `StringRedisTemplate`.

`redis:7.4.8-alpine` is actual Redis — not a mock, not a simulator. The real Redis binary, running the real server. So the Redis tests are actually *more* faithful than the S3 tests. There's nothing further to mock or simulate — you're already at the bottom of the stack.

## Contrast with S3Mock

S3Mock is a mock of the AWS S3 API — it simulates the HTTP protocol but isn't real S3. That tradeoff is accepted because you can't run real AWS in CI without credentials, network, cost, and flakiness.

## Version pinning

Both `redis` and `adobe/s3mock` are pinned to exact versions in the parent `pom.xml`.
See `docs/pinned-docker-versions.md` for the rationale and upgrade process.
