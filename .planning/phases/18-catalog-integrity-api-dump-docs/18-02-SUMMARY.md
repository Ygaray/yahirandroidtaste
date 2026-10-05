---
phase: 18-catalog-integrity-api-dump-docs
plan: 02
subsystem: verification
tags: [ship-gate, evidence, cat-02, api-02, inv-02, metalava, detekt]

requires:
  - phase: 18-catalog-integrity-api-dump-docs
    provides: plan 18-01 docs on HEAD (API.md section 10, INTEGRATION.md localization note)
provides:
  - "18-SHIP-GATE-EVIDENCE.md: fresh CAT-02 / API-02 / INV-02 transcript on code HEAD b9848a9, plus restore, DOC-02, boundary, caveat and Phase 19 hand-off"
affects: [phase-19-ship]

actuals:
  tokens: 9000
  tasks: 3
  commits: 3

plan_head_before: b9848a9c7b710dc44d34e466aa6bb88f6e910280
commits: 3

key-files:
  created:
    - .planning/phases/18-catalog-integrity-api-dump-docs/18-SHIP-GATE-EVIDENCE.md
  modified: []

key-decisions:
  - "D-01 reading applied: Metalava apiCheck green plus current api.txt is the pass condition; raw-line exit 3 recorded and allowlisted, not overridden"
  - "v2.4.0 swap-baseline additionally forced with --rerun, because v2.4.0 and v2.4.1 api.txt are the same blob and Gradle reported the plain run UP-TO-DATE"

requirements-completed: [CAT-02, API-02, INV-02]

status: complete
---

# Phase 18 Plan 02: Ship-gate evidence (CAT-02 / API-02 / INV-02) Summary

Re-ran every v2.5 catalog, API and invariant gate fresh on code HEAD `b9848a9c7b710dc44d34e466aa6bb88f6e910280` and recorded the verbatim transcript in `18-SHIP-GATE-EVIDENCE.md`; all gates are green, `api.txt` was restored byte-identical after every swap, and no tag, `src/`, `tools/` or config change was made.

## Tasks and commits

| Task | Name | Commit | Hook lane |
|------|------|--------|-----------|
| 1 | CAT-02 evidence (clean uncached full suite, drift guards, registry state) | `1a02e4a` | LANE 1 (mode=additive, baseline=v2.4.1), no override |
| 2 | API-02 evidence (dump idempotence, swap-baseline x2, raw-line allowlist, self-tests, javap) | `66b4d5c` | LANE 1, no override |
| 3 | INV-02 evidence + restore, DOC-02, boundary, caveat, summary sections | `c5717fb` | LANE 1, no override |

Commit count measured from the ledger: `b9848a9..HEAD` = 3 (docs-only, one new file under `.planning/`). No `HUB_LANE_OVERRIDE`, no `--no-verify`.

## Results

- **CAT-02:** `./gradlew cleanTestDebugUnitTest testDebugUnitTest detekt apiCheck --no-build-cache` BUILD SUCCESSFUL. 77 JUnit XML files, all newer than the pre-run marker; **tests=751, skipped=22, failures=0, errors=0**, identical to the research numbers at `fa6b7ac` (751 / 22 / 0 / 0). The four drift guards (ComponentRegistryDriftGuardTest 1, DomainVocabularyDriftGuardTest 2, GeneratedSymbolDriftGuardTest 2, ComponentRegistryTierTest 4) each ran fresh and green. Voice = 5 entries, total 61, nothing under `src/` changed since `625c714`, added public composable names since v2.4.1 are exactly the four registered hidden shims.
- **API-02:** `apiDump` byte-identical to committed `api.txt` (cmp clean); `apiCheck` green; swap-baseline `apiCheck` BUILD SUCCESSFUL against v2.4.1 and v2.4.0, each restored with `git checkout` (status empty); `metalavaCheckCompatibilityDebug` green; `bash tools/test/run-all.sh` rc 0 (7 / 5 / 5 PASS, 0 FAIL).
- **Raw-line allowlist:** `API_FILE=api.txt bash tools/verify-api-additive.sh v2.4.1` exits 3 with exactly 10 `API-ADDITIVE FAIL (lane 3)` lines (4 composables: ApproachLadderCard, ClarificationBar, ModelSelectCard, ProviderKeyCard; 6 `copy(optional ...)`: FailureActionUiModel, HandledByUiModel, ProposedItemUiModel, UndoRefusedUiModel, UndoRowUiModel, VoiceOutcomeUiState.Failure). Recorded as the known false positive; not overridden, hidden or edited. The absolute-path form's silent SKIP is recorded as an informational latent defect for Phase 19 D-03.
- **javap (informational):** v2.4.1 AAR was cached; `assembleRelease --offline` OK; `base=2526 head=2584 filtered_ComposableSingletons=12 missing=0`. Not a gate; Phase 19 D-03 owns the binary gate.
- **INV-02:** 12 changed `src/main` files; 10 added import lines (non-vacuous), none foreign (only androidx, `kotlin.jvm.internal.DefaultConstructorMarker` x4, and the library's own `ActionButtonDefaults`); build, gradle, settings, jitpack and config files unchanged vs v2.4.1; no logging or Hilt-host additions; `detekt --rerun` BUILD SUCCESSFUL, zero baseline untouched.
- **Boundary / DOC-02:** no `v2.5*` tag, none at HEAD; no protected path changed since `625c714`; API.md section 10, the trailing-lambda caveat, the sibling paragraph and the INTEGRATION.md localization note re-confirmed.

## Deviations from Plan

None to scope. Two minor execution notes:

1. The plain v2.4.0 swap-baseline run was reported UP-TO-DATE by Gradle because `v2.4.0:api.txt` and `v2.4.1:api.txt` are the same blob (`f5832527...`). To avoid resting that proof on a skip, `metalavaCheckCompatibilityRelease --rerun` was run against the v2.4.0 file and was BUILD SUCCESSFUL (recorded in the evidence). Likewise `detekt` and `metalavaCheckCompatibilityDebug` were forced with `--rerun` for fresh evidence (detekt was UP-TO-DATE inside the full-suite run).
2. The evidence "HEAD at capture" is the code HEAD `b9848a9`; the three commits that carry the evidence add only `.planning/` content on top of it, which the evidence states explicitly.

## Residuals and hand-offs

1. The `ProposedItemUiModel` trailing-lambda source break is **documented only** (API.md), no code fix, per the orchestrator ruling; it is surfaced in the evidence `## Known Caveat` for the orchestrator before Phase 19. Line 60 of `15-VERIFICATION.md` is incorrect for that type and was not edited.
2. The binary gate is re-run by Phase 19 D-03 on the tagged HEAD (`tools/verify-binary-abi.sh` is Phase 19's).
3. `tools/verify-api-additive.sh` keeps exiting 3 (10 lines) and the hook's absolute-path API half is a silent SKIP until Phase 19 D-03 retires them.
4. No tag cut, no consumer touched. Gate-2 human UAT items remain owned by their registered fragments under `.planning/uat-pending/`. `18-VALIDATION.md` not edited.

## Threat model

T-18-05 mitigated (single-shell swaps, trap plus explicit `git checkout`, final empty status and zero diff); T-18-06 mitigated (exit 3 recorded verbatim, Metalava authoritative, no override); T-18-07 mitigated (build-file diff, non-vacuous import filter, detekt green); T-18-08 mitigated (clean plus `--no-build-cache`, fresh XML, non-zero sum); T-18-09 mitigated (no tag, no protected-path change); T-18-10 mitigated (no logging added).

## Self-Check: PASSED

- `18-SHIP-GATE-EVIDENCE.md` present with the eight required sections and no failing verdict; commits `1a02e4a`, `66b4d5c`, `c5717fb` found in `git log`; `git status --short api.txt src tools` empty.
