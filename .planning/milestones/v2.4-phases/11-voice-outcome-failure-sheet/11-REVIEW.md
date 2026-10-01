---
phase: 11-voice-outcome-failure-sheet
reviewed: 2026-09-30T00:00:00Z
depth: standard
files_reviewed: 32
files_reviewed_list:
  - api.txt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/ClarificationBar.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/ClearableTextField.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/ModelSelectCard.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheet.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCard.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistry.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ExplorerEntry.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ExplorerIndexScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/VoiceCommandFamilyScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoGroupTypes.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoHistoryStore.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/ApproachRungUiModel.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/BatchRowResultUiModel.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/ClarificationOptionUiModel.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/FailureActionUiModel.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/HandledByUiModel.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/KeyFieldState.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/ModelOptionUiModel.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/ProviderOptionUiModel.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/UndoAffordanceUiModel.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/UndoRefusedUiModel.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/UndoRowUiModel.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/VoiceOutcomeUiState.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCardTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/ClarificationBarTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/ModelSelectCardTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCardTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/UndoHistoryStoreTest.kt
findings:
  critical: 2
  warning: 4
  info: 0
  total: 6
status: issues_found
---

# Phase 11: Code Review Report

**Reviewed:** 2026-09-30
**Depth:** standard
**Files Reviewed:** 32 (Phase 11 deliverable files reviewed at full depth; Phase 10 files in the same
commit range reviewed as supporting context per the workflow's SUMMARY-vs-git-diff cross-check)
**Status:** issues_found

## Summary

Phase 11 adds `OutcomeSheet` (VOUT-01/02/03), the grouped-undo affordance on `VoiceOutcomeUiState.Success`
(VUNDO-01), and the standalone `ClarificationBar` (VCLAR-01), plus the supporting UI models and
`UndoHistoryStore` group API (`openGroup`/`group`/`groupStatus`/`attemptUndoGroup`). The Compose
code itself is generally clean, prop-driven, and well-tested for the happy paths and several edge
cases (duplicate ids/labels, empty options, Unavailable/Refused undo rows, 50-cap group eviction).

Two real defects were found. The first is a hard fact, not a judgment call: the repo's committed
public-API signature dump (`api.txt`) was never regenerated for this phase, so it is missing every
new Phase 11 public symbol (`OutcomeSheet`, `ClarificationBar`, `VoiceOutcomeUiState`,
`UndoAffordanceUiModel`, `UndoRowUiModel`/`UndoRowState`, `UndoRefusedUiModel`,
`BatchRowResultUiModel`, `ClarificationOptionUiModel`, `FailureActionUiModel`, `HandledByUiModel`,
`UndoGroupStatus`, `UndoGroupResult`, `UndoGroupRefusedException`, and `UndoHistoryStore`'s entire
grouping API surface). The second is a genuine behavioral gap between `VoiceOutcomeUiState.Success`'s
own documented contract for `inFlight` and what `OutcomeSheet` actually renders: the KDoc promises
the sheet disables its undo controls while a batch write is in flight, but the implementation never
threads `inFlight` into the undo-rendering path at all, so undo buttons stay live and clickable
during an in-flight batch write.

## Critical Issues

### CR-01: `api.txt` was never regenerated for this phase's new public API

**File:** `api.txt` (and `build.gradle.kts:33-43` for the `apiDump`/`apiCheck` task pair that owns it)
**Issue:** Phase 11 introduces a large amount of new public API: `OutcomeSheet`/`ClarificationBar`
(composables), `VoiceOutcomeUiState` (+`Success`/`Failure`), `UndoAffordanceUiModel`,
`UndoRowUiModel`/`UndoRowState` (+`Available`/`Undone`/`Unavailable`), `UndoRefusedUiModel`,
`BatchRowResultUiModel`, `ClarificationOptionUiModel`, `FailureActionUiModel`, `HandledByUiModel`,
and on `UndoHistoryStore`: `UndoGroupStatus`, `UndoGroupResult`, `UndoGroupRefusedException`, the
grouped `append(message, preview, groupId, undoAction)` overload, `openGroup`, `group`, `groupIdOf`,
`groupLabel`, `groupStatus`, and `attemptUndoGroup`. None of this appears in the committed
`api.txt` — confirmed by grepping the file: the `component` package block has no `OutcomeSheetKt`/
`ClarificationBarKt` class, the `model` package block (lines 872-1121) jumps straight from
`ApproachRungUiModel` to `BrowseSortPreference` with no `BatchRowResultUiModel`/
`ClarificationOptionUiModel`/etc., and the `feedback` package's `UndoHistoryStore` entry (lines
776-783) still only lists the original 3-arg `append`, `attemptUndo`, `clearSpent`, and `entries` —
none of the grouping methods. By contrast, Phase 10's `ApproachLadderCard`/`ModelSelectCard`/
`ProviderKeyCard`/`RevealToggle` additions ARE present in `api.txt`, confirming the dump was
regenerated for Phase 10 but never re-run for Phase 11.

`api.txt` is explicitly "the committed public-API signature file" that `./gradlew apiDump`
regenerates and `./gradlew apiCheck` (wired to `metalavaCheckCompatibilityRelease`) verifies against
on every build per this repo's own `build.gradle.kts` comments. Per those same comments
(KI-2026-09-02-01), Metalava's compatibility check only fails on breaking *removals*, not additions,
so `apiCheck` will likely stay green despite this gap — which makes the drift silent rather than
loud, and is precisely why it slipped through. The net effect: the library's tracked public surface
is now materially wrong for anyone (a consumer, `API.md`, a future `apiDump` diff reviewer)
inspecting `api.txt` to understand what this release actually exports.
**Fix:** Run `./gradlew apiDump` and commit the regenerated `api.txt` before this phase ships.

### CR-02: `inFlight` is documented to disable undo controls, but `OutcomeSheet` never wires it there

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheet.kt:68-81`
(contract: `src/main/java/io/github/ygaray/yahirandroidtaste/model/VoiceOutcomeUiState.kt:31-33`)
**Issue:** `VoiceOutcomeUiState.Success.inFlight`'s KDoc states: *"the sheet uses this to disable its
own rendered undo controls and suppress `editableContent`; the library never infers this from any
other field."* The implementation only honors half of that contract:

```kotlin
private fun SuccessBody(success: VoiceOutcomeUiState.Success) {
    Column(...) {
        Text(text = success.summary, ...)
        success.handledBy?.let { HandledByRow(it) }
        success.batchResults.takeIf { it.isNotEmpty() }?.let { BatchResultsList(it) }
        success.editableContent?.takeIf { !success.inFlight }?.invoke()   // inFlight DOES gate this
        success.undo?.let { UndoAffordanceBody(it) }                     // inFlight NEVER reaches this
    }
}
```

`UndoAffordanceBody(it)` is called unconditionally whenever `success.undo` is non-null — `inFlight`
is never passed into it, and `UndoAffordanceBody`'s signature (`fun UndoAffordanceBody(undo:
UndoAffordanceUiModel)`) has no parameter through which it could even receive that signal. Undo-all
and every per-row `Available` undo button therefore stay live and clickable while a batch write is
still in flight, exactly the race the KDoc says this field exists to prevent (a user could tap
"Undo all" or a per-item undo mid-write, racing the in-flight batch write it is trying to protect
against). `OutcomeSheetTest.kt` has no test exercising `inFlight = true` together with a non-null
`undo`, so this gap is untested as well as unimplemented.
**Fix:** Thread `inFlight` into the undo render path and disable (or hide, per D-05 convention) the
undo controls while it's `true`, e.g.:

```kotlin
success.undo?.let { UndoAffordanceBody(it, locked = success.inFlight) }
```

and in `UndoAffordanceBody`/`UndoRowItem`, suppress the `clickable` modifier (or render a disabled
affordance) when `locked` is `true` — then add a regression test asserting that `undo_all`/
`undo_row` nodes lose their click action (or disappear, whichever the chosen convention is) when
`inFlight = true`.

## Warnings

### WR-01: `ApproachLadderCard`'s cap control can become clickable without a visible cap

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt:65,83`
**Issue:** The KDoc states `onMaxTierChange` "must be non-null exactly when `maxTierId` is
non-null," but the component only enforces half of that pairing at the call site:

```kotlin
val capRank = maxTierId?.let { id -> ladder.firstOrNull { it.id == id }?.rank } ?: Int.MAX_VALUE
...
onCapClick = onMaxTierChange?.let { callback -> { callback(rung.id) } }
```

`onCapClick` (and therefore every rung's clickability) is gated solely on `onMaxTierChange`, never
on `maxTierId`. If a caller violates the documented invariant — passing `onMaxTierChange` non-null
while leaving `maxTierId` null — every rung silently becomes clickable (contradicting the KDoc's "no
rung becomes clickable" guarantee for the null-cap state) even though `capRank` falls back to
`Int.MAX_VALUE` and no rung ever shows "Capped." This is a defensive gap rather than a reachable
library bug under correct usage, but nothing in the component (or its tests) catches the caller
contract violation.
**Fix:** Either assert the pairing (`require((maxTierId == null) == (onMaxTierChange == null))`) or
derive `onCapClick` from both props together, e.g. `onCapClick = if (maxTierId != null)
onMaxTierChange?.let { ... } else null`.

### WR-02: An unmatched `maxTierId` silently disables the cap with no signal

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt:65`
**Issue:** `val capRank = maxTierId?.let { id -> ladder.firstOrNull { it.id == id }?.rank } ?:
Int.MAX_VALUE` — if the caller passes a `maxTierId` that doesn't match any rung in `ladder` (e.g. a
stale id after the ladder changed), `firstOrNull` returns `null`, and the `?:` fallback silently
treats this exactly like "no cap at all" (`Int.MAX_VALUE`), with no assertion, log, or visible
affordance that the caller's `maxTierId` was invalid. A genuine integration bug (ladder/cap getting
out of sync) is masked as "nothing is capped" instead of surfacing.
**Fix:** At minimum, document this fallback explicitly in the KDoc (it currently isn't), or consider
failing loudly in debug builds (`check(ladder.any { it.id == maxTierId })`) so an integration mismatch
is caught in testing rather than silently rendered as "no cap."

### WR-03: `ClarificationBar`'s option row has no scroll/wrap for more options than fit on screen

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ClarificationBar.kt:66-77`
**Issue:**
```kotlin
Row(
    modifier = Modifier.padding(top = 8.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
) {
    options.forEach { option -> AppChip(..., modifier = Modifier.testTag("clarification_bar_option")) }
}
```
A plain `Row` with no `horizontalScroll`/`FlowRow` wrapping lays every option chip out in one line;
with more options than fit in the available width, the trailing chips are pushed off-screen rather
than wrapping or becoming scrollable, making them neither visible nor tappable. The KDoc and tests
only cover 1-3 options — nothing in this file bounds `options.size`, and the VCLAR-01 contract (no
de-dup, exact list order, consumer owns uniqueness) explicitly allows an arbitrarily long list.
**Fix:** Wrap the chip row in `Modifier.horizontalScroll(rememberScrollState())`, or switch to
`FlowRow` (already used elsewhere in this module, e.g. `ChipBar`) so overflow wraps to a second line
instead of clipping off-screen.

### WR-04: `ProviderKeyCard` trims the key value on every keystroke, not just on paste

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCard.kt:84`
**Issue:** The KDoc for `onKeyChange` says: *"Pasted whitespace is trimmed before this callback
fires (pure formatting, no validation)"* — but the implementation applies `.trim()` unconditionally
on every `onValueChange` call, not only on paste:

```kotlin
onValueChange = { onKeyChange(it.trim()) },
```

This is broader than documented: it strips leading/trailing whitespace on every single keystroke,
not just the paste event the KDoc specifically calls out. In practice this mostly matches intent for
a single-line key field, but it's worth tightening the doc (or the implementation) so the two agree
— e.g. if an IME momentarily inserts a trailing space mid-composition, the field will silently
strip it before the composition settles, which is a slightly different behavior than "trim pasted
whitespace."
**Fix:** Either update the KDoc to say "every edit is trimmed" (matching the code), or scope the
trim to the paste path only if that's the intended behavior.

---

_Reviewed: 2026-09-30_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
