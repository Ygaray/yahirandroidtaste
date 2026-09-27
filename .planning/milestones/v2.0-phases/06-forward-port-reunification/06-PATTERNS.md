# Phase 6: Forward-port reunification - Pattern Map

**Mapped:** 2026-09-26
**Files analyzed:** 16 (6 restored source files, 5 restored test files, 3 edited registration/allowlist files, 2 build-config files)
**Analogs found:** 16 / 16 (all are byte-faithful restores from tag `v1.13.0` — the "analog" for restored files is their own v1.13.0 self; genuine new-code analogs are needed only for the registration/allowlist edits)

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|--------------------|------|-----------|-----------------|----------------|
| `component/DateTimePicker.kt` | component | request-response (dialog pick → callback) | `v1.13.0:component/DateTimePicker.kt` (restore-verbatim) | exact (self) |
| `component/PresetChip.kt` | component | request-response (tap → callback) | `v1.13.0:component/PresetChip.kt` (restore-verbatim); shape-analog on `main`: `component/AppChip.kt` | exact (self) / role-match (AppChip) |
| `component/PlaceMapPicker.kt` | component | event-driven + streaming (map gesture, tile render) | `v1.13.0:component/PlaceMapPicker.kt` (restore-verbatim) | exact (self) |
| `component/PlaceMapOsmdroidConfig.kt` | utility (internal config) | transform (side-effecting config setup) | `v1.13.0:component/PlaceMapOsmdroidConfig.kt` (restore-verbatim) | exact (self) |
| `component/PlaceMapPickerModel.kt` | utility (pure math) | transform | `v1.13.0:component/PlaceMapPickerModel.kt` (restore-verbatim) | exact (self) |
| `model/SavedPlaceUiModel.kt` | model | CRUD (data holder) | `v1.13.0:model/SavedPlaceUiModel.kt` (restore-verbatim) | exact (self) |
| `src/test/.../component/DateTimePickerTest.kt` | test | request-response | `v1.13.0` same path (restore-verbatim) | exact (self) |
| `src/test/.../component/PlaceMapPickerTest.kt` | test | event-driven | `v1.13.0` same path (restore-verbatim) | exact (self) |
| `src/test/.../component/PlaceMapOsmdroidConfigTest.kt` | test | transform | `v1.13.0` same path (restore-verbatim) | exact (self) |
| `src/test/.../component/PlaceMapPickerModelTest.kt` | test | transform | `v1.13.0` same path (restore-verbatim) | exact (self) |
| `src/test/.../component/PresetChipTest.kt` | test | request-response | `v1.13.0` same path (restore-verbatim) | exact (self) |
| `explorer/PickersFamilyScreen.kt` (edit) | route/registry | CRUD (list registration) | itself, current `main` — pattern from `AccentColorPicker`/`IconPickerGrid` `Entry(...)` blocks (lines 39-64, 65-90) | exact (in-file precedent) |
| `explorer/ChipsFamilyScreen.kt` (edit) | route/registry | CRUD (list registration) | itself, current `main` — pattern from `AppChip`/`ChipBar` `Entry(...)` blocks (lines 56-103, 146-171) | exact (in-file precedent) |
| `src/test/.../explorer/DomainVocabularyDriftGuardTest.kt` (edit) | test / config | transform (allowlist maps) | itself, current `main` — `PRIMITIVE_NOUN_ALLOWLIST` (line 297+), `DOMAIN_VOCABULARY` (line 325+) | exact (in-file precedent) |
| `gradle/libs.versions.toml` (edit) | config | — | `v1.13.0:gradle/libs.versions.toml` osmdroid block (restore-verbatim addition) | exact (self) |
| `build.gradle.kts` (edit) | config | — | `v1.13.0:build.gradle.kts` osmdroid dependency line + comment (restore-verbatim addition) | exact (self) |

## Pattern Assignments

### `component/DateTimePicker.kt`, `component/PresetChip.kt`, `component/PlaceMapPicker.kt`, `component/PlaceMapOsmdroidConfig.kt`, `component/PlaceMapPickerModel.kt`, `model/SavedPlaceUiModel.kt` + their 5 tests

**Analog:** each file's own byte-identical content at git tag `v1.13.0`, at the **same relative path** on `main` (confirmed exception: `PlaceMapPickerModel.kt` stays under `component/`, not `model/` — see Pitfall 1 in RESEARCH.md).

**Restore command pattern** (do NOT whole-file `git checkout` any file that also exists independently-evolved on `main` — none of these 6+5 do, so plain restore is safe for exactly these 11 files):
```bash
git checkout v1.13.0 -- \
  src/main/java/io/github/ygaray/yahirandroidtaste/component/DateTimePicker.kt \
  src/main/java/io/github/ygaray/yahirandroidtaste/component/PresetChip.kt \
  src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapPicker.kt \
  src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapOsmdroidConfig.kt \
  src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapPickerModel.kt \
  src/main/java/io/github/ygaray/yahirandroidtaste/model/SavedPlaceUiModel.kt \
  src/test/java/io/github/ygaray/yahirandroidtaste/component/DateTimePickerTest.kt \
  src/test/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapPickerTest.kt \
  src/test/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapOsmdroidConfigTest.kt \
  src/test/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapPickerModelTest.kt \
  src/test/java/io/github/ygaray/yahirandroidtaste/component/PresetChipTest.kt
```
**No hand-editing of logic** — any deviation from the v1.13.0 byte content is scope creep (RESEARCH.md Anti-Patterns).

**`PresetChip`'s two-overload public API (must port both, verbatim)** — [source: `v1.13.0:component/PresetChip.kt`]:
```kotlin
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PresetChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supportingLabel: String? = null,
    enabled: Boolean = true,
    isSelected: Boolean = false,
    contentDescription: String?   // no default — required on this overload
) { /* ... */ }

@Composable
fun PresetChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supportingLabel: String? = null,
    enabled: Boolean = true,
    isSelected: Boolean = false
) {
    PresetChip(label, onClick, modifier, supportingLabel, enabled, isSelected, contentDescription = null)
}
```

**`configureOsmdroid`'s exact signature (called internally, never by the gallery)** — [source: `v1.13.0:component/PlaceMapOsmdroidConfig.kt`]:
```kotlin
internal fun configureOsmdroid(context: Context, userAgent: String) {
    val appContext = context.applicationContext
    val prefs = appContext.getSharedPreferences("yahirandroidtaste_osmdroid", Context.MODE_PRIVATE)
    Configuration.getInstance().load(appContext, prefs)
    Configuration.getInstance().userAgentValue = resolveOsmdroidUserAgent(userAgent, appContext.packageName)
    val basePath = File(appContext.cacheDir, "osmdroid")
    Configuration.getInstance().osmdroidBasePath = basePath
    Configuration.getInstance().osmdroidTileCache = File(basePath, "tiles")
}
```
Called only from `PlaceMapPicker.kt`'s private `createPlaceMapView(...)` at its first line — the gallery preview calls only the public `PlaceMapPicker(...)` composable, never `configureOsmdroid` directly.

**`ChipBar` same-package call site — verified compile-clean, no edit needed** — `PlaceMapPicker.kt`'s `PlaceMapSavedPlacesSection` calls:
```kotlin
ChipBar(
    items = chipItems,
    key = { it.place to it.occurrence },
    itemContent = { item -> PresetChip(...) },
    testTag = "${testTag}_saved_places_chips"
)
```
against `main`'s current `component/ChipBar.kt` signature `fun <T> ChipBar(items: List<T>, key: (T) -> Any, itemContent: @Composable (T) -> Unit, modifier: Modifier = Modifier, testTag: String = "chip_bar", leadingContent: (@Composable () -> Unit)? = null, trailingContent: (@Composable () -> Unit)? = null, expandable: ExpandableConfig? = null, rawContent: (@Composable FlowRowScope.() -> Unit)? = null)` — all 4 named args used still exist unchanged; `main` only added trailing optional params. No source edit required at this call site.

---

### `explorer/PickersFamilyScreen.kt` (edit — insert 2 new `Entry(...)` blocks)

**Analog (in-file precedent, current `main`, lines 39-64 and 65-90):**
```kotlin
ComponentRegistry.Entry(
    name = "AccentColorPicker",
    family = ExplorerFamilies.PICKERS,
    states = listOf(
        ComponentRegistry.StateCell(
            "Default",
            render = {
                var selectedColor by remember { mutableStateOf(0xFF6750A4L) }
                AccentColorPicker(
                    selectedColor = selectedColor,
                    onColorSelected = { selectedColor = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                )
            }
        ),
        ComponentRegistry.StateCell("Pressed / Selected"),
        ComponentRegistry.StateCell("Disabled"),
        ComponentRegistry.StateCell("Focused")
    ),
    content = { AccentColorPickerVariants() },
    tier = ComponentRegistry.Tier.PATTERN
),
```
**Action:** insert two analogous `Entry(...)` blocks (`DateTimePicker`, `PlaceMapPicker`) into the existing `pickersFamilyEntries` list (do NOT `git checkout` this whole file — it has independently diverged from v1.13.0; see Pitfall 2 below). Each new entry needs `tier = ComponentRegistry.Tier.PATTERN` (CONTEXT.md D-01) — this field did not exist on the v1.x line and has no default on `main`'s `Entry` data class. `states`/`content` shapes restore near-verbatim from `v1.13.0`'s own `PickersFamilyScreen.kt` demo bodies (RESEARCH.md Pattern 1, Pattern 2). Add the corresponding private `*Variants()` composable functions below the existing `AccentColorPickerVariants()`/`IconPickerGridVariants()` (lines 177-204), matching their shape (`SectionLabel(...)` caption + the real composable call at `Modifier.fillMaxWidth().padding(horizontal = 16.dp)`).

**Registration screen boilerplate pattern** (top-bar / navigation-row wiring, lines 152-175, unchanged, no edit needed — new entries auto-appear via the existing `ComponentRegistry.entries.filter { it.family == ExplorerFamilies.PICKERS }` + `LazyColumn`/`items` loop):
```kotlin
LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
    items(
        ComponentRegistry.entries.filter { it.family == ExplorerFamilies.PICKERS },
        key = { it.name }
    ) { entry ->
        ComponentRow(name = entry.name, tier = entry.tier, onClick = { onNavigateToDetail(entry.name) })
    }
}
```

---

### `explorer/ChipsFamilyScreen.kt` (edit — insert 1 new `Entry(...)` block for `PresetChip`)

**Analog (in-file precedent, current `main`, lines 56-103 `AppChip` and 146-171 `ChipBar`):**
```kotlin
ComponentRegistry.Entry(
    name = "AppChip",
    family = ExplorerFamilies.CHIPS,
    states = listOf(
        ComponentRegistry.StateCell(
            "Default",
            render = {
                AppChip(
                    label = ExplorerFakeData.tagChips[1].name,
                    isSelected = false,
                    onClick = {},
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        ),
        ComponentRegistry.StateCell(
            "Pressed / Selected",
            render = {
                AppChip(
                    label = ExplorerFakeData.tagChips.first().name,
                    isSelected = true,
                    onClick = {},
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        ),
        ComponentRegistry.StateCell("Disabled"),
        ComponentRegistry.StateCell("Focused")
    ),
    content = { AppChipVariants() },
    tier = ComponentRegistry.Tier.PATTERN
),
```
**Action:** insert one new `Entry("PresetChip", ...)` block into `chipsFamilyEntries` (lines 55-212), with `tier = ComponentRegistry.Tier.PATTERN`. Since `PresetChip` is consumed as `ChipBar`'s `itemContent`, model its demo the way `ChipBarVariants()` (lines 302-368) demonstrates `AppChip` as `itemContent` — i.e. wrap `PresetChip` calls inside a `ChipBar(...)` in the new `PresetChipVariants()` function, per UI-SPEC's two demo captions ("PresetChip - ChipBar itemContent, label only", "PresetChip - with supporting labels"). **Do NOT whole-file `git checkout`** — this file has also independently diverged (Pitfall 2).

---

### `src/test/.../explorer/DomainVocabularyDriftGuardTest.kt` (edit — 2 allowlist additions)

**Analog (in-file precedent, current `main`):**
```kotlin
// PRIMITIVE_NOUN_ALLOWLIST (line 297+) — Set<String> keyed on the bare HEAD TOKEN:
val PRIMITIVE_NOUN_ALLOWLIST: Set<String> = setOf(
    "Control", "Sheet", "Field", "Canvas", "State", "Dialog", "Bar", "Card", "Value",
    "Item", "Content", "Row", "Scaffold", "Swatch", "Grid", "View", "Chip", "Popup",
    "Picker", "Button", "Fab", "Badge", "Selector", "Ring", "Menu", "Overlay", "Preview",
    "Cue", "Editor", "Base", "Screen", "Theme", "Ladder", "Showcase",
    // ADD: "Date", "Preset"
)

// DOMAIN_VOCABULARY (line 325+) — Map<String, String> keyed on the FULL composable name -> rationale:
val DOMAIN_VOCABULARY: Map<String, String> = mapOf(
    "VoiceCard" to
        "Head token 'Voice' is a consumer-domain noun (voice recordings/clips) — ...",
    // ADD:
    "PlaceMapPicker" to
        "Head token 'Place' leans location-domain; acknowledged explicitly per Phase 6 D-05.2 " +
            "rather than treated as a domain-agnostic primitive.",
)
```
**Action:** add `"Date"`, `"Preset"` to `PRIMITIVE_NOUN_ALLOWLIST`; add `"PlaceMapPicker" to "<rationale>"` to `DOMAIN_VOCABULARY`. Key-shape difference is critical: `PRIMITIVE_NOUN_ALLOWLIST` is bare head-token, `DOMAIN_VOCABULARY` is full composable name — do not conflate the two shapes.

---

### `gradle/libs.versions.toml` + `build.gradle.kts` (edit — osmdroid dependency admission)

**Analog:** `v1.13.0`'s own osmdroid block (restore-verbatim additions, not a rewrite):
```kotlin
// gradle/libs.versions.toml — [versions]
osmdroid = "6.1.20"
// [libraries]
osmdroid-android = { group = "org.osmdroid", name = "osmdroid-android", version.ref = "osmdroid" }
```
```kotlin
// build.gradle.kts — dependencies block
// HUBW-02 map surface for PlaceMapPicker (D-01): an implementation dependency because
// no osmdroid type appears in a public signature. Archived upstream, human-approved in
// .planning/APPROVED-DEPS.md.
implementation(libs.osmdroid.android)
```
Also add `osmdroid` to `CLAUDE.md`'s allowed-deps line (currently: "Android SDK, AndroidX/Compose, Hilt, Coil, navigation-compose, reorderable").

## Shared Patterns

### Mandatory `tier` field on every restored `Entry(...)` (CONTEXT.md D-01, RESEARCH.md Pitfall 2)
**Source:** `explorer/ComponentRegistry.kt` — `Entry` data class has no default for `tier`.
**Apply to:** all 3 new `Entry(...)` blocks (`DateTimePicker`, `PlaceMapPicker` in `PickersFamilyScreen.kt`; `PresetChip` in `ChipsFamilyScreen.kt`).
```kotlin
data class Entry(
    val name: String,
    val family: String,
    val states: List<StateCell> = emptyList(),
    val content: (@Composable () -> Unit)? = null,
    val controls: List<Control> = emptyList(),
    val preview: (@Composable (PlaygroundState) -> Unit)? = null,
    val tier: Tier   // no default — must be supplied explicitly
)
```
All 3 new entries: `tier = ComponentRegistry.Tier.PATTERN`.

### Never whole-file `git checkout` a file that has independently diverged
**Applies to:** `PickersFamilyScreen.kt`, `ChipsFamilyScreen.kt` — both have `main`-side entries (`CropOverlay`, `SegmentedOptionSelector`, `TagChipWithContextMenu`, `SortControl`) that don't exist at `v1.13.0`, and v1.13.0-side entries that would clobber current tier/states/content enrichment. Restore only the 6 component/model source files and 5 test files via targeted `git checkout v1.13.0 -- <path>`; hand-insert the 3 new registry `Entry(...)` blocks into the current `main` version of the family-screen files.

### Governance-gate verification loop
**Applies to:** all restored/edited files.
```bash
./gradlew testDebugUnitTest --tests "*DateTimePickerTest*" --tests "*PlaceMapPickerTest*" \
  --tests "*PlaceMapOsmdroidConfigTest*" --tests "*PlaceMapPickerModelTest*" --tests "*PresetChipTest*"
./gradlew testDebugUnitTest --tests "*ComponentRegistryDriftGuardTest*" --tests "*DomainVocabularyDriftGuardTest*"
./gradlew detekt
./gradlew apiDump && ./gradlew apiCheck
```

## No Analog Found

None. Every file in scope is either a byte-identical self-restore from `v1.13.0`, or an edit whose in-file precedent (existing `Entry(...)` blocks, existing allowlist maps) is a direct, sufficient pattern.

## Metadata

**Analog search scope:** `src/main/java/io/github/ygaray/yahirandroidtaste/{component,model,explorer}/`, `src/test/java/io/github/ygaray/yahirandroidtaste/{component,explorer}/`, git tag `v1.13.0` (full tree), `gradle/libs.versions.toml`, `build.gradle.kts`
**Files scanned:** `ComponentRegistry.kt`, `PickersFamilyScreen.kt`, `ChipsFamilyScreen.kt`, `ChipBar.kt`, `DomainVocabularyDriftGuardTest.kt`, `ComponentRegistryDriftGuardTest.kt`, plus `v1.13.0` tag inspection (already performed by gsd-phase-researcher this session; excerpts re-verified here as tracked source — none are gitignored mirrors, all live under `src/main` or `src/test` in this repo, confirmed via `git ls-files`)
**Pattern extraction date:** 2026-09-26
