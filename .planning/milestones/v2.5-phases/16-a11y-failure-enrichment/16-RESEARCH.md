# Phase 16: A11y + Failure enrichment - Research

**Researched:** 2026-10-05
**Domain:** Additive Compose semantics + additive data-class evolution in a Jetpack Compose design-system library (Kotlin 2.3.20, Compose BOM 2026.04.01, AGP 9.2.1) under a Metalava `apiCheck` gate
**Confidence:** HIGH (every central claim below was read in source this session or reproduced empirically in a scratch copy of the repo outside the working tree; the one TalkBack-behavior claim is tagged `[ASSUMED]`)

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- **D-01 [selected-semantics]:** `selected = (rung.id == maxTierId)` — announce the rung the user actually chose as the cap, NOT `isCapped` (which is the excluded, above-cap rows). Resolution (human); corrects the brief's shorthand wording. Mirrors `PresetChip`'s `selected = isSelected`. — **Reversibility:** reversible — semantics-only, internal.
- **D-02 [failure-a11y]:** `Failure.semanticsPrefix` is announced by setting a combined `contentDescription` (prefix + reason) on the error surface's merged semantics node. Resolution (human). SB-175 is the confirming consumer and validates the announcement shape at integration.
- **D-03 [role-scope]:** Apply `Role.RadioButton` + `selected` to rungs only when the cap is actually selectable (`onMaxTierChange != null`) — a cap-less ladder must not announce non-interactive rows as an empty radio group. _(source: ai-auto)_
- **D-04 [min-size-visual]:** Add `Modifier.minimumInteractiveComponentSize()` to the per-rung interactive wrapper (`CapControl`'s Row); accept the slight row-height growth on short rows as standard a11y, and flag it as a Gate-2 visual check (brief §6 Q4). _(source: ai-auto)_

### Claude's Discretion
- Failure `body` slot placement inside the error surface Column (recommended: after `handledBy`/reason, before the optional action, so it rides the existing scroll region). Whether a `liveRegion` is added is left open for SB-175's a11y validation.

### Deferred Ideas (OUT OF SCOPE)
None — discussion stayed within phase scope.
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| VA11Y-01 | `ApproachLadderCard` rung rows enforce a minimum interactive size and expose selected semantics for the capped rung. Internal + semantics only. | `CapControl` is the single seam; `selectable(selected, role = Role.RadioButton)` + `minimumInteractiveComponentSize()` gated on `onClick != null` reproduced green (semantics tree printed). Falsifiable test shape found (touch-bounds overlap / row pitch) — `assertTouchHeightIsEqualTo(48.dp)` is NOT falsifiable. |
| VFAIL-01 | `FailureActionUiModel.role: ActionButtonRole = Neutral`, wired to the Failure action button | Plain field append FAILS `apiCheck` (RemovedMethod on ctor/`copy`); `@JvmOverloads constructor` + hand-written old-arity `copy` reproduced GREEN. Wire `role = action.role` at `OutcomeSheet.kt:305`. |
| VFAIL-02 | `Failure.body: (@Composable () -> Unit)? = null` rendered inside the error surface | Same recipe; render `failure.body?.invoke()` after `HandledByRow`, before the action. Reproduced green. |
| VFAIL-03 | `Failure.semanticsPrefix: String? = null` for the accessibility announcement | `Modifier.semantics(mergeDescendants = true) { contentDescription = … }` on the error `Surface`; semantics tree printed and asserted. TalkBack caveat below. |
</phase_requirements>

## Summary

Phase 16 is two small, well-bounded edits plus one API-evolution trap that Phase 15 already mapped. The a11y half (VA11Y-01) is entirely internal to `ApproachLadderCard.kt`: `RungRow` gains a private `isSelected` param and `CapControl` gains a private `selected` param; when `onClick != null` the Row swaps `Modifier.clickable(onClick = …)` for `Modifier.selectable(selected, role = Role.RadioButton, onClick)` and adds `minimumInteractiveComponentSize()`. When `onClick == null` (cap-less ladder) it stays exactly as today (no role, no `selected`, no min-size) — which satisfies D-03 and the library's "conditional-render-no-dead-space" convention. No public signature changes, so `api.txt` is untouched by this half.

The Failure half (VFAIL-01..03) appends one defaulted field to `FailureActionUiModel` and two to `VoiceOutcomeUiState.Failure`. **Both are `data class`es**, and Phase 15's critical finding applies unchanged: a naive field append makes `apiCheck` red (`Removed constructor` / `Removed method …copy`). The proven recipe — `@JvmOverloads constructor` plus a hand-written old-arity `copy` that delegates to the generated full-arity `copy` — was reproduced green again this session for both classes (`apiCheck`, `detekt`, and the existing `ApproachLadderCardTest`/`OutcomeSheetTest`/`VoiceModelLabelDefaultsTest` all green; `apiDump` diff shown below). The retained old constructor line means the only removed raw `api.txt` lines are the two old `copy(optional …)` lines, so the raw-line guard still reports lane 3 and every commit continues to need `HUB_LANE_OVERRIDE=3` (same as Phase 15).

Three planner-relevant surprises, all measured: (1) Compose already *expands touch bounds to 48dp* for any clickable node, so `assertTouchHeightIsEqualTo(48.dp)` passes with or without `minimumInteractiveComponentSize()` — the real pre-fix defect is that adjacent rungs' touch targets **overlap** (14–62 vs 59.5–107.5dp), so the test must assert non-overlap / row pitch. (2) Robolectric here cannot assert button color, so VFAIL-01's role wiring cannot be proven by a rendered-state test (Save/Neutral/Destructive buttons all measure 58x52); use the repo's source-contract idiom (`SourceContractTestSupport`) plus a model-default test. (3) `mergeDescendants` + `contentDescription` on the failure surface yields *both* `ContentDescription=[prefix reason]` and `Text=[reason, body…]` in the merged node; whether TalkBack speaks both is `[ASSUMED]` and is exactly what D-02 hands to SB-175.

**Primary recommendation:** Implement VA11Y-01 inside `CapControl` (gated on `onClick != null`), implement VFAIL-01..03 with `@JvmOverloads constructor` + legacy-arity `copy` on both data classes, `apiDump` in the same commit as each model change, gate on `./gradlew apiCheck`, commit with `HUB_LANE_OVERRIDE=3`, and test with the falsifiable shapes in the Validation Architecture.

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Rung min touch size + selected/role semantics | Library composable (`component/ApproachLadderCard.kt` `CapControl`) | — | Presentational; `CapControl` already owns the single merged-semantics node (D-03 swap seam). No consumer involvement. |
| Which rung is "selected" | Consumer (supplies `maxTierId`) | Library derives `rung.id == maxTierId` | Library displays + emits only; it never decides the cap. |
| Failure action role choice | Consumer (sets `FailureActionUiModel.role`) | Library renders via `DynamicActionButton` | Retry-safety/severity is a consumer decision (VOUT-03 stance). |
| Failure body content | Consumer (supplies `@Composable` slot) | Library hosts it inside the error `Surface` | Same shape as `Success.editableContent` — library defines no content. |
| Failure announcement prefix | Consumer (supplies localized string, INV-01) | Library merges into `contentDescription` | Hub localizes nothing; caller passes the string. |
| Additive-API enforcement | Build tooling (Metalava `apiCheck`, `tools/*.sh`, pre-commit) | — | Phase 15 D-01: `apiCheck` is the authoritative additive gate. |

## Standard Stack

No new libraries. `## Package Legitimacy Audit`: N/A — no external packages are installed; all APIs used already ship in the module's existing dependencies (Compose foundation/material3/ui).

### Core (existing; versions from project CLAUDE.md)
| Tool | Version | Purpose |
|------|---------|---------|
| AGP / Kotlin / Compose BOM | 9.2.1 / 2.3.20 / 2026.04.01 | build [VERIFIED: /home/yahir/Projects/Reusable/android/yahirandroidtaste/CLAUDE.md "AGP **9.2.1** / Kotlin **2.3.20** / Hilt **2.60.1** / Compose BOM **2026.04.01**"] |
| Metalava Gradle plugin | 0.5.0 | `apiDump`/`apiCheck` [VERIFIED: 15-RESEARCH.md citing gradle/libs.versions.toml:46; `build.gradle.kts:33-43` registers `apiDump` -> `metalavaGenerateSignatureRelease`, `apiCheck` -> `metalavaCheckCompatibilityRelease`] |
| Robolectric / Compose test | 4.16.1 [VERIFIED: gradle/libs.versions.toml:19 `robolectric = "4.16.1"`] | Compose UI tests, `@Config(sdk = [35])` |

### Compose APIs used (all already imported elsewhere in the repo)
| API | Where precedent exists |
|-----|------------------------|
| `androidx.compose.material3.minimumInteractiveComponentSize` | `PresetChip.kt:94`, `AppChip.kt:128` |
| `androidx.compose.foundation.selection.selectable` | new import (foundation, already a dependency) |
| `androidx.compose.ui.semantics.Role` | `PresetChip.kt:100` uses `Role.Button` |
| `androidx.compose.ui.semantics.contentDescription` (SemanticsPropertyReceiver) | new import in `OutcomeSheet.kt` |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| `selectable(selected, role=RadioButton, onClick)` | `clickable(role = Role.RadioButton)` + `.semantics { selected = … }` | Equivalent semantics; `selectable` is one modifier and is what M3 `RadioButton` itself uses. Reproduced: tree shows `Role = 'RadioButton'`, `Selected = 'true'`, `OnClick` action. Use `selectable`. |
| `clearAndSetSemantics { contentDescription }` for the prefix | `semantics(mergeDescendants = true)` | `clearAndSetSemantics` would delete child semantics incl. the action button's click node. **Do not use.** |
| Body-`var` (TagChipUiModel idiom) | `@JvmOverloads` + legacy `copy` | Body `var` loses all-`val` Compose stability (documented in `TagChipUiModel`); the legacy-`copy` recipe is already the Phase 15 standard. |

**Installation:** none.
**Version verification:** no packages added; toolchain versions are read from CLAUDE.md/libs.versions.toml, not from training data.

## Architecture Patterns

### System Architecture Diagram

```
Consumer (SB-175 / future apps)
  |  builds props: ladder + maxTierId + onMaxTierChange ;  VoiceOutcomeUiState.Failure(reason, handledBy, action{label,onClick,role}, body, semanticsPrefix)
  v
ApproachLadderCard ------------------------------+          OutcomeSheet / OutcomeSheetContent
  | derives per rung:                              |            |  scroll region (weight(1f, fill=false))
  |   isSelected = onMaxTierChange != null         |            v
  |                && rung.id == maxTierId   (D-01/D-03)      FailureBody(failure)
  v                                                |            |  Surface(errorContainer) .semantics(merge){ contentDescription = prefix + reason }  (iff prefix non-blank)
RungRow(isSelected, ...)  --- private ---          |            |   Column:
  v                                                |            |     Text(reason)
CapControl(onClick, selected)  <- single seam      |            |     HandledByRow?          (unchanged)
  onClick == null  -> plain merged Row (unchanged) |            |     failure.body?.invoke()  <- NEW slot (VFAIL-02)
  onClick != null  -> minimumInteractiveComponent  |            |     DynamicActionButton(role = action.role) <- was hardcoded Neutral (VFAIL-01)
        Size() + semantics(merge) + selectable(    |            v
        selected, role = RadioButton, onClick)     |         TalkBack / a11y services read merged node
  v                                                |
a11y tree: N radio-button rows, exactly one Selected (or none if maxTierId stale)
```

### Recommended change set (no new files in `src/main`)
```
src/main/java/io/github/ygaray/yahirandroidtaste/
├── component/ApproachLadderCard.kt   # RungRow(+isSelected) / CapControl(+selected): selectable + minInteractive (private)
├── component/OutcomeSheet.kt         # FailureBody: role = action.role ; body slot ; semanticsPrefix semantics
├── model/FailureActionUiModel.kt     # @JvmOverloads ctor + role field + legacy copy(label,onClick)
├── model/VoiceOutcomeUiState.kt      # Failure: @JvmOverloads ctor + body + semanticsPrefix + legacy copy(reason,handledBy,action)
api.txt                               # regenerated via ./gradlew apiDump in the SAME commit as each model change
```
Tests go in existing files (`ApproachLadderCardTest.kt`, `OutcomeSheetTest.kt`, `model/VoiceModelLabelDefaultsTest.kt`, `component/VoiceI18nSourceCompatTest.kt` or a sibling compat fixture) plus an optional new source-contract test.

### Pattern 1: Rung semantics + min-size inside `CapControl` (VA11Y-01)
**What:** Thread a private `isSelected` from `ApproachLadderCard` → `RungRow` → `CapControl`; gate every new modifier on `onClick != null`.
**Why gate on `onClick`:** D-03 says no radio announcement on a cap-less ladder; `onClick == null` is exactly that case (`onMaxTierChange == null`, enforced by the existing `require` pairing). Inflating non-interactive rows to 48dp would also be pure dead space.
**Example (reproduced green in scratch: `detekt`, `apiCheck`, `ApproachLadderCardTest`, `OutcomeSheetTest`):**
```kotlin
// ApproachLadderCard.kt — inside ladder.forEach { rung -> ... }
RungRow(
    rung = rung,
    isSelected = onMaxTierChange != null && rung.id == maxTierId,   // D-01 + D-03
    isEffective = isEffective,
    /* ...rest unchanged... */
)

// RungRow: add `isSelected: Boolean` (private; non-default) and pass `selected = isSelected` to CapControl.

@Composable
private fun CapControl(
    onClick: (() -> Unit)?,
    selected: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .then(if (onClick != null) Modifier.minimumInteractiveComponentSize() else Modifier)
            .semantics(mergeDescendants = true) {}
            .then(
                if (onClick != null) {
                    Modifier.selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
                } else {
                    Modifier
                }
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        content = content
    )
}
// imports: androidx.compose.foundation.selection.selectable (replaces ...foundation.clickable, now unused),
//          androidx.compose.material3.minimumInteractiveComponentSize, androidx.compose.ui.semantics.Role
```
**Measured semantics tree (scratch, ladder cap = "hybrid"):** every rung node prints `Role = 'RadioButton'`, `Selected = 'false'|'true'` (only Hybrid true), `Actions = [... OnClick ...]`, `MergeDescendants = 'true'`, `Text = '[Cloud, Capped]'`. With no cap props: `Role`/`Selected` keys are not defined and `assertHasNoClickAction` (existing test) still passes.

**Row-pitch fact for the Gate-2 visual check:** with the modifier the three rows occupy ~58dp pitch each (48dp interactive + 2×`Dimens.ContentSpacing` (4dp) outer padding + 2dp hairline) vs ~45–46dp before. The outer `padding(vertical = Dimens.ContentSpacing)` sits OUTSIDE the clickable, so it is not part of the touch target. D-04 accepts the growth; the planner should not "optimize" it away (e.g., do not move the padding inside) without owner sign-off — it changes the visual.

### Pattern 2: Append defaulted fields to the two data classes (VFAIL-01..03) — reproduced green
**What:** `@JvmOverloads constructor` keeps every shipped constructor arity; a hand-written legacy-arity `copy` (no defaults, original parameter list) delegates to the generated full-arity `copy` and preserves the new fields.
```kotlin
// FailureActionUiModel.kt  (needs: import io.github.ygaray.yahirandroidtaste.component.ActionButtonDefaults —
//  precedent: VoiceOutcomeUiState.kt:4 already imports it into model/)
data class FailureActionUiModel @JvmOverloads constructor(
    val label: String,
    val onClick: () -> Unit,
    val role: ActionButtonDefaults.ActionButtonRole = ActionButtonDefaults.ActionButtonRole.Neutral
) {
    // Hand-written v2.4 `copy(String, Function0)` so Metalava does not report RemovedMethod.
    fun copy(label: String, onClick: () -> Unit): FailureActionUiModel =
        copy(label = label, onClick = onClick, role = role)
}

// VoiceOutcomeUiState.Failure
data class Failure @JvmOverloads constructor(
    val reason: String,
    val handledBy: HandledByUiModel? = null,
    val action: FailureActionUiModel? = null,
    val body: (@Composable () -> Unit)? = null,
    val semanticsPrefix: String? = null
) : VoiceOutcomeUiState {
    fun copy(reason: String, handledBy: HandledByUiModel?, action: FailureActionUiModel?): Failure =
        copy(reason = reason, handledBy = handledBy, action = action, body = body, semanticsPrefix = semanticsPrefix)
}
```
Field order matters: append `role` after `onClick`, and `body`, `semanticsPrefix` after `action` in that order (CONTEXT Integration Points: preserve `componentN()`/`copy()` ordinals; brief §2(d) order is `body` then `semanticsPrefix`).

**Empirical `apiDump` delta (scratch; `apiCheck` BUILD SUCCESSFUL before the dump):**
```
+ ctor public FailureActionUiModel(String label, kotlin.jvm.functions.Function0<kotlin.Unit> onClick, optional ...ActionButtonDefaults.ActionButtonRole role);
- method ... FailureActionUiModel copy(optional String label, optional kotlin.jvm.functions.Function0<kotlin.Unit> onClick);
+ method ... FailureActionUiModel copy(String label, kotlin.jvm.functions.Function0<kotlin.Unit> onClick);
+ method ... FailureActionUiModel copy(optional String label, optional ... onClick, optional ...ActionButtonRole role);
+ component3(), getRole(), property role
+ ctor public VoiceOutcomeUiState.Failure(String reason);
+ ctor public VoiceOutcomeUiState.Failure(String reason, optional HandledByUiModel? handledBy);
  ctor public VoiceOutcomeUiState.Failure(String reason, optional HandledByUiModel? handledBy, optional FailureActionUiModel? action);   <- OLD LINE SURVIVES VERBATIM
+ ctor ...Failure(String reason, optional ... handledBy, optional ... action, optional kotlin.jvm.functions.Function0<kotlin.Unit>? body);
+ ctor ...Failure(String reason, optional ... handledBy, optional ... action, optional ...Function0<kotlin.Unit>? body, optional String? semanticsPrefix);
- method ... Failure copy(optional String reason, optional ... handledBy, optional ... action);
+ method ... Failure copy(String reason, ... handledBy, ... action);   (hand-written)
+ method ... Failure copy(optional ... reason, ... handledBy, ... action, optional ...Function0<kotlin.Unit>? body, optional String? semanticsPrefix);
+ component4(), component5(), getBody(), getSemanticsPrefix(), property body, property semanticsPrefix
```
(`body` dumps as `Function0<Unit>?` — same as the shipped `Success.editableContent`, so no new surface oddity.) The two `-` lines are why the raw-line guard still reports lane 3.

### Pattern 3: Failure body slot + role wiring + prefix semantics (reproduced)
```kotlin
// OutcomeSheet.kt FailureBody  (add import androidx.compose.ui.semantics.contentDescription)
Surface(
    color = MaterialTheme.colorScheme.errorContainer,
    contentColor = MaterialTheme.colorScheme.onErrorContainer,
    shape = MaterialTheme.expressive.cardShapeLarge,
    modifier = Modifier
        .fillMaxWidth()
        .testTag("outcome_sheet_failure_surface")
        .then(
            if (!failure.semanticsPrefix.isNullOrBlank()) {
                Modifier.semantics(mergeDescendants = true) {
                    contentDescription = "${failure.semanticsPrefix} ${failure.reason}"
                }
            } else {
                Modifier
            }
        )
) {
    Column(modifier = Modifier.padding(Dimens.HorizontalPadding)) {
        Text(text = failure.reason, style = MaterialTheme.typography.headlineSmall)
        failure.handledBy?.let { HandledByRow(it) }
        failure.body?.invoke()                         // NEW (VFAIL-02): rides the existing scroll region
        failure.action?.let { action ->
            DynamicActionButton(
                label = action.label,
                role = action.role,                    // WAS: ActionButtonDefaults.ActionButtonRole.Neutral (VFAIL-01)
                onClick = action.onClick,
                modifier = Modifier.padding(top = Dimens.ContentSpacing).testTag("outcome_sheet_action_button")
            )
        }
    }
}
```
**Measured merged tree for `Failure(reason="Network down", action=…Destructive, body={Text("BODYTEXT")}, semanticsPrefix="Error:")` (scratch used "Error: " + reason):** surface node `Tag: 'outcome_sheet_failure_surface'`, `ContentDescription = '[Error: Network down]'`, `Text = '[Network down, BODYTEXT]'`, `MergeDescendants = 'true'`; the action button stays a SEPARATE child node (`Tag: 'outcome_sheet_action_button'`, `Role = 'Button'`, `OnClick`) because a clickable is itself a merging node — tapping it still invokes `onClick` (asserted). So adding `mergeDescendants` on the surface does not swallow the action.

**Separator recommendation (planner decision; mark in KDoc):** join with one ASCII space (`"$prefix $reason"`), and document that punctuation belongs to the prefix (e.g., `"Error:"`) — consistent with Phase 15's documented joining rule ("fragments are joined by a single ASCII space … punctuation/colon belongs to the label", `15-REVIEW-FIX.md` WR-03 applied fix). D-02 says only "prefix + reason"; the separator is unspecified, so flag as Assumption A3.

**Blank prefix:** treat blank like null (`isNullOrBlank()`) so `""` can't produce `" Network down"`. Spec says "null = unchanged"; blank handling is additive-safe discretion.

### Anti-Patterns to Avoid
- **Plain field append on either data class** — `apiCheck` red (Phase 15 reproduced; re-confirmed here for the recipe, not the naive form). Always `@JvmOverloads` + legacy `copy`.
- **`isCapped` as `selected`** — contradicts locked D-01 (`isCapped` = excluded above-cap rows).
- **Applying `Role.RadioButton`/`selected`/min-size when `onClick == null`** — contradicts D-03; breaks `assertHasNoClickAction`-style expectations and adds dead space.
- **`clearAndSetSemantics` on the failure surface** — wipes the action button's semantics.
- **Adding `@HiltAndroidApp`/new public composables/registry entries** — none are needed; `ComponentRegistry` drift guard is unaffected (no new public `@Composable`).
- **Editing `explorer/` call sites** — all new params/fields are defaulted; gallery stays byte-identical (same stance as Phase 15 D-03; the `VoiceCommandFamilyScreen.kt:367-372` Failure fixtures need no change). An optional showcase of `role`/`body` is a discretionary add-on, not required.
- **Setting `liveRegion`** — left open by CONTEXT for SB-175 validation; do NOT add it in this phase (it would announce on first composition and is a behavior the consumer hasn't validated). Record as a follow-up note.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---|---|---|---|
| Binary-compat for added ctor params | Hand-written secondary constructors | `@JvmOverloads constructor` | Repo precedent (`TagChipUiModel`, `UndoRowUiModel`); generates every shorter arity. |
| Old-arity `copy` | Dropping it / body `var` | Hand-written delegating `copy` | Metalava `RemovedMethod` otherwise; Phase 15 recipe. |
| Radio + selected semantics | Custom `semantics { role = …; selected = … ; onClick(…) }` | `Modifier.selectable(selected, role, onClick)` | Sets role, selected, and click action together; same as M3 `RadioButton`. |
| 48dp touch target | `heightIn(min = 48.dp)` | `Modifier.minimumInteractiveComponentSize()` | Respects `LocalMinimumInteractiveComponentSize`; repo precedent. |
| Role → button treatment | Another enum / color switch | Existing `ActionButtonDefaults.ActionButtonRole` + `DynamicActionButton` | Locked by CONTEXT; `NeedsConfirmation.severity` is the precedent. |
| API diffing | Custom script | `./gradlew apiDump` / `apiCheck` | Authoritative additive gate (Phase 15 D-01). |

**Key insight:** every piece of this phase already has an in-repo precedent (`PresetChip`/`AppChip` min-size + selected, `NeedsConfirmation.severity` role field, `Success.editableContent` slot, Phase 15 data-class recipe). The risk is not novelty; it is deviating from the precedents.

## Runtime State Inventory
Not a rename/refactor/migration phase — omitted.

## Common Pitfalls

### Pitfall 1: `apiCheck` red on a data-class field append
**What goes wrong:** `Removed constructor …` / `Removed method …copy(…)` errors. **Why:** the compiler emits only the full-arity `copy` and a synthetic default ctor. **Avoid:** `@JvmOverloads` + hand-written legacy `copy` on BOTH `FailureActionUiModel` and `Failure`. **Warning sign:** `./gradlew apiCheck` exit 1 after the first model edit.

### Pitfall 2: Pre-commit hook blocks commits (lane 3)
**What goes wrong:** `tools/hooks/pre-commit` diffs HEAD vs the newest `v*` tag (`v2.4.1` today — [VERIFIED: `git describe --tags --abbrev=0 --match 'v*'` -> `v2.4.1`]) via `classify-hub-change.sh`; since Phase 15 already removed/changed `api.txt` lines vs `v2.4.1`, EVERY commit is lane 3. **Avoid:** `HUB_LANE_OVERRIDE=3 git commit …` throughout; the override must equal the detected lane exactly (`[ "${HUB_LANE_OVERRIDE:-}" = "$lane" ]`, `tools/hooks/pre-commit`). Regenerate `api.txt` in the same commit as the source change so a lane-2 mismatch (rewritten source lines without the dump) doesn't occur. [VERIFIED: tools/hooks/pre-commit read this session; lane analysis carried from 15-RESEARCH.md Pitfall 2]

### Pitfall 3: The min-size test passes vacuously
**What goes wrong:** `assertTouchHeightIsEqualTo(48.dp)` / `assertHeightIsAtLeast(48.dp)` give a false sense of coverage. **Measured:** with `minimumInteractiveComponentSize()` removed, `assertTouchHeightIsEqualTo(48.dp)` STILL passed (framework expands touch bounds for clickables), while `assertHeightIsAtLeast(48.dp)` FAILS even WITH the modifier (the semantics node bounds exclude the min-size slack: measured 36dp). **Avoid:** assert **touch-bounds non-overlap between consecutive rungs** (measured WITH: `[20–68]`, `[78.5–126.5]`, `[136.5–184.5]` dp -> no overlap; WITHOUT: `[14–62]`, `[59.5–107.5]`, `[104.5–152.5]` -> overlaps). That is falsifiable and is the real defect.

### Pitfall 4: Failure role wiring is not observable in Robolectric
**What goes wrong:** tests that "assert the role renders" can't — Save/Destructive/Neutral all measured `58 x 52` and expose `Role = 'Button'`. The repo already documents that this harness cannot assert rendered color (`CardTagRowTest.kt` header: "no `captureToImage` usage anywhere under `src/test/java`"). **Avoid:** (a) source-contract test via `SourceContractTestSupport` (`functionBody(src, "private fun FailureBody(")`, `stripComments`) asserting the body contains `role = action.role` and does NOT contain `role = ActionButtonDefaults.ActionButtonRole.Neutral`; (b) JVM model test that the default is `Neutral`; (c) a Compose test that every role still renders one clickable button that fires `onClick`. Gate-1/2 UAT covers the actual colors.

### Pitfall 5: `contentDescription` on a merged node vs. spoken body text
**What goes wrong:** the merged failure surface carries `ContentDescription=[prefix reason]` AND `Text=[reason, body…]`. In Compose, parent+child `ContentDescription` values are list-appended, and an accessibility service that prefers `contentDescription` over `text` may read only `prefix reason` and omit `handledBy`/`body` text. [ASSUMED — TalkBack's text-vs-contentDescription precedence was not verified on a device this session.] **How to handle:** implement D-02 exactly as decided; note in the plan that SB-175 validates the shape; do not widen the description to include `body` text (a slot's content can't be introspected anyway). Interactive content inside `body` (clickables) remains separately focusable, like the action button.

### Pitfall 6: `selectable` import swap leaves `clickable` import unused (detekt/compiler warning)
`ApproachLadderCard.kt:3` imports `androidx.compose.foundation.clickable`, used only in `CapControl`. Remove it when switching to `selectable`, or detekt (zero baseline) may flag an unused import. [VERIFIED: `grep clickable` shows its only use is `CapControl`'s `Modifier.clickable(onClick = onClick)` at ApproachLadderCard.kt:223.]

### Pitfall 7: Override semantics with `Failure.body` and equality
`Failure` is a data class; a lambda `body` participates in `equals`/`hashCode` by reference (same as `Success.editableContent`). A consumer that builds `Failure(... body = { … })` inside a composable each recomposition gets a new instance each time — a consumer concern, same as the existing slot; mention in KDoc ("remember the lambda if the Failure is compared/keyed").

### Pitfall 8: Binary vs source compatibility wording
Phase 15's API.md section states, correctly, that appending fields is **source-compatible, NOT binary-compatible for Kotlin default-arg call sites** (synthetic `…DefaultConstructorMarker`/`copy$default` arities change) and that consumers must recompile on repin — and that a clean `apiCheck` is "not proof of binary compatibility" ([VERIFIED: /home/yahir/Projects/Reusable/android/yahirandroidtaste/API.md lines ~224-236 read this session]). Phase 16's success criterion 5 says "binary-compat apiCheck / api.txt, source-compat with v2.4.x" — satisfy it as `apiCheck` green + a source-compat fixture; do NOT claim full binary compat. Extending the API.md paragraph is Phase 18 (DOC-02) scope; in Phase 16 add KDoc only.

## Code Examples

### Falsifiable min-size test (verified to distinguish with/without the modifier)
```kotlin
@Test
fun `rung touch targets do not overlap when the cap is selectable`() {
    composeTestRule.setContent {
        ApproachLadderCard(ladder = ladder, maxTierId = "hybrid", onMaxTierChange = {})
    }
    composeTestRule.waitForIdle()
    val touch = (0..2).map { rungNodes()[it].fetchSemanticsNode().touchBoundsInRoot }
    // px == dp under Robolectric's default mdpi density in this harness (printed bounds matched dp).
    assertTrue(touch[0].bottom <= touch[1].top && touch[1].bottom <= touch[2].top)
    // each target is >= 48dp tall:
    touch.forEach { assertTrue(it.height >= 48f) }   // or rungNodes()[i].assertTouchHeightIsEqualTo(48.dp)
}
```
(Use `LocalDensity` to convert instead of assuming px==dp if you want it density-robust: `with(density) { 48.dp.toPx() }`.)

### Selected + role + cap-less negative (verified)
```kotlin
rungNodes()[1].assertIsSelected()                       // maxTierId = "hybrid", ladder[1].id == "hybrid"
rungNodes()[0].assertIsNotSelected(); rungNodes()[2].assertIsNotSelected()
rungNodes()[1].assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton))
// cap-less (maxTierId = null, onMaxTierChange = null):
rungNodes()[0].assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Role))
rungNodes()[0].assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Selected))
// stale maxTierId (id not in ladder): no rung selected -> every rung assertIsNotSelected()
```

### Failure prefix + body + action still clickable (verified)
```kotlin
composeTestRule.setContent {
    OutcomeSheetContent(   // internal; same-module tests can host it directly
        VoiceOutcomeUiState.Failure(
            reason = "Network down",
            action = FailureActionUiModel("Delete", { clicked = true }, ActionButtonDefaults.ActionButtonRole.Destructive),
            body = { Text("BODYTEXT") },
            semanticsPrefix = "Error:"
        )
    )
}
composeTestRule.onNodeWithText("BODYTEXT").assertExists()
composeTestRule.onNodeWithTag("outcome_sheet_failure_surface").assertContentDescriptionEquals("Error: Network down")
composeTestRule.onNodeWithTag("outcome_sheet_action_button").performClick()   // still fires; not swallowed by the merge
// null prefix -> no ContentDescription on the surface:
//   .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.ContentDescription))
```
(`OutcomeSheetContent` is `internal` — `OutcomeSheet.kt:91` `internal fun OutcomeSheetContent(outcome: VoiceOutcomeUiState, modifier: Modifier = Modifier)` — and is already used for height-bounded hosting; `OutcomeSheet(…)` with a `ModalBottomSheet` also works and is what the existing Failure tests use.)

### Source-compat fixture additions (extend `VoiceI18nSourceCompatTest` or add `VoiceFailureSourceCompatTest`)
```kotlin
val f1 = VoiceOutcomeUiState.Failure("r")
val f2 = VoiceOutcomeUiState.Failure("r", null, null)
val (reason, handledBy, action) = f2                       // destructuring first three still compiles
val f3 = f2.copy("r2", null, null)                          // binds to legacy 3-arg copy; must preserve body/prefix
val a = FailureActionUiModel("l", {})                       // role defaults to Neutral
val a2 = a.copy("m", {})                                    // legacy 2-arg copy; must preserve role
assertEquals(ActionButtonDefaults.ActionButtonRole.Neutral, a.role)
```
Plus extend `VoiceModelLabelDefaultsTest`'s reflection drift guard (`assertLegacyCopyCarriesEveryField` — it requires an instance with EVERY field non-default, then invokes the shortest non-synthetic `copy` and asserts equality) with `Failure(…, body = {…}, semanticsPrefix = "x")` and `FailureActionUiModel(…, role = Destructive)`. Note: `body` is a lambda -> reuse the SAME lambda instance in the instance and the comparison (reference equality), and component accessors via `getMethod("componentN")` already work for data classes.

## State of the Art

| Old | Current | Impact |
|---|---|---|
| `Modifier.clickable(onClick)` for a single-choice row | `Modifier.selectable(selected, role = Role.RadioButton, onClick)` | One modifier supplies role + selected + click; what M3 `RadioButton` uses. |
| Assuming in-place data-class append is additive | Metalava flags ctor/`copy` removal | `@JvmOverloads` + legacy `copy` (Phase 15 standard). |
| `assertTouchHeightIsEqualTo` as min-size proof | Compose auto-expands clickable touch bounds | Assert touch-bounds overlap/pitch instead (falsifiable). |

**Deprecated/outdated:** the brief's shorthand `semantics { selected = isCapped; role = Role.RadioButton }` (RECONVENE-BRIEF §2(c)) — superseded by D-01 (`rung.id == maxTierId`).

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | TalkBack prefers `contentDescription` over `text` on a merged node, so a merged-node description of `prefix reason` may omit `handledBy`/`body` text from speech | Pitfall 5 | SB-175 hears less than expected; D-02 already delegates shape validation to SB-175, so low risk to the plan; a follow-up could fold `handledBy` into the description. |
| A2 | `Robolectric` px == dp in this harness (printed bounds were consistent with mdpi 1.0) | Code Examples (min-size test) | Test would use wrong thresholds; mitigate by converting with `LocalDensity`. |
| A3 | Single ASCII space is the prefix/reason separator, and a blank prefix is treated as null | Pattern 3 | Cosmetic announcement difference only; D-02 left the separator unspecified. |
| A4 | Apply `minimumInteractiveComponentSize()` ONLY when `onClick != null` (D-04 literally says "per-rung interactive wrapper (CapControl's Row)"; success criterion 1 says "each rung row") | Pattern 1 | If the owner intends cap-less rows to also grow to 48dp, the gate should be removed; cap-less rows aren't interactive, so growing them is dead space. Recommend confirming at plan-check. |
| A5 | Gallery (`explorer/`) is left unchanged | Anti-patterns | If the owner wants Gate-1/2 to see `role`/`body`/prefix in the Explorer, a small fixture addition in `VoiceCommandFamilyScreen.kt` is needed (and its `ComponentRegistry`/gallery tests re-run). |

## Open Questions

1. **Should cap-less ladders also get 48dp rows?**
   - What we know: D-03 forbids the radio role/selected there; D-04 says the "interactive wrapper"; criterion 1 says "each rung row".
   - What's unclear: whether the owner wants uniform row height regardless of interactivity.
   - Recommendation: gate on `onClick != null` (A4); verify with the Gate-2 visual check D-04 already mandates.
2. **Do we want a `handledBy`-inclusive announcement?**
   - Recommendation: no — implement D-02 literally; SB-175 validates.
3. **`liveRegion`?** — Recommendation: do not add in this phase (CONTEXT leaves it to SB-175).

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|---|---|---|---|---|
| JDK 17 | Gradle/Metalava | ✓ | worked in scratch (`BUILD SUCCESSFUL`) | — |
| Gradle wrapper (offline) | all commands | ✓ | 9.4.1 ([VERIFIED: "docs.gradle.org/9.4.1" in scratch run output]) | — |
| Android SDK (`local.properties`) | unit tests | ✓ | present (copied to scratch; Robolectric tests ran) | — |

Operational notes: first invocation in a fresh dir may need `-Dorg.gradle.vfs.watch=false`; use `--offline`. Observed timings: `apiCheck` ~20 s, `detekt`+`apiCheck`+3 test classes ~40 s warm.

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit4 + Robolectric 4.16.1 (`RobolectricTestRunner`, `@Config(sdk = [35])`) + `createComposeRule()` |
| Config file | none beyond `build.gradle.kts` (existing harness) |
| Quick run command | `./gradlew testDebugUnitTest --tests '*ApproachLadderCardTest' --tests '*OutcomeSheetTest' --tests '*VoiceModelLabelDefaultsTest' --tests '*VoiceI18nSourceCompatTest'` |
| Full suite command | `./gradlew testDebugUnitTest && ./gradlew apiCheck && ./gradlew detekt` |
| Additive gate | `F=$(mktemp) && cp api.txt "$F" && ./gradlew apiDump && ./gradlew apiCheck` (run `apiCheck` BEFORE `apiDump` too — authoritative additive gate); then review `git diff -- api.txt` |
| Guard self-tests | `bash tools/test/run-all.sh` (optional; before release) |

### Phase Requirements -> Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| VA11Y-01 | capped-selected rung (`rung.id == maxTierId`) is `Selected` + `Role.RadioButton`; others not selected | Compose UI | `./gradlew testDebugUnitTest --tests '*ApproachLadderCardTest'` | file ✅, new tests ❌ |
| VA11Y-01 | rung touch targets don't overlap / are >= 48dp (falsifiable: fails without the modifier) | Compose UI | same | ❌ new |
| VA11Y-01 | cap-less ladder: no `Role`, no `Selected`, still no click action (D-03; existing `assertHasNoClickAction` test) | Compose UI | same | ✅ existing + ❌ new key-not-defined asserts |
| VA11Y-01 | stale `maxTierId` -> no rung selected | Compose UI | same | ❌ new |
| VFAIL-01 | `FailureActionUiModel.role` defaults to Neutral; legacy 2-arg `copy` preserves role | JVM | `… --tests '*VoiceModelLabelDefaultsTest'` | file ✅, ❌ new |
| VFAIL-01 | `FailureBody` wires `role = action.role` (no hardcoded Neutral) | source-contract | `… --tests '*FailureRoleSourceContractTest'` (uses `SourceContractTestSupport`) | ❌ new |
| VFAIL-01 | each role renders exactly one clickable `outcome_sheet_action_button` that fires `onClick` | Compose UI | `… --tests '*OutcomeSheetTest'` | ✅ existing (Neutral) + ❌ Save/Destructive |
| VFAIL-02 | `body` renders inside the failure surface; null -> unchanged (no extra node, existing tests still green) | Compose UI | `… --tests '*OutcomeSheetTest'` | ❌ new |
| VFAIL-03 | `semanticsPrefix` -> surface `ContentDescription == "<prefix> <reason>"`; null/blank -> key not defined; action still clickable | Compose UI | same | ❌ new |
| SC5 | v2.4 call shapes still compile (`Failure("r")`, 3-arg positional, destructuring, legacy `copy`, `FailureActionUiModel("l", {})`) | compile-only + JVM | `… --tests '*VoiceI18nSourceCompatTest'` (extend) | ❌ extend |
| SC5 | Metalava additive | gradle | `./gradlew apiCheck` | ✅ |
| Regression net | existing Failure/rung/NeedsConfirmation assertions unchanged | Compose UI | quick run command | ✅ (passed in scratch) |
| Guard | no registry/drift break (no new public composable) | JVM | `./gradlew testDebugUnitTest --tests '*ComponentRegistryDriftGuardTest' --tests '*DomainVocabularyDriftGuardTest'` | ✅ |

### Sampling Rate
- **Per task commit:** quick run command + `./gradlew apiCheck` (after `apiDump` when a model signature changed) + `./gradlew detekt`.
- **Per wave merge:** `./gradlew testDebugUnitTest detekt apiCheck`.
- **Phase gate:** full `./gradlew testDebugUnitTest` + `detekt` + `apiCheck` green, plus `git diff v2.4.1 -- api.txt` showing only appended lines for this phase's two classes (and the two expected `copy(optional …)` replacements) before `/gsd-verify-work`; Gate-2 human visual check of rung row height (D-04) and a Failure with `role`/`body`/prefix.

### Wave 0 Gaps
- [ ] New tests in `ApproachLadderCardTest.kt`, `OutcomeSheetTest.kt` (use a non-default sentinel for prefix, e.g. "Erreur :" with a French-style space, so it can't pass on defaults).
- [ ] Extend `model/VoiceModelLabelDefaultsTest.kt` + `component/VoiceI18nSourceCompatTest.kt` for `Failure`/`FailureActionUiModel`.
- [ ] New `FailureRoleSourceContractTest` (source-contract idiom; `SourceContractTestSupport.source("OutcomeSheet.kt")`).
- [ ] No framework install needed.

## Security Domain

`security_enforcement` is enabled (`"security_enforcement": true`, `"security_asvs_level": 1` in `.planning/config.json`).

### Applicable ASVS Categories
| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2/V3/V4 Auth/Session/Access | no | UI semantics/data-model only; no auth surface |
| V5 Input Validation | minimal | `semanticsPrefix` and `body` are caller-supplied, rendered as plain `contentDescription` / Compose content — no HTML/format parsing, no `String.format`; keep concatenation plain |
| V6 Cryptography | no | none |

### Known Threat Patterns
| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| Sensitive failure text leaking via model `toString` / logs | Information disclosure | `Failure` uses the generated `toString` today (already prints `reason`); `semanticsPrefix`/`role` hold static UI copy/enum; `body` prints as a lambda reference. Do not add sensitive text to either; no override of `toString` needed (contrast `NeedsConfirmation`, which overrides it for privacy). |
| INV-01 breach (library importing host code) | Tampering | New imports are Compose/foundation/material3 + `component.ActionButtonDefaults` (same library). P18 INV-02 does import-inspection over P15–P17 diffs; keep Phase 16 imports library/AndroidX only. |

## Project Constraints (from CLAUDE.md)
- **One-way dependency / INV-01:** no consumer imports, no secrets, no domain nouns — the new fields are generic (`role`, `body`, `semanticsPrefix`).
- **`ComponentRegistry` drift guard:** no new public top-level `@Composable`; do not add one (private/internal sub-parts need no registration).
- **Bindings-only Hilt:** never add `@HiltAndroidApp`/`@AndroidEntryPoint`.
- **Interaction conventions travel with components:** preserve conditional-render-no-dead-space (hence the `onClick != null` gate), reveal-confirm swipe, snackbar/undo feedback (untouched).
- **Detekt zero baseline:** do not regenerate `config/detekt-baseline.xml`; detekt passed on the candidate code in scratch. Constructor `LongParameterList` limit is 18 with `ignoreDefaultParameters` (`config/detekt-compose.yml`, per 15-RESEARCH).
- **Gradle commands drop the module prefix** (`./gradlew apiCheck`, `./gradlew testDebugUnitTest`, `./gradlew detekt`, `./gradlew apiDump`).
- **Tags are immutable; shipping is human-gated:** this phase cuts NO tag and repins no consumer (Phase 19 owns `v2.5.0`); commit on `main`; no consumer-file edits (sequential-in-hub convention).
- **Commits end with the `Co-Authored-By` attribution line** from the session reminder; commit under `HUB_LANE_OVERRIDE=3` (see Pitfall 2).
- Project `.claude/CLAUDE.md` is a pointer only (defers to root `CLAUDE.md`); no project skills directory directives found beyond that.

## Sources

### Primary (HIGH — read/executed this session)
- Source read: `component/ApproachLadderCard.kt` (full), `component/OutcomeSheet.kt` (1-130, 270-340), `component/DynamicActionButton.kt` (40-85; `enum class ActionButtonRole { Destructive, Save, Neutral }` at line 67), `component/PresetChip.kt` (70-130), `model/VoiceOutcomeUiState.kt` (full), `model/FailureActionUiModel.kt` (full), `model/UndoRowUiModel.kt` (full; recipe precedent), `api.txt` (Failure/FailureActionUiModel/UndoRowUiModel blocks), `tools/hooks/pre-commit`, `API.md` §appending label fields, `src/test/.../ApproachLadderCardTest.kt` (full), `OutcomeSheetTest.kt` (140-200), `VoiceModelLabelDefaultsTest.kt`, `VoiceI18nSourceCompatTest.kt`, `CardTagRowTest.kt` (header), `SourceContractTestSupport.kt` (signatures).
- Planning: `16-CONTEXT.md`, `REQUIREMENTS.md`, `ROADMAP.md` Phase 16, `v2.5-DECISION-MAP.md`, `RECONVENE-BRIEF-R-v1.1.md` §2(c)(d), `15-RESEARCH.md`, `15-VALIDATION.md`, `15-REVIEW-FIX.md`.
- Empirical (scratch copy of the repo under the session scratchpad, working tree untouched): `apiCheck` BUILD SUCCESSFUL for the recipe; `apiDump` diff reproduced above; `detekt` + `apiCheck` + `ApproachLadderCardTest` + `OutcomeSheetTest` + `VoiceModelLabelDefaultsTest` green; printed merged semantics trees (rung `Role/Selected/Text/Actions`; failure surface `ContentDescription` + `Text` + separate button child); touch-bounds with/without `minimumInteractiveComponentSize()`; role renders all `58 x 52`.

### Secondary / Tertiary
- None. No web research was needed (no external libraries; behavior was measured directly). TalkBack speech precedence remains `[ASSUMED]` (A1).

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH — unchanged existing stack, versions read from CLAUDE.md/libs.versions.toml.
- Architecture / recipe: HIGH — reproduced green in scratch (apiCheck, detekt, existing tests).
- Pitfalls: HIGH for 1-4, 6-8 (measured/read); MEDIUM for 5 (TalkBack behavior assumed).

**Research date:** 2026-10-05
**Valid until:** 2026-11-04 (stable; invalidated only by a Metalava plugin/AGP/Compose BOM bump)
