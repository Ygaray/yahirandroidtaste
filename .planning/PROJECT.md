# yahirandroidtaste — Hub Stewardship

## What This Is

A GSD project that governs the reusable `yahirandroidtaste` Compose UI hub **as an artifact** —
its coherence, structure, and long-term health — distinct from the feature/component work that
consumer apps drive into it. **Consumers (SecondBrain, CalTracker) remain the primary editors**:
they grow the catalog additively through their own give-legs. This project owns the stewardship
that channel structurally *can't* do: pruning additive-duplicate accretion, making the latent
primitives/patterns tiering legible, curating the design language, and hardening ecosystem
governance.

## Core Value

The hub stays a **coherent** design system — not merely a safe, ever-growing pile of
domain-agnostic components — as more consumers contribute. If all else fails, this must keep the
catalog legible and prunable.

## Current State

**Shipped: milestone v2.4 — AI-Voice Command UI (2026-10-01) → library `v2.4.0`.** The hub now carries
a shared, domain-neutral AI-voice UI layer as its tenth "Voice Command" `ComponentRegistry` family:
`ProviderKeyCard` + `ModelSelectCard` (provider/key + model settings), `ApproachLadderCard`
(tier-ladder + offline-only toggle + max-tier cap), `OutcomeSheet` (a "handled by: tier/approach"
indicator, loud failure states, a generic undo affordance, and a domain-neutral needs-confirmation
state covering SB risk-confirm and CT weak-match/batch confirm), and `ClarificationBar` (tap-to-clarify
choices). All prop-driven and engine-free (no OkHttp, no `voice-action-engine` dependency — INV-01),
strictly additive versus `v2.3.0`, cut as `v2.4.0`. 16/16 requirements satisfied; Gate-2 signed off
(Yahir, 2026-10-01). Wave 0, §6.3 of the `vae-bilingual` cross-repo effort.

**Next concrete follow-on (Wave 1, each consumer's own channel):** SecondBrain (`v2.3.0`→`v2.4.0`) and
CalTracker (`v2.1.0`→`v2.4.0`) repin onto `v2.4.0` and adopt the voice UI, gated by the orchestrator's
R2 reconvene. Two Gate-2-waived `ApproachLadderCard` UI-polish notes are tracked as future polish
(`KNOWN-ISSUES.md` KI-2026-10-01-01).

**Prior milestones:** v1.0 — Hub Stewardship (2026-09-02, `v2.0.0`); v2.0 — Line Reunification
(2026-09-27, `v2.2.0`, + `TAGCOLOR-02` follow-on `v2.3.0`). See `.planning/MILESTONES.md`.

## Current Milestone: v2.5 — Voice UI Localization & Accessibility

**Goal:** Make the AI-voice UI surface fully caller-localizable and more accessible — strictly
additively, with no behavior change for existing callers.

**Target features:**
- i18n label params — settings-card labels (XR-172-01: Provider/Model) + voice-surface literals
  (XR-175-02 a,b: ClarificationBar / OutcomeSheet / HandledBy / ApproachLadderCard), some via
  additive model fields
- A11y + Failure enrichment — approach-row minimum interactive size + selected semantics
  (XR-175-02 c); `VoiceOutcomeUiState.Failure` action role + body slot + semantics prefix (XR-175-02 d)
- `ApproachLadderCard` Router ON/OFF toggle (XR-175-02 e) — `router`/`onRouterChange`, the
  `offlineOnly` pattern
- Catalog integrity + regenerated `api.txt` + docs (no tag), then cut `v2.5.0` (isolated, follows a green catalog)

**Cross-repo:** Wave 0, §6.3 of the `vae-bilingual` effort; orchestrator `yahir-gsd-control-plane-6e`
(of record per `xrepo/vae-bilingual/effort.json` @`87131d1`; prior `-f2` retired). R-v1.1 GO issued;
scope locked to `.planning/cross-repo/RECONVENE-BRIEF-R-v1.1.md` (`cb5f047`). The tag cut goes through
`xrepo build` and stays human-gated (A12 waiver pending Yahir's direct OK). The A13 reconvene protocol
governs this slice.

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

### Active

<!-- Milestone v2.5 — Voice UI Localization & Accessibility (scope locked by RECONVENE-BRIEF-R-v1.1, cb5f047). REQ-IDs defined in REQUIREMENTS.md. All strictly additive vs v2.4.0. -->

- [ ] Caller-localizable labels on the settings cards + voice-surface composables (XR-172-01, XR-175-02 a,b) — English defaults preserved
- [ ] Accessibility on approach-ladder rows: minimum interactive size + selected semantics (XR-175-02 c)
- [ ] `VoiceOutcomeUiState.Failure` enrichment: action role + optional body slot + a11y semantics prefix (XR-175-02 d)
- [ ] `ApproachLadderCard` Router ON/OFF toggle — `router`/`onRouterChange`, mirrors `offlineOnly` (XR-175-02 e)
- [ ] Catalog integrity + regenerated `api.txt` + docs, then additive `v2.5.0` cut (§11, human-gated)

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
*Last updated: 2026-10-05 — started milestone v2.5 (Voice UI Localization & Accessibility; vae-bilingual R-v1.1 GO, scope locked to RECONVENE-BRIEF-R-v1.1 cb5f047, all additive). Prior: v2.4 AI-Voice Command UI shipped as `v2.4.0` (2026-10-01, Gate-2 signed off, 16/16 reqs; a v2.4.1 OutcomeSheet hotfix followed). SB + CT repinned to v2.4.0 (→ v2.4.1 in their own channels). Earlier: v1.0 → v2.0.0 (2026-09-02); v2.0 Line Reunification → v2.2.0 (2026-09-27), TAGCOLOR-02 → v2.3.0.*
