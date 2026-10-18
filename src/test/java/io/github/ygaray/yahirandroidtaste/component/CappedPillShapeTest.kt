package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Typography
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Phase 29 gap plan 07 (D-03): the shape math of [CappedPillShape], the shape the feedback card's
 * Undo keeps. A one-line button is the exact old capsule at every font scale; a wrapped one keeps the
 * one-line corner radius. The 40dp text-button minimum height raises the cap so the capsule also
 * holds at font scale 1.0, where the minimum (not line height plus padding) sets the one-line height.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CappedPillShapeTest {

    private val lineHeight = Typography().labelLarge.lineHeight
    private val padding = 8.dp
    private val minHeight = 40.dp
    private val shape = CappedPillShape(lineHeight, padding, minHeight)

    private fun Outline.requireRounded() = this as? Outline.Rounded
        ?: error("expected Outline.Rounded but was $this")

    private fun radiusAt(density: Density, height: Float, width: Float = 400f): Float =
        shape.createOutline(Size(width, height), LayoutDirection.Ltr, density).requireRounded()
            .roundRect.topLeftCornerRadius.x

    private fun oneLineHeight(density: Density): Float = with(density) {
        maxOf(minHeight.toPx(), lineHeight.toPx() + 2 * padding.toPx())
    }

    @Test
    fun oneLineRadius_equalsCircleShape_atFontScales() {
        listOf(1f, 1.5f, 2f).forEach { fontScale ->
            val density = Density(2.625f, fontScale)
            val height = oneLineHeight(density)
            val size = Size(400f, height)
            val circle = CircleShape.createOutline(size, LayoutDirection.Ltr, density).requireRounded()
            assertEquals(
                "one-line radius at font scale $fontScale",
                circle.roundRect.topLeftCornerRadius.x,
                radiusAt(density, height),
                0.01f
            )
            assertEquals(height / 2f, radiusAt(density, height), 0.01f)
        }
    }

    @Test
    fun wrappedHeight_isCappedAtTheOneLineHalfHeight() {
        val density = Density(2.625f, 2f)
        val oneLine = oneLineHeight(density)
        val fourLines = with(density) { 4 * lineHeight.toPx() + 2 * padding.toPx() }
        assertTrue("four lines must be taller than one", fourLines > oneLine * 2)
        assertEquals(oneLine / 2f, radiusAt(density, fourLines), 0.01f)
        assertTrue(radiusAt(density, fourLines) < fourLines / 2f)
    }

    @Test
    fun minimumHeightRaisesTheCap_atFontScale1() {
        val density = Density(2.625f, 1f)
        val withMinimum = cappedPillCapPx(density, lineHeight, padding, minHeight)
        val withoutMinimum = handledByPillCapPx(density, lineHeight, padding)
        assertEquals(with(density) { 20.dp.toPx() }, withMinimum, 0.01f)
        assertTrue("the 40dp minimum must raise the cap: $withMinimum vs $withoutMinimum", withMinimum > withoutMinimum + 1f)
    }

    @Test
    fun zeroSizeOutline_neverThrows() {
        val density = Density(2.625f, 2f)
        listOf(Size.Zero, Size(0f, 40f), Size(40f, 0f)).forEach { size ->
            val radius = shape.createOutline(size, LayoutDirection.Ltr, density).requireRounded()
                .roundRect.topLeftCornerRadius.x
            assertTrue("radius $radius must be finite and non-negative", radius.isFinite() && radius >= 0f)
        }
    }

    @Test
    fun unspecifiedOrEmLineHeight_usesTheFallback_andNeverThrows() {
        val density = Density(2.625f, 1.5f)
        val expected = maxOf(
            handledByPillCapPx(density, 16.sp, padding),
            with(density) { minHeight.toPx() / 2f }
        )
        listOf(TextUnit.Unspecified, 1.5.em).forEach { height ->
            val cap = cappedPillCapPx(density, height, padding, minHeight)
            assertTrue("cap $cap must be finite and sensible", cap.isFinite() && cap in 0f..10_000f)
            assertEquals("a non-sp line height falls back to 16sp", expected, cap, 0.001f)
            val radius = CappedPillShape(height, padding, minHeight)
                .createOutline(Size(300f, 200f), LayoutDirection.Ltr, density).requireRounded()
                .roundRect.topLeftCornerRadius.x
            assertEquals(expected, radius, 0.001f)
        }
    }
}
