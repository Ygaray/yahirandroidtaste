---
phase: 26-receipt-card-stacked-snackbar-deck
verified: 2026-10-17T15:10:00Z
status: passed
score: 5/5 must-haves verified
covered_files:
  - ".planning/REQUIREMENTS.md#A11Y-01"
  - ".planning/REQUIREMENTS.md#FSTK-02"
  - ".planning/REQUIREMENTS.md#FSTK-03"
  - ".planning/REQUIREMENTS.md#FSTK-04"
  - ".planning/REQUIREMENTS.md#FSTK-05"
  - ".planning/REQUIREMENTS.md#FSTK-06"
  - ".planning/phases/26-receipt-card-stacked-snackbar-deck/26-01-PLAN.md"
  - ".planning/phases/26-receipt-card-stacked-snackbar-deck/26-01-SUMMARY.md"
  - ".planning/phases/26-receipt-card-stacked-snackbar-deck/26-02-PLAN.md"
  - ".planning/phases/26-receipt-card-stacked-snackbar-deck/26-02-SUMMARY.md"
  - ".planning/phases/26-receipt-card-stacked-snackbar-deck/26-03-PLAN.md"
  - ".planning/phases/26-receipt-card-stacked-snackbar-deck/26-03-SUMMARY.md"
  - ".planning/phases/26-receipt-card-stacked-snackbar-deck/26-04-PLAN.md"
  - ".planning/phases/26-receipt-card-stacked-snackbar-deck/26-04-SUMMARY.md"
  - ".planning/phases/26-receipt-card-stacked-snackbar-deck/26-05-PLAN.md"
  - ".planning/phases/26-receipt-card-stacked-snackbar-deck/26-05-SUMMARY.md"
  - ".planning/phases/26-receipt-card-stacked-snackbar-deck/26-06-PLAN.md"
  - ".planning/phases/26-receipt-card-stacked-snackbar-deck/26-06-SUMMARY.md"
  - "API.md"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/explorer/FeedbackFamilyScreen.kt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackCard.kt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackDeck.kt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackDeckMetrics.kt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHost.kt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackState.kt"
covered_digest: "v1:sha256:ca7a431c52d383636d0f6c09a0dec64168b2705149d32ab7b427bd0871a6b881"
behavior_unverified: 0
overrides_applied: 0
re_verification:
  previous_status: passed
  previous_score: "5/5"
  gaps_closed:
    - "Gate-1 cycle-1 SC3 failure: a peek tap over a collapsed back card's Undo ran Undo instead of fanning (closed by plan 26-06, CollapsedBackTouchBlocker)"
  gaps_remaining: []
  regressions: []
---

# Phase 26: Receipt-card stacked snackbar deck Verification Report

**Phase Goal:** Up to three snackbars sit on screen as a Receipt-card deck that the user can fan out, swipe away one at a time and tap to jump to the item they mention, with today's timeouts that pause while touched, and TalkBack reads each card as one button.
**Verified:** 2026-10-17
**Status:** passed
**Re-verification:** Yes, after gap-closure plan 26-06 and two code-review fix passes. The earlier report (written before 26-06) said passed; that verdict missed a real-device defect, which Gate-1 then caught. This pass re-derives every truth from the ROADMAP wording, with extra scrutiny on the 26-06 change and on SC3.

## Goal Achievement

### Observable Truths

| # | Truth (ROADMAP success criterion) | Status | Evidence |
|---|---|---|---|
| 1 | Up to 3 Receipt cards (surfaceContainerHighest, 16dp, tone dot, message + second line, Undo as primary text action), older cards tucked ~14dp at 0.94/0.88; 4th show() dismisses oldest as `Dismissed` and a tracked entry stays in the Undo Center; long German text at 360dp / font 2.0 wraps inside the card (FSTK-02) | VERIFIED | `FeedbackCard.kt:239-241` Surface uses `RoundedCornerShape(Dimens.CornerRadius.Card)`, `surfaceContainerHighest`, `Dimens.Elevation.Level2`; 8dp dot (`:287`, `:314-319`); message plus optional `supportingText` Column (`:320-333`); Undo is `DynamicActionButton(Neutral, Text)` (`:336-342`); custom row `Layout` caps Undo and keeps dot and chevron fixed. `FeedbackDeckMetrics.kt` (peek 14dp, depth scales 1.0/0.94/0.88) and `FeedbackStackState` cap eviction as `Dismissed`. Unit coverage: `FeedbackDeckMetricsTest`, `FeedbackDeckLayoutTest`, `FeedbackStackHostLongLabelTest`, eviction tests (all in the green 1613). Device (Gate-1 re-run, addendum): Burst4 gives `B1:Dismissed`, 3 cards, strips 13.9 dp, widths 0.940/0.880; tracked eviction gives `UI:Dismissed`, entry Available, `UNDORUNS=0`; German card at 360dp / font 2.0 holds message, second line, Undo and chevron inside. The 103dp body column at font 2.0 is a recorded UX note (owner call), not a criterion miss. |
| 2 | Body tap on a targeted card calls `onOpenTarget` once and resolves `Opened`; untargeted body tap does nothing; chevron only with a target; no action label (FSTK-03) | VERIFIED | `FeedbackCard.kt:196-210` single `detectTapGestures` owner: collapsed back card routes to `onPeekTap`, otherwise `latestOpenTarget?.let(latestBodyTap)`; `openTarget` is null when no target or spent. Chevron only when `showChevron = openable` (`:247`). Host resolves first and calls `onOpenTarget` only for the resolving tap (`FeedbackStackHost`). Device re-run: fanned Target tap gives `open tg n=1`, `TG:Opened`; double tap on the Undo-with-target card opens once; untargeted front tap changes nothing; chevrons only on targeted cards, no "Open ..." text. |
| 3 | Tapping the peek of a collapsed deck fans it so every card's body and Undo are tappable; deck re-collapses after idle; a horizontal swipe removes exactly that card as `Dismissed`, nothing undone, others stay (FSTK-04, FSTK-05) | VERIFIED (cycle-1 defect fixed) | Peek tap: `FeedbackCard.kt:200-208` tap owner calls `latestPeekTap()` for `collapsedBack`; `FeedbackStackHost:121-122` computes `collapsedBack = !fanned && index > 0` and `onPeekTap` sets `fanned`. **26-06 fix, confirmed in the source:** `FeedbackCard.kt:252-254` `if (collapsedBack) CollapsedBackTouchBlocker(Modifier.matchParentSize())` after the `Surface`, and `:274-277` `Box(modifier.pointerInput(Unit) { })`. It is a sibling above the content (last in the root `Box`, so first in hit-test order), has no semantics and consumes nothing, so the Undo button never gets the touch while every ancestor (the single tap owner, slop recorder, draggable, deck-wide pause observer) still sees an ordinary unconsumed tap. It leaves the tree when the deck fans or the card is the front card. Idle collapse: host `LaunchedEffect(state, fanned, interactionTick, pointerDown)` (`:91-94`). Swipe: `AnchoredDraggableState` seeded with the Settled anchor, anchors set in `onSizeChanged`, settle off Settled calls only `onDismiss` (`:137-140`), offset NaN-guarded; drag `enabled = !collapsedBack \|\| displaced` is unchanged. Tests I confirmed exist and are in fresh green results (`TEST-...FeedbackStackHostDeckTest.xml` tests=37, failures=0; written 14:29Z, after the last `src` commit `9a4b4ba` at 07:48 local): `peekTapOverTheBackCardsUndo_fansTheDeck_andNeverUndoes` (clicks the Undo's own bounds; the helper `undoPointInTheStrip` asserts the Undo overlaps the strip by more than 2dp, so it cannot pass vacuously), `threeCardDeck_peekTapOverTheMiddle/DeepestCardsUndo_...`, `afterTheFan_theBackCardsUndo_respondsToTouchAgain`, `collapsedFrontCardsUndo_stillRespondsToTouch`, `cardPushedToTheBackMidDrag_stillDismisses_...`, `freshDragOnADisplacedSettlingBackCard_dismissesThroughTheBlocker` (bites if the blocker consumes), `peekTapOnABackCardWithoutAnUndo_...`, `peekTapOnASpentBackCard_...`, plus the earlier swipe, idle-collapse and fan tests. The two weak tests (`..._control`) are honestly labelled controls. Device (uat_run `uatr-20261017T140846Z-f3f20544`, 43 signed captures, AAR confirmed to contain `CollapsedBackTouchBlocker`): the exact cycle-1 tap (430,1985) now fans with `UNDORUNS=0`; an 8-point sweep including the band above the Undo's bounds and its left/right edges all fan; 3-card deck with an Undo on every card fans from each sliver; Burst4 back card with Undo plus target fans; after the fan, Undo on the oldest card still runs (`UI:Undone`) and a collapsed front card's Undo still runs; swipes dismiss exactly one card as `Dismissed` with no Undo. |
| 4 | Timeouts at today's values (Short 4 s, Long 10 s through `calculateRecommendedTimeoutMillis`, controls flagged for Undo/target); timer stops while a pointer is down or the deck is fanned and resumes the remaining time (virtual-clock tests) (FSTK-06) | VERIFIED | `FeedbackStackState` timer on an injectable `timeSource`, deck-wide pause reasons (pointer, fanned), 800 ms floor; host wires `setPointerDown` (Initial-pass non-consuming observer in `FeedbackDeck.kt:121-130`), `setFanned` (`FeedbackStackHost:88`) and `resetPause` on dispose (`:101`). `FeedbackStackPauseTest` 18, `FeedbackStackStateTimeoutTest` and the host pause tests are green (0 failures in fresh XMLs). The 26-06 blocker is an Initial-pass-neutral child, so the pause observer still sees touches on a collapsed back card (reviewed in 26-REVIEW-2, re-observed on device). Device: plain Short 4025 ms, Long 10010 ms, held finger gives `TimedOut` after 10007 ms, fanned pair timed out at 14.3 s rather than 4 s, recommended-timeout scaling 7.02 s plain and 12.02 s for target or Undo cards. |
| 5 | Each card body is one merged node with `Role.Button`, `onClickLabel = openLabel`, a Polite live region; Undo a separate focusable child and a custom action; Dismiss custom action (`dismissLabel`) and `dismiss` semantics remove the card like a swipe; deck cards stay newest-first, collapsed or fanned (A11Y-01) | VERIFIED | `FeedbackCard.kt:147-179` root `semantics(mergeDescendants = true)`: `liveRegion = Polite`, `traversalIndex = index`, `dismiss(label = dismissLabel)`, `role = Button` plus `onClick(label = openLabel)` when openable, `customActions` = Undo (if undoable and not spent) then Dismiss. `FeedbackDeck.kt:113` `isTraversalGroup`, `:205` per-slot `traversalIndex`. The blocker adds no semantics (KDoc and code: bare `Box` with `pointerInput`), and the SC5 tree on device shows the collapsed back card's actions identical to cycle 1, Undo still a separate focusable child, and the custom Undo action on a collapsed back card runs `B2=Undone` with no fan. `FeedbackStackHostSemanticsTest` is 19 tests, 0 failures, including the three new `collapsedBackCard_*` guards. TalkBack itself is Phase 28 (A11Y-02), not counted here. |

**Score:** 5/5 truths verified (0 present, behavior-unverified).

### Deferred Items

None are needed to pass. Items already scheduled elsewhere (not gaps in this phase):

| # | Item | Addressed In | Evidence |
|---|------|-------------|----------|
| 1 | TalkBack device sweep of the snackbar cards (open label, reading order, whether TalkBack lists both Dismiss forms, the oldest card's Undo child reading outside its card) | Phase 28 | ROADMAP Phase 28 SC1: "new sheet target rows and snackbar cards announce their open label (A11Y-02)"; ROADMAP Phase 26 Notes assign TalkBack confirmation to Phase 28 |

### Required Artifacts

| Artifact | Expected | Status | Details |
|---|---|---|---|
| `feedback/FeedbackCard.kt` (407 lines) | Receipt card, tap owner, swipe, semantics, `CollapsedBackTouchBlocker` | VERIFIED | Substantive and used by `FeedbackStackHost`; blocker is private and called under `if (collapsedBack)` |
| `feedback/FeedbackDeck.kt` (263) | Deck layout, cap, scroll, pointer observer | VERIFIED | Wired from the host |
| `feedback/FeedbackDeckMetrics.kt` (121) | Pure geometry | VERIFIED | Pinned by metrics tests |
| `feedback/FeedbackStackHost.kt` (126) | Host wiring: fan, idle, pause, `collapsedBack` | VERIFIED | Public signature unchanged |
| `feedback/FeedbackStackState.kt` (384) | Pause-aware timer, injectable clock | VERIFIED | Internal-only additions |
| `explorer/FeedbackFamilyScreen.kt`, `API.md` | Burst demo, API row | VERIFIED | Registered; drift guards green inside the 1613 |
| `FeedbackStackHostDeckTest.kt` (37 `@Test`), `FeedbackStackHostSemanticsTest.kt` (19), `FeedbackStackTestSupport.kt` | 26-06 regression coverage | VERIFIED | Present, 0 failures, no `@Ignore` in the feedback test package |

### Key Link Verification

| From | To | Via | Status |
|---|---|---|---|
| `FeedbackStackHost` | `FeedbackCard` | `collapsedBack = !fanned && index > 0`, `onPeekTap` sets `fanned` | WIRED |
| `FeedbackCard` root `Box` | `CollapsedBackTouchBlocker` | conditional last child, `matchParentSize()` | WIRED |
| Card tap owner | `latestPeekTap` / `latestBodyTap` | `detectTapGestures` (the only one; source-contract test still green) | WIRED |
| Deck pointer observer | `state.setPointerDown` | Initial pass, non-consuming | WIRED |
| Host `fanned` | `state.setFanned`, `resetPause` on dispose | `LaunchedEffect`, `DisposableEffect` | WIRED |
| Card tap, Undo, swipe, semantics actions | `state.resolve(item, Opened/Undone/Dismissed)` | resolve-first callbacks | WIRED |

### Data-Flow Trace (Level 4)

| Artifact | Data | Source | Real data | Status |
|---|---|---|---|---|
| `FeedbackDeck` items | `state.items` | `FeedbackStackState.enqueue` fed by `show` | Yes (device run pushes real events through the public API from a consumer harness) | FLOWING |
| Card message, second line, target, Undo | `FeedbackStackItem` mapped from `FeedbackEvent` | `FeedbackStackItem.from` | Yes | FLOWING |

### Behavioral Spot-Checks

| Behavior | Evidence | Status |
|---|---|---|
| Feedback tests | Fresh result XMLs (14:29Z): HostDeck 37, HostSemantics 19, SourceContract 15, Host 12, Pause 18, all 0 failures/errors | PASS |
| Whole suite totals from the result XMLs on disk | 1613 tests, 22 skipped, 0 failures, 0 errors (matches the orchestrator's fresh gate: detekt, apiCheck green, `tools/test/run-all.sh` PASS=52) | PASS |
| `api.txt` untouched by Phase 26 | `git log -- api.txt` last commit is `fad787e` (25-05); no phase-26 commit touches it; source-additive check OK per orchestrator | PASS |
| Gate-1 on the real tester | `uat_run uatr-20261017T140846Z-f3f20544`, `all_pass`, 5/5 criteria, signed run | PASS |
| Gradle re-run by verifier | Not run (shared host, instructed); orchestrator evidence plus on-disk result XMLs relied on | SKIPPED |

### Probe Execution

Step 7c: SKIPPED. No `probe-*.sh` is declared by any Phase 26 plan; the Wave 0 "probes" are JVM tests inside the green suite.

### Requirements Coverage

Requirement IDs claimed across PLAN frontmatter: 26-01 `[FSTK-06]`, 26-02 `[FSTK-02, FSTK-03]`, 26-03 `[FSTK-03, FSTK-04, FSTK-06]`, 26-04 `[FSTK-05]`, 26-05 `[A11Y-01, FSTK-02]`, 26-06 `[FSTK-04]`. Union is exactly the six IDs the ROADMAP assigns to Phase 26.

| Requirement | Source Plans | Description (REQUIREMENTS.md) | Status | Evidence |
|---|---|---|---|---|
| FSTK-02 | 26-02, 26-05 | Up to 3 Receipt-card deck, 4th arrival dismisses the oldest | SATISFIED | Truth 1 |
| FSTK-03 | 26-02, 26-03 | Body tap opens + `Opened`; untargeted inert; chevron only with target; no action label | SATISFIED | Truth 2 |
| FSTK-04 | 26-03, 26-06 | Peek tap fans every snackbar reachable; re-collapses after idle | SATISFIED | Truth 3 (26-06 closed the Undo-in-strip hole) |
| FSTK-05 | 26-04 | Horizontal swipe dismisses exactly one (`Dismissed`), never undoes | SATISFIED | Truth 3 |
| FSTK-06 | 26-01, 26-03 | Today's timeouts; pause while pointer down or fanned; resume | SATISFIED | Truth 4 |
| A11Y-01 | 26-05 | Merged button node, polite region, Undo child + action, Dismiss action/semantics, newest-first | SATISFIED | Truth 5 |

No orphaned requirements: REQUIREMENTS.md maps only these six IDs to Phase 26 (rows at lines 68-73). Bookkeeping note: the six checkboxes (`[ ]`, lines 17-21 and 32) and traceability rows still read Pending. That is the phase-complete step's job, not an implementation gap.

### Anti-Patterns Found

None blocking. A `TBD|FIXME|XXX|TODO|HACK` scan over all five feedback sources and the three test files touched by 26-06 returns no match. Both review passes are fully resolved: `26-REVIEW.md` 8/8 fixed (`26-REVIEW-FIX.md`), `26-REVIEW-2.md` 5/5 fixed (`26-REVIEW-2-FIX.md`, commits `d638d53`, `ace65c9`, `1e3a48a`, `e515e65`, `9a4b4ba`). Stub check on the blocker: an empty `pointerInput(Unit) { }` is intentional and documented (it exists only to take the hit), not a stub.

### Human Verification Required

None under the stated rule. Residual points, all owned elsewhere and none a Phase 26 gap:

- The real touch-slop widening around the Undo can only be felt with a finger. Gate-1 probed points from 4 px below the strip's top edge to 1 px above the front card and 11 px outside the Undo's left and right edges, and the blocker is card-wide, so it does not depend on that geometry. Robolectric does not widen the hit region, which is why two tests are honestly labelled controls.
- Gate-2 (owner UAT) is registered in `.planning/uat-pending/26-receipt-card-stacked-snackbar-deck.md`.
- Forwarded notes (not failures): the oldest card's Undo child reading order and the TalkBack Dismiss-form question (Phase 28), keyboard opening while fanned showing the oldest card in an `imePadding` host, the 103dp body column for long German at font 2.0, and ghosting of back-card text through the strips at 0.9/0.75 alpha.

### Gaps Summary

No gaps. The one real defect in this phase (Gate-1 cycle 1, SC3: a peek tap over a collapsed back card's Undo ran Undo and could silently undo an item) is closed in the source by the semantics-free, non-consuming `CollapsedBackTouchBlocker`, guarded by tests that click the Undo's own bounds, and re-observed on the tester with the exact failing coordinates. The fix does not regress SC1, SC2, SC4 or SC5: the blocker adds no semantics, consumes nothing, appears only for a collapsed back card, and leaves layout, the single-tap-owner rule, the pause observer, swipes and the public API (`api.txt` unchanged) untouched. All six requirement IDs are satisfied and accounted for.

---

_Verified: 2026-10-17_
_Verifier: Claude (gsd-verifier)_
