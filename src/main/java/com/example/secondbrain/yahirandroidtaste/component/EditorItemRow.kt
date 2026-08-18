package com.example.secondbrain.yahirandroidtaste.component

import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.InterceptPlatformTextInput
import androidx.compose.ui.platform.PlatformTextInputMethodRequest
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.secondbrain.yahirandroidtaste.modifier.SwipeAnchor
import com.example.secondbrain.yahirandroidtaste.modifier.SwipeableActionRow
import com.example.secondbrain.yahirandroidtaste.model.ListItemUiModel
import sh.calvin.reorderable.ReorderableCollectionItemScope

@Suppress("ModifierParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ReorderableCollectionItemScope.EditorItemRow(
    item: ListItemUiModel,
    itemIndex: Int,
    isDragging: Boolean,
    autoFocus: Boolean,
    onAutoFocused: () -> Unit,
    dragHandleModifier: Modifier,
    subType: String,
    onToggleChecked: () -> Unit,
    onTextChange: (String) -> Unit,
    onDelete: () -> Unit,
    onEnterPressed: () -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false
) {
    val elevation by animateDpAsState(
        targetValue = if (isDragging) 4.dp else 0.dp,
        label = "editor_item_drag_elevation"
    )

    // D-15/F-03b: this row has no distinct Edit action (its text is always inline-editable),
    // so it is migrated onto SwipeableActionRow's delete-only mode (onEditClick = null) rather
    // than the raw M3 swipe-to-dismiss primitive. SwipeableActionRow's own .clickable-only
    // firing on the revealed Delete slot supplies the reveal-then-confirm SWIPE-01 contract
    // structurally — the former LaunchedEffect(dismissState.currentValue)-on-settle fire is
    // removed entirely (that was the SWIPE-01 violation this migration fixes). Not hoisted at
    // the caller's LazyColumn scope (ListCardEditorScreen.kt is out of this plan's <files>), so
    // each row's reveal state is local — acceptable since cross-row single-open is not a
    // Must-Preserve for this family.
    val openRowState = remember { mutableStateOf<AnchoredDraggableState<SwipeAnchor>?>(null) }

    // Set by the InputConnection interceptor when deleteSurroundingText fires on an empty field
    // (soft keyboards like Gboard use this instead of a key event).
    var pendingImeDelete by remember { mutableStateOf(false) }
    LaunchedEffect(pendingImeDelete) {
        if (pendingImeDelete) {
            pendingImeDelete = false
            onDelete()
        }
    }

    val rowContent: @Composable BoxScope.() -> Unit = {
        Surface(shadowElevation = elevation) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 4.dp, top = 2.dp, bottom = 2.dp)
            ) {
                // Left: sub-type indicator
                when (subType) {
                    "CHECKBOX" -> Checkbox(
                        checked = item.isCompleted,
                        onCheckedChange = { if (!readOnly) onToggleChecked() },
                        enabled = !readOnly,  // T-11-08
                        modifier = Modifier.size(36.dp)
                    )
                    "ORDERED" -> Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.width(36.dp)
                    ) {
                        Text(
                            text = "${itemIndex + 1}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    else -> Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.width(36.dp)
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Center: editable text field
                var editText by remember(item.id) {
                    mutableStateOf(TextFieldValue(item.text, selection = TextRange(item.text.length)))
                }

                val textDecoration = if (subType == "CHECKBOX" && item.isCompleted) {
                    TextDecoration.LineThrough
                } else {
                    TextDecoration.None
                }

                val focusRequester = remember { FocusRequester() }

                LaunchedEffect(item.id, autoFocus) {
                    if (autoFocus) {
                        // Move cursor to end of text before requesting focus so the insertion
                        // point lands after the last character (important for delete-back case).
                        editText = editText.copy(selection = TextRange(editText.text.length))
                        try { focusRequester.requestFocus() } catch (_: Exception) { }
                    }
                }

                // Intercept platform InputConnection to catch deleteSurroundingText on empty
                // fields — this is what modern soft keyboards (Gboard) send instead of a key event.
                InterceptPlatformTextInput(
                    interceptor = { request, nextHandler ->
                        nextHandler.startInputMethod(object : PlatformTextInputMethodRequest {
                            override fun createInputConnection(outAttrs: EditorInfo): InputConnection {
                                val ic = request.createInputConnection(outAttrs)
                                return object : InputConnectionWrapper(ic, true) {
                                    override fun deleteSurroundingText(
                                        beforeLength: Int,
                                        afterLength: Int
                                    ): Boolean {
                                        // Gboard sends beforeLength=1 on empty fields;
                                        // some IMEs send (0,0). Catch both when empty.
                                        if (editText.text.isEmpty() && afterLength == 0) {
                                            pendingImeDelete = true
                                            return true
                                        }
                                        return super.deleteSurroundingText(beforeLength, afterLength)
                                    }
                                    override fun sendKeyEvent(event: KeyEvent?): Boolean {
                                        // Samsung keyboard and some IMEs send KEYCODE_DEL
                                        // key events instead of deleteSurroundingText.
                                        if (event?.keyCode == KeyEvent.KEYCODE_DEL &&
                                            event.action == KeyEvent.ACTION_DOWN &&
                                            editText.text.isEmpty()
                                        ) {
                                            pendingImeDelete = true
                                            return true
                                        }
                                        return super.sendKeyEvent(event)
                                    }
                                }
                            }
                        })
                    }
                ) {
                    BasicTextField(
                        value = editText,
                        onValueChange = { newValue ->
                            if (!readOnly) {  // T-11-08: guard callback
                                editText = newValue
                                onTextChange(newValue.text) // keep ViewModel in sync on every keystroke
                            }
                        },
                        enabled = !readOnly,  // T-11-08: grays out field, prevents input
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            textDecoration = textDecoration
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (!readOnly) {
                                    onTextChange(editText.text)
                                    onEnterPressed()
                                }
                            }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                            .onFocusChanged { focusState ->
                                if (!focusState.isFocused) {
                                    onTextChange(editText.text)
                                } else if (autoFocus) {
                                    onAutoFocused()
                                }
                            }
                            .onKeyEvent { keyEvent ->
                                // Backspace on empty — reliable for hardware keyboards
                                if (keyEvent.type == KeyEventType.KeyDown &&
                                    keyEvent.key == Key.Backspace &&
                                    editText.text.isEmpty()
                                ) {
                                    onDelete()
                                    true
                                } else {
                                    false
                                }
                            }
                    )
                }

                // Right: drag handle
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Drag to reorder",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = dragHandleModifier.then(
                        Modifier
                            .size(20.dp)
                            .padding(end = 4.dp)
                    )
                )
            }
        }
    }

    // T-11-08: readOnly gates the swipe affordance at THIS call site, not by adding a
    // param to the shared SwipeableActionRow primitive (RESEARCH Open Question 1). A
    // readOnly row renders its content directly with no swipe wrapper at all — never an
    // interactive-looking-but-dead swipe.
    if (readOnly) {
        Box(modifier = modifier, content = rowContent)
    } else {
        SwipeableActionRow(
            onDeleteClick = onDelete,
            onEditClick = null, // delete-only mode (D-15) — no distinct Edit action on this row
            openRowState = openRowState,
            modifier = modifier,
            content = rowContent
        )
    }
}
