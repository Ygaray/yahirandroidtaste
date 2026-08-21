package io.github.ygaray.yahirandroidtaste.modifier

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * The three rest detents of [RevealActionRow]'s hoisted [AnchoredDraggableState].
 *
 * Named distinctly from [SwipeAnchor] (this file's sibling in the same package) to avoid any
 * collision — [RevealActionRow] and [SwipeableActionRow] are two independent APIs, not variants
 * of one shared state model.
 *
 * - [StartRevealed]: the content has slid right, exposing [RevealActionRow]'s `startAction` slot.
 * - [Resting]: the content is centered; no action slot is exposed.
 * - [EndRevealed]: the content has slid left, exposing [RevealActionRow]'s `endAction` slot.
 */
enum class RevealAnchor { StartRevealed, Resting, EndRevealed }

/**
 * A new, additive swipe-reveal sibling absorbing CalTracker's `SwipeRevealRow` call shape
 * (v1.6 give-leg, GIVE-02/D-01): arbitrary 0-2 action slots, a hoisted [AnchoredDraggableState],
 * a local 64dp `actionWidth` default, an explicit [contentBackground], and opt-in haptics.
 *
 * [SwipeableActionRow] (this package's other gesture wrapper) is NOT modified by this file in
 * any way — its fixed Delete/Edit pair, `openRowState`-owned single-open policy, and mandatory
 * haptics stay exactly as they are for SecondBrain's pinned build (GIVE-03). The two APIs diverge
 * too deeply to unify by parameter; this is a genuinely new sibling, not a superset signature.
 *
 * Reveal-then-confirm contract (the one true shared invariant with both [SwipeableActionRow] and
 * CalTracker's `SwipeRevealRow` — must hold byte-identically): swipe reveals a slot; the action
 * fires ONLY via [startAction]'s or [endAction]'s own `clickable`; the drag settling into a
 * revealed anchor never itself invokes an action. This composable never adds a settle/commit
 * callback of its own.
 *
 * @param state per-row hoisted draggable state, host-owned (mirrors `SwipeRevealRow`'s hoisting
 *   model, not [SwipeableActionRow]'s internally-created-and-registered `openRowState` model).
 * @param modifier applied to the outer [Box].
 * @param startAction revealed by swipe right (leading edge). Null = no start action / no right
 *   reveal — only a detent a caller actually supplied a slot for is installed as an anchor.
 * @param endAction revealed by swipe left (trailing edge). Null = no end action / no left reveal.
 * @param actionWidth width of each action zone. Defaults to a LOCAL 64dp literal — deliberately
 *   NOT sourced from the hub's shared `Dimens.SwipeReveal.ButtonWidth` (72dp), which stays scoped
 *   to [SwipeableActionRow]'s own contract. This matches CalTracker's current `LogRow`/
 *   `FoodListRow` call sites exactly.
 * @param contentBackground opaque fill painted behind [content] so the resting row fully covers
 *   both action zones (actions stay hidden until a swipe; a transparent value would show a hole
 *   mid-drag).
 * @param hapticsEnabled opt-in threshold haptic, default OFF. When true, reuses the already-tested
 *   [thresholdSide]/[ThresholdSide] helpers declared in [SwipeableActionRow]'s own file (same
 *   package, no import needed) to fire [HapticFeedbackType.GestureThresholdActivate] once per
 *   side-change — the identical pattern [SwipeableActionRow] already uses. When false (the
 *   default), fires nothing at all, matching CalTracker's current zero-haptics `SwipeRevealRow`
 *   behavior byte-for-byte.
 * @param content the row body laid on top of the action zones.
 */
@Composable
fun RevealActionRow(
    state: AnchoredDraggableState<RevealAnchor>,
    modifier: Modifier = Modifier,
    startAction: (@Composable () -> Unit)? = null,
    endAction: (@Composable () -> Unit)? = null,
    actionWidth: Dp = 64.dp,
    contentBackground: Color = MaterialTheme.colorScheme.background,
    hapticsEnabled: Boolean = false,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val widthPx = with(density) { actionWidth.toPx() }

    // Recompute anchors whenever the measured width changes. Only the detents the consumer
    // actually supplied a slot for are reachable, so a row with no startAction cannot be dragged
    // toward it — matching SwipeRevealRow's own conditional-anchor behavior.
    SideEffect {
        state.updateAnchors(
            DraggableAnchors {
                if (startAction != null) RevealAnchor.StartRevealed at widthPx
                RevealAnchor.Resting at 0f
                if (endAction != null) RevealAnchor.EndRevealed at -widthPx
            }
        )
    }

    if (hapticsEnabled) {
        val hapticFeedback = LocalHapticFeedback.current
        val deadbandPx = with(density) { 8.dp.toPx() }
        var lastSide by remember { mutableStateOf(ThresholdSide.None) }
        LaunchedEffect(state) {
            snapshotFlow { state.requireOffset() }.collect { offset ->
                val currentSide = thresholdSide(offsetPx = offset, deadbandPx = deadbandPx)
                if (currentSide != lastSide && currentSide != ThresholdSide.None) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                }
                lastSide = currentSide
            }
        }
    }

    // clipToBounds so the full-width foreground can't bleed past the row as it slides ±actionWidth.
    Box(modifier = modifier.clipToBounds()) {
        // Background action zones sit BEHIND the content — start at the leading edge, end at the
        // trailing edge, a weight(1f) spacer keeps them pinned as the content slides.
        Row(modifier = Modifier.matchParentSize()) {
            if (startAction != null) {
                Box(
                    modifier = Modifier
                        .width(actionWidth)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center,
                ) { startAction() }
            }
            Spacer(Modifier.weight(1f))
            if (endAction != null) {
                Box(
                    modifier = Modifier
                        .width(actionWidth)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center,
                ) { endAction() }
            }
        }

        // Foreground content carries the offset (driven by the draggable state) + the gesture.
        // fillMaxWidth + an opaque background make the resting row fully cover the action zones —
        // they only appear once the content slides away on a swipe (reveal-then-confirm).
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(state.requireOffset().roundToInt(), 0) }
                .background(contentBackground)
                .anchoredDraggable(state, Orientation.Horizontal)
        ) { content() }
    }
}
