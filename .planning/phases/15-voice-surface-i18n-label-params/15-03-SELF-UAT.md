---
status: complete
result: all_pass
gate: 1
phase: 15-voice-surface-i18n-label-params
source: [15-ROADMAP success criteria SC1-SC5]
device: Samsung SM-S908U Galaxy S22 Ultra, tester rig (USB serial R5CT10XNKQN, Android 15 / SDK 35)
apk: harness-debug.apk md5 7b5da4d953d181f6d6261b56592adab8 over library AAR yahirandroidtaste-1.10.0.aar md5 a5e6946417d793080cc7fed523a76677 @ 83169fe
run: 2026-10-05T03:31:56-06:00
---

<!-- Gate-1 agentic self-UAT, Phase 15 (closing plan 15-03). Platform: Android library, driver playbook
     /home/yahir/Projects/Reusable/android/yahirandroidtaste/AGENT-DEVICE-TESTING.md (project root). -->

# Phase 15 Gate-1 self-UAT: Voice-surface i18n label params

## Build identity and target

- Git HEAD `83169fe` (`src/`, `api.txt` clean vs HEAD at run time).
- Library coordinate (from the `publishing{}` block): `com.github.Ygaray:yahirandroidtaste:1.10.0` (NOT bumped
  per phase; the playbook examples are right on the number, but the log records it from the build file).
  Deleted `~/.m2/.../yahirandroidtaste/1.10.0` and `maven-metadata-local.xml` first, then
  `./gradlew clean publishReleasePublicationToMavenLocal --no-daemon -q` -> AAR md5
  `a5e6946417d793080cc7fed523a76677`.
- Throwaway harness app (session scratchpad, never committed), AGP 9.2.1, `compileSdk release(36){minorApiLevel=1}`,
  `minSdk 35`, depends on the mavenLocal AAR; `MainActivity` -> `ExplorerActivity`, plus a new
  `OverrideActivity` (source preserved as `15-03-evidence/harness-OverrideActivity.kt.txt`). APK md5
  `7b5da4d953d181f6d6261b56592adab8`, installed `-r` on `R5CT10XNKQN` (USB serial; the same phone also
  shows as `100.118.21.106:1496`, never used). Personal phone untouched.
- D1/D8 preflight: device `get-state=device`, `ro.build.version.sdk=35`, awake, keyguard not showing, no
  leases under `~/.gsd/leases/` (empty), no other tester process. Foreground was `com.caltracker.app`
  (not under test). Live probe -> reachable and free -> drove the real rig, no emulator fallback, no DEFERRED.
- Seed/fixture integrity (D5): no data layer. Gallery fixtures are static compiled-in values; "seeding" is the
  fresh publish above plus the harness resolving it. The harness `OverrideActivity` is a direct-call seed
  (D5 fallback: no UI path can inject caller overrides into the gallery, which hardcodes defaults).
- Restore: harness force-stopped and uninstalled, HOME pressed, `svc power stayon false` (the pre-run value
  was not captured; stayon was set true during the run per the playbook, and left off now).

## Method

The gallery proves DEFAULT English renders unchanged (the "omitted -> today's string" half of SC1-SC5).
The harness `OverrideActivity` proves the OVERRIDE half on the real device with distinctive `-OVR` marker
strings, which the gallery cannot do. Together they cover both halves of each criterion on the real app
(the earlier brief expected overrides to be unit-test-only; the harness made them drivable, so they are
PASS rather than PARTIAL).

### 1. SC1 - providerLabel / modelLabel render supplied text; omitted shows "Provider"/"Model" (VI18N-01)
result: passed
- Highest rung: 4 (uiautomator structure tree, text nodes). Text, not layout, is the claim; the label sits in
  an OutlinedTextField `label`, and the sheet screenshot rung (5) was used once for SC4 only.
- Arranged (seeded): fresh AAR publish; harness `OverrideActivity` with `providerLabel="Proveedor-OVR"`,
  `modelLabel="Modelo-OVR"`.
- Did (drove): launched gallery -> Voice Command -> ProviderKeyCard / ModelSelectCard detail pages; launched
  `OverrideActivity`.
- Observed, defaults (gallery): ProviderKeyCard texts `[... 'OpenAI','Provider','API key' ...]` (Default, Pressed,
  Focused cells all `Provider`); ModelSelectCard `['GPT-4','Model', ... 'Local Llama (offline)','Model' ...]`.
  Evidence: `15-03-evidence/g1-providerkeycard.xml`, `g2-modelselectcard.xml`.
- Observed, overrides: `['Anthropic','Proveedor-OVR','API key', ... 'GPT-4','Modelo-OVR' ...]`
  (`o1-override-main.xml`). Falsification: grep of the dump for bare `Provider`/`Model` text nodes -> no leaks.

### 2. SC2 - ClarificationBar renders caller dismissLabel, default "Dismiss" (VI18N-02)
result: passed
- Highest rung: 4.
- Arranged: harness `dismissLabel="Descartar-OVR"`.
- Did: gallery ClarificationBar page; `OverrideActivity`.
- Observed, default: both States-matrix cells show `'Dismiss'` (`g3-clarificationbar.xml`).
  Override: `'Descartar-OVR'` present, no bare `Dismiss` (`o1-override-main.xml`).
- Note: the override tap path (`onDismiss` still fires with a custom label) is a callback-identity claim; the
  gallery/harness `onDismiss` are no-ops, so that sub-claim rests on `ClarificationBarTest` (11 tests incl.
  "override still invokes onDismiss"), re-run green below.

### 3. SC3 - ApproachLadderCard five rung-state/toggle labels (VI18N-03)
result: passed
- Highest rung: 4.
- Arranged: harness ladder (cloud disabled + offline-incapable + above cap, `offlineOnly=true`,
  `maxTierId=local`) with all five label params overridden (`NoDisponible-OVR`, `Limitado-OVR`,
  `RequiereRed-OVR`, `EnLinea-OVR`, `SoloOffline-OVR`).
- Did: gallery ApproachLadderCard page (States matrix + scrolled to the combined-affordance Variants cell);
  `OverrideActivity`.
- Observed, defaults: `Unavailable`, `Capped`, `Needs network`, `Online`, `Offline only` all present
  (`g4-approachladder.xml`, `g4b-approachladder-variants.xml`: the combined cell shows
  `Cloud / Unavailable / Capped / Needs network`, toggle `Online` / `Offline only`).
- Observed, overrides: `Cloud / NoDisponible-OVR / Limitado-OVR / RequiereRed-OVR`, `Hybrid / Limitado-OVR`,
  toggle `EnLinea-OVR` / `SoloOffline-OVR`. A11y descriptions follow the label
  (`'EnLinea-OVR, not selected'`, `'SoloOffline-OVR, selected'`). No English default leaked (grep clean).
  This is the known, documented WR-04 residue only for the unrelated word "selected"/"not selected", which is
  out of VI18N-03 scope (see 15-VERIFICATION.md).

### 4. SC4 - OutcomeSheet literals via additive defaulted model fields (VI18N-04)
result: passed
- Highest rung: 5 (one downscaled capture, `o2-sheetA-small.jpg`, read; everything else rung 4).
- Arranged: harness sheet A = `Success` with `HandledByUiModel(escalationsLabel="Escalaciones-OVR:")`, an
  `UndoRowState.Undone` row with `undoneLabel="Deshecho-OVR"`, and `UndoRefusedUiModel(refusedPrefix=
  "NoSePudo-OVR:", changedSinceSuffix="cambioDesde-OVR")`; sheet B = `NeedsConfirmation` with
  `ProposedItemUiModel(removeContentDescription="Quitar-OVR")`.
- Did: gallery OutcomeSheet page -> "handled-by populated", "loud undo-refused" and "batch ... per-item
  remove" sheets; `OverrideActivity` -> Open sheet A / B.
- Observed, defaults (gallery): `Direct · OpenAI · gpt-4 · Escalations: 1` (`g5`); `Couldn't undo: Item changed
  since, Card deleted changed since` (`g6`); two `Remove` content descriptions on the batch items (`g7`).
  These match the v2.4.1 literals (`git show v2.4.1:.../OutcomeSheet.kt`: `"Escalations: $it"`,
  `"Couldn't undo: ${reason}${", $it changed since"}"`, `contentDescription = "Remove"`).
- Observed, overrides (harness): `Escalaciones-OVR: 1`; row trailing `Deshecho-OVR`; `NoSePudo-OVR: Item changed,
  Card deleted cambioDesde-OVR` (`o2-sheetA.xml`); the remove icon's content-description `Quitar-OVR`, and the
  count of `content-desc="Remove"` in that dump is 0 (`o3-sheetB.xml`). The screenshot confirms the text is
  laid out and readable inside the sheet (no clipping): headline, handled-by row, undo row with muted
  `Deshecho-OVR` at the trailing edge, and the error-container refusal line.
- Gap in the gallery (not a defect): no gallery fixture renders `UndoRowState.Undone`, so the default "Undone"
  string was NOT observed on the device; it rests on `OutcomeSheetTest` + `VoiceModelLabelDefaultsTest`
  (pins `undoneLabel == "Undone"`), re-run green below, plus the source diff (`row.undoneLabel` replaces the
  literal at the same site).

### 5. SC5 - Existing callers observe byte-identical English; strictly additive (no param/field removed or reshaped)
result: passed
- Highest rung: 2 (unit/coverage) corroborated at rung 4 on the device.
- Arranged/Did: re-ran `./gradlew testDebugUnitTest --tests *ProviderKeyCardTest *ModelSelectCardTest
  *ClarificationBarTest *ApproachLadderCardTest *OutcomeSheetTest *VoiceI18nSourceCompatTest
  *VoiceModelLabelDefaultsTest *ComponentRegistry* apiCheck --no-daemon -q` fresh on HEAD -> exit 0.
  Result XML summed from `build/test-results/testDebugUnitTest`: 105 tests, 0 failures, 0 errors, 0 skipped
  (Provider 8, Model 5, Clarification 11, Approach 15, OutcomeSheet 35, SourceCompat 2, ModelLabelDefaults 18,
  registry guards 11 incl. `ComponentRegistryDriftGuardTest`). `apiCheck` (Metalava) exit 0.
- Device corroboration: every default string in SC1-SC4 above renders exactly as the v2.4.1 literal on the
  untouched gallery fixtures (which pass none of the new params/fields).
- Caveat recorded, not a failure: per `15-VERIFICATION.md`, "additive" holds at the SOURCE/Metalava level;
  Kotlin default-arg call sites are not binary compatible, so consumers must recompile on repin (documented
  in `API.md`). This run does not assert binary compatibility. `tools/verify-api-additive.sh v2.4.1` exits 3
  (8 superseded lines, known false positive); it was not re-run here. Phase 18 SC2 has to use the lane-3
  override path or a Metalava judgment for that script.

## Logs

- `adb logcat -d -b crash` empty; `logcat -d *:E` filtered for `yahirandroidtaste`/`AndroidRuntime`: one
  Samsung `AppClassifier` "Package not found in BF and Manifest" line for the harness package (OS-side
  install-time classification noise, not the library). No exception, no crash.

## Audit of prior verdicts

`15-VERIFICATION.md` (static, 5/5 VERIFIED) was treated as a claim and re-derived from the ROADMAP criteria.
No contradiction found. It explicitly said no human/visual item was required; this run adds the one thing it
could not: the real device rendering of both defaults and overrides, plus the SC5 binary-compat caveat is
confirmed unchanged and out of scope for this gate.

## Evidence

`/home/yahir/Projects/Reusable/android/yahirandroidtaste/.planning/phases/15-voice-surface-i18n-label-params/15-03-evidence/`
(uiautomator dumps `g1`-`g7` gallery defaults, `o1`-`o3` harness overrides, `o2-sheetA-small.jpg` downscaled
capture, `harness-OverrideActivity.kt.txt` harness source).

## Gate-2

Registered as `.planning/uat-pending/15-voice-surface-i18n-label-params.md`; the owner's remaining visual
judgment is a spot check that real localized strings (consumer-supplied) do not truncate in the dropdown
labels, segmented toggle and sheet rows. No physical step is blocking.
