---
phase: 29-docs-v2-8-0-cut-readiness
plan: 06
subsystem: release
tags: [release-flip, gate-battery, publish-scan, proposal]
status: complete
requires:
  - phase: 29-docs-v2-8-0-cut-readiness
    provides: plans 29-01 to 29-05 and 29-07 (docs, guards, tools, Gate-1, the capped-Undo and scrolling-card fix)
provides:
  - "Release-flip commit ce855d2 (GATED_HEAD): README.md and CLAUDE.md at v2.8.0"
  - "Forced-fresh gate battery recorded on GATED_HEAD"
  - "29-PROPOSAL.md and 29-PROPOSAL-RANGE.txt (owner-gated v2.8.0 cut proposal)"
affects: [milestone master push and tag ask, SHIP-05 closure by phase.complete]
plan_head_before: a29a6cd2adcdc2abce2b8e74cb0d8c38874777d5
actuals:
  tokens: 18000
  tasks: 3
  commits: 3
tech-stack:
  added: []
  patterns:
    - "Gate record folded into the proposal; scan verdict matched on exit code and ^RESULT OK"
key-files:
  created:
    - .planning/phases/29-docs-v2-8-0-cut-readiness/29-PROPOSAL.md
    - .planning/phases/29-docs-v2-8-0-cut-readiness/29-PROPOSAL-RANGE.txt
  modified:
    - README.md
    - CLAUDE.md
key-decisions:
  - "Proposed tag target is GATED_HEAD ce855d2, not the later artifact commit (PD-1)"
  - "WR-01 (FeedbackStackHost under IntrinsicSize parent throws) and review items IN-01, IN-02, IN-05 are handed to the director, not fixed, because any src/main edit would invalidate the Gate-1 device evidence"
requirements-completed: []
duration: 55 min
completed: 2026-10-18
---

# Phase 29 Plan 06: Release flip, forced-fresh gate battery, publish scan and the v2.8.0 proposal Summary

README.md and CLAUDE.md flipped to v2.8.0 in the last non-.planning commit (`ce855d2`), the full gate battery passed forced-fresh on exactly that commit (1943 tests, 0 failures, detekt zero baseline, additivity OK against v2.7.1), the 314-commit unpushed range was scanned (0 credentials, 0 shipped-path hits) and the owner-gated proposal is written; nothing was tagged, pushed, repinned or sent.

## Tasks

| Task | Commit | What |
|------|--------|------|
| 1 | `ce855d2` | `docs(29-06): current release v2.8.0 (D-02)`: README.md (callout and sample coordinate, both were still v2.6.0) and CLAUDE.md (v2.8.0 plus the v2.8 descriptor). GATED_HEAD. LANE 1. |
| 2 | none (verification only) | Forced-fresh battery on GATED_HEAD, notes in untracked `.gate/battery-notes.txt` |
| 3 | `f3655ad` | `docs(29-06): v2.8.0 cut proposal, range and scan result (D-01)`: 29-PROPOSAL.md (12 sections) and 29-PROPOSAL-RANGE.txt (314 lines) |

## Gate battery on GATED_HEAD (`ce855d2e6b59faf610f015472803e0e9b1dae4f4`)

- `apiDump` alone: rc 0, `api.txt` unchanged.
- `testDebugUnitTest detekt apiCheck --rerun-tasks --no-build-cache --no-daemon`: BUILD SUCCESSFUL in 3m 25s, 43 of 43 tasks executed, no UP-TO-DATE or FROM-CACHE on the gate tasks.
- `verify-junit-fresh.sh`: FILES 182, STALE 0, tests 1943, skipped 22, failures 0, errors 0, JUNIT-FRESH OK, all 13 required classes ran (1943 = 1942 of plan 29-07 plus the one test the review fix added).
- `run-all.sh` rc 0 (last suite PASS=82 FAIL=0); additivity: `SEAMS: none`, `API SOURCE-ADDITIVE OK ... accepted_exceptions=0`, numstat `78 11`; binary ABI informational exit 3 (37 lane-3 lines); INV-03 greps and the configuration diff against v2.7.1 clean.
- Confinement: `src/main`, `api.txt`, `build.gradle.kts` identical to `0bad7b6` (the tree the Gate-1 SC3 Addendum tested); `src/main` vs `55f5288` is exactly the five plan 29-07 files; `api.txt` equals `c449db0`. HEAD equalled GATED_HEAD before and after.

## Scan (origin/main..GATED_HEAD, then re-run at the final HEAD)

`SUMMARY net_credential=0 net_shipped=0 net_planning=48 history_credential=0 history_shipped=1 history_planning=29 outside=0 binary=188 fastforward=yes`, `RESULT OK (history_shipped=1 binary=188 not enforced)`, four CONTROL lines ok, exit 0. Classes by net added lines: tailnet addresses 9 lines in 7 files, host suffix 5 in 4 files, home path 34 in 17 files, all in `.planning/` prose and evidence logs (reviewed and accepted, not scrubbed); credentials 0. History residue in shipped paths: one commit (`32bd7d5`, a tailnet address in a test fixture, neutralized in the tree by plan 29-04). 188 binary files unscanned (186 png, 2 harness cache dbs). The final re-run at HEAD `f3655ad` gives the identical SUMMARY line (315 commits, the proposal files add no hits).

## Descendant check, tag and remote

- `git diff --name-only ce855d2 HEAD` at the proposal commit printed only the two proposal paths under `.planning/` (PD-2 holds; the summary and the STATE and ROADMAP commits add `.planning` paths only).
- `git tag --list 'v2.8*'` prints nothing; `git ls-remote origin refs/heads/main` is still `c2182ecf92d3a4176466697a907dca689c6d97f2`, equal to the value recorded before the battery.
- Final HEAD at proposal commit: `f3655ad6ca2a69cdcaf4a345a8d58f2a5eab682b` (the summary commit follows it).

## Discrepancies for the master

1. Success row title: code uses `targetLabel` (fallback `summary`) plus optional `targetDetail`, not `summary` (brief D-15).
2. POL-03 Log.w: warns when the sanitized agent is blank or starts with the placeholder package prefix, once per picker instance; a bare non-template package expands silently.
3. Placeholder tile purge: scheduled on the first call of every process and while the install flag is unset (not "once per install"); matches size and md5, never wipes the cache.
4. The library manifest declares no INTERNET; the host does.
5. Refusal: one Assertive live region after the sheet settles, no focus request (the 28-11 route is superseded); 27 of 27 spoken once.
6. Collapsed deck cards are named from message plus second line, not `item.message` alone.
7. `UndoHistoryStore.append`: `groupId: String? = null`, `target` without default; extra source-true behaviors documented.
8. STATE.md's "4 docs commits" is stale; the range is 314 commits (315 with the proposal).
9. `apiCheck` alone only fails on removals; additions-only is carried by the additivity tool and the call-site canary.
10. Gate-1 diagnosis vs code: the deck gave every card an infinite max height (vertical scroll around the layout); the card was never limited.
11. Known limitation (WR-01, not fixed): `FeedbackStackHost` uses `BoxWithConstraints` (deck since Phase 26), so hosting it under an `IntrinsicSize` parent throws; deferred IN-01, IN-02, IN-05. Director choice: accept and document, or fix in v2.8.1.

## Deviations from Plan

**[Rule 3 - Blocking, narrowed check] Task 1 and Task 2 confinement diffs** - Found during: Task 1 precondition. The plan's `git diff --exit-code PLAN07_SRC_HEAD HEAD -- src api.txt tools build.gradle.kts` is non-empty because the code review fix pass (commits 3ad051f, d08d8a1, 06b1410, 6a45421) changed `tools/` and one test file after the device run. As the dispatch brief stated, the check was narrowed to `src/main`, `api.txt` and `build.gradle.kts` (empty diff, so the Addendum's device evidence still applies to the shipped library tree) and the four changed `tools/` files plus the one test were listed in the proposal. No file was edited to make it pass.

**[Output token change] verify-publish-range.sh verdict** - The tool now ends with `RESULT OK (history_shipped=N binary=M not enforced)` plus CAVEAT lines; matched on exit code and `^RESULT OK`, CAVEAT lines quoted in the proposal.

**Total deviations:** 1 narrowed check, 1 output-format adaptation, 0 code changes. **Impact:** none on the gate; both are recorded in the proposal.

## Issues Encountered

None. Pre-commit hooks passed on both task commits (LANE 1). No hook was bypassed.

## Hand-over

The milestone master relays `29-PROPOSAL.md` (with `29-PROPOSAL-RANGE.txt`) to director@office with teamlead@vae-bilingual in cc and sends the push and tag ask itself; this plan sent nothing, tagged nothing, pushed nothing and repinned no consumer. SHIP-05 is left for the orchestrator's `phase.complete`.

## Self-Check: PASSED

- 29-PROPOSAL.md, 29-PROPOSAL-RANGE.txt FOUND; commits `ce855d2` and `f3655ad` FOUND; 12 `##` sections, `git tag -a v2.8.0` text, harness-app list and `CappedPillShape` present; no tailnet address, home path or host suffix in the committed proposal files (re-scan at HEAD: 0 new hits).
