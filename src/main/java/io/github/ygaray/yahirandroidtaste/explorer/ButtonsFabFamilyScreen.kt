package io.github.ygaray.yahirandroidtaste.explorer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.component.ActionButtonDefaults
import io.github.ygaray.yahirandroidtaste.component.CycleSubTypeButton
import io.github.ygaray.yahirandroidtaste.component.DynamicActionButton
import io.github.ygaray.yahirandroidtaste.component.ExpandableFab
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode

/**
 * D-05: this family's slice of [ComponentRegistry.entries], declared here (not in
 * `ComponentRegistry.kt`) so this file stays the single place this plan enriches Buttons/FAB's
 * `states`/`content` without touching the shared registry file. Order preserved exactly from
 * the pre-Phase-62-Plan-02 inline registry list.
 */
internal val buttonsFabFamilyEntries: List<ComponentRegistry.Entry> = listOf(
    ComponentRegistry.Entry(
        name = "ExpandableFab",
        family = ExplorerFamilies.BUTTONS_FAB,
        states = listOf(
            // Default: WR-02 fix — the Default cell used to render the exact same demo Box the
            // Variants section renders below (with an extra "tap to expand" label), so the
            // identical FAB appeared twice on the page for no added information. There is no
            // distinguishing prop to vary between the two (ExpandableFab owns its own
            // expand/collapse state, no static "default vs. expanded" prop exists), so the
            // single interactive demo lives in Variants only — N/A here.
            ComponentRegistry.StateCell("Default"),
            // ExpandableFab owns its expand/collapse state internally — no external expanded
            // param to force the fan open for a static preview. Interactive tap-to-expand is
            // Gate-1 self-UAT's job.
            ComponentRegistry.StateCell("Pressed / Selected"),
            // ExpandableFab has no disabled param — N/A.
            ComponentRegistry.StateCell("Disabled"),
            // ExpandableFab has no focus-visual override — N/A.
            ComponentRegistry.StateCell("Focused")
        ),
        content = { ExpandableFabVariants() }
    ),
    ComponentRegistry.Entry(
        name = "CycleSubTypeButton",
        family = ExplorerFamilies.BUTTONS_FAB,
        states = listOf(
            // Default: BULLETED sub-type — the predictive icon previews the NEXT step (Ordered).
            ComponentRegistry.StateCell("Default") {
                CycleSubTypeButton(currentSubType = "BULLETED", onCycle = {})
            },
            // One tap advances one step; the button owns no pressed/selected visual state — N/A.
            ComponentRegistry.StateCell("Pressed / Selected"),
            // Disabled: standard M3 disabled IconButton (38% alpha); callers gate on !readOnly.
            ComponentRegistry.StateCell("Disabled") {
                CycleSubTypeButton(currentSubType = "BULLETED", onCycle = {}, enabled = false)
            },
            // No focus-visual override beyond the default IconButton ripple — N/A.
            ComponentRegistry.StateCell("Focused")
        ),
        content = { CycleSubTypeButtonVariants() }
    ),
    ComponentRegistry.Entry(
        name = "DynamicActionButton",
        family = ExplorerFamilies.BUTTONS_FAB,
        states = listOf(
            // Default: Save role — the filled-Button widget, the more visually distinct branch.
            ComponentRegistry.StateCell("Default") {
                DynamicActionButton(
                    label = "Save",
                    role = ActionButtonDefaults.ActionButtonRole.Save,
                    onClick = {}
                )
            },
            // The button owns no pressed/selected visual state beyond the default M3 ripple — N/A.
            ComponentRegistry.StateCell("Pressed / Selected"),
            // Disabled: standard M3 disabled treatment for any role's colors — no custom logic.
            ComponentRegistry.StateCell("Disabled") {
                DynamicActionButton(
                    label = "Save",
                    role = ActionButtonDefaults.ActionButtonRole.Save,
                    enabled = false,
                    onClick = {}
                )
            },
            // No focus-visual override beyond the default Button/TextButton focus ring — N/A.
            ComponentRegistry.StateCell("Focused")
        ),
        content = { DynamicActionButtonVariants() }
    )
)

/**
 * Buttons/FAB family (SHOW-01, D-04): a registry-filtered row-list picker. The real
 * `ExpandableFab` resolves to its own detail page (States matrix + Variants); the component owns
 * its own expand/collapse state internally — it renders collapsed by default; tap it to see the
 * expanded fan (Gate-1 self-UAT drives this interaction).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ButtonsFabFamilyScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit
) {
    YahirAndroidTasteTheme(themeMode = themeMode) {
        Scaffold(
            topBar = { ButtonsFabFamilyTopBar(onNavigateBack, themeMode, onToggleTheme) }
        ) { paddingValues ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                items(
                    ComponentRegistry.entries.filter { it.family == ExplorerFamilies.BUTTONS_FAB },
                    key = { it.name }
                ) { entry ->
                    ComponentRow(name = entry.name, onClick = { onNavigateToDetail(entry.name) })
                }
            }
        }
    }
}

/** ExpandableFab's Phase-61 curated demo, reused verbatim as its Variants. */
@Composable
private fun ExpandableFabVariants() {
    SectionLabel("ExpandableFab — tap to expand")
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .padding(horizontal = 16.dp)
    ) {
        ExpandableFab(
            onCreateTextCard = {},
            onCreateListCard = {},
            onCreateVoiceCard = {},
            onAlbumCamera = {},
            onAlbumGallery = {},
            onQuickTake = {}
        )
    }
}

/**
 * CycleSubTypeButton's Variants — the predictive icon always previews the NEXT sub-type (D-08):
 * from BULLETED it shows the Ordered glyph, from ORDERED the Checkbox glyph, from CHECKBOX the
 * Bulleted glyph; the trailing button shows the disabled (read-only) treatment.
 */
@Composable
private fun CycleSubTypeButtonVariants() {
    SectionLabel("CycleSubTypeButton — predictive icon = the NEXT sub-type; last is disabled")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CycleSubTypeButton(currentSubType = "BULLETED", onCycle = {})
        CycleSubTypeButton(currentSubType = "ORDERED", onCycle = {})
        CycleSubTypeButton(currentSubType = "CHECKBOX", onCycle = {})
        CycleSubTypeButton(currentSubType = "BULLETED", onCycle = {}, enabled = false)
    }
}

/**
 * DynamicActionButton's Variants — all 3 roles side by side, previewing the exact labels
 * Phase 82's album-create Discard/Save pair will use (D-01/D-02): Save is the filled Button,
 * Destructive/Neutral are TextButtons with error-red / default content color respectively.
 */
@Composable
private fun DynamicActionButtonVariants() {
    SectionLabel("DynamicActionButton — role drives color; Save is filled, Destructive/Neutral are text")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DynamicActionButton(
            label = "Discard",
            role = ActionButtonDefaults.ActionButtonRole.Destructive,
            onClick = {}
        )
        DynamicActionButton(
            label = "Cancel",
            role = ActionButtonDefaults.ActionButtonRole.Neutral,
            onClick = {}
        )
        DynamicActionButton(
            label = "Save",
            role = ActionButtonDefaults.ActionButtonRole.Save,
            onClick = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ButtonsFabFamilyTopBar(onNavigateBack: () -> Unit, themeMode: ThemeMode, onToggleTheme: () -> Unit) {
    TopAppBar(
        title = { Text("Buttons / FAB") },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = { ExplorerThemeToggleAction(themeMode = themeMode, onToggleTheme = onToggleTheme) }
    )
}

