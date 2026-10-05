# Phase 18: Catalog integrity + API dump + docs - Context

**Gathered:** 2026-10-05
**Status:** Ready for planning

<domain>
## Phase Boundary

After the three code phases land, keep the catalog coherent and the ship additive: ComponentRegistry drift guard green in the FULL suite, regenerate the Metalava `api.txt` and pass the additive gate, update `API.md`/`INTEGRATION.md`, preserve the one-way-dependency invariant + detekt zero-baseline. Cuts NO tag. Covers CAT-02, API-02, DOC-02, INV-02.

</domain>

<decisions>
## Implementation Decisions

### api-gate
- **D-01 [api-gate]:** API-02's "verify-api-additive passes" is read as "Metalava `apiCheck` green + `api.txt` regenerated", with the raw-line `verify-api-additive.sh` exit-3 handled via `HUB_LANE_OVERRIDE=3` (same resolution as Phase 15 [api-guard], human). Re-confirm empirically with `./gradlew apiDump && ./gradlew apiCheck`. _(source: ai-auto, consistent with the human P15 decision)_

### docs-depth
- **D-02 [docs-depth]:** `API.md` gets per-component table rows for the ~13 new params/fields plus a v2.5.0 "appended defaulted parameter" compatibility-scope paragraph (mirrors the `showTagColors` note); `INTEGRATION.md` gets a light localization-override note, not full per-param examples. _(source: ai-auto)_

### invariant-check
- **D-03 [invariant-check]:** INV-02's one-way-dependency is verified by an explicit import-inspection step over the Phase 15–17 diffs at review (there is no automated forbidden-import test); detekt stays zero-baseline because all new params are defaulted (`ignoreDefaultParameters: true`, threshold 18). _(provisional — refresh at execution; depends on Phase 17)_ _(source: ai-auto)_

### Claude's Discretion
- No new `ComponentRegistry` entry or `INTENTIONALLY_UNREGISTERED` allowlist line is needed (phases 15–17 add no new public top-level `@Composable`; model fields are out of registry scope). Confirm by running the FULL `testDebugUnitTest`, not a scoped subset.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Decision source
- `.planning/v2.5-DECISION-MAP.md` § Phase 18
- `.planning/cross-repo/RECONVENE-BRIEF-R-v1.1.md` (cb5f047) §1 (Phase 4)

### Requirements / roadmap
- `.planning/REQUIREMENTS.md` — CAT-02, API-02, DOC-02, INV-02
- `.planning/ROADMAP.md` § Phase 18

### Guards / docs
- `tools/verify-api-additive.sh`, `tools/classify-hub-change.sh`, `tools/hooks/pre-commit`, `tools/README-api-guard.md`
- `api.txt`; `build.gradle.kts` (metalava apiDump/apiCheck); `config/detekt-compose.yml`, `config/detekt-baseline.xml`
- `API.md` (§ component tables + the showTagColors compatibility note), `INTEGRATION.md`
- `src/test/.../explorer/ComponentRegistryDriftGuardTest.kt` — full-suite drift guard (@Composable-only; model/ out of scope)

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `API.md:198-199` — the `showTagColors` compatibility-scope paragraph to mirror for v2.5.0.

### Established Patterns
- Metalava value-class blind spot (`tools/README-api-guard.md:90-110`) does NOT bite this milestone — every new field/param is String/Boolean/enum/lambda, none value-class-typed.

### Integration Points
- api.txt surfaces that will be rewritten: the `ApproachLadderCard` method line + the `ctor`/`copy` lines of `HandledByUiModel`, `FailureActionUiModel`, `VoiceOutcomeUiState.Failure`, `ProposedItemUiModel`.

</code_context>

<specifics>
## Specific Ideas

No specific requirements — standard catalog/API/docs hygiene.

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope.

</deferred>

---

*Phase: 18-catalog-integrity-api-dump-docs*
*Context gathered: 2026-10-05*
