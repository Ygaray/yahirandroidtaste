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

**Shipped: milestone v1.0 — Hub Stewardship (2026-09-02) → library `v2.0.0`.** The hub is now a
legible, audited, governed two-tier (primitives/patterns) design system: explicit compile-enforced
tiers + gallery badges, a full coherence audit, tier-aware governance gates + a domain-vocabulary
drift guard, hardened repin bookkeeping, and the first breaking "gardening" unification (FilterBar→
ChipBar, SheetHeaderMenu) cut as `v2.0.0`. 8/9 requirements satisfied.

**One thing deliberately deferred (human-gated):** the **GARD-02 coordinated consumer repin** —
SecondBrain + CalTracker onto `v2.0.0`, each Gate-1 re-verified, then `repin_status.py reconcile`
(which also clears tech-debt W-1, the stale ECOSYSTEM.md matrix). That is the next concrete
follow-on. See `.planning/MILESTONES.md` and `.planning/milestones/v1.0-*`.

## Current Milestone: v2.0 Line Reunification

**Goal:** Collapse the divergent v1.x (SecondBrain `v1.13.0`) and v2.x/`main` (CalTracker `v2.1.0`)
release lines into one forward line on `main`, cut as library `v2.2.0`, so all consumers converge on
one tag and v1.x retires — completing v1.0's deferred **GARD-02** coordinated repin (now onto
`v2.2.0`) and clearing tech-debt **W-1**.

**Target features:**
- **Reunification** — forward-port the six v1.x-only components (`DateTimePicker`, the `PlaceMap*`
  cluster, `PresetChip`) onto `main` (net-additive); admit `osmdroid`; keep every v2.x improvement
  (MicButton, SheetHeaderMenu, tier legibility, governance gates).
- **Per-tag chip color** — a consumer-requested additive capability (SB Phase 165): opt-in
  `containerColorOverride` on `AppChip`/`TagChipWithContextMenu` + `color` on `TagChipUiModel`,
  auto-threaded at `CardTagRow`. *(Feature authorship, normally the consumers' channel — admitted
  here by explicit owner decision to keep it one tag / one SB repin.)*
- **MicButton hardening** — reusability + correctness fixes surfaced by CalTracker's review
  (parameterize microcopy, `rememberUpdatedState` callbacks, KDoc/ergonomics).
- **Ship & converge** — cut `v2.2.0` (human-gated), coordinated consumer repin, `reconcile`.

Full design: `docs/superpowers/specs/2026-09-26-hub-line-reunification-design.md`; scope brief:
`docs/superpowers/specs/2026-09-26-milestone-v2.0-scope.md`.

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

### Active

<!-- Milestone v2.0 (Line Reunification) scope. Hypotheses until shipped. -->

- [ ] **REUNI** — forward-port the 6 v1.x-only components (`DateTimePicker`, `PlaceMap*`,
  `PresetChip`) onto `main` + admit `osmdroid` + domain-vocab head-token entries
- [ ] **TAGCOLOR** — per-tag chip color slot (`containerColorOverride`/`color`/CardTagRow auto-thread),
  consumer-requested additive capability (owner-admitted into stewardship scope)
- [ ] **MICBTN** — MicButton reusability + correctness hardening (CalTracker findings)
- [ ] **GARD-02** (absorbed from v1.0, now onto `v2.2.0`): cut the unified tag, coordinated consumer
  repin (SB single-hop, CalTracker `v2.1.0→v2.2.0`), `repin_status.py reconcile` (clears W-1) —
  human-gated

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
| v2.0: reunify FORWARD onto `main` (not a v1.14.0 additive cherry-pick) + admit the chip-color feature onto the same tag | Kills the standing v1.x/v2.x divergence liability instead of perpetuating it; one unified `v2.2.0` = one SB repin. Keeping ChipBar consolidation (not restoring standalone FilterBar) preserves the coherence-audit gain | — Pending |

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
*Last updated: 2026-09-26 — milestone v2.0 (Line Reunification) started → will cut library v2.2.0; absorbs v1.0's deferred GARD-02 coordinated repin*
