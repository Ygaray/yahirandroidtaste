---
phase: "20"
slug: "tone-foundation-atoms"
status: secured
threats_open: 0
asvs_level: 1
audited_head: 405d125e614f6963c710ce851f5b73a4a9e64a6a
created: "2026-10-12"
---

# Phase 20 - Security

> Per-phase security contract: threat register, accepted risks, and audit trail.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| caller -> ToneCard / ToneColors / atoms | Caller-supplied colors, labels, icons and composable content cross into the library; rendered as given | static UI copy |
| library -> consumer test tooling | Stable test tags visible only if a consumer opts in to resource-id exposure | test tags |
| source tree -> api.txt | Committed signature file is the public-surface contract consumers recompile against | public API |

---

## Threat Register

| Threat ID | Category | Component | Severity | Disposition | Status | Evidence |
|-----------|----------|-----------|----------|-------------|--------|----------|
| T-20-01 | Info Disclosure | tone_card tag exposure | low | mitigate | CLOSED | No resource-id exposure flag in src/main; ToneFoundationInvariantsTest.kt:67-80 scans src/main (comments stripped) |
| T-20-02 | Tampering | unintended public surface (ToneColors copy/componentN) | low | mitigate | CLOSED | ToneTokens.kt:19 plain class; ToneTokensTest.kt:63-67 asserts no copy/component*; no @Deprecated shims |
| T-20-03 | DoS | caller colors with unreadable contrast | low | accept | CLOSED | Accepted; hub defaults >= 4.5:1 (ToneTokensTest.kt:103-111); dynamic-color KDoc at ToneTokens.kt:34-45 |
| T-20-04 | Info Disclosure | caller text in ToneCard | low | accept | CLOSED | Accepted; ToneCard.kt:20 KDoc "static UI copy only"; no logging/persistence |
| T-20-05 | Tampering (regression) | existing DynamicActionButton call sites | high | mitigate | CLOSED | Early-return guard above untouched legacy `when (role)` (DynamicActionButton.kt:75-92); legacy block identical to v2.5.1; DynamicActionButtonLegacyPathSourceContractTest; DynamicActionButtonTest/AppChipTest/PresetChipTest unedited |
| T-20-06 | Tampering (API reshaping) | overloads, shims, enum edits | medium | mitigate | CLOSED | ActionButtonRole unchanged; no @Deprecated shims; api.txt delta vs v2.5.1 removes exactly one line (old DynamicActionButton signature replaced by appended-defaults signature) |
| T-20-07 | DoS | DynamicActionButton long localized label | medium | mitigate | CLOSED | Label wraps (no maxLines/overflow), height is a minimum; ToneAtomsLongLabelTest covers 4 emphases at 360dp |
| T-20-08 | Info Disclosure | DynamicActionButton label/icon | low | accept | CLOSED | Accepted; icon decorative (contentDescription = null). Caveat: "static UI copy only" KDoc sentence absent on DynamicActionButton label (non-blocking) |
| T-20-09 | Info Disclosure | round_icon_button / pill_chip tags | low | mitigate | CLOSED | Tags applied (RoundIconButton.kt:59, PillChip.kt:91); no exposure flag; repo-wide scan |
| T-20-10 | DoS | touch target below 48dp | medium | mitigate | CLOSED | minimumInteractiveComponentSize on both; RoundIconButtonTest/PillChipTest assert touch bounds >= 48dp and pitch |
| T-20-11 | DoS | PillChip long label overflow | medium | mitigate | CLOSED | maxLines=1 + Ellipsis + weight(fill=false) (PillChip.kt:113-115); ToneAtomsLongLabelTest |
| T-20-12 | Info Disclosure | RoundIconButton / PillChip caller text | low | accept | CLOSED | Accepted; KDoc at PillChip.kt:57, RoundIconButton.kt:39-40 |
| T-20-13 | DoS | hostile fractions in StackedProgressBar | medium | mitigate | CLOSED | clampSegmentFractions (StackedProgressBar.kt:43-50) handles NaN/negative/Inf/over-1; zero-weight guards; StackedProgressBarTest clamp + render cases |
| T-20-14 | Info Disclosure | resource-id exposure of test tags | medium | mitigate | CLOSED | Repo-wide scan (ToneFoundationInvariantsTest.kt:67-80); segment tag documented test-only |
| T-20-15 | Tampering | one-way dependency / hosting invariants | medium | mitigate | CLOSED | No non-allowed imports, no color literals, no @HiltAndroidApp/@AndroidEntryPoint; ToneFoundationInvariantsTest.kt:67-99 |
| T-20-16 | Tampering | stale or reshaped api.txt | medium | mitigate | CLOSED | api.txt contains all new public symbols; delta additive except one superseded signature line; apiCheck green per orchestrator post-merge run |
| T-20-17 | Info Disclosure | caller text in registry demos | low | accept | CLOSED | Accepted; demos use static sample strings only |

*Status: open / closed*
*Disposition: mitigate (implementation required) / accept (documented risk) / transfer (third-party)*

---

## Accepted Risks Log

| Risk ID | Threat Ref | Rationale | Accepted By | Date |
|---------|------------|-----------|-------------|------|
| AR-20-01 | T-20-03 | Caller responsibility for custom colors; hub defaults verified | plan-time (20-01) | 2026-10-12 |
| AR-20-02 | T-20-04, T-20-12, T-20-17 | Atoms render caller content only; KDoc states static UI copy | plan-time (20-01/03/04) | 2026-10-12 |
| AR-20-03 | T-20-08 | Decorative icon, caller label; low severity | plan-time (20-02) | 2026-10-12 |

---

## Informational Notes (non-blocking)

- BarSegment is a public data class (publishes copy/componentN); code review IN-04 deferred any change to an owner decision before the v2.6.0 cut.
- T-20-08 KDoc sentence ("static UI copy only") is missing on DynamicActionButton label doc.
- DomainVocabularyDriftGuardTest scanned the main checkout rather than the worktree during plan 20-04; the merged main tree was checked directly and the full suite re-run green post-merge.

---

## Security Audit 2026-10-12

| Metric | Count |
|--------|-------|
| Threats found | 17 |
| Closed | 17 |
| Open | 0 |

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed
- [x] `status: secured` set in frontmatter

**Approval:** verified 2026-10-12
