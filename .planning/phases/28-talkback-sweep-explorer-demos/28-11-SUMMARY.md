---
phase: 28-talkback-sweep-explorer-demos
plan: 11
subsystem: feedback, component
tags: [a11y, talkback, gap-closure, stacked-snackbar, outcome-sheet, focus]
requires:
  - phase: 28-talkback-sweep-explorer-demos
    provides: "28-09 merged live-region node; 28-10 hidden middle strip; Gate-1 re-run 73b5451 and the 2026-10-18 ui-designer rulings"
provides:
  - "Every focusable stacked-snackbar card node is named from the data (C-5): card root contentDescription, Undo child contentDescription = undoLabel"
  - "C-1 route (b): input focus moves once to the refusal message node after the sheet settles, so TalkBack follows with accessibility focus; the node carries an explicit contentDescription"
affects: [28-gate-1-rerun]
tech-stack:
  added: []
  patterns:
    - "data-derived contentDescription on a merged node whose drawn text can be clipped away"
    - "focusRequester + focusable() on a live-region node, requested once per message value after the node position stops moving"
key-files:
  created: []
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackCard.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheet.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHostSemanticsTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetLiveRegionTest.kt
key-decisions:
  - "C-1 route used: (b), the focus move. Route (a), the deferred announcement, was NOT used and is not present (never both)"
  - "The card name is the message, plus the second line when there is one (Rule 2 deviation, below)"
  - "The sheet-settled signal is the refusal node's own window position, not the SheetState (pinned source contracts, below)"
requirements-completed: [A11Y-02]
status: complete
duration: ~1h 15m
completed: 2026-10-18
commits: 2
plan_head_before: 53dedc0a34f568b1507716561369480f4e333ec9
actuals:
  tokens: 21000
  tasks: 3
  commits: 2
---

# Phase 28 Plan 11: Names for the collapsed deck (C-5) and the focus route for the refusal message (C-1) Summary

**Every focusable stacked-snackbar card node is named from the data (card root = message, Undo child = the caller's undoLabel), and the Undo-refused message now takes input focus once, after the sheet has settled, through a focusable live-region node with an explicit description, which is the route TalkBack follows with its accessibility focus.**

## C-1: route used and why

**Route (b), focus move. Route (a) was not needed and is absent.**

### Research evidence (Task 2, time-boxed)

Read from the bytecode of the Compose UI actually resolved by the BOM (`androidx.compose.ui:ui-android` 1.11.x, `AndroidComposeViewAccessibilityDelegateCompat`):

1. When a node's `SemanticsProperties.Focused` turns true, the delegate sends `createEvent(virtualId, 8)` (`TYPE_VIEW_FOCUSED`) followed by a `TYPE_WINDOW_CONTENT_CHANGED`. That is the event a screen reader uses to follow input focus. So `FocusRequester` on a `focusable()` node is a supported way to move the reader.
2. `ACTION_ACCESSIBILITY_FOCUS` (64) in `performActionHelper` goes to `requestAccessibilityFocus`, which only records the delegate's own id and sends `TYPE_VIEW_ACCESSIBILITY_FOCUSED`. Accessibility focus is owned by the service, so calling that action from inside the app cannot move TalkBack. That alternative was rejected.
3. `createEvent` sets class name, package, source and password flags only. **It never sets event text or description.** So the `text=[]` of the observed `TYPE_WINDOW_CONTENT_CHANGED` is how Compose builds that event, and no semantics property can fill it. What the library controls is the source node: it now carries an explicit non-empty `contentDescription` equal to the full message (it already had the message as merged text), and the platform reads the source node. Whether the live event text counts as "non-empty" on the observer is therefore a device question for the Gate-1 re-run, not something this plan can prove.
4. Robolectric proof that the request reaches the delegate: with an accessibility service enabled in `ShadowAccessibilityManager`, hosting the refusal sends `TYPE_VIEW_FOCUSED` (8) and never `TYPE_ANNOUNCEMENT` (`refusalFocus_sendsAViewFocusedEvent_thatATalkBackServiceFollows`). The node reports `Focused = true` (`assertIsFocused`).

Limit: whether Samsung TalkBack 15.1 moves its accessibility focus on `TYPE_VIEW_FOCUSED` for a node inside the sheet window is not provable here. The Gate-1 re-run decides it. If it fails, cycle 3 tries route (a) per the ruling.

### What was built

In `UndoAffordanceBody` (`OutcomeSheet.kt`), the refused `Surface` keeps `.testTag("outcome_sheet_undo_refused").semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Assertive }` byte for byte and gains, after it: `.semantics { contentDescription = refusedMessage }`, `.onGloballyPositioned`, `.focusRequester(...)`, `.focusable()`. A `LaunchedEffect(refusedMessage)` waits for the sheet to settle, then calls `requestFocus()` once. Keyed on the message string, so an equal state never asks again and a different message asks once more; a new showing is a new composition and asks once.

### Double-read reasoning

The live region stays (the plan keeps it, and the existing tests pin exactly one `liveRegion = LiveRegionMode.`). A live region plus a focus move on one node could be read twice in principle. Why this combination should give one read:

- The node name is a `contentDescription`, which replaces the merged text for speech (it is not appended), so one event can only speak the message once.
- The live-region event fires about 0.2 s before the sheet window exists (Gate-1 evidence) and was dropped in 0 of 5 TalkBack-alone showings. The focus request comes after the settle (at least 300 ms later), as a different event type. If both do speak, the focus read interrupts the earlier live speech rather than queueing a copy (TalkBack focus feedback interrupts).
- No announcement and no `announceForAccessibility` is present (a source test pins it).

This is reasoning, not a measurement. The device re-run measures it (read once, 5 of 5).

## C-5: what changed

`FeedbackCard.kt`: the card root semantics gain `contentDescription = feedbackCardName(item.message, item.supportingText)`; the Undo `DynamicActionButton` modifier gains `.semantics { contentDescription = undoLabel }` on the same node (one layout node, so no new focus stop; testTag, role, click and the custom action are untouched). Middle-strip hide of 28-10, D-11/D-07 forms, tap owner, touch blocker, layout and visible text are unchanged. The Undo node is a merging node, so its description is not merged into the card root: the root's description is the message alone.

## Tasks and commits

| Task | Name | Commit |
|------|------|--------|
| 1 | Every focusable card node in the deck has a data-derived name (C-5) | 2c70311 |
| 2 | Refusal message reached by focus once when the sheet settles (C-1) | defa415 |
| 3 | Forced fresh battery (verification only, no commit) | - |

Both commits used `HUB_LANE_OVERRIDE=2` (reason: internal edit, no public signature changed, api.txt unchanged), as the plan authorises. No `--no-verify`.

## Tests

- `FeedbackStackHostSemanticsTest` (33 tests, 0 failures): added `collapsedDeckOfThree_everyVisibleFocusableNodeHasAName`, `collapsedDeckOfThreeInGerman_everyVisibleFocusableNodeHasAName`, `fannedDeckOfThree_everyFocusableNodeHasAName`, `decksOfOneAndTwo_everyFocusableNodeHasAName`, `cardRoot_nameIsExactlyTheMessage_notDoubled`, `undoChild_nameIsTheCallersUndoLabel_andStaysSeparate`, `cardNameComesFromAnExplicitContentDescription_notOnlyFromTheDrawnText`, `cardWithASecondLine_nameKeepsTheSecondLine_afterTheMessage`. Red phase: the three tests that assert the explicit ContentDescription failed on the old code; the generic "has a name" tests passed already on Robolectric because it shows unclipped text, which is why the property-level tests exist.
- `A11ySweepFeedbackCardTest` (3 tests, 0 failures): not edited; the locked D-11/D-07 assertions stay green unedited.
- `OutcomeSheetLiveRegionTest` (17 tests, 0 failures): added `refusedNode_isFocusable_andNamedWithTheFullMessage`, `refusedNode_withChangedItem_nameIncludesTheSuffix`, `refusedNode_hasNoClickAction_andIsTheOnlyAssertiveRegion`, `exactlyOneFocusRequestPerShowing`, `equalStateRecomposition_makesNoSecondRequest` (focus is moved to a rival and an equal outcome is recomposed twice: focus is not taken back), `aNewRefusal_makesANewSingleRequest`, `refusalInARealSheet_takesFocusOnlyAfterTheSheetHasSettled` (a real `ModalBottomSheet`: not focused at 100 ms, focused after), `neverBothAFocusMoveAndAnAnnouncement`, `refusalFocus_sendsAViewFocusedEvent_thatATalkBackServiceFollows`. Red phase: 7 failed before the implementation, for the right reasons (no Focused key, no ContentDescription).
- `OutcomeSheetClassicParitySourceContractTest` (16 tests): **not edited at all.** The pinned modifier chain stayed valid because the new modifiers were appended after the pinned `testTag().semantics(mergeDescendants=true){liveRegion=...}` adjacency. So the plan's allowed one-string edit was not used.

## Verification (commands and results)

- Task 1 verify (`FeedbackStack*`, `A11ySweepFeedbackCardTest`, `FeedbackCard*`, `FeedbackDeck*`, `LibraryInvariantsGuardTest`, `Phase28HygieneSourceContractTest`): BUILD SUCCESSFUL. `grep -c contentDescription FeedbackCard.kt` prints 4 (at least 3).
- Task 2 verify (`OutcomeSheetLiveRegionTest`, `OutcomeSheetClassicParitySourceContractTest`, `OutcomeSheet*`, `Receipt*`, `LibraryInvariantsGuardTest`, `Phase28HygieneSourceContractTest`): BUILD SUCCESSFUL. `grep -c 'liveRegion = LiveRegionMode\.' OutcomeSheet.kt` prints 1; `grep -c announceForAccessibility` prints 0.
- `./gradlew testDebugUnitTest --rerun-tasks --no-build-cache --no-daemon` (35 of 35 tasks executed, forced fresh, whole suite): BUILD SUCCESSFUL.
- `./gradlew apiDump --no-daemon` then `git diff --exit-code api.txt`: exit 0, api.txt byte-identical.
- `./gradlew detekt --no-daemon`: BUILD SUCCESSFUL (zero baseline).
- `bash tools/test/run-all.sh`: every script PASS, FAIL=0.
- `git diff --stat` against the plan base shows only four of the six named files (`A11ySweepFeedbackCardTest.kt` and `OutcomeSheetClassicParitySourceContractTest.kt` needed no edit).

## Deviations from Plan

**1. [Rule 2 - Missing critical functionality] The card name includes the second line when there is one.**
- Found during: Task 1.
- Issue: the plan says the card root's contentDescription equals `item.message`. A description replaces the merged text for speech, so for a card with a `supportingText` (a public `FeedbackEvent.Notify`/`WithUndo` field, shown in the Explorer as "Draft") the second line would silently stop being spoken. That is a regression of working behavior.
- Fix: name = `message`, or `"message, supportingText"` when a non-blank second line exists. For every card without a second line (all the C-5 cases) the name is exactly `item.message`, as the ruling requires. Pinned by `cardWithASecondLine_nameKeepsTheSecondLine_afterTheMessage`.
- Files: `FeedbackCard.kt`, `FeedbackStackHostSemanticsTest.kt`. Commit 2c70311.
- Note for the threat register: T-28-45 reads "the card name is exactly item.message". The second line is visible text the card already speaks, not hidden data, and the opaque target key is still never read (`keyNeverAppearsInTheSemanticsTree` is green).

**2. [Rule 3 - Blocking] The sheet-settled signal is the node's own position, not the `SheetState`.**
- Found during: Task 2.
- Issue: the plan suggests waiting on the sheet state. `UndoAffordanceBody` has no access to it. Providing it would need a `CompositionLocalProvider` inside `OutcomeSheet(...)`, which is pinned by `OutcomeSheetReceiptSourceContractTest` and `OpenTargetHygieneSourceContractTest` (the guard/call/return/legacy-scaffold order and the exact `SheetScaffold(...) {` line). Those files are outside the six allowed, and `UndoAffordanceBody`'s call sites in `SuccessBody` are hash-pinned to v2.5.1, so its signature cannot change either.
- Fix: `awaitSheetSettled` waits until the refusal node's window position has been unchanged for 6 frames and at least 300 ms have passed, with a 1.5 s cap so the message is always reached. A test with a real `ModalBottomSheet` proves the request is deferred (not focused at 100 ms, focused after).
- Risk: the thresholds (300 ms, 6 frames, 1.5 s) are chosen from the observed sheet timing, not measured on a device. If the Gate-1 re-run shows focus arriving too early or too late, these three constants are the knobs.

**Total deviations:** 2 auto-fixed (1 Rule 2, 1 Rule 3). **Impact:** none on scope or API; both keep the rulings intact.

## Observations outside this plan's scope

- `FeedbackCard.kt` (from plan 28-10) calls `invisibleToUser()`, which this Compose version reports as deprecated in favor of `hideFromAccessibility()`. It still works and is not in this plan's scope; a follow-up could migrate it.

## NOT proven by this plan

No device was touched. The TalkBack behavior is NOT proven here and is re-verified by the Gate-1 re-run: the refusal spoken on every showing (at least 5 of 5, TalkBack alone), spoken once and never twice, the observer event carrying the message, and the collapsed deck having no unnamed focus stop in English and German. What the unit tests prove is the tree and the events: the nodes carry explicit names, the refusal takes focus once after the settle, and the Compose delegate emits `TYPE_VIEW_FOCUSED` for it. If the device re-run fails, cycle 3 tries route (a) per the ruling (and the three settle constants are the first thing to check).

## Self-Check: PASSED

- Modified files present and committed: `FeedbackCard.kt`, `OutcomeSheet.kt`, `FeedbackStackHostSemanticsTest.kt`, `OutcomeSheetLiveRegionTest.kt`.
- Commits 2c70311 and defa415 exist on the worktree branch; `git rev-list --count 53dedc0..HEAD` was 2 at SUMMARY time; working tree clean before this file. No STATE.md or ROADMAP.md change, no device touched.
