package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
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
import org.robolectric.annotation.GraphicsMode

/**
 * Compose tests for [PillChip] (VCHIP-01): click and disabled behavior, tag and override, accessible
 * name both ways, the single clickable root, the 32dp drawn height, the 48dp touch region and layout
 * reserve, the per-side icon padding rule and the source contract.
 *
 * NATIVE graphics gives real text metrics, so width deltas between icon variants are meaningful.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PillChipTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `renders the label and click fires onClick exactly once`() {
        var clicks = 0
        composeTestRule.setContent { PillChip(label = "Today", onClick = { clicks++ }) }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Today").assertExists()
        composeTestRule.onNodeWithText("Today").performClick()
        composeTestRule.waitForIdle()

        assertEquals(1, clicks)
    }

    @Test
    fun `disabled node is not enabled and never fires onClick`() {
        var clicks = 0
        composeTestRule.setContent { PillChip(label = "Today", onClick = { clicks++ }, enabled = false) }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Today").assertIsNotEnabled()
        composeTestRule.onNodeWithText("Today").performClick()
        composeTestRule.waitForIdle()

        assertEquals(0, clicks)
    }

    @Test
    fun `default tag is pill_chip`() {
        composeTestRule.setContent { PillChip(label = "Today", onClick = {}) }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("pill_chip").assertExists()
    }

    @Test
    fun `a caller testTag overrides the library tag`() {
        composeTestRule.setContent {
            PillChip(label = "Today", onClick = {}, modifier = Modifier.testTag("custom_pill"))
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("custom_pill").assertExists()
        composeTestRule.onNodeWithTag("pill_chip").assertDoesNotExist()
    }

    @Test
    fun `a non-null contentDescription is the accessible name`() {
        composeTestRule.setContent {
            PillChip(label = "Today", onClick = {}, contentDescription = "Pick the date")
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Pick the date").assertExists()
    }

    @Test
    fun `a null contentDescription keeps the label as the node text and sets no description`() {
        composeTestRule.setContent { PillChip(label = "Today", onClick = {}) }
        composeTestRule.waitForIdle()

        val config = composeTestRule.onNodeWithTag("pill_chip").fetchSemanticsNode().config
        assertFalse(
            "no content description should be set",
            config.contains(SemanticsProperties.ContentDescription)
        )
        val texts = config[SemanticsProperties.Text].map { it.text }
        assertEquals(listOf("Today"), texts)
    }

    @Test
    fun `tag, click action and description live on one single node`() {
        composeTestRule.setContent {
            PillChip(label = "Today", onClick = {}, contentDescription = "Pick the date")
        }
        composeTestRule.waitForIdle()

        val tagged = composeTestRule.onNodeWithTag("pill_chip").fetchSemanticsNode()
        val described = composeTestRule.onNodeWithContentDescription("Pick the date").fetchSemanticsNode()
        val byText = composeTestRule.onNodeWithText("Today").fetchSemanticsNode()

        assertTrue("tagged node must carry the click action", tagged.config.contains(SemanticsActions.OnClick))
        assertEquals("description node must be the tagged node", tagged.id, described.id)
        assertEquals("text node must be the tagged node (label merged into the root)", tagged.id, byText.id)
    }

    @Test
    fun `label-only chip text node is the tagged node`() {
        composeTestRule.setContent { PillChip(label = "Today", onClick = {}) }
        composeTestRule.waitForIdle()

        val tagged = composeTestRule.onNodeWithTag("pill_chip").fetchSemanticsNode()
        val byText = composeTestRule.onNodeWithText("Today").fetchSemanticsNode()

        assertEquals(tagged.id, byText.id)
        assertTrue(tagged.config.contains(SemanticsActions.OnClick))
    }

    @Test
    fun `chip is 32dp tall, has a touch region of at least 48dp and stacked chips sit 48dp apart`() {
        var density: Density? = null
        composeTestRule.setContent {
            density = LocalDensity.current
            Column {
                PillChip(label = "Today", onClick = {})
                PillChip(label = "Tomorrow", onClick = {})
            }
        }
        composeTestRule.waitForIdle()

        val nodes = composeTestRule.onAllNodesWithTag("pill_chip")
        val first = nodes[0].getUnclippedBoundsInRoot()
        val second = nodes[1].getUnclippedBoundsInRoot()
        assertEquals("drawn height", 32f, first.height.value, 0.5f)

        val minPx = with(density!!) { 48.dp.toPx() }
        val touch = nodes[0].fetchSemanticsNode().touchBoundsInRoot
        assertTrue("touch width ${touch.width} >= $minPx", touch.width >= minPx)
        assertTrue("touch height ${touch.height} >= $minPx", touch.height >= minPx)

        assertEquals("stacked top-edge pitch", 48f, second.top.value - first.top.value, 0.5f)
    }

    @Test
    fun `icons add 18dp per icon side relative to a label-only chip`() {
        composeTestRule.setContent {
            Column {
                PillChip(label = LABEL, onClick = {})
                PillChip(label = LABEL, onClick = {}, leadingIcon = Icons.Filled.DateRange)
                PillChip(label = LABEL, onClick = {}, trailingIcon = Icons.Filled.ArrowDropDown)
                PillChip(
                    label = LABEL,
                    onClick = {},
                    leadingIcon = Icons.Filled.DateRange,
                    trailingIcon = Icons.Filled.ArrowDropDown
                )
            }
        }
        composeTestRule.waitForIdle()

        val nodes = composeTestRule.onAllNodesWithTag("pill_chip")
        val widths = (0..3).map { nodes[it].getUnclippedBoundsInRoot().width.value }
        val labelOnly = widths[0]
        // 12dp padding becomes 8dp + 18dp icon + 4dp gap = +18dp for each icon side.
        assertEquals("leading icon delta", 18f, widths[1] - labelOnly, 1f)
        assertEquals("trailing icon delta", 18f, widths[2] - labelOnly, 1f)
        assertEquals("both icons delta", 36f, widths[3] - labelOnly, 1f)
    }

    @Test
    fun `source contract - single line, ellipsis, theme roles, tokens and button role`() {
        val src = SourceContractTestSupport.stripComments(SourceContractTestSupport.source("PillChip.kt"))

        listOf(
            "maxLines = 1",
            "TextOverflow.Ellipsis",
            "MaterialTheme.typography.bodySmall",
            "CircleShape",
            "colorScheme.outlineVariant",
            "Dimens.PillChip.Height",
            "Dimens.PillChip.LabelOnlyHorizontalPadding",
            "Dimens.PillChip.IconSideHorizontalPadding",
            "Dimens.Icons.ChipIcon",
            "Role.Button"
        ).forEach { token -> assertTrue("source must contain $token", src.contains(token)) }
    }

    private companion object {
        const val LABEL = "Tomorrow evening"
    }
}
