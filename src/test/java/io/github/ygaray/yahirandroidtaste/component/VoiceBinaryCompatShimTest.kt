package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composer
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.ygaray.yahirandroidtaste.model.ApproachRungUiModel
import java.lang.reflect.Method
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.lang.reflect.Modifier as JModifier

/**
 * Binary-level evidence for INC-2026-10-05-02 F1: each hidden v2.4.1 overload's exact JVM
 * descriptor exists on its `*Kt` file facade and renders through to the current overload.
 *
 * A consumer compiled against v2.4.x calls `XKt.X(<v2.4.1 params>, Composer, int $changed,
 * int $default)` by exact descriptor. These tests resolve that method reflectively (Kotlin source
 * cannot name a `*Kt` facade nor call a `DeprecationLevel.HIDDEN` member), then invoke it inside a
 * composition with a v2.4-style `$default` mask -- passing `null` for every defaulted slot, which is
 * exactly what a v2.4-compiled caller that omits trailing defaults executes.
 *
 * Each test also renders the CURRENT overload, every parameter named with custom (Spanish) labels,
 * in the same composition: this pins that the shim delegates into the current overload with the
 * English defaults (an English label exists exactly once -- from the shim path only).
 *
 * Deliberately no overload-count assertions: Phase 17 (F2) may add further shims.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class VoiceBinaryCompatShimTest {

    @get:Rule
    val composeTestRule = createComposeRule()

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
    fun approachLadderCard_v241Descriptor_rendersThroughWithEnglishDefaults() {
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
        val ladder = listOf(
            ApproachRungUiModel(id = "cloud", label = "Cloud", rank = 3, offlineCapable = false),
            ApproachRungUiModel(id = "hybrid", label = "Hybrid", rank = 2, offlineCapable = true),
            ApproachRungUiModel(id = "local", label = "Local", rank = 1, offlineCapable = true)
        )
        var recorded: Boolean? = null
        val onOfflineOnlyChange: (Boolean) -> Unit = { recorded = it }

        composeTestRule.setContent {
            Column {
                // v2.4 caller shape: ladder, offlineOnly, onOfflineOnlyChange given; maxTierId,
                // onMaxTierChange, modifier defaulted -> $default bits 3-5 = 0b111000.
                shim.invoke(
                    null,
                    ladder,
                    false,
                    onOfflineOnlyChange,
                    null,
                    null,
                    null,
                    currentComposer,
                    0,
                    0b111000
                )
                ApproachLadderCard(
                    ladder = listOf(ApproachRungUiModel(id = "nube", label = "Nube", rank = 1)),
                    offlineOnly = false,
                    onOfflineOnlyChange = {},
                    maxTierId = null,
                    onMaxTierChange = null,
                    modifier = Modifier,
                    unavailableLabel = "No disponible",
                    cappedLabel = "Limitado",
                    needsNetworkLabel = "Requiere red",
                    onlineLabel = "En línea",
                    offlineOnlyLabel = "Solo sin conexión"
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Cloud").assertExists()
        composeTestRule.onNodeWithText("Hybrid").assertExists()
        composeTestRule.onNodeWithText("Local").assertExists()
        composeTestRule.onNodeWithText("Nube").assertExists()
        composeTestRule.onNodeWithContentDescription("Solo sin conexión, not selected").assertExists()
        composeTestRule.onAllNodesWithContentDescription("Offline only, not selected")
            .assertCountEquals(1)

        composeTestRule.onNodeWithContentDescription("Offline only, not selected").performClick()
        composeTestRule.waitForIdle()

        assertEquals(true, recorded)
    }
}
