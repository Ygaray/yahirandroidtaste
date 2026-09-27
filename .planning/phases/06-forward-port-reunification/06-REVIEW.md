---
phase: 06-forward-port-reunification
reviewed: 2026-09-27T00:00:00Z
depth: standard
files_reviewed: 16
files_reviewed_list:
  - CLAUDE.md
  - api.txt
  - build.gradle.kts
  - gradle/libs.versions.toml
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/DateTimePicker.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapOsmdroidConfig.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapPicker.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapPickerModel.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/PresetChip.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ChipsFamilyScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/PickersFamilyScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/SavedPlaceUiModel.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/DateTimePickerTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapOsmdroidConfigTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapPickerModelTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapPickerTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/PresetChipTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/SourceContractTestSupport.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt
findings:
  critical: 0
  warning: 2
  info: 2
  total: 4
status: issues_found
---

# Phase 06: Code Review Report

**Reviewed:** 2026-09-27T00:00:00Z
**Depth:** standard
**Files Reviewed:** 16 (production + test), plus CLAUDE.md/build.gradle.kts/api.txt/libs.versions.toml
**Status:** issues_found (no blockers; two maintainability warnings, two info items)

## Summary

This phase forward-ports `DateTimePicker`, `PlaceMapPicker` (+ its pure-Kotlin model math and the
osmdroid tile-policy config step), and the `PresetChip` `contentDescription` overload from the
`v1.13.0` tag into the renamed `io.github.ygaray.yahirandroidtaste` package, plus the corresponding
`ComponentRegistry` entries, gallery demos, and their test suites.

The ported code is unusually well-defended: extensive KDoc documents prior review rounds (163/164)
and the specific pitfalls each guard clause exists to prevent (day-shift-safe UTC round-tripping,
Web-Mercator-limit camera clamping, antimeridian clamp-not-wrap, idempotent `MapView`
resume/pause, freshness via `rememberUpdatedState`, echo-suppression for the camera refit, no
device-location/geocoding/logging/coordinate-rendering in the hub). I traced every public
composable's parameter contract against `api.txt` (all signatures match exactly, no ABI drift),
traced the camera-refit echo-suppression state machine (`shouldRefitCamera` /
`lastEmittedRadiusMeters` / `lastFittedCamera`) through several multi-step scenarios (handle-drag
echo, saved-place external change, pin-change) without finding a divergence from the documented
contract, and cross-checked the new `DateTimePicker`/`PresetChip`/`PlaceMapPicker` head-token
entries against `DomainVocabularyDriftGuardTest`'s allowlist/`DOMAIN_VOCABULARY` map (both
consistent). No BLOCKER-level defects, security issues, or logic errors were found in the reviewed
diff. The two WARNING items below are both toolchain/doc-hygiene issues, not runtime defects.

## Warnings

### WR-01: CLAUDE.md's documented Compose BOM version has drifted from the actual pin

**File:** `CLAUDE.md:64`
**Issue:** The root `CLAUDE.md` (the authoritative toolchain doc per its own charter) states:
`Compose BOM 2026.02.01`. The actual pin in `gradle/libs.versions.toml:12` is
`composeBom = "2026.04.01"` — a different minor version. This diff touched `CLAUDE.md` (adding
`osmdroid` to the one-way-dependency list at line 24-25) without correcting the adjacent stale BOM
figure, so a reader now gets an incorrect toolchain fact from the file that's supposed to be the
single source of truth. (Confirmed via `git log`/`git diff` that the BOM bump to `2026.04.01`
predates this phase — commit `449c4b1` — so this drift is pre-existing, but this phase's edit to
the same doc was a chance to fix it and didn't.)
**Fix:**
```diff
- Compose BOM **2026.02.01** / JDK **17**,
+ Compose BOM **2026.04.01** / JDK **17**,
```

### WR-02: `androidx-lifecycle-runtime-compose` hardcodes its version, breaking the catalog's own single-source-of-truth convention

**File:** `gradle/libs.versions.toml:41`
**Issue:** Every other entry in `[libraries]` resolves its version via `version.ref = "..."` against
the `[versions]` block (e.g. `robolectric`, `osmdroid`, `hilt-android` two lines above). The new
`androidx-lifecycle-runtime-compose` entry instead hardcodes a literal: `version = "2.9.4"`. The
inline comment justifies this as "the version already selected transitively," but that's exactly
the kind of pin that silently goes stale: if the transitively-resolved lifecycle version changes
later (e.g. via a Compose BOM bump), this literal is easy to forget and no `[versions]` entry
signals it needs revisiting.
**Fix:**
```toml
[versions]
lifecycleRuntimeCompose = "2.9.4"

[libraries]
androidx-lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycleRuntimeCompose" }
```

## Info

### IN-01: `PlaceMapPicker.kt` mixes several concerns in one ~900-line file

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapPicker.kt`
**Issue:** The file combines the public composable, its private section composables, osmdroid
`MapView` construction/lifecycle glue, two `Marker.OnMarkerDragListener` implementations, and
drawable-icon builders. This is a code-organization/readability observation only — the module's
own detekt zero-baseline policy has evidently already accepted this file's size/complexity (per
the phase's own "detekt green" governance-battery commit), so this is not a new regression, just
worth a note if a future phase wants to split view-glue from the public composable.
**Fix:** Optional: extract the `Marker.OnMarkerDragListener` implementations and the icon-builder
functions into a sibling `PlaceMapPickerOverlays.kt` file within the same package, purely for
navigability — no behavior change.

### IN-02: Redundant blank-check duplicated between the gallery demo and the hub's own gate

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/PickersFamilyScreen.kt:362-367`
**Issue:** `PlaceMapPickerDemo`'s `onSearch` lambda re-checks `if (query.isNotBlank())` before
setting the fixture pin, even though `PlaceMapPicker` itself already gates the search commit
through `canSubmitSearch` (blank/whitespace-only queries never reach `onSearch` at all — see
`PlaceMapPicker.kt`'s `PlaceMapSearchSection`). Harmless (defense in depth in demo-only code), but
it's dead logic in this call site specifically, since `onSearch` is documented to never fire for a
blank query.
**Fix:** Optional simplification — drop the inner `if` and always set the fixture coordinates,
relying on the hub's own gate:
```kotlin
onSearch = {
    // A second explorer fixture coordinate for any (necessarily non-blank) search query.
    latitude = 51.5
    longitude = -0.12
},
```

---

_Reviewed: 2026-09-27T00:00:00Z_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
