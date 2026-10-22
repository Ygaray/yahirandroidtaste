---
status: complete
phase: 28-talkback-sweep-explorer-demos
source: [28-01-SUMMARY.md, 28-02-SUMMARY.md, 28-03-SUMMARY.md, 28-04-SUMMARY.md, 28-05-SUMMARY.md, 28-06-SUMMARY.md, 28-07-SUMMARY.md, 28-08-SUMMARY.md, 28-09-SUMMARY.md, 28-10-SUMMARY.md, 28-11-SUMMARY.md, 28-12-SUMMARY.md]
started: 2026-10-22T19:05:00Z
updated: 2026-10-22T19:05:08Z
---

## Current Test

[testing complete]

## Tests

### 1. Refused undo is spoken once when it appears
expected: Turn TalkBack on. Explorer → Voice Command → OutcomeSheet, "Success, loud undo-refused substate", press Show sheet several times. Each time you hear "Couldn't undo: Item changed since, Card deleted changed since" exactly once.
result: pass

### 2. Physical: ReceiptRow swipe order to Undo
expected: TalkBack on. ReceiptRow page, "ReceiptRow - trailing action": from the focused row summary, swipe right with one finger; the next stop is the Undo button. Swipe left; you return to the summary.
result: skipped
reason: "Yahir: skip the rest"

### 3. Physical: headings navigation in the Undo Center groups
expected: TalkBack on. UndoCenterScreen page, group-rows demo: use TalkBack's headings navigation (Headings reading control, swipe up/down, or the local context menu). It reaches "Batch edit" and "Earlier batch" as headings.
result: skipped
reason: "Yahir: skip the rest"

### 4. Physical: openable Undo Center row stop count
expected: TalkBack on. UndoCenterScreen go-to-item rows demo: swipe right through "Changed item A". Stops: the row first (message plus open label), then its Undo button; nothing is announced twice.
result: skipped
reason: "Yahir: skip the rest"

### 5. Physical: collapsed deck walk, English and German
expected: TalkBack on. FeedbackStackHost page, fire "Burst x4" and swipe right through the collapsed deck (English, then German). Order: the oldest strip, its Undo, then the front card; each named once, no silent stop, nothing spoken twice; the middle strip is not a stop.
result: skipped
reason: "Yahir: skip the rest"

### 6. Physical: refusal message by swipe, TalkBack alone
expected: TalkBack on and no other accessibility service. Voice Command → OutcomeSheet, "Success, loud undo-refused substate", Show sheet, then one-finger swipe until focus lands on the red "Couldn't undo" message: it is spoken once, with no role (not "button").
result: skipped
reason: "Yahir: skip the rest"

### 7. C-3 / C-4 rulings on the collapsed deck
expected: With TalkBack on the collapsed deck: the oldest card's Undo comes right after its own card (C-3, ruled keep), and the actions menu lists Dismiss twice, system plus custom (C-4, ruled keep, cosmetic). Accept both as they are, or say what to change in v2.8.1.
result: skipped
reason: "Yahir: skip the rest"

### 8. Amber warn tone everywhere, light and dark
expected: On the tester (Material You on), go through the Explorer pages in light and dark theme: the warn tone is the hub amber everywhere, never mauve or wallpaper-tinted.
result: skipped
reason: "Yahir: skip the rest"

### 9. Map shows real OpenStreetMap tiles
expected: Wi-Fi on. Explorer → Pickers → PlaceMapPicker: the map shows real OpenStreetMap tiles, not the "403 Blocked" placeholder tile.
result: skipped
reason: "Yahir: skip the rest"

## Summary

total: 9
passed: 1
issues: 0
pending: 0
skipped: 8
blocked: 0

## Gaps

[none yet]
