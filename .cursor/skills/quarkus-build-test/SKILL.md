---
name: quarkus-build-test
description: Build, test, smoke-test, and run this Quarkus service - Maven wrapper commands, integration tests, dev mode, native builds, and Docker/Dev Services troubleshooting. Use when compiling, running tests or smoke tests, starting the dev server, packaging, or debugging a "configure the datasource / Docker daemon" build failure.
---

# Quarkus build and test

This project builds exclusively with Maven via the checked-in wrapper (`./mvnw`). Do not use Gradle: there is no Gradle build here, so `gradle`/`gradlew` commands and the Gradle variants shown in Quarkus docs (`quarkusDev`, `quarkusBuild`, `build.gradle` snippets) do not apply. Translate any Gradle-based upstream example to its Maven equivalent before using it.

All commands run from the repo root. Use `-B` in non-interactive contexts.

## Commands

| Task | Command |
|---|---|
| Compile | `./mvnw -B compile` |
| Unit + `@QuarkusTest` tests | `./mvnw -B test` |
| Everything incl. integration tests | `./mvnw -B verify -DskipITs=false` |
| Dev mode (live reload) | `./mvnw quarkus:dev` |
| Package (fast-jar in `target/quarkus-app/`) | `./mvnw -B package` |
| Native executable | `./mvnw -B package -Dnative` (add `-Dquarkus.native.container-build=true` without a local GraalVM; needs Docker) |
| Security SAST gate | `./mvnw -B compile spotbugs:check` (full scan suite: `security-scan` skill) |

Integration tests (`*IT.java`, annotated `@QuarkusIntegrationTest`) run against the packaged application via failsafe and are skipped by default (`skipITs=true` in `pom.xml`). The `native` profile (`-Dnative`) enables them automatically.

## Dev Services and Docker

`quarkus-jdbc-postgresql` makes tests and dev mode launch a disposable PostgreSQL container (Dev Services). This requires a running Docker daemon.

On a machine without Docker, the build fails with:

```text
Build step io.quarkus.datasource.deployment.devservices.DevServicesDatasourceProcessor#launchDatabases
threw an exception: java.lang.IllegalStateException: Please configure the datasource URL for default
datasource or ensure the Docker daemon is up and running.
```

Fix: disable Dev Services and supply a placeholder (or real) datasource URL. Verified to work for both `test` and `quarkus:dev` as long as no code path actually opens a connection:

```bash
./mvnw -B test -Dquarkus.datasource.devservices.enabled=false \
  -Dquarkus.datasource.jdbc.url=jdbc:postgresql://127.0.0.1:5432/app
```

## Dev mode for manual verification

Dev mode prompts for anonymous build analytics on first run, which hangs non-interactive shells for ~10 seconds; disable it. Run in a tmux session, wait for startup, then probe:

```bash
tmux new-session -d -s quarkus-dev -c "$PWD" -- ./mvnw quarkus:dev -Dquarkus.analytics.disabled=true
# wait ~30s for first-run compilation, then:
curl -s http://127.0.0.1:8080/hello
tmux kill-session -t quarkus-dev
```

Debug port 5005 is opened automatically in dev mode.

## Smoke testing this service

There is no Playwright/npm suite here; smoke verification is HTTP-level:

1. Fast: `./mvnw -B test` (REST Assured tests in `src/test/java` exercise endpoints in-process).
2. Packaged-app: `./mvnw -B verify -DskipITs=false` runs the `*IT` tests against the built artifact (needs Docker for the database).
3. Manual: dev-mode + `curl` loop above; check new endpoints return expected status codes and bodies.

## CI parity

- `.github/workflows/ci.yml`: `./mvnw verify -B` on PRs and pushes to `main` (GitHub runners have Docker, so Dev Services work there).
- `.github/workflows/security.yml`: Gitleaks, Semgrep, SpotBugs/FindSecBugs, and Trivy; all blocking. Run the same scans locally via the `security-scan` skill before pushing.
