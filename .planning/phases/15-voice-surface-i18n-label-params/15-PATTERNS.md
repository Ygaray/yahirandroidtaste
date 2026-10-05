# Phase 15: Voice-surface i18n label params - Pattern Map

**Mapped:** 2026-10-05
**Files analyzed:** 15 (5 composables, 4 models, 5 test files, api.txt) + 1 optional new test
**Analogs found:** all (in-repo precedent for every recipe)

Base: `src/main/java/io/github/ygaray/yahirandroidtaste/` (main), `src/test/java/io/github/ygaray/yahirandroidtaste/` (test). All analogs git-tracked.

## File Classification

| File | Role | Data Flow | Analog | Match |
|------|------|-----------|--------|-------|
| `component/ProviderKeyCard.kt` | component | request-response (props) | itself (`emptyProvidersReason` last-param precedent, `:69-71`) | exact |
| `component/ModelSelectCard.kt` | component | props | `ProviderKeyCard.kt` | exact |
| `component/ClarificationBar.kt` | component | props | `ProviderKeyCard.kt` | role-match |
| `component/ApproachLadderCard.kt` | component | props | `ProviderKeyCard.kt` + threading into private helper | role-match |
| `model/{HandledBy,UndoRow,UndoRefused,ProposedItem}UiModel.kt` | model | data carrier | `model/TagChipUiModel.kt:78` (`@JvmOverloads constructor`) + RESEARCH recipe | partial (copy half is new) |
| `component/OutcomeSheet.kt` | component | props via model | itself (private `UndoRowItem`/`HandledByRow`/etc.) | exact |
| `component/*Test.kt` (5 existing) | test | Compose UI | `ClarificationBarTest.kt:20-60` | exact |
| `model/VoiceModelLabelDefaultsTest.kt` (new) | test | JVM unit | `model/TagChipUiModelTest.kt` | role-match |
| `component/VoiceI18nSourceCompatTest.kt` (optional new) | test | compile-only | `component/ShowTagColorsSourceCompatTest.kt` | exact |
| `api.txt` | config | generated | `./gradlew apiDump` | n/a |

## Pattern Assignments

### Composables (VI18N-01/02/03): append defaulted String params LAST

**Recipe** (RESEARCH 15-RESEARCH.md "Composable param append"; `apiCheck` verified green):
```kotlin
fun ProviderKeyCard(
    /* existing params unchanged */
    modifier: Modifier = Modifier,
    emptyProvidersReason: String = "No providers configured yet",
    providerLabel: String = "Provider"   // NEW, last
) { ... ProviderDropdown(..., providerLabel = providerLabel) }
// private ProviderDropdown: add NON-default `providerLabel: String`; `label = { Text(providerLabel) }`
```
Per-target (after `modifier`, or after `emptyProvidersReason` for ProviderKeyCard):
- `ProviderKeyCard.kt:147` -> `providerLabel`; `ModelSelectCard.kt:112` -> `modelLabel` (thread to `ModelDropdown`)
- `ClarificationBar.kt:92` `Text("Dismiss")` -> `dismissLabel: String = "Dismiss"`
- `ApproachLadderCard.kt:111` `listOf(onlineLabel, offlineOnlyLabel)`; `:157/164/171` into private `RungRow` via non-default `unavailableLabel/cappedLabel/needsNetworkLabel`. Order: unavailable, capped, needsNetwork, online, offlineOnly (P17 appends after).
- Defaults live ONLY on the public signature (no duplicated defaults in private helpers).

### Models (VI18N-04): `@JvmOverloads` + hand-written old-arity `copy`

**Analog:** `model/TagChipUiModel.kt:78-83` (`data class X @JvmOverloads constructor(... defaulted tail)`). Copy half from RESEARCH (reproduced green):
```kotlin
data class UndoRowUiModel @JvmOverloads constructor(
    val id: String, val label: String, val state: UndoRowState,
    val undoneLabel: String = "Undone"
) {
    fun copy(id: String, label: String, state: UndoRowState): UndoRowUiModel =
        copy(id, label, state, undoneLabel)
}
```
Do NOT use TagChipUiModel's body-`var` approach (loses stability). Per model:
- `HandledByUiModel`: `escalationsLabel: String = "Escalations:"`; old `copy(tier, approach, provider, model, escalationCount)` -> delegate adding label
- `UndoRefusedUiModel`: `refusedPrefix = "Couldn't undo:"`, `changedSinceSuffix = "changed since"` (in that order after `changedItem`); old `copy(reason, changedItem)`
- `UndoRowUiModel`: `undoneLabel = "Undone"`; `UndoRowState.Undone` data object untouched (D-02)
- `ProposedItemUiModel`: `removeContentDescription = "Remove"`; old 7-arg `copy`; keep existing `toString()` unchanged (pinned by `OutcomeSheetTest.kt:463-470`)

### `component/OutcomeSheet.kt` (consume model fields; no signature change)
- `:176-178` `val suffix = refused.changedItem?.let { ", $it ${refused.changedSinceSuffix}" } ?: ""`; `Text("${refused.refusedPrefix} ${refused.reason}$suffix")`
- `:229-230` `text = row.undoneLabel`
- `:330` `add("${handledBy.escalationsLabel} $it")`
- `:433` `contentDescription = item.removeContentDescription` (use the actual local name)

### Tests
**Analog:** `component/ClarificationBarTest.kt:20-60` harness:
```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ClarificationBarTest {
    @get:Rule val composeTestRule = createComposeRule()
    @Test fun `...`() {
        composeTestRule.setContent { ClarificationBar(question = "Which list?", options = ..., onSelect = {}, onDismiss = {}) }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Which list?").assertExists()
    }
}
```
Override-test pattern: pass a non-English sentinel (e.g. "Fermer", "Fournisseur"), assert it exists, and assert the English default `assertDoesNotExist()`; keep default-English assertions (regression net: `OutcomeSheetTest.kt:203,325,379`, `ApproachLadderCardTest.kt:77,130,202,228`). Use `useUnmergedTree = true` for text-field labels and remove icon (`outcome_sheet_confirmation_item_remove` tag). Toggle: `onNodeWithContentDescription("<offlineOnlyLabel>, not selected")`.
Model JVM test: assert defaults and that `copy(a,b,c)` preserves a custom new-field value (model on `TagChipUiModelTest.kt`). Optional compile-only fixture modeled on `ShowTagColorsSourceCompatTest.kt` (named, positional-through-modifier, old-arity ctor/copy).

## Shared Patterns

- **Additive gate:** `./gradlew apiDump` in the SAME commit as src change, then `./gradlew apiCheck` (authoritative, D-01). Commit with `HUB_LANE_OVERRIDE=3` on all P15 commits (must equal detected lane exactly; pre-commit diffs HEAD vs `v2.4.1`, so even docs/test-only commits are lane 3 after the first api.txt change). Source commit without regenerated api.txt would classify lane 2.
- **Detekt zero baseline:** `./gradlew detekt`; defaults ignored by LongParameterList (limit 18).
- **Do not touch:** `explorer/` call sites (D-03), `SegmentedOptionSelector.kt:65` a11y English (record as residual), `UndoCenterScreen.kt:159`, `ComponentRegistry` (no new public composable), `ProposedItemUiModel.toString`.
- **No localized resources** in library (INV-01); no tag, no consumer edits.
- Commit trailer: `Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>`.

## No Analog Found
None. The hand-written old-arity `copy` overload has no prior in-repo instance (TagChipUiModel rejected it); RESEARCH reproduced it green in a scratch copy.

## Metadata
**Analog search scope:** model/, component/, src/test component+model; tracked-source gate verified via `git ls-files`.
**Pattern extraction date:** 2026-10-05
