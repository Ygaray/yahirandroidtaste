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

### Active

<!-- This project's charter. Hypotheses until shipped. -->

- [ ] Make the primitives/patterns **altitude legible** — tag every registered component's tier
- [ ] A design-**intent** doc distinct from the registry (what the hub means to be, per tier)
- [ ] **Coherence audit** of the 9 families — surface overlap, near-duplicate siblings, incoherence
- [ ] **Prune/unify** the additive-duplicate accretion (`v1.2→v1.10`) under a coordinated breaking
  "gardening" tag
- [ ] **Tier-aware contribution litmus** + a domain-vocabulary drift guard (flag, not forbid)
- [ ] Harden **repin bookkeeping** (tracked as `INC-2026-08-28-03`) so reconciliation isn't hand-done

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
| Treat the hub as a two-tier system (primitives + patterns); make it legible before formalizing | Enough evidence (one contribution each way) that the tiering is real, not yet forced | — Pending |

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
*Last updated: 2026-08-28 after initialization*
