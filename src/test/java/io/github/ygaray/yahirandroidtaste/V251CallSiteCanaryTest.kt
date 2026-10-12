package io.github.ygaray.yahirandroidtaste

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.ygaray.yahirandroidtaste.component.ActionButtonDefaults
import io.github.ygaray.yahirandroidtaste.component.DynamicActionButton
import io.github.ygaray.yahirandroidtaste.component.OutcomeSheet
import io.github.ygaray.yahirandroidtaste.component.ProviderKeyCard
import io.github.ygaray.yahirandroidtaste.component.SegmentedOptionSelector
import io.github.ygaray.yahirandroidtaste.component.SheetScaffold
import io.github.ygaray.yahirandroidtaste.component.SourceContractTestSupport
import io.github.ygaray.yahirandroidtaste.feedback.UndoCenterScreen
import io.github.ygaray.yahirandroidtaste.model.BatchRowResultUiModel
import io.github.ygaray.yahirandroidtaste.model.FailureActionUiModel
import io.github.ygaray.yahirandroidtaste.model.FailureTone
import io.github.ygaray.yahirandroidtaste.model.HandledByUiModel
import io.github.ygaray.yahirandroidtaste.model.KeyFieldState
import io.github.ygaray.yahirandroidtaste.model.ProposedItemUiModel
import io.github.ygaray.yahirandroidtaste.model.ProviderOptionUiModel
import io.github.ygaray.yahirandroidtaste.model.SelectionMode
import io.github.ygaray.yahirandroidtaste.model.UndoAffordanceUiModel
import io.github.ygaray.yahirandroidtaste.model.UndoRefusedUiModel
import io.github.ygaray.yahirandroidtaste.model.UndoRowState
import io.github.ygaray.yahirandroidtaste.model.UndoRowUiModel
import io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * D-01 cut-gate canary (Phase 24, API-03): VERBATIM v2.5.1-shaped call sites for the nine surfaces
 * the v2.6.0 widening touched, copied from the two consumers (read-only) and from the v2.5.1
 * Explorer sources (`git show v2.5.1:...`). Every shape in this file must compile on BOTH v2.5.1
 * and HEAD, and the model shapes must keep their v2.5.1 behavior (defaults). That is the whole
 * claim: it was proven by compiling and running this very file against a pristine `git archive` of
 * the `v2.5.1` tag (see 24-02-SUMMARY.md), so no shape here is a modernised one.
 *
 * Provenance legend (one comment per shape): `SB <file>:<line>` is SecondBrain
 * (`app/src/main/java/com/example/secondbrain/...`), `CT <file>:<line>` is CalTracker
 * (`app/src/main/java/com/caltracker/app/ui/...`), `Explorer@v2.5.1 <file>` is the hub's own
 * gallery at the v2.5.1 tag. Consumer-only identifiers (view models, resource strings) are replaced
 * by local holders and neutral strings; the argument shape (positional, named, trailing lambda) is
 * kept as written.
 *
 * Reuse, not duplication: shapes already pinned by `OutcomeSheetSourceCompatV251Test`
 * (positional `OutcomeSheet(outcome, onDismissRequest, modifier)`, the FailureActionUiModel
 * trailing lambda, the HandledByUiModel positional prefixes, Success at arities 1 to 6),
 * `ProviderKeyCardSourceCompatV251Test` (positional ProviderKeyCard shapes) and
 * `UndoCenterScreenSourceCompatV251Test` (the two Wave 0 UndoCenterScreen shapes) are referenced
 * at the shape, not copied. This file adds the consumer-verbatim and Explorer-verbatim shapes
 * those classes lack.
 *
 * EXCLUDED on purpose: the positional four-argument `UndoCenterScreen(entries, onNavigateBack,
 * onUndo, onClear)` call. It no longer compiles once `groups` and `onUndoGroup` sit before the
 * trailing `onClear` (accepted, documented and owner-flagged break of Phase 23; both consumers pass
 * named arguments). Composable calls live in non-invoked lambdas: they only have to COMPILE.
 */
@OptIn(ExperimentalMaterial3Api::class)
class V251CallSiteCanaryTest {

    // ------------------------------------------------------------------------------------------
    // OutcomeSheet
    // ------------------------------------------------------------------------------------------

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun outcomeSheet_v251ConsumerShapes_compile() {
        val failure: VoiceOutcomeUiState = VoiceOutcomeUiState.Failure(reason = "r")
        val success = VoiceOutcomeUiState.Success(summary = "s")
        val onDismiss: () -> Unit = {}

        // SB feature/voicecommand/VoiceSheetHost.kt:135 and :234: named outcome and
        // onDismissRequest, the latter with a trailing comma and a lambda that reads state.
        val sbNamed: @Composable () -> Unit = {
            OutcomeSheet(
                outcome = success,
                onDismissRequest = { if (!false) onDismiss() },
            )
        }

        // SB feature/voicecommand/VoiceSheetHost.kt:196 and :252: named outcome and onDismissRequest on one line.
        val sbOneLine: @Composable () -> Unit = {
            OutcomeSheet(outcome = success, onDismissRequest = { onDismiss() })
        }

        // CT voice/VoiceResultSheet.kt:136-146 (and the Success :147, Proposed :161, ProposedBatch :176,
        // SuccessBatch :192 arms below it): named outcome produced by a mapper, onDismissRequest named
        // last, trailing comma.
        val ctNamed: @Composable () -> Unit = {
            OutcomeSheet(
                outcome = failure,
                onDismissRequest = onDismiss,
            )
        }

        // Explorer@v2.5.1 explorer/VoiceCommandFamilyScreen.kt:641: named outcome, onDismissRequest
        // with a block body that invokes onCancel before clearing the visible outcome.
        val explorerNamed: @Composable () -> Unit = {
            OutcomeSheet(
                outcome = failure,
                onDismissRequest = {
                    (failure as? VoiceOutcomeUiState.NeedsConfirmation)?.onCancel?.invoke()
                },
            )
        }

        // Positional three-argument shape: OutcomeSheetSourceCompatV251Test
        // #v251PositionalOutcomeSheetCallShape_compiles (not copied). Positional with a Modifier
        // argument named, v2.5.1 had exactly these three parameters:
        val modifierNamed: @Composable () -> Unit = {
            OutcomeSheet(outcome = failure, onDismissRequest = onDismiss, modifier = Modifier)
        }
    }

    // ------------------------------------------------------------------------------------------
    // VoiceOutcomeUiState arms: Success, Failure, NeedsConfirmation
    // ------------------------------------------------------------------------------------------

    @Test
    fun success_v251Shapes_keepTheirDefaults() {
        // SB feature/voicecommand/VoiceSheetModels.kt:16: named summary and handledBy only.
        val sb = VoiceOutcomeUiState.Success(
            summary = "Done",
            handledBy = HandledByUiModel("Local"),
        )
        // Explorer@v2.5.1 VoiceCommandFamilyScreen.kt:410: summary only, named.
        val summaryOnly = VoiceOutcomeUiState.Success(summary = "Logged 1 item")
        // CT voice/VoiceOutcomeMapper.kt:136 and :216: named summary, handledBy and the
        // composable editableContent slot.
        val ct = VoiceOutcomeUiState.Success(
            summary = "Logged 2 items",
            handledBy = null,
            editableContent = { Text("editable") },
        )
        // Every named v2.5.1 field at once: editableContent, inFlight, batchResults, undo.
        val undo = UndoAffordanceUiModel("Undo all (2)")
        val everyNamed = VoiceOutcomeUiState.Success(
            summary = "Logged 2 items",
            handledBy = HandledByUiModel(tier = "Cloud"),
            editableContent = {},
            inFlight = true,
            batchResults = listOf(BatchRowResultUiModel("Item", true), BatchRowResultUiModel("Other", false, "why")),
            undo = undo,
        )
        // Explorer@v2.5.1 VoiceCommandFamilyScreen.kt:430-441: undo affordance with Available rows.
        val explorerUndo = VoiceOutcomeUiState.Success(
            summary = "Logged 2 items",
            undo = UndoAffordanceUiModel(
                allLabel = "Undo all (2)",
                rows = listOf(
                    UndoRowUiModel(id = "1", label = "Item removed", state = UndoRowState.Available(onUndo = {})),
                    UndoRowUiModel(id = "2", label = "Tag removed", state = UndoRowState.Unavailable(reason = "Entangled")),
                ),
                onUndoAll = {},
                refused = UndoRefusedUiModel(reason = "Item changed since", changedItem = "Item removed"),
            ),
        )
        // Positional arities 1 to 6: OutcomeSheetSourceCompatV251Test#v251StateArmShapes_...

        listOf(sb, summaryOnly, ct, everyNamed, explorerUndo).forEach {
            assertNull(it.badge)
            assertNull(it.titleActions)
            assertNull(it.startAction)
            assertNull(it.endAction)
        }
        assertTrue(everyNamed.inFlight)
        assertFalse(summaryOnly.inFlight)
        assertEquals(2, everyNamed.batchResults.size)
        assertSame(undo, everyNamed.undo)
        assertNull(summaryOnly.undo)
        assertTrue(summaryOnly.batchResults.isEmpty())
    }

    @Test
    fun failure_v251Shapes_keepTheirDefaults() {
        // Explorer@v2.5.1 VoiceCommandFamilyScreen.kt:421: reason only, named.
        val reasonOnly = VoiceOutcomeUiState.Failure(reason = "Could not reach the provider")
        // Explorer@v2.5.1 VoiceCommandFamilyScreen.kt:424-427 and CT voice/VoiceOutcomeMapper.kt:94-98:
        // reason, handledBy = null and a named FailureActionUiModel.
        val withAction = VoiceOutcomeUiState.Failure(
            reason = "API key rejected",
            action = FailureActionUiModel(label = "Open Settings", onClick = {}),
        )
        val ctShape = VoiceOutcomeUiState.Failure(
            reason = "No model",
            handledBy = null,
            action = FailureActionUiModel(label = "Download", onClick = {}),
        )
        // SB feature/voicecommand/VoiceFailureSheetModels.kt:129-138: named reason, handledBy and
        // action; the FailureActionUiModel built with named label and onClick.
        val sbShape = VoiceOutcomeUiState.Failure(
            reason = "Stopped",
            handledBy = HandledByUiModel(tier = "Local"),
            action = FailureActionUiModel(
                label = "Retry",
                onClick = {},
            ),
        )
        // SB feature/voicecommand/VoiceSheetHost.kt:135-138 (base.copy(body = body)): body slot
        // named, plus semanticsPrefix, the two v2.5.1 trailing fields.
        val bodyShape = VoiceOutcomeUiState.Failure(
            reason = "r",
            handledBy = null,
            action = null,
            body = { Text("body") },
            semanticsPrefix = "Error:",
        )
        val copied = sbShape.copy(body = { Text("body") })

        listOf(reasonOnly, withAction, ctShape, sbShape, bodyShape, copied).forEach {
            assertNull(it.title)
            assertNull(it.transcript)
            assertEquals(FailureTone.Error, it.tone)
            assertNull(it.tip)
            assertNull(it.secondaryAction)
            assertNull(it.badge)
            assertNull(it.undo)
        }
        assertEquals("Error:", bodyShape.semanticsPrefix)
        assertEquals("Retry", copied.action?.label)
        // Positional arities (reason through semanticsPrefix): OutcomeSheetSourceCompatV251Test.
    }

    @Test
    fun needsConfirmation_v251NamedShapes_keepTheirDefaults() {
        // Explorer@v2.5.1 VoiceCommandFamilyScreen.kt:476-486 (SB MutationGate shape): one
        // destructive item, a reversibility hint, onConfirm and onCancel LAST. All named, no
        // confirmEnabled and no titleActions (both are v2.6.0 additions).
        val single = VoiceOutcomeUiState.NeedsConfirmation(
            reason = "This will permanently remove the item and its history.",
            title = "Delete item?",
            items = listOf(ProposedItemUiModel(id = "item-1", title = "First", subtitle = "12 rows")),
            severity = ActionButtonDefaults.ActionButtonRole.Destructive,
            reversibilityHint = "Irreversible",
            confirmLabel = "Delete",
            onConfirm = {},
            onCancel = {},
        )
        // Explorer@v2.5.1 VoiceCommandFamilyScreen.kt:491-503 (CT ProposedBatch shape): a shared
        // topLevelContent row and per-item onRemove.
        val batch = VoiceOutcomeUiState.NeedsConfirmation(
            reason = "3 items parsed - check the one marked Weak match.",
            items = listOf(
                ProposedItemUiModel(id = "1", title = "Apple", subtitle = "2 ct"),
                ProposedItemUiModel(id = "2", title = "Banana", subtitle = "1 bunch", confidenceCue = "Weak match", onRemove = {}),
                ProposedItemUiModel(id = "3", title = "Bread", subtitle = "1 loaf", amended = true, onRemove = {}),
            ),
            confirmLabel = "Confirm all (3)",
            topLevelContent = { Text("Logged for: Today") },
            onConfirm = {},
            onCancel = {},
        )
        // Explorer@v2.5.1 VoiceCommandFamilyScreen.kt:510-516: per-item trailingContent slot.
        val trailing = VoiceOutcomeUiState.NeedsConfirmation(
            reason = "Review this item before confirming.",
            items = listOf(ProposedItemUiModel(id = "tag-1", title = "Apple", trailingContent = { Text("tag") })),
            onConfirm = {},
            onCancel = {},
        )
        // SB feature/voicecommand/VoiceConfirmSheetModels.kt:29: every named parameter the SB
        // mapper passes, in the order it passes them.
        val sb = VoiceOutcomeUiState.NeedsConfirmation(
            reason = "reason",
            items = emptyList(),
            title = "title",
            severity = ActionButtonDefaults.ActionButtonRole.Destructive,
            reversibilityHint = "hint",
            confirmLabel = "Delete",
            cancelLabel = "Cancel",
            onConfirm = {},
            onCancel = {},
        )
        // The full named v2.5.1 set, selectionMode and cancelLabel included, no v2.6.0 fields.
        val full = VoiceOutcomeUiState.NeedsConfirmation(
            reason = "r",
            items = emptyList(),
            selectionMode = SelectionMode.PerItem,
            title = "t",
            severity = ActionButtonDefaults.ActionButtonRole.Save,
            reversibilityHint = "h",
            confirmLabel = "Confirm",
            cancelLabel = "Back",
            topLevelContent = { Text("top") },
            onConfirm = {},
            onCancel = {},
        )

        listOf(single, batch, trailing, sb, full).forEach {
            assertTrue("confirmEnabled must default to true", it.confirmEnabled)
            assertNull(it.titleActions)
        }
        assertEquals(SelectionMode.PerItem, full.selectionMode)
        assertEquals("Back", full.cancelLabel)
        assertEquals(3, batch.items.size)
    }

    // ------------------------------------------------------------------------------------------
    // HandledByUiModel and FailureActionUiModel
    // ------------------------------------------------------------------------------------------

    @Test
    fun handledBy_v251Shapes_keepTheirDefaults() {
        // CT voice/VoiceOutcomeMapper.kt:115: named tier, provider and model (approach skipped).
        val ct = HandledByUiModel(tier = "Cloud", provider = "P", model = "M")
        // SB feature/voicecommand/VoiceSheetModels.kt:47: named tier, approach, provider, model.
        val sb = HandledByUiModel(tier = "Handled by Local", approach = "Started with grammar", provider = "P", model = "M")
        // Explorer@v2.5.1 VoiceCommandFamilyScreen.kt:413-419: all five named fields.
        val explorer = HandledByUiModel(
            tier = "Cloud",
            approach = "Direct",
            provider = "OpenAI",
            model = "gpt-4",
            escalationCount = 1,
        )
        // Positional prefixes (1 to 6 arguments): OutcomeSheetSourceCompatV251Test
        // #v251HandledByShapes_compileAndDefaultTheNewFields. One more positional, four arguments:
        val four = HandledByUiModel("Local", "a", "p", "m")

        listOf(ct, sb, explorer, four).forEach {
            assertEquals("Handled by", it.prefixLabel)
            assertFalse(it.onDevice)
        }
        assertEquals("Escalations:", explorer.escalationsLabel)
        assertNull(ct.approach)
        assertEquals(1, explorer.escalationCount)
    }

    @Test
    fun failureAction_v251Shapes_keepTheirDefaults() {
        // CT voice/VoiceOutcomeMapper.kt:73 and SB feature/voicecommand/VoiceFailureSheetModels.kt:133:
        // named label and onClick.
        var clicked = 0
        val named = FailureActionUiModel(label = "Open Settings", onClick = { clicked++ })
        // Explorer@v2.5.1 VoiceCommandFamilyScreen.kt:426: named label, onClick = {}.
        val explorer = FailureActionUiModel(label = "Open Settings", onClick = {})
        // The trailing-lambda form FailureActionUiModel("l") { } and the positional role shape:
        // OutcomeSheetSourceCompatV251Test#v251FailureActionShapes_compileAndDefaultTheIcon.
        val trailing = FailureActionUiModel("l") { clicked++ }
        val withRole = FailureActionUiModel(label = "l", onClick = {}, role = ActionButtonDefaults.ActionButtonRole.Destructive)

        named.onClick()
        trailing.onClick()
        assertEquals(2, clicked)
        listOf(named, explorer, trailing, withRole).forEach { assertNull(it.icon) }
        assertEquals(ActionButtonDefaults.ActionButtonRole.Neutral, named.role)
        assertEquals(ActionButtonDefaults.ActionButtonRole.Destructive, withRole.role)
    }

    // ------------------------------------------------------------------------------------------
    // DynamicActionButton
    // ------------------------------------------------------------------------------------------

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun dynamicActionButton_v251Shapes_compile() {
        val onClick: () -> Unit = {}

        // SB feature/voice/VoiceClipsScreen.kt:735-740 (Cancel): named label, role, onClick, modifier.
        val sbCancel: @Composable () -> Unit = {
            DynamicActionButton(
                label = "Cancel",
                role = ActionButtonDefaults.ActionButtonRole.Neutral,
                onClick = onClick,
                modifier = Modifier,
            )
        }

        // SB feature/voice/VoiceClipsScreen.kt:742-749 (Save): named label, role, enabled, onClick, modifier.
        val sbSave: @Composable () -> Unit = {
            DynamicActionButton(
                label = "Save",
                role = ActionButtonDefaults.ActionButtonRole.Save,
                enabled = "a".trim() != "b".trim(),
                onClick = onClick,
                modifier = Modifier,
            )
        }

        // Explorer@v2.5.1 explorer/ButtonsFabFamilyScreen.kt:88-93 and :223-237: named label, role,
        // onClick (no modifier); the Disabled cell adds enabled = false.
        val explorerNamed: @Composable () -> Unit = {
            DynamicActionButton(
                label = "Save",
                role = ActionButtonDefaults.ActionButtonRole.Save,
                onClick = {},
            )
            DynamicActionButton(
                label = "Save",
                role = ActionButtonDefaults.ActionButtonRole.Save,
                enabled = false,
                onClick = {},
            )
        }

        // SB feature/settings/voiceagent/VoiceAgentProviderCards.kt:87: named label, role, enabled
        // computed from state, onClick named, trailing comma.
        val sbAgent: @Composable () -> Unit = {
            DynamicActionButton(
                label = "Save key",
                role = ActionButtonDefaults.ActionButtonRole.Save,
                enabled = "typed".isNotBlank() && !false,
                onClick = onClick,
            )
        }

        // Positional shapes the v2.5.1 signature allowed: label, role, onClick; then enabled and
        // modifier appended (the v2.5.1 parameter order was label, role, onClick, enabled, modifier).
        val positionalThree: @Composable () -> Unit = {
            DynamicActionButton("Go", ActionButtonDefaults.ActionButtonRole.Save, onClick)
        }
        val positionalFive: @Composable () -> Unit = {
            DynamicActionButton("Go", ActionButtonDefaults.ActionButtonRole.Destructive, onClick, true, Modifier)
        }
    }

    // ------------------------------------------------------------------------------------------
    // SegmentedOptionSelector
    // ------------------------------------------------------------------------------------------

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun segmentedOptionSelector_v251Shapes_compile() {
        var selectedIndex = 0
        val options = listOf("Option A", "Option B")

        // SB feature/voice/VoiceClipsScreen.kt:495-499: named selectedIndex, options, onSelect (no
        // modifier); the options list is derived from a map.
        val sb: @Composable () -> Unit = {
            SegmentedOptionSelector(
                selectedIndex = selectedIndex,
                options = listOf("en", "es").map { it.uppercase() },
                onSelect = { selectedIndex = it }
            )
        }

        // Explorer@v2.5.1 explorer/PickersFamilyScreen.kt:200-206 and :454-460: named selectedIndex,
        // options, onSelect, modifier last.
        val explorer: @Composable () -> Unit = {
            SegmentedOptionSelector(
                selectedIndex = selectedIndex,
                options = options,
                onSelect = { selectedIndex = it },
                modifier = Modifier,
            )
        }

        // Explorer@v2.5.1 explorer/PickersFamilyScreen.kt:214-222 (the Disabled cell): enabled and
        // disabledReason named between onSelect and modifier.
        val disabled: @Composable () -> Unit = {
            SegmentedOptionSelector(
                selectedIndex = 0,
                options = options,
                onSelect = {},
                enabled = false,
                disabledReason = "Unavailable right now",
                modifier = Modifier,
            )
        }

        // Explorer@v2.5.1 explorer/SheetsFamilyScreen.kt:563-568: a boolean-derived selectedIndex
        // and an onSelect that compares the index.
        var useSharedEditor = true
        val sheets: @Composable () -> Unit = {
            SegmentedOptionSelector(
                selectedIndex = if (useSharedEditor) 0 else 1,
                options = listOf("Shared editor", "Local rename"),
                onSelect = { useSharedEditor = it == 0 },
                modifier = Modifier,
            )
        }

        // Positional shapes of the v2.5.1 signature (selectedIndex, options, onSelect, modifier,
        // enabled, disabledReason).
        val positionalThree: @Composable () -> Unit = {
            SegmentedOptionSelector(0, options, { _: Int -> })
        }
        val positionalSix: @Composable () -> Unit = {
            SegmentedOptionSelector(0, options, { _: Int -> }, Modifier, false, "reason")
        }
    }

    // ------------------------------------------------------------------------------------------
    // SheetScaffold
    // ------------------------------------------------------------------------------------------

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun sheetScaffold_v251Shapes_compile() {
        val onDismiss: () -> Unit = {}

        // SB feature/voicecommand/ClarificationSheetContent.kt:61-64: onDismissRequest and
        // sheetState named, content as the trailing lambda (ColumnScope receiver in use).
        val sbSheetState: @Composable () -> Unit = {
            SheetScaffold(
                onDismissRequest = onDismiss,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            ) {
                Text("clarification")
            }
        }

        // SB feature/browse/CardTypeFilterSheet.kt:59: onDismissRequest named, trailing content
        // lambda on the same line.
        val sbBare: @Composable () -> Unit = {
            SheetScaffold(onDismissRequest = onDismiss) {
                Text("filter")
            }
        }

        // Explorer@v2.5.1 explorer/SheetsFamilyScreen.kt:798-803: onDismissRequest named with a
        // lambda that flips state, content lambda with a Modifier.
        var show = true
        val explorer: @Composable () -> Unit = {
            SheetScaffold(onDismissRequest = { show = false }) {
                Text(text = "Generic SheetScaffold content", modifier = Modifier)
            }
        }

        // Positional onDismissRequest followed by the trailing content lambda.
        val positional: @Composable () -> Unit = {
            SheetScaffold(onDismiss) { Text("positional") }
        }
    }

    // ------------------------------------------------------------------------------------------
    // ProviderKeyCard
    // ------------------------------------------------------------------------------------------

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun providerKeyCard_v251ConsumerShapes_compile() {
        // SB feature/settings/voiceagent/VoiceAgentProviderCards.kt:67-78: the seven required
        // parameters plus modifier, named, trailing comma, id-keyed provider callback.
        val sb: @Composable () -> Unit = {
            ProviderKeyCard(
                providers = listOf(ProviderOptionUiModel(id = "p", label = "P")),
                selectedProviderId = "p",
                onProviderSelected = { id -> id.length },
                keyValue = "typed",
                onKeyChange = { _: String -> },
                keyState = KeyFieldState.Entered,
                keyLabel = "Key",
                modifier = Modifier,
            )
        }

        // CT settings/SettingsScreen.kt:641-650: single provider, an empty provider callback
        // (onProviderSelected = {}), keyValue and onKeyChange from state, keyLabel from a title.
        val ct: @Composable () -> Unit = {
            ProviderKeyCard(
                providers = listOf(ProviderOptionUiModel(id = "OPENAI", label = "OpenAI")),
                selectedProviderId = "OPENAI",
                onProviderSelected = {},
                keyValue = "",
                onKeyChange = { _: String -> },
                keyState = KeyFieldState.Invalid("too short"),
                keyLabel = "OpenAI key",
                modifier = Modifier,
            )
        }

        // Explorer@v2.5.1 explorer/VoiceCommandFamilyScreen.kt:228-238: the Valid and Empty states
        // are covered by the sealed arms; Validating and Valid compile as arguments too.
        val explorer: @Composable () -> Unit = {
            ProviderKeyCard(
                providers = emptyList(),
                selectedProviderId = null,
                onProviderSelected = { _: String -> },
                keyValue = "",
                onKeyChange = { _: String -> },
                keyState = KeyFieldState.Valid,
                keyLabel = "API key",
                modifier = Modifier,
            )
            ProviderKeyCard(
                providers = emptyList(),
                selectedProviderId = null,
                onProviderSelected = { _: String -> },
                keyValue = "",
                onKeyChange = { _: String -> },
                keyState = KeyFieldState.Validating,
                keyLabel = "API key",
            )
        }
        // Positional shapes through emptyProvidersReason and providerLabel:
        // ProviderKeyCardSourceCompatV251Test (not copied).
    }

    // ------------------------------------------------------------------------------------------
    // UndoCenterScreen
    // ------------------------------------------------------------------------------------------

    /** Stand-in for a consumer view model: bound function references, as SB wires them. */
    private class UndoHolder {
        fun onUndo(id: String) {
            id.length
        }

        fun onClear() = Unit
    }

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun undoCenterScreen_v251Shapes_compile() {
        val holder = UndoHolder()

        // SB ui/navigation/AppNavGraph.kt:870-875: ALL FOUR arguments NAMED, onUndo and onClear
        // passed as bound function references (holder::onUndo, holder::onClear).
        val sb: @Composable () -> Unit = {
            UndoCenterScreen(
                entries = emptyList(),
                onNavigateBack = { },
                onUndo = holder::onUndo,
                onClear = holder::onClear
            )
        }

        // Explorer@v2.5.1 explorer/FeedbackFamilyScreen.kt:81-86 and :187-192: all four named, with
        // the empty-lambda form onUndo = {} (it binds as (String) -> Unit).
        val explorer: @Composable () -> Unit = {
            UndoCenterScreen(
                entries = emptyList(),
                onNavigateBack = {},
                onUndo = {},
                onClear = {}
            )
        }

        // Named entries, onNavigateBack and onUndo as a reference, onClear as the trailing lambda
        // (the named-then-trailing form; the two Wave 0 shapes are in
        // UndoCenterScreenSourceCompatV251Test and are not copied).
        val trailing: @Composable () -> Unit = {
            UndoCenterScreen(
                entries = emptyList(),
                onNavigateBack = {},
                onUndo = holder::onUndo
            ) { holder.onClear() }
        }
        // NOT here on purpose: UndoCenterScreen(entries, onNavigateBack, onUndo, onClear) positional.
    }

    // ------------------------------------------------------------------------------------------
    // Self-coverage: a deleted surface must fail this class
    // ------------------------------------------------------------------------------------------

    @Test
    fun canary_coversEveryD01Surface() {
        val text = SourceContractTestSupport.stripComments(selfSource().readText())
        val surfaces = listOf(
            "OutcomeSheet",
            "DynamicActionButton",
            "SegmentedOptionSelector",
            "SheetScaffold",
            "ProviderKeyCard",
            "UndoCenterScreen",
            "HandledByUiModel",
            "FailureActionUiModel",
            "VoiceOutcomeUiState.Success",
            "VoiceOutcomeUiState.Failure",
            "VoiceOutcomeUiState.NeedsConfirmation",
        )
        val missing = surfaces.filter { name ->
            !Regex("(?<![A-Za-z0-9_])" + Regex.escape(name) + "\\s*\\(").containsMatchIn(text)
        }
        assertTrue("The canary no longer calls: $missing", missing.isEmpty())

        // The shapes that make the canary consumer-verbatim, not generic.
        assertTrue("function-reference UndoCenterScreen", text.contains("onUndo = holder" + "::onUndo"))
        assertTrue("SheetScaffold trailing content lambda", text.contains("SheetScaffold" + "(onDismissRequest = onDismiss) {"))
        assertTrue("SheetScaffold sheetState shape", text.contains("sheetState = remember" + "ModalBottomSheetState"))
        assertFalse("the positional UndoCenterScreen break is excluded", Regex("UndoCenterScreen\\(\\s*[a-z]+\\s*,").containsMatchIn(text))
    }

    private fun selfSource(): File {
        val relative = "src/test/java/io/github/ygaray/yahirandroidtaste/V251CallSiteCanaryTest.kt"
        val direct = File(relative)
        if (direct.isFile) return direct
        var dir: File? = File(".").absoluteFile
        var depth = 0
        while (dir != null && depth < 8) {
            val candidate = File(dir, relative)
            if (File(dir, "build.gradle.kts").isFile && candidate.isFile) return candidate
            dir = dir.parentFile
            depth++
        }
        error("Could not resolve $relative from ${File(".").absoluteFile}")
    }
}
