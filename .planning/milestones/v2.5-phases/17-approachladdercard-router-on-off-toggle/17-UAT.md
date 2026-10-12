---
status: complete
phase: 17-approachladdercard-router-on-off-toggle
source: [17-01-SUMMARY.md]
started: 2026-10-12T00:08:41Z
updated: 2026-10-12T00:13:13Z
---

## Current Test

[testing complete]

## Tests

### 1. Stacked Online/Offline + Router toggles look right (light and dark)
expected: Gate-2 app > Gallery > Voice Command > ApproachLadderCard. At the card bottom the Router off / Router on segmented toggle sits directly below the Online / Offline only toggle. Toggle Router on and off, then switch to dark theme (top-bar icon) and repeat. Spacing between the two toggles, the card's height growth, and the selected-segment contrast all look deliberate in both themes; the selection follows your taps.
result: pass

### 2. Rung tap still only picks the cap
expected: On the same card, tap a different rung. Only the cap selection moves; the Router toggle state, rung order, and the Capped / Needs network labels don't change and nothing navigates.
result: pass

### 3. Router ON/OFF toggle renders below the offline toggle, reflects state (false = Router off selected, true = Router on selected) and emits the tapped segment's target value
expected: Router ON/OFF toggle renders below the offline toggle, reflects state (false = Router off selected, true = Router on selected) and emits the tapped segment's target value
result: pass
source: automated
coverage_id: D1

### 4. Null pair hides the toggle with no dead space and half pairs throw in both directions; null -> false -> true -> null flips cleanly
expected: Null pair hides the toggle with no dead space and half pairs throw in both directions; null -> false -> true -> null flips cleanly
result: pass
source: automated
coverage_id: D2

### 5. Caller-localizable routerOnLabel / routerOffLabel replace the segment text with English defaults when omitted
expected: Caller-localizable routerOnLabel / routerOffLabel replace the segment text with English defaults when omitted
result: pass
source: automated
coverage_id: D3

### 6. Router never changes rung order, Capped / Needs network affordances, cap selection or rung positions; rung / offline / router taps emit only their own callback; two cards keep independent callbacks
expected: Router never changes rung order, Capped / Needs network affordances, cap selection or rung positions; rung / offline / router taps emit only their own callback; two cards keep independent callbacks
result: pass
source: automated
coverage_id: D4

### 7. Append-only binary/source compatibility: hidden v2.4.1 overload renders through with router = null, Phase-15 eleven-positional shape still binds, api.txt delta is one line, apiCheck green vs released v2.4.1, javap descriptor diff missing = 0
expected: Append-only binary/source compatibility: hidden v2.4.1 overload renders through with router = null, Phase-15 eleven-positional shape still binds, api.txt delta is one line, apiCheck green vs released v2.4.1, javap descriptor diff missing = 0
result: pass
source: automated
coverage_id: D5

### 8. Explorer fixture shows a live router toggle in every cell plus an appended 'router on' cell, reached through ComponentRegistry
expected: Explorer fixture shows a live router toggle in every cell plus an appended 'router on' cell, reached through ComponentRegistry
result: pass
source: automated
coverage_id: D6

## Summary

total: 8
passed: 8
issues: 0
pending: 0
skipped: 0
blocked: 0

## Gaps

[none yet]
