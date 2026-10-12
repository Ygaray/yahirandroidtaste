package io.github.ygaray.yahirandroidtaste.feedback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pure tests for the [buildUndoCenterRows] seam and its text helpers (VUNDO-02). No Compose. */
class UndoCenterRowsTest {

    private fun entry(id: String, status: UndoStatus = UndoStatus.Available) =
        UndoCenterScreenGoldens.entry(id, "message $id", status)

    private fun group(
        id: String,
        vararg members: String,
        status: UndoGroupStatus = UndoGroupStatus.Undoable
    ) = UndoGroupRowUiModel(groupId = id, label = "label $id", memberIds = members.toList(), status = status)

    /** Compact, order-preserving description of a row list, so assertions read as a table. */
    private fun describe(rows: List<UndoCenterRow>): List<String> = rows.map { row ->
        when (row) {
            is UndoCenterRow.Group -> "G:${row.group.groupId}#${row.memberCount}"
            is UndoCenterRow.Entry -> (if (row.indented) "  " else "") + "E:${row.entry.id}"
        }
    }

    private fun rowsOf(entryIds: List<String>, vararg groups: UndoGroupRowUiModel): List<String> =
        describe(buildUndoCenterRows(entryIds.map { entry(it) }, groups.toList()))

    @Test
    fun `empty groups returns every entry once flat and not indented in the given order`() {
        val entries = listOf(entry("a"), entry("b"), entry("c"))
        val rows = buildUndoCenterRows(entries, emptyList())
        assertEquals(listOf("E:a", "E:b", "E:c"), describe(rows))
        assertEquals(entries, rows.map { (it as UndoCenterRow.Entry).entry })
    }

    @Test
    fun `an interleaved group is emitted at its first member followed by all its members`() {
        val entries = listOf(entry("x"), entry("m1"), entry("y"), entry("m2"))
        val rows = buildUndoCenterRows(entries, listOf(group("g", "m1", "m2")))
        assertEquals(listOf("E:x", "G:g#2", "  E:m1", "  E:m2", "E:y"), describe(rows))
    }

    @Test
    fun `row keys are unique and distinguish a header from an entry`() {
        val entries = listOf(entry("x"), entry("m1"), entry("m2"))
        val rows = buildUndoCenterRows(entries, listOf(group("g", "m1", "m2")))
        val keys = rows.map { undoCenterRowKey(it) }
        assertEquals(listOf("e:x", "g:g", "e:m1", "e:m2"), keys)
        assertEquals(keys.size, keys.toSet().size)
    }

    @Test
    fun `an entry id that looks like a header key does not collide with the header`() {
        val entries = listOf(entry("group-g"), entry("g:g"), entry("m1"))
        val rows = buildUndoCenterRows(entries, listOf(group("g", "m1")))
        val keys = rows.map { undoCenterRowKey(it) }
        assertEquals(4, rows.size)
        assertEquals(keys.size, keys.toSet().size)
    }

    @Test
    fun `two interleaved groups each render their own header once at their newest member`() {
        val rows = rowsOf(
            listOf("a1", "b1", "a2", "b2"),
            group("g1", "a1", "a2"),
            group("g2", "b1", "b2")
        )
        assertEquals(listOf("G:g1#2", "  E:a1", "  E:a2", "G:g2#2", "  E:b1", "  E:b2"), rows)
    }

    @Test
    fun `a group whose members are all absent from the entries emits nothing`() {
        val rows = rowsOf(listOf("x", "y"), group("ghost", "m1", "m2"))
        assertEquals(listOf("E:x", "E:y"), rows)
    }

    @Test
    fun `a group counts only the member ids that are present in the entries`() {
        val rows = rowsOf(listOf("m1", "m2", "z"), group("g", "m1", "ghost", "m2"))
        assertEquals(listOf("G:g#2", "  E:m1", "  E:m2", "E:z"), rows)
    }

    @Test
    fun `an id listed by two groups belongs to the first and keys stay unique`() {
        val entries = listOf(entry("m1"), entry("m2"))
        val rows = buildUndoCenterRows(entries, listOf(group("g1", "m1"), group("g2", "m1", "m2")))
        assertEquals(listOf("G:g1#1", "  E:m1", "G:g2#1", "  E:m2"), describe(rows))
        val keys = rows.map { undoCenterRowKey(it) }
        assertEquals(keys.size, keys.toSet().size)
    }

    @Test
    fun `a duplicate groupId resolves first wins and the second definition is ignored`() {
        val rows = rowsOf(listOf("m1", "m2"), group("g", "m1"), group("g", "m2"))
        assertEquals(listOf("G:g#1", "  E:m1", "E:m2"), rows)
    }

    @Test
    fun `a groupId equal to an entry id does not crash and keeps unique keys`() {
        val entries = listOf(entry("g"), entry("m1"))
        val rows = buildUndoCenterRows(entries, listOf(group("g", "m1")))
        assertEquals(listOf("E:g", "G:g#1", "  E:m1"), describe(rows))
        val keys = rows.map { undoCenterRowKey(it) }
        assertEquals(keys.size, keys.toSet().size)
    }

    @Test
    fun `undoAllVisible is true only for an undoable or partly resolved group with a callback`() {
        val callback: (String) -> Unit = {}
        assertTrue(undoAllVisible(UndoGroupStatus.Undoable, callback))
        assertTrue(undoAllVisible(UndoGroupStatus.PartiallyResolved, callback))
        assertFalse(undoAllVisible(UndoGroupStatus.FullyResolved, callback))
        assertFalse(undoAllVisible(UndoGroupStatus.Empty, callback))
        listOf(
            UndoGroupStatus.Undoable,
            UndoGroupStatus.PartiallyResolved,
            UndoGroupStatus.FullyResolved,
            UndoGroupStatus.Empty
        ).forEach { assertFalse("null callback hides Undo all for $it", undoAllVisible(it, null)) }
    }

    @Test
    fun `count and status texts follow the signed wording`() {
        assertEquals("1 item", groupCountText(1))
        assertEquals("0 items", groupCountText(0))
        assertEquals("3 items", groupCountText(3))
        assertEquals("Undoable", groupStatusText(UndoGroupStatus.Undoable))
        assertEquals("Partly resolved", groupStatusText(UndoGroupStatus.PartiallyResolved))
        assertEquals("Resolved", groupStatusText(UndoGroupStatus.FullyResolved))
        assertEquals(null, groupStatusText(UndoGroupStatus.Empty))
    }

    @Test
    fun `the subtitle joins count and status by a middle dot and omits the dot without a status text`() {
        assertEquals("2 items · Undoable", groupSubtitle(2, UndoGroupStatus.Undoable))
        assertEquals("1 item · Partly resolved", groupSubtitle(1, UndoGroupStatus.PartiallyResolved))
        assertEquals("2 items · Resolved", groupSubtitle(2, UndoGroupStatus.FullyResolved))
        assertEquals("2 items", groupSubtitle(2, UndoGroupStatus.Empty))
    }
}
