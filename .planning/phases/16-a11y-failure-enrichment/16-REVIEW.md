---
phase: 16-a11y-failure-enrichment
reviewed: 2026-10-05T00:00:00Z
depth: standard
files_reviewed: 10
files_reviewed_list:
  - api.txt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheet.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/FailureActionUiModel.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/VoiceOutcomeUiState.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCardTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/FailureRoleSourceContractTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/VoiceI18nSourceCompatTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/model/VoiceModelLabelDefaultsTest.kt
findings:
  critical: 0
  warning: 2
  info: 3
  total: 5
status: issues_found
---

# Phase 16: Code Review Report

**Reviewed:** 2026-10-05
**Depth:** standard
**Files Reviewed:** 10
**Status:** issues_found

## Summary

The phase is largely sound.

- **Source compatibility.** The `@JvmOverloads` constructors and the hand-written legacy-arity `copy` overloads for `FailureActionUiModel` and `VoiceOutcomeUiState.Failure` are consistent. The delegating `copy` overloads carry the current `role`, `body` and `semanticsPrefix` forward, so a legacy `copy` never resets a custom value.
- **api.txt.** The changes are purely additive, with no removals.
- **Repo invariants.** No consumer imports, no `@HiltAndroidApp`, and no new public `@Composable`, so the registry drift guard is unaffected.
- **Ladder rung semantics.** `Selected` plus `Role.RadioButton` is applied only when the cap is selectable. Selection follows `maxTierId` and not the "Capped" state. A stale id selects nothing, and cap-less ladders get no role, selected state or click.

The concerns are accessibility correctness (WR-01, WR-02), not crashes.

## Warnings

### WR-01: `semanticsPrefix` contentDescription on the merged Failure surface can mask the handled-by and body text

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheet.kt:303-312`
**Issue:** When `semanticsPrefix` is non-blank, the surface node gets `semantics(mergeDescendants = true) { contentDescription = "$prefix $reason" }`. That node also merges the Text of `reason`, the handled-by row and the caller `body` slot. On Android, an explicit `contentDescription` on a node generally overrides its text for TalkBack, so the prefixed announcement may read only "prefix reason", dropping the handled-by tier text and any `body` content. This rests on TalkBack precedence that was not verified on a device. None of the new tests cover prefix together with `handledBy` or `body`.

**Fix:** Verify on TalkBack (Gate-2). If text is dropped, scope the prefix to the reason only or add the prefix as a separate node; add an `OutcomeSheetTest` case with prefix plus `handledBy` plus `body`.

### WR-02: Radio-button rungs lack a `selectableGroup()` container

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt:115`
**Issue:** The rung rows are now `Role.RadioButton` + `Selected`, but the parent `Column` has no `Modifier.selectableGroup()`, so accessibility services do not see a radio group (no position / group context announced).

**Fix:** Apply `Modifier.selectableGroup()` only when the cap is selectable (`onMaxTierChange != null`), so a cap-less ladder is not announced as a group.

## Info

### IN-01: Blank check and interpolation disagree on whitespace in `semanticsPrefix`

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheet.kt:303-306`
**Issue:** The guard uses `isNullOrBlank()`, but the description interpolates the untrimmed prefix; a padded prefix yields a double space. The KDoc promises "one ASCII space".

**Fix:** `.trim()` the prefix before interpolating, or document that the caller owns whitespace and pin it in a test.

### IN-02: Tests emit `println` noise

**File:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCardTest.kt` (about lines 393 and 421)
**Issue:** Debug `println`s add nothing since assertion messages carry the values.

**Fix:** Remove the `println` calls.

### IN-03: Explorer gallery not updated for the new Failure and rung capabilities

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/VoiceCommandFamilyScreen.kt:367-372`
**Issue:** The Failure fixtures still use only `reason` and `action`; the new `role`, `body`, `semanticsPrefix` features have no gallery example. Coverage gap, not a drift-guard violation. (Plan 16-03 pinned explorer/ byte-identical to v2.4.1 by design, RESEARCH A5.)

**Fix:** Add a fixture in a later phase if desired.

---

_Reviewed: 2026-10-05_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
