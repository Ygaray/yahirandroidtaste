package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * A generic tap-to-talk mic control (MIC-01): a single tap invokes [onTap] to start/toggle
 * listening; the icon crossfades mic -> stop while [isListening] so the active state is visible.
 * When [enabled] is false the control renders a disabled mic-off affordance and a tap resolves to
 * [onDisabledTap] instead. This composable is consumer-agnostic — it carries no permission logic
 * and no domain concept; a caller owns whatever gating (permission, configuration, feature flag,
 * etc.) it needs and maps that onto [enabled] before calling this. [onTap] and [onDisabledTap] are
 * dispatched via `rememberUpdatedState` (see the gesture block below), so a caller that swaps
 * either lambda's identity mid-press — e.g. from external recomposition — always has the LATEST
 * closure fire at release, never a stale one captured when the gesture began.
 *
 * The three rendered content descriptions ([disabledDescription], [tapToTalkDescription],
 * [listeningDescription]) are also caller-suppliable, with generic, hub-neutral defaults; a
 * consumer that needs domain-specific microcopy (e.g. pointing the user at a settings screen) can
 * override any subset without touching the others.
 *
 * ## Why a Surface and not `FloatingActionButton` (voice-mic-press-no-capture fix)
 * This was originally a `FloatingActionButton(onClick = …)` with a custom `modifier.pointerInput { … }`
 * on top. That put TWO gesture owners on the same node — the FAB's internal `clickable` and our
 * `detectTapGestures` — and the internal clickable consumed the pointer-down first, so our
 * `detectTapGestures`' `awaitFirstDown(requireUnconsumed = true)` never fired: the tap callback was
 * never called and pressing the mic did nothing (only the FAB ripple showed). The fix renders the FAB
 * visuals with a plain [Surface] whose **single** [pointerInput] owns the ONE gesture, so nothing
 * pre-consumes the press. The ripple is reproduced explicitly via [indication] + a
 * [MutableInteractionSource] fed from the same gesture. Any future rewrite of what happens after a
 * genuine release must preserve this exact single-owner structure byte-for-byte in shape — only the
 * post-release behavior may change, never the gesture-ownership discipline itself.
 *
 * @param disabledDescription accessibility label for the mic-off icon shown while [enabled] is
 * false. Defaults to a generic, hub-neutral string.
 * @param tapToTalkDescription accessibility label for the idle mic icon shown while [enabled] is
 * true and [isListening] is false. Defaults to a generic, hub-neutral string.
 * @param listeningDescription accessibility label for the stop icon shown while [isListening] is
 * true. Defaults to a generic, hub-neutral string.
 */
@Composable
fun MicButton(
    isListening: Boolean,
    enabled: Boolean = true,
    onTap: () -> Unit,
    onDisabledTap: () -> Unit = {},
    modifier: Modifier = Modifier,
    disabledDescription: String = "Microphone unavailable",
    tapToTalkDescription: String = "Tap to talk",
    listeningDescription: String = "Listening…",
) {
    val interactionSource = remember { MutableInteractionSource() }
    // Read via rememberUpdatedState instead of keying pointerInput on `enabled` directly. Keying
    // on `enabled` would restart (cancel) the gesture coroutine the instant it flips mid-press —
    // and a cancellation arriving between `interactionSource.emit(press)` and the paired
    // Release/Cancel emit below would leave an unterminated Press interaction, sticking the
    // ripple/pressed visual indefinitely. Reading the latest value through this state instead
    // keeps the SAME gesture coroutine alive for the whole press, so the `try`/`finally`-shaped
    // emit pair below always completes.
    val latestEnabled by rememberUpdatedState(enabled)
    // Same rationale, applied to the two dispatch callbacks: the gesture coroutine is keyed on
    // Unit and never restarts, so a caller that swaps `onTap`/`onDisabledTap` identity mid-press
    // (e.g. from external recomposition) must still have the release-time dispatch read the
    // LATEST closure, never the one captured when the coroutine launched.
    val latestOnTap by rememberUpdatedState(onTap)
    val latestOnDisabledTap by rememberUpdatedState(onDisabledTap)
    val containerColor = when {
        !enabled -> MaterialTheme.colorScheme.surfaceVariant
        isListening -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    val contentColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant
        isListening -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = FloatingActionButtonDefaults.shape,
        shadowElevation = 6.dp,
        modifier = modifier
            .size(56.dp)
            // Semantics-only click action: gives TalkBack an ACTION_CLICK to invoke and gives
            // keyboard/D-pad focus + Enter/Space a target, WITHOUT adding `clickable`/
            // `combinedClickable` — either of which would reintroduce a second touch-gesture owner
            // on this node (the exact voice-mic-press-no-capture bug described above). The
            // AccessibilityService dispatches this via `performAction(ACTION_CLICK)` on the
            // semantics node — a separate code path from the raw pointer-event stream `pointerInput`
            // below consumes, so this cannot race or double-fire against the touch gesture.
            .semantics(mergeDescendants = true) {
                role = Role.Button
                onClick {
                    if (latestEnabled) latestOnTap() else latestOnDisabledTap()
                    true
                }
            }
            .focusable(interactionSource = interactionSource)
            .indication(interactionSource, ripple())
            // ONE pointer-input owner for the ONE gesture — no internal clickable to pre-consume the
            // down (the voice-mic-press-no-capture root cause). Everything happens in `onPress`, gated
            // on a genuine release (`tryAwaitRelease()`); `detectTapGestures` needs no separate `onTap`
            // parameter. Keyed on Unit — NOT `enabled`/`onTap`/`onDisabledTap` — so an `enabled` flip or
            // a callback-identity swap mid-press can never cancel an in-flight gesture before its
            // paired Release/Cancel interaction emits; the latest values are read via `latestEnabled`/
            // `latestOnTap`/`latestOnDisabledTap` (rememberUpdatedState) instead.
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        val press = PressInteraction.Press(offset)
                        interactionSource.emit(press)
                        val released = tryAwaitRelease()
                        interactionSource.emit(
                            if (released) PressInteraction.Release(press) else PressInteraction.Cancel(press),
                        )
                        // Act ONLY on a genuine release — a drag-away/cancel never calls anything.
                        if (released) {
                            if (latestEnabled) latestOnTap() else latestOnDisabledTap()
                        }
                    },
                )
            },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Crossfade(targetState = Pair(enabled, isListening), label = "mic-button-icon") { (isEnabled, listening) ->
                when {
                    !isEnabled -> Icon(Icons.Default.MicOff, contentDescription = disabledDescription)
                    listening -> Icon(Icons.Default.Stop, contentDescription = listeningDescription)
                    else -> Icon(Icons.Default.Mic, contentDescription = tapToTalkDescription)
                }
            }
        }
    }
}
