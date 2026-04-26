# Pinned Docker Image Versions

## What We Did

Testcontainer image versions are pinned in the parent `pom.xml` and passed to
tests as system properties via Failsafe:

```xml
<properties>
    <docker.s3mock.version>5.0.0</docker.s3mock.version>
    <docker.redis.version>7.4.8-alpine</docker.redis.version>
</properties>
```

To change a version, update the property in `pom.xml`. Nothing else needs to change.

## Why

Using `latest` tags means a Docker image can change silently between CI runs.
A breaking change in a new image version would cause a flaky or failing build
with no corresponding code change — hard to diagnose and hard to reproduce locally.

Pinning to an exact version guarantees every CI run pulls the same image, making
builds repeatable and failures trustworthy.  It also speeds up builds because
the image is downloaded once and cached.

## The Trade-off

Pinned versions become stale. This is a deliberate maintenance chore: roughly
every 6 months, review and upgrade both images to current stable versions.

When upgrading:
1. Update the versions in `pom.xml`
2. Run `mvn verify` locally to confirm all integration tests pass
3. Commit and push — CI will pull the new images on its next run
