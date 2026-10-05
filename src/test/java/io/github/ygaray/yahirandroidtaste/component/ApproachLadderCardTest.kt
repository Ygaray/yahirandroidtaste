package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.ygaray.yahirandroidtaste.model.ApproachRungUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Compose tests for [ApproachLadderCard] (Phase 10 Plan 02, VAPPR-01/02/03).
 *
 * Infra mirrors this module's established Robolectric+Compose harness ([CountBadgeTest],
 * [ProviderKeyCardTest]): `@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [35])`,
 * `createComposeRule()`.
 *
 * Ordering ([rungNodes]) mirrors [io.github.ygaray.yahirandroidtaste.component.VoiceCardClipListTest]'s
 * shared-tag `onAllNodesWithTag("voice_clip_row")` convention — every rung row shares the tag
 * `"approach_ladder_card_rung"`, and [androidx.compose.ui.test.SemanticsNodeInteractionCollection]'s
 * indexed access reflects composition (= list) order.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ApproachLadderCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun rungNodes() = composeTestRule.onAllNodesWithTag("approach_ladder_card_rung")

    // Deliberately NOT in rank order -- proves VAPPR-01 renders LIST order, never sorted by rank.
    private val ladder = listOf(
        ApproachRungUiModel(id = "cloud", label = "Cloud", rank = 3, offlineCapable = false),
        ApproachRungUiModel(id = "hybrid", label = "Hybrid", rank = 2, offlineCapable = true),
        ApproachRungUiModel(id = "local", label = "Local", rank = 1, offlineCapable = true)
    )

    @Test
    fun `renders each ladder rung label in list order`() {
        composeTestRule.setContent {
            ApproachLadderCard(ladder = ladder)
        }
        composeTestRule.waitForIdle()

        rungNodes().assertCountEquals(3)
        rungNodes()[0].assert(hasText("Cloud"))
        rungNodes()[1].assert(hasText("Hybrid"))
        rungNodes()[2].assert(hasText("Local"))
    }

    @Test
    fun `toggling offline-only emits onOfflineOnlyChange with the new value`() {
        var lastValue: Boolean? = null
        composeTestRule.setContent {
            ApproachLadderCard(
                ladder = ladder,
                offlineOnly = false,
                onOfflineOnlyChange = { lastValue = it }
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("approach_ladder_card_offline_toggle").assertExists()
        composeTestRule.onNodeWithContentDescription("Offline only, not selected").performClick()
        composeTestRule.waitForIdle()

        assertEquals(true, lastValue)
    }

    @Test
    fun `offline-only toggle is not rendered when the prop is null`() {
        composeTestRule.setContent {
            ApproachLadderCard(ladder = ladder, offlineOnly = null, onOfflineOnlyChange = null)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("approach_ladder_card_offline_toggle").assertDoesNotExist()
    }

    @Test
    fun `tapping a rung emits onMaxTierChange with the tapped id when cap props are present`() {
        var lastTier: String? = null
        composeTestRule.setContent {
            ApproachLadderCard(
                ladder = ladder,
                maxTierId = "cloud",
                onMaxTierChange = { lastTier = it }
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Local").performClick()
        composeTestRule.waitForIdle()

        assertEquals(
            "Tapping the Local rung must emit onMaxTierChange(\"local\")",
            "local",
            lastTier
        )
    }

    @Test
    fun `a rung above the cap renders greyed-but-present`() {
        composeTestRule.setContent {
            ApproachLadderCard(
                ladder = ladder,
                maxTierId = "local",
                onMaxTierChange = {}
            )
        }
        composeTestRule.waitForIdle()

        // Cloud (rank 3) and Hybrid (rank 2) are above the "local" cap (rank 1) -- both must stay
        // visible (conditional-render-no-dead-space), and BOTH carry the capped affordance.
        composeTestRule.onNodeWithText("Cloud").assertExists()
        composeTestRule.onNodeWithText("Hybrid").assertExists()
        composeTestRule.onAllNodesWithText("Capped").assertCountEquals(2)
    }

    @Test
    fun `cap control is not rendered when maxTierId is null -- rungs are not clickable`() {
        composeTestRule.setContent {
            ApproachLadderCard(ladder = ladder, maxTierId = null, onMaxTierChange = null)
        }
        composeTestRule.waitForIdle()

        // Null cap props (D-05) -- no rung is wired to a cap-selection callback, proven by the
        // absence of a click action on the rung node (null onClick -> no clickable semantics node).
        composeTestRule.onNodeWithText("Local").assertHasNoClickAction()
    }

    // ── WR-01 regression: maxTierId/onMaxTierChange pairing is enforced, never silently violated ──

    @Test
    fun `a non-null onMaxTierChange with a null maxTierId throws -- the pairing invariant is enforced`() {
        assertThrows(IllegalArgumentException::class.java) {
            composeTestRule.setContent {
                ApproachLadderCard(ladder = ladder, maxTierId = null, onMaxTierChange = {})
            }
            composeTestRule.waitForIdle()
        }
    }

    @Test
    fun `a non-null maxTierId with a null onMaxTierChange throws -- the pairing invariant is enforced`() {
        assertThrows(IllegalArgumentException::class.java) {
            composeTestRule.setContent {
                ApproachLadderCard(ladder = ladder, maxTierId = "local", onMaxTierChange = null)
            }
            composeTestRule.waitForIdle()
        }
    }

    // ── WR-01 symmetry regression: offlineOnly/onOfflineOnlyChange pairing is enforced too ──

    @Test
    fun `a non-null onOfflineOnlyChange with a null offlineOnly throws -- the pairing invariant is enforced`() {
        assertThrows(IllegalArgumentException::class.java) {
            composeTestRule.setContent {
                ApproachLadderCard(ladder = ladder, offlineOnly = null, onOfflineOnlyChange = {})
            }
            composeTestRule.waitForIdle()
        }
    }

    @Test
    fun `a non-null offlineOnly with a null onOfflineOnlyChange throws -- the pairing invariant is enforced`() {
        assertThrows(IllegalArgumentException::class.java) {
            composeTestRule.setContent {
                ApproachLadderCard(ladder = ladder, offlineOnly = true, onOfflineOnlyChange = null)
            }
            composeTestRule.waitForIdle()
        }
    }

    @Test
    fun `offline-only on gives an offline-incapable rung a needs-network affordance while staying visible`() {
        composeTestRule.setContent {
            ApproachLadderCard(
                ladder = ladder,
                offlineOnly = true,
                onOfflineOnlyChange = {}
            )
        }
        composeTestRule.waitForIdle()

        // "Cloud" is offlineCapable = false -- needs the affordance but must remain visible.
        composeTestRule.onNodeWithText("Cloud").assertExists()
        composeTestRule.onNodeWithText("Needs network").assertExists()
    }

    // ── WR-02: a disabled rung renders an explicit "Unavailable" affordance ──

    @Test
    fun `a disabled rung renders greyed-but-present with an Unavailable affordance`() {
        val ladderWithDisabledRung = listOf(
            ApproachRungUiModel(id = "cloud", label = "Cloud", rank = 3, offlineCapable = false),
            ApproachRungUiModel(
                id = "hybrid",
                label = "Hybrid",
                rank = 2,
                enabled = false,
                offlineCapable = true
            ),
            ApproachRungUiModel(id = "local", label = "Local", rank = 1, offlineCapable = true)
        )
        composeTestRule.setContent {
            ApproachLadderCard(ladder = ladderWithDisabledRung)
        }
        composeTestRule.waitForIdle()

        // Disabled-but-present (conditional-render-no-dead-space) -- "Hybrid" stays visible and
        // carries an explicit affordance explaining why it reads as greyed-out.
        composeTestRule.onNodeWithText("Hybrid").assertExists()
        composeTestRule.onNodeWithText("Unavailable").assertExists()
    }

    // ── VI18N-03: caller-localizable rung-state and toggle labels ──

    @Test
    fun `supplied rung-state labels replace the Unavailable Capped and Needs network affordances`() {
        val mixedLadder = listOf(
            ApproachRungUiModel(id = "cloud", label = "Cloud", rank = 3, offlineCapable = false),
            ApproachRungUiModel(
                id = "hybrid",
                label = "Hybrid",
                rank = 2,
                enabled = false,
                offlineCapable = true
            ),
            ApproachRungUiModel(id = "local", label = "Local", rank = 1, offlineCapable = true)
        )
        composeTestRule.setContent {
            ApproachLadderCard(
                ladder = mixedLadder,
                offlineOnly = true,
                onOfflineOnlyChange = {},
                maxTierId = "local",
                onMaxTierChange = {},
                unavailableLabel = "Indisponible",
                cappedLabel = "Plafonné",
                needsNetworkLabel = "Réseau requis"
            )
        }
        composeTestRule.waitForIdle()

        // Cloud (rank 3) and Hybrid (rank 2) sit above the "local" cap -> two capped rows.
        composeTestRule.onAllNodesWithText("Plafonné").assertCountEquals(2)
        composeTestRule.onAllNodesWithText("Indisponible").assertCountEquals(1)
        composeTestRule.onAllNodesWithText("Réseau requis").assertCountEquals(1)
        composeTestRule.onAllNodesWithText("Capped").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("Unavailable").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("Needs network").assertCountEquals(0)
    }

    @Test
    fun `supplied toggle labels replace the Online and Offline only segments and still emit`() {
        var lastValue: Boolean? = null
        composeTestRule.setContent {
            ApproachLadderCard(
                ladder = ladder,
                offlineOnly = false,
                onOfflineOnlyChange = { lastValue = it },
                onlineLabel = "En ligne",
                offlineOnlyLabel = "Hors ligne"
            )
        }
        composeTestRule.waitForIdle()

        // The trailing state words "selected" / "not selected" come from SegmentedOptionSelector
        // and remain English (known residual; not part of VI18N-01..04).
        composeTestRule.onNodeWithContentDescription("En ligne, selected").assertExists()
        composeTestRule.onAllNodesWithContentDescription("Offline only, not selected")
            .assertCountEquals(0)

        composeTestRule.onNodeWithContentDescription("Hors ligne, not selected").performClick()
        composeTestRule.waitForIdle()

        assertEquals(true, lastValue)
    }

    @Test
    fun `omitting the toggle labels keeps the English Online segment`() {
        composeTestRule.setContent {
            ApproachLadderCard(
                ladder = ladder,
                offlineOnly = false,
                onOfflineOnlyChange = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Online, selected").assertExists()
    }

    // ── VA11Y-01 / D-01 / D-03: radio + selected semantics on the chosen cap rung ──

    private val radioButtonRole = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton)

    @Test
    fun `the rung whose id equals maxTierId is Selected RadioButton and the others are unselected RadioButtons`() {
        composeTestRule.setContent {
            ApproachLadderCard(ladder = ladder, maxTierId = "hybrid", onMaxTierChange = {})
        }
        composeTestRule.waitForIdle()

        rungNodes()[1].assertIsSelected()
        rungNodes()[0].assertIsNotSelected()
        rungNodes()[2].assertIsNotSelected()
        (0..2).forEach { rungNodes()[it].assert(radioButtonRole) }
    }

    @Test
    fun `selection follows the chosen cap id and never the above-cap capped state`() {
        composeTestRule.setContent {
            ApproachLadderCard(ladder = ladder, maxTierId = "local", onMaxTierChange = {})
        }
        composeTestRule.waitForIdle()

        // Cloud + Hybrid sit ABOVE the "local" cap (they render "Capped") yet are NOT selected;
        // only Local -- the rung the user actually chose -- is announced as selected (D-01).
        composeTestRule.onAllNodesWithText("Capped").assertCountEquals(2)
        rungNodes()[0].assertIsNotSelected()
        rungNodes()[1].assertIsNotSelected()
        rungNodes()[2].assertIsSelected()
    }

    @Test
    fun `a stale maxTierId that matches no rung leaves every rung unselected without throwing`() {
        composeTestRule.setContent {
            ApproachLadderCard(ladder = ladder, maxTierId = "ghost", onMaxTierChange = {})
        }
        composeTestRule.waitForIdle()

        (0..2).forEach {
            rungNodes()[it].assertIsNotSelected()
            rungNodes()[it].assert(radioButtonRole)
        }
    }

    @Test
    fun `a cap-less ladder exposes no role no selected state and no click action on any rung`() {
        composeTestRule.setContent {
            ApproachLadderCard(ladder = ladder)
        }
        composeTestRule.waitForIdle()

        (0..2).forEach {
            rungNodes()[it].assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Role))
            rungNodes()[it].assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Selected))
            rungNodes()[it].assertHasNoClickAction()
        }
    }
}
