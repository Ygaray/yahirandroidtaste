---
phase: 12-generic-needs-confirmation-state
reviewed: 2026-09-30T00:00:00Z
depth: standard
files_reviewed: 5
files_reviewed_list:
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/ProposedItemUiModel.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/VoiceOutcomeUiState.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheet.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/VoiceCommandFamilyScreen.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetTest.kt
findings:
  critical: 1
  warning: 5
  info: 1
  total: 7
status: issues_found
---

# Phase 12: Code Review Report

**Reviewed:** 2026-09-30
**Depth:** standard
**Files Reviewed:** 5
**Status:** issues_found

## Summary

Reviewed `VoiceOutcomeUiState.NeedsConfirmation`, the new `ProposedItemUiModel`/`SelectionMode`
model, `OutcomeSheet`'s `NeedsConfirmationBody`/`ProposedItemRow` rendering, the two new Explorer
gallery fixtures, and the VOUT-04 test section against the plan's `must_haves` and `prohibitions`.

The core contract holds up well: the sealed arm is purely additive, `Success`/`Failure` are
untouched, no new severity enum was introduced, ordering/no-dedup/opaque-id/empty-list behavior is
implemented and tested correctly, and `ProposedItemUiModel.toString()` is redacted exactly as
specified and unit-tested.

However, the plan's own privacy prohibition is **not fully satisfied**: it explicitly extends to
"any default data-class-generated representation reachable from `NeedsConfirmation`," and
`NeedsConfirmation` itself has no `toString()` override — its default representation prints
`title`/`reason` verbatim, which are exactly the kind of caller-formatted, subject-identifying
strings (e.g. SecondBrain's real "Delete '<card title>'?" case) the `ProposedItemUiModel` redaction
was built to avoid leaking into logs/crash reports/recomposition traces. This is classified a
Critical finding below.

Several warning-level gaps were also found: the Explorer gallery's own `NeedsConfirmation` fixtures
don't follow the component's own documented "`onDismissRequest` must route to the same decline
logic as `onCancel`" integration contract; two rendering code paths (`trailingContent`, and any
visual treatment of `amended`) ship with zero test/fixture coverage; action-button and item rows
lack the horizontal spacing convention used elsewhere in this same file and library; and
`VoiceOutcomeUiState`'s interface-level KDoc is now factually stale (still claims "exactly two
arms") as a side effect of the documented DS-05-guard revert.

## Critical Issues

### CR-01: `NeedsConfirmation`'s own default `toString()` leaks `title`/`reason` — violates the phase's own privacy prohibition

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/model/VoiceOutcomeUiState.kt:108-120`
**Issue:** The plan's privacy prohibition (12-01-PLAN.md, `must_haves.prohibitions`, category
`privacy`) reads: "MUST NOT let `ProposedItemUiModel`'s `toString()` **(or any default
data-class-generated representation reachable from `NeedsConfirmation`)** print title, subtitle, or
confidenceCue into a log, crash report, or recomposition trace." The mitigation actually shipped
(T-12-01 in the plan's own threat register) only overrides `ProposedItemUiModel.toString()`.
`VoiceOutcomeUiState.NeedsConfirmation` itself is a plain `data class` with **no `toString()`
override** — its Kotlin-generated default `toString()` prints every constructor property verbatim,
including `title: String?` and `reason: String`, both of which are documented as "caller-formatted,
human-readable" free text (see this same file's KDoc at lines 85 and 89: `reason` is "Caller-formatted,
human-readable reason this confirmation is needed", `title` is "e.g. 'Delete card?'"). In SecondBrain's
real `MutationGate`/`VoiceConfirmGate` usage this is exactly the field that will carry the subject's
own name (e.g. `title = "Delete 'My Secret Diary'?"`), which is precisely the class of information
the `ProposedItemUiModel` redaction exists to keep out of logs/crash reports/recomposition traces.
Logging or crash-reporting a `NeedsConfirmation` instance directly (e.g. `Log.d(TAG, "$outcome")` in
a consumer, or an automatic Compose state-dump in a crash handler) re-leaks exactly the data the
`ProposedItemUiModel` fix was designed to prevent — the mitigation is incomplete relative to its own
stated scope.
**Fix:**
```kotlin
data class NeedsConfirmation(
    val reason: String,
    val items: List<ProposedItemUiModel>,
    val selectionMode: SelectionMode = SelectionMode.AllOrNothing,
    val title: String? = null,
    val severity: ActionButtonDefaults.ActionButtonRole = ActionButtonDefaults.ActionButtonRole.Neutral,
    val reversibilityHint: String? = null,
    val confirmLabel: String = "Confirm",
    val cancelLabel: String = "Cancel",
    val topLevelContent: (@Composable () -> Unit)? = null,
    val onConfirm: () -> Unit,
    val onCancel: () -> Unit
) : VoiceOutcomeUiState {
    override fun toString(): String =
        "NeedsConfirmation(items=${items.size}, selectionMode=$selectionMode, severity=$severity)"
}
```
Add a unit test mirroring the existing `ProposedItemUiModel toString` privacy test, asserting
`NeedsConfirmation(...).toString()` never contains the fixture's `title`/`reason` text.

## Warnings

### WR-01: The library's own canonical gallery fixtures don't follow `NeedsConfirmation`'s own documented dismiss/cancel contract

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/VoiceCommandFamilyScreen.kt:488-490`
**Issue:** `NeedsConfirmation`'s KDoc (`VoiceOutcomeUiState.kt:78-83`) states a consumer "MUST route
`OutcomeSheet`'s own `onDismissRequest` param to the SAME decline logic as `onCancel`." The Explorer
gallery — the one place in this repo meant to demonstrate correct usage of every component — wires a
single generic `onDismissRequest = { visibleOutcome = null }` for every fixture type, including both
new `NeedsConfirmation` fixtures, while each fixture's own `onCancel = {}` is a separate no-op never
invoked by that dismiss path. Functionally harmless here (both callbacks are no-ops), but the
reference implementation a consumer would copy directly contradicts the contract the component's own
KDoc asks consumers to honor, undermining the gallery's value as a correctness example and risking a
consumer copying the same gap into production code with real `onCancel` side effects.
**Fix:** Either special-case the dismiss handler for the two `NeedsConfirmation` fixtures to also
invoke the fixture's own `onCancel`, or add a one-line gallery-only comment flagging the shortcut so
a reader doesn't mistake it for the recommended wiring.

### WR-02: `amended` is modeled, privacy-redacted, and exercised in the batch fixture, but never rendered or tested

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheet.kt:344-372`,
`src/main/java/io/github/ygaray/yahirandroidtaste/explorer/VoiceCommandFamilyScreen.kt:415`
**Issue:** `ProposedItemUiModel.amended` is a public, documented field (D-04: "Whether the caller has
in-place-edited this item") and the batch gallery fixture deliberately sets `amended = true` on the
"Bread" item to demonstrate it — but `ProposedItemRow` never reads `item.amended` at all, so there is
no library-native visual difference between an amended and unamended row, and no test in
`OutcomeSheetTest.kt` asserts any amended-related rendering. As shipped, `amended`'s only observable
effect anywhere in this codebase is its appearance in `toString()`'s debug output. If the intent is
"consumers render this themselves via `trailingContent`," that's a reasonable design, but it should
be stated in `amended`'s KDoc (it currently is not) rather than left to guesswork from a fixture that
implies — but doesn't deliver — a visible effect.
**Fix:** Either add a minimal rendering treatment (e.g. an "Edited" label/icon shown when
`item.amended`), or amend the KDoc on `ProposedItemUiModel.amended` to state explicitly that the
library intentionally renders nothing for this flag and that any visual indicator is the consumer's
responsibility via `trailingContent`.

### WR-03: `trailingContent` has a real rendering path with zero test or gallery coverage

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheet.kt:362`
**Issue:** `item.trailingContent?.invoke()` is live production code added this phase, but neither
Explorer fixture (`fixtureOutcomeNeedsConfirmationSingleDestructive`,
`fixtureOutcomeNeedsConfirmationBatch`) nor any `OutcomeSheetTest.kt` test ever constructs a
`ProposedItemUiModel` with `trailingContent` set — confirmed via direct grep, zero matches in either
file. This is the one per-item "opaque slot" field (parallel to `NeedsConfirmation.topLevelContent`,
which IS exercised and asserted) that ships with no demonstration and no regression coverage; a
future refactor of `ProposedItemRow` could silently break or remove this line without any test
failing.
**Fix:** Add a gallery fixture item and a Compose test asserting a `trailingContent` composable
(e.g. a `Text("tag")`) renders for its own row, mirroring the existing `topLevelContent` coverage.

### WR-04: Action-button and item rows omit the horizontal spacing convention used elsewhere in this file and library

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheet.kt:320-333` (Cancel/Confirm `Row`), `:344-372` (`ProposedItemRow`'s `Row`)
**Issue:** Neither the Cancel/Confirm action `Row` nor `ProposedItemRow`'s content `Row` sets a
`horizontalArrangement` (e.g. `Arrangement.spacedBy(...)` or `Arrangement.End`). This same file
already establishes the convention of an explicit arrangement for multi-child rows
(`BatchResultsList`'s `Column` uses `Arrangement.spacedBy(Dimens.HairlineSpacing)`), and the broader
component library consistently spaces action-button rows and item rows explicitly (e.g.
`BulkCreatePopup.kt:166` — `Arrangement.End` for a confirm/cancel row; `ChipBar.kt:110`,
`CardTagRow.kt:91` — `Arrangement.spacedBy(8.dp)` for row content). Without it, the Cancel/Confirm
`TextButton`s and the title-column/trailing-icon pairing in `ProposedItemRow` rely entirely on each
child's own incidental padding for separation, which is inconsistent with the rest of this component
and may render visually cramped compared to sibling composables.
**Fix:**
```kotlin
Row(
    modifier = Modifier.padding(top = Dimens.ContentSpacing),
    horizontalArrangement = Arrangement.spacedBy(Dimens.ContentSpacing, Alignment.End)
) { /* Cancel, Confirm */ }
```
and similarly add `horizontalArrangement = Arrangement.spacedBy(Dimens.ContentSpacing)` to
`ProposedItemRow`'s `Row`.

### WR-05: `VoiceOutcomeUiState`'s interface-level KDoc is now factually stale ("exactly two arms") in the same file that defines the third arm

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/model/VoiceOutcomeUiState.kt:13-16`
**Issue:** Per 12-01-SUMMARY.md's documented deviation, the executor reverted a KDoc reword here to
satisfy the repo's DS-05 append-only pre-commit guard. The result left behind is self-contradictory:
the class doc still reads "Exactly two arms this phase (D-02): `[Success]` and `[Failure]`. ... Phase
12 adds a `NeedsConfirmation` third arm purely additively ... Do not guess at or pre-model that third
arm here" — phrased as forward-looking guidance not to pre-build the arm, inside the very file that
now defines `NeedsConfirmation` fifty lines below. Any reader of this public, JitPack-shipped KDoc
(including IDE quick-docs/Javadoc for consumers) will be told there are "exactly two arms" in a file
that has three. `OutcomeSheet.kt`'s own top doc has the same partial staleness (its first paragraph's
arm enumeration still lists only `Success`/`Failure`), though it is at least followed by a correcting
"As of VOUT-04..." sentence; `VoiceOutcomeUiState.kt`'s doc has no such correction.
**Fix:** Add a new, purely additive sentence/paragraph updating the arm count (rather than editing
the existing stale sentence in place, to keep satisfying DS-05), e.g.: "Update (VOUT-04): a third
arm, `NeedsConfirmation`, now exists below — see its own KDoc; this paragraph's 'exactly two arms'
describes the Phase 11 baseline only."

## Info

### IN-01: Redundant explicit default value in the batch fixture

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/VoiceCommandFamilyScreen.kt:417`
**Issue:** `fixtureOutcomeNeedsConfirmationBatch` passes `selectionMode = SelectionMode.AllOrNothing`
explicitly, but that is already `NeedsConfirmation.selectionMode`'s default value — the line adds no
behavior, just noise.
**Fix:** Remove the explicit assignment (or, if its purpose is purely to make the fixture
self-documenting for gallery readers, leave as-is and ignore this note).

---

_Reviewed: 2026-09-30T00:00:00Z_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
