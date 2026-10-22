---
status: complete
phase: 26-receipt-card-stacked-snackbar-deck
source: [26-01-SUMMARY.md, 26-02-SUMMARY.md, 26-03-SUMMARY.md, 26-04-SUMMARY.md, 26-05-SUMMARY.md, 26-06-SUMMARY.md]
started: 2026-10-22T18:45:00Z
updated: 2026-10-22T18:46:36Z
---

## Current Test

[testing complete]

## Tests

### 1. Blank peek strip over a back card's Undo fans the deck, undoes nothing
expected: In the Explorer, Feedback → FeedbackStackHost, push two or three Undo cards (or Burst x4). With a real finger, tap the thin strip of a back card right where its "Undo" word would be (the strip looks blank). The deck fans out and nothing is undone. Then tap Undo on a fanned card (it undoes) and on the front card (it undoes).
result: pass

### 2. Tap, hold and fan feel right on a real finger
expected: Tapping a peek strip fans the deck; holding a card lets you read it without it opening or timing out; after about 4 s with no touch the fan folds back. The pacing and the strip's tap target feel right, not twitchy or sluggish.
result: pass

### 3. Swipe-to-dismiss feels right
expected: Swipe a card left or right: it follows the finger with some resistance, fades as it goes, and a fling past about 72dp removes just that card (Dismissed); a short drag springs back. A swipe on the collapsed strip does nothing.
result: pass

### 4. TalkBack reads the deck
expected: With TalkBack on, swipe through a deck: cards read newest first, each as a button with its open label, one announcement per arriving card, and the Undo action is offered on the card. The two Dismiss entries in the actions menu (system plus custom) are expected. (The full stop-by-stop walk is Phase 28's step; here just confirm it reads sensibly.)
result: pass

### 5. Look calls: ghosted deepest card and the German column
expected: Judge two looks the tester flagged as UX, not defects: the faded text of the deepest card in the middle strip of a 3-card deck, and the narrow (~103dp) message column for long German at font size max. Accept them as they are, or say what you'd change.
result: pass

### 6. Card timers pause while a pointer is down or the deck is fanned and resume the remaining time, with the 800 ms floor, on an injectable clock
expected: Card timers pause while a pointer is down or the deck is fanned and resume the remaining time, with the 800 ms floor, on an injectable clock
result: pass
source: automated
coverage_id: 26-01-D1

### 7. Timeout values unchanged (Short 4 s, Long 10 s, Indefinite never) through the existing scaler; Phase 25 suites green and unedited
expected: Timeout values unchanged (Short 4 s, Long 10 s, Indefinite never) through the existing scaler; Phase 25 suites green and unedited
result: pass
source: automated
coverage_id: 26-01-D2

### 8. Five Wave 0 Compose assumptions pinned with recorded outcomes and fallbacks, including the exact 72dp fling wiring
expected: Five Wave 0 Compose assumptions pinned with recorded outcomes and fallbacks, including the exact 72dp fling wiring
result: pass
source: automated
coverage_id: 26-01-D3

### 9. No public API change
expected: No public API change
result: pass
source: automated
coverage_id: 26-01-D4

### 10. A body tap on a targeted, unspent card resolves Opened first and calls onOpenTarget exactly once (double tap included); a no-target card, a spent card, a peek tap, a long-hold release and a drag release never open
expected: A body tap on a targeted, unspent card resolves Opened first and calls onOpenTarget exactly once (double tap included); a no-target card, a spent card, a peek tap, a long-hold release and a drag release never open
result: pass
source: automated
coverage_id: 26-03-D1

### 11. Tapping a peek strip fans the collapsed deck without opening; every fanned card body opens and every Undo resolves Undone; the fan re-collapses after 4000 ms idle, restarts on activity, never collapses under a held finger and folds at one card
expected: Tapping a peek strip fans the collapsed deck without opening; every fanned card body opens and every Undo resolves Undone; the fan re-collapses after 4000 ms idle, restarts on activity, never collapses under a held finger and folds at one card
result: pass
source: automated
coverage_id: 26-03-D2

### 12. A press anywhere in the deck (body, strip, Undo) pauses every timer through the deck-wide observer and clears on release, cancellation, card resolution mid-press and host disposal
expected: A press anywhere in the deck (body, strip, Undo) pauses every timer through the deck-wide observer and clears on release, cancellation, card resolution mid-press and host disposal
result: pass
source: automated
coverage_id: 26-03-D3

### 13. TalkBack path is separate from the pointer path: Role.Button and onClick exist only when openable, always open (even on a collapsed back card) and the open label reaches the semantics verbatim
expected: TalkBack path is separate from the pointer path: Role.Button and onClick exist only when openable, always open (even on a collapsed back card) and the open label reaches the semantics verbatim
result: pass
source: automated
coverage_id: 26-03-D4

### 14. No public API change; source contracts pin one gesture owner, no clickable, a non-consuming observer, resolve-before-open and no logging
expected: No public API change; source contracts pin one gesture owner, no clickable, a non-consuming observer, resolve-before-open and no logging
result: pass
source: automated
coverage_id: 26-03-D5

### 15. A horizontal swipe, left or right, removes exactly the swiped card with Dismissed; the other cards stay and the target is never opened
expected: A horizontal swipe, left or right, removes exactly the swiped card with Dismissed; the other cards stay and the target is never opened
result: pass
source: automated
coverage_id: 26-04-D1

### 16. A swipe never undoes: the Undo lambda is not run and the tracked store entry stays Available
expected: A swipe never undoes: the Undo lambda is not run and the tracked store entry stays Available
result: pass
source: automated
coverage_id: 26-04-D2

### 17. 72dp threshold, fast flick, sub-threshold return, vertical drag and the fanned column scroll behave as specified; collapsed deck swipes the front card only, fanned deck every card
expected: 72dp threshold, fast flick, sub-threshold return, vertical drag and the fanned column scroll behave as specified; collapsed deck swipes the front card only, fanned deck every card
result: pass
source: automated
coverage_id: 26-04-D3

### 18. First frame cannot throw; an arrival mid-swipe, a racing timer and a second finger do not move the swipe to another card or resolve a card twice
expected: First frame cannot throw; an arrival mid-swipe, a racing timer and a second finger do not move the swipe to another card or resolve a card twice
result: pass
source: automated
coverage_id: 26-04-D4

### 19. No public API change
expected: No public API change
result: pass
source: automated
coverage_id: 26-04-D5

### 20. Each card body is one merged Role.Button node with the open label (message fallback), a Polite live region, the dismiss semantics action and custom actions [Undo, Dismiss]; Undo is a separate focusable child in the unmerged tree whose label is not in the card's merged text
expected: Each card body is one merged Role.Button node with the open label (message fallback), a Polite live region, the dismiss semantics action and custom actions [Undo, Dismiss]; Undo is a separate focusable child in the unmerged tree whose label is not in the card's merged text
result: pass
source: automated
coverage_id: 26-05-D1

### 21. Every accessibility entry resolves like touch: both Dismiss forms resolve Dismissed and never undo, the open action fires onOpenTarget once, spent and untargeted cards expose no open action or role
expected: Every accessibility entry resolves like touch: both Dismiss forms resolve Dismissed and never undo, the open action fires onOpenTarget once, spent and untargeted cards expose no open action or role
result: pass
source: automated
coverage_id: 26-05-D2

### 22. Cards read newest first with unique traversal indices, collapsed and fanned; the deck container is a traversal group; an empty deck exposes no card node
expected: Cards read newest first with unique traversal indices, collapsed and fanned; the deck container is a traversal group; an empty deck exposes no card node
result: pass
source: automated
coverage_id: 26-05-D3

### 23. Nothing a card announces changes after arrival: content is identical across fan, collapse, a newer arrival and promotion to newest (D-07 live-region realization)
expected: Nothing a card announces changes after arrival: content is identical across fan, collapse, a newer arrival and promotion to newest (D-07 live-region realization)
result: pass
source: automated
coverage_id: 26-05-D4

### 24. The opaque FeedbackTarget.key never appears in the semantics tree; no consumer or domain import in the deck files
expected: The opaque FeedbackTarget.key never appears in the semantics tree; no consumer or domain import in the deck files
result: pass
source: automated
coverage_id: 26-05-D5

### 25. Explorer demo gains Burst x4, Short note and Long note with Undo; API.md row and host KDoc describe the finished deck; counts stay 67, 6, 73; api.txt byte-identical; additive check OK
expected: Explorer demo gains Burst x4, Short note and Long note with Undo; API.md row and host KDoc describe the finished deck; counts stay 67, 6, 73; api.txt byte-identical; additive check OK
result: pass
source: automated
coverage_id: 26-05-D6

### 26. A peek tap that lands on a collapsed back card's Undo fans the deck and never runs Undo (two-card deck)
expected: A peek tap that lands on a collapsed back card's Undo fans the deck and never runs Undo (two-card deck)
result: pass
source: automated
coverage_id: 26-06-D1

### 27. The same holds for the middle and the deepest card of a three-card deck and for the strip band above the Undo's bounds
expected: The same holds for the middle and the deepest card of a three-card deck and for the strip band above the Undo's bounds
result: pass
source: automated
coverage_id: 26-06-D2

### 28. The block is bound to the collapsed state: after the fan the same Undo works, and a collapsed front card's Undo works; a drag over the Undo neither fans nor undoes
expected: The block is bound to the collapsed state: after the fan the same Undo works, and a collapsed front card's Undo works; a drag over the Undo neither fans nor undoes
result: pass
source: automated
coverage_id: 26-06-D3

### 29. A11Y-01 untouched: the collapsed back card's Undo is enabled, clickable, a separate child and a custom action; semantic click and custom action resolve it Undone without fanning
expected: A11Y-01 untouched: the collapsed back card's Undo is enabled, clickable, a separate child and a custom action; semantic click and custom action resolve it Undone without fanning
result: pass
source: automated
coverage_id: 26-06-D4

## Summary

total: 29
passed: 29
issues: 0
pending: 0
skipped: 0
blocked: 0

## Gaps

[none yet]
