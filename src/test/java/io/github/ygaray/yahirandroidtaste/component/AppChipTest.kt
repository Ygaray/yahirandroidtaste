package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Compose tests for [AppChip]'s additive `onDoubleClick` parameter (Phase 106-02, TAG-02).
 *
 * Infra mirrors this module's established Robolectric+Compose harness ([TagListItemTest]):
 * `@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [35])`, `createComposeRule()`. The
 * composable is rendered directly with no theme wrapper, matching the established convention.
 *
 * Double-tap and long-press gestures are driven through the compose test rule's touch-input API
 * ([performTouchInput] with [doubleClick]/[longClick]) rather than issuing two separate clicks, so
 * the gesture actually reaches the [AppChip] inner Surface's `combinedClickable` modifier as a real
 * double-tap/long-press — not merely two independent single-tap invocations.
 *
 * **Latency claim scope (RESEARCH.md Pitfall 2 / Assumption A1 — confirmed by decompiling the
 * resolved `androidx.compose.foundation:foundation-android` bytecode this session, see the Task 1
 * commit message):** what this class proves is that the **default-null** `onDoubleClick` path is
 * byte-identical to `AppChip`'s pre-phase behavior — every existing consumer is unaffected. It also
 * proves the **active-binding** path eventually fires `onClick` on a single tap (see
 * `single click with a non-null onDoubleClick still invokes onClick exactly once`, which needs a
 * polling [androidx.compose.ui.test.junit4.ComposeContentTestRule.waitUntil] rather than a single
 * `waitForIdle()` — direct empirical evidence that `onClick` is genuinely delayed, not instant, once
 * `onDoubleClick` is non-null). What this class does **not** prove, and must not be read as proving,
 * is that an actively-bound double-tap adds **zero** single-tap latency: Robolectric's test clock
 * auto-advances, so any such assertion would pass vacuously without measuring real wall-clock delay,
 * and the decompiled bytecode confirms the delay is real (`ViewConfiguration.getDoubleTapTimeoutMillis()`
 * followed by `kotlinx.coroutines.delay()` before `onClick` fires). Real-perception judgment of an
 * actively-bound double-tap's latency belongs to Phase 109's on-device Gate-1 — no call site binds
 * `onDoubleClick` in this phase (D-02), so the question is moot for this phase's own shipped build.
 *
 * **Ordering/absence only, not value-correctness (WR-02, Phase 07 code review):** the
 * `containerColorOverride` precedence-order and D-01 tests below assert arm *presence and
 * relative position* inside [AppChip]'s raw `when`-block source text (via [whenBlock]), not the
 * resolved color value each arm evaluates to — a same-order swap of which value an arm resolves
 * to (e.g. `relatedness.containerColor` typo'd to `relatedness.contentColor` inside an unchanged
 * arm header) would pass these tests silently. Read the arm bodies by eye when reviewing a future
 * change to this file; these tests do not substitute for that.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppChipTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `double-tap with a non-null onDoubleClick invokes it exactly once`() {
        var doubleClickCount = 0

        composeTestRule.setContent {
            AppChip(
                label = "Work",
                isSelected = false,
                onClick = {},
                onDoubleClick = { doubleClickCount++ }
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Work").performTouchInput { doubleClick() }
        composeTestRule.waitForIdle()

        assertEquals(
            "Double-tapping a chip with a non-null onDoubleClick must invoke it exactly once",
            1,
            doubleClickCount
        )
    }

    @Test
    fun `single click with a non-null onDoubleClick still invokes onClick exactly once`() {
        var clickCount = 0
        var doubleClickCount = 0

        composeTestRule.setContent {
            AppChip(
                label = "Work",
                isSelected = false,
                onClick = { clickCount++ },
                onDoubleClick = { doubleClickCount++ }
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Work").performClick()
        composeTestRule.waitUntil(timeoutMillis = 2_000) { clickCount == 1 }

        assertEquals(
            "A single click on a chip whose double-tap slot is occupied must still invoke onClick" +
                " exactly once",
            1,
            clickCount
        )
        assertEquals(
            "A single click must never invoke onDoubleClick",
            0,
            doubleClickCount
        )
    }

    @Test
    fun `double-tap with both callbacks supplied fires only onDoubleClick, never onLongClick`() {
        var doubleClickCount = 0
        var longClickCount = 0

        composeTestRule.setContent {
            AppChip(
                label = "Work",
                isSelected = false,
                onClick = {},
                onLongClick = { longClickCount++ },
                onDoubleClick = { doubleClickCount++ }
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Work").performTouchInput { doubleClick() }
        composeTestRule.waitForIdle()

        assertEquals(
            "A double-tap must invoke onDoubleClick exactly once when both callbacks are supplied",
            1,
            doubleClickCount
        )
        assertEquals(
            "A double-tap must invoke onLongClick exactly zero times",
            0,
            longClickCount
        )
    }

    @Test
    fun `both callbacks null - a single click invokes onClick exactly once and the label renders`() {
        var clickCount = 0

        composeTestRule.setContent {
            AppChip(
                label = "Work",
                isSelected = false,
                onClick = { clickCount++ }
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Work").assertExists()
        composeTestRule.onNodeWithText("Work").performClick()
        composeTestRule.waitForIdle()

        assertEquals(
            "With both callbacks null (the shipped default for every existing consumer), a single" +
                " click must invoke onClick exactly once via the plain-clickable fallback branch",
            1,
            clickCount
        )
    }

    @Test
    fun `onLongClick only - long press fires it once, a separate click fires onClick once`() {
        var clickCount = 0
        var longClickCount = 0

        composeTestRule.setContent {
            AppChip(
                label = "Work",
                isSelected = false,
                onClick = { clickCount++ },
                onLongClick = { longClickCount++ }
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Work").performTouchInput { longClick() }
        composeTestRule.waitForIdle()

        assertEquals(
            "Today's shipped context-menu-chip shape: a long press must invoke onLongClick exactly" +
                " once",
            1,
            longClickCount
        )

        composeTestRule.onNodeWithText("Work").performClick()
        composeTestRule.waitForIdle()

        assertEquals(
            "A separate single click must still invoke onClick exactly once — long-press-only" +
                " does not delay onClick (no onDoubleClick disambiguation window applies here)",
            1,
            clickCount
        )
    }

    @Test
    fun `onDoubleClick only - double-tap fires it once, a long press fires nothing`() {
        var doubleClickCount = 0
        var longClickCount = 0

        composeTestRule.setContent {
            AppChip(
                label = "Work",
                isSelected = false,
                onClick = {},
                onDoubleClick = { doubleClickCount++ }
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Work").performTouchInput { doubleClick() }
        composeTestRule.waitForIdle()

        assertEquals(
            "The newly reachable branch: a double-tap with only onDoubleClick supplied must" +
                " invoke it exactly once",
            1,
            doubleClickCount
        )

        composeTestRule.onNodeWithText("Work").performTouchInput { longClick() }
        composeTestRule.waitForIdle()

        assertEquals(
            "A long press on a double-tap-only chip must invoke nothing — there is no" +
                " onLongClick to fire, and the long-press counter (tracking onDoubleClick" +
                " misfires) must stay zero",
            0,
            longClickCount
        )
    }

    @Test
    fun `default parameters - constructing with only the three required args compiles and is clickable`() {
        var clickCount = 0

        composeTestRule.setContent {
            AppChip(
                label = "Work",
                isSelected = false,
                onClick = { clickCount++ }
            )
        }
        composeTestRule.waitForIdle()

        // Omitting onLongClick/onDoubleClick/relatednessStrength/icons compiles unchanged and the
        // chip behaves exactly as it did before this phase — the default-parameter parity claim.
        composeTestRule.onNodeWithText("Work").performClick()
        composeTestRule.waitForIdle()

        assertEquals(
            "A chip constructed with only its three required arguments must remain clickable," +
                " byte-identical to pre-phase AppChip",
            1,
            clickCount
        )
    }

    // --- containerColorOverride (Phase 7 Plan 01, TAGCOLOR-01) -------------------------------

    @Test
    fun `containerColor precedence order is isSelected then relatedness then containerColorOverride then else`() {
        val containerColorBlock = whenBlock("containerColor")

        val isSelectedIndex = containerColorBlock.indexOf("isSelected ->")
        val relatednessIndex = containerColorBlock.indexOf("relatedness != null ->")
        val overrideIndex = containerColorBlock.indexOf("containerColorOverride != null ->")
        val elseIndex = containerColorBlock.indexOf("else ->")

        assertTrue(
            "containerColor's when-block must order its arms isSelected -> relatedness != null " +
                "-> containerColorOverride != null -> else -> (all found, in that order); found " +
                "indices: isSelected=$isSelectedIndex relatedness=$relatednessIndex " +
                "override=$overrideIndex else=$elseIndex",
            isSelectedIndex >= 0 && relatednessIndex > isSelectedIndex &&
                overrideIndex > relatednessIndex && elseIndex > overrideIndex
        )
    }

    @Test
    fun `contentColor when-block never references containerColorOverride (D-01)`() {
        val contentColorBlock = whenBlock("contentColor")

        assertEquals(
            "contentColor's when-block must never gain a containerColorOverride arm — content " +
                "color stays driven solely by isSelected/relatedness (D-01)",
            0,
            SourceContractTestSupport.countOccurrences(contentColorBlock, "containerColorOverride")
        )
    }

    @Test
    fun `borderStroke when-block never references containerColorOverride (D-01)`() {
        val borderStrokeBlock = whenBlock("borderStroke")

        assertEquals(
            "borderStroke's when-block must never gain a containerColorOverride arm — the border " +
                "stays driven solely by isSelected/relatedness (D-01)",
            0,
            SourceContractTestSupport.countOccurrences(borderStrokeBlock, "containerColorOverride")
        )
    }

    @Test
    fun `a non-null containerColorOverride still renders the label and stays clickable`() {
        var clickCount = 0

        composeTestRule.setContent {
            AppChip(
                label = "Work",
                isSelected = false,
                onClick = { clickCount++ },
                containerColorOverride = Color(0xFF6750A4)
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Work").assertExists()
        composeTestRule.onNodeWithText("Work").performClick()
        composeTestRule.waitForIdle()

        assertEquals(
            "A chip constructed with a non-null containerColorOverride must still render its " +
                "label and remain clickable exactly once — the new parameter's wiring must not " +
                "crash or break the existing gesture",
            1,
            clickCount
        )
    }

    @Test
    fun `omitting containerColorOverride - this phase's regression floor - still renders and clicks like pre-phase AppChip`() {
        var clickCount = 0

        composeTestRule.setContent {
            AppChip(
                label = "Work",
                isSelected = false,
                onClick = { clickCount++ }
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Work").assertExists()
        composeTestRule.onNodeWithText("Work").performClick()
        composeTestRule.waitForIdle()

        assertEquals(
            "Phase 7 Plan 01's own regression-floor proof: omitting containerColorOverride must " +
                "render/click exactly like pre-phase AppChip",
            1,
            clickCount
        )
    }

    // --- Source-reading helpers -------------------------------------------------------------

    /**
     * Isolates one of AppChip's three `val <name> = when { ... }` blocks by name via depth-aware
     * brace balancing, mirroring [CardBaseTest]'s `renderConditionalBraces` convention: scan
     * forward from the `val <name> = when {` anchor, balance `{`/`}` from its opening brace to
     * the matching close.
     */
    private fun whenBlock(name: String): String {
        val src = SourceContractTestSupport.stripComments(
            SourceContractTestSupport.source("AppChip.kt")
        )
        val anchor = "val $name = when {"
        val anchorIndex = src.indexOf(anchor)
        assertTrue("Could not locate '$anchor' in AppChip.kt", anchorIndex >= 0)

        val openBrace = src.indexOf('{', anchorIndex)
        val closeBrace = matchingCloseBraceIndex(src, openBrace)
        return src.substring(openBrace, closeBrace + 1)
    }

    private fun matchingCloseBraceIndex(text: String, openBraceIndex: Int): Int {
        var depth = 1
        var i = openBraceIndex + 1
        while (i < text.length) {
            when (text[i]) {
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) return i
                }
            }
            i++
        }
        error("No matching closing brace found for opening brace at index $openBraceIndex")
    }
}
