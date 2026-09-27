package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performTouchInput
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Regression coverage for **voice-mic-press-no-capture** (ported from CalTracker's
 * `MicFabGestureTest.kt`, the same gesture wiring, hub-side without any permission concept).
 *
 * [MicButton] renders the FAB visuals via a plain `Surface` whose single `pointerInput` owns the
 * ONE tap gesture. If a future edit reintroduces a second gesture owner (e.g. wrapping this in a
 * `FloatingActionButton`), the internal `clickable` would consume the pointer-down first and
 * [onTap] would never fire on a genuine release — only the ripple would show. This test injects a
 * REAL tap gesture through Compose so that regression fails here, in the JVM gate, before it can
 * ship.
 *
 * Runs under Robolectric (JVM) so it stays in the `testDebugUnitTest` gate — no device needed.
 * Infra mirrors this module's established Robolectric+Compose harness (`CycleSubTypeButtonTest`,
 * `AppChipTest`): `@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [35])` (the default SDK
 * 36 requires Java 21, unavailable in this module's build environment), `createComposeRule()`.
 *
 * Proves:
 *  - A single tap (down then up) on an enabled mic invokes [MicButton]'s `onTap` exactly once,
 *    ONLY after release (a `down` alone must not fire it — proving the action is release-gated,
 *    not press-gated) and never the disabled-tap affordance.
 *  - A disabled tap resolves via `onDisabledTap` and NEVER fires `onTap` — even for a slow
 *    press-and-hold-then-release.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MicButtonGestureTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val tapToTalk = "Tap to talk"
    private val notSetUp = "Microphone unavailable"

    @Test
    fun tap_onEnabledMic_invokesOnTapExactlyOnce() {
        var tapped = 0
        var disabledTap = 0
        composeRule.setContent {
            MaterialTheme {
                MicButton(
                    isListening = false,
                    enabled = true,
                    onTap = { tapped++ },
                    onDisabledTap = { disabledTap++ },
                )
            }
        }
        val node = composeRule.onNodeWithContentDescription(tapToTalk)
        node.assertExists()

        // Press (finger down) — the action must NOT fire yet (release-gated, not press-gated).
        node.performTouchInput { down(center) }
        composeRule.waitForIdle()
        assertEquals("onTap must NOT fire on press alone", 0, tapped)

        // Release (finger up) — the tap completes.
        node.performTouchInput { up() }
        composeRule.waitForIdle()
        assertEquals("onTap must fire exactly once, on release", 1, tapped)
        assertEquals("an enabled tap must never hit the disabled-tap affordance", 0, disabledTap)
    }

    @Test
    fun tap_onDisabledMic_invokesOnDisabledTap_andNeverFiresOnTap() {
        var tapped = 0
        var disabledTap = 0
        composeRule.setContent {
            MaterialTheme {
                MicButton(
                    isListening = false,
                    enabled = false,
                    onTap = { tapped++ },
                    onDisabledTap = { disabledTap++ },
                )
            }
        }
        val node = composeRule.onNodeWithContentDescription(notSetUp)
        node.assertExists()

        node.performTouchInput {
            down(center)
            up()
        }
        composeRule.waitForIdle()

        assertEquals("a disabled tap must NEVER fire onTap", 0, tapped)
        assertEquals("a disabled tap must invoke onDisabledTap", 1, disabledTap)
    }

    @Test
    fun pressAndHold_onDisabledMic_neverFiresOnTap() {
        var tapped = 0
        var disabledTap = 0
        composeRule.setContent {
            MaterialTheme {
                MicButton(
                    isListening = false,
                    enabled = false,
                    onTap = { tapped++ },
                    onDisabledTap = { disabledTap++ },
                )
            }
        }
        val node = composeRule.onNodeWithContentDescription(notSetUp)

        node.performTouchInput { down(center) }
        composeRule.waitForIdle()
        assertEquals("holding a disabled mic must never fire onTap", 0, tapped)

        node.performTouchInput { up() }
        composeRule.waitForIdle()
        assertEquals("onDisabledTap fires exactly once on release", 1, disabledTap)
        assertEquals(0, tapped)
    }

    @Test
    fun tap_midPressCallbackIdentitySwap_firesOnlyLatestOnTap() {
        var tapA = 0
        var tapB = 0
        lateinit var swapToTapB: () -> Unit
        composeRule.setContent {
            MaterialTheme {
                var onTapCallback by remember { mutableStateOf<() -> Unit>({ tapA++ }) }
                swapToTapB = { onTapCallback = { tapB++ } }
                MicButton(
                    isListening = false,
                    enabled = true,
                    onTap = onTapCallback,
                    onDisabledTap = {},
                )
            }
        }
        val node = composeRule.onNodeWithContentDescription(tapToTalk)

        // Press (finger down) — nothing has fired yet (release-gated, matches the file's convention).
        node.performTouchInput { down(center) }
        composeRule.waitForIdle()
        assertEquals("must not fire on press alone", 0, tapA)
        assertEquals("must not fire on press alone", 0, tapB)

        // Mid-press: swap the onTap identity to a DISTINCT counter and force recomposition while
        // the gesture is still in flight.
        swapToTapB()
        composeRule.waitForIdle()

        // Release — must dispatch through the LATEST closure (tapB), never the stale one (tapA).
        node.performTouchInput { up() }
        composeRule.waitForIdle()
        assertEquals("release must invoke the latest onTap closure (tapB)", 1, tapB)
        assertEquals("release must never invoke the stale onTap closure (tapA)", 0, tapA)
    }

    @Test
    fun tap_midPressDisabledTapCallbackIdentitySwap_firesOnlyLatestOnDisabledTap() {
        var disabledTapA = 0
        var disabledTapB = 0
        lateinit var swapToDisabledTapB: () -> Unit
        composeRule.setContent {
            MaterialTheme {
                var onDisabledTapCallback by remember { mutableStateOf<() -> Unit>({ disabledTapA++ }) }
                swapToDisabledTapB = { onDisabledTapCallback = { disabledTapB++ } }
                MicButton(
                    isListening = false,
                    enabled = false,
                    onTap = {},
                    onDisabledTap = onDisabledTapCallback,
                )
            }
        }
        val node = composeRule.onNodeWithContentDescription(notSetUp)

        // Press (finger down) — nothing has fired yet (release-gated, matches the file's convention).
        node.performTouchInput { down(center) }
        composeRule.waitForIdle()
        assertEquals("must not fire on press alone", 0, disabledTapA)
        assertEquals("must not fire on press alone", 0, disabledTapB)

        // Mid-press: swap the onDisabledTap identity to a DISTINCT counter and force recomposition
        // while the gesture is still in flight.
        swapToDisabledTapB()
        composeRule.waitForIdle()

        // Release — must dispatch through the LATEST closure (disabledTapB), never the stale one
        // (disabledTapA).
        node.performTouchInput { up() }
        composeRule.waitForIdle()
        assertEquals(
            "release must invoke the latest onDisabledTap closure (disabledTapB)",
            1,
            disabledTapB,
        )
        assertEquals(
            "release must never invoke the stale onDisabledTap closure (disabledTapA)",
            0,
            disabledTapA,
        )
    }

    @Test
    fun tap_midPressEnabledFlip_firesOnlyOnDisabledTap() {
        var tapped = 0
        var disabledTap = 0
        lateinit var disableMidPress: () -> Unit
        composeRule.setContent {
            MaterialTheme {
                var isEnabled by remember { mutableStateOf(true) }
                disableMidPress = { isEnabled = false }
                MicButton(
                    isListening = false,
                    enabled = isEnabled,
                    onTap = { tapped++ },
                    onDisabledTap = { disabledTap++ },
                )
            }
        }
        // Use `hasClickAction()` (not content-description) as the node matcher: flipping `enabled`
        // mid-press swaps the rendered icon's contentDescription (via Crossfade) from "Tap to talk"
        // to the disabled string, which would break a content-description-based lookup for the
        // later `up()` call. The semantics click action (CR-01) is stable across that flip since
        // it's always present regardless of `enabled`.
        val node = composeRule.onNode(hasClickAction())

        // Press (finger down) while still enabled — nothing has fired yet (release-gated).
        node.performTouchInput { down(center) }
        composeRule.waitForIdle()
        assertEquals("must not fire on press alone", 0, tapped)
        assertEquals("must not fire on press alone", 0, disabledTap)

        // Mid-press: flip `enabled` to false while the gesture is still in flight. This is the
        // literal scenario `latestEnabled` (rememberUpdatedState) exists to fix: the gesture
        // coroutine must NOT restart (it is keyed on Unit, not `enabled`), so the in-flight press
        // survives the flip and dispatch reads the latest `enabled` value at release time.
        disableMidPress()
        composeRule.waitForIdle()

        // Release — must dispatch through onDisabledTap (the LATEST enabled state), never onTap
        // (the state captured when the gesture began).
        node.performTouchInput { up() }
        composeRule.waitForIdle()
        assertEquals("release must invoke onDisabledTap per the latest enabled=false", 1, disabledTap)
        assertEquals("release must never invoke onTap once enabled flipped false mid-press", 0, tapped)
    }
}
