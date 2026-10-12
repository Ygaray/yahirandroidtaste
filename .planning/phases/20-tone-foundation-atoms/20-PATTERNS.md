# Phase 20: Tone foundation & atoms - Pattern Map

**Mapped:** 2026-10-12
**Files analyzed:** 17 (6 main-source, 4 registry edits, 1 test-allowlist edit, 6 new tests)
**Analogs found:** 17 / 17 (all analogs verified git-tracked under `src/`)

All paths below are relative to `src/main/java/io/github/ygaray/yahirandroidtaste/` (main) or `src/test/java/io/github/ygaray/yahirandroidtaste/` (test).

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|---|---|---|---|---|
| `theme/ToneTokens.kt` (ToneColors, ToneDefaults, ContentTone) | config/token | transform | `component/DynamicActionButton.kt` `ActionButtonDefaults` (lines 64-85) + `theme/Dimens.kt` | role-match |
| `component/DynamicActionButton.kt` (modify: emphasis/icon, `ActionButtonEmphasis`) | component | request-response | itself (lines 36-58) | exact |
| `component/RoundIconButton.kt` | component | request-response | `component/PresetChip.kt` (Box + minimumInteractiveComponentSize + clickable) | role-match |
| `component/StackedProgressBar.kt` (+ `BarSegment`, `clampSegmentFractions`) | component | transform | `component/MetricBar.kt` | role-match |
| `component/ToneCard.kt` | component | request-response | `component/ModelSelectCard.kt` (Surface + Column + testTag) | role-match |
| `component/PillChip.kt` | component | request-response | `component/PresetChip.kt` | exact |
| `explorer/ButtonsFabFamilyScreen.kt` (append entry) | registry | config | `explorer/ChipsFamilyScreen.kt` PresetChip entry | exact |
| `explorer/ProgressFamilyScreen.kt` (append entry) | registry | config | same file, MetricBar entry (lines 36-63) | exact |
| `explorer/CardsFamilyScreen.kt` (append entry) | registry | config | `explorer/ChipsFamilyScreen.kt` entry shape | exact |
| `explorer/ChipsFamilyScreen.kt` (append entry + Variants) | registry | config | same file, PresetChip entry (lines 173-199) | exact |
| `test/.../explorer/DomainVocabularyDriftGuardTest.kt` (append tokens) | test | config | same file lines 297-303 | exact |
| `test/.../component/PillChipTest.kt`, `RoundIconButtonTest.kt`, `ToneCardTest.kt`, `StackedProgressBarTest.kt`, `DynamicActionButtonEmphasisTest.kt` | test | request-response | `component/PresetChipTest.kt` | exact |
| `test/.../component/DynamicActionButtonLegacyPathSourceContractTest.kt` | test | file-I/O | `component/SourceContractTestSupport.kt` users (`FailureRoleSourceContractTest.kt`) | exact |
| `test/.../component/ToneAtomsLongLabelTest.kt` | test | request-response | `ApproachLadderCardLongLabelTest.kt` | exact |
| `test/.../theme/ToneTokensTest.kt` | test | transform | `theme/DimensTest` | role-match |

## Pattern Assignments

### `component/DynamicActionButton.kt` (modify; component, request-response)

**Analog:** itself. Keep legacy lines 44-57 byte-identical; insert an early-return branch above `when (role)`; only `modifier: Modifier = Modifier` (line 42) gains a trailing comma.

**Current signature + legacy body** (lines 36-58):
```kotlin
@Composable
fun DynamicActionButton(
    label: String,
    role: ActionButtonDefaults.ActionButtonRole,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    when (role) {
        ActionButtonDefaults.ActionButtonRole.Save -> Button(
            onClick = onClick,
            enabled = enabled,
            colors = ActionButtonDefaults.colors(role),
            modifier = modifier
        ) { Text(label) }
        else -> TextButton(
            onClick = onClick,
            enabled = enabled,
            colors = ActionButtonDefaults.colors(role),
            modifier = modifier
        ) { Text(label) }
    }
}
```

**Hue source** (lines 75-84; reuse `MaterialTheme.colorScheme.error` vs `primary` for Destructive vs other, per D-07):
```kotlin
@Composable
fun colors(role: ActionButtonRole) = when (role) {
    ActionButtonRole.Destructive -> ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
    ActionButtonRole.Save -> ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
    ActionButtonRole.Neutral -> ButtonDefaults.textButtonColors()
}
```

New: `enum class ActionButtonEmphasis { Filled, Tonal, Text, TextSmall }` top-level public in this file (next to `ActionButtonDefaults`); private/internal `EmphasisActionButton` plus an `internal fun` palette function (unit-testable with `LightColorScheme`/`DarkColorScheme`). Pass `elevation = null` for Tonal. Existing imports lines 3-9 already cover Button/ButtonDefaults/MaterialTheme/Text/TextButton; add `ImageVector`, `Icon`, `Row`/spacing as needed. Decide (and lock in plan) `emphasis == null && icon != null` -> legacy mapping (Save -> Filled, else Text) plus icon.

---

### `component/PillChip.kt` (component, request-response)

**Analog:** `component/PresetChip.kt`

**Imports** (lines 3-25): `BorderStroke`, `clickable`, `Box`, `padding`, `Surface`, `Text`, `minimumInteractiveComponentSize`, `Alignment`, `alpha`, `Role`, `contentDescription`, `semantics`, `dp`, `io.github.ygaray.yahirandroidtaste.theme.Dimens`.

**Core pattern** (lines 90-112; 48dp touch target Box outside, 32dp drawn chip, disabled alpha 0.38f, 1dp border):
```kotlin
val contentAlpha = if (enabled) 1f else 0.38f
Box(
    modifier = modifier.minimumInteractiveComponentSize(),
    contentAlignment = Alignment.Center
) {
    Surface(
        modifier = Modifier
            .heightIn(min = 32.dp)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { if (contentDescription != null) this.contentDescription = contentDescription }
            .semantics(mergeDescendants = true) { selected = isSelected },
        shape = shape, color = containerColor, border = borderStroke
    ) { /* content with .alpha(contentAlpha) */ }
}
```
Adapt: spec wants `CircleShape`, border `outlineVariant`, `bodySmall` single line (`maxLines = 1`, ellipsis), 18dp icons (`Dimens.Icons.ChipIcon`), fixed 32dp. Research recommends ONE root that is clickable + tagged + described (do not copy PresetChip's split where the Box carries `modifier` and the inner Surface carries the click); apply `modifier.testTag("pill_chip")` (caller modifier first, library tag after) on that clickable node. Copy the `import ...theme.Dimens` convention; no hard-coded colors.

---

### `component/RoundIconButton.kt` (component, request-response)

**Analog:** `component/PresetChip.kt` lines 92-100 (`Box(modifier.minimumInteractiveComponentSize())` outer 48dp target with inner clickable) plus `ModelSelectCard.kt` line 65 for the tag idiom `modifier = modifier.testTag("...")`.
Build by hand (D-08): 40dp (or `size`) `Box` with `clip(CircleShape)`, `border(Dimens.HairlineBorder, outlineVariant, CircleShape)`, `clickable(enabled, role = Role.Button, onClick)`, `Icon` 20dp tinted `onSurface`, `contentDescription` required. Disabled: 0.38f alpha. Do NOT use `OutlinedIconButton`.

---

### `component/StackedProgressBar.kt` (component, transform)

**Analog:** `component/MetricBar.kt`

**Imports/idiom** (lines 3-17): `Row`, `fillMaxWidth`, `height`, `Modifier`, `Color`, `dp`; trailing-comma style and KDoc `@param` per parameter. MetricBar's KDoc on `trackColor` (lines 42-48) is the precedent for documenting track-color choice; here `trackColor` is required (D-09).
**Core:** pure `internal fun clampSegmentFractions(raw: List<Float>): List<Float>` (NaN -> 0, negative -> 0, cumulative clamp to 1); `Box(modifier.testTag("stacked_progress_bar").fillMaxWidth().height(6.dp token).clip(CircleShape).background(trackColor))` with `Row(Modifier.matchParentSize())` of `weight(f)` segment Boxes, skipping `f <= 0f` (weight(0f) throws). `data class BarSegment(fraction, color)` or class per sketch Y-9; place in the same file (mirrors `enum class MetricBand` at MetricBar.kt line 113).
Guard style: `require(...)` with message as at MetricBar lines 64-66 is the repo idiom, but here prefer clamping (V5).

---

### `component/ToneCard.kt` (component, request-response)

**Analog:** `component/ModelSelectCard.kt`

**Core pattern** (lines 62-70):
```kotlin
Surface(
    shape = MaterialTheme.expressive.cardShapeLarge,
    color = MaterialTheme.colorScheme.surfaceContainer,
    modifier = modifier.testTag("model_select_card_surface")
) {
    Column(modifier = Modifier.padding(Dimens.HorizontalPadding)) {
```
Adapt: shape `RoundedCornerShape(Dimens.CornerRadius.Medium)` (12dp), `color = tone.container`, `contentColor = tone.content` (Surface sets `LocalContentColor`), `modifier.testTag("tone_card")`, `Column(Modifier.padding(horizontal = Dimens.HorizontalPadding, vertical = Dimens.CompactPadding), verticalArrangement = Arrangement.spacedBy(Dimens.ContentSpacing))`. Content lambda `@Composable ColumnScope.() -> Unit` last.

---

### `theme/ToneTokens.kt` (token, transform)

**Analog:** `component/DynamicActionButton.kt` lines 64-85 (`*Defaults` object with `@Composable` fns reading `MaterialTheme.colorScheme`, enum-not-boolean); KDoc style from `theme/Dimens.kt` header.
Use the Code Examples skeleton in 20-RESEARCH.md (`@Immutable class ToneColors` with hand-written equals/hashCode/toString, never `data class`; `object ToneDefaults { @Composable fun error(); @Composable fun warn() }`; `enum class ContentTone { Default, Muted, Warn, Error }`). Package `io.github.ygaray.yahirandroidtaste.theme`. If adding Dimens tokens, append only (DimensTest locks existing values) and extend `DimensTest`.

---

### Registry entries (4 family screens) (registry, config)

**Analog:** `explorer/ChipsFamilyScreen.kt` lines 173-199 (PresetChip entry) and `explorer/ProgressFamilyScreen.kt` lines 36-63 (display-only N/A cells).

```kotlin
ComponentRegistry.Entry(
    name = "PresetChip",
    family = ExplorerFamilies.CHIPS,
    states = listOf(
        ComponentRegistry.StateCell("Default", render = { PresetChip(label = "30 min", onClick = {}) }),
        ComponentRegistry.StateCell("Pressed / Selected", render = { ... isSelected = true ... }),
        ComponentRegistry.StateCell("Disabled", render = { ... enabled = false ... }),
        // PresetChip has no focus-visual override — N/A.
        ComponentRegistry.StateCell("Focused")
    ),
    content = { PresetChipVariants() },
    tier = ComponentRegistry.Tier.PATTERN
),
```
Rules: exactly the four labels in order; non-null `content`; `tier` required (research A9: PRIMITIVE for all four); N/A cells have no render. Variants composables are `private fun XVariants()` using `SectionLabel("...")` and `Modifier.padding(horizontal = 16.dp)` (ChipsFamilyScreen lines 405-421). Append at the end of each list: the preceding `)` becomes `),` (tolerated). Families: `ExplorerFamilies.BUTTONS_FAB`, `PROGRESS_METRICS`, `CARDS`, `CHIPS`. ButtonsFab entry for `RoundIconButton`; also consider showing new `DynamicActionButton` emphases in its existing Variants (do not edit its existing states).

---

### `test/.../explorer/DomainVocabularyDriftGuardTest.kt` (modify)

**Analog:** same file, `PRIMITIVE_NOUN_ALLOWLIST` (line 297 onward). Append `"Round"`, `"Stacked"`, `"Tone"`, `"Pill"` with a phase-20 comment in the set, otherwise the build fails (head token = first PascalCase word of each new composable).

---

### New component tests (test, request-response)

**Analog:** `component/PresetChipTest.kt` (lines 29-50): `@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [35])`, `@get:Rule val composeTestRule = createComposeRule()`, backtick-named tests, `onNodeWithTag`, `performClick`, `assertIsNotEnabled`, `getUnclippedBoundsInRoot`.
**Touch-bounds idiom** (`ApproachLadderCardTest.kt` ~lines 393-400):
```kotlin
val b = node.fetchSemanticsNode().touchBoundsInRoot
val minHeightPx = with(density!!) { 48.dp.toPx() }
assertTrue("touch height ${b.height} must be >= $minHeightPx", b.height >= minHeightPx)
```
Theme: use `YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT/DARK, dynamicColor = false)` or `MaterialTheme(colorScheme = LightColorScheme/DarkColorScheme)`.
**Source contract** (`SourceContractTestSupport.kt`): `source("DynamicActionButton.kt")`, `stripComments`, `functionBody`, `countOccurrences`; `source()` is hard-wired to `component/`. Pin: guard `if (emphasis != null || icon != null)` appears before the legacy `when (role)`, and legacy text unchanged.
**Long label:** class-level `@Config(sdk = [35], qualifiers = "w360dp-h800dp")` + `@GraphicsMode(GraphicsMode.Mode.NATIVE)`, copy from `ApproachLadderCardLongLabelTest`; unclipped geometry via `positionInRoot` + `size`; use a German label such as "Änderungen speichern und fortfahren".
Existing `DynamicActionButtonTest`, `AppChipTest`, `PresetChipTest` must pass unedited.

## Shared Patterns

### Tags (D-10)
**Source:** `component/ModelSelectCard.kt` line 65. **Apply to:** all four atoms. Caller `modifier` first, library tag appended on the SAME layout node as the caller modifier: `modifier = modifier.testTag("<tag>")`. Pin the override with a test. Never set `testTagsAsResourceId`.

### Theme roles only, no literals
**Source:** `DynamicActionButton.kt` KDoc lines 21-24, `PresetChip.kt` lines 44-45. All colors via `MaterialTheme.colorScheme.*`; dimensions via `theme/Dimens` (`TouchTarget` 48dp, `CornerRadius.Medium` 12dp, `HorizontalPadding` 16dp, `CompactPadding` 12dp, `ContentSpacing` 4dp, `HairlineBorder` 1dp, `Icons.ChipIcon` 18dp, `Icons.MenuIcon` 20dp). For values with no concept match (40dp round, 32dp pill, 6dp bar, button paddings) append named tokens to `Dimens` (append-only) rather than reusing same-valued tokens of another concept (see `ChipToTitleGap` KDoc, Dimens lines 41-48).

### Disabled treatment
**Source:** `PresetChip.kt` line 90 `val contentAlpha = if (enabled) 1f else 0.38f` with `.alpha(contentAlpha)` (line 110). **Apply to:** RoundIconButton, PillChip.

### Declaration formatting for drift guards
`@Composable` alone on its line at column 0, then column-0 `fun Name(` on its own line (PresetChip.kt lines 68-70, MetricBar.kt lines 50-51). One-line forms escape both registry guards silently.

### API gate / lane
Run `./gradlew apiDump` and commit `api.txt` with each public change; only the `DynamicActionButton` signature line (api.txt:158) is a rewrite; `HUB_LANE_OVERRIDE=2` if the classifier says lane 2. One gradle invocation at a time.

## No Analog Found

| File | Role | Data Flow | Reason |
|---|---|---|---|
| Pixel-color assertions (`captureToImage`) | test | transform | No `captureToImage`/`toPixelMap` anywhere in `src/test`; probe first, fall back to structural proof (`LocalContentColor`, pure palette fns, contrast math) |
| `ToneColors` value-class `equals/hashCode` | token | transform | No existing hand-written `@Immutable class` token; use RESEARCH Code Examples skeleton |

## Metadata

**Analog search scope:** `src/main/.../{component,theme,explorer}`, `src/test/.../{component,explorer}`
**Files scanned:** ~12 read in full or by range (DynamicActionButton, PresetChip, MetricBar, ModelSelectCard, Dimens, Chips/Progress family screens, DomainVocabularyDriftGuardTest, SourceContractTestSupport, PresetChipTest, ApproachLadderCardTest range)
**Tracked-source check:** `git ls-files` confirmed for DynamicActionButton, PresetChip, MetricBar, ModelSelectCard (all under `src/main`, no mirror paths)
**Pattern extraction date:** 2026-10-12
