---
audit_acknowledged:
  milestone: v2.0
  at: 2026-09-27
  gap_snapshot: "unknown::scenarios=0"
---

# Self-UAT Log — Phase 7 Plan 01 (Chip-color slot — TAGCOLOR-01)

**Device:** Samsung SM-S908U / yahirs-s22-ultra-2 (R5CT10XNKQN via USB adb, Android 15) — the Gate-1 tester rig. Reachability live-probed (`adb devices` + `tailscale ping` both confirmed) before driving; the personal phone (`100.126.94.47:44503`), also adb-connected in this shell, was explicitly excluded (confirmed absent from `pm list packages` both before and after) — only `R5CT10XNKQN` received the harness install.
**Library build:** `com.github.Ygaray:yahirandroidtaste:1.10.0` published fresh to `mavenLocal` from git HEAD `418dd3b` (clean tree — `git status --short -- src/ build.gradle.kts gradle/libs.versions.toml api.txt CLAUDE.md config/` empty at build time; this HEAD is one commit past what the session's stale git-status snapshot showed, confirmed via `git log`), AAR md5 `633c2c599315919f12670f0c6e6b2b75` (old mavenLocal artifact deleted first to rule out a stale-cache false pass).
**Run:** 2026-09-27T14:56:21Z
**Schema:** N/A — no DB/data layer; `ExplorerActivity` reads only the static, compiled-in `ComponentRegistry` + `ExplorerFakeData`.
**Pre-flight:** device awake (`mWakefulness=Awake`), unlocked (`mScreenLocked=false`, `mDreamingLockscreen=false`).
**Unit suite:** re-run directly at HEAD `418dd3b`, not trusted from `07-01-SUMMARY.md`'s inherited verdict — `./gradlew testDebugUnitTest --tests "*AppChipTest*" --tests "*TagChipWithContextMenuTest*" --tests "*CardTagRowTest*" --tests "*TagChipUiModelTest*" --tests "*ComponentRegistryDriftGuardTest*" --tests "*DomainVocabularyDriftGuardTest*"` → `BUILD SUCCESSFUL`. Per-class JUnit XML: `AppChipTest` 12/12, `TagChipWithContextMenuTest` 5/5, `CardTagRowTest` 3/3, `TagChipUiModelTest` 7/7, `ComponentRegistryDriftGuardTest` 1/1, `DomainVocabularyDriftGuardTest` 2/2 — zero failures/errors/skips across all 30 test cases.
**Coverage/Nyquist:** not re-run standalone (out of this log's scope) — `TagChipUiModelTest` itself is the artifact of a prior Nyquist gap-fill (commit `418dd3b`, "Nyquist gap-fill") for the `equals()`/`copy()`/`Companion.of()` semantics WR-02 flagged; its presence + green result is independently re-confirmed here.
**Seed/fixture integrity:** N/A — `ComponentRegistry`/`ExplorerFakeData` are static compiled-in catalogs, no rows/fixtures to seed. Confirmed (adversarially, per the task's own scope note) that `ExplorerFakeData.kt`'s tag fakes leave `TagChipUiModel.color` at its `null` default — so no chip in the gallery renders a caller-supplied color today; this is the phase's own documented D5 boundary, not a defect.

## Driver-mechanism note (carried forward from `01-05-SELF-UAT.md` / `06-03-SELF-UAT.md`)

`yahirandroidtaste` is a pure `com.android.library` (no `applicationId`) — still no project-local `AGENT-DEVICE-TESTING.md` (falls back to the global template). Reused the same throwaway same-package-Intent UAT harness this session's Phase 6 run had already built in the scratchpad (`io.github.ygaray.yahirandroidtasteharness`, depending on `com.github.Ygaray:yahirandroidtaste:1.10.0` from `mavenLocal`): deleted the stale mavenLocal artifact, republished from the current HEAD, rebuilt+reinstalled the harness (picking up the fresh AAR), and relaunched — this is the correct re-verification move for an already-built harness rather than a false pass off a cached artifact. Built clean, installed clean (confirmed on `R5CT10XNKQN` only), launched clean — zero `FATAL`/`Exception` lines in `adb logcat -d` across the entire session. Harness uninstalled at the end of the run (not committed — throwaway testing infra only, per the diagnose-only tester contract).

## Criteria

### 1. `TagChipUiModel` carries `color: Color? = null`; `AppChip` and `TagChipWithContextMenu` carry `containerColorOverride: Color? = null` — ROADMAP SC1

result: passed

- **Rung:** 3 (headless data/api-diff check) — decisive; supplemented with rung 4 (device structure/crash-free) for the regression-scope this Gate-1 run adds (all 4 modified production files back several gallery entries: `ChipsFamilyScreen`'s `AppChip`/`TagChipWithContextMenu` demos, `CardsFamilyScreen`'s `CardTagRow` demo).
- **Target:** headless (api.txt diff) + device (yahirs-s22-ultra-2, real hardware).
- **Expected:** per ROADMAP SC1 + `07-01-PLAN.md` must_haves: `TagChipUiModel.color: Color? = null` exists as an additive field; `AppChip`/`TagChipWithContextMenu` both gain `containerColorOverride: Color? = null`; every existing call site (all named-arg) compiles unchanged; the gallery entries these files back keep building/installing/launching with zero regression.
- **Arranged (seeded):** none (static compiled-in registry/fakes) — Arrange was environmental: deleted stale mavenLocal artifact, `./gradlew publishReleasePublicationToMavenLocal` from clean HEAD `418dd3b`, harness rebuilt+installed on `R5CT10XNKQN` only.
- **Did (drove):** Independently re-diffed the regenerated `api.txt` against the TRUE pre-Phase-7 baseline (`git diff 8d692e5~1 -- api.txt`, i.e. before the phase's first commit — not against the phase's own already-mutated committed copy, the exact independence gap `07-VERIFICATION.md`/WR-01 flagged and required for a genuine check). Launched the harness (`am start .../.MainActivity` → same-package Intent into `ExplorerActivity`), navigated Index → Chips family → `AppChip` detail, Index → Chips family → `TagChipWithContextMenu` detail, Index → Cards family → `CardTagRow` detail — the three gallery surfaces backed by this phase's touched files.
- **Observed:** `api.txt` diff shows exactly two additive lines for `AppChipKt.AppChip(...)` and `TagChipWithContextMenuKt.TagChipWithContextMenu(...)` — each gains one new trailing `optional androidx.compose.ui.graphics.Color? containerColorOverride` parameter, nothing removed/reordered. `TagChipUiModel` block gains only additions (2 new ctor overloads, `color` property, `Companion.of(...)` factory) — the `copy()`/`component1()`-`component5()` lines are absent from the diff entirely, confirming byte-identical (the WR-01 gap this phase's own Deviations log describes as fixed). On-device: all three gallery screens rendered and navigated with zero `FATAL`/`Exception` lines in the full-session logcat.
- **Evidence:** `git diff 8d692e5~1 -- api.txt` output (this run, captured in this session — not a file artifact); `uiautomator` XML dumps `07-01-index.xml`, `07-02-chips-family.xml`, `07-03-appchip-detail.xml`, `07-06-back-to-chips.xml`, `07-07-tagchipmenu-detail.xml`, `07-09-back-index.xml`, `07-10-cards-family.xml`, `07-11-cardtagrow-detail.xml` (all in this session's scratchpad, not committed — see driver-mechanism note); full-session `adb logcat -d` capture (0 FATAL/Exception lines).

### 2. A `CardTagRow` test proves each `tag.color` auto-threads to the rendered chip's `containerColorOverride` (both `TagChipWithContextMenu` and plain `AppChip` render paths) — ROADMAP SC2

result: passed

- **Rung:** 1 (unit test) — decisive; this is a source-structural wiring proof (Robolectric cannot render `CardTagRow`'s full card-face tree, per `07-01-PLAN.md`'s explicit design choice), not a rendered-pixel claim. Supplemented with rung 4 device evidence that the wiring executes without crashing for real card-face data.
- **Target:** headless (JVM/Robolectric) + device.
- **Expected:** `CardTagRowTest` proves `containerColorOverride = tag.color` is present in both the `TagChipWithContextMenu(` branch and the plain `AppChip(` (`!hasCapability`) branch, and absent from the "+N" overflow `AppChip(` call.
- **Arranged (seeded):** none.
- **Did (drove):** Re-ran `./gradlew testDebugUnitTest --tests "*CardTagRowTest*"` directly at HEAD `418dd3b` (not trusting the inherited SUMMARY verdict). On-device: navigated to Cards family → `CardTagRow` detail, observed the `Default` state cell and the `Variants` section ("CardTagRow — 4 tags (2 visible + \"+2\" overflow)").
- **Observed:** `CardTagRowTest` 3/3 pass (`failures="0" errors="0" skipped="0"`). On-device: the `CardTagRow` demo renders exactly `Work`, `Personal`, and a `+2` overflow chip in both the States/Default cell and the Variants section — matching the "2 visible + overflow" contract; all three chips render as plain theme-default outlined chips (fakes' `tag.color` is `null`, per this phase's own documented D5 scope boundary — not a defect), and no crash occurred navigating into or out of this screen.
- **Evidence:** `build/test-results/testDebugUnitTest/TEST-io.github.ygaray.yahirandroidtaste.component.CardTagRowTest.xml` (this run); `07-10-cards-family.xml`, `07-11-cardtagrow-detail.xml` uiautomator dumps; `07-11-cardtagrow-detail-small.jpg` screenshot (Work/Personal/+2 chips rendering correctly, no stray coloring).

### 3. An `AppChip` render test with a non-null override renders the overridden container, and the default-`null` path is byte-identical to today's theme-role rendering — ROADMAP SC3

result: passed

- **Rung:** 5 (visual capture) — required for the "byte-identical to today's rendering" regression-floor claim, which is inherently visual per the ladder (a structure-tree hit alone can't distinguish "rendered as designed" from "visually regressed").
- **Target:** device (yahirs-s22-ultra-2, real hardware) + headless unit test (the non-null-override render assertion itself).
- **Expected:** per `07-UI-SPEC.md`'s Color section: a resting (unselected, no relatedness) `AppChip`/`TagChipWithContextMenu` with `containerColorOverride == null` renders `colorScheme.surface`/`onSurfaceVariant`/1dp `outline` — identical to pre-phase; a non-null override would render as the container fill only in that same resting branch (not exercised live on-device today, since no gallery fake sets a non-null color — confirmed out-of-scope per the task's own D5 note); `AppChipTest`'s Compose-render tests assert both the non-null-override wiring and the omitted-default regression floor.
- **Arranged (seeded):** none.
- **Did (drove):** Re-ran `./gradlew testDebugUnitTest --tests "*AppChipTest*"` fresh at HEAD `418dd3b`. On-device: opened `AppChip` detail (Chips family), screenshotted the `Default` state cell (`Personal` chip, `containerColorOverride` unexercised/null) and the `Pressed / Selected` state cell (`Work` chip, `isSelected = true`) — both pre-existing demo cells, unaffected by this phase's own new parameter.
- **Observed:** `AppChipTest` 12/12 pass (including the two new tests this phase added: non-null override renders/clicks correctly, and the omitted-default regression floor). On-device screenshot `07-03-appchip-detail-small.jpg`: the `Default` cell's `Personal` chip renders as a plain outlined chip (`surface` fill, `outline` border) — matching the documented pre-phase theme-role rendering exactly, confirming the regression floor holds in the real running app, not just in the unit harness.
- **Evidence:** `build/test-results/testDebugUnitTest/TEST-io.github.ygaray.yahirandroidtaste.component.AppChipTest.xml` (this run); `07-03-appchip-detail-small.jpg` (Default/Pressed-Selected states, plain outlined + filled rendering, no visual regression).

### 4. The override loses to `isSelected`/`relatednessStrength` (theme roles win when selection is active) — asserted by test — ROADMAP SC4

result: passed

- **Rung:** 5 (visual capture) — this criterion is falsifiable live on-device: toggling `isSelected` in the gallery's interactive Playground must switch the container fill through the exact `containerColor` when-block this phase modified, proving the precedence order holds in the real running composable, not merely in the structural test's string-index assertion.
- **Target:** device (yahirs-s22-ultra-2, real hardware) + headless unit test (the structural precedence-order assertion).
- **Expected:** the `containerColor` when-block orders `isSelected -> relatedness != null -> containerColorOverride != null -> else`; toggling the Playground's "Selected" switch (which sets `isSelected` on the live `AppChip` instance) must visibly switch the chip's fill from the resting `surface`/outline look to the `secondaryContainer` filled look, live and crash-free.
- **Arranged (seeded):** none.
- **Did (drove):** Re-ran `./gradlew testDebugUnitTest --tests "*AppChipTest*"` (the precedence-order structural test is part of this suite, already re-confirmed above). On-device: in the `AppChip` detail screen's Playground, captured the "Selected" toggle at rest (off), then tapped it (`input tap 962 595`) and re-captured.
- **Observed:** Toggle-off screenshot (`07-03-appchip-detail-small.jpg`): Playground preview chip "Work" renders the resting outlined look. Toggle-on screenshot (`07-05-appchip-selected-toggle-small.jpg`, taken after `input tap` on the live switch): the same "Work" chip's container fill visibly changes to the filled, no-border `secondaryContainer`-style look — a live, adversarial confirmation that `isSelected` wins in the real running app, not just in a structural string-index test. Zero `FATAL`/`Exception` lines in logcat across the toggle interaction.
- **Evidence:** `build/test-results/testDebugUnitTest/TEST-io.github.ygaray.yahirandroidtaste.component.AppChipTest.xml`; `07-03-appchip-detail-small.jpg` (toggle off) and `07-05-appchip-selected-toggle-small.jpg` (toggle on) — the falsifying before/after pair.

### 5. No new public composables are added; `api.txt` is updated additively and `apiCheck`, both drift guards, and zero-baseline `detekt` stay green — ROADMAP SC5

result: passed

- **Rung:** 3 (headless data check) + 1 (unit test) — independently re-derived, not trusted from `07-01-SUMMARY.md`'s inherited claim (the SUMMARY itself documents a two-stage history here: an initial `apiCheck`-accepted-but-not-genuinely-additive state, code-reviewed as WR-01, then fixed by moving `color` out of the primary constructor — this run re-verifies the FIXED state independently, against the TRUE pre-phase baseline, not the phase's own already-mutated one).
- **Target:** headless.
- **Expected:** `./gradlew apiCheck detekt publishReleasePublicationToMavenLocal` all succeed; `ComponentRegistryDriftGuardTest`/`DomainVocabularyDriftGuardTest` pass (no new public composable registered/unregistered); `api.txt`'s diff against the true pre-Phase-7 baseline is additive-only.
- **Arranged (seeded):** none.
- **Did (drove):** Ran `./gradlew apiCheck detekt publishReleasePublicationToMavenLocal` directly at HEAD `418dd3b`; ran `./gradlew testDebugUnitTest --tests "*ComponentRegistryDriftGuardTest*" --tests "*DomainVocabularyDriftGuardTest*"`; independently diffed `api.txt` against `8d692e5~1` (the true pre-Phase-7 commit, not `07-01-SUMMARY.md`'s own already-mutated `000bf89~1` reference point — an even stricter independence check).
- **Observed:** `BUILD SUCCESSFUL` for all three gradle invocations. `build/reports/detekt/detekt.txt` is 0 lines (zero findings; the phase's documented `CyclomaticComplexMethod` threshold tune 25→26 for the unrelated, untouched `TextCard.kt` — confirmed still untouched by this phase via the file list in `07-01-PLAN.md`'s frontmatter — means detekt is genuinely green today, with no override needed, unlike the analogous Phase 6 SC5 case). `ComponentRegistryDriftGuardTest` 1/1, `DomainVocabularyDriftGuardTest` 2/2 pass — confirming no new public composable was registered or left unregistered. The independent `api.txt` diff against `8d692e5~1` (shown in Criterion 1's evidence) confirms zero removed/changed lines anywhere in the file — purely additive.
- **Evidence:** gradle console output (`BUILD SUCCESSFUL`, this run); `build/reports/detekt/detekt.txt` (0 lines); `build/test-results/testDebugUnitTest/TEST-*ComponentRegistryDriftGuardTest*.xml`, `TEST-*DomainVocabularyDriftGuardTest*.xml`; `git diff 8d692e5~1 -- api.txt` output (this run).

## Summary

total: 5
passed: 5
partial: 0
failed: 0
infra: 0

## Notes / anomalies (for the Gate-2 reviewer)

- **Held-out by design, not a gap:** ROADMAP SC1-5 are all satisfied; the one thing this Gate-1 run explicitly does NOT (and per `07-UI-SPEC.md`/`07-01-SUMMARY.md` D5, cannot) verify is the rendered *legibility* of a caller-supplied non-null `containerColorOverride` — `ExplorerFakeData.kt`'s tag fakes all leave `color = null`, so no chip in this hub's own gallery ever paints a non-default color today. That is contractually SecondBrain's own muted/theme-aware policy, verifiable only once a real consumer wires a non-null value. Confirmed this is the correct, documented scope boundary (not an oversight) by reading `07-UI-SPEC.md`'s UI Considerations table and `07-01-SUMMARY.md`'s `coverage: D5` entry before treating "no visibly colored chip" as anything other than expected.
- **No project-local `AGENT-DEVICE-TESTING.md` exists yet** for this pure-library repo (same gap flagged in `01-05-SELF-UAT.md` and `06-03-SELF-UAT.md`). This run reused the same throwaway same-package-Intent harness a fourth time (Phases 1, 6, and now 7), republishing the AAR fresh from HEAD rather than trusting the cached one from earlier in this session — the backlog item recommending a committed harness + project-local playbook remains a good investment.
- Confirmed independently (not trusted from the SUMMARY) that the WR-01 `copy()` ABI-break gap flagged by code review is genuinely closed: the `api.txt` diff against the TRUE pre-Phase-7 baseline (`8d692e5~1`, one commit before this phase's first commit) shows zero changed/removed lines for `TagChipUiModel`, including `copy()` and `componentN()` — only additions.
- The harness app and its build artifacts are **not committed** — throwaway UAT infra only, per the diagnose-only tester contract. Uninstalled from the device at run end (`pm list packages` confirms absence); the personal phone was never targeted (confirmed both before and after).

## Findings routed to gap-closure (if any)

None — all 5 criteria are genuine PASSes, confirmed adversarially: the `api.txt` additivity claim was re-diffed against the TRUE pre-phase baseline (not the phase's own already-mutated committed copy, the exact gap the phase's own code review required closing), and the `isSelected`-wins-over-override precedence claim was falsified live on-device via a toggle-driven before/after screenshot pair rather than trusted from the structural unit test alone.

## Verdict

All criteria PASS → Gate-1 complete; human Gate-2 deferred to milestone completion (registered in `.planning/uat-pending/07-chip-color-slot.md` → `HUMAN-UAT-PENDING.md`).
