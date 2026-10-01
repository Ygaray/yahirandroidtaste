---
phase: 11-voice-outcome-failure-sheet
plan: 02
subsystem: ui
tags: [compose, kotlin, jetpack-compose, voice-command, clarification, chip]

# Dependency graph
requires:
  - phase: 11-voice-outcome-failure-sheet (Plan 01)
    provides: OutcomeSheet's grouped undo affordance + VoiceCommandFamilyScreen.kt /
      DomainVocabularyDriftGuardTest.kt at their post-Plan-01 state (shared-file sequencing,
      no group-undo-related conflicts to merge)
provides:
  - ClarificationOptionUiModel (opaque id + label presentational model)
  - ClarificationBar: standalone, prop-driven "tap-to-clarify choices" composable (question +
    pressable AppChip options + onSelect(id)/onDismiss), registered in the Voice Command family
    with a 4-cell states matrix (Disabled/Focused documented N/A) and a duplicate-label-edge
    gallery fixture
  - DomainVocabularyDriftGuardTest's PRIMITIVE_NOUN_ALLOWLIST widened with "Clarification"
affects: [12-voice-confirm-clarify-sheet (may reuse this composable), 13-voice-ship-gate (tags v2.4.0)]

# Actuals (#2632) -- pairs with the plan's estimate to calibrate future estimates.
actuals:
  tokens: 5200
  tasks: 2
  commits: 2
  plan_head_before: e4dbde67283a141557bf74cc206d497beeb39d6f

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Standalone top-level composable (never nested in a sibling sheet's sealed when) for a
      swappable visual decision (D-07) -- ClarificationBar sits beside OutcomeSheet, not inside it"
    - "Opaque-id passthrough: the library never interprets/normalizes/case-folds an id crossing
      its boundary -- verified via a mixed-case/punctuation id round-tripping byte-for-byte"
    - "Behavioral (tap-to-id) order verification instead of hasText-on-shared-tag, because
      AppChip's own internal clickable Surface is its own semantics merge boundary that a
      wrapping testTag node cannot absorb -- performClick() dispatches a real on-screen touch at
      the resolved node's bounds regardless of merge boundaries, making indexed tap verification
      an equally strong (and purely behavioral) proof of render order"

key-files:
  created:
    - src/main/java/io/github/ygaray/yahirandroidtaste/model/ClarificationOptionUiModel.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/ClarificationBar.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/ClarificationBarTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/VoiceCommandFamilyScreen.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt

key-decisions:
  - "ClarificationBar built as its own top-level composable, never nested inside OutcomeSheet's
    sealed when, per D-07's explicit chips-vs-buttons/bar-vs-sheet-state swappability grant --
    folding it into the outcome sheet would couple an unrelated visual decision to the sheet's
    own chrome."
  - "Order verification for the three-option non-alphabetical edge test uses indexed
    performClick() -> captured onSelect id, not hasText() against the shared outer testTag.
    AppChip's internal clickable Surface declares its own semantics merge boundary
    (mergeDescendants=true); Compose does not let an enclosing merge boundary absorb an
    already-merging descendant's subtree, so the label text never becomes part of the outer
    tag's own merged config. performClick() instead simulates a real touch at the resolved
    node's on-screen bounds, which reaches the correct chip regardless -- tap-to-id is therefore
    an equally strong, purely behavioral proof that options render -- and are wired -- in exact
    list order. Label presence is independently confirmed via onNodeWithText (unaffected by the
    merge-boundary interaction since each Text's own SemanticsNode carries its Text property
    directly)."
  - "The STRIDE Tampering consumer-responsibility note (id-uniqueness is the consumer's
    documented responsibility) was written into ClarificationOptionUiModel.kt's KDoc during
    Task 1 (anticipating Task 2's explicit requirement) rather than as a separate Task 2 edit --
    no further change to that file was needed in Task 2."

requirements-completed: [VCLAR-01]

coverage:
  - id: D1
    description: "ClarificationBar renders a question + one pressable AppChip per option, in
      exact list order, and tapping an option invokes onSelect with exactly that option's id"
    requirement: VCLAR-01
    verification:
      - kind: unit
        ref: "component/ClarificationBarTest.kt#renders the question text and one AppChip per option"
        status: pass
      - kind: unit
        ref: "component/ClarificationBarTest.kt#tapping the Nth chip invokes onSelect with exactly that option's id"
        status: pass
    human_judgment: false
  - id: D2
    description: "Tapping the dismiss control invokes onDismiss and never onSelect; no timeout,
      default-selected option, or programmatic auto-invoke exists (T-11-07, the VCLAR-01 prohibition)"
    requirement: VCLAR-01
    verification:
      - kind: unit
        ref: "component/ClarificationBarTest.kt#tapping dismiss invokes onDismiss and never onSelect"
        status: pass
    human_judgment: false
  - id: D3
    description: "Two options sharing the same id or label both render as separate,
      non-deduplicated chips; tapping the second fires the second's own id, never the first's (T-11-05)"
    requirement: VCLAR-01
    verification:
      - kind: unit
        ref: "component/ClarificationBarTest.kt#two options sharing the same id or label both render as separate chips, and tapping the second fires its own id"
        status: pass
    human_judgment: false
  - id: D4
    description: "An empty options list hides the whole bar (no surface, no option node); a
      single-option list renders and behaves like any N-option case"
    requirement: VCLAR-01
    verification:
      - kind: unit
        ref: "component/ClarificationBarTest.kt#empty options renders no surface or option node at all"
        status: pass
      - kind: unit
        ref: "component/ClarificationBarTest.kt#a single-option list renders exactly one pressable chip that behaves like any N-option case"
        status: pass
    human_judgment: false
  - id: D5
    description: "An id containing mixed case/punctuation passes through onSelect byte-for-byte
      unmodified -- the library never normalizes, case-folds, or reinterprets the opaque id"
    requirement: VCLAR-01
    verification:
      - kind: unit
        ref: "component/ClarificationBarTest.kt#an id containing mixed case or punctuation passes through onSelect byte-for-byte unmodified"
        status: pass
    human_judgment: false
  - id: D6
    description: "Three options in a deliberately non-alphabetical order render -- and are wired
      to onSelect -- in that exact order, never re-sorted"
    requirement: VCLAR-01
    verification:
      - kind: unit
        ref: "component/ClarificationBarTest.kt#three options in non-alphabetical order render in that exact order, never re-sorted"
        status: pass
    human_judgment: false
  - id: D7
    description: "ClarificationBar is registered in the Voice Command family with a 4-cell states
      matrix (Default/duplicate-label edge, Disabled/Focused documented N/A) and DOMAIN
      vocabulary + ComponentRegistry drift guards are both green across the full suite"
    requirement: VCLAR-01
    verification:
      - kind: unit
        ref: "explorer/DomainVocabularyDriftGuardTest.kt (full suite run)"
        status: pass
      - kind: unit
        ref: "explorer/ComponentRegistryDriftGuardTest.kt (full suite run)"
        status: pass
    human_judgment: false
  - id: D8
    description: "Gate-1 self-UAT can drive the tap-to-clarify flow on-device in the gallery,
      including the duplicate-label-edge fixture, not just unit-level verification"
    requirement: VCLAR-01
    verification: []
    human_judgment: true
    rationale: "On-device visual/interactive verification is owned by the Gate-1 agentic tester,
      not this executor. This plan only guarantees the fixtures/buttons exist in
      VoiceCommandFamilyScreen.kt's ClarificationBarVariants()/states matrix for the tester to
      drive."

duration: 35min
completed: 2026-10-01
status: complete
---

# Phase 11 Plan 02: Clarification Choices Composable Summary

**`ClarificationBar` -- a standalone, prop-driven tap-to-clarify choices surface (question + opaque-id/label options + onSelect/onDismiss), registered in the Voice Command family, closing VCLAR-01 and Phase 11.**

## Performance

- **Duration:** ~35 min
- **Completed:** 2026-10-01
- **Tasks:** 2 (base build + edge hardening/phase-closing verification)
- **Files modified/created:** 5

## Accomplishments

- `ClarificationOptionUiModel` (`{id: String, label: String}`) -- opaque, all-`val` presentational
  model mirroring `ApproachRungUiModel`'s convention; KDoc documents `id` as never interpreted/
  normalized/case-folded, and that uniqueness within one `options` list is the CONSUMER's
  documented responsibility (T-11-05).
- `ClarificationBar` -- a standalone top-level composable (never nested inside `OutcomeSheet`'s
  sealed `when`, per D-07): renders a `question` plus `options` as pressable `AppChip`s in exact
  list order on a visually-informative (non-error) `cardShapeLarge`/`surfaceContainer` surface,
  with a trailing dismiss control. Empty `options` hides the whole bar. `onSelect`/`onDismiss`
  fire ONLY from their own `onClick` lambda -- no timeout, no default-selected option, no
  programmatic auto-invoke anywhere in the file (T-11-07).
- Registered in `ComponentRegistry`'s Voice Command family with a 4-cell states matrix (Default =
  2-option fixture; Pressed/Selected = duplicate-label-edge fixture; Disabled/Focused = documented
  "Not applicable", mirroring `ProviderKeyCard`'s precedent for controls with no disabled param).
- `DomainVocabularyDriftGuardTest`'s `PRIMITIVE_NOUN_ALLOWLIST` widened with `"Clarification"`,
  mirroring the already-landed `"Outcome"` entry's comment style verbatim.
- `ClarificationBarTest` (8 tests): base render/tap/dismiss behavior (Task 1) plus the four
  specless-probe edges -- adjacency (duplicate id/label), empty/single-option, encoding
  (mixed-case/punctuation id round-trips byte-for-byte), and ordering (Task 2).
- Full Phase 11 wave-merge gate (both plans) green: `testDebugUnitTest` (633 tests, 0
  failures/errors), `detekt` (0 code smells, zero-baseline held), `apiCheck` (no incompatible
  change vs `v2.3.0`).

## Task Commits

Each task was committed atomically:

1. **Task 1: ClarificationBar -- pressable choice surface (VCLAR-01, D-07)** - `c9ee4b9` (feat)
2. **Task 2: VCLAR-01 edge hardening + phase-closing full verification (both plans)** - `c4dac6d` (test)

**Plan metadata:** commit pending (this SUMMARY.md + REQUIREMENTS.md, orchestrator-excluded STATE.md/ROADMAP.md per worktree mode)

## Files Created/Modified

- `model/ClarificationOptionUiModel.kt` - opaque `{id, label}` presentational model + STRIDE
  consumer-responsibility KDoc
- `component/ClarificationBar.kt` - the standalone tap-to-clarify composable
- `explorer/VoiceCommandFamilyScreen.kt` - new `ClarificationBar` registry entry, fixtures
  (happy-path + duplicate-label-edge), and `ClarificationBarVariants()` gallery section
- `src/test/.../explorer/DomainVocabularyDriftGuardTest.kt` - `PRIMITIVE_NOUN_ALLOWLIST` widened
  with `"Clarification"`
- `src/test/.../component/ClarificationBarTest.kt` - 8 tests (base render/tap/dismiss trio +
  4 edge-hardening cases)

## Decisions Made

- `ClarificationBar` built as its own top-level composable, never folded into `OutcomeSheet`'s
  sealed `when` -- D-07's explicit swappability grant means this visual decision stays independent
  of the sheet's own chrome.
- Order verification for the three-option non-alphabetical edge test uses indexed
  `performClick()` -> captured `onSelect` id, not `hasText()` against the shared outer `testTag`
  (see Deviations below for why).
- The STRIDE Tampering consumer-responsibility KDoc note was written during Task 1 (anticipating
  Task 2's explicit requirement), so Task 2 needed no further edit to `ClarificationOptionUiModel.kt`.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] `hasText()` against `AppChip`'s shared `testTag` can't see the label text through its own internal merge boundary**
- **Found during:** Task 2 (writing the three-option non-alphabetical ordering edge test, per the
  plan's explicit "indexed `onAllNodesWithTag` access" wording)
- **Issue:** The plan's literal `ClarificationBar` action text places `Modifier.testTag(...)`
  directly on `AppChip`'s `modifier` parameter, which lands on `AppChip`'s OUTER `Box` (per
  `AppChip.kt`'s own KDoc: "Modifier applied to the outer Box"). `AppChip`'s inner `Surface`
  independently declares its own semantics merge boundary via `Modifier.clickable` (which sets
  `mergeDescendants = true`). Compose's semantics-merging rule does not let an enclosing
  `mergeDescendants = true` node absorb an ALREADY-merging descendant's subtree -- nested merge
  boundaries stay independent. So the outer, `testTag`-bearing `Box` node's own merged
  `SemanticsConfig` never includes the inner `Surface`'s `Text` property, and `hasText("Zebra")`
  asserted against the tag-resolved node failed with `AssertionError` even though the text was
  genuinely rendered on screen (confirmed via `onNodeWithText` succeeding independently).
  `performClick()` on the SAME tag-resolved node succeeded in every other test because
  `performClick()` dispatches a REAL synthetic touch at the resolved node's on-screen bounds
  (not a semantics-action invocation) -- the touch reaches whatever pointer-input consumer
  occupies that screen location (the inner `Surface`) regardless of the merge-boundary mismatch.
- **Fix:** First attempted wrapping each `AppChip` in an additional `Box.semantics(mergeDescendants
  = true) + testTag(...)` (mirroring Plan 01's `UndoRowItem`/`ApproachLadderCard`'s `CapControl`
  precedent) -- this did NOT resolve the issue, since the nested-merge-boundary rule means an
  outer merge wrapper still cannot absorb `AppChip`'s own pre-existing merge boundary (confirmed by
  re-running the test: identical failure, now showing `MergeDescendants = 'true'` on the wrapper
  but still "Has 1 child"). Reverted that wrapper to honor the plan's literal `AppChip(...,
  modifier = Modifier.testTag(...))` structure exactly, and instead rewrote the ordering test to
  verify order via indexed `performClick()` -> captured `onSelect` id (an equally strong, purely
  behavioral proof that composition order matches list order, since each `AppChip`'s `onClick`
  lambda closes over its own list-ordered `option.id`), plus an independent `onNodeWithText(...)`
  existence check per label (unaffected by the merge-boundary interaction).
- **Files modified:** `src/test/.../component/ClarificationBarTest.kt` (production
  `ClarificationBar.kt` ended up byte-identical to its Task 1 commit -- the wrapper attempt was
  added then reverted within Task 2, confirmed via `git diff` showing zero change before the
  Task 2 commit).
- **Verification:** `three options in non-alphabetical order render in that exact order, never
  re-sorted` passes; full suite + detekt + apiCheck re-verified green after the fix.
- **Committed in:** `c4dac6d`

**2. [Rule 1 - Bug] `setContent` called twice within one test (`IllegalStateException`)**
- **Found during:** Task 2, the same edge-hardening pass -- the originally-drafted combined
  "empty options + single-option" test called `composeTestRule.setContent { }` a second time
  within the same test body to switch fixtures.
- **Issue:** Compose's `ComposeContentTestRule.setContent` can only be called once per test
  (per-test `ComponentActivity` already has content set); the second call threw
  `IllegalStateException: ...has already set content`.
- **Fix:** Split into two independent `@Test` functions -- `empty options renders no surface or
  option node at all` and `a single-option list renders exactly one pressable chip that behaves
  like any N-option case` -- each with its own `setContent` call, matching every other test in
  the file.
- **Files modified:** `src/test/.../component/ClarificationBarTest.kt`
- **Verification:** Both split tests pass independently; full suite green.
- **Committed in:** `c4dac6d`

---

**Total deviations:** 2 auto-fixed (2 bugs, both Rule 1, both confined to the test file).
**Impact on plan:** Both fixes are test-only corrections to make the plan's own stated edge-case
truths (ordering, empty/single-option behavior) provable within this Robolectric+Compose harness's
actual semantics-merging constraints. No production-code behavior changed from what Task 1 already
shipped (confirmed `ClarificationBar.kt` is byte-identical to its Task 1 commit). No scope creep.

## Issues Encountered

None beyond the two auto-fixed test-authoring issues documented above.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- VCLAR-01 is fully closed: `ClarificationBar` renders a question + pressable options from props,
  resolves via tap with the exact opaque id, never auto-resolves, never de-duplicates, never
  reorders. It clears both drift guards (`DomainVocabularyDriftGuardTest`,
  `ComponentRegistryDriftGuardTest`), and the complete Phase 11 suite (both Plan 01 and Plan 02) is
  green end-to-end: `testDebugUnitTest` (633 tests, 0 failures), `detekt` (0 code smells,
  zero-baseline held), `apiCheck` (no incompatible change vs `v2.3.0`).
- Phase 11's scope is now fully implemented: VOUT-01/02/03 (already shipped before this phase's
  plans), VUNDO-01 (Plan 01), and VCLAR-01 (this plan).
- Gate-1 self-UAT still owes explicit on-device verification of the tap-to-clarify flow, including
  the duplicate-label-edge fixture, on top of Plan 01's grouped-undo/Failure branches -- out of
  this executor's lane per the behavioral-verification boundary; routes to the Gate-1 agentic
  tester.
- No blockers for Phase 12 (voice confirm/clarify sheet) or Phase 13 (ship gate, tags `v2.4.0`).

---
*Phase: 11-voice-outcome-failure-sheet*
*Completed: 2026-10-01*

## Self-Check: PASSED

- Created files verified on disk: `model/ClarificationOptionUiModel.kt`,
  `component/ClarificationBar.kt`, `src/test/.../component/ClarificationBarTest.kt` -- all FOUND.
- Both task commits verified present in `git log --oneline`: `c9ee4b9`, `c4dac6d`.
- Plan-level `<verification>` re-run at HEAD: `./gradlew testDebugUnitTest` (full suite, 633
  tests, 0 failures/0 errors), `./gradlew detekt` (0 code smells, zero-baseline held),
  `./gradlew apiCheck` (BUILD SUCCESSFUL, no incompatible change vs `v2.3.0`) -- all green.
- No unexpected file deletions in either task commit (`git diff --diff-filter=D` empty for both).
- `commits:` is MEASURED: `git rev-list --count e4dbde67283a141557bf74cc206d497beeb39d6f..HEAD`
  = 2 (matches the two task commits above; this metadata commit will bring the total to 3).
