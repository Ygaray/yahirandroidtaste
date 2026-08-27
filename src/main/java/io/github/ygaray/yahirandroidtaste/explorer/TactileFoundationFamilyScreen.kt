package io.github.ygaray.yahirandroidtaste.explorer

import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Modifier
import io.github.ygaray.yahirandroidtaste.theme.ElevationLadder
import io.github.ygaray.yahirandroidtaste.theme.TactileTypeShowcase
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode

/**
 * This family's slice of [ComponentRegistry.entries], declared here (not in
 * `ComponentRegistry.kt`) mirroring the existing `progressFamilyEntries` convention — new
 * families author their own entries list in their own family-screen file (Phase 123 DS-01).
 */
internal val tactileFoundationFamilyEntries: List<ComponentRegistry.Entry> = listOf(
    ComponentRegistry.Entry(
        name = "ElevationLadder",
        family = ExplorerFamilies.TACTILE_FOUNDATION,
        states = listOf(
            ComponentRegistry.StateCell(
                "Default",
                render = { ElevationLadder() }
            ),
            // ElevationLadder is a display-only token demo, no pressable/selectable element — N/A
            // (mirrors MetricBarVariants' own N/A precedent).
            ComponentRegistry.StateCell("Pressed / Selected"),
            // ElevationLadder has no `enabled` param — N/A.
            ComponentRegistry.StateCell("Disabled"),
            // ElevationLadder has no focus-visual override — N/A.
            ComponentRegistry.StateCell("Focused")
        ),
        content = { ElevationLadderVariants() }
    ),
    ComponentRegistry.Entry(
        name = "TactileTypeShowcase",
        family = ExplorerFamilies.TACTILE_FOUNDATION,
        states = listOf(
            ComponentRegistry.StateCell(
                "Default",
                render = { TactileTypeShowcase() }
            ),
            // TactileTypeShowcase is a display-only token demo, no pressable/selectable element — N/A
            // (mirrors ElevationLadderVariants' own N/A precedent).
            ComponentRegistry.StateCell("Pressed / Selected"),
            // TactileTypeShowcase has no `enabled` param — N/A.
            ComponentRegistry.StateCell("Disabled"),
            // TactileTypeShowcase has no focus-visual override — N/A.
            ComponentRegistry.StateCell("Focused")
        ),
        content = { TactileTypeShowcaseVariants() }
    )
)

/** Tactile Foundation family: a registry-filtered row-list picker for the Tactile primitives. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TactileFoundationFamilyScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit
) {
    YahirAndroidTasteTheme(themeMode = themeMode) {
        Scaffold(
            topBar = { TactileFoundationFamilyTopBar(onNavigateBack, themeMode, onToggleTheme) }
        ) { paddingValues ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                items(
                    ComponentRegistry.entries.filter { it.family == ExplorerFamilies.TACTILE_FOUNDATION },
                    key = { it.name }
                ) { entry ->
                    ComponentRow(name = entry.name, onClick = { onNavigateToDetail(entry.name) })
                }
            }
        }
    }
}

/** The Elevation Scale — Level0 through Level5, rendered live via [ElevationLadder]. */
@Composable
private fun ElevationLadderVariants() {
    SectionLabel("Elevation Scale — Level0 through Level5")
    ElevationLadder()
}

/** The Space Grotesk Type Ramp — 4 tiers vs. the default face, rendered live via [TactileTypeShowcase]. */
@Composable
private fun TactileTypeShowcaseVariants() {
    SectionLabel("Space Grotesk Type Ramp — 4 tiers vs. the default face")
    TactileTypeShowcase()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TactileFoundationFamilyTopBar(
    onNavigateBack: () -> Unit,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit
) {
    TopAppBar(
        title = { Text("Tactile Foundation") },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = { ExplorerThemeToggleAction(themeMode = themeMode, onToggleTheme = onToggleTheme) }
    )
}
