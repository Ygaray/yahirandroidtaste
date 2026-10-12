package io.github.ygaray.yahirandroidtaste.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Tests for the tone tokens: value semantics, active-scheme resolution, contrast, enum order. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ToneTokensTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /** Resolves error() and warn() under each scheme; one composition per test (rule limit). */
    private fun resolveAll(vararg schemes: ColorScheme): List<Pair<ToneColors, ToneColors>> {
        val out = mutableListOf<Pair<ToneColors, ToneColors>>()
        composeTestRule.setContent {
            schemes.forEach { scheme ->
                MaterialTheme(colorScheme = scheme) {
                    out += ToneDefaults.error() to ToneDefaults.warn()
                }
            }
        }
        composeTestRule.waitForIdle()
        return out
    }

    private fun resolve(scheme: ColorScheme): Pair<ToneColors, ToneColors> = resolveAll(scheme).single()

    @Test
    fun `ToneColors compares by value`() {
        val a = ToneColors(Color.Red, Color.Blue)
        val b = ToneColors(Color.Red, Color.Blue)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, ToneColors(Color.Green, Color.Blue))
        assertNotEquals(a, ToneColors(Color.Red, Color.Green))
        assertNotEquals(a, ToneColors(Color.Blue, Color.Red))
        assertFalse(a.equals(null))
        assertFalse(a.equals("not a tone"))
    }

    @Test
    fun `ToneColors toString mentions both colors`() {
        val text = ToneColors(Color.Red, Color.Blue).toString()
        assertTrue(text, text.contains(Color.Red.toString()))
        assertTrue(text, text.contains(Color.Blue.toString()))
    }

    @Test
    fun `ToneColors is not a data class - no copy or componentN members`() {
        val names = ToneColors::class.java.methods.map { it.name }
        assertFalse("copy leaked: $names", names.contains("copy"))
        assertTrue("componentN leaked: $names", names.none { it.startsWith("component") })
    }

    @Test
    fun `error and warn resolve from the light scheme`() {
        val (error, warn) = resolve(LightColorScheme)
        assertEquals(ToneColors(LightColorScheme.errorContainer, LightColorScheme.error), error)
        assertEquals(
            ToneColors(LightColorScheme.tertiaryContainer, LightColorScheme.onTertiaryContainer),
            warn
        )
    }

    @Test
    fun `error and warn resolve from the dark scheme`() {
        val (error, warn) = resolve(DarkColorScheme)
        assertEquals(ToneColors(DarkColorScheme.errorContainer, DarkColorScheme.error), error)
        assertEquals(
            ToneColors(DarkColorScheme.tertiaryContainer, DarkColorScheme.onTertiaryContainer),
            warn
        )
    }

    @Test
    fun `error and warn resolve from a custom active scheme not a hub palette`() {
        val custom = lightColorScheme(
            error = Color(0xFF123456),
            errorContainer = Color(0xFF654321),
            tertiaryContainer = Color(0xFF0A0B0C),
            onTertiaryContainer = Color(0xFFF0E0D0)
        )
        val (error, warn) = resolve(custom)
        assertEquals(ToneColors(Color(0xFF654321), Color(0xFF123456)), error)
        assertEquals(ToneColors(Color(0xFF0A0B0C), Color(0xFFF0E0D0)), warn)
    }

    @Test
    fun `hub tones meet 4_5 contrast on both hub schemes`() {
        val names = listOf("light", "dark")
        resolveAll(LightColorScheme, DarkColorScheme).forEachIndexed { i, (error, warn) ->
            val errorRatio = contrastRatio(error.content, error.container)
            val warnRatio = contrastRatio(warn.content, warn.container)
            assertTrue("error ${names[i]} $errorRatio", errorRatio >= 4.5f)
            assertTrue("warn ${names[i]} $warnRatio", warnRatio >= 4.5f)
        }
    }

    @Test
    fun `ContentTone declares exactly four constants in order`() {
        assertEquals(
            listOf("Default", "Muted", "Warn", "Error"),
            ContentTone.entries.map { it.name }
        )
    }
}
