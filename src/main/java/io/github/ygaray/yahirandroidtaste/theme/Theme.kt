package io.github.ygaray.yahirandroidtaste.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

val LightColorScheme = lightColorScheme(
    primary                  = TealPrimary,
    onPrimary                = TealOnPrimary,
    primaryContainer         = TealPrimaryContainer,
    onPrimaryContainer       = TealOnPrimaryContainer,
    secondary                = SlateSecondary,
    onSecondary              = SlateOnSecondary,
    secondaryContainer       = SlateSecondaryContainer,
    onSecondaryContainer     = SlateOnSecondaryContainer,
    tertiary                 = AmberTertiary,
    onTertiary               = AmberOnTertiary,
    tertiaryContainer        = AmberTertiaryContainer,
    onTertiaryContainer      = AmberOnTertiaryContainer,
    error                    = ErrorRed,
    onError                  = OnErrorRed,
    errorContainer           = ErrorRedContainer,
    onErrorContainer         = OnErrorRedContainer,
    background               = NeutralBgLight,
    onBackground             = NeutralOnBgLight,
    surface                  = NeutralBgLight,
    onSurface                = NeutralOnBgLight,
    surfaceVariant           = NeutralSurfaceVarLight,
    onSurfaceVariant         = NeutralOnSurfaceVarLight,
    outline                  = OutlineLight,
    outlineVariant           = OutlineVarLight,
    scrim                    = ScrimColor,
    inverseSurface           = InverseSurfaceLight,
    inverseOnSurface         = InverseOnSurfaceLight,
    inversePrimary           = InversePrimaryLight,
    surfaceDim               = SurfaceDimLight,
    surfaceBright            = SurfaceBrightLight,
    surfaceContainerLowest   = SurfaceContainerLowestLight,
    surfaceContainerLow      = SurfaceContainerLowLight,
    surfaceContainer         = SurfaceContainerLight,
    surfaceContainerHigh     = SurfaceContainerHighLight,
    surfaceContainerHighest  = SurfaceContainerHighestLight,
)

val DarkColorScheme = darkColorScheme(
    primary                  = TealPrimaryDark,
    onPrimary                = TealOnPrimaryDark,
    primaryContainer         = TealPrimaryContainerDark,
    onPrimaryContainer       = TealOnPrimaryContainerDark,
    secondary                = SlateSecondaryDark,
    onSecondary              = SlateOnSecondaryDark,
    secondaryContainer       = SlateSecondaryContainerDark,
    onSecondaryContainer     = SlateOnSecondaryContainerDark,
    tertiary                 = AmberTertiaryDark,
    onTertiary               = AmberOnTertiaryDark,
    tertiaryContainer        = AmberTertiaryContainerDark,
    onTertiaryContainer      = AmberOnTertiaryContainerDark,
    error                    = ErrorRedDark,
    onError                  = OnErrorRedDark,
    errorContainer           = ErrorRedContainerDark,
    onErrorContainer         = OnErrorRedContainerDark,
    background               = InkBackground,
    onBackground             = InkOnBackground,
    surface                  = InkSurface,
    onSurface                = InkOnSurface,
    surfaceVariant           = NeutralSurfaceVarDark,
    onSurfaceVariant         = NeutralOnSurfaceVarDark,
    outline                  = OutlineDark,
    outlineVariant           = OutlineVarDark,
    scrim                    = ScrimColor,
    inverseSurface           = InverseSurfaceDark,
    inverseOnSurface         = InverseOnSurfaceDark,
    inversePrimary           = InversePrimaryDark,
    surfaceDim               = SurfaceDimDark,
    surfaceBright            = SurfaceBrightDark,
    surfaceContainerLowest   = SurfaceContainerLowestDark,
    surfaceContainerLow      = SurfaceContainerLowDark,
    surfaceContainer         = SurfaceContainerDark,
    surfaceContainerHigh     = SurfaceContainerHighDark,
    surfaceContainerHighest  = SurfaceContainerHighestDark,
)

@Composable
fun YahirAndroidTasteTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = when {
        dynamicColor -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
