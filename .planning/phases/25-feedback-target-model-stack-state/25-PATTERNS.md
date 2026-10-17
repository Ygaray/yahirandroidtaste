# Phase 25: Feedback target model & stack state - Pattern Map

**Mapped:** 2026-10-17
**Files analyzed:** 20 (7 new main, 6 modified main, 7 tests/docs groups)
**Analogs found:** 18 / 20

All analog paths are git-tracked source under `src/` (root `src/main/java/io/github/ygaray/yahirandroidtaste/`, abbreviated `$M`; tests `src/test/java/io/github/ygaray/yahirandroidtaste/`, abbreviated `$T`).

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match |
|---|---|---|---|---|
| `$M/feedback/FeedbackTarget.kt` (new) | model | transform | `$M/feedback/FeedbackEvent.kt` (data classes) | role-match |
| `$M/feedback/FeedbackResult.kt` (new) | model (enum) | transform | `UndoStatus` enum, `$M/feedback/UndoHistoryEntry.kt:13` | exact |
| `$M/feedback/FeedbackEvent.kt` (mod) | model | transform | itself, lines 27-30 | exact |
| `$M/feedback/UndoHistoryEntry.kt` (mod) | model | CRUD | itself, lines 47-65 | exact |
| `$M/feedback/UndoHistoryStore.kt` (mod) | service | CRUD | its grouped `append`, lines 93-109 | exact |
| `$M/feedback/UndoTracking.kt` (mod) | service | event-driven | itself, lines 21-37 | exact |
| `$M/feedback/FeedbackStackState.kt` (new) | state holder | event-driven | `UndoHistoryStore` (CAS, `@Singleton`-style state) | partial |
| `$M/feedback/FeedbackStackHost.kt` (new) | component | request-response | `$M/feedback/UndoCenterScreen.kt` and `$M/component/AttentionCue.kt` | role-match |
| `$M/explorer/FeedbackFamilyScreen.kt` (mod) | config (registry entry) | - | `UndoCenterScreen` entry, lines 73-100 | exact |
| `$M/explorer/ComponentRegistry.kt` (mod) | config | - | `INTENTIONALLY_UNREGISTERED`, lines 104-127 | exact |
| `$T/explorer/DomainVocabularyDriftGuardTest.kt` (mod) | test | - | itself, `PRIMITIVE_NOUN_ALLOWLIST` lines 297-346 | exact |
| `API.md`, `README.md` (mod) | docs | - | lines named in RESEARCH "Docs locations" | exact |
| `api.txt` (mod) | generated | - | run `apiDump`, never hand-edit | n/a |
| `$T/V271CallSiteCanaryTest.kt` (new) | test | - | `$T/V260CallSiteCanaryTest.kt` | exact |
| `$T/feedback/FeedbackTargetTest.kt` (new) | test | - | `$T/feedback/UndoHistoryStoreTest.kt` | role-match |
| `$T/feedback/FeedbackStackStateTest.kt` (new) | test | event-driven | `UndoHistoryStoreTest.kt` (plain `runTest`) | role-match |
| `$T/feedback/FeedbackStackTrackedUndoTest.kt` (new) | test | event-driven | `UndoHistoryStoreTest.kt` (CAS/cancel cases) | role-match |
| `$T/feedback/FeedbackStackHostTest.kt` (new) | test | request-response | `$T/feedback/UndoCenterScreenTest.kt` | exact |
| `$T/feedback/UndoHistoryStoreTest.kt` (mod) | test | CRUD | itself | exact |

## Pattern Assignments

### `FeedbackEvent.kt` (mod: add `target`, `supportingText`)

**Analog:** itself, lines 27-30. Add a trailing comma to the existing last param line and append (lane-1 safe).
```kotlin
data class Notify(
    val message: String,
    val duration: SnackbarDuration = SnackbarDuration.Short
) : FeedbackEvent
```
Result (per RESEARCH Pattern 1): append `val target: FeedbackTarget? = null, val supportingText: String? = null` to `Notify` and to `WithUndo` (after `preview`, lines 44-49). Leave `DismissScreen` untouched. Extend the KDoc `@param` list as the file does (lines 24-25).

### `FeedbackTarget.kt` / `FeedbackResult.kt` (new)

**Analog:** `UndoStatus` (`UndoHistoryEntry.kt:13`): `enum class UndoStatus { Available, Undone, Failed }` preceded by KDoc listing each value (lines 5-12). Copy that KDoc-per-value style for `enum class FeedbackResult { Opened, Undone, Dismissed, TimedOut }`.
`FeedbackTarget`: `@Immutable data class FeedbackTarget(val key: String, val openLabel: String)` with KDoc (opaque key, localized non-blank openLabel, no lambda or Context). No `init`/`require` (D-02). Package `io.github.ygaray.yahirandroidtaste.feedback`; imports limited to the allowed roots (`androidx.compose.runtime.Immutable`).

### `UndoHistoryEntry.kt` (mod)

**Analog:** itself, lines 47-65.
```kotlin
data class UndoHistoryEntry internal constructor(
    ...
    val status: UndoStatus = UndoStatus.Available,
    private val consumedGuard: AtomicBoolean = AtomicBoolean(false)
) {
    fun tryConsume(): Boolean = consumedGuard.compareAndSet(false, true)
    fun withStatus(newStatus: UndoStatus): UndoHistoryEntry = copy(status = newStatus)
}
```
Change: add `,` after `consumedGuard`, then `val target: FeedbackTarget? = null` LAST (keeps positional `copy`/componentN unchanged). Add `val openableTarget: FeedbackTarget? get() = target?.takeIf { status == UndoStatus.Available }` inside the body. `withStatus` carries `target` via `copy` automatically.

### `UndoHistoryStore.kt` (mod: ONE new overload)

**Analog:** grouped `append`, lines 93-109. Do NOT edit existing two bodies (lane 2). Add a standalone third overload:
```kotlin
fun append(
    message: String, preview: UndoPreview? = null, groupId: String? = null,
    target: FeedbackTarget?,          // NO default, keeps old calls on old overloads
    undoAction: suspend () -> Unit
): String {
    val entry = UndoHistoryEntry(
        id = UUID.randomUUID().toString(), message = message,
        timestamp = System.currentTimeMillis(), undoAction = undoAction,
        preview = preview, target = target
    )
    if (groupId != null) groupIdByEntryId[entry.id] = groupId
    _entries.update { current -> evictIfNeeded(listOf(entry) + current) }
    return entry.id
}
```
Keep `undoAction` last (trailing-lambda rule, per KDoc lines 60-64). Existing `attemptUndo` (219-236) is the single CAS the stack must call; never call `event.undoAction` directly.

### `UndoTracking.kt` (mod)

**Analog:** lines 21-37 (shown in RESEARCH). Only change line 25 to add `target = event.target` and the new arg (this is a lane-2 commit: `HUB_LANE_OVERRIDE=2`). Keep `actionLabel = "Undo"` and no `NonCancellable` in the M3 function. Add the new extension per RESEARCH Pattern 4, mirroring the existing KDoc style ("Order matters: append BEFORE show") and the comment "only act on explicit user tap ... routed through the store's atomic guard".
Imports for the new function add `kotlinx.coroutines.NonCancellable`, `kotlinx.coroutines.withContext`, `kotlinx.coroutines.flow.first`.

### `FeedbackStackState.kt` (new)

**Analog:** no stack analog exists. Use RESEARCH Pattern 3 skeleton (CompletableDeferred per item, `resolve` under lock removes item, timer in `show()`, `coroutineScope` with `finally` cancelling children, `dismissAll()`, `timeoutScaler`). Conventions to copy from `UndoHistoryStore`: KDoc-heavy, plain class, `@Suppress("TooGenericExceptionCaught")` only where needed, `CancellationException` rethrown (see `attemptUndo` 219-236). `rememberFeedbackStackState(maxVisible: Int = 3)` and the internal `BindAccessibilityTimeout()` live here; the remember function is public top-level `@Composable` at column 0, so it must be allowlisted (below). Detekt zero-baseline applies.

### `FeedbackStackHost.kt` (new)

**Analog:** `$M/feedback/UndoCenterScreen.kt` (feedback-package composable with caller-localizable string params, per v2.6 "Undo Center group words") and `$M/component/AttentionCue.kt`. Use RESEARCH Pattern 5 signature: `(state, onOpenTarget, modifier = Modifier, undoLabel = "Undo", dismissLabel = "Dismiss")`; `modifier` first optional, no trailing lambda after; `rememberUpdatedState(onOpenTarget)`; `key(item.id)`; call `onOpenTarget` only if `state.resolve(item, Opened)` returned true. Use theme tokens and test tags like `feedback_stack_item`, `feedback_stack_undo` (free of API.md tag scan obligations).

### `FeedbackFamilyScreen.kt` (mod)

**Analog:** `UndoCenterScreen` entry, lines 73-100 (PATTERN tier, `Box(Modifier.fillMaxWidth().height(400.dp))` fixture).
```kotlin
ComponentRegistry.Entry(
    name = "UndoCenterScreen",
    family = ExplorerFamilies.FEEDBACK,
    states = listOf(
        ComponentRegistry.StateCell("Default", render = { ... }),
        ComponentRegistry.StateCell("Pressed / Selected"),
        ComponentRegistry.StateCell("Disabled"),
        ComponentRegistry.StateCell("Focused")
    ),
    content = { UndoCenterScreenVariants() },
    tier = ComponentRegistry.Tier.PATTERN
)
```
Append a `FeedbackStackHost` entry after `AttentionCue` (add `,` after line 124 `)`). Exactly the four state labels in that order, non-null `content`, no `controls` (playground guard requires `preview` otherwise). Default render: `rememberFeedbackStackState()` plus a `LaunchedEffect` that shows an Indefinite Notify with a target.

### `ComponentRegistry.kt` (mod)

**Analog:** lines 104-127; add a last entry after `SheetHeaderMenu` (add `,`):
```kotlin
"rememberFeedbackStackState" to
    "State holder remembered by callers and passed to FeedbackStackHost; non-visual, " +
    "exercised indirectly via the FeedbackStackHost entry."
```
Registered XOR allowlisted: never both (init at 129-151 throws). Counts become 67 registered / 6 unregistered / 73 total.

### `DomainVocabularyDriftGuardTest.kt` (mod)

**Analog:** `PRIMITIVE_NOUN_ALLOWLIST` (297-346), last element `"Stacked"` line 345 with comment block 343-344. Add `,` after `"Stacked"` plus a comment and `"Feedback"` (covers both `FeedbackStackHost` and `rememberFeedbackStackState`).

### `API.md` / `README.md` (mod)

Edit lines listed in RESEARCH "Docs locations to edit" (API.md:23, 29-30, 127 table row, 424 heading `(6)` + table row at ~429; README.md:84-87). The `CatalogDocsDriftGuardTest` literals: `**67 registered public composables**`, `**6 intentionally-unregistered**`, `= **73 public composables total**`, `## Intentionally-unregistered sub-parts (6)`; Feedback family row count 3 -> 4.

### `V271CallSiteCanaryTest.kt` (new)

**Analog:** `$T/V260CallSiteCanaryTest.kt`.
```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class V271CallSiteCanaryTest {
    @get:Rule val composeTestRule = createComposeRule()
    /** CT voice/VoiceOutcomeMapper.kt:661: ... every argument named. */   // provenance comment style
    private fun ctV260Shape() = ...
```
Copy: class-level KDoc with `SB <file>:<line>` / `CT <file>:<line>` provenance, neutral holders, composable calls in non-invoked lambdas, no class-level experimental opt-in. Shapes list is in RESEARCH "Canary shape list". Place in package `io.github.ygaray.yahirandroidtaste`.

### `FeedbackStackStateTest`, `FeedbackStackTrackedUndoTest`, `FeedbackTargetTest`, `UndoHistoryStoreTest` additions

**Analog:** `$T/feedback/UndoHistoryStoreTest.kt` (plain JUnit4, `runTest`, `CompletableDeferred`, `awaitCancellation`, `cancelAndJoin`, `AtomicInteger` for exactly-once; imports lines 3-14). Style: section comments `// ---- (a) ... ----`, assertion style `assertEquals(returnedId, entry.id)`. For virtual time use `runCurrent(); advanceTimeBy(3999)` (RESEARCH test recipe), `@OptIn(ExperimentalCoroutinesApi::class)`. Neutral fixtures only (no key-looking strings; secret scanner).

### `FeedbackStackHostTest.kt` (new)

**Analog:** `$T/feedback/UndoCenterScreenTest.kt` lines 39-44: `@RunWith(RobolectricTestRunner::class) @Config(sdk = [35])` plus `@get:Rule val composeTestRule = createComposeRule()`. Use `SnackbarDuration.Indefinite`, scope from `rememberCoroutineScope()`, inject a fake `LocalAccessibilityManager`.

## Shared Patterns

### Source-compat additive rule
**Source:** CLAUDE.md. Append params with defaults; trailing lambdas last; no new `@Deprecated(HIDDEN)`. Gate order: `./gradlew apiDump testDebugUnitTest detekt apiCheck --no-daemon` (apiCheck is RED until apiDump runs), then `bash tools/verify-api-source-additive.sh v2.7.1`. Expect exactly 5 removed api.txt lines (Notify ctor/copy, WithUndo ctor/copy, UndoHistoryEntry.copy).

### Exactly-once and single undo guard
**Source:** `UndoHistoryStore.attemptUndo` (219-236) and `UndoHistoryEntry.tryConsume` (line 58). The stack only reports `Undone`; only `store.attemptUndo(id)` runs `undoAction`, wrapped in `withContext(NonCancellable)` on the new path only.

### Lane classification
**Source:** `tools/hooks/pre-commit`. Removed lines without identical added line are lane 2. Rewriting `UndoTracking.kt:25` needs `HUB_LANE_OVERRIDE=2` with reason; trailing-comma additions are fine.

### Library invariants
**Source:** `LibraryInvariantsGuardTest`: imports only from the allowed roots; no Hilt host annotations; no consumer identifiers.

### Execute in main checkout
Drift guards walk up to a directory named `yahirandroidtaste`; in a differently named worktree they pass vacuously. Run inline or re-run the guards in the main checkout.

## No Analog Found

| File | Role | Data Flow | Reason |
|---|---|---|---|
| `FeedbackStackState.kt` | state holder | event-driven, per-item CAS + timer | No concurrent stack or in-`show()` timer exists; M3 `SnackbarHostState` is a single-item Mutex queue. Use RESEARCH Pattern 3. |
| `FeedbackStackHost.kt` (stack layout) | component | request-response | No stacked/deck host exists; Phase 26 owns the deck look. Use RESEARCH Pattern 5 tracer. |

## Metadata

**Analog search scope:** `src/main/.../feedback`, `explorer`, `component`; `src/test/...` (root, `feedback`, `explorer`)
**Files scanned:** about 15 read or excerpted
**Pattern extraction date:** 2026-10-17
