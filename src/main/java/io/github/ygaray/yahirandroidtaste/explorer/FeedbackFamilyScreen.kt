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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.component.AttentionCue
import io.github.ygaray.yahirandroidtaste.component.AttentionCueDefaults
import io.github.ygaray.yahirandroidtaste.component.ConfirmationDialog
import io.github.ygaray.yahirandroidtaste.feedback.FeedbackEvent
import io.github.ygaray.yahirandroidtaste.feedback.FeedbackStackHost
import io.github.ygaray.yahirandroidtaste.feedback.FeedbackTarget
import io.github.ygaray.yahirandroidtaste.feedback.UndoCenterScreen
import io.github.ygaray.yahirandroidtaste.feedback.rememberFeedbackStackState
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import kotlinx.coroutines.delay
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
    ),
    ComponentRegistry.Entry(
        name = "FeedbackStackHost",
        family = ExplorerFamilies.FEEDBACK,
        states = listOf(
            ComponentRegistry.StateCell(
                "Default",
                render = {
                    val state = rememberFeedbackStackState()
                    LaunchedEffect(state) {
                        state.show(
                            FeedbackEvent.Notify(
                                message = "Item saved",
                                duration = SnackbarDuration.Indefinite,
                                target = FeedbackTarget("demo-item", "Open item"),
                                supportingText = "Draft"
                            )
                        )
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                        FeedbackStackHost(
                            state = state,
                            onOpenTarget = {},
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }
            ),
            // A tap is transient: it is covered by the Variants demo and Gate-1 self-UAT — no
            // separate static pressed/selected preview.
            ComponentRegistry.StateCell("Pressed / Selected"),
            // FeedbackStackHost has no disabled param — N/A.
            ComponentRegistry.StateCell("Disabled"),
            // FeedbackStackHost has no focus-visual override — N/A.
            ComponentRegistry.StateCell("Focused")
        ),
        content = { FeedbackStackHostVariants() },
        tier = ComponentRegistry.Tier.PATTERN
    )
)

/**
 * Feedback family (SHOW-01, D-04): a registry-filtered row-list picker for `ConfirmationDialog`,
 * `UndoCenterScreen`, `AttentionCue` and `FeedbackStackHost` (4 rows). `FeedbackEvent` gets no row
 * of its own — it is non-visual infra, not a registered component — but its demo is preserved
 * inside [UndoCenterScreenVariants] (D-02).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackFamilyScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit
) {
    ExplorerTheme(themeMode) {
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
    UndoGroupsDemos()
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

/** Gap between the cards of the "Burst x4" demo button. */
private const val BURST_SPACING_MILLIS = 350L

/** The four Indefinite events of "Burst x4": a targeted note, one with Undo, a plain note, a target. */
private fun burstEvents(): List<FeedbackEvent> = listOf(
    FeedbackEvent.Notify(
        message = "Burst one",
        duration = SnackbarDuration.Indefinite,
        target = FeedbackTarget("demo-item", "Open item"),
        supportingText = "Second line"
    ),
    FeedbackEvent.WithUndo(
        message = "Burst two",
        undoAction = {},
        duration = SnackbarDuration.Indefinite,
        target = FeedbackTarget("demo-item", "Open item")
    ),
    FeedbackEvent.Notify(message = "Burst three", duration = SnackbarDuration.Indefinite),
    FeedbackEvent.Notify(
        message = "Burst four",
        duration = SnackbarDuration.Indefinite,
        target = FeedbackTarget("demo-item", "Open item")
    )
)

/**
 * FeedbackStackHost's interactive demo: buttons push cards (with a target, with Undo and a target,
 * plain, a four-card burst, a 4 s note and a 10 s note with Undo) through `state.show`, the host
 * renders the deck, and the last result and opened key are shown as text. A plain `show` only
 * reports Undone; it never runs an undo lambda.
 */
@Composable
private fun FeedbackStackHostVariants() {
    val state = rememberFeedbackStackState()
    val scope = rememberCoroutineScope()
    var lastResult by remember { mutableStateOf("none") }
    var lastOpened by remember { mutableStateOf("none") }

    fun push(event: FeedbackEvent) {
        scope.launch { lastResult = state.show(event).name }
    }

    SectionLabel("FeedbackStackHost — push cards, tap the body or Undo")
    Button(
        onClick = {
            push(
                FeedbackEvent.Notify(
                    message = "Item saved",
                    duration = SnackbarDuration.Indefinite,
                    target = FeedbackTarget("demo-item", "Open item"),
                    supportingText = "Draft"
                )
            )
        },
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text("Note with target")
    }
    Button(
        onClick = {
            push(
                FeedbackEvent.WithUndo(
                    message = "Item deleted",
                    undoAction = {},
                    duration = SnackbarDuration.Indefinite,
                    target = FeedbackTarget("demo-item", "Open item")
                )
            )
        },
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text("Note with Undo and target")
    }
    Button(
        onClick = {
            push(FeedbackEvent.Notify(message = "Plain note", duration = SnackbarDuration.Indefinite))
        },
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text("Plain note")
    }
    Button(
        onClick = {
            // Four Indefinite cards 350 ms apart: the 4th arrival evicts the oldest, so the deck, the
            // fan and the swipe can be exercised without a timer racing the tester.
            scope.launch {
                burstEvents().forEachIndexed { index, event ->
                    if (index > 0) delay(BURST_SPACING_MILLIS)
                    launch { lastResult = state.show(event).name }
                }
            }
        },
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text("Burst x4")
    }
    Button(
        onClick = {
            push(
                FeedbackEvent.Notify(
                    message = "Short note",
                    duration = SnackbarDuration.Short,
                    target = FeedbackTarget("demo-item", "Open item")
                )
            )
        },
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text("Short note")
    }
    Button(
        onClick = {
            push(
                FeedbackEvent.WithUndo(
                    message = "Long note",
                    undoAction = {},
                    duration = SnackbarDuration.Long,
                    target = FeedbackTarget("demo-item", "Open item")
                )
            )
        },
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text("Long note with Undo")
    }
    Button(
        onClick = { state.dismissAll() },
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text("Dismiss all")
    }
    Text(
        text = "Last result: $lastResult, last opened: $lastOpened",
        modifier = Modifier.padding(horizontal = 16.dp)
    )
    Box(modifier = Modifier.fillMaxWidth().height(240.dp).padding(horizontal = 16.dp)) {
        FeedbackStackHost(
            state = state,
            onOpenTarget = { lastOpened = it.key },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
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

