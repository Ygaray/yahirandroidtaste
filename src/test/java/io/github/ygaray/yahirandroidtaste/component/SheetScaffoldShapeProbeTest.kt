package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Recording-Shape probe (VSHEET-01, R-13): a caller-supplied [Shape] must reach the real
 * `ModalBottomSheet` through [SheetScaffold]. The shape records every size it is asked to outline;
 * a non-empty record proves the sheet container consulted the caller's shape. No pixel capture.
 */
@OptIn(ExperimentalMaterial3Api::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SheetScaffoldShapeProbeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `caller supplied shape is consulted by the real sheet`() {
        val recorded = mutableListOf<Size>()
        val recordingShape = object : Shape {
            override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
                recorded += size
                return Outline.Rectangle(Rect(0f, 0f, size.width, size.height))
            }
        }

        composeTestRule.setContent {
            SheetScaffold(onDismissRequest = {}, shape = recordingShape) {
                Text("probe body")
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("probe body").assertExists()
        assertTrue("recording shape was never asked for an outline", recorded.isNotEmpty())
    }
}
