# Phase 21: Receipt row, Pill selector & sheet shape - Pattern Map

**Mapped:** 2026-10-12
**Files analyzed:** 17 (3 main edits, 1 main new, 1 theme edit, 3 explorer edits, 9 test files incl. edits)
**Analogs found:** 17 / 17

All analog paths below are git-tracked (checked via `git ls-files` for the test files; main sources are under `src/main/java/io/github/ygaray/yahirandroidtaste/`, referred to as `MAIN/`).

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|---|---|---|---|---|
| `MAIN/component/ReceiptRow.kt` (new) | component (stateless) | request-response (state-in/callback-out) + animation | `MAIN/component/PillChip.kt` + `MAIN/component/ToneCard.kt` | role-match |
| `MAIN/component/SegmentedOptionSelector.kt` (edit) | component | request-response | `DynamicActionButton.kt` early-branch widening (Phase 20) | exact (technique) |
| `MAIN/component/SheetScaffold.kt` (edit) | component | request-response | itself (pure param append) | exact |
| `MAIN/theme/Dimens.kt` (append `ReceiptRow`, `SegmentedPill`) | config/tokens | n/a | `Dimens.PillChip` (Dimens.kt:223) | exact |
| `MAIN/explorer/CardsFamilyScreen.kt` (append entry + Variants) | registry | n/a | `ToneCard` entry (lines 287-301) | exact |
| `MAIN/explorer/PickersFamilyScreen.kt` (append Pill/disabled demos) | registry demo | n/a | `SegmentedOptionSelectorVariants` (line ~449) | exact |
| `MAIN/explorer/SheetsFamilyScreen.kt` (append shaped-sheet button) | registry demo | n/a | `SheetScaffoldSection` (~794-806) | exact |
| `src/test/.../component/ReceiptRowTest.kt` (new) | test (Robolectric) | bounds/semantics | `ToneCardTest.kt`, `SegmentedOptionSelectorTest.kt` | role-match |
| `.../component/ReceiptRowLongLabelTest.kt` (new, NATIVE w360dp) | test | bounds | `SegmentedOptionSelectorTest.kt` / `ApproachLadderCardLongLabelTest` | exact |
| `.../component/ReceiptRowLineHeightTest.kt` (new, Wave 0) | test | bounds | `SegmentedOptionSelectorTest.kt` harness | role-match |
| `.../component/ReceiptRowSourceContractTest.kt` (new) | test (plain JVM) | source scan | `DynamicActionButtonLegacyPathSourceContractTest.kt` | exact |
| `.../component/SegmentedOptionSelectorLegacyPathSourceContractTest.kt` (new) | test (plain JVM) | source scan | same | exact |
| `.../component/SegmentedOptionSelectorTest.kt` (append Pill/disabled tests; existing must pass unedited) | test | semantics/bounds | itself | exact |
| `.../component/SheetScaffoldShape*Test.kt` (new, source contract) | test | source scan | `DynamicActionButtonLegacyPathSourceContractTest.kt` | role-match |
| `.../explorer/ToneAtomsRegistryRenderTest.kt` (append `ReceiptRow` test) or new sibling | test | render | itself | exact |
| `.../component/ToneFoundationInvariantsTest.kt` (extend `phaseFiles` with `component/ReceiptRow.kt`) | test | source scan | itself | exact |
| `.../explorer/DomainVocabularyDriftGuardTest.kt` (add `"ReceiptRow" to "<rationale>"` in `DOMAIN_VOCABULARY`) | test | guard | existing entries | exact |
| `api.txt` (regenerate with `apiDump`) | config | n/a | Phase 20 api.txt diffs | exact |

## Pattern Assignments

### `MAIN/component/ReceiptRow.kt` (new component)

**Analogs:** `PillChip.kt` (tag, alpha, border, text idiom), `ToneCard.kt` (tone/tag/Dimens idiom).

**Imports and Dimens pattern** (ToneCard.kt:3-13): library-only imports, `io.github.ygaray.yahirandroidtaste.theme.Dimens` / `ToneColors` / `ToneDefaults`.

**Tag-after-modifier pattern** (ToneCard.kt:33-34, PillChip.kt:88-91): caller modifier first, then `.testTag(...)` so a caller tag wins. For `ReceiptRow` the root `Column(modifier.fillMaxWidth())`; the inner tags (`receipt_row_summary`, `receipt_row_trailing_action`, `receipt_row_expanded_content`) are fixed.
```kotlin
// ToneCard.kt:33-34
Surface(
    modifier = modifier.testTag("tone_card"),
```

**Disabled/dimmed alpha** (PillChip.kt:33, 92): reuse `internal const val DISABLED_CONTENT_ALPHA = 0.38f` (same package); `ReceiptRow` `dimmed` should reuse it rather than redefine (or define `RECEIPT_ROW_DIMMED_ALPHA = DISABLED_CONTENT_ALPHA`).
```kotlin
.alpha(if (enabled) 1f else DISABLED_CONTENT_ALPHA)
```

**Clickable with role + semantics** (PillChip.kt:95-96):
```kotlin
.clickable(enabled = enabled, role = Role.Button, onClick = onClick)
.semantics { if (contentDescription != null) this.contentDescription = contentDescription }
```
For ReceiptRow: summary Row = `tag -> semantics(mergeDescendants = true){stateDescription=...} -> clickable(role = Role.Button, onClickLabel = ...) -> padding(vertical)` (padding AFTER clickable, per RESEARCH Pattern 2). `trailingAction` Box is a SIBLING of this Row inside the outer summary line.

**Single-line text idiom** (PillChip.kt:109-116; use instead of `softWrap=false`):
```kotlin
Text(
    text = label,
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.onSurface,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    modifier = Modifier.weight(1f, fill = false)
)
```
Per RESEARCH Pitfall 1, map every ReceiptRow text style through `copy(lineHeightStyle = LineHeightStyle(Alignment.Center, Trim.None))` (no analog in repo; use RESEARCH skeleton).

**Hairline/Divider**: `HorizontalDivider(thickness = Dimens.HairlineBorder, color = MaterialTheme.colorScheme.outlineVariant)`.

**Rotation animation precedent**: `MAIN/component/ExpandableFab.kt:116-119` (`animateFloatAsState(targetValue = if (expanded) 45f else 0f, label = "fab_rotation")`). Copy shape; use `tween(200)` and the `internal` seam functions from RESEARCH Pattern 4.

**Tone mapping**: `ContentTone` -> Color via `onSurface` / `onSurfaceVariant` / `ToneDefaults.warn().content` / `ToneDefaults.error().content` (theme/ToneTokens.kt; D-05).

**Declaration shape (drift-guard critical):** `@Composable` on its own column-0 line, then `fun ReceiptRow(` on the next column-0 line, params one per line. Sub-parts must be `private`/`internal`.

---

### `MAIN/component/SegmentedOptionSelector.kt` (edit; Pill branch + disabledOptions)

**Analog (technique):** `DynamicActionButton` early-branch widening + `DynamicActionButtonLegacyPathSourceContractTest.kt` pin.

**Current signature to append to** (SegmentedOptionSelector.kt:45-53): append after the already comma-terminated `disabledReason: String? = null,`:
```kotlin
    disabledReason: String? = null,
    disabledOptions: Set<Int> = emptySet(),
    style: SegmentedOptionStyle = SegmentedOptionStyle.Material,
) {
```
(Final param names/order come from `.planning/seams/v2.6.0-SKETCH.md` §2 Y-11; verify there before coding.)

**Insertion point:** after the `require` block (lines 56-58), before `Column(` at line 59:
```kotlin
if (style == SegmentedOptionStyle.Pill) { PillSegmentedOptionSelector(...); return }
```

**The one Material-path edit** (line 68): `enabled = enabled,` -> `enabled = enabled && index !in disabledOptions,` (lane 2; commit with `HUB_LANE_OVERRIDE=2`) or the lane-1 shadow fallback (`val enabled = enabled && index !in disabledOptions` inserted before `SegmentedButton(`). Everything else in lines 59-139 (maxLines = 2 at 80, `reserveSegmentIconSlot()` at 83, `height(IntrinsicSize.Min)` at 62) stays byte-unchanged.

**Per-segment semantics to reproduce in Pill** (lines 73-75), keep the identical string:
```kotlin
modifier = Modifier.fillMaxHeight().semantics {
    contentDescription = "$label, ${if (selected) "selected" else "not selected"}"
},
```

**Disabled caption to duplicate into the Pill branch** (lines 88-97):
```kotlin
if (!enabled && disabledReason != null) {
    Text(
        text = disabledReason,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}
```
**Pill container idiom:** `Modifier.clip(CircleShape).border(Dimens.HairlineBorder, MaterialTheme.colorScheme.outlineVariant, CircleShape)` (PillChip.kt:93-94); selectable segments use `Modifier.selectableGroup()` + `selectable(role = Role.RadioButton)`; 38% via `DISABLED_CONTENT_ALPHA`. New enum `SegmentedOptionStyle { Material, Pill }` appended at file end (private branch fun in the same file). Append KDoc lines only (do not reword existing ones); add the KDoc warning about long localized labels truncating in Pill.

---

### `MAIN/component/SheetScaffold.kt` (edit)

Pure additions. New import `androidx.compose.ui.graphics.Shape`. Insert before the trailing lambda (lines 55-56):
```kotlin
    contentWindowInsets: @Composable () -> WindowInsets = { BottomSheetDefaults.windowInsets },
    shape: Shape = BottomSheetDefaults.ExpandedShape,
    content: @Composable ColumnScope.() -> Unit
```
Forward by name (lines 58-64): `contentWindowInsets = contentWindowInsets,` (gains comma) then `shape = shape`. KDoc: add one `@param shape` line after the `contentWindowInsets` param (line 44). Lane 1.

---

### `MAIN/theme/Dimens.kt` (append)

**Analog:** `object PillChip` (Dimens.kt:223-231): nested object, one KDoc line per `val X: Dp = N.dp`. Append `object ReceiptRow` (e.g. VerticalPadding 12, ColumnGap 8, ValueMinWidth 56, CueHeight 24, CueGap 4, ChevronSize 24, MotionMs 200 if kept here) and `object SegmentedPill` (Height 40, LabelHorizontalPadding). Extend `DimensTest` append-only (it regression-locks existing values).

---

### `MAIN/explorer/CardsFamilyScreen.kt` (append `ReceiptRow` entry)

**Analog:** ToneCard entry (lines 287-301). Closing `)` of that entry becomes `),` (additive guard tolerates trailing comma).
```kotlin
ComponentRegistry.Entry(
    name = "ToneCard",
    family = ExplorerFamilies.CARDS,
    states = listOf(
        ComponentRegistry.StateCell("Default") { ToneCardDefaultPreview() },
        // Pressed / Selected: ... N/A.
        ComponentRegistry.StateCell("Pressed / Selected"),
        ComponentRegistry.StateCell("Disabled"),
        ComponentRegistry.StateCell("Focused")
    ),
    content = { ToneCardVariants() },
    tier = ComponentRegistry.Tier.PRIMITIVE
)
```
State labels must be exactly `Default`, `Pressed / Selected`, `Disabled`, `Focused` in order (ComponentStatesMatrixTest). `ReceiptRow` is stateless; its Variants hold single-open accordion state via `remember { mutableStateOf<Int?>(null) }` (D-09). Private helper pattern: `private fun ToneCardVariants()` using `SectionLabel("...")` + `ToneCardSample(...)` (lines 504-522). Tier: PRIMITIVE (ToneCard precedent) or PATTERN (TagListItem precedent, line 285); planner decides.

### `MAIN/explorer/PickersFamilyScreen.kt` and `SheetsFamilyScreen.kt` (append)
Add Pill / disabled-option demos inside the existing private `SegmentedOptionSelectorVariants()` (~line 449-460), and a shaped-sheet `ShowSheetButton` inside `SheetScaffoldSection()` (~794-806, uses `var show by remember { mutableStateOf(false) }`). Append lines only. No new registry entries (both composables already registered).

---

### Tests

**`ReceiptRowSourceContractTest.kt` / `SegmentedOptionSelectorLegacyPathSourceContractTest.kt`** — copy `DynamicActionButtonLegacyPathSourceContractTest.kt` (lines 15-45). Plain JUnit4:
```kotlin
private val stripped: String = SourceContractTestSupport.stripComments(
    SourceContractTestSupport.source("SegmentedOptionSelector.kt"))
private fun body(): String =
    SourceContractTestSupport.functionBody(stripped, "fun SegmentedOptionSelector(").filterNot { it.isWhitespace() }
// guard index < return index < legacy block index; legacy block text pinned verbatim; countOccurrences == 1
```
Note `SourceContractTestSupport.source()` is hard-wired to `component/`. For the sheet shape: assert `shape:Shape=BottomSheetDefaults.ExpandedShape,content:` order and `shape=shape` forwarded.

**Geometry / long-label tests (ReceiptRow, Pill)** — copy the class header and harness from `SegmentedOptionSelectorTest.kt:39-68`:
```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ... {
    @get:Rule val composeTestRule = createComposeRule()
    // setContent { CompositionLocalProvider(LocalDensity provides Density(...)) { YahirAndroidTasteTheme { Box(host) { ... } } } }
    // composeTestRule.waitForIdle()
```
Use `onNodeWithTag(...).getUnclippedBoundsInRoot()` (ToneCardTest.kt imports line 13) with `assertEquals(msg, 92f, (b.bottom - b.top).value, 1f)`; ellipsis via the `GetTextLayoutResult` action + `isLineEllipsized(0)` (SegmentedOptionSelectorTest `textLayout()` helper). One `setContent` per test. No `captureToImage`.

**Simple (non-NATIVE) tests** — `ToneCardTest.kt:35-40` header (`@Config(sdk = [35])`, `createComposeRule()`), `assertDpEquals` helper (42-44), tag-exists tracer (69-82).

**Registry render test** — append to `ToneAtomsRegistryRenderTest.kt` using its `entry()` / `renderEverything()` / `canonicalLabels` helpers (lines 32-50) and the ToneCard test as template (lines 52-66): assert family, tier, `states.map { it.label }`, non-null Default render + content, then count tagged nodes (`receipt_row_summary`).

**`ToneFoundationInvariantsTest`** — add `"component/ReceiptRow.kt"` to `phaseFiles` (lines 36-43) so the import-prefix and no-color-literal scans cover it; ensure `ReceiptRow.kt` has no `Color(0x...)` literal, no `testTagsAsResourceId`.

**`DomainVocabularyDriftGuardTest`** — add one `DOMAIN_VOCABULARY` entry (`"ReceiptRow" to "<rationale>"`) or `"Receipt"` in `PRIMITIVE_NOUN_ALLOWLIST` (guard lines 97, 297); `Segmented` and `Sheet` already pass.

## Shared Patterns

### Tag placement
**Source:** `ToneCard.kt:34`, `PillChip.kt:91`. **Apply to:** `ReceiptRow` and Pill segments. Fixed tag AFTER caller modifier; library never sets `testTagsAsResourceId`.

### Disabled alpha
**Source:** `PillChip.kt:33,92` (`DISABLED_CONTENT_ALPHA = 0.38f`). **Apply to:** Pill disabled segments, `dimmed` row.

### Single-line ellipsis text
**Source:** `PillChip.kt:109-116`. **Apply to:** ReceiptRow title/subtitle/value/cue, Pill labels.

### Hairline outline
**Source:** `PillChip.kt:94` (`border(Dimens.HairlineBorder, outlineVariant, CircleShape)`). **Apply to:** Pill container, row divider.

### Early-branch widening pinned by a source-contract test
**Source:** `DynamicActionButtonLegacyPathSourceContractTest.kt`. **Apply to:** `SegmentedOptionSelector`, `SheetScaffold` shape.

### Vertical slice per component
Each slice lands: code + registry entry + vocabulary-guard edit + `api.txt` (`apiDump`) in the same commit. Batch gradle in one invocation: `./gradlew apiDump testDebugUnitTest --tests A --tests B detekt apiCheck` (only if the executor runs gradle; this mapping did not).

### Lane rules
Pure-add edits are lane 1. The `enabled = enabled,` rewrite needs `HUB_LANE_OVERRIDE=2` (or the shadow fallback). Do not reword existing KDoc lines. Never `git add .planning/` wholesale; no tag.

## No Analog Found

| File/Concern | Role | Data Flow | Reason |
|---|---|---|---|
| `mainClock` motion tests (chevron 200ms, dimmed fade) | test | animation | No test in `src/test` uses `mainClock` yet; follow RESEARCH Pattern 4 (internal seam composables + `autoAdvance = false`). Chevron rotation precedent in `ExpandableFab.kt:116-119`. |
| `LineHeightStyle(Center, Trim.None)` helper | utility | n/a | No `lineHeightStyle` anywhere in `src`; use RESEARCH Pitfall 1 snippet. |
| `AnimatedVisibility(expandVertically + fadeIn)` for `expandedContent` | component | animation | Planner should check for an in-repo `AnimatedVisibility` use (UI-SPEC cites the YAT editor); otherwise use the RESEARCH skeleton. |
| Recording-`Shape` probe for sheet shape | test | n/a | `ModalBottomSheet` shape not in semantics; use source-contract test (RESEARCH Pitfall 9). |

## Metadata

**Analog search scope:** `src/main/.../component`, `theme`, `explorer`; `src/test/.../component`, `explorer`.
**Files read:** SegmentedOptionSelector.kt, SheetScaffold.kt, ToneCard.kt, PillChip.kt, CardsFamilyScreen.kt (287-301), ToneCardTest.kt (1-90), DynamicActionButtonLegacyPathSourceContractTest.kt, ToneAtomsRegistryRenderTest.kt (1-80), SegmentedOptionSelectorTest.kt (1-70), ToneFoundationInvariantsTest.kt (1-60), plus CONTEXT.md and RESEARCH.md lines 1-405 (lines 406-571 not read; they hold validation architecture details).
**Pattern extraction date:** 2026-10-12
