---
phase: 19-cut-v2-5-0
plan: 02
subsystem: release
tags: [jitpack, git-tag, binary-abi, metalava, section-11]

requires:
  - phase: 19-cut-v2-5-0
    provides: tools/verify-binary-abi.sh (Plan 19-01, D-03)
  - phase: 18-catalog-integrity-api-dump-docs
    provides: green Phase 18 artifact
provides:
  - immutable annotated tag v2.5.0 on GATED_HEAD 7101516e, pushed and built by JitPack
  - 19-SHIP-GATE-EVIDENCE.md (verbatim fresh gate transcripts)
  - 19-SHIP-LEDGER-ROW.md (relay-bannered section 11 row for the orchestrator)
affects: [consumer repins SecondBrain/CalTracker (Wave-1), orchestrator ledger]

requirements-completed: [SHIP-03]
actuals:
  tokens: 9000
  tasks: 3
  commits: 1
plan_head_before: 7101516e089964b3fd45696bdeb4082d4f8c5580

tech-stack:
  added: []
  patterns: [gate-then-cut single task, scratch .gate/ dir for state across fresh shells, post-tag evidence commit]

key-files:
  created:
    - .planning/phases/19-cut-v2-5-0/19-SHIP-GATE-EVIDENCE.md
    - .planning/phases/19-cut-v2-5-0/19-SHIP-LEDGER-ROW.md
  modified: []

key-decisions:
  - "D-01 waiver honored: cut ran autonomously on green gates, no checkpoint before the one-way push"
  - "DEC-5: cut done in-repo (xrepo not on PATH here); manual annotated tag by SHA + tag-only push"
  - "DEC-2: no cross-session messaging tool, so no build-window notices or relay sent; ledger row file is the relay"
  - "DEC-3: evidence + ledger row committed after the tag; tag points at GATED_HEAD, evidence commit is a descendant"

coverage:
  - id: S1
    description: "Fresh battery, swap-baseline Metalava vs v2.4.1, run-all.sh and the D-03 ABI gate all green on GATED_HEAD before the tag"
    requirement: SHIP-03
    verification:
      - kind: integration
        ref: "battery BUILD SUCCESSFUL (751 tests, 0 fail/err, 77/77 XML fresh); swap-baseline BUILD SUCCESSFUL; run-all 4/4 ok; abi rc 0 SEAMS none missing=0"
        status: pass
    human_judgment: false
  - id: S2
    description: "Annotated v2.5.0 on GATED_HEAD pushed alone; exact tag set; create_tag false"
    requirement: SHIP-03
    verification:
      - kind: integration
        ref: "git ls-remote --tags origin 'v2.5.0^{}' == GATED_HEAD; git tag -l 'v2.5*' == v2.5.0"
        status: pass
    human_judgment: false
  - id: S3
    description: "JitPack resolves v2.5.0 and the published AAR keeps every v2.4.1 descriptor"
    requirement: SHIP-03
    verification:
      - kind: integration
        ref: "pom 200, aar 200, status ok, isTag true, commit == GATED_HEAD (round 2); published-AAR ABI rc 0 missing=0"
        status: pass
    human_judgment: false

duration: 45min
completed: 2026-10-05
status: complete
---

# Phase 19 Plan 02: Cut v2.5.0 Summary

**Annotated tag `v2.5.0` cut by SHA on the commit that passed a fresh battery, the v2.4.1 swap-baseline Metalava check, run-all.sh and the zero-missing binary ABI gate; JitPack built it and the published AAR re-passed the ABI check.**

## Result

- **GATED_HEAD:** `7101516e089964b3fd45696bdeb4082d4f8c5580` (tag object `07e89727381157fbd49e0bfeb01bedfca5f96fc0`). Coordinate: `com.github.Ygaray:yahirandroidtaste:v2.5.0`.
- **Task 1 (gate-then-cut):** preconditions met (0 secret-pattern hits across 138 commits / 35299 added lines; baseline AAR sha256 `df21a126...` equals the independent JitPack download). apiDump idempotent; swap-baseline `metalavaCheckCompatibilityRelease --rerun` executed (not UP-TO-DATE) and BUILD SUCCESSFUL, api.txt restored byte-identical. Battery BUILD SUCCESSFUL in 6m35s: 751 tests (22 skipped), 0 failures/errors, 77/77 JUnit XML newer than the marker, detekt baseline still empty and unchanged. `run-all.sh` 4/4 ok. `tools/verify-binary-abi.sh v2.4.1` with no seam: `SEAMS: none`, `HEAD=` equals GATED_HEAD, `base=2526 head=2584 missing=0`, rc 0. HEAD re-asserted, then `git tag -a v2.5.0 <GATED_HEAD>` and `git push origin refs/tags/v2.5.0` (dry run first). Peeled remote SHA equals GATED_HEAD; local and remote v2.5* sets are exactly v2.5.0. No commit was made between recording GATED_HEAD and the tag.
- **Task 2 (JitPack):** round 2 of the poll passed: pom 200, aar 200, `status: ok`, `isTag: true`, `commit` equals GATED_HEAD. Non-gating published-AAR ABI check (sha256 `d83212f7...`): rc 0, missing=0, so no corrective v2.5.1 is indicated.
- **Task 3 (evidence):** `19-SHIP-GATE-EVIDENCE.md` and `19-SHIP-LEDGER-ROW.md` committed by name after the tag (`2cc56c8`, hook reported LANE 1, baseline v2.5.0). Every path changed since GATED_HEAD is under `.planning/`.

## Commits

| Commit | Message |
|--------|---------|
| 2cc56c8 | docs(19): record v2.5.0 ship-gate evidence and ledger row |

Tasks 1 and 2 changed no tracked file by design (the tag push is a ref, not a commit).

## Deviations from Plan

None. No gate was red, no waiver was needed, nothing was retried.

## Relay and coordination notes (needs orchestrator action)

- **Ledger relay not sent.** This executor has no cross-session messaging tool, so the orchestrator (`yahir-gsd-control-plane-3b`, resolved from effort.json; D-02's `-6e` is the pre-restart session) was not messaged, and no xrepo build-window notice went out. `xrepo` is also not on PATH in this repo. Per DEC-2 this is a soft outcome, and no confirmation is claimed. The orchestrator must pick up `.planning/phases/19-cut-v2-5-0/19-SHIP-LEDGER-ROW.md` and run its own `xrepo ledger-row`. The section 11 ledger was never written here (A14).
- **Remote push scope.** origin HEAD was an ancestor of GATED_HEAD, so pushing the tag also uploaded the unpushed commits behind it (138 commits since v2.4.1). `main` itself was not pushed; origin/main is unchanged.
- **Deferred (out of scope):** consumer repins (SecondBrain required, CalTracker optional) and the stale root CLAUDE.md release line (needs Yahir's OK).
- The repo remains dirty with unrelated pre-existing `.planning` files and untracked dirs; none were staged.

## Self-Check: PASSED

Files found: 19-SHIP-GATE-EVIDENCE.md, 19-SHIP-LEDGER-ROW.md. Commit 2cc56c8 exists. Tag v2.5.0 resolves to GATED_HEAD locally and on origin; `git tag -l 'v2.5*'` is exactly v2.5.0; `create_tag` is false.
