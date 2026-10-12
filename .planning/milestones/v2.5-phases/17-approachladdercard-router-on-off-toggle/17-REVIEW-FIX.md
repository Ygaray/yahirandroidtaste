---
phase: 17-approachladdercard-router-on-off-toggle
fixed_at: 2026-10-05T00:00:00Z
review_path: /home/yahir/Projects/Reusable/android/yahirandroidtaste/.planning/phases/17-approachladdercard-router-on-off-toggle/17-REVIEW.md
iteration: 1
findings_in_scope: 4
fixed: 3
skipped: 1
status: resolved
---

# Phase 17: Code Review Fix Report

**Fixed at:** 2026-10-05
**Source review:** /home/yahir/Projects/Reusable/android/yahirandroidtaste/.planning/phases/17-approachladdercard-router-on-off-toggle/17-REVIEW.md
**Iteration:** 1

**Summary:**
- Findings in scope: 4 (fix_scope: all)
- Fixed: 3
- Skipped: 1 (documented acceptable-skip, deferred to Phase 18)

Status is `resolved`: every finding is either fixed or a documented acceptable-skip, and no
blocker / critical / high finding is open.

**Execution notes:**
- Fixes were applied sequentially on the main checkout (`branch main`), not in an isolated worktree,
  per the orchestrator's explicit constraint (the tree carries unrelated uncommitted files that must
  not be staged or stashed). Only explicit paths were staged; the pre-existing `.planning/*`,
  `.gsd/`, `.oc-audit/`, `graphify-out/`, `docs/superpowers/` files are untouched.
- Verification ran in the main checkout, so the numbers below are reproducible from this tree.

## Fixed Issues

### WR-02: Two private overloads of `ApproachLadderCardFixture` both have all-default parameters

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/VoiceCommandFamilyScreen.kt`
**Commit:** 24c5b36
**Applied fix:** Renamed the 3-param router-aware fixture to `ApproachLadderCardRouterFixture`; the
retained 2-param `ApproachLadderCardFixture` wrapper delegates to it, and the "router on" Variants
cell now calls `ApproachLadderCardRouterFixture(initialRouter = true)`. No call site relies on
Kotlin's fewest-defaults overload ranking any more. The wrapper keeps its shape, so the registry
lambdas' synthetic accessor descriptor `access$ApproachLadderCardFixture(ZLjava/lang/String;Landroidx/compose/runtime/Composer;II)V`
is unchanged.
**Verification:**
- Binary gate (javap descriptor diff, release AAR vs cached v2.4.1 AAR): `base=2526 head=2584 missing=0`;
  v2.4.1 `ApproachLadderCard` shim descriptor present in head; the fixture accessor descriptor present.
- `./gradlew testDebugUnitTest --tests '*ApproachLadderCard*' --tests '*ComponentRegistry*' --tests '*ComponentPlaygroundIntegrityTest' --tests '*ComponentStatesMatrixTest' detekt apiCheck` -> BUILD SUCCESSFUL.
- `apiDump` + `cmp`: `api.txt` byte-identical; `apiCheck` green.
**Hook lane:** hook printed `LANE 2` (the diff rewrites existing fixture lines) and blocked the first
attempt; the identical commit was re-run with `HUB_LANE_OVERRIDE=2` (the exact printed lane, no
`--no-verify`). Safe: private explorer-only code, no public API or ABI change (proven by the gates above).

### IN-01: Compat test name and comment overstate what is asserted

**Files modified:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderRouterCompatTest.kt`
**Commit:** 236b1c6
**Applied fix:** The shim card and the direct current-overload card are now each wrapped in a tagged
`Box` (`shim_card`, `direct_card`). The test asserts, scoped via `hasAnyAncestor`, that the shim card
has exactly one offline toggle and zero router toggles / zero "Router" content descriptions, and that
the direct card carries exactly one router toggle. The global counts are kept as a cross-check.
**Verification:** compat class ran 2 tests, 0 failures; `VoiceBinaryCompatShimTest` and
`VoiceI18nSourceCompatTest` still green and byte-identical to `0956d79`.
**Hook lane:** LANE 1, no override.

### IN-02: New router KDoc omits the D-05 hideable-by-null-prop reference

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt`
**Commit:** 7c040ca
**Applied fix:** The class-level KDoc sentence enumerating hideable controls now also lists the
`null` `[router]`/`[onRouterChange]` pair. KDoc only; no code change.
**Verification:** detekt green; `api.txt` unchanged (`apiDump` + `cmp`); hidden v2.4.1 shim block still
byte-identical to `0956d79` with exactly one `DeprecationLevel.HIDDEN`.
**Hook lane:** hook printed `LANE 2` (two existing KDoc lines were rewritten) and blocked the first
attempt; the identical commit was re-run with `HUB_LANE_OVERRIDE=2` (the exact printed lane, no
`--no-verify`). Safe: comment-only change.

## Skipped Issues

### WR-01: API.md does not document the new router parameters

**File:** `API.md:222-250`
**Reason:** skipped: deferred to Phase 18 DOC-02 by plan source_audit. 17-01-PLAN.md `source_audit`
lists "API.md / INTEGRATION.md documentation of the router toggle" as `EXCLUDED (DOC-02 owns it)`,
and the orchestrator constrained this fix pass not to edit `API.md`. This is a documented
acceptable-skip, not a failed or unapplied fix; the router params, the `require` pairing contract and
the v2.5.0 append-only note are Phase 18's deliverable and should be confirmed there.
**Original issue:** `API.md` does not mention `router`, `onRouterChange`, `routerOnLabel` or
`routerOffLabel`, so consumers wiring from `API.md` will not discover the toggle or its throw-on-violation
pairing contract.

## Final state

- `api.txt` unchanged by the fixes (the Phase 17 one-line delta from the original plan is intact).
- Untouched, byte-identical to `0956d79`: hidden v2.4.1 overload, `SegmentedOptionSelector.kt`,
  `model/`, `VoiceBinaryCompatShimTest.kt`, `VoiceI18nSourceCompatTest.kt`. `17-VALIDATION.md` not edited.
- No tag cut, no consumer touched, no push.
- `git status --porcelain -- src api.txt` is empty after the three commits.

---

_Fixed: 2026-10-05_
_Fixer: Claude (gsd-code-fixer)_
_Iteration: 1_
