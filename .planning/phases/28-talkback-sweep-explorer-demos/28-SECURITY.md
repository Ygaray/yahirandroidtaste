---
phase: "28"
slug: talkback-sweep-explorer-demos
status: verified
threats_open: 0
asvs_level: 1
audited_head: 4a549cbab9ba30aa8c65a43bc24de34a0c8e11b0
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
| T-28-20 | Tampering | Row interaction regression after the modifier move | medium | mitigate | One `combinedClickable` owner with `mergeDescendants` (`UndoCenterScreen.kt`); `UndoCenterScreenOpenTargetTest` (21) and `A11ySweepUndoCenterSemanticsTest` (10) green; `UndoCenterScreen.kt` and both tests have an empty diff since `ea169f0`. Re-confirmed on the tester after the gap plans: C-2 smoke PASS in `28-07-SELF-UAT.md` (one merged row node, Undo separate, tap opens, hold peeks without opening, Undo undoes without opening, Enter opens) | closed |
| T-28-21 | Repudiation | Locked Undo and Dismiss semantics altered later | low | mitigate | `FeedbackCard.kt` and `FeedbackStackHost.kt` changed in 28-10 to 28-12 (names, hide of the middle strip, single speech source), the locked forms still hold: system `dismiss(label)` plus custom Undo and Dismiss actions, the Undo child is its own node with `onClick` and `Role.Button`; `A11ySweepFeedbackCardTest.lockedForms_undoIsASeparateFocusableChild_andACustomAction_andBothDismissFormsExist` unedited and green; `FeedbackStackHostSemanticsTest` Undo click and custom-action tests green; device D-11/D-07 smoke PASS after 28-12 (Undo child activated by TalkBack double tap and keyboard; system and custom Dismiss on every visible card, English and German) | closed |
| T-28-22 | Tampering | Driving the wrong device (wireless phone, or owner mid-test) | high | mitigate | Static half: `tools/verify-tester-restored.sh` serial allowlist `^[A-Za-z0-9]+$` and package regex (review fixes WR-04, WR-03), fixture PASS=82 with an empty fake-adb log for each refusal. Runtime half (Gate-1 ran): all four signed `run.json` files name device R5CT10XNKQN / SM-S908U; a scan of the committed evidence finds no uninstall, `pm clear`, density reset, daemon-stop, dump-flag, connect/disconnect or 100.x address. Command-guard history: runs 1 and 3 each recorded one false-positive breach (script or file text that quoted a forbidden string was matched and refused), nothing forbidden ran | closed |
| T-28-23 | Tampering | Tester left modified (density, font scale, a11y service) | high | mitigate | G1-0 record and G1-8 restore (`wm density 450`, never reset); read-only oracle `tools/verify-tester-restored.sh` (fixture PASS=82); `RESTORED=yes` printed twice per run across the four runs; density read back 450 with no override, font scale 1.0, `enabled_accessibility_services` null, accessibility_enabled and touch_exploration_enabled 0; TalkBack developer switches read back as recorded. The harness leg is tracked as T-28-23b | closed |
| T-28-23b | Tampering | Throwaway harness packages left on the tester (harness leg of T-28-23) | low | mitigate | Plan G1-8 asked for an uninstall; the coordinator ruling for this run was NOT to uninstall, so `com.example.yat28head`, `com.example.yat28control` and `com.example.yat28obs` remain installed (SELF-UAT, VERIFICATION Gate-2 hand-off). The observer service is disabled. Resolution: Yahir removes the three packages (one command each) or accepts them in writing | open - below high threshold (non-blocking) |
| T-28-24 | Tampering | Third-party TalkBack-control APK or unvetted tool on the tester | high | mitigate | TalkBack was the pre-installed Samsung package; the only added tools are the gate's own throwaway packages built from local source (AAR and APK md5s recorded per run); no `.apk`, `.aab` or `.aar` tracked or added since `c1681ff`; build, dependency and publish files have an empty diff. Residual: the harness and observer sources lived in the session scratchpad and were never committed | closed |
| T-28-25 | Information disclosure | Evidence screencaps and pulled `cache.db` | low | accept | Fixture strings and public map tiles; evidence stays in the phase dir | closed (accepted) |
| T-28-26 | Repudiation | Gate-1 result narrated instead of measured | medium | mitigate | The auditor recomputed the sha256 of every capture in the four `run.json` files (448 captures, 0 mismatches, 0 files outside the record); fixtures re-run (PASS=82 and PASS=15); the SELF-UAT, its Addenda and the Gate-2 fragment come from the Gate-1 commits `5ba71f8`, `73b5451`, `0598142`, `83aafb7`; the one edit to earlier verdict text (`83aafb7`) is disclosed and was verified by diff (frontmatter, summary counts and five heading markers only) | closed |
| T-28-30 | Denial of service | `ConcurrentModificationException` from `load()` clearing the map | medium | mitigate | `osmdroidLoaded` AtomicBoolean inside `synchronized(osmdroidLoadLock)` gates `load()` once per process; later calls only put on an existing key; load-once tests. Documented residual (WR-02): the guard covers hub callers only | closed |
| T-28-31 | Tampering | Tile fetched with the normalized blocked agent during the clear window | medium | mitigate | Same fix as T-28-30: header applied in the same synchronized block right after the single `load()`; `aRemovedHeaderIsReappliedOnTheNextCall`; the purge (T-28-06 to T-28-08) is the recovery path | closed |
| T-28-40 | Repudiation | Refused-undo message not announced (user believes the undo happened) | medium | mitigate | Merged Assertive live region on the refusal node, the only `liveRegion = LiveRegionMode.` in `OutcomeSheet.kt`; the named node is composed after the sheet settles; `OutcomeSheetLiveRegionTest` (19, green) incl. `refusedNode_exposesTheFullMessageAfterTheSettle`, `refusalInARealSheet_isSilentUntilTheSheetHasSettled_thenSpokenOnce`; device: spoken exactly once on 27 of 27 showings (14 of 14 with TalkBack alone). Gate-1 runs 1 to 3 had failed first | closed |
| T-28-41 | Tampering | Pinned contract test weakened to let a change pass | medium | mitigate | `OutcomeSheetClassicParitySourceContractTest.kt` has one hunk since `c1681ff` (commit `6406901`): the pinned string gains `mergeDescendants=true`, the no-merge assertion is replaced with a rationale comment, the exactly-one-live-region count is kept; 28-12 did not edit the file; test green | closed |
| T-28-42 | Denial of service | TalkBack user cannot reach the hidden middle card while the deck is collapsed | low | accept | ui-designer ruling (28-CONTEXT 2026-10-17): front and oldest card stay stops; the middle card is reachable once the deck is fanned by touch or the cards in front are dismissed; TalkBack has no fan action. Logged as AR-28-10 | closed (accepted) |
| T-28-43 | Tampering | Hide flag leaking onto a card that must stay readable | medium | mitigate | `hiddenFromAccessibility = !fanned && index > 0 && index < state.items.size - 1` applied only to the card root; front card never hidden, fanned deck hides nothing, 1 or 2 cards hide nothing; `FeedbackStackHostSemanticsTest` cases for 1, 2, 3 cards, collapsed and fanned, English and German; device deck walk PASS | closed |
| T-28-44 | Repudiation | Refusal message not announced, or announced twice (28-11 focus route) | medium | mitigate | The 28-11 focus route was measured read twice on the tester and removed in 28-12; `OutcomeSheet.kt` has no `focusRequester`, `focusable`, `requestFocus` or `announceForAccessibility`; `OutcomeSheetLiveRegionTest.refusedNode_isNotFocusRequested_andHasNoFocusMachinery` pins it; device: 0 view-focused and 0 announcement events in all 13 observer-bound showings | closed |
| T-28-45 | Information disclosure | A card name speaking supporting text or the opaque target key | low | mitigate | No `.key` read in `FeedbackCard.kt`, `FeedbackStackHost.kt` or `OutcomeSheet.kt`; `FeedbackTarget.toString()` omits the key; the name is `feedbackCardName(message, supportingText)` (text the card already draws); `FeedbackStackHostSemanticsTest.keyNeverAppearsInTheSemanticsTree` green | closed |
| T-28-46 | Denial of service | Collapsed back card unreachable for TalkBack | low | mitigate | Every card root carries a data-derived name and only the middle strip is hidden; name tests for collapsed and fanned decks, English and German; device deck walk: no empty-label node, each stop spoken once, Undo child activated by TalkBack double tap and keyboard | closed |
| T-28-47 | Repudiation | Refusal message not announced, or announced twice (28-12) | medium | mitigate | One mechanism only (the live region); the node is named only after `awaitPositionAtRest` (hard cap 1.5 s, so the message is always reached); the drawn Text is hidden from the platform tree so the name is the single speech source; tests `refusedNode_messageIsExposedOnceAcrossTextAndDescription` (English, German, changed item), `exactlyOneContentChangeEventPerShowing`, `equalStateRecomposition_causesNoSecondChange`, `aNewRefusal_causesOneNewChange`; device 27 of 27 once | closed |
| T-28-48 | Information disclosure | A name that speaks the opaque target key | low | mitigate | Names are built only from `item.message`, `item.supportingText`, `undoLabel` and the refusal fields; no `.key` in the three files; `OpenTargetHygieneSourceContractTest.noFileReadsTheOpaqueTargetKey` unchanged and green (it scans `OutcomeSheet.kt` but not `FeedbackCard.kt`, which is covered by the runtime key test and a direct grep) | closed |
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
| AR-28-10 | T-28-42 | Collapsed deck's silent middle strip hidden from TalkBack per ui-designer ruling (28-CONTEXT 2026-10-17); front and oldest card stay stops; middle card reachable after fan by touch or after the cards in front are dismissed; no TalkBack fan action | plan disposition (accept, 28-10-PLAN) plus ui-designer ruling | 2026-10-18 |

---

## Unregistered Flags (non-blocking, low)

1. CR-01 background purge shares `cache.db` with osmdroid's `SqlTileWriter`: fail-soft (own connection, lock timeout returns -1, only exact placeholder rows are deleted). Device-side lock timing is a Gate-1 item.
2. Review IN-08: the test reset hooks are `internal` in `src/main`; `api.txt` is byte-identical.
3. WR-02 residual: a consumer with live osmdroid tile threads before the hub's first `configureOsmdroid` call is not protected; documented as a KDoc precondition (relates to T-28-30, T-28-31).

4. Settle heuristic (T-28-40, T-28-47): the spoken-once result rests on a position-stability heuristic (300 ms, 6 frames, 1.5 s cap, no link to the `SheetState`) and on one TalkBack build (Samsung 15.1.01.1). The wait is bounded and cancels with the composition; if it fails the message stays drawn but unspoken, the pre-phase baseline.
5. Undo child semantics (T-28-46, T-28-21): the innermost `clearAndSetSemantics { }` drops the Undo node's Focused and RequestFocus entries; TalkBack and keyboard activation were re-verified on the tester.
6. Hide-rule comment: `FeedbackStackState(maxVisible)` is public; with a cap above three the rule hides every middle strip. The front card and a fanned deck are never affected.
7. Guard coverage gap: `OpenTargetHygieneSourceContractTest` lists neither `FeedbackCard.kt` nor `FeedbackStackHost.kt` although T-28-45 and T-28-48 cite it; both are clean by direct grep and the runtime key test. Recommend adding them in a later test-only change.
8. Procedural notes (T-28-22, T-28-26): the `device:tester` and `build` leases were held by the orchestrator seat, not by the gate; the command-guard store was not available to the auditor, so breach non-execution rests on the signed run records, the evidence scan and the SELF-UAT text; the cycle-3 evidence directory holds duplicated ingested files (hashes still verify).

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-10-17 | 29 | 29 | 0 | gsd-security-auditor (ASVS L1, block_on high), audited_head ea169f0 |
| 2026-10-18 | 39 | 38 | 1 (T-28-23b, non-blocking, below the high threshold; threats_open 0) | gsd-security-auditor re-audit after the Gate-1 gap plans 28-09 to 28-12 (ASVS L1, block_on high), audited_head 4a549cb |

Implementation notes: the T-28-08 plan text said the purge "can move off-main"; review fix CR-01 moved it to a daemon thread with the same guarantees. T-28-22 to T-28-24 and T-28-26 are procedural; their runtime half is re-checked against the Gate-1 log after Gate-1.

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed
- [x] `status: verified` set in frontmatter

**Approval:** verified 2026-10-17
