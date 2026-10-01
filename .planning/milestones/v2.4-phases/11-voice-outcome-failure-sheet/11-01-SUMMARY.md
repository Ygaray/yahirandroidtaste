---
phase: 11-voice-outcome-failure-sheet
plan: 01
subsystem: ui
tags: [compose, kotlin-coroutines, mutex, undo, voice-command, jetpack-compose]

# Dependency graph
requires:
  - phase: 11-voice-outcome-failure-sheet (Plan 01 — original, superseded)
    provides: OutcomeSheet / VoiceOutcomeUiState (Success/Failure, HandledByUiModel,
      FailureActionUiModel, BatchRowResultUiModel), shipped at commit 6a946d3 (VOUT-01/02/03)
provides:
  - UndoHistoryStore's additive grouping API (openGroup, grouped append overload, group,
    groupIdOf, groupLabel, groupStatus, attemptUndoGroup) -- atomic, Mutex-guarded,
    releasable-on-Refused, group-atomic eviction/clearSpent
  - UndoGroupStatus / UndoGroupResult sealed types + UndoGroupRefusedException (feedback/UndoGroupTypes.kt)
  - UndoAffordanceUiModel / UndoRowUiModel (+ UndoRowState) / UndoRefusedUiModel presentational models
  - OutcomeSheet's new undo-affordance rendering (Undo-all button, per-item rows, loud refused substate)
  - Three new Gate-1 fixtures in VoiceCommandFamilyScreen.kt (happy-path, entangled-Unavailable, refused)
affects: [12-voice-confirm-clarify-sheet (extends this sheet additively), 13-voice-ship-gate (tags v2.4.0)]

# Actuals (#2632) -- pairs with the plan's estimate to calibrate future estimates.
actuals:
  tokens: 12800
  tasks: 2
  commits: 3

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Mutex-guarded releasable claim (kotlinx.coroutines.sync.Mutex) instead of a one-way
      AtomicBoolean CAS, for a group-level atomic operation that must be retryable on refusal"
    - "Group-aware rewrite of clearSpent()/evictIfNeeded() via a shared private helper taking an
      explicit source list, so in-flight `current` reads never race a stale StateFlow snapshot"
    - "Modifier.semantics(mergeDescendants = true) on a conditionally-clickable row, so
      onAllNodesWithTag(...) resolves label+trailing-text regardless of interactive state"

key-files:
  created:
    - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoGroupTypes.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/model/UndoAffordanceUiModel.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/model/UndoRowUiModel.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/model/UndoRefusedUiModel.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoHistoryStore.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/model/VoiceOutcomeUiState.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheet.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/VoiceCommandFamilyScreen.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/UndoHistoryStoreTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetTest.kt

key-decisions:
  - "Group claim guarded ENTIRELY by a store-wide groupUndoMutex, never UndoHistoryEntry.tryConsume() -- the one-way AtomicBoolean CAS has no release path and would permanently strand a claimed-then-Refused member (Pitfall 1, D-01's explicit requirement)."
  - "clearSpent()/evictIfNeeded() rewritten group-aware via one shared private computeGroupStatus(groupId, source) helper that takes the in-flight list explicitly, so both callers evaluate group resolution against their own `current` snapshot instead of a possibly-stale _entries.value read (Pitfall 2)."
  - "UndoRowItem's Row needs an unconditional Modifier.semantics(mergeDescendants = true) -- discovered live: a clickable modifier merges automatically, but Undone/Unavailable rows carried no clickable and stayed unmerged, so the shared testTag couldn't resolve their text. Fixed to mirror ApproachLadderCard's CapControl precedent."

requirements-completed: [VUNDO-01]

coverage:
  - id: D1
    description: "UndoHistoryStore's additive grouping API (openGroup/grouped append/group/groupIdOf/groupLabel/groupStatus/attemptUndoGroup) runs undoAll exactly once and marks every claimed member Undone on success"
    requirement: VUNDO-01
    verification:
      - kind: unit
        ref: "feedback/UndoHistoryStoreTest.kt#groupedAppend_thenAttemptUndoGroup_undoesAllOnceAndResolvesTheGroup"
        status: pass
    human_judgment: false
  - id: D2
    description: "A Refused result leaves every claimed member Available (never permanently stranded); a retried attemptUndoGroup on the same group can still succeed"
    requirement: VUNDO-01
    verification:
      - kind: unit
        ref: "feedback/UndoHistoryStoreTest.kt#attemptUndoGroup_refused_leavesEveryClaimedMemberAvailable_andARetriedCallCanSucceed"
        status: pass
    human_judgment: false
  - id: D3
    description: "Failed/NothingToUndo behave per D-01 (plain exception marks every claimed member Failed; unknown/all-spent groupId returns NothingToUndo without invoking undoAll)"
    requirement: VUNDO-01
    verification:
      - kind: unit
        ref: "feedback/UndoHistoryStoreTest.kt#attemptUndoGroup_undoAllThrowsAPlainException_marksEveryClaimedMemberFailed"
        status: pass
      - kind: unit
        ref: "feedback/UndoHistoryStoreTest.kt#attemptUndoGroup_unknownGroupId_orAllMembersAlreadySpent_returnsNothingToUndo_withoutInvokingUndoAll"
        status: pass
    human_judgment: false
  - id: D4
    description: "clearSpent()/evictIfNeeded() are group-atomic: a PartiallyResolved group's spent members are never dropped ahead of its still-Available member; eviction never strands one member of a group while evicting another"
    requirement: VUNDO-01
    verification:
      - kind: unit
        ref: "feedback/UndoHistoryStoreTest.kt#clearSpent_neverRemovesAPartiallyResolvedGroupsSpentMembers_removesTheWholeGroupOnceFullyResolved"
        status: pass
      - kind: unit
        ref: "feedback/UndoHistoryStoreTest.kt#evictIfNeeded_whenTheEvictionTargetBelongsToAGroup_evictsEveryMemberOfThatGroupTogether"
        status: pass
    human_judgment: false
  - id: D5
    description: "VoiceOutcomeUiState.Success.undo renders Undo-all(N) + per-item Available/Undone/Unavailable rows in exact list order; null undo or an empty/null-onUndoAll model renders no undo section"
    requirement: VUNDO-01
    verification:
      - kind: automated_ui
        ref: "component/OutcomeSheetTest.kt#Success with a non-null undo renders undo-all and every row, and undo-all invokes its callback"
        status: pass
      - kind: automated_ui
        ref: "component/OutcomeSheetTest.kt#an Undone row renders muted trailing text and is never clickable"
        status: pass
      - kind: automated_ui
        ref: "component/OutcomeSheetTest.kt#an Unavailable row renders its reason as non-clickable text while a sibling Available row still fires its own onUndo, and allLabel renders verbatim"
        status: pass
      - kind: automated_ui
        ref: "component/OutcomeSheetTest.kt#a null undo renders no undo_all or undo_row node at all"
        status: pass
      - kind: automated_ui
        ref: "component/OutcomeSheetTest.kt#an undo with empty rows and a null onUndoAll also renders neither undo_all nor undo_row"
        status: pass
      - kind: automated_ui
        ref: "component/OutcomeSheetTest.kt#undo rows render in the exact supplied list order, never resorted"
        status: pass
    human_judgment: false
  - id: D6
    description: "A non-null refused renders loudly on the theme's errorContainer/onErrorContainer roles (never AttentionCue), appending the changed-since suffix only when changedItem is non-null"
    requirement: VUNDO-01
    verification:
      - kind: automated_ui
        ref: "component/OutcomeSheetTest.kt#a non-null refused renders the error-container surface with the reason and appends the changed-since suffix when changedItem is non-null"
        status: pass
      - kind: automated_ui
        ref: "component/OutcomeSheetTest.kt#a non-null refused with a null changedItem renders the reason without the changed-since suffix"
        status: pass
    human_judgment: false
  - id: D7
    description: "Gate-1 self-UAT can drive the entangled-Unavailable and undo-refused branches on-device, not just the happy Undo-all path, per CONTEXT.md's Specific Ideas note"
    requirement: VUNDO-01
    verification: []
    human_judgment: true
    rationale: "On-device visual/interactive verification is owned by the Gate-1 agentic tester, not this executor. This plan only guarantees the fixtures/buttons exist in VoiceCommandFamilyScreen.kt's OutcomeSheetVariants() for the tester to drive."

duration: 30min
completed: 2026-09-30
status: complete
---

# Phase 11 Plan 01: Grouped Undo Store Extension + Outcome Sheet Rendering Summary

**Additive `UndoHistoryStore` grouping API (Mutex-guarded, releasable-on-Refused, group-atomic eviction) plus `OutcomeSheet`'s new "Undo all (N)" / per-item-row / loud-refused rendering, closing VUNDO-01.**

## Performance

- **Duration:** ~30 min
- **Completed:** 2026-09-30
- **Tasks:** 2 (tracer + edge-hardening), plus one Rule-1 fix discovered during self-verification
- **Files modified/created:** 10

## Accomplishments

- `UndoHistoryStore` gained a 7-member additive grouping API (`openGroup`, grouped `append`
  overload, `group`, `groupIdOf`, `groupLabel`, `groupStatus`, `attemptUndoGroup`) with zero ABI
  break to `UndoHistoryEntry`/`UndoStatus` — the existing 3-arg `append`/`attemptUndo`/`entries`
  stay byte-for-byte unchanged.
- `attemptUndoGroup` is atomic and releasable on refusal: a `groupUndoMutex` (never
  `UndoHistoryEntry.tryConsume()`'s one-way CAS) guards the whole claim→run→resolve section, so a
  `Refused` result leaves every claimed member `Available` and a retried call can still succeed.
- `clearSpent()`/`evictIfNeeded()` rewritten group-aware without any public signature change — a
  partially-resolved group's spent members are never dropped ahead of its still-Available members,
  and eviction never strands one group member while evicting another.
- Three new presentational types (`UndoAffordanceUiModel`, `UndoRowUiModel`/`UndoRowState`,
  `UndoRefusedUiModel`) built by the consumer from store reads, never inside the library (INV-01).
- `OutcomeSheet`'s `Success.undo` renders "Undo all (N)", per-item Available/Undone/Unavailable
  rows in list order, and a loud `errorContainer`-styled refused substate — never `AttentionCue`.
- Three new Gate-1 gallery fixtures (happy path, entangled-Unavailable, undo-refused) so the
  Gate-1 tester can drive both loud states on-device, per `11-CONTEXT.md`'s "Specific Ideas" note.

## Task Commits

Each task was committed atomically:

1. **Task 1 (tracer): grouped undo "open → append → render → undo" one path** - `25ecc41` (feat)
2. **Task 2: VUNDO-01 edge hardening — Refused/Failed/eviction/clearSpent + Unavailable/Refused rendering** - `55f5c43` (test)
3. **Rule 1 fix: merge undo row semantics so Undone/Unavailable rows resolve text via their shared tag** - `f715372` (fix)

**Plan metadata:** commit pending (this SUMMARY.md + REQUIREMENTS.md, orchestrator-excluded STATE.md/ROADMAP.md per worktree mode)

_Note: Task 1 is `type="tracer"` — its own tracer feedback gate (re-run `<verify>` under `HUMAN_VERIFY_MODE=end-of-phase`, automated-only) passed on the first run, so execution continued straight to Task 2 with no checkpoint synthesized._

## Files Created/Modified

- `feedback/UndoGroupTypes.kt` - `UndoGroupStatus`/`UndoGroupResult` sealed types + `UndoGroupRefusedException`
- `feedback/UndoHistoryStore.kt` - additive grouping API, group-aware `clearSpent`/`evictIfNeeded`
- `model/UndoAffordanceUiModel.kt` - grouped undo affordance projection (`allLabel`, `rows`, `onUndoAll`, `refused`)
- `model/UndoRowUiModel.kt` - per-row model + `UndoRowState` (Available/Undone/Unavailable)
- `model/UndoRefusedUiModel.kt` - loud undo-refused/partial substate model
- `model/VoiceOutcomeUiState.kt` - `Success` gains one new optional `undo` field (D-02)
- `component/OutcomeSheet.kt` - `SuccessBody` renders `undo`; new `UndoAffordanceBody`/`UndoRowItem`
- `explorer/VoiceCommandFamilyScreen.kt` - three new `OutcomeSheetVariants` fixtures/buttons
- `src/test/.../feedback/UndoHistoryStoreTest.kt` - 9 new tests (grouping happy path + 5 edge cases)
- `src/test/.../component/OutcomeSheetTest.kt` - 7 new tests (undo rendering + edge rendering)

## Decisions Made

- Group claim guarded entirely by a store-wide `groupUndoMutex`, never `tryConsume()` — the sole
  mechanism satisfying D-01's "releasable claim, not the one-way `consumedGuard`" requirement
  (Pitfall 1).
- `clearSpent()`/`evictIfNeeded()` share one private `computeGroupStatus(groupId, source)` helper
  taking the in-flight list explicitly, so neither reads a possibly-stale `_entries.value` snapshot
  mid-`update` (Pitfall 2).
- `UndoRowItem`'s `Row` needed an unconditional `Modifier.semantics(mergeDescendants = true)`,
  mirroring `ApproachLadderCard`'s `CapControl` precedent — discovered live when the Undone-row
  rendering test failed because a non-clickable row wasn't a semantics merge boundary.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] `UndoRowItem`'s Row wasn't a semantics merge boundary for non-clickable states**
- **Found during:** Self-verification after Task 2 (writing an additional Undone-row test beyond the plan's explicit task actions, to fully cover the plan's stated must_haves truth that all three row states — Available/Undone/Unavailable — render correctly)
- **Issue:** `Modifier.clickable` merges descendant semantics automatically, so the Available-state row's label + click action resolved fine via the shared `outcome_sheet_undo_row` tag. Undone/Unavailable rows carry no clickable modifier and therefore weren't merge boundaries at all — `onAllNodesWithTag(...)`'s default merged-tree lookup couldn't see their label or trailing text through the shared tag, and the new test failed with an `AssertionError`.
- **Fix:** Added `Modifier.semantics(mergeDescendants = true) {}` unconditionally to the row (before the conditional clickable), mirroring `ApproachLadderCard`'s established `CapControl` convention for exactly this situation.
- **Files modified:** `component/OutcomeSheet.kt`
- **Verification:** New `an Undone row renders muted trailing text and is never clickable` test passes; full suite + detekt + apiCheck re-verified green after the fix.
- **Committed in:** `f715372`

---

**Total deviations:** 1 auto-fixed (1 bug, Rule 1).
**Impact on plan:** Necessary correctness fix for the accessibility/testability contract the plan's own analog (`ApproachLadderCard`) already establishes. No scope creep — found and fixed within the same rendering surface Task 2 already touched, caught by an extra test written specifically to close a gap against the plan's own stated `must_haves` truths.

## Issues Encountered

- The hub's `tools/hooks/pre-commit` classifier flagged Task 1's commit as **lane 2** (API
  append-only, but pre-existing source lines in `clearSpent()`/`evictIfNeeded()`/`SuccessBody`
  were rewritten, not purely appended) — exactly as the plan's own action text requires
  ("Rewrite `clearSpent()`'s body...", "Rewrite `evictIfNeeded`'s body the same way"). This is the
  hub's own governance mechanism for exactly this kind of deliberate, plan-mandated internal
  rewrite (frozen and SB-confirmed via D-01's R1-seam reconvene) — used its sanctioned
  `HUB_LANE_OVERRIDE=2` declaration (printed by the hook itself as the way to "land it
  deliberately"), not a hook bypass. Task 2's and the fix commit's changes were pure additions and
  landed on the hook's default lane-1 fast path with no override needed.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- VUNDO-01 is fully closed: the additive `UndoHistoryStore` grouping API behaves exactly per
  D-01's frozen contract, and `OutcomeSheet`'s `Success.undo` renders the full Undo-all/per-item-
  row/refused-substate contract. Full `testDebugUnitTest` suite (all 196 Kotlin files), `detekt`
  (0 code smells, zero-baseline held), and `apiCheck` (no incompatible change vs `v2.3.0`) all
  green as of the final commit.
- Phase 11's only remaining requirement is VCLAR-01 (Plan 02, the clarification-choices
  composable) — not touched by this plan.
- Gate-1 self-UAT still owes explicit on-device verification of the entangled-Unavailable and
  undo-refused branches (fixtures/buttons are in place in `VoiceCommandFamilyScreen.kt`'s
  `OutcomeSheetVariants()` for exactly this) — out of this executor's lane per the behavioral-
  verification boundary; routes to the Gate-1 agentic tester.

---
*Phase: 11-voice-outcome-failure-sheet*
*Completed: 2026-09-30*

## Self-Check: PASSED

- Created files verified on disk: `feedback/UndoGroupTypes.kt`, `model/UndoAffordanceUiModel.kt`,
  `model/UndoRowUiModel.kt`, `model/UndoRefusedUiModel.kt` — all FOUND.
- All three task commits verified present in `git log --oneline --all`: `25ecc41`, `55f5c43`, `f715372`.
- Plan-level `<verification>` re-run at HEAD: `./gradlew testDebugUnitTest` (full suite, 69
  result files, 0 failures/0 errors), `./gradlew detekt` (0 code smells, zero-baseline held),
  `./gradlew apiCheck` (BUILD SUCCESSFUL, no incompatible change vs `v2.3.0`) — all green.
- No unexpected file deletions in any of the three commits (`git diff --diff-filter=D` empty
  for each).
