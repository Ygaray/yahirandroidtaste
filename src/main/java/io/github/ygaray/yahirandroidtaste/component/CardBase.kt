package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.modifier.SwipeAnchor
import io.github.ygaray.yahirandroidtaste.modifier.SwipeableActionRow
import io.github.ygaray.yahirandroidtaste.theme.Dimens

/**
 * Shared card shell composable for all CRUD cards across all modules.
 *
 * ## Swipe Standard (Universal — No Exceptions, Reveal-Then-Confirm)
 * - **Right swipe (RevealedRight)** = Edit — reveals a 72dp `primaryContainer` Edit button at start.
 *   Tapping the button fires [onEditClick].
 * - **Left swipe (RevealedLeft)** = Delete — reveals a 72dp `errorContainer` Delete button at end.
 *   Tapping the button fires [onDeleteClick].
 *
 * Swipe gesture is handled by [SwipeableActionRow]. Actions fire ONLY via button taps on the
 * revealed slot — never on swipe settle (SWIPE-01 contract).
 *
 * ## Three-Dot DropdownMenu (Single Combined-Row Trigger — G2-01/D-05/D-06)
 * - Use [dropdownMenuContent] to supply DropdownMenuItem entries.
 * - Always end with Delete as the last item, using `MaterialTheme.colorScheme.error` text + icon.
 * - Long-press on the card also opens the same menu.
 * - The header Row (when [headerContent] is non-null) renders ONLY the header content — no
 *   three-dot lives there anymore.
 * - The single `MoreVert` trigger lives in the trailing icon cluster of the combined bottom Row
 *   (see below), gated by [showThreeDot] and bound to the same `showMenu` state /
 *   [dropdownMenuContent]. Exactly one overflow trigger exists per card, regardless of whether
 *   [headerContent] is null.
 *
 * ## Combined bottom row (G2-01/D-05)
 * [tagRowContent] (leading) and [footerContent] + the [showThreeDot] `MoreVert` trigger (trailing)
 * are composed as siblings inside ONE `Row` with `Arrangement.SpaceBetween`, replacing the
 * previous two-tier layout (a separate `tagRowContent` Box stacked above a `footerContent` Row).
 * The row renders whenever any of {[tagRowContent] != null, [footerContent] != null,
 * [showThreeDot]} is true; when none are present, no row is rendered. An untagged card (no
 * [tagRowContent]) still renders its trailing icon cluster, right-aligned, since the leading slot
 * is always present as a (possibly empty) layout node for `SpaceBetween` to anchor against (D-05
 * empty-tag collapse). The row itself carries [Dimens.HorizontalPadding]/[Dimens.BottomPadding]
 * (plan 52-09) since per-card-type `footerContent` implementations no longer wrap their own
 * padded Row — this is now the single place that insets both the leading tag cluster and the
 * trailing icon cluster.
 *
 * @param showDragHandle Whether to render a drag handle icon (defaults false; reserved for future use).
 * @param showThreeDot Whether to render the MoreVert (three-dot) trigger in the combined bottom row's
 *   trailing icon cluster.
 * @param onDeleteClick Called when the revealed Delete button is tapped (left swipe → confirm).
 * @param onEditClick Called when the revealed Edit button is tapped (right swipe → confirm).
 * @param openRowState Single-open row reference hoisted at [CardListSection]. At most one row
 *   is non-Resting at any time.
 * @param onClick Called on single tap of the card face.
 * @param onLongClick Called on long-press of the card face.
 * @param dropdownMenuContent Content for the DropdownMenu; receives a dismissMenu callback.
 * @param headerContent Optional top row slot — renders inline in its own header Row when non-null.
 * @param bodyContent Optional middle slot — rendered inside a Box filling max width.
 * @param tagRowContent Optional cross-card-type tag-row slot — now the LEADING element of the
 *   combined bottom row (see above). Hosts [CardTagRow] (plan 51-02); lands inside the outer
 *   `combinedClickable` by construction and consumes its own touch via its own nested clickable —
 *   [CardBase]'s outer gesture handling is unaffected.
 * @param footerContent Optional bottom row slot — now the TRAILING element of the combined bottom
 *   row (see above), alongside the [showThreeDot] `MoreVert` trigger.
 * @param modifier Modifier applied to the outermost [SwipeableActionRow].
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CardBase(
    openRowState: MutableState<AnchoredDraggableState<SwipeAnchor>?>,
    modifier: Modifier = Modifier,
    // Behavior flags
    showDragHandle: Boolean = false,
    showThreeDot: Boolean = false,
    // Swipe callbacks — fire only via SwipeableActionRow button taps (SWIPE-01)
    onDeleteClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    // Card face callbacks
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    // DropdownMenu content — called inside the anchored Box; receives dismissMenu callback
    dropdownMenuContent: @Composable ColumnScope.(dismissMenu: () -> Unit) -> Unit = {},
    // Content slots
    headerContent: (@Composable RowScope.() -> Unit)? = null,
    bodyContent: (@Composable ColumnScope.() -> Unit)? = null,
    tagRowContent: (@Composable () -> Unit)? = null,
    footerContent: (@Composable RowScope.() -> Unit)? = null
) {
    val hapticFeedback = LocalHapticFeedback.current
    var showMenu by remember { mutableStateOf(false) }

    SwipeableActionRow(
        onDeleteClick = onDeleteClick,
        onEditClick = onEditClick,
        openRowState = openRowState,
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (showThreeDot) {
                            showMenu = true
                        }
                        onLongClick()
                    }
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                // Header slot — renders only headerContent; no overflow trigger here anymore
                // (the single MoreVert trigger now lives in the combined bottom row below).
                if (headerContent != null) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        headerContent()
                    }
                }

                // Body slot — unchanged
                if (bodyContent != null) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Column { bodyContent() }
                    }
                }

                // Combined bottom row (G2-01/D-05/D-06): tag cluster leading, footer content +
                // the single relocated MoreVert trigger trailing, one Row, SpaceBetween. Renders
                // whenever any of {tagRowContent, footerContent, showThreeDot} is present; the
                // leading Box is always emitted as a layout node (even when empty) so
                // SpaceBetween still right-aligns the trailing cluster on an untagged card.
                if (tagRowContent != null || footerContent != null || showThreeDot) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = Dimens.HorizontalPadding,
                                vertical = Dimens.BottomPadding
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f, fill = false)) {
                            if (tagRowContent != null) {
                                tagRowContent()
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (footerContent != null) {
                                footerContent()
                            }
                            if (showThreeDot) {
                                Box {
                                    IconButton(
                                        onClick = { showMenu = true }
                                        // No Modifier.size() — M3 default 48dp touch target (UIQ-01)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "More options",
                                            modifier = Modifier.size(Dimens.Icons.MenuIcon),
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false },
                                        content = { dropdownMenuContent { showMenu = false } }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
