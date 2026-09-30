package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
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
 * @param visualTransformation Forwarded to [OutlinedTextField]; defaults to
 *   [VisualTransformation.None] (added v2.4.0, additive, VSET-01) — preserves every existing
 *   caller's unmasked behavior byte-for-byte. Pass `PasswordVisualTransformation()` to mask an
 *   API-key field.
 * @param revealToggle      Optional reveal-eye affordance rendered in the trailing slot BESIDE
 *   the existing clear-✕ (added v2.4.0, additive, D-02/D-05). `null` (the default) shows no eye
 *   at all — never a disabled one. When non-null, an `IconButton` toggles between "Show key" and
 *   "Hide key" via [RevealToggle.onToggle].
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
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    revealToggle: RevealToggle? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        leadingIcon = leadingIcon,
        trailingIcon = {
            if (revealToggle != null || value.isNotEmpty()) {
                Row {
                    if (revealToggle != null) {
                        IconButton(onClick = revealToggle.onToggle) {
                            Icon(
                                imageVector = if (revealToggle.revealed) {
                                    Icons.Default.VisibilityOff
                                } else {
                                    Icons.Default.Visibility
                                },
                                contentDescription = if (revealToggle.revealed) "Hide key" else "Show key",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
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
                }
            }
        },
        isError = isError,
        supportingText = supportingText,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation
    )
}

/**
 * Hideable (D-05) reveal-eye affordance for [ClearableTextField]'s masked-entry mode (added
 * v2.4.0, additive, D-02). Pass `null` to [ClearableTextField]'s `revealToggle` param to render
 * no eye at all — never a disabled one.
 *
 * @param revealed Whether the field's raw value is currently visible (drives the eye icon +
 *   contentDescription and, at the call site, which [VisualTransformation] is passed).
 * @param onToggle Invoked when the eye affordance is tapped — the caller flips [revealed] and
 *   the [VisualTransformation] it passes to [ClearableTextField] in response.
 */
data class RevealToggle(
    val revealed: Boolean,
    val onToggle: () -> Unit
)
