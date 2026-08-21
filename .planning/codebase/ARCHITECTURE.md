<!-- refreshed: 2026-08-21 -->
# Architecture

**Analysis Date:** 2026-08-21

## System Overview

```text
┌─────────────────────────────────────────────────────────────────────────┐
│              Consumer Application (Jetpack Compose UI)                  │
│  Calls: TextCard, ListCard, AlbumCard, VoiceCard, Chips, Sheets, etc.  │
│  @HiltAndroidApp Application + @AndroidEntryPoint Activities           │
│  Receives LocalFeedbackDispatcher, injects UndoHistoryStore            │
└────────────────────┬────────────────────────────────────────────────────┘
                     │
        ┌────────────▼────────────────────────┐
        │   Component Composables (Public)    │
        │  `io.github.ygaray.yahirandroidtaste│
        │  /component/`, `feedback/`, etc.    │
        │  41 registered composables + 4      │
        │  intentionally-unregistered sub-parts│
        └────────────┬──────────────────────────┘
                     │
        ┌────────────▼────────────────────────────────────────────┐
        │              Theme + Modifier Layers                    │
        │  ┌────────────────────┬──────────────────────────────┐  │
        │  │ Theme Tokens       │ Modifier: SwipeableActionRow│  │
        │  │ Color, Dimens,     │ (Anchored drag state,       │  │
        │  │ Type, ThemeMode    │  reveal-confirm swipe)      │  │
        │  └────────────────────┴──────────────────────────────┘  │
        └─────────────────────────────────────────────────────────┘
                     │
        ┌────────────▼─────────────────────────────────────────┐
        │          Feedback & State Singleton (Hilt)          │
        │  ┌──────────────────┬──────────────────────────────┐ │
        │  │ UndoHistoryStore │ FeedbackDispatcher           │ │
        │  │ (@Singleton)     │ (Interface, consumer impls)  │ │
        │  │ - entry append   │ - emit(FeedbackEvent)        │ │
        │  │ - attemptUndo()  │ - LocalFeedbackController    │ │
        │  │ - clearSpent()   │   (CompositionLocal)         │ │
        │  └──────────────────┴──────────────────────────────┘ │
        └──────────────────────────────────────────────────────┘
                     │
        ┌────────────▼─────────────────────────────────────────┐
        │  Model Layer (UI Data Transfer Objects)              │
        │  ListItemUiModel, TagChipUiModel, UndoHistoryEntry, │
        │  BrowseSortPreference, TagManagementUiModel, etc.    │
        └──────────────────────────────────────────────────────┘
                     │
        ┌────────────▼─────────────────────────────────────────┐
        │  Gallery / Explorer Layer (Standalone Activity)      │
        │  ExplorerActivity → ExplorerEntry (NavHost)          │
        │  └─ Component Registry (single source of truth)      │
        │  └─ Per-family screens (Cards, Chips, Sheets, ...)   │
        └──────────────────────────────────────────────────────┘
```

## Component Responsibilities

| Component | Responsibility | File |
|-----------|----------------|------|
| **Theme tokens** | Define color, typography, spacing; apply Material 3 design system | `theme/Color.kt`, `theme/Type.kt`, `theme/Dimens.kt` |
| **YahirAndroidTasteTheme** | Material 3 theme wrapper; every component + consumer UI renders inside it | `theme/Theme.kt` |
| **Card family** | 5 card archetypes (Text, List, Album, Voice) + shared sub-rows (QuickView, TagRow, AdaptiveMediaPreview) | `component/TextCard.kt`, `component/ListCard.kt`, `component/AlbumCard.kt`, `component/VoiceCard.kt` |
| **CardBase** | Shared shell with reveal-confirm swipe (left=delete, right=edit), dropdown menu, tap/long-press | `component/CardBase.kt` |
| **SwipeableActionRow** | Horizontal drag-to-swipe gesture (AnchoredDraggable state machine); not a visible tile, infrastructure only | `modifier/SwipeableActionRow.kt` |
| **Chip family** | AppChip, TagChipWithContextMenu, generic ChipBar, SortControl, FilterBar | `component/AppChip.kt`, `component/ChipBar.kt`, `component/SortControl.kt`, `component/FilterBar.kt` |
| **Sheet family** | ModalBottomSheet content bodies: editors (TextCard, ListCard), tag picker, recording, album source/title, bulk create | `component/*BottomSheet.kt`, `component/*Sheet.kt` |
| **Button/FAB family** | ExpandableFab (per-type create actions), CycleSubTypeButton, DynamicActionButton | `component/ExpandableFab.kt`, `component/DynamicActionButton.kt` |
| **Picker family** | AccentColorPicker, IconPickerGrid, CropOverlay | `component/AccentColorPicker.kt`, `component/IconPickerGrid.kt`, `component/CropOverlay.kt` |
| **Feedback layer** | UndoHistoryStore (undo state machine), FeedbackEvent/Dispatcher (snackbar port), UndoCenterScreen | `feedback/UndoHistoryStore.kt`, `feedback/FeedbackDispatcher.kt`, `feedback/UndoCenterScreen.kt` |
| **Model layer** | Serializable UI data: ListItemUiModel, TagChipUiModel, UndoHistoryEntry, etc. | `model/*.kt` |
| **Explorer gallery** | Standalone ExplorerActivity; registry (single source of truth for all public composables); per-family screens | `explorer/ExplorerActivity.kt`, `explorer/ComponentRegistry.kt`, `explorer/*FamilyScreen.kt` |

## Pattern Overview

**Overall:** Stateless component catalog pattern. The library is a curated set of **pure Compose functions** that accept domain data + callbacks as parameters, combined with a **single shared Hilt singleton** (`UndoHistoryStore`) for undo state, and a **composition-local feedback dispatcher** that consumers implement.

**Key Characteristics:**
- **One-way dependency:** Library imports no consumer code, holds no domain secrets. Composables are data-agnostic—every parameter comes from the caller.
- **Bindings-only Hilt:** `@Singleton` bindings (`UndoHistoryStore` with `@Inject constructor()`) but no `@HiltAndroidApp` or `@AndroidEntryPoint`—consumer owns the Hilt `Application`.
- **Catalog organization:** 41 registered public composables + 4 intentionally-unregistered sub-parts (CardBase, WaveformCanvas, SwipeableActionRow, YahirAndroidTasteTheme) grouped into seven families: Cards (9), Chips (5), Sheets (18), Buttons/FAB (3), Pickers (3), Feedback (2), Empty-state (1).
- **Single source of truth:** `ComponentRegistry` in `explorer/ComponentRegistry.kt` registers every public composable; drift guard (build-time integrity test) ensures every public function is either registered or allowlisted.
- **Interaction conventions are load-bearing:** Reveal-confirm swipe (left→delete, right→edit), standardized snackbar/undo feedback, and conditional-render-no-dead-space are library-wide patterns, not ad-hoc.

## Layers

**Theme Layer:**
- Purpose: Define Material 3 design tokens; apply consistent light/dark color schemes.
- Location: `theme/`
- Contains: Color definitions (`Color.kt`), type scale (`Type.kt`), dimensions (`Dimens.kt`), `YahirAndroidTasteTheme` root composable, `ThemeMode` enum.
- Depends on: Android SDK, Compose Material3.
- Used by: Every component and consumer UI wraps inside `YahirAndroidTasteTheme`.

**Component Layer:**
- Purpose: Public composable functions for cards, chips, sheets, buttons, pickers, feedback surfaces, empty-state.
- Location: `component/`
- Contains: 41 registered composables (per family).
- Depends on: Theme, Modifier, Model, Compose runtime/foundation/material3, Coil (images), navigation-compose, reorderable.
- Used by: Consumer apps call these directly.

**Modifier Layer:**
- Purpose: Shared interaction logic, specifically swipe-to-reveal mechanics.
- Location: `modifier/`
- Contains: `SwipeableActionRow` (AnchoredDraggable state machine for left/right swipe, three-position anchors).
- Depends on: Compose foundation, material3 (colors).
- Used by: `CardBase`, `EditorItemRow`, and any component needing swipe interaction.

**Model Layer:**
- Purpose: Serializable UI data-transfer objects.
- Location: `model/`
- Contains: `ListItemUiModel`, `TagChipUiModel`, `UndoHistoryEntry`, `TagManagementUiModel`, `BrowseSortPreference`, `BrowseTagSortMode`, `TagSortMode`.
- Depends on: Pure Kotlin (no Compose).
- Used by: Components accept these as parameters; consumers map domain data into these shapes.

**Feedback Layer:**
- Purpose: Undo state machine, snackbar event dispatcher, and feedback UI (UndoCenterScreen).
- Location: `feedback/`
- Contains: `UndoHistoryStore` (Hilt singleton), `FeedbackEvent` enum, `FeedbackDispatcher` interface (consumer implementation port), `UndoCenterScreen` composable, undo tracking/preview utilities.
- Depends on: Pure Kotlin + `javax.inject`, Compose runtime, Kotlin Flow.
- Used by: Consumer apps inject `UndoHistoryStore`, implement `FeedbackDispatcher`, provide it via `LocalFeedbackController` CompositionLocal.

**Explorer / Gallery Layer:**
- Purpose: Standalone debug-only component gallery; registry of all public composables; per-family showcase screens.
- Location: `explorer/`
- Contains: `ExplorerActivity` (standalone entry point), `ComponentRegistry` (registry + drift guard), seven per-family screens (CardsFamilyScreen, ChipsFamilyScreen, SheetsFamilyScreen, etc.), `ExplorerEntry` (internal NavHost root), state/detail screens.
- Depends on: Theme, Components, Compose navigation.
- Used by: Consumers launch gallery via explicit Intent to `ExplorerActivity` (optional; for design-token browsing and component showcase).

**Icon Utilities Layer:**
- Purpose: Helper for card-type icon resolution.
- Location: `icon/`
- Contains: `CardTypeIconUtil` — maps card types to Material Icons.
- Depends on: Material Icons, pure Kotlin.
- Used by: Gallery screens, components that display card-type indicators.

## Data Flow

### Primary Request Path: Composing a Card

1. **Caller (Consumer)** (`MainActivity.kt` in SecondBrain) constructs domain model (e.g., a note).
2. **Caller maps** domain model → `TextCard` parameters: `id`, `title`, `content`, `isPinned`, tap/swipe callbacks.
3. **Caller invokes** `TextCard(...)` inside `YahirAndroidTasteTheme { ... }` (`theme/Theme.kt`).
4. **TextCard** (`component/TextCard.kt`) delegates to `CardBase` (`component/CardBase.kt`, line ~80+).
5. **CardBase** wraps content in `SwipeableActionRow` (`modifier/SwipeableActionRow.kt`); renders three-dot menu, footer content slot.
6. **Swipe gesture:** User drags card left/right. `SwipeableActionRow` updates `AnchoredDraggableState` (anchors: Resting, RevealedLeft, RevealedRight).
7. **Reveal slot render:** Edit (left anchor, `primaryContainer` color) or Delete (right anchor, `errorContainer` color) button appears.
8. **User taps button:** `onEditClick` or `onDeleteClick` callback fires (defined by caller in step 2).
9. **Caller receives callback** in its own event handler/ViewModel; updates domain model, re-calls `TextCard` with new state.

**Theme is applied transitively:** `YahirAndroidTasteTheme` (wrapper) → `CardBase` → material3 `Card` → theme colors resolved via `MaterialTheme.colorScheme`.

### Undo Flow: Append → Feedback → History Center

1. **Consumer action** triggers a domain-model update (e.g., user deletes a note).
2. **Consumer ViewModel** calls `UndoHistoryStore.append(message, preview, undoAction)` (injected from Hilt `SingletonComponent`).
3. **Store appends** entry (newest-first), applies 50-cap eviction, returns entry ID.
4. **Consumer** forwards `FeedbackEvent` to snackbar via `LocalFeedbackController.current.emit(event)`.
5. **FeedbackDispatcher** (consumer's `FeedbackController` in `:app`) routes event → snackbar Composable.
6. **Snackbar** shows "Undo delete note" + action button; user taps Undo.
7. **Snackbar** calls `UndoHistoryStore.attemptUndo(id)` → store invokes the captured `undoAction` lambda (domain reversal).
8. **Store updates** entry status to `UndoStatus.Undone`; emits state change via `StateFlow<List<UndoHistoryEntry>>`.
9. **UndoCenterScreen** (if open) recomposes; shows updated history with visual feedback (checkmark, strikethrough).

**State is durable:** `UndoHistoryStore` is `@Singleton`, so entries persist across composition recompositions and screen rotation.

### Secondary Flow: Gallery / Component Showcase

1. **Consumer (or developer) launches** `ExplorerActivity` via explicit Intent.
2. **ExplorerActivity.onCreate()** calls `setContent { YahirAndroidTasteTheme { ExplorerEntry(...) } }`.
3. **ExplorerEntry** (`explorer/ExplorerEntry.kt`) owns internal `NavHost` (string-based routes: "index", "family/$family", "detail/$name", "tokens").
4. **ExplorerIndexScreen** renders list of seven families.
5. **User taps a family** → `navController.navigate("family/Cards")` → **CardsFamilyScreen** renders.
6. **CardsFamilyScreen** reads `cardsFamilyEntries` from `ComponentRegistry.entries` (phase-62-onward structure: each family screen holds its own entries list, concatenated in registry).
7. **Per-entry detail page:** user taps a component tile → `navController.navigate("detail/TextCard")` → **ComponentDetailScreen** renders.
8. **ComponentDetailScreen** looks up `ComponentRegistry.entries.find { it.name == "TextCard" }`, renders its `states` matrix (Default/Pressed/Disabled/Focused) and `content` (Variants showcase).
9. **Playground section:** user adjusts live `controls` (e.g., Toggle: Pinned) → `PlaygroundState` updates → `preview` lambda re-renders with new knob value.
10. **Back button** on family screen calls `navController.navigateUp()` (internal) → returns to index. Back button on index calls `onNavigateBack` (external) → exits gallery to `:app`.

**Registry integrity:** Build-time test ensures every public composable in `component/`, `feedback/`, `modifier/`, `theme/` is either in `entries` (registered) or in `INTENTIONALLY_UNREGISTERED` (allowlisted with reason). Drift immediately fails the build.

**State Management:**
- **Theme selection (light/dark)** hoisted in `ExplorerEntry` via `rememberSaveable { mutableStateOf(ThemeMode.LIGHT) }` — persists across family navigation.
- **Detail page states** authored per-component in family screen files; each entry's `states: List<StateCell>` is fixed at authorship time (not dynamic).
- **Playground live state** captured in `PlaygroundState` (phase 63 onward); passed to component `preview` lambda for real-time re-render.

## Key Abstractions

**ComponentRegistry.Entry:**
- Purpose: Single cell in the seven-family component catalog. Registers a public composable for gallery inclusion + drift guard.
- Examples: `ComponentRegistry.Entry(name="TextCard", family=CARDS, states=[...], content={...}, controls=[...], preview={...})`.
- Pattern: Each family screen file (e.g., `CardsFamilyScreen.kt`) declares its own `<family>FamilyEntries: List<Entry>` list, concatenated in `ComponentRegistry.entries`. This decouples family-content authoring from the shared registry file.

**SwipeableActionRow & SwipeAnchor:**
- Purpose: Encapsulate horizontal swipe-to-reveal mechanics (AnchoredDraggable state machine).
- Examples: Used by `CardBase`, `EditorItemRow`.
- Pattern: Three anchors (Resting, RevealedLeft, RevealedRight); left swipe reveals Delete, right swipe reveals Edit. Actions fire on button tap, not on gesture settle (SWIPE-01 contract).

**FeedbackEvent & FeedbackDispatcher:**
- Purpose: Decouple component feedback (undo snackbars) from navigation concerns (NavHostController).
- Examples: `FeedbackEvent.WithUndo(message, preview, undoAction)`.
- Pattern: Components + store emit `FeedbackEvent`; consumer implements `FeedbackDispatcher` interface; event flows through `LocalFeedbackController` CompositionLocal (no import of consumer code, pure interface).

**UndoHistoryStore:**
- Purpose: Session-durable undo state machine (Hilt singleton).
- Pattern: `@Inject constructor()`, pure Kotlin + `javax.inject`, no Compose dependency. Append entries newest-first; 50-cap eviction (prefer evicting spent entries); CAS-guarded `tryConsume()` for first-consumer-wins concurrency.

**TagChipUiModel, ListItemUiModel, etc.:**
- Purpose: Serializable UI data-transfer objects; consumers map domain data into these.
- Pattern: No Compose, no domain leakage. Each model corresponds to a component or component family (e.g., `TagChipUiModel` for `AppChip` / `TagChipWithContextMenu`).

## Entry Points

**ExplorerActivity:**
- Location: `explorer/ExplorerActivity.kt`
- Triggers: Consumer launches via explicit Intent (optional, for debug/showcase).
- Responsibilities: Standalone Activity; hosts `ExplorerEntry` NavHost; applies edge-to-edge insets; wraps in `YahirAndroidTasteTheme`.

**Public Composables (Catalog):**
- Location: `component/`, `feedback/`, `theme/`, `modifier/` packages.
- Triggers: Consumer Composables call them directly by name (e.g., `TextCard(...)`, `AppChip(...)`).
- Responsibilities: Render UI given domain data + callbacks; enforce interaction conventions (swipe, snackbar, etc.).

**Hilt Singleton Injection:**
- Location: `feedback/UndoHistoryStore.kt`.
- Triggers: Consumer ViewModel or Composable injects via `@Inject val store: UndoHistoryStore`.
- Responsibilities: Persist undo entries across recompositions; execute undo actions on demand.

**CompositionLocal: LocalFeedbackController:**
- Location: `feedback/FeedbackDispatcher.kt`.
- Triggers: Components call `LocalFeedbackController.current.emit(event)` within a Composable scope.
- Responsibilities: Route feedback events (undo, confirmation) to consumer's snackbar host (consumer provides implementation).

## Architectural Constraints

- **Threading:** Single-threaded Compose event loop; undo action lambdas are `suspend` functions, executed in caller's coroutine scope (ViewModel, LaunchedEffect). Store appends/reads are `@Synchronized` via `MutableStateFlow`.
- **Global state:** `UndoHistoryStore` is the only module-level singleton. Hoisted at consumer's Hilt `Application`. No mutable companion objects or `object` singletons in the library.
- **Circular imports:** None detected. Dependency direction: Component → Theme/Modifier/Model (no reverse). Explorer → Component/Feedback (gallery is library-only, not exported to consumers).
- **No domain assumptions:** Composables accept callbacks (`onTap`, `onDelete`, `onTagClick`) and data models (`ListItemUiModel`, `TagChipUiModel`); they render and return control to caller. No app-specific enums, no consumer package imports.
- **Compose version alignment:** Consumer must use same Compose BOM (2026.02.01). Mismatch is a runtime crash (composition error), not a compile-time failure.

## Anti-Patterns

### Circular dependency between Component and Feedback

**What happens:** A component tries to import `FeedbackEvent` from `feedback/` and also exports callback types that feedback layer consumes, creating a cycle.

**Why it's wrong:** Hides architectural intent; makes layering unclear; complicates testing and reuse.

**Do this instead:** Feedback events flow one direction: Component emits via `LocalFeedbackController.current.emit(event)` (using the interface). Callbacks are always defined by the caller, never by the component. See `component/CardBase.kt` callbacks (`onDeleteClick`, `onEditClick`) — they are parameters, not feedback events.

### Allowing @HiltAndroidApp in the library

**What happens:** A developer adds `@HiltAndroidApp class YahtApplication : Application()` to the library module.

**Why it's wrong:** Only the consumer app owns the Hilt `Application`. The library is bindings-only. Adding an app host here breaks reusability—consumers can't have their own `@HiltAndroidApp` (Hilt allows only one per process).

**Do this instead:** Declare `@Singleton` bindings (e.g., `@Singleton class UndoHistoryStore @Inject constructor()`), but never `@HiltAndroidApp` or `@AndroidEntryPoint`. Consumer's `Application` class and `Activity` classes own those annotations. See `CLAUDE.md` invariant #2 and `INTEGRATION.md` §3.

### Registering a private composable in ComponentRegistry

**What happens:** A helper sub-part like `CardBase` or `WaveformCanvas` is added to the `entries` list as if it were a public, standalone component.

**Why it's wrong:** `CardBase` is infrastructure (wrapped by `TextCard`/`ListCard`/etc.); rendering it standalone in the gallery is confusing. The registry exists to showcase public surface composables, not internal plumbing.

**Do this instead:** If a composable is structural (a sub-part exercised indirectly), add it to `ComponentRegistry.INTENTIONALLY_UNREGISTERED` with a one-line reason. See `INTENTIONALLY_UNREGISTERED` map in `explorer/ComponentRegistry.kt`: CardBase, WaveformCanvas, SwipeableActionRow, YahirAndroidTasteTheme are all documented as "not standalone."

## Error Handling

**Strategy:** No error recovery inside components; components are data-driven. Errors bubble to caller via callback return values or exception propagation.

**Patterns:**
- **Validation before call:** Caller validates input (e.g., title is not blank) before calling component.
- **Callbacks report state:** Button/tab callbacks return success/failure to caller via sealed classes or suspending function results.
- **Undo action failures:** `UndoHistoryStore.attemptUndo()` catches `Exception` (not `Throwable`), marks entry `UndoStatus.Failed`, but does NOT re-throw. Caller observes via `entries: StateFlow<List<UndoHistoryEntry>>`. See `feedback/UndoHistoryStore.kt` line 74–91.
- **CompositionLocal runtime error:** Accessing `LocalFeedbackController.current` outside a `CompositionLocalProvider` scope throws `IllegalStateException` with a helpful message. See `feedback/FeedbackDispatcher.kt` line 44–46.

## Cross-Cutting Concerns

**Logging:** Not used in the library. Consumer ViewModel/Controller handles logging of user actions (delete, undo, etc.).

**Validation:** Components validate parameters at call-site only (e.g., `@Composable fun TextCard(id: String, title: String, ...)` expects non-empty strings). Pre-validation is caller's responsibility; components do not sanitize or retry.

**Authentication:** Not applicable. Library is domain-agnostic; no user identity, no permission checks.

---

*Architecture analysis: 2026-08-21*
