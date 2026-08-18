package io.github.ygaray.yahirandroidtaste.feedback

/**
 * Pure snapshot preview payload carried by an [UndoHistoryEntry] — Phase 58 (UNDO-04).
 *
 * D-03: the preview info (title, type/kind, thumbnail path or equivalent) is CAPTURED at
 * `emitTrackedWithUndo` time, not resolved later from a live repository lookup. This is
 * what lets the Undo Center's long-press peek survive the underlying card/photo being
 * purged, and it is what keeps `:designsystem/feedback` pure — no repository/Coil/Hilt
 * injection into this module. Every field is a plain `String`/`String?`, so structural
 * equality "just works" (no `AtomicBoolean`-style special-casing like [UndoHistoryEntry]'s
 * `consumedGuard` needs).
 *
 * [Card.thumbnailPath] and [Photo.thumbnailPath] are already-resolved ABSOLUTE file path
 * strings supplied verbatim by the producer — this pure module never calls `File()` or
 * `resolveFile()` on them; it only ever displays what it is handed.
 */
sealed interface UndoPreview {

    /** A text/list/voice/album card's identity — thumbnail is only present for Album cards. */
    data class Card(
        val title: String,
        val cardType: String,
        val thumbnailPath: String? = null
    ) : UndoPreview

    /** A single photo within an album. */
    data class Photo(
        val thumbnailPath: String,
        val albumTitle: String
    ) : UndoPreview

    /** A standalone tag (e.g. tag deleted/demoted). */
    data class Tag(
        val name: String
    ) : UndoPreview

    /** A relationship link between two cards. */
    data class Link(
        val cardATitle: String,
        val cardAType: String,
        val cardBTitle: String,
        val cardBType: String
    ) : UndoPreview

    /** A tag assignment on a specific card (e.g. tag removed from a card). */
    data class TagOnCard(
        val tagName: String,
        val cardTitle: String,
        val cardType: String
    ) : UndoPreview
}
