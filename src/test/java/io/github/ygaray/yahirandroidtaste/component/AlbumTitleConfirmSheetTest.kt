package io.github.ygaray.yahirandroidtaste.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

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
}
