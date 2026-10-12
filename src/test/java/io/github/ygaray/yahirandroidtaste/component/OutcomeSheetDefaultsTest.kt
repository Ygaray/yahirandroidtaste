package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.ToneColors
import io.github.ygaray.yahirandroidtaste.theme.ToneDefaults
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests for [OutcomeSheetStyle], [OutcomeSheetColors] and [OutcomeSheetDefaults] (Phase 22,
 * VRCPT-01). One `setContent` per test method (the compose rule allows only one).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OutcomeSheetDefaultsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val warn = ToneColors(Color(0xFFFFE0B2), Color(0xFF5D4037))
    private val error = ToneColors(Color(0xFFFFCDD2), Color(0xFFB71C1C))

    @Test
    fun `OutcomeSheetColors with the same two tones are equal and share a hash code`() {
        val a = OutcomeSheetColors(warn, error)
        val b = OutcomeSheetColors(ToneColors(warn.container, warn.content), ToneColors(error.container, error.content))

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `OutcomeSheetColors differ when either tone differs`() {
        val base = OutcomeSheetColors(warn, error)

        assertNotEquals(base, OutcomeSheetColors(error, error))
        assertNotEquals(base, OutcomeSheetColors(warn, warn))
    }

    @Test
    fun `OutcomeSheetColors is a plain class that publishes no copy and no componentN`() {
        val names = OutcomeSheetColors::class.java.declaredMethods.map { it.name }

        assertFalse(names.any { it == "copy" })
        assertFalse(names.any { it.startsWith("component") })
    }

    @Test
    fun `colors defaults equal the ToneDefaults warn and error tones`() {
        var defaults: OutcomeSheetColors? = null
        var expected: OutcomeSheetColors? = null
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                defaults = OutcomeSheetDefaults.colors()
                expected = OutcomeSheetColors(ToneDefaults.warn(), ToneDefaults.error())
            }
        }
        composeTestRule.waitForIdle()

        assertNotNull(defaults)
        assertEquals(expected, defaults)
    }

    @Test
    fun `colors keeps the default error tone when only a warn tone is passed`() {
        var custom: OutcomeSheetColors? = null
        var defaultError: ToneColors? = null
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                custom = OutcomeSheetDefaults.colors(warnTone = warn)
                defaultError = ToneDefaults.error()
            }
        }
        composeTestRule.waitForIdle()

        assertEquals(warn, custom?.warnTone)
        assertEquals(defaultError, custom?.errorTone)
    }

    @Test
    fun `ReceiptTitleStyle is titleLarge at 18 and 24 sp semibold`() {
        var style: TextStyle? = null
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                style = OutcomeSheetDefaults.ReceiptTitleStyle
            }
        }
        composeTestRule.waitForIdle()

        assertEquals(18.sp, style?.fontSize)
        assertEquals(24.sp, style?.lineHeight)
        assertEquals(FontWeight.SemiBold, style?.fontWeight)
    }
}
