package io.github.ygaray.yahirandroidtaste.theme

/**
 * Supported theme modes for the app. Canonical owner per D-04 — moved out of the app's
 * settings feature package to remove a feature-layer leak from the theme layer. Persisted by
 * `:app`'s ThemePreferenceManager via `.name` (a plain string) — this move does not affect
 * stored DataStore values.
 */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    companion object {
        fun fromString(value: String): ThemeMode =
            entries.firstOrNull { it.name == value } ?: SYSTEM
    }
}
