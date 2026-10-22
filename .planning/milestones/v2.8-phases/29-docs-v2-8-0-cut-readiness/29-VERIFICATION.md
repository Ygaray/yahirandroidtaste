---
phase: 29-docs-v2-8-0-cut-readiness
verified: 2026-10-18T09:00:00Z
status: passed
score: 4/4 must-haves verified
covered_files:
  - ".planning/REQUIREMENTS.md#SHIP-05"
  - ".planning/phases/29-docs-v2-8-0-cut-readiness/29-01-PLAN.md"
  - ".planning/phases/29-docs-v2-8-0-cut-readiness/29-01-SUMMARY.md"
  - ".planning/phases/29-docs-v2-8-0-cut-readiness/29-02-PLAN.md"
  - ".planning/phases/29-docs-v2-8-0-cut-readiness/29-02-SUMMARY.md"
  - ".planning/phases/29-docs-v2-8-0-cut-readiness/29-03-PLAN.md"
  - ".planning/phases/29-docs-v2-8-0-cut-readiness/29-03-SUMMARY.md"
  - ".planning/phases/29-docs-v2-8-0-cut-readiness/29-04-PLAN.md"
  - ".planning/phases/29-docs-v2-8-0-cut-readiness/29-04-SUMMARY.md"
  - ".planning/phases/29-docs-v2-8-0-cut-readiness/29-05-PLAN.md"
  - ".planning/phases/29-docs-v2-8-0-cut-readiness/29-05-SUMMARY.md"
  - ".planning/phases/29-docs-v2-8-0-cut-readiness/29-06-PLAN.md"
  - ".planning/phases/29-docs-v2-8-0-cut-readiness/29-06-SUMMARY.md"
  - ".planning/phases/29-docs-v2-8-0-cut-readiness/29-07-PLAN.md"
  - ".planning/phases/29-docs-v2-8-0-cut-readiness/29-07-SUMMARY.md"
  - ".planning/phases/29-docs-v2-8-0-cut-readiness/29-PROPOSAL.md"
  - "API.md"
  - "INTEGRATION.md"
  - "docs/STYLE.md"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/component/CappedPillShape.kt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackCard.kt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackDeck.kt"
covered_digest: "v1:sha256:f47ada81e62d13bfb34d43d2e71a4f9f0352d5730a10f39b68df67acf89947d8"
behavior_unverified: 0
overrides_applied: 0
---

# Phase 29: Docs & v2.8.0 cut readiness Verification Report

**Phase Goal:** The v2.8 API is documented and every release gate is green, so Yahir gets a ready-to-cut `v2.8.0` proposal. The phase ends at the proposal; the agent cuts no tag and pushes nothing.
**Verified:** 2026-10-18
**Status:** passed
**Re-verification:** No, initial verification (no earlier VERIFICATION.md existed)

I treated SUMMARY claims as claims. The evidence below comes from git (ancestry, diffs, tag and remote state), the recorded gate logs under `.gate/`, the signed Gate-1 run, and the doc files themselves. I did not run Gradle and did not touch a device.

## Goal Achievement

### Observable Truths (ROADMAP success criteria)

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 1 | API.md, STYLE.md and, where needed, INTEGRATION.md and the catalog docs describe `FeedbackTarget`, `FeedbackStackState` / `rememberFeedbackStackState` / `FeedbackStackHost`, the result type, both `emitTrackedWithUndo` paths, and `onOpenTarget` on `OutcomeSheet` and `UndoCenterScreen`, including how one app-level `onOpenTarget` maps keys to routes. Doc counts match the registry. | VERIFIED | Symbol counts in the docs: `FeedbackTarget` API 11 / INTEGRATION 5; `FeedbackStackState` API 4 / STYLE 2 / INTEGRATION 2 / README 1; `rememberFeedbackStackState` API 3 / INTEGRATION 1; `FeedbackStackHost` API 4 / STYLE 2 / INTEGRATION 3; `emitTrackedWithUndo` API 3 / STYLE 1 / INTEGRATION 3; `onOpenTarget` API 10 / STYLE 1 / INTEGRATION 7. The result type is documented as `FeedbackResult` (API 4, STYLE 1). INTEGRATION.md has a dedicated section (lines ~316-395) with "One app-level `onOpenTarget`" (key to route mapping, `else -> Unit`, idempotency, never log the key) and the `FeedbackStackHost` / `UndoCenterScreen` / `OutcomeSheet` wiring snippets. API.md line 29-30: 67 registered + 6 unregistered = 73; README.md says the same. The counts are enforced by `CatalogDocsDriftGuardTest` (16 tests) and `ComponentRegistryDriftGuardTest` (1), both executed in the forced-fresh run with 0 failures. No debt markers (TBD/FIXME/XXX) in the docs or changed sources. |
| 2 | On a freshly regenerated `api.txt`, `apiCheck` shows additions only against `v2.7.1`; a `v2.7.1` call-site canary compiles unchanged; `testDebugUnitTest`, `run-all.sh` and `detekt` (zero baseline) pass on a forced fresh run. | VERIFIED | Recorded on GATED_HEAD `ce855d2` in `.gate/battery-notes.txt`, `.gate/gradle-gated.log`, `.gate/gated-junit-fresh.txt`, `.gate/gated-additive.log`, `.gate/gated-run-all.log`: `apiDump` alone rc 0 and no diff in `api.txt`; `testDebugUnitTest detekt apiCheck --rerun-tasks --no-build-cache --no-daemon` BUILD SUCCESSFUL in 3m 25s; in the log `:detekt`, `:metalavaCheckCompatibilityRelease` and `:testDebugUnitTest` carry no `UP-TO-DATE` or `FROM-CACHE` suffix (the only UP-TO-DATE lines are `pre*Build` / asset tasks); `JUNIT-FRESH OK` with FILES 182, STALE 0, tests 1943, skipped 22, failures 0, errors 0, all 13 required classes ran (V271CallSiteCanaryTest 12). `tools/verify-api-source-additive.sh v2.7.1` reports `SEAMS: none` and `API SOURCE-ADDITIVE OK ... removed_classes=0 removed_members=0 required_param_changes=0 signature_changes=0 accepted_exceptions=0`; run-all.sh has no FAILED line. My own checks: `git diff v2.7.1 ce855d2 -- config build.gradle.kts settings.gradle.kts gradle.properties gradle jitpack.yml` is empty; no `@HiltAndroidApp` / `@AndroidEntryPoint` in `src/main`. Note: `apiCheck` alone only fails on removals; the additions-only guarantee is carried by the additivity tool plus the canary, and the proposal states this openly (discrepancy 10). |
| 3 | Long-string checks pass on the snackbar card, the sheet target rows and the handled-by pill in German and Spanish at ~360 dp and font scale 2.0, and the Gate-1 device checks pass on the TESTER, with evidence kept for the owner. | VERIFIED | Run 1 (`uatr-20261018T112553Z-e47a3028`) honestly failed SC3 on es-snackbar at font 2.0; that failure is kept as history. Gap plan 29-07 (designer-approved) fixed it internally. Run 2 (`uatr-20261018T134126Z-99ff70b7`, Addendum in `29-05-SELF-UAT.md`): 28/28 base scene runs PASS plus 7 scrolled and 3 pressed captures; es-snackbar f2.0 Undo wraps with whole glyphs and a capped-corner ripple, the card scrolls its own content, de-snackbar keeps the capsule Undo. The run directory exists with 75 entries (74 captures + `run.json`). Robolectric backing in the fresh run: HandledByPillShapeTest 10, FeedbackStackHostLongLabelTest 7, OutcomeSheetOpenTargetLongLabelTest 8, UndoCenterScreenOpenTargetLongLabelTest 5, CappedPillShapeTest 5, FeedbackCardUndoShapeTest 11, FeedbackCardTallContentTest 13. Tree identity (my checks): `git diff 0bad7b6 ce855d2 -- src/main api.txt build.gradle.kts` is empty, so the shipped library tree equals the tree the device run tested; `git diff 55f5288 ce855d2 -- src/main` lists exactly the five plan 29-07 files; `api.txt` equals `c449db0`. The runtime decision allowed this one `src/main` fix, so the "no library code" scope line does not apply. |
| 4 | A `v2.8.0` proposal (commit sha, `git log origin/main..HEAD` range, gate results) is produced; the agent creates no tag, pushes nothing and repins no consumer. | VERIFIED | `29-PROPOSAL.md` carries the tag target sha, remote state, range, gate table, long-string results, scan result, 13 discrepancies, owner items, owner-run commands (quoted as text only) and six director decisions. `29-PROPOSAL-RANGE.txt` has 314 lines; my `git log --oneline origin/main..ce855d2 \| wc -l` is 314, first line `ce855d2`, last `426fcbf`. `origin/main` is still `c2182ecf...` and equals `.gate/REMOTE_MAIN_BEFORE`; `git ls-remote origin refs/heads/main` returns the same; `git merge-base --is-ancestor origin/main ce855d2` holds (fast-forward). `git tag --list 'v2.8*'` is empty locally, `git ls-remote origin 'refs/tags/v2.8*'` is empty, `.planning/config.json` has `create_tag: false`. Descendant proof: `git diff --name-only ce855d2 HEAD` shows no path outside `.planning/`. Debris check over `55f5288..HEAD`: no state.json, config.json, graphs, WAITING, milestone.lock, `.gate`, stage markers, graphify-out, `.oc-audit` or `docs/superpowers` was committed. The relay of the proposal to director@office (cc teamlead) is the milestone master's step by design (29-06 Hand-over); I cannot observe it and the phase must not do it. |

**Score:** 4/4 truths verified (0 behavior-unverified)

### Requirements Coverage

| Requirement | Source Plans | Description | Status | Evidence |
|-------------|--------------|-------------|--------|----------|
| SHIP-05 | 29-01, 29-02, 29-03, 29-04, 29-05, 29-06, 29-07 (all list `requirements: [SHIP-05]`) | Docs describe the new API; additions-only; tests and detekt green; long-string and Gate-1 device checks pass; v2.8.0 tag and push proposed, not cut | SATISFIED | Truths 1-4 above. REQUIREMENTS.md line 52 text and traceability row 91 (still "Pending", to be flipped by `phase.complete`). No orphaned requirement IDs: SHIP-05 is the only ID mapped to Phase 29. |

### Required Artifacts

| Artifact | Expected | Status | Details |
|----------|----------|--------|---------|
| `API.md`, `INTEGRATION.md`, `docs/STYLE.md` | v2.8 API docs | VERIFIED | Substantive, symbols present, guarded by drift tests |
| `29-PROPOSAL.md`, `29-PROPOSAL-RANGE.txt` | Cut proposal and full unpushed range | VERIFIED | Contents cross-checked against git (range count, remote sha, tag state) |
| `CappedPillShape.kt`, `FeedbackCard.kt`, `FeedbackDeck.kt`, `FeedbackDeckMetrics.kt`, `DynamicActionButton.kt` | Gap-closure fix for SC3 | VERIFIED | Present, internal, wired from `FeedbackCard` / `FeedbackDeck`, covered by 29 tests, device-verified |
| `tools/verify-junit-fresh.sh`, `tools/verify-publish-range.sh`, `tools/verify-tester-restored.sh` and fixtures | Fresh-run and publish-scan tooling | VERIFIED | Used by the recorded battery; review fix pass hardened them |
| `29-05-SELF-UAT.md` plus two signed evidence run directories | Gate-1 record | VERIFIED | First-run FAIL kept, Addendum PASS on a new signed run |

### Key Link Verification

| From | To | Via | Status | Details |
|------|----|-----|--------|---------|
| Docs counts | `ComponentRegistry` | `CatalogDocsDriftGuardTest`, `ComponentRegistryDriftGuardTest` | WIRED | Both executed in the fresh run, 0 failures |
| Release flip `ce855d2` | gate battery | HEAD == GATED_HEAD before and after | WIRED | `battery-notes.txt`, plus my descendant diff |
| Device-tested tree | tag target tree | `src/main`, `api.txt`, `build.gradle.kts` identical to `0bad7b6` | WIRED | Empty git diff, checked by me |
| Proposal | scan evidence | `.gate/scan-gated.txt`, `.gate/scan-final.txt` | WIRED | SUMMARY line `net_credential=0 net_shipped=0 ... history_shipped=1 outside=0 binary=188 fastforward=yes`; RESULT OK, matches the proposal |

### Behavioral Spot-Checks

| Behavior | Command | Result | Status |
|----------|---------|--------|--------|
| No v2.8 tag, local or remote | `git tag --list 'v2.8*'`; `git ls-remote origin 'refs/tags/v2.8*'` | both empty | PASS |
| Remote main unchanged | `git ls-remote origin refs/heads/main` vs `.gate/REMOTE_MAIN_BEFORE` | `c2182ecf...` both | PASS |
| Range size | `git log --oneline origin/main..ce855d2 \| wc -l` | 314 | PASS |
| Tested tree equals tag-target tree | `git diff 0bad7b6 ce855d2 -- src/main api.txt build.gradle.kts` | empty | PASS |
| Post-GATED_HEAD commits are docs-only | `git diff --name-only ce855d2 HEAD \| grep -v '^\.planning/'` | empty | PASS |
| Gradle/Robolectric re-run | not run (read-only brief); recorded logs spot-checked instead | totals match across summary, junit file, notes | PASS (recorded) |

### Probe Execution

SKIPPED (no probe scripts declared for this phase; the equivalent checks are the recorded shell fixtures in `run-all.sh`, which passed).

### Plan 29-06 threat confirmation (T-29-27 to T-29-33, T-29-SC)

The security audit left these pending. I confirm each against the proposal and the scan evidence:

| Threat | Result | Evidence |
|--------|--------|----------|
| T-29-27 publishing tailnet addresses, home paths or keys | CLOSED | Scan run over `origin/main..ce855d2` and again at HEAD: four positive controls ok, `net_credential=0`, `history_credential=0`, `net_shipped=0`, `outside=0`. Hits are `.planning/` prose only, each listed and classified in the proposal. One history residue in a shipped path (`32bd7d5`, a test fixture address, already neutralized in the tree) is disclosed as AR-29-02 and put to the director (Decision 3). 188 binaries were not text-scanned; also disclosed. |
| T-29-28 agent tag, push or repin | CLOSED | No v2.8 tag anywhere, remote main unchanged, `create_tag: false`, owner commands only quoted as text. |
| T-29-29 wrong tag target or non-.planning change after GATED_HEAD | CLOSED | My descendant diff is empty of non-`.planning` paths; proposal names GATED_HEAD `ce855d2`. |
| T-29-30 stale Gradle result as proof | CLOSED | `--rerun-tasks --no-build-cache`, no UP-TO-DATE/FROM-CACHE on the three gate tasks in the log, JUNIT-FRESH OK, STALE 0. |
| T-29-31 supply-chain drift | CLOSED | Build/config diff against `v2.7.1` empty (checked by me); the tag is left to the owner, immutable. |
| T-29-32 unreviewed commits published by the tag push | CLOSED | Full 314-commit range listed and count verified; single author; fast-forward verified. |
| T-29-33 debris swept into a commit | CLOSED | My path scan over `55f5288..HEAD` found none of the debris classes; debris list recorded in the proposal. |
| T-29-SC package installs | CLOSED (accepted) | No dependency file changed (empty diff vs `v2.7.1`). |

`29-SECURITY.md` still carries one row marked "pending" for these ids. The evidence above supports flipping that row to closed; `threats_open` stays 0. This is a bookkeeping edit for the orchestrator, not a gap.

### WR-01 ruling (FeedbackStackHost under an IntrinsicSize parent)

Judgment: **an accepted, disclosed limitation, not a goal gap.**

- `FeedbackStackHost` and the deck do not exist in `v2.7.1` (`FeedbackDeck.kt` is absent from that tag), and the deck already used `BoxWithConstraints` at `55f5288`, before plan 29-07. So no released call shape regresses and no `v2.7.1` call site is affected; the additivity guarantee of the phase goal is intact.
- None of the four success criteria promises intrinsic-size hosting. SC3 is long-string behavior, which passed on the device.
- The review finding was reproduced (failing test and stack saved in `.gate/`), so it is real, not hypothetical.
- Fixing it needs a `src/main` edit, which would invalidate the Gate-1 device evidence for the exact tree. Deferring is a legitimate trade, and the proposal surfaces it as director Decision 2 and as discrepancy 12.
- Residual warning: the limitation is disclosed only in the proposal and the review reports. API.md, INTEGRATION.md and STYLE.md contain no "do not host under an intrinsic-size parent" line (grep for "Intrinsic" in those three files is empty). The director's Decision 2 ("accept and document, or fix in v2.8.1") covers it. A docs-only line would not affect device evidence but would move the tag target past `ce855d2`, so I leave that to the owner.

The same reasoning applies to the deferred IN-01, IN-02 and IN-05: cosmetic or test-shape items, disclosed, not blockers.

### Anti-Patterns Found

None blocking. No TBD/FIXME/XXX in the changed docs, `src/main` files or tools (checked against `v2.7.1..ce855d2`); no TODO/HACK/PLACEHOLDER in the phase's `src/main` and tools changes. The 29-VALIDATION.md frontmatter still reads `status: draft`, `nyquist_compliant: false`; this is a bookkeeping state for the validate step, not a goal item.

### Human Verification Required

None for this phase gate. Remaining human work is milestone-level and already registered:

- The relay of `29-PROPOSAL.md` to director@office (cc teamlead) and the push and tag decision belong to the milestone master and the owner. The agent must not do them.
- Gate-2 owner items (physical TalkBack steps, German font-2.0 observations, removing the four throwaway harness apps from the tester, the `stay_on_while_plugged_in` note) are recorded in `.planning/uat-pending/25-...` through `29-docs-v2-8-0-cut-readiness.md` and in the proposal's "Owner items" section.

### Gaps Summary

No gaps. All four success criteria hold in the codebase and in the recorded gate evidence, SHIP-05 is accounted for in every plan and satisfied, and the phase stopped at the proposal with no tag, no push and no repin. Notes for the orchestrator: (1) flip the plan 29-06 row in `29-SECURITY.md` from pending to closed, (2) let `phase.complete` mark SHIP-05, (3) carry the WR-01 "accept and document or fix in v2.8.1" choice to the director with the proposal.

---

_Verified: 2026-10-18_
_Verifier: Claude (gsd-verifier)_
