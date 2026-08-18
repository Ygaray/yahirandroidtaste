package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Canonical bottom-sheet chrome wrapper (CANON-01, Family B) — the single shared implementation
 * for all `ModalBottomSheet` call sites app-wide.
 *
 * Presentation-only: wraps M3's own [ModalBottomSheet] (drag handle, window insets, sheet
 * state) and renders [content] inside a single `Column` that owns the sheet's one
 * `.imePadding()` layer (D-05). Callers must NOT re-apply `.imePadding()` inside their own
 * content lambda — doing so would double-count the keyboard inset (see project memory:
 * "Nested-Scaffold inset double-count"; this scaffold's single layer is the fix, not a second
 * instance of the bug). [content] receives a [ColumnScope] directly — it is not handed a
 * `Column` to wrap around its own output; apply any additional layout modifiers to the
 * top-level composables inside [content] itself, not around the whole lambda.
 *
 * Chrome only: no confirm-callback param and no ViewModel/feedback-dependency param
 * (D-13/D-14). "Stays open on error" is a structural property of this shape — the content's
 * own confirm handler decides whether to call [onDismissRequest], so a failed validation path
 * simply doesn't call it. DI/feedback-coupled callers resolve their ViewModel and feedback
 * dependencies in the calling composable (screen module) and never pass them to this scaffold —
 * this component never references those types, keeping it eligible for an unchanged move to
 * `:designsystem` (Phase 46).
 *
 * @param onDismissRequest Invoked on scrim tap, back-press, or drag-to-dismiss.
 * @param modifier Modifier for the sheet's root composable.
 * @param sheetState The sheet's [SheetState]. Defaults to [rememberModalBottomSheetState]'s
 *   standard (un-skipped) behavior; callers needing `skipPartiallyExpanded = true` pass their
 *   own explicit override.
 * @param dragHandle Drag-handle slot, following M3's own optional-slot convention. Defaults to
 *   [BottomSheetDefaults.DragHandle]; pass null to omit it.
 * @param contentWindowInsets Window insets applied by [ModalBottomSheet] itself. Defaults to
 *   [BottomSheetDefaults.windowInsets].
 * @param content The sheet's body, rendered inside the scaffold's single `.imePadding()`
 *   `Column`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetScaffold(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    contentWindowInsets: @Composable () -> WindowInsets = { BottomSheetDefaults.windowInsets },
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        dragHandle = dragHandle,
        contentWindowInsets = contentWindowInsets
    ) {
        Column(modifier = Modifier.imePadding()) {  // D-05: once, at content root; callers do not re-apply
            content()
        }
    }
}
