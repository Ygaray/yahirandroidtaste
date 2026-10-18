---
phase: 29-docs-v2-8-0-cut-readiness
plan: 03
subsystem: docs
tags: [style-guide, talkback, drift-guard, rejected-ideas, device-testing-playbook]
status: complete
requires:
  - phase: 29-02
    provides: "API.md and INTEGRATION.md v2.8.0 text; INTEGRATION (vi) points to STYLE.md section 7 for the TalkBack lines"
provides:
  - "docs/STYLE.md: v2.8 header range, dot 3:1 rule, capped pill radius, go-to-item row rule, deck rules, and a current TalkBack section (single source)"
  - "docs/STYLE.REVERTED.md: three newest-first entries for the rejected v2.8 ideas"
  - "AGENT-DEVICE-TESTING.md: one D7 bullet on the observer AccessibilityService and the baseline-control harness"
  - "CatalogDocsDriftGuardTest: STYLE.md guard (16 cases now)"
affects: [29-05, 29-06]
plan_head_before: c595abe407498765f3fbcd39f3485377c924114b
actuals:
  tokens: 9000
  tasks: 3
  commits: 3
key-files:
  created: []
  modified:
    - docs/STYLE.md
    - docs/STYLE.REVERTED.md
    - AGENT-DEVICE-TESTING.md
    - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/CatalogDocsDriftGuardTest.kt
key-decisions:
  - "TalkBack lines live only in STYLE.md section 7 (PD-1); no TalkBack paragraph in API.md"
  - "Docs follow the shipped source where the Runtime Decision or the options page disagree; listed below"
requirements-completed: [SHIP-05]
duration: 25 min
completed: 2026-10-18
---

# Phase 29 Plan 03: STYLE.md v2.8 rules, rejected ideas and device-playbook note Summary

STYLE.md now states the accepted v2.8 look (Receipt-card deck with no action label and a chevron only when a target exists, swipe that only dismisses, timers paused while touched or fanned, 3-card cap, label-less go-to-item sheet row, 3:1 dot ring, capped pill radius) and replaces the stale "TalkBack gap is open as backlog 999.2" sentence with the swept state; the refusal is documented as the code ships it (a single Assertive live region after the sheet settles, the library never moves focus). A plain-JVM guard keeps it that way.

SHIP-05 stays open in REQUIREMENTS.md (plans 29-05 and 29-06 still own it); I did not mark it.

## Commits

| Task | Commit | Files |
|------|--------|-------|
| 1 STYLE.md v2.8 rules, TalkBack section, guard test | edbb0a3 | docs/STYLE.md, CatalogDocsDriftGuardTest.kt |
| 2 STYLE.REVERTED.md three entries | 5e81020 | docs/STYLE.REVERTED.md |
| 3 AGENT-DEVICE-TESTING.md D7 bullet | cfc6c69 | AGENT-DEVICE-TESTING.md |

## RED output (before STYLE.md was edited)

```
CatalogDocsDriftGuardTest > STYLE md carries the v2_8 deck and go-to-item rules and a current TalkBack section FAILED
    java.lang.AssertionError at CatalogDocsDriftGuardTest.kt:431
16 tests completed, 1 failed
```

(Line 431 is the header assertion: the header did not reach v2.8.) A first GREEN attempt failed once on `section 2 must state the capped pill corner radius` because my sentence said "cap its corner radius"; I reworded the doc to "keep its corner radius capped" rather than loosen the guard.

## Verification

| Check | Result |
|-------|--------|
| `./gradlew testDebugUnitTest --tests '*CatalogDocsDriftGuardTest' --no-daemon` | BUILD SUCCESSFUL; TEST xml tests=16 failures=0 errors=0, contains the new testcase name |
| `grep -c 'Assertive live region' docs/STYLE.md` | 1 |
| `grep -cE '^- \*\*2026-10-(17\|18) ' docs/STYLE.REVERTED.md` | 3; `28-12-SUMMARY.md` pointer count 1; every pointed path is tracked (`git ls-files`) |
| `git show --numstat` for AGENT-DEVICE-TESTING.md | 10 added, 0 deleted |
| STYLE.REVERTED.md diff | 15 insertions (git); no existing entry touched |
| `git tag --list 'v2.8*'` | empty |
| `git diff --stat 55f5288 HEAD -- src/main`, `git diff --stat c1681ff HEAD -- api.txt` | empty |
| Added lines scanned for home paths, 100.x addresses, `.ts.net` | none |
| Commit file lists | exactly the planned paths per task; the pre-commit hook passed each (LANE 1, additive, baseline v2.7.1) |

## Discrepancies for the master

1. **Refusal focus sentence vs shipped code.** The Runtime Decision says "the refused-undo message takes accessibility focus once when its sheet settles". That describes the superseded 28-11 route. The shipped code (OutcomeSheet.kt, `UndoAffordanceBody`, `ReplaceWhenChanged`, `awaitPositionAtRest`) has no focus request and no announcement call: a single Assertive live region whose named node appears after the sheet settles (about 0.3 to 1.5 s). Device result per 28-VERIFICATION C-1: 27 of 27 showings spoken exactly once. STYLE.md section 7 documents the live-region route and says the library never moves focus; the guard forbids the focus-move phrases.
2. **"Named from `item.message`" vs message plus second line.** The Runtime Decision says collapsed deck cards are named from `item.message`. The code (`feedbackCardName`, FeedbackCard.kt) names the card from the message followed by the second line when there is one. The docs say that.
3. **Options page vs plan wording for the rejected Open label.** The plan said the text Open action label was rejected as "options B and C" of the options page. The page itself (docs/design/snackbar-go-to-item/index.html, chip "no action label: tap the snackbar itself") fixes the no-label rule for all three directions, so the REVERTED entry says an Open text action beside Undo was considered and rejected per brief D-02/D-03, with direction A chosen on 2026-10-17, and does not assign it to B and C.
4. **"Read twice" wording for the focus route.** The plan asked to cite what 28-12 and 28-VERIFICATION say. They record that the focus move on a live-region node was read a second time (Gate-1 run 2), so the REVERTED entry says exactly that and does not add a claim that a focus move is invasive.
5. **`am start -S` fact source.** The plan said every fact in the AGENT-DEVICE-TESTING bullet appears in 28-07-SELF-UAT.md. The observer service, own package, the logged fields, `uiautomator dump` suppressing other services, the `git archive` control and the physical gestures are in the SELF-UAT method notes and header; the `am start -S` force-stop and observer-disable fact is in 28-RESEARCH.md (Pitfall 11, from the 27-04 method notes) and 28-07-PLAN.md, not in the SELF-UAT. It is correct and kept.
6. **Gate-2 owner items stay recorded, not fixed.** STYLE.md section 7 lists the four physical gestures (spoken cross-card order, heading navigation, stop count of an openable Undo Center row, explicit swipe focus on the refusal message) as Gate-2 owner steps. The Runtime Decision also names the German font-2.0 narrow body column, the German batch Receipt sheet's actions under the nav bar, and the three throwaway harness apps on the tester; those are not STYLE rules and are left for the 29-06 proposal.

## Deviations from Plan

None - plan executed as written (the only fix was the doc rewording noted under RED output).

## Issues Encountered

None.

## Self-Check: PASSED

- edbb0a3, 5e81020 and cfc6c69 exist in `git log`; docs/STYLE.md, docs/STYLE.REVERTED.md, AGENT-DEVICE-TESTING.md and the test file exist on disk.
- `git rev-list --count c595abe..HEAD` was 3 before this SUMMARY commit, matching `commits: 3`.
