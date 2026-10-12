package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
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

    private fun setCard(
        german: Boolean,
        horizontalPadding: Dp = 0.dp,
        fontScale: Float? = null,
        ladder: List<ApproachRungUiModel> = this.ladder,
        layoutDirection: LayoutDirection? = null,
        unavailableLabel: String = UNAVAILABLE_DE
    ) {
        composeTestRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale ?: density.fontScale),
                LocalLayoutDirection provides (layoutDirection ?: LocalLayoutDirection.current)
            ) {
                YahirAndroidTasteTheme {
                    val cardModifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding)
                    if (german) {
                        ApproachLadderCard(
                            ladder = ladder,
                            offlineOnly = true,
                            onOfflineOnlyChange = {},
                            maxTierId = "local",
                            onMaxTierChange = {},
                            modifier = cardModifier,
                            unavailableLabel = unavailableLabel,
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
                            modifier = cardModifier
                        )
                    }
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
    fun `German offline-only toggle label stays inside its selected segment`() {
        setCard(german = true)

        val labelRight = text(OFFLINE_ONLY_DE).edges().second
        val segmentRight = composeTestRule.onNodeWithContentDescription("$OFFLINE_ONLY_DE, selected")
            .edges().second
        assertTrue(
            "\"$OFFLINE_ONLY_DE\" right edge $labelRight must be <= its segment's right edge $segmentRight",
            labelRight <= segmentRight
        )
    }

    /**
     * IN-01: the 40% name floor is 40% of the WHOLE row; the name-affordance gap comes out of the
     * affordances' share, not the name's. With a rung name too long for the row and an affordance
     * label (one unbreakable compound word) that fills every pixel offered to it, the name still
     * keeps at least 40% of the row. A fixed 60% affordance cap left it 40% minus the 4dp gap.
     */
    @Test
    fun `a long rung name next to a row-filling affordance keeps at least 40 percent of the row`() {
        val longName = "Lokale Verarbeitung ausschließlich auf diesem Gerät"
        val rowFillingLabel = "Verarbeitungsdienstverfügbarkeitsprüfungsfehlschlagsmeldung"
        setCard(
            german = true,
            ladder = listOf(
                ApproachRungUiModel(id = "cloud", label = longName, rank = 3, enabled = false, offlineCapable = false),
                ApproachRungUiModel(id = "local", label = "Local", rank = 1, offlineCapable = true)
            ),
            unavailableLabel = rowFillingLabel
        )

        val rowWidth = composeTestRule.onAllNodesWithTag("approach_ladder_card_rung")[0]
            .fetchSemanticsNode().size.width
        val nameWidth = text(longName).fetchSemanticsNode().size.width
        assertTrue(
            "long name width $nameWidth must be >= 40% of the row width $rowWidth",
            nameWidth >= rowWidth * NAME_FLOOR_FRACTION
        )
        val nameRight = text(longName).edges().second
        listOf(rowFillingLabel, CAPPED_DE, NEEDS_NETWORK_DE).forEach { label ->
            val left = cloudRowNode(label).edges().first
            assertTrue("\"$label\" left edge $left must be >= the name's right edge $nameRight", left >= nameRight)
        }
    }

    /**
     * IN-02: RTL mirrors the row -- the name sits at the right, the affordances at the left without
     * overlapping it, and the offline-only segment labels stay inside their segments.
     */
    @Test
    fun `German labels in RTL put the affordances left of the name without overlap`() {
        setCard(german = true, layoutDirection = LayoutDirection.Rtl)

        val (nameLeft, nameRight) = text("Cloud").edges()
        assertTrue("Cloud name width must be > 0 in RTL", nameRight > nameLeft)
        assertFalse("Cloud name must not be ellipsized in RTL", text("Cloud").textLayout().isLineEllipsized(0))
        listOf(text(UNAVAILABLE_DE), cloudCappedNode(CAPPED_DE), text(NEEDS_NETWORK_DE)).forEach { node ->
            val right = node.edges().second
            assertTrue(
                "RTL affordance right edge $right must be at or left of the name's left edge $nameLeft",
                right <= nameLeft
            )
        }
        listOf(ONLINE_DE to "not selected", OFFLINE_ONLY_DE to "selected").forEach { (label, state) ->
            val (labelLeft, labelRight) = text(label).edges()
            val (segmentLeft, segmentRight) = composeTestRule.onNodeWithContentDescription("$label, $state")
                .edges()
            assertTrue(
                "RTL \"$label\" [$labelLeft, $labelRight] must stay inside its segment [$segmentLeft, $segmentRight]",
                labelLeft >= segmentLeft - EDGE_TOLERANCE_PX && labelRight <= segmentRight + EDGE_TOLERANCE_PX
            )
        }
    }

    /** The topmost match for [label] -- the first rung row (Cloud) in these fixtures. */
    private fun cloudRowNode(label: String) = allText(label).minBy { it.top() }

    @Test
    fun `English default labels at 360dp stay single-line and share one line with the Cloud name`() {
        setCard(german = false)
        assertEnglishCloudRowUnchanged()
    }

    @Test
    @Config(qualifiers = "w320dp-h800dp")
    fun `English default labels at 320dp share one line with the Cloud name`() {
        setCard(german = false)
        assertEnglishCloudRowUnchanged()
    }

    @Test
    fun `English default labels inside the gallery's 16dp padding share one line with the Cloud name`() {
        setCard(german = false, horizontalPadding = 16.dp)
        assertEnglishCloudRowUnchanged()
    }

    @Test
    fun `English default labels at fontScale 1_3 share one line with the Cloud name`() {
        setCard(german = false, fontScale = 1.3f)
        assertEnglishCloudRowUnchanged()
    }

    /**
     * v2.5.0 parity for short labels (CR-01 / WR-02): each English affordance is one line AND the
     * three of them sit on the SAME line next to the Cloud name. A per-Text line count alone cannot
     * see the affordance group wrapping "Needs network" under "Unavailable" / "Capped".
     */
    private fun assertEnglishCloudRowUnchanged() {
        listOf("Unavailable", "Needs network").forEach { label ->
            assertEquals("\"$label\" line count", 1, text(label).textLayout().lineCount)
        }
        allText("Capped").forEach { node ->
            assertEquals("\"Capped\" line count", 1, node.textLayout().lineCount)
        }
        val tops = listOf(text("Unavailable"), cloudCappedNode("Capped"), text("Needs network"))
            .map { it.top() }
        assertEquals("English affordances must share one line, tops=$tops", 1, tops.distinct().size)
        assertFalse("Cloud name must not be ellipsized", text("Cloud").textLayout().isLineEllipsized(0))
    }

    private companion object {
        const val NAME_FLOOR_FRACTION = 0.4f
        const val EDGE_TOLERANCE_PX = 0.5f
        const val UNAVAILABLE_DE = "Derzeit nicht verfügbar"
        const val CAPPED_DE = "Durch Obergrenze eingeschränkt"
        const val NEEDS_NETWORK_DE = "Netzwerkverbindung erforderlich"
        const val ONLINE_DE = "Onlineverarbeitung"
        const val OFFLINE_ONLY_DE = "Ausschließlich offline"
    }
}
