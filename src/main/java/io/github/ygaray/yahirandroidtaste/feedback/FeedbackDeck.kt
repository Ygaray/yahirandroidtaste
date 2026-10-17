package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import io.github.ygaray.yahirandroidtaste.theme.ExpressiveMotion
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

/** Test tag on the deck container of [FeedbackStackHost]. */
internal const val FEEDBACK_STACK_DECK_TEST_TAG = "feedback_stack_deck"

/** How long the scroll-to-newest after a fan waits for the fan spring to settle. */
private const val FAN_SETTLE_TIMEOUT_MILLIS = 2_000L

/**
 * The Direction A deck layout (D-01, D-08): [items] are listed newest first, the newest card sits
 * lowest at its natural height and every older card tucks behind it, its top edge one peek strip
 * above the next newer card, scaled about its top center and faded.
 *
 * Collapsed, older cards are measured at the newest card's height, so a taller older card can never
 * show more than its strip or hang below the front card (the clamp). With [fanned] the cards spring
 * into a bottom-anchored column of natural-height cards, oldest on top, separated by
 * [FEEDBACK_FANNED_GAP]; the fanned deck is capped at 60 percent of the available height minus the
 * keyboard inset and its cards scroll inside the cap, newest in view. An unbounded height (a deck
 * inside a scrolling parent) is neither capped nor scrolled. Cards share one width, the smaller of
 * the incoming width and [FEEDBACK_DECK_MAX_WIDTH], centered. No items lay out as 0 by 0.
 *
 * The host adds no window-inset padding of its own: a Scaffold snackbar slot is already lifted above
 * the system bars, and the keyboard only lowers the cap.
 *
 * Composition and the semantics tree stay newest first: every slot carries a `traversalIndex` equal
 * to its list index, because the draw order (zIndex) would otherwise reverse the reading order. No
 * exit animation is added: a resolved card leaves at once.
 *
 * [onPointerChange] reports whether any pointer is pressed anywhere inside the deck (a card body, a
 * peek strip, an Undo button). It is observed on the initial pass without consuming anything, so
 * the cards' own gestures are untouched, and it reports `false` again on release, on cancellation
 * and when the deck leaves composition mid-press, so a pause driven by it cannot stick.
 */
@Composable
internal fun FeedbackDeck(
    items: List<FeedbackStackItem>,
    modifier: Modifier = Modifier,
    fanned: Boolean = false,
    onPointerChange: (Boolean) -> Unit = {},
    card: @Composable (item: FeedbackStackItem, index: Int) -> Unit
) {
    // One spring drives both geometries; the State object (not its value) goes to the layout so only
    // layout and draw rerun per frame.
    val fan = animateFloatAsState(
        targetValue = if (fanned) 1f else 0f,
        animationSpec = ExpressiveMotion.emphasizedSpatialSpec,
        label = "feedback-deck-fan"
    )
    val ime = WindowInsets.ime
    val scroll = rememberScrollState()
    val latestOnPointerChange by rememberUpdatedState(onPointerChange)
    ScrollToNewestWhenFanned(fanned = fanned, fan = fan, scroll = scroll)
    BoxWithConstraints(
        modifier = modifier
            .testTag(FEEDBACK_STACK_DECK_TEST_TAG)
            .semantics { isTraversalGroup = true }
            .pointerInput(Unit) {
                // Non-consuming, initial-pass observer: the first down anywhere pauses the deck, and
                // it stays paused until no pointer is pressed. It must not wait for up or cancellation
                // on the child pass, which resumes early when a child consumes a drag.
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    latestOnPointerChange(true)
                    try {
                        do {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                        } while (event.changes.any { it.pressed })
                    } finally {
                        latestOnPointerChange(false)
                    }
                }
            }
    ) {
        // A vertical scroll container refuses an infinite max height, so an unbounded host gets the
        // plain, uncapped deck.
        val capped = Modifier.fanHeightCap(fan, ime)
        val container = if (constraints.hasBoundedHeight) capped.verticalScroll(scroll) else capped
        DeckLayout(items = items, fan = fan, modifier = container, card = card)
    }
}

/** After the fan settles, brings the newest (bottom) card into view when the cards overflow the cap. */
@Composable
private fun ScrollToNewestWhenFanned(fanned: Boolean, fan: State<Float>, scroll: ScrollState) {
    LaunchedEffect(fanned) {
        if (fanned) {
            withTimeoutOrNull(FAN_SETTLE_TIMEOUT_MILLIS) {
                snapshotFlow { fan.value }.first { it >= 1f }
                withFrameNanos { }
                scroll.scrollTo(scroll.maxValue)
            }
        }
    }
}

/**
 * While the deck is fanning or fanned, limits the height the container may take to
 * [feedbackFannedCap]. Collapsed (progress exactly 0) or unbounded it passes constraints through.
 */
private fun Modifier.fanHeightCap(fan: State<Float>, ime: WindowInsets): Modifier =
    layout { measurable, constraints ->
        val limit = if (fan.value > 0f && constraints.hasBoundedHeight) {
            feedbackFannedCap(constraints.maxHeight, ime.getBottom(this))
        } else {
            constraints.maxHeight
        }
        val capped = constraints.copy(
            minHeight = minOf(constraints.minHeight, limit),
            maxHeight = minOf(constraints.maxHeight, limit)
        )
        val placeable = measurable.measure(capped)
        layout(placeable.width, placeable.height) { placeable.place(0, 0) }
    }

@Composable
private fun DeckLayout(
    items: List<FeedbackStackItem>,
    fan: State<Float>,
    modifier: Modifier,
    card: @Composable (item: FeedbackStackItem, index: Int) -> Unit
) {
    Layout(
        content = {
            items.forEachIndexed { index, item ->
                key(item.id) {
                    Box(Modifier.semantics { traversalIndex = index.toFloat() }) {
                        card(item, index)
                    }
                }
            }
        },
        modifier = modifier
    ) { measurables, constraints ->
        // The spring overshoots; a value outside 0..1 would place cards beyond both geometries.
        measureDeck(measurables, constraints, fan.value.coerceIn(0f, 1f))
    }
}

private fun MeasureScope.measureDeck(
    measurables: List<Measurable>,
    constraints: Constraints,
    progress: Float
): MeasureResult {
    val count = measurables.size
    if (count == 0) {
        return layout(constraints.constrainWidth(0), constraints.constrainHeight(0)) {}
    }
    val peekPx = FEEDBACK_PEEK_HEIGHT.roundToPx()
    val gapPx = FEEDBACK_FANNED_GAP.roundToPx()
    val capPx = FEEDBACK_DECK_MAX_WIDTH.roundToPx()
    val available = if (constraints.hasBoundedWidth) constraints.maxWidth else capPx
    val cardWidth = minOf(available, capPx)
    val deckWidth = constraints.constrainWidth(if (constraints.hasBoundedWidth) available else cardWidth)
    val natural = Constraints(minWidth = cardWidth, maxWidth = cardWidth, maxHeight = constraints.maxHeight)

    val front = measurables[0].measure(natural)
    val placeables = buildList<Placeable> {
        add(front)
        for (i in 1 until count) {
            // Collapsed: clamp to the front height. Any fan progress: natural height.
            val sized = if (progress == 0f) Constraints.fixed(cardWidth, front.height) else natural
            add(measurables[i].measure(sized))
        }
    }
    val collapsedHeight = feedbackCollapsedHeight(front.height, count, peekPx)
    val fannedHeight = feedbackFannedHeight(placeables.map { it.height }, gapPx)
    val deckHeight = constraints.constrainHeight(feedbackLerp(collapsedHeight, fannedHeight, progress))
    return layout(deckWidth, deckHeight) {
        val x = (deckWidth - cardWidth) / 2
        var fannedDistance = 0
        placeables.forEachIndexed { depth, placeable ->
            fannedDistance += placeable.height + if (depth > 0) gapPx else 0
            val collapsedDistance = front.height + depth * peekPx
            val distance = feedbackLerp(collapsedDistance, fannedDistance, progress)
            placeable.placeWithLayer(x, deckHeight - distance, zIndex = (count - depth).toFloat()) {
                val scale = feedbackLerp(feedbackDepthScale(depth), 1f, progress)
                scaleX = scale
                scaleY = scale
                alpha = feedbackLerp(feedbackDepthAlpha(depth), 1f, progress)
                transformOrigin = TransformOrigin(0.5f, 0f)
            }
        }
    }
}
