# STYLE — tried and reverted

Read before editing `STYLE.md`. Newest first. One entry per idea: what was tried, why it went, where
it's recorded. "SB" is SecondBrain and "CT" is CalTracker. Their refs are paths inside those repos'
`.planning/`.

- **2026-10-14 — Tall sheets opening at the partial detent.** The pinned Confirm landed under the nav
  bar. Now the hub opens them fully expanded. → YAT v2.7.1, `.planning/cross-repo/HANDOFF.md`
- **2026-10-14 — Leaving dynamic color on in hue-coded demos.** Warn turned mauve. Pin the hub scheme
  instead. → YAT `.planning/HUMAN-UAT-PENDING.md`, backlog 999.3
- **2026-10-12 — Two 50% weight slots for the Receipt action row.** A short Cancel squeezed a long
  German Confirm. Replaced by a custom layout. → YAT `22-REVIEW-FIX.md`
- **2026-10-12 — 32dp under the Success actions.** Too loose; now 16dp. → YAT `22-REVIEW-FIX.md`
- **2026-10-11 — An app-side frame or extra rows to get the Receipt look.** Rejected: "recall the
  YAT is ours so it should evolve with us." The hub grows an opt-in style instead, and there are no app-side
  stopgaps. → CT `76-UI-SPEC.md`, `76-CONTEXT.md`
- **2026-10-11 — Expanded-by-default item lists.** "condense this into a digestible feed." Replaced by
  collapsed receipt rows with the editor below, never beside, the row. → CT `72-UAT.md`
- **2026-10-11 — An AmountEditor with a separate "Update" step.** Replaced by a write-through editor. → CT
  `76-UI-SPEC.md`
- **2026-10-11 — A count line for failed batch items.** The owner chose named failed rows plus Retry, for
  fidelity to the mockup. → CT `76-DISCUSSION-LOG.md`
- **2026-10 — Generic network errors.** "could not reach AI, check your network" is not specific.
  Replaced by named causes, each with its own fix. → CT `76-UAT.md`
- **2026-10 — Swapping between separate sheet windows.** It flashed a full-screen blank. The fix is to change the
  content in place and slide down. SB's clarification→confirm swap still blinks. → CT `76-UAT.md`, SB
  `181-05-SELF-UAT.md`
- **2026-10 — Ad-hoc UI with no mockups.** "too many ui mistakes". Now mockups are approved first and Gate-1 checks
  against them. → CT `75-UAT.md`
- **2026-10 — A modal sheet for a voice answer over search results.** Too heavy; he wants a lighter
  snackbar or toast over the results. → SB `176-UAT.md`
- **2026-10 — "This can't be undone." next to a working Undo all.** Marked FIX NOW. → SB `178-UAT.md`
- **2026-10 — A bare tier word on the sheet.** It "reads as a stray word". Replaced by the handled-by
  pill's `prefixLabel`. → SB `175-UI-SPEC.md`
- **2026-10 — A generic "Cancel" decline label.** Replaced by verb + noun. → SB `181-UI-SPEC.md`
- **2026-09 — Raising `maxLines` to fix a long M3 `SegmentedButton` label.** It doesn't help: M3 measures
  the label at full width. Use the Pill style or a custom layout. → YAT `RETROSPECTIVE.md` G-15-1
