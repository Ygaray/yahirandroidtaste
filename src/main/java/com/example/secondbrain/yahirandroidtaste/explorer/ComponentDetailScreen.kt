package com.example.secondbrain.yahirandroidtaste.explorer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.secondbrain.yahirandroidtaste.theme.Dimens
import com.example.secondbrain.yahirandroidtaste.theme.ThemeMode
import com.example.secondbrain.yahirandroidtaste.theme.YahirAndroidTasteTheme

/**
 * Per-component detail page (EXPLORE-01, D-02/D-03) resolved by [ComponentRegistry.Entry] —
 * renders the States matrix (this entry's [ComponentRegistry.Entry.states], D-05 single source)
 * followed by the Variants section ([ComponentRegistry.Entry.content], the preserved Phase-61
 * curated demos). Back navigation is always internal `navigateUp()` (threaded via
 * [onNavigateBack] by the `detail/{component}` route in `ExplorerEntry`, Plan 02) — never the
 * external `onNavigateBack` reserved for the index screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentDetailScreen(
    entry: ComponentRegistry.Entry,
    onNavigateBack: () -> Unit,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit
) {
    YahirAndroidTasteTheme(themeMode = themeMode) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(entry.name, fontWeight = FontWeight.Medium) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        ExplorerThemeToggleAction(themeMode = themeMode, onToggleTheme = onToggleTheme)
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                if (entry.controls.isNotEmpty()) {
                    val playgroundState = rememberSaveable(entry.name, saver = PlaygroundState.Saver) {
                        PlaygroundState.initial(entry.controls)
                    }
                    SectionLabel("Playground")
                    PlaygroundKnobs(controls = entry.controls, state = playgroundState)
                    SectionLabel("Preview")
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(Dimens.CornerRadius.Medium),
                        tonalElevation = 1.dp,
                        color = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 80.dp)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            entry.preview?.invoke(playgroundState)
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
                SectionLabel("States")
                StatesMatrixSection(cells = entry.states)
                Spacer(Modifier.height(24.dp))
                SectionLabel("Variants")
                entry.content?.invoke()
            }
        }
    }
}

/**
 * Renders every [cells] entry's label unconditionally, then either the cell's live preview (when
 * [ComponentRegistry.StateCell.render] is non-null) or the literal "Not applicable" caption —
 * always a complete, visible matrix, never a blank reserved slot (D-03).
 */
@Composable
fun StatesMatrixSection(cells: List<ComponentRegistry.StateCell>) {
    Column {
        cells.forEach { cell ->
            Text(
                text = cell.label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 0.dp)
            )
            val render = cell.render
            if (render != null) {
                render()
            } else {
                Text(
                    "Not applicable",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            DividerRow()
        }
    }
}

@Composable
internal fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)
    )
}

@Composable
internal fun DividerRow() {
    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
}

/**
 * Renders one knob row per [controls] entry, in declaration order (SC2, D-04): a labeled
 * [Switch] for [Control.Toggle], a labeled [SingleChoiceSegmentedButtonRow] for [Control.Enum].
 * Every knob mutates [state] directly — no local composable state, no submit step — so the caller's
 * `entry.preview(state)` recomposes live on the same frame.
 */
@Composable
private fun PlaygroundKnobs(controls: List<Control>, state: PlaygroundState) {
    controls.forEach { control ->
        when (control) {
            is Control.Toggle -> Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    control.label,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = state.boolean(control),
                    onCheckedChange = { state.setBoolean(control, it) }
                )
            }
            is Control.Enum -> Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                Text(control.label, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                SingleChoiceSegmentedButtonRow {
                    control.options.forEachIndexed { index, option ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index, control.options.size),
                            selected = state.selected(control) == option,
                            onClick = { state.setSelected(control, option) }
                        ) {
                            Text(option)
                        }
                    }
                }
            }
        }
    }
}
