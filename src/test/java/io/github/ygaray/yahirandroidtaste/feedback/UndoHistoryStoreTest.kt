package io.github.ygaray.yahirandroidtaste.feedback

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

/**
 * RED scaffold (Phase 53 Wave 0, plan 53-01) — encodes the full [UndoHistoryStore] contract
 * BEFORE the store exists. This file is intentionally compile-RED: [UndoHistoryStore],
 * [UndoHistoryEntry], and [UndoStatus] are not yet created — plan 53-02 introduces them and
 * turns this suite GREEN. Do NOT stub the store to make this file compile in this plan.
 *
 * Contract under test (UNDO-01/02/03, CONTEXT.md D-08/D-10):
 *  - append() creates exactly one [UndoStatus.Available] entry per [FeedbackEvent.WithUndo]-shaped
 *    input and returns its id.
 *  - 50-entry cap with status-aware eviction: on insert past 50, evict the oldest spent
 *    (Undone/Failed) entry first; if none exist (all Available), evict the oldest Available.
 *  - attemptUndo is a CAS guard — two calls (sequential or concurrent) on the same id run the
 *    undo lambda exactly once (first-consumer-wins).
 *  - Best-effort undo: a throwing lambda marks the entry Failed and does not propagate; a
 *    non-throwing lambda marks it Undone.
 *  - clearSpent() removes only Undone/Failed entries, never touches Available ones, and is a
 *    no-op when there is nothing spent to remove (all-Available or empty).
 *  - entries are exposed newest-first, with stable ordering even when timestamps tie.
 */
class UndoHistoryStoreTest {

    // ---- (a) append: WithUndo-only, exactly one Available entry, returns its id ----

    @Test
    fun append_createsExactlyOneAvailableEntry_andReturnsItsId() = runTest {
        val store = UndoHistoryStore()

        val returnedId = store.append("Card deleted") { /* no-op undo */ }

        val entries = store.entries.value
        assertEquals(1, entries.size)
        val entry = entries.single()
        assertEquals(returnedId, entry.id)
        assertEquals("Card deleted", entry.message)
        assertEquals(UndoStatus.Available, entry.status)
    }

    // ---- (b) 50-cap, status-aware eviction ----

    @Test
    fun appendPast50_evictsOldestSpentEntryFirst() = runTest {
        val store = UndoHistoryStore()

        // Oldest entry is immediately spent (Undone) — must be evicted first even though
        // it was inserted before 49 still-Available entries.
        val oldestSpentId = store.append("spent-oldest") { /* succeeds */ }
        store.attemptUndo(oldestSpentId)

        val stillAvailableIds = (1..49).map { i -> store.append("available-$i") { } }

        assertEquals(50, store.entries.value.size)

        val overflowId = store.append("overflow") { }

        val entries = store.entries.value
        assertEquals(50, entries.size)
        assertFalse(
            "Oldest spent entry must be evicted first, even though it is not the oldest overall",
            entries.any { it.id == oldestSpentId }
        )
        assertTrue(entries.any { it.id == stillAvailableIds.first() })
        assertTrue(entries.any { it.id == overflowId })
    }

    @Test
    fun appendPast50_allAvailable_fallsBackToEvictingOldestAvailable() = runTest {
        val store = UndoHistoryStore()

        val ids = (1..50).map { i -> store.append("available-$i") { } }
        assertEquals(50, store.entries.value.size)

        val overflowId = store.append("overflow") { }

        val entries = store.entries.value
        assertEquals(50, entries.size)
        assertFalse(
            "With no spent entries, the oldest Available entry must be evicted to hold the cap",
            entries.any { it.id == ids.first() }
        )
        assertTrue(entries.any { it.id == ids.last() })
        assertTrue(entries.any { it.id == overflowId })
    }

    // ---- (c) sequential double-attempt idempotency (CAS guard, first-consumer-wins) ----

    @Test
    fun attemptUndo_calledTwiceSequentially_runsLambdaExactlyOnce() = runTest {
        val store = UndoHistoryStore()
        val callCount = AtomicInteger(0)
        val id = store.append("Tag removed") { callCount.incrementAndGet() }

        store.attemptUndo(id)
        store.attemptUndo(id) // second attempt must be a no-op — guard already tripped

        assertEquals(1, callCount.get())
        assertEquals(UndoStatus.Undone, store.entries.value.single { it.id == id }.status)
    }

    // ---- (d) concurrent double-attempt idempotency ----

    @Test
    fun attemptUndo_calledConcurrently_stillYieldsExactlyOneExecution() = runTest {
        val store = UndoHistoryStore()
        val callCount = AtomicInteger(0)
        val id = store.append("Move undone") { callCount.incrementAndGet() }

        coroutineScope {
            launch { store.attemptUndo(id) }
            launch { store.attemptUndo(id) }
        }

        assertEquals(1, callCount.get())
        assertEquals(UndoStatus.Undone, store.entries.value.single { it.id == id }.status)
    }

    // ---- (e) best-effort undo: throwing -> Failed (no propagation); succeeding -> Undone ----

    @Test
    fun attemptUndo_throwingLambda_marksFailed_andDoesNotPropagate() = runTest {
        val store = UndoHistoryStore()
        val id = store.append("Stale purge undo") { throw IllegalStateException("already purged") }

        // Must not throw out of attemptUndo — the app never crashes on a stale undo.
        store.attemptUndo(id)

        assertEquals(UndoStatus.Failed, store.entries.value.single { it.id == id }.status)
    }

    @Test
    fun attemptUndo_succeedingLambda_marksUndone() = runTest {
        val store = UndoHistoryStore()
        val id = store.append("Card restored") { /* succeeds */ }

        store.attemptUndo(id)

        assertEquals(UndoStatus.Undone, store.entries.value.single { it.id == id }.status)
    }

    // ---- (e-cancel) CR-01 regression: cancellation must propagate, not be swallowed
    // as a Failed transition. A coroutine cancelled while undoAction() is suspended
    // (e.g. the hosting viewModelScope torn down mid-restore) must rethrow
    // CancellationException out of attemptUndo and must NOT flip the entry's status —
    // it must stay Available for a future attempt. ----

    @Test
    fun attemptUndo_cancelledWhileUndoActionSuspended_propagatesCancellation_andLeavesEntryUntouched() =
        runTest {
            val store = UndoHistoryStore()
            val undoActionStarted = CompletableDeferred<Unit>()
            val id = store.append("Card deleted") {
                undoActionStarted.complete(Unit)
                awaitCancellation()
            }

            var caughtCancellation = false
            val job = launch {
                try {
                    store.attemptUndo(id)
                } catch (e: CancellationException) {
                    caughtCancellation = true
                    throw e
                }
            }

            undoActionStarted.await()
            job.cancelAndJoin()

            assertTrue(
                "CancellationException must propagate out of attemptUndo, not be swallowed",
                caughtCancellation
            )
            assertEquals(
                "A cancelled attemptUndo must leave the entry Available (no Failed transition, " +
                    "no post-cancellation _entries.update)",
                UndoStatus.Available,
                store.entries.value.single { it.id == id }.status
            )
        }

    // ---- (e2) gap-closure regression (53-05 SC#3): status transitions must emit a
    // value-DISTINCT list so an already-subscribed StateFlow collector (Compose) sees
    // the change. Fails against the old in-place-mutation + `.toList()` implementation,
    // because a structurally-equal list is conflated by MutableStateFlow. ----

    @Test
    fun attemptUndo_emitsValueDistinctState_soAnAlreadySubscribedCollectorObservesTheChange() = runTest {
        val store = UndoHistoryStore()
        val id = store.append("Card deleted") { /* succeeds */ }

        val beforeState = store.entries.value
        store.attemptUndo(id)
        val afterState = store.entries.value

        assertTrue(
            "attemptUndo() must produce a list that is NOT equal to the pre-undo list " +
                "(same size, but the entry's status must differ) — otherwise " +
                "MutableStateFlow conflates the emission and Compose never recomposes " +
                "(53-05 SC#3 root cause: in-place mutation on a non-value-equal entry class)",
            beforeState != afterState
        )
        assertEquals(UndoStatus.Undone, afterState.single { it.id == id }.status)
    }

    @Test
    fun attemptUndo_failedTransition_emitsValueDistinctState() = runTest {
        val store = UndoHistoryStore()
        val id = store.append("Stale purge undo") { throw IllegalStateException("already purged") }

        val beforeState = store.entries.value
        store.attemptUndo(id)
        val afterState = store.entries.value

        assertTrue(
            "A Failed transition must also emit a value-distinct list (53-05 SC#3)",
            beforeState != afterState
        )
        assertEquals(UndoStatus.Failed, afterState.single { it.id == id }.status)
    }

    @Test
    fun clearSpent_emitsValueDistinctState_whenSomethingIsActuallyRemoved() = runTest {
        val store = UndoHistoryStore()
        val undoneId = store.append("undone-entry") { }
        store.attemptUndo(undoneId)
        val availableId = store.append("still-live") { }

        val beforeState = store.entries.value
        store.clearSpent()
        val afterState = store.entries.value

        assertTrue(
            "clearSpent() must emit a distinct (shorter) list when a spent entry is removed",
            beforeState != afterState
        )
        assertEquals(listOf(availableId), afterState.map { it.id })
    }

    @Test
    fun attemptUndo_preservesConsumedGuardAcrossReplacement_secondAttemptStaysNoOp() = runTest {
        val store = UndoHistoryStore()
        val callCount = AtomicInteger(0)
        val id = store.append("Card deleted") { callCount.incrementAndGet() }

        store.attemptUndo(id) // replaces the entry with a new, Undone-status instance
        store.attemptUndo(id) // must still be a no-op — the guard must have carried over

        assertEquals(
            "The consumed guard must survive the entry replacement — a second attemptUndo " +
                "on the replaced (Undone) entry must not re-run the undo lambda",
            1,
            callCount.get()
        )
        assertEquals(UndoStatus.Undone, store.entries.value.single { it.id == id }.status)
    }

    // ---- (e3) IN-01: the CAS-safety invariant is now compiler-enforced, not just a
    // convention — UndoHistoryEntry's primary constructor is `internal`, so
    // UndoHistoryStore.append() is the only way to mint a fresh, untripped-guard entry.
    // This test documents WHY that restriction exists: a freshly-constructed entry (even
    // one sharing an existing id) always starts with an untripped consumedGuard, which
    // would defeat first-consumer-wins if a caller could ever substitute one into the
    // store's list. ----

    @Test
    fun freshlyConstructedEntry_hasUntrippedGuard_evenWhenSharingAnExistingId() = runTest {
        val store = UndoHistoryStore()
        val id = store.append("Card deleted") { }
        store.attemptUndo(id) // trips the store's copy of the guard

        // Only reachable from within :designsystem — the primary constructor is `internal`
        // precisely so production code in other modules cannot do this.
        val freshEntrySharingTheId = UndoHistoryEntry(
            id = id,
            message = "Card deleted",
            timestamp = System.currentTimeMillis(),
            undoAction = { }
        )

        assertTrue(
            "A freshly-constructed entry must start with an untripped guard, demonstrating " +
                "why the store (not direct construction) must be the sole entry point for " +
                "anything wired into UndoHistoryStore's dedup logic",
            freshEntrySharingTheId.tryConsume()
        )
        assertEquals(
            "The store's own tracked entry must remain Undone — the fresh, never-appended " +
                "instance above has no bearing on the store's actual state",
            UndoStatus.Undone,
            store.entries.value.single { it.id == id }.status
        )
    }

    // ---- (f) scoped clear: only Undone/Failed removed; Available untouched; no-op cases ----

    @Test
    fun clearSpent_removesOnlyUndoneAndFailed_leavesAvailableIntact() = runTest {
        val store = UndoHistoryStore()

        val undoneId = store.append("undone-entry") { }
        store.attemptUndo(undoneId)

        val failedId = store.append("failed-entry") { throw RuntimeException("stale") }
        store.attemptUndo(failedId)

        val availableId = store.append("still-live") { }

        store.clearSpent()

        val entries = store.entries.value
        assertEquals(1, entries.size)
        assertEquals(availableId, entries.single().id)
        assertEquals(UndoStatus.Available, entries.single().status)
    }

    @Test
    fun clearSpent_isNoOp_whenAllEntriesAreAvailableOrListIsEmpty() = runTest {
        val emptyStore = UndoHistoryStore()
        emptyStore.clearSpent()
        assertTrue(emptyStore.entries.value.isEmpty())

        val store = UndoHistoryStore()
        val availableIds = (1..3).map { i -> store.append("live-$i") { } }

        store.clearSpent()

        val entries = store.entries.value
        assertEquals(3, entries.size)
        availableIds.forEach { id -> assertTrue(entries.any { it.id == id }) }
    }

    // ---- (g) newest-first exposure, stable on tied timestamps ----

    @Test
    fun entries_areExposedNewestFirst_withStableOrderOnTiedTimestamps() = runTest {
        val store = UndoHistoryStore()

        val firstId = store.append("first") { }
        val secondId = store.append("second") { }
        val thirdId = store.append("third") { }

        val entries = store.entries.value
        assertNotNull(entries)
        assertEquals(
            "Most recently appended entry must be first regardless of tied timestamps",
            thirdId,
            entries.first().id
        )
        assertEquals(
            "Least recently appended entry must be last",
            firstId,
            entries.last().id
        )
        assertEquals(listOf(thirdId, secondId, firstId), entries.map { it.id })
    }

    // ---- (h) UNDO-04 (Phase 58, D-03): preview carries through append and survives
    // status transitions — locks the equality-safe threading of UndoHistoryEntry.preview. ----

    @Test
    fun append_withNonNullPreview_entryCarriesTheSamePreview() = runTest {
        val store = UndoHistoryStore()
        val preview = UndoPreview.Tag("Work")

        val id = store.append("Tag deleted", preview = preview) { /* no-op undo */ }

        val entry = store.entries.value.single { it.id == id }
        assertEquals(preview, entry.preview)
    }

    @Test
    fun attemptUndo_preservesPreviewAcrossTheStatusTransition() = runTest {
        val store = UndoHistoryStore()
        val preview = UndoPreview.Card(title = "Note", cardType = "TEXT")
        val id = store.append("Card deleted", preview = preview) { /* succeeds */ }

        store.attemptUndo(id)

        val entry = store.entries.value.single { it.id == id }
        assertEquals(UndoStatus.Undone, entry.status)
        assertEquals(
            "The preview must survive the Available -> Undone withStatus/copy() transition",
            preview,
            entry.preview
        )
    }

    @Test
    fun append_withPreviewOmitted_entryHasNullPreview() = runTest {
        val store = UndoHistoryStore()

        val id = store.append("Card deleted") { /* no-op undo */ }

        val entry = store.entries.value.single { it.id == id }
        assertEquals(null, entry.preview)
    }

    // ---- (i) VUNDO-01 / D-01 grouping API happy path (Phase 11 Plan 01 Task 1) ----

    @Test
    fun groupedAppend_thenAttemptUndoGroup_undoesAllOnceAndResolvesTheGroup() = runTest {
        val store = UndoHistoryStore()
        val groupId = "group-1"
        val undoAllCallCount = AtomicInteger(0)

        store.openGroup(groupId, label = "Undo all (2)") { undoAllCallCount.incrementAndGet() }
        val firstId = store.append("Card deleted", groupId = groupId) { }
        val secondId = store.append("Tag removed", groupId = groupId) { }

        assertEquals(UndoGroupStatus.Undoable, store.groupStatus(groupId))
        assertEquals(2, store.group(groupId).size)
        assertEquals(groupId, store.groupIdOf(firstId))
        assertEquals(groupId, store.groupIdOf(secondId))
        assertEquals("Undo all (2)", store.groupLabel(groupId))

        val result = store.attemptUndoGroup(groupId)

        assertEquals(UndoGroupResult.Undone(count = 2), result)
        assertEquals(
            "undoAll must run exactly once for the whole group, never once per member",
            1,
            undoAllCallCount.get()
        )
        assertEquals(UndoGroupStatus.FullyResolved, store.groupStatus(groupId))
        assertTrue(store.group(groupId).all { it.status == UndoStatus.Undone })
    }

    // ---- (j) VUNDO-01 edge hardening (Phase 11 Plan 01 Task 2) ----

    @Test
    fun attemptUndoGroup_refused_leavesEveryClaimedMemberAvailable_andARetriedCallCanSucceed() = runTest {
        val store = UndoHistoryStore()
        val groupId = "group-refused"
        var shouldRefuse = true
        val undoAllCallCount = AtomicInteger(0)

        store.openGroup(groupId, label = "Undo all (2)") {
            undoAllCallCount.incrementAndGet()
            if (shouldRefuse) {
                throw UndoGroupRefusedException(reason = "Item changed", changedItem = "card-1")
            }
        }
        store.append("Card deleted", groupId = groupId) { }
        store.append("Tag removed", groupId = groupId) { }

        val refusedResult = store.attemptUndoGroup(groupId)

        assertEquals(
            UndoGroupResult.Refused(reason = "Item changed", changedItem = "card-1"),
            refusedResult
        )
        assertTrue(
            "Refused must leave every claimed member Available -- never permanently stranded " +
                "(Pitfall 1 closure)",
            store.group(groupId).all { it.status == UndoStatus.Available }
        )

        shouldRefuse = false
        val retryResult = store.attemptUndoGroup(groupId)

        assertEquals(
            "A retried attemptUndoGroup on the same group must still be able to succeed, proving " +
                "the claim was genuinely released, not a stuck guard",
            UndoGroupResult.Undone(count = 2),
            retryResult
        )
        assertEquals(2, undoAllCallCount.get())
    }

    @Test
    fun attemptUndoGroup_undoAllThrowsAPlainException_marksEveryClaimedMemberFailed() = runTest {
        val store = UndoHistoryStore()
        val groupId = "group-failed"
        store.openGroup(groupId, label = "Undo all (2)") { throw IllegalStateException("boom") }
        val firstId = store.append("Card deleted", groupId = groupId) { }
        val secondId = store.append("Tag removed", groupId = groupId) { }

        val result = store.attemptUndoGroup(groupId)

        assertEquals(UndoGroupResult.Failed, result)
        assertEquals(UndoStatus.Failed, store.entries.value.single { it.id == firstId }.status)
        assertEquals(UndoStatus.Failed, store.entries.value.single { it.id == secondId }.status)
    }

    @Test
    fun attemptUndoGroup_unknownGroupId_orAllMembersAlreadySpent_returnsNothingToUndo_withoutInvokingUndoAll() =
        runTest {
            val store = UndoHistoryStore()

            val unknownResult = store.attemptUndoGroup("never-opened")
            assertEquals(UndoGroupResult.NothingToUndo, unknownResult)

            val groupId = "group-all-spent"
            val undoAllCallCount = AtomicInteger(0)
            store.openGroup(groupId, label = "Undo all (1)") { undoAllCallCount.incrementAndGet() }
            store.append("Card deleted", groupId = groupId) { }
            store.attemptUndoGroup(groupId) // resolves the group to FullyResolved
            undoAllCallCount.set(0)

            val allSpentResult = store.attemptUndoGroup(groupId)

            assertEquals(UndoGroupResult.NothingToUndo, allSpentResult)
            assertEquals(
                "undoAll must never be invoked when every member is already spent",
                0,
                undoAllCallCount.get()
            )
        }

    @Test
    fun clearSpent_neverRemovesAPartiallyResolvedGroupsSpentMembers_removesTheWholeGroupOnceFullyResolved() =
        runTest {
            val store = UndoHistoryStore()
            val groupId = "group-partial"
            store.openGroup(groupId, label = "Undo all (3)") { }
            val firstId = store.append("first", groupId = groupId) { }
            val secondId = store.append("second", groupId = groupId) { }
            val thirdId = store.append("third", groupId = groupId) { }

            // Undo the first two individually via the UNCHANGED per-item path -- the group is
            // PartiallyResolved (2 spent, 1 still Available).
            store.attemptUndo(firstId)
            store.attemptUndo(secondId)
            assertEquals(UndoGroupStatus.PartiallyResolved, store.groupStatus(groupId))

            store.clearSpent()

            assertEquals(
                "clearSpent() must NOT remove a PartiallyResolved group's spent members ahead of " +
                    "its still-Available member",
                3,
                store.group(groupId).size
            )

            // Resolve the third member too -> FullyResolved -> clearSpent now removes the group.
            store.attemptUndo(thirdId)
            assertEquals(UndoGroupStatus.FullyResolved, store.groupStatus(groupId))

            store.clearSpent()

            assertEquals(
                "clearSpent() must remove the WHOLE group in one call once it is FullyResolved",
                0,
                store.group(groupId).size
            )
        }

    @Test
    fun evictIfNeeded_whenTheEvictionTargetBelongsToAGroup_evictsEveryMemberOfThatGroupTogether() = runTest {
        val store = UndoHistoryStore()
        val groupId = "group-evict"
        store.openGroup(groupId, label = "Undo all (2)") { }

        val firstMemberId = store.append("first-member", groupId = groupId) { }
        val secondMemberId = store.append("second-member", groupId = groupId) { }
        store.attemptUndo(firstMemberId) // spends the oldest group member individually

        // Fill up to the 50-entry cap with ungrouped Available entries appended AFTER the group.
        repeat(48) { i -> store.append("filler-$i") { } }
        assertEquals(50, store.entries.value.size)

        val overflowId = store.append("overflow") { }

        val entries = store.entries.value
        assertEquals(
            "The <= 50 cap holds even though a 2-member group is evicted together in one pass",
            49,
            entries.size
        )
        assertFalse(
            "The spent group member (the chosen eviction target) must be evicted",
            entries.any { it.id == firstMemberId }
        )
        assertFalse(
            "The still-Available sibling in the SAME group must be evicted together, never stranded",
            entries.any { it.id == secondMemberId }
        )
        assertTrue(entries.any { it.id == overflowId })
    }
}
