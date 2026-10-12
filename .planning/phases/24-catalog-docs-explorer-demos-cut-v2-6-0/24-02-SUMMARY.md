---
phase: 24-catalog-docs-explorer-demos-cut-v2-6-0
plan: 02
subsystem: testing
tags: [api-guard, canary, docs, api-md, integration-md, drift-guard, metalava, source-compat, test-tags]
requires:
  - phase: 24-catalog-docs-explorer-demos-cut-v2-6-0
    provides: plan 24-01 demos and the registry render tests (ReleaseDemos.kt is scanned by the invariants guard)
  - phase: 22-outcomesheet-receipt-style
    provides: the Y-14 tags and the Receipt types documented here
  - phase: 23-providerkeycard-lifecycle-undo-center-groups
    provides: the key lifecycle and Undo Center groups documented here
provides:
  - V251CallSiteCanaryTest (verbatim v2.5.1 SB, CT and Explorer call shapes for the nine D-01 surfaces; proven on a pristine v2.5.1 archive)
  - tools/verify-api-source-additive.sh (+ .py) with 22 permanent fixture cases run by tools/test/run-all.sh
  - LibraryInvariantsGuardTest (INV-03) and CatalogDocsDriftGuardTest (CAT-03)
  - API.md section 11 and the top-level Stable test tags (documented API) section; INTEGRATION.md 5b; README and ComponentRegistry KDoc counts (66 + 5 = 71)
affects: [24-03-tester-screencaps, 24-04-cut]
requirements-completed: [DOC-03, API-03, CAT-03, INV-03]
actuals:
  tokens: 29500
  tasks: 3
  commits: 5
duration: 18min
completed: 2026-10-12
status: complete
plan_head_before: cb335b5bb8d980ecc2d4f5a201dde7de00c33505
commits: 5
tech-stack:
  added: []
  patterns:
    - "Source-additivity is checked on parsed signatures (same ordered REQUIRED parameter types), not on a text diff"
    - "Doc counts, new symbols and tags are enforced by a plain-JVM test that reads API.md, README.md, api.txt and the src/main tag literals"
    - "A compile canary is only trusted once the same file also builds on a pristine archive of the baseline tag"
key-files:
  created:
    - src/test/java/io/github/ygaray/yahirandroidtaste/V251CallSiteCanaryTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/LibraryInvariantsGuardTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/CatalogDocsDriftGuardTest.kt
    - tools/verify-api-source-additive.sh
    - tools/verify-api-source-additive.py
    - tools/test/test-verify-api-source-additive.sh
  modified:
    - API.md
    - INTEGRATION.md
    - README.md
    - tools/README-api-guard.md
    - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistry.kt
key-decisions:
  - "The canary asserts only properties that exist on v2.5.1, so the same file builds on the v2.5.1 tag; v2.6.0-only defaults stay pinned by OutcomeSheetSourceCompatV251Test"
  - "R4 compares ordered REQUIRED parameter types, so NeedsConfirmation's inserted defaulted fields pass while an added required parameter fails"
  - "Tags whose KDoc does not say test-only are stable, including outcome_sheet_handled_by_dot, outcome_sheet_action_bar_divider and provider_key_saved_error"
  - "The invariants import allowlist includes dagger. (the charter names Hilt); the library currently imports only javax.inject"
coverage:
  - id: C1
    description: "V251CallSiteCanaryTest holds consumer-verbatim and Explorer-verbatim v2.5.1 shapes for the nine surfaces; 12 tests pass on HEAD and on a pristine git archive of v2.5.1"
    requirement: API-03
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/V251CallSiteCanaryTest.kt (12 tests, HEAD)"
        status: pass
      - kind: other
        ref: "./gradlew testDebugUnitTest --tests V251CallSiteCanaryTest --offline in a git archive of v2.5.1: BUILD SUCCESSFUL, 12 tests, 0 failures"
        status: pass
    human_judgment: false
  - id: C2
    description: "tools/verify-api-source-additive.sh v2.5.1 exits 0 with SEAMS: none; the four rules each have a failing fixture"
    requirement: API-03
    verification:
      - kind: other
        ref: "bash tools/verify-api-source-additive.sh v2.5.1 (exit 0, OK line recorded below)"
        status: pass
      - kind: other
        ref: "bash tools/test/test-verify-api-source-additive.sh (22 cases) and bash tools/test/run-all.sh"
        status: pass
    human_judgment: false
  - id: C3
    description: "LibraryInvariantsGuardTest enforces the import allowlist over 146 files, the one-activity manifest, no consumer identifier in code and a neutral ReleaseDemos.kt"
    requirement: INV-03
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/explorer/LibraryInvariantsGuardTest.kt (4 tests; mutation-checked once)"
        status: pass
    human_judgment: false
  - id: C4
    description: "CatalogDocsDriftGuardTest keeps the registration, counts, per-family counts, 29 new symbols and the stable/hook tag lists true"
    requirement: CAT-03
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/explorer/CatalogDocsDriftGuardTest.kt (9 tests; red against the old docs, green after)"
        status: pass
    human_judgment: false
  - id: C5
    description: "API.md, INTEGRATION.md and README cover every v2.6.0 surface (checklist table below)"
    requirement: DOC-03
    verification:
      - kind: unit
        ref: "CatalogDocsDriftGuardTest (counts, symbols, tags, INTEGRATION entry points)"
        status: pass
    human_judgment: true
    rationale: "Whether the prose explains each surface well enough for a consumer agent to wire it is a reading judgment; the guard proves presence, not clarity"
---

# Phase 24 Plan 02: Canary, additivity tool, docs and drift guards Summary

**The v2.6.0 surface is documented end to end (including a Stable test tags API section) and guarded by a v2.5.1 call-site canary that also builds on the v2.5.1 tag, a parsed-signature additivity tool with 22 fixture cases, and two permanent tests for the invariants and the doc counts. No library behaviour and no public API changed.**

## Performance

- **Duration:** 18 min (2026-10-12T18:58Z to 19:16Z)
- **Tasks:** 3 of 3 (5 commits: two fixes found by the plan's own falsifiers)
- **Files:** 6 created, 5 modified, plus this summary. `api.txt` is byte-identical to `cffad79`. The only `src/main` change is the `ComponentRegistry` count KDoc.
- **Full suite after the last commit:** `./gradlew testDebugUnitTest detekt apiCheck` BUILD SUCCESSFUL, 1315 tests, 0 failures, 22 skipped (skips pre-date this plan).

## Accomplishments

- **Task 1: canary.** `V251CallSiteCanaryTest` (12 tests, root test package) holds verbatim call shapes with a provenance comment each: SecondBrain `AppNavGraph.kt:870` (UndoCenterScreen, all four named, `holder::onUndo` and `holder::onClear` references), `VoiceSheetHost.kt:135/196/234/252`, `VoiceSheetModels.kt:16/47`, `VoiceConfirmSheetModels.kt:29`, `VoiceFailureSheetModels.kt:129-133`, `VoiceClipsScreen.kt:495/735/742`, `ClarificationSheetContent.kt:61`, `CardTypeFilterSheet.kt:59`, `VoiceAgentProviderCards.kt:67/87`; CalTracker `VoiceOutcomeMapper.kt:70-115/115/136/216/320/443`, `VoiceResultSheet.kt:136-192`, `SettingsScreen.kt:641`; and the v2.5.1 Explorer fixtures. A self-coverage test fails if any of the eleven names stops being called, if the function-reference or SheetScaffold trailing-content shapes disappear, or if a positional four-argument UndoCenterScreen call appears. Existing `*SourceCompatV251Test` classes are referenced, not copied.
- **Task 2: additivity tool and invariants guard.** See the output below. `LibraryInvariantsGuardTest` scans 146 `src/main` files (>1000 imports), the manifest (one `activity`, `application` without `android:name`, no service/receiver/provider), code for consumer identifiers, and `ReleaseDemos.kt` for consumer/domain nouns and a key-looking prefix. All forbidden identifiers are assembled from fragments (grep of the test source finds none whole).
- **Task 3: docs and CAT-03 guard.** `API.md`: five new family rows, six amended rows, new section 11 (types, Dimens groups, appended model fields, XR-178-01 mapping), the top-level `Stable test tags (documented API)` section (4 sub-tables of stable tags plus `Not part of the contract (test-only hooks)`), a v2.6.0 compatibility scope block and the corrected counts. `INTEGRATION.md` 5b (Receipt, key lifecycle, Undo Center groups, positional-call note, Explorer pointer). README counts and the registry KDoc paragraph corrected and now test-enforced. The README "Current release" line, the sample coordinate and the root `CLAUDE.md` are untouched (plan 24-04 changes them at the cut).

## Baseline compile of the canary (D-01 falsifier)

Command, run in an empty scratch directory holding `git archive v2.5.1` plus `local.properties` and the committed canary file:

```
./gradlew testDebugUnitTest --tests "io.github.ygaray.yahirandroidtaste.V251CallSiteCanaryTest" --offline
```

Result: **BUILD SUCCESSFUL, exit 0, 12 tests, 0 failures, 0 errors**, so every shape in the committed file exists on v2.5.1. The first run of the first cut was never executed on v2.5.1 before I noticed by reading that it asserted v2.6.0-only properties (`badge`, `tone`, `confirmEnabled`, `prefixLabel`, `icon`); that cut was fixed before the baseline run (deviation 1). `git diff --exit-code v2.5.1 -- build.gradle.kts settings.gradle.kts gradle.properties gradle jitpack.yml config` exits 0.

## Additivity tool output (real data, final version)

```
$ bash tools/verify-api-source-additive.sh v2.5.1
SEAMS: none
API SOURCE-ADDITIVE OK baseline=v2.5.1 removed_classes=0 removed_members=0 enum_constants_intact=55 required_param_changes=0 added_classes=29 added_members=224
```

Exit 0. Fixture test: 22 cases (identical, appended optional, mid-list optional, added class, added method, overload rewrite, removed class R1, removed method R2, removed property R2, dropped enum constant R3, added required parameter R4, retyped required parameter R4, removed required constructor parameter R4, required turned optional R4, removed annotated class R1, removed member of an annotated class R2, annotated class kept, unknown tag exit 2, unreadable head exit 2, missing argument exit 2, empty files exit 2, unrecognised header exit 2). `bash tools/test/run-all.sh` exits 0 and lists `test-verify-api-source-additive.sh`. The review against `git diff -U0 v2.5.1 -- api.txt` agrees: the 29 added classes are the 5 composable file classes, 17 types and 7 Dimens groups plus the annotated data classes.

## Final registry counts

66 registered + 5 intentionally unregistered = **71 total** public composables. Per family: Cards 13, Chips 6, Sheets 18, Buttons / FAB 5, Pickers 6, Feedback 3, Empty-state 1, Progress / Metrics 5, Tactile Foundation 4, Voice Command 5. API.md, README and the KDoc state these, and `CatalogDocsDriftGuardTest` fails if any drifts.

## Sketch checklist (DOC-03 item by item)

API.md headings are the family table of the component (section number in brackets) or "11" for section 11 "Tone foundation and Receipt types (v2.6.0)", "Tags" for the top-level "Stable test tags (documented API)". INTEGRATION.md "5b-i/ii/iii" are the three parts of section 5b.

| Item | API.md heading | INTEGRATION.md | KDoc symbol | Proving test |
|---|---|---|---|---|
| Sketch 1 ProviderKeyCard lifecycle (savedKeys, editingSavedKey, actions, labels; stateless; fingerprint only) | 10 Voice Command (`ProviderKeyCard` row), 11 (SavedKeyUiModel, ProviderKeyActions, ProviderKeyLabels, ProviderKeyDefaults) | 5b-ii | `ProviderKeyCard`, `SavedKeyUiModel`, `ProviderKeyActions` | `ProviderKeyCardLifecycleTest`, `ProviderKeyCardPerProviderTest`, `ProviderKeyCardRawKeySemanticsTest`, `ProviderKeyCardDefaultParityTest`, `ProviderKeyCardSourceCompatV251Test`, `ProviderKeyModelsTest` |
| Sketch 1 stable tags (`provider_key_*`) | Tags (ProviderKeyCard table, 9 tags) | 5b-i (resource-id note) | `ProviderKeyCard` TAG_ constants | `CatalogDocsDriftGuardTest`, `ProviderKeyCardLifecycleTest` |
| Y-1 tone foundation | 11 (ToneColors, ToneDefaults, ContentTone) | 5b-i (own tone tokens) | `ToneDefaults.error/warn`, `ContentTone` | `ToneTokensTest`, `ToneCardTest`, `ToneFoundationInvariantsTest` |
| Y-2 ReceiptRow | 1 Cards (`ReceiptRow` row) | 5b-i | `ReceiptRow` | `ReceiptRowTest`, `ReceiptRowLineHeightTest` (92dp), `ReceiptRowLongLabelTest`, `ReceiptRowMotionTest`, `ReceiptRowRegistryRenderTest` |
| Y-3 OutcomeSheet Receipt style | 10 (`OutcomeSheet` row), 11 (OutcomeSheetStyle, OutcomeSheetColors, OutcomeSheetDefaults) | 5b-i | `OutcomeSheet`, `OutcomeSheetDefaults` | `OutcomeSheetReceiptFrameTest`, `OutcomeSheetClassicParityTest`, `OutcomeSheetClassicParitySourceContractTest`, `OutcomeSheetDefaultsTest` |
| Y-4 NeedsConfirmation additions | 11 (appended model fields), compatibility scope | 5b-i (`confirmEnabled`, empty `items`) | `VoiceOutcomeUiState.NeedsConfirmation` | `OutcomeSheetReceiptNeedsConfirmationTest`, `OutcomeSheetSourceCompatV251Test`, `V251CallSiteCanaryTest` |
| Y-5 Success additions, SheetActionUiModel, OutcomeBadge | 11 (types and fields) | 5b-i (`activeWhileInFlight`) | `VoiceOutcomeUiState.Success`, `SheetActionUiModel` | `OutcomeSheetReceiptSuccessTest`, `OutcomeReceiptModelsTest` |
| Y-6 Failure additions, FailureTone | 11 (types and fields) | 5b-i (tip, secondaryAction, undo) | `VoiceOutcomeUiState.Failure`, `FailureTone` | `OutcomeSheetReceiptFailureTest`, `OutcomeSheetClassicDeltasTest` |
| Y-7 Handled-by pill | 11 (`HandledByUiModel` row) | 5b-i | `HandledByUiModel` | `OutcomeSheetClassicParityTest`, `OutcomeSheetReceiptSuccessTest`, `VoiceModelLabelDefaultsTest` |
| Y-8 Button upgrades, RoundIconButton | 4 Buttons / FAB (`DynamicActionButton`, `RoundIconButton` rows), 11 (ActionButtonEmphasis) | none (API only) | `DynamicActionButton`, `RoundIconButton` | `DynamicActionButtonEmphasisTest`, `DynamicActionButtonLegacyPathSourceContractTest`, `RoundIconButtonTest` |
| Y-9 StackedProgressBar | 8 Progress / Metrics (row), 11 (BarSegment) | none | `StackedProgressBar`, `BarSegment` | `StackedProgressBarTest`, `ToneAtomsRegistryRenderTest` |
| Y-10 ToneCard | 1 Cards (row) | 5b-i (via Receipt failure) | `ToneCard` | `ToneCardTest` |
| Y-11 SegmentedOptionSelector upgrades | 5 Pickers (row), 11 (SegmentedOptionStyle) | none | `SegmentedOptionSelector` | `SegmentedOptionSelectorPillTest`, `SegmentedOptionSelectorGalleryTest` |
| Y-12 PillChip | 2 Chips (row) | none | `PillChip` | `PillChipTest`, `ToneAtomsLongLabelTest` |
| Y-13 Sheet shape | 3 Sheets (`SheetScaffold` row) | none | `SheetScaffold` | `SheetScaffoldShapeTest`, `SheetScaffoldShapeProbeTest` |
| Y-14 Stable test tags | Tags (all sub-tables) | 5b-i | tag KDoc in `ReceiptRow`, `StackedProgressBar`, `ToneCard`, `PillChip`, `RoundIconButton` | `OutcomeSheetReceiptTagsTest`, `CatalogDocsDriftGuardTest` (stable/hook lists vs source) |
| Y-15 Explorer demos | covered by plan 24-01 (not API) | 5b-iv (pointer) | `ReleaseDemos.kt` | `ReceiptDemosRegistryRenderTest`, `ProviderKeyLifecycleDemoRegistryRenderTest`, `UndoGroupDemoRegistryRenderTest`, `LibraryInvariantsGuardTest` |
| P1 no shims | Adding / changing components, "Appending the Receipt, tone and key-lifecycle additions (v2.6.0)" | none | API.md compatibility block | `V251CallSiteCanaryTest`, `tools/verify-api-source-additive.sh` |
| P2 no literal append-only diff; cut gate | same block (guards bullet) | none | `tools/README-api-guard.md` "Source-additivity review (API-03)" | `tools/test/test-verify-api-source-additive.sh` (22 cases) |
| P3 Classic parity proven structurally | same block (Classic bullet) | none | `OutcomeSheet` | `OutcomeSheetClassicParitySourceContractTest`, `OutcomeSheetClassicParityTest`, `DynamicActionButtonLegacyPathSourceContractTest` |
| XR-178-01 a (refused/partial undo liveRegion Assertive, VXR-01) | 11 "XR-178-01 mapping" (a), Tags (`outcome_sheet_undo_refused`) | 5b-i | `OutcomeSheet` undo-refused surface | `OutcomeSheetLiveRegionTest` |
| XR-178-01 b (`Failure.undo`, VXR-02) | 11 (Failure row, mapping b) | 5b-i | `VoiceOutcomeUiState.Failure` | `OutcomeSheetReceiptFailureTest`, `OutcomeSheetClassicDeltasTest` |
| XR-178-01 c (Undo Center groups, VUNDO-02) | 6 Feedback (`UndoCenterScreen` row), 11 (UndoGroupRowUiModel, mapping c) | 5b-iii | `UndoCenterScreen`, `UndoGroupRowUiModel` | `UndoCenterScreenGroupsTest`, `UndoCenterRowsTest`, `UndoCenterScreenSourceCompatV251Test`, `UndoGroupDemoRegistryRenderTest` |

No row lacks a doc location, so there is no gap to carry over.

## Task Commits

| Task | Commit | Lane | Notes |
|---|---|---|---|
| 1 | 060ac09 | LANE 1 | first cut of the canary |
| 1 (fix) | c1827d9 | LANE 1 | canary asserts only v2.5.1 properties (deviation 1) |
| 2 | 2352b09 | LANE 1 | tool, fixtures, README section, invariants guard |
| 2 (fix) | 8813366 | LANE 1 | tool parses annotated class headers (deviation 2) |
| 3 | c3f3480 | LANE 2 (override declared) | `HUB_LANE_OVERRIDE=2`: the only `src/main` change rewrites the existing KDoc count paragraph of `ComponentRegistry`; no code line changed |

`git rev-list --count cb335b5..HEAD` measured 5 before this summary commit.

## Decisions Made

- The canary lives in the root test package and asserts only v2.5.1 properties; the rest of the Receipt defaults stay in `OutcomeSheetSourceCompatV251Test`.
- Additivity means "same required parameters", so the strict case "a required parameter turned optional" also fails (R4 is strict by design and has a fixture).
- Stable-vs-hook is read from the source KDoc: only `receipt_row_chevron`, `receipt_row_cue`, `receipt_row_value_column` and `stacked_progress_bar_segment` are declared test-only.
- The undo kebab-case tags and `demo_` tags are described in prose under the hook sub-heading, not as table rows, so the guard can assert they are in neither list.

## Owner flags

1. **Stable classification of three decoration/error tags.** `outcome_sheet_handled_by_dot`, `outcome_sheet_action_bar_divider` and `provider_key_saved_error` are listed as stable because their KDoc does not call them test-only (and D-09 asks for every `provider_key_*` tag). If you would rather not promise the dot and divider, add a "test-only" KDoc line in a later phase and move the rows to the hook table; the guard will follow the docs.
2. **Positional UndoCenterScreen break** is documented in API.md, INTEGRATION.md 5b-iii and excluded from the canary (as owner-flagged in Phase 23).
3. **Fingerprint examples** in the docs use only the placeholder form (`key-…c3d4`).

## Deviations from Plan

**1. [Rule 1 - Bug] The first canary cut did not compile on v2.5.1**
- **Found during:** Task 1, before the baseline run (reading the file against `git show v2.5.1:api.txt`)
- **Issue:** I asserted v2.6.0-only defaults (`badge`, `titleActions`, `tone`, `tip`, `confirmEnabled`, `prefixLabel`, `onDevice`, `icon`, the `FailureTone` import), which cannot compile on the tag, so the file would not have been a v2.5.1 canary.
- **Fix:** replaced them with v2.5.1 properties (`summary`, `handledBy`, `reason`, `action`, `body`, `semanticsPrefix`, `selectionMode`, defaults of `confirmLabel`/`cancelLabel`/`severity`, `escalationsLabel`, `role`). The baseline run then passed first time (12 tests).
- **Commit:** c1827d9 (first cut 060ac09)

**2. [Rule 1 - Bug] The additivity tool skipped annotated class headers**
- **Found during:** Task 3, reading the full list of new classes (eight `@Immutable` data classes were missing from my listing, built with the same pattern)
- **Issue:** headers like `@androidx.compose.runtime.Immutable public final class X {` did not match, so those classes were invisible to R1 and their members were credited to the preceding class (a false negative for removals).
- **Fix:** the header pattern accepts leading annotations; an unrecognised block opener now exits 2. Four fixture cases added (the removed-annotated-class case would have passed with the bug). Real run: `added_classes` 21 -> 29, still exit 0.
- **Commit:** 8813366

**3. [Rule 3 - Blocking] Two robustness fixes in CatalogDocsDriftGuardTest**
- The plan's verify command lists `apiDump` and the tests in one Gradle invocation; Gradle ran them in parallel and the test saw `api.txt` mid-rewrite. `readApiTxt()` retries for up to 10 s. (A `mustRunAfter` in `build.gradle.kts` was ruled out: D-01 forbids a Gradle config change.)
- The tag scan collects all stable-prefix string literals (needed for `outcome_sheet_action_start/_end`, passed as a parameter), but `receipt_row_alpha` is an animation `label = "..."` in `ReceiptRow.kt`, not a tag, so literals directly after `label = ` are skipped.
- **Commit:** c3f3480

**4. [Rule 2 - Missing critical] `dagger.` added to the import allowlist** because `CLAUDE.md` names Hilt in the allowed set; the library imports only `javax.inject` today, so nothing changes now.

**5. [Process] Not strictly red-first.** `CatalogDocsDriftGuardTest` was red against the old docs (recorded in the commit body). The canary, the invariants guard and the tool's fixtures were written alongside passing code: the canary is validated by the v2.5.1 build, the invariants guard by a one-off mutation (a probe string in `ReleaseDemos.kt` failed it; restored, `git status` clean), and the tool by the reversed-baseline run (21 classes removed) and the fixtures.

**Total deviations:** 5 (2 bugs found by the plan's own falsifiers, 1 blocking, 1 missing-critical, 1 process note). **Impact:** none on scope; no library behaviour or API change.

## Files plans 24-03 and 24-04 depend on

- **24-04 (cut gate):** `tools/verify-api-source-additive.sh` (+ `.py`) for D-02 step 5; `tools/test/run-all.sh` (now 5 suites); `V251CallSiteCanaryTest`, `LibraryInvariantsGuardTest`, `CatalogDocsDriftGuardTest` in the full `testDebugUnitTest`; `API.md`, `INTEGRATION.md`, `README.md` (current-release line and sample coordinate still `v2.5.0`, to be changed at the cut together with the root `CLAUDE.md` line).
- **24-03 (screencaps):** nothing new; the demo contract is in `24-01-SUMMARY.md`.

## Issues Encountered

None open. The pre-existing modified and untracked `.planning/` files listed in the dispatch were left alone and unstaged.

## Next Phase Readiness

Ready for 24-03 and 24-04. The cut gate can call the additivity tool and rely on the three new tests being part of the full suite.

## Self-Check: PASSED

- Created files exist (canary, two guard tests, tool `.sh` and `.py`, fixture test).
- Commits found: 060ac09, c1827d9, 2352b09, 8813366, c3f3480.
- Re-checked after the last commit: `git diff --exit-code cffad79 -- api.txt` clean, config files identical to v2.5.1, the five protected v2.5.1 tests byte-identical, `API.md` has exactly one `## Stable test tags (documented API)` heading, `INTEGRATION.md` names `OutcomeSheetStyle.Receipt`, the real additivity run prints `SEAMS: none` and OK, `bash tools/test/run-all.sh` passes, full `testDebugUnitTest detekt apiCheck` BUILD SUCCESSFUL (1315 tests, 0 failures).
