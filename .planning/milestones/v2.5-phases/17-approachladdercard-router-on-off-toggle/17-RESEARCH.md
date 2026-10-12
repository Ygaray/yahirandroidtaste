# Phase 17: ApproachLadderCard Router ON/OFF toggle - Research

**Researched:** 2026-10-05 (REFRESH — re-verified against `main` HEAD `d83e467`; supersedes the earlier 17-RESEARCH.md written before D-03)
**Domain:** Additive Jetpack Compose param pair on a published, Metalava-tracked composable that now also carries a hidden v2.4.1 binary-compat shim (no new libraries)
**Confidence:** HIGH — the full Phase 17 change (signature + require + render + fixture) was reproduced in a scratch clone (`git clone --local`, real repo untouched): `assembleRelease`, `apiCheck`, `apiDump`, `detekt`, the javap binary gate (missing=0), and the shim/compat/drift tests all green.

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- **D-01 [router-copy]:** Mirror whatever Phase 15 settles for the offline toggle — if Phase 15 makes the offline-toggle labels caller-overridable (`onlineLabel`/`offlineOnlyLabel`), add matching `routerOnLabel`/`routerOffLabel` params so the card has no lone non-localizable toggle; otherwise hardcode. Default copy: "Router off" / "Router on". Resolution (human). _(provisional — refresh at execution; depends on Phase 15)_ — **Reversibility:** costly — adding the label params later is another api.txt append across the published signature.
- **D-02 [toggle-placement]:** Render the router `SegmentedOptionSelector` below the offline toggle (card bottom), and extend `VoiceCommandFamilyScreen`'s fixture to wire a `router` demo state. _(source: ai-auto)_
- **D-03 [binary-compat] (LOCKED — orchestrator yahir-gsd-control-plane-3b ruling, 2026-10-05, F2 of the binary-compat fix plan):** Never change a **tagged** public composable's signature in place. The binding rules for this phase:
  - **Append order:** `router: Boolean? = null`, `onRouterChange: ((Boolean) -> Unit)? = null`, `routerOnLabel: String = "Router on"` and `routerOffLabel: String = "Router off"` are appended AFTER the current last parameter (`offlineOnlyLabel`). This makes the change append-only at source level.
  - **The F1 shim stays:** `ApproachLadderCard`'s `@Deprecated(level = HIDDEN)` v2.4.1 overload (quick 261005-dmc, `07f66cf`) MUST keep its exact v2.4.1 parameter list. It MUST keep delegating to the current overload by **named** args, so the new router params take their defaults. Its `VoiceBinaryCompatShimTest` case MUST stay green, including the `$default`-mask render-through. Update the test only if the current signature it calls with all params changes.
  - **No shim for the Phase-15 shape:** the Phase-15 v2.5 shape is untagged (v2.5.0 is not cut). Only tagged signatures get a hidden delegate, so Phase 17 adds **no** new shim for it. Each published signature keeps exactly one hidden shim.
  - **Data classes (variant K, orchestrator ruling 2026-10-05):** `@JvmOverloads` alone only covers Java callers. A Kotlin caller compiled against a tagged shape links to the synthetic `(…, int, DefaultConstructorMarker)` ctor and to `copy$default`. To keep both: keep `@JvmOverloads` and the visible legacy `copy`; add a `@Deprecated(HIDDEN)` ctor declared with the tagged shape's exact synthetic params (`…, mask: Int, marker: DefaultConstructorMarker?`); add a private companion `@JvmStatic @JvmName("copy\$default")` shim; give each a per-class reflection test plus a behavioural test with a non-zero mask. K members are needed **only for TAGGED shapes**: add them at the release cut, not mid-milestone. A property this phase appends to a data class needs no K member for the untagged v2.5 shape. The v2.4.1 K members (quick 261005-e2e / F1c) must stay intact. The recipe is in the quick 261005-e2e SUMMARY.
  - **Gates:** Metalava `apiCheck` is the **source-level** gate only. The **binary** gate is the javap descriptor diff of the release AAR against the v2.4.1 JitPack AAR, with zero missing public descriptors (method in quick 261005-dmc/261005-e2e SUMMARY; it becomes `tools/verify-binary-abi.sh` at the v2.5.0 cut, F3). Run it as part of the phase's closing gate. _(source: orchestrator ruling — not revisitable by the planner)_
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
| VAPPR-04 | `ApproachLadderCard` gains a Router ON/OFF toggle via `router: Boolean? = null` + `onRouterChange: ((Boolean) -> Unit)? = null`, `require()`-paired exactly like `offlineOnly`/`onOfflineOnlyChange`; `null` hides the toggle. No per-rung navigation. | Insertion points on the CURRENT file, append order, `require()` clone, render block, testTag, api.txt delta, tests, fixture, hook-lane behaviour and the D-03 binary gate are all specified below and were reproduced green in a scratch clone. |
</phase_requirements>

## What changed since the earlier RESEARCH.md (read this first)

| Earlier research assumed | Now true at HEAD `d83e467` | Effect on the plan |
|---|---|---|
| One `ApproachLadderCard` overload | **Two**: the current one (`ApproachLadderCard.kt:79-154`) and a `@Deprecated(HIDDEN)` v2.4.1 shim (`:156-175`) | Router params go on the FIRST (current) overload ONLY. The shim is not touched. |
| Closing gate = `apiCheck` vs `v2.4.1` api.txt | Add the **javap binary gate** (D-03); `apiCheck` is source-level only | New closing-gate step (see "D-03 constraints", §Closing binary gate) |
| Hook lane for Phase 17 is "2 or 3, follow the hook" | Measured: lane is **1 or 2**, decided purely by whether the staged `src/main` diff rewrites an existing line (hook API half is degraded) | Plan can KEEP lane 1 by being append-only; see "Pre-commit hook" |
| `VoiceBinaryCompatShimTest` n/a | Exists, 4 tests, ApproachLadderCard case at `:70-135` | Must stay green and unmodified |
| Full suite baseline 663 tests | 734 tests, 0 failures, 22 skipped (quick 261005-e2e SUMMARY) | New baseline for "no regression" |
| `explorer/` fixture edit rewrites the "Pressed / Selected" cell | That rewrite is a pre-existing-line edit => **lane 2** (measured) | Prefer an append-only fixture edit (below) |

Still valid from the earlier research (re-verified by reading the files or re-running): the template lines to clone, appending after `offlineOnlyLabel`, `approach_ladder_card_router_toggle` tag, option order `listOf(routerOffLabel, routerOnLabel)` with `index == 1 -> ON`, the registry-reached gallery render test need, no new `ComponentRegistry` entry, Gate-2 visual item.

## Project Constraints (from CLAUDE.md)

Extracted from `/home/yahir/Projects/Reusable/android/yahirandroidtaste/CLAUDE.md` (`.claude/CLAUDE.md` only points at it; no `.claude/skills` or `.agents/skills` dirs exist). The planner must verify compliance:

- **One-way dependency / INV-01:** library imports no host code, no secrets, no domain assumptions. The router toggle takes a plain `Boolean?` + callback; never import a voice-engine/router-policy type.
- **Bindings-only Hilt:** never add `@HiltAndroidApp` / `@AndroidEntryPoint`.
- **`ComponentRegistry` drift guard:** `ApproachLadderCard` is already registered; this phase adds a param, NOT a composable. No registry/allowlist change. Do not add a new public top-level `@Composable`.
- **Interaction conventions:** conditional-render-no-dead-space (null hides with zero space); row-click selects the cap (no per-rung navigation).
- **Detekt zero-baseline:** keep `./gradlew detekt` green; never regenerate a baseline.
- **Commands (no module prefix):** `./gradlew testDebugUnitTest`, `./gradlew detekt`, `./gradlew apiDump` / `apiCheck`.
- **Human-gated shipping:** NO tag, NO consumer repin (Phase 19 cuts v2.5.0). Commit on `main` (hub-sequential, no consumer worktrees).
- **Tags are immutable / never `main-SNAPSHOT`** (not relevant to code changes here, relevant to the gate baseline: use the immutable `v2.4.1` AAR).
- **Global (user CLAUDE.md):** UI placement/spacing is first-class (Gate-2 visual item); failures loud (`require()` throws, never silently omits). Flag any state changes made.

## Summary

The Phase 17 change is a faithful second copy of the offline toggle plus the D-03 discipline around it. Append `router`, `onRouterChange`, `routerOnLabel`, `routerOffLabel` (this order) after `offlineOnlyLabel` on the current overload, add a third `require()`, render a second `SegmentedOptionSelector` below the offline one, regenerate `api.txt` (exactly one changed line), extend the Explorer fixture, add tests. There is no new library, no new composable, no data-class change.

D-03 constrains it in three concrete ways, all measured this session in a scratch clone with the change applied: (1) the hidden v2.4.1 shim is untouched and still compiles because it delegates by **named** args, so `router`/`onRouterChange` default to `null` (toggle hidden) and the labels to English; (2) the new current-overload descriptor is `(List,Boolean,Function1,String,Function1,Modifier,String,String,String,String,String,Boolean,Function1,String,String,Composer,int,int,int)V`; the v2.4.1 descriptor `(List,Boolean,Function1,String,Function1,Modifier,Composer,int,int)V` is still present (the shim), and the full javap diff vs the v2.4.1 JitPack AAR reports `base=2526 head=2584 missing=0`; (3) no data class is touched, so no K member and the ten e2e synthetics stay as they are.

**Primary recommendation:** Do the change append-only everywhere (signature, `require`, render block, KDoc `@param` additions, an appended fixture cell) so the pre-commit hook classifies it LANE 1 with no override; leave `VoiceBinaryCompatShimTest` byte-identical as the proof the shim still holds; make `apiDump` part of the same commit as the signature change; close the phase with the javap binary gate (missing=0) in addition to `apiCheck`.

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Render the Router ON/OFF segmented control | Client UI library (`component/`) | — | Presentational composable; state hoisted |
| Own router on/off state and act on it | Consumer app (ViewModel/engine) | — | Library knows no router/engine concept (INV-01); emits `onRouterChange(Boolean)` only |
| `router`/`onRouterChange` pairing guard | Client UI library (`require()`) | — | Same fail-fast contract as the offline pair |
| Localized segment text | Consumer app (passes strings) | Library (English defaults) | Hub localizes nothing (INV-01) |
| v2.4.1 binary linkage for already-compiled consumers | Library (hidden `@Deprecated(HIDDEN)` overload) | Build tooling (javap gate) | D-03; shim exists since `07f66cf`, untouched here |
| Gallery demo of the toggle | Explorer (`explorer/VoiceCommandFamilyScreen.kt`) | — | Explorer-only fixtures, drift-guard denylisted, never registered |
| Public-API / ABI proof | Metalava `apiCheck` (source) | javap AAR diff (binary) | Two different gates; neither subsumes the other |

## Standard Stack

No new dependencies. Everything is already in the module.

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| `SegmentedOptionSelector` (in-repo `component/SegmentedOptionSelector.kt`) | n/a | Two-option toggle; `require(options.size == 2)`; emits `contentDescription = "$label, selected|not selected"` | Already what the offline toggle uses [VERIFIED: file read in prior research; usage read in `ApproachLadderCard.kt:141-150`] |
| Jetpack Compose BOM / Material3 | 2026.04.01 | UI | [CITED: CLAUDE.md Toolchain] |
| Metalava Gradle plugin `me.tylerbwong.gradle.metalava` | 0.5.0 | `apiDump` / `apiCheck` | [CITED: tools/README-api-guard.md] |
| JUnit4 + Robolectric (`@Config(sdk = [35])`) + `createComposeRule` | existing | Tests | Harness in `ApproachLadderCardTest`, `VoiceBinaryCompatShimTest` |
| detekt | existing, zero-baseline | Static analysis | Zero-baseline policy |
| `javap` (JDK 17) + `unzip` + `comm` | JDK 17.0.19 | Binary descriptor gate | [VERIFIED: ran this session] |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Append router params LAST | Insert beside the offline pair | REJECTED (D-03 + positional callers; `VoiceI18nSourceCompatTest` pins the 6-positional shape) |
| Add a second hidden shim for the Phase-15 11-param shape | — | REJECTED by D-03 (untagged shape) |
| One hand-rolled 4-way control | Two `SegmentedOptionSelector`s | REJECTED (D-02) |

**Installation:** none. **Version verification:** no external packages installed/upgraded; no registry lookup applicable.

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
 v2.4.x-compiled consumer binary                 v2.5 source consumer
 calls ApproachLadderCardKt.ApproachLadderCard   (router?, onRouterChange?, labels...)
   (List,Boolean,Fn1,String,Fn1,Modifier,               │
    Composer,int,int)  [v2.4.1 descriptor]              │
        │                                               ▼
        ▼                                 ┌──────────────────────────────────────────┐
 ┌─────────────────────────────┐          │ CURRENT overload (15 value params)       │
 │ HIDDEN shim (UNTOUCHED)     │ named    │ require(maxTier pair)                    │
 │ 6 params, ACC_SYNTHETIC     │ args ──► │ require(offline pair)                    │
 │ router/labels not passed =  │          │ require(router pair)        <- NEW       │
 │ router=null, English labels │          │ Surface > Column                         │
 └─────────────────────────────┘          │   ├ rungs (selectableGroup if cap)       │
                                          │   │    row tap -> onMaxTierChange(id)    │ (unchanged, no per-rung nav)
                                          │   ├ if offline pair: SegmentedOptionSel. │
                                          │   └ if router pair:  SegmentedOptionSel. │ <- NEW, below offline
                                          │        [routerOff | routerOn]            │
                                          │        onSelect(i) -> onRouterChange(i==1)
                                          └──────────────────────────────────────────┘
 router NEVER feeds isEffective/capRank (display + emit only)
```

### Recommended Project Structure (no new production files)
```
src/main/java/io/github/ygaray/yahirandroidtaste/
├── component/ApproachLadderCard.kt          # EDIT current overload ONLY: params, require, render, KDoc (+shim block untouched)
└── explorer/VoiceCommandFamilyScreen.kt     # EDIT fixture: router demo state (D-02)
src/test/java/io/github/ygaray/yahirandroidtaste/
├── component/ApproachLadderCardTest.kt      # EDIT: router tests (currently 21 tests)
├── component/VoiceBinaryCompatShimTest.kt   # DO NOT EDIT (byte-identical; see D-03 section)
├── component/VoiceI18nSourceCompatTest.kt   # DO NOT EDIT (byte-identical per e2e gate); optional pin goes in a NEW file
└── explorer/GalleryDemoInteractionTest.kt   # EDIT or sibling: registry-reached render of the ApproachLadderCard fixture
api.txt                                       # REGEN via ./gradlew apiDump (same commit as the signature change)
```

### Pattern 1: Clone of the offline pair (CURRENT file state)

Current signature `ApproachLadderCard.kt:79-92` [VERIFIED: Read this session]; the last param is verbatim:
```kotlin
    onlineLabel: String = "Online",
    offlineOnlyLabel: String = "Offline only"
) {
```
Current `require()` for the offline pair, `:102-106`; current offline render block `:141-151` (tag `"approach_ladder_card_offline_toggle"`, `selectedIndex = if (offlineOnly) 1 else 0`, `options = listOf(onlineLabel, offlineOnlyLabel)`, `onSelect = { index -> onOfflineOnlyChange(index == 1) }`), file ends the current overload at `:154`; the shim begins with its comment at `:156`.

**Prescribed edit (all four steps reproduced green in the scratch clone):**

1. Signature of the FIRST overload only — append LAST. The `offlineOnlyLabel` line only gains a trailing comma (the DS-05 guard normalizes a trailing `,`, so this is NOT a rewrite):
```kotlin
    offlineOnlyLabel: String = "Offline only",
    router: Boolean? = null,
    onRouterChange: ((Boolean) -> Unit)? = null,
    routerOnLabel: String = "Router on",
    routerOffLabel: String = "Router off"
) {
```
2. Third `require()` directly after the offline `require` (before `val effectiveOfflineOnly`):
```kotlin
    require((router == null) == (onRouterChange == null)) {
        "ApproachLadderCard: router and onRouterChange must both be null or both be non-null " +
            "(got router=$router, onRouterChange=${if (onRouterChange == null) "null" else "non-null"})"
    }
```
3. Render directly AFTER the offline block, inside the same outer `Column` (D-02: below offline). `listOf(routerOffLabel, routerOnLabel)` so `index == 1` is ON, matching `listOf(onlineLabel, offlineOnlyLabel)`:
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
4. KDoc: ADD `@param router`, `@param onRouterChange` (pair, `require`, null hides, "policy toggle, NOT per-rung navigation"), `@param routerOnLabel`, `@param routerOffLabel` (caller-localizable, English defaults, rendered verbatim; "selected"/"not selected" state words stay English, announced by `SegmentedOptionSelector`) as NEW lines after the existing `@param offlineOnlyLabel` block (ends at KDoc line 77). Do NOT reword the class-level sentence at lines 32-35 if you want lane 1 (see "Pre-commit hook"); a new trailing paragraph is append-only.

**Do NOT touch the shim at `:156-175`.** In particular do not add router args to its delegation call and do not add params to it.

Testtag (Claude's Discretion, recommended): `approach_ladder_card_router_toggle` — mirrors the offline tag; no prior `router` tag in the repo [ASSUMED: grep result from the earlier research was "router" only as an icon name in `IconPickerGrid.kt`; re-grep at execution].

### Pattern 2: Binary shim idiom (reference only — NOT new work)
The shim at `:156-175` is the pattern D-03 mandates for tagged shapes: `@Deprecated("Binary compatibility with v2.4.x", level = DeprecationLevel.HIDDEN) @Composable fun ApproachLadderCard(<v2.4.1 6 params with defaults>) = ApproachLadderCard(ladder = ladder, offlineOnly = offlineOnly, ..., modifier = modifier)`. Because every call into the current overload is by name, appending params to the current overload never requires editing it [VERIFIED: scratch clone built and `VoiceBinaryCompatShimTest` 4/4 green with the shim unedited].

### Anti-Patterns to Avoid
- **Inserting the router pair beside the offline pair** — breaks positional binding and `VoiceI18nSourceCompatTest`'s 6-positional call.
- **Editing the shim or adding a second shim** for the 11-param Phase-15 shape — violates D-03 ("exactly one hidden shim per published signature"; untagged shapes get none).
- **Feeding `router` into `isEffective`/`capRank`** — the card displays + emits only.
- **Adding gestures/navigation to rows** — row click remains the cap selector.
- **Adding a new public composable / registry entry**, regenerating a detekt baseline, hand-editing `api.txt`.
- **Reusing Phase 16's "explorer is byte-identical to v2.4.1" gate** — Phase 17 legitimately edits `explorer/`.
- **Using `HUB_LANE_OVERRIDE` / `--no-verify` pre-emptively** — read the hook's printed lane; with an append-only diff there should be none to override.

## D-03: how the binary-compat rule constrains this plan

This section is the refresh's addition. Every item was verified in the scratch clone unless tagged otherwise.

### 1. Append order — verified outcome
- `api.txt` delta after `apiDump`: exactly **one removed and one added line**, both the `ApproachLadderCard(` method line; the added line ends `... optional String offlineOnlyLabel, optional Boolean? router, optional kotlin.jvm.functions.Function1<java.lang.Boolean,kotlin.Unit>? onRouterChange, optional String routerOnLabel, optional String routerOffLabel);` [VERIFIED: scratch `git diff -U0 api.txt`; 2 changed lines].
- `apiCheck` passes with the committed `api.txt` BEFORE `apiDump`, and passes again with `v2.4.1`'s `api.txt` swapped in (rc 0) [VERIFIED: ran both].
- The v2.4.1 source-compat call (`VoiceI18nSourceCompatTest` 6-positional shape) still compiles; `VoiceI18nSourceCompatTest` 4/4 green [VERIFIED].

### 2. The shim stays and keeps delegating by named args
- Shim current text (read this session, `ApproachLadderCard.kt:156-175`): `@Deprecated("Binary compatibility with v2.4.x", level = DeprecationLevel.HIDDEN)`, params `ladder, offlineOnly = null, onOfflineOnlyChange = null, maxTierId = null, onMaxTierChange = null, modifier = Modifier`, body `= ApproachLadderCard(ladder = ladder, offlineOnly = offlineOnly, onOfflineOnlyChange = onOfflineOnlyChange, maxTierId = maxTierId, onMaxTierChange = onMaxTierChange, modifier = modifier)`.
- After the Phase 17 edit the shim compiles unmodified and the release AAR still contains both descriptors [VERIFIED: javap of scratch release AAR]:
  - v2.4.1 shim: `ApproachLadderCardKt#ApproachLadderCard(Ljava/util/List;Ljava/lang/Boolean;Lkotlin/jvm/functions/Function1;Ljava/lang/String;Lkotlin/jvm/functions/Function1;Landroidx/compose/ui/Modifier;Landroidx/compose/runtime/Composer;II)V`
  - current: `...Landroidx/compose/ui/Modifier;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Boolean;Lkotlin/jvm/functions/Function1;Ljava/lang/String;Ljava/lang/String;Landroidx/compose/runtime/Composer;III)V`
- **Router through the shim:** a v2.4.1-compiled caller gets `router = null`, `onRouterChange = null` (toggle not rendered, no `require` trip) and English router labels (not rendered while `router == null`). Named-arg delegation is why: omitted params in the delegating call take the current overload's defaults.

### 3. `$default` mask / bridge for appended composable params (the specific question asked)
- A `@Composable` function does NOT get a separate Kotlin-visible `$default` bridge method; the compiler appends `Composer, int $changed[, int $changed1], int $default` to the SAME method and decodes the default mask inside that method's prologue. Appending 4 value params adds bits 11-14 to the **current** overload's `$default` int and changes only the current overload's descriptor (it remains `…Composer;III`: `$changed`, `$changed1`, `$default`) [VERIFIED: javap above shows `Composer;III` before and after; the `III` count did not change at 15 params].
- The shim is a SEPARATE JVM method with its OWN `$default` int covering only its 6 value params (bits 3-5 = `0b111000` = 56 for the omitted `maxTierId, onMaxTierChange, modifier`, as the existing test passes). Its mask meaning is independent of the current overload's param count, so **the existing `$default`-mask render-through test needs no update** [VERIFIED: `VoiceBinaryCompatShimTest` 4/4 green with the router change applied; the test calls the shim by reflection with `…, Composer, 0, 0b111000` and renders the CURRENT overload with the 11 existing named params, which still compile because `router`… are defaulted].
- D-03 says to update the test only "if the current signature it calls with all params changes". The test names its 11 current-overload args and none is removed/renamed/reordered, so the test is **unchanged**. Recommend gating on `git diff --quiet <plan-start-SHA> -- …/VoiceBinaryCompatShimTest.kt` (the e2e SUMMARY already used this idiom against `489c410`).
- Stale comment note: the test's class KDoc says "Deliberately no overload-count assertions: Phase 17 (F2) may add further shims." Phase 17 adds none per D-03; do NOT edit the comment (it would break the byte-identical gate). Harmless.

### 4. No new shim for the untagged Phase-15 shape
The 11-param shape (labels, no router) never shipped in a tag (`git tag` shows up to `v2.4.1`; v2.5.0 not cut). Adding a hidden delegate for it is out of scope and would violate "one hidden shim per published signature". The released `v2.4.1` binary descriptor is the only one that needs preservation, and it is preserved by the existing shim.

### 5. Data classes: no K member, no touch
Phase 17 changes no `model/` file. The ten v2.4.1 synthetics restored by quick 261005-e2e stay intact. Gate: `git diff --quiet <plan-start-SHA> -- src/main/java/io/github/ygaray/yahirandroidtaste/model src/test/java/io/github/ygaray/yahirandroidtaste/model` (empty diff). The javap diff `missing=0` also covers them.

### 6. Closing binary gate — where it goes and how to run it
- `tools/verify-binary-abi.sh` does **NOT** exist yet [VERIFIED: `ls tools` this session lists only `classify-hub-change.sh`, `hooks`, `README-api-guard.md`, `test`, `verify-additive-diff.sh`, `verify-additive-surface.sh`, `verify-api-additive.sh`; `git ls-files | grep verify-binary` empty]. It is F3 at the v2.5.0 cut. Phase 17 must run the **inline javap recipe** from the quick 261005-e2e SUMMARY (§ Binary ABI gate).
- Place it as the LAST task of the phase's plan (closing gate, verify-only, no commit), after the full suite is green and `assembleRelease` has produced `build/outputs/aar/yahirandroidtaste-release.aar`. Place a cheaper per-task smoke (`assembleRelease` + the single-line descriptor grep below) after the task that changes the signature.
- Baseline AAR is the cached JitPack v2.4.1 artifact [VERIFIED: present at `~/.gradle/caches/modules-2/files-2.1/com.github.Ygaray/yahirandroidtaste/v2.4.1/e574584823b9cd55c67b50baabfc4875b9203fee/yahirandroidtaste-v2.4.1.aar`]. Resolve with a glob, not a hard-coded hash. If the cache were ever cold, fetch the AAR from JitPack (`https://jitpack.io/com/github/Ygaray/yahirandroidtaste/v2.4.1/yahirandroidtaste-v2.4.1.aar`) [ASSUMED: URL pattern from the standard JitPack layout; not exercised].

Gate script (cleaned version of the e2e recipe; filters `ComposableSingletons$*`, excludes `_Factory`/`_MembersInjector`; the `-ge 2000` floors guard against a silently-empty javap):
```bash
cd /home/yahir/Projects/Reusable/android/yahirandroidtaste
./gradlew assembleRelease
B=$(ls ~/.gradle/caches/modules-2/files-2.1/com.github.Ygaray/yahirandroidtaste/v2.4.1/*/yahirandroidtaste-v2.4.1.aar)
W=$(mktemp -d)
unzip -q -o "$B" classes.jar -d $W/base
unzip -q -o build/outputs/aar/yahirandroidtaste-release.aar classes.jar -d $W/head
for v in base head; do
  javap -public -s -cp $W/$v/classes.jar $(unzip -Z1 $W/$v/classes.jar | grep '\.class$' | grep -v '_Factory\|_MembersInjector' | sed 's/\.class$//; s#/#.#g') 2>/dev/null \
  | awk '/ (class|interface) [^ ]+.*\{$/ { match($0, /(class|interface) [^ <{]+/); s=substr($0, RSTART, RLENGTH); sub(/^(class|interface) /, "", s); cls=s }
         /descriptor:/ { n=prev; sub(/\(.*/, "", n); k=split(n, a, " "); print cls "#" a[k] $2 } { prev=$0 }' | sort -u > $W/$v.txt
done
test "$(wc -l < $W/base.txt)" -ge 2000 && test "$(wc -l < $W/head.txt)" -ge 2000
comm -23 $W/base.txt $W/head.txt | grep -v 'ComposableSingletons\$' > $W/missing.txt
echo "base=$(wc -l < $W/base.txt) head=$(wc -l < $W/head.txt) missing=$(wc -l < $W/missing.txt)"; cat $W/missing.txt
test ! -s $W/missing.txt
```
Expected output on a correct Phase 17 [VERIFIED in scratch: `base=2526 head=2584 missing=0`]. Also assert the one shim line explicitly: `grep -F 'ApproachLadderCardKt#ApproachLadderCard(Ljava/util/List;Ljava/lang/Boolean;Lkotlin/jvm/functions/Function1;Ljava/lang/String;Lkotlin/jvm/functions/Function1;Landroidx/compose/ui/Modifier;Landroidx/compose/runtime/Composer;II)V' $W/head.txt`.

### 7. What this gate does and doesn't prove
`apiCheck` and `api.txt` cannot see HIDDEN shims or synthetics (Metalava omits them); only the javap diff sees them. A green `apiCheck` with a deleted shim would still pass, so the javap gate (and the unmodified shim test) is the only guard against accidentally breaking F1 during Phase 17.

## Pre-commit hook behaviour (measured, replaces the earlier lane-prediction)

- Chain: global `core.hooksPath=/home/yahir/.config/git/hooks-chain` dispatches `pre-commit` to `git-secret-scan` then to the repo hook `tools/hooks/pre-commit` [VERIFIED: read `hooks-chain/pre-commit`]. The repo hook diffs against `BASE="$(git describe --tags --abbrev=0 --match 'v*')"` = `v2.4.1` [VERIFIED: ran `git describe`], sets `API_FILE` to the ABSOLUTE `$ROOT/api.txt`, runs `classify-hub-change.sh --baseline v2.4.1`, and blocks lane 2/3 unless `HUB_LANE_OVERRIDE` equals the printed lane exactly.
- Because `API_FILE` is absolute, `verify-api-additive.sh` evaluates `git cat-file -e v2.4.1:/abs/api.txt`, fails, prints `API-ADDITIVE SKIP`, exits 0 — so the API half is **degraded** and the lane is decided by `verify-additive-diff.sh` (staged-vs-HEAD diff of `src/main`, trailing `,`/`+` normalized). [VERIFIED: `API_FILE=$PWD/api.txt classify-hub-change.sh --baseline v2.4.1` => `LANE 1`, rc 0; with relative `API_FILE=api.txt` => `LANE 3`, rc 3 — the real api.txt already differs from v2.4.1 since Phase 15, which is the declared known false positive.]
- Measured lanes in the scratch clone with the Phase 17 change staged:
  | Staged change | Lane |
  |---|---|
  | `ApproachLadderCard.kt` signature/require/render appends (+ `api.txt`) | **LANE 1** (`DS-05 PASS: 1 removed line(s), all accounted for by an identical added line`) |
  | + fixture additive edit (new `initialRouter` param, trailing commas, new args) | **LANE 1** |
  | + rewrite of `render = { ApproachLadderCardFixture(initialOfflineOnly = true) }` | **LANE 2** (`DS-05 FAIL: line rewritten/removed in a pre-existing file: render = { ApproachLadderCardFixture(initialOfflineOnly = true) }`) |
- Test files and `api.txt` are outside the guarded `src/main` path set, so they never affect the lane.
- **Plan instruction:** keep every `src/main` edit append-only (no reworded KDoc line, no rewritten fixture cell) and expect `LANE 1` with no override. If the planner/executor deliberately rewrites an existing line, the hook will print `LANE 2`; re-run the identical commit with `HUB_LANE_OVERRIDE=2` (the value the hook prints, never a pre-guessed one). Never `--no-verify`. Note `tools/test/test-precommit-hook.sh` and `tools/test/run-all.sh` exist if the hook itself is under question.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Two-state segmented control | A Row of Buttons / Switch | `SegmentedOptionSelector` | Provides `"$label, selected|not selected"` semantics, contrast, exactly-2 `require` |
| api.txt maintenance | Hand-editing the signature line | `./gradlew apiDump` | Metalava formatting must match byte-for-byte |
| Binary-compat proof | Custom scripts / eyeballing | The javap descriptor-diff recipe above (soon `tools/verify-binary-abi.sh`) | Only tool that sees hidden shims and synthetics |
| Additive proof (source) | Custom diff | `./gradlew apiCheck` (+ released-baseline swap) | Established |
| Pairing guard | A silent `if` that hides the toggle | `require()` clone | Loud failure is the repo convention (WR-01) |
| v2.4.1 linkage for the composable | A new shim | The existing hidden overload `07f66cf` | Already exact, tested; D-03 forbids another |

**Key insight:** every piece of production code already exists once in the file; the work is a faithful second copy plus not breaking the shim.

## Common Pitfalls

### Pitfall 1: Inserting params mid-signature
Positional callers re-bind or fail to compile. Append after `offlineOnlyLabel`. Warning sign: `VoiceI18nSourceCompatTest.v240PositionalComposableCallShapes_compileAgainstV25Signatures` stops compiling.

### Pitfall 2: Editing the shim, or "fixing" its delegation
Adding router args or changing the shim's params changes the v2.4.1 descriptor and silently breaks F1. Only `apiCheck` would stay green. Guard: javap gate + shim test byte-identical.

### Pitfall 3: Rewriting an existing line => LANE 2
Rewording the class KDoc sentence (lines 32-35), rewriting the "Pressed / Selected" fixture cell, or rewording the "every control hidden" `SectionLabel` each count as a pre-existing line rewrite [VERIFIED: the fixture-cell rewrite measured LANE 2]. Use appended lines instead, or accept an explicit `HUB_LANE_OVERRIDE=2` with the printed lane.

### Pitfall 4: api.txt not regenerated in the same commit
Stale `api.txt`; Phase 18 API-02 or the next `apiCheck`/hook flags drift. Sequence: `apiCheck` (before) -> `apiDump` -> `cmp` freshness -> `apiCheck` (after) -> commit source + tests + `api.txt` together.

### Pitfall 5: Copying Phase 16's "explorer unchanged" gate
Phase 17 edits `explorer/VoiceCommandFamilyScreen.kt` (D-02). Keep asserting `SegmentedOptionSelector.kt` and `feedback/` unchanged vs `v2.4.1`; do NOT include `explorer/`. Phase 18's closing gate must also allow it.

### Pitfall 6: Nothing composes the Explorer fixture today
`ComponentStatesMatrixTest`/`ComponentPlaygroundIntegrityTest` only check that `render`/`content` lambdas are non-null; `GalleryDemoInteractionTest` only covers `TagChipEditorContent` [VERIFIED: read lines 1-90]. D-02's fixture would ship unexercised. Add a registry-reached render test (idiom: `ComponentRegistry.entries.first { it.name == "ApproachLadderCard" }.states.first { it.label == "Default" }.render`).

### Pitfall 7: Duplicate tags in the gallery detail page
The detail page composes several fixture instances; `onNodeWithTag("approach_ladder_card_router_toggle")` is not unique there. Render ONE cell lambda per test, as `GalleryDemoInteractionTest` does.

### Pitfall 8: Router leaking into per-rung derivation
Router is emit-only policy chrome. Add a test asserting rung rendering is unchanged when router toggles (rung count, Capped/Needs-network labels identical).

### Pitfall 9: Accessibility state words stay English
`SegmentedOptionSelector` builds `"$label, selected|not selected"`; tests locate segments by e.g. `"Router off, selected"`, `"Router on, not selected"`. A localized label changes the prefix only. Already-declared residual (Phase 15); document in KDoc, do not change `SegmentedOptionSelector`.

### Pitfall 10: Baseline AAR resolution
The binary gate needs the cached v2.4.1 AAR. Use the glob; assert both line-count floors so an empty javap cannot yield a false `missing=0`.

## Code Examples

### Component tests to add to `ApproachLadderCardTest.kt`
Mirror the offline analogues, which exist at these test names [VERIFIED: grep of the file]: `toggling offline-only emits onOfflineOnlyChange with the new value` (`:76`), `offline-only toggle is not rendered when the prop is null` (`:95`), `a non-null onOfflineOnlyChange with a null offlineOnly throws -- the pairing invariant is enforced` (`:181`), `a non-null offlineOnly with a null onOfflineOnlyChange throws -- ...` (`:191`), `supplied toggle labels replace the Online and Offline only segments and still emit` (`:281`), `omitting the toggle labels keeps the English Online segment` (`:307`). Idioms: `composeTestRule.onNodeWithTag(...)`, `onNodeWithContentDescription("Offline only, not selected").performClick()`, `assertThrows(IllegalArgumentException::class.java) { composeTestRule.setContent { ... }; composeTestRule.waitForIdle() }`.

```kotlin
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

@Test fun `router toggle is not rendered when the pair is null`() {
    composeTestRule.setContent { ApproachLadderCard(ladder = ladder, router = null, onRouterChange = null) }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("approach_ladder_card_router_toggle").assertDoesNotExist()
    composeTestRule.onAllNodesWithContentDescription("Router", substring = true).assertCountEquals(0)
}

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
Also add: label override (`routerOnLabel = "Routeur activé"`, `routerOffLabel = "Routeur désactivé"` => `"Routeur activé, not selected"` clickable + emits; English `"Router off, selected"` count 0); English default pin (`router = true` => `"Router on, selected"`); coexistence/independence (all three pairs set: rung tap emits only `onMaxTierChange`, router tap emits only `onRouterChange`, both toggle tags exist); placement (router toggle `boundsInRoot.top` > offline toggle's, both below the last rung); router toggling leaves rung capped/needs-network labels unchanged (Pitfall 8).

**Optional, recommended:** a NEW sibling test (e.g. `ApproachLadderRouterBinaryCompatTest.kt`, reusing the `facadeMethod`-style reflective lookup) that invokes the v2.4.1 descriptor via the shim and asserts `approach_ladder_card_router_toggle` does NOT exist and no `Router…` content description is present — proves "router defaults to null through the shim". Keeping it in a new file preserves the byte-identical `VoiceBinaryCompatShimTest` gate. Planner discretion; it is belt-and-braces on top of the existing shim test.

### Explorer fixture (D-02) — append-only form (measured LANE 1)
Current fixture `VoiceCommandFamilyScreen.kt:311-326` [VERIFIED: Read]. Prescribed edits (all pure appends modulo trailing commas):
```kotlin
@Composable
private fun ApproachLadderCardFixture(
    initialOfflineOnly: Boolean = false,
    initialMaxTierId: String = fixtureLadder.first().id,
    initialRouter: Boolean = false
) {
    var router by remember { mutableStateOf(initialRouter) }
    var offlineOnly by remember { mutableStateOf(initialOfflineOnly) }
    var maxTierId by remember { mutableStateOf(initialMaxTierId) }
    ApproachLadderCard(
        ladder = fixtureLadder,
        offlineOnly = offlineOnly,
        onOfflineOnlyChange = { offlineOnly = it },
        maxTierId = maxTierId,
        onMaxTierChange = { maxTierId = it },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        router = router,
        onRouterChange = { router = it }
    )
}
```
Every fixture cell then shows a live router toggle (router OFF initially). To show the ON polarity WITHOUT rewriting the existing "Pressed / Selected" cell (which would be LANE 2), append a new `SectionLabel("ApproachLadderCard — router on")` + `ApproachLadderCardFixture(initialRouter = true)` at the END of `ApproachLadderCardVariants()` (`:329-353`). Leave the "every control hidden" cell (`:334-338`, passes no router => doubles as the hidden-router demo) and the "combined subdued labels" cell untouched. If the owner prefers the ON polarity in the "Pressed / Selected" cell, that single-line rewrite is allowed but costs `HUB_LANE_OVERRIDE=2`. No registry change.

### Source-compat pin (optional)
`VoiceI18nSourceCompatTest` already pins the 6-positional v2.4.0 call (read lines 61-69). Any additional pin (all 11 pre-Phase-17 args positionally through `offlineOnlyLabel`) should go in a NEW test file to keep the existing file byte-identical.

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Additive = `apiCheck` green | Additive = `apiCheck` (source) + javap descriptor diff (binary) | D-03 / quick 261005-dmc, -e2e, -eyu (2026-10-05) | Phase 17 closing gate gains the binary step |
| In-place signature change, rebuild consumers | Hidden `@Deprecated(HIDDEN)` overload per tagged shape | `07f66cf` | Shim must survive Phase 17 |
| Hardcoded English toggle segments | Caller-overridable `String` params appended last | Phase 15 (`31595d4`) | Router follows the pattern |
| Unpaired optional props failing silently | `require()` pairing | Phase 10 WR-01 | Router gets the third `require()` |

**Deprecated/outdated:** the earlier research's lane prediction ("2 or 3") and its "rewrite the Pressed/Selected cell" fixture advice (causes lane 2).

## Runtime State Inventory

Not applicable — additive feature phase, not a rename/refactor/migration. No stored data, live-service config, OS-registered state, secrets/env vars, or build artifacts are renamed. None — verified: `router` is a new parameter name; no existing key/string is renamed.

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | The `router` testTag name is unique in the repo (earlier research's grep found "router" only as an icon name in `IconPickerGrid.kt`; not re-grepped this refresh) | Pattern 1 | Duplicate tag; trivial to rename. Re-grep at execution |
| A2 | JitPack URL pattern for the v2.4.1 AAR fallback (`https://jitpack.io/com/github/Ygaray/yahirandroidtaste/v2.4.1/yahirandroidtaste-v2.4.1.aar`) | D-03 §6 | Only matters if the Gradle cache is cold; not exercised |
| A3 | Compose compiler behaviour: no Kotlin-visible `$default` bridge for `@Composable`; mask decoded in the same method (the empirical part — descriptors, `III`, shim test green — is verified; the "why" is from compiler knowledge) | D-03 §3 | None for the plan; the verified javap/test results stand on their own |
| A4 | Two stacked segmented toggles may look/announce cramped; owner may want a visual look | Pitfalls / Gate-2 | Taste-only, surfaces at Gate-2 |
| A5 | The hook chain behaves in a real `git commit` as the standalone classifier run did (secret scanner `~/.local/bin/git-secret-scan` not exercised) | Pre-commit hook | A secret-scan failure would show loudly; nothing here adds secrets |

## Open Questions

1. **Which lane will each real commit print?**
   - Known: lane 1 for an append-only `src/main` diff (measured), lane 2 on any rewritten existing line.
   - Unclear: the secret scanner leg and exact staged set of the executor's atomic commits.
   - Recommendation: planner writes "if the hook prints LANE 2/3, re-run the identical commit with `HUB_LANE_OVERRIDE=<printed lane>`; never `--no-verify`".

2. **Router ON polarity in the gallery.**
   - Recommendation: append a new "router on" Variants cell (lane 1). Rewriting the Pressed/Selected cell is the lane-2 alternative.

3. **Where F3's `tools/verify-binary-abi.sh` lands.**
   - It does not exist at HEAD; Phase 17 runs the inline recipe. If F3 lands before Phase 17 executes, prefer the script and keep the inline recipe as fallback.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| JDK (`java`, `javap`) | Gradle, binary gate | ✓ | OpenJDK 17.0.19 [VERIFIED: prior `java -version`; `javap` ran this session] | — |
| Gradle wrapper + offline caches | `apiCheck`, `apiDump`, `testDebugUnitTest`, `detekt`, `assembleRelease` | ✓ | `--offline -Dorg.gradle.vfs.watch=false` worked in the scratch clone (≈30-35 s warm) | drop `--offline`, or keep `vfs.watch=false` if "Already watching path" |
| Android SDK | AGP/Robolectric | ✓ | `local.properties` `sdk.dir=/home/yahir/Android/Sdk` | — |
| Cached v2.4.1 JitPack AAR | Binary gate baseline | ✓ | `…/v2.4.1/e574584823b9cd55c67b50baabfc4875b9203fee/yahirandroidtaste-v2.4.1.aar` | JitPack download (A2) |
| `unzip`, `comm`, `awk` | Binary gate | ✓ | ran this session | — |
| Tester device / adb | Gate-1 self-UAT (post-execute verify workflow, not a planned task) | not probed | — | Explorer fixture is the built-in demo surface; throwaway harness is the Phase 15/16 fallback |

**Missing dependencies with no fallback:** none.

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit4 + Robolectric (`@Config(sdk = [35])`) + Compose UI test; Metalava `apiCheck`; detekt; javap diff |
| Config file | `build.gradle.kts` (existing); no install |
| Quick run command | `./gradlew testDebugUnitTest --tests '*ApproachLadderCardTest' --tests '*VoiceBinaryCompatShimTest' --tests '*VoiceI18nSourceCompatTest' --tests '*GalleryDemoInteractionTest'` |
| Full suite command | `./gradlew testDebugUnitTest && ./gradlew apiCheck && ./gradlew detekt` |

Baselines [VERIFIED: scratch run + SUMMARYs]: full suite at HEAD = 734 tests, 0 failures, 22 skipped (e2e SUMMARY); `ApproachLadderCardTest` = 21 tests, `VoiceBinaryCompatShimTest` = 4, `VoiceI18nSourceCompatTest` = 4, `DataClassBinaryCompatShimTest` = 6, `ComponentRegistryDriftGuardTest` = 1 — all 0 failures with the Phase 17 production change applied in the scratch clone.

### Phase Requirements -> Test Map
| Req / SC | Behavior | Test Type | Automated Command | File Exists? |
|----------|----------|-----------|-------------------|-------------|
| VAPPR-04 / SC1 | Both non-null renders toggle reflecting state (`Router off, selected` for false; `Router on, selected` for true) | Compose UI | `./gradlew testDebugUnitTest --tests '*ApproachLadderCardTest'` | file ✅; new cases ❌ Wave 0 |
| VAPPR-04 / SC2 | Tapping the other segment invokes `onRouterChange(new)` | Compose UI | same | new cases |
| VAPPR-04 / SC3 | Both null -> no router node and no `Router…` description; existing tests unchanged/green; v2.4 positional shape compiles | Compose UI + compile-only | `… --tests '*ApproachLadderCardTest' --tests '*VoiceI18nSourceCompatTest'` | existing + new |
| VAPPR-04 / SC4 | Exactly one of the pair -> `IllegalArgumentException` (both directions) | Compose UI (`assertThrows`) | same | new cases |
| VAPPR-04 / SC5 | Row tap still emits `onMaxTierChange`; router tap emits only `onRouterChange`; rung semantics unaffected | Compose UI | same | new cases |
| D-01 | `routerOnLabel`/`routerOffLabel` replace segment text; English defaults; still emits | Compose UI | same | new cases |
| D-02 | Router toggle below offline toggle; gallery fixture renders it and toggles live | Compose UI (registry-reached render) | `… --tests '*GalleryDemoInteractionTest'` (or sibling) | ❌ Wave 0 |
| D-03 shim intact | v2.4.1 descriptor present + renders through; router defaults null via shim | Reflection + Compose | `… --tests '*VoiceBinaryCompatShimTest'` (+ optional new sibling) | ✅ existing (unchanged) |
| D-03 binary gate | Zero missing public descriptors vs v2.4.1 AAR | javap diff | gate script in D-03 §6 | recipe exists; script ❌ (F3) |
| API source gate | `apiCheck` green vs committed and vs `v2.4.1`; delta = exactly one line | gradle + git | Closing gate | tooling exists |

### Sampling Rate
- **Per task commit:** quick run command + `./gradlew detekt` + (the api tail below on the signature-changing task).
- **After the signature-changing task:** `./gradlew assembleRelease` + the single-line v2.4.1 descriptor `grep -F` smoke from D-03 §6.
- **Per wave merge / phase gate:** full suite + closing gate.
- **Api tail (signature-changing task only):** `./gradlew apiCheck && F=$(mktemp) && cp api.txt "$F" && ./gradlew apiDump && cmp "$F" api.txt` — the first `cmp` is expected to differ on that task (commit the regenerated file); then `./gradlew apiCheck` again. On later tasks `cmp` must be identical.

### Closing gate (verify-only, no commit; record plan-start SHA `d83e467` or the SHA at plan time)
1. Full suite green incl. `ComponentRegistryDriftGuardTest`, `DomainVocabularyDriftGuardTest`, `GeneratedSymbolDriftGuardTest`, `GalleryDemoInteractionTest`, `VoiceBinaryCompatShimTest`, `DataClassBinaryCompatShimTest`; detekt 0 smells; `config/detekt-baseline.xml` untouched.
2. Released-baseline source gate: `( git show v2.4.1:api.txt > api.txt && ./gradlew apiCheck; rc=$?; git checkout -- api.txt; exit $rc )`, then `git status --porcelain -- api.txt` empty [VERIFIED: equivalent run in scratch, rc 0].
3. Removed-line allowlist vs `v2.4.1` unchanged at the ten symbols: `D=$(git diff v2.4.1 -- api.txt) && ! printf '%s\n' "$D" | grep -E '^-[^-]' | grep -vE 'ProviderKeyCard|ModelSelectCard|ClarificationBar|ApproachLadderCard|HandledByUiModel|ProposedItemUiModel|UndoRefusedUiModel|UndoRowUiModel|FailureActionUiModel|VoiceOutcomeUiState.Failure' | grep -q .` [VERIFIED: `allowlist-ok` in scratch].
4. Phase 17 api delta: `git diff <plan-start-SHA> HEAD -- api.txt` has exactly ONE removed and ONE added line, both containing `ApproachLadderCard(`; the added line contains `optional Boolean? router` and `optional String routerOffLabel` [VERIFIED: scratch, 2 changed lines].
5. **Binary gate (NEW, D-03):** run the D-03 §6 script: `missing=0`, floors met, v2.4.1 shim line present.
6. **Shim/K intact:** `git diff --quiet <plan-start-SHA> -- src/test/.../component/VoiceBinaryCompatShimTest.kt src/test/.../component/VoiceI18nSourceCompatTest.kt src/test/.../model src/main/.../model`; and `git diff <plan-start-SHA> -- src/main/.../component/ApproachLadderCard.kt` shows no hunk inside the shim block (`@Deprecated(... HIDDEN)` count in the file remains 1).
7. `git diff --quiet v2.4.1 -- src/main/java/io/github/ygaray/yahirandroidtaste/component/SegmentedOptionSelector.kt src/main/java/io/github/ygaray/yahirandroidtaste/feedback` (do NOT include `explorer/`).
8. Import/Hilt checks vs `v2.4.1` (`^\+import ` outside androidx/kotlin/android/library namespace => none; no `@HiltAndroidApp|@AndroidEntryPoint`).
9. No tag cut (`git tag --points-at HEAD` empty; `git.create_tag` is `false` [VERIFIED: config.json]); `src`/`api.txt` tree clean.

### Wave 0 Gaps
- [ ] Router cases in `ApproachLadderCardTest.kt` (SC1-SC5, D-01 overrides, placement, rung-unchanged).
- [ ] Registry-reached gallery render test for the `ApproachLadderCard` fixture (D-02 has no coverage otherwise).
- [ ] (Optional) new-file shim "router defaults null" test and positional pin.
- [ ] `.planning/uat-pending/17-approachladdercard-router-on-off-toggle.md` Gate-2 registration (Phase 15/16 convention; `ls .planning/uat-pending` shows 15 and 16 files, none for 17 yet).
- Framework install: none.

### Manual-only (Gate-2, owner; non-blocking)
Visual: two stacked segmented toggles at the card bottom — spacing, card height growth, light/dark contrast (UX is first-class for this owner). Gate-1 self-UAT on the tester rig is driven by the post-execute verify workflow, not a planned task; the Explorer fixture is its demo surface.

## Security Domain

`security_enforcement` is enabled (ASVS level 1, block on high) [VERIFIED: .planning/config.json in earlier research]. This phase adds a presentational boolean toggle with no network, storage, auth, or crypto surface.

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2 Authentication | no | — |
| V3 Session Management | no | — |
| V4 Access Control | no | — (consumer decides what the toggle does) |
| V5 Input Validation | minimal | Type-level `Boolean?`; pairing enforced by `require()`; labels rendered verbatim as plain `Text`/content-description, never parsed |
| V6 Cryptography | no | — |

### Known Threat Patterns for this stack

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| Guard bypass hiding a real API/ABI break (`HUB_LANE_OVERRIDE`, `--no-verify`) | Tampering | Released-baseline `apiCheck` + removed-line allowlist + exactly-one-line delta + javap binary gate; never `--no-verify` |
| One-way-dependency erosion (importing an engine/router-policy type) | Tampering / Elevation (supply chain) | Closing gate item 8; API takes `Boolean?` + `(Boolean) -> Unit` only |
| Silent loss of the v2.4.1 shim (a binary break invisible to `apiCheck`) | Tampering (integrity) | Unmodified shim test + javap gate |
| Info disclosure via exception message | Information disclosure | `require()` message interpolates only a Boolean and "null"/"non-null" |
| Half-configured toggle | Repudiation / UX integrity | `require()` fails loudly |

## Sources

### Primary (HIGH confidence — read/run this session)
- `/home/yahir/Projects/Reusable/android/yahirandroidtaste/src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt` (full file, 278 lines, shim at `:156-175`)
- `/home/yahir/Projects/Reusable/android/yahirandroidtaste/src/test/java/io/github/ygaray/yahirandroidtaste/component/VoiceBinaryCompatShimTest.kt` (header + ApproachLadderCard case `:30-135`)
- `…/component/ApproachLadderCardTest.kt` (function list + offline tests), `…/component/VoiceI18nSourceCompatTest.kt:50-90`, `…/explorer/GalleryDemoInteractionTest.kt:1-90`
- `…/explorer/VoiceCommandFamilyScreen.kt:115-144, 284-353`
- `/home/yahir/Projects/Reusable/android/yahirandroidtaste/api.txt:57-59`; `tools/hooks/pre-commit`, `tools/classify-hub-change.sh`, `tools/verify-api-additive.sh`, `tools/verify-additive-diff.sh`; `/home/yahir/.config/git/hooks-chain/pre-commit`
- `.planning/quick/261005-dmc-*/261005-dmc-SUMMARY.md`, `.planning/quick/261005-e2e-*/261005-e2e-SUMMARY.md`, `.planning/quick/261005-eyu-*/261005-eyu-SUMMARY.md`; `API.md` § "The binary-compatibility rule"
- `.planning/phases/17-…/17-CONTEXT.md`, `.planning/ROADMAP.md` § Phase 17, `.planning/REQUIREMENTS.md` VAPPR-04
- Scratch clone (`git clone --local` into the session scratchpad; real repo untouched): `assembleRelease` + `apiCheck` BUILD SUCCESSFUL; `apiDump` diff = 2 changed lines; `apiCheck` vs `v2.4.1` api.txt rc 0; removed-line allowlist ok; javap `base=2526 head=2584 missing=0`; tests: `ApproachLadderCardTest` 21/0, `VoiceBinaryCompatShimTest` 4/0, `VoiceI18nSourceCompatTest` 4/0, `DataClassBinaryCompatShimTest` 6/0, `ComponentRegistryDriftGuardTest` 1/0, detekt green; hook classifier lanes measured as tabulated.

### Secondary / Tertiary
- None; no external library or web source was needed.

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH — no new dependencies; files read.
- Architecture: HIGH — change reproduced end-to-end in scratch, including the binary gate.
- D-03 constraints: HIGH — descriptors, mask behaviour and gate output measured, not inferred.
- Pitfalls: HIGH for 1-8, 10; MEDIUM for real-commit hook behaviour (secret-scanner leg not exercised, A5).

**Research date:** 2026-10-05
**Valid until:** 2026-11-04 (stable; invalidated if `ApproachLadderCard.kt`, the shim, or `tools/` change before Phase 17 executes — refresh by re-reading `ApproachLadderCard.kt:79-175` and `api.txt:58`)
