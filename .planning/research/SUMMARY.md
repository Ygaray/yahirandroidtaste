# Project Research Summary

**Project:** yahirandroidtaste (reusable Compose design-system library)
**Milestone:** v2.8
**Domain:** Additive feedback-layer change: stacked tap-to-open snackbar deck, go-to-item rows on OutcomeSheet / Undo Center, pill polish
**Researched:** 2026-10-17
**Confidence:** HIGH (APIs javap-verified against BOM 2026.04.01; integration points read from live source; the state-machine design is MEDIUM until Phase 25 tests prove it)

## Executive Summary

v2.8 replaces M3's one-at-a-time snackbar with a Receipt-card deck of up to 3 cards (D-01). Tapping a card body opens the referenced item through an opaque `FeedbackTarget(key, openLabel)`, and the same target model lights up chevron rows in both OutcomeSheet styles and the Undo Center. The reference systems point at a well-understood combination: the Sonner deck, notification-style tap-to-open and MDC's pause-on-touch. v2.8 is stricter than Sonner on overflow (it evicts instead of hiding), which is safe only because the existing `UndoHistoryStore` keeps every undo. All four research files agree that the decisions D-01..D-17 hold up. None of them reopens a decision.

The stack answer is simple: **no new dependency, no catalog change, no BOM bump.** Everything needed (foundation `anchoredDraggable`, `pointerInput` Initial-pass observers, stable semantics APIs, `TestCoroutineScheduler.timeSource`) is already on the classpath and none of it is `@Experimental`. The architecture splits into three parts: a plain `@Stable FeedbackStackState` that owns the item list, one `CompletableDeferred` per item, and a per-item timer on an injected `TimeSource`, so it can be tested on virtual time with no composition. A public `FeedbackStackHost` renders that state. An internal `OpenTargetRow` handles the sheet and Undo Center rows. Undo stays routed only through `store.attemptUndo`.

The risks sit almost entirely in four places: race and resolution correctness, source-compat placement of the new params, a second drift guard that planners don't budget for (`DomainVocabularyDriftGuardTest` fails on the `Feedback` head token), and gesture/semantics interplay (pause vs tap vs swipe vs peek, newest-first traversal against newest-at-bottom visuals, Undo Center hold-to-peek). Each one has a concrete prevention, mapped below. The existing 25-29 roadmap shape is correct; research mainly adds decisions that Phase 25 must lock before the event shape freezes.

## Key Findings

### Recommended Stack

Stay on BOM 2026.04.01 (ui/foundation/animation 1.11.0, material3 1.4.0, coroutines-test 1.11.0). Do not add any snackbar or toast library, View-Material, material-color-utilities or Roborazzi. Keep experimental types (`AnchoredDraggableState`, `SheetState`, `SwipeToDismissBoxState`) out of public signatures.

**Core technologies:**
- Own `@Stable FeedbackStackState` + `CompletableDeferred<FeedbackResult>` per item: M3 `SnackbarHostState` is Mutex-serialized and single-visible, so a deck can't be built on it. `complete()` returning false is the exactly-once guard.
- Injected `TimeSource.WithComparableMarks` (tests: `testScheduler.timeSource`): virtual-time pause/resume with no composition. Never use `SystemClock` or `currentTimeMillis`.
- Foundation `anchoredDraggable` (Start/Settled/End anchors), not M3 `SwipeToDismissBox`: it's the house precedent, it isn't deprecation-churned, and it composes with the deck's `graphicsLayer`. (PITFALLS lists the M3 `onDismiss` overload as viable. STACK's reasons win.)
- Timeouts: copy M3 `toMillis` (Short 4000, Long 10000, Indefinite skips the timer), passed through `calculateRecommendedTimeoutMillis(…, containsControls = hasUndo || hasTarget)`. The host reads `LocalAccessibilityManager` and pushes it into the state.
- Semantics: `clickable(onClickLabel, Role.Button)`, `liveRegion = Polite` per card, `customActions` (Undo, Dismiss) + `dismiss{}`, `isTraversalGroup` + `traversalIndex`. Chevron is `Icons.AutoMirrored.Filled.KeyboardArrowRight` with `contentDescription = null`.

### Expected Features

**Must have (table stakes):**
- Exactly one terminal result per `show()`. A double tap opens once. Cancellation removes the card and yields no result (documented exception).
- Eviction at the cap animates out as `Dismissed`. Independent per-card timers on wall-clock `delay`, not the animation clock (reduced motion).
- Pause resumes the *remaining* time. Tap slop separates tap from swipe. Swipe dismisses in both directions with no reveal step.
- Collapsed: only the front card is swipeable, and a peek tap fans the deck out (never opens). Auto-collapse after idle (mock: 4 s) and at 1 card or fewer.
- Host applies no insets (Scaffold slot). KDoc covers IME/overlay use. Width cap (~560dp). Works as a separate sheet-local host.
- a11y: non-gesture dismiss, polite announce once, newest-first traversal, decorative chevron.
- Go-to rows: whole row, at least 48dp, `Role.Button` + `onClickLabel`. No target means v2.7.1 code path and tree.

**Should have (differentiators):**
- Dismissing never loses an undo (store-backed). One `onOpenTarget` router for every surface.
- Live store sync: a visible card drops Undo and the chevron when its entry is undone elsewhere.
- Deck-wide pause (avoids re-stacking under a finger mid-swipe).

**Defer / reject:**
- Haptic tick on swipe threshold (v2.8.1). Coalescing identical messages (later, caller-keyed).
- Anti-features: "Open" text action, Undo & open (D-12), swipe-to-undo, hidden overflow, Back-to-collapse, countdown bar, Indefinite-by-default for targeted cards, persisting the deck, `onOpen` lambdas, the library navigating or hiding the sheet itself.

### Architecture Approach

New files go in `feedback/`: `FeedbackTarget`, `FeedbackResult` (enum), `FeedbackStackState` (+ `rememberFeedbackStackState`, internal `FeedbackStackItem`, timer) and `FeedbackStackHost` (tracer in 25; internal `FeedbackDeck`/`FeedbackCard` in 26). There's also an internal `component/OpenTargetRow` (27). List mutation is a short non-suspending `synchronized` section with no Mutex. The pause signal is a `StateFlow` (snapshotFlow doesn't fire in plain JVM tests). The host keeps a local "exiting" copy for exit animation, because `show()` removes the item immediately.

**Major components:**
1. `FeedbackStackState`: items (newest first, at most `maxVisible`), resolve CAS, cap eviction, per-item timer with deck-wide pause, `emitTrackedWithUndo(store, event)` (append before visible, `attemptUndo` only on `Undone`, "spent" watcher on `store.entries`).
2. `FeedbackStackHost`: renders the deck, wires gestures/pause/fan-out/semantics, calls `onOpenTarget` only after `resolve(Opened)` returns true, reads `rememberUpdatedState(onOpenTarget)`.
3. `OpenTargetRow` + threading: `OutcomeSheet` (both overloads, Classic and Receipt) gets `onOpenTarget: ((FeedbackTarget) -> Unit)? = null` appended LAST. The new `UndoCenterScreen` overload has a **required** `onOpenTarget` before trailing `onClear`. `UndoHistoryEntry.target` goes after `consumedGuard`, with a computed `openableTarget`. A new `store.append` overload takes a required `target`.

### Critical Pitfalls

1. **Side effects before the result is claimed:** every resolver does `if (deferred.complete(r)) sideEffect()`. Never use a Boolean flag, and never resolve from `onAnimationEnd`. Phase 25 needs race tests for every resolver pair.
2. **Cancellation / no-host hangs and zombie cards:** remove the card in `finally`, rethrow cancellation, and decide timer ownership and host-attached gating in 25.
3. **Undo routed around the store:** the host never touches `undoAction`. Only the stack's `emitTrackedWithUndo` calls `attemptUndo`, once. Leave `SnackbarHostState.emitTrackedWithUndo` byte-identical apart from forwarding `target`.
4. **Source-compat traps:** append `target` after `consumedGuard`/`duration`/`preview`; use a new `append` overload, not a param before the trailing lambda; `UndoCenterScreen`'s `onOpenTarget` must be required (a default causes ambiguity); `OutcomeSheet` params go at the end, not after `modifier`; add no `FeedbackEvent` subtype. Add a `V271CallSiteCanaryTest`.
5. **The second drift guard + docs counts:** add `"Feedback"` to `PRIMITIVE_NOUN_ALLOWLIST`, register `FeedbackStackHost` (PATTERN) and allowlist `rememberFeedbackStackState`. Bump the API.md/README.md counts (66/5/71 today) in the same Phase 25 commit, or `CatalogDocsDriftGuardTest` goes red.
6. **Gesture/semantics/row traps (26/27):** use `key(item.id)`; a non-consuming Initial-pass pause observer (not `waitForUpOrCancellation` or `collectIsPressedAsState`); `zIndex` so peek strips don't steal taps; no `clearAndSetSemantics`; `traversalIndex` on the merged card node. Don't reuse `ReceiptRow(onClick)`, which announces "Collapsed". An Undo Center `clickable` kills hold-to-peek, so use one `detectTapGestures` with a manual `semantics { onClick }`.

## Implications for Roadmap

The existing 25-29 structure matches the research's build order. Keep it. Every phase rewrites `api.txt`, so they run in series.

### Phase 25: Feedback target model & stack state
**Rationale:** The event/entry/state shapes freeze here, and everything else depends on them.
**Delivers:** `FeedbackTarget`, `FeedbackResult`, `target` on `Notify`/`WithUndo`/`UndoHistoryEntry`, the store overload, `FeedbackStackState` (deferred CAS, eviction, timer on `TimeSource`, cancel rule), the stack `emitTrackedWithUndo` + spent watcher, a tracer host, registry + vocabulary + docs-count updates, the canary, `api.txt`.
**Order:** 25a types, then 25b store, then 25c state + JVM tests, then 25d tracked path + tracer + guards.
**Avoids:** Pitfalls 1, 2, 3, 4, 5, 13.

### Phase 26: Receipt-card stacked snackbar deck
**Rationale:** Needs the 25 state and host signature. All gesture, timer and semantics complexity lives here.
**Delivers:** Internal deck/card, `anchoredDraggable` swipe, peek-tap fan-out + idle collapse, deck-wide pause with a resume floor, a11y timeout wiring, D-11 semantics, back-card height clamp, fan-out height cap, peek hit area, width cap.
**Avoids:** Pitfalls 6, 7, 8 (tree level), 9 (in-sheet host behavior).

### Phase 27: Open from the sheet & Undo Center, pill fixes
**Rationale:** Needs only 25b, but runs after 26 because of `api.txt` sequencing. The pill fixes are independent and can go first.
**Delivers:** `Success.target`, `BatchRowResultUiModel.target`, `OpenTargetRow`, both OutcomeSheet styles, the UndoCenter overload (tap opens, long-press peeks), POL-01 custom capped-radius `Shape` (not `onTextLayout`, which flickers), POL-02 dot fallback that leaves `ToneDefaults` untouched.
**Avoids:** Pitfalls 4, 10, 11, 12. The existing OutcomeSheet/UndoCenter tests must pass **unedited**.

### Phase 28: TalkBack sweep & Explorer demos
**Delivers:** `HubSchemeScope` at the Explorer root, deck + go-to-item + sheet-local host + `hide()`-then-navigate demos (EN/DE), demos for the new overloads (no guard forces them), TalkBack sweep on the TESTER, reduced-motion check. Restore device state afterwards (`wm density 450`, font 1.0, TalkBack off). Park undrivable checks for Yahir; never waive them.

### Phase 29: Docs & v2.8.0 cut readiness
**Delivers:** API/STYLE/INTEGRATION docs: `scope.launch` per emit (a serial await turns the deck back into a queue), the key-to-route router, one root host + an optional sheet host with a per-host cap, overlay insets, and the snackbar-dismiss vs row reveal-confirm swipe distinction. Gates rerun with `--rerun-tasks`. A proposal with `git log origin/main..HEAD`. No tag, no push.

### Research Flags

- **Needs discuss/research depth:** Phase 25 (event-shape and contract decisions below), Phase 26 (gesture disambiguation, fan-out sizing, traversal; highest novelty).
- **Standard patterns, skip extra research:** Phase 27 (patterns already specified in detail), 28 (known device ritual), 29 (docs + gates).

## Confidence Assessment

| Area | Confidence | Notes |
|------|------------|-------|
| Stack | HIGH | javap on the exact resolved AARs; no dependency change |
| Features | HIGH | Primary-source reads of Sonner, MDC and M3; reduced-motion behavior MEDIUM |
| Architecture | HIGH / MEDIUM | Integration points from live source; the state machine is unproven until 25 |
| Pitfalls | HIGH | Repo-grounded; the ModalBottomSheet window finding is LOW alone but corroborated by SB's `SheetFeedbackHost` |

**Overall confidence:** HIGH

### Gaps to Address (deduplicated open questions, by owning phase)

- **[25]** Second line ("item name") and tone dot have no source in D-09. Add `supportingText: String? = null` (and maybe a tone) to `Notify`/`WithUndo` now, or derive the tone from the variant? Must be settled before the event shape freezes.
- **[25]** Cancellation contract: remove the card and rethrow with no result (recommended), documented as "exactly one result, or cancellation".
- **[25/26]** Timer ownership and gating: timer in `show()` (testable, recommended) vs host. Does it start at enqueue or at first composition? Do timers run only while a host is attached and the lifecycle is STARTED? Does `show()` with no host suspend like M3?
- **[25]** A visible card whose entry is undone or evicted elsewhere: hide Undo + chevron and keep the card (recommended), or auto-dismiss? Does a losing snackbar Undo tap still resolve `Undone`?
- **[25]** Programmatic dismiss is absent from D-09: add `dismissAll()` / `dismiss(target)` resolving `Dismissed`? Additive; confirm with teamlead@vae-bilingual.
- **[25]** `FeedbackResult` shape: enum (a 5th value later is a source break) vs sealed.
- **[25]** Require a non-blank `openLabel` (validate in `FeedbackTarget` init?).
- **[25]** Clock seam choice and the a11y-manager handoff from host to state (internal setter vs per item).
- **[26]** Pause scope and floor: whole deck (recommended, matches the mock) and the resume floor value (mock 800 ms vs ~1 s).
- **[26]** Press-and-hold: suppress the open after a long-press timeout or movement past slop?
- **[26]** Swipe threshold: ~56dp positional or fling (mock 90px)?
- **[26]** Back-card height clamp to the front card when collapsed; fanned-out column overflow (scroll vs overlap) at landscape + font 2.0 + IME.
- **[26]** Peek hit area (~48dp strip) and whether a "Show all" a11y action is needed. Optional `expand`/`collapse` semantics on the deck.
- **[26/28]** Live region only on the entering card (avoid burst re-announce). A possible duplicate "Dismiss" (custom action + `dismiss`) in TalkBack: raise it with the designer, don't silently drop one.
- **[26/28]** Edge-swipe vs system back gesture on the TESTER; reduced-motion ("Remove animations") behavior.
- **[26/28/29]** Sheet-local host: confirm with SecondBrain that the sheet gets its own `FeedbackStackState` (per-host cap, cancelled on sheet close).
- **[27/28]** Opening from a modal OutcomeSheet: does the library `hide()` before `onOpenTarget`, or does the caller do it? Recommendation: the library only calls back; KDoc and the demo show hide-then-navigate.
- **[27]** Defensive chevron hiding on batch rows whose own undo state is Undone (when the caller forgot to clear the target).
- **[27]** POL-02 fallback form when the dot fails 3:1: contrast ring vs darker tone. Proven against a synthetic dynamic scheme, since the Explorer pin hides the failure.
- **[29]** STYLE.md wording that reveal-confirm swipe applies to destructive rows only.

## Sources

### Primary (HIGH confidence)
- Compose BOM 2026.04.01 POM; javap of ui/foundation 1.11.0, material3 1.4.0 (`SnackbarHostState`, `toMillis`, `FadeInFadeOutWithScale` semantics, `SwipeToDismissBox`), ui-test 1.11.0, coroutines-test 1.11.0
- Live repo: `feedback/*.kt`, `OutcomeSheet*.kt`, `ReceiptRow.kt`, `UndoCenterScreen.kt`, `ComponentRegistry`, Registry/Vocabulary/CatalogDocs drift guards, `api.txt`, detekt configs, RETROSPECTIVE, STYLE.md
- Sonner `main`, MDC-Android Snackbar/BaseTransientBottomBar/SwipeDismissBehavior, androidx-main `SnackbarHost`/`Scaffold`/`SwipeToDismissBox`
- `v2.8-BRIEF.md` (D-01..D-17), the design mock `docs/design/snackbar-go-to-item/index.html`, SecondBrain `SNACKBAR-NAV-SURVEY.md`

### Secondary (MEDIUM confidence)
- developer.android.com traversal / swipe-to-dismiss docs (Context7); reduced-motion write-ups (Eevis Panula)

### Tertiary (LOW confidence)
- SO 78525894 and zenn.dev on snackbars behind ModalBottomSheet (corroborated by the consumer precedent)

---
*Research completed: 2026-10-17*
*Ready for roadmap: yes*
