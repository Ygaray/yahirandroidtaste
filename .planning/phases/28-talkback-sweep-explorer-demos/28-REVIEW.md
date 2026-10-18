---
phase: 28-talkback-sweep-explorer-demos
reviewed: 2026-10-17T00:00:00Z
depth: standard
files_reviewed: 40
files_reviewed_list:
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapOsmdroidConfig.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapPicker.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ExplorerTheme.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ExplorerActivity.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ExplorerEntry.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentDetailScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ExplorerIndexScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/TokenBrowserScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ButtonsFabFamilyScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/CardsFamilyScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ChipsFamilyScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/EmptyStateFamilyScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/FeedbackFamilyScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/PickersFamilyScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ProgressFamilyScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ReleaseDemos.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/SheetsFamilyScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/TactileFoundationFamilyScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/VoiceCommandFamilyScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreen.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/A11ySweepSemanticsTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapOsmdroidConfigTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapTilePurgeTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ExplorerMapUserAgentTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ExplorerSchemePinGuardTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ExplorerThemeTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/FeedbackStackDemoRegistryRenderTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/GoToItemSheetDemosRegistryRenderTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/HoistedSheetDemoRegistryRenderTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/Phase28HygieneSourceContractTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/UndoCenterTargetRowsDemoRegistryRenderTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/A11ySweepFeedbackCardTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/A11ySweepUndoCenterSemanticsTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreenOpenTargetTest.kt
  - tools/probe-osm-user-agent.sh
  - tools/test/test-verify-osm-tile-cache.sh
  - tools/test/test-verify-tester-restored.sh
  - tools/verify-osm-tile-cache.py
  - tools/verify-tester-restored.sh
findings:
  critical: 1
  warning: 4
  info: 9
  total: 14
status: issues_found
---

# Phase 28: Code Review Report

**Reviewed:** 2026-10-17
**Depth:** standard
**Files Reviewed:** 40
**Status:** issues_found

## Summary

Reviewed the POL-03 osmdroid configuration (user agent, placeholder purge, load-once), the
UndoCenter open-row a11y restructure, the Explorer scheme pin and the new release demos, the
Robolectric sweep and registry tests, and the shell/Python tools. I read the real osmdroid 6.1.20
bytecode (`DefaultConfigurationProvider.load`, `TileDownloader`, `SqlTileWriter`) to check the
config file's claims instead of trusting the comments.

The UndoCenter fix (tag, merge and `combinedClickable` moved onto the `ListItem` node) is sound.
The semantics merge rule (children that merge their own descendants stay separate nodes) matches
what the tests assert, and the press/peek state machine is consistent. The Explorer scheme pin is
mechanical and guarded.

The concerns are concentrated in `PlaceMapOsmdroidConfig.kt` (a main-thread, unbatched SQLite purge
and a thread-safety guarantee that is narrower than the comments claim) and in the shell tools
(remote-shell interpolation and a serial refusal that is only a colon check).

Confirmed from bytecode: osmdroid's `TileDownloader` iterates
`getAdditionalHttpRequestProperties().entrySet()` on the tile download threads with no
synchronization, so the load-once rationale is real. `load()` also clears, refills and (first run)
commits prefs.

## Critical Issues

### CR-01: Placeholder purge runs on the main thread with one autocommit transaction per deleted row

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapOsmdroidConfig.kt:127-136, 248-251`
**Issue:** `configureOsmdroid` is called from the `AndroidView` `factory` in `createPlaceMapView`
(`PlaceMapPicker.kt:716`), which runs on the main thread during composition. It calls
`purgePlaceholderTiles` synchronously. That function does `db.delete(...)` once per matching row,
outside any transaction, so every deleted row is its own journaled commit (journal write plus
fsync). The population this purge targets is exactly the one that makes it expensive: a cache that
accumulated placeholder tiles while the agent was blocked. That is plausibly hundreds to thousands
of rows, and it happens on the first map open after upgrade. At a few milliseconds per commit that
can pass the 5 s ANR threshold, and it blocks the frame in the meantime. The scan also reads and
md5-hashes candidate blobs on the same thread. Robolectric cannot show this because its SQLite is
in memory and fast.
**Fix:** Batch the deletes in one transaction, and take the whole scan off the main thread. The
purge must finish before the `MapView` opens `SqlTileWriter` (it uses a static `db_file` and `mDb`),
so either run it before `MapView` construction on a background dispatcher and create the map after
it completes, or purge lazily after first composition.
```kotlin
var deleted = 0
db.beginTransaction()
try {
    for ((key, provider) in matches) {
        deleted += db.delete("tiles", "key = ? AND provider = ?", arrayOf(key.toString(), provider))
    }
    db.setTransactionSuccessful()
} finally {
    db.endTransaction()
}
```

## Warnings

### WR-01: The "install flag" never prevents a scan, so every cold start rescans the cache on the main thread

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapOsmdroidConfig.kt:22-23, 126-136`
**Issue:** The condition is `firstScanThisProcess || !prefs.getBoolean(PREF_PLACEHOLDER_PURGED, false)`.
`placeholderScanStarted` is a process-local `AtomicBoolean`, so the first call of every process is
always a scan regardless of the persisted flag. The flag therefore only matters for a second or
later call in the same process after a failed scan. This is pinned by the test
`aNewProcessScansAgain_evenWhenTheInstallFlagIsSet`, so it is deliberate. But the KDoc
("set once a placeholder scan has completed on this install") and the flag's name suggest a
one-time purge, and the actual behavior is a full `tiles` table scan (`length(tile) = ?` over every
row) on every cold start where a map opens. Combined with CR-01 this is a recurring main-thread
cost. If the per-process rescan is intended (a blocked fetch can re-cache a placeholder within a
process), then the persisted flag is dead weight and misleading.
**Fix:** Pick one contract. If one-time per install is the intent, gate on the flag alone and drop
the process-local boolean. If per-process is the intent, delete `PREF_PLACEHOLDER_PURGED` and its
`SharedPreferences` write, and document the scan as per-process (and run it off the main thread).

### WR-02: Load-once guard does not protect the map that tile threads actually iterate

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapOsmdroidConfig.kt:36-39, 100-113, 146-155`
**Issue:** `osmdroidLoadLock` is private and only serializes this function's own callers. The
readers that matter are osmdroid's tile download threads, which iterate
`additionalHttpRequestProperties.entrySet()` (confirmed in `TileDownloader` bytecode) without
taking any lock. The hub still makes structural modifications of that `HashMap` on its first call
per process: `load()` runs `clear()` and refills, `applyUserAgentHeader` removes case variants, and
`put` on a missing key is structural too. That is safe only if no osmdroid `MapView` or tile thread
exists yet in the process. A consumer that already uses osmdroid elsewhere (its own map screen, or
its own `Configuration.load` in `Application.onCreate`) can have live tile threads when the hub's
first `configureOsmdroid` runs. A `ConcurrentModificationException` then surfaces on a tile thread.
The KDoc ("guards against concurrent callers", "touched only when needed") reads as a stronger
guarantee than this.
**Fix:** Either avoid mutating the shared map at all (set the header only if absent and skip
`load()` when `Configuration` is already initialized by the consumer), or document the precondition
explicitly in `INTEGRATION.md` and the KDoc: "call `configureOsmdroid` before any osmdroid
`MapView` exists in the process". If it is kept as is, state that the lock only covers hub callers.

### WR-03: `--package` value is interpolated into a remote shell command

**File:** `tools/verify-tester-restored.sh:111`
**Issue:** `adb_get shell pm list packages "$pkg"` expands to `adb -s ... shell pm list packages <pkg>`.
`adb shell` joins its arguments with spaces and hands them to the device's shell, so the local
double quotes do not protect the value. `--package 'x; <cmd>'` or `--package '$(<cmd>)'` runs on the
device. The script is advertised as READ-ONLY and its header and test assert that no state-changing
command is issued, yet the argument is not validated. The fixture test cannot catch this because its
fake `adb` never evaluates a shell.
**Fix:** Validate before use, in the option loop:
```bash
--package)
  need_value "$@"
  [[ "$2" =~ ^[A-Za-z][A-Za-z0-9_]*(\.[A-Za-z0-9_]+)+$ ]] || die "invalid package name: $2"
  PACKAGES+=("$2"); shift 2 ;;
```
Add a test case that passes `'a;b'` and expects exit 2 with an empty fake-adb log.

### WR-04: Wireless-serial refusal only matches a colon

**File:** `tools/verify-tester-restored.sh:48-51`
**Issue:** The refusal is `case "$SERIAL" in *:*)`. adb wireless-debugging devices that were paired
and auto-connected over mDNS have serials like `adb-<serial>-<suffix>._adb-tls-connect._tcp`, which
contain no colon, so they pass the check. Nothing verifies that the serial is the tester. Any
non-colon serial is accepted (`--serial` is free-form), including the personal phone if it ever
shows up under an mDNS name. The user's safety rules treat the personal phone as untouchable, and
this script's own header claims "the tester is addressed by its USB serial only". The script only
reads, but the guard is the stated safety control and it is incomplete.
**Fix:** Allowlist instead of blocklist. Accept only the literal tester USB serial pattern (for
example `^[A-Z0-9]{8,}$`) and reject anything containing `:`, `.` or `_adb`:
```bash
[[ "$SERIAL" =~ ^[A-Za-z0-9]+$ ]] || die "refusing serial ($SERIAL): the tester is addressed by its USB serial only"
```
Add a fixture for an `adb-XXXX-yyyy._adb-tls-connect._tcp` serial that must exit 2 before any adb call.

## Info

### IN-01: Go-to-item demo dismisses the sheet even when `hide()` is cancelled

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ReleaseDemos.kt:927, 936-939`
**Issue:** `scope.launch { sheetState.hide() }.invokeOnCompletion { shown = null }` also fires on
cancellation. The documented Material pattern guards with `if (!sheetState.isVisible)`. If `hide()`
is interrupted (for example by a drag), the sheet is removed and, in the `onOpenTarget` path, the
readout is recorded as though the slide finished. Demo-only, but the KDoc promises "records the
opened key only after the slide finished".
**Fix:** Guard both handlers: `.invokeOnCompletion { if (!sheetState.isVisible) { shown = null; ... } }`.

### IN-02: `awk` numeric comparison treats non-numeric text as 0

**File:** `tools/verify-tester-restored.sh:95`
**Issue:** `a + 0 == b + 0` makes any non-numeric string equal to `0` and also accepts a numeric
prefix (`1.0abc` equals `1`). A garbled `font_scale` reading and a garbled `--font-scale`
expectation compare equal, so the oracle can report `OK font_scale` for junk.
**Fix:** Require both sides to match `^[0-9]+(\.[0-9]+)?$` before comparing, else `bad`.

### IN-03: KDoc says a bare package name logs a warning; the code does not

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapOsmdroidConfig.kt:65-68`, `PlaceMapPicker.kt:128-130`
**Issue:** Both docs say a blank agent or a bare package name is replaced "and logs one warning".
The warning condition at line 114 is `sanitizeUserAgent(userAgent).isEmpty() || isBlockedClassUserAgent(resolvedAgent)`.
A bare non-placeholder package name (for example `com.host.app`) is expanded silently. The tests
agree with the code, not the docs.
**Fix:** Reword the docs ("a blank agent logs a warning; a bare package name is expanded silently")
or log for the bare-package case too.

### IN-04: First-run `load()` creates directories and persists paths outside `cacheDir`, contradicting the KDoc

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapOsmdroidConfig.kt:83-86, 106`
**Issue:** The KDoc says tiles are "confined to the app sandbox" under `cacheDir`. In osmdroid 6.1.20
the first-run branch of `load()` (no persisted `osmdroid.basePath`) calls
`getOsmdroidBasePath(context)`, which goes through `StorageUtils.getBestWritableStorage`, tries
`mkdirs` there (logging `Directory not created` on failure), falls back to
`getExternalFilesDir`/`filesDir`, then `commit()`s those default paths and the full config
(`save()`) into the hub's prefs. The hub afterwards overrides the in-memory paths to `cacheDir`, so
tiles do land there, but a default `osmdroid/tiles` directory can exist elsewhere, a synchronous
prefs `commit()` happens on the main thread, and the persisted paths disagree with the in-memory
ones.
**Fix:** Soften the KDoc to say the hub redirects osmdroid's paths to `cacheDir` after `load()`.
Optionally pre-seed `osmdroid.basePath` and `osmdroid.cachePath` in the hub prefs with the `cacheDir`
paths before the first `load()`, so the default directories are never created.

### IN-05: Vacuous assertions in new tests

**File:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/A11ySweepSemanticsTest.kt:384`, `src/test/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapOsmdroidConfigTest.kt:475`
**Issue:** `assertNotNull(style)` asserts a non-null `mutableStateOf` enum and cannot fail.
`assertSame(mapBefore, requestProperties())` in `secondCallDoesNotReload_...` is always true:
`load()` clears and refills the same `HashMap` instance. The surviving `X-Probe` entry on the next
lines is the real check. Both lines read as evidence they are not.
**Fix:** Delete both, or replace `assertSame` with a comment that the `X-Probe` assertion carries the
proof.

### IN-06: Hard-coded `2.8` in the Explorer user agent will go stale

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/PickersFamilyScreen.kt:38-39`, `ExplorerMapUserAgentTest.kt:19`
**Issue:** `EXPLORER_MAP_USER_AGENT` embeds a literal version while the library version lives
elsewhere (`v2.7.1` today). The next release makes the OSM-facing identification wrong, and the test
pins the literal string, so nothing will flag it.
**Fix:** Derive the version from one source (a `const` shared with the publish version, or the
packaged `versionName`), or add a note in the release ritual (`ECOSYSTEM.md` section 7) to bump it.

### IN-07: `Phase28HygieneSourceContractTest` depends on the Gradle working directory

**File:** `src/test/java/io/github/ygaray/yahirandroidtaste/explorer/Phase28HygieneSourceContractTest.kt:21, 37-38`
**Issue:** `File(root)` is a relative path and `explorerFiles()` uses `listFiles { ... }!!`. Run from
any other working directory (IDE default, a different module root) it throws an NPE instead of a
clear message. Sibling tests (`ExplorerSchemePinGuardTest`, `ExplorerMapUserAgentTest`) already have
an upward-walking resolver.
**Fix:** Reuse that resolver, or `checkNotNull(...) { "explorer source dir not found from ${File(".").absolutePath}" }`.

### IN-08: Test-only reset hooks ship in the library's main source set

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapOsmdroidConfig.kt:31-33, 42-44`
**Issue:** `resetPlaceholderScanForTests` and `resetOsmdroidLoadForTests` are `internal` but compiled
into the AAR (Kotlin `internal` top-level functions are public at the bytecode level). They allow a
reset of process-global guards. Low risk for a personal library with no outside consumers, but the
load-once guarantee can be defeated by anything that reflects into them.
**Fix:** Acceptable as is. If tidying, annotate with `@VisibleForTesting` (androidx.annotation) so lint flags non-test callers.

### IN-09: Verification tools make fragile assumptions about their inputs

**File:** `tools/verify-osm-tile-cache.py:37`, `tools/probe-osm-user-agent.sh:41-44`
**Issue:** The Python tool opens the pulled `cache.db` with `mode=ro`. A database that was in WAL
mode and pulled without its `-wal`/`-shm` sidecars may fail to open or read stale data, and read-only
without `immutable=1` can still try to create a `-shm`. This is reported as exit 2, so it fails safe.
The probe treats any non-200 as "infrastructure, not a verdict" and exits 2, so `--expect-blocked`
cannot succeed if OSM blocks with a 403 instead of the 200 placeholder tile. The current evidence
supports 200 plus placeholder, so this only matters if the server changes behavior.
**Fix:** Python: use `?mode=ro&immutable=1` for a snapshot pulled off a device. Probe: when
`--expect-blocked` is set, treat 403 or 429 plus an `x-blocked` header as `BLOCKED=yes` instead of
erroring.

---

_Reviewed: 2026-10-17_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
