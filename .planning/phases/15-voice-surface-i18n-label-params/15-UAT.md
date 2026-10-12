---
status: complete
phase: 15-voice-surface-i18n-label-params
source: [15-01-SUMMARY.md, 15-02-SUMMARY.md, 15-03-SUMMARY.md]
started: 2026-10-12T00:01:11Z
updated: 2026-10-12T01:24:46Z
---

## Current Test

[testing complete]

## Tests

### 1. Long localized labels render without breaking layout
expected: On personal, open the YAT v2.5 Gate-2 app, tap "P15: LONG German labels". Every overridden label shows the German text (no English leak) and nothing is clipped mid-word, overlapping, or pushing controls off-screen: ProviderKeyCard/ModelSelectCard dropdown labels (Anbieter für Spracherkennung / Sprachmodell für Befehlsverarbeitung), ClarificationBar dismiss (Verwerfen und schließen), ApproachLadderCard rung-state labels + Onlineverarbeitung / Ausschließlich offline toggle; then Open sheet A (Eskalationen an höhere Stufe: 1, Rückgängig gemacht, Konnte nicht rückgängig gemacht werden: ... wurde seitdem geändert). Graceful wrap or ellipsis is fine.
result: issue
reported: "there are bugs in the ui, you can see one of the side by side texts is all crammed"
severity: major
resolved_by_retest: 15 (pass, 2026-10-11; gap G-15-1 resolved by 15-04 + review fixes)

### 2. ProviderKeyCard renders a caller-supplied providerLabel on the provider dropdown field; default stays 'Provider'
expected: ProviderKeyCard renders a caller-supplied providerLabel on the provider dropdown field; default stays 'Provider'
result: pass
source: automated
coverage_id: 15-01 D1

### 3. ModelSelectCard renders a caller-supplied modelLabel on the model dropdown field; default stays 'Model'
expected: ModelSelectCard renders a caller-supplied modelLabel on the model dropdown field; default stays 'Model'
result: pass
source: automated
coverage_id: 15-01 D2

### 4. ClarificationBar renders a caller-supplied dismissLabel on the dismiss button; default stays 'Dismiss'; tap still emits onDismiss
expected: ClarificationBar renders a caller-supplied dismissLabel on the dismiss button; default stays 'Dismiss'; tap still emits onDismiss
result: pass
source: automated
coverage_id: 15-01 D3

### 5. ApproachLadderCard renders caller-supplied rung-state (unavailable/capped/needs-network) and toggle (online/offline-only) labels; each defaults to today's English
expected: ApproachLadderCard renders caller-supplied rung-state (unavailable/capped/needs-network) and toggle (online/offline-only) labels; each defaults to today's English
result: pass
source: automated
coverage_id: 15-01 D4

### 6. All new params appended last and defaulted; Metalava apiCheck green; api.txt regenerated with exactly the re-signatured lines; detekt zero-baseline green
expected: All new params appended last and defaulted; Metalava apiCheck green; api.txt regenerated with exactly the re-signatured lines; detekt zero-baseline green
result: pass
source: automated
coverage_id: 15-01 D5

### 7. An Undone row in OutcomeSheet renders caller-supplied UndoRowUiModel.undoneLabel; default stays 'Undone'; row stays non-clickable
expected: An Undone row in OutcomeSheet renders caller-supplied UndoRowUiModel.undoneLabel; default stays 'Undone'; row stays non-clickable
result: pass
source: automated
coverage_id: 15-02 D1

### 8. Refused surface renders '<refusedPrefix> <reason>' plus ', <changedItem> <changedSinceSuffix>' when changedItem non-null; English defaults byte-identical
expected: Refused surface renders '<refusedPrefix> <reason>' plus ', <changedItem> <changedSinceSuffix>' when changedItem non-null; English defaults byte-identical
result: pass
source: automated
coverage_id: 15-02 D2

### 9. Old constructor and copy JVM arities survive; legacy copy preserves custom labels; defaults pinned; new field participates in equality
expected: Old constructor and copy JVM arities survive; legacy copy preserves custom labels; defaults pinned; new field participates in equality
result: pass
source: automated
coverage_id: 15-02 D3

### 10. Metalava apiCheck green via the recipe; api.txt regenerated per commit with exactly one removed line; UndoRowState.Undone untouched; explorer/ and feedback/ unchanged vs v2.4.1
expected: Metalava apiCheck green via the recipe; api.txt regenerated per commit with exactly one removed line; UndoRowState.Undone untouched; explorer/ and feedback/ unchanged vs v2.4.1
result: pass
source: automated
coverage_id: 15-02 D4

### 11. HandledByUiModel.escalationsLabel renders '<label> <count>' in the handled-by caption; default 'Escalations: N' byte-identical; separator/order unchanged
expected: HandledByUiModel.escalationsLabel renders '<label> <count>' in the handled-by caption; default 'Escalations: N' byte-identical; separator/order unchanged
result: pass
source: automated
coverage_id: 15-03 D1

### 12. ProposedItemUiModel.removeContentDescription is the remove icon's content description; default 'Remove'; toString unchanged
expected: ProposedItemUiModel.removeContentDescription is the remove icon's content description; default 'Remove'; toString unchanged
result: pass
source: automated
coverage_id: 15-03 D2

### 13. Old constructor/copy arities survive for both models; legacy copy preserves custom labels (reflection pins 5/6 and 7/8)
expected: Old constructor/copy arities survive for both models; legacy copy preserves custom labels (reflection pins 5/6 and 7/8)
result: pass
source: automated
coverage_id: 15-03 D3

### 14. SC5 phase-wide: v2.4.0 call shapes (positional through former last param, old-arity ctor/copy, destructuring) compile and render English defaults; strictly additive vs released v2.4.1
expected: SC5 phase-wide: v2.4.0 call shapes (positional through former last param, old-arity ctor/copy, destructuring) compile and render English defaults; strictly additive vs released v2.4.1
result: pass
source: automated
coverage_id: 15-03 D4

### 15. Re-test G-15-1 — long German labels render legibly (post 15-04 + review fixes)
expected: Harness "P15: LONG German labels" + Explorer > Voice Command > ApproachLadderCard > "long localized labels (German)", light + dark: Cloud rung name fully visible; German affordances right-aligned, ≤2 lines each, no per-character wrap, no overlap with the name; "Onlineverarbeitung" / "Ausschließlich offline" wrap inside their segments without clipping, equal segment heights; OutcomeSheet German rows still render.
result: pass
source: gate2-retest (15-VERIFICATION human_verification #1)

### 16. English defaults look like v2.5.0
expected: In the Explorer gallery, the default-English ApproachLadderCard cells — especially "combined subdued labels" (Cloud with Unavailable + Capped + Needs network) — show all three English affordances on ONE line to the right of "Cloud", as in v2.5.0; row pitch and touch targets look unchanged.
result: pass
source: gate2 (15-VERIFICATION human_verification #2, CR-01)

### 17. Selected-segment check-icon margin with a near-full label
expected: With "Ausschließlich offline" selected (tap it) at phone width, the label stays inside the segment border and never runs under the check icon; the icon may sit close to the segment edge (accepted WR-03 trade-off, same as v2.5.0).
result: pass
source: gate2 (15-VERIFICATION human_verification #3, WR-03)

## Summary

total: 17
passed: 16
issues: 1
pending: 0
skipped: 0
blocked: 0

## Gaps

- gap_id: G-15-1
  truth: "ApproachLadderCard rung rows stay legible with long caller-supplied rung-state labels (rung name visible, affordance labels wrap/ellipsize gracefully, no per-character wrapping)"
  status: resolved
  reason: "User reported: there are bugs in the ui, you can see one of the side by side texts is all crammed"
  severity: major
  test: 1
  resolved_by: 15-04-PLAN.md (+ review fixes c877c9d..abdc4a8)
  resolved_at: 2026-10-11
  observed: "Cloud rung (disabled+capped+needs-network) with German labels: rung name squeezed to zero width, needs-network label wraps one character per line; also the offline segmented toggle clips \"Ausschließlich offline\" at the right edge"
  root_cause: "RungRow (ApproachLadderCard.kt ~L242-269) lays the rung name out with Modifier.weight(1f) and the unavailable/capped/needs-network affordance Texts unweighted with no maxLines. Compose Row measures unweighted children first, so long affordance labels consume the full width: the weighted rung name gets 0 width (disappears) and the last affordance gets the sliver left, wrapping per character. Latent since the affordances existed; reachable once Phase 15 made the labels caller-overridable. Separately, the Online/Offline (and Router) SegmentedOptionSelector segment labels are single-line and clip long text at the card edge."
  artifacts:
    - path: "src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt"
      issue: "RungRow affordance Texts unweighted/unbounded; rung name weight(1f) starves to 0 width"
    - path: "src/main/java/io/github/ygaray/yahirandroidtaste/component/SegmentedOptionSelector.kt"
      issue: "segment label clips long localized text instead of wrapping/ellipsizing"
  missing:
    - "Rung name keeps a guaranteed minimum share of the row; affordance labels share the remaining width (weight with fill=false / FlowRow / stack below the name) and wrap or ellipsize at word boundaries, never per character"
    - "Segmented toggle labels wrap (max 2 lines) or ellipsize instead of clipping"
    - "Regression test with long labels (e.g. the German set from the Gate-2 harness) asserting rung name is displayed with nonzero width and affordances render"
    - "Gallery/Explorer fixture with long localized labels so future Gate-2 can eyeball it"
  diagnosed_by: "orchestrator inline (screenshot p15s.png on personal + source read); no separate debug session"

## Deferred Diagnosis

Operator (2026-10-11): finish Gate-2 for phases 16-18 first, then diagnose and fix all gaps in one batch.
