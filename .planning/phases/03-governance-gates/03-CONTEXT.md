# Phase 3: Governance Gates - Context

**Gathered:** 2026-09-01
**Status:** Ready for planning

<domain>
## Phase Boundary

Harden the tooling substrate against future drift: a tier-aware contribution litmus, a
domain-vocabulary drift guard that flags (not forbids) new domain nouns, and a fix to the additive
pre-commit hook so `.planning/`/docs commits stop false-flagging as lane-2.

</domain>

<decisions>
## Implementation Decisions

### Pre-commit false-flag fix (GOV-03)
- **D-01 [gov03-fix]:** classify the commit's own staged delta (`git diff --cached` vs `HEAD`, ∩ `src/main`) — root cause reproduced: the hook classifies the whole working tree against the last release tag, so a legitimate post-tag src edit (`HeatSwatch.kt` in `5b01532`) is inherited by every later commit, including pure `.planning/` ones. Staged-delta is cleanest and re-flags only the introducing commit; the tag-baseline-restricted alternative still sees prior same-file rewrites. _(source: ai-auto)_

### Domain-noun guard semantics (GOV-02)
- **D-02 [gov02-semantics]:** "flag not forbid" = a **fail-until-allowlisted** JUnit test (forbid-until-acknowledged; cleared by adding the name + rationale to an `INTENTIONALLY_UNREGISTERED`-style allowlist) — audit-trailed and CI-enforced. An always-green report is ignorable and defeats "surface the coupling for review"; a hard permanent forbid would red-build day one on the ~10 grandfathered domain names. _(source: human)_
- **D-03 [gov02-detection]:** detect a "domain noun" via an **inverse allowlist** of the hub's own UI-primitive nouns (`Card`, `Chip`, `Sheet`, `Button`, `Bar`, `Swatch`, `Picker`…) — flag any net-new name whose head token falls outside it. Structural, ships no consumer terms in the hub; a consumer-term denylist would import the very domain knowledge the guard exists to prevent. _(source: ai-auto)_

### Contribution litmus location (GOV-01)
- **D-04 [litmus-location]:** extend Phase 1's `docs/DESIGN-INTENT.md` with a contribution-litmus section rather than adding a new `CONTRIBUTING.md` — P1 already commits to documenting the per-tier litmus there, so a separate file risks double-sourcing the tier prose; no `.github/` PR-template or CI-review surface exists to wire a checklist into. "Enforced where feasible" = the GOV-02 guard checks the strict-primitives half; the patterns-loose half stays prose. _(provisional — refresh at execution; depends on Phase 1)_ _(source: ai-auto)_

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Decisions & requirements
- `.planning/v1.0-DECISION-MAP.md` §Phase 3 — source of the four decisions above.
- `.planning/ROADMAP.md` §"Phase 3: Governance Gates" — goal + 4 success criteria; depends on Phase 1.
- `.planning/REQUIREMENTS.md` — GOV-01, GOV-02, GOV-03.

### Governance surfaces
- The additive-guard pre-commit hook (lane classifier) — the GOV-03 fix target; commit `5b01532` (`HeatSwatch.kt`) is the reproduced false-flag seed.
- `CLAUDE.md` (repo root) §"The invariants" — the `INTENTIONALLY_UNREGISTERED` allowlist pattern GOV-02 reuses, and the one-way / no-domain-assumption invariant the guard defends.
- `docs/DESIGN-INTENT.md` (from Phase 1) — where the contribution litmus is appended.
- Memory `[[hub-additive-guard-blocks-planning-docs]]` — the `HUB_LANE_OVERRIDE` false-flag this phase eliminates.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `INTENTIONALLY_UNREGISTERED` allowlist convention — reused for the GOV-02 acknowledge-to-clear mechanism.
- Existing JUnit / Robolectric test infra — hosts the fail-until-allowlisted guard test (no new dependency).

### Established Patterns
- ComponentRegistry drift guard uses source-text scanning — the GOV-02 inverse-allowlist head-token scan follows the same shape.
- `HUB_LANE_OVERRIDE=2` bypass — the GOV-03 fix removes the need for it on planning/doc commits.

### Integration Points
- Pre-commit hook staged-delta classifier (`git diff --cached` ∩ `src/main`).
- GOV-02 JUnit guard → CI red build until allowlisted.

</code_context>

<specifics>
## Specific Ideas

Build must go red until a net-new domain-noun name is explicitly allowlisted with rationale — no silent/always-green warning.

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope.

</deferred>

---

*Phase: 3-Governance Gates*
*Context gathered: 2026-09-01*
