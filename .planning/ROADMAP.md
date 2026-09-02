# Roadmap: yahirandroidtaste — Hub Stewardship

## Overview

This is a stewardship milestone, not a feature build: the hub goes from a flat, additive-only
catalog to a **legible, audited, governed** two-tier design system. The spine runs
Legibility → Audit → Gardening: first make the latent primitives/patterns tiering visible in code
and docs (Phase 1), then use that vocabulary to run a coherence audit that dispositions every
overlap/near-duplicate/altitude mismatch across the 9 families (Phase 2). Governance hardening
(Phase 3) and repin-bookkeeping hardening (Phase 4) are independent tracks that harden the
tooling substrate in parallel with — or ahead of — the one deliberately breaking phase: Gardening
(Phase 5), which unifies whatever the audit dispositioned "unify" and lands it via a single
human-gated coordinated repin of both consumers, each re-verified at Gate-1. No consumer is
stranded; nothing here touches feature/component authorship, which stays the consumers' give-legs.

## Phases

**Phase Numbering:**

- Integer phases (1, 2, 3): Planned milestone work
- Decimal phases (2.1, 2.2): Urgent insertions (marked with INSERTED)

Decimal phases appear between their surrounding integers in numeric order.

- [x] **Phase 1: Tier Legibility** - Every registered component gets an explicit tier, and a design-intent doc states each tier's contract and litmus (completed 2026-09-01)
- [x] **Phase 2: Coherence Audit** - The 9 families get a documented audit of overlaps, near-duplicate siblings, and altitude mismatches, each with a disposition (completed 2026-09-01)
- [ ] **Phase 3: Governance Gates** - A tier-aware contribution litmus, a domain-vocabulary drift guard, and a pre-commit false-flag fix prevent future drift
- [ ] **Phase 4: Repin Bookkeeping Hardening** - ECOSYSTEM.md carries the repin-matrix markers so reconciliation isn't hand-done
- [ ] **Phase 5: Gardening — Unify & Coordinated Repin** - Audit-dispositioned duplicate siblings are unified and landed via a coordinated, Gate-1-reverified repin of both consumers

## Phase Details

### Phase 1: Tier Legibility

**Goal**: The hub's latent two-tier structure (primitives vs. patterns) is visible — in the registry, in the gallery, and in a design-intent doc — instead of implicit tribal knowledge.
**Depends on**: Nothing (first phase)
**Requirements**: LEG-01, LEG-02
**Success Criteria** (what must be TRUE):

  1. Every entry in `ComponentRegistry` carries an explicit tier (`primitive` | `pattern`) that can be queried in code.
  2. The `ExplorerActivity` gallery displays each component's tier on its detail/list view, so a developer browsing the catalog can see tiering without reading source.
  3. A design-intent doc, distinct from the registry-of-what-exists, states what the hub means to be **per tier** — the primitives contract and the patterns contract.
  4. That design-intent doc states the litmus each tier must pass (what qualifies a component as primitive vs. pattern).

**Plans**: 5/5 plans executed

Plans:
**Wave 1**

- [x] 01-01-PLAN.md — Add Tier enum + Entry.tier field; tier Cards (11) + Chips (5) entries (tracer)
- [x] 01-02-PLAN.md — Write docs/DESIGN-INTENT.md (primitives/patterns contracts + litmus)

**Wave 2** *(blocked on Wave 1 completion)*

- [x] 01-03-PLAN.md — Tier all 18 Sheets-family entries
- [x] 01-04-PLAN.md — Tier remaining 19 entries (ButtonsFab/Pickers/Feedback/EmptyState/Progress/TactileFoundation)

**Wave 3** *(blocked on Wave 2 completion)*

- [x] 01-05-PLAN.md — Wire tier badge into ComponentRow + ComponentDetailScreen; apiDump/apiCheck/detekt; tier-queryability test

**UI hint**: yes

### Phase 2: Coherence Audit

**Goal**: The hub's incoherence — overlap, near-duplicate siblings, altitude mismatches — is surfaced and dispositioned, not merely suspected.
**Depends on**: Phase 1 (the primitive/pattern vocabulary is what makes "altitude mismatch" a nameable finding)
**Requirements**: AUD-01
**Success Criteria** (what must be TRUE):

  1. A written coherence audit enumerates all 9 registered families.
  2. The audit flags overlaps, near-duplicate sibling components, and altitude (tier) mismatches across those families.
  3. Every flagged finding carries a documented disposition: unify, keep-with-rationale, or prune.
  4. The "unify" dispositions form a concrete, actionable list that Phase 5 (Gardening) executes against.

**Plans**: 2/2 plans executed

Plans:
**Wave 1**

- [x] 02-01-PLAN.md — Skeleton (tracer) + audit Cards/Chips/Sheets/Tactile Foundation (38 of 53 entries)

**Wave 2** *(blocked on Wave 1 completion — same file)*

- [x] 02-02-PLAN.md — Audit Buttons-FAB/Pickers/Feedback/Empty-State/Progress-Metrics (15 entries) + assemble Unify Work-Order

### Phase 3: Governance Gates

**Goal**: Future additive-duplicate drift and lane-flagging friction are caught by tooling, not left to memory.
**Depends on**: Phase 1 (the tier-aware litmus references the tiers Phase 1 makes explicit)
**Requirements**: GOV-01, GOV-02, GOV-03
**Success Criteria** (what must be TRUE):

  1. A tier-aware contribution litmus is documented: primitives get the strict no-domain-vocabulary gate, patterns get the looser opinion-allowed gate.
  2. That litmus is enforced where feasible — wired into review/test tooling, not just prose.
  3. A domain-vocabulary drift guard flags (does not forbid) when a new public component name introduces a domain noun, surfacing the coupling for human review.
  4. The additive-guard pre-commit hook no longer false-flags non-AAR paths (`.planning/`, docs) as lane-2 — planning/doc commits land without needing `HUB_LANE_OVERRIDE`.

**Plans**: 2/2 plans executed

Plans:
**Wave 1**

- [x] 03-01-PLAN.md — Fix pre-commit false-flag: staged-delta diff basis (GOV-03) + regression tests + tracked residual-risk proof for the API-line sibling bug
- [x] 03-02-PLAN.md — Domain-vocabulary drift guard (GOV-02) + tier-aware contribution litmus doc (GOV-01)

### Phase 4: Repin Bookkeeping Hardening

**Goal**: Repin reconciliation across consumers is a tooling operation, not a hand-edited chore — closing a standing incident.
**Depends on**: Nothing new — independent hardening track, sequenced ahead of Phase 5 so the reconcile tooling is proven before it's exercised on the real coordinated repin
**Requirements**: REPIN-01
**Success Criteria** (what must be TRUE):

  1. `ECOSYSTEM.md` carries the `repin-matrix` markers `repin_status.py reconcile` requires.
  2. Running `repin_status.py reconcile` against the hub's current SecondBrain/CalTracker pins succeeds without any hand edits to `ECOSYSTEM.md`.
  3. `INC-2026-08-28-03` is verifiably closed — the reconcile output reflects the true pin state for both consumers.

**Plans**: TBD

### Phase 5: Gardening — Unify & Coordinated Repin

**Goal**: The additive-duplicate accretion the audit found is pruned/unified, and the breaking change reaches both consumers safely — the one thing the consumer-driven channel structurally can't do itself.
**Depends on**: Phase 2 (the audit's "unify" dispositions define the scope), Phase 4 (hardened repin bookkeeping verifies the coordinated repin)
**Requirements**: GARD-01, GARD-02
**Success Criteria** (what must be TRUE):

  1. Every "unify" disposition from the Phase 2 audit is implemented as a single unified component (duplicate siblings removed or folded in, renaming where needed).
  2. `ComponentRegistry`'s drift guard and Metalava `apiCheck` (rebaselined for this intentional breaking change) both pass after unification.
  3. A new immutable tag is cut containing the gardening changes.
  4. Both SecondBrain and CalTracker are repinned to the new tag, each re-verified at Gate-1, with neither consumer left stranded.

**Plans**: TBD

## Progress

**Execution Order:**
Phases execute in numeric order: 1 → 2 → 3 → 4 → 5

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Tier Legibility | 5/5 | Complete    | 2026-09-01 |
| 2. Coherence Audit | 2/2 | Complete    | 2026-09-01 |
| 3. Governance Gates | 2/2 | In Progress|  |
| 4. Repin Bookkeeping Hardening | 0/TBD | Not started | - |
| 5. Gardening — Unify & Coordinated Repin | 0/TBD | Not started | - |
