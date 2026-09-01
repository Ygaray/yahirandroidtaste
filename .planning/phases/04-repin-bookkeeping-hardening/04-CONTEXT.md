# Phase 4: Repin Bookkeeping Hardening - Context

**Gathered:** 2026-09-01
**Status:** Ready for planning

<domain>
## Phase Boundary

Make `ECOSYSTEM.md` carry the machine-owned `repin-matrix` markers so `repin_status.py reconcile`
runs without hand edits — proving the true pin state for both consumers and closing
`INC-2026-08-28-03`.

</domain>

<decisions>
## Implementation Decisions

### Seeded consumer row name
- **D-01 [consumer-name]:** seed the row name as `CalTracker_Android` (matches `render_matrix_block`'s output exactly → byte-stable first run) rather than `CalTracker` — `reconcile` always writes the checkout dir name, so seeding `CalTracker` makes run-one report a one-time rename a byte-stability verifier could misread as a failure. The matrix is a *separate* machine-owned `<!-- repin-matrix -->` block, never wrapping the §1 human prose table `reconcile` would overwrite (settled). _(source: ai-auto)_

### Incident closure demonstration
- **D-02 [inc-closure]:** confirm `INC-2026-08-28-03`'s worded acceptance text via the `incident` tooling / control-plane log first, then map the reconcile evidence 1:1 to it before marking closed — rather than treating a clean `reconcile` run as self-sufficient closure. The incident lives in the control-plane log, not this repo, so its acceptance condition must be read and matched explicitly. _(source: ai-auto)_

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Decisions & requirements
- `.planning/v1.0-DECISION-MAP.md` §Phase 4 — source of the consumer-name and inc-closure decisions.
- `.planning/ROADMAP.md` §"Phase 4: Repin Bookkeeping Hardening" — goal + 3 success criteria.
- `.planning/REQUIREMENTS.md` — REPIN-01.

### Repin surfaces
- `ECOSYSTEM.md` — gains the `<!-- repin-matrix -->` machine block (distinct from the §1 human prose table).
- `repin_status.py` (`reconcile` / `render_matrix_block` / `validate`) — the tooling this phase feeds.
- `INC-2026-08-28-03` (control-plane log, via the `incident` tooling) — the incident whose acceptance text closure maps to.
- `~/.claude/context/workflows/repin.md` — the repin ritual context.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `repin_status.py reconcile` + `render_matrix_block` — existing Python tooling; no new dependency, this phase only seeds the markers it consumes.

### Established Patterns
- Machine-owned marker block separated from human prose (the matrix never overwrites §1).
- Byte-stability verification of an idempotent first `reconcile` run.

### Integration Points
- `ECOSYSTEM.md` `<!-- repin-matrix -->` block ↔ `repin_status.py reconcile`.
- Reconcile evidence ↔ `INC-2026-08-28-03` acceptance criteria (1:1 mapping).

</code_context>

<specifics>
## Specific Ideas

First `reconcile` run must be byte-stable (no hand edits, no one-time rename) — seed row names to match the renderer's output exactly.

</specifics>

<deferred>
## Deferred Ideas

The actual coordinated repin execution is Phase 5 — this phase only hardens/proves the reconcile bookkeeping ahead of it.

</deferred>

---

*Phase: 4-Repin Bookkeeping Hardening*
*Context gathered: 2026-09-01*
