# Phase 26: Receipt-card stacked snackbar deck - Research

**Researched:** 2026-10-17
**Domain:** Compose gesture / semantics / timer work inside the existing `FeedbackStackHost` (internal deck + card, no new public API)
**Confidence:** HIGH for the file map, APIs and constraints (read from live source and javap this session); MEDIUM for gesture interplay and TalkBack behavior until Gate-1 / Phase 28 confirm it on the TESTER

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
Locked milestone decisions D-01..D-17 live in `.planning/research/v2.8-BRIEF.md` and apply here unchanged. The phase decisions below are numbered P26-D-xx in prose but use the parser shape.

- **D-01 [card-layout]:** wrap + capped Undo (the Receipt action-row lesson), measured heights, clamp collapsed back cards to the front card's height
- **D-02 [undo-button]:** DynamicActionButton Text emphasis (the Receipt bar's own text action)
- **D-03 [long-hold]:** suppress; pause-on-touch exists so the user can hold to read
- **D-04 [pause-scope]:** whole deck, reason set {pointer, fanned}, 800 ms floor
- **D-05 [swipe]:** anchoredDraggable, front card only when collapsed, every card when fanned; threshold ~72dp or fling
- **D-06 [peek-target]:** separate pointer and semantics paths (MicButton pattern); peek strip >= 24dp visual, 48dp touch via expanded hit area
- **D-07 [live-region]:** only the arriving card announces; keep both Dismiss forms and verify on device in Phase 28 (designer decides if TalkBack lists two)
- **D-08 [fanned-overflow]:** cap at ~60% of available height, scroll inside; host respects IME + nav insets

Boundary text: "Receipt-card stacked snackbar deck (UI) - milestone v2.8 (release v2.8.0, additive only). FSTK-02..06, A11Y-01. Deck and card are internal; the FeedbackStackHost signature from Phase 25 doesn't change. Install anchors before any requireOffset (the SwipeableActionRow first-frame crash keeps 14 card tests @Ignored)."

### Claude's Discretion
Internal naming, file split and test layout, within the hub conventions (CLAUDE.md, docs/STYLE.md).

### Deferred Ideas (OUT OF SCOPE)
999.4 voice-surface polish -> v2.8.1; combined "Undo & open"; share/MCP deep links; SB sheet-swap blink (app-side).

Also binding on every phase (brief): D-01 Direction A deck (`surfaceContainerHighest`, 16dp corners, tone dot, message + optional second line, Undo as `primary` text action, up to 3 visible, ~14dp peek, scale 0.94 / 0.88, peek tap fans out, re-collapses after idle); D-02 no action label; D-03 trailing chevron icon only with a target; D-04 timeouts unchanged (Short 4 s, Long 10 s, via `calculateRecommendedTimeoutMillis`, `containsControls` when Undo or target); D-05 pause while a pointer is down or the deck is fanned; D-06 4th arrival dismisses oldest; D-07 swipe dismisses one card (Dismissed), never undoes; D-11 a11y semantics; D-17 Phase 25 tracer host, Phase 26 owns look, gestures, timers, a11y.
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| FSTK-02 | Up to 3 Receipt cards, tucked deck, 4th arrival dismisses oldest | Cap + eviction already in `FeedbackStackState.enqueue` (Phase 25). Phase 26 adds the deck layout (custom `Layout`, depth transforms, back-card height clamp), the card (wrap + capped Undo), and the long DE/ES 360dp / font-scale 2.0 test. |
| FSTK-03 | Body tap opens (once), no target = inert, chevron only with target, no action label | Keep the CAS-winner-only navigation already in the host (`FeedbackStackHost.kt:85-87`). Move tap to `detectTapGestures` + a hand-written semantics `onClick` (MicButton pattern) so long-hold can be suppressed (D-03). |
| FSTK-04 | Peek tap fans out; re-collapse after idle | Host-local `fanned` state mirrored into `FeedbackStackState` pause reasons; `LaunchedEffect` idle collapse (mock: 4000 ms); fanned column capped at ~60% height with inner scroll (D-08). |
| FSTK-05 | Horizontal swipe dismisses exactly that card (`Dismissed`), never undoes | `anchoredDraggable` (Start / Settled / End), anchors installed before any offset read, `settledValue != Settled` resolves `Dismissed` through the existing CAS. |
| FSTK-06 | Today's timeouts through the accessibility API; pause while pointer down or fanned; resume remaining time | Pause-aware timer inside `show()` on an injectable `TimeSource` (virtual-clock tests), pause = `StateFlow` of reasons {pointer, fanned}, resume floor 800 ms. Timeout values and scaler already exist (`FeedbackStackState.kt:20-22,147-150`). |
| A11Y-01 | One merged `Role.Button` node, `onClickLabel`, Polite live region, Undo separate child + custom action, Dismiss custom action + `dismiss`, newest-first in tree | Semantics recipe in Code Examples; nested clickable (Undo) stays a separate node [CITED: developer.android.com/develop/ui/compose/accessibility/merging-clearing]; `isTraversalGroup` + `traversalIndex` for newest-first. |
</phase_requirements>

## Summary

Phase 26 turns the Phase 25 tracer host (a plain `Column` of cards) into the Direction A deck. No new dependency, no new public symbol: the host signature, `FeedbackStackState`, `FeedbackTarget`, `FeedbackResult` stay as they are; the deck and card are `internal`. The work splits into four concerns that touch the same two files, so they must run in series: (1) a pause-aware timer in `FeedbackStackState` (the only place the existing 25 code must be rewritten), (2) a deck `Layout` + card visuals (wrap + capped Undo, tucked transforms, back-card clamp), (3) gestures (tap vs long-hold, swipe, peek-tap fan, deck-wide Initial-pass pause observer, idle collapse, fanned overflow), (4) semantics (A11Y-01) and registry/doc touch-ups.

Three facts shape the plan. First, the timer today is `launch { delay(millis); resolve(TimedOut) }` (`FeedbackStackState.kt:125-131`) with no clock seam and no pause, so FSTK-06 needs a new timer body, an injectable `TimeSource` (set via an `internal var`, which leaves `api.txt` untouched), and a pause `StateFlow`. Second, every edit to an existing line of `FeedbackStackHost.kt` or `FeedbackStackState.kt` is a **lane-2** commit for the pre-commit classifier (baseline `v2.7.1`, file tracked in HEAD), so each such commit must carry `HUB_LANE_OVERRIDE=2` with a reason (the Phase 25 review-fix commits did exactly this); putting the deck in new files minimizes but does not remove it. Third, the `SwipeableActionRow` first-frame crash is a test-harness ordering problem: a `LaunchedEffect` collecting `requireOffset()` starts before the sibling `SideEffect` installs anchors. The card must never read `requireOffset()`; seed the state with anchors at construction and read the non-throwing `state.offset` (NaN-guarded).

There is one genuine conflict to resolve before planning (Open Question 1): D-06 says the peek strip is ">= 24dp visual" while the locked brief D-01 and ROADMAP success criterion 1 say "~14dp peek". This research recommends keeping 14dp visual (Yahir-locked) and does not find a layout-neutral way to give a 48dp touch zone, so it is escalated.

**Primary recommendation:** Build in four serial plans: (A) pause-aware timer + clock seam in `FeedbackStackState` with virtual-time tests; (B) internal `FeedbackDeck` / `FeedbackCard` layout and visuals with long-string tests; (C) gestures wired through one non-consuming Initial-pass pause observer, `detectTapGestures` for tap, `anchoredDraggable` for swipe; (D) semantics + exit animation + demo/doc touch-ups + Gate-1. Keep `FeedbackStackHostTest` green throughout as the parity net.

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Exactly-one result, cap, eviction | State holder (`FeedbackStackState`, composition-free) | Host (reads `items`) | Already done in Phase 25 (`resolve`, `enqueue`); Phase 26 must not move it |
| Timers, pause, 800 ms floor, a11y scaling | State holder (`show()` coroutine) | Host (pushes pointer / fanned reasons, binds accessibility manager) | Virtual-time testable with no composition; host owns only the signals |
| Fanned vs collapsed | Host (local UI state) | State holder (pause reason only) | Layout concern; the state needs just the boolean to pause timers |
| Swipe, tap, long-hold, peek tap | Card / deck composables (internal) | Host wires results into `state.resolve` | Gesture ownership stays on the card node; results always go through the CAS |
| Open navigation | Consumer (`onOpenTarget`) | Host calls it only after `resolve(Opened)` wins | Library never knows routes (D-08 brief) |
| Undo | Store via `emitTrackedWithUndo` | Host only resolves `Undone` | Host never touches an undo lambda |
| Semantics (A11Y-01) | Card merged node | Deck container (`isTraversalGroup`) | TalkBack reads the merged tree; order is set by `traversalIndex` |
| Insets / placement | Consumer (`Scaffold` slot or overlay) | Host (height cap only) | M3 Scaffold already lifts the snackbar slot above `contentWindowInsets`; the host must not add insets on top |

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| compose-bom | 2026.04.01 | pins ui / foundation / animation 1.11.0, material3 1.4.0 | Already the project BOM [VERIFIED: gradle/libs.versions.toml:12 `composeBom = "2026.04.01"`] |
| `androidx.compose.foundation` `anchoredDraggable` | 1.11.0 (BOM) | Swipe-to-dismiss, three anchors | House precedent (`SwipeableActionRow`, `RevealActionRow`); stable, non-deprecated ctor `AnchoredDraggableState(T, DraggableAnchors<T>)` exists [VERIFIED: javap of foundation `AnchoredDraggableState` list `AnchoredDraggableState(T)` and `AnchoredDraggableState(T, DraggableAnchors<T>)`, the `(T, Function1)` and 3-arg ctors carry `Deprecated: true`] |
| `androidx.compose.ui` semantics + pointer input | 1.11.0 (BOM) | `Role.Button`, `onClick(label)`, `liveRegion`, `customActions`, `dismiss`, `traversalIndex`, `isTraversalGroup`, `invisibleToUser` | All present in `SemanticsPropertiesKt` with no experimental marker [VERIFIED: javap `SemanticsPropertiesKt` lists `dismiss`, `setTraversalIndex`, `setTraversalGroup`, `setLiveRegion-hR3wRGc`, `setRole-kuIjeqM`, `setCustomActions`, `invisibleToUser`, `expand`, `collapse`; no `ExperimentalComposeUiApi` string in the class] |
| `kotlin.time.TimeSource` + `kotlinx-coroutines-test` 1.11.0 | stdlib / 1.11.0 | Remaining-time bookkeeping; tests inject `testScheduler.timeSource` | `testScheduler.timeSource` is `@ExperimentalCoroutinesApi` (existing timeout tests already `@OptIn(ExperimentalCoroutinesApi::class)`) [CITED: .planning/research/STACK.md, javap of coroutines-test 1.11.0] |
| Existing hub atoms | in-repo | `DynamicActionButton(role = Neutral, emphasis = Text)` for Undo (D-02); `Dimens.CornerRadius.Card`; `Dimens.Elevation.Level2` | Reuse, do not hand-roll |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| `androidx.compose.animation.core.Animatable` / `animateFloatAsState` | 1.11.0 | `fan` progress 0..1, swipe return, exit fade | Read in layout / `graphicsLayer` lambdas only, never in composition |
| `ui-test-junit4` + Robolectric 4.16.1 | existing | Host / semantics / long-label tests | `@Config(sdk = [35])`; geometry tests add `@GraphicsMode(NATIVE)` and `qualifiers = "w360dp-h800dp"` (see `OutcomeSheetReceiptLongLabelTest`) |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| `anchoredDraggable` | M3 `SwipeToDismissBox` | Rejected by research: deprecation churn, layout slot fights deck transforms, no house precedent (D-05 locked anyway) |
| `detectTapGestures` + hand-written semantics | `Modifier.clickable` | `clickable` cannot suppress the open after a long hold (D-03) and it pre-consumes the down that a second gesture owner needs (MicButton lesson, `MicButton.kt:49-59`) |
| Injected `TimeSource` | `System.currentTimeMillis()` / frame clock | Untestable on virtual time; forbidden by Phase 25 SC3 |

**Installation:** none. No new dependency, catalog entry or BOM bump.

**Version verification:** no package is added, so there is nothing to `npm view` / `pip index`. Resolved Compose versions were confirmed from the cached BOM and class jars this session (foundation / ui jars under `~/.gradle/caches/9.5.1/transforms`).

## Package Legitimacy Audit

No external packages are installed by this phase. Nothing to audit.

| Package | Registry | Age | Downloads | Source Repo | Verdict | Disposition |
|---------|----------|-----|-----------|-------------|---------|-------------|
| (none) | - | - | - | - | - | - |

**Packages removed due to [SLOP] verdict:** none
**Packages flagged as suspicious [SUS]:** none

## Architecture Patterns

### System Architecture Diagram

```
 consumer: scope.launch { state.show(event) }  /  state.emitTrackedWithUndo(store, event)
        |                                              (Phase 25, unchanged)
        v
 FeedbackStackState  --------------------------------------------------------------+
   items (newest first, <= maxVisible)   resolve(item, r) = CAS, first wins         |
   per-card timer in show():                                                        |
        wait while paused --> markNow --> withTimeoutOrNull(remaining){ paused }    |
        interrupted? remaining = max(800ms, remaining - elapsed) : TimedOut         |
   pause = StateFlow<{pointer, fanned}>  <------ setPointer(down) / setFanned(b) ---+--+
        ^                                                                           |  |
        | items (snapshot state)                                                    |  |
 FeedbackStackHost(state, onOpenTarget, modifier, undoLabel, dismissLabel)          |  |
   |- BindAccessibilityTimeout()  (existing)                                        |  |
   |- fanned (local) + idle-collapse LaunchedEffect  ------------------------------+--+
   `- FeedbackDeck (internal Layout, isTraversalGroup)
        |- deck-level Initial-pass observer: pointer down/up -> setPointer, idle reset
        `- per card (key(item.id)), index 0 = newest:
             FeedbackCard(merged semantics node, Role.Button only if openable)
               |- pointerInput: detectTapGestures(onLongPress = {}, onTap)  -> open | fan | nothing
               |- anchoredDraggable (enabled = fanned || index == 0) -> settled End/Start -> resolve(Dismissed)
               |- graphicsLayer: collapsed depth transform / fan interpolation / swipe fade
               `- row layout: dot | body(weight, wraps) | Undo (capped ~40%) | chevron (fixed 24dp)
                    Undo = DynamicActionButton(Neutral, Text)  --> resolve(Undone)
        tap on body (openable) --> if (state.resolve(item, Opened)) onOpenTarget(target)
```

### Recommended Project Structure
```
src/main/java/io/github/ygaray/yahirandroidtaste/feedback/
|- FeedbackStackState.kt     # MOD (lane 2): pause reasons, TimeSource seam, pause-aware timer
|- FeedbackStackHost.kt      # MOD (lane 2): host body swaps Column for FeedbackDeck, fan state, idle collapse
|- FeedbackDeck.kt           # NEW internal: Layout, depth/fan interpolation, traversal group, pause observer
|- FeedbackCard.kt           # NEW internal: card visuals, swipe, tap, semantics
`- FeedbackDeckMetrics.kt    # NEW internal: pure constants + functions (peek 14dp, scales, clamp, thresholds)
src/test/java/.../feedback/
|- FeedbackStackPauseTest.kt        # JVM runTest: pause / resume / floor / fanned / cleanup
|- FeedbackDeckMetricsTest.kt       # JVM: transforms, clamp, threshold
|- FeedbackStackHostDeckTest.kt     # Robolectric: fan, swipe, tap vs peek, long-hold, pause hook
|- FeedbackStackHostSemanticsTest.kt# Robolectric: A11Y-01 tree assertions
|- FeedbackStackHostLongLabelTest.kt# NATIVE, w360dp, fontScale 2.0, DE / ES
`- FeedbackStackSourceContractTest.kt # no requireOffset, no SwipeableActionRow, one gesture owner per card
```
All new composables are `internal` or `private`: the registry and `DomainVocabularyDriftGuardTest` only scan public top-level `@Composable`s, so no registration, no allowlist and no docs-count change is needed (counts stay 67 / 6 / 73).

### Pattern 1: Pause-aware timer (FSTK-06)
**What:** One `StateFlow<PauseState>` (pointer: Boolean, fanned: Boolean). Each card's timer waits until not paused, marks the clock, waits `remaining` or until a pause arrives, and on interruption reduces `remaining` with the 800 ms floor. This copies the approved mock: `function pauseTimer(ev){ clearTimeout(ev.t); ev.remaining=Math.max(800, ev.remaining-(Date.now()-ev.startedAt)); }` [VERIFIED: docs/design/snackbar-go-to-item/index.html:335], and the mock pauses the whole deck (`items.forEach(pauseTimer)` on pointerdown) and collapses after `4000` ms (`index.html:375`).
**When to use:** replaces the body of the timer `launch` at `FeedbackStackState.kt:125-131`.
**Why StateFlow:** `snapshotFlow` only sees snapshot writes after `Snapshot.sendApplyNotifications()`, which plain JVM tests do not trigger [CITED: .planning/research/ARCHITECTURE.md].
**Example:** see Code Examples.

### Pattern 2: One gesture owner per card node (MicButton lesson)
**What:** the card node carries `semantics(mergeDescendants = true){ role = Button; onClick(label) {...} }` for TalkBack / keyboard and ONE `pointerInput { detectTapGestures(...) }` for touch. No `clickable`. This is the shipped pattern: `MicButton.kt:117-123` sets `role = Role.Button` + `onClick { ...; true }` in `semantics(mergeDescendants = true)`, and `MicButton.kt:133-134` owns touch via `.pointerInput(Unit) { detectTapGestures(`. Read callbacks through `rememberUpdatedState` and key `pointerInput` on `Unit` or `item.id` only, never on `collapsed` / `openable`, so a state flip mid-press cannot cancel the gesture (`MicButton.kt:80-95`).
**Long-hold (D-03):** pass `onLongPress = { }` to `detectTapGestures`; once the long-press timeout elapses `onTap` is not invoked on release, so the open is suppressed [ASSUMED: Compose behavior from training knowledge; Wave 0 test pins it].
**Peek (D-06):** pointer `onTap` branches on the latest `collapsed && index > 0` -> fan out (never open). Semantics `onClick` always means open (a11y path), regardless of fan state.

### Pattern 3: Anchors before any offset read (the crash rule)
**What:** `SwipeableActionRow` creates `AnchoredDraggableState(initialValue = Resting)` with no anchors (`SwipeableActionRow.kt:171`), installs them in a `SideEffect` (`:177-184`), and a `LaunchedEffect(state)` collects `snapshotFlow { state.requireOffset() }` (`:205-206`). The `LaunchedEffect` runs before the `SideEffect` under the Robolectric dispatcher, so `requireOffset()` throws `The offset was read before being initialized` on frame one; `CardBaseTest.kt:30-34` documents it and 22 tests are skipped suite-wide [CITED: 25-VERIFICATION fresh run "22 skipped"].
**Rule for Phase 26:**
1. Construct with anchors: `AnchoredDraggableState(initialValue = Settled, anchors = DraggableAnchors { Settled at 0f })` so the offset is never NaN [ASSUMED: ctor seeds offset from the anchor of `initialValue`; Wave 0 probe].
2. Replace anchors once the width is known (`onSizeChanged` or the deck `Layout`) with `updateAnchors(DraggableAnchors { Start at -w; Settled at 0f; End at w })`.
3. Never call `requireOffset()` anywhere in the card. Read `state.offset.takeUnless { it.isNaN() } ?: 0f` inside `graphicsLayer {}` / `offset {}` lambdas.
4. Guard with a source-contract test that fails if `requireOffset` appears under `feedback/`.

### Pattern 4: Deck layout (D-01, D-05, D-08)
- Compose order stays **newest first** (index 0 = newest) so the semantics tree is newest-first; draw order is the opposite via `Modifier.zIndex(items.size - index)` so the front (newest) card is on top and peek strips do not steal front taps [CITED: .planning/research/PITFALLS.md #6].
- Collapsed: bottom-aligned overlay in one custom `Layout`. Depth d = index. Transform per mock: `translateY(-depth*14)`, `scale(1 - depth*0.06)`, origin bottom-center [VERIFIED: index.html:349 `tf='translateY('+(-depth*14)+'px) scale('+(1-depth*0.06)+')'`]. Brief gives 0.94 / 0.88 which equals 1 - 0.06d.
- Back cards are measured with height = front card height (clamp, D-01) so a tall older card cannot peek out more than ~14dp. Fanned: natural heights in a column, oldest on top, newest at the bottom (mock fanned layout `translateY(-depth*(H+G))`, `index.html:345,348`), 8dp gap.
- Drive collapsed -> fanned with one `Animatable` `fan` (0..1) read inside the `Layout` placement block and `graphicsLayer {}` so only the layout / draw phases rerun per frame (no per-frame recomposition; PITFALLS "Performance Traps").
- Fanned height cap: `min(60% of constraints.maxHeight, constraints.maxHeight - imeBottom)` with the fanned column inside `verticalScroll` (D-08). See Open Question 3 for the IME wording.
- Width: center and cap at about 560dp (FEATURES), 16dp side inset is the consumer's slot, the card itself keeps 16dp inner padding.

### Pattern 5: Card row with capped Undo (D-01, the ReceiptActionRow lesson)
`Row` measures unweighted children first, so a long German Undo label would take the whole width and squeeze the message to nothing. Use a small custom `Layout` (precedent: `ReceiptActionRow`, `OutcomeSheetReceipt.kt:503-553`, and `receiptActionSlotWidths` at `:485-493`): measure dot + chevron (fixed 24dp, `Dimens.ReceiptRow.ChevronSize`), measure Undo with `maxWidth = min(natural, 40% of row)` (wraps to at most 2 lines), give the body (message maxLines wrap, supporting line) the remainder, mirror for RTL. Chevron is `Icons.AutoMirrored.Filled.KeyboardArrowRight`, `contentDescription = null` (as in the current host).

### Anti-Patterns to Avoid
- **`clickable` + `detectTapGestures` on one node:** the clickable pre-consumes the down (MicButton root cause).
- **Reusing `SwipeableActionRow` / `RevealActionRow` for the swipe:** reveal-then-confirm semantics (left delete, right edit, snap back) are the wrong convention for D-07; also the crash lives there.
- **`requireOffset()` in the card, in a `LaunchedEffect`, or in `snapshotFlow`.**
- **`waitForUpOrCancellation` / `collectIsPressedAsState` for pause:** early resume when a child consumes (drag) and delayed press respectively [CITED: .planning/research/STACK.md].
- **`clearAndSetSemantics` on the card:** deletes the Undo child.
- **Reading animated values in composition** (use lambda forms) and **`LazyColumn`** for 3 items.
- **Unconditional wrapper nodes** that change the tree for the no-target card beyond what A11Y-01 requires.
- **Host adding `WindowInsets` padding** inside a Scaffold slot (double insets).
- **Calling `onOpenTarget` before `state.resolve(item, Opened)` returns true.**

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Exactly-one result | A `Boolean resolved` flag | Existing `FeedbackStackState.resolve` (lock-guarded `outcome` claim + `CompletableDeferred.complete`) | Races between tap / timer / swipe / eviction are already proven (50x8 parallel-resolver test) |
| Swipe physics | Custom `pointerInput` drag + fling | `anchoredDraggable` | Velocity, thresholds, snap are solved; house precedent |
| Undo button | Custom `TextButton` | `DynamicActionButton(label, role = Neutral, onClick, emphasis = ActionButtonEmphasis.Text)` | D-02; gives primary hue and 48dp minimum interactive size for free [VERIFIED: DynamicActionButton.kt:158-175 `val hue = if (destructive) scheme.error else scheme.primary`; `:211-220` Text branch uses `minimumInteractiveComponentSize()`] |
| Timeout scaling | Re-deriving M3 `toMillis` | Existing `timeoutScaler` / `scaledMillis` (`FeedbackStackState.kt:147-150`) and `BindAccessibilityTimeout` | Already wired and tested; D-04 only needs the pause layered on top |
| Corner / elevation tokens | Literals | `Dimens.CornerRadius.Card` (16dp) and `Dimens.Elevation.Level2` (3dp, "Standard list/card row rest state") | STYLE §2; mock shadow is 3dp |
| Reduced-motion handling | A custom setting reader | Compose animation scale + wall-clock `delay` for timers | Timers must not depend on the animation clock |

**Key insight:** this phase is composition of already-proven pieces (CAS resolution, accessibility scaling, `DynamicActionButton`, `anchoredDraggable`, the MicButton gesture split). The only genuinely new logic is the pause-aware timer and the deck geometry, and both are small pure-ish functions that should be unit-tested without composition.

## Common Pitfalls

### Pitfall 1: Rewriting a tracked line without the lane-2 declaration
**What goes wrong:** the pre-commit hook blocks the commit ("lane 2 (source-line rewrite) change on the fast path").
**Why:** `classify-hub-change.sh` diffs staged vs HEAD for every tracked `src/` file against the latest `v*` tag, so editing `FeedbackStackHost.kt` or `FeedbackStackState.kt` (new since v2.7.1 but tracked in HEAD) is lane 2 [VERIFIED: tools/hooks/pre-commit and tools/verify-additive-diff.sh comment "'pre-existing' now means 'tracked in HEAD'"].
**How to avoid:** put new code in new files; for the unavoidable rewrites use `HUB_LANE_OVERRIDE=2 git commit` with the reason in the message ("rewrites unreleased Phase 25 code; no released signature changes"), as `25-REVIEW-FIX.md` did. Plan tasks should say so explicitly.

### Pitfall 2: Existing Phase 25 host tests depend on structure
**What goes wrong:** `FeedbackStackHostTest` (12 tests) reads `config[CustomActions]`, `config[OnClick].label` and `assertHasNoClickAction()` from the node tagged `feedback_stack_item`, finds Undo by `feedback_stack_undo` with `useUnmergedTree`, and counts tagged nodes.
**How to avoid:** keep `testTag(FEEDBACK_STACK_ITEM_TEST_TAG)`, the Undo tag and the chevron tag on the same logical nodes; keep customActions order `[Undo?, Dismiss]`; keep the tagged node the merged node that also carries `OnClick` (only when openable) and `CustomActions`. Edit a Phase 25 test only when A11Y-01 forces it, and say so in the commit. `tapBody()` clicks at `(width*0.1, height*0.5)` of the tagged node, which still works with `detectTapGestures`.

### Pitfall 3: Back-card taps in tests land on the front card
**What goes wrong:** `performClick()` / center `click()` on a back card hits the front card (same layout slot, higher z-order).
**How to avoid:** tap a back card inside its peek strip (a few px from the back node's top edge). Hit testing follows `graphicsLayer` transforms [ASSUMED]; assert with a test that taps the strip and expects fan-out, not open.

### Pitfall 4: Pause stuck on after a card or the host leaves
**What goes wrong:** a card resolved (timer, Undo) while a finger is down, or the host leaves composition while fanned, leaves `pointer` / `fanned` true and every future timer waits forever.
**How to avoid:** the pause observer clears in `finally`; the host clears both reasons in a `DisposableEffect(state) { onDispose { ... } }`; collapse automatically when `items.size <= 1`. Test: remove the card mid-press, leave composition while fanned.

### Pitfall 5: Idle collapse fires under a held finger or after any interaction
**How to avoid:** key the idle `LaunchedEffect` on `(fanned, interactionTick, pointerDown)`; restart on every pointer event; do not start while down. The mock reschedules on release (`scheduleCollapse`, `index.html:375`).

### Pitfall 6: Resume floor misread
The mock's `Math.max(800, remaining - elapsed)` floors the *remaining* time when a pause begins, so a card touched at 3900 ms of 4000 times out 800 ms after release, not 100 ms. A card never touched is unaffected. Test both.

### Pitfall 7: Long German / Spanish Undo and the chevron
At fontScale 2.0 and about 296dp of card inner width, a 40% Undo cap (about 118dp minus button padding) can force a single long word to break mid-word. Test `Rückgängig` / `Deshacer` and a long caller label; if a single word breaks, raise the cap toward 50% or let Undo exceed the cap by one word width (Open Question 4). English defaults hide this (STYLE §7).

### Pitfall 8: Exit animation vs instant removal
`resolve` removes the item from `items` immediately (`FeedbackStackState.kt:98-110`), so without a host-local ghost the card vanishes with no motion (swipe is fine: the card is already off screen when it settles). If ghosts are added they must be inert (`clearAndSetSemantics {}` or `invisibleToUser()`), must not count toward the 3-card layout, and existing tests that `assertDoesNotExist()` after `waitForIdle()` must still pass (they will if the ghost finishes within idle). Treat ghosts as the last, optional task.

### Pitfall 9: Live-region re-announcement
Compose announces a live-region node when it appears or its content changes. Keep each card's structure stable across fan / collapse (same `key(item.id)`, same nodes), and expect that `spent` flipping (chevron + Undo removed) changes content. Verify on the TESTER in Phase 28; do not engineer toggling `liveRegion` per frame.

### Pitfall 10: `api.txt` should not change
All new members must be `internal` / `private`. Run `./gradlew apiDump` then `git diff --exit-code api.txt`; a non-empty diff means a public leak. `bash tools/verify-api-source-additive.sh v2.7.1` must still print `API SOURCE-ADDITIVE OK`.

## Code Examples

### Pause-aware timer inside `show()` (replaces `FeedbackStackState.kt:125-131`)
```kotlin
// Source: design from .planning/research/ARCHITECTURE.md "FeedbackStackState design", floor from
// docs/design/snackbar-go-to-item/index.html:335. internal var timeSource = TimeSource.Monotonic
// (tests set testScheduler.timeSource); PauseState(pointer, fanned) with val any get() = pointer || fanned.
private val paused = MutableStateFlow(PauseState())

internal suspend fun runTimer(item: FeedbackStackItem, total: Long) {
    var remaining = total.milliseconds
    while (true) {
        paused.first { !it.any }                                   // never counts down while paused
        val mark = timeSource.markNow()
        val interrupted = withTimeoutOrNull(remaining) { paused.first { it.any } } != null
        if (!interrupted) break
        remaining = maxOf(PAUSE_FLOOR, remaining - mark.elapsedNow())   // PAUSE_FLOOR = 800.milliseconds
    }
    resolve(item, FeedbackResult.TimedOut)
}
internal fun setPointerDown(down: Boolean) { paused.update { it.copy(pointer = down) } }
internal fun setFanned(fanned: Boolean) { paused.update { it.copy(fanned = fanned) } }
```
Keep `scaledMillis(base, hasControls)` read once at enqueue and keep `Indefinite` skipping the timer (`item.baseMillis` null).

### Deck-level non-consuming pause observer
```kotlin
// Source: .planning/research/STACK.md section 3 (Initial pass, never consume)
Modifier.pointerInput(Unit) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        state.setPointerDown(true); resetIdle()
        try {
            do { val e = awaitPointerEvent(PointerEventPass.Initial) } while (e.changes.any { it.pressed })
        } finally { state.setPointerDown(false) }   // also runs on cancellation (card removed mid-press)
    }
}
```

### Card gestures and semantics (one gesture owner)
```kotlin
// Source: pattern from component/MicButton.kt:117-148 (semantics onClick + single pointerInput owner)
val latestOpen by rememberUpdatedState(onBodyTap)       // read inside the gesture, not as a pointerInput key
val latestPeek by rememberUpdatedState(isCollapsedBackCard)
Modifier
    .testTag(FEEDBACK_STACK_ITEM_TEST_TAG)
    .semantics(mergeDescendants = true) {                // untargeted cards still merge into one node
        liveRegion = LiveRegionMode.Polite
        isTraversalGroup = false; traversalIndex = index.toFloat()   // newest (index 0) first
        if (openable) { role = Role.Button; onClick(label = openLabel) { onBodyTap(target); true } }
        customActions = listOfNotNull(undoActionOrNull, CustomAccessibilityAction(dismissLabel) { onDismiss(); true })
        dismiss(label = dismissLabel) { onDismiss(); true }
    }
    .pointerInput(item.id) {
        detectTapGestures(
            onLongPress = { /* D-03: swallow; the release will not open */ },
            onTap = { if (latestPeek) fan() else if (openable) onBodyTap(target) },
        )
    }
```
`openLabel` falls back to the message when blank (existing rule, `FeedbackStackHost.kt:109`: `target.openLabel.ifBlank { item.message }`). `traversalIndex` goes on the merged card node; the deck container sets `isTraversalGroup = true`.

### Swipe (anchors installed before any offset read)
```kotlin
// Source: API verified by javap (AnchoredDraggableState(T, DraggableAnchors<T>), updateAnchors, animateTo)
val drag = remember(item.id) {
    AnchoredDraggableState(initialValue = CardAnchor.Settled, anchors = DraggableAnchors { CardAnchor.Settled at 0f })
}
Modifier
    .onSizeChanged { s -> drag.updateAnchors(DraggableAnchors {
        CardAnchor.Start at -s.width.toFloat(); CardAnchor.Settled at 0f; CardAnchor.End at s.width.toFloat() }) }
    .graphicsLayer { translationX = drag.offset.takeUnless { it.isNaN() } ?: 0f /* + fade via progress */ }
    .anchoredDraggable(drag, Orientation.Horizontal, enabled = fanned || index == 0, flingBehavior = flingWith72dpThreshold)
LaunchedEffect(drag) { snapshotFlow { drag.settledValue }.first { it != CardAnchor.Settled }
    state.resolve(item, FeedbackResult.Dismissed) }
```
Threshold: positional threshold about 72dp (`{ _ -> with(density) { 72.dp.toPx() } }`) through `AnchoredDraggableDefaults.flingBehavior(state, positionalThreshold, snapAnimationSpec)` or `anchoredDraggableFlingBehavior(...)`, both present in foundation 1.11.0 [VERIFIED: javap `AnchoredDraggableDefaults.flingBehavior` and `AnchoredDraggableKt.anchoredDraggableFlingBehavior`]; the mock uses 90 px (`index.html:366` `Math.abs(dx)>90`). The exact fling wiring is a Wave 0 compile spike (Open Question 5).

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| `swipeable` / `SwipeToDismiss` | `anchoredDraggable` with `AnchoredDraggableState(T, DraggableAnchors)` | Foundation 1.6-1.9; ctors with `confirmValueChange` deprecated | Use the 2-arg ctor and `updateAnchors`; avoid deprecated ctors / `progress` property |
| M3 `SnackbarHostState` single-visible Mutex queue | Own `@Stable` state with a `CompletableDeferred` per card | Phase 25 | Deck is possible; M3 hosts remain untouched |

**Deprecated / outdated:**
- `AnchoredDraggableState(T, confirmValueChange)` ctors and the `progress` property [VERIFIED: javap shows `Deprecated: true` on the `(T, Function1)` and `(T, DraggableAnchors, Function1)` ctors and on `getProgress`].
- M3 `rememberSwipeToDismissBoxState(confirmValueChange)` [CITED: PITFALLS.md, not used here].

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | `LaunchedEffect { delay(...) }` and a `rememberCoroutineScope()` `delay` in a Robolectric Compose test are driven by `mainClock` (so idle-collapse can be tested with `mainClock.advanceTimeBy`). Phase 25's host test header says the card timer "runs on the show() caller's dispatcher, not on the Compose clock" | Validation | Idle-collapse tests flaky; fallback is an `internal var idleCollapseMillis` on the state set to a tiny value and `waitUntil` |
| A2 | `detectTapGestures(onLongPress = {}, onTap)` does not call `onTap` after the long-press timeout elapses | Pattern 2, D-03 | Long hold would still open; fallback is a manual `awaitEachGesture` measuring press duration |
| A3 | `AnchoredDraggableState(initial, DraggableAnchors{ initial at 0f })` makes `offset` non-NaN immediately | Pattern 3 | `.offset` lambda reads NaN on frame one; the NaN guard still protects, so low risk |
| A4 | Compose runs `RememberObserver` dispatch (LaunchedEffect start) before `SideEffect`s, which is why the crash occurs only under an eager test dispatcher | Pattern 3 | Explanation only; the rule (never `requireOffset`) holds either way |
| A5 | Hit-testing honors `graphicsLayer` translation / scale, so a tap on a back card's visible strip reaches that card | Pitfall 3 | Peek tap would not work; fallback is a layout-based offset for back cards |
| A6 | Polite live region set once on every card at composition produces exactly one announcement per arrival and none on fan / collapse; D-07 "only the arriving card announces" is satisfied by "no container live region, stable structure" | Pitfall 9, A11Y-01 | TalkBack re-reads cards; verify on the TESTER in Phase 28 |
| A7 | A 40% Undo cap is enough for German / Spanish at fontScale 2.0 without breaking a word mid-word | Pitfall 7 | Visible mid-word break; raise the cap |
| A8 | M3 `Scaffold` lifts a snackbar slot above `contentWindowInsets` (systemBars, not IME), so the host should add no insets and D-08 "respects IME" means the height cap, not padding | Open Question 3 | Double insets, or snackbar under the keyboard |

## Open Questions

1. **Peek strip: 14dp or >= 24dp visual, and how to get a 48dp touch zone?** (BLOCKING-ish, needs designer / Yahir via the orchestrator)
   - What we know: D-06 (phase decision) says "peek strip >= 24dp visual, 48dp touch via expanded hit area". The Yahir-locked brief D-01 and ROADMAP SC1 say "~14dp peek"; the approved mock uses 14 px per depth (`index.html:349`).
   - What's unclear: whether D-06 overrides the mock. Also, no layout-neutral way exists to extend a hit zone beyond the deck bounds: Compose hit-tests children only inside the parent's bounds, and extra transparent height raises the M3 Scaffold FAB offset (Scaffold sizes against the snackbar slot).
   - Recommendation: keep 14dp visual per depth (28dp total with three cards) and make each back card's strip full width; do not add layout height. If a 48dp zone is mandatory, ask the designer to approve either a larger visual strip or accepting the FAB lift. Do not silently pick; record the answer in the plan.
2. **Dismiss shown twice in TalkBack (custom action + `dismiss`).** Keep both per D-07 and check on the TESTER in Phase 28; Phase 26 only needs both present in the tree.
3. **IME and nav insets for the fanned cap (D-08).** Scaffold's default `contentWindowInsets` is `systemBars` (no IME) [ASSUMED: A8], so a Scaffold-slot host can sit under the keyboard unless the app uses `imePadding`. Recommendation: the host adds no padding; the fanned height cap is `min(0.6 * maxHeight, maxHeight - WindowInsets.ime bottom)` read in the layout block; KDoc covers overlay use. IME cannot be simulated reliably in Robolectric, so verify at Gate-1.
4. **Undo cap value.** D-01 says capped Undo (~40% width / 2 lines). Confirm with the long-label test; adjust the cap if a word breaks (A7).
5. **Fling wiring.** Which of `AnchoredDraggableDefaults.flingBehavior(...)` (composable) or `anchoredDraggableFlingBehavior(state, density, positionalThreshold, spec)` gives a 72dp threshold plus fling, and the default velocity threshold, needs a 10-minute compile spike. Does not change the design.
6. **Demo enrichment.** Phase 28 owns DEMO-03, but the existing `FeedbackStackHostVariants` (Indefinite durations only, `FeedbackFamilyScreen.kt:279-346`) cannot show timers or a burst. Recommendation: add a "Burst x4" and a Short-duration button in Phase 26 so Gate-1 and Yahir can exercise the deck; leave EN / DE demo parity to Phase 28.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| JDK 17 | build / tests | yes | OpenJDK 17.0.20.1 | - |
| Gradle wrapper | build / tests | yes | 9.4.1 (`./gradlew --version --offline`) | - |
| Android SDK | build | yes | `/home/yahir/Android/Sdk` | - |
| Robolectric / ui-test / coroutines-test | tests | yes (existing deps) | 4.16.1 / 1.11.0 / 1.11.0 | - |
| Gradle daemon (shared host) | build | one daemon already running | - | Use `--no-daemon`; never `./gradlew --stop` (kills every seat's daemon) |
| TESTER device `R5CT10XNKQN` | Gate-1 visual / gesture checks | not probed (do not touch devices during research) | SM-S908U | Park device-only checks for Yahir; restore density with `wm density 450` afterwards |

**Missing dependencies with no fallback:** none for build and unit tests.
**Missing dependencies with fallback:** device availability must be resolved through `~/.claude/context/devices/common.md` at Gate-1 time.

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit 4.13.2 + kotlinx-coroutines-test 1.11.0 (state) + Robolectric 4.16.1 / `createComposeRule` ui-test 1.11.0 (host) [CITED: 25-VALIDATION.md] |
| Config file | none beyond `build.gradle.kts`; no new infra |
| Quick run command | `./gradlew testDebugUnitTest --tests '*FeedbackStack*' --tests '*FeedbackDeck*' --no-daemon` |
| Guard run command | `./gradlew testDebugUnitTest --tests '*CatalogDocsDriftGuardTest' --tests '*DomainVocabularyDriftGuardTest' --tests '*ComponentRegistryDriftGuardTest' --tests '*ComponentStatesMatrixTest' --tests '*ComponentPlaygroundIntegrityTest' --tests '*LibraryInvariantsGuardTest' --no-daemon` |
| Full suite command | `./gradlew apiDump testDebugUnitTest detekt apiCheck --rerun-tasks --no-daemon`, then `git diff --exit-code api.txt`, `bash tools/verify-api-source-additive.sh v2.7.1`, `bash tools/test/run-all.sh` (never in parallel with Gradle; force a fresh rerun, not up-to-date reuse) |

### Phase Requirements -> Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| FSTK-02 | 4th show dismisses oldest; tracked WithUndo entry stays in store | unit (existing) | `--tests '*FeedbackStackStateTest' --tests '*FeedbackStackTrackedUndoTest'` | yes (Phase 25) |
| FSTK-02 | Collapsed transforms 14dp / 0.94 / 0.88, back cards clamped to front height | unit (pure) | `--tests '*FeedbackDeckMetricsTest'` | Wave 0 |
| FSTK-02 | DE / ES message + second line at 360dp, fontScale 2.0 wrap; Undo and chevron stay inside the card | Robolectric NATIVE | `--tests '*FeedbackStackHostLongLabelTest'` | Wave 0 |
| FSTK-03 | Body tap opens once + `Opened`; untargeted inert; chevron only with target; no action label | Robolectric | `--tests '*FeedbackStackHostTest'` | yes (keep green) |
| FSTK-03 | Long hold then release does not open (D-03) | Robolectric | `--tests '*FeedbackStackHostDeckTest'` | Wave 0 |
| FSTK-04 | Tap on peek strip fans, never opens; every card's body and Undo tappable when fanned; idle collapse | Robolectric | `--tests '*FeedbackStackHostDeckTest'` | Wave 0 |
| FSTK-04 | Fanned column capped at 60% height and scrolls | Robolectric NATIVE | `--tests '*FeedbackStackHostDeckTest'` | Wave 0 |
| FSTK-05 | `swipeLeft` / `swipeRight` on one card -> that card `Dismissed`, others stay, no undo run; collapsed: only front swipeable | Robolectric | `--tests '*FeedbackStackHostDeckTest'` | Wave 0 |
| FSTK-05 | A 4th arrival mid-swipe does not move the swipe to another card (`key(item.id)`) | Robolectric | `--tests '*FeedbackStackHostDeckTest'` | Wave 0 |
| FSTK-06 | Short 3999 / 4000, Long 9999 / 10000, scaler flags (existing) | unit (existing) | `--tests '*FeedbackStackStateTimeoutTest'` | yes |
| FSTK-06 | Pause keeps remainder; floor 800 ms; fanned pauses all; resume on release / collapse; cleanup when card removed or host leaves; test fails with pause removed | unit, virtual clock (`state.timeSource = testScheduler.timeSource`) | `--tests '*FeedbackStackPauseTest'` | Wave 0 |
| FSTK-06 | Pointer down on card or on Undo sets pause; up clears it | Robolectric (`performTouchInput { down(); up() }`, assert internal pause flag) | `--tests '*FeedbackStackHostDeckTest'` | Wave 0 |
| A11Y-01 | Merged node, `Role.Button` + `onClickLabel` when openable, Polite live region, Undo separate node + custom action, Dismiss custom action + `dismiss`, `traversalIndex` newest-first collapsed and fanned | Robolectric | `--tests '*FeedbackStackHostSemanticsTest'` | Wave 0 |
| Invariant | No `requireOffset`, no `SwipeableActionRow` / `RevealActionRow` import, no `clickable` on the card node, anchors seeded at construction | source contract | `--tests '*FeedbackStackSourceContractTest'` | Wave 0 |
| API | No public change | gate | `./gradlew apiDump && git diff --exit-code api.txt` | n/a |

### Sampling Rate
- **Per task commit:** the quick run command for the touched area (about 30-60 s)
- **Per wave merge:** quick run + guard run
- **Phase gate:** full suite fresh (`--rerun-tasks`), then Gate-1 self-UAT on the TESTER for the visual and gesture claims (deck look, peek, fan, swipe feel, pause by holding); TalkBack device confirmation is Phase 28 (A11Y-02)

### Wave 0 Gaps
- [ ] `FeedbackStackPauseTest.kt` - FSTK-06 (needs the `internal var timeSource` seam first)
- [ ] `FeedbackDeckMetricsTest.kt` - FSTK-02 geometry (pure functions in `FeedbackDeckMetrics.kt`)
- [ ] `FeedbackStackHostDeckTest.kt` - FSTK-03/04/05/06 gestures
- [ ] `FeedbackStackHostSemanticsTest.kt` - A11Y-01
- [ ] `FeedbackStackHostLongLabelTest.kt` - FSTK-02 long strings (use `SemanticsActions.GetTextLayoutResult`, `assertInsideWindow`, as in `OutcomeSheetReceiptLongLabelTest`; font scale via `LocalDensity provides Density(density.density, 2f)` as in `SegmentedOptionSelectorTest.kt:57`)
- [ ] `FeedbackStackSourceContractTest.kt` - crash-rule guard (precedent: `SourceContractTestSupport`)
- [ ] Probes to settle A1, A2, A3 and Open Question 5 before the gesture plan is written (one throwaway test each)

## Security Domain

`security_enforcement` is enabled in `.planning/config.json` (`security_asvs_level: 1`, `security_block_on: high`).

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2 Authentication | no | - |
| V3 Session Management | no | - |
| V4 Access Control | no | The library has no authorization surface; the consumer's `onOpenTarget` owns routing |
| V5 Input Validation | yes | `FeedbackTarget.key` stays opaque: never parsed, logged or echoed (`toString` omits it) [VERIFIED: FeedbackTarget.kt `override fun toString(): String = "FeedbackTarget(openLabel=$openLabel)"`]; blank `openLabel` falls back to the message at render time, no throwing init |
| V6 Cryptography | no | - |
| V7 Logging | yes | Phase 26 adds no logging; do not log message, supporting text or key |

### Known Threat Patterns for Compose UI + coroutines state

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| Double navigation from racing tap / timer / swipe | Tampering | Navigate only when `state.resolve(item, Opened)` returns true (CAS winner), already in place |
| Undo bypassing the store guard | Tampering | Host only resolves `Undone`; `emitTrackedWithUndo` is the only undo path (Phase 25) |
| Navigation lambda or Context retained by a singleton | Information disclosure | No lambdas in `FeedbackTarget` / state; host reads `rememberUpdatedState(onOpenTarget)`; state holds no Context (accessibility manager is released on detach) |
| Pause state stuck (denial of the timeout, card never leaves) | Denial of service | `finally` cleanup in the pointer observer, `DisposableEffect` reset on host disposal, auto-collapse at <= 1 card |
| Tapjacking over the deck | Spoofing | Out of scope for a Compose library; no overlay windows are created |
| Secret-looking fixtures | Information disclosure | Neutral test fixtures only (gitleaks over `v2.7.1..HEAD` was clean in Phase 25) |

## Project Constraints (from CLAUDE.md)

- Library is domain-agnostic and one-way: no consumer package imports, no secrets, no domain assumptions. `LibraryInvariantsGuardTest` enforces import roots (`kotlin.*`, `kotlinx.*`, `androidx.*` are allowed).
- Bindings-only Hilt: never add `@HiltAndroidApp` / `@AndroidEntryPoint`.
- `ComponentRegistry` drift guard: every public top-level `@Composable` in `component/`, `feedback/`, `modifier/`, `theme/` is registered XOR allowlisted. Keep all new composables `internal` / `private`.
- Source-compatible, not binary-compatible: append params with defaults, never after a trailing lambda, no new `@Deprecated(HIDDEN)` shims. Metalava `apiCheck` is the per-commit gate. Phase 26 should add no public API at all.
- Interaction conventions travel with the components: standardized snackbar / undo feedback, conditional-render-no-dead-space (an empty slot renders no node). The snackbar swipe is dismiss-either-way, distinct from the reveal-confirm row swipe; keep that distinction.
- Detekt zero baseline: never regenerate a baseline; module limits `LongMethod 240`, `LongParameterList 18` (defaults ignored), `CyclomaticComplexMethod 26` (`config/detekt-compose.yml`). Split card, deck layout and gesture modifier into private functions.
- Commands drop the module prefix: `./gradlew testDebugUnitTest`, `./gradlew detekt`, `./gradlew apiDump apiCheck`.
- Tags / repin / push are human-gated. Phase 26 cuts no tag and pushes nothing. Sequential-in-hub: commit on `main`, no consumer worktrees, do not touch consumer files.
- Shared host: never run `./gradlew --stop`; use `--no-daemon`. Independent verification must force a fresh rerun (Gradle "up-to-date" can be a prior build's output).
- STYLE.md rules that apply: surfaces on `surfaceContainerHighest`, 16dp card corners, 4dp grid, 48dp touch targets, weights 400 / 600 only, `primary` rare, no hand-rolled `tween` in app code (use `theme/Motion.kt` tokens or the 200 ms row expand), default calls stay pixel-identical, long strings (DE / ES, about 360dp, font scale 2.0) before sign-off, every new tappable gets a role and click label.
- Working-tree debris (`.planning/config.json`, `state.json`, `graphs/*`, stage markers, abandoned `uat-evidence` dirs) must never be committed.
- Project memory worth carrying: Overload-for-experimental-params (not needed here, no experimental type in a public signature); Test localized long strings before sign-off; Certify parks device / agentic gaps for Yahir, never waives them; Tester serial `R5CT10XNKQN`, restore density with `wm density 450`.

## File and Line Map (concrete, verified this session)

| File (under `src/main/java/io/github/ygaray/yahirandroidtaste/`) | Lines | What is there / what Phase 26 does |
|---|---|---|
| `feedback/FeedbackStackState.kt` | 20-22 | `private const val SHORT_MILLIS = 4_000L`, `LONG_MILLIS = 10_000L`, `DEFAULT_MAX_VISIBLE = 3` (keep) |
| | 55-56, 65 | `@Stable class FeedbackStackState(maxVisible: Int = DEFAULT_MAX_VISIBLE)`; `internal val items = mutableStateListOf<FeedbackStackItem>()` |
| | 71-72 | `@Volatile internal var timeoutScaler: ((Long, Boolean) -> Long)? = null` (read once at enqueue) |
| | 84-87 | `fun dismissAll()` (resolves Dismissed) |
| | 98-110 | `internal fun resolve(item, result): Boolean` (lock-guarded `outcome` claim, removes item, completes deferred outside the lock). **Do not change.** |
| | 112-141 | `showInternal(...)`: **lines 125-131 are the timer to replace** (`launch { delay(millis); resolve(item, FeedbackResult.TimedOut) }`); `finally` at 134-140 stays |
| | 147-150 | `scaledMillis` (keep) |
| | 152-167 | `enqueue` with cap loop `while (items.size > maxVisible)` (keep) |
| | 177-194 | `attachAccessibility` / `detachAccessibility` (reference-counted, keep) |
| | 216-273 | `FeedbackStackItem`: `outcome` (233), `spent` (236), `hasControls` (239), `from(...)` (242-271) |
| | 292-297, 305-314 | `rememberFeedbackStackState`, `BindAccessibilityTimeout` |
| `feedback/FeedbackStackHost.kt` | 34, 37, 40 | `FEEDBACK_STACK_ITEM_TEST_TAG = "feedback_stack_item"`, `..._UNDO_... = "feedback_stack_undo"`, `..._CHEVRON_... = "feedback_stack_chevron"` (keep names) |
| | 68-94 | Public host; signature at 69-75; `key(item.id)` at 80; `onBodyTap` at 85-87 (`if (state.resolve(item, FeedbackResult.Opened)) currentOnOpenTarget(target)`) |
| | 96-194 | `private fun FeedbackStackCard`: click modifier 108-114, customActions 122-137, shape / color 140-141 (`MaterialTheme.shapes.large`, `surfaceContainerHighest`), dot 148-157, texts 158-175 (`maxLines = 2` / `1` ellipsis, to be replaced by wrap), Undo `TextButton` 176-183, chevron 184-191. Replaced by `FeedbackDeck` / `FeedbackCard` |
| `feedback/UndoTracking.kt` | 76-107 | stack `emitTrackedWithUndo`; spent watcher sets `item.spent = true` (no change) |
| `modifier/SwipeableActionRow.kt` | 171, 177-184, 205-206, 302 | The crash: no-anchor ctor, `SideEffect` install, `LaunchedEffect` + `requireOffset()`, `.offset { requireOffset() }`. Reference only; do not edit (hub invariant: reveal-confirm) |
| `modifier/RevealActionRow.kt` | 104-112, 119, 134-135, 167-169 | Same pattern; its NaN-guarded `derivedStateOf { state.offset... !it.isNaN() }` (134-135) is the safe-read precedent |
| `component/MicButton.kt` | 80-95, 117-148 | `rememberUpdatedState` for callbacks; semantics `role = Role.Button` + `onClick`; single `pointerInput { detectTapGestures }` |
| `feedback/UndoCenterScreen.kt` | 296-338 | `detectTapGestures(onPress = ...)` + nested TextButton consumption precedent (comment at 302-307) |
| `component/DynamicActionButton.kt` | 65-93, 126-145, 158-175, 211-220 | Signature `(label, role, onClick, enabled, modifier, emphasis, icon)`; `enum ActionButtonEmphasis { Filled, Tonal, Text, TextSmall }`; hue = primary unless Destructive; Text branch |
| `component/OutcomeSheetReceipt.kt` | 485-553 | `receiptActionSlotWidths` + `ReceiptActionRow` custom `Layout` (model for the capped-Undo row) |
| `theme/Dimens.kt` | 146, 167, 275 | `CornerRadius.Card = 16.dp`; `Elevation.Level2 = 3.dp`; `ReceiptRow.ChevronSize = 24.dp`. Do not add public tokens (api.txt); keep metrics `internal` in `FeedbackDeckMetrics.kt` |
| `explorer/FeedbackFamilyScreen.kt` | 131-168, 279-346 | Registry entry (PATTERN, four state cells) and `FeedbackStackHostVariants`; optional burst / Short-duration buttons |
| `explorer/ComponentRegistry.kt` | 127-129 | `rememberFeedbackStackState` allowlist reason (no change) |
| `API.md` | 134 | Host row still says "Swipe, deck motion and pause are not part of this surface yet"; update the prose (counts unchanged) |
| Tests | `src/test/.../feedback/FeedbackStackHostTest.kt` (12), `FeedbackStackStateTest.kt` (22+), `FeedbackStackStateTimeoutTest.kt` (21), `FeedbackStackTrackedUndoTest.kt`, `RememberFeedbackStackStateTest.kt` (6), `testsupport/SemanticsDump.kt` | Parity net; extend, do not weaken |

## Suggested plan decomposition (series, one wave each; same files)

1. **Plan A, state (FSTK-06):** `internal var timeSource`, `PauseState` + `setPointerDown` / `setFanned` / reset, pause-aware `runTimer`, keep all existing 25 tests green; `FeedbackStackPauseTest`. Lane 2.
2. **Plan B, deck + card visuals (FSTK-02, FSTK-03 visuals):** `FeedbackDeckMetrics`, `FeedbackDeck` Layout (collapsed overlay, fan interpolation, clamp, width cap, 60% cap + scroll), `FeedbackCard` row (capped Undo via `DynamicActionButton`, wrap, chevron), host swapped to the deck with no gestures; `FeedbackDeckMetricsTest`, `FeedbackStackHostLongLabelTest`. Tracer first: one end-to-end render of the deck, then expand.
3. **Plan C, gestures (FSTK-03/04/05/06 wiring):** tap + long-hold + peek, swipe with seeded anchors, deck-level pause observer, fanned state + idle collapse + auto-collapse at <= 1 card + cleanup, host disposal reset; `FeedbackStackHostDeckTest`, `FeedbackStackSourceContractTest`.
4. **Plan D, semantics, polish, close (A11Y-01):** merged node, `Role.Button`, live region, `dismiss`, traversal; `FeedbackStackHostSemanticsTest`; optional exit ghosts; optional demo burst button; `API.md` prose; Gate-1 self-UAT on the TESTER with a throwaway harness (the Phase 25 method: `25-05-SELF-UAT.md`).

## Sources

### Primary (HIGH confidence)
- Live repo files read this session: `feedback/FeedbackStackState.kt`, `FeedbackStackHost.kt`, `FeedbackEvent.kt`, `FeedbackResult.kt`, `FeedbackTarget.kt`, `UndoTracking.kt`, `UndoCenterScreen.kt`, `modifier/SwipeableActionRow.kt`, `RevealActionRow.kt`, `component/MicButton.kt`, `DynamicActionButton.kt`, `OutcomeSheetReceipt.kt`, `theme/Dimens.kt`, `explorer/FeedbackFamilyScreen.kt`, `ComponentRegistry.kt`, `tools/hooks/pre-commit`, `tools/classify-hub-change.sh`, `tools/README-api-guard.md`, `config/detekt-compose.yml`, `docs/STYLE.md`, `docs/STYLE.REVERTED.md`
- `docs/design/snackbar-go-to-item/index.html` (script: pause floor, collapse timer, transforms, swipe threshold)
- javap of foundation 1.11.0 `AnchoredDraggableState`, `AnchoredDraggableKt`, `AnchoredDraggableDefaults` and ui 1.11.0 `SemanticsPropertiesKt` (cached class jars under `~/.gradle/caches/9.5.1/transforms`)
- `.planning/phases/25-*` SUMMARY / VERIFICATION / REVIEW-FIX / VALIDATION / SELF-UAT, `.planning/research/{v2.8-BRIEF,SUMMARY,ARCHITECTURE,PITFALLS,STACK}.md`, ROADMAP § Phase 26, `v2.8-DECISION-MAP.md` § Phase 26, CONTEXT

### Secondary (MEDIUM confidence)
- developer.android.com accessibility: merging and clearing semantics (nested clickable "defies the merge") via Context7 `/websites/developer_android_develop_ui_compose` [CITED: https://developer.android.com/develop/ui/compose/accessibility/merging-clearing]

### Tertiary (LOW confidence)
- Compose internal ordering of `RememberObserver` vs `SideEffect` dispatch and `detectTapGestures` long-press behavior (training knowledge; flagged A2, A4)

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - no new dependency; API presence confirmed by javap
- Architecture: HIGH for state / timer / host wiring (built on proven Phase 25 code); MEDIUM for deck geometry and gesture interplay until probes and Gate-1
- Pitfalls: HIGH (repo-grounded) with the TalkBack items MEDIUM until Phase 28

**Research date:** 2026-10-17
**Valid until:** 2026-11-16 (stable stack; revisit if the BOM changes)
