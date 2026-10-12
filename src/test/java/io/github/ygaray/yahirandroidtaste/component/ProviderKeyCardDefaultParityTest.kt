package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import io.github.ygaray.yahirandroidtaste.model.KeyFieldState
import io.github.ygaray.yahirandroidtaste.model.ProviderOptionUiModel
import io.github.ygaray.yahirandroidtaste.testsupport.SemanticsDump
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Wave 0 characterization of the DEFAULT [ProviderKeyCard] (empty saved keys, no actions), captured
 * from the unmodified v2.5.1 card BEFORE any Phase 23 edit and never edited afterwards. It pins the
 * normalized unmerged semantics tree of five states plus the tagged-node bounds, so the widening
 * (VKEY-01) is proven to leave a default call exactly as it was without any pixel capture.
 *
 * Later tests reuse [ProviderKeyCardGoldens] by import instead of copying the constants.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp")
class ProviderKeyCardDefaultParityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val providers = ProviderKeyCardGoldens.providers

    private fun assertGolden(name: String, golden: String, actual: String) {
        val paste = actual.lines().joinToString("\n") { "|$it" }
        assertEquals("$name drifted. Actual dump, ready to paste:\n$paste\n", golden, actual)
    }

    private fun assertNoLifecycleNodes(name: String, dump: String) {
        assertFalse("$name: no provider_key_saved* tag allowed", dump.contains("provider_key_saved"))
        assertFalse("$name: no provider_key_action_* tag allowed", dump.contains("provider_key_action_"))
        val forbidden = setOf("Save", "Update", "Clear", "Cancel", "Saved")
        val texts = Regex("""(?<![A-Za-z])Text=\[([^\]]*)]""").findAll(dump)
            .flatMap { it.groupValues[1].split("|") }.toSet()
        assertEquals("$name: no lifecycle text allowed", emptySet<String>(), texts.intersect(forbidden))
    }

    @Test
    fun `S1 two providers anthropic selected with an empty key`() {
        composeTestRule.setContent {
            ProviderKeyCard(
                providers = providers,
                selectedProviderId = "anthropic",
                onProviderSelected = {},
                keyValue = "",
                onKeyChange = {},
                keyState = KeyFieldState.Empty,
                keyLabel = "API key"
            )
        }
        composeTestRule.waitForIdle()
        val dump = SemanticsDump.allRoots(composeTestRule)
        assertNoLifecycleNodes("S1", dump)
        assertGolden("S1", ProviderKeyCardGoldens.S1, dump)
    }

    @Test
    fun `S2 openai selected with an entered masked key`() {
        composeTestRule.setContent {
            ProviderKeyCard(
                providers = providers,
                selectedProviderId = "openai",
                onProviderSelected = {},
                keyValue = ProviderKeyCardGoldens.ENTERED_KEY,
                onKeyChange = {},
                keyState = KeyFieldState.Entered,
                keyLabel = "API key"
            )
        }
        composeTestRule.waitForIdle()
        val dump = SemanticsDump.allRoots(composeTestRule)
        assertNoLifecycleNodes("S2", dump)
        assertGolden("S2", ProviderKeyCardGoldens.S2, dump)
    }

    @Test
    fun `S3 openai selected with an invalid key state`() {
        composeTestRule.setContent {
            ProviderKeyCard(
                providers = providers,
                selectedProviderId = "openai",
                onProviderSelected = {},
                keyValue = ProviderKeyCardGoldens.ENTERED_KEY,
                onKeyChange = {},
                keyState = KeyFieldState.Invalid("Key rejected by provider"),
                keyLabel = "API key"
            )
        }
        composeTestRule.waitForIdle()
        val dump = SemanticsDump.allRoots(composeTestRule)
        assertNoLifecycleNodes("S3", dump)
        assertGolden("S3", ProviderKeyCardGoldens.S3, dump)
    }

    @Test
    fun `S4 empty providers list renders the default reason caption`() {
        composeTestRule.setContent {
            ProviderKeyCard(
                providers = emptyList(),
                selectedProviderId = null,
                onProviderSelected = {},
                keyValue = "",
                onKeyChange = {},
                keyState = KeyFieldState.Empty,
                keyLabel = "API key"
            )
        }
        composeTestRule.waitForIdle()
        val dump = SemanticsDump.allRoots(composeTestRule)
        assertNoLifecycleNodes("S4", dump)
        assertGolden("S4", ProviderKeyCardGoldens.S4, dump)
    }

    @Test
    fun `S5 the S1 card with the dropdown opened lists both menu rows`() {
        composeTestRule.setContent {
            ProviderKeyCard(
                providers = providers,
                selectedProviderId = "anthropic",
                onProviderSelected = {},
                keyValue = "",
                onKeyChange = {},
                keyState = KeyFieldState.Empty,
                keyLabel = "API key"
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("provider_key_card_dropdown").performClick()
        composeTestRule.waitForIdle()
        val dump = SemanticsDump.allRoots(composeTestRule)
        assertNoLifecycleNodes("S5", dump)
        assertGolden("S5", ProviderKeyCardGoldens.S5, dump)
    }

    @Test
    fun `tagged node bounds are pinned for S1 and S2`() {
        var selected by mutableStateOf("anthropic")
        var key by mutableStateOf("")
        var state by mutableStateOf<KeyFieldState>(KeyFieldState.Empty)
        composeTestRule.setContent {
            ProviderKeyCard(
                providers = providers,
                selectedProviderId = selected,
                onProviderSelected = {},
                keyValue = key,
                onKeyChange = {},
                keyState = state,
                keyLabel = "API key"
            )
        }
        composeTestRule.waitForIdle()
        val tags = listOf("provider_key_card_surface", "provider_key_card_dropdown", "provider_key_card_key_field")
        val s1 = tags.joinToString("\n") { SemanticsDump.boundsLine(composeTestRule, it) }

        composeTestRule.runOnIdle {
            selected = "openai"
            key = ProviderKeyCardGoldens.ENTERED_KEY
            state = KeyFieldState.Entered
        }
        composeTestRule.waitForIdle()
        val s2 = tags.joinToString("\n") { SemanticsDump.boundsLine(composeTestRule, it) }

        assertGolden("S1 bounds", ProviderKeyCardGoldens.S1_BOUNDS, s1)
        assertGolden("S2 bounds", ProviderKeyCardGoldens.S2_BOUNDS, s2)
    }
}

/** Goldens captured from the unmodified v2.5.1 card (Wave 0). Reused by later tests, never edited. */
internal object ProviderKeyCardGoldens {
    const val ENTERED_KEY = "fixture-key-value-123"

    val providers = listOf(
        ProviderOptionUiModel(id = "openai", label = "OpenAI"),
        ProviderOptionUiModel(id = "anthropic", label = "Anthropic")
    )

    val S1 = """
|node
|  node TestTag=provider_key_card_surface
|    node TestTag=provider_key_card_dropdown
|      node EditableText=[Anthropic] Role=DropdownList
|        node
|        node Text=[Provider]
|        node
|    node EditableText=[] Password=kotlin.Unit TestTag=provider_key_card_key_field
|      node
|      node Text=[API key]
|      node
|      node Role=Button
|        node ContentDescription=[Show key] Role=Image
""".trimMargin()
    val S2 = """
|node
|  node TestTag=provider_key_card_surface
|    node TestTag=provider_key_card_dropdown
|      node EditableText=[OpenAI] Role=DropdownList
|        node
|        node Text=[Provider]
|        node
|    node EditableText=[•••••••••••••••••••••] Password=kotlin.Unit TestTag=provider_key_card_key_field
|      node
|      node Text=[API key]
|      node
|      node Role=Button
|        node ContentDescription=[Show key] Role=Image
|      node Role=Button
|        node ContentDescription=[Clear text] Role=Image
""".trimMargin()
    val S3 = """
|node
|  node TestTag=provider_key_card_surface
|    node TestTag=provider_key_card_dropdown
|      node EditableText=[OpenAI] Role=DropdownList
|        node
|        node Text=[Provider]
|        node
|    node EditableText=[•••••••••••••••••••••] Error=Invalid input Password=kotlin.Unit TestTag=provider_key_card_key_field
|      node
|      node Text=[API key]
|      node
|      node Role=Button
|        node ContentDescription=[Show key] Role=Image
|      node Role=Button
|        node ContentDescription=[Clear text] Role=Image
|      node Text=[Key rejected by provider]
""".trimMargin()
    val S4 = """
|node
|  node TestTag=provider_key_card_surface
|    node TestTag=provider_key_card_dropdown Text=[No providers configured yet]
|    node EditableText=[] Password=kotlin.Unit TestTag=provider_key_card_key_field
|      node
|      node Text=[API key]
|      node
|      node Role=Button
|        node ContentDescription=[Show key] Role=Image
""".trimMargin()
    val S5 = """
|node
|  node TestTag=provider_key_card_surface
|    node TestTag=provider_key_card_dropdown
|      node EditableText=[Anthropic] Role=DropdownList
|        node
|        node Text=[Provider]
|        node
|        node
|    node EditableText=[] Password=kotlin.Unit TestTag=provider_key_card_key_field
|      node
|      node Text=[API key]
|      node
|      node Role=Button
|        node ContentDescription=[Show key] Role=Image
|---- root ----
|node
|  node
|    node
|      node
|        node
|          node Text=[OpenAI]
|        node
|          node Text=[Anthropic]
""".trimMargin()
    val S1_BOUNDS = """
|provider_key_card_surface x=0 y=0 w=360 h=189
|provider_key_card_dropdown x=16 y=16 w=328 h=77
|provider_key_card_key_field x=16 y=97 w=328 h=76
""".trimMargin()
    val S2_BOUNDS = """
|provider_key_card_surface x=0 y=0 w=360 h=190
|provider_key_card_dropdown x=16 y=16 w=328 h=77
|provider_key_card_key_field x=16 y=97 w=328 h=77
""".trimMargin()
}
