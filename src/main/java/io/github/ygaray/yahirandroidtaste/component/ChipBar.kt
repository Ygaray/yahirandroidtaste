package io.github.ygaray.yahirandroidtaste.component

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
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Configuration for [ChipBar]'s optional expand/collapse chrome mode (WO-1,
 * `docs/COHERENCE-AUDIT.md` Finding CH-1) — folded in from the former standalone `FilterBar`.
 *
 * @param expanded Whether the bar is expanded (wrap-all) vs. collapsed (single line).
 * @param onExpand Called when the leading chevron is tapped while collapsed (down arrow).
 * @param onCollapse Called when the leading chevron is tapped while expanded (up arrow).
 * @param contentDescription Accessibility label announced for the outer chrome [Surface].
 *   Defaults to the tag-domain copy every current call site relies on (matching `FilterBar`'s
 *   former `filterContentDescription` default); non-tag consumers should override with copy
 *   appropriate to their own `T`.
 */
data class ExpandableConfig(
    val expanded: Boolean,
    val onExpand: () -> Unit,
    val onCollapse: () -> Unit,
    val contentDescription: String = "Tag filters"
)

/**
 * Generic FlowRow chip-container widget (WIDGET-04, D-15).
 *
 * `ChipBar` owns the genuinely duplicated, safely-extractable surface across the app's chip-bar
 * sites — the `FlowRow` arrangement (`Arrangement.spacedBy(8.dp)` both axes), `fillMaxWidth`, and
 * a `testTag` — while callers keep their own chip visuals (selectable, removable, or action-style
 * Material chips) and callbacks in the [leadingContent] / [itemContent] / [trailingContent] slots.
 * `ChipBar` holds no chip-rendering opinions and imports no `:app` type — it is pure presentation,
 * extraction-ready for the future separate-repo library milestone (999.19).
 *
 * As of WO-1 (`docs/COHERENCE-AUDIT.md` Finding CH-1), `ChipBar` also owns the former sibling
 * `FilterBar`'s expand/collapse chrome as an opt-in mode via [expandable]: when non-null, the
 * `FlowRow` is wrapped in a tonal [Surface] with a leading expand/collapse chevron, clips to a
 * single line while collapsed, and wraps/height-caps/scrolls while expanded. `FilterBar` is
 * retired as a standalone registered component — its behavior lives on here.
 *
 * Composition order when [expandable] is non-null: (1) the chevron `IconButton`, (2)
 * [leadingContent] (if non-null), (3) body content — either [items] via [itemContent] (typed
 * mode) or [rawContent] (freeform mode, carrying `FilterBar`'s former slot-based callers) — the
 * two are mutually exclusive alternate body-content modes, not combinable in one call, (4)
 * [trailingContent] (if non-null). When [expandable] is `null`, composition order is unchanged
 * from before WO-1: [leadingContent], then each [items] entry via [itemContent] in list order —
 * wrapped in Compose's generic [key] composition-identity helper (not a Lazy-list key; `FlowRow`'s
 * scope has no native per-item key the way `LazyColumn` does) so per-item remembered state
 * survives recomposition — then [trailingContent] last.
 *
 * @param items           The list of chip data items to render (typed mode). Ignored when
 *                        [rawContent] is non-null.
 * @param key             Stable identity key for each item (used with Compose's [key] helper).
 * @param itemContent     Renders a single item's chip visual (e.g. a selectable or removable
 *                        Material chip). Ignored when [rawContent] is non-null.
 * @param modifier        Applied to the outer element — the [FlowRow] when [expandable] is
 *                        `null`, the chrome [Surface] when [expandable] is non-null.
 * @param testTag         The `Modifier.testTag` applied to the `FlowRow` in either mode. Defaults
 *                        to `"chip_bar"`; callers override to preserve an existing site-specific
 *                        tag (e.g. `"tag_flow_row"`).
 * @param leadingContent  Optional slot composed before all items (e.g. a leading sort control or
 *                        an always-first create-chip, D-06). `null` (default) renders nothing.
 * @param trailingContent Optional slot composed after all items (e.g. an "Add tag" affordance).
 *                        `null` (default) renders nothing.
 * @param expandable      Optional expand/collapse chrome mode (WO-1). `null` (default) preserves
 *                        `ChipBar`'s pre-WO-1 bare shape exactly for every existing call site.
 * @param rawContent      Optional freeform [FlowRowScope] content, carrying a caller migrating off
 *                        the former `FilterBar`'s slot-based `content` param. When non-null, this
 *                        replaces the typed [items]/[itemContent] body-content mode entirely.
 *                        `null` (default) uses the typed [items] mode.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> ChipBar(
    items: List<T>,
    key: (T) -> Any,
    itemContent: @Composable (T) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "chip_bar",
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    expandable: ExpandableConfig? = null,
    rawContent: (@Composable FlowRowScope.() -> Unit)? = null
) {
    if (expandable == null) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier
                .fillMaxWidth()
                .testTag(testTag)
        ) {
            leadingContent?.invoke()
            items.forEach { item ->
                key(key(item)) { itemContent(item) }
            }
            trailingContent?.invoke()
        }
        return
    }

    // Expanded region is bounded to ~40% of screen height and scrolls internally, so a large
    // content set never overflows the screen and the trailing content stays reachable.
    val maxExpandedHeight = (LocalConfiguration.current.screenHeightDp * 0.4f).dp

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = expandable.contentDescription },
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (expandable.expanded) {
                        Modifier
                            .heightIn(max = maxExpandedHeight)
                            .verticalScroll(rememberScrollState())
                    } else {
                        Modifier
                    }
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag(testTag),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            // Tight gap between wrapped chip rows so the expanded bar reads compact.
            verticalArrangement = Arrangement.spacedBy(2.dp),
            itemVerticalAlignment = Alignment.CenterVertically,
            maxLines = if (expandable.expanded) Int.MAX_VALUE else 1
        ) {
            // Expand/collapse toggle — the first element in the flow, laid out just like the
            // caller-supplied content (it wraps and scrolls with the rest of the row). Down =
            // expand, up = collapse.
            IconButton(
                onClick = if (expandable.expanded) expandable.onCollapse else expandable.onExpand,
                modifier = Modifier.semantics {
                    contentDescription = if (expandable.expanded) "Show fewer tags" else "Show all tags"
                }
            ) {
                Icon(
                    imageVector = if (expandable.expanded) {
                        Icons.Default.KeyboardArrowUp
                    } else {
                        Icons.Default.KeyboardArrowDown
                    },
                    contentDescription = null // description on the IconButton above
                )
            }
            leadingContent?.invoke()
            if (rawContent != null) {
                rawContent()
            } else {
                items.forEach { item ->
                    key(key(item)) { itemContent(item) }
                }
            }
            trailingContent?.invoke()
        }
    }
}
