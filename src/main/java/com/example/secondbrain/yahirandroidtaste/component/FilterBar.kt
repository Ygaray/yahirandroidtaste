package com.example.secondbrain.yahirandroidtaste.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Domain-free expand/collapse filter-bar shell (GADGET-02, D-01), generalized from the former
 * `:app`-resident `TagFilterBar`'s outer [Surface] + [FlowRow] chevron/wrap-and-scroll structure.
 *
 * Per locked decision D-01, this is a shell-only extraction: breadcrumb crumbs, co-occurrence
 * chips, and any other tag-graph-specific content stay in `:app` and are supplied entirely
 * through the [content] slot. [FilterBar] itself carries no filter-item-typed parameter — `T` is
 * inferred purely from the caller's [content] lambda (e.g. `FilterBar<TagEntity> { ... }`), never
 * "used up" by a reintroduced domain param like `drillPath` or `sortMode` (Pitfall 2/6).
 *
 * Collapsed, content clips to a single line (`maxLines = 1`); expanded, it wraps freely,
 * height-capped to ~40% of the screen and scrolling internally so a large item set never
 * overflows the screen.
 *
 * @param expanded  Whether the bar is expanded (wrap-all) vs. collapsed (single line).
 * @param onExpand  Called when the leading chevron is tapped while collapsed (down arrow).
 * @param onCollapse Called when the leading chevron is tapped while expanded (up arrow).
 * @param modifier  Optional modifier for the outer [Surface].
 * @param filterContentDescription Accessibility label announced for the outer [Surface]. Defaults
 *   to the tag-domain copy that every current call site relies on; non-tag consumers should
 *   override this with copy appropriate to their own `T`.
 * @param content   Caller-supplied [FlowRowScope] content — the filter/sort controls, crumbs, and
 *   suggestion chips for the caller's own item type.
 */
@Suppress("unused") // T is inferred from `content`'s captured call-site variables — see D-01 above.
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> FilterBar(
    expanded: Boolean,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier,
    filterContentDescription: String = "Tag filters",
    content: @Composable FlowRowScope.() -> Unit
) {
    // Expanded region is bounded to ~40% of screen height and scrolls internally, so a large
    // content set never overflows the screen and the trailing content stays reachable.
    val maxExpandedHeight = (LocalConfiguration.current.screenHeightDp * 0.4f).dp

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = filterContentDescription },
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (expanded) {
                        Modifier
                            .heightIn(max = maxExpandedHeight)
                            .verticalScroll(rememberScrollState())
                    } else {
                        Modifier
                    }
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            // Tight gap between wrapped chip rows so the expanded bar reads compact.
            verticalArrangement = Arrangement.spacedBy(2.dp),
            itemVerticalAlignment = Alignment.CenterVertically,
            maxLines = if (expanded) Int.MAX_VALUE else 1
        ) {
            // Expand/collapse toggle — the first element in the flow, laid out just like the
            // caller-supplied content (it wraps and scrolls with the rest of the row). Down =
            // expand, up = collapse.
            IconButton(
                onClick = if (expanded) onCollapse else onExpand,
                modifier = Modifier.semantics {
                    contentDescription = if (expanded) "Show fewer tags" else "Show all tags"
                }
            ) {
                Icon(
                    imageVector = if (expanded) {
                        Icons.Default.KeyboardArrowUp
                    } else {
                        Icons.Default.KeyboardArrowDown
                    },
                    contentDescription = null // description on the IconButton above
                )
            }
            content()
        }
    }
}
