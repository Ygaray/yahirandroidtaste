package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * FOPEN-03: through the second [UndoCenterScreen] overload (required `onOpenTarget`) an Available
 * entry that carries a target opens its item with one tap (D-04 [undo-center-tap]).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class UndoCenterScreenOpenTargetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun availableEntryWithTarget_tapOpensOnce() {
        val store = UndoHistoryStore()
        val target = FeedbackTarget("item-42", "Open item")
        val opened = mutableListOf<FeedbackTarget>()
        var undoCount = 0
        val id = store.append("Tag deleted", target = target) { }

        composeTestRule.setContent {
            val entries by store.entries.collectAsState()
            UndoCenterScreen(
                entries = entries,
                onNavigateBack = {},
                onUndo = { undoCount++ },
                onOpenTarget = { opened += it },
                onClear = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("$UNDO_ROW_TEST_TAG_PREFIX$id")
            .performTouchInput { click(Offset(10f, center.y)) }
        composeTestRule.waitForIdle()

        assertEquals(listOf(target), opened)
        assertEquals(0, undoCount)
        composeTestRule.onNodeWithTag(UNDO_PEEK_TEST_TAG).assertDoesNotExist()
    }
}
