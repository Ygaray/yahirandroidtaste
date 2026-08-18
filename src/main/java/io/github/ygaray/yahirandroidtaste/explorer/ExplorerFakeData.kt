package io.github.ygaray.yahirandroidtaste.explorer

import io.github.ygaray.yahirandroidtaste.component.MediaThumbnailCell
import io.github.ygaray.yahirandroidtaste.feedback.UndoHistoryEntry
import io.github.ygaray.yahirandroidtaste.feedback.UndoPreview
import io.github.ygaray.yahirandroidtaste.feedback.UndoStatus
import io.github.ygaray.yahirandroidtaste.model.ListItemUiModel
import io.github.ygaray.yahirandroidtaste.model.TagChipUiModel

/**
 * Hand-typed fake data fed to the real `:yahirandroidtaste` composables rendered by the
 * component explorer (SHOW-01). Every value here is a literal — no repository, no Room,
 * no `core.data.*` — this object is the explorer's only "data layer".
 */
object ExplorerFakeData {

    // ---- Titles (D-04 long-title state) ------------------------------------------------

    const val SHORT_TITLE = "Grocery list"
    const val LONG_TITLE =
        "A deliberately long card title used to exercise ellipsis and wrapping behavior " +
            "across every card face and bottom sheet header in the showcase"

    // ---- Timestamps ----------------------------------------------------------------------

    const val CREATED_AT = 1_700_000_000_000L
    const val UPDATED_AT = 1_700_100_000_000L

    // ---- Body text -------------------------------------------------------------------------

    const val NOTE_BODY =
        "Remember to check the pantry before heading to the store — we may already have " +
            "flour and sugar. Pick up basil for the recipe this weekend."

    // ---- List items (EDGE empty: emptyListItems covers the empty-collection case) ---------

    val checklistItems: List<ListItemUiModel> = listOf(
        ListItemUiModel(id = "item-1", text = "Milk", isCompleted = true, sortOrder = 0),
        ListItemUiModel(id = "item-2", text = "Eggs", isCompleted = false, sortOrder = 1),
        ListItemUiModel(id = "item-3", text = "Bread", isCompleted = false, sortOrder = 2)
    )

    val bulletedItems: List<ListItemUiModel> = listOf(
        ListItemUiModel(id = "note-1", text = "Call the dentist", isCompleted = false, sortOrder = 0),
        ListItemUiModel(id = "note-2", text = "Renew library card", isCompleted = false, sortOrder = 1)
    )

    val emptyListItems: List<ListItemUiModel> = emptyList()

    // ---- Tags (Chips family + TagPickerSheet allTags) --------------------------------------

    val tagChips: List<TagChipUiModel> = listOf(
        TagChipUiModel(id = "tag-1", name = "Work", occurrenceCount = 12),
        TagChipUiModel(id = "tag-2", name = "Personal", occurrenceCount = 7),
        TagChipUiModel(id = "tag-3", name = "Recipes", occurrenceCount = 3),
        TagChipUiModel(id = "tag-4", name = "Travel", occurrenceCount = 1)
    )

    // A longer set so the merged TagFilterBar overflows a single line — exercises the
    // collapse-to-one-line + "More"/"Show less" expand behavior in the showcase.
    val manyTagChips: List<TagChipUiModel> = listOf(
        "Work", "Personal", "Recipes", "Travel", "Books", "Fitness", "Ideas", "Home",
        "Finance", "Health", "Projects", "Music", "Movies", "Garden", "Coding"
    ).mapIndexed { index, name ->
        TagChipUiModel(id = "many-$index", name = name, occurrenceCount = 15 - index)
    }

    // ---- Media thumbnails (Album family) ----------------------------------------------------
    // Sources are fake placeholder identifiers — Coil resolves them to an error/empty state
    // (no network call, no crash) since the showcase never reads real device media.

    val mediaThumbnails: List<MediaThumbnailCell> = listOf(
        MediaThumbnailCell(source = "showcase://fake-photo-1.jpg", memoryCacheKey = "showcase-1"),
        MediaThumbnailCell(source = "showcase://fake-photo-2.jpg", memoryCacheKey = "showcase-2"),
        MediaThumbnailCell(source = "showcase://fake-photo-3.jpg", memoryCacheKey = "showcase-3"),
        MediaThumbnailCell(source = "showcase://fake-photo-4.jpg", memoryCacheKey = "showcase-4")
    )

    val emptyMediaThumbnails: List<MediaThumbnailCell> = emptyList()

    // ---- Waveform amplitudes (Voice family / RecordingBottomSheetContent) ------------------

    val waveformBars: List<Float> = listOf(
        0.2f, 0.5f, 0.8f, 0.4f, 0.6f, 0.9f, 0.3f, 0.7f, 0.5f, 0.2f,
        0.4f, 0.6f, 0.8f, 0.5f, 0.3f, 0.7f, 0.9f, 0.4f, 0.2f, 0.6f
    )

    // ---- Undo history (Feedback family, UndoCenterScreen coverage-refresh entry) ------------
    // 3 representative rows exercising 3 distinct UndoPreview variants (Card/Tag/TagOnCard) and
    // all 3 UndoStatus values, so the embedded demo shows the live "Undo" action, the "Undone"
    // label, and the loud error-tinted "Couldn't undo" label in one screen (D-04 loud failures).

    val undoHistoryEntries: List<UndoHistoryEntry> = listOf(
        UndoHistoryEntry(
            id = "undo-1",
            message = "Deleted \"$SHORT_TITLE\"",
            timestamp = UPDATED_AT,
            undoAction = {},
            preview = UndoPreview.Card(title = SHORT_TITLE, cardType = "TEXT")
        ),
        UndoHistoryEntry(
            id = "undo-2",
            message = "Removed tag \"Work\"",
            timestamp = CREATED_AT,
            undoAction = {},
            preview = UndoPreview.Tag(name = "Work"),
            status = UndoStatus.Undone
        ),
        UndoHistoryEntry(
            id = "undo-3",
            message = "Removed tag \"Personal\" from \"$SHORT_TITLE\"",
            timestamp = CREATED_AT,
            undoAction = {},
            preview = UndoPreview.TagOnCard(
                tagName = "Personal",
                cardTitle = SHORT_TITLE,
                cardType = "TEXT"
            ),
            status = UndoStatus.Failed
        )
    )
}
