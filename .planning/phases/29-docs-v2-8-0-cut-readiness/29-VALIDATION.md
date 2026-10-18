---
phase: "29"
slug: "docs-v2-8-0-cut-readiness"
status: draft
nyquist_compliant: false
wave_0_complete: false
created: "2026-10-18"
---

# Phase 29 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution. Source: 29-RESEARCH.md `## Validation Architecture`.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit4 + Robolectric (native graphics) + Compose UI test via `testDebugUnitTest`; shell fixtures under `tools/test/` |
| **Config file** | `build.gradle.kts`, `config/detekt/detekt.yml`, `config/detekt-compose.yml`, `config/detekt-baseline.xml` (zero baseline) |
| **Quick run command** | `./gradlew testDebugUnitTest --tests '*CatalogDocsDriftGuardTest' --tests '*HandledByPillShapeTest' --tests '*V271CallSiteCanaryTest' --no-daemon` |
| **Full suite command** | `./gradlew testDebugUnitTest detekt --rerun-tasks --no-build-cache --no-daemon --console=plain` then `bash tools/test/run-all.sh` |
| **Estimated runtime** | ~330 seconds forced fresh (Phase 28: 5m31s, 1866 tests) |

---

## Sampling Rate

- **After every task commit:** the quick run command (guard + pill + canary) after any doc or test edit
- **After every plan wave:** `./gradlew testDebugUnitTest detekt --no-daemon`
- **Before verify-work:** the forced-fresh battery on GATED_HEAD, run once, last
- **Max feedback latency:** ~330 seconds

---

## Per-Task Verification Map

| Req | Behavior | Test Type | Automated Command | File Exists | Status |
|-----|----------|-----------|-------------------|-------------|--------|
| SHIP-05 / SC1 | docs name the v2.8 symbols; counts 67/6/73 | JVM guard | `./gradlew testDebugUnitTest --tests '*CatalogDocsDriftGuardTest' --no-daemon` | extend (Wave 0) | pending |
| SHIP-05 / SC2 | additions only vs v2.7.1 | tool + canary | `bash tools/verify-api-source-additive.sh v2.7.1`; `--tests '*V271CallSiteCanaryTest'` | yes | pending |
| SHIP-05 / SC2 | apiCheck green on regenerated api.txt | gradle | `./gradlew apiDump` then `git diff --exit-code -- api.txt` then `./gradlew apiCheck` | yes | pending |
| SHIP-05 / SC2 | tests, detekt, tools green, fresh | gradle + shell | full suite command | yes | pending |
| SHIP-05 / SC3 | DE/ES snackbar card, sheet rows, Undo Center rows at 360dp / 2.0 | Robolectric | `--tests '*FeedbackStackHostLongLabelTest' --tests '*OutcomeSheetOpenTargetLongLabelTest' --tests '*UndoCenterScreenOpenTargetLongLabelTest'` | yes | pending |
| SHIP-05 / SC3 | ES handled-by pill at 360dp / 2.0 | Robolectric | `--tests '*HandledByPillShapeTest'` | Spanish case missing (Wave 0) | pending |
| SHIP-05 / SC3 | DE + ES on the TESTER, evidence kept | Gate-1 device | agentic flow, `tools/verify-tester-restored.sh` | throwaway harness | pending |
| SHIP-05 / SC4 | proposal artifact, no tag, no push | git check | `git tag --list 'v2.8*'` empty; `git ls-remote origin refs/heads/main` unchanged | new artifact | pending |

---

## Wave 0 Requirements

- [ ] Spanish case in `component/HandledByPillShapeTest.kt`
- [ ] `newInV280` list + v2.8 entry-point assertions in `explorer/CatalogDocsDriftGuardTest.kt`
- [ ] Throwaway Spanish harness (scratchpad, not committed)

---

## Manual-Only Verifications

| Behavior | Why manual | Instructions |
|----------|-----------|--------------|
| Physical TalkBack steps, German font-2.0 stacked body column, German batch Receipt sheet under nav bar | physical, owner-only (Gate-2) | recorded as carry-forward in the proposal, not fixed here |

---

## Validation Sign-Off

Plan-time draft. Finalized post-execution by the Nyquist finalizer; do not set `nyquist_compliant: true` here.
