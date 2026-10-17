# Architecture Research: v2.8 Snackbar go-to-item + stacked feedback

**Domain:** Additive change to the feedback layer of a reusable Compose design library (`yahirandroidtaste`)
**Researched:** 2026-10-17
**Confidence:** HIGH for integration points (read from live source: `feedback/*.kt`, `OutcomeSheet*.kt`,
`ReceiptRow.kt`, `ComponentRegistry.kt`, the drift guards, `api.txt`, detekt config). HIGH for the M3 baseline
(disassembled `material3-android-1.4.0`: `SnackbarHostState` = `Mutex` + a single `currentSnackbarData`;
`toMillis` = 10000/4000 → `AccessibilityManager.calculateRecommendedTimeoutMillis`). HIGH that
`TestCoroutineScheduler.timeSource` exists (javap on `kotlinx-coroutines-test-jvm 1.11.0`). The state-machine
design below is my recommendation (MEDIUM until Phase 25 tests prove it).
*(Replaces the v2.4 ARCHITECTURE.md, which is still in git history.)*

## System overview

```
 consumer app (owns NavController, Hilt app, FeedbackController adapter)
   │  emit(event) ──launch──► FeedbackStackState.show / .emitTrackedWithUndo(store, event)
   │  onOpenTarget(target) ◄── FeedbackStackHost / OutcomeSheet / UndoCenterScreen   (key → route)
 ──┼───────────────────────────────────────────────────────────────────────────────
 feedback/  FeedbackTarget(key, openLabel)   FeedbackResult {Opened, Undone, Dismissed, TimedOut}
            FeedbackStackState ── items: SnapshotStateList<FeedbackStackItem> (newest first, ≤ maxVisible)
              │  each item: CompletableDeferred<FeedbackResult>, timer job, spent flag
              └─► FeedbackStackHost (reads items, writes resolve/pause/fan)  ── Phase 26 deck UI
            UndoHistoryStore (@Singleton) ── UndoHistoryEntry(+target) ── UndoCenterScreen(+onOpenTarget overload)
 model/     FeedbackEvent.Notify/WithUndo(+target)   Success(+target)   BatchRowResultUiModel(+target)
 component/ OutcomeSheet ×2 (+onOpenTarget) → OutcomeSheetContent / OutcomeSheetReceiptContent → internal OpenTargetRow
```

The one-way rule holds: the library never sees a route or a NavController. A target is an opaque key plus a
TalkBack label, and every surface reports it back through one `(FeedbackTarget) -> Unit`.

## New vs modified files

| File (under `src/main/java/.../yahirandroidtaste/`) | New/Mod | Phase | What changes |
|---|---|---|---|
| `feedback/FeedbackTarget.kt` | NEW | 25 | `@Immutable data class FeedbackTarget(val key: String, val openLabel: String)`. It goes in `feedback/` next to `UndoPreview`; `model/` already imports `feedback/`, so `Success` and the batch-row model can reference it. |
| `feedback/FeedbackResult.kt` | NEW | 25 | `enum class FeedbackResult { Opened, Undone, Dismissed, TimedOut }` |
| `feedback/FeedbackStackState.kt` | NEW | 25 (26 adds timer tuning) | The public state class, `@Composable rememberFeedbackStackState(maxVisible = 3)`, the internal `FeedbackStackItem`, and the timer |
| `feedback/FeedbackStackHost.kt` | NEW | 25 tracer, 26 full | Public host. Phase 26 adds the internal `FeedbackDeck` / `FeedbackCard` (internal composables, so the guard ignores them). |
| `component/OpenTargetRow.kt` | NEW (internal) | 27 | A label-less tappable row with a › chevron, shared by Classic, Receipt and batch rows (see Pitfall A) |
| `feedback/FeedbackEvent.kt` | MOD | 25 | Appends `target: FeedbackTarget? = null` as the LAST param of `Notify` and `WithUndo`. `DismissScreen` is untouched. |
| `feedback/UndoHistoryEntry.kt` | MOD | 25 | Appends `val target: FeedbackTarget? = null` after `consumedGuard` (the ctor is internal, so only `copy` / `componentN` matter: a new last param keeps every positional `copy` compiling). Adds a computed `val openableTarget get() = target?.takeIf { status == Available }`. `withStatus` carries `target` automatically. |
| `feedback/UndoHistoryStore.kt` | MOD | 25 | One new overload: `append(message, preview = null, groupId: String? = null, target: FeedbackTarget?, undoAction)`. `target` has NO default, so the two existing overloads stay the only candidates for every current call (see Pattern 4). |
| `feedback/UndoTracking.kt` | MOD | 25 | The existing `SnackbarHostState.emitTrackedWithUndo` now appends via the target overload (`target = event.target`); everything else stays byte-identical. Adds `suspend fun FeedbackStackState.emitTrackedWithUndo(store, event): FeedbackResult` in the same file, so "the ONE place" both sequences live stays one file. |
| `model/VoiceOutcomeUiState.kt` | MOD | 27 | `Success` gets `target: FeedbackTarget? = null` after `endAction` |
| `model/BatchRowResultUiModel.kt` | MOD | 27 | Gets `target: FeedbackTarget? = null` after `detail` |
| `component/OutcomeSheet.kt` | MOD | 27 | Both overloads end in `onOpenTarget: ((FeedbackTarget) -> Unit)? = null`. Internal `OutcomeSheetContent`, `SuccessBody` and `BatchResultsList` gain a defaulted `onOpenTarget`. |
| `component/OutcomeSheetReceipt.kt` | MOD | 27 | Same threading through `OutcomeSheetReceipt`, `OutcomeSheetReceiptContent` and `ReceiptSuccessBody`; the POL-01 pill-shape fix in `ReceiptHandledByPill`; the POL-02 dot in `handledByDotColor` |
| `feedback/UndoCenterScreen.kt` | MOD | 27 | New public overload; both overloads delegate to a private `UndoCenterScreenImpl`; `UndoHistoryRow` gets `onOpen: (() -> Unit)?` |
| `explorer/FeedbackFamilyScreen.kt`, `explorer/ComponentRegistry.kt` | MOD | 25 (demos 28) | Registers `FeedbackStackHost` (tier PATTERN) and allowlists `rememberFeedbackStackState` |
| `explorer/ExplorerActivity.kt`, `ReleaseDemos.kt`, `VoiceCommandFamilyScreen.kt` | MOD | 28 | `HubSchemeScope` at the Explorer root (DEMO-02); stack and go-to-item demos in EN and DE |
| `api.txt`, `API.md`, `README.md` | MOD | 25, 26, 27, 29 | `CatalogDocsDriftGuardTest` compares the registry counts against `API.md` / `README.md` (66/5/71 today), so **Phase 25 must bump the docs counts** in the same commit that registers the host |
| `src/test/.../V271CallSiteCanaryTest.kt` | NEW | 25 (grows in 27) | Every v2.7.1 call shape of the touched types compiles unchanged |

## FeedbackStackState design (Phase 25 core, Phase 26 timing)

**Model.** No `Mutex` serializes `show()`. That serialization is exactly the M3 queue we are replacing: M3
holds a `Mutex` around a single `currentSnackbarData`. Concurrency comes from **one
`CompletableDeferred<FeedbackResult>` per item**, and `complete()` returning `false` for every call after the
first IS the exactly-one-result guarantee: Opened, Undone, swipe, timer and eviction all race through the
same CAS, and the first one wins. List mutation (insert plus cap eviction) is a short **non-suspending**
critical section under a plain `synchronized(lock)`. A `Mutex` adds nothing here because nothing in that
section suspends.

```kotlin
@Stable class FeedbackStackState internal constructor(
    val maxVisible: Int,
    internal val timeSource: TimeSource = TimeSource.Monotonic,          // tests: testScheduler.timeSource
) {
    constructor(maxVisible: Int = 3) : this(maxVisible, TimeSource.Monotonic)
    internal val items = mutableStateListOf<FeedbackStackItem>()          // newest first; host reads
    internal var timeoutFor: (base: Long, hasControls: Boolean) -> Long = { b, _ -> b } // host wires a11y
    private val paused = MutableStateFlow(PauseState())                   // pointerDown || fanned

    suspend fun show(event: FeedbackEvent): FeedbackResult = showInternal(event, entryId = null)

    internal suspend fun showInternal(event: FeedbackEvent, entryId: String?, onShown: (FeedbackStackItem) -> Unit = {}) =
        coroutineScope {
            val item = FeedbackStackItem(event.toVisuals(), entryId)        // DismissScreen → message-only, Short (D-13)
            val evicted = synchronized(lock) { items.add(0, item); if (items.size > maxVisible) items.removeAt(items.lastIndex) else null }
            evicted?.result?.complete(FeedbackResult.Dismissed)            // D-06: the oldest leaves early
            onShown(item)
            val timer = launch { runTimer(item) }
            try { item.result.await() }
            finally { timer.cancel(); item.result.complete(FeedbackResult.Dismissed); synchronized(lock) { items.remove(item) } }
        }

    internal fun resolve(item: FeedbackStackItem, r: FeedbackResult): Boolean = item.result.complete(r)
    internal fun setPointerDown(down: Boolean) { paused.update { it.copy(pointer = down) } }
    internal fun setFanned(fanned: Boolean) { paused.update { it.copy(fanned = fanned) } }
}
```

- **How `show()` suspends and resolves.** It suspends on `item.result.await()` in the caller's coroutine. The
  host (Phase 25 tracer, then Phase 26) calls `resolve(item, Opened | Undone | Dismissed)`; the timer calls
  `resolve(item, TimedOut)`; eviction completes the oldest item with `Dismissed`. The item leaves `items` in
  `show()`'s `finally`. If the caller's coroutine is cancelled, the item is removed and stays resolved, so
  nothing is left visible.
- **`show()` never runs `undoAction`.** As with M3's `ActionPerformed`, the caller acts on `Undone`. This is
  what lets the tracked path route undo through `store.attemptUndo` exactly once (FSTK-07).
- **Per-item timer, owned by the state, not by the host.** The timer runs inside `show()`, so it is
  testable with no composition (Phase 25 SC3) and is cancelled automatically when the item resolves.
  Pause/resume with the remaining time:
  ```kotlin
  var remaining = timeoutFor(item.baseMillis, item.hasControls).milliseconds  // Indefinite → never launched
  while (remaining > Duration.ZERO) {
      paused.first { !it.any }                         // wait while touched / fanned
      val mark = timeSource.markNow()
      val pausedAgain = withTimeoutOrNull(remaining) { paused.first { it.any } }
      remaining -= mark.elapsedNow()
      if (pausedAgain == null) break
  }
  resolve(item, FeedbackResult.TimedOut)
  ```
  The pause is **deck-wide**: the design prototype does `items.forEach(pauseTimer)` on pointerdown. Its
  "remaining ≥ 800 ms" floor on resume is worth copying as a constant (a resumed card never vanishes
  instantly). The pause signal is a `StateFlow`, not snapshot state, on purpose: `snapshotFlow` only sees
  writes after `Snapshot.sendApplyNotifications()`, which plain JVM tests don't trigger.
- **Accessibility timeout.** `rememberFeedbackStackState` reads `LocalAccessibilityManager.current` and sets
  `timeoutFor = { b, c -> am?.calculateRecommendedTimeoutMillis(b, containsIcons = true, containsText = true, containsControls = c) ?: b }`,
  matching M3's `toMillis` exactly. Base values: Short 4000, Long 10000; `hasControls = hasUndo || target != null` (D-04).
- **Undone elsewhere (D-14).** The stack's `emitTrackedWithUndo` uses `showInternal(..., onShown = { item -> launch { store.entries.first { es -> es.none { it.id == id && it.status == Available } }; item.spent = true } })`.
  While `spent` is true the card hides Undo and the chevron. The watcher also fires on cap eviction.

```kotlin
suspend fun FeedbackStackState.emitTrackedWithUndo(store: UndoHistoryStore, event: FeedbackEvent.WithUndo): FeedbackResult {
    val id = store.append(event.message, event.preview, target = event.target, undoAction = event.undoAction) // BEFORE visible
    val result = showInternal(event, entryId = id, onShown = { /* spent watcher */ })
    if (result == FeedbackResult.Undone) store.attemptUndo(id)    // the only undo path; CAS guard vs Undo Center
    return result
}
```
It returns `FeedbackResult`, unlike the `Unit`-returning M3 variant. This is additive and costs nothing.

**How the host consumes state.** `FeedbackStackHost(state, onOpenTarget, modifier, undoLabel, dismissLabel)`
reads `state.items` (snapshot state, so it recomposes) and renders newest-first. A body tap runs
`if (item.target != null && !item.spent && state.resolve(item, Opened)) onOpenTarget(item.target)`; the
guard stops a double tap from navigating twice. Undo calls `resolve(Undone)`. A swipe or the Dismiss custom
action calls `resolve(Dismissed)`. `pointerInput` press/release calls `setPointerDown`, and fan-out/collapse
calls `setFanned`. Because `show()` removes the item at once, the **host keeps a local "exiting" copy for
exit animation**, as M3's `FadeInFadeOutWithScale` does. Consumers put the host in
`Scaffold(snackbarHost = { FeedbackStackHost(...) })`. `FeedbackStackItem` stays **internal** (a minimal API;
it can be made public later, additively).

## OutcomeSheet / UndoCenter: source-compat placement

- **`OutcomeSheet`, both overloads:** append `onOpenTarget: ((FeedbackTarget) -> Unit)? = null` LAST. Neither
  overload ends in a lambda today (`colors` and `sheetState` are last), so the CLAUDE.md "never after a
  trailing lambda" rule is not violated. `FeedbackTarget` is not experimental, so a plain defaulted param
  is right here; a separate overload, as `sheetState` needed, is not. The no-state overload forwards
  `onOpenTarget` into the hoisted one. A target row renders only when `success.target != null &&
  onOpenTarget != null`. Otherwise the code path is v2.7.1's, and the existing `OutcomeSheet*Test` files stay
  untouched (FOPEN-01).
- **`UndoCenterScreen`:** it ends in the trailing lambda `onClear`, so it **cannot** take an appended param.
  The new overload is `(entries, onNavigateBack, onUndo, groups = emptyList(), onUndoGroup = null,
  onOpenTarget: (FeedbackTarget) -> Unit, onClear)`. `onOpenTarget` is **required and non-null**, which is the
  only thing that tells the two overloads apart: every v2.7.1 call (including `UndoCenterScreen(e, b, u) { }`)
  matches only the old one. It sits after defaulted params, so callers must name it (the same idiom as
  `sheetState`). detekt `LongParameterList` (6, `ignoreDefaultParameters: true`) counts 5 params: OK. A row is
  openable iff `entry.openableTarget != null`. Add the tap as `onTap` in the existing `detectTapGestures`
  (which already owns `onPress` for the peek) plus `semantics { onClick(label = target.openLabel) }`. Do not
  stack a second `clickable`, because it would swallow the press-to-peek.

## ComponentRegistry placement

| Symbol | Where | Why |
|---|---|---|
| `FeedbackStackHost` | `feedbackFamilyEntries` (`FeedbackFamilyScreen.kt`), `tier = PATTERN` | A visual composable with built-in conventions (deck, swipe, undo). The Phase 25 tracer entry may have a minimal states matrix; Phase 28 enriches it. `ComponentPlaygroundIntegrityTest` requires `preview` whenever `controls` is non-empty. |
| `rememberFeedbackStackState` | `INTENTIONALLY_UNREGISTERED` | A public top-level `@Composable` (so the guard's column-0 scan catches it), but a state factory, not a visual. Reason: "state holder for FeedbackStackHost; exercised through its entry". This is the hub's first public `remember*`. |
| `FeedbackCard`, `FeedbackDeck`, `OpenTargetRow` | `internal` | The guard scans public only, so no action is needed |
| `OutcomeSheet` / `UndoCenterScreen` overloads | none | The guard checks names; both are registered already |

Total after v2.8: +1 registered, +1 allowlisted. Phase 25 must update the API.md and README.md counts, or
`CatalogDocsDriftGuardTest` goes red.

## Patterns to follow

1. **Opaque target, one app-level router.** The store is a `@Singleton` that outlives screens, so an
   entry may hold data only (D-08). The consumer's `onOpenTarget` decodes `key` → route and can skip a
   same-destination open.
2. **Deferred-per-item CAS for "exactly one".** Never track "resolved" with a boolean beside the deferred.
   `complete()`'s return value is the guard.
3. **Library derives, caller clears.** Library-owned state (`UndoHistoryEntry`, stack items) derives
   "not openable" from the entry's status. Caller-built state (`Success`, `BatchRowResultUiModel`) is
   cleared by the caller after Undo (D-14), and the KDoc says so.
4. **No-default discriminator param for overloads.** Use the shape `store.append(..., groupId, ...)` already
   uses: a new overload is reachable only when its new param is named, so existing calls never become
   ambiguous.

## Anti-patterns

- **Pitfall A, reusing `ReceiptRow(onClick=…)` for go-to-item.** Its summary always sets `stateDescription`
  to Expanded/Collapsed and draws the expand chevron when `onClick != null` (`ReceiptRow.kt:299`). TalkBack
  would announce "Collapsed" on a navigation row. Use the internal `OpenTargetRow` (Role.Button, `onClickLabel
  = openLabel`, › icon, no state description), styled to match ReceiptRow's padding and type.
- **Timers in a host `LaunchedEffect` (the M3 way).** These would make FSTK-06 untestable without
  composition and would stop timers whenever the host leaves composition. M3 gets away with it because only
  one item is visible at a time.
- **Serial `emit` in the consumer.** `FeedbackDispatcher.emit` is `suspend`. A controller that `await`s each
  `show()` turns the deck back into a queue. INTEGRATION.md must say `scope.launch { state.show(e) }` per
  event (Phase 29).
- **Calling `event.undoAction` from the stack path,** or `show()` running it, would double-undo against the
  Undo Center.

## Test strategy

| Layer | Harness | Covers |
|---|---|---|
| State machine | Plain JVM `runTest` (no Robolectric). `FeedbackStackState(3, testScheduler.timeSource)`. Each `show` runs under `async`, then `runCurrent()`. | One result per `show` (second `resolve` returns false); 4th arrival → oldest `Dismissed`; newest-first order; `advanceTimeBy(3999)` pending / `+1` → `TimedOut`; Long = 10000; pause then resume keeps the remainder (floor); fanned pauses all; Indefinite never times out; cancellation removes the item; DismissScreen mapping |
| Tracked path | JVM, real `UndoHistoryStore`, counting `undoAction` | Append before the first `items` emission; Undone → exactly one invocation; a racing `store.attemptUndo` still gives one; Opened/Dismissed/TimedOut give zero; target copied; `openableTarget == null` after Undone/Failed; `spent` flips when undone from the Center |
| Store / entry | Extend `UndoHistoryStoreTest` | New overload with and without a group; `withStatus` keeps the target; eviction unaffected |
| Host / deck UI | Robolectric `createComposeRule` | Body tap → `onOpenTarget` once + `Opened`; no target → `SemanticsMatcher.keyNotDefined(OnClick)` and no chevron tag; `swipeRight()` → `Dismissed` and no undo; semantics (Role.Button, onClickLabel, LiveRegion Polite, custom actions Undo + Dismiss, traversal newest-first); pointer down → `paused` true. **Don't assert wall-clock timeouts here:** the timer runs on the `show()` caller's dispatcher, not `mainClock`. |
| Sheet / Center | Robolectric | Target row present iff target and callback; batch rows mixed; the existing `OutcomeSheet*Test` and `UndoCenterScreen*Test` pass **unedited** (parity proof); long DE at 360dp, `fontScale = 2f` |
| Compat | `V271CallSiteCanaryTest` + `apiCheck` on a regenerated `api.txt` | Notify/WithUndo positional + named; `store.append` trailing-lambda shapes; `UndoHistoryEntry.copy`; both `OutcomeSheet` overloads; `UndoCenterScreen(e, b, u) { }` resolves to the old overload |
| Pill | Pure function + Robolectric | `handledByDotColor` contrast ≥ 3:1 vs pill container (light, dark, and a fixed dynamic sample palette); multi-line pill text bounds inside the shape at 2.0 |

## Suggested build order

1. **P25a (types):** `FeedbackTarget`, `FeedbackResult`, `target` on `Notify`/`WithUndo`, the canary. No behavior yet.
2. **P25b (store):** `UndoHistoryEntry.target` + `openableTarget`, the store overload, existing `emitTrackedWithUndo` passes the target.
3. **P25c (state):** `FeedbackStackState` with deferred, eviction and timer (the timer can land here: it's pure, and FSTK-06 is *accepted* in 26) + JVM tests.
4. **P25d (tracked + tracer):** stack `emitTrackedWithUndo` + spent watcher; tracer `FeedbackStackHost`; register + allowlist; docs counts; `api.txt`.
5. **P26:** internal deck/card, gestures (swipe, peek-tap fan, idle re-collapse), pause wiring, a11y timeout wiring in `remember…`, semantics.
6. **P27 (needs only 25b):** model `target` fields → `OpenTargetRow` → both OutcomeSheet styles + batch rows → UndoCenter overload → pill fixes (independent; can go first in the phase).
7. **P28:** `HubSchemeScope` at Explorer root, demos EN/DE + registry render tests, then the TalkBack sweep (fixes land here).
8. **P29:** API/STYLE/INTEGRATION (incl. the `launch`-per-emit rule and the key→route router), full gates, proposal.

## Open questions (resolve in Phase 25 discuss)

- **The card's second line and tone dot have no source in D-09.** `FeedbackEvent` carries only `message`
  and `target`; the brief's "item name" second line and the tone dot need a field. Options: append
  `supportingText: String? = null` (plus maybe a tone) to `Notify`/`WithUndo` now, or derive the tone from
  the variant (WithUndo = neutral). This must be settled in Phase 25, because the event shape freezes there.
- **Sheet-window snackbars.** SecondBrain's `SheetFeedbackHost` shows snackbars above a modal sheet. Inside
  the sheet it needs its own `FeedbackStackState` + host (a second, independent deck sharing the store);
  confirm with the consumer.
- **When the entry is undone from the Undo Center while the snackbar shows:** hide Undo/chevron
  (recommended) or auto-dismiss?

## Sources

- Live source (HIGH): `feedback/{FeedbackEvent,UndoTracking,UndoHistoryEntry,UndoHistoryStore,UndoCenterScreen}.kt`,
  `component/{OutcomeSheet,OutcomeSheetReceipt,ReceiptRow}.kt`, `explorer/ComponentRegistry.kt`,
  `src/test/.../explorer/{ComponentRegistryDriftGuardTest,CatalogDocsDriftGuardTest}.kt`, `api.txt`, `config/detekt/detekt.yml`
- `material3-android-1.4.0` bytecode (HIGH): `SnackbarHostState` (Mutex + single data), `SnackbarHostKt.toMillis`
- `kotlinx-coroutines-test-jvm 1.11.0` bytecode (HIGH): `TestCoroutineScheduler.getTimeSource()`
- Design prototype `docs/design/snackbar-go-to-item/index.html` (deck-wide pause, 800 ms floor); `.planning/research/v2.8-BRIEF.md` D-01..D-17
