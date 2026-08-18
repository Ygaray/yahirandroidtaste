package com.example.secondbrain.yahirandroidtaste.explorer

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.snapshots.SnapshotStateMap

/**
 * Single generic live-control state container for every Playground section (D-03) — a
 * `SnapshotStateMap<String, Any>` keyed by [Control.label], so heterogeneous Boolean/String knob
 * values are all `rememberSaveable` (via [Saver]) without a per-component custom Saver. This
 * generalizes [ExplorerEntry]'s existing `rememberSaveable { mutableStateOf(ThemeMode.LIGHT) }`
 * toggle-state precedent to a heterogeneous multi-key case (a Kotlin enum is `Serializable` and
 * needs no custom Saver; mixed Boolean/String values in one map do).
 */
class PlaygroundState internal constructor(
    private val values: SnapshotStateMap<String, Any>
) {
    /** Reads the current value of a [Control.Toggle], falling back to its declared default. */
    fun boolean(control: Control.Toggle): Boolean =
        values[control.label] as? Boolean ?: control.default

    /** Reads the current value of a [Control.Enum], falling back to its declared default. */
    fun selected(control: Control.Enum): String =
        values[control.label] as? String ?: control.default

    /** Mutates a [Control.Toggle]'s live value — the Switch's `onCheckedChange` target. */
    fun setBoolean(control: Control.Toggle, value: Boolean) {
        values[control.label] = value
    }

    /** Mutates a [Control.Enum]'s live value — a `SegmentedButton`'s `onClick` target. */
    fun setSelected(control: Control.Enum, value: String) {
        values[control.label] = value
    }

    companion object {
        /** Seeds a fresh [PlaygroundState] with every [controls] entry's declared default. */
        fun initial(controls: List<Control>): PlaygroundState {
            val map = mutableStateMapOf<String, Any>()
            controls.forEach { control ->
                when (control) {
                    is Control.Toggle -> map[control.label] = control.default
                    is Control.Enum -> map[control.label] = control.default
                }
            }
            return PlaygroundState(map)
        }

        /** Bundle-safe: every value is Boolean or String, both natively savable via `mapSaver`. */
        val Saver: Saver<PlaygroundState, Any> = mapSaver(
            save = { it.values.toMap() },
            restore = { restored ->
                @Suppress("UNCHECKED_CAST")
                PlaygroundState(
                    mutableStateMapOf<String, Any>().apply { putAll(restored as Map<String, Any>) }
                )
            }
        )
    }
}
