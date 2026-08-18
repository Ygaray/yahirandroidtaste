package io.github.ygaray.yahirandroidtaste.explorer

/**
 * A registry-native (D-03), Bundle-safe live prop/state knob descriptor for the Playground
 * section (EXPLORE-04). Exactly two variants exist this phase — [Toggle] and [Enum] — per D-04
 * (a Slider variant is explicitly deferred to a future phase). [Control] is a plain data
 * descriptor with no lambdas inside it, mirroring [ComponentRegistry.StateCell]'s
 * "label + optional payload" shape but deliberately holding no composable itself, so every
 * instance is trivially testable (`equals`/`hashCode`) and safe to key a [PlaygroundState] map by.
 */
sealed interface Control {
    val label: String

    /** A boolean prop/state knob, rendered as a `Switch` + label (D-04). */
    data class Toggle(
        override val label: String,
        val default: Boolean = false
    ) : Control

    /**
     * An enum/state-selector knob, rendered as a `SingleChoiceSegmentedButtonRow` (D-04).
     * [options] doubles as both the display labels and the stable value keys — the curated
     * `preview` lambda maps the selected label back to the real prop type (component-specific,
     * D-05).
     */
    data class Enum(
        override val label: String,
        val options: List<String>,
        val default: String = options.first()
    ) : Control {
        init {
            // IN-01: fail fast with a clear message at construction time rather than an
            // unqualified NoSuchElementException from `options.first()` during registry
            // static-initialization, if a future author ever supplies an empty options list.
            require(options.isNotEmpty()) { "Control.Enum(label = \"$label\") requires a non-empty options list." }
        }
    }
}
