package com.example.secondbrain.yahirandroidtaste.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.secondbrain.yahirandroidtaste.model.TagChipUiModel
import com.example.secondbrain.yahirandroidtaste.model.TagSortMode

/**
 * Maximum tag-name length enforced at input (D-03, Phase 29-01).
 * Matches the app-side tag-management screen's own cap of the same name — kept as a
 * designsystem-package constant (single public source of truth since Phase 46 Plan 2) to avoid
 * a core-to-feature layering dependency. Update both if the cap changes. Public so both the
 * app-resident tag-edit ViewModel's inline-create path (WR-01) and this composable enforce the
 * identical cap from one source.
 */
const val TAG_NAME_MAX_LENGTH = 50

/**
 * Multi-add modal bottom sheet for tag assignment (D-01 / D-01a / D-01c).
 *
 * Behavior:
 *  - Live-filters [allTags] case-insensitively on typed [query] (Kotlin in-memory filter, no new query)
 *    — the field is filter-only (D-07); it no longer offers inline creation.
 *  - Each matching tag is a [FilterChip]; tapping toggles selection (multi-add in one session)
 *  - Tag creation happens via the always-first create-chip (WIDGET-02, D-05/D-06), which opens
 *    [BulkCreatePopup]; duplicate-name and length validation live in that popup's `validate`
 *    closure at the call site (D-07), not in this composable.
 *  - Empty state (no tags + blank query): "Tap \"Create tag\" to make your first tag" (D-01c)
 *  - Done button commits all selected IDs then dismisses (via onDone + onDismiss callbacks)
 *  - Dismissing without Done discards staged selections (no partial commit)
 *
 * UI-SPEC §3 — layout, spacing, copy, color all exact.
 *
 * @param existingTagIds    Tag IDs already on the card (informational; callers may use to pre-select).
 * @param allTags           Full list of active tags for the picker (getAllActiveTags flow snapshot).
 * @param onDone            Called with the list of selected tag IDs when Done is tapped.
 * @param onCreate          Called with the trimmed, validated name when the create-chip's
 *                          [BulkCreatePopup] submits (D-08).
 * @param onDismiss         Called when the sheet is dismissed (backdrop tap or system back).
 * @param maxSelections     Maximum number of selectable tags. Default: unlimited (multi-add).
 *                          Pass 1 for single-select reuse in Phase 24 (D-01b forward seam).
 * @param sortMode          Currently active tag sort mode (D-02 — shared with the caller's other
 *                          canonical widget surface). Defaulted so existing callers keep compiling
 *                          until the :app wrapper wires a real value (Phase 48 Plan 05).
 * @param onSortModeChange  Invoked when the header's [SortControl] selects a new mode.
 *                          Defaulted to a no-op.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagPickerSheet(
    existingTagIds: Set<String> = emptySet(),
    allTags: List<TagChipUiModel>,
    onDone: (List<String>) -> Unit,
    onCreate: (String) -> Unit,
    onDismiss: () -> Unit,
    maxSelections: Int = Int.MAX_VALUE,
    sortMode: TagSortMode = TagSortMode.DEFAULT,
    onSortModeChange: (TagSortMode) -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    SheetScaffold(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        TagPickerSheetContent(
            existingTagIds = existingTagIds,
            allTags = allTags,
            onDone = onDone,
            onCreate = onCreate,
            onDismiss = onDismiss,
            maxSelections = maxSelections,
            sortMode = sortMode,
            onSortModeChange = onSortModeChange
        )
    }
}

/**
 * The body of [TagPickerSheet] without the ModalBottomSheet wrapper.
 *
 * Extracted as a public composable (was `internal` before Phase 46 Plan 2) so the :app-resident
 * `com.example.secondbrain.core.ui.components.TagPickerSheetTest` (Robolectric; :designsystem has
 * no test source set) can render it directly across the module boundary without popup-window
 * node-tree complications (D-12 unit-test contingency — plan 40-01 / 40-VALIDATION.md Wave 0
 * Requirements). `internal` visibility does not cross a Gradle module boundary, hence public.
 *
 * Plan 40-04 must place `Modifier.testTag("tag_flow_row")` on the FlowRow **in this composable**
 * (not on the ModalBottomSheet wrapper), since this is where the chip list lives.
 *
 * @param sortMode          Currently active tag sort mode (D-02). Defaulted so existing callers
 *                          (incl. `TagPickerSheetTest`) keep compiling.
 * @param onSortModeChange  Invoked when the header's [SortControl] selects a new mode. Defaulted
 *                          to a no-op. This composable never sorts [allTags] itself (Phase 48 —
 *                          `:designsystem` stays algorithm-free; sorted lists arrive pre-ordered).
 */
@Composable
fun TagPickerSheetContent(
    existingTagIds: Set<String> = emptySet(),
    allTags: List<TagChipUiModel>,
    onDone: (List<String>) -> Unit,
    onCreate: (String) -> Unit,
    onDismiss: () -> Unit,
    maxSelections: Int = Int.MAX_VALUE,
    sortMode: TagSortMode = TagSortMode.DEFAULT,
    onSortModeChange: (TagSortMode) -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var selectedIds by remember { mutableStateOf<Set<String>>(existingTagIds) }
    var pendingCreationNames by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showBulkCreatePopup by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // TAG-09 (D-08/D-09): autofocus the search/create field and raise the IME on every open —
    // LaunchedEffect(Unit) fires on each fresh composition of TagPickerSheetContent (not just the
    // first-ever open), so re-opening the sheet re-focuses. requestFocus() is guarded against the
    // node not yet being attached on the very first composition pass (BulkCreatePopup precedent);
    // keyboardController?.show() is the IME-raise half that precedent omits (RESEARCH Pitfall 1).
    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: IllegalStateException) {
            // Defensive: node may not be attached yet on the very first composition pass.
        }
        keyboardController?.show()
    }

    // WR-02 (D-07): the field is filter-only now — no create-row guard to keep in sync with the
    // filter, but the trim still matters: filtering on the raw query means a trailing space
    // (e.g. "Work ") makes "Work".contains("Work ") false, hiding the existing tag.
    val trimmedQuery = query.trim()

    // Live Kotlin filter — no new DAO query (D-04 constraint: no new query/DAO work)
    val filteredTags = allTags.filter { tag ->
        tag.name.contains(trimmedQuery, ignoreCase = true)
    }

    // BUG-02 (D-08 — widened to a Set<String>): reactively add every just-created tag to
    // selectedIds when Room emits it. The LaunchedEffect fires on each recomposition where
    // allTags or pendingCreationNames changes, looping over every still-pending name so a
    // "Create and more" burst auto-selects EVERY tag made in the session, not just the last.
    // Each resolved name is removed from the set so the effect does not re-fire for it
    // (names are added to the set BEFORE onCreate is invoked — see the create-chip's onAction).
    LaunchedEffect(allTags, pendingCreationNames) {
        val stillPending = pendingCreationNames.toList()
        for (pending in stillPending) {
            val newTag = allTags.find { it.name.equals(pending, ignoreCase = true) }
            if (newTag != null) {
                // WR-06: creation must respect the same maxSelections cap the chip onClick enforces.
                // Unconditional append let a user with maxSelections=1 (Phase 24 single-select seam)
                // end up with two selected ids after creating a tag. For single-select, replace;
                // otherwise append only while under the cap.
                selectedIds = when {
                    maxSelections == 1 -> setOf(newTag.id)
                    selectedIds.size < maxSelections -> selectedIds + newTag.id
                    else -> selectedIds
                }
                pendingCreationNames = pendingCreationNames - pending
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
            // --- Search/create field + sort control header (UI-SPEC §3, Locked Decision 2) ---
            // SortControl is trailing, vertically centered with the field (UI-SPEC placement
            // spec). This composable never sorts allTags itself — sorted lists arrive pre-ordered.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                ClearableTextField(
                    value = query,
                    // D-03 (Phase 29-01) — block at input (cap also bounds search text; tag names
                    // cannot exceed 50 chars, so search text beyond that matches nothing anyway)
                    onValueChange = { if (it.length <= TAG_NAME_MAX_LENGTH) query = it },
                    label = { Text("Search/filter tags") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .testTag("tag_search_field"),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                )
                Spacer(Modifier.width(8.dp))
                SortControl(
                    sortMode = sortMode,
                    options = TagSortMode.entries,
                    optionLabel = { it.label },
                    onSortModeChange = onSortModeChange
                )
            }

            // --- Tag list ---
            Column(
                modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())
            ) {
                // Selectable existing tag chips + the always-first create-chip — wrapping ChipBar
                // (LAYOUT-04 D-10; migrated onto the shared :designsystem container per
                // WIDGET-04/D-15/D-16 — behavior-preserving). The create-chip is ChipBar's
                // leadingContent slot (WIDGET-02, D-06): always rendered — NOT gated on
                // filteredTags being non-empty — so it remains present and first even when an
                // empty filter query yields no tag matches (D-06 empty edge).
                ChipBar(
                    items = filteredTags,
                    key = { it.id },
                    leadingContent = {
                        // tertiaryContainer-filled AssistChip (D-05) — secondaryContainer is
                        // already "selected FilterChip" and primaryContainer is already
                        // "selected-icon"/reveal-confirm Edit on this exact surface, so
                        // tertiaryContainer is the one unclaimed, genuinely distinct signal.
                        // Contrast verified: 13.4:1 light / 8.0:1 dark (WCAG AA, Task 3 test).
                        AssistChip(
                            onClick = { showBulkCreatePopup = true },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                labelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                leadingIconContentColor = MaterialTheme.colorScheme.onTertiaryContainer
                            ),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            },
                            label = {
                                Text(
                                    text = "Create tag",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            modifier = Modifier.testTag("create_tag_chip")
                        )
                    },
                    itemContent = { tag ->
                        FilterChip(
                            selected = tag.id in selectedIds,
                            onClick = {
                                selectedIds = if (tag.id in selectedIds) {
                                    selectedIds - tag.id
                                } else {
                                    if (selectedIds.size < maxSelections) {
                                        selectedIds + tag.id
                                    } else {
                                        selectedIds
                                    }
                                }
                            },
                            label = {
                                Text(
                                    text = tag.name,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        )
                    },
                    modifier = Modifier.padding(vertical = 8.dp),
                    testTag = "tag_flow_row"
                )

                // Empty state — shown when no tags exist at all and query is blank (D-01c)
                if (allTags.isEmpty() && query.isBlank()) {
                    Text(
                        text = "Tap \"Create tag\" to make your first tag",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // --- Done button (primary commit action — colorScheme.primary fill via M3 Button default) ---
            Button(
                onClick = {
                    onDone(selectedIds.toList())
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Done", style = MaterialTheme.typography.labelLarge)
            }

            Spacer(Modifier.height(24.dp))
        }

    // Create-chip popup (WIDGET-02, D-05/D-06) — opened by the leadingContent AssistChip above.
    // The uniqueness + length guard relocates here from the old inline "Create '<query>'" row
    // (D-07): validate runs on every submit, before onAction ever fires.
    if (showBulkCreatePopup) {
        BulkCreatePopup(
            onDismissRequest = { showBulkCreatePopup = false },
            actionLabel = "Create",
            actionAndMoreLabel = "Create and more",
            onAction = { name ->
                // SET BEFORE onCreate (BUG-02/D-08) — every name, every time, before onCreate fires.
                pendingCreationNames = pendingCreationNames + name
                onCreate(name)
            },
            validate = { candidate ->
                when {
                    candidate.isBlank() -> "Name cannot be blank"
                    candidate.length > TAG_NAME_MAX_LENGTH ->
                        "Name must be $TAG_NAME_MAX_LENGTH characters or fewer"
                    allTags.any { it.name.equals(candidate, ignoreCase = true) } ||
                        pendingCreationNames.any { it.equals(candidate, ignoreCase = true) } ->
                        "A tag with this name already exists"
                    else -> null
                }
            }
        )
    }
}
