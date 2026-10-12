package io.github.ygaray.yahirandroidtaste.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Structural same-code-path proof (P3, ROADMAP SC4) for [SegmentedOptionSelector]: a call with
 * `style = Material` and an empty `disabledOptions` must run the SAME G-15-1 Material block as
 * v2.5.1. The Pill guard has to sit AFTER the `require` and ABOVE the Material `Column` and return
 * early, and the Material block must equal the v2.5.1 text plus exactly one inserted line, the
 * per-segment `enabled` shadow directly after `val selected = index == selectedIndex`.
 *
 * Plain JUnit4 (no Robolectric): reads the main source via [SourceContractTestSupport], strips
 * comments and whitespace, and asserts on the function body.
 */
class SegmentedOptionMaterialParitySourceContractTest {

    private val stripped: String = SourceContractTestSupport.stripComments(
        SourceContractTestSupport.source("SegmentedOptionSelector.kt")
    )

    private fun body(): String =
        SourceContractTestSupport.functionBody(stripped, "fun SegmentedOptionSelector(")
            .filterNot { it.isWhitespace() }

    @Test
    fun `the require precedes the Pill guard which returns before the Material Column`() {
        val b = body()
        val require = b.indexOf("require(options.size==2){")
        val guard = b.indexOf("if(style==SegmentedOptionStyle.Pill){")
        val call = b.indexOf("PillSegmentedOptionSelector(", guard)
        val ret = b.indexOf("return", call)
        val column = b.indexOf(MATERIAL_COLUMN_START)
        assertTrue("require must exist", require >= 0)
        assertTrue("require ($require) must precede the Pill guard ($guard)", guard > require)
        assertTrue("guard must call PillSegmentedOptionSelector", call > guard)
        assertTrue("guard must return after the call", ret > call)
        assertTrue("Material Column must exist", column >= 0)
        assertTrue("return ($ret) must precede the Material Column ($column)", ret < column)
    }

    @Test
    fun `the Material block equals the v2_5_1 block plus exactly the per-segment enabled shadow`() {
        val b = body()
        val start = b.indexOf(MATERIAL_COLUMN_START)
        assertTrue("Material Column must exist", start >= 0)
        assertEquals(
            "Material block must be the v2.5.1 text plus the one shadow line",
            EXPECTED_MATERIAL_BLOCK,
            b.substring(start)
        )
        assertEquals(
            "the Material block must occur exactly once",
            1,
            SourceContractTestSupport.countOccurrences(b, MATERIAL_COLUMN_START)
        )
    }

    @Test
    fun `the G-15-1 helpers survive and no overload of the selector was added`() {
        val file = stripped.filterNot { it.isWhitespace() }
        assertTrue(
            "reserveSegmentIconSlot must survive verbatim",
            file.contains("privatefunModifier.reserveSegmentIconSlot():Modifier=layout{measurable,constraints->")
        )
        assertTrue(
            "SegmentedButtonIconSpacing must survive verbatim",
            file.contains("privatevalSegmentedButtonIconSpacing=8.dp")
        )
        assertEquals(
            "exactly one public SegmentedOptionSelector function (no overload, no shim)",
            1,
            SourceContractTestSupport.countOccurrences(file, "funSegmentedOptionSelector(")
        )
    }

    private companion object {
        const val MATERIAL_COLUMN_START =
            "Column(modifier=modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(4.dp)){"

        /**
         * v2.5.1 Material block, whitespace-stripped, with the one inserted shadow line.
         *
         * To update: this literal is intentionally brittle, since the point is that the Material
         * path stays the v2.5.1 text. If a deliberate edit to the Material block is approved, copy
         * the new block from `SegmentedOptionSelector.kt` (from the `Column(modifier = modifier
         * .fillMaxWidth(), ...)` line to the end of the function), strip all whitespace, and paste
         * it here.
         */
        const val EXPECTED_MATERIAL_BLOCK =
            "Column(modifier=modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(4.dp)){" +
                "SingleChoiceSegmentedButtonRow(modifier=Modifier.fillMaxWidth().height(IntrinsicSize.Min)){" +
                "options.forEachIndexed{index,label->valselected=index==selectedIndex" +
                "valenabled=enabled&&index!indisabledOptions" +
                "SegmentedButton(selected=selected,enabled=enabled,onClick={onSelect(index)}," +
                "shape=SegmentedButtonDefaults.itemShape(index=index,count=options.size)," +
                "colors=SegmentedButtonDefaults.colors()," +
                "modifier=Modifier.fillMaxHeight().semantics{contentDescription=" +
                "\"\$label,\${if(selected)\"selected\"else\"notselected\"}\"},){" +
                "Text(text=label,style=MaterialTheme.typography.labelLarge,maxLines=2," +
                "overflow=TextOverflow.Ellipsis,textAlign=TextAlign.Center," +
                "modifier=Modifier.reserveSegmentIconSlot(),)}}}" +
                "if(!enabled&&disabledReason!=null){Text(text=disabledReason," +
                "style=MaterialTheme.typography.labelLarge," +
                "color=MaterialTheme.colorScheme.onSurfaceVariant,maxLines=2," +
                "overflow=TextOverflow.Ellipsis,)}}}"
    }
}
