package io.github.ygaray.yahirandroidtaste.explorer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.component.MetricBand
import io.github.ygaray.yahirandroidtaste.component.MetricBar
import io.github.ygaray.yahirandroidtaste.component.ProgressRing
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode

/**
 * This family's slice of [ComponentRegistry.entries], declared here (not in
 * `ComponentRegistry.kt`) mirroring the existing `pickersFamilyEntries`/`feedbackFamilyEntries`
 * convention — new families author their own entries list in their own family-screen file.
 */
internal val progressFamilyEntries: List<ComponentRegistry.Entry> = listOf(
    ComponentRegistry.Entry(
        name = "MetricBar",
        family = ExplorerFamilies.PROGRESS_METRICS,
        states = listOf(
            ComponentRegistry.StateCell(
                "Default",
                render = {
                    MetricBar(
                        label = "Score",
                        valueText = "42 / 100 pts",
                        fraction = 0.42f,
                        band = MetricBand.Good,
                        remainingText = "58 pts left",
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    )
                }
            ),
            // MetricBar is display-only, no pressable/selectable element — N/A (mirrors
            // AccentColorPicker's own N/A precedent for its non-pressable swatches).
            ComponentRegistry.StateCell("Pressed / Selected"),
            // MetricBar has no `enabled` param — N/A.
            ComponentRegistry.StateCell("Disabled"),
            // MetricBar has no focus-visual override — N/A.
            ComponentRegistry.StateCell("Focused")
        ),
        content = { MetricBarVariants() }
    ),
    ComponentRegistry.Entry(
        name = "ProgressRing",
        family = ExplorerFamilies.PROGRESS_METRICS,
        states = listOf(
            ComponentRegistry.StateCell(
                "Default",
                render = {
                    ProgressRing(fraction = 0.65f, modifier = Modifier.size(96.dp))
                }
            ),
            // ProgressRing is display-only, no pressable/selectable element — N/A (mirrors
            // MetricBar's own N/A precedent above).
            ComponentRegistry.StateCell("Pressed / Selected"),
            // ProgressRing has no `enabled` param — N/A.
            ComponentRegistry.StateCell("Disabled"),
            // ProgressRing has no focus-visual override — N/A.
            ComponentRegistry.StateCell("Focused")
        ),
        content = { ProgressRingVariants() }
    )
)

/** Progress / Metrics family: a registry-filtered row-list picker for `MetricBar`. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressFamilyScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit
) {
    YahirAndroidTasteTheme(themeMode = themeMode) {
        Scaffold(
            topBar = { ProgressFamilyTopBar(onNavigateBack, themeMode, onToggleTheme) }
        ) { paddingValues ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                items(
                    ComponentRegistry.entries.filter { it.family == ExplorerFamilies.PROGRESS_METRICS },
                    key = { it.name }
                ) { entry ->
                    ComponentRow(name = entry.name, onClick = { onNavigateToDetail(entry.name) })
                }
            }
        }
    }
}

/** One `MetricBar` row per [MetricBand] value, demonstrating the full 5-band palette. */
@Composable
private fun MetricBarVariants() {
    SectionLabel("MetricBar — all 5 bands")
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MetricBar(
            label = "Very Under",
            valueText = "5 / 100 pts",
            fraction = 0.05f,
            band = MetricBand.VeryUnder,
            remainingText = "95 pts left"
        )
        MetricBar(
            label = "Under",
            valueText = "25 / 100 pts",
            fraction = 0.25f,
            band = MetricBand.Under,
            remainingText = "75 pts left"
        )
        MetricBar(
            label = "Good",
            valueText = "42 / 100 pts",
            fraction = 0.42f,
            band = MetricBand.Good,
            remainingText = "58 pts left"
        )
        MetricBar(
            label = "On Target",
            valueText = "100 / 100 pts",
            fraction = 1.0f,
            band = MetricBand.OnTarget,
            remainingText = "0 pts left"
        )
        MetricBar(
            label = "Over",
            valueText = "120 / 100 pts",
            fraction = 1.0f,
            band = MetricBand.Over,
            remainingText = "20 pts over"
        )
    }
}

/** A few sample `ProgressRing` fractions, demonstrating the ring at different fill levels. */
@Composable
private fun ProgressRingVariants() {
    SectionLabel("ProgressRing — sample fractions")
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProgressRing(fraction = 0.2f, modifier = Modifier.size(72.dp))
        ProgressRing(fraction = 0.65f, modifier = Modifier.size(72.dp))
        ProgressRing(fraction = 1.0f, modifier = Modifier.size(72.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProgressFamilyTopBar(onNavigateBack: () -> Unit, themeMode: ThemeMode, onToggleTheme: () -> Unit) {
    TopAppBar(
        title = { Text("Progress / Metrics") },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = { ExplorerThemeToggleAction(themeMode = themeMode, onToggleTheme = onToggleTheme) }
    )
}
