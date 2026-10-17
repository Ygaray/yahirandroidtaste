package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.runtime.Immutable

/**
 * Opaque "go to the thing this feedback is about" target carried by a [FeedbackEvent].
 *
 * - [key] is an opaque, consumer-encoded identifier. The library never parses, logs or echoes it;
 *   the consumer decodes it in its own open callback.
 * - [openLabel] is localized text used as the TalkBack click label for the open action. It must
 *   be non-blank, by this rule only: there is no `init` or `require`, so construction never throws
 *   (a blank label makes the host fall back to the card message at render time).
 *
 * No lambda and no `Context` may ever be added to this type: the singleton [UndoHistoryStore]
 * retains targets beyond the screen that created them, so anything captured here would leak.
 * Equality is plain data-class value equality.
 */
@Immutable
data class FeedbackTarget(val key: String, val openLabel: String)
