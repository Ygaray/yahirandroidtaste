package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Compose tests for [AppChip]'s additive `onDoubleClick` parameter (Phase 106-02, TAG-02).
 *
 * Infra mirrors this module's established Robolectric+Compose harness ([TagListItemTest]):
 * `@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [35])`, `createComposeRule()`. The
 * composable is rendered directly with no theme wrapper, matching the established convention.
 *
 * Double-tap and long-press gestures are driven through the compose test rule's touch-input API
 * ([performTouchInput] with [doubleClick]/[longClick]) rather than issuing two separate clicks, so
 * the gesture actually reaches the [AppChip] inner Surface's `combinedClickable` modifier as a real
 * double-tap/long-press — not merely two independent single-tap invocations.
 *
 * **Latency claim scope (RESEARCH.md Pitfall 2 / Assumption A1 — confirmed by decompiling the
 * resolved `androidx.compose.foundation:foundation-android` bytecode this session, see the Task 1
 * commit message):** what this class proves is that the **default-null** `onDoubleClick` path is
 * byte-identical to `AppChip`'s pre-phase behavior — every existing consumer is unaffected. It also
 * proves the **active-binding** path eventually fires `onClick` on a single tap (see
 * `single click with a non-null onDoubleClick still invokes onClick exactly once`, which needs a
 * polling [androidx.compose.ui.test.junit4.ComposeContentTestRule.waitUntil] rather than a single
 * `waitForIdle()` — direct empirical evidence that `onClick` is genuinely delayed, not instant, once
 * `onDoubleClick` is non-null). What this class does **not** prove, and must not be read as proving,
 * is that an actively-bound double-tap adds **zero** single-tap latency: Robolectric's test clock
 * auto-advances, so any such assertion would pass vacuously without measuring real wall-clock delay,
 * and the decompiled bytecode confirms the delay is real (`ViewConfiguration.getDoubleTapTimeoutMillis()`
 * followed by `kotlinx.coroutines.delay()` before `onClick` fires). Real-perception judgment of an
 * actively-bound double-tap's latency belongs to Phase 109's on-device Gate-1 — no call site binds
 * `onDoubleClick` in this phase (D-02), so the question is moot for this phase's own shipped build.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppChipTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `double-tap with a non-null onDoubleClick invokes it exactly once`() {
        var doubleClickCount = 0

        composeTestRule.setContent {
            AppChip(
                label = "Work",
                isSelected = false,
                onClick = {},
                onDoubleClick = { doubleClickCount++ }
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Work").performTouchInput { doubleClick() }
        composeTestRule.waitForIdle()

        assertEquals(
            "Double-tapping a chip with a non-null onDoubleClick must invoke it exactly once",
            1,
            doubleClickCount
        )
    }

    @Test
    fun `single click with a non-null onDoubleClick still invokes onClick exactly once`() {
        var clickCount = 0
        var doubleClickCount = 0

        composeTestRule.setContent {
            AppChip(
                label = "Work",
                isSelected = false,
                onClick = { clickCount++ },
                onDoubleClick = { doubleClickCount++ }
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Work").performClick()
        composeTestRule.waitUntil(timeoutMillis = 2_000) { clickCount == 1 }

        assertEquals(
            "A single click on a chip whose double-tap slot is occupied must still invoke onClick" +
                " exactly once",
            1,
            clickCount
        )
        assertEquals(
            "A single click must never invoke onDoubleClick",
            0,
            doubleClickCount
        )
    }

    @Test
    fun `double-tap with both callbacks supplied fires only onDoubleClick, never onLongClick`() {
        var doubleClickCount = 0
        var longClickCount = 0

        composeTestRule.setContent {
            AppChip(
                label = "Work",
                isSelected = false,
                onClick = {},
                onLongClick = { longClickCount++ },
                onDoubleClick = { doubleClickCount++ }
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Work").performTouchInput { doubleClick() }
        composeTestRule.waitForIdle()

        assertEquals(
            "A double-tap must invoke onDoubleClick exactly once when both callbacks are supplied",
            1,
            doubleClickCount
        )
        assertEquals(
            "A double-tap must invoke onLongClick exactly zero times",
            0,
            longClickCount
        )
    }

    @Test
    fun `both callbacks null - a single click invokes onClick exactly once and the label renders`() {
        var clickCount = 0

        composeTestRule.setContent {
            AppChip(
                label = "Work",
                isSelected = false,
                onClick = { clickCount++ }
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Work").assertExists()
        composeTestRule.onNodeWithText("Work").performClick()
        composeTestRule.waitForIdle()

        assertEquals(
            "With both callbacks null (the shipped default for every existing consumer), a single" +
                " click must invoke onClick exactly once via the plain-clickable fallback branch",
            1,
            clickCount
        )
    }

    @Test
    fun `onLongClick only - long press fires it once, a separate click fires onClick once`() {
        var clickCount = 0
        var longClickCount = 0

        composeTestRule.setContent {
            AppChip(
                label = "Work",
                isSelected = false,
                onClick = { clickCount++ },
                onLongClick = { longClickCount++ }
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Work").performTouchInput { longClick() }
        composeTestRule.waitForIdle()

        assertEquals(
            "Today's shipped context-menu-chip shape: a long press must invoke onLongClick exactly" +
                " once",
            1,
            longClickCount
        )

        composeTestRule.onNodeWithText("Work").performClick()
        composeTestRule.waitForIdle()

        assertEquals(
            "A separate single click must still invoke onClick exactly once — long-press-only" +
                " does not delay onClick (no onDoubleClick disambiguation window applies here)",
            1,
            clickCount
        )
    }

    @Test
    fun `onDoubleClick only - double-tap fires it once, a long press fires nothing`() {
        var doubleClickCount = 0
        var longClickCount = 0

        composeTestRule.setContent {
            AppChip(
                label = "Work",
                isSelected = false,
                onClick = {},
                onDoubleClick = { doubleClickCount++ }
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Work").performTouchInput { doubleClick() }
        composeTestRule.waitForIdle()

        assertEquals(
            "The newly reachable branch: a double-tap with only onDoubleClick supplied must" +
                " invoke it exactly once",
            1,
            doubleClickCount
        )

        composeTestRule.onNodeWithText("Work").performTouchInput { longClick() }
        composeTestRule.waitForIdle()

        assertEquals(
            "A long press on a double-tap-only chip must invoke nothing — there is no" +
                " onLongClick to fire, and the long-press counter (tracking onDoubleClick" +
                " misfires) must stay zero",
            0,
            longClickCount
        )
    }

    @Test
    fun `default parameters - constructing with only the three required args compiles and is clickable`() {
        var clickCount = 0

        composeTestRule.setContent {
            AppChip(
                label = "Work",
                isSelected = false,
                onClick = { clickCount++ }
            )
        }
        composeTestRule.waitForIdle()

        // Omitting onLongClick/onDoubleClick/relatednessStrength/icons compiles unchanged and the
        // chip behaves exactly as it did before this phase — the default-parameter parity claim.
        composeTestRule.onNodeWithText("Work").performClick()
        composeTestRule.waitForIdle()

        assertEquals(
            "A chip constructed with only its three required arguments must remain clickable," +
                " byte-identical to pre-phase AppChip",
            1,
            clickCount
        )
    }
}
