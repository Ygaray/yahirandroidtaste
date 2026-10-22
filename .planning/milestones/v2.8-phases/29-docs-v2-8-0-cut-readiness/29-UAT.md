---
status: complete
phase: 29-docs-v2-8-0-cut-readiness
source: [29-01-SUMMARY.md, 29-02-SUMMARY.md, 29-03-SUMMARY.md, 29-04-SUMMARY.md, 29-05-SUMMARY.md, 29-06-SUMMARY.md, 29-07-SUMMARY.md]
started: 2026-10-22T19:15:00Z
updated: 2026-10-22T19:15:00Z
---

## Current Test

[testing complete]

## Tests

### 1. Card scrolls its own content when taller than its host (29-07 a)
expected: A snackbar card taller than its host scrolls its content inside itself (no line cut, no ellipsis); vertical drags scroll, a horizontal swipe still dismisses, a tap still opens. Accept the design.
result: skipped
reason: "Yahir: skip the rest (Phase 29 included); signed off with gap"

### 2. Undo keeps a capped corner radius once its label wraps (29-07 b)
expected: The card's Undo is a capsule on one line and a capped-radius shape once its label wraps; at rest it has no container, so the shape only shows while pressed. Accept the design.
result: skipped
reason: "Yahir: skip the rest (Phase 29 included); signed off with gap"

### 3. Long Spanish/German strings at 360dp and font 2.0
expected: The stacked card, the sheet target rows and the handled-by pill keep every glyph inside the window in German and Spanish at font 2.0 (Gate-1 evidence 01 to 06, 27 to 42, 72 to 74).
result: skipped
reason: "Yahir: skip the rest (Phase 29 included); signed off with gap"

### 4. Gate-2 carry-forward notes
expected: Decide the carry-forward notes: German card column ~101dp with mid-word breaks, German batch Receipt actions below the fold at font 2.0, German "Bearbeiten" pill wrapping mid-word, the M3 drag-handle tooltip overlap, and the Undo Center chrome staying English.
result: skipped
reason: "Yahir: skip the rest (Phase 29 included); signed off with gap"

## Summary

total: 4
passed: 0
issues: 0
pending: 0
skipped: 4
blocked: 0

## Gaps

[none]
