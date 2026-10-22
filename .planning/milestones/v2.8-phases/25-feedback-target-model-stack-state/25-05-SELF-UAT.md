---
status: complete
result: all_pass
gate: 1
evidence_schema: 3
uat_run: uatr-20261017T093534Z-c9306f1b
phase: 25-feedback-target-model-stack-state
source:
  - "25-ROADMAP success criteria"
device: SM-S908U (R5CT10XNKQN)
apk: io.github.ygaray.yahirandroidtasteharness (sha256 34febb200ad6)
run: 2026-10-17T09:35:34.812Z
command_guard_breaches: 0
command_guard_refusals: 3
---

<!-- Gate-1 self-UAT, phase 25 (consolidated per phase; evidence under the last plan, 25-05). -->

# Self-UAT Log — Phase 25 (Feedback target model & stack state), run against plan 25-05

**Device:** SM-S908U tester, adb serial `R5CT10XNKQN` (Android 15, API 35, density 450, never changed)   **Run:** 2026-10-17T09:35:34Z (`uat_run` uatr-20261017T093534Z-c9306f1b, 15 signed captures, `uat-run verify` = verified)
**Build identity:** library HEAD `7c19bab`; library AAR (mavenLocal `com.github.Ygaray:yahirandroidtaste:1.10.0`, deleted then republished from this tree with `clean publishReleasePublicationToMavenLocal`) md5 `cd6bf08da91cc12e15b9a7c679269211`, contains the 12 `FeedbackStack*` classes; throwaway harness APK (scratchpad, never committed) md5 `329b9f0c0d16ef3317e64a240752ef71` (sha256 prefix `34febb200ad6`).
**Pre-flight:** device awake, no keyguard, `get-state` = device. Lease `device:tester` and `build` held via `yrig lease` and released at the end. Wireless devices were never touched.
**Unit suite (rung 1):** `./gradlew testDebugUnitTest detekt apiCheck --rerun --no-build-cache --no-daemon` → BUILD SUCCESSFUL, **1452 tests, 0 failures, 0 errors, 22 skipped (none in phase-25 classes)**; detekt and `metalavaCheckCompatibilityRelease` executed (not UP-TO-DATE). Summary ingested as `13-unit-detekt-apicheck-summary.txt`. `bash tools/verify-api-source-additive.sh v2.7.1` → `SEAMS: none`, `API SOURCE-ADDITIVE OK … removed_classes=0 removed_members=0 required_param_changes=0 signature_changes=0 added_classes=5 added_members=36` (`15-api-source-additive-v271.txt`).
**Coverage/Nyquist (rung 2):** every criterion maps to named, green tests (listed per criterion). `25-VALIDATION.md` is still the plan-time draft; I did not run `gsd-validate-phase`.
**Seed/fixture integrity:** the library has no data layer. Fixtures are compiled-in Explorer demos plus a throwaway harness `ConsumerActivity` that drives the library's PUBLIC API like a consumer would (it compiled against the fresh AAR, which is itself a source-compat signal). Seeding is "publish the right HEAD": done, md5 above.
**Destructive check:** not applicable. The repo has no `src/androidTest`, so there is no `connectedAndroidTest` / target-wiping suite (no `destructive_check`, no `final_state_check`). No provider key or cloud call exists in this phase (shipped-config rule: nothing substituted).

## Criteria

### 1. SC1 — `FeedbackTarget(key, openLabel)` on `Notify`/`WithUndo`; value equality, no lambda; v2.7.1 call shapes unchanged (FTGT-01)
result: passed
- **Rung:** 1 (unit) plus the consumer compile on a real harness and the additive-API tool; no visual claim.
- **Target:** headless + the device consumer build.
- **Expected:** a caller can pass `target = FeedbackTarget(key, openLabel)` to both event types; `FeedbackTarget` compares by value and has no lambda field; every v2.7.1 positional and named call shape compiles unchanged.
- **Arranged (seeded):** none.
- **Did (drove):** ran the fresh unit suite (above); compiled the harness `ConsumerActivity`, which builds `FeedbackEvent.Notify(...)` and `FeedbackEvent.WithUndo(...)` with `target =` and `supportingText =` against the freshly published AAR; ran `verify-api-source-additive.sh v2.7.1`.
- **Observed:** `FeedbackTargetTest` 11/0/0 (value equality, two String fields only), `V271CallSiteCanaryTest` 8/0/0 (positional, named, out-of-order named, `copy`, destructuring, exhaustive `when`, function-value shape), the additive tool reports 0 removed members, 0 required-param changes, 0 signature changes. On the device a `Notify` and a `WithUndo` with a target both rendered and routed (see SC5). Falsification attempt: looked for a lambda or non-String field on `FeedbackTarget` (`FeedbackTarget.kt`: only `key` and `openLabel`; `toString` omits the key, so the key never lands in logs).
- **Evidence:** `13-unit-detekt-apicheck-summary.txt`, `15-api-source-additive-v271.txt`, `04-consumer-tracked-card-up-entry-in-store-german-undo.xml`.

### 2. SC2 — a targeted `WithUndo` tracked through either `SnackbarHostState.emitTrackedWithUndo` or `FeedbackStackState.emitTrackedWithUndo` lands in `UndoHistoryStore` with the same target; once Undone it exposes no openable target (FTGT-02)
result: passed
- **Rung:** 3 (data state read out of the live store on the tester) plus rung 1.
- **Target:** device (tester) through the harness `ConsumerActivity`, which prints the live `UndoHistoryStore.entries` (message / status / `target.key` / `openableTarget.key`).
- **Expected:** target present and openable while Available, on both tracked paths; after Undo, the entry keeps its target data but `openableTarget` is null.
- **Arranged (seeded):** none beyond a fresh `UndoHistoryStore()` per activity launch.
- **Did (drove):** tapped StackTracked (stack path) and M3Tracked (Material3 `SnackbarHostState` path); then tapped the card Undo / the snackbar Undo through the real UI.
- **Observed:**
  - Stack path: `ENTRIES=Gelöscht/Available/tgt=k-stack/openable=k-stack`, then after the card Undo `Gelöscht/Undone/tgt=k-stack/openable=null`.
  - Material3 path: `M3 gelöscht/Available/tgt=k-m3/openable=k-m3`, then after the snackbar Undo `M3 gelöscht/Undone/tgt=k-m3/openable=null`, `UNDORUNS` 0 to 1.
  - Unit: `UndoHistoryStoreTest` 33/0/0 (`openableTarget_isNullAfterUndone_butTargetIsKept`, `…AfterFailed…`), `UndoTrackingMaterialPathTest` 6/0/0.
- **Evidence:** `04-…german-undo.xml`, `05-consumer-card-undo-once-entry-undone-not-openable.xml`, `09-consumer-m3-tracked-target-entry.xml`, `11-consumer-m3-undo-entry-undone-not-openable.xml`, `06-consumer-p25-log-card-undo.log`.

### 3. SC3 — `rememberFeedbackStackState(maxVisible = 3)` survives recomposition; each `show(event)` resolves to exactly one of `Opened` / `Undone` / `Dismissed` / `TimedOut` (FSTK-01)
result: passed
- **Rung:** 1 (virtual-time unit tests, the criterion's own stated mechanism) plus device observation of all four results.
- **Target:** headless (decisive for exactly-one and no-composition) and device.
- **Expected:** one result per `show`, never two and never none; the state needs no composition; the cap is 3.
- **Arranged (seeded):** none.
- **Did (drove):** on the device, `show()` calls from the harness (suspending caller prints the result): TargetNote body tap, StackTracked card Undo, DismissAll, and a Short note left to expire; in the Explorer demo, 4 pushes in a row (3 targeted notes then a plain one) on a cap of 3.
- **Observed:** results seen once each: `note:Opened` (with `OPENED=k-note`, one `open k-note` log line), `stack:Undone`, `stack:Dismissed`, `short:TimedOut` after about 8 s from tap to check (Short is 4 s; logged 03:37:23). Each caller resumed exactly once (one log line per `show`). In the Explorer, after 4 pushes only 3 cards are in the tree (oldest evicted). Unit: `FeedbackStackStateTest` 24/0/0 (`parallelResolvers_produceExactlyOneWinner`, `tapThenTimer`, `timerThenTap`, `evictionThenUndo`, `dismissAllThenTap`, cancel cases), `FeedbackStackStateTimeoutTest` 21/0/0, `RememberFeedbackStackStateTest` 6/0/0 (`sameInstanceAcrossRecomposition`). All under `runTest`, no composition.
- **Evidence:** `10-consumer-p25-log-open-timeout.log`, `08-consumer-p25-log-race.log`, `06-…card-undo.log`, `01-explorer-stack-three-cards-newest-first.xml`, `13-unit-detekt-apicheck-summary.txt`.

### 4. SC4 — `FeedbackStackState.emitTrackedWithUndo(store, event)` appends before the card is visible; `Undone` runs through `store.attemptUndo` exactly once; a racing Undo Center tap resolves through the same guard; `Opened` / `Dismissed` / `TimedOut` never undo (FSTK-07)
result: passed
- **Rung:** 3 (live store state and the undo-lambda counter read on the tester) plus rung 1 (race tests).
- **Target:** device (tester) and headless.
- **Expected:** the entry is in the store the whole time the card is up; one undo no matter who wins; non-Undo results leave the entry Available and the lambda unrun.
- **Arranged (seeded):** fresh store per launch.
- **Did (drove):** (a) StackTracked, read the tree with the card up, then tapped the card's Undo. (b) Fresh launch, StackTracked, then tapped CenterUndoFirst (`store.attemptUndo` on the entry, the Undo Center's call) while the card was up, then tapped where the card's Undo was, then DismissAll. (c) TargetNote opened (no store) and the Short note timed out.
- **Observed:** (a) with the card up the entry was already `Available` with `openable=k-stack`; after the Undo tap `RESULT=stack:Undone UNDORUNS=1`, log has exactly one `undoAction ran`. (b) Center first: `UNDORUNS=1`, entry `Undone`, card stays on screen but its Undo button is gone (spent), tapping the old Undo spot did nothing (`UNDORUNS` stays 1), DismissAll then resolves `stack:Dismissed` with `UNDORUNS` still 1 and the entry still `Undone` (the one undo came from the Center, Dismissed never undoes). One `undoAction ran` line in the log. (c) Opened and TimedOut produced no undo-lambda run (`UNDORUNS=0` in that session). Unit: `FeedbackStackTrackedUndoTest` 18/0/0 (`entryExistsAsSoonAsCardIsVisible_…`, `undoneResult_runsStoreUndoOnce_…`, `raceUndoCenterFirst_thenCardUndo_lambdaRunsExactlyOnce`, `raceCardUndoFirst_thenUndoCenter_…`, `openedResult_/dismissedResult_/timedOutResult_neverUndoes_…`, cancellation cases).
- **Evidence:** `04-…xml` (entry present, card up), `05-…xml`, `06-…log`, `07-consumer-race-center-first-card-spent.xml`, `08-consumer-p25-log-race.log`.

### 5. SC5 — `FeedbackStackHost(state, onOpenTarget, modifier, undoLabel = "Undo", dismissLabel = "Dismiss")`: body tap opens, Undo undoes, German label renders as passed; registered; registry and CATALOG guards green (FSTK-01, FSTK-07)
result: passed
- **Rung:** 5 for the German label (a visual claim, screenshot read), 4 for tap routing, 1 for the guards.
- **Target:** device (tester), both the Explorer Feedback family screen (the registered `FeedbackStackHost` entry) and the harness consumer.
- **Expected:** signature as stated; a body tap on a targeted card calls `onOpenTarget(target)` once and resolves `Opened`; a card with no target does nothing; Undo resolves `Undone`; the Undo text is the caller's label; the host is registered; the guards are green.
- **Arranged (seeded):** none (cards pushed through the demo buttons).
- **Did (drove):** Explorer, Feedback > FeedbackStackHost: pushed "Note with target", "Note with Undo and target", "Plain note"; tapped the Plain note body, the Undo, and the "Item saved" body; also the Default state cell renders a targeted card. Harness: `FeedbackStackHost(..., undoLabel = "Rückgängig machen", dismissLabel = "Verwerfen")` with a tracked German-labelled card; body tap on a targeted note.
- **Observed:** cards listed newest-first (Plain note, Item deleted, Item saved). Only the targeted cards have a clickable body node plus a chevron; the plain card has none (screenshot `02-…png` read: dot + text, no chevron, no action label). Plain note body tap: `Last result: none` and the card stayed. Undo tap: `Last result: Undone, last opened: none`. Body tap on "Item saved": `Last result: Opened, last opened: demo-item`, card removed. In the harness, the Undo text on the screenshot reads "Rückgängig machen" (`12-…png` read), and the body tap gave `OPENED=k-note` with one callback. The host compiles from the public API with the stated signature (harness build). Registry: `ComponentRegistryDriftGuardTest` 1/0/0, `CatalogDocsDriftGuardTest` 10/0/0, `DomainVocabularyDriftGuardTest` 2/0/0, `FeedbackStackHostTest` 12/0/0 (`bodyTap_onTargetedCard_opensTargetOnceAndResolvesOpened`, `doubleTap_navigatesOnce_…`, `undoTap_resolvesUndone_…`, `germanLabels_renderAsPassed_…`); the Explorer's Feedback family list shows the `FeedbackStackHost` entry (Pattern tier) and `rememberFeedbackStackState` is allowlisted in `ComponentRegistry.kt`.
- **Evidence:** `01-explorer-stack-three-cards-newest-first.xml`, `02-explorer-stack-three-cards-screen.png`, `03-explorer-after-plain-tap-undo-open.xml`, `12-consumer-german-undo-label-screen.png`, `04-…xml`.

## Summary

total: 5
passed: 5
partial: 0
failed: 0
infra: 0

## Notes / anomalies (for the Gate-2 reviewer)

- **Arrange vs Act boundary.** Nothing was seeded into a pass; the Act on every criterion was real taps on the tester. The harness `ConsumerActivity` only adds read-outs (store state, a result string, an undo-lambda counter) around the public API; it contains no logic that could produce a result by itself.
- **A first signed run was abandoned**, `uatr-20261017T093104Z-8b96ec81`: I reinstalled the harness APK (added `ConsumerActivity`) mid-run and `uat-run capture` correctly refused (APK changed since start). I abandoned it with a reason, removed its un-committed run dir, and re-ran everything on the final APK in the run named above. No result in this log comes from the abandoned run.
- **Command guard.** Three Bash calls were refused fail-closed before running: `./gradlew` in a harness dir that did not exist yet (uninspectable), a `~/.claude/...` path to `gsd_run` (tilde not resolved), and a `for` loop with a variable in an `adb shell input` command. All are `uninspectable` refusals, not breaches (none was forbidden, none ran); each was re-run in a literal form. `command_guard_refusals: 3`, `command_guard_breaches: 0`.
- **Scope honesty.** This phase delivers a tracer host: no deck motion, swipe, pause or semantics polish (FSTK-02..06 and A11Y-01 are Phase 26; TalkBack is Phase 28). Not tested here by design. The "Undo as a TalkBack action" added in review (WR-04) is covered by unit tests only; TalkBack on the tester belongs to the Phase 28 sweep.
- **Explorer demo timing.** Explorer demo cards use `SnackbarDuration.Indefinite`, so the timeout path was exercised only through the harness (`TimedOut` after the Short 4 s).
- **Device restore.** Harness APK uninstalled; display density untouched (450, `wm density` never invoked with a write); device left on the home screen. No residual difference.
- Raw unit-test detail for the 8 new phase test files sits in the one ingested summary; the full XML reports live under the (gitignored) `build/` dir.

## Findings routed to gap-closure (if any)

None.

## Verdict

All 5 criteria PASS on the real tester against HEAD `7c19bab` (library AAR md5 `cd6bf08d…`). Gate-1 complete; human Gate-2 deferred to milestone completion (registered as `.planning/uat-pending/25-feedback-target-model-stack-state.md`).
