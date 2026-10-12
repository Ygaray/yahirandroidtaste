package io.github.ygaray.yahirandroidtaste.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * A container/content color pair for a tinted surface (a "tone"), such as a warning or error card.
 *
 * A plain class on purpose, not a `data class`: a data class would publish `copy` and `componentN`
 * members into the public API surface. Equality is by value, so two tones built from the same
 * colors are interchangeable (and stable for Compose skipping).
 *
 * @property container Fill color of the tinted surface.
 * @property content Color for text and icons drawn on [container].
 */
@Immutable
class ToneColors(val container: Color, val content: Color) {

    override fun equals(other: Any?): Boolean =
        this === other ||
            (other is ToneColors && container == other.container && content == other.content)

    override fun hashCode(): Int = 31 * container.hashCode() + content.hashCode()

    override fun toString(): String = "ToneColors(container=$container, content=$content)"
}

/**
 * Default [ToneColors] resolved from the ACTIVE [MaterialTheme.colorScheme] at call time. The hub
 * carries no consumer color values: each default maps a tone onto standard Material 3 roles.
 *
 * Dynamic color note: [warn] reads the `tertiary` family, which is amber only in the hub's own
 * palettes. When dynamic color (Material You) makes `tertiary` non-amber, consumers should pass
 * their own [ToneColors] tokens instead of relying on [warn].
 */
object ToneDefaults {

    /** Error tone: `errorContainer` fill with `error` content. */
    @Composable
    fun error(): ToneColors = MaterialTheme.colorScheme.let { ToneColors(it.errorContainer, it.error) }

    /**
     * Warning tone: `tertiaryContainer` fill with `onTertiaryContainer` content. Pass your own
     * [ToneColors] when dynamic color makes `tertiary` non-amber.
     */
    @Composable
    fun warn(): ToneColors =
        MaterialTheme.colorScheme.let { ToneColors(it.tertiaryContainer, it.onTertiaryContainer) }
}

/** Semantic tone of a piece of content, mapped to concrete colors by the component that takes it. */
enum class ContentTone { Default, Muted, Warn, Error }
