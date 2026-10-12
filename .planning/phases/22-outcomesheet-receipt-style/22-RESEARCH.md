# Phase 22: OutcomeSheet Receipt style - Research

**Researched:** 2026-10-12
**Domain:** Source-compatible widening of six public signatures in a Jetpack Compose design-system library, plus an opt-in Receipt rendering of `OutcomeSheet` (Material 3 `ModalBottomSheet`), proven with Robolectric structure/semantics/bounds only.
**Confidence:** HIGH on repo mechanics (everything below tagged `[VERIFIED]` was opened this session), MEDIUM on Compose text/layout behavior that could not be run (read-only session, no gradle) and on the exact `api.txt` removed-line count.

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

- **D-01 [signatures]:** The signatures are exactly those in `.planning/seams/v2.6.0-SKETCH.md` §2 Y-3..Y-7, plus the accepted delta `SheetActionUiModel.activeWhileInFlight: Boolean = false` (teamlead, 2026-10-11). Reversibility: one-way, signed off by CT and SB.
- **D-02 [compat]:** Source compatibility only (P1): no hidden 3-arg `OutcomeSheet` overload, no old-arity `copy`, no hidden ctors, no `copy$default` shims.
  - Data-class fields are appended with defaults.
  - `NeedsConfirmation`'s `confirmEnabled` and `titleActions` are inserted before `onConfirm`/`onCancel`. Both consumers use named args: SB `VoiceConfirmSheetModels.kt:29`, CT `VoiceOutcomeMapper.kt:315/435`.
  - The v2.5.0 shims already in the code stay as they are.
  - Regenerate `api.txt` with each API commit.
- **D-03 [new-enums]:** The new enums are `OutcomeSheetStyle { Classic, Receipt }`, `OutcomeBadge { Success, Reverted, Error, Warn }` and `FailureTone { Error, Warn }`. No existing enum gains constants.
- **D-04 [receipt-structure]:** Receipt rendering lives in new private composables in a new file (`OutcomeSheetReceipt.kt`), dispatched by `style`. The Classic arm composables stay byte-unchanged. Parity is proven structurally: the defaulted call runs the same code path, plus Robolectric state assertions (P3, no Roborazzi). _(ai-auto)_
- **D-05 [receipt-frame]:** The Receipt frame (Y-3):
  - `SheetScaffold(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))`.
  - The body has 16dp horizontal and 4dp top padding.
  - **Title row:** `Row(CenterVertically, spacedBy 8dp)` = optional 32dp badge -> title (`OutcomeSheetDefaults.ReceiptTitleStyle`: `titleLarge` 18/24 SemiBold, `heading()`, weight 1f, maxLines 2) -> `titleActions`.
  - **Transcript line:** `bodyMedium` italic `onSurfaceVariant`, 4dp top and 8dp bottom, shown iff a transcript or reason exists.
  - **Pinned bar** for NeedsConfirmation and Failure: a 1dp `outlineVariant` divider, then a `Row` with 16dp start/end, 12dp top and 16dp bottom padding: start Text action, `Spacer(weight 1f)`, end Filled. Keep the v2.4.1 `weight(1f, fill = false)` scroll body so the bar never starves.
  - **Success actions** sit at the end of the scroll content with 24dp above them, not pinned.
- **D-06 [classic-new-fields]:** Classic ignores `badge`, `titleActions` and `startAction`/`endAction`. It honors `FailureTone.Warn` (painting `colors.warnTone`) and `Failure.undo`. The default `Error` tone and `undo = null` change nothing. _(ai-auto)_
- **D-07 [confirm]:** While `confirmEnabled = false`, the confirm node is `enabled = false` and `onConfirm` never fires. In Receipt: `reason` renders as the transcript; the start action is `cancelLabel` (Text); the end action is `confirmLabel` (Filled; `severity` picks the hue). The KDoc says `items` may be empty when `topLevelContent` renders the rows.
- **D-08 [success]:** Receipt order: title row -> handled-by pill -> `editableContent` -> existing `batchResults`/`undo` -> actions row. `inFlight` disables every action except those with `activeWhileInFlight = true` (SB in-flight Cancel). `Reverted` uses `Icons.AutoMirrored.Filled.Undo`; `Warn` uses `PriorityHigh` with content and container swapped.
- **D-09 [failure]:** Receipt `Failure`: the title row sits outside the card; the card is a Phase 20 `ToneCard` (tone from `FailureTone` -> `colors.errorTone`/`colors.warnTone`) holding `reason` (`bodyMedium` SemiBold, `onSurface`) plus `body`; `tip` renders under the card; `action` moves to the end of the pinned bar (Filled, keeping its role hue, with optional leading `icon`); `secondaryAction` sits at the start.
- **D-10 [handled-by]:** The Receipt pill: `surfaceContainerHighest`, full radius, 4dp x 12dp padding, `bodySmall` `onSurfaceVariant`; a 6dp dot in `primary`, or `warnTone.content` when `onDevice`; then `"{prefixLabel} "`, the tier in SemiBold `onSurface`, and `" · {approach/provider/model joined}"`. Classic `HandledByRow` is unchanged.
- **D-11 [failure-undo-placement]:** In Receipt, `Failure.undo` renders in the scroll content below the ToneCard and tip, above the pinned bar, using the same undo block as Success. In Classic it renders below the error surface. _(ai-auto)_
- **D-12 [live-region]:** The refused/partial undo surface gets `semantics { liveRegion = LiveRegionMode.Assertive }`. Semantics only, with no visual change.
- **D-13 [tags]:** The Y-14 Receipt tags are exactly as listed in REQUIREMENTS VTAG-01. The Receipt NeedsConfirmation bar keeps `outcome_sheet_confirmation_cancel`/`_confirm`; Success and Failure use `outcome_sheet_action_start`/`_end`. Classic tags are unchanged. Never set `testTagsAsResourceId`.

### Claude's Discretion

- How style and colors thread internally (CompositionLocal vs params) and the private file split, as long as Classic stays byte-unchanged.

### Deferred Ideas (OUT OF SCOPE)

None. This phase stays within its scope. (Docs/README/API.md/ComponentRegistry-count updates, Explorer demos and TESTER screencaps are Phase 24's, per ROADMAP and STATE.)
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| VRCPT-01 | `OutcomeSheet(style, colors)`, `OutcomeSheetStyle`, `OutcomeSheetColors`, `OutcomeSheetDefaults.colors()/ReceiptTitleStyle`; Receipt frame (24dp corners, title row, transcript, pinned bar, Success actions at end, `weight(1f, fill=false)` body); Classic unchanged | §Widening mechanics, §Architecture (early-branch), §Receipt arms, §Pitfalls P1/P2/P6, plan 22-02 |
| VRCPT-02 | `NeedsConfirmation.confirmEnabled` / `titleActions` before `onConfirm`; disabled confirm never fires; Receipt reason-as-transcript, Text/Filled bar | §Widening mechanics (NC), Plan 22-02 (Receipt) + 22-03 (Classic `enabled`) |
| VRCPT-03 | `Success.badge/titleActions/startAction/endAction`, `OutcomeBadge`, `SheetActionUiModel(+activeWhileInFlight)`; Receipt order; inFlight rule | §Widening mechanics, plan 22-04, §Code examples (action enable formula, pure badge seams) |
| VRCPT-04 | `Failure.title/transcript/tone/tip/secondaryAction/badge`, `FailureTone`, `FailureActionUiModel.icon`; Receipt ToneCard; Classic honors Warn | §Widening mechanics, plan 22-03 (Classic Warn) + 22-05 (Receipt) |
| VRCPT-05 | `HandledByUiModel.prefixLabel/onDevice`; Receipt pill text "Handled by Local grammar · offline" | §Widening mechanics (HandledBy), plan 22-04, §Pitfalls P9 (single-Text pill) |
| VTAG-01 | Y-14 tags per Receipt state; confirm bar keeps `_confirmation_*`; Classic tags unchanged; no `testTagsAsResourceId` in `src/main` | §Tag matrix, plan 22-05 (`OutcomeSheetReceiptTagsTest`); the source grep is already enforced by `ToneFoundationInvariantsTest` |
| VXR-01 | Assertive live region on the refused/partial undo surface, no visual change | §Test strategy (liveRegion), plan 22-03 |
| VXR-02 | `Failure.undo: UndoAffordanceUiModel?` rendered by the Success undo block in both styles | §Widening mechanics (Failure), plans 22-03 (Classic) and 22-05 (Receipt) |
</phase_requirements>

## Project Constraints (from CLAUDE.md)

- Root `CLAUDE.md` is authoritative; `.claude/CLAUDE.md` only points to it. [VERIFIED: both read this session]
- **One-way dependency:** the library imports no consumer code, holds no secrets, names no app concepts; allowed deps are Android SDK, AndroidX/Compose, Hilt, Coil, navigation-compose, reorderable, osmdroid. The new file must import only `androidx.*`, `kotlin.*`, `java.*`, `android.*` and the hub itself.
- **`ComponentRegistry` is the single source of truth + drift guard:** every public top-level `@Composable` in `component/`, `feedback/`, `modifier/`, `theme/` is registered XOR allowlisted. This phase should add **no** new public top-level composable (everything Receipt is `private`/`internal`), so no registry or vocabulary edit is needed.
- **Source-compatible, not binary-compatible:** append params with defaults, keep trailing lambdas last, never append after a trailing lambda; do not add `@Deprecated(HIDDEN)` overloads or hand-written `copy$default`/default-ctor synthetics (the v2.5.0 shims stay). Metalava `apiCheck` is the per-commit gate; `tools/verify-binary-abi.sh` is informational.
- **Detekt zero baseline:** fix findings or tune with justification; never regenerate a baseline. Active rules: LongMethod (240), LongParameterList (18, defaults ignored), CyclomaticComplexMethod (26), naming, UnusedPrivateMember. [VERIFIED: `config/detekt-compose.yml`, `config/detekt/detekt.yml`]
- Gradle commands drop the module prefix: `./gradlew testDebugUnitTest`, `./gradlew detekt`, `./gradlew apiDump`, `./gradlew apiCheck`.
- **Shipping is human-gated:** no tag, no push, no repin, no consumer-repo edits. Commit on `main`. Tags are immutable JitPack coordinates.
- Session constraints (binding): owner asleep; exactly ONE gradle invocation at a time; no `captureToImage`; stage only each task's own files (never `git add .planning/` wholesale; never stage `.planning/graphs/*`, `.planning/uat-pending/16*`..`18*`, `.planning/WAITING.json`, `.planning/milestones/v2.5-phases/19-cut-v2-5-0/.gate/`); do not push or tag (`git.create_tag` is false).
- Memory lessons that bind this phase: long German/Spanish ~360dp check before layout sign-off (`test-localized-long-strings-before-signoff`); source compat not binary (`yat-source-compat-not-binary`); a tag push publishes every unpushed commit (`pushing-version-tag-publishes-unpushed-commits`), so never tag from this phase; Gate-1 device gaps are parked for Yahir, never waived (`certify-parks-agentic-gaps`).
- Commits that re-signature a composable or rewrite an existing `src/main` line are lane 2 in the pre-commit hook: declare with `HUB_LANE_OVERRIDE=2` and name the commit in the SUMMARY. [VERIFIED: `tools/hooks/pre-commit`, `tools/README-api-guard.md`; STATE.md "Carried from v2.5"]

## Summary

Everything the Receipt arms need already exists on `main`: `ToneColors`/`ToneDefaults.error()/warn()`, `ToneCard`, `DynamicActionButton(emphasis, icon)` with `ActionButtonEmphasis { Filled, Tonal, Text, TextSmall }`, `RoundIconButton`, `PillChip`, `SheetScaffold(shape)`, and `Dimens` token groups. Phase 22 adds no dependency and no new public composable. The work splits into three kinds: (1) pure API widening of five data classes plus one composable plus four new types (declarative, but it ripples into `api.txt` and into three reflection "arity pin" assertions that **will fail** after widening); (2) four small, deliberate Classic edits (honor `confirmEnabled`, honor `FailureTone.Warn`, render `Failure.undo`, add the Assertive live region) that must leave every other Classic line and every existing `OutcomeSheetTest` untouched; and (3) a new `OutcomeSheetReceipt.kt` with all Receipt rendering, reached by an early-return branch in `OutcomeSheet` (the Phase 20/21 "early branch above untouched legacy block" pattern).

The biggest planning risks are not rendering: (a) three existing assertions in `VoiceModelLabelDefaultsTest` pin `copy`/constructor arities that disappear when the data classes are widened without shims (D-02), so those assertions must be edited in the same commit as the widening; (b) the private Classic helpers (`UndoAffordanceBody`, `BatchResultsList`, `ProposedItemRow`) live in `OutcomeSheet.kt` and are invisible to the new file, so they must be made `internal` (a visibility rewrite, lane 2) rather than duplicated; (c) Classic `Failure` must keep `errorContainer`/`onErrorContainer` for the default tone, **not** `colors.errorTone` (whose content is `error`, a different color), or "renders exactly as v2.5.1" is false; (d) the generated `toString` of `Failure` would start printing the user's spoken `transcript` into logs unless overridden (mirror `NeedsConfirmation`'s privacy `toString`); (e) the Receipt pill must be a single `Text` with an `AnnotatedString` so its text reads "Handled by Local grammar · offline" as one string.

Recommended shape: five serial plans. 22-01 widens the models (API-only commits), 22-02 is the Receipt tracer (`OutcomeSheet(style, colors)` + frame + NeedsConfirmation end to end), 22-03 lands the Classic deltas with the parity proof, 22-04 adds Receipt Success + pill + badges, 22-05 adds Receipt Failure, the Y-14 tag matrix and the phase gate. Public-API churn is confined to 22-01 and 22-02; plans 22-03..22-05 must leave `api.txt` byte-identical after `apiDump`, which makes "each commit keeps apiCheck green" a crisp, checkable invariant.

**Primary recommendation:** Keep Classic as the existing code path with four named edits; add `OutcomeSheetReceipt.kt` with explicit `colors` params (no CompositionLocal), pure internal palette seams for anything Robolectric cannot pixel-read, and gate every plan on `apiDump` + `git diff --exit-code -- api.txt` (or the expected churn) + `apiCheck` + `detekt` + targeted tests, one gradle at a time.

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Outcome state model (`VoiceOutcomeUiState`, new fields) | Library model layer (`model/`) | Consumer (computes + hoists state) | Display state only; the library never decides success/failure (INV-01) |
| Receipt/Classic rendering and dispatch by `style` | Library UI (`component/`) | Consumer picks `style` and `colors` | Opt-in per call; default is Classic |
| Tone/warn/error palette | Library theme (`ToneDefaults`) | Consumer overrides via `OutcomeSheetColors` | CT passes app-local amber through `colors`; hub defaults to Material roles |
| Stable test tags | Library (documented API, Phase 24 docs) | Consumer turns on `testTagsAsResourceId` | The hub never sets the flag (enforced by `ToneFoundationInvariantsTest`) |
| In-flight / enabled gating | Consumer supplies `inFlight`, `confirmEnabled`, `enabled`, `activeWhileInFlight` | Library applies them to node `enabled` | Presentational; no inference from other fields |
| Persistence, undo execution, retry, network | Consumer | n/a | Out of scope (INV-01) |

## Standard Stack

### Core (all already in the build; nothing to install)

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Compose BOM / Material 3 | `composeBom = "2026.04.01"` [VERIFIED: `gradle/libs.versions.toml:12`] | `ModalBottomSheet`, `HorizontalDivider`, `Surface`, `ColorScheme.surfaceContainerHighest/outlineVariant` | Already the hub's UI base |
| `androidx.compose.material:material-icons-extended` | 1.7.8 in the Gradle cache [VERIFIED: cache path `material-icons-extended-android/1.7.8`; `build.gradle.kts:102`] | `Icons.AutoMirrored.Filled.Undo`, `Icons.Filled.PriorityHigh` | `classes.jar` lists `automirrored/filled/UndoKt.class` and `filled/PriorityHighKt.class` [VERIFIED: `unzip -l` this session]; `Check`/`Close` come from `material-icons-core` |
| Robolectric 4.16.1 + `compose-ui-test-junit4` | `robolectric = "4.16.1"` [VERIFIED: `libs.versions.toml:19`] | Semantics, bounds, click tests; `GraphicsMode.NATIVE` for real text metrics | The hub's existing harness |
| Metalava plugin | `me.tylerbwong.gradle.metalava` 0.5.0 [VERIFIED: `libs.versions.toml:46`] | `apiDump`/`apiCheck` | The per-commit API gate |
| Phase 20 atoms | in `main` | `ToneCard`, `DynamicActionButton(emphasis, icon)`, `RoundIconButton`, `PillChip` | Reuse, never redraw |
| Phase 21 | in `main` | `SheetScaffold(shape)`, `ReceiptRow` (consumer-side in `topLevelContent`) | `shape` is a param before the trailing `content` |

### Alternatives Considered

| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Explicit `colors` param threaded to private composables | `CompositionLocal` | A local would force re-indenting the Classic `SheetScaffold { }` call (lane-2 rewrite anyway) and hides state from the internal test hosts. Params keep test seams (`OutcomeSheetContent`, `OutcomeSheetReceiptContent`) trivial. **Use params.** |
| Early-return branch in `OutcomeSheet` | Branches inside each Classic arm | Early branch leaves Classic text intact (Phase 21-02 precedent) |
| `Spacer(weight 1f)` between two non-weighted actions (D-05 literal) | `Arrangement.SpaceBetween` + `weight(1f, fill=false)` on both | Identical for short labels; the second survives a long start label. Use the literal first, switch only if the German test fails (see P7) |

**Installation:** none. **Version verification:** no package is added; the `material-icons-extended` class check above is the only registry-adjacent fact and it was read from the local Gradle cache, not a registry.

## Package Legitimacy Audit

No external packages are installed or added in this phase (no `build.gradle.kts` or version-catalog change). `gsd_run query package-legitimacy check` is therefore not applicable.

**Packages removed due to [SLOP] verdict:** none
**Packages flagged as suspicious [SUS]:** none

## Current state of the code (read this session)

### `component/OutcomeSheet.kt` (463 lines) [VERIFIED: file read]

| Item | Line | Verbatim |
|------|------|----------|
| public entry | 62-66 | `fun OutcomeSheet(` / `outcome: VoiceOutcomeUiState,` / `onDismissRequest: () -> Unit,` / `modifier: Modifier = Modifier` |
| entry body | 67-69 | `SheetScaffold(onDismissRequest = onDismissRequest, modifier = modifier) {` / `OutcomeSheetContent(outcome)` |
| content seam | 92 | `internal fun OutcomeSheetContent(outcome: VoiceOutcomeUiState, modifier: Modifier = Modifier) {` |
| scroll body | 94-98 | `Column(` ... `.weight(1f, fill = false)` / `.verticalScroll(rememberScrollState())` |
| pinned footer | 109-113 | only `NeedsConfirmation` pins: `is VoiceOutcomeUiState.NeedsConfirmation -> NeedsConfirmationActions(outcome)` |
| private helpers | 126, 150, 207, 249, 292, 344, 373, 403, 433 | `private fun SuccessBody`, `UndoAffordanceBody(undo, locked)`, `UndoRowItem`, `BatchResultsList`, `FailureBody(failure)`, `HandledByRow`, `NeedsConfirmationBody`, `NeedsConfirmationActions`, `ProposedItemRow` |
| Failure colors | 294-296 | `color = MaterialTheme.colorScheme.errorContainer,` / `contentColor = MaterialTheme.colorScheme.onErrorContainer,` / `shape = MaterialTheme.expressive.cardShapeLarge,` |
| refused surface | 166-176 | `undo.refused?.let { refused ->` ... `Surface(` ... `.testTag("outcome_sheet_undo_refused")` |
| confirm button | 417-422 | `DynamicActionButton(label = confirmation.confirmLabel, role = confirmation.severity, onClick = confirmation.onConfirm, modifier = Modifier.testTag("outcome_sheet_confirmation_confirm"))`, no `enabled` argument |

**Existing Classic tags** (all stay, unchanged): `outcome_sheet_undo_all` (162), `outcome_sheet_undo_refused` (175), `outcome_sheet_undo_row` (228), `outcome_sheet_batch_row` (257), `outcome_sheet_failure_surface` (299), `outcome_sheet_action_button` (331), `outcome_sheet_handled_by` (348), `outcome_sheet_confirmation_top_level_content` (389), `outcome_sheet_confirmation_cancel` (415), `outcome_sheet_confirmation_confirm` (421), `outcome_sheet_confirmation_item` (442), `outcome_sheet_confirmation_item_remove` (456). [VERIFIED: grep + read]

### Models [VERIFIED: files read]

- `VoiceOutcomeUiState.kt:46-53` `Success(summary, handledBy = null, editableContent = null, inFlight = false, batchResults = emptyList(), undo = null)` (plain data class).
- `VoiceOutcomeUiState.kt:77-83` `data class Failure @JvmOverloads constructor(reason, handledBy = null, action = null, body = null, semanticsPrefix = null)`; v2.5.0 shims at 90-97 (hand-written 3-arg `copy`), 103-114 (hidden default ctor `(String, HandledByUiModel?, FailureActionUiModel?, Int, DefaultConstructorMarker?)`), 116-134 (private companion with `@JvmStatic @JvmName("copy\$default") legacyCopyDefault`). These stay.
- `VoiceOutcomeUiState.kt:175-187` `NeedsConfirmation(reason, items, selectionMode = AllOrNothing, title = null, severity = Neutral, reversibilityHint = null, confirmLabel = "Confirm", cancelLabel = "Cancel", topLevelContent = null, onConfirm, onCancel)` with a privacy `toString()` at 197-198 that omits `reason` and `title`.
- `HandledByUiModel.kt:27-34` `@JvmOverloads constructor(tier, approach, provider, model, escalationCount, escalationsLabel = "Escalations:")`; hand-written 5-arg `copy` (40-53); hidden ctor (62-77); `legacyCopyDefault` (79-100). These stay.
- `FailureActionUiModel.kt` `data class FailureActionUiModel(label, onClick, role = Neutral)` with an explicit 2-arg secondary constructor (line 34-35), hand-written 2-arg `copy` (42-43) and a `legacyCopyDefault` companion (49+). The 2-arg constructor and `copy` stay.
- `UndoAffordanceUiModel.kt:20-25` `(allLabel, rows = emptyList(), onUndoAll = null, refused = null)`.

### Existing tests that touch this surface [VERIFIED: files read]

| Test file | What it pins | Effect of Phase 22 |
|-----------|--------------|--------------------|
| `component/OutcomeSheetTest.kt` (1116 lines, ~57 tests) | Public `OutcomeSheet(outcome =, onDismissRequest =)` and `internal OutcomeSheetContent(outcome)`; tags above; tall NC keeps Confirm full-height in `Box(Modifier.height(240.dp))`; Failure body order via `topOf(tag)`; `semanticsPrefix` merge; role matrix | Must pass **unedited**. Keep `OutcomeSheetContent(outcome, modifier = Modifier, colors = ...)` source-compatible (new param last, defaulted) |
| `component/FailureRoleSourceContractTest.kt` | Extracts `"private fun FailureBody("` from `OutcomeSheet.kt`; body must contain `outcome_sheet_action_button` and `role = action.role` and must NOT contain the substring `Neutral` | `FailureBody` must stay `private fun FailureBody(` and its body must not mention `Neutral`. Adding a `colors` param is fine |
| `model/DataClassBinaryCompatShimTest.kt` | Reflective lookup of the hidden v2.4.1 default ctors and `copy$default` for `HandledByUiModel`, `Failure`, `FailureActionUiModel` etc. (header: "Deliberately no overload-count or exact-arity assertions") | Should keep passing unedited: hidden members stay and the new compiler synthetics have different descriptors |
| `component/VoiceI18nSourceCompatTest.kt` | v2.4 call shapes compile: `OutcomeSheet(VoiceOutcomeUiState.Failure("r", null), {}, Modifier)` (positional through `modifier`), 5-arg `HandledByUiModel.copy`, 3-arg `Failure.copy`, `FailureActionUiModel("l") { }` trailing lambda, `getConstructor(String, Function0)` | Keeps passing: the new params are appended after `modifier` and the 2-arg `FailureActionUiModel` ctor stays |
| `model/VoiceModelLabelDefaultsTest.kt` lines 167-170, 255-258, 286-289 | **Arity pins** (see Pitfall P3) | **WILL FAIL after widening; must be edited in the widening commit** |
| `explorer/DomainVocabularyDriftGuardTest.kt` | head-token allowlist for public top-level `@Composable`s; `"Outcome"` already in `PRIMITIVE_NOUN_ALLOWLIST` (line 329) | Unaffected if no new public composable is added |
| `component/ToneFoundationInvariantsTest.kt` | Scans all `src/main` `.kt` for the forbidden resource-id flag and Hilt host annotations; import-prefix and "no `Color(0x`" scans only for its listed `phaseFiles` | VTAG-01's "no `testTagsAsResourceId`" criterion is already enforced here; add the new file to a Phase 22 copy of the import scan |

## Widening mechanics (Q2)

### Target signatures (copy from the sketch, plus the accepted delta)

```kotlin
// model/OutcomeBadge.kt        public enum class OutcomeBadge { Success, Reverted, Error, Warn }
// model/FailureTone.kt         public enum class FailureTone { Error, Warn }
// model/SheetActionUiModel.kt  (new)
@Immutable
data class SheetActionUiModel(
    val label: String,
    val onClick: () -> Unit,
    val emphasis: ActionButtonEmphasis,
    val enabled: Boolean = true,
    val icon: ImageVector? = null,
    val role: ActionButtonDefaults.ActionButtonRole = ActionButtonDefaults.ActionButtonRole.Neutral,
    val activeWhileInFlight: Boolean = false   // accepted delta, D-01
)

// HandledByUiModel: append after escalationsLabel
//   val prefixLabel: String = "Handled by", val onDevice: Boolean = false
// FailureActionUiModel: append after role
//   val icon: ImageVector? = null
// Success: append after undo
//   val badge: OutcomeBadge? = null, val titleActions: (@Composable () -> Unit)? = null,
//   val startAction: SheetActionUiModel? = null, val endAction: SheetActionUiModel? = null
// Failure: append after semanticsPrefix (keeps @JvmOverloads constructor)
//   val title: String? = null, val transcript: String? = null, val tone: FailureTone = FailureTone.Error,
//   val tip: (@Composable () -> Unit)? = null, val secondaryAction: SheetActionUiModel? = null,
//   val badge: OutcomeBadge? = null, val undo: UndoAffordanceUiModel? = null     // undo LAST (VXR-02)
// NeedsConfirmation: INSERT after topLevelContent, BEFORE onConfirm/onCancel
//   val confirmEnabled: Boolean = true, val titleActions: (@Composable () -> Unit)? = null,

// component/OutcomeSheetDefaults.kt (new, lane 1)
enum class OutcomeSheetStyle { Classic, Receipt }
@Immutable class OutcomeSheetColors(val warnTone: ToneColors, val errorTone: ToneColors) { /* equals/hashCode/toString by value, like ToneColors */ }
object OutcomeSheetDefaults {
    @Composable fun colors(warnTone: ToneColors = ToneDefaults.warn(), errorTone: ToneColors = ToneDefaults.error()): OutcomeSheetColors
    val ReceiptTitleStyle: TextStyle @Composable get() =
        MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold)
}

// OutcomeSheet: append after modifier
fun OutcomeSheet(outcome, onDismissRequest, modifier: Modifier = Modifier,
    style: OutcomeSheetStyle = OutcomeSheetStyle.Classic,
    colors: OutcomeSheetColors = OutcomeSheetDefaults.colors())
```

[VERIFIED sources: sketch Y-3..Y-7 read in full; REQUIREMENTS VRCPT-01..05 read; `SheetActionUiModel` field order from the sketch Y-5 + D-01.] `OutcomeSheetColors` is a plain class with value `equals` on purpose: Phase 20 documented that a data class would publish `copy`/`componentN` into the API [VERIFIED: `ToneTokens.kt:10-19` KDoc "A plain class on purpose, not a `data class`"]; mirror it.

### What `api.txt` will do (metalava)

- `./gradlew apiDump` regenerates `api.txt` (`metalavaGenerateSignatureRelease`); `./gradlew apiCheck` (`metalavaCheckCompatibilityRelease`) fails if the compiled API removes or changes anything the committed `api.txt` lists. [VERIFIED: `build.gradle.kts:33-43`, `tools/README-api-guard.md`] Therefore the workflow per API commit is **`apiDump` first, then `apiCheck`, commit `api.txt` in the same commit** (the stale-apitxt lesson). `apiCheck` only fails on removals/changes, not additions.
- Data-class widening rewrites these existing `api.txt` lines (each removed line has an added counterpart for the same member, so nothing is lost for source callers): the `OutcomeSheet(` method (api.txt:307); the full-arity `copy(...)` of `HandledByUiModel` (1110), `FailureActionUiModel` (1087), `Failure` (1441), `NeedsConfirmation` (1467), `Success` (1500); the `ctor` of `NeedsConfirmation` (1455) and `Success` (1493); the 3-arg `ctor FailureActionUiModel(label, onClick, optional role)` (1082); `NeedsConfirmation.component10/component11` (1457-1458, shifted by the two inserted fields). `@JvmOverloads` adds new prefix-ctor lines for `HandledByUiModel` (2 more) and `Failure` (7 more) and removes none.
- **Expected removed-line count in this phase: about 11** (1 OutcomeSheet + 2 FailureActionUiModel + 1 HandledBy copy + 1 Failure copy + 4 NeedsConfirmation + 2 Success), so `git diff --numstat v2.5.1 -- api.txt` should go from `111 3` (today) to roughly `x 14`. `[ASSUMED]` count, derived by reading `api.txt`, not by running Metalava; the gate must list every removed line and confirm each has a same-member counterpart. Phase 24's "no public symbol removed" check must treat "old arity of a widened member" as the accepted P2 rewrite (a literal append-only diff is impossible, per the sketch's P2).
- Source consequences (accepted by D-02): positional destructuring of `NeedsConfirmation` beyond `component9` changes meaning; positional calls to `NeedsConfirmation(...)` that passed `onConfirm`/`onCancel` positionally stop compiling. Both consumers and all in-repo callers use named args [VERIFIED: sketch §0; Explorer fixtures at `VoiceCommandFamilyScreen.kt:483-516` use `onConfirm = {}`/`onCancel = {}`].

### Serial split so each commit leaves `apiCheck` green

Order forced by type dependencies (a type must exist before a field uses it):

1. **Leaf types first** (no existing member changes): `OutcomeBadge`, `FailureTone`, `SheetActionUiModel`, then the two small widenings `HandledByUiModel` (+2) and `FailureActionUiModel` (+icon). `apiDump`, `apiCheck`, commit.
2. **Then the three state arms**: `Success`, `Failure`, `NeedsConfirmation`. `apiDump`, `apiCheck`, commit.
3. **Then the composable**: `OutcomeSheetStyle`, `OutcomeSheetColors`, `OutcomeSheetDefaults`, `OutcomeSheet(style, colors)`. `apiDump`, `apiCheck`, commit.
4. After that, plans 22-03..22-05 change no public symbol; `apiDump` must leave `api.txt` byte-identical.

## Architecture Patterns

### System Architecture Diagram

```
Consumer (CT / SB / Explorer)
  OutcomeSheet(outcome, onDismissRequest, modifier, style = Classic, colors = defaults)
        |
        v
  style == Receipt ? ------------------------ yes --> OutcomeSheetReceipt  (OutcomeSheetReceipt.kt, private/internal)
        |                                                 |
        no (early return skips this)                      v
        |                                    SheetScaffold(shape = 24dp top corners)
        v                                                 |
  SheetScaffold(onDismissRequest, modifier)               v
        |                                    OutcomeSheetReceiptContent(outcome, colors)   <- internal test seam
        v                                      Column(fillMaxWidth)
  OutcomeSheetContent(outcome, colors)           |-- Column(weight(1f, fill=false), verticalScroll, padding 16h/4t)
   (internal, existing)                          |      when (outcome)
   Column                                        |        Success  : titleRow -> pill -> editableContent -> batchResults -> undo(locked=inFlight) -> actionsRow(24dp above)
    |-- scroll body (weight 1f, fill=false)      |        Failure  : titleRow -> transcript -> pill -> ToneCard(reason+body) -> tip -> undo
    |     SuccessBody / FailureBody(+colors)     |        NeedsConf: titleRow -> transcript(reason) -> hint -> topLevelContent -> items
    |       FailureBody: Warn -> colors.warnTone |-- pinned bar (NeedsConfirmation + Failure only):
    |       then Failure.undo below the surface  |      HorizontalDivider(1dp outlineVariant) -> Row(16h, 12t, 16b): start | Spacer(1f) | end
    |-- NeedsConfirmationActions (pinned)
         Confirm: enabled = confirmEnabled   shared internal blocks: UndoAffordanceBody (liveRegion on refused), BatchResultsList, ProposedItemRow
```

### Recommended file structure

```
src/main/java/io/github/ygaray/yahirandroidtaste/
  component/OutcomeSheet.kt            # Classic + the four named edits + early-branch dispatch + 3 private->internal helpers
  component/OutcomeSheetReceipt.kt     # NEW: all Receipt composables (private/internal) + pure internal palette seams
  component/OutcomeSheetDefaults.kt    # NEW: OutcomeSheetStyle, OutcomeSheetColors, OutcomeSheetDefaults
  model/OutcomeBadge.kt  model/FailureTone.kt  model/SheetActionUiModel.kt   # NEW
  model/HandledByUiModel.kt  model/FailureActionUiModel.kt  model/VoiceOutcomeUiState.kt   # widened
  theme/Dimens.kt                      # append: object OutcomeReceipt { ... }  (avoid the name OutcomeSheet)
src/test/java/.../component/
  OutcomeSheetReceiptNeedsConfirmationTest.kt   OutcomeSheetReceiptSuccessTest.kt   OutcomeSheetReceiptFailureTest.kt
  OutcomeSheetReceiptTagsTest.kt   OutcomeSheetClassicParityTest.kt   OutcomeSheetClassicParitySourceContractTest.kt
  OutcomeSheetReceiptLongLabelTest.kt   OutcomeSheetLiveRegionTest.kt   OutcomeSheetReceiptSourceContractTest.kt
  model/OutcomeReceiptModelsTest.kt   component/OutcomeSheetSourceCompatV251Test.kt   (+ edits to VoiceModelLabelDefaultsTest, theme/DimensTest)
```

### Pattern 1: Early-branch widening above an untouched legacy block (Phase 20/21 precedent)

**What:** Add the new branch above the legacy code and `return`; the legacy lines stay textually intact. Phase 21-02 did this for `SegmentedOptionSelector` and pinned it with a whitespace-stripped source contract (`SegmentedOptionMaterialParitySourceContractTest`). [VERIFIED: 21-02-SUMMARY.md]
**When:** `OutcomeSheet(style = Receipt)`.

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutcomeSheet(
    outcome: VoiceOutcomeUiState,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,                       // gains a trailing comma only (normalized by verify-additive-diff)
    style: OutcomeSheetStyle = OutcomeSheetStyle.Classic,
    colors: OutcomeSheetColors = OutcomeSheetDefaults.colors()
) {
    if (style == OutcomeSheetStyle.Receipt) {
        OutcomeSheetReceipt(outcome, onDismissRequest, modifier, colors)   // private/internal, new file
        return
    }
    SheetScaffold(onDismissRequest = onDismissRequest, modifier = modifier) {   // unchanged line
        OutcomeSheetContent(outcome, colors = colors)                          // the ONE rewritten Classic line here (lane 2)
    }
}
```

`[ASSUMED]` that a Composable with an early `return` compiles cleanly; Phase 21-02 used the same shape in `SegmentedOptionSelector`. [VERIFIED: 21-02-SUMMARY "Early-return branch ... reached by an early `return`"]

### Pattern 2: Explicit `colors` param, pure palette seams

Pass `colors: OutcomeSheetColors` down to private composables. Put every color decision Robolectric cannot read (no `captureToImage`) in a pure `internal` function so JUnit can assert it directly. Precedent: `emphasisColors(emphasis, role, scheme)` in `DynamicActionButton.kt` [VERIFIED: file read]. Needed seams: `failureToneColors(tone, colors)`, `outcomeBadgeColors(badge, colors, scheme)`, `outcomeBadgeIcon(badge)`, `handledByDotColor(onDevice, colors, scheme)`, `actionEnabled(action, inFlight)`.

### Pattern 3: Shared undo block, not a copy

Make `UndoAffordanceBody`, `BatchResultsList` and `ProposedItemRow` `internal` (`private fun` -> `internal fun`, a visibility rewrite, lane 2) so the new file calls the *same* code (VXR-02: "the same undo block as Success"; VXR-01: one live-region edit covers both styles). `internal` composables are skipped by the registry and vocabulary guards (`isPrivateOrInternal`) and do not appear in `api.txt`. [VERIFIED: `DomainVocabularyDriftGuardTest.kt:201-202`]

### Anti-Patterns to Avoid

- **Using `colors.errorTone` for Classic default tone.** `ToneDefaults.error()` is `ToneColors(errorContainer, error)` [VERIFIED: `ToneTokens.kt:47`], but Classic paints `errorContainer`/`onErrorContainer` (`OutcomeSheet.kt:294-295`). Only `FailureTone.Warn` switches Classic to `colors.warnTone`; `Error` keeps the existing two lines.
- **Re-rendering the undo block or item rows inside `OutcomeSheetReceipt.kt`.** Drift from Classic and double maintenance.
- **A hidden 3-arg `OutcomeSheet`, old-arity `copy`, `@Deprecated(HIDDEN)` anything.** Forbidden by D-02/P1.
- **Setting `Modifier.testTagsAsResourceId`.** Forbidden and already scanned.
- **`mergeDescendants = true` on the refused surface just to carry the live region.** It would turn the child `Text` into part of one node and could change how existing `onNodeWithText` queries resolve; the documented `liveRegion` contract already covers children (Pitfall P10).
- **Tagging the Success actions row `outcome_sheet_action_bar`.** The tag is the *pinned* bar root; Success is not pinned (see tag matrix).

## Receipt arms: reuse map (Q3)

| Receipt element | Build with | Notes |
|-----------------|------------|-------|
| Sheet frame | `SheetScaffold(onDismissRequest, modifier, shape = RoundedCornerShape(topStart = Dimens.OutcomeReceipt.CornerRadius, topEnd = ...)) { OutcomeSheetReceiptContent(...) }` | `shape` sits before the trailing `content` [VERIFIED: `SheetScaffold.kt:54-61`]. Prove with a source contract (the shape cannot be seen in a semantics tree; Phase 21 Pitfall 9) plus the Gate-1 corner measurement |
| Failure card | `ToneCard(tone = failureToneColors(failure.tone, colors), modifier = Modifier.fillMaxWidth().testTag("outcome_sheet_failure_surface")) { ... }` | `ToneCard(tone, modifier, content: ColumnScope.() -> Unit)`; it applies `testTag("tone_card")` **after** `modifier`, so the caller tag wins [VERIFIED: `ToneCard.kt:28-31` and `ToneCardTest` "caller testTag overrides the default tone_card tag"]. 12dp corners, 12x16dp padding, 4dp child spacing built in |
| Pinned/Success buttons | `DynamicActionButton(label, role, onClick, enabled, modifier, emphasis, icon)` | `emphasis` picks the widget, `role` the hue (Destructive -> `error`, others -> `primary`) [VERIFIED: `DynamicActionButton.kt:66-74`, `emphasisColors`]. NC start = `Text`/Neutral; NC end = `Filled`/`severity`; Failure end = `Filled`/`action.role` + `action.icon`; Failure start / Success start+end from `SheetActionUiModel` |
| `titleActions` slot content | consumer-supplied (CT passes `RoundIconButton`) | The hub only hosts the slot; wrap in `Box(Modifier.testTag("outcome_sheet_title_actions"))` |
| Divider | `HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)` | Already used in `ReceiptRow.kt` [VERIFIED: grep] |
| Pill background | `Surface`/`Modifier.background(colorScheme.surfaceContainerHighest, CircleShape)` | `surfaceContainerHighest` is set in the hub's schemes [VERIFIED: `Theme.kt:49,87`] |
| warn / error tones | `colors.warnTone`, `colors.errorTone` from `OutcomeSheetColors`; defaults `ToneDefaults.warn()` = `tertiaryContainer`/`onTertiaryContainer`, `error()` = `errorContainer`/`error` [VERIFIED: `ToneTokens.kt:47-55`] | Badge: Success = `primary` fill + `onPrimary` icon; Reverted = `onSurfaceVariant` fill + `surface` icon; Error = `errorTone` (container fill, content icon); Warn = `warnTone` with content/container swapped (fill = `warnTone.content`, icon = `warnTone.container`) [CITED: CT 76-UI-SPEC Y-5, read this session]. Icons: Check, Close (core), `AutoMirrored.Filled.Undo`, `PriorityHigh` (extended, verified present) |

`Dimens` already has `HorizontalPadding = 16.dp` (line 37), `ContentSpacing = 4.dp` (46), `CompactPadding = 12.dp` (77), `HairlineBorder = 1.dp` (83), `ActionButton.*`. Add one new nested group (Phase 20/21 convention; a numeric-literal guard is not active, but tokens keep detekt/tests tidy): `Dimens.OutcomeReceipt { CornerRadius 24, BadgeSize 32, BadgeIconSize 18 [ASSUMED], TitleRowGap 8, BodyTopPadding 4, TranscriptTop 4, TranscriptBottom 8, BarTop 12, BarBottom 16, SuccessActionsTop 24, SuccessActionsBottom 16 [ASSUMED], PillVertical 4, PillHorizontal 12, DotSize 6, DotGap 6 [ASSUMED] }` plus a `DimensTest` case. Name it `OutcomeReceipt`, not `OutcomeSheet`, to avoid shadowing the composable name in call sites.

### Tag matrix (VTAG-01; "applicable" cells only)

| Tag | NC single/batch | Success single/batch/all-undone | Failure error/warn |
|-----|:---:|:---:|:---:|
| `outcome_sheet_title` | if `title` | always (`summary`) | if `title` |
| `outcome_sheet_badge` | n/a | if `badge` | if `badge` |
| `outcome_sheet_title_actions` | if `titleActions` | if `titleActions` | n/a (`Failure` has no slot) |
| `outcome_sheet_transcript` | `reason` (if not blank) | n/a | if `transcript` |
| `outcome_sheet_action_bar` | yes (pinned) | no (not pinned) | if `action` or `secondaryAction` |
| `outcome_sheet_confirmation_cancel` / `_confirm` | yes (kept) | n/a | n/a |
| `outcome_sheet_action_start` / `_end` | n/a | if `startAction` / `endAction` | if `secondaryAction` / `action` |
| `outcome_sheet_failure_surface` / `_tip` | n/a | n/a | always / if `tip` |
| `outcome_sheet_handled_by` | n/a (no field) | if `handledBy` | if `handledBy` |

Receipt NC additionally keeps `outcome_sheet_confirmation_item`, `_item_remove` and `_top_level_content` because it reuses `ProposedItemRow` and the same top-level box. A node carries one tag, so Receipt NC buttons keep `_confirmation_*` and do not also get `_action_*`. Test-only tags (stay out of API docs, like `receipt_row_chevron`): `outcome_sheet_action_bar_divider`, `outcome_sheet_handled_by_dot`. `[ASSUMED]` choice not to tag the Success row with `outcome_sheet_action_bar` and to render no bar/divider when a Failure has neither action (Open Question OQ-3).

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Tinted failure card | A new `Surface` with errorContainer | `ToneCard` | 12dp corners/padding/spacing, content color and the caller-tag-wins contract already tested |
| Action buttons | `Button`/`TextButton` with local colors | `DynamicActionButton(emphasis, icon)` | Hue/emphasis axis, 48dp target, disabled treatment, German wrap already tested (`ToneAtomsLongLabelTest`) |
| 1dp divider | `Box` with background | `HorizontalDivider` | Semantics and thickness handled |
| Undo block, batch rows, item rows | Receipt copies | The existing private blocks, made `internal` | One live-region edit and one `Failure.undo` path serve both styles |
| Palette checks | Pixel assertions | Pure `internal` mapping functions + `LocalContentColor` capture | `captureToImage` times out in this harness |
| Privacy `toString` | Ad-hoc | Copy `NeedsConfirmation.toString()` (`VoiceOutcomeUiState.kt:197-198`) | Same T-12-01 rationale |

**Key insight:** every hard problem in this phase is already solved elsewhere in the hub; the risk is breaking Classic or the signature pins while wiring, not inventing UI.

## Common Pitfalls

### P1: Classic byte-parity vs. the four required Classic edits
**What goes wrong:** "Classic stays byte-unchanged" (D-04) collides with D-06/D-07/VXR-01/VXR-02, which require Classic to honor `confirmEnabled`, `FailureTone.Warn`, `Failure.undo` and the live region.
**How to avoid:** Treat them as an enumerated, tested delta: (1) `NeedsConfirmationActions`: `enabled = confirmation.confirmEnabled` on the Confirm button; (2) `FailureBody(failure, colors)`: the two color lines become `if (failure.tone == FailureTone.Warn) colors.warnTone.container else colorScheme.errorContainer` and likewise for content; (3) `OutcomeSheetContent`: after `FailureBody(...)` render `failure.undo` below the surface inside a `Modifier.padding(horizontal = Dimens.HorizontalPadding)` wrapper (the Failure surface is edge-to-edge, so the undo block needs its own horizontal padding); (4) `UndoAffordanceBody`: `.semantics { liveRegion = LiveRegionMode.Assertive }` on the refused `Surface`. Pin everything else with a source contract (Phase 21-02 style) and print `git diff --numstat v2.5.1 -- OutcomeSheet.kt` plus each removed line in the SUMMARY. All four are lane 2 (`HUB_LANE_OVERRIDE=2`).
**Warning signs:** a changed removed-line list; any `OutcomeSheetTest` edit.

### P2: Private Classic helpers are invisible to `OutcomeSheetReceipt.kt`
Make exactly `UndoAffordanceBody`, `BatchResultsList`, `ProposedItemRow` `internal`; leave `FailureBody`, `SuccessBody`, `HandledByRow`, `NeedsConfirmationBody`, `NeedsConfirmationActions`, `UndoRowItem` private (`FailureRoleSourceContractTest` extracts `"private fun FailureBody("`).

### P3: Arity-pin assertions break on widening (no shims allowed)
`VoiceModelLabelDefaultsTest` pins, verbatim:
- lines 167-170: `HandledByUiModel` ... `containsAll(listOf(5, 6))` for constructors and `copyArities`. After widening, constructors still include 5 and 6 (`@JvmOverloads`), but the generated `copy` is 8-arity, so **copy arities become {5, 8}**: edit to `listOf(5, 8)`.
- lines 255-258: `FailureActionUiModel` ... `listOf(2, 3)`. After adding `icon`, the primary constructor is 4-arity and the generated `copy` is 4-arity: edit both to `listOf(2, 4)`. (The 2-arg ctor/copy survive; `getConstructor(String, Function0)` in `VoiceI18nSourceCompatTest` still resolves.)
- lines 286-289: `Failure` ... `copyArities ... listOf(3, 5)`. Constructors 1..5 survive; generated `copy` becomes 12-arity: edit to `listOf(3, 12)`.
`[ASSUMED]` arities, derived by hand (the harness could not be run); the executor confirms by running the class once. Edit these assertions **in the same commit** as each widening and say so in the SUMMARY; this is a necessary consequence of D-02, not a loosening of the Classic tests. `assertLegacyCopyCarriesEveryField` (shortest `copy`) keeps working because `copy`'s defaults are the receiver's values, so the legacy 5/3/2-arg copies preserve the new fields automatically.

### P4: Generated `toString` would log the spoken transcript
`Failure` will carry `transcript` (user speech). The data-class `toString` prints every field into logs, crash reports and recomposition traces. Override it like `NeedsConfirmation`'s (counts/enum values only, no `reason`, `title`, `transcript`, `semanticsPrefix`) and add a test mirroring `NeedsConfirmation toString never prints title or reason`. Threat T-22-01 (ASVS V7). Adding the override is additive in `src/main`.

### P5: `NeedsConfirmation` positional shift
`component10`/`component11` and positional construction change; in-repo callers are named (verified). Add a compile-only canary test with the v2.5.1 named shape (`reason`, `items`, `onConfirm`, `onCancel` only) and the full named shape.

### P6: Scroll body needs bounded height in tests
`verticalScroll` inside an unbounded-height parent throws. Host `OutcomeSheetReceiptContent` in `Box(Modifier.height(...))` or directly under the compose rule root (bounded), exactly as the existing tall test does (`Box(Modifier.height(240.dp))`). The pinned bar must be a sibling *after* the `weight(1f, fill = false)` scroll Column so it is measured first.

### P7: Long German labels in the pinned bar (the memory lesson)
D-05's literal row is `start | Spacer(weight 1f) | end`, with non-weighted buttons. A long start label is measured first and can leave the end button squeezed. `DynamicActionButton` labels wrap to >= 2 lines without ellipsis at 360dp [VERIFIED: `ToneAtomsLongLabelTest` assertLongLabelWraps], so the real risk is a cramped end button or an end edge past the window, not clipping. **Prescription:** implement D-05 literally, then add `OutcomeSheetReceiptLongLabelTest` (w360dp, `GraphicsMode.NATIVE`) with a long German Cancel and Confirm and assert: both buttons inside the 360dp window (unclipped `positionInRoot + size`), neither label ellipsized, bar bottom on screen. **Only if that fails**, switch to the escape hatch: both actions `Modifier.weight(1f, fill = false)` with `horizontalArrangement = Arrangement.SpaceBetween` and no `Spacer` (identical edges for short labels, 16dp / 368dp still asserted by the 384dp test). Do NOT put the `Spacer(weight 1f)` together with weighted actions: leftover space is not redistributed to the spacer, so the end action would drift left.

### P8: In-flight enable formula
`enabled = action.enabled && (!inFlight || action.activeWhileInFlight)`. A caller `enabled = false` always wins; `activeWhileInFlight` only exempts the in-flight lock. Cover: inFlight with one exempt action (fires) and one normal action (disabled, never fires), plus exempt with `enabled = false` (still disabled). Receipt must also keep Classic's other inFlight rule for the undo block: `UndoAffordanceBody(undo, locked = success.inFlight)`.

### P9: The pill must expose one string
`"Handled by Local grammar · offline"` is asserted as text. Build the content as **one `Text(buildAnnotatedString { ... })`** (SemiBold `onSurface` span for the tier) so the merged text node equals the full string; three adjacent `Text`s would produce a list. The 6dp dot is a separate `Box` (no text). Tag the pill `outcome_sheet_handled_by` with `semantics(mergeDescendants = true)`; test the dot color through the pure `handledByDotColor` seam plus a test-only `outcome_sheet_handled_by_dot` tag. Long text: let the `Text` take `Modifier.weight(1f, fill = false)` so a long German tier + provider wraps inside 360dp minus 32dp padding instead of pushing the dot out; assert pill right edge <= 360dp - 16dp.

### P10: Where the Assertive live region goes
Put it on the refused `Surface` only (it exists only when `refused != null`, so it "appears" and is announced); never on an always-present container, which would announce unrelated updates. Compose docs: "liveRegion ... allows accessibility services to automatically notify users of changes to this component or its children" [CITED: developer.android.com/develop/ui/compose/accessibility/semantics]. Assert in tests with `SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Assertive)` [CITED: developer.android.com/develop/ui/compose/testing/common-patterns]. Existing tests query the refused text via `onNodeWithText`, and a node that carries a testTag plus `semantics { liveRegion = ... }` without `mergeDescendants` leaves that query untouched. What TalkBack actually speaks is a Gate-2 item: `uiautomator` XML does not expose live-region mode, so the device pass cannot decide it (`[ASSUMED]`, flag for the owner's one-time TalkBack read-out, exactly as 21-04 did for `stateDescription`).

### P11: `editableContent` while `inFlight`
Classic hides `editableContent` while `inFlight` and the model KDoc states it unconditionally (`VoiceOutcomeUiState.kt:32-35`). D-08 is silent for Receipt. See OQ-1; default recommendation: follow the documented contract in both styles.

### P12: Worktree drift-guard blind spot
`config.workflow.use_worktrees` is `true`, and `ComponentRegistryDriftGuardTest`/`DomainVocabularyDriftGuardTest` walk up to the first directory named `yahirandroidtaste`, which is the **main** checkout, not an `agent-*` worktree. [VERIFIED: 21-04-SUMMARY "Worktree note"] Require one full `./gradlew testDebugUnitTest` in the main checkout after the merge.

### P13: Gradle hygiene
Exactly one gradle invocation at a time; `--tests X` binds to the preceding task only (`./gradlew testDebugUnitTest --tests X detekt apiCheck` is fine); text-geometry tests use `@Config(sdk = [35], qualifiers = "w360dp-h800dp")` + `@GraphicsMode(GraphicsMode.Mode.NATIVE)`; one `setContent` per test method. The 384dp bounds test uses `qualifiers = "w384dp-h800dp"`.

## Code Examples

### Pinned bar (Receipt NeedsConfirmation / Failure), D-05 literal

```kotlin
// Source: D-05 + sketch Y-3; building blocks verified in-repo (HorizontalDivider, DynamicActionButton, Dimens.HorizontalPadding)
@Composable
private fun ReceiptActionBar(modifier: Modifier = Modifier, start: (@Composable () -> Unit)?, end: (@Composable () -> Unit)?) {
    Column(modifier.fillMaxWidth().testTag("outcome_sheet_action_bar")) {
        HorizontalDivider(Modifier.testTag("outcome_sheet_action_bar_divider"), thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),  // use Dimens.OutcomeReceipt tokens
            verticalAlignment = Alignment.CenterVertically
        ) {
            start?.invoke()
            Spacer(Modifier.weight(1f))
            end?.invoke()
        }
    }
}
```

### Pure seams (unit-testable without pixels)

```kotlin
internal fun failureToneColors(tone: FailureTone, colors: OutcomeSheetColors): ToneColors =
    if (tone == FailureTone.Warn) colors.warnTone else colors.errorTone

internal fun outcomeBadgeIcon(badge: OutcomeBadge): ImageVector = when (badge) {
    OutcomeBadge.Success -> Icons.Filled.Check
    OutcomeBadge.Reverted -> Icons.AutoMirrored.Filled.Undo
    OutcomeBadge.Error -> Icons.Filled.Close
    OutcomeBadge.Warn -> Icons.Filled.PriorityHigh
}

internal fun outcomeBadgeColors(badge: OutcomeBadge, colors: OutcomeSheetColors, scheme: ColorScheme): ToneColors = when (badge) {
    OutcomeBadge.Success -> ToneColors(scheme.primary, scheme.onPrimary)            // container, content
    OutcomeBadge.Reverted -> ToneColors(scheme.onSurfaceVariant, scheme.surface)
    OutcomeBadge.Error -> colors.errorTone
    OutcomeBadge.Warn -> ToneColors(colors.warnTone.content, colors.warnTone.container)   // swapped
}

internal fun actionEnabled(action: SheetActionUiModel, inFlight: Boolean): Boolean =
    action.enabled && (!inFlight || action.activeWhileInFlight)
```

### Live region on the refused surface (the only visual-free Classic/Receipt-shared edit)

```kotlin
// Source: Compose accessibility docs (liveRegion), applied inside UndoAffordanceBody
Surface(
    color = MaterialTheme.colorScheme.errorContainer,
    contentColor = MaterialTheme.colorScheme.onErrorContainer,
    shape = MaterialTheme.expressive.cardShapeLarge,
    modifier = Modifier
        .fillMaxWidth()
        .padding(top = Dimens.ContentSpacing)
        .testTag("outcome_sheet_undo_refused")
        .semantics { liveRegion = LiveRegionMode.Assertive }   // imports: ...semantics.LiveRegionMode, ...semantics.liveRegion
) { /* unchanged Text */ }

// Test
composeTestRule.onNodeWithTag("outcome_sheet_undo_refused")
    .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Assertive))
// and: exactly one node in the tree defines it
composeTestRule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.LiveRegion)).assertCountEquals(1)
```

### 384dp bounds + node-order idioms (copy from existing tests)

```kotlin
// Bounds: unclipped, as in ToneAtomsLongLabelTest / SegmentedOptionSelectorPillTest
private fun SemanticsNodeInteraction.edges(): Pair<Float, Float> {
    val n = fetchSemanticsNode(); val left = n.positionInRoot.x; return left to left + n.size.width
}
// @Config(sdk = [35], qualifiers = "w384dp-h800dp"); host: Box(Modifier.width(384.dp).height(800.dp)) { OutcomeSheetReceiptContent(...) }
// density: assertEquals(16f * density, start.edges().first, 0.5f); assertEquals(368f * density, end.edges().second, 0.5f)
// Order (existing precedent OutcomeSheetTest.topOf): boundsInRoot.top of tagged nodes with useUnmergedTree = true
//   title < outcome_sheet_handled_by < editable probe < undo/batch rows < outcome_sheet_action_start
// Bar never starves: Box(Modifier.height(240.dp)) { OutcomeSheetReceiptContent(tallFixture) } -> both bar buttons assertIsDisplayed().assertHeightIsAtLeast(40.dp)
// Classic Warn content color: capture LocalContentColor.current inside Failure.body and compare with colors.warnTone.content
```

## State of the Art

| Old approach | Current approach | When | Impact |
|--------------|------------------|------|--------|
| Hand-written old-arity `copy`/hidden ctors for every widening (v2.5.0) | None: source compat only, `api.txt` regenerated per commit | v2.6 sketch P1 (2026-10-11) | The v2.5.0 shims stay but must not be extended; tests that pinned the old arities are edited |
| `verify-api-additive.sh` | `apiCheck` + `tools/test/run-all.sh` + a v2.5.1 call-site canary | retired in Phase 19 | Per-commit gate is `apiCheck` only |
| Golden/screenshot parity | Structural proof + Robolectric state assertions; owner screencap review in Phase 24 | P3 | No Roborazzi; `captureToImage` times out |
| `Icons.Default.Undo` | `Icons.AutoMirrored.Filled.Undo` | sketch Y-5 deviation | RTL-correct, non-deprecated |

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | About 11 `api.txt` lines are removed (rewritten) by this phase; cumulative `v2.5.1` removed count ~14 | Widening mechanics | Only the expected-number in the gate; the real invariant is "each removed line has a same-member counterpart" |
| A2 | Arity-pin values after widening: HandledBy copy {5,8}; FailureActionUiModel ctor/copy {2,4}; Failure copy {3,12} | P3 | Executor sees the exact failing assertion on first run and adjusts; no design impact |
| A3 | A `@Composable` with an early `return` compiles (Phase 21-02 precedent) | Pattern 1 | Fallback: `if (style == Receipt) { ... } else { <classic> }` rewrites more lines (lane 2 anyway) |
| A4 | Not hiding vs hiding `editableContent` while `inFlight` in Receipt (recommend: hide, per the model KDoc) | P11 / OQ-1 | CT's editor visibility during in-flight; confirm with the owner/teamlead before Phase 24 |
| A5 | Receipt pill also appends the escalation caption when `escalationCount != null` (joined with ` · `, same as Classic); D-10 lists only approach/provider/model | Receipt arms | Minor: info dropped vs shown; zero effect when null |
| A6 | Failure Receipt order: title row -> transcript -> handled-by pill -> ToneCard -> tip -> undo -> bar | Architecture | CT sets `handledBy = null` on Failure, so low risk |
| A7 | Title row renders iff `title != null || badge != null || titleActions != null` (Success always); no bar and no divider when a Failure has neither action | Tag matrix | Cosmetic edge cases not specified by Y-3 |
| A8 | Unspecified dimensions: badge icon 18dp, dot-to-text gap 6dp, Success actions bottom padding 16dp | Receipt arms | Cosmetic; covered by owner screencap review (Y-15) |
| A9 | `liveRegion` on the refused `Surface` (not merged) is enough for TalkBack to speak the child text | P10 | Gate-2 TalkBack read-out may ask for `mergeDescendants`; one-line follow-up |
| A10 | Overriding `Failure.toString` to redact free text is within scope (security hardening of a field this phase adds) | P4 | None for consumers; behavior change only in log text |

## Open Questions

1. **OQ-1: Does Receipt hide `editableContent` while `Success.inFlight`?**
   - Known: Classic hides it; the model KDoc documents "Hidden when null OR while inFlight is true"; D-08/Y-5 say only that actions are disabled.
   - Unclear: CT's Receipt body (day-delta card, receipt rows) lives in `editableContent`; hiding it during a write would blank the receipt.
   - Recommendation: follow the documented contract (hide) in both styles for 22, record A4 in the SUMMARY for the owner; revisit with CT before the cut if the blank flash is unwanted.
2. **OQ-2: Pill escalation caption.** See A5; default: include when non-null.
3. **OQ-3: Success row tag and empty Failure bar.** See A7 and the tag matrix; defaults chosen so the Y-14 list is honored literally.
4. **OQ-4: Should Phase 22 add a minimal Explorer Receipt demo for Gate-1 addressability?** Recommendation: **no**. DEMO-01 (Phase 24) owns the curated demos; the Gate-1 tester uses a throwaway ProbeActivity (21-04 precedent) with the probe-case list below, and Classic parity A/B uses the *existing* Voice Command `OutcomeSheetVariants` in both builds.

## Runtime State Inventory

Not a rename/refactor/migration phase (additive API + new rendering). Omitted.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| JDK 17 | gradle build/tests | yes | OpenJDK 17.0.19 | none needed |
| Gradle wrapper | all verification | present (not executed this session) | 9.4.1 | none |
| git | lane classifier, diffs | yes | tag `v2.5.1` = `b6c363c` | none |
| adb | Gate-1 (tester only) | `/usr/bin/adb` present; no device probed (read-only research, device rules in `~/.claude/context/devices/common.md`) | n/a | Gate-1 tester agent probes `R5CT10XNKQN`; a not-ready device after one retry is INFRA, not FAIL |
| `gsd_run uat-run` | Gate-1 evidence | gsd-tools present | n/a | none |
| mavenLocal AAR | Gate-1 harness | `1.10.0` and `1.10.0-v251` directories exist (stale) | n/a | **Delete then republish** before the harness build (hardcoded version, per `AGENT-DEVICE-TESTING.md` D7) |

**Missing with no fallback:** none. **Missing with fallback:** none.

## Proposed serial plan split (per-plan apiCheck gating)

All plans are strictly serial (shared files: `OutcomeSheet.kt`, `OutcomeSheetReceipt.kt`, `api.txt`). Every plan: one gradle at a time, stage only its own files, lane 2 commits declared with `HUB_LANE_OVERRIDE=2` and named in the SUMMARY.

| Plan | Wave | Scope | Reqs | Public API change? | apiCheck gate |
|------|------|-------|------|--------------------|---------------|
| **22-01** Models | 1 | T1: `OutcomeBadge`, `FailureTone`, `SheetActionUiModel`, widen `HandledByUiModel` + `FailureActionUiModel`, edit the 2 matching arity pins, `OutcomeReceiptModelsTest` (defaults, `activeWhileInFlight` default false, equality). T2: widen `Success`, `Failure` (+`toString` redaction + test), `NeedsConfirmation`; edit the Failure arity pin; v2.5.1 call-shape canary `OutcomeSheetSourceCompatV251Test` (named NC shape, `Failure("r", null, null)`, destructuring, trailing-lambda `FailureActionUiModel`) | VRCPT-02..05 (fields), VXR-02 (field) | yes, 2 commits | per commit: `apiDump` -> `apiCheck` -> `git add api.txt`; targeted: `VoiceModelLabelDefaultsTest`, `DataClassBinaryCompatShimTest`, `VoiceI18nSourceCompatTest`, `OutcomeSheetTest` (must be unedited), then `detekt` |
| **22-02** Tracer | 2 | T1: `OutcomeSheetStyle/Colors/Defaults` (new file), `Dimens.OutcomeReceipt` + `DimensTest`, `OutcomeSheet(style, colors)` early branch (lane 2), `OutcomeSheetContent(.., colors)` param, make `ProposedItemRow` internal. T2: `OutcomeSheetReceipt.kt` frame: `SheetScaffold(shape)`, scroll body + title row (badge slot, heading title, titleActions) + transcript + pinned `ReceiptActionBar`, **NeedsConfirmation end to end** (reason as transcript, hint, topLevelContent, items, Text cancel / Filled confirm with `severity`, `confirmEnabled`). T3: tests: 384dp edges (16/368), divider above bar, tall body keeps bar displayed, `confirmEnabled = false` -> disabled + no `onConfirm`, tags, German 360dp bar, source contract (shape + dispatch), Receipt NC with empty `items` + `topLevelContent` | VRCPT-01, VRCPT-02, VTAG-01 (partial) | yes, 1 commit (OutcomeSheet signature + new types) | `apiDump` -> `apiCheck`; expect the `OutcomeSheet(` line rewritten and 3 new public types |
| **22-03** Classic deltas + parity | 3 | The four named Classic edits (P1) incl. `UndoAffordanceBody` live region, `confirmEnabled` on Classic Confirm, `FailureBody(failure, colors)` Warn tone, `Failure.undo` below the surface; tests: `OutcomeSheetClassicParityTest` (each arm built from v2.5.1 args only: tag inventory + text inventory; with `badge/titleActions/startAction/endAction` set Classic renders no `outcome_sheet_badge`/`_title_actions`/`_action_start`/`_action_end`), Warn tone via `LocalContentColor` capture in `body` + Error default still `onErrorContainer`, `OutcomeSheetLiveRegionTest`, `OutcomeSheetClassicParitySourceContractTest` (Error color lines retained, dispatch order, `SheetScaffold(onDismissRequest = onDismissRequest, modifier = modifier)` untouched, `HandledByRow` text equal to v2.5.1) | VRCPT-01/03/04/05 (Classic), VXR-01, VXR-02 (Classic), VRCPT-02 (Classic enable) | **no** | `apiDump` then `git diff --exit-code -- api.txt` (must be empty) then `apiCheck`; `OutcomeSheetTest` + `FailureRoleSourceContractTest` unedited and green |
| **22-04** Receipt Success | 4 | `UndoAffordanceBody`/`BatchResultsList` -> `internal`; Receipt Success: title row (badge) -> pill -> `editableContent` (hidden when inFlight, OQ-1) -> `batchResults` -> `undo(locked = inFlight)` -> actions row (24dp above, start / Spacer / end); `ReceiptHandledByPill`; pure seams; tests: node order, inFlight matrix (P8), badge seams incl. `assertSame(Icons.AutoMirrored.Filled.Undo, outcomeBadgeIcon(Reverted))`, pill text `"Handled by Local grammar · offline"` + dot seam, German 360dp pill and actions | VRCPT-03, VRCPT-05 | **no** | same as 22-03 |
| **22-05** Receipt Failure + gate | 5 | Receipt Failure: title row outside card, transcript, `ToneCard` (`reason` bodyMedium SemiBold `onSurface` + `body`), `semanticsPrefix` merge on the card (keep behavior), tip, `Failure.undo` block, pinned bar (`secondaryAction` start / `action` end with `icon`, role hue); `OutcomeSheetReceiptTagsTest` (matrix above across confirm single/batch, success single/batch/all-undone, failure error/warn); `OutcomeSheetReceiptSourceContractTest` (no `Color(0x`, imports, `testTagsAsResourceId` absent); phase gate (full suite, run-all, api.txt freshness, removal audit, protected-test diffs, Gate-1 handoff, SUMMARY) | VRCPT-04, VXR-02 (Receipt), VTAG-01 | **no** | full gate below |

**Phase gate (22-05 final task), in order, one command at a time** (mirrors 21-04 Task 3):
1. `./gradlew apiDump testDebugUnitTest detekt apiCheck` (full unfiltered suite).
2. `git diff --exit-code -- api.txt` (committed equals fresh dump).
3. `bash tools/test/run-all.sh` (only after gradle finished).
4. `git diff --exit-code v2.5.1 -- src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetTest.kt src/test/java/io/github/ygaray/yahirandroidtaste/component/FailureRoleSourceContractTest.kt src/test/java/io/github/ygaray/yahirandroidtaste/model/DataClassBinaryCompatShimTest.kt` (protected tests unedited).
5. Audit pasted into the SUMMARY: `git diff --numstat v2.5.1 -- api.txt` and `git diff -U0 v2.5.1 -- api.txt | grep '^-'` (every removed line is the old signature of a widened member); `git diff --numstat v2.5.1 -- src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheet.kt` with each removed line named; `bash tools/classify-hub-change.sh --baseline v2.5.1`; `git log --oneline v2.5.1..HEAD` with `HUB_LANE_OVERRIDE=2` commits named; `git diff --exit-code v2.5.1 -- config/detekt-baseline.xml config/detekt/detekt.yml config/detekt-compose.yml`; `git log v2.5.1..HEAD -S"DeprecationLevel.HIDDEN" -- src/main` prints nothing new; `git tag --list "v2.6*"` prints nothing.
6. Worktree note (P12): one full `testDebugUnitTest` in the main checkout after merge.
7. Gate-1 handoff: `gsd_run query phase.has-uat-criteria 22 --pick has_uat_criteria` -> `true` with 5 criteria [VERIFIED: ran this session: `has_uat_criteria: true`, `criteria_count: 5`; `22-CONTEXT.md` has no `device_exempt` line]. Do not hand-write a SELF-UAT, edit ROADMAP criteria, write under `.planning/uat-pending`, or touch a device from the plan.
8. Deferred-to-Phase-24 list: README/API.md/ComponentRegistry KDoc counts; API.md stable-tag section (all Y-14 + `provider_key_*`; test-only tags stay out); Explorer Receipt demos and TESTER screencaps (DEMO-01/Y-15); extend `ToneFoundationInvariantsTest.phaseFiles` or keep the Phase 22 copy; the v2.5.1 call-site canary set; Phase 24's removal check must accept the accepted old-arity rewrites.

## Gate-1 device UAT recipe (Q5)

How Phases 20 and 21 did it [VERIFIED: 21-04-PLAN.md Task 3 item 7, 21-04-SELF-UAT.md, AGENT-DEVICE-TESTING.md]: **the plan does not drive a device and does not write the SELF-UAT.** The plan's gate task records `phase.has-uat-criteria` and a "Gate-1 handoff" section; the orchestrator's `verify_work_agentic_gate` dispatches a tester agent, which produces `{phase}-04-SELF-UAT.md` (`evidence_schema: 3`) and registers the Gate-2 fragment under `.planning/uat-pending/`. Replicate that division for 22 (the SELF-UAT would be `22-05-SELF-UAT.md`; `uat-run start --plan` accepts the bare number).

Recipe the tester repeats (device `R5CT10XNKQN` only, always `adb -s R5CT10XNKQN`; the tester screen is **384dp wide** natively (1080px at 450dpi), so the 16dp / 368dp edge claim is measurable at native width; pin probes to `Modifier.width(360.dp)` for the German checks):
1. Preflight per `AGENT-DEVICE-TESTING.md` D1 (awake, unlocked, `stay_on_while_plugged_in`; not-ready after one retry = INFRA). Do not address the other `:1496` wireless entry.
2. Build identity: `rm -rf ~/.m2/repository/com/github/Ygaray/yahirandroidtaste/1.10.0` (and the maven-metadata-local.xml), then `./gradlew clean publishReleasePublicationToMavenLocal --no-daemon -q`; record AAR md5 + HEAD short sha; confirm `classes.jar` contains `OutcomeSheetDefaults`/`OutcomeSheetStyle`.
3. Throwaway harness in the session scratchpad (never committed): `com.android.application`, `compileSdk { version = release(36) { minorApiLevel = 1 } }`, `minSdk = 35`, `mavenLocal()` first; a Java/Kotlin `MainActivity` that same-package-Intents into `ExplorerActivity`, plus an exported throwaway `ProbeActivity` that renders `OutcomeSheet` states via the public API with `Modifier.semantics { testTagsAsResourceId = true }` on the harness side (consumer-owned, per Y-14).
4. Classic A/B (the key Phase 22 risk): build the same harness against a real v2.5.1 AAR (`git archive b6c363c` into the scratchpad, publish as `1.10.0-v251`, applicationId `...harnessb`), open each existing Voice Command `OutcomeSheetVariants` demo (summary, handled-by, failure no action/with action, undo, undo unavailable, undo refused, NC single/batch/trailing/tall) in both builds, same theme, and diff the screenshots (Phase 21 reported `diff bbox None, sum 0`).
5. Receipt probes (ProbeActivity cases): NC single and batch with `confirmEnabled` true/false; tall `topLevelContent` (bar stays on screen); Success single / batch / all-undone with `inFlight` true (one `activeWhileInFlight` action); handled-by pill with `onDevice = true`; Failure error and warn with tip, `action` + `icon`, `secondaryAction`, and `undo` with `refused`; German strings at the 360dp pin for the bar and the pill; 24dp top-corner extent vs default sheet (Phase 21 measured corner extents: default 23.8dp vs shaped 19.9dp at the same threshold).
6. Evidence only via `gsd_run uat-run start --phase 22 --plan 05 --surface android --device R5CT10XNKQN --package <harness id> --pick run_id`, `uat-run capture --run "$RUN" --label <slug> --kind ui|screen|log`, `uat-run finish --run "$RUN" --pick selfuat_frontmatter`, then `uat-run verify`; use `--allow-empty` for a logcat FATAL check. [CITED: `~/.claude/gsd-core/workflows/verify-work-agentic.md` lines 204-212]
7. Known limits to record, not fail: dynamic color is on in `ExplorerActivity`/probe (hub-palette hex claims are Robolectric-only; device proves geometry, alpha, "reads the active scheme"); the live-region mode and `stateDescription`-style strings are not visible to `uiautomator` (Gate-2 TalkBack read-out); Samsung S Pen overlay appears in screenshots; reset any changed device setting (animator scale) and uninstall both harness packages.

## Validation Architecture

### Test Framework

| Property | Value |
|----------|-------|
| Framework | JUnit 4 + Robolectric 4.16.1 + Compose UI test (`createComposeRule`), `@Config(sdk = [35])`, `GraphicsMode.NATIVE` for text metrics |
| Config file | `build.gradle.kts` (`testOptions.unitTests.isIncludeAndroidResources = true`); no separate test config |
| Quick run command | `./gradlew testDebugUnitTest --tests "io.github.ygaray.yahirandroidtaste.component.OutcomeSheet*" --tests "io.github.ygaray.yahirandroidtaste.model.*"` |
| Full suite command | `./gradlew testDebugUnitTest detekt apiCheck` (preceded by `apiDump` when the public API changed) |

### Phase Requirements -> Test Map

| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| VRCPT-01 | Defaulted 3-arg call = Classic path; Classic ignores `badge`/`titleActions`/`startAction`/`endAction`; shape/dispatch contract | Robolectric + source contract | `--tests "*OutcomeSheetClassicParityTest" --tests "*OutcomeSheetClassicParitySourceContractTest" --tests "*OutcomeSheetReceiptSourceContractTest"` | Wave 0 (new) |
| VRCPT-01 | Existing Classic behavior unchanged | Robolectric | `--tests "*OutcomeSheetTest" --tests "*FailureRoleSourceContractTest"` | exists, unedited |
| VRCPT-01 | Receipt bar edges 16dp / 368dp at 384dp, divider above, bar not starved | Robolectric bounds | `--tests "*OutcomeSheetReceiptNeedsConfirmationTest"` | Wave 0 |
| VRCPT-02 | `confirmEnabled=false`: confirm `assertIsNotEnabled`, `onConfirm` never fires (both styles); Receipt Text/Filled bar, reason as transcript | Robolectric click | same class + Classic parity class | Wave 0 |
| VRCPT-03 | Receipt Success node order; inFlight matrix; badge seams (`assertSame(Icons.AutoMirrored.Filled.Undo, ...)`) | Robolectric + JUnit | `--tests "*OutcomeSheetReceiptSuccessTest"` | Wave 0 |
| VRCPT-04 | Failure: ToneCard holds reason+body, tip below, action end/secondary start; Classic Warn content color; Error default parity | Robolectric + pure seam | `--tests "*OutcomeSheetReceiptFailureTest" --tests "*OutcomeSheetClassicParityTest"` | Wave 0 |
| VRCPT-05 | Pill text equals `"Handled by Local grammar · offline"`; dot color seam by `onDevice`; German 360dp | Robolectric NATIVE | `--tests "*OutcomeSheetReceiptSuccessTest" --tests "*OutcomeSheetReceiptLongLabelTest"` | Wave 0 |
| VTAG-01 | Each applicable Y-14 tag per Receipt state; Classic tags unchanged; no `testTagsAsResourceId` in `src/main` | Robolectric + scan | `--tests "*OutcomeSheetReceiptTagsTest" --tests "*ToneFoundationInvariantsTest"` | tags test Wave 0; invariants exists |
| VXR-01 | `liveRegion = Assertive` on `outcome_sheet_undo_refused` (Success + Failure, both styles), exactly one such node | Robolectric semantics | `--tests "*OutcomeSheetLiveRegionTest"` | Wave 0 |
| VXR-02 | `Failure.undo` renders undo-all + per-item rows (+ refused) in Classic and Receipt; Classic placement below the surface; Receipt below tip, above bar | Robolectric | Classic parity + Receipt failure classes | Wave 0 |
| API (all) | Signatures, defaults, v2.5.1 call shapes compile, arity pins updated, `toString` redaction | JUnit | `--tests "*OutcomeReceiptModelsTest" --tests "*OutcomeSheetSourceCompatV251Test" --tests "*VoiceModelLabelDefaultsTest" --tests "*DataClassBinaryCompatShimTest" --tests "*VoiceI18nSourceCompatTest"` | partly new |

Proof instruments (no pixels): pure `internal` palette seams (JUnit), `LocalContentColor` capture inside a slot (Classic Warn / ToneCard content), source contracts via `SourceContractTestSupport` (Error color lines, shape, dispatch order), unclipped `positionInRoot + size` geometry, `boundsInRoot.top` ordering with `useUnmergedTree = true`, `SemanticsMatcher.expectValue` for live region. Domain-vocabulary / registry guards: public top-level composables unchanged, so `DomainVocabularyDriftGuardTest`, `ComponentRegistryDriftGuardTest`, `ComponentStatesMatrixTest` stay green with no allowlist edit (they only inspect column-0 `@Composable` public functions; words like "undo"/"reason"/"handled" are not composable head tokens, and `OutcomeSheetDefaults.colors()` is an indented member). CATALOG/doc/README count updates are deferred to Phase 24.

### Sampling Rate

- **Per task commit:** the quick command for the touched test classes + `./gradlew apiDump` + `git diff` check (or `apiCheck`) + `detekt` when `src/main` changed.
- **Per plan merge:** `./gradlew testDebugUnitTest detekt apiCheck` full, one invocation.
- **Phase gate:** the 22-05 gate above, then `/gsd-verify-work` (Gate-1 tester).

### Wave 0 Gaps

- [ ] The new test classes in the structure list above (none exist yet).
- [ ] Edits to `VoiceModelLabelDefaultsTest` (3 assertions) and `DimensTest` (new token group).
- [ ] No framework install needed.

## Security Domain

`security_enforcement` is enabled (absent = enabled; `security_asvs_level: 1`, `security_block_on: high`). [VERIFIED: `.planning/config.json`]

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2 Authentication | no | n/a (presentational library) |
| V3 Session Management | no | n/a |
| V4 Access Control | no | `enabled` flags are presentation affordances, not access control (documented in `DynamicActionButton` KDoc) |
| V5 Input Validation | limited | All text arrives as caller params; rendered as `Text`, never parsed or formatted (no format/markup interpretation) |
| V6 Cryptography | no | none |
| V7 Error Handling and Logging | **yes** | Redact `Failure.toString` (transcript is user speech); do not log inside the library |
| V14 Configuration | yes | Library never sets `testTagsAsResourceId`; no `@HiltAndroidApp`/`@AndroidEntryPoint` (scanned by `ToneFoundationInvariantsTest`) |

### Known Threat Patterns

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| T-22-01: spoken `transcript` / `reason` leaks through generated data-class `toString` into logs, crash reports, recomposition traces | Information disclosure | `override fun toString()` omitting free text, mirroring `NeedsConfirmation` (T-12-01); test asserts the text never appears |
| T-22-02: stable test tags exposed as resource ids by the hub | Information disclosure / Tampering of test surface | Hub never sets `testTagsAsResourceId` (enforced scan); consumers opt in |
| T-22-03: a consumer-domain noun or import leaks into the hub (one-way dependency) | Elevation of design invariants | Import-prefix scan on the new files; no new public composable; English defaults only where precedent exists (`"Handled by"`) |
| T-22-04: Gate-1 waived or hand-written | Repudiation | Handoff only; no SELF-UAT written by the plan; device gaps parked for Yahir |
| T-22-05: accidental tag/push/repin publishes unreviewed commits | Tampering (supply chain) | No tag, no push, no consumer edits; `git tag --list "v2.6*"` empty in the gate |

## Sources

### Primary (HIGH confidence, read this session)
- `/home/yahir/Projects/Reusable/android/yahirandroidtaste/.planning/phases/22-outcomesheet-receipt-style/22-CONTEXT.md`; `.planning/REQUIREMENTS.md` (VRCPT/VTAG/VXR); `.planning/ROADMAP.md` Phase 22; `.planning/STATE.md`; `.planning/seams/v2.6.0-SKETCH.md` (full); `.planning/v2.6-DECISION-MAP.md` Phase 22
- Source: `component/OutcomeSheet.kt`, `model/VoiceOutcomeUiState.kt`, `model/HandledByUiModel.kt`, `model/FailureActionUiModel.kt`, `model/UndoAffordanceUiModel.kt`, `theme/ToneTokens.kt`, `component/ToneCard.kt`, `component/DynamicActionButton.kt`, `component/SheetScaffold.kt`, `component/PillChip.kt`, `component/RoundIconButton.kt`, `theme/Dimens.kt`, `api.txt` (307, 1080-1119, 1426-1515), `build.gradle.kts`, `gradle/libs.versions.toml`, `config/detekt*.yml`
- Tests: `OutcomeSheetTest`, `FailureRoleSourceContractTest`, `DataClassBinaryCompatShimTest`, `VoiceI18nSourceCompatTest`, `VoiceModelLabelDefaultsTest`, `DomainVocabularyDriftGuardTest`, `ToneFoundationInvariantsTest`, `ToneCardTest`, `ToneAtomsLongLabelTest`, `SegmentedOptionSelectorPillTest`, `SourceContractTestSupport`
- Tooling/docs: `tools/README-api-guard.md`, `tools/classify-hub-change.sh`, `tools/hooks/pre-commit`, `tools/verify-additive-diff.sh`, `AGENT-DEVICE-TESTING.md`, `~/.claude/gsd-core/workflows/verify-work-agentic.md`
- Prior phases: `20-04-SELF-UAT.md`, `21-01..04-SUMMARY.md`, `21-04-PLAN.md` (Task 3), `21-04-SELF-UAT.md`, `20/21-REVIEW-FIX.md`, `21-RESEARCH.md` (format precedent)
- Consumer specs: CT `76-UI-SPEC.md` (Y-3..Y-7, Y-14, S4/S5), SB `178-XR-01.md` items (a), (b)
- Context7 `/websites/developer_android_develop_ui_compose` (liveRegion semantics) and `/websites/developer_android_develop_ui_compose_testing` (`SemanticsMatcher.expectValue`)
- Local Gradle cache: `material-icons-extended-android/1.7.8` `classes.jar` listing (Undo, PriorityHigh present)

### Secondary (MEDIUM confidence)
- Hand-derived `api.txt` rewrite count and arity-pin values (A1, A2): consistent with the files but not machine-run.

### Tertiary (LOW confidence)
- TalkBack's exact announcement of a non-merged live-region subtree (A9): needs the owner's Gate-2 read-out.

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH, nothing new; every reused API was read.
- Architecture: HIGH for dispatch/seams/file split (repo precedent); MEDIUM for pinned-bar German behavior (P7, to be settled by the NATIVE-graphics test).
- Pitfalls: HIGH for P1-P6 and P12 (read from code/tests); MEDIUM for P7/P10 (runtime behavior not executed).

**Research date:** 2026-10-12
**Valid until:** 30 days for the repo facts (stable), but re-verify line numbers if Phase 23 lands first (it will rewrite `api.txt` and touch `Dimens.kt`/family screens, not `OutcomeSheet*`).
