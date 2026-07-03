---
name: security-scan
description: Run the repo's security scan suite - Semgrep SAST, SpotBugs/FindSecBugs, Gitleaks secrets scan, and Trivy dependency/misconfig scan - and triage findings. Use before committing, before pushing or opening a PR, when the user asks for a security scan, SAST, vulnerability check, or secrets check, or when pom.xml or Dockerfiles change.
---

# Security scan

Run the right scanners for the current stage, fail on real findings, and fix rather than suppress. CI enforces the same suite in `.github/workflows/security.yml`, so anything skipped locally will block the PR.

## Stage matrix

| Stage | Run |
|---|---|
| Pre-commit | Secrets scan on staged changes |
| Pre-push / pre-PR | Diff-aware Semgrep + SpotBugs/FindSecBugs; add Trivy if `pom.xml`, `Dockerfile*`, or config changed |
| Full audit (on request, or first setup) | All scanners, full scope, including git history secrets scan |

## Tool bootstrap

Check availability first; install only what is missing.

```bash
command -v semgrep gitleaks trivy || true
```

- Semgrep: `python3 -m pip install --user semgrep` (needs network for registry rulesets)
- Trivy: `curl -sSfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/install.sh | sh -s -- -b "$HOME/.local/bin"`
- Gitleaks: download the `linux_x64` tarball from the latest [gitleaks release](https://github.com/gitleaks/gitleaks/releases) into `~/.local/bin`
- SpotBugs/FindSecBugs: no install needed; it is configured in `pom.xml` and runs through Maven

## 1. Secrets scan (Gitleaks)

Pre-commit, staged changes only:

```bash
gitleaks git --staged --redact --no-banner
```

Full history audit:

```bash
gitleaks git --redact --no-banner
```

Working tree only (fast, no git context):

```bash
gitleaks dir . --redact --no-banner
```

Exit code 1 means leaks were found. If a finding is a real credential, stop and report it — it must be rotated, not just removed. False positives go in `.gitleaksignore` by fingerprint (printed in the output), one line per finding, with a comment saying why.

## 2. SAST - Semgrep (source-level)

Diff-aware (pre-PR; scans only lines changed since the base):

```bash
semgrep scan --config p/java --config p/security-audit --config p/secrets \
  --metrics=off --error --baseline-commit "$(git merge-base origin/main HEAD)"
```

Full scan (audits and CI parity):

```bash
semgrep scan --config p/java --config p/security-audit --config p/secrets \
  --metrics=off --error
```

`--error` makes the exit code nonzero when findings exist. Registry configs (`p/...`) need network access; if offline, note that Semgrep was skipped and rely on SpotBugs plus CI.

## 3. SAST - SpotBugs + FindSecBugs (bytecode-level)

Configured in `pom.xml` with the `spotbugs-security-include.xml` filter so only SECURITY-category bugs fail the build:

```bash
./mvnw -B compile spotbugs:check
```

For a readable report of findings:

```bash
./mvnw -B compile spotbugs:spotbugs && ./mvnw spotbugs:gui   # or read target/spotbugsXml.xml
```

Suppressions require `@SuppressFBWarnings(value = "RULE_ID", justification = "...")` with a real justification — never a blanket exclude in the filter file.

## 4. Dependencies and misconfig (Trivy)

Vulnerable dependencies (reads `pom.xml`), leaked secrets, and IaC/Dockerfile misconfigurations in one pass:

```bash
trivy fs --scanners vuln,secret,misconfig --severity HIGH,CRITICAL --exit-code 1 .
```

- Vulnerable dependency: upgrade the Quarkus platform BOM or the individual artifact to the fixed version, then rerun.
- No fix available: add the CVE to `.trivyignore` with a comment containing the CVE id, the reason, and a revisit condition.
- Dockerfile findings (root user, unpinned images): fix the Dockerfile; do not ignore.

## Triage order

1. Secrets (rotate immediately if real)
2. Injection-class SAST findings (SQLi, command injection, path traversal, SSRF, deserialization)
3. Vulnerable dependencies with a fix available
4. AuthZ/crypto/config findings
5. Everything else by severity

Fix findings introduced by the current change before shipping. Pre-existing findings unrelated to the change: report them, fix if trivial, otherwise list them explicitly in the summary rather than silently ignoring.

## Output

Report per scanner: status (clean / findings / skipped+why), counts by severity, the fixes applied, and any suppressions added with their justification. End with an overall verdict: ship, ship-with-notes, or blocked.
