# Pitfalls Research

**Domain:** Adding a custom stacked snackbar host (Receipt-card deck), tap-to-open snackbars and clickable sheet / Undo Center rows to an existing, source-compatible Compose design library (`yahirandroidtaste`, milestone v2.8, Phases 25-29)
**Researched:** 2026-10-17
**Confidence:** HIGH for repo-grounded items (read from `feedback/*.kt`, `api.txt`, the drift-guard tests, detekt configs, `ReceiptRow.kt`, `OutcomeSheetReceipt.kt`); MEDIUM for Compose platform behavior (Context7 docs, javap of the resolved M3 1.4.0 AAR); the ModalBottomSheet window finding is LOW on its own but corroborated by the consumer's existing `SheetFeedbackHost` precedent.

## Critical Pitfalls

### 1. Side effects fire before the result is claimed (double-resolve, double navigation)
**What goes wrong:** A body tap calls `onOpenTarget` and then resolves `Opened`. A fast double tap, a tap landing in the same frame as the timeout, or a swipe that ends on a tap navigates twice or returns two results. The card is still composed until the next recomposition, so its handlers stay live.
**Why:** People treat "remove from list" as the guard, but removal is asynchronous to input.
**How to avoid:** Back every item with one `CompletableDeferred<FeedbackResult>`. Every resolver (body tap, Undo, swipe, Dismiss action, timer, cap eviction, TalkBack action) calls `if (deferred.complete(r)) { sideEffect() }`, and the side effect runs only on `true`. Removal and the exit animation follow the resolution and never gate it.
**Warning signs:** `onOpenTarget` called before `complete`, a `Boolean resolved` flag in snapshot state, results delivered from `onAnimationEnd`.
**Phase:** 25 (contract + unit tests for every pair of racing resolvers), 26 (gesture wiring keeps the rule).

### 2. `show()` that hangs, or leaks cards, on cancellation and when there is no host
**What goes wrong:** (a) With no `FeedbackStackHost` composed, or one inside a sheet that just closed, `show()` suspends forever, the same trap as M3 `showSnackbar`. (b) The caller's coroutine is cancelled (screen left, rotation) but the item stays in the list as a zombie card that still counts against the cap. (c) Cancellation is reported as `Dismissed`, which breaks "exactly one of four".
**How to avoid:** `try { deferred.await() } finally { if (!deferred.isCompleted) remove(item) }`. Cancellation rethrows `CancellationException` and produces **no** result, so document "exactly one result, or cancellation". Decide in Phase 25 whether the timer runs in the host (the M3 model, so no host means no timeout) or in `show()` (testable with no composition, matching SC-3). If it runs in `show()`, the a11y-adjusted timeout still needs `LocalAccessibilityManager`, so the host must publish it to the state, and the timer must start when the card is first composed, not at enqueue.
**Warning signs:** tests that only use `advanceUntilIdle` and never cancel, and `runTest` timeouts.
**Phase:** 25.

### 3. Undo routed around the store, or a target that survives Undo
**What goes wrong:** The host's Undo button calls `event.undoAction()` directly, bypassing `UndoHistoryStore.attemptUndo`'s first-consumer-wins CAS. A racing Undo Center tap then double-undoes. Or `Opened` / `TimedOut` / cap-`Dismissed` paths also call undo. Or the Undo Center undoes the entry while its card is still up and the card still opens a deleted item (D-14 says snackbars derive openability from the store status).
**How to avoid:** The host never touches `undoAction`. It only resolves `Undone`, and `FeedbackStackState.emitTrackedWithUndo` calls `store.attemptUndo(entryId)` once after `show()` returns `Undone`, mirroring `UndoTracking.kt`. Tracked items carry their `entryId`, and the host derives `openable = target != null && entry.status == Available` from `store.entries`. Add `append` before the item becomes visible (SC-4). Settle what a tracked card shows after an external undo (drop the chevron, or auto-dismiss) in discuss-phase. Today's M3 path just leaves a dead Undo, which is unspecified.
**Warning signs:** `undoAction` referenced anywhere under the host, or an untracked `show(WithUndo)` whose KDoc doesn't say "the caller undoes on `Undone`".
**Phase:** 25.

### 4. Source-compat traps specific to these signatures
- **`UndoHistoryEntry` has a public `copy` and `component1..6`** (`api.txt:916-923`) even though its ctor is internal. Insert `target` before `status` or `consumedGuard` and positional `copy` and destructuring shift. **Append it after `consumedGuard`.** It is fine after a private param, and `withStatus` keeps it. Expose openability as a computed member, not by nulling the field.
- **`UndoHistoryStore.append(message, preview, undoAction)`**: adding `target: FeedbackTarget? = null` before the trailing `undoAction` breaks any call that passes the action as a positional 3rd argument (a lambda becomes a type mismatch on `target`). Add a **new overload with `target` required**, so the old 3-arg and grouped overloads resolve exactly as before.
- **`UndoCenterScreen` overload:** the existing one ends in a required `onClear: () -> Unit` (the trailing lambda). If the new overload gives `onOpenTarget` a default, every existing call becomes an "overload resolution ambiguity" error. Make `onOpenTarget` **required**, place it before `onClear`, and keep `onClear` last.
- **`OutcomeSheet`:** the roadmap's "after `modifier`" must mean **at the end**. Inserting right after `modifier` shifts positional `style` / `colors`. In the hoisted overload, append after the required `sheetState`.
- **`FeedbackEvent` is a sealed interface.** Adding a subtype breaks consumers' exhaustive `when`. Don't add one (D-13 routes `DismissScreen` through `show()` instead). `FeedbackResult` is new: pick its shape (enum vs sealed) knowing a 5th value later is a source break.
- `WithUndo.target` goes after `preview`, and `Notify.target` after `duration`. Regenerate `api.txt` in the same commit and extend the call-site canary to a v2.7.1 file (`V271CallSiteCanaryTest`).
**Phase:** 25 (events, entry, store, host), 27 (`OutcomeSheet`, `UndoCenterScreen`, `Success`, `BatchRowResultUiModel`).

### 5. The *other* drift guard: domain vocabulary fails on `Feedback*`
**What goes wrong:** `DomainVocabularyDriftGuardTest` requires every public top-level `@Composable`'s head token to be in `PRIMITIVE_NOUN_ALLOWLIST` or `DOMAIN_VOCABULARY`. `"Feedback"` is in neither. `HEAD_TOKEN_REGEX.find` skips the lowercase `remember` prefix, so **both** `FeedbackStackHost` and `rememberFeedbackStackState` resolve to `Feedback` and go RED. Planners usually budget only for the registry guard.
**How to avoid:** Phase 25 adds `"Feedback"` to `PRIMITIVE_NOUN_ALLOWLIST` with a rationale (it's a hub structural noun like `Undo`), and registers `FeedbackStackHost` in the Feedback family. Allowlist `rememberFeedbackStackState` in `INTENTIONALLY_UNREGISTERED` ("state factory, exercised by the host demo"). Keep the card, peek and chevron sub-parts `private` / `internal`, because any public top-level `@Composable` trips both guards.
**Also:** the registry guard is **name-based**, so the new `UndoCenterScreen` and `OutcomeSheet` overloads raise no failure. Their demos (DEMO-03) and catalog-doc rows have to be added on purpose, because no guard forces them.
**Phase:** 25 (guards green with the tracer host), 28 (demos for the overloads).

### 6. Deck gesture conflicts (click vs swipe vs Undo vs peek vs pause)
- **Positional state:** rendering `items.forEach { Card(it) }` newest-first without `key(item.id)` means a new arrival shifts every card's `remember`ed swipe offset and press state onto its neighbour (a half-swiped card jumps). Always `key(item.id)`.
- **Pause detector eating taps:** detecting "pointer down" with `detectTapGestures` / `awaitFirstDown()` (`requireUnconsumed = true`) either consumes the down or misses downs that Undo already consumed. Observe it in a parent `pointerInput` using `PointerEventPass.Initial` with `awaitFirstDown(requireUnconsumed = false)`, then wait until all pointers are up (`changedToUpIgnoreConsumed`), and never consume. Pressing Undo must also pause.
- **Collapsed deck:** only the front card is swipeable. A tap on a tucked card's ~14dp peek must **fan out**, not open its target, so a back card's body click means "expand" while collapsed and "open" once fanned. Draw order must put the newest card on top (`zIndex`), or the peek strips steal the front card's taps.
- **Wrong swipe primitive:** don't reuse `SwipeableActionRow` / `RevealActionRow`. They are reveal-confirm (left=delete, right=edit) and snap back, while D-07 is a dismiss-either-way swipe that never undoes. Use foundation `anchoredDraggable` (already the hub's primitive) or the M3 1.4.0 `SwipeToDismissBox(..., onDismiss = ...)` overload (javap-verified). Don't use the deprecated `confirmValueChange`.
- **System back gesture:** with only the 16dp side inset, a swipe started near the edge triggers back navigation. Test it on the TESTER. Don't add a `BackHandler` for "collapse on back", because it would swallow back for the whole app while fanned out.
**Phase:** 26.

### 7. Timers that only work in wall-clock time
**What goes wrong:** Remaining time computed from `System.currentTimeMillis()` / `SystemClock` makes virtual-time tests lie: pause/resume look right on device but fail, or pass vacuously, under `runTest` / `mainClock`. A deck that re-collapses on idle while a finger is down, or a per-card timer that keeps running while fanned out (D-05), slips through.
**How to avoid:** Measure only with coroutine time: an injectable `TimeSource` (production `TimeSource.Monotonic`, tests `testScheduler.timeSource`). Pause by cancelling the `delay` job and storing the remaining time. In compose tests set `mainClock.autoAdvance = false` and use `advanceTimeBy`. Provide a fake `LocalAccessibilityManager` in tests, so Robolectric's default doesn't silently change 4000/10000. Timeout = M3 semantics: Short 4000, Long 10000, `calculateRecommendedTimeoutMillis(orig, containsIcons = true, containsText = true, containsControls = hasUndo || hasTarget)`. Write each test so it fails with pause removed (RETRO v2.5 lesson 3).
**Phase:** 26 (FSTK-06), with the clock seam chosen in 25.

### 8. Semantics that look right in the tree and wrong in TalkBack
- **`clearAndSetSemantics` on the card body deletes the Undo child.** Use `Modifier.clickable(onClickLabel = openLabel, role = Role.Button)`, or `semantics(mergeDescendants = true)` plus `onClick(label)`. A child `TextButton` keeps its own node because it merges itself.
- **Geometric traversal reads the oldest card first:** tucked cards peek *above* the front card, so top-to-bottom order gives oldest-first, which violates D-11 newest-first. Set `isTraversalGroup = true` on the deck and `traversalIndex` on **each merged card node** (it has no effect on a non-focusable wrapper).
- **Live region on the container** re-announces the whole deck on every arrival. Put `liveRegion = Polite` on each card. Content changes on a card (chevron dropped after an external undo) can trigger a re-announcement.
- **Duplicate "Dismiss":** D-11 asks for a custom action *and* `dismiss` semantics, and TalkBack may list both. Check on device, and raise any duplicate with the designer rather than silently dropping one.
- **Untargeted cards** must expose no click action and no `Role.Button`, but still read as one merged node.
- **TalkBack users can't pause:** explore-by-touch never produces a pointer-down, so the recommended-timeout setting is their only protection. Document it, and verify a Long + Undo card can be reached before it expires.
**Phase:** 26 (A11Y-01 tree assertions), 28 (device confirmation).

### 9. ModalBottomSheet window z-order and opening from a sheet
**What goes wrong:** M3 `ModalBottomSheet` is its own `Dialog` window, so a root `FeedbackStackHost` renders *under* the sheet and scrim, and `zIndex` can't fix that. The consumer already works around this with a sheet-routed `SheetFeedbackHost`. That means two `FeedbackStackState`s, two independent caps, and a sheet-hosted deck that vanishes (its `show()` callers cancelled) the moment the sheet closes. A go-to-item tap inside `OutcomeSheet` that navigates immediately leaves the sheet window over the new destination, or disposes it with no slide-down (STYLE §4 says to slide down on every close).
**How to avoid:** INTEGRATION.md states it: one root host plus an optional host inside sheet content, events routed by the consumer's dispatcher, and the cap is per host. Phase 27 decides the order for sheet rows: hide the sheet (the internal or hoisted `sheetState.hide()`) and then call `onOpenTarget`, or document that the caller must. Don't build a `FLAG_NOT_TOUCHABLE` overlay dialog, because it makes Undo untappable.
**Phase:** 26 (host behavior inside a sheet, demo), 27 (sheet-row ordering), 29 (INTEGRATION.md).

### 10. Undo Center row tap breaks hold-to-peek
**What goes wrong:** `UndoHistoryRow` uses `pointerInput { detectTapGestures(onPress = { showPeek = true; tryAwaitRelease(); ... }) }`. A `Modifier.clickable` added to the row's `ListItem` consumes the down in the Main pass, so the ancestor never sees it and **peek dies on openable rows**. Adding `onTap` to the same detector instead flashes the peek on every tap, and `detectTapGestures` has **no semantics**, so TalkBack gets no click action.
**How to avoid:** For openable rows (new overload only), use one detector: `onTap` opens, and the peek shows only after the long-press timeout (`onLongPress` sets it, then wait for release). Add `semantics { role = Role.Button; onClick(label = target.openLabel) { ... } }` by hand. The old overload keeps the exact v2.7.1 row code path, and the row Undo `TextButton` stays a separate node.
**Phase:** 27 (FOPEN-03).

### 11. Reusing `ReceiptRow` for the go-to-item row
**What goes wrong:** `ReceiptRow(onClick = ...)` always emits `stateDescription` Expanded/Collapsed and a rotating expand chevron, so the D-15 navigation row would announce "Collapsed" and point the wrong way. A batch row the caller forgot to clear after Undo all (D-14 puts that on the caller) stays navigable while dimmed. Unconditional wrappers added "for the clickable case" (`semantics(mergeDescendants = true)`, `then(...)`) change the v2.7.1 tree even with no target, which breaks "same code path".
**How to avoid:** Build a dedicated private target row (title = `summary`, trailing `›`, `Role.Button`, `onClickLabel = openLabel`, no state description). Branch `if (target != null && onOpenTarget != null)` around **new** nodes only, and keep the existing tests unedited. Defensively hide the chevron when the row's own undo state is Undone, and put the caller-clears rule in the KDoc.
**Phase:** 27 (FOPEN-01/02).

### 12. Pill fixes that change other people's pixels
- **Shape:** the pill uses `.background(..., CircleShape)`, so at font scale 2.0 the radius equals half of a 4-line height and clips text. Fixing it via `onTextLayout` plus a `lineCount` state costs an extra frame and a flicker. Use a custom `Shape` whose radius is `min(size.height / 2, cap)`, with `cap` at least half the one-line height so the 1.0 pill stays pixel-identical.
- **Dot:** `handledByDotColor` returns `colors.warnTone.content`, which is caller-passable. Changing `ToneDefaults.warn()` recolors every warn surface in both apps, which breaks the STYLE §1 hub-token rule. Instead, use the given color when it has at least 3:1 against `surfaceContainerHighest` (alpha composited, WCAG luminance) and fall back to a contrast-safe ring or darker tone only when it fails, so the hub-scheme default stays unchanged.
- **Testing:** the Explorer hub-scheme pin (Phase 28) *hides* the dynamic-color failure. Prove POL-02 with a unit test over a fixed synthetic dynamic scheme (a mauve-seeded `ColorScheme`), not via the Explorer. Geometry tests use `@GraphicsMode(NATIVE)`, because fake text measurement hides wraps (RETRO v2.5 lesson 2).
**Phase:** 27 (POL-01/02).

### 13. Navigation lambdas leaking into long-lived holders
**What goes wrong:** `onOpenTarget` captured by `pointerInput(Unit)` or `remember {}` goes stale, or a `NavController` or lambda ends up in `FeedbackStackState` / `FeedbackTarget` / `UndoHistoryStore` (the store is a Hilt `@Singleton` and outlives the Activity, which D-08 forbids).
**How to avoid:** `FeedbackTarget` stays a plain value pair (key, openLabel). The state holds no host callbacks, no Context and no `AccessibilityManager`. The host reads `rememberUpdatedState(onOpenTarget)` inside gesture and effect blocks. The KDoc warns against hoisting `FeedbackStackState` into a ViewModel or singleton, because its items hold `undoAction` lambdas that capture screen scope. Require a non-blank `openLabel`, so TalkBack never falls back to reading the opaque key.
**Phase:** 25 (model), 26 (host).

## Technical Debt Patterns

| Shortcut | Immediate Benefit | Long-term Cost | When Acceptable |
|----------|-------------------|----------------|-----------------|
| Timers on `System.currentTimeMillis()` | Quick to write | Untestable pause/resume, flaky virtual-time tests | Never |
| Defaulted `onOpenTarget` in the `UndoCenterScreen` overload | "Nicer" API | Ambiguity errors at every existing call site | Never |
| `Boolean` resolved flag instead of `CompletableDeferred.complete()` | Simple | Double navigation / double results under races | Never |
| One root host only, sheets ignored | Simpler docs | Snackbars invisible while any sheet is open | Only if INTEGRATION.md says so explicitly |
| Raising detekt thresholds for the host | Green build | Erodes the zero-baseline policy | Never. Module limits are already LongMethod 240 / LPL 18 (defaults ignored) / Cyclomatic 26 (`config/detekt-compose.yml`), so split the card, deck layout and gesture modifier into private functions instead |
| Fixing contrast in `ToneDefaults` | One-line fix | Recolors both consumers' warn surfaces | Never |

## Integration Gotchas

| Integration | Common Mistake | Correct Approach |
|-------------|----------------|------------------|
| M3 Scaffold `snackbarHost` slot | Host also applies `WindowInsets` padding, so the deck floats too high (double insets) | Mirror M3 `SnackbarHost`: no insets in the host, since Scaffold positions the slot. Document overlay use (Box over content) needing `safeDrawing` bottom insets, because minSdk 35 means edge-to-edge is always on |
| Host inside a sheet | Assumes it shares the root deck and cap | Separate state, separate cap, cancelled on sheet close (Pitfall 9) |
| IME open | A fanned 3-card column covers the focused field | The fan-out column is bounded and scrolls, or caps visible height. Check with the keyboard up on the TESTER |
| `emitTrackedWithUndo` (M3 path) | "Improving" it while adding the stack path | Leave `SnackbarHostState.emitTrackedWithUndo` byte-identical. The new extension is on `FeedbackStackState` |
| TESTER device sweep | Leaves TalkBack on, font scale 2.0, or density changed | Restore TalkBack off, `font_scale 1.0` and `wm density 450` (never `reset`), and report every state change. If TalkBack can't be driven agentically, park the check for Yahir. Never waive it |

## Performance Traps

| Trap | Symptoms | Prevention | When It Breaks |
|------|----------|------------|----------------|
| Reading animated offset/scale in composition | 3 cards recompose every frame during fan-out and swipe | Lambda forms: `graphicsLayer { }`, `offset { }`. Use `theme/Motion.kt` tokens, no hand-rolled `tween` | Visible jank on burst arrivals |
| Ticking remaining time in snapshot state | Recomposition every frame for every card | Keep time in the timer coroutine, not in `State` | Always |
| Collecting `store.entries` per card | 3 collectors, recompositions on unrelated store edits | One `collectAsState` in the host, then `derivedStateOf` for the visible ids' statuses | Store near its 50 cap |
| Snapshot list mutated off the main thread | Rare apply-conflict crashes when `show()` is called from IO | Confine mutations to the main thread (or a `Mutex`) inside the state | Concurrent bursts |

## "Looks Done But Isn't" Checklist

- [ ] **Result contract:** every racing pair (tap+timeout, swipe+Undo, cap+Undo, TalkBack action+tap) yields exactly one result, and cancellation yields none (Phase 25).
- [ ] **Vocabulary guard:** `DomainVocabularyDriftGuardTest` green, not just the registry guard (Phase 25).
- [ ] **Keys:** a 4th arrival during a half-swipe doesn't move the swipe to another card (Phase 26).
- [ ] **Pause:** a test fails when pause-on-press is removed, and Undo press also pauses (Phase 26).
- [ ] **TalkBack order:** newest card first, collapsed and fanned (Phase 26 tree, Phase 28 device).
- [ ] **Undo Center:** hold-to-peek still works on rows *with* a target (Phase 27).
- [ ] **Old paths:** existing `OutcomeSheet` / `UndoCenterScreen` tests pass unedited, with no new wrapper nodes (Phase 27).
- [ ] **Long strings:** DE/ES at ~360dp and font scale 2.0 on the card (Undo + chevron stay on it), on target rows and on the pill, checked with screenshots, not a11y dumps (STYLE §7) (Phases 26, 27, 29).
- [ ] **Fresh gates:** `testDebugUnitTest` / `detekt` / `apiCheck` forced to rerun (`--rerun-tasks`), not up-to-date reuse. Proposal lists `git log origin/main..HEAD`. No tag, no push (Phase 29).

## Pitfall-to-Phase Mapping

| Pitfall | Prevention Phase | Verification |
|---------|------------------|--------------|
| 1 Double-resolve | 25, 26 | Race unit tests on virtual time, double-tap UI test calls `onOpenTarget` once |
| 2 Cancel / no host | 25 | Cancel-mid-show test leaves no zombie, cap frees |
| 3 Undo routing / stale target | 25 | `attemptUndo` count == 1, external-undo test drops openability |
| 4 Source-compat | 25, 27 | Regenerated `api.txt` additions only, V2.7.1 canary compiles |
| 5 Vocabulary + registry guards | 25, 28 | Both guard tests green, overload demos present |
| 6 Gesture conflicts | 26 | Robolectric swipe/tap/peek tests, TESTER edge-swipe check |
| 7 Timers | 25 (seam), 26 | Virtual-clock pause/resume tests with a fake a11y manager |
| 8 Semantics | 26, 28 | Unmerged-tree assertions, TalkBack sweep on TESTER |
| 9 Sheet window | 26, 27, 29 | In-sheet demo, hide-then-open order test, INTEGRATION.md |
| 10 Undo Center peek | 27 | Peek-on-hold and tap-to-open both tested on one row |
| 11 ReceiptRow reuse | 27 | No `stateDescription` on target rows, unedited old tests |
| 12 Pill fixes | 27 | NATIVE geometry test at 2.0, contrast unit test on a synthetic dynamic scheme |
| 13 Lambda leaks | 25, 26 | No function-typed fields in `FeedbackTarget` / state, `rememberUpdatedState` in the host |

## Sources

- Repo (HIGH): `feedback/UndoTracking.kt`, `UndoHistoryStore.kt`, `UndoHistoryEntry.kt`, `UndoCenterScreen.kt:295-335`, `component/ReceiptRow.kt:174-190`, `OutcomeSheetReceipt.kt:261-300`, `api.txt` (UndoHistoryEntry 916-930, FeedbackEvent 787-816, OutcomeSheet 321-322, UndoCenterScreen 824), `ComponentRegistryDriftGuardTest.kt`, `DomainVocabularyDriftGuardTest.kt` (HEAD_TOKEN_REGEX, allowlist), `config/detekt-compose.yml`; `.planning/RETROSPECTIVE.md` v2.5/v2.6 lessons; `docs/STYLE.md`, `STYLE.REVERTED.md`.
- M3 1.4.0 AAR via javap (HIGH): `SwipeToDismissBox` overload with `onDismiss`; `SnackbarHostKt.toMillis(duration, hasAction, AccessibilityManager)`.
- Context7 / developer.android.com (MEDIUM): [Traversal order: isTraversalGroup, traversalIndex](https://developer.android.com/develop/ui/compose/accessibility/traversal); [Swipe to dismiss](https://developer.android.com/develop/ui/compose/touch-input/user-interactions/swipe-to-dismiss); [rememberSwipeToDismissBoxState, confirmValueChange deprecated](https://developer.android.com/reference/kotlin/androidx/compose/material3/rememberSwipeToDismissBoxState.composable).
- Web (LOW, corroborated by the consumer's `SheetFeedbackHost`): [SO 78525894, snackbar behind ModalBottomSheet](https://stackoverflow.com/questions/78525894/how-can-i-ensure-that-the-snackbar-remains-visible-and-not-hidden-behind-the-mod); [zenn.dev: ModalBottomSheet is a Dialog window; overlay workaround breaks action taps](https://zenn.dev/omori432/articles/compose-snackbar-above-modalbottomsheet?locale=en).

---
*Pitfalls research for: v2.8 Snackbar go-to-item + stacked feedback (yahirandroidtaste)*
*Researched: 2026-10-17*
