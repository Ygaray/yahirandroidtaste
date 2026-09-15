package io.github.ygaray.yahirandroidtaste.component

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

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
