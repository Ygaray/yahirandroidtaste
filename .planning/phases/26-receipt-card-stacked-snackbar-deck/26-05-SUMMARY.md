---
phase: 26-receipt-card-stacked-snackbar-deck
plan: 05
subsystem: ui
tags: [compose, feedback-stack, accessibility, semantics, live-region, traversal, robolectric, explorer-demo]

requires:
  - phase: 26-receipt-card-stacked-snackbar-deck
    provides: "Plans 26-01 to 26-04: pause-aware timers, the deck layout (slot traversalIndex, traversal-group container), card gestures and the swipe; the merged semantics block with Role.Button, onClick label and the Undo/Dismiss custom actions"
provides:
  - "A11Y-01 on FeedbackCard: Polite live region, dismiss semantics (labelled dismissLabel) and traversalIndex = index on the one merged node"
  - "FeedbackStackHostSemanticsTest (16 Robolectric tests, merged and unmerged tree) and 3 new source contracts"
  - "Explorer FeedbackStackHost demo buttons Burst x4, Short note, Long note with Undo (English only)"
  - "API.md FeedbackStackHost row and the host KDoc describing the finished deck; no public API change"
affects: [phase-27, phase-28]

requirements-completed: [A11Y-01, FSTK-02]

actuals:
  tokens: 8500
  tasks: 3
  commits: 4

plan_head_before: e67373e9655e1a6fad3fb7e046db136d6a66ce72

tech-stack:
  added: []
  patterns:
    - "Polite on every card body with content that never changes after arrival, instead of a per-card live-region toggle (D-07 realization, FA-2)"
    - "Every accessibility action runs the same resolve-first callbacks as touch (T-26-17)"
    - "Unmerged-tree Undo child plus the same Undo as a custom action on the merged node"

key-files:
  created:
    - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHostSemanticsTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackCard.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackHost.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/FeedbackFamilyScreen.kt
    - API.md
    - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackStackSourceContractTest.kt

key-decisions:
  - "The card also carries traversalIndex = index (the deck slot from plan 26-02 already did). The test and TalkBack read the card's own merged config, so the card needs the value itself; the duplicate on the slot is harmless and keeps FeedbackDeck self-contained."
  - "FeedbackDeck.kt needed no edit: isTraversalGroup and the per-slot traversalIndex were already added by plan 26-02 (probe A6 fallback)."
  - "Both Dismiss forms stay (custom action and dismiss semantics), as D-07 decided; how TalkBack lists them is Phase 28."
  - "Host KDoc rewritten to the finished surface and the stale 'swipe, deck motion and pause are not part of this surface yet' wording (API.md row and KDoc) removed."

patterns-established:
  - "Semantics-stability test: record a card's announced content, then fan, collapse, add a newer card and promote it to newest; only traversalIndex may change"
  - "Key-hygiene test: scan SemanticsDump.allRawRoots for the opaque key after asserting the dump is not vacuous"

duration: about 20min
completed: 2026-10-17
status: complete

coverage:
  - id: D1
    description: "Each card body is one merged Role.Button node with the open label (message fallback), a Polite live region, the dismiss semantics action and custom actions [Undo, Dismiss]; Undo is a separate focusable child in the unmerged tree whose label is not in the card's merged text"
    requirement: A11Y-01
    verification:
      - kind: unit
        ref: "FeedbackStackHostSemanticsTest#targetedCard_isOneMergedButtonNode_withOpenLabelPoliteRegionAndDismiss, undoIsASeparateFocusableChild_andAlsoACustomAction, customActionOrder_isUndoThenDismiss_andNotifyHasOnlyDismiss, everyCardIsPolite_collapsedAndFanned"
        status: pass
    human_judgment: false
  - id: D2
    description: "Every accessibility entry resolves like touch: both Dismiss forms resolve Dismissed and never undo, the open action fires onOpenTarget once, spent and untargeted cards expose no open action or role"
    requirement: A11Y-01
    verification:
      - kind: unit
        ref: "FeedbackStackHostSemanticsTest#dismissSemantics_resolvesDismissed_notUndone, dismissCustomAction_resolvesDismissed_notUndone, openAction_viaSemantics_opensOnce, openAction_isAbsent_onSpentAndUntargetedCards, cardWithoutTarget_hasNoRoleNoClick_butKeepsPoliteRegionAndDismiss, spentCard_dropsRoleOnClickAndUndoAction_keepsDismissAndRegion; FeedbackStackSourceContractTest#semanticsClickCallsTheResolveFirstCallback"
        status: pass
    human_judgment: false
  - id: D3
    description: "Cards read newest first with unique traversal indices, collapsed and fanned; the deck container is a traversal group; an empty deck exposes no card node"
    requirement: A11Y-01
    verification:
      - kind: unit
        ref: "FeedbackStackHostSemanticsTest#traversalIndex_isNewestFirst_whenCollapsed, traversalIndex_isNewestFirst_whenFanned, deckContainer_isATraversalGroup, emptyDeck_exposesNoCardNode"
        status: pass
    human_judgment: false
  - id: D4
    description: "Nothing a card announces changes after arrival: content is identical across fan, collapse, a newer arrival and promotion to newest (D-07 live-region realization)"
    requirement: A11Y-01
    verification:
      - kind: unit
        ref: "FeedbackStackHostSemanticsTest#semanticsAreStableAcrossFanCollapseAndNewerArrival"
        status: pass
    human_judgment: false
  - id: D5
    description: "The opaque FeedbackTarget.key never appears in the semantics tree; no consumer or domain import in the deck files"
    requirement: A11Y-01
    verification:
      - kind: unit
        ref: "FeedbackStackHostSemanticsTest#keyNeverAppearsInTheSemanticsTree; FeedbackStackSourceContractTest#noConsumerOrDomainImportInTheFeedbackDeckFiles, cardSemanticsCarryPoliteRegionDismissAndTraversalIndex"
        status: pass
    human_judgment: false
  - id: D6
    description: "Explorer demo gains Burst x4, Short note and Long note with Undo; API.md row and host KDoc describe the finished deck; counts stay 67, 6, 73; api.txt byte-identical; additive check OK"
    requirement: FSTK-02
    verification:
      - kind: other
        ref: "CatalogDocsDriftGuardTest (ran against this worktree, passes); ./gradlew apiDump && git diff --exit-code api.txt; bash tools/verify-api-source-additive.sh v2.7.1 -> API SOURCE-ADDITIVE OK signature_changes=0; ./gradlew detekt"
        status: pass
    human_judgment: false
  - id: D7
    description: "A TalkBack user can swipe-navigate to every card (collapsed cards overlap geometrically), newest first, and hears one announcement per arriving card; TalkBack lists the two Dismiss forms as expected"
    requirement: A11Y-01
    verification: []
    human_judgment: true
    rationale: "Screen-reader behavior is physical-only: Robolectric asserts the semantics tree, not TalkBack's traversal, announcement or action list. Gate-1 reads the accessibility tree dump on the tester; the TalkBack read-out is the Phase 28 sweep (A11Y-02), flagged FA-2."
---

# Phase 26 Plan 05: Card semantics, demo and phase close Summary

**Each feedback card is now one merged `Role.Button` node with the open label, a Polite live region, both Dismiss forms and a newest-first `traversalIndex`, Undo stays a separate focusable child, and the Explorer demo, API.md row and host KDoc describe the finished deck with `api.txt` unchanged.**

## Performance

- **Duration:** about 20 min
- **Tasks:** 3 (1 tracer, 2 auto), each committed individually
- **Files modified:** 6 (1 new test, 5 modified)

## Accomplishments

- **Tracer (Task 1):** `FeedbackCard` gained an `index` parameter and, on its single merged semantics block, `liveRegion = Polite`, `traversalIndex = index` and `dismiss(label = dismissLabel) { onDismiss(); true }`. `Role.Button`, `onClick(label = openLabel.ifBlank { message })` and the custom actions [Undo, Dismiss] were already there from plan 26-03. `FeedbackStackHost` passes `index = index`. The tracer test was run red first (it failed on the missing live region) and went green with the change.
- **Matrix (Task 2):** `FeedbackStackHostSemanticsTest` has 16 tests (merged and unmerged tree): open action once, absent on spent and untargeted cards, both Dismiss forms resolve `Dismissed` and never run Undo, unique newest-first indices collapsed and fanned, the traversal-group container, Polite on every card, stable announcements across fan, collapse, a newer arrival and promotion to newest, an empty deck, and the opaque key absent from the whole tree. `FeedbackStackSourceContractTest` grew from 12 to 15 tests.
- **Demo, docs, KDoc (Task 3):** "Burst x4" (four Indefinite cards 350 ms apart; the 4th evicts the oldest), "Short note" (4 s) and "Long note with Undo" (10 s) in the private `FeedbackStackHostVariants()`; the API.md `FeedbackStackHost` row replaced the "not part of this surface yet" disclaimer with the deck, fan, swipe, pause and merged-button description (the exact phrase "Polite live region" is in it); the `FeedbackStackHost` KDoc was rewritten as deck, card, timers, accessibility and placement sections. No stale "swipe/pause not part of the surface" wording is left in `src/main` or `API.md` (grep for `not part of`, `yet`, `later phase`, `Phase 26` finds none related).

## Task Commits

1. **Task 1 (tracer): card semantics for A11Y-01 (merged button, polite region, dismiss, traversal)** - `91de05a` (lane 1)
2. **Task 2: pin the A11Y-01 semantics matrix and the live-region stability** - `8711d10` (test-only, lane 1)
3. **Task 3: Explorer demo buttons, API.md row and host KDoc for the finished deck** - `43406b9` (lane 2, committed with `HUB_LANE_OVERRIDE=2`: rewrites unreleased Phase 25/26 feedback code; no v2.7.1 signature changes)

**Plan metadata:** the docs commit that adds this SUMMARY (the 4th commit since the recorded base `e67373e`, counted in `actuals.commits`).

## Mutation check (never committed)

Commented out `liveRegion = LiveRegionMode.Polite` in `FeedbackCard.kt` and re-ran `FeedbackStackHostSemanticsTest`: **5 of 16 failed**, including `everyCardIsPolite_collapsedAndFanned` (plus `targetedCard_...`, `cardWithoutTarget_...`, `spentCard_...` and `semanticsAreStableAcrossFanCollapseAndNewerArrival`). Restored with `git checkout -- FeedbackCard.kt`; the file matches the committed content and the full battery below ran on the restored code.

## Deviations from Plan

1. **[Note] `FeedbackDeck.kt` not modified.** The plan lists it, but plan 26-02 already put `isTraversalGroup = true` on the deck container and `traversalIndex = index` on each slot (probe A6 fallback). Task 1's greps for `isTraversalGroup` and `index = index` pass on the existing code. The card now also carries its own `traversalIndex`, because the A11Y-01 assertion reads the card's merged config; the slot copy is redundant but harmless and keeps the deck self-contained.
2. **[Precondition, flagged] Task 3 precondition unmet as written.** The precondition asks for the main checkout (directory named `yahirandroidtaste`); this executor runs in a worktree. The dispatch note says the drift guards that scan the main checkout are vacuous here and that the orchestrator re-runs them after the merge, so I continued instead of returning a checkpoint. What that means for the guard run (see Verification): `CatalogDocsDriftGuardTest` and `LibraryInvariantsGuardTest` locate the root by `build.gradle.kts` (+ `API.md` / `src/main`) and so ran against this worktree; `ComponentRegistryDriftGuardTest` and `DomainVocabularyDriftGuardTest` walk up to a directory named `yahirandroidtaste` and so scanned the main checkout, not these edits. **The orchestrator must re-run all six guards from the main checkout after the merge.**
3. **[Note] Phase gate run in the worktree, not the main checkout.** Item 3 of the plan's `<verification>` (full fresh battery, `run-all.sh`) ran here, with `--rerun-tasks`. The rest of items 1-2 and 4 hold as stated.
4. **[Note] Demo wording.** The Long WithUndo demo message is "Long note" (button label "Long note with Undo"); the plan only fixed the button labels.

**Total deviations:** 0 auto-fixed bugs, 1 precondition proceeded on the orchestrator's instruction, 3 notes. **Impact:** none on scope or public API.

## Verification (fresh runs on this worktree, `--no-daemon`)

- Tracer: red first (failed on the missing `LiveRegion`), then `FeedbackStackHostSemanticsTest` 1, `FeedbackStackHostTest` 12, `FeedbackStackHostDeckTest` 25, `FeedbackStackHostSwipeTest` 20, `FeedbackDeckLayoutTest` 11 all `failures="0"`.
- Matrix: `FeedbackStackHostSemanticsTest` 16, `FeedbackStackSourceContractTest` 15 (contains `cardSemanticsCarryPoliteRegionDismissAndTraversalIndex`, `semanticsClickCallsTheResolveFirstCallback`, `noConsumerOrDomainImportInTheFeedbackDeckFiles`), the three other host suites unchanged, all `failures="0"`.
- Guard run: the six guard classes (CatalogDocsDriftGuardTest 10, DomainVocabularyDriftGuardTest 2, ComponentRegistryDriftGuardTest 1, ComponentStatesMatrixTest 2, ComponentPlaygroundIntegrityTest 5, LibraryInvariantsGuardTest 5) all `failures="0"` (vacuity caveat in Deviation 2). Acceptance greps: `**67 registered public composables**` 1, `= **73 public composables total**` 1, `## Intentionally-unregistered sub-parts (6)` 1, `Polite live region` 1, `name = "FeedbackStackHost"` 1, `Burst x4` / `Short note` / `Long note with Undo` / `SnackbarDuration.Short` / `SnackbarDuration.Long` each present.
- `./gradlew apiDump` then `git diff --exit-code api.txt`: exit 0 (no public change). `bash tools/verify-api-source-additive.sh v2.7.1`: `API SOURCE-ADDITIVE OK ... removed_classes=0 removed_members=0 required_param_changes=0 signature_changes=0`. `./gradlew detekt`: success at zero baseline.
- Full fresh gate `./gradlew apiDump testDebugUnitTest detekt apiCheck --rerun-tasks --no-daemon`: BUILD SUCCESSFUL in 2m31s; 157 result classes, 1588 tests, 0 failures, 0 errors, 22 skipped (pre-existing `@Ignore`d tests, not touched here). `bash tools/test/run-all.sh`: all suites ok (additive script 34 of 34 cases, binary-ABI 52 of 52).
- `git tag --list 'v2.8*'`: empty. Nothing pushed, tagged or repinned. `git log --name-only e67373e..HEAD` lists only the files named by the plan (no debris path). Registry and docs counts stay 67 registered, 6 intentionally-unregistered, 73 total.
- No device work was done. Behavioral verification on the tester is the Gate-1 run owned by the orchestrator.

## Live-region realization and flagged assumptions (for the Gate-1 reviewer and the ui-designer follow-up)

- **FA-2 (this plan):** D-07 [live-region] "only the arriving card announces" is realized as "Polite on every card body, with content that never changes after arrival", because brief D-11 and ROADMAP SC5 require the region on each body. `semanticsAreStableAcrossFanCollapseAndNewerArrival` pins the no-change half; confirm on the tester in Phase 28 that TalkBack announces only the arriving card and how it lists the two Dismiss forms.
- **FA-8 (this plan):** the new demo buttons are English only; German parity is Phase 28 (DEMO-03).
- **FA-1:** the peek strip stays 14dp (locked brief D-01 and ROADMAP SC1 outrank D-06's 24dp); settled by the ui-designer, the 48dp target is met through the per-card semantics path (each card stays a full-size merged node with open, Undo and Dismiss actions). Gate-1 still checks swipe versus tap on the strip.
- **FA-3:** the deck and card never measure, compare or truncate text; Unicode normalization forms are not separately tested.
- **FA-4:** FSTK-05 was unclassified by the generic probe; the manual edge list (plan 26-04) is its resolution.
- **FA-5:** completion order among cards that expire at the same instant is unspecified.
- **FA-6:** no exit-animation ghosts and no arrival animation beyond the fan spring.
- **FA-7:** the IME inset is handled by the fanned height cap only; verified on the tester at Gate-1.
- **FA-9:** a single unbreakable word wider than 60 percent of the row still breaks mid-word.
- **Backstop truth (A11Y-01 adjacency):** "a TalkBack user can swipe-navigate to every card, collapsed or fanned, newest first, with none unreachable because the collapsed cards overlap geometrically, and gets one announcement per arriving card" is a backstop; Robolectric asserts the tree, not TalkBack. It abstains to human review until the Phase 28 sweep.

## Gate-1 inputs

The Explorer Feedback family screen's FeedbackStackHost demo now has the buttons Note with target, Note with Undo and target, Plain note, Burst x4, Short note, Long note with Undo and Dismiss all, as the plan's Gate-1 mapping (SC1 to SC5) expects. TESTER `R5CT10XNKQN`; restore display density with `wm density 450`, never `wm density reset`; wireless devices are never touched. The self-UAT run and its evidence are produced by the gate, not by this plan.

## Issues Encountered

None beyond the deviations above. The worktree-isolation hook refused two compound shell commands; both were re-issued as plain commands with no effect on the result.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- All five plans of Phase 26 are summarized. FSTK-02 to FSTK-06 and A11Y-01 hold at unit and Robolectric level; device claims await Gate-1, TalkBack confirmation is Phase 28.
- After merging this worktree, the orchestrator should re-run from `/home/yahir/Projects/Reusable/android/yahirandroidtaste`: the six drift guards (item 2 of the plan's verification) and, ideally, `git diff --exit-code api.txt`. No tag is cut; v2.8.0 and every push remain Yahir's.

## Self-Check: PASSED

- Files exist: FeedbackStackHostSemanticsTest.kt, FeedbackCard.kt, FeedbackStackHost.kt, FeedbackFamilyScreen.kt, FeedbackStackSourceContractTest.kt, API.md.
- Commits `91de05a`, `8711d10`, `43406b9` present; 3 task commits since the recorded base `e67373e` plus this SUMMARY commit give `actuals.commits: 4`.
- Acceptance literals re-checked (Polite 1, `dismiss(label` 1, `traversalIndex` present in the card, `isTraversalGroup` in the deck, `index = index` 1); result XMLs `failures="0"` in the final full run.

---
*Phase: 26-receipt-card-stacked-snackbar-deck*
*Completed: 2026-10-17*
