package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.model.ApproachRungUiModel
import io.github.ygaray.yahirandroidtaste.theme.Dimens
import io.github.ygaray.yahirandroidtaste.theme.expressive

/**
 * Prop-driven, presentational command-approach settings card (VAPPR-01/02/03) — the third card
 * in the Voice Command settings surface (Phase 10), alongside [ProviderKeyCard] and
 * [ModelSelectCard]. Renders the caller's tier [ladder] as a vertical, LIST-order column
 * (VAPPR-01 — never re-sorted by [ApproachRungUiModel.rank]), with an optional offline-only
 * toggle (VAPPR-02) and an optional max-tier cap (VAPPR-03). Every control is hideable-by-null-
 * prop, never shown-disabled (D-05): a `null` [offlineOnly]/[onOfflineOnlyChange] pair hides the
 * toggle entirely, and a `null` [maxTierId]/[onMaxTierChange] pair hides the cap control entirely
 * (no rung becomes clickable).
 *
 * Per-rung effective visual state is derived IN this composable from plain primitives (D-06 —
 * `rung.enabled && (!offlineOnly || rung.offlineCapable) && rung.rank <= capRank`); the library
 * never depends on any voice-action-engine `TierPolicy` type. The card DISPLAYS + EMITS only — it
 * never enforces, reorders, or executes the ladder.
 *
 * Mirrors [ProviderKeyCard]/[ModelSelectCard]'s card shape (required content params first, then
 * [modifier]), wrapped in a [Surface] + [Column] body with [Dimens] padding.
 *
 * @param ladder The caller's tier rungs, rendered as rows in LIST order (never sorted by
 *   [ApproachRungUiModel.rank]). The library knows no approach tiers of its own — the consumer
 *   maps its engine's tier metadata into [ApproachRungUiModel] at the call site.
 * @param offlineOnly Current offline-only state, or `null` to hide the toggle entirely (D-05).
 * @param onOfflineOnlyChange Invoked with the new value when the toggle is tapped, or `null` to
 *   hide the toggle entirely (D-05) — must be non-null exactly when [offlineOnly] is non-null
 *   (enforced via `require`; violating this pairing throws immediately rather than silently
 *   omitting the toggle with no signal).
 * @param maxTierId The currently-capped rung's [ApproachRungUiModel.id], or `null` to hide the
 *   cap control entirely (D-05) — no rung is clickable when this is `null`. If non-null but it
 *   matches no [ApproachRungUiModel.id] in [ladder] (e.g. a stale id after the ladder changed),
 *   the cap silently falls back to "no cap at all" (WR-02) — no rung renders "Capped" and no
 *   assertion fires; this is a caller/integration bug the component does not detect.
 * @param onMaxTierChange Invoked with a rung's `id` when it is tapped as the new cap, or `null`
 *   to hide the cap control entirely (D-05) — must be non-null exactly when [maxTierId] is
 *   non-null (enforced via `require`; violating this pairing throws immediately rather than
 *   silently leaving every rung clickable with no visible cap, WR-01).
 * @param modifier Applied to the outer [Surface].
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
    require((maxTierId == null) == (onMaxTierChange == null)) {
        "ApproachLadderCard: maxTierId and onMaxTierChange must both be null or both be non-null " +
            "(got maxTierId=$maxTierId, onMaxTierChange=${if (onMaxTierChange == null) "null" else "non-null"})"
    }
    require((offlineOnly == null) == (onOfflineOnlyChange == null)) {
        "ApproachLadderCard: offlineOnly and onOfflineOnlyChange must both be null or both be " +
            "non-null (got offlineOnly=$offlineOnly, onOfflineOnlyChange=" +
            "${if (onOfflineOnlyChange == null) "null" else "non-null"})"
    }
    val effectiveOfflineOnly = offlineOnly == true
    val capRank = maxTierId?.let { id -> ladder.firstOrNull { it.id == id }?.rank } ?: Int.MAX_VALUE

    Surface(
        shape = MaterialTheme.expressive.cardShapeLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.testTag("approach_ladder_card_surface")
    ) {
        Column(modifier = Modifier.padding(Dimens.HorizontalPadding)) {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.HairlineSpacing)) {
                ladder.forEach { rung ->
                    val needsNetwork = effectiveOfflineOnly && !rung.offlineCapable
                    val isCapped = rung.rank > capRank
                    val isEffective = rung.enabled && !needsNetwork && !isCapped
                    RungRow(
                        rung = rung,
                        isEffective = isEffective,
                        needsNetwork = needsNetwork,
                        isCapped = isCapped,
                        onCapClick = onMaxTierChange?.let { callback -> { callback(rung.id) } }
                    )
                }
            }

            if (offlineOnly != null && onOfflineOnlyChange != null) {
                SegmentedOptionSelector(
                    selectedIndex = if (offlineOnly) 1 else 0,
                    options = listOf("Online", "Offline only"),
                    onSelect = { index -> onOfflineOnlyChange(index == 1) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimens.ContentSpacing)
                        .testTag("approach_ladder_card_offline_toggle")
                )
            }
        }
    }
}

/** One rendered ladder rung — the label, plus visible-but-subdued capped/needs-network affordances. */
@Composable
private fun RungRow(
    rung: ApproachRungUiModel,
    isEffective: Boolean,
    needsNetwork: Boolean,
    isCapped: Boolean,
    onCapClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val contentColor = if (isEffective) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    // A single testTag per row (Compose semantics carries exactly one TestTag value) -- every
    // row shares this tag so onAllNodesWithTag(...) reflects LIST order (VAPPR-01); a specific
    // rung is targeted in tests via its (fixture-unique) label text instead of a second tag.
    CapControl(
        onClick = onCapClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.ContentSpacing)
            .testTag("approach_ladder_card_rung")
    ) {
        Text(
            text = rung.label,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (isCapped) {
            Text(
                text = "Capped",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (needsNetwork) {
            Text(
                text = "Needs network",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = Dimens.ContentSpacing)
            )
        }
    }
}

/**
 * D-03 swap seam: the DEFAULT tap-a-rung cap control. Wraps [content] in a [Row] that is
 * [Modifier.clickable] ONLY when [onClick] is non-null — a `null` [onClick] renders no clickable
 * semantics node at all (never a disabled one, matching [HeroStatCard]'s `onClick` convention).
 * Swapping the cap-selection mechanism to `SingleChoiceSegmentedButtonRow` later means changing
 * only this composable's body, not the ladder [Column]'s shape or its callers.
 */
@Composable
private fun CapControl(
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            // Merge this row's label + affordance Text children into ONE semantics node (also
            // the right a11y shape for a single tappable/readable rung, mirroring how Material3
            // clickable components merge their content).
            .semantics(mergeDescendants = true) {}
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        content = content
    )
}
