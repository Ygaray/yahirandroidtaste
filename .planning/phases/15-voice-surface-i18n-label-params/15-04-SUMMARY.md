---
phase: 15-voice-surface-i18n-label-params
plan: 04
subsystem: ui
tags: [compose, material3, flowrow, layout, i18n, robolectric, gap-closure]

requires:
  - phase: 15-voice-surface-i18n-label-params
    provides: caller-overridable ApproachLadderCard rung-state and toggle labels (15-01)
provides:
  - RungRow long-label layout: rung name keeps >= 40% of the row; affordances in a capped, right-aligned FlowRow, max 2 lines each
  - SegmentedOptionSelector long-label layout: labels wrap (max 2 lines) inside their segment, equal segment heights
  - German long-label regression tests (ApproachLadderCardLongLabelTest, SegmentedOptionSelectorTest)
  - Explorer long German label Gate-2 review cell
  - Corrected 15-VERIFICATION.md SC5 trailing-lambda claim; API.md v2.5.1 long-label notes
affects: [v2.5.1 tag, SecondBrain repin, CalTracker repin, milestone Gate-2]

actuals:
  tokens: 9700
  tasks: 3
  commits: 5
plan_head_before: ce3516c0989973db806acec7a453a411c6b1a430

tech-stack:
  added: []
  patterns:
    - "Private Modifier.layout width caps (maxWidthFraction, reserveSegmentIconSlot) to bound unweighted Row children without touching public API"
    - "Robolectric layout regression harness: qualifiers w360dp-h800dp + GraphicsMode.NATIVE + unclipped positionInRoot/size geometry + GetTextLayoutResult line counts"

key-files:
  created:
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCardLongLabelTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/SegmentedOptionSelectorTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/SegmentedOptionSelector.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/VoiceCommandFamilyScreen.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ApproachLadderCardGalleryDemoTest.kt
    - API.md
    - .planning/phases/15-voice-surface-i18n-label-params/15-VERIFICATION.md

key-decisions:
  - "Equal segment heights use the IntrinsicSize approach (row height(IntrinsicSize.Min) + segment fillMaxHeight()); the onSizeChanged fallback was not needed"
  - "Tracer gate continued without a mid-flight checkpoint: the tracer's only human-check is explicitly 'Deferred to milestone Gate-2' and human_verify_mode is end-of-phase"

patterns-established:
  - "Long-label regression tests read unclipped geometry (positionInRoot + size), never boundsInRoot"

requirements-completed: [VI18N-03, VI18N-04]

coverage:
  - id: D1
    description: "With German labels at 360dp the Cloud rung name stays displayed (nonzero, unellipsized width); affordances render on <= 2 lines with nonzero width and never overlap the name; English defaults stay single-line"
    requirement: VI18N-03
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCardLongLabelTest.kt"
        status: pass
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCardTest.kt (33 tests, unchanged file)"
        status: pass
    human_judgment: false
  - id: D2
    description: "SegmentedOptionSelector labels stay inside their segment (selected or not) on <= 2 lines; equal segment heights; short labels and a11y descriptions unchanged"
    requirement: VI18N-03
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/SegmentedOptionSelectorTest.kt"
        status: pass
      - kind: unit
        ref: "ApproachLadderCardLongLabelTest#German offline-only toggle label stays inside its selected segment"
        status: pass
    human_judgment: false
  - id: D3
    description: "Explorer long German label Variants cell reachable through ComponentRegistry"
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ApproachLadderCardGalleryDemoTest.kt#variantsContent_includesLongGermanLabelCell_withBoundedAffordanceWrapping"
        status: pass
    human_judgment: true
    rationale: "Visual legibility of the German cell in light and dark is a Gate-2 human judgment (deferred to milestone Gate-2, 'P15: LONG German labels')"
  - id: D4
    description: "No public API change: api.txt byte-identical to v2.5.0; detekt zero-baseline green; drift guards green"
    requirement: VI18N-04
    verification:
      - kind: other
        ref: "git diff --quiet v2.5.0 -- api.txt (exit 0); ./gradlew testDebugUnitTest detekt apiCheck (BUILD SUCCESSFUL)"
        status: pass
    human_judgment: false
  - id: D5
    description: "15-VERIFICATION.md SC5 names the ProposedItemUiModel trailing-lambda exception; API.md documents v2.5.1 long-label layout"
    requirement: VI18N-04
    verification:
      - kind: other
        ref: "grep acceptance checks (ProposedItemUiModel(id, title)=1, 18-UAT test 3=1, 'Long labels (v2.5.1'=1, v2.5.1=2)"
        status: pass
    human_judgment: false

duration: 11min
completed: 2026-10-12
status: complete
---

# Phase 15 Plan 04: Long-label layout fix (G-15-1) Summary

**Rung names stay visible with long localized labels: the affordances sit in a FlowRow capped at 60% of the row, and segmented labels get a reserved icon slot so they wrap inside their segment. Both changes are private, and api.txt is byte-identical to v2.5.0.**

## Superseded layout (post-review, 2026-10-11)

> The fixed 60% `maxWidthFraction` FlowRow cap described below was **replaced** by the private
> `RungLabelLayout` in `c877c9d` (code-review CR-01: the cap stacked short English labels, breaking
> the v2.5.0-identical truth). The segmented icon-slot reservation was also reduced to ~2dp
> (`b46e62a`, WR-03). See `15-REVIEW.md` / `15-REVIEW-FIX.md` and the re-verified
> `15-VERIFICATION.md` for the final layout. The body below is the historical 15-04 record.

## Performance

- **Duration:** about 11 min
- **Started:** 2026-10-12T00:35:41Z
- **Completed:** 2026-10-12T00:46:05Z
- **Tasks:** 3
- **Files modified:** 8 (2 created, 6 modified)

## Accomplishments

- **RungRow (G-15-1 root cause).** The three affordance Texts now share one `FlowRow`. A private `Modifier.maxWidthFraction(AFFORDANCE_MAX_WIDTH_FRACTION = 0.6f)` caps that FlowRow. Row measures unweighted children first, so the cap leaves the `weight(1f)` rung name at least 40% of the row.
  - Each affordance: `maxLines = 2`, `Ellipsis`, `TextAlign.End`.
  - The FlowRow is rendered only when some affordance applies, so affordance-free rows get no dead space.
  - Unchanged: `CapControl`, merged semantics and child order.
- **SegmentedOptionSelector.** A private `Modifier.reserveSegmentIconSlot()` shrinks the label's max width by `IconSize + IconSpacing`. This compensates for M3 1.4.0 measuring the label at full width and then placing it after the icon slot.
  - Label: `maxLines = 2`, `Ellipsis`, `TextAlign.Center`.
  - Equal heights come from `height(IntrinsicSize.Min)` on the row plus `fillMaxHeight()` on each segment.
- **Regression tests** use the German Gate-2 label set at w360dp with `GraphicsMode.NATIVE`. Each was proven RED before its fix.
- **Gallery.** A new Explorer cell, "ApproachLadderCard — long localized labels (German)", is reached through the registry-driven test. It passes no router, so the router-count pin stays at 2.
- **Docs.**
  - 15-VERIFICATION.md SC5 now names the `ProposedItemUiModel(id, title) { … }` exception and carries a dated correction marker.
  - API.md has a v2.5.1 long-label bullet in section 10 and a clause on the SegmentedOptionSelector row in section 5.

## RED evidence (pre-fix, unmodified source)

**ApproachLadderCardLongLabelTest** (Task 1, against unmodified `ApproachLadderCard.kt`, commit 39985c7):
- Test 1 FAILED: `Assert failed: The component with Text + InputText + EditableText contains 'Cloud' (ignoreCase: false) is not displayed!`
- Test 2 FAILED: `"Netzwerkverbindung erforderlich" #0 must render on <= 2 lines, rendered on 21`. This is the one-character-per-line wrap.
- Test 3 (no overlap) PASSED before the fix. See Deviations.
- Test 4 (English guard) PASSED, as expected.

**SegmentedOptionSelectorTest** plus the card-level toggle test (Task 2, against unmodified `SegmentedOptionSelector.kt`, commit 5534165):
- Test 1 FAILED: `"Ausschließlich offline" right edge 345.0 must be <= its segment's right edge 344.0`
- The card-level test `German offline-only toggle label stays inside its selected segment` FAILED with the same message (345.0 > 344.0).
- Test 2 (equal heights) and Test 3 (short labels) PASSED before the fix. They are guards.

**GREEN.** After each fix, all of these passed: ApproachLadderCardLongLabelTest (5/5), SegmentedOptionSelectorTest (3/3), ApproachLadderCardTest (33/33, file unedited), ApproachLadderCardGalleryDemoTest (4/4, router-count assertion still 2). The full `./gradlew testDebugUnitTest detekt apiCheck` run was BUILD SUCCESSFUL: 760 tests, 0 failures, 22 skipped (all already skipped before this plan).

**Mutation check (equal heights).** With both `height(IntrinsicSize.Min)` and `fillMaxHeight()` removed, `segments keep equal height when only one label wraps` FAILS. Removing only the row's `height(IntrinsicSize.Min)` still passes, so M3's row already bounds the height enough for `fillMaxHeight()`. Both modifiers stay, so the guarantee does not rest on M3 internals. The file was restored to the committed state afterwards.

## Task Commits

1. **Task 1 (tracer): RungRow long-label fix + gallery cell.** `39985c7` (test, RED), `f511b4e` (fix, GREEN, `HUB_LANE_OVERRIDE=2`)
2. **Task 2: SegmentedOptionSelector wrap inside segment.** `5534165` (test, RED), `d655bd2` (fix, GREEN, `HUB_LANE_OVERRIDE=2`)
3. **Task 3: Docs.** `d693928` (docs, lane 1)

## Files Created/Modified

- `src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt`: RungRow FlowRow, `AFFORDANCE_MAX_WIDTH_FRACTION`, private `maxWidthFraction`, one KDoc sentence.
- `src/main/java/io/github/ygaray/yahirandroidtaste/component/SegmentedOptionSelector.kt`: private `reserveSegmentIconSlot`, 2-line centered label, equal-height modifiers, updated `@param options`.
- `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/VoiceCommandFamilyScreen.kt`: German label consts (explorer-only) and the long-label Variants cell.
- `src/test/.../component/ApproachLadderCardLongLabelTest.kt`: new, 5 tests.
- `src/test/.../component/SegmentedOptionSelectorTest.kt`: new, 3 tests.
- `src/test/.../explorer/ApproachLadderCardGalleryDemoTest.kt`: comment update and the new German-cell test.
- `API.md`: v2.5.1 long-label notes in sections 5 and 10.
- `.planning/phases/15-voice-surface-i18n-label-params/15-VERIFICATION.md`: corrected SC5 bullet.

## Decisions Made

- **Equal heights use IntrinsicSize, not the onSizeChanged fallback.** No intrinsic-measurement exception occurred, and Test 2 passes.
- **Tracer feedback gate: continued without a checkpoint.** The tracer `<verify>` has a `<human-check>`, but it is explicitly "Deferred to milestone Gate-2", and `human_verify_mode` is `end-of-phase`. The literal precedence table (row 4) would STOP on any human-check. I treated a check that is explicitly deferred to end-of-milestone as not a mid-flight check, re-ran the full automated gate (green) and expanded. The visual check is still owed at milestone Gate-2.

## Deviations from Plan

**1. [RED expectation] Test 3 (no overlap) was not RED before the fix**
- **Found during:** Task 1, Step A
- **Issue:** The plan expected Tests 1-3 to fail on unmodified code. Before the fix, the name collapses to 0 width at the row start, so its right edge equals its left edge and sits left of every affordance. "No overlap" therefore holds trivially.
- **Resolution:** The harness clearly reproduces the bug: Test 1 shows the name not displayed, and Test 2 shows 21 lines. Test 3 stays as a guard that the new FlowRow layout never overlaps the name. Its assertion was not weakened.
- **Commit:** 39985c7

**2. [Rule 2 - Test strength] Equal-height test asserts that its precondition holds**
- **Found during:** Task 2, Step B
- **Issue:** Test 2 would pass vacuously if the long label never wrapped.
- **Fix:** Added `assertEquals(2, lineCount)` for the long label before the height comparison. The mutation check above confirms the test guards the mechanism.
- **Commit:** d655bd2

**Total deviations:** 2 (1 RED-expectation note, 1 test-strengthening). **Impact:** no scope change.

## Issues Encountered

- **README vs v2.5.0.** `git diff --quiet v2.5.0 -- api.txt README.md` exits 1, because commit 2072575 (a quick-task docs reframe) changed README after v2.5.0. This plan did not touch README.
  - `git diff --quiet ce3516c -- README.md api.txt` (the plan base) exits 0.
  - `git diff --quiet v2.5.0 -- api.txt` exits 0.
  - README's "Current release: `v2.5.0`" line is unchanged.
- **Lane-2 commits.** The pre-commit hook blocked both GREEN commits as lane 2, because they rewrite existing src/main lines. Both landed with `HUB_LANE_OVERRIDE=2`, as the plan anticipated.

## User Setup Required

None.

## Next Phase Readiness

- G-15-1 is closed in code and tests. The Gate-2 visual re-check of the German gallery cell (light and dark, on the personal device via the Gate-2 harness "P15: LONG German labels") is deferred to the milestone-close Gate-2 pass.
- **Human-gated, not done here:** the v2.5.1 tag cut plus the SecondBrain and CalTracker repins await the owner's go-ahead. Route sequencing to the control-plane orchestrator first. No tag was created and no consumer file was touched.

## Self-Check: PASSED

- FOUND: all 8 key files (created and modified) are on disk and appear in `git diff --name-only ce3516c HEAD`.
- FOUND commits: 39985c7, f511b4e, 5534165, d655bd2, d693928 (`git log ce3516c..HEAD`, count 5).
- Acceptance criteria re-run:
  - `AFFORDANCE_MAX_WIDTH_FRACTION` count 2; `reserveSegmentIconSlot` count 2.
  - `git diff --quiet v2.5.0 -- api.txt` exits 0.
  - The SC5 and API.md grep checks are all satisfied.

---
*Phase: 15-voice-surface-i18n-label-params*
*Completed: 2026-10-12*
