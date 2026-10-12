---
status: complete
result: all_pass
gate: 1
evidence_schema: 3
uat_run: uatr-20261012T223815Z-1f8b52e0
device: SM-S908U (R5CT10XNKQN)
apk: io.github.ygaray.yahirandroidtasteharness (sha256 8599ce51950a)
run: 2026-10-12T22:38:15.832Z
phase: 23-providerkeycard-lifecycle-undo-center-groups
source:
  - "ROADMAP Phase 23 success criteria SC1-SC4"
  - "23-02-SUMMARY Gate-1 handoff probe cases 1-20"
supersedes_uat_run: uatr-20261012T172501Z-65a2a2aa
---

# Self-UAT Log - Phase 23 Plan 02 (ProviderKeyCard lifecycle and Undo Center groups; phase-level Gate 1), fresh re-run 2026-10-12

**Why this run exists:** director ruling INC-2026-10-12-02. The prior signed run `uatr-20261012T172501Z-65a2a2aa` contained three void `ui` captures (79-byte files holding only `cat: ... No such file or directory`) that `uat-run` had recorded without error. This log replaces that run's verdict with a FRESH signed run against HEAD; every probe was re-driven on the device. The prior run dir stays committed (history), the prior log is in git at `888ccdb`. See "Supersession" at the end.

**Device:** tester `yahirs-s22-ultra-2`, SM-S908U, USB serial `R5CT10XNKQN`, Android 15 (API 35), 1080x2316 override at 450 dpi (1 dp = 2.8125 px, 384 dp wide). Every adb command used `-s R5CT10XNKQN`. `100.118.21.106:1496` also listed in `adb devices`; it was never addressed.
**Driver playbook:** `AGENT-DEVICE-TESTING.md` (project root; same-package-Intent harness) plus the 23-02-SUMMARY "Gate-1 handoff" (a)-(g). Same probe list and driver approach as the prior log.
**Build identity:** HEAD `deae923` (Phase 24 commits on top of the Phase 23 code; `git diff --name-only 8d54149 HEAD -- src/main` touches no ProviderKeyCard, UndoCenter, UndoGroup, UndoHistory or model file, only explorer/registry files). Library AAR `com.github.Ygaray:yahirandroidtaste:1.10.0` republished fresh (old mavenLocal `1.10.0` dir and `maven-metadata-local.xml` deleted first; `./gradlew clean publishReleasePublicationToMavenLocal --no-daemon -q --offline`), md5 `a695b8258e9e45c39ba9dad4b3cf359a`; `classes.jar` contains `ProviderKeyDefaults`, `ProviderKeyActions`, `ProviderKeyLabels`, `SavedKeyUiModel`, `UndoGroupRowUiModel`, `UndoCenterScreenKt`. Throwaway harness (session scratchpad `h23/`, outside the repo, never committed) rebuilt with `clean assembleDebug` against that AAR: package `io.github.ygaray.yahirandroidtasteharness`, on-device md5 `803ad6c80ffb98fa7baaeb115f9423ee` (read back with `adb shell md5sum`; the run's `apk` sha256 prefix `8599ce51950a` is that file). Harness content: a Java `MainActivity` that same-package-Intents into `ExplorerActivity`, and an exported Kotlin `ProbeActivity` with (i) a stateful `ProviderKeyCard` host (selected provider, typed text, `editingSavedKey`, saved-key map, fire counters as visible text) and (ii) `UndoCenterScreen` on a REAL `UndoHistoryStore` (`openGroup`, grouped and ungrouped `append`, groups mapped from `group`/`groupLabel`/`groupStatus`, `onUndoGroup` wired to `attemptUndoGroup`, counters and the last `UndoGroupResult` as visible text).
**Baseline for the default A/B:** the real v2.5.1 build. Scratchpad `v251/` source re-compared to `git archive v2.5.1 src api.txt` (`diff -rq` and `diff -q` clean). Its AAR is mavenLocal `1.10.0-v251`, md5 `033247da6e35f29a83063ff7c7f11197`, `classes.jar` has 0 of the Phase 23 classes. Baseline harness `...harnessb` rebuilt with `clean assembleDebug`: md5 `25629d72fe151618a9f884d56629b0f3` on the device. Both harnesses compile the byte-identical `Defaults.kt` (v2.5.1 API only).
**Run:** `uat-run` `uatr-20261012T223815Z-1f8b52e0` (`--plan 02`, prefix `23-02`), 89 captures (61 ui, 27 screen, 1 empty-allowed log), finished, `uat-run verify --run` = `verdict: verified, ok: true`. Evidence dir: `.planning/phases/23-providerkeycard-lifecycle-undo-center-groups/23-02-uat-evidence/uatr-20261012T223815Z-1f8b52e0/`.
**Capture audit (done by me after finish):** no ui or screen file under 200 bytes (smallest ui dump 3946 bytes, the open dropdown popup); 0 files contain `No such file`; all 61 xml files start with `<hierarchy` and end with `</hierarchy>`; all 27 png files decode, are 1080x2316 and are not single-colour; the `package` attribute is the harness in 54 dumps and the baseline harness in the 7 v2.5.1 dumps (no launcher dump). The only file under 200 bytes is `89-logcat-fatal-check-all-probe-launches.log` (63 bytes), the signed empty-logcat negative evidence.
**Pre-flight:** `mWakefulness=Awake`, `isKeyguardShowing=false`, launcher focused, `stay_on_while_plugged_in` `15`, animator scale `1`, no `~/.gsd-core/leases` entry, `pm list packages --user 0 | grep -ci yahirandroid` = 0 before install.
**Unit suite (fresh, uncached, rung 1):** `./gradlew cleanTestDebugUnitTest testDebugUnitTest detekt apiCheck --rerun-tasks --offline --no-daemon --console=plain` in the main checkout -> BUILD SUCCESSFUL in 2m 20s (44 tasks, 43 executed). 135 JUnit XML files, 0 older than the pre-run marker, **tests=1315 skipped=22 failures=0 errors=0** (up from 1258 because Phase 24 added tests). Phase classes (tests/failures/errors): ProviderKeyCardDefaultParityTest 6/0/0, ProviderKeyCardLifecycleTest 25/0/0, ProviderKeyCardPerProviderTest 5/0/0, ProviderKeyCardRawKeySemanticsTest 8/0/0, ProviderKeyCardLongLabelTest 5/0/0, ProviderKeyCardSourceCompatV251Test 5/0/0, ProviderKeyCardSourceContractTest 5/0/0, ProviderKeyCardTest 8/0/0 (protected), ProviderKeyModelsTest 5/0/0, UndoCenterRowsTest 13/0/0, UndoCenterScreenDefaultParityTest 5/0/0, UndoCenterScreenGroupsTest 22/0/0, UndoCenterScreenLongLabelTest 4/0/0, UndoCenterScreenSourceCompatV251Test 2/0/0, UndoCenterScreenSourceContractTest 7/0/0, UndoCenterScreenTest 4/0/0 (protected), UndoHistoryStoreTest 25/0/0 (protected), V251CallSiteCanaryTest 12/0/0, ComponentRegistryDriftGuardTest 1/0/0. detekt and apiCheck passed in the same invocation. `git diff --exit-code --quiet v2.5.1 -- <ProviderKeyCardTest, VoiceBinaryCompatShimTest, VoiceI18nSourceCompatTest, UndoCenterScreenTest, UndoHistoryStoreTest>` exits 0 (protected tests byte-identical to v2.5.1).
**Coverage/Nyquist (rung 2):** `23-VALIDATION.md` frontmatter `status: validated`, `nyquist_compliant: true`, `wave_0_complete: true`; not re-audited in this run (no Phase 23 component source changed since it was validated). Every criterion below maps to named, freshly passing tests plus device observations.
**Seed/fixture integrity:** all inputs are compiled-in probe values passed as intent extras (D5: no data layer; the Undo Center runs on a real in-memory `UndoHistoryStore`). Environmental integrity = AAR republished from HEAD and shown to contain the Phase 23 classes, baseline AAR shown to lack them, both on-device APK md5s read back and matching the built files.
**Dynamic color caveat:** the probe runs `YahirAndroidTasteTheme(dynamicColor = true)`, so hub-palette hex claims are Robolectric/source-contract proofs only; the device proves geometry, enabled state and that the buttons read the active scheme.
**Method notes:** the status-bar clock is excluded from every pixel diff (diff region starts at y=140 px). Tree diffs compare class, resource-id, text, content-desc, clickable, enabled and bounds, with the store's per-launch UUIDs in `undo-row-*` tags normalized. `uiautomator` does not expose `liveRegion`/`heading` semantics or TalkBack speech (Gate-2 item).

## Criteria

### 1. SC1 - Default calls render exactly as v2.5.1; existing tests pass unedited (VKEY-01, VUNDO-02)
result: passed
- **Rung:** 5 (pixel diff HEAD vs a real v2.5.1 build on the same device and theme), backed by rung 4 (UI trees) and rung 1 (fresh 1315-test run, protected tests byte-identical)
- **Target:** device (tester) + headless Robolectric
- **Expected:** `ProviderKeyCard` with empty `savedKeys` and `actions = null`, and `UndoCenterScreen` without group params, render identically to v2.5.1; the existing tests for both pass unedited.
- **Arranged (seeded):** `Defaults.kt` (v2.5.1 API only) compiled against each AAR. ProviderKeyCard states `d_pk_empty`, `d_pk_typed` (OpenAI, typed masked key, Valid), `d_pk_invalid` (Invalid reason), `d_pk_noproviders`; Undo Center states `d_uc_three` (Available, Failed, Undone), `d_uc_one`, `d_uc_empty`, on a real store.
- **Did (drove):** for each of the 7 states: launched the HEAD harness and the v2.5.1 harness (`am start ... ProbeActivity --es case <state>`), `uat-run capture` ui + screen in each, then pixel and tree diff of the signed files (`ab.py`).
- **Observed:**
  - **ProviderKeyCard, 4 states (captures 38-53):** screen diff below the status bar `bbox=None sum=0` for ALL 4; UI trees identical node for node (23, 26, 24, 18 nodes); `package` differs (`...harness` vs `...harnessb`), so two different builds were photographed. Falsifier: the same diff of HEAD `d_pk_empty` vs HEAD `d_pk_typed` gives `bbox=(124, 221, 955, 457)`, so the method detects a change.
  - **UndoCenterScreen, 3 states (captures 54-65):** `bbox=None sum=0` for all 3; trees identical (37, 24, 18 nodes); `Clear history` appears once in both `d_uc_three` dumps (one Undone entry) and in none of the `d_uc_one`/`d_uc_empty` dumps. Screen 57 (v2.5.1, three entries) read: Undo, red `Couldn't undo`, `Undone`, top-bar `Clear history`.
  - **Rung 1:** ProviderKeyCardDefaultParityTest 6/0/0, UndoCenterScreenDefaultParityTest 5/0/0, ProviderKeyCardTest 8/0/0, UndoCenterScreenTest 4/0/0, UndoHistoryStoreTest 25/0/0, V251CallSiteCanaryTest 12/0/0, all fresh; the five protected test classes diff clean against `v2.5.1`.
- **Evidence:** `38`-`53` (`*-sc1-p11-d-pk-*`), `54`-`65` (`*-sc1-p19-d-uc-*`).

### 2. SC2 - Summary with Update | Clear; field with Save (+ Cancel); inFlight disables every action; each action carries its tag (VKEY-01, VKEY-02)
result: passed
- **Rung:** 5 (screens read for the summary, the in-flight look and the German layouts), backed by rung 4 (tree text, enabled flags, tap counters) and rung 1
- **Target:** device (tester) + headless Robolectric
- **Expected:** saved key for the selected provider and `editingSavedKey = false` -> the field is replaced by `provider_key_saved_summary` reading "Saved · sk-ant-…abcd" with Update | Clear; otherwise the field with Save, plus Cancel when `onCancelEdit != null`; `inFlight = true` disables every action; tags `provider_key_action_{save,update,clear,cancel}`.
- **Arranged (seeded):** host providers Anthropic (`a`, saved `sk-ant-…abcd`), OpenAI (`b`, unsaved), Gemini (`c`, saved `AIza…wxyz`); intent flags for `sel`, `inflight`, `cancelalways`, `noactions`, `gate` (`saveEnabled = typed text non-blank`), `commit`, `lang` de/es, `w` 360 dp pin.
- **Did (drove):** taps on Update, Save, Cancel (A); Clear (A, fresh launch); B with and without a Cancel callback; `inflight` launches with taps on Update, Clear, the dropdown anchor, Cancel and Save; `gate` with an empty field, two field taps + one typed char via IME, then a Save tap; `noactions` with A and B; German/Spanish at 360 dp; `commit` flow Update -> type `abcd1234` -> Save.
- **Observed:**
  - **Probe 1 (01-02):** `provider_key_saved_summary` text exactly `Saved · sk-ant-…abcd`; no `provider_key_card_key_field`; `provider_key_action_update` and `provider_key_action_clear` present, 48 dp tall. Screen 02 read: check icon, summary, tonal Update pill, red Clear text button.
  - **Probe 2 (03-06):** one Update tap -> `edit=true upd=1`, the field with `provider_key_action_cancel` then `provider_key_action_save`, summary gone (screen 04); Save tap -> `save=1` (once); Cancel tap -> `can=1 edit=false`, summary back. **2b (36-37):** Update, 8 masked chars typed, Save -> `save=1`, summary `Saved · new-key…1234`.
  - **Probe 3 (07):** one Clear tap -> `clr=1`; host drops the key; the field with `provider_key_action_save` only, no summary, no Cancel.
  - **Probe 4 (08-09):** B shows the field with `provider_key_action_save` and no `provider_key_action_cancel`; with `onCancelEdit` supplied Cancel appears left of Save.
  - **Probe 6, `inFlight = true` (16-18):** Update, Clear and the dropdown anchor `enabled=false`; in field mode Cancel and Save `enabled=false`; after taps on all of them every counter stays 0 and no menu opened. Screen 17 read: dropdown, Update and Clear visibly greyed. The key text field stays `enabled=true` (ADV-2, reproduced).
  - **Probe 7 (19-22):** empty field -> Save `enabled=false`; after one typed char (`keyLen=1`, text `•`) Save `enabled=true`; the tap fires it (`save=1`).
  - **Probe 9 (28-29):** `actions = null` with A: summary only, no `provider_key_action_*` node, card 124.4 dp tall vs 180.6 dp; with B: the field, no button.
  - **Probe 10 (30-35):** German at the 360 dp pin: `Gespeichert · sk-ant-…abcd`, `Aktualisieren` (123.4 dp) and `Entfernen` (86.0 dp) on one row; `Abbrechen` (92.8 dp) and `Speichern` (103.8 dp) on one row; card right edge 344.2 dp, last button right edge 328.2 dp, inside the pin. Screens 31 and 33 read: nothing clipped or truncated. Spanish (35): `Guardada · …`, `Actualizar`, `Borrar`, same bounds discipline.
  - **Rung 1:** ProviderKeyCardLifecycleTest 25/0/0, ProviderKeyCardLongLabelTest 5/0/0, ProviderKeyCardSourceContractTest 5/0/0, fresh.
- **Evidence:** `01`-`09`, `16`-`22`, `28`-`33`, `35`-`37`.

### 3. SC3 - Switching across 3 providers; saved marker in the dropdown; a raw keyValue never appears while the summary shows (VKEY-02)
result: passed
- **Rung:** 5 (open-menu screen read), backed by rung 4 (trees, raw-text grep with a positive control) and rung 1
- **Target:** device (tester) + headless Robolectric
- **Expected:** switching `selectedProviderId` across 3 providers shows each provider's own summary or field; providers in `savedKeys` show `labels.savedMarker` in the dropdown; with a raw `keyValue` held while the summary shows, the raw key appears nowhere in the screen's semantics.
- **Arranged (seeded):** the same host; `keep` flag makes the host keep its typed text across a provider switch (hostile consumer, the KDoc says to clear it); a `raw` extra pre-sets the sentinel as `keyValue`.
- **Did (drove):** opened the dropdown and picked OpenAI, then Gemini, then Anthropic (3 real taps through the popup, all took on the first try); on B tapped the field twice and typed `RAWSECRET123XYZ` through the IME, hid the IME, picked Anthropic, captured; launched A (summary) with the sentinel pre-set as `keyValue`, captured; positive control: launched B with the sentinel, tapped the reveal eye, captured.
- **Observed:**
  - **Dropdown (10-11, 13, screen 11 read):** rows `Anthropic · saved`, `OpenAI`, `Gemini · saved`; the anchor keeps the plain label. German (34): `Anthropic · gespeichert`, `OpenAI`, `Gemini · gespeichert`.
  - **Switching (12, 14, 15):** B -> anchor `OpenAI`, `provider_key_card_key_field`, Save, no summary (`prov=1`); C -> `Saved · AIza…wxyz` with Update and Clear (`prov=2`); A -> `Saved · sk-ant-…abcd` (`prov=3`).
  - **Raw key (23-24, 27):** with the host holding the typed 15-char key (`keyLen=15`, field text 15 bullets in 23) and Anthropic selected, dump 24 contains `RAWSECRET` **0** times, no bullet text, no `provider_key_card_key_field`; dump 27 (summary with the sentinel pre-set as `keyValue`) also **0**. **Positive control (25-26):** masked field shows 15 bullets (25); revealed, the same sentinel appears **1** time (`text='RAWSECRET123XYZ'` on `provider_key_card_key_field`, 26), so the scan can see a leaked key.
  - **Rung 1:** ProviderKeyCardPerProviderTest 5/0/0, ProviderKeyCardRawKeySemanticsTest 8/0/0, fresh.
- **Evidence:** `10`-`15`, `23`-`27`, `34`.

### 4. SC4 - UndoCenterScreen group row: label, member count, status, Undo all callback; new params before the trailing onClear (VUNDO-02)
result: passed
- **Rung:** 5 (screens read for header, indent, German and long label), backed by rung 4 (trees, tap and hold counters, real-store results) and rung 1 (compile fixture and source contract)
- **Target:** device (tester) + headless Robolectric
- **Expected:** entries sharing an `UndoHistoryStore` group render one group row with the label, member count and status; its Undo all invokes the caller's callback for that group; the params sit before the trailing `onClear`.
- **Arranged (seeded):** a real `UndoHistoryStore`: group `g1` "Import batch" (Alpha, Beta, Gamma) and ungrouped Zeta, Eta, Theta interleaved (Eta appended between Beta and Gamma); variants `two` (group `g2` "Sync run", 2 members), `refuse` (group undo throws `UndoGroupRefusedException`), `nocb` (`onUndoGroup = null`), `flat` (groups left at the default), German/Spanish `undoAllLabel`, a 75-char label, a 360 dp pin.
- **Did (drove):** Undo all taps (full, after one member undo, refused twice, on g2 of two groups); one member Undo tap; 7 s long-presses on member Beta and on the group header label with a capture during the hold; launches `nocb`, `flat`, de/es/long at 360 dp.
- **Observed:**
  - **Probes 12 and 20 (66-67, screen 67 read):** order Theta, header, Gamma, Beta, Alpha, Eta, Zeta: one header at the first present member, all members after it although Eta sits between them in the store. `undo-group-header-g1` carries `Import batch` and `3 items · Undoable`, tinted band; `undo-group-undo-all-g1` `Undo all`. Members inset: row left 45 px (16 dp) vs 0 px, text 90 px vs 45 px. No `Clear history` (66).
  - **Probe 13 (68-69):** one Undo all tap -> `ug=[g1] u=0 runs=1 res=Undone(count=3)`; the 3 members read `Undone`, header `3 items · Resolved`, `undo-group-undo-all-g1` gone, the 3 ungrouped rows still offer `Undo`, `Clear history` now shown.
  - **Probe 14 (70-72, screen 71):** one member Undo (`u=1`) -> `3 items · Partly resolved`, Undo all still present; Undo all then -> `runs=1 res=Undone(count=2)`, `3 items · Resolved`.
  - **Probe 15 (73-74):** refusing group undo -> `res=Refused(reason=changed since, changedItem=null)`, all 6 `Undo` buttons still offered, header `Undoable`, Undo all present; a second tap runs it again (`ug=[g1, g1] runs=2`).
  - **Probe 16 (75-76):** holding member Beta shows `undo-preview-peek` with `undo-preview-peek-message` `Imported card Beta`; holding the header shows no peek node and fires nothing (`ug=[]`).
  - **Probe 17 (77-78):** `onUndoGroup = null`: header `3 items · Undoable`, no `undo-group-undo-all-g1` node.
  - **Probe 18 (79-83, screens 80, 82 read):** German at 360 dp: `Alle rückgängig machen` button 176.7 dp, right edge 344.2 dp, one line; label and subtitle end at 275/340 px, button starts at 471 px, no overlap. 75-char label: wraps to two lines and ends `the Monday research …` next to the button (ADV-1, reproduced). Spanish `Deshacer todo` 115.6 dp, inside the pin.
  - **Two groups (84-86):** headers `g2` (`2 items`) and `g1` (`3 items`) once each; Undo all on g2 -> `ug=[g2] res=Undone(count=2)`, g2 `Resolved`, g1 stays `3 items · Undoable` with its own Undo all.
  - **`flat` (87-88):** groups at the default while the store holds grouped entries: 6 `undo-row-*` rows, 0 `undo-group-*` nodes.
  - **Probe 19 and 20:** default Undo Center equals v2.5.1 (SC1, 54-65); `Clear history` absent with only Available entries (66, 73-79, 84) and present once an entry is spent (68, 70, 72, 86).
  - **Rung 1 and 0:** UndoCenterScreenGroupsTest 22/0/0, UndoCenterRowsTest 13/0/0, UndoCenterScreenLongLabelTest 4/0/0, UndoCenterScreenSourceCompatV251Test 2/0/0 (trailing-lambda and fully named call shapes compile), UndoCenterScreenSourceContractTest 7/0/0 (`groups`, `onUndoGroup` between `onUndo` and the trailing `onClear`); apiCheck green in the same run.
- **Evidence:** `66`-`88`.

## Summary

total: 4
passed: 4
partial: 0
failed: 0
infra: 0

## Notes / anomalies (for the Gate-2 reviewer and the orchestrator)

1. **ADV-1 (cosmetic, owner/UX call, reproduced):** a 75-char group label next to the German Undo all at 360 dp is cut at two lines (`the Monday research …`, screen 82). Specified `maxLines = 2`, nothing overlaps.
2. **ADV-2 (UX note, reproduced):** `inFlight = true` disables every action and the dropdown, but the key text field stays editable (capture 18, `enabled=true`).
3. **ADV-3 (design note, reproduced):** the header count is members present, not members still undoable (`3 items · Partly resolved`, capture 70).
4. **Known limits (Gate-2, not failures):** hub-palette hex claims are Robolectric-only (dynamic color on); TalkBack read-out of the summary and the group header heading is invisible to `uiautomator`; the S Pen overlay shows at the right edge of screenshots; the count/status words are English-only (signed trade-off).
5. **Captures not relied on for their label:** `88` is a duplicate of `87` (a redundant second capture of the same `flat` screen; identical content, 6 rows, 0 group nodes). No capture in this run is mis-timed or void.
6. **Tooling, verbatim.** (a) `uat-run abandon --run uatr-20261012T172501Z-65a2a2aa --reason "superseded: void captures, INC-2026-10-12-02"` -> `Error: uat-run abandon: run uatr-20261012T172501Z-65a2a2aa is finished, not open` (exit 1). `abandonRun` calls `loadOpenRun`, so a finished run cannot be abandoned; the old run still `verify`s as ok on its own. There is no CLI verb to mark a finished run superseded; supersession is recorded only by this log's `uat_run` now naming the new run (plus the non-standard `supersedes_uat_run` key above, which no consumer reads). (b) `uat-run finish` stderr: `uat-run: WARNING: force-staged 1 run file(s) the consumer's .gitignore matches (plain git add would drop them): .planning/phases/23-providerkeycard-lifecycle-undo-center-groups/23-02-uat-evidence/uatr-20261012T223815Z-1f8b52e0/89-logcat-fatal-check-all-probe-launches.log`; JSON `force_added` lists that file, `verify` = `verified, ok: true` with warning `capture 89-logcat-fatal-check-all-probe-launches.log is an empty logcat capture (negative evidence)`, `next`: `commit the run dir — finish stages (incl. force_added), it never commits`. (c) The INC-2026-10-12-02 fail-closed ui check is live: no ui capture in this run was refused or retried (my driver retries on refusal; it never fired). (d) The logcat FATAL capture (`AndroidRuntime:E *:S`, `--allow-empty`) covers five launches after one `adb logcat -c` (pk, uc, d_pk_empty, d_uc_three, uc+refuse with an Undo all tap): 0 lines; the probe process was alive and focused afterwards.
7. **Device residue.** Both harness packages uninstalled (`pm list packages --user 0 | grep -ci yahirandroid` = 0), Home pressed. No device setting changed (animator scale `1`, `stay_on_while_plugged_in` `15`, `wm size` override 1080x2316 unchanged). mavenLocal `1.10.0` is now the `deae923` build; `1.10.0-v251` the baseline. Harness and v2.5.1 archive live only in the session scratchpad; no `src/` file modified.
8. **Probe-list coverage (23-02-SUMMARY (e)).** 1: SC2, 01-02. 2: SC2, 03-06 (+2b 36-37). 3: SC2, 07. 4: SC2, 08-09. 5: SC3, 10-15. 6: SC2, 16-18. 7: SC2, 19-22. 8: SC3, 23-27. 9: SC2, 28-29. 10: SC2/SC3, 30-35. 11: SC1, 38-53. 12: SC4, 66-67. 13: SC4, 68-69. 14: SC4, 70-72. 15: SC4, 73-74. 16: SC4, 75-76. 17: SC4, 77-78. 18: SC4, 79-83. 19: SC1, 54-65. 20: SC4, 66/68/70/72/86 and SC1 54-65.
9. **Gate-2 fragment not refreshed.** `.planning/uat-pending/23-providerkeycard-lifecycle-undo-center-groups.md` still cites the old run id, AAR md5 `4f46ef75…` @ `8d54149` and the 79-byte captures. It points at this same SELF-UAT path, so the ledger entry stays valid, but its embedded identity is stale. Left untouched because this dispatch limits the commit to the run dir and this log; the orchestrator should refresh it (new run `uatr-20261012T223815Z-1f8b52e0`, AAR md5 `a695b8258e9e45c39ba9dad4b3cf359a` @ `deae923`) and run `gsd_run query milestone uat-ledger regen --raw`.

## Findings routed to gap-closure (if any)

None. Advisory only: ADV-1, ADV-2, ADV-3 (owner/UX calls, none a ROADMAP criterion).

## Verdict

All 4 criteria PASS on the real tester device in a fresh signed run (`uatr-20261012T223815Z-1f8b52e0`, verified, no void captures) against HEAD `deae923` -> Gate-1 complete; human Gate-2 stays deferred to milestone completion (fragment `.planning/uat-pending/23-providerkeycard-lifecycle-undo-center-groups.md`, see note 9).

## Supersession (2026-10-12)

This log supersedes the Gate-1 log written for run `uatr-20261012T172501Z-65a2a2aa` (committed at `888ccdb`, evidence committed at `13e19d2`). That run's verdict was also all 4 PASS, but three of its ui captures (`18`, `34`, `40`) were void 79-byte `No such file` files and several first attempts were mis-timed; under the director ruling for INC-2026-10-12-02 its captures are treated as void and the verdict is re-derived here from fresh observation only. Fresh observation re-confirms every prior per-criterion PASS (`PASS (re-confirmed)` for SC1-SC4) and all three advisories; nothing the prior log claimed was contradicted. The prior run could not be abandoned (note 6a); its run dir stays in the repo as history and is no longer referenced by this log's `uat_run`.
