# Phase 2: Coherence Audit - Context

**Gathered:** 2026-09-01
**Status:** Ready for planning

<domain>
## Phase Boundary

Produce a written `docs/COHERENCE-AUDIT.md` that enumerates all 9 registered families and
dispositions every overlap, near-duplicate sibling, and altitude (tier) mismatch as unify /
keep-with-rationale / prune — with the "unify" set forming the concrete work-order Phase 5 executes.

</domain>

<decisions>
## Implementation Decisions

### Tier source
- **D-01 [tier-source]:** consume Phase 1's ratified `entries[i].tier` labels + the `DESIGN-INTENT.md` litmus to name "altitude mismatch," rather than re-deriving tier ad hoc per component — re-deriving rests findings on an unratified taxonomy that can contradict the shipped gallery badge and makes the finding unfalsifiable. _(provisional — refresh at execution; depends on Phase 1)_ _(source: ai-auto)_

### Blast radius
- **D-02 [blast-radius]:** for each "unify" finding, pre-compute consumer blast radius by read-only grep of both on-disk consumer repos (`~/Projects/SecondBrain`, `~/Projects/CalTracker_Android`) now, recording call-sites per unify — read-for-blast-radius is sanctioned (only *editing* consumer files violates sequential-in-hub), and it gives Phase 5 a pre-flight against the documented stranded-consumer failure mode. _(source: ai-auto)_

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Decisions & requirements
- `.planning/v1.0-DECISION-MAP.md` §Phase 2 — source of the tier-source and blast-radius decisions.
- `.planning/ROADMAP.md` §"Phase 2: Coherence Audit" — goal + 4 success criteria; depends on Phase 1.
- `.planning/REQUIREMENTS.md` — AUD-01.

### Upstream inputs
- `.planning/phases/01-tier-legibility/01-CONTEXT.md` + Phase 1's `docs/DESIGN-INTENT.md` (once written) — the ratified tier labels + litmus this audit consumes.
- `CLAUDE.md` (repo root) §"The invariants" — the 9 visual families and the one-way-dependency litmus the altitude judgements rest on.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `ComponentRegistry`'s seven/nine family lists — the enumeration the audit walks.
- Phase 1's per-entry `tier` labels — the ratified taxonomy the audit references (not re-derives).

### Established Patterns
- Sequential-in-hub: read-only consumer greps are allowed; no consumer file edits from this hub phase.

### Integration Points
- Output `docs/COHERENCE-AUDIT.md` "unify" dispositions → Phase 5 `unify-scope` work-order (1:1).

</code_context>

<specifics>
## Specific Ideas

No specific requirements — open to standard approaches.

</specifics>

<deferred>
## Deferred Ideas

Call-site *editing* / actual unification is deferred to Phase 5 — this phase only records call-sites for blast-radius pre-flight.

</deferred>

---

*Phase: 2-Coherence Audit*
*Context gathered: 2026-09-01*
