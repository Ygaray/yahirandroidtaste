package io.github.ygaray.yahirandroidtaste.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Source-structural-contract tests proving CARD-01/D-07/RD-01's `statusContent` wiring is
 * identical across all four built card composables — [TextCard], [ListCard], [VoiceCard],
 * [AlbumCard] — and that NONE of them renders [ReminderIndicator] from its header anymore
 * (164-04 relocated REMIND-09's original Phase 155 header wiring this suite used to lock).
 *
 * Full `CardBase`-based card composables are **unrenderable under this module's Robolectric
 * harness**: `CardBase`'s unconditional `SwipeableActionRow` throws
 * `IllegalStateException: The offset was read before being initialized` on the very first frame,
 * unconditionally, inside a pre-existing file this plan does not touch — the identical,
 * already-documented blocker as `TextCardImageIndicatorTest` (Phase 107) and
 * `VoiceCardClipListTest` (Phase 129). Following the established repo remedy exactly, this suite
 * proves the wiring via direct source-text assertions against the real committed card files
 * instead of composing them — mirroring [VoiceCardClipListTest]'s active source-structural
 * assertions. [ReminderIndicatorTest] independently proves the composable's own render contract
 * with no card/sheet around it, and [CardStatusRowTest] independently proves [CardStatusRow]'s
 * own render contract (empty/inset/spacing/order/fontScale) and [CardBase]'s two-overload wiring.
 *
 * ListCard is the one face whose header content is rendered by a separate private function
 * (`RowScope.ListCardHeaderContent`) rather than inline in the `headerContent` lambda, so it gets
 * two extra checks: that the header FORWARD of `reminderCount` into that function is gone, and
 * that the function's own extracted body (via [SourceContractTestSupport.functionBody]'s
 * brace-balanced extraction, not a to-end-of-file scan — 164-04 review round 2 LOW) no longer
 * calls [ReminderIndicator].
 */
class ReminderIndicatorCardWiringTest {

    private fun source(file: String): String = SourceContractTestSupport.source(file)

    private fun strippedSource(file: String): String =
        SourceContractTestSupport.stripComments(source(file))

    private fun countOccurrences(haystack: String, needle: String): Int =
        SourceContractTestSupport.countOccurrences(haystack, needle)

    // --- TextCard.kt ---

    @Test
    fun `TextCard declares a reminderCount param defaulting to zero`() {
        assertEqualsOnce(strippedSource("TextCard.kt"), "reminderCount: Int = 0,")
    }

    @Test
    fun `TextCard's old header-cluster gate no longer exists anywhere in the file`() {
        assertEqualsOnce(strippedSource("TextCard.kt"), "if (reminderCount > 0) {")
    }

    @Test
    fun `TextCard's statusContent gate occurs exactly once`() {
        assertEqualsOnce(strippedSource("TextCard.kt"), "statusContent = if (reminderCount > 0) {")
    }

    @Test
    fun `TextCard's statusContent gate calls ReminderIndicator exactly once`() {
        val snippet = statusContentSnippet(strippedSource("TextCard.kt"), "TextCard.kt")
        assertEqualsOnce(snippet, "ReminderIndicator(reminderCount = reminderCount)")
    }

    @Test
    fun `TextCard's header region contains no ReminderIndicator call`() {
        val headerRegion = region(
            strippedSource("TextCard.kt"),
            "headerContent = if (",
            "bodyContent = if",
            "TextCard.kt"
        )
        assertEquals0(headerRegion, "ReminderIndicator(")
    }

    // --- ListCard.kt ---

    @Test
    fun `ListCard declares a reminderCount param defaulting to zero`() {
        assertEqualsOnce(strippedSource("ListCard.kt"), "reminderCount: Int = 0")
    }

    @Test
    fun `ListCard's old header-cluster gate no longer exists anywhere in the file`() {
        assertEqualsOnce(strippedSource("ListCard.kt"), "if (reminderCount > 0) {")
    }

    @Test
    fun `ListCard's statusContent gate occurs exactly once`() {
        assertEqualsOnce(strippedSource("ListCard.kt"), "statusContent = if (reminderCount > 0) {")
    }

    @Test
    fun `ListCard's statusContent gate calls ReminderIndicator exactly once`() {
        val snippet = statusContentSnippet(strippedSource("ListCard.kt"), "ListCard.kt")
        assertEqualsOnce(snippet, "ReminderIndicator(reminderCount = reminderCount)")
    }

    @Test
    fun `ListCard's inline header region contains no ReminderIndicator call`() {
        val headerRegion = region(
            strippedSource("ListCard.kt"),
            "headerContent = if",
            "bodyContent = {",
            "ListCard.kt"
        )
        assertEquals0(headerRegion, "ReminderIndicator(")
    }

    @Test
    fun `ListCard no longer forwards reminderCount into ListCardHeaderContent`() {
        // Only the ReminderIndicator(reminderCount = reminderCount) call inside statusContent
        // remains — the header's own forward argument is gone (CARD-01/D-07). Where the
        // pre-164-04 wiring test asserted exactly TWO occurrences (the forward plus the call),
        // this now asserts exactly ONE.
        assertEqualsOnce(strippedSource("ListCard.kt"), "reminderCount = reminderCount")
    }

    @Test
    fun `ListCardHeaderContent's own extracted body contains no ReminderIndicator call`() {
        val headerContentBody = SourceContractTestSupport.functionBody(
            strippedSource("ListCard.kt"),
            "fun RowScope.ListCardHeaderContent("
        )
        assertEquals0(headerContentBody, "ReminderIndicator(")
    }

    // --- VoiceCard.kt ---

    @Test
    fun `VoiceCard declares a reminderCount param defaulting to zero`() {
        assertEqualsOnce(strippedSource("VoiceCard.kt"), "reminderCount: Int = 0")
    }

    @Test
    fun `VoiceCard's old header-cluster gate no longer exists anywhere in the file`() {
        assertEqualsOnce(strippedSource("VoiceCard.kt"), "if (reminderCount > 0) {")
    }

    @Test
    fun `VoiceCard's statusContent gate occurs exactly once`() {
        assertEqualsOnce(strippedSource("VoiceCard.kt"), "statusContent = if (reminderCount > 0) {")
    }

    @Test
    fun `VoiceCard's statusContent gate calls ReminderIndicator exactly once`() {
        val snippet = statusContentSnippet(strippedSource("VoiceCard.kt"), "VoiceCard.kt")
        assertEqualsOnce(snippet, "ReminderIndicator(reminderCount = reminderCount)")
    }

    @Test
    fun `VoiceCard's header region contains no ReminderIndicator call`() {
        val headerRegion = region(
            strippedSource("VoiceCard.kt"),
            "headerContent = if",
            "bodyContent = {",
            "VoiceCard.kt"
        )
        assertEquals0(headerRegion, "ReminderIndicator(")
    }

    // --- AlbumCard.kt ---

    @Test
    fun `AlbumCard declares a reminderCount param defaulting to zero`() {
        assertEqualsOnce(strippedSource("AlbumCard.kt"), "reminderCount: Int = 0")
    }

    @Test
    fun `AlbumCard's old header-cluster gate no longer exists anywhere in the file`() {
        assertEqualsOnce(strippedSource("AlbumCard.kt"), "if (reminderCount > 0) {")
    }

    @Test
    fun `AlbumCard's statusContent gate occurs exactly once`() {
        assertEqualsOnce(strippedSource("AlbumCard.kt"), "statusContent = if (reminderCount > 0) {")
    }

    @Test
    fun `AlbumCard's statusContent gate calls ReminderIndicator exactly once`() {
        val snippet = statusContentSnippet(strippedSource("AlbumCard.kt"), "AlbumCard.kt")
        assertEqualsOnce(snippet, "ReminderIndicator(reminderCount = reminderCount)")
    }

    @Test
    fun `AlbumCard's header region contains no ReminderIndicator call`() {
        val headerRegion = region(
            strippedSource("AlbumCard.kt"),
            "headerContent = if",
            "bodyContent = {",
            "AlbumCard.kt"
        )
        assertEquals0(headerRegion, "ReminderIndicator(")
    }

    // --- Cross-face consistency ---

    @Test
    fun `all four faces' statusContent gate-plus-call snippets are identical after whitespace normalization`() {
        val faces = listOf("TextCard.kt", "ListCard.kt", "VoiceCard.kt", "AlbumCard.kt")
        val normalizedSnippets = faces.associateWith { file ->
            normalizeWhitespace(statusContentSnippet(strippedSource(file), file))
        }
        assertTrue(
            "expected the identical statusContent gate-plus-call shape on every face " +
                "(CARD-01/D-07 cross-face consistency); got: $normalizedSnippets",
            normalizedSnippets.values.toSet().size == 1
        )
    }

    // --- Neutral-tint contract (UI-SPEC: never accent-tinted) ---

    @Test
    fun `ReminderIndicator never references the primary accent color role`() {
        assertEquals0(source("ReminderIndicator.kt"), "colorScheme.primary")
    }

    // --- Shared helpers -------------------------------------------------------------------

    /**
     * Extracts the text from the `statusContent = if (reminderCount > 0) {` gate through its
     * matching `} else null` branch (inclusive) in [src], failing loudly (never vacuously) if
     * either marker is missing.
     */
    private fun statusContentSnippet(src: String, file: String): String {
        val gateMarker = "statusContent = if (reminderCount > 0) {"
        val gateStart = src.indexOf(gateMarker)
        assertTrue("Could not locate the statusContent gate in $file", gateStart >= 0)
        val elseMarker = "} else null"
        val gateEnd = src.indexOf(elseMarker, gateStart)
        assertTrue(
            "Could not locate the statusContent gate's '$elseMarker' branch in $file",
            gateEnd >= 0
        )
        return src.substring(gateStart, gateEnd + elseMarker.length)
    }

    private fun normalizeWhitespace(text: String): String =
        text.replace(Regex("\\s+"), " ").trim()

    private fun region(src: String, startMarker: String, endMarker: String, file: String): String {
        val start = src.indexOf(startMarker)
        val end = src.indexOf(endMarker, if (start >= 0) start else 0)
        assertTrue(
            "Could not locate the header region ('$startMarker' .. '$endMarker') in $file",
            start in 0 until end
        )
        return src.substring(start, end)
    }

    private fun assertEqualsOnce(haystack: String, needle: String) {
        assertEquals(
            "Expected exactly one occurrence of '$needle'",
            1,
            countOccurrences(haystack, needle)
        )
    }

    private fun assertEquals0(haystack: String, needle: String) {
        assertEquals(
            "Expected zero occurrences of '$needle'",
            0,
            countOccurrences(haystack, needle)
        )
    }
}
