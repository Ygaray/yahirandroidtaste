package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.component.ActionButtonDefaults.ActionButtonRole
import io.github.ygaray.yahirandroidtaste.theme.DarkColorScheme
import io.github.ygaray.yahirandroidtaste.theme.LightColorScheme
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * VBTN-01 behavior proof for [DynamicActionButton]'s emphasis and icon axis (Phase 20, plan 02).
 *
 * Pure palette tests need no composition. Compose tests render every variant of one concern in a
 * SINGLE composition (the rule allows one `setContent` per test) and compare tagged node sizes.
 * NATIVE graphics gives real text metrics (the legacy shadow measures about 1px per character, which
 * collapses every label under the Material minimum button width and hides the padding and icon
 * deltas); the tall window keeps all 12 stacked buttons on screen so every click lands.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h1600dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DynamicActionButtonEmphasisTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var density: Density = Density(1f)

    private val roles = listOf(ActionButtonRole.Save, ActionButtonRole.Neutral, ActionButtonRole.Destructive)

    // --- Pure palette ---

    private fun expectedPalette(
        emphasis: ActionButtonEmphasis,
        role: ActionButtonRole,
        scheme: ColorScheme
    ): EmphasisColors {
        val destructive = role == ActionButtonRole.Destructive
        val hue = if (destructive) scheme.error else scheme.primary
        return when (emphasis) {
            ActionButtonEmphasis.Filled -> EmphasisColors(hue, if (destructive) scheme.onError else scheme.onPrimary)
            ActionButtonEmphasis.Tonal -> EmphasisColors(hue.copy(alpha = 0.14f), hue)
            ActionButtonEmphasis.Text, ActionButtonEmphasis.TextSmall -> EmphasisColors(Color.Transparent, hue)
        }
    }

    @Test
    fun `emphasisColors matches the Y-8 palette for every emphasis and role in both schemes`() {
        listOf(LightColorScheme, DarkColorScheme).forEach { scheme ->
            ActionButtonEmphasis.entries.forEach { emphasis ->
                roles.forEach { role ->
                    assertEquals(
                        "$emphasis x $role",
                        expectedPalette(emphasis, role, scheme),
                        emphasisColors(emphasis, role, scheme)
                    )
                }
            }
        }
    }

    @Test
    fun `Filled uses primary and onPrimary for Save and Neutral and error and onError for Destructive`() {
        val scheme = LightColorScheme
        assertEquals(
            EmphasisColors(scheme.primary, scheme.onPrimary),
            emphasisColors(ActionButtonEmphasis.Filled, ActionButtonRole.Save, scheme)
        )
        assertEquals(
            EmphasisColors(scheme.primary, scheme.onPrimary),
            emphasisColors(ActionButtonEmphasis.Filled, ActionButtonRole.Neutral, scheme)
        )
        assertEquals(
            EmphasisColors(scheme.error, scheme.onError),
            emphasisColors(ActionButtonEmphasis.Filled, ActionButtonRole.Destructive, scheme)
        )
    }

    @Test
    fun `tonal alpha is 0_14 and the legacy emphasis mapping is Save Filled otherwise Text`() {
        assertEquals(0.14f, TONAL_CONTAINER_ALPHA, 0f)
        assertEquals(ActionButtonEmphasis.Filled, legacyEmphasisFor(ActionButtonRole.Save))
        assertEquals(ActionButtonEmphasis.Text, legacyEmphasisFor(ActionButtonRole.Neutral))
        assertEquals(ActionButtonEmphasis.Text, legacyEmphasisFor(ActionButtonRole.Destructive))
    }

    // --- Behavior per emphasis x role ---

    private fun combos() = ActionButtonEmphasis.entries.flatMap { e -> roles.map { r -> e to r } }

    private fun comboLabel(e: ActionButtonEmphasis, r: ActionButtonRole) = "Go $e $r"

    @Test
    fun `every emphasis and role shows its label and exposes Role Button`() {
        composeTestRule.setContent {
            YahirAndroidTasteTheme(dynamicColor = false) {
                Column {
                    combos().forEach { (e, r) ->
                        DynamicActionButton(comboLabel(e, r), r, onClick = {}, emphasis = e)
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
        combos().forEach { (e, r) ->
            composeTestRule.onNodeWithText(comboLabel(e, r))
                .assertExists()
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
                .assertIsEnabled()
        }
    }

    @Test
    fun `an enabled button of every emphasis and role invokes onClick exactly once`() {
        val counts = mutableMapOf<String, Int>()
        composeTestRule.setContent {
            YahirAndroidTasteTheme(dynamicColor = false) {
                Column {
                    combos().forEach { (e, r) ->
                        val label = comboLabel(e, r)
                        DynamicActionButton(
                            label,
                            r,
                            onClick = { counts[label] = (counts[label] ?: 0) + 1 },
                            emphasis = e
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
        combos().forEach { (e, r) ->
            composeTestRule.onNodeWithText(comboLabel(e, r)).performClick()
        }
        composeTestRule.waitForIdle()
        combos().forEach { (e, r) -> assertEquals("$e x $r", 1, counts[comboLabel(e, r)]) }
    }

    @Test
    fun `a disabled button of every emphasis and role is not enabled and never invokes onClick`() {
        var calls = 0
        composeTestRule.setContent {
            YahirAndroidTasteTheme(dynamicColor = false) {
                Column {
                    combos().forEach { (e, r) ->
                        DynamicActionButton(comboLabel(e, r), r, onClick = { calls++ }, enabled = false, emphasis = e)
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
        combos().forEach { (e, r) ->
            composeTestRule.onNodeWithText(comboLabel(e, r)).assertIsNotEnabled().performClick()
        }
        composeTestRule.waitForIdle()
        assertEquals(0, calls)
    }

    // --- Measurement helpers ---

    private fun widthDp(tag: String): Float =
        composeTestRule.onNodeWithTag(tag).fetchSemanticsNode().size.width / density.density

    private fun heightDp(tag: String): Float =
        composeTestRule.onNodeWithTag(tag).fetchSemanticsNode().size.height / density.density

    private fun renderTagged(
        entries: List<Triple<String, ActionButtonRole, Pair<ActionButtonEmphasis?, ImageVector?>>>,
        label: String = WIDE_LABEL
    ) {
        composeTestRule.setContent {
            density = LocalDensity.current
            YahirAndroidTasteTheme(dynamicColor = false) {
                Column {
                    entries.forEach { (tag, role, spec) ->
                        DynamicActionButton(
                            label = label,
                            role = role,
                            onClick = {},
                            modifier = Modifier.testTag(tag),
                            emphasis = spec.first,
                            icon = spec.second
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun tagged(tag: String, emphasis: ActionButtonEmphasis?, icon: ImageVector? = null) =
        Triple(tag, ActionButtonRole.Neutral, emphasis to icon)

    // --- Icon: 18dp icon + 8dp gap = 26dp ---

    @Test
    fun `the leading icon adds 26dp of width for Filled Text and TextSmall`() {
        val check = Icons.Filled.Check
        renderTagged(
            listOf(
                tagged("filled", ActionButtonEmphasis.Filled),
                tagged("filled_icon", ActionButtonEmphasis.Filled, check),
                tagged("text", ActionButtonEmphasis.Text),
                tagged("text_icon", ActionButtonEmphasis.Text, check),
                tagged("small", ActionButtonEmphasis.TextSmall),
                tagged("small_icon", ActionButtonEmphasis.TextSmall, check)
            )
        )
        listOf("filled", "text", "small").forEach { base ->
            val delta = widthDp(base + "_icon") - widthDp(base)
            assertEquals("$base icon delta", 26f, delta, 1f)
        }
    }

    // --- Padding rhythm ---

    @Test
    fun `Filled is 16dp wider than Text and Tonal matches Filled`() {
        renderTagged(
            listOf(
                tagged("filled", ActionButtonEmphasis.Filled),
                tagged("tonal", ActionButtonEmphasis.Tonal),
                tagged("text", ActionButtonEmphasis.Text)
            )
        )
        assertEquals("Filled - Text width", 16f, widthDp("filled") - widthDp("text"), 1f)
        assertEquals("Tonal vs Filled width", widthDp("filled"), widthDp("tonal"), 0.5f)
    }

    // --- Sizes ---

    @Test
    fun `Filled and Tonal are at least 40dp tall and drawn heights are recorded`() {
        renderTagged(
            listOf(
                tagged("filled", ActionButtonEmphasis.Filled),
                tagged("tonal", ActionButtonEmphasis.Tonal),
                tagged("text", ActionButtonEmphasis.Text),
                tagged("small", ActionButtonEmphasis.TextSmall)
            )
        )
        assertTrue("Filled height ${heightDp("filled")}", heightDp("filled") >= 40f)
        assertTrue("Tonal height ${heightDp("tonal")}", heightDp("tonal") >= 40f)
        println(
            "OBSERVED_HEIGHTS_DP density=${density.density} filled=${heightDp("filled")} tonal=${heightDp("tonal")} " +
                "text=${heightDp("text")} small=${heightDp("small")}"
        )
    }

    @Test
    fun `Text and TextSmall touch bounds are at least 48dp in both dimensions`() {
        composeTestRule.setContent {
            density = LocalDensity.current
            YahirAndroidTasteTheme(dynamicColor = false) {
                Column {
                    DynamicActionButton("Plain text", ActionButtonRole.Neutral, onClick = {}, emphasis = ActionButtonEmphasis.Text)
                    DynamicActionButton("Small text", ActionButtonRole.Neutral, onClick = {}, emphasis = ActionButtonEmphasis.TextSmall)
                }
            }
        }
        composeTestRule.waitForIdle()
        val minPx = with(density) { 48.dp.toPx() }
        listOf("Plain text", "Small text").forEach { label ->
            val touch = composeTestRule.onNodeWithText(label).fetchSemanticsNode().touchBoundsInRoot
            assertTrue("$label touch width ${touch.width} >= $minPx", touch.width >= minPx)
            assertTrue("$label touch height ${touch.height} >= $minPx", touch.height >= minPx)
        }
    }

    @Test
    fun `stacked TextSmall buttons reserve at least 48dp of layout so their touch targets do not overlap`() {
        renderTagged(
            listOf(
                tagged("small_a", ActionButtonEmphasis.TextSmall),
                tagged("small_b", ActionButtonEmphasis.TextSmall)
            ),
            label = "Short"
        )
        val topA = composeTestRule.onNodeWithTag("small_a").fetchSemanticsNode().positionInRoot.y
        val topB = composeTestRule.onNodeWithTag("small_b").fetchSemanticsNode().positionInRoot.y
        val minPx = with(density) { 48.dp.toPx() }
        assertTrue("top edges ${topB - topA} apart must be >= $minPx", topB - topA >= minPx)
    }

    // --- Defaulted-call parity ---

    @Test
    fun `the defaulted call and the explicit null emphasis and icon call have identical bounds`() {
        composeTestRule.setContent {
            density = LocalDensity.current
            YahirAndroidTasteTheme(dynamicColor = false) {
                Column {
                    roles.forEach { role ->
                        DynamicActionButton("Same", role, onClick = {}, modifier = Modifier.testTag("default_$role"))
                        DynamicActionButton(
                            "Same",
                            role,
                            onClick = {},
                            modifier = Modifier.testTag("explicit_$role"),
                            emphasis = null,
                            icon = null
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
        roles.forEach { role ->
            val a = composeTestRule.onNodeWithTag("default_$role").fetchSemanticsNode().size
            val b = composeTestRule.onNodeWithTag("explicit_$role").fetchSemanticsNode().size
            assertEquals("$role size", a, b)
        }
    }

    // --- Legacy mapping with an icon (OQ-2) ---

    @Test
    fun `an icon with no emphasis keeps the legacy role mapping`() {
        val check = Icons.Filled.Check
        composeTestRule.setContent {
            density = LocalDensity.current
            YahirAndroidTasteTheme(dynamicColor = false) {
                Column {
                    roles.forEach { role ->
                        DynamicActionButton(WIDE_LABEL, role, onClick = {}, modifier = Modifier.testTag("legacy_$role"), icon = check)
                    }
                    DynamicActionButton(
                        WIDE_LABEL, ActionButtonRole.Save, onClick = {},
                        modifier = Modifier.testTag("filled_ref"), emphasis = ActionButtonEmphasis.Filled, icon = check
                    )
                    DynamicActionButton(
                        WIDE_LABEL, ActionButtonRole.Neutral, onClick = {},
                        modifier = Modifier.testTag("text_ref"), emphasis = ActionButtonEmphasis.Text, icon = check
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
        assertEquals("Save", widthDp("filled_ref"), widthDp("legacy_${ActionButtonRole.Save}"), 0.5f)
        assertEquals("Neutral", widthDp("text_ref"), widthDp("legacy_${ActionButtonRole.Neutral}"), 0.5f)
        assertEquals("Destructive", widthDp("text_ref"), widthDp("legacy_${ActionButtonRole.Destructive}"), 0.5f)
    }

    private companion object {
        const val WIDE_LABEL = "Continue anyway now"
    }
}
