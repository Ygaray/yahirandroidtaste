package io.github.ygaray.yahirandroidtaste.model

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-JVM (no Robolectric) tests for [TagChipUiModel]'s deliberately-chosen `color` semantics
 * (Phase 07 / TAGCOLOR-01, re-shaped 2026-09-27 per the operator's ABI ruling — see
 * `07-01-SUMMARY.md` Deviations and this class's own KDoc). `color` is declared as a mutable body
 * `var` OUTSIDE the primary constructor specifically so it is excluded from the compiler-synthesized
 * `equals()`/`hashCode()`/`copy()`/`componentN()` — these tests lock that semantic in as a real,
 * failing-if-violated regression, since it was previously only documented, never test-enforced
 * (07-REVIEW-02.md: "no call site... invokes `.copy(...)`" on this model anywhere in this repo).
 */
class TagChipUiModelTest {

    private fun baseModel(color: Color? = null) = TagChipUiModel(
        id = "tag-1",
        name = "Kotlin",
        occurrenceCount = 3,
        createdAt = 100L,
        jaccard = 0.5
    ).apply { this.color = color }

    // ---- equals()/hashCode() exclude color ----

    @Test
    fun `two instances differing only in color are still equal`() {
        val a = baseModel(color = Color(0xFF6750A4))
        val b = baseModel(color = Color(0xFF00FF00))

        assertEquals(a, b)
    }

    @Test
    fun `two instances differing only in color have the same hashCode`() {
        val a = baseModel(color = Color(0xFF6750A4))
        val b = baseModel(color = null)

        assertEquals(a.hashCode(), b.hashCode())
    }

    // ---- copy() never carries color forward ----

    @Test
    fun `copy() never carries color forward, even when the source instance has a non-null color`() {
        val original = baseModel(color = Color(0xFF6750A4))

        val copied = original.copy(name = "Compose")

        assertNull("copy() must not propagate the source's color", copied.color)
    }

    @Test
    fun `copy() of a model whose color is already null stays null`() {
        val original = baseModel(color = null)

        val copied = original.copy(occurrenceCount = 9)

        assertNull(copied.color)
    }

    // ---- Companion.of(...) factory actually sets color ----

    @Test
    fun `Companion of() constructs a model with the given color set`() {
        val expected = Color(0xFF123456)

        val model = TagChipUiModel.of(
            id = "tag-2",
            name = "Compose",
            occurrenceCount = 1,
            createdAt = 0L,
            jaccard = null,
            color = expected
        )

        assertEquals(expected, model.color)
    }

    @Test
    fun `Companion of() defaults color to null when omitted`() {
        val model = TagChipUiModel.of(
            id = "tag-3",
            name = "Java",
            occurrenceCount = 2
        )

        assertNull(model.color)
    }

    @Test
    fun `Companion of() still populates the base fields identically to the primary constructor`() {
        val viaOf = TagChipUiModel.of(
            id = "tag-4",
            name = "Rust",
            occurrenceCount = 5,
            createdAt = 42L,
            jaccard = 0.75,
            color = Color(0xFFABCDEF)
        )

        val viaCtor = TagChipUiModel(
            id = "tag-4",
            name = "Rust",
            occurrenceCount = 5,
            createdAt = 42L,
            jaccard = 0.75
        )

        // equals() ignores color, so this also confirms the base fields match exactly.
        assertEquals(viaCtor, viaOf)
        assertTrue(viaOf.color != null)
    }
}
