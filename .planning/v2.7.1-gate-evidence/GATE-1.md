# Gate-1 device check: v2.7.1 patch

**Date:** 2026-10-14 · **Verdict: all 4 probes PASS** · diagnose-only, nothing fixed, committed, tagged or pushed.

**Device:** tester `yahirs-s22-ultra-2`, SM-S908U, USB serial `R5CT10XNKQN`, Android 15. Every adb command used `-s R5CT10XNKQN`. `100.118.21.106:1496` (same phone, wireless) and the personal phone `100.126.94.47:39087` were listed in `adb devices` but never addressed. Geometry: 1080x2316 override on a 1440x3088 panel, 450 dpi, so 1 dp = 2.8125 px and the screen is 384 dp wide (3 px/dp at the 480-dpi 360 dp check). The navigation bar is 3-button, frame `[0,2181][1080,2316]` (from `dumpsys window` InsetsSource navigationBars).

**Driver:** `AGENT-DEVICE-TESTING.md` D1-D7 (same-package harness launching `ExplorerActivity`), driven by text with host-side uiautomator helpers in the session scratchpad. Bounds are read from the dumps; the button bounds below are the nearest `clickable` ancestor of the label.

## Build identity

| Item | Value |
|---|---|
| HEAD | `489848c` (main), `git status --porcelain -- src api.txt build.gradle.kts settings.gradle.kts API.md INTEGRATION.md` empty before and after |
| Library AAR | `com.github.Ygaray:yahirandroidtaste:1.10.0`. First deleted `~/.m2/.../yahirandroidtaste/1.10.0/` and `maven-metadata-local.xml`, then ran `./gradlew clean publishReleasePublicationToMavenLocal --no-daemon -q` (rc 0). |
| AAR md5 | **`9f73ccbff8519343058d0705c175db34`** (2068229 bytes); re-read after the run, unchanged |
| classes.jar | contains `io/github/ygaray/yahirandroidtaste/feedback/UndoGroupRowUiModel.class` (8648 bytes); `explorer/ReleaseDemosKt.class` holds the v2.7.1 strings `Erledigt`, `Teilweise erledigt` and `group rows, German labels` |
| Harness | throwaway app `io.github.ygaray.yahirandroidtasteharness`, copied from the P24 `h24` scaffold into scratchpad `h271/` (outside the repo, never committed), `clean assembleDebug --no-daemon --refresh-dependencies` (rc 0); its `classes6.dex` contains `Teilweise erledigt` |
| APK md5 | **`a78b1f6b459cbbc1309d20eab86d1d49`**, the same value host-side and read back on the device with `adb shell md5sum <pm path>` |

## Pre-flight (before install)

- `mWakefulness=Awake` after `KEYCODE_WAKEUP`; `isKeyguardShowing=false`.
- `wm density` printed `Physical density: 450` with no override line; `wm size` printed Physical 1440x3088 with Override 1080x2316.
- `secure display_density_forced` = `450` (left over from the P24 restore, as P24 note 7 predicted).
- `stay_on_while_plugged_in` = `0` (P24 recorded 15; I did not change it), `animator_duration_scale` = `1`, `screen_off_timeout` = 600000.
- `pm list packages --user 0 | grep -ci yahirandroid` = 0.
- Focused window: `com.yahir.blackjacktrainer/.MainActivity`, the same leftover app as in P24. Nothing else was driving the phone; I left it alone.
- `adb logcat -c` was run right before the launch.

## Probes

### P1: Tall OutcomeSheets skip the partial detent. PASS (light and dark)

Every sheet was captured about 1.8 s after tapping `Show sheet`, with no drag. The baseline was P24 ADV-1: tall sheets opened at the partial detent, with `Confirm` under the nav Back arrow.

| # | Demo | Sheet bounds | Bar region | Cancel / Dismiss | Confirm / Retry / Done | Result |
|---|---|---|---|---|---|---|
| 05 (light), 09 (dark) | Classic `NeedsConfirmation, TALL body → scroll + pinned Cancel/Confirm` (body taller than the screen) | `[0,75][1080,2181]`, **fully expanded** (scroll body `[0,210][1080,2035]`) | 2035-2181 | `[503,2046][693,2181]` | `Confirm all` `[704,2046][1035,2181]` | Bar on screen and above the nav bar. Drawn pills are about y 2056-2170 per screenshots 05 and 09. |
| 01 (light), 10 (dark) | Receipt `Confirm, single item open` | `[0,993][1080,2181]` | `[0,1964][1080,2181]` | `[45,2001][236,2136]` | `[779,2001][1035,2136]` en=true | Bar fully visible, 45 px clear of the nav bar |
| 02 (light) | Receipt `Confirm, batch collapsed` | `[0,1057][1080,2181]` | 1964-2181 | `[45,2001][236,2136]` | `[779,2001][1035,2136]` en=true | PASS |
| 03 (light), 11 (dark) | Receipt `Confirm, batch with an attention row` | `[0,979][1080,2181]` | 1964-2181 | `[45,2001][236,2136]` | `[779,2001][1035,2136]` **en=false** (attention gate intact) | PASS |
| 04 (light) | Receipt `Confirm, batch, German labels` | `[0,1057][1080,2181]` | 1964-2181 | `Abbrechen` `[45,2001][306,2136]` | `Alle bestätigen (3)` `[594,2001][1035,2136]` | PASS |
| 07 (light) | Receipt `Failure, error` (Failure bar) | `[0,1442][1080,2181]` | 1964-2181 | `Dismiss` `[45,2001][255,2136]` | `Retry` `[754,2001][1035,2136]` | PASS |

- The bottom of every bar region is y 2181, which is the top of the nav bar, and all buttons end at or above it.
- In the Receipt sheets the buttons end at 2136, 45 px (16 dp) clear of the nav bar.
- The Receipt confirm sheets fit their content, so they open at content height with the bar fully visible.
- The Classic TALL body exceeds the window, so it opens fully expanded at y 75 (just below the status bar) and the body scrolls under the pinned bar.

**Short sheets still open at their content height:**
- **06**, Receipt `Success, single`: sheet `[0,1625][1080,2181]`, `Done` `[831,2001][1035,2136]`.
- **08**, Classic `Success, summary only`: sheet `[0,1877][1080,2181]`.
- Neither is stretched to full screen; screenshots 06 and 08 show the gallery above the scrim.

**Screens read:** 01, 03, 05, 06, 07, 08, 09, 10. Dark mode (09, 10) shows a dark surface with a cyan/periwinkle Confirm and a dark-gold warn card, and the bar is fully above the nav bar.

### P2: Undo Center group words are localizable. PASS

- **12, English section** `UndoCenterScreen — group rows (Undo all, indented members)`:
  - `Batch edit` with `3 items · Undoable` `[45,982][340,1021]` and `Undo all` `[855,949][1001,1006]`.
  - `Earlier batch` with `2 items · Resolved` `[45,1950][335,1989]`.
  - Members are indented to x 90, versus 45 for the flat `Renamed item D`.
- **13, German section** `UndoCenterScreen — group rows, German labels`, before the tap:
  - `Sammelbearbeitung der Positionen aus der letzten Sitzung` (ellipsized to 2 lines) with `3 Einträge · Rückgängig möglich` `[45,751][538,835]`, next to `Alle rückgängig machen` `[572,713][1001,770]`.
  - `Earlier batch` with `2 Einträge · Erledigt` `[45,1740][355,1779]`.
- **14, after one tap on `Alle rückgängig machen`:**
  - The header reads `3 Einträge · Erledigt` `[45,751][355,790]`.
  - The `Alle rückgängig machen` node count went from 1 to **0**.
  - Members A, B and C read `Undone`, and `Renamed item D` still offers `Undo`.
  - With the button gone, the title has the full width and now wraps onto 2 lines in full.
- **15, 360 dp** (`wm density 480`, fresh demo state after re-entering the page):
  - Subtitle `3 Einträge · Rückgängig möglich` at `[48,757][498,847]`; `Alle rückgängig machen` at `[534,718][996,778]`, entirely inside 0-1080.
  - The gap is 36 px (12 dp): **no overlap and no clipping**. The title ellipsizes (`der Positionen aus de…`) and the subtitle wraps onto its 2 allowed lines.
  - `2 Einträge · Erledigt` is on one line. Density was then restored with `wm density 450`.
- **Screens read:** 12, 13, 14, 15.

### P3: Count-editor demo padding. PASS

- Pickers > SegmentedOptionSelector, section `SegmentedOptionSelector — count editor (Pill, one option disabled, warn ToneCard)` (captures 16 and 17; it is the last demo on the page, so 17 is the end of the scroll).
- Demo column `[45,1703][1035,2181]`:
  - `Count` label left edge at 45.
  - Selector `[45,1773][1035,1886]` (`Few` 45-540, `Many` 540-1035).
  - Warn ToneCard `[45,1909][1035,2181]`.
- That is a **45 px = 16.0 dp inset on both edges** (45 / 2.8125; right edge 1080 - 1035 = 45), the same as every other demo on the page. In P24 (ADV-2) these spanned x 0-1080 with a 0 dp inset.
- **Screen read:** 17. The teal `Few`, the grey disabled `Many` and the amber card line up with the other demos.

### P4: No crash. PASS

- `adb logcat -c` ran before the launch.
- At the end, `logcat -d AndroidRuntime:E *:S` is empty (0 bytes, capture `18-p4-logcat-androidruntime-e.log`, kept as negative evidence).
- `grep -ciE "FATAL EXCEPTION|ANR in io.github"` = 0, and the harness process was still alive (pid 8896) before uninstall.

## Device state restoration

- Density changed once (`wm density 480`) for P2 and was restored with **`wm density 450`, never `reset`**. Afterwards `wm density` prints `Physical density: 450` with no override line, and `display_density_forced` = `450`, the same as pre-flight.
- Harness uninstalled (`Success`); `pm list packages --user 0 | grep -ci yahirandroid` = 0.
- `wm size` override 1080x2316, `stay_on_while_plugged_in` 0 and animator scale 1 were never touched and are unchanged.
- Awake, keyguard not showing. The focus is back on `com.yahir.blackjacktrainer`, as before the run.
- `/sdcard/w.xml` (the helper's dump file) was deleted.
- The `logcat -c` cleared the device log buffer.
- **Host side:** mavenLocal `1.10.0` now holds the AAR built from `489848c` (md5 above). No `./gradlew --stop`; both Gradle runs used `--no-daemon`, one at a time.
- No src/repo edit, no commit, no tag, no push, no consumer touched. This evidence dir is untracked.

## Advisories (none blocks the gate)

1. **ADV-A: residual vertical half of P24 ADV-2.** The count-editor demo is the last item on the SegmentedOptionSelector page. Its warn ToneCard ends exactly at y 2181, flush on the nav bar, with no bottom spacing (screenshot 17), so the rounded bottom corners sit on the nav-bar edge. v2.7.1 fixed only the horizontal inset, as specified. A bottom content padding on the detail list is a cosmetic, Explorer-only candidate.
2. **ADV-B: the hoisted-state demo still uses the bare default.** `VoiceCommandFamilyScreen.kt` `ReceiptConfirmHandledByVariant` builds `rememberModalBottomSheetState()` without `skipPartiallyExpanded = true`. Its sheet is short, so nothing is visibly wrong today. However, it is the gallery's reference example for hoisting, while the v2.7.1 KDoc tells hoisting callers to pass `skipPartiallyExpanded = true`. Not exercised on device (out of scope).
3. **ADV-C: German group subtitle at 2-line capacity.** At both 384 dp and 360 dp, `3 Einträge · Rückgängig möglich` wraps onto both of its `maxLines = 2` lines, beside `Alle rückgängig machen`. A longer status word (for example `Teilweise erledigt` plus a two-digit count) or a narrower width could truncate. It is fine now; a long-string check for the `PartiallyResolved` German state was not run because the demo has no partly-resolved group.
4. **ADV-D: the German demo is German only in the header.** Member rows still read `Changed item A`, `0 minutes ago`, `Undo` and `Undone`. That is the fixture's choice, and the spec covered only the group words.
5. **Device note.** The S Pen Air command floating pencil bubble (system overlay, right edge at about y 1120-1250) shows in every screenshot. It is not app UI and covered nothing measured.

## Evidence files (this directory)

- `01`-`11`: P1 (xml + png each).
- `12`-`15`: P2 (xml + png each).
- `16`-`17`: P3 (xml + png each).
- `18-p4-logcat-androidruntime-e.log`: P4.
