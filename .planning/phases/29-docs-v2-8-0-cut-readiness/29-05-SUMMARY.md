---
phase: 29-docs-v2-8-0-cut-readiness
plan: 05
subsystem: testing
tags: [gate-1, self-uat, tester-device, long-string, spanish, german, font-scale-2, release-gate]
status: blocked
requires:
  - phase: 29-04
    provides: "tools/verify-junit-fresh.sh and tools/verify-publish-range.sh, the forced-fresh battery as plain commands"
provides:
  - "a verified signed uat-run (75 captures) of 14 Spanish and German scenes on the TESTER at 360 dp, font scale 1.0 and 2.0"
  - "29-05-SELF-UAT.md with result has_fail: SC1, SC2 and the automatable half of SC4 pass, SC3 fails on one scene"
  - "the fresh rung-1 battery on this HEAD (1911 tests, 0 failures, 0 errors)"
affects: [29-06]
requirements-completed: []
actuals:
  tokens: 0
  tasks: 3
  commits: 1
---

# Phase 29 Plan 05: Gate-1 on the TESTER, long-string probe Summary

Gate-1 for Phase 29 ran on the real TESTER and found one genuine defect, so the plan ends BLOCKED, not complete: the Spanish snackbar card at 360 dp and font scale 2.0 clips text. Everything else passes. Plan 29-06 must not start until gap closure re-verifies SC3. SHIP-05 stays open (plan 29-06 closes it, and I did not mark it).

## Verdict

`uat-run verify` = verified (one warning: the logcat capture is empty, signed negative evidence). `29-05-SELF-UAT.md`: `status: failed`, `result: has_fail`, verified by `uat-run verify --self-uat`.

| Criterion | Rung | Result |
|-----------|------|--------|
| SC1 docs describe the v2.8 API, counts match the registry | 1 | PASS |
| SC2 additions only vs v2.7.1; fresh tests, detekt, run-all | 1 | PASS |
| SC3 long strings DE and ES at 360 dp and font 2.0; Gate-1 device checks | 5 | **FAIL** (es-snackbar at font 2.0) |
| SC4 no tag, no push, no repin (automatable half) | 3 | PASS (SHIP-05 is NOT satisfied by Gate-1; it is closed only by plan 29-06 writing the v2.8.0 proposal and recording the forced-fresh battery on the gated commit. An ordering fact; nothing was skipped.) |

## Per-scene result table (360 dp = `wm density 480`; screens read at 50 percent; evidence numbers are run-directory file prefixes, screen then ui = +1)

| Scene | Font scale | Verdict | Evidence | One line |
|-------|-----------|---------|----------|----------|
| es-snackbar | 1.0 | PASS | 01, 02 | message, second line, 3-line Undo, chevron, two peek strips |
| es-snackbar | 2.0 | **FAIL** | 29, 30 | Undo label (4 lines) cut by its rounded ends ("D" and "l" lose their left edge); card as tall as the window and still short, second line cut at "vez ayer"; narrow body column with mid-word breaks; no scroll |
| de-snackbar | 1.0 / 2.0 | PASS / PASS | 03, 04 / 31, 32 | fits; "Rückgängig" on one line; chevron inside; at 2.0 the narrow column breaks mid-word (known observation) |
| es-sheet-single-receipt | 1.0 / 2.0 | PASS / PASS | 05 / 33 | title, detail and chevron inside, actions visible |
| es-sheet-single-classic | 1.0 / 2.0 | PASS / PASS | 07 / 35 | no actions by documented design |
| de-sheet-single-receipt | 1.0 / 2.0 | PASS / PASS | 09 / 37 | "Bearbeiten" action pill wraps mid-word at 2.0 (new small observation), nothing clipped |
| de-sheet-single-classic | 1.0 / 2.0 | PASS / PASS | 11 / 39 | as Spanish |
| es-sheet-batch-receipt | 1.0 / 2.0 | PASS / PASS | 13 / 41, 57, 73 | chevron on targeted rows only; actions reachable after one swipe |
| es-sheet-batch-classic | 1.0 / 2.0 | PASS / PASS | 15 / 43, 59 | no actions by design; end reachable |
| de-sheet-batch-receipt | 1.0 / 2.0 | PASS / PASS | 17 / 45, 61, 69 | actions off screen before scroll, fully above the nav bar after one swipe |
| de-sheet-batch-classic | 1.0 / 2.0 | PASS / PASS | 19 / 47, 63, 71 | nothing clipped, end reachable |
| es-undo | 1.0 / 2.0 | PASS / PASS | 21 / 49, 65 | Undo and chevron inside; chevron on the targeted row only |
| de-undo | 1.0 / 2.0 | PASS / PASS | 23 / 51, 67 | as Spanish; "Wocheneinkau/f" breaks mid-word at 2.0 |
| es-pill | 1.0 / 2.0 | PASS / PASS | 25 / 53 | 5 wrapped lines, dot visible, no glyph clipped |
| de-pill | 1.0 / 2.0 | PASS / PASS | 27 / 55 | 4 wrapped lines, dot visible, nothing clipped |

Scrolled captures: 57 to 68 (first pass, slower swipe, partial scroll on the German batch sheets) and 69 to 74 (second pass, one full swipe). Logcat (`AndroidRuntime:E`): 75, empty.

## Root cause of the SC3 failure (diagnose only, nothing fixed)

1. The feedback card's Undo is `DynamicActionButton` with `ActionButtonEmphasis.Text` (`feedback/FeedbackCard.kt` about lines 368 to 395), a Material `TextButton` with its default fully rounded shape (`component/DynamicActionButton.kt`, Text branch near line 205, no `shape` passed). A 4-line label makes the button about 475 px tall (`[445,998][948,1473]` in the dump), so the half-height corner radius clips the first and last lines at the sides. Same class of defect plan 29-01 fixed for the handled-by pill.
2. The card is taller than the window at 360 dp and font 2.0 with the Spanish message; `FeedbackDeck.measureDeck` passes `maxHeight = constraints.maxHeight` and the collapsed card does not scroll, so the end of the second line is unreachable.
3. Robolectric missed both: `spanishLongUndoLabel_isCapped_andBodyKeepsTheMajority` runs at font 1.0 and `undoAndChevronStayInsideCard_spanish` (font 2.0) asserts horizontal bounds only.

Gap-closure routing: `/gsd-plan-phase 29 --gaps` then `/gsd-execute-phase 29 --gaps-only`; re-run this device pass afterwards. SC3 convergence count: 1.

## Build identity

HEAD `8607c12` (no `src/main` commit since `55f5288`; `git diff 8607c12..HEAD -- src api.txt build.gradle.kts tools` empty). Library AAR (mavenLocal 1.10.0, deleted then republished) md5 `684df61c57910824077f7b926f5f0000`; `classes.jar` lists the five v2.8 classes. Harness APK `com.example.yat29ls` md5 `fd18a7bcee2779fd1430370a312c4879`; the md5 pulled back from the device is identical. Run `uatr-20261018T112553Z-e47a3028` (`apk` sha256 prefix `99dbf38a517a`).

## Rung-1 totals (fresh)

`apiDump` then `git diff --exit-code -- api.txt` clean; `testDebugUnitTest detekt apiCheck --rerun-tasks --no-build-cache --no-daemon`: BUILD SUCCESSFUL in 2m 50s, 43 of 43 tasks executed, none of the three tasks UP-TO-DATE or FROM-CACHE. `verify-junit-fresh.sh`: 179 files, 0 stale, **1911 tests, 22 skipped, 0 failures, 0 errors**, ten required classes present, `JUNIT-FRESH OK`. `run-all.sh`: exit 0, 277 assertions, 0 FAIL. `verify-api-source-additive.sh v2.7.1`: `SEAMS: none`, `API SOURCE-ADDITIVE OK ... added_classes=5 added_members=48 signature_changes=0 accepted_exceptions=0`; `git diff --numstat v2.7.1 -- api.txt` = 78 added, 11 removed lines. `verify-binary-abi.sh v2.7.1` (informational): exit 3, 37 lane-3 lines (`BatchRowResultUiModel` and `VoiceOutcomeUiState.Success` constructors, `copy`, `copy$default`); never a gate. INV-03 greps and the configuration diff against v2.7.1 clean. Doc counts 67 registered, 6 unregistered, 73 in all (`API.md` line 29, checked by the guard tests).

## Device residue

State changes and what I did about each:

- `wm density` 450 to 480 to 450 (never `reset`); `font_scale` 1.0 to 2.0 to 1.0. `bash tools/verify-tester-restored.sh --density 450 --font-scale 1.0 --services null` printed `RESTORED=yes`, exit 0, twice. TalkBack and the observer service were never enabled.
- `svc power stayon true` and `KEYCODE_WAKEUP` (playbook step). Global `stay_on_while_plugged_in` reads 15 now; its earlier value was not read before the call, so it is not restored.
- Installed ONE new throwaway package.

Left installed on the tester for Yahir to remove (nothing uninstalled, no data cleared): `com.example.yat29ls` (new), `com.example.yat28head`, `com.example.yat28control`, `com.example.yat28obs`.

Leases: `build` and `device:tester` started by this session and released with `yrig lease done` (both read `free`; `repo:yahirandroidtaste` is held by ui-designer and was not touched). No tag, no push, no repin, no rig message.

## Owner flags and the Gate-2 carry-forward list

The Gate-2 fragment (`.planning/uat-pending/29-docs-v2-8-0-cut-readiness.md`) and the `HUMAN-UAT-PENDING.md` entry were NOT written: the workflow registers Gate-2 only for a passing Gate-1, and SC3 failed. The gap-closure re-run registers them. The list they must carry, from the Runtime Decision plus this run, so it is not lost:

- the physical TalkBack steps parked by Phases 25 to 28 (the spoken cross-card order, the Dismiss duplicate in the TalkBack actions menu, the refusal read-out by swipe with TalkBack alone), with pointers to the Phase 25 to 28 fragments;
- the German font-2.0 narrow body column on the stacked card (re-measured here: about 102 dp text column, mid-word breaks "Wochen/ende", "Einträge/n");
- the German batch Receipt sheet's actions at font 2.0 (re-measured here: off screen before scrolling in a four-row fixture, fully above the nav bar after one swipe);
- new small observation: the German "Bearbeiten" action pill wraps mid-word at font 2.0;
- the four throwaway harness apps left on the tester (`com.example.yat28head`, `com.example.yat28control`, `com.example.yat28obs`, `com.example.yat29ls`), which only Yahir removes.

Other flags: the tester's Samsung edge-panel overlay (a grey pencil button at the right edge) appears in every screenshot and covers part of a few chevrons (the `ui` dumps confirm the real nodes); the M3 drag-handle tooltip overlaps the heading on full-height sheets at font 2.0; command guard 0 breaches, 7 refusals (all benign "uninspectable" cases, re-run in readable form).

## Next

Plan 29-06 may proceed only if `uat-run verify` reported verified AND no criterion failed. The run verified, but SC3 failed, so 29-06 must NOT proceed: run gap closure for SC3, re-run this Gate-1, then 29-06.
