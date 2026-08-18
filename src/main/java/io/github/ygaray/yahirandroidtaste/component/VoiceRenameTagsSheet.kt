package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Unified name+tags bottom sheet for existing voice notes (RENAME-01, ASSIGN-02/03, D-01).
 *
 * Reached from **both** the "Rename" and "Edit tags" three-dot menu items and the right-swipe→Edit
 * action on [VoiceCard] — all three open the same hosted sheet instance (D-01). Rides the canonical
 * [SheetScaffold] chrome with a [NameAndTagsEditor] body (D-01/D-02): an empty `header` (rename has
 * no header content, D-03) → [name] field → optional [tagContent] slot → Save/Cancel row → 24dp
 * bottom clearance.
 *
 * Per Locked Decision 3: dismiss (back/swipe/Cancel) is a pure no-op close — unlike
 * [AlbumTitleConfirmSheet]'s create mode, this sheet does NOT auto-save on dismiss (the voice note
 * already exists; there is nothing to lose).
 *
 * [tagContent] is a `:app`-filled slot (Pattern 2, module-boundary) — `:designsystem` cannot import
 * `TagChipEditor` (`:app`), so the live tag row is injected by the caller.
 *
 * @param defaultTitle Current voice note title, pre-fills the title field.
 * @param onSave Called with the user's (possibly edited) title when "Rename Voice Note" is tapped.
 *   Falls back to [defaultTitle] when the trimmed field is blank.
 * @param onDismiss Called when Cancel is tapped or the sheet is dismissed (back/swipe). Pure no-op
 *   close — does NOT call [onSave].
 * @param tagContent Optional slot for the live tag row (`:app`'s `TagChipEditor`, keyed by cardId).
 *   Mounted bare directly below the title field — no preceding "Tags" label (Locked Decision 1).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceRenameTagsSheet(
    defaultTitle: String,
    onSave: (title: String) -> Unit,
    onDismiss: () -> Unit,
    tagContent: (@Composable () -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState()

    var title by remember { mutableStateOf(defaultTitle) }

    SheetScaffold(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        NameAndTagsEditor(
            name = title,
            onNameChange = { title = it },
            nameLabel = "Title",
            tagsContent = { tagContent?.invoke() },
            onSave = { onSave(title.trim().ifBlank { defaultTitle }) },
            onDismiss = onDismiss,
            saveLabel = "Rename Voice Note"
        )
    }
}
