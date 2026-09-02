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

_(PENDING - filled by a later task)_

### Buttons / FAB

_(PENDING - filled by a later task)_

### Pickers

_(PENDING - filled by a later task)_

### Feedback

_(PENDING - filled by a later task)_

### Empty State

_(PENDING - filled by a later task)_

### Progress / Metrics

_(PENDING - filled by a later task)_

### Tactile Foundation

_(PENDING - filled by a later task)_

### Unify Work-Order

_(PENDING - filled by a later task)_
