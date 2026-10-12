---
phase: 21-receipt-row-pill-selector-sheet-shape
reviewed: 2026-10-12T00:00:00Z
depth: standard
files_reviewed: 22
files_reviewed_list:
  - api.txt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/ReceiptRow.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/SegmentedOptionSelector.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/SheetScaffold.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/CardsFamilyScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/PickersFamilyScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/SheetsFamilyScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/theme/Dimens.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/ReceiptRowLineHeightTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/ReceiptRowLongLabelTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/ReceiptRowMotionTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/ReceiptRowSourceContractTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/ReceiptRowTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/SegmentedOptionMaterialParitySourceContractTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/SegmentedOptionSelectorPillTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/SheetScaffoldShapeProbeTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/SheetScaffoldShapeTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/ToneFoundationInvariantsTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ReceiptRowRegistryRenderTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/SegmentedOptionSelectorGalleryTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/theme/DimensTest.kt
findings:
  critical: 0
  warning: 2
  info: 5
  total: 7
status: issues_found
---

# Phase 21: Code Review Report

**Reviewed:** 2026-10-12
**Depth:** standard
**Files Reviewed:** 22
**Status:** issues_found

## Summary

Reviewed `ReceiptRow`, the Pill branch and per-option disable of `SegmentedOptionSelector`, the `SheetScaffold` `shape` parameter, the gallery wiring, `Dimens` tokens, `api.txt` and the new tests. Read only; I did not run gradle.

No blockers. The hub invariants hold:
- Source compatibility: `disabledOptions` and `style` are appended after `disabledReason`. `shape` sits before the trailing `content` lambda. No overloads or shims were added.
- One-way dependency: imports are limited to AndroidX, Kotlin and the hub.
- Registry drift guard: `ReceiptRow` is registered in the Cards family. `SegmentedOptionStyle` is an enum, so it is not a registry candidate.
- `api.txt` matches the new signatures.
- The Material block is pinned verbatim by a parity test, and the G-15-1 helpers are untouched.

The remaining issues are an accessibility regression carried into new code and a state that is conveyed only visually. There are also a few documentation and robustness nits.

## Warnings

### WR-01: Pill segments announce "selected" twice, and the strings can never be localized

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/SegmentedOptionSelector.kt:227-229`
**Issue:** The Pill segment sets `contentDescription = "$label, ${if (selected) "selected" else "not selected"}"` on a node that is already `selectable(selected = ..., role = Role.RadioButton)`.
- TalkBack reads the selection state twice: once from the description and once from the selected state/role (for example "Eggs, selected, radio button, selected").
- The two English literals cannot be localized by callers.
- The Material branch has the same pattern, and API.md documents the strings as intentionally English. But the Pill branch is new code that deliberately copies it, and v2.5.0 shipped voice UI localization and a11y work. The tests also address segments by this description, which bakes the defect in.

**Fix:** Leave the Material branch alone, since it is pinned byte-for-byte. For Pill, drop the `contentDescription` override and let `selectable` expose selected state and role. Keep `Text(label)` as the accessible name and change the tests to find segments by text plus `isSelected`. If the shared English strings must stay, add a `selectedLabel`/`notSelectedLabel` parameter pair (appended with defaults) so callers can localize them.

### WR-02: `dimmed` state is visual only, and the dim also covers `trailingAction`

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ReceiptRow.kt:207-211` (also `:160` KDoc, `:183`)
**Issue:** `dimmed` is expressed only through alpha and strikethrough. Nothing is added to the semantics, so a screen-reader user hears no difference between an "undone" row and a normal one. The `graphicsLayer { alpha }` is also applied to the outer `Row`, which wraps `trailingAction` as well as the summary. The KDoc says it "dims the summary line". A caller who passes both `dimmed = true` and a `trailingAction` gets a 38% action that is still fully clickable, so it looks disabled but is not. The gallery's "Disabled" state cell has the same shape: it passes `onClick = {}`, so the row looks disabled but stays interactive.
**Fix:** Move the `graphicsLayer` onto the `ReceiptRowSummary` modifier (or its `Row`) so only the summary dims, as documented. Expose the state to accessibility, for example a `stateDescription` suffix, or add an appended `dimmedStateLabel: String? = null` parameter. Document that `dimmed` is not an interaction block. Rename or comment the registry cell, since "Disabled" is misleading for a still-clickable row.

## Info

### IN-01: Per-option disabled `enabled` shadows the parameter and pins a compiler warning

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/SegmentedOptionSelector.kt:99`
**Issue:** `val enabled = enabled && index !in disabledOptions` shadows the function parameter inside the loop. Kotlin emits a "name shadowed" warning, and a later edit that reads `enabled` below the loop could pick up the wrong one. `SegmentedOptionMaterialParitySourceContractTest` pins this exact line, so renaming it means changing the test too.
**Fix:** Rename it to `segmentEnabled`, as the Pill branch does, and update `EXPECTED_MATERIAL_BLOCK` in the parity test to match.

### IN-02: Misplaced KDoc paragraph and stale `Dimens` header

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/SegmentedOptionSelector.kt:68-69` and `src/main/java/io/github/ygaray/yahirandroidtaste/theme/Dimens.kt:16-31`
**Issue:**
- The "Long localized labels truncate..." paragraph sits after the `@param` tags, so KDoc attaches it to `@param style`.
- The `Dimens` header "Nested groups" list was not updated with `SegmentedPill` and `ReceiptRow`.

**Fix:** Move the paragraph above the `@param` block. Add the two groups to the header list.

### IN-03: `supportingContent` color is not controlled

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ReceiptRow.kt:342-344`
**Issue:** Only `ProvideTextStyle` is applied, so a plain `Text` in the supporting line takes `LocalContentColor`. Inside a `Surface`, `Card` or sheet that is `onSurface`. In a bare container it can be the Compose default (black), which is unreadable on a dark theme. The gallery demo `ReceiptRowSupportingLine` relies on this.
**Fix:** Wrap the slot in `CompositionLocalProvider(LocalContentColor provides scheme.onSurfaceVariant)` (or document the contract), so the third line is as theme-safe as the title and subtitle.

### IN-04: Pill outline is not dimmed when the whole control is disabled

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/SegmentedOptionSelector.kt:203-210`
**Issue:** With `enabled = false` both segments drop to 38% alpha, but the container's 1dp `outlineVariant` border stays at full opacity. The control reads as half-dimmed.
**Fix:** Apply `alpha` to the border as well. The simplest way is to make the `Row` itself `alpha(if (enabled) 1f else DISABLED_CONTENT_ALPHA)` and keep the per-segment alpha only for `disabledOptions`.

### IN-05: Source-contract tests pin formatting and are brittle

**File:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/SegmentedOptionMaterialParitySourceContractTest.kt:83-99` and `ReceiptRowSourceContractTest.kt`
**Issue:** The parity test compares the whitespace-stripped source to a literal that includes trailing commas, so any benign edit to the Material block fails it. That is intended for the byte-unchanged goal, but the failure message gives no hint how to refresh the literal. Several `ReceiptRowSourceContractTest` assertions are also count thresholds, such as at least 5 `receiptRowLine()` uses and at least 6 tweens. Those pass or fail on refactors that do not change behavior.
**Fix:** Add a short "to update" note above `EXPECTED_MATERIAL_BLOCK`. Consider replacing the count thresholds with behavior assertions the Robolectric tests already cover.

---

_Reviewed: 2026-10-12_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
