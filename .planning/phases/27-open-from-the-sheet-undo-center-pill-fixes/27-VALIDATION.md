---
phase: "27"
slug: open-from-the-sheet-undo-center-pill-fixes
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
status: draft
nyquist_compliant: false
wave_0_complete: false
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
| FOPEN-01 | row shows title/detail/chevron, tap calls `onOpenTarget(target)` once, Role.Button; no-target / no-handler render equals v2.7.1; key never in semantics/testTag/log | Robolectric | `--tests '*OpenTargetRowTest' --tests '*OutcomeSheetOpenTargetTest'` | ❌ W0 | ⬜ pending |
| FOPEN-01 | Classic SHA pins, dispatch literal, Receipt order stay green unedited | source contract | `--tests '*OutcomeSheetClassicParitySourceContractTest' --tests '*OutcomeSheetReceiptSourceContractTest'` | ✅ | ⬜ pending |
| FOPEN-01 | source compat of v2.7.1 shapes | compile canary | `--tests '*V271CallSiteCanaryTest'` | ✅ extend | ⬜ pending |
| FOPEN-02 | per-row batch targets; target-less rows have no OnClick, no chevron | Robolectric | `--tests '*OutcomeSheetBatchOpenTargetTest'` | ❌ W0 | ⬜ pending |
| FOPEN-03 | Available+target opens; Undone/Failed/no-target/group header do not; stale-callback guard; hold shows peek only | Robolectric | `--tests '*UndoCenterScreenOpenTargetTest'` | ❌ W0 | ⬜ pending |
| FOPEN-03 | old overload unchanged (golden), source contract count 1 to 2 deliberate | golden + contract | `--tests '*UndoCenterScreenDefaultParityTest' --tests '*UndoCenterScreenOpenParityTest' --tests '*UndoCenterScreenSourceContractTest' --tests '*UndoCenterScreenSourceCompatV251Test'` | ✅ + ❌ W0 | ⬜ pending |
| FOPEN-01/02/03 | long German at w360dp and font scale 2.0: wraps, chevron inside window | Robolectric NATIVE | `--tests '*OutcomeSheetOpenTargetLongLabelTest' --tests '*UndoCenterScreenOpenTargetLongLabelTest'` | ❌ W0 | ⬜ pending |
| POL-01 | radius = min(height/2, cap); one-line equals CircleShape; multi-line text corners inside shape (CircleShape control fails) | JVM + Robolectric | `--tests '*HandledByPillShapeTest' --tests '*OutcomeSheetReceiptLongLabelTest'` | ❌ W0 | ⬜ pending |
| POL-02 | `handledByDotRing` null on hub light/dark, contrast >= 3 for synthetic white-on-light and dynamic samples; dot tag/size unchanged | JVM + Robolectric | `--tests '*HandledByDotContrastTest' --tests '*OutcomeSheetReceiptSuccessTest'` | ❌ W0 | ⬜ pending |
| API | additive only | gate | `./gradlew apiDump apiCheck`, `git diff api.txt`, `bash tools/verify-api-source-additive.sh v2.7.1` | n/a | ⬜ pending |
| Style | detekt zero baseline | static | `./gradlew detekt --no-daemon` | n/a | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] `component/OpenTargetRowTest.kt`, `OutcomeSheetOpenTargetTest.kt`, `OutcomeSheetBatchOpenTargetTest.kt`, `OutcomeSheetOpenTargetLongLabelTest.kt` (FOPEN-01/02)
- [ ] `feedback/UndoCenterScreenOpenTargetTest.kt`, `UndoCenterScreenOpenParityTest.kt`, `UndoCenterScreenOpenTargetLongLabelTest.kt` (FOPEN-03)
- [ ] `component/HandledByPillShapeTest.kt`, `HandledByDotContrastTest.kt` (POL-01/02)
- [ ] Extend `OutcomeSheetFixtures.kt` (`success(...)` + `batchResultsWithTargets()`), `V271CallSiteCanaryTest`
- [ ] Edit `UndoCenterScreenSourceContractTest` count 1 to 2 (D-04, deliberate)
- [ ] Pure-JVM contrast helper test mirroring `ContrastTestSupport.contrastRatio`

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| TalkBack reading of the new rows | FOPEN-01..03 | needs a screen reader | Phase 28 (A11Y-02); park for Yahir if it cannot be driven |

Gate-1 (agentic, TESTER `R5CT10XNKQN`) covers on-device tap-to-open, font scale 2.0 pill and dot under dynamic color; restore font scale 1.0 and `wm density 450` afterward.

---

## Validation Sign-Off

> **Plan-time state is a DRAFT.** Frontmatter `status: draft` and `nyquist_compliant: false` are finalized only post-execution by the Nyquist finalizer.

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 120s
- [ ] _(finalizer-only, post-execution)_ `nyquist_compliant` — leave `false` at plan time

**Approval:** pending (finalizer-owned)
