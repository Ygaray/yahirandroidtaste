package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties

/**
 * Canonical confirm/destructive dialog (CANON-01, Family A) — the single shared implementation
 * for all confirm-before-acting prompts app-wide.
 *
 * Presentation-only: takes a caller-computed [title]/[body] string pair, a confirm and a
 * dismiss lambda, and an optional [confirmStyle] to color the confirm action. Holds no
 * visibility state itself (the caller hoists that to a ViewModel `StateFlow` per S-12/D-04)
 * and references no ViewModel/Nav/FeedbackController type — DI/Nav-free so it can move to
 * `:designsystem` unchanged in Phase 46 (D-10).
 *
 * @param title The dialog headline.
 * @param body The dialog body copy — a plain `String`, never a `@Composable` slot (D-08); every
 *   caller (including live-count interpolation and manual pluralization) formats its own string.
 * @param onDismissRequest Invoked on scrim tap, back-press, or the dismiss button.
 * @param onConfirm Invoked when the confirm button is tapped. The caller is responsible for also
 *   dismissing (calling its own visibility setter) if that's the desired behavior.
 * @param modifier Modifier for the dialog's root composable.
 * @param confirmLabel Label for the confirm button. Defaults to [ConfirmationDialogDefaults.confirmLabel].
 * @param dismissLabel Label for the dismiss button, or null to omit the dismiss button entirely.
 *   Defaults to [ConfirmationDialogDefaults.dismissLabel].
 * @param confirmStyle Confirm-button color treatment — [ConfirmationDialogDefaults.ConfirmStyle.Destructive]
 *   (error-red) or [ConfirmationDialogDefaults.ConfirmStyle.Neutral] (default M3), never a boolean
 *   grab-bag (D-09).
 * @param properties Standard Compose [DialogProperties] passthrough.
 */
@Composable
fun ConfirmationDialog(
    title: String,
    body: String,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    confirmLabel: String = ConfirmationDialogDefaults.confirmLabel,
    dismissLabel: String? = ConfirmationDialogDefaults.dismissLabel,
    confirmStyle: ConfirmationDialogDefaults.ConfirmStyle = ConfirmationDialogDefaults.ConfirmStyle.Destructive,
    properties: DialogProperties = DialogProperties()
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ConfirmationDialogDefaults.confirmButtonColors(confirmStyle)
            ) { Text(confirmLabel) }
        },
        dismissButton = if (dismissLabel != null) {
            { TextButton(onClick = onDismissRequest) { Text(dismissLabel) } }
        } else null,
        properties = properties
    )
}

/**
 * Defaults + style enum for [ConfirmationDialog] — the codebase's first `*Defaults` object,
 * following official Compose component-API conventions (CANON-01).
 */
object ConfirmationDialogDefaults {

    /** Confirm-button color treatment (D-09) — an enum, never a boolean flag param. */
    enum class ConfirmStyle { Destructive, Neutral }

    /** Default confirm-button label. */
    val confirmLabel: String = "Confirm"

    /** Default dismiss-button label. */
    val dismissLabel: String? = "Cancel"

    /** Maps [ConfirmStyle] to the M3 [TextButton] color set — error-red for Destructive, default for Neutral. */
    @Composable
    fun confirmButtonColors(style: ConfirmStyle) = when (style) {
        ConfirmStyle.Destructive -> ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        )
        ConfirmStyle.Neutral -> ButtonDefaults.textButtonColors()
    }
}
