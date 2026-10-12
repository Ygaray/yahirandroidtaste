package io.github.ygaray.yahirandroidtaste.model

import androidx.compose.runtime.Immutable

/**
 * The saved-key summary of one provider in
 * [io.github.ygaray.yahirandroidtaste.component.ProviderKeyCard]'s `savedKeys` map (VKEY-01).
 *
 * The [fingerprint] is the ONLY thing the card ever shows of a stored key. A consumer must never
 * pass a raw key here (T-23-02): the card is presentational and cannot tell a fingerprint from a
 * key, so it renders the string verbatim.
 *
 * @param fingerprint Caller-derived, display-safe identifier of the stored key, for example
 *   `"sk-ant-…abcd"`.
 */
@Immutable
data class SavedKeyUiModel(
    val fingerprint: String
)
