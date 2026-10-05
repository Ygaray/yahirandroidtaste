---
phase: 17-approachladdercard-router-on-off-toggle
plan: 01
subsystem: ui
tags: [compose, material3, segmented-toggle, binary-compat, metalava, javap-abi-gate, explorer-fixture]

requires:
  - phase: 15-voice-surface-i18n-label-params
    provides: onlineLabel / offlineOnlyLabel caller-localizable label convention and the offline toggle this one mirrors
  - phase: 16-a11y-failure-enrichment
    provides: the v2.4.1 hidden-shim + data-class-synthetic baseline the binary gate protects
provides:
  - "ApproachLadderCard router: Boolean? / onRouterChange: ((Boolean) -> Unit)? pair (require()-paired, null hides) plus routerOnLabel / routerOffLabel"
  - "Router ON/OFF segmented toggle rendered below the offline toggle (testTag approach_ladder_card_router_toggle)"
  - "Explorer fixture router demo state + appended 'router on' Variants cell, exercised through ComponentRegistry"
  - "Gate-2 UAT fragment for the two-stacked-toggles visual judgment"
affects: [18-docs (DOC-02 API.md / INTEGRATION.md), 19-release (v2.5.0 cut + consumer repin)]

actuals:
  tokens: 8800
  tasks: 3
  commits: 5
plan_head_before: 2d355c3cb8da6ef54cfec0b70667441eb1a1acdb

tech-stack:
  added: []
  patterns:
    - "Append-only signature growth: new params LAST on the current overload, hidden v2.4.1 overload byte-identical"
    - "Private Explorer fixture keeps its original two-parameter shape as a wrapper so Kotlin synthetic access$ descriptors stay stable for the javap binary gate"

key-files:
  created:
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderRouterCompatTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ApproachLadderCardGalleryDemoTest.kt
    - .planning/uat-pending/17-approachladdercard-router-on-off-toggle.md
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/VoiceCommandFamilyScreen.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCardTest.kt
    - api.txt

key-decisions:
  - "Option order is listOf(routerOffLabel, routerOnLabel) and the card emits onRouterChange(index == 1): the target value of the tapped segment, never a negation of current state (idempotent re-taps)"
  - "Router stays display + emit only: no state holder, gesture or navigation construct added to the card; it never feeds rung-derived state"
  - "Explorer fixture keeps ApproachLadderCardFixture(initialOfflineOnly, initialMaxTierId) as a thin wrapper (router OFF) and adds a same-named router-aware overload, so the registry lambdas' synthetic accessor descriptor is unchanged (binary gate missing=0)"

patterns-established:
  - "A private composable called from registry lambdas leaks a public synthetic access$ descriptor into the AAR; changing its parameter list trips the javap ABI diff even though it is not API"

requirements-completed: [VAPPR-04]

coverage:
  - id: D1
    description: "Router ON/OFF toggle renders below the offline toggle, reflects state (false = Router off selected, true = Router on selected) and emits the tapped segment's target value"
    requirement: VAPPR-04
    verification:
      - kind: unit
        ref: "src/test/.../component/ApproachLadderCardTest.kt#toggling router emits onRouterChange with the new value"
        status: pass
      - kind: unit
        ref: "src/test/.../component/ApproachLadderCardTest.kt#router true renders Router on selected and Router off not selected with the English defaults"
        status: pass
      - kind: unit
        ref: "src/test/.../component/ApproachLadderCardTest.kt#the router toggle sits below the offline toggle which sits below the last rung"
        status: pass
    human_judgment: false
  - id: D2
    description: "Null pair hides the toggle with no dead space and half pairs throw in both directions; null -> false -> true -> null flips cleanly"
    requirement: VAPPR-04
    verification:
      - kind: unit
        ref: "src/test/.../component/ApproachLadderCardTest.kt#router toggle is not rendered when the pair is null"
        status: pass
      - kind: unit
        ref: "src/test/.../component/ApproachLadderCardTest.kt#a non-null router with a null onRouterChange throws -- the pairing invariant is enforced"
        status: pass
      - kind: unit
        ref: "src/test/.../component/ApproachLadderCardTest.kt#router toggle appears and disappears as the pair flips between null and non-null"
        status: pass
    human_judgment: false
  - id: D3
    description: "Caller-localizable routerOnLabel / routerOffLabel replace the segment text with English defaults when omitted"
    requirement: VAPPR-04
    verification:
      - kind: unit
        ref: "src/test/.../component/ApproachLadderCardTest.kt#supplied router labels replace the Router on and Router off segments and still emit"
        status: pass
    human_judgment: false
  - id: D4
    description: "Router never changes rung order, Capped / Needs network affordances, cap selection or rung positions; rung / offline / router taps emit only their own callback; two cards keep independent callbacks"
    requirement: VAPPR-04
    verification:
      - kind: unit
        ref: "src/test/.../component/ApproachLadderCardTest.kt#toggling router leaves rung order capped and needs-network affordances and cap selection unchanged"
        status: pass
      - kind: unit
        ref: "src/test/.../component/ApproachLadderCardTest.kt#tapping a router segment emits only onRouterChange while rung and offline taps emit only their own callbacks"
        status: pass
    human_judgment: false
  - id: D5
    description: "Append-only binary/source compatibility: hidden v2.4.1 overload renders through with router = null, Phase-15 eleven-positional shape still binds, api.txt delta is one line, apiCheck green vs released v2.4.1, javap descriptor diff missing = 0"
    requirement: VAPPR-04
    verification:
      - kind: unit
        ref: "src/test/.../component/ApproachLadderRouterCompatTest.kt#v2_4_1 descriptor defaults router to null and renders no router toggle"
        status: pass
      - kind: other
        ref: "javap public-descriptor diff of release AAR vs cached v2.4.1 AAR: base=2526 head=2584 missing=0"
        status: pass
    human_judgment: false
  - id: D6
    description: "Explorer fixture shows a live router toggle in every cell plus an appended 'router on' cell, reached through ComponentRegistry"
    requirement: VAPPR-04
    verification:
      - kind: unit
        ref: "src/test/.../explorer/ApproachLadderCardGalleryDemoTest.kt#defaultCell_rendersLiveRouterToggle_belowOfflineToggle"
        status: pass
      - kind: unit
        ref: "src/test/.../explorer/ApproachLadderCardGalleryDemoTest.kt#variantsContent_showsRouterOffAndRouterOnCells_andHiddenCellsStayToggleFree"
        status: pass
    human_judgment: false
  - id: D7
    description: "Visual judgment of two stacked segmented toggles at the card bottom (spacing, card height growth, light / dark contrast)"
    requirement: VAPPR-04
    verification: []
    human_judgment: true
    rationale: "Spacing, height growth and contrast are taste judgments no test asserts; registered as a Gate-2 owner item in .planning/uat-pending/17-approachladdercard-router-on-off-toggle.md"

duration: 12min
completed: 2026-10-05
status: complete
---

# Phase 17 Plan 01: ApproachLadderCard Router ON/OFF Toggle Summary

**Router ON/OFF segmented toggle on ApproachLadderCard via a require()-paired `router` / `onRouterChange` pair plus localizable `routerOnLabel` / `routerOffLabel`, appended last so v2.4.1-compiled consumers still link (javap diff vs the v2.4.1 AAR: missing = 0).**

## Performance

- **Duration:** 12 min
- **Started:** 2026-10-05T17:13:00Z
- **Completed:** 2026-10-05T17:25:00Z
- **Tasks:** 3 (Task 3 verification-only, no commit)
- **Files modified:** 7 (3 created, 4 modified)

## Accomplishments
- `ApproachLadderCard` gains `router: Boolean? = null`, `onRouterChange: ((Boolean) -> Unit)? = null`, `routerOnLabel = "Router on"`, `routerOffLabel = "Router off"`, appended LAST on the current overload with a third `require()` cloned from the offline pair. The toggle renders directly below the offline toggle (testTag `approach_ladder_card_router_toggle`), option order `listOf(routerOffLabel, routerOnLabel)`, emit `onRouterChange(index == 1)`.
- The hidden v2.4.1 overload is byte-identical; a new sibling test pins `router = null` through its descriptor and the Phase-15 eleven-positional call shape.
- 12 router tests in `ApproachLadderCardTest` (21 -> 33), 2 in the compat sibling, 3 registry-reached gallery tests. Full suite 751 tests, 22 skipped, 0 failures (baseline 734 + 17).
- Explorer fixture shows a live router toggle (OFF) in every cell and an appended "router on" Variants cell; the every-control-hidden cell stays toggle-free.
- Gate-2 UAT fragment registered for the two-stacked-toggles visual judgment.

## Task Commits

Each task was committed atomically (hook lane in brackets; no `HUB_LANE_OVERRIDE`, no `--no-verify` anywhere):

1. **Task 1: tracer (tests first, appended params + require + render, api.txt same commit)** - `b7a4e48` (feat) [LANE 1]
2. **Task 2: behavior matrix tests** - `902d030` (test) [LANE 1]
3. **Task 2: Explorer fixture router demo state + gallery tests** - `1b1974e` (feat) [LANE 1]
4. **Task 2: Gate-2 UAT registration** - `ebddac2` (docs) [via `gsd_run query commit`; amended once to add the Co-Authored-By trailer, unpushed]
5. **Task 3 gate fix: keep fixture synthetic accessor descriptor** - `26b66a1` (fix) [LANE 1, see Deviations]

Task 3 is verification-only and produced no commit of its own. **Plan metadata:** the final docs commit (SUMMARY + STATE + ROADMAP).

Precondition at start: `git diff --quiet 0956d79 -- src api.txt` exited 0 (HEAD `2d355c3`); the only `router` hit in `src/main` was the icon name in `IconPickerGrid.kt`, so the chosen testTag was unused.

## api.txt delta (exactly one removed + one added line)

```
-    method ... ApproachLadderCard(..., optional String onlineLabel, optional String offlineOnlyLabel);
+    method ... ApproachLadderCard(..., optional String onlineLabel, optional String offlineOnlyLabel, optional Boolean? router, optional kotlin.jvm.functions.Function1<java.lang.Boolean,kotlin.Unit>? onRouterChange, optional String routerOnLabel, optional String routerOffLabel);
```

## Closing-gate outcomes (Task 3, final run after the fix commit; all PASS)

1. `./gradlew testDebugUnitTest detekt` - BUILD SUCCESSFUL; 751 tests, 22 skipped, 0 failures, 0 errors; detekt green at zero baseline (`config/detekt-baseline.xml` unchanged).
2. apiCheck against the RELEASED v2.4.1 `api.txt` (temporarily written, restored by `git checkout`) - BUILD SUCCESSFUL, rc=0; `git status --porcelain -- api.txt` empty afterwards.
3. apiDump + `cmp` - byte-identical (FRESH_OK); apiCheck against committed `api.txt` - green. (Gradle reports `apiDump` UP-TO-DATE on the re-run, so freshness rests on apiCheck against the committed file after the final source plus the Task 1 / Task 2 dumps.)
4. Ten-symbol removed-line allowlist vs v2.4.1 - ALLOWLIST_OK.
5. Phase 17 api delta vs `0956d79` - exactly one removed + one added `ApproachLadderCard(` line (ONELINE_OK).
6. Binary gate, verbatim: `base=2526 head=2584 missing=0` (both floors >= 2000; v2.4.1 shim descriptor `ApproachLadderCardKt#ApproachLadderCard(List;Boolean;Function1;String;Function1;Modifier;Composer;II)V` present in head).
7. Hidden v2.4.1 overload block byte-identical to `0956d79`; exactly one `DeprecationLevel.HIDDEN`; card diff removes exactly one line (`offlineOnlyLabel: String = "Offline only"` trailing-comma re-emit).
8. Committed src change set vs `0956d79` is exactly the five expected files; `SegmentedOptionSelector.kt` and `feedback/` identical to v2.4.1; both protected compat tests byte-identical.
9. No import outside androidx / kotlin / android / library namespace added since v2.4.1 (INV-01).
10. No `@HiltAndroidApp` / `@AndroidEntryPoint` added (INV-02).
11. No `remember` / `mutableStateOf` / `clickable` / `selectable` / `navigate` / `pointerInput` in the card's added lines (stateless, no gesture).
12. Test-count pins: 33 / 2 / 3.
13. `git status --porcelain -- src api.txt` empty; no `v2.5*` tag, no tag at HEAD; UAT fragment present; `17-VALIDATION.md` still `status: draft` / `nyquist_compliant: false` (not edited).

`tools/verify-binary-abi.sh` (F3) did NOT exist at execute time, so the inline javap recipe was the gate.

## Files Created/Modified
- `src/main/.../component/ApproachLadderCard.kt` - four appended params, third `require()`, router render block, insert-only KDoc.
- `src/main/.../explorer/VoiceCommandFamilyScreen.kt` - router-aware fixture overload, wrapper retained, appended "router on" cell (pure insertions).
- `src/test/.../component/ApproachLadderCardTest.kt` - 12 router tests (tracer + 11-test matrix).
- `src/test/.../component/ApproachLadderRouterCompatTest.kt` - v2.4.1-shim-defaults-router-null and eleven-positional pins.
- `src/test/.../explorer/ApproachLadderCardGalleryDemoTest.kt` - 3 registry-reached fixture tests.
- `api.txt` - the one regenerated line.
- `.planning/uat-pending/17-approachladdercard-router-on-off-toggle.md` - Gate-2 fragment.

## Decisions Made
- Emit the tapped segment's target value (`index == 1`), never a negation, so repeated taps cannot flip policy (pinned by the idempotency test).
- Keep the router state caller-owned: the card adds no state holder.
- Explorer fixture compat shape: see Deviations.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Explorer fixture signature change broke the D-03 binary gate (missing=1)**
- **Found during:** Task 3 (closing javap diff), caused by Task 2 Step 3
- **Issue:** Task 2 Step 3 inserts `initialRouter` into the private `ApproachLadderCardFixture`. Kotlin emits a public synthetic `access$ApproachLadderCardFixture(Z,String,Composer,II)V` in `VoiceCommandFamilyScreenKt` for the registry lambdas; its descriptor became `(ZZString;Composer;II)V`, so the javap diff vs the v2.4.1 AAR reported `missing=1`. Not a real consumer break (private fixture, synthetic accessor no compiled caller can reference), but the plan's gate demands 0 and says to fix the source, not the gate.
- **Fix:** Kept the original two-parameter `ApproachLadderCardFixture(initialOfflineOnly, initialMaxTierId)` untouched in shape (now a thin wrapper that passes `initialRouter = false`) and left the router-aware body as a same-named overload with `initialRouter: Boolean = false`. Kotlin resolves the existing no-router calls to the original two-parameter function, so the accessor descriptor is unchanged. An interim attempt (making `initialRouter` required) flipped the hook to LANE 2 because it removed a just-committed line; reverting to the defaulted form made the diff a pure insertion again (LANE 1, no override).
- **Files modified:** `src/main/.../explorer/VoiceCommandFamilyScreen.kt`
- **Verification:** `base=2526 head=2584 missing=0`; gallery, states-matrix, playground-integrity and drift-guard tests green; DS-05 guard PASS (0 removed lines).
- **Committed in:** `26b66a1`

---

**Total deviations:** 1 auto-fixed (1 bug). **Impact on plan:** No scope change. The plan's literal acceptance strings still hold (`initialRouter: Boolean = false,`, `ApproachLadderCardFixture(initialRouter = true)`); only the wrapper layout differs. The plan's Task 2 Step 3 and Task 3 gate are mutually inconsistent as written; future plans that add a param to a private composable called from registry lambdas should expect this.

## Issues Encountered
- The first draft of the require() message split the phrase across two string literals, so the acceptance grep `router and onRouterChange must both be null or both be non-null` counted 0; reflowed to a single literal (same message text) before the Task 1 commit.
- `gsd_run query commit` omitted the Co-Authored-By trailer on the UAT docs commit; amended the unpushed commit to add it (only that one file was in it).

## Residuals and hand-offs
1. Gate-2 owner visual UAT pending: two stacked segmented toggles at the card bottom (spacing, card height growth, light / dark contrast), registered in `.planning/uat-pending/17-approachladdercard-router-on-off-toggle.md`.
2. The accessibility state words "selected" / "not selected" stay English (declared Phase 15 residual; `SegmentedOptionSelector` untouched).
3. The stale KDoc sentence in `VoiceBinaryCompatShimTest` ("Phase 17 (F2) may add further shims") was left unedited on purpose - Phase 17 added none and editing it would break the byte-identical gate.
4. `tools/verify-binary-abi.sh` (F3) did not exist at execute time; the inline javap recipe was used.
5. API.md / INTEGRATION.md documentation of the router toggle is Phase 18 DOC-02.
6. The raw-line `tools/verify-api-additive.sh` keeps reporting lane 3 for v2.5 until v2.5.0 is cut (declared false positive); the pre-commit hook's own lane stayed 1 on every code commit.
7. No tag cut, no consumer touched; the v2.5.0 cut and consumer repin are human-gated (Phase 19).
8. Gate-1 agentic self-UAT is driven by the post-execute verify workflow using the Explorer fixture as the demo surface, not by this plan. No device or app was driven here.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Phase 17 code complete; ready for Gate-1 verification, then Phase 18 (docs) and Phase 19 (v2.5.0 cut).
- Pre-existing unrelated uncommitted working-tree files (`.planning/config.json`, graphs, `state.json`, `v2.5-MILESTONE-RUN.md`, untracked `.gsd/`, `.oc-audit/`, `graphify-out/`, `docs/superpowers/`) were left untouched.

---
*Phase: 17-approachladdercard-router-on-off-toggle*
*Completed: 2026-10-05*
