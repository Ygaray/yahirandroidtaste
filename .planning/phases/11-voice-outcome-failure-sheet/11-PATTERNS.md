# Phase 11: Voice outcome & failure sheet - Pattern Map (REGENERATED)

**Mapped:** 2026-09-30
**Regeneration reason:** Previous `11-PATTERNS.md` predated the D-01 seam freeze (SB-confirmed
2026-09-30, commit `2e02abf`) and only covered the "new presentational projection" shape with no
`UndoHistoryStore` grouping-API changes. VOUT-01/02/03 are **already built** (commit `6a946d3`) —
this map focuses on what remains: the `UndoHistoryStore`/`UndoHistoryEntry` grouping-API additions,
the `UndoAffordanceUiModel`/`UndoRowUiModel`/`UndoRefusedUiModel` presentational types, and the
`ClarificationBar` composable + `ClarificationOptionUiModel`.
**Files analyzed:** 9 (2 extend, 7 new)
**Analogs found:** 9 / 9

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|---|---|---|---|---|
| `feedback/UndoHistoryStore.kt` (EXTEND) | service/store (`@Singleton` state holder) | CRUD + event-driven (claim/resolve state machine) | itself (existing `append`/`attemptUndo`/`clearSpent`/`evictIfNeeded`) — additive extension, not a fresh analog search | exact (self) |
| `feedback/UndoHistoryEntry.kt` | model (immutable data class) | N/A — **ZERO CHANGES this phase** | itself | exact (self, unchanged) |
| `feedback/UndoGroupTypes.kt` (NEW) | model (sealed result/status types) | transform (classifies an operation's outcome) | `feedback/UndoStatus` enum (inline in `UndoHistoryEntry.kt`) for the status-sibling shape; no existing sealed-result-type file in `feedback/` to mirror exactly | role-match |
| `model/UndoAffordanceUiModel.kt` (NEW) | model (presentational, all-val) | transform (store reads → UI projection) | `model/ApproachRungUiModel.kt` (all-val presentational model convention) | role-match |
| `model/UndoRowUiModel.kt` (NEW, incl. `UndoRowState` sealed interface) | model (presentational, sealed state) | transform | `model/KeyFieldState.kt` (sealed-interface-as-own-file convention) — cited in RESEARCH.md; not re-read this pass, convention confirmed by research | role-match |
| `model/UndoRefusedUiModel.kt` (NEW) | model (presentational, all-val) | transform | `model/ApproachRungUiModel.kt` | role-match |
| `model/ClarificationOptionUiModel.kt` (NEW) | model (presentational, all-val, `{id,label}`) | transform | `model/ApproachRungUiModel.kt` | role-match |
| `component/OutcomeSheet.kt` (EXTEND — add `undo` rendering to `SuccessBody`) | component (Compose, sheet content) | request-response (props in, callbacks out) | itself (existing `SuccessBody`/`FailureBody`/`HandledByRow`) — additive extension | exact (self) |
| `component/ClarificationBar.kt` (NEW) | component (Compose, standalone) | request-response (props in: question+options; callback out: onSelect/onDismiss) | `component/AppChip.kt` (pressable token) + `component/SegmentedOptionSelector.kt` (multi-option selection surface) | role-match |
| `explorer/VoiceCommandFamilyScreen.kt` (EXTEND) | config/registry wiring | N/A | itself — existing `OutcomeSheet` registry entry (`explorer/VoiceCommandFamilyScreen.kt:138-152`) is the exact template for the new `ClarificationBar` entry + the undo states added to `OutcomeSheet`'s matrix | exact (self) |
| `src/test/.../feedback/UndoHistoryStoreTest.kt` (NEW or EXTEND) | test | CRUD + event-driven | `src/test/.../component/OutcomeSheetTest.kt` (131 lines, existing) for test-file structure/assertion style; grep `src/test/` for an existing `UndoHistoryStoreTest.kt` before assuming new | role-match |
| `src/test/.../component/ClarificationBarTest.kt` (NEW) | test | request-response | `src/test/.../component/OutcomeSheetTest.kt` | role-match |

## Pattern Assignments

### `feedback/UndoHistoryStore.kt` (EXTEND — service/store)

**Analog:** itself — the file's own existing methods are the pattern to extend additively.

**Current full shape** (`feedback/UndoHistoryStore.kt:23-122`, verified in RESEARCH.md, quoted
here verbatim — this is what the new methods must sit beside, byte-for-byte unchanged):
```kotlin
@Singleton
class UndoHistoryStore @Inject constructor() {
    private val _entries = MutableStateFlow<List<UndoHistoryEntry>>(emptyList())
    val entries: StateFlow<List<UndoHistoryEntry>> = _entries.asStateFlow()

    fun append(message: String, preview: UndoPreview? = null, undoAction: suspend () -> Unit): String {
        val entry = UndoHistoryEntry(id = UUID.randomUUID().toString(), message = message,
            timestamp = System.currentTimeMillis(), undoAction = undoAction, preview = preview)
        _entries.update { current -> evictIfNeeded(listOf(entry) + current) }
        return entry.id
    }

    suspend fun attemptUndo(id: String) {
        val entry = _entries.value.firstOrNull { it.id == id } ?: return
        if (entry.status != UndoStatus.Available) return
        if (!entry.tryConsume()) return
        val newStatus = try { entry.undoAction(); UndoStatus.Undone }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { UndoStatus.Failed }
        _entries.update { current -> current.map { if (it.id == id) it.withStatus(newStatus) else it } }
    }

    fun clearSpent() {
        _entries.update { current -> current.filterNot { it.status == UndoStatus.Undone || it.status == UndoStatus.Failed } }
    }

    private fun evictIfNeeded(current: List<UndoHistoryEntry>): List<UndoHistoryEntry> {
        if (current.size <= 50) return current
        val oldestSpentIndex = current.indexOfLast { it.status == UndoStatus.Undone || it.status == UndoStatus.Failed }
        return if (oldestSpentIndex >= 0) current.filterIndexed { index, _ -> index != oldestSpentIndex }
        else current.dropLast(1)
    }
}
```

**Import pattern to preserve** — no new Gradle dependency; `kotlinx.coroutines.sync.Mutex` is
transitively available (the file already imports `kotlinx.coroutines.flow.*`/`CancellationException`
from the same artifact, per RESEARCH.md Standard Stack).

**Core additive pattern** — the 7-member grouping API, copied from RESEARCH.md's Code Examples
section (illustrative skeleton, `[ASSUMED]` mechanism but D-01-frozen external signatures):
```kotlin
private val groupIdByEntryId = mutableMapOf<String, String>()
private data class GroupMeta(val label: String, val undoAll: suspend () -> Unit)
private val groupMeta = mutableMapOf<String, GroupMeta>()
private val groupUndoMutex = Mutex()

fun openGroup(groupId: String, label: String, undoAll: suspend () -> Unit) {
    groupMeta[groupId] = GroupMeta(label, undoAll)
}

fun append(message: String, preview: UndoPreview? = null, groupId: String, undoAction: suspend () -> Unit): String {
    val entry = UndoHistoryEntry(
        id = UUID.randomUUID().toString(), message = message,
        timestamp = System.currentTimeMillis(), undoAction = undoAction, preview = preview
    )
    groupIdByEntryId[entry.id] = groupId
    _entries.update { current -> evictIfNeeded(listOf(entry) + current) } // eviction now group-aware
    return entry.id
}

fun group(groupId: String): List<UndoHistoryEntry> =
    _entries.value.filter { groupIdByEntryId[it.id] == groupId }

fun groupIdOf(entryId: String): String? = groupIdByEntryId[entryId]
fun groupLabel(groupId: String): String? = groupMeta[groupId]?.label

fun groupStatus(groupId: String): UndoGroupStatus {
    val members = group(groupId)
    if (members.isEmpty()) return UndoGroupStatus.Empty
    val availableCount = members.count { it.status == UndoStatus.Available }
    return when {
        availableCount == members.size -> UndoGroupStatus.Undoable
        availableCount == 0 -> UndoGroupStatus.FullyResolved
        else -> UndoGroupStatus.PartiallyResolved
    }
}

@Suppress("TooGenericExceptionCaught")
suspend fun attemptUndoGroup(groupId: String): UndoGroupResult = groupUndoMutex.withLock {
    val meta = groupMeta[groupId] ?: return@withLock UndoGroupResult.NothingToUndo
    val claimed = group(groupId).filter { it.status == UndoStatus.Available }
    if (claimed.isEmpty()) return@withLock UndoGroupResult.NothingToUndo

    // Deliberately NO tryConsume() call -- the Mutex IS the atomicity boundary (Pitfall 1).
    try {
        meta.undoAll()
        val claimedIds = claimed.map { it.id }.toSet()
        _entries.update { current ->
            current.map { if (it.id in claimedIds) it.withStatus(UndoStatus.Undone) else it }
        }
        UndoGroupResult.Undone(count = claimed.size)
    } catch (e: CancellationException) {
        throw e
    } catch (e: UndoGroupRefusedException) {
        // Refused: nothing was written -- entries were never modified, no "release" needed.
        UndoGroupResult.Refused(reason = e.reason, changedItem = e.changedItem)
    } catch (e: Exception) {
        val claimedIds = claimed.map { it.id }.toSet()
        _entries.update { current ->
            current.map { if (it.id in claimedIds) it.withStatus(UndoStatus.Failed) else it }
        }
        UndoGroupResult.Failed
    }
}
```

**Error-handling pattern to mirror** — the existing `attemptUndo`'s `try/catch(CancellationException)
rethrow → catch(Exception) → Failed` shape (`feedback/UndoHistoryStore.kt` lines within the quoted
block above) is the exact template `attemptUndoGroup` follows; do NOT invent a different exception
taxonomy.

**The trap to avoid (Pitfall 1, HIGH severity):** never call `entry.tryConsume()` inside
`attemptUndoGroup` — `UndoHistoryEntry.tryConsume()`'s `AtomicBoolean` CAS is one-way (KDoc:
*"Returns true only for the FIRST caller to claim this entry; false for every caller after"* —
`feedback/UndoHistoryEntry.kt:57`), so a claimed-then-Refused entry would be permanently
unconsumable even after its `.status` is reverted to `Available`. Use the `Mutex` as the sole
atomicity boundary instead.

**`clearSpent()`/`evictIfNeeded` must both gain a groupId-aware branch** (Pitfall 2) — ungrouped
entries keep byte-for-byte identical behavior; grouped entries are only dropped/evicted as a whole
group once `groupStatus(groupId) == FullyResolved`.

---

### `feedback/UndoHistoryEntry.kt` — ZERO CHANGES

**Analog:** itself. Confirmed current full shape (47-66 lines, `internal constructor`):
```kotlin
data class UndoHistoryEntry internal constructor(
    val id: String,
    val message: String,
    val timestamp: Long,
    val undoAction: suspend () -> Unit,
    val preview: UndoPreview? = null,
    val status: UndoStatus = UndoStatus.Available,
    private val consumedGuard: AtomicBoolean = AtomicBoolean(false)
) {
    fun tryConsume(): Boolean = consumedGuard.compareAndSet(false, true)
    fun withStatus(newStatus: UndoStatus): UndoHistoryEntry = copy(status = newStatus)
}
```
`UndoStatus` is `enum class UndoStatus { Available, Undone, Failed }` — exactly 3 members; D-01
forbids adding a 4th (would break consumers' exhaustive `when`s).

---

### `feedback/UndoGroupTypes.kt` (NEW)

**Analog:** the existing `UndoStatus` enum (inline in `UndoHistoryEntry.kt`) for a status-sibling
naming/placement convention; no closer existing sealed-result file exists in `feedback/`.

**Pattern** — copy verbatim from RESEARCH.md (field names are D-01-frozen, not invented):
```kotlin
package io.github.ygaray.yahirandroidtaste.feedback

sealed interface UndoGroupStatus {
    data object Undoable : UndoGroupStatus
    data object PartiallyResolved : UndoGroupStatus
    data object FullyResolved : UndoGroupStatus
    data object Empty : UndoGroupStatus
}

sealed interface UndoGroupResult {
    data class Undone(val count: Int) : UndoGroupResult
    data class Refused(val reason: String, val changedItem: String? = null) : UndoGroupResult
    data object Failed : UndoGroupResult
    data object NothingToUndo : UndoGroupResult
}

class UndoGroupRefusedException(
    val reason: String,
    val changedItem: String? = null
) : Exception(reason)
```
Organizational note (Open Question 3 in RESEARCH.md): a single `UndoGroupTypes.kt` file is fine —
this repo has precedent both for small-sealed-type-per-file and for types-beside-their-composable;
planner may choose either as long as the shapes above are preserved.

---

### `model/UndoAffordanceUiModel.kt`, `UndoRowUiModel.kt`, `UndoRefusedUiModel.kt` (NEW)

**Analog:** `model/ApproachRungUiModel.kt` — all-`val`, presentational, no logic, convention for
new UI-projection models in this repo's `model/` package.

**Pattern** — field names/shapes are copied verbatim from D-01's frozen `11-CONTEXT.md` text, not
invented:
```kotlin
// model/UndoRowUiModel.kt
sealed interface UndoRowState {
    data class Available(val onUndo: () -> Unit) : UndoRowState
    data object Undone : UndoRowState
    data class Unavailable(val reason: String) : UndoRowState
}

data class UndoRowUiModel(val id: String, val label: String, val state: UndoRowState)

// model/UndoRefusedUiModel.kt
data class UndoRefusedUiModel(val reason: String, val changedItem: String? = null)

// model/UndoAffordanceUiModel.kt
data class UndoAffordanceUiModel(
    val allLabel: String,
    val rows: List<UndoRowUiModel> = emptyList(),
    val onUndoAll: (() -> Unit)? = null,
    val refused: UndoRefusedUiModel? = null
)
```

Sealed-interface-as-own-file convention (`UndoRowState` nested in `UndoRowUiModel.kt`) mirrors
`model/KeyFieldState.kt` per RESEARCH.md's Sources list.

**Where the projection is built (do NOT build it inside the library):** per D-01 and the
Architectural Responsibility Map, `UndoAffordanceUiModel`/`UndoRowUiModel` are constructed by the
CONSUMER app from `store.group(groupId)`/`.groupStatus(groupId)` reads — the library itself never
holds a reference to any app's `UndoHistoryStore` instance (INV-01). These model files define the
shape only; `OutcomeSheet.kt` only renders whatever `Success.undo` it's given.

---

### `model/ClarificationOptionUiModel.kt` (NEW)

**Analog:** `model/ApproachRungUiModel.kt` (all-val presentational convention).

```kotlin
data class ClarificationOptionUiModel(val id: String, val label: String)
```
`id` is opaque — the library never interprets it (L7, no engine dependency); the consumer maps a
tapped `id` back to its own domain concept.

---

### `component/OutcomeSheet.kt` (EXTEND — add `undo` rendering to `SuccessBody`)

**Analog:** itself. Confirmed current file (167 lines) already implements `SuccessBody`/
`FailureBody`/`HandledByRow`/`BatchResultsList` per VOUT-01/02/03/D-04/05/06/08 — verified live,
registered in `ComponentRegistry` via `explorer/VoiceCommandFamilyScreen.kt:138-152`. Do not
re-touch this existing logic; only add the `undo` field to `Success` and one new render branch.

**Extension pattern** (illustrative diff shape from RESEARCH.md):
```kotlin
// VoiceOutcomeUiState.Success gains one new field (additive, defaulted null):
// val undo: UndoAffordanceUiModel? = null   // D-02: undo lives ON Success

@Composable
private fun SuccessBody(success: VoiceOutcomeUiState.Success) {
    Column(/* unchanged */) {
        Text(success.summary, style = MaterialTheme.typography.headlineSmall)
        success.handledBy?.let { HandledByRow(it) }
        success.batchResults.takeIf { it.isNotEmpty() }?.let { BatchResultsList(it) }
        success.editableContent?.takeIf { !success.inFlight }?.invoke()
        success.undo?.let { UndoAffordanceBody(it) }   // NEW
    }
}

@Composable
private fun UndoAffordanceBody(undo: UndoAffordanceUiModel) {
    Column {
        undo.onUndoAll?.let { onUndoAll ->
            DynamicActionButton(label = undo.allLabel, role = ActionButtonDefaults.ActionButtonRole.Neutral, onClick = onUndoAll)
        }
        undo.rows.forEach { row -> UndoRowItem(row) }
        undo.refused?.let { refused ->
            // Loud per VOUT-03's discipline: error container, not a muted caption.
            Surface(color = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer) {
                Text("Couldn't undo: ${refused.reason}" + (refused.changedItem?.let { ", $it changed since" } ?: ""))
            }
        }
    }
}
```

**Loud-failure pattern to reuse (already shipped in this file's `FailureBody`):** `errorContainer`/
`onErrorContainer` theme roles, never `AttentionCue` (its KDoc explicitly forbids use as a failure
signal — `component/AttentionCue.kt`).

---

### `component/ClarificationBar.kt` (NEW)

**Analogs:**
- `component/AppChip.kt` (`88-175`, verified) — compact pressable labeled token; already handles
  48dp touch target and selected/unselected roles.
- `component/SegmentedOptionSelector.kt` — multi-option selection surface precedent.
- `component/DynamicActionButton.kt` (`37-58`, verified) — role-colored action button for the
  dismiss action if a button (not a chip) is preferred.

**Pattern** (from RESEARCH.md Code Examples):
```kotlin
// model/ClarificationOptionUiModel.kt
data class ClarificationOptionUiModel(val id: String, val label: String)

// component/ClarificationBar.kt
@Composable
fun ClarificationBar(
    question: String,
    options: List<ClarificationOptionUiModel>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(question, style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                AppChip(label = option.label, isSelected = false, onClick = { onSelect(option.id) })
            }
        }
        TextButton(onClick = onDismiss) { Text("Dismiss") }
    }
}
```

**Naming-gate action required in the SAME commit** (Pitfall 3): add `"Clarification"` to
`PRIMITIVE_NOUN_ALLOWLIST` in `src/test/.../explorer/DomainVocabularyDriftGuardTest.kt` (currently
absent — confirmed via grep). Mirror the already-landed `"Outcome"` entry's comment style verbatim
(`explorer/DomainVocabularyDriftGuardTest.kt:326-329`):
```
"Outcome"   // Phase 11 (VOUT-01/02/03, VUNDO-01): 'Outcome' is OutcomeSheet's head token — a
            // generic UI-archetype noun (a command-result presentation surface), not
            // consumer-domain vocabulary; the library authors no app-specific noun of its own.
```
`"Undo"` is already present in `PRIMITIVE_NOUN_ALLOWLIST` (line 314) — a composable named
`UndoRow`/`UndoAllButton` needs no allowlist edit.

---

### `explorer/VoiceCommandFamilyScreen.kt` (EXTEND — registry wiring)

**Analog:** itself — the existing `OutcomeSheet` entry (`explorer/VoiceCommandFamilyScreen.kt:
138-152`) is the exact template: `name = "OutcomeSheet"`, full 4-state matrix,
`content = { OutcomeSheetVariants() }`. Add:
1. New undo/clarification states to `OutcomeSheet`'s existing matrix.
2. A new sibling registry entry for `ClarificationBar` following the identical shape.

---

### Test files

**Analog:** `src/test/.../component/OutcomeSheetTest.kt` (131 lines, verified) — covers
Success-summary render, handled-by present/absent, Failure surface+reason, action-button
present/click/absent. Mirror this assertion style for:
- `UndoHistoryStoreTest.kt` (pure-Kotlin JUnit, no Compose needed — grep `src/test/` first to check
  whether one already exists from Phase 53/58; extend rather than duplicate).
- New `OutcomeSheetTest.kt` cases for `undo` rendering (Undo-all, per-row states, nested Refused).
- `ClarificationBarTest.kt` (new file) — question/options render, `onSelect`/`onDismiss` firing.

## Shared Patterns

### Loud-failure / undo-refused visual treatment
**Source:** `component/OutcomeSheet.kt`'s existing `FailureBody` (verified this session — already
uses `errorContainer`/`onErrorContainer`).
**Apply to:** the new nested `UndoRefusedUiModel` surface inside `UndoAffordanceBody` — same theme
roles, sticky, never `AttentionCue` (forbidden by its own KDoc for failure signals).

### Pressable compact token
**Source:** `component/AppChip.kt:88-175`.
**Apply to:** per-item undo rows (`UndoRowItem`) and clarification chip options — do not hand-roll
a `Row`+`Surface`+`clickable`.

### Role-colored action button
**Source:** `component/DynamicActionButton.kt:37-58`.
**Apply to:** "Undo all (N)" button (role `Neutral`) — already the same component used for
Failure's existing action slot.

### Mutex-guarded suspend critical section
**Source:** `kotlinx.coroutines.sync.Mutex.withLock { }` — standard library, no new dependency
(`UndoHistoryStore.kt` already imports sibling `kotlinx.coroutines.*` symbols from the same
artifact).
**Apply to:** `attemptUndoGroup`'s claim→run→resolve section — explicitly NOT `tryConsume()`/
`AtomicBoolean` (one-way, cannot be released; see Pitfall 1).

### All-val presentational model
**Source:** `model/ApproachRungUiModel.kt`.
**Apply to:** `UndoAffordanceUiModel`, `UndoRowUiModel`, `UndoRefusedUiModel`,
`ClarificationOptionUiModel` — no logic, consumer builds these from store reads, library only
renders what it's given.

### `ComponentRegistry` + `DomainVocabularyDriftGuardTest` dual gate
**Source:** the already-landed `"Outcome"` precedent (`explorer/DomainVocabularyDriftGuardTest.kt:
326-329`) and `explorer/VoiceCommandFamilyScreen.kt:138-152`'s registry entry shape.
**Apply to:** `ClarificationBar` — register in the Voice Command family list AND add
`"Clarification"` to `PRIMITIVE_NOUN_ALLOWLIST` in the same commit.

## No Analog Found

None — every file has at least a role-match analog (see table above). The store-side grouping
mechanism itself (Mutex-based claim, group-aware eviction) is original synthesis against a
fully-specified frozen contract (marked `[ASSUMED]` in RESEARCH.md), not a codebase analog — the
planner should treat RESEARCH.md's Code Examples section as the design source for that file's
internals, and this PATTERNS.md's excerpts above as the copy-paste starting point.

## Metadata

**Analog search scope:** `feedback/`, `model/`, `component/`, `explorer/`, `src/test/` under
`src/main/java/io/github/ygaray/yahirandroidtaste/` and its test mirror — all per RESEARCH.md's
Sources (Primary, read in full this session): `UndoHistoryEntry.kt`, `UndoHistoryStore.kt`,
`UndoPreview.kt`, `VoiceOutcomeUiState.kt`, `HandledByUiModel.kt`, `FailureActionUiModel.kt`,
`BatchRowResultUiModel.kt`, `OutcomeSheet.kt`, `OutcomeSheetTest.kt`, `ComponentRegistry.kt`,
`VoiceCommandFamilyScreen.kt`, `DomainVocabularyDriftGuardTest.kt`, `TagChipUiModel.kt`,
`AppChip.kt`, `ApproachLadderCard.kt`, `SegmentedOptionSelector.kt`, `SheetScaffold.kt`,
`DynamicActionButton.kt`, `AttentionCue.kt`, `theme/Color.kt`, `ApproachRungUiModel.kt`,
`KeyFieldState.kt`.
**Files scanned:** 21 (all confirmed git-tracked source; no gitignored mirror paths used).
**Pattern extraction date:** 2026-09-30
