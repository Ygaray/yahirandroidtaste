package io.github.ygaray.yahirandroidtaste.explorer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Drives the hoisted `sheetState` demo of the `OutcomeSheet` registry entry (v2.7.0, fixed in v2.7.1)
 * through its explorer-only trigger tag (DEMO-02): Show, then Log it or Cancel, slides the sheet down
 * through `hide()`. The sheet is still composed shortly after the click, which proves the demo clears
 * its slot after the animation and not at once, and it is gone once the slide finished. The sheet can
 * be shown again. The trigger tag is asserted as a literal. NATIVE graphics; no pixel capture.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h3000dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HoistedSheetDemoRegistryRenderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val trigger = "demo_hoisted_sheet_show"
    private val confirm = "outcome_sheet_confirmation_confirm"
    private val cancel = "outcome_sheet_confirmation_cancel"
    private val title = "outcome_sheet_title"

    private fun render() {
        val content = checkNotNull(ComponentRegistry.entries.first { it.name == "OutcomeSheet" }.content)
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) { content() }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun count(tag: String): Int =
        composeTestRule.onAllNodes(hasTestTag(tag), useUnmergedTree = true).fetchSemanticsNodes().size

    private fun show() {
        composeTestRule.onNode(hasTestTag(trigger)).performScrollTo().performClick()
        composeTestRule.waitForIdle()
    }

    private fun assertShown() {
        assertEquals("the confirm sheet is shown", 1, count(confirm))
        assertEquals("with its title", 1, count(title))
    }

    private fun assertGone() {
        assertEquals("the confirm button is gone", 0, count(confirm))
        assertEquals("the title is gone", 0, count(title))
    }

    /** Clicks [button] on a stopped clock, checks the sheet is mid-slide, then lets the slide finish. */
    private fun clickAndSlide(button: String) {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.onNode(hasTestTag(button), useUnmergedTree = true).performClick()
        composeTestRule.mainClock.advanceTimeBy(MID_SLIDE_MS)
        assertEquals("still composed mid-slide", 1, count(confirm))
        composeTestRule.mainClock.advanceTimeBy(SLIDE_BUDGET_MS)
        composeTestRule.mainClock.autoAdvance = true
        composeTestRule.waitForIdle()
    }

    @Test
    fun triggerTagExistsOnce() {
        render()
        assertEquals(1, count(trigger))
    }

    @Test
    fun showThenConfirm_slidesTheSheetDownThroughHide() {
        render()
        show()
        assertShown()

        clickAndSlide(confirm)

        assertGone()
    }

    @Test
    fun showThenCancel_slidesTheSheetDownThroughHide() {
        render()
        show()
        assertShown()

        clickAndSlide(cancel)

        assertGone()
    }

    @Test
    fun sheetIsStillComposedMidSlide_thenGone() {
        render()
        show()

        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.onNode(hasTestTag(confirm), useUnmergedTree = true).performClick()
        composeTestRule.mainClock.advanceTimeBy(MID_SLIDE_MS)
        assertEquals("the confirm button still exists shortly after the click", 1, count(confirm))
        composeTestRule.mainClock.advanceTimeBy(SLIDE_BUDGET_MS)
        composeTestRule.mainClock.autoAdvance = true
        composeTestRule.waitForIdle()

        assertGone()
    }

    @Test
    fun canBeShownAgainAfterHide() {
        render()
        show()
        clickAndSlide(confirm)
        assertGone()

        show()

        assertShown()
    }

    private companion object {
        const val MID_SLIDE_MS = 50L
        const val SLIDE_BUDGET_MS = 2_000L
    }
}
