# JUnit 5 Parallel Test Execution

## What We Enabled

Each module with integration tests (`foo`, `bar`, `integration-test`) has a
`src/test/resources/junit-platform.properties` file with:

```properties
junit.jupiter.execution.parallel.enabled=true
junit.jupiter.execution.parallel.mode.default=same_thread
junit.jupiter.execution.parallel.mode.classes.default=concurrent
junit.jupiter.execution.parallel.config.strategy=fixed
junit.jupiter.execution.parallel.config.fixed.parallelism=2
```

The two JUnit properties control different scopes:

- `mode.default` — controls **test methods** (set to `same_thread` → sequential within a class)
- `mode.classes.default` — controls **test classes** (set to `concurrent` → classes run in parallel)

Combined with `mvn --threads 2`, this gives two levels of parallelism:

| Level | Mechanism | Effect |
|---|---|---|
| Module | Maven `--threads 2` | `foo` and `bar` build concurrently |
| Class | JUnit `mode.classes.default=concurrent` | `S3WriterServiceIT` and `RedisWriterServiceIT` run concurrently within `foo` |
| Method | JUnit `mode.default=same_thread` | Test methods within a class run sequentially on one thread |

## How Isolation Works

**Between classes:** Each test class starts its own containers (`@Container` static
fields) and registers its own ports via `@DynamicPropertySource`. Spring creates a
separate application context per class. Two classes running at the same time have
completely independent S3Mock and Redis instances — no shared state.

**Within a class:** `same_thread` means all test methods in a class execute on the
same thread, one at a time. Methods are sequential, so they can safely share
resources like S3 keys or Redis keys without conflicting with each other.

## Trade-off: Key Isolation Is a Class-Level Concern, Not Method-Level

Because methods are sequential within a class, two methods in the same class
writing to the same S3 key are fine. The isolation requirement is only between
*classes* — and since each class has its own container, that is already satisfied
automatically.

```java
// This is safe — methods run sequentially on one thread
static final String KEY = "writer-test/message.txt";

@Test void writesToS3() { writerService.write(KEY, CONTENT); ... }
@Test void overwritingKeyReplacesContent() { writerService.write(KEY, "second"); ... }
```

## What You Do Need to Watch For

If a future test class shares an external resource with another class (e.g. a
singleton container or a fixed port), that resource would need protection. With
the current design — each class owns its own containers — this does not apply.

However, if the design ever moves to shared containers (Testcontainers Singleton
Pattern), two classes running in parallel would share the same S3Mock bucket.
In that case, prefix every S3 key and Redis key with the test class's simple name
to prevent collisions.