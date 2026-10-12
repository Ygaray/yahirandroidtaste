---
phase: "18"
slug: "catalog-integrity-api-dump-docs"
status: verified
threats_open: 0
asvs_level: 1
audited_head: 5d7ea665a8fbbdd1b033eab9cecde4c113a0cee4
created: "2026-10-05"
---

# Phase 18 — Security

> Per-phase security contract: threat register, accepted risks, and audit trail.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| Hub docs to consumers | API.md / INTEGRATION.md guidance consumers follow | Documentation text only (no secrets) |
| Working tree vs released baselines | api.txt baseline swap used for additive proof | Source-controlled api.txt, restored after each swap |
| Release boundary | Tag / push / consumer repin (Phase 19, human-gated) | None in this phase |

---

## Threat Register

| Threat ID | Category | Component | Severity | Disposition | Mitigation | Status |
|-----------|----------|-----------|----------|-------------|------------|--------|
| T-18-01 | Information Disclosure | API.md section 10 semanticsPrefix guidance | medium | mitigate | Static-UI-copy-only rule at API.md Failure row and sibling paragraph; ProposedItemUiModel.toString omission recorded | closed |
| T-18-02 | Tampering (documentation overclaim) | API.md v2.5.0 compatibility notes | medium | mitigate | First VI18N bullet qualified; trailing-lambda caveat with named-arg fix; binary compat attributed to javap proof re-proven at the cut | closed |
| T-18-03 | Tampering (scope creep into Phase 19) | tools/, hook, build files, CLAUDE.md etc. | medium | mitigate | git diff 625c714..HEAD outside .planning is API.md + INTEGRATION.md only | closed |
| T-18-04 | Information Disclosure (consumer names) | New section 10 / INTEGRATION note | low | mitigate | No consumer names in new section 10 or localization subsection | closed |
| T-18-05 | Tampering (working-tree integrity) | api.txt baseline swap | high | mitigate | api.txt unchanged, restore confirmed, no modified api.txt committed | closed |
| T-18-06 | Tampering (guard bypass or hidden result) | verify-api-additive.sh exit 3 | high | mitigate | Exit 3 recorded with exact 10-line allowlist; no override or --no-verify used | closed |
| T-18-07 | Tampering (supply chain / one-way dependency) | src/main imports, build files, detekt baseline | high | mitigate | 10 added imports, none foreign; build/config files unchanged; no Hilt host; zero-baseline detekt | closed |
| T-18-08 | Repudiation (stale or cached evidence) | Gate evidence | medium | mitigate | Uncached suite with fresh JUnit XML (751/22/0/0), drift-guard XMLs fresh | closed |
| T-18-09 | Tampering (premature release) | Tag / push / consumer repo | high | mitigate | No v2.5* tag locally or on origin; no consumer file touched | closed |
| T-18-10 | Information Disclosure (logging) | src/main | low | mitigate | No Log/println/Timber added since v2.4.1 | closed |
| T-18-SC | Tampering (supply chain) | Dependency installs | low | accept | No build/dependency file changed; no install occurred | closed (accepted) |

*Status: open · closed · open — below high threshold (non-blocking)*

---

## Accepted Risks Log

| Risk ID | Threat Ref | Rationale | Accepted By | Date |
|---------|------------|-----------|-------------|------|
| AR-18-01 | T-18-SC (18-01, 18-02) | Docs/evidence-only phase; no dependency or build file changed, so no supply-chain exposure introduced | plan threat model (disposition accept) | 2026-10-05 |

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-10-05 | 11 | 11 | 0 | gsd-security-auditor (verdict SECURED, ASVS L1, block_on high) |

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed
- [x] `status: verified` set in frontmatter

**Approval:** verified 2026-10-05
