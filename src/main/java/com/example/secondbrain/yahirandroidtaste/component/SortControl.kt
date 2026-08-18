package com.example.secondbrain.yahirandroidtaste.component

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * Stateless icon → [DropdownMenu] sort affordance (GADGET-02, D-01), generalized in place from
 * `TagSortControl` (which was itself lifted verbatim from `TagManagementScreen`'s original 2-mode
 * sort menu). Caller supplies the option list and its label mapping — this component no longer
 * hardcodes `TagSortMode`.
 *
 * Owns only the local `expanded` toggle — it never sorts a list itself. Sorted lists are handed
 * down pre-ordered from the ViewModel layer; this composable only mounts UI state and forwards
 * the selected option via [onSortModeChange]. Untinted (default) content color throughout
 * (D-01/UI-SPEC Color) — a quiet utility affordance, not a primary-tinted one.
 *
 * @param sortMode         Currently active sort mode (radio-checked with a trailing check icon).
 * @param options          The full set of selectable modes, rendered in received (caller) order.
 * @param optionLabel      Maps an option to its menu-item display text. Always used — never fall
 *   back to the option's own `toString()`/name.
 * @param onSortModeChange Invoked with the newly selected mode; the menu closes immediately after.
 * @param modifier         Applied to the outer [Box].
 * @param sortContentDescription Accessibility label announced for the sort icon. Defaults to the
 *   tag-domain copy that every current call site relies on; non-tag consumers should override
 *   this with copy appropriate to their own `T`.
 */
@Composable
fun <T> SortControl(
    sortMode: T,
    options: List<T>,
    optionLabel: (T) -> String,
    onSortModeChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    sortContentDescription: String = "Sort tags"
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = sortContentDescription)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { mode ->
                DropdownMenuItem(
                    text = { Text(optionLabel(mode)) },
                    onClick = {
                        onSortModeChange(mode)
                        expanded = false
                    },
                    trailingIcon = {
                        if (mode == sortMode) {
                            Icon(Icons.Default.Check, contentDescription = null)
                        }
                    }
                )
            }
        }
    }
}
