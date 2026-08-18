package com.example.secondbrain.yahirandroidtaste.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Nature
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit + Compose tests for the DASH-03 picker expansion (`IconPickerGrid` / `AccentColorPicker`).
 *
 * Locks:
 *  - `ACCENT_COLORS` additive-invariant (D-04): the append-only expansion never renames, reorders,
 *    or removes any of the original 20 entries, and `ACCENT_COLORS[2]` is still the Purple default.
 *  - `showIndices` gating (D-05/D-06): the gallery-only index badge renders iff `showIndices = true`,
 *    on both `IconPickerGrid` and `AccentColorPicker` — the production default (`showIndices = false`)
 *    never renders a badge.
 *
 * Infra mirrors this module's established Robolectric+Compose harness
 * (`CycleSubTypeButtonTest.kt`): `@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [35])`,
 * `createComposeRule()`.
 *
 * ICON_MAP is NOT asserted for size here — Plan 66-03 owns the >=500 assertion once its mass
 * expansion lands.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PickerExpansionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // --- ACCENT_COLORS additive invariant (D-04) — plain JUnit, no Compose render needed ---

    private val originalTwentyAccentColors = listOf(
        AccentColor(0xFFB3261E, 0xFFFFB4AB),   // Red
        AccentColor(0xFF904D4D, 0xFFFFB4B4),   // Pink
        AccentColor(0xFF6750A4, 0xFFD0BCFF),   // Purple (default)
        AccentColor(0xFF4A4074, 0xFFCBC2FF),   // Deep Purple
        AccentColor(0xFF3A5BA0, 0xFFADC6FF),   // Indigo
        AccentColor(0xFF1A6FB8, 0xFFB3D9FF),   // Blue
        AccentColor(0xFF006A6A, 0xFF4DD9D9),   // Teal
        AccentColor(0xFF386A20, 0xFF9DD47A),   // Green
        AccentColor(0xFF516600, 0xFFCBEA65),   // Lime
        AccentColor(0xFF695E00, 0xFFE9C50D),   // Yellow
        AccentColor(0xFF8B4000, 0xFFFFB787),   // Orange
        AccentColor(0xFF6D4A26, 0xFFE7BFA0),   // Brown
        AccentColor(0xFFD84315, 0xFFFFAB91),   // Coral
        AccentColor(0xFFC2185B, 0xFFF48FB1),   // Rose
        AccentColor(0xFF7E57C2, 0xFFD1C4E9),   // Lavender
        AccentColor(0xFF00838F, 0xFF80DEEA),   // Cyan
        AccentColor(0xFF2E7D32, 0xFFA5D6A7),   // Mint
        AccentColor(0xFF546E7A, 0xFFB0BEC5),   // Slate
        AccentColor(0xFF37474F, 0xFF90A4AE),   // Charcoal
        AccentColor(0xFFBF8C00, 0xFFFFD54F),   // Gold
    )

    @Test
    fun `ACCENT_COLORS has grown to at least 30 entries`() {
        assertTrue(
            "Expected ACCENT_COLORS.size >= 30, was ${ACCENT_COLORS.size}",
            ACCENT_COLORS.size >= 30
        )
    }

    @Test
    fun `the original 20 ACCENT_COLORS entries are unchanged in value and order`() {
        assertEquals(originalTwentyAccentColors, ACCENT_COLORS.take(20))
    }

    @Test
    fun `ACCENT_COLORS index 2 is still the Purple default`() {
        assertEquals(AccentColor(0xFF6750A4, 0xFFD0BCFF), ACCENT_COLORS[2])
    }

    // --- ICON_MAP additive invariant (D-02/D-03, Plan 66-03) — plain JUnit, no Compose render ---

    private val originalFiftyIconKeys = listOf(
        "book", "star", "work", "school", "home", "favorite", "music", "camera",
        "palette", "fitness", "restaurant", "flight", "code", "science", "pets",
        "nature", "shopping", "medical", "build", "movie", "sports", "games",
        "photo", "mail", "phone", "money", "lightbulb", "directions_car",
        "local_cafe", "psychology", "checklist", "timer", "alarm", "calendar",
        "map", "language", "brush", "headphones", "wifi", "bolt", "rocket",
        "group", "child", "cake", "sunny", "nightlight", "forest", "beach",
        "shield", "lock",
    )

    // The full original 50 key -> ImageVector pairs, frozen from base commit 3d98e5f1
    // (pre-Phase-66 `IconPickerGrid.kt`). These 50 keys persist as `tags.icon_name` in the
    // database, so a codegen-appended key must never silently overwrite one of them.
    private val originalFiftyIconMap = mapOf(
        "book" to Icons.Default.Book,
        "star" to Icons.Default.Star,
        "work" to Icons.Default.Work,
        "school" to Icons.Default.School,
        "home" to Icons.Default.Home,
        "favorite" to Icons.Default.Favorite,
        "music" to Icons.Default.MusicNote,
        "camera" to Icons.Default.CameraAlt,
        "palette" to Icons.Default.Palette,
        "fitness" to Icons.Default.FitnessCenter,
        "restaurant" to Icons.Default.Restaurant,
        "flight" to Icons.Default.Flight,
        "code" to Icons.Default.Code,
        "science" to Icons.Default.Science,
        "pets" to Icons.Default.Pets,
        "nature" to Icons.Default.Nature,
        "shopping" to Icons.Default.ShoppingCart,
        "medical" to Icons.Default.MedicalServices,
        "build" to Icons.Default.Build,
        "movie" to Icons.Default.Movie,
        "sports" to Icons.Default.SportsBasketball,
        "games" to Icons.Default.SportsEsports,
        "photo" to Icons.Default.PhotoLibrary,
        "mail" to Icons.Default.Email,
        "phone" to Icons.Default.Phone,
        "money" to Icons.Default.AttachMoney,
        "lightbulb" to Icons.Default.Lightbulb,
        "directions_car" to Icons.Default.DirectionsCar,
        "local_cafe" to Icons.Default.LocalCafe,
        "psychology" to Icons.Default.Psychology,
        "checklist" to Icons.Default.Checklist,
        "timer" to Icons.Default.Timer,
        "alarm" to Icons.Default.Alarm,
        "calendar" to Icons.Default.CalendarMonth,
        "map" to Icons.Default.Map,
        "language" to Icons.Default.Language,
        "brush" to Icons.Default.Brush,
        "headphones" to Icons.Default.Headphones,
        "wifi" to Icons.Default.Wifi,
        "bolt" to Icons.Default.Bolt,
        "rocket" to Icons.Default.RocketLaunch,
        "group" to Icons.Default.Group,
        "child" to Icons.Default.ChildCare,
        "cake" to Icons.Default.Cake,
        "sunny" to Icons.Default.WbSunny,
        "nightlight" to Icons.Default.NightsStay,
        "forest" to Icons.Default.Forest,
        "beach" to Icons.Default.BeachAccess,
        "shield" to Icons.Default.Shield,
        "lock" to Icons.Default.Lock,
    )

    @Test
    fun `ICON_MAP has grown to at least 500 entries`() {
        assertTrue(
            "Expected ICON_MAP.size >= 500, was ${ICON_MAP.size}",
            ICON_MAP.size >= 500
        )
    }

    @Test
    fun `the original 50 ICON_MAP keys are present in their original order`() {
        assertEquals(originalFiftyIconKeys, ICON_MAP.keys.toList().take(50))
    }

    @Test
    fun `original ICON_MAP values are preserved for spot-checked keys`() {
        assertTrue(ICON_MAP["star"] === Icons.Default.Star)
        assertTrue(ICON_MAP["book"] === Icons.Default.Book)
        assertTrue(ICON_MAP["work"] === Icons.Default.Work)
        assertTrue(ICON_MAP["lock"] === Icons.Default.Lock)
    }

    @Test
    fun `no original ICON_MAP entry was overwritten by a later duplicate key`() {
        // `Map.size == Map.keys.toSet().size` is true by construction for every Map instance
        // and can never catch a collision (a Kotlin `mapOf` silently collapses a duplicate key
        // into a single entry, keeping the original's insertion position but the LATER value).
        // The only way to detect a same-key overwrite is a reference-equality check of every
        // original key against its expected `Icons.Default.X` value, so assert all 50 here
        // (not just the 4 previously spot-checked) — these keys persist as `tags.icon_name`,
        // so a codegen-appended key silently overwriting one would be a real data-integrity bug.
        originalFiftyIconMap.forEach { (key, expectedVector) ->
            assertTrue(
                "Key '$key' must still map to its original vector (not overwritten by a later duplicate key)",
                ICON_MAP[key] === expectedVector
            )
        }
    }

    // --- showIndices badge gating (D-05/D-06) — IconPickerGrid ---

    @Test
    fun `IconPickerGrid renders index badges when showIndices is true`() {
        composeTestRule.setContent {
            IconPickerGrid(selectedIcon = "star", onIconSelected = {}, showIndices = true)
        }
        composeTestRule.waitForIdle()

        val badges = composeTestRule.onAllNodesWithTag("icon_index_badge").fetchSemanticsNodes()
        assertTrue("Expected icon_index_badge nodes when showIndices=true", badges.isNotEmpty())
    }

    @Test
    fun `IconPickerGrid renders no index badges by default`() {
        composeTestRule.setContent {
            IconPickerGrid(selectedIcon = "star", onIconSelected = {})
        }
        composeTestRule.waitForIdle()

        val badges = composeTestRule.onAllNodesWithTag("icon_index_badge").fetchSemanticsNodes()
        assertTrue("Expected zero icon_index_badge nodes when showIndices=false (default)", badges.isEmpty())
    }

    // --- showIndices badge gating (D-05/D-06) — AccentColorPicker ---

    @Test
    fun `AccentColorPicker renders index badges when showIndices is true`() {
        composeTestRule.setContent {
            AccentColorPicker(
                selectedColor = ACCENT_COLORS[2].light,
                onColorSelected = {},
                showIndices = true
            )
        }
        composeTestRule.waitForIdle()

        val badges = composeTestRule.onAllNodesWithTag("color_index_badge").fetchSemanticsNodes()
        assertTrue("Expected color_index_badge nodes when showIndices=true", badges.isNotEmpty())
    }

    @Test
    fun `AccentColorPicker renders no index badges by default`() {
        composeTestRule.setContent {
            AccentColorPicker(selectedColor = ACCENT_COLORS[2].light, onColorSelected = {})
        }
        composeTestRule.waitForIdle()

        val badges = composeTestRule.onAllNodesWithTag("color_index_badge").fetchSemanticsNodes()
        assertTrue("Expected zero color_index_badge nodes when showIndices=false (default)", badges.isEmpty())
    }
}
