# Style — yahirandroidtaste

> Editing this file? Read `STYLE.REVERTED.md` first (rejected ideas, and why).

The look and feel Yahir has accepted, written as rules with the shipped code that proves them. It is
drawn from the v2.6 Receipt atoms and the Explorer release demos, the v2.7.x Receipt `OutcomeSheet`, and the CalTracker and
SecondBrain voice sheets built on them. `DESIGN-INTENT.md` says which tier a component belongs to;
this file says how anything in the hub should look and behave. Paths are under
`src/main/java/io/github/ygaray/yahirandroidtaste/`.

## 1. Surfaces and color

- **Do** build sheets on `surfaceContainerLow`, and draw cards, pills and rows on `surfaceContainerHighest`, with
  `outlineVariant` for 1dp dividers. **Don't** use `surfaceVariant`. (`component/OutcomeSheetReceipt.kt`
  `ReceiptHandledByPill`)
- **Do** keep `primary` rare: the Success badge, a status dot, the one Filled action. **Don't** color text
  with it. One visual anchor per sheet; the pill is always tertiary.
- **Do** take tones from `ToneDefaults` / `ToneColors` (`theme/ToneTokens.kt`). Red is only for errors
  (`errorContainer` + `error`). Warn (`tertiaryContainer`) is only for policy or tier notes. An app
  passes its own warn hue through `OutcomeSheetColors`; the hub's tokens never change for one app.
- **Do** pin the hub scheme where a hue carries meaning: under dynamic color, warn turns mauve
  (`explorer/ReleaseDemos.kt` `HubSchemeScope`).

## 2. Spacing and shape

- **Do** use the 4dp grid and the `Dimens` tokens (`theme/Dimens.kt`): 16dp side inset everywhere
  (`HorizontalPadding`), 12dp row padding (`ReceiptRow.VerticalPadding`), 48dp touch targets. Only
  1dp dividers and 6dp dots or bars are off-grid.
- **Do** use 24dp top corners on Receipt sheets (`Dimens.OutcomeReceipt.CornerRadius`), not M3's 28.
  Cards use 16dp, fields 12dp; chips, pills and buttons are fully round.
- **Do** give every demo and sheet body the 16dp inset, and keep content clear of the nav bar. **Don't**
  let a card sit flush on an edge (the v2.7.1 count-editor fix).
- **Do** split rows with a divider. **Don't** wrap each row in its own card.

## 3. Type

- Weights 400 and 600 only. Sheet title: 18/24sp SemiBold, max 2 lines, heading semantics
  (`OutcomeSheetDefaults` `ReceiptTitleStyle`). Row title: `titleMedium` SemiBold. Body: `bodyMedium`. Pill and
  captions: `bodySmall`. Transcript: italic, `onSurfaceVariant`.
- **Do** set `LineHeightStyle(trim = None)` on multi-line labels (`component/ReceiptRow.kt`).

## 4. Sheets

- **Do** open tall sheets fully expanded, with the action bar pinned above the nav bar. A sheet state
  the hub creates uses `skipPartiallyExpanded = true`. A caller that hoists its own passes it too
  (`component/OutcomeSheet.kt`, both overloads).
- **Do** pin the bar: a 1dp divider, the text action at the start, the Filled action at the end. No
  action means no bar and no divider. Lay out the two actions so a short Cancel never squeezes a long
  German Confirm (`OutcomeSheetReceipt.kt` `ReceiptActionRow`).
- **Do** use verb + noun labels ("Keep card", "Delete card", "Retry command"). **Don't** use a generic
  "Cancel". **Don't** put any copy on screen that contradicts an action ("This can't be undone." next to
  Undo all).
- **Do** show a summary first, with collapsed `ReceiptRow`s that expand to edit. **Don't** expand lists
  by default.
- **Do** say what handled the request (the handled-by pill) on both the confirm sheet and the Success sheet, and
  never name a tier that didn't run. **Don't** show a bare tier word.
- **Do** swap state inside one sheet window (one `OutcomeSheet` call site) and slide down on every
  close. **Don't** swap between separate sheet windows: it flashes a bare screen for about 110ms.
- Timers (owned by the app's sheet host): Success closes after 4000ms, Undo all and the answer after
  10000ms, both scaled by accessibility settings. Failure stays until dismissed.

## 5. Feedback and errors

- **Do** make failures loud: the error tone, a named cause, and one fix action. **Don't** say "could not
  reach AI, check your network". **Don't** use `AttentionCue` as a failure signal.
- **Do** keep a status badge decorative, and make the text state the status ("Reverted: 2 entries").
- **Do** dim undone rows; never remove them. Undo appears in the place it applies (a row's TextSmall
  Undo, the sheet's Tonal "Undo all (N)"). The Undo Center holds the rest.
- Snackbars: one `FeedbackEvent` per change, and `emitTrackedWithUndo` for undo, so the snackbar and
  the Undo Center share one guard (`feedback/UndoTracking.kt`).

## 6. States and motion

- Disabled is `0.38f` alpha, applied once (`PillChip.DISABLED_CONTENT_ALPHA`). Never dim an already
  dimmed part again.
- `inFlight` locks every action and every editable field except one marked `activeWhileInFlight`
  (`model/SheetActionUiModel.kt`).
- An empty slot (`null`) renders no node and leaves no space.
- Motion comes from the `theme/Motion.kt` tokens or the 200ms row expand. **Don't** hand-roll a
  `tween` in app code.

## 7. Before you sign off

- **Long strings:** check every caller-overridable label in long German or Spanish at about 360dp and
  at font scale 2.0. English defaults hide overflow, and accessibility dumps show text the screen
  truncates, so only screenshots count.
- **TalkBack:** every new tappable gets a role and a click label. The v2.6 TalkBack gap is open as
  backlog 999.2.
- **Default calls stay pixel-identical:** a new look is an opt-in param or style (`OutcomeSheetStyle`),
  never a change to an existing default.
- **Mockups first:** two or three options on a tailnet page, then build only the one Yahir picks.
