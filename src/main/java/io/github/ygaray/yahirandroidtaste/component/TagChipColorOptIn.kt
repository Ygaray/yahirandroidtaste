package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.SelectableChipColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Internal opt-in helpers backing the `showTagColors: Boolean = false` parameter appended to
 * [TagPickerSheet], [TagPickerSheetContent], [TagChipEditorContent] and
 * `RecordingBottomSheetContent` (Phase 169-05, TAGUI-01, Option A —
 * `tagui01-editor-picker-color-gap` runtime decision, 2026-09-28).
 *
 * Contract, shared by every surface these helpers back:
 *  - The hub renders the caller's supplied [io.github.ygaray.yahirandroidtaste.model.TagChipUiModel.color]
 *    AS-IS. It never mutes, clamps or theme-adjusts it — color policy belongs to the consumer
 *    (the TAGCOLOR-01 hub/consumer boundary).
 *  - Selection outranks tag color on every tag surface these helpers touch, mirroring
 *    [AppChip]'s `containerColorOverride` precedence (D-02): a selected/current chip's selected
 *    look always wins over an opted-in tag color.
 *  - `showTagColors = false` (the default on every public composable) is byte-identical to the
 *    hub's pre-v2.3.0 (`v2.2.0`) rendering — these helpers are pure no-ops on that path.
 */
internal fun optedInTagColor(showTagColors: Boolean, tagColor: Color?): Color? =
    if (showTagColors) tagColor else null

/**
 * Whether a current-tag editor chip should render with the Material3 "selected" look. `false`
 * (a resting chip, so [AppChip]'s `containerColorOverride` can take effect) only when the caller
 * has opted in AND the tag actually carries a color; otherwise `true`, matching
 * [TagChipEditorContent]'s pre-v2.3.0 hardcoded `isSelected = true` behavior for every other case
 * (flag off, or a tag with no color).
 */
internal fun editorTagChipIsSelected(showTagColors: Boolean, tagColor: Color?): Boolean =
    optedInTagColor(showTagColors, tagColor) == null

/**
 * [SelectableChipColors] for an unselected `FilterChip` in [TagPickerSheetContent]'s tag list.
 * Passes ONLY `containerColor` to [FilterChipDefaults.filterChipColors] when opted in with a
 * non-null tag color — `selectedContainerColor` keeps its Material3 default, which is what makes
 * a selected chip's own fill win over the tag-color override. Falls back to the FilterChip's own
 * default colors (identical to omitting the `colors` argument) whenever not opted in, or the tag
 * has no color.
 */
@Composable
internal fun pickerTagChipColors(showTagColors: Boolean, tagColor: Color?): SelectableChipColors {
    val opted = optedInTagColor(showTagColors, tagColor)
    return if (opted != null) {
        FilterChipDefaults.filterChipColors(containerColor = opted)
    } else {
        FilterChipDefaults.filterChipColors()
    }
}
