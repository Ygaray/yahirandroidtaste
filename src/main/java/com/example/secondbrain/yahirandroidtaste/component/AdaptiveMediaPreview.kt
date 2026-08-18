package com.example.secondbrain.yahirandroidtaste.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade

/**
 * Data class representing a single thumbnail cell in [AdaptiveMediaPreview].
 *
 * @param source File or Uri — Coil resolves directly.
 * @param memoryCacheKey Cache-busting key; update after image edits.
 * @param isVideo Whether this cell represents a video (not used in album images, future-proof).
 * @param durationMs Duration in ms (for video cells only).
 */
data class MediaThumbnailCell(
    val source: Any,
    val memoryCacheKey: String,
    val isVideo: Boolean = false,
    val durationMs: Long = 0L
)

/**
 * Adaptive thumbnail grid for album card faces.
 *
 * Renders 0, 1, 2, 3, or 4+ images in a consistent layout:
 * - 0: surfaceVariant placeholder box
 * - 1: Single full-size thumbnail
 * - 2: Two equal-weight columns
 * - 3: Three equal-weight columns
 * - 4+: 2x2 grid — cells 0 + 1 top row; cell 2 + "+N" overflow bottom row
 *
 * Disk cache is ENABLED and keyed by the versioned [MediaThumbnailCell.memoryCacheKey]
 * (format: "${card.id}_${i}_${updatedAt}") so Coil reads from disk on scroll (no re-decode
 * jank) and misses the cache after a photo edit when updatedAt changes — busting both disk
 * and memory caches together (D-06).
 *
 * A numeric count badge is overlaid top-right when [totalImageCount] > 1, showing the real
 * image count on a semi-opaque scrim (D-11).
 *
 * @param cells List of thumbnail cells to render (capped at 4 by callers for the 4+ branch).
 * @param totalImageCount Real total number of images in the album; used to compute the "+N"
 *   overflow label and the count badge. Callers that cap [cells] must pass the uncapped count
 *   here. Defaults to [cells.size] so existing call sites with no overflow are unaffected.
 * @param onCellTap Called with the index of the tapped cell.
 * @param onOverflowTap Called when the "+N" overflow box is tapped.
 * @param modifier Modifier applied to the outermost container.
 */
@Composable
fun AdaptiveMediaPreview(
    cells: List<MediaThumbnailCell>,
    onCellTap: (index: Int) -> Unit,
    onOverflowTap: () -> Unit,
    modifier: Modifier = Modifier,
    totalImageCount: Int = cells.size
) {
    val context = LocalContext.current

    // Outer Box owns the modifier and provides the stacking context for the count badge overlay.
    Box(modifier = modifier) {
        when (cells.size) {
            0 -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }

            1 -> {
                ThumbnailCell(
                    cell = cells[0],
                    context = context,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onCellTap(0) }
                )
            }

            2 -> {
                Row(modifier = Modifier.fillMaxSize()) {
                    cells.forEachIndexed { index, cell ->
                        ThumbnailCell(
                            cell = cell,
                            context = context,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { onCellTap(index) }
                        )
                    }
                }
            }

            3 -> {
                Row(modifier = Modifier.fillMaxSize()) {
                    cells.forEachIndexed { index, cell ->
                        ThumbnailCell(
                            cell = cell,
                            context = context,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { onCellTap(index) }
                        )
                    }
                }
            }

            else -> {
                // 4+ items: 2x2 grid
                Column(modifier = Modifier.fillMaxSize()) {
                    // Top row: cells[0] and cells[1]
                    Row(modifier = Modifier.weight(1f)) {
                        ThumbnailCell(
                            cell = cells[0],
                            context = context,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { onCellTap(0) }
                        )
                        ThumbnailCell(
                            cell = cells[1],
                            context = context,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { onCellTap(1) }
                        )
                    }
                    // Bottom row: cells[2] + "+N" overflow overlay
                    Row(modifier = Modifier.weight(1f)) {
                        ThumbnailCell(
                            cell = cells[2],
                            context = context,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { onCellTap(2) }
                        )
                        // "+N" overflow cell — shows how many images are not displayed
                        // Uses totalImageCount (real total) not cells.size (capped at 4)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(Color.Black.copy(alpha = 0.5f))
                                .clickable { onOverflowTap() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+${(totalImageCount - 3).coerceAtLeast(1)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Count badge: shows the total image count when the album has more than one photo (D-11).
        // Display-only (no click). Sits above the thumbnail layout in the top-right corner.
        // Coexists with the "+N" overflow label in the 4th cell for the 4+ branch.
        if (totalImageCount > 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                Text(
                    text = "$totalImageCount",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier
                        .background(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// ---- ThumbnailCell ----------------------------------------------------------

/**
 * A single thumbnail cell within [AdaptiveMediaPreview].
 *
 * Renders a Coil AsyncImage with Crop scale, disk cache enabled and keyed by the versioned
 * per-image [MediaThumbnailCell.memoryCacheKey] to prevent re-decode jank on scroll and
 * serve a fresh image after a photo edit (D-06).
 */
@Composable
private fun ThumbnailCell(
    cell: MediaThumbnailCell,
    context: android.content.Context,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(cell.source)
                .crossfade(true)
                .diskCachePolicy(CachePolicy.ENABLED)
                .diskCacheKey(cell.memoryCacheKey)
                .memoryCacheKey(cell.memoryCacheKey)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
