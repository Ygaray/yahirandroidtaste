package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.model.SavedPlaceUiModel
import io.github.ygaray.yahirandroidtaste.model.occurrenceIndices
import io.github.ygaray.yahirandroidtaste.model.savedPlaceChipLabels
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.osmdroid.util.TileSystemWebMercator
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val TEST_TAG = "place_map_picker"
private const val TEST_USER_AGENT = "test.agent"

/** Fixture for [PlaceMapPickerTest]'s `functionBody` sanity cases (164-02 Task 2). */
private const val FUNCTION_BODY_FIXTURE = """fun a(x: () -> Unit = {}) {
    b("}")
}
fun c() { }
"""

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

    // ---- Search tests (164-03 Task 1) ----

    @Test
    fun `onSearch null hides the search field`() {
        setPlaceMapPickerContent(pinLatitude = null, pinLongitude = null, onSearch = null)
        composeTestRule.onNodeWithText("Search for an address").assertDoesNotExist()
    }

    @Test
    fun `typing in the search field only forwards onSearchQueryChange, never onSearch`() {
        val queries = mutableListOf<String>()
        val submitted = mutableListOf<String>()
        composeTestRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                var query by remember { mutableStateOf("") }
                PlaceMapPicker(
                    pinLatitude = null,
                    pinLongitude = null,
                    onPinChange = { _, _ -> },
                    radiusMeters = 150f,
                    onRadiusChange = {},
                    minRadiusMeters = 50f,
                    maxRadiusMeters = 1000f,
                    defaultRadiusMeters = 150f,
                    userAgent = TEST_USER_AGENT,
                    searchQuery = query,
                    onSearchQueryChange = {
                        query = it
                        queries.add(it)
                    },
                    onSearch = { submitted.add(it) }
                )
            }
        }

        composeTestRule.onNodeWithTag("${TEST_TAG}_search").performTextInput("42 Main")

        assertTrue("onSearchQueryChange must fire at least once", queries.isNotEmpty())
        assertTrue("onSearch must never fire from typing", submitted.isEmpty())
    }

    @Test
    fun `IME Search delivers the query exactly once`() {
        val submitted = mutableListOf<String>()
        setPlaceMapPickerContent(
            pinLatitude = null,
            pinLongitude = null,
            searchQuery = "42 Main St",
            onSearch = { submitted.add(it) }
        )

        composeTestRule.onNodeWithTag("${TEST_TAG}_search").performImeAction()

        assertEquals(listOf("42 Main St"), submitted)
    }

    @Test
    fun `IME Search trims surrounding whitespace before delivering the query`() {
        val submitted = mutableListOf<String>()
        setPlaceMapPickerContent(
            pinLatitude = null,
            pinLongitude = null,
            searchQuery = "  42 Main St  ",
            onSearch = { submitted.add(it) }
        )

        composeTestRule.onNodeWithTag("${TEST_TAG}_search").performImeAction()

        assertEquals(listOf("42 Main St"), submitted)
    }

    @Test
    fun `IME Search on a blank query never calls onSearch`() {
        val submitted = mutableListOf<String>()
        setPlaceMapPickerContent(
            pinLatitude = null,
            pinLongitude = null,
            searchQuery = "   ",
            onSearch = { submitted.add(it) }
        )

        composeTestRule.onNodeWithTag("${TEST_TAG}_search").performImeAction()

        assertTrue(submitted.isEmpty())
    }

    @Test
    fun `IME Search while isResolving never calls onSearch`() {
        val submitted = mutableListOf<String>()
        setPlaceMapPickerContent(
            pinLatitude = null,
            pinLongitude = null,
            searchQuery = "42 Main St",
            onSearch = { submitted.add(it) },
            isResolving = true
        )

        composeTestRule.onNodeWithTag("${TEST_TAG}_search").performImeAction()

        assertTrue(submitted.isEmpty())
    }

    @Test
    fun `searchErrorText renders only when non-null`() {
        setPlaceMapPickerContent(
            pinLatitude = null,
            pinLongitude = null,
            onSearch = {},
            searchErrorText = "No address found"
        )
        composeTestRule.onNodeWithText("No address found").assertExists()
    }

    @Test
    fun `searchErrorText null renders no error text`() {
        setPlaceMapPickerContent(
            pinLatitude = null,
            pinLongitude = null,
            onSearch = {},
            searchErrorText = null
        )
        composeTestRule.onNodeWithText("No address found").assertDoesNotExist()
    }

    @Test
    fun `end to end search sets the pin which the widget then renders with its radius row`() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                var pinLatitude by remember { mutableStateOf<Double?>(null) }
                var pinLongitude by remember { mutableStateOf<Double?>(null) }
                var query by remember { mutableStateOf("") }
                PlaceMapPicker(
                    pinLatitude = pinLatitude,
                    pinLongitude = pinLongitude,
                    onPinChange = { lat, lng ->
                        pinLatitude = lat
                        pinLongitude = lng
                    },
                    radiusMeters = 150f,
                    onRadiusChange = {},
                    minRadiusMeters = 50f,
                    maxRadiusMeters = 1000f,
                    defaultRadiusMeters = 150f,
                    userAgent = TEST_USER_AGENT,
                    searchQuery = query,
                    onSearchQueryChange = { query = it },
                    onSearch = {
                        pinLatitude = 51.5
                        pinLongitude = -0.12
                    }
                )
            }
        }

        composeTestRule.onNodeWithText("Tap the map to drop a pin").assertExists()

        composeTestRule.onNodeWithTag("${TEST_TAG}_search").performTextInput("London")
        composeTestRule.onNodeWithTag("${TEST_TAG}_search").performImeAction()

        composeTestRule.onNodeWithText("Tap the map to drop a pin").assertDoesNotExist()
        composeTestRule.onNodeWithTag("${TEST_TAG}_radius").assertExists()
    }

    // ---- Current location, saved places and chip accessibility tests (164-03 Task 2) ----

    @Test
    fun `onUseCurrentLocation null hides the current-location button`() {
        setPlaceMapPickerContent(pinLatitude = null, pinLongitude = null, onUseCurrentLocation = null)
        composeTestRule.onNodeWithText("Use current location").assertDoesNotExist()
    }

    @Test
    fun `onUseCurrentLocation non-null renders the button and a click invokes it exactly once`() {
        var count = 0
        setPlaceMapPickerContent(pinLatitude = null, pinLongitude = null, onUseCurrentLocation = { count++ })

        composeTestRule.onNodeWithText("Use current location").assertExists()
        composeTestRule.onNodeWithContentDescription("Use current location").assertExists()
        composeTestRule.onNodeWithContentDescription("Use current location").performClick()

        assertEquals(1, count)
    }

    @Test
    fun `isResolving true disables the current-location button and a click does not invoke it`() {
        var count = 0
        setPlaceMapPickerContent(
            pinLatitude = null,
            pinLongitude = null,
            onUseCurrentLocation = { count++ },
            isResolving = true
        )

        composeTestRule.onNodeWithContentDescription("Use current location").assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription("Use current location").performClick()

        assertEquals(0, count)
    }

    @Test
    fun `busy policy keeps saved-place chips and drop-pin-at-center enabled while isResolving`() {
        val selected = mutableListOf<SavedPlaceUiModel>()
        val dropped = mutableListOf<Pair<Double, Double>>()
        val work = SavedPlaceUiModel("Work", 3.0, 4.0, 200f)
        composeTestRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                PlaceMapPicker(
                    pinLatitude = null,
                    pinLongitude = null,
                    onPinChange = { lat, lng -> dropped.add(lat to lng) },
                    radiusMeters = 150f,
                    onRadiusChange = {},
                    minRadiusMeters = 50f,
                    maxRadiusMeters = 1000f,
                    defaultRadiusMeters = 150f,
                    userAgent = TEST_USER_AGENT,
                    savedPlaces = listOf(SavedPlaceUiModel("Home", 1.0, 2.0, 150f), work),
                    onSavedPlaceSelected = { selected.add(it) },
                    isResolving = true
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Saved places Work").assertIsEnabled()
        composeTestRule.onNodeWithContentDescription("Saved places Work").performClick()
        assertEquals(listOf(work), selected)

        invokeDropPinAtCenter("${TEST_TAG}_map_placeholder")
        assertEquals(listOf(0.0 to 0.0), dropped)
    }

    @Test
    fun `currentLocationErrorText renders only when non-null`() {
        setPlaceMapPickerContent(
            pinLatitude = null,
            pinLongitude = null,
            onUseCurrentLocation = {},
            currentLocationErrorText = "Couldn't get your current location"
        )
        composeTestRule.onNodeWithText("Couldn't get your current location").assertExists()
    }

    @Test
    fun `currentLocationErrorText null renders no error text`() {
        setPlaceMapPickerContent(
            pinLatitude = null,
            pinLongitude = null,
            onUseCurrentLocation = {},
            currentLocationErrorText = null
        )
        composeTestRule.onNodeWithText("Couldn't get your current location").assertDoesNotExist()
    }

    @Test
    fun `or text exists only when both current location and search are present`() {
        setPlaceMapPickerContent(pinLatitude = null, pinLongitude = null, onUseCurrentLocation = {}, onSearch = {})
        composeTestRule.onNodeWithText("or").assertExists()
    }

    @Test
    fun `or text does not exist with only current location`() {
        setPlaceMapPickerContent(pinLatitude = null, pinLongitude = null, onUseCurrentLocation = {}, onSearch = null)
        composeTestRule.onNodeWithText("or").assertDoesNotExist()
    }

    @Test
    fun `or text does not exist with only search`() {
        setPlaceMapPickerContent(pinLatitude = null, pinLongitude = null, onUseCurrentLocation = null, onSearch = {})
        composeTestRule.onNodeWithText("or").assertDoesNotExist()
    }

    @Test
    fun `or text does not exist with neither source`() {
        setPlaceMapPickerContent(
            pinLatitude = null,
            pinLongitude = null,
            onUseCurrentLocation = null,
            onSearch = null
        )
        composeTestRule.onNodeWithText("or").assertDoesNotExist()
    }

    @Test
    fun `savedPlaces empty renders no Saved places section`() {
        setPlaceMapPickerContent(pinLatitude = null, pinLongitude = null, savedPlaces = emptyList())
        composeTestRule.onNodeWithText("Saved places").assertDoesNotExist()
    }

    @Test
    fun `savedPlaces with one unique-labelled entry renders its content description`() {
        setPlaceMapPickerContent(
            pinLatitude = null,
            pinLongitude = null,
            savedPlaces = listOf(SavedPlaceUiModel("Home", 1.0, 2.0, 150f))
        )
        composeTestRule.onNodeWithContentDescription("Saved places Home").assertExists()
    }

    @Test
    fun `savedPlaces with three unique-labelled entries renders all three content descriptions`() {
        setPlaceMapPickerContent(
            pinLatitude = null,
            pinLongitude = null,
            savedPlaces = listOf(
                SavedPlaceUiModel("Home", 1.0, 2.0, 150f),
                SavedPlaceUiModel("Work", 3.0, 4.0, 200f),
                SavedPlaceUiModel("Gym", 5.0, 6.0, 250f)
            )
        )
        composeTestRule.onNodeWithContentDescription("Saved places Home").assertExists()
        composeTestRule.onNodeWithContentDescription("Saved places Work").assertExists()
        composeTestRule.onNodeWithContentDescription("Saved places Gym").assertExists()
    }

    @Test
    fun `a saved-place chip exposes exactly one clickable node and both action paths invoke onSavedPlaceSelected once`() {
        val selected = mutableListOf<SavedPlaceUiModel>()
        val work = SavedPlaceUiModel("Work", 3.0, 4.0, 200f)
        setPlaceMapPickerContent(
            pinLatitude = null,
            pinLongitude = null,
            savedPlaces = listOf(SavedPlaceUiModel("Home", 1.0, 2.0, 150f), work),
            onSavedPlaceSelected = { selected.add(it) }
        )

        composeTestRule.onAllNodesWithContentDescription("Saved places Work").assertCountEquals(1)
        val node = composeTestRule.onNodeWithContentDescription("Saved places Work")
        node.assert(SemanticsMatcher.keyIsDefined(SemanticsActions.OnClick))

        node.performSemanticsAction(SemanticsActions.OnClick)
        assertEquals(listOf(work), selected)

        selected.clear()
        node.performClick()
        assertEquals(listOf(work), selected)
    }

    @Test
    fun `duplicate labels are disambiguated with radius and ordinal, and no ambiguous node remains`() {
        val selected = mutableListOf<SavedPlaceUiModel>()
        val second = SavedPlaceUiModel("Home", 5.0, 6.0, 300f)
        setPlaceMapPickerContent(
            pinLatitude = null,
            pinLongitude = null,
            savedPlaces = listOf(SavedPlaceUiModel("Home", 1.0, 2.0, 150f), second),
            onSavedPlaceSelected = { selected.add(it) }
        )

        composeTestRule.onNodeWithContentDescription("Saved places Home, 150 m radius, 1 of 2").assertExists()
        composeTestRule.onNodeWithContentDescription("Saved places Home, 300 m radius, 2 of 2").assertExists()
        composeTestRule.onAllNodesWithContentDescription("Saved places Home").assertCountEquals(0)
        composeTestRule.onNodeWithText("150 m, 1 of 2").assertExists()
        composeTestRule.onNodeWithText("300 m, 2 of 2").assertExists()

        composeTestRule.onNodeWithContentDescription("Saved places Home, 300 m radius, 2 of 2").performClick()
        assertEquals(listOf(second), selected)
    }

    @Test
    fun `ordinal follows the caller's list order, never reordered by the hub`() {
        val first = SavedPlaceUiModel("Home", 5.0, 6.0, 300f)
        val second = SavedPlaceUiModel("Home", 1.0, 2.0, 150f)
        setPlaceMapPickerContent(
            pinLatitude = null,
            pinLongitude = null,
            savedPlaces = listOf(first, second)
        )

        composeTestRule.onNodeWithContentDescription("Saved places Home, 300 m radius, 1 of 2").assertExists()
        composeTestRule.onNodeWithContentDescription("Saved places Home, 150 m radius, 2 of 2").assertExists()
    }

    @Test
    fun `identical duplicate entries render two distinct chip nodes without an exception`() {
        val gym = SavedPlaceUiModel("Gym", 7.0, 8.0, 150f)
        setPlaceMapPickerContent(pinLatitude = null, pinLongitude = null, savedPlaces = listOf(gym, gym))

        composeTestRule.onNodeWithContentDescription("Saved places Gym, 150 m radius, 1 of 2").assertExists()
        composeTestRule.onNodeWithContentDescription("Saved places Gym, 150 m radius, 2 of 2").assertExists()
    }

    @Test
    fun `saved-place chip texts never render a coordinate`() {
        setPlaceMapPickerContent(
            pinLatitude = null,
            pinLongitude = null,
            savedPlaces = listOf(SavedPlaceUiModel("Office", 37.4, -122.1, 150f))
        )

        composeTestRule.onAllNodesWithText("37.4", substring = true).assertCountEquals(0)
        composeTestRule.onAllNodesWithText("122.1", substring = true).assertCountEquals(0)
    }

    @Test
    fun `PresetChip's additive contentDescription overload puts the label on the single clickable node`() {
        var clicks = 0
        composeTestRule.setContent {
            PresetChip(label = "30 min", onClick = { clicks++ }, contentDescription = "Snooze thirty minutes")
        }

        composeTestRule.onAllNodesWithContentDescription("Snooze thirty minutes").assertCountEquals(1)
        composeTestRule.onNodeWithContentDescription("Snooze thirty minutes")
            .assert(SemanticsMatcher.keyIsDefined(SemanticsActions.OnClick))
        composeTestRule.onNodeWithContentDescription("Snooze thirty minutes").performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun `PresetChip's legacy overload still renders the label text unchanged`() {
        composeTestRule.setContent {
            PresetChip(label = "30 min", onClick = {})
        }
        composeTestRule.onNodeWithText("30 min").assertExists()
    }

    @Test
    fun `all sources render in the documented visual order`() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                PlaceMapPicker(
                    pinLatitude = 40.0,
                    pinLongitude = -74.0,
                    onPinChange = { _, _ -> },
                    radiusMeters = 150f,
                    onRadiusChange = {},
                    minRadiusMeters = 50f,
                    maxRadiusMeters = 1000f,
                    defaultRadiusMeters = 150f,
                    userAgent = TEST_USER_AGENT,
                    onUseCurrentLocation = {},
                    searchQuery = "",
                    onSearchQueryChange = {},
                    onSearch = {},
                    savedPlaces = listOf(SavedPlaceUiModel("Home", 1.0, 2.0, 150f))
                )
            }
        }

        val currentLocationTop = composeTestRule.onNodeWithTag("${TEST_TAG}_current_location")
            .getUnclippedBoundsInRoot().top
        val searchTop = composeTestRule.onNodeWithTag("${TEST_TAG}_search").getUnclippedBoundsInRoot().top
        val savedPlacesTop = composeTestRule.onNodeWithTag("${TEST_TAG}_saved_places").getUnclippedBoundsInRoot().top
        val mapTop = composeTestRule.onNodeWithTag("${TEST_TAG}_map_placeholder").getUnclippedBoundsInRoot().top
        val radiusTop = composeTestRule.onNodeWithTag("${TEST_TAG}_radius").getUnclippedBoundsInRoot().top

        assertTrue("current location must be above search", currentLocationTop < searchTop)
        assertTrue("search must be above saved places", searchTop < savedPlacesTop)
        assertTrue("saved places must be above the map", savedPlacesTop < mapTop)
        assertTrue("the map must be above the radius row", mapTop < radiusTop)
    }

    @Test
    fun `at fontScale 2 every saved-place chip stays within the widget's width`() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                val baseDensity = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(baseDensity.density, fontScale = 2f)) {
                    Box(modifier = Modifier.width(320.dp)) {
                        PlaceMapPicker(
                            pinLatitude = null,
                            pinLongitude = null,
                            onPinChange = { _, _ -> },
                            radiusMeters = 150f,
                            onRadiusChange = {},
                            minRadiusMeters = 50f,
                            maxRadiusMeters = 1000f,
                            defaultRadiusMeters = 150f,
                            userAgent = TEST_USER_AGENT,
                            savedPlaces = listOf(
                                SavedPlaceUiModel("Home", 1.0, 2.0, 150f),
                                SavedPlaceUiModel("Work", 3.0, 4.0, 200f),
                                SavedPlaceUiModel("Gym", 5.0, 6.0, 250f),
                                SavedPlaceUiModel("School", 7.0, 8.0, 300f),
                                SavedPlaceUiModel("Library", 9.0, 10.0, 350f)
                            )
                        )
                    }
                }
            }
        }

        val widgetRight = composeTestRule.onNodeWithTag(TEST_TAG).getUnclippedBoundsInRoot().right
        val chipNodes = composeTestRule.onAllNodes(hasContentDescription("Saved places", substring = true))
        val chipCount = chipNodes.fetchSemanticsNodes().size
        assertTrue("expected at least one saved-place chip node", chipCount > 0)
        for (index in 0 until chipCount) {
            val chipRight = chipNodes[index].getUnclippedBoundsInRoot().right
            assertTrue(
                "chip $index right edge ($chipRight) must stay within the widget's right edge ($widgetRight)",
                chipRight <= widgetRight
            )
        }
    }

    @Test
    fun `PlaceMapPicker source never imports or calls device location, geocoding or SharedPreferences APIs`() {
        val source = strippedPlaceMapPickerSource()
        assertFalse(source.contains("android.location"))
        assertFalse(source.contains("Geocoder"))
        assertFalse(source.contains("ACCESS_FINE_LOCATION"))
        assertFalse(source.contains("getSharedPreferences"))
    }

    // ---- Model tests: canSubmitSearch ----

    @Test
    fun `canSubmitSearch requires a non-blank query and no lookup in flight`() {
        assertTrue(canSubmitSearch("42 Main St", false))
        assertFalse(canSubmitSearch("", false))
        assertFalse(canSubmitSearch("   ", false))
        assertFalse(canSubmitSearch("42 Main St", true))
    }

    // ---- Model tests: SavedPlaceUiModel helpers (164-03 Task 2) ----

    @Test
    fun `occurrenceIndices assigns a 0-based occurrence within each equal-value group`() {
        assertEquals(listOf(0, 0, 1, 2), occurrenceIndices(listOf("a", "b", "a", "a")))
    }

    @Test
    fun `savedPlaceChipLabels disambiguates duplicate labels with radius and ordinal`() {
        val result = savedPlaceChipLabels(
            listOf("Home", "Work", "Home"),
            listOf("150 m", "300 m", "500 m")
        )
        assertEquals(
            listOf(
                "Saved places Home, 150 m radius, 1 of 2",
                "Saved places Work",
                "Saved places Home, 500 m radius, 2 of 2"
            ),
            result.map { it.contentDescription }
        )
        assertEquals(
            listOf("150 m, 1 of 2", null, "500 m, 2 of 2"),
            result.map { it.supportingLabel }
        )
    }

    @Test
    fun `savedPlaceChipLabels throws for mismatched list sizes`() {
        assertThrows(IllegalArgumentException::class.java) {
            savedPlaceChipLabels(listOf("Home"), listOf("150 m", "300 m"))
        }
    }

    @Test
    fun `SavedPlaceUiModel has value equality`() {
        assertEquals(
            SavedPlaceUiModel("Home", 1.0, 2.0, 150f),
            SavedPlaceUiModel("Home", 1.0, 2.0, 150f)
        )
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

    // ---- Source-contract tests (164-02 Task 2): touch containment, lifecycle, drag, no logging ----

    @Test
    fun `map gestures are contained via requestDisallowInterceptTouchEvent`() {
        val source = strippedPlaceMapPickerSource()
        assertTrue(source.contains("requestDisallowInterceptTouchEvent(true)"))
        assertTrue(source.contains("requestDisallowInterceptTouchEvent(false)"))
    }

    @Test
    fun `the MapView lifecycle is bound to onResume, onPause and onDetach`() {
        val source = strippedPlaceMapPickerSource()
        assertTrue(source.contains(".onResume()"))
        assertTrue(source.contains(".onPause()"))
        assertTrue(source.contains(".onDetach()"))
    }

    @Test
    fun `both the pin marker and the radius handle are draggable`() {
        val source = strippedPlaceMapPickerSource()
        assertTrue(SourceContractTestSupport.countOccurrences(source, "setDraggable(true)") >= 2)
    }

    @Test
    fun `both the slider and the handle record lastEmittedRadiusMeters`() {
        val source = strippedPlaceMapPickerSource()
        assertTrue(SourceContractTestSupport.countOccurrences(source, "lastEmittedRadiusMeters =") >= 2)
    }

    @Test
    fun `LocalLifecycleOwner is imported only from androidx-lifecycle-compose`() {
        val source = strippedPlaceMapPickerSource()
        assertTrue(source.contains("import androidx.lifecycle.compose.LocalLifecycleOwner"))
        val offendingImports = source.lineSequence()
            .filter { it.trim().endsWith(".LocalLifecycleOwner") }
            .filterNot { it.trim() == "import androidx.lifecycle.compose.LocalLifecycleOwner" }
            .toList()
        assertTrue("Unexpected LocalLifecycleOwner import(s): $offendingImports", offendingImports.isEmpty())
    }

    @Test
    fun `configureOsmdroid runs before the first MapView is constructed`() {
        val source = strippedPlaceMapPickerSource()
        val configureIndex = source.indexOf("configureOsmdroid(")
        // "= MapView(", not bare "MapView(" -- the latter also matches inside the
        // createPlaceMapView( function declaration itself, which always precedes its own body.
        val mapViewIndex = source.indexOf("= MapView(")
        assertTrue("configureOsmdroid( not found", configureIndex >= 0)
        assertTrue("= MapView( not found", mapViewIndex >= 0)
        assertTrue(configureIndex < mapViewIndex)
    }

    @Test
    fun `no logging call or bulk tile download API appears in PlaceMapPicker`() {
        val source = strippedPlaceMapPickerSource()
        assertFalse(source.contains("android.util.Log"))
        assertFalse(source.contains("Log.d("))
        assertFalse(source.contains("Log.i("))
        assertFalse(source.contains("CacheManager"))
    }

    @Test
    fun `PlaceMapOsmdroidConfig never overrides osmdroid's tile expiration`() {
        val source = SourceContractTestSupport.stripComments(
            SourceContractTestSupport.source("PlaceMapOsmdroidConfig.kt")
        )
        assertFalse(source.contains("expirationOverrideDuration"))
        assertFalse(source.contains("setExpirationOverrideDuration"))
    }

    @Test
    fun `clearPinOverlays removes the circle, pin marker and radius handle, and nulls all three`() {
        val source = strippedPlaceMapPickerSource()
        val body = SourceContractTestSupport.functionBody(source, "fun clearPinOverlays(")
        assertTrue(body.contains("circle"))
        assertTrue(body.contains("pinMarker"))
        assertTrue(body.contains("radiusHandle"))
        assertTrue(body.contains("overlays.remove"))
        assertTrue(SourceContractTestSupport.countOccurrences(body, "= null") >= 3)
    }

    @Test
    fun `every onResume and onPause call sits on the same line as its MapLifecycleGate call`() {
        val source = strippedPlaceMapPickerSource()
        val resumeLines = source.lineSequence().filter { it.contains(".onResume()") }.toList()
        val pauseLines = source.lineSequence().filter { it.contains(".onPause()") }.toList()
        assertTrue("No .onResume() call found", resumeLines.isNotEmpty())
        assertTrue("No .onPause() call found", pauseLines.isNotEmpty())
        resumeLines.forEach { line ->
            assertTrue("Line missing lifecycleGate.resume: $line", line.contains("lifecycleGate.resume"))
        }
        pauseLines.forEach { line ->
            assertTrue("Line missing lifecycleGate.pause: $line", line.contains("lifecycleGate.pause"))
        }
    }

    // ---- SourceContractTestSupport.functionBody sanity ----

    @Test
    fun `functionBody extracts one function's full body, string-literal aware`() {
        val result = SourceContractTestSupport.functionBody(FUNCTION_BODY_FIXTURE, "fun a(")
        assertTrue(result.startsWith("fun a("))
        assertTrue(result.endsWith("}"))
        assertTrue(result.contains("b("))
        assertFalse(result.contains("fun c"))
    }

    @Test
    fun `functionBody occurrence selects the matching declaration`() {
        val fixture = "$FUNCTION_BODY_FIXTURE\nfun a(x: () -> Unit = {}) {\n    b(\"second\")\n}\n"
        val result = SourceContractTestSupport.functionBody(fixture, "fun a(", occurrence = 2)
        assertTrue(result.contains("second"))
    }

    @Test
    fun `functionBody throws for a missing declaration`() {
        assertThrows(IllegalStateException::class.java) {
            SourceContractTestSupport.functionBody(FUNCTION_BODY_FIXTURE, "fun missing(")
        }
    }

    @Test
    fun `functionBody throws for an expression-bodied function`() {
        assertThrows(IllegalStateException::class.java) {
            SourceContractTestSupport.functionBody("fun a() = 1", "fun a(")
        }
    }

    @Test
    fun `functionBody throws when the body braces never balance`() {
        assertThrows(IllegalStateException::class.java) {
            SourceContractTestSupport.functionBody("fun a() {\n    b()\n", "fun a(")
        }
    }

    private fun strippedPlaceMapPickerSource(): String =
        SourceContractTestSupport.stripComments(SourceContractTestSupport.source("PlaceMapPicker.kt"))

    // ---- Helpers ----

    private fun setPlaceMapPickerContent(
        pinLatitude: Double?,
        pinLongitude: Double?,
        radiusMeters: Float = 150f,
        searchQuery: String = "",
        onSearch: ((query: String) -> Unit)? = null,
        searchErrorText: String? = null,
        isResolving: Boolean = false,
        onUseCurrentLocation: (() -> Unit)? = null,
        currentLocationErrorText: String? = null,
        savedPlaces: List<SavedPlaceUiModel> = emptyList(),
        onSavedPlaceSelected: (SavedPlaceUiModel) -> Unit = {}
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
                    radiusStepMeters = 50f,
                    onUseCurrentLocation = onUseCurrentLocation,
                    currentLocationErrorText = currentLocationErrorText,
                    searchQuery = searchQuery,
                    onSearchQueryChange = {},
                    onSearch = onSearch,
                    searchErrorText = searchErrorText,
                    savedPlaces = savedPlaces,
                    onSavedPlaceSelected = onSavedPlaceSelected,
                    isResolving = isResolving
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
