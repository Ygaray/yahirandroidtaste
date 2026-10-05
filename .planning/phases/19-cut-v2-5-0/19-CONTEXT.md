# Phase 19: Cut v2.5.0 - Context

**Gathered:** 2026-10-05
**Status:** Ready for planning

<domain>
## Phase Boundary

Cut the immutable `v2.5.0` JitPack tag via the §11 protocol, isolated so the cut follows a green Phase 18. Covers SHIP-03.

</domain>

<decisions>
## Implementation Decisions

### tag-gate
- **D-01 [tag-gate]:** Yahir **GRANTED the A12 tag-cut waiver for this effort's YAT tag**, directly in-session (2026-10-05). The agent MAY cut `v2.5.0` autonomously once Phase 18 verification is green (full §11 battery on the exact tagged HEAD, API strictly additive, pushed, JitPack-resolvable) — no further human gate required for this tag. Resolution (human). — **Reversibility:** one-way — a cut immutable public JitPack tag cannot be rescinded; a mistake is remedied only by a corrective patch tag. This waiver applies to v2.5.0 within the vae-bilingual effort, not a standing change to the repo's human-gated shipping policy.

### cut-mechanism
- **D-02 [cut-mechanism]:** Cut via the orchestrator's `xrepo build`, one repo at a time (orchestrator directive). Resolution (human). Relay the full §11 ledger row to `yahir-gsd-control-plane-6e`; never write the §11 ledger here. `git.create_tag` stays false (no stray marker tag — SHIP-02 guard).

### Claude's Discretion
- If `xrepo build` is unavailable/undefined at cut time, fall back to the in-repo manual annotated-tag+push precedent (v2.2.0/v2.4.1) after confirming with the orchestrator.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Decision source
- `.planning/v2.5-DECISION-MAP.md` § Phase 19
- `.planning/cross-repo/HANDOFF.md` — §11 steps 1-4, A12/A14, v2.4.1 cut precedent, protocol-notes addendum
- `.planning/cross-repo/RECONVENE-BRIEF-R-v1.1.md` (cb5f047) §7

### Requirements / roadmap
- `.planning/REQUIREMENTS.md` — SHIP-03
- `.planning/ROADMAP.md` § Phase 19

### Ship mechanics
- `ECOSYSTEM.md` §11 ledger + the v2.2.0/v2.4.0/v2.4.1 cut records; `jitpack.yml`; `.planning/config.json` (`git.create_tag` false)

</canonical_refs>

<code_context>
## Existing Code Insights

### Established Patterns
- §11 ritual: green governance battery on the exact tagged HEAD (`testDebugUnitTest detekt apiCheck metalavaCheckCompatibilityDebug publishReleasePublicationToMavenLocal` + `tools/test/run-all.sh`, gated on HEAD identity), API additive, annotated tag pushed, JitPack resolution confirmed (status:ok / isTag:true / commit match), THEN relay the ledger row to the orchestrator.

### Integration Points
- `xrepo build` is control-plane state (lives in `~/Projects/yahir-agentic-tools/yahir-gsd-control-plane/`), not defined in this repo — confirm invocation/sequencing with the orchestrator at cut time.

</code_context>

<specifics>
## Specific Ideas

Even with the A12 waiver granted, the cut still follows a GREEN Phase 18 and the full §11 verification battery — the waiver removes the human gate, not the verification gate.

</specifics>

<deferred>
## Deferred Ideas

Consumer repins (SB/CT → v2.5.0) are Wave-1, each consumer's own channel — not this phase.

</deferred>

---

*Phase: 19-cut-v2-5-0*
*Context gathered: 2026-10-05*
