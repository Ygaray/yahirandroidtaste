---
phase: 25-feedback-target-model-stack-state
plan: 01
subsystem: feedback
tags: [compose, api-additive, metalava, call-site-canary]
requires: []
provides:
  - "FeedbackTarget(key, openLabel) opaque, lambda-free, value-compared type"
  - "FeedbackEvent.Notify and WithUndo with appended target and supportingText params"
  - "V271CallSiteCanaryTest pinning every v2.7.1 call shape"
affects: [25-02, 25-03, 25-04, 25-05]
tech-stack:
  added: []
  patterns: ["append defaulted params after every existing param", "apiDump in the same commit as the signature change"]
key-files:
  created:
    - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackTarget.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackTargetTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/V271CallSiteCanaryTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackEvent.kt
    - api.txt
key-decisions:
  - "target then supportingText order, both last, on Notify and WithUndo only (D-01); DismissScreen untouched"
  - "No init/require on FeedbackTarget (D-02): blank openLabel is a KDoc rule plus a render-time fallback"
requirements-completed: [FTGT-01]
status: complete
commits: 2
plan_head_before: 06628db7303076bd905e31b414dc2abd4c1327e9
actuals:
  tokens: 9000
  tasks: 2
  commits: 2
duration: 20min
completed: 2026-10-17
---

# Phase 25 Plan 01: FeedbackTarget and widened events Summary

Opaque `FeedbackTarget(key, openLabel)` plus `target` / `supportingText` appended (null defaults) to `FeedbackEvent.Notify` and `WithUndo`, with a V271 compile canary proving every v2.7.1 call shape still builds and keeps its defaults.

## What was built

- `FeedbackTarget.kt`: `@Immutable data class FeedbackTarget(val key: String, val openLabel: String)`; only import is `androidx.compose.runtime.Immutable`; KDoc states the opaque-key, non-blank-label (KDoc rule only), no-lambda/no-Context contract.
- `FeedbackEvent.kt`: `Notify` and `WithUndo` gained `val target: FeedbackTarget? = null, val supportingText: String? = null` as the last two params, with `@param` KDoc. `DismissScreen` unchanged.
- `api.txt` regenerated via `apiDump` in the same commit. Diff versus v2.7.1 removes exactly four lines (Notify ctor/copy, WithUndo ctor/copy); `tools/verify-api-source-additive.sh v2.7.1` prints `API SOURCE-ADDITIVE OK` (required_param_changes=0, signature_changes=0).
- `FeedbackTargetTest` (10 tests): value equality and hashCode, reflective two-String-field check with a vacuity guard, blank-label no-throw, event defaults, read-back and `copy`, destructuring, DismissScreen single field.
- `V271CallSiteCanaryTest` (8 tests): Notify/WithUndo/DismissScreen positional, named and out-of-order shapes, copy and destructuring (including the four-component WithUndo), exhaustive three-branch `when`, all store `append` shapes (including the CT function-value form and grouped forms), entry `copy`/`withStatus`/six-component destructuring, and the `emitTrackedWithUndo` compile reference.

## Commits

- `266f94f` feat(25-01): add FeedbackTarget and widen Notify/WithUndo
- `56b0d91` test(25-01): add V271 call-site canary

## Verification

- `./gradlew testDebugUnitTest --tests '*FeedbackTargetTest' --tests '*V271CallSiteCanaryTest' detekt apiCheck --no-daemon`: BUILD SUCCESSFUL (detekt zero-baseline, apiCheck green).
- Test XML: FeedbackTargetTest 10 testcases, V271CallSiteCanaryTest 8 testcases, 0 failures each.

## Deviations from Plan

None. The plan was executed as written. Both commits were lane 1 (additive) in the pre-commit hook.

## Notes

- The canary's provenance comments cite consumer files by role (FeedbackController, SheetFeedbackHost, CT function-value append) with no line numbers, since the research did not record any.
- Executed in a worktree named `agent-...`, so directory-name-based drift guards (which walk up to a `yahirandroidtaste` directory) would pass vacuously here; the full guard suite is not part of this plan and should be re-run in the main checkout at phase end.
- No tag, push or consumer repin was done. STATE.md and ROADMAP.md untouched (orchestrator-owned).

## Self-Check: PASSED

- FOUND: FeedbackTarget.kt, FeedbackTargetTest.kt, V271CallSiteCanaryTest.kt, api.txt (contains FeedbackTarget)
- FOUND commits: 266f94f, 56b0d91
