---
status: failed
result: has_fail
gate: 1
evidence_schema: 3
uat_run: uatr-20261017T125116Z-fd0f39f7
phase: 26-receipt-card-stacked-snackbar-deck
source:
  - "26-ROADMAP success criteria"
device: SM-S908U (R5CT10XNKQN)
apk: io.github.ygaray.yahirandroidtasteharness (sha256 376f837d95f1)
run: 2026-10-17T12:51:16.852Z
command_guard_breaches: 0
command_guard_refusals: 4
---

<!-- Gate-1 self-UAT, phase 26 (consolidated per phase; evidence under the last plan, 26-05). -->

# Self-UAT Log — Phase 26 (Receipt-card stacked snackbar deck), run against plan 26-05

**Device:** SM-S908U tester, adb serial `R5CT10XNKQN` over USB (Android 15, API 35, density 450, font scale 1.0 at rest)   **Run:** 2026-10-17T12:51:16Z (`uat_run` uatr-20261017T125116Z-fd0f39f7, 51 signed captures, `uat-run verify` = verified)
**Build identity:** library HEAD `1f3237e` (`src/` clean); library AAR (mavenLocal `com.github.Ygaray:yahirandroidtaste:1.10.0`, deleted then republished from this tree with `clean publishReleasePublicationToMavenLocal`) md5 `0a99c53b341c65004221e762aa3d75e6`, confirmed to contain the code-review fixes (`FEEDBACK_FANNED_MIN_CAP`, `FEEDBACK_DEPTH_SCALE_MIN`); throwaway harness APK (scratchpad, never committed; `DeckActivity` consumer, `DeckA11yService` observer, `MainActivity` into the Explorer) md5 `828186cd8056c6b7c740e2d187a44b2f` (sha256 prefix `376f837d95f1`).
**Pre-flight:** device awake, no keyguard, `get-state` = device. Leases `device:tester` and `build` held via `yrig lease request` and released at the end. The wireless transport (`100.118.21.106:1496`) was never addressed; every adb call used the USB serial.
**Unit suite (rung 1):** `./gradlew testDebugUnitTest detekt apiCheck --rerun --no-build-cache --no-daemon` → BUILD SUCCESSFUL, **157 result classes, 1599 tests, 0 failures, 0 errors, 22 skipped (pre-existing `@Ignore`s, none in phase-26 classes)**; detekt and `metalavaCheckCompatibilityRelease` executed (not UP-TO-DATE). Ingested as `01-unit-detekt-apicheck-summary.txt`. `bash tools/verify-api-source-additive.sh v2.7.1` → `SEAMS: none`, `API SOURCE-ADDITIVE OK … removed_classes=0 removed_members=0 required_param_changes=0 signature_changes=0` (`02-api-source-additive-v271.txt`).
**Coverage/Nyquist (rung 2):** each criterion maps to named green suites (listed per criterion). I did not run `gsd-validate-phase`; `26-VALIDATION.md` is the plan-time draft.
**Seed/fixture integrity:** the library has no data layer. Fixtures are the compiled-in Explorer demos plus the harness `DeckActivity`, which drives only the library's PUBLIC API like a consumer (it compiled against the fresh AAR, itself a source-compat signal). Seeding is "publish the right HEAD": done, md5 above. Each criterion's cards were pushed through the harness buttons (Arrange) and the behavior under test was driven with real `adb input` taps and swipes (Act).
**Destructive check:** not applicable. The repo has no `src/androidTest` (`ls src` = `main`, `test`), so there is no `connectedAndroidTest` / wiping suite: no `destructive_check`, no `final_state_check`. No provider key or cloud call exists in this phase (shipped-config rule: nothing substituted).
**Device state changed and restored** (record `51-arrange-and-restore-record.txt`): `system font_scale` 1.0 → 2.0 → 1.0; secure `enabled_accessibility_services` null → harness service → null and `accessibility_enabled` 0 → 1 → 0; secure `accessibility_interactive_ui_timeout_ms` / `accessibility_non_interactive_ui_timeout_ms` null → 12000 / 7000 → null. `wm density` was never written (read back `Physical density: 450`, no override). All read back after restore. Harness APK uninstalled at the end.

## Criteria

### 1. SC1 — up to 3 Receipt cards, older cards tucked (~14dp, scale 0.94 / 0.88); a 4th show() evicts the oldest as Dismissed and a tracked WithUndo keeps its Undo Center entry; long German label at 360dp and font scale 2.0 wraps inside the card (FSTK-02)
result: passed
- **Rung:** 5 (card look, German wrap) plus 4 (geometry from bounds) and 3 (log, store state).
- **Target:** device (tester).
- **Expected:** at most 3 cards in the deck; card surface `surfaceContainerHighest`, 16dp corners, tone dot, message plus optional second line, Undo as a `primary` text action; strips of 14dp with scale 0.94 and 0.88; the 4th `show()` resolves the oldest `Dismissed` and a tracked entry stays Available in the store; at 360dp and font scale 2.0 the long German message and second line wrap inside the card and Undo and the chevron stay on it.
- **Arranged (seeded):** Burst4 (four Indefinite events 350 ms apart: targeted note with second line, tracked WithUndo, plain note, targeted note); for eviction, UndoI (tracked WithUndo) then Plain, Tgt, Plain. German: harness `GermanM` (the unit-test strings: "Eintrag Wocheneinkauf für das lange Wochenende wurde gelöscht" with second line "Liste mit zwölf Einträgen, zuletzt gestern Abend geändert", Undo "Rückgängig", open label "Eintrag öffnen") in a 360dp-wide host at `system font_scale` 2.0.
- **Did (drove):** tapped the harness buttons; read the structure tree and the screenshots.
- **Observed:**
  - Burst4: `RESULTS=B1:Dismissed` (the 4th arrival evicted the oldest), 3 cards remain. Bounds (`03`): front `[23,2000][1057,2158]` (1034 x 158 px = 56dp high), middle card top 1961 (width 972, ratio 0.940), back card top 1922 (width 910, ratio 0.880); both strips 39 px = 13.9dp. Screenshot read (`04`): lavender-grey rounded cards, primary-coloured dot and chevron on the targeted front card, older cards smaller and fainter behind it. Pixel probe: card body #E6E0E9 = M3 `surfaceContainerHighest`, dot #6750A4 = M3 `primary`, corner inset reaches 0 at about 40 px (14 to 16dp).
  - Fanned screenshot (`14`): Undo is bold `primary`-coloured text ("Undo"); the plain card has a grey dot and no chevron.
  - Tracked eviction (`06`, `07`): `RESULTS=UI:Dismissed` and `ENTRIES=Undo not/Available`: the evicted tracked card's store entry is still Available, the lambda never ran (`UNDORUNS=0`, log has no `undoAction ran`).
  - German at 360dp / font 2.0 (`08`, screenshot `09` read): card `[34,109][1047,1730]`; message `[136,132][426,1077]` and second line `[136,1077][426,1707]` wrap fully inside the card (last line "geändert" inside the card); Undo "Rückgängig" `[460,844][922,995]` and the chevron are inside the card's right part. The body column is only 290 px (103dp) wide because Undo takes its longest word: compound words break mid-word ("Wochen/einkauf", "Einträge/n") and the card is 576dp tall. That satisfies the criterion's wording but is a UX cost, see Notes.
  - Extreme string (`10`, the longer `German` button, 142 + 100 chars): the card fills the whole screen height and the text is clipped by it. Recorded for the owner; the criterion's own strings pass.
  - Units: `FeedbackDeckMetricsTest` 18, `FeedbackDeckLayoutTest` 15, `FeedbackStackHostLongLabelTest` 7, `FeedbackStackStateTest` 25 (eviction) all green.
- **Evidence:** `03`, `04`, `05`, `06`, `07`, `08`, `09`, `10`, `14`, `01`.

### 2. SC2 — tapping a targeted card's body calls onOpenTarget once and resolves Opened; a card with no target does nothing; the chevron shows only on targeted cards and no card shows an action label (FSTK-03)
result: passed
- **Rung:** 5 (chevron and label, screenshot read), 4 (tap routing), 3 (log counts).
- **Target:** device (tester).
- **Expected:** body tap on a targeted card: one `open` callback, result `Opened`; body tap on an untargeted card: nothing; chevron only on targeted cards; no action label text on any card.
- **Arranged (seeded):** Tgt, UndoI (tracked WithUndo with target), Plain pushed Indefinite; deck fanned by a peek tap.
- **Did (drove):** tapped the untargeted front card body; fanned the deck and tapped the Target card body once and the Undo-with-target card body twice in quick succession (double tap).
- **Observed:** untargeted front tap: `RESULTS=` empty, `N=0`, 3 cards still present (`11`). Fanned screenshot (`14`): chevron on "Target note" and "Undo note", none on "Plain note", and no "Open …" text on any card. Target tap: `open tg n=1`, `TG:Opened`. Double tap on the Undo-with-target card: one `open ui n=2`, one `UI:Opened` (log `18` has exactly two open lines for three taps, so the second tap of the double tap did not open again). Opened cards left the deck, Plain stayed. Units: `FeedbackStackHostTest` 12, `FeedbackStackHostDeckTest` 26 green.
- **Evidence:** `11`, `14`, `17`, `18`.

### 3. SC3 — tapping the peek of a collapsed deck fans it out into a column where every card's body and Undo can be tapped; it re-collapses after idle; a horizontal swipe on one card removes exactly that card as Dismissed, nothing undone, others stay (FSTK-04, FSTK-05)
result: failed
- **Rung:** 5 (screenshots) and 4 (tree) and 3 (log).
- **Target:** device (tester).
- **Expected:** a tap on the peek strip of a collapsed deck fans it, whatever is at the tapped spot; in the fanned column every card's body and Undo respond; re-collapse after idle; swipe removes exactly one card with `Dismissed` and never undoes.
- **Arranged (seeded):** Tgt/UndoI/Plain decks of 2 and 3 cards; Burst4.
- **Did (drove):** tapped the 14dp strip at several x positions; fanned and tapped bodies and Undo; swiped fanned cards, a collapsed front card and a collapsed peek strip; waited out the idle collapse.
- **Observed (passing legs):**
  - Peek tap away from the Undo label fans (`30`: cards at 1796 and 2000, "Undo note" and "Plain note" laid out as a column; `12`/`14` the same for 3 cards; screenshot `14` read: three separate full cards, oldest on top).
  - Fanned: Target and Undo-with-target bodies open once (SC2); Undo tap on a fanned card gives `UI:Undone`, `UNDORUNS=1`, other cards stay (`21`).
  - Idle: fanned at +1 s (`24`, two cards at text y 1869 and 2050), collapsed after 6 s with no touch (`25`, only the front card text at 2050).
  - Swipe in the fanned deck removes exactly one card: Target swiped → `TG:Dismissed`, not opened, Plain stays, `UNDORUNS` unchanged (`22`); a tracked WithUndo card swiped → `UI:Dismissed`, `UNDORUNS` stays 1, its entry stays `Available` (`23`).
  - Collapsed deck: a swipe across the peek strip neither dismisses nor fans (`31`, no log line, deck still collapsed); a swipe on the collapsed front card dismisses only it (`PL:Dismissed`, the Target card becomes the full front card, `32`, `33`).
- **Observed (FAILING leg):** in a collapsed deck whose back card has Undo, the back card's Undo button reaches up into the visible 14dp peek strip. Tree `26`: the strip is `[54,1961][1026,2000]` and a clickable Undo sliver `[356,1972][509,2000]` sits in it; the screenshot of the same state (`27`, read) shows an EMPTY strip with no visible Undo. Tapping that visually blank strip at (430,1985) did **not** fan the deck: log `29` shows `undoAction ran UI` and `result UI=Undone`, `UNDORUNS` 1 → 2, entry `Undone`, the card removed (`28`). The control tap on the same strip at x=800 fans (`30`). With 3 cards the deepest card's Undo pokes into its strip the same way (`03`: clickable `[356,1933][500,1961]`). A tap on the peek is the exact gesture the criterion names, and it silently runs a destructive Undo instead of fanning. The unit tests miss it because `tapStrip` taps the strip centre.
- **Evidence:** `12`, `13` (collapsed again by the time of this screenshot, see Notes), `14`, `15`, `19`, `21`, `22`, `23`, `24`, `25`, `26`, `27`, `28`, `29`, `30`, `31`, `32`, `33`.

### 4. SC4 — Short 4 s and Long 10 s through AccessibilityManager.calculateRecommendedTimeoutMillis (controls flagged for Undo or a target); the timer stops while a pointer is down or the deck is fanned and resumes the remaining time (FSTK-06)
result: passed
- **Rung:** 3 (timestamps in logcat, `SystemClock.elapsedRealtime`) plus 4 (tree presence) plus rung 1 (virtual-clock suites).
- **Target:** device (tester).
- **Expected:** Short card times out at 4 s, Long at 10 s; recommended-timeout scaling applies to cards with Undo or a target only; a pointer-down or a fanned deck pauses every timer, which then resumes the remaining time (floor 800 ms).
- **Arranged (seeded):** harness buttons ShortPlain, ShortPlain2, LongPlain, ShortTgt, ShortUndo; for scaling the secure settings `accessibility_interactive_ui_timeout_ms` = 12000 and `accessibility_non_interactive_ui_timeout_ms` = 7000, both deleted afterwards.
- **Did (drove):** pushed cards and let them run; held a finger (`input swipe` in place, 6 s) on a card; fanned a 2-card Short deck and kept it fanned with periodic card taps.
- **Observed:** plain Short `show SP t=…793` → `TimedOut t=…806` = **4013 ms**; plain Long = **10008 ms** (`34`). Pointer hold: show, finger down at about +1.2 s for 6 s, `TimedOut` after **10007 ms**, i.e. after the release and not at +4 s (`35`). Fan pause: two Short cards, fanned at about +1.4 s; at about +6 s both cards were still present and fanned (`36`, text y 1869 and 2050); they timed out at **16.1 s** after show (`37`), about 4 s after the last touch plus the remaining 2.8 s, not at 4 s. Scaling (`38`, settings above): plain Short = **7.01 s** (non-interactive setting applies to text), Short with target = **12.01 s** and Short with Undo = **12.01 s** (interactive setting applies to controls-flagged cards). Explorer demo (`45` to `47`): "Short note" ended `Last result: TimedOut` after about 6 s; "Long note with Undo" was still showing at about 5.5 s and gone with `TimedOut` after about 12 s. Units: `FeedbackStackPauseTest` 18, `FeedbackStackStateTimeoutTest` 21 green.
- **Evidence:** `34`, `35`, `36`, `37`, `38`, `45`, `46`, `47`.

### 5. SC5 — each card body is one merged Role.Button node with onClickLabel = openLabel and a Polite live region; Undo is a separate focusable child and a custom action; a Dismiss custom action and `dismiss` semantics remove the card like a swipe; deck cards stay in the tree newest first, collapsed or fanned (A11Y-01)
result: passed
- **Rung:** 4 (the real framework AccessibilityNodeInfo tree from an enabled AccessibilityService, plus performed actions).
- **Target:** device (tester). TalkBack itself is not run here; the criterion's Notes put that in Phase 28.
- **Expected:** per card: focusable node, `Role.Button` (targeted cards), click action labelled with `openLabel`, live region Polite, ACTION_DISMISS labelled Dismiss, custom actions Undo then Dismiss; Undo also a separate focusable child; cards ordered newest first in both states; every action resolves like touch.
- **Arranged (seeded):** harness `DeckA11yService` (a real AccessibilityService, enabled for this criterion only, then removed) dumping the window's tree to logcat and performing actions on request; Burst4 and Tgt/Plain/UndoI pushes.
- **Did (drove):** dumped the collapsed and fanned trees; performed ACTION_CLICK on the front card, ACTION_DISMISS on a card, the custom `Undo` action on a card with Undo, the custom `Dismiss` action on a card.
- **Observed:** card nodes (`48`, `49`): `a11yFocusable=true`, `live=1` (polite), actions `Open b4` (the openLabel, only on targeted cards), `1048576:Dismiss` (ACTION_DISMISS), custom `Dismiss`, and `Undo` before `Dismiss` on the card with Undo (`[…|16:Open b2|1048576:Dismiss|2131230721:Undo|2131230722:Dismiss]`); a plain card has no click action and no Button node. A node of class `android.widget.Button` with the card's bounds exists only under targeted cards (the `Role.Button` projection). The Undo button is a separate focusable child (`focusable=true a11yFocusable=true`, e.g. `[356,1933][500,1961]`). Card order: `traversalBefore` chains give B4 → B3 → B2 for the cards in both states (newest first). Actions (`50`): click → `open b4 n=1`, `B4=Opened`; dismiss → `B3=Dismissed`; custom Undo → `undoAction ran B2`, `B2=Undone`; custom Dismiss → `TG=Dismissed`; dismiss on a tracked Undo card → `UI=Dismissed` with no `undoAction ran`. Units: `FeedbackStackHostSemanticsTest` 16, `FeedbackStackSourceContractTest` 15 green. Falsification attempt on the order: the same chains put the OLDEST card's Undo button OUTSIDE its card (fanned: Undo(B2) → B4 → B3 → B2; collapsed: B4 → Undo(B2) → B3 → B2). The criterion speaks of the cards, which are in order, so this does not fail it, but it is a reading-order defect to confirm with TalkBack, see Notes.
- **Evidence:** `48`, `49`, `50`.

## Summary

total: 5
passed: 4
partial: 0
failed: 1
infra: 0

## Notes / anomalies (for the Gate-2 reviewer)

- **Arrange vs Act boundary.** Nothing was seeded into a pass: cards were pushed through harness buttons, and every behaviour under test (taps, swipes, holds, peek taps, accessibility actions) was driven on the tester. The harness only adds read-outs (results, open count, undo-run count, store entries, timestamps); it has no logic that can produce a result on its own.
- **Evidence clutter, honestly labelled.** `13` (screenshot named "peek-tap-fans-deck") shows the deck already collapsed again, because a capture takes longer than the 4 s idle collapse; `14` is the real fanned screenshot (finger held to stop the collapse). `16` and `20` are failed attempts (the deck had collapsed before my taps, nothing happened); the real captures are `17`/`18` and `21`. A first signed run (`uatr-20261017T124745Z-9d9776fb`) was abandoned when the APK was rebuilt to add the unit-test German button; its un-committed run dir was deleted and no verdict comes from it.
- **SC3 is the only failure** (genuine behaviour defect, not INFRA). See the routing section.
- **Ghosting in the peek strips (cosmetic).** The back cards are 0.9 and 0.75 alpha, so the deepest card's text and its Undo label show through the middle card's strip ("Burst two  Undo  ›" faintly readable in the second strip, `04`). It is the locked mock's alpha, so I did not fail it, but it is the same area as the SC3 defect.
- **Long German at font scale 2.0 (UX).** The criterion passes as worded, but the Undo column takes its longest word ("Rückgängig", 462 px) and leaves the body 103dp, so compounds break mid-word and the card is 576dp tall; with the harness's 142-character message it fills the screen. FA-9 acknowledges a single over-wide word; this shows the Undo width rule makes the body narrower than half the card at font 2.0. Owner call, not a Gate-1 failure.
- **Keyboard watch items (WR-01 / FA-7), both in an `imePadding` host.** Keyboard open, then fan (`39`, `40`, screenshot read): the fanned deck stays above the keyboard, is capped (about 212 px in this small host), shows the newest card at the bottom with a sliver of the next above; not zero-height and not behind the keyboard. Fan, then keyboard opens (`41`, `42`, screenshot read): the deck stays visible above the keyboard at the capped height, but the visible card is the OLDEST ("Burst two"), the newer cards are scrolled out below until the user scrolls; the "newest in view" scroll only re-runs when the fan flips or a card arrives, not when the cap shrinks. All cards stay reachable by scrolling, so I did not fail SC3; this is the residual case the review flagged.
- **FA-1 (14dp strip).** A tap inside the 14dp strip fans (measured strip 39 px); a swipe across it does nothing. The only trouble on the strip is the Undo sliver in SC3.
- **Accessibility reading order (for Phase 28 / Gate-2).** In the framework's traversal data the oldest card's separate Undo button is read ahead of the newest card when fanned (and between the newest card and the middle card when collapsed), detached from its own card. Cards themselves are newest first. TalkBack was not run; confirm the order and consider whether the separate Undo child is needed given the Undo custom action on the card.
- **Explorer demo buttons.** `Burst x4` (`Last result: Dismissed`, 3 cards), `Short note` (TimedOut) and `Long note with Undo` (10 s) all work on the tester (`43` to `47`).
- **Device labelling.** The dispatch called `100.118.21.106:1496` the personal phone; `~/.claude/context/devices/test-android.md` lists that address as the tester's wireless transport, and `adb devices` showed both it and the USB serial as the same model. I never addressed the wireless entry; worth correcting at the source (brief or context file), not a memory conflict.
- **Command guard.** Four Bash calls were refused fail-closed before running, all `uninspectable` (an inline `python3 -c`, a script path that did not exist yet, a shell `for` loop over `adb … settings get`, and a mistyped `cd` that pointed `./gradlew` at a directory without one); none was forbidden and none ran. Each was re-issued in a literal form. `command_guard_refusals: 4`, `command_guard_breaches: 0`.
- **Residual device differences:** none. The Samsung pencil overlay at the right edge of the screenshots is a pre-existing device overlay, not the app.

## Findings routed to gap-closure (if any)

- SC3 (FSTK-04): **a peek tap that lands on a back card's Undo button runs Undo instead of fanning the deck.** Root cause: `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackCard.kt:239` passes `showUndo = item.hasUndo && !spent` to `FeedbackCardRow` without regard to `collapsedBack`, so the clickable `DynamicActionButton` (`FeedbackCard.kt:305`) of a back card stays hit-testable; `FeedbackDeck.kt` places a back card's top 14dp (the peek) above the next card, and the Undo button's top part falls inside it. The button consumes the down and up before the card root's single `detectTapGestures` owner (`FeedbackCard.kt:190`, the only place that calls `onPeekTap`) can see a tap. Falsifying evidence: `26`/`27` (blank strip, Undo sliver `[356,1972][509,2000]`), `28`/`29` (`undoAction ran UI`, `UI=Undone`, `UNDORUNS` 1 → 2, no fan), control `30` (tap at x=800 fans). The suite is green because `FeedbackStackHostDeckTest.tapStrip` taps the strip centre, never over an Undo. Suggested direction for gap-closure (its call): make the Undo of a `collapsedBack` card non-interactive to touch (its tap should fall through to the peek-tap-fans path) while keeping the Undo custom action and the card's merged semantics, add a regression test that taps the strip over the Undo's x-range in a 2- and a 3-card deck and asserts the deck fans and no Undo runs, and keep the strip tap behaviour from SC3. Route: `/gsd-plan-phase 26 --gaps` then `/gsd-execute-phase 26 --gaps-only` (cycle 1/3; escalate to the human at the cap).
- Not routed (not criterion failures, forwarded as notes): the oldest card's Undo child reading order (Phase 28 sweep), keyboard-opens-while-fanned showing the oldest card, the German font-2.0 body width, strip ghosting.

## Verdict

4 of 5 criteria PASS (SC1, SC2, SC4, SC5); **SC3 FAILS** on the real tester against HEAD `1f3237e` (library AAR md5 `0a99c53b…`): tapping the peek over a back card's Undo runs Undo instead of fanning. Gate-1 is not satisfied and the phase is BLOCKED for gap-closure; no Gate-2 entry is registered until a re-run passes (a failing phase is not registered as a passing pending item).
