---
phase: 24-catalog-docs-explorer-demos-cut-v2-6-0
plan: 03
subsystem: testing
tags: [gate-1, self-uat, tester, explorer, screencaps, uat-run, demo-01]
requires:
  - phase: 24-catalog-docs-explorer-demos-cut-v2-6-0
    provides: plan 24-01 demos (ReleaseDemos.kt, registry render tests) and plan 24-02 canary, docs and drift guards
provides:
  - 24-03-SELF-UAT.md (Gate-1, 5 of 5 criteria passed, signed run uatr-20261012T230611Z-4dbe72cf, evidence_schema 3)
  - 33 TESTER screencaps for the owner under screencaps/ (DEMO-01, D-08)
  - the Phase 24 Gate-2 pending fragment (owner review after the cut; an objection ships as v2.6.1)
affects: [24-04-cut]
requirements-completed: [DEMO-01, API-03, INV-03]
status: complete
uat_run: uatr-20261012T230611Z-4dbe72cf
verdict: verified
plan_head: 13039d1203abb14e0da287b4d9c5613d4bd7931f
---

# Phase 24 Plan 03: Gate-1 self-UAT on the tester and the DEMO-01 screencaps Summary

**Every DEMO-01 demo was driven on the real tester (`R5CT10XNKQN`) through the Explorer in one signed `uat-run` (90 captures, `finish` and `verify` = verified); all 5 ROADMAP criteria pass at Gate-1, 33 screencaps are kept for the owner, no source changed, no tag, no push. SHIP-04 is NOT satisfied by Gate-1; it is closed only by the 24-04 SUMMARY recording JitPack ok for v2.6.0.**

## Result

- Run `uatr-20261012T230611Z-4dbe72cf`, verified, `uat-run verify --self-uat` on `24-03-SELF-UAT.md` verified. Plan 24-04 may proceed (its dependency on a verified Gate-1 is met).
- No criterion was skipped or deferred. `connectedAndroidTest` is not applicable (`git ls-files src/androidTest` prints nothing). The only items parked for the owner are physical-only (TalkBack read-out, visual taste), recorded in the Gate-2 fragment, not as PARTIAL.
- No gap-closure is needed. Advisory findings (owner or v2.6.1 calls): ADV-1 tall sheets open at the partial detent and, on the tester's 3-button navigation bar, the pinned Cancel/Confirm sit under the nav icons until dragged up (screencap 29; the known Phase 22 ADV-2 behaviour, `SheetScaffold` default since v2.4.1); ADV-2 the count-editor demo has no horizontal padding (`ReleaseDemos.kt:298`).

## Build identity

- HEAD `13039d1`; `git diff --exit-code HEAD -- api.txt src` exit 0 before and after.
- AAR `com.github.Ygaray:yahirandroidtaste:1.10.0` republished from this HEAD, **md5 `a695b8258e9e45c39ba9dad4b3cf359a`** (re-read after the run); `classes.jar` has `ReleaseDemosKt`, `ProviderKeyDefaults`, `UndoGroupRowUiModel`, `ToneCardKt`, `OutcomeSheetDefaults`.
- Throwaway harness (scratchpad, never committed) APK md5 `a853f475e555ca1b282840afb52786be`, read back on the device; `uat-run` records its sha256 prefix `7b095cd78ec7`.

## Rung-1 totals (fresh, uncached, re-run by the tester, matching the Task 1 numbers)

`./gradlew apiDump cleanTestDebugUnitTest testDebugUnitTest detekt apiCheck --rerun-tasks --offline --console=plain`: exit 0, 135 XML all newer than the marker, **tests 1315, skipped 22, failures 0, errors 0**. V251CallSiteCanaryTest 12/0/0, CatalogDocsDriftGuardTest 9/0/0, LibraryInvariantsGuardTest 4/0/0, ProviderKeyLifecycleDemoRegistryRenderTest 11/0/0, UndoGroupDemoRegistryRenderTest 5/0/0, ReceiptDemosRegistryRenderTest 14/0/0, ReceiptRowRegistryRenderTest 7/0/0, ComponentRegistryDriftGuardTest 1/0/0, DomainVocabularyDriftGuardTest 2/0/0, ComponentStatesMatrixTest 2/0/0. `api.txt` no diff after the dump; `tools/test/run-all.sh` exit 0; `tools/verify-api-source-additive.sh v2.5.1` exit 0 (`SEAMS: none`, SOURCE-ADDITIVE OK, removed 0, required-param changes 0, enum constants intact 55, added classes 29, added members 224); `tools/verify-binary-abi.sh v2.5.1` exit 3 (informational, 29 missing synthetic descriptors); INV-03 greps print nothing; API.md "Stable test tags" line 299; INTEGRATION.md "5b. Wiring the v2.6.0 ..." line 149; no `v2.6*` tag.

## Probe results (P01-P14)

| Probe | Result | Highest rung | Key fact |
|---|---|---|---|
| P01 Receipt confirm single open | PASS | 5 | one open row with the count editor (Few selected, Many disabled), warn card, pinned Cancel/Confirm, Confirm `enabled=true`; light 03/04, dark 07/08 |
| P02 batch collapsed | PASS | 5 | three collapsed rows; open B then C leaves exactly one detail (`More about` count 1) |
| P03 attention row | PASS | 5 | Confirm `enabled=false` before (captures 11, 25, 27), `enabled=true` after `Mark checked` (29), cue gone |
| P04 German batch | PASS | 5 | 384 dp: Abbrechen x 79-272, Alle bestätigen (3) x 650-979; 360 dp: 84-293 and 617-972; titles ellipsize, values stay |
| P05 count editor | PASS | 5 | Many ancestor `enabled=false`; tap leaves Few selected (two options per 24-01 deviation 1); warn amber vs failure error rose/red |
| P06 Success single/batch/all-undone | PASS | 5 | badge, handled-by pill, Done; `Saved 2 of 3 items` with `could not be saved` and Edit; `Reverted 2 items`, two `Undone`, 0 `Undo all` |
| P07 Failure error/warn | PASS | 5 | title, transcript, tone card, tip below the card, Dismiss at start and Retry at end; tones differ |
| P08 key lifecycle EN | PASS | 5 | summary, `Provider B · saved` only, Save disabled when empty, sentinel 0 hits (positive control 1), `key-…1234` after Save, Clear shows the field and drops the marker |
| P09 key lifecycle DE | PASS | 5 | Gespeichert/Aktualisieren/Entfernen/Abbrechen/Speichern inside the window at 384 and 360 dp |
| P10 Undo Center groups | PASS | 5 | indent 90 vs 45 px (16 dp); `Earlier batch` Resolved without Undo all; Undo all resolves `Batch edit` (button count 1 to 0) |
| P11 Undo Center DE | PASS | 5 | `Alle rückgängig machen` x 572-1001 (384 dp) and 534-996 px (360 dp), no overlap with the label column |
| P12 ReceiptRow Default cell | PASS | 4 | clickable ancestor 260 px = 92.4 dp (band 256-262) |
| P13 no crash | PASS | 3 | empty `AndroidRuntime:E` logcat capture 90 and `logcat -d` after one `logcat -c` before the first launch |
| P14 light and dark coverage | PASS | 5 | every Receipt demo and the count editor has both (Success batch light only, per plan) |

## Screencaps for the owner

33 PNG files (tester screens copied from the signed run, 1080x2316, 5.9 MB in total) in `.planning/phases/24-catalog-docs-explorer-demos-cut-v2-6-0/screencaps/`. Every file was opened with the image tool before staging; none shows a notification, a personal app or non-library content (the Samsung S Pen button at the right edge is a system overlay). Tall Receipt sheets were dragged up before capture (see ADV-1; the initial-detent shot is 29).

- `01-receipt-confirm-single-open-light.png` — Confirm changes?, Item A open with Few selected and Many greyed, amber warn card, teal Confirm.
- `02-receipt-confirm-single-open-dark.png` — same sheet in dark: dark teal surface, cyan Confirm, dark gold warn card.
- `03-receipt-confirm-batch-collapsed-light.png` — three collapsed rows A/B/C with counts and chevrons, Confirm enabled.
- `04-receipt-confirm-batch-collapsed-dark.png` — same in dark.
- `05-receipt-confirm-batch-attention-light.png` — Item B carries the amber "Check this one" cue, Confirm greyed out.
- `06-receipt-confirm-batch-attention-dark.png` — same in dark (gold cue, greyed Confirm).
- `07-receipt-confirm-batch-attention-resolved-light.png` — Item B expanded with the teal "Mark checked" button, cue gone, Confirm enabled.
- `08-receipt-confirm-german-360-light.png` — German batch at 360 dp: long titles ellipsize, counts and "Anzahl" visible, Abbrechen and "Alle bestätigen (3)" both fully inside.
- `09-count-editor-light.png` — Pickers page: Pill selector with Few (hub teal) and Many greyed plus the amber warn card; the demo is full-bleed (ADV-2) and navy dynamic-colour Pill rows above show the hub-vs-dynamic difference.
- `10-count-editor-dark.png` — same in dark (status-bar icons dim on the dark page).
- `11-receipt-success-single-light.png` — check badge, "Saved 1 item", "Handled by Local grammar · offline" pill, Done.
- `12-receipt-success-single-dark.png` — same in dark.
- `13-receipt-success-batch-light.png` — "Saved 2 of 3 items", Item C in red "could not be saved", Edit and Done.
- `14-receipt-success-all-undone-light.png` — grey reverted badge "Reverted 2 items", rows read "Undone", Done.
- `15-receipt-success-all-undone-dark.png` — same in dark.
- `16-receipt-failure-error-light.png` — red cross badge "Couldn't complete", rose error card, tip "Check the connection and try again." flush on the bar divider, Dismiss and Retry.
- `17-receipt-failure-error-dark.png` — same in dark (deep red card).
- `18-receipt-failure-warn-light.png` — amber "!" badge "Needs a closer look", amber card, same tip and bar.
- `19-receipt-failure-warn-dark.png` — same in dark (dark gold card).
- `20-key-lifecycle-summary-light.png` — Provider B saved: "Saved · key-…c3d4" with tonal Update and red Clear, no key field; German demo below.
- `21-key-lifecycle-edit-light.png` — edit flow: masked field (8 bullets) with Cancel and filled Save.
- `22-key-lifecycle-clear-light.png` — after Clear: empty field with disabled Save, no summary, no Cancel.
- `23-key-lifecycle-dropdown-light.png` — dropdown open: Provider A, "Provider B · saved", Provider C; only B carries the marker.
- `24-key-lifecycle-german-360-light.png` — German summary at 360 dp: Gespeichert, Aktualisieren, Entfernen, nothing clipped.
- `25-undo-groups-undoable-light.png` — "Batch edit · 3 items · Undoable" with Undo all, members indented 16 dp, the flat "Renamed item D", the resolved "Earlier batch".
- `26-undo-groups-resolved-light.png` — after Undo all: "3 items · Resolved", three "Undone", Undo all gone, item D still offers Undo.
- `27-undo-groups-german-360-light.png` — German group header at 360 dp: label ellipsizes at two lines, "Alle rückgängig machen" beside it, no overlap.
- `28-receipt-row-default-cell-light.png` — ReceiptRow States matrix, Default cell (measured 92.4 dp), Pressed, Disabled, Focused.
- `29-receipt-confirm-single-open-initial-detent-light.png` — ADV-1 evidence: the sheet at its initial partial detent, pinned Cancel and Confirm sit under the system navigation icons.
- `30-receipt-confirm-batch-item-c-open-light.png` — batch with Item C open and A and B closed (single-open accordion).
- `31-receipt-confirm-german-384-light.png` — German batch at the native 384 dp width.
- `32-key-lifecycle-german-edit-360-light.png` — German edit flow at 360 dp: Zugangsschlüssel field, Abbrechen and disabled Speichern.
- `33-key-lifecycle-german-384-light.png` — German edit flow at 384 dp, same layout.

## Device residue and state changes (all flagged)

- **Display density:** set to 480 for the three 360 dp German checks, restored. `wm density reset` did NOT return to the original on this Samsung (it printed `Override density: 600`, 288 dp wide); I restored with `wm density 450`, after which `wm density` prints exactly the pre-run `Physical density: 450` with no override line and the Explorer geometry is bit-identical. The setting `secure display_density_forced` now reads `450` (not read before the run, probably empty), so that one key may differ in form, not effect. Future runs: restore with `wm density 450`, never `reset`.
- Harness package installed then uninstalled (`pm list packages --user 0 | grep -ci yahirandroid` = 0); launcher focused; `wm size` override 1080x2316, `stay_on_while_plugged_in` 15 and animator scale 1 untouched; `logcat -c` cleared the device log buffer. A foreign app (`com.yahir.blackjacktrainer`) was focused when I started and was left alone.
- Repo: no `src/` change (`git diff --exit-code HEAD -- api.txt src` rc 0), no tag, no push, no consumer edit, nothing outside this plan's six paths staged.

## Owner flags

1. **HubSchemeScope (uat-pending/20 item 2):** the Receipt and count-editor demos pin the hub palette, the rest of the Explorer runs dynamic colour; screencap 09 shows both side by side. Decide whether to pin the hub scheme everywhere.
2. **360 dp via density:** German checks used `wm density 480` (1080 px = 360 dp); the Explorer itself cannot be width-pinned.
3. **English-only library words:** the group count and status words (`3 items`, `Undoable`, `Resolved`) stay English next to German labels by library design.
4. **ADV-1 and ADV-2** above, plus the Phase 20-23 advisories listed in the Gate-2 fragment.
5. **Tooling notes:** a 65-character `uat-run capture` label is refused (≤64); a second `uat-run finish` on a finished run exits 1 silently; two stray captures are signed and kept (`05`/`06` labelled dark but light, retaken as `07`/`08`; `71`/`72` duplicate `73`). Details in the SELF-UAT notes.

## Deviations from Plan

- The count editor has two options (Few and Many) rather than One/Few/Many (24-01-SUMMARY deviation 1); P01/P05 were read accordingly.
- Task 1 (rung-1 battery, build identity, harness) was done by a prior executor; the tester re-ran the full battery itself and the numbers matched.
- I took Receipt confirm screencaps after dragging the sheet up (initial-detent shot kept as 29).

## Self-Check: PASSED

- `uat-run verify --run uatr-20261012T230611Z-4dbe72cf` and `uat-run verify --self-uat 24-03-SELF-UAT.md` both `verified, ok: true`.
- 33 screencaps exist, all opened; byte check of the run dir: no ui/screen capture under 200 bytes (smallest 3959), none containing `No such file`, all png 1080x2316 and not single-colour.
