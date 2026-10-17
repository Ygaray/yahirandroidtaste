---
phase: 28-talkback-sweep-explorer-demos
plan: 03
subsystem: explorer
tags: [compose, theme, dynamic-color, explorer, guard-test]
requires:
  - phase: 28-02
    provides: placeholder tile purge (prior wave, unrelated to the theme pin)
provides:
  - "internal ExplorerTheme(themeMode, content) that pins every Explorer page to the static hub scheme"
  - "ExplorerSchemePinGuardTest source-scan guard against direct library-theme wraps"
affects: [28-05, 28-07]
tech-stack:
  added: []
  patterns: ["single internal theme helper + comment-stripped source-scan guard"]
key-files:
  created:
    - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ExplorerTheme.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ExplorerThemeTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ExplorerSchemePinGuardTest.kt
  modified:
    - ExplorerActivity.kt, ExplorerEntry.kt, ComponentDetailScreen.kt, ExplorerIndexScreen.kt, TokenBrowserScreen.kt
    - CardsFamilyScreen.kt, ChipsFamilyScreen.kt, SheetsFamilyScreen.kt, ButtonsFabFamilyScreen.kt, PickersFamilyScreen.kt
    - FeedbackFamilyScreen.kt, EmptyStateFamilyScreen.kt, ProgressFamilyScreen.kt, TactileFoundationFamilyScreen.kt, VoiceCommandFamilyScreen.kt
    - ReleaseDemos.kt (HubSchemeScope now delegates to ExplorerTheme)
key-decisions:
  - "PD-1: ExplorerActivity wraps with ExplorerTheme(ThemeMode.SYSTEM)"
  - "PD-2: detail/{component} not-found EmptyState wrapped as ExplorerTheme(themeMode) { EmptyState(...) }"
  - "PD-3: HubSchemeScope kept, delegates to ExplorerTheme, KDoc updated"
status: complete
plan_head_before: ebd23b5ed6f14e8a9f6f56b0b9c63ffad3ea7a2c
commits: 3
actuals:
  tokens: 9000
  tasks: 3
  commits: 3
---

# Phase 28 Plan 03: Explorer hub scheme pin Summary

Every Explorer page, the Activity and the not-found fallback now render through an internal `ExplorerTheme` helper (`YahirAndroidTasteTheme(dynamicColor = false)`), so the warn tone is never the wallpaper tone; a non-vacuous source-scan guard keeps it that way.

## Tasks

| Task | Name | Commit |
|------|------|--------|
| 1 | ExplorerTheme helper, Activity / Entry fallback / detail path, ExplorerThemeTest | e477a78 |
| 2 | Pin index, token browser and five family screens | f7714ef |
| 3 | Pin last five screens + HubSchemeScope, add ExplorerSchemePinGuardTest | 778c307 |

## Verification

- ExplorerThemeTest: 5 tests, 0 failures. The four plan-named cases are present (`systemMode_followsTheSystemDarkSetting_withTheStaticHubScheme` runs under the `night` qualifier; an extra `..._whenNotNight` case covers `notnight`). Tracer red: before `ExplorerTheme` existed the test source failed to compile (`compileDebugUnitTestKotlin FAILED`).
- ExplorerSchemePinGuardTest: 4 tests, 0 failures after migration.
- Teeth proof (guard run BEFORE migrating the remaining files): `noExplorerFileOutsideTheHelperWrapsTheLibraryTheme` failed naming exactly `[EmptyStateFamilyScreen.kt, FeedbackFamilyScreen.kt, ProgressFamilyScreen.kt, ReleaseDemos.kt, TactileFoundationFamilyScreen.kt, VoiceCommandFamilyScreen.kt]`, and `everyExpectedScreenCallsTheHelper` failed naming the same six files. Green after the edits.
- ReceiptDemosRegistryRenderTest (14 tests, includes the unedited HubSchemeScope test), ComponentStatesMatrixTest, ComponentPlaygroundIntegrityTest, GalleryDemoInteractionTest, TokenBrowserContentTest, CatalogDocsDriftGuardTest, DomainVocabularyDriftGuardTest, ComponentRegistryDriftGuardTest, LibraryInvariantsGuardTest: all 0 failures. The drift-guard batch was run with `--rerun-tasks`.
- `./gradlew apiDump --no-daemon` succeeded and `git status` shows `api.txt` untouched (helper is internal). `./gradlew detekt --no-daemon` green at zero baseline.
- Not done here by design: the TESTER screencap (Gate-1 G1-1, plan 28-07) and the hoisted OutcomeSheet `hide()` demo (plan 28-05).

## Deviations from Plan

None in behavior. Minor notes:
- Test method for the system-mode case is split in two (night, notnight) because a Robolectric qualifier is per method; the plan allowed "a parameterized pair".
- The token swatch colors, screen content, tags, routes and registry counts were not touched.

## Self-Check: PASSED

- ExplorerTheme.kt, ExplorerThemeTest.kt, ExplorerSchemePinGuardTest.kt exist; commits e477a78, f7714ef, 778c307 present on the worktree branch.
