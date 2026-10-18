package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Typography
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import io.github.ygaray.yahirandroidtaste.theme.Dimens
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * POL-01 (D-06 [pill-shape]): the Receipt handled-by pill is painted with a capped-radius shape so a
 * pill that wraps keeps its text inside its outline while a one-line pill is the exact old capsule.
 *
 * NATIVE graphics gives real font metrics and real wrapping. The semantics tree cannot show a shape,
 * so the geometry is checked on the outline the production accessor builds, against the measured
 * text box, and a source contract proves the pill uses that accessor. A control with `CircleShape`
 * must FAIL the same corner check, so the check cannot pass vacuously. The wrapped case runs in German
 * and in Spanish at 360dp and font scale 2.0 (Phase 29, D-03 [spanish-check], Robolectric half).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HandledByPillShapeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val germanPrefix = "Bearbeitet von"
    private val germanTier = "Verarbeitung auf dem Gerät ohne Netzwerkverbindung"
    private val germanProvider = "Lokale Grammatik offline"

    private val spanishPrefix = "Gestionado por"
    private val spanishTier = "Procesamiento en el dispositivo sin conexión de red"
    private val spanishProvider = "Gramática local sin conexión"

    /** The four corners of the pill text box in the painted rectangle's local space, plus the shape and density. */
    private class Measured(
        val shape: Shape,
        val density: Density,
        val paintedSize: Size,
        val textCorners: List<Offset>,
        val textLineCount: Int
    )

    private fun SemanticsNodeInteraction.textLayout(): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        val action = fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action
        checkNotNull(action) { "node has no GetTextLayoutResult action" }.invoke(results)
        return results.single()
    }

    private fun measureWrappedPill(prefix: String, tier: String, provider: String): Measured {
        var shape: Shape? = null
        var density: Density? = null
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                val scaled = Density(LocalDensity.current.density, fontScale = 2f)
                CompositionLocalProvider(LocalDensity provides scaled) {
                    shape = rememberHandledByPillShape()
                    density = scaled
                    Box(Modifier.width(360.dp).height(900.dp)) {
                        OutcomeSheetReceiptContent(
                            OutcomeFixtures.success(
                                handledBy = OutcomeFixtures.handledBy(
                                    prefixLabel = prefix,
                                    tier = tier,
                                    provider = provider,
                                    onDevice = true
                                )
                            )
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()

        val capturedDensity = checkNotNull(density)
        val pill = composeTestRule.onNodeWithTag("outcome_sheet_handled_by", useUnmergedTree = true)
            .fetchSemanticsNode()
        val textInteraction = composeTestRule.onNodeWithText("$prefix $tier · $provider", useUnmergedTree = true)
        val text = textInteraction.fetchSemanticsNode()

        // The tag sits after the inner padding, so the tagged bounds are the CONTENT area; the painted
        // rectangle is that area inflated by the pill padding.
        val padH = with(capturedDensity) { Dimens.OutcomeReceipt.PillHorizontalPadding.toPx() }
        val padV = with(capturedDensity) { Dimens.OutcomeReceipt.PillVerticalPadding.toPx() }
        val paintedLeft = pill.positionInRoot.x - padH
        val paintedTop = pill.positionInRoot.y - padV
        val paintedSize = Size(pill.size.width + 2 * padH, pill.size.height + 2 * padV)

        val left = text.positionInRoot.x - paintedLeft
        val top = text.positionInRoot.y - paintedTop
        val right = left + text.size.width
        val bottom = top + text.size.height
        return Measured(
            shape = checkNotNull(shape),
            density = capturedDensity,
            paintedSize = paintedSize,
            textCorners = listOf(Offset(left, top), Offset(right, top), Offset(left, bottom), Offset(right, bottom)),
            textLineCount = textInteraction.textLayout().lineCount
        )
    }

    private fun Outline.requireRounded() = this as? Outline.Rounded
        ?: error("expected Outline.Rounded but was $this")

    private fun measureWrappedGermanPill(): Measured = measureWrappedPill(germanPrefix, germanTier, germanProvider)

    private fun measureWrappedSpanishPill(): Measured =
        measureWrappedPill(spanishPrefix, spanishTier, spanishProvider)

    @Test
    fun wrappedLongGermanPill_atFontScale2_keepsEveryTextCornerInsideTheShape() {
        val m = measureWrappedGermanPill()
        assertTrue("the pill must wrap to several lines, had ${m.textLineCount}", m.textLineCount > 1)

        val outline = m.shape.createOutline(m.paintedSize, LayoutDirection.Ltr, m.density).requireRounded()
        val outside = m.textCorners.filterNot { outline.roundRect.contains(it) }
        assertTrue("every text corner must lie inside the painted shape, outside: $outside", outside.isEmpty())
    }

    @Test
    fun controlCircleShape_failsTheSameCornerCheck_whenWrapped() {
        val m = measureWrappedGermanPill()
        assertTrue("the pill must wrap to several lines, had ${m.textLineCount}", m.textLineCount > 1)

        val outline = CircleShape.createOutline(m.paintedSize, LayoutDirection.Ltr, m.density).requireRounded()
        val outside = m.textCorners.filterNot { outline.roundRect.contains(it) }
        assertTrue("the CircleShape control must lose at least one corner, so the check is not vacuous", outside.isNotEmpty())
    }

    @Test
    fun wrappedLongSpanishPill_atFontScale2_keepsEveryTextCornerInsideTheShape() {
        val m = measureWrappedSpanishPill()
        assertTrue("the pill must wrap to several lines, had ${m.textLineCount}", m.textLineCount > 1)

        val outline = m.shape.createOutline(m.paintedSize, LayoutDirection.Ltr, m.density).requireRounded()
        val outside = m.textCorners.filterNot { outline.roundRect.contains(it) }
        assertTrue("every text corner must lie inside the painted shape, outside: $outside", outside.isEmpty())
    }

    @Test
    fun controlCircleShape_failsTheSameCornerCheck_whenWrapped_inSpanish() {
        val m = measureWrappedSpanishPill()
        assertTrue("the pill must wrap to several lines, had ${m.textLineCount}", m.textLineCount > 1)

        val outline = CircleShape.createOutline(m.paintedSize, LayoutDirection.Ltr, m.density).requireRounded()
        val outside = m.textCorners.filterNot { outline.roundRect.contains(it) }
        assertTrue("the CircleShape control must lose at least one corner, so the check is not vacuous", outside.isNotEmpty())
    }

    @Test
    fun oneLinePill_radiusEqualsCircleShape_atFontScales() {
        val lineHeight = Typography().bodySmall.lineHeight
        val verticalPadding = Dimens.OutcomeReceipt.PillVerticalPadding
        listOf(1f, 1.5f, 2f).forEach { fontScale ->
            val density = Density(2.625f, fontScale)
            val height = with(density) { lineHeight.toPx() + 2 * verticalPadding.toPx() }
            val size = Size(400f, height)
            val capped = HandledByPillShape(lineHeight, verticalPadding)
                .createOutline(size, LayoutDirection.Ltr, density).requireRounded()
            val circle = CircleShape.createOutline(size, LayoutDirection.Ltr, density).requireRounded()
            assertEquals(
                "one-line corner radius at font scale $fontScale",
                circle.roundRect.topLeftCornerRadius.x,
                capped.roundRect.topLeftCornerRadius.x,
                0.01f
            )
            assertEquals(height / 2f, capped.roundRect.topLeftCornerRadius.x, 0.01f)
        }
    }

    @Test
    fun radius_isHalfTheHeight_whenTheHeightIsAtMostTheCap() {
        assertEquals(10f, handledByPillCornerRadius(heightPx = 20f, capPx = 10f), 0f)
        assertEquals(5f, handledByPillCornerRadius(heightPx = 10f, capPx = 10f), 0f)
    }

    @Test
    fun radius_isTheCap_whenTheHeightExceedsTwiceTheCap() {
        assertEquals(10f, handledByPillCornerRadius(heightPx = 80f, capPx = 10f), 0f)
        assertEquals(10f, handledByPillCornerRadius(heightPx = 21f, capPx = 10f), 0f)
    }

    @Test
    fun zeroSizeOutline_neverThrows() {
        val density = Density(2.625f, 2f)
        val shape = HandledByPillShape(16.sp, 4.dp)
        listOf(Size.Zero, Size(0f, 40f), Size(40f, 0f)).forEach { size ->
            val outline = shape.createOutline(size, LayoutDirection.Ltr, density).requireRounded()
            val radius = outline.roundRect.topLeftCornerRadius.x
            assertTrue("radius $radius must be finite and non-negative", radius.isFinite() && radius >= 0f)
        }
    }

    @Test
    fun unspecifiedOrEmLineHeight_usesTheFallback_andNeverThrows() {
        val density = Density(2.625f, 1.5f)
        val fallback = handledByPillCapPx(density, 16.sp, 4.dp)
        listOf(TextUnit.Unspecified, 1.5.em).forEach { lineHeight ->
            val cap = handledByPillCapPx(density, lineHeight, 4.dp)
            assertTrue("cap $cap must be finite and non-negative", cap.isFinite() && cap >= 0f)
            assertEquals("a non-sp line height falls back to 16sp", fallback, cap, 0.001f)
            val outline = HandledByPillShape(lineHeight, 4.dp)
                .createOutline(Size(300f, 200f), LayoutDirection.Ltr, density).requireRounded()
            assertEquals(fallback, outline.roundRect.topLeftCornerRadius.x, 0.001f)
        }
    }

    // ---- Wiring (PD-15) -------------------------------------------------------------------------

    @Test
    fun pillBackgroundUsesTheCappedShape() {
        val receipt = SourceContractTestSupport.stripComments(SourceContractTestSupport.source("OutcomeSheetReceipt.kt"))
        val body = SourceContractTestSupport.functionBody(receipt, "fun ReceiptHandledByPill(")
            .filterNot { it.isWhitespace() }
        assertTrue("the pill must obtain the shape from rememberHandledByPillShape()", body.contains("rememberHandledByPillShape()"))
        assertTrue(
            "the pill background must take the capped shape",
            body.contains(".background(scheme.surfaceContainerHighest,pillShape)")
        )
    }
}
