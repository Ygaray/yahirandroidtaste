---
phase: "6"
slug: "forward-port-reunification"
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
# audit-milestone §5.5 distinguishes NOT-VALIDATED (draft) from PARTIAL (validated + nyquist_compliant: false) (#2117)
status: draft
nyquist_compliant: false
wave_0_complete: false
created: "2026-09-26"
---

# Phase 6 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 4 (`junit:junit:4.13.2`) + Robolectric (`4.16.1`) + Compose UI Test (`androidx.compose.ui:ui-test-junit4`, via BOM `2026.04.01`) |
| **Config file** | `build.gradle.kts` (module-root, no separate test config file) |
| **Quick run command** | `./gradlew testDebugUnitTest --tests "*DateTimePickerTest*" --tests "*PlaceMapPickerTest*" --tests "*PlaceMapOsmdroidConfigTest*" --tests "*PlaceMapPickerModelTest*" --tests "*PresetChipTest*"` |
| **Full suite command** | `./gradlew testDebugUnitTest` (includes both drift guards + all 5 ported tests + every existing test) |
| **Estimated runtime** | ~90 seconds (quick), ~4 minutes (full) |

---

## Sampling Rate

- **After every task commit:** Run the specific ported test(s) for whichever file was just restored, plus `./gradlew detekt` on the touched files.
- **After every plan wave:** Run `./gradlew testDebugUnitTest` (full suite, both drift guards).
- **Before `/gsd-verify-work`:** Full suite green + `./gradlew apiCheck` green + `./gradlew publishReleasePublicationToMavenLocal` succeeds + device Gate-1 gallery render confirmed.
- **Max feedback latency:** 240 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 06-01-* | 01 | 1 | REUNI-01 | — | N/A | unit (Robolectric Compose UI) | `./gradlew testDebugUnitTest --tests "*DateTimePickerTest*"` | ✅ (restore from `v1.13.0`) | ⬜ pending |
| 06-02-* | 01 | 1 | REUNI-02 | T-06-01 | Tile fetch stays within `configureOsmdroid`'s existing non-bulk pattern | unit (Robolectric Compose UI + plain JUnit for the model) | `./gradlew testDebugUnitTest --tests "*PlaceMapPickerTest*" --tests "*PlaceMapOsmdroidConfigTest*" --tests "*PlaceMapPickerModelTest*"` | ✅ (restore from `v1.13.0`) | ⬜ pending |
| 06-03-* | 01 | 1 | REUNI-03 | — | N/A | unit (Robolectric Compose UI) | `./gradlew testDebugUnitTest --tests "*PresetChipTest*"` | ✅ (restore from `v1.13.0`) | ⬜ pending |
| 06-04-* | 01 | 1 | REUNI-04 | T-06-02 | Tiles cache to private `cacheDir` only; consumer declares `INTERNET` | unit (source-text scan guards) + manual doc edit | `./gradlew testDebugUnitTest --tests "*ComponentRegistryDriftGuardTest*" --tests "*DomainVocabularyDriftGuardTest*"` | ✅ (guards exist; new allowlist entries are the task) | ⬜ pending |
| 06-05-* | 01 | 1 | (all) | — | N/A | build-time gate | `./gradlew apiDump && ./gradlew apiCheck` | ✅ (metalava already wired; `api.txt` confirmed to contain none of the 3 new symbols today) | ⬜ pending |
| 06-06-* | 01 | 1 | (all) | — | N/A | build-time gate | `./gradlew detekt` | ✅ (already wired) | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

*None — existing test infrastructure (Robolectric + Compose UI Test, already configured for `sdk=[35]` across the module) and existing governance tests (both drift guards) fully cover this phase's requirements. The 5 test files themselves are restored, not authored fresh.*

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Gallery renders `DateTimePicker`, `PlaceMapPicker`, `PresetChip` in ExplorerActivity family-screen previews | REUNI-01, REUNI-02, REUNI-03 | Live `MapView` rendering (osmdroid tile fetch/draw) is device-only-verifiable, not resolvable from source or Robolectric | Launch `ExplorerActivity` on-device (Gate-1), navigate to Pickers/Chips families, confirm each of the 3 composables renders without crash and `PlaceMapPicker`'s `MapView` draws tiles |

---

## Validation Sign-Off

> **Plan-time state is a DRAFT.** Leave frontmatter `status: draft` and `nyquist_compliant: false`.
> These are finalized ONLY post-execution by the Nyquist finalizer (the `verify:post` →
> `validate-phase` hook, invoked by execute-phase `finalize_nyquist_validation` after Gate-1). Never
> set `nyquist_compliant: true` — or otherwise "sign off" compliance — at plan time, and do not let
> the plan-checker do so (INC-2026-07-27-01: a premature plan-time flip is what caused inconsistent
> COMPLIANT/PARTIAL milestone-audit states).

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 240s
- [ ] _(finalizer-only, post-execution)_ `nyquist_compliant` — leave `false` at plan time; the
      finalizer sets `true` iff its gap analysis finds zero gaps

**Approval:** pending — finalizer-owned, not set at plan time
