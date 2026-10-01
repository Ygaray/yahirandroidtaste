package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import io.github.ygaray.yahirandroidtaste.model.KeyFieldState
import io.github.ygaray.yahirandroidtaste.model.ProviderOptionUiModel
import io.github.ygaray.yahirandroidtaste.theme.Dimens
import io.github.ygaray.yahirandroidtaste.theme.expressive

/**
 * Prop-driven, presentational provider + API-key settings card (VSET-01) — the tracer entry
 * point of the Voice Command settings surface (Phase 10). Renders a provider dropdown and a
 * masked API-key field purely from [providers]/[selectedProviderId]/[keyValue]/[keyState] props;
 * selecting a provider and editing the key emit back via [onProviderSelected]/[onKeyChange]. The
 * card holds NO key of its own — [keyValue] is a hoisted `String` prop and no model stores it;
 * the library persists, logs, and transmits nothing (INV-01, T-10-01/T-10-02/T-10-03).
 *
 * Mirrors [HeroStatCard]'s prop-driven card shape (required content params first, then
 * [modifier]), wrapped in a [Surface] + [Column] body with [Dimens] padding.
 *
 * @param providers The caller's known providers, rendered as dropdown rows in list order.
 * @param selectedProviderId The currently-selected provider's [ProviderOptionUiModel.id], or
 *   `null` when nothing is selected yet.
 * @param onProviderSelected Invoked with a provider's `id` when the caller picks it from the
 *   dropdown.
 * @param keyValue The API key's current raw value — hoisted, never stored by this composable.
 * @param onKeyChange Invoked on every edit (including clear-✕). Every edit is trimmed of
 *   leading/trailing whitespace before this callback fires (WR-04 — not scoped to paste alone;
 *   pure formatting, no validation — INV-01).
 * @param keyState Render-only validation state (see [KeyFieldState]) driving the field's
 *   error/supporting-text presentation. The library never validates the key itself.
 * @param keyLabel Caller-formatted label text for the key field (e.g. `"API key"`).
 * @param modifier Applied to the outer [Surface].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderKeyCard(
    providers: List<ProviderOptionUiModel>,
    selectedProviderId: String?,
    onProviderSelected: (String) -> Unit,
    keyValue: String,
    onKeyChange: (String) -> Unit,
    keyState: KeyFieldState,
    keyLabel: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.expressive.cardShapeLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.testTag("provider_key_card_surface")
    ) {
        Column(modifier = Modifier.padding(Dimens.HorizontalPadding)) {
            ProviderDropdown(
                providers = providers,
                selectedProviderId = selectedProviderId,
                onProviderSelected = onProviderSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("provider_key_card_dropdown")
            )

            var revealed by rememberSaveable { mutableStateOf(false) }
            val invalidState = keyState as? KeyFieldState.Invalid
            ClearableTextField(
                value = keyValue,
                onValueChange = { onKeyChange(it.trim()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.ContentSpacing)
                    .testTag("provider_key_card_key_field"),
                label = { Text(keyLabel) },
                isError = invalidState != null,
                supportingText = invalidState?.let { state -> { Text(state.reason) } },
                visualTransformation =
                    if (revealed) VisualTransformation.None else PasswordVisualTransformation(),
                revealToggle = RevealToggle(revealed = revealed, onToggle = { revealed = !revealed })
            )
        }
    }
}

/**
 * Private provider-selection dropdown — first in-tree [ExposedDropdownMenuBox] use (RESEARCH.md
 * Q2, canonical Material 3 shape): a read-only [OutlinedTextField] anchor + [ExposedDropdownMenu]
 * listing [providers] in list order.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProviderDropdown(
    providers: List<ProviderOptionUiModel>,
    selectedProviderId: String?,
    onProviderSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val selectedLabel = providers.firstOrNull { it.id == selectedProviderId }?.label ?: ""
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Provider") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            providers.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onProviderSelected(option.id)
                        expanded = false
                    }
                )
            }
        }
    }
}
