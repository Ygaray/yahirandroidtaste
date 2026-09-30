# Phase 13: Catalog integrity & v2.4.0 ship - Context

**Gathered:** 2026-09-29
**Status:** Ready for planning

<domain>
## Phase Boundary

Register every new composable in the "Voice Command" family, prove the public API is strictly additive and engine-free, and correct the family-count doc drift — all VERIFIED. **This phase cuts NO tag.**

> **SPLIT (orchestrator ruling, INC-2026-09-30-01):** the `v2.4.0` cut moved to **Phase 14** (`14-CONTEXT.md`), because GSD execute-phase runs all a phase's plans before that phase's verification — bundling the immutable cut here would tag before Phase 13 is green (violating §11 step 1). Phase 13 = CAT-01/API-01/INV-01 (integrity + docs); Phase 14 = SHIP-01/SHIP-02 (the cut). The `[tag-procedure]` and `[no-stray-tag]` decisions below now live in Phase 14 — kept here only as context for what a green Phase 13 must hand off.

</domain>

<decisions>
## Implementation Decisions

Resolved in `ai` mode (`source: ai-auto`).

### family
- **D-01 [family]:** Register the voice cohort as ONE new tenth "Voice Command" `ComponentRegistry` family — a new entries slice appended to the concatenation, a new `ExplorerFamilies` const, and a new `ORDERED_KEYS` row — with each `Entry` carrying `tier = PATTERN` and a full 4-cell states matrix (rather than scattering into Cards/Sheets/Feedback). Matches the Tactile Foundation family-9 precedent.

### tag-procedure
- **D-02 [tag-procedure]:** `v2.4.0` is "cut" only after: (1) green FULL suite (`./gradlew testDebugUnitTest` — all four drift guards: registry, tier, domain-vocabulary, generated-symbol — plus zero-baseline detekt), (2) Metalava `apiCheck` additive vs `v2.3.0` (refresh + commit `api.txt`), (3) the tagged commit pushed, and (4) JitPack resolves `v2.4.0` from a clean Gradle cache. THEN message the orchestrator the full §11 ledger row (repo, tag, commit, coordinate, contents, evidence) — never self-write the §11 ledger (A14). — **Reversibility:** one-way — the tag is immutable once pushed; a defect needs a new `v2.4.1` patch tag + superseded row.

### doc-drift
- **D-03 [doc-drift]:** Correct the stale "seven families" wording to the post-cohort count (ten) in the 4 load-bearing files — root `CLAUDE.md`, `README.md`, `ComponentRegistry` KDoc, and `API.md` — as a P13 gate; treat the generated `.planning/codebase/*` map snapshots as out of scope (regenerated artifacts).

### Claude's Discretion
Do NOT edit the hardcoded publishing `version` in `build.gradle.kts` — JitPack derives the version from the resolved git ref; the tag is authoritative.

### Ship guards (research confident — not gray areas, but binding)
- Milestone close cuts NO git tag (SHIP-02): the ONLY tag this milestone produces is the `v2.4.0` release coordinate. A bare `v2.2` milestone-marker tag already leaked into the JitPack coordinate namespace at the v2.0 close — do not repeat it. Git tags in this repo ARE JitPack coordinates.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Milestone planning
- `.planning/ROADMAP.md` § Phase 13 — goal + success criteria
- `.planning/REQUIREMENTS.md` — CAT-01, API-01, INV-01, SHIP-01, SHIP-02
- `.planning/v2.4-DECISION-MAP.md` § Phase 13 — the decisions above

### Cross-repo tag protocol
- `~/Projects/Reusable/android/voice-action-engine/CROSS-REPO-SCOPE-CONTRACT.md` §11 + A12 (waiver) + A14 (orchestrator is sole ledger writer)
- `.planning/cross-repo/HANDOFF.md` §11 steps 1–4
- `~/.claude/context/workflows/repin.md` — repin ritual (for consumers, later wave)

### Research
- `.planning/research/PITFALLS.md` — additive-API/`copy()`-ABI; CATALOG-03 full-suite-only
- `.planning/research/ARCHITECTURE.md` — tenth family mechanics; doc-drift spots

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `explorer/ComponentRegistry.kt` (Entry shape + nine-list concatenation), `explorer/ExplorerIndexScreen.kt` (`ORDERED_KEYS` consts).
- `api.txt` (Metalava dump at repo root) + `tools/verify-api-additive.sh` + the additive-only pre-commit guard.
- `jitpack.yml` runs `publishReleasePublicationToMavenLocal`.

### Established Patterns
- Drift guards: `ComponentRegistryDriftGuardTest`, `DomainVocabularyDriftGuardTest`, `ComponentRegistryTierTest`, `GeneratedSymbolDriftGuardTest` — CATALOG-03 fails only in the FULL suite.
- Doc-drift live spots: `CLAUDE.md` line ~35, `README.md` line ~79, `ComponentRegistry.kt` KDoc lines ~88/92 (say "seven"); `API.md` says "nine".

### Integration Points
- Depends on Phases 10, 11, 12 having authored + registered their composables before the ship gate runs.

</code_context>

<specifics>
## Specific Ideas

"Cut on green verification" (human gate waived, A12) does not mean "push and walk away" — JitPack must actually build/resolve the tag before it counts as cut (§11 step 4), and the orchestrator re-checks.

</specifics>

<deferred>
## Deferred Ideas

Consumer repins (SecondBrain, CalTracker → `v2.4.0`) are Wave-1 work in the consumers' own channels, not this phase.

</deferred>

---

*Phase: 13-catalog-integrity-v2-4-0-ship*
*Context gathered: 2026-09-29*
