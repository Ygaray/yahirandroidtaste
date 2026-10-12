package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import io.github.ygaray.yahirandroidtaste.explorer.ComponentRegistry
import io.github.ygaray.yahirandroidtaste.explorer.ExplorerFamilies
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * VSHEET-01 contract for [SheetScaffold]'s `shape` parameter (D-01, D-02).
 *
 * The source-contract cases use no composition: they pin the declaration order (shape sits
 * directly before the trailing `content` lambda), the default (the very expression
 * `ModalBottomSheet` itself defaults to, so a defaulted call renders as in v2.5.1), the by-name
 * forwarding, and that no overload or shim exists.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SheetScaffoldShapeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val stripped: String = SourceContractTestSupport.stripComments(
        SourceContractTestSupport.source("SheetScaffold.kt")
    )

    private fun declaration(): String =
        SourceContractTestSupport.functionBody(stripped, "fun SheetScaffold(").filterNot { it.isWhitespace() }

    @Test
    fun `shape sits between contentWindowInsets and the trailing content lambda with the M3 default`() {
        val d = declaration()
        val expected = "contentWindowInsets:@Composable()->WindowInsets={BottomSheetDefaults.windowInsets}," +
            "shape:Shape=BottomSheetDefaults.ExpandedShape," +
            "content:@ComposableColumnScope.()->Unit)"
        assertTrue("declaration must contain the shape parameter in position, was: $d", d.contains(expected))
    }

    @Test
    fun `parameter names keep the documented order`() {
        val d = declaration()
        val names = listOf("onDismissRequest:", "modifier:", "sheetState:", "dragHandle:", "contentWindowInsets:", "shape:", "content:")
        val positions = names.map { d.indexOf(it) }
        positions.forEachIndexed { i, p -> assertTrue("missing ${names[i]}", p >= 0) }
        assertEquals("parameters out of order: $positions", positions.sorted(), positions)
    }

    @Test
    fun `shape is forwarded by name to ModalBottomSheet exactly once`() {
        val d = declaration()
        assertTrue(
            "ModalBottomSheet call must end its argument list with contentWindowInsets then shape, was: $d",
            d.contains("contentWindowInsets=contentWindowInsets,shape=shape)")
        )
        assertEquals(1, SourceContractTestSupport.countOccurrences(d, "shape=shape"))
    }

    @Test
    fun `exactly one SheetScaffold function exists so no overload or shim was added`() {
        val flat = stripped.filterNot { it.isWhitespace() }
        assertEquals(1, SourceContractTestSupport.countOccurrences(flat, "funSheetScaffold("))
    }

    @Test
    fun `gallery keeps SheetScaffold as a Sheets primitive and shows both demo buttons`() {
        val entry = ComponentRegistry.entries.first { it.name == "SheetScaffold" }
        assertEquals(ExplorerFamilies.SHEETS, entry.family)
        assertEquals(ComponentRegistry.Tier.PRIMITIVE, entry.tier)
        assertEquals(listOf("Default", "Pressed / Selected", "Disabled", "Focused"), entry.states.map { it.label })
        val content = checkNotNull(entry.content)
        assertNotNull(content)

        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                Column { content() }
            }
        }
        composeTestRule.waitForIdle()

        // The sheet window cannot be driven in this harness, so only the launcher buttons are asserted.
        composeTestRule.onNodeWithText("SheetScaffold — generic chrome").assertExists()
        composeTestRule.onNodeWithText("SheetScaffold — custom shape (24dp top corners)").assertExists()
    }
}
