package io.github.ygaray.yahirandroidtaste.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.ygaray.yahirandroidtaste.component.ActionButtonDefaults
import io.github.ygaray.yahirandroidtaste.component.ActionButtonEmphasis

/**
 * A label+callback action drawn in the Receipt style's start/end action slots
 * ([VoiceOutcomeUiState.Success.startAction], [VoiceOutcomeUiState.Success.endAction],
 * [VoiceOutcomeUiState.Failure.secondaryAction], VRCPT-03/05). Static UI copy only; the Classic
 * style ignores these slots.
 *
 * An action is clickable when `enabled && (!inFlight || activeWhileInFlight)`. A caller
 * `enabled = false` always wins; [activeWhileInFlight] only exempts the in-flight lock (a Cancel
 * that must stay usable while a write is in flight). Equality is by lambda reference, so remember
 * the lambda if this model is compared or used as a key.
 *
 * @param label The action button's visible text.
 * @param onClick Invoked when the action is tapped. Crosses back OUT to consumer-owned code.
 * @param emphasis The button widget (Filled, Tonal or Text); the [role] picks the hue.
 * @param enabled `false` disables the action regardless of the in-flight state.
 * @param icon Optional leading icon.
 * @param role The button role/severity that picks the hue. Defaults to
 *   [ActionButtonDefaults.ActionButtonRole.Neutral].
 * @param activeWhileInFlight `true` keeps the action usable while the outcome is in flight
 *   (e.g. an in-flight Cancel); `false` (the default) locks it like every other action.
 */
@Immutable
data class SheetActionUiModel(
    val label: String,
    val onClick: () -> Unit,
    val emphasis: ActionButtonEmphasis,
    val enabled: Boolean = true,
    val icon: ImageVector? = null,
    val role: ActionButtonDefaults.ActionButtonRole = ActionButtonDefaults.ActionButtonRole.Neutral,
    val activeWhileInFlight: Boolean = false
)
