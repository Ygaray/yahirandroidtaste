---
phase: 09-ship-coordinated-repin
plan: 02
subsystem: infra
tags: [jitpack, repin, ecosystem-docs, ship]

requires:
  - phase: 09-ship-coordinated-repin (Plan 01)
    provides: the human-approved, pushed, immutable v2.2.0 annotated tag at commit 5310b9a
provides:
  - JitPack-resolution evidence proving com.github.Ygaray:yahirandroidtaste:v2.2.0 is a real,
    non-cached build (pom+aar HTTP 200, builds-API status=ok/isTag=true)
  - Machine-reconciled repin-matrix block showing v2.2.0 as Latest for both consumers
  - ECOSYSTEM.md Section 1 narrative corrected -- no stale "current tag" claim remains
  - A new v2.2.0 tag-cut record in the established per-tag format
  - "Pending repins (post-v2.2.0)" subsection naming each consumer's exact bump path
affects: []

actuals:
  tokens: 2576
  tasks: 2
  commits: 2

tech-stack:
  added: []
  patterns:
    - "JitPack lazy-build verification: request pom+aar directly, cross-check the builds-API JSON (status/isTag/commit), tolerate a lazy-build 404 with bounded retry rather than treating one slow response as failure"
    - "repin_status.py tags.json cache has a 1h TTL; a tag pushed within that window requires --refresh on reconcile/status/validate or the matrix silently reconciles against stale tags"

key-files:
  created: []
  modified:
    - ECOSYSTEM.md

key-decisions:
  - "Used --refresh on the reconcile invocation (not in the plan's literal action text) after discovering the tags.json cache (TTL 3600s) was fetched ~54min before v2.2.0's push and therefore lacked it -- the first reconcile attempt returned 'no drift' against stale v2.1.0-latest data, which would have silently failed Task 1's own matrix verify check. This is a Rule 3 (blocking-issue) auto-fix: the plan's literal command as written could not satisfy the plan's own verify block against the actual cache state at execution time."
  - "Kept the historical fork rationale in Section 1's SecondBrain table cell intact (per the plan's explicit instruction) while updating the bolded pin to v1.13.0 and noting the pin has since moved further -- distinguishes the point-in-time historical record from the current-truth pointer to the machine matrix."

patterns-established:
  - "Point-in-time framing for superseded 'X pins Y' narrative sentences: state what was true when the paragraph's own tag shipped, then point at the machine-reconciled matrix above as the authoritative current-pin source, rather than let the narrative claim present-tense truth it can no longer make."

requirements-completed: [SHIP-01, SHIP-02]

coverage:
  - id: D1
    description: "v2.2.0 confirmed to resolve via a real, non-cached JitPack build (pom+aar HTTP 200, builds-API status=ok, isTag=true)"
    requirement: "SHIP-01"
    verification:
      - kind: other
        ref: "curl https://jitpack.io/com/github/Ygaray/yahirandroidtaste/v2.2.0/yahirandroidtaste-v2.2.0.pom (200), .../yahirandroidtaste-v2.2.0.aar (200, 1618685 bytes), https://jitpack.io/api/builds/com.github.Ygaray/yahirandroidtaste/v2.2.0 (status=ok, isTag=true, commit=5310b9a14ad675a9511d301aa69898047d04d4b4)"
        status: pass
    human_judgment: false
  - id: D2
    description: "ECOSYSTEM.md's machine-reconciled matrix shows v2.2.0 as Latest for both consumers; Section 1 narrative no longer claims a stale tag is current and carries a new v2.2.0 tag-cut record"
    requirement: "SHIP-02"
    verification:
      - kind: other
        ref: "grep -cE '\\| (CalTracker_Android|SecondBrain) \\| [^|]+\\| v2\\.2\\.0 \\|' ECOSYSTEM.md == 2; grep -c 'Current published tag' ECOSYSTEM.md == 0; grep -c 'v2.2.0' ECOSYSTEM.md >= 4; grep -c 'v1.13.0' ECOSYSTEM.md >= 2"
        status: pass
    human_judgment: false
  - id: D3
    description: "A 'Pending repins' section names SecondBrain's and CalTracker's exact bump paths; no consumer repo file touched by this plan"
    requirement: "SHIP-02"
    verification:
      - kind: other
        ref: "grep -c 'Pending repins' ECOSYSTEM.md == 1; git diff --stat 5310b9a..HEAD shows only ECOSYSTEM.md changed (no SecondBrain/CalTracker path touched)"
        status: pass
    human_judgment: false

duration: 15min
completed: 2026-09-27
status: complete
---

# Phase 9 Plan 02: JitPack Verify + ECOSYSTEM.md Reconcile Summary

**Confirmed `v2.2.0` is a real, non-cached JitPack build on the first request, reconciled the machine repin matrix (after forcing a cache refresh past a stale 1h TTL window), and hand-corrected ECOSYSTEM.md's stale "current tag" narrative with a new v2.2.0 tag-cut record and a "Pending repins" section for both consumers -- closing W-1 fully per D-03.**

## Performance

- **Duration:** ~15 min
- **Started:** 2026-09-27
- **Completed:** 2026-09-27
- **Tasks:** 2 of 2
- **Files modified:** 1 (ECOSYSTEM.md, across 2 commits)

## Accomplishments
- Confirmed the plan's precondition (`v2.2.0` exists, immutable, pushed to origin) via `repin_status.py tag-status`.
- Verified JitPack actually resolves `com.github.Ygaray:yahirandroidtaste:v2.2.0`: pom HTTP 200, aar HTTP 200 (1,618,685 bytes), builds-API `status=ok`/`isTag=true`/`commit=5310b9a14ad675a9511d301aa69898047d04d4b4` -- resolved on the very first request (no lazy-build wait needed).
- Reconciled the `<!-- repin-matrix -->` block: both `CalTracker_Android` and `SecondBrain` now show `v2.2.0` as Latest (both `behind`, since neither has repinned yet).
- Hand-corrected ECOSYSTEM.md's stale Section 1 narrative in three places: the `v1.10.0` paragraph's false "Current published tag" opening, the doubly-stale "SecondBrain pins.../CalTracker pins..." present-tense claim (rephrased to point-in-time + pointer to the machine matrix), and the Section 1 consumer table's stale SecondBrain pin cell (`v1.11.0` -> `v1.13.0`, historical rationale preserved).
- Added a new `v2.2.0` tag-cut record in the established per-tag format, naming the requirement IDs it carries (REUNI-01..04, TAGCOLOR-01, MICBTN-01..03, SHIP-01) and Task 1's JitPack resolution evidence.
- Added a "Pending repins (post-v2.2.0)" subsection naming each consumer's exact bump path, explicitly stating this hub phase performs neither bump.

## Task Commits

1. **Task 1: Verify v2.2.0 resolves via JitPack, then reconcile the machine repin matrix** - `5d3555b` (feat)
2. **Task 2: Hand-correct the stale narrative, record the v2.2.0 cut, surface each consumer's repin path (D-03, W-1)** - `c793ab8` (docs)

**Plan metadata:** this summary + STATE/ROADMAP updates follow in the next commit.

## Files Created/Modified
- `ECOSYSTEM.md` - machine repin-matrix reconciled to `v2.2.0`; Section 1 narrative corrected (no stale "current tag" claim); Section 1 SecondBrain table cell updated to `v1.13.0`; new `v2.2.0` tag-cut record added; new "Pending repins (post-v2.2.0)" subsection added

## Decisions Made
- Added `--refresh` to the `reconcile` invocation, deviating from the plan's literal action text, because the on-disk `tags.json` cache (1h TTL) was fetched before `v2.2.0` was pushed and the first `reconcile` run returned "no drift" against stale `v2.1.0`-latest data -- silently failing to satisfy Task 1's own `grep -cE ... v2\.2\.0` verify check. Confirmed via the cache file's timestamp (age ~3228s, TTL 3600s) and its cached tag list ending at `v2.1.0`. This is a Rule 3 blocking-issue auto-fix: proceeding without `--refresh` would have left the plan's own verify block failing.
- Kept the historical fork rationale in the Section 1 SecondBrain table cell intact per the plan's explicit instruction, updating only the bolded pin and adding a "moved further" note plus a pointer to the new "Pending repins" subsection.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Added `--refresh` to the reconcile invocation to bypass a stale 1h tag cache**
- **Found during:** Task 1 (reconcile step)
- **Issue:** `python3 ~/.claude/context/deps/repin_status.py reconcile --hub yahirandroidtaste --hubs-root ~/Projects/Reusable/android` (the plan's literal command) returned `no drift` and left the matrix showing `v2.1.0` as Latest, because the script's on-disk tag cache (`~/.cache/repin-status/tags.json`, TTL 3600s) had been populated ~54 minutes earlier -- before `v2.2.0` was pushed -- and had not yet expired.
- **Fix:** Re-ran with `--refresh` (a flag the script's own `reconcile` subparser already exposes), forcing a fresh `git ls-remote --tags`. The matrix then correctly reconciled to `v2.2.0` as Latest for both consumers.
- **Files modified:** ECOSYSTEM.md (matrix block only)
- **Verification:** Post-fix `grep -cE '\| (CalTracker_Android|SecondBrain) \| [^|]+\| v2\.2\.0 \|' ECOSYSTEM.md` returns `2`; a subsequent non-`--refresh` `reconcile` run now correctly reports `no drift` against the now-current cache.
- **Committed in:** `5d3555b` (Task 1 commit)

---

**Total deviations:** 1 auto-fixed (1 blocking-issue).
**Impact on plan:** Necessary for the plan's own Task 1 verify block to pass against real-world cache timing; no scope creep -- same script, same flag family, just forcing freshness.

## Issues Encountered
None beyond the cache-staleness issue documented above. Both plan-level `<verify>` blocks and all four Task 2 grep checks passed on the corrected attempt.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- SHIP-01's JitPack-resolvable clause and SHIP-02 are both closed; W-1 is cleared fully per D-03 (machine block + stale prose both fixed).
- Both consumer repins remain outstanding and are surfaced in ECOSYSTEM.md's new "Pending repins (post-v2.2.0)" subsection for the owner to action in each consumer's own repo/channel (cross-repo-hub convention -- this plan touched no consumer file).
- No blockers for closing Phase 9 or this milestone from the hub side.

---
*Phase: 09-ship-coordinated-repin*
*Completed: 2026-09-27*

## Self-Check: PASSED
- FOUND: ECOSYSTEM.md
- FOUND: .planning/phases/09-ship-coordinated-repin/09-02-SUMMARY.md
- FOUND: 5d3555b
- FOUND: c793ab8
