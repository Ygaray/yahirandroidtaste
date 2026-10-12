---
phase: 15-voice-surface-i18n-label-params
verified: 2026-10-12T01:20:00Z
status: passed
score: 13/13 must-haves verified
covered_files:

  - ".planning/REQUIREMENTS.md#VI18N-01"
  - ".planning/REQUIREMENTS.md#VI18N-02"
  - ".planning/REQUIREMENTS.md#VI18N-03"
  - ".planning/REQUIREMENTS.md#VI18N-04"
  - ".planning/phases/15-voice-surface-i18n-label-params/15-01-PLAN.md"
  - ".planning/phases/15-voice-surface-i18n-label-params/15-01-SUMMARY.md"
  - ".planning/phases/15-voice-surface-i18n-label-params/15-02-PLAN.md"
  - ".planning/phases/15-voice-surface-i18n-label-params/15-02-SUMMARY.md"
  - ".planning/phases/15-voice-surface-i18n-label-params/15-03-PLAN.md"
  - ".planning/phases/15-voice-surface-i18n-label-params/15-03-SUMMARY.md"
  - ".planning/phases/15-voice-surface-i18n-label-params/15-04-PLAN.md"
  - ".planning/phases/15-voice-surface-i18n-label-params/15-04-SUMMARY.md"
  - "API.md"
  - "api.txt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/component/ClarificationBar.kt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/component/ModelSelectCard.kt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheet.kt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCard.kt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/component/SegmentedOptionSelector.kt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/explorer/VoiceCommandFamilyScreen.kt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/model/HandledByUiModel.kt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/model/ProposedItemUiModel.kt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/model/UndoRefusedUiModel.kt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/model/UndoRowUiModel.kt"

covered_digest: "v1:sha256:fe75c31f82e60afffc6a0bd98536483d83892de509c31bb8139548a32033d9fc"
behavior_unverified: 0
overrides_applied: 0
re_verification:
  previous_status: passed
  previous_score: 5/5
  previous_verified: 2026-10-05
  reopened_by: "15-UAT.md Gate-2 test 1 (gap G-15-1, major)"
  gaps_closed:
    - "G-15-1: ApproachLadderCard rung rows stay legible with long caller-supplied rung-state labels (rung name visible, affordances wrap/ellipsize, no per-character wrapping); segmented toggle labels no longer clip"
    - "Gate-2 correction item: SC5 trailing-lambda claim corrected for ProposedItemUiModel"
  gaps_remaining: []
  regressions: []
coincidental_reliance_items: []
warnings:

  - "15-04-PLAN.md (key_links[0], Task 1 Step B, Task 3 API.md text, threat T-15-04-01) and 15-04-SUMMARY.md (headline, Accomplishments, tech-stack pattern, Files) still describe the private Modifier.maxWidthFraction 60% FlowRow cap. That mechanism was replaced by the private RungLabelLayout in c877c9d (CR-01). maxWidthFraction no longer exists in the code. The truths still hold against the final code (see below), but the SUMMARY is not an accurate record of what shipped."
  - "The previous report's warning 'NOT binary-compatible for Kotlin default-arg call sites' is stale. The 2026-10-05 binary-compat fix added @Deprecated(HIDDEN) v2.4.1 composable overloads and variant-K synthetics (visible in ApproachLadderCard.kt:201-220; documented in API.md). Project policy is now source-compatibility only (API.md 'Compatibility rule'). The same stale sentence is in .planning/uat-pending/15-voice-surface-i18n-label-params.md line 15."
  - "The v2.5.1 tag and the SecondBrain/CalTracker repins are human-gated and not done. API.md already labels the layout fix 'v2.5.1'; no v2.5.1 tag exists (only v2.5.0). 28 local commits on main are ahead of origin/main."

human_verification:

  - test: "Gate-2 on the personal device: open the YAT v2.5 Gate-2 harness 'P15: LONG German labels', then Explorer > Voice Command > ApproachLadderCard > 'long localized labels (German)', in light and dark."
    expected: "The Cloud rung name is fully visible. The three German affordances (Derzeit nicht verfügbar / Durch Obergrenze eingeschränkt / Netzwerkverbindung erforderlich) sit right-aligned, at most 2 lines each, with no character-per-line wrapping and no overlap with the name. 'Onlineverarbeitung' / 'Ausschließlich offline' wrap inside their segments without clipping at the card edge, and both segments are the same height. The OutcomeSheet German rows from the harness still render."
    why_human: "Visual legibility on real hardware with real fonts. Robolectric NATIVE geometry tests pin the numbers, not the look. Note: under the final RungLabelLayout the German affordances get more width than in the 15-04 build that the original UAT plan described, so they may stack differently."
  - test: "In the same gallery, compare the default-English ApproachLadderCard cells (especially 'combined subdued labels', where Cloud carries Unavailable + Capped + Needs network) with v2.5.0."
    expected: "All three English affordances share one line to the right of 'Cloud', as in v2.5.0. Row pitch and touch targets look unchanged."
    why_human: "CR-01 replaced the layout algorithm. The fixer flagged it 'requires human verification'. Tests prove the three tops are equal at 320/360dp, with 16dp padding and at fontScale 1.3, but not that the row looks pixel-identical."
  - test: "With a segment label that nearly fills its half (e.g. 'Ausschließlich offline' selected at 360dp), look at the check icon and label margins on the selected segment."
    expected: "The label stays inside the segment border and never runs under the check icon. The icon may reach close to the segment edge (the accepted WR-03 trade-off, the same as v2.5.0)."
    why_human: "WR-03 reserves only about 2dp rather than the full 26dp icon slot. Whether the tighter margin looks acceptable is a visual call."
---

# Phase 15: Voice-surface i18n label params Verification Report

**Phase Goal:** Every hardcoded label on the voice-command settings cards and voice-surface composables becomes caller-overridable, through optional defaulted params or additive defaulted model fields, so a consumer can pass localized strings. English defaults are preserved, and the hub itself localizes nothing (INV-01).
**Verified:** 2026-10-12T01:20:00Z
**Status:** human_needed. Everything that can be automated is verified. What remains is the Gate-2 visual check on the device.
**Re-verification:** Yes. The previous report (2026-10-05) passed 5/5. Gate-2 UAT then reopened the phase with G-15-1. This pass checks plan 15-04 and the code-review fixes (c877c9d..abdc4a8) against the code as it stands at HEAD `b5a51e4`.

## Goal Achievement

### Observable Truths

ROADMAP success criteria (SC1-SC5), then the 15-04 gap-closure truths (G1-G8). Every G-truth was checked against the **final** code (`RungLabelLayout`). The 60% `maxWidthFraction` FlowRow cap described in 15-04-PLAN/SUMMARY is no longer in the code.

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| SC1 | `providerLabel` on `ProviderKeyCard` and `modelLabel` on `ModelSelectCard` render the supplied text; defaults are "Provider" / "Model" | VERIFIED (regression) | Unchanged since the prior pass (no diff vs v2.5.0). `ProviderKeyCardTest` 8/8 and `ModelSelectCardTest` 5/5 re-run green this session. |
| SC2 | `ClarificationBar` renders the caller's `dismissLabel`; default "Dismiss" | VERIFIED (regression) | Unchanged. `ClarificationBarTest` 11/11 green. |
| SC3 | `ApproachLadderCard`'s five rung/toggle labels are caller-supplied with English defaults | VERIFIED | Signature `ApproachLadderCard.kt:105-121` unchanged; defaults "Unavailable"/"Capped"/"Needs network"/"Online"/"Offline only". The three rung labels pass through `RungRow` into `AffordanceText` (L276-284); the toggle labels go into `SegmentedOptionSelector` options (L177). `ApproachLadderCardTest` 33/33, a file not edited since v2.5.0 (`git diff --quiet v2.5.0` exit 0). |
| SC4 | `OutcomeSheet` literals can be overridden through additive defaulted model fields | VERIFIED (regression) | Model and sheet files unchanged since v2.5.0. `OutcomeSheetTest` 49/49 and `VoiceModelLabelDefaultsTest` 27/27 green. |
| SC5 | Callers that pass none of the new params see byte-identical English; the change is strictly additive | VERIFIED (source level; see SC5 section) | `apiCheck` green; `git diff --quiet v2.5.0 -- api.txt` exit 0, so 15-04 and the review fixes added no public surface. `VoiceI18nSourceCompatTest` 4/4. Documented exception: the `ProposedItemUiModel` trailing lambda (see G7). |
| G1 | With the German labels at 360dp, the Cloud rung name is displayed with nonzero width and is not ellipsized | VERIFIED | `RungLabelMeasurePolicy` (L338-371): the name reserves `min(maxIntrinsicWidth, row - floor(0.6*row))`; the affordances are measured with the remainder; the name gets everything that is left. Tests: `German labels at 360dp keep the Cloud rung name displayed...`, the RTL variant, and the gallery test (RED against ce3516c per 15-REVIEW-FIX: "width must be > 0, was 0"). All green. |
| G2 | No affordance renders on more than 2 lines or one character per line; no affordance overlaps the name | VERIFIED | `AffordanceText` (L294-304): `maxLines = 2`, Ellipsis, End. The affordances are placed at `rowWidth - it.width`, after the name's width plus the gap. Tests `German affordance labels each render ... at most two lines` (RED in 15-04 at 21 lines) and `... never overlap the rung name`, plus the RTL variant `affordances left of the name without overlap`. All green. |
| G3 | Short (English default) labels look as they did in v2.5.0: affordances on one line trailing on the right, name fills the rest; pre-existing ApproachLadderCardTest passes unchanged | VERIFIED | When the name's need is under 40%, the affordances get `row - nameNeed - gap`. Spacing matches v2.5.0: a 4dp gap after the name, `spacedBy(4.dp)` between affordances, and the needs-network start padding kept. Rows without affordances compose no affordance child, so the name takes the full width. Tests `English default labels ... share one line with the Cloud name` at 360dp, 320dp, with 16dp gallery padding, and at fontScale 1.3 (RED against the 60% cap per 15-REVIEW-FIX: tops `[30,30,45]`). `ApproachLadderCardTest` 33/33 unedited. The visual pixel match is in human item 2. |
| G4 | A segment label too long for its half wraps to at most 2 lines and then ellipsizes; it stays inside its segment, selected or not; both segments keep equal height; short labels and the content descriptions are unchanged | VERIFIED | `SegmentedOptionSelector.kt`: `maxLines = 2`, Ellipsis, Center (L77-84). `reserveSegmentIconSlot` reserves `(IconSize + 8dp) - horizontal ContentPadding` (L116-130; the WR-03/WR-04 final form, not the 26dp of the 15-04 SUMMARY). Equal height comes from `height(IntrinsicSize.Min)` on the row (L62) plus `fillMaxHeight()` on each segment (L73). contentDescription is unchanged (L74). `SegmentedOptionSelectorTest` 5/5: German inside-segment (RED 345 > 344 in 15-04), equal height, short labels and descriptions, and both fontScale-2 single-line tests (RED against the 26dp reserve). |
| G5 | Explorer > Voice Command > ApproachLadderCard Variants has a long German label cell reached through ComponentRegistry; existing cells keep English defaults | VERIFIED | `VoiceCommandFamilyScreen.kt`: private German consts and the new cell between "combined subdued labels" and "router on", with no router. `ApproachLadderCardGalleryDemoTest` reaches it through `ComponentRegistry.entries.first { it.name == "ApproachLadderCard" }.content` and is now NATIVE at w360dp (WR-01). 4/4 green, router-count pin still 2. |
| G6 | No public API change: apiCheck green and api.txt byte-identical to v2.5.0; detekt green at zero baseline; drift guard green with nothing new registered | VERIFIED | Ran this session: `apiCheck` and `detekt` exit 0. `git diff --quiet v2.5.0 -- api.txt` exit 0. `ComponentRegistryDriftGuardTest` 1/1 green. Every new symbol (`RungLabelLayout`, `RungLabelMeasurePolicy`, `AffordanceText`, `NameAffordanceGap`, `reserveSegmentIconSlot`, `SegmentedButtonIconSpacing`) is `private`. |
| G7 | 15-VERIFICATION no longer claims every trailing-lambda shape binds; it names `ProposedItemUiModel(id, title) { … }` and points to the API.md caveat | VERIFIED | Kept below in the SC5 section, with the dated marker. Accurate against the code: `ProposedItemUiModel.kt:44-45` has `trailingContent` and then `removeContentDescription: String = "Remove"`. The API.md bullet "**NOT source-compatible** for a **trailing-lambda** call of `ProposedItemUiModel`" exists (API.md ~L302). The consumer claim also checks out (read-only grep): CalTracker `VoiceOutcomeMapper.kt:319/372/443` and SecondBrain `VoiceConfirmSheetModels.kt:52` use the named form. |
| G8 | API.md documents the long-label behavior as a v2.5.1 layout-only fix; no tag cut, no consumer touched | VERIFIED | API.md §10 has the "Long labels (v2.5.1, layout only, no signature change)" bullet, updated in c877c9d to the final "full width or 40%, whichever is smaller" behavior, and the §5 SegmentedOptionSelector row has a v2.5.1 clause. `git tag -l 'v2.5*'` shows only v2.5.0. No consumer files were changed. |

**Score:** 13/13 truths verified; 0 present but behavior-unverified. The layout truths G1-G4 are backed by geometry tests that ran, and were shown RED before their fixes, not by code presence alone.

### Success Criterion 5 - explicit evaluation

- No parameter, field, or property was removed, renamed, retyped, or reordered. Every addition is appended after the former last element and defaulted. Positional and named v2.4.0 call shapes still bind the same values, and so do trailing-lambda shapes, with one exception: `ProposedItemUiModel(id, title) { … }` no longer compiles, because `removeContentDescription: String` was appended after the former last parameter `trailingContent`, so Kotlin binds the trailing lambda to the `String`. Callers must pass `trailingContent = { … }` by name. This is a documented source-level break only, with no shim; it already shipped this way in v2.5.0, and both consumers use the named form. See `API.md`, section "Appending label fields to the voice models and composables (v2.5.0, VI18N-01..04)", bullet "NOT source-compatible for a trailing-lambda call of `ProposedItemUiModel`". (Corrected 2026-10-11 per 18-UAT test 3; the original claim was wrong for ProposedItemUiModel.) Verified by compile-time fixture plus runtime assertions for the shapes `VoiceI18nSourceCompatTest` covers: positional composable calls through the former last parameter, positional/named construction, positional and legacy-arity `copy`, destructuring, and the `FailureActionUiModel("l") { … }` trailing lambda.
- The api.txt delta vs v2.4.1 is the 8 superseded lines analysed in the prior pass (4 composable re-signatures, 4 generated `copy` lines replaced by a legacy-arity `copy` plus the full-arity `copy`). 15-04 and the review fixes add **zero** api.txt lines (byte-identical to v2.5.0).
- Binary compatibility, updated: the prior report said it was not preserved. Since then the 2026-10-05 binary-compat fix added hidden v2.4.1 overloads and variant-K synthetics, and the project policy (API.md "Compatibility rule") now requires source compatibility only. Neither change touches SC5, which is a source-level criterion.
- Judgment: SC5 holds at the contract the project gates on, and its one exception is documented accurately.

### Required Artifacts

| Artifact | Expected | Status | Details |
|----------|----------|--------|---------|
| `component/ApproachLadderCard.kt` | 5 label params (SC3); long-label rung layout (G1-G3) | VERIFIED | Private `RungLabelLayout` + `RungLabelMeasurePolicy` + `AffordanceText`; `AFFORDANCE_MAX_WIDTH_FRACTION` declared and used (L310, L352) |
| `component/SegmentedOptionSelector.kt` | Wrap-in-segment, equal heights (G4) | VERIFIED | `reserveSegmentIconSlot` declared and used; IntrinsicSize equal-height |
| `explorer/VoiceCommandFamilyScreen.kt` | German Gate-2 cell (G5) | VERIFIED | Registry-reached, no router |
| `ApproachLadderCardLongLabelTest.kt` | German/English/RTL/floor geometry | VERIFIED | 10 tests, NATIVE, w360dp |
| `SegmentedOptionSelectorTest.kt` | Segment overflow / height / fontScale | VERIFIED | 5 tests |
| `ApproachLadderCardGalleryDemoTest.kt` | Gallery cell guard | VERIFIED | 4 tests; the German test is now NATIVE and checks the Cloud width |
| `component/{ProviderKeyCard,ModelSelectCard,ClarificationBar,OutcomeSheet}.kt`, `model/*UiModel.kt` | SC1, SC2, SC4 | VERIFIED (regression) | No diff vs v2.5.0; suites green |
| `API.md` | v2.5.1 notes; SC5 caveat | VERIFIED | Matches the final code (40%-or-need wording) |
| `api.txt` | Byte-identical to v2.5.0 | VERIFIED | diff exit 0 |

### Key Link Verification

| From | To | Via | Status | Details |
|------|----|-----|--------|---------|
| `RungRow` | name + affordance `FlowRow` | `RungLabelLayout(modifier = Modifier.weight(1f))` as the single `CapControl` child | WIRED | Replaces the 15-04 key_link "FlowRow capped by Modifier.maxWidthFraction" (that link no longer exists; superseded by CR-01, c877c9d). `CapControl` signature and merged semantics are unchanged; composition order is name, then affordances. |
| `SegmentedButton` label `Text` | `reserveSegmentIconSlot()` | modifier on the label | WIRED | The reserve is `slot - ContentPadding`, about 2dp (WR-03), using a pinned private 8dp spacing (WR-04), not the plan's `ButtonDefaults.IconSpacing` |
| Variants German cell | `ComponentRegistry` "ApproachLadderCard" `.content` | `ApproachLadderCardVariants()` (L142) | WIRED | Exercised by `ApproachLadderCardGalleryDemoTest` |
| 15-VERIFICATION SC5 | API.md trailing-lambda caveat | Section and bullet name | WIRED | Both texts present |
| Public label params | `AffordanceText` / segment options | Non-default private params | WIRED | English literals only on the public signature |

### Data-Flow Trace (Level 4)

Pure pass-through from caller params to `Text`/`contentDescription`, with no fetch or store. FLOWING. The only literals are the intended English defaults on the public signatures plus the explorer-only German fixture.

### Behavioral Spot-Checks

| Behavior | Command | Result | Status |
|----------|---------|--------|--------|
| Phase-15 suites + registry guards + API/lint gates | `./gradlew testDebugUnitTest --tests {ApproachLadderCardLongLabelTest, SegmentedOptionSelectorTest, ApproachLadderCardTest, ApproachLadderCardGalleryDemoTest, ProviderKeyCardTest, ModelSelectCardTest, ClarificationBarTest, OutcomeSheetTest, VoiceI18nSourceCompatTest, VoiceModelLabelDefaultsTest, ComponentRegistry*} apiCheck detekt` | exit 0; JUnit XML (timestamps 2026-10-12T01:15Z): 10+5+33+4+8+5+11+49+4+27+1+6+4 tests, 0 failures, 0 errors, 0 skipped | PASS |
| Public surface frozen | `git diff --quiet v2.5.0 -- api.txt` | exit 0 | PASS |
| Pre-existing card test untouched | `git diff --quiet v2.5.0 HEAD -- .../ApproachLadderCardTest.kt` | exit 0 | PASS |
| Full suite (767 tests) | orchestrator / 15-REVIEW-FIX report | reported green at HEAD; not re-run in full by me (the targeted run covers every phase-15 class) | ACCEPTED |

Probes: none declared in any phase-15 plan (SKIPPED).

### Requirements Coverage

| Requirement | Source Plan | Description | Status | Evidence |
|-------------|-------------|-------------|--------|----------|
| VI18N-01 | 15-01 | `providerLabel` / `modelLabel` | SATISFIED | SC1 |
| VI18N-02 | 15-01 | `dismissLabel` | SATISFIED | SC2 |
| VI18N-03 | 15-01, 15-04 | Five ApproachLadderCard labels | SATISFIED | SC3, plus G1-G4: the overrides are now usable with long localized strings |
| VI18N-04 | 15-02, 15-03, 15-04 | OutcomeSheet model label fields | SATISFIED | SC4, G7 |

Every ID in PLAN frontmatter (`[VI18N-01,02,03]`, `[VI18N-03,04]`, `[VI18N-04]` x2) is mapped to Phase 15 in REQUIREMENTS.md and marked Complete. There are no orphaned requirements.

### Invariants

- INV-01: the src/main diff vs v2.5.0 adds only `androidx.compose.*` imports. It references no consumer package, and the hub localizes nothing; the German strings are explorer-only fixtures.
- Registry: no new public composable; drift guard green.
- Debt markers (TBD/FIXME/XXX/TODO/HACK) in added src/main lines vs v2.5.0: none.

### Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
|------|------|---------|----------|--------|
| `15-04-SUMMARY.md` | 27, 106, 118, 156 | Describes the replaced `maxWidthFraction` / 60% cap mechanism | Warning | Historical record does not match shipped code; later readers will be misled. Suggest a "superseded by CR-01 (c877c9d) — see 15-REVIEW-FIX.md" note. |
| `15-04-PLAN.md` | 46, 152-155, 255, 290 | Same superseded mechanism in key_links and threat model | Info | Plan is a historical intent record; the truths still hold |
| `.planning/uat-pending/15-…md` | 15 | Stale "not binary compatible" note | Info | Contradicts current API.md |
| `SegmentedOptionSelector.kt` | 123 | A label within ~2dp of the full content width now wraps where v2.5.0 kept it on one line | Info | Practically invisible; "short labels unchanged" holds for real short labels (fontScale-2 tests pin Router/Offline) |

No stubs, placeholders, or empty implementations.

### Human Verification Required

#### 1. German long labels on the device (G-15-1 Gate-2 re-check)

**Test:** On the personal device, open the YAT v2.5 Gate-2 harness "P15: LONG German labels" and Explorer > Voice Command > ApproachLadderCard > "long localized labels (German)", in light and dark.
**Expected:** The Cloud name is visible. The German affordances are right-aligned, at most 2 lines each, with no per-character wrap and no overlap. The segmented toggle labels wrap inside their segments without clipping, and the segments are the same height.
**Why human:** Visual legibility with real fonts. The final layout also gives the affordances more width than the 15-04 build did.

#### 2. English defaults look as they did in v2.5.0

**Test:** Check the English gallery cells, especially "combined subdued labels".
**Expected:** Unavailable, Capped and Needs network sit on one line next to Cloud. Row pitch is unchanged.
**Why human:** CR-01 swapped the layout algorithm. Tests prove the shared line, not a pixel match.

#### 3. Check-icon margin on a full-width selected segment

**Test:** Look at "Ausschließlich offline" while it is selected.
**Expected:** The label is inside the border and never under the check icon. The icon may sit close to the edge (the accepted WR-03 trade-off).
**Why human:** This is an aesthetic judgment.

### Gaps Summary

There are no blocking gaps. G-15-1 is closed in code. The final `RungLabelLayout` gives the rung name its full need or 40% of the row, whichever is smaller. The affordances get the rest, wrap to at most 2 lines, and never overlap the name. Segment labels stay inside their segments, and the segments have equal heights. All of this is backed by geometry tests that were shown RED before their fixes and are green now. English defaults stay on one line, which is guarded at 320/360dp, with padding, and at fontScale 1.3. The public surface is byte-identical to v2.5.0. The SC5 trailing-lambda correction is present and accurate.

There are two documentation-hygiene warnings, neither blocking. 15-04-SUMMARY/PLAN describe the replaced 60% `maxWidthFraction` cap rather than what shipped. The earlier "not binary-compatible" caveat is stale.

The phase stays `human_needed` until the Gate-2 visual check is done. After that, the v2.5.1 tag and the consumer repins still need the owner's go-ahead, routed through the orchestrator.

---

_Verified: 2026-10-12T01:20:00Z_
_Verifier: Claude (gsd-verifier)_
