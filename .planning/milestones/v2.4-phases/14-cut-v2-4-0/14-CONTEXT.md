# Phase 14: Cut v2.4.0 - Context

**Gathered:** 2026-09-29
**Status:** Ready for planning

<domain>
## Phase Boundary

With Phase 13 green, cut the `v2.4.0` tag via the §11 protocol, confirm JitPack resolves it, and message the orchestrator the §11 ledger row. This is the ONLY tag this milestone produces — nothing else. Split out of Phase 13 (orchestrator ruling) because GSD execute-phase runs all a phase's plans before that phase's verification, so bundling the immutable cut with catalog/doc work would tag before green (violating §11 step 1).

</domain>

<decisions>
## Implementation Decisions

Moved here from the Phase 13 split (orchestrator R1 ruling, INC-2026-09-30-01). `source: ai-auto` unless noted.

### tag-procedure
- **D-01 [tag-procedure]:** `v2.4.0` is "cut" ONLY after §11 steps 1–4 all hold: (1) green FULL suite (`./gradlew testDebugUnitTest` — all four drift guards: registry, tier, domain-vocabulary, generated-symbol — plus zero-baseline `./gradlew detekt`), (2) Metalava `./gradlew apiCheck` additive vs `v2.3.0` (with `api.txt` refreshed + committed), (3) the tagged commit pushed, and (4) JitPack resolves `v2.4.0` from a clean Gradle cache. THEN message the orchestrator the full §11 ledger row (repo, tag, commit, coordinate, contents, evidence). NEVER self-write the §11 ledger (A14). Tag-cut human gate is WAIVED (A12, Yahir-confirmed in-session). — **Reversibility:** one-way — the tag is immutable once pushed; a defect needs a new `v2.4.1` patch tag + a superseded ledger row.

### no-stray-tag
- **D-02 [no-stray-tag]:** Milestone close cuts NO git tag beyond the `v2.4.0` release coordinate. `git.create_tag` is already set to `false` in `.planning/config.json` (committed `b20d79d`) as the mechanical guard. A bare `v2.2` milestone-marker tag already leaked into the JitPack coordinate namespace at the v2.0 close — do not repeat it (SHIP-02, INC-2026-09-30-01). Git tags in this repo ARE JitPack coordinates.

### Claude's Discretion
"Cut on green verification" (gate waived) does not mean "push and walk away" — JitPack must actually build/resolve the tag before it counts as cut (§11 step 4), and the orchestrator re-checks (A14). Do NOT edit the hardcoded publishing `version` in `build.gradle.kts` — JitPack derives it from the resolved git ref.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Cross-repo tag protocol
- `~/Projects/Reusable/android/voice-action-engine/CROSS-REPO-SCOPE-CONTRACT.md` §11 + A12 (waiver) + A14 (orchestrator is sole ledger writer)
- `.planning/cross-repo/HANDOFF.md` §11 steps 1–4
- `~/.claude/context/workflows/repin.md` — repin ritual (for consumers, Wave 1)

### Milestone planning
- `.planning/ROADMAP.md` § Phase 14
- `.planning/REQUIREMENTS.md` — SHIP-01, SHIP-02
- `.planning/phases/13-catalog-integrity-v2-4-0-ship/13-CONTEXT.md` — the green Phase 13 this gates on

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `jitpack.yml` runs `publishReleasePublicationToMavenLocal` (what JitPack builds).
- `tools/verify-api-additive.sh` + the additive-only pre-commit guard.

### Integration Points
- Depends on a fully green Phase 13 (registration + additive API + docs). No new code — this phase is the tag + verification + orchestrator message.

</code_context>

<specifics>
## Specific Ideas

Orchestrator (`yahir-gsd-control-plane-f2`) re-checks JitPack after the ledger row and broadcasts to peers (SB, CT). Consumers repin to `v2.4.0` in Wave 1, their own channels.

</specifics>

<deferred>
## Deferred Ideas

Consumer repins (SecondBrain, CalTracker → `v2.4.0`) are Wave-1 work in the consumers' own channels.

</deferred>

---

*Phase: 14-cut-v2-4-0*
*Context gathered: 2026-09-29*
