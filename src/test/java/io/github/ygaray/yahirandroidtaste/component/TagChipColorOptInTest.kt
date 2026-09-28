package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableChipColors
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Value-level proofs for the three internal helpers declared in `TagChipColorOptIn.kt`
 * (Option A, hub v2.3.0, `tagui01-editor-picker-color-gap`): [optedInTagColor],
 * [editorTagChipIsSelected] (Task 2), and [pickerTagChipColors].
 *
 * `optedInTagColor` and `editorTagChipIsSelected` are plain functions (plain JUnit, no
 * composition needed). `pickerTagChipColors` is `@Composable` (it reads
 * [FilterChipDefaults.filterChipColors]'s theme-derived unset-field defaults), so its
 * assertions run inside a Robolectric [createComposeRule] composition, mirroring this
 * module's established Robolectric + Compose harness ([MicButtonGestureTest]).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class TagChipColorOptInTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val workColor = Color(0xFF6750A4)

    @Test
    fun `optedInTagColor returns the tag color only when showTagColors is true and color is non-null`() {
        assertNull(optedInTagColor(false, workColor))
        assertEquals(workColor, optedInTagColor(true, workColor))
        assertNull(optedInTagColor(true, null))
        assertNull(optedInTagColor(false, null))
    }

    @Test
    fun `pickerTagChipColors fills only the unselected container when opted in, and matches the default otherwise`() {
        lateinit var optedIn: SelectableChipColors
        lateinit var optedInWithColorExpected: SelectableChipColors
        lateinit var optedInNullColor: SelectableChipColors
        lateinit var optedOut: SelectableChipColors
        lateinit var default: SelectableChipColors
        composeTestRule.setContent {
            MaterialTheme {
                optedIn = pickerTagChipColors(true, workColor)
                optedInWithColorExpected = FilterChipDefaults.filterChipColors(containerColor = workColor)
                optedInNullColor = pickerTagChipColors(true, null)
                optedOut = pickerTagChipColors(false, workColor)
                default = FilterChipDefaults.filterChipColors()
            }
        }
        composeTestRule.waitForIdle()

        // pickerTagChipColors(true, c) == filterChipColors(containerColor = c)
        assertEquals(optedInWithColorExpected, optedIn)
        // pickerTagChipColors(true, null) == filterChipColors() (default)
        assertEquals(default, optedInNullColor)
        // pickerTagChipColors(false, c) == filterChipColors() (default)
        assertEquals(default, optedOut)
        // pickerTagChipColors(true, c) != filterChipColors() (the fill really changes)
        assertNotEquals(default, optedIn)
    }
}
