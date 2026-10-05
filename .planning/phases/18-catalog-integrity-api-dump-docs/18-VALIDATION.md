---
phase: "18"
slug: "catalog-integrity-api-dump-docs"
status: draft
nyquist_compliant: false
wave_0_complete: false
created: "2026-10-05"
---

# Phase 18 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit4 + Robolectric + Compose UI test via Gradle `testDebugUnitTest` |
| **Config file** | `build.gradle.kts`; detekt `config/detekt-compose.yml`, `config/detekt-baseline.xml` |
| **Quick run command** | `./gradlew testDebugUnitTest --tests "*ComponentRegistryDriftGuardTest*" --tests "*DomainVocabularyDriftGuardTest*" --tests "*GeneratedSymbolDriftGuardTest*" --tests "*ComponentRegistryTierTest*"` |
| **Full suite command** | `./gradlew cleanTestDebugUnitTest testDebugUnitTest detekt apiCheck --no-build-cache` |
| **Estimated runtime** | ~75 seconds (tests) |

---

## Sampling Rate

- **After every task commit:** docs-only tasks run the grep checks for their own file; otherwise the quick run command
- **After every plan wave:** Run the full suite command
- **Before `/gsd-verify-work`:** Full suite + the whole evidence battery green on the final HEAD
- **Max feedback latency:** ~75 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 18-01-xx | 01 | 1 | DOC-02 | — | N/A | grep | API.md names all 20 new params/fields + `## 10. Voice Command` + `trailingContent =` caveat; INTEGRATION.md has a localization note | ❌ W0 (the doc edit itself) | ⬜ pending |
| 18-02-xx | 02 | 2 | CAT-02 | — | N/A | unit (source-scan) | `./gradlew cleanTestDebugUnitTest testDebugUnitTest --no-build-cache` (unscoped) | ✅ | ⬜ pending |
| 18-02-xx | 02 | 2 | API-02 | — | N/A | gate | `API_FILE=api.txt bash tools/verify-api-additive.sh v2.4.1` exit 3 with exactly the 10-line allowlist; swap-baseline `apiCheck` vs v2.4.1 and v2.4.0; `apiDump` idempotent; `bash tools/test/run-all.sh` | ✅ | ⬜ pending |
| 18-02-xx | 02 | 2 | INV-02 | — | N/A | script | import filter prints nothing; `git diff --exit-code v2.4.1..HEAD -- build.gradle.kts gradle/ settings.gradle.kts jitpack.yml`; `./gradlew detekt`; baseline unchanged | ✅ | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] `18-SHIP-GATE-EVIDENCE.md` — evidence file (headers `## CAT-02 Evidence`, `## API-02 Evidence`, `## INV-02 Evidence`, `## Restore Confirmation`), modelled on the v2.4 Phase 13 analog
- No new test framework, fixtures or automated tests (D-03)

---

## Manual-Only Verifications

All phase behaviors have automated verification.

---

## Validation Sign-Off

> **Plan-time state is a DRAFT.** Leave frontmatter `status: draft` and `nyquist_compliant: false`.
> These are finalized ONLY post-execution by the Nyquist finalizer (the `verify:post` →
> `validate-phase` hook). Never set `nyquist_compliant: true` at plan time.

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 90s
- [ ] _(finalizer-only, post-execution)_ `nyquist_compliant` — leave `false` at plan time

**Approval:** pending
