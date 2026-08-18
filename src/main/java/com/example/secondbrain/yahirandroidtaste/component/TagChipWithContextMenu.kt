package com.example.secondbrain.yahirandroidtaste.component

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp

/**
 * Policy-free chip wrapper adding a long-press-anchored Material3 [DropdownMenu] to [AppChip]
 * (Phase 93, TMENU-01). Mirrors [AppChip]'s full parameter surface verbatim (per UI-SPEC's
 * resolved Open Question 2) plus three nullable capability callbacks — never an internal
 * mode/kind string the component introspects. A null callback simply omits its menu item
 * (`!= null` gating), so a null [onRemoveFromContext] can never alias to [onDelete] (T-93-02).
 *
 * Menu-open state is self-contained ([remember]-scoped to this composable instance) — not
 * hoisted to the caller, since (unlike `HomeTagTile`) there is no drag gesture to disambiguate
 * against (UI-SPEC "Menu-open state ownership").
 *
 * Gesture: [AppChip]'s own `onLongClick` slot drives the menu open — a single
 * `combinedClickable` on AppChip's inner Surface handles both tap and long-press; its built-in
 * mutual exclusion (D-03) suppresses [onClick] when the long-press fires, so no bespoke
 * `pointerInput` guard is added here.
 *
 * Menu item order (never more than 3, never an overflow submenu — UI-SPEC "Menu Visual
 * Contract"): Edit -> scoped Remove ([removeLabel]) -> [HorizontalDivider] (only when at least
 * one non-destructive item precedes Delete) -> "Delete tag everywhere" (always last, the only
 * `colorScheme.error`-tinted row).
 *
 * @param label See [AppChip.label].
 * @param isSelected See [AppChip.isSelected].
 * @param onClick See [AppChip.onClick] — the chip's existing tap behavior, unchanged.
 * @param modifier Applied to this component's own root [Box] (not forwarded into [AppChip]) so
 *   that layout-identity modifiers from the caller — e.g. `LazyRow`'s `Modifier.animateItem()` —
 *   land on the actual node the caller's layout container measures/places/animates, not on a
 *   descendant one level deeper (CR-01).
 * @param leadingIcon See [AppChip.leadingIcon].
 * @param trailingIcon See [AppChip.trailingIcon].
 * @param relatednessStrength See [AppChip.relatednessStrength].
 * @param onEdit Opens the rename-only Edit sheet (TMENU-02). Null omits the "Edit" item.
 * @param onRemoveFromContext Removes the tag from the current scope only (card / drill / etc.).
 *   Null omits the scoped-Remove item — never falls back to [onDelete].
 * @param onDelete Deletes the tag everywhere (all cards, globally). Null omits the item.
 * @param removeLabel Scoped-Remove item text (e.g. "Remove from this card", "Remove from
 *   drill") — surface-specific per UI-SPEC's Per-Surface Capability Table.
 */
@Composable
fun TagChipWithContextMenu(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    relatednessStrength: Float? = null,
    onEdit: (() -> Unit)? = null,
    onRemoveFromContext: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    removeLabel: String = ""
) {
    var showMenu by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Box(modifier = modifier) {
        AppChip(
            label = label,
            isSelected = isSelected,
            onClick = onClick,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            relatednessStrength = relatednessStrength,
            onLongClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                showMenu = true
            }
        )
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            val hasNonDestructiveItem = onEdit != null || onRemoveFromContext != null

            if (onEdit != null) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = {
                        showMenu = false
                        onEdit()
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                    }
                )
            }

            if (onRemoveFromContext != null) {
                DropdownMenuItem(
                    text = { Text(removeLabel) },
                    onClick = {
                        showMenu = false
                        onRemoveFromContext()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.RemoveCircleOutline,
                            contentDescription = removeLabel
                        )
                    }
                )
            }

            if (onDelete != null) {
                if (hasNonDestructiveItem) {
                    HorizontalDivider(thickness = 1.dp)
                }
                DropdownMenuItem(
                    text = {
                        Text(
                            "Delete tag everywhere",
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = {
                        showMenu = false
                        onDelete()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete tag everywhere",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                )
            }
        }
    }
}
