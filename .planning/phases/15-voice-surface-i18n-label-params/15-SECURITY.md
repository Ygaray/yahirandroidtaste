---
phase: "15"
slug: "voice-surface-i18n-label-params"
status: verified
threats_open: 0
asvs_level: 1
audited_head: 82c1cce1333f191117354ff42eea05e015e25c3f
created: "2026-10-05"
---

# Phase 15 — Security

> Per-phase security contract: threat register, accepted risks, and audit trail.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| consumer app -> library composable params / model fields | caller-supplied label strings enter the library and are rendered as plain Text / contentDescription | static host UI copy (low sensitivity); ProposedItemUiModel also carries user content (title/subtitle/confidenceCue) |
| library public API -> consumers' compiled code | shipped signatures, constructors, componentN and copy are source/binary contracts | API surface (Metalava api.txt) |
| developer commit -> hub additive guard | the HUB_LANE_OVERRIDE escape hatch could mask a real API break | commit metadata |

---

## Threat Register

| Threat ID | Category | Component | Severity | Disposition | Mitigation | Status |
|-----------|----------|-----------|----------|-------------|------------|--------|
| T-15-01 | Tampering (contract integrity) | ProviderKeyCard, ModelSelectCard, ClarificationBar, ApproachLadderCard signatures | high | mitigate | Only defaulted trailing String params appended; apiCheck vs released v2.4.1 green (negative control validated); v2.4.0 call-shape fixture compiles | closed |
| T-15-02 | Tampering (guard bypass) | HUB_LANE_OVERRIDE on 15-01 commits | medium | mitigate | Per-commit removed api.txt lines match plan (1/2/1); override = detected lane (2), narrowest valid | closed |
| T-15-03 | Info disclosure / Injection (V5) | caller label strings rendered as Text | low | accept | Static host copy, plain Text only; no format/HTML/logging in src/main | closed |
| T-15-04 | Tampering (INV-01) | imports in edited composables | medium | mitigate | No import added in src/main across phase (git diff v2.4.1 and 2f3d943) | closed |
| T-15-05 | Tampering (contract integrity) | UndoRowUiModel / UndoRefusedUiModel ctor + copy | high | mitigate | @JvmOverloads ctor + old-arity copy delegating current labels; reflection arity pins + copy-drift guard tests | closed |
| T-15-06 | Tampering (guard bypass) | HUB_LANE_OVERRIDE on 15-02 commits | medium | mitigate | Exactly one superseded copy line removed per commit; old ctor lines retained | closed |
| T-15-07 | Info disclosure | generated toString of Undo models includes labels | low | accept | Static UI copy; `reason` was already in generated toString | closed |
| T-15-08 | Tampering (INV-01) / Injection (V5) | OutcomeSheet render path + 2 models | medium | mitigate | No new imports; labels concatenated as plain string templates only | closed |
| T-15-09 | Tampering (contract integrity) | HandledByUiModel / ProposedItemUiModel ctor + copy | high | mitigate | @JvmOverloads ctor + old-arity copy; apiCheck vs v2.4.1 green; api.txt fresh | closed |
| T-15-10 | Info disclosure | ProposedItemUiModel.toString privacy | medium | mitigate | toString byte-identical to v2.4.1; pinned by pre-existing + custom-label tests | closed |
| T-15-11 | Tampering (guard bypass) | HUB_LANE_OVERRIDE across phase | medium | mitigate | Removed-line set vs v2.4.1 equals the 8-line allowlist; every removal has a superset replacement | closed |
| T-15-12 | Tampering (INV-01) / Info disclosure | src/main import set; removeContentDescription | medium | mitigate | No import added; label reaches only contentDescription; never logged/stored | closed |
| T-15-SC | Tampering (supply chain) | dependency installs (x3 plans) | low | accept | Build/gradle/jitpack files unchanged vs v2.4.1 and 2f3d943 | closed |

*Status: open · closed · open — below block_on threshold (non-blocking)*

---

## Accepted Risks Log

| Risk ID | Threat Ref | Rationale | Accepted By | Date |
|---------|------------|-----------|-------------|------|
| AR-15-01 | T-15-03 | Labels are static host UI copy rendered verbatim; library never logs, stores or format-templates them | plan 15-01 (plan-time disposition) | 2026-10-05 |
| AR-15-02 | T-15-07 | New fields hold static UI copy; `reason` was already in generated toString | plan 15-02 (plan-time disposition) | 2026-10-05 |
| AR-15-03 | T-15-SC | No package installed or version-changed in any plan | plans 15-01/02/03 (plan-time disposition) | 2026-10-05 |

---

## Observations (non-blocking, not registered threats)

- Pre-existing: `tools/hooks/pre-commit` exports an absolute `API_FILE`, so `verify-api-additive.sh` degrades to source-only and under-reports API removals (lane 1/2 instead of 3). Not introduced by this phase; T-15-02/06/11 were verified independently via apiCheck vs the released baseline and the removed-line allowlist. Recommend reporting to the control-plane technician.
- Review WR-01: Kotlin default-arg synthetics change shape (source-compatible, NOT binary-compatible; consumers must recompile). Documented in API.md; carry wording into v2.5.0 release notes.
- Review WR-02/WR-03/WR-04: i18n completeness limits, not security threats; backlog follow-ups.

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-10-05 | 15 | 15 | 0 | gsd-security-auditor (ASVS L1, block_on high) |

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed
- [x] `status: verified` set in frontmatter

**Approval:** verified 2026-10-05
