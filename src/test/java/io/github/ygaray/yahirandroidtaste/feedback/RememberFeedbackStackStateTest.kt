package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.AccessibilityManager
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric tests for [rememberFeedbackStackState] and its accessibility binding: identity across
 * recomposition, the visible-card cap, and the reference-counted `LocalAccessibilityManager`
 * binding shared with [FeedbackStackHost] (D-06). A fake manager is always provided, because the
 * Robolectric default manager is not under test control.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RememberFeedbackStackStateTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private class FakeAccessibilityManager(private val factor: Long = 2) : AccessibilityManager {
        var lastFlags: Triple<Boolean, Boolean, Boolean>? = null

        override fun calculateRecommendedTimeoutMillis(
            originalTimeoutMillis: Long,
            containsIcons: Boolean,
            containsText: Boolean,
            containsControls: Boolean
        ): Long {
            lastFlags = Triple(containsIcons, containsText, containsControls)
            return originalTimeoutMillis * factor
        }
    }

    private var tick by mutableStateOf(0)
    private var showRemember by mutableStateOf(true)
    private var showHost by mutableStateOf(true)

    @Test
    fun sameInstanceAcrossRecomposition() {
        val seen = mutableListOf<FeedbackStackState>()
        composeTestRule.setContent {
            Text("tick $tick")
            seen += rememberFeedbackStackState(maxVisible = 3)
        }
        composeTestRule.waitForIdle()
        composeTestRule.runOnIdle { tick++ }
        composeTestRule.waitForIdle()
        composeTestRule.runOnIdle { tick++ }
        composeTestRule.waitForIdle()

        assertTrue("expected at least two compositions, got ${seen.size}", seen.size >= 2)
        seen.forEach { assertSame(seen.first(), it) }
    }

    @Test
    fun maxVisible_readsBackAndCoercesToAtLeastOne() {
        var two: FeedbackStackState? = null
        var zero: FeedbackStackState? = null
        composeTestRule.setContent {
            two = rememberFeedbackStackState(maxVisible = 2)
            zero = rememberFeedbackStackState(maxVisible = 0)
        }
        composeTestRule.waitForIdle()

        assertEquals(2, two!!.maxVisible)
        assertEquals(1, zero!!.maxVisible)
    }

    @Test
    fun accessibilityBinding_installsScalerWithMaterialFlags_andClearsOnLeave() {
        val fake = FakeAccessibilityManager(factor = 2)
        var state: FeedbackStackState? = null
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAccessibilityManager provides fake) {
                if (showRemember) state = rememberFeedbackStackState()
            }
        }
        composeTestRule.waitForIdle()

        val scaler = state!!.timeoutScaler
        assertNotNull(scaler)
        assertEquals(8_000L, scaler!!.invoke(4_000L, true))
        assertEquals(Triple(true, true, true), fake.lastFlags)
        assertEquals(20_000L, scaler.invoke(10_000L, false))
        assertEquals(Triple(true, true, false), fake.lastFlags)

        composeTestRule.runOnIdle { showRemember = false }
        composeTestRule.waitForIdle()

        assertNull(state!!.timeoutScaler)
    }

    @Test
    fun hostOverManuallyConstructedState_installsScaler_andRemovingItClears() {
        val fake = FakeAccessibilityManager()
        val state = FeedbackStackState()
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAccessibilityManager provides fake) {
                if (showHost) FeedbackStackHost(state = state, onOpenTarget = {})
            }
        }
        composeTestRule.waitForIdle()

        assertNotNull(state.timeoutScaler)
        assertEquals(8_000L, state.timeoutScaler!!.invoke(4_000L, false))

        composeTestRule.runOnIdle { showHost = false }
        composeTestRule.waitForIdle()

        assertNull(state.timeoutScaler)
    }

    @Test
    fun rememberAndHostOnTheSameManager_areReferenceCounted() {
        val fake = FakeAccessibilityManager()
        var state: FeedbackStackState? = null
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAccessibilityManager provides fake) {
                if (showRemember) {
                    val remembered = rememberFeedbackStackState()
                    state = remembered
                    if (showHost) FeedbackStackHost(state = remembered, onOpenTarget = {})
                }
            }
        }
        composeTestRule.waitForIdle()
        assertNotNull(state!!.timeoutScaler)

        composeTestRule.runOnIdle { showHost = false }
        composeTestRule.waitForIdle()
        assertNotNull("removing only the host must leave the binding", state!!.timeoutScaler)

        composeTestRule.runOnIdle { showRemember = false }
        composeTestRule.waitForIdle()
        assertNull("removing the remembered state's composable clears it", state!!.timeoutScaler)
    }

    @Test
    fun nullAccessibilityManager_leavesTheScalerNull() {
        var state: FeedbackStackState? = null
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAccessibilityManager provides null) {
                val remembered = rememberFeedbackStackState()
                state = remembered
                FeedbackStackHost(state = remembered, onOpenTarget = {})
            }
        }
        composeTestRule.waitForIdle()

        assertNull(state!!.timeoutScaler)
    }
}
