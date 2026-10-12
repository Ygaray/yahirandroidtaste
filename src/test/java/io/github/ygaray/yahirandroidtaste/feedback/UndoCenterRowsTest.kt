package io.github.ygaray.yahirandroidtaste.feedback

import org.junit.Assert.assertEquals
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
        assertEquals(listOf("x", "group-g", "m1", "m2"), keys)
        assertEquals(keys.size, keys.toSet().size)
    }
}
