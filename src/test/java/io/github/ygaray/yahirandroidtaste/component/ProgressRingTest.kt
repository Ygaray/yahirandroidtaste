package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme

/**
 * Compose tests for [ProgressRing] (Phase 44 Plan 01 Task 1 tracer).
 *
 * Locks DS-01's literal "non-throwing" contract through the real consumer (composing
 * [ProgressRing] with no [YahirAndroidTasteTheme] wrapper must not throw) and DS-03's
 * clamp-not-crash boundary probe (out-of-range `fraction` values compose without throwing).
 *
 * Infra mirrors this module's established Robolectric+Compose harness ([CountBadgeTest]):
 * `@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [35])`, `createComposeRule()`.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ProgressRingTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `composes without throwing outside YahirAndroidTasteTheme`() {
        composeTestRule.setContent {
            ProgressRing(fraction = 0.5f)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun `composes inside YahirAndroidTasteTheme`() {
        composeTestRule.setContent {
            YahirAndroidTasteTheme(dynamicColor = false) {
                ProgressRing(fraction = 0.42f)
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun `clamps out-of-range fraction instead of crashing`() {
        composeTestRule.setContent {
            ProgressRing(fraction = 1.5f)
            ProgressRing(fraction = -0.5f)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onRoot().assertExists()
    }
}
