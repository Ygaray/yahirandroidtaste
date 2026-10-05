---
phase: "17"
slug: approachladdercard-router-on-off-toggle
status: secured
threats_open: 0
asvs_level: 1
audited_head: a8bc41bab9cf89374d0757baf35e6a4ad9c2e7e2
created: "2026-10-05"
---

# Phase 17 — Security

> Per-phase security contract: threat register, accepted risks, and audit trail.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| consumer app -> ApproachLadderCard params | router Boolean? / callback and two label strings; rendered verbatim, no library state | Boolean, String |
| v2.4.1-compiled consumer binary -> hidden v2.4.1 overload | exact JVM descriptor linkage; apiCheck cannot see the hidden shim | binary ABI |
| developer commit -> hub additive guard | lane override / --no-verify could hide an API/ABI break | commit metadata |
| gate command -> working tree | released-baseline apiCheck overwrites api.txt and must restore it | api.txt |
| src/main import set -> one-way dependency | only AndroidX, Android, Kotlin, library namespace | imports |

---

## Threat Register

| Threat ID | Category | Component | Severity | Disposition | Mitigation | Status |
|-----------|----------|-----------|----------|-------------|------------|--------|
| T-17-01 | Tampering (binary contract) | hidden v2.4.1 ApproachLadderCard overload + data-class synthetics | high | mitigate | Shim block byte-identical to 0956d79; shim tests/model untouched; ApproachLadderRouterCompatTest pins router=null; javap diff vs v2.4.1 AAR missing=0 (base=2526 head=2584), shim and fixture accessor descriptors present | closed |
| T-17-02 | Tampering (guard bypass) | HUB_LANE_OVERRIDE / --no-verify | high | mitigate | Lane recomputed per commit; the two LANE 2 commits (24c5b36, 7c040ca) overrode with exactly lane 2, recorded in 17-REVIEW-FIX.md; no --no-verify; apiCheck vs released v2.4.1 api.txt green; api.txt delta exactly one removed + one added line | closed |
| T-17-03 | Tampering (INV-01) | src/main imports | medium | mitigate | All added imports androidx/kotlin/library namespace; no Hilt host annotations; detekt baseline untouched | closed |
| T-17-04 | Repudiation (silent misconfiguration) | half-configured router pair | medium | mitigate | Third require() symmetric (ApproachLadderCard.kt:126-129); both-direction throw tests pass | closed |
| T-17-05 | Tampering (unsanctioned release) | tag / push / repin | medium | mitigate | No v2.5* tag, no tag at HEAD, nothing pushed, no consumer/build file touched | closed |
| T-17-06 | Tampering (working-tree integrity) | api.txt rewrite by baseline check | low | mitigate | git status for src and api.txt empty; api.txt unchanged since tracer commit | closed |
| T-17-07 | Information disclosure | require() message | low | accept | Message interpolates only a Boolean and null/non-null; labels rendered as plain text | closed |
| T-17-SC | Tampering (supply chain) | dependency installs | low | accept | No dependency or build-file change | closed |

*Status: open · closed · open — below high threshold (non-blocking)*

---

## Accepted Risks Log

| Risk ID | Threat Ref | Rationale | Accepted By | Date |
|---------|------------|-----------|-------------|------|
| AR-17-01 | T-17-07 | require() message exposes only a Boolean and null / non-null (ASVS V5 minimal); labels never parsed or templated | plan (disposition accept) | 2026-10-05 |
| AR-17-02 | T-17-SC | No packages installed or changed in Phase 17 | plan (disposition accept) | 2026-10-05 |

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-10-05 | 8 | 8 | 0 | gsd-security-auditor |

Informational notes (non-blocking): review-fix IN-02 (7c040ca) rewrote two comment-only KDoc lines, so the plan's literal "exactly one removed line" gate would now see 3 (comment lines only; shim, descriptors, api.txt unaffected). The pre-commit hook's API-line check runs degraded to source-only (absolute API_FILE path) — pre-existing, outside the Phase 17 register; route to the control-plane technician.

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed
- [x] `status: secured` set in frontmatter

**Approval:** verified 2026-10-05
