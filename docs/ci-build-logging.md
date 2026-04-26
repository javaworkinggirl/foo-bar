# CI Build Logging

## What We Did

The main Maven build step in CI redirects all output to a file rather than
printing to the console:

```yaml
run: mvn --batch-mode --no-transfer-progress --threads 2 verify -l target/maven-output.log
```

The `-l` flag tells Maven to write all output — compiler warnings, test results,
Testcontainers startup, Spring context logs — to `target/maven-output.log`.
A subsequent step uploads that file as a GitHub Actions artifact so it is
accessible from the Actions UI.

## Why

Writing to disk is significantly faster than flushing thousands of lines to the
GitHub Actions log stream. On a build with parallel modules, Testcontainers
startup noise, and Spring context output, this reduces the time spent on I/O
and keeps the Actions console uncluttered.

## The Trade-off

You can no longer watch the build progress in real time from the Actions UI.
The console shows only the step name while the build runs — nothing else until
it finishes.

To inspect the output after a build:

1. Open the GitHub Actions run
2. Scroll to the **Upload Maven log** step
3. Click the artifact link to download `maven-output.log`
4. Open the file locally to read the full build and test output
