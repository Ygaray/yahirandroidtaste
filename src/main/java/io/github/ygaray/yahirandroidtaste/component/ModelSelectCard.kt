package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.ygaray.yahirandroidtaste.model.ModelOptionUiModel
import io.github.ygaray.yahirandroidtaste.theme.expressive

/**
 * RED-phase stub (Phase 10 Plan 02, VSET-02, TDD) — intentionally incomplete. Renders neither the
 * selected-model dropdown nor the empty-state reason yet; GREEN phase fills in the real behavior.
 */
@Composable
fun ModelSelectCard(
    models: List<ModelOptionUiModel>,
    selectedModelId: String?,
    onModelSelected: (String) -> Unit,
    emptyReason: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.expressive.cardShapeLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.testTag("model_select_card_surface")
    ) {}
}
