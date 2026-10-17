# Phase 27: Open from the sheet and Undo Center, pill fixes - Pattern Map

**Mapped:** 2026-10-17
**Files analyzed:** 23 (9 main, 14 test)
**Analogs found:** 23 / 23
All analog paths below are git-tracked (verified `git ls-files` for OutcomeSheet.kt and UndoCenterScreen.kt; the rest are under tracked `src/`). `M` = `src/main/java/io/github/ygaray/yahirandroidtaste`, `T` = `src/test/java/io/github/ygaray/yahirandroidtaste`.

Key correction (RESEARCH): `Success.target`, `BatchRowResultUiModel.target` and `onOpenTarget` do NOT exist yet; Phase 27 adds them. Do not assume Phase 25 shipped them.

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match |
|---|---|---|---|---|
| `M/model/VoiceOutcomeUiState.kt` (MOD, Success +target,targetLabel,targetDetail) | model | transform | same file (Success, lines 55-66) | exact |
| `M/model/BatchRowResultUiModel.kt` (MOD, +target) | model | transform | same file (lines 14-18) | exact |
| `M/component/OpenTargetRow.kt` (NEW: OpenTargetRow, OpenTargetBatchList, hasOpenTarget) | component (internal) | request-response (tap) | `M/component/ReceiptRow.kt` ReceiptRowSummary + `M/feedback/FeedbackCard.kt` chevron | role-match |
| `M/component/OutcomeSheet.kt` (MOD: `onOpenTarget` on both overloads, appended branch, new private Classic open body) | component | request-response | same file (dispatch lines ~88-135) | exact |
| `M/component/OutcomeSheetReceipt.kt` (MOD: thread onOpenTarget, pill shape, dot ring) | component | request-response | same file (ReceiptHandledByPill ~278-320) | exact |
| `M/component/HandledByPillShape.kt` (NEW internal Shape) | utility | transform | RESEARCH Code Examples "Shape" (no in-repo Shape analog) | partial |
| `M/component/HandledByDot.kt` (NEW: handledByDotRing + ring modifier) | utility | transform | `M/component/ColorUtils.kt` `contrastingForeground` + `T/theme/ContrastTestSupport.kt` | role-match |
| `M/feedback/UndoCenterScreen.kt` (MOD: new overload, private CompositionLocal, UndoHistoryOpenRow) | component | event-driven (gesture) | same file UndoHistoryRow (lines 295-340) + `M/feedback/FeedbackCard.kt` (150-205) | exact |
| `M/theme/Dimens.kt` (optional ring width under OutcomeReceipt) | config | n/a | `Dimens.OutcomeReceipt` block | exact |
| `api.txt` (regenerated) | config | n/a | Phase 25 apiDump precedent | exact |
| `T/component/OpenTargetRowTest.kt` | test | request-response | `T/component/OutcomeSheetHoistedStateTest.kt` + ReceiptRow tests | role-match |
| `T/component/OutcomeSheetOpenTargetTest.kt` | test | request-response | `T/component/OutcomeSheetHoistedStateTest.kt` (hostHoisted) | exact |
| `T/component/OutcomeSheetBatchOpenTargetTest.kt` | test | request-response | `T/component/OutcomeSheetClassicParityTest.kt` (tag inventories) | role-match |
| `T/component/OutcomeSheetOpenTargetLongLabelTest.kt` | test | request-response | `T/component/OutcomeSheetReceiptLongLabelTest.kt` | exact |
| `T/component/HandledByPillShapeTest.kt` | test | transform | `T/component/OutcomeSheetReceiptLongLabelTest.kt` | role-match |
| `T/component/HandledByDotContrastTest.kt` | test | transform | `T/theme/ToneTokensTest.kt` + `ContrastTestSupport.kt` | role-match |
| `T/feedback/UndoCenterScreenOpenTargetTest.kt` | test | event-driven | `T/feedback/UndoCenterScreenTest.kt` + FeedbackCard "Long hold then release does not open" test | role-match |
| `T/feedback/UndoCenterScreenOpenParityTest.kt` | test | transform | `T/feedback/UndoCenterScreenDefaultParityTest.kt` (reuse `UndoCenterScreenGoldens`) | exact |
| `T/feedback/UndoCenterScreenOpenTargetLongLabelTest.kt` | test | request-response | `T/feedback/UndoCenterScreenLongLabelTest.kt` | exact |
| `T/feedback/UndoCenterScreenSourceContractTest.kt` (EDIT count 1 to 2, deliberate per D-04) | test | source-contract | same file lines 23-40, 58-62 | exact |
| `T/component/OutcomeSheetFixtures.kt` (EXTEND success(...) + batchResultsWithTargets()) | test util | n/a | same file `success(` (147-160), `batchResults()` (122-125) | exact |
| `T/V271CallSiteCanaryTest.kt` (EXTEND) | test | compile canary | same file | exact |
| SHA-pin/contract tests (ClassicParitySource, ReceiptSource) | test | n/a | MUST stay green UNEDITED | n/a |

## Pattern Assignments

### Model fields (`VoiceOutcomeUiState.kt`, `BatchRowResultUiModel.kt`)
**Analog:** themselves (RESEARCH Pattern 1, verified lines). Append defaulted params after `endAction` / `detail`:
```kotlin
// VoiceOutcomeUiState.kt:55-66 current tail
        val startAction: SheetActionUiModel? = null,
        val endAction: SheetActionUiModel? = null
    ) : VoiceOutcomeUiState
// add: , val target: FeedbackTarget? = null, val targetLabel: String? = null, val targetDetail: String? = null
// BatchRowResultUiModel.kt:14-18: add val target: FeedbackTarget? = null after detail
```
Rules: append only, no `@Deprecated(HIDDEN)` shims, `./gradlew apiDump` in the same commit. KDoc: row title is `targetLabel ?: summary`; `openLabel` is TalkBack-only; CALLER clears `target` after Undo (D-14). Never use `target.key` outside the callback.

### `OpenTargetRow.kt` (internal, shared by both styles and the batch list)
**Analog 1:** `M/component/ReceiptRow.kt:303-309` (clickable idiom; but do NOT reuse ReceiptRow, it emits Expanded/Collapsed stateDescription):
```kotlin
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClickLabel = onClickLabel, role = Role.Button, onClick = onClick)
                } else { Modifier }
            )
```
**Analog 2 (chevron):** `M/feedback/FeedbackCard.kt:347-353`:
```kotlin
Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null,
    tint = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier = Modifier.size(Dimens.ReceiptRow.ChevronSize).testTag(FEEDBACK_STACK_CHEVRON_TEST_TAG))
```
**Core skeleton:** copy the "Shared row" block in RESEARCH Code Examples (Row, weighted Column with title/detail, `semantics(mergeDescendants = true) {}` then `clickable(onClickLabel = target.openLabel.ifBlank { title }, role = Role.Button)`, `heightIn(min = Dimens.TouchTarget)`, tags `outcome_sheet_target_row`, `outcome_sheet_target_chevron`).
**Batch list:** mirror `BatchResultsList` text rules (`M/component/OutcomeSheet.kt` ~327-355): `color = if (row.succeeded) onSurface else error`; detail only `if (!row.succeeded && row.detail != null)` in `labelSmall` error; keep `testTag("outcome_sheet_batch_row")` on every row; Column `padding(top = Dimens.ContentSpacing)`, `spacedBy(Dimens.HairlineSpacing)`. Rows without a target: no chevron, no clickable, no reserved space (D-03). `hasOpenTarget()` = `this is Success && (target != null || batchResults.any { it.target != null })`.

### `OutcomeSheet.kt` (both overloads, Classic open path)
**Analog:** same file, hoisted overload lines ~88-102 and no-state overload ~118-135.
Current dispatch (must stay byte-valid for contract tests):
```kotlin
    if (style == OutcomeSheetStyle.Receipt) {
        OutcomeSheetReceipt(outcome, onDismissRequest, modifier, colors, sheetState)   // line rewrite: append , onOpenTarget (lane 2)
        return
    }
    SheetScaffold(onDismissRequest = onDismissRequest, modifier = modifier, sheetState = sheetState) {
        OutcomeSheetContent(outcome, colors = colors)   // MUST stay literal
    }
```
Insert AFTER the Receipt `return`, BEFORE the legacy `SheetScaffold` (pure addition):
```kotlin
    if (onOpenTarget != null && outcome.hasOpenTarget()) {
        OutcomeSheetClassicOpenTarget(outcome, onDismissRequest, modifier, colors, sheetState, onOpenTarget)
        return
    }
```
Rules: append `onOpenTarget: ((FeedbackTarget) -> Unit)? = null` LAST on both overloads (no-state overload forwards `onOpenTarget = onOpenTarget`); add no second `SheetScaffold(onDismissRequest = onDismissRequest, modifier = modifier, sheetState = sheetState)` line in this file (give the helper's scaffold different args or put it in another file); no `return` before the Receipt guard; the new private Classic body lives in `OutcomeSheet.kt` (appended) because `HandledByRow` is private. NEVER edit `SuccessBody`, `HandledByRow`, `BatchResultsList`, `UndoRowItem`, `NeedsConfirmationBody`, `ProposedItemRow` or change their visibility. Body order (D-02): summary headline, `HandledByRow`, `OpenTargetRow`, batch list switch, editable (hidden while inFlight), `UndoAffordanceBody`. KDoc: library never hides the sheet (D-05); hide() on hoisted state then navigate.

### `OutcomeSheetReceipt.kt` (thread param, pill shape, dot ring)
**Thread:** `onOpenTarget: ((FeedbackTarget) -> Unit)? = null` as LAST (defaulted) param of `OutcomeSheetReceipt`, `OutcomeSheetReceiptContent`, `ReceiptSuccessBody` (tests call `OutcomeSheetReceiptContent(outcome)` with one arg). Insert open row between pill and editable slot; replace the batch line with the if/else keeping the literal `BatchResultsList(` (RESEARCH Pattern 4).
**Pill (POL-01), current code lines ~283-290:**
```kotlin
    Row(
        modifier = Modifier
            .padding(top = Dimens.ContentSpacing)
            .background(scheme.surfaceContainerHighest, CircleShape)   // swap CircleShape -> HandledByPillShape(...)
            .padding(vertical = Dimens.OutcomeReceipt.PillVerticalPadding, horizontal = Dimens.OutcomeReceipt.PillHorizontalPadding)
            .semantics(mergeDescendants = true) {}
            .testTag("outcome_sheet_handled_by"),
```
Keep exactly one `Text(` in the pill, `weight(1f, fill = false)`, the tag and mergeDescendants (source contract).
**Dot (POL-02), current:**
```kotlin
        Box(
            modifier = Modifier
                .size(Dimens.OutcomeReceipt.DotSize)
                .background(handledByDotColor(handledBy.onDevice, colors, scheme), CircleShape)
                .testTag("outcome_sheet_handled_by_dot")
        )
```
Add `.drawBehind { ring }` BEFORE `.background(...)` only when `handledByDotRing(...) != null`; do not use an inside border (shrinks the 6dp fill). `handledByDotColor` and `ToneDefaults.warn()` stay unchanged.
Commit rewrites of the above lines with `HUB_LANE_OVERRIDE=2` and a stated reason (lane classification).

### `HandledByPillShape.kt` (NEW internal Shape)
No in-repo Shape analog; use RESEARCH Code Examples "Shape":
```kotlin
internal data class HandledByPillShape(val oneLineContentHeight: TextUnit, val verticalPadding: Dp) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val cap = with(density) { (oneLineContentHeight.toPx() + 2 * verticalPadding.toPx()) / 2f }
        return Outline.Rounded(RoundRect(size.toRect(), CornerRadius(minOf(size.height / 2f, cap))))
    }
}
```
Cap math pulled into a pure function for the JVM test. Inputs: `MaterialTheme.typography.bodySmall.lineHeight`, `Dimens.OutcomeReceipt.PillVerticalPadding` (4dp).

### `HandledByDot.kt` (NEW)
**Analog:** `M/component/ColorUtils.kt:13` `fun contrastingForeground(background: Color): Color =` and the WCAG formula in `T/theme/ContrastTestSupport.kt:10-18`:
```kotlin
internal fun contrastRatio(a: Color, b: Color): Float {
    val la = a.luminance(); val lb = b.luminance()
    return (maxOf(la, lb) + 0.05f) / (minOf(la, lb) + 0.05f)
}
```
Main needs its own internal copy. Then:
```kotlin
internal fun handledByDotRing(fill: Color, pill: Color, scheme: ColorScheme): Color? {
    if (contrast(fill, pill) >= 3f) return null
    return listOf(scheme.onSurface, contrastingForeground(pill)).first { contrast(it, pill) >= 3f }
}
```
`pill` = `scheme.surfaceContainerHighest`. Ring radius = `size.minDimension/2f + ringWidth.toPx()`.

### `UndoCenterScreen.kt` (new overload + open row)
**Analog:** same file. Keep the v2.7.1 overload FIRST and byte-unchanged (signature lines 78-87: `entries, onNavigateBack, onUndo, groups = emptyList(), onUndoGroup = null, onClear`). Append the new overload after it:
```kotlin
fun UndoCenterScreen(entries, onNavigateBack, onUndo, groups = emptyList(), onUndoGroup = null,
    onOpenTarget: (FeedbackTarget) -> Unit,   // REQUIRED, non-null: a default makes trailing-lambda calls ambiguous
    onClear: () -> Unit) {
    CompositionLocalProvider(LocalUndoCenterOpenTarget provides onOpenTarget) {
        UndoCenterScreen(entries = entries, onNavigateBack = onNavigateBack, onUndo = onUndo,
            groups = groups, onUndoGroup = onUndoGroup, onClear = onClear)
    }
}
private val LocalUndoCenterOpenTarget = compositionLocalOf<((FeedbackTarget) -> Unit)?> { null }
```
**Row early branch** at the top of `UndoHistoryRow` (current body lines 295-340; the existing row):
```kotlin
private fun UndoHistoryRow(entry: UndoHistoryEntry, onUndo: () -> Unit, modifier: Modifier = Modifier) {
    var showPeek by remember(entry.id) { mutableStateOf(false) }
    Box(modifier = modifier.testTag("$UNDO_ROW_TEST_TAG_PREFIX${entry.id}")
        .pointerInput(entry.id) { detectTapGestures(onPress = { showPeek = true; tryAwaitRelease(); showPeek = false }) }) {
        ListItem(headlineContent = { Text(entry.message) },
            supportingContent = { RelativeTimestampText(entry.timestamp) },
            trailingContent = { when (entry.status) { UndoStatus.Available -> TextButton(onClick = onUndo) { Text("Undo") } ... } })
        if (showPeek) UndoPreviewPeek(preview = entry.preview, message = entry.message)
    }
}
```
Add: `val open = LocalUndoCenterOpenTarget.current; val target = entry.openableTarget; if (open != null && target != null) { UndoHistoryOpenRow(entry, onUndo, target, open, modifier); return }`.
**UndoHistoryOpenRow gesture + semantics analog:** `M/feedback/FeedbackCard.kt:150-162, 190-205`:
```kotlin
if (openTarget != null) {
    role = Role.Button
    onClick(label = openTarget.openLabel.ifBlank { item.message }) { latestBodyTap(openTarget); true }
}
...
.pointerInput(item.id) { detectTapGestures(onLongPress = { }, onTap = { ... }) }
```
Adapt: `semantics(mergeDescendants = true) { role = Role.Button; onClick(label = target.openLabel.ifBlank { entry.message }) { open(target); true } }`; one `detectTapGestures(onPress = { tryAwaitRelease(); showPeek = false }, onLongPress = { showPeek = true }, onTap = { latestOpen(target) })`; NO `clickable` on the row; read `target`/`open` via `rememberUpdatedState` (stale-callback guard after Undo); trailing = Undo `TextButton` then chevron (same chevron snippet as above). Same testTag as the v2.7.1 row. Rows that are Undone/Failed/targetless and group headers never reach this branch (D-04 refresh).

## Test Pattern Assignments

- **OutcomeSheetOpenTargetTest / OpenTargetRowTest:** copy `T/component/OutcomeSheetHoistedStateTest.kt` structure: `@RunWith(RobolectricTestRunner::class) @Config(sdk = [35])`, `createComposeRule()`, `hostHoisted(outcome, style)` using `rememberModalBottomSheetState()` and `OutcomeSheet(outcome=, onDismissRequest=, style=, sheetState=)`, plus `onNodeWithTag/onNodeWithText`. Assert `SemanticsActions.OnClick` label = openLabel, `Role.Button`, no `StateDescription`, counter == 1 on click, blank openLabel falls back to title, no handler or no target gives tag/text inventory equal to v2.7.1.
- **OutcomeSheetBatchOpenTargetTest:** inventory style from `OutcomeSheetClassicParityTest` (stable tag sets ~226-236); targets-less rows have no OnClick and no `outcome_sheet_target_chevron`.
- **LongLabel tests (OutcomeSheet + UndoCenter):** copy `T/feedback/UndoCenterScreenLongLabelTest.kt` header: `@Config(sdk = [35], qualifiers = "w360dp-h800dp") @GraphicsMode(GraphicsMode.Mode.NATIVE)`, `YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false)`, read geometry unclipped via `positionInRoot` + `size` (not `boundsInRoot`). Add fontScale 2.0 via `Density(density, fontScale = 2f)` with German strings; model on `T/component/OutcomeSheetReceiptLongLabelTest.kt` for the pill. Pill containment test needs a `CircleShape` control that FAILS (red first) and must add back `PillHorizontalPadding/PillVerticalPadding` because the tag sits after the inner padding.
- **HandledByDotContrastTest:** pure JVM; assert null for hub light/dark (`#FF251A00` on `#FFDCE3E5`, `#FFFFE0A0` on `#FF213836`), non-null with ring contrast >= 3 for synthetic white-on-light; include a mirror check against `T/theme/contrastRatio` so formulas cannot diverge; keep `OutcomeSheetReceiptSuccessTest` lines ~202-212 (`handledByDotColor`) green.
- **UndoCenterScreenOpenTargetTest:** build entries with `UndoHistoryStore().append(..., target = ...)` (Phase 25 overload) as in `UndoCenterScreenLongLabelTest.render`; cover Available opens once, Undone/Failed/targetless/group header do not, Undo tap neither opens nor peeks, long hold shows peek and release neither opens nor undoes, undo via store then tap leaves count 0. Hold-test analog: FeedbackCard "Long hold then release does not open".
- **UndoCenterScreenOpenParityTest:** reuse `UndoCenterScreenGoldens` from `UndoCenterScreenDefaultParityTest`; call the NEW overload with no openable rows and compare to D1..D4.
- **UndoCenterScreenSourceContractTest edit:** `countOccurrences(source, "fun UndoCenterScreen(") == 1` (lines 33-40) becomes 2, add an assertion on the second overload's param list; commit message says deliberate (D-04).
- **Fixtures:** extend `OutcomeFixtures.success(...)` (OutcomeSheetFixtures.kt:147-160) with defaulted `target/targetLabel/targetDetail`; add `batchResultsWithTargets()` next to `batchResults()` (122-125).
- **V271CallSiteCanaryTest:** add OutcomeSheet positional/named, `Success(...)` 10 positional args + `copy` + destructuring, `BatchRowResultUiModel(label, succeeded, detail)`, and a `UndoCenterScreen(..., onOpenTarget = ..., onClear)` call.

## Shared Patterns

### Pinned-code safety (applies to every OutcomeSheet/Receipt edit)
**Source:** `T/component/OutcomeSheetClassicParitySourceContractTest.kt:49-74, 182-210`, `OutcomeSheetReceiptSourceContractTest.kt:27-55, 127-146`. SHA-pinned: SuccessBody, UndoRowItem, HandledByRow, NeedsConfirmationBody, BatchResultsList, ProposedItemRow (visibility pinned too). Literal `OutcomeSheetContent(outcome, colors = colors)` and one legacy `SheetScaffold(...)` line must remain. These tests must pass unedited.

### Opaque key hygiene
Use `target.openLabel` only in semantics/testTag; never `target.key` in tags, text, contentDescription or logs (`FeedbackTarget.toString` already omits it, `M/feedback/FeedbackTarget.kt:19-21`).

### Source-compat / lane rules
Append defaulted params, trailing lambdas last, no new `@Deprecated(HIDDEN)` or `copy$default` shims; commit line rewrites with `HUB_LANE_OVERRIDE=2`; `./gradlew apiDump apiCheck`, then `bash tools/verify-api-source-additive.sh v2.7.1`. No new public `@Composable` so ComponentRegistry counts (67/6/73) are unchanged. Gradle: `--no-daemon`, never `--stop`.

### Test conventions
Robolectric `@Config(sdk = [35])`, semantics/tag inventories (no pixel capture), NATIVE graphics + `w360dp-h800dp` for long-string checks, detekt zero baseline applies to tests.

## No Analog Found

| File | Role | Data Flow | Reason |
|---|---|---|---|
| `M/component/HandledByPillShape.kt` | utility | transform | No custom `Shape` exists in the repo; use RESEARCH sketch (API shape ASSUMED, confirmed by compile + Wave 0 test) |

## Metadata

**Analog search scope:** `src/main/.../{component,feedback,model,theme}`, `src/test/.../{component,feedback,theme}`, RESEARCH.md verified excerpts.
**Pattern extraction date:** 2026-10-17
