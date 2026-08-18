package com.example.secondbrain.yahirandroidtaste.explorer

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.secondbrain.yahirandroidtaste.component.EmptyState
import com.example.secondbrain.yahirandroidtaste.theme.ThemeMode

/**
 * `:yahirandroidtaste`'s single public entry point into the debug-only component explorer
 * (SHOW-01, D-02). Owns its own internal string-route [NavHost] (index -> family) and hoists
 * [ThemeMode] itself, so the same toggle choice reaches every family screen and persists across
 * internal navigation without the host app threading any explorer-specific state.
 *
 * Back-navigation contract (UI-SPEC.md Explorer Chrome Contract item 4): [onNavigateBack] is the
 * EXTERNAL exit — called only from the index screen's back arrow, taking the caller back to
 * `:app` Settings. Every family screen's back arrow instead calls the internal NavHost's own
 * `navController.navigateUp()`, returning to the index screen without exiting the explorer.
 */
@Composable
fun ExplorerEntry(onNavigateBack: () -> Unit) {
    val navController = rememberNavController()
    var themeMode by rememberSaveable { mutableStateOf(ThemeMode.LIGHT) }
    val onToggleTheme: () -> Unit = {
        themeMode = if (themeMode == ThemeMode.LIGHT) ThemeMode.DARK else ThemeMode.LIGHT
    }

    NavHost(navController = navController, startDestination = "index") {
        composable("index") {
            ExplorerIndexScreen(
                onNavigateBack = onNavigateBack,
                onNavigateToFamily = { family -> navController.navigate("family/$family") },
                onNavigateToDetail = { name -> navController.navigate("detail/$name") },
                onNavigateToTokens = { navController.navigate("tokens") },
                themeMode = themeMode,
                onToggleTheme = onToggleTheme
            )
        }
        composable("tokens") {
            val onBack: () -> Unit = { navController.navigateUp() }
            TokenBrowserScreen(
                onNavigateBack = onBack,
                themeMode = themeMode,
                onToggleTheme = onToggleTheme
            )
        }
        composable("family/{family}") { backStackEntry ->
            val family = backStackEntry.arguments?.getString("family").orEmpty()
            val onBack: () -> Unit = { navController.navigateUp() }
            val onDetail: (String) -> Unit = { name -> navController.navigate("detail/$name") }
            when (family) {
                ExplorerFamilies.CARDS -> CardsFamilyScreen(
                    onNavigateBack = onBack,
                    onNavigateToDetail = onDetail,
                    themeMode = themeMode,
                    onToggleTheme = onToggleTheme
                )
                ExplorerFamilies.CHIPS -> ChipsFamilyScreen(
                    onNavigateBack = onBack,
                    onNavigateToDetail = onDetail,
                    themeMode = themeMode,
                    onToggleTheme = onToggleTheme
                )
                ExplorerFamilies.SHEETS -> SheetsFamilyScreen(
                    onNavigateBack = onBack,
                    onNavigateToDetail = onDetail,
                    themeMode = themeMode,
                    onToggleTheme = onToggleTheme
                )
                ExplorerFamilies.BUTTONS_FAB -> ButtonsFabFamilyScreen(
                    onNavigateBack = onBack,
                    onNavigateToDetail = onDetail,
                    themeMode = themeMode,
                    onToggleTheme = onToggleTheme
                )
                ExplorerFamilies.PICKERS -> PickersFamilyScreen(
                    onNavigateBack = onBack,
                    onNavigateToDetail = onDetail,
                    themeMode = themeMode,
                    onToggleTheme = onToggleTheme
                )
                ExplorerFamilies.FEEDBACK -> FeedbackFamilyScreen(
                    onNavigateBack = onBack,
                    onNavigateToDetail = onDetail,
                    themeMode = themeMode,
                    onToggleTheme = onToggleTheme
                )
                ExplorerFamilies.EMPTY_STATE -> EmptyStateFamilyScreen(
                    onNavigateBack = onBack,
                    onNavigateToDetail = onDetail,
                    themeMode = themeMode,
                    onToggleTheme = onToggleTheme
                )
                else -> ExplorerIndexScreen(
                    // This branch renders the INDEX composable, so per this file's own back-nav
                    // contract (doc comment above) it must use the EXTERNAL onNavigateBack, not
                    // the internal onBack — even though it is reached from the family/{family}
                    // route. Unreachable today (every navigate("family/$family") call passes a
                    // key from ExplorerFamilies.ORDERED_KEYS, which always matches one of the
                    // seven branches above), but if it is ever reached, the rendered screen's
                    // back arrow must behave like an index screen's, not a family screen's.
                    onNavigateBack = onNavigateBack,
                    onNavigateToFamily = { fam -> navController.navigate("family/$fam") },
                    onNavigateToDetail = onDetail,
                    onNavigateToTokens = { navController.navigate("tokens") },
                    themeMode = themeMode,
                    onToggleTheme = onToggleTheme
                )
            }
        }
        composable("detail/{component}") { backStackEntry ->
            val componentName = backStackEntry.arguments?.getString("component").orEmpty()
            val entry = ComponentRegistry.entries.find { it.name == componentName }
            val onBack: () -> Unit = { navController.navigateUp() }
            if (entry != null) {
                ComponentDetailScreen(
                    entry = entry,
                    onNavigateBack = onBack,
                    themeMode = themeMode,
                    onToggleTheme = onToggleTheme
                )
            } else {
                // Defensive fallback — a stale/typo'd route arg must never crash the app
                // (RESEARCH Pitfall 3, mirrors this file's own `else ->` defensive precedent
                // above): render EmptyState instead of asserting non-null.
                EmptyState(
                    icon = Icons.Default.Warning,
                    title = "Component not found",
                    body = "\"$componentName\" is not registered.",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
