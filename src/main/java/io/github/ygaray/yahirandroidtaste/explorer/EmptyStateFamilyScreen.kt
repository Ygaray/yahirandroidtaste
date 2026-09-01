package io.github.ygaray.yahirandroidtaste.explorer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.ygaray.yahirandroidtaste.component.EmptyState
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode

/**
 * D-05: this family's slice of [ComponentRegistry.entries], declared here (not in
 * `ComponentRegistry.kt`) so this file stays the single place this plan enriches Empty State's
 * `states`/`content` without touching the shared registry file. Order preserved exactly from
 * the pre-Phase-62-Plan-02 inline registry list.
 */
internal val emptyStateFamilyEntries: List<ComponentRegistry.Entry> = listOf(
    ComponentRegistry.Entry(
        name = "EmptyState",
        family = ExplorerFamilies.EMPTY_STATE,
        states = listOf(
            ComponentRegistry.StateCell(
                "Default",
                render = {
                    EmptyState(
                        icon = Icons.Default.Inbox,
                        title = "No cards yet",
                        body = "Create your first card to get started",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            ),
            // EmptyState is static illustrative content with no interactive/selected visual
            // state — N/A.
            ComponentRegistry.StateCell("Pressed / Selected"),
            // EmptyState has no disabled param — N/A.
            ComponentRegistry.StateCell("Disabled"),
            // EmptyState has no focus-visual override — N/A.
            ComponentRegistry.StateCell("Focused")
        ),
        content = { EmptyStateVariants() },
        tier = ComponentRegistry.Tier.PRIMITIVE
    )
)

/** Empty State family (SHOW-01, D-04 / EDGE empty): a registry-filtered row-list picker (1 row). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmptyStateFamilyScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit
) {
    YahirAndroidTasteTheme(themeMode = themeMode) {
        Scaffold(
            topBar = { EmptyStateFamilyTopBar(onNavigateBack, themeMode, onToggleTheme) }
        ) { paddingValues ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                items(
                    ComponentRegistry.entries.filter { it.family == ExplorerFamilies.EMPTY_STATE },
                    key = { it.name }
                ) { entry ->
                    ComponentRow(name = entry.name, onClick = { onNavigateToDetail(entry.name) })
                }
            }
        }
    }
}

/** EmptyState's Phase-61 curated demo, reused verbatim as its Variants. */
@Composable
private fun EmptyStateVariants() {
    EmptyState(
        icon = Icons.Default.Inbox,
        title = "No cards yet",
        body = "Create your first card to get started",
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmptyStateFamilyTopBar(onNavigateBack: () -> Unit, themeMode: ThemeMode, onToggleTheme: () -> Unit) {
    TopAppBar(
        title = { Text("Empty State") },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = { ExplorerThemeToggleAction(themeMode = themeMode, onToggleTheme = onToggleTheme) }
    )
}
