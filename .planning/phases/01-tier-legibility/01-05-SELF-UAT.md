---
status: complete
result: all_pass
gate: 1
phase: 01-tier-legibility
source: [01-ROADMAP success criteria #2, 01-VERIFICATION.md human_verification #1, 01-UAT.md #1]
device: Samsung SM-S908U / yahirs-s22-ultra-2 (R5CT10XNKQN, Android 15)
apk: yahirandroidtaste-1.10.0.aar (md5 18e493e666f8bcaeed23b2c22953fadb @ 87561ff) hosted via throwaway uat-harness app-debug.apk (md5 c8dff9ca01856a2f5a098a224dc7080d)
run: 2026-09-02T00:23:00Z
---

# Self-UAT Log — Phase 01 Plan 05 (Wire Entry.tier into Both Gallery Surfaces)

**Device:** Samsung SM-S908U / yahirs-s22-ultra-2 (100.118.21.106:1496, R5CT10XNKQN, Android 15)
**Library build:** `com.github.Ygaray:yahirandroidtaste:1.10.0` published to mavenLocal from git HEAD
`87561ff` (clean tree — `git status --short -- src/` empty at build time), md5
`18e493e666f8bcaeed23b2c22953fadb`.
**Run:** 2026-09-02T00:23Z
**Schema:** N/A (no DB/data layer for this phase)
**Pre-flight:** device awake (`mWakefulness=Awake`), user-0 unlocked (`deviceLocked=0`; the `deviceLocked=1`
also present in `dumpsys trust` belongs to the unrelated Secure Folder profile, id=150 — a known
device quirk, not the primary user).
**Unit suite:** not re-run here (already green per `01-VERIFICATION.md`: `compileDebugKotlin`,
`testDebugUnitTest` incl. `ComponentRegistryTierTest` 4/4, `apiCheck`, `detekt` — all BUILD SUCCESSFUL).
**Coverage/Nyquist:** N/A for this log — this log covers only the single outstanding
`human_verification` visual item from `01-VERIFICATION.md`.
**Seed/fixture integrity:** N/A — `ExplorerActivity` reads only the static, compiled-in
`ComponentRegistry`; no DB/fixture to seed.

## Driver-mechanism note (read before the criteria — this is the interesting part)

`yahirandroidtaste` is a pure `com.android.library` module (no `applicationId`, no `androidTest`
source set) — there is no installable APK produced by this repo alone, so the global
`AGENT-DEVICE-TESTING.md` template's D2 assumption ("build the app, `am start -n
<APPLICATION_ID>/.MainActivity`") does not directly apply, and no project-local driver playbook
exists yet. Two dead ends before landing the real fix:
1. `./gradlew assembleDebugAndroidTest` produces an installable, self-instrumenting APK
   (`io.github.ygaray.yahirandroidtaste.test`) whose merged manifest DOES contain `ExplorerActivity`
   — but `adb shell am start` (shell UID) cannot launch it: `ExplorerActivity` is declared
   `exported=false` in the library's own manifest (by design, per `INTEGRATION.md` — same-package
   launch only), and Android 15 throws `SecurityException: not exported from uid …` for an
   external-UID launch attempt. This is expected platform behavior, not a bug.
2. The correct, production-faithful path — exactly what `INTEGRATION.md` §6 documents for a real
   consumer — is a **same-package Intent from a genuine host app**. Built a throwaway,
   uncommitted **UAT harness app** (`io.github.ygaray.yahirandroidtasteharness`, outside the git
   tree, in the session scratchpad) that: (a) depends on
   `com.github.Ygaray:yahirandroidtaste:1.10.0` resolved from `mavenLocal()` (published from the
   current tree via `./gradlew publishReleasePublicationToMavenLocal`, `compileSdk` matched to
   `release(36){minorApiLevel=1}` per the library's own AAR-metadata requirement), and (b) has one
   `MainActivity` whose entire body is `startActivity(Intent(this, ExplorerActivity::class.java));
   finish()`. `ExplorerActivity` itself is a plain `ComponentActivity` (not `@AndroidEntryPoint`)
   and nothing reachable from the gallery's navigation/list/detail path touches Hilt
   (`grep -rln "hiltViewModel\|@Inject\|@HiltViewModel" src/main/.../explorer/` → no hits), so the
   harness needed no Hilt setup — it built and installed clean, `am start` on the harness's own
   exported launcher worked, and `ExplorerActivity` displayed with zero crashes
   (`ActivityTaskManager: Displayed …ExplorerActivity …`, confirmed via `logcat`).
   This harness is **not** part of the library and was **not** committed — it exists only as this
   run's testing infrastructure, matching the driver-playbook contract's D2 role for a
   library-only project. **Recommendation for gap-closure/next-phase bootstrap:** author a
   project-local `AGENT-DEVICE-TESTING.md` documenting this same-package-harness pattern so future
   Gate-1 runs for this repo don't have to re-derive it.
- **Gotcha hit and confirmed:** the global playbook's "`KEYCODE_BACK` pops the whole
  Activity/screen, not just the last nav step" warning is real here too — two `KEYCODE_BACK`
  presses from the detail screen popped past the harness's empty task stack (since `MainActivity`
  self-finishes) into whatever app was previously foregrounded on the device (SecondBrain, already
  running from a prior session) — not a defect, just re-launched the harness and drove one back-step
  at a time thereafter.

## Criteria

### 1. On-device visual confirmation of the TierBadge (Primitive/Pattern) on both gallery surfaces (ROADMAP SC2 / VERIFICATION.md human_verification #1 / 01-UAT.md #1)
result: passed
- **Rung:** 5 (visual capture) — required; a structure-tree dump alone is explicitly insufficient
  and was proven so mid-run (see Observed below).
- **Target:** device (yahirs-s22-ultra-2, real hardware; no emulator fallback needed — device was
  reachable and leased on the first probe).
- **Expected:** Per `01-UAT.md`/`01-VERIFICATION.md`: the Primitive/Pattern badge renders fully,
  never clipped or pushed off-row on `ComponentRow` (list + index search results); on
  `ComponentDetailScreen`'s `TopAppBar` title, the component name truncates with an ellipsis (never
  the badge) when the two don't fit between the back arrow and the theme-toggle action; badge color
  is visually distinguishable between Primitive (`secondaryContainer`) and Pattern
  (`tertiaryContainer`) in both light and dark theme. Checked for a short name (`AppChip`) and the
  three longest registered names (`RecordingBottomSheetContent` 28 chars, `TagChipWithContextMenu`/
  `SegmentedOptionSelector` 23 chars) called out by the ROADMAP/prior verification.
- **Arranged (seeded):** none — `ComponentRegistry` is a static compiled-in catalog, no
  rows/fixtures to seed. Arrange work here was entirely environmental: publish the library AAR to
  `mavenLocal` from the current tree, build+install the throwaway harness app (see driver-mechanism
  note above).
- **Did (drove):** Launched the harness (`am start io.github.ygaray.yahirandroidtasteharness/.MainActivity`,
  which same-package-Intents into `ExplorerActivity`). Tapped into the **Sheets** family list (18
  entries incl. the 28-char `RecordingBottomSheetContent`), captured light theme, toggled dark
  theme via the `TopAppBar` action, re-captured. Used the index screen's own search field
  (`input text`) to reach the **index search-results `ComponentRow`** surface (distinct render path
  from a family screen's own list) for `SegmentedOptionSelector` (23 ch), `RecordingBottomSheetContent`
  (28 ch), and `AppChip` (short) — then tapped each into `ComponentDetailScreen` and captured its
  `TopAppBar` in both themes.
- **Observed:**
  - **List/search surface (`ComponentRow`), light + dark, long names:** `RecordingBottomSheetContent`
    (28 ch, longest registered name) renders in full next to a fully-visible `"Pattern"` badge, no
    clipping/push-off in either theme (evidence 03, 06, 21). `uiautomator` bounds cross-check:
    name `[45,932]-[708,1000]` px, badge `[729,899]-[864,1034]` px, both comfortably inside the
    1080px-wide row.
  - **Detail surface (`ComponentDetailScreen` `TopAppBar`) — the falsifying case:** for
    `SegmentedOptionSelector` (23 ch) the **`uiautomator` accessibility-tree dump reported the full,
    un-truncated string** (`text="SegmentedOptionSelector"`) — if I had stopped at rung 4 (structure
    tree) this would have looked like a pass with zero truncation happening. The **screenshot**
    (rung 5) showed the opposite: the name is visually clipped to `"SegmentedOptionS…"` with a real
    ellipsis, while the `"Primitive"` badge, back arrow, and theme-toggle icon are all fully intact
    (evidence 08). This is exactly the ladder's documented trap ("a structure-tree COUNT/text is not
    a visual verification") and it fired for real on this exact criterion — confirming rung 5 was
    the correct, necessary layer for this claim, not an overcautious escalation. Re-confirmed
    identically for the 28-char `RecordingBottomSheetContent` (`"RecordingBottomSh…"` + intact
    `"Pattern"` badge, evidence 13) and in dark theme (evidence 15). The short name `AppChip`
    needed no truncation and rendered in full next to its badge in dark theme (evidence 24).
  - **Color distinguishability:** Primitive badges render in a blue-gray `secondaryContainer` tone,
    Pattern badges in a mauve/purple `tertiaryContainer` tone — clearly distinct from each other in
    both light (evidence 03) and dark (evidence 21) theme; verified side-by-side in the same
    screenshot (`ListCardBottomSheet`=Pattern, `RecordingBottomSheetContent`=Pattern,
    `SheetScaffold`=Primitive all visible in one frame, both themes).
  - **Logs:** `adb logcat` across the whole session shows zero crashes/FATAL/uncaught exceptions
    for either the harness or the library's explorer package.
- **Evidence:** (all under session scratchpad `evidence/`, referenced here by content since they are
  outside the git tree — see note below)
  1. `03-crop-recording-row.png` — Sheets list, light theme, `RecordingBottomSheetContent`
     (28 ch) + `Pattern` badge fully visible next to `SheetScaffold` + `Primitive`.
  2. `06-crop-search-row.png` — index search-results `ComponentRow`, light theme,
     `SegmentedOptionSelector` (23 ch) + `Primitive` badge.
  3. `08-crop-topbar-recording.png`(same run)/`08-crop-topbar.png` — `ComponentDetailScreen`
     `TopAppBar`, light theme: `"SegmentedOptionS…"` ellipsis-truncated, `Primitive` badge intact.
  4. `13-crop-topbar-recording.png` — `TopAppBar`, light theme, 28-char name:
     `"RecordingBottomSh…"` + intact `Pattern` badge.
  5. `15-crop-topbar-recording-dark.png` — same, dark theme.
  6. `21-crop-recording-row-dark.png` — Sheets list, dark theme, same three rows as (1), colors
     still distinguishable.
  7. `24-crop-topbar-appchip.png` — `TopAppBar`, dark theme, short name `AppChip` + `Pattern`
     badge, no truncation needed.
  8. `uiautomator` XML dumps `02-sheets-family.xml`, `05-search-results.xml`,
     `07-detail.xml`, `11-search-recording.xml`, `12-detail-recording.xml`, `18/20-sheets-dark*.xml`
     — bounds cross-checks backing the "not clipped" claims above.
  9. `adb logcat -d` capture — no FATAL/crash lines for either package across the full run.

## Summary

total: 1
passed: 1
partial: 0
failed: 0
infra: 0

## Notes / anomalies (for the Gate-2 reviewer)

- **No project-local `AGENT-DEVICE-TESTING.md` exists yet** for this pure-library repo — this run
  fell back to the global template, which assumes an installable app and doesn't directly cover a
  library-only module with no `applicationId`. The throwaway same-package-Intent harness (see
  driver-mechanism note) is the correct, production-faithful pattern (it's literally
  `INTEGRATION.md`'s own documented consumer-integration path) and is cheap to re-derive, but
  authoring a project-local playbook that documents it explicitly would save re-deriving this on
  every future Gate-1 run against this repo. Recommend adding one in a later phase (not a blocker
  here — Gate-1 for THIS phase is fully satisfied on real hardware).
- The harness app and its build artifacts are **not committed** — they live only in this session's
  scratchpad (`/tmp/claude-1000/.../scratchpad/uat-harness/` and `evidence/`) as throwaway UAT
  tooling, per the diagnose-only tester contract (no source-tree edits). The evidence screenshots
  referenced above are attached to this run's context; they are not persisted as repo artifacts
  since the harness itself isn't part of the library.
- Both `pm uninstall`/device-state cleanup and the device lease were released at the end of the run;
  `pm list packages --user 0` confirms neither `io.github.ygaray.yahirandroidtasteharness` nor
  `io.github.ygaray.yahirandroidtaste.test` remain installed.
- Confirmed the `KEYCODE_BACK`-exits-the-whole-task gotcha from the global playbook fires for this
  harness shape (self-finishing `MainActivity` + single-Activity task) — not a library defect.

## Findings routed to gap-closure (if any)

None — the criterion is a genuine PASS on real hardware, confirmed adversarially (the structure-tree
rung alone would have produced a false PASS on the exact truncation claim under test; only the
screenshot rung settled it, and it settled it correctly in the app's favor).

## Verdict

All criteria PASS → Gate-1 complete; human Gate-2 deferred to milestone completion (registered in
`.planning/uat-pending/01-tier-legibility.md` → `HUMAN-UAT-PENDING.md`).
