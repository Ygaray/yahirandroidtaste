---
phase: 28-talkback-sweep-explorer-demos
verified: 2026-10-18T01:21:30Z
status: human_needed
score: 3/5 must-haves verified
behavior_unverified: 2
overrides_applied: 0
behavior_unverified_items:
  - truth: "ROADMAP SC1 (A11Y-02): with TalkBack on the TESTER the ReceiptRow reads as one button with its state and Undo as a separate stop, the refused-undo Assertive live region is spoken, the saved-key summary is one item with Update/Clear as separate buttons, the group header is announced as a heading, and sheet target rows and snackbar cards announce their open label"
    test: "Gate-1 G1-2 to G1-5 on the TESTER: dump the accessibility tree (class, state, heading, actionList, live region) and the content-changed/announcement event text, then a time-boxed TalkBack attempt"
    expected: "Button+Collapsed/Expanded and Undo is its own focusable node; the refused node reports liveRegion=2 and the event carries the full 'Couldn't undo' message; the saved summary is one node and Update/Clear are separate Buttons; group label heading=true; actionList carries the exact open labels (English and German). Spoken-only items (P-1 to P-5) are parked for Yahir, never waived."
    why_human: "Robolectric proves semantics-tree structure only (Role, stateDescription, Heading, LiveRegionMode, custom actions, node merge). It cannot show what TalkBack actually speaks, the event text emitted when the Assertive surface appears (candidate C-1), or the felt stop count."
  - truth: "ROADMAP SC5 (POL-03), wire-level bullet: on the TESTER an Explorer picker with a real-id UA caches tiles whose size and md5 are not the placeholder"
    test: "Gate-1 G1-6: HEAD harness with a com.example. application id, open the Explorer Pickers page, let tiles load, pull cache.db (+wal/shm) and run `python3 -I tools/verify-osm-tile-cache.py <db>`; control on the v2.7.1 AAR with --expect-placeholder; re-run `tools/probe-osm-user-agent.sh` with the exact resolved strings"
    expected: "Exit 0 with zero placeholder rows and at least one real tile row; control shows the 6987-byte placeholder; probe prints BLOCKED=no for the Explorer agent and BLOCKED=yes for the com.example agent"
    why_human: "The unit tests prove the request-property map and the purge in memory/SQLite under Robolectric. Only a real MapView with osmdroid's TileDownloader and the live OSM server shows that the header reaches the wire and that the cache stays clean."
human_verification:
  - test: "Gate-1 G1-1 (DEMO-02): one screencap of a warn-toned atom on a page that had no HubSchemeScope before (Cards or Feedback family) in the HEAD harness and the v2.7.1 control; toggle Explorer light/dark"
    expected: "HEAD shows the hub static warn amber, the v2.7.1 control shows the device wallpaper tone, and the toggle flips light and dark inside the pin"
    why_human: "ExplorerThemeTest proves the resolved ColorScheme under a foreign outer scheme and the guard proves every file goes through ExplorerTheme, but the rendered pixels under real dynamic color are a device observation"
  - test: "Gate-1 G1-7 (DEMO-03): reach the FeedbackStackHost stage, the sheet go-to-item demos and the Undo Center target rows from their registry detail pages; fire, burst x4, swipe, tap-to-open; German at 360dp (wm density 480) and at font_scale 2.0"
    expected: "3 cards visible after the burst, oldest gone, readouts update, no clipped Undo/chevron/text in German at 2.0"
    why_human: "Registry render tests run at w360dp under Robolectric; real swipe gestures, timers and large-font layout are device behavior (long-string checks are also a Phase 29 criterion)"
  - test: "Gate-1 G1-4 regression smoke on the Undo Center openable row (touched by 28-06 C-2) plus designer evidence C-3/C-4 (oldest card's Undo order; double Dismiss listing)"
    expected: "tap opens once and neither undoes nor peeks, hold shows the peek and opens nothing, Undo undoes once and the row loses chevron and click action; C-3/C-4 evidence goes to the ui-designer seat (P-6), not decided here"
    why_human: "Real touch dispatch and the designer decisions are not Robolectric-provable"
  - test: "Review fix CR-01 (flagged 'requires human verification' in 28-REVIEW-FIX.md): placeholder scan now runs on a daemon thread beside the MapView's own cache.db connection"
    expected: "Opening the Explorer map picker on the TESTER does not stall or throw, and a second open after the scan leaves real tiles intact"
    why_human: "Robolectric cannot show device-side SQLite lock timing between the scan connection and osmdroid's SqlTileWriter; the code is built to fail soft (-1, flag stays unset)"
  - test: "G1-8 restore: `wm density 450` (never reset), font_scale, enabled_accessibility_services, accessibility_enabled, touch_exploration_enabled, harness packages; then `bash tools/verify-tester-restored.sh ...` exits 0; leases released"
    expected: "RESTORED=yes"
    why_human: "Device state; the read-only oracle exists and its fixture test passes, but only the Gate-1 run can use it"
---

# Phase 28: TalkBack sweep & Explorer demos Verification Report

**Phase Goal:** The new snackbars, the outcome sheet and the Undo Center pass a scoped TalkBack sweep on the TESTER, and the Explorer shows every v2.8 surface in English and German inside the pinned hub color scheme.
**Verified:** 2026-10-18T01:21:30Z
**Status:** human_needed (nothing failed; the TESTER/TalkBack and wire-level checks belong to the Gate-1 self-UAT that runs after this report)
**Re-verification:** No, initial verification

Per the run brief: no adb command was issued, no device was touched, no gsd-tools call was made, nothing was committed. Judged on code at `main` HEAD `998ab46` and on tests I re-ran myself. `covered_files` / `covered_digest` were not written because they need a `gsd-tools` call; the orchestrator can stamp them.

## Goal Achievement

### Observable Truths (ROADMAP success criteria are the contract)

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 1 | SC1 A11Y-02: TalkBack on the TESTER reads ReceiptRow as one button with state and a separate Undo; refused-undo Assertive region spoken; saved-key summary one item with Update/Clear separate; group header a heading; sheet target rows and snackbar cards announce their open label | PRESENT_BEHAVIOR_UNVERIFIED | Structure is proven in code and tests. `ReceiptRow.kt:298-305` merged `Role.Button` + `stateDescription` Collapsed/Expanded. `OutcomeSheet.kt:275` `liveRegion = Assertive` on the refused surface. `UndoCenterScreen.kt:323` `heading()` on the group label. `OutcomeSheetReceipt.kt:192` heading on the title. Open labels pass through byte for byte. `A11ySweepSemanticsTest` 9/9, `A11ySweepUndoCenterSemanticsTest` 10/10, `A11ySweepFeedbackCardTest` 3/3 pass on my forced fresh run, including falsification controls (Undo node outside the summary, subtitle not a heading, composed vs decomposed label not equal). What is not proven: the spoken output and, for the refused-undo region, the event text the surface emits (candidate C-1, structure characterized only, no `mergeDescendants` added by design). Needs Gate-1 G1-2 to G1-5, P-1 to P-6 parked for Yahir if speech cannot be captured. Not FAILED |
| 2 | SC2 DEMO-02: every Explorer screen renders in the pinned hub scheme (warn never mauve under dynamic color) and the hoisted `OutcomeSheet` `sheetState` demo closes through `hide()` | VERIFIED (JVM); on-device screencap is Gate-1 G1-1 | `ExplorerTheme.kt` calls `YahirAndroidTasteTheme(themeMode, dynamicColor = false)`. All 15 screen/entry files call it; the only direct `YahirAndroidTasteTheme(` in `explorer/` is inside the helper (grep). Activity (`ExplorerActivity.kt:22`), index, token browser, detail screen, all family screens and the not-found fallback (`ExplorerEntry.kt:153`) are wrapped. `ExplorerSchemePinGuardTest` 4/4 (a source scan that failed naming six files before migration, per 28-03). `ExplorerThemeTest` 5/5 asserts the resolved surface/primary/warn equal the hub scheme and differ from a foreign outer scheme, light, dark, and system-follows-night. Hoisted demo: `VoiceCommandFamilyScreen.kt:674-676` calls `sheetState.hide()` and clears the slot in `invokeOnCompletion`; `HoistedSheetDemoRegistryRenderTest` 5/5 (still composed mid-slide, gone after, re-showable) |
| 3 | SC3 DEMO-03: stacked-snackbar demo (fire, burst of 4 past the cap, swipe, tap to open with the opened key shown) and go-to-item demos (Success single, batch with mixed rows, Undo Center with target rows), each in English and German, each reached by a registry render test | VERIFIED (JVM); device feel is Gate-1 G1-7 | `ReleaseDemos.kt`: `FeedbackStackStageDemos` (English + German stage, 7 buttons, `Last result` and `Opened: key` readouts, German `Rückgängig/Verwerfen/Eintrag öffnen`), wired from `FeedbackFamilyScreen.kt:277`; `UndoCenterTargetRowsDemos` (rows A open, B no target, C Undone, with readout), wired at `FeedbackFamilyScreen.kt:254`; `GoToItemSheetDemos` (8-trigger enum: single/batch x Receipt/Classic x EN/DE, own slot and own hoisted state, hide-then-record), wired at `VoiceCommandFamilyScreen.kt:660`. Tests pass: `FeedbackStackDemoRegistryRenderTest` 15/15 (burst keeps exactly 3, body tap after burst opens `demo-burst-4`, swipe, German labels and key readout, 360dp bounds), `UndoCenterTargetRowsDemoRegistryRenderTest` 6/6, `GoToItemSheetDemosRegistryRenderTest` 14/14, `ReceiptDemosRegistryRenderTest` 14/14 (unedited, so no stacking regression) |
| 4 | SC4: every new public composable from Phases 25-27 is registered in `ComponentRegistry` or allowlisted, registry integrity test and CATALOG drift guard green | VERIFIED | `ComponentRegistryDriftGuardTest` 1/1 and `CatalogDocsDriftGuardTest` 10/10 pass on the forced run (the latter includes the API.md/README count check). `ComponentRegistry.kt` has no diff since `c1681ff`, `api.txt` has no diff since `c1681ff` (Phase 28 added no public composable); `FeedbackStackHost` is registered (`FeedbackFamilyScreen.kt:131`). 28-07 recorded 67 registered, 6 unregistered, 73 total confirmed by the guards |
| 5 | SC5 POL-03: the caller's user agent reaches the wire | PRESENT_BEHAVIOR_UNVERIFIED (JVM bullets VERIFIED; wire-level bullet pending Gate-1 G1-6) | See POL-03 below |

**Score:** 3/5 truths verified (SC2, SC3, SC4); 2 present and wired but behavior only partly provable off-device (SC1, SC5). 0 failed.

### POL-03 detail (SC5 sub-bullets)

| Sub-bullet | Status | Evidence |
|------------|--------|----------|
| `additionalHttpRequestProperties["User-Agent"]` equals the resolved UA after a first and a second `configureOsmdroid`; pre-seeded prefs value overwritten; one entry | VERIFIED | `PlaceMapOsmdroidConfig.kt` `configureOsmdroid` + `applyUserAgentHeader`: removes non-exact case variants, puts only when missing/different, never writes the persisted prefs key (grep: no `additionalHttpRequestProperty` write in main). `PlaceMapOsmdroidConfigTest` 47/47: `secondCallWithAnotherAgent_replacesTheHeader_andKeepsExactlyOneUserAgentEntry`, `persistedUserAgentValue_isOverwrittenInMemory_andTheHubNeverRewritesThePrefs`, cased-variant test, `secondCallDoesNotReload_andKeepsAnUnrelatedRequestProperty`, `aRemovedHeaderIsReappliedOnTheNextCall` |
| Gap plan 28-08 (CONTEXT "load() race"): `load()` at most once per process, header re-applied without reload | VERIFIED | `osmdroidLoaded` AtomicBoolean + `osmdroidLoadLock`; exactly one `Configuration.getInstance().load(` call site, inside `!osmdroidLoaded.get()`; later calls only `userAgentValue` assign + `applyUserAgentHeader`. `resetOsmdroidLoadForTests` test hook proves the reload occurs only after reset. Residual: the guard covers hub callers only, a consumer with live osmdroid threads before the first hub call is still exposed. This is documented as a precondition in the KDoc (review WR-02), not closed in code |
| Blank-derived or `com.example` UA logs one `Log.w` | VERIFIED | `Log.w` in `configureOsmdroid` gated on `sanitizeUserAgent(userAgent).isEmpty() || isBlockedClassUserAgent(resolvedAgent)`; tests: blank agent exactly one warning, placeholder-prefixed exactly one, placeholder package + blank agent exactly one (not two), real agent none, `warningCountPerCallIsUnchangedOnLaterCalls`. `com.example.` appears once in main (the detection constant, `PlaceMapOsmdroidConfig.kt:77`) |
| Placeholder rows (6987 B + pinned md5) purged, real tiles kept | VERIFIED | `purgePlaceholderTiles`: `length(tile) = CAST(? AS INTEGER)` then md5 compare, deletes by key+provider in one transaction, no-op `DatabaseErrorHandler`, missing file never created, `-1` on lock/corrupt. Constants 6987 / `c069a15b...`, proven against a real fetched fixture `osm-blocked-placeholder.png`. `PlaceMapTilePurgeTest` 12/12 (size-only match survives, 594 of 600 placeholders deleted while real tiles survive, garbage file returns -1). Scan runs off the main thread (CR-01 fix) with a single-flight guard |
| On the TESTER an Explorer picker with a real-id UA caches non-placeholder tiles (wire-level) | UNVERIFIED (device) | Gate-1 G1-6. Tooling exists and is tested: `tools/verify-osm-tile-cache.py` (15/15 fixtures per 28-07 `run-all.sh`), `tools/probe-osm-user-agent.sh`. Explorer passes `EXPLORER_MAP_USER_AGENT` in both picker demos (`PickersFamilyScreen.kt:360,406`; `ExplorerMapUserAgentTest` 4/4) |
| Host `curl -A` probe with the exact resolved strings | Recorded, not re-run | 28-01 recorded live results 2026-10-17: Explorer agent `BLOCKED=no` (4472 B, md5 `ce9c4e90...`), `com.example.probe/1.0` `BLOCKED=yes` (6987 B, md5 `c069a15b...`). I did not re-run it (network call, outside the offline brief); Gate-1 G1-6 re-runs it |

### Required Artifacts

| Artifact | Expected | Status | Details |
|----------|----------|--------|---------|
| `component/PlaceMapOsmdroidConfig.kt` | UA injection, load-once, Log.w, purge | VERIFIED | Substantive, wired from `PlaceMapPicker.kt:717` |
| `explorer/ExplorerTheme.kt` | helper pinning the static hub scheme | VERIFIED | internal, used by every screen |
| `explorer/ReleaseDemos.kt` | stack stage, Undo Center target rows, go-to-item demos, `HubSchemeScope` | VERIFIED | 1000 lines, three entry points wired from family screens |
| `feedback/UndoCenterScreen.kt` | openable row as one merged button (C-2 fix) | VERIFIED | `mergeDescendants` + single `combinedClickable` owner on the row; 27-era interaction suite still passes |
| `tools/probe-osm-user-agent.sh`, `tools/verify-osm-tile-cache.py`, `tools/verify-tester-restored.sh` (+ fixture tests) | host probe, offline cache check, read-only restore oracle | VERIFIED | review-fix WR-03/WR-04/IN-02/IN-09 hardening landed; `tools/test/run-all.sh` recorded green by the fixer (82/15 checks) |
| 8 new test classes + 3 `A11ySweep*` + hygiene contract | see tests | VERIFIED | all in my forced fresh run below |

### Key Link Verification

| From | To | Via | Status |
|------|----|-----|--------|
| `PlaceMapPicker` factory | `configureOsmdroid(context, userAgent)` | `PlaceMapPicker.kt:717`, before the `MapView` is built | WIRED |
| Every Explorer screen | `ExplorerTheme` -> `YahirAndroidTasteTheme(dynamicColor=false)` | direct calls, guard-scanned | WIRED |
| `FeedbackFamilyScreen` | `FeedbackStackStageDemos`, `UndoCenterTargetRowsDemos` | lines 277 and 254 | WIRED |
| `VoiceCommandFamilyScreen` | `GoToItemSheetDemos`, hoisted `sheetState.hide()` demo | lines 660, 674-676 | WIRED |
| `FeedbackStackHost(onOpenTarget)` in demo | `Opened: key` readout | `lastOpened = it.key` | WIRED |
| go-to-item `OutcomeSheet(onOpenTarget)` | hide() then readout | `invokeOnCompletion`, only when `!sheetState.isVisible` | WIRED |

### Data-Flow Trace (Level 4)

| Artifact | Data | Source | Real data | Status |
|----------|------|--------|-----------|--------|
| Stack stage readouts | `lastResult`, `lastOpened` | `state.show(event)` result and `onOpenTarget` callback of the real `FeedbackStackHost` | Yes | FLOWING |
| Undo Center demo readout | `opened` | real `UndoCenterScreen` second overload | Yes | FLOWING |
| `User-Agent` request property | resolved agent | `resolveOsmdroidUserAgent` of the caller param | Yes | FLOWING (to the in-memory map; wire is G1-6) |

### Behavioral Spot-Checks (run by the verifier)

| Behavior | Command | Result | Status |
|----------|---------|--------|--------|
| Phase 28 test classes, forced fresh | `./gradlew --no-daemon testDebugUnitTest --rerun-tasks --tests` on PlaceMapOsmdroidConfigTest, PlaceMapTilePurgeTest, ExplorerMapUserAgentTest, ExplorerThemeTest, ExplorerSchemePinGuardTest, Phase28HygieneSourceContractTest, FeedbackStackDemo/UndoCenterTargetRowsDemo/GoToItemSheetDemos/HoistedSheetDemo/ReceiptDemos RegistryRenderTest, `*A11ySweep*`, ComponentRegistryDriftGuardTest, CatalogDocsDriftGuardTest, LibraryInvariantsGuardTest | BUILD SUCCESSFUL, 35 of 35 tasks executed (none up-to-date). 17 classes, 171 tests, 0 failures, 0 errors, 0 skipped (from the XML reports) | PASS |
| Debt markers in files changed since `c1681ff` | grep `TBD|FIXME|XXX` over changed `src`/`tools` | one hit: `adb-XXXX-yyyy...` is a fake mDNS serial string in `test-verify-tester-restored.sh:148`, test data, not a marker | PASS |
| `api.txt` / locked files unchanged | `git diff --stat c1681ff HEAD -- api.txt ComponentRegistry.kt OutcomeSheet.kt feedback/` | only `feedback/UndoCenterScreen.kt` (the deliberate C-2 internal move, +17/-15); no `api.txt`, `ComponentRegistry.kt`, `OutcomeSheet.kt`, `FeedbackCard.kt` or `FeedbackStackHost.kt` change | PASS |
| No tag cut | `git tag --list 'v2.8*'` | empty | PASS |

I did not re-run `detekt`, `apiCheck` or `tools/test/run-all.sh` (single filtered Gradle run per brief). Their latest recorded results are in 28-07-SUMMARY (fresh forced gate: 179 suites, 1855 tests, 0 failures, detekt and apiCheck green, `API SOURCE-ADDITIVE OK`) and 28-REVIEW-FIX (final `testDebugUnitTest --rerun-tasks` green, `api.txt` byte-identical, detekt green). The review fixes landed after the 28-07 gate; the fixer's re-run plus my targeted run cover every file they changed.

### Probe Execution

Step 7c: not applicable. The phase declares no `scripts/*/tests/probe-*.sh`. `tools/probe-osm-user-agent.sh` is a network probe (explicitly Gate-1's G1-6, not run here); `bash -n` coverage only, per 28-REVIEW-FIX.

### Requirements Coverage

| Requirement | Source Plans | Description | Status | Evidence |
|-------------|--------------|-------------|--------|----------|
| A11Y-02 | 28-06, 28-07 | scoped TalkBack sweep | NEEDS HUMAN (structure SATISFIED) | Layer-1 tests green; spoken output/live-region event text is Gate-1 |
| DEMO-02 | 28-03, 28-05, 28-07 | whole Explorer in pinned hub scheme; hoisted `sheetState` demo calls `hide()` | SATISFIED (JVM) | SC2; G1-1 screencap pending |
| DEMO-03 | 28-04, 28-05, 28-07 | stacked-snackbar and go-to-item demos EN+DE; registry | SATISFIED (JVM) | SC3 and SC4; G1-7 pending |
| POL-03 | 28-01, 28-02, 28-07, 28-08 | UA reaches OSM, blocked agent never silent | SATISFIED in code and Robolectric; NEEDS HUMAN for the wire | SC5 |

All four IDs from the phase brief appear in `REQUIREMENTS.md` (lines 33, 37, 38, 41; traceability lines 87-90, all "Phase 28") and in plan frontmatter (28-01/02/08: POL-03; 28-03: DEMO-02; 28-04: DEMO-03; 28-05: DEMO-03, DEMO-02; 28-06: A11Y-02; 28-07: all four). Orphaned requirements: none (no other ID is mapped to Phase 28).

### Judgment-tier prohibitions (non-authoritative judge verdict)

Held, each backed by a deterministic check where one exists. Recorded as `unverified-prohibition: human review recommended`, not blocking.

| Prohibition (plan) | Verdict | Backing |
|--------------------|---------|---------|
| No second `User-Agent` entry, no write of the persisted prefs key (28-01, 28-08) | Held | grep: no request-property key write in main; `...KeepsExactlyOneUserAgentEntry...` tests; `Phase28HygieneSourceContractTest` |
| No consumer id or extra `com.example` literal; one detection constant only (28-01, 28-08) | Held | grep shows a single occurrence at `PlaceMapOsmdroidConfig.kt:77`; hygiene test |
| No public signature or `api.txt` change, no cache-manager / bulk / expiry API (28-01, 28-02, 28-08) | Held | `api.txt` unchanged since `c1681ff`; hygiene test forbids the identifiers |
| No UA-keyed or whole-cache wipe, no size-only purge, never create the db (28-02) | Held | purge is fingerprint-matched parameterized delete, size-only survives test, missing file returns 0 |
| Pin every screen, not only the root; token swatch colors, content and registry counts untouched (28-03) | Held | guard test plus no diff in `ComponentRegistry.kt`/`api.txt` |
| Do not change `feedback/` sources other than the declared C-2 move; `FeedbackCard`/`FeedbackStackHost`/`OutcomeSheet.kt` locked; no `mergeDescendants` on the live-region surface (28-04, 28-06) | Held | diff shows only `UndoCenterScreen.kt` changed under `feedback/`; `OutcomeSheet.kt` and the two locked files unchanged |
| Demos stack no second sheet, do not add `ReceiptDemo` entries, library never hides or navigates (28-05) | Held | own slot and state for go-to-item; `ReceiptDemosRegistryRenderTest` unedited and green |
| Gate-1 must not waive/park automatable checks, no `wm density reset`, no wireless phone (28-07) | Not yet exercised | belongs to the Gate-1 run; the restore oracle refuses any serial that is not plain USB shape |

## Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
|------|------|---------|----------|--------|
| `explorer/ReleaseDemos.kt` | go-to-item and Undo Center demos | English-only library chrome in the German demos (Undo Center `Undo`, `Preview`, `Undone`, `Clear history`, `Undo History`) and an English `Show sheet` button / initial `Opened: none` readout | Info | Known and recorded (FA-3/FA-4, deferred to v2.8.1 in 28-04/28-06/27-VERIFICATION). The caller-supplied German strings render; the library's own hard-coded words do not. SC3 "in English and German" is met for the strings the caller controls |
| `component/PlaceMapOsmdroidConfig.kt` | `configureOsmdroid` KDoc precondition | Load-once guard covers hub callers only | Warning | Residual race if a consumer already has live osmdroid tile threads before the first hub call (review WR-02, documented, not closable inside the hub). Not a SC5 failure; SecondBrain should call it first |
| `component/PlaceMapOsmdroidConfig.kt` | test hooks `resetPlaceholderScanForTests`, `resetOsmdroidLoadForTests`, `placeholderPurgeExecutor`, `resetPlaceholderPurgeExecutorForTests` | internal test hooks compiled into the AAR | Info | Review IN-08, documented acceptable skip; `internal`, absent from `api.txt` |
| POL-03 | no response-side detection | A blocked tile *response* (`x-blocked`) is not detected at runtime; only the config-time `Log.w` fires | Info | CONTEXT scoped this "only if cheap" (osmdroid's downloader is internal) and the ROADMAP criterion asks only for the `Log.w`. The purge is the recovery path. Suggest a backlog item if wanted |

No stubs, TODO/HACK/placeholder returns, or empty handlers found in the phase's production code. No unreferenced debt markers.

## Gate-1 hand-off (what only the TESTER can confirm)

Use the contract in `28-07-PLAN.md` (G1-0 to G1-8, parking list P-1 to P-6). Tester serial `R5CT10XNKQN`, USB only, `wm density 450` on restore (never reset), then `tools/verify-tester-restored.sh`.

1. G1-1 DEMO-02 pixel check against the `v2.7.1` control (human item above).
2. G1-2 to G1-5 A11Y-02 tree dump and a time-boxed TalkBack attempt; include the C-1 live-region event text (SC1). If the event and node text are empty, that is a defect for a gap-closure plan (recipe PD-4 of 28-06), not a waiver.
3. G1-4 C-2 regression smoke and C-3/C-4 evidence for the designer.
4. G1-6 POL-03 wire-level tile cache check and the two host probes (SC5).
5. G1-7 DEMO-03 device walkthrough EN/DE at 360dp and font_scale 2.0.
6. CR-01 device sanity for the off-thread placeholder scan.
7. G1-8 restore, then release both leases.

Spoken-only items (P-1 to P-5) may be parked for Yahir only if G1-5 cannot capture speech; never waived. v2.6 `uat_gaps` rows (21-24) stay open until Yahir signs them.

## Gaps Summary

No gaps. Nothing is missing or stubbed. SC2, SC3 and SC4 are fully backed by passing tests on a forced fresh run and by code inspection. SC1 and SC5 are implemented, wired and structurally proven, but each has a claim (what TalkBack speaks, what reaches the OSM server) that only the TESTER can confirm, so they are held as PRESENT_BEHAVIOR_UNVERIFIED and the phase status is `human_needed` rather than `passed`. If Gate-1 returns a defect on C-1 or on the wire check, route it to a gap-closure plan.

---

_Verified: 2026-10-18T01:21:30Z_
_Verifier: Claude (gsd-verifier)_
