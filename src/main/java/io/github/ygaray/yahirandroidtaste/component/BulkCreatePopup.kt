package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Reusable "type -> do-and-close / do-and-keep-open -> repeat" popup (WIDGET-01).
 *
 * A single [Dialog]-based, policy-free `:designsystem` widget that powers fast repeated
 * creation across the app — consumed by the tag create-chip (Plan 54-03) and the list-editor
 * Add-FAB (Plan 54-04). Presentation-only: data is hoisted as params, intents as callbacks
 * ([onAction]/[validate]); this file imports no `:app` types and stays extraction-ready (D-02).
 *
 * A centered [Dialog] rather than a `ModalBottomSheet` (D-01) — floats identically above any
 * host regardless of sheet nesting depth (the create-chip host is already 2 sheets deep). Owns
 * its own single keyboard-inset padding layer (D-04, applied once on the wrapper [Surface])
 * since Compose `Dialog` does not inherit a host `Scaffold`'s inset plumbing, unlike
 * [SheetScaffold]'s proven D-05 layer for `ModalBottomSheet`.
 *
 * @param onDismissRequest Invoked on scrim tap, back-press, or after the "close" variant fires.
 * @param actionLabel Label for the create/add-then-close button (e.g. "Create" / "Add").
 * @param actionAndMoreLabel Label for the create/add-and-keep-open button (e.g. "Create and
 *   more" / "Add & add more").
 * @param onAction Invoked with the trimmed, validated name on either button's submit.
 * @param validate Returns a non-null error message to block the submit (case-insensitive
 *   duplicate guard, length cap, etc. — hoisted; this widget enforces no policy of its own).
 *   Defaults to always-valid.
 * @param modifier Modifier for the Dialog's content [Surface].
 */
@Composable
fun BulkCreatePopup(
    onDismissRequest: () -> Unit,
    actionLabel: String,
    actionAndMoreLabel: String,
    onAction: (String) -> Unit,
    validate: (String) -> String? = { null },
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.9f)
                .imePadding(), // D-04: single IME layer — BulkCreatePopupContent must NOT re-apply
            shape = MaterialTheme.shapes.large
        ) {
            BulkCreatePopupContent(
                actionLabel = actionLabel,
                actionAndMoreLabel = actionAndMoreLabel,
                onAction = onAction,
                onDismissRequest = onDismissRequest,
                validate = validate
            )
        }
    }
}

/**
 * The testable body of [BulkCreatePopup] without the `Dialog`/keyboard-inset-padding wrapper.
 *
 * Public so cross-module Robolectric tests (`app/src/test/.../BulkCreatePopupContentTest.kt`)
 * can render it directly — `:designsystem` has no Compose-test infra (Pitfall 3), mirroring the
 * `TagCreateSheet`/`TagCreateSheetContent` and `TagPickerSheet`/`TagPickerSheetContent`
 * Content-split convention.
 *
 * A name field autofocuses on first composition (and re-focuses after each "and more" clear,
 * via the [FocusRequester] guard pattern from `EditorItemRow`); both action buttons disable on a
 * blank/whitespace-only name; [validate] runs on every submit (both variants) before [onAction]
 * ever fires, surfacing a non-null error via `supportingText` and blocking the submit.
 */
@Composable
fun BulkCreatePopupContent(
    actionLabel: String,
    actionAndMoreLabel: String,
    onAction: (String) -> Unit,
    onDismissRequest: () -> Unit,
    validate: (String) -> String? = { null },
    modifier: Modifier = Modifier
) {
    var fieldValue by remember { mutableStateOf(TextFieldValue("")) }
    var errorText by remember { mutableStateOf<String?>(null) }
    // Bumped every time the field is cleared (the "and more" path) so the LaunchedEffect below
    // re-keys and re-requests focus — same defensive guard EditorItemRow uses for autofocus.
    var focusGeneration by remember { mutableIntStateOf(0) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(focusGeneration) {
        try {
            focusRequester.requestFocus()
        } catch (_: IllegalStateException) {
            // Defensive: node may not be attached yet on the very first composition pass.
        }
    }

    val trimmedName = fieldValue.text.trim()
    val isBlank = trimmedName.isEmpty()

    fun submit(clearAndKeepOpen: Boolean) {
        val error = validate(trimmedName)
        if (error != null) {
            errorText = error
            return
        }
        errorText = null
        onAction(trimmedName)
        if (clearAndKeepOpen) {
            fieldValue = TextFieldValue("")
            focusGeneration++
        } else {
            onDismissRequest()
        }
    }

    Column(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        OutlinedTextField(
            value = fieldValue,
            onValueChange = {
                fieldValue = it
                errorText = null
            },
            label = { Text("Name") },
            singleLine = true,
            isError = errorText != null,
            supportingText = errorText?.let { message ->
                { Text(message, color = MaterialTheme.colorScheme.error) }
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .testTag("bulk_create_popup_name_field")
        )

        Spacer(Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(
                onClick = { submit(clearAndKeepOpen = true) },
                enabled = !isBlank
            ) {
                Text(actionAndMoreLabel)
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = { submit(clearAndKeepOpen = false) },
                enabled = !isBlank
            ) {
                Text(actionLabel)
            }
        }
    }
}
