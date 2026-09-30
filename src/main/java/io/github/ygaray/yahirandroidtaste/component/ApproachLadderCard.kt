package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.ygaray.yahirandroidtaste.model.ApproachRungUiModel
import io.github.ygaray.yahirandroidtaste.theme.expressive

/**
 * RED-phase stub (Phase 10 Plan 02, VAPPR-01/02/03, TDD) — intentionally incomplete. Renders
 * neither the ladder, the offline-only toggle, nor the cap control yet; GREEN phase fills in the
 * real behavior.
 */
@Composable
fun ApproachLadderCard(
    ladder: List<ApproachRungUiModel>,
    offlineOnly: Boolean? = null,
    onOfflineOnlyChange: ((Boolean) -> Unit)? = null,
    maxTierId: String? = null,
    onMaxTierChange: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.expressive.cardShapeLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.testTag("approach_ladder_card_surface")
    ) {}
}
