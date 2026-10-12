package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Permanent regression pin of the assumption behind the 92dp [ReceiptRow] anatomy
 * (12 + 24 + 4 + 20 + 4 + 16 + 12): a single line styled through [receiptRowLine] is exactly as
 * tall as its `lineHeight` (24dp `titleMedium`, 20dp `bodyMedium`, 16dp `bodySmall`).
 *
 * NATIVE graphics are required: legacy graphics report synthetic font metrics.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ReceiptRowLineHeightTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `single lines styled by receiptRowLine are as tall as their line height`() {
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                Box(Modifier.width(360.dp)) {
                    Column {
                        Text(
                            "Title",
                            style = MaterialTheme.typography.titleMedium.receiptRowLine(),
                            modifier = Modifier.testTag("line_title")
                        )
                        Text(
                            "Body",
                            style = MaterialTheme.typography.bodyMedium.receiptRowLine(),
                            modifier = Modifier.testTag("line_body")
                        )
                        Text(
                            "Small",
                            style = MaterialTheme.typography.bodySmall.receiptRowLine(),
                            modifier = Modifier.testTag("line_small")
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()

        listOf("line_title" to 24f, "line_body" to 20f, "line_small" to 16f).forEach { (tag, expected) ->
            val bounds = composeTestRule.onNodeWithTag(tag).getUnclippedBoundsInRoot()
            assertEquals("$tag height", expected, (bounds.bottom - bounds.top).value, 0.5f)
        }
    }
}
