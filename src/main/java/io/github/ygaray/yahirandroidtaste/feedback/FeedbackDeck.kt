package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth

/** Test tag on the deck container of [FeedbackStackHost]. */
internal const val FEEDBACK_STACK_DECK_TEST_TAG = "feedback_stack_deck"

/**
 * The Direction A deck layout (D-01): [items] are listed newest first, the newest card sits lowest
 * at its natural height and every older card tucks behind it, its top edge one peek strip above the
 * next newer card, scaled about its top center and faded.
 *
 * Older cards are measured at the newest card's height, so a taller older card can never show more
 * than its strip or hang below the front card (the clamp). Cards share one width, the smaller of the
 * incoming width and [FEEDBACK_DECK_MAX_WIDTH], centered. No items lay out as 0 by 0.
 *
 * Composition and the semantics tree stay newest first: every slot carries a `traversalIndex` equal
 * to its list index, because the draw order (zIndex) would otherwise reverse the reading order.
 */
@Composable
internal fun FeedbackDeck(
    items: List<FeedbackStackItem>,
    modifier: Modifier = Modifier,
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
            .testTag(FEEDBACK_STACK_DECK_TEST_TAG)
            .semantics { isTraversalGroup = true }
    ) { measurables, constraints ->
        val count = measurables.size
        if (count == 0) {
            return@Layout layout(constraints.constrainWidth(0), constraints.constrainHeight(0)) {}
        }
        val peekPx = FEEDBACK_PEEK_HEIGHT.roundToPx()
        val capPx = FEEDBACK_DECK_MAX_WIDTH.roundToPx()
        val available = if (constraints.hasBoundedWidth) constraints.maxWidth else capPx
        val cardWidth = minOf(available, capPx)
        val deckWidth = constraints.constrainWidth(if (constraints.hasBoundedWidth) available else cardWidth)

        val front = measurables[0].measure(
            Constraints(minWidth = cardWidth, maxWidth = cardWidth, maxHeight = constraints.maxHeight)
        )
        val placeables = buildList<Placeable> {
            add(front)
            for (i in 1 until count) {
                add(measurables[i].measure(Constraints.fixed(cardWidth, front.height)))
            }
        }
        val deckHeight = constraints.constrainHeight(feedbackCollapsedHeight(front.height, count, peekPx))
        layout(deckWidth, deckHeight) {
            val x = (deckWidth - cardWidth) / 2
            placeables.forEachIndexed { depth, placeable ->
                val y = (count - 1 - depth) * peekPx
                placeable.placeWithLayer(x, y, zIndex = (count - depth).toFloat()) {
                    val scale = feedbackDepthScale(depth)
                    scaleX = scale
                    scaleY = scale
                    alpha = feedbackDepthAlpha(depth)
                    transformOrigin = TransformOrigin(0.5f, 0f)
                }
            }
        }
    }
}
