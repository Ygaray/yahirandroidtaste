package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.theme.Dimens
import kotlinx.coroutines.flow.drop
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * HUBW-01: the reusable date and time picker. Value contract is `java.time.LocalDate` /
 * `java.time.LocalTime` in and out only -- no app type, no Material3 picker-state type, ever
 * appears in this public signature. See this file's own [DateTimePickerTest] for the full
 * behavior contract.
 *
 * Renders nothing (reserves no space) when neither [showDate] nor [showTime] is true. Within one
 * `DateTimePicker` instance only one panel (date or time) is expanded at a time; a panel whose
 * field stops being shown collapses rather than silently reopening when the field returns. Panel
 * exclusivity is scoped to this instance -- a consumer that needs date/time panel exclusivity
 * across two logically-related pickers renders both fields from one `DateTimePicker` and toggles
 * [showDate] / [showTime] (review round 2 MEDIUM, Plan 05's editor does exactly this).
 *
 * An open panel resyncs to [selectedDate] / [selectedTime] if the caller changes either value out
 * from under it (e.g. a Quick-pick chip elsewhere on the same screen while the calendar/clock
 * stays open) -- the panel's Material3 picker state is rekeyed on that value, not created once
 * and left stale (review 163 WR-01).
 *
 * @param selectedDate The currently selected date, or `null` for "not yet picked".
 * @param onDateSelected Invoked with a real user pick, always through the most recently passed
 *   lambda (never a stale one captured at panel-open time -- both the current value and the
 *   current callback are read via [rememberUpdatedState] inside the date panel's collector).
 * @param selectedTime The currently selected time, or `null` for "not yet picked".
 * @param onTimeSelected Invoked with a real user pick, same freshness guarantee as
 *   [onDateSelected].
 * @param modifier Applied to the outer [Column].
 * @param showDate Whether the date field (and its panel) renders at all.
 * @param showTime Whether the time field (and its panel) renders at all.
 * @param minDate Optional lower bound (inclusive); dates before it are not selectable in the date
 *   panel. `null` allows every date.
 * @param is24Hour Explicit 12/24-hour override for the time field's display and the time panel's
 *   dial. `null` resolves the platform's own setting via `DateFormat.is24HourFormat`.
 * @param enabled When `false`, both fields ignore taps (no panel ever opens) and render dimmed.
 * @param testTag Root testTag; the date/time fields and panels derive `"${testTag}_date"`,
 *   `"${testTag}_date_panel"`, `"${testTag}_time"`, `"${testTag}_time_panel"`.
 */
@Composable
fun DateTimePicker(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    selectedTime: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit,
    modifier: Modifier = Modifier,
    showDate: Boolean = true,
    showTime: Boolean = true,
    minDate: LocalDate? = null,
    is24Hour: Boolean? = null,
    enabled: Boolean = true,
    testTag: String = "date_time_picker"
) {
    if (!showDate && !showTime) return

    var expandedPanel by rememberSaveable { mutableStateOf(DateTimePickerPanel.NONE) }
    LaunchedEffect(showDate, showTime) {
        val dateHidden = expandedPanel == DateTimePickerPanel.DATE && !showDate
        val timeHidden = expandedPanel == DateTimePickerPanel.TIME && !showTime
        if (dateHidden || timeHidden) {
            expandedPanel = DateTimePickerPanel.NONE
        }
    }

    Column(modifier = modifier.fillMaxWidth().testTag(testTag)) {
        if (showDate) {
            DateTimePickerFieldRow(
                testTag = "${testTag}_date",
                label = "Date",
                valueText = selectedDate?.let {
                    DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.getDefault()).format(it)
                } ?: "Pick a date",
                enabled = enabled,
                onClick = {
                    expandedPanel = if (expandedPanel == DateTimePickerPanel.DATE) {
                        DateTimePickerPanel.NONE
                    } else {
                        DateTimePickerPanel.DATE
                    }
                }
            )
            if (expandedPanel == DateTimePickerPanel.DATE) {
                DateTimePickerDatePanel(
                    testTag = "${testTag}_date_panel",
                    selectedDate = selectedDate,
                    minDate = minDate,
                    onDateSelected = onDateSelected,
                    onCollapse = { expandedPanel = DateTimePickerPanel.NONE }
                )
            }
        }
        if (showTime) {
            val resolved24Hour = is24Hour
                ?: android.text.format.DateFormat.is24HourFormat(LocalContext.current)
            DateTimePickerFieldRow(
                testTag = "${testTag}_time",
                label = "Time",
                valueText = selectedTime?.let { formatPickerTime(it, resolved24Hour, Locale.getDefault()) }
                    ?: "Pick a time",
                enabled = enabled,
                onClick = {
                    expandedPanel = if (expandedPanel == DateTimePickerPanel.TIME) {
                        DateTimePickerPanel.NONE
                    } else {
                        DateTimePickerPanel.TIME
                    }
                }
            )
            if (expandedPanel == DateTimePickerPanel.TIME) {
                DateTimePickerTimePanel(
                    testTag = "${testTag}_time_panel",
                    selectedTime = selectedTime,
                    is24Hour = resolved24Hour,
                    onTimeSelected = onTimeSelected,
                    onCollapse = { expandedPanel = DateTimePickerPanel.NONE }
                )
            }
        }
    }
}

private enum class DateTimePickerPanel { NONE, DATE, TIME }

@Composable
private fun DateTimePickerFieldRow(
    testTag: String,
    label: String,
    valueText: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = Dimens.HorizontalPadding)
            .alpha(if (enabled) 1f else 0.38f)
            .testTag(testTag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = valueText, style = MaterialTheme.typography.bodyLarge)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateTimePickerDatePanel(
    testTag: String,
    selectedDate: LocalDate?,
    minDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onCollapse: () -> Unit
) {
    // Re-key on selectedDate (review 163 WR-01): rememberDatePickerState only reads its
    // initial* args on first composition, so without this key an external change to
    // selectedDate while the panel stays open (e.g. a Quick-pick chip tapped elsewhere on the
    // same screen while the calendar is still expanded) never reaches the already-created
    // DatePickerState -- the panel keeps showing a stale selection until the user taps inside
    // it, silently overwriting the external change. key() forces this whole subtree (and its
    // DatePickerState) to be recreated whenever selectedDate changes, so the panel always
    // reflects the latest external value the next time it renders.
    key(selectedDate) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate?.toUtcStartOfDayMillis(),
            initialDisplayedMonthMillis = (selectedDate ?: minDate ?: LocalDate.now()).toUtcStartOfDayMillis(),
            selectableDates = minDateSelectableDates(minDate)
        )
        // Freshness guarantee (T-163-24 / review 163-01 MEDIUM): always read the LATEST value
        // and callback at collection time, never the ones captured when this panel first
        // composed.
        val currentDate by rememberUpdatedState(selectedDate)
        val currentOnDateSelected by rememberUpdatedState(onDateSelected)

        LaunchedEffect(state) {
            snapshotFlow { state.selectedDateMillis }
                .drop(1)
                .collect { millis ->
                    if (emitPickedDate(millis, currentDate, currentOnDateSelected)) {
                        onCollapse()
                    }
                }
        }

        Column(modifier = Modifier.testTag(testTag)) {
            DatePicker(state = state, title = null, headline = null, showModeToggle = false)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateTimePickerTimePanel(
    testTag: String,
    selectedTime: LocalTime?,
    is24Hour: Boolean,
    onTimeSelected: (LocalTime) -> Unit,
    onCollapse: () -> Unit
) {
    // Re-key on selectedTime -- same rationale as the date panel's key(selectedDate) above
    // (review 163 WR-01): without it, an external change to selectedTime while the panel stays
    // open never reaches the already-created TimePickerState.
    key(selectedTime) {
        val state = rememberTimePickerState(
            initialHour = selectedTime?.hour ?: 9,
            initialMinute = selectedTime?.minute ?: 0,
            is24Hour = is24Hour
        )
        // Same freshness guarantee as the date panel above.
        val currentTime by rememberUpdatedState(selectedTime)
        val currentOnTimeSelected by rememberUpdatedState(onTimeSelected)

        LaunchedEffect(state) {
            snapshotFlow { state.hour to state.minute }
                .drop(1)
                .collect { (hour, minute) ->
                    emitPickedTime(hour, minute, currentTime, currentOnTimeSelected)
                }
        }

        Column(modifier = Modifier.testTag(testTag)) {
            TimePicker(state = state)
            TextButton(onClick = onCollapse) {
                Text("Done")
            }
        }
    }
}

// ---- Internal helpers (tested directly by DateTimePickerTest) ----

internal fun LocalDate.toUtcStartOfDayMillis(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

// Day-shift pitfall fix: Material3's DatePicker reports UTC-midnight millis, so this converts
// back through ZoneOffset.UTC (never the JVM default zone) to avoid moving the day backward in
// negative-offset zones.
internal fun utcStartOfDayMillisToLocalDate(utcMillis: Long): LocalDate =
    Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate()

internal fun minDateSelectableDates(minDate: LocalDate?): SelectableDates = object : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        if (minDate == null) return true
        return !utcStartOfDayMillisToLocalDate(utcTimeMillis).isBefore(minDate)
    }

    override fun isSelectableYear(year: Int): Boolean {
        if (minDate == null) return true
        return year >= minDate.year
    }
}

internal fun emitPickedDate(
    utcMillis: Long?,
    current: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
): Boolean {
    if (utcMillis == null) return false
    val picked = utcStartOfDayMillisToLocalDate(utcMillis)
    if (picked == current) return false
    onDateSelected(picked)
    return true
}

internal fun emitPickedTime(
    hour: Int,
    minute: Int,
    current: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit
): Boolean {
    val picked = LocalTime.of(hour, minute)
    if (picked == current) return false
    onTimeSelected(picked)
    return true
}

// Forced-locale-formatter pitfall fix: explicit h:mm a / HH:mm patterns, never a localized SHORT
// formatter -- a localized formatter follows the LOCALE's own clock convention and would render
// 24-hour text under a 24-hour locale even when the caller explicitly asked for 12-hour display.
internal fun formatPickerTime(time: LocalTime, is24Hour: Boolean, locale: Locale): String {
    val pattern = if (is24Hour) "HH:mm" else "h:mm a"
    return DateTimeFormatter.ofPattern(pattern, locale).format(time)
}
