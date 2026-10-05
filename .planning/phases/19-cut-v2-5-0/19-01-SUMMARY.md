---
phase: 19-cut-v2-5-0
plan: 01
subsystem: tooling
tags: [javap, binary-abi, shell-tests, git-hooks, metalava]

requires:
  - phase: 18-catalog-integrity-api-dump-docs
    provides: green Phase 18 artifact (the AAR this gate measures; no src change here)
provides:
  - tools/verify-binary-abi.sh javap AAR-diff gate (D-03), proven on the real v2.4.1 baseline
  - lane pipeline reduced to lane 1 / lane 2 (raw-line api.txt check unwired)
  - docs pointing at the real binary gate
affects: [19-02 cut v2.5.0]

requirements-completed: [SHIP-03]
actuals:
  tokens: 10572
  tasks: 3
  commits: 4
plan_head_before: 1f372e95b6aa0614118a38b21fcdd8308ffbb21a

tech-stack:
  added: []
  patterns: [offline javac+jar fixture AARs, RED-first shell test, SEAMS line proving no test seam in a cut]

key-files:
  created:
    - tools/verify-binary-abi.sh
    - tools/test/test-verify-binary-abi.sh
  modified:
    - tools/classify-hub-change.sh
    - tools/hooks/pre-commit
    - tools/test/test-classify-hub-change.sh
    - tools/test/test-precommit-hook.sh
    - tools/README-api-guard.md
    - API.md
  deleted:
    - tools/verify-api-additive.sh
    - tools/test/test-verify-api-additive.sh

key-decisions:
  - "DEC-1: git rm the raw-line api script and its test after the classifier stopped calling it"
  - "DEC-7: hook override arm narrowed to lane 2 only (HUB_LANE_OVERRIDE=3 now falls into fail-closed)"
  - "DEC-8: single SEAMS line (none or the set seams) printed first so cut evidence proves a seam-free run"
  - "DEC-9: class-entry whitelist allows an inner hyphen, never a leading hyphen (javap option injection)"

coverage:
  - id: D1
    description: "tools/verify-binary-abi.sh exists (100755), is tested offline, and passes on the real v2.4.1 baseline with missing=0 and no seam"
    requirement: SHIP-03
    verification:
      - kind: unit
        ref: "bash tools/test/test-verify-binary-abi.sh (PASS=26 FAIL=0)"
        status: pass
      - kind: integration
        ref: "tools/verify-binary-abi.sh v2.4.1 -> SEAMS: none, base=2526 head=2584 missing=0, rc 0"
        status: pass
    human_judgment: false
  - id: D2
    description: "The gate can fail: swapped-roles negative control exits 3 listing 70 missing descriptors"
    requirement: SHIP-03
    verification:
      - kind: integration
        ref: "BASELINE_AAR=<HEAD aar> HEAD_AAR=<v2.4.1 aar> SKIP_BUILD=1 tools/verify-binary-abi.sh v2.4.1 -> rc 3"
        status: pass
    human_judgment: false
  - id: D3
    description: "Raw-line api.txt check unwired from classifier, hook and tests; hook still blocks lane 2 and fails closed"
    requirement: SHIP-03
    verification:
      - kind: unit
        ref: "bash tools/test/run-all.sh (all ok); real classifier --baseline v2.4.1 -> LANE 1, rc 0"
        status: pass
    human_judgment: false
  - id: D4
    description: "README-api-guard.md and API.md binary-compatibility rule name the real command, exit codes and run-before-tagging"
    requirement: SHIP-03
    verification:
      - kind: other
        ref: "grep checks from the plan verify block (retired names absent, command present, no API.md hunk before line 343)"
        status: pass
    human_judgment: false

duration: 75min
completed: 2026-10-05
status: complete
---

# Phase 19 Plan 01: Binary ABI gate Summary

**tools/verify-binary-abi.sh (javap descriptor diff of the release AAR vs the previous tag) added test-first and proven on the real v2.4.1 baseline (base=2526, head=2584, missing=0), with the dead raw-line api.txt check removed from the whole lane pipeline.**

## What was done

- **Task 1 (tracer).** RED fixture test committed first (it failed naming the missing script), then the script. The test builds fixture AARs offline with javac + jar and covers cases (a)-(l): identical, additive, removed method, ComposableSingletons loss, `*_Factory` loss, sanity floor, bad/missing/hyphen tag, unobtainable baseline (missing override file; no cache plus dead JitPack port), changed descriptor, hostile entry name, corrupt class, SEAMS line. Final count PASS=26 FAIL=0.
- **Real runs.** `tools/verify-binary-abi.sh v2.4.1` with no seam: `SEAMS: none`, `HEAD=290be75...`, `HEAD_AAR sha256=27b6fd79...`, `BASELINE_AAR sha256=df21a126... source=gradle-cache` (matches the hash recorded at research time), `base=2526 head=2584 filtered_ComposableSingletons=12 missing=0`, rc 0. Swapped-roles negative control: rc 3 with 70 missing descriptors.
- **Task 2.** Classifier emits lane 1/2 only; hook export removed and override arm narrowed to lane 2; both tests rewritten (api-line removal inert, lane-2 block/override, GOV-03 regression kept, fail-closed on a missing sub-guard). Real `classify-hub-change.sh --baseline v2.4.1` now exits 0 (LANE 1) instead of 1.
- **Task 3.** `git rm` of the retired script and test; README gained a "Binary ABI gate" section and lost every reference to the retired script and the lane-3 override; API.md rule section names `tools/verify-binary-abi.sh <previous-tag>`. Earlier API.md compatibility sentences were left untouched (first changed line is 350).

## Commits

| Commit | Message |
|--------|---------|
| 290be75 | test(19-01): add failing fixture test for the binary ABI gate |
| dea4fae | feat(19-01): add tools/verify-binary-abi.sh javap AAR-diff gate (D-03) |
| 9f2a98a | refactor(19-01): unwire the raw-line api.txt check from the lane pipeline (D-03) |
| 2e7e44c | docs(19-01): retire raw-line api check, document the binary ABI gate (D-03) |

All four commits reported `LANE 1` from the installed pre-commit hook; no `HUB_LANE_OVERRIDE` was used.

## Deviations from Plan

**1. [Rule 1 - Bug] Fixture assertion used the wrong normalized form.** Found during Task 1 GREEN. The plan text writes the descriptor as `p.Alpha#a ()V`, but the proven awk (kept byte-for-byte, it yields base=2526) concatenates member and descriptor, giving `p.Alpha#a()V`. Fixed the test's regex to the real form; the script was not changed. Folded into the Task 1 GREEN commit (dea4fae).

**2. [Minor hardening] `LC_ALL=C` on the normalization sort and the `comm`.** Added so the diff does not depend on the caller's locale. Verified it did not change the real counts (still 2526 / 2584).

**Total deviations:** 1 auto-fixed (Rule 1), 1 hardening note. **Impact:** none on behavior or scope.

## Verification

Plan-level checks re-run at the end: `bash tools/test/run-all.sh` exits 0 with every test ok (classify, precommit, additive-diff, verify-binary-abi; the retired test is gone); no match for the retired path variable or script in the hook, classifier or rewritten tests; only the ten listed files changed since the start commit `1f372e9`; no `v2.5*` tag exists; no change under src/, build.gradle.kts, api.txt, jitpack.yml, config/ or the root CLAUDE.md.

## Notes

- The final metadata commit stages `.planning/STATE.md`, which already carried unrelated uncommitted edits from the milestone driver; those are swept into that docs commit as a side effect of updating STATE.md. Other dirty `.planning` files and untracked dirs were never staged.
- No tag was cut and the release battery was not run; Plan 19-02 owns the cut. The exact invocation proven here (`tools/verify-binary-abi.sh v2.4.1`, no seam) is the one 19-02 runs on `GATED_HEAD`.
- `build/outputs/aar/yahirandroidtaste-release.aar` was rebuilt by the gate run (untracked build output).

## Self-Check: PASSED

Files found: tools/verify-binary-abi.sh, tools/test/test-verify-binary-abi.sh (both 100755), and the edited files; the two retired files are absent. Commits 290be75, dea4fae, 9f2a98a, 2e7e44c exist.
