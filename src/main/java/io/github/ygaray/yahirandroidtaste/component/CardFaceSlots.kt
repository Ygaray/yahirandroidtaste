package io.github.ygaray.yahirandroidtaste.component

/**
 * Adaptive card face slot-visibility predicates.
 *
 * Both functions use `isNullOrBlank()` semantics: a slot is visible only when the
 * string is non-null and contains at least one non-whitespace character. Null, empty,
 * and whitespace-only strings all collapse the slot — no reserved height is left
 * (D-07: whitespace-only is treated as empty, slot fully collapses).
 *
 * These are the single source of truth for all card call sites (ADAPT-01, ADAPT-02).
 * Pass the return value to CardBase `headerContent` / `bodyContent` as a null guard:
 * pass `null` for the slot when the predicate returns `false`, and pass the content
 * composable lambda when it returns `true`.
 */

/**
 * Returns `true` when [title] should be rendered in the card's header slot.
 *
 * All four card types (TextCard, ListCard, VoiceCard, AlbumCard) use this predicate
 * for `headerContent` null-guarding (ADAPT-01).
 */
fun titleSlotVisible(title: String?): Boolean = !title.isNullOrBlank()

/**
 * Returns `true` when [body] should be rendered in the card's body slot.
 *
 * Only TextCard uses this predicate for `bodyContent` null-guarding (ADAPT-02).
 * Other card types always render their body (list items, waveform, album grid).
 */
fun bodySlotVisible(body: String?): Boolean = !body.isNullOrBlank()
