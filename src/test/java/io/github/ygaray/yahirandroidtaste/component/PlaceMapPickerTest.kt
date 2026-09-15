package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.osmdroid.util.TileSystemWebMercator

private const val TEST_TAG = "place_map_picker"
private const val TEST_USER_AGENT = "test.agent"

/**
 * Tests for [PlaceMapPicker] (HUBW-02) -- render behavior under [LocalInspectionMode] (D-05; the
 * live osmdroid `MapView` surface is device-only verified) -- and, in the same file per this
 * plan's convention, the pure-Kotlin [PlaceMapPickerModel] math. Robolectric+Compose harness
 * mirrors [DateTimePickerTest]: `@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [35])`,
 * `createComposeRule()`, no theme wrapper.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PlaceMapPickerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ---- Render tests ----

    @Test
    fun `no pin shows the placeholder, hint and attribution, and no radius row`() {
        setPlaceMapPickerContent(pinLatitude = null, pinLongitude = null)
        assertNoPinPlaceholder()
    }

    @Test
    fun `one coordinate only is treated the same as no pin`() {
        setPlaceMapPickerContent(pinLatitude = 40.0, pinLongitude = null)
        assertNoPinPlaceholder()
    }

    @Test
    fun `a valid pin hides the hint, shows the radius row, and never renders a coordinate`() {
        setPlaceMapPickerContent(pinLatitude = 40.0, pinLongitude = -74.0, radiusMeters = 300f)

        composeTestRule.onNodeWithText("Tap the map to drop a pin").assertDoesNotExist()
        composeTestRule.onNodeWithText("300 m radius").assertExists()
        composeTestRule.onNodeWithText("50 m").assertExists()
        composeTestRule.onNodeWithText("1000 m").assertExists()
        composeTestRule.onNodeWithContentDescription("Radius").assertExists()
        composeTestRule.onNodeWithText("© OpenStreetMap contributors").assertExists()

        composeTestRule.onAllNodesWithText("40.0", substring = true).assertCountEquals(0)
        composeTestRule.onAllNodesWithText("74.0", substring = true).assertCountEquals(0)
        composeTestRule.onAllNodes(hasContentDescription("40.0", substring = true)).assertCountEquals(0)
        composeTestRule.onAllNodes(hasContentDescription("74.0", substring = true)).assertCountEquals(0)
    }

    @Test
    fun `dragging the radius slider sanitizes and emits through onRadiusChange`() {
        val emitted = mutableListOf<Float>()
        composeTestRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                var radius by remember { mutableStateOf(300f) }
                PlaceMapPicker(
                    pinLatitude = 40.0,
                    pinLongitude = -74.0,
                    onPinChange = { _, _ -> },
                    radiusMeters = radius,
                    onRadiusChange = {
                        radius = it
                        emitted.add(it)
                    },
                    minRadiusMeters = 50f,
                    maxRadiusMeters = 1000f,
                    defaultRadiusMeters = 150f,
                    userAgent = TEST_USER_AGENT,
                    radiusStepMeters = 50f
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Radius")
            .performSemanticsAction(SemanticsActions.SetProgress) { it(520f) }
        assertEquals(500f, emitted.last())

        composeTestRule.onNodeWithContentDescription("Radius")
            .performSemanticsAction(SemanticsActions.SetProgress) { it(5000f) }
        assertEquals(1000f, emitted.last())

        composeTestRule.onNodeWithContentDescription("Radius")
            .performSemanticsAction(SemanticsActions.SetProgress) { it(-10f) }
        assertEquals(50f, emitted.last())
    }

    @Test
    fun `a non-finite radius with a pin renders the default`() {
        setPlaceMapPickerContent(pinLatitude = 40.0, pinLongitude = -74.0, radiusMeters = Float.NaN)
        composeTestRule.onNodeWithText("150 m radius").assertExists()
    }

    @Test
    fun `drop pin at map center with no pin delivers 0,0 exactly once`() {
        val received = mutableListOf<Pair<Double, Double>>()
        composeTestRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                PlaceMapPicker(
                    pinLatitude = null,
                    pinLongitude = null,
                    onPinChange = { lat, lng -> received.add(lat to lng) },
                    radiusMeters = 150f,
                    onRadiusChange = {},
                    minRadiusMeters = 50f,
                    maxRadiusMeters = 1000f,
                    defaultRadiusMeters = 150f,
                    userAgent = TEST_USER_AGENT
                )
            }
        }

        invokeDropPinAtCenter("${TEST_TAG}_map_placeholder")

        assertEquals(listOf(0.0 to 0.0), received)
    }

    @Test
    fun `drop pin at map center with a pin delivers the pin's own coordinate`() {
        val received = mutableListOf<Pair<Double, Double>>()
        composeTestRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                PlaceMapPicker(
                    pinLatitude = 40.0,
                    pinLongitude = -74.0,
                    onPinChange = { lat, lng -> received.add(lat to lng) },
                    radiusMeters = 150f,
                    onRadiusChange = {},
                    minRadiusMeters = 50f,
                    maxRadiusMeters = 1000f,
                    defaultRadiusMeters = 150f,
                    userAgent = TEST_USER_AGENT
                )
            }
        }

        invokeDropPinAtCenter("${TEST_TAG}_map_placeholder")

        assertEquals(listOf(40.0 to -74.0), received)
    }

    @Test
    fun `a polar pin renders the radius row, never calls onPinChange, and drop-pin delivers the real latitude`() {
        val received = mutableListOf<Pair<Double, Double>>()
        composeTestRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                PlaceMapPicker(
                    pinLatitude = 87.0,
                    pinLongitude = 10.0,
                    onPinChange = { lat, lng -> received.add(lat to lng) },
                    radiusMeters = 300f,
                    onRadiusChange = {},
                    minRadiusMeters = 50f,
                    maxRadiusMeters = 1000f,
                    defaultRadiusMeters = 150f,
                    userAgent = TEST_USER_AGENT
                )
            }
        }

        composeTestRule.onNodeWithText("Tap the map to drop a pin").assertDoesNotExist()
        composeTestRule.onNodeWithText("300 m radius").assertExists()
        assertTrue("Composition must never call onPinChange on its own", received.isEmpty())

        invokeDropPinAtCenter("${TEST_TAG}_map_placeholder")

        assertEquals(listOf(87.0 to 10.0), received)
    }

    @Test
    fun `a swapped onRadiusChange callback always routes to the most recently passed lambda`() {
        val first = mutableListOf<Float>()
        val second = mutableListOf<Float>()
        composeTestRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                var onRadiusChange by remember {
                    mutableStateOf<(Float) -> Unit>({ first.add(it) })
                }
                Column {
                    PlaceMapPicker(
                        pinLatitude = 40.0,
                        pinLongitude = -74.0,
                        onPinChange = { _, _ -> },
                        radiusMeters = 300f,
                        onRadiusChange = onRadiusChange,
                        minRadiusMeters = 50f,
                        maxRadiusMeters = 1000f,
                        defaultRadiusMeters = 150f,
                        userAgent = TEST_USER_AGENT
                    )
                    TextButton(onClick = { onRadiusChange = { second.add(it) } }) { Text("Swap") }
                }
            }
        }

        composeTestRule.onNodeWithText("Swap").performClick()
        composeTestRule.onNodeWithContentDescription("Radius")
            .performSemanticsAction(SemanticsActions.SetProgress) { it(600f) }

        assertTrue("The pre-swap callback must never fire after the swap", first.isEmpty())
        assertEquals(600f, second.last())
    }

    // ---- Model tests: RadiusSpec ----

    @Test
    fun `RadiusSpec sanitize clamps, snaps to the step grid, and falls back on non-finite input`() {
        val spec = RadiusSpec(50f, 1000f, 150f, 50f)
        assertEquals(500f, spec.sanitize(520f))
        assertEquals(50f, spec.sanitize(49f))
        assertEquals(1000f, spec.sanitize(1001f))
        assertEquals(150f, spec.sanitize(Float.NaN))
        assertEquals(150f, spec.sanitize(Float.POSITIVE_INFINITY))

        val wholeMeterSpec = RadiusSpec(50f, 1000f, 150f, 0f)
        assertEquals(124f, wholeMeterSpec.sanitize(123.6f))
    }

    @Test
    fun `sliderSteps and sanitize agree on the same representable radii`() {
        val spec = RadiusSpec(50f, 1000f, 150f, 50f)
        assertEquals(18, spec.sliderSteps())

        val representable = (0..19).map { 50f + 50f * it }.toSet()
        for (k in 0..19) {
            for (offset in listOf(-20f, 0f, 20f)) {
                val input = 50f + 50f * k + offset
                assertTrue(
                    "sanitize($input) = ${spec.sanitize(input)} must be a Slider stop",
                    spec.sanitize(input) in representable
                )
            }
        }

        assertEquals(0, RadiusSpec(50f, 1000f, 150f, 0f).sliderSteps())
    }

    @Test
    fun `RadiusSpec rejects min greater than max`() {
        assertThrows(IllegalArgumentException::class.java) { RadiusSpec(100f, 50f, 75f, 0f) }
    }

    @Test
    fun `RadiusSpec rejects a default above max`() {
        assertThrows(IllegalArgumentException::class.java) { RadiusSpec(50f, 1000f, 2000f, 0f) }
    }

    @Test
    fun `RadiusSpec rejects a non-positive min`() {
        assertThrows(IllegalArgumentException::class.java) { RadiusSpec(0f, 1000f, 150f, 0f) }
    }

    @Test
    fun `RadiusSpec rejects a negative step`() {
        assertThrows(IllegalArgumentException::class.java) { RadiusSpec(50f, 1000f, 150f, -1f) }
    }

    @Test
    fun `RadiusSpec rejects a non-finite min`() {
        assertThrows(IllegalArgumentException::class.java) { RadiusSpec(Float.NaN, 1000f, 150f, 0f) }
    }

    @Test
    fun `RadiusSpec rejects a non-finite max`() {
        assertThrows(IllegalArgumentException::class.java) {
            RadiusSpec(50f, Float.POSITIVE_INFINITY, 150f, 0f)
        }
    }

    @Test
    fun `RadiusSpec rejects a non-finite default`() {
        assertThrows(IllegalArgumentException::class.java) { RadiusSpec(50f, 1000f, Float.NaN, 0f) }
    }

    @Test
    fun `RadiusSpec rejects a non-finite step`() {
        assertThrows(IllegalArgumentException::class.java) { RadiusSpec(50f, 1000f, 150f, Float.NaN) }
    }

    @Test
    fun `RadiusSpec rejects a step that does not evenly divide the range`() {
        assertThrows(IllegalArgumentException::class.java) { RadiusSpec(50f, 1000f, 150f, 70f) }
    }

    @Test
    fun `RadiusSpec rejects a default that is off the step grid`() {
        assertThrows(IllegalArgumentException::class.java) { RadiusSpec(50f, 1000f, 175f, 50f) }
    }

    // ---- Model tests: pinKeyOrNull ----

    @Test
    fun `pinKeyOrNull validates both coordinates`() {
        assertEquals(PinKey(40.0, -74.0), pinKeyOrNull(40.0, -74.0))
        assertEquals(null, pinKeyOrNull(Double.NaN, 1.0))
        assertEquals(null, pinKeyOrNull(91.0, 0.0))
        assertEquals(null, pinKeyOrNull(0.0, 181.0))
        assertEquals(null, pinKeyOrNull(null, 1.0))
    }

    // ---- Model tests: shouldRefitCamera ----

    @Test
    fun `shouldRefitCamera refits on pin change or external radius change, never on its own echo`() {
        val p1 = PinKey(10.0, 20.0)
        val p2 = PinKey(11.0, 21.0)

        assertTrue(shouldRefitCamera(null, CameraKey(p1, 150f), null))
        assertFalse(shouldRefitCamera(CameraKey(p1, 150f), CameraKey(p1, 150f), null))
        assertTrue(shouldRefitCamera(CameraKey(p1, 150f), CameraKey(p2, 150f), 150f))
        assertTrue(shouldRefitCamera(CameraKey(p1, 150f), CameraKey(p1, 500f), null))
        assertFalse(shouldRefitCamera(CameraKey(p1, 150f), CameraKey(p1, 500f), 500f))
        assertFalse(shouldRefitCamera(CameraKey(p1, 150f), null, null))
    }

    // ---- Model tests: radiusBounds / cameraLatitude / Web Mercator limit ----

    @Test
    fun `radiusBounds at the equator is symmetric around the pin`() {
        val bounds = radiusBounds(0.0, 0.0, 1000f)
        assertEquals(1000.0 / METERS_PER_DEGREE_LATITUDE, bounds.north, 1e-6)
        assertEquals(-bounds.north, bounds.south, 1e-9)
        assertEquals(bounds.north, bounds.east, 1e-9)
        assertEquals(-bounds.east, bounds.west, 1e-9)

        assertTrue(radiusBounds(85.0, 0.0, 50000f).north <= WEB_MERCATOR_MAX_LATITUDE)
    }

    @Test
    fun `WEB_MERCATOR_MAX_LATITUDE equals osmdroid's own TileSystemWebMercator MaxLatitude exactly`() {
        assertEquals(TileSystemWebMercator.MaxLatitude, WEB_MERCATOR_MAX_LATITUDE, 0.0)
    }

    @Test
    fun `cameraLatitude clamps at the Web Mercator limit without touching in-range latitudes`() {
        assertEquals(WEB_MERCATOR_MAX_LATITUDE, cameraLatitude(90.0), 0.0)
        assertEquals(WEB_MERCATOR_MAX_LATITUDE, cameraLatitude(85.1), 0.0)
        assertEquals(WEB_MERCATOR_MAX_LATITUDE, cameraLatitude(85.05112878), 0.0)
        assertEquals(-WEB_MERCATOR_MAX_LATITUDE, cameraLatitude(-90.0), 0.0)
        assertEquals(-WEB_MERCATOR_MAX_LATITUDE, cameraLatitude(-85.1), 0.0)
        assertEquals(-WEB_MERCATOR_MAX_LATITUDE, cameraLatitude(-85.05112878), 0.0)
        assertEquals(60.0, cameraLatitude(60.0), 0.0)
    }

    @Test
    fun `radiusBounds at the poles never collapses north onto south`() {
        val north = radiusBounds(90.0, 0.0, 1000f)
        assertEquals(WEB_MERCATOR_MAX_LATITUDE, north.north, 0.0)
        assertEquals(WEB_MERCATOR_MAX_LATITUDE - 1000.0 / METERS_PER_DEGREE_LATITUDE, north.south, 1e-6)

        val south = radiusBounds(-90.0, 0.0, 1000f)
        assertEquals(-WEB_MERCATOR_MAX_LATITUDE, south.south, 0.0)
        assertEquals(-WEB_MERCATOR_MAX_LATITUDE + 1000.0 / METERS_PER_DEGREE_LATITUDE, south.north, 1e-6)
    }

    @Test
    fun `polar and dateline sweep always yields a valid, non-degenerate osmdroid box`() {
        val tileSystem = TileSystemWebMercator()
        val latitudes = listOf(85.05112877980658, 85.05112878, 85.1, 87.0, 90.0)
            .flatMap { listOf(it, -it) }
        val longitudes = listOf(-180.0, -179.995, 0.0, 179.995, 180.0)
        val radii = listOf(0f, 1f, 50f, 1000f, 50000f, 20000000f)

        for (latitude in latitudes) {
            for (longitude in longitudes) {
                for (radius in radii) {
                    val bounds = radiusBounds(latitude, longitude, radius)
                    val context = "lat=$latitude lng=$longitude radius=$radius bounds=$bounds"
                    assertTrue("south < north failed: $context", bounds.south < bounds.north)
                    assertTrue("west < east failed: $context", bounds.west < bounds.east)
                    val clampedLatitude = cameraLatitude(latitude)
                    assertTrue(
                        "cameraLatitude not within [south, north]: $context",
                        clampedLatitude in bounds.south..bounds.north
                    )
                    assertTrue(
                        "north not a valid osmdroid latitude: $context",
                        tileSystem.isValidLatitude(bounds.north)
                    )
                    assertTrue(
                        "south not a valid osmdroid latitude: $context",
                        tileSystem.isValidLatitude(bounds.south)
                    )
                    assertTrue(
                        "east not a valid osmdroid longitude: $context",
                        tileSystem.isValidLongitude(bounds.east)
                    )
                    assertTrue(
                        "west not a valid osmdroid longitude: $context",
                        tileSystem.isValidLongitude(bounds.west)
                    )
                }
            }
        }
    }

    @Test
    fun `radiusBounds clamps at the antimeridian instead of wrapping`() {
        val east = radiusBounds(0.0, 179.995, 1000f)
        assertEquals(180.0, east.east, 0.0)
        assertTrue(east.west < east.east)
        assertEquals(179.995 - 1000.0 / METERS_PER_DEGREE_LATITUDE, east.west, 1e-6)

        val west = radiusBounds(0.0, -179.995, 1000f)
        assertEquals(-180.0, west.west, 0.0)
        assertTrue(west.east > west.west)

        var longitude = -180.0
        while (longitude <= 180.0) {
            for (latitude in listOf(0.0, 60.0)) {
                val bounds = radiusBounds(latitude, longitude, 50000f)
                assertTrue(
                    "west < east failed at lat=$latitude lng=$longitude: $bounds",
                    bounds.west < bounds.east
                )
                assertTrue(
                    "south < north failed at lat=$latitude lng=$longitude: $bounds",
                    bounds.south < bounds.north
                )
            }
            longitude += 0.5
        }
    }

    // ---- Model tests: normalizeLongitude ----

    @Test
    fun `normalizeLongitude wraps into range and keeps 180 at 180`() {
        assertEquals(-179.0, normalizeLongitude(181.0), 1e-9)
        assertEquals(179.0, normalizeLongitude(-181.0), 1e-9)
        assertEquals(180.0, normalizeLongitude(180.0), 1e-9)
        assertEquals(180.0, normalizeLongitude(540.0), 1e-9)
    }

    // ---- Helpers ----

    private fun setPlaceMapPickerContent(
        pinLatitude: Double?,
        pinLongitude: Double?,
        radiusMeters: Float = 150f
    ) {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                PlaceMapPicker(
                    pinLatitude = pinLatitude,
                    pinLongitude = pinLongitude,
                    onPinChange = { _, _ -> },
                    radiusMeters = radiusMeters,
                    onRadiusChange = {},
                    minRadiusMeters = 50f,
                    maxRadiusMeters = 1000f,
                    defaultRadiusMeters = 150f,
                    userAgent = TEST_USER_AGENT,
                    radiusStepMeters = 50f
                )
            }
        }
    }

    private fun assertNoPinPlaceholder() {
        composeTestRule.onNodeWithTag("${TEST_TAG}_map_placeholder").assertExists()
        composeTestRule.onNodeWithTag("${TEST_TAG}_map").assertDoesNotExist()
        composeTestRule.onNodeWithText("Tap the map to drop a pin").assertExists()
        composeTestRule.onNodeWithText("© OpenStreetMap contributors").assertExists()
        composeTestRule.onNodeWithTag("${TEST_TAG}_radius").assertDoesNotExist()
    }

    private fun invokeDropPinAtCenter(tag: String) {
        val node = composeTestRule.onNodeWithTag(tag).fetchSemanticsNode()
        val action = node.config[SemanticsActions.CustomActions]
            .first { it.label == "Drop pin at map center" }
        composeTestRule.runOnIdle { action.action() }
    }
}
