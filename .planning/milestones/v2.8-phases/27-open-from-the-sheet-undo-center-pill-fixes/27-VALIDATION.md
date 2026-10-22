---
phase: "27"
slug: open-from-the-sheet-undo-center-pill-fixes
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
status: validated
nyquist_compliant: true
wave_0_complete: true
created: "2026-10-17"
---

# Phase 27 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 4.13.2 + Robolectric 4.16.1 + `createComposeRule` (ui-test 1.11.0) |
| **Config file** | `build.gradle.kts`; detekt `config/detekt/detekt.yml` + `config/detekt-compose.yml` (zero baseline) |
| **Quick run command** | `./gradlew testDebugUnitTest --tests '*OpenTarget*' --tests '*HandledByPill*' --tests '*HandledByDot*' --tests '*OutcomeSheet*' --tests '*UndoCenterScreen*' --no-daemon` |
| **Guard run command** | `./gradlew testDebugUnitTest --tests '*CatalogDocsDriftGuardTest' --tests '*DomainVocabularyDriftGuardTest' --tests '*ComponentRegistryDriftGuardTest' --tests '*ComponentStatesMatrixTest' --tests '*ComponentPlaygroundIntegrityTest' --tests '*LibraryInvariantsGuardTest' --no-daemon` |
| **Full suite command** | `./gradlew apiDump testDebugUnitTest detekt apiCheck --rerun-tasks --no-daemon`, then `git diff api.txt` (additions only), `bash tools/verify-api-source-additive.sh v2.7.1`, `bash tools/test/run-all.sh` |
| **Estimated runtime** | quick ~60 s; full several minutes |

Rules: one Gradle invocation at a time, always `--no-daemon`, never `--stop`, never `--offline`; force a fresh run (`--rerun-tasks`) for gate evidence. Baseline: 1613 tests, 22 pre-existing skips, `run-all.sh` PASS=52 FAIL=0.

---

## Sampling Rate

- **After every task commit:** quick run command narrowed to the touched area
- **After every plan wave:** guard run command + quick run command + the three source-contract tests (`OutcomeSheetClassicParitySourceContractTest`, `OutcomeSheetReceiptSourceContractTest`, `UndoCenterScreenSourceContractTest`)
- **Before `/gsd-verify-work`:** full suite fresh, `git diff api.txt` additive only
- **Max feedback latency:** ~120 seconds

---

## Per-Task Verification Map

Task IDs are bound by the planner; requirement coverage below is from 27-RESEARCH.md (Validation Architecture).

| Requirement | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|-------------|-----------------|-----------|-------------------|-------------|--------|
| FOPEN-01 | row shows title/detail/chevron, tap calls `onOpenTarget(target)` once, Role.Button; no-target / no-handler render equals v2.7.1; key never in semantics/testTag/log | Robolectric | `--tests '*OpenTargetRowTest' --tests '*OutcomeSheetOpenTargetTest'` | ✅ | ✅ green |
| FOPEN-01 | Classic SHA pins, dispatch literal, Receipt order stay green unedited | source contract | `--tests '*OutcomeSheetClassicParitySourceContractTest' --tests '*OutcomeSheetReceiptSourceContractTest'` | ✅ | ✅ green |
| FOPEN-01 | source compat of v2.7.1 shapes | compile canary | `--tests '*V271CallSiteCanaryTest'` | ✅ | ✅ green |
| FOPEN-02 | per-row batch targets; target-less rows have no OnClick, no chevron | Robolectric | `--tests '*OutcomeSheetBatchOpenTargetTest'` | ✅ | ✅ green |
| FOPEN-03 | Available+target opens; Undone/Failed/no-target/group header do not; stale-callback guard; hold shows peek only | Robolectric | `--tests '*UndoCenterScreenOpenTargetTest'` | ✅ | ✅ green |
| FOPEN-03 | old overload unchanged (golden), source contract count 1 to 2 deliberate | golden + contract | `--tests '*UndoCenterScreenDefaultParityTest' --tests '*UndoCenterScreenOpenParityTest' --tests '*UndoCenterScreenSourceContractTest' --tests '*UndoCenterScreenSourceCompatV251Test'` | ✅ | ✅ green |
| FOPEN-01/02/03 | long German at w360dp and font scale 2.0: wraps, chevron inside window | Robolectric NATIVE | `--tests '*OutcomeSheetOpenTargetLongLabelTest' --tests '*UndoCenterScreenOpenTargetLongLabelTest'` | ✅ | ✅ green |
| POL-01 | radius = min(height/2, cap); one-line equals CircleShape; multi-line text corners inside shape (CircleShape control fails) | JVM + Robolectric | `--tests '*HandledByPillShapeTest' --tests '*OutcomeSheetReceiptLongLabelTest'` | ✅ | ✅ green |
| POL-02 | `handledByDotRing` null on hub light/dark, contrast >= 3 for synthetic white-on-light and dynamic samples; dot tag/size unchanged | JVM + Robolectric | `--tests '*HandledByDotContrastTest' --tests '*OutcomeSheetReceiptSuccessTest'` | ✅ | ✅ green |
| API | additive only | gate | `./gradlew apiDump apiCheck`, `git diff api.txt`, `bash tools/verify-api-source-additive.sh v2.7.1` | n/a | ✅ green |
| Style | detekt zero baseline | static | `./gradlew detekt --no-daemon` | n/a | ✅ green |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [x] `component/OpenTargetRowTest.kt`, `OutcomeSheetOpenTargetTest.kt`, `OutcomeSheetBatchOpenTargetTest.kt`, `OutcomeSheetOpenTargetLongLabelTest.kt` (FOPEN-01/02)
- [x] `feedback/UndoCenterScreenOpenTargetTest.kt`, `UndoCenterScreenOpenParityTest.kt`, `UndoCenterScreenOpenTargetLongLabelTest.kt` (FOPEN-03)
- [x] `component/HandledByPillShapeTest.kt`, `HandledByDotContrastTest.kt` (POL-01/02)
- [x] Extend `OutcomeSheetFixtures.kt` (`success(...)` + `batchResultsWithTargets()`), `V271CallSiteCanaryTest`
- [x] Edit `UndoCenterScreenSourceContractTest` count 1 to 2 (D-04, deliberate)
- [x] Pure-JVM contrast helper test mirroring `ContrastTestSupport.contrastRatio`

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| TalkBack reading of the new rows | FOPEN-01..03 | needs a screen reader | Phase 28 (A11Y-02); park for Yahir if it cannot be driven |

Gate-1 (agentic, TESTER `R5CT10XNKQN`) covers on-device tap-to-open, font scale 2.0 pill and dot under dynamic color; restore font scale 1.0 and `wm density 450` afterward.

---

## Validation Sign-Off

> Finalized post-execution by the Nyquist finalizer (2026-10-17).

- [x] All tasks have `<automated>` verify or Wave 0 dependencies
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all MISSING references
- [x] No watch-mode flags
- [x] Feedback latency < 120s
- [x] _(finalizer-only, post-execution)_ `nyquist_compliant: true` set by the finalizer

**Approval:** validated 2026-10-17 (finalizer)

---

## Validation Audit 2026-10-17

| Metric | Count |
|--------|-------|
| Gaps found | 0 |
| Resolved | 0 |
| Escalated | 0 |

Every requirement row maps to an existing test class that ran green in the fresh post-fix run (18 mapped classes, 0 failures, 0 errors, 0 skipped), plus the fresh full gate (`testDebugUnitTest detekt apiCheck --rerun-tasks`, rc=0), the additive script (`API SOURCE-ADDITIVE OK`) and the Gate-1 signed run `uatr-20261017T194228Z-30718f43` (all five criteria PASS on the tester). The single Manual-Only row (TalkBack reading of the new rows) is Phase 28 scope (A11Y-02), not an automatable gap. No code outside `.planning` changed between the security audit head (5017ce6) and HEAD, so the SECURITY audit stays fresh.
