package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import io.github.ygaray.yahirandroidtaste.modifier.SwipeAnchor
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Compose tests proving IMG-02's `imageCount` indicator end-to-end on both `TextCard`-family
 * surfaces: [TextCardBottomSheet] directly (Task 1 tracer) and [TextCard], including the
 * card-to-sheet forwarding hop (Task 2 expansion).
 *
 * Infra mirrors this module's established Robolectric+Compose harness
 * ([DynamicActionButtonTest]): `@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [35])`,
 * `createComposeRule()`. The indicator is located by its count-carrying accessibility
 * description ([onNodeWithContentDescription]); absence is asserted via
 * [assertDoesNotExist] (existence-negative), not by asserting a zero numeral is missing.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class TextCardImageIndicatorTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // --- TextCardBottomSheet (Task 1 tracer) ---

    @Test
    fun `bottom sheet with positive imageCount exposes count-carrying accessibility node and visible text`() {
        composeTestRule.setContent {
            TextCardBottomSheet(
                title = "Trip notes",
                content = "Some body text",
                categoryPath = null,
                createdAt = 0L,
                updatedAt = 0L,
                isPinned = false,
                isFavorite = false,
                onEdit = {},
                onDismiss = {},
                onTogglePin = {},
                onToggleFavorite = {},
                onDelete = {},
                onConfirmRename = {},
                imageCount = 3
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("3 inline images").assertExists()
        composeTestRule.onNodeWithText("3").assertExists()
    }

    @Test
    fun `bottom sheet with zero imageCount renders no indicator node and no count text`() {
        composeTestRule.setContent {
            TextCardBottomSheet(
                title = "Trip notes",
                content = "Some body text",
                categoryPath = null,
                createdAt = 0L,
                updatedAt = 0L,
                isPinned = false,
                isFavorite = false,
                onEdit = {},
                onDismiss = {},
                onTogglePin = {},
                onToggleFavorite = {},
                onDelete = {},
                onConfirmRename = {},
                imageCount = 0
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("0 inline images").assertDoesNotExist()
        composeTestRule.onNodeWithText("0").assertDoesNotExist()
    }

    @Test
    fun `bottom sheet with imageCount omitted defaults to zero and renders nothing`() {
        composeTestRule.setContent {
            TextCardBottomSheet(
                title = "Trip notes",
                content = "Some body text",
                categoryPath = null,
                createdAt = 0L,
                updatedAt = 0L,
                isPinned = false,
                isFavorite = false,
                onEdit = {},
                onDismiss = {},
                onTogglePin = {},
                onToggleFavorite = {},
                onDelete = {},
                onConfirmRename = {}
                // imageCount omitted — must default to 0
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("0 inline images").assertDoesNotExist()
        composeTestRule.onNodeWithText("0").assertDoesNotExist()
    }

    // --- TextCard (Task 2 expansion — probe first, per plan protocol) ---
    //
    // [Rule 3 - Blocking] Full card composable unrenderable under the unit harness.
    // The probe ran alone and crashed with:
    //   java.lang.IllegalStateException: The offset was read before being initialized. Did
    //   you access the offset in a phase before layout, like effects or composition?
    //     at androidx.compose.foundation.gestures.AnchoredDraggableState.requireOffset(...)
    //     at io.github.ygaray.yahirandroidtaste.modifier.SwipeableActionRowKt$SwipeableActionRow$3$1...
    // The crash originates inside CardBase's unconditional `SwipeableActionRow` — its
    // `LaunchedEffect(state) { snapshotFlow { state.requireOffset() }... }` (SwipeableActionRow.kt
    // line ~205-206) collects before the sibling `SideEffect` that installs the drag anchors has
    // run, so `requireOffset()` throws on the very first frame. This is unconditional or
    // `openRowState`'s value (null vs non-null makes no difference) and is entirely inside a
    // pre-existing file this plan does not touch — exactly the risk RESEARCH.md Pitfall 3 named
    // ("no test in the repo instantiates a full card composable"). Remedy attempted:
    // `composeTestRule.mainClock.autoAdvance = false` before `setContent` — same crash, since the
    // exception fires synchronously inside `setContent`'s initial `applyChanges` pass, before any
    // clock stepping is possible. No further remedy is available without modifying
    // SwipeableActionRow.kt, which is out of this plan's scope (Rule 1 bug fixes are scoped to
    // files this task's changes touch).
    //
    // Per the plan's step-c fallback, all three card-render cases below are quarantined with
    // @Ignore (not deleted, goal not weakened). The substitute evidence for the unprovable half:
    // (i) the source-level wiring grep in this task's acceptance criteria proving
    // `TextCard.kt`'s header block calls `ImageCountIndicator(imageCount = imageCount)` and its
    // hosted-sheet call forwards `imageCount = imageCount`; and (ii) Task 3's isolated
    // `ImageCountIndicatorTest`, which renders the shared composable directly with no card/sheet
    // around it and proves the whole render contract independently of any card-render capability.
    // Task 1's `TextCardBottomSheet` cases already prove the render contract end-to-end on the
    // one surface this harness CAN render.

    @Test
    @Ignore("TextCard is unrenderable under this Robolectric harness — CardBase's SwipeableActionRow throws IllegalStateException (requireOffset read before layout) on the very first frame, unconditionally, in a pre-existing file this plan does not touch. See block comment above. [Rule 3 - Blocking]")
    fun `card face with positive imageCount exposes count-carrying accessibility node`() {
        composeTestRule.setContent {
            val openRowState = remember { mutableStateOf<AnchoredDraggableState<SwipeAnchor>?>(null) }
            TextCard(
                id = "card-1",
                title = "Trip notes",
                content = "Some body text",
                categoryPath = null,
                createdAt = 0L,
                updatedAt = 0L,
                isPinned = false,
                isFavorite = false,
                onEdit = {},
                onDelete = {},
                onTogglePin = {},
                onToggleFavorite = {},
                onConfirmRename = {},
                openRowState = openRowState,
                imageCount = 4
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("4 inline images").assertExists()
    }

    @Test
    @Ignore("TextCard is unrenderable under this Robolectric harness — see the block comment above the card-face positive-count case. [Rule 3 - Blocking]")
    fun `card face with zero imageCount exposes no indicator node`() {
        composeTestRule.setContent {
            val openRowState = remember { mutableStateOf<AnchoredDraggableState<SwipeAnchor>?>(null) }
            TextCard(
                id = "card-2",
                title = "Trip notes",
                content = "Some body text",
                categoryPath = null,
                createdAt = 0L,
                updatedAt = 0L,
                isPinned = false,
                isFavorite = false,
                onEdit = {},
                onDelete = {},
                onTogglePin = {},
                onToggleFavorite = {},
                onConfirmRename = {},
                openRowState = openRowState,
                imageCount = 0
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("0 inline images").assertDoesNotExist()
    }

    @Test
    @Ignore("TextCard is unrenderable under this Robolectric harness — see the block comment above the card-face positive-count case. [Rule 3 - Blocking]")
    fun `card with positive imageCount and its bottom sheet open forwards the count to the sheet`() {
        composeTestRule.setContent {
            val openRowState = remember { mutableStateOf<AnchoredDraggableState<SwipeAnchor>?>(null) }
            TextCard(
                id = "card-3",
                title = "Trip notes",
                content = "Some body text",
                categoryPath = null,
                createdAt = 0L,
                updatedAt = 0L,
                isPinned = false,
                isFavorite = false,
                onEdit = {},
                onDelete = {},
                onTogglePin = {},
                onToggleFavorite = {},
                onConfirmRename = {},
                openRowState = openRowState,
                showBottomSheet = true,
                imageCount = 7
            )
        }
        composeTestRule.waitForIdle()

        // Distinct count value (7, vs 4 in the card-face case) so a stale/hardcoded value
        // cannot satisfy both — proves the value crossed the card-to-sheet forwarding hop.
        composeTestRule.onNodeWithContentDescription("7 inline images").assertExists()
    }
}
