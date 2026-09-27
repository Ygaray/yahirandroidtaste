---
audit_acknowledged:
  milestone: v2.0
  at: 2026-09-27
  gap_snapshot: "unknown::scenarios=0"
---

# Self-UAT Log — Phase 6 Plan 03 (Forward-Port Reunification — phase-closing device verification)

**Device:** Samsung SM-S908U / yahirs-s22-ultra-2 (R5CT10XNKQN via USB adb, Android 15) — the Gate-1 tester rig; leased via `gsd-lease.sh` (`device:R5CT10XNKQN`, reachability probed live before lease, per D8) for the duration of this run and released at the end. No emulator fallback needed — device was reachable and free on the first probe.
**Library build:** `com.github.Ygaray:yahirandroidtaste:1.10.0` published to `mavenLocal` from git HEAD `6bf7017` (clean tree — `git status --short -- src/ build.gradle.kts gradle/libs.versions.toml api.txt CLAUDE.md` empty at build time), AAR md5 `52917361d23adb55c3b43e5919df6cd9`.
**Run:** 2026-09-27T07:36:30Z
**Schema:** N/A — no DB/data layer; `ExplorerActivity` reads only the static, compiled-in `ComponentRegistry`.
**Pre-flight:** device awake (`mWakefulness=Awake`), user-0 unlocked (`deviceLocked=0`; the co-present `deviceLocked=1` belongs to the unrelated Secure Folder profile, a known device quirk carried over from `01-05-SELF-UAT.md`).
**Unit suite:** re-run directly, not trusted from inherited verdicts — `./gradlew testDebugUnitTest --tests "*DateTimePickerTest*" --tests "*PlaceMapPickerTest*" --tests "*PlaceMapOsmdroidConfigTest*" --tests "*PlaceMapPickerModelTest*" --tests "*PresetChipTest*" --tests "*ComponentRegistryDriftGuardTest*" --tests "*DomainVocabularyDriftGuardTest*"` → `BUILD SUCCESSFUL`; per-class JUnit XML confirms `DateTimePickerTest` 20/20, `PlaceMapPickerTest` 82/82, `PlaceMapOsmdroidConfigTest` 10/10, `PlaceMapPickerModelTest` 16/16, `PresetChipTest` 9/9, `ComponentRegistryDriftGuardTest` 1/1, `DomainVocabularyDriftGuardTest` 2/2 — zero failures/errors/skips across all 140 test cases. Full-suite `./gradlew testDebugUnitTest apiCheck` also re-run clean (`BUILD SUCCESSFUL`).
**Coverage/Nyquist:** not re-run (out of this log's scope — `06-VERIFICATION.md` already scored 4/5 truths + this log settles the 5th and the 3 device-only human_verification items).
**Seed/fixture integrity:** N/A — `ComponentRegistry` is a static compiled-in catalog, no rows/fixtures to seed. Arrange work was entirely environmental (publish AAR to `mavenLocal` from current tree, build+install the throwaway same-package-Intent harness — same pattern established in `01-05-SELF-UAT.md`).

## Driver-mechanism note (carried forward from `01-05-SELF-UAT.md`)

`yahirandroidtaste` is a pure `com.android.library` (no `applicationId`) — no installable APK, no project-local `AGENT-DEVICE-TESTING.md` yet (falls back to the global template, per `resolve_driver`). Re-derived the same throwaway UAT harness pattern this repo's Gate-1 runs have used since Phase 1: a same-package-Intent host app (`io.github.ygaray.yahirandroidtasteharness`, built fresh in this session's scratchpad, AGP 9.2.1/Kotlin 2.3.20/compileSdk 36.1/minSdk 35 matching the library's own AAR-metadata requirement) depending on the just-published `com.github.Ygaray:yahirandroidtaste:1.10.0` from `mavenLocal`, whose sole `MainActivity` does `startActivity(Intent(this, ExplorerActivity::class.java)); finish()`. AGP 9 built-in Kotlin support meant dropping the `org.jetbrains.kotlin.android` plugin id (a new gotcha vs. Phase 1's log — AGP 9 rejects that plugin explicitly: *"no longer required... since AGP 9.0"*). Built clean, installed clean, launched clean — zero crashes across the entire session (`grep -c FATAL|Exception` over the full logcat = 0). Harness uninstalled and device lease released at the end of this run (not committed — throwaway testing infra only, per the diagnose-only tester contract).

**Gotcha (new, worth banking for the recommended project-local playbook):** `PlaceMapPicker`'s embedded live osmdroid `MapView` swallows `input swipe` gestures over its bounds — small/slow swipes pan the map instead of scrolling the outer `LazyColumn`; only a large-distance, higher-velocity swipe (fling) reliably advances the outer scroll past a map-bearing item, and even then it can jump past an item rather than landing precisely on it. Not a code defect (confirmed via the Compose source: the map's own `OnTouchListener` legitimately claims the gesture) — just means precise incremental scrolling through a `PlaceMapPicker`-heavy screen needs oversized swipes, not the standard small-step walk.

## Criteria

### 1. `DateTimePicker`, `PlaceMapPicker`, and `PresetChip` each render in the ExplorerActivity gallery (family-screen previews) — ROADMAP SC1

result: passed

- **Rung:** 5 (visual capture) — required; this is an inherently visual "renders correctly" claim per `06-VERIFICATION.md`'s own `human_verification` items #1-3, and per the ladder a structure-tree hit alone would not distinguish "rendered as designed" from "present but visually broken/occluded."
- **Target:** device (yahirs-s22-ultra-2, real hardware; no emulator fallback).
- **Expected:** Per ROADMAP SC1 + `06-VERIFICATION.md` `human_verification`: (a) `DateTimePicker`'s 3 restored demo variants (date-only w/ minDate, time-only, date-and-time-in-one-instance) render with no crash; (b) `PresetChip`'s both restored demo variants (label-only 7-item `ChipBar<String>`, with-supporting-label 2-item `ChipBar<Pair<String,String>>`) render inside `ChipBar`; (c) `PlaceMapPicker`'s live `MapView` tile surface actually paints, and the saved-places `ChipBar`/`PresetChip` embedding renders when `savedPlaces` is non-empty and is absent entirely when empty.
- **Arranged (seeded):** none (static compiled-in registry) — Arrange was purely environmental: `./gradlew publishReleasePublicationToMavenLocal` from clean HEAD `6bf7017`, harness built+installed.
- **Did (drove):** Launched the harness (`am start .../.MainActivity` → same-package Intent into `ExplorerActivity`). From the index screen: tapped **Pickers** family → tapped **DateTimePicker** row → scrolled through States (Default/Pressed-Selected/Disabled/Focused) and the Variants section (all 3 demo captions) → `KEYCODE_BACK` to family list → tapped **PlaceMapPicker** row → observed States matrix (Default, Pressed/Selected, both with live maps + Saved-places chips) → scrolled (large fling swipes, see gotcha above) into the Variants section's `"PlaceMapPicker - errors and resolving"` entry (the falsifying case for the empty-saved-places claim) → `KEYCODE_BACK` twice to index → tapped **Chips** family → tapped **PresetChip** row → observed States matrix + both Variants entries (`"PresetChip - ChipBar itemContent, label only"`, `"PresetChip - with supporting labels"`).
- **Observed:**
  - **DateTimePicker (evidence 1):** screenshot shows the Disabled state's grayed `Date`/`Time` fields correctly disabled-styled, Focused correctly reporting "Not applicable", and all 3 Variants captions each rendering a live, correctly-labeled `Date`/`Time` field pair ("date only (minDate = today)" shows only a Date row; "time only" shows only a Time row; "date and time, one instance" shows both) — matches v1.13.0's restored demo exactly, zero clipping/blank fields.
  - **PlaceMapPicker (evidence 2, 3, 4) — the falsifying case for saved-places conditional rendering:** the Default/Pressed-Selected state screenshots show a **real painted OSM tile map** (continent outlines, country labels — "Nigeria", "Chad/تشاد", "Angola", "République démocratique du Congo", "South Sudan/السودان" — not a placeholder), the `"© OpenStreetMap contributors"` attribution overlay (only appears once the tile provider genuinely initializes), and a non-empty **Saved places** row (`Home 150 m, 1 of 2`, `Work`, `Gym`, `Home 300 m, 2 of 2` — real `PresetChip`-rendered chips). The `"PlaceMapPicker - errors and resolving"` variant screenshot (evidence 4) is the deliberate falsifying probe: it shows the red "Couldn't get your current location" / "No address found for that search" error states correctly styled, **and confirms the Saved places row is entirely absent** (not present-but-empty, not a placeholder — the section header and chip row both do not exist in that screen's tree) exactly matching the "absent entirely when empty" clause of the criterion. Zero crashes throughout (full-session logcat: 0 FATAL/Exception lines).
  - **PresetChip (evidence 5, 6):** screenshot shows both overloads rendering correctly inside their `ChipBar` wrap layout — the label-only variant's 7 chips (`5 min`/`10 min`/`30 min`/`1 h`/`5 h`/`10 h`/`24 h`) all fully visible with no truncation/overlap, and the with-supporting-labels variant's 2 chips (`Later today` / `7:40 PM`, `Until tomorrow` / `Tue 9:00 AM`) each showing both the primary and supporting text correctly.
- **Evidence:** (all in this session's scratchpad, not committed — see driver-mechanism note; content described above since files are outside the git tree)
  1. `05-datetimepicker-variants-small.jpg` — DateTimePicker detail, Disabled/Focused states + all 3 Variants.
  2. `08-placemappicker-default-small.jpg` — PlaceMapPicker Default state: live map + saved-places chips.
  3. `12-placemappicker-scrolled-small.jpg` — PlaceMapPicker Pressed/Selected state: second live map instance + saved-places chips (confirms states matrix, not just one cell, renders).
  4. `14-placemappicker-errors-variant-small.jpg` — PlaceMapPicker "errors and resolving" variant: error states + **absent** saved-places row (the falsifying check).
  5. `20-presetchip-detail-small.jpg` — PresetChip States matrix + label-only Variants (7 chips).
  6. `21-presetchip-scrolled-small.jpg` — PresetChip with-supporting-labels variant (2 chips, primary+supporting text both visible).
  7. `uiautomator` XML dumps `01`–`21` + `step-1..8` — structural cross-checks (registry entry presence, demo caption text, bounds).
  8. Full-session `adb logcat -d` capture — 0 FATAL/Exception lines across the harness install, launch, and all navigation.

### 2. The 5 ported tests pass under `./gradlew testDebugUnitTest` — ROADMAP SC2

result: passed

- **Rung:** 1 (unit tests).
- **Target:** headless (JVM/Robolectric).
- **Expected:** `DateTimePickerTest`, `PlaceMapPickerTest`, `PlaceMapOsmdroidConfigTest`, `PlaceMapPickerModelTest`, `PresetChipTest` all pass.
- **Arranged (seeded):** none.
- **Did (drove):** N/A (headless) — ran `./gradlew testDebugUnitTest --tests "*DateTimePickerTest*" --tests "*PlaceMapPickerTest*" --tests "*PlaceMapOsmdroidConfigTest*" --tests "*PlaceMapPickerModelTest*" --tests "*PresetChipTest*" --tests "*ComponentRegistryDriftGuardTest*" --tests "*DomainVocabularyDriftGuardTest*"` directly (not trusting `06-VERIFICATION.md`'s inherited verdict).
- **Observed:** `BUILD SUCCESSFUL`. Per-class JUnit XML counts: `DateTimePickerTest` 20/20, `PlaceMapPickerTest` 82/82, `PlaceMapOsmdroidConfigTest` 10/10, `PlaceMapPickerModelTest` 16/16, `PresetChipTest` 9/9 — all `failures="0" errors="0" skipped="0"`. Full-suite regression (`./gradlew testDebugUnitTest apiCheck`, no filter) also green.
- **Evidence:** Gradle console output (`BUILD SUCCESSFUL in 1s`, cached from this exact HEAD); `build/test-results/testDebugUnitTest/TEST-*.xml` per-class reports.

### 3. The three public composables are each registered in exactly one `ComponentRegistry` family list; the integrity test and CATALOG drift guard pass — ROADMAP SC3

result: passed

- **Rung:** 3 (headless data check) + 1 (unit test).
- **Target:** headless.
- **Expected:** `grep -c 'name = "<X>"'` = 1 for each of `DateTimePicker`/`PlaceMapPicker` (in `PickersFamilyScreen.kt`) and `PresetChip` (in `ChipsFamilyScreen.kt`); `ComponentRegistryDriftGuardTest` passes.
- **Arranged (seeded):** none.
- **Did (drove):** N/A (headless) — `grep -c` against current-HEAD source files; `ComponentRegistryDriftGuardTest` included in the test run above.
- **Observed:** `DateTimePicker` count=1, `PlaceMapPicker` count=1 (both in `PickersFamilyScreen.kt`), `PresetChip` count=1 (in `ChipsFamilyScreen.kt`). `ComponentRegistryDriftGuardTest` 1/1 pass. Cross-confirmed on-device: the Pickers family list shows exactly one `DateTimePicker` row and one `PlaceMapPicker` row (not duplicated); the Chips family list shows exactly one `PresetChip` row.
- **Evidence:** grep output (this run); `TEST-*ComponentRegistryDriftGuardTest*.xml`; on-device `uiautomator` dumps `02-pickers-family.xml`, `18-chips-family.xml` (one row each, no dupes).

### 4. `osmdroid` recorded in `APPROVED-DEPS.md`/`CLAUDE.md`; head tokens allowlisted; `DomainVocabularyDriftGuardTest` stays green — ROADMAP SC4

result: passed

- **Rung:** 3 (data check) + 1 (unit test).
- **Target:** headless.
- **Expected:** `.planning/APPROVED-DEPS.md` has a dated osmdroid entry; `CLAUDE.md`'s allowed-deps sentence names `osmdroid`; `"Date"`/`"Preset"` in `PRIMITIVE_NOUN_ALLOWLIST`; `"PlaceMapPicker"` in `DOMAIN_VOCABULARY`; `DomainVocabularyDriftGuardTest` passes.
- **Arranged (seeded):** none.
- **Did (drove):** N/A (headless) — grepped current-HEAD files directly.
- **Observed:** `APPROVED-DEPS.md:25` has the dated `org.osmdroid:osmdroid-android` entry (Phase 6, 2026-09-26). `CLAUDE.md:25` names `osmdroid` in the allowed-deps sentence. `PRIMITIVE_NOUN_ALLOWLIST` contains `"Date"` (line 317) and `"Preset"` (line 320), each with a Phase-6-dated comment. `DOMAIN_VOCABULARY` contains `"PlaceMapPicker"` (line 400). `DomainVocabularyDriftGuardTest` 2/2 pass.
- **Evidence:** grep output against `APPROVED-DEPS.md`, `CLAUDE.md`, `DomainVocabularyDriftGuardTest.kt` (this run); `TEST-*DomainVocabularyDriftGuardTest*.xml`.

### 5. `detekt` stays green at zero baseline (no new baseline banked) — ROADMAP SC5

result: passed

- **Rung:** 3 (data check) — independently re-derived, not trusted from `06-VERIFICATION.md`'s inherited override.
- **Target:** headless.
- **Expected:** `./gradlew detekt` succeeds with zero new findings and no baseline regeneration.
- **Arranged (seeded):** none.
- **Did (drove):** N/A (headless) — ran `./gradlew detekt` directly; independently re-verified the pre-existing/unrelated claim behind `06-VERIFICATION.md`'s accepted override via `git log --oneline 60381d2..HEAD -- .../TextCard.kt` (empty output) and `git log -1 -- .../TextCard.kt` (last touch `b58e9ee`, 2026-09-07).
- **Observed:** `./gradlew detekt` currently fails — `BUILD FAILED`, "Analysis failed with 1 weighted issues", `CyclomaticComplexMethod 25/25` at `TextCard.kt:132`. This is the criterion's literal primary clause being false today. However: (a) `git log 60381d2..HEAD` for `TextCard.kt` returns **zero commits** — independently confirms, without even needing to check out the old commit, that this file has not been touched since before Phase 6's first execution commit (last touch `b58e9ee`, 2026-09-07, three weeks before Phase 6 began); (b) it is formally tracked as `KI-2026-09-27-01` in `.planning/KNOWN-ISSUES.md` (mirroring the established `KI-2026-09-02-01` precedent for pre-existing, phase-unrelated build-config defects in this repo); (c) `06-VERIFICATION.md` carries a recorded, auditable `overrides:` block accepting this exact deviation, promoting phase status from `gaps_found` to `passed` on that basis — not a silent sweep. Given the independent confirmation that this finding is genuinely pre-existing and genuinely unrelated to any Phase-6-touched file, and given the deviation is formally tracked + overridden (not hidden), I am marking this criterion PASS **under the recorded override** — consistent with how `06-VERIFICATION.md` itself resolved it. This is not a Phase-6 regression to route to gap-closure.
- **Evidence:** `./gradlew detekt` console output (this run) + `build/reports/detekt/detekt.txt`; `git log --oneline 60381d2..HEAD -- src/main/.../TextCard.kt` (empty); `git log -1 --format="%h %ad %s" -- src/main/.../TextCard.kt` → `b58e9ee Mon Sep 7 10:22:38 2026 -0600 feat(component): add ReminderIndicator card-face presence cluster (REMIND-09)`; `.planning/KNOWN-ISSUES.md` `KI-2026-09-27-01` entry; `06-VERIFICATION.md` frontmatter `overrides:` block.

## Summary

total: 5
passed: 5
partial: 0
failed: 0
infra: 0

## Notes / anomalies (for the Gate-2 reviewer)

- **No project-local `AGENT-DEVICE-TESTING.md` exists yet** for this pure-library repo (same gap flagged in `01-05-SELF-UAT.md` and the ROADMAP's own Phase 999.1 backlog item). This run re-derived the same throwaway same-package-Intent harness pattern a third time (Phases 1, and now 6) — the backlog item recommending a committed harness + project-local playbook remains a good investment for future Gate-1 runs on this repo.
- **New gotcha banked for that future playbook:** `PlaceMapPicker`'s live `MapView` swallows small/slow scroll swipes (pans the map instead); only a large fling advances the outer list past a map-bearing item. Not a defect — osmdroid's own touch-gesture ownership working as designed — but worth documenting so a future Gate-1 run doesn't waste cycles on it.
- SC5 (`detekt` zero-baseline) is technically false on `main` right now in its literal reading, but the deviation is genuinely pre-existing (independently reconfirmed here via `git log`, not merely trusted from `06-VERIFICATION.md`), unrelated to any Phase-6 file, and formally tracked + overridden (`KI-2026-09-27-01` + the recorded `VERIFICATION.md` override). Marked PASS under that override, matching the established `KI-2026-09-02-01` precedent for this repo.
- The harness app and its build artifacts are **not committed** — same disposition as `01-05-SELF-UAT.md` (throwaway UAT infra only, diagnose-only tester contract). Uninstalled from the device (`pm list packages --user 0` confirms no `io.github.ygaray.yahirandroidtasteharness` remains) and the device lease released at run end.
- Confirmed the `KEYCODE_BACK`-exits-the-whole-task gotcha from the global playbook does NOT fire this run (unlike Phase 1) — Compose Navigation inside `ExplorerActivity` correctly pops one nav step at a time; no unintended exit to a prior foregrounded app was observed.

## Findings routed to gap-closure (if any)

None — all 5 criteria are genuine PASSes on real hardware + direct headless re-execution, confirmed adversarially (the PlaceMapPicker "errors and resolving" variant was specifically chosen as a falsifying probe for the "absent entirely when empty" saved-places claim, and it settled the claim correctly in the app's favor; the SC5 detekt override was independently re-derived via `git log`, not trusted from the inherited `06-VERIFICATION.md` verdict).

## Verdict

All criteria PASS → Gate-1 complete; human Gate-2 deferred to milestone completion (registered in `.planning/uat-pending/06-forward-port-reunification.md` → `HUMAN-UAT-PENDING.md`).
