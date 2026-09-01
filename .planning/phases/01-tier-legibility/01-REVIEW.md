---
phase: 01-tier-legibility
reviewed: 2026-09-01T00:00:00Z
depth: standard
files_reviewed: 14
files_reviewed_list:
  - api.txt
  - docs/DESIGN-INTENT.md
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ButtonsFabFamilyScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/CardsFamilyScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ChipsFamilyScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentDetailScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistry.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/EmptyStateFamilyScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ExplorerIndexScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/FeedbackFamilyScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/PickersFamilyScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ProgressFamilyScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/SheetsFamilyScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/TactileFoundationFamilyScreen.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistryTierTest.kt
findings:
  critical: 0
  warning: 2
  info: 2
  total: 4
status: issues_found
---

# Phase 01: Code Review Report

**Reviewed:** 2026-09-01T00:00:00Z
**Depth:** standard
**Files Reviewed:** 14
**Status:** issues_found

## Summary

This phase adds a required `ComponentRegistry.Entry.tier` field (`PRIMITIVE`/`PATTERN`), tiers
all 53 registered components across the 9 family-screen files, and wires a shared `TierBadge`
composable into both `ComponentRow` (list surfaces) and `ComponentDetailScreen` (detail header).
A new `ComponentRegistryTierTest` cross-checks the three worked examples from
`docs/DESIGN-INTENT.md` (`CardBase`→PATTERN, `ChipBar`→PRIMITIVE, `HeatSwatch`→PATTERN) against
the live registry.

Traced the diff against `699b68feba8f39be5c483ae7a080f96940e395dc^..HEAD` (plans 03/04/05: Sheets
tiering, ButtonsFab/EmptyState/Pickers/Feedback/Progress/TactileFoundation tiering, and the
badge-wiring + test). All 53 entries across all 9 family files supply an explicit `tier` (the
constructor has no default, so this is compiler-enforced — a missing assignment would not
compile). The three worked examples do match their tier in the registry, and
`ComponentRegistryTierTest` genuinely fails loudly (via `.first { }` throwing, and an explicit
vacuous-pass guard) rather than silently passing on a renamed/missing entry.

No critical/blocking defects found. Two warnings: one substantive (a tier classification that
looks inconsistent with the phase's own litmus when checked against the actual component
source), one a UI-robustness gap (the new badge has no overflow/truncation handling next to long
component names). Two minor info-level notes on pre-existing issues visible in the reviewed
files, unrelated to this phase's own changes.

## Warnings

### WR-01: `AppChip` tiered PRIMITIVE despite a documented, non-caller-driven visual encoding baked into `relatednessStrength`

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ChipsFamilyScreen.kt:97` (entry
`tier = ComponentRegistry.Tier.PRIMITIVE`), cross-referenced against
`src/main/java/io/github/ygaray/yahirandroidtaste/component/AppChip.kt:51-56,85,89-109`

**Issue:** `docs/DESIGN-INTENT.md`'s litmus is disjunctive: PATTERN if EITHER (1) the name/any
parameter introduces domain vocabulary, OR (2) the component does not render only caller-passed
content (i.e. it bakes in its own interaction/visual convention). `AppChip`'s own KDoc documents
`relatednessStrength: Float?` as, when non-null and unselected, driving `containerColor`,
`contentColor`, `borderStroke`, and `labelFontWeight` through `relatednessVisual(...)` —
internally computed, not caller-supplied — the exact same "bakes in a specific encoding the hub
decided is the right shape" reasoning that earned `HeatSwatch` its PATTERN tier in this same
document's worked examples. The parameter name itself (`relatednessStrength`) also directly names
the hub's own "Relatedness" domain vocabulary (`RelatednessTier`/`RelatednessVisual`/
`relatednessVisual`, already registered `component/` exports), which is the kind of
parameter-level domain noun condition (1) is testing for — arguably closer to "Tag"/"Card" than a
generic `Float` weight, since it's coupled 1:1 to a specific hub feature (tag co-occurrence
scoring), not reusable for an arbitrary consumer signal.

Since no comment in `ChipsFamilyScreen.kt` addresses why `AppChip` is PRIMITIVE despite this
parameter (unlike the file's `Pressed / Selected` / `Disabled` state-cell comments, which
consistently justify each N/A), this reads as an oversight in applying condition (2) rather than
a deliberate, argued exception.

**Fix:** Re-apply the litmus to `AppChip` against its actual signature/body (per
`docs/DESIGN-INTENT.md`'s own instruction to read "the signature and body, not the call site"),
and either retier it to PATTERN or add a one-line justification comment next to its `tier =`
assignment explaining why the `relatednessStrength` branch doesn't count (e.g. "generic
continuous-score encoding, not `Tag`-specific — only the KDoc's example usage is
tag-relatedness").

### WR-02: New `TierBadge` has no overflow/truncation handling next to long component names

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ExplorerIndexScreen.kt:291-318`
(`ComponentRow`'s `headlineContent`), also
`src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentDetailScreen.kt:60-67` (the
detail screen's `TopAppBar` title)

**Issue:** Both surfaces now render:
```kotlin
Row(verticalAlignment = Alignment.CenterVertically) {
    Text(name)             // no weight, no maxLines, no overflow
    Spacer(Modifier.width(8.dp))
    TierBadge(tier)
}
```
Neither `Text` carries `Modifier.weight(1f, fill = false)` nor `maxLines = 1` +
`TextOverflow.Ellipsis`. `Row`'s default measure policy gives each unweighted child up to the
full remaining width independently — it does not shrink an earlier child to make room for a
later one. Several registered component names are long enough to be a real risk in practice:
`RecordingBottomSheetContent` (28 chars), `TagChipWithContextMenu` (23),
`SegmentedOptionSelector` (23), `BulkCreatePopupContent` (22), `AlbumSourcePickerSheet` (22),
`AlbumTitleConfirmSheet` (22), `TagPickerSheetContent` (21). On a `ListItem` row (`ComponentRow`)
this risks the trailing chevron/badge being pushed out of the visible row or clipped; in the
`ComponentDetailScreen` `TopAppBar` title — squeezed between the back arrow and the theme-toggle
action, i.e. even less horizontal room than a full-width list row — the risk is higher. Notably,
`AppChip` itself (in `component/AppChip.kt:151-158`) already establishes the codebase's own
convention for exactly this situation (`maxLines = 1, overflow = TextOverflow.Ellipsis`), which
this new code doesn't reuse.

**Fix:**
```kotlin
Row(verticalAlignment = Alignment.CenterVertically) {
    Text(
        name,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1f, fill = false)
    )
    Spacer(Modifier.width(8.dp))
    TierBadge(tier)
}
```
Apply to both `ComponentRow`'s `headlineContent` and `ComponentDetailScreen`'s `TopAppBar` title.
Worth confirming on-device (per this project's Gate-1 self-UAT convention) against the longest
registered names before considering this closed.

## Info

### IN-01: `HeroStatCard`'s "Pressed / Selected" and "Focused" state cells render an identical preview

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ProgressFamilyScreen.kt:117-131`

**Issue:** Both cells render the exact same call —
`HeroStatCard(label = "Total", value = "1,204", onClick = {})` — with no distinguishing param, so
the States matrix shows two visually identical rows for what are meant to be two distinct
interaction states. This predates this phase's diff (only the trailing `tier = PATTERN` line was
added to this entry by this phase) so it isn't a regression introduced here, but it's visible in
a file this phase touches and undermines the "every state cell is either genuinely distinct or
honestly N/A" discipline the rest of the registry follows.

**Fix:** Either mark `"Focused"` as N/A (`ComponentRegistry.StateCell("Focused")`, matching the
`"Disabled"` cell's own N/A precedent in the same entry) or find a genuinely distinguishing
render for it.

### IN-02: Unused `MaterialTheme` import in `FeedbackFamilyScreen.kt`

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/FeedbackFamilyScreen.kt:16`

**Issue:** `import androidx.compose.material3.MaterialTheme` has no corresponding usage anywhere
else in the file. Pre-existing (not touched by this phase's diff, which only added the
`ConfirmationDialog`/`UndoCenterScreen`/`AttentionCue` `tier =` lines and the `ComponentRow` tier
arg), but present in a file listed for this review and would normally be caught by this project's
stated zero-baseline detekt policy.

**Fix:** Remove the unused import.

---

_Reviewed: 2026-09-01T00:00:00Z_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
