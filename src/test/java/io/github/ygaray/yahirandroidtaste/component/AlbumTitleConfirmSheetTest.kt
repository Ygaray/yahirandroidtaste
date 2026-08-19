package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
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

    @Test
    fun `isEditDirty delegates to isRenameDirty when tags not dirty`() {
        assertEquals(true, isEditDirty("Bar", "Foo", tagsDirty = false))
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
}
