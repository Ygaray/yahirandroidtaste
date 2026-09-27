package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone

/**
 * Tests for [DateTimePicker] — the HUBW-01 LocalDate/LocalTime-contract date and time picker.
 * Infra mirrors this module's established Robolectric+Compose harness ([AppChipTest],
 * [PickerExpansionTest]): `@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [35])`,
 * `createComposeRule()`, no theme wrapper.
 *
 * Locale/timezone are pinned in [setUp] and restored in [tearDown] so this class never leaks
 * global JVM state across the suite.
 *
 * The two real-interaction tests (swapped callbacks) drive the actual Material3 `TimePicker` /
 * `DatePicker` composables via [performSemanticsAction] against [SemanticsActions.OnClick] — the
 * material3:1.4.0 clock-face content descriptions (`m3c_time_picker_hour_suffix` /
 * `m3c_time_picker_minute_suffix`, confirmed against the resolved AAR's `values.xml`) are
 * `"%1$d o'clock"` / `"%1$d minutes"`.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DateTimePickerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var originalLocale: Locale
    private lateinit var originalTimeZone: TimeZone

    @Before
    fun setUp() {
        originalLocale = Locale.getDefault()
        originalTimeZone = TimeZone.getDefault()
        Locale.setDefault(Locale.US)
    }

    @After
    fun tearDown() {
        Locale.setDefault(originalLocale)
        TimeZone.setDefault(originalTimeZone)
    }

    // ---- utcStartOfDayMillisToLocalDate / toUtcStartOfDayMillis: no day-shift round trip ----

    @Test
    fun `utcStartOfDayMillisToLocalDate round trips without a day shift across timezones`() {
        val dates = listOf(LocalDate.of(2026, 9, 15), LocalDate.of(2026, 3, 8))
        val zoneIds = listOf("America/Los_Angeles", "Pacific/Kiritimati", "UTC")

        zoneIds.forEach { zoneId ->
            TimeZone.setDefault(TimeZone.getTimeZone(zoneId))
            dates.forEach { date ->
                val millis = date.toUtcStartOfDayMillis()
                assertEquals(
                    "utcStartOfDayMillisToLocalDate must round-trip $date under zone $zoneId " +
                        "without shifting by a day",
                    date,
                    utcStartOfDayMillisToLocalDate(millis)
                )
            }
        }
    }

    // ---- minDateSelectableDates ----

    @Test
    fun `minDateSelectableDates rejects a day before minDate, accepts minDate and later, and gates the year`() {
        val minDate = LocalDate.of(2026, 9, 14)
        val selectable = minDateSelectableDates(minDate)

        assertFalse(
            "A day before minDate must not be selectable",
            selectable.isSelectableDate(LocalDate.of(2026, 9, 13).toUtcStartOfDayMillis())
        )
        assertTrue(
            "minDate itself must be selectable",
            selectable.isSelectableDate(minDate.toUtcStartOfDayMillis())
        )
        assertTrue(
            "A day after minDate must be selectable",
            selectable.isSelectableDate(LocalDate.of(2026, 9, 15).toUtcStartOfDayMillis())
        )
        assertFalse("A year before minDate's year must be rejected", selectable.isSelectableYear(2025))
        assertTrue("minDate's own year must be accepted", selectable.isSelectableYear(2026))
    }

    @Test
    fun `a null minDate accepts every date and year`() {
        val selectable = minDateSelectableDates(null)

        assertTrue(selectable.isSelectableDate(LocalDate.of(2000, 1, 1).toUtcStartOfDayMillis()))
        assertTrue(selectable.isSelectableYear(1900))
    }

    // ---- emitPickedDate ----

    @Test
    fun `emitPickedDate returns false and never invokes the callback for null millis`() {
        var calls = 0
        val result = emitPickedDate(null, LocalDate.of(2026, 9, 15)) { calls++ }

        assertFalse(result)
        assertEquals(0, calls)
    }

    @Test
    fun `emitPickedDate returns false for a millis equal to the current date`() {
        var calls = 0
        val current = LocalDate.of(2026, 9, 15)
        val result = emitPickedDate(current.toUtcStartOfDayMillis(), current) { calls++ }

        assertFalse(result)
        assertEquals(0, calls)
    }

    @Test
    fun `emitPickedDate returns true and invokes the callback once for a different day`() {
        var picked: LocalDate? = null
        var calls = 0
        val current = LocalDate.of(2026, 9, 15)
        val newDate = LocalDate.of(2026, 9, 16)
        val result = emitPickedDate(newDate.toUtcStartOfDayMillis(), current) { picked = it; calls++ }

        assertTrue(result)
        assertEquals(1, calls)
        assertEquals(newDate, picked)
    }

    // ---- emitPickedTime ----

    @Test
    fun `emitPickedTime returns false for an hour and minute equal to the current time`() {
        var calls = 0
        val result = emitPickedTime(20, 0, LocalTime.of(20, 0)) { calls++ }

        assertFalse(result)
        assertEquals(0, calls)
    }

    @Test
    fun `emitPickedTime returns true and invokes the callback with the new time`() {
        var picked: LocalTime? = null
        var calls = 0
        val result = emitPickedTime(8, 15, LocalTime.of(20, 0)) { picked = it; calls++ }

        assertTrue(result)
        assertEquals(1, calls)
        assertEquals(LocalTime.of(8, 15), picked)
    }

    // ---- formatPickerTime: explicit patterns, locale-independent of the clock convention ----

    @Test
    fun `formatPickerTime uses explicit 12-hour and 24-hour patterns regardless of the locale's own clock convention`() {
        val time = LocalTime.of(20, 0)

        listOf(Locale.US, Locale.GERMANY).forEach { locale ->
            val twelveHour = formatPickerTime(time, is24Hour = false, locale = locale)
            val twentyFourHour = formatPickerTime(time, is24Hour = true, locale = locale)

            assertEquals(
                "is24Hour = false must match the explicit h:mm a pattern under $locale",
                DateTimeFormatter.ofPattern("h:mm a", locale).format(time),
                twelveHour
            )
            assertEquals(
                "is24Hour = true must match the explicit HH:mm pattern under $locale",
                DateTimeFormatter.ofPattern("HH:mm", locale).format(time),
                twentyFourHour
            )
            assertTrue(
                "is24Hour = false and true must render differently under $locale",
                twelveHour != twentyFourHour
            )
        }
    }

    // ---- Rendered field text ----

    @Test
    fun `rendered date and time fields show the formatted date and 12-hour time under Locale-US`() {
        Locale.setDefault(Locale.US)
        composeTestRule.setContent {
            DateTimePicker(
                selectedDate = LocalDate.of(2026, 9, 15),
                onDateSelected = {},
                selectedTime = LocalTime.of(20, 0),
                onTimeSelected = {},
                is24Hour = false
            )
        }
        composeTestRule.waitForIdle()

        val expectedDate = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.US)
            .format(LocalDate.of(2026, 9, 15))
        val expectedTime = formatPickerTime(LocalTime.of(20, 0), is24Hour = false, locale = Locale.US)

        composeTestRule.onNodeWithText(expectedDate).assertExists()
        composeTestRule.onNodeWithText(expectedTime).assertExists()
    }

    @Test
    fun `rendered time field shows the German 12-hour form when is24Hour is false under Locale-GERMANY`() {
        Locale.setDefault(Locale.GERMANY)
        composeTestRule.setContent {
            DateTimePicker(
                selectedDate = LocalDate.of(2026, 9, 15),
                onDateSelected = {},
                selectedTime = LocalTime.of(20, 0),
                onTimeSelected = {},
                is24Hour = false
            )
        }
        composeTestRule.waitForIdle()

        val expectedTime = formatPickerTime(LocalTime.of(20, 0), is24Hour = false, locale = Locale.GERMANY)
        composeTestRule.onNodeWithText(expectedTime).assertExists()
    }

    @Test
    fun `rendered time field shows the 24-hour form when is24Hour is true under Locale-US`() {
        Locale.setDefault(Locale.US)
        composeTestRule.setContent {
            DateTimePicker(
                selectedDate = LocalDate.of(2026, 9, 15),
                onDateSelected = {},
                selectedTime = LocalTime.of(20, 0),
                onTimeSelected = {},
                is24Hour = true
            )
        }
        composeTestRule.waitForIdle()

        val expectedTime = formatPickerTime(LocalTime.of(20, 0), is24Hour = true, locale = Locale.US)
        composeTestRule.onNodeWithText(expectedTime).assertExists()
    }

    @Test
    fun `null selectedDate and selectedTime render placeholder text`() {
        composeTestRule.setContent {
            DateTimePicker(
                selectedDate = null,
                onDateSelected = {},
                selectedTime = null,
                onTimeSelected = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Pick a date").assertExists()
        composeTestRule.onNodeWithText("Pick a time").assertExists()
    }

    // ---- Panel toggling / mutual exclusivity ----

    @Test
    fun `tapping the date field shows the date panel, tapping again hides it`() {
        composeTestRule.setContent {
            DateTimePicker(
                selectedDate = LocalDate.of(2026, 9, 15),
                onDateSelected = {},
                selectedTime = LocalTime.of(20, 0),
                onTimeSelected = {},
                testTag = "dtp"
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("dtp_date_panel").assertDoesNotExist()

        composeTestRule.onNodeWithTag("dtp_date").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("dtp_date_panel").assertExists()

        composeTestRule.onNodeWithTag("dtp_date").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("dtp_date_panel").assertDoesNotExist()
    }

    @Test
    fun `tapping the time field shows the time panel and collapses an open date panel`() {
        composeTestRule.setContent {
            DateTimePicker(
                selectedDate = LocalDate.of(2026, 9, 15),
                onDateSelected = {},
                selectedTime = LocalTime.of(20, 0),
                onTimeSelected = {},
                testTag = "dtp"
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("dtp_date").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("dtp_date_panel").assertExists()

        composeTestRule.onNodeWithTag("dtp_time").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("dtp_time_panel").assertExists()
        composeTestRule.onNodeWithTag("dtp_date_panel").assertDoesNotExist()
    }

    @Test
    fun `enabled false - tapping either field shows no panel`() {
        composeTestRule.setContent {
            DateTimePicker(
                selectedDate = LocalDate.of(2026, 9, 15),
                onDateSelected = {},
                selectedTime = LocalTime.of(20, 0),
                onTimeSelected = {},
                enabled = false,
                testTag = "dtp"
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("dtp_date").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("dtp_date_panel").assertDoesNotExist()

        composeTestRule.onNodeWithTag("dtp_time").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("dtp_time_panel").assertDoesNotExist()
    }

    @Test
    fun `a panel collapses when its field is hidden and does not reopen when the field returns`() {
        // The toggle control is placed BEFORE DateTimePicker in this host: once the date panel
        // (a real, tall Material3 DatePicker) is open, a control placed AFTER it in an
        // unscrolled Column can fall outside the Robolectric root's dispatchable bounds and
        // silently fail to receive the click. Placing durable test controls above the picker
        // sidesteps that Robolectric+Compose measurement pitfall.
        composeTestRule.setContent {
            var showDate by remember { mutableStateOf(true) }
            Column {
                TextButton(onClick = { showDate = !showDate }) {
                    Text("Toggle showDate")
                }
                DateTimePicker(
                    selectedDate = LocalDate.of(2026, 9, 15),
                    onDateSelected = {},
                    selectedTime = LocalTime.of(20, 0),
                    onTimeSelected = {},
                    showDate = showDate,
                    showTime = true,
                    testTag = "dtp"
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("dtp_date").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("dtp_date_panel").assertExists()

        composeTestRule.onNodeWithText("Toggle showDate").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("dtp_date").assertDoesNotExist()
        composeTestRule.onNodeWithTag("dtp_date_panel").assertDoesNotExist()

        composeTestRule.onNodeWithText("Toggle showDate").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("dtp_date").assertExists()
        composeTestRule.onNodeWithTag("dtp_date_panel").assertDoesNotExist()
    }

    @Test
    fun `showDate false and showTime false renders no node with the testTag`() {
        composeTestRule.setContent {
            DateTimePicker(
                selectedDate = null,
                onDateSelected = {},
                selectedTime = null,
                onTimeSelected = {},
                showDate = false,
                showTime = false,
                testTag = "dtp_empty"
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("dtp_empty").assertDoesNotExist()
    }

    // ---- Real interaction: proves the panel always reports through the LATEST callback ----

    @Test
    fun `a swapped onTimeSelected callback receives the real clock-face pick, never the stale one`() {
        val first = mutableListOf<LocalTime>()
        val second = mutableListOf<LocalTime>()

        composeTestRule.setContent {
            var callback by remember { mutableStateOf<(LocalTime) -> Unit>({ first.add(it) }) }
            var selectedTime by remember { mutableStateOf(LocalTime.of(9, 0)) }

            Column {
                TextButton(onClick = { callback = { second.add(it) } }) {
                    Text("Swap")
                }
                DateTimePicker(
                    selectedDate = null,
                    onDateSelected = {},
                    selectedTime = selectedTime,
                    onTimeSelected = { time ->
                        callback(time)
                        selectedTime = time
                    },
                    showDate = false,
                    is24Hour = false,
                    testTag = "dtp"
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("dtp_time").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Swap").performClick()
        composeTestRule.waitForIdle()

        val hourNode = composeTestRule.onNode(
            hasContentDescription("8 o'clock") and hasAnyAncestor(hasTestTag("dtp_time_panel"))
        )
        hourNode.performSemanticsAction(SemanticsActions.OnClick)
        composeTestRule.waitForIdle()

        // material3:1.4.0's clock face does not auto-advance from hour to minute selection on an
        // hour tap (confirmed by decompiling the resolved AAR's rendered content-description set
        // in this session) — the dial stays in hour mode until the "Select minutes" field is
        // tapped, mirroring the native TimePickerDialog's hour/minute field-focus convention.
        val selectMinutesNode = composeTestRule.onNode(
            hasContentDescription("Select minutes") and hasAnyAncestor(hasTestTag("dtp_time_panel"))
        )
        selectMinutesNode.performSemanticsAction(SemanticsActions.OnClick)
        composeTestRule.waitForIdle()

        val minuteNode = composeTestRule.onNode(
            hasContentDescription("15 minutes") and hasAnyAncestor(hasTestTag("dtp_time_panel"))
        )
        minuteNode.performSemanticsAction(SemanticsActions.OnClick)
        composeTestRule.waitForIdle()

        assertTrue("The stale (pre-swap) callback must never fire", first.isEmpty())
        assertEquals(LocalTime.of(8, 15), second.last())
    }

    @Test
    fun `a swapped onDateSelected callback receives the real day-cell pick and collapses the panel`() {
        val first = mutableListOf<LocalDate>()
        val second = mutableListOf<LocalDate>()

        composeTestRule.setContent {
            var callback by remember { mutableStateOf<(LocalDate) -> Unit>({ first.add(it) }) }
            var selectedDate by remember { mutableStateOf(LocalDate.of(2026, 9, 15)) }

            Column {
                TextButton(onClick = { callback = { second.add(it) } }) {
                    Text("Swap")
                }
                DateTimePicker(
                    selectedDate = selectedDate,
                    onDateSelected = { date ->
                        callback(date)
                        selectedDate = date
                    },
                    selectedTime = null,
                    onTimeSelected = {},
                    showTime = false,
                    minDate = null,
                    testTag = "dtp"
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("dtp_date").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Swap").performClick()
        composeTestRule.waitForIdle()

        // material3:1.4.0's day cells expose their FULL accessible date string (e.g. "Wednesday,
        // September 16, 2026") as their Text semantics, not the bare visible digit "16"
        // (confirmed by decompiling the resolved AAR's rendered node set in this session) — so
        // this locates the cell by a substring match instead of an exact "16".
        val dayCell = composeTestRule.onNode(
            hasText("16", substring = true) and hasClickAction() and hasAnyAncestor(hasTestTag("dtp_date_panel"))
        )
        dayCell.performSemanticsAction(SemanticsActions.OnClick)
        composeTestRule.waitForIdle()

        assertTrue("The stale (pre-swap) callback must never fire", first.isEmpty())
        assertEquals(LocalDate.of(2026, 9, 16), second.last())
        composeTestRule.onNodeWithTag("dtp_date_panel").assertDoesNotExist()
    }
}
