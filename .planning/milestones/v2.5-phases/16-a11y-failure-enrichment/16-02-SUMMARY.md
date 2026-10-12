---
phase: 16-a11y-failure-enrichment
plan: 02
subsystem: ui-voice-failure
tags: [compose, accessibility, semantics, data-class, metalava, additive-api, OutcomeSheet]
requires: []
provides:
  - "FailureActionUiModel.role (default Neutral) wired to the Failure action button"
  - "VoiceOutcomeUiState.Failure.body optional composable slot inside the error surface"
  - "VoiceOutcomeUiState.Failure.semanticsPrefix merged-node contentDescription (prefix + one ASCII space + reason)"
affects: [16-03, 18-docs, 19-release]
tech-stack:
  added: []
  patterns:
    - "@JvmOverloads constructor + hand-written old-arity copy (Phase 15 recipe) on two more shipped data classes"
    - "Source-contract test for wiring Robolectric cannot observe (button color)"
key-files:
  created:
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/FailureRoleSourceContractTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/model/FailureActionUiModel.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/model/VoiceOutcomeUiState.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheet.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/model/VoiceModelLabelDefaultsTest.kt
    - api.txt
key-decisions:
  - "Body slot placed after the handled-by row and before the action, no wrapper container (null body adds no node/space; rides the existing scroll region)"
  - "No live-region semantics added (left open for SB-175 validation); merge-only semantics, never the child-clearing variant"
  - "Prefix joined to the reason by a single ASCII space (punctuation belongs to the prefix); null, empty and whitespace-only prefixes are treated as null"
  - "Gallery (explorer/) left unchanged (RESEARCH A5): all new fields are defaulted, no fixture requested"
requirements-completed: [VFAIL-01, VFAIL-02, VFAIL-03]
duration: 8 min
completed: 2026-10-05
status: complete
commits: 2
plan_head_before: 04f142c9bfb0eb2cf87f51c02e7f254b91557157
actuals:
  tokens: 9000
  tasks: 2
  commits: 2
coverage:
  - deliverable: "FailureActionUiModel.role defaults to Neutral, survives legacy copy, and FailureBody binds role = action.role (no hardcoded Neutral)"
    verification:
      - kind: test
        ref: "VoiceModelLabelDefaultsTest#FailureActionUiModel role/copy/arity tests; FailureRoleSourceContractTest"
        status: pass
    human_judgment: false
  - deliverable: "Each of Save/Destructive/Neutral renders exactly one clickable action button that fires onClick"
    verification:
      - kind: test
        ref: "OutcomeSheetTest#Failure action with the {Save,Destructive,Neutral} role renders one clickable action button"
        status: pass
    human_judgment: false
  - deliverable: "Failure.body renders inside the error surface, between handled-by and the action; null body unchanged"
    verification:
      - kind: test
        ref: "OutcomeSheetTest#Failure body renders inside the failure surface, #Failure body sits below handled-by and above the action, #Failure body without an action ..."
        status: pass
    human_judgment: false
  - deliverable: "Failure.semanticsPrefix yields contentDescription 'prefix reason'; null/blank leave it undefined; verbatim; action still clickable"
    verification:
      - kind: test
        ref: "OutcomeSheetTest#Failure semanticsPrefix ... (5 tests) and #Failure action stays a separate clickable node ..."
        status: pass
    human_judgment: false
  - deliverable: "Old constructor/copy arities survive; legacy copy carries every new field; apiCheck green; api.txt one removed line per commit"
    verification:
      - kind: test
        ref: "VoiceModelLabelDefaultsTest arity pins + legacy-arity drift guard; ./gradlew apiCheck"
        status: pass
    human_judgment: false
  - deliverable: "Rendered role color and the TalkBack announcement of a prefix on a real device"
    human_judgment: true
    rationale: "Robolectric cannot assert button color and TalkBack speech precedence (text vs contentDescription on a merged node) is unverified; the hub gallery has no role/body/prefix fixture, so this is confirmed at SB-175 integration (D-02) or via an owner-requested gallery fixture"
---

# Phase 16 Plan 02: Failure enrichment (VFAIL-01..03) Summary

**`VoiceOutcomeUiState.Failure` now takes an optional `body` slot and `semanticsPrefix`, and `FailureActionUiModel` takes an optional `role`, all appended through the Phase 15 `@JvmOverloads` + legacy-`copy` recipe so v2.4 callers and `apiCheck` are untouched.**

## Performance
- **Duration:** 8 min (about 04:05 to 04:12 local) | **Tasks:** 2 (tracer + auto, both TDD) | **Files:** 6 modified, 1 created

## Accomplishments
- Task 1 (tracer): `FailureActionUiModel` -> `@JvmOverloads constructor` with `role` appended after `onClick` (reuses `ActionButtonDefaults.ActionButtonRole`, default `Neutral`) and a hand-written `copy(label, onClick)` that carries the current `role`. `FailureBody` passes `role = action.role` instead of the hardcoded `Neutral`. Wiring pinned by `FailureRoleSourceContractTest` (Robolectric cannot assert color).
- Task 2: `Failure` -> `@JvmOverloads constructor` with `body` then `semanticsPrefix` appended after `action`, plus a hand-written three-argument `copy` carrying both. `FailureBody` invokes `failure.body?.invoke()` directly after the handled-by row and before the action; a non-blank prefix adds `Modifier.semantics(mergeDescendants = true) { contentDescription = "<prefix> <reason>" }` on the surface.
- 3 new source-contract tests, 12 new Compose tests, 9 new JVM model tests (incl. the extended all-non-default drift guard for both classes).

## Task Commits
| Task | Commit | Message |
|------|--------|---------|
| 1 | `4f5f61c` | feat(16-02): FailureActionUiModel.role wired to the Failure action button (VFAIL-01) |
| 2 | `42d8458` | feat(16-02): Failure.body slot and Failure.semanticsPrefix (VFAIL-02, VFAIL-03, D-02) |

## api.txt per commit (apiCheck was green BEFORE each apiDump)
**`4f5f61c` — removed (exactly 1):**
`- method public ...FailureActionUiModel copy(optional String label, optional kotlin.jvm.functions.Function0<kotlin.Unit> onClick);`
**added (7):** 3-parameter `ctor ... (String label, Function0 onClick, optional ...ActionButtonRole role)`, `component3()`, hand-written `copy(String label, Function0 onClick)`, generated 3-parameter `copy(optional ...)`, `getRole()`, `property role`. The old 2-parameter `ctor` line survives verbatim.

**`42d8458` — removed (exactly 1):**
`- method public ...VoiceOutcomeUiState.Failure copy(optional String reason, optional HandledByUiModel? handledBy, optional FailureActionUiModel? action);`
**added (14):** 1-, 2-, 4- and 5-parameter constructor overloads (the old 3-parameter `ctor` line survives verbatim), `component4`, `component5`, hand-written 3-parameter `copy`, generated 5-parameter `copy`, `getBody`, `getSemanticsPrefix`, `property body`, `property semanticsPrefix`.

Plan-level check: `git diff v2.4.1 -- api.txt` removed lines naming these two classes = exactly 2 (the two superseded generated `copy(optional ...)` lines). `git diff v2.4.1 -- explorer/` is empty. No new non-androidx/non-library import since v2.4.1 in `src/main` (INV-01).

## Verification results
- `./gradlew testDebugUnitTest detekt` (full suite): BUILD SUCCESSFUL, zero-baseline detekt green.
- Test classes after Task 2: `VoiceModelLabelDefaultsTest` 27/27, `FailureRoleSourceContractTest` 3/3, `OutcomeSheetTest` 47/47 (0 failures, 0 skipped).
- `apiDump` then `cmp` against the pre-dump file: identical (api.txt fresh) after both tasks; `apiCheck` BUILD SUCCESSFUL after both.
- Acceptance greps all PASS: `@JvmOverloads constructor` on both classes; `Failure` val order `reason handledBy action body semanticsPrefix`; FailureBody order HandledByRow < failure.body < failure.action; `mergeDescendants = true` and `contentDescription` present in FailureBody; no `clearAndSetSemantics` and no `liveRegion` in code lines of OutcomeSheet.kt; no `ActionButtonRole.Neutral` in FailureBody.
- RED observed first. Task 1: `FailureRoleSourceContractTest` (2 of 3 fail on assertions on the unmodified source; the model tests are compile-red by construction since `role` did not exist). Task 2: with the model fields added but rendering unchanged, 6 Compose tests failed on assertions (body-inside-surface, vertical order, body-without-action, prefix exact, prefix verbatim, action-still-clickable-under-prefix); the null/empty/blank-prefix tests and role tests pass pre-fix as regression pins, as the plan anticipated.

## Claude's-discretion choices as applied
- Body placement: after `HandledByRow`, before the action, no wrapper container (null body adds no node and no space).
- No live-region semantics added (CONTEXT left it open for SB-175).
- Prefix joiner: one ASCII space (mirrors the Phase 15 WR-03 joining rule); blank/empty prefix treated as null.
- Merge-only semantics on the surface; the action button stays a separate clickable node (asserted by a tap test under a non-blank prefix).

## Deviations from Plan

**1. [Rule 3 - Blocking] HUB_LANE_OVERRIDE=2 instead of 3**
- Found during: Task 1 commit. The classifier (`tools/classify-hub-change.sh --baseline v2.4.1`) reports `LANE 2 (mode=additive, baseline=v2.4.1)` for both commits, not lane 3 as the plan assumed; the hook requires the override to equal the detected lane exactly, so both commits used `HUB_LANE_OVERRIDE=2` (same as 16-01 and Phase 15). The authoritative additive gate (`apiCheck`) was green before each dump, and each commit removes exactly one api.txt line. Commits: `4f5f61c`, `42d8458`.

**2. [Rule 1 - Bug in test] `onNode` import**
- `androidx.compose.ui.test.onNode` is a `ComposeTestRule` member, not a top-level extension, so the plan's suggested import failed to compile; dropped it (the tests call `composeTestRule.onNode(...)`). File: `OutcomeSheetTest.kt`, in commit `42d8458`.

**3. [Process] Valid RED sequencing for Task 2**
- Because the new Compose tests reference `body`/`semanticsPrefix`, they cannot compile before the model change. To observe assertion-level RED (not a compile error) the model edit was applied first, then the render change; the 6 behavioral tests failed in between as listed above. Both land in the single task commit as planned.

**Total deviations:** 3 (1 blocking-process, 1 test fix, 1 sequencing note). **Impact:** none on scope or public API.

## Gate-2 notes (human, not run here)
- The rendered color of the Save/Destructive/Neutral Failure action button needs a device; Robolectric cannot assert it (structural wiring is pinned by `FailureRoleSourceContractTest`).
- TalkBack's announcement of a prefix: the merged node carries both `ContentDescription = "<prefix> <reason>"` and the descendant `Text`; whether TalkBack speaks `handledBy`/body text alongside the description is unverified (RESEARCH A1). The hub gallery has no role/body/prefix fixture, so this is confirmed at SB-175 integration (D-02) or via an owner-requested gallery fixture.

## Notes for later phases
- Phase 18 (DOC-02): the API.md source-compatible-NOT-binary-compatible wording for appended data-class fields should also cover `FailureActionUiModel` and `VoiceOutcomeUiState.Failure` (documentation not done here).
- No tag cut, no consumer touched, `explorer/` and `ComponentRegistry` unchanged, 16-VALIDATION.md untouched. No authentication gates, no deferred issues.

## Self-Check: PASSED
- Files exist: FailureActionUiModel.kt, VoiceOutcomeUiState.kt, OutcomeSheet.kt, FailureRoleSourceContractTest.kt, OutcomeSheetTest.kt, VoiceModelLabelDefaultsTest.kt, api.txt, this SUMMARY.
- Commits `4f5f61c` and `42d8458` present in `git log`; each lists exactly its five plan files; `git rev-list --count 04f142c..HEAD` = 2 (before the docs commit).
