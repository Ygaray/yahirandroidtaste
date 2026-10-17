# Phase 25: Feedback target model & stack state - Research

**Researched:** 2026-10-17
**Domain:** Additive Kotlin/Compose API work in a source-compatible design library (data-class widening, a coroutine-based state holder, a tracer composable, and four drift guards)
**Confidence:** HIGH for the code map, the guards, the gates and the coroutine semantics (all read or run this session). MEDIUM for the state-machine skeleton (my recommendation; Phase 25 tests prove it).

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

Locked milestone decisions D-01..D-17 live in `.planning/research/v2.8-BRIEF.md` and apply here unchanged. The phase decisions below are numbered P25-D-xx in prose but use the parser shape.

### event-shape
- **D-01 [event-shape]:** add `supportingText: String? = null` (last param) to Notify and WithUndo in Phase 25; no tone field; dot = primary with a target, onSurfaceVariant without

### target-validation
- **D-02 [target-validation]:** KDoc rule plus render-time fallback to `message`; no throwing init (hub types don't throw on bad input)

### entry-target
- **D-03 [entry-target]:** `val target: FeedbackTarget? = null` appended after `consumedGuard`, plus `openableTarget` non-null only while Available; new `store.append` overload with a required `target`

### result-type
- **D-04 [result-type]:** enum (matches SnackbarResult / UndoStatus; sealed buys nothing for exhaustive whens)

### cancellation
- **D-05 [cancellation]:** remove + rethrow; tracked entry stays Available in the store

### timer-owner
- **D-06 [timer-owner]:** inside `show()` (virtual-time testable); starts at enqueue; Indefinite never times out; the host also pushes `LocalAccessibilityManager` scaling into the state so a manually constructed state still scales

### programmatic-dismiss
- **D-07 [programmatic-dismiss]:** add `dismissAll()`; additive and cheap

### spent-card
- **D-08 [spent-card]:** keep the card, hide Undo + chevron; a losing Undo tap still resolves Undone (store CAS no-op)

### undo-cancel
- **D-09 [undo-cancel]:** `withContext(NonCancellable)` around the store undo on the new path only

### Claude's Discretion
- Internal naming, file split and test layout, within the hub conventions (CLAUDE.md, docs/STYLE.md).

### Deferred Ideas (OUT OF SCOPE)
999.4 voice-surface polish -> v2.8.1; combined "Undo & open"; share/MCP deep links; SB sheet-swap blink (app-side).

### Milestone-locked decisions that bind this phase (v2.8-BRIEF.md, verbatim excerpts)
- **D-04** "Timeouts unchanged: Short 4 s, Long 10 s, through `AccessibilityManager.calculateRecommendedTimeoutMillis` (containsControls when Undo or a target)."
- **D-06** "Stack cap: a 4th arrival dismisses the oldest early (result Dismissed); WithUndo entries stay in the Undo Center."
- **D-08** "Target model: `FeedbackTarget(key: String, openLabel: String)`; opaque consumer-encoded key, localized TalkBack click label. Not a lambda (singleton `UndoHistoryStore` must not hold a NavController)."
- **D-09** "Additive API: `target: FeedbackTarget? = null` appended to `FeedbackEvent.Notify`, `FeedbackEvent.WithUndo`, ... `UndoHistoryEntry` (internal ctor). New `FeedbackStackState` / `rememberFeedbackStackState(maxVisible = 3)` with `suspend fun show(event): FeedbackResult` (Opened | Undone | Dismissed | TimedOut); `FeedbackStackHost(state, onOpenTarget, modifier, undoLabel = "Undo", dismissLabel = "Dismiss")`; `FeedbackStackState.emitTrackedWithUndo(store, event)`. ... Existing `SnackbarHostState.emitTrackedWithUndo` + M3 hosts keep working unchanged."
- **D-13** "`show()` DismissScreen: rendered like `Notify` (message only, no target, Short); the 'dismiss/navigate back' stays the consumer dispatcher's job, as today. Result follows the normal rules (TimedOut / Dismissed)." (This closes the ROADMAP open question.)
- **D-14** "snackbars and Undo Center rows derive [openability] in the library from the store entry's status (Undone/Failed -> not openable)."
- **D-17** "Phase 25 lands `FeedbackStackHost` with basic newest-first rendering so the registry drift guard holds; Phase 26 owns the deck look, gestures, timers, a11y."
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| FTGT-01 | `FeedbackTarget(key, openLabel)` on `Notify` / `WithUndo` via a defaulted `target`; existing call sites compile unchanged | Code map 1-2, order rationale (A1), V271 canary shapes incl. real SB/CT call sites, api.txt removed-line audit (5 lines) |
| FTGT-02 | A tracked `WithUndo` copies its target into `UndoHistoryEntry.target`; an Undone entry exposes no openable target | Entry/store/`UndoTracking.kt` map, `openableTarget` design, store overload resolution proof, lane-2 note for `UndoTracking.kt:25` |
| FSTK-01 | `FeedbackStackState` / `rememberFeedbackStackState(maxVisible = 3)` / `FeedbackStackHost(...)`; `show()` returns exactly one of four results | State skeleton (CAS deferred, cap, timer, cancellation), tracer host, plain-JVM test recipe (spike-verified), guard updates |
| FSTK-07 | `FeedbackStackState.emitTrackedWithUndo(store, event)` appends before showing, undoes only through the store guard; labels caller-localizable; M3 path keeps working | Tracked-path skeleton with `NonCancellable`, spent watcher, race tests, M3-path characterization test (none exists today) |
</phase_requirements>

## Summary

Phase 25 is an additive widening of `feedback/` plus one new state holder and a tracer host. The real work is not the Kotlin (about 400 lines) but the four things around it that fail the build if forgotten: `ComponentRegistry` (register `FeedbackStackHost`, allowlist `rememberFeedbackStackState`), `DomainVocabularyDriftGuardTest` (`"Feedback"` head token), the docs counts that `CatalogDocsDriftGuardTest` compares (66/5/71 become 67/6/73), and `api.txt`. All of these were read this session and the exact lines are below.

Three facts were verified by running things, and they change how the plan should be written. (1) `apiCheck` goes RED the moment a data-class parameter is appended (it reports "Removed constructor ... Notify(String,SnackbarDuration)" and "Removed method ... copy"), so `apiDump` must run in the same invocation before `apiCheck`, and the real additive proof is `bash tools/verify-api-source-additive.sh v2.7.1`, not `apiCheck`. (2) `mutableStateListOf`, `SnackbarHostState` and virtual-time `delay` all work in plain-JVM `runTest` (a throwaway spike test passed 3/3 and was deleted), so SC3 "the state needs no composition" is achievable with no Robolectric. (3) `--offline` does not work on this host (the Metalava plugin jar is not cached); the working command is `./gradlew ... --no-daemon`.

**Primary recommendation:** Build in four sequential slices (types + canary; entry/store/M3 path; state + JVM tests; tracked stack path + tracer host + guards/docs/api.txt). Use one `CompletableDeferred` per item as the exactly-once guard (its `complete()` return value is the guard), remove an item from `items` inside the same lock as the CAS, run the timer inside `show()` with plain `delay`, and call `store.attemptUndo` inside `withContext(NonCancellable)` only on the new path.

## Project Constraints (from CLAUDE.md)

- Library imports no host code, holds no secrets, no domain assumptions; library -> (Android SDK, AndroidX/Compose, Hilt, Coil, navigation-compose, reorderable, osmdroid) only. `LibraryInvariantsGuardTest` enforces import roots `androidx. android. kotlin. kotlinx. java. javax. dagger. coil3. org.osmdroid. sh.calvin.reorderable. io.github.ygaray.yahirandroidtaste.` [VERIFIED: LibraryInvariantsGuardTest.kt:41-52].
- No `@HiltAndroidApp` / `@AndroidEntryPoint`; the new state is not injectable (plain class).
- `ComponentRegistry` is the single source of truth: every public top-level `@Composable` in `component/ feedback/ modifier/ theme/` is registered XOR allowlisted.
- Source-compatible, not binary-compatible: append params with defaults, trailing lambdas last, never append after a trailing lambda, no new `@Deprecated(HIDDEN)` overloads, no hand-written `copy$default` synthetics. Metalava `apiCheck` per commit; `verify-binary-abi.sh` informational only.
- Detekt zero baseline; do not regenerate a baseline.
- Shared host: never `./gradlew --stop`; exactly one Gradle invocation at a time; use `--no-daemon` or kill your own daemon by PID. Do not tag or push (`v2.8.0` is Yahir's).
- Working-tree debris (`.planning/graphs/*`, `state.json`, `config.json`, stage markers, `.gate/`, `uat-evidence` dirs, `.oc-audit/`, `graphify-out/`, `docs/superpowers/`) never goes into a commit.
- No project skills directory exists (`.claude/skills` absent).

## Architectural Responsibility Map

An Android library has no web tiers, so tiers here are the library's own layers.

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| `FeedbackTarget`, `FeedbackResult`, event fields | Library model (`feedback/`, pure Kotlin) | Consumer (builds the key) | Opaque value types; no lambda, no Context (D-08) |
| "Exactly one result", cap, timer, cancellation | Library state holder (`FeedbackStackState`) | — | Testable with no composition (SC3); host only reports gestures |
| Undo execution and first-consumer-wins | `UndoHistoryStore` (`@Singleton`, in-memory) | State's tracked path calls it | The store's `tryConsume` CAS is the one guard (SC4) |
| Openability after Undo | Library (`UndoHistoryEntry.openableTarget`, card `spent`) | Caller clears caller-built state (Phase 27) | D-14: library-owned state derives from status |
| Rendering + body tap + Undo tap | Library UI (`FeedbackStackHost`, tracer) | Consumer supplies `onOpenTarget` | Host never touches `undoAction` |
| key -> route, navigation | Consumer app | — | Library never sees a route or NavController |
| Accessibility timeout scaling | Compose UI layer (`LocalAccessibilityManager`) pushed into state | State applies it at timer start | Keeps the state free of Context |

## Standard Stack

No new dependencies. Everything below is already in the build. [VERIFIED: gradle/libs.versions.toml:1-18 and build.gradle.kts dependencies block]

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Kotlin / Compose BOM | `kotlin = "2.3.20"`, `composeBom = "2026.04.01"` | Language, `mutableStateListOf`, `@Stable`, `LocalAccessibilityManager` | Project toolchain |
| kotlinx-coroutines (via Compose/Hilt transitively) | main; test `coroutinesTest = "1.11.0"` | `CompletableDeferred`, `coroutineScope`, `withContext(NonCancellable)`, `runTest` | Native tools for exactly-once + cancellation |
| Material3 | 1.4.0 (resolved) | `SnackbarDuration`, `SnackbarHostState` (M3 path stays) | Already used by `UndoTracking.kt` |
| Robolectric / JUnit | `robolectric = "4.16.1"`, `junit = "4.13.2"` | Host tests (`createComposeRule`, `@Config(sdk = [35])`) | Existing convention |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| `CompletableDeferred` per item | `Mutex` + boolean | Boolean-beside-deferred is Pitfall 1 (double navigation). A `Mutex` around `show()` is exactly the M3 queue being replaced. |
| `delay`-only timer in Phase 25 | `TimeSource` + pause StateFlow now | Pause/resume is accepted in Phase 26 (FSTK-06); Phase 26 CONTEXT owns its semantics (reason set, 800 ms floor). Add the `TimeSource` seam there. |
| `sealed interface FeedbackResult` | enum | Decided: enum (D-04). A 5th value later is a source break either way. |

**Installation:** none. **Version verification:** versions above read from `gradle/libs.versions.toml` this session; `AccessibilityManager.calculateRecommendedTimeoutMillis(long, boolean, boolean, boolean)` and `LocalAccessibilityManager` confirmed by `javap` on the resolved `ui` jar [VERIFIED: javap androidx.compose.ui.platform.AccessibilityManager].

## Package Legitimacy Audit

No external packages are installed by this phase. Nothing to audit. Packages removed (SLOP): none. Packages flagged (SUS): none.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| JDK 17 | Gradle/Kotlin build | yes | OpenJDK 17.0.20.1 | — |
| Gradle wrapper | all gates | yes | 9.4.1 (`gradle-wrapper.properties`) | — |
| Network | Gradle plugin resolution | yes | `--offline` FAILS: "Could not download plugin-0.5.0.jar (me.tylerbwong.gradle.metalava:plugin:0.5.0): No cached version available for offline mode" | do not pass `--offline` |
| python3, bash | `tools/verify-api-source-additive.sh`, `tools/test/run-all.sh` | yes | — | — |
| TESTER device / adb | none in this phase (ROADMAP Phase 25 carries no "UI hint"; tracer host is not the deck UI) | not needed | — | Gate-1 device UAT belongs to Phases 26/28 |

Baselines run on this host at HEAD (`30ded40`) this session, all green: filtered guard tests (3 classes, 13 tests, 0 failures, 31 s), `apiCheck detekt` (29 s), `verify-api-source-additive.sh v2.7.1` (`API SOURCE-ADDITIVE OK ... added_members=0 signature_changes=0`), `tools/test/run-all.sh` (all PASS, 24 s).

## Code Map (current v2.7.1 source, file:line)

Source root `src/main/java/io/github/ygaray/yahirandroidtaste/`. All quotes verbatim.

### feedback/FeedbackEvent.kt [VERIFIED: read this session]
- `sealed interface FeedbackEvent {` line 19.
- `Notify` lines 27-30: `val message: String,` / `val duration: SnackbarDuration = SnackbarDuration.Short`
- `WithUndo` lines 44-49: `val message: String,` / `val undoAction: suspend () -> Unit,` / `val duration: SnackbarDuration = SnackbarDuration.Long,` / `val preview: UndoPreview? = null`
- `DismissScreen` lines 56-58: `val message: String` (untouched in this phase).
- Adding a field means adding a trailing comma to the last existing param line (lines 29 and 48). The lane guard normalizes a trailing `,` away (see Pitfall 2), so this stays lane 1.

### feedback/UndoHistoryEntry.kt [VERIFIED]
- Line 13: `enum class UndoStatus { Available, Undone, Failed }`
- Lines 47-55: `data class UndoHistoryEntry internal constructor(` ... `val preview: UndoPreview? = null,` / `val status: UndoStatus = UndoStatus.Available,` / `private val consumedGuard: AtomicBoolean = AtomicBoolean(false)` / `) {`
- Line 58 `fun tryConsume(): Boolean = consumedGuard.compareAndSet(false, true)`; line 65 `fun withStatus(newStatus: UndoStatus): UndoHistoryEntry = copy(status = newStatus)` (carries `target` automatically).
- Construction sites that must keep compiling (all use named args): `UndoHistoryStore.kt:66,99`, `ExplorerFakeData.kt:94,101,109`, `ReleaseDemos.kt:248`, tests `UndoCenterScreenDefaultParityTest.kt:120`, `UndoHistoryStoreTest.kt:288`.

### feedback/UndoHistoryStore.kt [VERIFIED]
- Line 25 `class UndoHistoryStore @Inject constructor() {`
- 3-arg append lines 65-77: `fun append(message: String, preview: UndoPreview? = null, undoAction: suspend () -> Unit): String {`
- Grouped append lines 93-109: `message: String,` / `preview: UndoPreview? = null,` / `groupId: String,` / `undoAction: suspend () -> Unit`
- `attemptUndo` lines 219-236 (`@Suppress("TooGenericExceptionCaught")` at 219; `suspend fun attemptUndo(id: String)` at 220): returns on missing entry, returns if `status != Available`, returns if `!entry.tryConsume()`, runs `entry.undoAction()`, writes `Undone`/`Failed`; rethrows `CancellationException`.
- `clearSpent` 250-261, `evictIfNeeded` 275-289 (50-cap).

### feedback/UndoTracking.kt [VERIFIED]
- Lines 21-37 `suspend fun SnackbarHostState.emitTrackedWithUndo(store: UndoHistoryStore, event: FeedbackEvent.WithUndo)`.
- Line 25 (the one line that must change): `val entryId = store.append(message = event.message, preview = event.preview, undoAction = event.undoAction)`
- Lines 26-30 `showSnackbar(message = event.message, actionLabel = "Undo", duration = event.duration)`; lines 34-36 `if (result == SnackbarResult.ActionPerformed) { store.attemptUndo(entryId) }`.
- No test exercises this function today (`grep emitTrackedWithUndo src/test` hits only `GeneratedSymbolDriftGuardTest`, which counts lines in `api.txt`). Phase 25 adds the first characterization test.

### explorer/ComponentRegistry.kt [VERIFIED]
- Lines 88-97: `val entries: List<Entry> = cardsFamilyEntries + chipsFamilyEntries + sheetsFamilyEntries + buttonsFabFamilyEntries + pickersFamilyEntries + feedbackFamilyEntries + emptyStateFamilyEntries + progressFamilyEntries + tactileFoundationFamilyEntries + voiceCommandFamilyEntries`
- Lines 104-127 `val INTENTIONALLY_UNREGISTERED: Map<String, String> = mapOf(`; last entry (`SheetHeaderMenu`) lines 123-126 ends `"exercised indirectly via every sheet entry's own header/menu/rename interaction."` then `)`.
- Lines 129-151 `init { ... require(...) }`: duplicate names, registered-XOR-allowlisted overlap, blank reason all throw.
- KDoc lines 18-19 hold the prose snapshot: `**66 registered, 5 intentionally unregistered = 71 total public composables**`. Not asserted by any test; editing it is a rewritten line (lane 2). Optional.
- `Entry(name, family, states = emptyList(), content = null, controls = emptyList(), preview = null, tier)` lines 66-74; `enum class Tier { PRIMITIVE, PATTERN }` line 44.

### explorer/FeedbackFamilyScreen.kt [VERIFIED]
- `internal val feedbackFamilyEntries: List<ComponentRegistry.Entry> = listOf(` line 46; entries `ConfirmationDialog` (47-72), `UndoCenterScreen` (73-100), `AttentionCue` (101-124); list closes `)` line 125. Append the new `FeedbackStackHost` entry after `AttentionCue` (add `,` after line 124's `)`).
- Screen `FeedbackFamilyScreen` lines 133-155 filters `ComponentRegistry.entries` by `ExplorerFamilies.FEEDBACK` (no edit needed; the new row appears automatically).
- Entry pattern to copy: 4 `StateCell`s labelled `"Default"`, `"Pressed / Selected"`, `"Disabled"`, `"Focused"` (only Default needs a `render`), `content = { ... }`, `tier = ComponentRegistry.Tier.PATTERN`. Demo fixture host box idiom: `Box(modifier = Modifier.fillMaxWidth().height(400.dp))` (lines 80, 186). `ExplorerFeedbackStub` is defined at `ExplorerIndexScreen.kt:86`.

### Guards (src/test/java/io/github/ygaray/yahirandroidtaste/explorer/)
- `DomainVocabularyDriftGuardTest.kt`: `HEAD_TOKEN_REGEX = Regex("[A-Z]+(?=[A-Z][a-z]|$)|[A-Z][a-z0-9]*")` line 272; `PRIMITIVE_NOUN_ALLOWLIST` lines 297-346, last element `"Stacked"` at line 345 (preceded by the comment block 343-344). The regex skips the lowercase `remember` prefix, so BOTH `FeedbackStackHost` and `rememberFeedbackStackState` have head token `Feedback`; `"Feedback"` is in neither list today, so both go RED until it is added to `PRIMITIVE_NOUN_ALLOWLIST` (add `,` after `"Stacked"` and a rationale comment).
- `ComponentRegistryDriftGuardTest.kt`: scans every top-level package except `explorer` for column-0 `@Composable` + public `fun`; `internal`/`private` are skipped.
- `CatalogDocsDriftGuardTest.kt`: counts tests lines 104-122 require, in `API.md`: `**$registered registered public composables**`, `**$unregistered intentionally-unregistered**`, `= **$total public composables total**`, `## Intentionally-unregistered sub-parts ($unregistered)`; in `README.md`: `**$registered registered public composables**`, `**$unregistered intentionally-unregistered**`, `$total public composables total`. Per-family table test lines 124-142 matches rows `^\|\s*\d+\.\s` and compares column 2 with the live per-family count and the sum with `registered`.
- Tag scan (lines 188-205) only inspects literals starting with `outcome_sheet_`, `receipt_row_`, `provider_key_` or the five `singleTags`. Test tags such as `feedback_stack_item` / `feedback_stack_undo` are therefore free of any API.md obligation.
- `ComponentStatesMatrixTest`: every entry needs exactly the four canonical labels in order and non-null `content`. `ComponentPlaygroundIntegrityTest`: non-empty `controls` requires a non-null `preview` (the tracer entry should declare no controls).
- `LibraryInvariantsGuardTest`: import-root allowlist (above), one manifest `activity`, no Hilt host annotations, no consumer identifiers in code.

### Docs locations to edit [VERIFIED: grep + read]
- `API.md:23` `| 6. Feedback | 3 | Confirmation dialog, the Undo Center (with group rows since v2.6.0), and the attention-cue glyph |` -> count 4 and mention the stacked host.
- `API.md:29-30` `**66 registered public composables** across the ten families, plus **5 intentionally-unregistered**` / `structural sub-parts (see the end of this doc) = **71 public composables total**.` -> 67 / 6 / 73.
- `API.md:127` `## 6. Feedback` table: add a `FeedbackStackHost` row (signature per D-09).
- `API.md:424` `## Intentionally-unregistered sub-parts (5)` -> `(6)` plus a `rememberFeedbackStackState` row in the table that starts at line 429.
- `README.md:84-87`: `**66 registered public composables**` ... `**5 intentionally-unregistered**` structural sub-parts (`WaveformCanvas`, `SwipeableActionRow`, `RevealActionRow`, `YahirAndroidTasteTheme`, `SheetHeaderMenu`) `— 71 public composables total` -> 67 / 6 / 73 and add `rememberFeedbackStackState` to the list.
- Expected post-phase numbers: registered 66 + 1 = **67**, unregistered 5 + 1 = **6**, total **73**, Feedback family **4**.
- Full milestone docs (STYLE, INTEGRATION `launch`-per-event rule, `newInV280` list in the drift guard) are Phase 29. Phase 25 only keeps the guards green.

### api.txt lines that will be rewritten (appended optional params) [VERIFIED: api.txt]
- `Notify` ctor line 796 and `copy` line 799; `WithUndo` ctor line 807 and `copy` line 812; `UndoHistoryEntry.copy` line 923. That is the **5 removed lines** an audit of `git diff v2.7.1 -- api.txt` should show (each replaced by the same signature plus appended `optional` params). Everything else is an addition (`component*`, `get*`, new classes, new `append`, new `emitTrackedWithUndo`).

## Architecture Patterns

### System Architecture Diagram

```
 consumer coroutine ──launch──► state.show(event) ───────────────┐   (one launch per event; never await serially)
                                      │                           │
                                      ▼                           │ suspends on item.result.await()
              ┌──────────── FeedbackStackState ────────────┐      │
              │ lock { items.add(0,item); if >cap:         │      │
              │        oldest.result.complete(Dismissed) }  │      │
              │ timer: delay(scaled base) ─► resolve(TimedOut)    │
              │ resolve(item,r) = lock { if complete(r) {items.remove; true} else false }
              └───────▲──────────────▲───────────────▲─────┘      │
        host body tap │   Undo tap   │  dismissAll() │            ▼
   FeedbackStackHost ─┘ resolve(Opened)  resolve(Undone)   FeedbackResult returned
   (reads state.items newest-first; calls onOpenTarget ONLY if resolve==true)
                                                                   │
 tracked path: emitTrackedWithUndo(store, event)                   │
   1 store.append(target=event.target)  (BEFORE visible)           │
   2 showInternal(... spent-watcher on store.entries)  ◄───────────┘
   3 result==Undone ─► withContext(NonCancellable){ store.attemptUndo(id) }  (CAS: loser is a no-op)
```

### Recommended Project Structure
```
feedback/
├── FeedbackTarget.kt        # NEW  @Immutable data class FeedbackTarget(key, openLabel)
├── FeedbackResult.kt        # NEW  enum { Opened, Undone, Dismissed, TimedOut }
├── FeedbackStackState.kt    # NEW  state, rememberFeedbackStackState, internal FeedbackStackItem, a11y binding
├── FeedbackStackHost.kt     # NEW  public host (tracer) + private card
├── FeedbackEvent.kt         # MOD  +target, +supportingText on Notify/WithUndo
├── UndoHistoryEntry.kt      # MOD  +target, +openableTarget
├── UndoHistoryStore.kt      # MOD  +1 append overload (existing two bodies untouched)
└── UndoTracking.kt          # MOD  line 25 passes target; +FeedbackStackState.emitTrackedWithUndo
explorer/FeedbackFamilyScreen.kt, ComponentRegistry.kt   # MOD (entry; allowlist)
src/test/.../feedback/  FeedbackTargetTest, FeedbackStackStateTest, FeedbackStackTrackedUndoTest, FeedbackStackHostTest; UndoHistoryStoreTest (+cases)
src/test/.../V271CallSiteCanaryTest.kt   # NEW, same package as V251/V260 canaries
```

### Pattern 1: Event, target and result types (signatures to freeze now)
`api.txt` freezes these in this phase; Phases 26-29 only append.
```kotlin
// feedback/FeedbackTarget.kt   (androidx.compose.runtime.Immutable is an allowed import root)
@Immutable
data class FeedbackTarget(val key: String, val openLabel: String)

// feedback/FeedbackResult.kt
enum class FeedbackResult { Opened, Undone, Dismissed, TimedOut }

// FeedbackEvent.kt (append after the existing last param; order is A1 below)
data class Notify(
    val message: String,
    val duration: SnackbarDuration = SnackbarDuration.Short,
    val target: FeedbackTarget? = null,
    val supportingText: String? = null
) : FeedbackEvent
data class WithUndo(
    val message: String,
    val undoAction: suspend () -> Unit,
    val duration: SnackbarDuration = SnackbarDuration.Long,
    val preview: UndoPreview? = null,
    val target: FeedbackTarget? = null,
    val supportingText: String? = null
) : FeedbackEvent
```
KDoc requirements: `openLabel` is localized TalkBack text and must be non-blank (D-02: no `init` check; the host falls back to the card message when blank); `key` is opaque, never parsed or logged by the library; no lambda or `Context` in the type (D-08). `FeedbackTarget` equality is the data-class value equality.

### Pattern 2: Entry and store (lane-1 friendly)
```kotlin
// UndoHistoryEntry.kt: append after consumedGuard (add "," to its line)
    private val consumedGuard: AtomicBoolean = AtomicBoolean(false),
    val target: FeedbackTarget? = null
) {
    /** The target only while the entry is still Available (D-03, D-14). Undone/Failed -> null. */
    val openableTarget: FeedbackTarget? get() = target?.takeIf { status == UndoStatus.Available }
```
```kotlin
// UndoHistoryStore.kt: ONE new overload; leave the two existing bodies byte-identical.
fun append(
    message: String,
    preview: UndoPreview? = null,
    groupId: String? = null,
    target: FeedbackTarget?,            // NO default: this is what keeps every old call on the old overloads
    undoAction: suspend () -> Unit
): String { /* same body as the grouped append; if (groupId != null) groupIdByEntryId[entry.id] = groupId; pass target = target */ }
```
Overload resolution proof [reasoned from the signatures read above; the canary compiles every shape]: `append(m) { }` and `append(m, p) { }` -> only the 3-arg (new overload needs `target`); `append(m, groupId = g) { }` and `append(m, null, g, fn)` -> only the grouped one (new overload's 4th positional is `FeedbackTarget?`, a function does not fit); CalTracker's `undoHistoryStore.append(event.message, event.preview, event.undoAction)` (function value as 3rd positional, not a trailing lambda) -> only the 3-arg (the new overload's 3rd positional is `String?`); `append(message = ..., preview = ..., target = ..., undoAction = ...)` -> only the new one. `groupId: String? = null` on the new overload costs nothing and lets a grouped entry carry a target later (Phase 27 Undo Center rows).

### Pattern 3: `FeedbackStackState` (CAS deferred per item; skeleton, not compiled)
```kotlin
@Stable
class FeedbackStackState(val maxVisible: Int = 3) {
    private val cap = maxVisible.coerceAtLeast(1)            // hub types never throw on bad input (D-02 spirit)
    private val lock = Any()
    private var nextId = 0L
    internal val items = mutableStateListOf<FeedbackStackItem>()        // newest first
    @Volatile internal var timeoutScaler: ((baseMillis: Long, containsControls: Boolean) -> Long)? = null

    suspend fun show(event: FeedbackEvent): FeedbackResult = showInternal(event, entryId = null)

    fun dismissAll() { synchronized(lock) { items.toList() }.forEach { resolve(it, FeedbackResult.Dismissed) } }

    internal fun resolve(item: FeedbackStackItem, r: FeedbackResult): Boolean =
        synchronized(lock) { if (item.result.complete(r)) { items.remove(item); true } else false }

    internal suspend fun showInternal(
        event: FeedbackEvent,
        entryId: String?,
        watch: (suspend (FeedbackStackItem) -> Unit)? = null
    ): FeedbackResult = coroutineScope {
        val item = synchronized(lock) { FeedbackStackItem.from(event, nextId++, entryId) }
        synchronized(lock) {
            items.add(0, item)
            if (items.size > cap) items.removeAt(items.lastIndex).result.complete(FeedbackResult.Dismissed)   // D-06
        }
        val timer = item.baseMillis?.let { base ->            // null for Indefinite: never launched
            launch { delay(timeoutScaler?.invoke(base, item.hasControls) ?: base); resolve(item, FeedbackResult.TimedOut) }
        }
        val watcher = watch?.let { launch { it(item) } }
        try { item.result.await() }
        finally { timer?.cancel(); watcher?.cancel(); resolve(item, FeedbackResult.Dismissed) } // no-op unless cancelled
    }
}
```
Key behaviors the tests must pin: `complete()` returning `false` on every call after the first is the exactly-one guarantee [CITED: kotlinlang.org/api/kotlinx.coroutines CompletableDeferred.complete, fetched via Context7: "Returns true if this invocation completed the deferred, false if it was already completed. Subsequent invocations have no effect and always return false."]. Removal happens inside the CAS lock so a resolved-but-not-yet-removed card never counts toward the cap. Cancellation of the caller propagates `CancellationException` out of `coroutineScope` (no result, D-05), `finally` removes the item.

`FeedbackStackItem` (internal, `@Stable`): `id: Long`, `message`, `supportingText`, `target`, `hasUndo`, `baseMillis: Long?`, `entryId: String?`, `val result = CompletableDeferred<FeedbackResult>()`, `var spent by mutableStateOf(false)` (must be snapshot state so the host recomposes), `val hasControls get() = hasUndo || target != null` (D-04). Mapping: `Notify` and `WithUndo` copy their fields; `DismissScreen` -> message only, `SnackbarDuration.Short`, no target, no undo (D-13). Base millis: `Short` 4000, `Long` 10000, `Indefinite` -> `null` [VERIFIED: javap of material3 1.4.0 `SnackbarHostKt.toMillis`: `ldc2_w 9223372036854775807l` / `10000l` / `4000l`, then `calculateRecommendedTimeoutMillis(orig, true, true, hasAction)`].

### Pattern 4: Tracked path (SC4)
```kotlin
// UndoTracking.kt: new function; the M3 function changes only line 25 (adds target = event.target)
suspend fun FeedbackStackState.emitTrackedWithUndo(store: UndoHistoryStore, event: FeedbackEvent.WithUndo): FeedbackResult {
    val entryId = store.append(message = event.message, preview = event.preview, target = event.target, undoAction = event.undoAction)  // BEFORE visible
    val result = showInternal(event, entryId) { item ->            // D-08 spent watcher
        store.entries.first { es -> es.none { it.id == entryId && it.status == UndoStatus.Available } }
        item.spent = true
    }
    if (result == FeedbackResult.Undone) withContext(NonCancellable) { store.attemptUndo(entryId) }   // D-09, new path only
    return result
}
```
`NonCancellable` semantics [CITED: Context7 /websites/kotlinlang_api_kotlinx_coroutines, withContext "Passing NonCancellable in the context argument is a special case that allows the block to run even if the parent coroutine is cancelled"]. It closes the window where `tryConsume()` succeeded and a cancellation lands before the status write (entry would stay Available with a tripped guard, "inert"). If the caller is cancelled before `showInternal` returns, no undo runs at all and the entry stays Available and still undoable from the Undo Center (D-05). The result is `Undone` even when an Undo Center tap won the CAS (D-08: `attemptUndo` is a no-op for the loser).

### Pattern 5: Tracer host and accessibility binding
```kotlin
@Composable
fun FeedbackStackHost(
    state: FeedbackStackState,
    onOpenTarget: (FeedbackTarget) -> Unit,
    modifier: Modifier = Modifier,
    undoLabel: String = "Undo",
    dismissLabel: String = "Dismiss"
)
```
- Public signature is the D-09 one. `onOpenTarget` is a required non-trailing function parameter and the last parameter is a `String`, so a trailing lambda can never bind wrongly; Phase 26+ appends only after `dismissLabel`.
- Tracer behavior: `Column` of cards from `state.items` (newest-first, `key(item.id)`); body tap runs `if (item.target != null && !item.spent && state.resolve(item, Opened)) currentOnOpen(item.target)` (callback only after winning the CAS, so a double tap navigates once); Undo (`undoLabel`, shown only for `hasUndo && !item.spent`) runs `state.resolve(item, Undone)`; the click label is `target.openLabel.ifBlank { item.message }` (D-02). `dismissLabel` is accepted but unused until Phase 26 wires the Dismiss custom action (say so in KDoc). Read `onOpenTarget` through `rememberUpdatedState`.
- A11y binding (D-06): one `internal` composable extension (`FeedbackStackState.BindAccessibilityTimeout()`, internal so the registry guard ignores it) reads `LocalAccessibilityManager.current` (nullable) and installs `timeoutScaler = { base, controls -> am.calculateRecommendedTimeoutMillis(base, containsIcons = true, containsText = true, containsControls = controls) }` in a `DisposableEffect(state, am)` with `onDispose { state.timeoutScaler = null }`. Call it from both `FeedbackStackHost` and `rememberFeedbackStackState`. It is read once, when the item's timer starts, so an event enqueued before any host composes keeps the unscaled value (document it).
- `rememberFeedbackStackState(maxVisible: Int = 3) = remember(maxVisible) { FeedbackStackState(maxVisible) }` plus the binding. KDoc: treat `maxVisible` as constant; do not hoist into a ViewModel/singleton (items hold `undoAction` lambdas that capture screen scope); no host means a non-Indefinite card still times out, an Indefinite one never resolves.

### Anti-Patterns to Avoid
- **Boolean "resolved" flag beside the deferred**, or running `onOpenTarget` before `complete()` wins (Pitfall 1).
- **Calling `event.undoAction` from `show()` or the host.** The stack path only reports `Undone`; only `store.attemptUndo(id)` runs the action.
- **Forgetting to cancel child coroutines.** `coroutineScope` joins its children before returning; an uncancelled `store.entries.first {}` watcher or timer makes `show()` hang forever after the item resolved. Cancel both in `finally`.
- **Editing `SnackbarHostState.emitTrackedWithUndo` beyond line 25** (no "improvements", keep `actionLabel = "Undo"`), or adding `NonCancellable` to it.
- **A `target` default on the new `append` overload** (makes old calls ambiguous), or inserting any new param before an existing one (R5).
- **Throwing `init`/`require` in `FeedbackTarget` or the state** (D-02).

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Exactly one result under racing resolvers | a boolean + lock dance | `CompletableDeferred.complete()` return value | It is an atomic CAS; the first of tap/timeout/swipe/eviction/`dismissAll` wins |
| First-consumer-wins undo | a second guard in the stack | `UndoHistoryStore.attemptUndo` (`tryConsume` CAS) | One guard shared with the Undo Center and the M3 path |
| Accessibility-adjusted timeout | hard-coded 4000/10000 only | `AccessibilityManager.calculateRecommendedTimeoutMillis(base, true, true, hasControls)` | Matches M3's `toMillis` exactly (D-04) |
| Virtual-time timers | `System.currentTimeMillis` | `delay` under `runTest` | `advanceTimeBy(3999)` pending, `+1` resolves; no clock seam needed without pause |
| `api.txt` edits | hand-editing the signature file | `./gradlew apiDump` | Deterministic; the additive proof is `verify-api-source-additive.sh` |
| Copy/default synthetics for old signatures | `@Deprecated(HIDDEN)` shims | nothing (source compat only) | CLAUDE.md forbids new ones |

**Key insight:** every "exactly once" requirement in this phase already has a primitive (`CompletableDeferred`, the store's `AtomicBoolean`); the bugs come from adding a second, weaker guard beside it.

## Common Pitfalls

### Pitfall 1: `apiCheck` is red until `apiDump` runs
**What goes wrong:** Appending a param to a data class makes Metalava report `Removed constructor ... FeedbackEvent.Notify(String,SnackbarDuration) [RemovedMethod]` and `Removed method ... Notify.copy(...)`. [VERIFIED: ran it this session with a one-line `Notify` edit, exit 1, then reverted with `git checkout`.]
**Why:** the compat check compares to the committed `api.txt`, and a changed signature reads as removed + added.
**How to avoid:** every public-API commit runs `./gradlew apiDump` first (single invocation: `./gradlew apiDump testDebugUnitTest detekt apiCheck --no-daemon`, the form Phase 20 used), commits `api.txt`, then proves additivity with `bash tools/verify-api-source-additive.sh v2.7.1` (R1-R8; expect `required_param_changes=0 signature_changes=0 accepted_exceptions=0`) and the removed-line audit (5 lines, above).
**Warning signs:** `apiCheck` red with only "Removed ..." lines for the types you widened.

### Pitfall 2: Lane-2 classification on `UndoTracking.kt:25`
**What goes wrong:** `tools/hooks/pre-commit` runs `classify-hub-change.sh`; any staged `src/main` removed line without an identical added line (after stripping one trailing `,` or `+` and whitespace) is lane 2 and the commit is blocked. [VERIFIED: tools/verify-additive-diff.sh normalize(), lines ~104-112.] Adding a trailing comma to an existing param line is safe; rewriting `UndoTracking.kt:25` is not.
**How to avoid:** keep `UndoHistoryStore`'s two existing `append` bodies untouched (add the new overload as a standalone function), and commit the `UndoTracking.kt` line-25 change with `HUB_LANE_OVERRIDE=2` and a stated reason (passes `target`; identical behavior when `target == null`). Editing the `ComponentRegistry.kt` KDoc counts (lines 18-19) is also lane 2; either skip it (it says to read the registry) or fold it into a declared commit. A global secret scanner also runs on staged content: keep test fixtures like `"item-42"`, never key-looking strings.

### Pitfall 3: Drift guards scan the wrong tree in a worktree
`DomainVocabularyDriftGuardTest` / `ComponentRegistryDriftGuardTest` walk up to a directory literally named `yahirandroidtaste`. `.planning/config.json` has `use_worktrees: true`; in a worktree not named that, the guards scan the MAIN checkout and pass vacuously (recorded in Phase 20 summary). **Avoid:** execute inline in the main checkout, or re-run the two guards in the main checkout after the merge before closing the phase.

### Pitfall 4: Exactly-once holes
Test each racing pair at the same virtual instant: tap + timeout, Undo + eviction, `dismissAll` + tap, double tap, cancellation + resolve. A resolved-but-present item counting toward the cap (fixed by removing inside the CAS lock) would evict a live card early.

### Pitfall 5: `show()` hangs
Indefinite + no host never resolves (document it); an uncancelled child makes `show()` hang after resolution (above); the `watch` lambda must not outlive the item.

### Pitfall 6: Positional-shape regressions
`UndoHistoryEntry` has a public `copy` and `component1..6` even with an internal ctor; `target` goes last so positional `copy` and destructuring are unchanged. Real consumer shapes to pin in the canary: CalTracker `undoHistoryStore.append(event.message, event.preview, event.undoAction)`; SecondBrain `FeedbackEvent.Notify(event.message, event.duration)` (positional) and the fully-named `WithUndo(message = ..., preview = ..., undoAction = ..., duration = ...)` in a non-declaration order.

### Pitfall 7: Guards that look done but are not
`"Feedback"` allowlist (both composables share it), the API.md table row AND the three prose counts AND the `(6)` heading AND the README sentence, the `states` matrix (4 labels) and non-null `content` for the new entry, and `remember*` composable at column 0 (allowlisted, not registered). A registered entry that is also in `INTENTIONALLY_UNREGISTERED` throws in `ComponentRegistry.init`.

### Pitfall 8: Compose-test timing
Host tests must use `SnackbarDuration.Indefinite` (the timer runs on the `show()` caller's dispatcher, not `mainClock`). Launch `show` from a `rememberCoroutineScope()` captured in `setContent`. Inject a fake `AccessibilityManager` through `LocalAccessibilityManager` (Robolectric's default is unverified, see A4). `advanceTimeBy`/`runCurrent` need `@OptIn(ExperimentalCoroutinesApi::class)` (otherwise only compiler warnings; the spike showed warnings, not errors).

## Code Examples

### Plain-JVM state test recipe (spike-verified idioms)
```kotlin
// Source: spike run this session (3/3 passed, then deleted): mutableStateListOf, delay boundary, SnackbarHostState in plain runTest
@Test fun short_times_out_at_4000() = runTest {
    val state = FeedbackStackState(3)
    val r = async { state.show(FeedbackEvent.Notify("m")) }   // Short
    runCurrent(); advanceTimeBy(3999); assertFalse(r.isCompleted)
    advanceTimeBy(1); runCurrent(); assertEquals(FeedbackResult.TimedOut, r.await())
}
```
```kotlin
// M3-path characterization (no such test exists today); SnackbarHostState works without composition
val host = SnackbarHostState(); val store = UndoHistoryStore(); var undone = 0
val job = launch { host.emitTrackedWithUndo(store, FeedbackEvent.WithUndo("m", { undone++ }, target = t)) }
runCurrent(); assertEquals(t, store.entries.value.single().target)      // appended before/while shown
host.currentSnackbarData!!.performAction(); runCurrent(); assertEquals(1, undone)
```
Canary shape list (`V271CallSiteCanaryTest`, package `io.github.ygaray.yahirandroidtaste`, mirrors `V260CallSiteCanaryTest`: `@RunWith(RobolectricTestRunner::class) @Config(sdk = [35])`, provenance comments `SB <file>:<line>` / `CT <file>:<line>`, composable calls in non-invoked lambdas): `Notify("m")`, `Notify("m", SnackbarDuration.Long)`, `Notify(message = "m", duration = ...)`, `WithUndo("m", { })`, `WithUndo("m", { }, SnackbarDuration.Short, UndoPreview.Tag("x"))`, the named shapes above, `copy(message = "x")` / `copy(duration = ...)`, destructuring `val (m, d) = notify` and `val (m, u, d, p) = withUndo`, `store.append("m") { }`, `store.append("m", preview) { }`, `store.append(m, p, fn)` (function value, CT shape), `store.append("m", groupId = "g") { }`, `entry.copy(status = ...)`, `entry.withStatus(...)`, and `SnackbarHostState.emitTrackedWithUndo(store, event)`. Assert defaults too (`target == null`, `supportingText == null`).

## State of the Art

| Old Approach | Current Approach | Impact |
|--------------|------------------|--------|
| M3 `SnackbarHostState`: one `Mutex` + single `currentSnackbarData`, timeout in the host's `LaunchedEffect` | Per-item `CompletableDeferred` + timer inside `show()` | Concurrent cards, timers testable without composition |
| Unscoped `emitTrackedWithUndo` returning `Unit` | Stack variant returns `FeedbackResult` | Additive; callers can branch on Opened vs Dismissed |

**Deprecated/outdated:** nothing in this phase's surface. Do not use the deprecated `SwipeToDismissBox` `confirmValueChange` (Phase 26 concern).

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | Param order `target` then `supportingText` on both events (D-09 lists `target`; D-01 says `supportingText` is "last"). Both are appended after every existing param, so no positional v2.7.1 call changes | Pattern 1 | Low: only the positional position of two new optional params; confirm before `api.txt` freezes |
| A2 | Phase 25 timer is plain `delay`, no pause/`TimeSource`; pause semantics (reason set {pointer, fanned}, 800 ms floor) land in Phase 26 | Pattern 3 | Low: internal code only; Phase 26 restructures `showInternal`'s timer |
| A3 | `groupId: String? = null` on the new `append` overload | Pattern 2 | Low: harmless extra param; drop it if the planner wants the smallest surface |
| A4 | Robolectric's default `LocalAccessibilityManager` does not change 4000/10000 | Pitfall 8 | Medium: tests that rely on it could flake; mitigated by always injecting a fake |
| A5 | Android's `calculateRecommendedTimeoutMillis` returns >= the original (never shortens) | Pattern 5 | Low: M3 relies on the same call |
| A6 | `DisposableEffect` (set on enter, null on dispose) is the right binding for the scaler | Pattern 5 | Low: `SideEffect` also works; chosen to avoid a leaked reference |
| A7 | Metalava will list the new data-class `component*`/`get*` members without surprises | api.txt | Low: `apiDump` output is reviewed in the commit; R2 treats them as additions |

## Open Questions

1. **Pause plumbing in Phase 25?** Recommendation: no (A2). ROADMAP says the cap/timer "may" live in the state but FSTK-06 is accepted in Phase 26.
2. **`dismissLabel` unused in the tracer.** Recommendation: accept and document; Phase 26 wires the custom action.
3. **Should the `ComponentRegistry` KDoc counts be refreshed?** Recommendation: yes if a lane-2 commit is already declared, otherwise leave (no test reads it).

## Validation Architecture

`workflow.nyquist_validation` is `true` in `.planning/config.json`, so this section applies.

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit 4.13.2 + kotlinx-coroutines-test 1.11.0 (state, store, tracked path, canary compile) + Robolectric 4.16.1 / `createComposeRule` (host) |
| Config file | none beyond `build.gradle.kts` (`isIncludeAndroidResources = true`); no new infra |
| Quick run command | `./gradlew testDebugUnitTest --tests '*FeedbackStackStateTest' --tests '*FeedbackTargetTest' --tests '*FeedbackStackTrackedUndoTest' --no-daemon` |
| Guard run command | `./gradlew testDebugUnitTest --tests '*CatalogDocsDriftGuardTest' --tests '*DomainVocabularyDriftGuardTest' --tests '*ComponentRegistryDriftGuardTest' --tests '*ComponentStatesMatrixTest' --tests '*ComponentPlaygroundIntegrityTest' --tests '*LibraryInvariantsGuardTest' --no-daemon` |
| Full suite / phase gate | `./gradlew apiDump testDebugUnitTest detekt apiCheck --no-daemon` then `bash tools/verify-api-source-additive.sh v2.7.1` then `bash tools/test/run-all.sh` (never in parallel with Gradle) |
| Fresh-evidence rerun | add `--rerun-tasks` for gate evidence (an "up-to-date" result can be another build's cache) |

Measured on this host: a filtered run is 22-32 s with warm caches; `apiCheck detekt` 29 s; `run-all.sh` 24 s. Full-suite duration was not measured (Phase 20 recorded 867 tests, 0 failures, 22 skipped).

### Phase Requirements -> Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| FTGT-01 | `FeedbackTarget` value-equal, no function-typed field (reflect `declaredFields`) | unit | `--tests '*FeedbackTargetTest'` | Wave 0 |
| FTGT-01 | Every v2.7.1 `Notify`/`WithUndo`/`append`/`copy`/destructure shape compiles and keeps defaults | unit (Robolectric runner like V260) | `--tests '*V271CallSiteCanaryTest'` | Wave 0 |
| FTGT-02 | `append(target=)` stores target; `withStatus` keeps it; `openableTarget` null after Undone and Failed; old overloads unchanged | unit | `--tests '*UndoHistoryStoreTest'` | exists, extend |
| FTGT-02 | M3 path: target lands in store; ActionPerformed undoes once; Dismissed never undoes | unit | `--tests '*FeedbackStackTrackedUndoTest'` (or a sibling `UndoTrackingMaterialPathTest`) | Wave 0 |
| FSTK-01 | one result per `show` (second `resolve` false); Opened/Undone/Dismissed; Short 3999/+1, Long 10000, Indefinite never; 4th arrival -> oldest Dismissed, newest-first; `dismissAll`; cancel removes item + rethrows; DismissScreen mapping; scaler receives `containsControls = hasUndo || target != null`; resolve frees cap; `maxVisible = 0` coerced | unit (plain `runTest`) | `--tests '*FeedbackStackStateTest'` | Wave 0 |
| FSTK-01 | `rememberFeedbackStackState` survives recomposition | Robolectric | `--tests '*FeedbackStackHostTest'` | Wave 0 |
| FSTK-07 | append before visible; Undone -> `undoAction` exactly once via `attemptUndo`; racing `attemptUndo` still once; Opened/Dismissed/TimedOut zero; target copied; spent flips on external undo; cancel during undo (gated `undoAction`) still ends Undone; cancel before result leaves entry Available | unit | `--tests '*FeedbackStackTrackedUndoTest'` | Wave 0 |
| FSTK-01/07 | Host: body tap calls `onOpenTarget` once (double tap = once) and resolves Opened; Undo resolves Undone; German `undoLabel` renders as passed; no-target card not clickable; spent card hides Undo; blank `openLabel` falls back to message | Robolectric compose | `--tests '*FeedbackStackHostTest'` | Wave 0 |
| FSTK-01 | Registry/allowlist/vocabulary/docs guards green | guard | the guard run command | exists, edit |

### Sampling Rate
- **Per task commit:** the quick run command for the touched area (about 30 s).
- **Per wave merge:** the guard run command plus quick run.
- **Phase gate:** full suite + `detekt` + `apiDump`/`apiCheck` + `verify-api-source-additive.sh v2.7.1` (expect 0 removed classes/members, 0 required-param changes) + `run-all.sh`, all fresh, in the main checkout.

### Wave 0 Gaps
- [ ] `src/test/.../feedback/FeedbackTargetTest.kt` (FTGT-01)
- [ ] `src/test/.../V271CallSiteCanaryTest.kt` (FTGT-01; new, no V27x canary exists today: `grep -rln V27 src/test` is empty)
- [ ] `src/test/.../feedback/FeedbackStackStateTest.kt` (FSTK-01)
- [ ] `src/test/.../feedback/FeedbackStackTrackedUndoTest.kt` incl. M3-path characterization (FTGT-02, FSTK-07)
- [ ] `src/test/.../feedback/FeedbackStackHostTest.kt` (FSTK-01/07)
- [ ] Extend `UndoHistoryStoreTest.kt` (FTGT-02)
- [ ] Each new test must fail with its feature removed (RETRO lesson 3); geometry tests are not needed in this phase (no layout contract yet, Phase 26)

## Security Domain

`security_enforcement` is `true` (ASVS level 1, block on high) in `.planning/config.json`.

### Applicable ASVS Categories
| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2 Authentication / V3 Session | no | — |
| V4 Access Control | no (no authorization decisions; consumer routes) | — |
| V5 Input Validation | yes (limited) | `key` is opaque and never parsed, logged or echoed by the library; blank `openLabel` falls back to the message; no throwing validation (D-02) |
| V6 Cryptography | no | — |
| V7 Error handling / logging | yes (limited) | a throwing `undoAction` marks the entry `Failed` (store behavior unchanged); do not log message, key or label |
| V11 Business logic | yes | exactly-once resolution and single undo guard prevent double navigation / double undo |

### Known Threat Patterns
| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| Race: two resolvers (tap + timeout) cause double open or double undo | Tampering | `CompletableDeferred.complete()` CAS; side effects only on `true`; undo only via `store.attemptUndo` |
| Stale target opens a deleted/undone item | Tampering / Info | `openableTarget` and the card `spent` flag derive from store status (D-14) |
| Context or NavController retained by the singleton store | Info disclosure / DoS (leak) | `FeedbackTarget` is two strings; no lambda, no Context (D-08); state holds no AccessibilityManager after dispose |
| Secret-looking test fixtures blocked or leaked | Info disclosure | global secret scanner runs at commit; use neutral fixtures |

## Sources

### Primary (HIGH confidence)
- Repo files read this session (paths and lines above): `feedback/FeedbackEvent.kt`, `UndoHistoryEntry.kt`, `UndoHistoryStore.kt`, `UndoTracking.kt`, `FeedbackDispatcher.kt`, `explorer/ComponentRegistry.kt`, `FeedbackFamilyScreen.kt`, the five drift-guard tests, `V260CallSiteCanaryTest.kt`, `api.txt`, `API.md`, `README.md`, `build.gradle.kts`, `gradle/libs.versions.toml`, `config/detekt*.yml`, `tools/*` (hook, classifier, additive scripts, README-api-guard.md), `.planning/config.json`, STATE/ROADMAP/REQUIREMENTS/DECISION-MAP/BRIEF, milestone research (ARCHITECTURE, PITFALLS).
- Commands run this session: filtered guard tests (green), `apiCheck detekt` (green), `verify-api-source-additive.sh v2.7.1` (OK), `tools/test/run-all.sh` (all pass), a negative `apiCheck` experiment (red as described, reverted), a 3-test spike (green, deleted), `javap` on `ui` and `material3` jars.
- Real consumer call sites read: SecondBrain `feature/voicecommand/VoiceCommitRenderer.kt`, `VoiceOutcomeHost.kt`, `feature/voice/VoiceClipsViewModel.kt`, `ui/feedback/FeedbackController.kt`; CalTracker `feedback/FeedbackController.kt`, `ui/dailylog/DailyLogScreen.kt` (both pinned `v2.7.1`).

### Secondary (MEDIUM confidence)
- Context7 `/websites/kotlinlang_api_kotlinx_coroutines`: `CompletableDeferred.complete` and `withContext(NonCancellable)` semantics [CITED].

### Tertiary (LOW confidence)
- Android platform behavior of `calculateRecommendedTimeoutMillis` and Robolectric's default accessibility manager (A4, A5): not fetched this session.

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH, no new dependencies, versions read from the toml.
- Architecture: MEDIUM-HIGH, the skeleton follows the milestone research and the spike-verified idioms, but it is uncompiled.
- Pitfalls: HIGH, the three load-bearing ones (apiCheck order, lane 2, worktree blind spot) were verified by running or reading the guard code.

**Research date:** 2026-10-17
**Valid until:** 2026-11-16 (stable; re-check only if `v2.7.1` baseline, the toolchain pins, or a guard test change)
