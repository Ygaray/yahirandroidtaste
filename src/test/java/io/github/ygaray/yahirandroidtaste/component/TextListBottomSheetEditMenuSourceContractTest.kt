package io.github.ygaray.yahirandroidtaste.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

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
 * `TextCardBottomSheet.kt` coverage lands in this task (Task 1 of 114-01-PLAN.md);
 * `ListCardBottomSheet.kt` coverage is added by Task 2, and the cross-file backward-compatibility
 * guard by Task 3.
 */
class TextListBottomSheetEditMenuSourceContractTest {

    private fun source(file: String): String =
        File("src/main/java/io/github/ygaray/yahirandroidtaste/component/$file").readText()

    private fun countOccurrences(haystack: String, needle: String): Int =
        haystack.split(needle).size - 1

    /**
     * Strips comment noise from a source excerpt so label-literal counts are not polluted by
     * comment prose. Strips three things:
     *  1. whole lines whose first non-whitespace characters are `//`;
     *  2. whole lines whose first non-whitespace character is `*` (KDoc/block-comment
     *     continuation lines);
     *  3. trailing inline `//` comments — the tail of a line from a `//` that appears *after*
     *     code on the same line, cut only when that `//` sits outside a double-quoted string
     *     (scanned left to right, honouring backslash escapes) so a legitimate `//` inside a
     *     string literal (a URL, a path) is never truncated.
     *
     * Documented blind spots — deliberately NOT handled, extend this helper if a future
     * assertion needs either: block `/* ... */` comments that open and close on a code line, and
     * raw triple-quoted strings (a `//` inside one may be mis-treated as a comment start).
     */
    private fun stripComments(src: String): String =
        src.lineSequence()
            .filterNot { line ->
                val trimmed = line.trimStart()
                trimmed.startsWith("//") || trimmed.startsWith("*")
            }
            .map { line -> stripTrailingInlineComment(line) }
            .joinToString("\n")

    private fun stripTrailingInlineComment(line: String): String {
        var inString = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '\\' && inString -> i++ // skip escaped char inside string
                c == '"' -> inString = !inString
                c == '/' && !inString && i + 1 < line.length && line[i + 1] == '/' ->
                    return line.substring(0, i)
            }
            i++
        }
        return line
    }

    /**
     * Isolates the substring between the `// region:edit-menu-item` /
     * `// endregion:edit-menu-item` marker comments added in this plan. Located on the RAW
     * source first (the markers are themselves comments), before [stripComments] is applied by
     * the caller — that ordering matters, since stripping first would destroy the anchors.
     */
    private fun editMenuItemRegion(src: String): String {
        val start = src.indexOf("// region:edit-menu-item")
        val end = src.indexOf("// endregion:edit-menu-item")
        require(start >= 0 && end > start) {
            "114-01: could not locate the // region:edit-menu-item / // endregion:edit-menu-item " +
                "markers — they are load-bearing anchors for " +
                "TextListBottomSheetEditMenuSourceContractTest, not decorative comments. Restore " +
                "them around the first DropdownMenuItem."
        }
        return src.substring(start, end)
    }

    // --- TextCardBottomSheet (Task 1) ---

    @Test
    fun `TextCardBottomSheet declares a trailing nullable defaulted onEditRequest param`() {
        val src = source("TextCardBottomSheet.kt")
        assertTrue(
            "TextCardBottomSheet must declare a trailing, nullable, defaulted onEditRequest param",
            src.contains("onEditRequest: (() -> Unit)? = null")
        )
    }

    @Test
    fun `TextCardBottomSheet Edit row branches non-null onEditRequest, null local dialog, not inverted`() {
        val src = source("TextCardBottomSheet.kt")
        assertTrue(
            "TextCardBottomSheet's Edit row must branch on `if (onEditRequest != null)` " +
                "(not inverted)",
            src.contains("if (onEditRequest != null)")
        )
        assertTrue(
            "TextCardBottomSheet must retain its showRenameDialog fallback",
            src.contains("showRenameDialog = true")
        )
        assertTrue(
            "TextCardBottomSheet must retain its local rename AlertDialog",
            src.contains("AlertDialog(")
        )
    }

    @Test
    fun `TextCardBottomSheet menu row reads Edit exactly once and Rename zero times`() {
        val src = source("TextCardBottomSheet.kt")
        val region = stripComments(editMenuItemRegion(src))
        assertEquals(
            "TextCardBottomSheet menu-item region must have exactly one Text(\"Edit\") row",
            1,
            countOccurrences(region, "Text(\"Edit\")")
        )
        assertEquals(
            "TextCardBottomSheet menu-item region must have no Text(\"Rename\") row",
            0,
            countOccurrences(region, "Text(\"Rename\")")
        )
    }

    @Test
    fun `TextCardBottomSheet retains showRenameDialog and onConfirmRename local dialog fallback`() {
        val src = source("TextCardBottomSheet.kt")
        assertTrue(
            "TextCardBottomSheet must retain showRenameDialog state (null-hook fallback)",
            src.contains("showRenameDialog")
        )
        assertTrue(
            "TextCardBottomSheet must retain its local rename AlertDialog block",
            src.contains("AlertDialog(")
        )
        assertTrue(
            "TextCardBottomSheet must still invoke onConfirmRename(",
            src.contains("onConfirmRename(")
        )
    }

    // --- ListCardBottomSheet (Task 2) ---

    @Test
    fun `ListCardBottomSheet declares a trailing nullable defaulted onEditRequest param`() {
        val src = source("ListCardBottomSheet.kt")
        assertTrue(
            "ListCardBottomSheet must declare a trailing, nullable, defaulted onEditRequest param",
            src.contains("onEditRequest: (() -> Unit)? = null")
        )
    }

    @Test
    fun `ListCardBottomSheet Edit row branches non-null onEditRequest, null local dialog, not inverted`() {
        val src = source("ListCardBottomSheet.kt")
        assertTrue(
            "ListCardBottomSheet's Edit row must branch on `if (onEditRequest != null)` " +
                "(not inverted)",
            src.contains("if (onEditRequest != null)")
        )
        assertTrue(
            "ListCardBottomSheet must retain its showRenameDialog fallback",
            src.contains("showRenameDialog = true")
        )
        assertTrue(
            "ListCardBottomSheet must retain its local rename AlertDialog",
            src.contains("AlertDialog(")
        )
    }

    @Test
    fun `ListCardBottomSheet menu row reads Edit exactly once and Rename zero times`() {
        val src = source("ListCardBottomSheet.kt")
        val region = stripComments(editMenuItemRegion(src))
        assertEquals(
            "ListCardBottomSheet menu-item region must have exactly one Text(\"Edit\") row",
            1,
            countOccurrences(region, "Text(\"Edit\")")
        )
        assertEquals(
            "ListCardBottomSheet menu-item region must have no Text(\"Rename\") row",
            0,
            countOccurrences(region, "Text(\"Rename\")")
        )
    }

    @Test
    fun `ListCardBottomSheet retains showRenameDialog local dialog fallback and unchanged Edit list button`() {
        val src = source("ListCardBottomSheet.kt")
        assertTrue(
            "ListCardBottomSheet must retain showRenameDialog state (null-hook fallback)",
            src.contains("showRenameDialog")
        )
        assertTrue(
            "ListCardBottomSheet must retain its local rename AlertDialog block",
            src.contains("AlertDialog(")
        )
        assertTrue(
            "ListCardBottomSheet's bottom content-editor Button copy must be unchanged",
            src.contains("Text(\"Edit list\")")
        )
    }
}
