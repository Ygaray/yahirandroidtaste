package io.github.ygaray.yahirandroidtaste.component

import android.content.Context
import android.graphics.drawable.GradientDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.github.ygaray.yahirandroidtaste.theme.Dimens
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

/** Map area height (Claude's discretion -- no UI-SPEC exists for this phase; see 164-02-PLAN.md). */
private val PLACE_MAP_HEIGHT: Dp = 240.dp

/** Diameter of the pin marker's drawn icon. */
private val PIN_DIAMETER: Dp = 28.dp

/** Stroke width of the pin marker's icon border. */
private val PIN_STROKE_WIDTH: Dp = 2.dp

/** Stroke width of the radius circle's outline. */
private val CIRCLE_OUTLINE_WIDTH: Dp = 2.dp

/** Border padding `zoomToBoundingBox` leaves around a fit circle so it never touches the map's edge. */
private val CAMERA_FIT_BORDER: Dp = 32.dp

/**
 * HUBW-02: the reusable, controlled place picker -- a caller-held pin (nullable latitude and
 * longitude) and an adjustable radius, both rendered on an OpenStreetMap [MapView] (D-01) plus an
 * accessible [Slider]. Value contract is primitives in and out only: `Double?`/`Float`/`String`
 * and lambdas -- no app type, no osmdroid type, ever appears in this public signature (D-03).
 *
 * Controlled like [DateTimePicker]: the caller owns [pinLatitude]/[pinLongitude]/[radiusMeters];
 * this widget only renders them and reports user intent through [onPinChange]/[onRadiusChange]. A
 * pin exists only when both [pinLatitude] and [pinLongitude] are non-null, finite and in range
 * ([pinKeyOrNull]) -- with no valid pin the map area shows the hint "Tap the map to drop a pin"
 * and no marker, circle, handle or radius row renders (`conditional-render-no-dead-space`).
 *
 * [minRadiusMeters], [maxRadiusMeters] and [defaultRadiusMeters] are required (D-04: the hub bakes
 * in no geofence policy); [defaultRadiusMeters] is what a non-finite [radiusMeters] sanitizes to.
 * [radiusStepMeters] defaults to `0f` (whole meters, no step). An invalid combination throws
 * [IllegalArgumentException] at composition (see [RadiusSpec]) rather than letting the `Slider`'s
 * discrete stops and the sanitizer disagree.
 *
 * The camera refits (fits the whole radius circle in view) whenever the pin changes, or whenever
 * the radius changes to a value the widget did *not* just emit itself from the handle or the
 * slider ([shouldRefitCamera]) -- so a saved place with a bigger radius at the same coordinate
 * still brings the whole circle into view, but dragging the handle or the slider never makes the
 * camera jump (163 review WR-01: external value changes must resync the open surface).
 *
 * A circle crossing the antimeridian is clamped, not wrapped, at `±180°` longitude, trading a
 * sliver of the circle past the dateline for a camera box osmdroid can always fit. A pin beyond
 * osmdroid's Web Mercator limit (`±85.05112877980658°`, [WEB_MERCATOR_MAX_LATITUDE]) is framed at
 * that limit's edge for camera purposes only -- [pinLatitude] itself, every callback, and the
 * radius are never rewritten (see [cameraLatitude], [radiusBounds]).
 *
 * This widget never renders a raw coordinate as text or a content description, and never logs one
 * (the app's own T-157-11 privacy invariant, mirrored here). Under [LocalInspectionMode] (previews
 * and Robolectric tests) the map area renders a same-size, same-tagged placeholder instead of
 * constructing a [MapView] (D-05); the `MapView` surface itself is device-only verified.
 *
 * [userAgent] should be the host app's application id (D-06); [configureOsmdroid] runs before any
 * `MapView` is constructed so every consumer is OSM tile-policy compliant by default, including
 * the host's own `INTERNET` permission requirement.
 *
 * **Parameter order contract:** a later plan inserts search/current-location/saved-place
 * parameters between [radiusStepMeters] and [testTag] -- this signature's relative order of the
 * parameters before [radiusStepMeters] does not change.
 *
 * @param pinLatitude The pin's latitude, or `null` when no pin is set.
 * @param pinLongitude The pin's longitude, or `null` when no pin is set.
 * @param onPinChange Invoked with a user-picked coordinate (a map tap, a pin/handle drag end, or
 *   the "Drop pin at map center" accessibility action), always through the most recently passed
 *   lambda.
 * @param radiusMeters The current radius in meters; sanitized through [RadiusSpec.sanitize]
 *   before it is ever displayed or emitted.
 * @param onRadiusChange Invoked with a sanitized radius from the slider or the map handle, always
 *   through the most recently passed lambda.
 * @param minRadiusMeters Inclusive lower bound for [radiusMeters].
 * @param maxRadiusMeters Inclusive upper bound for [radiusMeters].
 * @param defaultRadiusMeters What a non-finite [radiusMeters] sanitizes to; must lie within
 *   `[minRadiusMeters, maxRadiusMeters]`.
 * @param userAgent The identifying User-Agent osmdroid's tile requests carry (D-06) -- pass the
 *   host application's id.
 * @param modifier Applied to the outer [Column].
 * @param radiusStepMeters The radius grid step in meters, or `0f` for whole-meter granularity.
 * @param testTag Root testTag; the map area, hint, attribution and radius row derive
 *   `"${testTag}_map"` / `"${testTag}_map_placeholder"`, `"${testTag}_hint"`,
 *   `"${testTag}_attribution"`, `"${testTag}_radius"`.
 */
@Composable
fun PlaceMapPicker(
    pinLatitude: Double?,
    pinLongitude: Double?,
    onPinChange: (latitude: Double, longitude: Double) -> Unit,
    radiusMeters: Float,
    onRadiusChange: (radiusMeters: Float) -> Unit,
    minRadiusMeters: Float,
    maxRadiusMeters: Float,
    defaultRadiusMeters: Float,
    userAgent: String,
    modifier: Modifier = Modifier,
    radiusStepMeters: Float = 0f,
    testTag: String = "place_map_picker"
) {
    val spec = remember(minRadiusMeters, maxRadiusMeters, defaultRadiusMeters, radiusStepMeters) {
        RadiusSpec(minRadiusMeters, maxRadiusMeters, defaultRadiusMeters, radiusStepMeters)
    }
    val pin = pinKeyOrNull(pinLatitude, pinLongitude)
    val radius = spec.sanitize(radiusMeters)
    val currentOnPinChange by rememberUpdatedState(onPinChange)
    val currentOnRadiusChange by rememberUpdatedState(onRadiusChange)

    // Shared across the map area and the radius row (below) so a slider or handle emission can
    // record holder.lastEmittedRadiusMeters and suppress the camera-refit echo the map area's
    // sync pass would otherwise trigger for its own change.
    val holder = remember { PlaceMapPickerHolder() }
    holder.pin = pin
    holder.spec = spec
    holder.onPinChange = currentOnPinChange
    holder.onRadiusChange = currentOnRadiusChange

    Column(modifier = modifier.fillMaxWidth().testTag(testTag)) {
        PlaceMapArea(holder = holder, pin = pin, radius = radius, userAgent = userAgent, testTag = testTag)
        if (pin != null) {
            Spacer(modifier = Modifier.height(Dimens.CompactPadding))
            PlaceMapRadiusRow(
                holder = holder,
                spec = spec,
                radius = radius,
                testTag = testTag,
                onRadiusChange = currentOnRadiusChange
            )
        }
    }
}

@Composable
private fun PlaceMapArea(
    holder: PlaceMapPickerHolder,
    pin: PinKey?,
    radius: Float,
    userAgent: String,
    testTag: String
) {
    val inspectionMode = LocalInspectionMode.current
    val density = LocalDensity.current
    val colorScheme = MaterialTheme.colorScheme
    val colors = remember(colorScheme, density) { resolvePlaceMapOverlayColors(colorScheme, density) }
    val cameraFitBorderPx = remember(density) { with(density) { CAMERA_FIT_BORDER.roundToPx() } }

    val dropPinAtCenter: () -> Unit = {
        val mapView = holder.mapView
        val center = when {
            mapView != null -> mapView.mapCenter.let { it.latitude to it.longitude }
            pin != null -> pin.latitude to pin.longitude
            else -> 0.0 to 0.0
        }
        holder.onPinChange(center.first, center.second)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(PLACE_MAP_HEIGHT)
            .clip(RoundedCornerShape(Dimens.CornerRadius.Small))
    ) {
        if (inspectionMode) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("${testTag}_map_placeholder")
                    .semantics { placeMapSemantics(dropPinAtCenter) }
            )
        } else {
            AndroidView(
                factory = { context -> createPlaceMapView(context, userAgent, holder) },
                update = { mapView -> syncPlaceMapOverlays(mapView, holder, pin, radius, colors, cameraFitBorderPx) },
                onRelease = { mapView ->
                    mapView.onDetach()
                    holder.mapView = null
                },
                modifier = Modifier
                    .matchParentSize()
                    .testTag("${testTag}_map")
                    .semantics { placeMapSemantics(dropPinAtCenter) }
            )
        }

        if (pin == null) {
            PlaceMapHint(testTag)
        }
        PlaceMapAttribution(testTag)
    }
}

@Composable
private fun BoxScope.PlaceMapHint(testTag: String) {
    PlaceMapOverlayCaption(
        text = "Tap the map to drop a pin",
        style = MaterialTheme.typography.labelMedium,
        alignment = Alignment.TopCenter,
        testTag = "${testTag}_hint"
    )
}

@Composable
private fun BoxScope.PlaceMapAttribution(testTag: String) {
    PlaceMapOverlayCaption(
        text = "© OpenStreetMap contributors",
        style = MaterialTheme.typography.labelSmall,
        alignment = Alignment.BottomEnd,
        testTag = "${testTag}_attribution"
    )
}

@Composable
private fun BoxScope.PlaceMapOverlayCaption(text: String, style: TextStyle, alignment: Alignment, testTag: String) {
    Text(
        text = text,
        style = style,
        modifier = Modifier
            .align(alignment)
            .padding(4.dp)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag(testTag)
    )
}

@Composable
private fun PlaceMapRadiusRow(
    holder: PlaceMapPickerHolder,
    spec: RadiusSpec,
    radius: Float,
    testTag: String,
    onRadiusChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().testTag("${testTag}_radius")) {
        Text(text = "${formatMeters(radius)} radius", style = MaterialTheme.typography.labelLarge)
        Slider(
            value = radius,
            onValueChange = {
                val sanitized = spec.sanitize(it)
                holder.lastEmittedRadiusMeters = sanitized
                onRadiusChange(sanitized)
            },
            valueRange = spec.minMeters..spec.maxMeters,
            steps = spec.sliderSteps(),
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Radius" }
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = formatMeters(spec.minMeters),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatMeters(spec.maxMeters),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun SemanticsPropertyReceiver.placeMapSemantics(onDropPinAtCenter: () -> Unit) {
    contentDescription = "Map"
    customActions = listOf(
        CustomAccessibilityAction("Drop pin at map center") {
            onDropPinAtCenter()
            true
        }
    )
}

/**
 * Per-[PlaceMapPicker]-instance mutable state for the live osmdroid surface, `remember`ed once at
 * the top of [PlaceMapPicker] and shared by the map area and the radius row. Every field is
 * refreshed on each recomposition/update pass so a listener created once in [createPlaceMapView]
 * -- whose closures capture only this holder, never a composable-scope value directly -- always
 * reads the latest pin, [RadiusSpec] and callback (never a stale one).
 */
private class PlaceMapPickerHolder {
    var mapView: MapView? = null
    var circle: Polygon? = null
    var pinMarker: Marker? = null
    var lastFittedCamera: CameraKey? = null
    var lastEmittedRadiusMeters: Float? = null
    var pin: PinKey? = null
    var spec: RadiusSpec? = null
    var onPinChange: (Double, Double) -> Unit = { _, _ -> }
    var onRadiusChange: (Float) -> Unit = {}
}

private data class PlaceMapOverlayColors(
    val circleFillColor: Int,
    val circleOutlineColor: Int,
    val circleOutlineWidthPx: Float,
    val pinFillColor: Int,
    val pinBorderColor: Int,
    val pinStrokeWidthPx: Int,
    val pinDiameterPx: Int
)

private fun resolvePlaceMapOverlayColors(colorScheme: ColorScheme, density: Density): PlaceMapOverlayColors =
    with(density) {
        PlaceMapOverlayColors(
            circleFillColor = colorScheme.primary.copy(alpha = 0.2f).toArgb(),
            circleOutlineColor = colorScheme.primary.toArgb(),
            circleOutlineWidthPx = CIRCLE_OUTLINE_WIDTH.toPx(),
            pinFillColor = colorScheme.primary.toArgb(),
            pinBorderColor = colorScheme.surface.toArgb(),
            pinStrokeWidthPx = PIN_STROKE_WIDTH.roundToPx(),
            pinDiameterPx = PIN_DIAMETER.roundToPx()
        )
    }

private fun buildPinIcon(colors: PlaceMapOverlayColors): GradientDrawable {
    val drawable = GradientDrawable()
    drawable.shape = GradientDrawable.OVAL
    drawable.setColor(colors.pinFillColor)
    drawable.setStroke(colors.pinStrokeWidthPx, colors.pinBorderColor)
    drawable.setSize(colors.pinDiameterPx, colors.pinDiameterPx)
    drawable.setBounds(0, 0, colors.pinDiameterPx, colors.pinDiameterPx)
    return drawable
}

/**
 * Builds the live osmdroid [MapView] once per [PlaceMapPicker] instance. [configureOsmdroid]
 * (D-06) runs first, before any `MapView` is constructed, so this hub is OSM tile-policy
 * compliant by default. The single [MapEventsOverlay] added here is the map's tap-to-drop-pin
 * catch-all: it always reads the *current* [PlaceMapPickerHolder.onPinChange] through [holder],
 * never one captured at this factory's single invocation.
 */
private fun createPlaceMapView(context: Context, userAgent: String, holder: PlaceMapPickerHolder): MapView {
    configureOsmdroid(context, userAgent)
    val mapView = MapView(context)
    mapView.setTileSource(TileSourceFactory.MAPNIK)
    mapView.setMultiTouchControls(true)
    mapView.setTilesScaledToDpi(true)
    mapView.setMinZoomLevel(PLACE_MAP_WORLD_ZOOM)
    mapView.setMaxZoomLevel(PLACE_MAP_MAX_ZOOM)
    mapView.controller.setZoom(PLACE_MAP_WORLD_ZOOM)
    mapView.overlays.add(
        MapEventsOverlay(
            object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                    holder.onPinChange(p.latitude, p.longitude)
                    return true
                }

                override fun longPressHelper(p: GeoPoint): Boolean = false
            }
        )
    )
    holder.mapView = mapView
    return mapView
}

/**
 * The `AndroidView` `update` pass: with no [pin], clears every pin-owned overlay
 * ([clearPinOverlays]); with a pin, lazily creates the circle and the pin marker, re-applies their
 * theme colors on every pass (so a theme change while the picker is open recolors the overlays),
 * and refits the camera through [shouldRefitCamera] when the pin or an externally-supplied radius
 * changed.
 */
private fun syncPlaceMapOverlays(
    mapView: MapView,
    holder: PlaceMapPickerHolder,
    pin: PinKey?,
    radius: Float,
    colors: PlaceMapOverlayColors,
    cameraFitBorderPx: Int
) {
    if (pin == null) {
        clearPinOverlays(holder)
        mapView.invalidate()
        return
    }

    val center = GeoPoint(pin.latitude, pin.longitude)

    val circle = holder.circle ?: Polygon(mapView).also {
        mapView.overlays.add(it)
        holder.circle = it
    }
    circle.fillPaint.color = colors.circleFillColor
    circle.outlinePaint.color = colors.circleOutlineColor
    circle.outlinePaint.strokeWidth = colors.circleOutlineWidthPx
    circle.setPoints(Polygon.pointsAsCircle(center, radius.toDouble()))

    val marker = holder.pinMarker ?: Marker(mapView).also {
        it.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        it.setInfoWindow(null)
        mapView.overlays.add(it)
        holder.pinMarker = it
    }
    marker.position = center
    marker.icon = buildPinIcon(colors)

    if (shouldRefitCamera(holder.lastFittedCamera, CameraKey(pin, radius), holder.lastEmittedRadiusMeters)) {
        fitPlaceMapCamera(mapView, holder, pin, radius, cameraFitBorderPx)
    }

    mapView.invalidate()
}

private fun fitPlaceMapCamera(
    mapView: MapView,
    holder: PlaceMapPickerHolder,
    pin: PinKey,
    radius: Float,
    borderPx: Int
) {
    val bounds = radiusBounds(pin.latitude, pin.longitude, radius)
    val boundingBox = BoundingBox(bounds.north, bounds.east, bounds.south, bounds.west)
    val animated = holder.lastFittedCamera != null
    if (mapView.width == 0) {
        mapView.addOnFirstLayoutListener { view, _, _, _, _ ->
            (view as MapView).zoomToBoundingBox(boundingBox, animated, borderPx)
        }
    } else {
        mapView.zoomToBoundingBox(boundingBox, animated, borderPx)
    }
    holder.lastFittedCamera = CameraKey(pin, radius)
    holder.lastEmittedRadiusMeters = null
}

/**
 * Removes every pin-owned overlay (the circle and the pin marker) from [holder]'s `MapView` and
 * clears each holder reference, so no stale overlay remains when the caller's pin becomes invalid
 * (e.g. the place is cleared).
 */
private fun clearPinOverlays(holder: PlaceMapPickerHolder) {
    val mapView = holder.mapView
    holder.circle?.let { mapView?.overlays?.remove(it) }
    holder.pinMarker?.let { mapView?.overlays?.remove(it) }
    holder.circle = null
    holder.pinMarker = null
    holder.lastFittedCamera = null
}
