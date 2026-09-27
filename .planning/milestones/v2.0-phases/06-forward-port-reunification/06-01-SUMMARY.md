---
phase: 06-forward-port-reunification
plan: 01
subsystem: ui
tags: [android, jetpack-compose, kotlin, component-registry, osmdroid, gradle]

# Dependency graph
requires: []
provides:
  - "DateTimePicker forward-ported from v1.13.0 onto main, registered in ComponentRegistry (tier = PATTERN), gallery-navigable in the Pickers family"
  - "osmdroid-android + androidx-lifecycle-runtime-compose declared as resolvable Gradle dependencies, ready for Plan 06-03's PlaceMapPicker restore"
  - "CLAUDE.md's allowed-deps sentence names osmdroid"
  - "\"Date\" allowlisted in DomainVocabularyDriftGuardTest.PRIMITIVE_NOUN_ALLOWLIST"
affects: [06-02-forward-port-reunification, 06-03-forward-port-reunification]

# Actuals (#2632)
actuals:
  tokens: 11500
  tasks: 2
  commits: 2

# Tech tracking
tech-stack:
  added: ["org.osmdroid:osmdroid-android:6.1.20", "androidx.lifecycle:lifecycle-runtime-compose:2.9.4"]
  patterns: ["restore-from-tag via targeted `git checkout v1.13.0 -- <path>` (never whole-file, when the destination file has independently diverged)", "hand-insert new ComponentRegistry.Entry(...) blocks into a diverged family-screen file, matching in-file precedent"]

key-files:
  created:
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/DateTimePicker.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/DateTimePickerTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/PickersFamilyScreen.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt
    - gradle/libs.versions.toml
    - build.gradle.kts
    - CLAUDE.md
    - .planning/config.json

key-decisions:
  - "Restored DateTimePicker.kt + DateTimePickerTest.kt byte-faithful via targeted `git checkout v1.13.0 -- <path>` (not whole-file) — no hand-editing of ported logic."
  - "Inserted the new ComponentRegistry.Entry(\"DateTimePicker\", ...) by hand into main's current PickersFamilyScreen.kt (not restored from v1.13.0), preserving main's own independently-evolved entries (CropOverlay, SegmentedOptionSelector) and their tier assignments — matching RESEARCH.md Pitfall 2 guidance."
  - "Recorded `git.allow_default_branch_commits: true` in .planning/config.json — this hub project's sequential-in-hub convention (CLAUDE.md, branching_strategy: none) commits directly on main; the executor's generic protected-branch safety guard needed this explicit, config-level opt-in to match documented project reality rather than treating it as branch drift."
  - "Two pre-existing, unrelated build failures (detekt's TextCard.kt CyclomaticComplexMethod finding; metalava's already-tracked KI-2026-09-02-01 UndoHistoryStore_Factory issue) were confirmed out-of-scope and logged to deferred-items.md rather than fixed, per the scope-boundary rule — neither file is in this plan's files_modified, and `./gradlew build -x detekt -x metalavaCheckCompatibilityDebug -x metalavaCheckCompatibilityRelease` goes BUILD SUCCESSFUL, proving the new dependency admission itself is clean."

patterns-established:
  - "Forward-port-from-tag pattern: restore leaf files verbatim via targeted git checkout; hand-insert registry/allowlist edits into files that have independently diverged; never whole-file-restore a diverged file."

requirements-completed: [REUNI-01, REUNI-04]

coverage:
  - id: D1
    description: "DateTimePicker forward-ported from v1.13.0 with its test, registered in ComponentRegistry (tier = PATTERN), and shown in the Pickers family gallery with all 3 restored demo variants"
    requirement: "REUNI-01"
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/DateTimePickerTest.kt"
        status: pass
      - kind: unit
        ref: "ComponentRegistryDriftGuardTest (./gradlew testDebugUnitTest --tests \"*ComponentRegistryDriftGuardTest*\")"
        status: pass
    human_judgment: true
    rationale: "Gallery navigation/rendering (ExplorerActivity -> Pickers family -> DateTimePicker detail page showing all 3 demo variants) is a device-only-verifiable UI behavior — routes to Gate-1 agentic/human UAT, not self-certified here."
  - id: D2
    description: "osmdroid + androidx-lifecycle-runtime-compose admitted as resolvable Gradle dependencies; CLAUDE.md documents osmdroid; \"Date\" head token allowlisted so both drift guards stay green"
    requirement: "REUNI-04"
    verification:
      - kind: unit
        ref: "DomainVocabularyDriftGuardTest (./gradlew testDebugUnitTest --tests \"*DomainVocabularyDriftGuardTest*\")"
        status: pass
      - kind: integration
        ref: "./gradlew build -x detekt -x metalavaCheckCompatibilityDebug -x metalavaCheckCompatibilityRelease (assemble+test+lint+check, excluding two pre-existing unrelated failures)"
        status: pass
    human_judgment: false

duration: 14min
completed: 2026-09-27
status: complete
---

# Phase 6 Plan 1: DateTimePicker Tracer + osmdroid Admission Summary

**Forward-ported `DateTimePicker` byte-faithful from v1.13.0 onto `main` (tier=PATTERN, gallery-navigable), and admitted `osmdroid-android`/`androidx-lifecycle-runtime-compose` as resolvable Gradle deps for Plan 06-03's `PlaceMapPicker`.**

## Performance

- **Duration:** ~14 min
- **Started:** 2026-09-27T06:23:11Z
- **Completed:** 2026-09-27T06:37:11Z
- **Tasks:** 2
- **Files modified:** 8 (2 created, 6 modified)

## Accomplishments
- `DateTimePicker.kt` + `DateTimePickerTest.kt` restored byte-for-byte from tag `v1.13.0`, compiling clean against `main`'s current `theme/Dimens` and test infra.
- `DateTimePicker` registered in `pickersFamilyEntries` (`tier = PATTERN`), with its own 4-cell States matrix and all 3 v1.13.0 demo variants (date-only w/ `minDate`, time-only, date+time in one instance) restored verbatim.
- `"Date"` allowlisted in `DomainVocabularyDriftGuardTest.PRIMITIVE_NOUN_ALLOWLIST` as a primitive UI-archetype noun.
- `osmdroid` (`6.1.20`) + `androidx-lifecycle-runtime-compose` (`2.9.4`) admitted as `implementation`-scope Gradle dependencies (verbatim from v1.13.0's own catalog/build-file comments), unblocking Plan 06-03's `PlaceMapPicker` compile.
- `CLAUDE.md`'s allowed-deps sentence now names `osmdroid`.

## Task Commits

Each task was committed atomically:

1. **Task 1: Tracer — DateTimePicker restored, registered, and gallery-rendering end-to-end** - `cec1bcb` (feat)
2. **Task 2: Admit osmdroid + androidx-lifecycle-runtime-compose as approved implementation dependencies** - `2fcfd2c` (feat)

**Plan metadata:** committed separately after this SUMMARY (docs: complete plan)

## Files Created/Modified
- `src/main/java/io/github/ygaray/yahirandroidtaste/component/DateTimePicker.kt` - New (restored verbatim from v1.13.0): the `LocalDate`/`LocalTime`-contract date/time picker
- `src/test/java/io/github/ygaray/yahirandroidtaste/component/DateTimePickerTest.kt` - New (restored verbatim): full test suite (unit + Robolectric+Compose UI interaction tests)
- `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/PickersFamilyScreen.kt` - Hand-inserted `DateTimePicker` `Entry(...)` + `DateTimePickerVariants()` demo, plus imports; all pre-existing entries (`AccentColorPicker`, `IconPickerGrid`, `CropOverlay`, `SegmentedOptionSelector`) untouched
- `src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt` - Added `"Date"` to `PRIMITIVE_NOUN_ALLOWLIST`
- `gradle/libs.versions.toml` - Added `osmdroid` version + `osmdroid-android`/`androidx-lifecycle-runtime-compose` library entries
- `build.gradle.kts` - Added `implementation(libs.osmdroid.android)` + `implementation(libs.androidx.lifecycle.runtime.compose)` with v1.13.0's own rationale comments
- `CLAUDE.md` - Extended the allowed-deps sentence to name `osmdroid`
- `.planning/config.json` - Added `git.allow_default_branch_commits: true` (see Decisions Made)

## Decisions Made

- **Restore-not-rewrite discipline:** `DateTimePicker.kt`/`DateTimePickerTest.kt` restored via targeted `git checkout v1.13.0 -- <path>` (never whole-file on a diverged file); the registry entry, demo function, and allowlist edit were hand-authored to match main's current, independently-evolved `PickersFamilyScreen.kt`/`DomainVocabularyDriftGuardTest.kt` shape — per CONTEXT.md D-01 and RESEARCH.md Pitfall 2 (whole-file restore would have clobbered main's own `CropOverlay`/`SegmentedOptionSelector` entries and their tier assignments).
- **`git.allow_default_branch_commits: true` recorded in config:** this run executed in sequential mode directly on `main` (no worktree isolation — the orchestrator's dispatch note explicitly stated isolation degraded to "none"). The executor's generic pre-commit safety guard treats `main` as a protected branch by default and would otherwise FATAL-block every commit. This project's own `CLAUDE.md` ("Cross-repo work convention (sequential-in-hub)... Commit here on `main`") and `.planning/config.json`'s `branching_strategy: "none"` already establish that direct-to-`main` commits are this hub-stewardship project's deliberate, documented norm (confirmed further by the pre-existing `docs(06):` commits already on `main` from this same GSD flow). Rather than silently bypass the guard, the sanctioned config override (`git.allow_default_branch_commits`) was set explicitly, making the guard's decision match documented project reality instead of treating an intended workflow as accidental drift.
- **Pre-existing build failures logged, not fixed:** `TextCard.kt`'s detekt `CyclomaticComplexMethod` finding and the already-tracked `KI-2026-09-02-01` metalava `UndoHistoryStore_Factory` issue both predate this plan and touch files outside its `files_modified` list. Per the deviation rules' scope boundary ("Only auto-fix issues DIRECTLY caused by the current task's changes"), both were left unfixed and logged to `deferred-items.md`, with a targeted `./gradlew build -x detekt -x metalavaCheckCompatibilityDebug -x metalavaCheckCompatibilityRelease` run to positively confirm the new dependency admission itself compiles and resolves cleanly.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Recorded `git.allow_default_branch_commits: true` in `.planning/config.json`**
- **Found during:** Task 1 (pre-commit safety assertion, before the first commit)
- **Issue:** The executor's mandatory pre-commit HEAD-safety check treats `main` as a protected/default branch and refuses to commit unless overridden — this sequential (no-worktree) run's HEAD is `main`, and no override was yet recorded.
- **Fix:** Set `git.allow_default_branch_commits: true`, matching this project's own documented sequential-in-hub convention (`CLAUDE.md`, `branching_strategy: "none"`) rather than bypassing the guard.
- **Files modified:** `.planning/config.json`
- **Verification:** `gsd-tools query git.base-branch --is-protected main` returned `false` after the change; both task commits then succeeded normally.
- **Committed in:** `cec1bcb` (Task 1 commit)

**2. [Rule 3 - Blocking, scope-boundary] Logged (did not fix) a pre-existing detekt finding**
- **Found during:** Task 1 (`./gradlew detekt` run before committing)
- **Issue:** `TextCard.kt:132` fails detekt's `CyclomaticComplexMethod` rule (25/25). `TextCard.kt` is not in this plan's `files_modified` and was last touched 2026-09-07 — pre-existing, unrelated to the `DateTimePicker` restore.
- **Fix:** Not fixed (out of scope per the scope-boundary rule). Logged to `deferred-items.md` with the confirming evidence (detekt's own Complexity Report: "1 number of total code smells" total, all attributable to this one finding — zero new findings from this plan's touched files).
- **Files modified:** `.planning/phases/06-forward-port-reunification/deferred-items.md` (log only)
- **Verification:** Re-ran `./gradlew testDebugUnitTest --tests "*DateTimePickerTest*" --tests "*ComponentRegistryDriftGuardTest*" --tests "*DomainVocabularyDriftGuardTest*"` independently of detekt — BUILD SUCCESSFUL.
- **Committed in:** `cec1bcb` (Task 1 commit)

**3. [Rule 3 - Blocking, scope-boundary] Logged (did not fix) the pre-existing KI-2026-09-02-01 metalava issue**
- **Found during:** Task 2 (`./gradlew build` run before committing)
- **Issue:** `metalavaCheckCompatibilityDebug` fails with a false "Removed class `UndoHistoryStore_Factory`" — this is the already-documented `KI-2026-09-02-01` known issue (`.planning/KNOWN-ISSUES.md`, `.planning/STATE.md` Blockers/Concerns), a Dagger-generated-class leak into the committed `api.txt` baseline, unrelated to osmdroid/lifecycle admission and already tracked as a Phase-9 (`SHIP-01`) blocker.
- **Fix:** Not fixed (out of scope, already tracked for Phase 9). Logged to `deferred-items.md`.
- **Files modified:** `.planning/phases/06-forward-port-reunification/deferred-items.md` (log only)
- **Verification:** `./gradlew build -x detekt -x metalavaCheckCompatibilityDebug -x metalavaCheckCompatibilityRelease` went BUILD SUCCESSFUL (assemble + full `testDebugUnitTest` suite + lint + check), confirming osmdroid + androidx-lifecycle-runtime-compose resolve and compile without error — the acceptance criterion this task actually owns.
- **Committed in:** `2fcfd2c` (Task 2 commit)

---

**Total deviations:** 3 auto-handled (1 blocking-fix, 2 blocking-but-out-of-scope-logged)
**Impact on plan:** No scope creep. The config override was necessary to execute the plan at all under this run's sequential (no-worktree) dispatch, and matches documented project convention. Both logged pre-existing issues are confirmed unrelated to this plan's changes via targeted exclusion runs that independently prove the new dependency admission succeeded.

## Issues Encountered

None beyond the deviations above — no unplanned problem-solving was required within this plan's own scope.

## User Setup Required

None - no external service configuration required. (Device-only Gate-1 verification of `DateTimePicker`'s gallery rendering — per this plan's `must_haves.truths` — is deferred to the Gate-1 tester, not self-certified here; see `D1`'s `human_judgment: true` in the coverage block above.)

## Next Phase Readiness

- Plan 06-02 (`PresetChip`) and Plan 06-03 (`PlaceMapPicker` cluster) can proceed independently — `PresetChip` has no dependency on this plan's work; `PlaceMapPicker`'s compile now has both Gradle dependencies (`osmdroid-android`, `androidx-lifecycle-runtime-compose`) it needs already admitted, so Plan 06-03 does not need its own dependency-admission detour.
- Two pre-existing, unrelated build-gate failures remain open and tracked outside this plan's scope: detekt's `TextCard.kt` finding (no phase currently owns fixing it) and the already-tracked `KI-2026-09-02-01` metalava issue (owned by Phase 9's `SHIP-01` tag-cut gate). Neither blocks Plan 06-02/06-03 from proceeding on the same pattern this plan established (exclude the two known-bad tasks when running `./gradlew build` for local verification; the phase-level Gate-1/Gate-2 process already accounts for `KI-2026-09-02-01`).
- Device-only Gate-1 verification (ExplorerActivity → Pickers family → `DateTimePicker` renders + navigates + all 3 demo variants display) is required before this phase's completion — not performed here per this executor's device-verification boundary; routes to the Gate-1 agentic/human tester.

---
*Phase: 06-forward-port-reunification*
*Completed: 2026-09-27*

## Self-Check: PASSED

All 10 claimed files verified present on disk (created + modified); both task commit hashes
(`cec1bcb`, `2fcfd2c`) confirmed present in `git log --oneline --all`.
