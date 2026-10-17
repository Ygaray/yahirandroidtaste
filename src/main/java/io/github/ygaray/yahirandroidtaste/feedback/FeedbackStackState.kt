package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.AccessibilityManager
import androidx.compose.ui.platform.LocalAccessibilityManager
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SHORT_MILLIS = 4_000L
private const val LONG_MILLIS = 10_000L
private const val DEFAULT_MAX_VISIBLE = 3

/**
 * Composition-free state of the feedback card stack (FSTK-01).
 *
 * [show] suspends until the card it enqueued resolves to **exactly one** [FeedbackResult], or
 * throws `CancellationException` if the caller is cancelled first (in which case the card is
 * removed, its slot is freed and no result is invented). Resolution is a single
 * claim per card (one winner chosen under a lock), so tap, timer, swipe, eviction and
 * [dismissAll] can race freely: the first one wins and every later resolver is told `false`.
 *
 * Behavior owned here:
 *  - at most [maxVisible] cards are held, newest first; a further arrival dismisses the oldest
 *    card early with [FeedbackResult.Dismissed];
 *  - each card's timer runs inside [show] on the caller's dispatcher and starts at enqueue
 *    (Short 4000 ms, Long 10000 ms, Indefinite never times out);
 *  - [dismissAll] resolves every visible card as [FeedbackResult.Dismissed].
 *
 * A [FeedbackEvent.DismissScreen] is shown as a plain message card; the stack sends no dismiss
 * signal, so the caller owns dismissing the screen.
 *
 * A plain `show(FeedbackEvent.WithUndo)` only reports [FeedbackResult.Undone]; it never runs the
 * event's undo lambda. Use the tracked path in `UndoTracking.kt` for store-backed undo.
 *
 * Do not hoist an instance into a ViewModel or a singleton: callers' coroutines are suspended on
 * it. With no host showing the cards, a non-Indefinite card still times out, but an Indefinite
 * event never returns until the caller is cancelled or [dismissAll] runs. An event enqueued
 * before any accessibility manager is attached keeps the unscaled timer, because the scaler is
 * read once, when the card's timer starts.
 *
 * @param maxVisible how many cards may be visible at once; a value below 1 reads back as 1
 *                   (the hub never throws on bad input).
 */
@Stable
class FeedbackStackState(maxVisible: Int = DEFAULT_MAX_VISIBLE) {

    /** The effective visible-card cap (at least 1). */
    val maxVisible: Int = maxVisible.coerceAtLeast(1)

    private val lock = Any()
    private var nextId = 0L

    /** Visible cards, newest first. */
    internal val items = mutableStateListOf<FeedbackStackItem>()

    /**
     * Optional timeout scaler taking the base millis and `containsControls`; null means unscaled.
     * Read once at enqueue.
     */
    @Volatile
    internal var timeoutScaler: ((Long, Boolean) -> Long)? = null

    /**
     * Enqueue [event] as a card and suspend until it resolves.
     *
     * @return exactly one [FeedbackResult]; a cancelled caller gets a `CancellationException`
     *         instead and no result.
     */
    suspend fun show(event: FeedbackEvent): FeedbackResult =
        showInternal(event, entryId = null, watch = null)

    /** Resolve every visible card as [FeedbackResult.Dismissed]. A no-op when empty. */
    fun dismissAll() {
        val snapshot = synchronized(lock) { items.toList() }
        snapshot.forEach { resolve(it, FeedbackResult.Dismissed) }
    }

    /**
     * Try to resolve [item] with [result]. Returns true only for the first resolver.
     *
     * The winner is chosen and the item is removed inside one critical section, so a resolved card
     * never counts toward the cap. The deferred is completed only after the lock is released: a
     * caller on an inline-resuming dispatcher (`Dispatchers.Main.immediate`, `Unconfined`) may run
     * its continuation, and call [show] again, from inside `complete`, and by then the card is
     * already gone.
     */
    internal fun resolve(item: FeedbackStackItem, result: FeedbackResult): Boolean {
        val won = synchronized(lock) {
            if (item.outcome == null) {
                item.outcome = result
                items.remove(item)
                true
            } else {
                false
            }
        }
        if (won) item.result.complete(result)
        return won
    }

    internal suspend fun showInternal(
        event: FeedbackEvent,
        entryId: String?,
        watch: (suspend (FeedbackStackItem) -> Unit)?,
        onEnqueued: ((FeedbackStackItem) -> Unit)? = null
    ): FeedbackResult = coroutineScope {
        val item = enqueue(event, entryId)
        onEnqueued?.invoke(item)
        var timer: Job? = null
        var watcher: Job? = null
        try {
            // Everything after enqueue lives inside the try, so a throwing step below still
            // reaches the finally and the card is removed instead of leaking a slot.
            timer = item.baseMillis?.let { base ->
                val millis = scaledMillis(base, item.hasControls)
                launch {
                    delay(millis)
                    resolve(item, FeedbackResult.TimedOut)
                }
            }
            watcher = watch?.let { launch { it(item) } }
            item.result.await()
        } finally {
            timer?.cancel()
            watcher?.cancel()
            // No-op unless the caller was cancelled (or a step above threw): then it completes
            // the deferred and removes the card.
            resolve(item, FeedbackResult.Dismissed)
        }
    }

    /**
     * The timer length for [base]. A scaler that throws (it wraps a platform call) falls back to
     * the unscaled timer rather than failing the whole [show].
     */
    private fun scaledMillis(base: Long, hasControls: Boolean): Long {
        val scaler = timeoutScaler ?: return base
        return runCatching { scaler(base, hasControls) }.getOrNull() ?: base
    }

    private fun enqueue(event: FeedbackEvent, entryId: String?): FeedbackStackItem {
        val evicted = mutableListOf<FeedbackStackItem>()
        val item = synchronized(lock) {
            val created = FeedbackStackItem.from(event, nextId++, entryId)
            items.add(0, created)
            while (items.size > maxVisible) {
                val oldest = items.removeAt(items.lastIndex)
                oldest.outcome = FeedbackResult.Dismissed
                evicted.add(oldest)
            }
            created
        }
        // Completed outside the lock for the same reason as in [resolve].
        evicted.forEach { it.result.complete(FeedbackResult.Dismissed) }
        return item
    }

    private val accessibilityManagers = mutableListOf<AccessibilityManager>()

    /**
     * Bind [manager] so card timers are scaled by its recommended timeout. Bindings are
     * reference-counted by manager identity: a host and `rememberFeedbackStackState` can both bind
     * the same manager and neither clears the other. The scaler is read once, when a card's timer
     * starts, so cards enqueued earlier keep their timer.
     */
    internal fun attachAccessibility(manager: AccessibilityManager) {
        synchronized(lock) {
            accessibilityManagers.add(manager)
            refreshScaler()
        }
    }

    /**
     * Release one binding of [manager] (a no-op when it was never attached). When the last
     * binding goes, [timeoutScaler] is cleared so the state keeps no manager reference.
     */
    internal fun detachAccessibility(manager: AccessibilityManager) {
        synchronized(lock) {
            val index = accessibilityManagers.indexOfFirst { it === manager }
            if (index >= 0) accessibilityManagers.removeAt(index)
            refreshScaler()
        }
    }

    private fun refreshScaler() {
        val current = accessibilityManagers.lastOrNull()
        val scaler: ((Long, Boolean) -> Long)? = current?.let { manager ->
            { base, containsControls ->
                manager.calculateRecommendedTimeoutMillis(
                    base,
                    containsIcons = true,
                    containsText = true,
                    containsControls = containsControls
                )
            }
        }
        timeoutScaler = scaler
    }
}

/**
 * One card in the stack. Holds no undo lambda by design: only the tracked path performs undo.
 */
@Stable
internal class FeedbackStackItem(
    val id: Long,
    val message: String,
    val supportingText: String?,
    val target: FeedbackTarget?,
    val hasUndo: Boolean,
    val baseMillis: Long?,
    val entryId: String?
) {
    val result = CompletableDeferred<FeedbackResult>()

    /**
     * The result that won resolution, set (under the state's lock) before [result] completes.
     * Non-null means the card is claimed and already off the stack, even if a dispatcher has not
     * yet resumed the caller awaiting [result].
     */
    @Volatile
    var outcome: FeedbackResult? = null

    /** Snapshot state so a host recomposes when the backing undo entry is spent elsewhere. */
    var spent by mutableStateOf(false)

    /** Whether the card shows controls, which the accessibility scaler needs to know. */
    val hasControls: Boolean get() = hasUndo || target != null

    companion object {
        fun from(event: FeedbackEvent, id: Long, entryId: String?): FeedbackStackItem =
            when (event) {
                is FeedbackEvent.Notify -> FeedbackStackItem(
                    id = id,
                    message = event.message,
                    supportingText = event.supportingText,
                    target = event.target,
                    hasUndo = false,
                    baseMillis = event.duration.toBaseMillis(),
                    entryId = entryId
                )
                is FeedbackEvent.WithUndo -> FeedbackStackItem(
                    id = id,
                    message = event.message,
                    supportingText = event.supportingText,
                    target = event.target,
                    hasUndo = true,
                    baseMillis = event.duration.toBaseMillis(),
                    entryId = entryId
                )
                is FeedbackEvent.DismissScreen -> FeedbackStackItem(
                    id = id,
                    message = event.message,
                    supportingText = null,
                    target = null,
                    hasUndo = false,
                    baseMillis = SHORT_MILLIS,
                    entryId = entryId
                )
            }
    }
}

private fun SnackbarDuration.toBaseMillis(): Long? = when (this) {
    SnackbarDuration.Short -> SHORT_MILLIS
    SnackbarDuration.Long -> LONG_MILLIS
    SnackbarDuration.Indefinite -> null
}

/**
 * Remember a [FeedbackStackState] across recomposition and bind the composition's accessibility
 * manager to it, so card timers honour the user's recommended timeout (FSTK-01).
 *
 * Treat [maxVisible] as constant: changing it creates a new state and drops the old cards. Do not
 * hoist the result into a ViewModel or a singleton, because callers' coroutines are suspended on
 * it. With no [FeedbackStackHost] showing the cards, a non-Indefinite card still times out, but an
 * Indefinite card never resolves until its caller is cancelled or `dismissAll` runs.
 *
 * @param maxVisible how many cards may be visible at once; a value below 1 reads back as 1.
 */
@Composable
fun rememberFeedbackStackState(maxVisible: Int = DEFAULT_MAX_VISIBLE): FeedbackStackState {
    val state = remember(maxVisible) { FeedbackStackState(maxVisible) }
    state.BindAccessibilityTimeout()
    return state
}

/**
 * Bind the current `LocalAccessibilityManager` to this state while the caller is in composition
 * (D-06). Internal on purpose: the registry guard only scans public composables. Bindings are
 * reference-counted by the state, so [FeedbackStackHost] and [rememberFeedbackStackState] can both
 * call this and neither clears the other.
 */
@Composable
internal fun FeedbackStackState.BindAccessibilityTimeout() {
    val manager = LocalAccessibilityManager.current
    DisposableEffect(this, manager) {
        if (manager != null) attachAccessibility(manager)
        onDispose {
            if (manager != null) detachAccessibility(manager)
        }
    }
}
