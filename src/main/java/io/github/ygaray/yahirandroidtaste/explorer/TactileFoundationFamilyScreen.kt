package io.github.ygaray.yahirandroidtaste.explorer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.ygaray.yahirandroidtaste.component.ACCENT_COLORS
import io.github.ygaray.yahirandroidtaste.component.GradientSwatch
import io.github.ygaray.yahirandroidtaste.component.HeatSwatch
import io.github.ygaray.yahirandroidtaste.theme.Dimens
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
    ),
    ComponentRegistry.Entry(
        name = "GradientSwatch",
        family = ExplorerFamilies.TACTILE_FOUNDATION,
        states = listOf(
            ComponentRegistry.StateCell(
                "Default",
                render = { GradientSwatch(accentColor = Color(ACCENT_COLORS[2].light)) }
            ),
            // GradientSwatch is a display-only token demo, no pressable/selectable element — N/A
            // (mirrors TactileTypeShowcaseVariants' own N/A precedent).
            ComponentRegistry.StateCell("Pressed / Selected"),
            // GradientSwatch has no `enabled` param — N/A.
            ComponentRegistry.StateCell("Disabled"),
            // GradientSwatch has no focus-visual override — N/A.
            ComponentRegistry.StateCell("Focused")
        ),
        content = { GradientSwatchVariants() }
    ),
    ComponentRegistry.Entry(
        name = "HeatSwatch",
        family = ExplorerFamilies.TACTILE_FOUNDATION,
        states = listOf(
            ComponentRegistry.StateCell(
                "Default",
                render = { HeatSwatch() }
            ),
            // HeatSwatch is a display-only token demo, no pressable/selectable element — N/A
            // (mirrors GradientSwatchVariants' own N/A precedent).
            ComponentRegistry.StateCell("Pressed / Selected"),
            // HeatSwatch has no `enabled` param — N/A.
            ComponentRegistry.StateCell("Disabled"),
            // HeatSwatch has no focus-visual override — N/A.
            ComponentRegistry.StateCell("Focused")
        ),
        content = { HeatSwatchVariants() }
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

/**
 * Gradient & Tint Accents — four sample accent colors, each rendered via [GradientSwatch],
 * sourced from the existing public `ACCENT_COLORS` palette rather than invented hex literals.
 */
@Composable
private fun GradientSwatchVariants() {
    SectionLabel("Gradient & Tint Accents — four sample accent colors")
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Dimens.HorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.CompactPadding)
    ) {
        GradientSwatch(accentColor = Color(ACCENT_COLORS[0].light)) // Red
        GradientSwatch(accentColor = Color(ACCENT_COLORS[2].light)) // Purple
        GradientSwatch(accentColor = Color(ACCENT_COLORS[5].light)) // Blue
        GradientSwatch(accentColor = Color(ACCENT_COLORS[7].light)) // Green
    }
}

/** Heat Relatedness Ramp — 4 tiers plus the distinct hub node, rendered live via [HeatSwatch]. */
@Composable
private fun HeatSwatchVariants() {
    SectionLabel("Heat Relatedness Ramp — 4 tiers plus the distinct hub node")
    HeatSwatch()
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
