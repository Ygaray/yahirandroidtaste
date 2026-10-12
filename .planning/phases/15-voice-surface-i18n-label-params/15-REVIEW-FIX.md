---
phase: 15-voice-surface-i18n-label-params
fixed_at: 2026-10-11T00:00:00Z
review_path: .planning/phases/15-voice-surface-i18n-label-params/15-REVIEW.md
iteration: 1
findings_in_scope: 8
fixed: 8
skipped: 0
status: all_fixed
---

# Phase 15: Code Review Fix Report

**Fixed at:** 2026-10-11
**Source review:** .planning/phases/15-voice-surface-i18n-label-params/15-REVIEW.md (15-04 gap-closure increment, G-15-1)
**Iteration:** 1
**Fix scope:** all (Critical + Warning + Info)

**Summary:**
- Findings in scope: 8 (1 critical, 4 warning, 3 info)
- Fixed: 8
- Skipped: 0

**Where verification ran:** in the **main checkout** on `main` (sequential-in-hub convention, as the
orchestrator instructed; no review-fix worktree was created, even though `workflow.use_worktrees` is
`true` in config). The numbers below can be reproduced from this tree.
- Final gate `./gradlew testDebugUnitTest detekt apiCheck`: BUILD SUCCESSFUL, 767 tests, 0 failures
  (22 skipped, as before), detekt 0 code smells at zero baseline (no baseline regenerated).
- `git diff --quiet v2.5.0 -- api.txt` exits 0, so the public surface is byte-identical to v2.5.0.
- Every new or strengthened test was run **RED against the broken code first**, by temporarily
  swapping in the old component files and restoring them with `git checkout --`. The RED messages are
  quoted per finding below.

**Commits:** c877c9d, 44ed997, 9d3597b, b46e62a, 5bae3f6, 699e12b, 96ad1c2, abdc4a8. Source-rewriting
commits used `HUB_LANE_OVERRIDE=2`; test-only commits were lane 1. Nothing was tagged or pushed, and no
consumer was touched.

## Fixed Issues

### CR-01: The 60% cap stacks the English default affordances onto two lines (v2.5.0 regression)

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt`, `API.md`
**Commit:** c877c9d
**Status:** fixed: requires human verification (layout algorithm; please check visually at Gate-2)
**Applied fix:**
- Replaced the bare name `Text` plus the `maxWidthFraction`-capped `FlowRow` with a private
  multi-content `Layout`, `RungLabelLayout`. It is the single `weight(1f)` child of `CapControl`, whose
  signature is unchanged. This is the reviewer's sketch, with two changes:
  - The floor is computed as `row - floor(0.6 * row)` to avoid float drift.
  - An unbounded-width fallback was added.
- The affordance group now gets every pixel the name does not need. The name only reserves
  `min(its max-intrinsic width, 40% of the row)` and fills the rest, as `weight(1f)` did before.
- Composition order is still name, then affordances, so the merged a11y announcement is unchanged.
  The affordance child is composed only when an affordance applies (no dead gap).
- The affordance `Text`s moved into a small private `AffordanceText` helper with the same
  style, color, `maxLines = 2`, Ellipsis and End alignment.
- The public KDoc and the API.md "Long labels (v2.5.1)" bullet now say: the name keeps its full width
  or 40% of the row, whichever is smaller. The affordances never take more than 60%. Short labels
  render as in v2.5.0, all on one line next to the name.
- All German G-15-1 truths still hold: `ApproachLadderCardLongLabelTest` German tests,
  `ApproachLadderCardTest` 33/33, and the gallery test all pass.

### WR-01: The new gallery test passes against the pre-fix code, so it guards nothing

**Files modified:** `src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ApproachLadderCardGalleryDemoTest.kt`
**Commit:** 9d3597b
**Applied fix:**
- Added `@GraphicsMode(NATIVE)` and `@Config(qualifiers = "w360dp-h800dp")` to the method, and
  wrapped the content in `YahirAndroidTasteTheme`.
- The test now finds the German cell's Cloud name: the first `"Cloud"` node below the
  "long localized labels (German)" section label. It asserts that node has `size.width > 0` and is
  not ellipsized. The existing assertions are kept.
- **RED** with the `ce3516c` `ApproachLadderCard.kt` and `SegmentedOptionSelector.kt`:
  `German cell's Cloud name width must be > 0, was 0`. **GREEN** at HEAD.

### WR-02: The English guard tests cannot detect CR-01

**Files modified:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCardLongLabelTest.kt`
**Commit:** 44ed997
**Applied fix:**
- The English guard now also asserts that the three Cloud-row affordances share one top, through
  a shared `assertEnglishCloudRowUnchanged()`.
- It runs at w360dp, at w320dp (method-level `@Config`), inside the gallery's 16dp horizontal
  padding, and at fontScale 1.3 (`LocalDensity` override).
- **RED** against 187548d (the pre-CR-01 HEAD) in all 4 cases, for example
  `English affordances must share one line, tops=[30.0, 30.0, 45.0]` and, at fontScale 1.3,
  `tops=[27.0, 27.0, 45.0]`. These match the reviewer's probes. **GREEN** after CR-01.

### WR-03: The unconditional 26dp slot wraps short labels at large font scales

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/SegmentedOptionSelector.kt`, `src/test/java/io/github/ygaray/yahirandroidtaste/component/SegmentedOptionSelectorTest.kt`
**Commit:** b46e62a
**Status:** fixed: requires human verification (the selected check icon has a tighter margin on a label that fills its segment, as in v2.5.0)
**Applied fix:**
- Took option (b). I confirmed the geometry by disassembling M3 1.4.0:
  - `SegmentedButtonContent` is a `Box(Alignment.Center)` padded by
    `SegmentedButtonDefaults.ContentPadding` (= `ButtonDefaults.TextButtonContentPadding`).
  - A full-width label therefore overflows only by `slot - horizontal padding`.
- `reserveSegmentIconSlot()` now reserves `(IconSize + IconSpacing) - (left + right ContentPadding)`
  in px, about 2dp, instead of the whole 26dp.
- The German no-clip test still passes; with a reserve of 0 it was RED by 1px in 15-04.
- Two new tests run at fontScale 2.0 and check that each label is on one line and inside its segment:
  "Router off"/"Router on" in a 296dp toggle, and "Online"/"Offline only" in a 328dp toggle.
  - **RED** before the change: `"Router off" line count at fontScale 2 expected:<1> but was:<2>`
    and `"Offline only" ... expected:<1> but was:<2>`.
  - **GREEN** after it.
- API.md section 5 is unchanged, because its claims ("never under the selected check icon", equal
  heights) still hold.

### WR-04: The reserved slot copies the wrong M3 constant (`ButtonDefaults.IconSpacing`)

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/SegmentedOptionSelector.kt`
**Commit:** 5bae3f6
**Applied fix:**
- Confirmed via `javap` that `SegmentedButtonContentMeasurePolicy` reads the private
  `SegmentedButtonKt.IconSpacing`, which is set to `8` dp in `<clinit>`.
- Added a private `SegmentedButtonIconSpacing = 8.dp` with a KDoc that cites the M3 source, and used
  it in the slot computation.
- Corrected the modifier KDoc and dropped the now-unused `ButtonDefaults` import.
- The segment-overflow tests remain the M3-bump tripwire.

### IN-01: "At least 40%" is really 40% minus the 4dp gap

**Files modified:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCardLongLabelTest.kt`
**Commit:** 699e12b
**Applied fix:**
- CR-01's layout takes the 4dp gap from the affordances' share, so the floor is 40% of the whole row.
  The API.md wording was corrected in the CR-01 commit.
- This commit pins it with a new test: a too-long rung name next to a row-filling affordance (one
  unbreakable compound word, so the affordance takes every pixel offered) keeps `>= 40%` of the row.
- **RED** against 187548d: `long name width 128 must be >= 40% of the row width 328`. **GREEN** at HEAD.

### IN-02: No RTL coverage, and the overlap test is LTR-only

**Files modified:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCardLongLabelTest.kt`
**Commit:** 96ad1c2
**Applied fix:** Added an RTL variant using `LocalLayoutDirection provides Rtl` with the German labels.
It asserts:
- the Cloud name has nonzero width and is not ellipsized;
- every Cloud-row affordance's right edge is at or left of the name's left edge;
- both offline-only segment labels stay inside their segments (0.5px tolerance).

The floor check the review suggested is the IN-01 test. Like the reviewer's probe, this test passes on
both the old and the new layout. It is a coverage pin, not a regression repro.

### IN-03: Misleading "placeRelative so RTL mirrors" comments

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/SegmentedOptionSelector.kt`
**Commit:** abdc4a8
**Applied fix:**
- Reworded the `reserveSegmentIconSlot` KDoc. The wrapper reports exactly the label's size at (0, 0)
  and mirrors nothing itself; RTL placement comes from M3's own segment layout.
- The `ApproachLadderCard` instance went away with CR-01, which removed `maxWidthFraction`. The new
  `RungLabelLayout` places its children at different x offsets with `placeRelative`, so its
  "RTL mirrors" comment is accurate, and the IN-02 test pins it.

## Notes for the orchestrator

- The 15-04 planning artifacts still describe the replaced mechanism: the `15-04-PLAN.md` key_links,
  the `15-04-SUMMARY.md` and possibly `15-VERIFICATION.md` mention `maxWidthFraction` and the fixed 60%
  cap. I did not edit them, because they are historical plan records outside the fixer's scope.
  `AFFORDANCE_MAX_WIDTH_FRACTION` is still declared and used (the plan's grep acceptance holds).
- The Gate-2 visual check of the German gallery cell is still pending. The German affordances now get
  more width (they use whatever the name does not need), so they may stack differently than in the
  15-04 build.
- Not tagged, not pushed, no consumer touched. The v2.5.1 tag and the repins still need human sign-off.

## Prior fix pass (2026-10-05)

Prior-pass frontmatter: `fixed_at: 2026-10-05`, `iteration: 1`, `findings_in_scope: 6`, `fixed: 4`,
`skipped: 2`, `status: resolved` (review of plans 15-01..03).

**Fixed at:** 2026-10-05
**Source review:** .planning/phases/15-voice-surface-i18n-label-params/15-REVIEW.md
**Iteration:** 1

**Summary:**
- Findings in scope: 6
- Fixed: 4
- Skipped: 2 (both documented acceptable-skips: each needs new public API beyond VI18N-01..04 scope)

**Verification:** run in the isolated review-fix worktree (`.claude/worktrees/rf-15-*`, since torn down), not the
main checkout. `./gradlew testDebugUnitTest --tests '*VoiceI18nSourceCompatTest*'` (2 tests, 0 failures),
`--tests '*VoiceModelLabelDefaultsTest*'` (18 tests, 0 failures) and `./gradlew detekt` (exit 0) were run on
the touched test files. No main-source or api.txt changes were made, so `apiCheck` is unaffected (the commit
hook classified every commit as LANE 1, additive). The full test suite was not run (out of scope for the fixer).

### Fixed Issues

#### WR-01: Synthetic `$default` members change; Metalava does not see this

**Files modified:** `API.md`
**Commit:** 0d94c11
**Applied fix:** Added an API.md section "Appending label fields to the voice models and composables (v2.5.0)"
stating the change is source-compatible but NOT binary-compatible for Kotlin default-arg call sites (synthetic
constructor / `copy$default` / composable `$default` and changed-mask members), that a clean `apiCheck` is not
proof of binary compat, and that consumers must recompile on repin to v2.5.0. Doc-only, as the review
recommended. Note: the phase closing-gate / v2.5.0 release notes were not edited here; if they claim "binary
compat" they should be reworded to match (not found in the reviewed files).

#### WR-03: Hard-coded joining spaces mean empty or punctuation-sensitive labels produce malformed text

**Files modified:** `API.md`
**Commit:** 156f99f
**Applied fix:** Documented (the review's first, doc-level option) that fragments are joined by a single ASCII
space and a literal `", "`, that punctuation/colon belongs to the label, that a blank label must not be used to
mean "no lead-in", and that other orders/separators require folding the item into `reason`. The runtime
empty-fragment skipping (the second option) and any separator redesign were NOT applied (see Follow-ups).

#### IN-01: Vacuous assertions in the source-compat test

**Files modified:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/VoiceI18nSourceCompatTest.kt`
**Commit:** e9a5adc
**Applied fix:** Removed the four `assertNotNull` lines and the unused import, added an explanatory
compile-only comment and `@Suppress("UNUSED_VARIABLE")` on the test. The second half of the finding (exercising
the positional shapes' default rendering at runtime) is already covered by the per-component tests named in the
review and was not duplicated.

#### IN-02: No guard against legacy-arity `copy` drift

**Files modified:** `src/test/java/io/github/ygaray/yahirandroidtaste/model/VoiceModelLabelDefaultsTest.kt`
**Commit:** 37266ed
**Applied fix:** Added a reflection-based helper `assertLegacyCopyCarriesEveryField` and a test that, for each of
the four models built with every field non-default, invokes the shortest hand-written `copy` with the instance's
own leading components and asserts `copy == original`. A legacy copy that resets or drops a later-appended field
now fails.

### Skipped Issues

#### WR-02: `removeContentDescription` is per item, so a partially localized list mixes languages

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/model/ProposedItemUiModel.kt:41`
**Reason:** Documented acceptable-skip. The fix is a sheet-level `OutcomeSheet(removeItemContentDescription = ...)`
override, i.e. new public API outside the VI18N-01..04 scope; the review itself says to accept the per-item
design for v2.5 and record a follow-up. The optional "mixed custom + default list renders each as set" test was
not added either (it would pin behavior of a design slated for revision).
**Original issue:** The label is a per-item field, so any item that forgets it announces English "Remove" in an
otherwise localized TalkBack session; it also enters `equals`/`hashCode`/`copy`.

#### WR-04: Localized segment label is announced with English state words

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt:132`
**Reason:** Documented acceptable-skip. Either fix (replace the manual `contentDescription` with
`Role.RadioButton`/`selected` semantics, or add `selectedStateLabel`/`notSelectedStateLabel` params to
`SegmentedOptionSelector`) changes shared component behavior or public API beyond VI18N-01..04, and the existing
tests intentionally pin the current announcement (`"En ligne, selected"`) as the documented residual.
**Original issue:** TalkBack announces "En ligne, selected": localized label plus English state words.

### Follow-ups (not applied, for the orchestrator/backlog)

- WR-02: sheet-level remove-description override on `OutcomeSheet`.
- WR-03: skip empty fragments / configurable separators when joining label fragments (behavioral change).
- WR-04: localizable or semantics-based selected state in `SegmentedOptionSelector`; then re-baseline the tests
  that pin the English state words.

---

_Fixed: 2026-10-05_
_Fixer: Claude (gsd-code-fixer)_
_Iteration: 1_
