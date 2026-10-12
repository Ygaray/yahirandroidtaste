package io.github.ygaray.yahirandroidtaste.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * WCAG contrast ratio of two opaque colors: (lighter + 0.05) / (darker + 0.05), from each color's
 * relative luminance. Shared by the tone token and atom tests.
 */
internal fun contrastRatio(a: Color, b: Color): Float {
    val la = a.luminance()
    val lb = b.luminance()
    val lighter = maxOf(la, lb)
    val darker = minOf(la, lb)
    return (lighter + 0.05f) / (darker + 0.05f)
}
