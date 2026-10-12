# Phase 15: Voice-surface i18n label params - Research

**Researched:** 2026-10-05
**Domain:** Additive API evolution of a Jetpack Compose design-system library (Kotlin 2.3.20, Compose BOM 2026.04.01, AGP 9.2.1) under a Metalava + raw-line additive guard
**Confidence:** HIGH (every central claim below was either read in source this session or reproduced empirically in a scratch copy of the repo)

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- **D-01 [api-guard]:** Adding a defaulted param/field to a symbol already shipped in v2.4.0 is source-compatible but trips the raw-line `tools/verify-api-additive.sh` as lane-3. Resolution (human): treat Metalava `apiCheck` as the authoritative additive gate, append every new param as the LAST parameter, and declare the raw-line guard's lane-3 as a known false-positive via `HUB_LANE_OVERRIDE=3` on these commits. — **Reversibility:** reversible — a guard-invocation/override choice, not a code contract.
- **D-02 [undo-label]:** The overridable "Undone" label lives on `UndoRowUiModel` as a defaulted `undoneLabel: String = "Undone"` (per-row); never convert `UndoRowState.Undone` (a `data object`) to a class — that would delete the public `INSTANCE` symbol (a lane-3 break). "Escalations:" → `HandledByUiModel`, "Couldn't undo:"/" changed since" → `UndoRefusedUiModel`, "Remove" → `ProposedItemUiModel.removeContentDescription`, all defaulted. _(source: ai-auto)_
- **D-03 [gallery]:** Leave the Explorer gallery call sites on English defaults (byte-identical gallery); a localized showcase is optional, not required. _(source: ai-auto)_

### Claude's Discretion
- Exact param names beyond those the brief fixes, and the ordering of appended params among themselves (as long as each is appended after `modifier`/after existing fields to keep `apiCheck` green).

### Deferred Ideas (OUT OF SCOPE)
The offline-toggle label decision here (`onlineLabel`/`offlineOnlyLabel`) sets the precedent Phase 17's router-toggle labels will mirror (P17 [router-copy] is provisional on this phase's outcome).
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| VI18N-01 | `ProviderKeyCard.providerLabel = "Provider"`, `ModelSelectCard.modelLabel = "Model"` | Exact literal sites + signatures below; both are plain appended-last defaulted params, `apiCheck` passes (reproduced) |
| VI18N-02 | `ClarificationBar.dismissLabel = "Dismiss"` | One literal at `ClarificationBar.kt:92`; appended-last param, `apiCheck` passes (reproduced) |
| VI18N-03 | `ApproachLadderCard` `unavailableLabel/cappedLabel/needsNetworkLabel/onlineLabel/offlineOnlyLabel` | Literals at `ApproachLadderCard.kt:111,157,164,171`; labels must be threaded into private `RungRow` and into `SegmentedOptionSelector.options` |
| VI18N-04 | OutcomeSheet literals via defaulted model fields | **Plain data-class field appends FAIL `apiCheck`** (RemovedMethod on ctor + `copy`). Required recipe: `@JvmOverloads constructor` + hand-written old-arity `copy()` overload — reproduced green. See "Critical Finding". |
</phase_requirements>

## Summary

The five composable edits (VI18N-01/02/03) are trivial and low-risk: every target already ends with `modifier: Modifier = Modifier` (or, for `ProviderKeyCard`, `emptyProvidersReason: String = ...`), none ends in a function-typed parameter, so appending defaulted `String` params is source-compatible for named, positional, and trailing-lambda call shapes (no trailing-lambda trap like v2.3.0's `showTagColors`). Metalava `apiCheck` passed for all five composable re-signatures in an empirical run. Every existing call site in the repo (tests + Explorer gallery) constructs the four models with **named arguments** — no positional construction exists, so the additive-field risk is on the binary/Metalava side, not the source side.

**Critical finding (contradicts the DECISION-MAP/CONTEXT assumption behind D-01/D-02):** appending a defaulted field to the four model `data class`es does NOT keep Metalava `apiCheck` green. In a scratch copy of the repo, naive field appends produced 8 errors: `Binary breaking change: Removed constructor …` and `Removed method …copy(…)` for all four models (output quoted below). D-01 says `apiCheck` is "the authoritative additive gate", so the model edits must be shaped so that gate passes. The repo already contains the recipe's first half (`TagChipUiModel` uses `@JvmOverloads constructor`); the second half (a hand-written old-arity `copy()` overload that delegates to the generated full-arity `copy`) was reproduced green here: with both, `apiCheck` → `BUILD SUCCESSFUL`, `detekt` → `BUILD SUCCESSFUL`, and the existing 5 component test classes pass unchanged.

**Primary recommendation:** Append the label params LAST on the five composables (plain defaulted `String`s); for the four models use `data class X @JvmOverloads constructor(…, newField: String = "<English>")` plus a hand-written `fun copy(<old params, no defaults>)` overload that delegates to the generated `copy(…, newField)`; regenerate `api.txt` with `./gradlew apiDump` in the same commit; gate on `./gradlew apiCheck`; commit with `HUB_LANE_OVERRIDE=3`.

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Label text supplied by consumer | Consumer app (caller) | — | INV-01: hub localizes nothing; callers pass strings (props only) |
| Label rendering | Library composables (`component/`) | — | Presentational; reads label param, renders `Text`/`contentDescription` |
| Per-row/per-model label carriage (undone, escalations, refused, remove cd) | Library models (`model/`) | — | OutcomeSheet is a prop-driven renderer of `VoiceOutcomeUiState`; models are the only data channel into its private sub-composables |
| Additive-API enforcement | Build/guard tooling (Metalava `apiCheck`, `tools/*.sh`, pre-commit) | — | D-01 designates `apiCheck` as authoritative |

## Standard Stack

No new libraries. Phase is pure Kotlin/Compose edits inside the existing module. (`## Package Legitimacy Audit`: N/A — no external packages installed.)

### Core (existing, versions from CLAUDE.md + build logs)
| Tool | Version | Purpose |
|------|---------|---------|
| AGP / Kotlin / Compose BOM | 9.2.1 / 2.3.20 / 2026.04.01 | build [VERIFIED: /home/yahir/Projects/Reusable/android/yahirandroidtaste/CLAUDE.md] |
| Metalava Gradle plugin | `me.tylerbwong.gradle.metalava` 0.5.0 | `apiDump` / `apiCheck` [VERIFIED: gradle/libs.versions.toml:46 `metalava = { id = "me.tylerbwong.gradle.metalava", version = "0.5.0" }`] |
| Gradle | 9.4.1 | [VERIFIED: Gradle run output "docs.gradle.org/9.4.1"] |
| Robolectric + `createComposeRule()` | existing | Compose UI tests, `@Config(sdk = [35])` [VERIFIED: ClarificationBarTest.kt:20-24] |

## Architecture Patterns

### Current signatures and exact literal sites (all read this session)

Quotes are verbatim.

| Composable | Current last params | Literal site | New param (append LAST) |
|---|---|---|---|
| `ProviderKeyCard` (`ProviderKeyCard.kt:62-72`) | `…keyLabel: String, modifier: Modifier = Modifier, emptyProvidersReason: String = "No providers configured yet"` | `ProviderKeyCard.kt:147` `label = { Text("Provider") },` inside **private** `ProviderDropdown` (`:118-124`, params end `emptyReason: String, modifier: Modifier = Modifier`) | `providerLabel: String = "Provider"` after `emptyProvidersReason`; thread to `ProviderDropdown` (add param there) |
| `ModelSelectCard` (`ModelSelectCard.kt:52-58`) | `emptyReason: String, modifier: Modifier = Modifier` | `ModelSelectCard.kt:112` `label = { Text("Model") },` inside **private** `ModelDropdown` (`:95-100`) | `modelLabel: String = "Model"` after `modifier`; thread to `ModelDropdown` |
| `ClarificationBar` (`ClarificationBar.kt:53-59`) | `onDismiss: () -> Unit, modifier: Modifier = Modifier` | `ClarificationBar.kt:92` `Text("Dismiss")` | `dismissLabel: String = "Dismiss"` after `modifier` |
| `ApproachLadderCard` (`ApproachLadderCard.kt:62-69`) | `maxTierId: String? = null, onMaxTierChange: ((String) -> Unit)? = null, modifier: Modifier = Modifier` | `:111` `options = listOf("Online", "Offline only"),`; in private `RungRow` (`:125-131`): `:157` `text = "Unavailable",` / `:164` `text = "Capped",` / `:171` `text = "Needs network",` | `unavailableLabel = "Unavailable"`, `cappedLabel = "Capped"`, `needsNetworkLabel = "Needs network"`, `onlineLabel = "Online"`, `offlineOnlyLabel = "Offline only"` (all `String`), after `modifier`; thread first three into `RungRow` (add 3 params), last two into `listOf(onlineLabel, offlineOnlyLabel)` |
| `OutcomeSheet` (`OutcomeSheet.kt:61-65`) | `modifier` | **No signature change** — literals reach it via model fields | — |

OutcomeSheet literals (CONTEXT line numbers are slightly off; real lines):

| Real line | Literal | Private composable | Source model field |
|---|---|---|---|
| `OutcomeSheet.kt:176-178` | `val suffix = refused.changedItem?.let { ", $it changed since" } ?: ""` and `text = "Couldn't undo: ${refused.reason}$suffix",` | `UndoAffordanceBody` | `UndoRefusedUiModel` (two fields: prefix + suffix) |
| `OutcomeSheet.kt:229-230` | `is UndoRowState.Undone -> Text(\n text = "Undone",` | `UndoRowItem(row: UndoRowUiModel, …)` | `UndoRowUiModel.undoneLabel` (`row.undoneLabel`) |
| `OutcomeSheet.kt:330` | `handledBy.escalationCount?.let { add("Escalations: $it") }` | `HandledByRow` | `HandledByUiModel.escalationsLabel` |
| `OutcomeSheet.kt:433` | `Icon(Icons.Default.Close, contentDescription = "Remove")` | `ProposedItemRow` | `ProposedItemUiModel.removeContentDescription` |

**Additional English literals found on these surfaces NOT in the CONTEXT list:**
1. `SegmentedOptionSelector.kt:65` — `contentDescription = "$label, ${if (selected) "selected" else "not selected"}"`. This is the accessibility description of the `ApproachLadderCard` online/offline toggle (a **public, separate** composable, shared with non-voice surfaces). With `onlineLabel`/`offlineOnlyLabel` localized, TalkBack will still announce English "selected"/"not selected". The existing test depends on the default text: `ApproachLadderCardTest.kt:77` `onNodeWithContentDescription("Offline only, not selected")`. **Not required by VI18N-01..04; recommend NOT changing it in this phase** (touches a public non-voice composable; scope creep) but surface it to the owner as a known residual (Open Question 1).
2. `UndoCenterScreen.kt:159` `"Undone"` — in `feedback/`, a different (non-OutcomeSheet) surface. Out of scope; do not touch.
3. Non-literals that look like literals but need no change: the `" · "` join separator (`OutcomeSheet.kt:333`) is language-neutral punctuation; `NeedsConfirmation.confirmLabel/cancelLabel` defaults ("Confirm"/"Cancel", `VoiceOutcomeUiState.kt:118-119`) and `UndoAffordanceUiModel.allLabel` are already caller-supplied.
4. English **composition** (not word) residue in the refused message: the format `"<prefix> <reason>, <item> <suffix>"` hard-codes word order (item before suffix), the `", "` join and the space. Acceptable for the brief's "prefix/suffix" shape; flag in docs that languages needing different order should fold the item into `reason` and omit `changedItem`.

### Critical Finding: model field appends vs Metalava (reproduced)

Scratch experiment (copy of repo at `/tmp/claude-1000/…/scratchpad/repo`, repo itself untouched). Naive append of one defaulted field to each of the four data classes, then `./gradlew apiCheck --offline`:

```
api.txt:1050: error: Binary breaking change: Removed constructor io.github.ygaray.yahirandroidtaste.model.HandledByUiModel(String,String,String,String,Integer) [RemovedMethod]
api.txt:1056: error: Binary breaking change: Removed method io.github.ygaray.yahirandroidtaste.model.HandledByUiModel.copy(String,String,String,String,Integer) [RemovedMethod]
api.txt:1134: error: Binary breaking change: Removed constructor …ProposedItemUiModel(String,String,String,String,boolean,Function0,Function0) [RemovedMethod]
api.txt:1142: error: Binary breaking change: Removed method …ProposedItemUiModel.copy(…) [RemovedMethod]
api.txt:1281: …UndoRefusedUiModel(String,String) [RemovedMethod]   api.txt:1284: …UndoRefusedUiModel.copy(String,String) [RemovedMethod]
api.txt:1315: …UndoRowUiModel(String,String,UndoRowState) [RemovedMethod]  api.txt:1319: …UndoRowUiModel.copy(String,String,UndoRowState) [RemovedMethod]
```
(The same run produced **zero** errors for the 4 composable param appends: composable function re-signatures with an appended `optional` param pass Metalava; only the model constructors/`copy` are flagged.)

**Working recipe (same scratch copy → `apiCheck` BUILD SUCCESSFUL, `detekt` BUILD SUCCESSFUL, 5 existing test classes BUILD SUCCESSFUL):**

```kotlin
// UndoRowUiModel.kt — pattern applies identically to all four models
data class UndoRowUiModel @JvmOverloads constructor(
    val id: String,
    val label: String,
    val state: UndoRowState,
    val undoneLabel: String = "Undone"
) {
    // Old-arity copy kept ONLY so the pre-v2.5 JVM `copy(String,String,UndoRowState)` symbol survives
    // (Metalava RemovedMethod). Delegates to the generated full-arity copy, preserving the new field.
    fun copy(id: String, label: String, state: UndoRowState): UndoRowUiModel =
        copy(id, label, state, undoneLabel)
}
```
- `@JvmOverloads` preserves every old constructor arity (the old `ctor` lines survive verbatim in `api.txt`); same technique already used by `TagChipUiModel` (`TagChipUiModel.kt:78` `data class TagChipUiModel @JvmOverloads constructor(`) — that file's KDoc (lines 38-56) explains why a *6th primary-ctor param* was rejected there because the synthetic `copy()` "is a genuine, unfixable API removal"; it chose a body `var`. **That claim is too strong for these models:** a hand-written old-arity `copy` overload sidesteps it (verified green). Using a body `var` instead is NOT recommended here (loses all-`val` Compose stability; `TagChipUiModel` documents that cost at lines 62-70).
- Kotlin overload resolution: a 3-arg `copy(id, label, state)` call now binds to the hand-written overload (no defaults needed), partial named calls like `copy(label = "x")` still bind to the generated one — both preserve the new field. [VERIFIED: compiled in scratch; existing tests unchanged] Behavioral equivalence of the 3-arg path is by construction (delegation).
- Per-model old-arity `copy` signatures to hand-write (drop all defaults, original parameter list exactly):
  - `HandledByUiModel`: `copy(tier: String, approach: String?, provider: String?, model: String?, escalationCount: Int?)` → `copy(tier, approach, provider, model, escalationCount, escalationsLabel)`
  - `UndoRefusedUiModel`: `copy(reason: String, changedItem: String?)` → delegates with the two new fields
  - `UndoRowUiModel`: `copy(id: String, label: String, state: UndoRowState)`
  - `ProposedItemUiModel`: `copy(id, title, subtitle: String?, confidenceCue: String?, amended: Boolean, onRemove: (() -> Unit)?, trailingContent: (@Composable () -> Unit)?)` — keep the existing `override fun toString()` (privacy: omits title/subtitle/cue — must stay unchanged, tests at `OutcomeSheetTest.kt:463-470` pin `"ProposedItemUiModel(id=x, amended=false)"`). Must not add the new label to `toString`.
- Resulting `api.txt` delta for `UndoRowUiModel` (from `apiDump` in scratch): new ctor line `ctor public UndoRowUiModel(String id, String label, …UndoRowState state, optional String undoneLabel);`, new `component4()`, new `getUndoneLabel()`, `property public String undoneLabel;`, the generated `copy(optional …, optional String undoneLabel)`, and the hand-written `copy(String id, String label, …UndoRowState state)`. The original line `copy(optional String id, optional String label, optional …UndoRowState state)` is **removed/changed** → the raw-line guard still reports lane 3 — expected and covered by D-01's `HUB_LANE_OVERRIDE=3`.
- `data object UndoRowState.Undone` stays untouched (D-02).

Suggested field names (brief fixes three; the rest are discretion): `HandledByUiModel.escalationsLabel: String = "Escalations:"`, `UndoRowUiModel.undoneLabel: String = "Undone"`, `ProposedItemUiModel.removeContentDescription: String = "Remove"` (brief + D-02), and for `UndoRefusedUiModel` (brief only says "refused-prefix on undo model"): `refusedPrefix: String = "Couldn't undo:"`, `changedSinceSuffix: String = "changed since"` (append after `changedItem`, in that order). Render: `"$refusedPrefix ${refused.reason}$suffix"` with `suffix = refused.changedItem?.let { ", $it ${refused.changedSinceSuffix}" } ?: ""` — byte-identical English.

### Threading labels into private composables
`ProviderDropdown`/`ModelDropdown`/`RungRow` are `private`; adding params to them is free (not API). Give them non-default params (the public composable always supplies them) to avoid duplicating English defaults in two places — **single source of default text = the public signature / model field**.

### Anti-Patterns to Avoid
- **Do not** append model fields without `@JvmOverloads` + old-arity `copy` (fails `apiCheck`, reproduced).
- **Do not** convert `UndoRowState.Undone` to a class, or put the label on `UndoRowState` (D-02).
- **Do not** place new composable params anywhere but last (after `modifier`; for `ProviderKeyCard` after `emptyProvidersReason`). Phase 17 will append `router`/`onRouterChange` (+ possibly `routerOnLabel`/`routerOffLabel`) AFTER Phase 15's five labels on `ApproachLadderCard`; Phase 16 touches `FailureActionUiModel`/`Failure` (not these signatures). Keep P15's order stable.
- **Do not** add library-owned localized strings/`stringResource` (INV-01: hub localizes nothing).
- **Do not** edit `explorer/` call sites (D-03 — gallery stays on defaults; none of them need to change since every new param/field is defaulted).

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---|---|---|---|
| Binary-compat for added ctor params | Secondary constructors by hand | `@JvmOverloads constructor` | Already the repo precedent (`TagChipUiModel`); generates every shorter arity |
| API-diff checking | Custom script | `./gradlew apiCheck` / `apiDump` | D-01's authoritative gate |
| String resources / localization | Library-side `strings.xml` | Caller-supplied `String` params | INV-01 |
| Source-compat proof | Prose only | A compile-only fixture like `ShowTagColorsSourceCompatTest.kt` (optional) | Precedent in repo; pins v2.4-style call shapes |

## Runtime State Inventory
Not a rename/refactor/migration phase — omitted.

## Common Pitfalls

### Pitfall 1: apiCheck red on model field append
**What goes wrong:** `Removed constructor`/`Removed method …copy` errors (see Critical Finding). **Avoid:** `@JvmOverloads` + old-arity `copy`. **Warning sign:** `./gradlew apiCheck` exit 1 after the first model edit.

### Pitfall 2: Pre-commit hook blocks commits after the first src change
`tools/hooks/pre-commit` uses `BASE="$(git describe --tags --abbrev=0 --match 'v*')"` (= `v2.4.1`, the newest tag) and `tools/classify-hub-change.sh --baseline $BASE`, which diffs **HEAD vs the tag**, not the staged diff [VERIFIED: tools/hooks/pre-commit, tools/classify-hub-change.sh, tools/verify-api-additive.sh read this session]. Consequences: (a) no commit since `v2.4.1` touched `src/` or `api.txt` (`git log v2.4.1..HEAD -- src api.txt` is empty), so today it is lane 1; (b) once `api.txt` is regenerated with re-signatured lines, `verify-api-additive.sh` exits 3 (its `comm -23` finds old lines missing) so **every subsequent commit in P15–P18, even docs/test-only, is lane 3** and needs `HUB_LANE_OVERRIDE=3`; (c) a commit that rewrites source lines (e.g. `Text("Dismiss")` → `Text(dismissLabel)`) but does **not** yet include the regenerated `api.txt` classifies as lane 2 (`verify-additive-diff.sh` flags rewritten lines) and needs `HUB_LANE_OVERRIDE=2`. **Avoid mismatch:** regenerate `api.txt` (`./gradlew apiDump`) in the same commit as the source change (README discipline: `tools/README-api-guard.md` "API dump discipline"), then use `HUB_LANE_OVERRIDE=3` throughout. The hook's override must equal the detected lane exactly (`[ "${HUB_LANE_OVERRIDE:-}" = "$lane" ]`).

### Pitfall 3: Duplicated English defaults drift
If private helpers (`RungRow`, dropdowns) also carry default strings, the two defaults can diverge. Keep defaults only on the public signature/model field.

### Pitfall 4: toString of ProposedItemUiModel
Hand-written `copy` is fine, but do not regenerate/replace the privacy `toString()`; `OutcomeSheetTest.kt:463-470` pins it. Other three models use the generated `toString` (will include the new label — harmless, not privacy-bearing; `UndoRefusedUiModel.reason` was already printed).

### Pitfall 5: Test node lookup for OutlinedTextField labels
`label = { Text(providerLabel) }` renders inside a text field; if `onNodeWithText("Fournisseur")` fails against the merged tree, retry with `useUnmergedTree = true` (existing tests use `useUnmergedTree = true` for icon-button queries, `OutcomeSheetTest.kt` ~line 543). [ASSUMED — the existing tests only query dropdown *option* text, not the label text; confirm on first run.]

### Pitfall 6: Positional `modifier` callers
All new params follow `modifier`, so v2.4 calls that pass `modifier` positionally still compile; none of the five composables ends in a function-typed param so no trailing-lambda breakage (contrast v2.3.0, `API.md:198-214`). [VERIFIED: signatures above]

## Call-site audit (additive-field risk)
Every construction site found by `grep` of `UndoRowUiModel(|UndoRefusedUiModel(|HandledByUiModel(|ProposedItemUiModel(` outside `model/` uses **named arguments** (tests: `OutcomeSheetTest.kt` lines 74, 160-161, 191, 217-222, 290-292, 316, 340, 370, 391-717; Explorer: `VoiceCommandFamilyScreen.kt` lines 359, 381-382, 397-398, 410-414, 425, 440-442, 459, 501-504). No destructuring (`val (a, b) = …`) or `.copy(` on these four types in `src/main`/`src/test` (grep returned nothing). Therefore appending fields shifts no positional meaning in-repo; the only compatibility exposure is binary/ABI, handled by the recipe above. Explorer gallery needs **zero edits** (D-03).

## Code Examples

### Composable param append + threading (ProviderKeyCard)
```kotlin
fun ProviderKeyCard(
    /* …existing params unchanged… */
    modifier: Modifier = Modifier,
    emptyProvidersReason: String = "No providers configured yet",
    providerLabel: String = "Provider"          // NEW, last
) { /* … */ ProviderDropdown(/* … */ emptyReason = emptyProvidersReason, providerLabel = providerLabel, /* … */) }
// private ProviderDropdown: add `providerLabel: String` and use  label = { Text(providerLabel) }
```
Verified shape (scratch `apiCheck` accepted the identical pattern for `ClarificationBar`, `ModelSelectCard`, `ApproachLadderCard`, `ProviderKeyCard`).

### ApproachLadderCard threading
```kotlin
options = listOf(onlineLabel, offlineOnlyLabel),
// RungRow(rung, isEffective, needsNetwork, isCapped, onCapClick, unavailableLabel, cappedLabel, needsNetworkLabel)
```

### Refused-message composition (byte-identical English)
```kotlin
val suffix = refused.changedItem?.let { ", $it ${refused.changedSinceSuffix}" } ?: ""
Text(text = "${refused.refusedPrefix} ${refused.reason}$suffix", /* … */)
```

## State of the Art
| Old | Current | Impact |
|---|---|---|
| In-place `data class` field append assumed additive | Metalava 0.5.0 flags ctor/`copy` removal as binary-breaking | Use `@JvmOverloads` + hand-written old-arity `copy` |

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | `onNodeWithText("<localized label>")` resolves the `OutlinedTextField` label in the merged tree (else use `useUnmergedTree = true`) | Pitfall 5 / tests | Test authoring friction only |
| A2 | Names `refusedPrefix` / `changedSinceSuffix` are acceptable to SecondBrain (brief only says "refused-prefix on undo model") | Models | Consumer must rename once on repin; Claude's-discretion area per CONTEXT |
| A3 | Leaving `SegmentedOptionSelector`'s English "selected"/"not selected" a11y text untouched is acceptable for VI18N-03 | Residual literals | SB TalkBack users hear mixed-language toggle announcement; may need a follow-up phase/requirement |

## Open Questions (RESOLVED)

1. **`SegmentedOptionSelector` a11y English ("selected"/"not selected")** — RESOLVED: out of scope; recorded as an explicit residual in 15-01/15-03 plans and a Phase 18 docs-note candidate.
   - Known: `SegmentedOptionSelector.kt:65` builds the contentDescription; it is public and shared; existing test pins English at `ApproachLadderCardTest.kt:77`.
   - Unclear: whether SB's localization needs this overridable.
   - Recommendation: out of Phase 15 scope (not in VI18N-01..04); record as a follow-up/backlog item and mention in the P18 docs note ("toggle a11y state words remain English"). If the owner wants it, add optional `selectedStateLabel`/`notSelectedStateLabel` params to `SegmentedOptionSelector` last — would also be a new API line.
2. **Refused-message word order** — RESOLVED: shipped as brief shape; limitation documented in KDoc per 15-02. prefix/suffix composition can't express reordered grammars. Recommendation: ship brief's shape; document the workaround (fold `changedItem` into `reason`, pass `changedItem = null`).
3. **Optional showcase (D-03)** — RESOLVED: skipped (D-03); gallery untouched; zero guard impact.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|---|---|---|---|---|
| JDK 17 | Gradle/Metalava | ✓ | `/usr/lib/jvm/java-17-openjdk-amd64` [VERIFIED: Metalava worker path in run log] | — |
| Gradle wrapper (offline) | all commands | ✓ | 9.4.1; ran `--offline` successfully in scratch | — |
| Android SDK (`local.properties` `sdk.dir`) | unit tests | ✓ | present (value not echoed) | — |

Operational notes: first Gradle invocation in a fresh directory once failed with `Caught exception: Already watching path: …` — rerun with `-Dorg.gradle.vfs.watch=false` (worked). Timings observed: `apiCheck` ≈ 23 s–1 m, `detekt` ≈ 6 s, the five target test classes ≈ 1 m 17 s (cold).

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit4 + Robolectric (`RobolectricTestRunner`, `@Config(sdk = [35])`) + `androidx.compose.ui.test.junit4.createComposeRule()` |
| Config file | none beyond `build.gradle.kts` (existing harness) |
| Quick run command | `./gradlew testDebugUnitTest --tests '*ProviderKeyCardTest' --tests '*ModelSelectCardTest' --tests '*ClarificationBarTest' --tests '*ApproachLadderCardTest' --tests '*OutcomeSheetTest'` (all five passed, scratch) |
| Full suite command | `./gradlew testDebugUnitTest` (includes `ComponentRegistryDriftGuardTest`, `DomainVocabularyDriftGuardTest`, `GeneratedSymbolDriftGuardTest`, `GalleryDemoInteractionTest`, `ShowTagColorsSourceCompatTest`) |
| Additive gate | `./gradlew apiDump` then `./gradlew apiCheck` (Metalava, release variant) |
| Static analysis | `./gradlew detekt` (zero baseline) |
| Guard self-tests | `bash tools/test/run-all.sh` (README-api-guard; run before release) |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| VI18N-01 | `providerLabel="Fournisseur"` renders; absent → "Provider" (existing selection tests still pass) | Compose UI | `./gradlew testDebugUnitTest --tests '*ProviderKeyCardTest'` | file ✅, new test ❌ Wave 0 |
| VI18N-01 | `modelLabel="Modèle"` renders; default "Model" | Compose UI | `… --tests '*ModelSelectCardTest'` (needs non-empty `models`, dropdown only renders then) | file ✅, new test ❌ |
| VI18N-02 | `dismissLabel="Fermer"` renders on the `clarification_bar_dismiss` button; default "Dismiss" | Compose UI | `… --tests '*ClarificationBarTest'` | file ✅, new test ❌ |
| VI18N-03 | each of 5 labels overridden: Unavailable (rung `enabled=false`), Capped (`maxTierId`+`onMaxTierChange`, rung rank > cap), Needs network (`offlineOnly=true`, rung `offlineCapable=false`), toggle segments via `onNodeWithContentDescription("<offlineOnlyLabel>, not selected")` | Compose UI | `… --tests '*ApproachLadderCardTest'` | file ✅, new tests ❌ |
| VI18N-04 | `undoneLabel`, `escalationsLabel` (set `escalationCount`), `refusedPrefix`+`changedSinceSuffix` (+`changedItem`), `removeContentDescription` (query `onAllNodesWithTag("outcome_sheet_confirmation_item_remove", useUnmergedTree = true)` + `onNodeWithContentDescription(…, useUnmergedTree = true)`) | Compose UI | `… --tests '*OutcomeSheetTest'` | file ✅, new tests ❌ |
| VI18N-04 | model defaults + old-arity `copy` equivalence (`copy(id,label,state)` preserves the custom `undoneLabel`; old ctor arities still compile) | JVM unit | new `…/model/VoiceModelLabelDefaultsTest.kt` (or similar) | ❌ Wave 0 |
| Success #5 (byte-identical English) | Existing assertions are the regression net: `OutcomeSheetTest.kt:203` (`hasText("Undone")`), `:325` (`"Couldn't undo: Item changed, Card 1 changed since"`), `:379`; `ApproachLadderCardTest.kt:77,130,202,228` | existing | quick run command above | ✅ |
| Additive API | `apiCheck` green after `apiDump` | gradle | `./gradlew apiDump && ./gradlew apiCheck` | ✅ |
| Guard | no registry/drift break (no new public composable) | JVM | `./gradlew testDebugUnitTest --tests '*ComponentRegistryDriftGuardTest' --tests '*DomainVocabularyDriftGuardTest'` | ✅ |
| Optional | v2.4-style call shapes still compile (named, positional-through-`modifier`, old-arity model ctor/`copy`) | compile-only | add `VoiceI18nSourceCompatTest.kt` modeled on `ShowTagColorsSourceCompatTest.kt` | ❌ optional |

### Sampling Rate
- **Per task commit:** the five-class quick run + `./gradlew apiCheck` (after `apiDump` when signatures changed).
- **Per wave merge:** `./gradlew testDebugUnitTest detekt apiCheck`.
- **Phase gate:** full `./gradlew testDebugUnitTest` + `detekt` + `apiCheck` green, plus `git diff v2.4.1 -- api.txt` showing only appended/re-signatured voice-surface lines before `/gsd-verify-work`.

### Wave 0 Gaps
- [ ] New override tests added to the five existing test files (use a non-English sentinel such as "Fournisseur" so the assertion can't pass on the default; also assert `onNodeWithText("Provider").assertDoesNotExist()` where practical).
- [ ] `src/test/java/io/github/ygaray/yahirandroidtaste/model/` test for model defaults + hand-written `copy` equivalence (only `TagChipUiModelTest.kt` exists there today).
- [ ] No framework install needed.

## Security Domain

`security_enforcement` is enabled in `.planning/config.json` (`"security_enforcement": true`, `"security_asvs_level": 1`).

### Applicable ASVS Categories
| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2/V3/V4 Auth/Session/Access | no | UI labels only; no auth surface |
| V5 Input Validation | minimal | Labels are caller-supplied `String`s rendered as plain `Text`/`contentDescription` (no HTML/format parsing, no `String.format` on caller text) — keep it that way; do not introduce `%s` templating |
| V6 Cryptography | no | `ProviderKeyCard` key handling is untouched (trim + mask unchanged; keys never stored/logged) |

### Known Threat Patterns
| Pattern | STRIDE | Mitigation |
|---------|--------|------------|
| Sensitive data printed via model `toString` | Information disclosure | Leave `ProposedItemUiModel.toString()` untouched (omits title/subtitle/cue); new label fields hold only static UI copy |
| INV-01 breach (library importing host code / secrets) | Tampering | No new imports beyond existing; P18 INV-02 does import-inspection over P15–P17 diffs |

## Project Constraints (from CLAUDE.md)
- One-way dependency; library imports no host/consumer code; no app-specific concepts (INV-01) — new labels are generic English words.
- `ComponentRegistry` drift guard: **no new public `@Composable`** is added in this phase, so no registry change; do not add one.
- Detekt zero baseline: do not regenerate `config/detekt-baseline.xml`; thresholds `LongParameterList` function/constructor 18 with `ignoreDefaultParameters: true` (`config/detekt-compose.yml:29-33`) — all new params are defaulted; the hand-written `copy` overloads' non-default params (≤7) are under 18. `detekt` passed in the scratch run.
- Additive-only API; tags immutable; **do not tag or repin** (phase cuts no tag; Phase 19 owns `v2.5.0`); commit on `main`, no consumer-file edits (sequential-in-hub convention).
- Gradle commands drop the module prefix (root is the module).
- Commits end with the `Co-Authored-By` attribution line from the session reminder.

## Sources

### Primary (HIGH — read/executed this session)
- Source: `component/{ProviderKeyCard,ModelSelectCard,ClarificationBar,ApproachLadderCard,OutcomeSheet,SegmentedOptionSelector}.kt`, `model/{HandledByUiModel,UndoRowUiModel,UndoRefusedUiModel,ProposedItemUiModel,TagChipUiModel,VoiceOutcomeUiState}.kt`
- Tooling: `build.gradle.kts` (metalava block, `apiDump`/`apiCheck` aliases), `tools/README-api-guard.md`, `tools/verify-api-additive.sh`, `tools/classify-hub-change.sh`, `tools/hooks/pre-commit`, `api.txt`, `config/detekt-compose.yml`
- Tests: `component/{ClarificationBar,ProviderKeyCard,ModelSelectCard,ApproachLadderCard,OutcomeSheet}Test.kt`, `ShowTagColorsSourceCompatTest.kt`
- Planning: `15-CONTEXT.md`, `ROADMAP.md` Phase 15, `REQUIREMENTS.md`, `.planning/v2.5-DECISION-MAP.md`, `.planning/cross-repo/RECONVENE-BRIEF-R-v1.1.md` (field names `escalationsLabel`, `removeContentDescription`)
- Empirical (scratch copy, outside the repo): naive append → `apiCheck` FAIL (8 errors); `@JvmOverloads` + old-arity `copy` → `apiCheck` / `detekt` / 5 test classes green; `apiDump` diff reviewed.

### Secondary / Tertiary
- None. No web research was needed (no external libraries involved).

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH — unchanged existing stack.
- Architecture / recipe: HIGH — reproduced green in a scratch copy.
- Pitfalls: HIGH for 1-4, 6; MEDIUM for 5 (assumed test-query behavior).

**Research date:** 2026-10-05
**Valid until:** 2026-11-04 (stable; invalidated only by a Metalava plugin/AGP bump)
