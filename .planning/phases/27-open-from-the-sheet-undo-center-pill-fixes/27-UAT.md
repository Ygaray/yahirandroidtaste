---
status: complete
phase: 27-open-from-the-sheet-undo-center-pill-fixes
source: [27-01-SUMMARY.md, 27-02-SUMMARY.md, 27-03-SUMMARY.md, 27-04-SUMMARY.md]
started: 2026-10-22T18:55:00Z
updated: 2026-10-22T19:02:32Z
---

## Current Test

[testing complete]

## Tests

### 1. Success sheet: tap the row that names the item
expected: Explorer → Voice Command → OutcomeSheet, section "go-to-item rows". Show the Receipt single-row sheet, then the Classic one. Tap the row that names the item: the sheet slides down and the readout shows Opened: <key>, once. The row has a chevron and no action label next to it.
result: pass

### 2. Batch sheet: each targeted row opens its own item
expected: Show a batch go-to-item sheet. Each row with a chevron opens its own item (the readout shows that row's key); rows without a target (including a failed one) have no chevron and do nothing when tapped.
result: pass

### 3. Long German rows at max font keep the chevron
expected: Set font size to the largest. Show the German go-to-item sheets (single and batch, Receipt and Classic): long row text wraps and the chevron stays on the row, inside the screen; nothing is cut off.
result: pass

### 4. Undo Center go-to-item rows
expected: Explorer → Feedback → UndoCenterScreen, section "go-to-item rows". Tap row A (chevron): it opens. Hold a row: the preview shows and releasing opens nothing. Tap Undo on row A: it undoes, nothing opens, and its chevron goes away. Rows B (no target) and C (Undone) do nothing on tap. German section: same, and the layout holds.
result: pass

### 5. Handled-by pill at max font
expected: With font size at the largest, show the OutcomeSheet "Success, handled-by populated" (and a Receipt sheet with the handled-by pill): every line of the pill text sits inside the pill's shape. At normal font a one-line pill looks like the old capsule.
result: pass

### 6. Warn dot under dynamic colour: design call
expected: Open the montage phases/27-open-from-the-sheet-undo-center-pill-fixes/27-04-uat-evidence/uatr-20261017T194228Z-30718f43/31-sc5-dot-montage-v271-vs-head.png (v2.7.1 vs v2.8.0). Under dynamic colour the small warn dot on a pale pill used to nearly vanish; v2.8.0 gives it a ring so it reads as an outlined circle. Accept the ring on a white centre, or say you'd rather invert the fill (that would be a v2.8.1 change).
result: pass

### 7. Success with target and onOpenTarget shows a label-less chevron row in Receipt and Classic that calls onOpenTarget(target) once per tap, through both OutcomeSheet overloads
expected: Success with target and onOpenTarget shows a label-less chevron row in Receipt and Classic that calls onOpenTarget(target) once per tap, through both OutcomeSheet overloads
result: pass
source: automated
coverage_id: 27-01-D1

### 8. Without target or without handler the sheet renders exactly as v2.7.1 (same code path); v2.7.1 call shapes of OutcomeSheet and Success compile with null defaults
expected: Without target or without handler the sheet renders exactly as v2.7.1 (same code path); v2.7.1 call shapes of OutcomeSheet and Success compile with null defaults
result: pass
source: automated
coverage_id: 27-01-D2

### 9. Each targeted batch row opens its own item with its own target exactly once per tap; a target-less row has no chevron, no click action and does nothing, in Receipt and Classic
expected: Each targeted batch row opens its own item with its own target exactly once per tap; a target-less row has no chevron, no click action and does nothing, in Receipt and Classic
result: pass
source: automated
coverage_id: 27-02-D1

### 10. Edges: equal targets, equal labels (order kept, no de-duplication), empty batch, single-row batch, all rows target-less
expected: Edges: equal targets, equal labels (order kept, no de-duplication), empty batch, single-row batch, all rows target-less
result: pass
source: automated
coverage_id: 27-02-D2

### 11. No handler, or no targeted row, renders the v2.7.1 list (same semantics tree); BatchResultsList keeps its pin and visibility; both source-contract tests pass unedited
expected: No handler, or no targeted row, renders the v2.7.1 list (same semantics tree); BatchResultsList keeps its pin and visibility; both source-contract tests pass unedited
result: pass
source: automated
coverage_id: 27-02-D3

### 12. Source-compat: BatchRowResultUiModel 2- and 3-argument construction, copy and destructuring still compile with a null target; api.txt additive
expected: Source-compat: BatchRowResultUiModel 2- and 3-argument construction, copy and destructuring still compile with a null target; api.txt additive
result: pass
source: automated
coverage_id: 27-02-D4

### 13. Locked while in flight (dimmed, chevron kept, no click) then unlocked; error text for failed rows; no reserved chevron space; opaque keys never in the tree
expected: Locked while in flight (dimmed, chevron kept, no click) then unlocked; error text for failed rows; no reserved chevron space; opaque keys never in the tree
result: pass
source: automated
coverage_id: 27-02-D5

### 14. Through the new overload, an Available entry with a target opens its item with one tap; Undone, Failed, targetless entries and group headers never open
expected: Through the new overload, an Available entry with a target opens its item with one tap; Undone, Failed, targetless entries and group headers never open
result: pass
source: automated
coverage_id: 27-03-D1

### 15. Hold shows the peek and release neither opens nor undoes; a tap never composes the peek; a drag never opens; tapping Undo undoes once and neither opens nor peeks
expected: Hold shows the peek and release neither opens nor undoes; a tap never composes the peek; a drag never opens; tapping Undo undoes once and neither opens nor peeks
result: pass
source: automated
coverage_id: 27-03-D2

### 16. Stale-callback guard: after an Undo through the store the chevron and click action are gone and a tap opens nothing
expected: Stale-callback guard: after an Undo through the store the chevron and click action are gone and a tap opens nothing
result: pass
source: automated
coverage_id: 27-03-D3

### 17. Edges: adjacency (Undone row with an equal target), equal targets on two Available rows keep order and open once each, empty list renders the empty state, blank openLabel falls back to the message
expected: Edges: adjacency (Undone row with an equal target), equal targets on two Available rows keep order and open once each, empty list renders the empty state, blank openLabel falls back to the message
result: pass
source: automated
coverage_id: 27-03-D4

### 18. The existing call renders exactly as v2.7.1 (DefaultParityTest unedited) and the new overload reproduces goldens D1 to D4 and the row bounds when no row is openable; every v2.7.1 call shape binds to the old overload
expected: The existing call renders exactly as v2.7.1 (DefaultParityTest unedited) and the new overload reproduces goldens D1 to D4 and the row bounds when no row is openable; every v2.7.1 call shape binds to the old overload
result: pass
source: automated
coverage_id: 27-03-D5

### 19. A wrapped long German pill at font scale 2.0 keeps all four text-box corners inside its painted capped shape; the CircleShape control fails the same check
expected: A wrapped long German pill at font scale 2.0 keeps all four text-box corners inside its painted capped shape; the CircleShape control fails the same check
result: pass
source: automated
coverage_id: 27-04-D1

### 20. Phase-wide hygiene: no key access, no logging, nothing public in the new files, Classic dispatch literals once
expected: Phase-wide hygiene: no key access, no logging, nothing public in the new files, Classic dispatch literals once
result: pass
source: automated
coverage_id: 27-04-D4

## Summary

total: 20
passed: 20
issues: 0
pending: 0
skipped: 0
blocked: 0

## Gaps

[none yet]
