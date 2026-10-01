package io.github.ygaray.yahirandroidtaste.explorer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.component.ActionButtonDefaults
import io.github.ygaray.yahirandroidtaste.component.ApproachLadderCard
import io.github.ygaray.yahirandroidtaste.component.ClarificationBar
import io.github.ygaray.yahirandroidtaste.component.ModelSelectCard
import io.github.ygaray.yahirandroidtaste.component.OutcomeSheet
import io.github.ygaray.yahirandroidtaste.component.ProviderKeyCard
import io.github.ygaray.yahirandroidtaste.model.ApproachRungUiModel
import io.github.ygaray.yahirandroidtaste.model.ClarificationOptionUiModel
import io.github.ygaray.yahirandroidtaste.model.FailureActionUiModel
import io.github.ygaray.yahirandroidtaste.model.HandledByUiModel
import io.github.ygaray.yahirandroidtaste.model.KeyFieldState
import io.github.ygaray.yahirandroidtaste.model.ModelOptionUiModel
import io.github.ygaray.yahirandroidtaste.model.ProposedItemUiModel
import io.github.ygaray.yahirandroidtaste.model.ProviderOptionUiModel
import io.github.ygaray.yahirandroidtaste.model.SelectionMode
import io.github.ygaray.yahirandroidtaste.model.UndoAffordanceUiModel
import io.github.ygaray.yahirandroidtaste.model.UndoRefusedUiModel
import io.github.ygaray.yahirandroidtaste.model.UndoRowState
import io.github.ygaray.yahirandroidtaste.model.UndoRowUiModel
import io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode

/**
 * D-05: this family's slice of [ComponentRegistry.entries], declared here (not in
 * `ComponentRegistry.kt`) so this file stays the single place this plan enriches Voice
 * Command's `states`/`content` without touching the shared registry file — mirrors
 * `pickersFamilyEntries` in [PickersFamilyScreen].
 *
 * 13-prep groundwork (Phase 10 Plan 01, D-01): this is the NEW tenth registry family. It held
 * ONE entry ([ProviderKeyCard], VSET-01) after Plan 01; Plan 02 appends `ModelSelectCard`
 * (VSET-02) and `ApproachLadderCard` (VAPPR-01/02/03) here.
 */
internal val voiceCommandFamilyEntries: List<ComponentRegistry.Entry> = listOf(
    ComponentRegistry.Entry(
        name = "ProviderKeyCard",
        family = ExplorerFamilies.VOICE_COMMAND,
        states = listOf(
            ComponentRegistry.StateCell(
                "Default",
                render = { ProviderKeyCardFixture(keyValue = "", keyState = KeyFieldState.Empty) }
            ),
            ComponentRegistry.StateCell(
                "Pressed / Selected",
                render = {
                    ProviderKeyCardFixture(keyValue = "sk-fixture-key-value", keyState = KeyFieldState.Valid)
                }
            ),
            // ProviderKeyCard has no `enabled`/disabled param — every control is hideable via
            // null props (D-05), not disabled-but-shown — N/A.
            ComponentRegistry.StateCell("Disabled"),
            ComponentRegistry.StateCell(
                "Focused",
                render = {
                    ProviderKeyCardFixture(
                        keyValue = "sk-bad-fixture",
                        keyState = KeyFieldState.Invalid("Key rejected by provider")
                    )
                }
            )
        ),
        content = { ProviderKeyCardVariants() },
        tier = ComponentRegistry.Tier.PATTERN
    ),
    ComponentRegistry.Entry(
        name = "ModelSelectCard",
        family = ExplorerFamilies.VOICE_COMMAND,
        states = listOf(
            ComponentRegistry.StateCell(
                "Default",
                render = { ModelSelectCardFixture(selectedModelId = fixtureModels.first().id) }
            ),
            ComponentRegistry.StateCell(
                "Pressed / Selected",
                render = { ModelSelectCardFixture(selectedModelId = fixtureModels.last().id) }
            ),
            // The empty-models disabled+reason affordance is ModelSelectCard's only "unavailable"
            // state (D-05 — hideable-by-null-prop convention doesn't apply here; `models` itself
            // signals unavailability), so this cell doubles as the Disabled preview.
            ComponentRegistry.StateCell(
                "Disabled",
                render = {
                    ModelSelectCard(
                        models = emptyList(),
                        selectedModelId = null,
                        onModelSelected = {},
                        emptyReason = "Set a provider and key first",
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    )
                }
            ),
            // ModelSelectCard's dropdown has no custom focus-visual override — N/A.
            ComponentRegistry.StateCell("Focused")
        ),
        content = { ModelSelectCardVariants() },
        tier = ComponentRegistry.Tier.PATTERN
    ),
    ComponentRegistry.Entry(
        name = "ApproachLadderCard",
        family = ExplorerFamilies.VOICE_COMMAND,
        states = listOf(
            ComponentRegistry.StateCell(
                "Default",
                render = { ApproachLadderCardFixture() }
            ),
            ComponentRegistry.StateCell(
                "Pressed / Selected",
                render = { ApproachLadderCardFixture(initialOfflineOnly = true) }
            ),
            // The max-tier cap greyed-but-present treatment is ApproachLadderCard's "unavailable"
            // posture (D-05 — the cap itself is hideable by null props, not disabled-but-shown),
            // so this cell previews a capped rung instead of a literal `enabled = false` prop.
            ComponentRegistry.StateCell(
                "Disabled",
                render = { ApproachLadderCardFixture(initialMaxTierId = fixtureLadder.last().id) }
            ),
            // ApproachLadderCard's rung rows have no custom focus-visual override — N/A.
            ComponentRegistry.StateCell("Focused")
        ),
        content = { ApproachLadderCardVariants() },
        tier = ComponentRegistry.Tier.PATTERN
    ),
    ComponentRegistry.Entry(
        name = "OutcomeSheet",
        family = ExplorerFamilies.VOICE_COMMAND,
        states = listOf(
            // Default / Pressed-Selected / Disabled / Focused: WR-01 fix precedent
            // (AlbumSourcePickerSheet/AlbumTitleConfirmSheet, SheetsFamilyScreen.kt) --
            // OutcomeSheet's only "state" is opening it as a ModalBottomSheet; rendering it
            // directly in a states-matrix cell alongside Variants' own demo would compose
            // multiple simultaneous ModalBottomSheet instances on the same detail page.
            // Variants below owns every fixture demo via its own "Show sheet" triggers -- N/A.
            ComponentRegistry.StateCell("Default"),
            ComponentRegistry.StateCell("Pressed / Selected"),
            ComponentRegistry.StateCell("Disabled"),
            ComponentRegistry.StateCell("Focused")
        ),
        content = { OutcomeSheetVariants() },
        tier = ComponentRegistry.Tier.PATTERN
    ),
    ComponentRegistry.Entry(
        name = "ClarificationBar",
        family = ExplorerFamilies.VOICE_COMMAND,
        states = listOf(
            ComponentRegistry.StateCell(
                "Default",
                render = { ClarificationBarFixture(options = fixtureClarificationOptionsTwo) }
            ),
            ComponentRegistry.StateCell(
                "Pressed / Selected",
                render = { ClarificationBarFixture(options = fixtureClarificationOptionsDuplicateLabel) }
            ),
            // ClarificationBar has no disabled/greyed posture -- onSelect/onDismiss fire ONLY
            // from a direct tap and there is no "unavailable" visual state to preview (mirrors
            // ProviderKeyCard's precedent for controls with no disabled param) -- N/A.
            ComponentRegistry.StateCell("Disabled"),
            // No custom focus-visual override -- N/A.
            ComponentRegistry.StateCell("Focused")
        ),
        content = { ClarificationBarVariants() },
        tier = ComponentRegistry.Tier.PATTERN
    )
)

/** Voice Command family (SHOW-01, Phase 10 D-01): a registry-filtered row-list for the new family. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceCommandFamilyScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit
) {
    YahirAndroidTasteTheme(themeMode = themeMode) {
        Scaffold(
            topBar = { VoiceCommandFamilyTopBar(onNavigateBack, themeMode, onToggleTheme) }
        ) { paddingValues ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                items(
                    ComponentRegistry.entries.filter { it.family == ExplorerFamilies.VOICE_COMMAND },
                    key = { it.name }
                ) { entry ->
                    ComponentRow(name = entry.name, tier = entry.tier, onClick = { onNavigateToDetail(entry.name) })
                }
            }
        }
    }
}

/** Fixture providers list — explorer-only, never registered (explorer/ is drift-guard denylisted). */
private val fixtureProviders = listOf(
    ProviderOptionUiModel(id = "openai", label = "OpenAI"),
    ProviderOptionUiModel(id = "anthropic", label = "Anthropic"),
    ProviderOptionUiModel(id = "local", label = "Local (offline)")
)

/**
 * Shared hoisted-state wrapper for the States matrix cells — each cell seeds an initial
 * [keyValue]/[keyState] fixture (never a real key, T-10-01) and lets the live composable own the
 * provider selection + key edits from there.
 */
@Composable
private fun ProviderKeyCardFixture(keyValue: String, keyState: KeyFieldState) {
    var selectedProviderId by remember { mutableStateOf(fixtureProviders.first().id) }
    var currentKeyValue by remember { mutableStateOf(keyValue) }
    ProviderKeyCard(
        providers = fixtureProviders,
        selectedProviderId = selectedProviderId,
        onProviderSelected = { selectedProviderId = it },
        keyValue = currentKeyValue,
        onKeyChange = { currentKeyValue = it },
        keyState = keyState,
        keyLabel = "API key",
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    )
}

/** ProviderKeyCard's interactive demo, reused verbatim as its Variants. */
@Composable
private fun ProviderKeyCardVariants() {
    SectionLabel("ProviderKeyCard")
    ProviderKeyCardFixture(keyValue = "", keyState = KeyFieldState.Empty)
}

/** Fixture models list — explorer-only, never registered (explorer/ is drift-guard denylisted). */
private val fixtureModels = listOf(
    ModelOptionUiModel(id = "gpt-4", label = "GPT-4"),
    ModelOptionUiModel(id = "claude-3", label = "Claude 3"),
    ModelOptionUiModel(id = "local-llama", label = "Local Llama (offline)")
)

/**
 * Shared hoisted-state wrapper for the States matrix cells — each cell seeds an initial
 * [selectedModelId] fixture and lets the live composable own the selection from there.
 */
@Composable
private fun ModelSelectCardFixture(selectedModelId: String?) {
    var currentSelectedId by remember { mutableStateOf(selectedModelId) }
    ModelSelectCard(
        models = fixtureModels,
        selectedModelId = currentSelectedId,
        onModelSelected = { currentSelectedId = it },
        emptyReason = "Set a provider and key first",
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    )
}

/** ModelSelectCard's interactive demo, reused verbatim as its Variants. */
@Composable
private fun ModelSelectCardVariants() {
    SectionLabel("ModelSelectCard")
    ModelSelectCardFixture(selectedModelId = fixtureModels.first().id)

    SectionLabel("ModelSelectCard — empty (disabled + reason)")
    ModelSelectCard(
        models = emptyList(),
        selectedModelId = null,
        onModelSelected = {},
        emptyReason = "Set a provider and key first",
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    )
}

/** Fixture ladder — explorer-only, never registered (explorer/ is drift-guard denylisted). */
private val fixtureLadder = listOf(
    ApproachRungUiModel(id = "cloud", label = "Cloud", rank = 3, offlineCapable = false),
    ApproachRungUiModel(id = "hybrid", label = "Hybrid", rank = 2, offlineCapable = true),
    ApproachRungUiModel(id = "local", label = "Local", rank = 1, offlineCapable = true)
)

/**
 * Shared hoisted-state wrapper for the States matrix cells — seeds initial offline-only/cap
 * fixture values and lets the live composable own state from there.
 */
@Composable
private fun ApproachLadderCardFixture(
    initialOfflineOnly: Boolean = false,
    initialMaxTierId: String = fixtureLadder.first().id
) {
    var offlineOnly by remember { mutableStateOf(initialOfflineOnly) }
    var maxTierId by remember { mutableStateOf(initialMaxTierId) }
    ApproachLadderCard(
        ladder = fixtureLadder,
        offlineOnly = offlineOnly,
        onOfflineOnlyChange = { offlineOnly = it },
        maxTierId = maxTierId,
        onMaxTierChange = { maxTierId = it },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    )
}

/** ApproachLadderCard's interactive demo, reused verbatim as its Variants. */
@Composable
private fun ApproachLadderCardVariants() {
    SectionLabel("ApproachLadderCard")
    ApproachLadderCardFixture()

    SectionLabel("ApproachLadderCard — every control hidden (null offlineOnly/cap props)")
    ApproachLadderCard(
        ladder = fixtureLadder,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    )
}

/** Fixture outcomes -- explorer-only, never registered (explorer/ is drift-guard denylisted). */
private val fixtureOutcomeSuccessSummaryOnly = VoiceOutcomeUiState.Success(summary = "Logged 1 item")
private val fixtureOutcomeSuccessHandledBy = VoiceOutcomeUiState.Success(
    summary = "Logged 2 of 3",
    handledBy = HandledByUiModel(
        tier = "Cloud",
        approach = "Direct",
        provider = "OpenAI",
        model = "gpt-4",
        escalationCount = 1
    )
)
private val fixtureOutcomeFailureNoAction = VoiceOutcomeUiState.Failure(
    reason = "Could not reach the provider"
)
private val fixtureOutcomeFailureWithAction = VoiceOutcomeUiState.Failure(
    reason = "API key rejected",
    action = FailureActionUiModel(label = "Open Settings", onClick = {})
)

/** Fixture undo affordance -- VUNDO-01 happy path: two Available rows, "Undo all (2)". */
private val fixtureOutcomeSuccessUndo = VoiceOutcomeUiState.Success(
    summary = "Logged 2 items",
    undo = UndoAffordanceUiModel(
        allLabel = "Undo all (2)",
        rows = listOf(
            UndoRowUiModel(id = "1", label = "Card deleted", state = UndoRowState.Available(onUndo = {})),
            UndoRowUiModel(id = "2", label = "Tag removed", state = UndoRowState.Available(onUndo = {}))
        ),
        onUndoAll = {}
    )
)

/**
 * Fixture undo affordance -- VUNDO-01 entangled-item state: one Available row alongside one
 * Unavailable row (a per-item entangled action the app can't undo individually).
 */
private val fixtureOutcomeSuccessUndoUnavailable = VoiceOutcomeUiState.Success(
    summary = "Logged 2 items",
    undo = UndoAffordanceUiModel(
        allLabel = "Undo all (1)",
        rows = listOf(
            UndoRowUiModel(id = "1", label = "Card deleted", state = UndoRowState.Available(onUndo = {})),
            UndoRowUiModel(id = "2", label = "Tag removed", state = UndoRowState.Unavailable(reason = "Entangled with another edit"))
        ),
        onUndoAll = {}
    )
)

/** Fixture undo affordance -- VUNDO-01 loud undo-refused/partial substate. */
private val fixtureOutcomeSuccessUndoRefused = VoiceOutcomeUiState.Success(
    summary = "Logged 2 items",
    undo = UndoAffordanceUiModel(
        allLabel = "Undo all (2)",
        rows = listOf(
            UndoRowUiModel(id = "1", label = "Card deleted", state = UndoRowState.Available(onUndo = {})),
            UndoRowUiModel(id = "2", label = "Tag removed", state = UndoRowState.Available(onUndo = {}))
        ),
        onUndoAll = {},
        refused = UndoRefusedUiModel(reason = "Item changed since", changedItem = "Card deleted")
    )
)

/**
 * Fixture needs-confirmation -- VOUT-04 single destructive item (SB's `MutationGate`/
 * `VoiceConfirmGate` shape: one item, Destructive severity, a reversibility hint).
 */
private val fixtureOutcomeNeedsConfirmationSingleDestructive = VoiceOutcomeUiState.NeedsConfirmation(
    reason = "This will permanently remove the card and its history.",
    title = "Delete card?",
    items = listOf(ProposedItemUiModel(id = "card-1", title = "Grocery list", subtitle = "12 items")),
    severity = ActionButtonDefaults.ActionButtonRole.Destructive,
    reversibilityHint = "Irreversible",
    confirmLabel = "Delete",
    onConfirm = {},
    onCancel = {}
)

/**
 * Fixture needs-confirmation -- VOUT-04 batch (CT's `ProposedBatch` shape: a shared
 * `topLevelContent` date row and per-item `onRemove`).
 */
private val fixtureOutcomeNeedsConfirmationBatch = VoiceOutcomeUiState.NeedsConfirmation(
    reason = "3 items parsed from your grocery run -- check the one marked Weak match.",
    items = listOf(
        ProposedItemUiModel(id = "1", title = "Apple", subtitle = "2 ct"),
        ProposedItemUiModel(id = "2", title = "Banana", subtitle = "1 bunch", confidenceCue = "Weak match", onRemove = {}),
        ProposedItemUiModel(id = "3", title = "Bread", subtitle = "1 loaf", amended = true, onRemove = {})
    ),
    selectionMode = SelectionMode.AllOrNothing,
    confirmLabel = "Confirm all (3)",
    topLevelContent = { Text("Logged for: Today") },
    onConfirm = {},
    onCancel = {}
)

/**
 * Fixture needs-confirmation -- WR-03 coverage: a single item using
 * [ProposedItemUiModel.trailingContent] (the opaque per-item slot, D-01), e.g. a tag badge a
 * consumer renders via its own composable. Mirrors [fixtureOutcomeNeedsConfirmationBatch]'s
 * [VoiceOutcomeUiState.NeedsConfirmation.topLevelContent] coverage, but for the per-item slot.
 */
private val fixtureOutcomeNeedsConfirmationTrailingContent = VoiceOutcomeUiState.NeedsConfirmation(
    reason = "Review this item before confirming.",
    items = listOf(
        ProposedItemUiModel(id = "tag-1", title = "Apple", trailingContent = { Text("tag") })
    ),
    onConfirm = {},
    onCancel = {}
)

/**
 * OutcomeSheet's interactive demo -- WR-01-safe pattern (mirrors `AlbumSourcePickerSheetSection`'s
 * "Show sheet" trigger in SheetsFamilyScreen.kt): a single hoisted [visibleOutcome] slot holds AT
 * MOST one fixture at a time, so tapping a different "Show" button swaps the live sheet instead
 * of stacking a second simultaneous ModalBottomSheet.
 */
@Composable
private fun OutcomeSheetVariants() {
    var visibleOutcome by remember { mutableStateOf<VoiceOutcomeUiState?>(null) }

    SectionLabel("OutcomeSheet — Success, summary only")
    Button(
        onClick = { visibleOutcome = fixtureOutcomeSuccessSummaryOnly },
        modifier = Modifier.padding(horizontal = 16.dp)
    ) { Text("Show sheet") }

    SectionLabel("OutcomeSheet — Success, handled-by populated")
    Button(
        onClick = { visibleOutcome = fixtureOutcomeSuccessHandledBy },
        modifier = Modifier.padding(horizontal = 16.dp)
    ) { Text("Show sheet") }

    SectionLabel("OutcomeSheet — Failure, no action (absent action -> no action contract)")
    Button(
        onClick = { visibleOutcome = fixtureOutcomeFailureNoAction },
        modifier = Modifier.padding(horizontal = 16.dp)
    ) { Text("Show sheet") }

    SectionLabel("OutcomeSheet — Failure, action populated")
    Button(
        onClick = { visibleOutcome = fixtureOutcomeFailureWithAction },
        modifier = Modifier.padding(horizontal = 16.dp)
    ) { Text("Show sheet") }

    SectionLabel("OutcomeSheet — Success, undo all (2) + per-item rows (VUNDO-01)")
    Button(
        onClick = { visibleOutcome = fixtureOutcomeSuccessUndo },
        modifier = Modifier.padding(horizontal = 16.dp)
    ) { Text("Show sheet") }

    SectionLabel("OutcomeSheet — Success, undo with an entangled Unavailable row (VUNDO-01)")
    Button(
        onClick = { visibleOutcome = fixtureOutcomeSuccessUndoUnavailable },
        modifier = Modifier.padding(horizontal = 16.dp)
    ) { Text("Show sheet") }

    SectionLabel("OutcomeSheet — Success, loud undo-refused substate (VUNDO-01)")
    Button(
        onClick = { visibleOutcome = fixtureOutcomeSuccessUndoRefused },
        modifier = Modifier.padding(horizontal = 16.dp)
    ) { Text("Show sheet") }

    SectionLabel("OutcomeSheet — NeedsConfirmation, single destructive item (VOUT-04)")
    Button(
        onClick = { visibleOutcome = fixtureOutcomeNeedsConfirmationSingleDestructive },
        modifier = Modifier.padding(horizontal = 16.dp)
    ) { Text("Show sheet") }

    SectionLabel("OutcomeSheet — NeedsConfirmation, batch with shared topLevelContent + per-item remove (VOUT-04)")
    Button(
        onClick = { visibleOutcome = fixtureOutcomeNeedsConfirmationBatch },
        modifier = Modifier.padding(horizontal = 16.dp)
    ) { Text("Show sheet") }

    SectionLabel("OutcomeSheet — NeedsConfirmation, per-item trailingContent (VOUT-04)")
    Button(
        onClick = { visibleOutcome = fixtureOutcomeNeedsConfirmationTrailingContent },
        modifier = Modifier.padding(horizontal = 16.dp)
    ) { Text("Show sheet") }

    visibleOutcome?.let { outcome ->
        OutcomeSheet(outcome = outcome, onDismissRequest = { visibleOutcome = null })
    }
}

/** Fixture options -- explorer-only, never registered (explorer/ is drift-guard denylisted). */
private val fixtureClarificationOptionsTwo = listOf(
    ClarificationOptionUiModel(id = "groceries", label = "Groceries"),
    ClarificationOptionUiModel(id = "work", label = "Work")
)

/**
 * Fixture options -- VCLAR-01 duplicate-label edge: two options sharing the SAME label but
 * distinct opaque ids, demonstrating the no-de-duplication/no-merging contract.
 */
private val fixtureClarificationOptionsDuplicateLabel = listOf(
    ClarificationOptionUiModel(id = "list-a", label = "Shopping List"),
    ClarificationOptionUiModel(id = "list-b", label = "Shopping List")
)

/** Shared fixture wrapper for the States matrix cells. */
@Composable
private fun ClarificationBarFixture(options: List<ClarificationOptionUiModel>) {
    ClarificationBar(
        question = "Which list?",
        options = options,
        onSelect = {},
        onDismiss = {},
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    )
}

/** ClarificationBar's interactive demo, including the duplicate-label edge fixture. */
@Composable
private fun ClarificationBarVariants() {
    SectionLabel("ClarificationBar")
    ClarificationBarFixture(options = fixtureClarificationOptionsTwo)

    SectionLabel("ClarificationBar — duplicate-label edge (two options, same label, different ids)")
    ClarificationBarFixture(options = fixtureClarificationOptionsDuplicateLabel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VoiceCommandFamilyTopBar(onNavigateBack: () -> Unit, themeMode: ThemeMode, onToggleTheme: () -> Unit) {
    TopAppBar(
        title = { Text("Voice Command") },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = { ExplorerThemeToggleAction(themeMode = themeMode, onToggleTheme = onToggleTheme) }
    )
}
