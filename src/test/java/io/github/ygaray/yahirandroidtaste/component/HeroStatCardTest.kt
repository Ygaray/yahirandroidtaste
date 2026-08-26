package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Compose tests for [HeroStatCard] (Phase 44-02, DS-03).
 *
 * Infra mirrors this module's established Robolectric+Compose harness ([CountBadgeTest]), with
 * the callback-capture-via-mutable-var tap-interaction pattern from [AppChipTest]:
 * `@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [35])`, `createComposeRule()`.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class HeroStatCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `renders both label and value text`() {
        composeTestRule.setContent {
            HeroStatCard(label = "Total", value = "1,204")
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Total").assertExists()
        composeTestRule.onNodeWithText("1,204").assertExists()
    }

    @Test
    fun `tapping the card with a non-null onClick invokes it exactly once`() {
        var callCount = 0

        composeTestRule.setContent {
            HeroStatCard(label = "Total", value = "1,204", onClick = { callCount++ })
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("1,204").performClick()
        composeTestRule.waitForIdle()

        assertEquals(
            "Tapping a HeroStatCard with a non-null onClick must invoke it exactly once",
            1,
            callCount
        )
    }

    @Test
    fun `onClick null composes with no clickable semantics node and does not throw`() {
        composeTestRule.setContent {
            HeroStatCard(label = "Total", value = "1,204", onClick = null)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Total").assertExists()
        composeTestRule.onNodeWithText("Total").assertHasNoClickAction()
    }

    @Test
    fun `empty label and value compose without throwing`() {
        composeTestRule.setContent {
            HeroStatCard(label = "", value = "")
        }
        composeTestRule.waitForIdle()

        composeTestRule.onRoot().assertExists()
    }

    // --- CR-01 regression coverage: accent-stripe geometry (44-REVIEW.md) ---
    //
    // Modifier.fillMaxHeight() on the accent Box only resolves correctly if the parent Row
    // measures its own height via IntrinsicSize.Min. Without that, the stripe either collapses to
    // 0dp (unbounded-height parent, e.g. a scrollable column) or overshoots the Surface's bounds
    // (bounded-height parent). These tests assert the accent stripe's measured height always
    // equals the card Surface's measured height, in both parent-height regimes.

    @Test
    fun `accent stripe height matches surface height in an unbounded-height parent`() {
        composeTestRule.setContent {
            Box(modifier = Modifier.verticalScroll(rememberScrollState())) {
                HeroStatCard(label = "Total", value = "1,204", modifier = Modifier.fillMaxWidth())
            }
        }
        composeTestRule.waitForIdle()

        val stripeHeight =
            composeTestRule.onNodeWithTag("hero_stat_card_accent_stripe").fetchSemanticsNode().size.height
        val surfaceHeight =
            composeTestRule.onNodeWithTag("hero_stat_card_surface").fetchSemanticsNode().size.height

        assertTrue(
            "Accent stripe must have non-zero height in an unbounded-height parent, was $stripeHeight",
            stripeHeight > 0
        )
        assertEquals(
            "Accent stripe height must match the card Surface's height in an unbounded-height parent",
            surfaceHeight,
            stripeHeight
        )
    }

    @Test
    fun `accent stripe height matches surface height in a bounded-height parent`() {
        composeTestRule.setContent {
            Box(modifier = Modifier.height(200.dp)) {
                HeroStatCard(label = "Total", value = "1,204", modifier = Modifier.fillMaxWidth())
            }
        }
        composeTestRule.waitForIdle()

        val stripeHeight =
            composeTestRule.onNodeWithTag("hero_stat_card_accent_stripe").fetchSemanticsNode().size.height
        val surfaceHeight =
            composeTestRule.onNodeWithTag("hero_stat_card_surface").fetchSemanticsNode().size.height

        assertTrue(
            "Accent stripe must have non-zero height in a bounded-height parent, was $stripeHeight",
            stripeHeight > 0
        )
        assertEquals(
            "Accent stripe height must match the card Surface's height in a bounded-height parent " +
                "(it must not overshoot the Surface's content-driven bounds)",
            surfaceHeight,
            stripeHeight
        )
    }
}
