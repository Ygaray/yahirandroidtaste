package io.github.ygaray.yahirandroidtaste.component

import io.github.ygaray.yahirandroidtaste.model.TagSortMode
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Compile-only fixture behind `API.md`'s v2.3.0 compatibility statement (Phase 169-05 Task 3,
 * review cycle 2, L159-165 / L170-171@836ead8e3; Planner decision 6).
 *
 * Each of [TagPickerSheet], [TagPickerSheetContent], [TagChipEditorContent] and
 * `RecordingBottomSheetContent` gained a trailing `showTagColors: Boolean = false` appended AFTER
 * their pre-existing last parameter, which is itself function-typed (`onSortModeChange` on the
 * first three, `onDeleteTag` on the fourth). Appending after a function-typed parameter is
 * source-compatible for every v2.2-style call EXCEPT a trailing-lambda call of that callback —
 * Kotlin now binds a trailing lambda to `showTagColors`, the new last parameter, not the callback.
 *
 * This fixture proves (by compiling, never by running — none of these lambdas are invoked) every
 * v2.2-style call shape that DOES stay compatible: named arguments with the former-last callback
 * passed by name INSIDE the parentheses, and — for the two `TagPickerSheet`/`TagPickerSheetContent`
 * overloads — a fully positional call of all eight v2.2.0 parameters, the last also inside the
 * parentheses. `showTagColors` is omitted from every call here, exercising its default.
 *
 * The trailing-lambda form that no longer compiles is deliberately absent — proving a compile
 * failure needs a scratch, never-committed probe (`TrailingLambdaProbe.kt`), whose compiler
 * errors are recorded as evidence in this plan's `169-05-SUMMARY.md`, not in this file.
 */
class ShowTagColorsSourceCompatTest {

    @Test
    fun v22StyleCallShapes_compileAgainstShowTagColorsSignatures() {
        // --- (a) named arguments, former-last callback passed by name inside the parens ---

        val tagPickerSheetNamed: @androidx.compose.runtime.Composable () -> Unit = {
            TagPickerSheet(
                allTags = emptyList(),
                onDone = {},
                onCreate = {},
                onDismiss = {},
                onSortModeChange = { _: TagSortMode -> }
            )
        }

        val tagPickerSheetContentNamed: @androidx.compose.runtime.Composable () -> Unit = {
            TagPickerSheetContent(
                allTags = emptyList(),
                onDone = {},
                onCreate = {},
                onDismiss = {},
                onSortModeChange = { _: TagSortMode -> }
            )
        }

        val recordingBottomSheetContentNamed: @androidx.compose.runtime.Composable () -> Unit = {
            RecordingBottomSheetContent(
                uiState = RecordingSheetUiState.IDLE,
                elapsedSeconds = 0L,
                amplitudeBars = emptyList(),
                titleText = "",
                defaultTitle = "",
                permissionDenied = false,
                onTitleChange = {},
                onPause = {},
                onResume = {},
                onStop = {},
                onSave = {},
                onDiscard = {},
                onDismissRequest = {},
                onSortModeChange = { _: TagSortMode -> }
            )
        }

        val tagChipEditorContentNamed: @androidx.compose.runtime.Composable () -> Unit = {
            TagChipEditorContent(
                currentTags = emptyList(),
                isLastTag = false,
                allTags = emptyList(),
                onRemoveTag = {},
                onAddTags = {},
                onRemoveTagNoUndo = {},
                onCreateTag = {},
                onDeleteTag = { _: String, _: String -> }
            )
        }

        // --- (b) fully positional, all 8 v2.2.0 parameters in declaration order, last inside the
        // parentheses (TagPickerSheet / TagPickerSheetContent only) ---

        val tagPickerSheetPositional: @androidx.compose.runtime.Composable () -> Unit = {
            TagPickerSheet(
                emptySet(),
                emptyList(),
                {},
                {},
                {},
                Int.MAX_VALUE,
                TagSortMode.DEFAULT,
                { _: TagSortMode -> }
            )
        }

        val tagPickerSheetContentPositional: @androidx.compose.runtime.Composable () -> Unit = {
            TagPickerSheetContent(
                emptySet(),
                emptyList(),
                {},
                {},
                {},
                Int.MAX_VALUE,
                TagSortMode.DEFAULT,
                { _: TagSortMode -> }
            )
        }

        assertNotNull(tagPickerSheetNamed)
        assertNotNull(tagPickerSheetContentNamed)
        assertNotNull(recordingBottomSheetContentNamed)
        assertNotNull(tagChipEditorContentNamed)
        assertNotNull(tagPickerSheetPositional)
        assertNotNull(tagPickerSheetContentPositional)
    }
}
