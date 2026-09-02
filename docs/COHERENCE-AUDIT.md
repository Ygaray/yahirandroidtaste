# Coherence Audit — yahirandroidtaste

This audit surfaces and dispositions overlap, near-duplicate-sibling, and altitude-mismatch
findings across the hub's 9 registered `ComponentRegistry` families (AUD-01). Every finding
raised below carries an explicit disposition — **unify**, **keep-with-rationale**, or **prune** —
with cited rationale. The final "Unify Work-Order" section aggregates every "unify" disposition
into the 1:1 work-order Phase 5 (Gardening) consumes.

## Scope & Method

- **Tier source (D-01):** every component's `PRIMITIVE`/`PATTERN` tier is consumed verbatim from
  Phase 1's ratified `ComponentRegistry.Entry.tier` values (one per entry, in each family's own
  `*FamilyScreen.kt` file) — never re-derived ad hoc. `docs/DESIGN-INTENT.md` states the two
  contracts and the decidable two-question D-03 litmus this audit cites (not re-pastes) whenever
  an altitude-mismatch candidate is evaluated.
- **Blast radius (D-02):** every "unify" finding below carries a read-only consumer blast-radius
  grep (`grep -rl "<ComponentName>" ~/Projects/SecondBrain/app/src ~/Projects/CalTracker_Android/app/src`)
  recorded as a per-repo file count. This grep is a **single-name lower bound** — a consumer could
  alias an import, so a zero count is a floor, not proof of zero usage.
- **Consumer pin skew:** CalTracker_Android is pinned to hub tag `v1.5.0` while SecondBrain is
  pinned to `v1.10.0` (a newer, later on-disk snapshot). When a "unify" finding's blast-radius grep
  returns zero CalTracker hits, this audit checks that component's family-screen file for a
  phase-provenance comment (e.g. Tactile Foundation entries carry "Phase 123"-style notes) and
  records whether the component simply postdates CalTracker's `v1.5.0` pin — so a zero-hit
  CalTracker count reads correctly as "too new for that pin," not "genuinely unused."

### Cards

**All 11 entries** (name + shipped tier, transcribed verbatim from `CardsFamilyScreen.kt`'s
`cardsFamilyEntries`, D-01):

| Component | Tier |
|-----------|------|
| `CardBase` | PATTERN |
| `CardTypeChip` | PATTERN |
| `TextCard` | PATTERN |
| `ListCard` | PATTERN |
| `AlbumCard` | PATTERN |
| `VoiceCard` | PATTERN |
| `AdaptiveMediaPreview` | PATTERN |
| `CardTagRow` | PATTERN |
| `CardQuickView` | PATTERN |
| `CountBadge` | PRIMITIVE |
| `TagListItem` | PATTERN |

**Finding C-1 — `TextCard`/`ListCard`/`AlbumCard`/`VoiceCard` near-duplicate-sibling shape.**
Confirmed by reading each component's own source: `TextCard.kt:177`, `ListCard.kt:175`,
`AlbumCard.kt:139`, `VoiceCard.kt:462` each directly compose `CardBase(...)`, and each entry's
registry `Entry` exposes an identically-shaped `isPinned` `Control.Toggle` (`textCardPinnedControl`
/ `listCardPinnedControl` / `albumCardPinnedControl` / `voiceCardPinnedControl`,
`CardsFamilyScreen.kt:76-79`) with the same States-matrix shape (Default / Pinned as the only
two non-N/A cells).

**Disposition: keep-with-rationale.** The shared `CardBase` composition is the *correct* use of
`CardBase` as the family's shell primitive — that is exactly what `CardBase` exists for (per its
own PATTERN rationale in `docs/DESIGN-INTENT.md`: it bakes in the reveal-confirm destructive-swipe
convention so callers don't re-derive it). Past the shared shell, each sibling's actual body
content is genuinely distinct and non-interchangeable: `TextCard` renders a text preview,
`ListCard` renders a `subType`-driven checklist (`items: List<ListItemUiModel>`), `AlbumCard`
renders `thumbnailItems` media thumbnails, and `VoiceCard` renders `durationMs`/`samplesPath`
waveform + `clips` rows — different domain content per the D-03 litmus's own domain-noun question
("Text"/"List"/"Album"/"Voice" are each a real domain noun naming genuinely different rendered
content, not four coats of paint on one shape). Folding these into one parameterized card would
trade a thin, already-shared shell for a wide content-type-switch composable — a net loss of
legibility for a shell that is already shared. No blast-radius grep needed (not a unify finding).

**Finding C-2 — `CardQuickView`, evaluated separately.** `CardQuickView.kt` was read in full: it
does **not** compose `CardBase` anywhere in its body (confirmed: zero `CardBase(` matches) — it
builds its own `Column`-based layout with its own title/pin/favorite header, its own
`tagContent`/`body` slots, and its own Created/Updated timestamp footer
(`CardQuickView.kt:52-126`). Its own KDoc states it was "extracted from the common structure of
`TextCardBottomSheet` and `ListCardBottomSheet`" as a **content-only, non-swipeable display
archetype** — deliberately renders no `SheetScaffold`/`ModalBottomSheet` chrome of its own, unlike
`TextCard`/`ListCard`/`AlbumCard`/`VoiceCard`'s swipeable interactive row shape (via `CardBase`'s
`SwipeableActionRow` infrastructure). Its registry shape (`isPinned` + `isFavorite`
`Control.Toggle` pair) superficially resembles the other four, but this reflects a shared
*vocabulary* (pin/favorite are hub-wide concepts), not a shared *implementation*.

**Disposition: keep-with-rationale.** `CardQuickView` serves a structurally different purpose —
the read-only quick-view body composed inside `TextCardBottomSheet`/`ListCardBottomSheet` (see
Sheets §, Finding S-1) — from the swipeable, `CardBase`-based row items in Finding C-1. Not folded
into that group. No blast-radius grep needed (not a unify finding).

**Altitude check.** No new cross-entry altitude-mismatch candidate surfaced in Cards beyond
tier values already ratified by Phase 1 — every entry's domain-noun-bearing name (Card/Text/List/
Album/Voice/Tag) and/or baked-in composition opinion (swipe convention, adaptive-grid layout,
siblings-band overflow) correctly earns PATTERN per the D-03 litmus; `CountBadge` (no domain noun,
renders only a caller-supplied `count`/`tileAccentColor`, no interaction convention of its own)
correctly earns PRIMITIVE. No restated findings.

### Chips

**All 5 entries** (name + shipped tier, transcribed verbatim from `ChipsFamilyScreen.kt`'s
`chipsFamilyEntries`, D-01):

| Component | Tier |
|-----------|------|
| `AppChip` | PATTERN |
| `TagChipWithContextMenu` | PATTERN |
| `ChipBar` | PRIMITIVE |
| `SortControl` | PATTERN |
| `FilterBar` | PRIMITIVE |

**Finding CH-1 — `ChipBar` vs. `FilterBar` overlap.** Both read in full
(`ChipBar.kt`, `FilterBar.kt`). Both are generic `<T>` `FlowRow`-based chip containers sharing the
same core arrangement (`Arrangement.spacedBy(8.dp)` horizontal). Real differences: `ChipBar` is a
data-driven list container (`items: List<T>`, `key: (T) -> Any`, `itemContent: @Composable (T) ->
Unit`, plus optional `leadingContent`/`trailingContent` slots, bare `FlowRow`, no chrome) —
"holds no chip-rendering opinions" per its own KDoc. `FilterBar` is a slot-based freeform container
(`content: @Composable FlowRowScope.() -> Unit`, no typed item list) that adds real chrome on top
of the same `FlowRow` shape: an outer `Surface` with `tonalElevation`, a prepended
expand/collapse `IconButton` (chevron), and an `expanded`-gated height cap + internal vertical
scroll. Per the D-03 litmus, `FilterBar`'s baked-in expand/collapse affordance and `Surface` chrome
opinion is exactly the kind of "composition opinion of its own" that already correctly earns it
registry-adjacent scrutiny — but both are still tiered PRIMITIVE today because neither introduces
a domain noun and both render only caller-supplied content (the litmus's condition 2 is about
domain-agnostic caller content, not the presence of chrome per se).

**Disposition: unify.** `FilterBar`'s expand/collapse chrome is a genuine, reusable mode that
`ChipBar` does not offer, and the two components' overlapping FlowRow-container purpose (holding
chip-shaped children in a wrapping row) is real, not superficial. Recommended unify shape for
Phase 5: fold `FilterBar`'s `expanded`/`onExpand`/`onCollapse` + `Surface`/height-cap chrome into
`ChipBar` as an optional mode (e.g. new nullable `expandable: ExpandableConfig?` parameter), then
retire `FilterBar` as a standalone entry — eliminating a sibling PRIMITIVE that duplicates the
FlowRow chip-container shape `ChipBar` already owns. `ChipBar`'s existing typed
`items`/`key`/`itemContent` shape would need to gain (or continue to coexist with) a raw-content
slot to carry `FilterBar`'s freeform `content: @Composable FlowRowScope.() -> Unit` callers — this
is real Phase-5 design work, not a mechanical merge, and is recorded as such in the Unify
Work-Order below.

Blast radius (D-02, read-only grep):
- `FilterBar`: SecondBrain 5 files, CalTracker_Android 0 files.
- `ChipBar`: SecondBrain 5 files, CalTracker_Android 0 files.

Both grep to zero on CalTracker — `ChipBar.kt`'s KDoc marks it "extraction-ready for the future
separate-repo library milestone (999.19)" and `FilterBar.kt`'s KDoc cites Phase-level provenance
("GADGET-02"), both consistent with these being newer additions than CalTracker's `v1.5.0` pin
rather than genuinely unused by that consumer; CalTracker's own hub surface (per RESEARCH.md's
Pitfall 4 finding) touches no Cards/Chips-family symbols at all, so this reads as "not yet on that
consumer's pin," not "rejected by that consumer."

**Finding CH-2 — `AppChip` vs. `TagChipWithContextMenu`.** Both read in full (`AppChip.kt`,
`TagChipWithContextMenu.kt`). Confirmed: `TagChipWithContextMenu` directly composes `AppChip(...)`
internally (`TagChipWithContextMenu.kt:88-100`), forwarding 7 of `AppChip`'s params verbatim
(`label`, `isSelected`, `onClick`, `leadingIcon`, `trailingIcon`, `relatednessStrength`,
`onDoubleClick`), wrapping `onLongClick` locally to fire a haptic + open its own `DropdownMenu`,
and adding 4 new menu-specific params of its own (`onEdit`, `onRemoveFromContext`, `onDelete`,
`removeLabel`). This is a decorator/wrapper relationship — `TagChipWithContextMenu` is "policy-free
chip wrapper adding a long-press-anchored Material3 `DropdownMenu` to `AppChip`" per its own KDoc —
not two independent implementations of the same chip-rendering logic.

**Disposition: keep-with-rationale.** Already correctly composed (wrapping, not duplicating) — no
unification needed, the existing decorator shape is the right one. No blast-radius grep needed
(not a unify finding).

**Altitude check.** No new cross-entry altitude-mismatch candidate surfaced in Chips beyond the
tier values already ratified by Phase 1. `AppChip`'s own PRIMITIVE-to-PATTERN correction is a
resolved, cited precedent — not a new finding (`ChipsFamilyScreen.kt:97-102`, WR-01 fix:
`relatednessStrength` bakes in the hub's own "Relatedness" domain vocabulary and a computed visual
encoding, same reasoning `docs/DESIGN-INTENT.md`'s `HeatSwatch` worked example uses). `ChipBar`
and `FilterBar` remain correctly tiered PRIMITIVE per the litmus (Finding CH-1's chrome difference
is an overlap/near-duplicate finding, not an altitude question — neither introduces a domain noun).

### Sheets

**All 18 entries** (name + shipped tier, transcribed verbatim from `SheetsFamilyScreen.kt`'s
`sheetsFamilyEntries`, D-01):

| Component | Tier |
|-----------|------|
| `AlbumSourcePickerSheet` | PATTERN |
| `AlbumTitleConfirmSheet` | PATTERN |
| `CardEditorShellContent` | PATTERN |
| `ListCardBottomSheet` | PATTERN |
| `RecordingBottomSheetContent` | PATTERN |
| `SheetScaffold` | PRIMITIVE |
| `TagChipEditorContent` | PATTERN |
| `TagPickerSheetContent` | PATTERN |
| `TextCardBottomSheet` | PATTERN |
| `TagPickerSheet` | PATTERN |
| `BulkCreatePopup` | PATTERN |
| `BulkCreatePopupContent` | PATTERN |
| `NameAndTagsEditor` | PATTERN |
| `TagCreateSheet` | PATTERN |
| `TagCreateSheetContent` | PATTERN |
| `VoiceRenameTagsSheet` | PATTERN |
| `ClearableTextField` | PRIMITIVE |
| `EditorItemRow` | PATTERN |

**Finding S-1 — `TextCardBottomSheet` vs. `ListCardBottomSheet` near-duplicate-sibling.** Both
read in full. Substantial, real duplication beyond the already-known `CardQuickView`-body sharing
(Cards §, Finding C-2): both ride `SheetScaffold`, both build a byte-for-byte-identical header
`Row` (title + `PushPin`/`Star` pin/favorite indicators, same padding/sizing/tint) with an
identical three-dot `DropdownMenu` (Edit → Pin/Unpin → Favorite/Unfavorite → Delete, same icons,
same `colorScheme.error`-tinted Delete row, same `region:edit-menu-item` `onEditRequest`-vs-local-
rename-dialog fallback logic), and both render the *exact same* local rename `AlertDialog` +
`ClearableTextField` block verbatim. The only genuine differences: `TextCardBottomSheet` renders
its body via `content: String?` plus an `ImageCountIndicator` in the header (`imageCount: Int`,
IMG-02) that `ListCardBottomSheet` lacks; `ListCardBottomSheet` renders `items: List<ListItemUiModel>`
via `ListPreviewItemRow` plus `readOnlyPreview`/`previewOverflowCount` (LIST-04) that
`TextCardBottomSheet` lacks. Both already delegate their shared body region to `CardQuickView`
with a blank `title` (per each file's own D-04 KDoc) — the *remaining* unshared duplication is the
header Row + three-dot menu + rename dialog, none of which is currently factored out.

**Disposition: unify.** Unlike Finding C-1 (Cards' `CardBase`-sharing siblings, which are already
correctly deduplicated down to the shell), this pair still duplicates real, non-trivial chrome
(header row, menu, rename dialog) verbatim across two files. Recommended unify shape for Phase 5:
extract the shared header-Row + three-dot-menu (Edit/Pin/Favorite/Delete) + rename-`AlertDialog`
block into a new shared composable (mirroring the precedent `CardQuickView` itself set for the
body region, D-04) that both `TextCardBottomSheet` and `ListCardBottomSheet` compose, parameterized
by their differing body slot (`content` + `imageCount` vs. `items` + `onToggleItem` +
`readOnlyPreview` + `previewOverflowCount`).

Blast radius (D-02, read-only grep):
- `TextCardBottomSheet`: SecondBrain 2 files, CalTracker_Android 0 files.
- `ListCardBottomSheet`: SecondBrain 2 files, CalTracker_Android 0 files.

Both grep to zero on CalTracker; per RESEARCH.md's Pitfall 4 finding, CalTracker's entire hub
surface excludes the Sheets family, so this reads as "not on that consumer's pin at all" rather
than "rejected."

**Finding S-2 — the 3 Sheet+Content shell/body pairs, confirmed as the intentional precedent
(not a duplicate).** All 3 pairs' actual signatures were read in full:
- `TagPickerSheet`/`TagPickerSheetContent` (`TagPickerSheet.kt:88-115` / `:152-161`):
  `TagPickerSheet` wraps `SheetScaffold` and calls `TagPickerSheetContent(...)` forwarding every
  one of its 8 params unchanged.
- `BulkCreatePopup`/`BulkCreatePopupContent` (`BulkCreatePopup.kt:59-86` / `:102-109`):
  `BulkCreatePopup` wraps a `Dialog` + one shared `.imePadding()` layer and calls
  `BulkCreatePopupContent(...)` forwarding all 5 of its params unchanged.
- `TagCreateSheet`/`TagCreateSheetContent` (`TagCreateSheet.kt:60-80` / `:90-95`):
  `TagCreateSheet` wraps `SheetScaffold` and calls `TagCreateSheetContent(...)` forwarding all 4
  of its params unchanged.

Each `*Content` composable's own KDoc states the same reason for the split: extracted as a public
composable purely so a cross-module Robolectric test can render the body directly without
popup-window node-tree complications (`:designsystem` has no Compose-test infra of its own) — an
infrastructure reason, not a design duplication. This confirms RESEARCH.md's Pitfall 3 reading.

**Disposition: keep-with-rationale**, for all 3 pairs — each `*Content` composable is the modal
body without its chrome wrapper, both independently registered because both are real public
exports (the wrapper composes the content directly, with no logic duplicated between the two).
No blast-radius grep needed (not a unify finding).

**Remaining non-paired entries.** Of the 12 Sheets entries not part of Finding S-2's 3 pairs
(`AlbumSourcePickerSheet`, `AlbumTitleConfirmSheet`, `CardEditorShellContent`,
`ListCardBottomSheet`, `RecordingBottomSheetContent`, `SheetScaffold`, `TagChipEditorContent`,
`TextCardBottomSheet`, `NameAndTagsEditor`, `VoiceRenameTagsSheet`, `ClearableTextField`,
`EditorItemRow`): `TextCardBottomSheet` and `ListCardBottomSheet` are addressed above (Finding
S-1). The remaining 10 — `AlbumSourcePickerSheet` (camera/gallery source picker),
`AlbumTitleConfirmSheet` (title-confirm dialog), `CardEditorShellContent` (full-screen editor
shell), `RecordingBottomSheetContent` (voice-recording state machine sheet), `SheetScaffold`
(generic chrome-only `ModalBottomSheet` wrapper, the PRIMITIVE every other Sheets entry rides),
`TagChipEditorContent` (tag add/remove editor body), `NameAndTagsEditor` (name field + tags slot
editor), `VoiceRenameTagsSheet` (voice-clip rename + tags sheet), `ClearableTextField` (the
generic PRIMITIVE text-field every sheet's rename/create flow reuses), and `EditorItemRow`
(reorderable list-item row) — each serves a genuinely distinct purpose per its own signature and
KDoc; no further overlap, near-duplicate-sibling, or altitude-mismatch candidate was found among
them. Explicitly stated rather than left unaddressed, per this task's own requirement.

**Altitude check.** No new cross-entry altitude-mismatch candidate surfaced in Sheets beyond tier
values already ratified by Phase 1 — `SheetScaffold` (no domain noun, renders only caller-supplied
`content` inside generic chrome) and `ClearableTextField` (no domain noun, a generic text-field
primitive) correctly earn PRIMITIVE; every other entry's domain-noun-bearing name (Tag/Card/Album/
Voice/List) and/or baked-in modal-chrome/menu/editor composition opinion correctly earns PATTERN.

### Buttons / FAB

**All 3 entries** (name + shipped tier, transcribed verbatim from `ButtonsFabFamilyScreen.kt`'s
`buttonsFabFamilyEntries`, D-01):

| Component | Tier |
|-----------|------|
| `ExpandableFab` | PATTERN |
| `CycleSubTypeButton` | PATTERN |
| `DynamicActionButton` | PATTERN |

**Overlap/near-duplicate-sibling check.** All 3 read in full (`ExpandableFab.kt`,
`CycleSubTypeButton.kt`, `DynamicActionButton.kt`). No candidate surfaced: `ExpandableFab` is a
Google-Keep-style expand/collapse fan of `SmallFloatingActionButton`s with a nested tier-2
sub-fan, `CycleSubTypeButton` is a single predictive-icon `IconButton` that cycles a fixed
3-value string enum, and `DynamicActionButton` is a role-driven filled-`Button`/`TextButton`
switch. Each renders a structurally distinct widget shape (FAB fan vs. single icon toggle vs.
role-colored button) with no shared internal composition and no overlapping parameter surface —
not a naming/family/tier coincidence like Finding T-1's `*Swatch` pair, genuinely three different
shapes. RESEARCH.md's own skim did not surface a seed here; this task's direct read confirms the
family is clean rather than leaving it unaddressed, per this task's own requirement.

**Disposition: no overlap/near-duplicate finding for this family.**

**Altitude check.** No cross-entry altitude-mismatch candidate surfaced beyond tier values already
ratified by Phase 1. `ExpandableFab` bakes in domain vocabulary directly into its own callback
names (`onCreateTextCard`/`onCreateListCard`/`onCreateVoiceCard`/`onAlbumCamera`/`onAlbumGallery`)
plus a fixed multi-tier fan/scrim/back-gesture interaction convention — correctly PATTERN twice
over (fails both litmus conditions). `CycleSubTypeButton` hardcodes the List card's own sub-type
vocabulary (`"BULLETED"`/`"ORDERED"`/`"CHECKBOX"`, `internal fun nextSubType`) and a fixed
predictive-icon cycle convention rather than rendering only caller-passed content — correctly
PATTERN. `DynamicActionButton` bakes in a fixed `role -> widget/color` mapping
(`ActionButtonDefaults.ActionButtonRole`) as a deliberate, non-overridable composition opinion
(per its own KDoc: "There is deliberately no caller-supplied widget override") — correctly
PATTERN. No restated findings.

### Pickers

**All 4 entries** (name + shipped tier, transcribed verbatim from `PickersFamilyScreen.kt`'s
`pickersFamilyEntries`, D-01):

| Component | Tier |
|-----------|------|
| `AccentColorPicker` | PATTERN |
| `IconPickerGrid` | PATTERN |
| `CropOverlay` | PATTERN |
| `SegmentedOptionSelector` | PRIMITIVE |

**Finding PK-1 — `AccentColorPicker` vs. `IconPickerGrid` shape echo, confirmed as genuinely
different implementations (not a duplicate).** Both read in full (`AccentColorPicker.kt`,
`IconPickerGrid.kt`). Both share a superficial "grid/flow selection picker" shape: a single
`selected*`/`on*Selected` callback pair, a caller-facing `showIndices: Boolean = false`
gallery-debug flag that overlays a numeric badge on each cell, and tap-to-select semantics with a
highlighted selected cell. Per RESEARCH.md Pitfall 3, read both actual bodies rather than
disposition from this naming/param echo alone: `AccentColorPicker` lays out a fixed, hardcoded
32-entry `ACCENT_COLORS` palette in a `FlowRow` of 40dp circular swatches (light/dark-aware);
`IconPickerGrid` lays out a hardcoded ~2,038-entry `ICON_MAP` in a `LazyVerticalGrid` with a live,
case-insensitive substring-search `ClearableTextField` above the grid (`filterIconEntries`) — a
materially more complex, independently-filterable widget with no live-search equivalent in
`AccentColorPicker`. The two hardcoded content sources (32 static colors vs. ~2,038 searchable
icons), the two layout primitives (`FlowRow` vs. `LazyVerticalGrid`), and the two cell shapes
(circle swatch vs. square `IconButton`) are genuinely distinct implementations — `showIndices` is
a shared gallery-only debug convention (present for this task's own state-matrix authoring, per
each file's own Variants usage), not evidence of a shared production primitive underneath.

**Disposition: keep-with-rationale.** The picker "shape echo" is coincidental convergence on the
same debug-affordance convention (`showIndices`), not implementation duplication — extracting a
shared `GridPicker<T>` primitive would need to abstract over a fixed-cardinality circular-swatch
grid and a filterable, ~2,038-entry searchable icon grid, which is a materially different problem
than either component solves today; no net legibility win identified. No blast-radius grep needed
(not a unify finding).

**Altitude check.** No new cross-entry altitude-mismatch candidate surfaced beyond tier values
already ratified by Phase 1. `AccentColorPicker`, `IconPickerGrid`, and `CropOverlay` each fail
condition (2) of the D-03 litmus the same way `HeatSwatch` does — each hardcodes its own content
source (a fixed color list, a fixed icon map, or a bespoke 8-handle drag-crop interaction
convention) rather than rendering only caller-passed content — correctly PATTERN. `CropOverlay`
additionally bakes in a whole gesture-driven interaction convention (`detectDragGestures` across 8
handles, aspect-ratio clamping), reinforcing PATTERN. `SegmentedOptionSelector` introduces no
domain noun and renders only the two caller-supplied `options` labels through a generic M3
`SingleChoiceSegmentedButtonRow` wrapper with no interaction convention beyond the toggle itself —
correctly PRIMITIVE, the same shape `ChipBar`'s own worked-example precedent covers (fully
generic, "holds no \[X\]-rendering opinions" in spirit). No restated findings.

### Feedback

**All 3 entries** (name + shipped tier, transcribed verbatim from `FeedbackFamilyScreen.kt`'s
`feedbackFamilyEntries`, D-01):

| Component | Tier |
|-----------|------|
| `ConfirmationDialog` | PRIMITIVE |
| `UndoCenterScreen` | PATTERN |
| `AttentionCue` | PRIMITIVE |

This family is consumed by CalTracker_Android (`ConfirmationDialog`, `ConfirmationDialogDefaults`)
as well as SecondBrain (per RESEARCH.md Pitfall 4) — given real attention below, not a rubber
stamp.

**Overlap/near-duplicate-sibling check.** All 3 read in full (`ConfirmationDialog.kt`,
`UndoCenterScreen.kt`, `AttentionCue.kt`). No candidate surfaced: `ConfirmationDialog` is a
title/body/confirm/dismiss `AlertDialog` wrapper, `UndoCenterScreen` is a full-screen scaffold
(`TopAppBar` + `LazyColumn` of undo entries + hold-to-peek gesture + `EmptyState` fallback), and
`AttentionCue` is a small inline caution glyph (`Inline`/`Dot` style). Three non-overlapping
widget classes (modal dialog / full screen / inline glyph) with no shared parameter surface or
internal composition.

**Disposition: no overlap/near-duplicate finding for this family.**

**Altitude check.** `ConfirmationDialog`'s PRIMITIVE tier was explicitly re-examined against the
D-03 litmus, since its own KDoc calls it "the single shared implementation for all
confirm-before-acting prompts app-wide" — language that could suggest a baked-in "modal-chrome
pattern" the litmus's own condition-2 parenthetical calls out. Confirmed after reading the body:
it renders a bare Material3 `AlertDialog` with caller-supplied `title`/`body` strings and a fixed
confirm/dismiss button pair — no domain noun in its name or parameters, and (per its own KDoc)
"DI/Nav-free," referencing no ViewModel/Nav/FeedbackController type. Its "canonical shared
implementation" framing describes *why* the hub ships it (avoiding N hand-rolled duplicates
downstream), not a baked-in interaction convention beyond what Material3's own `AlertDialog`
primitive already provides — the same "fully generic, pure presentation" shape `ChipBar`'s own
worked example sets the PRIMITIVE bar at. Shipped tier stands; no new finding. `UndoCenterScreen`
correctly earns PATTERN (bakes in a full scaffold + hold-to-peek gesture + relative-timestamp
ticking, well past "caller content only"). `AttentionCue` correctly earns PRIMITIVE (no domain
noun, renders only caller-passed `text`/`icon`/`tint` through one of two minimal, non-interactive
layout variants). No restated findings.

### Empty State

**The single entry** (name + shipped tier, transcribed verbatim from
`EmptyStateFamilyScreen.kt`'s `emptyStateFamilyEntries`, D-01):

| Component | Tier |
|-----------|------|
| `EmptyState` | PRIMITIVE |

**No overlap/near-duplicate-sibling finding possible** — a 1-entry family cannot have an
intra-family overlap or near-duplicate-sibling finding by definition; no cross-family comparison
is invented in its place, per this task's own instruction.

**Altitude check.** `EmptyState.kt` was read in full: its signature
(`icon: ImageVector, title: String, modifier, body: String? = null, ctaLabel: String? = null,
onCta: (() -> Unit)? = null`) introduces no domain noun and renders only caller-passed content
through a fixed icon/title/body/CTA vertical arrangement — the same "fixed presentational
arrangement of purely caller-supplied content" shape `ChipBar`'s own PRIMITIVE precedent covers
(a fixed layout convention alone does not fail condition 2; hardcoding *content*, as `HeatSwatch`
does, is what fails it). Nothing about the actual signature looks domain-coupled. Shipped tier
stands; no new finding.

### Progress / Metrics

_(PENDING - filled by a later task)_

### Tactile Foundation

**All 4 entries** (name + shipped tier, transcribed verbatim from
`TactileFoundationFamilyScreen.kt`'s `tactileFoundationFamilyEntries`, D-01):

| Component | Tier |
|-----------|------|
| `ElevationLadder` | PATTERN |
| `TactileTypeShowcase` | PATTERN |
| `GradientSwatch` | PATTERN |
| `HeatSwatch` | PATTERN |

**Finding T-1 — `GradientSwatch` vs. `HeatSwatch`, confirmed as genuinely different purposes
(not a duplicate).** Both read in full. Per `docs/DESIGN-INTENT.md`'s own worked example,
`HeatSwatch` takes **no caller-supplied content** (`HeatSwatch(modifier: Modifier = Modifier)`
only) — it hardcodes its own 6-sample jaccard array and renders a specific mindmap-relatedness
visual convention (connected nodes/edges that thicken toward the hotter sample, plus a
ring-bordered "distinct hub" node example), per `HeatSwatch.kt:37-42`'s own KDoc: "Heat targets
mindmap nodes/edges — a different consumer archetype than a chip." `GradientSwatch`
(`GradientSwatch.kt:32`) takes a **caller-supplied `accentColor: Color`** and renders two generic
accent bands (`accentGradient` hero band + `accentTint` flat-fill band) for whatever color the
caller passes — a reusable accent-ramp preview, not a hardcoded domain-specific visual. Same
family, same PATTERN tier, same `*Swatch` naming convention, genuinely different purposes —
confirmed by reading both signatures/bodies rather than assumed from the registry listing alone
(RESEARCH.md Pitfall 3).

**Disposition: keep-with-rationale.** No blast-radius grep needed (not a unify finding).

**Remaining entries.** `ElevationLadder` (the Elevation Scale token demo, Level0–Level5) and
`TactileTypeShowcase` (the Space Grotesk type-ramp token demo) were checked for any further
finding: both are display-only, parameterless token showcases imported from the `theme` package
(not `component/`), each demonstrating a distinct token domain (shadow-elevation scale vs.
typography ramp) with no shape or purpose overlap with each other, with the `*Swatch` pair, or
with any other family. No finding surfaced for either — stated explicitly rather than left
unaddressed.

**Altitude check.** No new cross-entry altitude-mismatch candidate surfaced in Tactile Foundation
beyond tier values already ratified by Phase 1. All 4 entries correctly earn PATTERN: none renders
only caller-passed content with zero composition opinion — each hardcodes a specific visual
convention or (for `GradientSwatch`) at minimum a fixed two-band composition — so all fail
condition (2) of the D-03 litmus regardless of domain-noun status, consistent with
`docs/DESIGN-INTENT.md`'s own `HeatSwatch` worked example.

### Unify Work-Order

_(PENDING - filled by a later task)_
