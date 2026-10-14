---
phase: 22-outcomesheet-receipt-style
reviewed: 2026-10-12T00:00:00Z
depth: standard
files_reviewed: 30
files_reviewed_list:
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheet.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetDefaults.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceipt.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/FailureActionUiModel.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/FailureTone.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/HandledByUiModel.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/OutcomeBadge.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/SheetActionUiModel.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/VoiceOutcomeUiState.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/theme/Dimens.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetClassicDeltasTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetClassicParitySourceContractTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetClassicParityTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetDefaultsTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetFixtures.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetLiveRegionTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceiptBarGeometryTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceiptFailureTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceiptFrameTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceiptLongLabelTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceiptNeedsConfirmationTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceiptSourceContractTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceiptSuccessTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceiptTagsTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetSourceCompatV251Test.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/ToneFoundationInvariantsTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/model/OutcomeReceiptModelsTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/model/VoiceModelLabelDefaultsTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/theme/DimensTest.kt
  - api.txt
findings:
  critical: 0
  warning: 2
  info: 6
  total: 8
status: resolved
---

# Phase 22: Code Review Report

**Reviewed:** 2026-10-12
**Depth:** standard
**Files Reviewed:** 30
**Status:** issues_found

## Summary

Static review only (Gradle not run). Reviewed the Receipt renderer, the Classic deltas, the new and
widened models, `Dimens.OutcomeReceipt`, `api.txt` and the Phase 22 tests against `22-CONTEXT.md`
and the library invariants in `CLAUDE.md`.

What holds up:
- The Classic path is intact. The `style == Receipt` guard returns before the legacy `SheetScaffold`
  line, and the Classic functions are pinned by SHA-256 of their v2.5.1 text.
- The legacy-arity `copy` shims on `Failure`, `HandledByUiModel` and `FailureActionUiModel` do
  carry the new fields forward. The compiler-generated full-arity `copy` defaults every omitted
  argument to `this.<field>`, and `OutcomeReceiptModelsTest` asserts this.
- No new public top-level `@Composable`, so the `ComponentRegistry` drift guard is unaffected.
  Everything in `OutcomeSheetReceipt.kt` is `internal` or `private`.
- No consumer imports, no secrets, no `@HiltAndroidApp`, no `testTagsAsResourceId`.
- Source-compat additions follow the append-with-defaults rule. The `NeedsConfirmation` mid-list
  insertion is the documented D-02 exception.

No blockers found. Two warnings (one layout, one accessibility-verification gap) and six info items follow.

## Warnings

### WR-01: Equal-weight action slots cap each button at 50% of the row, so one long label wraps even when its sibling is short

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceipt.kt:498-507`
**Issue:** `ReceiptActionRow` wraps both slots in `Box(Modifier.weight(1f, fill = false))`. Compose's
`Row` hands each weighted child `max = remainingWidth * weight / weightSum` and never redistributes
space the other child leaves unused. At 360dp the row has 328dp, so each button is capped at about
164dp no matter how small its sibling is. A short Cancel ("Abbrechen", about 90dp) next to a long
German confirm ("Alle ausgewählten Einträge endgültig löschen") makes the Filled confirm wrap to 3
lines inside a 164dp slot while about 70dp stays empty. That is the tall, narrow pill the P7 escape
hatch was meant to avoid. `OutcomeSheetReceiptLongLabelTest` only uses long labels on both sides,
so it cannot see this. It asserts "inside the window and not ellipsized", which wrapping satisfies.
Success, Failure and NeedsConfirmation all share this row.
**Fix:** Let the slots take the width they need and wrap only when the two do not fit. For example,
put the slots in a `FlowRow(horizontalArrangement = Arrangement.SpaceBetween)` with
`maxItemsInEachRow = 2`, or use a small custom `Layout` that measures both at intrinsic width first
and only then splits the remainder. Add a test with a short start label and a long end label that
asserts the end button's line count is no more than 2.

### WR-02: The assertive live region is on an unmerged container and the contract test forbids the usual mitigation

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheet.kt:195-214` (and `OutcomeSheetClassicParitySourceContractTest.kt:~140`)
**Issue:** `liveRegion = Assertive` sits on the `Surface`, whose text lives in a child `Text` node.
`OutcomeSheetClassicParitySourceContractTest` asserts `assertFalse("the live region must not merge descendants", ...)`.
So the only node that defines the live region has no text or `contentDescription` of its own. What
TalkBack speaks for a text-less live-region container, and whether it announces a node that appears
already carrying the region (as opposed to one whose content changes), depends on platform and
TalkBack behavior that no Robolectric test can show. `OutcomeSheetLiveRegionTest` proves only that
the key is defined on exactly one node. The KDoc defers the read-out to a Gate-2 item (A9). The risk
is that VXR-01 passes every automated check yet announces nothing on device.
**Fix:** Keep A9 as a hard Gate-2 item, with explicit pass/fail wording for "refused surface is spoken
on appearance". If it fails, put the region on the same node that carries the text, for example
`Modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Assertive }` on the
`Surface`, and update the `mergeDescendants` pin deliberately. The undo block is shared, so the
change lands in both styles.

## Info

### IN-01: Success actions get 32dp below them (acknowledged in 22-04-SUMMARY, still unresolved)

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceipt.kt:98-103, 333-336`
**Issue:** `receiptHasBar` is false for Success, so the scroll body adds `HorizontalPadding` (16dp) at
the bottom. `ReceiptSuccessActions` then adds `SuccessActionsBottomPadding` (16dp), giving 32dp under
the actions row. Nothing in D-08 asks for that. Pinned-bar states use 16dp.
**Fix:** Drop `SuccessActionsBottomPadding`, or give the scroll body `bottom = 0.dp` when Success has
actions. Resolve it at the Phase 24 screencap review and then update `DimensTest`.

### IN-02: The status badge has no text alternative, so Success vs Reverted is conveyed by color and glyph only

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceipt.kt:187-205`
**Issue:** `Icon(contentDescription = null)` inside an unlabeled `Box`. For Failure the reason text
carries the meaning. For Success, "Logged 2 of 3" with a Reverted badge reads identically to a
plain Success for a screen-reader user. `OutcomeBadge` has no label field to supply a description.
**Fix:** Add an optional caller-localized `badgeContentDescription: String? = null` (appended with a
default) to `Success` and `Failure`, or document that the title must state the status. Apply
`semantics { contentDescription = ... }` on the badge `Box` when it is set.

### IN-03: `Failure.title` KDoc promises behavior the code does not have

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/model/VoiceOutcomeUiState.kt:86-87`
**Issue:** "`null` lets the sheet fall back to its default title behavior." There is no default title.
With `title == null && badge == null`, `ReceiptTitleRow` renders nothing.
**Fix:** Reword to "`null` renders no title text (a badge alone still renders)".

### IN-04: The generic legacy-copy drift guard was not extended with the new fields

**File:** `src/test/java/io/github/ygaray/yahirandroidtaste/model/VoiceModelLabelDefaultsTest.kt:~296-330`
**Issue:** `legacy-arity copy of every voice model carries every field forward` is documented as "every
field non-default". Its `HandledByUiModel` and `Failure` fixtures still use only the pre-Phase-22
fields. `prefixLabel`/`onDevice`, `title`/`transcript`/`tone`/`tip`/`secondaryAction`/`badge`/`undo` and
`FailureActionUiModel.icon` stay at defaults there, so a future shim that hard-codes those defaults
would not trip this guard. `OutcomeReceiptModelsTest` has targeted per-model checks, which is why
this is only Info. Two guards now cover the same property by different routes.
**Fix:** Populate every new field in the `HandledByUiModel`, `FailureActionUiModel` and `Failure`
fixtures of `assertLegacyCopyCarriesEveryField`, or delete the duplicate coverage.

### IN-05: Several source-text contract tests cannot catch the regression they name

**File:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceiptSourceContractTest.kt:~70-82` and `OutcomeSheetReceiptTagsTest.kt:~203-208`
**Issue:**
- `the bar is a sibling placed after the weighted scroll body` only checks `indexOf` order of three
  strings. Moving `ReceiptNeedsConfirmationBar(` inside the scrolling `Column` (after the `weight`
  and `verticalScroll` text) would still pass. Only the behavioral 240dp starvation tests protect
  the v2.4.1 invariant.
- `failure warn carries the same tags as error and differs only in tone` asserts only tag counts,
  not the tone, despite the name. The tone is proven in `OutcomeSheetReceiptFailureTest`.
- The tag-literal presence checks pass if a tag string sits in a comment, an unused constant or a
  dead branch.
**Fix:** Rename the warn tags test to match what it asserts. For the sibling test, assert structure
at runtime (the bar's `positionInRoot.y` is at or below the scroll body's bottom, and the bar is not
a descendant of the scrollable node). Treat the string pins as a secondary guard only.

### IN-06: Public docs and gallery are not updated for the new public surface

**File:** `API.md`, `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/VoiceCommandFamilyScreen.kt`
**Issue:** `OutcomeSheetStyle`, `OutcomeSheetColors`, `OutcomeSheetDefaults`, `OutcomeBadge`, `FailureTone`
and `SheetActionUiModel` appear in `api.txt` but not in `API.md`. `VoiceCommandFamilyScreen` has no
Receipt-style sample. This may be planned for a later phase (Phase 24 is referenced for screencap
review), so confirm that before tagging. If v2.6.0 is cut without them, the agent-facing "reach for an
existing widget" catalog is stale for the main new feature.
**Fix:** Add an `API.md` section and a Receipt gallery state (Confirm, Success, Failure Error/Warn)
before the v2.6.0 tag, or record the deferral in the roadmap.

---

_Reviewed: 2026-10-12_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
