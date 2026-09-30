package io.github.ygaray.yahirandroidtaste.explorer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import io.github.ygaray.yahirandroidtaste.component.ProviderKeyCard
import io.github.ygaray.yahirandroidtaste.model.KeyFieldState
import io.github.ygaray.yahirandroidtaste.model.ProviderOptionUiModel
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode

/**
 * D-05: this family's slice of [ComponentRegistry.entries], declared here (not in
 * `ComponentRegistry.kt`) so this file stays the single place this plan enriches Voice
 * Command's `states`/`content` without touching the shared registry file — mirrors
 * `pickersFamilyEntries` in [PickersFamilyScreen].
 *
 * 13-prep groundwork (Phase 10 Plan 01, D-01): this is the NEW tenth registry family. It
 * currently holds ONE entry ([ProviderKeyCard], VSET-01) — Plan 02's `ModelSelectCard` and
 * `ApproachLadderCard` append to this same list.
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
