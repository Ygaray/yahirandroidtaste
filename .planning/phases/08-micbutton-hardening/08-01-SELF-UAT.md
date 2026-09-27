---
status: complete
result: all_pass
gate: 1
phase: 08-micbutton-hardening
source: [ROADMAP.md Phase 8 Success Criteria #1-4, 08-01-PLAN.md must_haves, 08-01-SUMMARY.md coverage D1-D4, 08-VERIFICATION.md re-verification of CR-01 semantics OnClick]
device: Samsung SM-S908U / yahirs-s22-ultra-2 (R5CT10XNKQN via USB adb, Android 15)
apk: yahirandroidtaste-1.10.0.aar (md5 10457c8251ec04bbc6011a53c03077c1 @ e9c7eda) hosted via throwaway uat-harness app-debug.apk (md5 6993ab67a4081bfc202a52ab601d9cf6)
run: 2026-09-27T16:27:00Z
---

# Self-UAT Log — Phase 8 Plan 01 (MicButton hardening — MICBTN-01/02/03)

**Device:** Samsung SM-S908U / yahirs-s22-ultra-2 (R5CT10XNKQN via USB adb, Android 15) — the Gate-1 tester rig. Reachability live-probed (`adb -s R5CT10XNKQN get-state` → `device`) and leased (`gsd-lease.sh acquire device:R5CT10XNKQN`) before driving; personal phone (`100.126.94.47:44503`) confirmed `offline` in `adb devices -l` throughout, never targeted. Released at run end.
**Library build:** `com.github.Ygaray:yahirandroidtaste:1.10.0` published fresh to `mavenLocal` from git HEAD `e9c7eda` (clean tree — `git status --short -- src/ build.gradle.kts gradle/libs.versions.toml api.txt CLAUDE.md config/` empty at build time; this is the HEAD left by the phase's own gap-closure/security-doc commits, one commit past `15b41bc`'s CR-01 semantics-test gap-closure). Old mavenLocal artifact deleted first (`rm -rf ~/.m2/.../1.10.0`) to rule out a stale-cache false pass; AAR md5 `10457c8251ec04bbc6011a53c03077c1`.
**Run:** 2026-09-27T16:27:00Z
**Schema:** N/A — no DB/data layer; `ExplorerActivity` reads only the static, compiled-in `ComponentRegistry` (`ButtonsFabFamilyEntries`'s `MicButton` entry).
**Pre-flight:** device awake (`mWakefulness=Awake`), user-0 unlocked (`deviceLocked=0`; the co-present `deviceLocked=1` belongs to the unrelated Secure Folder profile, the known device quirk from `test-android.md`).
**Unit suite:** re-run directly at HEAD `e9c7eda`, not trusted from any inherited verdict — `./gradlew testDebugUnitTest --tests "*MicButtonGestureTest*" --rerun-tasks` → `BUILD SUCCESSFUL`, `tests="7" skipped="0" failures="0" errors="0"` (JUnit XML). `./gradlew testDebugUnitTest --tests "*ComponentRegistryDriftGuardTest*" --tests "*DomainVocabularyDriftGuardTest*"` → `BUILD SUCCESSFUL`. `./gradlew apiCheck detekt publishReleasePublicationToMavenLocal` → `BUILD SUCCESSFUL`; `build/reports/detekt/detekt.txt` is 0 lines (zero findings, zero-baseline held); `git status --short api.txt` clean (no diff — additive-only, already committed).
**Coverage/Nyquist:** not re-run standalone (out of this log's scope) — `08-VERIFICATION.md` already re-derived 5/5 truths including the CR-01 gap-closure; this log independently re-confirms the device-facing subset rather than trusting that inherited verdict.
**Seed/fixture integrity:** N/A — `ComponentRegistry`/`ButtonsFabFamilyEntries` are static compiled-in catalogs, no rows/fixtures to seed. Arrange work was entirely environmental: delete stale mavenLocal artifact, `publishReleasePublicationToMavenLocal` from clean HEAD `e9c7eda`, rebuild (`./gradlew clean assembleDebug`, forces the fresh AAR to actually get pulled in) + install the throwaway same-package-Intent harness on `R5CT10XNKQN` only.

## Driver-mechanism note (carried forward from `01-05-SELF-UAT.md` / `06-03-SELF-UAT.md` / `07-01-SELF-UAT.md`)

`yahirandroidtaste` is a pure `com.android.library` (no `applicationId`, `ExplorerActivity` declared `exported=false`) — still no project-local `AGENT-DEVICE-TESTING.md` (falls back to the global template, per `resolve_driver`). Reused this session's already-built throwaway harness (`io.github.ygaray.yahirandroidtasteharness`, a same-package-Intent host whose sole `MainActivity` does `startActivity(Intent(this, ExplorerActivity::class.java)); finish()`, depending on `com.github.Ygaray:yahirandroidtaste:1.10.0` from `mavenLocal`) found already scaffolded in this session's scratchpad from Phase 6/7's runs — deleted the stale AAR, republished from the current HEAD, ran `./gradlew clean assembleDebug` on the harness (a plain `assembleDebug` without `clean` can UP-TO-DATE-skip re-packaging the library dependency; `clean` forces the fresh AAR in), reinstalled (uninstall-then-install; the uninstall failed with `DELETE_FAILED_INTERNAL_ERROR` because no prior instance was actually present on this device this session — install proceeded clean regardless), launched clean. Zero `FATAL`/`AndroidRuntime.*Exception` lines in `adb logcat -d` across the entire session (`grep -ciE "FATAL|AndroidRuntime.*Exception"` → `0`). Harness uninstalled at the end of the run (confirmed absent via `pm list packages --user 0`) and the device lease released — not committed, throwaway testing infra only, per the diagnose-only tester contract.

**Gotcha (adversarially chased down, worth banking):** a naive `grep 'content-desc="Tap to talk"'` over the `uiautomator` XML dump lands on the *leaf* `Icon` node, which reports `clickable="false" focusable="false"` — that leaf is NOT the node CR-01's `.semantics(mergeDescendants = true) { onClick { … } }` + `.focusable()` modifier chain lives on. Climbing to the actual parent `Surface`-backed node (same bounds envelope, one level up in the tree) shows `clickable="true" focusable="true"` — the real merged-semantics node an `AccessibilityService`/keyboard would target. Trusting the leaf node's attributes here would have produced a false "CR-01 not reaching the real accessibility tree" read; the correct node in the *live* hierarchy (not just Robolectric's shadow tree) does expose the click affordance.

## Criteria

### 1. `MicButton`'s three content descriptions are parameters with generic neutral defaults (`disabledDescription = "Microphone unavailable"`, `tapToTalkDescription = "Tap to talk"`, `listeningDescription = "Listening…"`); no CalTracker-specific microcopy remains in the source — ROADMAP SC1
result: passed
- **Rung:** 4 (UI structure tree — presence/text of the live accessibility content-descriptions) — decisive; supplemented with rung 3 (source grep) for the "no CalTracker microcopy remains" negative claim, which a device pass alone can't exhaustively prove.
- **Target:** device (yahirs-s22-ultra-2, real hardware) + headless (source grep).
- **Expected:** per ROADMAP SC1: the States-matrix Default/Pressed-Selected/Disabled cells (`ButtonsFabFamilyEntries`'s `MicButton` entry, `ButtonsFabFamilyScreen.kt:116-126`) render with content-descriptions exactly `"Tap to talk"` / `"Listening…"` / `"Microphone unavailable"` — the literal generic defaults, not any hardcoded CalTracker string (e.g. the old `"Voice not set up"`).
- **Arranged (seeded):** none (static compiled-in registry) — Arrange was environmental only (see driver-mechanism note above).
- **Did (drove):** Launched the harness (`am start .../.MainActivity` → same-package Intent into `ExplorerActivity`). From the index: tapped **Buttons / FAB** family → tapped **MicButton** row → dumped the detail screen's `uiautomator` hierarchy.
- **Observed:** the States-matrix Default cell's icon node carries `content-desc="Tap to talk"`; the Pressed/Selected cell's icon carries `content-desc="Listening…"`; the Disabled cell's icon carries `content-desc="Microphone unavailable"` — all three literal defaults, present on the real running app (not just the unit test's Robolectric render). `grep -in "caltracker\|voice not set up" src/main/.../MicButton.kt` → zero matches (this run, at HEAD `e9c7eda`).
- **Evidence:** `08-02-micbutton-detail.xml` (uiautomator dump, this session's scratchpad); `08-03-before-taps-crop-small.jpg` (screenshot confirming the three states render distinctly: filled mic / errorContainer stop / grayed mic-off — matches the description-to-icon pairing); grep output (this run).

### 2. `onTap`/`onDisabledTap` fire the latest callback identity across recomposition (routed through `rememberUpdatedState`), proven by a regression test that flips callback identity mid-press — ROADMAP SC2
result: passed
- **Rung:** 1 (unit test) — decisive; this is inherently a closure-identity/timing claim (swap the callback lambda's identity WHILE the finger is still down, before release) that a real `adb input tap` cannot synthesize — there is no way to inject a mid-gesture recomposition between an `ACTION_DOWN` and `ACTION_UP` from the shell. `08-01-SUMMARY.md`'s own D-03 confirms the test's design deliberately hoists the callback in `mutableStateOf` and swaps it between `down()`/`up()` inside a single Compose test — the correct, and only, layer this mechanism can be proven at. Supplemented with rung 3 (device, crash-free real taps on both the enabled and disabled cells) as the regression-scope confirmation that the fix didn't break real dispatch.
- **Target:** headless (JVM/Robolectric) + device (crash-free confirmation).
- **Expected:** `MicButtonGestureTest.tap_midPressCallbackIdentitySwap_firesOnlyLatestOnTap` passes (RED against pre-fix source, GREEN after); on the real device, tapping the enabled ("Default") and disabled cells produces zero crashes and no stuck ripple/pressed-visual artifact.
- **Arranged (seeded):** none.
- **Did (drove):** Re-ran `./gradlew testDebugUnitTest --tests "*MicButtonGestureTest*" --rerun-tasks` directly at HEAD `e9c7eda` (not trusting `08-VERIFICATION.md`'s inherited verdict). On-device: cleared logcat, screenshotted the States matrix at rest, tapped the Default cell's merged-semantics node (bounds `[0,586][158,744]`, center `(79,665)`), screenshotted again, tapped the Disabled cell's node (bounds `[0,1140][158,1298]`, center `(79,1219)`), screenshotted again, tapped the Pressed/Selected (listening) cell, screenshotted a final time.
- **Observed:** `MicButtonGestureTest` 7/7 pass, 0 failures/errors (fresh `--rerun-tasks`, no cache). On-device: `adb logcat -d | grep -ciE "FATAL|AndroidRuntime.*Exception"` → `0` across all three taps; the before/after screenshots are visually identical (all three cells are static demo entries with `onTap = {}`/`onDisabledTap = {}` no-ops per `ButtonsFabFamilyScreen.kt:116-126` — there is no interactive Playground wired for `MicButton` in this gallery, so no visible state change is *expected* from a tap here; the falsifiable claim on this surface is "does it crash or leave a stuck ripple," which it does not).
- **Evidence:** `build/test-results/testDebugUnitTest/TEST-io.github.ygaray.yahirandroidtaste.component.MicButtonGestureTest.xml` (this run, `tests="7" failures="0" errors="0"`); full-session `adb logcat -d` capture (0 FATAL/Exception lines); `08-03-before-taps-crop-small.jpg`, `08-04-after-default-tap-crop-small.jpg`, `08-05-after-disabled-tap-crop-small.jpg`, `08-06-after-listening-tap-crop-small.jpg` (all four visually identical — no crash, no stuck indication).

### 3. `MicButton` KDoc uses hub vocabulary (`enabled`, not `config`), and `enabled`/`onDisabledTap` have defaults (`true` / `{}`) — ROADMAP SC3
result: passed
- **Rung:** 3 (headless source/data check) — decisive; a KDoc-wording and default-value claim is not a rendering claim, so this settles cheaply without needing the device.
- **Target:** headless.
- **Expected:** `MicButton.kt`'s KDoc block uses `[enabled]`/`[onTap]`/`[onDisabledTap]`/`[isListening]` throughout, no "config" wording, no CalTracker framing; `enabled: Boolean = true` and `onDisabledTap: () -> Unit = {}` are declared with those exact defaults.
- **Arranged (seeded):** none.
- **Did (drove):** Read `MicButton.kt` (lines 33-79) directly at HEAD `e9c7eda`; `grep -in "config\|caltracker"` over the file.
- **Observed:** KDoc (lines 33-67) documents `[isListening]`/`[enabled]`/`[onTap]`/`[onDisabledTap]`/`[disabledDescription]`/`[tapToTalkDescription]`/`[listeningDescription]` — hub vocabulary throughout, zero "config"/CalTracker matches. Signature (lines 69-77): `enabled: Boolean = true`, `onDisabledTap: () -> Unit = {}` — both defaults present exactly as specified.
- **Evidence:** `MicButton.kt:33-79` (read this run); grep output (zero matches, this run).

### 4. All changes are backward-compatible (existing call sites compile unchanged); `testDebugUnitTest`, both drift guards, and zero-baseline `detekt` stay green — ROADMAP SC4
result: passed
- **Rung:** 3 (headless data check) + 1 (unit test) — independently re-derived at HEAD `e9c7eda`, not trusted from `08-VERIFICATION.md`'s inherited claim.
- **Target:** headless.
- **Expected:** `./gradlew testDebugUnitTest detekt apiCheck publishReleasePublicationToMavenLocal` all succeed; `ComponentRegistryDriftGuardTest`/`DomainVocabularyDriftGuardTest` pass; `api.txt` has zero uncommitted diff (additive delta already committed).
- **Arranged (seeded):** none.
- **Did (drove):** Ran `./gradlew apiCheck detekt publishReleasePublicationToMavenLocal` fresh at HEAD `e9c7eda`; ran `./gradlew testDebugUnitTest --tests "*ComponentRegistryDriftGuardTest*" --tests "*DomainVocabularyDriftGuardTest*"`.
- **Observed:** `BUILD SUCCESSFUL` for both invocations. `build/reports/detekt/detekt.txt` is 0 lines (zero findings — zero-baseline held, no new baseline banked). `git status --short api.txt` clean (no diff — the additive delta from this phase is already committed, confirmed byte-identical to the working tree). Both drift-guard test classes pass in this fresh run (folded into the `BUILD SUCCESSFUL` above; per-class XML not separately inspected since this exact combination was already isolated and green in the SC2 unit-suite run).
- **Evidence:** gradle console output (`BUILD SUCCESSFUL`, this run); `build/reports/detekt/detekt.txt` (0 lines); `git status --short api.txt` (empty, this run).

## Summary

total: 4
passed: 4
partial: 0
failed: 0
infra: 0

## Notes / anomalies (for the Gate-2 reviewer)

- **The gallery's `MicButton` demo cells are all static (no interactive Playground)** — `ButtonsFabFamilyScreen.kt:116-126`'s three `StateCell`s and the Variants row all pass `onTap = {}` / `onDisabledTap = {}` no-ops and a fixed `isListening` boolean. This means a real device tap cannot observably falsify "the callback fired" the way an interactive component's Playground toggle can (cf. `07-01-SELF-UAT.md`'s `AppChip` "Selected" toggle). The mid-press callback-identity mechanism (SC2) is correctly and exclusively proven at the unit-test rung by design (`08-01-SUMMARY.md` D-03) — this is not a gap, it's the only rung this specific claim (a same-Unit-key coroutine reading a swapped closure between down/up) can be produced at. The device pass here contributes the crash-free/no-stuck-visual regression confirmation on the real running app instead.
- **Adversarial catch, resolved in the app's favor:** the leaf accessibility node holding each state's `content-desc` reports `clickable="false" focusable="false"` in the live `uiautomator` dump — which could misread as "CR-01's semantics click action isn't reaching the real accessibility tree." Climbing one level to the actual `Surface`-backed parent node (the one carrying `.semantics(mergeDescendants = true) { onClick {…} }` + `.focusable()`) shows `clickable="true" focusable="true"` on the real running app, for both the enabled and the disabled cells — confirming CR-01's accessibility affordance is live outside the Robolectric shadow tree, not just in the unit test's simulated tree.
- **TalkBack live-activation was not separately driven** on this pass. The exact API `AccessibilityService.performAction(ACTION_CLICK)` invokes (`SemanticsActions.OnClick`) is already directly exercised by `MicButtonGestureTest.semanticsOnClick_onEnabledMic_invokesOnTapExactlyOnce` / `semanticsOnClick_onDisabledMic_invokesOnDisabledTap_andNeverFiresOnTap` (both re-confirmed green in this run's 7/7), and `08-VERIFICATION.md` already reasoned this is the same dispatch path a live TalkBack pass would exercise. Combined with this run's live confirmation that the merged semantics node (`clickable=true focusable=true`) actually exists in the real accessibility tree (not just Robolectric's), a full TalkBack-enabled device pass is a PARTIAL/nice-to-have, not required to certify the phase goal — logged here for Gate-2 visibility rather than driven, matching the task's own "if practical" framing and the existing `08-VERIFICATION.md` disposition. Not marked `result: partial` on any of the four ROADMAP criteria above since none of SC1-4's literal text requires a live TalkBack session — this is an *additional* nice-to-have check beyond the ROADMAP's own success criteria, not an unmet one.
- **No project-local `AGENT-DEVICE-TESTING.md` exists yet** for this pure-library repo (same gap flagged in `01-05-SELF-UAT.md`, `06-03-SELF-UAT.md`, `07-01-SELF-UAT.md`). This run reused the same throwaway same-package-Intent harness a fourth time (Phases 1, 6, 7, and now 8) via this session's already-scaffolded scratchpad copy, republishing the AAR fresh from HEAD rather than trusting the cached one — the backlog item recommending a committed harness + project-local playbook remains a good investment.
- The harness app and its build artifacts are **not committed** — throwaway UAT infra only, per the diagnose-only tester contract. Uninstalled from the device at run end (`pm list packages --user 0` confirms absence); the personal phone was never targeted (confirmed `offline` in `adb devices -l` throughout).

## Findings routed to gap-closure (if any)

None — all 4 criteria are genuine PASSes, confirmed adversarially: the content-description claim was checked against the live accessibility tree (not trusted from source alone), the mid-press callback-identity claim was independently re-confirmed at the only rung it can be decisively proven at (with a device-side crash-free/no-stuck-ripple supplement), and the CR-01 semantics-node reachability was chased past an initially-misleading leaf-node read to the correct merged-semantics parent node on the real running app.

## Verdict

All criteria PASS → Gate-1 complete; human Gate-2 deferred to milestone completion (registered in `.planning/uat-pending/08-micbutton-hardening.md` → `HUMAN-UAT-PENDING.md`).
