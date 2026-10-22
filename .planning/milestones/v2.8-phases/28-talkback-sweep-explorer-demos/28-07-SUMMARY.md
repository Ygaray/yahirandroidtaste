---
phase: 28-talkback-sweep-explorer-demos
plan: 07
subsystem: testing
tags: [hygiene-contract, source-contract-test, adb-oracle, fake-adb, phase-gate, gate-1]
requires:
  - phase: 28-01
    provides: PlaceMapOsmdroidConfig agent resolution and the single placeholder detection constant
  - phase: 28-02
    provides: purgePlaceholderTiles, the hub-owned install flag, tools/verify-osm-tile-cache.py
  - phase: 28-03
    provides: ExplorerTheme (dynamicColor = false pin)
  - phase: 28-06
    provides: Layer-1 TalkBack sweep tests and the merged openable Undo Center row
provides:
  - Phase28HygieneSourceContractTest (plain JVM, 7 tests)
  - tools/verify-tester-restored.sh (read-only TESTER restore oracle) and its fake-adb fixture test
  - The fresh, forced full phase gate record (below)
  - The Gate-1 contract (in 28-07-PLAN.md verification) that the orchestrator's separate Gate-1 tester runs
affects: [28-08, gate-1, phase-29]
tech-stack:
  added: []
  patterns:
    - "Hygiene contracts assert invariants (absence of forbidden identifiers, one literal, one warn call), never an exact file hash or a load() count, so internal edits of the config file stay legal"
    - "Read-only adb oracle fixture-tested with a fake adb that logs every call"
key-files:
  created:
    - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/Phase28HygieneSourceContractTest.kt
    - tools/verify-tester-restored.sh
    - tools/test/test-verify-tester-restored.sh
  modified: []
key-decisions:
  - "The hygiene test strips block comments (regex) before SourceContractTestSupport.stripComments, because single-line KDoc lines such as '/** ... */' survive the shared helper"
  - "The oracle gained two optional expectations (--a11y-enabled, --touch-exploration, default null) so the gate can pass recorded values for accessibility_enabled and touch_exploration_enabled exactly as PD-3 describes for the services setting"
  - "PlaceMapOsmdroidConfig assertions are tolerant of plan 28-08: no load() count, no file hash; only the invariants named in the plan"
requirements-completed: [A11Y-02, DEMO-02, DEMO-03, POL-03]
duration: 30min
completed: 2026-10-17
status: complete
commits: 2
plan_head_before: 391d1d934c49d04160ce4af0c41a3683c1662835
actuals:
  tokens: 9000
  tasks: 3
  commits: 2
---

# Phase 28 Plan 07: Phase hygiene contract, TESTER restore oracle and the fresh phase gate Summary

**A plain-JVM hygiene contract pins the POL-03 and Explorer promises on every test run, a read-only USB-only oracle verifies the TESTER restore step, and the forced fresh full gate passes with api.txt unchanged since c1681ff.**

## Performance

- **Tasks:** 3 of 3 (Task 3 is verification only, no commit)
- **Commits:** 2 task commits (plus this SUMMARY commit)

## Accomplishments

- `Phase28HygieneSourceContractTest` (7 tests, 0 failures): the config file names no whole-cache wipe, no cache-manager type, no expiration or expiry override, no persisted request-property key family and no string preference write (the install flag is a boolean write); exactly one `com.example.` literal exists in all main code, inside `PlaceMapOsmdroidConfig.kt`; the config logs exactly one warning and no other level, prints nothing; the picker and every `explorer/*.kt` never log or print; `PlaceMapOsmdroidConfig.kt`, `ExplorerTheme.kt` and `ReleaseDemos.kt` declare nothing public at top level; `ExplorerTheme` passes `dynamicColor = false`; the picker names no request-property or purge call. Forbidden identifiers are assembled from fragments.
- `tools/verify-tester-restored.sh`: reads density (override when present, physical otherwise), font scale (null equals 1.0), enabled_accessibility_services, accessibility_enabled, touch_exploration_enabled and exact-line package presence through `adb -s <serial>` getters only. Refuses a `host:port` serial with exit 2 before any adb call; exit 0 restored, 1 differs, 2 usage or unreadable device. Prints `OK`/`FAIL` per check and `RESTORED=yes|no`.
- `tools/test/test-verify-tester-restored.sh`: fake adb that logs every call; 34 checks (PASS=34 FAIL=0) covering restored, override 480, override 450 over a different physical density, physical-only mismatch, font scale 2.0 / 1.0 / recorded 1.15, services set / recorded, accessibility_enabled 1, touch_exploration 1, null and empty values, installed package, a longer package name that is not a match, offline device, unparsable density, wireless serial with an empty adb log, unknown option, missing option value, and no state-changing verb in the adb log of any run.

## Task Commits

1. **Task 1: hygiene contract** - `b2765a7` (test) - lane 1, additive
2. **Task 2: restore oracle + fixture test** - `32bd7d5` (chore) - lane 1, additive
3. **Task 3: fresh full phase gate** - verification only, recorded below

## Fresh phase gate record (Task 3)

Run in this worktree (clean `git status --short src tools` before and after), one Gradle invocation at a time, `--no-daemon`, forced fresh.

| Command | Result |
|---|---|
| `./gradlew apiDump testDebugUnitTest detekt apiCheck --rerun-tasks --no-build-cache --no-daemon` | BUILD SUCCESSFUL in 5m 32s, 44 actionable tasks, 44 executed. `:testDebugUnitTest`, `:detekt`, `:metalavaCheckCompatibilityRelease`, `:apiDump`, `:apiCheck` all executed (none up-to-date). Executed unit tests: 179 suites, 1855 tests, 22 skipped, 0 failures, 0 errors. |
| `git diff --exit-code api.txt` | exit 0 (apiDump regenerated an identical api.txt) |
| `git diff --quiet c1681ff HEAD -- api.txt` | exit 0 (Phase 28 did not change api.txt) |
| `bash tools/verify-api-source-additive.sh v2.7.1` | `API SOURCE-ADDITIVE OK baseline=v2.7.1 removed_classes=0 removed_members=0 enum_constants_intact=73 required_param_changes=0 added_classes=5 added_members=48 signature_changes=0 accepted_exceptions=0` |
| `bash tools/test/run-all.sh` | exit 0; every suite ok: classify 12, precommit 6, additive-diff 5, api-source-additive 34, binary-abi 52, osm-tile-cache 15, tester-restored 34 (all FAIL=0) |
| `git diff --quiet c1681ff HEAD -- feedback/FeedbackCard.kt feedback/FeedbackStackHost.kt component/OutcomeSheet.kt` | exit 0 (locked files unchanged since the baseline) |
| `git tag --list 'v2.8*'` | prints nothing (no tag cut) |
| `git log --name-only --format= c1681ff..HEAD -- <debris paths>` | prints nothing (no working-tree debris in Phase 28 commits) |

Registry counts: `ComponentRegistryDriftGuardTest` and `CatalogDocsDriftGuardTest` (including "API md / README state the live registered unregistered and total counts") pass with 0 failures against the documented **67 registered, 6 unregistered, 73 total**. No test prints the numbers, so the counts are confirmed by the guard comparing live registry sizes with those documented values.

Quick and guard runs of 28-VALIDATION (PlaceMapOsmdroidConfig, PlaceMapTilePurge, DemoRegistryRender, ReceiptDemosRegistryRender, ExplorerSchemePinGuard, A11ySweep, Phase28HygieneSourceContract) are all contained in the full 1855-test run, 0 failures.

Nothing was tagged, pushed or repinned; STATE.md and ROADMAP.md were not touched; no adb device was touched.

## Lane-2 lines, PD and FA of the seven plans

- Lane 2 (`HUB_LANE_OVERRIDE=2`) was declared in 28-04 (Task 2 reshaped the Explorer-only stage) and 28-05 (Tasks 2 and 3 replaced one line each of the Explorer-only builder and the Show button modifier). 28-06 moved the modifiers of the openable Undo Center row (internal rewrite, public API unchanged). 28-07 commits are lane 1, additive.
- PD: 28-01 PD-1 to PD-6, 28-02 PD-1 to PD-5, 28-03 PD-1 to PD-3, 28-04 PD-1 to PD-5, 28-05 PD-1 to PD-5, 28-06 PD-1/PD-2 (PD-4 holds the pre-recorded C-1 recipe), 28-07 PD-1 to PD-4, all taken as written.
- FA: 28-01 FA-1 to FA-4 (+FA-3 single detection constant), 28-02 FA-1 to FA-4, 28-04 FA-3 (hard-coded English Undo Center chrome, v2.8.1), 28-06 FA-2 held, FA-3 did not trigger, FA-4 stands.

## Decisions Made

- Hygiene scans use a local `code()` helper (block-comment regex then the shared `stripComments`) so one-line `/** ... */` doc comments never trip a forbidden-identifier scan.
- The `PlaceMapOsmdroidConfig.kt` assertions deliberately state invariants only, so plan 28-08 (`load()` once per process) can edit the file internally without touching this test.
- Two optional oracle options added beyond the plan's list (`--a11y-enabled`, `--touch-exploration`), defaulting to `null`, so recorded standing values never produce a false failure.

## Deviations from Plan

None in behavior. One small additive extension: the two optional oracle options above (Rule 2: the plan's restore step restores accessibility_enabled and touch_exploration_enabled to "recorded values", and the oracle needed a way to receive them). Defaults match the plan's stated defaults.

One fixture-test defect fixed during Task 2 before commit: the fake adb's `${*#...}` expansion applied per element, so the package filter never matched; replaced with a scalar copy of `"$*"`.

The plan says to run the gate from the main checkout; it was run in this worktree, whose HEAD contains every Phase 28 commit and the two new ones.

## Issues Encountered

None. No fix attempts beyond the single fixture bug above.

## Gate-1 note

The "Gate-1 contract for the autonomous self-UAT" in 28-07-PLAN.md `<verification>` (G1-0 to G1-8, parking list P-1 to P-6) was not run here and nothing was written for it: no adb device touched, no `28-07-SELF-UAT.md`, no uat-pending entry. The orchestrator's Gate-1 tester owns it. `tools/verify-tester-restored.sh` is ready for its G1-8 step.

## Self-Check: PASSED

- FOUND: src/test/java/io/github/ygaray/yahirandroidtaste/explorer/Phase28HygieneSourceContractTest.kt
- FOUND: tools/verify-tester-restored.sh (executable)
- FOUND: tools/test/test-verify-tester-restored.sh (executable)
- FOUND commits: b2765a7, 32bd7d5
