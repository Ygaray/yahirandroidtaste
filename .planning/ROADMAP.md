# Roadmap: yahirandroidtaste — Hub Stewardship

## Milestones

- ✅ **v1.0 — Hub Stewardship** — Phases 1-5 (shipped 2026-09-02, library `v2.0.0`)
- ✅ **v2.0 — Line Reunification** — Phases 6-9 (shipped 2026-09-27, library `v2.2.0`)
- ✅ **v2.4 — AI-Voice Command UI** — Phases 10-14 (shipped 2026-10-01, library `v2.4.0`)
- ✅ **v2.5 — Voice UI Localization & Accessibility** — Phases 15-19 (shipped 2026-10-11, library `v2.5.0`; the G-15-1 long-label fix shipped as patch `v2.5.1` on 2026-10-11)
- ✅ **v2.6 — Receipt voice surfaces + ProviderKeyCard lifecycle** — Phases 20-24 (shipped 2026-10-14, library `v2.6.0`; follow-up `v2.7.0` on 2026-10-13 and patch `v2.7.1` on 2026-10-14)
- 🚧 **v2.8 — Snackbar go-to-item + stacked feedback** — Phases 25-29 (in progress, ships library `v2.8.0`)

## Phases

### 🚧 v2.8 — Snackbar go-to-item + stacked feedback (In Progress)

**Milestone Goal:** Ship library `v2.8.0`, additive only. Snackbars and the success/outcome sheet take
the user straight to the item they reference (tap the body, no action label), and snackbars stack up
to 3 as a Receipt-card deck with swipe-to-dismiss. Ride-alongs: a scoped TalkBack sweep (backlog
999.2), Explorer demo polish (backlog 999.3) and two handled-by pill fixes. Decisions D-01..D-12 are
locked (Yahir, 2026-10-17, via director@office): `.planning/research/v2.8-BRIEF.md`. Design source:
`docs/design/snackbar-go-to-item/index.html`. Client: teamlead@vae-bilingual. Baseline: `v2.7.1`
(tag commit `f60bfc4`). Phase numbering continues from v2.6 (which ended at Phase 24).

**Rules for every phase:**

- **Additive, source-compatible only.** Every new param or field is defaulted and appended: data-class
  fields after the last existing one (`WithUndo.target` goes after `preview`), composable params after
  `modifier` or before a trailing lambda, never after one. `UndoHistoryEntry` keeps its internal
  ctor. No new `@Deprecated(HIDDEN)` overloads, hand-written old-arity `copy` or `copy$default`
  shims. A phase that changes a public signature regenerates `api.txt` in the same commit and keeps
  `./gradlew apiCheck` green.
- **Existing paths keep working.** `SnackbarHostState.emitTrackedWithUndo`, the M3 snackbar hosts and
  default `OutcomeSheet` / `UndoCenterScreen` calls behave and render exactly as `v2.7.1` (same code
  path; Robolectric state assertions). No Roborazzi.
- **The library never navigates.** `FeedbackTarget.key` is opaque and consumer-encoded; the library
  only hands it to the caller's `onOpenTarget`. No routes, `NavController` or consumer nouns in the
  hub (D-08, one-way dependency).
- **Strings come from the caller.** Labels have English defaults and are caller-localizable. Anything
  that renders a caller label gets a long German/Spanish ~360dp and font-scale 2.0 check before layout
  sign-off.
- **Register in the same phase.** A phase that adds a public top-level `@Composable` in `component/`,
  `feedback/`, `modifier/` or `theme/` registers it in its family screen, or allowlists it in
  `INTENTIONALLY_UNREGISTERED` with a reason, in that phase. Detekt stays at zero baseline.
- **No tags, no pushes.** `v2.8.0` and every push are Yahir's. No GSD milestone marker tag
  (`git.create_tag` false); tags here are JitPack coordinates.

- [x] **Phase 25: Feedback target model & stack state** - `FeedbackTarget` on events and Undo history, `FeedbackStackState` + result contract, tracked undo through the store, the `FeedbackStackHost` entry point (D-08, D-09) (completed 2026-10-17)
- [x] **Phase 26: Receipt-card stacked snackbar deck** - Direction A deck (max 3), tap-to-open with chevron, fan-out, swipe dismiss, timeouts with pause, merged-button semantics (D-01..D-07, D-11) (completed 2026-10-17)
- [x] **Phase 27: Open from the sheet & Undo Center, pill fixes** - Label-less go-to-item rows in Receipt and Classic `OutcomeSheet` and in the Undo Center; handled-by pill at font scale 2.0 and a contrast-safe Grammar dot (D-09, D-10) (completed 2026-10-17)
- [ ] **Phase 28: TalkBack sweep & Explorer demos** - Scoped TalkBack sweep on the TESTER (999.2), pinned hub scheme + `hide()` demo (999.3), stacked-snackbar and go-to-item demos in English and German
- [ ] **Phase 29: Docs & v2.8.0 cut readiness** - API/STYLE/INTEGRATION docs, additive `apiCheck`, full gates, long-string and Gate-1 device checks, then a `v2.8.0` proposal to Yahir (no tag cut by the agent)

## Phase Details

### Phase 25: Feedback target model & stack state

**Goal**: A caller can tag any snackbar event with an opaque `FeedbackTarget`, push events through a `FeedbackStackState` whose `show()` resolves to exactly one result, and track undoable ones in the Undo Center through the store's one guard, while every existing `FeedbackEvent` and `SnackbarHostState.emitTrackedWithUndo` call behaves as in `v2.7.1`.
**Depends on**: Phase 24 (milestone v2.6; library `v2.7.1` baseline, tag commit `f60bfc4`)
**Requirements**: FTGT-01, FTGT-02, FSTK-01, FSTK-07
**Success Criteria** (what must be TRUE):

  1. A caller can pass `target = FeedbackTarget(key, openLabel)` to `FeedbackEvent.Notify` and `FeedbackEvent.WithUndo`. `FeedbackTarget` compares by value and holds no lambda, and every `v2.7.1` `Notify` / `WithUndo` call shape (positional and named) compiles unchanged in a call-site canary (FTGT-01).
  2. A `WithUndo` with a target that is tracked through either `SnackbarHostState.emitTrackedWithUndo` or the new `FeedbackStackState.emitTrackedWithUndo` lands in `UndoHistoryStore` carrying that same target. Once the entry is Undone, it no longer exposes an openable target (unit tests) (FTGT-02).
  3. `rememberFeedbackStackState(maxVisible = 3)` keeps its state across recomposition, and each `show(event)` suspends until it resolves to exactly one of `Opened`, `Undone`, `Dismissed` or `TimedOut`, never two and never none (unit tests on virtual time; the state needs no composition) (FSTK-01).
  4. `FeedbackStackState.emitTrackedWithUndo(store, event)` appends the entry before the snackbar is visible, so it is in the Undo Center the whole time the snackbar is up. An `Undone` result goes through `store.attemptUndo(entryId)` exactly once, never `event.undoAction` directly, and a racing Undo Center tap on the same entry resolves through the same first-consumer-wins guard. `Opened`, `Dismissed` and `TimedOut` never undo (FSTK-07).
  5. `FeedbackStackHost(state, onOpenTarget, modifier, undoLabel = "Undo", dismissLabel = "Dismiss")` exists with the D-09 signature: a body tap on a targeted item calls `onOpenTarget(target)` and resolves `Opened`, Undo resolves `Undone`, and the Undo text is the caller's `undoLabel` (a German label renders as passed). The host is registered in the Feedback family screen (`rememberFeedbackStackState` registered or allowlisted), and the registry integrity test and the CATALOG drift guard are green (FSTK-01, FSTK-07).

**Notes**: Boundary with Phase 26: this phase owns the target model, the state, the result contract, the tracked-undo path and the host's public signature (a tracer host that renders the visible items newest-first with body tap and Undo wired; no deck motion, gestures, pause or semantics polish). The cap and timer logic may live in `FeedbackStackState`, but FSTK-02 and FSTK-06 are accepted in Phase 26. The host and `rememberFeedbackStackState` are public `@Composable`s in `feedback/`, so the drift guard forces their registration (or allowlisting) here. Open for discuss-phase: what `show()` does with `FeedbackEvent.DismissScreen` (message only, as today's dispatcher, or rejected).
**Plans:** 5/5 plans complete

Plans:
**Wave 1**

- [x] 25-01-PLAN.md — `FeedbackTarget` + `Notify`/`WithUndo` `target` and `supportingText`, V271 call-site canary, `api.txt` (FTGT-01)

**Wave 2** *(blocked on Wave 1 completion)*

- [x] 25-02-PLAN.md — `UndoHistoryEntry.target` / `openableTarget`, store `append` overload, Material3 tracked path forwards the target (FTGT-02)

**Wave 3** *(blocked on Wave 2 completion)*

- [x] 25-03-PLAN.md — `FeedbackResult` + composition-free `FeedbackStackState` (exactly-one result, cap, timers, cancel, `dismissAll`, accessibility binding) with virtual-time tests (FSTK-01)

**Wave 4** *(blocked on Wave 3 completion)*

- [x] 25-04-PLAN.md — `FeedbackStackState.emitTrackedWithUndo` (append before show, undo only via the store guard, NonCancellable) with race and cancel tests (FSTK-07, FTGT-02)

**Wave 5** *(blocked on Wave 4 completion)*

- [x] 25-05-PLAN.md — `rememberFeedbackStackState`, `FeedbackStackHost` tracer, registry/allowlist/vocabulary/docs counts (67/6/73), full phase gate (FSTK-01, FSTK-07)

### Phase 26: Receipt-card stacked snackbar deck

**Goal**: Up to three snackbars sit on screen as a Receipt-card deck that the user can fan out, swipe away one at a time and tap to jump to the item they mention, with today's timeouts that pause while touched, and TalkBack reads each card as one button.
**Depends on**: Phase 25 (`FeedbackTarget`, `FeedbackStackState`, `FeedbackStackHost` signature)
**Requirements**: FSTK-02, FSTK-03, FSTK-04, FSTK-05, FSTK-06, A11Y-01
**Success Criteria** (what must be TRUE):

  1. Up to 3 snackbars show as Receipt cards (`surfaceContainerHighest`, 16dp corners, tone dot, message plus an optional second line, Undo as a `primary` text action), with older cards tucked behind the newest (~14dp peek, scale 0.94 / 0.88). A 4th `show()` dismisses the oldest early with `Dismissed`, and if that was a tracked `WithUndo`, its entry stays in the Undo Center. A long German message and second line at 360dp and font scale 2.0 wrap inside the card without pushing Undo or the chevron off it (FSTK-02).
  2. Tapping the body of a card that has a target calls `onOpenTarget(target)` once and resolves `Opened`; tapping a card with no target does nothing. A trailing › chevron icon shows only on cards with a target, and no card shows an action label (FSTK-03).
  3. Tapping the peek of a collapsed deck fans it out into a column where every card's body and Undo can be tapped, and the deck re-collapses after idle. A horizontal swipe on any one card removes exactly that card with `Dismissed`; nothing is undone and the other cards stay (FSTK-04, FSTK-05).
  4. Each card times out at today's values, Short 4 s and Long 10 s passed through `AccessibilityManager.calculateRecommendedTimeoutMillis` (controls flagged when the card has Undo or a target). Its timer stops while a pointer is down on it or while the deck is fanned out, and resumes the remaining time on release or collapse (virtual-clock tests) (FSTK-06).
  5. In the semantics tree each card body is one merged node with `Role.Button`, `onClickLabel = openLabel` and a Polite live region. Undo is a separate focusable child and also a custom action. A Dismiss custom action (labeled `dismissLabel`) and `dismiss` semantics remove the card the way a swipe does. Deck cards stay in the tree newest-first, collapsed or fanned out (A11Y-01).

**Notes**: Any new public composable is registered or allowlisted here; `apiCheck` stays green on the regenerated `api.txt`. The TalkBack device confirmation of these semantics happens in Phase 28's sweep.
**Plans:** 6/6 plans complete

Plans:
**Wave 1**

- [x] 26-01-PLAN.md — pause-aware card timer on an injectable clock (deck-wide pause, 800 ms floor) and the Wave 0 Compose probes (FSTK-06)

**Wave 2** *(blocked on Wave 1 completion)*

- [x] 26-02-PLAN.md — collapsed and fanned Receipt-card deck: metrics, FeedbackDeck, FeedbackCard with capped Undo, 60 percent fan cap, DE and ES long-label tests (FSTK-02, FSTK-03)

**Wave 3** *(blocked on Wave 2 completion)*

- [x] 26-03-PLAN.md — single-owner tap gesture, peek-tap fan, long-hold suppression, deck-wide pointer pause observer, idle collapse and cleanup (FSTK-03, FSTK-04, FSTK-06)

**Wave 4** *(blocked on Wave 3 completion)*

- [x] 26-04-PLAN.md — swipe-to-dismiss with seeded anchors, Dismissed only, source-contract crash guard (FSTK-05)

**Wave 5** *(blocked on Wave 4 completion)*

- [x] 26-05-PLAN.md — A11Y-01 merged-button semantics, Explorer burst demo buttons, API.md row, full phase gate and Gate-1 inputs (A11Y-01, FSTK-02)
- [x] 26-06-PLAN.md — gap closure: a peek tap over a collapsed back card's Undo fans the deck instead of running Undo (FSTK-04)

**UI hint**: yes

### Phase 27: Open from the sheet & Undo Center, pill fixes

**Goal**: From a success sheet or the Undo Center, the user taps the row that names an item to jump to it (a chevron, no label), in both the Receipt and Classic `OutcomeSheet` styles, and the handled-by pill stays readable at large font sizes and under dynamic color.
**Depends on**: Phase 25 (`FeedbackTarget`, `UndoHistoryEntry.target`); runs after Phase 26 because every phase rewrites the shared `api.txt`
**Requirements**: FOPEN-01, FOPEN-02, FOPEN-03, POL-01, POL-02
**Success Criteria** (what must be TRUE):

  1. With `VoiceOutcomeUiState.Success.target` set and `onOpenTarget` passed to either `OutcomeSheet` overload (including the hoisted-`sheetState` one), the receipt row that names the item shows a chevron and calls `onOpenTarget(target)` on tap, in both Receipt and Classic, with no action label. With no target or no `onOpenTarget`, the sheet renders exactly as `v2.7.1` (same code path; Robolectric state assertions) and the existing `OutcomeSheet` tests pass unedited (FOPEN-01).
  2. In a batch success, each `BatchRowResultUiModel` with a `target` opens its own item; rows without one show no chevron, aren't clickable and expose no click action in the semantics tree (FOPEN-02).
  3. Through a new `UndoCenterScreen` overload with `onOpenTarget`, an Available entry with a target opens its item on tap; entries that aren't Available, or have no target, don't. The existing `UndoCenterScreen` call renders exactly as `v2.7.1`, and its trailing-lambda call sites still compile (FOPEN-03).
  4. At font scale 2.0, a long handled-by pill that wraps to several lines keeps all its text inside its shape (corner radius capped once it is multi-line, or equivalent); at font scale 1.0 a one-line pill looks as in `v2.7.1` (POL-01).
  5. The Grammar (warn, on-device) handled-by dot meets 3:1 non-text contrast against the pill container under the hub light and dark schemes and under a dynamic-color scheme sample (POL-02).

**Notes**: Open for discuss-phase (D-10): how a sheet row drops its target after Undo / Undo all. The library can derive it from the item's undo row state, or the caller can clear `target`; the brief only fixes the Undo Center side (FTGT-02). Also confirm which `Success` receipt row "names the item". The target rows get the long German ~360dp check (the chevron must not push the value off). The pill fixes apply to Receipt; Classic's `HandledByRow` stays untouched unless the same defect reproduces there.
**Plans:** 4/4 plans complete

Plans:
**Wave 1**

- [x] 27-01-PLAN.md — `Success.target`/`targetLabel`/`targetDetail`, shared internal open-target row, `onOpenTarget` on both `OutcomeSheet` overloads, Receipt and Classic single-target rows (Classic through new functions inside the single scaffold) (FOPEN-01)

**Wave 2** *(blocked on Wave 1 completion)*

- [x] 27-02-PLAN.md — `BatchRowResultUiModel.target`, per-row open list in Receipt and Classic, target-less rows inert with no reserved space (FOPEN-02)

**Wave 3** *(blocked on Wave 2 completion)*

- [x] 27-03-PLAN.md — second `UndoCenterScreen` overload with a required `onOpenTarget`, merged `Role.Button` open row, hold-to-peek kept, stale-callback guard, deliberate contract count edit (FOPEN-03)

**Wave 4** *(blocked on Wave 3 completion)*

- [x] 27-04-PLAN.md — capped-radius handled-by pill shape, contrast ring for the warn dot, phase hygiene contract, fresh full gate and Gate-1 inputs (POL-01, POL-02)

**UI hint**: yes

### Phase 28: TalkBack sweep & Explorer demos

**Goal**: The new snackbars, the outcome sheet and the Undo Center pass a scoped TalkBack sweep on the TESTER, and the Explorer shows every v2.8 surface in English and German inside the pinned hub color scheme.
**Depends on**: Phase 26, Phase 27
**Requirements**: A11Y-02, DEMO-02, DEMO-03, POL-03
**Success Criteria** (what must be TRUE):

  1. With TalkBack on the TESTER: a clickable `ReceiptRow` reads as one button with its expanded/collapsed state and Undo is a separate stop; the refused-undo "Couldn't undo" Assertive live region is spoken when it appears; the saved-key summary reads as one item with Update and Clear as separate buttons; the Undo Center group header is announced as a heading; and the new sheet target rows and snackbar cards announce their open label (A11Y-02).
  2. Every Explorer screen renders in the pinned hub color scheme, so the warn tone is never mauve under dynamic color anywhere, and the hoisted `OutcomeSheet` `sheetState` demo closes its sheet through `hide()` (DEMO-02).
  3. The Explorer has a stacked-snackbar demo (fire one, a burst of 4 past the cap, swipe one away, tap to open with the opened key shown) and go-to-item demos (Success single, batch with mixed target and no-target rows, Undo Center with target rows), each in English and German and each reached by a registry render test (DEMO-03).
  4. Every new public composable from Phases 25-27 is registered in `ComponentRegistry` or allowlisted with a reason, and the registry integrity test and the CATALOG drift guard are green (DEMO-03).
  5. The caller's user agent reaches the wire (POL-03).
     - A Robolectric test shows `additionalHttpRequestProperties["User-Agent"]` equals the resolved UA after a first and a second `configureOsmdroid` call, with a pre-seeded prefs value overwritten and only one entry.
     - A blank-derived or `com.example` UA logs one `Log.w`.
     - Placeholder rows (6987 B plus the pinned md5) are purged, and real tiles are kept.
     - On the TESTER, an Explorer picker with a real-id UA caches tiles whose size and md5 are not the placeholder (wire-level check).
     - The host `curl -A` probe with the exact resolved strings supports this check.

**Notes**: Defects the sweep finds are fixed in this phase. Already in the code, so verify rather than rebuild: the hoisted-`sheetState` demo that calls `hide()` (`VoiceCommandFamilyScreen.kt`, added in `v2.7.0` `5206be9`, fixed in `v2.7.1` `a5b29eb`), and `heading()` on the Undo Center group label (`UndoGroupHeaderRow`). `HubSchemeScope` (`ReleaseDemos.kt`) wraps only the Receipt and tone demos today. Device work uses the TESTER (`R5CT10XNKQN`) only; after UAT, restore its density with `wm density 450`. If TalkBack can't be driven agentically, park those checks for Yahir; never waive them. Screencaps are kept for the owner.
**Plans:** 7 plans

Plans:
**Wave 1**

- [x] 28-01-PLAN.md — POL-03 part 1: caller user agent injected into `additionalHttpRequestProperties["User-Agent"]` after every `load()`, honest fallback agent, one `Log.w` for a blocked agent, `PlaceMapPicker` KDoc ask, hub-owned Explorer agent, host probe script (POL-03)

**Wave 2** *(blocked on Wave 1 completion)*

- [ ] 28-02-PLAN.md — POL-03 part 2: placeholder-tile purge (6987 B plus md5, once per install and per process, never a wipe), real placeholder fixture, offline tile-cache check tool (POL-03)

**Wave 3** *(blocked on Wave 2 completion)*

- [ ] 28-03-PLAN.md — `ExplorerTheme` helper pins the hub scheme on every Explorer screen, the Activity and the not-found fallback, plus a source-scan guard (DEMO-02, D-01)

**Wave 4** *(blocked on Wave 3 completion)*

- [ ] 28-04-PLAN.md — stacked-snackbar stage (fire, burst past the cap, swipe, tap to open) and Undo Center target rows, English and German, with registry render tests (DEMO-03, D-02)

**Wave 5** *(blocked on Wave 4 completion)*

- [ ] 28-05-PLAN.md — sheet go-to-item demos (Success single, mixed batch; Receipt and Classic; English and German), hoisted `sheetState` `hide()` demo test, registry/CATALOG verification (DEMO-03, DEMO-02)

**Wave 6** *(blocked on Wave 5 completion)*

- [ ] 28-06-PLAN.md — Layer-1 TalkBack sweep assertions, Undo Center group-header heading test, openable Undo Center row as one merged button, locked forms pinned (A11Y-02, D-03)

**Wave 7** *(blocked on Wave 6 completion)*

- [ ] 28-07-PLAN.md — phase hygiene contract, read-only TESTER restore oracle, fresh full gate, and the Gate-1 contract (TESTER sweep, wire-level POL-03 check, DEMO-03 on device, restore, parking list) (A11Y-02, DEMO-02, DEMO-03, POL-03)

**UI hint**: yes

### Phase 29: Docs & v2.8.0 cut readiness

**Goal**: The v2.8 API is documented and every release gate is green, so Yahir gets a ready-to-cut `v2.8.0` proposal. The phase ends at the proposal; the agent cuts no tag and pushes nothing.
**Depends on**: Phase 25, Phase 26, Phase 27, Phase 28
**Requirements**: SHIP-05
**Success Criteria** (what must be TRUE):

  1. `API.md`, `STYLE.md` and, where they need it, `INTEGRATION.md` and the catalog docs describe `FeedbackTarget`, `FeedbackStackState` / `rememberFeedbackStackState` / `FeedbackStackHost`, the result type, both `emitTrackedWithUndo` paths and `onOpenTarget` on `OutcomeSheet` and `UndoCenterScreen`, including how one app-level `onOpenTarget` maps keys to routes. Doc counts match the registry.
  2. On a freshly regenerated `api.txt`, `./gradlew apiCheck` shows additions only against `v2.7.1`, and a `v2.7.1` call-site canary compiles unchanged. `./gradlew testDebugUnitTest`, `bash tools/test/run-all.sh` and `./gradlew detekt` (zero baseline) pass on a forced fresh run, not an up-to-date reuse.
  3. Long-string checks pass on the snackbar card, the sheet target rows and the handled-by pill in German and Spanish at ~360dp and at font scale 2.0, and the Gate-1 device checks pass on the TESTER, with evidence kept for the owner.
  4. A `v2.8.0` proposal (commit sha, the `git log origin/main..HEAD` range, gate results) reaches Yahir through the teamlead. The agent creates no tag (neither `v2.8.0` nor a milestone marker), pushes nothing and repins no consumer from the hub.

**Notes**: A tag push publishes every unpushed commit reachable from it, so the proposal lists the full unpushed range. Version, tag and sequencing confirmations route through the teamlead/orchestrator first.
**Plans**: TBD

## Completed Milestones

<details>
<summary>✅ v1.0 — Hub Stewardship (Phases 1-5) — SHIPPED 2026-09-02, library <code>v2.0.0</code></summary>

The hub went from a flat, additive-only catalog to a legible, audited, governed two-tier design
system — Tier Legibility → Coherence Audit → Governance Gates → Repin Bookkeeping → Gardening (unify
shipped; the coordinated consumer repin, GARD-02, was deferred human-gated and was **absorbed into
v2.0's Phase 9** onto `v2.2.0`). Full detail:
[`.planning/milestones/v1.0-ROADMAP.md`](milestones/v1.0-ROADMAP.md).

</details>

<details>
<summary>✅ v2.0 — Line Reunification (Phases 6-9) — SHIPPED 2026-09-27, library <code>v2.2.0</code></summary>

Collapsed the divergent v1.x (SecondBrain `v1.13.0`) and v2.x/`main` (CalTracker `v2.1.0`) release
lines into one forward line on `main`, cut as library `v2.2.0`, so every consumer converges on one
tag and the v1.x line can retire — while keeping every v2.x improvement. Also landed two additive
riders (per-tag chip color, MicButton hardening), completed v1.0's deferred **GARD-02** coordinated
repin (onto `v2.2.0`), and cleared tech-debt **W-1**.

- [x] Phase 6: Forward-port reunification (3/3 plans) — DateTimePicker, PlaceMap* cluster, PresetChip forward-ported net-additively; osmdroid admitted; both drift guards green — completed 2026-09-27
- [x] Phase 7: Chip-color slot (1/1 plan) — opt-in backward-compatible per-tag chip container-color override — completed 2026-09-27
- [x] Phase 8: MicButton hardening (1/1 plan) — consumer-agnostic microcopy, latest-callback safety, hub-vocabulary KDoc — completed 2026-09-27
- [x] Phase 9: Ship & coordinated repin (2/2 plans) — cut library `v2.2.0` (human-gated), reconciled ECOSYSTEM.md matrix, surfaced consumer repins, closed KI-2026-09-02-01 — completed 2026-09-27

**Gate-2:** Phases 6, 7, 8 human-signed-off (Yahir, 2026-09-27); Phase 9 had no deferred device
checkpoint (doc/config ship phase). Audit: 10/10 requirements satisfied, cross-phase integration
CLEAN. Full detail: [`.planning/milestones/v2.0-ROADMAP.md`](milestones/v2.0-ROADMAP.md).

**Deferred (human-gated, cross-repo convention):** consumer repin execution — SecondBrain
(`v1.13.0→v2.2.0` + FilterBar→ChipBar migration) and CalTracker (`v2.1.0→v2.2.0`) — runs in each
consumer's own channel; repin paths surfaced in `ECOSYSTEM.md`.

</details>

<details>
<summary>✅ v2.4 — AI-Voice Command UI (Phases 10-14) — SHIPPED 2026-10-01, library <code>v2.4.0</code></summary>

Shipped the shared AI-voice UI layer in the hub — generic, prop-driven presentational composables
(`ProviderKeyCard`, `ModelSelectCard`, `ApproachLadderCard`, `OutcomeSheet` with a "handled by:
tier/approach" indicator + loud failure states + a generic undo affordance + a domain-neutral
needs-confirmation state, and `ClarificationBar` tap-to-clarify choices) as the tenth "Voice Command"
`ComponentRegistry` family, all strictly additive versus `v2.3.0` and engine-free (no OkHttp, no
`voice-action-engine` dependency — one-way-dependency invariant, INV-01). Cut as library `v2.4.0`.
Wave 0, §6.3 of the frozen `vae-bilingual` cross-repo contract.

- [x] Phase 10: Voice command settings surfaces (2/2 plans) — ProviderKeyCard, ModelSelectCard, ApproachLadderCard — completed 2026-09-30
- [x] Phase 11: Voice outcome & failure sheet (2/2 plans) — OutcomeSheet (handled-by + loud failure + generic undo) + ClarificationBar — completed 2026-09-30
- [x] Phase 12: Generic needs-confirmation state (1/1 plan) — additive NeedsConfirmation sealed arm (single + batch) — completed 2026-09-30
- [x] Phase 13: Catalog integrity & docs (1/1 plan, cuts NO tag) — registry registration + CATALOG-03 drift guard + additive API + doc-drift fix — completed 2026-09-30
- [x] Phase 14: Cut v2.4.0 (1/1 plan) — cut + push + JitPack-confirm `v2.4.0`, §11 ledger row relayed — completed 2026-10-01

**Gate-2:** Phases 10-13 human-signed-off (Yahir, 2026-10-01, PASS via orchestrator); Phase 14 had no
deferred device checkpoint (release-tooling phase). Audit: 16/16 requirements satisfied, cross-phase
integration CLEAN, Nyquist compliant. Closed via override_closeout (phase verifications read
mtime-`stale` after the downstream tag/doc commits — the known mtime artifact; real state verified by
certify `all_pass` + the full suite green at the tagged commit). Full detail:
[`.planning/milestones/v2.4-ROADMAP.md`](milestones/v2.4-ROADMAP.md).

**Deferred (future polish, Gate-2-waived):** two non-blocking `ApproachLadderCard` UI-polish notes
(combined-subdued-label right-edge crowding; light-theme capped-rung dimming) — see
`KNOWN-ISSUES.md` KI-2026-10-01-01. Consumer repins (SecondBrain `v2.3.0`→, CalTracker `v2.1.0`→
`v2.4.0`) are Wave-1, in each consumer's own channel.

</details>

<details>
<summary>✅ v2.5 — Voice UI Localization & Accessibility (Phases 15-19) — SHIPPED 2026-10-11, library <code>v2.5.0</code></summary>

Made the AI-voice UI surface fully caller-localizable and more accessible, strictly additively.
This is Wave 0, §6.3 of the `vae-bilingual` effort, with scope locked to R-v1.1 `cb5f047`.

- [x] Phase 15: Voice-surface i18n label params (4/4 plans) — caller-overridable English-default labels on the settings cards and voice-surface composables (VI18N-01..04). Gate-2 reopened it with G-15-1 (long German labels crammed the rung rows); 15-04 plus the code-review fixes closed it with the private `RungLabelLayout` — completed 2026-10-11
- [x] Phase 16: A11y + Failure enrichment (3/3 plans) — minimum interactive size and selected-radio semantics on rung rows; `Failure` gets an action role, a body slot and a semantics prefix (VA11Y-01, VFAIL-01..03) — completed 2026-10-05
- [x] Phase 17: ApproachLadderCard Router ON/OFF toggle (1/1 plan) — `require()`-paired `router`/`onRouterChange` (VAPPR-04) — completed 2026-10-05
- [x] Phase 18: Catalog integrity + API dump + docs (2/2 plans) — registry and drift guard green, additive API, docs, invariants (CAT-02, API-02, DOC-02, INV-02) — completed 2026-10-05
- [x] Phase 19: Cut v2.5.0 (2/2 plans) — immutable `v2.5.0` tag via §11, ledger relayed, no stray marker tag (SHIP-03) — completed 2026-10-05

**Gate-2:** all five phases are human-signed-off; Phase 15 was re-verified 17/17 on the personal
device on 2026-10-11. Audit: 14/14 requirements, integration WIRED, status `tech_debt`. Full detail:
[`.planning/milestones/v2.5-ROADMAP.md`](milestones/v2.5-ROADMAP.md).

**Follow-up (human-gated):** the G-15-1 fix landed after the immutable `v2.5.0` tag. It ships as
`v2.5.1` (layout-only, `api.txt` unchanged), plus SecondBrain/CalTracker repins, routed through the
orchestrator.

</details>

<details>
<summary>✅ v2.6 — Receipt voice surfaces + ProviderKeyCard lifecycle (Phases 20-24) — SHIPPED 2026-10-14, library <code>v2.6.0</code></summary>

Shipped an additive, opt-in "Receipt" rendering of the voice outcome sheet, the atoms it needs, and the
full key lifecycle on `ProviderKeyCard`. Every default reproduces the v2.5.1 pixels. Decision record:
`.planning/seams/v2.6.0-SKETCH.md` (`105255d`).

- [x] Phase 20: Tone foundation & atoms (4/4 plans) — tone tokens, button emphasis + `RoundIconButton`, `StackedProgressBar`, `ToneCard`, `PillChip` — completed 2026-10-12
- [x] Phase 21: Receipt row, Pill selector & sheet shape (4/4 plans) — `ReceiptRow`, Pill style + `disabledOptions`, `SheetScaffold` shape — completed 2026-10-12
- [x] Phase 22: OutcomeSheet Receipt style (5/5 plans) — Receipt NeedsConfirmation/Success/Failure, stable test tags, in-flight action, XR-178-01 a/b — completed 2026-10-12
- [x] Phase 23: ProviderKeyCard lifecycle & Undo Center groups (2/2 plans) — Save/Update/Clear/Cancel with a fingerprint-only summary; Undo Center group rows — completed 2026-10-12
- [x] Phase 24: Catalog, docs, Explorer demos & cut v2.6.0 (4/4 plans) — registry/API/docs gate, Explorer demos + 33 TESTER screencaps, `v2.6.0` cut — completed 2026-10-12

**Gate-2 (Yahir, 2026-10-14):** all five phases are signed off with a gap. The gap is that no TalkBack
checks were run; a hub-wide TalkBack sweep (999.2) covers them. Yahir signed off the Y-15 screencaps with
no v2.6.1. Certify passed with 0 gaps. The audit is `tech_debt`: 27/27 requirements, integration WIRED.
Full detail: [`.planning/milestones/v2.6-ROADMAP.md`](milestones/v2.6-ROADMAP.md).

**Follow-ups:** `v2.7.0` (2026-10-13) added CalTracker 76-14's optional `NeedsConfirmation.handledBy` and
the hoistable `OutcomeSheet` `sheetState` overload on top of v2.6.0. Patch `v2.7.1` (2026-10-14, tag
commit `f60bfc4`, Gate-1 4/4 on the TESTER) made tall outcome sheets open fully expanded so the pinned
action bar shows, made the Undo Center group count and status words caller-localizable, and padded the
count-editor demo. `v2.7.1` is the current release and the v2.8 baseline.

</details>

## Backlog

### Phase 999.1: Formalize reusable Gate-2 visualization harness APK (BACKLOG)

**Goal:** Promote the throwaway same-package-Intent harness — which every Gate-1 agent currently re-derives from scratch — into a committed, launchable Gate-2 visualization app for this library-only repo.

**Requirements:** TBD

**Plans:** 0 plans

Context:

- `yahirandroidtaste` is a pure `com.android.library` (no `applicationId`), so it ships **no installable APK**. Human Gate-2 on-device review therefore has nothing to open. Every Gate-1 self-UAT run rebuilds the same throwaway harness (a 1-Activity app that depends on the mavenLocal AAR and `startActivity(Intent(this, ExplorerActivity::class.java))`) just to see the gallery — documented in `01-05-SELF-UAT.md`'s "Driver-mechanism note" and re-derived by later verify sessions too (incl. v2.4's Gate-2 prep).
- Deliverables to scope when promoted: (a) a committed harness — a dedicated app module or a gradle task that assembles an installable debug APK opening `ExplorerActivity`; (b) a project-local `AGENT-DEVICE-TESTING.md` documenting the same-package-harness driver pattern (the SELF-UAT logs explicitly recommend authoring one so future Gate-1 runs don't re-derive it).
- **Invariant guard:** harness → library only, never the reverse (one-way dependency). The harness is host/consumer-side tooling; it must name no library-internal concepts and must not become something the library depends on.

Plans:

- [ ] TBD (promote with /gsd-review-backlog when ready)

### Phase 999.2: Hub-wide TalkBack sweep (PROMOTED into v2.8 → Phase 28, A11Y-02)

**Promoted 2026-10-17** into milestone v2.8 as requirement A11Y-02 (Phase 28), scoped to the new
snackbars, the outcome sheet and the Undo Center. Kept here as history.

**Goal:** One TalkBack pass over the hub's voice surfaces, covering the v2.6 Gate-2 gaps. That pass checks
that:

- a clickable ReceiptRow reads as one button item with its expanded/collapsed state, and Undo is a
  separate stop;
- the refused-undo "Couldn't undo" Assertive live region is spoken on appearance;
- the saved-key summary reads as one item, with Update and Clear as separate buttons;
- the Undo Center group header is announced as a heading.

Source: Yahir, v2.6 Gate-2 2026-10-14.

**Plans:** 0 plans (tracked in Phase 28)

### Phase 999.3: Explorer demo polish (PROMOTED into v2.8 → Phase 28, DEMO-02)

**Promoted 2026-10-17** into milestone v2.8 as requirement DEMO-02 (Phase 28). Kept here as history.

**Goal:** Two Explorer demo changes:

- Pin the hub color scheme across the whole Explorer. Today only the Receipt and count-editor demos
  pin it, and the warn tone renders mauve under dynamic color. This is a demo-only change.
- Add a demo of the hoisted `OutcomeSheet` `sheetState` that calls `hide()`.

Source: Yahir, v2.6 Gate-2 and certify advisory 2.

**Plans:** 0 plans (tracked in Phase 28)

### Phase 999.4: Voice-surface polish (BACKLOG, target v2.8.1)

**Goal:** Fix these from the v2.6 Gate-2 list:

- (a) Add a small gap between the last body element and the pinned-bar divider. This is cosmetic.
- (b) A saved `ProviderKeyCard` summary with `actions = null` has no way out.
- (c) A long group label is cut at 2 lines next to a long German Undo all.
- (d) The key field stays typeable while `inFlight`.
- (e) The group header count shows members present, not members still undoable.
- (f) The count-editor demo's warn card sits flush on the bottom nav bar (no bottom spacing; v2.7.1 Gate-1 advisory).
- (g) The German group subtitle "3 Einträge · Rückgängig möglich" already fills its 2 allowed lines at 384/360dp; a longer status word may clip (v2.7.1 Gate-1 advisory).

Source: Yahir, v2.6 Gate-2 2026-10-14. Deferred out of v2.8 to patch `v2.8.1` (v2.8 brief).

**Plans:** 0 plans

## Progress

| Milestone | Phases | Plans | Status | Shipped |
|-----------|--------|-------|--------|---------|
| v1.0 | 1-5 | — | Complete | 2026-09-02 |
| v2.0 | 6-9 | 7/7 | Complete | 2026-09-27 |
| v2.4 | 10-14 | — | Complete | 2026-10-01 |
| v2.5 | 15-19 | 12/12 | Complete | 2026-10-11 |
| v2.6 | 20-24 | 19/19 | Complete | 2026-10-14 |
| v2.8 | 25-29 | 0/TBD | In progress | - |

**v2.8 execution order:** Phases execute in numeric order: 25 → 26 → 27 → 28 → 29. (Phase 27 depends
only on Phase 25, but it runs after 26 because every phase rewrites the shared `api.txt`.)

| Phase | Milestone | Plans Complete | Status | Completed |
|-------|-----------|----------------|--------|-----------|
| 25. Feedback target model & stack state | v2.8 | 5/5 | Not started |  |
| 26. Receipt-card stacked snackbar deck | v2.8 | 6/6 | Not started |  |
| 27. Open from the sheet & Undo Center, pill fixes | v2.8 | 4/4 | Not started |  |
| 28. TalkBack sweep & Explorer demos | v2.8 | 1/7 | Not started |  |
| 29. Docs & v2.8.0 cut readiness | v2.8 | 0/TBD | Not started | - |
