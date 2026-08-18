package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Returns Color.Black or Color.White depending on which provides better contrast
 * against [background]. Uses W3C relative luminance threshold of 0.5.
 */
fun contrastingForeground(background: Color): Color =
    if (background.luminance() > 0.5f) Color.Black else Color.White
