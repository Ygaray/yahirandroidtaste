---
phase: 16-a11y-failure-enrichment
plan: 03
subsystem: ui-voice-failure
tags: [source-compat, additive-api, metalava, regression-pin, closing-gate]
requires: ["16-01", "16-02"]
provides:
  - "Compile-only v2.4 call-shape fixture for Failure and FailureActionUiModel (SC5)"
  - "Render pin: a v2.4-shaped Failure renders unchanged through FailureBody"
  - "Phase 16 closing gate evidence (released-baseline apiCheck, removed-line allowlist, exactly-two delta)"
affects: [18-docs, 19-release]
tech-stack:
  added: []
  patterns:
    - "Non-invoked composable lambda + plain-JVM model assertions as compile-only fixture (extends Phase 15 file)"
key-files:
  created: []
  modified:
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/VoiceI18nSourceCompatTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetTest.kt
key-decisions:
  - "No src/main, api.txt, explorer/, or 16-VALIDATION.md edits; gate task is verification-only with no commit"
requirements-completed: [VA11Y-01, VFAIL-01, VFAIL-02, VFAIL-03]
duration: 2 min
completed: 2026-10-05
status: complete
commits: 1
plan_head_before: 9ade39e2fa0cbc94ffce871e5589b977b711053c
actuals:
  tokens: 5000
  tasks: 2
  commits: 1
coverage:
  - deliverable: "v2.4 Failure / FailureActionUiModel call shapes (positional, named, destructuring, legacy copy) compile against v2.5 signatures and carry defaults; legacy copy preserves custom body/prefix/role"
    verification:
      - kind: test
        ref: "VoiceI18nSourceCompatTest#v24FailureAndActionShapes_compileAndCarryDefaults"
        status: pass
    human_judgment: false
  - deliverable: "A v2.4-shaped Failure renders unchanged through FailureBody (surface, reason, handled-by, one clickable action, no ContentDescription)"
    verification:
      - kind: test
        ref: "OutcomeSheetTest#v2_4-shaped Failure renders unchanged - surface, reason, handled-by, one action, no description"
        status: pass
    human_judgment: false
  - deliverable: "Phase 16 is strictly additive over released v2.4.1 (apiCheck vs v2.4.1 api.txt, ten-symbol allowlist, exactly two Phase 16 removed lines, none for ApproachLadderCard, INV-01 imports and Hilt host)"
    verification:
      - kind: command
        ref: "git show v2.4.1:api.txt > api.txt && ./gradlew apiCheck (restored after); git diff allowlist/delta/import/hilt checks"
        status: pass
    human_judgment: false
  - deliverable: "Rung row pitch after minimum-size modifier (D-04), Failure role color, TalkBack announcement of a prefix"
    human_judgment: true
    rationale: "Gate-2 human/device checks; no hub gallery fixture for role/body/prefix (A5)"
---

# Phase 16 Plan 03: v2.4 source-compat fixture and Phase 16 closing gate Summary

**A v2.4-shaped `Failure` / `FailureActionUiModel` consumer is now pinned end to end (compile shapes, defaults, legacy copy, unchanged render), and the phase closing gate proves Phase 16 strictly additive over the released v2.4.1 api.txt.**

## Performance
- **Duration:** 2 min | **Tasks:** 2 (tracer + verification-only gate) | **Files:** 2 modified

## Accomplishments
- Task 1 (tracer): `VoiceI18nSourceCompatTest` gained `v24FailureAndActionShapes_compileAndCarryDefaults` (Failure at 1/2/3 positional and named arities, positional destructuring of the first three components, legacy three-argument copy preserving a custom body via `assertSame` and prefix, FailureActionUiModel two-argument construction/destructuring/legacy copy preserving Neutral and a custom Destructive role) and a non-invoked composable lambda calling `OutcomeSheet(Failure("r", null), {}, Modifier)`. `OutcomeSheetTest` gained one SC5 render pin through `OutcomeSheetContent` (surface exists, no ContentDescription, reason, handled-by, exactly one action button that fires onClick).
- Task 2: verification-only closing gate, all green, no commit.

## Task Commits
| Task | Commit | Message |
|------|--------|---------|
| 1 | `385719c` | test(16-03): v2.4 Failure call-shape fixture and unchanged-caller render pin (SC5) |
| 2 | (none) | verification-only gate |

## Closing-gate results (Task 2)
| Check | Result |
|-------|--------|
| `./gradlew testDebugUnitTest detekt` (full suite) | BUILD SUCCESSFUL; 722 tests, 0 failures, 0 errors; ComponentRegistryDriftGuardTest, DomainVocabularyDriftGuardTest, GeneratedSymbolDriftGuardTest, GalleryDemoInteractionTest all present and green; detekt zero-baseline green |
| apiCheck against RELEASED v2.4.1 api.txt | BUILD SUCCESSFUL (api.txt restored; `git status --porcelain -- api.txt` empty) |
| apiDump vs committed api.txt (`cmp`) | identical (fresh) |
| Removed-line allowlist (ten symbols) | PASS |
| Phase 16 delta `git diff 084317e HEAD -- api.txt` | exactly 2 removed lines, both ` copy(`; no ApproachLadderCard line changed |
| Removed lines for the Failure classes since v2.4.1 | exactly two: `FailureActionUiModel copy(optional String label, optional Function0 onClick)` and `VoiceOutcomeUiState.Failure copy(optional String reason, optional HandledByUiModel? handledBy, optional FailureActionUiModel? action)` |
| explorer/, SegmentedOptionSelector.kt, feedback/ vs v2.4.1 | byte-identical (A5 resolved: gallery unchanged) |
| src/main imports since v2.4.1 | only androidx/kotlin/android/library namespace (INV-01) |
| `@HiltAndroidApp` / `@AndroidEntryPoint` added | none |
| Clean tree under src + api.txt; no `v2.5*` tag | PASS |
| 16-VALIDATION.md | still `status: draft`, `nyquist_compliant: false` (untouched) |

`git log --oneline v2.4.1..HEAD -- src api.txt`: Phase 15 commits plus two feat(16-01), two feat(16-02) and one test(16-03).

## Deviations from Plan

**1. [Rule 3 - Blocking/process] No HUB_LANE_OVERRIDE needed on the Task 1 commit**
- The plan said to commit with `HUB_LANE_OVERRIDE=3`. The pre-commit hook classified this test-only commit (no api.txt change) as `LANE 1 (mode=additive, baseline=v2.4.1)`, and the commit passed with no override. Using no override avoids a mismatched lane value. Commit `385719c`.

**2. [Rule 1 - Bug in test] Trailing-lambda form of FailureActionUiModel**
- The first draft of the render pin used `FailureActionUiModel("Retry") { ... }`, which does not compile because `role` (not `onClick`) is now the last parameter. Switched to the positional form `FailureActionUiModel("Retry", { ... })`. This is itself a v2.4-vs-v2.5 source-compat nuance: trailing-lambda construction of `FailureActionUiModel` with only a label and a lambda body no longer binds (the lambda would bind to `role`). Only the explicit two-argument form is source-compatible; worth noting for Phase 18 DOC-02 wording. Fixed before commit.

**Total deviations:** 2 (1 process, 1 test fix). **Impact:** none on scope or public API.

## Residuals and hand-offs
1. Gate-2 human checks pending: rung row pitch after the minimum-size modifier (D-04) in the Explorer ApproachLadderCard fixtures; Failure role color and TalkBack announcement of a prefix (no hub gallery fixture exists for role/body/prefix, A5 left the gallery unchanged; confirm at SB-175 integration or ask the owner for a fixture).
2. SB-175 validates the D-02 announcement shape (merged node carries both ContentDescription and Text; whether TalkBack speaks both is unverified on device, RESEARCH A1).
3. `liveRegion` deliberately not added.
4. The raw-line `tools/verify-api-additive.sh` keeps reporting lane 3 for the whole of v2.5 until v2.5.0 is cut (declared false positive); the hook classifier with `--baseline v2.4.1` reported lane 1 or 2 per commit.
5. API.md still describes source-compat-not-binary-compat for Phase 15's classes only; extending it to Failure / FailureActionUiModel (including the trailing-lambda caveat above) is Phase 18 DOC-02 scope.
6. No tag cut, no consumer touched, no src/main edit, 16-VALIDATION.md untouched. No authentication gates, no deferred issues.

## Self-Check: PASSED
- Files exist: VoiceI18nSourceCompatTest.kt, OutcomeSheetTest.kt, this SUMMARY.
- Commit `385719c` present in `git log`; lists exactly the two test files; `git rev-list --count 9ade39e..HEAD` = 1 before the docs commit.
