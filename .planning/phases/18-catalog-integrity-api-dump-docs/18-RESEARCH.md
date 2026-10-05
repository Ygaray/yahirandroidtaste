# Phase 18: Catalog integrity + API dump + docs - Research

**Researched:** 2026-10-05
**Domain:** Gradle/Metalava API-gate verification, ComponentRegistry drift guards, Markdown API docs for a Jetpack Compose library (no production code changes)
**Confidence:** HIGH (every gate below was run live this session against HEAD `fa6b7ac`; the only `[ASSUMED]` items are logged in the Assumptions Log)

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

- **D-01 [api-gate]:** API-02's "verify-api-additive passes" is read as "Metalava `apiCheck` green + `api.txt` regenerated", with the raw-line `verify-api-additive.sh` exit-3 handled via `HUB_LANE_OVERRIDE=3` (same resolution as Phase 15 [api-guard], human). Re-confirm empirically with `./gradlew apiDump && ./gradlew apiCheck`. _(source: ai-auto, consistent with the human P15 decision)_
- **D-02 [docs-depth]:** `API.md` gets per-component table rows for the ~13 new params/fields plus a v2.5.0 "appended defaulted parameter" compatibility-scope paragraph (mirrors the `showTagColors` note); `INTEGRATION.md` gets a light localization-override note, not full per-param examples. _(source: ai-auto)_
- **D-03 [invariant-check]:** INV-02's one-way-dependency is verified by an explicit import-inspection step over the Phase 15-17 diffs at review (there is no automated forbidden-import test); detekt stays zero-baseline because all new params are defaulted (`ignoreDefaultParameters: true`, threshold 18). _(provisional - refresh at execution; depends on Phase 17)_ _(source: ai-auto)_
- **Runtime Decision (invariant-check, re-resolved 2026-10-05, source: ai-auto):** explicit import-inspection step over all src/main diffs v2.4.1..HEAD (Phases 15-17 plus binary-compat quicks 261005-dmc/e2e); no new test this milestone. Pre-check at refresh: 12 files changed, no +import outside androidx/kotlin/java/own package.

### Claude's Discretion

- No new `ComponentRegistry` entry or `INTENTIONALLY_UNREGISTERED` allowlist line is needed (phases 15-17 add no new public top-level `@Composable`; model fields are out of registry scope). Confirm by running the FULL `testDebugUnitTest`, not a scoped subset.

### Deferred Ideas (OUT OF SCOPE)

None - discussion stayed within phase scope.

**Also out of scope for this phase (from ROADMAP / Phase 19 CONTEXT):** cutting any tag (SHIP-03), `tools/verify-binary-abi.sh`, the pre-commit hook edit, and the `tools/README-api-guard.md` rewrite are all Phase 19 D-03 work. Phase 18 must not do them.
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| CAT-02 | Every new/changed public composable stays registered in `ComponentRegistry` (or allowlisted); the CATALOG drift guard is green in the full suite. | No new public top-level composable exists since v2.4.1 (only four `@Deprecated(HIDDEN)` same-name overloads); the guard is a name-set diff so it stays green. Full clean suite run: 751 tests, 0 failures. See Finding 1. |
| API-02 | Public API strictly additive vs `v2.4.x`; `api.txt` regenerated and `tools/verify-api-additive.sh` passes. | `apiDump` idempotent vs committed `api.txt`; swap-baseline `apiCheck` against v2.4.1 AND v2.4.0 `api.txt` green; the raw-line script exits 3 with exactly 10 superseded lines (allowlist in Finding 2). |
| DOC-02 | `API.md` / `INTEGRATION.md` updated for the new label params, Failure enrichment, and router toggle. | API.md has NO "Voice Command" section at all (sections stop at 9). Gap analysis and exact new-surface inventory in Finding 3. |
| INV-02 | One-way-dependency preserved; detekt zero-baseline green. | Import inspection over all 12 changed `src/main` files returns zero foreign imports; build files untouched; detekt green with empty baseline. Finding 4. |
</phase_requirements>

## Summary

Phase 18 is a **verification-plus-documentation gate, not a feature phase**. Nothing in `src/main` needs to change. I ran every gate live against HEAD this session: `apiCheck` and `detekt` are green; `apiDump` regenerates a byte-identical `api.txt`; swap-baseline `apiCheck` against both the released `v2.4.1` and `v2.4.0` `api.txt` is green; `metalavaCheckCompatibilityDebug` is green; a clean, uncached `testDebugUnitTest` ran 751 tests with 0 failures/errors (22 pre-existing skips); `tools/test/run-all.sh` passes 4/4 suites. The 12 changed `src/main` files add no import outside `androidx`/`kotlin`/`java`/the library's own package, and no build file changed since `v2.4.1`.

The real work is **DOC-02**. `API.md` already carries a v2.5.0 compatibility paragraph and a "binary-compatibility rule" section (added by Phases 15 and quick 261005-eyu), but it has **no "10. Voice Command" per-component table** (its numbered sections stop at "9. Tactile Foundation"; the family only appears as a count row in "Surface at a glance"). The existing v2.5.0 paragraph also only covers the label params (VI18N-01..04): it does not mention `FailureActionUiModel.role`, `VoiceOutcomeUiState.Failure.body` / `semanticsPrefix`, the router toggle (`router`, `onRouterChange`, `routerOnLabel`, `routerOffLabel`), or the Phase 16 rung-selection semantics. `INTEGRATION.md` has no voice or localization content.

One genuine finding the planner must handle: **`ProposedItemUiModel(id, title) { ... }` (a trailing-lambda call, the v2.4.x shape for `trailingContent`) no longer compiles** because the appended `removeContentDescription: String` is now the last parameter. I reproduced the compile error. It is the exact same hazard class as the documented v2.3.0 `showTagColors` caveat, and Phase 15's VERIFICATION wrongly states trailing-lambda shapes still bind. Both known consumers use the named form, so nothing breaks today, but API.md must say so.

**Primary recommendation:** Two plans mirroring v2.4 Phase 13 split by concern: Plan 01 = DOC-02 (API.md "10. Voice Command" section + extended v2.5.0 compat paragraph incl. the trailing-lambda caveat, light INTEGRATION.md note); Plan 02 (last, on final HEAD) = verification battery that captures CAT-02 / API-02 / INV-02 into an `18-SHIP-GATE-EVIDENCE.md` Phase 19 consumes. Cut NO tag, touch NO `src/main`.

## Architectural Responsibility Map

This phase has no runtime tiers (it ships no feature). The ownership that matters is which *gate* owns which claim:

| Capability | Primary Owner | Secondary | Rationale |
|------------|--------------|-----------|-----------|
| Catalog coverage (every public composable registered/allowlisted) | `ComponentRegistryDriftGuardTest` (JUnit source-text scan) | `ComponentRegistry.INTENTIONALLY_UNREGISTERED` | Single source of truth + drift guard per root CLAUDE.md |
| Source-level API additivity | Metalava `apiCheck` / swap-baseline `metalavaCheckCompatibilityRelease` | `tools/verify-api-additive.sh` (advisory, raw-line) | Metalava models Kotlin signatures; the raw-line script false-positives on in-place appends (D-01) |
| Binary-level API additivity | `javap` descriptor diff (Phase 19 D-03 script) | quick 261005-e2e proven command | NOT a Phase 18 gate; Phase 18 may re-run as informational only |
| Public-surface documentation | `API.md` / `INTEGRATION.md` | `ECOSYSTEM.md` (Phase 19 ledger) | Human-readable contract for consumers |
| One-way dependency | Import inspection (manual, per D-03) + `build.gradle.kts` unchanged | `DomainVocabularyDriftGuardTest` (names only) | No automated forbidden-import test exists; none to be added this milestone |
| Style / complexity | detekt, zero baseline | `config/detekt-compose.yml` | `ignoreDefaultParameters: true` |

## Standard Stack

No external package is installed or added. The "stack" is the repo's existing gate tooling.

### Core

| Tool | Version | Purpose | Why Standard |
|------|---------|---------|--------------|
| Gradle wrapper | 9.4.1 | Runs all gates | Repo wrapper; output of `./gradlew` shows `docs.gradle.org/9.4.1` [VERIFIED: gradle run output this session] |
| Metalava via `me.tylerbwong.gradle.metalava` | 0.5.0 | `apiDump` / `apiCheck` aliases over `metalavaGenerateSignatureRelease` / `metalavaCheckCompatibilityRelease` | Only mechanism that works on AGP 9 + built-in Kotlin [CITED: tools/README-api-guard.md "ABI-dump mechanism" section] |
| detekt | repo-configured | Static analysis, zero baseline | `config/detekt-baseline.xml` is `<CurrentIssues/>` [VERIFIED: cat config/detekt-baseline.xml this session] |
| JUnit + Robolectric/Compose UI tests | repo-configured | `testDebugUnitTest` (751 tests) | Existing suite; includes the 4 drift guards |
| `tools/*.sh` + `tools/test/run-all.sh` | repo | Additive-lane guards and their self-tests | 4 suites, all PASS this session |

### Supporting

| Tool | Purpose | When to Use |
|------|---------|-------------|
| `git show <tag>:api.txt` swap | The authoritative baseline proof (v2.4 Phase 13 and Phase 15-03 precedent) | API-02 evidence; always restore with `git checkout -- api.txt` |
| `javap -public -s` AAR diff | Binary descriptor proof | Informational only here; formal script is Phase 19 |

### Alternatives Considered

| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Swap-baseline `apiCheck` | Literal clean `verify-api-additive.sh` exit 0 | Unachievable by construction for in-place appends (D-01 already decided) |
| Manual import inspection | New automated forbidden-import test | D-03 / runtime decision: explicitly NOT this milestone |

**Installation:** none. **Version verification:** not applicable (no registry packages). 

## Package Legitimacy Audit

No external packages are installed or recommended in this phase. `build.gradle.kts`, `gradle/`, `settings.gradle.kts`, `jitpack.yml` and `config/` are byte-unchanged since `v2.4.1` [VERIFIED: `git diff --stat v2.4.1..HEAD -- build.gradle.kts gradle/ settings.gradle.kts jitpack.yml config/` printed nothing].

**Packages removed due to [SLOP] verdict:** none
**Packages flagged as suspicious [SUS]:** none

## Architecture Patterns

### Phase flow (what produces what)

```
 Phases 15/16/17 (done, on main)            Phase 18 (this phase)                     Phase 19 (later)
 ┌──────────────────────────┐   ┌───────────────────────────────────────┐   ┌─────────────────────────┐
 │ src/main + src/test      │   │ Plan 01 (docs)                        │   │ verify-binary-abi.sh    │
 │ api.txt (already         │──▶│  API.md  +"10. Voice Command" section │   │ pre-commit hook edit    │
 │ regenerated per commit)  │   │  API.md  extend v2.5.0 compat para    │   │ tag v2.5.0 via xrepo    │
 │ API.md (partial: label   │   │  INTEGRATION.md light i18n note       │   └────────────▲────────────┘
 │ params + binary rule)    │   └──────────────────┬────────────────────┘                │
 └──────────────────────────┘                      │ (docs first so evidence is on final HEAD)
                                                   ▼                                     │
                              ┌───────────────────────────────────────┐                  │
                              │ Plan 02 (verification, final HEAD)    │                  │
                              │  full clean testDebugUnitTest (CAT-02)│                  │
                              │  apiDump idempotent + swap-baseline   │  18-SHIP-GATE-   │
                              │  apiCheck v2.4.1 (+v2.4.0) (API-02)   │  EVIDENCE.md ────┘
                              │  raw-line script exit-3 allowlist     │
                              │  import inspection + detekt (INV-02)  │
                              └───────────────────────────────────────┘
```

### Recommended plan structure

Mirror v2.4 Phase 13 (`13-01-PLAN.md`: verification tracer task + doc task + `13-SHIP-GATE-EVIDENCE.md`) but split because DOC-02 is real writing here, unlike Phase 13's family-count wording fix:

- **18-01 (wave 1, docs):** API.md + INTEGRATION.md. Docs are test-neutral: no test reads `API.md`/`INTEGRATION.md` content (only comments mention them) [VERIFIED: `grep -rln "API.md|README.md|INTEGRATION.md|CATALOG" src/test` matched only `ShowTagColorsSourceCompatTest.kt` and `ComponentRegistryDriftGuardTest.kt`, both comment-only mentions].
- **18-02 (wave 2, depends on 18-01):** verification battery + `18-SHIP-GATE-EVIDENCE.md` with the literal `PREFIX: verdict` per-command convention Phase 13 used (`CAT-02 PASS: ...`, `API-02 PASS: ...`, `INV-02 PASS: ...`, `Restore Confirmation`).

### Anti-Patterns to Avoid

- **Trusting a cached test run as evidence.** `./gradlew testDebugUnitTest` returned `FROM-CACHE` in 4 s for me. Use `./gradlew cleanTestDebugUnitTest testDebugUnitTest --no-build-cache` (73 s) for the CAT-02 evidence.
- **Running `verify-api-additive.sh` with an absolute `API_FILE`.** See Pitfall 1.
- **Using `INTENTIONALLY_UNREGISTERED` or a registry edit to "fix" anything.** None is needed. Adding an entry for a hidden shim would be wrong.
- **Doing Phase 19's work early** (binary-abi script, hook change, README-api-guard, API.md wording "It becomes `tools/verify-binary-abi.sh` at the v2.5.0 cut", CLAUDE.md edits). Leave the F3 wording untouched; CLAUDE.md edit is "pending Yahir" per STATE.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| API-02 proof | A bespoke signature differ | `git show v2.4.1:api.txt > api.txt; ./gradlew apiCheck; git checkout -- api.txt` | Metalava is the authority on Kotlin-level compat; precedent in Phase 13 and 15-03 |
| Forbidden-import check | A new Gradle/JUnit guard | One `git diff v2.4.1..HEAD -U0 -- src/main \| grep '^+import '` filter (command in Validation Architecture) | D-03 rules a new test out of this milestone |
| Registry coverage | A hand-count of composables | `ComponentRegistryDriftGuardTest` in the unscoped suite | Single source of truth |
| Doc param tables | Re-deriving signatures from memory | Copy from `api.txt` / source KDoc (values quoted below) | A paraphrased default fails reviewers; defaults are English literals that must match |

**Key insight:** every gate already exists and passes. The planner's job is to *capture evidence* and *write docs*, not to build tooling.

## Runtime State Inventory

Not a rename/refactor/migration phase. Omitted.

## Findings

### Finding 1 - CAT-02: registry is clean; no registry edit

- Registered composables = 61, intentionally-unregistered = 5, total 66, matching API.md's "61 registered ... 66 public composables total" [VERIFIED: per-family `ComponentRegistry.Entry(` counts this session: Cards 11, Chips 5, Sheets 18, ButtonsFab 4, Pickers 6, Feedback 3, EmptyState 1, Progress 4, Tactile 4, Voice 5 = 61]. The Voice family still has exactly 5 entries.
- Public top-level composables added by `v2.4.1..HEAD` in `src/main`: only four hidden shims, each preceded by the verbatim line `@Deprecated("Binary compatibility with v2.4.x", level = DeprecationLevel.HIDDEN)` then `@Composable` then `fun ApproachLadderCard(` / `fun ClarificationBar(` / `fun ModelSelectCard(` / `fun ProviderKeyCard(`; plus one `private fun ApproachLadderCardRouterFixture(` in `explorer/` [VERIFIED: `git diff v2.4.1..HEAD -U0 -- src/main | grep` output this session].
- `ComponentRegistryDriftGuardTest` computes `scannedComposableNames - registeredNames - allowlistedNames` over a **set of names** [VERIFIED: Read of ComponentRegistryDriftGuardTest.kt lines 1-130]. The four shims reuse already-registered names, so the guard stays green with no entry. (Consequence worth a sentence in the evidence file: the guard cannot see the shims at all, which is fine.)
- Gate command that actually exercises the CATALOG-03 full-registry check is the **unscoped** run; a `--tests` filter does not (Phase 13 RESEARCH Pitfall, carried in the Phase 13 plan text).
- Result: clean uncached run, 751 tests, 22 skipped, 0 failures, 0 errors; BUILD SUCCESSFUL in 1m13s [VERIFIED: `cleanTestDebugUnitTest testDebugUnitTest --no-build-cache` + XML result summation this session].

### Finding 2 - API-02: three distinct signals, and what each means

Run live this session, HEAD `fa6b7ac`, `api.txt` clean in `git status`:

| Check | Command | Result |
|-------|---------|--------|
| Committed `api.txt` is current | `./gradlew apiDump` then `cmp` vs saved copy | **identical** (idempotent) |
| `apiCheck` vs HEAD | `./gradlew apiCheck detekt` | BUILD SUCCESSFUL |
| Additive vs **v2.4.1** (authoritative) | `git show v2.4.1:api.txt > api.txt; ./gradlew apiCheck; git checkout -- api.txt` | BUILD SUCCESSFUL, `api.txt` restored clean |
| Additive vs **v2.4.0** | same with `v2.4.0` | BUILD SUCCESSFUL |
| Debug variant (part of the §11 battery) | `./gradlew metalavaCheckCompatibilityDebug` | BUILD SUCCESSFUL |
| Raw-line script, relative path | `API_FILE=api.txt bash tools/verify-api-additive.sh v2.4.1` | **exit 3**, 10 removed lines (below) |
| Hook-style classifier (absolute path) | `tools/classify-hub-change.sh --baseline v2.4.1` with absolute `API_FILE` | `LANE 1`, exit 0 (the check silently SKIPs, see Pitfall 1) |
| Guard self-tests | `bash tools/test/run-all.sh` | rc 0; 4 suites, PASS=4/7/5/5, FAIL=0 |

**The 10 lines the raw-line script reports as removed vs v2.4.1** (api.txt was 1600 lines at v2.4.1, 1650 now) [VERIFIED: script output this session]. This is the allowlist the evidence file should record, each one a *superseded* line whose replacement is an appended-defaulted-param line:

| # | Superseded v2.4.1 line (subject) | Why it is not a real removal |
|---|----------------------------------|------------------------------|
| 1 | `ApproachLadderCard(...)` composable method | Replaced by the same signature plus appended optional params; the v2.4.1 JVM shape survives as a `HIDDEN` overload (Metalava omits hidden members) |
| 2 | `ClarificationBar(...)` | same, `dismissLabel` appended |
| 3 | `ModelSelectCard(...)` | same, `modelLabel` appended |
| 4 | `ProviderKeyCard(...)` | same, `providerLabel` appended |
| 5 | `FailureActionUiModel.copy(optional String label, optional Function0 onClick)` | Generated `copy` gains `role`; a hand-written old-arity `copy(String, Function0)` is present |
| 6 | `HandledByUiModel.copy(optional ...)` | `escalationsLabel` appended; old-arity `copy` hand-written |
| 7 | `ProposedItemUiModel.copy(optional ...)` | `removeContentDescription` appended; old-arity `copy` hand-written |
| 8 | `UndoRefusedUiModel.copy(optional reason, optional changedItem)` | `refusedPrefix`/`changedSinceSuffix` appended; old-arity `copy` |
| 9 | `UndoRowUiModel.copy(optional id, optional label, optional state)` | `undoneLabel` appended; old-arity `copy` |
| 10 | `VoiceOutcomeUiState.Failure.copy(optional reason, optional handledBy, optional action)` | `body`/`semanticsPrefix` appended; old-arity `copy` |

Rows 5-10 are the v2.4.1 `copy(optional ...)` lines (the "optional" markers are Metalava's rendering of Kotlin defaults); rows 1-4 are the four composable lines. Phase 15-03 recorded 8 (rows 1-4, 6-9); Phase 16 added 5 and 10. The delta fits CONTEXT's "ApproachLadderCard method line + ctor/copy lines" description, but note the count is **10, not the 8 recorded in 15-03**: the plan must assert 10, not 8.

**D-01 reading:** the raw-line script's exit 3 is the known false positive. The gate is "apiCheck green + api.txt regenerated". Present all of the above in the evidence file; do not hide the exit 3.

**On `HUB_LANE_OVERRIDE=3`:** that variable is read **only by `tools/hooks/pre-commit`**, not by `verify-api-additive.sh` [VERIFIED: cat of both scripts]. With the installed hook (`.git/hooks/pre-commit -> ../../tools/hooks/pre-commit`) and its absolute `API_FILE`, a docs-only commit is currently classified LANE 1 (source diff vs v2.4.1 is append-only `DS-05 PASS`, API check SKIPs). So Phase 18's docs commits need **no override** today. D-01's override instruction therefore only matters if the executor invokes the script with a relative `API_FILE` and then wants a green exit; the right record is "exit 3 observed, allowlisted, `apiCheck` authoritative", not an override on a docs commit. Never use an override to dismiss exit 3 without the swap-baseline proof (v2.4 Phase 13 prohibition, still applicable). [ASSUMED] that GSD's own commit helper runs the hook (not verified; see Assumptions A1).

### Finding 3 - DOC-02: precise documentation gap

State of `API.md` (290 lines) at HEAD [VERIFIED: Read of API.md lines 1-222 plus `git diff v2.4.1..HEAD -- API.md`]:

- Numbered sections: `## 1. Cards` ... `## 9. Tactile Foundation`, then `## Intentionally-unregistered sub-parts (5)`, then `## Adding / changing components (breaking-change note)`. **There is no `## 10. Voice Command`.** The only Voice-family mention is the "Surface at a glance" row `| 10. Voice Command | 5 | ... |`. The five composables (`ProviderKeyCard`, `ModelSelectCard`, `ApproachLadderCard`, `OutcomeSheet`, `ClarificationBar`) have no table rows. D-02's "per-component table rows" therefore means *creating* the section, between section 9 and the "Intentionally-unregistered" divider, in the same `| Composable | Purpose | Key parameters |` format.
- Already present (added by Phases 15/quick-eyu): the paragraph "Appending label fields to the voice models and composables (v2.5.0, VI18N-01..04)", the "Voice label fragments - how the sheet joins them" paragraph, and "### The binary-compatibility rule (applies to every change from v2.5 on)". Do not duplicate; extend.
- **Missing** and required by DOC-02:
  - Failure enrichment: `FailureActionUiModel.role`, `VoiceOutcomeUiState.Failure.body`, `VoiceOutcomeUiState.Failure.semanticsPrefix` (VFAIL-01..03).
  - Router toggle: `ApproachLadderCard` `router`, `onRouterChange`, `routerOnLabel`, `routerOffLabel` (VAPPR-04) and the `require()` pairing rule.
  - Phase 16 a11y behaviour (VA11Y-01): internal only, no signature change; document as behaviour on the `ApproachLadderCard` row (selected semantics for the capped rung; min interactive size only when the cap is selectable).
  - The existing compat paragraph names four composables + four models; it must be extended to the router/Failure additions (and `FailureActionUiModel`, `Failure` are named only in the binary paragraph).
- Full new-surface inventory (**20** additions, not "~13": 13 are VI18N, +3 VFAIL, +4 router). Defaults are quoted verbatim from source Read this session:

| Where | New parameter / field | Default (verbatim) | Requirement |
|-------|----------------------|--------------------|-------------|
| `ProviderKeyCard` | `providerLabel` | `"Provider"` | VI18N-01 |
| `ModelSelectCard` | `modelLabel` | `"Model"` | VI18N-01 |
| `ClarificationBar` | `dismissLabel` | `"Dismiss"` | VI18N-02 |
| `ApproachLadderCard` | `unavailableLabel` | `"Unavailable"` | VI18N-03 |
| `ApproachLadderCard` | `cappedLabel` | `"Capped"` | VI18N-03 |
| `ApproachLadderCard` | `needsNetworkLabel` | `"Needs network"` | VI18N-03 |
| `ApproachLadderCard` | `onlineLabel` | `"Online"` | VI18N-03 |
| `ApproachLadderCard` | `offlineOnlyLabel` | `"Offline only"` | VI18N-03 |
| `ApproachLadderCard` | `router: Boolean?` | `null` | VAPPR-04 |
| `ApproachLadderCard` | `onRouterChange: ((Boolean) -> Unit)?` | `null` | VAPPR-04 |
| `ApproachLadderCard` | `routerOnLabel` | `"Router on"` | VAPPR-04 (D-01 of Phase 17) |
| `ApproachLadderCard` | `routerOffLabel` | `"Router off"` | VAPPR-04 |
| `HandledByUiModel` | `escalationsLabel` | `"Escalations:"` | VI18N-04 |
| `UndoRowUiModel` | `undoneLabel` | `"Undone"` | VI18N-04 |
| `UndoRefusedUiModel` | `refusedPrefix` | `"Couldn't undo:"` | VI18N-04 |
| `UndoRefusedUiModel` | `changedSinceSuffix` | `"changed since"` | VI18N-04 |
| `ProposedItemUiModel` | `removeContentDescription` | `"Remove"` | VI18N-04 |
| `FailureActionUiModel` | `role: ActionButtonDefaults.ActionButtonRole` | `ActionButtonDefaults.ActionButtonRole.Neutral` | VFAIL-01 |
| `VoiceOutcomeUiState.Failure` | `body: (@Composable () -> Unit)?` | `null` | VFAIL-02 |
| `VoiceOutcomeUiState.Failure` | `semanticsPrefix: String?` | `null` | VFAIL-03 |

  Verbatim source evidence: `ApproachLadderCard.kt:102-110` reads `unavailableLabel: String = "Unavailable",` / `cappedLabel: String = "Capped",` / `needsNetworkLabel: String = "Needs network",` / `onlineLabel: String = "Online",` / `offlineOnlyLabel: String = "Offline only",` / `router: Boolean? = null,` / `onRouterChange: ((Boolean) -> Unit)? = null,` / `routerOnLabel: String = "Router on",` / `routerOffLabel: String = "Router off"`; `VoiceOutcomeUiState.kt:81-82` reads `val body: (@Composable () -> Unit)? = null,` / `val semanticsPrefix: String? = null`; `FailureActionUiModel.kt:25` reads `val role: ActionButtonDefaults.ActionButtonRole = ActionButtonDefaults.ActionButtonRole.Neutral` [VERIFIED: Read of those line ranges this session]. The other defaults are from `grep -nE "val ..."` of the model files and `Name: String = "..."` lines of the three smaller composables, same session; they match the `api.txt` property lines [VERIFIED: grep output + `git diff v2.4.1..HEAD -- api.txt`]. The planner should have the executor re-open each source file to copy defaults rather than trust this table for anything it quotes verbatim in API.md.
- Behavioural doc facts worth including (from the source KDoc read this session): `semanticsPrefix` is "joined by a single ASCII space so any punctuation belongs to the prefix", null or blank leaves the announcement unchanged, plain text, "Static UI copy only - never put sensitive text here (this class uses the generated `toString`)"; `body` renders "inside the error surface, after the handled-by row and before the [action]", `null` renders no node/space, equality is by lambda reference; the router toggle renders below the offline toggle, emits the tapped segment's target value (never a negation), and a half pair throws via `require()` exactly like `offlineOnly`/`onOfflineOnlyChange`; its a11y state words ("selected"/"not selected") stay English via `SegmentedOptionSelector` (documented residual, Phase 15 WR / Phase 17 VERIFICATION truth 6).
- **Trailing-lambda caveat (new, must be documented):** the v2.4.x call `ProposedItemUiModel("a", "b") { ... }` binds the lambda to the old last parameter `trailingContent`. With `removeContentDescription: String` appended last, Kotlin now binds it to the `String`. Reproduced: a temporary probe test produced `Argument type mismatch: actual type is '() -> Unit', but 'String' was expected.` (file removed afterwards; working tree clean). This is the same class as the v2.3.0 `showTagColors` note at `API.md:198-199`. Mitigation text: pass `trailingContent = { ... }` by name. Read-only sweep of both consumers found only the named form: `CalTracker_Android/.../VoiceOutcomeMapper.kt` uses `trailingContent = {` (3 sites) and `SecondBrain/.../VoiceConfirmSheetModels.kt` passes no trailing lambda [VERIFIED: grep of consumer working trees 2026-10-05; consumer repos not modified]. `15-VERIFICATION.md:60` ("trailing-lambda v2.4.0 call shapes bind the same values") is wrong for this type; do not copy that sentence into API.md. `FailureActionUiModel("l") { }` is the one trailing-lambda shape that *is* preserved (explicit two-arg constructor, pinned by `VoiceI18nSourceCompatTest`). The other appended-last additions are safe because the former last parameter was not a function type (`modifier`, `emptyProvidersReason: String`, `action: FailureActionUiModel?`, etc.).
- INTEGRATION.md (139 lines) [VERIFIED: Read] has no voice/localization/failure/router content. D-02 asks for "a light localization-override note": add one short subsection to §5 "Call components from your UI" (or a Notes bullet) saying: the hub ships English defaults and localizes nothing; pass localized strings via the optional label params / defaulted model fields; point to API.md section 10 and the compat scope. Include at most one tiny example (e.g. `ClarificationBar(..., dismissLabel = stringResource(...))` shape) only if the executor confirms it compiles against the real signature; D-02 says no full per-param examples.
- **Pre-existing INTEGRATION.md drift (optional, planner discretion, low risk):** it says the library builds against "Compose BOM 2026.02.01" (twice) but `gradle/libs.versions.toml` has `composeBom = "2026.04.01"` [VERIFIED: grep of libs.versions.toml and INTEGRATION.md]; it says "call the seven-family components" (the catalog is ten families, which is exactly the stale wording v2.4 Phase 13 D-03 fixed in four other files but not this one); it says "The tag is cut in Phase 102 (human-gated) - none exists yet" (stale; tags exist). Fixing the BOM and family-count is a one-line each, in-spirit with DOC-02; the stale "Phase 102" note and `CLAUDE.md`'s "v2.3.0 is the current release" are release-bookkeeping and belong to Phase 19. Recommend: fix BOM + "seven-family" in 18-01, leave the rest, and say so in the summary.

### Finding 4 - INV-02: invariant checks, live

- Import inspection over the **12** changed `src/main` files [VERIFIED: `git diff --name-only v2.4.1..HEAD -- src/main | wc -l` = 12, matching CONTEXT]: added `+import` lines by root: `androidx` 5, `kotlin` 4, `io` (own package) 1; the filter for anything outside `androidx|kotlin|kotlinx.coroutines|java|javax|io.github.ygaray.yahirandroidtaste` returned empty. All-of-`src/main` import roots are androidx/compose, own package, kotlinx.coroutines, kotlin, osmdroid, java, coil3, sh.calvin, navigation, javax.inject: no consumer package or engine.
- Name mentions of consumers/engine in `src/main` are KDoc prose only (`ApproachLadderCard.kt:40` "never depends on any voice-action-engine `TierPolicy` type"; `PlaceMapPicker.kt`, `TagChipUiModel.kt`, `Motion.kt` prose) [VERIFIED: grep this session]. These are pre-existing or explanatory; Phase 13's guard counted only `import` lines. Use the import filter, not a bare name grep, as the gate.
- No `Log.*` / `println(` / `Timber` added in the diff [VERIFIED: grep of `+` lines of `git diff v2.4.1..HEAD -U0 -- src/main` returned empty].
- Build/dependency files unchanged since v2.4.1 (Package Legitimacy Audit above).
- detekt: BUILD SUCCESSFUL; `config/detekt-baseline.xml` is `<SmellBaseline><ManuallySuppressedIssues/><CurrentIssues/></SmellBaseline>`; `config/detekt-compose.yml` has `functionThreshold: 18`, `constructorThreshold: 18`, `ignoreDefaultParameters: true` [VERIFIED: file output this session]. D-03's rationale ("all new params are defaulted") holds. Do not regenerate the baseline (root CLAUDE.md zero-baseline policy).
- Add (cheap, strengthens INV-02): the Phase 15 / quick precedent that the **Explorer fixture's `access$` synthetic descriptor is preserved** is already covered by Phase 19's binary gate; Phase 18 need not repeat it.

## Common Pitfalls

### Pitfall 1: `API_FILE` absolute path silently disables the raw-line API check
**What goes wrong:** `API_FILE=$PWD/api.txt bash tools/verify-api-additive.sh v2.4.1` prints `API-ADDITIVE SKIP: baseline v2.4.1 has no /home/.../api.txt yet` and exits 0, which looks like a pass.
**Why:** the script does `git cat-file -e "$BASE:$API_FILE"`, which needs a repo-relative path; an absolute path never resolves in the tag tree, so it takes the "baseline predates api file" degrade branch. `tools/hooks/pre-commit` itself exports an **absolute** `API_FILE` ("$ROOT/api.txt"), so the hook's API half has been a silent SKIP (this is the latent defect behind INC-2026-10-05-02 that Phase 19 D-03 removes by deleting the invocation).
**How to avoid:** in the evidence run use `API_FILE=api.txt` (relative) and expect exit 3 with the 10-line allowlist; label the absolute-path hook behaviour as "SKIP (known, retired in Phase 19)". Do not "fix" the hook or script in Phase 18.
**Warning signs:** an `API-ADDITIVE SKIP` line, or exit 0 from a baseline that definitely changed.

### Pitfall 2: Gradle build cache / up-to-date makes "full suite green" evidence stale
**What goes wrong:** `testDebugUnitTest` shows `FROM-CACHE`; the evidence file claims a full run that did not execute.
**How to avoid:** `./gradlew cleanTestDebugUnitTest testDebugUnitTest --no-build-cache` for the recorded run, then sum the XML (`build/test-results/testDebugUnitTest/*.xml`) for counts. Expect ~751 tests, 22 skipped (the skips are pre-existing, e.g. `CardBaseTest`, `VoiceCardClipListTest`), 0 failures; the count will rise if Phase 18 adds tests (it should not).
**Warning signs:** BUILD SUCCESSFUL in a few seconds.

### Pitfall 3: Leaving `api.txt` swapped after the baseline proof
**What goes wrong:** the executor swaps in `v2.4.1`'s `api.txt`, runs `apiCheck`, and a failure or abort leaves the old file in place; a later commit then regresses `api.txt`.
**How to avoid:** `git show v2.4.1:api.txt > api.txt; ./gradlew apiCheck; rc=$?; git checkout -- api.txt; test -z "$(git status --short api.txt)" && exit $rc`. Capture the restore check in the evidence ("Restore Confirmation"). Safe here because `api.txt` has no uncommitted changes at HEAD [VERIFIED: `git status --short api.txt` empty].

### Pitfall 4: Documenting a claim the code does not support
**What goes wrong:** copying the Phase 15 "trailing-lambda shapes bind the same values" claim, or saying the whole v2.5.0 addition is "source-compatible for every v2.4.x call shape".
**How to avoid:** write the `ProposedItemUiModel` trailing-lambda exception (Finding 3). Keep the "binary-compatible with v2.4.x" claim attributed to the javap proof and note Phase 19 re-proves it on the tagged HEAD; do not assert binary compat as a Phase 18 verified fact beyond citing the 261005-e2e result (`base=2526 ... missing=0`).

### Pitfall 5: Breaking documented counts
**What goes wrong:** "fixing" `61 registered ... 5 intentionally-unregistered = 66` in API.md or README (`41/4/45`-style counts from v2.4). No composable was added or removed, so every count stays byte-identical (Phase 13 Pitfall 4 repeated).

### Pitfall 6: Scope creep into Phase 19 / CLAUDE.md
**What goes wrong:** editing `tools/hooks/pre-commit`, `tools/README-api-guard.md`, adding `tools/verify-binary-abi.sh`, changing API.md's "It becomes `tools/verify-binary-abi.sh` at the v2.5.0 cut", editing root `CLAUDE.md` (explicitly "pending Yahir" in STATE), cutting or creating any tag (`git.create_tag` is false), or touching `.planning/uat-pending/` Gate-2 fragments.
**How to avoid:** the plan's `files_modified` should be exactly `API.md`, `INTEGRATION.md`, and the new `18-SHIP-GATE-EVIDENCE.md` (plus bookkeeping). Add a verify step `git diff --name-only <phase-start>..HEAD` showing nothing under `src/` or `tools/`, and `git tag --list 'v2.5*'` empty.

### Pitfall 7: Registry-guard false comfort / false alarm
The guard's source-text scan excludes `explorer/`, matches names only, and ignores `private`/`internal`. A hidden overload cannot trip it and a new `@Composable` in `model/` would be scanned (denylist is only `explorer`). Not a Phase 18 risk since no composable is added; mention only so nobody adds a registry entry "for" the shims.

## Code Examples

### Evidence run (copy-paste skeleton for 18-02)

```bash
# CAT-02 + detekt (uncached, final HEAD)
./gradlew cleanTestDebugUnitTest testDebugUnitTest detekt --no-build-cache
# count: sum tests/skipped/failures/errors across build/test-results/testDebugUnitTest/*.xml
# detekt baseline unchanged:
git diff --exit-code -- config/detekt-baseline.xml

# API-02: dump is idempotent
cp api.txt "$SCRATCH/api.head.txt"; ./gradlew apiDump -q; cmp api.txt "$SCRATCH/api.head.txt"

# API-02: authoritative swap-baseline proof, v2.4.1 then v2.4.0, always restore
for T in v2.4.1 v2.4.0; do
  git show $T:api.txt > api.txt; ./gradlew apiCheck; rc=$?
  git checkout -- api.txt
  [ -z "$(git status --short api.txt)" ] && [ $rc -eq 0 ] || { echo "API-02 FAIL vs $T"; break; }
done
./gradlew metalavaCheckCompatibilityDebug

# API-02: raw-line script, RELATIVE path, expect exit 3 + the 10-line allowlist
API_FILE=api.txt bash tools/verify-api-additive.sh v2.4.1; echo "exit=$?"
bash tools/test/run-all.sh

# INV-02: import inspection over every src/main diff v2.4.1..HEAD (must print nothing)
git diff v2.4.1..HEAD -U0 -- src/main | grep -E '^\+import ' \
  | grep -vE '^\+import (androidx|kotlin|kotlinx\.coroutines|java|javax|io\.github\.ygaray\.yahirandroidtaste)'
git diff --exit-code v2.4.1..HEAD -- build.gradle.kts gradle/ settings.gradle.kts jitpack.yml config/detekt-baseline.xml
git diff v2.4.1..HEAD -U0 -- src/main | grep -E '^\+.*(Log\.[dewiv]\(|println\(|Timber)'   # must print nothing

# Phase scope guard
test -z "$(git tag --list 'v2.5*')" && echo "no v2.5 tag"
```
// Source: commands executed live this session; swap-baseline pattern from v2.4 `13-01-PLAN.md` Task 1 and `15-03-SUMMARY.md` closing gate.

### API.md "10. Voice Command" section shape (table format to match sections 1-9)

```markdown
## 10. Voice Command

<one-paragraph intro: data+callback-only; the hub localizes nothing (INV-01) - every label has an English default; callers pass localized strings>

| Composable | Purpose | Key parameters |
|-----------|---------|----------------|
| `ProviderKeyCard` | Provider dropdown + API-key field | `providers, selectedProviderId, onProviderSelected, keyValue, onKeyChange, keyState, keyLabel, emptyProvidersReason, providerLabel = "Provider"` |
| `ModelSelectCard` | ... | `models, selectedModelId, onModelSelected, emptyReason, modelLabel = "Model"` |
| `ApproachLadderCard` | Command-approach ladder + offline / Router toggles | `ladder, offlineOnly?/onOfflineOnlyChange?, maxTierId?/onMaxTierChange?, <5 rung/toggle labels>, router: Boolean? = null / onRouterChange: ((Boolean)->Unit)? = null (require()-paired; null hides), routerOnLabel = "Router on", routerOffLabel = "Router off"` |
| `OutcomeSheet` | Success / Failure / NeedsConfirmation outcome sheet | `outcome: VoiceOutcomeUiState, onDismissRequest` - caller-overridable strings via model fields (below) |
| `ClarificationBar` | Tap-to-clarify bar | `question, options, onSelect, onDismiss, dismissLabel = "Dismiss"` |
```
// Source: the `OutcomeSheet` / composable signatures in `api.txt` (e.g. line 290 reads `OutcomeSheet(io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState outcome, kotlin.jvm.functions.Function0<kotlin.Unit> onDismissRequest, optional androidx.compose.ui.Modifier modifier)`). Executor must re-read each KDoc before finalizing the "Purpose" cells; the wording above is a shape, not copy.

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Phase 13: swap-baseline `apiCheck` + manual evidence file | Same, vs `v2.4.1` and `v2.4.0` | v2.4 -> v2.5 | Reuse unchanged |
| Raw-line `verify-api-additive.sh` as the gate | Metalava `apiCheck` authoritative; script exit 3 allowlisted (Phase 18); script/hook check deleted and replaced by `javap` gate (Phase 19 D-03) | 2026-10-05 | Phase 18 only *records* exit 3 |
| API.md "not binary compatible in general" (v2.3.0 note) | v2.5.0: binary compat restored via hidden shims + synthetic-descriptor "variant K" | quick 261005-dmc/e2e/eyu | Doc already written; Phase 18 extends the compat paragraph, does not rewrite it |

**Deprecated/outdated:** the Phase 15 VERIFICATION wording "trailing-lambda v2.4.0 call shapes bind the same values" (false for `ProposedItemUiModel`).

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | GSD's commit helper (`gsd_run query commit`) runs `.git/hooks/pre-commit`, so docs-only commits are classified by it | Finding 2 | If it uses `--no-verify` the hook is moot; if it runs and a lane-2 appears (e.g. an executor touches `src/`), the commit blocks and needs `HUB_LANE_OVERRIDE=2`. Low: Phase 18 should not touch `src/`. |
| A2 | Whether the owner wants the `ProposedItemUiModel` trailing-lambda break documented-only versus fixed in code before the tag | Finding 3 / Open Question 1 | A code fix is out of this phase's scope (it reshapes a public constructor); documenting is the v2.3.0 precedent. If the owner wants it fixed, it becomes a new phase/quick before Phase 19. |
| A3 | The optional drive-by INTEGRATION.md fixes (BOM `2026.02.01` -> `2026.04.01`, "seven-family" -> "ten-family") are wanted in DOC-02 | Finding 3 | Low; both are factual corrections verified against the repo. The planner may defer them. |

## Open Questions

1. **`ProposedItemUiModel` trailing-lambda source break: document-only or fix?**
   - What we know: reproduced compile error; both known consumers use the named form; same class as the accepted v2.3.0 `showTagColors` caveat; REQUIREMENTS says "no behavior change for existing callers" and "strictly additive".
   - What's unclear: whether the owner/orchestrator accepts a documented source caveat for a *model* constructor in a minor version, since the milestone brief frames the whole release as additive.
   - Recommendation: document it in API.md in Plan 01 (this phase's mandate), and surface it in the phase return so the orchestrator can decide before Phase 19. Do not change `src/main` in Phase 18. Route to the control-plane orchestrator first (project memory: coordination goes to the orchestrator, not Yahir).
2. **Should Phase 18 run the javap binary diff as informational evidence?**
   - What we know: the proven command and `missing=0` result (base=2526) are in `261005-e2e-SUMMARY.md`; a cached v2.4.1 AAR exists under `~/.gradle/caches/modules-2/files-2.1/com.github.Ygaray/yahirandroidtaste/` for older tags (v2.4.1 presence not confirmed this session; the listing was truncated at `v2.3.0`); a release AAR exists at `build/outputs/aar/yahirandroidtaste-release.aar` (may be stale).
   - Recommendation: optional line in the evidence file, clearly labelled informational; the gating run is Phase 19 D-03 on the exact tagged HEAD. Skip if the baseline AAR is not cached (needs network).
3. **Count wording in D-02 ("~13")** is the VI18N subset; the real inventory is 20. Recommendation: document all 20 (Finding 3 table); not a decision conflict, just a count correction.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| JDK | Gradle | yes | OpenJDK 17.0.19 | none needed |
| Gradle wrapper (offline) | all gates | yes | 9.4.1 (`--offline` worked) | online |
| `git` tags `v2.4.0`, `v2.4.1` with `api.txt` | swap baseline | yes | both tags present locally; both contain `api.txt` (1600 lines at v2.4.1) | none needed |
| `javap`, `unzip` | optional binary diff | yes (JDK) | - | skip |
| Network / JitPack | not required for Phase 18 | not probed | - | Phase 18 needs none |

**Missing dependencies with no fallback:** none.

## Validation Architecture

### Test Framework

| Property | Value |
|----------|-------|
| Framework | JUnit4 + Robolectric + Compose UI test, run via Gradle `testDebugUnitTest` |
| Config file | `build.gradle.kts`; detekt `config/detekt-compose.yml`, `config/detekt-baseline.xml` |
| Quick run command | `./gradlew testDebugUnitTest --tests "*ComponentRegistryDriftGuardTest*" --tests "*DomainVocabularyDriftGuardTest*" --tests "*GeneratedSymbolDriftGuardTest*" --tests "*ComponentRegistryTierTest*"` |
| Full suite command | `./gradlew cleanTestDebugUnitTest testDebugUnitTest detekt apiCheck --no-build-cache` (about 75 s for tests) |

### Phase Requirements -> Test Map

| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| CAT-02 | Every public top-level composable registered or allowlisted; drift guards green in the FULL suite | unit (source-scan) | `./gradlew cleanTestDebugUnitTest testDebugUnitTest --no-build-cache` (unscoped) | yes - `ComponentRegistryDriftGuardTest`, `DomainVocabularyDriftGuardTest`, `GeneratedSymbolDriftGuardTest`, `ComponentRegistryTierTest` |
| CAT-02 | Voice family still has exactly 5 entries / registry count 61 + 5 | grep | `grep -c "ComponentRegistry.Entry(" src/main/java/io/github/ygaray/yahirandroidtaste/explorer/VoiceCommandFamilyScreen.kt` prints 5 | yes |
| API-02 | `api.txt` current vs source | gate | `cp api.txt $S/a; ./gradlew apiDump -q; cmp api.txt $S/a` | yes |
| API-02 | Additive vs v2.4.1 and v2.4.0 (source level) | gate | swap-baseline `apiCheck` loop in Code Examples; `git status --short api.txt` empty after | yes |
| API-02 | Raw-line script result is the documented 10-line allowlist | script | `API_FILE=api.txt bash tools/verify-api-additive.sh v2.4.1` -> exit 3, exactly 10 `FAIL (lane 3)` lines, each matching the allowlist table | yes |
| API-02 | Guard self-tests still pass | script | `bash tools/test/run-all.sh` rc 0 | yes |
| DOC-02 | API.md has a `## 10. Voice Command` section and names every one of the 20 new params/fields | grep | `for n in providerLabel modelLabel dismissLabel unavailableLabel cappedLabel needsNetworkLabel onlineLabel offlineOnlyLabel router onRouterChange routerOnLabel routerOffLabel escalationsLabel undoneLabel refusedPrefix changedSinceSuffix removeContentDescription role body semanticsPrefix; do grep -q "\`$n" API.md \|\| echo MISSING $n; done` prints nothing; `grep -n "^## 10. Voice Command" API.md` matches | no - Wave 0 is the doc edit itself |
| DOC-02 | API.md documents the `ProposedItemUiModel` trailing-lambda caveat | grep | `grep -n "trailingContent =" API.md` matches inside the v2.5.0 compat paragraph | no |
| DOC-02 | INTEGRATION.md has the localization-override note | grep | `grep -n -i "locali" INTEGRATION.md` matches | no |
| DOC-02 | Documented counts unchanged | grep | `grep -n "61 registered" API.md` still matches; `git diff` shows no change to the `61`/`5`/`66` numbers | yes |
| INV-02 | No foreign import added | script | import filter in Code Examples prints nothing | yes |
| INV-02 | No dependency/build change, no logging | script | `git diff --exit-code v2.4.1..HEAD -- build.gradle.kts gradle/ settings.gradle.kts jitpack.yml`; log/print grep prints nothing | yes |
| INV-02 | detekt green at zero baseline | gate | `./gradlew detekt` + `git diff --exit-code -- config/detekt-baseline.xml` | yes |
| Phase boundary | No tag, no src/tools change | git | `test -z "$(git tag --list 'v2.5*')"`; `git diff --name-only <phase-start>..HEAD -- src tools` empty | yes |

### Sampling Rate

- **Per task commit:** the quick-run drift-guard command above (docs-only tasks need only the grep checks for their own file).
- **Per wave merge:** full suite command.
- **Phase gate:** full suite + the whole evidence battery green on the final HEAD before `/gsd-verify-work`; docs edits cannot affect test results, but run the battery last anyway so evidence is for the exact HEAD Phase 19 will tag.

### Wave 0 Gaps

- [ ] `18-SHIP-GATE-EVIDENCE.md` - new evidence file (literal headers `## CAT-02 Evidence`, `## API-02 Evidence`, `## INV-02 Evidence`, `## Restore Confirmation`, each followed by a `PREFIX: verdict` line), modelled on `.planning/milestones/v2.4-phases/13-catalog-integrity-v2-4-0-ship/13-SHIP-GATE-EVIDENCE.md`.
- [ ] No new test framework or fixtures needed. No new automated test should be added (D-03).

## Security Domain

`security_enforcement` is enabled (config.json `security_enforcement: true`, `security_asvs_level: 1`). This phase adds no code and no runtime surface; it re-confirms existing invariants.

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2 Authentication | no | - |
| V3 Session Management | no | - |
| V4 Access Control | no | - |
| V5 Input Validation | no | no new input path; label params are caller-supplied display text |
| V6 Cryptography | no | none; the library holds no secrets (root CLAUDE.md invariant) |
| V10 Malicious code / dependencies | yes | build/dependency files unchanged since v2.4.1; import inspection |
| V14 Configuration | yes | detekt zero baseline unchanged; no `@HiltAndroidApp` added |

### Known Threat Patterns for this stack

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| Supply-chain / new dependency slipped in | Tampering | `git diff --exit-code v2.4.1..HEAD -- build.gradle.kts gradle/ ...` empty; import filter |
| Sensitive text leaking into `toString` / logs via a new model field | Information disclosure | No `Log.*`/`println` added (grep); docs must repeat the existing KDoc rule that `semanticsPrefix` is static UI copy only (the generated `toString` includes it) and that `ProposedItemUiModel.toString` was deliberately kept free of the new label (Phase 15-03 decision) |
| Mutable moving ref pinned by a consumer | Tampering | No tag cut here; tags immutable (Phase 19) |

## Project Constraints (from CLAUDE.md)

Root `CLAUDE.md` + `.claude/CLAUDE.md` directives that bind this phase:

- Library imports **no host code**, holds no secrets, makes no domain assumptions; docs must not introduce consumer-specific concepts into the hub's own wording (a mention of consumers as examples already exists in ECOSYSTEM.md; keep API.md consumer-name-free in the new section).
- Bindings-only Hilt; never add `@HiltAndroidApp` / `@AndroidEntryPoint`.
- `ComponentRegistry` is the single source of truth + drift guard; register XOR allowlist every public top-level `@Composable` (nothing to do here).
- Interaction conventions (reveal-confirm swipe, snackbar/undo, conditional-render-no-dead-space) must be preserved; the router toggle's "null hides, no dead space" behaviour belongs in the docs.
- Tag/bump/deploy is human-gated (waiver granted only for Phase 19, v2.5.0). **Phase 18 cuts no tag.** Tags immutable; never `main-SNAPSHOT`.
- Cross-repo convention: sequential in the hub, commit on `main`, **no consumer worktrees; do not modify consumer files**. (I read consumer files for one grep only; the plan must not edit them.)
- Gradle commands drop the module prefix (`./gradlew testDebugUnitTest`, `./gradlew detekt`, `./gradlew apiDump`/`apiCheck`).
- Detekt zero-baseline: do not regenerate a baseline.
- Read `API.md` + `INTEGRATION.md` for the public surface (they are the files being edited).
- Project memory: route version/tag/sequencing confirmations to the control-plane orchestrator first.
- Global: the developer prefers brief, root-cause-forward explanations; flag any state change made (I made one transient change: a probe test file, created and deleted within one command; `git status` clean afterwards).

## Sources

### Primary (HIGH confidence)
- Live gate runs against HEAD `fa6b7ac` in `/home/yahir/Projects/Reusable/android/yahirandroidtaste` this session: `apiCheck`, `detekt`, `apiDump` idempotency, swap-baseline `apiCheck` v2.4.1 and v2.4.0, `metalavaCheckCompatibilityDebug`, clean `testDebugUnitTest` (751/22/0/0), `tools/test/run-all.sh`, `tools/verify-api-additive.sh` (relative and absolute `API_FILE`), `tools/classify-hub-change.sh`, import/log/build-file diffs, trailing-lambda compile probe.
- Source files read in full or by range: `ComponentRegistryDriftGuardTest.kt`, `ApproachLadderCard.kt:90-115`, `VoiceOutcomeUiState.kt:60-90`, `FailureActionUiModel.kt:14-35`, `API.md` (1-222 + diffs), `INTEGRATION.md`, `tools/verify-api-additive.sh`, `tools/classify-hub-change.sh`, `tools/hooks/pre-commit`, `tools/README-api-guard.md`, `build.gradle.kts` (Metalava block).
- `git diff v2.4.1..HEAD -- api.txt` (read in full), `git diff v2.4.1..HEAD -- API.md`.
- Planning artifacts: `18-CONTEXT.md`, `REQUIREMENTS.md`, `STATE.md`, `ROADMAP.md` Phase 18, `v2.5-DECISION-MAP.md` Phase 18/19, Phase 15/16/17 VERIFICATION and SUMMARY, Phase 19 CONTEXT, quick `261005-e2e-SUMMARY.md`, v2.4 `13-01-PLAN.md` (analog).

### Secondary (MEDIUM confidence)
- Read-only grep of consumer working trees (`SecondBrain`, `CalTracker_Android`) for `ProposedItemUiModel` call shapes (working-tree state on 2026-10-05, not a tagged ref).

### Tertiary (LOW confidence)
- None. No web research was needed; no external library behaviour is in play (the research-plan/Context7 seam was not used because every question is an in-repo fact).

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - existing tooling, all run live.
- Architecture (plan shape): HIGH - direct analog (v2.4 Phase 13) plus live results.
- Pitfalls: HIGH for 1-4 (each reproduced or read from source); MEDIUM for A1 (hook invocation by the GSD commit helper not exercised).

**Research date:** 2026-10-05
**Valid until:** until any `src/main` change lands on `main` or Phase 19 starts (the 10-line allowlist and the 751-test count are HEAD-specific); otherwise 30 days.
