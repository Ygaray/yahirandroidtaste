package com.example.secondbrain.yahirandroidtaste.component

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Shared, hoisted, stateless clear-`×` [OutlinedTextField] wrapper (D-09).
 *
 * Extracted from the duplicated name-input pattern in `TagPickerSheet`'s create/search field and
 * `TagManagementScreen`'s rename field. Renders a trailing [Icons.Default.Close] icon ONLY when
 * [value] is non-empty; tapping it invokes [onValueChange] with an empty string. Tag-agnostic —
 * all label/error copy is passed in as params so Phase 49 (RENAME-01) can reuse this for every
 * rename field app-wide. Carries no tag-specific logic.
 *
 * @param value            Current field text.
 * @param onValueChange     Invoked on every edit and on clear-icon tap (with `""`).
 * @param modifier          Applied to the outer [OutlinedTextField].
 * @param label             Field label slot (e.g. "Search/create tags", "Name").
 * @param leadingIcon       Optional leading icon slot (e.g. search icon) — passed straight through.
 * @param isError           Error state, forwarded to [OutlinedTextField].
 * @param supportingText    Optional supporting/error text slot, forwarded to [OutlinedTextField].
 * @param singleLine        Forwarded to [OutlinedTextField]; defaults to `true` (name/search fields).
 * @param keyboardOptions   Forwarded to [OutlinedTextField].
 * @param keyboardActions   Forwarded to [OutlinedTextField]; defaults to [KeyboardActions.Default]
 *   (no-op) — added for the TagManagementEditSheet rename swap (Phase 49) which relies on a
 *   Done-key `onDone` action, preserving existing callers' behavior unchanged.
 */
@Composable
fun ClearableTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        leadingIcon = leadingIcon,
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear text",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        isError = isError,
        supportingText = supportingText,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions
    )
}
