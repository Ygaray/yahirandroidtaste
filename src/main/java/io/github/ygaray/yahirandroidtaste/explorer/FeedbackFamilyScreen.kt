package io.github.ygaray.yahirandroidtaste.explorer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.component.AttentionCue
import io.github.ygaray.yahirandroidtaste.component.AttentionCueDefaults
import io.github.ygaray.yahirandroidtaste.component.ConfirmationDialog
import io.github.ygaray.yahirandroidtaste.feedback.FeedbackEvent
import io.github.ygaray.yahirandroidtaste.feedback.UndoCenterScreen
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import kotlinx.coroutines.launch

/**
 * D-05: this family's slice of [ComponentRegistry.entries], declared here (not in
 * `ComponentRegistry.kt`) so this file stays the single place Plans 03-05 enrich Feedback's
 * `states`/`content` without touching the shared registry file. Order preserved exactly from
 * the pre-Phase-62-Plan-02 inline registry list.
 *
 * `FeedbackEvent` is non-visual infra with no registered entry of its own (D-04) — its Phase-61
 * demo is preserved (D-02) inside [UndoCenterScreenVariants] rather than discarded, since the
 * Undo Center is the surface that visibly reacts to dispatched feedback.
 */
internal val feedbackFamilyEntries: List<ComponentRegistry.Entry> = listOf(
    ComponentRegistry.Entry(
        name = "ConfirmationDialog",
        family = ExplorerFamilies.FEEDBACK,
        states = listOf(
            ComponentRegistry.StateCell(
                "Default",
                render = {
                    ConfirmationDialog(
                        title = "Delete card?",
                        body = "This card will be moved to Trash and can be restored within 30 days.",
                        onDismissRequest = {},
                        onConfirm = {}
                    )
                }
            ),
            // ConfirmationDialog only has a shown/dismissed state, already demonstrated as
            // Default — no separate discrete pressed/selected visual.
            ComponentRegistry.StateCell("Pressed / Selected"),
            // ConfirmationDialog has no disabled param — N/A.
            ComponentRegistry.StateCell("Disabled"),
            // ConfirmationDialog has no focus-visual override — N/A.
            ComponentRegistry.StateCell("Focused")
        ),
        content = { ConfirmationDialogVariants() },
        tier = ComponentRegistry.Tier.PRIMITIVE
    ),
    ComponentRegistry.Entry(
        name = "UndoCenterScreen",
        family = ExplorerFamilies.FEEDBACK,
        states = listOf(
            ComponentRegistry.StateCell(
                "Default",
                render = {
                    Box(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                        UndoCenterScreen(
                            entries = ExplorerFakeData.undoHistoryEntries,
                            onNavigateBack = {},
                            onUndo = {},
                            onClear = {}
                        )
                    }
                }
            ),
            // Undo/clear tap interactions are Gate-1 self-UAT's job — no separate static
            // pressed/selected preview.
            ComponentRegistry.StateCell("Pressed / Selected"),
            // UndoCenterScreen has no disabled param — N/A.
            ComponentRegistry.StateCell("Disabled"),
            // UndoCenterScreen has no focus-visual override — N/A.
            ComponentRegistry.StateCell("Focused")
        ),
        content = { UndoCenterScreenVariants() },
        tier = ComponentRegistry.Tier.PATTERN
    ),
    ComponentRegistry.Entry(
        name = "AttentionCue",
        family = ExplorerFamilies.FEEDBACK,
        states = listOf(
            ComponentRegistry.StateCell(
                "Default",
                render = {
                    AttentionCue(
                        text = "Double-check this value",
                        style = AttentionCueDefaults.Style.Inline,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            ),
            // AttentionCue is a non-interactive glyph — no press/select semantics — N/A.
            ComponentRegistry.StateCell("Pressed / Selected"),
            // AttentionCue has no enabled param — N/A.
            ComponentRegistry.StateCell("Disabled"),
            // AttentionCue has no focus-visual override — N/A.
            ComponentRegistry.StateCell("Focused")
        ),
        content = { AttentionCueVariants() },
        tier = ComponentRegistry.Tier.PRIMITIVE
    )
)

/**
 * Feedback family (SHOW-01, D-04): a registry-filtered row-list picker for `ConfirmationDialog`
 * and `UndoCenterScreen` (2 rows). `FeedbackEvent` gets no row of its own — it is non-visual
 * infra, not a registered component — but its demo is preserved inside
 * [UndoCenterScreenVariants] (D-02).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackFamilyScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit
) {
    YahirAndroidTasteTheme(themeMode = themeMode) {
        Scaffold(
            topBar = { FeedbackFamilyTopBar(onNavigateBack, themeMode, onToggleTheme) }
        ) { paddingValues ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                items(
                    ComponentRegistry.entries.filter { it.family == ExplorerFamilies.FEEDBACK },
                    key = { it.name }
                ) { entry ->
                    ComponentRow(name = entry.name, tier = entry.tier, onClick = { onNavigateToDetail(entry.name) })
                }
            }
        }
    }
}

/** ConfirmationDialog's Phase-61 curated demo, reused verbatim as its Variants. */
@Composable
private fun ConfirmationDialogVariants() {
    var showDialog by remember { mutableStateOf(true) }
    SectionLabel("ConfirmationDialog — shown state")
    Button(
        onClick = { showDialog = true },
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text("Show confirmation dialog")
    }
    if (showDialog) {
        ConfirmationDialog(
            title = "Delete card?",
            body = "This card will be moved to Trash and can be restored within 30 days.",
            onDismissRequest = { showDialog = false },
            onConfirm = { showDialog = false }
        )
    }
}

/**
 * UndoCenterScreen's Phase-61 curated demo, reused verbatim, PLUS the `FeedbackEvent` demo folded
 * in beneath it (D-02: `FeedbackEvent` has no registered entry of its own — non-visual infra —
 * so its Phase-61 demo is preserved here, in context, rather than discarded).
 */
@Composable
private fun UndoCenterScreenVariants() {
    SectionLabel("UndoCenterScreen — 3 representative preview variants")
    Box(modifier = Modifier.fillMaxWidth().height(400.dp)) {
        UndoCenterScreen(
            entries = ExplorerFakeData.undoHistoryEntries,
            onNavigateBack = {},
            onUndo = {},
            onClear = {}
        )
    }

    // FeedbackEvent is non-visual infra with no registered entry of its own — its demo is
    // preserved here (D-02), since the Undo Center is the surface that visibly reacts to
    // dispatched feedback.
    val scope = rememberCoroutineScope()
    SectionLabel("FeedbackEvent — dispatched via the no-op stub")
    Button(
        onClick = {
            scope.launch {
                ExplorerFeedbackStub.emit(FeedbackEvent.Notify("Sample feedback event"))
            }
        },
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text("Emit sample feedback event")
    }
}

/** AttentionCue's two real usage shapes, rendered side by side as its Variants. */
@Composable
private fun AttentionCueVariants() {
    SectionLabel("AttentionCue — Inline style")
    AttentionCue(
        text = "Double-check this value",
        style = AttentionCueDefaults.Style.Inline,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
    SectionLabel("AttentionCue — Dot style")
    AttentionCue(
        text = null,
        style = AttentionCueDefaults.Style.Dot,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedbackFamilyTopBar(onNavigateBack: () -> Unit, themeMode: ThemeMode, onToggleTheme: () -> Unit) {
    TopAppBar(
        title = { Text("Feedback") },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = { ExplorerThemeToggleAction(themeMode = themeMode, onToggleTheme = onToggleTheme) }
    )
}

