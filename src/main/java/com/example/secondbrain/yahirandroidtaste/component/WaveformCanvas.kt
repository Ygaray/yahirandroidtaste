package com.example.secondbrain.yahirandroidtaste.component

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * WaveformCanvas — renders amplitude bars as rounded rectangles on a Canvas.
 *
 * Used in two contexts:
 * - RecordingBottomSheet: live bars from MediaRecorder.getMaxAmplitude() at 100ms intervals
 * - VoicePlaybackScreen: static bars from companion .bin file with a playback progress cursor
 * - VoiceCard face: compact downsampled bars
 *
 * @param bars Normalized amplitude values (0..1). Each value becomes one bar.
 * @param progressFraction 0..1 — bars at or before this fraction use [activeColor];
 *   bars after use [inactiveColor]. Set to 1f (default) for recording (all bars active).
 * @param activeColor Color for bars at or before the progress cursor.
 * @param inactiveColor Color for bars after the progress cursor.
 * @param modifier Modifier applied to the Canvas.
 */
@Composable
fun WaveformCanvas(
    bars: List<Float>,
    activeColor: Color,
    inactiveColor: Color,
    modifier: Modifier = Modifier,
    progressFraction: Float = 1f
) {
    Canvas(modifier = modifier) {
        if (bars.isEmpty()) return@Canvas

        val barWidth = size.width / bars.size
        val maxBarHeight = size.height * 0.9f
        val minBarHeight = 4.dp.toPx()

        bars.forEachIndexed { i, amplitude ->
            val barHeight = minBarHeight + amplitude * (maxBarHeight - minBarHeight)
            val color = if (i.toFloat() / bars.size < progressFraction || progressFraction >= 1f) activeColor else inactiveColor

            drawRoundRect(
                color = color,
                topLeft = Offset(
                    x = i * barWidth + barWidth * 0.1f,
                    y = (size.height - barHeight) / 2f
                ),
                size = Size(barWidth * 0.8f, barHeight),
                cornerRadius = CornerRadius(barWidth * 0.4f)
            )
        }
    }
}

// downsample() moved to WaveformUtil.kt (same package) for JVM unit testability.
