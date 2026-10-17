---
phase: 28-talkback-sweep-explorer-demos
plan: 06
subsystem: ui
tags: [accessibility, talkback, semantics, robolectric, compose, undo-center]
requires:
  - phase: 27-open-from-the-sheet-undo-center-pill-fixes
    provides: openable Undo Center row, open label plumbing, ReceiptRow/ProviderKeyCard/OutcomeSheet semantics under test
provides:
  - Layer-1 (Robolectric) TalkBack sweep: three A11ySweep* test classes covering every A11Y-02 structural claim
  - Undo Center group header proven to be a heading
  - Openable Undo Center row is one merged Role.Button node carrying message and timestamp (candidate C-2 fixed)
affects: [28-07 Gate-1 TalkBack sweep on the TESTER]
status: complete
commits: 5
plan_head_before: 87238dafb110fe51fa232c05b3d337c088a26121
actuals:
  tokens: 20000
  tasks: 3
  commits: 5
tech-stack:
  added: []
  patterns:
    - "Sweep tests carry a falsification control inside each test (the wrong node / other Unicode form / other state must fail the same check)"
key-files:
  created:
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/A11ySweepSemanticsTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/A11ySweepUndoCenterSemanticsTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/A11ySweepFeedbackCardTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreen.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreenOpenTargetTest.kt
key-decisions:
  - "PD-1 applied: testTag, semantics(mergeDescendants) and combinedClickable moved from the openable row's Box onto the ListItem modifier; the ListItem node is the row node"
  - "C-1 (refused live region), C-3 and C-4 deliberately untouched; evidence goes to Gate-1 (28-07)"
requirements-completed: [A11Y-02]
---

# Phase 28 Plan 06: Layer-1 TalkBack Sweep Summary

Robolectric sweep of every structural A11Y-02 claim in three named classes, plus a geometry-neutral fix that makes the openable Undo Center row a single screen-reader stop carrying its message.

## Accomplishments

- **A11ySweepSemanticsTest (9 tests, green on first run on today's code):** ReceiptRow is one `Role.Button` node with the caller's click label and Collapsed/Expanded state, Undo is a separate clickable stop that never toggles the row, and a double toggle returns to Collapsed. The refused-undo surface is the only Assertive live region (Success and Failure arms), it is the node tagged `outcome_sheet_undo_refused`, its subtree holds the full message, there is no suffix without a changed item, and equal-state recomposition keeps exactly one region. The saved-key summary is one merged `Saved · <fingerprint>` node with Update and Clear as separate `Role.Button` nodes outside it (also with no actions, and with only Update). Sheet target-row and batch-row open labels pass through byte for byte, composed and decomposed, Receipt and Classic.
- **A11ySweepUndoCenterSemanticsTest (10 tests):** group header label is a `Heading`, subtitle and Undo all are not, heading precedes Undo all in child order, no handler keeps the heading and has no button, a decomposed German label is the heading text verbatim. Row tests: one merged `Role.Button` stop with the open label and the message, no separate message node, Undo its own button, verbatim open labels, and the non-openable row keeps its v2.7.1 structure.
- **C-2 fix:** `UndoHistoryOpenRow` now applies the tag, the merge and the single `combinedClickable` owner on the `ListItem` modifier (the `Box` keeps the caller modifier and the peek overlay).
- **A11ySweepFeedbackCardTest (3 tests):** card open label verbatim (composed, decomposed, blank falls back to the message), the locked forms (Undo is a separate focusable child and a custom action, Undo precedes Dismiss, both Dismiss forms exist), newest-first deck with Undo before Dismiss on every card.

## Red run (Task 2, recorded before the fix)

Commit 723583f. Of the 10 `A11ySweepUndoCenterSemanticsTest` tests, the header tests and three row tests passed on the old code; exactly the two predicted by 27-03 FA-7 failed:

- `openableRow_isOneStopWithTheMessageAndTheOpenLabel`: `the row node carries the message: []`
- `openableRow_hasNoSeparateMessageNode`: `and it is the row node expected:<82> but was:<83>` (the message was in the ListItem's child node, id 83)

After the modifier move (53fead4) both pass; FA-2 held (Material3 `ListItem` collapses with the modifier passed in).

## Layer-1 command and counts

`./gradlew testDebugUnitTest --tests '*A11ySweep*' --tests '*ReceiptRowTest' --tests '*OutcomeSheetLiveRegionTest' --tests '*ProviderKeyCardLifecycleTest' --tests '*UndoCenterScreen*' --tests '*OpenTarget*' --tests '*FeedbackStackHostSemanticsTest' --no-daemon` exits 0, 0 failures in every class:

A11ySweepSemanticsTest 9, A11ySweepUndoCenterSemanticsTest 10, A11ySweepFeedbackCardTest 3, ReceiptRowTest 31, OutcomeSheetLiveRegionTest 5, ProviderKeyCardLifecycleTest 25, FeedbackStackHostSemanticsTest 19, UndoCenterScreenTest 4, UndoCenterScreenGroupsTest 25, UndoCenterScreenDefaultParityTest 5, UndoCenterScreenLongLabelTest 4, UndoCenterScreenSourceContractTest 7, UndoCenterScreenSourceCompatV251Test 2, UndoCenterScreenOpenTargetTest 21, UndoCenterScreenOpenTargetLongLabelTest 5, UndoCenterScreenOpenParityTest 7, OpenTargetRowTest 7, OpenTargetHygieneSourceContractTest 5, OutcomeSheetOpenTargetTest 26, OutcomeSheetBatchOpenTargetTest 17, OutcomeSheetOpenTargetLongLabelTest 8, V271CallSiteCanaryTest 1.

Guards: the six guard classes (CatalogDocsDriftGuard 10, DomainVocabularyDriftGuard 2, ComponentRegistryDriftGuard 1, ComponentStatesMatrix 2, ComponentPlaygroundIntegrity 5, LibraryInvariantsGuard 5) pass; `./gradlew detekt` is green with 0 code smells; `apiDump` left `api.txt` byte-identical (no diff against c1681ff).

## Falsification controls (one example per claim)

- ReceiptRow: Undo node is asserted not inside the summary, has a different node id, and clicking it leaves the state description at Collapsed.
- Live region: a text that is not the message is asserted not under the region; a refusal-free outcome has 0 regions; a changed item does add the `, Row A` suffix.
- Saved-key: the summary itself has no `OnClick`, Update and Clear ids differ from the summary id.
- Open labels: composed and decomposed forms are asserted `!=` each other and the node never carries the other form; the label is not drawn as a text node.
- Heading: the subtitle in the same header is not a heading; exactly one heading in the header.
- Row: Undo's click label differs from the row's open label.
- Card: a Notify card has no Undo child and no Undo custom action.

## Deviations from Plan

None needing a rule. One structural choice: Task 2 was committed as a RED commit (new test class) followed by a GREEN commit (production move plus the deliberate edit), instead of one commit, to keep the red run auditable. The GREEN commit changed only `UndoCenterScreen.kt` and `UndoCenterScreenOpenTargetTest.kt`, and only inside `rowIsOneMergedButton_andUndoIsItsOwnNode`.

## Decisions and untouched items

- C-1 (refused live region): structure characterized only. No `mergeDescendants` added, `component/OutcomeSheet.kt` and `OutcomeSheetClassicParitySourceContractTest` untouched. Gate-1 G1-3 reads the real event text; PD-4 contingency recipe stands for a gap-closure plan.
- C-3 and C-4: `feedback/FeedbackCard.kt` and `feedback/FeedbackStackHost.kt` untouched (git diff against c1681ff is empty); the locked forms are pinned by `lockedForms_undoIsASeparateFocusableChild_andACustomAction_andBothDismissFormsExist`. Designer decision parked for Gate-1 G1-4.
- PD-2 (peek): the preview overlay is now a sibling of the row node; the existing unmerged-tree `peek()` helper and all hold/peek tests pass unedited.
- FA-3 did not trigger: geometry, parity and long-label tests pass unedited after the move.
- FA-4 stands: hard-coded English chrome words in the Undo Center ('Preview', 'Undo', 'Clear history', 'Back', 'Undo History') are a known localization gap, logged for v2.8.1, not widened here.

## Issues Encountered

None. All nine `A11ySweepSemanticsTest` claims held on today's code, so no plan-level defect was found in ReceiptRow, the live region, the saved-key summary or the sheet labels.

## Threat model

T-28-19 accepted (message/timestamp already exposed, opaque key still unread: `OpenTargetHygieneSourceContractTest` green). T-28-20 mitigated: the full `UndoCenterScreen*` interaction suite passes unedited. T-28-21 mitigated: locked-forms test plus empty git diff on FeedbackCard.kt and FeedbackStackHost.kt.

## Next

Plan 28-07 (Gate-1 on the TESTER): accessibility-tree dump, G1-3 live-region event text, G1-4 traversal chain and action list, TalkBack smoke; spoken-only items parked for Yahir, never waived. No tag, push or repin was done.

## Self-Check: PASSED

- Created files exist: the three A11ySweep* test classes.
- Commits found: c6f3ed6, 723583f, 53fead4, d6ba327.
