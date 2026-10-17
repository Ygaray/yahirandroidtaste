---
phase: 25
fixed_at: 2026-10-17T00:00:00Z
review_path: .planning/phases/25-feedback-target-model-stack-state/25-REVIEW.md
iteration: 1
findings_in_scope: 8
fixed: 8
skipped: 0
status: all_fixed
---

# Phase 25: Code Review Fix Report

**Fixed at:** 2026-10-17
**Source review:** .planning/phases/25-feedback-target-model-stack-state/25-REVIEW.md
**Iteration:** 1

**Summary:**
- Findings in scope: 8 (fix_scope: all)
- Fixed: 8
- Skipped: 0

**Verification:** run in the main checkout (no worktree, per the task notes), so the numbers are
reproducible from the current tree. `./gradlew testDebugUnitTest detekt apiCheck --no-daemon` ended
BUILD SUCCESSFUL after all fixes (detekt at zero baseline, `api.txt` unchanged, `apiCheck` green).
The three new regression tests for WR-01..WR-03 were also confirmed red against the pre-fix sources
(3 failures out of 73 in `*feedback.FeedbackStack*`) and green after the fix.

**Commit lane note:** the pre-commit classifier reports LANE 2 for every fix because it flags
rewritten lines against the v2.7.1 baseline, even though all rewritten lines are unreleased Phase 25
code. Each commit declares `HUB_LANE_OVERRIDE=2` with that reason in its message, per the project
convention in `.planning/STATE.md`. No public signature changed.

## Fixed Issues

### WR-01: A tapped Undo is silently lost if the caller is cancelled before `show` resumes

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoTracking.kt`, `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackState.kt`, `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackTrackedUndoTest.kt`
**Commit:** 13a840d (shared with WR-02 and WR-03, see note below)
**Status:** fixed: requires human verification (concurrency logic)
**Applied fix:** Each `FeedbackStackItem` now records the winning result in an internal `outcome` field (set under the state lock before the deferred completes). `showInternal` takes an optional `onEnqueued` hook; `emitTrackedWithUndo` catches `CancellationException`, and if the card's `outcome` is `Undone` it runs `store.attemptUndo` under `NonCancellable` before rethrowing. KDoc updated. New test `cancelAfterUndoTapBeforeCallerResumes_undoStillRunsOnce_entryEndsUndone`.

### WR-02: Card is leaked if the timeout scaler throws

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackState.kt`, `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackStateTest.kt`
**Commit:** 13a840d
**Status:** fixed: requires human verification
**Applied fix:** The timer and watcher setup now live inside the `try`, so the `finally` always removes the card. The scaler call goes through `scaledMillis`, which falls back to the unscaled timer when the platform call throws (a slight improvement on the suggested `runCatching`, since the card still gets its normal timeout). New test `throwingTimeoutScaler_stillShowsCard_withUnscaledTimer`.

### WR-03: `resolve` completes the deferred before removing the item (inline-resume over-eviction)

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackState.kt`, `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackStateTest.kt`
**Commit:** 13a840d
**Status:** fixed: requires human verification
**Applied fix:** `resolve` picks the single winner and removes the item under the lock (via the new `outcome` claim), then completes the deferred after releasing the lock. `enqueue` collects evicted items under the lock and completes them afterwards. New test `inlineResumingCaller_reshowingFromContinuation_doesNotEvictAnOlderCard` uses `Dispatchers.Unconfined`.

Note: WR-01, WR-02 and WR-03 edit the same functions and share the `outcome` field, so they are one
commit rather than three. They were not separable without leaving an intermediate commit that does
not compile or pass.

### WR-04: Undo button is unreachable by TalkBack on cards that carry a target

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHost.kt`, `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHostTest.kt`
**Commit:** 4ea597d
**Applied fix:** The card root now exposes an Undo `CustomAccessibilityAction` (label `undoLabel`) ahead of Dismiss whenever the card is `WithUndo` and not spent. The KDoc no longer lists merged-button semantics as missing. New merged-tree tests assert the action exists and resolves `Undone` without opening the target, and that a `Notify` card has no Undo action. Existing click-behavior tests are unchanged and pass.

### WR-05: Stale count prose in the `ComponentRegistry` KDoc

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistry.kt`
**Commit:** 15d862d
**Applied fix:** Counts updated to 67 registered, 6 intentionally unregistered, 73 total (Phase 25, 2026-10-17). The paragraph no longer claims a test enforces it, since `CatalogDocsDriftGuardTest` only compares `API.md` and `README.md`.

### IN-01: `FeedbackEvent` header KDoc is stale

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackEvent.kt`, `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackState.kt`
**Commit:** c69b2c8
**Applied fix:** Replaced the "pure addition, zero consumers" paragraph with the current rendering paths. Added to the `DismissScreen` KDoc and the `FeedbackStackState` KDoc that the stack only displays the message and the caller owns dismissing the screen.

### IN-02: `FeedbackTarget.key` is echoed by generated `toString`

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackTarget.kt`, `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackTargetTest.kt`
**Commit:** 0a6fdec
**Applied fix:** Added an explicit `toString()` printing only `openLabel`; KDoc updated. `api.txt` is unchanged and `apiCheck` is green. New test asserts neither the target nor an event holding it prints the key.

### IN-03: Test gaps in the highest-risk area

**Files modified:** `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackTrackedUndoTest.kt`, `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackStateTest.kt`
**Commit:** 13a840d
**Applied fix:** The three missing tests (cancel-after-resolve, inline-resuming dispatcher, throwing scaler) were added alongside the WR-01..WR-03 fixes, as the review suggested.

## Skipped Issues

None.

---

_Fixed: 2026-10-17_
_Fixer: Claude (gsd-code-fixer)_
_Iteration: 1_
