package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.model.FailureTone
import io.github.ygaray.yahirandroidtaste.theme.LightColorScheme
import io.github.ygaray.yahirandroidtaste.theme.ToneColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Behavior proof for the Classic deltas of plan 22-03 (D-06, D-07, D-11): the Classic Confirm
 * button honors `confirmEnabled`, the Warn failure tone paints the caller's warn tone through the
 * pure [classicFailureSurfaceColors] seam, and a Classic `Failure.undo` renders below the failure
 * surface inside a 16dp inset. Robolectric here cannot read a painted color, so the container and
 * content colors are proven through the seam and `LocalContentColor`; no pixel capture.
 *
 * Hosts the internal `OutcomeSheetContent` (as OutcomeSheetTest does) so no ModalBottomSheet is
 * driven. One `setContent` per test method.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OutcomeSheetClassicDeltasTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /** Two distinctive tones so no assertion can pass by coincidence with the scheme roles. */
    private val customColors = OutcomeSheetColors(
        warnTone = ToneColors(container = Color(0xFF123456), content = Color(0xFF654321)),
        errorTone = ToneColors(container = Color(0xFF0A0B0C), content = Color(0xFFC0B0A0))
    )

    // -- D-07: Confirm enabled -------------------------------------------------------------------

    @Test
    fun `disabled classic confirm never fires onConfirm while cancel still fires`() {
        var confirms = 0
        var cancels = 0
        composeTestRule.setContent {
            Box(Modifier.height(800.dp)) {
                OutcomeSheetContent(
                    OutcomeFixtures.needsConfirmation(
                        confirmEnabled = false,
                        onConfirm = { confirms++ },
                        onCancel = { cancels++ }
                    )
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("outcome_sheet_confirmation_confirm").assertIsNotEnabled().performClick()
        composeTestRule.onNodeWithTag("outcome_sheet_confirmation_cancel").assertIsEnabled().performClick()
        composeTestRule.waitForIdle()

        assertEquals("a disabled Confirm must never fire onConfirm", 0, confirms)
        assertEquals("Cancel stays live while Confirm is disabled", 1, cancels)
    }

    @Test
    fun `default classic confirm is enabled and fires onConfirm once`() {
        var confirms = 0
        composeTestRule.setContent {
            Box(Modifier.height(800.dp)) {
                OutcomeSheetContent(OutcomeFixtures.needsConfirmation(onConfirm = { confirms++ }))
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("outcome_sheet_confirmation_confirm").assertIsEnabled().performClick()
        composeTestRule.waitForIdle()

        assertEquals(1, confirms)
    }

    // -- D-06: the Warn seam ---------------------------------------------------------------------

    @Test
    fun `seam paints the warn tone for Warn`() {
        val result = classicFailureSurfaceColors(FailureTone.Warn, customColors, LightColorScheme)

        assertEquals(customColors.warnTone, result)
        assertEquals(customColors.warnTone.container, result.container)
        assertEquals(customColors.warnTone.content, result.content)
    }

    @Test
    fun `seam keeps the error container roles for Error and not the colors error tone`() {
        val result = classicFailureSurfaceColors(FailureTone.Error, customColors, LightColorScheme)

        assertEquals(ToneColors(LightColorScheme.errorContainer, LightColorScheme.onErrorContainer), result)
        assertNotEquals(
            "the Error branch must read the scheme, not the OutcomeSheetColors error tone",
            customColors.errorTone,
            result
        )
    }

    @Test
    fun `classic Warn failure content color is the custom warn content color`() {
        var captured = Color.Unspecified
        composeTestRule.setContent {
            MaterialTheme(colorScheme = LightColorScheme) {
                Box(Modifier.height(800.dp)) {
                    OutcomeSheetContent(
                        OutcomeFixtures.failure(
                            tone = FailureTone.Warn,
                            body = { captured = LocalContentColor.current }
                        ),
                        colors = customColors
                    )
                }
            }
        }
        composeTestRule.waitForIdle()

        assertEquals(customColors.warnTone.content, captured)
    }

    @Test
    fun `classic default Error failure content color is onErrorContainer`() {
        var captured = Color.Unspecified
        composeTestRule.setContent {
            MaterialTheme(colorScheme = LightColorScheme) {
                Box(Modifier.height(800.dp)) {
                    OutcomeSheetContent(
                        OutcomeFixtures.failure(body = { captured = LocalContentColor.current }),
                        colors = customColors
                    )
                }
            }
        }
        composeTestRule.waitForIdle()

        assertEquals(LightColorScheme.onErrorContainer, captured)
    }

    // -- D-11: Failure.undo ----------------------------------------------------------------------

    @Test
    fun `classic Failure with undo renders the undo block below the surface with a 16dp inset`() {
        var undoAlls = 0
        composeTestRule.setContent {
            Box(Modifier.height(800.dp)) {
                OutcomeSheetContent(
                    OutcomeFixtures.failure(
                        undo = OutcomeFixtures.undo(onUndoAll = { undoAlls++ })
                    )
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("outcome_sheet_undo_all").assertExists()
        composeTestRule.onAllNodesWithTag("outcome_sheet_undo_row").assertCountEquals(2)
        composeTestRule.onAllNodesWithTag("outcome_sheet_undo_row")[0].assert(hasText("Row A"))
        composeTestRule.onAllNodesWithTag("outcome_sheet_undo_row")[1].assert(hasText("Row B"))

        val surface = composeTestRule.onNodeWithTag("outcome_sheet_failure_surface", useUnmergedTree = true)
            .getUnclippedBoundsInRoot()
        val undoAll = composeTestRule.onNodeWithTag("outcome_sheet_undo_all", useUnmergedTree = true)
            .getUnclippedBoundsInRoot()
        assertTrue(
            "the undo block must sit at or below the failure surface (${undoAll.top} >= ${surface.bottom})",
            undoAll.top >= surface.bottom
        )
        assertTrue(
            "the undo block needs a horizontal inset of at least 16dp (left = ${undoAll.left})",
            undoAll.left >= 16.dp
        )

        composeTestRule.onNodeWithTag("outcome_sheet_undo_all").performClick()
        composeTestRule.waitForIdle()
        assertEquals(1, undoAlls)
    }

    @Test
    fun `classic Failure with undo and a refusal renders the refused surface`() {
        composeTestRule.setContent {
            Box(Modifier.height(800.dp)) {
                OutcomeSheetContent(
                    OutcomeFixtures.failure(undo = OutcomeFixtures.undo(refused = OutcomeFixtures.refused()))
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("outcome_sheet_undo_refused").assertExists()
    }

    @Test
    fun `classic Failure without undo renders no undo nodes`() {
        composeTestRule.setContent {
            Box(Modifier.height(800.dp)) {
                OutcomeSheetContent(OutcomeFixtures.failure(undo = null))
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithTag("outcome_sheet_undo_all").assertCountEquals(0)
        composeTestRule.onAllNodesWithTag("outcome_sheet_undo_row").assertCountEquals(0)
        composeTestRule.onAllNodesWithTag("outcome_sheet_undo_refused").assertCountEquals(0)
    }
}
