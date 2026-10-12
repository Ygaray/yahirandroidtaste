---
phase: 23-providerkeycard-lifecycle-undo-center-groups
plan: 01
subsystem: ui
tags: [compose, providerkeycard, semantics, metalava, robolectric, source-compat]
requires:
  - phase: 20-tone-foundation
    provides: DynamicActionButton roles and emphasis (Save/Update/Clear/Cancel buttons)
provides:
  - ProviderKeyCard key lifecycle (savedKeys, editingSavedKey, actions, labels) with fingerprint-only summary
  - SavedKeyUiModel, ProviderKeyActions, ProviderKeyLabels, ProviderKeyDefaults public types
  - Stable tags provider_key_saved_summary and provider_key_action_{save,update,clear,cancel}
  - Shared test support SemanticsDump (normalized tree, raw scan, bounds)
affects: [23-02-undo-center-groups, 24-registry-explorer-docs]
tech-stack:
  added: []
  patterns:
    - "Wave 0 characterization: goldens captured from unmodified code, committed before any src/main edit, never edited"
    - "Real if/else Summary/Field branch so the key field is not composed in Summary mode"
    - "Raw-config semantics scan over every root with a positive control"
key-files:
  created:
    - src/main/java/io/github/ygaray/yahirandroidtaste/model/SavedKeyUiModel.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/model/ProviderKeyActions.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/model/ProviderKeyLabels.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyDefaults.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/testsupport/SemanticsDump.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCardDefaultParityTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCardSourceCompatV251Test.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCardLifecycleTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCardPerProviderTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCardRawKeySemanticsTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCardLongLabelTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCardSourceContractTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/model/ProviderKeyModelsTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCard.kt
    - api.txt
key-decisions:
  - "A1: a saved-key summary renders from props alone; both action rows render only when actions != null"
  - "A2: dropdown menu rows of saved providers read label, middle dot, savedMarker; the anchor keeps the plain label"
  - "A3: emphasis Save Filled, Update Tonal, Clear Text, Cancel Text (sketch fixes roles only)"
patterns-established:
  - "Internal seams (providerKeyMode, savedSummaryText, providerMenuLabel) so decisions are testable without a render"
requirements-completed: [VKEY-01, VKEY-02]
duration: ~75min
completed: 2026-10-12
status: complete
plan_head_before: 92febe1a0ffd89f5f2bf382d644874800cf0856e
commits: 4
actuals:
  tokens: 23000
  tasks: 4
  commits: 4
---

# Phase 23 Plan 01: ProviderKeyCard key lifecycle Summary

**ProviderKeyCard now owns Save/Update/Clear/Cancel with a fingerprint-only saved-key summary (key field not composed), while a default call renders exactly as v2.5.1, proven by goldens captured before the first src/main edit.**

## Accomplishments

- Wave 0: five default-state semantics trees plus tagged-node bounds pinned from the unmodified v2.5.1 card, and compile canaries for the v2.5.1 named and positional call shapes. Never edited afterwards; still green after the widening.
- Widened `ProviderKeyCard` with `savedKeys`, `editingSavedKey`, `actions`, `labels` appended after `providerLabel`, all defaulted off. Four new public types (D-01). No hidden overload, no copy shim (D-02): the single pre-existing v2.4.x hidden overload is untouched.
- Summary mode (selected provider saved and not editing): the key field is not composed; one merged `provider_key_saved_summary` node reads `Saved · <fingerprint>`; Update then Clear below it.
- Field mode: the unchanged `ClearableTextField` call, then (when `actions != null`) Cancel then Save. Save enabled iff `saveEnabled && !inFlight`; everything else iff `!inFlight`.
- Dropdown menu rows of saved providers read `Label · saved`; the anchor field keeps the plain label.
- Proofs: lifecycle/gating/48dp/custom-label/explicit-default-parity (25 tests), three-provider switching + reveal flag (4), raw-key scan with positive control (6), German/Spanish 360dp NATIVE (5), model defaults (5), source contract (5).

## Task Commits

| Task | Commit | Lane | Notes |
|------|--------|------|-------|
| 1 Wave 0 characterization | a9ab630 | LANE 1 | tests only |
| 2 Tracer (types, signature, summary, Update/Clear, api.txt) | 8a9ee49 | LANE 2, `HUB_LANE_OVERRIDE=2` | rewrites the v2.5.1 `ProviderKeyCard` signature line and moves the field call into a branch; defaults proven unchanged by Wave 0 goldens |
| 3 Field Save/Cancel, gating, saved markers | ab562ed | LANE 2, `HUB_LANE_OVERRIDE=2` | extends lines added in Task 2 plus the dropdown call; cumulative classification vs v2.5.1 is LANE 2; api.txt unchanged |
| 4 Raw-key, long-label, models, contract | fe2b5ac | LANE 1 | tests only; `ProviderKeyCard.kt` not touched |

Measured: `git rev-list --count 92febe1..HEAD` = 4 (before this SUMMARY commit).

## Gate outputs (Task 4)

- Full unfiltered `./gradlew apiDump testDebugUnitTest detekt apiCheck`: BUILD SUCCESSFUL. 1202 tests, 0 failures, 22 skipped (pre-existing `@Ignore`). That includes ComponentRegistryDriftGuardTest, DomainVocabularyDriftGuardTest and the other drift guards; no registry/allowlist edit was needed or made.
- detekt: zero findings, no baseline change.
- `git diff --exit-code -- api.txt`: clean (fresh).
- Protected tests (`ProviderKeyCardTest`, `VoiceBinaryCompatShimTest`, `VoiceI18nSourceCompatTest`, `UndoCenterScreenTest`): byte-identical to v2.5.1.
- `git diff --exit-code 36ff044 -- API.md INTEGRATION.md README.md src/main/.../explorer`: clean (Phase 24 untouched).
- `git log v2.5.1..HEAD -S"DeprecationLevel.HIDDEN" -- src/main`: no output (no hidden-deprecated declaration added or removed).

### api.txt removed-line audit vs v2.5.1: 15 (as expected)

Phase 20-22: `DynamicActionButton` method, `OutcomeSheet` method, `SegmentedOptionSelector` method, `SheetScaffold` method, `FailureActionUiModel` ctor and `copy`, `HandledByUiModel.copy`, `VoiceOutcomeUiState.Failure.copy`, `VoiceOutcomeUiState.NeedsConfirmation` ctor, `component10`, `component11` and `copy`, `VoiceOutcomeUiState.Success` ctor and `copy` (14). This plan: the old `ProviderKeyCard` signature (1). Total 15.

## Decisions Made

- A1, A2, A3 as in front matter (recorded in the plan; implemented as specified).
- The summary Row holds only the decorative check icon and one `Text`; the buttons are siblings, never inside the merged node.

## Deviations from Plan

**1. [Rule 1 - Test defect] EditableText assertion in the raw-key scan.** The plan says no node may carry an `EditableText` entry in Summary mode. The read-only provider dropdown anchor is itself an `OutlinedTextField` and always carries `EditableText=<plain label>`, so the literal assertion is unsatisfiable. The test instead requires every `EditableText` line to equal the anchor's plain label (`Anthropic`) and asserts no `provider_key_card_key_field` tag, which is what the assertion was protecting.

**2. [Rule 1 - Test defect] Positive control reveals the key.** In this Compose version the masked field's unmerged `EditableText` carries the transformed bullets, not the raw value (visible in the Wave 0 S2 golden). The control therefore taps Show key before scanning, then finds the sentinel; a masked Field scan alone would not prove the scan can see the key.

**3. [Rule 1 - Test defect] Overflow check in the long-label test.** `TextLayoutResult.hasVisualOverflow` / `didOverflowWidth` read true for every wrap-content label narrower than its slot in this NATIVE setup (a 59px "Cancelar" in a 136px slot), so they prove nothing. The test asserts instead: no ellipsized line, no height overflow, laid-out width within its constraint, unclipped right edge inside the 360dp window, plus the button tag's in-window left/right edges.

**4. [Rule 1 - Test defect] `$stable` field.** The Compose compiler adds a synthetic `$stable` field; the model-shape reflection test filters `$`-prefixed names.

**5. Bounds test is one method.** S1 and S2 bounds share one `setContent` (state driven) instead of two; the plan allowed one bounds test. Note S1 key field is 76px tall and S2 77px (clear icon appears); both pinned.

**Process note.** The Gradle daemon disappeared mid-run once (daemon pid exited during the first full gate); the run was repeated with no code change. Not a plan defect.

No source change was needed in Task 4 (`ProviderKeyCard.kt` unchanged), so no wrap escape hatch was applied.

## Owner flags (for Yahir / the orchestrator)

- **A1 summary without actions:** a summary shown with `actions == null` renders no buttons and offers no way out. Spec-literal; one condition changes if the owner wants it gated on `actions != null`.
- **A2 marker format:** `Label · saved` on dropdown menu rows only; the anchor stays plain. Tests depend on the exact string. Cosmetic.
- **A3 emphasis:** Save Filled, Update Tonal, Clear Text (error hue), Cancel Text. Visual only, one parameter per button.
- **Long labels:** German (Speichern, Aktualisieren, Entfernen, Abbrechen, Gespeichert) and Spanish (Guardar, Actualizar, Borrar, Cancelar, Guardada) all stay inside 0..360dp in both modes on one row; no wrap needed. A 12-plus-character fingerprint stays within the summary node. The open dropdown shows `Anthropic · gespeichert`.
- **Gate-1 not done here:** no device work was performed by this plan. The visual look of the new rows (spacing, tonal Update, destructive Clear) is only structurally proven; the orchestrator/tester owns on-device verification, and Phase 24 owns the Explorer demos.
- **Worktree drift guards:** ComponentRegistryDriftGuardTest and DomainVocabularyDriftGuardTest walk up to the first directory named `yahirandroidtaste`, which can be the main checkout rather than this worktree; run one full `./gradlew testDebugUnitTest` in the main checkout after merge (22-RESEARCH P12).
- No tag, no push, no consumer edits.

## Self-Check: PASSED

- Files exist: the four model/defaults sources, `SemanticsDump.kt`, the seven new test classes, `ProviderKeyModelsTest.kt` (all present, committed).
- Commits exist: a9ab630, 8a9ee49, ab562ed, fe2b5ac.
- `STATE.md` and `ROADMAP.md` not modified.
