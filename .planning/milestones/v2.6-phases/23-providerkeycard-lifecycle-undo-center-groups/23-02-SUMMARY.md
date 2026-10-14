---
phase: 23-providerkeycard-lifecycle-undo-center-groups
plan: 02
subsystem: ui
tags: [compose, undo-center, groups, semantics, metalava, robolectric, source-compat]
requires:
  - phase: 23-providerkeycard-lifecycle-undo-center-groups
    provides: plan 23-01 shared SemanticsDump test support and the Wave 0 characterization protocol
  - phase: 20-tone-foundation
    provides: Dimens and surfaceContainer tokens reused by the group header
provides:
  - UndoGroupRowUiModel (D-06 signed shape) in feedback/
  - UndoCenterScreen widened with groups and onUndoGroup before the trailing onClear
  - Pure row-builder seam buildUndoCenterRows plus text helpers and the Undo all visibility rule
  - Tags undo-group-header-{groupId} and undo-group-undo-all-{groupId}
  - Phase 23 gate evidence, owner flags and the Gate-1 handoff
affects: [24-registry-explorer-docs, secondbrain-repin]
requirements-completed: [VUNDO-02]
actuals:
  tokens: 15000
  tasks: 5
  commits: 4
duration: ~45min
completed: 2026-10-12
status: complete
plan_head_before: 8738c76c4f87d5cd1b0507176e5690a9643e0afa
commits: 4
tech-stack:
  added: []
  patterns:
    - "Wave 0 characterization before the first edit of the composable, goldens never edited afterwards"
    - "Pure internal seam (buildUndoCenterRows) decides every row; empty groups returns the v2.5.1 flat list 1:1"
    - "Optional modifier on the private row so the ungrouped modifier chain is untouched"
key-files:
  created:
    - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoGroupRowUiModel.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreenDefaultParityTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreenSourceCompatV251Test.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterRowsTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreenGroupsTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreenLongLabelTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreenSourceContractTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreen.kt
    - api.txt
key-decisions:
  - "Undo all is shown only when onUndoGroup != null and the status is Undoable or PartiallyResolved"
  - "Status wording: Undoable, Partly resolved, Resolved (FullyResolved also covers failed members, so not Undone); count text 1 item / n items; all English-only library text"
  - "Group tag names are kebab-case internal constants, not Y-14 tags"
  - "Member indent is Dimens.HorizontalPadding; header background is surfaceContainer; header label carries heading semantics and no gesture"
---

# Phase 23 Plan 02: Undo Center group rows Summary

**UndoCenterScreen now renders one header per UndoHistoryStore group (label, count, status, a single Undo all) with indented members and flat ungrouped entries, while a default call renders exactly the v2.5.1 flat list, proven by goldens captured before the first edit of the screen.**

## Accomplishments

- Wave 0: four default-state semantics trees (three entries Available/Undone/Failed, empty list, one entry, two entries; relative-time text normalized to a placeholder, never dropped) plus row bounds and order, captured from the byte-identical v2.5.1 `UndoCenterScreen.kt`. Never edited afterwards; still green after the widening.
- `UndoGroupRowUiModel(groupId, label, memberIds, status, undoAllLabel = "Undo all")` exactly as D-06 signs it, `@Immutable`, reusing `UndoGroupStatus`.
- `UndoCenterScreen(entries, onNavigateBack, onUndo, groups = emptyList(), onUndoGroup = null, onClear)`. Trailing-lambda and fully named sites compile.
- `buildUndoCenterRows(entries, groups)`: empty groups returns entries 1:1 flat; otherwise the first present member emits the header followed by all present members (indented, entries order); a group with no present member emits nothing; duplicate groupId and double-listed ids resolve first-wins so keys stay unique.
- Header: label (heading semantics, no gesture, two lines max), `n items · status` subtitle, Undo all `TextButton` tagged `undo-group-undo-all-{id}`; `onUndoGroup(groupId)` fires once per click.
- Real-store end to end: Undo all drives `attemptUndoGroup`, members read Undone, header reads `Resolved`, Undo all disappears; a refusal leaves members Available and Undo all present for retry; undoing one member first yields `Partly resolved`.
- German `Alle rückgängig machen`, Spanish `Deshacer todo` and a long group label at 360dp NATIVE: the button stays inside the window, no ellipsis or height overflow, one line, and the label and subtitle end before the button starts. No escape hatch (stack-under-text) was needed.

## Task Commits

| Task | Commit | Lane | Notes |
|------|--------|------|-------|
| 1 Wave 0 characterization | 9f83754 | LANE 1 | tests only; `UndoCenterScreen.kt` byte-identical to v2.5.1 at capture time |
| 2 Tracer (model, seam, header, click, api.txt) | 06cc1e2 | LANE 2, `HUB_LANE_OVERRIDE=2` | rewrites the v2.5.1 `UndoCenterScreen` signature and the list body; defaults proven unchanged by the Wave 0 goldens |
| 3 Indent, status rule, edge cases, end-to-end | 6b3890e | LANE 2, `HUB_LANE_OVERRIDE=2` | edits the root modifier-chain line of the private row and the list body (both v2.5.1 lines); default chain unchanged; api.txt unchanged |
| 4 Long-label, call shapes, source contract | 6c5286d | LANE 1 | tests only; no production change |

Measured: `git rev-list --count 8738c76..HEAD` = 4 (before this SUMMARY commit). `plan_head_before` is 8738c76 (the per-plan ledger file could not be written inside the worktree git dir, so the base was taken from the dispatch prompt and verified as HEAD at start).

Phase roll-up (23-01 and 23-02), `git log --oneline 36ff044..HEAD`: 92febe1 (plan), a9ab630, 8a9ee49, ab562ed, fe2b5ac (23-01 code), 39756e9, 0b717c1, 8738c76 (23-01 docs and merge), then this plan's 9f83754, 06cc1e2, 6b3890e, 6c5286d. `HUB_LANE_OVERRIDE=2` commits: 8a9ee49 and ab562ed (23-01 ProviderKeyCard), 06cc1e2 and 6b3890e (this plan, reasons in the table above).

## Requirement coverage

| Requirement | Plan | Proving tests |
|-------------|------|---------------|
| VKEY-01, VKEY-02 | 23-01 | ProviderKeyCardLifecycleTest, ProviderKeyCardPerProviderTest, ProviderKeyCardRawKeySemanticsTest, ProviderKeyCardLongLabelTest, ProviderKeyCardDefaultParityTest, ProviderKeyCardSourceContractTest |
| VUNDO-02 | 23-02 | UndoCenterScreenDefaultParityTest (default = v2.5.1), UndoCenterRowsTest (seam), UndoCenterScreenGroupsTest (header, Undo all, status matrix, indent, gestures, real-store end to end), UndoCenterScreenLongLabelTest, UndoCenterScreenSourceCompatV251Test, UndoCenterScreenSourceContractTest |

## Gate outputs (Task 5)

- Full unfiltered `./gradlew apiDump testDebugUnitTest detekt apiCheck`: BUILD SUCCESSFUL. 1254 tests, 0 failures, 0 errors, 22 skipped (pre-existing `@Ignore`). Includes ComponentRegistryDriftGuardTest, DomainVocabularyDriftGuardTest and the other drift guards; no registry or allowlist edit was needed (no new public composable).
- detekt: zero findings; `git diff --exit-code v2.5.1 -- config/detekt-baseline.xml config/detekt/detekt.yml config/detekt-compose.yml` clean.
- apiCheck green; `git diff --exit-code -- api.txt` after the dump: clean (fresh).
- `bash tools/test/run-all.sh`: all four fixtures ok (12, 6, 5 and 52 passes, 0 failures).
- Protected tests (`ProviderKeyCardTest`, `VoiceBinaryCompatShimTest`, `VoiceI18nSourceCompatTest`, `UndoCenterScreenTest`, `UndoHistoryStoreTest`): byte-identical to v2.5.1.
- `git log v2.5.1..HEAD --oneline -S"DeprecationLevel.HIDDEN" -- src/main`: no output.
- `git tag --list "v2.6*"`: no output. Nothing pushed.
- `git diff --exit-code 36ff044 -- API.md INTEGRATION.md README.md src/main/.../explorer`: clean (Phase 24 files untouched).
- `phase.has-uat-criteria 23 --pick has_uat_criteria`: `true`. `23-CONTEXT.md` carries no `device_exempt`.

### Audits

`git diff --numstat v2.5.1 -- api.txt`: `337 16 api.txt` (16 removed lines). The 16 removed lines, each with a same-member added counterpart: `DynamicActionButton`, `OutcomeSheet`, `ProviderKeyCard`, `SegmentedOptionSelector`, `SheetScaffold` and `UndoCenterScreen` methods; `FailureActionUiModel` ctor and `copy`; `HandledByUiModel.copy`; `VoiceOutcomeUiState.Failure.copy`; `VoiceOutcomeUiState.NeedsConfirmation` ctor, `component10`, `component11` and `copy`; `VoiceOutcomeUiState.Success` ctor and `copy`. That is the 14 from Phases 20-22, `ProviderKeyCard` from 23-01 and `UndoCenterScreen` from this plan.

`git diff --numstat v2.5.1`: `UndoCenterScreen.kt` 151 added, 4 removed; `ProviderKeyCard.kt` 179 added, 18 removed.
- `UndoCenterScreen.kt` removed v2.5.1 lines (4): the `items(entries, key = { it.id }) { entry ->` line and its `UndoHistoryRow(entry = entry, ...)` body line (replaced by the row `when`), the `private fun UndoHistoryRow(entry, onUndo)` signature line (gains an optional `modifier`) and the `modifier = Modifier` line (now `modifier = modifier`). The two new parameters are pure insertions. These are the Classic-path deltas; the default-parity goldens are the behavioral proof.
- `ProviderKeyCard.kt` removed v2.5.1 lines: the `providerLabel: String = "Provider"` line (now has a trailing comma), the old key-field block (15 lines, re-indented inside the Field branch), one KDoc line and the `text = { Text(option.label) }` menu line. Default behavior proven by the 23-01 goldens.

`bash tools/classify-hub-change.sh --baseline v2.5.1` run with nothing staged prints `LANE 1 (mode=additive, baseline=v2.5.1)`; it classifies the staged diff, so the cumulative LANE 2 shape is visible only in the per-commit results above. Informational.

### Worktree note (Pitfall 14)

This phase ran in the `agent-af5a0f52f36652bde` worktree. `DomainVocabularyDriftGuardTest` and `ComponentRegistryDriftGuardTest` walk up to the first directory named `yahirandroidtaste`, which is the MAIN checkout, so their green result says nothing certain about the worktree's new sources. Re-run one full `./gradlew testDebugUnitTest` in the main checkout after the merge as the real confirmation for those two guards. No public composable was added, so no allowlist or registry edit is expected.

## Deviations from Plan

**1. [Rule 3 - Blocked tooling] Per-plan commit ledger not written.** The harness refused a Write into the shared git dir from the worktree, so `gsd-plan-head-before-23-02` was not created. The base commit 8738c76 was verified at start (HEAD equals the dispatch base) and used for `plan_head_before` and the measured commit count.

**2. [Rule 1 - Test scope] Wave 0 harness shared constants.** The Wave 0 file also exposes `UndoCenterScreenGoldens.entry/threeEntries/availableEntries/normalizeTime` helpers (reused by the later test classes instead of copied), in addition to the D1-D4 and bounds constants. The goldens themselves are untouched after commit 9f83754.

**3. UndoCenterScreenSourceCompatV251Test append.** The two Task 1 compile properties and the Task 1 test are unchanged; the class KDoc first sentence gained a clause and the new properties and the model test were appended.

No production change was needed in Task 4; no long-label escape hatch was applied.

## Owner flags (for Yahir / the orchestrator)

1. **Positional four-argument call.** `UndoCenterScreen(e, b, u, c)` no longer compiles (user-mandated placement of `groups` and `onUndoGroup` before the trailing `onClear`). Trailing-lambda and fully named call sites compile; SecondBrain calls it fully named.
2. **English-only count and status text.** Under the signed `UndoGroupRowUiModel` shape the member count and status words (`1 item`/`n items`, `Undoable`, `Partly resolved`, `Resolved`) are English-only library text; only the Undo all label is caller-overridable. `Resolved` is used rather than `Undone` because `FullyResolved` also covers failed members (their rows already read `Couldn't undo`). Adding defaulted `countLabel` and `statusLabel` fields later is source-compatible but deviates from the signed shape, so it needs approval.
3. **Undo all visibility rule.** Shown only when `onUndoGroup != null` and the status is Undoable or PartiallyResolved (planner decision).
4. **A1 (23-01).** A saved-key summary without `actions` shows no buttons and offers no way out.
5. **A2 (23-01).** Dropdown marker `Label · saved` on menu rows only; the anchor stays plain.
6. **A3 (23-01).** Button emphasis: Save Filled, Update Tonal, Clear Text (error hue), Cancel Text.
7. **Group tags.** `undo-group-header-{id}` and `undo-group-undo-all-{id}` are kebab-case internal constants and not Y-14 tags; Phase 24 decides whether to document them.
8. **Indent and header styling.** Members are inset by `Dimens.HorizontalPadding`; the header uses `surfaceContainer`, `titleSmall` and `bodySmall` hub tokens. Visual only, structurally proven; the device look is a Gate-1/Gate-2 item.
9. **TalkBack.** The read-out of the group header (heading) and of the card summary is not visible to `uiautomator`; it is a Gate-2 owner item.
10. **SecondBrain repin.** The consumer must map store groups to `UndoGroupRowUiModel` (from `group`, `groupLabel`, `groupStatus`) on every recomposition and wire `onUndoGroup` to `attemptUndoGroup`. Stale member ids are safe (ignored).

## Gate-1 handoff

`phase.has-uat-criteria 23` resolves `true` (binding, no `device_exempt`). This plan neither waived it, edited the ROADMAP criteria, wrote a SELF-UAT, wrote under `.planning/uat-pending` nor drove a device. The execute-stage agentic tester owns the steps below.

**(a) Preconditions.** Device `R5CT10XNKQN` only, always `adb -s R5CT10XNKQN`; preflight per `AGENT-DEVICE-TESTING.md` D1 (awake, unlocked; not-ready after one retry is INFRA, not FAIL); the tester screen is 384dp wide natively, so pin probes to `Modifier.width(360.dp)` for the German checks; never the personal phone.

**(b) Build identity.** Delete the stale mavenLocal AAR directories (the current library version and any `-v251` baseline copy) and republish with `clean publishReleasePublicationToMavenLocal --no-daemon -q`; record the AAR md5 and the HEAD short sha; confirm `classes.jar` contains `SavedKeyUiModel`, `ProviderKeyActions`, `ProviderKeyLabels`, `ProviderKeyDefaults` and `UndoGroupRowUiModel`.

**(c) Throwaway harness** (session scratchpad, never committed): a `com.android.application` with `mavenLocal()` first, a `MainActivity` that same-package-Intents into `ExplorerActivity`, and an exported throwaway `ProbeActivity` hosting (i) a stateful wrapper around `ProviderKeyCard` that owns the selected provider, the typed text, `editingSavedKey`, `inFlight` and per-action fire counters rendered into visible text, and (ii) `UndoCenterScreen` driven by a real `UndoHistoryStore` with `openGroup` and grouped appends plus ungrouped appends, mapping store groups to `UndoGroupRowUiModel` and wiring `onUndoGroup` to `attemptUndoGroup`.

**(d) Default A/B.** Build the same harness against a real v2.5.1 AAR (`git archive` of the `v2.5.1` tag, published under a second version and applicationId) and diff screenshots of the existing Explorer demos in both builds: the Voice Command ProviderKeyCard variants and the Feedback Undo Center default (flat) list; expect `diff bbox None, sum 0` below the status bar.

**(e) Probe cases.** ProviderKeyCard (three providers: A saved, B unsaved, C saved):
1. A selected and saved shows the check icon and the summary `Saved`, a middle dot, the short fingerprint (`sk-ant-` ellipsis `abcd`), then Update and Clear, and no key field.
2. Tap Update shows the field with Save and Cancel; Cancel returns to the summary; Save fires once.
3. Tap Clear fires onClear once (the wrapper then drops the saved key and the field with Save appears).
4. B selected shows the field with Save and no Cancel; with `onCancelEdit` set it shows Cancel.
5. Switch A to B to C through the dropdown: each shows its own summary or field, the dropdown rows for A and C carry the saved marker and B does not, the anchor keeps the plain label.
6. `inFlight = true`: every action is visibly disabled and taps fire nothing (counters stay).
7. `saveEnabled = false` with an empty field: Save disabled.
8. Type a distinctive raw string into B's field, switch to a saved provider: the summary does not show it and a `uiautomator dump` of the screen contains no occurrence of it (device proof of T-23-01).
9. `actions = null` with a saved key: summary only, no buttons.
10. German labels at the 360dp pin (Speichern, Aktualisieren, Entfernen, Abbrechen, Gespeichert): nothing clipped or off-screen.
11. The default card (empty `savedKeys`, `actions = null`) equals v2.5.1 (the A/B of (d)).

Undo Center:
12. One group of three entries plus two ungrouped: the header shows the label, `3 items`, a middle dot, `Undoable`, and Undo all; members are indented under it; ungrouped entries stay flat.
13. Tap Undo all: `onUndoGroup` fires once with the group id; through the real store the members read Undone, the header reads `Resolved` and Undo all disappears.
14. Undo one member first: header `Partly resolved` and Undo all still present.
15. A refusing group undo leaves members Available and Undo all present.
16. Hold-to-peek works on a member row and does nothing on the header.
17. `onUndoGroup = null`: no Undo all.
18. German `Alle rückgängig machen` at the 360dp pin: button inside the window, label unclipped, text column not overlapped.
19. The default Undo Center (empty `groups`) equals v2.5.1 (the A/B of (d)).
20. The top-bar Clear history still appears only when a spent entry exists.

**(f) Evidence** only through `gsd_run uat-run start --phase 23 --plan 02 --surface android --device R5CT10XNKQN --package <harness id> --pick run_id` (the plan argument is the BARE number 02, never 23-02), `uat-run capture --run "$RUN" --label <lowercase-slug> --kind ui|screen|log` (labels are lowercase slugs), `uat-run finish --run "$RUN" --pick selfuat_frontmatter`, then `uat-run verify`, with `--allow-empty` for the logcat FATAL check, `evidence_schema: 3`; the SELF-UAT is named `23-02-SELF-UAT.md` and is produced by the dispatched tester agent, not by this plan.

**(g) Known limits** to record, not fail: dynamic color is on in `ExplorerActivity` and the probe, so hub-palette hex claims (button hues for Destructive, Save, Tonal) are Robolectric or source-contract proofs only and the device proves geometry, enabled state and that it reads the active scheme; the TalkBack read-out of the summary node and of the group header, and any `stateDescription`-style strings, are not visible to `uiautomator` and are a Gate-2 owner item; the Samsung S Pen overlay appears in screenshots; reset any changed device setting and uninstall both harness packages.

## Deferred to Phase 24

- Explorer demos: ProviderKeyCard lifecycle with 3 providers (one saved, edit and clear flows) and an Undo Center group row, with registry state cells and TESTER screencaps (DEMO-01, Y-15).
- API.md stable-tag section entries for the five `provider_key_*` tags.
- INTEGRATION.md snippets: CalTracker deleting its dead dropdown and own Save rows; SecondBrain mapping store groups, including the positional-call note.
- README.md and ComponentRegistry KDoc counts.
- Folding the two v2.5.1 canary classes (`ProviderKeyCardSourceCompatV251Test`, `UndoCenterScreenSourceCompatV251Test`) into the Phase 24 call-site canary.
- Whether to fold the new feedback and model files into the import litmus of `ToneFoundationInvariantsTest`.
- Phase 24's "no public symbol removed" check must accept the old-arity rewrites of `ProviderKeyCard` and `UndoCenterScreen` (a literal append-only diff is impossible, sketch P2).

`23-VALIDATION.md` was not edited.

## Post-merge actions

- Run one full `./gradlew testDebugUnitTest` in the main checkout (registry and vocabulary drift guards resolve to the main checkout, see the worktree note above).
- No tag, no push, no consumer edit was made; tagging, push and the SecondBrain repin remain human-gated.

## Self-Check: PASSED

- Files exist: `UndoGroupRowUiModel.kt`, the modified `UndoCenterScreen.kt` and `api.txt`, and the six new test classes (all committed).
- Commits exist: 9f83754, 06cc1e2, 6b3890e, 6c5286d.
- `STATE.md` and `ROADMAP.md` not modified; no `*-SELF-UAT.md` created; nothing staged under `.planning/graphs`, `.planning/uat-pending` or `.planning/WAITING.json`.
