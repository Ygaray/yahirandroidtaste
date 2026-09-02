---
phase: 03
slug: governance-gates
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
# audit-milestone §5.5 distinguishes NOT-VALIDATED (draft) from PARTIAL (validated + nyquist_compliant: false) (#2117)
status: validated
nyquist_compliant: true
wave_0_complete: true
created: 2026-09-01
---

# Phase 03 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit4 (plain JVM, no Robolectric) for GOV-01/GOV-02; bash fixture-tests (existing pattern in `tools/test/*.sh`) for GOV-03 |
| **Config file** | none beyond the existing Gradle module config (`build.gradle.kts`, root) |
| **Quick run command** | `./gradlew testDebugUnitTest --tests "*DomainVocabularyDriftGuardTest*"` (GOV-01/GOV-02); `bash tools/test/test-precommit-hook.sh` (GOV-03) |
| **Full suite command** | `./gradlew testDebugUnitTest` (GOV-01/GOV-02); `bash tools/test/run-all.sh` (GOV-03) |
| **Estimated runtime** | ~30-90 seconds per quick command; a few minutes for full suites |

---

## Sampling Rate

- **After every task commit:** `./gradlew testDebugUnitTest --tests "*DomainVocabularyDriftGuardTest*"` and `bash tools/test/test-precommit-hook.sh` for any `tools/` edit.
- **After every plan wave:** `./gradlew testDebugUnitTest` (full) + `bash tools/test/run-all.sh` (full).
- **Before `/gsd-verify-work`:** both full suites green; additionally, the ORIGINAL bug's exact commit sequence (a `.planning/`-only commit after a `src/main` rewrite in history) was manually reproduced against the fixed hook, closing the loop on GOV-03's own stated success criterion.
- **Max feedback latency:** ~90 seconds (Gradle unit test suite is the slowest leg). Confirmed in execution: `./gradlew testDebugUnitTest detekt` ran in under a minute on a warm daemon.

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|--------------------|-------------|--------|
| 03-01-01 | 01 | 1 | GOV-03 | T-03-01 (accept) | N/A | unit (bash) | `bash tools/test/test-verify-additive-diff.sh` | ✅ | ✅ covered |
| 03-01-02 | 01 | 1 | GOV-03 | T-03-01 (accept) | N/A | integration (bash) | `bash tools/test/test-precommit-hook.sh` | ✅ | ✅ covered |
| 03-01-03 | 01 | 1 | GOV-03 | T-03-01 (accept) | N/A | unit (bash) | `bash tools/test/test-verify-api-additive.sh` | ✅ | ✅ covered (residual-risk case (e); tracked in STATE.md for Phase 5) |
| 03-02-01 | 02 | 1 | GOV-02 | T-03-02 (accept) | N/A | unit (JUnit) | `./gradlew testDebugUnitTest --tests "*DomainVocabularyDriftGuardTest*"` | ✅ | ✅ covered |
| 03-02-02 | 02 | 1 | GOV-01 | T-03-02 (accept) | N/A | doc-review + delegated to GOV-02's test | `./gradlew testDebugUnitTest --tests "*DomainVocabularyDriftGuardTest*"` | ✅ | ✅ covered |
| 03-02-03 | 02 | 1 | GOV-01, GOV-02 | T-03-02 (accept) | N/A | doc | manual read (`docs/DESIGN-INTENT.md` `## The Tier-Aware Contribution Litmus` + `## Enforcement`) | ✅ | ✅ covered |

All task IDs above are actual, cross-referenced against 03-01-PLAN.md/03-01-SUMMARY.md and
03-02-PLAN.md/03-02-SUMMARY.md `coverage:` blocks (D1-D3 for 03-01, D1-D2 for 03-02), and against
03-VERIFICATION.md's Required Artifacts / Behavioral Spot-Checks tables. All corresponding test
files exist on disk and were confirmed GREEN as of commit `853c3de`:

- `bash tools/test/run-all.sh` → `PASS=4/7/5/5 FAIL=0` (all four shell suites)
- `./gradlew testDebugUnitTest detekt` → BUILD SUCCESSFUL (full suite, incl. `DomainVocabularyDriftGuardTest`), zero detekt findings
- Post-code-review-fix re-run (after 5 fix commits) confirmed both green independently

---

## Wave 0 Requirements

- [x] `src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt` — created, covers GOV-01 (strict-half enforcement) + GOV-02. Manual red→green demo performed (`VoiceCard` removed/restored).
- [x] `tools/test/test-precommit-hook.sh` — extended with the "post-lane-2 unrelated commit unblocked (GOV-03 fix)" regression case, reproducing and proving the fix for GOV-03's exact original bug sequence.
- [x] Framework install: none needed — JUnit4 and bash were already wired into `./gradlew testDebugUnitTest` and `tools/test/run-all.sh` respectively.

---

## Manual-Only Verifications

*All phase behaviors have automated verification. `docs/DESIGN-INTENT.md`'s prose sections (GOV-01)
are documentation, verified by direct read (03-VERIFICATION.md truths #1-2) rather than an
executable test — this is the expected shape for a documentation deliverable and does not
constitute a coverage gap.*

---

## Validation Audit 2026-09-01

| Metric | Count |
|--------|-------|
| Gaps found | 0 |
| Resolved | 0 |
| Escalated | 0 |

All six tasks across both plans have automated command coverage that runs green. No `MISSING` or
`PARTIAL` requirements remain. `nyquist_compliant: true` set by the post-execution finalizer per
INC-2026-07-27-01 (never set at plan time).

---

## Validation Sign-Off

- [x] All tasks have `<automated>` verify or Wave 0 dependencies
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all MISSING references
- [x] No watch-mode flags
- [x] Feedback latency < 90s
- [x] `nyquist_compliant: true` — finalizer's gap analysis found zero gaps

**Approval:** validated 2026-09-01 (gsd-validate-phase, auto mode)
