package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextReplacement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for [previewImageFile] (LAYOUT-05 D-06 seam).
 *
 * Locks the absolute-path contract: create-mode `photoPaths` are ABSOLUTE cacheDir temp
 * paths produced by the capture flow, so [previewImageFile] must resolve the first path
 * directly via `File(path)` with NO `context.filesDir` join.
 */
class AlbumTitleConfirmSheetTest {

    @Test
    fun `resolves absolute first path unchanged`() {
        val absolutePath = "/data/user/0/io.github.ygaray.yahirandroidtaste/cache/album_gallery_x.jpg"

        val result = previewImageFile(listOf(absolutePath))

        assertEquals(absolutePath, result?.path)
    }

    @Test
    fun `returns null for empty photoPaths`() {
        val result = previewImageFile(emptyList())

        assertNull(result)
    }

    @Test
    fun `ignores all but the first path`() {
        val first = "/data/user/0/io.github.ygaray.yahirandroidtaste/cache/first.jpg"
        val second = "/data/user/0/io.github.ygaray.yahirandroidtaste/cache/second.jpg"

        val result = previewImageFile(listOf(first, second))

        assertEquals(first, result?.path)
    }

    // ── isRenameDirty (ALBUM-07, D-03) ──────────────────────────────────────

    @Test
    fun `isRenameDirty is true when title changed`() {
        assertEquals(true, isRenameDirty("Bar", "Foo"))
    }

    @Test
    fun `isRenameDirty is false for leading whitespace only`() {
        assertEquals(false, isRenameDirty(" Foo", "Foo"))
    }

    @Test
    fun `isRenameDirty is false for trailing whitespace only`() {
        assertEquals(false, isRenameDirty("Foo ", "Foo"))
    }

    @Test
    fun `isRenameDirty is false when identical`() {
        assertEquals(false, isRenameDirty("Foo", "Foo"))
    }

    @Test
    fun `isRenameDirty is true when cleared title vs non-empty baseline`() {
        assertEquals(true, isRenameDirty("", "Foo"))
    }

    // ── isEditDirty (EDIT-02, D-01) ─────────────────────────────────────────

    @Test
    fun `isEditDirty is true when tags dirty only, title unchanged`() {
        assertEquals(true, isEditDirty("Foo", "Foo", tagsDirty = true))
    }

    // ── isEditDirty four-combination matrix (Task 2) ────────────────────────

    @Test
    fun `isEditDirty is false when neither name nor tags dirty`() {
        assertEquals(false, isEditDirty("Foo", "Foo", tagsDirty = false))
    }

    @Test
    fun `isEditDirty is true when name only dirty`() {
        assertEquals(true, isEditDirty("Bar", "Foo", tagsDirty = false))
    }

    @Test
    fun `isEditDirty is true when tags only dirty`() {
        assertEquals(true, isEditDirty("Foo", "Foo", tagsDirty = true))
    }

    @Test
    fun `isEditDirty is true when both name and tags dirty`() {
        assertEquals(true, isEditDirty("Bar", "Foo", tagsDirty = true))
    }

    @Test
    fun `isEditDirty is false for whitespace-only title edit with tags not dirty`() {
        assertEquals(false, isEditDirty(" Foo", "Foo", tagsDirty = false))
    }

    @Test
    fun `isEditDirty is true for whitespace-only title edit when tags dirty`() {
        assertEquals(true, isEditDirty(" Foo", "Foo", tagsDirty = true))
    }

    @Test
    fun `isEditDirty is idempotent across repeated calls with identical arguments`() {
        val first = isEditDirty("Bar", "Foo", tagsDirty = true)
        val second = isEditDirty("Bar", "Foo", tagsDirty = true)

        assertEquals(first, second)
    }
}

/**
 * Compose-level tracer proof for EDIT-02 (D-01): the rendered `Rename Album` control actually
 * enables on a tags-only change, not merely the extracted [isEditDirty] predicate. Closes the
 * silent-wiring-break risk RESEARCH.md Pitfall 2 names — a wiring bug that leaves [isEditDirty]
 * correct but never passes its result to the button would pass a helper-only test suite silently.
 *
 * Harness mirrors [DynamicActionButtonTest] (Robolectric + `createComposeRule()`, no theme
 * wrapper, direct instantiation). `AlbumTitleConfirmSheet` renders through `SheetScaffold`'s
 * `ModalBottomSheet`, a separately-rooted composition window (unlike `DynamicActionButton`'s bare
 * composable) — per 107-01-PLAN.md's precision note this is encouraging-but-not-proven territory,
 * so this test resolves the node with the plain default finder first and only falls back to the
 * click-and-count behavioral equivalent if the enabled-state assertion cannot resolve.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AlbumTitleConfirmSheetComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `tags-only edit enables Save in rename mode`() {
        composeTestRule.setContent {
            AlbumTitleConfirmSheet(
                photoPaths = emptyList(),
                defaultTitle = "My Album",
                isRenameMode = true,
                tagsDirty = true,
                onSave = {},
                onDismiss = {}
            )
        }
        composeTestRule.waitForIdle()

        // Enabled-state assertion form (tried first per the de-risking ladder): resolves the node
        // by its rendered label and asserts it enabled directly, inside SheetScaffold's
        // ModalBottomSheet composition root.
        composeTestRule.onNodeWithText("Rename Album").assertIsEnabled()
    }

    // ── Rendered dirty-gate matrix (Task 2) ─────────────────────────────────
    // Every rendered case pins photoPaths explicitly empty (create-mode cases especially) so
    // Coil's async image-preview loader never composes under Robolectric — the create-mode
    // header only renders it when isRenameMode is false AND photoPaths is non-empty.

    @Test
    fun `rename mode, name-only dirty enables Rename Album`() {
        composeTestRule.setContent {
            AlbumTitleConfirmSheet(
                photoPaths = emptyList(),
                defaultTitle = "My Album",
                isRenameMode = true,
                tagsDirty = false,
                onSave = {},
                onDismiss = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("My Album").performTextReplacement("My Album Renamed")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Rename Album").assertIsEnabled()
    }

    @Test
    fun `rename mode, neither dirty does NOT enable Rename Album`() {
        composeTestRule.setContent {
            AlbumTitleConfirmSheet(
                photoPaths = emptyList(),
                defaultTitle = "My Album",
                isRenameMode = true,
                tagsDirty = false,
                onSave = {},
                onDismiss = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Rename Album").assertIsNotEnabled()
    }

    @Test
    fun `create mode with false tags signal keeps Save Album enabled`() {
        composeTestRule.setContent {
            AlbumTitleConfirmSheet(
                photoPaths = emptyList(),
                defaultTitle = "My Album",
                isRenameMode = false,
                tagsDirty = false,
                onSave = {},
                onDismiss = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Save Album").assertIsEnabled()
    }

    @Test
    fun `create mode with true tags signal keeps Save Album enabled — unconditional, new param cannot leak in`() {
        composeTestRule.setContent {
            AlbumTitleConfirmSheet(
                photoPaths = emptyList(),
                defaultTitle = "My Album",
                isRenameMode = false,
                tagsDirty = true,
                onSave = {},
                onDismiss = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Save Album").assertIsEnabled()
    }
}
