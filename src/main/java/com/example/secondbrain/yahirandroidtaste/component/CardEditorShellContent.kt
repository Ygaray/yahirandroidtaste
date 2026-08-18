package com.example.secondbrain.yahirandroidtaste.component

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Pure Scaffold + TopAppBar chrome shared by all card editors (D-02, D-14 successor).
 *
 * Carries ONLY generic presentation chrome: the readOnly-aware BackHandler (save-on-back vs.
 * plain back), the TopAppBar (ArrowBack navigationIcon, Check-save action gated on !readOnly,
 * accent-color container), and the imePadding body Column rendering [header] then [content].
 * No app-specific wiring (Hilt, feature.* navigation, relationship/tag logic) lives here — that
 * stays in the :app `CardEditorShell` wrapper, which fills [header] with its own composables
 * (Phase 46 Plan 3, D-02/D-05).
 *
 * @param accentColor Module accent color as Long (from ModuleEntity.color). Null -> colorScheme.surface.
 * @param onSave Called on Back press and Check button tap. Caller handles NonCancellable save.
 * @param onNavigateBack Called after onSave to pop the back stack.
 * @param readOnly When true, suppresses the Check-save IconButton and replaces the save-on-back
 *   BackHandler with a plain back (no save). Default false keeps all existing callers unchanged.
 *   Prevents accidental write-back when previewing soft-deleted (trashed) cards (T-11-07).
 * @param title Optional TopAppBar title text (Phase 65 D-01/D-02). Null (default) keeps the title
 *   slot empty, identical to today's Text/List editors. When non-null, renders at
 *   `titleLarge`/1-line/ellipsized (e.g. Album's viewer title).
 * @param showSaveAction Gates ONLY the visible Check/Save IconButton (Phase 65). Does NOT affect
 *   [BackHandler] semantics — Back still follows [readOnly] alone. Default true preserves existing
 *   Text/List editor behavior; viewers (Album/Voice, no deferred save) pass false.
 * @param onRelatedClick Presence-flag gate for the shared Related action (EDIT-01, D-04/D-05/D-09):
 *   non-null renders an icon-only Link IconButton (contentDescription "Related") in the actions
 *   row; null (default) renders nothing. Callers compute this under their own `!readOnly` gate.
 * @param contentBottomPadding Bottom padding applied to the body Column.
 * @param trailingContent Optional slot in TopAppBar actions row (used for overflow menus, or
 *   Restore/Delete-permanently actions in a trashed-card preview). Renders FIRST in the actions
 *   row (Phase 65 intentional reorder, D-01/D-05), followed by Related then Save.
 * @param header Optional slot rendered above [content] inside the body Column — the :app wrapper
 *   fills this with its tag/relationship app-wiring (T-46-05).
 * @param floatingActionButton Optional Scaffold-layer FAB slot (D-12, WIDGET-03). Forwarded
 *   directly to the inner [Scaffold]'s `floatingActionButton` param — a sibling slot Material3
 *   positions at [Alignment.BottomEnd] with default margins, NOT inside the imePadding'd body
 *   Column below. Default `{}` keeps Text/Voice/Album/Video editors unaffected; only
 *   ListCardEditorScreen passes a non-default value (the "Add item" ExtendedFloatingActionButton).
 * @param content Composable content body rendered inside the body Column below [header].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardEditorShellContent(
    accentColor: Long?,
    onSave: () -> Unit,
    onNavigateBack: () -> Unit,
    readOnly: Boolean = false,
    title: String? = null,
    showSaveAction: Boolean = true,
    onRelatedClick: (() -> Unit)? = null,
    contentBottomPadding: Dp = 24.dp,
    trailingContent: @Composable RowScope.() -> Unit = {},
    header: @Composable ColumnScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    // T-11-07: when readOnly, back never triggers onSave — prevents stale-data write-back
    // for trashed-card previews. Default (readOnly = false) preserves existing behaviour.
    if (readOnly) {
        BackHandler { onNavigateBack() }
    } else {
        BackHandler {
            try { onSave() } finally { onNavigateBack() }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (title != null) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (readOnly) {
                            onNavigateBack()
                        } else {
                            try { onSave() } finally { onNavigateBack() }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Phase 65 D-01/D-05: trailingContent() renders FIRST (intentional reorder from
                    // today's Check-before-trailingContent order), then Related (if onRelatedClick !=
                    // null), then Save/Check (if showSaveAction && !readOnly). Related sits in one
                    // predictable relative slot: immediately before Save when Save exists, rightmost
                    // otherwise. Do NOT "fix" this order back — see 65-UI-SPEC.md.
                    trailingContent()
                    if (onRelatedClick != null) {
                        IconButton(onClick = onRelatedClick) {
                            Icon(Icons.Default.Link, contentDescription = "Related")
                        }
                    }
                    if (showSaveAction && !readOnly) {
                        IconButton(onClick = {
                            try { onSave() } finally { onNavigateBack() }
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "Save")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = accentColor?.let { Color(it) }
                        ?: MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = floatingActionButton
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = contentBottomPadding)
                .imePadding()
        ) {
            header()
            content()
        }
    }
}
