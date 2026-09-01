---
status: testing
phase: 01-tier-legibility
source: [01-VERIFICATION.md]
started: 2026-09-01T23:06:07Z
updated: 2026-09-01T23:06:07Z
---

## Current Test

number: 1
name: On-device visual confirmation of the TierBadge (list + detail surfaces)
expected: |
  The Primitive/Pattern badge renders fully, never clipped or pushed off-row on the
  list surface (ComponentRow); on the detail screen's TopAppBar title, the component
  name truncates with an ellipsis (not the badge) when the two don't fit between the
  back arrow and the theme-toggle action. Badge color is visually distinguishable
  between Primitive (secondaryContainer) and Pattern (tertiaryContainer) in both
  light and dark theme.
awaiting: user response

## Tests

### 1. On-device visual confirmation of the TierBadge (list + detail surfaces)
expected: |
  Open ExplorerActivity on-device or emulator. Browse a family screen's component
  list and the index search results (ComponentRow surface), then open a detail page
  (ComponentDetailScreen TopAppBar surface) — check both a short name (e.g. AppChip)
  and the longest registered names (RecordingBottomSheetContent, 28 chars;
  TagChipWithContextMenu/SegmentedOptionSelector, 23 chars each). Check in both light
  and dark theme. Expected: badge renders fully, never clipped/pushed off-row; name
  truncates with ellipsis (not the badge) when both don't fit; badge color is
  visually distinguishable between tiers in both themes.
result: [pending]

## Summary

total: 1
passed: 0
issues: 0
pending: 1
skipped: 0
blocked: 0

## Gaps
