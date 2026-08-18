package com.example.secondbrain.yahirandroidtaste.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import java.io.File

/**
 * Resolves the File to show as the create-mode image preview from the captured/imported
 * [photoPaths]. Pure, unit-testable seam (LAYOUT-05).
 *
 * The paths passed into this sheet's create mode are ABSOLUTE cacheDir temp file paths
 * (produced by the capture flow, e.g. `AlbumCaptureViewModel`) — NOT paths relative to the
 * app's private files directory. This resolves the first path directly via [File], with no
 * private-files-directory join. Returns null when [photoPaths] is empty.
 */
fun previewImageFile(photoPaths: List<String>): File? = photoPaths.firstOrNull()?.let(::File)

/**
 * Rename-mode dirty predicate (ALBUM-07, D-03): trimmed Kotlin String (in)equality, no Unicode
 * normalization applied. A whitespace-only edit (e.g. " Foo" vs "Foo") resolves not-dirty so the
 * Save/Rename button doesn't false-flicker enabled; an edit that clears the title entirely (""
 * vs a non-empty [baseline]) resolves dirty.
 */
internal fun isRenameDirty(title: String, baseline: String): Boolean = title.trim() != baseline.trim()

/**
 * Title confirmation bottom sheet displayed after photos are captured or imported (create mode),
 * or when renaming an existing album (rename mode).
 *
 * Per D-02 (Phase 52): Pre-fills the title field with [defaultTitle] (a timestamp string like
 * "Album May 7, 2026 3:42 PM"). Dismissing the sheet (back/swipe) calls [onDismiss] with the
 * CURRENT field text and the CALLER auto-saves it as-is — blank stays blank (ALBUM-04 D-01/D-02);
 * no card is lost, only the name is optional.
 *
 * KEY DIFFERENCE from EmptyApp4's SharedImageTitleConfirmSheet:
 * - Title state initialized to [defaultTitle] (NOT empty string) — per Pitfall 7 in RESEARCH.
 * - Accepts a list of [photoPaths] (absolute cacheDir temp paths, create mode) instead of a
 *   single filePath.
 * - The first photo is shown as a preview; count badge shown if more than one photo.
 *
 * Phase 52 (D-01/D-02/D-03): rides [SheetScaffold] for chrome and [NameAndTagsEditor] for its
 * body. The old hand-rolled `ModalBottomSheet` + inline preview/field/button layout is REPLACED
 * (not patched) — [NameAndTagsEditor]'s single dynamic `header` slot carries the image preview in
 * create mode; in rename mode the header is empty (D-03 — no card-type branching inside the
 * editor itself, the branching is only which header this caller supplies).
 *
 * @param photoPaths Absolute cacheDir temp file paths (create mode) of the captured/imported
 *   photos, resolved via [previewImageFile]. May be empty.
 * @param defaultTitle Timestamp title to pre-fill (e.g. "Album May 7, 2026 3:42 PM").
 * @param isRenameMode When true: hides image preview (empty header), CTA is "Rename Album".
 *   When false (default): create mode with image-preview header and "Save Album" CTA.
 * @param onSave Called with the user's (possibly edited) title when the primary CTA is tapped.
 *   Persists the trimmed field text as-is (ALBUM-04 D-01) — no default-name coercion on blank.
 * @param onDismiss Called with the current (possibly edited) field text when Cancel is tapped or
 *   the sheet is dismissed (back/swipe). ALBUM-04 D-02: the caller auto-saves with whatever text
 *   is currently in the field (blank stays blank) — NOT a forced default title.
 * @param onDiscard Create-mode-only (ALBUM-06/D-01): invoked when the red "Discard" button is
 *   tapped, discarding the in-progress album with no save. Defaults to `{}` so existing rename-mode
 *   call sites (`BrowseScreen`) need no change — rename mode never renders this button (the
 *   `!isRenameMode` seam lives here, not inside `NameAndTagsEditor`). The ambient
 *   `SheetScaffold.onDismissRequest` (back/swipe) is UNCHANGED in both modes — Discard is the only
 *   new no-save path (top-priority ALBUM-04 non-inversion).
 * @param tagContent Tag add/remove widget slot rendered below the title field (Phase 49 Pattern
 *   2; net-new for create mode per Phase 52 G2-03c). `:designsystem` cannot import `:app`'s
 *   `TagChipEditor` directly (one-directional module graph) — the caller (`AlbumCaptureHost` for
 *   create, `BrowseScreen` for rename) fills this slot. Rendered whenever supplied, in both modes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumTitleConfirmSheet(
    photoPaths: List<String>,
    defaultTitle: String,
    isRenameMode: Boolean = false,
    onSave: (title: String) -> Unit,
    onDismiss: (title: String) -> Unit,
    onDiscard: () -> Unit = {},
    tagContent: (@Composable () -> Unit)? = null
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Per D-02 and Pitfall 7: initialize to defaultTitle, NOT empty string
    var title by remember { mutableStateOf(defaultTitle) }

    SheetScaffold(
        // ALBUM-04 D-02: forward the current field text on back/swipe dismiss, not a forced
        // default (Pitfall 2 — onDismiss and onSave stay textually distinct callbacks).
        onDismissRequest = { onDismiss(title.trim()) },
        sheetState = sheetState
    ) {
        NameAndTagsEditor(
            header = {
                // D-03: one dynamic header slot — create mode supplies the image preview,
                // rename mode supplies nothing (empty, no reserved height per D-10).
                if (!isRenameMode && photoPaths.isNotEmpty()) {
                    // Create-mode photoPaths are ABSOLUTE cacheDir temp paths (produced by the
                    // capture flow) — previewImageFile resolves the first path directly, with no
                    // filesDir join. Pitfall 7 in RESEARCH.md governs only the defaultTitle
                    // pre-fill below, not this preview.
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(previewImageFile(photoPaths))
                            .crossfade(true)
                            .build(),
                        contentDescription = "Image preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )

                    // Photo count badge (shown when more than one photo)
                    if (photoPaths.size > 1) {
                        Text(
                            text = "${photoPaths.size} photos",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp)) // 12dp: preview -> name field
                }
            },
            name = title,
            onNameChange = { title = it },
            nameLabel = "Title",
            tagsContent = { tagContent?.invoke() },
            // ALBUM-04 D-01: persist the trimmed title as-is (do NOT coerce to defaultTitle) so a
            // deliberately-cleared name saves genuinely blank, matching the text/list ADAPT-01
            // precedent.
            onSave = { onSave(title.trim()) },
            // ALBUM-06/ALBUM-07 (D-01): create mode's dismiss slot is the red no-save Discard
            // button (onDiscard); rename mode's dismiss slot stays the existing neutral Cancel,
            // which forwards the current field text exactly as onDismissRequest does above
            // (Pitfall 2 — kept textually distinct from onSave). The !isRenameMode seam lives
            // here, never inside NameAndTagsEditor — Voice call sites are unaffected.
            onDismiss = if (!isRenameMode) onDiscard else { { onDismiss(title.trim()) } },
            dismissLabel = if (isRenameMode) "Cancel" else "Discard",
            dismissDestructive = !isRenameMode,
            // ALBUM-07 (D-03): rename-mode Save/Rename is dirty-gated via isRenameDirty;
            // create-mode Save stays enabled unconditionally (a blank/default title is already a
            // valid save).
            enabled = if (isRenameMode) isRenameDirty(title, defaultTitle) else true,
            saveLabel = if (isRenameMode) "Rename Album" else "Save Album",
            modifier = Modifier.fillMaxWidth()
        )
    }
}
