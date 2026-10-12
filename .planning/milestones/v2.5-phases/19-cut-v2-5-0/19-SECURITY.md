---
phase: "19"
slug: "cut-v2-5-0"
status: verified
threats_open: 0
asvs_level: 1
audited_head: 44476752ba29b09ccc3f41fe96c1d88fdf8458ea
created: "2026-10-05"
---

# Phase 19 - Security

> Per-phase security contract: threat register, accepted risks, and audit trail.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| Baseline AAR source -> gate script | Downloaded/cached binary archive decides the gate verdict | AAR bytes (sha256 pinned and compared to an independent download) |
| Archive entry names -> javap / shell | Class entry names come from a zip and reach a command line | Entry names (whitelisted) |
| Hook / classifier -> every commit | Fail-open / fail-always bug would weaken governance | Commit lane decisions |
| Gate exit code -> v2.5.0 cut decision | A vacuous pass would ship an immutable tag with a missing descriptor | Exit codes |
| Local gated commit -> annotated tag -> origin | Publishes an immutable public coordinate | Git objects (138 commits, secret-scanned) |
| origin -> JitPack remote build | JitPack builds from GitHub; observed over HTTPS | Build status |
| This repo -> orchestrator session | Ledger row relay only; contract file never edited here | Relay file |
| Working tree -> commit | Dirty unrelated files must not leak into evidence commit | Named-file commits |

---

## Threat Register

| Threat ID | Category | Component | Severity | Disposition | Mitigation | Status |
|-----------|----------|-----------|----------|-------------|------------|--------|
| T-19-01 | Tampering (false pass) | verify-binary-abi.sh normalization | high | mitigate | Sanity floor MIN_LINES, hard exit 2 on javap stderr/non-zero; swapped-roles control exits 3 | closed |
| T-19-02 | Tampering | Baseline AAR (cache poisoning / MITM) | high | mitigate | HTTPS-only, sha256 printed, BASELINE_AAR no fall-through; independent JitPack download hash matches | closed |
| T-19-03 | Tampering / EoP | Archive entry names, args | high | mitigate | Single named member into mktemp -d, class-name whitelist, strict tag pattern + refs/tags verification | closed |
| T-19-04 | DoS / Repudiation | Hook + classifier unwiring | high | mitigate | Files changed together, fail-closed and GOV-03 cases kept, run-all.sh green, real classifier exit 0 | closed |
| T-19-05 | Repudiation | Raw-line check removed | medium | accept | Replaced by Metalava apiCheck + javap binary gate (DEC-1) | closed |
| T-19-06 | Info disclosure | Temp files / logs | low | accept | mktemp -d with trap; .gate/ never tracked | closed |
| T-19-07 | Tampering | v2.5.0 tag integrity | high | mitigate | Single-refspec push; no move/delete; remote peeled SHA == GATED_HEAD | closed |
| T-19-08 | Tampering / Repudiation | Tagging an unverified commit | high | mitigate | GATED_HEAD file, tag by SHA, no commit between record and tag | closed |
| T-19-09 | Tampering | False additive claim | critical | mitigate | Swap-baseline Metalava + javap ABI gate fresh on GATED_HEAD (751 tests, missing=0); hardened gate re-run on JitPack AAR | closed |
| T-19-10 | Tampering | Poisoned baseline AAR | high | mitigate | SEAMS: none; baseline sha256 equals independent HTTPS download | closed |
| T-19-11 | Info disclosure | Secrets published via tag | high | mitigate | Secret-pattern scan 0 hits (net diff and full 138-commit history) | closed |
| T-19-12 | Tampering | Stray marker tag | high | mitigate | git.create_tag false; v2.5* exactly v2.5.0 local and remote | closed |
| T-19-13 | Repudiation | Cut declared before JitPack built | medium | mitigate | pom 200, aar 200, status ok, isTag true, commit == GATED_HEAD | closed |
| T-19-14 | Tampering | api.txt left swapped | medium | mitigate | api.txt sha256 equals head; clean git status; evidence commit two named files | closed |
| T-19-15 | EoP / Integrity | Ledger write / other repos | medium | mitigate | Relay-bannered row file only; contract file and consumers untouched | closed |
| T-19-16 | Spoofing | Wrong/retired orchestrator | low | mitigate | Orchestrator resolved from effort.json; both names in row; no fabricated message | closed |
| T-19-SC | Tampering (supply chain) | Package installs | low | accept | No dependency or build-file change this phase | closed |

*Status: open · closed · open — below {block_on} threshold (non-blocking)*

---

## Accepted Risks Log

| Risk ID | Threat Ref | Rationale | Accepted By | Date |
|---------|------------|-----------|-------------|------|
| AR-19-01 | T-19-05 | Raw-line api.txt check was inert in the hook and false-positive when fixed; replaced by stronger Metalava + javap signals (DEC-1) | plan 19-01 (pre-approved in PLAN threat model) | 2026-10-05 |
| AR-19-02 | T-19-06 | No secrets handled; temp dirs trap-cleaned; .gate/ never staged | plan 19-01 | 2026-10-05 |
| AR-19-03 | T-19-SC | No package installs in this phase | plans 19-01, 19-02 | 2026-10-05 |

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-10-05 | 17 | 17 | 0 | gsd-security-auditor (SECURED, ASVS 1, block_on high) |

Informational: review-fix commits (40d8da6..4447675) hardened tools/ after the tag; the hardened gate was re-validated on the JitPack-published AAR (missing=0). Ledger relay to orchestrator is an operational follow-up, not an open threat.

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed
- [x] `status: verified` set in frontmatter

**Approval:** verified 2026-10-05
