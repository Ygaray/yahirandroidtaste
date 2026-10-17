# Stack Research: v2.8 Snackbar go-to-item + stacked feedback

**Domain:** Custom Compose snackbar stack (deck) host, tap-to-open targets, a11y semantics, inside the existing `yahirandroidtaste` Android library
**Researched:** 2026-10-17
**Confidence:** HIGH. Every API below was checked with `javap` against the artifacts that BOM `2026.04.01` actually resolves in `~/.gradle/caches`. That covers signatures, `@Deprecated` markers and `@Experimental*` opt-in markers. It is not taken from docs memory.

## Bottom line

**No new dependency. No catalog change. No BOM bump.** Everything v2.8 needs is already on the
compile classpath through `compose-bom 2026.04.01`, `material3`, `ui` (which pulls in `foundation` and
`animation`) and `kotlinx-coroutines` (transitive, already used by `UndoHistoryStore`). The
test side already has `ui-test-junit4`, Robolectric 4.16.1 and `kotlinx-coroutines-test` 1.11.0.
None of the APIs we need is `@Experimental*`, so no opt-in leaks into the public surface or into
consumers. That keeps the Metalava `apiCheck` and dependency-allowlist invariants clean.

## Resolved versions (BOM 2026.04.01, from the cached BOM POM)

| Artifact | Resolved | Notes |
|---|---|---|
| `androidx.compose.ui:ui` | **1.11.0** | semantics, `LocalAccessibilityManager`, `pointerInput` |
| `androidx.compose.foundation:foundation` | **1.11.0** | `anchoredDraggable`, `awaitEachGesture`, `clickable` |
| `androidx.compose.animation:animation(-core)` | **1.11.0** | `Animatable`, `animateFloatAsState`, `AnimatedVisibility` |
| `androidx.compose.material3:material3` | **1.4.0** | `SnackbarDuration` (already used by `FeedbackEvent`), `SwipeToDismissBox` (rejected, see below) |
| `androidx.compose.runtime:runtime` | 1.11.0 | `mutableStateListOf`, `snapshotFlow`, `@Stable` |
| `androidx.compose.ui:ui-test(-junit4)` | 1.11.0 | `performCustomAccessibilityActionWithLabel`, `DeviceConfigurationOverride` |
| `kotlinx-coroutines-test` | 1.11.0 | `TestCoroutineScheduler.timeSource` (virtual-clock `TimeSource`) |

## Recommended APIs, by concern

### 1. Stack state (Phase 25): `FeedbackStackState`

| API | Package | Why |
|---|---|---|
| Plain `@Stable class FeedbackStackState` holding a `mutableStateListOf<StackItem>()` | `androidx.compose.runtime` | M3 `SnackbarHostState` is single-visible by design: an internal `Mutex` queues `showSnackbar`, so a deck can't be built on it. Own the list. |
| `CompletableDeferred<FeedbackResult>` per item, `show()` = append + `await()` | `kotlinx.coroutines` | Gives exactly-one-result for free. Complete it from whichever path wins (open / undo / swipe / timeout / cap eviction) with `complete()`. That returns `false` for the losers, so there's no double resolution. |
| `rememberSaveable`? **No.** Use `remember { FeedbackStackState(maxVisible) }` | runtime | The items hold suspended continuations and undo lambdas, so they can't be saved. M3's `SnackbarHostState` isn't saveable either. |
| Injected `timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic` (internal ctor param) | `kotlin.time` (stdlib) | The pause-remaining timer has to measure elapsed time. Tests pass `testScheduler.timeSource` (verified present in coroutines-test 1.11.0), so FSTK-01/06 run on virtual time with **no composition** (Phase 25 criterion 3). Never use `SystemClock`/`System.nanoTime` directly. |

**Timeout mapping.** Copy M3's private `SnackbarDuration.toMillis` exactly. I verified it in the
`material3-1.4.0` bytecode. It's `internal`, so it can't be called from our code:

```kotlin
val original = when (duration) { Short -> 4_000L; Long -> 10_000L; Indefinite -> Long.MAX_VALUE }
accessibilityManager?.calculateRecommendedTimeoutMillis(
    original, containsIcons = true, containsText = true, containsControls = hasUndo || hasTarget,
) ?: original
```

- `AccessibilityManager.calculateRecommendedTimeoutMillis(Long, Boolean, Boolean, Boolean): Long` and
  `LocalAccessibilityManager` are stable in ui 1.11.0.
- M3 passes `containsControls = actionLabel != null`. D-04 widens that to Undo **or** target.
- Read `LocalAccessibilityManager.current` in the **host** and push it into the state with an internal
  setter, or pass it per item. Don't capture it in `show()`, which runs outside composition.
- `Long.MAX_VALUE` (Indefinite) must skip the timer entirely. Don't `delay(Long.MAX_VALUE)` and do
  arithmetic on it.

### 2. Swipe-to-dismiss (Phase 26): use `foundation` `anchoredDraggable`, not M3 `SwipeToDismissBox`

**Recommendation:** `Modifier.anchoredDraggable(state, Orientation.Horizontal, enabled = …)` with
`AnchoredDraggableState(initialValue = Settled)` and three anchors `{ Start at -width, Settled at 0, End at +width }`.
Set the anchors via `updateAnchors(DraggableAnchors { … })` once the card width is measured. When
`settledValue != Settled`, resolve `Dismissed`.

Why this and not `SwipeToDismissBox`:

| Factor | `anchoredDraggable` (foundation 1.11.0) | `SwipeToDismissBox` (M3 1.4.0) |
|---|---|---|
| House precedent | Already used by `SwipeableActionRow`, `RevealActionRow` and 5 cards, with the same `AnchoredDraggableState(initialValue)` ctor | Not used anywhere in the hub |
| API churn | Stable. The non-deprecated ctors are `(T)` and `(T, DraggableAnchors)` | 1.4.0 deprecated the old `SwipeToDismissBox` overload ("use updated signature with onDismissed") and every `confirmValueChange` ctor/remember. This is the second reshuffle in two releases. |
| Deck integration | We own the offset, so it composes with the deck's `graphicsLayer` (peek translation, 0.94/0.88 scale, swipe fade via `progress(from, to)`) | It forces a `Row`-scoped `backgroundContent` slot and its own layout. The fade/scale fights its internal offset. |
| Enable only the front card when collapsed | `enabled =` per card | `enableDismissFromStartToEnd/EndToStart` only |
| Programmatic dismiss for the a11y `dismiss` action | `state.animateTo(End)` (extension, stable) | `state.dismiss(direction)`. Equivalent, but it buys nothing here. |

Details to follow:
- Don't use the deprecated `AnchoredDraggableState(T, confirmValueChange)` ctors or the
  deprecated `progress` property. Use `progress(from, to)`. Detekt/compiler warnings on deprecations
  would otherwise creep in.
- Fling/threshold: take the defaults (`AnchoredDraggableDefaults.flingBehavior(state, positionalThreshold, …)`
  is available if tuning is needed). Thresholds now live on the fling behavior, not the state.
- Key each card with `key(item.id) { … }` so a card's draggable state survives deck reordering and
  cap eviction. Don't use `LazyColumn`: there are at most 3 items, and lazy lists add item-animation
  and semantics-ordering complications for nothing.

### 3. Pause-on-touch (D-05)

**Recommendation:** a non-consuming `Initial`-pass observer on each card. It sees every pointer
before the children do (body `clickable`, Undo `TextButton`, the `anchoredDraggable`) and never steals
events:

```kotlin
Modifier.pointerInput(item.id) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        onPause()
        try {
            do { val e = awaitPointerEvent(PointerEventPass.Initial) } while (e.changes.any { it.pressed })
        } finally { onResume() }   // runs on cancellation too (card removed mid-press)
    }
}
```

- Use `awaitEachGesture`, `awaitFirstDown(Boolean, PointerEventPass)` (foundation 1.11.0, stable) and
  `awaitPointerEvent(pass)`.
- Do **not** use `waitForUpOrCancellation()` here. It returns `null` as soon as a child consumes
  (the drag does), so the timer would resume mid-swipe.
- Do **not** rely on `interactionSource.collectIsPressedAsState()`. Press is delayed by the tap
  timeout and cancelled when a drag starts, so the timer would run during the touch.
- The second pause source, "deck fanned out", is plain state (`isExpanded`) in `FeedbackStackState`.
  Model pause as a **counter or set of reasons** (`pointerDown[id]`, `expanded`), not one boolean, so
  releasing a finger doesn't resume a fanned-out deck.

### 4. Semantics (D-11, A11Y-01): all in `androidx.compose.ui.semantics`, all stable in ui 1.11.0

| Need | API (verified signature) |
|---|---|
| Body = one node, button, open label | `Modifier.clickable(onClickLabel = target.openLabel, role = Role.Button, onClick = …)`. `clickable` already merges descendants. With no target, omit `clickable` (no click action in the tree, per FOPEN-02). |
| Undo stays a separate stop | Keep Undo a `TextButton` child. Merging stops at children that are themselves clickable, so it stays its own focus node. |
| Undo also as a custom action | `semantics { customActions = listOf(CustomAccessibilityAction(undoLabel) { …; true }, CustomAccessibilityAction(dismissLabel) { …; true }) }` (`setCustomActions(List<CustomAccessibilityAction>)`) |
| Dismiss = swipe equivalent | `semantics { dismiss(label = dismissLabel) { …; true } }` (`dismiss(String?, () -> Boolean)`). M3's own `SnackbarHost` sets exactly `dismiss` + `liveRegion` + `paneTitle` (verified in `SnackbarHostKt$FadeInFadeOutWithScale`). |
| Announce on arrival | `semantics { liveRegion = LiveRegionMode.Polite }` on the card body |
| Newest-first in the tree | Emit cards newest-first in composition. If the visual z-order needs the newest drawn last, use `Modifier.zIndex` for drawing and `semantics { isTraversalGroup = true }` on the deck plus `traversalIndex` (`setTraversalIndex(Float)`) per card, rather than reordering composition. |
| Fan out via a11y | Optional: `expand { … }` / `collapse { … }` on the deck (stable, same file) |

Tests use `performSemanticsAction(SemanticsActions.Dismiss)`,
`performCustomAccessibilityActionWithLabel(label)` (ui-test 1.11.0, no opt-in), and
`SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite)`.

### 5. Deck visuals (D-01)

`Modifier.graphicsLayer { translationY; scaleX; scaleY; alpha }`, driven by `animateFloatAsState`
(`Animatable` for the swipe-return), plus `AnimatedVisibility(enter = fadeIn() + slideInVertically())`
for arrival. These are animation 1.11.0 APIs, already used across the hub. Card surface:
`MaterialTheme.colorScheme.surfaceContainerHighest` with `RoundedCornerShape(16.dp)`. The tone dot
reuses the v2.6 tone tokens. The chevron is `Icons.AutoMirrored.Filled.KeyboardArrowRight` from
`material-icons-extended`, already a dependency. Use the AutoMirrored variant so it flips in RTL.
The idle re-collapse after fan-out is a `LaunchedEffect(isExpanded, lastInteraction) { delay(idle); collapse() }`.

### 6. Phase 27 ride-alongs

- **POL-01 pill at font scale 2.0:** measure lines with `onTextLayout { lineCount }` (or
  `TextLayoutResult`), then switch `RoundedCornerShape(50%)` to a capped `RoundedCornerShape(12.dp)`
  when `lineCount > 1`. No new API.
- **POL-02 dot 3:1 contrast:** compute it in a unit test with `Color.luminance()` (`ui-graphics`, already a dep):
  `(L1 + 0.05) / (L2 + 0.05) >= 3.0`. Use the hub light/dark schemes plus `dynamicLightColorScheme(context)` /
  `dynamicDarkColorScheme` (M3 1.4.0, API 31+, fine at minSdk 35) under Robolectric. Robolectric gives the
  framework default palette, so also add one or two hard-coded sample schemes captured from the TESTER.
- **Long-string / font-scale tests:** `DeviceConfigurationOverride(FontScale(2f) then ForcedSize(DpSize(360.dp, …)))`
  from ui-test 1.11.0. Already used in `SegmentedOptionSelectorTest` and `PlaceMapPickerTest`.

### 7. Undo Center target derivation (FTGT-02, D-14)

`UndoHistoryStore` already exposes a `StateFlow`. The host and Undo Center read it with
`collectAsStateWithLifecycle()` (`lifecycle-runtime-compose` 2.9.4, already a dep). They derive
"openable" from `entry.status == Available && entry.target != null`. Nothing new.

## What NOT to add

| Don't add | Why | Use instead |
|---|---|---|
| Any snackbar/toast library (e.g. third-party "stacked toast" libs, Accompanist) | Breaks the dependency allowlist (Android SDK, AndroidX/Compose, Hilt, Coil, navigation-compose, reorderable, osmdroid). It's also unnecessary. | The `foundation` + `ui` primitives above |
| `com.google.android.material:material` or `material-color-utilities` for POL-02 | Not on the allowlist, and it pulls in the View-system Material | `Color.luminance()` + M3 `dynamic*ColorScheme` |
| A Compose BOM bump (e.g. to a newer `2026.x`) | It's not needed, and it ripples into both consumers' resolution and the zero-baseline detekt/Metalava state mid-milestone | Stay on `2026.04.01` |
| `navigation-compose` types in any v2.8 signature | D-08: the library must not know routes. `FeedbackTarget` is an opaque `key` | `onOpenTarget: (FeedbackTarget) -> Unit` callback |
| `@ExperimentalMaterial3Api`/`@ExperimentalFoundationApi` types in public params (`SheetState`, `AnchoredDraggableState`, `SwipeToDismissBoxState`) | They force opt-in on consumers (see the v2.7.0 overload decision) and leak implementation detail into `api.txt` | Keep swipe/drag state private to the host. The public surface is only `FeedbackTarget`, `FeedbackResult`, `FeedbackStackState`, `rememberFeedbackStackState` and `FeedbackStackHost` |
| `kotlinx-coroutines-core` as an explicit catalog entry | Already transitive via compose runtime, and already used in `main` (`Mutex`/`StateFlow`) | Leave as is |
| Roborazzi / screenshot tooling | Declined by Yahir in v2.6 (Key Decisions) | Robolectric semantics/state assertions + Gate-1 device screencaps |

## Alternatives considered

| Category | Recommended | Alternative | Why not |
|---|---|---|---|
| Swipe | `foundation.anchoredDraggable` | M3 `SwipeToDismissBox` | Deprecation churn in 1.4.0, layout slot fights the deck transforms, no house precedent |
| Stack state | Own `@Stable` class + `CompletableDeferred` | Several `SnackbarHostState`s | Each one is internally serialized and owns its timer, so there's no pause, no cap eviction and no shared deck state |
| Timer clock | Injected `TimeSource` + `delay` | Frame clock (`withFrameMillis`) | The frame clock needs composition. Phase 25 requires state tests without composition. |
| Pause detection | `Initial`-pass `awaitEachGesture` observer | `collectIsPressedAsState` / `waitForUpOrCancellation` | Delayed press, and early resume when a child consumes |
| Deck layout | `Box` + `key(id)` + `graphicsLayer` | `LazyColumn` | At most 3 items. Lazy adds animation/semantics-order complexity. |

## Integration points (existing code touched)

- `feedback/FeedbackEvent.kt`: append `target: FeedbackTarget? = null` **after** `duration` on
  `Notify`/`WithUndo` (they're data classes, so `componentN` grows at the end only). Keep the
  `SnackbarDuration` type: the stack reuses it, which avoids a parallel duration enum.
- `feedback/UndoHistoryEntry.kt` (internal ctor) and `UndoHistoryStore.append(...)`: carry `target`.
- `feedback/UndoTracking.kt`: the existing `SnackbarHostState.emitTrackedWithUndo` must forward
  `event.target` into the store (FTGT-02). The new `FeedbackStackState.emitTrackedWithUndo` mirrors
  its order: append, then show, then `store.attemptUndo(id)` on `Undone` only.
- `feedback/` gets new files (`FeedbackTarget.kt`, `FeedbackStackState.kt`, `FeedbackStackHost.kt`).
  Both public `@Composable`s are registered or allowlisted in `ComponentRegistry` (drift guard).
- `modifier/SwipeableActionRow.kt`: reference pattern only. Don't reuse it for snackbar swipe. Its
  reveal-then-confirm semantics (left=delete, right=edit) is the wrong convention for D-07 dismiss.

## Sources

- `~/.gradle/caches/modules-2/files-2.1/androidx.compose/compose-bom/2026.04.01/*.pom`: resolved
  versions (HIGH)
- `javap` on `ui-android-1.11.0.aar`, `foundation-android-1.11.0.aar`, `material3-android-1.4.0.aar`,
  `ui-test-android-1.11.0.aar`, `kotlinx-coroutines-test-jvm-1.11.0.jar`: signatures, `@Deprecated`
  messages, absence of `@Experimental*` markers (control: `ModalBottomSheetKt` shows 5 hits) (HIGH)
- `material3` `SnackbarHostKt.toMillis` bytecode: 4000/10000/MAX and the
  `calculateRecommendedTimeoutMillis(orig, true, true, hasAction)` argument order (HIGH)
- `material3` `SnackbarHostKt$FadeInFadeOutWithScale$1$1`: M3's own `dismiss` + `liveRegion` +
  `paneTitle` semantics (HIGH)
- Hub source: `modifier/SwipeableActionRow.kt`, `RevealActionRow.kt`, `feedback/*`, `gradle/libs.versions.toml`,
  `build.gradle.kts` (HIGH)
