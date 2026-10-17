---
phase: 28-talkback-sweep-explorer-demos
plan: 02
subsystem: placemap-osmdroid-config
tags: [osmdroid, tile-cache, placeholder-purge, sqlite, POL-03]
requires:
  - phase: 28-01
    provides: honest fallback agent and configureOsmdroid structure in PlaceMapOsmdroidConfig.kt
provides:
  - purgePlaceholderTiles (size AND md5 fingerprint delete) and its once-per-install / first-call-per-process trigger
  - tools/verify-osm-tile-cache.py, the offline instrument for Gate-1 check G1-6 (plan 28-07)
affects: [28-07]
tech-stack:
  added: []
  patterns: [bound-argument SQL with CAST(? AS INTEGER), no-op DatabaseErrorHandler so a purge never deletes the db]
key-files:
  created:
    - src/test/resources/osm-blocked-placeholder.png
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapTilePurgeTest.kt
    - tools/verify-osm-tile-cache.py
    - tools/test/test-verify-osm-tile-cache.sh
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapOsmdroidConfig.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapOsmdroidConfigTest.kt
key-decisions:
  - "PD-1..PD-5 from the plan implemented as written (per-process AtomicBoolean evaluated before the install-flag test, synchronous best-effort scan, -1 for a scan that could not run, real fixture fetched with curl)"
  - "Open the cache with a no-op DatabaseErrorHandler: Android's default handler deletes a database it finds corrupt"
requirements-completed: [POL-03]
status: complete
commits: 3
plan_head_before: 89dec1cc606d44c0640e9f355a18385ea70a7d07
actuals:
  tokens: 14000
  tasks: 3
  commits: 3
---

# Phase 28 Plan 02: Placeholder tile purge Summary

**Fingerprint-only (6987 bytes AND md5) purge of OSM "blocked" placeholder tiles, run on the first map setup of every process and until the install flag is set, plus an offline script that checks a pulled cache.db.**

## What was built

- `purgePlaceholderTiles(dbFile, sizeBytes, md5Hex)` in `PlaceMapOsmdroidConfig.kt` (internal). Returns the deleted-row count, `0` for nothing to do (a missing file is never created), `-1` when the scan could not run. Bound arguments only, with the length compared through `CAST(? AS INTEGER)`; deletes go through `db.delete` with where-args.
- `configureOsmdroid` calls it on `<tileCache>/cache.db` on the first call of each process and whenever the hub's `placeholder_tiles_purged` flag is unset. The flag is set only after a scan completed, so a failed scan is retried and never breaks map creation. It is not keyed on an agent change and uses no cache-manager or whole-cache wipe API.
- `src/test/resources/osm-blocked-placeholder.png`: the real blocked tile fetched once from the OSM server. It is 6987 bytes with md5 `c069a15b2cc2d6b6f527ad09eb93c61a`, so the pinned constants are proven against real bytes.
- `tools/verify-osm-tile-cache.py`: read-only (`mode=ro`) check over a pulled cache. Prints `PLACEHOLDER_ROWS`, `REAL_ROWS` and `TOTAL_ROWS`. Exit 0 needs zero placeholder rows and at least one real row (or at least one placeholder with `--expect-placeholder`). Exit 1 is a failed assertion and exit 2 a usage or database error.

## Verification (plan-level)

- `PlaceMapTilePurgeTest` 11/11, `PlaceMapOsmdroidConfigTest` 37/37 (29 earlier plus 8 new), `PlaceMapPickerTest` 82/82, `LibraryInvariantsGuardTest` 5/5. All run with `--no-daemon`; the test task re-executed after the source change (not up-to-date).
- `./gradlew apiDump` then `git diff --exit-code api.txt`: unchanged. `./gradlew detekt`: green at zero baseline.
- `bash tools/test/test-verify-osm-tile-cache.sh`: `PASS=15 FAIL=0`. `bash tools/test/run-all.sh`: exit 0, every suite `ok`, six suites including the new one.

## Deviations from Plan

**1. [Rule 1 - Bug] Android's default database error handler deletes a corrupt db**
- Found during: Task 1 (`garbageFile_returnsMinusOne_withoutThrowing` failed with `FileNotFoundException`).
- Issue: `SQLiteDatabase.openDatabase(path, null, flags)` installs `DefaultDatabaseErrorHandler`, which deletes a database it finds corrupt. A purge that removes the user's cache file violates "the file bytes stay unchanged".
- Fix: pass a no-op `DatabaseErrorHandler` to `openDatabase`.
- Files: `PlaceMapOsmdroidConfig.kt`. Commit: 318233c.

**2. [Process] Task 3 commit used no HUB_LANE_OVERRIDE**: only new tools files, classified lane 1 by the hook.

No other deviations. Tasks 1 and 2 were committed with `HUB_LANE_OVERRIDE=2` (internal edit, no public signature changed, api.txt unchanged), as the plan prescribed.

## Flagged assumptions carried forward (FA-1 to FA-4)

Unchanged from the plan: a per-process scan reads "again whenever a placeholder is detected" (FA-1); `length(tile)` is header-answered so the synchronous scan is cheap, not measured on a large cache (FA-2); a second SQLite connection beside osmdroid's `SqlTileWriter` is assumed safe, and any lock error returns `-1` (FA-3); an OSM placeholder change makes the purge miss, which is fail-safe (FA-4). None is exercised on a device here; device behavior belongs to Gate-1 (G1-6, plan 28-07).

## Notes for the orchestrator

- Tasks were executed without a separate RED run for Task 2 and Task 3 tests (the Task 1 RED was observed: compile failure on the missing function); Task 2 and 3 tests were written before their implementation.
- No tag, push, repin or device access.

## Self-Check: PASSED

- Files present: PlaceMapOsmdroidConfig.kt, PlaceMapTilePurgeTest.kt, osm-blocked-placeholder.png, verify-osm-tile-cache.py, test-verify-osm-tile-cache.sh.
- Commits present: 318233c, 4429a4d, 08ceecd (3 measured from plan_head_before).
