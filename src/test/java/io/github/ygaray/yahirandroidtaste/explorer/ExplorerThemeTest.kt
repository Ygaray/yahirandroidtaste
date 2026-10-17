package io.github.ygaray.yahirandroidtaste.explorer

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.ToneColors
import io.github.ygaray.yahirandroidtaste.theme.ToneDefaults
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * D-01 [scheme-pin]: [ExplorerTheme] pins every Explorer page to the static hub scheme, so the warn
 * tone is never the device wallpaper tone. Each test reads the hub reference values from
 * `YahirAndroidTasteTheme(dynamicColor = false)` in the same composition and compares them with what
 * the helper resolves under a foreign outer scheme. Scheme level only; the on-device screencap is the
 * Gate-1 check G1-1.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExplorerThemeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val foreignLight = Color(0xFFFFEEFF)
    private val foreignDark = Color(0xFF001100)

    private data class Probe(val surface: Color, val primary: Color, val warn: ToneColors)

    @androidx.compose.runtime.Composable
    private fun probe(): Probe = Probe(
        surface = MaterialTheme.colorScheme.surface,
        primary = MaterialTheme.colorScheme.primary,
        warn = ToneDefaults.warn()
    )

    @Test
    fun lightMode_resolvesTheHubLightScheme_underAForeignOuterScheme() {
        var hub: Probe? = null
        var pinned: Probe? = null
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) { hub = probe() }
            MaterialTheme(
                colorScheme = lightColorScheme(surface = foreignLight, primary = foreignLight)
            ) {
                ExplorerTheme(ThemeMode.LIGHT) { pinned = probe() }
            }
        }
        composeTestRule.waitForIdle()

        assertEquals(hub!!.surface, pinned!!.surface)
        assertEquals(hub!!.primary, pinned!!.primary)
        assertNotEquals(foreignLight, pinned!!.surface)
        assertNotEquals(foreignLight, pinned!!.primary)
    }

    @Test
    fun darkMode_resolvesTheHubDarkScheme_underAForeignOuterScheme() {
        var hubLight: Probe? = null
        var hubDark: Probe? = null
        var pinned: Probe? = null
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                hubLight = probe()
            }
            YahirAndroidTasteTheme(themeMode = ThemeMode.DARK, dynamicColor = false) {
                hubDark = probe()
            }
            MaterialTheme(
                colorScheme = darkColorScheme(surface = foreignDark, primary = foreignDark)
            ) {
                ExplorerTheme(ThemeMode.DARK) { pinned = probe() }
            }
        }
        composeTestRule.waitForIdle()

        assertNotEquals("the hub light and dark schemes differ", hubLight!!.surface, hubDark!!.surface)
        assertEquals(hubDark!!.surface, pinned!!.surface)
        assertEquals(hubDark!!.primary, pinned!!.primary)
        assertNotEquals(foreignDark, pinned!!.surface)
        assertNotEquals(foreignDark, pinned!!.primary)
    }

    @Test
    fun warnTone_isTheHubAmber_notTheWallpaperTone() {
        var hubWarn: ToneColors? = null
        var pinnedWarn: ToneColors? = null
        val wallpaperTone = Color(0xFF7A3E9D)
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                hubWarn = ToneDefaults.warn()
            }
            MaterialTheme(
                colorScheme = lightColorScheme(
                    tertiaryContainer = wallpaperTone,
                    onTertiaryContainer = wallpaperTone
                )
            ) {
                ExplorerTheme(ThemeMode.LIGHT) { pinnedWarn = ToneDefaults.warn() }
            }
        }
        composeTestRule.waitForIdle()

        assertEquals(hubWarn, pinnedWarn)
        assertNotEquals(wallpaperTone, pinnedWarn!!.container)
    }

    @Test
    @Config(qualifiers = "night")
    fun systemMode_followsTheSystemDarkSetting_withTheStaticHubScheme() {
        assertSystemModeMatches(ThemeMode.DARK)
    }

    @Test
    @Config(qualifiers = "notnight")
    fun systemMode_followsTheSystemDarkSetting_withTheStaticHubScheme_whenNotNight() {
        assertSystemModeMatches(ThemeMode.LIGHT)
    }

    private fun assertSystemModeMatches(expected: ThemeMode) {
        var hub: Probe? = null
        var pinned: Probe? = null
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = expected, dynamicColor = false) { hub = probe() }
            ExplorerTheme(ThemeMode.SYSTEM) { pinned = probe() }
        }
        composeTestRule.waitForIdle()

        assertEquals(hub!!.surface, pinned!!.surface)
        assertEquals(hub!!.primary, pinned!!.primary)
        assertEquals(hub!!.warn, pinned!!.warn)
    }
}
