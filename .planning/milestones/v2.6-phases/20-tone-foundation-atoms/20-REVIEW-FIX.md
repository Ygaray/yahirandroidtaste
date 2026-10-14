---
phase: 20-tone-foundation-atoms
fixed_at: 2026-10-12T00:00:00Z
review_path: .planning/phases/20-tone-foundation-atoms/20-REVIEW.md
iteration: 1
findings_in_scope: 10
fixed: 9
skipped: 1
status: resolved
---

# Phase 20: Code Review Fix Report

**Fixed at:** 2026-10-12
**Source review:** .planning/phases/20-tone-foundation-atoms/20-REVIEW.md
**Iteration:** 1

**Summary:**
- Findings in scope: 10 (4 Warning, 6 Info)
- Fixed: 9 (IN-03 and IN-06 partially, remainder documented below)
- Skipped: 1 (IN-04, deferred)

**Verification:** run in the main checkout (no worktree), sequential, one Gradle invocation at a
time. Per-fix targeted runs for WR-01, WR-02, WR-04. Final gate
`./gradlew testDebugUnitTest detekt apiCheck --console=plain -q` exits 0 (detekt zero-baseline
kept, apiCheck green; no public API change, so `api.txt` was not regenerated).

## Fixed Issues

### WR-01: StackedProgressBar caches fractions on a mutable list
**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/StackedProgressBar.kt`, `src/test/java/io/github/ygaray/yahirandroidtaste/component/StackedProgressBarTest.kt`
**Commit:** 1b485ab
**Applied fix:** Dropped `remember(segments)`; fractions and colors are now read from one `segments.toList()` snapshot per composition. Added a regression test that shrinks and edits a `SnapshotStateList` in place. Status: fixed (logic change; the new test covers it, human glance still advisable).

### WR-02: TextSmall compact tokens are inert
**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/DynamicActionButton.kt`, `src/main/java/io/github/ygaray/yahirandroidtaste/theme/Dimens.kt`, `src/test/java/io/github/ygaray/yahirandroidtaste/component/DynamicActionButtonEmphasisTest.kt`
**Commit:** 79ecf8a
**Applied fix:** Minimal honest fix: corrected the `ActionButtonEmphasis.TextSmall` KDoc and the two `Dimens.ActionButton.TextSmall*` KDocs to say the M3 `TextButton` minimum governs the drawn size. Replaced the `println` debug artifact with assertions that Text and TextSmall draw at 40dp. No behavior change.

### WR-03: icon-only call restyles a legacy button
**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/DynamicActionButton.kt`
**Commit:** 4a977b3
**Applied fix:** Documentation fix. The KDoc now states that an icon (or emphasis) routes through the emphasis styling (SemiBold label, pill Save with 20dp padding, 48dp touch target) and is not pixel-identical to the no-icon button. The no-emphasis/no-icon legacy path is untouched.

### WR-04: tautological icon-legacy test
**Files modified:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/DynamicActionButtonEmphasisTest.kt`, `src/test/java/io/github/ygaray/yahirandroidtaste/component/DynamicActionButtonLegacyPathSourceContractTest.kt`
**Commit:** 23736b2
**Applied fix:** Added a test comparing the icon-only render with the real no-icon legacy render and asserting the documented width deltas (+26dp, -8dp more for Save, 4dp label-weight tolerance); a mapping regression shifts it by 16dp and fails. Kept the existing mapping test. Scoped the source-contract token checks (Dimens tokens, `CircleShape`, `FontWeight.SemiBold`) to the `EmphasisActionButton` and `EmphasisContent` bodies. `DynamicActionButtonTest`, `AppChipTest`, `PresetChipTest` untouched.

### IN-01: class KDoc contradicts `emphasis`
**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/DynamicActionButton.kt`
**Commit:** 69b0c79
**Applied fix:** First KDoc paragraph now reads "With no `emphasis`, the role -> widget mapping is fixed..." and names `emphasis` as the only widget choice.

### IN-02: PillChip.Height KDoc says "Fixed"
**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/theme/Dimens.kt`
**Commit:** dcdf8bb
**Applied fix:** KDoc now reads "Minimum drawn height of the chip (it grows with font scale)."

### IN-03: DISABLED_ALPHA duplicated (partial)
**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/PillChip.kt`, `src/main/java/io/github/ygaray/yahirandroidtaste/component/RoundIconButton.kt`
**Commit:** 87a3585
**Applied fix:** One `internal const val DISABLED_CONTENT_ALPHA` in `PillChip.kt`, used by both Phase 20 atoms. Not touched: the bare `0.38f` in `PresetChip`, `DateTimePicker`, `OutcomeSheet`. Those are v2.5.1 sources in a lane-2 additive phase; editing them is out of scope for this fix and carries regression risk for no behavior gain. Acceptable-skip for that sub-part; suitable for a later cleanup.

### IN-05: StackedProgressBar tag and RTL wording
**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/StackedProgressBar.kt`
**Commit:** ebf4af8
**Applied fix:** KDoc now says "start to end" (left in LTR, right in RTL), notes that a consumer enabling `testTagsAsResourceId` would expose both tags, and notes the unbounded-width collapse. The segment `testTag` itself was kept (existing tests rely on it; removing it is a behavior change).

### IN-06: smaller test and design notes (partial)
**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/theme/ToneTokens.kt`
**Commit:** 72fe0fd
**Applied fix:** Extended the `ToneDefaults.error()` KDoc with the thin contrast margin and the "pass your own `ToneColors` under dynamic color" advice. The `println` was removed earlier in the WR-02 commit (79ecf8a). Not applied: scoping the drift-guard allowlist (the guard only supports a global head-token set; scoping needs a guard redesign, not a fix) and confirming `ContentTone` has a Phase 21/22 consumer (a planning question for the owner, not a code change).

## Skipped Issues

### IN-04: BarSegment is a public data class
**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/StackedProgressBar.kt:29-33`
**Reason:** skipped: deferred. Converting to the `ToneColors` plain-class pattern removes `copy`/`componentN`/destructuring from the public API, which is a non-additive change, and the choice is explicitly the owner's call before the v2.6.0 cut (D-01 signature already signed off). Adding a justification KDoc would record a decision that has not been made. Left as is.
**Original issue:** `BarSegment` publishes `copy` and `componentN` into `api.txt`, inconsistent with `ToneColors`.

---

_Fixed: 2026-10-12_
_Fixer: Claude (gsd-code-fixer)_
_Iteration: 1_
