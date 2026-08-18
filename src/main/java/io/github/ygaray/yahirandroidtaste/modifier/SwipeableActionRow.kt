package io.github.ygaray.yahirandroidtaste.modifier

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.theme.Dimens
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/**
 * Anchor enum for [SwipeableActionRow]'s three-position AnchoredDraggable state (D-01).
 *
 * - [Resting]: card face is centered (no offset). Default state.
 * - [RevealedLeft]: card face is shifted left (-ButtonWidth), revealing the Delete slot on the right edge.
 * - [RevealedRight]: card face is shifted right (+ButtonWidth), revealing the Edit slot on the left edge.
 *
 * Top-level in `core.ui.components` so CardBase, CardListSection, and any future consumers
 * can import it without referencing a nested class.
 */
enum class SwipeAnchor {
    Resting,
    RevealedLeft,
    RevealedRight
}

/**
 * Side classification used by [thresholdSide] to drive the D-08 threshold haptic.
 *
 * [None] means the drag offset is within the deadband — no side change, no haptic.
 * [Left] means the card face has been dragged right (revealing the left Delete slot).
 * [Right] means the card face has been dragged left (revealing the right Edit slot).
 *
 * Internal so unit tests in the same module can assert on specific variants without
 * exposing this implementation detail to callers outside the module.
 */
internal enum class ThresholdSide { None, Left, Right }

/**
 * Pure helper that classifies a horizontal drag offset into a [ThresholdSide].
 *
 * This is the JVM-extractable slice of the settle logic — no Compose types, no Android
 * runtime — so it can be verified by plain JUnit4 tests in [SwipeThresholdTest].
 *
 * @param offsetPx Signed horizontal offset in pixels. Negative = dragged left (Edit direction);
 *   positive = dragged right (Delete direction). Matches AnchoredDraggable convention where
 *   RevealedRight has a positive anchor offset.
 * @param deadbandPx Half-width of the center deadband in pixels. Offsets with |offsetPx| ≤
 *   deadbandPx report [ThresholdSide.None] — preventing premature haptic firing on small
 *   or neutral drags.
 * @return [ThresholdSide.Left] when `offsetPx < -deadbandPx`,
 *   [ThresholdSide.Right] when `offsetPx > deadbandPx`, else [ThresholdSide.None].
 */
internal fun thresholdSide(offsetPx: Float, deadbandPx: Float): ThresholdSide = when {
    offsetPx < -deadbandPx -> ThresholdSide.Left
    offsetPx > deadbandPx -> ThresholdSide.Right
    else -> ThresholdSide.None
}

/**
 * Pure JVM helper (D-15) deciding which [SwipeAnchor]s are installed on the
 * [AnchoredDraggableState] for a given row.
 *
 * [SwipeAnchor.RevealedRight] (the Edit slot) is installed ONLY when an edit handler is
 * present (`hasEditAction`). This gates the anchor at the physics layer — not just the
 * render — so a delete-only row (`onEditClick == null`) physically cannot settle into
 * RevealedRight, no Edit slot ever renders, and no Edit-side threshold haptic can fire
 * (Pitfall 4 / D-15 null-input edge). [SwipeAnchor.Resting] and [SwipeAnchor.RevealedLeft]
 * (the Delete slot) are always installed — delete is never optional on this primitive.
 *
 * No Compose runtime types involved — verified by plain JUnit4 tests in [SwipeThresholdTest].
 *
 * @param hasEditAction Whether a non-null `onEditClick` handler was supplied.
 * @return the set of [SwipeAnchor]s that should be installed via `updateAnchors`.
 */
internal fun installedAnchors(hasEditAction: Boolean): Set<SwipeAnchor> = if (hasEditAction) {
    setOf(SwipeAnchor.Resting, SwipeAnchor.RevealedLeft, SwipeAnchor.RevealedRight)
} else {
    setOf(SwipeAnchor.Resting, SwipeAnchor.RevealedLeft)
}

/**
 * Gesture wrapper that implements the SWIPE-01 reveal-then-confirm contract using
 * [AnchoredDraggableState] with three anchors (D-01): [SwipeAnchor.Resting], [SwipeAnchor.RevealedLeft],
 * and [SwipeAnchor.RevealedRight].
 *
 * ## Architecture
 * The card face ([content]) is the foreground layer inside an [AnchoredDraggable] Box. Two 72dp peek
 * action slots (Delete and Edit) are the background layer, visible only when the card face slides
 * past them. **Actions fire exclusively from the button's [clickable] — never on anchor settle
 * (swipe alone is structurally incapable of triggering Delete or Edit, satisfying SWIPE-01).**
 *
 * ## Decision compliance
 * - D-01: Three anchors (`Resting at 0f`, `RevealedLeft at -ButtonWidth`, `RevealedRight at +ButtonWidth`); positionalThreshold = 0.5 (default).
 * - D-02: Single-open policy — [openRowState] tracks the currently-open row; settling non-Resting closes the previous one. **This is also the primary mitigation for the D-13 LazyColumn `animateItem()` offset race**: the placement animation (item bounds) and the AnchoredDraggable offset (content transform) operate at different layout levels and do not structurally conflict. Delete-while-open visual flash (if any) and rapid-scroll-dismiss behavior are confirmed by Gate-1 on-device testing.
 * - D-07: Fixed 72dp peek slot via [Dimens.SwipeReveal.ButtonWidth]; icon-only, not full-bleed.
 * - D-08: [HapticFeedbackType.GestureThresholdActivate] fires once per side-change inside the [snapshotFlow] collector — never in the composition body, never on oscillation.
 * - D-09: [Spring.DampingRatioNoBouncy] + [Spring.StiffnessMedium] for all transitions.
 *
 * ## Thread safety
 * Null-safe reads on [openRowState].value before [animateTo] guard against evicted-state crashes
 * (T-31-02 from plan threat model).
 *
 * @param onDeleteClick Invoked when the revealed Delete slot is tapped.
 * @param onEditClick Invoked when the revealed Edit slot is tapped. `null` (D-15) puts the
 *   row in delete-only mode: the RevealedRight anchor is omitted entirely (not just the
 *   render), so no Edit slot can ever be revealed and no Edit-side haptic can fire.
 * @param openRowState Hoisted single-open reference, owned by the LazyColumn host
 *   ([CardListSection]). At most one row is non-Resting at a time.
 * @param modifier Applied to the outer [Box] containing both background slots and card face.
 * @param content The card face composable — typically a [CardBase] call.
 */
@Composable
fun SwipeableActionRow(
    onDeleteClick: () -> Unit,
    onEditClick: (() -> Unit)? = null,
    openRowState: MutableState<AnchoredDraggableState<SwipeAnchor>?>,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val density = LocalDensity.current
    val hapticFeedback = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    // D-09: NoBouncy spring matches LazyColumn scroll feel; StiffnessMedium gives responsive snap
    val animSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
    val deadbandPx = with(density) { 8.dp.toPx() }

    // State creation — non-deprecated form (BOM 2026.02.01 = Foundation ~1.9+):
    // constructor takes only initialValue; anchors are supplied via updateAnchors in SideEffect
    val state = remember { AnchoredDraggableState(initialValue = SwipeAnchor.Resting) }
    val revealPx = with(density) { Dimens.SwipeReveal.ButtonWidth.toPx() }
    // D-15: hasEditAction gates the RevealedRight anchor at the physics layer via the pure
    // installedAnchors() helper — a null onEditClick means RevealedRight is never installed,
    // so the row structurally cannot settle into it (Pitfall 4).
    val hasEditAction = onEditClick != null
    SideEffect {
        val anchors = installedAnchors(hasEditAction)
        state.updateAnchors(DraggableAnchors {
            if (SwipeAnchor.Resting in anchors) SwipeAnchor.Resting at 0f
            if (SwipeAnchor.RevealedLeft in anchors) SwipeAnchor.RevealedLeft at -revealPx
            if (SwipeAnchor.RevealedRight in anchors) SwipeAnchor.RevealedRight at revealPx
        })
    }

    // D-02: Single-open policy — when this row settles to non-Resting, animate the prior
    // open row back to Resting and register this row as the open one. When this row returns
    // to Resting, clear the reference (null-safe guard per T-31-02).
    LaunchedEffect(state.settledValue) {
        if (state.settledValue != SwipeAnchor.Resting) {
            val prior = openRowState.value
            if (prior != null && prior !== state) {
                prior.animateTo(SwipeAnchor.Resting)
            }
            openRowState.value = state
        } else if (openRowState.value === state) {
            openRowState.value = null
        }
    }

    // D-08: Threshold haptic — fire GestureThresholdActivate once per side-change only.
    // Runs inside snapshotFlow so requireOffset() is NOT read in the composition body
    // (avoids @FrequentlyChangingValue excessive recomposition — RESEARCH Pitfall 1).
    var lastSide by remember { mutableStateOf(ThresholdSide.None) }
    LaunchedEffect(state) {
        snapshotFlow { state.requireOffset() }.collect { offset ->
            val currentSide = thresholdSide(offsetPx = offset, deadbandPx = deadbandPx)
            // UIBUG-02 (D-04): only fire haptic when ENTERING a revealed side (Left or Right).
            // Adding `currentSide != ThresholdSide.None` suppresses the spurious buzz that
            // previously fired when the card returned to None (programmatic close / settle-back).
            // Moving `lastSide = currentSide` OUTSIDE the if-block is equally critical: it
            // records None transitions so that re-entering the same side after passing through
            // None re-triggers the haptic (if we skipped None the second entry would see
            // currentSide == lastSide and fire nothing — Pitfall 2 from RESEARCH).
            // D-15: suppress the Edit-side (Right) haptic entirely when no edit handler is
            // present — RevealedRight isn't an installed anchor in that mode, so no Edit-side
            // threshold activation should ever be felt (Pitfall 4).
            val shouldFireHaptic = currentSide != lastSide &&
                currentSide != ThresholdSide.None &&
                (currentSide != ThresholdSide.Right || hasEditAction)
            if (shouldFireHaptic) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
            }
            lastSide = currentSide  // unconditional — records every transition including None
        }
    }

    // derivedStateOf: the boolean is recomputed lazily in snapshot scope, but the read that
    // triggers it (the `if (showEditSlot)` below) executes in the composition body. Anchors are
    // installed in the SideEffect above, which runs AFTER composition, so on the very first frame
    // the offset is still NaN. Use the non-throwing `state.offset` and treat NaN (uninitialized)
    // as "no slot" — slots stay hidden until anchors land. `requireOffset()` would throw here.
    // Only triggers recomposition when the boolean value actually flips (slot appears/disappears),
    // not on every animation frame — avoids the @FrequentlyChangingValue lint (Pitfall 1).
    val showDeleteSlot by remember { derivedStateOf { state.offset.let { !it.isNaN() && it < 0f } } }
    // D-15: showEditSlot is additionally gated on hasEditAction — belt-and-suspenders with the
    // anchor-level gate above, since the RevealedRight anchor is never installed in delete-only
    // mode and the render must never race ahead of a null handler.
    val showEditSlot by remember { derivedStateOf { hasEditAction && state.offset.let { !it.isNaN() && it > 0f } } }

    Box(modifier = modifier) {
        // Background layer: 72dp peek action slots — drawn BEHIND the sliding card face (D-07)
        Row(modifier = Modifier.matchParentSize()) {
            // Edit slot: visible on the left when the card face is dragged right (positive offset)
            if (showEditSlot) {
                Box(
                    modifier = Modifier
                        .width(Dimens.SwipeReveal.ButtonWidth)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable {
                            // showEditSlot is already gated on hasEditAction, so onEditClick is
                            // never null when this slot is reachable — the ?.invoke() is a
                            // defensive no-op, never a silent failure path.
                            onEditClick?.invoke()
                            // Return to Resting after action — card snaps back cleanly
                            scope.launch { state.animateTo(SwipeAnchor.Resting, animSpec) }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(Dimens.SwipeReveal.IconSize)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Delete slot: visible on the right when the card face is dragged left (negative offset)
            if (showDeleteSlot) {
                Box(
                    modifier = Modifier
                        .width(Dimens.SwipeReveal.ButtonWidth)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .clickable {
                            onDeleteClick()
                            // Return to Resting after action — card snaps back cleanly
                            scope.launch { state.animateTo(SwipeAnchor.Resting, animSpec) }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(Dimens.SwipeReveal.IconSize)
                    )
                }
            }
        }

        // Foreground layer: card face — slides horizontally over the background slots.
        // requireOffset() is ONLY read inside the offset{} layout lambda and the anchoredDraggable
        // modifier — never in the composition body (RESEARCH Pitfall 1 compliance).
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(state.requireOffset().roundToInt(), 0) }
                .anchoredDraggable(state, Orientation.Horizontal),
            content = content
        )
    }
}
