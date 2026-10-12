package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.model.BatchRowResultUiModel
import io.github.ygaray.yahirandroidtaste.model.FailureActionUiModel
import io.github.ygaray.yahirandroidtaste.model.FailureTone
import io.github.ygaray.yahirandroidtaste.model.HandledByUiModel
import io.github.ygaray.yahirandroidtaste.model.OutcomeBadge
import io.github.ygaray.yahirandroidtaste.model.ProposedItemUiModel
import io.github.ygaray.yahirandroidtaste.model.SheetActionUiModel
import io.github.ygaray.yahirandroidtaste.model.UndoAffordanceUiModel
import io.github.ygaray.yahirandroidtaste.model.UndoRefusedUiModel
import io.github.ygaray.yahirandroidtaste.model.UndoRowState
import io.github.ygaray.yahirandroidtaste.model.UndoRowUiModel
import io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState

/**
 * Shared test fixtures for every Receipt and Classic outcome test of Phase 22. Builders take
 * defaults for every field so a test only names what it cares about, and record calls through the
 * lambdas the test passes in. Plan 22-03 appended the Success, Failure, handled-by and undo builders.
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

    fun handledBy(
        tier: String = "Local",
        approach: String? = null,
        provider: String? = null,
        model: String? = null,
        escalationCount: Int? = null,
        escalationsLabel: String = "Escalations:",
        prefixLabel: String = "Handled by",
        onDevice: Boolean = false
    ) = HandledByUiModel(
        tier = tier,
        approach = approach,
        provider = provider,
        model = model,
        escalationCount = escalationCount,
        escalationsLabel = escalationsLabel,
        prefixLabel = prefixLabel,
        onDevice = onDevice
    )

    /** One Available row and one Undone row, the shape the undo tests walk in list order. */
    fun undoRows(onUndo: () -> Unit = {}): List<UndoRowUiModel> = listOf(
        UndoRowUiModel(id = "row-a", label = "Row A", state = UndoRowState.Available(onUndo)),
        UndoRowUiModel(id = "row-b", label = "Row B", state = UndoRowState.Undone)
    )

    fun refused(reason: String = "it was edited elsewhere", changedItem: String? = null) =
        UndoRefusedUiModel(reason = reason, changedItem = changedItem)

    fun undo(
        rows: List<UndoRowUiModel> = undoRows(),
        allLabel: String = "Undo all (1)",
        onUndoAll: (() -> Unit)? = {},
        refused: UndoRefusedUiModel? = null
    ) = UndoAffordanceUiModel(allLabel = allLabel, rows = rows, onUndoAll = onUndoAll, refused = refused)

    fun batchResults(): List<BatchRowResultUiModel> = listOf(
        BatchRowResultUiModel(label = "Oats", succeeded = true),
        BatchRowResultUiModel(label = "Milk", succeeded = false, detail = "not found")
    )

    /** Every Success field defaulted; the Receipt-only fields are named arguments. */
    @Suppress("LongParameterList")
    fun success(
        summary: String = "Logged 2 of 3",
        handledBy: HandledByUiModel? = null,
        editableContent: (@Composable () -> Unit)? = null,
        inFlight: Boolean = false,
        batchResults: List<BatchRowResultUiModel> = emptyList(),
        undo: UndoAffordanceUiModel? = null,
        badge: OutcomeBadge? = null,
        titleActions: (@Composable () -> Unit)? = null,
        startAction: SheetActionUiModel? = null,
        endAction: SheetActionUiModel? = null
    ) = VoiceOutcomeUiState.Success(
        summary = summary,
        handledBy = handledBy,
        editableContent = editableContent,
        inFlight = inFlight,
        batchResults = batchResults,
        undo = undo,
        badge = badge,
        titleActions = titleActions,
        startAction = startAction,
        endAction = endAction
    )

    /** Every Failure field defaulted; the Receipt-only fields are named arguments. */
    @Suppress("LongParameterList")
    fun failure(
        reason: String = "Network down",
        handledBy: HandledByUiModel? = null,
        action: FailureActionUiModel? = null,
        body: (@Composable () -> Unit)? = null,
        semanticsPrefix: String? = null,
        title: String? = null,
        transcript: String? = null,
        tone: FailureTone = FailureTone.Error,
        tip: (@Composable () -> Unit)? = null,
        secondaryAction: SheetActionUiModel? = null,
        badge: OutcomeBadge? = null,
        undo: UndoAffordanceUiModel? = null
    ) = VoiceOutcomeUiState.Failure(
        reason = reason,
        handledBy = handledBy,
        action = action,
        body = body,
        semanticsPrefix = semanticsPrefix,
        title = title,
        transcript = transcript,
        tone = tone,
        tip = tip,
        secondaryAction = secondaryAction,
        badge = badge,
        undo = undo
    )
}
