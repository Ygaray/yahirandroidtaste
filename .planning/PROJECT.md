# yahirandroidtaste — Hub Stewardship

## What This Is

`yahirandroidtaste` is Yahir's **personal reusable widget library**. Its job is to keep his agents
from coding, from scratch, widgets he already reuses across his own apps (SecondBrain, CalTracker,
and future ones). Those consumers are his and recompile from an immutable tag on every repin. There
are no outside consumers, and it is not a public release product. JitPack is just the pinning
mechanism. The long-term direction is a cross-platform rewrite (likely Kotlin Multiplatform /
Compose Multiplatform), not a public Android release.

This GSD project governs the library **as an artifact**: its coherence, structure and long-term
health. That is distinct from the feature/component work consumer apps drive into it. Consumers
remain the primary editors and grow the catalog additively through their own give-legs. This
project owns what that channel structurally can't do: pruning additive-duplicate accretion, keeping
the primitives/patterns tiering legible, and curating the design language.

## Core Value

An agent building one of Yahir's apps finds the widget it needs already here, coherent and legible,
and drops it in instead of writing a new one. If all else fails, the catalog must stay legible and
prunable, so reuse stays cheaper than rewriting.

**Compatibility bar:** source compatibility for Yahir's own consumers (they recompile on every
repin). Binary compatibility is not a goal (see API.md § "Compatibility rule").

## Current Milestone: v2.8 Snackbar go-to-item + stacked feedback

**Goal:** Snackbars and the success/outcome sheet take the user straight to the item they reference
(tap the body, no action label), and snackbars stack up to 3 as a Receipt-card deck with
swipe-to-dismiss; release `v2.8.0`, additive only.

**Target features:**
- `FeedbackTarget` + target on `FeedbackEvent` / outcome Success / batch rows / Undo history
- `FeedbackStackHost` (direction A deck, max 3, pause-on-touch, swipe dismiss, chevron, a11y)
- Outcome-sheet row tap to open (Receipt and Classic); Undo Center rows open their item
- Ride-alongs: TalkBack sweep (999.2, scoped), Explorer hub-scheme pin + `hide()` demo (999.3),
  long handled-by pill at font scale 2.0, Grammar pill dot contrast

Locked by Yahir 2026-10-17 (via director@office). Brief: `.planning/research/v2.8-BRIEF.md`.

## Current State

**Shipped: milestone v2.6 — Receipt voice surfaces + ProviderKeyCard lifecycle (2026-10-14) → library
`v2.6.0`.**

- **Receipt outcome sheet.** `OutcomeSheet(style = Receipt)` is an opt-in rendering of
  NeedsConfirmation, Success and Failure, with stable test tags, a pinned action bar, an in-flight
  action, and XR-178-01 a/b. Classic stays pixel-identical to v2.5.1.
- **Atoms.** The tone tokens, the button emphasis axis, `RoundIconButton`, `StackedProgressBar`,
  `ToneCard`, `PillChip`, `ReceiptRow`, the Pill selector with `disabledOptions`, and `SheetScaffold`
  shape.
- **Key lifecycle.** `ProviderKeyCard` gains Save/Update/Clear/Cancel with a fingerprint-only saved
  summary. `UndoCenterScreen` gains group rows.
- **Status.** Source compatible. Requirements: 27/27. Gate-2 is signed off with a gap on all five phases
  (Yahir): the TalkBack checks went to a hub-wide sweep in the backlog.

**`v2.7.0` shipped (2026-10-13):** CalTracker 76-14 asked for two additions, the optional
`NeedsConfirmation.handledBy` (a Receipt confirm pill) and a hoistable `OutcomeSheet` `sheetState`
overload. The `handledBy` field moved `NeedsConfirmation`'s positional `componentN`; Yahir accepted
that break.

**Next:** patch `v2.7.1` (Yahir approved it 2026-10-14; it starts on the teamlead's GO). It has three
fixes:
- tall outcome sheets skip the partial detent;
- the Undo Center group count and status words become localizable;
- the count-editor demo gets padding.

The backlog is ROADMAP 999.2-999.4.

**Prior milestones:** v1.0 — Hub Stewardship (2026-09-02, `v2.0.0`); v2.0 — Line Reunification
(2026-09-27, `v2.2.0`, + `TAGCOLOR-02` follow-on `v2.3.0`); v2.4 — AI-Voice Command UI (2026-10-01,
`v2.4.0`, + `v2.4.1` OutcomeSheet hotfix); v2.5 — Voice UI Localization & Accessibility (2026-10-11,
`v2.5.0`, + `v2.5.1`). See `.planning/MILESTONES.md`.

## Context

- Extracted from SecondBrain; now a two-consumer ecosystem (SB pins `v1.10.0`, CalTracker pins
  `v1.5.0`). 16 tags `v1.0.0→v1.10.0`, 9 registered families, Metalava `apiCheck` freeze-gate,
  zero-baseline detekt, ComponentRegistry drift guard.
- Sessions on 2026-08-21 diagnosed the spine: the hub is a **latent two-tier design system**
  (CalTracker-style *primitives* + SecondBrain-style opinionated *patterns*) whose taxonomy,
  litmus, and governance still treat it as flat. CalTracker v1.7 built the *primitives* half
  (DS-03) but not the governance half.
- Additive-only + consumer-driven ⇒ the hub can only accrete, never prune; unification is always a
  breaking change, so it never happens in the consumer channel. **This project is the sanctioned
  home for the breaking "gardening" work, batched with coordinated consumer repins.**
- **Phase 1 (Tier Legibility) complete** (2026-09-01): the latent two-tier structure is now
  legible — not just diagnosed. All 53 registered components carry an explicit `Tier`, the
  design-intent doc states both contracts + a decidable litmus, and the gallery surfaces it
  on-device (confirmed via Gate-1 self-UAT). Phase 2 (Coherence Audit) can now use tier as a
  first-class signal when dispositioning overlap/near-duplicate siblings.
- **Phase 2 (Coherence Audit) complete** (2026-09-02): `docs/COHERENCE-AUDIT.md` enumerates all
  9 registered families (53 entries), flags overlap/near-duplicate-sibling/altitude-mismatch
  findings, and dispositions each as unify/keep-with-rationale/prune. Two "unify" findings
  (`ChipBar`/`FilterBar`; `TextCardBottomSheet`/`ListCardBottomSheet`) are aggregated into a
  concrete Unify Work-Order with pre-computed SecondBrain + CalTracker_Android blast-radius
  counts, ready for Phase 5 (Gardening) to execute against.

## Constraints

- **Division of labor**: consumers are the main editors (additive growth); this project does *not*
  take over feature/component authorship — stewardship only.
- **Breaking changes are gated + coordinated**: any prune/unify/rename → new tag + human-gated
  coordinated repin of **both** consumers, each Gate-1 re-verified. Never strand a consumer.
- **Invariants hold**: one-way dependency, bindings-only Hilt, ComponentRegistry drift guard, zero
  detekt baseline, Metalava `apiCheck` — all preserved.
- **Sequential-in-hub**: commit on `main`; no consumer worktrees; don't modify consumer files from
  hub-scoped tasks.

## Requirements

### Validated

<!-- Existing capabilities inferred from the codebase map. -->

- ✓ 9-family ComponentRegistry catalog + drift guard — existing
- ✓ Metalava `apiCheck` API-compatibility freeze-gate — existing
- ✓ One-way-dependency + bindings-only-Hilt invariants — existing
- ✓ Immutable-tag JitPack publishing + human-gated repin ritual — existing
- ✓ ExplorerActivity in-AAR gallery — existing
- ✓ Primitives/patterns **altitude legibility** — every `ComponentRegistry.Entry` carries an
  explicit, compile-time-enforced `Tier`; `docs/DESIGN-INTENT.md` states the primitives/patterns
  contracts and a decidable litmus; both gallery surfaces (`ComponentRow`, `ComponentDetailScreen`)
  display the tier — validated Phase 1 (Tier Legibility)
- ✓ **Coherence audit** of the 9 families — `docs/COHERENCE-AUDIT.md` enumerates all 53 entries,
  flags overlaps/near-duplicate siblings/altitude mismatches, dispositions each (unify /
  keep-with-rationale / prune), and aggregates "unify" findings into an actionable Unify
  Work-Order with per-consumer blast-radius counts — validated Phase 2 (Coherence Audit)

- ✓ **Prune/unify** the additive-duplicate accretion under a coordinated breaking "gardening" tag —
  unify shipped as `v2.0.0` (P5, GARD-01); coordinated consumer repin (GARD-02) deferred (human-gated)
- ✓ **Tier-aware contribution litmus** + a domain-vocabulary drift guard (flag, not forbid) —
  validated Phase 3 (GOV-01/02/03)
- ✓ Harden **repin bookkeeping** so reconciliation isn't hand-done — validated Phase 4 (REPIN-01),
  `INC-2026-08-28-03` closed

- ✓ **Line reunification** — forward-ported the 6 v1.x-only components (`DateTimePicker`, `PlaceMap*`
  cluster, `PresetChip`) onto `main` + admitted `osmdroid` — validated milestone v2.0, shipped `v2.2.0`
- ✓ **Per-tag chip color** — opt-in `containerColorOverride`/`color`/`CardTagRow` auto-thread —
  validated milestone v2.0 (`v2.2.0`), extended `TAGCOLOR-02` (pickers/editor strips) in `v2.3.0`
- ✓ **MicButton hardening** — parameterized microcopy, `rememberUpdatedState` callbacks, hub-neutral
  KDoc — validated milestone v2.0, shipped `v2.2.0`

- ✓ **Voice-command settings surfaces** — `ProviderKeyCard` (masked key + reveal, no persistence/no
  network), `ModelSelectCard`, `ApproachLadderCard` (tier ladder + offline-only toggle + max-tier cap),
  all prop-driven — validated milestone v2.4 (VSET-01/02, VAPPR-01/02/03), shipped `v2.4.0`
- ✓ **Voice outcome/failure sheet + clarification** — `OutcomeSheet` ("handled by: tier/approach"
  indicator, loud failure states, generic "Undo all (N)" + per-item undo with unavailable/refused
  states) and `ClarificationBar` tap-to-clarify choices — validated milestone v2.4 (VOUT-01/02/03,
  VUNDO-01, VCLAR-01), shipped `v2.4.0`
- ✓ **Generic needs-confirmation state** — additive `VoiceOutcomeUiState.NeedsConfirmation` sealed arm
  (reason + single-or-batch items + confirm/cancel) rendering both SB `MutationGate`/`VoiceConfirmGate`
  risk confirm and CT weak-match/batch confirm, domain-neutral — validated milestone v2.4 (VOUT-04),
  shipped `v2.4.0`
- ✓ **Catalog integrity + additive ship** — all 5 composables registered in the tenth "Voice Command"
  family (CATALOG-03 drift guard green in the full suite), API strictly additive vs `v2.3.0`, engine-free
  (INV-01), cut as `v2.4.0` via the §11 protocol with the ledger row relayed to the orchestrator —
  validated milestone v2.4 (CAT-01, API-01, INV-01, SHIP-01/02)
- ✓ Caller-localizable labels on the settings cards + voice-surface composables (XR-172-01, XR-175-02 a,b), English defaults preserved — v2.5 (VI18N-01..04; long-label layout hardened by G-15-1, ships `v2.5.1`)
- ✓ Approach-ladder rows: minimum interactive size + selected semantics (XR-175-02 c) — v2.5 (VA11Y-01)
- ✓ `VoiceOutcomeUiState.Failure` action role + body slot + a11y semantics prefix (XR-175-02 d) — v2.5 (VFAIL-01..03)
- ✓ `ApproachLadderCard` Router ON/OFF toggle, `require()`-paired like `offlineOnly` (XR-175-02 e) — v2.5 (VAPPR-04)
- ✓ Catalog integrity + regenerated `api.txt` + docs, then the additive `v2.5.0` cut (§11) — v2.5 (CAT-02, API-02, DOC-02, INV-02, SHIP-03)
- ✓ Tone foundation + atom components (Y-1, Y-8, Y-9, Y-10, Y-12) — v2.6 (VTONE-01, VBTN-01/02, VBAR-01, VCARD-01, VCHIP-01)
- ✓ `ReceiptRow` + segmented Pill style + sheet shape (Y-2, Y-11, Y-13) — v2.6 (VROW-01/02, VSEG-01, VSHEET-01)
- ✓ `OutcomeSheet` Receipt style + state additions + test tags (Y-3..Y-7, Y-14, SB in-flight action, XR-178-01 a/b) — v2.6 (VRCPT-01..05, VTAG-01, VXR-01/02)
- ✓ `ProviderKeyCard` key lifecycle + Undo Center group row (sketch §1, XR-178-01 c) — v2.6 (VKEY-01/02, VUNDO-02)
- ✓ Explorer/docs/catalog integrity + the `v2.6.0` cut (Y-15) — v2.6 (CAT-03, API-03, DOC-03, INV-03, DEMO-01, SHIP-04)

### Active

- [ ] v2.8: snackbar/outcome-sheet go-to-item + stacked Receipt-card snackbars (see Current Milestone)
- [ ] v2.8 ride-alongs: TalkBack sweep (999.2, scoped), Explorer demo polish (999.3), pill font-scale + dot contrast fixes
- [ ] Backlog: voice-surface polish (999.4) -> v2.8.1

### Out of Scope

- Feature / new-component **authorship** — stays the consumers' give-legs — the hub isn't the
  editing channel
- Forcing SB/CalTracker onto a **shared pin** — consumers repin on their own cadence; gardening
  coordinates repins, it doesn't mandate lockstep
- Any `@HiltAndroidApp` / consumer import — violates the reusability invariants

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Hub gets its own GSD project for stewardship; consumers stay main editors | Coherence is a global property no single consumer's litmus can enforce; additive-only can't prune | — Pending |
| Treat the hub as a two-tier system (primitives + patterns); make it legible before formalizing | Enough evidence (one contribution each way) that the tiering is real, not yet forced | Validated — Phase 1 shipped `Tier` enum, `DESIGN-INTENT.md`, and gallery-visible badges on both surfaces |
| v2.0: reunify FORWARD onto `main` (not a v1.14.0 additive cherry-pick) + admit the chip-color feature onto the same tag | Kills the standing v1.x/v2.x divergence liability instead of perpetuating it; one unified `v2.2.0` = one SB repin. Keeping ChipBar consolidation (not restoring standalone FilterBar) preserves the coherence-audit gain | ✓ Good — shipped v2.2.0 (2026-09-27) |
| v2.4: admit the shared AI-voice UI (settings + outcome/failure surfaces) into the hub as generic, prop-driven presentational composables — no OkHttp, no engine dependency | §6.3 of the vae-bilingual contract: SB + CT both need the same voice-command settings/outcome UI; putting the domain-neutral shell in the hub (apps map engine outcomes → props) keeps the one-way-dependency invariant clean and gives both consumers one surface | ✓ Good — shipped `v2.4.0` (2026-10-01), 16/16 reqs, one-way-dependency invariant held (INV-01) |
| v2.4: cut the immutable `v2.4.0` tag autonomously on green verification (A12 human-gate waiver) rather than pausing for a human tag gate | The vae-bilingual effort waived per-repo human tag gates (A12, Yahir) so the five coordinated repos can ship without serialized human checkpoints; the agent owns tag correctness via §11 steps 1–4 + the orchestrator's ledger re-check (A14) | ✓ Good — `v2.4.0` cut + JitPack-confirmed; §11 ledger relayed; no stray marker tag (SHIP-02) |
| v2.5: ship the G-15-1 long-label layout fix as a post-tag `v2.5.1` patch, not by re-cutting `v2.5.0` | Tags are immutable JitPack coordinates. The fix is layout-only (`api.txt` unchanged), so a patch tag is the clean path | ✓ Good. `v2.5.1` cut 2026-10-11, JitPack green, SB + CT repinned |
| v2.6: source compatibility only, with no new hidden overloads or binary shims; cut gate = `apiCheck` on a regenerated `api.txt` + `tools/test/run-all.sh` + a v2.5.1 call-site compile canary; no Roborazzi (sketch P1-P3) | Consumers recompile on every repin; shims are dead weight and a known defect source (INC-2026-10-05-02 F1c). `verify-api-additive.sh` was retired in Phase 19. Screenshot tooling was declined (Yahir) | ✓ Good. `v2.6.0` and `v2.7.0` shipped on these gates. The one positional break (`handledBy`) was reviewed and accepted |
| v2.7.0: add an experimental-typed param (`SheetState`) as a separate `OutcomeSheet` overload, not a defaulted param | A defaulted `@ExperimentalMaterial3Api` param would force the opt-in on every caller | ✓ Good. Existing call sites are unchanged |
| v2.8: snackbar targets are an opaque `FeedbackTarget(key, openLabel)`, not an `onOpen` lambda | The singleton `UndoHistoryStore` must not capture a NavController; one app-level `onOpenTarget` maps keys to routes and can skip same-destination opens | — Pending |
| v2.5: make rung-row label width depend on what the name needs, not a fixed 60% cap (CR-01) | A fixed cap stacked short English labels and broke the identical-to-v2.5.0 promise; the name now keeps whichever is smaller, its full width or 40%, and the affordances get the rest | ✓ Good. English one-line and German no-clip both verified on device (Gate-2 17/17) |

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition** (via `/gsd-transition`):
1. Requirements invalidated? → Move to Out of Scope with reason
2. Requirements validated? → Move to Validated with phase reference
3. New requirements emerged? → Add to Active
4. Decisions to log? → Add to Key Decisions
5. "What This Is" still accurate? → Update if drifted

**After each milestone** (via `/gsd-complete-milestone`):
1. Full review of all sections
2. Core Value check — still the right priority?
3. Audit Out of Scope — reasons still valid?
4. Update Context with current state

---
*Last updated: 2026-10-14 after the v2.6 milestone (Gate-2 signed off with a TalkBack gap; next: v2.7.1). Prior: v2.6 → `v2.6.0`/`v2.7.0`; v2.5 → `v2.5.0`/`v2.5.1`; v2.4 → `v2.4.0`/`v2.4.1`; v2.0 → `v2.2.0`/`v2.3.0`; v1.0 → `v2.0.0`.*
