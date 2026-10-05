package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
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
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.model.ApproachRungUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
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

    // ── VA11Y-01 / D-04: minimum interactive size on the interactive wrapper only ──

    @Test
    fun `rung touch targets do not overlap and are at least the minimum interactive size when the cap is selectable`() {
        var density: Density? = null
        composeTestRule.setContent {
            density = LocalDensity.current
            ApproachLadderCard(ladder = ladder, maxTierId = "hybrid", onMaxTierChange = {})
        }
        composeTestRule.waitForIdle()

        // Measures touch-bounds OVERLAP, not assertTouchHeightIsEqualTo / assertHeightIsAtLeast:
        // the framework already expands a clickable's touch bounds to 48dp, so the former passes
        // without the fix; the latter measures node bounds that exclude the min-size slack
        // (RESEARCH Pitfall 3). The real pre-fix defect is adjacent targets overlapping.
        val touch = (0..2).map { rungNodes()[it].fetchSemanticsNode().touchBoundsInRoot }
        val minHeightPx = with(density!!) { 48.dp.toPx() }
        touch.forEachIndexed { i, b ->
            assertTrue("rung $i touch height ${b.height} must be >= $minHeightPx", b.height >= minHeightPx)
        }
        for (i in 0..1) {
            assertTrue(
                "rung $i bottom ${touch[i].bottom} must not be below rung ${i + 1} top ${touch[i + 1].top}",
                touch[i].bottom <= touch[i + 1].top
            )
        }
    }

    @Test
    fun `a cap-less ladder keeps its compact row pitch -- the minimum size is only on the interactive wrapper`() {
        composeTestRule.setContent {
            Column {
                ApproachLadderCard(ladder = ladder)
                // Cap at the top rung ("cloud"): no row renders the "Capped" affordance, so the
                // two ladders' rows differ ONLY by the interactive wrapper (no text-height confound).
                ApproachLadderCard(ladder = ladder, maxTierId = "cloud", onMaxTierChange = {})
            }
        }
        composeTestRule.waitForIdle()

        rungNodes().assertCountEquals(6)
        fun top(i: Int) = rungNodes()[i].fetchSemanticsNode().boundsInRoot.top
        val caplessPitch = top(1) - top(0)
        val selectablePitch = top(4) - top(3)
        assertTrue(
            "cap-less pitch $caplessPitch must be strictly smaller than cap-selectable pitch $selectablePitch",
            caplessPitch < selectablePitch
        )
    }

    // ── VAPPR-04 / Phase 17: Router ON/OFF policy toggle ──

    @Test
    fun `toggling router emits onRouterChange with the new value`() {
        var lastValue: Boolean? = null
        composeTestRule.setContent {
            ApproachLadderCard(
                ladder = ladder,
                router = false,
                onRouterChange = { lastValue = it }
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("approach_ladder_card_router_toggle").assertExists()
        composeTestRule.onNodeWithContentDescription("Router off, selected").assertExists()
        composeTestRule.onNodeWithContentDescription("Router on, not selected").performClick()
        composeTestRule.waitForIdle()

        assertEquals(true, lastValue)
    }

    @Test
    fun `router true renders Router on selected and Router off not selected with the English defaults`() {
        composeTestRule.setContent {
            ApproachLadderCard(ladder = ladder, router = true, onRouterChange = {})
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Router on, selected").assertExists()
        composeTestRule.onNodeWithContentDescription("Router off, not selected").assertExists()
    }

    @Test
    fun `router toggle is not rendered when the pair is null`() {
        composeTestRule.setContent {
            ApproachLadderCard(ladder = ladder, router = null, onRouterChange = null)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("approach_ladder_card_router_toggle").assertDoesNotExist()
        composeTestRule.onAllNodesWithContentDescription("Router", substring = true).assertCountEquals(0)
        rungNodes().assertCountEquals(3)
    }

    @Test
    fun `router toggle appears and disappears as the pair flips between null and non-null`() {
        var state by mutableStateOf<Boolean?>(null)
        composeTestRule.setContent {
            val current = state
            // Both args derive from the same read, so the pair stays consistent across the flip.
            ApproachLadderCard(
                ladder = ladder,
                router = current,
                onRouterChange = if (current != null) { _: Boolean -> } else null
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("approach_ladder_card_router_toggle").assertDoesNotExist()

        composeTestRule.runOnIdle { state = false }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Router off, selected").assertExists()

        composeTestRule.runOnIdle { state = true }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Router on, selected").assertExists()

        composeTestRule.runOnIdle { state = null }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("approach_ladder_card_router_toggle").assertDoesNotExist()
    }

    @Test
    fun `a non-null onRouterChange with a null router throws -- the pairing invariant is enforced`() {
        assertThrows(IllegalArgumentException::class.java) {
            composeTestRule.setContent {
                ApproachLadderCard(ladder = ladder, router = null, onRouterChange = {})
            }
            composeTestRule.waitForIdle()
        }
    }

    @Test
    fun `a non-null router with a null onRouterChange throws -- the pairing invariant is enforced`() {
        assertThrows(IllegalArgumentException::class.java) {
            composeTestRule.setContent {
                ApproachLadderCard(ladder = ladder, router = true, onRouterChange = null)
            }
            composeTestRule.waitForIdle()
        }
    }

    @Test
    fun `supplied router labels replace the Router on and Router off segments and still emit`() {
        var lastValue: Boolean? = null
        composeTestRule.setContent {
            ApproachLadderCard(
                ladder = ladder,
                router = false,
                onRouterChange = { lastValue = it },
                routerOnLabel = "Routeur activé",
                routerOffLabel = "Routeur désactivé"
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Routeur désactivé, selected").assertExists()
        composeTestRule.onAllNodesWithContentDescription("Router off, selected").assertCountEquals(0)

        composeTestRule.onNodeWithContentDescription("Routeur activé, not selected").performClick()
        composeTestRule.waitForIdle()

        assertEquals(true, lastValue)
    }

    @Test
    fun `tapping a router segment emits only onRouterChange while rung and offline taps emit only their own callbacks`() {
        val tiers = mutableListOf<String>()
        val offline = mutableListOf<Boolean>()
        val routers = mutableListOf<Boolean>()
        composeTestRule.setContent {
            ApproachLadderCard(
                ladder = ladder,
                offlineOnly = false,
                onOfflineOnlyChange = { offline += it },
                maxTierId = "cloud",
                onMaxTierChange = { tiers += it },
                router = false,
                onRouterChange = { routers += it }
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("approach_ladder_card_offline_toggle").assertExists()
        composeTestRule.onNodeWithTag("approach_ladder_card_router_toggle").assertExists()

        composeTestRule.onNodeWithText("Local").performClick()
        composeTestRule.waitForIdle()
        assertEquals(listOf("local"), tiers)
        assertEquals(emptyList<Boolean>(), offline)
        assertEquals(emptyList<Boolean>(), routers)

        composeTestRule.onNodeWithContentDescription("Router on, not selected").performClick()
        composeTestRule.waitForIdle()
        assertEquals(listOf("local"), tiers)
        assertEquals(emptyList<Boolean>(), offline)
        assertEquals(listOf(true), routers)

        composeTestRule.onNodeWithContentDescription("Offline only, not selected").performClick()
        composeTestRule.waitForIdle()
        assertEquals(listOf("local"), tiers)
        assertEquals(listOf(true), offline)
        assertEquals(listOf(true), routers)
    }

    @Test
    fun `the router toggle sits below the offline toggle which sits below the last rung`() {
        composeTestRule.setContent {
            ApproachLadderCard(
                ladder = ladder,
                offlineOnly = false,
                onOfflineOnlyChange = {},
                maxTierId = "cloud",
                onMaxTierChange = {},
                router = false,
                onRouterChange = {}
            )
        }
        composeTestRule.waitForIdle()

        val lastRungBottom = rungNodes()[2].fetchSemanticsNode().boundsInRoot.bottom
        val offline = composeTestRule.onNodeWithTag("approach_ladder_card_offline_toggle")
            .fetchSemanticsNode().boundsInRoot
        val router = composeTestRule.onNodeWithTag("approach_ladder_card_router_toggle")
            .fetchSemanticsNode().boundsInRoot
        assertTrue(
            "offline top ${offline.top} must be >= last rung bottom $lastRungBottom",
            offline.top >= lastRungBottom
        )
        assertTrue(
            "router top ${router.top} must be >= offline bottom ${offline.bottom}",
            router.top >= offline.bottom
        )
        assertTrue("router top ${router.top} must be > offline top ${offline.top}", router.top > offline.top)
    }

    @Test
    fun `toggling router leaves rung order capped and needs-network affordances and cap selection unchanged`() {
        var routerState by mutableStateOf(false)
        composeTestRule.setContent {
            ApproachLadderCard(
                ladder = ladder,
                offlineOnly = true,
                onOfflineOnlyChange = {},
                maxTierId = "local",
                onMaxTierChange = {},
                router = routerState,
                onRouterChange = {}
            )
        }
        composeTestRule.waitForIdle()

        fun tops() = (0..2).map { rungNodes()[it].fetchSemanticsNode().boundsInRoot.top }
        fun assertRungsIntact() {
            rungNodes().assertCountEquals(3)
            rungNodes()[0].assert(hasText("Cloud"))
            rungNodes()[1].assert(hasText("Hybrid"))
            rungNodes()[2].assert(hasText("Local"))
            rungNodes()[2].assertIsSelected()
            composeTestRule.onAllNodesWithText("Capped").assertCountEquals(2)
            composeTestRule.onAllNodesWithText("Needs network").assertCountEquals(1)
        }

        assertRungsIntact()
        composeTestRule.onNodeWithContentDescription("Router off, selected").assertExists()
        val topsBefore = tops()

        composeTestRule.runOnIdle { routerState = true }
        composeTestRule.waitForIdle()

        assertRungsIntact()
        composeTestRule.onNodeWithContentDescription("Router on, selected").assertExists()
        assertEquals(topsBefore, tops())
    }

    @Test
    fun `tapping the already-selected router segment re-emits its own value and a repeated tap emits the same target value`() {
        val routers = mutableListOf<Boolean>()
        composeTestRule.setContent {
            // Router state is deliberately NOT updated by the callback: the caller owns it.
            ApproachLadderCard(ladder = ladder, router = true, onRouterChange = { routers += it })
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Router on, selected").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Router on, selected").performClick()
        composeTestRule.waitForIdle()
        assertEquals(listOf(true, true), routers)

        composeTestRule.onNodeWithContentDescription("Router off, not selected").performClick()
        composeTestRule.waitForIdle()
        assertEquals(listOf(true, true, false), routers)
    }

    @Test
    fun `two cards in one composition keep independent router callbacks`() {
        val a = mutableListOf<Boolean>()
        val b = mutableListOf<Boolean>()
        composeTestRule.setContent {
            Column {
                ApproachLadderCard(ladder = ladder, router = false, onRouterChange = { a += it })
                ApproachLadderCard(ladder = ladder, router = true, onRouterChange = { b += it })
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithTag("approach_ladder_card_router_toggle").assertCountEquals(2)

        // Only card A (router = false) exposes "Router on, not selected".
        composeTestRule.onNodeWithContentDescription("Router on, not selected").performClick()
        composeTestRule.waitForIdle()
        assertEquals(listOf(true), a)
        assertEquals(emptyList<Boolean>(), b)

        // Only card B (router = true) exposes "Router off, not selected".
        composeTestRule.onNodeWithContentDescription("Router off, not selected").performClick()
        composeTestRule.waitForIdle()
        assertEquals(listOf(true), a)
        assertEquals(listOf(false), b)
    }
}
