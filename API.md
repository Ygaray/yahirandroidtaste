# API.md — `yahirandroidtaste` public surface (the ten-family composable catalog)

Everything a consumer calls. Package root: `io.github.ygaray.yahirandroidtaste`. To wire the
library, see `INTEGRATION.md`; for the reuse rules, `CLAUDE.md`.

This is a **UI component library**, so its public surface is a **catalog of composables**, not a
service seam. The composables are organized into the library's **ten families** — the same
taxonomy the library ships in `explorer/ComponentRegistry.kt` (`cardsFamilyEntries +
chipsFamilyEntries + sheetsFamilyEntries + buttonsFabFamilyEntries + pickersFamilyEntries +
feedbackFamilyEntries + emptyStateFamilyEntries + progressFamilyEntries +
tactileFoundationFamilyEntries + voiceCommandFamilyEntries`), which is the single source of truth
and the CATALOG drift guard.

## Surface at a glance

| Family | Registered composables | What it is |
|--------|-----------------------|------------|
| 1. Cards | 11 | Card faces + card-face sub-rows for the five card archetypes |
| 2. Chips | 5 | Tag/selection chips and the bar that lays them out (with an optional filter/sort chrome mode) |
| 3. Sheets | 18 | Bottom-sheet / editor / popup content surfaces and their scaffolding |
| 4. Buttons / FAB | 4 | The expandable create-FAB and dynamic action buttons |
| 5. Pickers | 6 | Accent-color, icon, crop, and segmented-option pickers |
| 6. Feedback | 3 | Confirmation dialog, the Undo Center, and the attention-cue glyph |
| 7. Empty-state | 1 | The shared empty-state surface |
| 8. Progress / Metrics | 4 | Determinate ring / count-up / hero-card primitives for at-a-glance stat display |
| 9. Tactile Foundation | 4 | Elevation ladder, Space Grotesk display ramp, gradient/tint accent surfaces, and the Heat relatedness ramp |
| 10. Voice Command | 5 | Provider/API-key, model, and command-approach settings cards, plus the outcome/undo/confirmation sheet and the tap-to-clarify bar |

**61 registered public composables** across the ten families, plus **5 intentionally-unregistered**
structural sub-parts (see the end of this doc) = **66 public composables total**. Every component
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
| `CardBase` | Structural card shell every card face wraps (reveal-confirm swipe, tap, long-press, tactile depth) | `openRowState: MutableState<AnchoredDraggableState<SwipeAnchor>?>, onDeleteClick, onEditClick, onClick, onLongClick, accent: Color?, tactileDepth: Boolean = false, …` content slots |
| `CardTypeChip` | Small accent-tinted badge icon rendered on a card face | `accent: Color?, icon: @Composable () -> Unit` |
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
| `PresetChip` | Selectable preset/filter chip with an optional supporting label | `label: String, onClick: () -> Unit, supportingLabel: String? = null, enabled = true, isSelected = false, contentDescription: String?` (an overload without `contentDescription` preserves the pre-164-03 signature) |
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
| `RecordingBottomSheetContent` | Voice-recording sheet body (waveform + timer) | `uiState: RecordingSheetUiState, elapsedSeconds` — `showTagColors = false` opt-in (v2.3.0): forwards into its TITLE-state `TagChipEditorContent` (and, through it, its launched picker); default unchanged |
| `AlbumSourcePickerSheet` | Camera-vs-gallery source picker sheet | `onNavigateToCamera, onNavigateToGallery, …` |
| `AlbumTitleConfirmSheet` | Album-title confirmation sheet | title state + confirm callback |
| `VoiceRenameTagsSheet` | Voice-note rename + tags sheet | `defaultTitle, onSave(title), …` |
| `TagPickerSheet` | Full tag-picker sheet (host + content) | `existingTagIds: Set<String>, allTags: List<TagChipUiModel>, onDone(List<String>)` — `showTagColors = false` opt-in (v2.3.0): unselected picker chips fill with `TagChipUiModel.color` as-is; selection wins; default unchanged |
| `TagPickerSheetContent` | Tag-picker body (no host) | `allTags, selection, onDone` — `showTagColors = false` opt-in (v2.3.0): unselected picker chips fill with `TagChipUiModel.color` as-is; selection wins; default unchanged |
| `TagChipEditorContent` | Inline tag-chip editor body | `currentTags: List<TagChipUiModel>, isLastTag, …` — applied chips also support double-tap-to-remove, routed through the same undo-backed `onRemoveTag` callback the long-press menu's "Remove from this card" item uses; `showTagColors = false` opt-in (v2.3.0): current-tag strip chips fill with `TagChipUiModel.color` as-is (resting-chip look); selection wins; the flag also reaches the launched picker; default unchanged |
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
| `MicButton` | Tap-to-talk mic button with single-owner press/release gesture handling | `isListening: Boolean, enabled: Boolean = true, onTap: () -> Unit, onDisabledTap: () -> Unit = {}, disabledDescription, tapToTalkDescription, listeningDescription` |

## 5. Pickers

| Composable | Purpose | Key parameters |
|-----------|---------|----------------|
| `AccentColorPicker` | Accent-color swatch picker | `selectedColor: Long, onColorSelected: (Long) -> Unit` |
| `DateTimePicker` | Date/time field pair with Material3 calendar/clock panels | `selectedDate: LocalDate?, onDateSelected, selectedTime: LocalTime?, onTimeSelected, showDate = true, showTime, minDate, is24Hour, enabled, testTag` |
| `PlaceMapPicker` | Map-based place/radius picker (search, saved places, current-location, pin + radius) | `pinLatitude: Double?, pinLongitude: Double?, onPinChange, radiusMeters: Float, onRadiusChange, minRadiusMeters, maxRadiusMeters, defaultRadiusMeters, userAgent: String, …` search/saved-places callbacks — the hub never geocodes; the caller resolves and reports back |
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

## 10. Voice Command

Five data-and-callback composables for a voice-command settings and outcome surface: the provider /
API-key card, the model dropdown, the command-approach ladder, the outcome / undo / confirmation
sheet and the tap-to-clarify bar. They are domain-agnostic: the hub localizes nothing, so the
user-visible literals are defaulted, caller-overridable parameters or model fields whose default is
the English text, and callers pass already-localized strings, with two documented residuals that stay
English and are not yet overridable: the segmented toggles' "selected" / "not selected" state words
(`ApproachLadderCard`), and the `ProviderKeyCard` key field's reveal / hide toggle and clear control,
announced as "Show key", "Hide key" and "Clear text". Props that default to `null` hide their
control entirely, with no reserved space (conditional-render-no-dead-space).

| Composable | Purpose | Key parameters |
|-----------|---------|----------------|
| `ProviderKeyCard` | Provider dropdown plus a hoisted API-key field; the card stores, validates and transmits nothing; the field is masked by default with a reveal toggle, and every edit is trimmed of leading/trailing whitespace before `onKeyChange` (pure formatting, no validation) | `providers, selectedProviderId, onProviderSelected, keyValue, onKeyChange, keyState, keyLabel, modifier`, `emptyProvidersReason = "No providers configured yet"`, `providerLabel = "Provider"` |
| `ModelSelectCard` | Model dropdown over the caller's model list, with a disabled reason caption when the list is empty | `models, selectedModelId, onModelSelected, emptyReason, modifier`, `modelLabel = "Model"` |
| `ApproachLadderCard` | Command-approach settings card: the caller's tier ladder in list order, an optional offline-only toggle, an optional max-tier cap and an optional router toggle | `ladder, offlineOnly, onOfflineOnlyChange, maxTierId, onMaxTierChange, modifier`, `unavailableLabel = "Unavailable"`, `cappedLabel = "Capped"`, `needsNetworkLabel = "Needs network"`, `onlineLabel = "Online"`, `offlineOnlyLabel = "Offline only"`, `router: Boolean? = null`, `onRouterChange: ((Boolean) -> Unit)? = null`, `routerOnLabel = "Router on"`, `routerOffLabel = "Router off"` |
| `OutcomeSheet` | Bottom sheet rendering a command outcome (success, failure or needs-confirmation) with its undo affordances | `outcome: VoiceOutcomeUiState, onDismissRequest, modifier` — the overridable strings live on the models in the table below |
| `ClarificationBar` | Tap-to-clarify bar: a question, option chips in exact list order and a dismiss control; renders nothing for an empty option list | `question, options, onSelect, onDismiss, modifier`, `dismissLabel = "Dismiss"` |

**`ApproachLadderCard` behaviour (v2.5.0):**

- **Router toggle.** Rendered below the offline-only toggle. It emits the tapped segment's target
  value (`true` means ON), never a negation of the current state, so re-tapping the selected segment
  re-emits its own value. A `null` `router` / `onRouterChange` pair hides it with no reserved space.
  Exactly one of the pair non-null throws through `require()`, exactly like the `offlineOnly` /
  `onOfflineOnlyChange` pair. It is a policy toggle, not per-rung navigation: it never changes rung
  order or state, and tapping a rung still selects the cap.
- **Rung accessibility (VA11Y-01, no signature change).** When the cap is selectable (`maxTierId` and
  `onMaxTierChange` non-null) the rung rows are exposed as radio buttons; the rung whose id equals
  `maxTierId` is announced as selected and a stale id selects none. The minimum interactive size
  applies only in that case, so cap-less ladders stay compact. The segmented toggles' state words
  "selected" / "not selected" are announced by `SegmentedOptionSelector` and stay English, a
  documented residual the hub does not localize.

**Appended model fields (v2.5.0).** Each is defaulted and appended as the last field; see
"Voice label fragments" below for how `OutcomeSheet` joins the label fields.

| Model | Appended field | Behaviour |
|-------|----------------|-----------|
| `HandledByUiModel` | `escalationsLabel = "Escalations:"` | Prefix of the escalation caption, rendered as `"<escalationsLabel> <escalationCount>"`; plain text. |
| `UndoRowUiModel` | `undoneLabel = "Undone"` | Text of a row whose state is `UndoRowState.Undone`; plain text. |
| `UndoRefusedUiModel` | `refusedPrefix = "Couldn't undo:"` and `changedSinceSuffix = "changed since"` | Lead-in rendered before `reason`, and the fragment rendered after `changedItem` when it is non-null; plain text, no templating. The apostrophe in the default is an ASCII apostrophe. |
| `ProposedItemUiModel` | `removeContentDescription = "Remove"` | Accessibility description of the row's remove control; set it on every item that supplies `onRemove`. Its `toString` deliberately omits this label along with the other display fields. |
| `FailureActionUiModel` | `role = ActionButtonDefaults.ActionButtonRole.Neutral` | Button role of the failure action (VFAIL-01). Reuses the existing public enum, no new type. The explicit two-argument `(label, onClick)` constructor keeps the v2.4.0 shapes working with role Neutral. |
| `VoiceOutcomeUiState.Failure` | `body: (@Composable () -> Unit)? = null` and `semanticsPrefix: String? = null` | `body` (VFAIL-02) renders inside the error surface after the handled-by row and before the action; `null` renders no node and no space; equality is by lambda reference. `semanticsPrefix` (VFAIL-03) is prepended to the reason in the accessibility announcement, joined by a single ASCII space so any punctuation belongs to the prefix (the prefix is trimmed before joining); `null` or blank leaves the announcement unchanged; plain text, no templating. Static UI copy only, never sensitive text, because the generated `toString` of this class includes it. |

---

## Intentionally-unregistered sub-parts (5)

Public composables that are **not** standalone catalog tiles (structural sub-parts, exercised
indirectly), tracked in `ComponentRegistry.INTENTIONALLY_UNREGISTERED`:

| Composable | Why unregistered |
|-----------|------------------|
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

**Appending a defaulted parameter (v2.3.0's `showTagColors`) — the precise compatibility scope.**
v2.3.0 appended `showTagColors: Boolean = false` as the new LAST parameter on `TagPickerSheet`,
`TagPickerSheetContent`, `TagChipEditorContent` and `RecordingBottomSheetContent`, each of which
already ended with a function-typed parameter (`onSortModeChange` on the first three,
`onDeleteTag` on `TagChipEditorContent`).

- Appending a parameter with a default value is **source-compatible** for named-argument,
  positional and fully-parenthesized call sites — pinned by the committed
  `ShowTagColorsSourceCompatTest` compile fixture — and Metalava `apiCheck` passes.
- It is **NOT source-compatible** for a **trailing-lambda** call of the callback that used to be
  last: `onSortModeChange` on `TagPickerSheet` / `TagPickerSheetContent` /
  `RecordingBottomSheetContent`, and `onDeleteTag` on `TagChipEditorContent`. Kotlin binds a
  trailing lambda to the LAST parameter — now the `Boolean` — so such a caller must pass that
  callback as a **named argument** (e.g. `onSortModeChange = { … }`) when moving to v2.3.0.
  Metalava's `api.txt` check does not cover call syntax. No known call site (SecondBrain,
  CalTracker, hub-internal) used that form as of this release (swept 2026-09-28), and v2.2.0's own
  `AppChip` `containerColorOverride` addition had the identical shape.
- It is **not binary-compatible** in general: the compiled JVM signature of a Kotlin function
  changes when a parameter is added, and for a `@Composable`, the generated default/changed-mask
  parameters change too — so a consumer artifact compiled against the previous tag can fail at
  link time if run against the new AAR without recompiling.
- This ecosystem relies on every consumer rebuilding from an **immutable tag** on each repin
  (never mixing prebuilt binaries across tags), which is why such an addition ships as a minor
  version bump.

**Appending label fields to the voice models and composables (v2.5.0, VI18N-01..04) — the precise
compatibility scope.** v2.5.0 appended caller-localizable `String` label parameters (English
defaults, byte-identical to the literals they replaced) as the LAST parameter of `ProviderKeyCard`,
`ModelSelectCard`, `ClarificationBar` and `ApproachLadderCard`, and as appended constructor fields on
`HandledByUiModel`, `ProposedItemUiModel`, `UndoRefusedUiModel` and `UndoRowUiModel` (each keeps its
shipped constructor arity via `@JvmOverloads` plus a hand-written old-arity `copy`).

- It is **source-compatible**: every v2.4.x call shape (positional, named, partial, `copy(...)`,
  destructuring) still compiles, except the `ProposedItemUiModel` trailing-lambda call below —
  pinned by the committed `VoiceI18nSourceCompatTest` fixture for the other shapes — and
  Metalava `apiCheck` passes (it tracks only the public, non-synthetic surface).
- It is **binary-compatible with v2.4.x** (binary-compat fix, 2026-10-05). Every v2.4.1 public
  JVM descriptor is still present in the v2.5.0 AAR. The four composables keep a
  `@Deprecated(level = HIDDEN)` overload with their exact v2.4.1 signature that delegates to the
  current one. The six affected data classes (`HandledByUiModel`, `ProposedItemUiModel`,
  `UndoRefusedUiModel`, `UndoRowUiModel`, `FailureActionUiModel`, `VoiceOutcomeUiState.Failure`) keep
  hidden members with the exact v2.4.1 synthetic descriptors: the `(…, int, DefaultConstructorMarker)`
  default-argument constructor and `copy$default`. Kotlin callers built against v2.4.x link to
  those synthetics. The proof is a `javap -public -s` diff of the release AAR against the v2.4.1
  JitPack AAR. It showed zero missing descriptors when the binary-compat fix landed; it is re-proven
  at the v2.5.0 cut and is not asserted here as freshly verified for any later addition.
- `FailureActionUiModel("l") { … }` (the v2.4.0 trailing-lambda shape) compiles again through an
  explicit `(label, onClick)` constructor.
- It is **NOT source-compatible** for a **trailing-lambda** call of `ProposedItemUiModel`. The v2.4.x
  call `ProposedItemUiModel(id, title) { … }` bound its lambda to `trailingContent`, which was the
  last constructor parameter. v2.5.0 appended `removeContentDescription: String` after it, so Kotlin
  now binds a trailing lambda to that `String` and the call no longer compiles. Pass the slot as the
  named argument `trailingContent = { … }` when moving to v2.5.0. Metalava's `api.txt` check does not
  cover call syntax. It is a source-level break only: v2.4.x-compiled binaries still link, per the
  binary-compatibility bullet above. A sweep of the known call sites on 2026-10-05 (SecondBrain,
  CalTracker, hub-internal) found only the named form, so none breaks. It is the same hazard class as
  the v2.3.0 `showTagColors` addition. Every other appended-last addition is unaffected because its
  former last parameter was not a function type, and `FailureActionUiModel` (whose former last
  parameter `onClick` is a function type) is covered by its explicit two-argument constructor.
- Consumers still rebuild from an immutable tag on every repin (`ECOSYSTEM.md` §7). Binary
  compatibility is a safety net for prebuilt dependents, not a reason to skip the rebuild.

**Appending the Failure enrichment and router toggle (v2.5.0, VFAIL-01..03, VAPPR-04) — the precise
compatibility scope.** v2.5.0 appended `role` to `FailureActionUiModel`, `body` and `semanticsPrefix`
to `VoiceOutcomeUiState.Failure`, and `router`, `onRouterChange`, `routerOnLabel` and `routerOffLabel`
as the last four parameters of `ApproachLadderCard`. Every one is defaulted (see section 10 for the
defaults and behaviour).

- It is **source-compatible** for positional, named, partial, `copy(...)` and destructuring shapes.
  `FailureActionUiModel(label, onClick)` and the trailing-lambda `FailureActionUiModel("l") { … }`
  bind to `onClick` with `role` Neutral through the explicit two-argument constructor, and a
  hand-written legacy-arity `copy` on `FailureActionUiModel` and on `Failure` keeps the old `copy`
  shapes working.
- **Binary compatibility** follows the mechanism described above: the hidden v2.4.1
  `ApproachLadderCard` overload passes `router = null`, and the `FailureActionUiModel` and `Failure`
  synthetics are among the six restored. That claim rests on the same `javap` proof stated in the
  binary-compatibility bullet above, with the same status: re-proven at the v2.5.0 cut, not asserted
  here as freshly verified.
- The rung accessibility change (VA11Y-01) is behaviour only, with no signature change; it is
  documented in section 10.
- `semanticsPrefix` is static UI copy only, never sensitive text, because the generated `toString`
  of `Failure` includes it.

**Voice label fragments — how the sheet joins them (v2.5.0, VI18N-04).** The label fields are plain
text fragments; `OutcomeSheet` composes them with fixed separators: a single ASCII space between a
label and the value that follows it (`"<refusedPrefix> <reason>"`, `"<escalationsLabel> <count>"`),
and a literal `", "` before the optional changed-item fragment
(`", <changedItem> <changedSinceSuffix>"`). Consequences for callers localizing:

- **Punctuation belongs to the label.** The English defaults carry their colon (`"Couldn't undo:"`,
  `"Escalations:"`); a language that needs a different or full-width colon supplies it inside the
  label (note the separating space is still inserted after it).
- **Do not pass a blank label to mean "no lead-in"** — it renders a leading space. Fold the text
  into the value instead.
- The `", "` list separator and the label-then-value order are fixed. A language that needs a
  different order or a non-ASCII separator should fold the item into `reason` and pass
  `changedItem = null` (see `UndoRefusedUiModel`).

### Compatibility rule (personal library; source compatibility only)

This is Yahir's personal library. Its only consumers are his own apps, and each recompiles from an
immutable tag on every repin. So the bar is **source compatibility** for those consumers:

- New parameters and properties are appended with **defaults**, so existing call sites keep
  compiling.
- **Trailing lambdas stay last.** Never append a parameter after a function-typed parameter that
  callers may pass as a trailing lambda. If a new parameter must go there, document the caveat
  (pass the lambda by name), as with v2.3.0's `showTagColors` and v2.5.0's `ProposedItemUiModel`.
- **Metalava `apiCheck`** is the per-commit source-level gate. Run `./gradlew apiDump` and commit
  `api.txt` with every public API change.
- **Binary compatibility is NOT required.** Do not add new `@Deprecated(HIDDEN)` overloads or new
  variant-K synthetic members for old signatures. The shims that shipped in v2.5.0 (four hidden
  v2.4.1 composable overloads and the variant-K members on six data classes) stay as they are. Do
  not remove them.
- `tools/verify-binary-abi.sh <previous-tag>` (a `javap` AAR descriptor diff) is an **optional,
  informational** check, not a release gate. A missing descriptor is expected after a deliberate
  signature change and does not block a tag. See `tools/README-api-guard.md`.
- A deliberate source break is fine when Yahir's consumers are updated in the same repin. Note it
  in this file and bump the version accordingly.
