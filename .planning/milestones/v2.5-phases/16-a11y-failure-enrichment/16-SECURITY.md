---
phase: "16"
slug: "a11y-failure-enrichment"
status: verified
threats_open: 0
asvs_level: 1
audited_head: be27076b1820b5f20ec96cf6a5b12e3ea63afb3d
created: "2026-10-05"
---

# Phase 16 — Security

> Per-phase security contract: threat register, accepted risks, and audit trail.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| consumer app -> ApproachLadderCard props | caller-supplied rung ids/labels and the maxTierId/onMaxTierChange pair drive the semantics tree | UI copy |
| semantics tree -> accessibility services | role and selected state are trusted by TalkBack | a11y state |
| consumer app -> Failure / FailureActionUiModel fields | reason, semanticsPrefix, body composable, action role/callback are rendered/announced | UI copy |
| published model classes -> consumers' compiled code | constructors/componentN/copy are source/binary contracts | API surface |
| developer commit -> hub additive guard | HUB_LANE_OVERRIDE can hide a break if the authoritative gate is skipped | build gate |
| src/main import set -> one-way dependency | library imports only AndroidX/Android/Kotlin/own namespace | imports |

---

## Threat Register

| Threat ID | Category | Component | Severity | Disposition | Mitigation | Status |
|-----------|----------|-----------|----------|-------------|------------|--------|
| T-16-01 | Information disclosure | generated toString of Failure / FailureActionUiModel | low | accept | static UI copy; KDoc warns prefix must never carry sensitive text (VoiceOutcomeUiState.kt:73-74); no toString override | closed |
| T-16-02 | Tampering (contract integrity) | constructors/copy of FailureActionUiModel / Failure | high | mitigate | @JvmOverloads + hand-written legacy copy carrying fields; arity pins in VoiceModelLabelDefaultsTest | closed |
| T-16-03 | Tampering (guard bypass) | HUB_LANE_OVERRIDE on 16-02 commits | medium | mitigate | one removed api.txt line per commit; old ctor lines survive | closed |
| T-16-04 | Tampering / Injection | semanticsPrefix in contentDescription | low | mitigate | plain template, no format/HTML; verbatim + blank tests (OutcomeSheet.kt:305-307) | closed |
| T-16-05 | Spoofing (a11y state) | CapControl selectable role/selected | medium | mitigate | selected = onMaxTierChange != null && rung.id == maxTierId; tests pin chosen/stale/cap-less | closed |
| T-16-06 | Tampering (API leak) | RungRow / CapControl / FailureBody | medium | mitigate | all private; no api.txt diff for ApproachLadderCard | closed |
| T-16-07 | Tampering (INV-01) | ApproachLadderCard.kt imports | low | mitigate | androidx-only imports added | closed |
| T-16-08 | DoS (a11y reachability) | merged semantics on failure surface | medium | mitigate | merge-only semantics, no clearAndSetSemantics/liveRegion; action tap test under prefix | closed |
| T-16-09 | Tampering (INV-01) | model/ and component/ imports | low | mitigate | contentDescription + same-module ActionButtonDefaults only | closed |
| T-16-10 | Tampering (guard bypass) | HUB_LANE_OVERRIDE across Phases 15-16 | high | mitigate | exactly 10 removed api.txt lines vs v2.4.1 (allowlisted); Phase 16 removed exactly 2 | closed |
| T-16-11 | Tampering (contract integrity) | v2.4 call shapes | high | mitigate | VoiceI18nSourceCompatTest fixture + OutcomeSheetTest render pin | closed |
| T-16-12 | Tampering (INV-01) | src/main imports and Hilt annotations | medium | mitigate | no non-library import, no Hilt host/entry point added | closed |
| T-16-13 | Tampering (working tree) | released-baseline check rewrites api.txt | low | mitigate | git status for src/api.txt empty at HEAD | closed |
| T-16-SC | Tampering (supply chain) | dependency installs (plans 01, 02, 03) | low | accept | no dependency added or changed | closed |

*Status: open · closed · open — below {block_on} threshold (non-blocking)*
*Severity: critical > high > medium > low — only open threats at or above workflow.security_block_on count toward threats_open*
*Disposition: mitigate (implementation required) · accept (documented risk) · transfer (third-party)*

---

## Accepted Risks Log

| Risk ID | Threat Ref | Rationale | Accepted By | Date |
|---------|------------|-----------|-------------|------|
| AR-16-01 | T-16-01 | New fields hold static UI copy / enum / lambda reference; reason already in generated toString; KDoc forbids sensitive prefix text | plan (16-02) | 2026-10-05 |
| AR-16-02 | T-16-SC | No package installed or version-changed in any Phase 16 plan | plan (16-01/02/03) | 2026-10-05 |

---

## Security Audit 2026-10-05

| Metric | Count |
|--------|-------|
| Threats found | 15 |
| Closed | 15 |
| Open | 0 |

Auditor verdict: SECURED (ASVS L1, block_on high). 13/13 distinct threat IDs closed (3x T-16-SC counted separately above = 15 register rows across plans). No unregistered threat flags in any SUMMARY.
