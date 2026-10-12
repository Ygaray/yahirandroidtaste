package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Assert.assertEquals
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
 * description so one addressing scheme works for both.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SegmentedOptionSelectorPillTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setSelector(
        options: List<String>,
        selectedIndex: Int,
        style: SegmentedOptionStyle = SegmentedOptionStyle.Pill,
        disabledOptions: Set<Int> = emptySet(),
        enabled: Boolean = true,
        disabledReason: String? = null,
        onSelect: (Int) -> Unit = {}
    ) {
        composeTestRule.setContent {
            YahirAndroidTasteTheme {
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
        composeTestRule.waitForIdle()
    }

    private fun segment(text: String, selected: Boolean) = composeTestRule.onNodeWithContentDescription(
        "$text, ${if (selected) "selected" else "not selected"}"
    )

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
}
