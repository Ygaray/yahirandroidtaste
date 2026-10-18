---
phase: 29
proposal: v2.8.0
status: proposed - nothing cut, nothing pushed
tag_target: ce855d2e6b59faf610f015472803e0e9b1dae4f4
route: director@office
cc: teamlead@vae-bilingual
---

# v2.8.0 cut proposal (yahirandroidtaste)

## Proposal

This document proposes the v2.8.0 cut of the `yahirandroidtaste` hub; it does not perform it. The owner cuts: the tag, the push and the consumer repins are human-gated and none of them was done, scripted or sent by the agent that wrote this (no tag, no push, no repin, no rig message). The proposed tag target is `ce855d2` (the release-flip commit, called GATED_HEAD below), because the forced-fresh gate battery ran on exactly that commit. v2.8.0 is **not a docs-only cut**: Phase 29 contains one library change besides docs, tests and tooling. After Gate-1 SC3 failed on the Spanish stacked card at font scale 2.0 (the Undo label was clipped and the message lines were unreachable), the designer ruled on 2026-10-18 to approve ONE scoped fix, plan 29-07: an internal capped-radius Undo shape (`CappedPillShape`) so a wrapped Undo label no longer sits in a stadium, and a stacked card that scrolls its own content when it is taller than the space the host gives it. It touches five internal `src/main` files, leaves `api.txt` byte-identical to its pinned base, and the additivity check against `v2.7.1` passes. The Gate-1 SC3 re-verification on the tester passed on that tree. The rest of the v2.8.0 library content (FeedbackTarget, the stacked deck, `onOpenTarget`, the TalkBack sweep, the OSM tile fix) was built in Phases 25 to 28 and is documented and gated by Phase 29.

## Identity

- **GATED_HEAD (proposed tag target):** `ce855d2e6b59faf610f015472803e0e9b1dae4f4`, subject `docs(29-06): current release v2.8.0 (D-02)`. It edits exactly `README.md` and `CLAUDE.md` (the release line and the descriptor) and nothing else; `build.gradle.kts` (publishing version constant, JitPack overrides it from the tag) and the Explorer user-agent literal are untouched.
- **Phase 29's only `src/main` change** (`git log 55f5288..GATED_HEAD --oneline -- src/main`):
  - `46da199` fix(29-07): the stacked card's Undo keeps a capped-radius shape once its label wraps
  - `e5392d0` fix(29-07): a tall stacked card scrolls its own content inside the host
  - files, all under `src/main/java/io/github/ygaray/yahirandroidtaste`: `component/CappedPillShape.kt`, `component/DynamicActionButton.kt`, `feedback/FeedbackCard.kt`, `feedback/FeedbackDeck.kt`, `feedback/FeedbackDeckMetrics.kt`.
  - `api.txt` is identical to the pinned base `c449db0`; `src/main`, `api.txt` and `build.gradle.kts` are identical to `0bad7b6` (the tree the Gate-1 SC3 re-verification tested on the device).
- **After the Gate-1 device run, only tests, tooling and docs changed** (code review fix pass, commits `3ad051f`, `d08d8a1`, `06b1410`, `6a45421`, `388cbad`, `a29a6cd`): `tools/verify-junit-fresh.sh`, `tools/verify-publish-range.sh` and their fixtures, one test file (`FeedbackCardUndoShapeTest`), `API.md`. None of them is in the library AAR; the gate battery below ran after all of them.
- **Descendant proof:** every commit after GATED_HEAD touches only `.planning/` paths (PD-2). The check is `git diff --name-only ce855d2e6b59faf610f015472803e0e9b1dae4f4 HEAD`, which must print only `.planning/` paths; it is re-run and recorded in `29-06-SUMMARY.md`. Pushing `main` instead of only the tag also publishes every later `.planning` commit (this proposal, the summary, the STATE and ROADMAP updates), and the tag itself must be cut at GATED_HEAD, not at HEAD.

## Remote state

Read-only network use only (`git ls-remote`, `git fetch origin main`). At proposal time `origin/main` is `c2182ecf92d3a4176466697a907dca689c6d97f2` (`c2182ec`), identical to the value recorded before the battery (the remote did not move) and to `git rev-parse origin/main` after a fetch. GATED_HEAD is a fast-forward over it (scan line `FASTFORWARD yes`). No `v2.8*` tag exists locally or on the remote (`git ls-remote origin 'refs/tags/v2.8*'` prints nothing; `.planning/config.json` has `create_tag: false`).

## Range

- **What a push would publish:** `git log --oneline origin/main..ce855d2` is **314 commits**, written in full to `29-PROPOSAL-RANGE.txt` (314 lines). Newest `ce855d2` (the release flip, 2026-10-18); oldest `426fcbf` (`docs(style): STYLE.md + STYLE.REVERTED.md ...`, 2026-10-17). Distinct authors: 1 (Yahir). The path allowlist found 0 paths outside the allowed set (`outside=0`).
- **Size:** `git diff --shortstat origin/main ce855d2` is 1072 files, +61942 / -1307; 959 of those files are under `.planning/`, 93 under `src`, 9 under `tools`, 4 under `docs`, plus `API.md`, `INTEGRATION.md`, `README.md`, `CLAUDE.md`, `ECOSYSTEM.md`, `AGENT-DEVICE-TESTING.md` and `api.txt`. `src/main` alone is 41 files (+3381 / -125), all of it v2.8.0 library content.
- **A tag push publishes every commit reachable from it.** Pushing the tag `v2.8.0` at GATED_HEAD publishes all 314 commits above, not only the ones Phase 29 made. Pushing `main` also publishes the later `.planning` commits.
- **Stale claim in STATE.md:** its blocker line "4 docs commits (`426fcbf`..`1ecab5f`) are already unpushed" is out of date; the real range is 314 commits (313 before the release flip) and was measured, not copied.

## Gate results

All of these ran on GATED_HEAD itself, on the main checkout, one Gradle invocation at a time with `--no-daemon`, with the forced-fresh flags. The v2.6 gate-record format is folded in here (no separate gate-record file, PD-3).

| Check | Result |
|-------|--------|
| HEAD equals GATED_HEAD before and after the battery; artifact paths clean | yes, both times |
| `./gradlew apiDump` on its own, then `git diff --exit-code -- api.txt` | rc 0, no diff (regenerated equals committed) |
| `./gradlew testDebugUnitTest detekt apiCheck --rerun-tasks --no-build-cache --no-daemon` | rc 0, BUILD SUCCESSFUL in 3m 25s, 43 of 43 tasks executed; detekt at zero baseline; apiCheck passes |
| Reuse check on the three gate tasks | no `UP-TO-DATE` or `FROM-CACHE` on `:testDebugUnitTest`, `:detekt`, `:metalavaCheckCompatibilityRelease` (grep rc 1) |
| `tools/verify-junit-fresh.sh` (marker, 13 required classes) | `FILES 182`, `STALE 0`, `TOTALS tests=1943 skipped=22 failures=0 errors=0`, `JUNIT-FRESH OK` |
| Required class counts | CatalogDocsDriftGuardTest 16, ComponentRegistryDriftGuardTest 1, V271CallSiteCanaryTest 12, LibraryInvariantsGuardTest 5, DomainVocabularyDriftGuardTest 2, GeneratedSymbolDriftGuardTest 2, HandledByPillShapeTest 10, FeedbackStackHostLongLabelTest 7, OutcomeSheetOpenTargetLongLabelTest 8, UndoCenterScreenOpenTargetLongLabelTest 5, CappedPillShapeTest 5, FeedbackCardUndoShapeTest 11, FeedbackCardTallContentTest 13 |
| Registry counts (enforced by the two drift guards) | 67 registered, 6 allowlisted, 73 in all |
| `bash tools/test/run-all.sh` | rc 0, no FAILED line; suites PASS=12, 6, 5, 52, 40, 15, 92, 82, all FAIL=0 |
| `bash tools/verify-api-source-additive.sh v2.7.1` | first line `SEAMS: none`; last line `API SOURCE-ADDITIVE OK baseline=v2.7.1 removed_classes=0 removed_members=0 enum_constants_intact=73 required_param_changes=0 added_classes=5 added_members=48 signature_changes=0 accepted_exceptions=0` |
| `git diff --numstat v2.7.1 -- api.txt` | `78 11` (78 added and 11 removed lines in `api.txt`, unchanged between the first and the second Gate-1 battery; the additivity tool reports 0 removed members) |
| Call-site canary | V271CallSiteCanaryTest 12 tests, 0 failures |
| `tools/verify-binary-abi.sh v2.7.1` (INFORMATIONAL, never a gate) | exit 3 with 37 lane-3 lines (for example the `VoiceOutcomeUiState.Success` copy and constructor signatures); expected after deliberate signature changes; the hub is source-compatible, not binary-compatible (consumers recompile) |
| INV-03 | no `DeprecationLevel.HIDDEN` shim added or removed since `v2.7.1`; no `@HiltAndroidApp` or `@AndroidEntryPoint` in `src/main`; no consumer-package import in `src` |
| Configuration diff vs `v2.7.1` (`config`, `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, `gradle`, `jitpack.yml`) | empty |
| Library-change confinement | `git diff --name-only 55f5288 HEAD -- src/main` lists exactly the five plan 29-07 files; `api.txt` equals `c449db0`; `src/main`, `api.txt`, `build.gradle.kts` equal `0bad7b6` |
| Gate-1 run 1 (pre-fix tree) | `uatr-20261018T112553Z-e47a3028`, verified; verdict HAS_FAIL: SC3 failed on es-snackbar at font 2.0 (kept untouched as history) |
| Gate-1 run 2 (after plan 29-07, the Addendum in `29-05-SELF-UAT.md`) | `uatr-20261018T134126Z-99ff70b7`, `uat-run verify` verified (one warning: the logcat capture is empty, signed negative evidence); verdict PASS on the tester; HEAD used `f08c866`, whose `src` equals `0bad7b6` |
| Build identity of the device run | AAR rebuilt and republished at the tested `src` (md5 `c8d83de76279bf687de54733a2149430`; the first run's was `684df61c57910824077f7b926f5f0000`); harness APK rebuilt and the device copy matched |
| Tag and push state | no tag; remote `main` unchanged |

## Long-string results

- **Robolectric, fresh on GATED_HEAD:** HandledByPillShapeTest 10 (including the Spanish wrapped pill, with a failing CircleShape control), FeedbackStackHostLongLabelTest 7, OutcomeSheetOpenTargetLongLabelTest 8, UndoCenterScreenOpenTargetLongLabelTest 5, and the three classes plan 29-07 added: CappedPillShapeTest 5, FeedbackCardUndoShapeTest 11, FeedbackCardTallContentTest 13.
- **Device, Gate-1 run 1 (pre-fix):** 28 base scene runs (14 scenes at font scale 1.0 and 2.0, about 360 dp, German and Spanish): 27 passed; **es-snackbar at font 2.0 failed** (Undo glyphs cut at the rounded ends, message lines unreachable).
- **Device, Gate-1 run 2 (Addendum, supersedes the es-snackbar failure):** 28 of 28 base scene runs PASS, plus 7 scrolled captures and 3 pressed-state captures. es-snackbar at font 2.0: the Undo label wraps over four lines with every glyph whole and a capped-corner ripple; the card is a scroll container, one vertical drag on its text column brings the last line into view while the card does not move; de-snackbar keeps a one-line capsule Undo at both scales. 74 signed captures, evidence in `29-05-uat-evidence/uatr-20261018T134126Z-99ff70b7/`. Narrow-column mid-word breaks (for example German at font 2.0) are observations, not clipping.

## Scan result

Command: `bash tools/verify-publish-range.sh --base origin/main --head ce855d2e6b59faf610f015472803e0e9b1dae4f4 --out <scratch>/scan-gated`, exit 0. It scans net added lines and per-commit history for four classes (tailnet addresses, the `ts.net` host suffix, the host home path, credentials); it never prints a matched value, only class, path and count.

- **Controls:** four positive controls (tailnet_ip, ts_net, home_path, credential) all `ok`.
- **Summary line:** `net_credential=0 net_shipped=0 net_planning=48 history_credential=0 history_shipped=1 history_planning=29 outside=0 binary=188 fastforward=yes`. Verdict: `RESULT OK (history_shipped=1 binary=188 not enforced)` with two `CAVEAT` lines.
- **Per class, net added lines (all in `.planning/`, none in shipped paths):**
  - tailnet addresses: 9 added lines in 7 files; in 8 history commits.
  - `ts.net` host suffix: 5 added lines in 4 files; in 4 history commits.
  - home path: 34 added lines in 17 files; in 18 history commits.
  - credentials: 0 net, 0 history.
- **Classification of the `.planning/` hits: reviewed and accepted, not scrubbed.** `origin/main` already publishes the same classes in about 52 `.planning` files. Per file:
  - tailnet address (the tester's wireless-transport address, named in prose to say it is never driven): `26-receipt-card-stacked-snackbar-deck/26-05-SELF-UAT.md`, `27-open-from-the-sheet-undo-center-pill-fixes/27-04-PLAN.md`, `27-04-SELF-UAT.md`, `27-RESEARCH.md`, `27-SECURITY.md`, `28-talkback-sweep-explorer-demos/28-07-PLAN.md`, `29-docs-v2-8-0-cut-readiness/29-RESEARCH.md`. Prose and research notes; no credential, no actionable access.
  - `ts.net` suffix: `29-03-SUMMARY.md`, `29-04-PLAN.md`, `29-06-PLAN.md`, `29-RESEARCH.md` (phase 29 planning text that describes the scan and the hosting rule).
  - home path: phase 26 `26-02/03/04/05-SUMMARY.md`, `26-RESEARCH.md`, `26-REVIEW-FIX.md`; phase 27 `27-01-PLAN.md`, `27-RESEARCH.md`, `27-04-uat-evidence/uatr-20261017T194228Z-30718f43/01-00-build-identity-note.log`; phase 28 `28-RESEARCH.md`, `28-VERIFICATION.md` and four build-identity logs under `28-07-uat-evidence/` (`uatr-20261018T023153Z-fe922063` twice, `uatr-20261018T044022Z-2fbbbce3`, `uatr-20261018T055800Z-2b1265c4`); phase 29 `29-CONTEXT.md`, `29-RESEARCH.md`. Local build paths in logs and notes, an account name at most; no secret.
  - **Action items: none.** No real key, no credential, no address that grants access.
- **History hits (history is never rewritten here):** 29 history hit lines point at `.planning` files with these classes (same files as above) and **one shipped-path commit**: `32bd7d5` (`chore(28-07): read-only TESTER restore oracle with a fake-adb fixture test`, unpushed) added a tailnet address in a test fixture under `tools/test/`. Plan 29-04 replaced that fixture value, so the tree and the net diff no longer carry it (`net_shipped=0`), but the commit stays in the range and a push publishes it. Rewriting history is not proposed; the director decides whether it matters (see Decisions).
- **Binary files: 188, not text-scanned.** 186 are `.png` files (185 Gate-1 evidence screenshots of phases 25 to 29 under `.planning/phases/`, harness-only screens, no personal app content per the Phase 29 security audit, and the test fixture `src/test/resources/osm-blocked-placeholder.png`), and 2 are `.db` pulled caches of the throwaway harness apps (a string check found no tailnet address, home path or host-suffix string in either). They were reviewed by metadata and name only; the pixels are unscanned.

## Discrepancies between briefs and the shipped code

Consolidated for the milestone master; in every case the docs follow the code (PD-1).

1. **Success row title vs brief D-15** (29-02). The brief said the row title is `summary`; the code uses `targetLabel`, falling back to `summary` when null or blank, with `targetDetail` as an optional second line.
2. **Log.w wording of POL-03** (29-02). The Runtime Decision said a blank or bare-package agent falls back "with one Log.w". The code warns when the sanitized agent is blank or starts with the placeholder package prefix, once per `PlaceMapPicker` instance (each configure call), and a bare package that is not a template id expands silently.
3. **Purge cadence** (29-02). The Runtime Decision said placeholder tiles are purged once per install; the code schedules the scan on the first call of every process and on later calls only while the install flag is unset; it matches on size and md5 and never wipes the cache.
4. **Manifest** (29-02). The library manifest declares no `INTERNET`; the docs say the host declares it.
5. **Refusal focus sentence vs the live-region route** (29-03). The Runtime Decision said the refused-undo message takes accessibility focus once when its sheet settles; that is the superseded 28-11 route. The code has one Assertive live region whose node appears after the sheet settles, no focus request and no announcement call (device result: 27 of 27 showings spoken once). STYLE.md documents the live-region route.
6. **"Named from `item.message`" vs message plus second line** (29-03). The collapsed deck card is named from the message followed by the second line when there is one.
7. **Options-page wording for the rejected Open label, and "read twice"** (29-03). The rejected-label entry follows the options page (no action label for all directions) and 28-12 / 28-VERIFICATION (a focus move on a live-region node was read a second time) exactly, without adding claims.
8. **`UndoHistoryStore.append` signature** (29-01). The plan wrote a bare `groupId`; the source has `groupId: String? = null` and a `target` without default. Also documented beyond the plan: timers scale with the accessibility recommended timeout, `maxVisible` is constant per call site, an `Indefinite` card without a host never returns until cancel or `dismissAll()`, and a cancelled caller after an Undo tap still completes the undo. Eviction wording: a `WithUndo` entry of an evicted card stays in the Undo Center on the tracked path only.
9. **Stale "4 docs commits"** (STATE.md). The real range is 314 commits.
10. **"apiCheck shows additions only"** (ROADMAP criterion 2). `apiCheck` alone fails only on removals; additions-only is carried by `tools/verify-api-source-additive.sh v2.7.1` plus the call-site canary, both green.
11. **Gate-1 diagnosis vs code** (29-07). The Gate-1 root-cause block said the card was "as tall as the window and still too short" and that the collapsed card does not scroll. In the code the deck wraps its layout in a vertical scroll, so every card was handed an infinite max height and was never limited; the deck scrolled as one block. The effect on screen was the same; the fix location (a finite max height for the card first) differed.
12. **Known limitation, not fixed (code review WR-01).** `FeedbackStackHost` uses `BoxWithConstraints` (the deck since Phase 26, plus the card bound plan 29-07 added), so hosting it under an `IntrinsicSize` parent throws `IllegalStateException: Asking for intrinsic measurements of SubcomposeLayout layouts is not supported`. A four-case Robolectric test reproduced it on the pinned Compose BOM, including with no card on screen. It was NOT changed because any `src/main` edit would invalidate the Gate-1 device evidence of the exact tree. The failing test and its evidence are kept untracked in the phase's `.gate` directory. The component is meant for a screen overlay and the harness apps do not use it this way.
13. **Deferred review items** (from `29-REVIEW-FIX.md`, all `src/main` or test-shape cleanups that would invalidate the device evidence): IN-01 (`shape` accepted by the private `EmphasisActionButton` but only honoured for the Text emphasis), IN-02 (the single-line capsule test is largely self-referential; a measured assertion needs a tag on the inner button), IN-05 (Undo is part of the scrolled row, so on a tall scrolling card it is off screen until the user scrolls back; touch-only, TalkBack has the custom action).

## Owner items (Gate-2, recorded, not fixed here)

The registered Gate-2 pointers are `.planning/uat-pending/25-feedback-target-model-stack-state.md` through `28-talkback-sweep-explorer-demos.md` and `29-docs-v2-8-0-cut-readiness.md`.

1. **Physical TalkBack steps parked by Phases 25 to 28:** the spoken cross-card order of the stacked deck, the Dismiss duplicate in TalkBack's actions menu (system plus custom, ruled keep), and the refusal read-out by swipe with TalkBack alone.
2. **German stacked card at font scale 2.0:** the narrow body column (about 101 dp at 360 dp wide, mid-word breaks such as "Wochen/ende" and "Einträge/n"; nothing clipped; unchanged by plan 29-07).
3. **German batch Receipt sheet at font scale 2.0:** the actions sit below the fold in the four-row fixture (one swipe brings them fully above the navigation bar) and partly under the bar before scrolling in the two-row demo; the German "Bearbeiten" pill wraps mid-word.
4. **Throwaway harness apps left on the tester, which only Yahir removes:** `com.example.yat28head`, `com.example.yat28control`, `com.example.yat28obs` and the one this phase added, `com.example.yat29ls`.
5. **Tester state changes for awareness:** global `stay_on_while_plugged_in` reads 15 (set by the playbook in the first Phase 29 run whose prior value was not recorded); density, font scale and accessibility settings were restored and verified.
6. Smaller notes from the logs: the M3 drag-handle tooltip overlaps the sheet heading on full-height sheets at font 2.0, and the Undo Center chrome stays English in the German and Spanish demos.

## Owner-run commands

Quoted as text only. No agent runs them. They assume the owner has confirmed the decisions below. Run from the repository root.

```
git tag -a v2.8.0 ce855d2e6b59faf610f015472803e0e9b1dae4f4 -m "v2.8.0"
git push origin main
git push origin v2.8.0
```

After the tag push, check the JitPack build status at `https://jitpack.io/api/builds/com.github.Ygaray/yahirandroidtaste/v2.8.0` (and that the consumer-facing coordinate `com.github.Ygaray:yahirandroidtaste:v2.8.0` resolves). Tags are immutable; never point a consumer at `main-SNAPSHOT`. Repinning SecondBrain and CalTracker to the new tag is a separate human-gated step in each consumer.

## Decisions for the director

1. **Tag target:** GATED_HEAD `ce855d2` (proposed, so the tagged tree equals the gated tree) versus the later artifact commit (HEAD, which only adds `.planning` files).
2. **Library limitation WR-01 (a recommended decision):** accept and document it ("do not host `FeedbackStackHost` under an intrinsic-size parent"), or fix the deck and the card in a v2.8.1 with a Gate-1 re-run. Same choice for the deferred IN-01, IN-02 and IN-05.
3. **Tailnet-address fixture in history:** commit `32bd7d5` carries a tailnet address in a test fixture and is in the range; the tree no longer does. Accept the history residue (the same classes already ship in about 52 `.planning` files on `origin/main`), or ask for a history rewrite (not proposed, destructive).
4. **Internal test tags:** the `feedback_stack_*`, `open_target_*` and `undo-open-chevron-*` test tags are internal and left unpublished; confirm they stay out of the public API docs.
5. **CLAUDE.md descriptor wording:** the v2.8 descriptor in the coordinate bullet ("go-to-item snackbars: FeedbackTarget, a stacked FeedbackStackHost deck, onOpenTarget on OutcomeSheet and UndoCenterScreen, a TalkBack sweep, the OSM tile user-agent fix and long-label hardening of the stacked card") is the planner's wording; reword before the cut if wanted (a reword is a docs commit that would need the tag target to move).
6. **Push scope:** tag only versus `main` plus tag (the latter also publishes the `.planning` commits after GATED_HEAD).

## Debris not committed

Deliberately excluded from every Phase 29 commit; all are working-tree leftovers that `git status` shows as modified or untracked:

- `graphify-out/`, `.oc-audit/` (and the `.oc-audit/` line in `.gitignore`, a modified tracked file), `docs/superpowers/`.
- `.planning/graphs/*` (graph snapshots and report), `.planning/state.json`, `.planning/config.json` (the run flag), `.planning/WAITING.json`, `.planning/milestone.lock`, `.planning/metrics/`, `.planning/research/.cache/`, `.planning/v2.8-MILESTONE-RUN.md`.
- `.gsd-stage-*` markers in phase directories, the `.gate` directories (Phase 29's holds the battery logs, `GATED_HEAD`, the scan outputs and the WR-01 evidence), and abandoned `uat-evidence` run directories of Phases 22 and 24.
