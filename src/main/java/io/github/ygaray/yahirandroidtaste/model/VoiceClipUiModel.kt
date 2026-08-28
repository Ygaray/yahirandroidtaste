package io.github.ygaray.yahirandroidtaste.model

/**
 * One per-clip row in [io.github.ygaray.yahirandroidtaste.component.VoiceCard]'s read-only
 * clip-list (Phase 129 DS-03 D-02) — a UI projection consumed by `VoiceCard`'s aggregate
 * clip-count header pill and its capped per-clip mini-rows, following the same convention as
 * [ListItemUiModel].
 *
 * The hub renders clips in the exact order the caller supplies and applies no sorting, filtering
 * or deduplication of its own — [sortOrder] is a caller-supplied zero-based ordinal rendered
 * one-based in a clip row's index label; it does not drive reordering.
 *
 * @param id Stable per-clip identifier.
 * @param sortOrder Caller-supplied zero-based ordinal. A clip row's index label renders this
 *   value one-based (`sortOrder + 1`); the hub trusts and preserves the caller's list order rather
 *   than sorting by this field.
 * @param durationMs This clip's own duration in milliseconds.
 * @param samplesPath Path to this clip's companion `.bin` amplitude-sample file — the same
 *   on-disk format `VoiceCard`'s existing overview strip already decodes. Null (default) renders
 *   a blank waveform track for this row; a missing or corrupt file degrades to the same blank
 *   track rather than throwing.
 */
data class VoiceClipUiModel(
    val id: String,
    val sortOrder: Int,
    val durationMs: Long,
    val samplesPath: String? = null
)
