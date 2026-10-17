# Requirements: yahirandroidtaste — milestone v2.8

**Defined:** 2026-10-17
**Core Value:** An agent building one of Yahir's apps finds the widget it needs already here, coherent and legible, and drops it in instead of writing a new one.
**Source:** `.planning/research/v2.8-BRIEF.md` (decisions D-01..D-12, locked by Yahir 2026-10-17) and the options page `docs/design/snackbar-go-to-item/index.html`.

## v2.8 Requirements

### Feedback target model

- [x] **FTGT-01**: A caller can attach a `FeedbackTarget(key, openLabel)` to `FeedbackEvent.Notify` and `FeedbackEvent.WithUndo` via a defaulted `target` param; existing call sites compile unchanged (D-08, D-09).
- [x] **FTGT-02**: A tracked `WithUndo` copies its target into the `UndoHistoryEntry` (`target`, defaulted), and an entry that is Undone no longer exposes an openable target (D-09, D-10).

### Stacked snackbar host (direction A)

- [x] **FSTK-01**: A caller can show feedback through `FeedbackStackState` / `rememberFeedbackStackState(maxVisible = 3)` and `FeedbackStackHost(state, onOpenTarget, ...)`; `show(event)` returns `Opened | Undone | Dismissed | TimedOut` (D-09).
- [x] **FSTK-02**: Up to 3 snackbars are visible as a Receipt-card cascading deck (surfaceContainerHighest, 16dp corners, tone dot, message + optional second line, Undo text action); a 4th arrival dismisses the oldest early (D-01, D-06).
- [x] **FSTK-03**: Tapping a snackbar body that has a target calls `onOpenTarget(target)` and dismisses it (`Opened`); a body with no target does nothing; a trailing chevron icon shows only when a target exists; there is no action label (D-02, D-03).
- [x] **FSTK-04**: Tapping the peek of a collapsed deck fans it out so every snackbar is reachable; it re-collapses after idle (D-01).
- [x] **FSTK-05**: A horizontal swipe dismisses exactly one snackbar (`Dismissed`) and never undoes (D-07).
- [x] **FSTK-06**: Each snackbar keeps today's timeout (Short 4 s / Long 10 s through the accessibility recommended-timeout API); timers pause while a pointer is down on a snackbar or the deck is fanned out, and resume afterwards (D-04, D-05).
- [x] **FSTK-07**: `FeedbackStackState.emitTrackedWithUndo(store, event)` appends to `UndoHistoryStore` before showing and undoes only through the store's guard; Undo and Dismiss labels are caller-localizable (`undoLabel`, `dismissLabel`); the existing `SnackbarHostState.emitTrackedWithUndo` keeps working (D-09).

### Open from the sheet and the Undo Center

- [ ] **FOPEN-01**: `VoiceOutcomeUiState.Success.target` makes the receipt row that names the item a label-less tap target with a chevron, in both the Receipt and Classic `OutcomeSheet` styles, via a defaulted `onOpenTarget` on both `OutcomeSheet` overloads (D-09, D-10).
- [ ] **FOPEN-02**: Each `BatchRowResultUiModel` with a `target` opens its own item; rows without one show no chevron and aren't clickable (D-10).
- [ ] **FOPEN-03**: An `UndoCenterScreen` overload with `onOpenTarget` lets Available rows with a target open their item (D-09).

### Accessibility

- [x] **A11Y-01**: Each snackbar body is one merged node with `Role.Button`, `onClickLabel = openLabel`, a polite live region, Undo as a separate focusable child plus a custom action, and a Dismiss custom action / `dismiss` semantics; deck items stay in the tree newest-first (D-11).
- [ ] **A11Y-02**: TalkBack sweep (999.2, scoped): a clickable ReceiptRow reads as one button with its expanded/collapsed state and Undo as a separate stop; the refused-undo "Couldn't undo" assertive live region is spoken; the saved-key summary reads as one item with Update/Clear as separate buttons; the Undo Center group header is a heading; the new sheet row targets announce their open label.

### Explorer and polish

- [ ] **DEMO-02**: The whole Explorer renders in the pinned hub color scheme (999.3), and a hoisted `OutcomeSheet` `sheetState` demo calls `hide()`.
- [ ] **DEMO-03**: The Explorer shows the stacked snackbar host (fire/burst/swipe/open demo) and the sheet go-to-item rows, in English and German; new public composables are registered in `ComponentRegistry`.
- [ ] **POL-01**: The handled-by pill keeps its text inside its shape when it wraps at font scale 2.0 (corner radius capped when multi-line, or equivalent).
- [ ] **POL-02**: The Grammar (warn) handled-by dot stays clearly visible on the pill under dynamic color (contrast-safe dot).

### Ship

- [ ] **SHIP-05**: Docs (API.md, STYLE.md, INTEGRATION/CATALOG as needed) describe the new API; `apiCheck` shows additions only; tests + detekt green; long-string (DE/ES ~360dp, font scale 2.0) and Gate-1 device checks pass; the `v2.8.0` tag + push are proposed to Yahir (not cut by the agent).

## Future Requirements

- **POL-xx** Voice-surface polish 999.4 (pinned-bar gap, saved-summary exit, long group label, key field in flight, group count, count-editor inset, German subtitle) — v2.8.1.
- Harness APK 999.1.

## Out of Scope

| Feature | Reason |
|---------|--------|
| Combined "Undo & open" for soft-deleted items | Deleted items have no target in v2.8 (D-12) |
| App-side routing / current-destination guard | The consumer's `onOpenTarget` owns it; the library can't know the route |
| Share-sheet / MCP / notification deep links | No snackbar host on those surfaces; separate design |
| SecondBrain sheet-swap blink | App-side (two sheet windows) |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| FTGT-01 | Phase 25 | Complete |
| FTGT-02 | Phase 25 | Complete |
| FSTK-01 | Phase 25 | Complete |
| FSTK-07 | Phase 25 | Complete |
| FSTK-02 | Phase 26 | Complete |
| FSTK-03 | Phase 26 | Complete |
| FSTK-04 | Phase 26 | Complete |
| FSTK-05 | Phase 26 | Complete |
| FSTK-06 | Phase 26 | Complete |
| A11Y-01 | Phase 26 | Complete |
| FOPEN-01 | Phase 27 | Pending |
| FOPEN-02 | Phase 27 | Pending |
| FOPEN-03 | Phase 27 | Pending |
| POL-01 | Phase 27 | Pending |
| POL-02 | Phase 27 | Pending |
| A11Y-02 | Phase 28 | Pending |
| DEMO-02 | Phase 28 | Pending |
| DEMO-03 | Phase 28 | Pending |
| SHIP-05 | Phase 29 | Pending |

**Coverage:**

- v2.8 requirements: 19 total
- Mapped to phases: 19 (Phases 25-29)
- Unmapped: 0

---
*Requirements defined: 2026-10-17*
*Last updated: 2026-10-17 after the v2.8 roadmap (traceability filled)*
