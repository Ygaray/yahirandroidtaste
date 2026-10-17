---
phase: "26"
slug: receipt-card-stacked-snackbar-deck
status: validated
nyquist_compliant: true
wave_0_complete: true
created: "2026-10-17"
---

# Phase 26 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 4.13.2 + kotlinx-coroutines-test 1.11.0 (state, virtual clock) + Robolectric 4.16.1 / `createComposeRule` ui-test 1.11.0 (host) |
| **Config file** | none beyond `build.gradle.kts`; no new infra |
| **Quick run command** | `./gradlew testDebugUnitTest --tests '*FeedbackStack*' --tests '*FeedbackDeck*' --no-daemon` |
| **Guard run command** | `./gradlew testDebugUnitTest --tests '*CatalogDocsDriftGuardTest' --tests '*DomainVocabularyDriftGuardTest' --tests '*ComponentRegistryDriftGuardTest' --tests '*ComponentStatesMatrixTest' --tests '*ComponentPlaygroundIntegrityTest' --tests '*LibraryInvariantsGuardTest' --no-daemon` |
| **Full suite command** | `./gradlew apiDump testDebugUnitTest detekt apiCheck --rerun-tasks --no-daemon`, then `git diff --exit-code api.txt`, `bash tools/verify-api-source-additive.sh v2.7.1`, `bash tools/test/run-all.sh` (never in parallel with Gradle) |
| **Estimated runtime** | ~30-60 s per filtered run; full suite several minutes |

---

## Sampling Rate

- **After every task commit:** Run the quick run command for the touched area
- **After every plan wave:** Run the guard run command plus the quick run command
- **Before `/gsd-verify-work`:** Full suite must be green (fresh, `--rerun-tasks`)
- **Max feedback latency:** ~60 seconds

---

## Per-Task Verification Map

| Requirement | Behavior | Test Type | Automated Command | File Exists | Status |
|-------------|----------|-----------|-------------------|-------------|--------|
| FSTK-02 | 4th show dismisses oldest; tracked WithUndo entry stays in store | unit | `--tests '*FeedbackStackStateTest' --tests '*FeedbackStackTrackedUndoTest'` | ✅ | ✅ green |
| FSTK-02 | Collapsed transforms 14dp / 0.94 / 0.88, back cards clamped to front height | unit (pure) | `--tests '*FeedbackDeckMetricsTest'` | ✅ | ✅ green |
| FSTK-02 | DE/ES message + second line at 360dp, fontScale 2.0 wrap inside card | Robolectric | `--tests '*FeedbackStackHostLongLabelTest'` | ✅ | ✅ green |
| FSTK-03 | Body tap opens once + `Opened`; untargeted inert; chevron only with target | Robolectric | `--tests '*FeedbackStackHostTest'` | ✅ | ✅ green |
| FSTK-03 | Long hold then release does not open | Robolectric | `--tests '*FeedbackStackHostDeckTest'` | ✅ | ✅ green |
| FSTK-04 | Peek tap fans, never opens; every card's body and Undo tappable when fanned; idle collapse; 60% cap scrolls | Robolectric | `--tests '*FeedbackStackHostDeckTest'` | ✅ | ✅ green |
| FSTK-05 | Swipe on one card dismisses only that card; collapsed only front swipeable | Robolectric | `--tests '*FeedbackStackHostDeckTest'` | ✅ | ✅ green |
| FSTK-06 | Timeouts 4 s / 10 s through the accessibility scaler (existing) | unit | `--tests '*FeedbackStackStateTimeoutTest'` | ✅ | ✅ green |
| FSTK-06 | Pause keeps remainder, 800 ms floor, fanned pauses all, resume on release/collapse | unit, virtual clock | `--tests '*FeedbackStackPauseTest'` | ✅ | ✅ green |
| A11Y-01 | Merged Button node, onClickLabel, Polite live region, Undo child + custom action, Dismiss action, newest-first order | Robolectric | `--tests '*FeedbackStackHostSemanticsTest'` | ✅ | ✅ green |
| Invariant | No `requireOffset`, no SwipeableActionRow import, no `clickable` on the card node | source contract | `--tests '*FeedbackStackSourceContractTest'` | ✅ | ✅ green |
| API | No public API change | gate | `./gradlew apiDump && git diff --exit-code api.txt` | n/a | ✅ green |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [x] `FeedbackStackPauseTest.kt` — FSTK-06 (needs the `internal var timeSource` seam first)
- [x] `FeedbackDeckMetricsTest.kt` — FSTK-02 geometry
- [x] `FeedbackStackHostDeckTest.kt` — FSTK-03/04/05/06 gestures
- [x] `FeedbackStackHostSemanticsTest.kt` — A11Y-01
- [x] `FeedbackStackHostLongLabelTest.kt` — FSTK-02 long strings
- [x] `FeedbackStackSourceContractTest.kt` — crash-rule guard
- [x] Throwaway probes for the research assumptions (mainClock vs LaunchedEffect delay, anchoredDraggable threshold, seeded anchors offset)

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Deck look, peek, fan feel, swipe feel on a real device | FSTK-02/04/05 | Visual and gesture feel | Gate-1 agentic self-UAT on the TESTER |
| TalkBack announcement of the cards | A11Y-01 | Screen reader behavior | Confirmed in Phase 28's sweep (A11Y-02) |

---

## Validation Sign-Off

> **Plan-time state is a DRAFT.** Leave frontmatter `status: draft` and `nyquist_compliant: false`.
> These are finalized ONLY post-execution by the Nyquist finalizer (the `verify:post` →
> `validate-phase` hook, invoked by execute-phase `finalize_nyquist_validation` after Gate-1). Never
> set `nyquist_compliant: true` — or otherwise "sign off" compliance — at plan time, and do not let
> the plan-checker do so (INC-2026-07-27-01).

- [x] All tasks have `<automated>` verify or Wave 0 dependencies
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all MISSING references
- [x] No watch-mode flags
- [x] Feedback latency < 60s
- [x] _(finalizer-only, post-execution)_ `nyquist_compliant: true` set by the Nyquist finalizer

**Approval:** validated 2026-10-17

---

## Validation Audit 2026-10-17

| Metric | Count |
|--------|-------|
| Gaps found | 0 |
| Resolved | 0 |
| Escalated | 0 |

Fresh full gate on main (`apiDump testDebugUnitTest detekt apiCheck --rerun-tasks --no-build-cache --no-daemon`): 1613 tests, 22 pre-existing skips, 0 failures, detekt and apiCheck green, `api.txt` unchanged, `verify-api-source-additive.sh v2.7.1` OK, `tools/test/run-all.sh` PASS=52 FAIL=0. Every requirement (FSTK-02..06, A11Y-01) maps to a named green automated test; the gap-closure plan 26-06 added peek-tap-over-Undo regression tests. Manual-only rows (device feel, TalkBack) are covered by Gate-1 (all_pass, uat_run uatr-20261017T140846Z-f3f20544) and Phase 28.
