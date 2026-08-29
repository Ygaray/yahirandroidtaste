# Project Research Summary

**Project:** yahirandroidtaste (hub stewardship)
**Milestone:** v1.0
**Domain:** Design-system stewardship of a mature, invariant-guarded, two-consumer reusable Jetpack Compose UI hub
**Researched:** 2026-08-28
**Confidence:** HIGH

## Executive Summary

This is **not a greenfield build**. `yahirandroidtaste` is an existing, single-module, additive-only reusable Compose UI library with two live consumers (SecondBrain pinned `v1.10.0`, CalTracker pinned `v1.5.0`). This milestone is five **stewardship deliverables** — make the latent two-tier taxonomy legible, audit the catalog for incoherence, add governance gates, harden repin bookkeeping, and execute the one coordinated breaking "gardening" pass — all while preserving four load-bearing invariants (one-way dependency / no domain assumptions, ComponentRegistry drift guard, zero-baseline detekt, Metalava `apiCheck`). No new runtime dependency is permitted; every version is already pinned and every tool the work needs already exists in `tools/`, the test harness, or the external control-plane `repin_status.py`.

The recommended approach is dictated by the existing substrate, not chosen freely. Tier data lives as a **required `enum class Tier { PRIMITIVE, PATTERN }` field on `ComponentRegistry.Entry`** (never a parallel map — that violates the single-source-of-truth invariant), threaded through all 53 `Entry(...)` call sites across the 9 family files and surfaced as a gallery badge. The single most load-bearing technical finding: adding *any* field to `Entry` — required or defaulted — rewrites its one-line Metalava ctor signature in `api.txt`, so the set-based `verify-api-additive.sh` guard reads it as a lane-3 removal. Phase 1 is therefore unavoidably a **curation-lane commit + `apiDump` rebaseline**, not "just an additive field." Because that cost is paid either way, making `tier` required (no default) is free and buys a compile-time "every entry is tiered" guarantee.

The dominant risks are all about **discipline and scale**, not implementation difficulty. Phases 1–4 are documentation/tooling with low physical risk; their value is judgment quality and de-risking Phase 5. Phase 5 is the only phase with real breaking-change and multi-repo cost — its marquee failure mode is **stranding a consumer** (a documented precedent: SecondBrain sat stranded at `v1.8.2` through two cycles). Mitigation: treat the coordinated repin as atomic across both consumers, gate tag-cut and consumer edits behind human sign-off (sequential-in-hub, tags immutable), and prove the repin matrix reconcile *before* exercising it on the real break. The second discipline risk is **over-engineering governance for two consumers** — the requirements deliberately defer GOV-04 (build-fail on missing tier) and ECO-02 (auto-repin) to v2; do not pull them forward.

## Key Findings

### Recommended Stack

No new stack — every version is already pinned (Kotlin 2.3.20, AGP 9.2.1, Compose BOM 2026.02.01, Hilt 2.60.1 bindings-only, JDK 17, minSdk 35 / compileSdk 36.1). The "stack" question is *which existing language features, test patterns, and tooling techniques* implement the five deliverables with zero new dependencies. Single-module hub at repo root (D-01): every Gradle command drops the module prefix.

**Core technologies (all already present):**
- **Kotlin `enum class Tier`** — the tier representation; gives exhaustiveness + `.name` for gallery display, zero dependency. Precedent: repo already ships public enums.
- **Metalava (`apiDump`/`apiCheck`)** — the freeze-gate; the rebaseline mechanism for Phase 1 (tier field) and Phase 5 (unify break). Kotlin built-in ABI validators were already spiked and rejected on this AGP-9 stack.
- **JUnit source-TEXT-scan tests** — reuse `ComponentRegistryDriftGuardTest`'s reflection-free name extraction for the GOV-02 domain-noun guard (reflection is fragile against Compose synthetic params).
- **Existing `tools/` governance chain** — `classify-hub-change.sh --mode curation` and `HUB_LANE_OVERRIDE` are the *designed* escape hatch for deliberate non-additive stewardship (Phases 1 and 5).
- **External `repin_status.py reconcile`** (control plane, outside this repo) — Phase 4 contributes only a Markdown `repin-matrix` marker block; no Python added here.

### Expected Features

The five "features" are stewardship deliverables. The overriding discipline is **right-sizing**: every mature-org practice has a heavyweight form (dashboards, multi-tier taxonomies, forbidding gates, auto-repin) that is *wrong* at two consumers.

**Must have (table stakes):**
- Every registry entry tiered + tier badge in gallery + design-intent doc with per-tier **contract** and **litmus** (LEG-01/02)
- Written audit of all 9 families, every finding dispositioned (unify / keep-with-rationale / prune), concrete unify list (AUD-01)
- Tier-aware litmus documented + **flag-not-forbid** domain-noun drift guard + GOV-03 pre-commit false-flag fixed (GOV-01/02/03)
- `repin-matrix` markers + `reconcile` runs hand-edit-free reflecting true pins (REPIN-01)
- Unify list implemented, drift guard + Metalava green (rebaselined), one tag, both consumers repinned + Gate-1 verified (GARD-01/02)

**Should have (the good-vs-checkbox delta — the point of the milestone):**
- Litmus as one **decidable question** + borderline-cases section (P1)
- Quantified duplication + `keep-with-rationale` as first-class + consumer call-sites per unify (P2)
- Guard suggests the resolution + allowlist as the domain-coupling audit trail (P3)
- Drift/behind flag per consumer + idempotent reconcile (P4)
- Per-component old→new migration map + single batched tag (P5)

**Defer (v2+):**
- GOV-04 (build-failing tier-presence enforcement), ECO-02 (auto-repin tooling)
- Third/fourth tier, per-tier module split, cross-repo adoption scanner, severity-scoring, forbidding name gates, RFC/approval boards

### Architecture Approach

Integration research, not greenfield: new data, docs, and gates slot into the existing `ComponentRegistry → drift-guard → gallery → governance-chain → repin` substrate without touching the four invariants. Tier lives ON `Entry`; the design-intent doc is a NEW `docs/DESIGN-INTENT.md` distinct from `API.md`; P4's machine matrix is a NEW marker-delimited block, NOT the existing §1 prose table.

**Major components:**
1. **`ComponentRegistry.Entry` + `entries`** — single authoritative record; P1 adds `tier` here and on all 53 call sites across 9 family files.
2. **Gallery (`ExplorerEntry` NavHost)** — index → family → detail; P1 renders the tier badge on list row + detail header.
3. **Governance chain (`tools/` + pre-commit)** — lane 1/2/3 classifier; P1 trips it (curation lane), P3 fixes the false-flag, P5 lands the deliberate lane-3 break.
4. **`docs/` artifacts** — NEW `DESIGN-INTENT.md` (P1) and `COHERENCE-AUDIT.md` (P2).
5. **`ECOSYSTEM.md` + external `repin_status.py`** — P4 adds machine matrix markers; P5 records the new tag.

### Critical Pitfalls

1. **Adding `tier` the wrong way** — a silent default hollows out legibility (every entry "tiered" but nobody decided); mid-list insertion shifts `componentN()`. → Add `tier` **required, last position**; if defaulted for staged migration, pair with a fail-on-default test the same phase.
2. **Treating Phase 1 as "just a refactor"** — `Entry` is public API; the change trips Metalava + the additive guard. → Plan it as a known curation commit + deliberate `apiDump`; it's additive (no consumer break) but needs a tag before tiers ship.
3. **Stranding a consumer in the Phase 5 repin** (the marquee failure, with real precedent). → Atomic across both consumers: stage both bumps, don't cut the tag until both ready, Gate-1 verify both, prove pins moved via reconcile.
4. **Governing domain vocabulary by importing domain vocabulary** — a consumer-term denylist ships domain knowledge into the hub, breaking the very invariant it protects. → Keep the guard structural/heuristic and flag-not-forbid; consumer terms only in test fixtures.
5. **Blind `apiDump` masking an unintended break** — one-shot "make it green" accepts every delta. → Review the `api.txt` diff line-by-line; every changed line must map 1:1 to the intended change set.

Also watch: unfalsifiable tier taxonomy (litmus must be a *decidable* test), drift-guard integrity breaks during unify, un-actionable audit findings, over-engineered governance, marker/prose double-source drift, autonomous tag/repin past the human gate, and detekt baseline burial.

## Implications for Roadmap

The existing 5-phase ROADMAP is validated by research. Numeric order `1 → 2 → 3 → 4 → 5` is safe and violates no invariant. Research surfaces one sequencing refinement to flag for discussion.

### Phase 1: Tier Legibility (LEG-01, LEG-02)
**Rationale:** Defines the tier vocabulary both P2 (altitude-mismatch findings) and P3 (tier-aware litmus) consume — the milestone's spine.
**Delivers:** `Tier` enum + required field on `Entry` (all 53 sites), gallery badge, `docs/DESIGN-INTENT.md` with per-tier contract + decidable litmus.
**Uses:** Kotlin enum, Material3 badge (both on classpath), Metalava `apiDump`.
**Avoids:** Pitfalls 1, 2, 3 — required field (no hollow default), curation-lane + `apiDump` (recognize the API event), decidable litmus (falsifiable taxonomy).

### Phase 2: Coherence Audit (AUD-01)
**Rationale:** "Altitude mismatch" is only nameable once tiers exist (hard dep on P1); produces the unify list that is P5's entire work order.
**Delivers:** `docs/COHERENCE-AUDIT.md` — 9 families enumerated, every finding typed (overlap / near-duplicate sibling / altitude) and dispositioned, concrete unify tuples with consumer call-sites.
**Avoids:** Pitfall 8 — dispositions as verbs not adjectives; every unify a concrete (A,B)→C tuple.

### Phase 3: Governance Gates (GOV-01, GOV-02, GOV-03)
**Rationale:** Independent of P2 (needs only P1's tiers); its GOV-03 false-flag fix unblocks clean doc-commits for P2 and P4.
**Delivers:** Tier-aware litmus prose, flag-not-forbid `DomainVocabularyDriftGuardTest` with allowlist, pre-commit path-scope fix (retiring the `HUB_LANE_OVERRIDE=2` planning-doc bypass).
**Avoids:** Pitfalls 4, 9, 12 — structural guard (no domain terms), stay inside the v2 boundary, zero detekt baseline.

### Phase 4: Repin Bookkeeping Hardening (REPIN-01)
**Rationale:** Independent track, sequenced before P5 so reconcile is proven on current pins before the real coordinated break (closes INC-2026-08-28-03).
**Delivers:** `<!-- repin-matrix:begin/end -->` markers + machine-owned 4-column block (header contains "Consumer") seeded with true pins, hand-edit-free `reconcile`.
**Avoids:** Pitfall 10 — single machine source, don't wrap markers around the §1 prose table (reconcile would destroy it).

### Phase 5: Gardening — Unify & Coordinated Repin (GARD-01, GARD-02)
**Rationale:** The one deliberate breaking phase; consumes P2's unify list + P4's proven reconcile. The thing the additive-only channel structurally cannot do.
**Delivers:** Unify dispositions implemented, drift guard + Metalava rebaselined, migration map, one immutable tag (semver argues `v2.0.0`), both consumers repinned + Gate-1 verified.
**Avoids:** Pitfalls 5, 6, 7, 11 — atomic repin (no strand), line-by-line rebaseline review, registry integrity kept, human-gated tag/repin.

### Phase Ordering Rationale
- **P1 is the hard prerequisite** for P2 and P3 (both consume the tier vocabulary/litmus).
- **P2 (scope) + P4 (verification) gate P5** — the unify list is P5's backlog, the matrix drift flag is P5's no-strand pre-flight.
- **P3 is independent of P4** — both are hardening tracks, parallelizable and sequenceable ahead of P5.
- **Sequencing refinement to discuss:** running **P3 before P2/P4** lets their pure-doc commits land without `HUB_LANE_OVERRIDE`. The roadmap's numeric order still works (P2/P4 use the override until P3 lands). Either is invariant-safe — flag as a discuss decision.

### Research Flags

Phases likely needing deeper research/verification during planning:
- **Phase 1:** Confirm whether Metalava emits an overloaded ctor (→ lane-2 only) or rewrites the single ctor line (→ lane-3) for a defaulted-param addition on a Kotlin data class. Budget the `api.txt` regen + governance override either way.
- **Phase 3:** REPRODUCE the GOV-03 false-flag first — `verify-additive-diff.sh` already scopes to `src/main`, so the exact offending step (classifier, api-additive lane, or pre-commit) must be pinned before editing. Do not assume the fix locus.

Phases with well-documented patterns (lighter research):
- **Phase 2:** Doc-only audit against the registry SSOT; method (interface inventory) is established.
- **Phase 4:** Exact `repin_status.py` marker/table contract is verified against tool source.
- **Phase 5:** Mechanics (curation lane, rebaseline, repin ritual) are documented in `ECOSYSTEM.md §7` + `~/.claude/context/workflows/repin.md`; the *judgment* (which siblings fold) comes from P2.

## Confidence Assessment

| Area | Confidence | Notes |
|------|------------|-------|
| Stack | HIGH | Grounded in repo's own pinned versions, `api.txt`, tools, and external `repin_status.py` — cited, not invented. |
| Features | MEDIUM | Practices cross-corroborated across authoritative DS sources; their right-sized application to this two-consumer hub is opinionated synthesis. |
| Architecture | HIGH | All integration points verified against live source, tests, tooling, `api.txt`; two items flagged MEDIUM (P1 lane, GOV-03 locus). |
| Pitfalls | HIGH | Grounded in live code + the real stranded-consumer precedent in `ECOSYSTEM.md`. |

**Overall confidence:** HIGH

### Gaps to Address
- **P1 lane-2 vs lane-3 under Metalava defaulted-ctor emission** (MEDIUM): resolve by running `apiDump` in Phase 1 planning and inspecting the emitted ctor line. Either way the mitigation (curation commit + rebaseline) holds.
- **GOV-03 false-flag root cause not yet located** (MEDIUM): the guard already scopes to `src/main`, so a residual path or a mixed-commit artifact is suspected. Reproduce with a `.planning/`-only commit under the active hook before editing.
- **Semver for the Phase-5 break** (`v2.0.0` vs continued `v1.x`): a decision for the owner — the ecosystem has only ever cut additive `v1.x` minors.

## Sources

### Primary (HIGH confidence)
- Repo source: `ComponentRegistry.kt`, 9 `*FamilyScreen.kt` (53 call sites), `ComponentRegistryDriftGuardTest.kt`, `api.txt`, `build.gradle.kts`, `CLAUDE.md` — pinned versions, Entry shape, drift-guard machinery.
- `tools/classify-hub-change.sh`, `verify-additive-diff.sh`, `verify-api-additive.sh`, `hooks/pre-commit` — lane semantics, `--mode curation` / `HUB_LANE_OVERRIDE`, tag-baseline behavior.
- `~/.claude/context/deps/repin_status.py` — exact `repin-matrix` marker + table contract, reconcile overwrite semantics, `ValueError` on absent markers.
- `ECOSYSTEM.md` (§1 pins, §7 repin ritual, stranded-SB precedent), `.planning/PROJECT.md` / `ROADMAP.md` / `REQUIREMENTS.md`.

### Secondary (MEDIUM confidence)
- Design-system practice literature (Brad Frost interface inventory + recipe tiers, Rad UI three-layer rule, UXPin/Miro governance, Rangle Radius Tracker) — establishes the mature-org forms these deliverables scale *down* from.

### Tertiary (LOW confidence)
- Two-consumer right-sizing and the mapping onto this hub's specific phases — author synthesis grounded in the planning docs.

---
*Research completed: 2026-08-28*
*Ready for roadmap: yes*
