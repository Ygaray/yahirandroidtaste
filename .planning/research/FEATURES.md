# Feature Research: v2.8 Snackbar go-to-item + stacked feedback

**Domain:** stacked, tap-to-open transient feedback (snackbar deck) plus go-to-item rows on the outcome sheet and in the Undo Center, inside a reusable Compose design library
**Researched:** 2026-10-17
**Confidence:** HIGH for reference behaviors (read from upstream source: Sonner `main`, MDC-Android `Snackbar`/`BaseTransientBottomBar`/`SwipeDismissBehavior`, Compose M3 `SnackbarHost`/`Scaffold`/`SwipeToDismissBox` on androidx-main). MEDIUM for reduced motion (Compose honoring the animator duration scale comes from accessibility-engineer write-ups, not docs).

> **Scope.** D-01..D-17 are locked (`v2.8-BRIEF.md`). This file doesn't reopen them. It lists what comparable systems do, so the plans don't miss a behavior the locked design leaves implicit. The most useful part for the roadmap is **"Gaps the brief leaves implicit"**: each gap comes with a recommended default and the phase that owns it.

---

## How the reference systems behave

| Behavior | Sonner (web) | MDC Snackbar (Views) | Compose M3 `SnackbarHost` | iOS / Android notifications | **v2.8 (locked)** |
|---|---|---|---|---|---|
| Concurrency | Up to 3 visible (`VISIBLE_TOASTS_AMOUNT=3`); extras are **hidden, not dropped**, and their timers keep running | One at a time; a new one dismisses the current one with `DISMISS_EVENT_CONSECUTIVE` | One at a time; a `Mutex` queue holds the rest | Grouped stack, unbounded | Up to 3; a 4th **evicts** the oldest as `Dismissed` (D-06) |
| Collapsed look | Back toasts scale `1-0.05*i`, lift `gap=14px`, and **take the front toast's height** | n/a | n/a | Stack shows the edges of up to 2 cards behind | Peek ~14dp, scale 0.94/0.88 (D-01) |
| Expand | Hover or Alt+T; collapses on leave (not while interacting) or Escape; auto-collapses at ≤1 toast | n/a | n/a | Tap the stack to expand; "Show less" collapses | Tap the peek to fan out, re-collapse after idle (D-01). The design mock uses **4 s** idle |
| Timer | 4 s default. **All** timers pause while expanded, while a pointer is down on the list, and while the document is hidden. Remaining time resumes | Short/Long go through `getRecommendedTimeoutMillis` (CONTROLS flag when there's an action); pauses on ACTION_DOWN in bounds and while dragging; restores on UP/CANCEL | `delay()` inside the **host's** `LaunchedEffect` through `calculateRecommendedTimeoutMillis`; never pauses. With no host attached, `showSnackbar` suspends forever | n/a (persistent) | 4 s/10 s through a11y (D-04). Pauses while a pointer is down or the deck is fanned out (D-05). The mock pauses **all** cards and floors the remaining time at 800 ms |
| Swipe | 45px or velocity > 0.11 px/ms, in allowed directions only | START_TO_END only, 50% of width, alpha fades with the swipe | None built in; `SwipeToDismissBox` default threshold is 56dp | Swipe dismisses one notification | Horizontal swipe dismisses one card, never undoes (D-07). The mock uses 90px |
| Tap body | No body action (action and cancel buttons only) | Body inert; one action slot | Body inert; one action plus an optional dismiss icon | **Tap body opens the item** | Tap body opens; chevron hint; no label (D-02/D-03) |
| Caller cancels | n/a | n/a | `finally { currentSnackbarData = null }`: card removed, no result | n/a | **Unspecified** (see G-2) |
| A11y | `aria-live=polite` on the list; hotkey focus | Longer timeout under touch exploration | `liveRegion=Polite` (visible only), `dismiss{}` action, `paneTitle` | System handled | Merged `Role.Button`, Polite live region, Undo child plus custom action, Dismiss action, newest-first traversal (D-11) |
| Reduced motion | `prefers-reduced-motion` turns off all transitions | System animator scale | Compose animations honor animator scale 0 (Compose 1.2+) | System | **Unspecified** (see G-12) |
| Process death / reload | Gone | Gone | Gone (`remember`ed host state) | Persisted by the OS | Gone; the store is in-memory too |

Takeaway: v2.8 combines the **Sonner deck**, **notification-style tap-to-open** and **MDC's pause-on-touch**. It is stricter than Sonner on overflow: it evicts instead of hiding, which works because the Undo Center catches evicted undos. Every reference keeps the timer, the touch pause and the a11y timeout out of the caller's hands, so v2.8 should too.

---

## Table Stakes

Users get no credit for these, but they notice when one is missing.

### Stack model & result contract (Phase 25)

| Feature | Why Expected | Complexity | Notes |
|---|---|---|---|
| Newest card in front (bottom, nearest the thumb); older cards tuck up behind it | Sonner and iOS both work this way; the newest event is the one being read | LOW | Locked in D-01. Visual order is newest at the **bottom**, while semantics order is newest **first** (G-9) |
| Exactly one terminal result per `show()` | M3 `SnackbarResult` precedent; callers branch on it | MEDIUM | Use a single-resolution guard (CAS) so a racing body tap, timeout, eviction and swipe can't double-resolve. Double-tapping the body calls `onOpenTarget` **once** |
| Eviction at cap is a normal `Dismissed`, not an error | MDC `CONSECUTIVE` precedent | LOW | Locked in D-06. The evicted card animates out; it doesn't vanish |
| Defined behavior when the caller is cancelled | M3 removes the card and returns no result | LOW | See G-2. Without this, "exactly one result" is false on rotation |
| Programmatic dismissal (dismiss one, dismiss all) | M3 `SnackbarData.dismiss()`, Sonner `toast.dismiss(id)`; SB drops per-call snackbars after Undo-all (survey §1a) | LOW | **Not in the D-09 signature** (G-5). Additive method on `FeedbackStackState` |
| Tracked undo goes through the store only | Existing UNDO-02 invariant (`UndoTracking.kt`) | LOW | Locked as FSTK-07. Reuse the existing "append before visible" ordering |

### Timers (Phase 26, logic may live in 25)

| Feature | Why Expected | Complexity | Notes |
|---|---|---|---|
| Short/Long scaled by `calculateRecommendedTimeoutMillis`, with CONTROLS when the card has Undo or a target | MDC and M3 both do this; TalkBack "Time to take action" depends on it | LOW | Locked in D-04. A target counts as a control, because the body tap is an action |
| Pause on touch, resume the **remaining** time (not a restart) | MDC (ACTION_DOWN/drag) and Sonner both resume the remainder | MEDIUM | Locked in D-05. Scope and floor are open (G-1) |
| Independent per-card timers that run concurrently | Sonner: back cards were the front card earlier and have already been read | LOW | A back card can expire first; the deck re-stacks smoothly |
| Timers run on wall-clock `delay`, not the animation clock | With "Remove animations" (scale 0), anything driven by animation finishes instantly | LOW | See G-12. A countdown built on `Animatable` would make cards vanish at once for reduced-motion users |

### Deck interaction (Phase 26)

| Feature | Why Expected | Complexity | Notes |
|---|---|---|---|
| Tap slop between tap and swipe | Every swipe-dismiss system has one (the mock uses 4-6px) | LOW | A drag past touch slop never opens. Use `ViewConfiguration.touchSlop` |
| Swipe follows the finger, fades with distance, and flings out past a threshold **or** velocity | Sonner (distance or velocity), MDC (alpha fade) | MEDIUM | Recommend about the M3 default of 56dp positional, or a fling. Other cards hold still until the swiped card is removed, then re-stack |
| Swipe works in both directions with no reveal step | Dismissing isn't destructive; the undo stays in the Undo Center | LOW | Don't reuse the hub's reveal-confirm row swipe (left = delete, right = edit). That convention is for destructive row actions |
| Fanned-out column: every card's body, Undo and swipe can be reached | iOS expanded stack; D-01 | MEDIUM | Back cards' bodies must not be tappable while collapsed (a tap there fans out instead) |
| Auto-collapse after idle, and at ≤1 card | Sonner resets at ≤1; the mock waits 4 s idle | LOW | A new arrival while fanned out joins the column and keeps it fanned out. Timers resume on collapse |
| Collapsed back cards clamped to the front card's height | Sonner `--front-toast-height`; otherwise a tall German card behind a short one sticks out well past 14dp | LOW | See G-7 |

### Placement & z-order (Phase 26, docs in 29)

| Feature | Why Expected | Complexity | Notes |
|---|---|---|---|
| Sits above the nav bar, the FAB and the **IME** | M3 Scaffold handles the nav bar and FAB, but `ScaffoldDefaults.contentWindowInsets` (`systemBarsForVisualComponents`) **excludes the IME** | LOW | The host applies no insets of its own (in a Scaffold slot that would double-pad). KDoc shows `Modifier.windowInsetsPadding(WindowInsets.ime.union(navigationBars))` for overlay use. minSdk 35 means edge-to-edge always |
| Works inside a `ModalBottomSheet` | A modal sheet is its own window, so a root host renders **under** it (SB G9, today's `SheetFeedbackHost`) | MEDIUM | Give the sheet-local host its own `FeedbackStackState`. Already promised on the design page; needs a demo (Phase 28) |
| Width cap on wide screens | Sonner is fixed at 356px; M3 snackbars cap their width on large screens | LOW | Something like `widthIn(max = 560.dp)`, start/center aligned. Landscape matters too |
| Fanned-out column never runs off-screen | Landscape (~360dp tall) plus font scale 2.0 plus the IME can leave less room than 3 fanned cards need | MEDIUM | See G-8 |

### Accessibility (Phase 26, device check in 28)

| Feature | Why Expected | Complexity | Notes |
|---|---|---|---|
| Swipe has a non-gesture equivalent | WCAG 2.5.1; M3 host exposes `dismiss{}` | LOW | Locked in D-11 (Dismiss custom action plus `dismiss` semantics) |
| Announce on arrival, politely, once | M3 sets the live region only while visible | LOW | Evicted or exiting cards drop the live region so a 4-event burst doesn't re-announce (G-10) |
| Reading order that matches meaning | D-11 says newest first, but visually the newest is at the bottom | LOW | Set an explicit `traversalIndex` (the design page already names it); the default top-to-bottom order would read oldest first |
| Chevron is decorative | The `onClickLabel` already carries the meaning | LOW | `contentDescription = null`; use `Icons.AutoMirrored` so it flips in RTL |

### Go-to-item rows: sheet & Undo Center (Phase 27)

| Feature | Why Expected | Complexity | Notes |
|---|---|---|---|
| The whole row is the target, ≥48dp, with ripple, a trailing chevron and `Role.Button` with `onClickLabel` | M3 lists use a trailing arrow to mean navigation | LOW | Undo inside the row stays a separate node (a nested `clickable` keeps its own merge boundary) |
| A non-openable row looks and behaves exactly like v2.7.1 | Locked in D-10/D-15 | LOW | No chevron, no click action in semantics, same code path |
| The row stops being openable after Undo | SB survey: created items stop existing after Undo-all | LOW | Library-derived for the Undo Center and snackbars (D-14); caller-cleared for `Success` and batch rows. Spell that out in KDoc |
| Opening from a modal sheet doesn't land under the sheet | The sheet's window sits above the NavHost | LOW | The library only calls `onOpenTarget`. KDoc and the Explorer demo should show `sheetState.hide()` and then navigate (G-11) |

---

## Differentiators

| Feature | Value Proposition | Complexity | Notes |
|---|---|---|---|
| Dismissing never loses an undo | Swipe, eviction and timeout all leave the tracked entry in the Undo Center. Sonner and M3 lose the action once the toast is gone | LOW (already built) | Comes from the existing `UndoHistoryStore`. Document it as the reason eviction is safe |
| One go-to-item model across snackbars, outcome sheet (both styles) and Undo Center | The same chevron, the same `FeedbackTarget` and a single app-level `onOpenTarget` mean consumers write one router | MEDIUM | Opaque key: no NavController in the singleton, and it works in any harness |
| Pause-on-touch for the **whole** deck | Avoids the deck re-stacking under a finger in mid-swipe | LOW | Recommended reading of D-05 (G-1) |
| Live store sync on visible cards | Undoing from the Undo Center or the sheet's "Undo all" immediately removes Undo and the chevron from the matching visible card | MEDIUM | Collect `UndoHistoryStore.entries` (G-4). None of the references do this, because none has an Undo Center |
| Receipt-card look shared with the v2.7 sheets | Snackbars and sheets read as one system | MEDIUM | Locked in D-01 |
| Haptic tick when a swipe crosses the dismiss threshold | Tells the user the release will dismiss | LOW | Optional; can wait for v2.8.1 |

---

## Anti-Features

| Anti-Feature | Why Requested | Why Problematic | Alternative |
|---|---|---|---|
| "Open"/"View" text action next to Undo | Gmail/Drive "View" snackbars | Two actions crowd the card and wrap badly in German; M3 allows one action | Body tap plus chevron (D-02/D-03) |
| Combined "Undo & open" | Soft-deleted targets (SB G11) | Two outcomes from one tap; out of scope | D-12; a later consumer-side design |
| Swipe-to-undo, or a reveal-confirm swipe on cards | Matches the hub's list-row swipe convention | Dismissing isn't destructive, and accidental undos are | Swipe = dismiss only (D-07) |
| Sonner-style hidden overflow (keep more than 3, show 3) | Nothing is lost | Hidden timers expire unseen, and TalkBack reaches cards that aren't shown | Evict to Dismissed; the Undo Center keeps the undo (D-06) |
| One-at-a-time queue (M3 default) | Simpler | Bursts (voice batches) wait 10 s each, and the user loses the context | Deck of 3 |
| Deck consuming Back (collapse on back) | Feels tidy | A non-modal surface that steals Back breaks screen navigation and predictive back | Idle auto-collapse; Back never intercepted |
| Visible countdown bar or progress animation | "Shows remaining time" | Motion under reduced-motion, a11y noise, and it fights the touch pause | No countdown visuals |
| Defaulting cards with a target to Indefinite | "Can't miss it" | Cards pile up at the cap and evict each other | A11y-scaled timeouts (D-04) |
| Persisting the deck across process death or restoring it on rotation | Completeness | No reference does it; stale cards after a restart can't open anything sensible | Cancel-safe removal (G-2); the undo survives in the store for the process lifetime |
| Auto-coalescing identical messages | Bursts of "Card deleted" | Each tracked card has its own undo entry; merging would hide undos | Defer; a caller-chosen key could replace a card in place later |
| `onOpen` lambda on events or entries | Simplest call site | The singleton store would capture a NavController (leak) and it isn't harness-agnostic | `FeedbackTarget(key, openLabel)` (D-08) |
| The library navigating, or hiding the sheet on open by itself | Saves the caller a step | Violates "the library never navigates"; the caller may want to skip same-destination opens (SB G10) | `onOpenTarget` only; document hide-then-navigate |

---

## Gaps the brief leaves implicit (resolve in discuss/plan)

| # | Gap | Reference evidence | Recommended default | Phase |
|---|---|---|---|---|
| G-1 | Pause scope: D-05 says "a pointer down **on a snackbar**". Does that pause that card or every card? Is there a floor on the remaining time? | Sonner pauses all; the design mock pauses all (`items.forEach(pauseTimer)`) and floors at 800 ms; MDC restores exactly | Pause **all** cards when a pointer is down anywhere on the deck. Resume each card with `max(remaining, ~1 s)` | 26 |
| G-2 | What `show()` does when the caller is cancelled (rotation with remembered state, a disposed `LaunchedEffect`, a sheet host leaving composition) | M3: card removed, `CancellationException`, no result | Remove the card and rethrow cancellation, with **no** result (an exception to "exactly one", stated in KDoc). Tracked entries stay in the Undo Center. Virtual-time test | 25 |
| G-3 | Timers running while no host is attached, or while the app is in the background | M3 never times out without a host (timer lives in the host); Sonner pauses when hidden | The host marks itself attached; timers run only while a host is attached and its lifecycle is at least STARTED. `show()` with no host suspends like M3. The state stays testable without composition through an internal "attached" flag | 25/26 |
| G-4 | A visible card whose entry is undone elsewhere (Undo Center tap, the sheet's Undo all, group undo) or evicted from the store (`evictIfNeeded`) | None (unique to the hub) | Visible tracked cards observe `entries`. If the entry isn't Available, hide Undo and the chevron and leave the card up. A snackbar Undo tap that loses the race still resolves `Undone` (meaning the user chose Undo). `attemptUndo` returns `Unit`, so the KDoc must not promise success | 25 |
| G-5 | No programmatic dismiss in the D-09 API | M3 `dismiss()`, Sonner `toast.dismiss` | Additive `FeedbackStackState.dismissAll()` (and maybe `dismiss(target)`), resolving `Dismissed` | 25 |
| G-6 | Press-and-hold to read: pointer-down pauses the timer, so a long hold followed by release fires a body tap and opens the item | Notifications open on tap only; holding is a different gesture | Don't open if the press outlasted the long-press timeout or moved past touch slop. Releasing just resumes the timers | 26 |
| G-7 | A taller back card (long German message under a short new one) sticks out past the 14dp peek | Sonner clamps back cards to the front card's height | Clip or clamp collapsed back cards to the front card's measured height; restore their full height when fanned out | 26 |
| G-8 | Fanned-out column taller than the space left (landscape, font 2.0, IME up) | iOS expanded stacks scroll | Cap the fanned-out height to the space available. The newest card stays pinned at the bottom; the column scrolls, or the oldest overlaps. Test at 360x780 landscape with font 2.0 | 26 |
| G-9 | Newest-first semantics vs. newest-at-bottom visuals | Design page names `traversalIndex` | Explicit `traversalIndex` per card in a traversal group; Robolectric semantics-order test | 26 |
| G-10 | Live-region burst: 4 events in about 1 s | M3 sets the live region only while visible | Live region only on the entering card; cleared when it exits or is evicted; device check in the TalkBack sweep | 26/28 |
| G-11 | Opening from a modal `OutcomeSheet`: the sheet's window covers the destination | SB survey (queued sheets, R\* hidden hosts) | The library calls `onOpenTarget` only. KDoc and the Explorer demo show hide-then-navigate (pairs with the 999.3 `hide()` demo) | 27/28 |
| G-12 | Reduced motion ("Remove animations", animator scale 0) | Sonner turns off transitions; Compose 1.2+ animations jump to their end | Rely on Compose honoring the scale; timers on `delay`; swipe still dismisses when released past the threshold; fan-out is instant. Add one device check with Remove animations on | 26/28 |
| G-13 | Peek tap target: 14dp is far below 48dp | WCAG 2.5.8 / M3 minimum touch target | Extend the peek hit area, e.g. a 48dp strip covering the peek, with a `Role.Button` "Show all" only if TalkBack needs it (it shouldn't, since cards are flat in the tree) | 26 |

---

## Feature Dependencies

```
FeedbackTarget (25) ──> FeedbackEvent.Notify/WithUndo.target (25) ──> UndoHistoryEntry.target (25)
FeedbackStackState + result contract + cancel rule G-2 (25)
    ├──> tracer FeedbackStackHost (25) ──> deck look/gestures/timers/a11y (26)
    └──> emitTrackedWithUndo(store) (25) ──requires──> UndoHistoryStore.append/attemptUndo (existing)
Live store sync G-4 ──requires──> UndoHistoryStore.entries StateFlow (existing)
Undo Center go-to rows (27) ──requires──> UndoHistoryEntry.target (25) + UndoCenterScreen rows/UndoGroupHeaderRow (existing)
OutcomeSheet go-to rows (27) ──requires──> Success.target/BatchRowResultUiModel.target (25/27) + ReceiptRow, Classic body (existing)
Sheet-local host demo (28) ──requires──> 26 deck + Explorer HubSchemeScope (existing, widened by 999.3)
TalkBack sweep (28) ──requires──> 26 semantics + 27 rows
Docs/cut (29) ──requires──> all; api.txt rewritten by 25, 26, 27 in order
Pill fixes POL-01/02 (27) ── independent of the snackbar work (same phase for api.txt sequencing only)
```

Builds on existing pieces: `FeedbackEvent` (append fields after `preview`/`duration`), `SnackbarHostState.emitTrackedWithUndo` (must stay byte-identical in behavior), `UndoHistoryStore` (`append`, `attemptUndo`, `entries`, group undo, `evictIfNeeded`), `UndoStatus { Available, Undone, Failed }`, the `ReceiptRow`/`ToneCard`/tone-token look from v2.6, `SheetScaffold`, `ComponentRegistry` (Feedback family) and `HubSchemeScope`.

**Conflict to watch:** the hub-wide reveal-confirm swipe convention vs. D-07's immediate swipe-dismiss. Say in STYLE.md (Phase 29) that the reveal-confirm swipe applies to destructive **rows** only, and that snackbars dismiss straight away.

---

## Priority by phase

| Feature | User Value | Cost | Priority | Phase |
|---|---|---|---|---|
| Result contract + single-resolution guard + cancel rule (G-2) | HIGH | LOW | P1 | 25 |
| Host-attached/lifecycle timer gating (G-3) | HIGH | MEDIUM | P1 | 25/26 |
| Live store sync of visible cards (G-4) | HIGH | MEDIUM | P1 | 25 |
| `dismissAll()` (G-5) | MEDIUM | LOW | P2 (additive; ask the teamlead) | 25 |
| Deck visuals + back-card height clamp (G-7) | HIGH | MEDIUM | P1 | 26 |
| Tap vs. hold vs. swipe disambiguation (G-6, slop) | HIGH | LOW | P1 | 26 |
| Deck-wide pause + resume floor (G-1) | MEDIUM | LOW | P1 | 26 |
| Fanned-out height cap (G-8), peek hit area (G-13) | MEDIUM | MEDIUM | P1 | 26 |
| `traversalIndex` + live region on entry only (G-9/G-10) | HIGH | LOW | P1 | 26 |
| IME/inset KDoc + sheet-local host demo | MEDIUM | LOW | P1 | 26/28/29 |
| Hide-then-navigate guidance (G-11) | MEDIUM | LOW | P1 | 27/28 |
| Reduced-motion device check (G-12) | MEDIUM | LOW | P2 | 28 |
| Haptic tick on swipe threshold | LOW | LOW | P3 (v2.8.1) | — |
| Width cap on wide screens | LOW | LOW | P2 | 26 |

---

## Sources

- Sonner `src/index.tsx` and `src/styles.css`, `main` branch (constants, pause conditions, collapsed height, expand/collapse, swipe threshold and velocity, reduced-motion media query). HIGH: primary source.
- MDC-Android `Snackbar.getDuration`, `BaseTransientBottomBar` (touch and drag pause, `DISMISS_EVENT_CONSECUTIVE`), `SwipeDismissBehavior` (0.5 threshold, alpha), `master`. HIGH: primary source.
- androidx-main `compose/material3` `SnackbarHost.kt` (mutex queue, cancel cleanup, host-driven `delay`, a11y semantics), `Scaffold.kt` (snackbar offset, `systemBarsForVisualComponents`), `SwipeToDismissBox.kt` (56dp). HIGH: primary source; check that the BOM 2026.04.01 behavior matches.
- Eevis Panula, "Android, Animations and Reduced Motion" (2022) and the Inclusive Android Apps newsletter: Compose 1.2+ honors Remove animations. MEDIUM.
- `docs/design/snackbar-go-to-item/index.html` interactive mock (4 s idle collapse, pause-all, 800 ms floor, 90px swipe, 4-6px slop). Project source.
- SecondBrain `SNACKBAR-NAV-SURVEY.md` (G1-G12, hidden-behind-sheet hosts, Undo-all dropping snackbars, same-destination opens). Project source.

---
*Feature research for: v2.8 Snackbar go-to-item + stacked feedback*
*Researched: 2026-10-17*
