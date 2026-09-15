package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import io.github.ygaray.yahirandroidtaste.modifier.SwipeAnchor
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests for [CardBase]'s `statusContent` row (CARD-01/D-07/RD-01, Phase 164).
 *
 * ## Why this suite is split render-tests + source-structural, not full-card render
 * [CardStatusRow] itself is a small, self-contained composable that DOES render under this
 * module's Robolectric harness (unlike a full [CardBase]-based card — see [CardBaseTest]'s KDoc
 * for why those are unrenderable: [SwipeableActionRow] throws on its first frame). The render
 * tests below therefore drive [CardStatusRow] directly, with no [CardBase]/[SwipeableActionRow]
 * around it, proving the empty/inset/spacing/order/fontScale contract for real.
 *
 * What still cannot be rendered is [CardStatusRow]'s WIRING inside [CardBase] — that the row sits
 * between the body slot and the combined bottom row, that the legacy overload delegates with
 * `statusContent = null`, and that both overloads keep the v1.12.x signature intact. Those facts
 * are locked by [cardBaseStructureViolations], a single structural checker over the real,
 * committed `CardBase.kt` source (isolating each function via
 * [SourceContractTestSupport.functionBody]'s brace-balanced extraction, order-independent —
 * 164-04 review round 2 HIGH, replacing an earlier to-end-of-file scan that failed on a correct
 * implementation). Overload resolution itself (that old and new call shapes both still compile,
 * unambiguously) is locked by the never-invoked compile fixture at the bottom of this file.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CardStatusRowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // --- Render tests --------------------------------------------------------------------

    @Test
    fun `null statusContent composes no tagged node and closes the gap between siblings`() {
        composeTestRule.setContent {
            YahirAndroidTasteTheme(dynamicColor = false) {
                Column {
                    Text("above", modifier = Modifier.testTag("above"))
                    CardStatusRow(statusContent = null)
                    Text("below", modifier = Modifier.testTag("below"))
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("card_status_row").assertDoesNotExist()

        val aboveBottom = composeTestRule.onNodeWithTag("above").getUnclippedBoundsInRoot().bottom
        val belowTop = composeTestRule.onNodeWithTag("below").getUnclippedBoundsInRoot().top
        assertWithinDp(
            aboveBottom,
            belowTop,
            "the gap between two siblings around a null CardStatusRow must be zero (no dead space)"
        )
    }

    @Test
    fun `a non-null statusContent renders a tagged row with positive height containing its content`() {
        composeTestRule.setContent {
            YahirAndroidTasteTheme(dynamicColor = false) {
                CardStatusRow(statusContent = { ReminderIndicator(reminderCount = 2) })
            }
        }
        composeTestRule.waitForIdle()

        val rowBounds = composeTestRule.onNodeWithTag("card_status_row").getUnclippedBoundsInRoot()
        assertTrue(
            "card_status_row must report a positive height when statusContent is non-null, " +
                "was ${rowBounds.height}",
            rowBounds.height > 0.dp
        )
        composeTestRule.onNodeWithContentDescription("2 reminders").assertExists()
    }

    @Test
    fun `statusContent items are inset 16dp and spaced 4dp apart, tagged bounds span the full row width`() {
        composeTestRule.setContent {
            YahirAndroidTasteTheme(dynamicColor = false) {
                Box(modifier = Modifier.width(320.dp)) {
                    CardStatusRow(
                        statusContent = {
                            Text("A", modifier = Modifier.testTag("a"))
                            Text("B", modifier = Modifier.testTag("b"))
                        }
                    )
                }
            }
        }
        composeTestRule.waitForIdle()

        val rowBounds = composeTestRule.onNodeWithTag("card_status_row").getUnclippedBoundsInRoot()
        assertWithinDp(320.dp, rowBounds.width, "card_status_row's width must span its 320dp host")

        val aBounds = composeTestRule.onNodeWithTag("a").getUnclippedBoundsInRoot()
        val bBounds = composeTestRule.onNodeWithTag("b").getUnclippedBoundsInRoot()
        assertWithinDp(
            rowBounds.left + 16.dp,
            aBounds.left,
            "the first status item must sit 16dp right of the tagged row's left edge (the inset " +
                "is measurable because the tag precedes the padding in the modifier chain)"
        )
        assertWithinDp(
            aBounds.right + 4.dp,
            bBounds.left,
            "the second status item must sit 4dp right of the first item's right edge"
        )
    }

    @Test
    fun `at fontScale 2 the row grows to contain the reminder indicator's count text`() {
        composeTestRule.setContent {
            YahirAndroidTasteTheme(dynamicColor = false) {
                val baseDensity = LocalDensity.current
                CompositionLocalProvider(
                    LocalDensity provides Density(baseDensity.density, fontScale = 2f)
                ) {
                    CardStatusRow(statusContent = { ReminderIndicator(reminderCount = 12) })
                }
            }
        }
        composeTestRule.waitForIdle()

        val rowBounds = composeTestRule.onNodeWithTag("card_status_row").getUnclippedBoundsInRoot()
        val textBounds = composeTestRule.onNodeWithText("12").getUnclippedBoundsInRoot()

        assertTrue(
            "at fontScale 2 the '12' text's top (${textBounds.top}) must not sit above the " +
                "row's top (${rowBounds.top}) — the row must grow to contain it, not clip it",
            textBounds.top >= rowBounds.top
        )
        assertTrue(
            "at fontScale 2 the '12' text's bottom (${textBounds.bottom}) must not sit below " +
                "the row's bottom (${rowBounds.bottom}) — the row must grow to contain it, not " +
                "clip it",
            textBounds.bottom <= rowBounds.bottom
        )
    }

    @Test
    fun `a reminder count of one inside the row exposes singular accessibility wording`() {
        composeTestRule.setContent {
            YahirAndroidTasteTheme(dynamicColor = false) {
                CardStatusRow(statusContent = { ReminderIndicator(reminderCount = 1) })
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("1 reminder").assertExists()
    }

    // --- Source-structural tests: CardBase wiring, both overloads, no-dead-code -----------

    @Test
    fun `cardBaseStructureViolations reports no violation against the real committed CardBase source`() {
        val src = SourceContractTestSupport.stripComments(SourceContractTestSupport.source("CardBase.kt"))
        val violations = cardBaseStructureViolations(src)
        assertTrue(
            "the real CardBase.kt must satisfy every statusContent wiring rule; violations: $violations",
            violations.isEmpty()
        )
    }

    @Test
    fun `cardBaseStructureViolations is order-independent — a fixture with a different function order passes`() {
        val violations = cardBaseStructureViolations(FIXTURE_LEGACY_FIRST)
        assertTrue(
            "fixture (a) reorders legacy-then-CardStatusRow-then-full and must satisfy every " +
                "rule; violations: $violations",
            violations.isEmpty()
        )
    }

    @Test
    fun `cardBaseStructureViolations flags a CardStatusRow call inside the legacy overload's body`() {
        val violations = cardBaseStructureViolations(FIXTURE_LEGACY_CALLS_STATUS_ROW)
        assertTrue(
            "expected a violation naming the legacy overload, got: $violations",
            violations.isNotEmpty() && violations.any { it.contains("LEGACY") }
        )
    }

    @Test
    fun `cardBaseStructureViolations flags a default value on the statusContent overload's slot parameter`() {
        val violations = cardBaseStructureViolations(FIXTURE_STATUS_CONTENT_HAS_DEFAULT)
        assertTrue(
            "expected a violation naming the default, got: $violations",
            violations.isNotEmpty() && violations.any { it.contains("default") }
        )
    }

    // --- dp-tolerant comparison helper -----------------------------------------------------

    private fun assertWithinDp(expected: Dp, actual: Dp, label: String, tolerance: Dp = 0.5.dp) {
        val diff = if (expected > actual) expected - actual else actual - expected
        assertTrue(
            "$label — expected ~$expected, was $actual (diff $diff exceeds tolerance $tolerance)",
            diff <= tolerance
        )
    }
}

/**
 * Order-independent structural checker for [CardBase]'s statusContent wiring (164-04 review
 * round 2 HIGH). Isolates the two `fun CardBase(` overloads and `fun CardStatusRow(` by
 * [SourceContractTestSupport.functionBody]'s brace-balanced extraction — never by "from this
 * declaration to the end of the file" — so the rules hold regardless of which function the
 * source lists first. Classifies the two `CardBase` overloads by CONTENT (whichever one contains
 * `val cardColumnContent` is FULL, the statusContent-accepting overload; the other is LEGACY, the
 * v1.12.x-preserving delegate), not by declaration order.
 *
 * @return a human-readable violation message per broken rule; an empty list means every rule
 *   held.
 */
private fun cardBaseStructureViolations(src: String): List<String> {
    val violations = mutableListOf<String>()

    val cardBaseDeclCount = SourceContractTestSupport.countOccurrences(src, "fun CardBase(")
    if (cardBaseDeclCount != 2) {
        violations += "'fun CardBase(' must occur exactly twice in the file, found $cardBaseDeclCount"
    }
    val cardColumnContentCount = SourceContractTestSupport.countOccurrences(src, "val cardColumnContent")
    if (cardColumnContentCount != 1) {
        violations += "'val cardColumnContent' must occur exactly once in the file (the body " +
            "must not be duplicated), found $cardColumnContentCount"
    }

    if (cardBaseDeclCount != 2) {
        // Cannot safely extract both overloads by occurrence index; report what we have so far.
        return violations
    }

    val body1 = SourceContractTestSupport.functionBody(src, "fun CardBase(", 1)
    val body2 = SourceContractTestSupport.functionBody(src, "fun CardBase(", 2)
    val body1HasColumn = body1.contains("val cardColumnContent")
    val body2HasColumn = body2.contains("val cardColumnContent")

    if (body1HasColumn == body2HasColumn) {
        violations += "exactly one of the two CardBase overloads must contain " +
            "'val cardColumnContent' (found in first=$body1HasColumn, second=$body2HasColumn)"
        return violations
    }

    val full = if (body1HasColumn) body1 else body2
    val legacy = if (body1HasColumn) body2 else body1

    violations += fullOverloadViolations(full)
    violations += legacyOverloadViolations(legacy)
    violations += wholeFileStatusContentViolations(src)
    violations += cardStatusRowFunctionViolations(src)

    return violations
}

/** Rules scoped to the FULL (statusContent-accepting) [CardBase] overload's own body text. */
private fun fullOverloadViolations(full: String): List<String> {
    val violations = mutableListOf<String>()

    val fullStatusParamCount = SourceContractTestSupport.countOccurrences(
        full,
        "statusContent: (@Composable RowScope.() -> Unit)?"
    )
    if (fullStatusParamCount != 1) {
        violations += "the FULL overload must declare " +
            "'statusContent: (@Composable RowScope.() -> Unit)?' exactly once, " +
            "found $fullStatusParamCount"
    } else {
        val tactileIndex = full.indexOf("tactileDepth: Boolean = false")
        val statusIndex = full.indexOf("statusContent: (@Composable RowScope.() -> Unit)?")
        if (tactileIndex < 0 || statusIndex < tactileIndex) {
            violations += "the FULL overload's statusContent parameter must appear after " +
                "'tactileDepth: Boolean = false'"
        }
    }

    val fullStatusRowCallCount = SourceContractTestSupport.countOccurrences(
        full,
        "CardStatusRow(statusContent = statusContent)"
    )
    if (fullStatusRowCallCount != 1) {
        violations += "the FULL overload must call 'CardStatusRow(statusContent = statusContent)' " +
            "exactly once, found $fullStatusRowCallCount"
    } else {
        val bodyContentCallIndex = full.indexOf("bodyContent()")
        val statusRowCallIndex = full.indexOf("CardStatusRow(statusContent = statusContent)")
        val bottomRowGateIndex = full.indexOf(
            "if (tagRowContent != null || footerContent != null || showThreeDot)"
        )
        if (bodyContentCallIndex < 0 || statusRowCallIndex < bodyContentCallIndex) {
            violations += "the FULL overload's CardStatusRow(...) call must appear after bodyContent()"
        }
        if (bottomRowGateIndex < 0 || statusRowCallIndex > bottomRowGateIndex) {
            violations += "the FULL overload's CardStatusRow(...) call must appear before the " +
                "combined bottom row gate"
        }
    }

    return violations
}

/** Rules scoped to the LEGACY (v1.12.x-preserving delegate) [CardBase] overload's own body text. */
private fun legacyOverloadViolations(legacy: String): List<String> {
    val violations = mutableListOf<String>()

    val legacyTactileParamCount = SourceContractTestSupport.countOccurrences(
        legacy,
        "tactileDepth: Boolean = false"
    )
    if (legacyTactileParamCount != 1) {
        violations += "the LEGACY overload must declare 'tactileDepth: Boolean = false' " +
            "exactly once, found $legacyTactileParamCount"
    }
    val legacyTactileForwardCount = SourceContractTestSupport.countOccurrences(
        legacy,
        "tactileDepth = tactileDepth"
    )
    if (legacyTactileForwardCount != 1) {
        violations += "the LEGACY overload must forward 'tactileDepth = tactileDepth' exactly " +
            "once, found $legacyTactileForwardCount"
    }
    val legacyStatusNullCount = SourceContractTestSupport.countOccurrences(legacy, "statusContent = null")
    if (legacyStatusNullCount != 1) {
        violations += "the LEGACY overload must delegate 'statusContent = null' exactly once " +
            "(it must pass a null status slot on the caller's behalf), found $legacyStatusNullCount"
    }
    val legacyCardBaseCount = SourceContractTestSupport.countOccurrences(legacy, "CardBase(")
    if (legacyCardBaseCount != 2) {
        violations += "the LEGACY overload must contain 'CardBase(' exactly twice (its own " +
            "declaration and the delegated call to the FULL overload), found $legacyCardBaseCount"
    }
    if (legacy.contains("statusContent:")) {
        violations += "the LEGACY overload must not declare a statusContent parameter of its " +
            "own (found 'statusContent:')"
    }
    if (legacy.contains("CardStatusRow(")) {
        violations += "the LEGACY overload must not call CardStatusRow(...) directly — it must " +
            "delegate to the FULL overload with statusContent = null instead"
    }
    if (legacy.contains("val cardColumnContent")) {
        violations += "the LEGACY overload must not declare its own cardColumnContent"
    }

    return violations
}

/** Rules scoped to the whole file (both overloads combined), not either one alone. */
private fun wholeFileStatusContentViolations(src: String): List<String> {
    val violations = mutableListOf<String>()

    val defaultedStatusParamCount = SourceContractTestSupport.countOccurrences(
        src,
        "statusContent: (@Composable RowScope.() -> Unit)? ="
    )
    if (defaultedStatusParamCount != 0) {
        violations += "no statusContent parameter anywhere in the file may carry a default " +
            "value (a default would make every call omitting it ambiguous between the two " +
            "overloads), found $defaultedStatusParamCount occurrence(s)"
    }
    val wholeFileStatusRowCallCount = SourceContractTestSupport.countOccurrences(
        src,
        "CardStatusRow(statusContent = statusContent)"
    )
    if (wholeFileStatusRowCallCount != 1) {
        violations += "the whole file must contain 'CardStatusRow(statusContent = statusContent)' " +
            "exactly once, found $wholeFileStatusRowCallCount"
    }
    val bottomRowGateWholeFileCount = SourceContractTestSupport.countOccurrences(
        src,
        "if (tagRowContent != null || footerContent != null || showThreeDot)"
    )
    if (bottomRowGateWholeFileCount != 1) {
        violations += "the combined bottom row gate must occur exactly once in the whole file " +
            "(unchanged), found $bottomRowGateWholeFileCount"
    }

    return violations
}

/** Rules scoped to the `CardStatusRow` composable itself. */
private fun cardStatusRowFunctionViolations(src: String): List<String> {
    val violations = mutableListOf<String>()

    val cardStatusRowDeclCount = SourceContractTestSupport.countOccurrences(src, "fun CardStatusRow(")
    if (cardStatusRowDeclCount != 1) {
        violations += "'fun CardStatusRow(' must occur exactly once, found $cardStatusRowDeclCount"
        return violations
    }

    val rowBody = SourceContractTestSupport.functionBody(src, "fun CardStatusRow(")
    val tagIndex = rowBody.indexOf(".testTag(\"card_status_row\")")
    val paddingIndex = rowBody.indexOf(".padding(")
    if (tagIndex < 0 || paddingIndex < 0 || tagIndex > paddingIndex) {
        violations += "CardStatusRow's .testTag(\"card_status_row\") must appear before " +
            ".padding(...) in its modifier chain (so the tagged bounds are the full row, " +
            "not the inset content)"
    }
    val statusInvokeCount = SourceContractTestSupport.countOccurrences(rowBody, "statusContent()")
    if (statusInvokeCount != 1) {
        violations += "CardStatusRow must invoke statusContent() exactly once, " +
            "found $statusInvokeCount"
    }

    return violations
}

/**
 * Fixture (a): a minimal text holding the LEGACY overload FIRST, then `CardStatusRow`, then the
 * FULL (statusContent-accepting) overload — the opposite order from the real source — satisfying
 * every [cardBaseStructureViolations] rule. Proves the checker classifies by content, not
 * declaration order.
 */
private val FIXTURE_LEGACY_FIRST = """
fun CardBase(
    openRowState: Any,
    tactileDepth: Boolean = false
) {
    CardBase(
        openRowState = openRowState,
        tactileDepth = tactileDepth,
        statusContent = null
    )
}

fun CardStatusRow(statusContent: (@Composable RowScope.() -> Unit)?) {
    if (statusContent == null) return
    Row(
        modifier = Modifier
            .testTag("card_status_row")
            .padding(horizontal = 1.dp)
    ) {
        statusContent()
    }
}

fun CardBase(
    openRowState: Any,
    tactileDepth: Boolean = false,
    statusContent: (@Composable RowScope.() -> Unit)?
) {
    val cardColumnContent = {
        bodyContent()
        CardStatusRow(statusContent = statusContent)
        if (tagRowContent != null || footerContent != null || showThreeDot) {
            doSomething()
        }
    }
}
""".trimIndent()

/**
 * Fixture (b): [FIXTURE_LEGACY_FIRST] with a `CardStatusRow(statusContent = null)` call added
 * inside the LEGACY overload's body — the checker must report a violation naming the legacy
 * overload.
 */
private val FIXTURE_LEGACY_CALLS_STATUS_ROW = """
fun CardBase(
    openRowState: Any,
    tactileDepth: Boolean = false
) {
    CardStatusRow(statusContent = null)
    CardBase(
        openRowState = openRowState,
        tactileDepth = tactileDepth,
        statusContent = null
    )
}

fun CardStatusRow(statusContent: (@Composable RowScope.() -> Unit)?) {
    if (statusContent == null) return
    Row(
        modifier = Modifier
            .testTag("card_status_row")
            .padding(horizontal = 1.dp)
    ) {
        statusContent()
    }
}

fun CardBase(
    openRowState: Any,
    tactileDepth: Boolean = false,
    statusContent: (@Composable RowScope.() -> Unit)?
) {
    val cardColumnContent = {
        bodyContent()
        CardStatusRow(statusContent = statusContent)
        if (tagRowContent != null || footerContent != null || showThreeDot) {
            doSomething()
        }
    }
}
""".trimIndent()

/**
 * Fixture (c): [FIXTURE_LEGACY_FIRST] with ` = null` appended to the FULL overload's
 * `statusContent` slot parameter — the checker must report a violation naming the default.
 */
private val FIXTURE_STATUS_CONTENT_HAS_DEFAULT = """
fun CardBase(
    openRowState: Any,
    tactileDepth: Boolean = false
) {
    CardBase(
        openRowState = openRowState,
        tactileDepth = tactileDepth,
        statusContent = null
    )
}

fun CardStatusRow(statusContent: (@Composable RowScope.() -> Unit)?) {
    if (statusContent == null) return
    Row(
        modifier = Modifier
            .testTag("card_status_row")
            .padding(horizontal = 1.dp)
    ) {
        statusContent()
    }
}

fun CardBase(
    openRowState: Any,
    tactileDepth: Boolean = false,
    statusContent: (@Composable RowScope.() -> Unit)? = null
) {
    val cardColumnContent = {
        bodyContent()
        CardStatusRow(statusContent = statusContent)
        if (tagRowContent != null || footerContent != null || showThreeDot) {
            doSomething()
        }
    }
}
""".trimIndent()

/**
 * Never-invoked compile fixture (164-04 review round 1 LOW): proves every pre-existing CardBase
 * call shape still resolves unambiguously to the LEGACY overload, and that naming `statusContent`
 * resolves to the FULL overload — the exact shapes built and compiled at planning time in an
 * isolated hub clone. `@Suppress("UnusedPrivateMember")` because this function exists only to be
 * compiled by `compileDebugUnitTestKotlin`, never called.
 */
@Suppress("UnusedPrivateMember")
@Composable
private fun CardStatusRowOverloadCompileFixture() {
    val state = remember { mutableStateOf<AnchoredDraggableState<SwipeAnchor>?>(null) }

    // Zero/one positional argument — resolves to LEGACY (FULL's statusContent has no default).
    CardBase(state)
    CardBase(state, Modifier)

    // All 15 LEGACY positional arguments — resolves to LEGACY.
    CardBase(
        state,
        Modifier,
        false,
        true,
        {},
        {},
        {},
        {},
        {},
        null,
        null,
        null,
        null,
        null,
        false
    )

    // Named arguments omitting statusContent — resolves to LEGACY.
    CardBase(openRowState = state, headerContent = { Text("h") }, tactileDepth = true)

    // Naming statusContent — resolves to FULL, with both a null and a lambda slot.
    CardBase(openRowState = state, statusContent = null)
    CardBase(openRowState = state, statusContent = { Text("r") })
}
