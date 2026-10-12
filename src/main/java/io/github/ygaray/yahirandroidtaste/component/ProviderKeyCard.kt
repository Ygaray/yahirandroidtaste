package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import io.github.ygaray.yahirandroidtaste.model.KeyFieldState
import io.github.ygaray.yahirandroidtaste.model.ProviderKeyActions
import io.github.ygaray.yahirandroidtaste.model.ProviderKeyLabels
import io.github.ygaray.yahirandroidtaste.model.ProviderOptionUiModel
import io.github.ygaray.yahirandroidtaste.model.SavedKeyUiModel
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
 * @param providers The caller's known providers, rendered as dropdown rows in list order. When
 *   empty, the dropdown is replaced by a caption rendering [emptyProvidersReason] (never a blank,
 *   still-tappable anchor) — mirrors [ModelSelectCard]'s empty-list convention.
 * @param selectedProviderId The currently-selected provider's [ProviderOptionUiModel.id], or
 *   `null` when nothing is selected yet.
 * @param onProviderSelected Invoked with a provider's `id` when the caller picks it from the
 *   dropdown.
 * @param keyValue The API key's current raw value — hoisted, never stored by this composable.
 * @param onKeyChange Invoked on every edit (including clear-✕). Every edit is trimmed of
 *   leading/trailing whitespace before this callback fires — not scoped to paste alone; pure
 *   formatting, no validation (INV-01).
 * @param keyState Render-only validation state (see [KeyFieldState]) driving the field's
 *   error/supporting-text presentation. While a saved-key summary shows, an `Invalid` reason is
 *   rendered as an error line under the summary instead. The library never validates the key itself.
 * @param keyLabel Caller-formatted label text for the key field (e.g. `"API key"`).
 * @param modifier Applied to the outer [Surface].
 * @param emptyProvidersReason Caption shown when [providers] is empty (e.g. "No providers
 *   configured yet") — never a blank control. Defaults to a generic caption so existing callers
 *   are source-compatible; callers with a more specific message should override it.
 * @param providerLabel Caller-localizable label of the provider dropdown's text field. Defaults to
 *   the English `"Provider"`; rendered verbatim as plain text (no formatting or templating).
 * @param savedKeys Saved keys by provider id (`providerId -> SavedKeyUiModel`, whose fingerprint is the
 *   only thing ever shown). The card stays stateless:
 *   the consumer owns persistence. When the selected provider has an entry (and [editingSavedKey] is
 *   `false`) the key field is replaced by a summary showing the fingerprint only, never the raw key.
 *   Empty (the default) renders today's card.
 * @param editingSavedKey `true` shows the key field even though the selected provider has a saved
 *   key (the consumer's "Update" flow). It is one flag for the whole card, not per provider: it
 *   applies to whichever provider is selected, so the consumer must set it back to `false` (and
 *   clear its typed [keyValue]) when [onProviderSelected] fires, or the newly selected provider
 *   shows the field instead of its own summary.
 * @param actions The Save / Update / Clear / Cancel callbacks and gating, or `null` (the default)
 *   for no button row. A summary shown with `actions == null` renders no buttons. While
 *   [ProviderKeyActions.inFlight] is `true` the provider dropdown is disabled as well.
 * @param labels Caller-localizable button, summary and dropdown-marker text. Defaults to English.
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
    modifier: Modifier = Modifier,
    emptyProvidersReason: String = "No providers configured yet",
    providerLabel: String = "Provider",
    savedKeys: Map<String, SavedKeyUiModel> = emptyMap(),
    editingSavedKey: Boolean = false,
    actions: ProviderKeyActions? = null,
    labels: ProviderKeyLabels = ProviderKeyDefaults.labels()
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
                emptyReason = emptyProvidersReason,
                providerLabel = providerLabel,
                savedIds = savedKeys.keys,
                labels = labels,
                enabled = actions?.inFlight != true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("provider_key_card_dropdown")
            )

            var revealed by rememberSaveable { mutableStateOf(false) }
            if (providerKeyMode(savedKeys, selectedProviderId, editingSavedKey) == ProviderKeyMode.Summary) {
                // Summary mode implies a non-null selection with a saved entry (providerKeyMode).
                val saved = savedKeys.getValue(checkNotNull(selectedProviderId))
                SavedKeySummary(
                    text = savedSummaryText(labels, saved),
                    errorReason = (keyState as? KeyFieldState.Invalid)?.reason,
                    actions = actions,
                    labels = labels
                )
            } else {
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
                if (actions != null) FieldActionRow(actions = actions, labels = labels)
            }
        }
    }
}

// v2.4.x binary-compatibility shim (INC-2026-10-05-02 F1): restores the pre-v2.5 JVM descriptor for
// consumers compiled against v2.4.x. Hidden from Kotlin and Java source -- do not call it or
// document it as API.
@Deprecated("Binary compatibility with v2.4.x", level = DeprecationLevel.HIDDEN)
@Composable
fun ProviderKeyCard(
    providers: List<ProviderOptionUiModel>,
    selectedProviderId: String?,
    onProviderSelected: (String) -> Unit,
    keyValue: String,
    onKeyChange: (String) -> Unit,
    keyState: KeyFieldState,
    keyLabel: String,
    modifier: Modifier = Modifier,
    emptyProvidersReason: String = "No providers configured yet"
) = ProviderKeyCard(
    providers = providers,
    selectedProviderId = selectedProviderId,
    onProviderSelected = onProviderSelected,
    keyValue = keyValue,
    onKeyChange = onKeyChange,
    keyState = keyState,
    keyLabel = keyLabel,
    modifier = modifier,
    emptyProvidersReason = emptyProvidersReason
)

/** Which body [ProviderKeyCard] shows under the dropdown. */
internal enum class ProviderKeyMode { Field, Summary }

/**
 * Summary iff the selected provider has a saved key and the consumer is not editing it (D-03);
 * Field for everything else, including a null selection and an empty [savedKeys].
 */
internal fun providerKeyMode(
    savedKeys: Map<String, SavedKeyUiModel>,
    selectedProviderId: String?,
    editingSavedKey: Boolean
): ProviderKeyMode =
    if (selectedProviderId != null && savedKeys[selectedProviderId] != null && !editingSavedKey) {
        ProviderKeyMode.Summary
    } else {
        ProviderKeyMode.Field
    }

/** The summary line as ONE string (one `Text` node): prefix, a middle dot, the fingerprint. */
internal fun savedSummaryText(labels: ProviderKeyLabels, saved: SavedKeyUiModel): String =
    "${labels.savedPrefix} \u00B7 ${saved.fingerprint}"

/** A dropdown row's text: the plain label, or label + middle dot + marker for a provider with a saved key. */
internal fun providerMenuLabel(
    option: ProviderOptionUiModel,
    savedIds: Set<String>,
    labels: ProviderKeyLabels
): String = if (option.id in savedIds) "${option.label} \u00B7 ${labels.savedMarker}" else option.label

/** Horizontal gap between the summary's check icon and its text (8dp; the vertical `TopPadding` names a different axis). */
private val SUMMARY_ICON_GAP = Dimens.ContentSpacing * 2

/** Horizontal gap between the buttons of an action row (8dp). */
private val ACTION_ROW_SPACING = Dimens.ContentSpacing * 2

private const val TAG_ACTION_SAVE = "provider_key_action_save"
private const val TAG_ACTION_CANCEL = "provider_key_action_cancel"
private const val TAG_SAVED_SUMMARY = "provider_key_saved_summary"
private const val TAG_SAVED_ERROR = "provider_key_saved_error"
private const val TAG_ACTION_UPDATE = "provider_key_action_update"
private const val TAG_ACTION_CLEAR = "provider_key_action_clear"

/**
 * The saved-key summary: a decorative check plus the whole summary string as one merged node, then
 * the [errorReason] of a [KeyFieldState.Invalid] state (when there is one), then (when [actions] has
 * an Update or a Clear) the button row. Emits up to three siblings into the card column; the error
 * and the buttons are deliberately NOT inside the merged summary node.
 */
@Composable
private fun SavedKeySummary(
    text: String,
    errorReason: String?,
    actions: ProviderKeyActions?,
    labels: ProviderKeyLabels
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SUMMARY_ICON_GAP),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Dimens.ContentSpacing)
            .semantics(mergeDescendants = true) {}
            .testTag(TAG_SAVED_SUMMARY)
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
    if (errorReason != null) {
        // A failure reported while the summary shows (a rejected saved key, a failed Clear) stays loud.
        Text(
            text = errorReason,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = Dimens.ContentSpacing).testTag(TAG_SAVED_ERROR)
        )
    }
    if (actions != null && (actions.onUpdate != null || actions.onClear != null)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(ACTION_ROW_SPACING, Alignment.End),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimens.TopPadding)
        ) {
            actions.onUpdate?.let { onUpdate ->
                DynamicActionButton(
                    label = labels.update,
                    role = ActionButtonDefaults.ActionButtonRole.Neutral,
                    onClick = onUpdate,
                    enabled = !actions.inFlight,
                    emphasis = ActionButtonEmphasis.Tonal,
                    modifier = Modifier.weight(1f, fill = false).testTag(TAG_ACTION_UPDATE)
                )
            }
            actions.onClear?.let { onClear ->
                DynamicActionButton(
                    label = labels.clear,
                    role = ActionButtonDefaults.ActionButtonRole.Destructive,
                    onClick = onClear,
                    enabled = !actions.inFlight,
                    emphasis = ActionButtonEmphasis.Text,
                    modifier = Modifier.weight(1f, fill = false).testTag(TAG_ACTION_CLEAR)
                )
            }
        }
    }
}

/** Field mode's button row: Cancel (when [ProviderKeyActions.onCancelEdit] is set) then Save. */
@Composable
private fun FieldActionRow(actions: ProviderKeyActions, labels: ProviderKeyLabels) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(ACTION_ROW_SPACING, Alignment.End),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Dimens.TopPadding)
    ) {
        actions.onCancelEdit?.let { onCancelEdit ->
            DynamicActionButton(
                label = labels.cancel,
                role = ActionButtonDefaults.ActionButtonRole.Neutral,
                onClick = onCancelEdit,
                enabled = !actions.inFlight,
                emphasis = ActionButtonEmphasis.Text,
                modifier = Modifier.weight(1f, fill = false).testTag(TAG_ACTION_CANCEL)
            )
        }
        DynamicActionButton(
            label = labels.save,
            role = ActionButtonDefaults.ActionButtonRole.Save,
            onClick = actions.onSave,
            enabled = actions.saveEnabled && !actions.inFlight,
            emphasis = ActionButtonEmphasis.Filled,
            modifier = Modifier.weight(1f, fill = false).testTag(TAG_ACTION_SAVE)
        )
    }
}

/**
 * Private provider-selection dropdown — first in-tree [ExposedDropdownMenuBox] use (RESEARCH.md
 * Q2, canonical Material 3 shape): a read-only [OutlinedTextField] anchor + [ExposedDropdownMenu]
 * listing [providers] in list order. When [providers] is empty, renders [emptyReason] as a caption
 * instead (never a blank, still-tappable anchor) — mirrors [ModelSelectCard]'s empty-list
 * convention. Menu rows of providers in [savedIds] read the label, a middle dot and
 * [ProviderKeyLabels.savedMarker]; the anchor keeps the plain label. [enabled] is `false` while a
 * save is in flight, so the provider cannot change under an in-progress request.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProviderDropdown(
    providers: List<ProviderOptionUiModel>,
    selectedProviderId: String?,
    onProviderSelected: (String) -> Unit,
    emptyReason: String,
    providerLabel: String,
    savedIds: Set<String>,
    labels: ProviderKeyLabels,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    if (providers.isEmpty()) {
        Text(
            text = emptyReason,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = modifier
        )
        return
    }
    var expanded by rememberSaveable { mutableStateOf(false) }
    val selectedLabel = providers.firstOrNull { it.id == selectedProviderId }?.label ?: ""
    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(providerLabel) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && enabled) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = enabled)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded && enabled, onDismissRequest = { expanded = false }) {
            providers.forEach { option ->
                DropdownMenuItem(
                    text = { Text(providerMenuLabel(option, savedIds, labels)) },
                    onClick = {
                        onProviderSelected(option.id)
                        expanded = false
                    }
                )
            }
        }
    }
}
