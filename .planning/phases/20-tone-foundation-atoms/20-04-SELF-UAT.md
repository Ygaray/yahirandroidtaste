---
status: complete
result: all_pass
gate: 1
evidence_schema: 3
uat_run: uatr-20261012T112516Z-132c1d6b
phase: 20-tone-foundation-atoms
source:
  - "20-ROADMAP success criteria SC1-SC5"
device: SM-S908U (R5CT10XNKQN)
apk: io.github.ygaray.yahirandroidtasteharness (sha256 31e1ba14bf4e)
run: 2026-10-12T11:25:16.455Z
---

# Self-UAT Log - Phase 20 Plan 04 (Tone foundation and atoms; phase-level Gate 1), 2026-10-12

**Device:** tester `yahirs-s22-ultra-2`, SM-S908U, USB serial `R5CT10XNKQN`, Android 15 (API 35), 1080x2316 at 450 dpi (1 dp = 2.8125 px). Always driven with `adb -s R5CT10XNKQN`; the personal phone was never touched.
**Driver playbook:** `AGENT-DEVICE-TESTING.md` (project root; same-package-Intent harness mechanic).
**Build identity:** HEAD `3015de6`. Library AAR `com.github.Ygaray:yahirandroidtaste:1.10.0` republished fresh (old mavenLocal dir deleted first), md5 `aab6d21f0faf978c2300ce5df6c66d88`; `classes.jar` confirmed to contain `ToneCardKt`, `PillChipKt`, `RoundIconButtonKt`, `StackedProgressBarKt`. Throwaway harness APK (outside the repo, scratchpad `harness/`, never committed) md5 `23b27a4f892093116af345c64b2524b6`, package `io.github.ygaray.yahirandroidtasteharness`, one Java `MainActivity` that same-package-Intents into `ExplorerActivity`.
**Run:** `uat-run` `uatr-20261012T112516Z-132c1d6b` (`--plan 04`), 18 captures (ui, screen, log), finished and verified. Evidence dir: `.planning/phases/20-tone-foundation-atoms/20-04-uat-evidence/uatr-20261012T112516Z-132c1d6b/`.
**Pre-flight:** device `get-state` = device, `mWakefulness=Awake`, `isKeyguardShowing=false`, no passcode by design. Teamlead held the cross-repo device lock. Foreground before the run was a different app (`com.example.secondbrain`); the harness was installed with `adb install -r`, and both harness packages were uninstalled at the end (`pm list packages` shows no `yahirandroidtaste*`).
**Unit suite (fresh, uncached, rung 1):** `./gradlew cleanTestDebugUnitTest testDebugUnitTest detekt apiCheck --rerun-tasks --offline` -> BUILD SUCCESSFUL in 1m 26s, 44 of 44 tasks executed. 89 JUnit XML files, 0 older than the pre-run marker, tests=869 skipped=22 failures=0 errors=0. Phase classes: ToneTokensTest 8, ToneCardTest 7, DynamicActionButtonEmphasisTest 14, DynamicActionButtonLegacyPathSourceContractTest 3, DynamicActionButtonTest 5, RoundIconButtonTest 8, PillChipTest 11, StackedProgressBarTest 21, ToneAtomsRegistryRenderTest 10, ToneAtomsLongLabelTest 11, AppChipTest 12, PresetChipTest 9, ComponentRegistryDriftGuardTest 1, DomainVocabularyDriftGuardTest 2, ComponentRegistryTierTest 4; all 0 failures / 0 errors.
**Coverage/Nyquist:** `20-VALIDATION.md` frontmatter still reads `status: draft`, `nyquist_compliant: false` (validate-phase section 6 was not run to flip it). Every criterion below is nonetheless mapped to a named, freshly passing test plus a device observation. Not a blocker for Gate 1; flagged in Notes.
**Seed/fixture integrity:** the Explorer fixtures are static compiled-in Kotlin values (D5: no data layer). Environmental integrity = AAR republished from HEAD and confirmed to contain the phase-20 classes; the on-device gallery itself listed `ToneCard`, `RoundIconButton`, `PillChip`, `StackedProgressBar` in their families, which a stale AAR could not. The v2.5.1 baseline AAR used for the A/B in SC2 was published under a separate version (`1.10.0-v251`, built from `git archive b6c363c` in the scratchpad, repo untouched) and deleted from mavenLocal afterwards.
**Dynamic color caveat (applies to every colour reading):** `ExplorerActivity` runs `YahirAndroidTasteTheme(dynamicColor = true)`, so on this Android 15 device the active scheme is Material You (wallpaper-derived: page `#FAF8FF`, primary `#2A4174`, M3-baseline error). The device therefore shows the dynamic scheme, NOT the hub's static light/dark schemes. The hub-scheme contrast claim in SC1 is Robolectric-only by the criterion's own wording; the device observations below prove geometry, behaviour and "reads the ACTIVE scheme", not hub-palette hex values.

## Criteria

### 1. SC1 / VTONE-01 + VCARD-01 - tone tokens resolve from the active scheme; ToneCard geometry and contrast
result: passed
- **Rung:** 5 (device screenshot read and pixel-measured), backed by rung 1 (Robolectric)
- **Target:** device (tester) + headless Robolectric
- **Expected:** `ToneDefaults.error()` = `errorContainer`/`error`, `warn()` = `tertiaryContainer`/`onTertiaryContainer` from the active `ColorScheme`; `ToneColors` value equality; `ToneCard` 12dp corners, 12dp x 16dp padding, 4dp child spacing, content-on-container contrast >= 4.5:1 under the hub light and dark schemes (Robolectric).
- **Arranged (seeded):** none (static Explorer fixtures; AAR republish only).
- **Did (drove):** launched harness -> Cards family -> `ToneCard` detail (States "Default" cell = error tone; Variants = error tone and warn tone); captured light, tapped the `Switch to dark theme` action, captured dark, toggled back.
- **Observed:**
  - Rung 1: `ToneTokensTest` 8/0/0 (value equality, light/dark/custom scheme resolution, `hub tones meet 4_5 contrast on both hub schemes`), `ToneCardTest` 7/0/0 (padding/spacing/bounds, 12dp corner token, 4-way scheme x tone contrast), all fresh.
  - Rung 5, light: error card container `#F9DEDC`, content `#B3261E` (the M3-BASELINE `errorContainer`/`error` pair, not the hub's static `#FFDAD6`/`#BA1A1A`, which proves the card reads the ACTIVE dynamic scheme rather than a hard-coded hub value, and uses `error` as content, not `onErrorContainer`); measured contrast 5.14:1. Dark: container `#8C1D18`, content `#F2B8B5`, contrast 5.34:1.
  - Geometry from the uiautomator tree + pixels (error card): text starts 45 px (16dp) inside the container edge; container top to title top 34 px (12dp); title bottom to body top 11 px (4dp); body bottom to container bottom 33-34 px (12dp); container is 61.5dp tall for two lines; top-left corner starts curving about 31 px (11dp, 12dp within antialiasing) in from the edge. All match 12dp x 16dp / 4dp / 12dp.
  - Warn card (`ToneDefaults.warn()`): light container `#8A6A89` with `#FFFFFF` content, contrast 4.66:1; dark container `#A886A6` with `#000000`-ink content, contrast 6.63:1. Both clear 4.5:1 on this dynamic scheme. See Notes: the warn tone is mauve here, not the hub amber.
- **Evidence:** `01-sc1-tonecard-light-ui.xml`, `02-sc1-tonecard-light-screen.png`, `03-sc1-tonecard-dark-screen.png`.

### 2. SC2 / VBTN-01 - DynamicActionButton emphasis x role, icon, TextSmall 48dp, defaulted call identical to v2.5.1
result: passed
- **Rung:** 5 (device screenshots, pixel diff against a real v2.5.1 build), backed by rung 1
- **Target:** device (tester) + headless Robolectric
- **Expected:** each `ActionButtonEmphasis` renders as its Y-8 widget in the role's hue; `icon` shows when given; `TextSmall` keeps >= 48dp touch target; `emphasis = null` and `icon = null` renders exactly as v2.5.1; existing `DynamicActionButton` tests pass unedited.
- **Arranged (seeded):** none. For the A/B, a v2.5.1 baseline harness was built from `git archive b6c363c` under a separate mavenLocal version and a second applicationId (`...harnessb`), so both builds could be photographed on the same device in the same theme.
- **Did (drove):** Buttons / FAB -> `DynamicActionButton` detail, scrolled to the "emphasis x role (Y-8)" matrix and the "leading icon" row, captured ui + screen. Then captured the unscrolled page top (States cells + legacy Variants row) on HEAD, launched the v2.5.1 harness, opened the same page, captured the same frame, and pixel-diffed.
- **Observed:**
  - Matrix read from the screenshot: Filled = solid pill, Tonal = tinted pill, Text = bold label no fill, TextSmall = lighter smaller label; columns Skip (Neutral), Save (Save), Delete (Destructive). Sampled pixels: Filled Neutral and Save both `#2A4174` (primary), Filled Delete `#B3261E` (error); Tonal Neutral/Save `#DDDEEB` (primary at 14% alpha over page `#FAF8FF`), Tonal Delete `#F0DADF` (error at 14%); Text/TextSmall ink `#2A4174` / `#B3261E`. Role drives hue, as the criterion requires.
  - Icon row: Filled and Text "Done" both show a leading check glyph.
  - Touch targets from the tree: all four emphasis rows are 135 px tall = 48.0dp, including TextSmall (label node 39 px tall, `bodySmall`, inside a 135 px clickable). Legacy row also 135 px.
  - Falsifier for "renders exactly as v2.5.1": pixel diff of HEAD vs the real v2.5.1 build on the same device, same light theme: States "Default" cell, States "Disabled" cell, and the legacy Variants row (Discard/Cancel/Save) all have diff bbox `None`, sum 0; the whole y=300..1780 region is byte-identical. The full-frame diff is confined to the status-bar clock and the new Y-8 rows below the legacy row (absent in v2.5.1). The v2.5.1 Buttons list also lacks `RoundIconButton`, confirming the baseline really is the pre-phase build.
  - Rung 1: `DynamicActionButtonEmphasisTest` 14/0/0 (palette per emphasis x role x scheme, click once, disabled never fires, touch >= 48dp, defaulted vs explicit-null identical, icon-only delta), `DynamicActionButtonLegacyPathSourceContractTest` 3/0/0, `DynamicActionButtonTest` 5/0/0. `git diff b6c363c HEAD --stat` for `DynamicActionButtonTest.kt`, `AppChipTest.kt`, `PresetChipTest.kt`, `AppChip.kt`, `PresetChip.kt` is empty (unedited).
- **Evidence:** `04-sc2-emphasis-matrix-ui.xml`, `05-sc2-emphasis-matrix-light-screen.png`, `17-sc2-legacy-head-states-variants-screen.png`, `18-sc2-legacy-baseline-v251-screen.png`.

### 3. SC3 / VBTN-02 + VCHIP-01 - RoundIconButton and PillChip dimensions, touch target, semantics, disabled never fires
result: passed
- **Rung:** 5 (device press-state screenshots + pixel diff), backed by rung 1 and rung 4
- **Target:** device (tester) + headless Robolectric
- **Expected:** `RoundIconButton` 40dp circle, 1dp `outlineVariant` border, 20dp icon; `PillChip` 32dp tall, single-line `bodySmall` label, 18dp icons; each >= 48dp touch target and exposes its description/label; a click with `enabled = false` never fires; `AppChip`/`PresetChip` tests unchanged.
- **Arranged (seeded):** none. Gallery callbacks are `onClick = {}` no-ops, so "never fires" cannot be observed as a side effect on device; the device proof is the held-press ripple (a disabled control shows none) plus the `enabled=false` node flag, and the callback-identity proof is the Robolectric tests (per the playbook D4/D7 guidance).
- **Did (drove):** RoundIconButton detail then PillChip detail. Held a press in place (`adb shell input swipe x y x y 2500`) on an enabled control and on a disabled control while `uat-run capture --kind screen` fired mid-hold; compared regions pixel-wise.
- **Observed:**
  - RoundIconButton tree: `Add`/`Remove`/`Mic` nodes are 135x135 px = 48dp touch regions with `content-desc` exposed ("Add", "Remove", "Mic", "Add small", "Add large", "Add disabled"); the disabled ones report `enabled="false"`, enabled ones `enabled="true"`. Drawn diameters from pixels: default 40.2dp, `size=36` 35.9dp, `size=56` 56.2dp; border pixel `#797A83` 1dp ring; neighbour pitch 158 px (56dp).
  - RoundIconButton press: enabled `Add` region differs between pressed and rest frames (diff bbox (11,11)-(124,124), a filled grey ripple, read visually); disabled `Add disabled` region unchanged (diff `None`, sum 0); an untouched control (Mic) also `None`.
  - PillChip tree: every chip clickable is 135 px tall = 48dp touch region; label-only chip draws 53.0 x 32.7dp (32dp + border antialiasing). The 200dp-wide long-label chip is exactly 563 px = 200dp wide with a single 39 px label line, visibly ellipsized ("Alle ausstehenden Aen..."), so single-line `bodySmall` holds for a long German string. Disabled chips report `enabled="false"`.
  - PillChip press: the "Default" cell shows the ripple when held (diff bbox (0,22)-(266,112)); the "Disabled" cell is unchanged when held (diff `None`); the untouched variants row is `None`.
  - Rung 1: `RoundIconButtonTest` 8/0/0, `PillChipTest` 11/0/0, `ToneAtomsLongLabelTest` 11/0/0, `AppChipTest` 12/0/0, `PresetChipTest` 9/0/0, all fresh and unedited (see SC2 diff note).
- **Evidence:** `06-sc3-roundiconbutton-ui.xml`, `07-sc3-roundiconbutton-enabled-pressed-screen.png`, `08-sc3-roundiconbutton-disabled-pressed-screen.png`, `09-sc3-pillchip-ui.xml`, `10-sc3-pillchip-enabled-pressed-screen.png`, `12-sc3-pillchip-disabledcell-pressed-screen.png`, `13-sc3-pillchip-defaultcell-pressed-screen.png`. (`11-sc3-pillchip-disabled-pressed-screen.png` is a mis-aimed capture, see Notes; it is not relied on.)

### 4. SC4 / VBAR-01 - StackedProgressBar left-to-right segments, rounded track, clamp, negative as 0
result: passed
- **Rung:** 5 (device screenshot, per-segment pixel widths), backed by rung 1
- **Target:** device (tester) + headless unit/Robolectric
- **Expected:** segments drawn left to right inside a fully rounded track; a list summing above 1 is clamped to <= 1; a negative fraction draws as 0.
- **Arranged (seeded):** none (the Variants block ships exactly the falsifying inputs: 0.4+0.18, 0.7+0.6, -0.3+0.5, empty, 12dp).
- **Did (drove):** Progress / Metrics -> `StackedProgressBar` detail; captured ui + screen; measured each bar's colour runs across the 990 px track (45..1034).
- **Observed:** track is 990 px wide. Bar 1 (0.4, 0.18): primary `#2A4174` 396 px (0.400) then tertiary `#553A55` 178 px (0.180) then track `#5D5F67` 416 px (0.420), order left to right as given. Bar 2 (0.7, 0.6, sum 1.3): 693 px (0.700) + 297 px (0.300) = 990 px, no overflow, the second segment is truncated to the remaining 0.3. Bar 3 (-0.3, 0.5): no primary pixels at all, tertiary 495 px (0.500) then track 495 px. Empty list: 990 px of pure track. Bar heights 6.04dp (default) and 12.09dp (`height = 12.dp`); ends are visibly rounded pills in the screenshot. Rung 1: `StackedProgressBarTest` 21/0/0 (8 pure clamp tests, per-segment Robolectric widths, in-place list mutation regression, NaN/empty cases).
- **Evidence:** `14-sc4-stackedbar-ui.xml`, `15-sc4-stackedbar-light-screen.png`.

### 5. SC5 - four atoms registered in family screens; registry/drift guards and apiCheck green
result: passed
- **Rung:** 4 (on-device registry listing), backed by rungs 1 and 3
- **Target:** device (tester) + headless
- **Expected:** `RoundIconButton`, `StackedProgressBar`, `ToneCard`, `PillChip` registered in their family screens; registry integrity test and CATALOG drift guard green; `apiCheck` green on an `api.txt` regenerated in the same commit.
- **Arranged (seeded):** none.
- **Did (drove):** opened the Cards, Buttons / FAB, Chips and Progress / Metrics families on the device and read the tree lists.
- **Observed:** `ToneCard` (Primitive) in Cards, `RoundIconButton` in Buttons / FAB, `PillChip` in Chips, `StackedProgressBar` in Progress; each opens a detail page with States and Variants. The v2.5.1 baseline Buttons list had no `RoundIconButton`, so the registrations are new. Rung 1/3: `ComponentRegistryDriftGuardTest` 1/0/0, `DomainVocabularyDriftGuardTest` 2/0/0, `ComponentRegistryTierTest` 4/0/0, `ToneAtomsRegistryRenderTest` 10/0/0; `apiCheck` and `detekt` ran fresh in the same Gradle invocation and passed (BUILD SUCCESSFUL, 44/44 executed). `git log -- api.txt` shows api.txt changed in each atom commit (`0ad6374` tone+ToneCard, `b376f45` RoundIconButton, `2a4d58e` PillChip, `116a08e` StackedProgressBar), i.e. regenerated in the same commit as the code.
- **Evidence:** `01-sc1-tonecard-light-ui.xml`, `06-sc3-roundiconbutton-ui.xml`, `09-sc3-pillchip-ui.xml`, `14-sc4-stackedbar-ui.xml` (family lists were read live from the tree), plus the Gradle output above.

## Summary

total: 5
passed: 5
partial: 0
failed: 0
infra: 0

## Notes / anomalies (for the Gate-2 reviewer)

- **Warn tone is not amber on a Material You device (owner judgement, not a code defect).** On the tester, `ToneDefaults.warn()` renders mauve (`#8A6A89`/white light, `#A886A6`/black dark), because the Explorer enables dynamic color and `tertiary` is not amber there. This is the caveat already written into the `ToneTokens.kt` KDoc ("pass your own ToneColors when dynamic color makes tertiary non-amber"). The container/content pair passes 4.5:1 in both themes here, but the hub amber warn look can only be seen under the static schemes. Gate-2 asks the owner to judge the warn look in the Phase 24 screencap review; the hub-palette contrast numbers remain Robolectric-only (and green).
- **Hub-scheme pixel colours are not device-verified.** Because the Explorer is dynamic, the hub static light/dark hex values are asserted only by Robolectric (`hub tones meet 4_5 contrast on both hub schemes`), which is exactly how SC1 words it. The device run corroborates geometry, behaviour and active-scheme resolution.
- **Mis-aimed capture 11.** The first attempt to press the bottom-of-page disabled chip landed on the system navigation bar (the chip sat under it), opened Recents, and `11-sc3-pillchip-disabled-pressed-screen.png` shows that. It is a signed capture but proves nothing about the chip. The disabled chip was re-tested in the States cell (captures 12 and 13). BACK from Recents returned to the Explorer with state intact. Tooling artifact, not a defect.
- **`uat-run finish` verify warning:** `capture 16-logcat-fatal-check.log is an empty logcat capture (negative evidence)`. Recorded on purpose with `--allow-empty` and the filter `AndroidRuntime:E *:S` (no crash during the run); the run's CLI captures are the ui/screen items.
- **Plan id.** `uat-run start --plan` rejected `20-04` ("--plan is required (e.g. 01)") and accepted `04`; the log is `20-04-SELF-UAT.md`, evidence dir `20-04-uat-evidence/`.
- **TextSmall visual size.** Per code-review WR-02 the 8dp x 4dp TextSmall padding tokens are inert (M3 `TextButton` minimums govern); on device the 48dp touch region holds, but its drawn look is the owner's call at the Phase 24 review.
- **`BarSegment` is a public data class** (code-review IN-04, owner decision deferred before the v2.6.0 cut); not a Gate-1 matter.
- **Nyquist.** `20-VALIDATION.md` is still `status: draft`, `nyquist_compliant: false`; `gsd-validate-phase` section 6 should flip it. Not a Gate-1 blocker.
- **Device residue.** `svc power stayon true` was set per playbook D1 and left on (rig is always plugged). Both harness packages uninstalled; the launcher is now foreground (SecondBrain was foreground before the run and was not relaunched). The mavenLocal `1.10.0` AAR is the phase-20 HEAD build; the temporary `1.10.0-v251` baseline was deleted. Harness and v2.5.1 archive live only in the session scratchpad, never in the repo.
- **Prior verdicts audited.** No prior SELF-UAT existed for Phase 20; `20-VERIFICATION.md` (code-read, "5/5 verified", no gradle, no device) was treated as a claim. Re-observed fresh: its test list and counts reproduce, its "legacy block byte-identical" claim is now also backed by a pixel-identical A/B against a real v2.5.1 build, and no contradiction was found.

## Findings routed to gap-closure (if any)

None.

## Verdict

All 5 criteria PASS on the real tester device (harness against the phase-20 AAR, plus a fresh green Robolectric/detekt/apiCheck run) -> Gate-1 complete; human Gate-2 deferred to milestone completion (registered as `.planning/uat-pending/20-tone-foundation-atoms.md`).
