package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.model.ProposedItemUiModel
import io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState

/**
 * Shared test fixtures for every Receipt and Classic outcome test of Phase 22. Builders take
 * defaults for every field so a test only names what it cares about, and record calls through the
 * lambdas the test passes in. Later plans append Success, Failure and undo builders here.
 */
internal object OutcomeFixtures {

    fun proposedItems(count: Int): List<ProposedItemUiModel> = List(count) { i ->
        ProposedItemUiModel(id = "item-$i", title = "Proposed item $i", subtitle = "detail $i")
    }

    @Suppress("LongParameterList")
    fun needsConfirmation(
        reason: String = "You said: “I ate two eggs and toast”",
        title: String? = "Log these?",
        items: List<ProposedItemUiModel> = proposedItems(2),
        confirmEnabled: Boolean = true,
        titleActions: (@Composable () -> Unit)? = null,
        topLevelContent: (@Composable () -> Unit)? = null,
        reversibilityHint: String? = null,
        confirmLabel: String = "Confirm",
        cancelLabel: String = "Cancel",
        severity: ActionButtonDefaults.ActionButtonRole = ActionButtonDefaults.ActionButtonRole.Save,
        onConfirm: () -> Unit = {},
        onCancel: () -> Unit = {}
    ) = VoiceOutcomeUiState.NeedsConfirmation(
        reason = reason,
        items = items,
        title = title,
        severity = severity,
        reversibilityHint = reversibilityHint,
        confirmLabel = confirmLabel,
        cancelLabel = cancelLabel,
        topLevelContent = topLevelContent,
        confirmEnabled = confirmEnabled,
        titleActions = titleActions,
        onConfirm = onConfirm,
        onCancel = onCancel
    )

    /** Mirrors the CT Phase 74 shape of `OutcomeSheetTest`: a 400dp body plus four tall items. */
    fun tallNeedsConfirmation(
        onConfirm: () -> Unit = {},
        onCancel: () -> Unit = {}
    ) = VoiceOutcomeUiState.NeedsConfirmation(
        title = "Log these?",
        reason = "You said: “I ate two eggs and toast”",
        reversibilityHint = "You can undo this from the log.",
        severity = ActionButtonDefaults.ActionButtonRole.Save,
        confirmLabel = "Confirm",
        cancelLabel = "Cancel",
        topLevelContent = { Box(Modifier.height(400.dp)) { Text("date + correction controls") } },
        items = List(4) { i ->
            ProposedItemUiModel(
                id = "item-$i",
                title = "Proposed item $i",
                subtitle = "an amount / unit detail line",
                trailingContent = { Box(Modifier.height(80.dp)) { Text("editor $i") } }
            )
        },
        onConfirm = onConfirm,
        onCancel = onCancel
    )
}
