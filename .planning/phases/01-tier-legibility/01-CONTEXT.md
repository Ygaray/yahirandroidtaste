# Phase 1: Tier Legibility - Context

**Gathered:** 2026-09-01
**Status:** Ready for planning

<domain>
## Phase Boundary

Make the hub's latent two-tier structure (primitives vs. patterns) explicit — carried on every
`ComponentRegistry.Entry`, surfaced in the `ExplorerActivity` gallery, and stated (with a per-tier
litmus) in a new design-intent doc — without touching component authorship.

</domain>

<decisions>
## Implementation Decisions

### Tier representation
- **D-01 [tier-field]:** required (no default) `tier` on `ComponentRegistry.Entry`, placed last — the Metalava/api.txt rewrite cost is paid either way, so defaulting only surrenders the compile-time "every entry is tiered" guarantee and risks LEG-01 going technically-green but semantically empty. The enum-on-Entry representation is settled; a parallel `Map<name,Tier>` is forbidden by the "entries alone is authoritative" invariant. — **Reversibility:** costly — the `tier` field lands in the published `api.txt`; removing/redefaulting it later is an API break requiring a Metalava rebaseline. _(source: ai-auto)_

### Gallery display
- **D-02 [gallery-display]:** badge on **both** the list row (`ComponentRow` in `ExplorerIndexScreen`) and the `ComponentDetailScreen` header, reusing Material3 `Badge`/`AssistChip` rather than a new primitive — one `ComponentRow` change covers search + all per-family lists; single-surface (detail only) fails "a developer browsing the catalog can see tiering without reading source." _(source: human)_

### Design-intent litmus
- **D-03 [litmus]:** anchor the per-tier litmus in `docs/DESIGN-INTENT.md` to the existing one-way-dependency / no-domain-assumption invariant — **primitive** = zero domain nouns in name+params, renders only caller-passed content; **pattern** = encodes an opinion/composition/interaction convention — a decidable test. An adjective-based ("simpler" vs "more opinionated") litmus lets the same component be tiered two ways, making Phase 2's altitude-mismatch findings un-defensible and Phase 3's tier-aware gate unwritable. Borderline components (`CardBase`, `ChipBar`, `HeatSwatch`) are tiered by applying this decidable test. _(source: ai-auto)_

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Decisions & requirements
- `.planning/v1.0-DECISION-MAP.md` §Phase 1 — source of the three decisions above (tier-field, gallery-display, litmus).
- `.planning/ROADMAP.md` §"Phase 1: Tier Legibility" — goal + 4 success criteria.
- `.planning/REQUIREMENTS.md` — LEG-01, LEG-02.

### Hub invariants (litmus anchor)
- `CLAUDE.md` (repo root) §"The invariants" — one-way dependency / no-domain-assumption, and the `ComponentRegistry` single-source-of-truth + drift-guard rule the tier field must not break.
- `API.md` — the public surface the new `tier` field is added to (distinct from the new `docs/DESIGN-INTENT.md`).

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- Material3 `Badge`/`AssistChip`: available via the Compose BOM already on the classpath — reuse for the tier badge, no new dependency.
- `ComponentRegistry` family lists: the authoritative enumeration each `Entry` now gains a `tier` on.
- `ExplorerIndexScreen` `ComponentRow` + `ComponentDetailScreen`: the two badge surfaces.

### Established Patterns
- Registered-XOR-`INTENTIONALLY_UNREGISTERED` invariant + the registry integrity/CATALOG drift test — the `tier` field addition must keep these green.
- Metalava `api.txt` gate — adding a public `tier` field requires an `apiDump` rebaseline.

### Integration Points
- `ComponentRegistry.Entry` (new field) → `api.txt` (rebaseline) → gallery surfaces (badge render).

</code_context>

<specifics>
## Specific Ideas

Reuse an existing chip/badge primitive for the badge rather than introducing a new one (design-conscious, component-unification preference).

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope.

</deferred>

---

*Phase: 1-Tier Legibility*
*Context gathered: 2026-09-01*
