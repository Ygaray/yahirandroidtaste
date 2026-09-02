package io.github.ygaray.yahirandroidtaste.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Proves EDIT-04's bottom-sheet half in the hub: `TextCardBottomSheet` and `ListCardBottomSheet`
 * gain a trailing, nullable `onEditRequest` trigger, their three-dot menu row is relabelled
 * "Rename" -> "Edit", and that row routes to `onEditRequest()` + `onDismiss()` when wired (the
 * host-owned shared tag-inclusive Edit sheet, mirroring the already-shipped card-face
 * [TextCard]/[ListCard] pattern) or falls back to the local tag-less rename dialog when null
 * (backward-compat).
 *
 * ## Why this is a source-structural contract, not a rendered interaction test
 * Both composables wrap [SheetScaffold]'s live `ModalBottomSheet`, which this module's
 * Robolectric harness cannot drive — the hub already recorded this exact blocker in
 * [VoiceRenameTagsSheetGateTest]'s KDoc, and the card-face equivalent ([TextListEditMenuTest])
 * made the same call for the same reason. This class therefore needs no
 * `@RunWith(RobolectricTestRunner::class)` and no compose rule; every assertion below is a plain
 * file read. It proves the parameter exists with the right shape, that the branch is not
 * inverted, that the label copy is exactly right, and that the null-hook fallback survives — it
 * does **not** prove that tapping the rendered `DropdownMenuItem` actually invokes
 * `onEditRequest()` and then `onDismiss()` at runtime. That rendered proof is an outstanding
 * obligation discharged at Phase 115's SecondBrain Gate-1, on-device, across the repin boundary.
 *
 * ## File boundary (post-WO-2, 05-02-PLAN.md)
 * `TextCardBottomSheet.kt` and `ListCardBottomSheet.kt` originally carried this coverage's
 * assertions directly (`114-01-PLAN.md` Task 1/Task 2, cross-file guard Task 3). WO-2 extracted
 * the shared header `Row` + three-dot `DropdownMenu` + rename `AlertDialog` triad — everything
 * this class asserts on, other than each sheet's own trailing `onEditRequest` param declaration
 * and `ListCardBottomSheet`'s unrelated "Edit list" button copy — into a new shared composable,
 * `SheetHeaderMenu.kt`. This class is retargeted accordingly: the moved structural assertions
 * (Edit-row branching, region-marker Edit/Rename copy count, rename-dialog structural presence,
 * `onConfirmRename(` invocation) now check `source("SheetHeaderMenu.kt")` **once**, since both
 * sheets share one implementation, rather than duplicating the same check per host file. Each
 * host sheet's own `onEditRequest` param declaration and its wiring into the shared composable's
 * call site are still checked per-file, proving the extraction did not silently drop either
 * sheet's forwarding.
 */
class TextListBottomSheetEditMenuSourceContractTest {

    private fun source(file: String): String = SourceContractTestSupport.source(file)

    private fun countOccurrences(haystack: String, needle: String): Int =
        SourceContractTestSupport.countOccurrences(haystack, needle)

    private fun stripComments(src: String): String = SourceContractTestSupport.stripComments(src)

    /**
     * Isolates the substring between the `// region:edit-menu-item` /
     * `// endregion:edit-menu-item` marker comments — moved into `SheetHeaderMenu.kt` verbatim by
     * WO-2's extraction. Located on the RAW source first (the markers are themselves comments),
     * before [stripComments] is applied by the caller — that ordering matters, since stripping
     * first would destroy the anchors.
     */
    private fun editMenuItemRegion(src: String): String {
        val start = src.indexOf("// region:edit-menu-item")
        val end = src.indexOf("// endregion:edit-menu-item")
        require(start >= 0 && end > start) {
            "114-01/05-02: could not locate the // region:edit-menu-item / " +
                "// endregion:edit-menu-item markers — they are load-bearing anchors for " +
                "TextListBottomSheetEditMenuSourceContractTest, not decorative comments. Restore " +
                "them around the first DropdownMenuItem in SheetHeaderMenu.kt."
        }
        return src.substring(start, end)
    }

    // --- Each host sheet's own onEditRequest param declaration (unmoved by WO-2) ---

    @Test
    fun `TextCardBottomSheet declares a trailing nullable defaulted onEditRequest param`() {
        val src = source("TextCardBottomSheet.kt")
        assertTrue(
            "TextCardBottomSheet must declare a trailing, nullable, defaulted onEditRequest param",
            src.contains("onEditRequest: (() -> Unit)? = null")
        )
    }

    @Test
    fun `ListCardBottomSheet declares a trailing nullable defaulted onEditRequest param`() {
        val src = source("ListCardBottomSheet.kt")
        assertTrue(
            "ListCardBottomSheet must declare a trailing, nullable, defaulted onEditRequest param",
            src.contains("onEditRequest: (() -> Unit)? = null")
        )
    }

    // --- Each host sheet forwards onEditRequest into the shared SheetHeaderMenu call ---

    @Test
    fun `both bottom sheets wire onEditRequest into their SheetHeaderMenu call site`() {
        for (file in listOf("TextCardBottomSheet.kt", "ListCardBottomSheet.kt")) {
            val src = source(file)
            assertTrue(
                "$file must call the shared SheetHeaderMenu composable",
                src.contains("SheetHeaderMenu(")
            )
            assertTrue(
                "$file must forward its own onEditRequest into SheetHeaderMenu",
                src.contains("onEditRequest = onEditRequest")
            )
        }
    }

    // --- ListCardBottomSheet's own unrelated button copy (unmoved by WO-2) ---

    @Test
    fun `ListCardBottomSheet's Edit list button copy is unchanged`() {
        val src = source("ListCardBottomSheet.kt")
        assertTrue(
            "ListCardBottomSheet's bottom content-editor Button copy must be unchanged",
            src.contains("Text(\"Edit list\")")
        )
    }

    // --- Shared SheetHeaderMenu.kt (WO-2's extraction target — the moved assertions) ---

    @Test
    fun `SheetHeaderMenu Edit row branches non-null onEditRequest, null local dialog, not inverted`() {
        val src = source("SheetHeaderMenu.kt")
        assertTrue(
            "SheetHeaderMenu's Edit row must branch on `if (onEditRequest != null)` (not inverted)",
            src.contains("if (onEditRequest != null)")
        )
        assertTrue(
            "SheetHeaderMenu must retain the showRenameDialog fallback",
            src.contains("showRenameDialog = true")
        )
        assertTrue(
            "SheetHeaderMenu must retain the local rename AlertDialog",
            src.contains("AlertDialog(")
        )
    }

    @Test
    fun `SheetHeaderMenu menu row reads Edit exactly once and Rename zero times`() {
        val src = source("SheetHeaderMenu.kt")
        val region = stripComments(editMenuItemRegion(src))
        assertEquals(
            "SheetHeaderMenu menu-item region must have exactly one Text(\"Edit\") row",
            1,
            countOccurrences(region, "Text(\"Edit\")")
        )
        assertEquals(
            "SheetHeaderMenu menu-item region must have no Text(\"Rename\") row",
            0,
            countOccurrences(region, "Text(\"Rename\")")
        )
    }

    @Test
    fun `SheetHeaderMenu retains showRenameDialog and onConfirmRename local dialog fallback`() {
        val src = source("SheetHeaderMenu.kt")
        assertTrue(
            "SheetHeaderMenu must retain showRenameDialog state (null-hook fallback)",
            src.contains("showRenameDialog")
        )
        assertTrue(
            "SheetHeaderMenu must retain the local rename AlertDialog block",
            src.contains("AlertDialog(")
        )
        assertTrue(
            "SheetHeaderMenu must still invoke onConfirmRename(",
            src.contains("onConfirmRename(")
        )
    }

    // --- Cross-file backward-compatibility guard ---

    @Test
    fun `both bottom sheets retain the full null-hook fallback structurally intact`() {
        val sharedSrc = source("SheetHeaderMenu.kt")
        assertTrue(
            "SheetHeaderMenu.kt must still declare showRenameDialog state — deleting it would " +
                "silently break every consumer that has not yet bound onEditRequest",
            sharedSrc.contains("showRenameDialog")
        )
        assertTrue(
            "SheetHeaderMenu.kt must still contain a local rename AlertDialog( block",
            sharedSrc.contains("AlertDialog(")
        )
        assertTrue(
            "SheetHeaderMenu.kt must still invoke onConfirmRename( from its local rename dialog",
            sharedSrc.contains("onConfirmRename(")
        )
        for (file in listOf("TextCardBottomSheet.kt", "ListCardBottomSheet.kt")) {
            val src = source(file)
            assertTrue(
                "$file must still declare its own onEditRequest param",
                src.contains("onEditRequest: (() -> Unit)? = null")
            )
            assertTrue(
                "$file must still call the shared SheetHeaderMenu composable",
                src.contains("SheetHeaderMenu(")
            )
        }
    }
}
