# Coding Conventions

**Analysis Date:** 2026-08-21

## Naming Patterns

**Files:**
- PascalCase for component files: `AlbumCard.kt`, `TagListItem.kt`, `VoiceCard.kt`
- camelCase for utility/helper files: `ColorUtils.kt`, `RelatednessEncoding.kt`
- Test files mirror source names with `Test` suffix: `AlbumCardTest.kt`, `DynamicActionButtonTest.kt`
- Theme/configuration files descriptive: `Dimens.kt`, `Color.kt`, `Type.kt`, `ThemeMode.kt`

**Functions (Composables):**
- PascalCase for all public `@Composable` functions (detekt rule `FunctionNaming` ignoreAnnotated: `Composable`)
- Examples: `AlbumCard()`, `TagListItem()`, `CardBase()`, `DynamicActionButton()`, `CycleSubTypeButton()`
- Private composables allowed and follow same PascalCase: `private fun VoiceWaveformCanvas(...)`
- Callback parameters are lambda expressions: `onTap: () -> Unit`, `onDelete: () -> Unit`, `onRename: () -> Unit`
- Nullable callbacks are explicitly typed: `onEditTags: (() -> Unit)? = null`

**Functions (Logic):**
- camelCase for pure utility functions and helpers: `nextSubType()`, `relatednessTier()`, `relatednessVisual()`
- camelCase for private component helpers: `titleSlotVisible()`, `downsample()`

**Variables:**
- camelCase for all variables: `isPinned`, `isFavorite`, `openRowState`, `callCount`
- Mutable state using Kotlin `by` delegation: `var callCount = 0`, `var captured: String? = null`
- Boolean properties use `is`/`has`/`can` prefixes: `isPinned`, `isFavorite`, `isHome`

**Types & Classes:**
- PascalCase for data classes and UI models: `TagChipUiModel`, `MediaThumbnailCell`, `TagManagementUiModel`
- PascalCase for sealed types: `RelatednessTier`, `UndoStatus`
- Enums PascalCase with UPPER_CASE members where applicable: `ActionButtonDefaults.ActionButtonRole.Save`
- Objects PascalCase: `Dimens`, `ComponentRegistry`, `LightColorScheme`

## Code Style

**Formatting:**
- Kotlin standard formatting (4-space indentation inferred from codebase)
- Line length guides visible in detekt config but no hard limit enforced beyond complexity rules
- Imports organized by: AndroidX/Compose → Material3 → local library packages
- Example order in `AlbumCard.kt`:
  ```kotlin
  import androidx.compose.foundation.*      // AndroidX/Compose
  import androidx.compose.material.icons.*  // Material Icons
  import androidx.compose.material3.*       // Material3 (latest Material Design)
  import androidx.compose.runtime.*         // Compose runtime
  import androidx.compose.ui.*              // Compose UI
  import io.github.ygaray.yahirandroidtaste.* // Local packages
  ```

**Linting:**
- Tool: **Detekt 1.23.8** (zero-baseline policy — no baseline exists for this module)
- Config: `config/detekt/detekt.yml` + `config/detekt-compose.yml` (Compose-idiomatic overrides)
- Zero-baseline constraint: new issues must be fixed, not banked as debt — **never regenerate baseline to bury findings**
- Enabled rulesets: complexity (LongMethod, LongParameterList, CyclomaticComplexMethod), naming (FunctionNaming, ClassNaming, VariableNaming), style (UnusedPrivateMember)
- Test sources excluded from naming rules (snake_case is acceptable in test methods: `fun someMethod_state_returnsX()`)
- Detekt run: `./gradlew detekt`

## Import Organization

**Order:**
1. AndroidX/Compose foundation, layout, gestures: `androidx.compose.foundation.*`
2. Material3 design system: `androidx.compose.material3.*`
3. Material Icons: `androidx.compose.material.icons.*`
4. Compose runtime: `androidx.compose.runtime.*`
5. Other AndroidX: `androidx.compose.ui.*`, `androidx.activity.compose.*`, `androidx.navigation.compose.*`
6. Third-party integrations: `coil.compose.*`, `org.burnoutcrew.reorderable.*`
7. Kotlin/coroutines: `kotlinx.coroutines.*`
8. Java stdlib: `java.io.*`, etc.
9. Local library packages: `io.github.ygaray.yahirandroidtaste.*` (grouped by subpackage: component, feedback, modifier, theme, model, explorer)

**Path Aliases:**
- No aliases used; all imports are explicit full paths
- Local imports use publisher-owned root: `io.github.ygaray.yahirandroidtaste` (renamed in Phase 101, LIB-03)

## Error Handling

**Patterns:**
- **Null-safe callbacks:** Optional callbacks are nullable parameters with explicit null checks at call site
  ```kotlin
  if (onEditTags != null) {
      DropdownMenuItem(
          text = { Text("Edit tags") },
          onClick = { dismissMenu(); onEditTags() },  // Safe call after null check
          ...
      )
  }
  ```
- **Default parameter values:** Use `= null` or `= {}` for optional callbacks; never throw on null
  ```kotlin
  onEditTags: (() -> Unit)? = null,  // Optional, defaults to null
  onTagClick: (tagId: String) -> Unit = {},  // Optional, defaults to no-op lambda
  ```
- **Collection safety:** Default to `emptyList()` and `emptyMap()` rather than null
  ```kotlin
  tags: List<TagChipUiModel> = emptyList(),
  onTagClick: (tagId: String) -> Unit = {},
  ```
- **Coroutine error handling:** Use `withContext(Dispatchers.IO)` for blocking I/O; exceptions bubble to caller
- **No try/catch in components:** Components do not catch exceptions; they are handled by calling screen/activity
- **State validation:** Assertions in logic functions (e.g., `ComponentRegistry` init block validates no duplicate entries)

## Logging

**Framework:** `android.util.Log` (no custom wrapper observed); sparsely used in production code

**Patterns:**
- Avoided in library components (library remains domain-agnostic and consumer-agnostic)
- Debug logs possible in explorer gallery activity (not checked in current scan)

## Comments

**When to Comment:**
- **KDoc for public APIs:** Every public `@Composable` and public data class must have KDoc
- **Phase/design references:** Comments link to design docs (e.g., "D-05", "EDIT-01", "UNDO-01")
- **Intent comments:** Explain WHY, not WHAT (code should be self-evident for WHAT)
- **Workarounds:** Document non-obvious conditional renders: "Only rendered when a handler is wired — avoids a silent dead menu item"
- **Edge cases:** Comment on boundary conditions and special handling: "NaN and Infinity inputs are clamped, not thrown"

**KDoc/JSDoc:**
- Comprehensive: Every public `@Composable` includes parameter descriptions, return value (if any), and behavior notes
- Example from `AlbumCard.kt` (89 lines of KDoc for 5 core params, 10+ optional params)
  ```kotlin
  /**
   * Album card face for the module card list (list-context).
   *
   * Wraps [CardBase] with:
   * - Title header row with pin/favorite indicators and three-dot menu (Edit/Pin/Fav/Delete)
   * - Full-width [AdaptiveMediaPreview] at fixed 196dp height (D-05, D-06)
   * - Optional category path footer
   *
   * @param id Stable card ID used for keyed remember state.
   * @param title Card title.
   * ...
   */
  ```
- Behavior documented inline with `// `comment patterns for decision points

## Function Design

**Size:**
- Target: under 60 lines (detekt LongMethod threshold = 60)
- Component functions may approach 300+ lines (AlbumCard ~315) when layout + dropdown menu is inline; this is acceptable for single coherent visual unit
- Actual practice: extract repeated patterns into private helpers or separate composables (e.g., `CardBase`, `CardTagRow`)

**Parameters:**
- Required params first (no defaults), optional params last (with defaults)
- All required params should have meaningful names (no positional ambiguity)
- Callbacks always optional: `onTap: () -> Unit` (required) vs. `onEditTags: (() -> Unit)? = null` (optional)
- Destructured when domain-specific: `id: String, title: String` (not wrapped in a holder object)
- State holders passed as `MutableState<T>` for swipe reveal state: `openRowState: MutableState<AnchoredDraggableState<SwipeAnchor>?>`

**Return Values:**
- Composables return `Unit` (no explicit return in @Composable functions)
- Pure logic functions return values: `String`, `RelatednessTier`, `RelatednessVisual` (data class)
- Nullable returns only when absence is semantically meaningful; otherwise use empty collections or default values

## Module Design

**Exports:**
- Public top-level `@Composable` functions (registered in `ComponentRegistry.entries`)
- UI models and data classes used by callbacks (e.g., `TagChipUiModel`, `MediaThumbnailCell`)
- Theme tokens: `Dimens` object + `Dimens.Icons`, `Dimens.SwipeReveal`, `Dimens.CornerRadius` nested objects
- Public utility functions: `relatednessTier()`, `relatednessVisual()`, etc.

**Barrel Files:**
- Not used; imports are explicit from package root
- Example: `import io.github.ygaray.yahirandroidtaste.component.AlbumCard` (not a wildcard or barrel re-export)

**Visibility:**
- Structural components kept private: `CardBase`, `WaveformCanvas`, `SwipeableActionRow` (marked in `ComponentRegistry.INTENTIONALLY_UNREGISTERED`)
- Private helpers nested inside composables: `private fun VoiceWaveformCanvas()` inside `VoiceCard.kt`
- Private extensions: `private fun titleSlotVisible()` (call-site specific, not exported)
- **One-way dependency rule:** Library imports only AndroidX/Compose/Hilt/Coil/Navigation/Reorderable, never consumer packages

## Theme Tokens & Dimensions

**Dimens.kt:** Single source of truth for all spacing, sizing, and corner radius

**Top-level tokens (apply to all cards):**
- `HorizontalPadding = 16.dp` — horizontal padding for header/body/footer rows (per UI-SPEC)
- `TopPadding = 8.dp` — top padding for first content row
- `BottomPadding = 4.dp` — bottom padding for last content row
- `ContentSpacing = 4.dp` — spacing between title/body, body/footer
- `HairlineSpacing = 2.dp` — smallest spacing value (consolidation from 25 occurrences, Phase 43)
- `TouchTarget = 48.dp` — minimum accessible touch-target size (UIQ-01)
- `CompactPadding = 12.dp` — compact padding where standard rhythm too generous (Phase 43)
- `HairlineBorder = 1.dp` — hairline divider/border width

**Nested objects:**
- `Dimens.Icons.MenuButton = 32.dp` (MoreVert IconButton container)
- `Dimens.Icons.MenuIcon = 20.dp` (MoreVert icon size inside button)
- `Dimens.Icons.DragHandle = 24.dp` (drag handle icon)
- `Dimens.SwipeReveal.ButtonWidth = 72.dp` (swipe action button slot width, per D-07)
- `Dimens.SwipeReveal.IconSize = 24.dp` (swipe action icon size)
- `Dimens.CornerRadius.Small = 8.dp` (AppChip, IconPickerGrid, BreadcrumbBar, AlbumCameraScreen)
- `Dimens.CornerRadius.Medium = 12.dp` (AlbumTitleConfirmSheet, DrawToolPalette)

**No hardcoded `.dp` literals** in card/component code; always reference `Dimens` values.

## Interaction Conventions

**Standardized patterns** travel with components and must be preserved:

- **Reveal-confirm destructive swipe** (CardBase + SwipeableActionRow):
  - Right swipe: reveals Edit button → `onRename` callback
  - Left swipe: reveals Delete button → `onDelete` callback
  - Interaction: swipe to reveal, tap to confirm (D-04, D-05, D-07)

- **Menu edit/delete actions:**
  - Destructive actions (Delete) rendered in `MaterialTheme.colorScheme.error` (red)
  - Non-destructive actions (Edit, Pin, Favorite) in default color
  - Edit actions always present and labeled clearly ("Edit" not "Rename" per EDIT-01)

- **Undo feedback:**
  - Soft-delete followed by undo-capable snackbar (UndoHistoryStore pattern)
  - Entry stored in 50-entry cap with status-aware eviction (UNDO-01/02/03)

- **Conditional render no dead-space:**
  - Empty slots (headerContent, footerContent, tagRowContent) pass `null` instead of empty Box
  - Caller ensures no slot → no composable rendition (WR-01, G2-01/D-05)
  - Example: AlbumCard passes `tagRowContent = if (tags.isNotEmpty()) { { CardTagRow(...) } } else null`

---

*Convention analysis: 2026-08-21*
