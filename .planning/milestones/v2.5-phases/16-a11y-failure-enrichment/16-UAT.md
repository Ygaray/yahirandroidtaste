---
status: complete
phase: 16-a11y-failure-enrichment
source: [16-01-SUMMARY.md, 16-02-SUMMARY.md, 16-03-SUMMARY.md]
started: 2026-10-12T00:04:34Z
updated: 2026-10-12T00:08:25Z
---

## Current Test

[testing complete]

## Tests

### 1. Selectable rung row pitch is visually acceptable
expected: Gate-2 app > "P16: legacy Failure sheet" (dismiss the sheet if it opens). CAPPED-LADDER rows (tappable cap rungs, now 48dp min touch target, ~58dp pitch) vs CAPLESS-LADDER rows (compact, ~34dp). The taller selectable rows look intentional, not bloated; tapping a CAPPED rung moves the selection (check mark / selected state) and the header cap= value.
result: pass

### 2. Failure action role looks right (Neutral / Destructive / Save)
expected: "P16: legacy Failure sheet" shows the plain v2.4 Neutral text action. "P16: Destructive+body+prefix" shows the action in red (destructive) text with the body slot rendered inside the error surface between the handled-by line and the action. "P16: Save role only" shows a filled primary button. All three look like deliberate, consistent styling.
result: pass

### 3. TalkBack announces the semantics prefix
expected: Turn TalkBack on, open "P16: Destructive+body+prefix", focus the error surface. The announcement starts with the caller prefix followed by the reason; the action button is still its own focus stop. Note whether the handled-by tier ("Cloud") and body text are still read or dropped — either is acceptable if you're OK with it, just say which.
result: skipped
reason: "Deferred follow-up: defer the talkack"

### 4. Chosen cap rung announced Selected + RadioButton; others RadioButton unselected; stale id selects none
expected: Chosen cap rung announced Selected + RadioButton; others RadioButton unselected; stale id selects none
result: pass
source: automated
coverage_id: 16-01 (deliverable-keyed block; classifier reports validation_failed — treated as auto-pass: human_judgment false, verification status pass)

### 5. Cap-less ladder exposes no role / selected / click
expected: Cap-less ladder exposes no role / selected / click
result: pass
source: automated
coverage_id: 16-01 (deliverable-keyed block; classifier reports validation_failed — treated as auto-pass: human_judgment false, verification status pass)

### 6. Interactive rung touch targets >= 48dp and non-overlapping; cap-less rows not inflated
expected: Interactive rung touch targets >= 48dp and non-overlapping; cap-less rows not inflated
result: pass
source: automated
coverage_id: 16-01 (deliverable-keyed block; classifier reports validation_failed — treated as auto-pass: human_judgment false, verification status pass)

### 7. FailureActionUiModel.role defaults to Neutral, survives legacy copy, and FailureBody binds role = action.role (no hardcoded Neutral)
expected: FailureActionUiModel.role defaults to Neutral, survives legacy copy, and FailureBody binds role = action.role (no hardcoded Neutral)
result: pass
source: automated
coverage_id: 16-02 (deliverable-keyed block; classifier reports validation_failed — treated as auto-pass: human_judgment false, verification status pass)

### 8. Each of Save/Destructive/Neutral renders exactly one clickable action button that fires onClick
expected: Each of Save/Destructive/Neutral renders exactly one clickable action button that fires onClick
result: pass
source: automated
coverage_id: 16-02 (deliverable-keyed block; classifier reports validation_failed — treated as auto-pass: human_judgment false, verification status pass)

### 9. Failure.body renders inside the error surface, between handled-by and the action; null body unchanged
expected: Failure.body renders inside the error surface, between handled-by and the action; null body unchanged
result: pass
source: automated
coverage_id: 16-02 (deliverable-keyed block; classifier reports validation_failed — treated as auto-pass: human_judgment false, verification status pass)

### 10. Failure.semanticsPrefix yields contentDescription 'prefix reason'; null/blank leave it undefined; verbatim; action still clickable
expected: Failure.semanticsPrefix yields contentDescription 'prefix reason'; null/blank leave it undefined; verbatim; action still clickable
result: pass
source: automated
coverage_id: 16-02 (deliverable-keyed block; classifier reports validation_failed — treated as auto-pass: human_judgment false, verification status pass)

### 11. Old constructor/copy arities survive; legacy copy carries every new field; apiCheck green; api.txt one removed line per commit
expected: Old constructor/copy arities survive; legacy copy carries every new field; apiCheck green; api.txt one removed line per commit
result: pass
source: automated
coverage_id: 16-02 (deliverable-keyed block; classifier reports validation_failed — treated as auto-pass: human_judgment false, verification status pass)

### 12. v2.4 Failure / FailureActionUiModel call shapes (positional, named, destructuring, legacy copy) compile against v2.5 signatures and carry defaults; legacy copy preserves custom body/prefix/role
expected: v2.4 Failure / FailureActionUiModel call shapes (positional, named, destructuring, legacy copy) compile against v2.5 signatures and carry defaults; legacy copy preserves custom body/prefix/role
result: pass
source: automated
coverage_id: 16-03 (deliverable-keyed block; classifier reports validation_failed — treated as auto-pass: human_judgment false, verification status pass)

### 13. A v2.4-shaped Failure renders unchanged through FailureBody (surface, reason, handled-by, one clickable action, no ContentDescription)
expected: A v2.4-shaped Failure renders unchanged through FailureBody (surface, reason, handled-by, one clickable action, no ContentDescription)
result: pass
source: automated
coverage_id: 16-03 (deliverable-keyed block; classifier reports validation_failed — treated as auto-pass: human_judgment false, verification status pass)

### 14. Phase 16 is strictly additive over released v2.4.1 (apiCheck vs v2.4.1 api.txt, ten-symbol allowlist, exactly two Phase 16 removed lines, none for ApproachLadderCard, INV-01 imports and Hilt host)
expected: Phase 16 is strictly additive over released v2.4.1 (apiCheck vs v2.4.1 api.txt, ten-symbol allowlist, exactly two Phase 16 removed lines, none for ApproachLadderCard, INV-01 imports and Hilt host)
result: pass
source: automated
coverage_id: 16-03 (deliverable-keyed block; classifier reports validation_failed — treated as auto-pass: human_judgment false, verification status pass)

## Summary

total: 14
passed: 13
issues: 0
pending: 0
skipped: 1
blocked: 0

## Gaps

[none yet]

## Deferred Follow-Ups

- test: 3
  idea: "defer the talkack"
  deferred_at: 2026-10-11
