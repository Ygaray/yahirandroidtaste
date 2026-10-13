---
phase: "24"
slug: "catalog-docs-explorer-demos-cut-v2-6-0"
status: secured
threats_open: 0
asvs_level: 1
audited_head: b2eb0aeaba60dc9ab0b03f536290f1bacec48dd3
created: "2026-10-12"
---

# Phase 24 - Security

> Per-phase security contract: threat register, accepted risks, and audit trail.
> Pre-cut audit (plans 24-01 to 24-03 verified; plan 24-04 threats T-24-20 to T-24-28 are
> PENDING-AT-CUT and are re-audited after the cut, which also refreshes the audited_head stamp).

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| demo seed -> semantics tree | ProviderKeyCard lifecycle demo renders typed text and a saved fingerprint | raw typed key text (field mode only), fingerprint |
| host -> tester device | adb commands reach a physical phone | adb serial, harness APK |
| device screen -> committed files | screencaps and UI dumps are committed to a public repo | pixels, UI hierarchy |
| executor -> gate | Gate-1 could be waived or hand-written | verdicts, evidence |
| build output -> evidence | stale mavenLocal AAR could pass as the build under test | AAR |
| local repo -> GitHub / JitPack | tag + push is the irreversible half of the phase (plan 24-04) | commits, tag |

---

## Threat Register

| Threat ID | Category | Component | Severity | Disposition | Mitigation | Status |
|-----------|----------|-----------|----------|-------------|------------|--------|
| T-24-01 | Information Disclosure | raw key text in UI | high | mitigate | fingerprint keeps last 4; render test scans every semantics root with positive control; device capture 52 = 0 hits, control 54 = 1 | closed |
| T-24-02 | Tampering | existing tests | high | mitigate | protected tests byte-identical to cffad79; src/test appended only | closed |
| T-24-03 | Tampering | API or registry | medium | mitigate | api.txt byte-identical; registry counts 66+5 match docs | closed |
| T-24-04 | Repudiation | 92dp test | medium | mitigate | failing-first run recorded at 90.0dp | closed |
| T-24-05 | Denial of Service | German 360dp overflow | medium | mitigate | w360dp native render tests + device P04/P09/P11 | closed |
| T-24-06 | Information Disclosure | consumer nouns / key text | low | mitigate | LibraryInvariantsGuardTest + grep clean | closed |
| T-24-07 | Tampering | protected tests / gradle config | high | mitigate | git diff v2.5.1 HEAD clean over protected paths | closed |
| T-24-08 | Tampering | V251 canary | high | mitigate | 37 provenance comments, coverage test, pristine-archive pass | closed |
| T-24-09 | Tampering | additivity tool false negative | high | mitigate | 34-case fixture suite; rules R1-R8 after review fix | closed |
| T-24-10 | Information Disclosure | key-looking doc strings | medium | mitigate | no key patterns in added lines | closed |
| T-24-11 | Repudiation | doc drift | medium | mitigate | CatalogDocsDriftGuardTest | closed |
| T-24-12 | Elevation of Privilege | consumer import / Hilt host | medium | mitigate | invariants guard (WR-05 hardened) | closed |
| T-24-13 | Tampering | adb off-tester | high | mitigate | every adb command `-s R5CT10XNKQN` (transcripts scanned) | closed |
| T-24-14 | Repudiation | Gate-1 waived/hand-written | high | mitigate | uat-run verify (run and --self-uat) ok; no androidTest | closed |
| T-24-15 | Information Disclosure | screencap personal data | medium | mitigate | all UI nodes in the harness package; PNGs viewed | closed |
| T-24-16 | Tampering | device left altered | medium | mitigate | harness uninstalled, density restored; `secure display_density_forced` was not read before the run (reads 450 now) | open - below high threshold (non-blocking) |
| T-24-17 | Tampering | stale AAR | high | mitigate | AAR md5 recorded and re-read; classes.jar names confirmed | closed |
| T-24-18 | Elevation of Privilege | accidental tag/push from evidence plan | high | mitigate | no v2.6* tag local or remote at audit | closed |
| T-24-19 | Tampering | evidence commit sweep | medium | mitigate | commit 75fdf9f touched only owned paths | closed |
| T-24-20 to T-24-28 | various | plan 24-04 cut (range, author, paths, secret scan, exact push forms, tag by SHA, JitPack commit equality, clock, resume, no adb) | high x7, medium x2 | mitigate | specified in 24-04-PLAN.md | PENDING-AT-CUT |

*Status: open · closed · open - below block_on threshold (non-blocking)*

---

## Accepted Risks Log

| Risk ID | Threat Ref | Rationale | Accepted By | Date |
|---------|------------|-----------|-------------|------|
| AR-24-01 | T-24-16 | `secure display_density_forced` on the tester reads 450 (was empty or 450 before); no functional effect; flagged to the owner | orchestrator (non-blocking, below high) | 2026-10-12 |
| AR-24-02 | T-24-22 (hygiene) | Two superseded Phase 23 UI dumps (23-02 run uatr-20261012T172501Z-65a2a2aa, files 28 and 30) show launcher app names (no text content of the user); not scrubbed because that would break the superseded run signature; reviewed and listed in the 24-04 gate record | orchestrator, flagged to owner | 2026-10-12 |

---

## Pre-push hygiene (origin/main..HEAD, 187 commits)

Single author; origin/main is an ancestor of HEAD; 0 paths outside the allowlist; all 213 binaries are PNG;
secret-pattern scans over 39,421 added lines exit 1 (clean). Carry-forward for 24-04: widen the `sk-` pattern
to `sk-[A-Za-z0-9_-]{16,}`; re-record the real range count (187, not ~167).

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-10-12 (pre-cut) | 28 | 18 | 1 (non-blocking) + 9 pending-at-cut | gsd-security-auditor |

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed (blocking count)
- [ ] Post-cut re-audit of T-24-20 to T-24-28 (pending)

**Approval:** pre-cut secured 2026-10-12
