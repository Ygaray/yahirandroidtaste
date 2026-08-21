package io.github.ygaray.yahirandroidtaste.component

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Regression guard (SB Phase 113 Gate-1 finding 1, fixed in hub v1.1.3): the Voice rename+tags sheet
 * must gate its Save button on name-OR-tags dirty (EDIT-02), NOT leave it always-enabled.
 *
 * `NameAndTagsEditor` defaults `enabled = true`; before this fix `VoiceRenameTagsSheet` omitted the
 * `enabled` argument, so Voice's Save was enabled at rest (unlike Album/Text/List). This guard is
 * source-structural — the sheet uses a ModalBottomSheet the module's harness cannot drive — asserting
 * the `enabled` expression and the `tagsDirty` param are present so the always-on regression cannot
 * silently return. The rendered dirty-gate proof is discharged on-device at Phase 113's Gate-1.
 */
class VoiceRenameTagsSheetGateTest {

    private val src: String =
        File("src/main/java/io/github/ygaray/yahirandroidtaste/component/VoiceRenameTagsSheet.kt")
            .readText()

    @Test
    fun `VoiceRenameTagsSheet declares a tagsDirty param defaulting to false`() {
        assertTrue(
            "VoiceRenameTagsSheet must expose `tagsDirty: Boolean = false` so :app supplies the tags-dirty half",
            src.contains("tagsDirty: Boolean = false")
        )
    }

    @Test
    fun `VoiceRenameTagsSheet gates Save on name-OR-tags dirty, not always-enabled`() {
        assertTrue(
            "VoiceRenameTagsSheet must pass enabled = (title.trim() != defaultTitle) || tagsDirty to NameAndTagsEditor",
            src.contains("enabled = (title.trim() != defaultTitle) || tagsDirty")
        )
    }
}
