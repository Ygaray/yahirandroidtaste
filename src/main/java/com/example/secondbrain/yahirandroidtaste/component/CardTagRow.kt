package com.example.secondbrain.yahirandroidtaste.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.secondbrain.yahirandroidtaste.model.TagChipUiModel

/**
 * Pure `:designsystem` card-face tag row — the third cross-card-type slot hosted by
 * [CardBase.tagRowContent] (plan 51-02).
 *
 * Mirrors `SearchResultRow`'s read-only tappable tag block (2 chips + tappable `+N`, empty →
 * nothing renders). [tags] arrive pre-sorted by frequency — this composable does not sort; the
 * `:app` caller (plan 51-04) owns the sort and the empty-tag / single-tag data shaping.
 *
 * ## Two tap regions
 * - **Chip tap** → [AppChip]'s inner Surface.clickable consumes the gesture and calls
 *   [onTagClick] with that tag's id (visible chips) or [onSiblingsClick] (the "+N" overflow
 *   chip, Phase 57 FACE-03).
 * - **Card-open tap** lives one layer further out, on [CardBase]'s outer `combinedClickable`
 *   — untouched by this composable.
 *
 * There is no row-band tap target (D-06 unchanged): the "+N" chip is the only siblings-drill
 * affordance added by this row; the card's overflow-menu "See exact siblings" item (gated to
 * 2+ tags at that call site, D-15) remains the sole exact-set drill for 2-tag cards, since "+N"
 * only renders at 3+ tags. Neither is a `Modifier.clickable` on this Row itself.
 *
 * Empty state (D-04): `tags.isEmpty()` renders nothing — no row, no placeholder. (Callers
 * additionally gate the whole slot to null when a card has no tags — WR-01 — so this guard is a
 * defensive second line for any direct `:designsystem` consumer.)
 *
 * WR-02 (testing note): every instance carries the same literal `testTag("card_tag_row")` /
 * `"card_tag_overflow"` — there is no per-card discriminator. A UI test that renders a list of
 * 2+ tagged cards must therefore use `onAllNodesWithTag("card_tag_row")[index]`, not
 * `onNodeWithTag(...)`, which requires a unique match and will throw on multiple cards.
 *
 * @param tags Frequency-pre-sorted tags for this card. Empty renders nothing.
 * @param onTagClick Called with the tag id when an individual visible chip is tapped.
 * @param onSiblingsClick Phase 57 (FACE-03) revival: invoked when the "+N" overflow chip is
 *   tapped (3+ tags only). v1.7 (D-06) stopped invoking this parameter and left it wired only
 *   for call-site compatibility; Phase 57 reconnects it to the tappable overflow chip without
 *   changing this function's signature. Still does NOT back a row-band tap — the row itself has
 *   no `Modifier.clickable`.
 * @param modifier Modifier applied to the outer Row (when tags is non-empty).
 * @param onTagEdit Phase 93 (TMENU-01/D-01 row 3): opens the shared rename-only Edit sheet for
 *   the long-pressed tag. Null (default) omits the "Edit" menu item — every existing call site
 *   keeps rendering a plain [AppChip] with no long-press menu (backward-compat).
 * @param onTagDelete Phase 93 (TMENU-01/04): deletes the tag everywhere (all cards, globally).
 *   Null (default) omits the "Delete tag everywhere" menu item.
 * @param onTagRemoveFromCard Phase 93 (TMENU-01/04/05): removes the tag from THIS card only
 *   (scoped, never aliases to [onTagDelete]). Null (default) omits the "Remove from this card"
 *   menu item. A visible chip renders [TagChipWithContextMenu] instead of a plain [AppChip] only
 *   when at least one of [onTagEdit]/[onTagDelete]/[onTagRemoveFromCard] is non-null.
 */
@Composable
fun CardTagRow(
    tags: List<TagChipUiModel>,
    onTagClick: (tagId: String) -> Unit,
    onSiblingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    onTagEdit: ((tagId: String) -> Unit)? = null,
    onTagDelete: ((tagId: String, name: String) -> Unit)? = null,
    onTagRemoveFromCard: ((tagId: String) -> Unit)? = null
) {
    if (tags.isEmpty()) return

    val hasCapability = onTagEdit != null || onTagDelete != null || onTagRemoveFromCard != null

    // WR-03: [tags] is the card's full tag list (not just the 2 visible chips — the "+N"
    // overflow count above is computed from its size too), so "last tag" is derivable here
    // without threading a separate signal from the caller. Mirrors TagChipEditorContent's
    // proactive last-tag warning ("Removing this tag will leave the card untagged") for the
    // identical "Remove from this card" action — CardTagRow's read-only card-face surface has
    // no persistent warning row to show ahead of the tap, so the warning is folded into the
    // menu item's own label instead (REVIEW.md WR-03 fix option 1).
    val isLastTag = tags.size == 1
    val removeFromCardLabel =
        if (isLastTag) "Remove from this card (leaves it untagged)" else "Remove from this card"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_tag_row"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val visible = tags.take(2)
        val overflow = tags.size - 2
        visible.forEach { tag ->
            if (hasCapability) {
                TagChipWithContextMenu(
                    label = tag.name,
                    isSelected = false,
                    onClick = { onTagClick(tag.id) },
                    modifier = Modifier.weight(1f, fill = false).semantics {
                        contentDescription = "Browse cards tagged ${tag.name}"
                    },
                    onEdit = onTagEdit?.let { edit -> { edit(tag.id) } },
                    onRemoveFromContext = onTagRemoveFromCard?.let { remove -> { remove(tag.id) } },
                    onDelete = onTagDelete?.let { delete -> { delete(tag.id, tag.name) } },
                    removeLabel = removeFromCardLabel
                )
            } else {
                AppChip(
                    label = tag.name,
                    isSelected = false,
                    onClick = { onTagClick(tag.id) },
                    modifier = Modifier.weight(1f, fill = false).semantics {
                        contentDescription = "Browse cards tagged ${tag.name}"
                    }
                )
            }
        }
        // Tappable "+N" overflow chip (Phase 57 FACE-03 revival) — styled isSelected = false,
        // identical to the sibling visible-tag chips above (UI-SPEC §7: blends into the row as
        // "one more tappable chip," not a distinct CTA). Invokes the revived onSiblingsClick,
        // drilling into this card's exact tag-set siblings (AND-intersection incl. source card).
        if (overflow > 0) {
            AppChip(
                label = "+$overflow",
                isSelected = false,
                onClick = onSiblingsClick,
                modifier = Modifier
                    .testTag("card_tag_overflow")
                    .semantics {
                        contentDescription = "$overflow more tags — view this card's exact tag set"
                    }
            )
        }
    }
}
