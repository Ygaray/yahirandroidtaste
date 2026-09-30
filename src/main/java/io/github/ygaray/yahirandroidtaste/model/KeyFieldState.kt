package io.github.ygaray.yahirandroidtaste.model

/**
 * Render-only validation state for an API-key entry field
 * ([io.github.ygaray.yahirandroidtaste.component.ProviderKeyCard], VSET-01). This describes HOW
 * the card should render its key field (masked/error copy) — it is display state the consumer
 * computes and hoists in; the library never validates a key or makes a network call of its own
 * (INV-01).
 */
sealed interface KeyFieldState {
    /** No key entered yet. */
    data object Empty : KeyFieldState

    /** A key has been entered but not yet validated. */
    data object Entered : KeyFieldState

    /** The consumer is currently validating the entered key (e.g. an in-flight network check). */
    data object Validating : KeyFieldState

    /** The entered key validated successfully. */
    data object Valid : KeyFieldState

    /**
     * The entered key failed validation.
     *
     * @param reason Caller-formatted, human-readable failure reason shown as the field's
     *   supporting/error text.
     */
    data class Invalid(val reason: String) : KeyFieldState
}
