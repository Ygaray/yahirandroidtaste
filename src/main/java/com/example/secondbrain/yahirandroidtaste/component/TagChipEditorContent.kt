package com.example.secondbrain.yahirandroidtaste.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.secondbrain.yahirandroidtaste.model.TagChipUiModel
import com.example.secondbrain.yahirandroidtaste.model.TagSortMode
import com.example.secondbrain.yahirandroidtaste.theme.Dimens

/**
 * Pure inline chip strip composable consumed by every card editor surface (D-02b), extracted
 * from the :app-resident `TagChipEditor` (Phase 46 Plan 2, D-04/D-05 clean 1:1 split).
 *
 * Shows current tags via [TagChipWithContextMenu] (D-04, Phase 93 TMENU-05) — long-press opens
 * Edit + "Remove from this card" ([onRemoveTag]) + "Delete tag everywhere" ([onDeleteTag]) — and
 * an "Add tag" [AssistChip] that opens [TagPickerSheet]. When [isLastTag] is true, an
 * [AnimatedVisibility]-wrapped warning row appears BEFORE the removal (D-03 / D-03a).
 *
 * UI-SPEC §1 (chip strip) + §2 (last-tag warning) — layout, spacing, color, copy exact.
 *
 * Zero ViewModel/Hilt/feature/Room coupling (App-Coupling Test) — all data is hoisted as params
 * and all mutation intents are forwarded as callbacks. The 4-second Undo Snackbar after a scoped
 * removal is owned entirely by the :app wrapper's ViewModel; this composable only invokes
 * [onRemoveTag] and lets the caller drive the undo state.
 *
 * @param currentTags       Tags currently on the card (or staged for a new card).
 * @param isLastTag         True when exactly one tag remains — drives the inline warning row.
 * @param allTags           All active tags for the [TagPickerSheet] autocomplete/picker list.
 * @param onRemoveTag       Invoked with a tag id when the current-tags menu's "Remove from this
 *                          card" item is tapped (undo-window path — mirrors the host's
 *                          undo-not-confirm removal). Scoped removal only — never routes to
 *                          [onDeleteTag] (T-93-02).
 * @param onAddTags         Invoked with the batch of newly selected tag ids when the picker's
 *                          Done button commits a diff that includes additions.
 * @param onRemoveTagNoUndo Invoked per tag id the picker's Done button removed (no undo window —
 *                          the user already made a deliberate picker selection).
 * @param onCreateTag       Invoked with the trimmed typed name when the picker's "Create" row
 *                          is tapped.
 * @param modifier          Applied to the outer Column.
 * @param onEditTag         Invoked with a tag id when the current-tags menu's "Edit" item is
 *                          tapped (TMENU-05, D-04). Nullable, defaults to null so call sites
 *                          that don't wire an Edit sheet keep compiling — a null value omits
 *                          the "Edit" menu item entirely ([TagChipWithContextMenu] semantics).
 * @param onDeleteTag       Invoked with a tag id + display name when the current-tags menu's
 *                          "Delete tag everywhere" item is tapped (TMENU-05, D-04). Nullable,
 *                          defaults to null so call sites that don't wire a global-delete
 *                          handler keep compiling — a null value omits the "Delete tag
 *                          everywhere" menu item entirely.
 * @param sortMode          Currently active tag sort mode (D-02). Defaulted so existing callers
 *                          keep compiling until the :app wrapper wires a real value (Phase 48
 *                          Plan 05). Forwarded straight into the launched [TagPickerSheet] so both
 *                          canonical widgets share the identical sort state.
 * @param onSortModeChange  Invoked when the leading [SortControl] selects a new mode. Defaulted
 *                          to a no-op. This composable never sorts [currentTags]/[allTags] itself
 *                          — sorted lists arrive pre-ordered from the ViewModel layer.
 * @param showSortControl   Render-only gate for the leading [SortControl] (G2-02/G2-04, D-07).
 *                          Defaults `true` for backward compatibility with existing callers
 *                          (e.g. [TagPickerSheet]'s own sort surface is unaffected either way).
 *                          `sortMode`/`onSortModeChange` are always forwarded to the launched
 *                          [TagPickerSheet] regardless of this flag — it never touches the
 *                          sort data plane, only whether the inline control node is emitted here.
 */
@Composable
fun TagChipEditorContent(
    currentTags: List<TagChipUiModel>,
    isLastTag: Boolean,
    allTags: List<TagChipUiModel>,
    onRemoveTag: (String) -> Unit,
    onAddTags: (List<String>) -> Unit,
    onRemoveTagNoUndo: (String) -> Unit,
    onCreateTag: (String) -> Unit,
    modifier: Modifier = Modifier,
    sortMode: TagSortMode = TagSortMode.DEFAULT,
    onSortModeChange: (TagSortMode) -> Unit = {},
    showSortControl: Boolean = true,
    onEditTag: ((tagId: String) -> Unit)? = null,
    onDeleteTag: ((tagId: String, name: String) -> Unit)? = null
) {
    var showPicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        // --- Last-tag inline warning (D-03 / D-03a): visible BEFORE the user taps X ---
        AnimatedVisibility(
            visible = isLastTag,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null, // adjacent Text provides semantic context
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Removing this tag will leave the card untagged",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // --- Chip strip — migrated onto the shared :designsystem ChipBar container (WIDGET-04,
        // D-15/D-16, behavior-preserving). ChipBar's default fillMaxWidth + testTag are additive
        // (this site had neither before) — no behavior change, only the container is replaced.
        ChipBar(
            items = currentTags,
            key = { it.id },
            leadingContent = {
                // Leading sort affordance (D-02/D-13 primary placement — UI-SPEC Locked Decision
                // 2). A quiet utility control, not a primary focal point; the dedicated fallback
                // row is a Gate-1 device decision only, not built here. Suppressed entirely
                // (G2-02/G2-04, D-07) when showSortControl is false — no ghost/disabled node is
                // left behind.
                if (showSortControl) {
                    SortControl(
                        sortMode = sortMode,
                        options = TagSortMode.entries,
                        optionLabel = { it.label },
                        onSortModeChange = onSortModeChange,
                        modifier = Modifier.size(Dimens.Icons.MenuButton)
                    )
                }
            },
            itemContent = { tag ->
                // TMENU-05 (D-04): the trailing ✕ IconButton is retired — scoped removal now
                // flows through the shared long-press menu's "Remove from this card" item,
                // which reuses onRemoveTag verbatim (T-93-02: never aliases to onDeleteTag).
                TagChipWithContextMenu(
                    label = tag.name,
                    isSelected = true,
                    onClick = {},
                    modifier = Modifier.semantics { contentDescription = "${tag.name} tag" },
                    onEdit = onEditTag?.let { edit -> { edit(tag.id) } },
                    onRemoveFromContext = { onRemoveTag(tag.id) },
                    removeLabel = "Remove from this card",
                    onDelete = onDeleteTag?.let { del -> { del(tag.id, tag.name) } }
                )
            },
            trailingContent = {
                // "Add tag" AssistChip — opens TagPickerSheet (UI-SPEC §1)
                AssistChip(
                    onClick = { showPicker = true },
                    label = { Text("Add tag", style = MaterialTheme.typography.labelLarge) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        )
    }

    // --- TagPickerSheet modal ---
    if (showPicker) {
        TagPickerSheet(
            existingTagIds = currentTags.map { it.id }.toSet(),
            allTags = allTags,
            onDone = { finalIds ->
                val existingIds = currentTags.map { it.id }
                val toAdd = finalIds.filter { it !in existingIds }
                val toRemove = existingIds.filter { it !in finalIds }
                if (toAdd.isNotEmpty()) onAddTags(toAdd)
                toRemove.forEach { tagId -> onRemoveTagNoUndo(tagId) }
                showPicker = false
            },
            onCreate = { name -> onCreateTag(name) },
            onDismiss = { showPicker = false },
            sortMode = sortMode,
            onSortModeChange = onSortModeChange
        )
    }
}
