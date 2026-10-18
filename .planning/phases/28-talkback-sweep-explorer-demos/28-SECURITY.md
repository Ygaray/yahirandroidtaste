---
phase: "28"
slug: talkback-sweep-explorer-demos
status: verified
threats_open: 0
asvs_level: 1
audited_head: ea169f0bc3e7263255d866094bb26bad071f9bfa
created: "2026-10-17"
---

# Phase 28 - Security

> Per-phase security contract: threat register, accepted risks, and audit trail.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| Caller to library | Host app passes a user agent string to `PlaceMapPicker` / `configureOsmdroid` | A display-free identifying string (becomes an HTTP header) |
| Library to OSM tile server | osmdroid fetches public map tiles with the resolved `User-Agent` | Tile coordinates, the agent string |
| Untrusted cache file to tools | `tools/verify-osm-tile-cache.py` reads a pulled `cache.db` | Public tile blobs |
| Agent to the shared TESTER device | adb commands change device settings (density, font scale, accessibility services) | Display and accessibility settings only |
| Agent to the personal phone | A wireless adb transport exists on the same host and must never be addressed | Nothing (must not cross) |

---

## Threat Register

| Threat ID | Category | Component | Severity | Disposition | Mitigation | Status |
|-----------|----------|-----------|----------|-------------|------------|--------|
| T-28-01 | Tampering | `resolveOsmdroidUserAgent` header injection | medium | mitigate | `sanitizeUserAgent` strips ISO control chars before trim (`PlaceMapOsmdroidConfig.kt`); `PlaceMapOsmdroidConfigTest` control-character and control-only-agent tests | closed |
| T-28-02 | Spoofing | Fallback agent | low | mitigate | `buildFallbackUserAgent` uses only packageName, versionName, Android release; `isBlockedClassUserAgent` triggers the `Log.w`; honest-form and placeholder-warning tests | closed |
| T-28-03 | Information disclosure | `Log.w` line | low | mitigate | Single `Log.w` carries only the resolved agent and the policy URL; `Phase28HygieneSourceContractTest.placeMapConfigWarnsExactlyOnce_andNoOtherLogLevelIsUsed`, `pickerAndExplorerFilesNeverLogOrPrint` | closed |
| T-28-04 | Denial of service | `tools/probe-osm-user-agent.sh` | low | accept | One fixed tile, `curl --max-time 20`, no loop; developer tool, not shipped | closed (accepted) |
| T-28-05 | Tampering | Persisted request properties | low | mitigate | In-memory overwrite only; hygiene test forbids `putString(`, `putStringSet(` and the `additionalHttpRequestProperty.` key family; precedence test | closed |
| T-28-06 | Tampering | `purgePlaceholderTiles` SQL injection | high | mitigate | `CAST(? AS INTEGER)` bound arg; `db.delete("tiles", "key = ? AND provider = ?", args)`; md5 compared in Kotlin; `PlaceMapTilePurgeTest.providerWithAnApostrophe_isDeletedSafely` | closed |
| T-28-07 | Tampering | Over-broad delete (data loss) | medium | mitigate | Deletes only on size AND md5; `sizeOnlyMatch_isKept`, `realTile_isKept`, `changingTheAgentNeverWipesRealTiles`; hygiene test bans purgeCache, CacheManager and Expiry; no-op `DatabaseErrorHandler` | closed |
| T-28-08 | Denial of service | Placeholder scan on map creation | low | mitigate | `SQLiteException` returns -1; after review fix CR-01 the scan runs on a daemon thread, single-flight, a rejected task releases the guard; off-thread, overlap, rejection and failed-scan tests | closed |
| T-28-09 | Information disclosure | Cache contents | low | accept | Cache in app-private `cacheDir/osmdroid`; blobs read only for 6987-byte rows; the purge logs nothing. Residual (review IN-04): osmdroid first-run `load()` may persist its default paths | closed (accepted) |
| T-28-10 | Tampering | `verify-osm-tile-cache.py` on an untrusted pulled DB | low | mitigate | `?mode=ro&immutable=1` URI; static SQL; `python3 -I`; malformed file exits 2; `test-verify-osm-tile-cache.sh` (PASS=15) | closed |
| T-28-11 | Tampering | Regression of the Explorer scheme pin | low | mitigate | `ExplorerSchemePinGuardTest` scans every `explorer/*.kt` with a vacuity guard; only `ExplorerTheme.kt` calls `YahirAndroidTasteTheme(` with `dynamicColor = false` | closed |
| T-28-12 | Information disclosure | Theme helper | low | accept | No data flows through a color wrap | closed (accepted) |
| T-28-13 | Information disclosure | "Opened: key" readout | low | accept | `demo-*` fixture string shown on purpose; library never reads `.key` (`OpenTargetHygieneSourceContractTest`) | closed (accepted) |
| T-28-14 | Tampering | Demo copy drifting into consumer wording | low | mitigate | `LibraryInvariantsGuardTest` release-demo-copy scan with a vacuity guard | closed |
| T-28-15 | Denial of service | Indefinite demo cards on screen | low | accept | Explorer-only; "Dismiss all" and swipe clear them | closed (accepted) |
| T-28-16 | Information disclosure | Opened-key readout (28-05) | low | accept | `demo-*` fixture keys only | closed (accepted) |
| T-28-17 | Tampering | Demo copy drift (28-05) | low | mitigate | Same as T-28-14 | closed |
| T-28-18 | Denial of service | Two modal sheets stacked by a demo | low | mitigate | `GoToItemSheetDemos` owns one `shown` slot and its own hoisted sheet state; `ReceiptDemosRegistryRenderTest` unedited and green; `GoToItemSheetDemosRegistryRenderTest` | closed |
| T-28-19 | Information disclosure | Undo Center row exposes message and timestamp in one node | low | accept | Merging changes the stop count, not the exposed content; the key stays unread (`UndoCenterScreenOpenTargetTest.opaqueKey_neverAppearsInTheSemanticsTree`) | closed (accepted) |
| T-28-20 | Tampering | Row interaction regression after the modifier move | medium | mitigate | One `combinedClickable` owner with `mergeDescendants`; `UndoCenterScreenOpenTargetTest` (21), `A11ySweepUndoCenterSemanticsTest` (10), parity and long-label tests | closed |
| T-28-21 | Repudiation | Locked Undo and Dismiss semantics altered later | low | mitigate | `A11ySweepFeedbackCardTest` locked-forms test; `FeedbackCard.kt` and `FeedbackStackHost.kt` have an empty diff since `c1681ff` | closed |
| T-28-22 | Tampering | Driving the wrong device (wireless phone, or owner mid-test) | high | mitigate | `tools/verify-tester-restored.sh` serial allowlist `^[A-Za-z0-9]+$` refuses host:port, mDNS and metachar serials before any adb call (review fix WR-04); `--package` regex (WR-03); fixture test PASS=82 asserts an empty fake-adb log for each refusal; USB-only serial and lease rules bound in `28-07-PLAN.md`. Runtime half is checked against the Gate-1 log | closed |
| T-28-23 | Tampering | Tester left modified (density, font scale, a11y service, harness) | high | mitigate | G1-0 record and G1-8 restore (`wm density 450`, never reset) in `28-07-PLAN.md`; read-only oracle `tools/verify-tester-restored.sh`; fixture asserts no state-changing adb verb. Runtime half is checked against the Gate-1 log | closed |
| T-28-24 | Tampering | Third-party TalkBack-control APK or unvetted tool on the tester | high | mitigate | Prohibition in `28-07-PLAN.md`; throwaway observer built from local source in the scratchpad only; no `.apk`/`.aab` tracked; no build or dependency file changed. Runtime half is checked against the Gate-1 log | closed |
| T-28-25 | Information disclosure | Evidence screencaps and pulled `cache.db` | low | accept | Fixture strings and public map tiles; evidence stays in the phase dir | closed (accepted) |
| T-28-26 | Repudiation | Gate-1 result narrated instead of measured | medium | mitigate | Script oracles `verify-tester-restored.sh` and `verify-osm-tile-cache.py` pass their fixtures; plan forbids hand-written SELF-UAT or pending entries. SELF-UAT authorship is the Gate-1 tester's | closed |
| T-28-30 | Denial of service | `ConcurrentModificationException` from `load()` clearing the map | medium | mitigate | `osmdroidLoaded` AtomicBoolean inside `synchronized(osmdroidLoadLock)` gates `load()` once per process; later calls only put on an existing key; load-once tests. Documented residual (WR-02): the guard covers hub callers only | closed |
| T-28-31 | Tampering | Tile fetched with the normalized blocked agent during the clear window | medium | mitigate | Same fix as T-28-30: header applied in the same synchronized block right after the single `load()`; `aRemovedHeaderIsReappliedOnTheNextCall`; the purge (T-28-06 to T-28-08) is the recovery path | closed |
| T-28-SC | Tampering | npm, pip or cargo installs | high | accept | No dependency change: `build.gradle.kts`, `gradle/libs.versions.toml`, `settings.gradle.kts` have no diff since `c1681ff` | closed (accepted) |

*Status: open · closed · open - below high threshold (non-blocking)*
*Severity: critical > high > medium > low - only open threats at or above workflow.security_block_on count toward threats_open*
*Disposition: mitigate (implementation required) · accept (documented risk) · transfer (third-party)*

---

## Accepted Risks Log

| Risk ID | Threat Ref | Rationale | Accepted By | Date |
|---------|------------|-----------|-------------|------|
| AR-28-01 | T-28-04 | Developer probe tool, one tile, no loop, not shipped | plan disposition (accept) | 2026-10-17 |
| AR-28-02 | T-28-09 | Cache stays app-private; residual osmdroid first-run default paths (IN-04) | plan disposition (accept) | 2026-10-17 |
| AR-28-03 | T-28-12 | The scheme helper carries no data | plan disposition (accept) | 2026-10-17 |
| AR-28-04 | T-28-13 | Explorer fixture key `demo-*` shown on purpose | plan disposition (accept) | 2026-10-17 |
| AR-28-05 | T-28-15 | Explorer-only; dismiss-all and swipe clear the cards | plan disposition (accept) | 2026-10-17 |
| AR-28-06 | T-28-16 | Explorer fixture key readout (28-05) | plan disposition (accept) | 2026-10-17 |
| AR-28-07 | T-28-19 | The row exposes nothing new; the key is still unread | plan disposition (accept) | 2026-10-17 |
| AR-28-08 | T-28-25 | Fixture strings and public map tiles only | plan disposition (accept) | 2026-10-17 |
| AR-28-09 | T-28-SC | No dependency change in the phase | plan disposition (accept) | 2026-10-17 |

---

## Unregistered Flags (non-blocking, low)

1. CR-01 background purge shares `cache.db` with osmdroid's `SqlTileWriter`: fail-soft (own connection, lock timeout returns -1, only exact placeholder rows are deleted). Device-side lock timing is a Gate-1 item.
2. Review IN-08: the test reset hooks are `internal` in `src/main`; `api.txt` is byte-identical.
3. WR-02 residual: a consumer with live osmdroid tile threads before the hub's first `configureOsmdroid` call is not protected; documented as a KDoc precondition (relates to T-28-30, T-28-31).

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-10-17 | 29 | 29 | 0 | gsd-security-auditor (ASVS L1, block_on high), audited_head ea169f0 |

Implementation notes: the T-28-08 plan text said the purge "can move off-main"; review fix CR-01 moved it to a daemon thread with the same guarantees. T-28-22 to T-28-24 and T-28-26 are procedural; their runtime half is re-checked against the Gate-1 log after Gate-1.

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed
- [x] `status: verified` set in frontmatter

**Approval:** verified 2026-10-17
