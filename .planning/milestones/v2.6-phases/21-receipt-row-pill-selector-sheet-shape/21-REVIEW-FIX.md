---
phase: 21-receipt-row-pill-selector-sheet-shape
fixed_at: 2026-10-12T00:00:00Z
review_path: .planning/phases/21-receipt-row-pill-selector-sheet-shape/21-REVIEW.md
iteration: 1
findings_in_scope: 7
fixed: 5
skipped: 2
status: resolved
---

# Phase 21: Code Review Fix Report

**Fixed at:** 2026-10-12
**Source review:** .planning/phases/21-receipt-row-pill-selector-sheet-shape/21-REVIEW.md
**Iteration:** 1

**Summary:**
- Findings in scope: 7 (fix_scope = all)
- Fixed: 5
- Skipped: 2 (both documented acceptable-skips, each conflicting with a locked decision)

**Verification:** `./gradlew testDebugUnitTest detekt apiCheck` ran once, in the main checkout (no
worktree, per the session constraints), after all fixes. Result: BUILD SUCCESSFUL, detekt at zero
baseline with 0 code smells, `apiCheck` and `metalavaCheckCompatibilityRelease` green. No public
API changed, so `api.txt` was not regenerated. Per-fix verification was Tier 1 (re-read of the diff)
only, because Kotlin has no cheap per-file syntax check; the single full run above is the compile
and test proof.

Commits that touch existing source lines (Lane 2) were made with `HUB_LANE_OVERRIDE=2`.

## Fixed Issues

### WR-02: `dimmed` state is visual only, and the dim also covers `trailingAction`

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ReceiptRow.kt`, `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/CardsFamilyScreen.kt`
**Commit:** 51d1538
**Applied fix:**
- Moved the dim `graphicsLayer` from the outer `Row` onto the `ReceiptRowSummary` modifier, so only the summary dims and a `trailingAction` stays at full opacity, as the KDoc already said.
- Rewrote the `dimmed` KDoc: it is visual-only, it does not block `onClick`, and callers who need to tell screen-reader users must say so in their own label or text.
- The gallery's "Disabled" state cell no longer passes `onClick = {}` (so the demo row is really non-interactive), with a comment that the fixed state-axis name stands in for `dimmed`.

**Judgment call (partial):** the review also suggested exposing a `stateDescription` or adding a `dimmedStateLabel` parameter. That needs a new localizable parameter, which would change the signature that D-01 locks to sketch Y-2, so I documented the limitation instead of adding the parameter. The consumer spec (76-UI-SPEC) has an undone row use `dimmed = true` with no trailing action, so dimming the summary only does not affect that use. Worth a human glance since it is a behavior change in a new component: the `trailingAction` no longer dims.

### IN-02: Misplaced KDoc paragraph and stale `Dimens` header

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/SegmentedOptionSelector.kt`, `src/main/java/io/github/ygaray/yahirandroidtaste/theme/Dimens.kt`
**Commit:** f306103
**Applied fix:** Moved the "Long localized labels truncate..." paragraph above the `@param` block, and added `SegmentedPill` and `ReceiptRow` to the `Dimens` "Nested groups" list. Comments only, so the parity source-contract test (which strips comments) is unaffected.

### IN-03: `supportingContent` color is not controlled

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ReceiptRow.kt`
**Commit:** e224ed9
**Applied fix:** Wrapped the supporting slot in `CompositionLocalProvider(LocalContentColor provides palette.caption)` (which is `onSurfaceVariant`), and noted the default color in the `supportingContent` KDoc. A caller can still set its own color.

### IN-04: Pill outline is not dimmed when the whole control is disabled

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/SegmentedOptionSelector.kt`
**Commit:** 86ccb86
**Applied fix:** The Pill `Row` now carries `alpha(if (enabled) 1f else DISABLED_CONTENT_ALPHA)`, so the outline dims with the segments. The per-segment alpha now applies only for a per-option disable while the control is enabled (`enabled && index in disabledOptions`), so a disabled control does not dim twice (38% of 38%). The `disabledReason` caption sits outside the `Row` and stays fully visible. The Material branch is untouched. Worth a human glance at the disabled Pill in the gallery.

### IN-05: Source-contract tests pin formatting and are brittle

**Files modified:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/SegmentedOptionMaterialParitySourceContractTest.kt`
**Commit:** 5ffe6dd
**Applied fix:** Added a "To update" note above `EXPECTED_MATERIAL_BLOCK` explaining how to refresh the literal after an approved Material-block edit. KDoc comment only; the assertions are unchanged.

**Judgment call (partial):** I did not replace the count thresholds in `ReceiptRowSourceContractTest` (at least 5 `receiptRowLine()` uses, at least 6 tweens). They are cheap tripwires, the Robolectric tests already cover the behavior, and loosening or rewriting them is a judgment about test intent the review itself only says to "consider".

## Skipped Issues

### WR-01: Pill segments announce "selected" twice, and the strings can never be localized

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/SegmentedOptionSelector.kt:227-229`
**Reason:** Documented acceptable-skip. Both remedies conflict with locked decisions or hub conventions:
- Adding `selectedLabel`/`notSelectedLabel` parameters changes the Y-11 signature, which D-01 locks "exactly" as sketched and as signed off by both consumers.
- Dropping the `contentDescription` override in Pill would make the two styles address differently. The Pill test header and the gallery tests deliberately rely on one addressing scheme (`"$label, selected"` / `"$label, not selected"`) for both styles. Consumers' tests already find the Material segments that way (for example `ApproachLadderCardGalleryDemoTest`), and the plan states the Pill segment semantics keep the Material strings.
- API.md documents these two English strings as a known, intentionally non-localized residual of the hub.
**Original issue:** Pill sets `contentDescription` on a node that already has `selectable(selected, Role.RadioButton)`, so TalkBack can read the selection twice, and the English literals cannot be localized.
**Follow-up for a human:** a hub-wide change to the selected/not-selected semantics (both styles, with a localizable label pair) belongs in a separate, owner-approved signature change, not this phase.

### IN-01: Per-option disabled `enabled` shadows the parameter and pins a compiler warning

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/SegmentedOptionSelector.kt:99`
**Reason:** Documented acceptable-skip. The shadow is deliberate (the line carries a comment saying so): D-07 and the P3 parity proof require the Material block to equal the v2.5.1 text plus exactly one inserted line, so the per-segment `SegmentedButton(... enabled = enabled ...)` call stays byte-identical to v2.5.1. Renaming it would alter that call and force an edit to `EXPECTED_MATERIAL_BLOCK`, weakening the very guard the shadow exists to satisfy. The detekt run is green, so the shadowing warning is not a gate failure.
**Original issue:** `val enabled = enabled && index !in disabledOptions` shadows the function parameter.

---

_Fixed: 2026-10-12_
_Fixer: Claude (gsd-code-fixer)_
_Iteration: 1_
