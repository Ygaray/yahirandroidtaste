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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.component.AnimatedStatValue
import io.github.ygaray.yahirandroidtaste.component.BarSegment
import io.github.ygaray.yahirandroidtaste.component.HeroStatCard
import io.github.ygaray.yahirandroidtaste.component.MetricBand
import io.github.ygaray.yahirandroidtaste.component.MetricBar
import io.github.ygaray.yahirandroidtaste.component.ProgressRing
import io.github.ygaray.yahirandroidtaste.component.StackedProgressBar
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
        content = { MetricBarVariants() },
        tier = ComponentRegistry.Tier.PATTERN
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
        content = { ProgressRingVariants() },
        tier = ComponentRegistry.Tier.PATTERN
    ),
    ComponentRegistry.Entry(
        name = "AnimatedStatValue",
        family = ExplorerFamilies.PROGRESS_METRICS,
        states = listOf(
            ComponentRegistry.StateCell(
                "Default",
                render = {
                    AnimatedStatValue(targetValue = 128f)
                }
            ),
            // AnimatedStatValue is display-only, no pressable/selectable element — N/A (mirrors
            // ProgressRing's own N/A precedent above).
            ComponentRegistry.StateCell("Pressed / Selected"),
            // AnimatedStatValue has no `enabled` param — N/A.
            ComponentRegistry.StateCell("Disabled"),
            // AnimatedStatValue has no focus-visual override — N/A.
            ComponentRegistry.StateCell("Focused")
        ),
        content = { AnimatedStatValueVariants() },
        tier = ComponentRegistry.Tier.PRIMITIVE
    ),
    ComponentRegistry.Entry(
        name = "HeroStatCard",
        family = ExplorerFamilies.PROGRESS_METRICS,
        states = listOf(
            ComponentRegistry.StateCell(
                "Default",
                render = {
                    HeroStatCard(label = "Total", value = "1,204")
                }
            ),
            ComponentRegistry.StateCell(
                "Pressed / Selected",
                render = {
                    HeroStatCard(label = "Total", value = "1,204", onClick = {})
                }
            ),
            // HeroStatCard has no `enabled` param — a non-clickable card is just onClick = null,
            // not a disabled clickable one — N/A.
            ComponentRegistry.StateCell("Disabled"),
            // IN-01 fix: HeroStatCard has no focus-visual override param, so a static preview
            // cannot render a distinguishing focused state — N/A (mirrors the Disabled cell's own
            // N/A precedent above; the previous render was byte-identical to "Pressed / Selected").
            ComponentRegistry.StateCell("Focused")
        ),
        content = { HeroStatCardVariants() },
        tier = ComponentRegistry.Tier.PATTERN
    ),
    ComponentRegistry.Entry(
        name = "StackedProgressBar",
        family = ExplorerFamilies.PROGRESS_METRICS,
        states = listOf(
            ComponentRegistry.StateCell(
                "Default",
                render = {
                    // outline stays visible in both schemes (the MetricBar track-contrast lesson).
                    StackedProgressBar(
                        segments = listOf(
                            BarSegment(0.4f, MaterialTheme.colorScheme.primary),
                            BarSegment(0.18f, MaterialTheme.colorScheme.tertiary)
                        ),
                        trackColor = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    )
                }
            ),
            // StackedProgressBar is display-only, no pressable/selectable element - N/A (mirrors
            // MetricBar's own N/A precedent above).
            ComponentRegistry.StateCell("Pressed / Selected"),
            // StackedProgressBar has no `enabled` param - N/A.
            ComponentRegistry.StateCell("Disabled"),
            // StackedProgressBar has no focus-visual override - N/A.
            ComponentRegistry.StateCell("Focused")
        ),
        content = { StackedProgressBarVariants() },
        tier = ComponentRegistry.Tier.PRIMITIVE
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
    ExplorerTheme(themeMode) {
        Scaffold(
            topBar = { ProgressFamilyTopBar(onNavigateBack, themeMode, onToggleTheme) }
        ) { paddingValues ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                items(
                    ComponentRegistry.entries.filter { it.family == ExplorerFamilies.PROGRESS_METRICS },
                    key = { it.name }
                ) { entry ->
                    ComponentRow(name = entry.name, tier = entry.tier, onClick = { onNavigateToDetail(entry.name) })
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

/**
 * A few sample `AnimatedStatValue` target values, including a 5+-digit long numeral (no
 * truncation/clipping) and a decrease from the prior sample (demonstrating the tween runs in
 * both directions, not just increase-only).
 */
@Composable
private fun AnimatedStatValueVariants() {
    SectionLabel("AnimatedStatValue — sample targets")
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedStatValue(targetValue = 128f)
        AnimatedStatValue(targetValue = 12480f)
        AnimatedStatValue(targetValue = 12f)
    }
}

/**
 * A few sample `HeroStatCard`s: a deliberately long label/value pair (confirms Ellipsis
 * truncation), one with `onClick` set, and one with a `content` slot filled by an embedded
 * `ProgressRing` (demonstrating primitive composition).
 */
@Composable
private fun HeroStatCardVariants() {
    SectionLabel("HeroStatCard — sample cards")
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeroStatCard(
            label = "A deliberately long supporting label that should truncate with an ellipsis",
            value = "1,204,983 total"
        )
        HeroStatCard(label = "Tap me", value = "1,204", onClick = {})
        HeroStatCard(label = "With embedded ring", value = "1,204") {
            ProgressRing(fraction = 0.65f, modifier = Modifier.size(48.dp))
        }
    }
}

/**
 * Sample `StackedProgressBar`s: two segments, a sum above 1 (clamped to the full track), a
 * negative fraction (drawn as zero), an empty list (bare track) and a taller 12dp track.
 */
@Composable
private fun StackedProgressBarVariants() {
    SectionLabel("StackedProgressBar - sample segments")
    val track = MaterialTheme.colorScheme.outline
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StackedProgressBar(
            segments = listOf(BarSegment(0.4f, primary), BarSegment(0.18f, tertiary)),
            trackColor = track
        )
        StackedProgressBar(
            segments = listOf(BarSegment(0.7f, primary), BarSegment(0.6f, tertiary)),
            trackColor = track
        )
        StackedProgressBar(
            segments = listOf(BarSegment(-0.3f, primary), BarSegment(0.5f, tertiary)),
            trackColor = track
        )
        StackedProgressBar(segments = emptyList(), trackColor = track)
        StackedProgressBar(
            segments = listOf(BarSegment(0.4f, primary), BarSegment(0.18f, tertiary)),
            trackColor = track,
            height = 12.dp
        )
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
