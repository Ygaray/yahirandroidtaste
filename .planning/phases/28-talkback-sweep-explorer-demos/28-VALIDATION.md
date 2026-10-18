---
phase: "28"
slug: "talkback-sweep-explorer-demos"
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
# audit-milestone §5.5 distinguishes NOT-VALIDATED (draft) from PARTIAL (validated + nyquist_compliant: false) (#2117)
status: validated
nyquist_compliant: true
wave_0_complete: true
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
| 28-01-01 | 28-01 | 1 | POL-03 | T-28-05 | persisted UA overwritten in memory, never rewritten; exactly one User-Agent entry | Robolectric | `./gradlew testDebugUnitTest --tests '*PlaceMapOsmdroidConfigTest' --tests '*PlaceMapPickerTest' --no-daemon` | extend existing | ✅ green |
| 28-01-02 | 28-01 | 1 | POL-03 | T-28-01, T-28-02, T-28-03 | control characters stripped; blank/bare/placeholder agent expanded or warned once | Robolectric | `./gradlew testDebugUnitTest --tests '*PlaceMapOsmdroidConfigTest' --tests '*PlaceMapPickerTest' --tests '*LibraryInvariantsGuardTest' --no-daemon` | extend existing | ✅ green |
| 28-01-03 | 28-01 | 1 | POL-03 | T-28-04 | Explorer uses a hub-owned real agent; one-tile host probe of the exact strings | unit + host curl | `./gradlew testDebugUnitTest --tests '*ExplorerMapUserAgentTest' --no-daemon` and `bash tools/probe-osm-user-agent.sh "<agent>"` | ❌ W0 | ✅ green |
| 28-02-01 | 28-02 | 2 | POL-03 | T-28-06, T-28-07 | only size AND md5 matched rows deleted; bound arguments; best-effort | Robolectric real SQLite | `./gradlew testDebugUnitTest --tests '*PlaceMapTilePurgeTest' --no-daemon` | ❌ W0 | ✅ green |
| 28-02-02 | 28-02 | 2 | POL-03 | T-28-07, T-28-08 | scan once per process and per install flag; never keyed on an agent change | Robolectric | `./gradlew testDebugUnitTest --tests '*PlaceMapOsmdroidConfigTest' --no-daemon` | extend existing | ✅ green |
| 28-02-03 | 28-02 | 2 | POL-03 | T-28-10 | offline cache check: zero placeholder rows and at least one real tile | shell fixture | `bash tools/test/test-verify-osm-tile-cache.sh` | ❌ W0 | ✅ green |
| 28-03-01 | 28-03 | 3 | DEMO-02 | T-28-11 | ExplorerTheme resolves the hub scheme under a foreign outer scheme | Robolectric | `./gradlew testDebugUnitTest --tests '*ExplorerThemeTest' --tests '*ReceiptDemosRegistryRenderTest' --no-daemon` | ❌ W0 | ✅ green |
| 28-03-02 | 28-03 | 3 | DEMO-02 | T-28-11 | index, token browser and five family screens pinned | Robolectric | `./gradlew testDebugUnitTest --tests '*ExplorerThemeTest' --tests '*ComponentStatesMatrixTest' --no-daemon` | exists | ✅ green |
| 28-03-03 | 28-03 | 3 | DEMO-02 | T-28-11 | source-scan guard: no direct library-theme wrap in explorer/ | plain JVM | `./gradlew testDebugUnitTest --tests '*ExplorerSchemePinGuardTest' --no-daemon` | ❌ W0 | ✅ green |
| 28-04-01 | 28-04 | 4 | DEMO-03 | T-28-13, T-28-14 | English stage: fire one, tap opens, key readout | Robolectric | `./gradlew testDebugUnitTest --tests '*FeedbackStackDemoRegistryRenderTest' --no-daemon` | ❌ W0 | ✅ green |
| 28-04-02 | 28-04 | 4 | DEMO-03 | T-28-14, T-28-15 | burst past the cap, swipe, Undo, German stage at 360dp | Robolectric | `./gradlew testDebugUnitTest --tests '*FeedbackStackDemoRegistryRenderTest' --tests '*LibraryInvariantsGuardTest' --no-daemon` | ❌ W0 | ✅ green |
| 28-04-03 | 28-04 | 4 | DEMO-03 | T-28-14 | Undo Center target rows EN and DE; registry and CATALOG guards green | Robolectric | `./gradlew testDebugUnitTest --tests '*UndoCenterTargetRowsDemoRegistryRenderTest' --tests '*ComponentRegistryDriftGuardTest' --tests '*CatalogDocsDriftGuardTest' --no-daemon` | ❌ W0 | ✅ green |
| 28-05-01 | 28-05 | 5 | DEMO-03 | T-28-16, T-28-18 | Receipt single go-to-item: hide() then key readout | Robolectric | `./gradlew testDebugUnitTest --tests '*GoToItemSheetDemosRegistryRenderTest' --tests '*ReceiptDemosRegistryRenderTest' --no-daemon` | ❌ W0 | ✅ green |
| 28-05-02 | 28-05 | 5 | DEMO-03 | T-28-17 | mixed batch, Classic, German at 360dp | Robolectric | `./gradlew testDebugUnitTest --tests '*GoToItemSheetDemosRegistryRenderTest' --tests '*LibraryInvariantsGuardTest' --no-daemon` | ❌ W0 | ✅ green |
| 28-05-03 | 28-05 | 5 | DEMO-02, DEMO-03 | T-28-18 | hoisted demo closes through hide(); registry story of Phases 25-27 verified | Robolectric + guards | `./gradlew testDebugUnitTest --tests '*HoistedSheetDemoRegistryRenderTest' --tests '*ComponentRegistryDriftGuardTest' --no-daemon` | ❌ W0 | ✅ green |
| 28-06-01 | 28-06 | 6 | A11Y-02 | T-28-21 | ReceiptRow, refused live region, saved-key summary, sheet row label verbatim | Robolectric | `./gradlew testDebugUnitTest --tests '*A11ySweepSemanticsTest' --no-daemon` | ❌ W0 | ✅ green |
| 28-06-02 | 28-06 | 6 | A11Y-02 | T-28-19, T-28-20 | group header is a heading; openable Undo Center row is one merged button (C-2) | Robolectric | `./gradlew testDebugUnitTest --tests '*A11ySweepUndoCenterSemanticsTest' --tests '*UndoCenterScreen*' --no-daemon` | ❌ W0 | ✅ green |
| 28-06-03 | 28-06 | 6 | A11Y-02 | T-28-21 | snackbar card label verbatim; locked D-11/D-07 forms pinned; full Layer-1 command | Robolectric | the A11Y Layer-1 command above | ❌ W0 | ✅ green |
| 28-07-01 | 28-07 | 7 | POL-03, DEMO-03, DEMO-02 | T-28-03, T-28-07 | phase hygiene: no wipe, no UA prefs key, one placeholder literal, nothing public | plain JVM | `./gradlew testDebugUnitTest --tests '*Phase28HygieneSourceContractTest' --no-daemon` | ❌ W0 | ✅ green |
| 28-07-02 | 28-07 | 7 | A11Y-02 | T-28-22, T-28-23 | read-only USB-only restore oracle | shell fixture (fake adb) | `bash tools/test/test-verify-tester-restored.sh` | ❌ W0 | ✅ green |
| 28-07-03 | 28-07 | 7 | A11Y-02, DEMO-02, DEMO-03, POL-03 | T-28-24, T-28-26 | fresh full gate; api.txt unchanged since c1681ff; locked files untouched | gradle + scripts | `./gradlew apiDump testDebugUnitTest detekt apiCheck --rerun-tasks --no-build-cache --no-daemon` | exists | ✅ green |
| 28-07-G1 | 28-07 | after 7 | A11Y-02, DEMO-02, DEMO-03, POL-03 | T-28-22..T-28-26 | Gate-1 on the TESTER (G1-0 to G1-8), restore, parking of P-1 to P-6 only | device (autonomous gate) | `verify_work_agentic_gate` per the contract in 28-07-PLAN.md | gate-authored | ✅ green |
| 28-08-01 | 28-08 | 8 | POL-03 | T-28-30, T-28-31 | `load()` at most once per process; header re-applied without a reload when missing or different | Robolectric | `./gradlew testDebugUnitTest --tests '*PlaceMapOsmdroidConfigTest' --tests '*Phase28HygieneSourceContractTest' --no-daemon` | extend existing | ✅ green |
| 28-09-01 | 28-09 | 9 | A11Y-02 | T-28-40, T-28-41 | refused-undo surface is one merged Assertive live-region node carrying the message (superseded in effect by 28-12) | Robolectric | `./gradlew testDebugUnitTest --tests '*OutcomeSheetLiveRegionTest' --tests '*OutcomeSheetClassicParitySourceContractTest' --no-daemon` | extend existing | ✅ green |
| 28-10-01 | 28-10 | 10 | A11Y-02 | T-28-42, T-28-43 | middle strip of a collapsed deck hidden from accessibility; 1, 2 cards and fanned deck hide nothing | Robolectric | `./gradlew testDebugUnitTest --tests '*FeedbackStackHostSemanticsTest' --no-daemon` | extend existing | ✅ green |
| 28-11-01 | 28-11 | 11 | A11Y-02 | T-28-45, T-28-46 | every focusable deck node data-named, English and German (the focus route of this plan was removed in 28-12) | Robolectric | `./gradlew testDebugUnitTest --tests '*FeedbackStackHostSemanticsTest' --tests '*A11ySweepFeedbackCardTest' --no-daemon` | extend existing | ✅ green |
| 28-12-01 | 28-12 | 12 | A11Y-02 | T-28-44, T-28-47, T-28-48 | single speech source per node on the platform AccessibilityNodeInfo; one live-region mechanism after the settle | Robolectric (platform node) | `./gradlew testDebugUnitTest --tests '*OutcomeSheetLiveRegionTest' --tests '*FeedbackStackHostSemanticsTest' --no-daemon` | extend existing | ✅ green |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [x] `explorer/ExplorerTheme.kt` helper + `ExplorerSchemePinGuardTest` — DEMO-02
- [x] `FeedbackStackDemoRegistryRenderTest`, `GoToItemDemosRegistryRenderTest`, `HoistedSheetDemoRegistryRenderTest` — DEMO-02, DEMO-03
- [x] `A11ySweepSemanticsTest` (incl. the missing `Heading` assertion) — A11Y-02
- [x] `PlaceMapTilePurgeTest` + real placeholder PNG fixture (6987 B, md5 `c069a15b2cc2d6b6f527ad09eb93c61a`) — POL-03
- [x] Widen `LibraryInvariantsGuardTest` demo-copy scan if demo copy leaves `ReleaseDemos.kt`

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Spoken TalkBack read-out, reading feel, double Dismiss listing | A11Y-02 | Speech output cannot be asserted from adb | Parked in `.planning/uat-pending/` / `HUMAN-UAT-PENDING.md` for Yahir; never waived. The agentic Gate-1 covered speech on the TESTER (`R5CT10XNKQN`) through TalkBack's speech overlay and the observer; five physical-only steps remain (swipe order against Undo, heading navigation, openable-row stop count, collapsed-deck swipe walk, explicit-focus swipe on the refusal message), plus the owner listen-through of the C-1 message |

---

## Validation Sign-Off

> Finalized post-execution by the Nyquist finalizer (2026-10-18).

- [x] All tasks have `<automated>` verify or Wave 0 dependencies
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all MISSING references
- [x] No watch-mode flags
- [x] Feedback latency < 420s
- [x] _(finalizer-only, post-execution)_ `nyquist_compliant: true` set by the finalizer

**Approval:** validated 2026-10-18 (finalizer)

---

## Validation Audit 2026-10-18

| Metric | Count |
|--------|-------|
| Gaps found | 0 |
| Resolved | 0 |
| Escalated | 0 |

Every requirement (A11Y-02, DEMO-02, DEMO-03, POL-03) maps to existing test classes that ran green: the executors' forced-fresh runs (the last, after gap plan 28-12: the full unit suite with `--rerun-tasks --no-build-cache`, `apiDump` byte-identical, detekt zero baseline, `tools/test/run-all.sh` FAIL=0), the verifier's forced-fresh targeted run (71 classes, 948 tests, 0 failures) and the Gate-1 signed runs on the TESTER (`uatr-20261018T080238Z-a214dfe5` for the final build, result has_partial: all automatable checks PASS, physical-only steps parked). The gap plans 28-08 to 28-12 added rows above. The Manual-Only row is not an automatable gap. Code under `src` and `tools` has not changed since the security re-audit head (4a549cb); only `.planning` artifacts did.
