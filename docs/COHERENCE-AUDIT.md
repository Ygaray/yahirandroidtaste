# Coherence Audit — yahirandroidtaste

This audit surfaces and dispositions overlap, near-duplicate-sibling, and altitude-mismatch
findings across the hub's 9 registered `ComponentRegistry` families (AUD-01). Every finding
raised below carries an explicit disposition — **unify**, **keep-with-rationale**, or **prune** —
with cited rationale. The final "Unify Work-Order" section aggregates every "unify" disposition
into the 1:1 work-order Phase 5 (Gardening) consumes.

## Scope & Method

- **Tier source (D-01):** every component's `PRIMITIVE`/`PATTERN` tier is consumed verbatim from
  Phase 1's ratified `ComponentRegistry.Entry.tier` values (one per entry, in each family's own
  `*FamilyScreen.kt` file) — never re-derived ad hoc. `docs/DESIGN-INTENT.md` states the two
  contracts and the decidable two-question D-03 litmus this audit cites (not re-pastes) whenever
  an altitude-mismatch candidate is evaluated.
- **Blast radius (D-02):** every "unify" finding below carries a read-only consumer blast-radius
  grep (`grep -rl "<ComponentName>" ~/Projects/SecondBrain/app/src ~/Projects/CalTracker_Android/app/src`)
  recorded as a per-repo file count. This grep is a **single-name lower bound** — a consumer could
  alias an import, so a zero count is a floor, not proof of zero usage.
- **Consumer pin skew:** CalTracker_Android is pinned to hub tag `v1.5.0` while SecondBrain is
  pinned to `v1.10.0` (a newer, later on-disk snapshot). When a "unify" finding's blast-radius grep
  returns zero CalTracker hits, this audit checks that component's family-screen file for a
  phase-provenance comment (e.g. Tactile Foundation entries carry "Phase 123"-style notes) and
  records whether the component simply postdates CalTracker's `v1.5.0` pin — so a zero-hit
  CalTracker count reads correctly as "too new for that pin," not "genuinely unused."

### Cards

_(PENDING - filled by a later task)_

### Chips

_(PENDING - filled by a later task)_

### Sheets

_(PENDING - filled by a later task)_

### Buttons / FAB

_(PENDING - filled by a later task)_

### Pickers

_(PENDING - filled by a later task)_

### Feedback

_(PENDING - filled by a later task)_

### Empty State

_(PENDING - filled by a later task)_

### Progress / Metrics

_(PENDING - filled by a later task)_

### Tactile Foundation

_(PENDING - filled by a later task)_

### Unify Work-Order

_(PENDING - filled by a later task)_
