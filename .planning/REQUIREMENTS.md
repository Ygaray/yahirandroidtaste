# Requirements: yahirandroidtaste — Hub Stewardship

**Defined:** 2026-08-28
**Core Value:** The hub stays a coherent design system — not merely a safe, ever-growing pile of
domain-agnostic components — as more consumers contribute.

## Requirements

Stewardship scope for the hub itself. Consumers keep authoring components additively via their own
give-legs; these requirements are the hub-owned coherence / gardening / governance work.

### Legibility — make the two-tier structure visible

- [ ] **LEG-01**: Every component registered in `ComponentRegistry` carries an explicit **tier**
  label (`primitive` | `pattern`), queryable in the registry and shown in the `ExplorerActivity`
  gallery.
- [ ] **LEG-02**: A design-**intent** doc (distinct from the registry-of-what-exists) states what
  the hub is *per tier* — the primitives contract and the patterns contract — and the litmus each
  tier must pass.

### Coherence Audit — surface the incoherence

- [ ] **AUD-01**: A coherence audit enumerates the 9 families and flags overlaps, near-duplicate
  sibling components, and altitude mismatches, with a documented disposition
  (unify / keep-with-rationale / prune) for each finding.

### Gardening — the breaking unification work

- [ ] **GARD-01**: Additive-duplicate siblings identified by AUD-01 are **unified** into single
  components (removing/renaming where needed — the breaking work the consumer channel can't do).
- [ ] **GARD-02**: The gardening changes land via the **human-gated coordinated repin** ritual —
  new immutable tag → both consumers (SecondBrain + CalTracker) repinned and Gate-1 re-verified —
  with no consumer left stranded.

### Governance — prevent future drift

- [ ] **GOV-01**: A **tier-aware contribution litmus** is documented (and enforced where feasible) —
  primitives get the strict no-domain-vocabulary gate; patterns get the looser opinion-allowed gate.
- [ ] **GOV-02**: A **domain-vocabulary drift guard** *flags* (not forbids) when a public component
  name introduces a domain noun, surfacing coupling for review.
- [ ] **GOV-03**: The additive-guard pre-commit hook stops false-flagging non-AAR paths (`.planning/`,
  docs) as lane-2, so planning/doc commits land without `HUB_LANE_OVERRIDE`.

### Repin Bookkeeping — harden reconciliation

- [ ] **REPIN-01**: The hub's `ECOSYSTEM.md` carries the `repin-matrix` markers so
  `repin_status.py reconcile` operates without hand edits — closes `INC-2026-08-28-03`.

## v2 / Future Requirements

Tracked, not in this milestone.

- **GOV-04**: Automate tier-labeling enforcement in the drift-guard test (fail the build if a new
  public composable ships without a tier).
- **ECO-02**: Auto-repin tooling across all consumers (the ecosystem's stated end goal).

## Out of Scope

| Feature | Reason |
|---------|--------|
| New feature / component authorship | Consumers' give-legs own this; the hub isn't the editing channel |
| Forcing SB/CalTracker onto a shared pin | Consumers repin on their own cadence; gardening coordinates, it doesn't mandate lockstep |
| Any `@HiltAndroidApp` / consumer import | Violates the reusability invariants |

## Traceability

Populated during roadmap creation.

| Requirement | Phase | Status |
|-------------|-------|--------|
| LEG-01 | — | Pending |
| LEG-02 | — | Pending |
| AUD-01 | — | Pending |
| GARD-01 | — | Pending |
| GARD-02 | — | Pending |
| GOV-01 | — | Pending |
| GOV-02 | — | Pending |
| GOV-03 | — | Pending |
| REPIN-01 | — | Pending |

**Coverage:**
- Requirements: 9 total
- Mapped to phases: 0 (pending roadmap)

---
*Requirements defined: 2026-08-28*
