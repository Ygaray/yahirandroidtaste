package io.github.ygaray.yahirandroidtaste.component

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * HUBW-02 D-04/D-05: pure-Kotlin state math for [PlaceMapPicker] -- radius sanitization, pin
 * validity, camera-refit decisions, and dateline-/pole-safe camera bounds. No Android, Compose or
 * osmdroid import here (grep-enforced by this plan's acceptance criteria); every function is
 * exercised directly by [PlaceMapPickerTest]'s model-test section without Robolectric. The
 * `MapView` surface itself is device-only verified (D-05) -- see [PlaceMapPicker].
 */

/**
 * Meters per degree of latitude, used for the camera box's north/south span (this file) and,
 * independently, tuned per-function for the handle math this hub adds later so a handle placed
 * [radiusMeters] away and measured back through a haversine distance round-trips tightly (see
 * that function's own KDoc when it lands).
 */
internal const val METERS_PER_DEGREE_LATITUDE = 111_320.0

/**
 * osmdroid's exact `org.osmdroid.util.TileSystemWebMercator.MaxLatitude` (javap-confirmed against
 * the resolved 6.1.20 AAR during 164-02 review round 2 HIGH) -- the tile system's own inclusive
 * `isValidLatitude` limit. A rounded value such as `85.05112878` lies just *above* this exact
 * constant and fails that check; see [cameraLatitude] and [radiusBounds].
 */
internal const val WEB_MERCATOR_MAX_LATITUDE = 85.05112877980658

/**
 * Floor for the camera box's latitude half-span (degrees) so a zero (or tiny) radius never
 * collapses north onto south. About 11 meters at the equator.
 */
internal const val MIN_CAMERA_HALF_SPAN_DEGREES = 0.0001

/** osmdroid's initial/minimum world-view zoom level for [PlaceMapPicker]'s map area. */
internal const val PLACE_MAP_WORLD_ZOOM = 3.0

/** osmdroid's maximum zoom level for [PlaceMapPicker]'s map area. */
internal const val PLACE_MAP_MAX_ZOOM = 19.0

/**
 * D-04: a validated, immutable min/max/default/step radius configuration. [sanitize] and
 * [sliderSteps] agree on exactly the same set of representable radii, so a caller-driven `Slider`
 * (whose discrete stops [sliderSteps] describes) and a map-handle drag (which always resolves
 * through [sanitize]) can never disagree about which radii are reachable (164-02 review round 1
 * MEDIUM).
 *
 * @throws IllegalArgumentException unless all four values are finite, [minMeters] is positive,
 *   `minMeters <= defaultMeters <= maxMeters`, [stepMeters] is non-negative, and -- when
 *   [stepMeters] is positive -- `(maxMeters - minMeters)` is a whole multiple of [stepMeters]
 *   (within `1e-3`) and [defaultMeters] itself lies on that step grid.
 */
internal class RadiusSpec(
    val minMeters: Float,
    val maxMeters: Float,
    val defaultMeters: Float,
    val stepMeters: Float
) {
    init {
        require(
            minMeters.isFinite() && maxMeters.isFinite() && defaultMeters.isFinite() && stepMeters.isFinite()
        ) {
            "RadiusSpec requires all four values finite: min=$minMeters max=$maxMeters " +
                "default=$defaultMeters step=$stepMeters"
        }
        require(minMeters > 0f) { "RadiusSpec requires minMeters > 0, was $minMeters" }
        require(minMeters <= defaultMeters && defaultMeters <= maxMeters) {
            "RadiusSpec requires minMeters <= defaultMeters <= maxMeters, was " +
                "$minMeters/$defaultMeters/$maxMeters"
        }
        require(stepMeters >= 0f) { "RadiusSpec requires stepMeters >= 0, was $stepMeters" }
        if (stepMeters > 0f) {
            val stepsAcrossRange = (maxMeters - minMeters) / stepMeters
            require(abs(stepsAcrossRange - round(stepsAcrossRange)) <= 1e-3f) {
                "RadiusSpec requires (maxMeters - minMeters) to be a whole multiple of " +
                    "stepMeters, was $stepsAcrossRange steps"
            }
            val snappedDefault = snapToStep(defaultMeters)
            require(abs(snappedDefault - defaultMeters) <= 1e-3f) {
                "RadiusSpec requires defaultMeters to lie on the step grid, was $defaultMeters " +
                    "(nearest grid value $snappedDefault)"
            }
        }
    }

    /**
     * Non-finite input returns [defaultMeters]; otherwise clamps to [minMeters]..[maxMeters],
     * snaps to the step grid (or the nearest whole meter when [stepMeters] is `0`), then clamps
     * again so a snap can never push the result back outside the range.
     */
    fun sanitize(value: Float): Float {
        if (!value.isFinite()) return defaultMeters
        val clamped = value.coerceIn(minMeters, maxMeters)
        val snapped = if (stepMeters > 0f) snapToStep(clamped) else round(clamped)
        return snapped.coerceIn(minMeters, maxMeters)
    }

    /**
     * The number of interior `Slider` stops (the Compose `Slider.steps` parameter): `0` when
     * [stepMeters] is `0` (continuous), otherwise the count of grid points strictly between
     * [minMeters] and [maxMeters].
     */
    fun sliderSteps(): Int {
        if (stepMeters <= 0f) return 0
        val raw = round((maxMeters - minMeters) / stepMeters) - 1
        return max(raw, 0f).toInt()
    }

    private fun snapToStep(value: Float): Float =
        minMeters + round((value - minMeters) / stepMeters) * stepMeters

    /**
     * The distance from [center] to [handle] ([haversineMeters]), sanitized through this spec --
     * what a radius-handle drag on the map circle resolves to (GREEN).
     */
    fun radiusFromHandle(center: PinKey, handle: PinKey): Float =
        sanitize(haversineMeters(center.latitude, center.longitude, handle.latitude, handle.longitude).toFloat())
}

/** A validated pin: both coordinates present, finite, and within their valid geographic ranges. */
internal data class PinKey(val latitude: Double, val longitude: Double)

/**
 * Returns a [PinKey] only when both [latitude] and [longitude] are non-null, finite, and within
 * range (`latitude` in `[-90, 90]`, `longitude` in `[-180, 180]`); `null` otherwise. A pin
 * beyond the Web Mercator limit (`|latitude| > `[WEB_MERCATOR_MAX_LATITUDE]`) is still a *valid*
 * pin here -- see [cameraLatitude] for why the camera, not the pin, is what clamps.
 */
internal fun pinKeyOrNull(latitude: Double?, longitude: Double?): PinKey? {
    if (latitude == null || longitude == null) return null
    if (!latitude.isFinite() || !longitude.isFinite()) return null
    if (latitude < -90.0 || latitude > 90.0) return null
    if (longitude < -180.0 || longitude > 180.0) return null
    return PinKey(latitude, longitude)
}

/** The pin + radius pair a camera fit was (or is being asked to be) computed from. */
internal data class CameraKey(val pin: PinKey, val radiusMeters: Float)

/**
 * Camera-refit decision (164-02 review round 1 LOW): `true` only when [current] is non-null and
 * either there is no [previous] fit, the pin moved, or the radius changed to a value that is
 * *not* the echo of the widget's own most recent handle/slider emission
 * ([lastEmittedRadiusMeters]) -- so dragging the handle or the slider never makes the camera
 * jump, but an externally supplied radius change (a saved place, search, current location) at the
 * same pin still refits.
 */
internal fun shouldRefitCamera(
    previous: CameraKey?,
    current: CameraKey?,
    lastEmittedRadiusMeters: Float?
): Boolean {
    if (current == null) return false
    if (previous == null) return true
    if (previous.pin != current.pin) return true
    if (previous.radiusMeters == current.radiusMeters) return false
    return current.radiusMeters != lastEmittedRadiusMeters
}

/** Normalizes [value] into `[-180, 180]`, keeping `180` at `180` (never wraps it to `-180`). */
internal fun normalizeLongitude(value: Double): Double {
    var result = value
    while (result > 180.0) result -= 360.0
    while (result < -180.0) result += 360.0
    return result
}

/**
 * A camera bounding box in the degenerate-free form osmdroid's Web Mercator tile system requires:
 * strictly `north > south`, strictly `east > west`, both inside their respective ±limits.
 */
internal data class RadiusBounds(val north: Double, val south: Double, val east: Double, val west: Double)

/**
 * Clamps [latitude] to `±`[WEB_MERCATOR_MAX_LATITUDE] for camera-box purposes only -- it never
 * rewrites the caller's stored pin value (164-02 review round 2 HIGH). A pin may legitimately
 * hold any latitude in `[-90, 90]` (a stored reminder, a geocoder or GPS result), but osmdroid's
 * Web Mercator tiles stop at this limit; the camera box is framed at the nearest representable
 * edge instead of rejecting such a pin (see [PlaceMapPicker]'s KDoc for the full rationale).
 */
internal fun cameraLatitude(latitude: Double): Double =
    latitude.coerceIn(-WEB_MERCATOR_MAX_LATITUDE, WEB_MERCATOR_MAX_LATITUDE)

/**
 * The camera box that frames the circle at ([latitude], [longitude]) with radius [radiusMeters],
 * computed for camera purposes only:
 *  - latitude is clamped through [cameraLatitude] (never the caller's pin);
 *  - the latitude half-span is floored at [MIN_CAMERA_HALF_SPAN_DEGREES] so a zero radius still
 *    yields a non-degenerate box, and a non-finite or negative radius is treated as zero;
 *  - `north`/`south` are each clamped to `±`[WEB_MERCATOR_MAX_LATITUDE], which keeps `south`
 *    strictly below `north` for every input (the clamped center always lies strictly inside the
 *    limit and the half-span is always strictly positive);
 *  - the longitude half-span is the latitude half-span divided by `cos(cameraLatitude)`, with
 *    that cosine denominator floored at `0.01` so a pin near a pole never explodes the span
 *    toward the opposite side of the globe;
 *  - the box is *clamped* (never wrapped) at `±180` longitude, trading a sliver of a circle that
 *    crosses the antimeridian for a box osmdroid can always fit (164-02 review round 1 MEDIUM); a
 *    span covering more than the whole world returns the full `[-180, 180]` longitude range.
 *
 * The result therefore always satisfies `south < north` and `west < east`.
 */
internal fun radiusBounds(latitude: Double, longitude: Double, radiusMeters: Float): RadiusBounds {
    val center = cameraLatitude(latitude)
    val safeRadius = if (radiusMeters.isFinite() && radiusMeters > 0f) radiusMeters.toDouble() else 0.0
    val latHalfSpan = max(safeRadius / METERS_PER_DEGREE_LATITUDE, MIN_CAMERA_HALF_SPAN_DEGREES)
    val north = min(center + latHalfSpan, WEB_MERCATOR_MAX_LATITUDE)
    val south = max(center - latHalfSpan, -WEB_MERCATOR_MAX_LATITUDE)

    val cosCenter = cos(center * PI / 180.0)
    val lngHalfSpan = latHalfSpan / max(cosCenter, 0.01)
    if (lngHalfSpan >= 180.0) {
        return RadiusBounds(north = north, south = south, east = 180.0, west = -180.0)
    }
    val rawEast = longitude + lngHalfSpan
    val rawWest = longitude - lngHalfSpan
    return RadiusBounds(
        north = north,
        south = south,
        east = min(rawEast, 180.0),
        west = max(rawWest, -180.0)
    )
}

/** Formats [value] as rounded whole meters followed by `" m"` (e.g. `"300 m"`). */
internal fun formatMeters(value: Float): String = "${round(value).toInt()} m"

/** Mean earth radius in meters (IUGG), used by [haversineMeters] and [handlePoint]'s round-trip. */
internal const val EARTH_RADIUS_METERS = 6_371_008.8

/**
 * Meters per degree of longitude at the equator on [EARTH_RADIUS_METERS]'s sphere -- the same
 * spherical model [haversineMeters] measures distance on. [handlePoint] uses this (not
 * [METERS_PER_DEGREE_LATITUDE], a separate, deliberately different constant the camera-box math
 * in [radiusBounds] uses) so a handle it places and a distance [haversineMeters] measures back
 * always agree to sub-meter precision -- mixing the two constants here would put the handle
 * roughly 0.1% off its true radius (about 1.1m at 1000m), which is exactly the round-trip
 * [PlaceMapPickerModelTest] locks down.
 */
private val METERS_PER_DEGREE_AT_EQUATOR = EARTH_RADIUS_METERS * PI / 180.0

/**
 * Great-circle distance in meters between ([lat1], [lng1]) and ([lat2], [lng2]) on a sphere of
 * radius [EARTH_RADIUS_METERS] (the standard haversine formula).
 */
internal fun haversineMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val phi1 = lat1 * PI / 180.0
    val phi2 = lat2 * PI / 180.0
    val deltaPhi = (lat2 - lat1) * PI / 180.0
    val deltaLambda = (lng2 - lng1) * PI / 180.0
    val a = sin(deltaPhi / 2.0).pow(2) + cos(phi1) * cos(phi2) * sin(deltaLambda / 2.0).pow(2)
    val c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))
    return EARTH_RADIUS_METERS * c
}

/**
 * The point [radiusMeters] due east of ([latitude], [longitude]), on the same spherical model
 * [haversineMeters] uses ([METERS_PER_DEGREE_AT_EQUATOR]) -- so a handle placed here and
 * measured back through [haversineMeters] round-trips to [radiusMeters] to sub-meter precision.
 * Longitude is wrapped through [normalizeLongitude] (a `Marker` position may cross the
 * antimeridian; only the camera box in [radiusBounds] must not). The cosine denominator is
 * floored at `0.01`, the same floor [radiusBounds] uses, so a handle near a pole never explodes
 * toward the opposite side of the globe.
 */
internal fun handlePoint(latitude: Double, longitude: Double, radiusMeters: Float): PinKey {
    val cosLatitude = cos(latitude * PI / 180.0)
    val degreesPerMeter = 1.0 / (METERS_PER_DEGREE_AT_EQUATOR * max(cosLatitude, 0.01))
    val handleLongitude = normalizeLongitude(longitude + radiusMeters * degreesPerMeter)
    return PinKey(latitude, handleLongitude)
}

/**
 * Makes [org.osmdroid.views.MapView.onResume]/`onPause` idempotent per `MapView` (164-02 review
 * round 2 MEDIUM): androidx.lifecycle's `addObserver` replays `ON_CREATE`/`ON_START`/`ON_RESUME`
 * to catch a newly-registered observer up to the host's current state, and [PlaceMapPicker]'s
 * `AndroidView` factory may run before or after that replay -- without this gate, both paths
 * could call `onResume()` on the same `MapView`. One gate per `MapView`, main thread only.
 *
 * (GREEN) `resume` runs [block] and marks this gate resumed only when it was not already
 * resumed; `pause` runs its block and clears the flag only when it was resumed. Each returns
 * whether its block ran, so a caller can tell a real transition from a no-op.
 */
internal class MapLifecycleGate {
    private var resumed = false

    /** Runs [block] and marks this gate resumed only when it was not already resumed. */
    fun resume(block: () -> Unit): Boolean {
        if (resumed) return false
        block()
        resumed = true
        return true
    }

    /** Runs [block] and clears the resumed flag only when this gate was resumed. */
    fun pause(block: () -> Unit): Boolean {
        if (!resumed) return false
        block()
        resumed = false
        return true
    }
}
