<!-- refreshed: 2026-08-21 -->
# Codebase Structure

**Analysis Date:** 2026-08-21

## Directory Layout

```
yahirandroidtaste/ (repo root = publishable library module)
├── src/
│   ├── main/
│   │   ├── AndroidManifest.xml         # Library manifest; declares ExplorerActivity
│   │   ├── java/io/github/ygaray/
│   │   │   └── yahirandroidtaste/
│   │   │       ├── component/          # 43 public composables (card faces, sheets, chips, etc.)
│   │   │       │   ├── TextCard.kt
│   │   │       │   ├── ListCard.kt
│   │   │       │   ├── AlbumCard.kt
│   │   │       │   ├── VoiceCard.kt
│   │   │       │   ├── CardBase.kt     # Shared shell (private, unregistered sub-part)
│   │   │       │   ├── CardTagRow.kt
│   │   │       │   ├── CardQuickView.kt
│   │   │       │   ├── CountBadge.kt
│   │   │       │   ├── TagListItem.kt
│   │   │       │   ├── AdaptiveMediaPreview.kt
│   │   │       │   ├── AppChip.kt
│   │   │       │   ├── TagChipWithContextMenu.kt
│   │   │       │   ├── ChipBar.kt
│   │   │       │   ├── SortControl.kt
│   │   │       │   ├── FilterBar.kt
│   │   │       │   ├── SheetScaffold.kt
│   │   │       │   ├── NameAndTagsEditor.kt
│   │   │       │   ├── ClearableTextField.kt
│   │   │       │   ├── EditorItemRow.kt
│   │   │       │   ├── CardEditorShellContent.kt
│   │   │       │   ├── TextCardBottomSheet.kt
│   │   │       │   ├── ListCardBottomSheet.kt
│   │   │       │   ├── RecordingBottomSheetContent.kt
│   │   │       │   ├── AlbumSourcePickerSheet.kt
│   │   │       │   ├── AlbumTitleConfirmSheet.kt
│   │   │       │   ├── VoiceRenameTagsSheet.kt
│   │   │       │   ├── TagPickerSheet.kt
│   │   │       │   ├── TagPickerSheetContent.kt
│   │   │       │   ├── TagChipEditorContent.kt
│   │   │       │   ├── TagCreateSheet.kt
│   │   │       │   ├── TagCreateSheetContent.kt
│   │   │       │   ├── BulkCreatePopup.kt
│   │   │       │   ├── BulkCreatePopupContent.kt
│   │   │       │   ├── ExpandableFab.kt
│   │   │       │   ├── CycleSubTypeButton.kt
│   │   │       │   ├── DynamicActionButton.kt
│   │   │       │   ├── AccentColorPicker.kt
│   │   │       │   ├── IconPickerGrid.kt
│   │   │       │   ├── CropOverlay.kt
│   │   │       │   ├── EmptyState.kt
│   │   │       │   ├── ConfirmationDialog.kt
│   │   │       │   ├── WaveformCanvas.kt        # Private; used by VoiceCard (unregistered)
│   │   │       │   ├── WaveformUtil.kt
│   │   │       │   ├── RelatednessEncoding.kt   # Utility for encode/decode visual state
│   │   │       │   ├── ColorUtils.kt            # Utility for color conversions
│   │   │       │   ├── ImageCountIndicator.kt   # Private sub-component
│   │   │       │   └── CardFaceSlots.kt         # Typealias definitions for card slots
│   │   │       │
│   │   │       ├── feedback/            # Undo state machine, feedback dispatcher, undo UI
│   │   │       │   ├── UndoHistoryStore.kt      # @Singleton; core undo state machine
│   │   │       │   ├── UndoHistoryEntry.kt      # Data class: undo entry with status
│   │   │       │   ├── UndoStatus.kt            # Enum: Available/Undone/Failed
│   │   │       │   ├── FeedbackEvent.kt         # Sealed class: snackbar events
│   │   │       │   ├── FeedbackDispatcher.kt    # Interface; consumer provides implementation
│   │   │       │   ├── UndoTracking.kt          # Utilities for undo action tracking
│   │   │       │   ├── UndoPreview.kt           # Data class: preview snapshot payload
│   │   │       │   ├── UndoCenterScreen.kt      # Composable: full undo history UI
│   │   │       │   └── UndoPreview.kt           # Preview snapshot model
│   │   │       │
│   │   │       ├── modifier/            # Shared interaction mechanics (swipe, drag)
│   │   │       │   └── SwipeableActionRow.kt    # AnchoredDraggable swipe-to-reveal (left delete, right edit)
│   │   │       │
│   │   │       ├── model/               # Serializable UI data-transfer objects
│   │   │       │   ├── ListItemUiModel.kt       # List-item row model
│   │   │       │   ├── TagChipUiModel.kt        # Tag chip with id, label, icon, color
│   │   │       │   ├── TagManagementUiModel.kt  # Tag with id, label, usage count, color
│   │   │       │   ├── UndoHistoryEntry.kt      # (also in feedback/, imported here)
│   │   │       │   ├── BrowseTagSortMode.kt     # Enum: sort modes for tag browse
│   │   │       │   ├── BrowseSortPreference.kt  # User browse/sort preference
│   │   │       │   └── TagSortMode.kt           # Enum: tag sort strategies
│   │   │       │
│   │   │       ├── theme/               # Material 3 design tokens, color scheme, typography
│   │   │       │   ├── Color.kt         # Light/Dark color tokens (Teal, Slate, Amber, Error, Neutral)
│   │   │       │   ├── Type.kt          # Typography: Headline, Body, Label scale
│   │   │       │   ├── Dimens.kt        # Spacing constants (HorizontalPadding, BottomPadding, etc.)
│   │   │       │   ├── Theme.kt         # YahirAndroidTasteTheme composable; light/dark scheme selection
│   │   │       │   ├── ThemeMode.kt     # Enum: LIGHT, DARK, SYSTEM
│   │   │       │   └── (implicitly unregistered; see ComponentRegistry.INTENTIONALLY_UNREGISTERED)
│   │   │       │
│   │   │       ├── icon/                # Card-type icon utilities
│   │   │       │   └── CardTypeIconUtil.kt      # Resolve icon vector for card type
│   │   │       │
│   │   │       └── explorer/            # Gallery / component showcase (debug-only)
│   │   │           ├── ExplorerActivity.kt      # Standalone Activity; owns nav + theme wrapper
│   │   │           ├── ExplorerEntry.kt         # Internal NavHost root; family navigation
│   │   │           ├── ExplorerIndexScreen.kt   # Landing screen; list of seven families
│   │   │           ├── ComponentRegistry.kt     # SINGLE SOURCE OF TRUTH: registry of all 45 public composables (41 registered + 4 allowlisted)
│   │   │           ├── ComponentDetailScreen.kt # Per-component detail page (States, Variants, Playground)
│   │   │           ├── ComponentSearch.kt       # Name search / filter
│   │   │           │
│   │   │           ├── CardsFamilyScreen.kt     # Family screen: TextCard, ListCard, AlbumCard, VoiceCard, sub-rows
│   │   │           ├── ChipsFamilyScreen.kt     # Family screen: AppChip, TagChipWithContextMenu, ChipBar, SortControl, FilterBar
│   │   │           ├── SheetsFamilyScreen.kt    # Family screen: editors, pickers, bulk create
│   │   │           ├── ButtonsFabFamilyScreen.kt # Family screen: ExpandableFab, CycleSubTypeButton, DynamicActionButton
│   │   │           ├── PickersFamilyScreen.kt   # Family screen: AccentColorPicker, IconPickerGrid, CropOverlay
│   │   │           ├── FeedbackFamilyScreen.kt  # Family screen: ConfirmationDialog, UndoCenterScreen
│   │   │           ├── EmptyStateFamilyScreen.kt # Family screen: EmptyState
│   │   │           │
│   │   │           ├── TokenBrowserScreen.kt    # Theme token showcase (colors, type, dimens)
│   │   │           ├── TokenSwatches.kt         # Color swatch gallery
│   │   │           │
│   │   │           ├── ExplorerFakeData.kt      # Hard-coded preview data for gallery
│   │   │           ├── PlaygroundState.kt       # Live state knobs for per-component Playground (Phase 63)
│   │   │           ├── Control.kt               # UI for playground control (Toggle, Slider, etc.)
│   │   │           └── ExplorerEntry.kt         # Fixture definitions and preview renderables
│   │   │
│   │   └── res/
│   │       └── (AndroidManifest.xml, if separate)
│   │
│   └── test/
│       └── java/io/github/ygaray/yahirandroidtaste/
│           ├── component/                       # Compose UI tests + JVM unit tests
│           │   ├── *Test.kt (20+ test files)
│           │   ├── CountBadgeTest.kt
│           │   ├── AppChipTest.kt
│           │   ├── CardEditorShellContentTest.kt
│           │   ├── VoiceRenameTagsSheetGateTest.kt
│           │   └── ... (one per public composable; co-located test)
│           │
│           ├── feedback/
│           │   ├── UndoHistoryStoreTest.kt      # State machine logic, append, undo, eviction
│           │   └── UndoCenterScreenTest.kt      # UI snapshot tests
│           │
│           ├── modifier/
│           │   └── SwipeThresholdTest.kt        # Pure JVM tests for threshold logic
│           │
│           └── (other packages: no tests yet)
│
├── build.gradle.kts                 # Single-module Gradle build; AGP 9.2.1, Compose BOM 2026.02.01
├── settings.gradle.kts              # rootProject.name = "yahirandroidtaste" (single-module)
├── jitpack.yml                      # JitPack build script (runs ./gradlew publishReleasePublicationToMavenLocal)
│
├── config/                          # Lint/static analysis config
│   └── detekt/
│
├── .planning/                       # GSD planning documents
│   └── codebase/                    # This directory: ARCHITECTURE.md, STRUCTURE.md, CONVENTIONS.md, TESTING.md, CONCERNS.md, STACK.md, INTEGRATIONS.md
│
├── API.md                           # Public surface: catalog of 45 composables, parameters, usage
├── INTEGRATION.md                   # Consumer integration checklist (Hilt, Compose BOM, dependency)
├── CLAUDE.md                        # Codebase rules (one-way dependency, bindings-only Hilt, ComponentRegistry invariant)
├── README.md                        # (if exists)
└── ECOSYSTEM.md                     # (if exists; cross-repo phase rituals)
```

## Directory Purposes

**component/**
- Purpose: Public top-level composables (45 total: 41 registered + 4 sub-parts).
- Contains: Card faces (Text, List, Album, Voice), sheets/editors, chips, buttons, pickers, feedback UI, empty-state.
- Key files: `CardBase.kt` (shared card shell), `TextCard.kt`, `ListCard.kt`, `AlbumCard.kt`, `VoiceCard.kt`.

**feedback/**
- Purpose: Undo state machine (@Singleton), feedback event dispatcher (interface), and undo UI.
- Contains: `UndoHistoryStore` (core logic), `FeedbackDispatcher` (consumer port), `UndoCenterScreen` (UI), models and utilities.
- Key files: `UndoHistoryStore.kt`, `FeedbackDispatcher.kt`, `UndoCenterScreen.kt`.

**modifier/**
- Purpose: Shared interaction primitives (currently swipe-to-reveal only).
- Contains: `SwipeableActionRow` (AnchoredDraggable state machine).
- Key files: `SwipeableActionRow.kt`.

**model/**
- Purpose: Serializable UI data-transfer objects (no Compose, no domain leakage).
- Contains: `ListItemUiModel`, `TagChipUiModel`, `TagManagementUiModel`, undo/sort/browse models.
- Key files: One file per model type.

**theme/**
- Purpose: Material 3 design tokens (colors, typography, spacing) and theme wrapper.
- Contains: Light/dark color schemes, type scale, dimensions, `YahirAndroidTasteTheme` root composable.
- Key files: `Color.kt`, `Type.kt`, `Dimens.kt`, `Theme.kt`.

**icon/**
- Purpose: Card-type icon resolution utility.
- Contains: `CardTypeIconUtil`.
- Key files: `CardTypeIconUtil.kt`.

**explorer/**
- Purpose: Standalone debug-only component gallery (ExplorerActivity + registry + per-family screens).
- Contains: Registry (single source of truth), navigation, seven family screens, detail pages, theme browser, fake data.
- Key files: `ExplorerActivity.kt`, `ComponentRegistry.kt`, `ExplorerEntry.kt`, `*FamilyScreen.kt`.

## Key File Locations

**Entry Points:**
- `explorer/ExplorerActivity.kt`: Standalone gallery Activity; launched via explicit Intent.
- `component/TextCard.kt`, `component/ListCard.kt`, etc.: Public composables called directly by consumers.
- `feedback/UndoHistoryStore.kt`: Injected singleton; called by consumer ViewModel.
- `feedback/FeedbackDispatcher.kt`: CompositionLocal interface; consumer provides implementation.

**Configuration:**
- `build.gradle.kts`: Gradle build configuration (AGP 9.2.1, Compose BOM 2026.02.01, Hilt 2.60.1, Kotlin 2.3.20).
- `settings.gradle.kts`: `rootProject.name = "yahirandroidtaste"` (single-module library).
- `jitpack.yml`: JitPack build instructions (runs `publishReleasePublicationToMavenLocal`).
- `config/detekt/` (if present): Detekt static analysis baseline (zero-baseline policy enforced).

**Core Logic:**
- `modifier/SwipeableActionRow.kt`: Swipe-to-reveal gesture (Resting → RevealedLeft/RevealedRight).
- `component/CardBase.kt`: Shared card shell; delegates to SwipeableActionRow, renders dropdown menu.
- `feedback/UndoHistoryStore.kt`: Undo state machine; append, attemptUndo, clearSpent, 50-cap eviction.
- `theme/Theme.kt`: Material 3 theme wrapper; light/dark color selection.

**Testing:**
- `src/test/java/.../component/*Test.kt`: Compose UI tests + JUnit4 tests (co-located with source).
- `src/test/java/.../feedback/UndoHistoryStoreTest.kt`: State machine unit tests.
- `src/test/java/.../modifier/SwipeThresholdTest.kt`: Pure JVM threshold logic tests.

## Naming Conventions

**Files:**
- Composable files: PascalCase, match function name. Example: `TextCard.kt` contains `@Composable fun TextCard(...)`.
- Utility files: PascalCase. Example: `ColorUtils.kt`, `WaveformUtil.kt`, `CardTypeIconUtil.kt`.
- Model data classes: PascalCase. Example: `ListItemUiModel.kt`, `TagChipUiModel.kt`.
- Test files: Source name + "Test" suffix. Example: `TextCard.kt` → `TextCardTest.kt`, `UndoHistoryStore.kt` → `UndoHistoryStoreTest.kt`.

**Directories:**
- Package names: lowercase, no hyphens. Example: `io.github.ygaray.yahirandroidtaste.component`.
- Nested packages: One per logical feature area. Example: `feedback/` for undo, `modifier/` for swipe, `theme/` for design tokens.
- No abbreviations in directory names (spell out: `component`, not `comp`; `feedback`, not `fb`).

**Composables:**
- Public composables: PascalCase `@Composable` function names matching file name. Example: `TextCard`, `ListCard`, `AppChip`.
- Private composables: same PascalCase, but file is not registered in `ComponentRegistry` (unless accidentally public—lint enforces this).
- Preview composables: suffix with "Preview". Example: `TextCardPreview()`, `AppChipPreview()`.

**Callbacks:**
- Format: `on<Event>`. Example: `onTap`, `onDelete`, `onEditClick`, `onTagClick`, `onSortModeChange`.
- Type: Usually `() -> Unit`, `(value: T) -> Unit`, or `suspend () -> Unit`.

## Where to Add New Code

**New public Composable (Card/Chip/Sheet/etc.):**

1. Create file in appropriate family directory under `component/`: `component/NewComponentName.kt`.
2. Define `@Composable fun NewComponentName(...)` with public parameters (accept data + callbacks, never assume domain).
3. Implement using `@Composable` functions from the same package; import from `theme/`, `modifier/`, `model/` as needed.
4. **Mandatory:** Add entry to `ComponentRegistry` in the corresponding family screen file:
   - Cards → `explorer/CardsFamilyScreen.kt`: add to `cardsFamilyEntries` list.
   - Chips → `explorer/ChipsFamilyScreen.kt`: add to `chipsFamilyEntries` list.
   - Sheets → `explorer/SheetsFamilyScreen.kt`: add to `sheetsFamilyEntries` list.
   - And so on for other families.
   - See API.md and existing entries for `states`, `content`, `controls`, `preview` structure.
5. Create unit/UI test: `src/test/java/.../component/NewComponentNameTest.kt`.
6. Verify `./gradlew build` passes (ComponentRegistry drift guard enforces registration).

**New Theme Token (Color/Type/Dimension):**

1. Add token definition to appropriate file:
   - Color: `theme/Color.kt` (both light and dark variants).
   - Typography: `theme/Type.kt`.
   - Dimension: `theme/Dimens.kt`.
2. Apply in `theme/Theme.kt` MaterialTheme composable.
3. Showcase in `explorer/TokenBrowserScreen.kt` + `TokenSwatches.kt`.
4. No ComponentRegistry entry needed (theme tokens are not composables).

**New State Model (for Undo/Browse/etc.):**

1. Create data class in `model/`: `model/MyNewUiModel.kt`.
2. Make it serializable (no mutable state, no references to Composables or Android contexts).
3. Used by components as parameter types; consumers map domain data into these before calling components.
4. No ComponentRegistry entry needed (models are not composables).

**New Undo / Feedback Feature:**

1. Add event type to `feedback/FeedbackEvent.kt` (sealed class).
2. Update consumer's `FeedbackDispatcher` implementation in `:app` to handle it.
3. If new UI surface is needed (e.g., new snackbar variant), add to `feedback/` (owned by consumer).
4. Update `UndoHistoryStore` if new state transitions or eviction logic is needed.
5. Add test: `src/test/java/.../feedback/FeedbackEventTest.kt` (or extend existing test).

**New Modifier / Interaction Primitive:**

1. Create file in `modifier/`: `modifier/MyNewModifier.kt`.
2. Define as `fun Modifier.myNewModifier(...): Modifier` (extension function on Modifier).
3. Do NOT add to ComponentRegistry (modifiers are infrastructure, not visible tiles).
4. If used by multiple components, place here; if single-component-only, keep private in that component's file.
5. Add test: `src/test/java/.../modifier/MyNewModifierTest.kt` (focus on JVM logic, not Compose preview).

**Tests for Existing Code:**

- **Composable UI tests:** `src/test/java/.../component/MyComponentTest.kt`. Use Compose test framework (screenshot assertions if visual; snapshot testing via roborazzi if available).
- **Undo state machine tests:** `src/test/java/.../feedback/UndoHistoryStoreTest.kt`. JUnit4 + coroutine test dispatcher (`runTest { }`).
- **Modifier logic tests:** `src/test/java/.../modifier/MyModifierTest.kt`. Pure JVM, no Compose; test `thresholdSide()`, drag offset calculations, etc.
- **Model tests:** `src/test/java/.../model/MyModelTest.kt`. Serialization, equality, edge cases.

## Special Directories

**explorer/**
- Purpose: Gallery / debug infrastructure.
- Generated: NO (all hand-authored).
- Committed: YES (part of the library).
- Deployed: NO (only runs inside `:yahirandroidtaste` or via consumer's explicit Intent; not shipped as APK on its own).

**build/** (artifact output, .gitignored)
- Purpose: Gradle build outputs (compiled classes, AAR, test results).
- Generated: YES (by `./gradlew build`).
- Committed: NO.
- Deployed: AAR files used by JitPack.

**.planning/codebase/** (this directory)
- Purpose: GSD codebase analysis documents (ARCHITECTURE.md, STRUCTURE.md, CONVENTIONS.md, TESTING.md, CONCERNS.md, STACK.md, INTEGRATIONS.md).
- Generated: NO (hand-written or generated by GSD mapping agent).
- Committed: YES (part of repo for orchestration reference).
- Deployed: NO (documentation only).

**config/detekt/** (if present)
- Purpose: Detekt static analysis rules and baseline.
- Generated: Baseline may be auto-regenerated (policy: zero-baseline, so new findings fail build).
- Committed: YES (rules checked in; baseline regenerated only by human approval).
- Deployed: NO (dev-only).

---

*Structure analysis: 2026-08-21*
