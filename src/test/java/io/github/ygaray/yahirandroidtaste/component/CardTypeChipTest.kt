package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.ygaray.yahirandroidtaste.theme.TactileType
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests for [CardTypeChip] (Phase 129 DS-02 D-01) and [TactileType.CardTitle] (Phase 129 DS-02).
 *
 * Infra mirrors this module's established Robolectric+Compose harness ([CountBadgeTest]):
 * `@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [35])`, `createComposeRule()`. The two
 * typography cases are plain JUnit assertions on [TactileType.CardTitle]'s `TextStyle` properties
 * — no compose rule needed for them, but sharing this file/class keeps a single `--tests` filter
 * covering both (per 129-01-PLAN.md Task 2's action).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CardTypeChipTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `chip with a non-null accent composes a 32dp badge`() {
        composeTestRule.setContent {
            YahirAndroidTasteTheme(dynamicColor = false) {
                CardTypeChip(accent = Color(0xFF6750A4)) {
                    Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice")
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("card_type_chip")
            .assertWidthIsEqualTo(32.dp)
            .assertHeightIsEqualTo(32.dp)
    }

    @Test
    fun `chip with a null accent still composes a 32dp badge without throwing`() {
        composeTestRule.setContent {
            YahirAndroidTasteTheme(dynamicColor = false) {
                CardTypeChip(accent = null) {
                    Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice")
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("card_type_chip")
            .assertWidthIsEqualTo(32.dp)
            .assertHeightIsEqualTo(32.dp)
    }

    @Test
    fun `TactileType CardTitle is Space Grotesk SemiBold 18sp 24sp lineHeight 0-1sp letterSpacing`() {
        val style = TactileType.CardTitle

        assertEquals(FontWeight.SemiBold, style.fontWeight)
        assertEquals(18.sp, style.fontSize)
        assertEquals(24.sp, style.lineHeight)
        assertEquals(0.1.sp, style.letterSpacing)
    }

    @Test
    fun `TactileType DisplayXSmall weight is not mutated by this task`() {
        assertEquals(FontWeight.Normal, TactileType.DisplayXSmall.fontWeight)
    }
}
