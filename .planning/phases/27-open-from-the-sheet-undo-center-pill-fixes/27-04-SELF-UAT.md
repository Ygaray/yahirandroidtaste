---
status: complete
result: all_pass
gate: 1
evidence_schema: 3
uat_run: uatr-20261017T194228Z-30718f43
phase: 27-open-from-the-sheet-undo-center-pill-fixes
source:
  - "27-ROADMAP success criteria"
device: SM-S908U (R5CT10XNKQN)
apk: io.github.ygaray.yahirandroidtasteharness (sha256 381843a6269f)
run: 2026-10-17T19:42:28.410Z
command_guard_breaches: 0
command_guard_refusals: 12
---

<!-- Gate-1 self-UAT, phase 27 (consolidated per phase; evidence under the last plan, 27-04, as for Phases 25 and 26). -->

# Self-UAT Log — Phase 27 (Open from the sheet and Undo Center, pill fixes), run against plan 27-04

**Device:** SM-S908U tester, adb serial `R5CT10XNKQN` over USB (Android 15, API 35, density 450, `Override size 1080x2316`, font scale 1.0 at rest)   **Run:** 2026-10-17T19:42:28Z (`uat_run` uatr-20261017T194228Z-30718f43, 114 signed captures/ingests, `uat-run verify` = verified, 4 empty-logcat captures recorded as signed negative evidence)
**Build identity:** library HEAD at the AAR build `b9f8451`, now `2271a0f` (the commits between are `.planning` docs only: `git diff --stat b9f8451 HEAD -- src` is empty and `git status --short src` is empty; `src/` is identical to `ca5fae5`, the phase-27 verification commit). Library AAR (mavenLocal `com.github.Ygaray:yahirandroidtaste:1.10.0`, deleted, then republished from this tree with `clean publishReleasePublicationToMavenLocal`) md5 `01e623ffa9f2e8542e8c42e507e62240`; its `classes.jar` contains `OpenTargetRow`, `HandledByPillShape`, `HandledByDot`, `UndoHistoryOpenRow` and the harness dex references them. Throwaway HEAD harness APK (scratchpad, never committed; `P27Activity` consumer plus `DeckA11yService` observer) md5 `06f7476680a5cf9b7c7bb2018b745fb4` (sha256 prefix `381843a6269f`). BASELINE: the `v2.7.1` tag exported with `git archive` (no working tree), same republish procedure, AAR md5 `41959a0847c8291fb8b1f0dee44f9354` (contains none of the Phase 27 classes), baseline harness APK md5 `586bdf5f582668a376d9d6b4312eb1fe` (a copy of the harness minus every Phase 27 call). The baseline AAR was replaced by the HEAD AAR before the signed run; it exists to give the "renders exactly as v2.7.1" claims and the POL-01 defect a real control. Record `114-99-arrange-and-restore-record.txt`.
**Pre-flight:** device awake, no keyguard, `get-state` = device. Leases `device:tester` and `build` were held by `secondbrain@vae-bilingual` (SB 181.1 Gate-1) at dispatch; I queued and was granted both at 12:44, waited about 20 minutes, never drove the device before, released both at the end. The wireless transport (`100.126.94.47` and `100.118.21.106:1496`) was never addressed; every adb call used the USB serial.
**Rung 1 (fresh):** `./gradlew testDebugUnitTest detekt apiCheck --rerun --no-build-cache --no-daemon` in the main checkout → BUILD SUCCESSFUL in 6m31s, **167 result classes, 1735 tests, 22 skipped (pre-existing `@Ignore`s), 0 failures, 0 errors**; `:detekt`, `:metalavaCheckCompatibilityRelease` and `:testDebugUnitTest` executed (not UP-TO-DATE). `bash tools/verify-api-source-additive.sh v2.7.1` → `SEAMS: none`, `API SOURCE-ADDITIVE OK baseline=v2.7.1 removed_classes=0 removed_members=0 enum_constants_intact=73 required_param_changes=0 added_classes=5 added_members=48 signature_changes=0 accepted_exceptions=0`. Ingested as `02-01-unit-detekt-apicheck-summary.txt`, `03-02-api-source-additive-v271.txt`. Test edits since `v2.7.1` (`git diff v2.7.1..HEAD --name-status -- src/test`): the only modified OutcomeSheet or UndoCenterScreen test files are `OutcomeSheetFixtures.kt` (additive defaulted parameters) and `UndoCenterScreenSourceContractTest.kt` (the deliberate D-04 count edit, +8/-2); no `OutcomeSheet*Test` was edited. I did not run `gsd-validate-phase`; `27-VALIDATION.md` is the plan-time draft.
**Seed/fixture integrity:** the library has no data layer. The fixtures are the harness's in-memory `UndoHistoryStore` (real store, real `attemptUndo`, entries and a group seeded in `LaunchedEffect`, one entry undone through the real store so an Undone row exists) and `VoiceOutcomeUiState.Success` values built in the harness. Seeding is "publish the right HEAD": done, md5 above. Arrange = building those states; Act = real `adb input` taps/holds/key events/swipes, plus accessibility actions from a real `AccessibilityService`. The harness only prints open count, undo-run count and which key opened; it has no logic that can produce a result.
**Destructive check:** not applicable. The repo has no `src/androidTest` (`ls src` = `main`, `test`), so there is no `connectedAndroidTest` / wiping suite: no `destructive_check`, no `final_state_check`. No provider key or cloud call exists in this phase (shipped-config rule: nothing substituted).
**Device state changed and restored** (record `114`): `system font_scale` 1.0 → 2.0 → 1.0; `wm density` 450 → 480 (so the 1080 px wide window is exactly 360dp) → 450 (`wm density reset` never run; read back `Physical density: 450`, no override); secure `enabled_accessibility_services` null → harness service → null and `accessibility_enabled` 0 → 1 → 0. All read back after restore. Harness APK uninstalled (re-installed once only so `uat-run finish` could re-verify the APK, then uninstalled again, `pm list packages` = 0).

## Method notes (read these before the verdicts)

- **Three earlier runs were abandoned and deleted, none feeds a verdict:** `uatr-20261017T190020Z-6291aa6b` and `uatr-20261017T190901Z-6a7ab214` (harness APK rebuilt: first to add `keep`/`inflight_ms`/`cleartgt_ms` extras, then because the observer's press-less long-click lookup only matched a node's own text), and `uatr-20261017T193316Z-cc39dc86` (its ingests were confounded by keyboard-focus mode, see below). The signed run above was driven end to end on the final APK.
- **Keyboard-focus mode confound, found and removed.** The keyboard leg (TAB/ENTER) leaves the window in keyboard-focus mode (`mInTouchMode=false`), which paints a focus state on the first Button and a "Drag handle" tooltip, producing ~61k differing pixels against the baseline. I restored touch mode with a tap (`mInTouchMode=true`) before the parity battery and ran the keyboard leg last in the signed run; the parity set (`03`) is clean.
- **Observer service dies with a force-stop.** `am start -S` force-stops the package, which disables the accessibility service; the harness re-enables it after each launch where a dump or action is needed. Two of my first attempts logged nothing for that reason and were redone.
- **Signed `screen`/`ui` captures take longer than 3 s**, so the 3 s self-dismissing press-less peek is shown with two raw back-to-back `uiautomator` dumps (`104`, `105`) ingested with a timing record (`106`), not with signed captures.
- **Uniform lookups.** Pixel probes (`06`, `19`, `30`) are my own scripts over the signed PNGs: WCAG relative-luminance contrast, row-by-row "is every dark pixel between pill-coloured pixels of its row".

## Criteria

### 1. SC1 — success sheet row that names the item shows a chevron and calls `onOpenTarget(target)` on tap, in Receipt and Classic, via the default-state and the hoisted-`sheetState` overload, with no action label; no target or no handler renders exactly as v2.7.1 (FOPEN-01)
result: passed
- **Rung:** 5 (chevron and absence of an action label read on screenshots), with 4 (tree, accessibility actions), 3 (log) and 1 (unit suite) beneath it.
- **Target:** device (tester).
- **Expected (re-derived from the ROADMAP row):** with `Success.target` and `onOpenTarget`, the row that names the item has a chevron and one tap makes exactly one `onOpenTarget(target)` call, in Receipt and Classic, through both `OutcomeSheet` overloads, and the row carries no action label text; with no target, or a target and no handler, the sheet is the v2.7.1 sheet; existing OutcomeSheet tests unedited.
- **Arranged (seeded):** `Success(summary, handledBy, target=FeedbackTarget("s1", openLabel), targetLabel, targetDetail)` in German (long label, detail) and English, harness `onOpenTarget` that logs `open <key> n=<count>`; hoisted variant: `rememberModalBottomSheetState` passed as `sheetState`, handler does `scope.launch { sheetState.hide(); … ; log }`, default variant hides by clearing its own flag.
- **Did (drove):** one real `adb input tap` at the row centre in each of four variants (Receipt default, Receipt hoisted, Classic default, Classic hoisted); in-flight lock with `inFlight = true` then cleared after 16 s (tap while locked, tap after unlock); the caller clearing `target` while the sheet is up; accessibility dump of the row; screenshots at 384dp and at 360dp / font 2.0.
- **Observed:**
  - **Single tap, four variants.** Logs `35` (Receipt default), `38` (Receipt hoisted), `42` (Classic default), `45` (Classic hoisted) each contain exactly one line `open s1 n=1`; the tree after the tap shows the page behind the sheet again. The hoisted variants hid the sheet first, then logged.
  - **Chevron, no label.** Screenshots `33` (Receipt) and `40` (Classic) read: item title, detail, chevron on the right, hairline above; English check `46`/`49` (targetLabel "Weekly shopping list", openLabel "Open item") shows "Weekly shopping list / 12 items, edited yesterday / >" and the string "Open item" appears nowhere on screen (`grep -c "Open item"` on trees `47`, `50` = 0). The openLabel lives only as the accessibility click label.
  - **Semantics.** `35`, `38`, `42`, `45`, `48`, `51`: the row is one merged View `click=true focusable=true a11yFocusable=true`, `acts=[64:|1:|16:<openLabel>]` (`16:Eintrag öffnen`, `16:Open item`), the `android.widget.Button` projection of `Role.Button` is a child with the row's bounds, the chevron is a child with `imp=false` (decorative). The title/detail are the caller's `targetLabel`/`targetDetail` text, nothing from the library.
  - **No target / no handler = v2.7.1.** Baseline harness compiled against the real `v2.7.1` AAR vs the HEAD harness with `tgt=0 handler=0`, `tgt=1 handler=0`, `tgt=0 handler=1`, default and hoisted, both styles (`03`, threshold 2 on a 0 to 255 luminance difference): identical node sets (19/18 nodes, `only_b=0 only_h=0`) and **0 differing pixels** for all of them, including the `Receipt hoisted + target + no handler` and `Classic hoisted + target + no handler` cases. A target with no handler draws no row and no chevron.
  - **In-flight lock.** `52`/`57` (locked trees): no clickable row node; screenshots `53`/`58` read: the row is dimmed (title, detail and chevron faded); a tap at the row centre logs nothing (`54`, `59` empty); after `inFlight` clears (`55`, `60` show the row clickable again) one tap logs `open s1 n=1` (`56`, `61`).
  - **Caller drops the target.** `62`→`63` and `65`→`66`: after the caller clears `target` the row node is gone while the sheet content ("…gespeichert") stays and no new sheet window was created (D-10 / PD-2).
  - **Long German at 360dp, font 2.0** (`76`, `80`, screens read): the row title and detail wrap to 3 lines each and the chevron stays on the right edge of the row in both styles; nothing pushes it off.
  - **Rung 1:** the 1735-test suite includes the Robolectric state assertions (`OutcomeSheetOpenTargetTest`, parity and source-contract tests, `V271CallSiteCanaryTest`), 0 failures; no existing OutcomeSheet test was edited (see above).
- **Evidence:** `32` to `51`, `52` to `67`, `76`, `77`, `80`, `81`, `03`, `04`, `02`, `01`.

### 2. SC2 — in a batch success each `BatchRowResultUiModel` with a `target` opens its own item; rows without one show no chevron, aren't clickable and expose no click action (FOPEN-02)
result: passed
- **Rung:** 5 (chevrons per row read on screenshots) with 4 (tree and accessibility actions) and 3 (log).
- **Target:** device (tester).
- **Expected:** targeted rows open their own target once per tap, in both styles; target-less rows (including a failed one with a detail) draw no chevron, are not clickable and have no click action.
- **Arranged (seeded):** a five-row batch: Milch (ok, target `b1`), Haferflocken (ok, no target), Käse (failed with detail, target `b3`), Joghurt (failed with detail, no target), a long German Vollkornbrot row (ok, target `b5`); handler logs the key and keeps the sheet up (`keep=1`) so one launch can take every tap.
- **Did (drove):** one real tap on each targeted row top to bottom, then one tap on each of the two target-less rows, in Receipt and in Classic; accessibility dump; scrolled screenshots at 360dp / font 2.0.
- **Observed:**
  - **Exactly three opens, in row order.** Logs `71` (Receipt) and `75` (Classic): `open b1 n=1`, `open b3 n=2`, `open b5 n=3`; the taps on "Haferflocken" and "Joghurt" logged nothing.
  - **No click action on target-less rows.** Dumps in `71`/`75`: exactly three nodes carry `acts=[64:|1:|16:Eintrag öffnen]`; the "Haferflocken" and "Joghurt" nodes are bare `TextView`s `click=false focusable=false` with no `16:`. uiautomator: only the three targeted rows are `clickable` (`row0..row2` bounds in the transcript; `68`/`72` trees).
  - **Chevrons only on targeted rows.** Screenshots `69` (Receipt) and `73` (Classic) read: chevron on Milch, on the red failed Käse row and on Vollkornbrot; none on Haferflocken or on the red failed Joghurt row; the target-less rows reserve no chevron space and keep the same text inset, but they lack the 48dp minimum height of the clickable rows, so they are a little shorter (FA-4 as designed; a uniform-height call stays with the owner).
  - **Long German at 360dp, font 2.0** (`78`, `82`, scrolled screenshots read): "Käse konnte nicht gespeichert werden" and the long Vollkornbrot label wrap over three to four lines and the chevron stays at the right edge of its row, vertically centred, in both styles.
  - **Rung 1:** `OutcomeSheetBatchOpenTargetTest`, the mixed-batch, equal-targets and `noHandler_withTargetedRows_rendersTheV271List` tests are in the 1735 green; device parity `batch_*_rt_nohandler` (targets present, no handler) = 0 differing pixels against the v2.7.1 baseline (`03`).
- **Evidence:** `68` to `75`, `78` to `83`, `03`.

### 3. SC3 — through the new `UndoCenterScreen` overload with `onOpenTarget`, an Available entry with a target opens its item on tap; entries that aren't Available, or have no target, don't; the existing overload renders as v2.7.1 (FOPEN-03, including the WR-01/WR-02 review fixes)
result: passed
- **Rung:** 5 (chevrons, press and focus states, peek, post-undo row read on screenshots) with 4 (tree, accessibility actions, focus), 3 (log) and 1.
- **Target:** device (tester).
- **Expected:** Available + target: one tap opens once and neither undoes nor peeks; Undone, target-less and group-header rows do nothing and show no chevron or click action; a hold shows the peek and its release opens nothing; the Undo button undoes once without opening, after which the row shows neither chevron nor click action; keyboard Enter on the focused row opens; the press-less long-click (accessibility `Preview` action) shows the peek and it hides itself; the old overload is v2.7.1.
- **Arranged (seeded):** a real `UndoHistoryStore`: `U1` Available + target `u1` (long German message), `U2` Available no target, `U3` target `u3` then `store.attemptUndo` so it is Undone, a group "Drei Einträge importiert" with members `G1B` (no target) and `G1A` (target `g1a`); harness logs `open …`, `undo tap …`, `undoAction ran …`. Seeding itself ran `U3`'s undo once (`UNDORUNS=1` at start).
- **Did (drove):** taps on `U2`, `U3`, the group header, `G1B`, then `U1` and `G1A` (one launch); a 6.5 s hold on `U1` with a screenshot mid-hold; a tap on `U1`'s Undo button, then a tap on the now-Undone row; `TAB` ×3 to focus the row, `ENTER`, `TAB`, `DPAD_CENTER`; the accessibility long-click and click actions; the v2.7.1 overload with taps on the same rows; the same screen at 360dp / font 2.0.
- **Observed:**
  - **Taps.** Log `87`: only `open u1 n=1` and `open g1a n=2`; the four taps on non-openable rows (`U2`, `U3`, group header, `G1B`) logged nothing. The first dump `84` shows exactly two clickable+long-clickable+focusable rows (`U1`, `G1A`); `U2`, `U3` ("Undone"), the header and `G1B` are not clickable. Screenshot `85` read: chevron (after the Undo button) on `U1` and `G1A` only, "Undone" label and no chevron on `U3`, no chevron on `U2`, `G1B` or the header.
  - **Semantics.** `87`: the two openable rows carry `acts=[64:|1:|16:Open item|32:Preview]` (click labelled with the openLabel, long-click labelled "Preview").
  - **Hold.** `88` (screenshot read): the row is pressed (grey indication layer) and the preview card with the message floats over it; tree `89` has the peek text node `[102,481][978,585]`; after release `90` has no peek node; log `91` is empty (no open, no undo).
  - **Undo button.** `94`: `undo tap <id>` then `undoAction ran U1` once, no `open`; `92` tree and `93` screenshot read: the row now shows "Undone", no chevron, no clickable node; a tap on its body logs nothing new (`95` identical to `94`: stale-callback guard).
  - **Keyboard (WR-01).** `100`: after TAB ×3 the openable row is the focused node (`focused=true`, clickable+long-clickable); `101` screenshot read: focus highlight over the row; `102`: `ENTER` → `open u1 n=1`; `103`: TAB moves to the Undo button and `DPAD_CENTER` → `undoAction ran U1` with no second open.
  - **Press-less long-click (WR-02).** `98`: `act2 'Wocheneinkauf' longclick … -> true`; `104` (dump ~3.4 s after the action) contains the peek node, `105` (~9.8 s) does not (`106`); `99`: the accessibility click opened `u1` once; nothing opened or undid on the long-click.
  - **Old overload = v2.7.1.** `undo` parity: 59 nodes vs 59, 0 differing pixels against the real `v2.7.1` AAR even though the entries carry targets (`03`, `11`, `12`); taps on `U1` and `G1A` through the old overload log nothing (`110` empty, `108` tree has no clickable merged row).
  - **Long German at 360dp, font 2.0** (`111` read): the Undo and chevron stay on the row; the message column is 186dp wide against 210dp on the old overload (the chevron costs 24dp, `113`), so the message wraps to 7 lines and breaks mid-word as it already did at this size.
  - **Rung 1:** `UndoCenterScreenOpenTargetTest`, `UndoCenterScreenOpenParityTest`, `UndoCenterScreenSourceCompatV251Test`, the default-parity and `V271CallSiteCanaryTest` (trailing-lambda call sites) are in the 1735 green; trailing-lambda compilation is a rung-1 claim (the harness uses named arguments).
- **Evidence:** `84` to `87`, `88` to `91`, `92` to `95`, `96` to `99`, `100` to `103`, `104` to `107`, `108` to `110`, `111` to `113`, `03`, `11`, `12`.

### 4. SC4 — at font scale 2.0 a long handled-by pill that wraps to several lines keeps all its text inside its shape; at 1.0 a one-line pill looks as in v2.7.1 (POL-01)
result: passed
- **Rung:** 5 (screenshot read plus a row-by-row pixel probe), with a v2.7.1 control.
- **Target:** device (tester).
- **Expected:** the Receipt pill's corner radius is capped so a multi-line pill leaves no text outside the outline; a one-line pill is the old capsule at every font scale.
- **Arranged (seeded):** `HandledByUiModel(prefixLabel "Bearbeitet von", tier "Verarbeitung auf dem Gerät ohne Netzwerkverbindung", provider "Lokale Grammatik offline", onDevice = true)` in a Receipt sheet, 360dp wide window (`wm density 480`), `font_scale 2.0`, hub-light and device-dynamic themes; the same harness scenario built against the `v2.7.1` AAR as the control.
- **Did (drove):** launched the scenario, took signed screenshots and trees, ran the pill probe on the signed PNGs.
- **Observed:**
  - **HEAD at font 2.0, 360dp.** Screenshot `14` (hub) and `16` (dynamic) read: the pill wraps to five lines ("Bearbeitet von / Verarbeitung auf dem / Gerät ohne / Netzwerkverbindung · / Lokale Grammatik offline"), the dot sits at the left of the third line, every glyph is inside a rounded rectangle with a small corner radius. Probe `19`: `rows with ink=305, rows with ink outside the pill-colour span=0`, `OK-all-text-inside-shape`, for both themes (painted rect 328dp x 164dp).
  - **Control: the v2.7.1 AAR with the same scenario.** `07` (screenshot read: "Bearbeitet von" and "Lokale Grammatik offline" cut by the capsule arc, the first line starts left of where the shape begins) and probe `06`: `rows with ink outside the pill-colour span=80`, first violation row y=1659, first ink x=126 against pill span 150 to 899, `FAIL-text-outside-shape`; the same for the dynamic theme. So the probe is not vacuous and the defect exists on the shipped v2.7.1 and is gone at HEAD.
  - **One-line pill unchanged.** `18` (HEAD, font 1.0, 384dp) read: the old capsule with dot and "Handled by Local grammar"; pixel comparison against the v2.7.1 baseline at threshold 2/255 (`03`): `sheet_receipt` 0 px, `pill_one_f1_360` 0 px (360dp), `pill_one_f2` 0 px (font 2.0, still one line), `dot_dark` 0 px; at threshold 24 the same. The capsule shape did not change for any one-line case. Two-line pills at font 1.0 (`pill_long_f1_360`, 1868 px) change corner geometry by design (cap = one-line half height).
  - **Classic untouched** (`sheet_classic`, `batch_classic` 0 px against v2.7.1).
  - **Rung 1:** `HandledByPillShapeTest` (NATIVE wrapped-pill corner containment with its failing `CircleShape` control, one-line radius equality at 1.0/1.5/2.0, zero size, unspecified/em line height) is in the 1735 green.
- **Evidence:** `13` to `19`, `06` to `10`, `03`, `04`.

### 5. SC5 — the Grammar (warn, on-device) handled-by dot meets 3:1 non-text contrast against the pill under the hub light and dark schemes and under a dynamic-color sample (POL-02)
result: passed
- **Rung:** 5 (screenshots read, enlarged montage `31`) with pixel probes of the signed PNGs.
- **Target:** device (tester).
- **Expected:** a dot whose fill already has 3:1 against the pill gets no ring and is unchanged; a dot that fails gets a ring (outside the 6dp footprint) that itself has 3:1; `ToneDefaults.warn()` and the dot's size/position stay.
- **Arranged (seeded):** a Receipt sheet with `HandledByUiModel(tier "Local grammar", onDevice = true)` under: hub light (`dynamicColor=false`), hub dark, the device's real dynamic colour (`dynamicColor=true`, Android 15 One UI), and a caller-passed white warn tone (`OutcomeSheetDefaults.colors(warnTone = ToneColors(White, White))`) under hub and under dynamic colour.
- **Did (drove):** signed screenshots and trees of the five cases, probe on the signed PNGs (`30`), the same five scenarios against the v2.7.1 AAR as controls (`04`/`03` list them).
- **Observed (probe `30`, WCAG ratio of the dot fill and of the ring against the pill colour):**
  - **hub light** (`20`): pill (220,227,229), fill (37,26,0), 13.18:1, **no ring** (pixel-identical to v2.7.1: `sheet_receipt` 0 px).
  - **hub dark** (`26`): pill (33,56,54), fill (255,224,160), 9.75:1, **no ring** (0 px against v2.7.1).
  - **device dynamic colour** (`24`): pill (226,226,233) and the warn content colour is **white** (255,255,255): 1.29:1, which is what v2.7.1 draws (the dot is near invisible on the baseline: montage `31`, rows 1 and 3, and the baseline controls differ only in the ring region, bbox 27x27 px around the dot, 356 px); HEAD draws a **ring** (26,27,32) at 13.33:1, 1.42dp wide (spec 1.5dp), around the unchanged fill chord of 5.3dp inside the 6dp footprint.
  - **caller white-on-light tone under hub** (`22`): fill (255,255,255), 1.30:1; ring (23,29,30) = `onSurface`, 13.13:1, 1.42dp.
  - **dynamic + white tone** (`28`): fill 1.29:1, ring 13.33:1.
  - Screenshot montage `31` read: v2.7.1 shows a white dot that almost disappears on the pale pill (dynamic and white-tone rows); HEAD shows a dark ring around a white centre. The dot's position, text and the rest of the pill are untouched. FA-9 is answered: the near-invisible dot reproduces on this device's real dynamic colour, and the guard ring fixes it.
  - **Rung 1:** `HandledByDotContrastTest` (hub, synthetic white-on-light, four dynamic-style schemes, the 0..255 grey ramp totality, the 3:1 boundary, `handledByDotColor` byte-stable) is in the 1735 green.
- **Evidence:** `20` to `31`, `04`, `03`.

## Summary

total: 5
passed: 5
partial: 0
failed: 0
infra: 0

## Notes / anomalies (for the Gate-2 reviewer)

- **Design call for the owner (not a defect):** under this device's real dynamic colour the ring is visible on every Receipt pill with an on-device handled-by; with the ring the dot reads as an outlined circle with a white centre (montage `31`), a heavier look than the hub-colour pill. The criterion is met either way; whether to prefer an inverted fill is a taste decision.
- **Undo Center message column (informational):** an openable row's chevron takes 24dp from the message column; at 360dp / font 2.0 a long German message wraps to 7 lines (`113`). Nothing is pushed off. v2.7.1 already breaks the same message mid-word at this size.
- **Title truncation (pre-existing):** at 360dp / font 2.0 the Receipt sheet title truncates with an ellipsis after two lines ("Eintrag Wocheneinkauf für das lange Wochene…"); the v2.7.1 baseline `07` truncates identically. Not touched by this phase.
- **Peek timing:** the press-less long-click peek lasts about 3 s; the signed capture pipeline is slower than that, so the visible/gone pair is raw dumps with timestamps (`104`, `105`, `106`).
- **Not exercised here:** TalkBack itself (Phase 28, A11Y-02), the Explorer demos for these surfaces (DEMO-03), trailing-lambda `UndoCenterScreen` call sites on device (rung 1 canary), a real finger's press indication timing (the press state is seen with a 6.5 s `adb` hold).
- **Samsung pencil overlay** at the right edge of some screenshots is a device overlay, not the app.
- **Command guard.** `command_guard_breaches: 0`. `command_guard_refusals: 12`, all fail-closed `uninspectable` refusals of calls that never ran: a `source /dev/stdin`, four scripts written and run in the same call, three python invocations whose path was a variable or a not-yet-written file, four calls whose script or `git` argument used an unresolved variable or command substitution. None was a forbidden command (`git push`, `git tag`, `wm density reset`, `adb connect`, `gradlew --stop`, a personal-phone adb address); each was re-issued as a literal command or a script file written beforehand.
- **Residual device differences:** none (font scale 1.0, density 450, accessibility settings null/0, harness uninstalled).
- **Convergence:** first Gate-1 cycle for Phase 27; no prior SELF-UAT existed, so there was no prior verdict to audit. The claims I treated as claims to falsify were the plan's `27-VERIFICATION.md` "device-only aspects deferred to Gate-1" items, each re-derived from the ROADMAP wording above.

## Findings routed to gap-closure

None.

## Verdict

**ALL 5 criteria PASS** on the real tester against library `src` at `ca5fae5`/`b9f8451`/`2271a0f` (AAR md5 `01e623ff…`, harness APK sha256 `381843a6269f`, uat_run `uatr-20261017T194228Z-30718f43`). FOPEN-01/02/03 open the right item exactly once, through both sheet overloads in both styles, with no label text and no behaviour change for a v2.7.1 caller (0 differing pixels and identical trees against the real `v2.7.1` AAR); POL-01 is fixed (the v2.7.1 control fails the same pixel probe the HEAD build passes, and one-line pills are pixel-identical); POL-02 holds on hub light/dark and on the device's real dynamic colour (where v2.7.1's dot was 1.29:1 and HEAD adds a 13.3:1 ring). Gate-1 is satisfied; the Gate-2 obligation is registered in `.planning/uat-pending/27-open-from-the-sheet-undo-center-pill-fixes.md`.
