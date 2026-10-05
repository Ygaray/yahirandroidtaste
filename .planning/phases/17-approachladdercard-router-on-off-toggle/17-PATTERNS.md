# Phase 17: ApproachLadderCard Router ON/OFF toggle - Pattern Map

**Mapped:** 2026-10-05
**Files analyzed:** 6 (all modified, none created)
**Analogs found:** 6 / 6 (all tracked via `git ls-files`)

All excerpts below are from 17-RESEARCH.md, which read the current files (Phases 15+16 landed). Line numbers are those cited by research; planner should re-read `ApproachLadderCard.kt:80-92` and `api.txt:58` before editing (they shift if Phase 15/16 files change again).

## File Classification

| Modified File | Role | Data Flow | Closest Analog | Match Quality |
|---------------|------|-----------|----------------|---------------|
| `src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt` | component | request-response (display + emit) | itself: `offlineOnly`/`onOfflineOnlyChange` pair (sig ~:80-92, require ~:102-106, render ~:141-151) | exact (self-clone) |
| `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/VoiceCommandFamilyScreen.kt` | gallery fixture | state-hoisting demo | itself: `ApproachLadderCardFixture` `:311-326` (`offlineOnly` state) | exact |
| `src/test/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCardTest.kt` | test | Compose UI | itself: offline toggle tests (~:76-102, :167-198, :281-318) | exact |
| `src/test/java/io/github/ygaray/yahirandroidtaste/explorer/GalleryDemoInteractionTest.kt` (or new sibling `ApproachLadderGalleryFixtureTest.kt`) | test | registry-reached render | `tagChipEditorContent_defaultCell_*` tests `:54-83` | role-match |
| `src/test/java/io/github/ygaray/yahirandroidtaste/component/VoiceI18nSourceCompatTest.kt` (optional) | test | compile-only pin | existing `v240PositionalComposableCallShapes_compileAgainstV25Signatures` (:61-69) | exact |
| `api.txt` (line 58) | config (Metalava dump) | generated | Phase 15 commit `31595d4` (same line re-signatured) | exact |

## Pattern Assignments

### `ApproachLadderCard.kt` (component)

**Analog:** its own offline pair. Append router params LAST (never beside the offline pair: shifts positional binding, breaks `VoiceI18nSourceCompatTest`).

**Signature** (current tail, :80-92):
```kotlin
    onlineLabel: String = "Online",
    offlineOnlyLabel: String = "Offline only"
) {
```
becomes
```kotlin
    offlineOnlyLabel: String = "Offline only",
    router: Boolean? = null,
    onRouterChange: ((Boolean) -> Unit)? = null,
    routerOnLabel: String = "Router on",
    routerOffLabel: String = "Router off"
) {
```

**require() guard** (clone of :102-106, add right after it):
```kotlin
    require((router == null) == (onRouterChange == null)) {
        "ApproachLadderCard: router and onRouterChange must both be null or both be non-null " +
            "(got router=$router, onRouterChange=${if (onRouterChange == null) "null" else "non-null"})"
    }
```

**Render block** (clone of :141-151; place directly AFTER the offline block, inside the same outer Column; index 1 = ON):
```kotlin
            if (router != null && onRouterChange != null) {
                SegmentedOptionSelector(
                    selectedIndex = if (router) 1 else 0,
                    options = listOf(routerOffLabel, routerOnLabel),
                    onSelect = { index -> onRouterChange(index == 1) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimens.ContentSpacing)
                        .testTag("approach_ladder_card_router_toggle")
                )
            }
```
(Offline original: `selectedIndex = if (offlineOnly) 1 else 0`, `options = listOf(onlineLabel, offlineOnlyLabel)`, `onSelect = { index -> onOfflineOnlyChange(index == 1) }`, tag `approach_ladder_card_offline_toggle`.)

**KDoc:** add `@param router`, `@param onRouterChange` (pair, require, null hides, "policy toggle, NOT per-rung navigation"), `@param routerOnLabel`, `@param routerOffLabel` (caller-localizable, English defaults; "selected/not selected" words stay English, announced by `SegmentedOptionSelector.kt:65`). Amend the class-level "hideable-by-null-prop" sentence to mention the router pair.

**Do not touch:** `RungRow`, `CapControl`, `isEffective`/`capRank` derivation (router is display + emit only), `SegmentedOptionSelector.kt`, `ComponentRegistry` (param addition, not new composable).

---

### `VoiceCommandFamilyScreen.kt` (gallery fixture, D-02)

**Analog:** `ApproachLadderCardFixture` `:311-326`:
```kotlin
@Composable
private fun ApproachLadderCardFixture(
    initialOfflineOnly: Boolean = false,
    initialMaxTierId: String = fixtureLadder.first().id
) {
    var offlineOnly by remember { mutableStateOf(initialOfflineOnly) }
    var maxTierId by remember { mutableStateOf(initialMaxTierId) }
    ApproachLadderCard(
        ladder = fixtureLadder,
        offlineOnly = offlineOnly,
        onOfflineOnlyChange = { offlineOnly = it },
        maxTierId = maxTierId,
        onMaxTierChange = { maxTierId = it },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    )
}
```
Edit: add `initialRouter: Boolean = false`, `var router by remember { mutableStateOf(initialRouter) }`, pass `router = router, onRouterChange = { router = it }`. Wire the "Pressed / Selected" cell (`:128-131`, `ApproachLadderCardFixture(initialOfflineOnly = true)`) to also pass `initialRouter = true`. Leave the "every control hidden" Variants cell and "combined subdued labels" cell untouched except updating the Variants `SectionLabel` text to mention `router`.

---

### `ApproachLadderCardTest.kt` (test)

**Analog:** existing offline tests. Conventions: `composeTestRule.setContent { ApproachLadderCard(ladder = ladder, ...) }`, `waitForIdle()`, `onNodeWithTag(...)`, `onNodeWithContentDescription("Offline only, not selected").performClick()`, `assertThrows(IllegalArgumentException::class.java) { setContent {...}; waitForIdle() }`.

New tests to add (bodies in 17-RESEARCH.md "Code Examples"):
- emits: `router=false` -> "Router off, selected" exists; click "Router on, not selected" -> `last == true`.
- null hides: tag absent; `onAllNodesWithContentDescription("Router", substring = true).assertCountEquals(0)`.
- pairing both directions via `assertThrows`.
- label override (`"Routeur activé"` etc.), default pin, coexistence/independence (rung tap emits only `onMaxTierChange`; router tap emits only `onRouterChange`), placement (router `boundsInRoot.top` > offline top), rung rendering unchanged when router toggles.

---

### `GalleryDemoInteractionTest.kt` or new sibling (test, registry-reached)

**Analog** (:54-63):
```kotlin
private fun tagChipEditorEntry(): ComponentRegistry.Entry =
    ComponentRegistry.entries.first { it.name == "TagChipEditorContent" }

private fun pressedSelectedCellRender(): @androidx.compose.runtime.Composable () -> Unit =
    tagChipEditorEntry().states.first { it.label == "Pressed / Selected" }.render
        ?: error("... has no render lambda")
```
Clone with `it.name == "ApproachLadderCard"`; render ONE cell per test (tags duplicate across cells). Class header: `@RunWith(RobolectricTestRunner::class) @Config(sdk = [35])`, `@get:Rule val composeTestRule = createComposeRule()`. Assert router toggle exists in the "Pressed / Selected" cell ("Router on, selected") and toggles live on click.

---

### `VoiceI18nSourceCompatTest.kt` (optional)

Existing positional call `ApproachLadderCard(emptyList(), false, { _: Boolean -> }, null, { _: String -> }, Modifier)` (:61-69) must compile UNCHANGED. Optionally add a second lambda passing all 11 pre-Phase-17 args positionally through `offlineOnlyLabel`.

---

### `api.txt` (generated)

Never hand-edit. Sequence: `./gradlew apiCheck` -> `F=$(mktemp) && cp api.txt "$F" && ./gradlew apiDump && cmp "$F" api.txt` (first cmp differs on the signature task) -> `./gradlew apiCheck` -> commit with source. Expected delta: single line `:58` gains `optional Boolean? router, optional kotlin.jvm.functions.Function1<java.lang.Boolean,kotlin.Unit>? onRouterChange, optional String routerOnLabel, optional String routerOffLabel`.

## Shared Patterns

### Fail-loud pairing (require)
**Source:** `ApproachLadderCard.kt:102-106`. Apply to the router pair; never a silent `if`.

### Commit/hook discipline
**Source:** `tools/README-api-guard.md`, `tools/hooks/pre-commit`. If the hook blocks, re-run with `HUB_LANE_OVERRIDE=<lane the hook printed>`; never `--no-verify`, never hard-code the lane.

### Gates
`./gradlew testDebugUnitTest --tests '*ApproachLadderCardTest' --tests '*VoiceI18nSourceCompatTest' --tests '*GalleryDemoInteractionTest'`, `./gradlew detekt` (zero baseline), `apiCheck`. Add `-Dorg.gradle.vfs.watch=false` on "Already watching path". Closing gate must NOT reuse Phase 16's "explorer unchanged" check (explorer legitimately changes); keep `SegmentedOptionSelector.kt` and `feedback/` unchanged. No tag, no repin.

## No Analog Found

None.

## Metadata

**Analog search scope:** component/, explorer/, test dirs, api.txt (via 17-RESEARCH.md reads plus tracked-source verification)
**Tracked check:** `git ls-files` confirmed all six paths are tracked.
**Pattern extraction date:** 2026-10-05
