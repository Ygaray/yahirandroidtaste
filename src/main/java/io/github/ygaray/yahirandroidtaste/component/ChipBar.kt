package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

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
 * Composition order: [leadingContent] (if non-null) first, then each [items] entry via
 * [itemContent] in list order — wrapped in Compose's generic [key] composition-identity helper
 * (not a Lazy-list key; `FlowRow`'s scope has no native per-item key the way `LazyColumn` does) so
 * per-item remembered state survives recomposition — then [trailingContent] (if non-null) last.
 *
 * @param items           The list of chip data items to render.
 * @param key             Stable identity key for each item (used with Compose's [key] helper).
 * @param itemContent     Renders a single item's chip visual (e.g. a selectable or removable
 *                        Material chip).
 * @param modifier        Applied to the outer [FlowRow], in addition to `fillMaxWidth` + [testTag].
 * @param testTag         The container's `Modifier.testTag`. Defaults to `"chip_bar"`; callers
 *                        override to preserve an existing site-specific tag (e.g. `"tag_flow_row"`).
 * @param leadingContent  Optional slot composed before all items (e.g. a leading sort control or
 *                        an always-first create-chip, D-06). `null` (default) renders nothing.
 * @param trailingContent Optional slot composed after all items (e.g. an "Add tag" affordance).
 *                        `null` (default) renders nothing.
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
    trailingContent: (@Composable () -> Unit)? = null
) {
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
}
