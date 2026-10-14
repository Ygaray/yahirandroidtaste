# Phase 23: ProviderKeyCard lifecycle & Undo Center groups - Research

**Researched:** 2026-10-12
**Domain:** In-repo Jetpack Compose widening (source-compatible), Robolectric semantics proofs, Metalava `api.txt` discipline
**Confidence:** HIGH (everything below was read from the repo this session; the few `[ASSUMED]` items are design choices listed in the Assumptions Log)

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- **D-01 [signatures]:** `ProviderKeyCard` and its new types (`SavedKeyUiModel`, `ProviderKeyActions`, `ProviderKeyLabels`, `ProviderKeyDefaults`) are exactly as in `.planning/seams/v2.6.0-SKETCH.md` §1. The new params are appended after `providerLabel`. — **Reversibility:** one-way — signed off by CT and SB.
- **D-02 [compat]:** Source compatibility only: no hidden overloads or shims. Regenerate `api.txt` per API commit.
- **D-03 [card-behavior]:** The card stays stateless and presentational, with no persistence, validation or network (INV-01).
  - The key field follows `selectedProviderId`.
  - If `savedKeys[selected] != null && !editingSavedKey`, the field is replaced by the summary: a check icon plus `"{savedPrefix} · {fingerprint}"`, then Update | Clear.
  - Otherwise you get the field plus Save, and Cancel when `onCancelEdit != null`.
  - `inFlight` disables every action. `saveEnabled` gates Save.
  - Providers present in `savedKeys` get `labels.savedMarker` appended in the dropdown.
  - The raw `keyValue` never renders while the summary shows.
- **D-04 [tags]:** `provider_key_saved_summary` and `provider_key_action_{save,update,clear,cancel}`.
- **D-05 [key-buttons]:** Actions are `DynamicActionButton`s: Save = `Save` role, Clear = `Destructive`, Update/Cancel = `Neutral`. Each has a 48dp touch target. _(ai-auto)_
- **D-06 [undo-group-api]:** `UndoCenterScreen` gains `groups: List<UndoGroupRowUiModel> = emptyList()` and `onUndoGroup: ((String) -> Unit)? = null`, inserted before the trailing `onClear` so trailing-lambda call sites still compile. The new `UndoGroupRowUiModel(groupId, label, memberIds: List<String>, status: UndoGroupStatus, undoAllLabel: String = "Undo all")` goes in `feedback/` and reuses the existing `UndoGroupStatus`.
  - The screen stays stateless.
  - Each group row (label, member count, status, "Undo all" → `onUndoGroup(groupId)`) is followed by its member rows, indented.
  - Ungrouped entries stay flat.
  - Empty `groups` gives today's flat list.
  _(ai-auto)_ — **Reversibility:** costly — SB adopts the shape on its next repin.

### Claude's Discretion
- Group row styling within hub tokens. Whether "Undo all" hides when `onUndoGroup == null` or when the status says nothing is undoable.

### Deferred Ideas (OUT OF SCOPE)
None. This phase stays within its scope.
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| VKEY-01 | `ProviderKeyCard` gains `savedKeys`, `editingSavedKey`, `actions`, `labels` appended after `providerLabel`; defaults reproduce v2.5.1 | Seam functions + default-path characterization test (Architecture, Validation) |
| VKEY-02 | Per-provider summary/field switching, `savedMarker` in dropdown, raw key never rendered, stable tags | Summary/field mode seam, semantics-leak scan (Pitfall 1, Code Examples) |
| VUNDO-02 | `UndoCenterScreen` group row with label, member count, status, "Undo all" → callback; params before trailing `onClear`; default = flat list | Pure row-builder seam, compile fixture (Architecture, Pitfall 6) |
</phase_requirements>

## Summary

This is a pure in-repo widening of two existing public composables. No new dependency, no new public `@Composable`, no new Gradle plugin. `ProviderKeyCard` (component/ProviderKeyCard.kt, read this session) is a `Surface` + `Column` with a private `ProviderDropdown` and a `ClearableTextField`; it already carries a `@Deprecated(HIDDEN)` v2.4.x shim that must stay untouched. `UndoCenterScreen` (feedback/UndoCenterScreen.kt) is a `Scaffold` + `LazyColumn` of a private `UndoHistoryRow`. `UndoHistoryStore` already has every group primitive the screen needs (`openGroup`, `group`, `groupIdOf`, `groupLabel`, `groupStatus`, `attemptUndoGroup`) and `UndoGroupStatus` (Undoable / PartiallyResolved / FullyResolved / Empty) exists, so the screen only needs a stateless projection model and a row builder. [VERIFIED: feedback/UndoGroupTypes.kt:8-20, UndoHistoryStore.kt:115-133]

The hard parts are not the UI; they are (1) proving "the default call renders exactly as v2.5.1" without pixel capture, (2) proving the raw `keyValue` appears nowhere in the semantics tree while the summary shows, (3) adding params without breaking any positional/trailing-lambda call shape, and (4) not editing the existing tests (the roadmap says they pass unedited). Prescriptions: characterization (golden) tests written and committed BEFORE touching either composable; all new tests in NEW files; pure `internal` seam functions for mode selection, dropdown labels, summary text and the group-row builder; a semantics-tree scan helper that walks every node's config (unmerged tree) for the raw key.

Both edits rewrite existing `src/main` lines (adding a param after `providerLabel`, adding params before `onClear`, restructuring bodies), so `tools/classify-hub-change.sh --baseline v2.5.1` will report LANE 2; the commits need `HUB_LANE_OVERRIDE=2` with a stated reason (same as Phase 22 P1). [VERIFIED: tools/classify-hub-change.sh:26-37, tools/README-api-guard.md]

**Primary recommendation:** Build a Wave 0 of golden/characterization tests against the unmodified v2.5.1 composables, then widen `ProviderKeyCard` (plan A) and `UndoCenterScreen` (plan B) as two independent plans, each ending with `apiDump` + `apiCheck` + `detekt` in the same commit; leave Explorer, registry counts, `API.md`/`INTEGRATION.md`/`README.md` to Phase 24.

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Saved-key persistence, validation, network | Consumer app (out of library) | — | INV-01: card is presentational; keys and `editingSavedKey` are consumer state |
| Summary vs field mode selection | Library composable (pure seam fn) | — | Derived purely from props `savedKeys`, `selectedProviderId`, `editingSavedKey` |
| Fingerprint derivation (what string goes in `SavedKeyUiModel.fingerprint`) | Consumer app | — | Library renders the string verbatim; never sees a raw key in summary mode |
| Group membership / status / undo-all execution | `UndoHistoryStore` (library state holder) + consumer ViewModel | Consumer maps store -> `UndoGroupRowUiModel` | Store already owns it; screen is stateless (D-06) |
| Group row rendering, row ordering, indentation | Library composable (pure row builder) | — | Presentational |
| Stable test tags | Library | — | Y-14 style stable tags |

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Jetpack Compose (BOM 2026.04.01) + Material3 | in repo | UI | Already the stack; `ExposedDropdownMenuBox`, `Surface`, `Icon`, `Text` are already used in this file [VERIFIED: CLAUDE.md toolchain, ProviderKeyCard.kt:3-24] |
| `DynamicActionButton` + `ActionButtonDefaults.ActionButtonRole` + `ActionButtonEmphasis` | in repo (Phase 20) | Save/Update/Clear/Cancel buttons | D-05; signature: `DynamicActionButton(label, role, onClick, enabled = true, modifier = Modifier, emphasis: ActionButtonEmphasis? = null, icon: ImageVector? = null)` [VERIFIED: component/DynamicActionButton.kt:66-74] |
| Robolectric 4.16.1 + Compose ui-test (`createComposeRule`) | in repo | Tests | Existing harness, `@Config(sdk = [35])` [CITED: 22-VALIDATION.md] |
| Metalava (`apiDump`/`apiCheck`) | in repo | `api.txt` gate | Per-commit gate [VERIFIED: tools/README-api-guard.md] |

### Supporting
| Item | Purpose | When to Use |
|------|---------|-------------|
| `Dimens.HorizontalPadding` (16dp), `Dimens.ContentSpacing` (4dp), `Dimens.TouchTarget` (48dp) | Spacing / target tokens | Card layout and 48dp assertions [VERIFIED: theme/Dimens.kt:37,46,70] |
| `SourceContractTestSupport` (`source`, `stripComments`, function extraction) | Source-contract tests | Only if a source-text contract is wanted (see Validation) [VERIFIED: component/SourceContractTestSupport.kt] |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| New `ProviderKeySummary` public composable | Private composables inside `ProviderKeyCard.kt` | A new public composable would need registry/allowlist + DomainVocabulary entry; roadmap note says none is expected. Keep everything `private`. |
| strings.xml for default labels | English literals in `ProviderKeyDefaults.labels()` | The library has no `res/values` (only `res/font`); every existing default label is a caller-overridable English literal [VERIFIED: `ls src/main/res` -> `font`]. Follow that. |

**Installation:** none. **Package Legitimacy Audit:** not applicable, this phase installs no external packages (no `npm`/`pip`/`cargo`/Maven additions).

## Architecture Patterns

### System Architecture Diagram

```
Consumer ViewModel (owns keys, typed text, editingSavedKey, inFlight)
   |  props: providers, selectedProviderId, keyValue, savedKeys, editingSavedKey, actions, labels
   v
ProviderKeyCard
   |-- ProviderDropdown(providers, selectedProviderId, savedMarker for ids in savedKeys)
   |        `-- onProviderSelected(id) --------------------------> consumer switches provider
   |-- providerKeyMode(savedKeys, selectedProviderId, editingSavedKey)   [pure seam]
   |        |-- Summary -> SavedKeySummary(check icon + "Saved · fingerprint")  tag provider_key_saved_summary
   |        |                 `-- (actions != null) Update | Clear  -> onUpdate / onClear
   |        `-- Field   -> ClearableTextField(keyValue ...)   [unchanged code path]
   |                          `-- (actions != null) Save [+ Cancel if onCancelEdit != null]
   `-- (actions == null and savedKeys empty) == exactly the v2.5.1 tree

Consumer maps UndoHistoryStore -> UndoGroupRowUiModel(groupId,label,memberIds,status)
   v
UndoCenterScreen(entries, onNavigateBack, onUndo, groups, onUndoGroup, onClear)
   |-- buildUndoCenterRows(entries, groups)   [pure seam]  -> List<UndoCenterRow>
   |        entries order preserved (newest-first); first member encountered emits Group header,
   |        then all its members (indented); ungrouped entries flat; groups == empty -> all Entry rows
   `-- LazyColumn: GroupHeaderRow(label, count, status, "Undo all") -> onUndoGroup(groupId)
                   UndoHistoryRow(entry, indent) -> onUndo(entry.id)  [unchanged row]
```

### Recommended Project Structure
```
src/main/java/io/github/ygaray/yahirandroidtaste/
├── model/SavedKeyUiModel.kt, ProviderKeyActions.kt, ProviderKeyLabels.kt   # @Immutable data classes (sketch §1)
├── component/ProviderKeyCard.kt        # widened; adds ProviderKeyDefaults object (like OutcomeSheetDefaults in component/)
└── feedback/UndoGroupRowUiModel.kt     # new model (D-06 says feedback/)
    feedback/UndoCenterScreen.kt        # widened + internal row builder + private GroupHeaderRow
src/test/java/.../   # ALL NEW FILES (existing ProviderKeyCardTest / UndoCenterScreenTest stay byte-identical)
```
Placement of the three UI models in `model/` follows the sketch preamble ("UI models in `model/`") and `SheetActionUiModel`/`ProviderOptionUiModel` precedent; `ProviderKeyDefaults` goes next to the composable in `component/` (cf. `OutcomeSheetDefaults`, `ActionButtonDefaults`). [VERIFIED: seams/v2.6.0-SKETCH.md:11, model/SheetActionUiModel.kt:29, component/OutcomeSheetDefaults.kt:53] Planner may choose to keep the four types in one file; the package for the models is the only thing consumers see and `api.txt` will list it, so decide once.

### Pattern 1: Pure `internal` seams for every decision
**What:** put branching in small `internal` functions so tests assert the decision without a render.
```kotlin
// ProviderKeyCard.kt (internal, testable)
internal enum class ProviderKeyMode { Field, Summary }
internal fun providerKeyMode(
    savedKeys: Map<String, SavedKeyUiModel>, selectedProviderId: String?, editingSavedKey: Boolean
): ProviderKeyMode =
    if (selectedProviderId != null && savedKeys[selectedProviderId] != null && !editingSavedKey)
        ProviderKeyMode.Summary else ProviderKeyMode.Field
internal fun savedSummaryText(labels: ProviderKeyLabels, saved: SavedKeyUiModel) =
    "${labels.savedPrefix} · ${saved.fingerprint}"            // ONE string -> one Text node
internal fun providerMenuLabel(option: ProviderOptionUiModel, savedIds: Set<String>, labels: ProviderKeyLabels) =
    if (option.id in savedIds) "${option.label} · ${labels.savedMarker}" else option.label
```
(Names/format are `[ASSUMED]` design; the separator " · " matches the sketch's summary format. A11y: marker is plain text in the menu row.)

```kotlin
// UndoCenterScreen.kt
internal sealed interface UndoCenterRow {
    data class Group(val group: UndoGroupRowUiModel, val memberCount: Int) : UndoCenterRow
    data class Entry(val entry: UndoHistoryEntry, val indented: Boolean) : UndoCenterRow
}
internal fun buildUndoCenterRows(entries: List<UndoHistoryEntry>, groups: List<UndoGroupRowUiModel>): List<UndoCenterRow>
```
Algorithm (prescriptive): if `groups.isEmpty()` return `entries.map { Entry(it, indented = false) }` (guarantees the default path is today's list 1:1). Otherwise index `groupByMember = groups.flatMap { g -> g.memberIds.map { it to g } }.toMap()`; iterate `entries` in given order; for an entry whose id is in `groupByMember` and whose group was not yet emitted, emit `Group(g, memberCount = number of entries present whose id is in g.memberIds)` then every present member (in `entries` order) as `Entry(indented = true)`, mark the group emitted, and skip those members later; ungrouped entries emit as flat `Entry`. A group with zero members present in `entries` is not emitted (conditional-render, no dead space). Newest-first order is therefore anchored on each group's newest member. LazyColumn keys must stay unique: entries keep `key = entry.id`; group header key `"group-${groupId}"` (prefix cannot collide with UUID ids).

### Pattern 2: Hoist state above the branch
`var revealed by rememberSaveable { mutableStateOf(false) }` must stay declared unconditionally in the same position (before the Summary/Field `if`), not inside the Field branch, so the default path keeps identical composition-slot behavior and reveal state survives a Summary->Field flip within one card. Do NOT key it on `selectedProviderId`: that would change v2.5.1 default behavior (reveal currently persists across provider switches). [VERIFIED: ProviderKeyCard.kt:93]

### Anti-Patterns to Avoid
- **Editing `ProviderKeyCardTest` / `UndoCenterScreenTest` / `VoiceBinaryCompatShimTest` / `VoiceI18nSourceCompatTest`:** SC1 says they pass unedited. Put new tests in new files; at the phase gate print `git diff --stat v2.5.1 -- <those files>` and require empty.
- **A new hidden/old-arity overload or hand-written `copy$default`:** forbidden (D-02, CLAUDE.md). The existing v2.4.x hidden shim stays exactly as is; do not add a v2.5.1-descriptor shim.
- **`@HiltAndroidApp`, consumer imports, persistence:** invariants.
- **Putting `keyValue` into any semantics (`contentDescription`, `stateDescription`, `testTag`, `Text`) or any `toString`/log.**

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Action buttons with role colors, 48dp targets | Custom buttons | `DynamicActionButton` (D-05) | Already handles enabled, role hue and `minimumInteractiveComponentSize` on the emphasis path [VERIFIED: DynamicActionButton.kt:195-230 `.minimumInteractiveComponentSize()`] |
| Masked field + reveal + clear-x | New field | `ClearableTextField` + `RevealToggle` (unchanged call) | Existing, tested |
| Group status | New status enum | `UndoGroupStatus` | D-06 reuses it |
| Group lookup / undo-all | Anything in the screen | `UndoHistoryStore` (consumer calls `attemptUndoGroup`) | Screen is stateless |
| Relative timestamps | New formatter | Existing `RelativeTimestampText` inside `UndoHistoryRow` | Unchanged |

**Key insight:** every behavior is already a primitive in this repo; the phase is composition + proof, not new machinery.

## Runtime State Inventory

Not a rename/refactor/migration phase. Omitted.

## Common Pitfalls

### Pitfall 1: Raw key leaks into the semantics tree (the core VKEY-02 risk)
**What goes wrong:** `OutlinedTextField`/`EditableText` semantics always carry the raw (untransformed) text even when masked (the existing test KDoc says exactly this). If the field stays composed (e.g., hidden with alpha/size 0) while the summary shows, the raw key is in the tree. [VERIFIED: ProviderKeyCardTest.kt:31-38]
**How to avoid:** branch with a real `if/else` so the field is NOT composed in Summary mode (no `AnimatedVisibility` with kept state, no `Modifier.alpha(0f)`). Never reuse `keyValue` in a summary label. **Scan:** `composeTestRule.onRoot(useUnmergedTree = true)` then walk `fetchSemanticsNode()` recursively (`node.children`), and for every node assert `node.config.toList().toString()` (every key/value pair, including `Text`, `EditableText`, `ContentDescription`, `StateDescription`, `TestTag`, `Password`, `InputText`) does not contain the raw sentinel (use a distinctive `keyValue` like `"RAW-SENTINEL-0123456789"`). Run it in Summary mode, in Summary mode with the dropdown open, and in `inFlight` mode. Add a positive control: in Field mode the same scan DOES find the sentinel (proves the scan works and avoids a vacuous pass).
**Warning signs:** `provider_key_card_key_field` tag exists in Summary mode.

### Pitfall 2: Positional 4-arg `UndoCenterScreen(entries, back, undo, onClear)` stops compiling
**What goes wrong:** inserting defaulted `groups`/`onUndoGroup` before required `onClear` means a call passing four positional args binds the 4th to `groups` (type error). Trailing-lambda `UndoCenterScreen(e, b, u) { clear() }` and fully-named calls (SB `AppNavGraph.kt:870` is fully named [VERIFIED]) keep compiling. The signed-off order (D-06) accepts this; cover it in a compile fixture (trailing-lambda shape + all-named shape + new-named shape) and record the positional-4 break as an accepted owner flag in the SUMMARY. `[ASSUMED]` that no other consumer uses positional-4 (SB verified; CT does not use `UndoCenterScreen` in the grep of `/home/yahir/Projects/AndroidApps` [VERIFIED: grep only found SB]).

### Pitfall 3: "Renders exactly as v2.5.1" cannot be pixel-proven here
`captureToImage` times out in this environment. Prove structurally with a **characterization test written first** (Wave 0) against the unmodified code:
- ProviderKeyCard defaults: for 4 states (empty key + selected, entered/masked, `Invalid("...")`, empty providers) record the unmerged semantics tree as a normalized string (per node: testTag, Text, EditableText, ContentDescription, Role, `Password` presence, `isEnabled`; strip node ids; sorted depth-first) as inline golden strings, plus `getBoundsInRoot()` of `provider_key_card_surface` / dropdown / key field at a fixed width (`@Config(qualifiers = "w360dp-h800dp")`). Commit this test green BEFORE any `src/main` edit; it must stay green after, unmodified.
- UndoCenterScreen defaults: tree dump + row order for (a) 3 entries (Available/Undone/Failed), (b) empty list (EmptyState), (c) `groups = emptyList()` explicitly equals (a). Strip the relative-time `Text` from the dump (it is wall-clock dependent via `DateUtils`).
- Also assert **absence**: none of the new tags, no "Save"/"Update"/"Clear"/"Undo all" text, no extra nodes in the default tree.
- Plus a source contract (optional): default path calls the same private composables (mirrors 21-02/22 style).

### Pitfall 4: Summary shown but `actions == null`
D-03 is silent. Recommended (spec-literal): summary condition uses only `savedKeys`/`editingSavedKey`; the Update|Clear row and the Save/Cancel row render only when `actions != null`. This lets a consumer show a display-only summary, and keeps `actions = null` as "no button row (today)" per sketch. Flag in Open Questions (A1) because a card showing a summary with no way out may surprise SB.

### Pitfall 5: Dropdown marker affects existing assertions
Existing tests query `onNodeWithText("Anthropic")` for the anchor and the menu row [VERIFIED: ProviderKeyCardTest.kt:75,130]. Apply the marker ONLY to menu items, only for ids in `savedKeys` (so default calls are unchanged), and keep the anchor text field showing the plain label. New tests match with `substring = true`/exact on the full composed text ("Anthropic · saved"). `[ASSUMED]` format `"{label} · {savedMarker}"`.

### Pitfall 6: Semantics shape of the summary and buttons
- Put `provider_key_saved_summary` on a `Row(Modifier.semantics(mergeDescendants = true))` containing ONLY the check `Icon` (`contentDescription = null`, decorative) and one `Text` of the full `"Saved · sk-ant-…abcd"` string, so `assertTextEquals("Saved · sk-ant-…abcd")` works (P9 precedent: one string, one Text). Do not wrap the Update/Clear buttons inside the merged node (it would merge their text into the summary).
- Tag the `DynamicActionButton`s through its `modifier` param (`Modifier.testTag("provider_key_action_save")`). The emphasis path applies `.minimumInteractiveComponentSize()` AFTER the passed modifier so the tagged node is >= 48dp [VERIFIED: DynamicActionButton.kt:196-200]. On the legacy path (emphasis null, icon null) the M3 `Button`/`TextButton` also enforce the minimum interactive size (M3 default `LocalMinimumInteractiveComponentSize` = 48dp) `[ASSUMED]`; assert `>= 48dp` in a test either way (existing precedent: DynamicActionButtonEmphasisTest.kt:278).
- Disabled assertion: `assertIsNotEnabled()` plus click-never-fires (`performClick()` on a disabled button; counter stays 0).

### Pitfall 7: Emphasis choice is open
D-05 locks roles, not emphasis. "Depends on Phase 20 (emphasis for the action row only)" implies using emphasis. Recommended: `Save = Filled`, `Update = Tonal`, `Clear = Text` (Destructive hue), `Cancel = Text` (all pass `emphasis` explicitly; Save/Clear/Update/Cancel roles per D-05). `[ASSUMED]` visual choice, a UX item for Gate-1/owner; trivially changeable (one param). Alternative: leave `emphasis = null` (legacy path: filled Save, plain TextButtons).

### Pitfall 8: Long localized labels in the action row (memory lesson)
`ProviderKeyLabels` and `UndoGroupRowUiModel.undoAllLabel` are caller-overridable. At 360dp with German (`"Aktualisieren"`, `"Entfernen"`, `"Abbrechen"`, `"Alle rückgängig machen"`) the Update|Clear row and the group header must not clip or push a button off-screen. Layout prescription: summary row (icon + text, text `weight(1f)`, `maxLines = 2`) on its own line; the action row below it, buttons `Modifier.weight(1f, fill = false)` in a `Row(horizontalArrangement = Arrangement.spacedBy(8.dp))` (22-RESEARCH P7 escape hatch). Group header: label+count+status in a `Column(Modifier.weight(1f))` and the "Undo all" button as a non-weighted trailing item; if it does not fit, wrap the button under the text. Test with `@Config(sdk=[35], qualifiers="w360dp-h800dp")` + `@GraphicsMode(GraphicsMode.Mode.NATIVE)`: assert every action's `positionInRoot + size` within the 360dp window and no label ellipsized (precedent: ToneAtomsLongLabelTest, OutcomeSheetReceiptLongLabelTest).

### Pitfall 9: Group row English text is not localizable under D-06's shape
`UndoGroupRowUiModel` carries only `label` and `undoAllLabel`; the member count and status words ("3 changes", "Undoable"/"Partly undone"/"Undone") would be library-hard-coded English. This matches the screen today (`"Undo History"`, `"Clear history"`, `"Undone"`, `"Couldn't undo"` are already hard-coded [VERIFIED: UndoCenterScreen.kt:74,84,156-163]). Recommended: keep D-06's exact signature, render count as `"$n items"`/`"1 item"` and status via a private `groupStatusText(status)`; record "count/status not localizable" as an owner flag (adding defaulted `countLabel`/`statusLabel` fields later is source-compatible). `[ASSUMED]` wording; see Open Question Q2.

### Pitfall 10: Per-row/hold-to-peek gestures inside groups
`UndoHistoryRow` has a `pointerInput` hold-to-peek and tag `undo-row-{id}`. Indent member rows by wrapping with a padding `Modifier` passed into the row (add an optional `modifier: Modifier = Modifier` or `indent: Dp = 0.dp` to the PRIVATE `UndoHistoryRow`; applied outside the tagged `Box` so the tag/gesture node is unchanged for ungrouped rows). The group header must NOT carry the peek gesture. Existing tests tap `onNodeWithText("Undo")` expecting exactly one match with a single entry; in grouped tests the header's button label is "Undo all" and `onNodeWithText("Undo")` is exact-match so it will not collide, but use tags (`undo_group_undo_all_{groupId}` or similar) for group assertions. Suggested tags (`[ASSUMED]`, not in D-04): `undo-group-{groupId}` for the header, `undo-group-undo-all-{groupId}` for its button; keep the existing `undo-row-` kebab-case prefix style used in this file rather than snake_case, OR use snake_case to match Y-14; pick one and document (UI hint, internal-const like the existing ones).

### Pitfall 11: `clear history` and the group
The top-bar "Clear history" shows when any entry is non-Available (unchanged). Group rows add no new top-bar behavior; `clearSpent` store semantics are untouched [VERIFIED: UndoHistoryStoreTest has group clearSpent coverage].

### Pitfall 12: detekt / lint thresholds
Active config is `config/detekt/detekt.yml` overlaid by `config/detekt-compose.yml`: `LongParameterList` function/constructor threshold 18 with `ignoreDefaultParameters: true`; `LongMethod` 240; `CyclomaticComplexMethod` 26; plus `UnusedPrivateMember` and naming. [VERIFIED: config/detekt-compose.yml:29-42, config/detekt/detekt.yml] ProviderKeyCard (7 required + 7 defaulted) is fine; keep helper bodies small and avoid unused private members (every private helper must be referenced). Baseline is zero (`detekt-baseline.xml` is 5 lines) so fix findings, do not baseline. [VERIFIED: wc -l]

### Pitfall 13: `api.txt` churn
`api.txt` line 347 (`ProviderKeyCard` signature) and 818 (`UndoCenterScreen`) get REPLACED with widened signatures, and new classes (`SavedKeyUiModel`, `ProviderKeyActions`, `ProviderKeyLabels`, `ProviderKeyDefaults`, `UndoGroupRowUiModel`) appear (data classes also publish `copy`/`componentN`). [VERIFIED: api.txt:347, 818] Workflow per API commit: `./gradlew apiDump`, then `apiCheck`, commit `api.txt` with the change. Expect `git diff v2.5.1 -- api.txt` to remove exactly those two old signature lines and nothing else. The hidden v2.4.x shim is `@Deprecated(HIDDEN)`, so it does not appear in `api.txt`; unaffected.

### Pitfall 14: Worktree drift-guard blind spot
`workflow.use_worktrees` is true; `ComponentRegistryDriftGuardTest`/`DomainVocabularyDriftGuardTest` walk up to the first directory named `yahirandroidtaste`, i.e. the main checkout, not an agent worktree (22-RESEARCH P12). Require one full `./gradlew testDebugUnitTest` in the main checkout after merge. [CITED: 22-RESEARCH.md P12]

## Code Examples

### Summary vs Field branch (sketch of the body)
```kotlin
// Source: seams/v2.6.0-SKETCH.md §1 + existing ProviderKeyCard.kt body (field code path unchanged)
Column(Modifier.padding(Dimens.HorizontalPadding)) {
    ProviderDropdown(providers, selectedProviderId, onProviderSelected, emptyProvidersReason,
        providerLabel, savedIds = savedKeys.keys, labels = labels, modifier = /* unchanged tag */)
    var revealed by rememberSaveable { mutableStateOf(false) }          // stays here, unconditional
    val saved = selectedProviderId?.let { savedKeys[it] }
    if (providerKeyMode(savedKeys, selectedProviderId, editingSavedKey) == ProviderKeyMode.Summary && saved != null) {
        SavedKeySummary(text = savedSummaryText(labels, saved), actions = actions, labels = labels)
    } else {
        ClearableTextField(/* byte-identical to v2.5.1 arguments */)
        if (actions != null) FieldActionRow(actions, labels)                // Save [+ Cancel]
    }
}
```
Notes: `ProviderKeyActions` in `actions == null` and `savedKeys.isEmpty()` takes the `else` branch with no extra node, so the tree equals v2.5.1 (verify with the Wave 0 golden). Save `enabled = actions.saveEnabled && !actions.inFlight`; Update/Clear/Cancel `enabled = !actions.inFlight`; Update rendered iff `actions.onUpdate != null`, Clear iff `actions.onClear != null`, Cancel iff `actions.onCancelEdit != null`.

### Raw-key semantics scan helper (test)
```kotlin
// Source: pattern derived from ProviderKeyCardTest.kt unmerged-tree usage (useUnmergedTree = true)
private fun SemanticsNode.dump(sb: StringBuilder = StringBuilder()): StringBuilder {
    sb.append(config.joinToString { (k, v) -> "${k.name}=$v" }).append('\n')
    children.forEach { it.dump(sb) }
    return sb
}
val tree = composeTestRule.onRoot(useUnmergedTree = true).fetchSemanticsNode().dump().toString()
assertFalse(tree.contains(RAW_KEY))
```
(Also open the dropdown (`performClick` on `provider_key_card_dropdown`) and re-scan: popups are separate roots; use `onAllNodes(isRoot(), useUnmergedTree = true)` and dump each.)

### Compile fixture for source compatibility (new file, mirrors VoiceI18nSourceCompatTest)
```kotlin
// Source: VoiceI18nSourceCompatTest.kt:38-55 (v2.4/2.5 positional shape) + SB VoiceAgentProviderCards.kt:67-80
// v2.5.1 SB-shape named call (no new params), v2.4-shape fully positional call through emptyProvidersReason,
// UndoCenterScreen all-named v2.5.1 shape, UndoCenterScreen trailing-lambda shape, and the new-named shape.
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Consumers draw their own Save/Configured rows under `ProviderKeyCard` (CT SettingsScreen.kt:641, SB VoiceAgentProviderCards.kt:67) | Card owns the lifecycle via opt-in `actions` | v2.6.0 | CT can delete its dead dropdown rows; SB stays unchanged (defaults) |
| Flat Undo Center list | Optional group rows | v2.6.0 | SB maps `UndoHistoryStore` groups -> `UndoGroupRowUiModel` on repin |

**Deprecated/outdated:** none relevant.

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | Summary shows even when `actions == null` (button row only when `actions != null`) | Pitfall 4 | A card could show a summary with no way to leave it; if owner wants summary gated on `actions != null`, one condition changes |
| A2 | Dropdown marker format `"{label} · {savedMarker}"`, menu items only, anchor unchanged | Pitfall 5 | Cosmetic; tests depend on the exact string |
| A3 | Button emphasis Save=Filled, Update=Tonal, Clear=Text, Cancel=Text | Pitfall 7 | Visual only (UX-conscious owner); one-line change |
| A4 | Group header count wording/status wording and tag names are library English / chosen here | Pitfalls 9-10 | Not localizable under D-06; tag names may need alignment with SB tests |
| A5 | M3 legacy `Button`/`TextButton` enforce 48dp interactive size | Pitfall 6 | Only relevant if emphasis stays null; covered by a bounds test |
| A6 | "Undo all" visibility rule: show iff `onUndoGroup != null` and status is Undoable or PartiallyResolved; FullyResolved hides it (Empty groups not rendered) | Architecture, Open Q3 | Claude's-discretion item; if owner prefers always visible, tests change |
| A7 | Gate-1 (device) is required for this phase (UI hint yes; CONTEXT sets no `device_exempt`) | Validation | If exempt, drop the handoff plan; if required, a tester-device probe pass follows (22-05 precedent) |
| A8 | Member count = number of group members present in `entries` (not `memberIds.size`) | Architecture | Differs only if `memberIds` contains evicted ids (store evicts groups whole, so equal in practice) |

## Open Questions

1. **Summary with `actions == null` (A1).** Recommendation: render the summary from props alone; buttons only with `actions`. Planner may confirm with orchestrator; default is the recommendation.
2. **Localizable group count/status text (Pitfall 9).** D-06 fixes the model; recommendation: keep it and record an owner flag. Decision cost now is low (defaulted fields are source-compatible), but it deviates from the signed shape, so only add it if the orchestrator approves.
3. **"Undo all" visibility (A6).** Discretion item; recommendation above.
4. **Gate-1 device pass (A7).** Confirm with orchestrator whether Phase 23 needs the tester-device pass like Phase 22 (`phase.has-uat-criteria`); never waive or hand-write a SELF-UAT (memory: certify-parks-agentic-gaps). Phase 24 owns the Explorer demos and screencaps, so Gate-1 for 23 would need a throwaway probe activity (22 precedent) since no Explorer demo exists yet.
5. **Reveal state across providers.** `revealed` persists across provider switches in v2.5.1; leaving it untouched keeps parity. Raise only if the owner objects to a revealed field staying revealed after switching.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| JDK | Gradle build/tests | yes | OpenJDK 17.0.19 | — |
| `./gradlew` | build/tests/apiDump/apiCheck/detekt | yes (file present; not executed this session) | wrapper | — |
| Git tags `v2.5.1`, `v2.5.0` | baseline diffs, classifier | yes | `git tag --list` shows both; no `v2.6*` tag | — |
| Android device (tester R5CT10XNKQN) | Gate-1 only | not probed (not needed for planning) | — | Park gap for Yahir; never waive |

No missing blocking dependency. Host constraint: ONE gradle invocation at a time (22-VALIDATION).

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit4 + Robolectric 4.16.1 + Compose ui-test (`createComposeRule`), `@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [35])`; text geometry: `@Config(sdk=[35], qualifiers="w360dp-h800dp")` + `@GraphicsMode(GraphicsMode.Mode.NATIVE)` |
| Config file | `build.gradle.kts` (`testOptions.unitTests.isIncludeAndroidResources = true`) |
| Quick run command | `./gradlew testDebugUnitTest --tests "io.github.ygaray.yahirandroidtaste.component.ProviderKey*" --tests "io.github.ygaray.yahirandroidtaste.feedback.Undo*"` (batch all filters for a task in ONE invocation) |
| Full suite command | `./gradlew apiDump testDebugUnitTest detekt apiCheck` then `bash tools/test/run-all.sh` |

No pixel capture (`captureToImage` times out): semantics, bounds, source-contract proofs only. One `setContent` per test method.

### Phase Requirements -> Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| VKEY-01 (SC1) | Default call tree + bounds equal the v2.5.1 golden; no new tags/texts; existing 4 test classes unedited | characterization + `git diff v2.5.1 --stat` | `--tests "*ProviderKeyCardDefaultParityTest" --tests "*ProviderKeyCardTest" --tests "*VoiceBinaryCompatShimTest" --tests "*VoiceI18nSourceCompatTest"` | ❌ Wave 0 (`ProviderKeyCardDefaultParityTest`); others ✅ |
| VKEY-01 | Summary/field/Save/Update/Clear/Cancel render rules; `saveEnabled`; null callbacks hide buttons; `inFlight` disables all and clicks never fire; buttons >= 48dp; roles | Robolectric semantics + clicks + pure seam | `--tests "*ProviderKeyCardLifecycleTest"` | ❌ Wave 0 |
| VKEY-02 (SC3) | Switching `selectedProviderId` across 3 providers (A saved, B unsaved, C saved) shows own summary/field; `savedMarker` on menu items for saved ids only; summary text exactly "Saved · sk-ant-…abcd"; raw key found nowhere (summary, dropdown open, inFlight) with positive control in Field mode | Robolectric + tree scan | `--tests "*ProviderKeyCardPerProviderTest" --tests "*ProviderKeyCardRawKeySemanticsTest"` | ❌ Wave 0 |
| VKEY (i18n) | German/Spanish labels at 360dp stay in window, no ellipsis | Robolectric NATIVE | `--tests "*ProviderKeyCardLongLabelTest"` | ❌ Wave 0 |
| VUNDO-02 (SC1) | Default flat list tree/order identical (3-entry + empty + `groups = emptyList()`); existing `UndoCenterScreenTest`/`UndoHistoryStoreTest` unedited | characterization | `--tests "*UndoCenterScreenDefaultParityTest" --tests "*UndoCenterScreenTest" --tests "*UndoHistoryStoreTest"` | ❌ Wave 0 (`UndoCenterScreenDefaultParityTest`); others ✅ |
| VUNDO-02 (SC4) | Group header shows label, count, status text for Undoable/PartiallyResolved/FullyResolved; members follow indented; ungrouped flat; "Undo all" fires `onUndoGroup(groupId)` once; hidden when callback null / nothing undoable; real `UndoHistoryStore` groups end-to-end (`openGroup` + grouped `append` + `groupStatus`) | pure seam + Robolectric + click | `--tests "*UndoCenterRowsTest" --tests "*UndoCenterScreenGroupsTest"` | ❌ Wave 0 |
| API (compat) | v2.5.1 SB/CT call shapes + trailing-lambda `UndoCenterScreen` compile; new named shapes compile | compile-only | `--tests "*ProviderKeyUndoSourceCompatV251Test"` | ❌ Wave 0 |
| API gate | `apiDump` then `apiCheck` per API commit; `git diff v2.5.1 -- api.txt` removes only the two old signature lines | Gradle | `./gradlew apiDump apiCheck` | ✅ |
| Detekt | zero baseline, no new findings | Gradle | `./gradlew detekt` | ✅ |

### Sampling Rate
- **Per task commit:** that task's `--tests` filters in ONE gradle invocation (`apiDump` first when public API changed), then `apiCheck` and `detekt` when `src/main` changed.
- **Per plan:** `./gradlew apiDump testDebugUnitTest detekt apiCheck`.
- **Phase gate:** full unfiltered suite + detekt + apiCheck green in the MAIN checkout; `api.txt` equals a fresh `apiDump`; `bash tools/test/run-all.sh`; `bash tools/classify-hub-change.sh --baseline v2.5.1` (expect LANE 2 -> `HUB_LANE_OVERRIDE=2` with reason); the 4 protected test files byte-identical to `v2.5.1`; no `@Deprecated(HIDDEN)` added since v2.5.1; no `v2.6*` tag; nothing pushed; ComponentRegistry/Explorer/README/API.md/INTEGRATION.md untouched (Phase 24).

### Wave 0 Gaps
- [ ] `ProviderKeyCardDefaultParityTest` and `UndoCenterScreenDefaultParityTest` (golden dumps), authored and run GREEN against UNMODIFIED code, committed before any `src/main` edit.
- [ ] Shared test support (new file, e.g. `SemanticsDump.kt`) with the unmerged-tree dumper + raw-key scan helper + bounds helper.
- [ ] The remaining new test classes listed above; no framework install needed (harness present).

## Security Domain

`security_enforcement` is enabled (ASVS level 1, block on high) [VERIFIED: .planning/config.json].

### Applicable ASVS Categories
| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2/V3 Authentication/Session | no | n/a |
| V4 Access Control | no | n/a |
| V5 Input Validation | limited | `onKeyChange` keeps trimming only (pure formatting, INV-01); no validation added |
| V6 Cryptography | no | library never hashes/stores; fingerprint is consumer-supplied |
| V7 Error Handling & Logging | yes | no `toString`/log of `keyValue`; new models must not embed a raw key (SavedKeyUiModel carries fingerprint only); no `Log` calls |
| V8 Data Protection | yes | raw key never in semantics (Summary mode), no persistence, `rememberSaveable` holds only the reveal boolean |

### Known Threat Patterns
| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| T-23-01 raw key exposed to accessibility/semantics while summary is shown | Information disclosure | Field not composed in Summary mode; tree scan test with positive control |
| T-23-02 consumer passes a raw key as `fingerprint` | Information disclosure | Cannot be enforced by a presentational card; document in KDoc ("fingerprint only, never a raw key"); Phase 24 docs |
| T-23-03 `inFlight` double-submit | Tampering | All actions disabled while `inFlight`; click-never-fires test |
| T-23-04 key logged via data-class `toString` | Information disclosure | New data classes hold no key; verify none adds `keyValue` (keyValue stays a composable param, not a model field) |

## Project Constraints (from CLAUDE.md)

- One-way dependency: library imports no host code; no secrets; no domain nouns. Keep `ProviderKeyDefaults` labels generic.
- Bindings-only Hilt: never add `@HiltAndroidApp`/`@AndroidEntryPoint`.
- `ComponentRegistry` drift guard: any new PUBLIC `@Composable` must be registered XOR allowlisted (none planned; keep helpers `private`). `DomainVocabularyDriftGuardTest` scans only public top-level `@Composable` names (head token must be a primitive noun or acknowledged domain word); `Provider` is already acknowledged [VERIFIED: DomainVocabularyDriftGuardTest.kt:321-325]. No allowlist edit is needed unless a new public composable is introduced; `key`/`fingerprint` in data-class or label names are not scanned.
- Source-compatible only: append params with defaults, trailing lambdas last, never append after a trailing lambda; no new `@Deprecated(HIDDEN)` overloads / hand-written `copy$default`; Metalava `apiCheck` is the per-commit gate; `tools/verify-binary-abi.sh` informational only.
- Detekt zero baseline; fix or tune with justification, never re-baseline.
- Shipping is human-gated: no tag, no push, no consumer repin or consumer-file edits; commit on `main` in the hub (sequential-in-hub); never `main-SNAPSHOT`.
- Gradle: single-module root, commands without module prefix; JDK 17; `minSdk 35`.
- Memory lessons binding here: test long German/Spanish at ~360dp before layout sign-off; source-compat not binary; a tag push publishes unpushed commits (no tags this phase); Gate-1 device gaps are parked for Yahir, never waived; `captureToImage` unusable here.
- Staging hygiene (from 22-05): stage only the task's files; never `git add .planning/` wholesale; never stage `.planning/graphs/*`, `.planning/uat-pending/16*`..`18*`, `.planning/WAITING.json`, `.planning/milestones/v2.5-phases/19-cut-v2-5-0/.gate/`. [CITED: 22-05-PLAN.md]

## Sources

### Primary (HIGH confidence, read this session)
- `.planning/phases/23-.../23-CONTEXT.md`; `.planning/seams/v2.6.0-SKETCH.md` §1; `.planning/REQUIREMENTS.md` (VKEY-01/02, VUNDO-02); `.planning/ROADMAP.md` Phase 23; `.planning/STATE.md`
- `src/main/.../component/ProviderKeyCard.kt`, `ClearableTextField.kt`, `DynamicActionButton.kt`, `OutcomeSheetDefaults.kt`; `model/ProviderOptionUiModel.kt`, `KeyFieldState.kt`, `SheetActionUiModel.kt`
- `src/main/.../feedback/UndoCenterScreen.kt`, `UndoGroupTypes.kt`, `UndoHistoryEntry.kt`, `UndoHistoryStore.kt`
- `src/main/.../explorer/VoiceCommandFamilyScreen.kt`, `FeedbackFamilyScreen.kt`, `ExplorerFakeData.kt`
- `src/test/.../ProviderKeyCardTest.kt`, `UndoCenterScreenTest.kt`, `VoiceBinaryCompatShimTest.kt`, `VoiceI18nSourceCompatTest.kt`, `DomainVocabularyDriftGuardTest.kt`, `SourceContractTestSupport.kt`
- `config/detekt/detekt.yml`, `config/detekt-compose.yml`, `tools/classify-hub-change.sh`, `tools/README-api-guard.md`, `api.txt` (lines 347, 818), `.planning/config.json`
- Consumers (read-only): SB `VoiceAgentProviderCards.kt:67-80`, SB `AppNavGraph.kt:870`, CT `SettingsScreen.kt:641-650`
- Phase 22 artifacts: `22-RESEARCH.md` (P1-P13), `22-VALIDATION.md`, `22-05-PLAN.md`

### Secondary / Tertiary
- None (no web research; pure in-repo phase).

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH, no new libraries; all APIs read in-repo.
- Architecture: HIGH for seams and ordering; MEDIUM for exact visual layout (Gate-1 / owner).
- Pitfalls: HIGH for 1-6, 12-14 (read from code/config); MEDIUM for 7-10 (design choices, runtime not executed).

**Research date:** 2026-10-12
**Valid until:** 2026-11-11 (repo-internal; invalidated only by edits to the files listed above)
