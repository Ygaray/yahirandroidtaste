package com.example.secondbrain.yahirandroidtaste.component

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Reusable, semantically-colored dynamic action button (BTN-01) — a single library component
 * generalizing the hand-rolled `TextButton`/filled-`Button` split shipped in
 * [NameAndTagsEditor]'s Discard/Save row into one role-driven, discoverable component.
 *
 * The [role] -> widget mapping is fixed and internal (D-01): [ActionButtonDefaults.ActionButtonRole.Save]
 * renders a filled [Button]; [ActionButtonDefaults.ActionButtonRole.Destructive] and
 * [ActionButtonDefaults.ActionButtonRole.Neutral] render a [TextButton]. There is deliberately no
 * caller-supplied widget override — this is a locked design decision, not an oversight.
 *
 * Stateless: state-in/callback-out only, matching [CycleSubTypeButton]'s documented convention.
 * All colors resolve exclusively through `MaterialTheme.colorScheme.*` — never a hardcoded ARGB
 * literal — so the component tracks the app's live theme (light/dark, teal primary/error red)
 * automatically.
 *
 * @param label The button's visible text.
 * @param role Color/widget treatment — [ActionButtonDefaults.ActionButtonRole.Destructive],
 *   [ActionButtonDefaults.ActionButtonRole.Save], or [ActionButtonDefaults.ActionButtonRole.Neutral].
 *   There is no 4th Grey/Disabled role — Material 3's own [enabled] flag renders the correct
 *   disabled treatment for every role.
 * @param onClick Invoked on tap when [enabled] is true. Never invoked when [enabled] is false.
 * @param enabled Standard Material 3 enabled/disabled passthrough — a presentation affordance,
 *   not an access-control gate.
 * @param modifier Modifier for the button's root composable.
 */
@Composable
fun DynamicActionButton(
    label: String,
    role: ActionButtonDefaults.ActionButtonRole,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    when (role) {
        ActionButtonDefaults.ActionButtonRole.Save -> Button(
            onClick = onClick,
            enabled = enabled,
            colors = ActionButtonDefaults.colors(role),
            modifier = modifier
        ) { Text(label) }
        else -> TextButton(
            onClick = onClick,
            enabled = enabled,
            colors = ActionButtonDefaults.colors(role),
            modifier = modifier
        ) { Text(label) }
    }
}

/**
 * Defaults + role enum for [DynamicActionButton] (D-02), mirroring [ConfirmationDialogDefaults]'s
 * `*Defaults`-object + enum-not-boolean convention.
 */
object ActionButtonDefaults {

    /** Role -> widget/color treatment (D-01) — an enum, never a boolean flag param. */
    enum class ActionButtonRole { Destructive, Save, Neutral }

    /**
     * Maps [role] to the M3 [ButtonDefaults] color set: Destructive is a text button with
     * `colorScheme.error` content; Save is a filled button with `colorScheme.primary` container
     * and `colorScheme.onPrimary` content; Neutral is the plain default text button.
     */
    @Composable
    fun colors(role: ActionButtonRole) = when (role) {
        ActionButtonRole.Destructive -> ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        )
        ActionButtonRole.Save -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
        ActionButtonRole.Neutral -> ButtonDefaults.textButtonColors()
    }
}
