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
import androidx.compose.ui.text.style.TextOverflow
import io.github.ygaray.yahirandroidtaste.model.ModelOptionUiModel
import io.github.ygaray.yahirandroidtaste.theme.Dimens
import io.github.ygaray.yahirandroidtaste.theme.expressive

/**
 * Prop-driven, presentational model-selection settings card (VSET-02) — expands the Voice
 * Command settings surface (Phase 10) alongside [ProviderKeyCard]. Renders the caller's [models]
 * catalog as a dropdown purely from props; picking a model emits back via [onModelSelected]. When
 * [models] is empty, the card renders a DISABLED affordance carrying [emptyReason] as a caption
 * (never a blank dropdown) — the reason is informational, not an error (mirrors
 * [SegmentedOptionSelector]'s disabled+reason posture, D-05).
 *
 * Mirrors [ProviderKeyCard]'s card shape (required content params first, then [modifier]),
 * wrapped in a [Surface] + [Column] body with [Dimens] padding, and the same
 * [ExposedDropdownMenuBox] pattern (first established by [ProviderKeyCard]'s provider dropdown)
 * for the model selector.
 *
 * @param models The caller's known models, rendered as dropdown rows in list order. The library
 *   knows no models of its own — the consumer maps its engine's known models into
 *   [ModelOptionUiModel] at the call site.
 * @param selectedModelId The currently-selected model's [ModelOptionUiModel.id], or `null` when
 *   nothing is selected yet.
 * @param onModelSelected Invoked with a model's `id` when the caller picks it from the dropdown.
 * @param emptyReason Caption shown when [models] is empty (e.g. "Set a provider and key first")
 *   — never a blank control.
 * @param modifier Applied to the outer [Surface].
 * @param modelLabel Caller-localizable label of the model dropdown's text field. Defaults to the
 *   English `"Model"`; rendered verbatim as plain text. Only visible when [models] is non-empty.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSelectCard(
    models: List<ModelOptionUiModel>,
    selectedModelId: String?,
    onModelSelected: (String) -> Unit,
    emptyReason: String,
    modifier: Modifier = Modifier,
    modelLabel: String = "Model"
) {
    Surface(
        shape = MaterialTheme.expressive.cardShapeLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.testTag("model_select_card_surface")
    ) {
        Column(modifier = Modifier.padding(Dimens.HorizontalPadding)) {
            if (models.isEmpty()) {
                Text(
                    text = emptyReason,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("model_select_card_empty_reason")
                )
            } else {
                ModelDropdown(
                    models = models,
                    selectedModelId = selectedModelId,
                    onModelSelected = onModelSelected,
                    modelLabel = modelLabel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("model_select_card_dropdown")
                )
            }
        }
    }
}

// v2.4.x binary-compatibility shim (INC-2026-10-05-02 F1): restores the pre-v2.5 JVM descriptor for
// consumers compiled against v2.4.x. Hidden from Kotlin and Java source -- do not call it or
// document it as API.
@Deprecated("Binary compatibility with v2.4.x", level = DeprecationLevel.HIDDEN)
@Composable
fun ModelSelectCard(
    models: List<ModelOptionUiModel>,
    selectedModelId: String?,
    onModelSelected: (String) -> Unit,
    emptyReason: String,
    modifier: Modifier = Modifier
) = ModelSelectCard(
    models = models,
    selectedModelId = selectedModelId,
    onModelSelected = onModelSelected,
    emptyReason = emptyReason,
    modifier = modifier
)

/**
 * Private model-selection dropdown — reuses the [ExposedDropdownMenuBox] shape [ProviderKeyCard]
 * established (RESEARCH.md Q2, canonical Material 3 pattern): a read-only [OutlinedTextField]
 * anchor + [ExposedDropdownMenu] listing [models] in list order.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModelDropdown(
    models: List<ModelOptionUiModel>,
    selectedModelId: String?,
    onModelSelected: (String) -> Unit,
    modelLabel: String,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val selectedLabel = models.firstOrNull { it.id == selectedModelId }?.label ?: ""
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(modelLabel) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            models.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onModelSelected(option.id)
                        expanded = false
                    }
                )
            }
        }
    }
}
