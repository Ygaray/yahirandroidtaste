---
phase: 12-generic-needs-confirmation-state
plan: "01"
subsystem: ui
tags: [compose, jetpack-compose, material3, sealed-interface, metalava, hub-governance]

# Dependency graph
requires:
  - phase: 11-voice-outcome-failure-sheet
    provides: "The sealed VoiceOutcomeUiState (Success/Failure) and OutcomeSheet's exhaustive `when`, extended here with a third additive arm."
provides:
  - "VoiceOutcomeUiState.NeedsConfirmation -- a domain-neutral sealed arm covering both single and batch confirm shapes"
  - "ProposedItemUiModel + SelectionMode -- the per-item/selection-mode model vocabulary"
  - "OutcomeSheet's NeedsConfirmationBody/ProposedItemRow private rendering"
  - "Two Explorer gallery fixtures (single destructive, batch with topLevelContent + per-item remove)"
affects: [13-ship-and-register, secondbrain-mutationgate-wiring, caltracker-voiceresultsheet-wiring]

# Actuals (#2632)
actuals:
  tokens: 9471
  tasks: 2
  commits: 2
plan_head_before: d6163d8b1e68153083f8a90cdfbd412b7cdae7f7

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Additive sealed-interface arm + exhaustive `when` compiler enforcement (mirrors Success/Failure)"
    - "Null-prop-hides optional fields (topLevelContent, onRemove, trailingContent, confidenceCue)"
    - "Privacy-safe toString() override omitting PII-bearing fields (mirrors SecondBrain's PendingConfirmation.toString())"
    - "Shared testTag-per-row convention for indexed Compose test access, with explicit semantics(mergeDescendants=true) + useUnmergedTree for a nested interactive child"
    - "Metalava apiDump discipline: run ./gradlew apiDump and commit api.txt alongside any additive sealed-arm/public-symbol change, since Metalava's AddedSubclassToSealedClass check flags new sealed subclasses relative to a stale baseline"

key-files:
  created:
    - src/main/java/io/github/ygaray/yahirandroidtaste/model/ProposedItemUiModel.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/model/VoiceOutcomeUiState.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheet.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/VoiceCommandFamilyScreen.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetTest.kt
    - api.txt

key-decisions:
  - "Reused the existing public ActionButtonDefaults.ActionButtonRole enum for NeedsConfirmation.severity instead of inventing a new ConfirmSeverity type (D-05, Don't Hand-Roll) -- zero new public enum surface."
  - "Reverted two optional KDoc rewordings (VoiceOutcomeUiState's top-of-interface doc, OutcomeSheet's function doc) back to their original Phase-11 wording after the repo's own DS-05 append-only pre-commit guard (tools/verify-additive-diff.sh) flagged them as non-additive line rewrites -- kept all documentation of the new behavior scoped to newly-added doc blocks instead."
  - "Ran ./gradlew apiDump and committed the resulting api.txt delta in Task 2 to satisfy Metalava's AddedSubclassToSealedClass compatibility check, which flags any new subclass to the `exhaustive` sealed VoiceOutcomeUiState relative to the last committed api.txt baseline -- this is the project's documented apiDump discipline (tools/README-api-guard.md), not a scope deviation."

patterns-established:
  - "NeedsConfirmationBody/ProposedItemRow: the private-body-function-per-sealed-arm pattern (SuccessBody/FailureBody/UndoAffordanceBody) now has a third precedent."

requirements-completed: [VOUT-04]

coverage:
  - id: D1
    description: "NeedsConfirmation renders a single destructive item end-to-end: title, reason, reversibilityHint, item title/subtitle, and Confirm/Cancel buttons wired to their own callbacks."
    requirement: "VOUT-04"
    verification:
      - kind: unit
        ref: "src/test/.../OutcomeSheetTest.kt#NeedsConfirmation with a single item renders its title, reason, reversibilityHint, and the item's title and subtitle"
        status: pass
      - kind: unit
        ref: "src/test/.../OutcomeSheetTest.kt#tapping the Confirm button invokes onConfirm exactly once and does not invoke onCancel"
        status: pass
      - kind: unit
        ref: "src/test/.../OutcomeSheetTest.kt#tapping the Cancel button invokes onCancel exactly once and does not invoke onConfirm"
        status: pass
    human_judgment: false
  - id: D2
    description: "ProposedItemUiModel.toString() never prints title/subtitle/confidenceCue -- privacy-safe representation."
    requirement: "VOUT-04"
    verification:
      - kind: unit
        ref: "src/test/.../OutcomeSheetTest.kt#ProposedItemUiModel toString never prints title, subtitle, or confidenceCue"
        status: pass
    human_judgment: false
  - id: D3
    description: "A batch of N items renders exactly N rows in supplied order; a shared topLevelContent slot renders exactly once regardless of item count; per-item remove invokes only its own row's callback; duplicate id/title never merges rows; empty items renders zero rows without crashing; destructive severity still fires onConfirm on tap."
    requirement: "VOUT-04"
    verification:
      - kind: unit
        ref: "src/test/.../OutcomeSheetTest.kt#a batch of 3 items renders exactly 3 rows in the exact supplied order, and topLevelContent renders exactly once"
        status: pass
      - kind: unit
        ref: "src/test/.../OutcomeSheetTest.kt#tapping a specific row's remove control invokes THAT row's own onRemove and no other row's"
        status: pass
      - kind: unit
        ref: "src/test/.../OutcomeSheetTest.kt#two items sharing the same id, and separately two sharing the same title, both render as 2 separate rows, never merged"
        status: pass
      - kind: unit
        ref: "src/test/.../OutcomeSheetTest.kt#items with duplicate titles still render in the exact supplied list order, never resorted"
        status: pass
      - kind: unit
        ref: "src/test/.../OutcomeSheetTest.kt#an empty items list renders zero item rows without crashing, while reason and both buttons still render"
        status: pass
      - kind: unit
        ref: "src/test/.../OutcomeSheetTest.kt#severity Destructive still renders a clickable Confirm button whose tap invokes onConfirm"
        status: pass
    human_judgment: false
  - id: D4
    description: "Swipe-to-dismiss/outside-tap/back on an open NeedsConfirmation sheet behaves as decline without crashing or flashing; destructive-severity visual treatment reads correctly on-device; a batch confirm with at least one per-item remove tap works end-to-end on a real device."
    verification: []
    human_judgment: true
    rationale: "Per 12-VALIDATION.md's Manual-Only Verifications table: gesture-driven dismissal and rendered-color visual review are out of Robolectric's reach in this repo (confirmed precedent in CardTagRowTest.kt); reserved for Gate-1 self-UAT, not this executor's lane."

duration: 19min
completed: 2026-10-01
status: complete
---

# Phase 12 Plan 01: Generic Needs-Confirmation State Summary

**`VoiceOutcomeUiState.NeedsConfirmation` -- a purely additive, domain-neutral third sealed arm rendering both single-item destructive confirms (SecondBrain) and batch confirms with a shared `topLevelContent` slot and per-item remove (CalTracker), reusing `ActionButtonDefaults.ActionButtonRole` for severity with zero new public enum.**

## Performance

- **Duration:** ~19 min
- **Started:** 2026-10-01T03:14:55Z
- **Completed:** 2026-10-01T03:33:19Z
- **Tasks:** 2
- **Files modified:** 6 (5 modified, 1 created)

## Accomplishments
- `VoiceOutcomeUiState.NeedsConfirmation` ships as a new sealed-interface arm (`reason`, `items`, `selectionMode`, `title`, `severity`, `reversibilityHint`, `confirmLabel`, `cancelLabel`, `topLevelContent`, `onConfirm`, `onCancel`) -- `Success`/`Failure` are byte-identical to Phase 11.
- `ProposedItemUiModel` (new file) + `SelectionMode` enum model the per-item/batch vocabulary, cross-checked against SecondBrain's real `ConfirmSubject`/`PendingConfirmation` and CalTracker's real `ProposedBatch`/`BatchItemState` shapes from 12-RESEARCH.md.
- `OutcomeSheet.kt`'s exhaustive `when` gains the `NeedsConfirmation` branch; two new private composables (`NeedsConfirmationBody`, `ProposedItemRow`) mirror the existing `SuccessBody`/`UndoAffordanceBody` pattern exactly -- no new top-level composable, no `ComponentRegistry` edit, no drift-guard allowlist edit.
- The SAME render path handles both a single item (size 1) and a batch (size N) -- confirmed by both a single-destructive-item fixture/tests and a 3-item batch fixture/tests exercising the identical composables.
- Full phase-closing verification green: `./gradlew testDebugUnitTest` (25 OutcomeSheetTest cases + both full-suite drift guards), `./gradlew detekt` (0 code smells, zero baseline), `./gradlew apiCheck` (additive, after `apiDump`).

## Task Commits

Each task was committed atomically:

1. **Task 1: NeedsConfirmation end-to-end -- single destructive item, Confirm/Cancel callbacks** - `7c9b373` (feat)
2. **Task 2: Batch + shared topLevelContent + per-item remove + edge coverage + phase-closing full verification** - `e11add1` (feat)

**Plan metadata:** pending (this SUMMARY.md + REQUIREMENTS.md commit, made immediately after this file)

_Note: Task 1 is `type="tracer"` per the plan; its own `<verify>` (the fast-feedback `OutcomeSheetTest` run) passed before Task 2 began, so no tracer-feedback checkpoint was triggered._

## Files Created/Modified
- `src/main/java/io/github/ygaray/yahirandroidtaste/model/ProposedItemUiModel.kt` - New: `ProposedItemUiModel` (id/title/subtitle/confidenceCue/amended/onRemove/trailingContent, privacy-safe `toString()`) + `SelectionMode` enum (AllOrNothing, PerItem)
- `src/main/java/io/github/ygaray/yahirandroidtaste/model/VoiceOutcomeUiState.kt` - Added `NeedsConfirmation` as a third sealed arm; `Success`/`Failure` unchanged
- `src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheet.kt` - Added the `NeedsConfirmation` `when` branch and two new private composables (`NeedsConfirmationBody`, `ProposedItemRow`)
- `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/VoiceCommandFamilyScreen.kt` - Added two new `OutcomeSheet` gallery fixtures (single destructive, batch) inside the existing `OutcomeSheetVariants()`
- `src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetTest.kt` - Added the VOUT-04 test section: single-item render/callback/privacy tests (Task 1) + batch/topLevelContent/per-item-remove/adjacency/ordering/empty-items/destructive-severity tests (Task 2)
- `api.txt` - Regenerated via `./gradlew apiDump` to include the new `NeedsConfirmation`/`ProposedItemUiModel`/`SelectionMode` public symbols

## Decisions Made
- Reused `ActionButtonDefaults.ActionButtonRole` for `NeedsConfirmation.severity` rather than a new severity enum (D-05, Don't Hand-Roll) -- zero new public enum surface, `Destructive` maps directly to the red confirm button.
- Kept `PerItem` (`SelectionMode`) reserved with no rendering recipe -- neither live consumer (SecondBrain or CalTracker) exercises it this phase (per D-03/D-06 and 12-RESEARCH.md's Alternatives Considered).
- Did not invoke `item.trailingContent`/`item.onRemove` or `confirmation.topLevelContent` in Task 1 (deferred to Task 2 per the plan's explicit task-scoping) -- Task 1 proved the end-to-end single-item path first (tracer discipline).

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Reverted two optional KDoc rewordings that tripped the repo's DS-05 append-only pre-commit guard**
- **Found during:** Task 1 (first commit attempt)
- **Issue:** Beyond the plan's required new-arm KDoc, I had also reworded two pre-existing comment blocks (the sealed interface's top-of-file doc "Exactly two arms this phase..." and `OutcomeSheet`'s function KDoc) to read more naturally with the new third arm. The repo's `tools/hooks/pre-commit` runs `tools/classify-hub-change.sh`, which calls `tools/verify-additive-diff.sh` (DS-05): any content line removed/rewritten without an identical line re-added anywhere fails the commit as "lane 2" (non-additive) on the fast path.
- **Fix:** Reverted both doc blocks to their exact original Phase-11 wording; all new documentation of `NeedsConfirmation`'s behavior lives in newly-added KDoc (the new arm's own doc, plus one new appended sentence on `OutcomeSheet`'s doc) rather than edits to pre-existing lines.
- **Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/model/VoiceOutcomeUiState.kt`, `src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheet.kt`
- **Verification:** `bash tools/verify-additive-diff.sh v2.3.0` reports `DS-05 PASS: 0 removed line(s)`; commit then classified `LANE 1 (mode=additive)`.
- **Committed in:** `7c9b373` (Task 1 commit)

**2. [Rule 3 - Blocking] Ran `./gradlew apiDump` to satisfy Metalava's `AddedSubclassToSealedClass` compatibility check**
- **Found during:** Task 2 (phase-closing `apiCheck` run)
- **Issue:** `./gradlew apiCheck` failed with `error: Added a subclass to a sealed interface that can be exhaustively matched [AddedSubclassToSealedClass]` -- Metalava's compatibility checker specifically flags any new subclass added to an `exhaustive` sealed interface relative to the committed `api.txt` baseline (a genuine signal: a consumer's own non-`else` exhaustive `when` on `VoiceOutcomeUiState` would need updating). This is exactly the intentional, locked (D-02) change the whole phase exists to make.
- **Fix:** Ran `./gradlew apiDump` to regenerate `api.txt` with the new `NeedsConfirmation`/`ProposedItemUiModel`/`SelectionMode` symbols, per the project's own documented discipline (`tools/README-api-guard.md`: "On every additive change, run `./gradlew apiDump` and commit the updated `$API_FILE` in the same commit"). `apiCheck` then compares current-vs-freshly-dumped-baseline (identical) and passes.
- **Files modified:** `api.txt` (69 pure-append lines)
- **Verification:** `./gradlew apiCheck` exits 0; `bash tools/verify-additive-diff.sh v2.3.0` still reports 0 removed lines with `api.txt` included; commit classified `LANE 1 (mode=additive)`.
- **Committed in:** `e11add1` (Task 2 commit)

---

**Total deviations:** 2 auto-fixed (both Rule 3 - blocking issues preventing a green commit/verification)
**Impact on plan:** Neither changed the shipped behavior or public shape described in the plan -- one reverted optional doc polish, the other is the project's own documented `apiDump` step. No scope creep.

## Issues Encountered
- Initial Compose test assertions (`rows[n].assert(hasText(...))` on `outcome_sheet_confirmation_item` nodes) failed because `ProposedItemRow`'s `Row` did not merge its children's text into its own semantics node. Fixed by adding `.semantics(mergeDescendants = true) {}` to the row (mirroring `UndoRowItem`'s existing convention), and querying the nested remove `IconButton`'s own `testTag` via `useUnmergedTree = true` so it stays independently resolvable after the merge. Both committed as part of Task 2 (test-and-production-code fix together, not a separate deviation from the plan's own design -- the plan's illustrative code examples did not specify the semantics-merge detail).

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Phase 12 is complete: `VOUT-04` is satisfied, `Success`/`Failure` are unchanged, and `api.txt` reflects the new additive public surface.
- Phase 13 (ship/register) can proceed: `NeedsConfirmation` needs no `ComponentRegistry` entry of its own (confirmed, no drift-guard edits were needed this phase) and the public API is already additive per `apiCheck`.
- Downstream consumer wiring (SecondBrain's `MutationGate`/`VoiceConfirmGate`, CalTracker's `VoiceResultSheet`) is explicitly deferred to each consumer's own milestone (tracked in STATE.md's Deferred Items) -- not blocked by anything in this plan.
- Gate-1 self-UAT still owes the three Manual-Only Verifications from 12-VALIDATION.md (swipe-dismiss-as-decline, destructive-severity visual color, batch-remove-then-confirm end-to-end) -- out of this executor's lane per policy, routed to `gsd-agentic-tester`.

---
*Phase: 12-generic-needs-confirmation-state*
*Completed: 2026-10-01*

## Self-Check: PASSED

All claimed files exist on disk (`ProposedItemUiModel.kt`, `VoiceOutcomeUiState.kt`, `OutcomeSheet.kt`, `VoiceCommandFamilyScreen.kt`, `OutcomeSheetTest.kt`, `api.txt`, this SUMMARY.md) and both task commits (`7c9b373`, `e11add1`) are present in `git log`.
