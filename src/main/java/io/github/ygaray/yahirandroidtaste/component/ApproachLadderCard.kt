package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
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
 * The optional router toggle (VAPPR-04) is a policy toggle rendered below the offline-only toggle.
 * It is NOT per-rung navigation: it never re-orders, filters or changes any rung, and tapping a
 * rung still selects the cap.
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
 *   the cap silently falls back to "no cap at all" — no rung renders "Capped" and no assertion
 *   fires; this is a caller/integration bug the component does not detect. When the cap is
 *   selectable the rows are exposed to accessibility services as radio buttons: the rung whose
 *   id equals [maxTierId] is announced as selected, and a stale id selects no rung.
 * @param onMaxTierChange Invoked with a rung's `id` when it is tapped as the new cap, or `null`
 *   to hide the cap control entirely (D-05) — must be non-null exactly when [maxTierId] is
 *   non-null (enforced via `require`; violating this pairing throws immediately rather than
 *   silently leaving every rung clickable with no visible cap).
 * @param modifier Applied to the outer [Surface].
 * @param unavailableLabel Caller-localizable text of the affordance shown on a disabled rung.
 *   Defaults to the English `"Unavailable"`; rendered verbatim as plain text.
 * @param cappedLabel Caller-localizable text of the affordance shown on a rung above the cap.
 *   Defaults to the English `"Capped"`; rendered verbatim as plain text.
 * @param needsNetworkLabel Caller-localizable text of the affordance shown on an
 *   offline-incapable rung while offline-only is on. Defaults to the English `"Needs network"`;
 *   rendered verbatim as plain text.
 * @param onlineLabel Caller-localizable text of the toggle's "online" segment. Defaults to the
 *   English `"Online"`. The toggle's accessibility state words ("selected" / "not selected") are
 *   announced by [SegmentedOptionSelector] and stay English.
 * @param offlineOnlyLabel Caller-localizable text of the toggle's "offline only" segment. Defaults
 *   to the English `"Offline only"`. The toggle's accessibility state words ("selected" /
 *   "not selected") are announced by [SegmentedOptionSelector] and stay English.
 * @param router Current router state (`true` = ON), or `null` to hide the router toggle entirely.
 * @param onRouterChange Invoked with the tapped segment's target value (`true` = ON) when the
 *   router toggle is tapped, or `null` to hide the toggle entirely — must be non-null exactly when
 *   [router] is non-null (enforced via `require`, exactly like the offline-only pair).
 * @param routerOnLabel Caller-localizable text of the router toggle's "on" segment. Defaults to
 *   the English `"Router on"`; rendered verbatim. The toggle's accessibility state words
 *   ("selected" / "not selected") are announced by [SegmentedOptionSelector] and stay English.
 * @param routerOffLabel Caller-localizable text of the router toggle's "off" segment. Defaults to
 *   the English `"Router off"`; rendered verbatim. The toggle's accessibility state words
 *   ("selected" / "not selected") are announced by [SegmentedOptionSelector] and stay English.
 */
@Composable
fun ApproachLadderCard(
    ladder: List<ApproachRungUiModel>,
    offlineOnly: Boolean? = null,
    onOfflineOnlyChange: ((Boolean) -> Unit)? = null,
    maxTierId: String? = null,
    onMaxTierChange: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    unavailableLabel: String = "Unavailable",
    cappedLabel: String = "Capped",
    needsNetworkLabel: String = "Needs network",
    onlineLabel: String = "Online",
    offlineOnlyLabel: String = "Offline only",
    router: Boolean? = null,
    onRouterChange: ((Boolean) -> Unit)? = null,
    routerOnLabel: String = "Router on",
    routerOffLabel: String = "Router off"
) {
    // Both optional prop pairs below are gated by `require()` rather than left to fail silently:
    // an earlier revision only guarded the maxTierId/onMaxTierChange pair, leaving a caller that
    // violated the (already-documented) offlineOnly/onOfflineOnlyChange pairing with no exception
    // and no signal -- the toggle was just silently omitted. Both pairs now enforce their KDoc
    // contract identically.
    require((maxTierId == null) == (onMaxTierChange == null)) {
        "ApproachLadderCard: maxTierId and onMaxTierChange must both be null or both be non-null " +
            "(got maxTierId=$maxTierId, onMaxTierChange=${if (onMaxTierChange == null) "null" else "non-null"})"
    }
    require((offlineOnly == null) == (onOfflineOnlyChange == null)) {
        "ApproachLadderCard: offlineOnly and onOfflineOnlyChange must both be null or both be " +
            "non-null (got offlineOnly=$offlineOnly, onOfflineOnlyChange=" +
            "${if (onOfflineOnlyChange == null) "null" else "non-null"})"
    }
    require((router == null) == (onRouterChange == null)) {
        "ApproachLadderCard: router and onRouterChange must both be null or both be non-null " +
            "(got router=$router, onRouterChange=${if (onRouterChange == null) "null" else "non-null"})"
    }
    val effectiveOfflineOnly = offlineOnly == true
    val capRank = maxTierId?.let { id -> ladder.firstOrNull { it.id == id }?.rank } ?: Int.MAX_VALUE

    Surface(
        shape = MaterialTheme.expressive.cardShapeLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.testTag("approach_ladder_card_surface")
    ) {
        Column(modifier = Modifier.padding(Dimens.HorizontalPadding)) {
            Column(
                // VA11Y-01 review fix (WR-02): the rungs are Role.RadioButton + Selected only when the
                // cap is selectable, so only then expose them as a radio group (position/group context
                // for accessibility services). A cap-less ladder is not announced as a group.
                modifier = if (onMaxTierChange != null) Modifier.selectableGroup() else Modifier,
                verticalArrangement = Arrangement.spacedBy(Dimens.HairlineSpacing)
            ) {
                ladder.forEach { rung ->
                    val needsNetwork = effectiveOfflineOnly && !rung.offlineCapable
                    val isCapped = rung.rank > capRank
                    val isEffective = rung.enabled && !needsNetwork && !isCapped
                    RungRow(
                        rung = rung,
                        isEffective = isEffective,
                        needsNetwork = needsNetwork,
                        isCapped = isCapped,
                        isSelected = onMaxTierChange != null && rung.id == maxTierId,
                        unavailableLabel = unavailableLabel,
                        cappedLabel = cappedLabel,
                        needsNetworkLabel = needsNetworkLabel,
                        onCapClick = onMaxTierChange?.let { callback -> { callback(rung.id) } }
                    )
                }
            }

            if (offlineOnly != null && onOfflineOnlyChange != null) {
                SegmentedOptionSelector(
                    selectedIndex = if (offlineOnly) 1 else 0,
                    options = listOf(onlineLabel, offlineOnlyLabel),
                    onSelect = { index -> onOfflineOnlyChange(index == 1) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimens.ContentSpacing)
                        .testTag("approach_ladder_card_offline_toggle")
                )
            }

            if (router != null && onRouterChange != null) {
                SegmentedOptionSelector(
                    selectedIndex = if (router) 1 else 0,
                    options = listOf(routerOffLabel, routerOnLabel),
                    onSelect = { index -> onRouterChange(index == 1) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimens.ContentSpacing)
                        .testTag("approach_ladder_card_router_toggle")
                )
            }
        }
    }
}

// v2.4.x binary-compatibility shim (INC-2026-10-05-02 F1): restores the pre-v2.5 JVM descriptor for
// consumers compiled against v2.4.x. Hidden from Kotlin and Java source -- do not call it or
// document it as API.
@Deprecated("Binary compatibility with v2.4.x", level = DeprecationLevel.HIDDEN)
@Composable
fun ApproachLadderCard(
    ladder: List<ApproachRungUiModel>,
    offlineOnly: Boolean? = null,
    onOfflineOnlyChange: ((Boolean) -> Unit)? = null,
    maxTierId: String? = null,
    onMaxTierChange: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) = ApproachLadderCard(
    ladder = ladder,
    offlineOnly = offlineOnly,
    onOfflineOnlyChange = onOfflineOnlyChange,
    maxTierId = maxTierId,
    onMaxTierChange = onMaxTierChange,
    modifier = modifier
)

/** One rendered ladder rung — the label, plus visible-but-subdued capped/needs-network affordances. */
@Composable
private fun RungRow(
    rung: ApproachRungUiModel,
    isEffective: Boolean,
    needsNetwork: Boolean,
    isCapped: Boolean,
    isSelected: Boolean,
    unavailableLabel: String,
    cappedLabel: String,
    needsNetworkLabel: String,
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
        selected = isSelected,
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
        if (!rung.enabled) {
            Text(
                text = unavailableLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isCapped) {
            Text(
                text = cappedLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (needsNetwork) {
            Text(
                text = needsNetworkLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = Dimens.ContentSpacing)
            )
        }
    }
}

/**
 * D-03 swap seam: the DEFAULT tap-a-rung cap control. Wraps [content] in a [Row] that is
 * [Modifier.selectable] with [Role.RadioButton] and a Selected state ([selected]) ONLY when
 * [onClick] is non-null — a `null` [onClick] renders no role, no selected state and no click
 * semantics at all (never a disabled one, matching [HeroStatCard]'s `onClick` convention), so a
 * cap-less ladder is never announced as an empty radio group. The row also gets the minimum
 * interactive size only when [onClick] is non-null, so cap-less rows stay compact
 * (conditional-render-no-dead-space; D-03/D-04 — locked reading of research item A4).
 * Swapping the cap-selection mechanism to `SingleChoiceSegmentedButtonRow` later means changing
 * only this composable's body, not the ladder [Column]'s shape or its callers.
 */
@Composable
private fun CapControl(
    onClick: (() -> Unit)?,
    selected: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            // D-04: grow the interactive target to the minimum size INSIDE the caller's outer
            // padding. The resulting row-pitch growth is accepted (Gate-2 visual check).
            .then(if (onClick != null) Modifier.minimumInteractiveComponentSize() else Modifier)
            // Merge this row's label + affordance Text children into ONE semantics node (also
            // the right a11y shape for a single tappable/readable rung, mirroring how Material3
            // clickable components merge their content).
            .semantics(mergeDescendants = true) {}
            .then(
                if (onClick != null) {
                    Modifier.selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
                } else {
                    Modifier
                }
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        content = content
    )
}
