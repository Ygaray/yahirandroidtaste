package io.github.ygaray.yahirandroidtaste.model

import androidx.compose.runtime.Immutable

/**
 * The button-row callbacks and gating of
 * [io.github.ygaray.yahirandroidtaste.component.ProviderKeyCard] (VKEY-01). Passing `null` for the
 * card's `actions` parameter renders no button row at all (today's card).
 *
 * Equality is by lambda reference, so remember the lambdas if this model is compared or used as a
 * key. The card holds no state: the consumer owns persistence, the typed text and the in-flight
 * flag.
 *
 * @param onSave Invoked when Save is tapped in field mode.
 * @param saveEnabled `false` disables Save (for example while the typed key is empty).
 * @param onUpdate Invoked when Update is tapped in summary mode. `null` hides Update.
 * @param onClear Invoked when Clear is tapped in summary mode. `null` hides Clear. The card does not
 *   confirm: Clear fires immediately, so wrap this callback in a confirmation dialog or an undo
 *   snackbar.
 * @param onCancelEdit Invoked when Cancel is tapped in field mode. `null` hides Cancel.
 * @param inFlight `true` disables every action, and the provider dropdown, while a save is in
 *   progress.
 */
@Immutable
data class ProviderKeyActions(
    val onSave: () -> Unit,
    val saveEnabled: Boolean = true,
    val onUpdate: (() -> Unit)? = null,
    val onClear: (() -> Unit)? = null,
    val onCancelEdit: (() -> Unit)? = null,
    val inFlight: Boolean = false
)
