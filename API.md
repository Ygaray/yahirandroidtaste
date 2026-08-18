# API.md — `yahirandroidtaste` public surface (the seven-family composable catalog)

Everything a consumer calls. Package root: `io.github.ygaray.yahirandroidtaste`. To wire the
library, see `INTEGRATION.md`; for the reuse rules, `CLAUDE.md`.

This is a **UI component library**, so its public surface is a **catalog of composables**, not a
service seam. The composables are organized into the library's **seven families** — the same
taxonomy the library ships in `explorer/ComponentRegistry.kt` (`cardsFamilyEntries +
chipsFamilyEntries + sheetsFamilyEntries + buttonsFabFamilyEntries + pickersFamilyEntries +
feedbackFamilyEntries + emptyStateFamilyEntries`), which is the single source of truth and the
CATALOG drift guard.

## Surface at a glance

| Family | Registered composables | What it is |
|--------|-----------------------|------------|
| 1. Cards | 9 | Card faces + card-face sub-rows for the five card archetypes |
| 2. Chips | 5 | Tag/selection chips and the bars that lay them out (filter/sort) |
| 3. Sheets | 18 | Bottom-sheet / editor / popup content surfaces and their scaffolding |
| 4. Buttons / FAB | 3 | The expandable create-FAB and dynamic action buttons |
| 5. Pickers | 3 | Accent-color, icon, and crop pickers |
| 6. Feedback | 2 | Confirmation dialog + the Undo Center |
| 7. Empty-state | 1 | The shared empty-state surface |

**41 registered public composables** across the seven families, plus **4 intentionally-unregistered**
structural sub-parts (see the end of this doc) = **45 public composables total**. Every component
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
| `ChipBar` | Generic horizontally-scrolling chip row | `items: List<T>, key: (T)->Any, itemContent: @Composable (T)->Unit`, optional `leading/trailingContent` |
| `SortControl` | Generic sort-mode selector | `sortMode: T, options: List<T>, optionLabel: (T)->String, onSortModeChange` |
| `FilterBar` | Expand/collapse filter surface hosting chips in a `FlowRow` | `expanded, onExpand, onCollapse, content: @Composable FlowRowScope.() -> Unit` |

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
| `TextCardBottomSheet` | Text-card create/edit sheet content | title/content state + save callbacks |
| `ListCardBottomSheet` | List-card create/edit sheet content | list items + subtype + save callbacks |
| `RecordingBottomSheetContent` | Voice-recording sheet body (waveform + timer) | `uiState: RecordingSheetUiState, elapsedSeconds` |
| `AlbumSourcePickerSheet` | Camera-vs-gallery source picker sheet | `onNavigateToCamera, onNavigateToGallery, …` |
| `AlbumTitleConfirmSheet` | Album-title confirmation sheet | title state + confirm callback |
| `VoiceRenameTagsSheet` | Voice-note rename + tags sheet | `defaultTitle, onSave(title), …` |
| `TagPickerSheet` | Full tag-picker sheet (host + content) | `existingTagIds: Set<String>, allTags: List<TagChipUiModel>, onDone(List<String>)` |
| `TagPickerSheetContent` | Tag-picker body (no host) | `allTags, selection, onDone` |
| `TagChipEditorContent` | Inline tag-chip editor body | `currentTags: List<TagChipUiModel>, isLastTag, …` |
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
| `IconPickerGrid` | Module/tag icon grid picker | `selectedIcon: String, onIconSelected: (String) -> Unit` |
| `CropOverlay` | Crop-rectangle overlay for image editing | `bitmapWidth, bitmapHeight, aspectRatio: Float?, …` |

## 6. Feedback

| Composable | Purpose | Key parameters |
|-----------|---------|----------------|
| `ConfirmationDialog` | Standard confirm/cancel dialog | `title, body, onDismissRequest, …` confirm/dismiss callbacks |
| `UndoCenterScreen` | The Undo Center — a history of undoable actions (backed by `UndoHistoryStore`, see `INTEGRATION.md`) | `entries: List<UndoHistoryEntry>, onNavigateBack, onUndo: (String) -> Unit` |

## 7. Empty-state

| Composable | Purpose | Key parameters |
|-----------|---------|----------------|
| `EmptyState` | Shared empty-state surface (icon + title + optional body/action) | `icon: ImageVector, title: String, …` |

---

## Intentionally-unregistered sub-parts (4)

Public composables that are **not** standalone catalog tiles (structural sub-parts, exercised
indirectly), tracked in `ComponentRegistry.INTENTIONALLY_UNREGISTERED`:

| Composable | Why unregistered |
|-----------|------------------|
| `CardBase` | Structural shell every card type wraps — exercised via `TextCard`/`ListCard`/`AlbumCard`/`VoiceCard`. |
| `WaveformCanvas` | Sub-part rendered inside the voice-recording sheet / `VoiceCard` — exercised indirectly. |
| `SwipeableActionRow` | The reveal-confirm swipe mechanics powering `CardBase` and `EditorItemRow` — infrastructure, not a visual archetype. |
| `YahirAndroidTasteTheme` | The theme wrapper every component (and every gallery screen) renders inside — it *is* the chrome, not a showcaseable tile. Wrap your UI in it: `YahirAndroidTasteTheme { … }`. |

## Adding / changing components (breaking-change note)

Removing or renaming a public composable, or changing a component's required parameters, is a
**breaking change** for every consumer — bump the major and coordinate the human-gated repin (see
`CLAUDE.md` / `ECOSYSTEM.md` §7). Adding a new public composable requires registering it in its
family's entries list (or allowlisting it) or the `ComponentRegistry` drift guard fails the build.
