# Phase 9 / Plan 09-01 — SHIP-01 Validation

**Scope:** Retroactive Nyquist validation-gap fill for requirement `SHIP-01` as delivered by
Phase 9 Plan 09-01 (commit `ff3d9c6` on `main`).

## Gap identified

Plan 09-01's own verification of SHIP-01 (the apiCheck-genuinely-green clause) was **entirely
mechanical but non-durable**: one-off `grep -c` commands and `./gradlew` invocations run once
against the tag-candidate commit, never captured as a persisted, CI-enforced test. This left a
real regression window:

- `metalavaCheckCompatibilityDebug`/`Release` only fail on **breaking removals** from the tracked
  `api.txt` surface. They do **not** fail when a symbol is **added back** — which is exactly the
  failure mode this gap covers: if `build.gradle.kts`'s `metalava { hiddenAnnotations }` entry for
  `dagger.internal.DaggerGenerated` is ever reverted, edited, or broken by a future Hilt/Dagger
  version bump (annotation renamed, etc.), the next `./gradlew apiDump` would silently readmit
  `UndoHistoryStore_Factory` into the committed `api.txt`, and no existing hub gate would catch it
  (an "added" symbol is non-breaking from metalava's point of view).
- The repo already has a durable-regression-guard convention for exactly this class of risk
  (`ComponentRegistryDriftGuardTest`, `DomainVocabularyDriftGuardTest` — both run on every
  `testDebugUnitTest`), but no equivalent guard existed for the `api.txt` Dagger-generated-symbol
  invariant SHIP-01 depends on.

**Verdict: non-compliant as delivered** (gap real, not a false positive) — closed below.

## Fix applied

Added `GeneratedSymbolDriftGuardTest`
(`src/test/java/io/github/ygaray/yahirandroidtaste/explorer/GeneratedSymbolDriftGuardTest.kt`),
a plain JUnit test that runs on every `testDebugUnitTest` invocation and:

1. Fails if `api.txt` contains any line referencing `DaggerGenerated` or
   `UndoHistoryStore_Factory` (the KI-2026-09-02-01 regression case).
2. Fails if `UndoHistoryStore`'s real class, `@Inject` constructor, or `emitTrackedWithUndo`
   extension are missing from `api.txt` (guards the inverse regression — the fix accidentally
   over-hiding real API).

## Behavioral proof (red/green, not just "test exists")

| Step | Command | Result |
|------|---------|--------|
| Baseline (as committed at `ff3d9c6`) | `./gradlew testDebugUnitTest --tests "*GeneratedSymbolDriftGuardTest*"` | BUILD SUCCESSFUL — both test methods pass |
| Adversarial: appended a synthetic `UndoHistoryStore_Factory` / `DaggerGenerated` block back into `api.txt`, then re-ran with `--rerun-tasks` (forces the test to re-read the mutated file — normal Gradle caching does not treat `api.txt` as a tracked task input) | same test filter | BUILD FAILED — `apiTxtNeverReadmitsAHiddenDaggerGeneratedFactorySymbol` failed with an assertion naming the offending line, exactly the regression this guard exists to catch |
| Restored `api.txt` to its committed state (`git status --porcelain api.txt` confirmed clean) | `./gradlew testDebugUnitTest detekt --rerun-tasks` | BUILD SUCCESSFUL — full battery green, zero detekt smells, zero-baseline policy intact |

This confirms the test is a real, failing-when-it-should-fail regression guard, not a trivial
tautology.

## Resolution

| Gap | Status | Test file | Command |
|-----|--------|-----------|---------|
| No durable regression test for the generated-symbol-reappearing case | **FILLED** | `src/test/java/io/github/ygaray/yahirandroidtaste/explorer/GeneratedSymbolDriftGuardTest.kt` | `./gradlew testDebugUnitTest --tests "*GeneratedSymbolDriftGuardTest*"` (also runs as part of plain `./gradlew testDebugUnitTest`) |

No implementation files were modified. `build.gradle.kts`, `api.txt`, and `.planning/KNOWN-ISSUES.md`
from commit `ff3d9c6` are unchanged by this validation pass.
