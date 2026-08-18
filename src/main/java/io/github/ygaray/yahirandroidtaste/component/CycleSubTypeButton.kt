package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Pure cycle-order function for the List card's sub-type control (EDIT-02, D-07).
 *
 * Cycle order: BULLETED -> ORDERED -> CHECKBOX -> BULLETED (no dead-end). Any unrecognized value
 * falls back to "BULLETED" rather than crashing.
 *
 * Kept `internal` (not `private`) so it is directly unit-testable without Compose (RESEARCH
 * "Don't Hand-Roll" table) while staying out of this module's public API surface.
 */
internal fun nextSubType(current: String): String = when (current) {
    "BULLETED" -> "ORDERED"
    "ORDERED" -> "CHECKBOX"
    "CHECKBOX" -> "BULLETED"
    else -> "BULLETED"
}

/**
 * Standalone, reusable predictive 3-way cycle button for the List card's sub-type (EDIT-02, D-10).
 *
 * Replaces `ListCardEditorScreen`'s private `SubTypeToggle` (`SingleChoiceSegmentedButtonRow`)
 * with a single icon-only `IconButton` (48dp M3 default touch target, D-09 — no visible text
 * label). One tap advances one step through the fixed cycle
 * (BULLETED -> ORDERED -> CHECKBOX -> BULLETED); the icon shown is always the NEXT sub-type, not
 * the current one (predictive icon, D-08), so the glyph previews what tapping will do.
 *
 * Dumb, state-in/callback-out: this composable holds no state and performs no persistence — it
 * only computes [nextSubType] from [currentSubType] and invokes [onCycle] with that value. The
 * caller (`ListCardEditorScreen`) owns forwarding to `viewModel.requestSubTypeChange(next)`.
 *
 * @param currentSubType Current sub-type: "BULLETED" | "ORDERED" | "CHECKBOX". String in/out —
 *   no enum is introduced; these are the exact literals `ListCardEditorViewModel
 *   .requestSubTypeChange` and the Room `list_type` column already use (Pitfall 4).
 * @param onCycle Invoked with the target sub-type computed from [currentSubType] when the button
 *   is tapped while [enabled].
 * @param enabled When false, renders the standard M3 disabled `IconButton` treatment (38% alpha)
 *   and does not invoke [onCycle] on tap. Callers gate this on `!readOnly` (T-65-01).
 * @param modifier Applied to the underlying [IconButton].
 *
 * Note: `Icons.AutoMirrored.Filled.FormatListNumbered` (RESEARCH Assumption A1) did not resolve
 * at compile time in this project's current icons-extended version — falls back to the
 * non-AutoMirrored `Icons.Default.FormatListNumbered` as anticipated by the plan.
 */
@Composable
fun CycleSubTypeButton(
    currentSubType: String,
    onCycle: (nextSubType: String) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val next = nextSubType(currentSubType)
    val (icon, description) = when (next) {
        "ORDERED" -> Icons.Default.FormatListNumbered to "Switch to Ordered list"
        "CHECKBOX" -> Icons.Default.Checklist to "Switch to Checkbox list"
        else -> Icons.AutoMirrored.Filled.FormatListBulleted to "Switch to Bulleted list"
    }
    IconButton(onClick = { onCycle(next) }, enabled = enabled, modifier = modifier) {
        Icon(icon, contentDescription = description)
    }
}
