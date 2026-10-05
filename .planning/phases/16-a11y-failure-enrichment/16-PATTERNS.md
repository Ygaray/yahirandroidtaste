# Phase 16: A11y + Failure enrichment - Pattern Map

**Mapped:** 2026-10-05
**Files analyzed:** 9 (4 main + api.txt + 4 test files; 1 new test)
**Analogs found:** 9 / 9

All analog paths are git-tracked (verified with `git ls-files`). Main root: `src/main/java/io/github/ygaray/yahirandroidtaste/`. Test root: `src/test/java/io/github/ygaray/yahirandroidtaste/`.

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match |
|---|---|---|---|---|
| `component/ApproachLadderCard.kt` (CapControl/RungRow) | component | request-response | `component/PresetChip.kt:94-106` + self | exact |
| `component/OutcomeSheet.kt` (FailureBody) | component | request-response | self + `Success.editableContent` slot | exact |
| `model/FailureActionUiModel.kt` | model | transform | `model/UndoRowUiModel.kt:35-47` | exact |
| `model/VoiceOutcomeUiState.kt` (Failure) | model | transform | `model/UndoRowUiModel.kt` recipe + `Success.editableContent` | exact |
| `api.txt` | config | batch | Phase 15 apiDump process | exact |
| `component/ApproachLadderCardTest.kt` (modify) | test | request-response | self | exact |
| `component/OutcomeSheetTest.kt` (modify) | test | request-response | self (`:143-200`) | exact |
| `model/VoiceModelLabelDefaultsTest.kt` + `component/VoiceI18nSourceCompatTest.kt` (modify) | test | transform | self | exact |
| `component/FailureRoleSourceContractTest.kt` (NEW) | test | file-I/O | `component/TextListBottomSheetEditMenuSourceContractTest.kt` + `SourceContractTestSupport.kt` | exact |

## Pattern Assignments

### `component/ApproachLadderCard.kt` (component, request-response)

**Analog:** itself (`CapControl`, lines ~187-204 of the 160-235 region) plus `component/PresetChip.kt:94-106` for min-size/selected precedent.

**Current CapControl (the seam to modify):**
```kotlin
@Composable
private fun CapControl(
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .semantics(mergeDescendants = true) {}
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        content = content
    )
}
```
The call site is `CapControl(onClick = onCapClick, modifier = modifier.fillMaxWidth().padding(vertical = Dimens.ContentSpacing).testTag("approach_ladder_card_rung")) {...}`. Outer padding stays OUTSIDE the clickable (do not move it).

**Precedent to copy (PresetChip.kt:93-103):**
```kotlin
Box(modifier = modifier.minimumInteractiveComponentSize(), ...) {
    Surface(modifier = Modifier
        .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
        .semantics(mergeDescendants = true) { selected = isSelected }, ...
```

**Target (per RESEARCH Pattern 1):** add private `selected: Boolean` to `CapControl` and `isSelected` to `RungRow`; call site passes `isSelected = onMaxTierChange != null && rung.id == maxTierId`. Gate on `onClick != null`:
```kotlin
.then(if (onClick != null) Modifier.minimumInteractiveComponentSize() else Modifier)
.semantics(mergeDescendants = true) {}
.then(if (onClick != null) Modifier.selectable(selected = selected, role = Role.RadioButton, onClick = onClick) else Modifier)
```
Imports: add `androidx.compose.foundation.selection.selectable`, `androidx.compose.material3.minimumInteractiveComponentSize`, `androidx.compose.ui.semantics.Role`; REMOVE `androidx.compose.foundation.clickable` (only used here; detekt zero-baseline). Update the CapControl KDoc ("clickable ONLY when..." -> selectable).

---

### `component/OutcomeSheet.kt` (FailureBody) (component, request-response)

**Analog:** itself. Current (lines ~290-315):
```kotlin
Surface(
    color = ..errorContainer, contentColor = ..onErrorContainer,
    shape = MaterialTheme.expressive.cardShapeLarge,
    modifier = Modifier.fillMaxWidth().testTag("outcome_sheet_failure_surface")
) {
    Column(modifier = Modifier.padding(Dimens.HorizontalPadding)) {
        Text(text = failure.reason, style = MaterialTheme.typography.headlineSmall)
        failure.handledBy?.let { HandledByRow(it) }
        failure.action?.let { action ->
            DynamicActionButton(
                label = action.label,
                role = ActionButtonDefaults.ActionButtonRole.Neutral,   // -> action.role
                onClick = action.onClick,
                modifier = Modifier.padding(top = Dimens.ContentSpacing).testTag("outcome_sheet_action_button")
            )
        }
    }
}
```
**Changes:** (1) `role = action.role`; (2) insert `failure.body?.invoke()` after `HandledByRow`, before action (nullable slot idiom same as `Success.editableContent`); (3) on the Surface modifier append
```kotlin
.then(if (!failure.semanticsPrefix.isNullOrBlank()) Modifier.semantics(mergeDescendants = true) {
    contentDescription = "${failure.semanticsPrefix} ${failure.reason}"
} else Modifier)
```
Add import `androidx.compose.ui.semantics.contentDescription` (and `semantics` if missing). Do NOT use `clearAndSetSemantics`; do NOT add `liveRegion`. `ActionButtonDefaults` import may become unused here — check before removing.

---

### `model/FailureActionUiModel.kt` (model, transform)

**Analog:** `model/UndoRowUiModel.kt:35-47` (the Phase 15 recipe).
```kotlin
data class UndoRowUiModel @JvmOverloads constructor(
    val id: String, val label: String, val state: UndoRowState,
    val undoneLabel: String = "Undone"
) {
    // Hand-written pre-v2.5 `copy` arity ... delegates with the CURRENT undoneLabel
    fun copy(id: String, label: String, state: UndoRowState): UndoRowUiModel =
        copy(id = id, label = label, state = state, undoneLabel = undoneLabel)
```
**Apply:** current file is `data class FailureActionUiModel(val label: String, val onClick: () -> Unit)` (no ctor annotation). Convert to `@JvmOverloads constructor`, append `val role: ActionButtonDefaults.ActionButtonRole = ActionButtonDefaults.ActionButtonRole.Neutral`, add `fun copy(label: String, onClick: () -> Unit) = copy(label = label, onClick = onClick, role = role)`. Import `io.github.ygaray.yahirandroidtaste.component.ActionButtonDefaults` (precedent: `VoiceOutcomeUiState.kt:4`). Add KDoc `@param role`, keep existing KDoc.

---

### `model/VoiceOutcomeUiState.kt` (Failure) (model, transform)

**Analog:** UndoRowUiModel recipe above; slot type from `Success.editableContent: (@Composable () -> Unit)? = null`; defaulted `ActionButtonRole` precedent `NeedsConfirmation.severity` (~line 116).

**Current:**
```kotlin
data class Failure(
    val reason: String,
    val handledBy: HandledByUiModel? = null,
    val action: FailureActionUiModel? = null
) : VoiceOutcomeUiState
```
**Target:** `data class Failure @JvmOverloads constructor(reason, handledBy, action, val body: (@Composable () -> Unit)? = null, val semanticsPrefix: String? = null)` with body `fun copy(reason: String, handledBy: HandledByUiModel?, action: FailureActionUiModel?): Failure = copy(reason = reason, handledBy = handledBy, action = action, body = body, semanticsPrefix = semanticsPrefix)`. Append order `body` then `semanticsPrefix` (preserves componentN ordinals). Add `@param body`, `@param semanticsPrefix` KDoc incl. "remember the lambda if compared/keyed" and "punctuation belongs to the prefix; joined by one ASCII space".

---

### `api.txt` (config)
Regenerate with `./gradlew apiDump` in the SAME commit as each model change; run `./gradlew apiCheck` BEFORE the dump (authoritative additive gate). Expect exactly two `-` lines (old `copy(optional ...)` for each class). Commit with `HUB_LANE_OVERRIDE=3` (every commit is lane 3 vs `v2.4.1`).

---

### `component/ApproachLadderCardTest.kt` (test, modify)
**Analog:** itself (Robolectric + `createComposeRule`, `rungNodes()` helper, `@Config(sdk=[35])`). Add per RESEARCH "Code Examples": selected/role asserts (D-01), cap-less `SemanticsMatcher.keyNotDefined(Role/Selected)` (D-03), stale maxTierId -> none selected, and touch-bounds NON-overlap test (NOT `assertTouchHeightIsEqualTo(48.dp)` - vacuous). Keep existing `assertHasNoClickAction` test.

### `component/OutcomeSheetTest.kt` (test, modify)
**Analog:** existing Failure tests at lines 143-200 (`Failure renders on the failure surface tag...`, `non-null action renders exactly one action button and invokes onClick on tap`, `null action renders NO action`). Add: body renders inside surface; prefix -> `assertContentDescriptionEquals("Erreur : Network down")` style sentinel; null/blank prefix -> ContentDescription key undefined; Save/Destructive roles each render one clickable button firing onClick (colors not assertable in Robolectric).

### `model/VoiceModelLabelDefaultsTest.kt` and `component/VoiceI18nSourceCompatTest.kt` (test, modify)
**Analog:** themselves. Compat test pattern: pure-JVM model shapes with assertEquals + composables inside non-invoked lambdas (header lines 1-40). Add the fixture block from RESEARCH (`Failure("r")`, 3-arg positional, destructuring, legacy `copy`, `FailureActionUiModel("l", {})`, default `Neutral`). Extend `assertLegacyCopyCarriesEveryField` with all-non-default `Failure` (reuse the SAME lambda instance for `body`) and `FailureActionUiModel(role = Destructive)`.

### `component/FailureRoleSourceContractTest.kt` (NEW, test, file-I/O)
**Analog:** `component/TextListBottomSheetEditMenuSourceContractTest.kt` (uses `SourceContractTestSupport`; package `io.github.ygaray.yahirandroidtaste.component`, class is `internal object` helper with `source(file)`, `stripComments(src)`, `functionBody(src, declaration, occurrence = 1)`, `countOccurrences`). Pattern:
```kotlin
private fun source(file: String): String = SourceContractTestSupport.source(file)
// body = SourceContractTestSupport.functionBody(
//     SourceContractTestSupport.stripComments(source("OutcomeSheet.kt")), "private fun FailureBody(")
// assertTrue(body.contains("role = action.role"))
// assertFalse(body.contains("role = ActionButtonDefaults.ActionButtonRole.Neutral"))
```
Verify the exact `FailureBody` declaration string in OutcomeSheet.kt before hardcoding.

## Shared Patterns

### Additive data-class evolution
**Source:** `model/UndoRowUiModel.kt:35-47`. **Apply to:** `FailureActionUiModel`, `Failure`. `@JvmOverloads constructor` + hand-written legacy-arity `copy` delegating to the full copy; plain append fails `apiCheck`.

### Min-size + selected semantics
**Source:** `component/PresetChip.kt:94-103`, `AppChip.kt:128`. **Apply to:** `CapControl`.

### Source-contract testing
**Source:** `component/SourceContractTestSupport.kt` (lines 16, 37, 88). **Apply to:** VFAIL-01 role-wiring test (rendered color is not assertable under Robolectric).

### Commit/gate hygiene
`HUB_LANE_OVERRIDE=3 git commit` (with Co-Authored-By line); `./gradlew testDebugUnitTest detekt apiCheck`; no new public `@Composable` (ComponentRegistry unaffected); no explorer/ edits; no tag/repin; imports library/AndroidX only (INV-01).

## No Analog Found
None. (Only non-precedented element: touch-bounds non-overlap assertion; use RESEARCH Code Examples.)

## Metadata
**Analog search scope:** `component/`, `model/`, test `component/` and `model/`
**Files scanned:** ~12 read/grepped; excerpts line-anchored to current HEAD
**Pattern extraction date:** 2026-10-05
