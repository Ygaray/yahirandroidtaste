---
phase: 27-open-from-the-sheet-undo-center-pill-fixes
plan: 03
subsystem: ui
tags: [compose, undo-center, go-to-item, accessibility, source-compat, overload]

requires:
  - phase: 25
    provides: "FeedbackTarget, UndoHistoryEntry.target and openableTarget, UndoHistoryStore.append with a target"
provides:
  - "Second UndoCenterScreen overload with a required onOpenTarget (v2.7.1 overload unchanged and first)"
  - "Private LocalUndoCenterOpenTarget composition local, early branch in UndoHistoryRow, private UndoHistoryOpenRow and UndoOpenTrailing"
  - "internal UNDO_OPEN_CHEVRON_TEST_TAG_PREFIX = undo-open-chevron-"
affects: [27-04 phase source audit, 29 docs]

actuals:
  tokens: 28000
  tasks: 3
  commits: 3

tech-stack:
  added: []
  patterns:
    - "Overload plumbing through a private composition local (lane 1: the v2.7.1 body is not rewritten)"
    - "Single detectTapGestures owner with onLongPress for the peek and onTap for the open; no clickable"
    - "Peek of an openable row is only visible in the UNMERGED tree (it sits inside the merged row node)"

key-files:
  created:
    - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreenOpenTargetTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreenOpenParityTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreenOpenTargetLongLabelTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreen.kt
    - api.txt
    - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreenSourceContractTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/V271CallSiteCanaryTest.kt

key-decisions:
  - "PD-11: a hold never opens and never undoes; the read-only peek is shown from onLongPress (not onPress), so a tap never flashes it. Rows that are not openable keep the v2.7.1 peek-on-press byte-identically"
  - "PD-12: trailing slot of an openable row is Undo then a decorative 24dp chevron"
  - "PD-13: the new overload wraps the v2.7.1 one through a private composition local"
  - "PD-14: the open row keeps the row tag undo-row-<id>; the chevron uses undo-open-chevron-<id> (not a prefix the CATALOG tag scan inspects, API.md untouched)"

patterns-established:
  - "Stale-callback guard: target and callback read through rememberUpdatedState, and the row leaves the open branch the moment openableTarget turns null"

requirements-completed: [FOPEN-03]

coverage:
  - id: D1
    description: "Through the new overload, an Available entry with a target opens its item with one tap; Undone, Failed, targetless entries and group headers never open"
    requirement: FOPEN-03
    verification:
      - kind: unit
        ref: "UndoCenterScreenOpenTargetTest#availableEntryWithTarget_tapOpensOnce"
        status: pass
      - kind: unit
        ref: "UndoCenterScreenOpenTargetTest#undoneEntry_doesNotOpen_andHasNoChevronOrClick"
        status: pass
      - kind: unit
        ref: "UndoCenterScreenOpenTargetTest#failedEntry_doesNotOpen"
        status: pass
      - kind: unit
        ref: "UndoCenterScreenOpenTargetTest#groupHeader_neverOpens_membersDo"
        status: pass
    human_judgment: false
  - id: D2
    description: "Hold shows the peek and release neither opens nor undoes; a tap never composes the peek; a drag never opens; tapping Undo undoes once and neither opens nor peeks"
    requirement: FOPEN-03
    verification:
      - kind: unit
        ref: "UndoCenterScreenOpenTargetTest#holdingAnOpenableRow_showsThePeek_andReleaseNeitherOpensNorUndoes"
        status: pass
      - kind: unit
        ref: "UndoCenterScreenOpenTargetTest#tappingAnOpenableRow_neverComposesThePeek"
        status: pass
      - kind: unit
        ref: "UndoCenterScreenOpenTargetTest#dragOverAnOpenableRow_neverOpens"
        status: pass
      - kind: unit
        ref: "UndoCenterScreenOpenTargetTest#tappingUndo_runsUndoOnce_neitherOpensNorPeeks"
        status: pass
    human_judgment: false
  - id: D3
    description: "Stale-callback guard: after an Undo through the store the chevron and click action are gone and a tap opens nothing"
    requirement: FOPEN-03
    verification:
      - kind: unit
        ref: "UndoCenterScreenOpenTargetTest#afterUndoViaTheStore_tappingTheRow_doesNotOpen"
        status: pass
    human_judgment: false
  - id: D4
    description: "Edges: adjacency (Undone row with an equal target), equal targets on two Available rows keep order and open once each, empty list renders the empty state, blank openLabel falls back to the message"
    requirement: FOPEN-03
    verification:
      - kind: unit
        ref: "UndoCenterScreenOpenTargetTest#availableRowNextToUndoneRowWithEqualTarget_onlyTheAvailableOpens"
        status: pass
      - kind: unit
        ref: "UndoCenterScreenOpenTargetTest#equalTargetsOnTwoAvailableRows_eachOpensOnceOnItsOwnTap"
        status: pass
      - kind: unit
        ref: "UndoCenterScreenOpenTargetTest#emptyEntries_withTheNewOverload_rendersTheEmptyState"
        status: pass
      - kind: unit
        ref: "UndoCenterScreenOpenTargetTest#blankOpenLabel_fallsBackToTheMessageAsClickLabel"
        status: pass
    human_judgment: false
  - id: D5
    description: "The existing call renders exactly as v2.7.1 (DefaultParityTest unedited) and the new overload reproduces goldens D1 to D4 and the row bounds when no row is openable; every v2.7.1 call shape binds to the old overload"
    requirement: FOPEN-03
    verification:
      - kind: unit
        ref: "UndoCenterScreenOpenParityTest (7 tests) and UndoCenterScreenDefaultParityTest (5, unedited)"
        status: pass
      - kind: unit
        ref: "V271CallSiteCanaryTest#undoCenterScreen_v271Shapes_bindToTheOldOverload_andOnOpenTargetBindsToTheNewOne"
        status: pass
      - kind: other
        ref: "bash tools/verify-api-source-additive.sh v2.7.1 prints API SOURCE-ADDITIVE OK; ./gradlew apiCheck passes"
        status: pass
    human_judgment: false
  - id: D6
    description: "On the tester, an Available row with a target opens on tap and shows a chevron after Undo, an Undone row shows neither, a hold shows the preview, Undo still undoes without opening; layout in German at 360dp; TalkBack reading order of the row"
    requirement: FOPEN-03
    verification:
      - kind: unit
        ref: "UndoCenterScreenOpenTargetLongLabelTest (5 tests, NATIVE graphics, 360dp, font scale 1.0 and 2.0)"
        status: pass
    human_judgment: true
    rationale: "STYLE.md section 7: only real-device screenshots count for layout sign-off, and the gesture feel and TalkBack order (FA-5, FA-7) are not asserted by any unit test; the Gate-1 tester owns the on-device re-check"

duration: 33min
completed: 2026-10-17
status: complete
plan_head_before: ad85f188b391275cde546371d1faddfe78b8eb8c
commits: 3
---

# Phase 27 Plan 03: Open from the Undo Center Summary

**A second `UndoCenterScreen` overload with a required `onOpenTarget` makes every Available entry that carries a target open its item on tap: one merged `Role.Button` row driven by a single tap/long-press owner (a hold shows the read-only peek and never opens), with Undo then a decorative chevron; the v2.7.1 overload is byte-unchanged and first in the file.**

## Performance

- **Duration:** about 33 min
- **Tasks:** 3 (1 tracer, 2 auto)
- **Files:** 3 created, 4 modified (including the regenerated `api.txt`, one added line)

## Accomplishments

- New public overload `UndoCenterScreen(entries, onNavigateBack, onUndo, groups, onUndoGroup, onOpenTarget, onClear)` placed directly after the v2.7.1 one. It wraps the old overload in a `CompositionLocalProvider` of the private `LocalUndoCenterOpenTarget`; `onOpenTarget` is required and non-null so no v2.7.1 call (including trailing-lambda shapes) can become ambiguous.
- `UndoHistoryRow` gained five lines at the top: when the local and `entry.openableTarget` are both non-null it delegates to the new private `UndoHistoryOpenRow` and returns. Every other row keeps the exact v2.7.1 tree and behaviour.
- `UndoHistoryOpenRow`: same `Box`/`ListItem`/peek anatomy, plus `semantics(mergeDescendants = true) { role = Button; onClick(label = openLabel or message) }` and ONE `pointerInput(entry.id)` with `detectTapGestures(onPress = release hides peek, onLongPress = show peek, onTap = open)`. No `clickable`. Target and callback go through `rememberUpdatedState`.
- `UndoOpenTrailing`: Undo `TextButton`, then the 24dp `KeyboardArrowRight` chevron tagged `undo-open-chevron-<id>`.
- `UndoCenterScreenSourceContractTest`: deliberate D-04 edit (public function count 1 to 2, plus an assertion on the second overload's parameter list); nothing else in that file changed.
- Tests: `UndoCenterScreenOpenTargetTest` (19), `UndoCenterScreenOpenParityTest` (7), `UndoCenterScreenOpenTargetLongLabelTest` (5), canary `undoCenterScreen_v271Shapes_bindToTheOldOverload_andOnOpenTargetBindsToTheNewOne`.

## Task Commits

1. Task 1 (tracer): `ca4771f` feat(27-03): UndoCenterScreen overload with onOpenTarget
2. Task 2: `659bb2c` test(27-03): pin the Undo Center open row gestures, inert cases, the stale guard and the golden parity of the new overload
3. Task 3: `ad80c4c` test(27-03): pin the Undo Center row geometry in long German and Spanish and the UndoCenterScreen call shapes

All three commits were classified LANE 1 (mode=additive, baseline=v2.7.1) by the pre-commit hook; no `HUB_LANE_OVERRIDE` was used.

## Verification (commands and results)

All with `--no-daemon`, one Gradle invocation at a time, from the worktree.

- Task 1: `apiDump` plus OpenTargetTest, SourceContractTest, SourceCompatV251Test, DefaultParityTest, UndoCenterScreenTest, GroupsTest, UndoCenterRowsTest: failures 0. `apiCheck`: pass. `bash tools/verify-api-source-additive.sh v2.7.1`: `API SOURCE-ADDITIVE OK` (removed 0, signature_changes 0). Acceptance greps as specified (two `fun UndoCenterScreen(`, one `onOpenTarget: (FeedbackTarget) -> Unit`, two `detectTapGestures(` outside comments, `occurrence = 2` present, api.txt line for the new overload).
- Task 2: OpenTargetTest 19, OpenParityTest 7, DefaultParityTest 5 (unedited), UndoCenterScreenTest 4, GroupsTest, SourceContractTest 7: failures 0. `apiDump` left `api.txt` unchanged.
- Task 3: OpenTargetLongLabelTest 5, V271CallSiteCanaryTest 12, SourceCompatV251Test 2, UndoCenterScreenLongLabelTest, SourceContractTest: failures 0. Six guard classes (CatalogDocsDrift 10, DomainVocabularyDrift 2, ComponentRegistryDrift 1, ComponentStatesMatrix 2, ComponentPlaygroundIntegrity 5, LibraryInvariants 5): failures 0. `detekt`: pass at zero baseline.
- Plan gate: `*UndoCenterScreen*`, `*UndoCenterRowsTest`, `*V271CallSiteCanaryTest` plus `apiCheck`: green. `git tag --list 'v2.8*'` prints nothing; nothing pushed; no debris path in any plan commit.
- Registry counts unchanged (an added overload of an existing name introduces no new public composable name; ComponentRegistryDriftGuardTest green).

## Decisions Made

PD-11 to PD-14 as in the plan, implemented unchanged. **FA-5** (a hold shows the peek and never opens) and **FA-6** (research A3: `detectTapGestures` does not call `onTap` after `onLongPress`) are carried forward; FA-6 is now pinned by `holdingAnOpenableRow_showsThePeek_andReleaseNeitherOpensNorUndoes`, which passes.

## Deviations from Plan

None - plan executed exactly as written, with these notes:

- The "run it red first" step for the tracer was not run as a separate Gradle invocation: before the overload exists the test cannot compile. The first compile failure was only a missing test import.
- The drift guards were run from the worktree. They walk up to a directory named `yahirandroidtaste`, which from here resolves to the MAIN checkout, so they probably read the main checkout's sources and not these worktree sources. The orchestrator should re-run them from the main checkout after merge (`*CatalogDocsDriftGuardTest`, `*ComponentRegistryDriftGuardTest`, `*DomainVocabularyDriftGuardTest`).
- Test method names that the plan wrote with dots in prose (`v2.7.1`) use a hyphen (`v2-7-1`) where the name is a backtick name, because a dot is illegal in a JVM method name.
- Robolectric gesture notes learned while writing the tests: the peek overlay of an openable row lives inside the merged row node, so its test tag is visible only in the unmerged tree (all peek assertions on openable rows use `useUnmergedTree = true`); the drag test uses 20 entries so the list can scroll and consume the drag.
- Two tests beyond the plan list: `aRowWithoutATarget_hasNoChevron` and, in the long-label class, a control that renders the v2.7.1 overload and the new overload side by side to prove a targetless row's Undo does not move.

**Total deviations:** 0 auto-fixed. **Impact:** none.

## Issues Encountered

One observation for the Gate-1 tester and the orchestrator (not a defect in this plan's contract): Material3 `ListItem` is itself a merging semantics node, so the openable row's own merged node carries `Role.Button` and the open label but NOT the message text; the message and timestamp live in the ListItem's child node and Undo is a third node. A screen reader therefore has three stops on an openable row (row button with the open label, message, Undo). The plan's contract (one merged `Role.Button` node, Undo as its own node) is met; whether the reading order is acceptable is a Gate-1 / TalkBack judgment (call it FA-7). Fixing it would mean replacing `ListItem` (changing row geometry) or hiding the ListItem node from accessibility, neither of which this plan authorizes.

Behavioral verification on a device is not part of this plan; the Gate-1 tester owns the on-device re-check described in the plan's last truth.

## Next Phase Readiness

Ready for 27-04 (phase source audit). The phase-wide source contract can scan `UndoCenterScreen.kt` for key reads: none exist, only `target.openLabel` and the target value handed back to the handler are used.

## Self-Check: PASSED

- Created files verified on disk: `UndoCenterScreenOpenTargetTest.kt`, `UndoCenterScreenOpenParityTest.kt`, `UndoCenterScreenOpenTargetLongLabelTest.kt`.
- Commits `ca4771f`, `659bb2c`, `ad80c4c` exist on `worktree-agent-aa03a44401c75828f`; `git rev-list --count` from the recorded base `ad85f188b391275cde546371d1faddfe78b8eb8c` is 3.
