package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import io.github.ygaray.yahirandroidtaste.model.ApproachRungUiModel
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
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
 * G-15-1 regression: [ApproachLadderCard] rung rows must stay legible with long caller-supplied
 * (localized) rung-state labels. Uses the German Gate-2 label set on a 360dp-wide screen.
 *
 * Harness differs from [ApproachLadderCardTest] on purpose: a fixed `w360dp-h800dp` window (the
 * Gate-2 device width class) and `GraphicsMode.NATIVE` so text is measured with real font metrics
 * (the legacy shadow measurement does not reproduce the wrap). Rung rows merge their descendants,
 * so every name/affordance Text is reached with `useUnmergedTree = true`, and geometry is read
 * UNCLIPPED (`positionInRoot` + `size`) -- `boundsInRoot` is clipped to ancestors and would hide
 * an overflow.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ApproachLadderCardLongLabelTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Cloud is disabled + offline-incapable + above the "local" cap, so with offlineOnly = true it
    // carries all three affordances at once (the row the Gate-2 screenshot showed collapsing).
    private val ladder = listOf(
        ApproachRungUiModel(id = "cloud", label = "Cloud", rank = 3, enabled = false, offlineCapable = false),
        ApproachRungUiModel(id = "hybrid", label = "Hybrid", rank = 2, offlineCapable = true),
        ApproachRungUiModel(id = "local", label = "Local", rank = 1, offlineCapable = true)
    )

    private fun setCard(german: Boolean) {
        composeTestRule.setContent {
            YahirAndroidTasteTheme {
                if (german) {
                    ApproachLadderCard(
                        ladder = ladder,
                        offlineOnly = true,
                        onOfflineOnlyChange = {},
                        maxTierId = "local",
                        onMaxTierChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        unavailableLabel = UNAVAILABLE_DE,
                        cappedLabel = CAPPED_DE,
                        needsNetworkLabel = NEEDS_NETWORK_DE,
                        onlineLabel = ONLINE_DE,
                        offlineOnlyLabel = OFFLINE_ONLY_DE
                    )
                } else {
                    ApproachLadderCard(
                        ladder = ladder,
                        offlineOnly = true,
                        onOfflineOnlyChange = {},
                        maxTierId = "local",
                        onMaxTierChange = {},
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun text(value: String) = composeTestRule.onNodeWithText(value, useUnmergedTree = true)

    private fun allText(value: String): List<SemanticsNodeInteraction> {
        val matches = composeTestRule.onAllNodesWithText(value, useUnmergedTree = true)
        val count = matches.fetchSemanticsNodes().size
        assertTrue("expected at least one \"$value\" node", count > 0)
        return (0 until count).map { matches[it] }
    }

    /** The node's single [TextLayoutResult], via the GetTextLayoutResult semantics action. */
    private fun SemanticsNodeInteraction.textLayout(): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        val action = fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action
        checkNotNull(action) { "node has no GetTextLayoutResult action" }.invoke(results)
        return results.single()
    }

    /** Unclipped horizontal layout edges in root px: (left, right). */
    private fun SemanticsNodeInteraction.edges(): Pair<Float, Float> {
        val node = fetchSemanticsNode()
        val left = node.positionInRoot.x
        return left to left + node.size.width
    }

    private fun SemanticsNodeInteraction.top(): Float = fetchSemanticsNode().positionInRoot.y

    /** The Cloud row's capped affordance: "capped" also shows on Hybrid, Cloud's is the topmost. */
    private fun cloudCappedNode(label: String) = allText(label).minBy { it.top() }

    @Test
    fun `German labels at 360dp keep the Cloud rung name displayed with nonzero unellipsized width`() {
        setCard(german = true)

        val name = text("Cloud")
        name.assertIsDisplayed()
        val width = name.fetchSemanticsNode().size.width
        assertTrue("Cloud name width must be > 0, was $width", width > 0)
        assertFalse("Cloud name must not be ellipsized", name.textLayout().isLineEllipsized(0))
    }

    @Test
    fun `German affordance labels each render with nonzero width on at most two lines`() {
        setCard(german = true)

        listOf(UNAVAILABLE_DE, CAPPED_DE, NEEDS_NETWORK_DE).forEach { label ->
            allText(label).forEachIndexed { i, node ->
                val width = node.fetchSemanticsNode().size.width
                assertTrue("\"$label\" #$i width must be > 0, was $width", width > 0)
                val lines = node.textLayout().lineCount
                assertTrue("\"$label\" #$i must render on <= 2 lines, rendered on $lines", lines <= 2)
            }
        }
    }

    @Test
    fun `German affordances on the Cloud row never overlap the rung name`() {
        setCard(german = true)

        val nameRight = text("Cloud").edges().second
        val affordances = listOf(
            text(UNAVAILABLE_DE),
            cloudCappedNode(CAPPED_DE),
            text(NEEDS_NETWORK_DE)
        )
        affordances.forEach { node ->
            val (left, _) = node.edges()
            assertTrue(
                "affordance left edge $left must be at or right of the name's right edge $nameRight",
                left >= nameRight
            )
        }
    }

    @Test
    fun `English default labels stay single-line and the Cloud name is not ellipsized`() {
        setCard(german = false)

        listOf("Unavailable", "Needs network").forEach { label ->
            assertEquals("\"$label\" line count", 1, text(label).textLayout().lineCount)
        }
        allText("Capped").forEach { node ->
            assertEquals("\"Capped\" line count", 1, node.textLayout().lineCount)
        }
        assertFalse("Cloud name must not be ellipsized", text("Cloud").textLayout().isLineEllipsized(0))
    }

    private companion object {
        const val UNAVAILABLE_DE = "Derzeit nicht verfügbar"
        const val CAPPED_DE = "Durch Obergrenze eingeschränkt"
        const val NEEDS_NETWORK_DE = "Netzwerkverbindung erforderlich"
        const val ONLINE_DE = "Onlineverarbeitung"
        const val OFFLINE_ONLY_DE = "Ausschließlich offline"
    }
}
