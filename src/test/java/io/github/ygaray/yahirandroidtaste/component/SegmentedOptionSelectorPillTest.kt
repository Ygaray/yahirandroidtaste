package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
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
 * VSEG-01: the Pill style and per-option disable of [SegmentedOptionSelector].
 *
 * Same harness idiom as [SegmentedOptionSelectorTest] (w360dp, GraphicsMode.NATIVE, the library
 * theme). Both styles expose the same `"$label, selected"` / `"$label, not selected"` content
 * description so one addressing scheme works for both. Geometry is read UNCLIPPED because clipped
 * bounds would hide the overflow these tests exist to catch. No pixel capture (R-14): container
 * color and shape are proven by a source contract on the Pill body, text colors by the text layout.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SegmentedOptionSelectorPillTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var scheme: ColorScheme? = null

    private fun setSelector(
        options: List<String>,
        selectedIndex: Int,
        style: SegmentedOptionStyle = SegmentedOptionStyle.Pill,
        disabledOptions: Set<Int> = emptySet(),
        enabled: Boolean = true,
        disabledReason: String? = null,
        fontScale: Float? = null,
        onSelect: (Int) -> Unit = {}
    ) {
        composeTestRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale ?: density.fontScale)
            ) {
                YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                    scheme = MaterialTheme.colorScheme
                    Box(Modifier.fillMaxWidth().padding(16.dp)) {
                        SegmentedOptionSelector(
                            selectedIndex = selectedIndex,
                            options = options,
                            onSelect = onSelect,
                            enabled = enabled,
                            disabledReason = disabledReason,
                            disabledOptions = disabledOptions,
                            style = style
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun segment(text: String, selected: Boolean) = composeTestRule.onNodeWithContentDescription(
        "$text, ${if (selected) "selected" else "not selected"}"
    )

    private fun label(text: String) = composeTestRule.onNodeWithText(text, useUnmergedTree = true)

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

    // ---- Tracer cases -------------------------------------------------------------------------

    @Test
    fun `pill shows both labels and a click on the second segment selects it once`() {
        val calls = mutableListOf<Int>()
        setSelector(listOf("Eggs", "Grams"), selectedIndex = 0, onSelect = { calls += it })

        segment("Eggs", selected = true).assertExists()
        segment("Grams", selected = false).assertExists().performClick()
        composeTestRule.waitForIdle()

        assertEquals(listOf(1), calls)
    }

    @Test
    fun `pill with the first option disabled never selects it while the sibling still does`() {
        val calls = mutableListOf<Int>()
        setSelector(
            listOf("Eggs", "Grams"),
            selectedIndex = 1,
            disabledOptions = setOf(0),
            onSelect = { calls += it }
        )

        segment("Eggs", selected = false).performClick()
        composeTestRule.waitForIdle()
        assertEquals(emptyList<Int>(), calls)

        segment("Grams", selected = true).performClick()
        composeTestRule.waitForIdle()
        assertEquals(listOf(1), calls)
    }

    @Test
    fun `pill sibling of a disabled option is enabled and the disabled one is not`() {
        setSelector(listOf("Eggs", "Grams"), selectedIndex = 1, disabledOptions = setOf(0))

        segment("Eggs", selected = false).assertIsNotEnabled()
        segment("Grams", selected = true).assertIsEnabled()
    }

    // ---- Geometry and semantics ---------------------------------------------------------------

    @Test
    fun `pill segments are 40dp tall equal in height and each about half the container`() {
        setSelector(listOf("Eggs", "Grams"), selectedIndex = 0)

        val a = segment("Eggs", selected = true).getUnclippedBoundsInRoot()
        val b = segment("Grams", selected = false).getUnclippedBoundsInRoot()
        val heightA = (a.bottom - a.top).value
        val heightB = (b.bottom - b.top).value
        assertEquals("selected segment height", 40f, heightA, 1f)
        assertEquals("unselected segment height", 40f, heightB, 1f)
        assertEquals("segments must be equal height", heightA, heightB, 0.5f)
        // 360dp window minus 16dp of padding on each side.
        assertEquals("selected segment width", 164f, (a.right - a.left).value, 1f)
        assertEquals("unselected segment width", 164f, (b.right - b.left).value, 1f)
    }

    @Test
    fun `pill labels sit at the center of their own segment with no check icon slot`() {
        setSelector(listOf("Eggs", "Grams"), selectedIndex = 0)

        listOf("Eggs" to true, "Grams" to false).forEach { (text, selected) ->
            val seg = segment(text, selected).getUnclippedBoundsInRoot()
            val lab = label(text).getUnclippedBoundsInRoot()
            assertEquals(
                "\"$text\" horizontal center",
                (seg.left + seg.right).value / 2f,
                (lab.left + lab.right).value / 2f,
                1f
            )
            assertEquals(
                "\"$text\" vertical center",
                (seg.top + seg.bottom).value / 2f,
                (lab.top + lab.bottom).value / 2f,
                1f
            )
        }
    }

    @Test
    fun `pill segments are radio buttons and the selected one is selected`() {
        setSelector(listOf("Eggs", "Grams"), selectedIndex = 0)
        val radio = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton)

        segment("Eggs", selected = true).assert(radio).assertIsSelected()
        segment("Grams", selected = false).assert(radio).assertIsNotSelected()
    }

    @Test
    fun `pill selected label is onPrimary and the unselected label is onSurface`() {
        setSelector(listOf("Eggs", "Grams"), selectedIndex = 0)
        val colors = checkNotNull(scheme)

        // A3: the text layout input carries the resolved style color in this harness.
        val selectedColor = label("Eggs").textLayout().layoutInput.style.color
        val unselectedColor = label("Grams").textLayout().layoutInput.style.color
        assertEquals("selected label color", colors.onPrimary, selectedColor)
        assertEquals("unselected label color", colors.onSurface, unselectedColor)
    }

    // ---- Long localized labels (ROADMAP SC3, long-localized-label lesson) ----------------------

    @Test
    fun `long German pill labels truncate to one ellipsized line inside their segment at 360dp`() {
        val short = "Onlineverarbeitung"
        val long = "Ausschließlich lokal ohne Netzwerkverbindung verarbeiten"
        setSelector(listOf(short, long), selectedIndex = 1)

        assertEquals("short label line count", 1, label(short).textLayout().lineCount)
        assertEquals("long label line count", 1, label(long).textLayout().lineCount)
        assertTrue("long label must be ellipsized", label(long).textLayout().isLineEllipsized(0))

        listOf(short to false, long to true).forEach { (text, selected) ->
            val (labelLeft, labelRight) = label(text).edges()
            val (segmentLeft, segmentRight) = segment(text, selected).edges()
            assertTrue(
                "\"$text\" right edge $labelRight must be <= its segment's right edge $segmentRight",
                labelRight <= segmentRight + EDGE_TOLERANCE_PX
            )
            assertTrue(
                "\"$text\" left edge $labelLeft must be >= its segment's left edge $segmentLeft",
                labelLeft >= segmentLeft - EDGE_TOLERANCE_PX
            )
        }
        val shortHeight = segment(short, selected = false).getUnclippedBoundsInRoot()
        val longHeight = segment(long, selected = true).getUnclippedBoundsInRoot()
        assertEquals(
            "segments keep the same height",
            (shortHeight.bottom - shortHeight.top).value,
            (longHeight.bottom - longHeight.top).value,
            0.5f
        )
        assertEquals("segment height stays 40dp", 40f, (longHeight.bottom - longHeight.top).value, 1f)
    }

    @Test
    fun `short pill labels are not ellipsized`() {
        setSelector(listOf("Eggs", "Grams"), selectedIndex = 0)

        assertFalse(label("Eggs").textLayout().isLineEllipsized(0))
        assertFalse(label("Grams").textLayout().isLineEllipsized(0))
    }

    @Test
    fun `pill segments stay single-line equal and at least 40dp at fontScale 2`() {
        setSelector(listOf("Eggs", "Grams"), selectedIndex = 0, fontScale = 2f)

        assertEquals(1, label("Eggs").textLayout().lineCount)
        assertEquals(1, label("Grams").textLayout().lineCount)
        val a = segment("Eggs", selected = true).getUnclippedBoundsInRoot()
        val b = segment("Grams", selected = false).getUnclippedBoundsInRoot()
        val heightA = (a.bottom - a.top).value
        val heightB = (b.bottom - b.top).value
        assertTrue("selected segment height $heightA must be >= 40dp", heightA >= 40f - 0.5f)
        assertTrue("unselected segment height $heightB must be >= 40dp", heightB >= 40f - 0.5f)
        assertEquals("segments must be equal height", heightA, heightB, 0.5f)
    }

    @Test
    fun `pill touch bounds are at least 48dp tall`() {
        setSelector(listOf("Eggs", "Grams"), selectedIndex = 0)

        // A6: the framework expands a selectable's touch bounds to the 48dp minimum.
        val density = composeTestRule.density
        val minHeightPx = with(density) { 48.dp.toPx() }
        listOf("Eggs" to true, "Grams" to false).forEach { (text, selected) ->
            val touch = segment(text, selected).fetchSemanticsNode().touchBoundsInRoot
            assertTrue(
                "\"$text\" touch height ${touch.height} must be >= $minHeightPx",
                touch.height >= minHeightPx - 0.5f
            )
        }
    }

    // ---- Whole-control disable ----------------------------------------------------------------

    @Test
    fun `pill with enabled false disables both segments, never selects and shows the reason`() {
        val calls = mutableListOf<Int>()
        setSelector(
            listOf("Eggs", "Grams"),
            selectedIndex = 0,
            enabled = false,
            disabledReason = "Unavailable offline",
            onSelect = { calls += it }
        )

        segment("Eggs", selected = true).assertIsNotEnabled()
        segment("Grams", selected = false).assertIsNotEnabled().performClick()
        composeTestRule.waitForIdle()
        assertEquals(emptyList<Int>(), calls)
        composeTestRule.onNodeWithText("Unavailable offline").assertExists()
    }

    // ---- Per-option disable, both styles ------------------------------------------------------

    private fun assertFirstOptionDisabled(style: SegmentedOptionStyle) {
        val calls = mutableListOf<Int>()
        setSelector(
            listOf("Eggs", "Grams"),
            selectedIndex = 1,
            style = style,
            disabledOptions = setOf(0),
            onSelect = { calls += it }
        )

        segment("Eggs", selected = false).assertIsNotEnabled().performClick()
        composeTestRule.waitForIdle()
        assertEquals("disabled option must never fire onSelect ($style)", emptyList<Int>(), calls)

        // The sibling is the already selected segment; a click on an enabled segment still fires.
        segment("Grams", selected = true).assertIsEnabled().performClick()
        composeTestRule.waitForIdle()
        assertEquals("sibling must fire onSelect exactly once ($style)", listOf(1), calls)
    }

    @Test
    fun `pill first option disabled never fires and the sibling still does`() {
        assertFirstOptionDisabled(SegmentedOptionStyle.Pill)
    }

    @Test
    fun `material first option disabled never fires and the sibling still does`() {
        assertFirstOptionDisabled(SegmentedOptionStyle.Material)
    }

    private fun assertSecondOptionDisabled(style: SegmentedOptionStyle) {
        val calls = mutableListOf<Int>()
        setSelector(
            listOf("Eggs", "Grams"),
            selectedIndex = 0,
            style = style,
            disabledOptions = setOf(1),
            onSelect = { calls += it }
        )

        segment("Grams", selected = false).assertIsNotEnabled().performClick()
        composeTestRule.waitForIdle()
        assertEquals("disabled option must never fire onSelect ($style)", emptyList<Int>(), calls)
        segment("Eggs", selected = true).assertIsEnabled().performClick()
        composeTestRule.waitForIdle()
        assertEquals("sibling must fire onSelect exactly once ($style)", listOf(0), calls)
    }

    @Test
    fun `pill second option disabled never fires and the sibling still does`() {
        assertSecondOptionDisabled(SegmentedOptionStyle.Pill)
    }

    @Test
    fun `material second option disabled never fires and the sibling still does`() {
        assertSecondOptionDisabled(SegmentedOptionStyle.Material)
    }

    private fun assertSelectedAndDisabled(style: SegmentedOptionStyle) {
        val calls = mutableListOf<Int>()
        setSelector(
            listOf("Eggs", "Grams"),
            selectedIndex = 0,
            style = style,
            disabledOptions = setOf(0),
            onSelect = { calls += it }
        )

        segment("Eggs", selected = true).assertIsNotEnabled().assertIsSelected()
        segment("Grams", selected = false).assertIsEnabled().performClick()
        composeTestRule.waitForIdle()
        assertEquals(listOf(1), calls)
    }

    @Test
    fun `pill selected and disabled option is selected, not enabled, and the sibling still fires`() {
        assertSelectedAndDisabled(SegmentedOptionStyle.Pill)
    }

    @Test
    fun `material selected and disabled option is selected, not enabled, and the sibling still fires`() {
        assertSelectedAndDisabled(SegmentedOptionStyle.Material)
    }

    private fun assertForeignIndicesInert(style: SegmentedOptionStyle) {
        setSelector(
            listOf("Eggs", "Grams"),
            selectedIndex = 0,
            style = style,
            disabledOptions = setOf(-1, 2, 7)
        )

        segment("Eggs", selected = true).assertIsEnabled()
        segment("Grams", selected = false).assertIsEnabled()
    }

    @Test
    fun `pill indices outside the option range are inert`() {
        assertForeignIndicesInert(SegmentedOptionStyle.Pill)
    }

    @Test
    fun `material indices outside the option range are inert`() {
        assertForeignIndicesInert(SegmentedOptionStyle.Material)
    }

    // ---- Source contract on the Pill body -----------------------------------------------------

    @Test
    fun `pill body carries its colors shape tokens and single-line label rules and no icon or segmented button`() {
        val stripped = SourceContractTestSupport.stripComments(
            SourceContractTestSupport.source("SegmentedOptionSelector.kt")
        )
        val body = SourceContractTestSupport.functionBody(stripped, "fun PillSegmentedOptionSelector(")
            .filterNot { it.isWhitespace() }

        listOf(
            "DISABLED_CONTENT_ALPHA",
            "colorScheme.primary",
            "onPrimary",
            "CircleShape",
            "outlineVariant",
            "Dimens.HairlineBorder",
            "Dimens.SegmentedPill.Height",
            "selectableGroup",
            "Role.RadioButton",
            "TextOverflow.Ellipsis",
            "maxLines=1"
        ).forEach { token -> assertTrue("Pill body must contain $token", body.contains(token)) }
        assertFalse("Pill body must not draw an Icon", body.contains("Icon("))
        assertFalse("Pill body must not use SegmentedButton", body.contains("SegmentedButton("))
    }

    private companion object {
        const val EDGE_TOLERANCE_PX = 0.5f
    }
}
