---
phase: 09-ship-coordinated-repin
reviewed: 2026-09-27T00:00:00Z
depth: standard
files_reviewed: 1
files_reviewed_list:
  - ECOSYSTEM.md
findings:
  critical: 0
  warning: 1
  info: 1
  total: 2
status: issues_found
---

# Phase 9 (Plan 02): Code Review Report — commits `5d3555b`, `c793ab8`, `a3d4e3f`

**Reviewed:** 2026-09-27
**Depth:** standard (diff read in full + independent external re-verification, not just static reading)
**Files Reviewed:** 1 (`ECOSYSTEM.md` — the only source/doc file this plan touched)
**Status:** issues_found (one low-severity wording-ambiguity WARNING, one INFO nit; no BLOCKER)

## Summary

Reviewed the full `5310b9a..HEAD` diff (`git diff --stat` confirms only `ECOSYSTEM.md` plus
planning-bookkeeping files changed — no `SecondBrain`/`CalTracker_Android` file touched, so the
"hub phase performs neither bump" claim holds mechanically, not just by assertion). I did not just
read the diff — I independently re-verified every falsifiable claim the new prose makes:

- **Tag/commit-SHA consistency:** `git rev-list -n1 v2.2.0` → `5310b9a14ad675a9511d301aa69898047d04d4b4`,
  exactly matching the SHA the new tag-cut paragraph cites twice (`ECOSYSTEM.md:428-429,444`) and
  the commit the plan's precondition names. No drift between prose and reality.
- **Live JitPack re-check (today, independent of the original Task 1 run):** `pom` → HTTP 200;
  `aar` → HTTP 200, `Content-Length: 1618685` — byte-identical to the doc's claimed
  "`Content-Length: 1618685` bytes" (`ECOSYSTEM.md:440`); `builds-API` →
  `{"status":"ok","commit":"5310b9a14ad675a9511d301aa69898047d04d4b4","isTag":true}` — matches the
  doc's claims verbatim (`ECOSYSTEM.md:443-445`). This is externally falsifiable evidence and it
  checks out exactly.
- **Machine-matrix-vs-prose consistency:** matrix (`ECOSYSTEM.md:45-50`) shows
  `SecondBrain: v1.13.0 → v2.2.0 (behind)` and `CalTracker_Android: v2.1.0 → v2.2.0 (behind)`.
  Section 1's hand-authored table (`ECOSYSTEM.md:33-34`) now says SecondBrain pins `v1.13.0` and
  CalTracker pins `v2.1.0` — an exact match on both cells, both column-count-preserved (6 pipes /
  row, unchanged from the pre-existing 5-column table — no markdown table breakage).
- **Matrix-marker integrity:** diffed exactly the text between `<!-- repin-matrix:begin -->` and
  `<!-- repin-matrix:end -->` (`ECOSYSTEM.md:45-50`) — only the two data rows' `Latest`/`Status`
  cells changed (`v2.1.0→v2.2.0`/`current→behind` for CalTracker, `v2.1.0→v2.2.0` Latest for
  SecondBrain), which is exactly what a `reconcile --refresh` run against a newly-pushed tag
  produces. No header row, no column, no surrounding prose was touched inside the markers — the
  hand-edit boundary the plan's own T-09-05 threat entry calls out was respected.
- **Backtick/bold-span balance:** scanned every added line for unbalanced inline-code backticks
  (none found) and unbalanced `**bold**` markers (several "odd" per-line counts, but all are
  intentional bold spans wrapping a soft line-break mid-sentence — the same long-paragraph,
  line-wrapped-prose convention already used throughout this file pre-diff, e.g. the untouched
  Phase-43/Phase-48 paragraphs below). No broken markdown was introduced.
- **Requirement-ID accuracy:** the new v2.2.0 tag-cut record's `REUNI-01`..`REUNI-04`,
  `TAGCOLOR-01`, `MICBTN-01`..`MICBTN-03`, `SHIP-01` all cross-check against
  `.planning/REQUIREMENTS.md`'s `[x]` completed list and traceability table — correct IDs, correct
  phase attribution, no fabricated or misattributed requirement.
- **Cross-reference direction:** the Section 1 table's "see §'Version-numbering / branch-topology
  deviation...' below" (`ECOSYSTEM.md:33`) and the new Pending-repins bullet's "the
  `v1.10.0`→`v1.11.0` fork note below" (`ECOSYSTEM.md:62`) both correctly point *forward* — that
  section actually lives at line 387, genuinely below both referencing points. No backwards/stale
  section pointer.
- **Verify-gate re-check:** all four of Task 2's literal grep checks re-run clean against the
  current file (`Current published tag` count `0`; `Pending repins` count `1`; `v2.2.0` count `16`
  ≥ 4; `v1.13.0` count `4` ≥ 2) — the SUMMARY's claimed pass state is real, not asserted.

This is a narrowly-scoped, purely-additive/corrective documentation change with no code touched,
no consumer repo touched, and every checkable factual claim independently confirmed accurate. The
two findings below are wording-quality nits, not correctness defects.

## Narrative Findings (AI reviewer)

### WR-01: "Pending repins" bullets end on an ambiguous past-participle fragment that reads like a completed-action claim

**File:** `ECOSYSTEM.md:65,68`
**Issue:** Both bullets in the new "Pending repins (post-v2.2.0)" subsection end with a standalone
sentence fragment: "Executed in SecondBrain's own repo/channel." and "Executed in CalTracker's own
repo/channel." Read in isolation (e.g. skimmed out of context, or quoted back to the owner without
the intro paragraph above), "Executed in X's own repo/channel." reads as a **past-tense completion
claim** ("this was done, in X's repo") rather than the intended **location/ownership qualifier**
("this is to be executed there, not here"). The section's opening sentence ("This hub phase...
performs **neither bump**... Each repin executes in the consumer's own repo/channel") does
disambiguate it in full context, and no consumer file was actually touched (confirmed via
`git diff --stat`), so this is not a factual error — but it is exactly the kind of phrasing the
review brief flagged as a misleading-an-owner risk, and it's an easy fix.
**Fix:** Make the qualifier present/future-tense and attach it to the clause rather than standing
alone, e.g.:
```markdown
- **SecondBrain (`v1.13.0` → `v2.2.0`):** bump the hub coordinate ... (to be done in
  SecondBrain's own repo/channel — not performed by this hub phase).
- **CalTracker (`v2.1.0` → `v2.2.0`):** bump the same coordinate ... (to be done in
  CalTracker's own repo/channel — not performed by this hub phase).
```

### IN-01: Inherited dangling-preposition phrasing carried forward unchanged by the tense rewrite

**File:** `ECOSYSTEM.md:33`
**Issue:** "...a breaking change SecondBrain's `BrowseScreen.kt` depends on directly and had not
yet migrated for at that time." The trailing "migrated for at that time" is a dangling-preposition
construction ("migrated for" with no object) that already existed pre-diff (previously "has not
yet migrated for") — this plan's tense correction (`has`→`had`, appending `at that time`) preserved
the awkward phrasing rather than tightening it, since D-03's scope was tense-correctness, not prose
polish. Not a factual defect; purely a readability nit noticed while re-reading the touched
sentence.
**Fix (optional, not required by this plan's scope):** "...and had not yet migrated to it at that
time" or "...and had not yet completed that migration at that time."

---

_Reviewed: 2026-09-27_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
