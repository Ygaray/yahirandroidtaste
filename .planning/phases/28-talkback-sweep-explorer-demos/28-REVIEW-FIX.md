---
phase: 28-talkback-sweep-explorer-demos
fixed_at: 2026-10-17T00:00:00Z
review_path: .planning/phases/28-talkback-sweep-explorer-demos/28-REVIEW.md
iteration: 1
findings_in_scope: 14
fixed: 13
skipped: 1
status: resolved
---

# Phase 28: Code Review Fix Report

**Fixed at:** 2026-10-17
**Source review:** .planning/phases/28-talkback-sweep-explorer-demos/28-REVIEW.md
**Iteration:** 1

**Summary:**
- Findings in scope: 14 (1 critical, 4 warning, 9 info; fix_scope all)
- Fixed: 13
- Skipped: 1 (IN-08, a documented acceptable-skip)

Status is `resolved`: every finding is fixed or a documented acceptable-skip, and no open
blocker/critical/high remains.

**Verification (ran in the main checkout, no worktree, no adb device touched):**
- Targeted classes after the code changes, `--rerun-tasks`: PlaceMapOsmdroidConfigTest,
  PlaceMapTilePurgeTest, PlaceMapPickerTest, Phase28HygieneSourceContractTest,
  ExplorerMapUserAgentTest, ExplorerSchemePinGuardTest, A11ySweepSemanticsTest,
  GoToItemSheetDemosRegistryRenderTest: all green.
- `tools/test/run-all.sh` (all shell and Python tool tests, `test-verify-tester-restored.sh`
  PASS=82 FAIL=0, `test-verify-osm-tile-cache.sh` PASS=15 FAIL=0): all ok.
- Final `./gradlew testDebugUnitTest --no-daemon --rerun-tasks`: BUILD SUCCESSFUL (179 result files).
- `./gradlew apiDump --no-daemon` then `git diff --exit-code api.txt`: exit 0, api.txt byte-identical.
- `./gradlew detekt --no-daemon`: BUILD SUCCESSFUL, zero baseline untouched.
- Library commits that touch `src/main` or `src/test` used `HUB_LANE_OVERRIDE=2` (reason: internal
  edit, no public signature changed, api.txt unchanged); no hook was bypassed, no `--no-verify`.

## Fixed Issues

### CR-01: Placeholder purge runs on the main thread with one autocommit transaction per deleted row

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapOsmdroidConfig.kt`, `src/test/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapOsmdroidConfigTest.kt`, `src/test/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapTilePurgeTest.kt`
**Commit:** 17ec2ee
**Status:** fixed: requires human verification (concurrency/logic change; Robolectric cannot show the device-side timing)
**Applied fix:**
- `purgePlaceholderTiles` now deletes all matches in one `beginTransaction`/`setTransactionSuccessful`/`endTransaction`
  block (still bound arguments, still size AND md5, still the no-op `DatabaseErrorHandler`).
- `configureOsmdroid` no longer scans inline: `schedulePlaceholderScan` runs the scan on a short-lived
  daemon thread (`yat-osm-placeholder-purge`), at most one at a time (an in-flight guard drops an
  overlapping call), and a rejected or throwing task releases the guard and never reaches map creation.
- Ordering decision: the scan cannot be strictly ordered before the `MapView` opens `cache.db` without
  making the main thread wait, so it is not. The KDoc on `schedulePlaceholderScan` records why this is
  safe: the scan uses its own SQLite connection, file locking serializes it against the tile writer, a
  lock timeout becomes `-1` (flag stays unset, retried later), and the delete can only remove a row that
  is exactly the placeholder. No cache-manager, bulk-tile, expiry-override or whole-cache-wipe API is
  used and the public surface is unchanged (api.txt identical).
- Tests: the existing config test class now installs an inline executor in `setUp` (and restores the
  production one in `tearDown`) so every existing scan assertion is unchanged and deterministic. New
  tests: the production executor purges off the calling thread, an overlapping scan is dropped not
  queued, a rejected task neither throws nor wedges the guard, and 594 of 600 placeholder rows are
  deleted in one pass while real tiles survive. The extra internal hooks `placeholderPurgeExecutor` and
  `resetPlaceholderPurgeExecutorForTests` live in main source next to the existing reset hooks (see IN-08).

### WR-01: The "install flag" never prevents a scan, so every cold start rescans the cache

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapOsmdroidConfig.kt`
**Commit:** b516210
**Applied fix:** Kept the per-process rescan (the plan's decision, "once per install and again when found",
is binding, and after CR-01 it is a background scan, so it is cheap for the UI). Corrected the KDoc of
`PREF_PLACEHOLDER_PURGED` and policy bullet 3 of `configureOsmdroid` to state the real contract: every
process scans once (off the main thread); the flag only stops later same-process calls from retrying after
a success, and stays unset after a failed scan. Behavior and the pinned test
`aNewProcessScansAgain_evenWhenTheInstallFlagIsSet` are unchanged.

### WR-02: Load-once guard does not protect the map that tile threads actually iterate

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapOsmdroidConfig.kt`
**Commit:** 6e4fe08
**Applied fix:** Kept the load-once guard and documented the residual instead of inventing a lock that
cannot cover osmdroid's own threads. The `configureOsmdroid` KDoc now has an explicit precondition (the
guarantee covers hub callers only; the first call per process is safe only if no osmdroid `MapView` or
other osmdroid user, such as a consumer's own `Configuration.load`, is already running) and the lock's
KDoc says it serializes hub callers only. Not closed in code: a consumer that already has live osmdroid
tile threads before the hub's first call remains a documented risk.

### WR-03: `--package` value is interpolated into a remote shell command

**Files modified:** `tools/verify-tester-restored.sh`, `tools/test/test-verify-tester-restored.sh`
**Commit:** e1c3b1d
**Applied fix:** `--package` is validated against `^[A-Za-z][A-Za-z0-9_]*(\.[A-Za-z0-9_]+)+$` in the option
loop (exit 2 before any adb call). Fixtures added: 11 hostile or malformed names (`a;b`, `$(id)`, backticks,
pipes, spaces, leading dash, single segment, empty segments) must exit 2 with an empty fake-adb log, and a
valid name with digits and underscores is accepted.

### WR-04: Wireless-serial refusal only matches a colon

**Files modified:** `tools/verify-tester-restored.sh`, `tools/test/test-verify-tester-restored.sh`
**Commit:** 228b6ba
**Applied fix:** Replaced the colon check with the allowlist `^[A-Za-z0-9]+$` (plain USB serial shape),
refused before any adb call. Fixtures added: `adb-*._adb-tls-connect._tcp` mDNS names, `emulator-5554`,
metacharacters, dots, spaces and a leading dash all exit 2 with an empty fake-adb log; the plain USB
serial is accepted and reaches adb with `-s`. The existing `host:port` fixture still passes. Header
exit-code docs updated.

### IN-01: Go-to-item demo dismisses the sheet even when `hide()` is cancelled

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ReleaseDemos.kt`
**Commit:** 60cffce
**Applied fix:** Both `invokeOnCompletion` handlers (the outcome `slideDown` and the `onOpenTarget` path) now
act only when `!sheetState.isVisible`, so a cancelled `hide()` neither removes the sheet nor records the
readout. GoToItemSheetDemosRegistryRenderTest still passes.

### IN-02: `awk` numeric comparison treats non-numeric text as 0

**Files modified:** `tools/verify-tester-restored.sh`, `tools/test/test-verify-tester-restored.sh`
**Commit:** 4e79738
**Applied fix:** The font-scale comparison runs only when both sides match `^[0-9]+(\.[0-9]+)?$`; junk on
either side is a `FAIL font_scale`. Fixtures: garbled reading, garbled reading vs garbled expectation, and
`1.0abc` all exit 1; a real `0` equals `0.0`.

### IN-03: KDoc says a bare package name logs a warning; the code does not

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapOsmdroidConfig.kt`, `src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapPicker.kt`
**Commit:** 9e42e01
**Applied fix:** Reworded both KDocs: a blank agent or a placeholder application id logs the warning; a bare
non-placeholder package name is expanded silently. Code untouched (the tests agree with the code).

### IN-04: First-run `load()` creates directories and persists paths outside `cacheDir`

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapOsmdroidConfig.kt`
**Commit:** adc97c6
**Applied fix:** Softened the KDoc to say the hub redirects osmdroid's paths to `cacheDir` in memory after
`load()`, and that osmdroid's first-run `load()` may still create and persist its default paths. The
optional pre-seeding of the prefs was not done (extra behavior, and it would touch prefs keys the hygiene
contract keeps tight).

### IN-05: Vacuous assertions in new tests

**Files modified:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/A11ySweepSemanticsTest.kt`, `src/test/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapOsmdroidConfigTest.kt`
**Commit:** 8f2312e
**Applied fix:** Removed `assertNotNull(style)` and the `assertSame(mapBefore, requestProperties())` line
(replaced by a comment that the surviving `X-Probe` entry is the proof) plus their now-unused imports.
Both were assertions that could not fail; the real checks beside them are unchanged.

### IN-06: Hard-coded `2.8` in the Explorer user agent will go stale

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/PickersFamilyScreen.kt`, `ECOSYSTEM.md`
**Commit:** 88bed0f
**Applied fix:** The release version is the git tag and is not in the repo, so it cannot be derived. Added a
comment at `EXPLORER_MAP_USER_AGENT` and a bullet in ECOSYSTEM.md section 7 naming the literal and its test
as a bump-with-release item (the review's second suggested option).

### IN-07: `Phase28HygieneSourceContractTest` depends on the Gradle working directory

**Files modified:** `src/test/java/io/github/ygaray/yahirandroidtaste/explorer/Phase28HygieneSourceContractTest.kt`
**Commit:** c79b094
**Applied fix:** The source root now comes from an upward-walking resolver (same pattern as the sibling
tests, depth 8) with a clear error, and `explorerFiles()` uses `checkNotNull` with a message instead of
`!!`. All assertions unchanged.

### IN-09: Verification tools make fragile assumptions about their inputs

**Files modified:** `tools/verify-osm-tile-cache.py`, `tools/probe-osm-user-agent.sh`
**Commit:** db086df
**Applied fix:** The Python tool opens the pulled snapshot with `?mode=ro&immutable=1`. The probe treats a
403 or 429 that carries an `x-blocked` header as the server's verdict (`BLOCKED=yes`) instead of exit 2;
any other non-200 is still infrastructure (exit 2). The probe has no offline fixture test (it needs the
network), so that change is covered by `bash -n` only. The cache tool's 15 fixtures still pass, including
"the input database is not modified".

## Skipped Issues

### IN-08: Test-only reset hooks ship in the library's main source set

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapOsmdroidConfig.kt:31-33, 42-44`
**Reason:** Documented acceptable-skip. The review itself rates it "Acceptable as is" and low risk for a
personal library with no outside consumers. The suggested `@VisibleForTesting` would add an
`androidx.annotation` dependency/import that no main file uses today, which is churn for a lint hint that
does not change behavior. Note: CR-01 added two more such internal hooks (`placeholderPurgeExecutor`,
`resetPlaceholderPurgeExecutorForTests`) beside the existing ones; they are `internal` and absent from api.txt.
**Original issue:** `resetPlaceholderScanForTests` and `resetOsmdroidLoadForTests` are `internal` but
compiled into the AAR and can reset process-global guards.

---

_Fixed: 2026-10-17_
_Fixer: Claude (gsd-code-fixer)_
_Iteration: 1_
