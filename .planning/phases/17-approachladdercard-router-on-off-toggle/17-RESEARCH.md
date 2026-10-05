# Phase 17: ApproachLadderCard Router ON/OFF toggle - Research

**Researched:** 2026-10-05
**Domain:** Additive Jetpack Compose component param pair on an already-published, Metalava-tracked composable (no new libraries)
**Confidence:** HIGH (the exact change was reproduced green in a scratch copy of the repo: `apiCheck`, `apiDump`, `detekt`, and `ApproachLadderCardTest` incl. two new router tests)

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- **D-01 [router-copy]:** Mirror whatever Phase 15 settles for the offline toggle — if Phase 15 makes the offline-toggle labels caller-overridable (`onlineLabel`/`offlineOnlyLabel`), add matching `routerOnLabel`/`routerOffLabel` params so the card has no lone non-localizable toggle; otherwise hardcode. Default copy: "Router off" / "Router on". Resolution (human). _(provisional — refresh at execution; depends on Phase 15)_ — **Reversibility:** costly — adding the label params later is another api.txt append across the published signature.
- **D-02 [toggle-placement]:** Render the router `SegmentedOptionSelector` below the offline toggle (card bottom), and extend `VoiceCommandFamilyScreen`'s fixture to wire a `router` demo state. _(source: ai-auto)_
- **Runtime Decision (FINAL, human/Yahir in-session, 2026-10-05):** The router toggle's two segment labels are caller-localizable overridable params — add `routerOnLabel: String = "Router on"` and `routerOffLabel: String = "Router off"` to `ApproachLadderCard`, mirroring Phase 15's `offlineOnlyLabel`/`onlineLabel` pattern (those DID become overridable). Strictly additive; defaults are English.

### Claude's Discretion
- Exact testTag name for the router toggle; the `index==1 → ON` mapping must match the offline-toggle convention.

### Deferred Ideas (OUT OF SCOPE)
None — discussion stayed within phase scope. (Phase boundary: additive `router: Boolean? = null` + `onRouterChange: ((Boolean) -> Unit)? = null` pair, `require()`-paired and `null`-hides, mirroring `offlineOnly`/`onOfflineOnlyChange`. NOT per-rung navigation. Covers VAPPR-04.)
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| VAPPR-04 | `ApproachLadderCard` gains a Router ON/OFF toggle via `router: Boolean? = null` + `onRouterChange: ((Boolean) -> Unit)? = null`, `require()`-paired exactly like `offlineOnly`/`onOfflineOnlyChange`; `null` hides the toggle. No per-rung navigation. | Exact insertion points, parameter order, `require()` clone, render block, testTag, api.txt delta, tests, gallery fixture and gates are all specified below and were reproduced green in a scratch copy. |
</phase_requirements>

## Project Constraints (from CLAUDE.md)

Extracted from `/home/yahir/Projects/Reusable/android/yahirandroidtaste/CLAUDE.md` (root; `.claude/CLAUDE.md` only points at it). The planner must verify compliance:

- **One-way dependency / INV-01:** library imports no host code, no secrets, no domain assumptions. The router toggle takes a plain `Boolean?` + callback; never import a voice-engine/router-policy type.
- **Bindings-only Hilt:** never add `@HiltAndroidApp` / `@AndroidEntryPoint`.
- **`ComponentRegistry` drift guard:** `ApproachLadderCard` is already registered; this phase adds a param, NOT a composable, so no registry/allowlist change. Do not add a new public top-level `@Composable`.
- **Interaction conventions preserved:** conditional-render-no-dead-space (null hides the toggle with zero space), row-click selects the cap (no per-rung navigation).
- **Detekt zero-baseline:** keep `./gradlew detekt` green; never regenerate a baseline.
- **Toolchain/commands (no module prefix):** `./gradlew testDebugUnitTest`, `./gradlew detekt`, `./gradlew apiDump` / `apiCheck`.
- **Human-gated shipping:** NO tag, NO consumer repin in this phase (Phase 19 owns the v2.5.0 cut). Commit on `main` (no consumer worktrees; hub-sequential convention).
- **Global (user CLAUDE.md):** UI placement/spacing is first-class (the Gate-2 visual item below); failures must be loud (the `require()` throws, never silently omits).

## Summary

The change is small and fully template-driven: clone the `offlineOnly`/`onOfflineOnlyChange` pair in `ApproachLadderCard.kt` four ways (signature params, `require()` guard, `SegmentedOptionSelector` render block, KDoc), append the four new params LAST on the public signature so every existing positional/named call shape still binds, regenerate `api.txt` in the same commit, and extend tests + the Explorer gallery fixture. There is no new library, no new composable (so no registry change), and the card still only DISPLAYS + EMITS.

The one genuinely load-bearing risk is the published-API gate. Appending defaulted params re-signatures the single `ApproachLadderCard(...)` line in `api.txt` (exactly like Phase 15's `31595d4`). I reproduced the final shape in a scratch copy (repo untouched): `./gradlew apiCheck` passes against the committed `api.txt`, `apiDump` produces exactly ONE changed line (the `ApproachLadderCard` method line gains `optional Boolean? router, optional kotlin.jvm.functions.Function1<java.lang.Boolean,kotlin.Unit>? onRouterChange, optional String routerOnLabel, optional String routerOffLabel`), `detekt` reports 0 code smells, and `ApproachLadderCardTest` ran 23 tests / 0 failures (21 existing + 2 router probes).

Phase 17 is the first phase of v2.5 that must edit `explorer/` (D-02), so Phase 16's "explorer is byte-identical to v2.4.1" closing-gate check must NOT be copied forward. Also, no existing test composes the Explorer `ApproachLadderCardFixture`, so a small registry-reached render test (the `GalleryDemoInteractionTest` idiom) is needed to make D-02 verifiable.

**Primary recommendation:** Append `router`, `onRouterChange`, `routerOnLabel`, `routerOffLabel` (in that order) after `offlineOnlyLabel`; render a second `SegmentedOptionSelector(options = listOf(routerOffLabel, routerOnLabel))` below the offline block with `index == 1 -> ON`; `apiDump` in the same commit as the source change; gate on `apiCheck` (authoritative) and run the commit with `HUB_LANE_OVERRIDE` set to whatever lane the pre-commit hook prints.

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Render the Router ON/OFF segmented control | Client UI library (this repo, `component/`) | — | Presentational composable; state is hoisted |
| Own the router on/off state and act on it | Consumer app (e.g. SecondBrain ViewModel/engine) | — | The library knows no router/engine concept (INV-01); it emits `onRouterChange(Boolean)` only |
| `router`/`onRouterChange` pairing guard | Client UI library (`require()`) | — | Same fail-fast contract as `offlineOnly`/`onOfflineOnlyChange` |
| Localized segment text | Consumer app (passes strings) | Library (English defaults) | Hub localizes nothing (INV-01); `routerOnLabel`/`routerOffLabel` default English |
| Gallery demo of the toggle | Explorer (`explorer/VoiceCommandFamilyScreen.kt`) | — | Explorer-only fixtures, drift-guard denylisted, never registered |
| Public-API additivity proof | Build tooling (Metalava `apiCheck`, `api.txt`) | `tools/*.sh` pre-commit guards | `apiCheck` is the authoritative gate (Phase 15 D-01) |

## Standard Stack

No new dependencies. Everything used is already in the module.

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| `SegmentedOptionSelector` (in-repo, `component/SegmentedOptionSelector.kt`) | n/a | The two-option toggle widget; hard-requires exactly 2 options (`require(options.size == 2)`) | Already what the offline toggle uses |
| Jetpack Compose BOM / Material3 | 2026.04.01 [CITED: /home/yahir/Projects/Reusable/android/yahirandroidtaste/CLAUDE.md Toolchain] | UI | Existing |
| Metalava Gradle plugin `me.tylerbwong.gradle.metalava` | 0.5.0 [CITED: tools/README-api-guard.md; build.gradle.kts `alias(libs.plugins.metalava)`] | `apiDump` / `apiCheck` | Existing authoritative additive gate |
| JUnit4 + Robolectric (`@Config(sdk = [35])`) + Compose UI test `createComposeRule` | existing | Component tests | Established harness in `ApproachLadderCardTest` |
| detekt | existing, zero-baseline | Static analysis | Zero-baseline policy |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| New param order at the END of the signature | Inserting `router` pair next to `offlineOnly` pair | REJECTED: shifts positional binding of `maxTierId`/`modifier`; breaks v2.4 positional callers (`VoiceI18nSourceCompatTest` pins the 6-positional shape) |
| Two `SegmentedOptionSelector`s | A custom 4-way control / Switch | REJECTED: D-02 + "mirror the offline toggle"; SegmentedOptionSelector already handles disabled/contrast/semantics |

**Installation:** none. **Version verification:** no external packages are installed or upgraded, so no registry lookup is applicable.

## Package Legitimacy Audit

No external packages are installed by this phase.

| Package | Registry | Age | Downloads | Source Repo | Verdict | Disposition |
|---------|----------|-----|-----------|-------------|---------|-------------|
| (none) | — | — | — | — | — | N/A |

**Packages removed due to [SLOP] verdict:** none
**Packages flagged as suspicious [SUS]:** none

## Architecture Patterns

### System Architecture Diagram

```
Consumer app state                      yahirandroidtaste (this repo)
(router: Boolean?, handler)    ┌──────────────────────────────────────────────┐
        │                      │ ApproachLadderCard(ladder, offlineOnly?, ... │
        │ router/onRouterChange│   router?, onRouterChange?, routerOn/OffLabel)│
        ├─────────────────────►│                                              │
        │                      │ require((router==null)==(onRouterChange==null))
        │                      │      │ both-or-neither else IllegalArgumentException
        │                      │      ▼                                        │
        │                      │ Surface > Column                              │
        │                      │   ├─ ladder rungs (selectableGroup if cap)    │
        │                      │   │     row tap ──► onMaxTierChange(rung.id)  │ (unchanged; no per-rung nav)
        │                      │   ├─ if offlineOnly!=null && cb!=null:        │
        │                      │   │     SegmentedOptionSelector[Online|Offline only]
        │                      │   └─ if router!=null && cb!=null:  (NEW, below)│
        │                      │         SegmentedOptionSelector[routerOff|routerOn]
        │  onRouterChange(Bool)│           onSelect(idx) ──► cb(idx == 1)      │
        │◄─────────────────────┤                                              │
        ▼                      └──────────────────────────────────────────────┘
 consumer updates state → recomposition → selectedIndex = if (router) 1 else 0
 (router NEVER feeds the per-rung isEffective derivation — display + emit only)
```

### Recommended Project Structure (no new files)
```
src/main/java/io/github/ygaray/yahirandroidtaste/
├── component/ApproachLadderCard.kt            # EDIT: params, require, render, KDoc
└── explorer/VoiceCommandFamilyScreen.kt       # EDIT: ApproachLadderCardFixture router demo state (D-02)
src/test/java/io/github/ygaray/yahirandroidtaste/
├── component/ApproachLadderCardTest.kt        # EDIT: router tests
├── component/VoiceI18nSourceCompatTest.kt     # EDIT (optional): router-null positional pin
└── explorer/GalleryDemoInteractionTest.kt     # EDIT or sibling: render the registry fixture
api.txt                                         # REGEN via ./gradlew apiDump (same commit as source)
```

### Pattern 1: Clone of the offline pair (current file state, verbatim)

Current signature tail, `ApproachLadderCard.kt:80-92` (read this session):

```kotlin
@Composable
fun ApproachLadderCard(
    ladder: List<ApproachRungUiModel>,
    offlineOnly: Boolean? = null,
    onOfflineOnlyChange: ((Boolean) -> Unit)? = null,
    maxTierId: String? = null,
    onMaxTierChange: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    unavailableLabel: String = "Unavailable",
    cappedLabel: String = "Capped",
    needsNetworkLabel: String = "Needs network",
    onlineLabel: String = "Online",
    offlineOnlyLabel: String = "Offline only"
) {
```

Current `require()` for the offline pair, `:102-106` (template to clone):

```kotlin
    require((offlineOnly == null) == (onOfflineOnlyChange == null)) {
        "ApproachLadderCard: offlineOnly and onOfflineOnlyChange must both be null or both be " +
            "non-null (got offlineOnly=$offlineOnly, onOfflineOnlyChange=" +
            "${if (onOfflineOnlyChange == null) "null" else "non-null"})"
    }
```

Current offline render block, `:141-151` (template to clone; testTag `"approach_ladder_card_offline_toggle"`, `index == 1` = offline-only ON):

```kotlin
            if (offlineOnly != null && onOfflineOnlyChange != null) {
                SegmentedOptionSelector(
                    selectedIndex = if (offlineOnly) 1 else 0,
                    options = listOf(onlineLabel, offlineOnlyLabel),
                    onSelect = { index -> onOfflineOnlyChange(index == 1) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimens.ContentSpacing)
                        .testTag("approach_ladder_card_offline_toggle")
                )
            }
```

**Prescribed edit (reproduced green in scratch):**

1. Signature — append LAST, in this order (the callback is NOT last, so no future trailing-lambda trap like v2.3.0's `showTagColors`; the last param stays a `String`):
```kotlin
    offlineOnlyLabel: String = "Offline only",
    router: Boolean? = null,
    onRouterChange: ((Boolean) -> Unit)? = null,
    routerOnLabel: String = "Router on",
    routerOffLabel: String = "Router off"
```
2. `require()` — add after the offline `require` (same message shape, names both params):
```kotlin
    require((router == null) == (onRouterChange == null)) {
        "ApproachLadderCard: router and onRouterChange must both be null or both be non-null " +
            "(got router=$router, onRouterChange=${if (onRouterChange == null) "null" else "non-null"})"
    }
```
3. Render — directly AFTER the offline block, still inside the outer `Column` (D-02: card bottom, below offline toggle). Options order is `listOf(routerOffLabel, routerOnLabel)` so `index == 1` is ON, matching `listOf(onlineLabel, offlineOnlyLabel)` where index 1 is the "active" state:
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
4. KDoc — add `@param router`, `@param onRouterChange` (pair, `require`, null hides, "policy toggle, NOT per-rung navigation"), `@param routerOnLabel`, `@param routerOffLabel` (caller-localizable, English defaults `"Router on"` / `"Router off"`, rendered verbatim; the "selected"/"not selected" state words are announced by `SegmentedOptionSelector` and stay English — same sentence as the offline labels). Also amend the class-level sentence "Every control is hideable-by-null-prop… a `null` offlineOnly/onOfflineOnlyChange pair hides the toggle entirely" to mention the router pair.

Testtag name (Claude's Discretion): `approach_ladder_card_router_toggle` — mirrors `approach_ladder_card_offline_toggle`; unique in the repo (no prior `router` tag; a repo grep found "router" only as an icon name in `IconPickerGrid.kt`).

### Pattern 2: api.txt + lane handling (established by Phases 15 and 16)

- `api.txt` currently holds ONE line for the composable [VERIFIED: /home/yahir/Projects/Reusable/android/yahirandroidtaste/api.txt:58], ending in `optional String onlineLabel, optional String offlineOnlyLabel);`.
- After the edit, `apiDump` rewrites that single line to end `... optional String offlineOnlyLabel, optional Boolean? router, optional kotlin.jvm.functions.Function1<java.lang.Boolean,kotlin.Unit>? onRouterChange, optional String routerOnLabel, optional String routerOffLabel);` [VERIFIED: scratch `apiDump` diff this session — exactly 1 line changed, all else identical].
- Commit discipline (`tools/README-api-guard.md` "API dump discipline"): run `./gradlew apiCheck` BEFORE `apiDump` (authoritative additive gate), then `apiDump`, then commit source + tests + `api.txt` together.
- The pre-commit hook (`tools/hooks/pre-commit`) blocks a lane-2/3 commit unless `HUB_LANE_OVERRIDE` equals the DETECTED lane exactly. Phase 15 observed lane 2 on its first commits and used `HUB_LANE_OVERRIDE=2` (not the planned 3); Phase 16 planned `=3`. The detected lane is environment-sensitive (see Pitfall 2). **The plan must say: "if the hook blocks, re-run the identical commit with `HUB_LANE_OVERRIDE=<lane the hook printed>`", not hard-code 2 or 3.** The raw-line guard reporting a lane for the whole of v2.5 is the declared, known false positive (Phase 15 D-01); `apiCheck` is the real gate.

### Anti-Patterns to Avoid
- **Inserting the router pair beside the offline pair** — shifts positional params; breaks v2.4 callers and `VoiceI18nSourceCompatTest`'s positional shape.
- **Feeding `router` into the rung `isEffective`/`capRank` derivation** — the card displays + emits only (class KDoc, D-06); Router is a policy toggle, not a rung filter. Do not touch `RungRow`/`CapControl`.
- **Making rows navigate / adding a gesture** — row click remains the cap selector.
- **Adding a new public composable or a registry entry** — param addition only.
- **Regenerating a detekt baseline / hand-editing api.txt** — use `apiDump`.
- **Putting English defaults in a private helper** — defaults live only on the public signature (Phase 15 pattern); here there is no private helper involved.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Two-state segmented control | A custom Row of Buttons | `SegmentedOptionSelector` | Already provides `contentDescription = "$label, selected|not selected"`, M3 contrast, exactly-2 `require` |
| api.txt maintenance | Hand-editing the signature line | `./gradlew apiDump` | Metalava formatting (`@KotlinOnly`, `optional`) must match byte-for-byte |
| Additive proof | Custom diff scripts | `./gradlew apiCheck`, and the Phase 15/16 closing-gate recipe (released-baseline `apiCheck`) | Established, reviewed gate |
| Pairing guard | A silent `if` hiding the toggle | `require()` clone | Loud failure is the repo convention (WR-01) |

**Key insight:** every piece of this phase already exists once in the file; the work is a faithful second copy plus the compatibility/gate discipline around it.

## Common Pitfalls

### Pitfall 1: Inserting params mid-signature
**What goes wrong:** Positional callers silently re-bind (e.g. `modifier` position taken by a router param) or fail to compile. **Why:** Kotlin binds positional args by order. **Avoid:** append after `offlineOnlyLabel`. **Warning sign:** `VoiceI18nSourceCompatTest.v240PositionalComposableCallShapes_compileAgainstV25Signatures` fails to compile.

### Pitfall 2: Pre-commit lane mismatch
**What goes wrong:** `HUB_LANE_OVERRIDE` is rejected ("BLOCKED") because the value must equal the detected lane. **Why:** the hook runs `classify-hub-change.sh --baseline v2.4.1`. With the hook's default ABSOLUTE `API_FILE` (`$ROOT/api.txt`), `verify-api-additive.sh` evaluates `git cat-file -e v2.4.1:/abs/path/api.txt`, which fails, so the API half DEGRADES ("baseline … has no … api.txt yet") and the lane is decided by the source-line guard; with a RELATIVE `API_FILE=api.txt` the same script reports lane 3 [VERIFIED: ran both this session — absolute: `LANE 1`; relative `API_FILE=api.txt`: `LANE 3 (mode=additive, baseline=v2.4.1)`, rc=3]. Phase 15 hit lane 2 and had to change its planned override. **Avoid:** never pre-commit-guess; read the hook's printed lane and re-run with that exact value. Do not use `--no-verify`.

### Pitfall 3: api.txt not regenerated in the same commit
**What goes wrong:** stale `api.txt`; the next `apiCheck` or the Phase 18 API-02 gate flags drift, and the hook may classify the source rewrite as lane 2 without the matching api change. **Avoid:** `apiCheck` → `apiDump` → `cmp` freshness (the Phase 15 tail `F=$(mktemp) && cp api.txt "$F" && ./gradlew apiDump && cmp "$F" api.txt && ./gradlew apiCheck`) → commit together.

### Pitfall 4: Copying Phase 16's "explorer unchanged" gate
**What goes wrong:** Phase 16's closing gate included `git diff --quiet v2.4.1 -- …/explorer …/SegmentedOptionSelector.kt …/feedback`. Phase 17 legitimately edits `explorer/` (D-02), so that check would fail if reused. **Avoid:** for Phase 17 keep `SegmentedOptionSelector.kt` and `feedback/` unchanged (still true; assert it), but allow `explorer/VoiceCommandFamilyScreen.kt` to differ. Note for Phase 18 closing gate too.

### Pitfall 5: Nothing composes the Explorer fixture today
**What goes wrong:** D-02's fixture wiring compiles but is never rendered by a test (`ComponentStatesMatrixTest` / `ComponentPlaygroundIntegrityTest` only check that `render`/`content` lambdas are non-null, not that they compose; `GalleryDemoInteractionTest` covers only `TagChipEditorContent`). A fixture bug (e.g. router state not hoisted, wrong index) would ship invisibly. **Avoid:** add a registry-reached render test (see Validation Architecture), exactly the `GalleryDemoInteractionTest` idiom (`ComponentRegistry.entries.first { it.name == "ApproachLadderCard" }.states.first { it.label == "Default" }.render`).

### Pitfall 6: Duplicate tags/semantics collide in the gallery
**What goes wrong:** the detail page composes several fixture instances (3 state cells + Variants), so `onNodeWithTag("approach_ladder_card_router_toggle")` is not unique there. **Avoid:** in the gallery test render ONE cell's lambda per test (as `GalleryDemoInteractionTest` does); component tests render a single card.

### Pitfall 7: Router leaking into per-rung derivation
**What goes wrong:** treating "Router off" as "rungs unavailable" changes today's rendering contract. **Avoid:** router is emit-only policy chrome; add a test asserting rung rendering is unchanged when `router` toggles (rung count, capped/needs-network labels identical).

### Pitfall 8: Accessibility state words stay English
`SegmentedOptionSelector` builds `"$label, selected|not selected"` (`SegmentedOptionSelector.kt:65` [VERIFIED: read this session]). Tests locate segments via these strings (`"Router off, selected"`, `"Router on, not selected"`); a localized label changes the prefix only. Known residual already declared in Phase 15; document in the new KDoc, do not fix here (SegmentedOptionSelector must stay unchanged).

## Code Examples

### Component tests to add to `ApproachLadderCardTest.kt` (mirror lines 76-102, 180-198, 281-318 of the current file)

Read this session; the offline analogues use `onNodeWithTag("approach_ladder_card_offline_toggle")`, `onNodeWithContentDescription("Offline only, not selected").performClick()`, and `assertThrows(IllegalArgumentException::class.java)`.

```kotlin
// renders + emits (SC1, SC2): index 1 -> true; default English copy
@Test fun `toggling router emits onRouterChange with the new value`() {
    var last: Boolean? = null
    composeTestRule.setContent {
        ApproachLadderCard(ladder = ladder, router = false, onRouterChange = { last = it })
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("approach_ladder_card_router_toggle").assertExists()
    composeTestRule.onNodeWithContentDescription("Router off, selected").assertExists()
    composeTestRule.onNodeWithContentDescription("Router on, not selected").performClick()
    composeTestRule.waitForIdle()
    assertEquals(true, last)
}

// null hides (SC3)
@Test fun `router toggle is not rendered when the pair is null`() {
    composeTestRule.setContent { ApproachLadderCard(ladder = ladder, router = null, onRouterChange = null) }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("approach_ladder_card_router_toggle").assertDoesNotExist()
    composeTestRule.onAllNodesWithContentDescription("Router", substring = true).assertCountEquals(0)
}

// pairing (SC4) -- BOTH directions
@Test fun `a non-null onRouterChange with a null router throws`() {
    assertThrows(IllegalArgumentException::class.java) {
        composeTestRule.setContent { ApproachLadderCard(ladder = ladder, router = null, onRouterChange = {}) }
        composeTestRule.waitForIdle()
    }
}
@Test fun `a non-null router with a null onRouterChange throws`() {
    assertThrows(IllegalArgumentException::class.java) {
        composeTestRule.setContent { ApproachLadderCard(ladder = ladder, router = true, onRouterChange = null) }
        composeTestRule.waitForIdle()
    }
}
```

Also add: (a) label override test (`routerOnLabel = "Routeur activé"`, `routerOffLabel = "Routeur désactivé"` -> `"Routeur activé, not selected"` clickable and still emits; English `"Router off, selected"` count 0); (b) default pin (omit labels -> `"Router off, selected"` exists for `router = false`; `"Router on, selected"` for `router = true`); (c) coexistence + independence (SC5): with `offlineOnly`, `maxTierId`, and `router` all set, tapping rung `Local` emits ONLY `onMaxTierChange("local")` (router/offline callbacks untouched), tapping the router segment emits ONLY `onRouterChange` and `onMaxTierChange` is not invoked; the offline toggle and router toggle tags both exist (distinct nodes); rung nodes still `Role.RadioButton` + selection unchanged; (d) placement: router toggle node's `boundsInRoot.top` > offline toggle's `boundsInRoot.top` (D-02, below), and both below the last rung.

### Explorer fixture (D-02) — `VoiceCommandFamilyScreen.kt`

Current fixture, `:311-326` (read this session):
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
Prescribed: add `initialRouter: Boolean = false`, `var router by remember { mutableStateOf(initialRouter) }`, pass `router = router, onRouterChange = { router = it }`. Wire the "Pressed / Selected" state cell (`:128-131`, currently `ApproachLadderCardFixture(initialOfflineOnly = true)`) to also pass `initialRouter = true` so the matrix shows both polarities. Leave the "every control hidden" Variants cell (`:334-338`) untouched (it now doubles as the "router null → hidden" demo; update its `SectionLabel` text to mention `router`) and the "combined subdued labels" cell untouched. No registry change (entry already exists).

### Source-compat pin (optional but cheap)
`VoiceI18nSourceCompatTest.v240PositionalComposableCallShapes_compileAgainstV25Signatures` already calls `ApproachLadderCard(emptyList(), false, { _: Boolean -> }, null, { _: String -> }, Modifier)` positionally through `modifier` (read this session, lines 61-69). It must keep compiling UNCHANGED — that is the proof that appending (not inserting) preserved positional binding. Optionally add a second lambda passing all 11 pre-Phase-17 args positionally through `offlineOnlyLabel` to pin the Phase 15 shape against Phase 17.

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Hardcoded English toggle segments | Caller-overridable defaulted `String` label params appended last | Phase 15 (`31595d4`) | Router toggle follows the same pattern from day one (runtime decision) |
| Unpaired optional props failing silently | `require()` pairing for both optional prop pairs | Phase 10 WR-01 | Router pair gets the third `require()` |

**Deprecated/outdated:** none relevant.

## Runtime State Inventory

Not applicable — additive feature phase, not a rename/refactor/migration. No stored data, live-service config, OS-registered state, secrets/env vars, or build-artifact renames are involved. (`router` is a new parameter name; no existing key/string is being renamed.)

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | The hook's detected lane for the Phase 17 commit(s) is 2 or 3 (could also be 1 on the very first commit if it sees only HEAD); the plan should follow the hook output, not a fixed value | Pattern 2 / Pitfall 2 | A wrongly hard-coded override makes the commit fail loudly (no silent damage). `core.hooksPath` points at `/home/yahir/.config/git/hooks-chain`, which I did not inspect, so I could not simulate the exact hook run |
| A2 | Adding the second `SegmentedOptionSelector` increases card height and the owner may want a visual look at the stacked toggles | Pitfalls / Validation (Gate-2) | Taste-only; surfaces at Gate-2 |
| A3 | TalkBack announcing two adjacent segmented controls is unambiguous because the labels differ ("Router off, selected" vs "Offline only, …") | Pitfall 8 | Minor a11y confusion; owner can judge at Gate-2 |

**Everything else in this document was verified in this session** (files read, scratch build run).

## Open Questions

1. **Which lane will the hook print?**
   - What we know: Phase 15 → lane 2; Phase 16 plan → lane 3; absolute vs relative `API_FILE` changes the answer (Pitfall 2).
   - What's unclear: the exact hook chain behavior under `core.hooksPath`.
   - Recommendation: planner writes "re-run with `HUB_LANE_OVERRIDE=<printed lane>`"; never bypass with `--no-verify`.

2. **Should the Explorer "Pressed / Selected" cell show router ON?**
   - Recommendation: yes (`initialRouter = true`) so both polarities are visible; trivial to revert, no API impact.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| JDK | Gradle build/tests | ✓ | OpenJDK 17.0.19 [VERIFIED: `java -version`] | — |
| Gradle wrapper + offline caches | `apiCheck`, `apiDump`, `testDebugUnitTest`, `detekt` | ✓ | `./gradlew … --offline` worked in a scratch copy (≈ minutes cold) | rerun with `-Dorg.gradle.vfs.watch=false` if "Already watching path" (Phase 15 note) |
| Android SDK (`local.properties` `sdk.dir`) | Robolectric/AGP | ✓ | present | — |
| Tester device / adb | Gate-1 self-UAT (post-execute workflow, not a planned task) | not probed | — | The Explorer fixture (D-02) gives a built-in on-device demo path, unlike Phase 16; a throwaway harness is the Phase 15/16 fallback (`AGENT-DEVICE-TESTING.md`) |

**Missing dependencies with no fallback:** none for planning/execution.

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit4 + Robolectric (`@Config(sdk = [35])`) + Compose UI test (`createComposeRule`); Metalava `apiCheck`; detekt |
| Config file | `build.gradle.kts` (existing); no install needed |
| Quick run command | `./gradlew testDebugUnitTest --tests '*ApproachLadderCardTest' --tests '*VoiceI18nSourceCompatTest' --tests '*GalleryDemoInteractionTest'` |
| Full suite command | `./gradlew testDebugUnitTest && ./gradlew apiCheck && ./gradlew detekt` |

(If Gradle fails with `Already watching path`, add `-Dorg.gradle.vfs.watch=false`. Baseline from Phase 15: full suite 663 tests, 0 failures, 22 skipped; `ApproachLadderCardTest` currently has 21 tests.)

### Phase Requirements → Test Map
| Req / SC | Behavior | Test Type | Automated Command | File Exists? |
|----------|----------|-----------|-------------------|-------------|
| VAPPR-04 / SC1 | Both non-null → toggle renders reflecting state (`Router off, selected` for false; `Router on, selected` for true) | Compose UI | `./gradlew testDebugUnitTest --tests '*ApproachLadderCardTest'` | ✅ file; new cases ❌ Wave 0 |
| VAPPR-04 / SC2 | Tapping the other segment invokes `onRouterChange(new)` (index 1 → true, index 0 → false) | Compose UI | same | new cases |
| VAPPR-04 / SC3 | Both null → no router node, no `Router…` content description; existing tests unchanged and green; (and) positional v2.4 shape still compiles | Compose UI + compile-only | `… --tests '*ApproachLadderCardTest' --tests '*VoiceI18nSourceCompatTest'` | existing + new |
| VAPPR-04 / SC4 | Exactly one of the pair → `IllegalArgumentException` (both directions) | Compose UI (`assertThrows`) | same | new cases |
| VAPPR-04 / SC5 | Row tap still emits `onMaxTierChange`; router tap emits only `onRouterChange`; rung semantics (`Role.RadioButton`, selected) unaffected by router | Compose UI | same | new cases |
| D-01 (label overrides) | `routerOnLabel`/`routerOffLabel` replace segment text; defaults English; still emits | Compose UI | same | new cases |
| D-02 (placement + fixture) | Router toggle below offline toggle; gallery fixture renders router toggle and toggles live | Compose UI (registry-reached render) | `… --tests '*GalleryDemoInteractionTest'` (or a sibling `ApproachLadderGalleryFixtureTest`) | ❌ Wave 0 (see Pitfall 5) |
| API additive | `apiCheck` green vs committed AND vs released `v2.4.1`; api.txt delta exactly the one `ApproachLadderCard` line | gradle + git | see Closing Gate | tooling exists |
| INV / quality | no non-library import, no Hilt host, detekt zero-baseline | gradle + git | see Closing Gate | tooling exists |

### Sampling Rate
- **Per task commit:** quick run command + `./gradlew detekt` + (api tail below when `ApproachLadderCard.kt` signature changed).
- **Per wave merge / phase gate:** full suite command, plus the closing gate.
- **Per-task api tail (copy of Phase 15's):** `./gradlew apiCheck && F=$(mktemp) && cp api.txt "$F" && ./gradlew apiDump && cmp "$F" api.txt` — note `apiCheck` BEFORE `apiDump`; after `apiDump` run `./gradlew apiCheck` again. The first `cmp` is expected to differ on the signature-changing task (commit the regenerated file); on later tasks it must be identical.

### Closing gate (adapt Phase 16's 16-03 Task 2 commands; verify-only, no commit)
1. Full suite green incl. `ComponentRegistryDriftGuardTest`, `DomainVocabularyDriftGuardTest` (no new composable, head tokens unchanged), `GeneratedSymbolDriftGuardTest`, `GalleryDemoInteractionTest`.
2. Released-baseline additive proof: `( git show v2.4.1:api.txt > api.txt && ./gradlew apiCheck; rc=$?; git checkout -- api.txt; exit $rc )` then confirm `git status --porcelain -- api.txt` is empty.
3. Removed-line allowlist vs `v2.4.1` is UNCHANGED at the ten Phase 15/16 symbols (Phase 17 must remove no new symbol; `ApproachLadderCard`'s line is already in the allowlist): `D=$(git diff v2.4.1 -- api.txt) && ! printf '%s\n' "$D" | grep -E '^-[^-]' | grep -vE 'ProviderKeyCard|ModelSelectCard|ClarificationBar|ApproachLadderCard|HandledByUiModel|ProposedItemUiModel|UndoRefusedUiModel|UndoRowUiModel|FailureActionUiModel|VoiceOutcomeUiState.Failure' | grep -q .`
4. Phase 17 delta: against the pre-Phase-17 HEAD (record the SHA at plan start, e.g. `335e97f`), `git diff <SHA> HEAD -- api.txt` has exactly ONE removed and ONE added line, both containing `ApproachLadderCard(`, and the added line contains `optional Boolean? router` and `optional String routerOffLabel`.
5. `git diff --quiet v2.4.1 -- src/main/java/io/github/ygaray/yahirandroidtaste/component/SegmentedOptionSelector.kt src/main/java/io/github/ygaray/yahirandroidtaste/feedback` (do NOT include `explorer/`; see Pitfall 4).
6. Import / Hilt checks vs `v2.4.1` as in Phase 16 (`^\+import ` outside androidx/kotlin/android/library namespace → none; no `@HiltAndroidApp|@AndroidEntryPoint`).
7. No tag cut (`git tag --points-at HEAD` empty; `git.create_tag` is false), tree clean for `src`/`api.txt`.

### Wave 0 Gaps
- [ ] New router cases in `src/test/.../component/ApproachLadderCardTest.kt` (SC1-SC5, D-01 overrides, placement).
- [ ] Registry-reached gallery render test for the `ApproachLadderCard` fixture (extend `GalleryDemoInteractionTest.kt` or add a sibling) — D-02 has no automated coverage otherwise.
- [ ] (Optional) extra positional pin in `VoiceI18nSourceCompatTest.kt`.
- Framework install: none.

### Manual-only (Gate-2, owner; non-blocking, register `.planning/uat-pending/17-approachladdercard-router-on-off-toggle.md` per the Phase 15/16 convention)
- Visual: two stacked segmented toggles at the card bottom — spacing, card height growth, contrast in light/dark (UX is first-class for this owner). Gate-1 self-UAT on the tester rig is driven by the post-execute verify workflow (Phase 16: `fd34194`), not a planned task; the Explorer fixture is its demo surface.

## Security Domain

`security_enforcement` is enabled (ASVS level 1, block on high) [VERIFIED: .planning/config.json]. This phase adds a presentational boolean toggle with no network, storage, auth, or crypto surface.

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2 Authentication | no | — |
| V3 Session Management | no | — |
| V4 Access Control | no | — (the consumer decides what the router toggle does) |
| V5 Input Validation | minimal | Type-level (`Boolean?`); pairing enforced by `require()`; label params are rendered verbatim as plain `Text`/content-description, never parsed/evaluated |
| V6 Cryptography | no | — |

### Known Threat Patterns for this stack

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| Guard bypass hiding a real API break (`HUB_LANE_OVERRIDE`, `--no-verify`) | Tampering | Released-baseline `apiCheck` + removed-line allowlist + exactly-one-line Phase 17 delta (Closing Gate 2-4); never `--no-verify` |
| One-way-dependency erosion (importing an engine/router-policy type) | Tampering / Elevation of privilege (supply chain) | Closing Gate 6 import allowlist; the API takes `Boolean?` + `(Boolean) -> Unit` only |
| Information disclosure via exception message | Information disclosure | The `require()` message interpolates only `router` (a Boolean) and a "null"/"non-null" word — no user data |
| Silent half-configured toggle | Repudiation / UX-integrity | `require()` fails loudly on a half pair |

## Sources

### Primary (HIGH confidence — read/run this session)
- `/home/yahir/Projects/Reusable/android/yahirandroidtaste/src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt` (full file, Phases 15+16 landed)
- `/home/yahir/Projects/Reusable/android/yahirandroidtaste/src/test/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCardTest.kt` (full file)
- `/home/yahir/Projects/Reusable/android/yahirandroidtaste/src/main/java/io/github/ygaray/yahirandroidtaste/component/SegmentedOptionSelector.kt`
- `/home/yahir/Projects/Reusable/android/yahirandroidtaste/src/main/java/io/github/ygaray/yahirandroidtaste/explorer/VoiceCommandFamilyScreen.kt` (full file)
- `/home/yahir/Projects/Reusable/android/yahirandroidtaste/src/test/java/io/github/ygaray/yahirandroidtaste/component/VoiceI18nSourceCompatTest.kt`, `…/explorer/GalleryDemoInteractionTest.kt`
- `/home/yahir/Projects/Reusable/android/yahirandroidtaste/api.txt:57-59`; `build.gradle.kts:1-60`; `tools/hooks/pre-commit`, `tools/classify-hub-change.sh`, `tools/verify-api-additive.sh`, `tools/README-api-guard.md`
- Phase 15/16 artifacts: `15-01-SUMMARY.md`, `15-RESEARCH.md`, `15-VALIDATION.md`, `16-03-PLAN.md`, `16-03-SUMMARY.md`, `16-VALIDATION.md`, `uat-pending/16-a11y-failure-enrichment.md`
- Scratch reproduction (copy of repo in the session scratchpad; real repo untouched): `apiCheck` rc 0; `apiDump` diff = exactly one changed line; `detekt` 0 code smells; `ApproachLadderCardTest` `tests="23" skipped="0" failures="0"`.

### Secondary / Tertiary
- None; no web sources were needed (no external library or API involved).

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH — no new dependencies; all existing, files read.
- Architecture: HIGH — change reproduced end-to-end in scratch.
- Pitfalls: HIGH for 1, 3-8 (read/ran); MEDIUM for lane prediction (A1: hook chain not simulated).

**Research date:** 2026-10-05
**Valid until:** 2026-11-04 (stable; invalidated if Phase 16/15 files change again before Phase 17 executes — refresh by re-reading `ApproachLadderCard.kt:80-92` and `api.txt:58`)
