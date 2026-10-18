---
phase: 28-talkback-sweep-explorer-demos
verified: 2026-10-18T09:43:06Z
status: human_needed
score: 5/5 must-haves verified
behavior_unverified: 0
overrides_applied: 0
re_verification:
  previous_status: human_needed
  previous_score: 3/5
  gaps_closed: []
  gaps_remaining: []
  regressions: []
coincidental_reliance_items:
  - truth: "ROADMAP SC1 item 2 (A11Y-02): the refused-undo Assertive live region is spoken when it appears"
    reason: incidental-ordering
    harden: "The message is spoken once only because the named live-region node is composed AFTER the sheet window exists. The code infers that from the node's window position being still (6 frames, at least 300 ms, 1.5 s cap, OutcomeSheet.kt awaitPositionAtRest), not from the SheetState, which UndoAffordanceBody cannot reach. Nothing enforces that the window is attached when `spoken` flips. A declared precondition (pass the settled signal in, or document the dependency in the OutcomeSheet KDoc) would remove the reliance on a heuristic and on one TalkBack build (Samsung 15.1.01.1)."
human_verification:
  - test: "Gate-2 physical step 1 (SC1, ReceiptRow): with TalkBack on, ReceiptRow registry page, 'ReceiptRow - trailing action': from the focused row summary swipe right with one finger, then left"
    expected: "Right lands on the Undo button, left returns to the summary"
    why_human: "One-finger TalkBack swipes cannot be synthesized over adb on this device (input swipe only scrolled or dismissed the sheet). The Gate-1 run proved the two nodes are separate, in layout order, and each speaks correctly on explicit focus"
  - test: "Gate-2 physical step 2 (SC1, Undo Center heading): UndoCenterScreen group-rows demo, use TalkBack headings navigation"
    expected: "It reaches 'Batch edit' and 'Earlier batch' as headings"
    why_human: "Headings navigation is a gesture or reading-control action that cannot be injected. Gate-1 proved heading=true and the spoken 'Batch edit, Heading, In list, 8 items' on explicit focus"
  - test: "Gate-2 physical step 3 (SC1, openable Undo Center row): UndoCenterScreen go-to-item rows demo, swipe right through the row 'Changed item A' and count stops"
    expected: "The row first (message plus open label), then its Undo, nothing announced twice"
    why_human: "Swipe traversal cannot be synthesized. Gate-1 proved one merged row node plus a separate Undo child, each spoken once on explicit focus"
  - test: "Gate-2 physical step 4 (SC1, collapsed stacked-snackbar deck): FeedbackStackHost page, fire 'Burst x4', swipe right through the collapsed deck in English and German"
    expected: "Oldest strip, its Undo, then the front card, each named once, no silent stop, nothing spoken twice (the middle strip is not a stop)"
    why_human: "The cross-card speaking order comes from traversalIndex, which a node dump cannot show, and the swipe cannot be injected (also the C-3 note)"
  - test: "Gate-2 physical step 5 (SC1 item 2, explicit focus): TalkBack alone, Voice Command, OutcomeSheet, 'Success, loud undo-refused substate', Show sheet, then move TalkBack focus by one-finger swipes to the red 'Couldn't undo' message"
    expected: "Spoken once, no role"
    why_human: "Explicit focus by swipe cannot be injected with TalkBack alone, and a tap on the message speaks nothing because the node has no click action. The same node focused through the observer speaks once with no role, and the appearance is spoken once on 14 of 14 TalkBack-alone showings"
  - test: "Designer confirmation (ui-designer seat, not a failure): the refused live-region content-change events still carry empty text (TYPE_WINDOW_CONTENT_CHANGED changeTypes=1, text=[], desc=null) after the 2026-10-18 ruling asked for 'the observer shows the focus or announce event carrying the message text'"
    expected: "The designer accepts the spoken outcome (exactly once, 27 of 27 showings) as the C-1 acceptance, or asks for a follow-up"
    why_human: "It is a ruling-versus-evidence call for the seat that wrote the ruling. The ROADMAP wording (spoken when it appears) is met, the ruling's secondary event-text acceptance is not literally met"
---

# Phase 28: TalkBack sweep & Explorer demos Verification Report

**Phase Goal:** The new snackbars, the outcome sheet and the Undo Center pass a scoped TalkBack sweep on the TESTER, and the Explorer shows every v2.8 surface in English and German inside the pinned hub color scheme.
**Verified:** 2026-10-18T09:43:06Z
**Status:** human_needed (nothing failed; the only open items are the five physical TalkBack gestures that the Gate-1 self-UAT parks as PARTIAL for Gate-2, plus one designer confirmation)
**Re-verification:** Yes, after the Gate-1 self-UAT and gap plans 28-08 to 28-12. The previous report (`ea169f0`, status human_needed, 3/5 with SC1 and SC5 held as PRESENT_BEHAVIOR_UNVERIFIED) had no `gaps:` section.

Per the run brief: no adb command, no device touched, no gsd-tools call, nothing committed. Judged on code and tests at `main` HEAD `83aafb7` (library `src` and `tools` are byte-identical to `ca69f92`, the build the last Gate-1 run tested: `git diff ca69f92 HEAD -- src tools api.txt` is empty) plus the signed SELF-UAT evidence. `covered_files` / `covered_digest` were not written because they need a gsd-tools call; the orchestrator can stamp them.

## What changed since the first report

| Item | Effect on the verdict |
|------|-----------------------|
| Gap plans 28-09 to 28-12 changed `OutcomeSheet.kt`, `FeedbackCard.kt`, `FeedbackStackHost.kt` and added tests, in response to Gate-1 findings C-1 (refused-undo not spoken), C-5 (unnamed deck stops), C-6 (double read) | The first report listed these three files as locked. That prohibition was scoped to plans 28-04/28-06 and is deliberately superseded by the designer-authorized gap plans. `OutcomeSheetClassicParitySourceContractTest` was edited once, by 28-09, reversing the pinned "live region does not merge descendants" assertion (Phase 22 A9 named device evidence as the trigger; PD-4 of 28-06 pre-recorded the edit). `api.txt` and `ComponentRegistry.kt` are unchanged since `c1681ff` |
| Gate-1 self-UAT on the TESTER, four runs, last one `uatr-20261018T080238Z-a214dfe5` at HEAD `ca69f92` | Answers the device-only items of the first report. Verdict in the file: every automatable criterion passes, five physical steps PARTIAL |
| 28-SECURITY.md | `status: verified`, `threats_open: 0`, but `audited_head: ea169f0` predates the gap plans. The gap plans change accessibility semantics only (no new input, network or storage surface), but the orchestrator's re-audit is still open and I did not substitute for it |

## Goal Achievement

### Observable Truths (ROADMAP success criteria are the contract)

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 1 | SC1 A11Y-02: with TalkBack on the TESTER a ReceiptRow reads as one button with its state and a separate Undo; the refused-undo Assertive live region is spoken when it appears; the saved-key summary is one item with Update/Clear separate; the group header is a heading; sheet target rows and snackbar cards announce their open label | VERIFIED (Gate-1 device evidence, JVM at HEAD); five swipe/heading gestures parked for Gate-2 | See the sub-claim table below. Real TalkBack speech was captured on the SM-S908U tester (overlay transcripts, observer tree dumps, Samsung TTS utterance log) for every sub-claim; the physical one-finger gestures are Gate-2 items, not gaps |
| 2 | SC2 DEMO-02: every Explorer screen renders in the pinned hub scheme, warn never mauve; the hoisted `OutcomeSheet` `sheetState` demo closes through `hide()` | VERIFIED | Device: warn container pixel `(255,224,160)` hub amber in HEAD light, `(86,61,0)` HEAD dark, `(138,106,137)` mauve in the v2.7.1 control (SELF-UAT block 2, first run, evidence 06 to 12). JVM at HEAD: `ExplorerThemeTest` 5/5, `ExplorerSchemePinGuardTest` 4/4, `HoistedSheetDemoRegistryRenderTest` 5/5 in my fresh run. The slide-down through `hide()` is the unit claim; on the device only open, close, re-open and 0 sheet nodes are observable |
| 3 | SC3 DEMO-03: stacked-snackbar demo (fire, burst of 4, swipe, tap to open with the key shown) and go-to-item demos (single, batch with mixed rows, Undo Center target rows), English and German, each reached by a registry render test | VERIFIED | Device (first run, block 3): `Opened: demo-note`, burst of 4 shows 3, swipe, `Opened: demo-burst-4`, eight go-to-item variants, Undo Center rows, German at 360 dp / font 2.0 with nothing clipped once scrolled. Cycle-3 run re-exercised the stack stage and the Undo Center rows on the HEAD build (blocks 15 and 16). JVM at HEAD: `FeedbackStackDemoRegistryRenderTest` 15/15, `GoToItemSheetDemosRegistryRenderTest` 14/14, `UndoCenterTargetRowsDemoRegistryRenderTest` 6/6 |
| 4 | SC4: every new public composable from Phases 25-27 is registered or allowlisted; registry integrity test and CATALOG drift guard green | VERIFIED | `ComponentRegistryDriftGuardTest` 1/1 and `CatalogDocsDriftGuardTest` 10/10 in my fresh run. `git diff c1681ff HEAD -- api.txt ComponentRegistry.kt` is empty (the gap plans added no public composable). SELF-UAT recorded 67 registered, 6 unregistered, 73 on its own full forced run |
| 5 | SC5 POL-03: the caller's user agent reaches the wire (Robolectric UA map, one `Log.w`, placeholder purge, TESTER wire check) | VERIFIED | See POL-03 below. The code for it is unchanged since the first run (`PlaceMapOsmdroidConfig.kt` and `PlaceMapPicker.kt` are not in the `830caeb..HEAD` diff), so the first-run device result still describes HEAD |

**Score:** 5/5 truths verified. 0 present-but-behavior-unverified, 0 failed. Open: five Gate-2 physical steps and one designer confirmation (above).

### SC1 sub-claims against the ROADMAP wording

| Sub-claim | Verdict | Evidence at HEAD `ca69f92` |
|-----------|---------|----------------------------|
| 1. ReceiptRow is one button with Collapsed/Expanded; Undo is a separate stop | Met, swipe order Gate-2 | Code: merged `Role.Button` + `stateDescription` in `ReceiptRow.kt`. Device (SELF-UAT 16, evidence 07, 84 to 89): TalkBack speaks "Collapsed, Accordion row 1, Tap to open row 1, 250, Total, Button" then the hint, "Expanded" after a click; trailing-action row "Collapsed, Item title, Subtitle detail, 1,250, Button" with no Undo in it, and "Undo, Button" as its own node. Only the one-finger swipe between them is physical (Gate-2 step 1) |
| 2. Refused-undo "Couldn't undo" Assertive live region is spoken when it appears | Met in outcome, with the caveats below | See the C-1 section |
| 3. Saved-key summary is one item, Update and Clear separate buttons | Met | Evidence 06, 90 to 95: "Saved : key-...c3d4" once, "Update, Button", "Clear, Button"; German "Gespeichert...", "Aktualisieren, Button", "Entfernen, Button" |
| 4. Undo Center group header is a heading | Met, heading navigation Gate-2 | `UndoCenterScreen.kt` `heading()`; device speech "Batch edit, Heading, In list, 8 items", subtitle without a role (evidence 10, 96 to 98). Gesture is Gate-2 step 2 |
| 5. Sheet target rows and snackbar cards announce their open label | Met, English and German | actionList `16:Open item` / `16:Eintrag öffnen` on cards, sheet rows (batch rows only where a target exists, target-less rows bare), Undo Center row; spoken "Double-tap to Open item" and German equivalent (SELF-UAT block 1 item 5, block 15 and 16). After the C-6 change the card name still carries the open label (block 16: "Double-tap to Open item") |

### C-1 section (the criterion-1 wording versus the final implementation)

ROADMAP: "the refused-undo 'Couldn't undo' Assertive live region is spoken when it appears". What actually ships, read from `OutcomeSheet.kt` (`UndoAffordanceBody`, `ReplaceWhenChanged`, `awaitPositionAtRest`):

- The refusal `Surface` keeps `liveRegion = Assertive` and `mergeDescendants = true`, so it is still an Assertive live region (device node dump `live=2`, evidence 21).
- It is composed at once for layout, but silent: no `contentDescription`, drawn `Text` hidden via `hideFromAccessibility()`. When the surface's window position has been still for 6 frames and at least 300 ms (1.5 s cap), `spoken` flips and the same surface is composed from a second call site, i.e. a new node carrying the name. That new live-region node appearing after the sheet window exists is the one event Samsung TalkBack speaks. No focus move, no `announceForAccessibility` (greps in 28-12 SUMMARY: 0 hits for `focusRequester`, `awaitSheetSettled`, `announceForAccessibility`; one `liveRegion = LiveRegionMode.` in the file).
- Device result (SELF-UAT block 14, tally evidence 108, which I re-read): 27 of 27 valid showings spoke the message exactly once (one 198240-byte / 4.13 s utterance each, none of the 349440-byte doubled readings of cycle 2). 14 of 14 with TalkBack alone, 13 of 13 with the observer bound; `TYPE_VIEW_FOCUSED` 0 and `TYPE_ANNOUNCEMENT` 0 in all 13 observer-bound showings. Order spoken: "Bottom Sheet", the message, then the focus hint.

Judgment: the criterion is met as an outcome, "spoken when it appears" read as "spoken as part of the sheet appearing, once". Honest residuals, none of which I treat as a gap:

1. It is spoken after the sheet settles (about 0.3 to 1.5 s after the window appears, after the pane title), not at node composition. The wording "when it appears" is satisfied from a listener's view, not literally at the first frame.
2. The platform event for the live region still has `text=[]` and `desc=null` (SELF-UAT block 14 and the cycle-3 findings). The ui-designer ruling of 2026-10-18 for gap cycle 2 asked that "the observer shows the focus or announce event carrying the message text" and that the live-region node "must still carry non-empty text". That secondary acceptance is not met literally; the SELF-UAT records it as an observation and judges on the spoken outcome. I list it as a designer confirmation, not a failure, because the ROADMAP criterion is about speech.
3. The device proof is one TalkBack build (Samsung 15.1.01.1 on SM-S908U). The mechanism rests on how that build treats a live-region node added after the window exists (`coincidental_reliance_items`, advisory). The unit tests prove event shape on the platform `AccessibilityNodeInfo` (exactly one content-change event on the named node per showing, none on equal-state recomposition, one on a new message) but cannot say what another TalkBack does.
4. Explicit focus by swipe with TalkBack alone is physical (Gate-2 step 5).

## POL-03 detail (SC5 sub-bullets)

| Sub-bullet | Status | Evidence |
|------------|--------|----------|
| `additionalHttpRequestProperties["User-Agent"]` equals the resolved UA after a first and second `configureOsmdroid`; pre-seeded prefs value overwritten; one entry | VERIFIED | `PlaceMapOsmdroidConfigTest` 47/47 in my fresh run. Unchanged code since the first report |
| `load()` at most once per process, header re-applied without reload (CONTEXT "load() race", plan 28-08) | VERIFIED | One `load(` call site behind the `osmdroidLoaded` guard; reset hook test proves reload only after reset. Residual (documented, review WR-02): the guard covers hub callers only |
| Blank-derived or `com.example` UA logs one `Log.w` | VERIFIED | Same test class (blank, placeholder-prefixed, both, real agent none, count unchanged on later calls) |
| Placeholder rows (6987 B plus pinned md5) purged, real tiles kept | VERIFIED | `PlaceMapTilePurgeTest` 12/12. Off-thread scan (CR-01 fix) |
| TESTER wire check: an Explorer picker with a real-id UA caches non-placeholder tiles | VERIFIED on the device (first run, SELF-UAT block 5) | HEAD harness under a `com.example.` id: `PLACEHOLDER_ROWS=0 REAL_ROWS=24`; the v2.7.1 control: `PLACEHOLDER_ROWS=12 REAL_ROWS=0` with `--expect-placeholder`; map screenshots read (real tiles versus the OSM "403 Blocked" tile). CR-01 device sanity: 56 seeded placeholder rows purged within about 0.5 s of the map composing, 8 real rows kept, no lock error, no `ConcurrentModificationException`, also on a second map creation in the same process |
| Host `curl -A` probe with the exact resolved strings | VERIFIED (recorded by the self-UAT, not re-run by me, network call outside the brief) | Explorer agent `BLOCKED=no SIZE=4473`; `com.example.probe/1.0` `BLOCKED=yes SIZE=6987 MD5=c069a15b...` |

## Required Artifacts

| Artifact | Expected | Status | Details |
|----------|----------|--------|---------|
| `component/PlaceMapOsmdroidConfig.kt` | UA injection, load-once, Log.w, purge | VERIFIED | Unchanged since first report, wired from `PlaceMapPicker.kt` |
| `explorer/ExplorerTheme.kt` and the 15 screen/entry files | pinned hub scheme | VERIFIED | Guard test scans every file |
| `explorer/ReleaseDemos.kt` | stack stage, Undo Center rows, go-to-item demos | VERIFIED | Wired from `FeedbackFamilyScreen` and `VoiceCommandFamilyScreen`; render tests green |
| `component/OutcomeSheet.kt` (gap plans 28-09, 28-11, 28-12) | refusal as one Assertive live-region node, spoken once after settle | VERIFIED | Read in full (diff `830caeb..HEAD`). No focus machinery left. `OutcomeSheetLiveRegionTest` 19/19 |
| `feedback/FeedbackCard.kt`, `FeedbackStackHost.kt` (28-10, 28-11, 28-12) | card name from data, drawn lines hidden, Undo child named once, middle strip hidden from accessibility | VERIFIED | `feedbackCardName`, `hideFromAccessibility()` on the two drawn lines, Undo node merge boundary plus innermost `clearAndSetSemantics {}`, `invisibleToUser()` on the middle strip only when `!fanned && index > 0 && index < size - 1`. `FeedbackStackHostSemanticsTest` 42/42 |
| `feedback/UndoCenterScreen.kt` | openable row as one merged button | VERIFIED | Unchanged since first report; C-2 smoke passes on the HEAD build |
| `tools/probe-osm-user-agent.sh`, `tools/verify-osm-tile-cache.py`, `tools/verify-tester-restored.sh` | probe, offline cache check, restore oracle | VERIFIED | Used by the Gate-1 run; restore oracle printed `RESTORED=yes` twice |

## Key Link Verification

| From | To | Via | Status |
|------|----|-----|--------|
| `PlaceMapPicker` factory | `configureOsmdroid(context, userAgent)` | before the `MapView` is built | WIRED |
| Every Explorer screen | `ExplorerTheme` -> `YahirAndroidTasteTheme(dynamicColor=false)` | direct calls, guard-scanned | WIRED |
| `UndoAffordanceBody` refusal | `awaitPositionAtRest` -> `spoken` -> `ReplaceWhenChanged` | `LaunchedEffect(refusedMessage)` | WIRED (device: 27 of 27 spoken once) |
| `FeedbackStackHost` | `FeedbackCard(hiddenFromAccessibility = ...)` | middle-strip flag at line 125 | WIRED |
| `FeedbackCard` root | `feedbackCardName(message, supportingText)` as the single speech source | contentDescription plus hidden drawn lines | WIRED (device: each stop spoken once, English and German) |
| `FeedbackFamilyScreen` / `VoiceCommandFamilyScreen` | stack stage, Undo Center rows, go-to-item demos, hoisted `hide()` demo | as in the first report | WIRED |

## Data-Flow Trace (Level 4)

| Artifact | Data | Source | Real data | Status |
|----------|------|--------|-----------|--------|
| Refusal node name | `refusedMessage` | `UndoAffordanceUiModel.refused` prefix, reason, changed item, suffix, all caller-supplied | Yes | FLOWING (device speech equals the caller text) |
| Card name | `item.message`, `item.supportingText` | caller `FeedbackEvent` | Yes | FLOWING (device: "Burst four, Button", "Serie vier: Eintrag geändert und gespeichert, Button") |
| Stack stage readouts | `lastResult`, `lastOpened` | real `FeedbackStackHost` callbacks | Yes | FLOWING |
| `User-Agent` request property | resolved agent | caller param | Yes | FLOWING (wire verified, first run) |

## Behavioral Spot-Checks

| Behavior | Command | Result | Status |
|----------|---------|--------|--------|
| Phase 28 and gap-plan test classes, forced fresh | `./gradlew --no-daemon testDebugUnitTest --rerun-tasks --tests` `*OutcomeSheet*`, `*FeedbackStack*`, `*A11ySweep*`, `*UndoCenter*`, `*Explorer*`, `*Phase28*`, `*RegistryRenderTest`, `*DriftGuardTest`, `*LibraryInvariantsGuardTest`, `*PlaceMap*`, `*FeedbackDeck*` | BUILD SUCCESSFUL, 35 of 35 tasks executed (none up-to-date), 71 result classes, 948 tests, 0 failures, 0 errors, 0 skipped (from the XML reports) | PASS |
| Debt markers in files changed since `c1681ff` | grep `TBD\|FIXME\|XXX` over changed `src` and `tools` | no hits | PASS |
| `api.txt` / `ComponentRegistry.kt` unchanged | `git diff --stat c1681ff HEAD -- api.txt .../ComponentRegistry.kt` | empty | PASS |
| `src`/`tools` identical to the Gate-1 tested build | `git diff ca69f92 HEAD -- src tools api.txt` | empty (only `.planning` docs and evidence after `ca69f92`) | PASS |
| No tag cut | `git tag --list 'v2.8*'` | empty | PASS |
| Signed evidence integrity (latest run) | `python3 -I`: sha256 of every file in `uatr-20261018T080238Z-a214dfe5` against `run.json` | 206 captures, 0 missing, 0 hash mismatches, no unlisted file; `run.json` `head_at_start` is `ca69f927...`, `apk_sha256` prefix `403ed64cfe02e5a9` equals the claimed harness build; 88 byte-identical duplicate groups, confirming the disclosed double ingest. I did not verify the `sig` value (no key or tool in scope) | PASS |
| C-1 tally, restore | read evidence 108, 109, 114 | 14 + 13 = 27 valid showings, all "SPOKEN ONCE"; AAR md5 `684df61c...`, APK md5 `a4f95260...`, the APK pulled back from the device has the same md5; the AAR `classes.jar` has `awaitPositionAtRest` and no `awaitSheetSettled`; `RESTORED=yes` twice | PASS (consistent) |

I did not re-run `detekt`, `apiCheck` or `tools/test/run-all.sh`. Their latest recorded results: the first-run rung 1 of the SELF-UAT (forced fresh, 179 classes, 1866 tests, 0 failures, detekt and apiCheck green, `API SOURCE-ADDITIVE OK`) and the 28-12 SUMMARY (forced fresh whole suite, detekt zero baseline, `api.txt` byte-identical, `run-all.sh` FAIL=0). Those are claims of the executors, not mine; my targeted run covers every class the gap plans touched.

## Probe Execution

Step 7c: not applicable. The phase declares no `scripts/*/tests/probe-*.sh`. `tools/probe-osm-user-agent.sh` is a network probe (run by the Gate-1 tester, not here).

## Requirements Coverage

| Requirement | Source Plans | Description | Status | Evidence |
|-------------|--------------|-------------|--------|----------|
| A11Y-02 | 28-06, 28-07, 28-09, 28-10, 28-11, 28-12 | scoped TalkBack sweep on the tester | SATISFIED on code, tests and Gate-1 speech evidence; five physical gestures are Gate-2 | SC1 and the C-1 section |
| DEMO-02 | 28-03, 28-05, 28-07 | whole Explorer in the pinned hub scheme; hoisted `sheetState` demo calls `hide()` | SATISFIED | SC2 |
| DEMO-03 | 28-04, 28-05, 28-07 | stacked-snackbar and go-to-item demos in English and German; registry | SATISFIED | SC3 and SC4 |
| POL-03 | 28-01, 28-02, 28-07, 28-08 | UA reaches OSM, blocked agent never silent | SATISFIED, wire verified on the device | SC5 |

All four IDs appear in `REQUIREMENTS.md` (A11Y-02 line 33, DEMO-02 line 37, DEMO-03 line 38, POL-03 line 41; traceability rows 87 to 90 list all four as `Phase 28 | Pending`) and in plan frontmatter (28-09 to 28-11 declare `requirements-completed: [A11Y-02]`). No orphaned requirement is mapped to Phase 28. The REQUIREMENTS.md checkboxes and traceability rows still read unchecked/Pending; flipping them is the orchestrator's close-out step, not a gap.

## Judgment-tier prohibitions (non-authoritative judge verdict)

Recorded as `unverified-prohibition: human review recommended`, not blocking. The first-report rows still hold except where a gap plan deliberately changed the scope.

| Prohibition (plan) | Verdict | Backing |
|--------------------|---------|---------|
| No public signature or `api.txt` change, no new public composable (28-01 to 28-12) | Held | `api.txt` and `ComponentRegistry.kt` empty diff since `c1681ff`; hygiene and registry tests green |
| `FeedbackCard`, `FeedbackStackHost`, `OutcomeSheet.kt` locked; no `mergeDescendants` on the live-region surface (28-04, 28-06) | Superseded by designer-authorized gap plans 28-09 to 28-12 | Rulings of 2026-10-17 and 2026-10-18 in 28-CONTEXT; PD-4 of 28-06; the contract test edit is commented as deliberate |
| D-11 Undo child plus custom action, D-07 both Dismiss forms preserved (designer) | Held | Device actionList `[16:Open item\|1048576:Dismiss\|2131230721:Undo\|2131230722:Dismiss]` on the HEAD build, English and German |
| No second live-region mechanism, never read twice (2026-10-18 ruling) | Held on the device | One live-region node, 0 `TYPE_ANNOUNCEMENT`, 0 `TYPE_VIEW_FOCUSED`, one utterance per showing |
| Gate-1 must not waive or park automatable checks; no `wm density reset`; no wireless phone (28-07) | Held per the self-UAT | Restore oracle `RESTORED=yes` twice; density stayed 450 in the last run; `command_guard_breaches: 0` in the latest run. Earlier runs recorded one guard hit each (script-text false positives, nothing forbidden ran) |

## Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
|------|------|---------|----------|--------|
| `component/OutcomeSheet.kt` | `awaitPositionAtRest`, 300 ms / 6 frames / 1.5 s | Window-attached state inferred from a position heuristic | Warning | Advisory (see `coincidental_reliance_items`). Not a stub; behavior device-verified on one TalkBack build |
| `feedback/FeedbackCard.kt` | Undo node `clearAndSetSemantics { }` | Drops the button's `Focused`/`RequestFocus` semantics | Info | 28-12 deviation 1; the Gate-1 run confirmed the Undo child is still reached and activated by TalkBack double tap and by keyboard (TAB then ENTER) |
| `explorer/ReleaseDemos.kt` | German demos | Library chrome words stay English in German demos (`Undo`, `Preview`, `Undone`, `Clear history`, `Show sheet`, `Opened: none`) | Info | Known, recorded FA-3/FA-4, deferred to v2.8.1. Caller-supplied German strings render |
| `component/PlaceMapOsmdroidConfig.kt` | `configureOsmdroid` KDoc | Load-once guard covers hub callers only | Warning | Review WR-02, documented precondition, not closable inside the hub |
| `feedback/FeedbackStackHost.kt` | collapsed deck | TalkBack has no fan action (back strips expose Open/Undo/Dismiss only; sighted tap fans) | Info | Designer ruling of 2026-10-18: every card stays a full node, so no fan action is needed. Recorded in the SELF-UAT notes |
| POL-03 | no response-side `x-blocked` detection | only the config-time `Log.w` | Info | CONTEXT scoped it "only if cheap"; the purge is the recovery path |

No stubs, placeholder returns or empty handlers in the phase's production code. No unreferenced debt markers.

## SELF-UAT claims I checked and a correction

- Evidence files cited for C-1, the deck and the restore exist and match their signed hashes (above). Build identity is consistent: `run.json` HEAD `ca69f927`, harness APK sha256 prefix `403ed64cfe02e5a9`, APK md5 `a4f9526090024238b0124bb6be0f70fc` pulled back from the device equals the installed build; HEAD `83aafb7` differs from `ca69f92` by docs and evidence only.
- The addendum says SC2 to SC5 were not re-run because gap plan 28-12 changed "only `FeedbackCard.kt` and `OutcomeSheet.kt`, which those criteria do not touch". That reasoning is imprecise: SC3's stack stage runs on `FeedbackCard`/`FeedbackStackHost` and its go-to-item sheets on `OutcomeSheet`. The change is semantics-only (no size, padding or layout edit in the diff), the cycle-3 run did exercise the stack stage, the fanned and collapsed deck, tap-to-open and the Undo Center rows on the HEAD build, and my fresh JVM run covers SC3's render tests at HEAD. What was not repeated on the device after the gap plans is the German 360 dp / font scale 2.0 layout check of the stack and the sheets. Phase 29 already carries the long-string and Gate-1 device criteria, so I treat this as covered there rather than as a gap.
- The 28-12 SUMMARY says `OutcomeSheetClassicParitySourceContractTest` "was not edited". True for 28-12; 28-09 edited it (the deliberate PD-4 edit shown in the diff).

## Gate-2 hand-offs

Registered in `/home/yahir/Projects/Reusable/android/yahirandroidtaste/.planning/uat-pending/28-talkback-sweep-explorer-demos.md` (status pending). They do not make the phase gaps_found. Owner steps at milestone completion:

1. ReceiptRow trailing-action: one-finger swipe right from the summary reaches Undo, left returns.
2. UndoCenterScreen group rows: headings navigation reaches "Batch edit" and "Earlier batch".
3. UndoCenterScreen go-to-item rows: swipe through "Changed item A", row then its Undo, nothing twice.
4. FeedbackStackHost collapsed deck, English and German: oldest strip, its Undo, front card, each named once.
5. Refused-undo message focused by one-finger swipe with TalkBack alone: spoken once, no role.
6. Hear the C-1 message once per showing on a real device (owner how-to-verify item 2) and open the Explorer on a Material You device (amber warn kept in light and dark, real OSM tiles with Wi-Fi on).
7. C-3 and C-4 were ruled "keep" by the ui-designer (2026-10-17); the Gate-2 note is the cosmetic duplicate Dismiss in the actions menu.
8. Housekeeping: three throwaway harness packages were left installed on the tester per the dispatch ruling (`com.example.yat28head`, `com.example.yat28control`, `com.example.yat28obs`) for Yahir to remove. The v2.6 `uat_gaps` rows 21 to 24 remain the owner's call.

Orchestrator follow-ups outside this verification: re-audit `28-SECURITY.md` (`audited_head ea169f0` predates the gap plans); flip A11Y-02, DEMO-02, DEMO-03 and POL-03 in REQUIREMENTS.md and mark Phase 28 complete in ROADMAP.md at close-out; stamp `covered_files` / `covered_digest`.

## Gaps Summary

No gaps. All five ROADMAP criteria are met on code, on a fresh forced test run and on signed device evidence from the TESTER. The earlier Gate-1 failures (C-1 not spoken, then spoken twice; C-5 unnamed deck stops; C-6 doubled card names) were closed by plans 28-09 to 28-12 and re-measured on the real device against the HEAD build. The status is `human_needed` rather than `passed` only because five physical one-finger TalkBack gestures cannot be synthesized and are legitimately Gate-2, and because the designer should confirm that the spoken outcome satisfies the C-1 ruling although the live-region event text is still empty.

---

_Verified: 2026-10-18T09:43:06Z_
_Verifier: Claude (gsd-verifier)_
