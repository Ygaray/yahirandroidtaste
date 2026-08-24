package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * ICON-01 live-search tests for [IconPickerGrid].
 *
 * Infra mirrors the module's established Robolectric+Compose harness (`PickerExpansionTest`):
 * `@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [35])`, `createComposeRule()`.
 *
 * Starts with a cell-semantics probe (114-REVIEWS.md cycle-1 finding 3, cycle-3 finding pinning the
 * trial key to `ICON_MAP.keys.first()` so it is guaranteed inside the `LazyVerticalGrid` viewport)
 * run against the UNMODIFIED composable, establishing which selector individual icon cells are
 * addressable by before any rendered assertion in this file depends on it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class IconPickerSearchTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // --- Cell-semantics probe (run against the UNMODIFIED composable) ---

    @Test
    fun `probe - an individual icon cell is uniquely addressable by contentDescription`() {
        val probeKey = ICON_MAP.keys.first()
        composeTestRule.setContent {
            IconPickerGrid(selectedIcon = "", onIconSelected = {})
        }
        composeTestRule.waitForIdle()

        val matches = composeTestRule
            .onAllNodesWithContentDescription(probeKey)
            .fetchSemanticsNodes()
        assertEquals(
            "Expected exactly one node with contentDescription '$probeKey' " +
                "(the cell's Icon sets contentDescription = name); " +
                "found ${matches.size}. If not 1, a per-cell testTag must be added instead.",
            1,
            matches.size
        )
    }

    // --- Test 1: search field exists ---

    @Test
    fun `search field node exists when IconPickerGrid is composed`() {
        composeTestRule.setContent {
            IconPickerGrid(selectedIcon = "", onIconSelected = {})
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("icon_search_field").assertExists()
    }

    // --- Test 2: typing a query live-filters the rendered grid (wiring proof) ---

    @Test
    fun `typing a uniquely-identifying query renders that icon's cell`() {
        // "zoom_out_map" is the only ICON_MAP key containing this substring, so the filtered
        // list narrows to exactly one entry, rendered at the top of the viewport.
        composeTestRule.setContent {
            IconPickerGrid(selectedIcon = "", onIconSelected = {})
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("icon_search_field").performTextInput("zoom_out_map")
        composeTestRule.waitForIdle()

        // Selector confirmed by the cell-semantics probe above: unique contentDescription match.
        composeTestRule.onNodeWithContentDescription("zoom_out_map").assertExists()
    }

    // --- Test 3: filterIconEntries("") returns everything, unchanged order ---

    @Test
    fun `filterIconEntries with a blank query returns every ICON_MAP entry in original order`() {
        val result = filterIconEntries("")

        assertEquals(ICON_MAP.entries.toList(), result)
    }

    // --- Test 4: filterIconEntries substring match, case-insensitive, multiple hits ---

    @Test
    fun `filterIconEntries with a substring query returns only matching keys, more than one`() {
        val result = filterIconEntries("zoom")

        assertTrue("Expected more than one match for 'zoom'", result.size > 1)
        assertTrue(result.all { it.key.contains("zoom", ignoreCase = true) })
        assertEquals(
            ICON_MAP.entries.filter { it.key.contains("zoom", ignoreCase = true) },
            result
        )
    }

    // --- Test 5: zero-match query renders the empty state ---

    @Test
    fun `a zero-match query renders the empty-state heading, body, and container`() {
        composeTestRule.setContent {
            IconPickerGrid(selectedIcon = "", onIconSelected = {})
        }
        composeTestRule.waitForIdle()

        // Obviously non-lexical -- cannot start matching/not-matching for vocabulary reasons if
        // ICON_MAP grows (114-02-PLAN.md Task 2 action).
        composeTestRule.onNodeWithTag("icon_search_field").performTextInput(ZERO_MATCH_QUERY)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("icon_search_empty_state").assertExists()
        composeTestRule.onNodeWithText("No icons found").assertExists()
        composeTestRule.onNodeWithText("Try a different search term.").assertExists()
    }

    // --- Test 6: zero-match query -- the grid branch is not composed at all ---

    @Test
    fun `a zero-match query does not compose the grid branch`() {
        composeTestRule.setContent {
            IconPickerGrid(selectedIcon = "", onIconSelected = {})
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("icon_search_field").performTextInput(ZERO_MATCH_QUERY)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("icon_search_grid").assertDoesNotExist()
    }

    // --- Test 7: clearing the field restores the full grid ---

    @Test
    fun `clearing the field via the built-in clear affordance restores the full grid`() {
        composeTestRule.setContent {
            IconPickerGrid(selectedIcon = "", onIconSelected = {})
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("icon_search_field").performTextInput(ZERO_MATCH_QUERY)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("icon_search_empty_state").assertExists()

        composeTestRule.onNodeWithContentDescription("Clear text").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("icon_search_empty_state").assertDoesNotExist()
        // Selector confirmed by the Task-1 cell-semantics probe: unique contentDescription match.
        // ICON_MAP.keys.first() is at scroll position zero, so it is guaranteed inside the viewport.
        composeTestRule.onNodeWithContentDescription(ICON_MAP.keys.first()).assertExists()
    }

    // --- Test 8: the two branches are mutually exclusive ---

    @Test
    fun `a matching query does not render the empty state`() {
        composeTestRule.setContent {
            IconPickerGrid(selectedIcon = "", onIconSelected = {})
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("icon_search_field").performTextInput("zoom_out_map")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("icon_search_empty_state").assertDoesNotExist()
        composeTestRule.onNodeWithTag("icon_search_grid").assertExists()
    }

    // --- Test 9: case-insensitivity ---

    @Test
    fun `filterIconEntries is case-insensitive`() {
        val lower = filterIconEntries("zoom")
        val upper = filterIconEntries("ZOOM")

        assertEquals(lower, upper)
    }

    // --- Test 10: mid-key substring match, not just prefix ---

    @Test
    fun `filterIconEntries matches mid-key substrings, not just prefixes`() {
        // "out_map" is a substring drawn from the middle/end of "zoom_out_map" -- not a prefix
        // of any ICON_MAP key.
        val result = filterIconEntries("out_map")

        assertTrue(result.any { it.key == "zoom_out_map" })
    }

    // --- Test 11: non-lexical query is empty; blank query is everything ---

    @Test
    fun `filterIconEntries on a non-lexical query returns empty, on blank returns everything`() {
        assertTrue(filterIconEntries(ZERO_MATCH_QUERY).isEmpty())
        assertEquals(ICON_MAP.size, filterIconEntries("").size)
    }

    // --- Test 12: a long query returns empty without error ---

    @Test
    fun `filterIconEntries on a long query returns empty without error`() {
        val longQuery = "a".repeat(500)

        val result = filterIconEntries(longQuery)

        assertTrue(result.isEmpty())
    }

    // --- Test 13: selection-during-filter ---

    @Test
    fun `selecting a key the active query does not match still renders, filter unaffected by selection`() {
        // "zoom_out_map" is the only key matching this query; ICON_MAP.keys.first() (e.g. "book")
        // does not match it, so the selection is filtered out of view -- correct, expected
        // single-select pick-one-grid UX (UI-SPEC selection-during-filter resolution, PITFALLS
        // Pitfall 3). No "current selection" preview chip is implemented or asserted.
        val nonMatchingSelection = ICON_MAP.keys.first()
        composeTestRule.setContent {
            IconPickerGrid(selectedIcon = nonMatchingSelection, onIconSelected = {})
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("icon_search_field").performTextInput("zoom_out_map")
        composeTestRule.waitForIdle()

        // Render succeeds (no crash/exception) and the matching icon's cell is present --
        // the filter decision is independent of the selection.
        composeTestRule.onNodeWithContentDescription("zoom_out_map").assertExists()
    }

    // --- Test 14: vocabulary spot-check locks D-01 ---

    @Test
    fun `the D-01 vocabulary spot-check terms all return non-empty results`() {
        val terms = listOf("car", "food", "phone", "money", "music", "book", "heart")

        terms.forEach { term ->
            assertTrue(
                "Expected a non-empty result for common search term '$term' -- an empty result " +
                    "would be real signal against D-01's no-alias-layer decision",
                filterIconEntries(term).isNotEmpty()
            )
        }
    }

    private companion object {
        // A run of repeated consonants -- not a plausible English word, so it cannot start
        // matching/not-matching for vocabulary reasons if ICON_MAP grows (Task 2 action).
        const val ZERO_MATCH_QUERY = "qxzqxzqxz"
    }
}
