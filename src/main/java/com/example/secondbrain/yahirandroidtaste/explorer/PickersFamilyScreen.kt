package com.example.secondbrain.yahirandroidtaste.explorer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.secondbrain.yahirandroidtaste.component.AccentColorPicker
import com.example.secondbrain.yahirandroidtaste.component.CropOverlay
import com.example.secondbrain.yahirandroidtaste.component.IconPickerGrid
import com.example.secondbrain.yahirandroidtaste.theme.YahirAndroidTasteTheme
import com.example.secondbrain.yahirandroidtaste.theme.ThemeMode

/**
 * D-05: this family's slice of [ComponentRegistry.entries], declared here (not in
 * `ComponentRegistry.kt`) so this file stays the single place this plan enriches Pickers'
 * `states`/`content` without touching the shared registry file. Order preserved exactly from
 * the pre-Phase-62-Plan-02 inline registry list.
 */
internal val pickersFamilyEntries: List<ComponentRegistry.Entry> = listOf(
    ComponentRegistry.Entry(
        name = "AccentColorPicker",
        family = ExplorerFamilies.PICKERS,
        states = listOf(
            ComponentRegistry.StateCell(
                "Default",
                render = {
                    var selectedColor by remember { mutableStateOf(0xFF6750A4L) }
                    AccentColorPicker(
                        selectedColor = selectedColor,
                        onColorSelected = { selectedColor = it },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    )
                }
            ),
            // The currently-selected swatch is already shown live in Default — no separate
            // discrete pressed/selected preview.
            ComponentRegistry.StateCell("Pressed / Selected"),
            // AccentColorPicker has no disabled param — N/A.
            ComponentRegistry.StateCell("Disabled"),
            // AccentColorPicker has no focus-visual override — N/A.
            ComponentRegistry.StateCell("Focused")
        ),
        content = { AccentColorPickerVariants() }
    ),
    ComponentRegistry.Entry(
        name = "IconPickerGrid",
        family = ExplorerFamilies.PICKERS,
        states = listOf(
            ComponentRegistry.StateCell(
                "Default",
                render = {
                    var selectedIcon by remember { mutableStateOf("star") }
                    IconPickerGrid(
                        selectedIcon = selectedIcon,
                        onIconSelected = { selectedIcon = it },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    )
                }
            ),
            // The currently-selected icon is already shown live in Default — no separate
            // discrete pressed/selected preview.
            ComponentRegistry.StateCell("Pressed / Selected"),
            // IconPickerGrid has no disabled param — N/A.
            ComponentRegistry.StateCell("Disabled"),
            // IconPickerGrid has no focus-visual override — N/A.
            ComponentRegistry.StateCell("Focused")
        ),
        content = { IconPickerGridVariants() }
    ),
    ComponentRegistry.Entry(
        name = "CropOverlay",
        family = ExplorerFamilies.PICKERS,
        states = listOf(
            ComponentRegistry.StateCell(
                "Default",
                render = {
                    CropOverlayFreeCropPreview()
                }
            ),
            // Pressed / Selected: CropOverlay is gesture-driven (8 drag handles) — no discrete
            // caller-supplied interaction-state prop exists to exercise — N/A.
            ComponentRegistry.StateCell("Pressed / Selected"),
            // Disabled: no `enabled` param exposed — N/A.
            ComponentRegistry.StateCell("Disabled"),
            // Focused: no focus-visual param exposed — N/A.
            ComponentRegistry.StateCell("Focused")
        ),
        content = { CropOverlayVariants() }
    )
)

/** Pickers family (SHOW-01, D-04): a registry-filtered row-list picker for `IconPickerGrid` and `AccentColorPicker`. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickersFamilyScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit
) {
    YahirAndroidTasteTheme(themeMode = themeMode) {
        Scaffold(
            topBar = { PickersFamilyTopBar(onNavigateBack, themeMode, onToggleTheme) }
        ) { paddingValues ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                items(
                    ComponentRegistry.entries.filter { it.family == ExplorerFamilies.PICKERS },
                    key = { it.name }
                ) { entry ->
                    ComponentRow(name = entry.name, onClick = { onNavigateToDetail(entry.name) })
                }
            }
        }
    }
}

/** IconPickerGrid's Phase-61 curated demo, reused verbatim as its Variants. */
@Composable
private fun IconPickerGridVariants() {
    var selectedIcon by remember { mutableStateOf("star") }
    SectionLabel("IconPickerGrid")
    IconPickerGrid(
        selectedIcon = selectedIcon,
        onIconSelected = { selectedIcon = it },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        showIndices = true
    )
}

/** AccentColorPicker's Phase-61 curated demo, reused verbatim as its Variants. */
@Composable
private fun AccentColorPickerVariants() {
    var selectedColor by remember { mutableStateOf(0xFF6750A4L) }
    SectionLabel("AccentColorPicker")
    AccentColorPicker(
        selectedColor = selectedColor,
        onColorSelected = { selectedColor = it },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        showIndices = true
    )
}

/** CropOverlay free-crop fixture — small bitmap dims, no aspect-ratio lock, no prior selection. */
@Composable
private fun CropOverlayFreeCropPreview() {
    Box(modifier = Modifier.fillMaxWidth().height(240.dp)) {
        CropOverlay(
            bitmapWidth = 400,
            bitmapHeight = 300,
            aspectRatio = null,
            initialCropBounds = null,
            onCropBoundsChanged = { _, _, _, _ -> },
            modifier = Modifier.fillMaxSize()
        )
    }
}

/** CropOverlay fixed-aspect-ratio fixture (1:1) — exercises the aspect-lock branch. */
@Composable
private fun CropOverlayFixedAspectPreview() {
    Box(modifier = Modifier.fillMaxWidth().height(240.dp)) {
        CropOverlay(
            bitmapWidth = 400,
            bitmapHeight = 300,
            aspectRatio = 1f,
            initialCropBounds = null,
            onCropBoundsChanged = { _, _, _, _ -> },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun CropOverlayVariants() {
    SectionLabel("CropOverlay — free crop")
    CropOverlayFreeCropPreview()
    SectionLabel("CropOverlay — fixed 1:1 aspect ratio")
    CropOverlayFixedAspectPreview()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PickersFamilyTopBar(onNavigateBack: () -> Unit, themeMode: ThemeMode, onToggleTheme: () -> Unit) {
    TopAppBar(
        title = { Text("Pickers") },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = { ExplorerThemeToggleAction(themeMode = themeMode, onToggleTheme = onToggleTheme) }
    )
}

