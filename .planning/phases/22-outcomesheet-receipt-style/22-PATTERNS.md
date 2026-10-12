# Phase 22: OutcomeSheet Receipt style - Pattern Map

**Mapped:** 2026-10-12
**Files analyzed:** 26 (6 main edits, 6 main new, 1 theme edit, 13 tests new/edited)
**Analogs found:** 26 / 26

`MAIN/` = `src/main/java/io/github/ygaray/yahirandroidtaste/`, `TEST/` = `src/test/java/io/github/ygaray/yahirandroidtaste/`. Every analog below was confirmed git-tracked (`git ls-files`); none is a gitignored mirror.

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|---|---|---|---|---|
| `MAIN/model/OutcomeBadge.kt` (new) | model (enum) | n/a | `model/SelectionMode` enum (same package) | exact |
| `MAIN/model/FailureTone.kt` (new) | model (enum) | n/a | same | exact |
| `MAIN/model/SheetActionUiModel.kt` (new) | model (data class) | request-response | `model/FailureActionUiModel.kt` | role-match |
| `MAIN/model/HandledByUiModel.kt` (edit, +2 fields) | model | n/a | itself (v2.5.0 widening of `escalationsLabel`) | exact |
| `MAIN/model/FailureActionUiModel.kt` (edit, +icon) | model | n/a | itself | exact |
| `MAIN/model/VoiceOutcomeUiState.kt` (edit Success/Failure/NeedsConfirmation) | model (sealed) | n/a | itself; privacy `toString` at 197-198 | exact |
| `MAIN/component/OutcomeSheetDefaults.kt` (new: Style/Colors/Defaults) | config/tokens | n/a | `MAIN/theme/ToneTokens.kt` (`ToneColors`, `ToneDefaults`) | exact |
| `MAIN/component/OutcomeSheet.kt` (edit: dispatch + 4 Classic deltas + 3 helpers to internal) | component | request-response | itself; Phase 21 early-branch in `SegmentedOptionSelector.kt` | exact |
| `MAIN/component/OutcomeSheetReceipt.kt` (new) | component | request-response | `OutcomeSheet.kt` (Classic arms) + `ToneCard.kt` + `DynamicActionButton.kt` | role-match |
| `MAIN/theme/Dimens.kt` (append `OutcomeReceipt`) | config/tokens | n/a | `Dimens.PillChip` (~223), Phase 21 `ReceiptRow`/`SegmentedPill` groups | exact |
| `TEST/component/OutcomeSheetReceiptNeedsConfirmationTest.kt` (new) | test (Robolectric) | bounds/semantics/click | `OutcomeSheetTest.kt` (tall NC at 858-870) + `SegmentedOptionSelectorPillTest.kt` | role-match |
| `TEST/component/OutcomeSheetReceiptSuccessTest.kt` (new) | test | semantics/order | `OutcomeSheetTest.kt` `topOf` (887) | role-match |
| `TEST/component/OutcomeSheetReceiptFailureTest.kt` (new) | test | semantics, LocalContentColor | `ToneCardTest.kt` | role-match |
| `TEST/component/OutcomeSheetReceiptTagsTest.kt` (new) | test | semantics | `OutcomeSheetTest.kt` tag tests | role-match |
| `TEST/component/OutcomeSheetReceiptLongLabelTest.kt` (new, NATIVE w360dp) | test | bounds | `SegmentedOptionSelectorPillTest.kt`, `ReceiptRowLongLabelTest.kt`, `ToneAtomsLongLabelTest.kt` | exact |
| `TEST/component/OutcomeSheetClassicParityTest.kt` (new) | test | semantics inventory | `OutcomeSheetTest.kt` | role-match |
| `TEST/component/OutcomeSheetClassicParitySourceContractTest.kt` (new) | test (plain JUnit) | source scan | `SegmentedOptionMaterialParitySourceContractTest.kt` | exact |
| `TEST/component/OutcomeSheetReceiptSourceContractTest.kt` (new) | test (plain JUnit) | source scan | `ToneFoundationInvariantsTest.kt` + `SourceContractTestSupport` | exact |
| `TEST/component/OutcomeSheetLiveRegionTest.kt` (new) | test | semantics | `OutcomeSheetTest.kt` (undo refused tests) | role-match |
| `TEST/component/OutcomeSheetSourceCompatV251Test.kt` (new) | test | compile canary | `VoiceI18nSourceCompatTest.kt` | exact |
| `TEST/model/OutcomeReceiptModelsTest.kt` (new) | test (JUnit) | model | `VoiceModelLabelDefaultsTest.kt` | exact |
| `TEST/model/VoiceModelLabelDefaultsTest.kt` (edit 3 arity pins) | test | reflection | itself (lines 167-170, 255-258, 286-289) | exact |
| `TEST/theme/DimensTest.kt` (append group) | test | tokens | itself | exact |
| `TEST/component/ToneFoundationInvariantsTest.kt` (add `component/OutcomeSheetReceipt.kt` + `OutcomeSheetDefaults.kt` to `phaseFiles`) | test | source scan | itself (lines 36-46) | exact |
| `api.txt` (regenerate per API commit) | config | n/a | Phase 20/21 api.txt diffs | exact |

Protected, must stay unedited: `OutcomeSheetTest.kt`, `FailureRoleSourceContractTest.kt`, `DataClassBinaryCompatShimTest.kt`.

## Pattern Assignments

### `MAIN/model/VoiceOutcomeUiState.kt` (widen three arms)

**Analog:** itself.

**Field-append rule** (Success at 46-53): append after `undo` with defaults, nothing else moves.
```kotlin
data class Success(
    val summary: String,
    val handledBy: HandledByUiModel? = null,
    val editableContent: (@Composable () -> Unit)? = null,
    val inFlight: Boolean = false,
    val batchResults: List<BatchRowResultUiModel> = emptyList(),
    val undo: UndoAffordanceUiModel? = null
    // + badge, titleActions, startAction, endAction (all defaulted) appended here
) : VoiceOutcomeUiState
```

**Failure** (77-83): keep `@JvmOverloads constructor`, append after `semanticsPrefix`, with `undo` LAST. Do NOT touch the v2.5.0 shims at 84-134 (hand-written 3-arg `copy`, hidden `(…, mask, marker)` ctor, `legacyCopyDefault`). The 3-arg `copy` delegates by naming every field, so it must gain the new fields in its delegation call (`title = title, transcript = transcript, ...`) or the build fails; the shim `legacyCopyDefault` (127-132) likewise passes `self.<field>`. Those two bodies are the only shim edits allowed.

**NeedsConfirmation** (175-187): insert `confirmEnabled: Boolean = true` and `titleActions` between `topLevelContent` (184) and `onConfirm` (185).

**Privacy toString to mirror on Failure** (197-198; Pitfall P4 / T-22-01):
```kotlin
override fun toString(): String =
    "NeedsConfirmation(items=${items.size}, selectionMode=$selectionMode, severity=$severity)"
```
For Failure, print only enum/boolean/null-ness values (tone, hasAction, ...), never `reason`, `title`, `transcript`, `semanticsPrefix`. Mirror the existing test `ProposedItemUiModel toString ... prints only id and amended` (`VoiceModelLabelDefaultsTest.kt:191-200`).

---

### `MAIN/model/HandledByUiModel.kt` / `FailureActionUiModel.kt` (append fields)

**Analog:** `HandledByUiModel.kt:27-34` (`@JvmOverloads constructor`, defaulted tail field `escalationsLabel = "Escalations:"`). Append `prefixLabel: String = "Handled by"`, `onDevice: Boolean = false` after it. Update the hand-written 5-arg `copy` (40-53) so its delegation also forwards `prefixLabel`/`onDevice`, and the `legacyCopyDefault` (~79-100) likewise. `FailureActionUiModel`: append `icon: ImageVector? = null` after `role`; the explicit 2-arg ctor (line ~34-35) and 2-arg `copy` (~42-43) stay and must forward the new field.

---

### `MAIN/model/SheetActionUiModel.kt`, `OutcomeBadge.kt`, `FailureTone.kt` (new)

**Analog:** `FailureActionUiModel.kt` (label + onClick + role action model) and an existing plain enum. Shape is given verbatim in RESEARCH "Target signatures" (`@Immutable data class SheetActionUiModel(label, onClick, emphasis, enabled = true, icon = null, role = Neutral, activeWhileInFlight = false)`). No `copy`-arity shims (D-02). Public model types need KDoc (the repo documents every param).

---

### `MAIN/component/OutcomeSheetDefaults.kt` (new)

**Analog:** `MAIN/theme/ToneTokens.kt` (`ToneColors` is a plain class with value `equals/hashCode/toString`, "a plain class on purpose, not a data class", lines ~10-19; `ToneDefaults.warn()` = tertiaryContainer/onTertiaryContainer, `error()` = errorContainer/error at ~47-55). Copy that for `OutcomeSheetColors(warnTone, errorTone)` and `OutcomeSheetDefaults.colors(warnTone = ToneDefaults.warn(), errorTone = ToneDefaults.error())`. `ReceiptTitleStyle` is a `@Composable get()` returning `MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold)`. Public composable-free (a `@Composable fun` inside an object is indented, so the column-0 registry/vocabulary guards skip it).

---

### `MAIN/component/OutcomeSheet.kt` (edit: dispatch, four Classic deltas, visibility)

**Analog:** itself plus `SegmentedOptionSelector.kt` early-branch (Phase 21-02), pinned by `SegmentedOptionMaterialParitySourceContractTest`.

**Current entry to widen** (62-70): append `style`, `colors` after `modifier`; add trailing comma to `modifier`.
```kotlin
fun OutcomeSheet(
    outcome: VoiceOutcomeUiState,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    SheetScaffold(onDismissRequest = onDismissRequest, modifier = modifier) {
        OutcomeSheetContent(outcome)
    }
}
```
Insert before the `SheetScaffold(...)` line: `if (style == OutcomeSheetStyle.Receipt) { OutcomeSheetReceipt(outcome, onDismissRequest, modifier, colors); return }`. The `SheetScaffold(...)` line stays textually identical; only the inner line becomes `OutcomeSheetContent(outcome, colors = colors)`.

**Content seam** (92): `internal fun OutcomeSheetContent(outcome, modifier = Modifier)` gains `colors` as a LAST defaulted param (existing tests call `OutcomeSheetContent(outcome)` and with `modifier =`).

**Scroll-body + pinned-footer skeleton to preserve and copy in Receipt** (93-114):
```kotlin
Column(modifier = modifier.fillMaxWidth()) {
    Column(modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
        when (outcome) { ... }
    }
    when (outcome) {
        is VoiceOutcomeUiState.Success -> Unit
        is VoiceOutcomeUiState.Failure -> Unit
        is VoiceOutcomeUiState.NeedsConfirmation -> NeedsConfirmationActions(outcome)
    }
}
```

**The four Classic deltas (enumerated in P1, all lane 2, `HUB_LANE_OVERRIDE=2`):**
1. `NeedsConfirmationActions` Confirm button (417-422): add `enabled = confirmation.confirmEnabled,` between `onClick` and `modifier`. Cancel untouched.
2. `FailureBody` (292-296): `color`/`contentColor` lines become `if (failure.tone == FailureTone.Warn) colors.warnTone.container else MaterialTheme.colorScheme.errorContainer` (likewise content). Error default must stay `errorContainer`/`onErrorContainer`, NOT `colors.errorTone` (content `error` differs). The function must remain `private fun FailureBody(` and its body must not contain the substring `Neutral` (`FailureRoleSourceContractTest`).
3. After `FailureBody(outcome)` in the scroll `when` (101): render `failure.undo` below the surface, wrapped in `Modifier.padding(horizontal = Dimens.HorizontalPadding)` (the Failure surface is edge-to-edge).
4. `UndoAffordanceBody` refused `Surface` (168-176): append `.semantics { liveRegion = LiveRegionMode.Assertive }` after `.testTag("outcome_sheet_undo_refused")`. New imports: `androidx.compose.ui.semantics.LiveRegionMode`, `androidx.compose.ui.semantics.liveRegion`. Do NOT add `mergeDescendants`.
```kotlin
modifier = Modifier
    .fillMaxWidth()
    .padding(top = Dimens.ContentSpacing)
    .testTag("outcome_sheet_undo_refused")
    .semantics { liveRegion = LiveRegionMode.Assertive }
```

**Visibility change (P2):** make exactly `UndoAffordanceBody` (150), `BatchResultsList` (249), `ProposedItemRow` (433) `internal fun`. Leave `FailureBody`, `SuccessBody`, `HandledByRow`, `NeedsConfirmationBody`, `NeedsConfirmationActions`, `UndoRowItem` private.

---

### `MAIN/component/OutcomeSheetReceipt.kt` (new)

**Analogs:** Classic arms in `OutcomeSheet.kt` (structure, tags, shared blocks), `ToneCard.kt` (failure card), `DynamicActionButton.kt` (buttons), `ReceiptRow.kt`/`PillChip.kt` (Phase 20/21 tag and Dimens idiom).

**Imports convention:** `androidx.*`, `kotlin.*`, `java.*`, `android.*`, and hub packages only (`ToneFoundationInvariantsTest` import-prefix scan). No `Color(0x` literals, no `testTagsAsResourceId`.

**Frame (D-05):** `SheetScaffold(onDismissRequest, modifier, shape = RoundedCornerShape(topStart = Dimens.OutcomeReceipt.CornerRadius, topEnd = Dimens.OutcomeReceipt.CornerRadius)) { OutcomeSheetReceiptContent(outcome, colors) }`. `shape` precedes the trailing `content` in `SheetScaffold` (`SheetScaffold.kt:54-61`). `OutcomeSheetReceiptContent` is `internal` (test seam, mirrors `OutcomeSheetContent`) and copies the same scroll-body + pinned-sibling skeleton above (pinned bar is a sibling AFTER the `weight(1f, fill = false)` column, P6).

**Failure card (D-09):** `ToneCard` signature (`ToneCard.kt:27-31`) is `ToneCard(tone, modifier, content: ColumnScope.() -> Unit)`; it applies `.testTag("tone_card")` AFTER the caller modifier so the caller tag wins:
```kotlin
Surface(modifier = modifier.testTag("tone_card"), shape = RoundedCornerShape(Dimens.CornerRadius.Medium),
        color = tone.container, contentColor = tone.content)
```
Use `ToneCard(tone = failureToneColors(failure.tone, colors), modifier = Modifier.fillMaxWidth().testTag("outcome_sheet_failure_surface")) { ... }`. Keep the `semanticsPrefix` merge from Classic `FailureBody` (304-312) on the card modifier:
```kotlin
.then(if (!failure.semanticsPrefix.isNullOrBlank()) Modifier.semantics(mergeDescendants = true) {
    contentDescription = "${failure.semanticsPrefix.trim()} ${failure.reason}" } else Modifier)
```

**Buttons:** `DynamicActionButton(label, role, onClick, enabled, modifier, emphasis, icon)` (`DynamicActionButton.kt` signature ~66-74). NC bar: start `DynamicActionButton(label = cancelLabel, role = Neutral, emphasis = ActionButtonEmphasis.Text, ... testTag("outcome_sheet_confirmation_cancel"))`, end `Filled` with `role = confirmation.severity`, `enabled = confirmation.confirmEnabled`, tag `outcome_sheet_confirmation_confirm`. Failure/Success use `outcome_sheet_action_start`/`_end`. A node carries one tag, so do not double-tag.

**Pinned bar (D-05, code example in RESEARCH "Pinned bar"):** `Column(Modifier.fillMaxWidth().testTag("outcome_sheet_action_bar"))` containing `HorizontalDivider(Modifier.testTag("outcome_sheet_action_bar_divider"), thickness = 1.dp, color = outlineVariant)` then `Row(padding start 16, end 16, top 12, bottom 16, CenterVertically) { start; Spacer(Modifier.weight(1f)); end }` using `Dimens.OutcomeReceipt` tokens. Escape hatch only if the German test fails: weighted actions + `SpaceBetween`, no Spacer (P7).

**Shared blocks, not copies (VXR-02):** call `UndoAffordanceBody(undo, locked = success.inFlight)`, `BatchResultsList`, `ProposedItemRow` (made internal) from this file. Success order: title row -> pill -> `editableContent?.takeIf { !success.inFlight }` (OQ-1: hide while inFlight per model KDoc, record as A4) -> batchResults -> undo -> actions row with 24dp above, not pinned. Mirror Classic's `success.editableContent?.takeIf { !success.inFlight }?.invoke()` (OutcomeSheet.kt:135).

**Pure internal seams** (copy shape from `emphasisColors(emphasis, role, scheme)` in `DynamicActionButton.kt`; bodies are in RESEARCH "Pure seams"): `failureToneColors`, `outcomeBadgeIcon`, `outcomeBadgeColors`, `actionEnabled` (`action.enabled && (!inFlight || action.activeWhileInFlight)`), `handledByDotColor`.

**Handled-by pill (D-10, P9):** one `Text(buildAnnotatedString { ... })` so merged text reads `"Handled by Local grammar · offline"`; SemiBold `onSurface` span for tier; 6dp dot is a separate text-less `Box` (test tag `outcome_sheet_handled_by_dot`); container tagged `outcome_sheet_handled_by` with `semantics(mergeDescendants = true)`; text `Modifier.weight(1f, fill = false)`. Secondary-join pattern from Classic `HandledByRow` (351-359):
```kotlin
val secondary = buildList {
    handledBy.approach?.let { add(it) }
    handledBy.provider?.let { add(it) }
    handledBy.model?.let { add(it) }
    handledBy.escalationCount?.let { add("${handledBy.escalationsLabel} $it") }
}
... secondary.joinToString(" · ")
```

**Title row slots:** wrap consumer `titleActions` in `Box(Modifier.testTag("outcome_sheet_title_actions"))`; badge tagged `outcome_sheet_badge`; title `outcome_sheet_title` with `heading()` semantics (`androidx.compose.ui.semantics.heading`).

**Tag matrix:** see RESEARCH "Tag matrix (VTAG-01)". Receipt NC also keeps `outcome_sheet_confirmation_item`, `_item_remove`, `_top_level_content` (reuse `ProposedItemRow` and the same `Box(testTag("outcome_sheet_confirmation_top_level_content"))` from `NeedsConfirmationBody`, OutcomeSheet.kt:388-390).

**Declaration shape (drift guard):** Receipt composables are `private`/`internal`; no public top-level `@Composable`, so no registry/vocabulary edit.

---

### `MAIN/theme/Dimens.kt` (append `object OutcomeReceipt`)

**Analog:** `object PillChip` (Dimens.kt ~223-231) and Phase 21's `ReceiptRow`/`SegmentedPill` groups: nested object, one KDoc line per `val X: Dp = N.dp`. Values listed in RESEARCH "Receipt arms" (CornerRadius 24, BadgeSize 32, BadgeIconSize 18, TitleRowGap 8, BodyTopPadding 4, TranscriptTop 4, TranscriptBottom 8, BarTop 12, BarBottom 16, SuccessActionsTop 24, SuccessActionsBottom 16, PillVertical 4, PillHorizontal 12, DotSize 6, DotGap 6). Name it `OutcomeReceipt`, not `OutcomeSheet`. Existing reusable tokens: `HorizontalPadding` 16, `ContentSpacing` 4, `CompactPadding` 12, `HairlineBorder` 1, `HairlineSpacing`. Extend `DimensTest` append-only.

---

## Test Pattern Assignments

### Robolectric semantics/click tests (`OutcomeSheetReceipt{NeedsConfirmation,Success,Failure,Tags}Test`, `OutcomeSheetLiveRegionTest`, `OutcomeSheetClassicParityTest`)

**Analog:** `TEST/component/OutcomeSheetTest.kt` (header lines 53-58; protected, copy only).
```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OutcomeSheetTest {
    @get:Rule
    val composeTestRule = createComposeRule()
```
Host the internal seam directly, no `ModalBottomSheet` (P6). Bounded height idiom (858-870):
```kotlin
composeTestRule.setContent { Box(Modifier.height(240.dp)) { OutcomeSheetContent(outcome = tallNeedsConfirmationFixture()) } }
composeTestRule.onNodeWithTag("outcome_sheet_confirmation_confirm").assertIsDisplayed().assertHeightIsAtLeast(40.dp)
```
Receipt version: same with `OutcomeSheetReceiptContent`, asserting `outcome_sheet_action_start/_end` or `_confirmation_*` stay displayed.

**Node order idiom** (887-888), use `useUnmergedTree = true`:
```kotlin
private fun topOf(tag: String): Float =
    composeTestRule.onNodeWithTag(tag, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.top
```
Success order: title < `outcome_sheet_handled_by` < editable probe < undo/batch rows < `outcome_sheet_action_start`.

**Live region assertion** (RESEARCH P10): `onNodeWithTag("outcome_sheet_undo_refused").assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Assertive))` and `onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.LiveRegion)).assertCountEquals(1)`.

**Disabled-confirm:** `assertIsNotEnabled()`, `performClick()`, assert `onConfirm` call count 0 (copy the click-once pattern from `SegmentedOptionSelectorPillTest.kt:116-119`, `calls += it`).

**Color proofs without pixels** (`ToneCardTest.kt:46-66, 68-82`): capture `LocalContentColor.current` inside a slot (`Failure.body`) and compare to `colors.warnTone.content`; Error default must equal `onErrorContainer`. Pure seams get plain JUnit asserts (`assertSame(Icons.AutoMirrored.Filled.Undo, outcomeBadgeIcon(OutcomeBadge.Reverted))`).

### Bounds / long-label tests (`OutcomeSheetReceiptLongLabelTest`, 384dp edge test)

**Analog:** `SegmentedOptionSelectorPillTest.kt:50-112` (also `ReceiptRowLongLabelTest.kt`, `ToneAtomsLongLabelTest.kt`).
```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp")   // 384 test: "w384dp-h800dp"
@GraphicsMode(GraphicsMode.Mode.NATIVE)
...
composeTestRule.setContent {
    val density = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale ?: density.fontScale)) {
        YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) { Box(...) { ... } }
    }
}
composeTestRule.waitForIdle()

private fun SemanticsNodeInteraction.edges(): Pair<Float, Float> {
    val node = fetchSemanticsNode(); val left = node.positionInRoot.x
    return left to left + node.size.width
}
private fun SemanticsNodeInteraction.textLayout(): TextLayoutResult { /* GetTextLayoutResult action, results.single() */ }
```
Assertions: unclipped edges (16dp / 368dp at 384dp: `assertEquals(16f * density, start.edges().first, 0.5f)`), no ellipsis via `isLineEllipsized`, pill right edge <= 360dp minus 16dp. One `setContent` per test. No `captureToImage`. Use long German strings (memory: test-localized-long-strings-before-signoff).

### Source-contract tests (`OutcomeSheetClassicParitySourceContractTest`, `OutcomeSheetReceiptSourceContractTest`)

**Analog:** `SegmentedOptionMaterialParitySourceContractTest.kt` (plain JUnit4) over `SourceContractTestSupport` (`source(file)` is hard-wired to `component/`; `stripComments`, `functionBody(src, "fun X(")`, `countOccurrences`).
```kotlin
private val stripped = SourceContractTestSupport.stripComments(SourceContractTestSupport.source("OutcomeSheet.kt"))
private fun body() = SourceContractTestSupport.functionBody(stripped, "fun OutcomeSheet(").filterNot { it.isWhitespace() }
// order: val guard = b.indexOf("if(style==OutcomeSheetStyle.Receipt){"); call; ret = indexOf("return", call); legacy = indexOf("SheetScaffold(onDismissRequest=onDismissRequest,modifier=modifier){")
// assertTrue(guard < call < ret < legacy); countOccurrences(b, legacy) == 1
```
Also pin: the Error color lines (`MaterialTheme.colorScheme.errorContainer` / `onErrorContainer`) survive in `private fun FailureBody(`; exactly one public `fun OutcomeSheet(`; no `DeprecationLevel.HIDDEN` added. Receipt contract: file contains `shape=RoundedCornerShape(topStart=` with 24dp token passed to `SheetScaffold`, no `Color(0x`, no `testTagsAsResourceId`, no `Neutral`-style leakage. The shape cannot be seen in semantics (Phase 21 Pitfall 9), so source contract plus Gate-1 corner measurement. Note `functionBody` throws on expression-bodied functions and needs a block-bodied `fun OutcomeSheet(` (it is).

### `OutcomeSheetSourceCompatV251Test` (compile canary)

**Analog:** `TEST/component/VoiceI18nSourceCompatTest.kt`. Build each v2.5.1 call shape with named args only (`NeedsConfirmation(reason=, items=, onConfirm=, onCancel=)`, `Failure("r", null, null)`, 3-arg `OutcomeSheet(outcome, {}, Modifier)`, trailing-lambda `FailureActionUiModel("l") { }`, destructuring `Success`), then the full named new shape. Compile success is the assertion.

### `VoiceModelLabelDefaultsTest` arity pins (edit in the SAME commit as each widening)

**Analog:** itself, verbatim current assertions:
- 167-170 `HandledByUiModel`: `constructorArities(...).containsAll(listOf(5, 6))`, `copyArities(...).containsAll(listOf(5, 6))` -> copy list becomes `listOf(5, 8)` (ctor list stays valid).
- 255-258 `FailureActionUiModel`: `listOf(2, 3)` for both -> `listOf(2, 4)`.
- 286-289 `Failure`: ctor `listOf(1, 2, 3, 4, 5)` stays; copy `listOf(3, 5)` -> `listOf(3, 12)`.
Values are hand-derived (A2); executor confirms on first run. Rename the test titles only if they now lie ("new six-arg"). Do not touch `DataClassBinaryCompatShimTest`.

### `ToneFoundationInvariantsTest` (edit)

**Analog:** itself, `phaseFiles` list at lines 36-46. Append `"component/OutcomeSheetReceipt.kt"` and `"component/OutcomeSheetDefaults.kt"` so the import-prefix scan (87-95), `Color(0x` scan (100) and column-0 declaration scan (104+) cover them. The `testTagsAsResourceId` scan already covers all `src/main`.

## Shared Patterns

### Stable tag placement
**Source:** `ToneCard.kt` (`modifier.testTag("tone_card")` after caller modifier), `OutcomeSheet.kt` tags. **Apply to:** all Receipt nodes. Fixed tag AFTER caller modifier; a node carries one tag; the library never sets `testTagsAsResourceId`; test-only tags (`outcome_sheet_action_bar_divider`, `outcome_sheet_handled_by_dot`) stay out of API docs.

### Merged-semantics rows
**Source:** `OutcomeSheet.kt:220` and `441` (`.semantics(mergeDescendants = true) {}` placed before `.testTag(...)`). **Apply to:** handled-by pill container, any new Receipt row that must be indexed by `onAllNodesWithTag`.

### Early branch above an untouched legacy block, pinned by source contract
**Source:** `SegmentedOptionSelector.kt` + `SegmentedOptionMaterialParitySourceContractTest.kt`. **Apply to:** `OutcomeSheet` dispatch.

### Pure palette seams instead of pixel reads
**Source:** `emphasisColors` in `DynamicActionButton.kt`; `ToneCardTest` `LocalContentColor` capture. **Apply to:** badge, dot, tone, enabled formula.

### Source-compat-only widening
**Source:** CLAUDE.md invariants, D-02. Append defaulted params, trailing lambdas last, no new `@Deprecated(HIDDEN)`, no `copy$default` synthetics. Per API commit: `./gradlew apiDump` then `apiCheck`, commit `api.txt` with it. After 22-02, `apiDump` must leave `api.txt` byte-identical. Lane-2 commits (`HUB_LANE_OVERRIDE=2`): OutcomeSheet signature, the four Classic deltas, the three visibility rewrites. Never stage `.planning/graphs/*`, `.planning/uat-pending/16*`-`18*`, `.planning/WAITING.json`; no tag, no push.

### Gradle hygiene
One gradle invocation at a time; `--tests` binds to the preceding task; text geometry needs `@GraphicsMode(NATIVE)`; one `setContent` per test method.

## No Analog Found

| File/Concern | Role | Data Flow | Reason |
|---|---|---|---|
| Pinned bar with `Spacer(weight 1f)` between two non-weighted actions | component | layout | Closest is Classic `NeedsConfirmationActions` (`Arrangement.spacedBy(ContentSpacing, Alignment.End)`, OutcomeSheet.kt:404-410), which differs (end-aligned pair); use RESEARCH "Pinned bar" snippet and P7 fallback |
| `liveRegion` semantics | a11y | semantics | No `liveRegion` in repo yet; use RESEARCH P10 snippet |
| Single-`Text` `AnnotatedString` pill | component | render | No `buildAnnotatedString` pill in repo; follow P9 |
| Recording-`Shape` probe for sheet shape | test | n/a | `ModalBottomSheet` shape not in semantics; source contract plus Gate-1 corner measurement |

## Metadata

**Analog search scope:** `MAIN/component`, `MAIN/model`, `MAIN/theme`; `TEST/component`, `TEST/model`, `TEST/theme`.
**Files read:** OutcomeSheet.kt (full), VoiceOutcomeUiState.kt (40-200), HandledByUiModel.kt (20-70), ToneCard.kt, DynamicActionButton.kt (55-90), SourceContractTestSupport.kt, SegmentedOptionMaterialParitySourceContractTest.kt, SegmentedOptionSelectorPillTest.kt (1-120), ToneCardTest.kt (1-90), OutcomeSheetTest.kt (targeted), VoiceModelLabelDefaultsTest.kt (targeted), ToneFoundationInvariantsTest.kt (targeted), plus CONTEXT, RESEARCH, 21-PATTERNS.
**Pattern extraction date:** 2026-10-12
