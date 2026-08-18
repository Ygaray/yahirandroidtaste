package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.model.TagChipUiModel
import io.github.ygaray.yahirandroidtaste.model.TagSortMode
import java.util.Locale

/**
 * Designsystem-native mirror of `RecordingBottomSheetViewModel.SheetState` (D-03) —
 * the pure UI states [RecordingBottomSheetContent] renders. The `:app` wrapper maps the
 * ViewModel's `SheetState` onto this enum; nothing here references the ViewModel.
 */
enum class RecordingSheetUiState { RECORDING, PAUSED, TITLE, IDLE }

/**
 * RecordingBottomSheetContent — pure `ModalBottomSheet` body for voice recording and saving,
 * driven entirely by hoisted params (D-03/D-05). No ViewModel, no foreground-service start,
 * no permission launcher — those stay in the `:app` `RecordingBottomSheet` wrapper.
 *
 * States (driven by [uiState]):
 * - RECORDING: elapsed timer + live waveform bars + Pause + Stop buttons + pulsing ring
 * - PAUSED: timer frozen + dimmed waveform + Resume + Stop buttons
 * - TITLE: title ClearableTextField + capture-time tag row + "Save Recording" + "Discard" buttons
 * - IDLE: rendered as the RECORDING/PAUSED layout (waveform empty) while the wrapper is
 *   still resolving the permission/auto-start sequence
 *
 * @param uiState Current sheet UI state (mirrors the ViewModel's SheetState).
 * @param elapsedSeconds Elapsed recording time in seconds.
 * @param amplitudeBars Live waveform amplitude samples (last-100-bars slicing is the
 *   caller's responsibility — see the RECORDING/PAUSED branch below for the same trim
 *   this component always applies).
 * @param titleText Current value of the title text field (TITLE state).
 * @param defaultTitle Timestamp-based default title, used as placeholder text.
 * @param permissionDenied True when RECORD_AUDIO was denied — renders the explanation message
 *   instead of the normal sheet body.
 * @param onTitleChange Invoked as the user edits the title field.
 * @param onPause Invoked when Pause is tapped while RECORDING.
 * @param onResume Invoked when Resume is tapped while PAUSED.
 * @param onStop Invoked when Stop is tapped (RECORDING or PAUSED).
 * @param onSave Invoked when "Save Recording" is tapped (TITLE state).
 * @param onDiscard Invoked when "Discard" is tapped (TITLE state).
 * @param onDismissRequest Invoked when the sheet requests dismissal (swipe-down/outside-tap).
 * @param onClosePermissionDenied Invoked when "Close" is tapped on the permission-denied
 *   message. Defaults to [onDismissRequest].
 * @param bufferedTags Capture-time tag buffer (TITLE state only, D-04/49-03) — tags chosen so
 *   far, mounted via the pure [TagChipEditorContent] (no `:app` `TagChipEditor` wrapper — no
 *   `cardId` exists yet at capture time).
 * @param allTags All active tags for the picker's autocomplete/create list.
 * @param onAddBufferedTags Invoked with newly-picked tag ids to append to the buffer.
 * @param onRemoveBufferedTag Invoked with a tag id to remove from the buffer (no undo, D-05).
 * @param onCreateAndBufferTag Invoked with a typed name to create-then-buffer a new tag.
 * @param sortMode Currently active tag sort mode (TSORT-01), sourced from the global preference.
 * @param onSortModeChange Invoked when the leading sort control selects a new mode.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingBottomSheetContent(
    uiState: RecordingSheetUiState,
    elapsedSeconds: Long,
    amplitudeBars: List<Float>,
    titleText: String,
    defaultTitle: String,
    permissionDenied: Boolean,
    onTitleChange: (String) -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    onDismissRequest: () -> Unit,
    onClosePermissionDenied: () -> Unit = onDismissRequest,
    bufferedTags: List<TagChipUiModel> = emptyList(),
    allTags: List<TagChipUiModel> = emptyList(),
    onAddBufferedTags: (List<String>) -> Unit = {},
    onRemoveBufferedTag: (String) -> Unit = {},
    onCreateAndBufferTag: (String) -> Unit = {},
    sortMode: TagSortMode = TagSortMode.DEFAULT,
    onSortModeChange: (TagSortMode) -> Unit = {},
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    SheetScaffold(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        when {
            permissionDenied -> {
                // T-05-05: show explanation if permission was denied
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Microphone access is required to record voice notes.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    TextButton(onClick = onClosePermissionDenied) { Text("Close") }
                }
                // Bottom spacing so content clears the navigation bar
                Spacer(Modifier.height(48.dp))
            }

            uiState == RecordingSheetUiState.TITLE -> {
                // ── TITLE state (D-01/D-02/D-03) — NameAndTagsEditor, empty header (A3: the
                // recording controls belong to the earlier RECORDING/PAUSED states, not here).
                // NameAndTagsEditor supplies its own 24dp bottom clearance — no extra Spacer.
                TitleStateContent(
                    titleText = titleText,
                    defaultTitle = defaultTitle,
                    onTitleChange = onTitleChange,
                    onSave = onSave,
                    onDiscard = onDiscard,
                    bufferedTags = bufferedTags,
                    allTags = allTags,
                    onAddBufferedTags = onAddBufferedTags,
                    onRemoveBufferedTag = onRemoveBufferedTag,
                    onCreateAndBufferTag = onCreateAndBufferTag,
                    sortMode = sortMode,
                    onSortModeChange = onSortModeChange
                )
            }

            else -> {
                // ── RECORDING or PAUSED state (or IDLE while permission not yet answered) ──
                val isPaused = uiState == RecordingSheetUiState.PAUSED
                RecordingStateContent(
                    elapsedSeconds = elapsedSeconds,
                    amplitudeBars = amplitudeBars,
                    isPaused = isPaused,
                    onPauseResume = { if (isPaused) onResume() else onPause() },
                    onStop = onStop
                )
                // Bottom spacing so content clears the navigation bar
                Spacer(Modifier.height(48.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// RECORDING state content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RecordingStateContent(
    elapsedSeconds: Long,
    amplitudeBars: List<Float>,
    isPaused: Boolean,
    onPauseResume: () -> Unit,
    onStop: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Elapsed timer — headlineMedium per UI-SPEC typography contract
        Text(
            text = formatElapsedTime(elapsedSeconds),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        // Live waveform — last 100 bars from the end for scrolling effect
        val displayBars = if (amplitudeBars.size > 100) {
            amplitudeBars.subList(amplitudeBars.size - 100, amplitudeBars.size)
        } else {
            amplitudeBars
        }

        WaveformCanvas(
            bars = displayBars,
            progressFraction = 1f,
            activeColor = MaterialTheme.colorScheme.primary.copy(
                alpha = if (isPaused) 0.4f else 1f
            ),
            inactiveColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(vertical = 8.dp)
        )

        // Control row: Pause/Resume (72dp) + Stop button
        Row(
            modifier = Modifier.padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pause/Resume button with pulsing ring animation when RECORDING
            PauseResumeButton(
                isPaused = isPaused,
                onClick = onPauseResume
            )

            Spacer(Modifier.width(24.dp))

            // Stop button — errorContainer color per UI-SPEC
            FilledIconButton(
                onClick = onStop,
                modifier = Modifier.size(56.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop recording"
                )
            }
        }
    }
}

@Composable
private fun PauseResumeButton(
    isPaused: Boolean,
    onClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    // Pulsing ring — shown only when RECORDING (not paused), per UI-SPEC
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_ring")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.2f,
        animationSpec = InfiniteRepeatableSpec(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = InfiniteRepeatableSpec(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    // Ring behind button (only visible when recording)
    if (!isPaused) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            // Pulsing ring drawn behind button
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                        alpha = pulseAlpha
                    }
                    .drawBehind {
                        drawCircle(
                            color = primaryColor,
                            radius = size.minDimension / 2f
                        )
                    }
            )
            FilledIconButton(
                onClick = onClick,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = "Pause recording"
                )
            }
        }
    } else {
        FilledIconButton(
            onClick = onClick,
            modifier = Modifier.size(72.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Resume recording"
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TITLE state content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TitleStateContent(
    titleText: String,
    defaultTitle: String,
    onTitleChange: (String) -> Unit,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    bufferedTags: List<TagChipUiModel>,
    allTags: List<TagChipUiModel>,
    onAddBufferedTags: (List<String>) -> Unit,
    onRemoveBufferedTag: (String) -> Unit,
    onCreateAndBufferTag: (String) -> Unit,
    sortMode: TagSortMode,
    onSortModeChange: (TagSortMode) -> Unit
) {
    // D-01/D-02/D-03: canonical NameAndTagsEditor body, empty header (A3 — recording controls
    // live in the RECORDING/PAUSED states, not here). Discard is wired through the archetype's
    // onDismiss slot (per the canonical Save/Cancel button row, UI-SPEC Component Contract §2);
    // "Name your recording" heading text is dropped (D-10, matches the empty-header treatment
    // applied to album-rename/voice-rename's own mode headings).
    NameAndTagsEditor(
        name = titleText,
        onNameChange = { newValue ->
            // T-05-06: trim leading/trailing whitespace before storing
            onTitleChange(newValue)
        },
        nameLabel = "Recording title",
        tagsContent = {
            // Capture-time tag row (D-04, 49-03 ASSIGN-02) — the canonical PURE widget (no
            // `:app` TagChipEditor wrapper — no cardId exists yet), bare, no "Tags" label
            // (Locked Decision 1). isLastTag is always false here — no existing card can be
            // orphaned by a buffered removal. Sort control suppressed (D-07/G2-02).
            TagChipEditorContent(
                currentTags = bufferedTags,
                isLastTag = false,
                allTags = allTags,
                onRemoveTag = onRemoveBufferedTag,
                onAddTags = onAddBufferedTags,
                onRemoveTagNoUndo = onRemoveBufferedTag,
                onCreateTag = onCreateAndBufferTag,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                sortMode = sortMode,
                onSortModeChange = onSortModeChange,
                showSortControl = false
            )
        },
        onSave = onSave,
        onDismiss = onDiscard,
        saveLabel = "Save Recording",
        // G2/52-07 follow-up: voice-capture dismiss discards the recording — keep it a
        // first-class destructive affordance ("Discard" + error color), not a neutral "Cancel".
        dismissLabel = "Discard",
        dismissDestructive = true
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Elapsed time formatter — MM:SS or H:MM:SS for recordings >= 1 hour
// ─────────────────────────────────────────────────────────────────────────────

private fun formatElapsedTime(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", h, m, s)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", m, s)
    }
}
