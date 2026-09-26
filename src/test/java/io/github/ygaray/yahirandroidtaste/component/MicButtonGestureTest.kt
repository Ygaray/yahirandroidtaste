package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.material3.MaterialTheme
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
    private val notSetUp = "Voice not set up — open Settings"

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
}
