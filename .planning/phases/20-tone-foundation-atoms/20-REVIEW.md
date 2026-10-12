---
phase: 20-tone-foundation-atoms
reviewed: 2026-10-12T00:00:00Z
depth: standard
files_reviewed: 23
files_reviewed_list:
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/DynamicActionButton.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/PillChip.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/RoundIconButton.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/StackedProgressBar.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/ToneCard.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ButtonsFabFamilyScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/CardsFamilyScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ChipsFamilyScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ProgressFamilyScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/theme/Dimens.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/theme/ToneTokens.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/DynamicActionButtonEmphasisTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/DynamicActionButtonLegacyPathSourceContractTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/PillChipTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/RoundIconButtonTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/StackedProgressBarTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/ToneAtomsLongLabelTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/ToneCardTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/ToneFoundationInvariantsTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ToneAtomsRegistryRenderTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/theme/ContrastTestSupport.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/theme/DimensTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/theme/ToneTokensTest.kt
findings:
  critical: 0
  warning: 4
  info: 6
  total: 10
status: resolved
---

# Phase 20: Code Review Report

**Reviewed:** 2026-10-12
**Depth:** standard
**Files Reviewed:** 23
**Status:** issues_found

## Summary

Reviewed the tone tokens, the four new atoms, the `DynamicActionButton` emphasis/icon axis, the
four registry/gallery screens and the new tests. No static analysis or Gradle run was done (the
host build lock is held); contrast numbers were computed by hand from `Color.kt`.

The code is in good shape. The invariants hold: one-way dependency (imports only AndroidX, Kotlin
and the hub), no `testTagsAsResourceId`, no Hilt host, no `@Deprecated(HIDDEN)` shims, new params
appended after `modifier` with defaults, and the no-emphasis/no-icon call still runs the untouched
legacy `when (role)` block. Hub-palette contrast is above 4.5:1 for the error tone (5.00 light,
5.51 dark), the warn tone (13.4, 7.96) and the Tonal label over its 14% tint (4.83 to 7.5).

There are no BLOCKERs. The warnings are one latent crash in `StackedProgressBar` with in-place
mutated lists, a `TextSmall` emphasis whose "compact" tokens do nothing, a restyle that an icon-only
`DynamicActionButton` call gets, and a test that cannot detect what it claims to detect.

## Warnings

### WR-01: StackedProgressBar can crash or draw stale segments when the caller mutates the list in place

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/StackedProgressBar.kt:90-108`
**Issue:** `fractions` is cached with `remember(segments)`, but the draw loop reads the live
`segments[index].color`. The parameter type is `List<BarSegment>`, so a caller may pass a
`mutableStateListOf` or an `ArrayList` and mutate it in place. The `remember` key is then the same
instance, so it compares equal to itself and the cache is never recomputed. Two bad outcomes:

- The list shrinks while `fractions` keeps its old size. `segments[index]` then throws
  `IndexOutOfBoundsException` during composition.
- A segment's fraction changes. The bar keeps drawing the stale widths. Colors would update
  because they are read live, so the bar is internally inconsistent.

The clamp is a single pass over a handful of floats, so the cache buys nothing.
**Fix:** Drop `remember`, and zip the clamped fractions with the segments from one snapshot:
```kotlin
val fractions = clampSegmentFractions(segments.map { it.fraction })
val rest = 1f - fractions.sum()
// ...
fractions.forEachIndexed { index, fraction ->
    if (fraction > 0f) { /* uses segments[index] */ }
}
```
If caching is kept, key on the content (`remember(segments.map { it.fraction })`) and read colors
from the same snapshot, and add a regression test that shrinks a `SnapshotStateList`.

### WR-02: TextSmall's "compact" tokens are inert, and the public KDoc promises a compact button

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/DynamicActionButton.kt:130-131, 211-220`; `src/main/java/io/github/ygaray/yahirandroidtaste/theme/Dimens.kt:~196-200`
**Issue:** `ActionButtonEmphasis.TextSmall` is documented as a "Compact text button (small label,
tight padding)". It is built from an M3 `TextButton`, whose content row enforces a 58dp by 40dp
minimum. With a `bodySmall` label and 4dp vertical padding the content is about 24dp tall, so the
M3 minimum governs. `TextSmallVerticalPadding` (4dp) has no visible effect, and
`TextSmallHorizontalPadding` (8dp) has none for any label narrower than 58dp. The drawn size is
identical to `Text`, and `minimumInteractiveComponentSize()` then reserves 48dp. The 20-02 SUMMARY
records this ("TextSmall 40dp tall ... flagged for the owner screencap review"), but the public KDoc
and `Dimens` KDoc still describe a compact button. The emphasis API is one-way (D-01), so consumers
will code against a promise the widget does not keep.
**Fix:** Either of these.
- Correct the KDoc now, so the contract matches what ships: "smaller label; drawn size is still the
  M3 40dp minimum".
- Make it truly compact with a custom clickable (`Modifier.clickable` + `Text`) that owns its
  padding and keeps the 48dp touch target.

Either way, add an assertion that records the intended `TextSmall` height. The current test only
`println`s it.

### WR-03: Passing only `icon` restyles a legacy button instead of just adding the icon

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/DynamicActionButton.kt:55-58, 69-72, 178-182`
**Issue:** The KDoc says "an [icon] alone keeps the legacy role mapping ... and adds the icon".
Routing it through `EmphasisActionButton` changes more than the icon:

- The label becomes `labelLarge` SemiBold (legacy is the default Medium weight).
- Save's horizontal padding goes from the M3 default 24dp to 20dp.
- Text-role padding is unchanged.

A consumer who adds `icon = ...` to an existing Save button sees the button re-weight and narrow,
which is an unrequested visual change. Phase 20's own goal is "every existing call renders exactly
as in v2.5.1". That holds for the no-icon path, but the icon-only path is a different render.
**Fix:** Either document it ("an icon routes the call through the emphasis styling: SemiBold label,
20dp/12dp padding"), or keep the legacy look by passing the legacy `ButtonDefaults.ContentPadding`
and the default label style when `emphasis == null`.

### WR-04: The "icon keeps the legacy role mapping" test is tautological

**File:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/DynamicActionButtonEmphasisTest.kt:~345-372`
**Issue:** The test compares an icon-only call to `emphasis = Filled/Text` plus the same icon. Both
go through `EmphasisActionButton`, so equal widths are guaranteed by construction. The test cannot
fail if the mapping or the visual parity with the real legacy widget regresses. It only re-proves
`legacyEmphasisFor`, which another test already covers. WR-03 passes it unnoticed. Related test
fragility:

- `DynamicActionButtonLegacyPathSourceContractTest` pins the exact whitespace-stripped legacy
  source, so any reformat of that block fails it.
- Its token checks (`stripped.contains("CircleShape")`, `"FontWeight.SemiBold"`) scan the whole
  file, so they are satisfied by any occurrence.

**Fix:** Compare the icon-only render against a no-icon legacy render. For example, assert that
Save + icon width equals the legacy Save width plus 26dp minus the padding delta, or assert the
intended deltas explicitly. Scope the source-token checks to `EmphasisActionButton`'s body with
`functionBody`.

## Info

### IN-01: DynamicActionButton class KDoc contradicts the new public `emphasis` parameter

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/DynamicActionButton.kt:31-34`
**Issue:** The first paragraph still says "The role -> widget mapping is fixed and internal (D-01)
... There is deliberately no caller-supplied widget override — this is a locked design decision".
`emphasis` is now exactly a caller-supplied widget choice. The second KDoc paragraph describes it
correctly, so the two disagree.
**Fix:** Reword the first paragraph to "With no `emphasis`, the role -> widget mapping is fixed and
internal".

### IN-02: Dimens.PillChip.Height KDoc says "Fixed"; the chip treats it as a minimum

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/theme/Dimens.kt:~215`
**Issue:** The token is documented as "Fixed drawn height of the chip", but `PillChip` applies it
as `heightIn(min = ...)` and its own KDoc says the chip grows at large font scales.
**Fix:** "Minimum drawn height of the chip (it grows with font scale)".

### IN-03: DISABLED_ALPHA duplicated in two files, and the literal 0.38f appears in several more

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/PillChip.kt:30`; `src/main/java/io/github/ygaray/yahirandroidtaste/component/RoundIconButton.kt:25`
**Issue:** Two identical private constants. `PresetChip`, `DateTimePicker` and `OutcomeSheet` use the
bare `0.38f`. It is a magic number repeated across five files.
**Fix:** One shared `internal const val DISABLED_CONTENT_ALPHA` (or a `Dimens`/theme token) used by
all of them. Non-urgent.

### IN-04: BarSegment is a public data class, which exposes copy/componentN

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/StackedProgressBar.kt:29-33`
**Issue:** `ToneColors` is deliberately a plain class so `copy`/`componentN` stay out of the public
API (ToneTokens.kt KDoc). `BarSegment` publishes `copy` into `api.txt` (line 85), which is the
exact surface the project avoids freezing. The inconsistency is minor, and the signature is
signed off (D-01).
**Fix:** Decide once. Either note why `BarSegment` is allowed to be a data class, or convert it
to the `ToneColors` pattern before the v2.6.0 cut.

### IN-05: Production test-only tag and RTL wording in StackedProgressBar

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/StackedProgressBar.kt:109, 54-57`
**Issue:**

- Each segment carries a production `testTag("stacked_progress_bar_segment")` that the KDoc labels
  "test-only and not stable API". It adds a semantics node per segment in every consumer's tree. If
  a consumer enables `testTagsAsResourceId`, the hook leaks as a resource id.
- KDoc says segments are "drawn left to right"; in RTL the `Row` mirrors, so they draw right to
  left. That is correct behavior for a progress bar, but the doc is inexact.
- `fillMaxWidth()` has no effect under unbounded width (for example inside a horizontal scroller),
  so the bar collapses to zero width. Documented as "pass a width", but easy to hit.

**Fix:** Say "start to end" in the KDoc. Consider keeping the segment tag but documenting the
`testTagsAsResourceId` caveat in API.md in Phase 24.

### IN-06: Smaller test and design notes

**File:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/DynamicActionButtonEmphasisTest.kt:~245`; `src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt:333-345`; `src/main/java/io/github/ygaray/yahirandroidtaste/theme/ToneTokens.kt:42, 54`
**Issue:**

- A `println("OBSERVED_HEIGHTS_DP ...")` debug artifact sits in a test that asserts nothing about
  `text` or `small` heights (see WR-02).
- The vocabulary drift guard's allowlist gained the generic nouns `Tone`, `Round`, `Pill`,
  `Stacked`. The allowlist is global, so any future component may now use those head tokens
  unflagged.
- `ToneDefaults.error()` uses `error` on `errorContainer` (D-05) rather than `onErrorContainer`.
  The hub palette passes at 5.0:1, a thin margin. The contrast test covers only the two hub
  palettes. The KDoc warns about dynamic color for `warn()` only, though `YahirAndroidTasteTheme`
  defaults to `dynamicColor = true`.
- `ContentTone` is public API with no consumer in the library yet.

**Fix:** Remove the `println` or turn it into an assertion. Scope the allowlist entries if the guard
supports it. Extend the `error()` KDoc with the same "pass your own tokens under dynamic color"
advice as `warn()`. Confirm `ContentTone` is consumed in Phase 21 or 22 before the cut.

---

_Reviewed: 2026-10-12_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
