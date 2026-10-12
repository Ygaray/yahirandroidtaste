package io.github.ygaray.yahirandroidtaste.model

import androidx.compose.runtime.Immutable

/**
 * Caller-localizable text of
 * [io.github.ygaray.yahirandroidtaste.component.ProviderKeyCard]'s key lifecycle (VKEY-01). The
 * defaults are the English strings; each is rendered verbatim as plain text.
 *
 * The saved-key summary renders [savedPrefix], a middle dot and the fingerprint as one line, for
 * example `Saved · sk-ant-…abcd`. [savedMarker] is the suffix appended, after a middle dot, to a
 * provider's dropdown row when that provider has a saved key.
 *
 * @param save Label of the Save button.
 * @param update Label of the Update button.
 * @param clear Label of the Clear button.
 * @param cancel Label of the Cancel button.
 * @param savedPrefix Leading word of the saved-key summary.
 * @param savedMarker Dropdown suffix for providers that have a saved key.
 */
@Immutable
data class ProviderKeyLabels(
    val save: String = "Save",
    val update: String = "Update",
    val clear: String = "Clear",
    val cancel: String = "Cancel",
    val savedPrefix: String = "Saved",
    val savedMarker: String = "saved"
)
