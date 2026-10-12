---
phase: 22-outcomesheet-receipt-style
plan: 05
subsystem: ui
tags: [compose, outcome-sheet, receipt, failure, tag-matrix, phase-gate, gate-1-handoff]

requires:
  - phase: 22-outcomesheet-receipt-style
    provides: "Receipt frame and bar (22-02), Classic deltas and the shared undo block (22-03), Receipt Success and ReceiptActionRow (22-04)"
provides:
  - "Receipt Failure arm: title row, transcript, handled-by pill, ToneCard (reason + body), tip, shared undo block, pinned bar"
  - "internal failureToneColors seam; private ReceiptFailureBar and receiptHasBar"
  - "Y-14 tag matrix test across every Receipt state; final Receipt source contracts"
  - "Phase 22 gate evidence and the Gate-1 handoff"
affects: [24-docs, gate-1-tester]

requirements-completed: [VRCPT-04, VTAG-01, VXR-01, VXR-02]

actuals:
  tokens: 14000
  tasks: 3
  commits: 2
plan_head_before: 1bf8b12c101e26c276ab50f118b4fe70b04d8b9f

key-files:
  created:
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceiptFailureTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceiptTagsTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceipt.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceiptBarGeometryTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceiptLongLabelTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceiptSourceContractTest.kt

key-decisions:
  - "OQ-3: a Failure with neither action nor secondaryAction renders no bar and no divider; the scroll body then has 16dp bottom padding (receiptHasBar predicate)"
  - "The Receipt Failure puts the handled-by pill between the transcript and the card (assumption A6)"
  - "No fixture change was needed: the 22-03 OutcomeFixtures.failure builder already took every Receipt field"

coverage:
  - id: D1
    description: "Failure sections render title, transcript, pill, card, tip, undo, bar in that vertical order; the card holds the reason and the body only; the actions sit in the bar with the secondary left of the action"
    requirement: VRCPT-04
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceiptFailureTest.kt#Failure sections render in the D-09 order"
        status: pass
    human_judgment: false
  - id: D2
    description: "Warn paints warnTone and Error errorTone, proven through failureToneColors and a LocalContentColor capture with distinctive custom colors; the caller tag outcome_sheet_failure_surface wins over tone_card; semanticsPrefix merges on the card while the action stays its own node"
    requirement: VRCPT-04
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceiptFailureTest.kt"
        status: pass
    human_judgment: false
  - id: D3
    description: "384dp edges (secondary start 16dp, action end 368dp), 240dp over-long body keeps both buttons displayed and at least 40dp tall, long German Failure labels at 360dp NATIVE stay in the window unellipsized"
    requirement: VRCPT-04
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceiptBarGeometryTest.kt"
        status: pass
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceiptLongLabelTest.kt"
        status: pass
    human_judgment: false
  - id: D4
    description: "Y-14 tag matrix: every applicable stable tag present with the exact count and every other stable tag absent in nine Receipt states (confirm single and batch, success single, batch and all-undone, failure error, warn and without actions) plus Classic has no action bar"
    requirement: VTAG-01
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceiptTagsTest.kt"
        status: pass
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/ToneFoundationInvariantsTest.kt"
        status: pass
    human_judgment: false
  - id: D5
    description: "Refused surface in Receipt Failure is the single Assertive live region; none without a refusal; Failure.undo renders through the shared block with undo-all firing once and rows in list order"
    requirement: VXR-01
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceiptFailureTest.kt"
        status: pass
    human_judgment: true
  - id: D6
    description: "Failure.undo renders in both styles through one shared UndoAffordanceBody (Receipt: locked = false, after the tip, before the bar)"
    requirement: VXR-02
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceiptSourceContractTest.kt#the Failure body is a ToneCard with the body in the body medium style and the undo after the tip"
        status: pass
    human_judgment: false

duration: 30min
completed: 2026-10-12
status: complete
---

# Phase 22 Plan 05: Receipt Failure arm, tag matrix and phase gate Summary

**The Receipt `Failure` arm now renders a title row, transcript and pill outside a `ToneCard` that holds the reason and body, then the tip, the shared undo block and a pinned bar (secondary at the start, role-hued Filled action at the end); the Y-14 tag matrix is proven across nine Receipt states, the full suite (1136 tests), detekt, apiCheck and run-all are green, and `api.txt` is byte-identical after `apiDump`.**

## Accomplishments

- `failureToneColors(tone, colors)` (internal seam): Warn -> `colors.warnTone`, otherwise `colors.errorTone`.
- `ReceiptFailureBody`: `ReceiptTitleRow` (only when a title or badge exists), transcript (non-blank only), handled-by pill, `ToneCard` tagged `outcome_sheet_failure_surface` (the caller tag wins over `tone_card`) with the Classic `semanticsPrefix` merge, reason (bodyMedium, SemiBold, onSurface) and the body in `ProvideTextStyle(bodyMedium)`; then the tip tagged `outcome_sheet_failure_tip`, then `UndoAffordanceBody(undo, locked = false)`.
- `ReceiptFailureBar`: `ReceiptActionBar` with the secondary action at the START (`outcome_sheet_action_start`, own emphasis, role, icon and enabled) and the action at the END (`outcome_sheet_action_end`, Filled, `role = it.role`, `icon = it.icon`). No bar and no divider when both are absent (OQ-3).
- `receiptHasBar(outcome)` is the one predicate that decides the scroll body's bottom padding (0dp when a bar follows, 16dp otherwise).
- Tests: `OutcomeSheetReceiptFailureTest` (19), `OutcomeSheetReceiptTagsTest` (9), `OutcomeSheetReceiptBarGeometryTest` (6, one new), `OutcomeSheetReceiptLongLabelTest` (7, one new), `OutcomeSheetReceiptSourceContractTest` (17, five new); all 0 failures.

## Task Commits

| Task | Commit | Lane | Message |
| ---- | ------ | ---- | ------- |
| 1 (tracer) | e06c7a8 | LANE 2 (cumulative vs v2.5.1), committed with `HUB_LANE_OVERRIDE=2` | feat(22-05): Receipt Failure card, tip, undo and pinned bar |
| 2 | 69d480e | LANE 1 | test(22-05): Receipt tag matrix, Failure live region and long German bar |
| 3 | (this SUMMARY commit) | docs only | docs(22-05): complete the Receipt OutcomeSheet phase and record the Gate-1 handoff |

Why task 1 was LANE 2: the plan expected LANE 1 (the Receipt file did not exist at v2.5.1), but the classifier compares the whole working tree with the baseline and the staged `OutcomeSheetReceipt.kt` change was classified LANE 2 (`LANE 2 (mode=additive, baseline=v2.5.1)`), blocking the commit until `HUB_LANE_OVERRIDE=2` was declared. This plan edited no line that exists in v2.5.1 (the diff is a new file's edits plus new tests), so the override is the same cumulative-classification effect seen in 22-02 to 22-04. The tracer feedback gate: Task 1's verify (Failure, geometry, Success, NeedsConfirmation, ToneCard and Classic tests) re-ran end to end green before Task 2 expanded.

## Phase roll-up (plans 22-01 to 22-05)

| Plan | Outcome | Commits (lane) |
| ---- | ------- | -------------- |
| 22-01 | Public types and widening: `OutcomeBadge`, `FailureTone`, `SheetActionUiModel`, widened `Success` / `Failure` / `NeedsConfirmation` / `HandledByUiModel` / `FailureActionUiModel`, `OutcomeSheetStyle`, `OutcomeSheetColors`, `OutcomeSheetDefaults`, `Dimens.OutcomeReceipt`, v2.5.1 call-shape canary | 51c905f (1), 3f58240 (2, override), 4ab499f (1) |
| 22-02 | `OutcomeSheet(style, colors)` dispatch, Receipt frame, `NeedsConfirmation` pinned bar, P7 escape hatch | c483729 (2, override), 658de68 (2, override), 4f17851 (1), 30f3d65 (1) |
| 22-03 | Classic deltas: confirm enabled, Warn tone, `Failure.undo`, Assertive live region; Classic parity inventories | c1d3cd5 (2, override), bdb9530 (1) |
| 22-04 | Receipt Success: pill, shared batch and undo blocks, in-flight actions row | 5ebd9a2 (2, override), 61c7173 (1) |
| 22-05 | Receipt Failure, tag matrix, phase gate | e06c7a8 (2, override), 69d480e (1) |

`HUB_LANE_OVERRIDE=2` commits: 3f58240, c483729, 658de68, c1d3cd5, 5ebd9a2, e06c7a8. Every one only touches lines that the audits below name.

### Requirement coverage

| Requirement | Plan | Proving test |
| ----------- | ---- | ------------ |
| VRCPT-01 | 22-02 | `OutcomeSheetReceiptSourceContractTest` (dispatch, 24dp corners), `OutcomeSheetReceiptFrameTest` |
| VRCPT-02 | 22-02 | `OutcomeSheetReceiptNeedsConfirmationTest`, `OutcomeSheetReceiptBarGeometryTest` |
| VRCPT-03 | 22-04 | `OutcomeSheetReceiptSuccessTest` |
| VRCPT-04 | 22-03, 22-05 | `OutcomeSheetReceiptFailureTest`, `OutcomeSheetClassicDeltasTest` (Classic Warn) |
| VRCPT-05 | 22-04 | `OutcomeSheetReceiptSuccessTest` (pill) |
| VTAG-01 | 22-05 | `OutcomeSheetReceiptTagsTest`, `ToneFoundationInvariantsTest` |
| VXR-01 | 22-03, 22-04, 22-05 | `OutcomeSheetLiveRegionTest`, Success and Failure live-region tests |
| VXR-02 | 22-03, 22-04, 22-05 | shared `UndoAffordanceBody` in Classic, Receipt Success and Receipt Failure |

## Phase gate outputs

- `./gradlew apiDump testDebugUnitTest detekt apiCheck` (full unfiltered suite, one invocation): BUILD SUCCESSFUL in 52s. Summed from the result XML: **1136 tests, 22 skipped (pre-existing), 0 failures, 0 errors**. detekt: zero findings at the zero baseline. apiCheck passed.
- `git diff --exit-code -- api.txt` after the dump: exit 0 (the committed `api.txt` equals a fresh dump). No `chore(22-05): refresh api.txt` commit was needed. `api.txt` was byte-identical after `apiDump` in both task verifies too.
- `bash tools/test/run-all.sh`: all four fixture scripts `PASS`, none `FAIL` (classify 12/12, precommit hook 6/6, additive diff 5/5, binary ABI 52/52). (The hook script prints a deliberate BLOCKED line for its own negative case; it is expected.)
- Protected tests vs v2.5.1 (`OutcomeSheetTest`, `FailureRoleSourceContractTest`, `DataClassBinaryCompatShimTest`): `git diff --exit-code v2.5.1 --` exit 0, byte-identical.
- `git diff --exit-code v2.5.1 -- config/detekt-baseline.xml config/detekt/detekt.yml config/detekt-compose.yml`: exit 0.
- `git log v2.5.1..HEAD --oneline -S"DeprecationLevel.HIDDEN" -- src/main`: prints nothing.
- `git tag --list "v2.6*"`: prints nothing. Nothing pushed.
- `phase.has-uat-criteria 22 --pick has_uat_criteria`: `true`.

## Audits

### `git diff --numstat v2.5.1 -- api.txt`

```
255	14	api.txt
```

### Removed lines of `git diff -U0 v2.5.1 -- api.txt` (14)

Three older-phase widenings (Phase 20 and 21) plus the eleven Phase 22 members; each removed line is an old signature with a same-member added counterpart:

1. `DynamicActionButton(label, role, onClick, enabled, modifier)` (Phase 20, gained `emphasis`, `icon`)
2. `SegmentedOptionSelector(... enabled, disabledReason)` (Phase 21, gained `disabledOptions`, `style`)
3. `SheetScaffold(... contentWindowInsets, content)` (Phase 21, gained `shape`)
4. `OutcomeSheet(outcome, onDismissRequest, modifier)` (gained `style`, `colors`)
5. `FailureActionUiModel(label, onClick, role)` constructor
6. `FailureActionUiModel.copy(label, onClick, role)`
7. `HandledByUiModel.copy(tier, approach, provider, model, escalationCount, escalationsLabel)`
8. `Failure.copy(reason, handledBy, action, body, semanticsPrefix)`
9. `NeedsConfirmation` constructor (old 11 parameters)
10. `NeedsConfirmation.component10()` (old `onConfirm`; now `confirmEnabled`)
11. `NeedsConfirmation.component11()` (old `onCancel`; now the nullable `titleActions` slot)
12. `NeedsConfirmation.copy(...)`
13. `Success` constructor
14. `Success.copy(...)`

Cumulative 14 matches the research estimate (3 from Phase 21 plus 11 from Phase 22). Note for the owner: the shifted `componentN` indices of `NeedsConfirmation` only matter to a consumer that destructures it (none known); the `copy` and constructor call shapes stay source-compatible, proven by `OutcomeSheetSourceCompatV251Test`.

### `OutcomeSheet.kt` removed lines against v2.5.1 (10, all enumerated Classic deltas)

1. the `modifier: Modifier = Modifier` line of `OutcomeSheet` (gained a trailing comma for `style`, `colors`)
2. the `OutcomeSheetContent(outcome)` call
3. the `OutcomeSheetContent(outcome, modifier)` signature line (now multi-line with `colors`)
4. the Failure arm `is Failure -> FailureBody(outcome)`
5. `private fun UndoAffordanceBody(...)` declaration (now `internal`)
6. `private fun BatchResultsList(...)` declaration (now `internal`)
7. `private fun FailureBody(failure)` signature (now takes `colors`)
8. `color = MaterialTheme.colorScheme.errorContainer,`
9. `contentColor = MaterialTheme.colorScheme.onErrorContainer,`
10. `private fun ProposedItemRow(...)` declaration (now `internal`)

This plan itself did not touch `OutcomeSheet.kt`; the list is identical to the end of 22-04 plus 22-03's two color lines and `ProposedItemRow`, giving the ten expected.

### Classifier and lane log

`bash tools/classify-hub-change.sh --baseline v2.5.1` (cumulative, informational): the commit-time output was `LANE 2` for task 1 and `LANE 1` for task 2; LANE 2 is expected cumulatively. `git log --oneline v2.5.1..HEAD` shows the five phase 22 plans' commits above plus the merge and tracking commits of the orchestrator.

### Arity sets observed in 22-01 (A2)

`HandledByUiModel` constructors contain {5, 6}, copy {5, 8}; `FailureActionUiModel` constructors and copy {2, 4}; `Failure` constructors {1, 2, 3, 4, 5} (unchanged), copy {3, 12}. All hand-derived sets matched observation.

### Carried-over flags

- **P7: escape hatch used (22-02).** The literal `Spacer(weight 1f)` row failed the long German bar test, so `ReceiptActionRow` uses two `weight(1f, fill = false)` slots. In 22-05 the German Failure bar passed on the first run, so no further fix was needed.
- **A3: confirmed.** `layoutInput.style` of the reason text (bodyMedium font size, SemiBold, onSurface) is readable here and asserted in `OutcomeSheetReceiptFailureTest`; no fallback to the source contract.
- **22-03 reading, restated:** Classic ignores the Receipt-only Failure fields `title`, `transcript`, `tip`, `secondaryAction` and `badge` (not only the four D-06 fields). If the owner wants Classic to render any of them, that is a Classic change.

## Worktree note on the drift guards (flag for the orchestrator)

This plan ran in the `agent-*` worktree `agent-a8ca141674a5f9630`. `ComponentRegistryDriftGuardTest` and `DomainVocabularyDriftGuardTest` walk up to the first directory named `yahirandroidtaste`, which is the MAIN checkout, so their green result here says nothing certain about this worktree's sources. This plan adds no public top-level composable or public symbol (everything new is `internal` or `private`), so no registry or vocabulary allowlist edit is needed. **One full `./gradlew testDebugUnitTest` in the main checkout after merge is the real confirmation** for those two guards (and for `GeneratedSymbolDriftGuardTest` and `ComponentPlaygroundIntegrityTest` if they resolve the repo root the same way).

## Owner flags

- **OQ-1.** `editableContent` is hidden while `inFlight` in Receipt, following the model KDoc and Classic. If CalTracker needs its receipt body to stay visible during the write, it is a one-line change plus a KDoc edit, diverging from Classic. Revisit with CT if the blank flash is unwanted.
- **OQ-2.** The handled-by pill appends the escalation caption (`"{escalationsLabel} {count}"`) when `escalationCount` is set, joined by " · ", mirroring Classic. Dropping it is a one-line change in `handledBySecondaryParts`.
- **OQ-3.** A Failure without actions renders no bar and no divider (the scroll body gets 16dp bottom padding); the Success actions row is not tagged `outcome_sheet_action_bar` and has no divider.
- **OQ-4.** No Explorer Receipt demo in Phase 22 (Gate-1 uses a throwaway ProbeActivity).
- **Classic ignores the Receipt-only Failure fields** (see above).
- **Failure pill order (A6).** The handled-by pill sits between the transcript and the card in Receipt Failure; CalTracker passes `handledBy = null` on Failure, so the order is low risk.
- **Cosmetic spacing choices** for the Phase 24 screencap review: the card has `Dimens.ContentSpacing` (4dp) above it; the tip also 4dp above; the undo block pads its own top; the Success actions row has 24dp above and 16dp below plus the 16dp scroll padding (32dp total under the actions). All trivial to tune.
- **A9.** The Assertive live region is proven structurally (semantics key and value, exactly one node). What TalkBack actually speaks is a Gate-2 owner item.

## Gate-1 handoff

`gsd-tools query phase.has-uat-criteria 22 --pick has_uat_criteria` prints **`true`** (5 criteria, binding). `22-CONTEXT.md` carries no `device_exempt` line (count 0). This plan did not waive Gate-1, edit a ROADMAP criterion, write any `*-SELF-UAT.md`, write under `.planning/uat-pending`, or touch a device. The orchestrator's `verify_work_agentic_gate` and the dedicated tester own it; the spec-less probe fallback was skipped visibly in this unattended run.

For the dedicated tester:

**(a) Preconditions.** Device `R5CT10XNKQN` only, always `adb -s R5CT10XNKQN`. Preflight per `AGENT-DEVICE-TESTING.md` D1 (awake, unlocked; not-ready after one retry is INFRA, not FAIL). The tester screen is 384dp wide natively, so the 16dp / 368dp edge claim is measurable at native width. Pin the probes to `Modifier.width(360.dp)` for the German checks. Never the personal phone.

**(b) Build identity.** Delete the stale mavenLocal AAR directories (`1.10.0` and `1.10.0-v251`) and republish with `clean publishReleasePublicationToMavenLocal --no-daemon -q`. Record the AAR md5 and the HEAD short sha. Confirm `classes.jar` contains `OutcomeSheetDefaults` and `OutcomeSheetStyle`.

**(c) Throwaway harness** (session scratchpad, never committed): a `com.android.application` with `mavenLocal()` first, a `MainActivity` that same-package-Intents into `ExplorerActivity`, and an exported throwaway `ProbeActivity` rendering `OutcomeSheet` states through the public API, with the consumer-side resource-id exposure flag set in the harness (consumer-owned, Y-14).

**(d) Classic A/B.** Build the same harness against a real v2.5.1 AAR (`git archive b6c363c`, published as `1.10.0-v251`, a second applicationId) and diff screenshots of the existing Voice Command `OutcomeSheetVariants` demos in both builds: summary, handled-by, failure no action, failure with action, undo, undo unavailable, undo refused, confirm single, batch, trailing, tall. Phase 21 reported `diff bbox None, sum 0` for the analogous check.

**(e) Receipt probe cases (ProbeActivity).**
1. Confirm single and batch, each with `confirmEnabled` true and false (a disabled confirm never fires).
2. A tall `topLevelContent` (the bar stays on screen).
3. Success single, batch and all-undone with `inFlight = true` and one `activeWhileInFlight` action.
4. The handled-by pill with `onDevice = true`.
5. Failure error and warn, each with a tip, an `action` with an `icon` (mic-style Retry), a `secondaryAction` (Close) and an `undo` carrying `refused`; also a Failure with no actions (no bar, no divider).
6. German strings at the 360dp pin for the bar and the pill (Failure bar: "Erneut versuchen und Aufnahme wiederholen" and "Schließen und Vorgang abbrechen").
7. The 24dp top-corner extent against the default sheet (Phase 21 measured 23.8dp default against 19.9dp shaped at the same threshold).

**(f) Evidence** only through `gsd_run uat-run start --phase 22 --plan 05 --surface android --device R5CT10XNKQN --package <harness id> --pick run_id`, `uat-run capture --run "$RUN" --label <slug> --kind ui|screen|log`, `uat-run finish --run "$RUN" --pick selfuat_frontmatter`, then `uat-run verify`, with `--allow-empty` for the logcat FATAL check, `evidence_schema: 3`. The SELF-UAT is named `22-05-SELF-UAT.md` and is produced by the dispatched tester agent, not by this plan.

**(g) Known limits to record, not fail.** Dynamic color is on in `ExplorerActivity` and the probe, so hub-palette hex claims are Robolectric-only (the device proves geometry, alpha and that it reads the active scheme). The live-region mode and the `stateDescription`-style strings are not visible to `uiautomator`, so the TalkBack read-out of the Assertive refused surface is a Gate-2 owner item (A9). The Samsung S Pen overlay appears in screenshots. Reset any changed device setting (animator scale) and uninstall both harness packages.

## Deviations from Plan

**1. [Plan expectation] Task 1 classified LANE 2, not LANE 1.** See Task Commits. Declared `HUB_LANE_OVERRIDE=2`; no pre-existing v2.5.1 line was edited by this plan.

**2. [Rule 1] One of my new source-contract assertions was wrong on first run.** `receiptHasBar` is expression-bodied, so the brace-based `functionBody` extractor threw; the assertion now reads the compacted file text (the same approach 22-04 used for `actionEnabled`). No production change.

**3. [Plan wording] `OutcomeFixtures` unchanged.** The plan listed the fixtures file as modified, but the 22-03 `failure(...)` builder already took `title`, `transcript`, `tone`, `badge`, `handledBy`, `body`, `tip`, `undo`, `action` and `secondaryAction`, and `sheetAction(...)` and `undo(...)` existed, so no fixture edit was required.

**4. [Process] Red step not run separately** (tests and implementation written, then one gradle run; one gradle at a time).

**5. [Tooling] Commit ledger not persisted.** The sandbox refuses compound `git` redirects, so no ledger file was written. `plan_head_before` is the worktree base 1bf8b12 and `commits: 2` was measured with `git rev-list --count 1bf8b12..HEAD` before this SUMMARY commit.

**Total deviations:** none changing behavior or scope.

## Deferred to Phase 24

- `README.md`, `API.md` and the `ComponentRegistry` KDoc counts (no test enforces them).
- The `API.md` stable-tag section for every Y-14 tag (`outcome_sheet_title`, `_badge`, `_title_actions`, `_transcript`, `_action_bar`, `_action_start`, `_action_end`, `_failure_surface`, `_failure_tip`, `_handled_by`, `_confirmation_cancel`, `_confirmation_confirm`, plus the existing item, remove, top-level-content, batch-row and undo tags) and the five `provider_key_*` tags. The two test-only hooks `outcome_sheet_action_bar_divider` and `outcome_sheet_handled_by_dot` stay out.
- The Explorer Receipt demos and the TESTER screencap review (DEMO-01, Y-15).
- Whether to fold `component/OutcomeSheetReceipt.kt` and `component/OutcomeSheetDefaults.kt` into the permanent `ToneFoundationInvariantsTest` list (done in 22-02).
- The v2.5.1 call-site canary set (`OutcomeSheetSourceCompatV251Test`).
- Phase 24's "no public symbol removed" check must accept the old-arity rewrites of the six widened members (a literal append-only diff is impossible, sketch P2).
- One full `./gradlew testDebugUnitTest` in the main checkout after the merge (worktree drift-guard note above).

## Self-Check: PASSED

- Files exist: `OutcomeSheetReceiptFailureTest.kt`, `OutcomeSheetReceiptTagsTest.kt`, and the modified `OutcomeSheetReceipt.kt`, `OutcomeSheetReceiptBarGeometryTest.kt`, `OutcomeSheetReceiptLongLabelTest.kt`, `OutcomeSheetReceiptSourceContractTest.kt`.
- Commits e06c7a8 and 69d480e exist on `worktree-agent-a8ca141674a5f9630`.
- STATE.md and ROADMAP.md untouched; nothing pushed or tagged; no device touched; no gradle process left running; no `*-SELF-UAT.md` and nothing under `.planning/uat-pending` written.
