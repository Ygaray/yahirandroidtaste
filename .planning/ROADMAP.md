# Roadmap: yahirandroidtaste — Hub Stewardship

## Completed Milestones

- ✅ **v1.0 — Hub Stewardship** (shipped 2026-09-02, library `v2.0.0`): the hub went from a flat,
  additive-only catalog to a legible, audited, governed two-tier design system — Tier Legibility →
  Coherence Audit → Governance Gates → Repin Bookkeeping → Gardening (unify shipped; coordinated
  consumer repin deferred to a downstream human-gated phase). Full detail:
  [`.planning/milestones/v1.0-ROADMAP.md`](milestones/v1.0-ROADMAP.md).

## Active Milestone

_None. Start the next milestone with `/gsd-new-milestone` (it defines fresh requirements + roadmap)._

Known downstream follow-ons carried out of v1.0:
- **GARD-02 coordinated repin** — cut nothing new; repin SecondBrain (single-hop) + CalTracker
  (two-hop catch-up) onto `v2.0.0`, each Gate-1 re-verified, then `repin_status.py reconcile`
  (also clears W-1, the stale ECOSYSTEM.md matrix). Human-gated per CLAUDE.md.

## Backlog

### Phase 999.1: Formalize reusable Gate-2 visualization harness APK (BACKLOG)

**Goal:** Promote the throwaway same-package-Intent harness — which every Gate-1 agent currently re-derives from scratch — into a committed, launchable Gate-2 visualization app for this library-only repo.

**Requirements:** TBD

**Plans:** 0 plans

Context:
- `yahirandroidtaste` is a pure `com.android.library` (no `applicationId`), so it ships **no installable APK**. Human Gate-2 on-device review therefore has nothing to open. Every Gate-1 self-UAT run rebuilds the same throwaway harness (a 1-Activity app that depends on the mavenLocal AAR and `startActivity(Intent(this, ExplorerActivity::class.java))`) just to see the gallery — documented in `01-05-SELF-UAT.md`'s "Driver-mechanism note" and re-derived by this milestone's verify session too.
- Deliverables to scope when promoted: (a) a committed harness — a dedicated app module or a gradle task that assembles an installable debug APK opening `ExplorerActivity`; (b) a project-local `AGENT-DEVICE-TESTING.md` documenting the same-package-harness driver pattern (the SELF-UAT logs explicitly recommend authoring one so future Gate-1 runs don't re-derive it).
- **Invariant guard:** harness → library only, never the reverse (one-way dependency). The harness is host/consumer-side tooling; it must name no library-internal concepts and must not become something the library depends on.

Plans:
- [ ] TBD (promote with /gsd-review-backlog when ready)
