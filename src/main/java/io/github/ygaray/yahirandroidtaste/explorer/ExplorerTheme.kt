package io.github.ygaray.yahirandroidtaste.explorer

import androidx.compose.runtime.Composable
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme

/**
 * The one theme wrap every Explorer page uses: the hub's static palette for [themeMode], never
 * dynamic color.
 *
 * Under dynamic color the warn tone (`tertiary` family) became the device wallpaper tone on every
 * page that lacked `HubSchemeScope`, so the gallery no longer showed the amber the owner reviews.
 * Every Explorer wrap (each screen, the Activity and the not-found fallback) MUST go through this
 * helper; `ExplorerSchemePinGuardTest` fails the build when a file wraps the library theme directly.
 */
@Composable
internal fun ExplorerTheme(themeMode: ThemeMode, content: @Composable () -> Unit) {
    YahirAndroidTasteTheme(themeMode = themeMode, dynamicColor = false, content = content)
}
