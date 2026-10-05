---
phase: 18-catalog-integrity-api-dump-docs
plan: 01
subsystem: docs
tags: [api-md, integration-md, voice-command, v2.5.0, doc-02]

requires:
  - phase: 15-voice-surface-i18n-label-params
    provides: the 13 label params/fields and the appended-last compatibility pattern
  - phase: 16
    provides: Failure enrichment (role, body, semanticsPrefix)
  - phase: 17-approachladdercard-router-on-off-toggle
    provides: router toggle params
provides:
  - "API.md section 10 Voice Command (5 composable rows, 6-row appended-model table, all 20 v2.5.0 additions with verbatim defaults)"
  - "API.md v2.5.0 compatibility notes extended: ProposedItemUiModel trailing-lambda caveat + Failure/router sibling paragraph"
  - "INTEGRATION.md localization-override subsection; BOM and family-count corrections"
affects: [phase-19-ship, 18-02-evidence]

actuals:
  tokens: 2800
  tasks: 3
  commits: 3

plan_head_before: bee155d787a4d2b9a58b9b804b81b486e3fde0ec
commits: 3

tech-stack:
  added: []
  patterns:
    - "document-only source-break caveat in the showTagColors style (named-argument mitigation)"

key-files:
  created: []
  modified:
    - API.md
    - INTEGRATION.md

key-decisions:
  - "D-02 '~13' reconciled: all 20 additions documented (13 label + 3 Failure + 4 router), none deferred"
  - "ProposedItemUiModel trailing-lambda break is documented only, no code fix (orchestrator ruling)"
  - "Binary compatibility attributed to the javap proof, re-proven at the v2.5.0 cut; not asserted as Phase 18 verified"

requirements-completed: [DOC-02]

status: complete
---

# Phase 18 Plan 01: Voice Command docs (DOC-02) Summary

Documented the full v2.5.0 voice surface in `API.md` (new `## 10. Voice Command`, all 20 additions with source-verbatim defaults) and `INTEGRATION.md` (localization note), including an honest `ProposedItemUiModel` trailing-lambda source-break caveat, with no code, `api.txt`, registry or tag change.

## Tasks and commits

| Task | Name | Commit | Hook lane |
|------|------|--------|-----------|
| 1 (tracer) | API.md section 10 Voice Command | `8552c23` | LANE 1 (mode=additive, baseline=v2.4.1), no override |
| 2 | Compatibility notes: ProposedItemUiModel caveat + Failure/router sibling paragraph | `faf0331` | LANE 1, no override |
| 3 | INTEGRATION.md localization note + BOM / family-count fixes | `b86c710` | LANE 1, no override |

Commit count measured from the ledger: `bee155d..HEAD` = 3 (all docs-only; only API.md and INTEGRATION.md).

## What was built

- **API.md section 10** sits between section 9 and "Intentionally-unregistered sub-parts (5)". Five composable rows in registry order under the standard header, an `ApproachLadderCard` behaviour list (router toggle semantics, rung radio-button accessibility, English state words as a declared residual), and a six-row appended-model-fields table. Null semantics are stated for router/onRouterChange (hide, no space), body (no node, no space), semanticsPrefix (null/blank leaves announcement unchanged) and role (defaults Neutral). The single-ASCII-space join and "Static UI copy only, never sensitive text" rule (T-18-01) are included, as is the note that `ProposedItemUiModel.toString` omits the new label.
- **Compatibility notes (API.md):** first VI18N bullet qualified with "except the `ProposedItemUiModel` trailing-lambda call below"; one new bullet added; one new sibling paragraph "Appending the Failure enrichment and router toggle (v2.5.0, VFAIL-01..03, VAPPR-04)" between the VI18N paragraph and "Voice label fragments".
- **INTEGRATION.md:** `### Localizing the voice surface (optional)` in section 5 (names all 16 label params/fields plus `trailingContent`, one `ClarificationBar` example, points to API.md section 10). BOM 2026.02.01 -> 2026.04.01 (both places; matches `composeBom` in `gradle/libs.versions.toml`); "seven-family" -> "ten-family".

### Exact text of the new ProposedItemUiModel trailing-lambda bullet

> - It is **NOT source-compatible** for a **trailing-lambda** call of `ProposedItemUiModel`. The v2.4.x call `ProposedItemUiModel(id, title) { … }` bound its lambda to `trailingContent`, which was the last constructor parameter. v2.5.0 appended `removeContentDescription: String` after it, so Kotlin now binds a trailing lambda to that `String` and the call no longer compiles. Pass the slot as the named argument `trailingContent = { … }` when moving to v2.5.0. Metalava's `api.txt` check does not cover call syntax. It is a source-level break only: v2.4.x-compiled binaries still link, per the binary-compatibility bullet above. A sweep of the known call sites on 2026-10-05 (SecondBrain, CalTracker, hub-internal) found only the named form, so none breaks. It is the same hazard class as the v2.3.0 `showTagColors` addition. Every other appended-last addition is unaffected because its former last parameter was not a function type, and `FailureActionUiModel` (whose former last parameter `onClick` is a function type) is covered by its explicit two-argument constructor.

## Source defaults that differed from the plan's quoted values

None. All 20 defaults re-read from source (`ApproachLadderCard.kt`, `ProviderKeyCard.kt`, `ModelSelectCard.kt`, `ClarificationBar.kt`, `HandledByUiModel.kt`, `UndoRowUiModel.kt`, `UndoRefusedUiModel.kt`, `ProposedItemUiModel.kt`, `FailureActionUiModel.kt`, `VoiceOutcomeUiState.kt`) matched byte-for-byte, including the ASCII apostrophe in `"Couldn't undo:"`.

## Verification

All task `<verify>` commands re-run after each commit and passed: region-scoped 20-pair check, exactly-one heading, 5 registry-order rows, standard header, privacy rule and single-ASCII-space text present, no consumer names in section 10 or the INTEGRATION note, 61/5/66 counts and the Phase 19 `tools/verify-binary-abi.sh` sentence byte-present, sibling paragraph exactly once, at most three pre-existing lines replaced, unsupported Phase 15 claim absent, nothing changed under `src/`, `tools/`, `api.txt`, `config/`, build files, `CLAUDE.md`, `README.md`, `ECOSYSTEM.md` since `625c714`. The "SecondBrain" occurrence in INTEGRATION.md is the pre-existing intro line, outside the new subsection.

`15-VERIFICATION.md` was not edited and its line-60 claim was not carried over.

## Deviations from Plan

None - plan executed exactly as written.

## Residuals and hand-offs

1. The `ProposedItemUiModel` trailing-lambda break is **documented only**, no code fix, per the orchestrator ruling; surfaced for the orchestrator before Phase 19.
2. INTEGRATION.md's stale "tag is cut in Phase 102" note and CLAUDE.md's "v2.3.0 is the current release" line are release bookkeeping left to Phase 19.
3. The `tools/verify-binary-abi.sh` wording in API.md is Phase 19's.
4. No tag cut, no consumer touched. `18-VALIDATION.md` not edited.

## Threat model

T-18-01 mitigated (static-copy rule in section 10 and sibling paragraph), T-18-02 mitigated (first bullet qualified, caveat documented, binary compat attributed to javap proof), T-18-03 mitigated (only API.md and INTEGRATION.md changed), T-18-04 mitigated (no consumer names in new section/note; named consumers only in the sweep sentence, per the v2.3.0 precedent).

## Self-Check: PASSED

- API.md and INTEGRATION.md present; commits `8552c23`, `faf0331`, `b86c710` found in `git log`.
