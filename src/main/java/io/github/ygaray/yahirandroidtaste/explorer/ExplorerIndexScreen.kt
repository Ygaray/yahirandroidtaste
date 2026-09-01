package io.github.ygaray.yahirandroidtaste.explorer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.component.EmptyState
import io.github.ygaray.yahirandroidtaste.feedback.FeedbackDispatcher
import io.github.ygaray.yahirandroidtaste.feedback.FeedbackEvent
import io.github.ygaray.yahirandroidtaste.theme.Dimens
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode

/**
 * Family keys dispatched via the explorer's internal `"family/{family}"` NavHost route (D-02) —
 * the single source of truth for the explorer's fixed authored order (EDGE ordering, D-03).
 */
object ExplorerFamilies {
    const val CARDS = "cards"
    const val CHIPS = "chips"
    const val SHEETS = "sheets"
    const val BUTTONS_FAB = "buttons_fab"
    const val PICKERS = "pickers"
    const val FEEDBACK = "feedback"
    const val EMPTY_STATE = "empty_state"
    const val PROGRESS_METRICS = "progress_metrics"
    const val TACTILE_FOUNDATION = "tactile_foundation"

    /** Fixed authored order rendered by [ExplorerIndexScreen] (EDGE ordering). */
    val ORDERED_KEYS: List<Pair<String, String>> = listOf(
        CARDS to "Cards",
        CHIPS to "Chips",
        SHEETS to "Sheets",
        BUTTONS_FAB to "Buttons / FAB",
        PICKERS to "Pickers",
        FEEDBACK to "Feedback",
        EMPTY_STATE to "Empty State",
        PROGRESS_METRICS to "Progress / Metrics",
        TACTILE_FOUNDATION to "Tactile Foundation"
    )
}

/**
 * No-op [FeedbackDispatcher] stub (SHOW-01) supplied to family screens that render components
 * consuming `LocalFeedbackController` — the explorer has no real Nav-coupled controller.
 */
val ExplorerFeedbackStub: FeedbackDispatcher = object : FeedbackDispatcher {
    override suspend fun emit(event: FeedbackEvent) {}
}

/**
 * SHOW-01 debug-only component gallery — family index screen.
 *
 * The [ThemeMode] and toggle callback are hoisted above the explorer's internal nav destinations
 * (in `ExplorerEntry`, D-02/D-04) so the same choice reaches every family screen and persists
 * across navigation, instead of being scoped to this screen's own composition.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorerIndexScreen(
    onNavigateBack: () -> Unit,
    onNavigateToFamily: (String) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToTokens: () -> Unit,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    val results = remember(query) { ComponentSearch.filter(query) }

    YahirAndroidTasteTheme(themeMode = themeMode) {
        Scaffold(
            topBar = {
                ExplorerTopBar(
                    themeMode = themeMode,
                    onToggleTheme = onToggleTheme,
                    onNavigateBack = onNavigateBack
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    label = { Text("Search components") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (query.isNotBlank()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.HorizontalPadding, vertical = Dimens.TopPadding)
                )

                if (results == null) {
                    FamilyRow(
                        label = "Design Tokens",
                        leadingIcon = Icons.Default.Palette,
                        onClick = onNavigateToTokens
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    ExplorerFamilies.ORDERED_KEYS.forEach { (key, label) ->
                        FamilyRow(label = label, onClick = { onNavigateToFamily(key) })
                    }
                } else if (results.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.Search,
                        title = "No components found",
                        body = "No component name matches \"$query\".",
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    results.forEach { entry ->
                        ComponentRow(
                            name = entry.name,
                            tier = entry.tier,
                            supportingLabel = familyLabelFor(entry.family),
                            onClick = { onNavigateToDetail(entry.name) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Resolves a family key to its display label (e.g. `"cards"` -> `"Cards"`) via
 * [ExplorerFamilies.ORDERED_KEYS], falling back to the raw key if somehow unmatched — used for
 * the search-result row's family subtitle (component-list/family-picker rows pass
 * `supportingLabel = null` instead, per the Family Screen contract).
 */
internal fun familyLabelFor(familyKey: String): String {
    return ExplorerFamilies.ORDERED_KEYS.toMap()[familyKey] ?: familyKey
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExplorerTopBar(
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onNavigateBack: () -> Unit
) {
    TopAppBar(
        title = { Text("Component Gallery") },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = { ExplorerThemeToggleAction(themeMode = themeMode, onToggleTheme = onToggleTheme) }
    )
}

/**
 * Shared light/dark toggle action (D-04) reused by the index and all 7 family TopAppBars so the
 * explorer's single hoisted [ThemeMode] surfaces identically everywhere without 8x drift.
 */
@Composable
fun ExplorerThemeToggleAction(themeMode: ThemeMode, onToggleTheme: () -> Unit) {
    IconButton(onClick = onToggleTheme) {
        Icon(
            imageVector = if (themeMode == ThemeMode.LIGHT) {
                Icons.Default.DarkMode
            } else {
                Icons.Default.LightMode
            },
            contentDescription = if (themeMode == ThemeMode.LIGHT) {
                "Switch to dark theme"
            } else {
                "Switch to light theme"
            }
        )
    }
}

@Composable
private fun FamilyRow(
    label: String,
    onClick: () -> Unit,
    leadingIcon: ImageVector? = null
) {
    ListItem(
        headlineContent = { Text(label) },
        leadingContent = leadingIcon?.let {
            {
                Icon(
                    it,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        trailingContent = {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

/**
 * Renders the tier chip (Primitive / Pattern labels) shared by [ComponentRow]'s list surface and
 * [ComponentDetailScreen]'s detail-header surface (D-02) — a single shared helper so the
 * label/color mapping lives in exactly one place and cannot drift between the two surfaces. Not a
 * showcaseable component (not registered in [ComponentRegistry], not added to `component/`) —
 * mirrors the existing `internal fun SectionLabel`/`internal fun DividerRow` precedent.
 */
@Composable
internal fun TierBadge(tier: ComponentRegistry.Tier) {
    val (containerColor, contentColor) = when (tier) {
        ComponentRegistry.Tier.PRIMITIVE ->
            MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        ComponentRegistry.Tier.PATTERN ->
            MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
    }
    Badge(containerColor = containerColor, contentColor = contentColor) {
        Text(
            when (tier) {
                ComponentRegistry.Tier.PRIMITIVE -> "Primitive"
                ComponentRegistry.Tier.PATTERN -> "Pattern"
            }
        )
    }
}

/**
 * Shared tappable component row (mirrors [FamilyRow]'s `ListItem` shape) — used both by the
 * index screen's search-result list (with a family [supportingLabel]) and, from Plans 03-05, by
 * each family screen's own row-list picker body (with `supportingLabel = null`, per the Family
 * Screen contract — a component row on its own family screen does not repeat the family name).
 * Non-private so the 7 family-screen files can reuse it directly.
 */
@Composable
fun ComponentRow(name: String, tier: ComponentRegistry.Tier, supportingLabel: String? = null, onClick: () -> Unit) {
    ListItem(
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(name)
                Spacer(Modifier.width(8.dp))
                TierBadge(tier)
            }
        },
        supportingContent = supportingLabel?.let {
            {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        trailingContent = {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}
