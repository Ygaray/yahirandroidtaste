package io.github.ygaray.yahirandroidtaste.component

import org.junit.Test

/**
 * Source-structural-contract tests proving REMIND-09's `reminderCount` parameter is wired into
 * all four built card composables — [TextCard], [ListCard], [VoiceCard], [AlbumCard].
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
 * with no card/sheet around it.
 */
class ReminderIndicatorCardWiringTest {

    private fun source(file: String): String = SourceContractTestSupport.source(file)

    private fun countOccurrences(haystack: String, needle: String): Int =
        SourceContractTestSupport.countOccurrences(haystack, needle)

    // --- TextCard.kt ---

    @Test
    fun `TextCard declares a reminderCount param defaulting to zero`() {
        val src = source("TextCard.kt")
        assertEqualsOnce(src, "reminderCount: Int = 0,")
    }

    @Test
    fun `TextCard's reminderCount cluster is gated on a positive count and calls ReminderIndicator`() {
        val src = source("TextCard.kt")
        assertEqualsOnce(src, "if (reminderCount > 0) {")
        assertEqualsOnce(src, "ReminderIndicator(reminderCount = reminderCount)")
    }

    // --- ListCard.kt ---

    @Test
    fun `ListCard declares a reminderCount param and forwards it to ListCardHeaderContent`() {
        val src = source("ListCard.kt")
        assertEqualsOnce(src, "reminderCount: Int = 0")
        // Appears twice: once forwarding ListCard's param into ListCardHeaderContent's call,
        // once inside ListCardHeaderContent's own ReminderIndicator(reminderCount = reminderCount) call.
        org.junit.Assert.assertEquals(
            "Expected exactly two occurrences of 'reminderCount = reminderCount' " +
                "(the ListCardHeaderContent forward + the ReminderIndicator call)",
            2,
            countOccurrences(src, "reminderCount = reminderCount")
        )
    }

    @Test
    fun `ListCard's reminderCount cluster is gated on a positive count and calls ReminderIndicator`() {
        val src = source("ListCard.kt")
        assertEqualsOnce(src, "if (reminderCount > 0) {")
        assertEqualsOnce(src, "ReminderIndicator(reminderCount = reminderCount)")
    }

    // --- VoiceCard.kt ---

    @Test
    fun `VoiceCard declares a reminderCount param defaulting to zero`() {
        val src = source("VoiceCard.kt")
        assertEqualsOnce(src, "reminderCount: Int = 0")
    }

    @Test
    fun `VoiceCard's reminderCount cluster is gated on a positive count and calls ReminderIndicator`() {
        val src = source("VoiceCard.kt")
        assertEqualsOnce(src, "if (reminderCount > 0) {")
        assertEqualsOnce(src, "ReminderIndicator(reminderCount = reminderCount)")
    }

    // --- AlbumCard.kt ---

    @Test
    fun `AlbumCard declares a reminderCount param defaulting to zero`() {
        val src = source("AlbumCard.kt")
        assertEqualsOnce(src, "reminderCount: Int = 0")
    }

    @Test
    fun `AlbumCard's reminderCount cluster is gated on a positive count and calls ReminderIndicator`() {
        val src = source("AlbumCard.kt")
        assertEqualsOnce(src, "if (reminderCount > 0) {")
        assertEqualsOnce(src, "ReminderIndicator(reminderCount = reminderCount)")
    }

    // --- Neutral-tint contract (UI-SPEC: never accent-tinted) ---

    @Test
    fun `ReminderIndicator never references the primary accent color role`() {
        val src = source("ReminderIndicator.kt")
        assertEquals0(src, "colorScheme.primary")
    }

    private fun assertEqualsOnce(haystack: String, needle: String) {
        org.junit.Assert.assertEquals(
            "Expected exactly one occurrence of '$needle'",
            1,
            countOccurrences(haystack, needle)
        )
    }

    private fun assertEquals0(haystack: String, needle: String) {
        org.junit.Assert.assertEquals(
            "Expected zero occurrences of '$needle'",
            0,
            countOccurrences(haystack, needle)
        )
    }
}
