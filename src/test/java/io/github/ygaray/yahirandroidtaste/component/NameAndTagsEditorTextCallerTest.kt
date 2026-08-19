package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Proves EDIT-01 and EDIT-03's hub-capability precursor per **D-02**: [NameAndTagsEditor] (the
 * already-generic archetype) is proven generic by direct instantiation with a Text-card-shaped
 * caller — no wrapping sheet and no album-only concept from [AlbumTitleConfirmSheet]'s own
 * surface anywhere in this file (its photo list, its create/rename mode flag, or either of its
 * two CTA labels). [AlbumTitleConfirmSheet] is never stretched to serve Text; the actual Text-card
 * sheet swap and menu-row collapse in `:app` are Phase 109 work.
 *
 * Covers both tag-slot shapes the codebase actually uses: a plain required lambda (the archetype's
 * own signature, `tagsContent: @Composable () -> Unit`) and the nullable-wrapper shape every real
 * caller (`AlbumTitleConfirmSheet`, `VoiceRenameTagsSheet`) applies before passing it down — so
 * Phase 109's real Text-card call site has a proven precedent for either.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class NameAndTagsEditorTextCallerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `Text-shaped render with a required tag slot renders all labels and composes the slot`() {
        var name = "My Note"
        var tagSlotRendered = false

        composeTestRule.setContent {
            NameAndTagsEditor(
                name = name,
                onNameChange = { name = it },
                nameLabel = "Title",
                tagsContent = { tagSlotRendered = true },
                onSave = {},
                onDismiss = {},
                saveLabel = "Save",
                dismissLabel = "Cancel",
                dismissDestructive = false
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Title").assertExists()
        composeTestRule.onNodeWithText("Save").assertExists()
        composeTestRule.onNodeWithText("Cancel").assertExists()
        assert(tagSlotRendered)
    }

    @Test
    fun `nullable-wrapper tag slot with content renders the caller-supplied marker`() {
        var name = "My Note"
        val tagContent: (@Composable () -> Unit)? = { Text("TAG_MARKER") }

        composeTestRule.setContent {
            NameAndTagsEditor(
                name = name,
                onNameChange = { name = it },
                nameLabel = "Title",
                tagsContent = { tagContent?.invoke() },
                onSave = {},
                onDismiss = {},
                saveLabel = "Save",
                dismissLabel = "Cancel",
                dismissDestructive = false
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("TAG_MARKER").assertExists()
    }

    @Test
    fun `empty tag selection via nullable-wrapper tag slot renders with no marker and no error`() {
        var name = "My Note"
        val tagContent: (@Composable () -> Unit)? = null

        composeTestRule.setContent {
            NameAndTagsEditor(
                name = name,
                onNameChange = { name = it },
                nameLabel = "Title",
                tagsContent = { tagContent?.invoke() },
                onSave = {},
                onDismiss = {},
                saveLabel = "Save",
                dismissLabel = "Cancel",
                dismissDestructive = false
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Title").assertExists()
        composeTestRule.onNodeWithText("Save").assertExists()
        composeTestRule.onNodeWithText("Cancel").assertExists()
        composeTestRule.onNodeWithText("TAG_MARKER").assertDoesNotExist()
    }

    @Test
    fun `clean form with empty tag selection is not saveable`() {
        val baseline = "My Note"
        var name = baseline
        val tagContent: (@Composable () -> Unit)? = null

        composeTestRule.setContent {
            NameAndTagsEditor(
                name = name,
                onNameChange = { name = it },
                nameLabel = "Title",
                tagsContent = { tagContent?.invoke() },
                onSave = {},
                onDismiss = {},
                saveLabel = "Save",
                dismissLabel = "Cancel",
                dismissDestructive = false,
                enabled = isEditDirty(name, baseline, tagsDirty = false)
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Save").assertIsNotEnabled()
    }

    @Test
    fun `dirty form with empty tag selection but a true tags signal is saveable`() {
        val baseline = "My Note"
        var name = baseline
        val tagContent: (@Composable () -> Unit)? = null

        composeTestRule.setContent {
            NameAndTagsEditor(
                name = name,
                onNameChange = { name = it },
                nameLabel = "Title",
                tagsContent = { tagContent?.invoke() },
                onSave = {},
                onDismiss = {},
                saveLabel = "Save",
                dismissLabel = "Cancel",
                dismissDestructive = false,
                enabled = isEditDirty(name, baseline, tagsDirty = true)
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Save").assertIsEnabled()
    }

    @Test
    fun `no album-specific input is required to compose and render the editor`() {
        // No header slot content and no reference to any album-only concept anywhere in this
        // class — the archetype composes and renders correctly with only the Text-shaped
        // generic contract.
        var name = "My Note"

        composeTestRule.setContent {
            NameAndTagsEditor(
                name = name,
                onNameChange = { name = it },
                nameLabel = "Title",
                tagsContent = {},
                onSave = {},
                onDismiss = {},
                saveLabel = "Save",
                dismissLabel = "Cancel",
                dismissDestructive = false
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Title").assertExists()
    }
}
