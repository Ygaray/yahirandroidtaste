package com.example.secondbrain.yahirandroidtaste.feedback

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Compose UI test for [UndoCenterScreen] — locks the gesture-composition contract (RESEARCH.md
 * Assumption A1, Phase 58 Plan 06, UNDO-04):
 *  - Press-and-hold on a row shows the read-only [UndoPreviewPeek] overlay while held.
 *  - Releasing hides the peek and does NOT invoke [onUndo].
 *  - Tapping the "Undo" `TextButton` invokes [onUndo] exactly once and does NOT show the peek —
 *    the nested clickable consumes the pointer-down during the Main pass before the row's
 *    ancestor `pointerInput` sees it as unconsumed.
 *  - A row whose `entry.preview` is null still responds to hold with a message-only peek,
 *    without crashing (D-01).
 *
 * `testTag`s ([UNDO_ROW_TEST_TAG_PREFIX], [UNDO_PEEK_TEST_TAG]) are `internal` in
 * `UndoCenterScreen.kt` — this test lives in the same module/package, so no plumbing is needed.
 *
 * Infra mirrors `:app`'s `TagCreateSheetTest`/`BulkCreatePopupContentTest` convention
 * (isIncludeAndroidResources = true in `designsystem/build.gradle.kts` enables
 * `createComposeRule()`; `sdk = [35]` avoids a Robolectric `PackageParserException`, minSdk = 35)
 * — newly wired directly into `:designsystem` for this plan (see build.gradle.kts changes).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class UndoCenterScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun holdingARow_showsThePeekOverlay() {
        val store = UndoHistoryStore()
        val id = store.append("Tag deleted", preview = UndoPreview.Tag("Work")) { }

        composeTestRule.setContent {
            UndoCenterScreen(
                entries = store.entries.value,
                onNavigateBack = {},
                onUndo = {},
                onClear = {}
            )
        }
        composeTestRule.waitForIdle()

        val row = composeTestRule.onNodeWithTag("$UNDO_ROW_TEST_TAG_PREFIX$id")
        row.performTouchInput { down(Offset(10f, center.y)) }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(UNDO_PEEK_TEST_TAG).assertExists()

        // Release so the held gesture doesn't leak into the next test.
        row.performTouchInput { up() }
        composeTestRule.waitForIdle()
    }

    @Test
    fun releasingAHeldRow_hidesThePeek_andDoesNotInvokeOnUndo() {
        val store = UndoHistoryStore()
        var undoInvoked = false
        val id = store.append("Tag deleted", preview = UndoPreview.Tag("Work")) { }

        composeTestRule.setContent {
            UndoCenterScreen(
                entries = store.entries.value,
                onNavigateBack = {},
                onUndo = { undoInvoked = true },
                onClear = {}
            )
        }
        composeTestRule.waitForIdle()

        val row = composeTestRule.onNodeWithTag("$UNDO_ROW_TEST_TAG_PREFIX$id")
        row.performTouchInput { down(Offset(10f, center.y)) }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(UNDO_PEEK_TEST_TAG).assertExists()

        row.performTouchInput { up() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(UNDO_PEEK_TEST_TAG).assertDoesNotExist()
        assertFalse("Releasing the hold must NOT invoke onUndo", undoInvoked)
    }

    @Test
    fun tappingUndo_invokesOnUndoExactlyOnce_andDoesNotShowThePeek() {
        val store = UndoHistoryStore()
        var undoCallCount = 0
        store.append("Tag deleted", preview = UndoPreview.Tag("Work")) { }

        composeTestRule.setContent {
            UndoCenterScreen(
                entries = store.entries.value,
                onNavigateBack = {},
                onUndo = { undoCallCount++ },
                onClear = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Undo").performClick()
        composeTestRule.waitForIdle()

        assertEquals(
            "Tapping Undo must invoke onUndo exactly once (A1 — no double-fire via the peek gesture)",
            1,
            undoCallCount
        )
        composeTestRule.onNodeWithTag(UNDO_PEEK_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun aNullPreviewRow_respondsToHold_withAMessageOnlyPeek_withoutCrashing() {
        val store = UndoHistoryStore()
        val id = store.append("Card moved") { } // preview omitted -> null

        composeTestRule.setContent {
            UndoCenterScreen(
                entries = store.entries.value,
                onNavigateBack = {},
                onUndo = {},
                onClear = {}
            )
        }
        composeTestRule.waitForIdle()

        val row = composeTestRule.onNodeWithTag("$UNDO_ROW_TEST_TAG_PREFIX$id")
        row.performTouchInput { down(Offset(10f, center.y)) }
        composeTestRule.waitForIdle()

        // Message-only peek: UNDO_PEEK_MESSAGE_TEST_TAG is only emitted by the null-preview
        // branch of UndoPreviewPeek — its presence proves the graceful fallback rendered
        // (D-01), and its text is the entry's own message (no crash, no repository lookup).
        composeTestRule.onNodeWithTag(UNDO_PEEK_TEST_TAG).assertExists()
        composeTestRule.onNodeWithTag(UNDO_PEEK_MESSAGE_TEST_TAG).assertTextEquals("Card moved")

        row.performTouchInput { up() }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(UNDO_PEEK_TEST_TAG).assertDoesNotExist()
    }
}
