# Phase 5: Gardening — Unify & Coordinated Repin - Context

**Gathered:** 2026-09-01
**Status:** Ready for planning

<domain>
## Phase Boundary

Execute Phase 2's "unify" dispositions as unified components, rebaseline the registry drift guard +
Metalava `apiCheck` for the intentional break, cut a new immutable tag, and land it on both
SecondBrain and CalTracker via a single human-gated coordinated repin — each re-verified at Gate-1,
neither stranded.

</domain>

<decisions>
## Implementation Decisions

### Unify scope
- **D-01 [unify-scope]:** implement exactly Phase 2's dispositioned unify tuples 1:1, adding/removing no scope at execution time — re-analyzing overlaps desyncs the unify count from the audit and breaks the 1:1 mapping the `api.txt` rebaseline diff review depends on. _(provisional — refresh at execution; depends on Phase 2)_ _(source: ai-auto)_
- **D-02 [fold-mechanism]:** decide per fold from Phase 2's disposition wording — default to **remove entirely** for a `prune`, and **fold-then-demote to `INTENTIONALLY_UNREGISTERED` with a rationale (or remove)** for a `unify` — keeping registered-XOR-allowlisted true either way. The general mechanic (update the registry cell, `./gradlew apiDump` rebaseline with line-by-line review, commit via curation lane `HUB_LANE_OVERRIDE=3`) is settled. _(provisional — refresh at execution; depends on Phase 2)_ _(source: ai-auto)_

### Versioning
- **D-03 [semver]:** cut the gardening tag as **v2.0.0** (semver-correct major) — this is the first true break in an all-additive `v1.0.0→v1.10.0` history; a break under a non-breaking version lies to consumers and any future range-based resolution would silently pull it. The tag-cut itself stays human-gated regardless. _(source: human)_

### Coordinated repin
- **D-04 [repin-seam]:** the hub cuts the tag on `main` here; each consumer repin runs through that consumer's **own channel** (human-gated), staged so the ecosystem isn't "moved" until both are ready + Gate-1 re-verified — editing consumer files from this hub phase would violate sequential-in-hub. `repin_status.py reconcile` (Phase 4) proves both pins moved. _(source: ai-auto)_
- **D-05 [caltracker-catchup]:** CalTracker (at `v1.5.0`, 7 additive tags behind) gets an **intermediate catch-up repin to `v1.10.0` (Gate-1 verified)** before moving to the gardening tag — a single jump across `v1.6.0→v1.10.0` plus the break gives it a far wider blast radius than SecondBrain's single-tag move, raising the odds an unrelated regression strands it while SecondBrain passes. _(source: human)_

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Decisions & requirements
- `.planning/v1.0-DECISION-MAP.md` §Phase 5 — source of the five decisions above.
- `.planning/ROADMAP.md` §"Phase 5: Gardening — Unify & Coordinated Repin" — goal + 4 success criteria; depends on Phases 2 and 4.
- `.planning/REQUIREMENTS.md` — GARD-01, GARD-02.

### Upstream inputs & ritual
- `.planning/phases/02-coherence-audit/02-CONTEXT.md` + `docs/COHERENCE-AUDIT.md` (once written) — the unify work-order this phase implements 1:1.
- `.planning/phases/04-repin-bookkeeping-hardening/04-CONTEXT.md` — the reconcile tooling proving both pins moved.
- `CLAUDE.md` (repo root) §"Changes here ripple to every consumer — and shipping is human-gated" + §"Cross-repo work convention (sequential-in-hub)" — the tag/bump/deploy human-gate and no-consumer-worktrees rule.
- `ECOSYSTEM.md` §7 + `~/.claude/context/workflows/repin.md` — the full repin ritual.
- `CLAUDE.md` §"The invariants" — `ComponentRegistry` registered-XOR-`INTENTIONALLY_UNREGISTERED` guard + Metalava `apiCheck` the rebaseline must satisfy.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `./gradlew apiDump` — Metalava rebaseline for the intentional break.
- `repin_status.py reconcile` (Phase 4) — proves both consumer pins moved to the new tag.
- `INTENTIONALLY_UNREGISTERED` allowlist — the demote-with-rationale target for folded siblings.

### Established Patterns
- Curation lane commit via `HUB_LANE_OVERRIDE=3` for the registry/api changes.
- Sequential-in-hub: tag cut on hub `main`; consumer repins run in each consumer's own channel, human-gated.
- Staged coordinated repin — ecosystem not "moved" until both consumers pass Gate-1.

### Integration Points
- Registry cell edits → `apiDump` rebaseline (line-by-line diff review) → immutable `v2.0.0` tag → JitPack → consumer coordinate bumps (SecondBrain single-tag; CalTracker via `v1.10.0` catch-up hop first).

</code_context>

<specifics>
## Specific Ideas

CalTracker gets a staged two-step move (catch-up to v1.10.0 first, then the gardening tag); SecondBrain moves in a single tag. Neither consumer is "moved" until both are ready and re-verified at Gate-1.

</specifics>

<deferred>
## Deferred Ideas

None — this is the terminal phase; unify scope is bounded to Phase 2's dispositions.

</deferred>

---

## Runtime Decisions

*Refreshed during milestone execution as upstream phases produced real output. Both grounded in
Phase 2's shipped `docs/COHERENCE-AUDIT.md` (Unify Work-Order section).*

- **unify-scope** (provisional guess confirmed + made concrete, source: ai-auto, 2026-09-01):
  Implement exactly Phase 2's unify tuples **1:1**. The Unify Work-Order contains **exactly two**
  unify dispositions:
  - **WO-1** — Fold `FilterBar` into `ChipBar` as an optional expandable mode; retire `FilterBar`
    as a standalone registered entry. Family: Chips. Blast radius: SecondBrain 5 files each,
    CalTracker 0.
  - **WO-2** — Extract the shared header-`Row` + three-dot `DropdownMenu` + rename `AlertDialog`
    from `TextCardBottomSheet`/`ListCardBottomSheet` into a new shared composable; both sheets
    retained. Family: Sheets. Blast radius: SecondBrain 2 files each, CalTracker 0.
  - No other unify dispositions exist. Do **not** re-analyze — re-deriving desyncs the count from
    the audit and breaks the `api.txt` rebaseline 1:1 diff review.

- **fold-mechanism** (provisional guess confirmed, source: ai-auto, 2026-09-01): Decide per fold
  from Phase 2's disposition wording, keeping registered-XOR-allowlisted true.
  - **WO-1** folds `FilterBar`'s behavior INTO `ChipBar` (nullable expandable config + raw-content
    slot), then removes or demotes `FilterBar`'s registry cell to `INTENTIONALLY_UNREGISTERED` with
    a one-line reason (per-fold Phase-5 design call; SecondBrain uses `FilterBar` in 5 files, so
    preserve source-compat / deprecation as needed).
  - **WO-2** retains both sheets and only **adds** a new shared composable (private sub-part) —
    nothing is removed from any family list, so no demote/delete.
  - Settled general mechanic: update registry cell → `./gradlew apiDump` rebaseline with
    line-by-line review → commit via curation lane `HUB_LANE_OVERRIDE=3`.

---

*Phase: 5-Gardening — Unify & Coordinated Repin*
*Context gathered: 2026-09-01*
