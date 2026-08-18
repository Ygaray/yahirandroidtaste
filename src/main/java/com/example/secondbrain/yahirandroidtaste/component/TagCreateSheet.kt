package com.example.secondbrain.yahirandroidtaste.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Canonical, one create-surface bottom sheet for tags (D-07, TWID-01/SC #1).
 *
 * Collapses the previously-duplicated create UIs (Dashboard's `CreateHomeTagSheet`, and
 * Tag-Management's create `AlertDialog`) onto this single `:designsystem` widget. The required
 * input is always just the [ClearableTextField] name field + duplicate-name error; the icon-grid
 * + accent-color "Appearance" block is optional and gated behind [showAppearanceOption] — home-tile
 * create call sites (Dashboard) pass `true`, name-only call sites (Tag-Management) pass `false`.
 *
 * Wraps [SheetScaffold] (the canonical bottom-sheet chrome, owns the single `.imePadding()` layer —
 * D-05/F-02b) around the testable [TagCreateSheetContent], mirroring the `TagPickerSheet`/
 * `TagPickerSheetContent` split (D-40-02) so a Robolectric test can render the content directly
 * across the `:designsystem`/`:app` module boundary.
 *
 * @param onDismiss Called when the user dismisses the sheet without confirming.
 * @param onConfirm Called with (name, isHome, iconName?, color?) when the user taps "Create tag".
 *                  iconName and color are non-null only when [showAppearanceOption] is true AND
 *                  the "Add to home screen" section was revealed.
 * @param createTagError If non-null, shown as a red helper text below the name field; the sheet
 *   stays open (no auto-dismiss) so the user can correct the name.
 * @param showAppearanceOption When true, renders the collapsed-by-default "Add to home screen"
 *   reveal + IconPickerGrid + AccentColorPicker (Dashboard's full appearance flow). When false,
 *   only the name field + error render — no reveal, structurally absent (Tag-Management).
 * @param defaultAddToHome When true (and [showAppearanceOption] is true), the sheet opens with the
 *   Appearance section already revealed. Defaults to false.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagCreateSheet(
    onDismiss: () -> Unit,
    onConfirm: (name: String, isHome: Boolean, iconName: String?, color: Long?) -> Unit,
    createTagError: String? = null,
    showAppearanceOption: Boolean = false,
    defaultAddToHome: Boolean = false
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    SheetScaffold(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        TagCreateSheetContent(
            onConfirm = onConfirm,
            createTagError = createTagError,
            showAppearanceOption = showAppearanceOption,
            defaultAddToHome = defaultAddToHome
        )
    }
}

/**
 * The body of [TagCreateSheet] without the [SheetScaffold]/`ModalBottomSheet` wrapper.
 *
 * Extracted as a public composable (mirrors `TagPickerSheetContent`, D-40-02) so
 * `TagCreateSheetTest` (Robolectric; `:designsystem` has no test source set) can render it
 * directly across the module boundary without popup-window node-tree complications.
 */
@Composable
fun TagCreateSheetContent(
    onConfirm: (name: String, isHome: Boolean, iconName: String?, color: Long?) -> Unit,
    createTagError: String? = null,
    showAppearanceOption: Boolean = false,
    defaultAddToHome: Boolean = false
) {
    val isDark = isSystemInDarkTheme()
    val defaultColor = if (isDark) ACCENT_COLORS[2].dark else ACCENT_COLORS[2].light

    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("book") }
    var selectedColor by remember { mutableStateOf(defaultColor) }
    var showHomeSection by remember { mutableStateOf(defaultAddToHome) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Scrollable content region
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Text("New tag", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            ClearableTextField(
                value = name,
                // D-03 (Phase 29-01) — block at input; no toast, no silent truncation.
                // Enforces the single public TAG_NAME_MAX_LENGTH (TagPickerSheet.kt) — no second
                // 50 literal declared here (D-07 prohibition).
                onValueChange = { if (it.length <= TAG_NAME_MAX_LENGTH) name = it },
                label = { Text("Tag name") },
                isError = createTagError != null,
                supportingText = createTagError?.let {
                    { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            if (showAppearanceOption) {
                if (!showHomeSection) {
                    // Collapsed: show "Add to home screen" TextButton
                    TextButton(
                        onClick = { showHomeSection = true },
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Add to home screen",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                } else {
                    // Revealed: show Appearance section with IconPickerGrid + AccentColorPicker
                    Text(
                        "Appearance",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Icon",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(8.dp))
                    IconPickerGrid(
                        selectedIcon = selectedIcon,
                        onIconSelected = { selectedIcon = it }
                    )

                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Color",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(8.dp))
                    AccentColorPicker(
                        selectedColor = selectedColor,
                        onColorSelected = { selectedColor = it }
                    )

                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        onClick = { showHomeSection = false },
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Text(
                            "Don't add to home screen",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }

        // Sticky button outside scroll
        Button(
            onClick = {
                val isHome = showAppearanceOption && showHomeSection
                onConfirm(
                    name.trim(),
                    isHome,
                    if (isHome) selectedIcon else null,
                    if (isHome) selectedColor else null
                )
            },
            enabled = name.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Create tag")
        }
    }
}
