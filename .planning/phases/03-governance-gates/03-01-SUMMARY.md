---
phase: 03-governance-gates
plan: 01
subsystem: infra
tags: [bash, git-hooks, pre-commit, governance, testing]

# Dependency graph
requires: []
provides:
  - "verify-additive-diff.sh diffs the staged index vs HEAD (scoped to src/main), not a stale
    release tag vs the whole working tree — a .planning/-only commit no longer inherits any prior
    src/main rewrite's lane-2 classification"
  - "End-to-end pre-commit-hook regression proving the exact reproduced bug shape (5b01532-then-
    unrelated-commit) is fixed"
  - "Live-proven, tracked residual-risk characterization of verify-api-additive.sh's identical
    latent architecture flaw, for Phase 5 to pick up at its first tag-cut that includes api.txt"
affects: [phase-05-gardening]

# Actuals (#2632)
actuals:
  tokens: 1967
  tasks: 3
  commits: 3

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Staged-vs-HEAD diff basis for pre-commit additive-guard classification (D-01) — per-commit,
      not cumulative-since-last-tag"
    - "git reset --hard (not git checkout -- .) for inter-case cleanup in bash fixture tests once
      a case stages via git add -A — checkout -- . does not unstage the index"

key-files:
  created: []
  modified:
    - tools/verify-additive-diff.sh
    - tools/test/test-verify-additive-diff.sh
    - tools/test/test-precommit-hook.sh
    - tools/test/test-verify-api-additive.sh

key-decisions:
  - "Followed D-01 literally: fixed only verify-additive-diff.sh's comparison basis (src/main
    scope); verify-api-additive.sh's identical bug is proven live and tracked as a residual risk
    for Phase 5, not fixed this phase (RESEARCH.md Open Question 1 recommendation)"
  - "STATE.md's Blockers/Concerns note for the residual risk is NOT committed by this worktree
    executor — worktree isolation forbids touching STATE.md/ROADMAP.md. The exact text to append
    is provided below for the orchestrator to apply post-merge."

patterns-established:
  - "Bash fixture tests that stage edits via git add -A must reset with git reset --hard between
    cases, not git checkout -- . (which only syncs the working tree to the INDEX, never unstages
    it) — this was itself a Rule-1 bug this plan's own test edits introduced and fixed inline."

requirements-completed: [GOV-03]

coverage:
  - id: D1
    description: "verify-additive-diff.sh classifies each commit's own staged delta (git diff --cached vs HEAD, scoped to src/main) instead of a stale release tag vs the working tree"
    requirement: "GOV-03"
    verification:
      - kind: unit
        ref: "tools/test/test-verify-additive-diff.sh (PASS=3 FAIL=0)"
        status: pass
      - kind: integration
        ref: "git diff --cached -U0 -- src/main (manual repro, empty/exit 0 with nothing staged)"
        status: pass
    human_judgment: false
  - id: D2
    description: "End-to-end pre-commit-hook regression reproducing the exact original bug sequence: a lane-2 override-landed src/main rewrite followed by an unrelated docs-only commit with NO HUB_LANE_OVERRIDE, asserted to succeed"
    requirement: "GOV-03"
    verification:
      - kind: integration
        ref: "tools/test/test-precommit-hook.sh (PASS=7 FAIL=0, case 'post-lane-2 unrelated commit unblocked (GOV-03 fix)')"
        status: pass
    human_judgment: false
  - id: D3
    description: "verify-api-additive.sh's identical latent architecture bug is proven live (not just asserted) and tracked as a residual risk for Phase 5's first tag-cut that includes api.txt"
    requirement: "GOV-03"
    verification:
      - kind: unit
        ref: "tools/test/test-verify-api-additive.sh (PASS=5 FAIL=0, case (e) 'KNOWN RESIDUAL RISK...')"
        status: pass
    human_judgment: true
    rationale: "The STATE.md tracking note itself (the second half of this deliverable's <done> criteria) could not be committed by this worktree executor per the orchestrator's explicit worktree-isolation instruction; the orchestrator must apply the note below post-merge before this deliverable is fully closed."

# Metrics
duration: ~10min
completed: 2026-09-01
status: complete
---

# Phase 3 Plan 1: GOV-03 Additive-Guard Staged-Delta Fix Summary

**Fixed `verify-additive-diff.sh`'s pre-commit false-flag root cause by switching its comparison basis from a stale release tag vs. the working tree to the commit's own staged delta vs. HEAD, closing the bug class end-to-end and live-proving (not just flagging) the sibling `verify-api-additive.sh` script's identical dormant flaw as a tracked Phase-5 risk.**

## Performance

- **Duration:** ~10 min
- **Tasks:** 3/3 completed
- **Files modified:** 4

## Accomplishments

- `tools/verify-additive-diff.sh` now diffs `git diff --cached` (the staged index) vs `HEAD`,
  scoped to `src/main`, instead of a fixed release tag vs. the current working tree — a commit
  that stages nothing under `src/main` is unconditionally lane-1 clean, with zero dependency on
  how long ago the last tag was cut or what rewrites happened in prior history.
- `tools/test/test-verify-additive-diff.sh`'s fixture cases (a)/(b) updated to `git add -A` their
  edits before invoking the script (staged-delta semantics require it), with `git reset --hard`
  (not `git checkout -- .`) between cases so a prior case's staged edit can't leak into the next.
- New end-to-end regression case in `tools/test/test-precommit-hook.sh`, reproducing the exact
  original bug sequence live: a lane-2, override-landed `src/main` rewrite followed by an
  unrelated docs-only commit with **no** `HUB_LANE_OVERRIDE` — asserted to succeed, proving
  GOV-03's literal success criterion.
- New case (e) in `tools/test/test-verify-api-additive.sh` proves `verify-api-additive.sh` shares
  the identical stale-cumulative-comparison architecture: after a declared, already-landed API
  break, a later completely unrelated commit that never touches `api.txt` still reports exit 3
  forever against the same baseline tag. Tracked as a residual risk per D-01's literal
  `src/main`-only scope, not fixed this phase.

## Task Commits

Each task was committed atomically:

1. **Task 1: Fix verify-additive-diff.sh's diff basis to staged-vs-HEAD (D-01) and update its own fixture test** - `5ea861d` (fix)
2. **Task 2: Regression-test the exact reproduced bug at the pre-commit-hook level** - `a1bb049` (test)
3. **Task 3: Prove and track verify-api-additive.sh's identical latent bug as a residual risk** - `663bacd` (test)

_No TDD tasks this plan; no plan metadata commit (worktree mode — orchestrator commits shared docs post-merge)._

## Files Created/Modified

- `tools/verify-additive-diff.sh` - Diff basis switched from `git diff -U0 "$BASELINE_COMMIT"` (stale tag vs. working tree) to `git diff --cached -U0` (staged index vs. HEAD); `BASELINE_COMMIT` now used only for default-path enumeration and ref-resolution
- `tools/test/test-verify-additive-diff.sh` - Cases (a)/(b) now `git add -A` before invoking the script; inter-case reset changed to `git reset --hard` to actually clear the index
- `tools/test/test-precommit-hook.sh` - New regression case reproducing the original bug's exact commit sequence; the following classifier-error block's `git reset --hard HEAD~1` adjusted to `HEAD~2` to account for the inserted commit
- `tools/test/test-verify-api-additive.sh` - New case (e) proving the sibling script's dormant identical bug via a two-commit simulated history (declared break, then an unrelated later commit)

## Decisions Made

- Followed D-01's literal `src/main`-only scope: fixed `verify-additive-diff.sh` only this phase;
  `verify-api-additive.sh`'s identical flaw is proven live and tracked, not fixed, per
  RESEARCH.md's Open Question 1 recommendation.
- Chose to reset via `git reset --hard` rather than `git checkout -- .` in both fixture-test files
  once staging (`git add -A`) was introduced — `checkout -- .` only syncs the working tree to
  whatever the index currently holds, it never unstages, so a prior case's staged edit would
  otherwise silently leak into the next case's `git diff --cached` result.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Task 1's own test-fixture cleanup didn't unstage between cases**
- **Found during:** Task 1 verification (`bash tools/test/test-verify-additive-diff.sh` failed
  case (c) with `got 1 want 0`)
- **Issue:** Case (a) and (b)'s new `git add -A` calls (required by the staged-delta semantics
  change) left their edits staged; the existing `git checkout -q -- .` cleanup between cases only
  restores the working tree from the INDEX, it never unstages, so case (b)'s staged `B.kt`
  rewrite silently leaked into case (c)'s `git diff --cached`, falsely failing a doc-only rewrite.
- **Fix:** Changed the inter-case reset in `test-verify-additive-diff.sh` from
  `git checkout -q -- .; git clean -fdq` to `git reset -q --hard; git clean -fdq`.
- **Files modified:** `tools/test/test-verify-additive-diff.sh`
- **Verification:** `bash tools/test/test-verify-additive-diff.sh` → `PASS=3 FAIL=0`
- **Committed in:** `5ea861d` (Task 1 commit)

**2. [Rule 1 - Bug] Task 2's new commit shifted the classifier-error block's reset depth**
- **Found during:** Task 2 implementation (traced before running, confirmed by test run)
- **Issue:** Inserting the new GOV-03 regression commit ahead of the existing "classifier error:
  missing API_FILE" block meant that block's `git reset --hard HEAD~1` (intended to land back on
  the "additive" commit) would instead land one commit short, on "declared behavior change".
- **Fix:** Changed `HEAD~1` to `HEAD~2` at that reset call, with a comment explaining why.
- **Files modified:** `tools/test/test-precommit-hook.sh`
- **Verification:** `bash tools/test/test-precommit-hook.sh` → `PASS=7 FAIL=0`
- **Committed in:** `a1bb049` (Task 2 commit)

**3. [Rule 1 - Bug] Task 3's new case (e) hit an empty-tree `git checkout -- .` error**
- **Found during:** Task 3 verification (`error: pathspec '.' did not match any file(s) known to
  git`)
- **Issue:** Case (d) (pre-existing) ends by committing the removal of the ONLY tracked file in
  this test's throwaway repo (`git rm -q "$API"; git commit`), leaving an empty tree. The new case
  (e)'s `git checkout -q -- .` cleanup errors against an empty index.
- **Fix:** Changed case (e)'s cleanup to `git reset -q --hard; git clean -fdq` (handles the
  empty-tree case), and added `mkdir -p api` before recreating the `.api` file (the directory
  itself is removed by `git clean` once its only file is untracked).
- **Files modified:** `tools/test/test-verify-api-additive.sh`
- **Verification:** `bash tools/test/test-verify-api-additive.sh` → `PASS=5 FAIL=0`
- **Committed in:** `663bacd` (Task 3 commit)

**4. [Worktree isolation constraint] STATE.md note deferred to orchestrator, not committed here**
- **Found during:** Task 3 (plan explicitly lists `.planning/STATE.md` in `files_modified` and
  directs appending a Blockers/Concerns note)
- **Issue:** The dispatch prompt for this worktree executor explicitly prohibits modifying
  `STATE.md`/`ROADMAP.md` ("the orchestrator owns those writes after all worktree agents in the
  wave complete"), which overrides the plan task's literal instruction.
- **Resolution:** The STATE.md edit was made, verified for correct wording, then reverted before
  committing (confirmed `git diff --stat .planning/STATE.md` is empty on this branch). The exact
  text to append is reproduced below for the orchestrator to apply post-merge.
- **Text for orchestrator to append to STATE.md's `## Accumulated Context` → `### Blockers/Concerns`:**
  > GOV-03 residual risk (tracked, not fixed): `verify-api-additive.sh` shares
  > `verify-additive-diff.sh`'s pre-fix architecture (stale cumulative baseline-vs-current
  > comparison) — currently dormant because `v1.10.0` predates `api.txt`, but will reproduce the
  > identical false-flag bug the moment Phase 5 cuts a tag that includes `api.txt`. Proven live in
  > `tools/test/test-verify-api-additive.sh` case (e). Phase 5 must apply the same staged-delta fix
  > (`git show ":$API_FILE"` vs `git show "HEAD:$API_FILE"`, per 03-RESEARCH.md's Pattern-2 code
  > excerpt) before or at its first tag-cut.

---

**Total deviations:** 4 (3 auto-fixed Rule-1 bugs in this plan's own test edits, 1 worktree-isolation
deferral of a documentation note to the orchestrator).
**Impact on plan:** All three Rule-1 fixes were necessary consequences of the staged-delta
semantics change itself (introducing `git add -A` requires correct unstaging, and inserting new
commits into a fixture's linear history requires downstream reset-depth math) — no scope creep.
The STATE.md deferral is a process/isolation constraint, not a content gap: the required text is
fully drafted and ready for the orchestrator to apply.

## Issues Encountered

None beyond the auto-fixed issues documented above.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- GOV-03 is fully closed: `.planning/`/docs-only commits land without `HUB_LANE_OVERRIDE` even
  in the presence of prior `src/main` rewrites since the last tag (proven both by direct diff-basis
  test and by an end-to-end pre-commit-hook regression).
- `bash tools/test/run-all.sh` is fully green (4 suites, 19 total passing checks: 4+7+3+5).
- **Action needed before this deliverable is fully closed:** the orchestrator must append the
  STATE.md Blockers/Concerns note (verbatim text above) after merging this wave — this worktree
  executor could not commit it directly.
- Residual risk for Phase 5: `verify-api-additive.sh` must receive the same staged-delta fix
  before or at Phase 5's first tag-cut that includes `api.txt`, or it will reproduce GOV-03's bug
  class for API lines. This is now proven live (not speculative) via
  `tools/test/test-verify-api-additive.sh` case (e).

---
*Phase: 03-governance-gates*
*Completed: 2026-09-01*
