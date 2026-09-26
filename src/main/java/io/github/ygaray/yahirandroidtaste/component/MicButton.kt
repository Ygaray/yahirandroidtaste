package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.animation.Crossfade
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
import androidx.compose.ui.unit.dp

/**
 * A generic tap-to-talk mic control (MIC-01): a single tap invokes [onTap] to start/toggle
 * listening; the icon crossfades mic -> stop while [isListening] so the active state is visible.
 * When [enabled] is false the control renders a disabled mic-off affordance and a tap resolves to
 * [onDisabledTap] instead. This composable is consumer-agnostic — it carries no permission logic
 * and no domain concept; a caller (e.g. CalTracker's `MicFab` wrapper) owns any RECORD_AUDIO
 * permission gating and maps its own "configured" concept onto [enabled] before calling this.
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
 */
@Composable
fun MicButton(
    isListening: Boolean,
    enabled: Boolean,
    onTap: () -> Unit,
    onDisabledTap: () -> Unit,
    modifier: Modifier = Modifier,
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
            .indication(interactionSource, ripple())
            // ONE pointer-input owner for the ONE gesture — no internal clickable to pre-consume the
            // down (the voice-mic-press-no-capture root cause). Everything happens in `onPress`, gated
            // on a genuine release (`tryAwaitRelease()`); `detectTapGestures` needs no separate `onTap`
            // parameter. Keyed on Unit — NOT `enabled` — so a config flip mid-press can never cancel
            // an in-flight gesture before its paired Release/Cancel interaction emits; the latest
            // `enabled` value is read via `latestEnabled` (rememberUpdatedState) instead.
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
                            if (latestEnabled) onTap() else onDisabledTap()
                        }
                    },
                )
            },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Crossfade(targetState = Pair(enabled, isListening), label = "mic-button-icon") { (isEnabled, listening) ->
                when {
                    !isEnabled -> Icon(Icons.Default.MicOff, contentDescription = "Voice not set up — open Settings")
                    listening -> Icon(Icons.Default.Stop, contentDescription = "Listening…")
                    else -> Icon(Icons.Default.Mic, contentDescription = "Tap to talk")
                }
            }
        }
    }
}
