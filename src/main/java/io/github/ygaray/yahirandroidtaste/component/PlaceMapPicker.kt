package io.github.ygaray.yahirandroidtaste.component

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.MotionEvent
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
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

/** Diameter of the radius handle's drawn icon. */
private val HANDLE_DIAMETER: Dp = 24.dp

/** Stroke width of the radius handle's icon border. */
private val HANDLE_STROKE_WIDTH: Dp = 2.dp

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
 * **Parameter order contract:** a later plan inserts current-location/saved-place parameters
 * between [radiusStepMeters] and [testTag] -- this signature's relative order of the parameters
 * before [radiusStepMeters] does not change.
 *
 * [onSearch] is the address-search source (D-05): the caller resolves [searchQuery] itself (the
 * hub never geocodes) and reports the pin through [onPinChange] on success. A lookup fires on at
 * most one explicit commit -- the field's IME Search action -- never on typing and never while
 * [isResolving] is `true`; the delivered query is [searchQuery] trimmed of surrounding
 * whitespace. `onSearch = null` hides the search field entirely.
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
 * @param onSearch Invoked with the trimmed, non-blank [searchQuery] on an explicit IME Search
 *   action, only when [isResolving] is `false`. Hides the search field when `null`. The hub never
 *   geocodes; the caller resolves the query and calls [onPinChange] itself.
 * @param searchQuery The search field's current text; the hub renders it but never geocodes it.
 * @param onSearchQueryChange Invoked on every keystroke in the search field with the raw
 *   (untrimmed) text; never triggers a lookup by itself.
 * @param searchErrorText Caller-supplied error text rendered under the search field, or `null`
 *   to render none. The hub holds no copy of its own about why a lookup failed.
 * @param isResolving `true` while a caller-owned lookup (search or current location) is in
 *   flight. Disables only the controls that start a new lookup (the search field's IME Search
 *   action and, when this plan lands, "Use current location"); saved places and map placement
 *   stay interactive so a slow geocoder never blocks the user. The consumer must treat every
 *   [onSearch]/[onPinChange] (and, later, current-location/saved-place) call as a new source and
 *   discard an in-flight lookup result from an older one.
 * @param testTag Root testTag; the map area, hint, attribution, radius row and search field
 *   derive `"${testTag}_map"` / `"${testTag}_map_placeholder"`, `"${testTag}_hint"`,
 *   `"${testTag}_attribution"`, `"${testTag}_radius"`, `"${testTag}_search"`.
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
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    onSearch: ((query: String) -> Unit)? = null,
    searchErrorText: String? = null,
    isResolving: Boolean = false,
    testTag: String = "place_map_picker"
) {
    val spec = remember(minRadiusMeters, maxRadiusMeters, defaultRadiusMeters, radiusStepMeters) {
        RadiusSpec(minRadiusMeters, maxRadiusMeters, defaultRadiusMeters, radiusStepMeters)
    }
    val pin = pinKeyOrNull(pinLatitude, pinLongitude)
    val radius = spec.sanitize(radiusMeters)
    val currentOnPinChange by rememberUpdatedState(onPinChange)
    val currentOnRadiusChange by rememberUpdatedState(onRadiusChange)
    val currentOnSearchQueryChange by rememberUpdatedState(onSearchQueryChange)
    val currentOnSearch by rememberUpdatedState(onSearch)

    // Shared across the map area and the radius row (below) so a slider or handle emission can
    // record holder.lastEmittedRadiusMeters and suppress the camera-refit echo the map area's
    // sync pass would otherwise trigger for its own change.
    val holder = remember { PlaceMapPickerHolder() }
    holder.pin = pin
    holder.spec = spec
    holder.onPinChange = currentOnPinChange
    holder.onRadiusChange = currentOnRadiusChange

    Column(modifier = modifier.fillMaxWidth().testTag(testTag)) {
        if (currentOnSearch != null) {
            PlaceMapSearchSection(
                searchQuery = searchQuery,
                onSearchQueryChange = currentOnSearchQueryChange,
                onSearch = currentOnSearch,
                searchErrorText = searchErrorText,
                isResolving = isResolving,
                testTag = testTag
            )
            Spacer(modifier = Modifier.height(Dimens.CompactPadding))
        }
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

/**
 * The address-search source (D-05): a [ClearableTextField] whose `onValueChange` only forwards
 * [onSearchQueryChange] (never a lookup) and whose IME Search action calls [onSearch] with the
 * trimmed query, gated through [canSubmitSearch] so a blank query or a lookup already in flight
 * never fires. [searchErrorText] renders directly under the field only when non-null; the hub
 * never invents its own error copy.
 */
@Composable
private fun PlaceMapSearchSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: ((query: String) -> Unit)?,
    searchErrorText: String?,
    isResolving: Boolean,
    testTag: String
) {
    ClearableTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        label = { Text("Search for an address") },
        singleLine = true,
        isError = searchErrorText != null,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                if (canSubmitSearch(searchQuery, isResolving)) {
                    onSearch?.invoke(searchQuery.trim())
                }
            }
        ),
        modifier = Modifier.fillMaxWidth().testTag("${testTag}_search")
    )
    if (searchErrorText != null) {
        Text(
            text = searchErrorText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
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
    val lifecycleOwner = LocalLifecycleOwner.current
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
                factory = { context -> createPlaceMapView(context, userAgent, holder, lifecycleOwner) },
                update = { mapView -> syncPlaceMapOverlays(mapView, holder, pin, radius, colors, cameraFitBorderPx) },
                onRelease = { mapView ->
                    holder.lifecycleGate.pause { mapView.onPause() }
                    mapView.onDetach()
                    holder.mapView = null
                },
                modifier = Modifier
                    .matchParentSize()
                    .testTag("${testTag}_map")
                    .semantics { placeMapSemantics(dropPinAtCenter) }
            )

            // Balanced lifecycle binding (164-02 review round 2 MEDIUM): addObserver replays
            // ON_CREATE/ON_START/ON_RESUME to catch this observer up to the host's current state,
            // and the AndroidView factory above may run before or after that replay -- the
            // holder's MapLifecycleGate (created fresh per MapView in createPlaceMapView) makes
            // whichever path runs second a no-op, so each MapView gets exactly one onResume per
            // resumed period and one onPause per exit.
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    val mapView = holder.mapView ?: return@LifecycleEventObserver
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> holder.lifecycleGate.resume { mapView.onResume() }
                        Lifecycle.Event.ON_PAUSE -> holder.lifecycleGate.pause { mapView.onPause() }
                        else -> Unit
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }
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
    var radiusHandle: Marker? = null
    var lastFittedCamera: CameraKey? = null
    var lastEmittedRadiusMeters: Float? = null
    var pin: PinKey? = null
    var spec: RadiusSpec? = null
    var radius: Float = 0f
    var lifecycleGate: MapLifecycleGate = MapLifecycleGate()
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
    val pinDiameterPx: Int,
    val handleStrokeWidthPx: Int,
    val handleDiameterPx: Int
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
            pinDiameterPx = PIN_DIAMETER.roundToPx(),
            handleStrokeWidthPx = HANDLE_STROKE_WIDTH.roundToPx(),
            handleDiameterPx = HANDLE_DIAMETER.roundToPx()
        )
    }

private fun buildPinIcon(colors: PlaceMapOverlayColors): GradientDrawable =
    buildOvalIcon(colors.pinFillColor, colors.pinBorderColor, colors.pinStrokeWidthPx, colors.pinDiameterPx)

/** The draggable radius-handle marker's icon -- same fill/border colors as the pin, smaller. */
private fun buildHandleIcon(colors: PlaceMapOverlayColors): GradientDrawable =
    buildOvalIcon(colors.pinFillColor, colors.pinBorderColor, colors.handleStrokeWidthPx, colors.handleDiameterPx)

private fun buildOvalIcon(fillColor: Int, borderColor: Int, strokeWidthPx: Int, diameterPx: Int): GradientDrawable {
    val drawable = GradientDrawable()
    drawable.shape = GradientDrawable.OVAL
    drawable.setColor(fillColor)
    drawable.setStroke(strokeWidthPx, borderColor)
    drawable.setSize(diameterPx, diameterPx)
    drawable.setBounds(0, 0, diameterPx, diameterPx)
    return drawable
}

/** The point [radiusMeters] due east of [center] ([handlePoint]), as an osmdroid [GeoPoint]. */
private fun handlePointGeoPoint(center: GeoPoint, radiusMeters: Float): GeoPoint {
    val handle = handlePoint(center.latitude, center.longitude, radiusMeters)
    return GeoPoint(handle.latitude, handle.longitude)
}

/**
 * Builds the live osmdroid [MapView] once per [PlaceMapPicker] instance. [configureOsmdroid]
 * (D-06) runs first, before any `MapView` is constructed, so this hub is OSM tile-policy
 * compliant by default. The single [MapEventsOverlay] added here is the map's tap-to-drop-pin
 * catch-all: it always reads the *current* [PlaceMapPickerHolder.onPinChange] through [holder],
 * never one captured at this factory's single invocation.
 *
 * Gesture containment: a touch listener requests the parent (the scrolling container the picker
 * sits inside, e.g. the reminder editor's bottom sheet) never intercept a pan or pinch that starts
 * on the map, and releases that request once the gesture ends -- so panning the map never scrolls
 * or dismisses its host. The listener always returns `false` (never consumes the event) so
 * osmdroid's own gesture handling still runs; suppressed only because it never affects behavior.
 *
 * Lifecycle: a fresh [MapLifecycleGate] is created for this `MapView` (one gate per `MapView`,
 * 164-02 review round 2 MEDIUM), and `onResume()` is called here only when [lifecycleOwner] is
 * already at least `RESUMED` -- the composable's own `DisposableEffect`-registered observer
 * handles every *subsequent* resume/pause. Whichever of these two paths (this eager call, or the
 * observer's replay of `ON_RESUME` on registration) runs second is a no-op through the gate, so
 * this `MapView` gets exactly one `onResume()` for the current resumed period.
 */
@SuppressLint("ClickableViewAccessibility") // the touch listener below never consumes an event
private fun createPlaceMapView(
    context: Context,
    userAgent: String,
    holder: PlaceMapPickerHolder,
    lifecycleOwner: LifecycleOwner
): MapView {
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
    mapView.setOnTouchListener { view, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE ->
                view.parent?.requestDisallowInterceptTouchEvent(true)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                view.parent?.requestDisallowInterceptTouchEvent(false)
        }
        false
    }
    holder.mapView = mapView
    holder.lifecycleGate = MapLifecycleGate()
    if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
        holder.lifecycleGate.resume { mapView.onResume() }
    }
    return mapView
}

/**
 * The `AndroidView` `update` pass: with no [pin], clears every pin-owned overlay
 * ([clearPinOverlays]); with a pin, lazily creates the circle, the pin marker and the radius
 * handle, re-applies their theme colors on every pass (so a theme change while the picker is open
 * recolors the overlays), snaps the handle back to [handlePointGeoPoint] of the sanitized
 * [radius], and refits the camera through [shouldRefitCamera] when the pin or an
 * externally-supplied radius changed.
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

    holder.radius = radius
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
        it.setDraggable(true)
        it.setOnMarkerDragListener(PlaceMapPinDragListener(mapView, holder))
        mapView.overlays.add(it)
        holder.pinMarker = it
    }
    marker.position = center
    marker.icon = buildPinIcon(colors)

    val handle = holder.radiusHandle ?: Marker(mapView).also {
        it.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        it.setInfoWindow(null)
        it.setDraggable(true)
        it.setOnMarkerDragListener(PlaceMapHandleDragListener(mapView, holder))
        mapView.overlays.add(it)
        holder.radiusHandle = it
    }
    handle.position = handlePointGeoPoint(center, radius)
    handle.icon = buildHandleIcon(colors)

    if (shouldRefitCamera(holder.lastFittedCamera, CameraKey(pin, radius), holder.lastEmittedRadiusMeters)) {
        fitPlaceMapCamera(mapView, holder, pin, radius, cameraFitBorderPx)
    }

    mapView.invalidate()
}

/**
 * Dragging the pin: moves the circle and the radius handle locally to follow the finger (drag
 * callbacks never call [PlaceMapPickerHolder.onPinChange], so nothing else would move them until
 * drag-end), then commits the new coordinate on drag end -- reading [holder]'s latest radius/pin
 * on every callback, never a value captured when this listener was created.
 */
private class PlaceMapPinDragListener(
    private val mapView: MapView,
    private val holder: PlaceMapPickerHolder
) : Marker.OnMarkerDragListener {
    override fun onMarkerDrag(marker: Marker) {
        val position = marker.position
        val currentRadius = holder.radius
        holder.circle?.setPoints(Polygon.pointsAsCircle(position, currentRadius.toDouble()))
        holder.radiusHandle?.position = handlePointGeoPoint(position, currentRadius)
        mapView.invalidate()
    }

    override fun onMarkerDragEnd(marker: Marker) {
        holder.onPinChange(marker.position.latitude, marker.position.longitude)
    }

    override fun onMarkerDragStart(marker: Marker) = Unit
}

/**
 * Dragging the radius handle: every drag step (and drag end) resolves the new radius through
 * [RadiusSpec.radiusFromHandle], records it in [PlaceMapPickerHolder.lastEmittedRadiusMeters] (so
 * the camera-refit sync pass this emission triggers treats it as an echo, not an external change),
 * and reports it through [PlaceMapPickerHolder.onRadiusChange] -- the resulting state change and
 * recomposition redraw the circle and snap the handle back to the sanitized radius on the next
 * `update` pass.
 */
private class PlaceMapHandleDragListener(
    private val mapView: MapView,
    private val holder: PlaceMapPickerHolder
) : Marker.OnMarkerDragListener {
    override fun onMarkerDrag(marker: Marker) = emitHandleRadius(marker)
    override fun onMarkerDragEnd(marker: Marker) = emitHandleRadius(marker)
    override fun onMarkerDragStart(marker: Marker) = Unit

    private fun emitHandleRadius(marker: Marker) {
        val spec = holder.spec ?: return
        val pin = holder.pin ?: return
        val handlePosition = PinKey(marker.position.latitude, marker.position.longitude)
        val sanitized = spec.radiusFromHandle(pin, handlePosition)
        holder.lastEmittedRadiusMeters = sanitized
        mapView.invalidate()
        holder.onRadiusChange(sanitized)
    }
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
 * Removes every pin-owned overlay (the circle, the pin marker and the radius handle) from
 * [holder]'s `MapView` and clears each holder reference, so no stale overlay remains when the
 * caller's pin becomes invalid (e.g. the place is cleared) -- 164-02 review round 2 MEDIUM: the
 * radius handle Task 2 adds must be cleared here too, not just the circle and the pin marker.
 */
private fun clearPinOverlays(holder: PlaceMapPickerHolder) {
    val mapView = holder.mapView
    if (mapView != null) {
        holder.circle?.let { mapView.overlays.remove(it) }
        holder.pinMarker?.let { mapView.overlays.remove(it) }
        holder.radiusHandle?.let { mapView.overlays.remove(it) }
    }
    holder.circle = null
    holder.pinMarker = null
    holder.radiusHandle = null
    holder.lastFittedCamera = null
}
