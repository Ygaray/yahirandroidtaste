package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Compose tests for [RoundIconButton] (VBTN-02): click and disabled behavior, accessible name, the
 * stable tag and its caller override, the drawn size for the default / large / small diameters, the
 * 48dp touch region and the 48dp layout reserve between neighbours.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RoundIconButtonTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `click fires onClick exactly once when enabled`() {
        var clicks = 0
        composeTestRule.setContent {
            RoundIconButton(icon = Icons.Filled.Add, contentDescription = "Add", onClick = { clicks++ })
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Add").performClick()
        composeTestRule.waitForIdle()

        assertEquals(1, clicks)
    }

    @Test
    fun `disabled node is not enabled and never fires onClick`() {
        var clicks = 0
        composeTestRule.setContent {
            RoundIconButton(
                icon = Icons.Filled.Add,
                contentDescription = "Add",
                onClick = { clicks++ },
                enabled = false
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Add").assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription("Add").performClick()
        composeTestRule.waitForIdle()

        assertEquals(0, clicks)
    }

    @Test
    fun `supplied contentDescription is exposed`() {
        composeTestRule.setContent {
            RoundIconButton(icon = Icons.Filled.Add, contentDescription = "Add item", onClick = {})
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Add item").assertExists()
    }

    @Test
    fun `default tag is round_icon_button`() {
        composeTestRule.setContent {
            RoundIconButton(icon = Icons.Filled.Add, contentDescription = "Add", onClick = {})
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("round_icon_button").assertExists()
    }

    @Test
    fun `a caller testTag overrides the library tag`() {
        composeTestRule.setContent {
            RoundIconButton(
                icon = Icons.Filled.Add,
                contentDescription = "Add",
                onClick = {},
                modifier = Modifier.testTag("custom_round")
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("custom_round").assertExists()
        composeTestRule.onNodeWithTag("round_icon_button").assertDoesNotExist()
    }

    /** Six buttons: default, 36dp and 64dp, each as a side-by-side pair (pair order: default, 36, 64). */
    private fun setPairsContent(captureDensity: (Density) -> Unit) {
        composeTestRule.setContent {
            captureDensity(LocalDensity.current)
            Column {
                Row {
                    RoundIconButton(icon = Icons.Filled.Add, contentDescription = "a", onClick = {})
                    RoundIconButton(icon = Icons.Filled.Add, contentDescription = "b", onClick = {})
                }
                Row {
                    RoundIconButton(icon = Icons.Filled.Add, contentDescription = "c", onClick = {}, size = 36.dp)
                    RoundIconButton(icon = Icons.Filled.Add, contentDescription = "d", onClick = {}, size = 36.dp)
                }
                Row {
                    RoundIconButton(icon = Icons.Filled.Add, contentDescription = "e", onClick = {}, size = 64.dp)
                    RoundIconButton(icon = Icons.Filled.Add, contentDescription = "f", onClick = {}, size = 64.dp)
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun `node is the drawn circle for default 64dp and 36dp and the touch region is at least 48dp`() {
        var density: Density? = null
        setPairsContent { density = it }
        val nodes = composeTestRule.onAllNodesWithTag("round_icon_button")
        val minPx = with(density!!) { 48.dp.toPx() }

        // Pair leaders: indices 0 (default 40dp), 2 (36dp), 4 (64dp).
        val expected = listOf(0 to 40f, 2 to 36f, 4 to 64f)
        for ((index, dp) in expected) {
            val bounds = nodes[index].getUnclippedBoundsInRoot()
            assertEquals("node $index width", dp, bounds.width.value, 0.5f)
            assertEquals("node $index height", dp, bounds.height.value, 0.5f)

            val touch = nodes[index].fetchSemanticsNode().touchBoundsInRoot
            assertTrue("node $index touch width ${touch.width} >= $minPx", touch.width >= minPx)
            assertTrue("node $index touch height ${touch.height} >= $minPx", touch.height >= minPx)
        }
    }

    @Test
    fun `neighbours keep a 48dp layout pitch for default and 36dp and 64dp for the large size`() {
        setPairsContent { }
        val nodes = composeTestRule.onAllNodesWithTag("round_icon_button")

        val expectedPitch = listOf(0 to 48f, 2 to 48f, 4 to 64f)
        for ((index, pitch) in expectedPitch) {
            val first = nodes[index].getUnclippedBoundsInRoot()
            val second = nodes[index + 1].getUnclippedBoundsInRoot()
            assertEquals(
                "pair at $index left-edge pitch",
                pitch,
                second.left.value - first.left.value,
                0.5f
            )
        }
    }

    @Test
    fun `source contract - theme roles, circle shape, tokens and button role`() {
        val src = SourceContractTestSupport.stripComments(SourceContractTestSupport.source("RoundIconButton.kt"))

        listOf(
            "colorScheme.outlineVariant",
            "colorScheme.onSurface",
            "CircleShape",
            "Dimens.HairlineBorder",
            "Dimens.RoundIconButton.Size",
            "Dimens.RoundIconButton.IconSize",
            "Role.Button"
        ).forEach { token -> assertTrue("source must contain $token", src.contains(token)) }
        assertFalse("must not use the Material outlined icon button", src.contains("OutlinedIconButton"))
    }
}
