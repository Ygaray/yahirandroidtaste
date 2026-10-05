---
phase: 17-approachladdercard-router-on-off-toggle
verified: 2026-10-05T00:00:00Z
status: passed
score: 11/11 must-haves verified
covered_files:
  - .planning/REQUIREMENTS.md
  - .planning/phases/17-approachladdercard-router-on-off-toggle/17-01-PLAN.md
  - .planning/phases/17-approachladdercard-router-on-off-toggle/17-01-SUMMARY.md
  - api.txt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/VoiceCommandFamilyScreen.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCardTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderRouterCompatTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ApproachLadderCardGalleryDemoTest.kt
covered_digest: "v1:sha256:546ef6249011c5daad118f0b59ad341db4bac726af6cba7091106d2aec0c9062"
behavior_unverified: 0
overrides_applied: 0
---

# Phase 17: ApproachLadderCard Router ON/OFF toggle Verification Report

**Phase Goal:** ApproachLadderCard can surface a Router ON/OFF policy toggle driven by an additive `router: Boolean? = null` + `onRouterChange: ((Boolean) -> Unit)? = null` pair, `require()`-paired exactly like `offlineOnly`/`onOfflineOnlyChange`. Null hides it. Policy-card toggle, NOT per-rung navigation.
**Verified:** 2026-10-05
**Status:** passed
**Re-verification:** No, initial verification

## Goal Achievement

### Observable Truths

Baseline for diffs: `0956d79` (Phase-16 state). All claims below were checked against code and a fresh test run, not SUMMARY.md.

| #  | Truth | Status | Evidence |
| -- | ----- | ------ | -------- |
| 1  | SC1: both `router` and `onRouterChange` non-null renders a Router ON/OFF segmented toggle reflecting state | VERIFIED | `ApproachLadderCard.kt` renders `SegmentedOptionSelector(selectedIndex = if (router) 1 else 0, options = listOf(routerOffLabel, routerOnLabel))` tagged `approach_ladder_card_router_toggle`, guarded by `router != null && onRouterChange != null`. Tests for router false/true content descriptions ("Router off, selected" / "Router on, selected") pass. |
| 2  | SC2: tapping the non-selected segment emits the new boolean | VERIFIED | `onSelect = { index -> onRouterChange(index == 1) }`. `toggling router emits onRouterChange with the new value` and the idempotency (re-tap) test pass in a fresh run. |
| 3  | SC3: both null (default) means no node, no "Router" content description, no reserved space; 21 pre-existing tests unchanged | VERIFIED | Render is inside an `if`, so nothing is emitted when null. The base `ApproachLadderCardTest` had 21 `@Test`; test numstat is 276 insertions / 0 deletions on that file, so the originals are untouched. Fresh run: 33 tests (21 + 12 new), 0 failures. Hidden-pair test and flip test pass. |
| 4  | SC4: exactly one of the pair non-null throws IAE in both directions, via a third `require()` after the offline pair | VERIFIED | Third `require((router == null) == (onRouterChange == null))` placed after the offline `require`. Message interpolates only the Boolean and "null"/"non-null". Both direction tests pass. |
| 5  | SC5: rung tap emits only `onMaxTierChange`, router tap only `onRouterChange`, offline tap only `onOfflineOnlyChange`; toggling router leaves rung order / affordances / cap unchanged; no per-rung navigation | VERIFIED | `router` is used only in the guarded render block and the `require`; it feeds no rung-derived state, and `RungRow`/`CapControl` are untouched (diff has no removed lines there). Isolation and rung-intact tests pass (behavior-dependent truth, exercised by named tests, so VERIFIED rather than PRESENT_BEHAVIOR_UNVERIFIED). |
| 6  | D-01: `routerOnLabel`/`routerOffLabel` are caller-localizable defaulted params ("Router on"/"Router off") | VERIFIED | Params present with those defaults; `supplied router labels replace ... and still emit` test passes. State words stay English via `SegmentedOptionSelector` (documented residual, file unedited). |
| 7  | D-02: router toggle below offline toggle; Explorer fixture gains router demo state; one appended "router on" cell; hidden cell stays toggle-free; registry-reached render test | VERIFIED | Placement test (`router toggle sits below the offline toggle ... below the last rung`) passes. `VoiceCommandFamilyScreen.kt` adds `ApproachLadderCardRouterFixture` (`initialRouter`) plus a "router on" Variants cell. `ApproachLadderCardGalleryDemoTest` (3 tests) passes, asserting 2 router toggles in Variants, ON/OFF descriptions, and toggle-free hidden cells. |
| 8  | D-03 append order: four params appended last in order after `offlineOnlyLabel`, current overload only | VERIFIED | Signature order is `... onlineLabel, offlineOnlyLabel, router, onRouterChange, routerOnLabel, routerOffLabel`. The only removed source line in the diff is the old last-param line (`offlineOnlyLabel` lost its trailing comma) plus the reworded-for-append class KDoc line. |
| 9  | D-03 shim: hidden v2.4.1 overload byte-identical; shim tests byte-identical and green; one hidden deprecation | VERIFIED | The shim block shows no `-` lines in the diff vs `0956d79` (delegation passes no router args). `VoiceBinaryCompatShimTest` (4) and `VoiceI18nSourceCompatTest` (4) have zero diff lines and pass. Exactly one `DeprecationLevel.HIDDEN` in the file. |
| 10 | D-03 data classes: no `model/` file or test touched | VERIFIED | `git diff 0956d79 HEAD --stat -- src/main/.../model` is empty; no model test in the diff list. |
| 11 | D-03 gates: apiCheck green; `api.txt` delta is exactly one removed + one added `ApproachLadderCard(` line | VERIFIED | `git diff -U0 0956d79 HEAD -- api.txt` shows exactly one `-` and one `+` line, the `+` ending `optional Boolean? router, ... optional String routerOnLabel, optional String routerOffLabel);`. `./gradlew apiCheck detekt` re-run: exit 0. Binary (javap) gate was recorded green in the fix report and the explorer accessor descriptor was preserved by commit 26b66a1; not re-run here (see note). |

**Score:** 11/11 truths verified (0 present, behavior-unverified)

### Required Artifacts

| Artifact | Expected | Status | Details |
| -------- | -------- | ------ | ------- |
| `component/ApproachLadderCard.kt` | router pair, require, render, KDoc | VERIFIED | Substantive, wired (registry fixture + tests call it) |
| `explorer/VoiceCommandFamilyScreen.kt` | router demo fixture + "router on" cell | VERIFIED | Wired through the registry cells; reached by `ApproachLadderCardGalleryDemoTest` |
| `ApproachLadderCardTest.kt` | router behavior matrix | VERIFIED | 12 new tests, additions only |
| `ApproachLadderRouterCompatTest.kt` | v2.4.1 shim / Phase-15 shape compat | VERIFIED | 2 tests pass |
| `ApproachLadderCardGalleryDemoTest.kt` | registry-reached render | VERIFIED | 3 tests pass |
| `api.txt` | one-line signature delta | VERIFIED | See truth 11 |
| `.planning/uat-pending/17-...md` | Gate-2 fragment | VERIFIED | Present, status pending (owner-deferred) |

### Key Link Verification

| From | To | Via | Status |
| ---- | -- | --- | ------ |
| `ApproachLadderCard` | `SegmentedOptionSelector` | router block, `onSelect` emits `index == 1` | WIRED |
| Explorer fixture | `ApproachLadderCard(router=, onRouterChange=)` | hoisted `remember` state | WIRED |
| Registry lambdas | 2-param fixture wrapper | kept shape, delegates to `ApproachLadderCardRouterFixture` | WIRED |
| v2.4.1 shim | current overload | named-arg delegation, router defaults to null | WIRED |

### Data-Flow Trace (Level 4)

| Artifact | Variable | Source | Real Data | Status |
| -------- | -------- | ------ | --------- | ------ |
| router `SegmentedOptionSelector` | `router` | caller param (Explorer: hoisted state, updated by `onRouterChange`) | Yes | FLOWING |

### Behavioral Spot-Checks

| Behavior | Command | Result | Status |
| -------- | ------- | ------ | ------ |
| Card, compat, gallery, shim tests | `./gradlew testDebugUnitTest --tests '*ApproachLadderCardTest' '*ApproachLadderRouterCompatTest' '*ApproachLadderCardGalleryDemoTest' '*VoiceBinaryCompatShimTest' '*VoiceI18nSourceCompatTest'` | 33 + 2 + 3 + 4 + 4 tests, 0 failures/errors/skips | PASS |
| API + static analysis | `./gradlew apiCheck detekt` | exit 0 | PASS |

### Probe Execution

No probes declared by the plan. Step 7c: SKIPPED (none).

### Requirements Coverage

| Requirement | Source Plan | Description | Status | Evidence |
| ----------- | ----------- | ----------- | ------ | -------- |
| VAPPR-04 | 17-01-PLAN.md | Router ON/OFF toggle via `router`/`onRouterChange`, `require()`-paired, null hides, no per-rung navigation | SATISFIED | Truths 1-5. REQUIREMENTS.md marks it `[x]` / "Phase 17 / Complete". |

Only VAPPR-04 is mapped to Phase 17 in REQUIREMENTS.md and it is the sole ID in the plan frontmatter; no orphaned requirements.

### Anti-Patterns Found

None. No `TBD`/`FIXME`/`XXX`/`TODO`/`HACK` in any of the five modified source/test files. No stub or empty implementations in the changed code.

### Review Status

17-REVIEW.md `status: resolved` (0 critical, 2 warning, 2 info). WR-02, IN-01 and IN-02 fixed (commits 24c5b36, 236b1c6, 7c040ca). WR-01 (API.md missing router docs) is a documented skip, deferred to Phase 18 DOC-02 (ROADMAP Phase 18 SC3 explicitly covers "the router toggle" in API.md/INTEGRATION.md), so it is not a Phase 17 gap.

### Deferred Items

| # | Item | Addressed In | Evidence |
| - | ---- | ------------ | -------- |
| 1 | API.md / INTEGRATION.md documentation of the router params | Phase 18 | ROADMAP Phase 18 SC3: "`API.md` and `INTEGRATION.md` are updated for the new label params, the Failure enrichment, and the router toggle (DOC-02)" |

### Human Verification Required

None blocking. The Gate-2 visual judgment (spacing between the two stacked segmented toggles, card height growth, selected-segment contrast in light/dark) is registered in `.planning/uat-pending/17-approachladdercard-router-on-off-toggle.md` as non-blocking, owner-deferred end-of-phase UAT; per instruction it does not set `human_needed`.

### Notes

- The javap binary-descriptor gate (release AAR vs cached v2.4.1 AAR) was not re-run in this verification; it requires `assembleRelease` plus a cached v2.4.1 AAR. Its result (`missing=0`, shim and fixture accessor descriptors present) is recorded in 17-REVIEW-FIX.md. The source evidence supports it: the shim and 2-param fixture wrapper keep their prior shapes, and the single `api.txt` line delta contains only appended params. Treated as corroborating, not load-bearing, for the verdict.

### Gaps Summary

No gaps. The router toggle is implemented as an additive, `require()`-paired, append-only param set with a hidden v2.4.1 shim left intact, covered by behavioral tests that pass in a fresh run, with `apiCheck` and `detekt` green.

---

_Verified: 2026-10-05_
_Verifier: Claude (gsd-verifier)_
