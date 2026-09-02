# API.md — `yahirandroidtaste` public surface (the nine-family composable catalog)

Everything a consumer calls. Package root: `io.github.ygaray.yahirandroidtaste`. To wire the
library, see `INTEGRATION.md`; for the reuse rules, `CLAUDE.md`.

This is a **UI component library**, so its public surface is a **catalog of composables**, not a
service seam. The composables are organized into the library's **nine families** — the same
taxonomy the library ships in `explorer/ComponentRegistry.kt` (`cardsFamilyEntries +
chipsFamilyEntries + sheetsFamilyEntries + buttonsFabFamilyEntries + pickersFamilyEntries +
feedbackFamilyEntries + emptyStateFamilyEntries + progressFamilyEntries +
tactileFoundationFamilyEntries`), which is the single source of truth and the CATALOG drift guard.

## Surface at a glance

| Family | Registered composables | What it is |
|--------|-----------------------|------------|
| 1. Cards | 9 | Card faces + card-face sub-rows for the five card archetypes |
| 2. Chips | 4 | Tag/selection chips and the bar that lays them out (with an optional filter/sort chrome mode) |
| 3. Sheets | 18 | Bottom-sheet / editor / popup content surfaces and their scaffolding |
| 4. Buttons / FAB | 3 | The expandable create-FAB and dynamic action buttons |
| 5. Pickers | 4 | Accent-color, icon, crop, and segmented-option pickers |
| 6. Feedback | 3 | Confirmation dialog, the Undo Center, and the attention-cue glyph |
| 7. Empty-state | 1 | The shared empty-state surface |
| 8. Progress / Metrics | 4 | Determinate ring / count-up / hero-card primitives for at-a-glance stat display |
| 9. Tactile Foundation | 4 | Elevation ladder, Space Grotesk display ramp, gradient/tint accent surfaces, and the Heat relatedness ramp |

**51 registered public composables** across the nine families, plus **5 intentionally-unregistered**
structural sub-parts (see the end of this doc) = **56 public composables total**. Every component
renders inside `YahirAndroidTasteTheme` (family 7's theme wrapper — see the tail note). Every
`Modifier` parameter defaults to `Modifier`; only the load-bearing parameters are listed below.

> Model types referenced below (e.g. `TagChipUiModel`, `ListItemUiModel`, `MediaThumbnailCell`,
> `UndoHistoryEntry`) live in the library's `model/` package and are part of the public surface —
> the consumer maps its domain data into them at the call site.

---

## 1. Cards

Card faces for the five archetypes (Text, List, Album, Voice) plus shared card-face sub-rows. All
card faces support the library's reveal-confirm swipe convention (left→delete, right→edit) via the
underlying `CardBase` shell.

| Composable | Purpose | Key parameters |
|-----------|---------|----------------|
| `TextCard` | Text-note card face | `id, title, content: String?`, tap/swipe callbacks |
| `ListCard` | List card face (bulleted / ordered / checkbox `subType`) | `id, title, subType, …` list preview + callbacks |
| `AlbumCard` | Photo-album card face | `id, title, isPinned, …` thumbnails + callbacks |
| `VoiceCard` | Voice-note card face | `id, title, durationMs, …` play/rename callbacks |
| `AdaptiveMediaPreview` | Adaptive thumbnail grid used inside media card faces | `cells: List<MediaThumbnailCell>, onCellTap(index), onOverflowTap` |
| `CardTagRow` | Capped tag-chip row on a card face (with `+N` overflow) | `tags: List<TagChipUiModel>, onTagClick(tagId), onSiblingsClick` |
| `CardQuickView` | Read-only quick-view of a card's metadata | `title, createdAt, updatedAt, …` |
| `CountBadge` | Small count badge, accent-tinted | `count: Int, tileAccentColor: Color` |
| `TagListItem` | A tag row in tag-management surfaces | `tag: TagManagementUiModel, onClick` |

## 2. Chips

Tag / selection chips and the generic bars that arrange them. The bars are generic (`fun <T> …`) so
a consumer supplies its own item type.

| Composable | Purpose | Key parameters |
|-----------|---------|----------------|
| `AppChip` | The base selectable chip | `label, isSelected, onClick` |
| `TagChipWithContextMenu` | A tag chip carrying a long-press context menu | `label, isSelected, onClick, …` menu callbacks |
| `ChipBar` | Generic horizontally-scrolling chip row, with an optional expand/collapse chrome mode (WO-1) | `items: List<T>, key: (T)->Any, itemContent: @Composable (T)->Unit`, optional `leading/trailingContent`, optional `expandable: ExpandableConfig? = null` — non-null wraps the row in expand/collapse chrome (chevron + tonal `Surface`, single-line-clip collapsed / height-capped-scroll expanded), null (default) renders the bare row unchanged; same two-state opt-in-mode contract as `TextCardBottomSheet`'s `onEditRequest`. Optional `rawContent: (@Composable FlowRowScope.() -> Unit)? = null` carries freeform body content in place of `items`/`itemContent` |
| `SortControl` | Generic sort-mode selector | `sortMode: T, options: List<T>, optionLabel: (T)->String, onSortModeChange` |

## 3. Sheets

Bottom-sheet / editor / popup **content** surfaces (the caller owns the `ModalBottomSheet` host;
these render its body) plus the shared scaffolding and editor rows.

| Composable | Purpose | Key parameters |
|-----------|---------|----------------|
| `SheetScaffold` | Shared modal-bottom-sheet scaffold (drag handle, dismissal) | `onDismissRequest, sheetState: SheetState = rememberModalBottomSheetState()` |
| `NameAndTagsEditor` | Name field + tag editor with a dynamic header slot (conditional-render) | `header: @Composable ColumnScope.() -> Unit = {}, name, onNameChange, …` |
| `ClearableTextField` | Text field with a clear (✕) affordance | `value, onValueChange` |
| `EditorItemRow` | A reorderable list-editor row — **exposes a `ReorderableCollectionItemScope` receiver** (from `sh.calvin.reorderable`, re-exported via `api`) | receiver `ReorderableCollectionItemScope`; `item: ListItemUiModel, itemIndex, isDragging, …` |
| `CardEditorShellContent` | The pluggable card-editor shell body (content slot per card type) | `accentColor: Long?, onSave, onNavigateBack, …` |
| `TextCardBottomSheet` | Read-only text-card preview sheet content (pin/favorite/edit/delete) | metadata + `onEditRequest: (() -> Unit)? = null` — bound routes the Edit row to the host's shared name-and-tags sheet, null (default) falls back to this sheet's local tag-less rename dialog |
| `ListCardBottomSheet` | Read-only list-card preview sheet content (pin/favorite/edit/delete) | items + subtype + `onEditRequest: (() -> Unit)? = null` — same two-state Edit-routing contract as `TextCardBottomSheet` |
| `RecordingBottomSheetContent` | Voice-recording sheet body (waveform + timer) | `uiState: RecordingSheetUiState, elapsedSeconds` |
| `AlbumSourcePickerSheet` | Camera-vs-gallery source picker sheet | `onNavigateToCamera, onNavigateToGallery, …` |
| `AlbumTitleConfirmSheet` | Album-title confirmation sheet | title state + confirm callback |
| `VoiceRenameTagsSheet` | Voice-note rename + tags sheet | `defaultTitle, onSave(title), …` |
| `TagPickerSheet` | Full tag-picker sheet (host + content) | `existingTagIds: Set<String>, allTags: List<TagChipUiModel>, onDone(List<String>)` |
| `TagPickerSheetContent` | Tag-picker body (no host) | `allTags, selection, onDone` |
| `TagChipEditorContent` | Inline tag-chip editor body | `currentTags: List<TagChipUiModel>, isLastTag, …` — applied chips also support double-tap-to-remove, routed through the same undo-backed `onRemoveTag` callback the long-press menu's "Remove from this card" item uses |
| `TagCreateSheet` | Create-a-tag sheet (host + content) | new-tag name/color + confirm |
| `TagCreateSheetContent` | Create-a-tag body (no host) | new-tag name/color + confirm |
| `BulkCreatePopup` | Bulk create-multiple popup (host + content) | `onDismissRequest, actionLabel, …` |
| `BulkCreatePopupContent` | Bulk-create body (no host) | line entries + create callback |

> **Host vs. content split:** several sheets ship both a `…Sheet`/`…Popup` (owns the modal host) and
> a `…Content` (body only) so a consumer can either drop in the full sheet or embed the body in its
> own host. Both are registered so the gallery showcases each independently.

## 4. Buttons / FAB

| Composable | Purpose | Key parameters |
|-----------|---------|----------------|
| `ExpandableFab` | The expandable create-FAB that fans out per-card-type create actions | `onCreateTextCard, onCreateListCard, …` per-type callbacks |
| `CycleSubTypeButton` | Cycles a card's sub-type (e.g. list ordering) | `currentSubType, onCycle(nextSubType), enabled = true` |
| `DynamicActionButton` | Semantically-colored dynamic action button (destructive/save/neutral; disabled until dirty) | `label, role: ActionButtonDefaults.ActionButtonRole, onClick` |

## 5. Pickers

| Composable | Purpose | Key parameters |
|-----------|---------|----------------|
| `AccentColorPicker` | Accent-color swatch picker | `selectedColor: Long, onColorSelected: (Long) -> Unit` |
| `IconPickerGrid` | Module/tag icon grid picker | `selectedIcon: String, onIconSelected: (String) -> Unit` — public parameters unchanged; the grid includes a built-in live case-insensitive name-substring search field with an empty-state when nothing matches |
| `CropOverlay` | Crop-rectangle overlay for image editing | `bitmapWidth, bitmapHeight, aspectRatio: Float?, …` |
| `SegmentedOptionSelector` | Two-option segmented toggle with an always-visible disabled+reason affordance | `selectedIndex: Int, options: List<String>, onSelect: (Int) -> Unit, enabled: Boolean, disabledReason: String?` |

## 6. Feedback

| Composable | Purpose | Key parameters |
|-----------|---------|----------------|
| `ConfirmationDialog` | Standard confirm/cancel dialog | `title, body, onDismissRequest, …` confirm/dismiss callbacks |
| `UndoCenterScreen` | The Undo Center — a history of undoable actions (backed by `UndoHistoryStore`, see `INTEGRATION.md`) | `entries: List<UndoHistoryEntry>, onNavigateBack, onUndo: (String) -> Unit` |
| `AttentionCue` | Caution/verify signal glyph — never a failure signal | `text: String?, style: AttentionCueDefaults.Style, icon: ImageVector, tint: Color` |

## 7. Empty-state

| Composable | Purpose | Key parameters |
|-----------|---------|----------------|
| `EmptyState` | Shared empty-state surface (icon + title + optional body/action) | `icon: ImageVector, title: String, …` |

## 8. Progress / Metrics

Determinate progress / count-up / hero-card primitives for at-a-glance stat display, originally
upstreamed from CalTracker's give-leg (Phase 42/43, GIVE-04).

| Composable | Purpose | Key parameters |
|-----------|---------|----------------|
| `MetricBar` | Labeled progress bar with an optional header-only mode | `label, valueText, fraction: Float?, band: MetricBand?, remainingText: String?` — `fraction`/`band` are required only when `remainingText` is non-null (IN-02) |
| `ProgressRing` | Determinate animated ring, draw-phase-only fill read (perf discipline) | `fraction: Float, strokeWidth = 8.dp, trackColor, progressColor, animationSpec, content: @Composable BoxScope.() -> Unit = {}` |
| `AnimatedStatValue` | Animated count-up/count-down numeral, caller-formatted | `targetValue: Float, style, color, animationSpec, format: (Float) -> String` |
| `HeroStatCard` | Generic hero/stat card face with a thin leading-edge accent stripe | `label: String, value: String, onClick: (() -> Unit)?, shape, containerColor, accentBrush, content: (@Composable ColumnScope.() -> Unit)?` |

## 9. Tactile Foundation

Four foundational design-primitive families shipped as one cohesive drop (SecondBrain v2.0
Phase 123, `DS-01`): an elevation/shadow scale, a Space Grotesk display-type ramp, gradient/tint
accent-surface helpers, and an independent Heat relatedness color ramp. Each showcase demos its
own primitive(s) live in the Explorer; none of the four existing shared surfaces they sit beside
(`Dimens`, `theme/Type.kt`'s `Typography`, `ColorUtils.contrastingForeground`,
`RelatednessEncoding`'s Jaccard ramp) was modified — every addition here is a wholly additive
sibling.

| Composable | Purpose | Key parameters |
|-----------|---------|----------------|
| `ElevationLadder` | Renders all six `Dimens.Elevation` levels (Level0–Level5) as real-shadow bands for a light/dark depth-scale comparison | `modifier: Modifier = Modifier` |
| `TactileTypeShowcase` | Renders all four `TactileType` display tiers (real Space Grotesk weights) with a long-sample clipping check and a same-text `FontFamily.Default` comparison row | `modifier: Modifier = Modifier` |
| `GradientSwatch` | Renders `accentGradient`'s hero band and `accentTint`'s flat card fill side by side for one caller-supplied accent | `accentColor: Color, modifier: Modifier = Modifier` |
| `HeatSwatch` | Renders all six Heat tiers as sized/colored/stroked mindmap-node samples connected by edges (horizontally scrollable), plus one distinct-hub-ring example | `modifier: Modifier = Modifier` |

**Non-composable primitives (also part of the public surface, called directly rather than
rendered):**
- `Dimens.Elevation` (`Level0`..`Level5`) — the six dp shadow-elevation levels these showcases demo.
- `TactileType` (`DisplayLarge/Medium/Small/XSmall`) + `SpaceGroteskFamily` — the Space Grotesk
  `TextStyle` ramp and its backing `FontFamily`.
- `accentGradientStops`, `accentGradient`, `accentTint` (`component/ColorUtils.kt`) — the
  parametrized gradient/tint pure functions `GradientSwatch` demos.
- `HeatTier`, `HeatVisual`, `heatTier`, `heatVisual`, `hubNodeVisual` (`component/RelatednessEncoding.kt`)
  — the independent Heat ramp's types and pure functions `HeatSwatch` demos.

---

## Intentionally-unregistered sub-parts (6)

Public composables that are **not** standalone catalog tiles (structural sub-parts, exercised
indirectly), tracked in `ComponentRegistry.INTENTIONALLY_UNREGISTERED`:

| Composable | Why unregistered |
|-----------|------------------|
| `CardBase` | Structural shell every card type wraps — exercised via `TextCard`/`ListCard`/`AlbumCard`/`VoiceCard`. |
| `WaveformCanvas` | Sub-part rendered inside the voice-recording sheet / `VoiceCard` — exercised indirectly. |
| `SwipeableActionRow` | The reveal-confirm swipe mechanics powering `CardBase` and `EditorItemRow` — infrastructure, not a visual archetype. |
| `RevealActionRow` | Swipe-reveal mechanics for arbitrary 0-2 action slots — infrastructure, not an independent visual archetype; exercised indirectly via callers' own row demos. |
| `YahirAndroidTasteTheme` | The theme wrapper every component (and every gallery screen) renders inside — it *is* the chrome, not a showcaseable tile. Wrap your UI in it: `YahirAndroidTasteTheme { … }`. |
| `SheetHeaderMenu` | Header/menu/rename chrome extracted from `TextCardBottomSheet`/`ListCardBottomSheet` (WO-2) — infrastructure, not an independently showcase-able archetype; already exercised indirectly via every sheet entry's own header/menu/rename interaction. |

## Adding / changing components (breaking-change note)

Removing or renaming a public composable, or changing a component's required parameters, is a
**breaking change** for every consumer — bump the major and coordinate the human-gated repin (see
`CLAUDE.md` / `ECOSYSTEM.md` §7). Adding a new public composable requires registering it in its
family's entries list (or allowlisting it) or the `ComponentRegistry` drift guard fails the build.
