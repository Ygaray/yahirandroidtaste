package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.model.ClarificationOptionUiModel
import io.github.ygaray.yahirandroidtaste.theme.expressive

/**
 * Standalone, prop-driven "tap-to-clarify choices" composable (VCLAR-01, contract A19) -- a
 * [question] plus a list of opaque-id/label [options] the user resolves by TAPPING an option
 * rather than re-speaking. Built as its own top-level composable (never nested inside
 * [OutcomeSheet]'s sealed `when`), per D-07's explicit chips-vs-buttons/bar-vs-sheet-state
 * swappability grant -- folding it into the outcome sheet would couple an unrelated visual
 * decision to the sheet's own chrome.
 *
 * Rendered on [MaterialTheme.expressive.cardShapeLarge] over
 * [MaterialTheme.colorScheme.surfaceContainer] -- the same visually-informative, NON-error card
 * shape [ApproachLadderCard] uses (D-07/Claude's Discretion): this is explicitly NOT an error
 * surface. Callers with no pending clarification should simply not compose this function at all;
 * an empty [options] list is only the defensive fallback for the identical hidden outcome -- when
 * [options] is empty, this composable renders nothing (the same null/empty-hides-control
 * convention already used by [ApproachLadderCard]/[OutcomeSheet]'s undo section).
 *
 * [options] render in EXACT list order, with NO de-duplication and NO re-sorting, even when two
 * options share an [ClarificationOptionUiModel.id] or [ClarificationOptionUiModel.label] --
 * uniqueness of `id` within one list is the consumer's documented responsibility (T-11-05). No
 * timeout, no default-selected option, and no programmatic invocation of [onSelect]/[onDismiss]
 * exists anywhere in this file -- both fire ONLY from their own `onClick` lambda, i.e. only in
 * direct response to a user tap (T-11-07, the VCLAR-01 prohibition).
 *
 * @param question Caller-formatted question text rendered above the options.
 * @param options The pending clarification's options, rendered as pressable chips in EXACT list
 *   order. An empty list hides the whole bar.
 * @param onSelect Invoked with the TAPPED option's exact [ClarificationOptionUiModel.id] --
 *   verbatim, never normalized/case-folded -- only in direct response to a chip tap.
 * @param onDismiss Invoked only in direct response to a tap on the trailing dismiss control.
 * @param modifier Applied to the outer [Surface].
 * @param dismissLabel Caller-localizable text of the trailing dismiss button. Defaults to the
 *   English `"Dismiss"`; rendered verbatim as plain text.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClarificationBar(
    question: String,
    options: List<ClarificationOptionUiModel>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissLabel: String = "Dismiss"
) {
    if (options.isEmpty()) return

    Surface(
        shape = MaterialTheme.expressive.cardShapeLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.testTag("clarification_bar_surface")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = question, style = MaterialTheme.typography.titleSmall)
            // WR-03: FlowRow (mirrors ChipBar's own non-expandable layout) wraps overflow onto a
            // second line instead of a plain Row clipping trailing chips off-screen when more
            // options arrive than fit on one line.
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { option ->
                    AppChip(
                        label = option.label,
                        isSelected = false,
                        onClick = { onSelect(option.id) },
                        modifier = Modifier.testTag("clarification_bar_option")
                    )
                }
            }
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("clarification_bar_dismiss")
            ) {
                Text(dismissLabel)
            }
        }
    }
}
