package io.github.ygaray.yahirandroidtaste.model

/**
 * One item row in a ListCard — mirrors ListItemEntity fields needed by UI.
 *
 * Relocated from feature.module.CardUiModels (S-01 / D38-05) into core.ui.model so
 * that core.ui.components (ListCard, EditorItemRow, ListCardBottomSheet) can import it
 * without a core→feature layering violation.
 *
 * S-10: renamed from ListItemUiModel → ListItemUiModel to follow Convention 7 (*UiModel
 * for item projections, *UiState reserved for screen-level aggregates). Rename committed
 * separately (plan 38-08 Task 2) for clean history.
 */
data class ListItemUiModel(
    val id: String,
    val text: String,
    val isCompleted: Boolean,
    val sortOrder: Int,
    val completedAt: Long? = null
)
