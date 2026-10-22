---
phase: 28-talkback-sweep-explorer-demos
plan: 08
subsystem: map-osmdroid-config
tags: [osmdroid, tile-policy, concurrency, POL-03, gap-plan]
requires: [28-01, 28-02, 28-07]
provides:
  - "configureOsmdroid calls Configuration.load() at most once per process"
  - "User-Agent header re-applied in place on later calls, never cleared or refilled"
affects: [PlaceMapPicker, ExplorerActivity]
tech-stack:
  added: []
  patterns: ["load-once guard (AtomicBoolean + lock object)", "touch shared HashMap only when needed"]
key-files:
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapOsmdroidConfig.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapOsmdroidConfigTest.kt
decisions:
  - "Guard load() with a private AtomicBoolean plus a lock object; flag set only after load() returns so a throwing load is retried"
  - "Header edits go through one private helper: remove case variants only when present, put only when missing or different"
metrics:
  tasks: 2
  files: 2
status: complete
commits: 1
plan_head_before: 9701f2491b04ad233d5c714aebb824270efb4cf4
actuals:
  tokens: 9000
  tasks: 2
  commits: 1
---

# Phase 28 Plan 08: POL-03 load() once per process Summary

`configureOsmdroid` now calls `Configuration.getInstance().load(...)` at most once per process (flag + lock) and keeps the one `User-Agent` request property correct on later calls with an in-place `put`, closing the SecondBrain 181.1 WR-02 race.

## What changed

- `PlaceMapOsmdroidConfig.kt` (internal only): private `osmdroidLoaded` AtomicBoolean and `osmdroidLoadLock`; `internal fun resetOsmdroidLoadForTests()`; the load + `userAgentValue` + header block is inside `synchronized`. First call: load, then set the agent and header (old behaviour, once). Later calls: no `load()`, `userAgentValue` assigned only when different, header handled by new private `applyUserAgentHeader` (removes non-exact case variants only if present, puts only when missing or different). KDoc rule 1 updated. Log.w block, base path, tile cache and placeholder-scan order unchanged.
- `PlaceMapOsmdroidConfigTest.kt`: hook called in setUp and tearDown; seven new tests (`secondCallDoesNotReload_andKeepsAnUnrelatedRequestProperty`, `secondCallWithTheSameAgentKeepsTheHeader`, `secondCallWithAnotherAgentReplacesTheValueWithoutReloading`, `secondCallWithAnotherAgentKeepsExactlyOneUserAgentEntryEvenWhenCasedDifferently`, `aRemovedHeaderIsReappliedOnTheNextCall`, `resettingTheLoadGuardMakesTheNextCallLoadAgain`, `warningCountPerCallIsUnchangedOnLaterCalls`). No existing test needed editing or weakening.

## Task commits

| Task | Commit | Notes |
|------|--------|-------|
| 1 (tracer) | 4f8ceea | fix(28-08): call osmdroid Configuration.load() once per process (committed with HUB_LANE_OVERRIDE=2, internal edit, api.txt unchanged) |
| 2 | none | verification only |

## Verification (fresh evidence)

- RED first: with only the hook stub in place, 3 of the new tests failed (unrelated request-property entry wiped by the reload), the right reason.
- Targeted run (PlaceMapOsmdroidConfigTest, PlaceMapTilePurgeTest, PlaceMapPickerTest, LibraryInvariantsGuardTest, Phase28HygieneSourceContractTest, ExplorerMapUserAgentTest): BUILD SUCCESSFUL; PlaceMapOsmdroidConfigTest tests=44 failures=0, all seven new names present.
- `./gradlew testDebugUnitTest --rerun-tasks --no-build-cache --no-daemon`: BUILD SUCCESSFUL, 35 of 35 tasks executed (forced fresh).
- `./gradlew apiDump --no-daemon` then `git diff --exit-code api.txt`: identical.
- `./gradlew detekt --no-daemon`: BUILD SUCCESSFUL (zero baseline).
- `bash tools/test/run-all.sh`: all suites PASS, FAIL=0.
- `grep -c 'Configuration.getInstance().load('` = 1 (inside the `!osmdroidLoaded.get()` branch); `fun resetOsmdroidLoadForTests` = 1; `com.example` appears once (the existing detection constant).
- Diff versus base touches only the two named files.

## Deviations from Plan

None. Phase28HygieneSourceContractTest stayed green without edits.

## Notes for Gate-1

Behavioural wire check (a cached tile that is not the 6987-byte placeholder) was not run here; it belongs to Gate-1 after this plan. No device was touched; nothing tagged, pushed or repinned.

## Self-Check: PASSED

- PlaceMapOsmdroidConfig.kt and PlaceMapOsmdroidConfigTest.kt: FOUND
- Commit 4f8ceea: FOUND
