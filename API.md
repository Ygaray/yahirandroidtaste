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
| 1. Cards | 13 | Card faces + card-face sub-rows for the five card archetypes, plus the tone card and the Receipt row (v2.6.0) |
| 2. Chips | 6 | Tag/selection chips and the bar that lays them out (with an optional filter/sort chrome mode), plus the outlined pill chip (v2.6.0) |
| 3. Sheets | 18 | Bottom-sheet / editor / popup content surfaces and their scaffolding |
| 4. Buttons / FAB | 5 | The expandable create-FAB, dynamic action buttons and the round outlined icon button (v2.6.0) |
| 5. Pickers | 6 | Accent-color, icon, crop, and segmented-option pickers |
| 6. Feedback | 3 | Confirmation dialog, the Undo Center (with group rows since v2.6.0), and the attention-cue glyph |
| 7. Empty-state | 1 | The shared empty-state surface |
| 8. Progress / Metrics | 5 | Determinate ring / count-up / hero-card primitives for at-a-glance stat display, plus the stacked multi-segment bar (v2.6.0) |
| 9. Tactile Foundation | 4 | Elevation ladder, Space Grotesk display ramp, gradient/tint accent surfaces, and the Heat relatedness ramp |
| 10. Voice Command | 5 | Provider/API-key (with the v2.6.0 key lifecycle), model, and command-approach settings cards, plus the outcome/undo/confirmation sheet (Classic and Receipt styles) and the tap-to-clarify bar |

**66 registered public composables** across the ten families, plus **5 intentionally-unregistered**
structural sub-parts (see the end of this doc) = **71 public composables total**. Every component
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
| `ToneCard` (v2.6.0) | Tinted card: a surface filled with the tone container color and content in the tone content color (12dp corners, 4dp between children) | `tone: ToneColors, modifier, content: @Composable ColumnScope.() -> Unit` — stable tag `tone_card`; static UI copy only |
| `ReceiptRow` (v2.6.0) | Stateless list row that reads as one accessible button: title, subtitle, supporting line and attention cue, an end-aligned value with caption, an optional chevron or trailing action and optional expanded content. Value wins over the title when they collide (the title ellipsizes) | `title: String, modifier, subtitle: String? = null, subtitleTone = ContentTone.Muted, supportingContent: (@Composable () -> Unit)? = null, value: String? = null, valueCaption: String? = null, valueTone = ContentTone.Default, titleTone = ContentTone.Default, attentionCue: String? = null, warnTone: ToneColors = ToneDefaults.warn(), expanded: Boolean = false, onClick: (() -> Unit)? = null, onClickLabel: String? = null, expandedStateLabel = "Expanded", collapsedStateLabel = "Collapsed", trailingAction: (@Composable () -> Unit)? = null, dimmed = false, showDivider = true, expandedContent: (@Composable () -> Unit)? = null` — the row holds no state; keep `expanded` (and any single-open accordion rule) in the caller |

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
| `PillChip` (v2.6.0) | Fully rounded outlined chip with a single-line ellipsized label and optional leading / trailing icons; 32dp tall at the default font scale (a minimum, it grows with the font), 48dp touch region. `AppChip` and `PresetChip` are unchanged | `label: String, onClick, modifier, leadingIcon: ImageVector? = null, trailingIcon: ImageVector? = null, enabled = true, contentDescription: String? = null` — stable tag `pill_chip` |

## 3. Sheets

Bottom-sheet / editor / popup **content** surfaces (the caller owns the `ModalBottomSheet` host;
these render its body) plus the shared scaffolding and editor rows.

| Composable | Purpose | Key parameters |
|-----------|---------|----------------|
| `SheetScaffold` | Shared modal-bottom-sheet scaffold (drag handle, dismissal) | `onDismissRequest, modifier, sheetState: SheetState = rememberModalBottomSheetState(), dragHandle, contentWindowInsets, shape: Shape = BottomSheetDefaults.ExpandedShape` (v2.6.0, inserted before the trailing `content` lambda; the default is the unchanged M3 28dp shape), `content: @Composable ColumnScope.() -> Unit` |
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
| `DynamicActionButton` | Semantically-colored dynamic action button (destructive/save/neutral; disabled until dirty) | `label, role: ActionButtonDefaults.ActionButtonRole, onClick, enabled = true, modifier, emphasis: ActionButtonEmphasis? = null, icon: ImageVector? = null` — `emphasis = null` with `icon = null` is the unchanged role-to-widget mapping of earlier releases; a non-null `emphasis` (v2.6.0) picks Filled, Tonal, Text or TextSmall explicitly |
| `MicButton` | Tap-to-talk mic button with single-owner press/release gesture handling | `isListening: Boolean, enabled: Boolean = true, onTap: () -> Unit, onDisabledTap: () -> Unit = {}, disabledDescription, tapToTalkDescription, listeningDescription` |
| `RoundIconButton` (v2.6.0) | Round outlined icon button: transparent circle, 1dp `outlineVariant` border, 20dp icon; the drawn circle is `size` but the touch region and layout reserve are never below 48dp | `icon: ImageVector, contentDescription: String, onClick, modifier, enabled = true, size: Dp = 40.dp` — `contentDescription` is required static UI copy; stable tag `round_icon_button` |

## 5. Pickers

| Composable | Purpose | Key parameters |
|-----------|---------|----------------|
| `AccentColorPicker` | Accent-color swatch picker | `selectedColor: Long, onColorSelected: (Long) -> Unit` |
| `DateTimePicker` | Date/time field pair with Material3 calendar/clock panels | `selectedDate: LocalDate?, onDateSelected, selectedTime: LocalTime?, onTimeSelected, showDate = true, showTime, minDate, is24Hour, enabled, testTag` |
| `PlaceMapPicker` | Map-based place/radius picker (search, saved places, current-location, pin + radius) | `pinLatitude: Double?, pinLongitude: Double?, onPinChange, radiusMeters: Float, onRadiusChange, minRadiusMeters, maxRadiusMeters, defaultRadiusMeters, userAgent: String, …` search/saved-places callbacks — the hub never geocodes; the caller resolves and reports back |
| `IconPickerGrid` | Module/tag icon grid picker | `selectedIcon: String, onIconSelected: (String) -> Unit` — public parameters unchanged; the grid includes a built-in live case-insensitive name-substring search field with an empty-state when nothing matches |
| `CropOverlay` | Crop-rectangle overlay for image editing | `bitmapWidth, bitmapHeight, aspectRatio: Float?, …` |
| `SegmentedOptionSelector` | Two-option segmented toggle with an always-visible disabled+reason affordance; labels too long for their half wrap to at most two lines, then ellipsize, staying inside the segment (never under the selected check icon); both segments keep equal height (v2.5.1) | `selectedIndex: Int, options: List<String>, onSelect: (Int) -> Unit, modifier, enabled: Boolean, disabledReason: String?`, `disabledOptions: Set<Int> = emptySet()` (v2.6.0: a disabled segment ignores taps and is announced as not selected), `style: SegmentedOptionStyle = SegmentedOptionStyle.Material` (v2.6.0; `Pill` is single-line with an ellipsis, so a long localized label truncates there where Material wraps to two lines) |

## 6. Feedback

| Composable | Purpose | Key parameters |
|-----------|---------|----------------|
| `ConfirmationDialog` | Standard confirm/cancel dialog | `title, body, onDismissRequest, …` confirm/dismiss callbacks |
| `UndoCenterScreen` | The Undo Center — a history of undoable actions (backed by `UndoHistoryStore`, see `INTEGRATION.md`) | `entries: List<UndoHistoryEntry>, onNavigateBack, onUndo: (String) -> Unit`, `groups: List<UndoGroupRowUiModel> = emptyList()` and `onUndoGroup: ((String) -> Unit)? = null` (v2.6.0, VUNDO-02: a group row with label, member count, status and "Undo all" above its indented members), then the trailing `onClear: () -> Unit` — a positional four-argument call must now name `onClear` or pass it as the trailing lambda (see the v2.6.0 compatibility scope below) |
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
| `StackedProgressBar` (v2.6.0) | Decorative multi-segment horizontal bar on a pill-shaped track (default 6dp tall); the caller describes the values in adjacent text. Negative or `NaN` fractions draw as zero and the running total is capped at 1 | `segments: List<BarSegment>, trackColor: Color` (required on purpose: each consumer owns its contrast rule), `modifier, height: Dp = 6.dp` — stable tag `stacked_progress_bar`; needs a bounded width |

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
| `ProviderKeyCard` | Provider dropdown plus a hoisted API-key field; the card stores, validates and transmits nothing; the field is masked by default with a reveal toggle, and every edit is trimmed of leading/trailing whitespace before `onKeyChange` (pure formatting, no validation). Since v2.6.0 it can own the whole key lifecycle: a provider with a saved key shows a summary (check icon plus the fingerprint) with Update and Clear instead of the field | `providers, selectedProviderId, onProviderSelected, keyValue, onKeyChange, keyState, keyLabel, modifier`, `emptyProvidersReason = "No providers configured yet"`, `providerLabel = "Provider"`, then the v2.6.0 key-lifecycle parameters, all off by default: `savedKeys: Map<String, SavedKeyUiModel> = emptyMap()`, `editingSavedKey: Boolean = false`, `actions: ProviderKeyActions? = null`, `labels: ProviderKeyLabels = ProviderKeyDefaults.labels()` — see section 11 |
| `ModelSelectCard` | Model dropdown over the caller's model list, with a disabled reason caption when the list is empty | `models, selectedModelId, onModelSelected, emptyReason, modifier`, `modelLabel = "Model"` |
| `ApproachLadderCard` | Command-approach settings card: the caller's tier ladder in list order, an optional offline-only toggle, an optional max-tier cap and an optional router toggle | `ladder, offlineOnly, onOfflineOnlyChange, maxTierId, onMaxTierChange, modifier`, `unavailableLabel = "Unavailable"`, `cappedLabel = "Capped"`, `needsNetworkLabel = "Needs network"`, `onlineLabel = "Online"`, `offlineOnlyLabel = "Offline only"`, `router: Boolean? = null`, `onRouterChange: ((Boolean) -> Unit)? = null`, `routerOnLabel = "Router on"`, `routerOffLabel = "Router off"` |
| `OutcomeSheet` | Bottom sheet rendering a command outcome (success, failure or needs-confirmation) with its undo affordances | `outcome: VoiceOutcomeUiState, onDismissRequest, modifier`, `style: OutcomeSheetStyle = OutcomeSheetStyle.Classic` and `colors: OutcomeSheetColors = OutcomeSheetDefaults.colors()` (v2.6.0; `Classic` is the unchanged earlier look, `Receipt` is the badge, title row, pinned action bar and `ReceiptRow` look) — the overridable strings live on the models in the table below |
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
- **Long labels (v2.5.1, layout only, no signature change).** The unavailable / capped /
  needs-network affordance labels get whatever row width the rung name does not need. The name
  always keeps its full width or 40% of the row, whichever is smaller, so the affordances never
  take more than 60%. Each affordance wraps at word boundaries to at most two lines, then
  ellipsizes, and they stack right-aligned when they don't fit side by side. Short labels
  (including the English defaults) render exactly as in v2.5.0: all affordances on one line next
  to the name. The offline-only and router toggles get the
  `SegmentedOptionSelector` long-label behaviour (section 5): wrap to at most two lines inside the
  segment, equal segment heights.

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

## 11. Tone foundation and Receipt types (v2.6.0)

v2.6.0 adds five composables (`ToneCard`, `ReceiptRow`, `PillChip`, `RoundIconButton`,
`StackedProgressBar`, listed in their family tables above) and the non-composable types below.
Everything is opt-in and defaulted, and the named and trailing-lambda call shapes of v2.5.1 still
compile. Three positional shapes do not: `UndoCenterScreen` with four positional arguments,
`NeedsConfirmation` constructed or destructured positionally (and its `copy`), and `SheetScaffold`
with `content` passed as the sixth positional argument instead of as the trailing lambda. Each is a
deliberate, documented source break with a one-line fix, listed in "Adding / changing components"
below. `OutcomeSheet` keeps rendering the Classic look unless a caller passes
`OutcomeSheetStyle.Receipt`. Wiring examples are in `INTEGRATION.md` section 5b.

### New types

| Type | Package | What it is |
|------|---------|------------|
| `ToneColors` | `theme` | `@Immutable class ToneColors(container: Color, content: Color)`; equality by value. A pair of colors a component paints a tinted surface with. |
| `ToneDefaults` | `theme` | `@Composable error()` is `errorContainer` over `error`; `@Composable warn()` is `tertiaryContainer` over `onTertiaryContainer`. Under dynamic color (Material You) these roles come from the wallpaper and are unchecked, so pass your own `ToneColors` when you need a guaranteed amber or a guaranteed contrast. |
| `ContentTone` | `theme` | `Default`, `Muted`, `Warn`, `Error`: the semantic tone of a piece of text. `Default` is `onSurface`, `Muted` is `onSurfaceVariant`, `Warn` is the `warnTone` content color, `Error` resolves through `ToneDefaults.error()`. Used by `ReceiptRow`. |
| `ActionButtonEmphasis` | `component` | `Filled`, `Tonal`, `Text`, `TextSmall`: the explicit widget for `DynamicActionButton`. `null` keeps the role-to-widget mapping of earlier releases. |
| `BarSegment` | `component` | `@Immutable class BarSegment(fraction: Float, color: Color)`: one segment of a `StackedProgressBar`. Out-of-range fractions are clamped by the bar, not by this class. |
| `OutcomeSheetStyle` | `component` | `Classic` (the default and the unchanged look) and `Receipt`. A new enum, so no existing `when` over another enum gains a constant. |
| `OutcomeSheetColors` | `component` | `@Immutable class OutcomeSheetColors(warnTone: ToneColors, errorTone: ToneColors)`: the tones both styles paint warnings and errors with. |
| `OutcomeSheetDefaults` | `component` | `@Composable colors(warnTone = ToneDefaults.warn(), errorTone = ToneDefaults.error())` and the `@Composable ReceiptTitleStyle` getter (`titleLarge` at 18sp / 24sp SemiBold). |
| `OutcomeBadge` | `model` | `Success`, `Reverted`, `Error`, `Warn`: the status badge of the Receipt title row. `Reverted` draws the RTL-aware `Icons.AutoMirrored.Filled.Undo`. The badge is decorative and has no text alternative, so the title or summary text must itself state the status (for example "Reverted: 2 entries"). |
| `FailureTone` | `model` | `Error` (default) and `Warn`. Receipt paints a warn failure on `colors.warnTone`; Classic also honors `Warn` (it paints `colors.warnTone` instead of the error container), and its default `Error` keeps the earlier pixels. |
| `SheetActionUiModel` | `model` | `@Immutable data class SheetActionUiModel(label, onClick, emphasis: ActionButtonEmphasis, enabled = true, icon: ImageVector? = null, role = ActionButtonRole.Neutral, activeWhileInFlight = false)`. An action is clickable when `enabled && (!inFlight || activeWhileInFlight)`; `enabled = false` always wins, `activeWhileInFlight = true` only exempts the in-flight lock (for example a Cancel). |
| `SegmentedOptionStyle` | `component` | `Material` (default, the v2.5.1 layout with two-line wrapping) and `Pill`. |
| `SavedKeyUiModel` | `model` | `@Immutable data class SavedKeyUiModel(fingerprint: String)`. Holds a short display fingerprint such as `key-…c3d4` (the text key, a dash, an ellipsis and the last four characters), never a raw key: the card renders only this string. |
| `ProviderKeyActions` | `model` | `@Immutable data class ProviderKeyActions(onSave, saveEnabled = true, onUpdate: (() -> Unit)? = null, onClear: (() -> Unit)? = null, onCancelEdit: (() -> Unit)? = null, inFlight = false)`. A `null` callback hides that button; `inFlight = true` disables every action and the provider dropdown. |
| `ProviderKeyLabels` | `model` | `@Immutable data class ProviderKeyLabels(save = "Save", update = "Update", clear = "Clear", cancel = "Cancel", savedPrefix = "Saved", savedMarker = "saved")`. The summary renders `"<savedPrefix> · <fingerprint>"`; `savedMarker` is appended to providers that have a saved key in the dropdown. |
| `ProviderKeyDefaults` | `component` | `labels(save, update, clear, cancel, savedPrefix, savedMarker)`: builds a `ProviderKeyLabels`, every argument optional with the English default. |
| `UndoGroupRowUiModel` | `feedback` | `@Immutable data class UndoGroupRowUiModel(groupId, label, memberIds: List<String>, status: UndoGroupStatus, undoAllLabel = "Undo all")`: the caller's projection of one `UndoHistoryStore` group for `UndoCenterScreen`. The member count and the status words are English-only library text; only the button label is overridable. |

### Dimens token groups

The new components read their measurements from new nested groups of `Dimens` (all `Dp` properties,
none of the earlier tokens changed): `Dimens.ActionButton` (filled and text padding, icon size and gap),
`Dimens.OutcomeReceipt` (badge, title row, action bar, transcript, pill and dot measurements, corner
radius), `Dimens.PillChip` (height and horizontal padding), `Dimens.ReceiptRow` (vertical padding, gaps,
chevron size, value-column minimum width, cue padding), `Dimens.RoundIconButton` (`Size`, `IconSize`),
`Dimens.SegmentedPill` (height and label padding) and `Dimens.StackedProgressBar` (`Height`).

### Appended model fields (v2.6.0)

Each field is defaulted. `NeedsConfirmation` is the one exception to "appended last": its two fields
are inserted before the two required lambdas (see the compatibility scope below).

| Model | Added field(s) | Behaviour |
|-------|----------------|-----------|
| `VoiceOutcomeUiState.NeedsConfirmation` | `confirmEnabled: Boolean = true` and `titleActions: (@Composable () -> Unit)? = null`, inserted before `onConfirm` and `onCancel` | While `confirmEnabled` is `false` the confirm node is disabled and `onConfirm` never fires (both styles). `titleActions` is drawn at the end of the Receipt title row. `items` may be empty when `topLevelContent` renders the rows, for example `ReceiptRow`s whose accordion state the caller holds. |
| `VoiceOutcomeUiState.Success` | `badge: OutcomeBadge?`, `titleActions`, `startAction: SheetActionUiModel?`, `endAction: SheetActionUiModel?` | Receipt only; Classic ignores all four, so setting them on Classic keeps the earlier pixels. While `inFlight` every action except one with `activeWhileInFlight = true` is disabled. |
| `VoiceOutcomeUiState.Failure` | `title: String?`, `transcript: String?`, `tone: FailureTone = Error`, `tip: (@Composable () -> Unit)?`, `secondaryAction: SheetActionUiModel?`, `badge: OutcomeBadge?`, `undo: UndoAffordanceUiModel?` | Receipt renders them all. Classic ignores `title`, `transcript`, `tip`, `secondaryAction` and `badge`, honors `tone = Warn`, and renders `undo` through the same undo block as `Success` in both styles (XR-178-01 b), so a consumer can retire a local failure-undo body. |
| `FailureActionUiModel` | `icon: ImageVector? = null` | The Receipt failure button draws it; the explicit `(label, onClick)` constructor and the trailing-lambda form keep working. |
| `HandledByUiModel` | `prefixLabel: String = "Handled by"` and `onDevice: Boolean = false` | Receipt renders a pill: a 6dp dot (`primary`, or the warn tone content when `onDevice`), `"<prefixLabel> "`, the tier in SemiBold, then `" · <approach / provider / model>"`. Classic `HandledByRow` ignores both. |

### XR-178-01 mapping

- **a.** The refused / partial undo surface of `OutcomeSheet` is an Assertive live region
  (`LiveRegionMode.Assertive`, VXR-01), so TalkBack announces it when it appears. Semantics only.
- **b.** `VoiceOutcomeUiState.Failure.undo` (VXR-02) renders the grouped undo affordance on a failure.
- **c.** `UndoCenterScreen` renders group rows from `groups` and `onUndoGroup` (VUNDO-02).

## Stable test tags (documented API)

The test tags below are **documented, stable API** (D-09, Y-14): a consumer's UI tests may rely on
them and a rename is a breaking change. The library never sets `testTagsAsResourceId`; a consumer that
wants these tags to reach UI Automator sets `Modifier.semantics { testTagsAsResourceId = true }` on its
own root. A caller-supplied `Modifier.testTag(...)` wins over the library tag on the nodes whose KDoc
says so (`tone_card`, `pill_chip`, `round_icon_button`, `stacked_progress_bar`). Tags marked
"shared" are one tag on every row of a list: query them by index.

### OutcomeSheet (Classic and Receipt)

| Tag | Node |
|-----|------|
| `outcome_sheet_failure_surface` | The failure surface (the Classic card, the Receipt failure tone card) |
| `outcome_sheet_handled_by` | The handled-by line (the Classic row, the Receipt pill) |
| `outcome_sheet_action_button` | The failure action button in the Classic style |
| `outcome_sheet_undo_all` | The "Undo all" button of the undo affordance |
| `outcome_sheet_undo_refused` | The refused / partial undo surface (an Assertive live region) |
| `outcome_sheet_undo_row` | Each undo row (shared) |
| `outcome_sheet_batch_row` | Each batch result row (shared) |
| `outcome_sheet_confirmation_top_level_content` | The wrapper around `topLevelContent` |
| `outcome_sheet_confirmation_item` | Each proposed-item row (shared) |
| `outcome_sheet_confirmation_item_remove` | The remove control of a proposed item (shared) |
| `outcome_sheet_confirmation_cancel` | The Cancel button of a confirmation (both styles) |
| `outcome_sheet_confirmation_confirm` | The Confirm button of a confirmation (both styles) |

### OutcomeSheet (Receipt style)

| Tag | Node |
|-----|------|
| `outcome_sheet_title` | The Receipt title text |
| `outcome_sheet_badge` | The status badge at the start of the title row |
| `outcome_sheet_title_actions` | The wrapper around the `titleActions` slot |
| `outcome_sheet_transcript` | The transcript or reason text under the title |
| `outcome_sheet_action_bar` | The pinned action bar of a Failure |
| `outcome_sheet_action_bar_divider` | The 1dp divider above the action bar (a decoration node) |
| `outcome_sheet_action_start` | The start action (`startAction`, or a Failure `secondaryAction`) |
| `outcome_sheet_action_end` | The end action (`endAction`, or the Failure `action`) |
| `outcome_sheet_failure_tip` | The wrapper around the Failure `tip` slot |
| `outcome_sheet_handled_by_dot` | The 6dp status dot inside the handled-by pill (a decoration node) |

### ReceiptRow

| Tag | Node |
|-----|------|
| `receipt_row_summary` | The merged summary node: title, subtitle, value, caption and cue text; a button when `onClick` is set |
| `receipt_row_trailing_action` | The wrapper around `trailingAction` (a sibling of the summary) |
| `receipt_row_expanded_content` | The wrapper around `expandedContent` |

### Atoms

| Tag | Node |
|-----|------|
| `stacked_progress_bar` | The track node of `StackedProgressBar` |
| `tone_card` | The `ToneCard` surface |
| `round_icon_button` | The `RoundIconButton` node |
| `pill_chip` | The `PillChip` node (one clickable node) |

### ProviderKeyCard

| Tag | Node |
|-----|------|
| `provider_key_card_surface` | The card surface |
| `provider_key_card_dropdown` | The provider dropdown |
| `provider_key_card_key_field` | The key field (shown when no saved key summary replaces it) |
| `provider_key_saved_summary` | The saved-key summary (check icon plus fingerprint, one merged node) |
| `provider_key_saved_error` | The error line shown under the summary for an `Invalid` key state |
| `provider_key_action_save` | The Save button |
| `provider_key_action_update` | The Update button |
| `provider_key_action_clear` | The Clear button |
| `provider_key_action_cancel` | The Cancel button |

### Not part of the contract (test-only hooks)

These nodes carry tags for the library's own tests. Their KDoc declares them test-only, so they may
change or disappear in any release; do not depend on them.

| Tag | Node |
|-----|------|
| `receipt_row_chevron` | The expand chevron of a `ReceiptRow` |
| `receipt_row_cue` | The attention-cue pill of a `ReceiptRow` |
| `receipt_row_value_column` | The end-aligned value column of a `ReceiptRow` |
| `stacked_progress_bar_segment` | Each drawn segment of a `StackedProgressBar` |

Also outside the contract: the internal kebab-case tags of the Undo Center (`undo-row-<id>`,
`undo-group-header-<groupId>`, `undo-group-undo-all-<groupId>` and the preview peek tags) and every
`demo_` tag, which belongs to the Explorer gallery and is not library API. Tags of earlier families
(`approach_ladder_card_*`, `clarification_bar_*`, `model_select_card_*` and so on) keep their existing
behaviour and are not part of this list.

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

**Appending the Receipt, tone and key-lifecycle additions (v2.6.0) — the precise compatibility scope.**
v2.6.0 widened `DynamicActionButton`, `SheetScaffold`, `SegmentedOptionSelector`, `UndoCenterScreen`,
`ProviderKeyCard` and `OutcomeSheet`, and appended fields to `NeedsConfirmation`, `Success`, `Failure`,
`FailureActionUiModel` and `HandledByUiModel` (sections 4 to 6, 10 and 11 above). Every addition is
defaulted.

- It is **source-compatible only, with no new shims** (sketch P1): no new `@Deprecated(HIDDEN)`
  overloads, no hand-written old-arity `copy`, no hidden constructors and no `copy$default` members
  were added. The v2.5.0 shims stay. Binary compatibility is not claimed for any v2.6.0 signature;
  consumers recompile from an immutable tag on every repin.
- New composable parameters are appended after `modifier`. `SheetScaffold.shape` is inserted before the
  trailing `content` lambda, which stays last, so every trailing-content call and every call that
  names `content` still binds. The one shape that no longer compiles is passing `content` as the
  sixth positional argument (it now binds to `shape`): use the trailing lambda or `content = …`.
  `DynamicActionButton`, `SegmentedOptionSelector`, `OutcomeSheet` and `ProviderKeyCard` end in
  non-function parameters, so no former trailing lambda is displaced.
- `NeedsConfirmation` is the one data class whose new fields (`confirmEnabled`, `titleActions`) are
  inserted **before** its two required lambdas `onConfirm` and `onCancel` instead of appended. Both
  known consumers construct it with named arguments, which still compile. A positional construction
  (or a positional `copy(...)` that reaches the lambdas) does not, and destructuring shifts:
  `component10()` and `component11()` are now `confirmEnabled` and `titleActions`, and the two
  lambdas are `component12()` and `component13()`.
- The **positional four-argument call `UndoCenterScreen(entries, onNavigateBack, onUndo, onClear)` no
  longer compiles**: `groups` and `onUndoGroup` now sit before the trailing `onClear`, so the fourth
  positional argument binds to `groups`. Name it (`onClear = …`) or pass it as the trailing lambda.
  Both consumers pass named arguments, and every named and trailing-lambda form compiles. This is an
  accepted, owner-flagged source break and the one shape deliberately excluded from the canary.
- `FailureActionUiModel("l") { … }` and `FailureActionUiModel(label, onClick)` still bind to
  `onClick` through the explicit two-argument constructor; `icon` is appended after `role`.
- `OutcomeSheetStyle.Classic` renders the earlier code path. Classic ignores the Receipt-only fields
  (`badge`, `titleActions`, `startAction`, `endAction`, `title`, `transcript`, `tip`,
  `secondaryAction`, `prefixLabel`, `onDevice`, `icon`), honors `FailureTone.Warn`, `confirmEnabled`
  and `Failure.undo`, and keeps its tags.
- The group count and status words of an `UndoCenterScreen` group row ("3 items", "Undoable",
  "Resolved") are English-only library text; only `UndoGroupRowUiModel.undoAllLabel` is overridable.
- Guards that keep this true: `V251CallSiteCanaryTest` compiles verbatim v2.5.1 call shapes of the nine
  affected surfaces (consumer and Explorer call sites), `tools/verify-api-source-additive.sh <tag>`
  reviews `api.txt` against the baseline tag (no removed symbol, no changed required parameter, no
  parameter inserted before an existing one, none appended after a trailing lambda, no changed return
  or property type, no enum constant added or removed; the three positional breaks listed above are
  its reviewed, printed exceptions), and `CatalogDocsDriftGuardTest` keeps this file's counts, symbols
  and tags true.

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
