---
phase: "21"
slug: "receipt-row-pill-selector-sheet-shape"
status: secured
threats_open: 0
asvs_level: 1
audited_head: eb987763efb1f95b01638d1f02bd5b224bbdf641
created: "2026-10-12"
---

# Phase 21 - Security

> Per-phase security contract: threat register, accepted risks, and audit trail.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| caller -> ReceiptRow / SegmentedOptionSelector / SheetScaffold | Caller-supplied strings, shapes, option sets and composable slots cross into the library; rendered as given | static UI copy |
| library -> consumer test tooling | Stable test tags visible only if a consumer opts in to resource-id exposure | test tags |
| source tree -> api.txt | Committed signature file is the public-surface contract consumers recompile against | public API |

---

## Threat Register

| Threat ID | Category | Component | Severity | Disposition | Status | Evidence |
|-----------|----------|-----------|----------|-------------|--------|----------|
| T-21-01 | Tampering | existing SheetScaffold call sites regress | high | mitigate | CLOSED | SheetScaffold.kt:60 default is BottomSheetDefaults.ExpandedShape, forwarded by name at :69; SheetScaffoldShapeTest pins order/default/forward; SheetScaffoldShapeProbeTest proves the shape reaches ModalBottomSheet; OutcomeSheetTest and AlbumTitleConfirmSheetTest unedited |
| T-21-02 | Tampering | accidental reshaping of public API (overloads, shims) | medium | mitigate | CLOSED | Single SheetScaffold function (api.txt:401); no @Deprecated/@JvmOverloads in the phase files |
| T-21-03 | Info Disclosure | test text and tags in the gallery demo | low | accept | CLOSED | Accepted; static literal strings (SheetsFamilyScreen.kt:797-817); no testTagsAsResourceId in src/main code (ToneFoundationInvariantsTest:71-83) |
| T-21-04 | Tampering | Material selector regression (G-15-1 layout, equal height, icon slot) | high | mitigate | CLOSED | Pill early return (SegmentedOptionSelector.kt:88-91) above the Material block; zero removed lines vs v2.5.1; SegmentedOptionMaterialParitySourceContractTest; SegmentedOptionSelectorTest, ApproachLadderCard tests unedited |
| T-21-05 | DoS | over-long labels overflow the Pill container | medium | mitigate | CLOSED | maxLines=1 + Ellipsis in weighted segments (:220, :246-247); German 360dp case in SegmentedOptionSelectorPillTest |
| T-21-06 | Tampering | disabled option still fires onSelect | medium | mitigate | CLOSED | segmentEnabled into selectable(enabled=) (:217,:228); Material per-segment enabled (:99,:103); tests assert no onSelect for the disabled option in both styles |
| T-21-07 | Tampering | disabledOptions out-of-range or selected-and-disabled | low | mitigate | CLOSED | Set only queried with index !in; tests cover {-1,2,7} and selected-and-disabled |
| T-21-08 | Info Disclosure | labels and disabledReason in contentDescription | low | accept | CLOSED | Accepted; caller strings only. Caveat: SegmentedOptionSelector KDoc lacks the "static UI copy only" sentence (documentation gap, non-blocking) |
| T-21-09 | Tampering | stale or reshaped api.txt (selector) | medium | mitigate | CLOSED | api.txt:392,395 match source; exactly 3 removed lines vs v2.5.1 |
| T-21-10 | Tampering | merge swallows trailingAction | high | mitigate | CLOSED | trailingAction is the outer Row's second child with its own tag (ReceiptRow.kt:231-233); summary takes no trailingAction param; ReceiptRowSourceContractTest and ReceiptRowTest assert separation |
| T-21-11 | DoS | very long title/subtitle/value/caption | medium | mitigate | CLOSED | maxLines=1 + Ellipsis on every text; title is the only weighted child; ReceiptRowLongLabelTest |
| T-21-12 | Info Disclosure | caller text in stateDescription/merged text/tags | medium | mitigate | CLOSED | KDoc ReceiptRow.kt:142 "static UI copy"; six constant tags; flag scan as T-21-03 |
| T-21-13 | Tampering | one-way dependency / hosting invariant erosion | medium | mitigate | CLOSED | ToneFoundationInvariantsTest covers ReceiptRow, SegmentedOptionSelector, SheetScaffold (imports, no Color(0x, no Hilt host); DomainVocabularyDriftGuardTest:428 |
| T-21-14 | Tampering | stale api.txt (ReceiptRow) | medium | mitigate | CLOSED | api.txt:332 matches ReceiptRow.kt:172-193; no removed lines from ReceiptRow |
| T-21-15 | Repudiation | in-phase fix silently rewrites committed ReceiptRow lines | low | mitigate | CLOSED | Pre-commit lane classifier active; the two lane-2 rewrites (51d1538, e224ed9) recorded with HUB_LANE_OVERRIDE=2 and rationale in 21-REVIEW-FIX.md. Caveat: override is an env var; audit trail is the REVIEW-FIX report |
| T-21-16 | DoS | extreme localized value/title/subtitle at 360dp | medium | mitigate | CLOSED | ReceiptRowLongLabelTest: German title, subtitle, trailing action, 1.5x font scale, 80-digit bound; limit documented in KDoc |
| T-21-17 | Info Disclosure | resource-id exposure of tags in release builds | medium | mitigate | CLOSED | No testTagsAsResourceId in src/main code; ToneFoundationInvariantsTest scan; test-only hooks documented |
| T-21-18 | Tampering | invariant erosion in the phase's new code | medium | mitigate | CLOSED | Same evidence as T-21-13 |
| T-21-19 | Tampering | stale api.txt, hidden-deprecated overload or copy shim | medium | mitigate | CLOSED | 3 removed lines, 0 deprecated/hidden/copy$default added; one each of SheetScaffold, SegmentedOptionSelector, ReceiptRow in api.txt |
| T-21-20 | Repudiation | Gate-1 waived or hand-written | high | mitigate | CLOSED | No SELF-UAT written by the plans; no device_exempt in 21-CONTEXT.md; ROADMAP success criteria unedited; Gate-1 handed to the tester |
| T-21-21 | Info Disclosure | caller text in gallery demos | low | accept | CLOSED | Accepted; static sample strings only |

*Status: open / closed*
*Disposition: mitigate (implementation required) / accept (documented risk) / transfer (third-party)*

---

## Accepted Risks Log

| Risk ID | Threat Ref | Rationale | Accepted By | Date |
|---------|------------|-----------|-------------|------|
| AR-21-01 | T-21-03, T-21-21 | Gallery demos render static sample strings only | plan-time (21-01/04) | 2026-10-12 |
| AR-21-02 | T-21-08 | Component renders caller strings only; static UI copy; low severity | plan-time (21-02) | 2026-10-12 |

---

## Security Audit 2026-10-12

| Metric | Count |
|--------|-------|
| Threats found | 21 |
| Closed | 21 |
| Open | 0 |

Unregistered flags: none. Auditor verdict: SECURED (ASVS 1, block_on high).
