---
phase: 25-feedback-target-model-stack-state
reviewed: 2026-10-17T00:00:00Z
depth: standard
files_reviewed: 22
files_reviewed_list:
  - API.md
  - README.md
  - api.txt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistry.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/FeedbackFamilyScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackEvent.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackResult.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHost.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackState.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackTarget.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoHistoryEntry.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoHistoryStore.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoTracking.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/V271CallSiteCanaryTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHostTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackStateTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackStateTimeoutTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackTrackedUndoTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackTargetTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/RememberFeedbackStackStateTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/UndoHistoryStoreTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/UndoTrackingMaterialPathTest.kt
findings:
  critical: 0
  warning: 5
  info: 3
  total: 8
status: issues_found
---

# Phase 25: Code Review Report

**Reviewed:** 2026-10-17
**Depth:** standard
**Files Reviewed:** 22
**Status:** issues_found

## Summary

The additive-API invariants hold: `FeedbackEvent` params are appended with defaults, the new
`UndoHistoryStore.append` overload has a required `target`, so v2.7.1 call shapes keep binding to the
older overloads, and `UndoHistoryEntry.target` is appended last. There are no consumer imports, no
Hilt host, and `rememberFeedbackStackState` is correctly allowlisted. The registry, API.md and README
counts (67/6/73) are consistent with each other.

The highest-risk area (concurrency and cancellation in `FeedbackStackState` and the tracked-undo
path) is mostly sound. Single-winner resolution via `CompletableDeferred.complete` under a lock
holds, and eviction and cancel-cleanup are correct. I found four correctness and robustness gaps
there, none of them a data-loss or security blocker. The largest is a window where a user's Undo tap
is silently dropped (WR-01). One TalkBack accessibility gap on cards that carry a target (WR-04) was
shipped knowingly but is not tracked anywhere I could find.

## Warnings

### WR-01: A tapped Undo is silently lost if the caller is cancelled before `show` resumes

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackState.kt:99-121` and `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoTracking.kt:84-93`
**Issue:** `emitTrackedWithUndo` only protects the store undo (`withContext(NonCancellable)`) once
`showInternal` has returned `Undone`. The window before that is not protected. Sequence:

1. The Undo tap runs `resolve(item, Undone)`. The deferred completes and card is removed.
2. The awaiting continuation is only queued on the dispatcher, not yet resumed.
3. The caller's job is cancelled, for example the composable hosting the `LaunchedEffect` or
   `rememberCoroutineScope` leaves composition in the same frame (navigation after the tap).
4. At dispatch, the prompt-cancellation guarantee resumes `await()` with `CancellationException`
   because the scope job is no longer active. `coroutineScope` also rethrows on cancelled completion
   even if the block had a value.

The user tapped Undo and the card disappeared as if it had been honored, but `attemptUndo` never
runs and the entry stays `Available`. The KDoc promises the opposite for "cancelled while the undo is
running". The existing test `cancelDuringStoreUndo_*` only cancels after `runCurrent()` has already
resumed the caller, so it does not cover this window.
**Fix:** Decide the Undo outcome from the deferred, not from the resumed `await()`. In
`emitTrackedWithUndo`, catch the cancellation and honor a completed `Undone`:
```kotlin
val result = try {
    showInternal(event, entryId) { /* watcher */ }
} catch (e: CancellationException) {
    // The card may already have resolved as Undone before the cancel won the race.
    if (undoneByTap(entryId)) withContext(NonCancellable) { store.attemptUndo(entryId) }
    throw e
}
```
`showInternal` would need to expose the item's resolved result, for example by passing a holder in or
returning the item. Add a test: `resolve(Undone)` then `job.cancel()` then `runCurrent()`, and assert
the lambda ran once.

### WR-02: Card is leaked (never resolved, never removed) if the timeout scaler throws

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackState.kt:103-121`
**Issue:** `enqueue(event, entryId)` publishes the card, then `timeoutScaler?.invoke(...)` runs
before the `try`. `timeoutScaler` wraps `AccessibilityManager.calculateRecommendedTimeoutMillis`, a
platform call that can throw. If it does, the exception propagates out of `coroutineScope`. The
`finally` that calls `resolve(item, Dismissed)` is never entered, so the card stays in `items`
forever. It is a ghost the user can still see, and it permanently consumes one of `maxVisible` slots.
The caller sees an exception for a card that is still on screen. Anything else added between
`enqueue` and `try` has the same exposure.
**Fix:** Move all post-enqueue setup inside the `try`:
```kotlin
val item = enqueue(event, entryId)
var timer: Job? = null
var watcher: Job? = null
try {
    timer = item.baseMillis?.let { base ->
        val millis = runCatching { timeoutScaler?.invoke(base, item.hasControls) }.getOrNull() ?: base
        launch { delay(millis); resolve(item, FeedbackResult.TimedOut) }
    }
    watcher = watch?.let { launch { it(item) } }
    item.result.await()
} finally {
    timer?.cancel(); watcher?.cancel()
    resolve(item, FeedbackResult.Dismissed)
}
```

### WR-03: `resolve` completes the deferred before removing the item, while holding the lock (inline-resume over-eviction)

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackState.kt:89-97` (also `enqueue` at 123-131)
**Issue:** `item.result.complete(result)` runs first and `items.remove(item)` second, both inside
`synchronized(lock)`. `complete()` runs the awaiting continuation's resumption on the completing
thread when the caller's dispatcher does not need a dispatch (`Dispatchers.Main.immediate` and
`Unconfined` both skip it). That means a `viewModelScope` or other immediate-dispatcher caller:

- resumes inline while the lock is held;
- sees `show()` return `Undone` or `Opened`;
- can immediately call `show()` again (the lock is reentrant, so it does not block).

At that moment the just-resolved item is still in `items`, so `enqueue` counts it toward the cap and
evicts the oldest real card early as `Dismissed`. The `enqueue` eviction loop has the same shape: it
completes evicted deferreds inline, mid-loop, with the state half-updated. The KDoc says "the item is
removed in the same critical section so a resolved card never counts toward the cap". That is not true
for inline-resuming callers. The KDoc warns against hoisting the state into a ViewModel, but nothing
enforces it, and `viewModelScope` is a likely call site.
**Fix:** Remove first, then complete. Better, collect completions and run them after releasing the
lock:
```kotlin
internal fun resolve(item: FeedbackStackItem, result: FeedbackResult): Boolean {
    val won = synchronized(lock) {
        if (item.result.isCompleted) false else { items.remove(item); true }
    }
    return won && item.result.complete(result)   // only the lock-holder path reaches here
}
```
Keep a single "claimed" flag (for example an `AtomicBoolean` per item) so `complete` is still
exactly-once. In `enqueue`, gather the evicted items inside the lock and complete them after it.
Add a test using `Dispatchers.Unconfined` for the caller that re-shows from the continuation.

### WR-04: Undo button is unreachable by TalkBack on cards that carry a target

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHost.kt:106-125, 162-169`
**Issue:** The card root gets `Modifier.clickable(onClickLabel = ...)` when `openable`. `clickable`
produces a `mergeDescendants = true` semantics node. The TextButton Undo child is therefore merged
into the root, and the root's own `OnClick` (open the target) wins the merge. For TalkBack the only
actions on a targeted `WithUndo` card are Open and Dismiss. Undo cannot be invoked. The tests hide
this because they query with `useUnmergedTree = true`. The KDoc notes that "merged-button semantics
are not part of this surface yet", but it describes it as a missing nicety and not as a functional
accessibility regression. Phase 25 ships a tagged `FeedbackStackHost` that has this gap.
**Fix:** Add a custom accessibility action for Undo next to the existing Dismiss action when
`item.hasUndo && !spent`:
```kotlin
customActions = buildList {
    if (item.hasUndo && !spent) add(CustomAccessibilityAction(undoLabel) { onUndo(); true })
    add(CustomAccessibilityAction(dismissLabel) { onDismiss(); true })
}
```
Add a merged-tree test that asserts the action exists. If it is intentionally deferred to Phase 26,
record that in the Phase 26 requirements so it is not lost.

### WR-05: Stale count prose in the `ComponentRegistry` KDoc is not guarded

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistry.kt:18-20`
**Issue:** The KDoc still says "66 registered, 5 intentionally unregistered = 71 total public
composables". Phase 25 made the live numbers 67/6/73 and updated `API.md` and `README.md`. The KDoc
claims `CatalogDocsDriftGuardTest` enforces these numbers, but that test only compares `API.md` and
`README.md`. The KDoc is already wrong and nothing fails.
**Fix:** Update to "67 registered, 6 intentionally unregistered = 73 total" and re-date the snapshot.
Better, add the KDoc to the files `CatalogDocsDriftGuardTest` checks, or drop the numbers from the
prose and point to `entries.size`.

## Info

### IN-01: `FeedbackEvent` header KDoc is stale

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackEvent.kt:5-18`
**Issue:** The type KDoc still says "This is a PURE ADDITION in Phase 31 (D-11): zero call sites,
zero consumers. Emitters and collectors are wired in Phase 32". That has been false for a long time,
and Phase 25 adds two new optional fields without touching it. The `DismissScreen` KDoc says it
"signals the current screen to dismiss", but `FeedbackStackState`/`FeedbackStackHost` render it as a
plain message card with no dismiss signal. That is a silent behavior divergence from the Material
path.
**Fix:** Refresh the header. Add a sentence to the `DismissScreen` KDoc and to `FeedbackStackState`
saying that the stack only displays the message and the caller owns the screen dismiss.

### IN-02: `FeedbackTarget.key` is echoed by generated `toString`, contradicting the KDoc

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackTarget.kt:8, 19`
**Issue:** The KDoc says "the library never parses, logs or echoes [key]". As a `data class` it
auto-generates `toString` that includes `key`. `UndoHistoryEntry` and `FeedbackEvent.*` data classes
carry it into their own `toString`, so any consumer logging an event or entry prints the opaque key.
Equality is intentionally value-based, but the no-echo promise is not true of `toString`.
**Fix:** Override `toString()` to `"FeedbackTarget(openLabel=$openLabel)"`, or soften the KDoc. Note
that an explicit `toString` override in a data class is source-compatible.

### IN-03: Test gaps in the highest-risk area

**File:** `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackTrackedUndoTest.kt:293-326`, `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackStateTest.kt`
**Issue:** The concurrency tests all use the `StandardTestDispatcher` and sequence `resolve`,
`runCurrent`, `cancel` deterministically. Three cases the production code actually has to survive are
not exercised:

- cancel-after-resolve-before-resume (WR-01);
- an inline-resuming dispatcher (WR-03);
- a throwing `timeoutScaler` (WR-02).

`parallelResolvers_produceExactlyOneWinner` is the only real race test, and it covers `resolve`
only.
**Fix:** Add the three tests above alongside the fixes.

---

_Reviewed: 2026-10-17_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
