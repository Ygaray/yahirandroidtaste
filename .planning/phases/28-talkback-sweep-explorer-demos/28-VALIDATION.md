---
phase: "28"
slug: "talkback-sweep-explorer-demos"
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
# audit-milestone §5.5 distinguishes NOT-VALIDATED (draft) from PARTIAL (validated + nyquist_compliant: false) (#2117)
status: draft
nyquist_compliant: false
wave_0_complete: false
created: "2026-10-17"
---

# Phase 28 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.
> Source of truth for the test map: `28-RESEARCH.md` § Validation Architecture.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 4.13.2 + Robolectric 4.16.1 + `createComposeRule` (ui-test 1.11.0) |
| **Config file** | `build.gradle.kts`; detekt `config/detekt/detekt.yml` + `config/detekt-compose.yml` (zero baseline) |
| **Quick run command** | `./gradlew testDebugUnitTest --tests '*PlaceMapOsmdroidConfig*' --tests '*PlaceMapTilePurge*' --tests '*DemoRegistryRender*' --tests '*ReceiptDemosRegistryRenderTest' --tests '*ExplorerSchemePinGuardTest' --no-daemon` |
| **Full suite command** | `./gradlew testDebugUnitTest detekt apiCheck --rerun --no-build-cache --no-daemon`, then `git diff --stat api.txt` (empty), `bash tools/verify-api-source-additive.sh v2.7.1`, `bash tools/test/run-all.sh` |
| **Estimated runtime** | quick ~60-120 s; full ~400 s |

Rules: one Gradle invocation at a time, always `--no-daemon`, never `--stop`, never `--offline`.

---

## Sampling Rate

- **After every task commit:** the narrowest `--tests` set for the touched area
- **After every plan wave:** guard run (`*CatalogDocsDriftGuardTest`, `*DomainVocabularyDriftGuardTest`, `*ComponentRegistryDriftGuardTest`, `*ComponentStatesMatrixTest`, `*ComponentPlaygroundIntegrityTest`, `*LibraryInvariantsGuardTest`) plus quick run
- **Before `/gsd-verify-work`:** full suite fresh must be green
- **Max feedback latency:** ~420 seconds

---

## Per-Task Verification Map

Filled by the planner/executor per task; the requirement-to-test map is in `28-RESEARCH.md` § Validation Architecture (POL-03, DEMO-02, DEMO-03, A11Y-02 rows) and in each PLAN.md `<verify>` block.

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 28-01-01 | 28-01 | 1 | POL-03 | T-28-05 | persisted UA overwritten in memory, never rewritten; exactly one User-Agent entry | Robolectric | `./gradlew testDebugUnitTest --tests '*PlaceMapOsmdroidConfigTest' --tests '*PlaceMapPickerTest' --no-daemon` | extend existing | ⬜ pending |
| 28-01-02 | 28-01 | 1 | POL-03 | T-28-01, T-28-02, T-28-03 | control characters stripped; blank/bare/placeholder agent expanded or warned once | Robolectric | `./gradlew testDebugUnitTest --tests '*PlaceMapOsmdroidConfigTest' --tests '*PlaceMapPickerTest' --tests '*LibraryInvariantsGuardTest' --no-daemon` | extend existing | ⬜ pending |
| 28-01-03 | 28-01 | 1 | POL-03 | T-28-04 | Explorer uses a hub-owned real agent; one-tile host probe of the exact strings | unit + host curl | `./gradlew testDebugUnitTest --tests '*ExplorerMapUserAgentTest' --no-daemon` and `bash tools/probe-osm-user-agent.sh "<agent>"` | ❌ W0 | ⬜ pending |
| 28-02-01 | 28-02 | 2 | POL-03 | T-28-06, T-28-07 | only size AND md5 matched rows deleted; bound arguments; best-effort | Robolectric real SQLite | `./gradlew testDebugUnitTest --tests '*PlaceMapTilePurgeTest' --no-daemon` | ❌ W0 | ⬜ pending |
| 28-02-02 | 28-02 | 2 | POL-03 | T-28-07, T-28-08 | scan once per process and per install flag; never keyed on an agent change | Robolectric | `./gradlew testDebugUnitTest --tests '*PlaceMapOsmdroidConfigTest' --no-daemon` | extend existing | ⬜ pending |
| 28-02-03 | 28-02 | 2 | POL-03 | T-28-10 | offline cache check: zero placeholder rows and at least one real tile | shell fixture | `bash tools/test/test-verify-osm-tile-cache.sh` | ❌ W0 | ⬜ pending |
| 28-03-01 | 28-03 | 3 | DEMO-02 | T-28-11 | ExplorerTheme resolves the hub scheme under a foreign outer scheme | Robolectric | `./gradlew testDebugUnitTest --tests '*ExplorerThemeTest' --tests '*ReceiptDemosRegistryRenderTest' --no-daemon` | ❌ W0 | ⬜ pending |
| 28-03-02 | 28-03 | 3 | DEMO-02 | T-28-11 | index, token browser and five family screens pinned | Robolectric | `./gradlew testDebugUnitTest --tests '*ExplorerThemeTest' --tests '*ComponentStatesMatrixTest' --no-daemon` | exists | ⬜ pending |
| 28-03-03 | 28-03 | 3 | DEMO-02 | T-28-11 | source-scan guard: no direct library-theme wrap in explorer/ | plain JVM | `./gradlew testDebugUnitTest --tests '*ExplorerSchemePinGuardTest' --no-daemon` | ❌ W0 | ⬜ pending |
| 28-04-01 | 28-04 | 4 | DEMO-03 | T-28-13, T-28-14 | English stage: fire one, tap opens, key readout | Robolectric | `./gradlew testDebugUnitTest --tests '*FeedbackStackDemoRegistryRenderTest' --no-daemon` | ❌ W0 | ⬜ pending |
| 28-04-02 | 28-04 | 4 | DEMO-03 | T-28-14, T-28-15 | burst past the cap, swipe, Undo, German stage at 360dp | Robolectric | `./gradlew testDebugUnitTest --tests '*FeedbackStackDemoRegistryRenderTest' --tests '*LibraryInvariantsGuardTest' --no-daemon` | ❌ W0 | ⬜ pending |
| 28-04-03 | 28-04 | 4 | DEMO-03 | T-28-14 | Undo Center target rows EN and DE; registry and CATALOG guards green | Robolectric | `./gradlew testDebugUnitTest --tests '*UndoCenterTargetRowsDemoRegistryRenderTest' --tests '*ComponentRegistryDriftGuardTest' --tests '*CatalogDocsDriftGuardTest' --no-daemon` | ❌ W0 | ⬜ pending |
| 28-05-01 | 28-05 | 5 | DEMO-03 | T-28-16, T-28-18 | Receipt single go-to-item: hide() then key readout | Robolectric | `./gradlew testDebugUnitTest --tests '*GoToItemSheetDemosRegistryRenderTest' --tests '*ReceiptDemosRegistryRenderTest' --no-daemon` | ❌ W0 | ⬜ pending |
| 28-05-02 | 28-05 | 5 | DEMO-03 | T-28-17 | mixed batch, Classic, German at 360dp | Robolectric | `./gradlew testDebugUnitTest --tests '*GoToItemSheetDemosRegistryRenderTest' --tests '*LibraryInvariantsGuardTest' --no-daemon` | ❌ W0 | ⬜ pending |
| 28-05-03 | 28-05 | 5 | DEMO-02, DEMO-03 | T-28-18 | hoisted demo closes through hide(); registry story of Phases 25-27 verified | Robolectric + guards | `./gradlew testDebugUnitTest --tests '*HoistedSheetDemoRegistryRenderTest' --tests '*ComponentRegistryDriftGuardTest' --no-daemon` | ❌ W0 | ⬜ pending |
| 28-06-01 | 28-06 | 6 | A11Y-02 | T-28-21 | ReceiptRow, refused live region, saved-key summary, sheet row label verbatim | Robolectric | `./gradlew testDebugUnitTest --tests '*A11ySweepSemanticsTest' --no-daemon` | ❌ W0 | ⬜ pending |
| 28-06-02 | 28-06 | 6 | A11Y-02 | T-28-19, T-28-20 | group header is a heading; openable Undo Center row is one merged button (C-2) | Robolectric | `./gradlew testDebugUnitTest --tests '*A11ySweepUndoCenterSemanticsTest' --tests '*UndoCenterScreen*' --no-daemon` | ❌ W0 | ⬜ pending |
| 28-06-03 | 28-06 | 6 | A11Y-02 | T-28-21 | snackbar card label verbatim; locked D-11/D-07 forms pinned; full Layer-1 command | Robolectric | the A11Y Layer-1 command above | ❌ W0 | ⬜ pending |
| 28-07-01 | 28-07 | 7 | POL-03, DEMO-03, DEMO-02 | T-28-03, T-28-07 | phase hygiene: no wipe, no UA prefs key, one placeholder literal, nothing public | plain JVM | `./gradlew testDebugUnitTest --tests '*Phase28HygieneSourceContractTest' --no-daemon` | ❌ W0 | ⬜ pending |
| 28-07-02 | 28-07 | 7 | A11Y-02 | T-28-22, T-28-23 | read-only USB-only restore oracle | shell fixture (fake adb) | `bash tools/test/test-verify-tester-restored.sh` | ❌ W0 | ⬜ pending |
| 28-07-03 | 28-07 | 7 | A11Y-02, DEMO-02, DEMO-03, POL-03 | T-28-24, T-28-26 | fresh full gate; api.txt unchanged since c1681ff; locked files untouched | gradle + scripts | `./gradlew apiDump testDebugUnitTest detekt apiCheck --rerun-tasks --no-build-cache --no-daemon` | exists | ⬜ pending |
| 28-07-G1 | 28-07 | after 7 | A11Y-02, DEMO-02, DEMO-03, POL-03 | T-28-22..T-28-26 | Gate-1 on the TESTER (G1-0 to G1-8), restore, parking of P-1 to P-6 only | device (autonomous gate) | `verify_work_agentic_gate` per the contract in 28-07-PLAN.md | gate-authored | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] `explorer/ExplorerTheme.kt` helper + `ExplorerSchemePinGuardTest` — DEMO-02
- [ ] `FeedbackStackDemoRegistryRenderTest`, `GoToItemDemosRegistryRenderTest`, `HoistedSheetDemoRegistryRenderTest` — DEMO-02, DEMO-03
- [ ] `A11ySweepSemanticsTest` (incl. the missing `Heading` assertion) — A11Y-02
- [ ] `PlaceMapTilePurgeTest` + real placeholder PNG fixture (6987 B, md5 `c069a15b2cc2d6b6f527ad09eb93c61a`) — POL-03
- [ ] Widen `LibraryInvariantsGuardTest` demo-copy scan if demo copy leaves `ReleaseDemos.kt`

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Spoken TalkBack read-out, reading feel, double Dismiss listing | A11Y-02 | Speech output cannot be asserted from adb | Parked in `.planning/uat-pending/` / `HUMAN-UAT-PENDING.md` for Yahir; never waived. The agentic Gate-1 covers the structural layer on the TESTER (`R5CT10XNKQN`) |

---

## Validation Sign-Off

> **Plan-time state is a DRAFT.** Leave frontmatter `status: draft` and `nyquist_compliant: false`.
> These are finalized ONLY post-execution by the Nyquist finalizer.

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 420s
- [ ] _(finalizer-only, post-execution)_ `nyquist_compliant` — leave `false` at plan time

**Approval:** pending — finalizer-owned, not set at plan time
