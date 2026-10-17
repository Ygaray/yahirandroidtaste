---
phase: 28-talkback-sweep-explorer-demos
plan: 01
subsystem: placemap-osmdroid-config
tags: [osmdroid, user-agent, osm-tile-policy, robolectric, explorer]
requires: []
provides:
  - "configureOsmdroid injects the resolved caller agent as the User-Agent request property after every Configuration.load()"
  - "resolveOsmdroidUserAgent(userAgent, packageName, versionName, androidRelease), buildFallbackUserAgent, isBlockedClassUserAgent (all internal)"
  - "EXPLORER_MAP_USER_AGENT (internal const) used by both Explorer PlaceMapPicker demos"
  - "tools/probe-osm-user-agent.sh one-tile host probe"
affects: [28-02, 28-07]
tech-stack:
  added: []
  patterns: ["in-memory request-property injection after every osmdroid load()", "ContextWrapper to fake a host package in Robolectric"]
key-files:
  created:
    - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ExplorerMapUserAgentTest.kt
    - tools/probe-osm-user-agent.sh
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapOsmdroidConfig.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapPicker.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/PickersFamilyScreen.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapOsmdroidConfigTest.kt
decisions:
  - "PD-1 to PD-6 of the plan taken as written (literal User-Agent key, case-variant removal, control-char strip, Build.VERSION.RELEASE fallback, one Log.w per call with tag YatOsmdroid, probed Explorer agent)"
  - "The placeholder prefix literal appears exactly once in main code (BLOCKED_PLACEHOLDER_PACKAGE_PREFIX); KDoc and the log line describe it without repeating it"
metrics:
  duration: "about 40 minutes"
  completed: 2026-10-17
status: complete
commits: 3
plan_head_before: 48f73a80daf11812622cc00f8f3d3c86ed2826d7
actuals:
  tokens: 8100
  tasks: 3
  commits: 3
requirements-completed: [POL-03]
---

# Phase 28 Plan 01: OSM User-Agent reaches the wire, blocked agents are loud Summary

The caller's user agent now reaches the OSM tile server through `Configuration.additionalHttpRequestProperties["User-Agent"]` (set after every `load()`), a blank or bare-package agent expands to an honest `<pkg>/<version> (Android <release>)`, and a blocked-class agent logs one `Log.w`; the Explorer demos pass a hub-owned real agent.

## What was built

1. **Task 1 (tracer, commit 042667c):** `configureOsmdroid` removes case variants of the header key and puts the resolved agent under `User-Agent` after each `load()`. `userAgentValue` is still set. The hub never writes the persisted prefs key. Seven Robolectric tests pin first and second call, persisted-value precedence (overwritten in memory, prefs untouched), the one-entry rule with a differently cased persisted key, survival of unrelated persisted properties, and the literal key. Process-global state (request properties and the hub prefs file) is snapshotted and restored per test.
2. **Task 2 (commit f3a11f2):** `resolveOsmdroidUserAgent` takes `(userAgent, packageName, versionName, androidRelease)`, strips ISO control characters before trimming, expands a blank or bare-package agent, and renders a null or blank versionName as `0`. `isBlockedClassUserAgent` plus one `Log.w` per call (tag `YatOsmdroid`, names the resolved agent and the OSM policy URL, nothing else). `PlaceMapPicker` KDoc asks for `AppName/version (+contact)`. No public signature or `api.txt` change.
3. **Task 3 (commit ec4a597):** `EXPLORER_MAP_USER_AGENT` (`YahirAndroidTasteExplorer/2.8 (+https://github.com/Ygaray/yahirandroidtaste)`) replaces `LocalContext.current.packageName` in both picker demos; `ExplorerMapUserAgentTest` pins it; `tools/probe-osm-user-agent.sh` probes exactly one tile.

## Host probe results (live, 2026-10-17)

```
AGENT=YahirAndroidTasteExplorer/2.8 (+https://github.com/Ygaray/yahirandroidtaste) BLOCKED=no SIZE=4472 MD5=ce9c4e9084891d7cae5390520b1756ea
AGENT=io.github.ygaray.yahirandroidtasteharness/1.0 (Android 15) BLOCKED=no SIZE=4472 MD5=ce9c4e9084891d7cae5390520b1756ea
AGENT=com.example.probe/1.0 (Android 15) BLOCKED=yes SIZE=6987 MD5=c069a15b2cc2d6b6f527ad09eb93c61a
```

The third line ran with `--expect-blocked` (exit 0).

## Verification

- `testDebugUnitTest` for `PlaceMapOsmdroidConfigTest`, `PlaceMapPickerTest`, `LibraryInvariantsGuardTest`, `ExplorerMapUserAgentTest`: all green.
- `apiDump` then `git diff --exit-code api.txt`: unchanged.
- `detekt`: green at zero baseline.
- `bash -n` and `test -x` on the probe script: pass. No on-device or wire-level behavioral verification was done (Gate-1 and 28-07 own that).

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] `@Config(packageName = ...)` does not exist in this Robolectric**
- **Found during:** Task 2 (the "placeholder package with a blank agent logs exactly one warning, not two" test).
- **Fix:** the test passes a `ContextWrapper` that reports a placeholder package (built from the constant) and returns itself as `applicationContext`. This also exercises the `NameNotFoundException` path (versionName `0`, no throw).

**2. [Rule 1 - Test correctness] osmdroid's first-run `load()` branch wipes persisted request properties**
- **Found during:** Task 1 (bytecode check of `DefaultConfigurationProvider.load`). With no persisted `osmdroid.basePath` the first load rewrites the persisted `additionalHttpRequestProperty.*` keys from memory instead of restoring them.
- **Fix:** the precedence tests seed `osmdroid.basePath` and `osmdroid.cachePath` first (a helper `seedPersistedPrefs`) so they reach the restore branch a real consumer is in. This is a test-setup fact, not a hub behavior change.

**3. [Plan-internal conflict, resolved toward the prohibition] placeholder literal in KDoc**
- Task 2's action asks the KDoc to name the `com.example` placeholder ids, but the plan's prohibition and Task 2 itself say the constant is the only occurrence of that literal in main code. I followed the prohibition: KDoc and the log line say "placeholder application ids (template ids such as the Android Studio default)". `grep com.example src/main` shows only the constant.

**4. [Test-fixture hygiene]** The pre-existing fixtures using a consumer-style placeholder agent were replaced with neutral `Probe/1.0 (+https://example.org)`; blocked-class fixtures are built from the constant.

## Flagged assumptions carried (unchanged from the plan)

FA-1 (`Build.VERSION.RELEASE`), FA-2 (bare package means a trimmed agent equal to the host packageName), FA-3 (single detection constant), FA-4 (case-insensitive header names; the one-entry test is the source of truth), FA-5 (one log line per call, not per process).

## Issues Encountered

None blocking. No auth gates, no checkpoints, nothing deferred.

## Self-Check: PASSED

- Files found: PlaceMapOsmdroidConfig.kt, PlaceMapPicker.kt, PickersFamilyScreen.kt, PlaceMapOsmdroidConfigTest.kt, ExplorerMapUserAgentTest.kt, tools/probe-osm-user-agent.sh (executable).
- Commits found: 042667c, f3a11f2, ec4a597 (3 measured by `git rev-list --count` from `plan_head_before`).
