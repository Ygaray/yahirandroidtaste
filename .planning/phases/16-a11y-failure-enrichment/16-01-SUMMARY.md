---
phase: 16-a11y-failure-enrichment
plan: 01
subsystem: ui-a11y
tags: [compose, accessibility, semantics, radio-button, touch-target, ApproachLadderCard]
requires: []
provides:
  - "ApproachLadderCard rung rows expose Role.RadioButton + Selected (rung.id == maxTierId) when the cap is selectable"
  - "Interactive rung wrapper presents at least the minimum interactive touch size (non-overlapping targets)"
affects: [16-02, 16-03, 18-docs, 19-release]
tech-stack:
  added: []
  patterns:
    - "Private-param threading (RungRow.isSelected -> CapControl.selected); every a11y modifier gated on onClick != null"
    - "Falsifiable touch-bounds overlap test instead of assertTouchHeightIsEqualTo"
key-files:
  created: []
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCardTest.kt
key-decisions:
  - "D-01: selection derives from rung.id == maxTierId (the chosen cap), never from the above-cap isCapped state"
  - "D-03: Role/Selected/min-size only when onMaxTierChange != null; cap-less ladder carries no role, selected or click"
  - "A4 locked: minimumInteractiveComponentSize() applied only on the interactive wrapper (onClick != null); cap-less rows stay compact"
  - "D-04: ~58dp rung pitch growth accepted and left as a Gate-2 visual check"
requirements-completed: [VA11Y-01]
duration: 4 min
completed: 2026-10-05
status: complete
commits: 2
plan_head_before: 31a7c4afcf5fa35cf50fde9c4496e32bdbfa5a18
actuals:
  tokens: 4500
  tasks: 2
  commits: 2
coverage:
  - deliverable: "Chosen cap rung announced Selected + RadioButton; others RadioButton unselected; stale id selects none"
    verification:
      - kind: test
        ref: "ApproachLadderCardTest#the rung whose id equals maxTierId is Selected RadioButton ..., #selection follows the chosen cap id ..., #a stale maxTierId ..."
        status: pass
    human_judgment: false
  - deliverable: "Cap-less ladder exposes no role / selected / click"
    verification:
      - kind: test
        ref: "ApproachLadderCardTest#a cap-less ladder exposes no role no selected state and no click action on any rung"
        status: pass
    human_judgment: false
  - deliverable: "Interactive rung touch targets >= 48dp and non-overlapping; cap-less rows not inflated"
    verification:
      - kind: test
        ref: "ApproachLadderCardTest#rung touch targets do not overlap ..., #a cap-less ladder keeps its compact row pitch ..."
        status: pass
    human_judgment: false
  - deliverable: "Row-pitch growth (45dp -> 58dp) is visually acceptable"
    human_judgment: true
    rationale: "Visual adequacy of the accepted D-04 growth is an owner Gate-2 judgment; no test asserts it"
---

# Phase 16 Plan 01: ApproachLadderCard rung a11y (VA11Y-01) Summary

**Rung rows now announce the user's chosen cap as the selected radio button (`Modifier.selectable(role = Role.RadioButton)`, `rung.id == maxTierId`) and, when interactive, grow to the minimum interactive size inside the unchanged outer padding; strictly private, `api.txt` byte-identical.**

## Performance
- **Duration:** 4 min (2026-10-05T10:00:39Z to 10:04:54Z)
- **Tasks:** 2 (tracer + auto, both TDD) | **Files:** 2 modified

## Accomplishments
- Task 1 (tracer): `isSelected = onMaxTierChange != null && rung.id == maxTierId` threaded rung loop -> private `RungRow(isSelected)` -> private `CapControl(selected)`, where `clickable` became `selectable(selected, role = Role.RadioButton, onClick)` (only when `onClick != null`; `clickable` import dropped). KDoc updated on `CapControl` and `@param maxTierId`.
- Task 2: `Modifier.minimumInteractiveComponentSize()` on `CapControl`'s Row, gated on `onClick != null`, positioned before the merged-semantics step. `RungRow`'s `padding(vertical = Dimens.ContentSpacing)` untouched (outside the touch target).
- 6 new tests in `ApproachLadderCardTest` (4 semantics + 2 layout).

## Task Commits
| Task | Commit | Message |
|------|--------|---------|
| 1 | `8f1b602` | feat(16-01): selected + RadioButton semantics on the chosen cap rung (VA11Y-01, D-01, D-03) |
| 2 | `1f549e1` | feat(16-01): minimum interactive size on the interactive rung wrapper (VA11Y-01, D-04) |

## Verification results
- `./gradlew testDebugUnitTest --tests '*ApproachLadderCardTest' detekt`: green after each task (21 tests in the class after Task 2: 15 pre-existing + 6 new, 0 failures); detekt zero-baseline green.
- `apiDump` byte-identical (`cmp` OK) after both tasks; `apiCheck` BUILD SUCCESSFUL both tasks. Neither commit touches `api.txt`.
- All `<acceptance_criteria>` grep/cmp checks of both tasks PASS (re-run before each commit).
- RED observed first: Task 1 tests (a),(b),(c) failed on the unmodified source (AssertionError); Task 2 tests both failed on the Task 1 source (see deviation 2).

## Measured values for Gate-2 (Robolectric, mdpi: 1 px = 1 dp)
| | Without min-size (Task 1 source) | With min-size (Task 2) |
|---|---|---|
| Touch bounds rung 0/1/2 (top-bottom) | 14-62, 59.5-107.5, 104.5-152.5 (OVERLAP) | 20-68, 78.5-126.5, 136.5-184.5 (no overlap) |
| Row pitch, cap-selectable ladder | 45 | 58 |
| Row pitch, cap-less ladder | 45 | 45 (unchanged; not inflated) |

Matches RESEARCH Pitfall 3 exactly. A4 applied as: min-size only when `onClick != null`.

## Deviations from Plan

**1. [Rule 3 - Blocking] HUB_LANE_OVERRIDE=2 instead of 3**
- Found during: Task 1 commit. The pre-commit classifier reported `LANE 2 (mode=additive, baseline=v2.4.1)` for these source-only commits (no `api.txt` line removed in this plan), not lane 3 as the plan assumed (lane 3 arises only once plan 16-02 removes api.txt `copy` lines). The hook requires the override to equal the detected lane exactly, so the commits were made with `HUB_LANE_OVERRIDE=2`. No other behavior changed. Commits: `8f1b602`, `1f549e1`.

**2. [Rule 1 - Bug in test] Cap-less pitch pin was vacuous as first written**
- Found during: Task 2 RED run. With the second ladder capped at "hybrid", the Cloud row renders the "Capped" label which adds ~1px, so cap-less=45 vs cap-selectable=46 passed BEFORE the fix. Fixed the test (cap at the top rung "cloud" so no row shows "Capped"); re-run RED showed 45 vs 45 (fails), GREEN shows 45 vs 58. Strict inequality kept. File: `ApproachLadderCardTest.kt`, in commit `1f549e1`.

**Total deviations:** 2 (1 blocking-process, 1 test fix). **Impact:** none on scope or public API.

## Gate-2 item left for the owner
Open the Explorer ApproachLadderCard fixtures and confirm the ~58dp rung pitch (was ~45dp) is comfortable (D-04). Not optimized away; the outer padding was deliberately left outside the touch target.

## Notes
- Explorer, `ComponentRegistry`, `api.txt`, consumers, 16-VALIDATION.md untouched; no tag cut.
- No authentication gates. No deferred issues.

## Self-Check: PASSED
- Files exist: ApproachLadderCard.kt, ApproachLadderCardTest.kt, this SUMMARY.
- Commits `8f1b602` and `1f549e1` present in `git log`; each lists exactly the two plan files and no `api.txt`.
