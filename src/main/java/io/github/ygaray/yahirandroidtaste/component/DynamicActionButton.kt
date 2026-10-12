package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import io.github.ygaray.yahirandroidtaste.theme.Dimens

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
 * @param emphasis Optional widget choice ([ActionButtonEmphasis]); [role] then picks only the hue
 *   (Destructive -> error, every other role -> primary). `null` keeps the role-driven mapping.
 * @param icon Optional decorative leading icon (18dp, 8dp gap before the label). Its
 *   content description is always null; the label carries the meaning.
 *
 * With no [emphasis] and no [icon] the role-to-widget mapping is exactly the earlier one. An
 * explicit [emphasis] picks the widget independent of [role]. An [icon] alone keeps the legacy
 * role-to-hue mapping (Save -> filled, otherwise text) but is **not** a pixel-identical render of
 * the no-icon button: any call with an icon (or an emphasis) goes through the emphasis styling,
 * so the label is `labelLarge` SemiBold, Save is a pill (`CircleShape`) with 20dp horizontal
 * padding (the M3 default is 24dp), and the button reserves a 48dp touch target. Leave both
 * parameters unset to keep the earlier button exactly.
 */
@Composable
fun DynamicActionButton(
    label: String,
    role: ActionButtonDefaults.ActionButtonRole,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    emphasis: ActionButtonEmphasis? = null,
    icon: ImageVector? = null
) {
    if (emphasis != null || icon != null) {
        EmphasisActionButton(label, role, onClick, enabled, modifier, emphasis ?: legacyEmphasisFor(role), icon)
        return
    }
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

/**
 * Widget choice for [DynamicActionButton] (Phase 20 VBTN-01). Emphasis picks the WIDGET, the
 * button's role picks the HUE.
 */
enum class ActionButtonEmphasis {
    /** Pill-shaped filled button: hue container, on-hue content. */
    Filled,

    /** Pill-shaped tonal button: hue at low alpha as container, hue content, no elevation. */
    Tonal,

    /** Plain text button in the hue. */
    Text,

    /**
     * Text button with a smaller label (`bodySmall`). The drawn size is still governed by the M3
     * `TextButton` minimum (40dp tall, 58dp wide), so it is not physically more compact than [Text];
     * the touch target is 48dp.
     */
    TextSmall
}

/** Alpha of the Tonal container over the hue (Y-8). */
internal const val TONAL_CONTAINER_ALPHA = 0.14f

/** Emphasis used when only an icon is passed: Save keeps its filled look, others stay text. */
internal fun legacyEmphasisFor(role: ActionButtonDefaults.ActionButtonRole): ActionButtonEmphasis =
    if (role == ActionButtonDefaults.ActionButtonRole.Save) {
        ActionButtonEmphasis.Filled
    } else {
        ActionButtonEmphasis.Text
    }

/** Resolved container/content pair for one emphasis x role. */
internal data class EmphasisColors(val container: Color, val content: Color)

/** Pure palette mapping for [ActionButtonEmphasis]; Destructive -> error hue, others -> primary. */
internal fun emphasisColors(
    emphasis: ActionButtonEmphasis,
    role: ActionButtonDefaults.ActionButtonRole,
    scheme: ColorScheme
): EmphasisColors {
    val destructive = role == ActionButtonDefaults.ActionButtonRole.Destructive
    val hue = if (destructive) scheme.error else scheme.primary
    return when (emphasis) {
        ActionButtonEmphasis.Filled ->
            EmphasisColors(hue, if (destructive) scheme.onError else scheme.onPrimary)
        ActionButtonEmphasis.Tonal ->
            EmphasisColors(hue.copy(alpha = TONAL_CONTAINER_ALPHA), hue)
        ActionButtonEmphasis.Text, ActionButtonEmphasis.TextSmall ->
            EmphasisColors(Color.Transparent, hue)
    }
}

@Composable
@Suppress("LongParameterList")
private fun EmphasisActionButton(
    label: String,
    role: ActionButtonDefaults.ActionButtonRole,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier,
    emphasis: ActionButtonEmphasis,
    icon: ImageVector?
) {
    val palette = emphasisColors(emphasis, role, MaterialTheme.colorScheme)
    val labelStyle = if (emphasis == ActionButtonEmphasis.TextSmall) {
        MaterialTheme.typography.bodySmall
    } else {
        MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
    }
    when (emphasis) {
        ActionButtonEmphasis.Filled, ActionButtonEmphasis.Tonal -> Button(
            onClick = onClick,
            enabled = enabled,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = palette.container,
                contentColor = palette.content
            ),
            elevation = if (emphasis == ActionButtonEmphasis.Tonal) null else ButtonDefaults.buttonElevation(),
            contentPadding = PaddingValues(
                horizontal = Dimens.ActionButton.FilledHorizontalPadding,
                vertical = Dimens.ActionButton.ContentVerticalPadding
            ),
            modifier = modifier
                .minimumInteractiveComponentSize()
                .heightIn(min = Dimens.ActionButton.FilledMinHeight)
        ) { EmphasisContent(label, icon, labelStyle) }
        ActionButtonEmphasis.Text -> TextButton(
            onClick = onClick,
            enabled = enabled,
            colors = ButtonDefaults.textButtonColors(contentColor = palette.content),
            contentPadding = PaddingValues(
                horizontal = Dimens.ActionButton.TextHorizontalPadding,
                vertical = Dimens.ActionButton.ContentVerticalPadding
            ),
            modifier = modifier.minimumInteractiveComponentSize()
        ) { EmphasisContent(label, icon, labelStyle) }
        ActionButtonEmphasis.TextSmall -> TextButton(
            onClick = onClick,
            enabled = enabled,
            colors = ButtonDefaults.textButtonColors(contentColor = palette.content),
            contentPadding = PaddingValues(
                horizontal = Dimens.ActionButton.TextSmallHorizontalPadding,
                vertical = Dimens.ActionButton.TextSmallVerticalPadding
            ),
            modifier = modifier.minimumInteractiveComponentSize()
        ) { EmphasisContent(label, icon, labelStyle) }
    }
}

@Composable
private fun RowScope.EmphasisContent(label: String, icon: ImageVector?, style: TextStyle) {
    if (icon != null) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(Dimens.ActionButton.IconSize)
        )
        Spacer(Modifier.width(Dimens.ActionButton.IconGap))
    }
    Text(label, style = style)
}
