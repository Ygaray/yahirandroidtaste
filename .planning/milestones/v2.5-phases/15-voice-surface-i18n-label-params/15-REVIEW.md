---
phase: 15-voice-surface-i18n-label-params
reviewed: 2026-10-11T00:00:00Z
depth: standard
scope: plan 15-04 gap closure (G-15-1), git diff ce3516c..HEAD
files_reviewed: 7
files_reviewed_list:
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/SegmentedOptionSelector.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/VoiceCommandFamilyScreen.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCardLongLabelTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/SegmentedOptionSelectorTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ApproachLadderCardGalleryDemoTest.kt
  - API.md
findings:
  critical: 1
  warning: 4
  info: 3
  total: 8
status: resolved
---

# Phase 15: Code Review Report (gap closure 15-04, G-15-1)

**Reviewed:** 2026-10-11
**Depth:** standard
**Files Reviewed:** 7
**Status:** issues_found

## Prior review

The earlier `15-REVIEW.md` (2026-10-05) covered plans 15-01..03 across 17 files and found
0 critical, 4 warning and 2 info issues. All of them were handled in `15-REVIEW-FIX.md`
(status `resolved`): 4 were fixed, and 2 were skipped because each needed new public API outside
VI18N-01..04. This report replaces that one and covers only the 15-04 increment.

## Summary

The German long-label fix works. At 360dp the Cloud name stays visible, the affordances stay at
2 lines or fewer, segment labels stay inside their segment, and RTL mirrors correctly. But the
fixed 60% cap is too tight for the **English defaults**. At every common width, "Needs network" now
drops to a second line under "Unavailable"/"Capped". That breaks the plan's must-have truth #3 and
the API.md claim that short labels "render exactly as in v2.5.0". None of the tests catches it: the
English guard test checks each Text's own line count, not whether the three Texts share a line.
Separately, the new gallery test passes against the **pre-fix** code, so it guards nothing.

**How the evidence was gathered.** I ran throwaway probe tests (Robolectric, `GraphicsMode.NATIVE`)
in an isolated copy of HEAD made with `git archive` under the session scratchpad. I swapped in the
`ce3516c` versions of the two components to compare before and after. The repo checkout was not
touched (git status is unchanged).

| Probe (Cloud row: disabled + capped + needs-network) | v2.5.0 (`ce3516c`) | HEAD |
|---|---|---|
| English, w360dp, card fillMaxWidth | all 3 affordances at y=38 (one line) | Unavailable/Capped y=30, **Needs network y=45** (2nd line) |
| English, w360dp, gallery padding (16dp) | one line, y=38 | **2 lines** (y=30 / y=45) |
| English, w320dp | one line, y=38 | **2 lines** (y=30 / y=45) |
| English, w360dp, fontScale 1.3 | one line, y=37 | **2 lines** (y=27 / y=45) |
| German, w360dp, RTL | n/a | Cloud x=203..344, affordances x=16..195, no overlap (OK) |
| Router off/on selector, fontScale 2.0, 296dp wide | 1 line each, segment h=46 | **2 lines each, segment h=84** |

## Narrative Findings (AI reviewer)

## Critical Issues

### CR-01: The 60% cap stacks the English default affordances onto two lines (v2.5.0 regression)

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt:263-267, 309`; claim in `API.md:213-214`
**Issue:**
- `AFFORDANCE_MAX_WIDTH_FRACTION = 0.6f` is a fixed share of the row. It does not depend on what the name actually needs.
- At w360dp the row is 328dp wide, so the cap is 196.8dp. The three English labels need 198dp: 63 + 4 + 41 + 4 + (82 + 4 start padding).
- The FlowRow therefore wraps "Needs network" to a second line, even though "Cloud" plus all three labels fit on one line with about 126dp to spare (v2.5.0 gave the name 126dp).
- The same happens at 320dp, in the gallery cell (296dp row) and at fontScale 1.3. So every English consumer gets a visibly different, taller affordance block on any rung that carries all three affordances.
- This contradicts plan truth #3 ("affordances trail on the right on one line ... exactly as in v2.5.0") and API.md line 214.
- The design is a cliff: a 2dp shortfall switches the layout from one line to stacked, and the name ends up with 216dp it doesn't need.

**Fix:** Cap the affordances by what the name actually needs, not by a fixed fraction. Give them the
whole remaining row, and only fall back to the 40% floor when the name itself would be squeezed.
`CapControl`'s signature can stay as it is: emit one private `Layout` child with `Modifier.weight(1f)`.
```kotlin
// inside RungRow's RowScope content (replaces the bare name Text + FlowRow):
Layout(
    contents = listOf({ NameText() }, { if (hasAffordance) AffordanceFlowRow() }),
    modifier = Modifier.weight(1f)
) { (nameMs, affMs), c ->
    val gap = 4.dp.roundToPx()
    val name = nameMs.single()
    val nameNeed = name.maxIntrinsicWidth(c.maxHeight)
    val nameFloor = minOf(nameNeed, (c.maxWidth * (1f - AFFORDANCE_MAX_WIDTH_FRACTION)).toInt())
    val aff = affMs.firstOrNull()?.measure(
        Constraints(maxWidth = (c.maxWidth - nameFloor - gap).coerceAtLeast(0))
    )
    val nameW = c.maxWidth - (aff?.let { it.width + gap } ?: 0)
    val nameP = name.measure(Constraints.fixedWidth(nameW.coerceAtLeast(0)))
    val h = maxOf(nameP.height, aff?.height ?: 0)
    layout(c.maxWidth, h) {
        nameP.placeRelative(0, (h - nameP.height) / 2)
        aff?.placeRelative(c.maxWidth - aff.width, (h - aff.height) / 2)
    }
}
```
Keep the composition order (name, then affordances) so the merged a11y announcement order does not
change. Lock the result in with the test from WR-02.

## Warnings

### WR-01: The new gallery test passes against the pre-fix code, so it guards nothing

**File:** `src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ApproachLadderCardGalleryDemoTest.kt:93-113` (class harness at 39-40)
**Issue:**
- `variantsContent_includesLongGermanLabelCell_withBoundedAffordanceWrapping` lives in a class with no `@GraphicsMode(NATIVE)` and no width qualifier. Under legacy text measurement the German labels never wrap. `ApproachLadderCardLongLabelTest`'s own KDoc says "the legacy shadow measurement does not reproduce the wrap".
- **Verified:** with `ce3516c`'s `ApproachLadderCard.kt` and `SegmentedOptionSelector.kt` (no FlowRow, no reserved slot), all 4 tests in this class pass, including the new one.
- It also never checks the actual G-15-1 symptom, the Cloud name width. Its only other assertion, that "Ausschließlich offline, selected" appears exactly once, holds no matter the layout.
- The plan describes this test as the only way the gallery fixture is exercised, so the Gate-2 cell has no working automated guard.

**Fix:** Add `@GraphicsMode(GraphicsMode.Mode.NATIVE)` and `@Config(qualifiers = "w360dp-h800dp")` to this method (both annotations accept METHOD), or move it into a NATIVE-harness class.
- Assert that the German cell's Cloud name has `size.width > 0` and is not ellipsized. Scope the lookup to the German cell, for example the "Cloud" node directly below the German section label, because "Cloud" appears in several cells.
- Re-run it against `ce3516c` and confirm it fails there.

### WR-02: The English guard tests cannot detect CR-01

**File:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCardLongLabelTest.kt:170-181`; `ApproachLadderCardTest` (unchanged, legacy graphics, default ~320dp window)
**Issue:**
- Test 4 asserts `lineCount == 1` for each affordance Text. Each Text still is one line; what changed is that the FlowRow stacks them onto two lines. So the test passes against the regression it is meant to catch.
- The SUMMARY treats "ApproachLadderCardTest 33/33 passes unchanged, including row-pitch/touch-target geometry" as evidence that short-label rows are unchanged. That class runs under legacy measurement, where the English labels are a few px wide, so it cannot see FlowRow wrapping either.

**Fix:** In Test 4, also assert that the Cloud row's affordances share one line:
```kotlin
val tops = listOf(text("Unavailable"), cloudCappedNode("Capped"), text("Needs network"))
    .map { it.top() }
assertTrue("English affordances must share one line, tops=$tops", tops.distinct().size == 1)
```
Parameterize it over w320dp and w360dp (and the gallery's extra 16dp padding) so the cap cannot regress silently.

### WR-03: The unconditional 26dp slot wraps short labels at large font scales, against the "short labels unchanged" truth

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/SegmentedOptionSelector.kt:113-122`
**Issue:**
- `reserveSegmentIconSlot()` always takes `IconSize + IconSpacing` (26dp) off every label, selected or not.
- Before the fix, a selected label wider than `content - 26dp` spilled into the segment's 12dp `TextButtonContentPadding`, but still stayed inside the segment border. Only labels within about 2dp of the full content width actually crossed the border. That was the 1px overflow in the RED evidence (345 vs 344).
- After the fix, short English labels wrap at accessibility font scales.
  - Measured at fontScale 2.0: "Router off"/"Router on" went from 1 line to 2 lines each, and the toggle grew from 46dp to 84dp.
  - "Offline only" in the 360dp card went from 1 line to 2 lines.
- The unselected segment, which has no icon, also loses 26dp. It wraps only so that it matches its sibling.
- Plan truth #4 says short labels are unchanged, and nothing tests fontScale > 1. So this behavior change is undocumented and unpinned.

**Fix:** Pick one of these and pin it with a fontScale-2.0 test:
- (a) Accept it: document in API.md section 5 that labels wrap at large font scales because the check-icon slot is always reserved, so the label does not reflow on toggle.
- (b) Reserve only what keeps the label inside the border: `slot - 2 * TextButtonContentPadding.horizontal`, which comes to about 2dp. This lets the label use the padding as v2.5.0 did. The trade-off is a tighter visual margin on the selected segment.

### WR-04: The reserved slot copies the wrong M3 constant (`ButtonDefaults.IconSpacing`)

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/SegmentedOptionSelector.kt:105-106, 115`
**Issue:**
- The KDoc says M3 places the label after `SegmentedButtonDefaults.IconSize + ButtonDefaults.IconSpacing`.
- Disassembling material3 1.4.0 shows that `SegmentedButtonContentMeasurePolicy` actually reads a **private** `SegmentedButtonKt.IconSpacing`, an 8dp literal (`access$getIconSpacing$p`).
- `ButtonDefaults.IconSpacing` is a different token, `ButtonSmallTokens.IconLabelSpace`. It also happens to be 8dp today.
- The two are equal by coincidence. A Material token update (M3 Expressive has already reshuffled button tokens) could move `ButtonDefaults.IconSpacing` without touching the segmented layout, and the reservation would silently over- or under-shoot.

**Fix:** Pin the value M3 really uses, and say so:
```kotlin
// M3 1.4.0 SegmentedButton.kt: `private val IconSpacing = 8.dp` (not ButtonDefaults.IconSpacing).
private val SegmentedButtonIconSpacing = 8.dp
...
val slot = (SegmentedButtonDefaults.IconSize + SegmentedButtonIconSpacing).roundToPx()
```
Correct the KDoc to match. The existing overflow test stays as the M3-bump tripwire.

## Info

### IN-01: "At least 40%" is really 40% minus the 4dp gap

**File:** `ApproachLadderCard.kt:81-82, 305-309`; `API.md:210-211`
**Issue:** The cap is applied to the full row width, and `CapControl`'s 4dp gap then comes out of the name's share. Measured with German labels: 115dp of a 296dp row in the gallery (38.9%), and 112dp of 288dp at w320dp (38.9%). API.md says "always keeps at least 40%". If CR-01 is fixed with a need-based floor this goes away. Otherwise, subtract the gap inside the cap or reword the claim as "about 40%".

### IN-02: No RTL coverage, and the overlap test is LTR-only

**File:** `ApproachLadderCardLongLabelTest.kt:138-155`; `SegmentedOptionSelectorTest.kt`
**Issue:** The probes show RTL is correct: the name sits at the right, the affordances at the left with no overlap, and segment labels stay inside their segments. But no test pins it. Test 3's `left >= nameRight` only holds in LTR. As the SUMMARY admits, it also passed before the fix, because the collapsed 0-width name sits trivially left of everything. Add an RTL variant (`LocalLayoutDirection provides Rtl`) that asserts `affordanceRight <= nameLeft`, plus an explicit floor check such as `nameWidth >= 0.4 * rowWidth - gap`.

### IN-03: Misleading "placeRelative so RTL mirrors" comments

**File:** `ApproachLadderCard.kt:312-314, 323`; `SegmentedOptionSelector.kt:110-111, 121`
**Issue:** Both modifiers report exactly the placeable's size and place it at (0, 0). With a wrapper the same size as its child, `place` and `placeRelative` are identical, so nothing gets mirrored. RTL actually comes from the parent Row/FlowRow and from M3's own placement, which in 1.4.0 uses non-relative `place`. The behavior is harmless; the comment overstates what these modifiers do. Reword it or drop the clause.

---

_Reviewed: 2026-10-11_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
