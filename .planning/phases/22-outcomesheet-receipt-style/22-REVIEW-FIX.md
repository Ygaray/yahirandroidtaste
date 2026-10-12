---
phase: 22-outcomesheet-receipt-style
fixed_at: 2026-10-12T00:00:00Z
review_path: .planning/phases/22-outcomesheet-receipt-style/22-REVIEW.md
iteration: 1
findings_in_scope: 8
fixed: 6
skipped: 2
status: resolved
---

# Phase 22: Code Review Fix Report

**Fixed at:** 2026-10-12
**Source review:** .planning/phases/22-outcomesheet-receipt-style/22-REVIEW.md
**Iteration:** 1

**Summary:**
- Findings in scope: 8 (fix_scope: all)
- Fixed: 6
- Skipped: 2 (both documented acceptable-skips; no open blocker, critical or high remains)

**Verification ran in the main checkout** (`workflow`: operator-directed, branch `main`, no worktree).
Final gate: `./gradlew apiDump testDebugUnitTest detekt apiCheck --console=plain -q` green, then
`apiCheck testDebugUnitTest detekt` re-run green (exit 0). Detekt at zero baseline, no baseline touched.
`apiDump` produced no `api.txt` change (no public surface changed).

## Fixed Issues

### WR-01: Equal-weight action slots cap each button at 50% of the row

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceipt.kt`, `src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceiptLongLabelTest.kt`, `src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceiptSourceContractTest.kt`
**Commit:** 0b740b3
**Status:** fixed: layout logic, covered by Robolectric geometry tests; a Gate-1 look at a long German bar on the TESTER is still worthwhile.
**Applied fix:** Replaced the two `weight(1f, fill = false)` boxes in `ReceiptActionRow` with a small custom `Layout`. Natural widths come from `maxIntrinsicWidth` (a `Measurable` may only be measured once per pass), then the new internal pure function `receiptActionSlotWidths` decides the split. If both fit, each keeps its natural width. Otherwise an action that needs no more than half the row keeps its natural width and the other takes the rest. If both need more than half, each gets half. Start is placed at the start edge and end at the end edge (mirrored for RTL), and a lone end action stays at the end edge. A short sibling no longer makes a long label wrap early.
Tests added before the fix (two failed with confirm/cancel capped at 164px of a 328px row, the edge test passed):
- short Cancel + long German Confirm: confirm gets more than half the row and wraps to at most 2 lines
- short Confirm + long Cancel (mirror case)
- two short labels still sit on the 16dp start edge and the 16dp end edge
The 16dp/368dp edge tests and the long-German-both-sides tests stay green. `OutcomeSheetReceiptSourceContractTest` pinned the old implementation (two `weight(1f,fill=false)`, `barArrangement`, `SpaceBetween` text); it now pins the measured `Layout` and `receiptActionSlotWidths` and forbids `weight(` and `Spacer(` in the row. This is a deliberate update of the pin.

### IN-01: Success actions get 32dp below them

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceipt.kt`, `src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceiptSuccessTest.kt`, `src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceiptSourceContractTest.kt`
**Commit:** d19931b
**Applied fix:** The scroll body now drops its 16dp bottom padding when a Success renders an actions row (`receiptSuccessHasActions`), so the space under the row is exactly the row's own `SuccessActionsBottomPadding` (16dp). The token and `DimensTest` are unchanged. A Success without actions keeps the 16dp body padding. New test measured 32dp before the fix and 16dp after. The existing `barFollows` source pin is kept and a pin for the new predicate was added.

### IN-02: The status badge has no text alternative

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/model/VoiceOutcomeUiState.kt`, `src/main/java/io/github/ygaray/yahirandroidtaste/model/OutcomeBadge.kt`
**Commit:** 958806b
**Applied fix:** Took the review's second option (document, do not add API). The KDoc on `Success.badge`, `Failure.badge` and `OutcomeBadge` now states the badge is decorative and that the title or reason text must state the status in words. A new `badgeContentDescription` parameter was deliberately not added: it widens the public data-class surface (copy shims, `api.txt`) in an unsupervised run, and can be taken as a separate, owner-approved addition.

### IN-03: `Failure.title` KDoc promises behavior the code does not have

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/model/VoiceOutcomeUiState.kt`
**Commit:** 6b57989
**Applied fix:** Reworded to "`null` renders no title text (a badge alone still renders)".

### IN-04: The generic legacy-copy drift guard was not extended with the new fields

**Files modified:** `src/test/java/io/github/ygaray/yahirandroidtaste/model/VoiceModelLabelDefaultsTest.kt`
**Commit:** 561b6d3
**Applied fix:** `legacy-arity copy of every voice model carries every field forward` now populates every Phase 22 field: `HandledByUiModel.prefixLabel`/`onDevice`, `FailureActionUiModel.icon`, and `Failure.title`/`transcript`/`tone`/`tip`/`secondaryAction`/`badge`/`undo` (plus the handled-by and action fields nested in it).

### IN-05: Several source-text contract tests cannot catch the regression they name

**Files modified:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceiptFrameTest.kt`, `src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceiptTagsTest.kt`
**Commit:** d936aa4
**Applied fix:** Renamed the warn tags test to `failure warn carries the same tags as error` (it only asserts tags; tone is proven in `OutcomeSheetReceiptFailureTest`). Added a runtime structure test: with 30 items the body scrolls, the `outcome_sheet_action_bar` is not a descendant of the scrollable node (with a positive control that the title is), and the bar's top is at or below the scroll body's bottom. The string pins remain as a secondary guard. The "tag literal in a comment or dead branch" point is covered by the existing runtime tag tests and was not changed further.

## Skipped Issues

### WR-02: The assertive live region is on an unmerged container and the contract test forbids the usual mitigation

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheet.kt:195-214` (and `OutcomeSheetClassicParitySourceContractTest.kt:~140`)
**Reason:** Documented acceptable-skip. Whether TalkBack speaks a text-less assertive live-region container on appearance is platform and TalkBack behavior that no Robolectric test can show; it is already a Gate-2 hardware read-out (A9). The suggested mitigation (`semantics(mergeDescendants = true)` on the `Surface`) is conditional on that read-out failing, and applying it blind would merge the undo and action buttons into one node in BOTH styles, which could break correct-by-design behavior (separately focusable undo controls) and reverses a deliberate `mergeDescendants` pin. No safe code change resolves it without device evidence. Recommended for the Gate-2 owner: keep A9 as a hard item with explicit pass/fail wording ("refused surface is spoken on appearance"). If it fails, move the region to the text node or merge as in the review's Fix, and update the pin deliberately.
**Original issue:** `liveRegion = Assertive` sits on the `Surface`, whose text lives in a child `Text` node; the contract test forbids merging, so VXR-01 could pass every automated check yet announce nothing on device.

### IN-06: Public docs and gallery are not updated for the new public surface

**File:** `API.md`, `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/VoiceCommandFamilyScreen.kt`
**Reason:** Documented acceptable-skip: deferred to Phase 24 by the roadmap. Phase 24 success criterion 3 owns the `API.md` update (including the stable test-tags section) and `INTEGRATION.md`/KDoc coverage, and criterion 4 owns the Explorer demos (Receipt confirm, Success, Failure error/warn). Both are gated before the `v2.6.0` cut, so no stale catalog ships. Writing them here would duplicate and pre-empt Phase 24 planning.
**Original issue:** `OutcomeSheetStyle`, `OutcomeSheetColors`, `OutcomeSheetDefaults`, `OutcomeBadge`, `FailureTone` and `SheetActionUiModel` are in `api.txt` but not in `API.md`, and the gallery has no Receipt sample.

---

_Fixed: 2026-10-12_
_Fixer: Claude (gsd-code-fixer)_
_Iteration: 1_
