# Redis Integration Testing Approach

## Why Testcontainers + real Redis (not a mock)

Integration tests use `redis:7-alpine` via Testcontainers rather than mocking `StringRedisTemplate`.

Mocking `StringRedisTemplate` would only verify that your code calls the right methods on the mock. It wouldn't catch serialization issues, key expiry behavior, connection pool exhaustion, or any real Redis semantics. The Testcontainers container gives you actual Redis behavior with full confidence at very low cost — it spins up in under a second on a warm Docker daemon and is completely isolated per test class.

## Contrast with S3Mock

S3Mock is a mock of the AWS S3 API — it simulates the HTTP protocol but isn't real S3. That tradeoff is accepted because you can't run real AWS in CI without credentials, network, cost, and flakiness.

`redis:7-alpine` is actual Redis — not a mock, not a simulator. The real Redis binary, running the real server. So the Redis tests are actually *more* faithful than the S3 tests. There's nothing further to mock or simulate — you're already at the bottom of the stack.

The only reasonable alternative to Testcontainers Redis would be a real shared dev/CI Redis instance, which is strictly worse for test isolation.

## One thing to consider

`redis:7-alpine` is unpinned. For reproducible builds, consider pinning to a specific version (e.g. `redis:7.2.4-alpine`), consistent with the same recommendation for `adobe/s3mock:latest`.
