# Phase 7: Chip-color slot - Pattern Map

**Mapped:** 2026-09-27
**Files analyzed:** 4 (all modified, no new files)
**Analogs found:** 4 / 4 (all self-analogs — this phase edits the canonical files in place; the
precedent for the *shape* of the edit comes from `CardBase.kt`'s existing `accent: Color?` param
and each target file's own prior additive-param phases)

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|---|---|---|---|---|
| `src/main/java/.../model/TagChipUiModel.kt` | model | transform (data-class field add) | same file, prior `jaccard: Double? = null` addition (lines 23-34) | exact — identical nullable-optional-field idiom |
| `src/main/java/.../component/AppChip.kt` | component | request-response (Composable render) | same file, prior `relatednessStrength`/`onDoubleClick` additive params (lines 85-109); `CardBase.kt`'s `accent: Color? = null` (line 140, 255) | exact — same file's own precedent for `when` precedence-ordering + `CardBase` for the `Color?`-slot convention itself |
| `src/main/java/.../component/TagChipWithContextMenu.kt` | component | request-response (Composable render, pass-through wrapper) | same file, prior `relatednessStrength`/`onDoubleClick` verbatim pass-through params (lines 77, 82, 94, 99) | exact — this file already has the exact "mirror AppChip's param, pass straight through" idiom |
| `src/main/java/.../component/CardTagRow.kt` | component | transform (model→chip mapper / auto-thread site) | same file, existing `tag.name`/`tag.id` threading into both chip branches (lines 96-119) | exact — same two-branch (`TagChipWithContextMenu` vs. plain `AppChip`) structure already threads other `tag.*` fields |

## Pattern Assignments

### `src/main/java/io/github/ygaray/yahirandroidtaste/model/TagChipUiModel.kt` (model, transform)

**Analog:** same file's own `jaccard` field (Phase 91).

**Existing nullable-optional-field pattern** (lines 23-35):
```kotlin
/**
 * [jaccard] (Phase 91 Plan 01, VISUAL-01/02/03) — populated ONLY by Related-mode chip sources
 * ...; every other producer leaves it `null`. Nullable, NOT a numeric default like [createdAt]'s
 * `0L`, because a real jaccard of `0.0` ... must not collide with "not applicable."
 */
data class TagChipUiModel(
    val id: String,
    val name: String,
    val occurrenceCount: Int,
    val createdAt: Long = 0L,
    val jaccard: Double? = null
)
```

**Apply:** append `val color: Color? = null` as a new trailing field (after `jaccard`), with a
KDoc block following the same "who populates it / who leaves it null / why nullable not
defaulted" structure. Needs `import androidx.compose.ui.graphics.Color` added to this file (not
currently imported here — confirmed via `CardBase.kt`/`AccentColorPicker.kt`'s identical import
line).

---

### `src/main/java/io/github/ygaray/yahirandroidtaste/component/AppChip.kt` (component, request-response)

**Analog:** same file's own `containerColor`/`contentColor`/`borderStroke` `when` blocks (lines
94-108), plus `CardBase.kt`'s `accent: Color?` slot for the parameter-naming/nullability
convention.

**Imports pattern** (lines 1-21) — no new import needed for `Color` itself; `AppChip.kt` does not
currently import `androidx.compose.ui.graphics.Color` and must add it:
```kotlin
import androidx.compose.ui.graphics.Color
```

**Existing `Color?`-slot convention** (`CardBase.kt` lines 101-108, 140, 255):
```kotlin
 * @param accent Optional per-card accent [Color] supplied by the caller (Phase 129 DS-02 D-01).
 *   ...this param only renders whatever [Color] it is handed.
...
    accent: Color? = null,
...
                                val spineColor = accent ?: neutralSpineColor
```
This is the exact precedent for `containerColorOverride: Color? = null` — hub renders the
supplied `Color` as-is, no contrast/mute computation, `?:` fallback to the existing default.

**Core precedence pattern to extend** (lines 89-108, the load-bearing `when` blocks):
```kotlin
val relatedness = if (!isSelected && relatednessStrength != null)
    relatednessVisual(relatednessStrength, MaterialTheme.colorScheme)
else null

val shape = RoundedCornerShape(8.dp)
val containerColor = when {
    isSelected -> MaterialTheme.colorScheme.secondaryContainer
    relatedness != null -> relatedness.containerColor
    else -> MaterialTheme.colorScheme.surface
}
val contentColor = when {
    isSelected -> MaterialTheme.colorScheme.onSecondaryContainer
    relatedness != null -> relatedness.contentColor
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}
val borderStroke = when {
    isSelected -> null
    relatedness != null -> relatedness.borderStroke
    else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
}
```
**Apply (per UI-SPEC precedence order):** add `containerColorOverride: Color? = null` to the
function signature (new trailing param, after `onDoubleClick`, matching the file's established
"new params always append at the end" convention seen in `relatednessStrength`/`onLongClick`/
`onDoubleClick`'s own addition history). Slot a new arm into `containerColor`'s `when` ONLY,
between the `relatedness != null` arm and the `else` arm:
```kotlin
val containerColor = when {
    isSelected -> MaterialTheme.colorScheme.secondaryContainer
    relatedness != null -> relatedness.containerColor
    containerColorOverride != null -> containerColorOverride
    else -> MaterialTheme.colorScheme.surface
}
```
Do NOT touch `contentColor`'s or `borderStroke`'s `when` blocks (D-01 / UI-SPEC precedence rows
3-4 — content color and border stay at resting-default regardless of override).

**KDoc pattern to follow** (matching the `relatednessStrength`/`onDoubleClick` doc style, lines
51-74): document the param with its precedence position explicitly stated ("Ignored when
`isSelected` or an active `relatednessStrength` wins... Defaults to `null`, so every existing
call site renders byte-identical to today").

---

### `src/main/java/io/github/ygaray/yahirandroidtaste/component/TagChipWithContextMenu.kt` (component, request-response pass-through)

**Analog:** same file's own `relatednessStrength`/`onDoubleClick` pass-through pattern.

**Existing pass-through pattern** (lines 61-67, 77, 82, 94, 99):
```kotlin
 * @param onDoubleClick Optional double-tap callback (Phase 106-02, TAG-02), forwarded verbatim to
 *   the underlying [AppChip]'s parameter of the same name — a straight passthrough, not wrapped in
 *   a lambda...
...
fun TagChipWithContextMenu(
    ...
    relatednessStrength: Float? = null,
    ...
    onDoubleClick: (() -> Unit)? = null
) {
    ...
    Box(modifier = modifier) {
        AppChip(
            label = label,
            isSelected = isSelected,
            onClick = onClick,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            relatednessStrength = relatednessStrength,
            onLongClick = { ... },
            onDoubleClick = onDoubleClick
        )
```
**Apply:** add `containerColorOverride: Color? = null` as a new trailing param (after
`onDoubleClick`) and pass it straight through into the nested `AppChip(...)` call
(`containerColorOverride = containerColorOverride`), exactly like `onDoubleClick`'s treatment —
no wrapping lambda needed since it's a plain value, not a callback. Needs
`import androidx.compose.ui.graphics.Color` added.

---

### `src/main/java/io/github/ygaray/yahirandroidtaste/component/CardTagRow.kt` (component, transform / auto-thread)

**Analog:** same file's existing `tag.name`/`tag.id` threading into both visible-chip branches
(lines 96-119).

**Existing two-branch threading pattern** (lines 94-119):
```kotlin
val visible = tags.take(2)
val overflow = tags.size - 2
visible.forEach { tag ->
    if (hasCapability) {
        TagChipWithContextMenu(
            label = tag.name,
            isSelected = false,
            onClick = { onTagClick(tag.id) },
            modifier = ...,
            onEdit = onTagEdit?.let { edit -> { edit(tag.id) } },
            onRemoveFromContext = onTagRemoveFromCard?.let { remove -> { remove(tag.id) } },
            onDelete = onTagDelete?.let { delete -> { delete(tag.id, tag.name) } },
            removeLabel = removeFromCardLabel
        )
    } else {
        AppChip(
            label = tag.name,
            isSelected = false,
            onClick = { onTagClick(tag.id) },
            modifier = ...
        )
    }
}
```
**Apply:** thread `containerColorOverride = tag.color` into BOTH branches' calls (both
`TagChipWithContextMenu(...)` and the plain `AppChip(...)` in the `else` branch). The "+N"
overflow `AppChip` call (lines 125-136) is NOT touched — it has no backing `TagChipUiModel`, stays
theme-default per CONTEXT.md/UI-SPEC.

---

## Shared Patterns

### Nullable `Color?` slot, rendered as-is, no hub-side computation
**Source:** `src/main/java/.../component/CardBase.kt` lines 101-108, 140, 255 (`accent: Color?`)
**Apply to:** `AppChip.kt`, `TagChipWithContextMenu.kt` — the hub never computes contrast/muting
on the supplied `Color`; it only accepts and (conditionally) renders it, with `?:`/`when`-arm
fallback to the pre-existing default when `null`.

### Additive-optional-param-with-default, always appended, always byte-identical-when-null
**Source:** `AppChip.kt`'s own history (`relatednessStrength`, `onLongClick`, `onDoubleClick` —
lines 85-87) and `TagChipWithContextMenu.kt`'s mirrored history (lines 77-82)
**Apply to:** all three signature edits (`TagChipUiModel.color`, `AppChip.containerColorOverride`,
`TagChipWithContextMenu.containerColorOverride`) — new param goes at the end of the parameter
list, defaults to `null`, and every existing call site (untouched) must keep compiling and
rendering identically.

### Parity-test idiom: Robolectric + Compose, assert default-null path is byte-identical
**Source:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/AppChipTest.kt` — full
file (confirmed as the reusable idiom named in 07-CONTEXT.md)
```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppChipTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `default parameters - constructing with only the three required args compiles and is clickable`() {
        var clickCount = 0

        composeTestRule.setContent {
            AppChip(
                label = "Work",
                isSelected = false,
                onClick = { clickCount++ }
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Work").performClick()
        composeTestRule.waitForIdle()

        assertEquals(
            "A chip constructed with only its three required arguments must remain clickable," +
                " byte-identical to pre-phase AppChip",
            1,
            clickCount
        )
    }
}
```
**Apply to:** the new tests for `containerColorOverride`. This class disclaims pixel-golden
testing explicitly (see class KDoc, lines 16-26) — the new tests should assert *behavior*
(e.g. semantic-tree color reads via Compose test API, or structural assertions on which
`when`-arm fired) rather than pixel comparison. Needed test cases per UI-SPEC's UI Considerations
table: (1) `containerColorOverride` non-null + `isSelected == true` → override ignored; (2)
non-null override + active `relatednessStrength` + `!isSelected` → override ignored; (3) null
override → byte-identical resting render (regression floor); (4) non-null override in the plain
resting branch → override renders. Use `@RunWith(RobolectricTestRunner::class)` /
`@Config(sdk = [35])` / bare `createComposeRule()`, no theme wrapper, matching this file's
established convention exactly.

## No Analog Found

None — all four target files are themselves the best precedent for their own edit (self-analog),
and `CardBase.kt` supplies the cross-cutting `Color?`-slot convention. No file in this phase lacks
a close match.

## Metadata

**Analog search scope:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/`,
`src/main/java/io/github/ygaray/yahirandroidtaste/model/`,
`src/test/java/io/github/ygaray/yahirandroidtaste/component/`
**Files scanned:** `AppChip.kt`, `TagChipWithContextMenu.kt`, `CardTagRow.kt`,
`TagChipUiModel.kt`, `AppChipTest.kt`, `CardBase.kt`, `AccentColorPicker.kt` (all confirmed
git-tracked via `git ls-files`)
**Pattern extraction date:** 2026-09-27
