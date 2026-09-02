---
phase: 02-coherence-audit
reviewed: 2026-09-01T00:00:00Z
depth: standard
files_reviewed: 1
files_reviewed_list:
  - docs/COHERENCE-AUDIT.md
findings:
  critical: 2
  warning: 1
  info: 0
  total: 3
status: resolved
---

# Phase 2: Code Review Report

**Reviewed:** 2026-09-01
**Depth:** standard
**Files Reviewed:** 1
**Status:** issues_found

## Summary

`docs/COHERENCE-AUDIT.md` was reviewed against the live `ComponentRegistry`/`*FamilyScreen.kt`
sources it claims to transcribe, plus the two on-disk consumer repos (`~/Projects/SecondBrain`,
`~/Projects/CalTracker_Android`) it grep-verifies for blast radius, and `docs/DESIGN-INTENT.md`
(the D-03 litmus source). Verification was extensive: all 53 name+tier pairs across all 9 family
tables were cross-checked against their `*FamilyScreen.kt` source (exact match, including order,
in every family); every cited line-number range spot-checked (`TextCard.kt:177`,
`ListCard.kt:175`, `AlbumCard.kt:139`, `VoiceCard.kt:462`, `CardsFamilyScreen.kt:76-79`,
`TagChipWithContextMenu.kt:88-100`, `HeatSwatch.kt:37-42`, `GradientSwatch.kt:32`,
`TagPickerSheet.kt:88-115`/`152-161`, `BulkCreatePopup.kt:59-86`/`102-109`,
`TagCreateSheet.kt:60-80`/`90-95`, `CardQuickView.kt:52-126`) landed exactly on the claimed
content; every precise numeric claim (32-entry `ACCENT_COLORS`, ~2,038-entry `ICON_MAP`, the 5/2
SecondBrain file counts and 0/0 CalTracker counts for `FilterBar`/`ChipBar`/
`TextCardBottomSheet`/`ListCardBottomSheet`, `ConfirmationDialog`'s 3-file CalTracker exposure,
`CropOverlay`'s "8 draggable handles") reproduced exactly via independent `grep`/`wc -l`. No
placeholder/PENDING text remains, and the 9-family / 53-entry / plan-attribution ("02-01 did
Cards/Chips/Sheets/Tactile Foundation, 02-02 did the remaining 5") bookkeeping in the Unify
Work-Order section is internally consistent with the phase's own `02-01-PLAN.md`/`02-02-PLAN.md`.

Against that generally very high accuracy bar, two concrete, evidence-based defects were found:
one factual claim that is verifiably wrong despite being presented with strong "confirmed by
direct read" verification language, and one completeness gap where 5 of the 53 registered
components are silently absent from any disposition despite the document's own established
per-family accounting pattern. A third, narrower issue is an internal self-contradiction within
a single finding. None of these change any "unify" disposition or the Unify Work-Order's content,
but all three violate the audit's own stated evidentiary bar ("confirmed... not re-derived from
memory," "explicitly stated rather than left unaddressed, per this task's own requirement").

## Critical Issues

### CR-01: Progress/Metrics "consumer exposure note" undercounts CalTracker's real hub usage

**File:** `docs/COHERENCE-AUDIT.md:441-462` (the "Consumer exposure note (D-02 verification, not a
unify finding)" paragraph in the Progress / Metrics section)

**Issue:** The document asserts, with strong verification language ("direct inspection shows...",
"confirmed via...", "confirmed by direct read, not counted", "no blast-radius grep required beyond
this verification"): *"CalTracker's real hub-Progress/Metrics exposure is 1 file
(`RemainingBudgetHero.kt`, importing `AnimatedStatValue`/`HeroStatCard`/`ProgressRing`)."*

This is factually wrong. A second CalTracker file genuinely imports and calls the hub's
`ProgressRing`:

```
/home/yahir/Projects/CalTracker_Android/app/src/main/java/com/caltracker/app/ui/dailylog/components/CollapsingDayHeader.kt:37:
import io.github.ygaray.yahirandroidtaste.component.ProgressRing
...
CollapsingDayHeader.kt:183:
            ProgressRing(
```

This is not a false-positive comment-only hit like the `HomeScreen.kt` case the document
correctly dismisses — it is a real `import` line plus a real composable call site (line 183),
identical in kind to the `RemainingBudgetHero.kt` usage the document does count. The document's
own verification methodology (grepping `AnimatedStatValue|HeroStatCard|ProgressRing` and manually
disambiguating real imports from comment mentions) missed this file entirely: it checked
`HomeScreen.kt` and `RemainingBudgetHero.kt` but never surfaced `CollapsingDayHeader.kt`, even
though a plain `grep -rln -E "AnimatedStatValue|HeroStatCard|ProgressRing" .` from the CalTracker
app root surfaces it immediately (verified this session).

The true count is at least 2 files, not 1. This doesn't change the Progress/Metrics family's
disposition (no unify finding was raised there), but it is a specific, falsifiable factual claim
presented as independently verified — exactly the class of unsubstantiated/incorrect claim this
review is scoped to catch — and it weakens the document's credibility on every other
"confirmed by direct read" claim elsewhere.

**Fix:** Correct the exposure count and file list:
```markdown
CalTracker's real hub-Progress/Metrics exposure is 2 files — `RemainingBudgetHero.kt`
(importing `AnimatedStatValue`/`HeroStatCard`/`ProgressRing`) and `CollapsingDayHeader.kt`
(importing and calling `ProgressRing` for its compact 32dp mini progress indicator) — and
`MetricBar` has zero genuine hub-import exposure in CalTracker today...
```

---

### CR-02: 5 of 53 registered components are silently absent from any disposition

**File:** `docs/COHERENCE-AUDIT.md` — Cards section (lines 27-90) and Chips section (lines 91-163)

**Issue:** The document's own established method, followed consistently in 7 of 9 families,
explicitly accounts for every single entry in a family — either via a blanket "All N entries read
in full" overlap-check statement naming every component (Buttons/FAB line 277-278, Pickers
line 314-315 area, Feedback line 365-366, Progress/Metrics line 428-429), or via an explicit
"Remaining entries" / "Remaining non-paired entries" paragraph that names and dispositions every
leftover component not covered by a specific Finding (Sheets lines 244-258, Tactile Foundation
lines 503-509). This pattern is explicitly called out as deliberate: *"Explicitly stated rather
than left unaddressed, per this task's own requirement"* (Buttons/FAB, line 286; Sheets, line 258).

Two families break this pattern silently:

- **Cards** (11 entries): Finding C-1 covers `TextCard`/`ListCard`/`AlbumCard`/`VoiceCard`;
  Finding C-2 covers `CardQuickView`; the Altitude check paragraph names `CountBadge` individually.
  That accounts for 6 of 11 entries. `CardTypeChip`, `AdaptiveMediaPreview`, `CardTagRow`, and
  `TagListItem` are transcribed into the tier table (lines 35, 40, 41, 44) and never mentioned
  again anywhere in the section — no overlap/near-duplicate-sibling check, no altitude-mismatch
  check, no "remaining entries" catch-all the way Sheets and Tactile Foundation provide.
- **Chips** (5 entries): Finding CH-1 covers `ChipBar`/`FilterBar`; Finding CH-2 covers
  `AppChip`/`TagChipWithContextMenu`. `SortControl` is transcribed into the tier table (line 101)
  and never mentioned again anywhere in the section.

Verified by searching the full document text for every entry name in every family's table: these
5 names (`CardTypeChip`, `AdaptiveMediaPreview`, `CardTagRow`, `TagListItem`, `SortControl`) each
occur exactly once in the entire document — their single table-row occurrence — versus every other
one of the 53 entries occurring 2+ times (table row plus at least one prose mention).

This is a completeness gap against the document's own bar, not a hypothetical one: the Altitude
check paragraph for Cards makes a blanket claim ("every entry's domain-noun-bearing name...
correctly earns PATTERN per the D-03 litmus") that reads as covering all 11 entries but was never
actually demonstrated against these 4 specific components' real signatures, and the Chips section
makes no blanket claim covering `SortControl` at all.

**Fix:** Add the missing per-entry accounting, mirroring the Sheets/Tactile Foundation
"Remaining entries" pattern, e.g. for Cards:
```markdown
**Remaining entries.** `CardTypeChip`, `AdaptiveMediaPreview`, `CardTagRow`, and `TagListItem`
were checked for any further finding: [read each signature/body and state the actual
overlap/altitude conclusion for each, or explicitly state none was found and why].
```
and for Chips:
```markdown
**Finding CH-3 — `SortControl`, evaluated separately.** [read SortControl.kt in full and state
its overlap/altitude conclusion, or explicitly state no candidate was found].
```

## Warnings

### WR-01: Finding S-1 claims a "byte-for-byte-identical header Row" that the same finding immediately contradicts

**File:** `docs/COHERENCE-AUDIT.md:190-203`

**Issue:** Finding S-1 states: *"both build a byte-for-byte-identical header `Row` (title +
`PushPin`/`Star` pin/favorite indicators, same padding/sizing/tint)..."* — then, three sentences
later in the same finding: *"The only genuine differences: `TextCardBottomSheet` renders its body
via `content: String?` plus an `ImageCountIndicator` in the header (`imageCount: Int`, IMG-02) that
`ListCardBottomSheet` lacks..."*

Verified against source: `TextCardBottomSheet.kt` lines 149-154 render an `ImageCountIndicator`
inside the header `Row`, positioned between the Favorite icon and the three-dot menu button.
`ListCardBottomSheet.kt`'s header `Row` (lines 136-243) has no equivalent element — it goes
directly from the Favorite icon to the three-dot menu `Box`. The header `Row` is therefore not
byte-for-byte identical between the two files; it differs by exactly one child element. The
finding's own later sentence acknowledges this, creating a direct internal contradiction within a
single finding — the kind of self-contradiction this review is scoped to flag even though the
underlying "unify" disposition (extract the shared header/menu/rename-dialog chrome) is otherwise
well-supported and not in question.

**Fix:** Scope the "byte-for-byte-identical" claim precisely, e.g.:
```markdown
both build a near-identical header `Row` (title + `PushPin`/`Star` pin/favorite indicators, same
padding/sizing/tint, differing only by `TextCardBottomSheet`'s additional `ImageCountIndicator`
element) with an identical three-dot `DropdownMenu`...
```

---

_Reviewed: 2026-09-01_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
