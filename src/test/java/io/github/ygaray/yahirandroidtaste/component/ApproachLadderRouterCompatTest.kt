package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composer
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import io.github.ygaray.yahirandroidtaste.model.ApproachRungUiModel
import java.lang.reflect.Method
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.lang.reflect.Modifier as JModifier

/**
 * Phase 17 (VAPPR-04 / D-03) compatibility pins for the appended router params on
 * [ApproachLadderCard]. This is a NEW sibling of [VoiceBinaryCompatShimTest] on purpose: that file
 * stays byte-identical so the v2.4.1 shim evidence is untouched.
 *
 * 1. The hidden v2.4.1 descriptor (6 value params) must still render through to the current
 *    overload with `router = null` (no router toggle, no pairing exception).
 * 2. The Phase-15 eleven-positional call shape (through `offlineOnlyLabel`) must still bind its
 *    labels in order, with the router params appended last and defaulted.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ApproachLadderRouterCompatTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val ladder = listOf(
        ApproachRungUiModel(id = "cloud", label = "Cloud", rank = 3, offlineCapable = false),
        ApproachRungUiModel(id = "hybrid", label = "Hybrid", rank = 2, offlineCapable = true),
        ApproachRungUiModel(id = "local", label = "Local", rank = 1, offlineCapable = true)
    )

    private fun facadeMethod(facade: String, name: String, vararg v241Params: Class<*>): Method {
        val method = Class.forName("io.github.ygaray.yahirandroidtaste.component.$facade")
            .getMethod(
                name,
                *v241Params,
                Composer::class.java,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType
            )
        assertTrue("$facade.$name v2.4.1 shim must be public", JModifier.isPublic(method.modifiers))
        assertTrue("$facade.$name v2.4.1 shim must be static", JModifier.isStatic(method.modifiers))
        return method
    }

    @Test
    fun `v2_4_1 descriptor defaults router to null and renders no router toggle`() {
        val shim = facadeMethod(
            "ApproachLadderCardKt",
            "ApproachLadderCard",
            List::class.java,
            Boolean::class.javaObjectType,
            Function1::class.java,
            String::class.java,
            Function1::class.java,
            Modifier::class.java
        )
        val noOpOffline: (Boolean) -> Unit = {}

        composeTestRule.setContent {
            Column {
                // v2.4 caller shape: maxTierId, onMaxTierChange, modifier defaulted -> 0b111000.
                shim.invoke(null, ladder, false, noOpOffline, null, null, null, currentComposer, 0, 0b111000)
                // A direct current-overload card with a router makes the router absence through
                // the shim non-vacuous: a router leaking through the shim would double the counts.
                ApproachLadderCard(
                    ladder = listOf(ApproachRungUiModel(id = "nube", label = "Nube", rank = 1)),
                    router = false,
                    onRouterChange = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Cloud").assertExists()
        composeTestRule.onNodeWithText("Hybrid").assertExists()
        composeTestRule.onNodeWithText("Local").assertExists()
        composeTestRule.onAllNodesWithTag("approach_ladder_card_offline_toggle").assertCountEquals(1)
        composeTestRule.onAllNodesWithTag("approach_ladder_card_router_toggle").assertCountEquals(1)
        composeTestRule.onAllNodesWithContentDescription("Router off, selected").assertCountEquals(1)
    }

    @Test
    fun `Phase-15 eleven-positional shape binds labels in order and leaves router hidden`() {
        composeTestRule.setContent {
            ApproachLadderCard(
                ladder,
                true,
                { _: Boolean -> },
                null,
                null,
                Modifier,
                "U-label",
                "C-label",
                "N-label",
                "On-label",
                "Off-label"
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("On-label, not selected").assertExists()
        composeTestRule.onNodeWithContentDescription("Off-label, selected").assertExists()
        composeTestRule.onAllNodesWithText("N-label").assertCountEquals(1)
        composeTestRule.onNodeWithTag("approach_ladder_card_router_toggle").assertDoesNotExist()
    }
}
