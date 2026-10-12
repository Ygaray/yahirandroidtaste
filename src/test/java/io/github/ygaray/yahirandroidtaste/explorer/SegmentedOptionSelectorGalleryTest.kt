package io.github.ygaray.yahirandroidtaste.explorer

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Gallery coverage for the Phase 21 `SegmentedOptionSelector` additions (VSEG-01): the Pill and
 * disabled-option demos are present, interactive (they drive real state) and keep the registry
 * entry's tier, family and canonical state labels.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SegmentedOptionSelectorGalleryTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun entry(): ComponentRegistry.Entry =
        ComponentRegistry.entries.first { it.name == "SegmentedOptionSelector" }

    private fun renderVariants() {
        val content = checkNotNull(entry().content)
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                Column { content() }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun segment(text: String, selected: Boolean) = composeTestRule.onNodeWithContentDescription(
        "$text, ${if (selected) "selected" else "not selected"}"
    )

    @Test
    fun `variants show the original demo and the four new section labels`() {
        renderVariants()

        listOf(
            "SegmentedOptionSelector",
            "SegmentedOptionSelector — Pill style",
            "SegmentedOptionSelector — Pill, first option disabled",
            "SegmentedOptionSelector — Material, second option disabled",
            "SegmentedOptionSelector — Pill, long German labels (truncate)"
        ).forEach { composeTestRule.onNodeWithText(it).assertExists() }
        segment("Option A", selected = true).assertExists()
    }

    @Test
    fun `the pill demo drives real state on click`() {
        renderVariants()

        segment("Pill two", selected = false).assertExists().performClick()
        composeTestRule.waitForIdle()

        segment("Pill two", selected = true).assertExists()
        segment("Pill one", selected = false).assertExists()
    }

    @Test
    fun `the pill demo with a disabled first option exposes it as not enabled and ignores clicks`() {
        renderVariants()

        segment("Alpha", selected = false).assertIsNotEnabled().performClick()
        composeTestRule.waitForIdle()

        segment("Alpha", selected = false).assertExists()
        segment("Beta", selected = true).assertIsEnabled()
    }

    @Test
    fun `the material demo with a disabled second option exposes it as not enabled`() {
        renderVariants()

        segment("Delta", selected = false).assertIsNotEnabled()
        segment("Gamma", selected = true).assertIsEnabled()
    }

    @Test
    fun `the registry entry keeps its tier family and canonical state labels`() {
        val entry = entry()

        assertEquals(ComponentRegistry.Tier.PRIMITIVE, entry.tier)
        assertEquals(ExplorerFamilies.PICKERS, entry.family)
        assertEquals(listOf("Default", "Pressed / Selected", "Disabled", "Focused"), entry.states.map { it.label })
        assertNotNull(entry.content)
    }
}
