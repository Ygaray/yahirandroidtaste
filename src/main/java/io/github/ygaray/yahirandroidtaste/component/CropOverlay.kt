package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.gestures.detectDragGestures

/**
 * CropOverlay — inline Compose crop overlay with 8 draggable handles.
 *
 * Renders over the image inside the Crop tab. Computes the displayed image rect
 * using ContentScale.Fit logic from bitmapWidth/bitmapHeight. All crop coordinates
 * are tracked in screen/display pixels and normalized to 0..1 fractions for output.
 *
 * Handles: 4 corners (TL, TR, BL, BR) + 4 midpoints (TC, RC, BC, LC).
 * Each handle updates the corresponding edge(s) of the crop rect on drag.
 * Minimum crop size is 50.dp to prevent degenerate crops.
 *
 * The Apply Crop button lives in AlbumImageEditorScreen's bottom palette (not here).
 *
 * Phase 86 (GADGET-03): relocated verbatim from `:app`'s
 * `feature/album/components/CropOverlay.kt` into `:yahirandroidtaste/component/` — a
 * pure-presentational move, no logic change (D-02). Every import was already
 * `androidx.compose.*`, so only the package declaration changes.
 *
 * @param bitmapWidth         Original bitmap width in pixels
 * @param bitmapHeight        Original bitmap height in pixels
 * @param aspectRatio         null = free crop, 1f = 1:1, 4f/3f = 4:3, 16f/9f = 16:9
 * @param initialCropBounds   When non-null, restores a previous crop selection (normalized [0..1]) on
 *                            (re-)entry instead of emitting full-image [0,0,1,1] bounds. Prevents the
 *                            tab-switch reset bug (Pitfall 3): CropOverlay leaves composition when the
 *                            user switches to the Draw/Text tab; on re-entry, [initialized] resets and
 *                            [onSizeChanged] would silently emit [0,0,1,1]. With this param the Screen
 *                            passes its persisted [currentCropBounds] so handles restore to the correct
 *                            position and no spurious full-image emission overwrites the selection.
 * @param onCropBoundsChanged Called with normalized (0..1) left, top, right, bottom on every drag
 * @param modifier            Applied to the root Box
 */
@Composable
fun CropOverlay(
    bitmapWidth: Int,
    bitmapHeight: Int,
    aspectRatio: Float?,
    onCropBoundsChanged: (left: Float, top: Float, right: Float, bottom: Float) -> Unit,
    modifier: Modifier = Modifier,
    initialCropBounds: List<Float>? = null
) {
    val density = LocalDensity.current
    val minCropDp = 50.dp

    var containerWidth by remember { mutableIntStateOf(0) }
    var containerHeight by remember { mutableIntStateOf(0) }

    // Crop rect in screen-space pixels (relative to the container / composable)
    // Initialized lazily once containerWidth/Height are known.
    var cropLeft by remember { mutableFloatStateOf(0f) }
    var cropTop by remember { mutableFloatStateOf(0f) }
    var cropRight by remember { mutableFloatStateOf(0f) }
    var cropBottom by remember { mutableFloatStateOf(0f) }

    // Track whether we have done initial init
    var initialized by remember { mutableFloatStateOf(0f) }

    // Set to true in onSizeChanged when bounds are restored from initialCropBounds (tab re-entry).
    // The LaunchedEffect checks this flag to skip the "reset to full image" step that would
    // otherwise overwrite the restored selection on the first containerWidth/Height change.
    var restoredFromBounds by remember { mutableStateOf(false) }

    // When aspectRatio changes, reset crop rect to full image and re-apply ratio.
    // Guard: skip reset when the current re-measure was caused by tab re-entry and bounds were
    // already restored from initialCropBounds in onSizeChanged (restoredFromBounds == true).
    LaunchedEffect(aspectRatio, containerWidth, containerHeight) {
        if (bitmapWidth > 0 && bitmapHeight > 0 && containerWidth > 0 && containerHeight > 0 && initialized > 0f) {
            if (restoredFromBounds) {
                // Container re-measured because CropOverlay just re-entered composition after a
                // tab switch. Bounds were already restored in onSizeChanged — do NOT reset to
                // full image. Clear the flag so future genuine aspect-ratio or size changes run.
                restoredFromBounds = false
                return@LaunchedEffect
            }
            // Reset to full image + re-apply aspect ratio (extracted — see computeResetCropRect).
            val geometry = computeDisplayGeometry(bitmapWidth, bitmapHeight, containerWidth, containerHeight)
            val rect = computeResetCropRect(geometry, aspectRatio)
            cropLeft = rect.left
            cropTop = rect.top
            cropRight = rect.right
            cropBottom = rect.bottom

            // Emit initial/reset bounds
            val normalized = normalizeCropRect(rect, geometry)
            onCropBoundsChanged(normalized.left, normalized.top, normalized.right, normalized.bottom)
        }
    }

    Box(
        modifier = modifier
            .onSizeChanged { size ->
                containerWidth = size.width
                containerHeight = size.height
                // Initialize or restore crop rect on first measure (including tab re-entry).
                if (bitmapWidth > 0 && bitmapHeight > 0 && size.width > 0 && size.height > 0) {
                    val geometry = computeDisplayGeometry(bitmapWidth, bitmapHeight, size.width, size.height)
                    // Restore previous selection (tab re-entry) or initialize to full image —
                    // extracted, see computeInitialCropRect.
                    val result = computeInitialCropRect(geometry, initialCropBounds)
                    cropLeft = result.rect.left
                    cropTop = result.rect.top
                    cropRight = result.rect.right
                    cropBottom = result.rect.bottom
                    initialized = 1f
                    restoredFromBounds = result.restoredFromBounds
                    if (!result.restoredFromBounds) {
                        // First entry with no prior selection — full image, emit.
                        onCropBoundsChanged(0f, 0f, 1f, 1f)
                    }
                    // else: Screen already holds the correct bounds in currentCropBounds — no emission.
                }
            }
    ) {
        // Only render overlay content once container is measured
        if (containerWidth > 0 && containerHeight > 0 && initialized > 0f) {

            val minCropPx = with(density) { minCropDp.toPx() }
            val handleSize = with(density) { 12.dp.toPx() }

            // Compute displayed image bounds for clamping
            val geometry = computeDisplayGeometry(bitmapWidth, bitmapHeight, containerWidth, containerHeight)
            val imgLeft = geometry.offsetX
            val imgTop = geometry.offsetY
            val imgRight = imgLeft + geometry.displayedWidth
            val imgBottom = imgTop + geometry.displayedHeight

            // ── Scrim + frame canvas ─────────────────────────────────────────
            Canvas(modifier = Modifier.fillMaxSize()) {
                val scrimColor = Color.Black.copy(alpha = 0.5f)
                val frameColor = Color.White
                val gridColor = Color.White.copy(alpha = 0.3f)
                val strokeWidth2dp = 2.dp.toPx()
                val strokeWidth1dp = 1.dp.toPx()

                val cl = cropLeft
                val ct = cropTop
                val cr = cropRight
                val cb = cropBottom

                // Four scrim regions outside crop rect
                // Top strip
                drawRect(
                    color = scrimColor,
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width, ct)
                )
                // Bottom strip
                drawRect(
                    color = scrimColor,
                    topLeft = Offset(0f, cb),
                    size = Size(size.width, size.height - cb)
                )
                // Left strip (between top and bottom strips)
                drawRect(
                    color = scrimColor,
                    topLeft = Offset(0f, ct),
                    size = Size(cl, cb - ct)
                )
                // Right strip (between top and bottom strips)
                drawRect(
                    color = scrimColor,
                    topLeft = Offset(cr, ct),
                    size = Size(size.width - cr, cb - ct)
                )

                // Crop frame border
                drawRect(
                    color = frameColor,
                    topLeft = Offset(cl, ct),
                    size = Size(cr - cl, cb - ct),
                    style = Stroke(width = strokeWidth2dp)
                )

                // Rule-of-thirds grid lines inside crop area
                val thirdW = (cr - cl) / 3f
                val thirdH = (cb - ct) / 3f
                // Vertical thirds
                drawLine(
                    color = gridColor,
                    start = Offset(cl + thirdW, ct),
                    end = Offset(cl + thirdW, cb),
                    strokeWidth = strokeWidth1dp
                )
                drawLine(
                    color = gridColor,
                    start = Offset(cl + 2 * thirdW, ct),
                    end = Offset(cl + 2 * thirdW, cb),
                    strokeWidth = strokeWidth1dp
                )
                // Horizontal thirds
                drawLine(
                    color = gridColor,
                    start = Offset(cl, ct + thirdH),
                    end = Offset(cr, ct + thirdH),
                    strokeWidth = strokeWidth1dp
                )
                drawLine(
                    color = gridColor,
                    start = Offset(cl, ct + 2 * thirdH),
                    end = Offset(cr, ct + 2 * thirdH),
                    strokeWidth = strokeWidth1dp
                )
            }

            // ── 8 Draggable handles ──────────────────────────────────────────

            // Helper to place a handle at a screen position
            @Composable
            fun BoxScope.Handle(
                screenX: Float,
                screenY: Float,
                onDrag: (dragX: Float, dragY: Float) -> Unit
            ) {
                val handleDp = 12.dp
                val handleOffsetX = with(density) { (screenX - handleSize / 2f).toDp() }
                val handleOffsetY = with(density) { (screenY - handleSize / 2f).toDp() }

                Canvas(
                    modifier = Modifier
                        .size(handleDp)
                        .offset(handleOffsetX, handleOffsetY)
                        .pointerInput(Unit) {
                            detectDragGestures { _, dragAmount ->
                                onDrag(dragAmount.x, dragAmount.y)
                            }
                        }
                ) {
                    // White fill
                    drawCircle(
                        color = Color.White,
                        radius = size.minDimension / 2f
                    )
                    // Dark outline
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.6f),
                        radius = size.minDimension / 2f,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }

            // Clamp helper — also enforces aspect ratio and emits normalized bounds.
            // The actual clamp/aspect-ratio math is extracted to clampCropRect() (top-level, pure)
            // so it is counted toward its own function's complexity, not CropOverlay's.
            fun clampCrop(
                newLeft: Float, newTop: Float, newRight: Float, newBottom: Float
            ) {
                val clamped = clampCropRect(
                    newLeft, newTop, newRight, newBottom,
                    imgLeft, imgTop, imgRight, imgBottom,
                    minCropPx, aspectRatio
                )
                cropLeft = clamped.left
                cropTop = clamped.top
                cropRight = clamped.right
                cropBottom = clamped.bottom

                // Emit normalized bounds
                val normalized = normalizeCropRect(clamped, geometry)
                onCropBoundsChanged(normalized.left, normalized.top, normalized.right, normalized.bottom)
            }

            val cl = cropLeft
            val ct = cropTop
            val cr = cropRight
            val cb = cropBottom
            val midX = (cl + cr) / 2f
            val midY = (ct + cb) / 2f

            // Corner: Top-Left
            Handle(cl, ct) { dx, dy ->
                clampCrop(cropLeft + dx, cropTop + dy, cropRight, cropBottom)
            }
            // Corner: Top-Right
            Handle(cr, ct) { dx, dy ->
                clampCrop(cropLeft, cropTop + dy, cropRight + dx, cropBottom)
            }
            // Corner: Bottom-Left
            Handle(cl, cb) { dx, dy ->
                clampCrop(cropLeft + dx, cropTop, cropRight, cropBottom + dy)
            }
            // Corner: Bottom-Right
            Handle(cr, cb) { dx, dy ->
                clampCrop(cropLeft, cropTop, cropRight + dx, cropBottom + dy)
            }
            // Midpoint: Top-Center
            Handle(midX, ct) { _, dy ->
                clampCrop(cropLeft, cropTop + dy, cropRight, cropBottom)
            }
            // Midpoint: Bottom-Center
            Handle(midX, cb) { _, dy ->
                clampCrop(cropLeft, cropTop, cropRight, cropBottom + dy)
            }
            // Midpoint: Left-Center
            Handle(cl, midY) { dx, _ ->
                clampCrop(cropLeft + dx, cropTop, cropRight, cropBottom)
            }
            // Midpoint: Right-Center
            Handle(cr, midY) { dx, _ ->
                clampCrop(cropLeft, cropTop, cropRight + dx, cropBottom)
            }
        }
    }
}

// ── Pure geometry/clamp helpers ──────────────────────────────────────────────
//
// Extracted from CropOverlay's body (DETEKT-02 pre-req refactor, 97-03) so their branching is
// counted toward these top-level private functions' own cyclomatic complexity instead of
// CropOverlay's — behavior-preserving, same formulas as the original inline code verbatim.

/** Screen-space rect for the crop selection (or any of its intermediate computations). */
private data class CropRect(val left: Float, val top: Float, val right: Float, val bottom: Float)

/** ContentScale.Fit-equivalent placement of the bitmap within its container. */
private data class DisplayGeometry(
    val offsetX: Float,
    val offsetY: Float,
    val displayedWidth: Float,
    val displayedHeight: Float
)

/** Normalized (0..1) crop bounds ready for [CropOverlay]'s onCropBoundsChanged callback. */
private data class NormalizedBounds(val left: Float, val top: Float, val right: Float, val bottom: Float)

/** Result of restoring or initializing the crop rect in onSizeChanged. */
private data class InitialCropResult(val rect: CropRect, val restoredFromBounds: Boolean)

/** Computes the ContentScale.Fit placement of the bitmap within a [containerWidth]x[containerHeight] box. */
private fun computeDisplayGeometry(
    bitmapWidth: Int,
    bitmapHeight: Int,
    containerWidth: Int,
    containerHeight: Int
): DisplayGeometry {
    val scale = minOf(
        containerWidth.toFloat() / bitmapWidth,
        containerHeight.toFloat() / bitmapHeight
    )
    val displayedW = bitmapWidth * scale
    val displayedH = bitmapHeight * scale
    val offX = (containerWidth - displayedW) / 2f
    val offY = (containerHeight - displayedH) / 2f
    return DisplayGeometry(offX, offY, displayedW, displayedH)
}

/** Converts a screen-space [rect] to normalized (0..1) bounds within [geometry]. */
private fun normalizeCropRect(rect: CropRect, geometry: DisplayGeometry): NormalizedBounds {
    return NormalizedBounds(
        left = ((rect.left - geometry.offsetX) / geometry.displayedWidth).coerceIn(0f, 1f),
        top = ((rect.top - geometry.offsetY) / geometry.displayedHeight).coerceIn(0f, 1f),
        right = ((rect.right - geometry.offsetX) / geometry.displayedWidth).coerceIn(0f, 1f),
        bottom = ((rect.bottom - geometry.offsetY) / geometry.displayedHeight).coerceIn(0f, 1f)
    )
}

/**
 * Resets the crop rect to the full displayed image within [geometry] and re-applies
 * [aspectRatio] (centered), matching the original LaunchedEffect reset logic verbatim.
 */
private fun computeResetCropRect(geometry: DisplayGeometry, aspectRatio: Float?): CropRect {
    var left = geometry.offsetX
    var top = geometry.offsetY
    var right = geometry.offsetX + geometry.displayedWidth
    var bottom = geometry.offsetY + geometry.displayedHeight

    if (aspectRatio != null && aspectRatio > 0f) {
        val w = right - left
        val h = bottom - top
        val desiredH = w / aspectRatio
        if (desiredH <= geometry.displayedHeight) {
            // Center the constrained height
            val centerY = (top + bottom) / 2f
            top = (centerY - desiredH / 2f).coerceAtLeast(geometry.offsetY)
            bottom = (top + desiredH).coerceAtMost(geometry.offsetY + geometry.displayedHeight)
        } else {
            val desiredW = h * aspectRatio
            val centerX = (left + right) / 2f
            left = (centerX - desiredW / 2f).coerceAtLeast(geometry.offsetX)
            right = (left + desiredW).coerceAtMost(geometry.offsetX + geometry.displayedWidth)
        }
    }
    return CropRect(left, top, right, bottom)
}

/**
 * Restores the crop rect from [initialCropBounds] (tab re-entry) if present and valid, else
 * initializes to the full displayed image — matching the original onSizeChanged logic verbatim.
 */
private fun computeInitialCropRect(geometry: DisplayGeometry, initialCropBounds: List<Float>?): InitialCropResult {
    val ib = initialCropBounds
    return if (ib != null && ib.size == 4) {
        InitialCropResult(
            rect = CropRect(
                left = geometry.offsetX + ib[0] * geometry.displayedWidth,
                top = geometry.offsetY + ib[1] * geometry.displayedHeight,
                right = geometry.offsetX + ib[2] * geometry.displayedWidth,
                bottom = geometry.offsetY + ib[3] * geometry.displayedHeight
            ),
            restoredFromBounds = true
        )
    } else {
        InitialCropResult(
            rect = CropRect(
                left = geometry.offsetX,
                top = geometry.offsetY,
                right = geometry.offsetX + geometry.displayedWidth,
                bottom = geometry.offsetY + geometry.displayedHeight
            ),
            restoredFromBounds = false
        )
    }
}

/**
 * Clamps a proposed crop rect to the image bounds, enforces the minimum crop size, and
 * re-applies [aspectRatio] if set — matching the original local clampCrop() math verbatim.
 */
private fun clampCropRect(
    newLeft: Float,
    newTop: Float,
    newRight: Float,
    newBottom: Float,
    imgLeft: Float,
    imgTop: Float,
    imgRight: Float,
    imgBottom: Float,
    minCropPx: Float,
    aspectRatio: Float?
): CropRect {
    var left = newLeft.coerceIn(imgLeft, imgRight - minCropPx)
    var top = newTop.coerceIn(imgTop, imgBottom - minCropPx)
    var right = newRight.coerceIn(imgLeft + minCropPx, imgRight)
    var bottom = newBottom.coerceIn(imgTop + minCropPx, imgBottom)
    // Ensure minimum size is maintained after clamping
    if (right - left < minCropPx) right = left + minCropPx
    if (bottom - top < minCropPx) bottom = top + minCropPx

    // Enforce aspect ratio if set
    if (aspectRatio != null && aspectRatio > 0f) {
        val currentW = right - left
        val currentH = bottom - top
        val desiredH = currentW / aspectRatio
        if (desiredH <= (imgBottom - top)) {
            bottom = (top + desiredH).coerceIn(imgTop + minCropPx, imgBottom)
        } else {
            val desiredW = currentH * aspectRatio
            right = (left + desiredW).coerceIn(imgLeft + minCropPx, imgRight)
        }
    }

    return CropRect(left, top, right, bottom)
}
