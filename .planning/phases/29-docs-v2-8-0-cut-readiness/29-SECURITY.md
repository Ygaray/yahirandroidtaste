---
phase: "29"
slug: "docs-v2-8-0-cut-readiness"
status: verified
threats_open: 0
asvs_level: 1
audited_head: a00a2d32c1e21ee47b64718d72f28779b1aa4b79
created: "2026-10-18"
---

# Phase 29 - Security

> Per-phase security contract: threat register, accepted risks, and audit trail. Audited by gsd-security-auditor (ASVS L1, block_on high) over plans 29-01 to 29-05 and 29-07. Plan 29-06 threats were re-verified after that plan executed (second pass, 47 of 47 closed).

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| repo to PUBLIC remote | unpushed range `origin/main..HEAD` ships to a public repo | tailnet IPs, home paths, keys (scanned, never echoed) |
| agent to TESTER device | adb to the Gate-1 phone R5CT10XNKQN over USB | device settings, throwaway harness package |
| docs to integrators | API.md / INTEGRATION.md / STYLE.md describe the opaque FeedbackTarget key and OSM tile policy | opaque key, user-agent string |
| library internals to consumers | internal fix for the stacked card must not change the public API | api.txt, source compatibility |

---

## Threat Register

| Threat ID | Category | Component | Severity | Disposition | Mitigation | Status |
|-----------|----------|-----------|----------|-------------|------------|--------|
| T-29-01 | Info disclosure | docs on the opaque FeedbackTarget key | medium | mitigate | API.md and INTEGRATION.md say the key is never logged or parsed; FeedbackTarget.toString omits the key | closed |
| T-29-02 | Tampering | docs drifting from the API | medium | mitigate | CatalogDocsDriftGuardTest ties v2.8.0 symbols to API.md and api.txt | closed |
| T-29-03, T-29-09, T-29-12, T-29-18, T-29-38 | Info disclosure | debris swept into a commit (plans 01, 02, 03, 04, 07) | high | mitigate | explicit-path commits; range check shows no state.json, config.json, graphs, WAITING, .gate or stage markers in 732ea62..HEAD | closed |
| T-29-04 | Repudiation | stale or vacuous guard run | medium | mitigate | RED output recorded before docs, main checkout only | closed |
| T-29-05 | Info disclosure | INTEGRATION snippet logging the key | medium | mitigate | snippet has no log call; "never log the key" stated (note: the guard checks consumer names and focus phrases, not log calls) | closed |
| T-29-06 | Tampering | non-idempotent handler / unsafe key parsing | medium | mitigate | snippet ends `else -> Unit`; idempotency rule documented | closed |
| T-29-07 | Info disclosure | consumer name in hub docs | low | mitigate | drift guard scans v2.8.0 blocks for consumer names | closed |
| T-29-08 | Spoofing | tile User-Agent fallback docs | low | mitigate | docs match PlaceMapOsmdroidConfig behavior | closed |
| T-29-10 | Tampering | doc claiming the refusal moves focus | medium | mitigate | STYLE.md says the library never moves focus; guard forbids the phrases | closed |
| T-29-11 | Repudiation | TalkBack steps presented as verified | medium | mitigate | physical steps listed as Gate-2 owner steps in STYLE.md and the uat-pending fragment | closed |
| T-29-13 | Info disclosure | internal addresses, home paths or keys to the PUBLIC repo | high | mitigate | tools/verify-publish-range.sh scans four classes over net and history; fixture address replaced | closed |
| T-29-14 | Info disclosure | scan echoing a matched credential | high | mitigate | tool prints class, path, count only; fixture asserts no credential text on stdout | closed |
| T-29-15 | Repudiation | vacuous scan / stale test result | high | mitigate | positive controls per class (exit 2 on failure); verify-junit-fresh fails on stale or zero-run classes | closed |
| T-29-16 | Tampering | `--require` / ref smuggling shell syntax | medium | mitigate | allow-list regex and `git rev-parse --verify` | closed |
| T-29-17 | Info disclosure | tool or fixture text matching its own classes | medium | mitigate | patterns built from fragments; fixture self-scan | closed |
| T-29-19 | Tampering | adb reaching the personal phone | high | mitigate | USB serial only; verify-tester-restored refuses non-serial arguments; command guard 0 breaches | closed |
| T-29-20 | Tampering | tester left altered or data destroyed | high | mitigate | density 450 restored (never reset), RESTORED=yes twice; stay_on_while_plugged_in disclosed for the owner | closed |
| T-29-21 | Repudiation | Gate-1 waived / hand-written / partly skipped | high | mitigate | two signed uat-run directories; first-run FAIL kept; Addendum all_pass | closed |
| T-29-22 | Info disclosure | screenshots / dumps with personal content | medium | mitigate | text evidence scanned clean; harness-only screens | closed |
| T-29-23 | Tampering | stale mavenLocal AAR | high | mitigate | AAR md5 differs between runs; republished at the tested src | closed |
| T-29-24, T-29-39 | Elevation | accidental tag, push or repin | high | mitigate | no v2.8 tag; remote unchanged | closed |
| T-29-25 | Tampering | evidence commit sweeping unrelated dirs | medium | mitigate | only the two referenced run dirs committed | closed |
| T-29-26 | DoS | driving the shared tester while in use | medium | mitigate | leases taken first, released after; second run queued behind another seat | closed |
| T-29-34 | Tampering | public API changed by an internal fix | high | mitigate | api.txt byte-identical; new symbols internal; canary and legacy-path contracts green | closed |
| T-29-35 | DoS | verticalScroll on infinite max height | high | mitigate | scroll only when `hasBoundedHeight` and not collapsedBack; tests | closed |
| T-29-36 | Tampering | gesture regression on the scrolling card | medium | mitigate | FeedbackCardTallContentTest covers drag, swipe, tap, hold, pause, semantics | closed |
| T-29-37 | Repudiation | stale Gradle result as proof | high | mitigate | forced-fresh run, no UP-TO-DATE on the gate tasks, JUNIT-FRESH OK | closed |
| T-29-40 | Repudiation | green Robolectric read as a Gate-1 pass | medium | mitigate | Hand-over says it is not a pass; Addendum from a signed run exists | closed |
| T-29-SC (plans 01-05, 07) | Tampering | package installs | high | accept | no dependency file changed over 732ea62..HEAD; harness is a throwaway outside the repo | closed (accepted) |
| T-29-27 | Info disclosure | addresses, home paths or keys to the PUBLIC repo (29-06) | high | mitigate | full-history scan origin/main..GATED_HEAD, classes and counts only, 0 credential hits, history residue disclosed | closed |
| T-29-28 | Elevation | agent tag, push or repin (29-06) | high | mitigate | no v2.8 tag local or remote; remote main unchanged; create_tag false | closed |
| T-29-29 | Tampering | wrong tag target or code change after GATED_HEAD | high | mitigate | commits after ce855d2 touch only .planning; src, api.txt, build.gradle.kts identical to the Gate-1 tested tree | closed |
| T-29-30 | Repudiation | stale Gradle result as proof (29-06) | high | mitigate | forced-fresh run on GATED_HEAD, 43 of 43 tasks executed, JUNIT-FRESH OK | closed |
| T-29-31 | Tampering | supply-chain drift | high | mitigate | build and dependency files identical to v2.7.1 | closed |
| T-29-32 | Info disclosure | tag push publishes unreviewed commits | medium | mitigate | full 314-commit range listed in the proposal and range file | closed |
| T-29-33 | Info disclosure | debris swept into a commit (29-06) | high | mitigate | explicit-path commits; no debris in the phase window | closed |
| T-29-SC (plan 29-06) | Tampering | package installs | high | accept | no dependency change in plan 29-06 | closed (accepted) |

*Status: open, closed. Only open threats at or above block_on count toward threats_open.*

---

## Accepted Risks Log

| Risk ID | Threat Ref | Rationale | Accepted By | Date |
|---------|------------|-----------|-------------|------|
| AR-29-01 | T-29-SC (plans 01-05, 07) | no package or dependency changes in the phase; the Gate-1 harness is a throwaway built outside the repo | gsd-security-auditor (premise verified) | 2026-10-18 |
| AR-29-02 | T-29-13 (history residue) | commit 32bd7d5 (phase 28, unpushed) still carries a tailnet address in a test fixture in history; the tree no longer does. History is not rewritten; the director decides at the push ask | disclosed in 29-04-SUMMARY, carried to the 29-06 proposal | 2026-10-18 |
| AR-29-03 | T-29-SC (plan 29-06) | no package or dependency changed in plan 29-06; build files identical to v2.7.1 | gsd-security-auditor (premise verified) | 2026-10-18 |

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-10-18 | 39 (plans 01-05, 07) | 39 | 0 | gsd-security-auditor |
| 2026-10-18 | 47 (adds plan 29-06 threats, re-audit at a00a2d3 .. HEAD) | 47 | 0 | gsd-security-auditor |

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed
- [x] `status: verified` set in frontmatter

**Approval:** verified 2026-10-18
