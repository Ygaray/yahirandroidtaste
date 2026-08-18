package com.example.secondbrain.yahirandroidtaste.explorer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.example.secondbrain.yahirandroidtaste.theme.Dimens
import com.example.secondbrain.yahirandroidtaste.theme.InkOnBackground
import com.example.secondbrain.yahirandroidtaste.theme.NeutralOnBgLight
import com.example.secondbrain.yahirandroidtaste.theme.ThemeMode
import com.example.secondbrain.yahirandroidtaste.theme.YahirAndroidTasteTheme

/**
 * EXPLORE-03 token browser — a single scrollable screen rendering the five documented token
 * categories (Color -> Type -> Spacing -> Elevation -> Shape, D-01) in fixed authored order from
 * the [colorSwatches]/[typeScaleEntries]/[dimensEntries]/[elevationLevels]/[shapeEntries] tables
 * (`TokenSwatches.kt`, Plan 02). Reached via the "Design Tokens" row on [ExplorerIndexScreen]
 * (Plan 03 Task 2) and the internal `"tokens"` NavHost route (`ExplorerEntry`).
 *
 * Composes under the hoisted [ThemeMode] exactly like every other explorer screen, so SC3's
 * simultaneous chrome re-theme is automatic. The Color section renders each swatch's RAW
 * `theme/Color.kt` vals directly (never `MaterialTheme.colorScheme.*`, which is wallpaper-derived
 * under `dynamicColor = true` — RESEARCH.md Pitfall 1) — the swatch fills are a static reference
 * table that does not change on theme toggle, per UI-SPEC Surface Contract 1.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenBrowserScreen(
    onNavigateBack: () -> Unit,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit
) {
    YahirAndroidTasteTheme(themeMode = themeMode) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Design Tokens") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        ExplorerThemeToggleAction(themeMode = themeMode, onToggleTheme = onToggleTheme)
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                SectionLabel("Color")
                ColorSection()
                Spacer(Modifier.height(24.dp))

                SectionLabel("Type")
                TypeSection()
                Spacer(Modifier.height(24.dp))

                SectionLabel("Spacing")
                SpacingSection()
                Spacer(Modifier.height(24.dp))

                SectionLabel("Elevation")
                ElevationSection(themeMode = themeMode)
                Spacer(Modifier.height(24.dp))

                SectionLabel("Shape")
                ShapeSection()
            }
        }
    }
}

/**
 * One row per [colorSwatches] entry — light swatch, dark swatch, then name + hex caption. The
 * swatch `Box` fills bind directly to `swatch.light`/`swatch.dark` (RAW `Color.kt` vals), never
 * `MaterialTheme.colorScheme.*` (D-02/Pitfall 1); only the caption text color uses `colorScheme`.
 */
@Composable
private fun ColorSection() {
    Column {
        colorSwatches.forEach { swatch ->
            val outlineColor = MaterialTheme.colorScheme.outline
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(Dimens.CornerRadius.Small))
                        .background(swatch.light)
                        .border(Dimens.HairlineBorder, outlineColor, RoundedCornerShape(Dimens.CornerRadius.Small))
                )
                Spacer(Modifier.width(4.dp))
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(Dimens.CornerRadius.Small))
                        .background(swatch.dark)
                        .border(Dimens.HairlineBorder, outlineColor, RoundedCornerShape(Dimens.CornerRadius.Small))
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(swatch.name, style = MaterialTheme.typography.titleSmall)
                    val darkCaption = if (swatch.light == swatch.dark) {
                        "same value"
                    } else {
                        "#${swatch.dark.toHex()}"
                    }
                    Text(
                        "Light #${swatch.light.toHex()}  ·  Dark $darkCaption",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            DividerRow()
        }
    }
}

/** One row per [typeScaleEntries] entry, rendered IN its own style, separated by a 4.dp spacer. */
@Composable
private fun TypeSection() {
    Column {
        typeScaleEntries.forEach { entry ->
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(entry.name, style = entry.style)
                Text(
                    "${entry.style.fontSize.value.toInt()}sp · ${entry.style.fontWeight}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(Dimens.ContentSpacing))
        }
    }
}

/** One labeled proportional bar per [dimensEntries] value — bar width equals the dp value. */
@Composable
private fun SpacingSection() {
    Column {
        dimensEntries.forEach { entry ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(entry.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Box(
                    Modifier
                        .width(entry.value)
                        .height(8.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(Dimens.CornerRadius.Small))
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "${entry.value.value.toInt()}dp",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(Dimens.ContentSpacing))
        }
    }
}

/**
 * One full-width 56.dp band per [elevationLevels] entry, stacked with no gaps. Unlike
 * [ColorSection]'s static reference swatches, the ramp follows the current [themeMode] — it
 * demonstrates the ramp as it actually renders in the active theme (UI-SPEC Surface Contract 1,
 * locked distinction from the Color section).
 */
@Composable
private fun ElevationSection(themeMode: ThemeMode) {
    Column {
        elevationLevels.forEach { swatch ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(if (themeMode == ThemeMode.LIGHT) swatch.light else swatch.dark),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    swatch.name,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = if (themeMode == ThemeMode.LIGHT) NeutralOnBgLight else InkOnBackground
                )
            }
        }
    }
}

/**
 * Fixed-column rows of [shapeEntries] tiles (3 per row) — each a 64.dp `primaryContainer` box
 * clipped to the shape, with a name caption. Plain `Row`s (not a lazy grid) since this section
 * lives inside the screen's single outer `verticalScroll` Column (UI-SPEC "FlowRow or fixed-column
 * Rows" — a lazy grid would conflict with the already-scrollable parent).
 */
@Composable
private fun ShapeSection() {
    shapeEntries().chunked(3).forEach { rowEntries ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.HorizontalPadding, vertical = 8.dp)
        ) {
            rowEntries.forEach { entry ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        Modifier
                            .size(64.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, entry.shape)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(entry.name, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

/** Hex string (no `#` prefix) for a [Color]'s RGB channels — used only for Color-section captions. */
private fun Color.toHex(): String {
    val argb = toArgb()
    return String.format("%06X", argb and 0xFFFFFF)
}
