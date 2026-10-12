# Phase 24: Catalog, docs, Explorer demos & cut v2.6.0 - Context

**Gathered:** 2026-10-12
**Status:** Ready for planning

<domain>
## Phase Boundary

This phase runs the milestone-wide gate: registry, source-compatible API, docs and invariants. It adds the Explorer demos and captures TESTER screencaps for the owner. Then it cuts, pushes and JitPack-confirms the immutable `v2.6.0` tag and reports to the teamlead. Requirements: CAT-03, API-03, DOC-03, INV-03, DEMO-01, SHIP-04.

</domain>

<decisions>
## Implementation Decisions

### canary-form
- **D-01 [canary-form]:** The v2.5.1 call-site canary is a Kotlin test file under `src/test` (`V251CallSiteCanaryTest.kt`), compiled by `testDebugUnitTest`, with no gradle config change. It holds verbatim v2.5.1-shaped calls (SB + CT call sites + Explorer usage): positional where they were, named where they were, and trailing lambdas where used. It covers `OutcomeSheet`, every `VoiceOutcomeUiState` arm, `DynamicActionButton`, `SegmentedOptionSelector`, `SheetScaffold`, `ProviderKeyCard`, `UndoCenterScreen`, `HandledByUiModel` and `FailureActionUiModel`. _(ai-auto)_

### Cut gate (P2)
- **D-02 [gate]:** The gate before tagging:
  1. `./gradlew apiDump`, then `apiCheck` on the regenerated `api.txt`;
  2. `./gradlew testDebugUnitTest` (the full suite, including the canary, registry integrity and the CATALOG drift guard);
  3. `./gradlew detekt` at zero baseline;
  4. `bash tools/test/run-all.sh`;
  5. a review of the `api.txt` diff vs `v2.5.1`: no public symbol removed, no required param added, enum constants intact.

  `tools/verify-binary-abi.sh v2.5.1` runs for information only; exit 3 is expected and does not block. One gradle at a time; this session holds the build lock.

### cut-gating
- **D-03 [cut-gating]:** Cut and push tonight after the gates. The owner reviews the screencaps AFTER the cut, and an objection ships as `v2.6.1`. _(human: director ruling relayed via the teamlead, Yahir-approved, 2026-10-12)_ — **Reversibility:** one-way — tags are immutable JitPack coordinates.
- **D-04 [push]:** The push sequence, after D-02 passes:
  1. Create the annotated tag `v2.6.0` on the gated commit.
  2. Check `git log origin/main..v2.6.0` (a tag push publishes every unpushed commit reachable from it).
  3. Run exactly `git push origin main`, then `git push origin v2.6.0`. Never `--all` or `--tags`.

  The push OK expires at midnight 2026-10-12→13. If the push slips past it, stop before pushing and ask the teamlead. There is no milestone marker tag (`git.create_tag` false), and no consumer is repinned from the hub.
- **D-05 [report]:** Confirm the JitPack build (API status `ok` and the AAR/POM/module returning HTTP 200 for `com.github.Ygaray:yahirandroidtaste:v2.6.0`). Then report tag sha, commit, coordinate, JitPack status and gate results to the teamlead with `yrig send --reply none teamlead@vae-bilingual "…"`. The teamlead records §11.

### Demos & screencaps
- **D-06 [demos]:** Explorer demos per DEMO-01:
  - Receipt confirm: single open, batch collapsed, batch with an attention row;
  - a count editor with a disabled Pill segment and a warn `ToneCard`;
  - Success single/batch/all-undone;
  - Failure error/warn;
  - the ProviderKeyCard lifecycle: 3 providers, one saved, edit and clear flows;
  - an Undo Center group row.

  Each is reached by a registry render test. Demo copy is domain-neutral, with no consumer nouns.
- **D-07 [device]:** TESTER only: `yahirs-s22-ultra-2`, `adb -s R5CT10XNKQN`. Never the personal phone. Install via a throwaway same-package harness (mavenLocal AAR → `ExplorerActivity`), since the library has no APK (backlog 999.1).

### screencap-store
- **D-08 [screencap-store]:** Screencaps are committed under `.planning/phases/24-catalog-docs-explorer-demos-cut-v2-6-0/screencaps/` and listed in the SUMMARY for the owner. _(ai-auto)_

### Docs
- **D-09 [docs]:** `API.md` covers every new surface and gets a "Stable test tags" API section listing every Y-14 and `provider_key_*` tag. `INTEGRATION.md` covers wiring Receipt and the key lifecycle. Update the root `CLAUDE.md` "current release" line to `v2.6.0` at the cut.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Decision record & requirements
- `.planning/seams/v2.6.0-SKETCH.md` §2 Y-15, §3 P1-P3
- `.planning/REQUIREMENTS.md` — CAT-03, API-03, DOC-03, INV-03, DEMO-01, SHIP-04
- `.planning/ROADMAP.md` § Phase 24 (SC5 carries the push rules)
- `.planning/v2.6-DECISION-MAP.md` § Phase 24

### Release procedure
- `ECOSYSTEM.md` §7/§11; `~/.claude/context/workflows/repin.md` (read-only here: no repins); `tools/README-api-guard.md`
- `~/.claude/context/devices/common.md` — device resolution rules (TESTER only)
- `.planning/milestones/v2.5-phases/19-cut-v2-5-0/` — the prior cut-phase precedent

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `explorer/*FamilyScreen.kt` + `ComponentRegistry` (registration and demos); the registry render tests.
- `jitpack.yml` (`publishReleasePublicationToMavenLocal`). The internal publish version reads 1.10.0 on prior tags, which is expected.

### Integration Points
- The JitPack API: `https://jitpack.io/api/builds/com.github.Ygaray/yahirandroidtaste/v2.6.0`.

</code_context>

<specifics>
## Specific Ideas

- Review the long German labels on the demos at ~360dp.

</specifics>

<deferred>
## Deferred Ideas

- Backlog 999.1, a committed Gate-2 harness APK, stays deferred.

</deferred>
