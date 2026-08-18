package com.example.secondbrain.yahirandroidtaste.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.secondbrain.yahirandroidtaste.theme.Dimens

/**
 * Canonical name+tags edit archetype (D-02/D-03).
 *
 * Extracted from `AlbumTitleConfirmSheet`'s rename-mode layout: a single dynamic [header] slot
 * (e.g. album image preview, voice-capture recording controls, or nothing), a
 * [ClearableTextField] name field, a caller-supplied [tagsContent] block (e.g.
 * `TagChipEditorContent` with its sort control omitted per D-07), and a Save/Cancel button row.
 *
 * Content-only — this archetype renders NO `SheetScaffold`/`ModalBottomSheet` chrome and NO
 * `SortControl` (sort lives only on `TagPickerSheet`, D-07). Adopters own the sheet chrome and
 * supply [tagsContent] with sort disabled.
 *
 * D-10 (conditional render, no dead space): [header] defaults to an empty lambda that composes
 * nothing — it is called directly with no fixed-height wrapper, so an empty header reserves no
 * space at all. Callers that DO render header content (image preview, recording controls) are
 * responsible for including their own trailing 12dp spacer as part of that content (matching
 * `AlbumTitleConfirmSheet`'s existing preview→field rhythm) — this archetype does not branch on
 * header emptiness, and contains no card-type `if` branching anywhere in its body (D-03).
 *
 * @param header Single dynamic header slot rendered above the name field. Defaults to empty (no
 *   header rendered, no reserved height). E.g. album image preview or voice-capture recording
 *   controls/waveform.
 * @param name Current name/title field value.
 * @param onNameChange Invoked on every edit of the name field.
 * @param nameLabel Field label — e.g. "Title" (album) or "Name" (voice); caller-supplied, never
 *   hardcoded here for either card type.
 * @param tagsContent Tag add/remove widget slot (e.g. `TagChipEditorContent` with its sort
 *   control omitted), rendered below the name field with an 8dp leading spacer.
 * @param onSave Invoked when the primary CTA (labelled [saveLabel]) is tapped.
 * @param onDismiss Invoked when the dismiss button (labelled [dismissLabel]) is tapped.
 * @param saveLabel Primary CTA label — e.g. "Save Album" | "Rename Album" | "Rename" |
 *   "Save Recording".
 * @param dismissLabel Dismiss button label. Defaults to "Cancel" for the common edit/rename
 *   surfaces. Callers whose dismiss action is destructive (e.g. voice-capture "Discard", which
 *   throws the recording away) pass their own label here.
 * @param dismissDestructive When true, the dismiss button renders in the theme error color to
 *   signal a destructive action (G2 UX: destructive affordances stay first-class). Defaults to
 *   false so all standard edit/rename surfaces keep the neutral "Cancel" treatment unchanged.
 * @param enabled Enabled/disabled passthrough for the primary [saveLabel] `DynamicActionButton`
 *   (BTN-01). Defaults to true so every existing caller (Voice rename + recording sheets) keeps
 *   its unconditional-tappable Save unchanged; callers that need dirty-gated Save (e.g. album
 *   rename mode) pass a computed value.
 * @param modifier Applied to the outer [Column].
 */
@Composable
fun NameAndTagsEditor(
    header: @Composable ColumnScope.() -> Unit = {},
    name: String,
    onNameChange: (String) -> Unit,
    nameLabel: String,
    tagsContent: @Composable () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    saveLabel: String,
    dismissLabel: String = "Cancel",
    dismissDestructive: Boolean = false,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        header()

        ClearableTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(nameLabel) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.HorizontalPadding)
        )

        Spacer(modifier = Modifier.height(8.dp)) // 8dp: name field -> tags block

        tagsContent()

        Spacer(modifier = Modifier.height(16.dp)) // 16dp: tags block -> button row

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.HorizontalPadding)
        ) {
            DynamicActionButton(
                label = dismissLabel,
                role = if (dismissDestructive) {
                    ActionButtonDefaults.ActionButtonRole.Destructive
                } else {
                    ActionButtonDefaults.ActionButtonRole.Neutral
                },
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            DynamicActionButton(
                label = saveLabel,
                role = ActionButtonDefaults.ActionButtonRole.Save,
                onClick = onSave,
                enabled = enabled,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp)) // 24dp: bottom nav clearance
    }
}
