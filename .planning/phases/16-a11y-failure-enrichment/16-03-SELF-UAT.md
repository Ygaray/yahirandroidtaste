---
status: partial
result: has_partial
gate: 1
phase: 16-a11y-failure-enrichment
source: [16-ROADMAP success criteria SC1-SC5]
device: Samsung SM-S908U Galaxy S22 Ultra, tester rig (USB serial R5CT10XNKQN, Android 15 / SDK 35, 1080x2316 @ 450dpi)
apk: harness-debug.apk md5 2f2d2a1ec9f62c34263457e1226830c7 over library AAR yahirandroidtaste-1.10.0.aar md5 2a7ce56547374776d5f5583031b80597 @ 118af62
run: 2026-10-05T04:34:00-06:00
---

<!-- Gate-1 agentic self-UAT, Phase 16 (closing plan 16-03). Platform: Android library, driver playbook
     /home/yahir/Projects/Reusable/android/yahirandroidtaste/AGENT-DEVICE-TESTING.md (project root). -->

# Phase 16 Gate-1 self-UAT: A11y + Failure enrichment

## Build identity and target

- Git HEAD `118af62` (`git status --short src api.txt` empty before and after the run; code includes review fixes).
- Library coordinate `com.github.Ygaray:yahirandroidtaste:1.10.0` (hardcoded in `publishing{}`, not bumped per phase).
  Deleted `~/.m2/.../yahirandroidtaste/1.10.0` and `maven-metadata-local.xml`, then
  `./gradlew clean publishReleasePublicationToMavenLocal --no-daemon -q` -> AAR md5
  `2a7ce56547374776d5f5583031b80597`.
- Throwaway harness app (session scratchpad, never committed), AGP 9.2.1, `compileSdk release(36){minorApiLevel=1}`,
  `minSdk 35`, over the mavenLocal AAR, plus a new `Phase16Activity` (source preserved as
  `16-03-evidence/harness-Phase16Activity.kt.txt`). APK md5 `2f2d2a1ec9f62c34263457e1226830c7`, installed `-r` on
  `R5CT10XNKQN`. Personal phone never touched; no airplane mode.
- D1/D8 preflight (live probe): `get-state=device`, `ro.build.version.sdk=35`, was Dozing (woken, `wm dismiss-keyguard`,
  `svc power stayon true`), keyguard not showing, `~/.gsd/leases/` empty, no other tester process. Reachable and free,
  so the real rig was driven for everything: no emulator fallback, nothing DEFERRED.
- Seed/fixture integrity (D5): no data layer. The hub gallery has no fixture for `FailureActionUiModel.role`,
  `Failure.body`, `Failure.semanticsPrefix`, or a selectable-cap ladder with a stale id, so `Phase16Activity` is a
  direct-call seed (D5 fallback, same approach as Phase 15). Intent extra `sheet` selects 0 none / 1 legacy v2.4-shaped
  Failure / 2 Destructive role + body + prefix + handledBy / 3 Save role.
- Restore: harness force-stopped and uninstalled, HOME pressed, `svc power stayon false` (the pre-run stayon value was
  not captured; the device was Dozing and is left off).

## Method

Cheapest layer first: unit/source-contract tests (rung 1, re-run fresh), then uiautomator trees on the real device
(rung 4: `checkable`/`checked`/`clickable` flags, bounds for pitch, `content-desc` on the merged node), and one
downscaled capture session (rung 5) for the genuinely visual claims (role color, body inside the surface).
A baseline arm (sheet 1, ladders with no cap) is driven next to each override arm so "null renders unchanged" is
falsifiable, not assumed.

### 1. SC1 - ApproachLadderCard rung min interactive size + selected/RadioButton semantics, no public API change (VA11Y-01)
result: passed
- Highest rung: 5 (rung 4 settled the semantics; one downscaled capture `p1-ladders-small.jpg` for the pitch).
- Arranged (seeded): `Phase16Activity` hosts three ladders on the same 3-rung model: (A) `maxTierId="hybrid"` +
  `onMaxTierChange` (selectable cap), (B) cap-less (both null), (C) `maxTierId="ghost"` (stale id, selectable).
- Did (drove): launched; dumped; tapped rung "Cloud" in ladder A, tapped a rung in ladder B, tapped "Local" in A.
- Observed (`p1-ladders.xml`): ladder A rows are `[90,170][990,305]`, `[90,333][990,468]`, `[90,496][990,631]`, each
  135 px = 48 dp (450 dpi) = the minimum interactive target. All three `checkable=true`; only `Hybrid` (the cap)
  `checked=true`; the other two `clickable=true`. (The selected node reports `clickable=false`, which is how the
  platform bridge exposes a checked radio; the node is still checkable and focusable.) Falsification: ladder B
  (cap-less) rows are 68 px tall text nodes, NOT checkable, NOT clickable, no `checked`, i.e. compact and with no
  radio announced (conditional-render-no-dead-space holds). Ladder C (stale id) has 3 checkable rows and NONE checked
  (stale id selects no rung, matching the KDoc).
- Behavior: tap "Cloud" -> header `cap=cloud`, checked moved to Cloud (`p2-after-tap-cloud.xml`); tap in cap-less
  ladder -> cap unchanged (`p3`); tap "Local" -> `cap=local` (`p4`). Row pitch grew from 96 px (34 dp, cap-less rows)
  to 163 px (58 dp, selectable rows), matching the plan's accepted D-04 growth; the screenshot shows the cap-less ladder
  visibly tighter than the selectable one.
- Mechanism: `ApproachLadderCardTest` (+198 lines of semantics tests) re-run green below.
- No public API change from the Phase 16 rung work: the only `ApproachLadderCard` line in `git diff v2.4.1 -- api.txt`
  is Phase 15's label-param supersede (the `ApproachLadderCard` signature is identical before and after Phase 16;
  the rung change touches only private `CapControl`/`RungRow` and KDoc). `apiCheck` re-run exit 0.
- Caveat (Gate-2, not a failure): the Android class reports `android.view.View`, not a RadioButton class name, in the
  uiautomator dump; the `Role.RadioButton` announcement ("radio button", "selected", radio-group position) is what
  TalkBack speaks and cannot be heard by this tester.

### 2. SC2 - FailureActionUiModel.role wired to the Failure action button (VFAIL-01)
result: passed
- Highest rung: 5 (role color is visual).
- Arranged: sheet 1 = action built with the v2.4 two-argument shape (default role); sheet 2 = `role = Destructive`;
  sheet 3 = `role = Save`.
- Did: opened each sheet; tapped the Destructive action once, dismissed with BACK, read the harness counter.
- Observed: capture `s123-sheets-small.jpg` (read): sheet 1 `LEGACY-ACTION` is the plain purple text button (today's
  Neutral); sheet 2 `DESTRUCT-ACTION` is red text (error color); sheet 3 `SAVE-ACTION` is a filled primary pill. Three
  roles, three visibly different buttons on the real device, and the omitted-role case matches the old hardcoded
  Neutral. Tapping `DESTRUCT-ACTION` incremented the harness counter to `clicks=1` (`s2-after-action-tap.xml`), so
  the callback still crosses out with a custom role.
- Mechanism: `FailureRoleSourceContractTest` + `OutcomeSheetTest` role cases re-run green below.

### 3. SC3 - Failure.body renders inside the error surface; null unchanged (VFAIL-02)
result: passed
- Highest rung: 5.
- Arranged: sheet 2 `body = { Text("BODY-SLOT-MARKER extra detail") }` with a handledBy row and an action; sheet 1/3
  have `body = null`.
- Did: opened sheets 1, 2, 3, dumped and captured.
- Observed: in sheet 2 the surface node is `[0,1719][1080,2181]`; `BODY-SLOT-MARKER extra detail` is at
  `[45,1922][735,1990]` (inside it), after the reason `[45,1764]` and the handled-by `Cloud` `[45,1865]`, before the
  action button `[45,2001]`. The capture shows it on the same pink error-container surface, ordered
  reason / handled-by / body / action. Sheets 1 and 3 (null body): no extra node and no gap between handled-by and the
  action (sheet 1: `Cloud` [45,1933][149,1990] then action [45,2001], identical to the v2.4 layout). 
- Mechanism: `OutcomeSheetTest` body cases re-run green below.

### 4. SC4 - Failure.semanticsPrefix prefixes the accessibility announcement; null unchanged (VFAIL-03)
result: passed
- Highest rung: 4 (merged-node `content-desc`). The spoken announcement itself is Gate-2 (see below).
- Arranged: sheet 2 `semanticsPrefix = "PREFIX-ERROR:"`, reason `NEW-REASON key rejected`.
- Did: opened sheet 2 and sheets 1/3; dumped.
- Observed: the surface node `[0,1719][1080,2181]` carries `content-desc='PREFIX-ERROR: NEW-REASON key rejected'`
  (prefix, one ASCII space, reason). The action button remains its own clickable+focusable node
  (`[45,2001][461,2136]`), not swallowed into the merge. Falsification: sheets 1 and 3 (null prefix) have NO
  `content-desc` on the surface or any node other than the sheet chrome ("Close sheet", "Drag handle"), i.e. the
  announcement is the plain reason text as today.
- Open concern, deferred to Gate-2 (the PARTIAL half): the prefix is implemented as a `contentDescription` on a
  `mergeDescendants` node, which in Compose REPLACES the merged children's text for a screen reader. The tree shows
  the reason, the handled-by tier ("Cloud") and the body slot text as separate text nodes, but whether TalkBack reads
  ONLY `PREFIX-ERROR: NEW-REASON key rejected` and drops "Cloud" and the body text cannot be settled by uiautomator.
  The owner should listen with TalkBack on the sheet-2 layout (harness source preserved).
- Mechanism: `OutcomeSheetTest` semantics cases re-run green below.

### 5. SC5 - All additions strictly additive; existing Failure callers unchanged, nothing removed or reshaped
result: partial
- Highest rung: 2 (unit/coverage + api check) corroborated at rung 4/5 on the device for the passing half.
- Arranged/Did: re-ran `./gradlew testDebugUnitTest --tests *ApproachLadderCardTest *OutcomeSheetTest
  *VoiceI18nSourceCompatTest *VoiceModelLabelDefaultsTest *FailureRoleSourceContractTest *ComponentRegistry* apiCheck
  --no-daemon -q` fresh on HEAD -> exit 0; result XML summed: 114 tests, 0 failures, 0 errors, 0 skipped. `apiCheck`
  (Metalava) exit 0. `git status --short src api.txt` empty.
- Device corroboration: the legacy-shaped arm (sheet 1, positional two-argument `FailureActionUiModel`, no new Failure
  fields) renders the v2.4 look: reason, handled-by, a plain Neutral button, no description node. All v2.4 call shapes
  in the real consumers found on this host (CalTracker `VoiceOutcomeMapper.kt`, SecondBrain
  `VoiceFailureSheetModels.kt`) use NAMED `label = ..., onClick = ...` arguments, so they still compile.
- Why PARTIAL and not PASS: while writing the harness I hit a SOURCE-compat break that I reproduced on the real build:
  `FailureActionUiModel("x") { ... }` (trailing-lambda construction, valid against v2.4.1 where `onClick` was the last
  parameter) FAILS to compile against this AAR (`No value passed for parameter 'onClick'` / `Argument type mismatch:
  actual type is '() -> Int', but 'ActionButtonRole' was expected`), because the appended defaulted `role` is now the
  last parameter and the lambda binds to it. The phase's own 16-03 SUMMARY (deviation 2) already found and
  documented this and deferred wording to Phase 18 DOC-02; no test pins it and no tag/API gate flags it (apiCheck is
  an additive-symbol check, not a call-idiom check). So SC5's literal clause "existing callers are unchanged"
  is not true for the trailing-lambda idiom. Whether that is acceptable under the v2.5 "strictly additive" bar, or
  needs a gap-closure change, is the owner's API-contract call (it is a choice about what "additive" promises, not an
  obvious engineering default). Everything else under SC5 passed.
- Not asserted: binary compatibility (Kotlin default-arg call sites need recompile on repin; same caveat as Phase 15,
  documented for Phase 18 DOC-02). `tools/verify-api-additive.sh` was not re-run (known false positive vs v2.4.1,
  Phase 18 scope).

## Logs

`adb logcat -d -b crash` is empty (0 lines); `logcat -d *:E` filtered for `yahirandroidtaste`/`AndroidRuntime`: no
matches. No exception, no crash across 3 sheets + 3 ladders.

## Audit of prior verdicts

`16-VERIFICATION.md` (static) and `16-03-SUMMARY.md` were treated as claims and re-derived from the ROADMAP criteria.
The summary's own deviation note about the trailing lambda is the one claim the verdict language ("strictly
additive") does not carry through; recorded above as the SC5 PARTIAL. No contradiction on SC1-SC4; the summary's
list of Gate-2 items (rung pitch, role color, TalkBack prefix) is confirmed and sharpened: the role color is now
observed on the device (red / primary-filled / purple), leaving the owner's visual taste call and the TalkBack
listen as the genuinely human steps.

## Evidence

`/home/yahir/Projects/Reusable/android/yahirandroidtaste/.planning/phases/16-a11y-failure-enrichment/16-03-evidence/`
(uiautomator dumps `p1`-`p4` ladders, `s1`-`s3` sheets, `s2-after-action-tap.xml`; downscaled captures
`p1-ladders-small.jpg`, `s123-sheets-small.jpg`; harness source `harness-Phase16Activity.kt.txt`).

## Gate-2

Registered as `.planning/uat-pending/16-a11y-failure-enrichment.md`: (a) TalkBack listen on the Failure sheet with
prefix + handled-by + body (does the merged `contentDescription` drop "Cloud" / the body text), (b) the owner's taste
call on the 58 dp selectable rung pitch and the Destructive/Save role colors, (c) the owner's decision on the
`FailureActionUiModel` trailing-lambda source break (accept + document in DOC-02, or route to gap-closure).
