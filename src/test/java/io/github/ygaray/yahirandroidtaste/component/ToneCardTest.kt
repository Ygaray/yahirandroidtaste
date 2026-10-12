package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import io.github.ygaray.yahirandroidtaste.theme.LightColorScheme
import io.github.ygaray.yahirandroidtaste.theme.ToneDefaults
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Compose tests for [ToneCard]. Color wiring is proven structurally (LocalContentColor), since the
 * repo has no pixel-assertion harness.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ToneCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `tracer - error tone content color flows to children and default tag exists`() {
        var captured = Color.Unspecified
        composeTestRule.setContent {
            MaterialTheme(colorScheme = LightColorScheme) {
                ToneCard(tone = ToneDefaults.error()) {
                    captured = LocalContentColor.current
                }
            }
        }
        composeTestRule.waitForIdle()

        assertEquals(LightColorScheme.error, captured)
        composeTestRule.onNodeWithTag("tone_card").assertExists()
    }
}
