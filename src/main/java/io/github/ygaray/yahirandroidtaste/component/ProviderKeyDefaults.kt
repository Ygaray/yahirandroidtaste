package io.github.ygaray.yahirandroidtaste.component

import io.github.ygaray.yahirandroidtaste.model.ProviderKeyLabels

/** Defaults for [ProviderKeyCard]'s key lifecycle (VKEY-01). */
object ProviderKeyDefaults {

    /**
     * Builds the [ProviderKeyLabels] used by [ProviderKeyCard]. A plain function (not composable)
     * so it can be a default argument. Every argument overrides only its own field.
     *
     * @param save Label of the Save button.
     * @param update Label of the Update button.
     * @param clear Label of the Clear button.
     * @param cancel Label of the Cancel button.
     * @param savedPrefix Leading word of the saved-key summary.
     * @param savedMarker Dropdown suffix for providers that have a saved key.
     */
    fun labels(
        save: String = "Save",
        update: String = "Update",
        clear: String = "Clear",
        cancel: String = "Cancel",
        savedPrefix: String = "Saved",
        savedMarker: String = "saved"
    ): ProviderKeyLabels = ProviderKeyLabels(
        save = save,
        update = update,
        clear = clear,
        cancel = cancel,
        savedPrefix = savedPrefix,
        savedMarker = savedMarker
    )
}
