---
phase: 28-talkback-sweep-explorer-demos
plan: 12
subsystem: feedback, component
tags: [a11y, talkback, gap-closure, double-read, live-region, platform-node]
requires:
  - phase: 28-talkback-sweep-explorer-demos
    provides: "28-11 names for every deck node (C-5) and the focus route; Gate-1 re-run 2 (commit 0598142, uat_run uatr-20261018T055800Z-2b1265c4) found the double read (C-1 read twice, C-6 cards doubled)"
provides:
  - "Every stacked-snackbar card root and Undo child has exactly ONE speech source on the platform node (C-6)"
  - "The Undo-refused message has exactly ONE mechanism, the Assertive live region, and it fires after the sheet has settled (C-1, route a)"
  - "Unit tests that read the platform AccessibilityNodeInfo (what TalkBack reads), not only Compose semantics"
affects: [28-gate-1-rerun]
tech-stack:
  added: []
  patterns:
    - "hideFromAccessibility() on the drawn Text of a node whose explicit contentDescription is the name: Text stays drawn and stays in the semantics tree, the platform node is marked not visible to the user"
    - "merge boundary + clearAndSetSemantics {} (innermost) to give a third-party button one name, role and click and no drawn-label child"
    - "two call sites of one content lambda to make a state flip a node REPLACEMENT (a live-region node appearing) instead of an in-place property change"
key-files:
  created: []
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackCard.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheet.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHostSemanticsTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetLiveRegionTest.kt
key-decisions:
  - "Single source asserted on the PLATFORM AccessibilityNodeInfo (built through the Compose accessibility delegate), not on Compose semantics; limit named below"
  - "Settle signal: the refusal node's own window position (6 stable frames, at least 300 ms, 1.5 s cap), the 28-11 technique, helper renamed awaitPositionAtRest; the SheetState is not reachable from UndoAffordanceBody"
  - "Drawn lines are hidden with hideFromAccessibility(), not cleared with clearAndSetSemantics, so the Text stays in the merged and unmerged trees that many tests outside the six files read"
requirements-completed: [A11Y-02]
status: complete
duration: ~2h 30m
completed: 2026-10-18
commits: 2
plan_head_before: cfca4cbf13e677fb618d395d2cb3f1a8528afbb3
actuals:
  tokens: 13000
  tasks: 3
  commits: 2
---

# Phase 28 Plan 12: One read, not two. Single live-region route for the refusal (C-1) and card names spoken once (C-6) Summary

**Every card root and Undo child now has one speech source on the platform node, and the Undo-refused message is a single Assertive live region whose named node appears only after the sheet has settled. The focus move of 28-11 is gone. This is proven at unit level on the real AccessibilityNodeInfo; whether TalkBack now speaks it once is NOT proven here and is the Gate-1 re-run's measurement.**

## Root cause, measured instead of reasoned

The earlier plans reasoned that "a description replaces the merged text for speech". The platform node says otherwise. Building the real `AccessibilityNodeInfo` tree through the Compose accessibility delegate (this Compose version, `androidx.compose.ui` 1.11) shows, for a node that merges its descendants:

- the node itself has `text=null desc=null`;
- its own `contentDescription` is exposed as a **fake child node** (id 2000000000 + node id, `desc=<name>`), and a `Role` as another fake child;
- every visible descendant `Text` is exposed as a child `TextView`.

TalkBack aggregates the node's name from those children, so an explicit description plus a drawn Text is the message twice. The unit probe reproduced the device exactly (`Saved, Draft | Saved | Draft`, `Undo | Undo`, `Burst four | Burst four`) before the fix. A `Text` marked with `hideFromAccessibility()` stays in the Compose tree but its platform child becomes `visibleToUser=false importantForAccessibility=false`, the same flag mechanism the plan-28-10 hide of the middle strip already uses (device-verified to remove that stop).

## What was built

**Task 1, FeedbackCard.kt (commit 170b2d1):**
- The message Text and the second-line Text carry `Modifier.semantics { hideFromAccessibility() }`. The card root keeps `contentDescription = feedbackCardName(message, supportingText)` and is the single source. The Polite live region, traversalIndex, onClick label, role, dismiss, custom actions and the 28-10 middle-strip hide are untouched.
- The Undo `DynamicActionButton` modifier is now `testTag` then `semantics(mergeDescendants = true) { contentDescription = undoLabel; role = Button; onClick { onUndo(); true } }` then an innermost `clearAndSetSemantics { }`. The innermost clear removes the button's own semantics and its drawn label (the second source); the outer block re-declares the single name, role and click and keeps the node a merge boundary, so it is still a separate focusable child of the card and still not part of the card name. Plain `clearAndSetSemantics { contentDescription = ... }` alone broke the click and the separate-child form (8 existing tests failed), which is why the merge block is outside the clear.

**Task 2, OutcomeSheet.kt (commit 55f5288):**
- Removed: `FocusRequester`, `focusRequester(...)`, `focusable()`, the `LaunchedEffect` focus request, their imports. `grep -c 'focusRequester\|awaitSheetSettled\|announceForAccessibility'` prints 0 and `grep -c 'liveRegion = LiveRegionMode\.'` prints 1.
- The refused surface is composed at once (layout and visible text unchanged), silent: no name, drawn Text hidden from accessibility. A `LaunchedEffect(refusedMessage)` waits for the surface to be at rest (`awaitPositionAtRest`) and sets `spoken`. The named surface is then a **new node**, because the content lambda is invoked from two call sites in `ReplaceWhenChanged` (an `if/else` that composes the same content in each branch): the flip removes one group and inserts the other, so the platform sees one live-region node appearing after the window exists. A different message later resets `spoken`, so it goes the same way. The node has no click action, keeps the testTag and the pinned `.testTag(...).semantics(mergeDescendants = true) { liveRegion = ... }` chain.

## Which settle signal

The refusal node's own window position (`onGloballyPositioned`): unchanged for 6 frames and at least 300 ms elapsed, 1.5 s cap so the message is always reached. This is the 28-11 wait with the constants unchanged, renamed `awaitPositionAtRest`. The `SheetState` is not used (not reachable from `UndoAffordanceBody` without editing pinned, outside-scope signatures, 28-11 deviation 2). A real `ModalBottomSheet` test shows the node is below the window edge (absent from the platform tree) while the sheet slides, then spoken once.

## Single-source property: platform node, and the limit

Asserted on the **platform `AccessibilityNodeInfo`**, not only Compose semantics: the tests take `node.root` (the `AndroidComposeView`), enable an accessibility service in `ShadowAccessibilityManager`, force the delegate to rebuild its node map (a private field read by reflection, tests only), call `view.accessibilityNodeProvider.createAccessibilityNodeInfo(id)` and walk children through the hidden `getChildId`/`getVirtualDescendantId`. "Spoken" is a TalkBack-style aggregation of text and description of the node plus every child that is visible to the user and is not a stop of its own; the message must appear exactly once.

Limits, stated plainly:
1. Robolectric has no screen reader, so the aggregation is my emulation of how TalkBack builds a name. The actual utterance count is a Gate-1 device measurement.
2. These tests need `GraphicsMode.NATIVE` (the legacy Region makes Compose treat every node as covered and the provider returns nothing). They are annotated per method, not per class.
3. In this Robolectric the card's description fake node only appears once the deck holds more than one card (a lone card exposes only its Text children); the device builds it in decks. So every card test hosts at least two cards. The lone-card case is single source either way.
4. The probe also showed the event shapes (below); how TalkBack treats them is not testable here.

## Event shapes seen (Robolectric, probes before choosing the design)

| Design tried | Events after the settle on the live-region node |
|---|---|
| name absent then present on the same node | 2: subtree (ct=1) and description (ct=4) |
| name "" then message | 3: fake child ct=4, ancestor ct=1, node ct=4 |
| live region toggled on a node that always has the name | 2: ancestor ct=1, node ct=0 |
| **named node replaces the silent node (chosen)** | **1 on the node: ct=1 (the same shape as the device's appear event `changeTypes=1 srcLive=2`)**, plus one subtree event on the container |

The chosen design gives exactly one content-change event on the named live-region node per showing, equal-state recomposition gives none, and a new message gives exactly one on its new node.

## Tests

- `FeedbackStackHostSemanticsTest`: 9 new tests, all red on the 28-11 code for the right reason (`Burst four | Burst four`, `Undo | Undo`, `Saved, Draft | Saved | Draft`): front card, second line, card with Undo (and not the Undo label), Undo child English and German, collapsed deck English and German, fanned deck, plus `cardBodyText_staysDrawnAndInTheTree_butIsHiddenFromAccessibility`. Existing 28-10/28-11 tests pass unchanged except three comments that wrongly claimed a description replaces text.
- `OutcomeSheetLiveRegionTest`: the 28-11 focus tests (focusable, focus request counts, view-focused event, real-sheet focus, the `.requestFocus()` source count) were replaced because their route no longer exists. New: `refusedNode_isNotFocusRequested_andHasNoFocusMachinery` (source assertion, also exactly one `liveRegion = LiveRegionMode.`), `refusedNode_messageIsExposedOnceAcrossTextAndDescription`, `..._withChangedItem_...`, `..._german`, `refusedNode_exposesNoSpeechBeforeTheSheetHasSettled`, `refusedNode_exposesTheFullMessageAfterTheSettle`, `exactlyOneContentChangeEventPerShowing`, `equalStateRecomposition_causesNoSecondChange`, `aNewRefusal_causesOneNewChange`, `refusalInARealSheet_isSilentUntilTheSheetHasSettled_thenSpokenOnce`. The 28-09 tests (only Assertive region, no click action, merged node carries the message as its Text) pass unchanged. Run against the 28-11 `OutcomeSheet.kt`, 10 of the 19 fail (red phase confirmed).
- `A11ySweepFeedbackCardTest` and `OutcomeSheetClassicParitySourceContractTest`: **not edited**. The pinned refused-surface chain `.testTag(...).semantics(mergeDescendants=true){liveRegion=...}` is intact inside `UndoAffordanceBody`, so the plan's one-string allowance was not used.

## Verification (commands and results)

- Task 1 battery (`*FeedbackStack*`, `*A11ySweepFeedbackCardTest`, `*FeedbackCard*`, `*FeedbackDeck*`, `*LibraryInvariantsGuardTest`, `*Phase28HygieneSourceContractTest`): BUILD SUCCESSFUL; also `*FeedbackStackDemoRegistryRenderTest`, `*DomainVocabularyDriftGuardTest`: BUILD SUCCESSFUL.
- Task 2 battery (`*OutcomeSheetLiveRegionTest`, `*OutcomeSheetClassicParitySourceContractTest`, `*OutcomeSheet*`, `*Receipt*`, `*LibraryInvariantsGuardTest`, `*Phase28HygieneSourceContractTest`, plus `*A11ySweep*` and `*OpenTargetHygieneSourceContractTest`, 421 tests): BUILD SUCCESSFUL.
- `./gradlew testDebugUnitTest --rerun-tasks --no-build-cache --no-daemon` (35 of 35 tasks executed, forced fresh, whole unit suite): BUILD SUCCESSFUL.
- `./gradlew apiDump --no-daemon` then `git diff --exit-code api.txt`: exit 0, api.txt byte-identical.
- `./gradlew detekt --no-daemon`: BUILD SUCCESSFUL, zero baseline.
- `bash tools/test/run-all.sh`: every script PASS, FAIL=0 (the BLOCKED lines in its output are the hook script's own negative cases).
- `git diff --stat` against the plan base: 4 of the 6 named files (`A11ySweepFeedbackCardTest.kt`, `OutcomeSheetClassicParitySourceContractTest.kt` needed no edit). No STATE.md or ROADMAP.md change, no device touched, no `--no-verify`.
- Both commits used `HUB_LANE_OVERRIDE=2` after the hook blocked the first attempt (reason: internal edit, no public signature changed, api.txt unchanged), as the plan authorises.

## Deviations from Plan

**1. [Rule 1/3 - Blocking] The drawn lines are hidden with `hideFromAccessibility()`, not cleared with `clearAndSetSemantics {}`.**
- Plan suggested `clearAndSetSemantics {}` on the Texts "or an equivalent that keeps the drawn text visible". Clearing removes the Text from the merged and the unmerged trees, and many tests outside the six files find cards and Undo buttons through them (`FeedbackStackTestSupport.undoOf` uses `hasAnyDescendant(hasText)`, `FeedbackStackHostDeckTest`, `FeedbackStackDemoRegistryRenderTest`, `A11ySweepSemanticsTest`'s refusal lookup). `hideFromAccessibility()` keeps the Text in both trees and removes it from the platform speech (`visibleToUser=false`). The plan's truth allows "hidden".
- The Undo child could not use it (its Text is inside the shared `DynamicActionButton`, which is not in the file set), so it uses merge boundary plus innermost clear, re-declaring name, role and click (`FeedbackCard.kt`). Dropped from the Undo node by the clear: the `Focused` and `RequestFocus` semantics entries. Keyboard focus is unaffected (the focus system is separate from semantics); the node stays clickable for TalkBack and Switch Access. Flag for the Gate-1 re-run: confirm the Undo child is still reachable and activatable with TalkBack and by keyboard.

**2. [Rule 3 - Blocking] The named refusal node replaces the silent one through two call sites, not through `androidx.compose.runtime.key`.**
- A `key(...)` replacement was the first working design (events probed). `OpenTargetHygieneSourceContractTest.noFileReadsTheOpaqueTargetKey` (outside the six files) greps `\.key\b` in `OutcomeSheet.kt` and the `key` import trips it, so the replacement is an `if/else` that invokes the same content lambda in each branch (`ReplaceWhenChanged`). Same effect, verified by the event count test.

**3. [Rule 2 - correctness] A new refusal message resets `spoken` to false before settling again.** Keeps "a showing is a new refusal message" one live-region node appearing with its name each time, instead of an in-place change that produced two events in the probe.

**4. Test-only: the Robolectric facts above** (GraphicsMode per method, at least two cards for the card node, a reflective node-map refresh, the sheet window node being absent from the platform tree while it slides). The last one means the pre-settle assertion for a real sheet accepts "no platform node" as silent.

**Total deviations:** 4 (1 and 2 forced by files outside the set; none changes scope or API). **Impact:** none on the public surface.

## Risks for the Gate-1 re-run (what this plan cannot prove)

- Whether Samsung TalkBack 15.1 skips children that are `visibleToUser=false` when it builds the name (the 28-10 hide proved the flag removes a focus STOP; skipping hidden CHILDREN in the aggregation is the same flag but is not yet device-proven). If a card is still read twice, this is the first place to look.
- Whether the one appear event (`TYPE_WINDOW_CONTENT_CHANGED changeTypes=1`, plus one subtree event on the container) of the named live-region node yields exactly one utterance, and whether the silent first node's appear event stays silent.
- The settle constants (300 ms, 6 frames, 1.5 s) are the knobs if the named node appears too early or too late.
- The TalkBack-alone setup (5 of 5, one utterance, TTS request count 1) and the English and German card stops (`X, Button` once, `Undo, Button` once) are the acceptance. A failure is the third failure and escalates to Yahir per the 2026-10-18 ruling.

## Self-Check: PASSED

- Modified files present and committed: `FeedbackCard.kt`, `OutcomeSheet.kt`, `FeedbackStackHostSemanticsTest.kt`, `OutcomeSheetLiveRegionTest.kt`.
- Commits 170b2d1 and 55f5288 exist on the worktree branch; `git rev-list --count cfca4cb..HEAD` was 2 at SUMMARY time; working tree clean before this file. The scratch probe test was never committed.
