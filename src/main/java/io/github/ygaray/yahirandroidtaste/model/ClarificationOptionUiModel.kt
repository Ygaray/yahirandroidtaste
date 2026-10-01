package io.github.ygaray.yahirandroidtaste.model

/**
 * One tap-to-resolve option of a
 * [io.github.ygaray.yahirandroidtaste.component.ClarificationBar] (VCLAR-01, contract A19) --
 * mirrors [ApproachRungUiModel]'s all-`val` immutable shape so this class stays Compose-inferred
 * STABLE and introduces no `copy()`/`var` ABI trap.
 *
 * [id] is opaque -- the library never interprets, normalizes, case-folds, or reinterprets it; it
 * is only ever passed verbatim to `onSelect`, compared by the consumer via Kotlin's standard
 * `String.equals` (never Unicode-normalized, never case-insensitive). Uniqueness of [id] within
 * one `options` list is the CONSUMER's documented responsibility (T-11-05) -- the library renders
 * duplicate ids/labels exactly as given, never de-duplicating or merging them.
 *
 * @param id Opaque identifier emitted verbatim via `onSelect` -- never rendered as text.
 * @param label Caller-formatted display text rendered on the option's pressable chip.
 */
data class ClarificationOptionUiModel(
    val id: String,
    val label: String
)
