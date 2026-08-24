package io.github.ygaray.yahirandroidtaste.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Proves LIST-04's read-only, truncated items preview in the hub: `ListCardBottomSheet` gains two
 * trailing defaulted params — `readOnlyPreview` (swaps the live CHECKBOX `Checkbox` for a static
 * check `Icon`, structurally never invoking [onToggleItem]) and `previewOverflowCount` (renders a
 * "+N more" hint when the caller truncated the list; the component never truncates on its own).
 *
 * ## Why this is a source-structural contract, not a rendered interaction test
 * `ListCardBottomSheet` wraps [SheetScaffold]'s live `ModalBottomSheet`, which this module's
 * Robolectric harness cannot drive — the same blocker
 * [TextListBottomSheetEditMenuSourceContractTest] already recorded for EDIT-04. This class
 * therefore needs no `@RunWith(RobolectricTestRunner::class)` and no compose rule; every
 * assertion below is a plain file read against the comment-stripped source, anchored on the
 * `// region:readonly-item-row` / `// region:preview-overflow-hint` marker comments Task 1 added
 * (load-bearing test anchors, not decoration). It proves the branch is parameter-driven, that the
 * read-only side never wires the item-toggle callback, and that the overflow hint's copy and
 * zero-guard are exactly right — it does **not** prove the rendered sheet behaves this way at
 * runtime. That rendered proof is Gate-1's job on-device, across the repin boundary.
 */
class ListCardBottomSheetReadOnlyPreviewSourceContractTest {

    /**
     * Isolates the substring between the `// region:readonly-item-row` /
     * `// endregion:readonly-item-row` marker comments added in Task 1. Located on the RAW source
     * first (the markers are themselves comments), before [SourceContractTestSupport.stripComments]
     * is applied by the caller — that ordering matters, since stripping first would destroy the
     * anchors.
     */
    private fun readOnlyItemRowRegion(src: String): String {
        val start = src.indexOf("// region:readonly-item-row")
        val end = src.indexOf("// endregion:readonly-item-row")
        require(start >= 0 && end > start) {
            "118-01: could not locate the // region:readonly-item-row / " +
                "// endregion:readonly-item-row markers — they are load-bearing anchors for " +
                "ListCardBottomSheetReadOnlyPreviewSourceContractTest, not decorative comments. " +
                "Restore them around the item-row when (subType) block."
        }
        return src.substring(start, end)
    }

    /**
     * Isolates the substring between the `// region:preview-overflow-hint` /
     * `// endregion:preview-overflow-hint` marker comments added in Task 1.
     */
    private fun previewOverflowHintRegion(src: String): String {
        val start = src.indexOf("// region:preview-overflow-hint")
        val end = src.indexOf("// endregion:preview-overflow-hint")
        require(start >= 0 && end > start) {
            "118-01: could not locate the // region:preview-overflow-hint / " +
                "// endregion:preview-overflow-hint markers — they are load-bearing anchors for " +
                "ListCardBottomSheetReadOnlyPreviewSourceContractTest, not decorative comments. " +
                "Restore them around the \"+N more\" hint Text."
        }
        return src.substring(start, end)
    }

    // --- region:readonly-item-row ---

    @Test
    fun `readOnlyPreviewRegion contains both branches`() {
        val src = SourceContractTestSupport.source("ListCardBottomSheet.kt")
        val region = SourceContractTestSupport.stripComments(readOnlyItemRowRegion(src))
        assertTrue(
            "LIST-04: the readonly-item-row region must still contain a Checkbox( construction " +
                "— existing non-preview call sites must keep their live, interactive checkbox",
            region.contains("Checkbox(")
        )
        assertTrue(
            "LIST-04: the readonly-item-row region must contain an Icon( construction — the " +
                "read-only branch was added, not swapped in place of the interactive path",
            region.contains("Icon(")
        )
    }

    @Test
    fun `readOnlyPreviewRegion gates on the readOnlyPreview flag`() {
        val src = SourceContractTestSupport.source("ListCardBottomSheet.kt")
        val region = SourceContractTestSupport.stripComments(readOnlyItemRowRegion(src))
        assertTrue(
            "LIST-04: the readonly-item-row region must reference readOnlyPreview — the branch " +
                "must be parameter-driven, not hard-coded",
            region.contains("readOnlyPreview")
        )
    }

    @Test
    fun `readOnlyBranch does not invoke the item-toggle callback`() {
        val src = SourceContractTestSupport.source("ListCardBottomSheet.kt")
        val region = SourceContractTestSupport.stripComments(readOnlyItemRowRegion(src))
        assertTrue(
            "LIST-04: the region must branch on readOnlyPreview for this callback-absence check " +
                "to be meaningful — without the conditional there is no read-only side to guard " +
                "(this also fails the spot-check when the whole branch is deleted, not just when " +
                "onToggleItem leaks into it)",
            region.contains("readOnlyPreview")
        )
        val checkboxIndex = region.indexOf("Checkbox(")
        require(checkboxIndex >= 0) {
            "118-01: readonly-item-row region must contain a Checkbox( construction to split on"
        }
        val beforeCheckbox = region.substring(0, checkboxIndex)
        assertFalse(
            "LIST-04's load-bearing bar: everything in the region BEFORE the live Checkbox( " +
                "construction (i.e. the readOnlyPreview/static-icon side) must never reference " +
                "onToggleItem — the read-only preview must be structurally incapable of mutating " +
                "list state",
            beforeCheckbox.contains("onToggleItem")
        )
        assertEquals(
            "LIST-04: onToggleItem must be invoked exactly once in the region, wired only into " +
                "the live Checkbox's onCheckedChange — a second invocation would mean it leaked " +
                "into the read-only branch too",
            1,
            SourceContractTestSupport.countOccurrences(region, "onToggleItem")
        )
    }

    @Test
    fun `signature keeps the new params trailing and defaulted`() {
        val src = SourceContractTestSupport.source("ListCardBottomSheet.kt")
        assertTrue(
            "ListCardBottomSheet must declare a trailing, defaulted readOnlyPreview: Boolean param",
            src.contains("readOnlyPreview: Boolean = false")
        )
        assertTrue(
            "ListCardBottomSheet must declare a trailing, defaulted previewOverflowCount: Int param",
            src.contains("previewOverflowCount: Int = 0")
        )
        val onEditRequestIndex = src.indexOf("onEditRequest: (() -> Unit)? = null")
        val readOnlyPreviewIndex = src.indexOf("readOnlyPreview: Boolean = false")
        val previewOverflowCountIndex = src.indexOf("previewOverflowCount: Int = 0")
        require(onEditRequestIndex >= 0) { "onEditRequest param not found in source" }
        assertTrue(
            "readOnlyPreview must appear AFTER onEditRequest in the signature — both new params " +
                "must be trailing so every existing call site keeps compiling on defaults",
            readOnlyPreviewIndex > onEditRequestIndex
        )
        assertTrue(
            "previewOverflowCount must appear AFTER onEditRequest in the signature — both new " +
                "params must be trailing so every existing call site keeps compiling on defaults",
            previewOverflowCountIndex > onEditRequestIndex
        )
    }

    // --- region:preview-overflow-hint ---

    @Test
    fun `overflowHintRegion is gated on a positive count`() {
        val src = SourceContractTestSupport.source("ListCardBottomSheet.kt")
        val region = SourceContractTestSupport.stripComments(previewOverflowHintRegion(src))
        assertTrue(
            "LIST-04: the preview-overflow-hint region must guard on previewOverflowCount > 0 " +
                "— a zero-valued hint must never render",
            region.contains("previewOverflowCount > 0")
        )
    }

    @Test
    fun `overflowHintRegion uses the canonical hint copy`() {
        val src = SourceContractTestSupport.source("ListCardBottomSheet.kt")
        val region = SourceContractTestSupport.stripComments(previewOverflowHintRegion(src))
        assertTrue(
            "LIST-04: the preview-overflow-hint region must use the exact canonical template " +
                "\"+\$previewOverflowCount more\" (leading plus, interpolation, one space, the " +
                "word) — this pins the ONE copy the must_haves truth promises, so a future edit " +
                "that drops the plus sign, the space, or the word fails the build",
            region.contains("\"+\$previewOverflowCount more\"")
        )
    }
}
