---
status: complete
phase: 18-catalog-integrity-api-dump-docs
source: [18-01-SUMMARY.md, 18-02-SUMMARY.md]
started: 2026-10-12T00:15:15Z
updated: 2026-10-12T00:17:32Z
---

## Current Test

[testing complete]

## Tests

### 1. Accept the SC2 reading: Metalava apiCheck green = 'verify-api-additive passes'
expected: Roadmap SC2 says verify-api-additive passes. The raw-line tools/verify-api-additive.sh exits 3 because it can't tell an appended-defaulted-param rewrite from a break (all 10 flagged lines were checked by hand and are appends). Phase 18 (CONTEXT D-01) and the v2.5.0 cut treated Metalava apiCheck green + swap-baseline check vs v2.4.1 green + current api.txt as satisfying it. 'pass' = you accept that reading; otherwise say you want the roadmap wording amended or the script fixed.
result: pass

### 2. Docs read right, incl. the ProposedItemUiModel trailing-lambda caveat as document-only
expected: Skim API.md section 10 and INTEGRATION.md 'Localizing the voice surface'. They list every new label param, the Failure enrichment (role/body/semanticsPrefix) and the router toggle with defaults matching source. The one source break, ProposedItemUiModel(id, title) { ... } no longer compiling (callers must write trailingContent = { ... }), is documented only, with no shim (it already shipped this way in v2.5.0; both consumers use named args). 'pass' = documented-only is fine.
result: pass

### 3. Fix the wrong line 60 of 15-VERIFICATION.md
expected: 15-VERIFICATION.md line 60 says positional, named AND trailing-lambda v2.4.0 call shapes all still bind, which is false for ProposedItemUiModel's trailing lambda. Proposal: fold a correction (annotate the line, pointing at the API.md caveat) into the post-Gate-2 fix batch. 'pass' = do that; otherwise say how you want it handled.
result: pass

## Summary

total: 3
passed: 3
issues: 0
pending: 0
skipped: 0
blocked: 0

## Gaps

[none yet]

## Follow-Up Actions

- Test 3 approved: correct 15-VERIFICATION.md line 60 (ProposedItemUiModel trailing lambda does not bind) in the post-Gate-2 fix batch alongside G-15-1.
