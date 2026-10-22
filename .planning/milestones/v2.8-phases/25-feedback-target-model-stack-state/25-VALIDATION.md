---
phase: "25"
slug: feedback-target-model-stack-state
status: validated
nyquist_compliant: true
wave_0_complete: true
created: "2026-10-17"
---

# Phase 25 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 4.13.2 + kotlinx-coroutines-test 1.11.0 (state, store, tracked path, canary) + Robolectric 4.16.1 / `createComposeRule` (host) |
| **Config file** | none beyond `build.gradle.kts`; no new infra |
| **Quick run command** | `./gradlew testDebugUnitTest --tests '*FeedbackStackStateTest' --tests '*FeedbackTargetTest' --tests '*FeedbackStackTrackedUndoTest' --no-daemon` |
| **Guard run command** | `./gradlew testDebugUnitTest --tests '*CatalogDocsDriftGuardTest' --tests '*DomainVocabularyDriftGuardTest' --tests '*ComponentRegistryDriftGuardTest' --tests '*ComponentStatesMatrixTest' --tests '*ComponentPlaygroundIntegrityTest' --tests '*LibraryInvariantsGuardTest' --no-daemon` |
| **Full suite command** | `./gradlew apiDump testDebugUnitTest detekt apiCheck --no-daemon` then `bash tools/verify-api-source-additive.sh v2.7.1` then `bash tools/test/run-all.sh` (never in parallel with Gradle) |
| **Estimated runtime** | ~30 s per filtered run; full suite several minutes |

---

## Sampling Rate

- **After every task commit:** Run the quick run command for the touched area
- **After every plan wave:** Run the guard run command plus the quick run command
- **Before `/gsd-verify-work`:** Full suite must be green (fresh, `--rerun-tasks` for gate evidence)
- **Max feedback latency:** ~60 seconds

---

## Per-Task Verification Map

| Requirement | Behavior | Test Type | Automated Command | File Exists | Status |
|-------------|----------|-----------|-------------------|-------------|--------|
| FTGT-01 | `FeedbackTarget` value-equal, no lambda field | unit | `--tests '*FeedbackTargetTest'` | ✅ | ✅ green |
| FTGT-01 | v2.7.1 call shapes compile unchanged | unit | `--tests '*V271CallSiteCanaryTest'` | ✅ | ✅ green |
| FTGT-02 | `append(target=)` stores target; `openableTarget` null after Undone/Failed | unit | `--tests '*UndoHistoryStoreTest'` | ✅ extended | ✅ green |
| FTGT-02, FSTK-07 | tracked undo path (M3 and stack) | unit | `--tests '*FeedbackStackTrackedUndoTest'` | ✅ | ✅ green |
| FSTK-01 | exactly-one result, timers, cap, cancel, dismissAll | unit | `--tests '*FeedbackStackStateTest'` | ✅ | ✅ green |
| FSTK-01, FSTK-07 | host: body tap, Undo, label, remember survives recomposition | Robolectric | `--tests '*FeedbackStackHostTest'` | ✅ | ✅ green |
| FSTK-01 | registry / vocabulary / docs-count guards | guard | guard run command | ✅ edited | ✅ green |

*Status: ✅ green · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [x] `FeedbackTargetTest.kt` — FTGT-01
- [x] `V271CallSiteCanaryTest.kt` — FTGT-01
- [x] `FeedbackStackStateTest.kt` — FSTK-01
- [x] `FeedbackStackTrackedUndoTest.kt` — FTGT-02, FSTK-07
- [x] `FeedbackStackHostTest.kt` — FSTK-01/07
- [ ] extend `UndoHistoryStoreTest.kt` — FTGT-02

---

## Manual-Only Verifications

All phase behaviors have automated verification (no layout/motion contract until Phase 26).

---

## Validation Sign-Off

> Finalized post-execution by the Nyquist finalizer (2026-10-17): zero automatable gaps.

- [x] All tasks have `<automated>` verify or Wave 0 dependencies
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all MISSING references
- [x] No watch-mode flags
- [x] Feedback latency < 60s
- [x] `nyquist_compliant: true` set by the finalizer (zero automatable gaps)

**Approval:** validated 2026-10-17

---

## Validation Audit 2026-10-17

| Metric | Count |
|--------|-------|
| Gaps found | 0 |
| Resolved | 0 |
| Escalated | 0 |

Every requirement row (FTGT-01, FTGT-02, FSTK-01, FSTK-07) maps to an existing test class that ran green in the fresh post-fix run (1452 tests, 0 failures, 22 skipped outside this phase) and again in the Gate-1 rerun. Guard classes (CatalogDocsDrift, DomainVocabularyDrift, ComponentRegistryDrift, LibraryInvariants) are green in the main checkout. Auto-mode finalizer; no tests generated.
