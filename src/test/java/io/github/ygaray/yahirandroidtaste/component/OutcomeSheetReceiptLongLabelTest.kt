package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.ygaray.yahirandroidtaste.model.FailureActionUiModel
import io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Long localized strings at 360dp for the Receipt frame (the "test localized long strings before
 * signoff" lesson): both pinned bar buttons stay inside the window with no ellipsized label line,
 * and a very long title wraps to at most two lines and ends ellipsized. NATIVE graphics gives real
 * font metrics; geometry is read UNCLIPPED; one `setContent` per test method; no pixel capture.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OutcomeSheetReceiptLongLabelTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val longCancel = "Vorgang abbrechen und alle Änderungen verwerfen"
    private val longConfirm = "Alle ausgewählten Einträge endgültig löschen"
    private val longTitle =
        "Außergewöhnlich lange Überschrift für die Bestätigung mehrerer Einträge, " +
            "die unmöglich in zwei Zeilen auf ein schmales Telefon passt"

    private fun setContent(outcome: VoiceOutcomeUiState) {
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                Box(Modifier.width(360.dp).height(800.dp)) { OutcomeSheetReceiptContent(outcome) }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun windowWidthPx(): Float = 360f * composeTestRule.density.density

    private fun windowHeightPx(): Float = 800f * composeTestRule.density.density

    private fun tagged(tag: String): SemanticsNodeInteraction =
        composeTestRule.onNodeWithTag(tag, useUnmergedTree = true)

    private fun SemanticsNodeInteraction.textLayout(): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        val action = fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action
        checkNotNull(action) { "node has no GetTextLayoutResult action" }.invoke(results)
        return results.single()
    }

    private fun assertInsideWindow(node: SemanticsNodeInteraction, label: String) {
        val n = node.fetchSemanticsNode()
        val left = n.positionInRoot.x
        val right = left + n.size.width
        assertTrue("$label $left..$right must lie inside 0..${windowWidthPx()}", left >= -1f && right <= windowWidthPx() + 1f)
        assertTrue("$label must have a visible width, was ${n.size.width}", n.size.width > 0)
    }

    private fun assertNoEllipsizedLine(label: String, text: String) {
        val layout = composeTestRule.onNodeWithText(text, useUnmergedTree = true).textLayout()
        (0 until layout.lineCount).forEach { line ->
            assertFalse("$label line $line must not be ellipsized", layout.isLineEllipsized(line))
        }
    }

    @Test
    fun `long German bar labels keep both buttons inside the window and unellipsized`() {
        setContent(OutcomeFixtures.needsConfirmation(cancelLabel = longCancel, confirmLabel = longConfirm))

        assertInsideWindow(tagged("outcome_sheet_confirmation_cancel"), "cancel button")
        assertInsideWindow(tagged("outcome_sheet_confirmation_confirm"), "confirm button")
        assertNoEllipsizedLine("cancel label", longCancel)
        assertNoEllipsizedLine("confirm label", longConfirm)

        val bar = tagged("outcome_sheet_action_bar").fetchSemanticsNode()
        val barBottom = bar.positionInRoot.y + bar.size.height
        assertTrue("bar bottom $barBottom must be inside the window height ${windowHeightPx()}", barBottom <= windowHeightPx() + 1f)
    }

    @Test
    fun `short bar labels are not ellipsized`() {
        setContent(OutcomeFixtures.needsConfirmation())

        assertNoEllipsizedLine("cancel label", "Cancel")
        assertNoEllipsizedLine("confirm label", "Confirm")
    }

    @Test
    fun `a very long title wraps to two lines and ends ellipsized`() {
        setContent(OutcomeFixtures.needsConfirmation(title = longTitle))

        val layout = composeTestRule.onNodeWithText(longTitle, useUnmergedTree = true).textLayout()
        assertEquals("title line count", 2, layout.lineCount)
        assertTrue("the last title line must be ellipsized", layout.isLineEllipsized(1))

        val style = layout.layoutInput.style
        assertEquals("title font size", 18.sp, style.fontSize)
        assertEquals("title line height", 24.sp, style.lineHeight)
        assertEquals("title font weight", FontWeight.SemiBold, style.fontWeight)
    }

    // ---- Receipt Success (22-04, T-22-06) -------------------------------------------------------

    private val germanPrefix = "Bearbeitet von"
    private val germanTier = "Verarbeitung auf dem Gerät ohne Netzwerkverbindung"
    private val germanProvider = "Lokale Grammatik offline"
    private val longUndoAction = "Änderungen rückgängig machen"
    private val longSaveAction = "Alle Einträge endgültig speichern"

    @Test
    fun `a long German pill stays inside the window with its dot visible and its text unellipsized`() {
        setContent(
            OutcomeFixtures.success(
                handledBy = OutcomeFixtures.handledBy(
                    prefixLabel = germanPrefix,
                    tier = germanTier,
                    provider = germanProvider,
                    onDevice = true
                )
            )
        )

        val pill = tagged("outcome_sheet_handled_by").fetchSemanticsNode()
        val pillRight = (pill.positionInRoot.x + pill.size.width) / composeTestRule.density.density
        assertTrue("pill right edge $pillRight dp must be at most 344dp", pillRight <= 344f + 1f)
        assertInsideWindow(tagged("outcome_sheet_handled_by_dot"), "pill dot")
        assertNoEllipsizedLine("pill text", "$germanPrefix $germanTier · $germanProvider")
    }

    @Test
    fun `long German Success actions stay inside the window and unellipsized`() {
        setContent(
            OutcomeFixtures.success(
                startAction = OutcomeFixtures.sheetAction(label = longUndoAction),
                endAction = OutcomeFixtures.sheetAction(label = longSaveAction)
            )
        )

        assertInsideWindow(tagged("outcome_sheet_action_start"), "start action")
        assertInsideWindow(tagged("outcome_sheet_action_end"), "end action")
        assertNoEllipsizedLine("start label", longUndoAction)
        assertNoEllipsizedLine("end label", longSaveAction)
    }

    @Test
    fun `a 120 character German Success summary wraps to at most two lines`() {
        val summary = "Drei Einträge wurden gespeichert und die Zusammenfassung ist bewusst sehr lang formuliert, " +
            "damit sie auf einem schmalen Telefon umbrechen muss"
        assertTrue("fixture must be at least 120 characters, was ${summary.length}", summary.length >= 120)
        setContent(OutcomeFixtures.success(summary = summary))

        val layout = tagged("outcome_sheet_title").textLayout()
        assertTrue("summary line count ${layout.lineCount} must be at most 2", layout.lineCount <= 2)
    }

    // ---- Receipt Failure (22-05, T-22-06) -------------------------------------------------------

    private val longRetryAction = "Erneut versuchen und Aufnahme wiederholen"
    private val longCloseAction = "Schließen und Vorgang abbrechen"

    @Test
    fun `long German Failure bar labels stay inside the window and unellipsized`() {
        setContent(
            OutcomeFixtures.failure(
                action = FailureActionUiModel(label = longRetryAction, onClick = {}),
                secondaryAction = OutcomeFixtures.sheetAction(label = longCloseAction)
            )
        )

        assertInsideWindow(tagged("outcome_sheet_action_end"), "failure action")
        assertInsideWindow(tagged("outcome_sheet_action_start"), "failure secondary")
        assertNoEllipsizedLine("failure action label", longRetryAction)
        assertNoEllipsizedLine("failure secondary label", longCloseAction)

        val bar = tagged("outcome_sheet_action_bar").fetchSemanticsNode()
        val barBottom = bar.positionInRoot.y + bar.size.height
        assertTrue("bar bottom $barBottom must be inside the window height ${windowHeightPx()}", barBottom <= windowHeightPx() + 1f)
    }
}
