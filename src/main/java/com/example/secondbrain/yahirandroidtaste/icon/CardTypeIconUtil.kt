package com.example.secondbrain.yahirandroidtaste.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Maps a card type storage string to the appropriate Material icon vector.
 *
 * Originally extracted from the search-result-row screen (was private) so relationship
 * composables (RelationshipPickerSheet, RelationshipPanelSheet) and the tag-management screen
 * can all share a single source of truth for card-type icon mapping. Relocated into
 * `:designsystem/icon` (Phase 46 Plan 1) — a pure `String -> ImageVector` function with zero
 * app-package imports.
 *
 * Per UI-SPEC: NoteAlt (text), List (list), Mic (voice), PhotoAlbum (album).
 * Unrecognised types fall back to NoteAlt.
 *
 * @param type The card-entity type string (e.g. "TEXT", "LIST", "VOICE", "ALBUM").
 *             Input is uppercased before matching.
 */
fun cardTypeIcon(type: String): ImageVector = when (type.uppercase()) {
    "TEXT"  -> Icons.Default.NoteAlt
    "LIST"  -> Icons.AutoMirrored.Filled.List
    "VOICE" -> Icons.Default.Mic
    "ALBUM" -> Icons.Default.PhotoAlbum
    else    -> Icons.Default.NoteAlt
}
