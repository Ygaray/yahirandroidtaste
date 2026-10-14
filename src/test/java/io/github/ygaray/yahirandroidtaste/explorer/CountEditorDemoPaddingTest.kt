package io.github.ygaray.yahirandroidtaste.explorer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * v2.7.1 (Yahir, v2.6 Gate-2 item 12): the count-editor demo keeps the gallery's 16dp horizontal
 * inset like the other release demos, instead of sitting flush on the screen edges.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CountEditorDemoPaddingTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `the count editor content is inset 16dp from both edges`() {
        composeTestRule.setContent {
            Box(Modifier.fillMaxWidth().testTag(HOST)) { CountEditorDemo() }
        }
        composeTestRule.waitForIdle()
        val host = composeTestRule.onNodeWithTag(HOST).getUnclippedBoundsInRoot()
        val label = composeTestRule.onNodeWithText("Count").getUnclippedBoundsInRoot()
        val card = composeTestRule.onNodeWithTag("tone_card", useUnmergedTree = true).getUnclippedBoundsInRoot()

        assertEquals(16f, (label.left - host.left).value, 0.5f)
        assertEquals(16f, (card.left - host.left).value, 0.5f)
        assertEquals(16f, (host.right - card.right).value, 0.5f)
    }

    private companion object {
        const val HOST = "count_editor_host"
    }
}
