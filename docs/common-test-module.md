# common-test Module

`common-test` is a shared test utility library imported as a `test`-scoped dependency by `foo`, `bar`, `carnival`, and `integration-test`. It has no production code and is never deployed.

## Why it exists

Without it, boilerplate like the S3Mock client factory, container declarations, and `@DynamicPropertySource` registration patterns were copy-pasted identically across multiple IT classes. `common-test` gives one place to change them.

## What's in it

### `ContainerFactory`

Static factory methods for creating Testcontainers containers. Image versions are
read from system properties set by Failsafe (defined in the parent `pom.xml`),
with hardcoded fallbacks for running tests directly from an IDE:

```java
ContainerFactory.s3Mock()  // adobe/s3mock — version from docker.s3mock.version
ContainerFactory.redis()   // redis alpine  — version from docker.redis.version
ContainerFactory.simulateSlowSetup("FOO")  // artificial delay used in @BeforeAll to demonstrate parallel builds
```

### `TestPropertyRegistrar`

Registers container connection properties into a `DynamicPropertyRegistry`:

```java
TestPropertyRegistrar.registerS3(registry, s3MockContainer, bucketName);
TestPropertyRegistrar.registerRedis(registry, redisContainer);
```

### `S3TestClient`

Builds a short-lived `S3Client` pointed at an S3Mock container — used in `@BeforeAll` to create buckets before the Spring context starts:

```java
S3Client client = S3TestClient.forPort(S3_MOCK.getMappedPort(9090));
```

### `TestLifecycleLogger`

A JUnit 5 extension that prints `[START]` and `[END]` lines for every test method.
Auto-registered via `META-INF/services/org.junit.jupiter.api.extension.Extension` —
no `@ExtendWith` needed on any test class. Enabled per module by setting
`junit.jupiter.extensions.autodetection.enabled=true` in `junit-platform.properties`.

```
[START] S3WriterServiceIT > writesToS3AndContentIsVerifiableDirectly()
[END]   S3WriterServiceIT > writesToS3AndContentIsVerifiableDirectly()
```

## Usage pattern in an IT class

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class MyServiceIT {

    @Container
    static final GenericContainer<?> S3_MOCK = ContainerFactory.s3Mock();
    @Container
    static final GenericContainer<?> REDIS = ContainerFactory.redis();

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        TestPropertyRegistrar.registerS3(registry, S3_MOCK, "my-bucket");
        TestPropertyRegistrar.registerRedis(registry, REDIS);
    }

    @BeforeAll
    static void setup() throws InterruptedException {
        try (S3Client client = S3TestClient.forPort(S3_MOCK.getMappedPort(9090))) {
            client.createBucket(b -> b.bucket("my-bucket"));
        }
    }
}
```
