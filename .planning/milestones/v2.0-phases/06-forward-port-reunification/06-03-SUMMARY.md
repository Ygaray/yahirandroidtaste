---
phase: 06-forward-port-reunification
plan: 03
subsystem: ui
tags: [android, jetpack-compose, kotlin, component-registry, osmdroid]

# Dependency graph
requires: ["06-01", "06-02"]
provides:
  - "PlaceMapPicker cluster (PlaceMapPicker, PlaceMapOsmdroidConfig, PlaceMapPickerModel, model.SavedPlaceUiModel) forward-ported from v1.13.0 onto main, registered in ComponentRegistry (tier = PATTERN), gallery-navigable in the Pickers family"
  - "\"PlaceMapPicker\" allowlisted in DomainVocabularyDriftGuardTest.DOMAIN_VOCABULARY"
  - "Phase 6's phase-closing governance battery green: full test suite, both drift guards, apiCheck (net-additive api.txt: DateTimePicker, PlaceMapPicker, PresetChip's 2 overloads), publishReleasePublicationToMavenLocal"
affects: []

# Actuals (#2632)
actuals:
  tokens: 38000
  tasks: 3
  commits: 3
plan_head_before: 9b857f62457f32ddd123c0868ee617f992061804

# Tech tracking
tech-stack:
  added: []
  patterns: ["restore-from-tag via targeted `git checkout v1.13.0 -- <path>` (never whole-file, when the destination file has independently diverged)", "hand-insert new ComponentRegistry.Entry(...) blocks into a diverged family-screen file, matching in-file precedent", "restore a missing shared test-helper method (functionBody) onto a test-support object whose main copy predated the v1.x-line addition, rather than editing the restored test to avoid it"]

key-files:
  created:
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapPicker.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapOsmdroidConfig.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapPickerModel.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/model/SavedPlaceUiModel.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapPickerTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapOsmdroidConfigTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapPickerModelTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/PickersFamilyScreen.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/SourceContractTestSupport.kt
    - api.txt
    - .planning/phases/06-forward-port-reunification/deferred-items.md

key-decisions:
  - "Restored PlaceMapPicker.kt, PlaceMapOsmdroidConfig.kt, PlaceMapPickerModel.kt (stays under component/, not model/, per RESEARCH.md Pitfall 1), and model/SavedPlaceUiModel.kt byte-faithful via targeted `git checkout v1.13.0 -- <path>` — no hand-editing of ported logic."
  - "Restored the missing `functionBody` helper (+ its private balancing helpers findDeclarationStart/balanceFrom/findBodyStart) onto main's SourceContractTestSupport.kt (Rule 3 blocking fix, not in this plan's files_modified): main's copy of this shared test-support object predated this method's addition on the v1.x line, and the restored PlaceMapPickerTest.kt's own source-contract assertions call it directly — without it the module does not compile. Restored verbatim from v1.13.0's version, translated to main's own em-dash KDoc style for consistency with the surrounding file."
  - "Inserted the new ComponentRegistry.Entry(\"PlaceMapPicker\", ...) by hand into main's current PickersFamilyScreen.kt (not restored from v1.13.0), alongside the DateTimePicker entry added in Plan 06-01, preserving main's own independently-evolved entries (AccentColorPicker, IconPickerGrid, CropOverlay, SegmentedOptionSelector) and their tier assignments — matching RESEARCH.md Pitfall 2 guidance and the pattern established in Plans 06-01/06-02."
  - "PlaceMapPickerDemo()/PlaceMapPickerVariants() restored verbatim from v1.13.0's own gallery demo code, including the fixture-coordinate-never-shown-as-text convention (T-164-04)."
  - "Re-confirmed (did not re-fix) the pre-existing TextCard.kt detekt CyclomaticComplexMethod finding already logged in Plan 06-01/06-02's deferred-items.md — detekt's own Complexity Report shows exactly 1 total code smell after this plan's restore, confirming zero new findings from this plan's touched files."
  - "Ran apiDump/apiCheck against the metalava Release variant (metalavaCheckCompatibilityRelease), which is green; the already-tracked KI-2026-09-02-01 (metalavaCheckCompatibilityDebug, the `./gradlew build` task-chain variant) was not re-triggered by this plan's targeted governance-battery command and remains an unrelated Phase 9 (SHIP-01) blocker."

patterns-established:
  - "When a byte-faithful tag restore's test depends on a shared test-support helper that main's independently-evolved copy lacks, restore the missing method(s) onto the shared helper (Rule 3) rather than editing the restored test — preserves the ported test's own byte-faithfulness."

requirements-completed: [REUNI-02, REUNI-04]

coverage:
  - id: D1
    description: "PlaceMapPicker cluster (PlaceMapPicker, PlaceMapOsmdroidConfig, PlaceMapPickerModel, SavedPlaceUiModel) forward-ported from v1.13.0 with its 3 tests, registered in ComponentRegistry (tier = PATTERN), and shown in the Pickers family gallery with both restored demo variants (tap-to-drop-pin/drag-handle, errors-and-resolving)"
    requirement: "REUNI-02"
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapPickerTest.kt, PlaceMapOsmdroidConfigTest.kt, PlaceMapPickerModelTest.kt (./gradlew testDebugUnitTest --tests \"*PlaceMapPickerTest*\" --tests \"*PlaceMapOsmdroidConfigTest*\" --tests \"*PlaceMapPickerModelTest*\")"
        status: pass
      - kind: unit
        ref: "ComponentRegistryDriftGuardTest (./gradlew testDebugUnitTest --tests \"*ComponentRegistryDriftGuardTest*\")"
        status: pass
    human_judgment: true
    rationale: "Gallery navigation/rendering (ExplorerActivity -> Pickers family -> PlaceMapPicker detail page, including the live MapView tile-paint behavior per D-03) is device-only-verifiable — routes to Gate-1 agentic/human UAT, not self-certified here."
  - id: D2
    description: "\"PlaceMapPicker\" allowlisted in DOMAIN_VOCABULARY so both drift guards stay green; phase-closing governance battery (full test suite, both drift guards, detekt zero-new-findings, apiCheck net-additive, publishReleasePublicationToMavenLocal) green in one pass"
    requirement: "REUNI-04"
    verification:
      - kind: unit
        ref: "DomainVocabularyDriftGuardTest (./gradlew testDebugUnitTest --tests \"*DomainVocabularyDriftGuardTest*\")"
        status: pass
      - kind: integration
        ref: "./gradlew testDebugUnitTest detekt apiDump apiCheck publishReleasePublicationToMavenLocal (detekt: 1 pre-existing, unrelated TextCard.kt finding, zero new; apiCheck: net-additive, zero removals; publish: BUILD SUCCESSFUL)"
        status: pass
    human_judgment: false

duration: 10min
completed: 2026-09-27
status: complete
---

# Phase 6 Plan 3: PlaceMapPicker Forward-Port + Phase-Closing Governance Battery Summary

**Forward-ported the `PlaceMapPicker` cluster (4 files + 3 tests) byte-faithful from v1.13.0 onto `main` (tier=PATTERN, gallery-navigable with a live `MapView` surface), and closed out Phase 6 with a green full-suite governance battery (tests, detekt, apiCheck net-additive, publish).**

## Performance

- **Duration:** ~10 min
- **Started:** 2026-09-27T06:46:36Z
- **Completed:** 2026-09-27T06:53:29Z
- **Tasks:** 3
- **Files modified:** 12 (7 created, 5 modified)

## Accomplishments
- `PlaceMapPicker.kt`, `PlaceMapOsmdroidConfig.kt`, `PlaceMapPickerModel.kt` (component/), and `SavedPlaceUiModel.kt` (model/) restored byte-for-byte from tag `v1.13.0`, along with their 3 tests — compiling clean against `main`'s current `theme/Dimens`, `ChipBar` (same-package saved-places `itemContent`), and `PresetChip` (Plan 06-02).
- Restored the missing `functionBody` shared test-helper (+ private balancing helpers) onto `main`'s `SourceContractTestSupport.kt` — `main`'s copy predated this method's addition on the v1.x line, and the restored `PlaceMapPickerTest.kt`'s own source-contract assertions require it to compile.
- `PlaceMapPicker` registered in `pickersFamilyEntries` (`tier = PATTERN`), alongside `DateTimePicker`, with its own 4-cell States matrix and both v1.13.0 demo variants ("tap to drop a pin, drag the handle or slide to resize"; "errors and resolving") restored verbatim.
- `"PlaceMapPicker"` allowlisted in `DomainVocabularyDriftGuardTest.DOMAIN_VOCABULARY` (full composable name, per its domain-vocabulary rationale — contrast with `PRIMITIVE_NOUN_ALLOWLIST`'s bare head-token shape).
- Phase 6's phase-closing governance battery run in one pass: full `testDebugUnitTest` suite (all 5 restored tests across the 3 plans + both drift guards) green; `detekt` shows only the pre-existing, unrelated `TextCard.kt` finding (zero new); `apiDump`/`apiCheck` confirm `api.txt` is net-additive (`DateTimePickerKt`, `PlaceMapPickerKt`, `PresetChipKt`'s 2 overloads added, zero removals); `publishReleasePublicationToMavenLocal` succeeded.

## Task Commits

Each task was committed atomically:

1. **Task 1: Restore the PlaceMapPicker cluster (4 source files + 3 tests) from v1.13.0** - `d9fd663` (feat)
2. **Task 2: Register PlaceMapPicker in PickersFamilyScreen.kt; add to DOMAIN_VOCABULARY** - `c17086c` (feat)
3. **Task 3: Phase-closing governance battery — tests, detekt, apiCheck, publish** - `054b79a` (docs)

**Plan metadata:** committed separately after this SUMMARY (docs: complete plan)

## Files Created/Modified
- `src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapPicker.kt` - New (restored verbatim from v1.13.0): the pin/radius/search/saved-places map picker composable, embedding `ChipBar` + `PresetChip` and a live osmdroid `MapView`
- `src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapOsmdroidConfig.kt` - New (restored verbatim): `internal fun configureOsmdroid` (cacheDir tiles, user-agent), called only from `PlaceMapPicker`'s private `createPlaceMapView()`
- `src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapPickerModel.kt` - New (restored verbatim): pure `kotlin.math` distance/bearing/radius helpers
- `src/main/java/io/github/ygaray/yahirandroidtaste/model/SavedPlaceUiModel.kt` - New (restored verbatim): the 4-field saved-place data class + chip-label helpers
- `src/test/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapPickerTest.kt` - New (restored verbatim): full Robolectric+Compose UI test suite, including source-contract assertions
- `src/test/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapOsmdroidConfigTest.kt` - New (restored verbatim)
- `src/test/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapPickerModelTest.kt` - New (restored verbatim)
- `src/test/java/io/github/ygaray/yahirandroidtaste/component/SourceContractTestSupport.kt` - Restored the missing `functionBody` helper + private balancing helpers (Rule 3 blocking fix)
- `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/PickersFamilyScreen.kt` - Hand-inserted `PlaceMapPicker` `Entry(...)` + `PlaceMapPickerDemo()`/`PlaceMapPickerVariants()` demo, plus imports; all pre-existing entries untouched
- `src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt` - Added `"PlaceMapPicker"` to `DOMAIN_VOCABULARY`
- `api.txt` - Regenerated via `apiDump`; net-additive (`DateTimePickerKt`, `PlaceMapPickerKt`, `PresetChipKt`, zero removals)
- `.planning/phases/06-forward-port-reunification/deferred-items.md` - Logged re-confirmation of the pre-existing `TextCard.kt` detekt finding and the unrelated `KI-2026-09-02-01` metalava Debug-variant issue

## Decisions Made

- **Restore-not-rewrite discipline:** all 4 component/model files + 3 tests restored via targeted `git checkout v1.13.0 -- <path>` (never whole-file on a diverged file) — no hand-editing of ported logic.
- **Rule 3 blocking fix — restored a missing shared test-helper method rather than editing the restored test:** `PlaceMapPickerTest.kt`'s source-contract assertions call `SourceContractTestSupport.functionBody(...)`, a method that exists on v1.13.0's copy of this shared JVM test-support object but not on `main`'s (main's copy predates this method's addition on the v1.x line — confirmed via `git diff` against the v1.13.0 version, which showed only this method + its 3 private helpers as the delta). Restored the method verbatim (translated to main's own em-dash KDoc convention) rather than rewriting the ported test to avoid calling it — preserves `PlaceMapPickerTest.kt`'s own byte-faithfulness, the more valuable invariant.
- **Entry insertion alongside `DateTimePicker`, not `git checkout`:** the new `PlaceMapPicker` `Entry(...)` was hand-inserted into main's current, independently-evolved `PickersFamilyScreen.kt` (which already carries Plan 06-01's `DateTimePicker` entry plus main's own `CropOverlay`/`SegmentedOptionSelector`), per RESEARCH.md Pitfall 2 and this plan's own prohibition against whole-file restore.
- **detekt/metalava pre-existing-issue disposition unchanged from 06-01/06-02:** re-ran and re-confirmed (did not re-fix) the same `TextCard.kt` finding and the same `KI-2026-09-02-01` Debug-variant metalava issue — both remain out of this plan's scope (neither file is in `files_modified`), consistent with the scope-boundary rule and the precedent both prior plans established.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Restored the missing `functionBody` helper onto `SourceContractTestSupport.kt`**
- **Found during:** Task 1 (`./gradlew testDebugUnitTest --tests "*PlaceMapPickerTest*" ...` — first compile attempt)
- **Issue:** `compileDebugUnitTestKotlin` failed with 6 "Unresolved reference 'functionBody'" errors in the restored `PlaceMapPickerTest.kt`. `main`'s `SourceContractTestSupport.kt` (an existing shared test-support object, not new to this plan) lacked the `functionBody` method (plus its private `findDeclarationStart`/`balanceFrom`/`findBodyStart` helpers) that v1.13.0's copy of the same file has — a shared-utility divergence the plan's own research didn't anticipate (RESEARCH.md scoped only the 6+5 restore-target files and the 3 registration/allowlist edits, not this shared helper).
- **Fix:** Restored the missing method + its 3 private helpers verbatim from `v1.13.0`'s `SourceContractTestSupport.kt`, adapted only to match `main`'s own em-dash KDoc punctuation convention (the rest of the file already uses `—`, v1.13.0's addition used `--`) for stylistic consistency with the surrounding, unrelated file content.
- **Files modified:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/SourceContractTestSupport.kt`
- **Verification:** Re-ran `./gradlew testDebugUnitTest --tests "*PlaceMapPickerTest*" --tests "*PlaceMapOsmdroidConfigTest*" --tests "*PlaceMapPickerModelTest*"` — BUILD SUCCESSFUL, all 3 tests pass.
- **Committed in:** `d9fd663` (Task 1 commit)

---

**Total deviations:** 1 auto-fixed (1 blocking)
**Impact on plan:** No scope creep — the fix restores an existing shared test-helper's missing method (itself a verbatim v1.13.0 restoration), required for the plan's own designated restore-target test to compile. No production/library code was touched.

## Issues Encountered

None beyond the deviation above. Both drift guards, the full `testDebugUnitTest` suite, `apiCheck`, and `publishReleasePublicationToMavenLocal` all passed on their first run once the `functionBody` fix landed.

## User Setup Required

None - no external service configuration required. (Device-only Gate-1 verification of `PlaceMapPicker`'s gallery rendering and live `MapView` tile-paint behavior — per this plan's `must_haves.truths` D-03 backstop items — is deferred to the Gate-1 tester, not self-certified here; see `D1`'s `human_judgment: true` in the coverage block above.)

## Next Phase Readiness

- Phase 6 (Forward-Port Reunification) is now code-complete: all 3 plans (`DateTimePicker`+osmdroid admission, `PresetChip`, `PlaceMapPicker` cluster) landed on `main`, all governance gates green in one final pass (full test suite, both drift guards, detekt zero-new-findings, apiCheck net-additive, publish succeeds).
- Two pre-existing, unrelated issues remain open, tracked outside this phase's scope: detekt's `TextCard.kt` `CyclomaticComplexMethod` finding (no phase currently owns fixing it) and the already-tracked `KI-2026-09-02-01` metalava Debug-variant issue (owned by Phase 9's `SHIP-01` tag-cut gate — confirmed this plan's targeted `apiCheck` run uses the Release variant and is unaffected).
- Device-only Gate-1 verification is required before this phase's completion: `ExplorerActivity` -> Pickers family -> `PlaceMapPicker` renders + navigates + both demo variants display, including confirming the live `MapView` tile surface actually paints in on-device (D-03's two backstop truths) — not performed here per this executor's device-verification boundary; routes to the Gate-1 agentic/human tester, alongside the already-pending Gate-1 checks for `DateTimePicker` (06-01) and `PresetChip` (06-02).
- Phase 9 (Ship) can proceed once Gate-1 confirms all three composables on-device: cut the `v2.2.0` tag (human-gated), coordinate the consumer repin, and reconcile the hub's ECOSYSTEM.md matrix.

---
*Phase: 06-forward-port-reunification*
*Completed: 2026-09-27*

## Self-Check: PASSED

All 12 claimed files verified present on disk (7 created + 5 modified); all 3 task commit hashes
(`d9fd663`, `c17086c`, `054b79a`) confirmed present in `git log --oneline --all`.
