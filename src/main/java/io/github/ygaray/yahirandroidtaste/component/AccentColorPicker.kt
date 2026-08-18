package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

data class AccentColor(val light: Long, val dark: Long)

val ACCENT_COLORS: List<AccentColor> = listOf(
    AccentColor(0xFFB3261E, 0xFFFFB4AB),   // Red
    AccentColor(0xFF904D4D, 0xFFFFB4B4),   // Pink
    AccentColor(0xFF6750A4, 0xFFD0BCFF),   // Purple (default)
    AccentColor(0xFF4A4074, 0xFFCBC2FF),   // Deep Purple
    AccentColor(0xFF3A5BA0, 0xFFADC6FF),   // Indigo
    AccentColor(0xFF1A6FB8, 0xFFB3D9FF),   // Blue
    AccentColor(0xFF006A6A, 0xFF4DD9D9),   // Teal
    AccentColor(0xFF386A20, 0xFF9DD47A),   // Green
    AccentColor(0xFF516600, 0xFFCBEA65),   // Lime
    AccentColor(0xFF695E00, 0xFFE9C50D),   // Yellow
    AccentColor(0xFF8B4000, 0xFFFFB787),   // Orange
    AccentColor(0xFF6D4A26, 0xFFE7BFA0),   // Brown
    AccentColor(0xFFD84315, 0xFFFFAB91),   // Coral
    AccentColor(0xFFC2185B, 0xFFF48FB1),   // Rose
    AccentColor(0xFF7E57C2, 0xFFD1C4E9),   // Lavender
    AccentColor(0xFF00838F, 0xFF80DEEA),   // Cyan
    AccentColor(0xFF2E7D32, 0xFFA5D6A7),   // Mint
    AccentColor(0xFF546E7A, 0xFFB0BEC5),   // Slate
    AccentColor(0xFF37474F, 0xFF90A4AE),   // Charcoal
    AccentColor(0xFFBF8C00, 0xFFFFD54F),   // Gold
    // --- DASH-03 expansion (append-only below this line; existing 20 entries above are frozen) ---
    AccentColor(0xFF006A60, 0xFF52DBC9),   // Turquoise
    AccentColor(0xFF825500, 0xFFFFB951),   // Amber
    AccentColor(0xFF8E4585, 0xFFECB6E4),   // Plum
    AccentColor(0xFF2B4C7E, 0xFFAEC6FF),   // Navy
    AccentColor(0xFF57662D, 0xFFC0D284),   // Olive
    AccentColor(0xFF7B2D26, 0xFFFFB4A6),   // Maroon
    AccentColor(0xFF0077B6, 0xFF97D3FF),   // Sky
    AccentColor(0xFF1B7F4C, 0xFF8FE6B4),   // Emerald
    AccentColor(0xFFB5504A, 0xFFFFB3AC),   // Salmon
    AccentColor(0xFF5B4B8A, 0xFFC9BEEF),   // Violet
    AccentColor(0xFFAD6247, 0xFFFFC0A3),   // Peach
    AccentColor(0xFF3E5C76, 0xFFA9C6DF),   // Steel
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccentColorPicker(
    selectedColor: Long,
    onColorSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
    showIndices: Boolean = false
) {
    val isDark = isSystemInDarkTheme()
    Box(
        modifier = modifier
            .heightIn(max = 160.dp)
            .verticalScroll(rememberScrollState())
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ACCENT_COLORS.forEachIndexed { index, accentColor ->
                val colorValue = if (isDark) accentColor.dark else accentColor.light
                Box {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(colorValue))
                            .clickable { onColorSelected(colorValue) }
                            .testTag("accent_color_swatch_$index")
                    ) {
                        if (colorValue == selectedColor) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = contrastingForeground(Color(colorValue))
                            )
                        }
                    }
                    if (showIndices) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.inverseSurface)
                                .padding(horizontal = 4.dp)
                                .testTag("color_index_badge")
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.inverseOnSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
