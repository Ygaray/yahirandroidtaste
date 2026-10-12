package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.theme.DarkColorScheme
import io.github.ygaray.yahirandroidtaste.theme.LightColorScheme
import io.github.ygaray.yahirandroidtaste.theme.ToneColors
import io.github.ygaray.yahirandroidtaste.theme.ToneDefaults
import io.github.ygaray.yahirandroidtaste.theme.contrastRatio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Compose tests for [ToneCard]. Color wiring is proven structurally (LocalContentColor, contrast
 * math), since the repo has no pixel-assertion harness.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ToneCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun assertDpEquals(message: String, expected: Dp, actual: Dp) {
        assertEquals(message, expected.value, actual.value, 0.5f)
    }

    /** Renders a ToneCard for each scheme/tone-kind pair in ONE composition (rule limit). */
    private fun captureAll(
        schemes: List<ColorScheme>,
        tones: List<@androidx.compose.runtime.Composable () -> ToneColors>
    ): List<Pair<ToneColors, Color>> {
        val out = mutableListOf<Pair<ToneColors, Color>>()
        composeTestRule.setContent {
            schemes.forEach { scheme ->
                tones.forEach { tone ->
                    MaterialTheme(colorScheme = scheme) {
                        val resolved = tone()
                        ToneCard(tone = resolved) {
                            out += resolved to LocalContentColor.current
                        }
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
        return out
    }

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

    @Test
    fun `caller testTag overrides the default tone_card tag`() {
        composeTestRule.setContent {
            MaterialTheme(colorScheme = LightColorScheme) {
                ToneCard(tone = ToneDefaults.error(), modifier = Modifier.testTag("custom_tone")) {
                    Box(Modifier.size(10.dp))
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("custom_tone").assertExists()
        composeTestRule.onNodeWithTag("tone_card").assertDoesNotExist()
    }

    @Test
    fun `padding spacing and bounds follow 16 horizontal 12 vertical 4 gap`() {
        composeTestRule.setContent {
            MaterialTheme(colorScheme = LightColorScheme) {
                ToneCard(tone = ToneDefaults.error(), modifier = Modifier.width(300.dp)) {
                    Box(Modifier.size(width = 40.dp, height = 20.dp).testTag("child_a"))
                    Box(Modifier.size(width = 40.dp, height = 20.dp).testTag("child_b"))
                }
            }
        }
        composeTestRule.waitForIdle()

        val card = composeTestRule.onNodeWithTag("tone_card").getUnclippedBoundsInRoot()
        val a = composeTestRule.onNodeWithTag("child_a").getUnclippedBoundsInRoot()
        val b = composeTestRule.onNodeWithTag("child_b").getUnclippedBoundsInRoot()

        assertDpEquals("left padding", 16.dp, a.left - card.left)
        assertDpEquals("top padding", 12.dp, a.top - card.top)
        assertDpEquals("child gap", 4.dp, b.top - a.bottom)
        assertDpEquals("bottom padding", 12.dp, card.bottom - b.bottom)
        assertDpEquals("card width", 300.dp, card.right - card.left)
    }

    @Test
    fun `content color equals tone content and is readable for error and warn on both hub schemes`() {
        val results = captureAll(
            schemes = listOf(LightColorScheme, DarkColorScheme),
            tones = listOf({ ToneDefaults.error() }, { ToneDefaults.warn() })
        )
        assertEquals(4, results.size)
        results.forEach { (tone, content) ->
            assertEquals(tone.content, content)
            assertTrue(
                "contrast ${contrastRatio(content, tone.container)}",
                contrastRatio(content, tone.container) >= 4.5f
            )
        }
    }

    @Test
    fun `consumer supplied tone passes straight through`() {
        val supplied = ToneColors(Color(0xFF102030), Color(0xFFE0D0C0))
        var captured = Color.Unspecified
        composeTestRule.setContent {
            ToneCard(tone = supplied) {
                captured = LocalContentColor.current
            }
        }
        composeTestRule.waitForIdle()

        assertEquals(supplied.content, captured)
    }

    @Test
    fun `content lambda has a ColumnScope receiver`() {
        composeTestRule.setContent {
            ToneCard(tone = ToneColors(Color.White, Color.Black), modifier = Modifier.width(200.dp)) {
                Box(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(10.dp)
                        .testTag("aligned_child")
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("aligned_child").assertExists()
    }

    @Test
    fun `corner radius is the 12dp Medium token`() {
        val src = SourceContractTestSupport.stripComments(SourceContractTestSupport.source("ToneCard.kt"))
        assertTrue(src.contains("RoundedCornerShape(Dimens.CornerRadius.Medium)"))
    }
}
