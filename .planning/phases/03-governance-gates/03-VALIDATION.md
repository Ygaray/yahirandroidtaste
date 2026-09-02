---
phase: 03
slug: governance-gates
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
# audit-milestone §5.5 distinguishes NOT-VALIDATED (draft) from PARTIAL (validated + nyquist_compliant: false) (#2117)
status: draft
nyquist_compliant: false
wave_0_complete: false
created: 2026-09-01
---

# Phase 03 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit4 (plain JVM, no Robolectric) for GOV-02; bash fixture-tests (existing pattern in `tools/test/*.sh`) for GOV-03 |
| **Config file** | none beyond the existing Gradle module config (`build.gradle.kts`, root) |
| **Quick run command** | `./gradlew testDebugUnitTest --tests "*DomainVocabularyDriftGuardTest*"` (GOV-02); `bash tools/test/test-precommit-hook.sh` (GOV-03) |
| **Full suite command** | `./gradlew testDebugUnitTest` (GOV-02); `bash tools/test/run-all.sh` (GOV-03) |
| **Estimated runtime** | ~30-90 seconds per quick command; a few minutes for full suites |

---

## Sampling Rate

- **After every task commit:** `./gradlew testDebugUnitTest --tests "*DomainVocabularyDriftGuardTest*"` (once the file exists) and `bash tools/test/test-precommit-hook.sh` for any `tools/` edit.
- **After every plan wave:** `./gradlew testDebugUnitTest` (full) + `bash tools/test/run-all.sh` (full).
- **Before `/gsd-verify-work`:** both full suites green; additionally, manually reproduce the ORIGINAL bug's exact commit sequence (a `.planning/`-only commit after a `src/main` rewrite in history) against the fixed hook to close the loop on GOV-03's own stated success criterion.
- **Max feedback latency:** ~90 seconds (Gradle unit test suite is the slowest leg).

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 03-01-01 | 01 | 0 | GOV-02 | — | N/A | unit | `./gradlew testDebugUnitTest --tests "*DomainVocabularyDriftGuardTest*"` | ❌ W0 | ⬜ pending |
| 03-01-02 | 01 | 1 | GOV-03 | — | N/A | integration (bash) | `bash tools/test/test-precommit-hook.sh` | ✅ (extend) | ⬜ pending |
| 03-01-03 | 01 | 1 | GOV-01 | — | N/A | doc-review + delegated to GOV-02's test | `./gradlew testDebugUnitTest --tests "*DomainVocabularyDriftGuardTest*"` | ❌ W0 | ⬜ pending |

*Task IDs above are illustrative pending the planner's actual wave/task breakdown — the planner's PLAN.md is authoritative for exact task numbering.*

---

## Wave 0 Requirements

- [ ] `src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt` — new file, covers GOV-01 (strict-half enforcement) + GOV-02.
- [ ] `tools/test/test-precommit-hook.sh` — extend with the "rewrite commit, then unrelated commit" regression case that reproduces and proves the fix for GOV-03's exact bug.
- [ ] Framework install: none — JUnit4 and bash are already wired into `./gradlew testDebugUnitTest` and `tools/test/run-all.sh` respectively.

---

## Manual-Only Verifications

*All phase behaviors have automated verification.*

---

## Validation Sign-Off

> **Plan-time state is a DRAFT.** Leave frontmatter `status: draft` and `nyquist_compliant: false`.
> These are finalized ONLY post-execution by the Nyquist finalizer (the `verify:post` →
> `validate-phase` hook, invoked by execute-phase `finalize_nyquist_validation` after Gate-1). Never
> set `nyquist_compliant: true` — or otherwise "sign off" compliance — at plan time, and do not let
> the plan-checker do so (INC-2026-07-27-01: a premature plan-time flip is what caused inconsistent
> COMPLIANT/PARTIAL milestone-audit states).

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 90s
- [ ] _(finalizer-only, post-execution)_ `nyquist_compliant` — leave `false` at plan time; the
      finalizer sets `true` iff its gap analysis finds zero gaps

**Approval:** pending — finalizer-owned, not set at plan time
