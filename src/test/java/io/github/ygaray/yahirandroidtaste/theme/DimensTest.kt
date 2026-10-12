package io.github.ygaray.yahirandroidtaste.theme

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-JUnit tests for [Dimens] (Phase 123 Plan 01 Task 1, DS-01).
 *
 * Locks the six new [Dimens.Elevation] levels' exact values and strictly-increasing order, and
 * regression-locks every pre-existing [Dimens] field's exact pre-phase value — the automated
 * backstop for the additive-only owner directive on this file (this is a NEW test file; no
 * pre-existing test was modified).
 */
class DimensTest {

    @Test
    fun `Elevation levels equal their exact expected Dp values`() {
        assertEquals(0.dp, Dimens.Elevation.Level0)
        assertEquals(1.dp, Dimens.Elevation.Level1)
        assertEquals(3.dp, Dimens.Elevation.Level2)
        assertEquals(6.dp, Dimens.Elevation.Level3)
        assertEquals(8.dp, Dimens.Elevation.Level4)
        assertEquals(12.dp, Dimens.Elevation.Level5)
    }

    @Test
    fun `Elevation levels are strictly increasing and six distinct values`() {
        val levels = listOf(
            Dimens.Elevation.Level0,
            Dimens.Elevation.Level1,
            Dimens.Elevation.Level2,
            Dimens.Elevation.Level3,
            Dimens.Elevation.Level4,
            Dimens.Elevation.Level5
        )

        for (i in 0 until levels.size - 1) {
            assertTrue(
                "Expected ${levels[i]} < ${levels[i + 1]} at index $i",
                levels[i] < levels[i + 1]
            )
        }
        assertEquals(6, levels.toSet().size)
    }

    @Test
    fun `every pre-existing Dimens field still equals its pre-phase value`() {
        assertEquals(16.dp, Dimens.HorizontalPadding)
        assertEquals(8.dp, Dimens.TopPadding)
        assertEquals(4.dp, Dimens.BottomPadding)
        assertEquals(4.dp, Dimens.ContentSpacing)
        assertEquals(2.dp, Dimens.HairlineSpacing)
        assertEquals(48.dp, Dimens.TouchTarget)
        assertEquals(12.dp, Dimens.CompactPadding)
        assertEquals(1.dp, Dimens.HairlineBorder)
        assertEquals(32.dp, Dimens.Icons.MenuButton)
        assertEquals(20.dp, Dimens.Icons.MenuIcon)
        assertEquals(24.dp, Dimens.Icons.DragHandle)
        assertEquals(72.dp, Dimens.SwipeReveal.ButtonWidth)
        assertEquals(24.dp, Dimens.SwipeReveal.IconSize)
        assertEquals(8.dp, Dimens.CornerRadius.Small)
        assertEquals(12.dp, Dimens.CornerRadius.Medium)
        assertEquals(28.dp, Dimens.CornerRadius.Large)
    }

    @Test
    fun `ActionButton tokens equal their phase 20 values`() {
        assertEquals(40.dp, Dimens.ActionButton.FilledMinHeight)
        assertEquals(20.dp, Dimens.ActionButton.FilledHorizontalPadding)
        assertEquals(8.dp, Dimens.ActionButton.ContentVerticalPadding)
        assertEquals(12.dp, Dimens.ActionButton.TextHorizontalPadding)
        assertEquals(8.dp, Dimens.ActionButton.TextSmallHorizontalPadding)
        assertEquals(4.dp, Dimens.ActionButton.TextSmallVerticalPadding)
        assertEquals(18.dp, Dimens.ActionButton.IconSize)
        assertEquals(8.dp, Dimens.ActionButton.IconGap)
    }

    @Test
    fun `RoundIconButton tokens equal their phase 20 values`() {
        assertEquals(40.dp, Dimens.RoundIconButton.Size)
        assertEquals(20.dp, Dimens.RoundIconButton.IconSize)
    }

    @Test
    fun `PillChip tokens equal their phase 20 values`() {
        assertEquals(32.dp, Dimens.PillChip.Height)
        assertEquals(12.dp, Dimens.PillChip.LabelOnlyHorizontalPadding)
        assertEquals(8.dp, Dimens.PillChip.IconSideHorizontalPadding)
    }

    @Test
    fun `StackedProgressBar tokens equal their phase 20 values`() {
        assertEquals(6.dp, Dimens.StackedProgressBar.Height)
    }
}
