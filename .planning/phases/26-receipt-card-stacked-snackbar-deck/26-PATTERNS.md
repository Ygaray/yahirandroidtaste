# Phase 26: Receipt-card stacked snackbar deck - Pattern Map

**Mapped:** 2026-10-17
**Files analyzed:** 13 (2 modified, 3 new main, 6 new tests, 2 touch-ups)
**Analogs found:** 12 / 13

All paths below are git-tracked. Base: `src/main/java/io/github/ygaray/yahirandroidtaste/` (MAIN) and `src/test/java/io/github/ygaray/yahirandroidtaste/` (TEST).

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match |
|---|---|---|---|---|
| MAIN `feedback/FeedbackStackState.kt` (MOD, lane 2) | state holder | event-driven (timer, pause) | itself, lines 90-160 | exact |
| MAIN `feedback/FeedbackStackHost.kt` (MOD, lane 2) | component (host) | request-response | itself | exact |
| MAIN `feedback/FeedbackDeck.kt` (NEW, internal) | component (custom Layout) | transform + event-driven | `component/OutcomeSheetReceipt.kt` `ReceiptActionRow` (503-553) | role-match |
| MAIN `feedback/FeedbackCard.kt` (NEW, internal) | component | event-driven (gestures) | `component/MicButton.kt` (semantics + single pointerInput), `modifier/SwipeableActionRow.kt` (anchoredDraggable) | role-match |
| MAIN `feedback/FeedbackDeckMetrics.kt` (NEW) | utility (pure) | transform | `receiptActionSlotWidths` (`OutcomeSheetReceipt.kt:485-493`) | exact |
| TEST `feedback/FeedbackStackPauseTest.kt` | test (JVM virtual time) | event-driven | `FeedbackStackStateTimeoutTest.kt` | exact |
| TEST `feedback/FeedbackDeckMetricsTest.kt` | test (pure JVM) | transform | `component/` receipt slot-width tests (search `receiptActionSlotWidths` in TEST) | role-match |
| TEST `feedback/FeedbackStackHostDeckTest.kt` | test (Robolectric) | event-driven | `FeedbackStackHostTest.kt` | exact |
| TEST `feedback/FeedbackStackHostSemanticsTest.kt` | test (Robolectric) | request-response | `FeedbackStackHostTest.kt` | exact |
| TEST `feedback/FeedbackStackHostLongLabelTest.kt` | test (NATIVE) | request-response | `component/OutcomeSheetReceiptLongLabelTest.kt` | exact |
| TEST `feedback/FeedbackStackSourceContractTest.kt` | test (source scan) | file-I/O | `feedback/UndoCenterScreenSourceContractTest.kt` + `component/SourceContractTestSupport.kt` | exact |
| `explorer/.../FeedbackFamilyScreen.kt` `FeedbackStackHostVariants` (~279-346; MOD, Burst x4 and Short buttons) | demo | event-driven | itself | exact |
| `CATALOG`/`API.md` docs touch-up | docs | n/a | none needed beyond internal-only (counts stay 67/6/73) | n/a |

## Pattern Assignments

### FeedbackStackState.kt (pause-aware timer; lane 2)

**Rewrite site** (`FeedbackStackState.kt:125-131`), the current body:
```kotlin
timer = item.baseMillis?.let { base ->
    val millis = scaledMillis(base, item.hasControls)
    launch {
        delay(millis)
        resolve(item, FeedbackResult.TimedOut)
    }
}
```
Replace `delay` with `runTimer(item, millis)` using `MutableStateFlow<PauseState>` and `internal var timeSource = TimeSource.Monotonic` (RESEARCH Code Examples, "Pause-aware timer"). Keep: `resolve` CAS (`:97-110`), `scaledMillis` read once (`:147-150`), `finally { timer?.cancel(); resolve(item, Dismissed) }`. All new members `internal`/`private` so `api.txt` is unchanged. Commit with `HUB_LANE_OVERRIDE=2` and a reason.

### FeedbackStackHost.kt (swap Column for FeedbackDeck; lane 2)

Keep the host signature (`state, onOpenTarget, modifier, undoLabel, dismissLabel`), `state.BindAccessibilityTimeout()`, `rememberUpdatedState(onOpenTarget)`, `key(item.id)`, and the CAS-winner rule (lines 85-87):
```kotlin
onBodyTap = { target ->
    if (state.resolve(item, FeedbackResult.Opened)) currentOnOpenTarget(target)
},
onUndo = { state.resolve(item, FeedbackResult.Undone) },
onDismiss = { state.resolve(item, FeedbackResult.Dismissed) }
```
Keep the three `internal const val` test tags (lines 33-39). Add host-local `fanned`, idle-collapse `LaunchedEffect`, and `DisposableEffect(state) { onDispose { clear pause reasons } }`.

### FeedbackCard.kt (card visuals, tap, semantics, swipe)

**Existing card to evolve** (`FeedbackStackHost.kt:97-193`): `Surface(shape = MaterialTheme.shapes.large, color = colorScheme.surfaceContainerHighest)`, 8dp dot (primary if target else onSurfaceVariant), message `bodyMedium`, supporting `bodySmall`, chevron `Icons.AutoMirrored.Filled.KeyboardArrowRight` with `contentDescription = null` and `FEEDBACK_STACK_CHEVRON_TEST_TAG`. Remove `clickable`; remove `maxLines=2/Ellipsis` per D-01 (wrap). Use `Dimens.CornerRadius.Card` and `Dimens.Elevation.Level2`.

Preserved semantics contract (Pitfall 2): keep customActions order `[Undo?, Dismiss]`, `testTag(FEEDBACK_STACK_ITEM_TEST_TAG)` on the merged node, and `target.openLabel.ifBlank { item.message }` as the onClick label (`FeedbackStackHost.kt:109`).

**One gesture owner** (analog `component/MicButton.kt:80-95, 117-148`):
```kotlin
val latestEnabled by rememberUpdatedState(enabled)
val latestOnTap by rememberUpdatedState(onTap)
...
.semantics(mergeDescendants = true) {
    role = Role.Button
    onClick { if (latestEnabled) latestOnTap() else latestOnDisabledTap(); true }
}
.pointerInput(Unit) {
    detectTapGestures(onPress = { offset -> ... tryAwaitRelease() ... })
}
```
Apply: `Role.Button` and `onClick` only when openable, `pointerInput(item.id) { detectTapGestures(onLongPress = {}, onTap = {...}) }` branching on the latest `isCollapsedBackCard` (fan) vs openable (open). Key on `Unit`/`item.id` only, never on state that flips mid-press.

**Undo button** (D-02): `DynamicActionButton(label, role = ActionButtonDefaults.ActionButtonRole.Neutral, onClick, modifier = Modifier.testTag(FEEDBACK_STACK_UNDO_TEST_TAG), emphasis = ActionButtonEmphasis.Text)` (signature `component/DynamicActionButton.kt:66-74`).

**Row layout with capped Undo** (analog `OutcomeSheetReceipt.kt:503-553`): custom `Layout`; do not call `measure` twice (use `maxIntrinsicWidth` for natural widths), mirror for RTL:
```kotlin
val startNatural = startMeasurable?.maxIntrinsicWidth(Constraints.Infinity)?.coerceAtMost(available)
...
val rtl = layoutDirection == LayoutDirection.Rtl
fun place(p: Placeable, logicalX: Int) {
    val x = if (rtl) rowWidth - logicalX - p.width else logicalX
    p.place(x, (rowHeight - p.height) / 2)
}
```
Card version: measure dot and chevron fixed first, Undo with `maxWidth = min(natural, 40% of row)`, body gets the remainder.

**Swipe** (analog `modifier/SwipeableActionRow.kt:171-184`; do NOT reuse the composable):
```kotlin
val state = remember { AnchoredDraggableState(initialValue = SwipeAnchor.Resting) }
SideEffect { state.updateAnchors(DraggableAnchors { ... Resting at 0f ... }) }
```
Divergence required: seed anchors at construction (`AnchoredDraggableState(initialValue, DraggableAnchors { Settled at 0f })`) and never call `requireOffset()` (the line 205-206 `snapshotFlow { state.requireOffset() }` is the first-frame crash). Read `state.offset.takeUnless { it.isNaN() } ?: 0f` inside `graphicsLayer {}`. Resolve `Dismissed` when `settledValue != Settled`. `enabled = fanned || index == 0`.

### FeedbackDeck.kt (Layout, pause observer, fan)

Analog for the custom `Layout` shape: `ReceiptActionRow` (above). Deck specifics from RESEARCH Pattern 4: newest first in composition order, `Modifier.zIndex(items.size - index)`, collapsed `translateY(-depth*14dp)` and `scale(1 - 0.06*depth)` bottom-center, back cards measured at front card height, one `Animatable` fan 0..1 read in layout/graphicsLayer lambdas, fanned cap `min(0.6*maxHeight, maxHeight - ime)` with `verticalScroll`, container `semantics { isTraversalGroup = true }`, card `traversalIndex = index.toFloat()`.

Deck-wide non-consuming pause observer (Initial pass, `finally` clears); copy from RESEARCH "Deck-level non-consuming pause observer". Never `waitForUpOrCancellation`.

### FeedbackDeckMetrics.kt

Analog `receiptActionSlotWidths` (`OutcomeSheetReceipt.kt:485-493`): a pure `internal fun` with a `when`, unit-testable with no composition. Put peek 14dp, scales 0.94/0.88, undo cap fraction, back-card height clamp, 72dp threshold here.

### Tests

- `FeedbackStackPauseTest`: copy header and helpers from `FeedbackStackStateTimeoutTest.kt:1-50` (`@OptIn(ExperimentalCoroutinesApi::class)`, `runTest`, `showAsync`, `advanceTimeBy` plus `runCurrent()` at boundaries); set `state.timeSource = testScheduler.timeSource`.
- `FeedbackStackHostDeckTest` / `SemanticsTest`: copy from `FeedbackStackHostTest.kt:1-60` (`@RunWith(RobolectricTestRunner::class) @Config(sdk = [35])`, `createComposeRule`, `setHost(...)` with `rememberCoroutineScope`, `SnackbarDuration.Indefinite` events because the card timer is not on the Compose clock). Tap back cards inside their peek strip.
- `LongLabelTest`: analog `component/OutcomeSheetReceiptLongLabelTest.kt` (`@GraphicsMode(NATIVE)`, `qualifiers = "w360dp-h800dp"`, `GetTextLayoutResult`, `assertInsideWindow`); font scale via `LocalDensity provides Density(density.density, 2f)` as in `SegmentedOptionSelectorTest.kt:57`.
- `SourceContractTest`: analog `feedback/UndoCenterScreenSourceContractTest.kt` using `component/SourceContractTestSupport.kt`; assert no `requireOffset`, no `SwipeableActionRow`/`RevealActionRow`, no `clickable` on the card.

## Shared Patterns

### Lane-2 commits
Any edit to a tracked line of `FeedbackStackHost.kt` or `FeedbackStackState.kt` needs `HUB_LANE_OVERRIDE=2` and a reason ("rewrites unreleased Phase 25 code; no released signature changes"). Prefer new files.

### Visibility and registry
All new composables and helpers `internal` or `private`. No ComponentRegistry change; run `./gradlew apiDump` then `git diff --exit-code api.txt` and `bash tools/verify-api-source-additive.sh v2.7.1`.

### Single resolve path
Every gesture, timer and button goes through `state.resolve(item, result)` (CAS). Navigate only on `true`.

### Compose-state rules
Lambda-form `graphicsLayer {}` and `Layout` placement for animated values; `rememberUpdatedState` for callbacks; pointerInput keys only `Unit`/`item.id`.

## No Analog Found

| File | Role | Reason |
|---|---|---|
| Pause-aware timer with `TimeSource` seam | state | No existing pause or clock-injection pattern; use RESEARCH Code Examples and the mock `docs/design/snackbar-go-to-item/index.html:335` |
| Fan/collapse deck transforms | layout | No stacked-deck layout in the repo; use the mock (`index.html:345-349`) |

## Open items for the planner (from RESEARCH)

Peek 14dp (brief D-01) vs >= 24dp (P26-D-06) conflict is unresolved: escalate to the orchestrator before plan B. Wave 0 probes: A1 (idle-collapse clock), A2 (long-press suppresses onTap), A3 (anchor-seeded offset), fling wiring.

## Metadata

**Analog search scope:** feedback/, component/, modifier/, and matching test directories
**Files read:** FeedbackStackHost.kt, FeedbackStackState.kt (90-160), MicButton.kt (40-150), OutcomeSheetReceipt.kt (480-555), SwipeableActionRow.kt (165-210), FeedbackStackHostTest.kt (1-60), FeedbackStackStateTimeoutTest.kt (1-50)
**Pattern extraction date:** 2026-10-17
