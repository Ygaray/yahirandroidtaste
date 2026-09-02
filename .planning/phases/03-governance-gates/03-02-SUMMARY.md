---
phase: 03-governance-gates
plan: 02
subsystem: testing
tags: [junit, kotlin, compose, drift-guard, design-intent, source-text-scan]

# Dependency graph
requires:
  - phase: 01-tier-legibility
    provides: docs/DESIGN-INTENT.md (## The Litmus, ## Applying the Litmus) — extended here, not replaced
provides:
  - DomainVocabularyDriftGuardTest.kt — fail-until-allowlisted JUnit guard flagging any public
    top-level @Composable whose leading name token isn't an established UI-primitive noun or an
    acknowledged domain name
  - docs/DESIGN-INTENT.md ## The Tier-Aware Contribution Litmus + ## Enforcement sections
affects: [03-05 (any future phase touching ComponentRegistry-scanned source), gardening/repin phases that add new components]

# Actuals (#2632)
actuals:
  tokens: 5913
  tasks: 3
  commits: 2

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Fail-until-allowlisted source-text-scan drift guard, duplicated shape from ComponentRegistryDriftGuardTest (independent predicate/allowlist, never conflated with INTENTIONALLY_UNREGISTERED)"

key-files:
  created:
    - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt
  modified:
    - docs/DESIGN-INTENT.md

key-decisions:
  - "Widened PRIMITIVE_NOUN_ALLOWLIST beyond RESEARCH.md's 34-word trailing-word seed with 28 additional leading-word structural/generic UI descriptors (Accent, Adaptive, Animated, App, Attention, Bulk, Clearable, Confirmation, Count, Crop, Cycle, Dynamic, Elevation, Empty, Expandable, Filter, Gradient, Hero, Icon, List, Metric, Name, Progress, Segmented, Sort, Tactile, Text, Undo) — the seed list was derived from TRAILING words only, but the guard's predicate tests the LEADING word (head token); a first live run against the real corpus surfaced 31 false offenders that were all genuinely non-domain descriptors, not consumer nouns (Rule 1 auto-fix, required to meet the plan's own day-one-green acceptance criterion)."
  - "Used HUB_LANE_OVERRIDE=2 for both task commits — the pre-commit hook's reproduced GOV-03 false-flag (stale v1.10.0 baseline vs. cumulative working tree) blocks every commit today regardless of content; confirmed via `git diff --cached -U0 -- src/main` being empty for both commits. GOV-03's actual fix is plan 03-01's independent scope."

patterns-established:
  - "DOMAIN_VOCABULARY: Map<String, String> allowlist (name -> rationale) mirrors ComponentRegistry.INTENTIONALLY_UNREGISTERED's shape but answers an independent question — never conflate the two maps."

requirements-completed: [GOV-01, GOV-02]

coverage:
  - id: D1
    description: "DomainVocabularyDriftGuardTest — fail-until-allowlisted JUnit guard flags any public top-level @Composable whose head token is neither an established UI-primitive noun nor an acknowledged DOMAIN_VOCABULARY entry; day-one green against the live corpus; manually confirmed red->green by temporarily removing VoiceCard from the allowlist"
    requirement: "GOV-02"
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt#everyPublicComposableHeadTokenIsPrimitiveOrAcknowledgedDomainVocabulary"
        status: pass
      - kind: manual_procedural
        ref: "manual red->green demo: temporarily removed VoiceCard from DOMAIN_VOCABULARY, confirmed AssertionError listing exactly [VoiceCard], restored entry, confirmed green again"
        status: pass
    human_judgment: false
  - id: D2
    description: "docs/DESIGN-INTENT.md extended with '## The Tier-Aware Contribution Litmus' (asymmetric strict/loose gate per tier) and '## Enforcement' (accurately scopes mechanical enforcement to the strict-primitives half only, naming DomainVocabularyDriftGuardTest by file path)"
    requirement: "GOV-01"
    verification:
      - kind: other
        ref: "grep -c '^## ' docs/DESIGN-INTENT.md -> 7 (up from 5)"
        status: pass
    human_judgment: false

duration: 10min
completed: 2026-09-02
status: complete
---

# Phase 3 Plan 02: Domain-Vocabulary Drift Guard + Tier-Aware Litmus Doc Summary

**Fail-until-allowlisted JUnit source-text-scan guard (`DomainVocabularyDriftGuardTest`) flagging any public composable whose leading name token isn't a hub UI-primitive noun or an acknowledged domain name, paired with `docs/DESIGN-INTENT.md`'s new tier-aware contribution litmus + accurately-scoped enforcement sections.**

## Performance

- **Duration:** 10 min
- **Started:** 2026-09-02T02:08:48Z
- **Completed:** 2026-09-02T02:18:38Z
- **Tasks:** 3
- **Files modified:** 2 (1 created, 1 modified)

## Accomplishments
- `DomainVocabularyDriftGuardTest.kt` created — duplicates `ComponentRegistryDriftGuardTest`'s
  source-text-scan shape (source-root resolver, extraction machinery, vacuous-pass guards) with an
  independent head-token predicate: any public top-level `@Composable` outside `explorer/` whose
  leading PascalCase word isn't in `PRIMITIVE_NOUN_ALLOWLIST` and whose full name isn't in
  `DOMAIN_VOCABULARY` fails the build with a sorted offender list + remediation instructions.
- Seeded `DOMAIN_VOCABULARY` with all 18 currently-known domain-flavored/borderline/brand-edge-case
  names from RESEARCH.md's live survey (Voice/Album/Heat/Recording/Tag-headed names,
  `YahirAndroidTasteTheme`'s brand-prefix exception, and the three borderline UI-interaction
  descriptors `WaveformCanvas`/`SwipeableActionRow`/`RevealActionRow`), each with a one-line
  rationale — an audit trail mirroring `ComponentRegistry.INTENTIONALLY_UNREGISTERED`'s shape.
- Widened `PRIMITIVE_NOUN_ALLOWLIST` with 28 additional leading-word structural/generic UI
  descriptors beyond RESEARCH.md's 34-word trailing-word seed (see Deviations) so the guard is
  actually green day-one against the real live corpus, per the plan's own acceptance criterion.
- `docs/DESIGN-INTENT.md` extended with `## The Tier-Aware Contribution Litmus` (states the
  asymmetric strict-primitives / loose-patterns gate, GOV-01/D-04) and `## Enforcement` (names
  `DomainVocabularyDriftGuardTest` as the mechanical enforcement of the strict half, explicitly
  scopes the patterns-loose half as prose-only — no `.github/` or CI-review surface exists).
- Manually demonstrated the fail-until-allowlisted mechanism live: removed `VoiceCard` from
  `DOMAIN_VOCABULARY`, confirmed the guard fails listing exactly `[VoiceCard]` with the correct
  remediation message, restored the entry, confirmed green again.

## Task Commits

Each task was committed atomically:

1. **Task 1: Create DomainVocabularyDriftGuardTest.kt** - `2c7c133` (test)
2. **Task 2: Append tier-aware contribution litmus + enforcement scoping to docs/DESIGN-INTENT.md** - `aea6d7d` (docs)
3. **Task 3: Full-suite verification (detekt + doc/guard consistency)** - no commit (verification-only task; confirmed zero drift, no changes needed)

**Plan metadata:** committed by orchestrator after wave merge (worktree mode — this agent does not commit STATE.md/ROADMAP.md)

## Files Created/Modified
- `src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt` - new fail-until-allowlisted JUnit guard (GOV-02)
- `docs/DESIGN-INTENT.md` - two new `##` sections: the tier-aware contribution litmus and its accurately-scoped enforcement (GOV-01)

## Decisions Made
- **Head-token extraction reading:** first/leading PascalCase word (`Regex("[A-Z][a-z0-9]*")` first match), per RESEARCH.md's corroborated Open Question 2 reading — e.g. `VoiceCard` -> `Voice`. This is the only reading under which the guard is structurally capable of catching this codebase's actual domain-flavored names.
- **PRIMITIVE_NOUN_ALLOWLIST widening (see Deviations):** kept the original 34 RESEARCH.md words in their own labeled block and appended 28 new leading-word entries in a separate labeled block, each cross-referenced by name to the exact composables that motivated it, for auditability.
- **HUB_LANE_OVERRIDE=2 on both commits:** the reproduced GOV-03 pre-commit false-flag (stale `v1.10.0` baseline diffed against the cumulative working tree, not the staged delta) blocks every commit today regardless of content. Confirmed both times via `git diff --cached -U0 -- src/main` returning empty — neither commit touches `src/main`. GOV-03's actual fix is plan 03-01's independent, parallel scope; this plan does not depend on it landing first.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Widened PRIMITIVE_NOUN_ALLOWLIST beyond RESEARCH.md's 34-word seed**
- **Found during:** Task 1 (running `./gradlew testDebugUnitTest --tests "*DomainVocabularyDriftGuardTest*"` immediately after writing the file per the plan's literal 34-word list)
- **Issue:** The plan's must_haves explicitly require the guard to be "green on day one against the live corpus" with the specified 18-entry `DOMAIN_VOCABULARY` grandfather list. The first live run instead found 31 offenders — none of them domain nouns (e.g. `AccentColorPicker`, `AdaptiveMediaPreview`, `BulkCreatePopup`, `ClearableTextField`, `ConfirmationDialog`, `ElevationLadder`, `TactileTypeShowcase`, `UndoCenterScreen`, etc.). RESEARCH.md's 34-word `PRIMITIVE_NOUN_ALLOWLIST` seed was derived from the corpus's distinct TRAILING words, but the guard's predicate tests each name's LEADING word (head token) — the two seeding methods aren't guaranteed to produce the same coverage, and in practice didn't.
- **Fix:** Verified each of the 31 offending head tokens against its actual composable's purpose/signature — all 28 distinct tokens (`Accent`, `Adaptive`, `Animated`, `App`, `Attention`, `Bulk`, `Clearable`, `Confirmation`, `Count`, `Crop`, `Cycle`, `Dynamic`, `Elevation`, `Empty`, `Expandable`, `Filter`, `Gradient`, `Hero`, `Icon`, `List`, `Metric`, `Name`, `Progress`, `Segmented`, `Sort`, `Tactile`, `Text`, `Undo`) are generic UI/structural descriptors, not consumer-domain nouns comparable to Voice/Album/Heat/Recording/Tag. Added them to `PRIMITIVE_NOUN_ALLOWLIST` in a clearly-labeled second block (not `DOMAIN_VOCABULARY`, which is reserved for genuine domain coupling).
- **Files modified:** src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt
- **Verification:** `./gradlew testDebugUnitTest --tests "*DomainVocabularyDriftGuardTest*"` — BUILD SUCCESSFUL, zero offenders, after the widening.
- **Committed in:** 2c7c133 (Task 1 commit)

**2. [Environment finding, not auto-fixed] `resolveModuleSourceRoot()`'s named-ancestor walk-up resolves to the wrong tree when run from a nested GSD worktree**
- **Found during:** Task 1's manual red->green demonstration (first attempt: appended a hypothetical `ProjectCard` composable to a file in this worktree, expected RED, got unexpected green)
- **Issue:** This worktree lives at `.claude/worktrees/agent-.../` under the main checkout, which is itself named `yahirandroidtaste`. `resolveModuleSourceRoot()` (duplicated verbatim from `ComponentRegistryDriftGuardTest`, per the plan's explicit instruction) walks UP from the test process's CWD looking for the first ancestor directory literally named `yahirandroidtaste` with a `build.gradle.kts` + valid source root — and finds the MAIN checkout 3 levels up before ever falling back to the CWD-relative default, silently scanning the main checkout's `src/main` instead of this worktree's own copy. Confirmed via a temporary diagnostic print: `sourceRoot=/home/yahir/Projects/Reusable/yahirandroidtaste/src/main/...` (not the worktree path).
- **Why not auto-fixed:** This bug is pre-existing in `ComponentRegistryDriftGuardTest` (identical logic, duplicated per the plan's own explicit "duplicate verbatim" instruction and RESEARCH.md's "Don't Hand-Roll" guidance) and is out of this plan's `files_modified` scope (only `DomainVocabularyDriftGuardTest.kt` and `docs/DESIGN-INTENT.md`). It is purely a nested-GSD-worktree execution artifact — a normal, non-nested checkout run resolves correctly (the CWD itself matches at depth 0). Fixing it would mean touching the sibling guard too, which the plan's Anti-Patterns section explicitly discourages for this phase.
- **Worked around:** Performed the red->green demonstration by toggling a `DOMAIN_VOCABULARY` entry (`VoiceCard`) within the test file itself instead of editing a scanned `src/main` composable — this exercises the identical fail-until-allowlisted mechanism without depending on which physical tree gets scanned (since `VoiceCard` exists identically in both trees). See Accomplishments for the confirmed result.
- **Recommendation for a future phase:** if GSD worktree-isolated test execution becomes a routine verification path for this repo, `resolveModuleSourceRoot()` (both guards) should prefer the CWD-relative default over the named-ancestor walk-up, or bound the walk-up to stop at the first `.git`-file (worktree) boundary.

---

**Total deviations:** 1 auto-fixed (1 bug), 1 environment finding worked around (not a code fix)
**Impact on plan:** The allowlist widening was necessary to meet the plan's own day-one-green acceptance criterion — no scope creep, purely additive to the seed list the plan specified. The worktree source-root finding is an execution-environment characteristic, not a defect introduced by this plan; it does not affect the guard's correctness for any non-nested (normal) test run.

## Issues Encountered
- Both task commits were blocked by the pre-commit hook's reproduced GOV-03 false-flag (RESEARCH.md's documented stale-baseline bug — `tools/verify-additive-diff.sh` diffs `v1.10.0` against the cumulative working tree, not the staged delta, so it inherits every `src/main` rewrite since the tag regardless of what the current commit actually touches). Resolved both times via the sanctioned `HUB_LANE_OVERRIDE=2` bypass, confirmed each time that `git diff --cached -U0 -- src/main` was empty (i.e. under the D-01 fix, both commits would classify as lane 1 unconditionally). GOV-03's actual fix is plan 03-01's independent scope, running in parallel in its own worktree.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- GOV-01 and GOV-02 are both complete: the tier-aware litmus is documented and its strict half is
  mechanically enforced by a green, audit-trailed guard.
- Any future phase that adds a new public top-level `@Composable` to `component/`, `feedback/`,
  `modifier/`, or `theme/` will now be gated by `DomainVocabularyDriftGuardTest` — a name whose
  head token isn't an established primitive noun will need an explicit `DOMAIN_VOCABULARY` entry
  with rationale, or confirmation it belongs in `PRIMITIVE_NOUN_ALLOWLIST`.
- No blockers for GOV-03 (plan 03-01, running in parallel) — this plan's commits did not depend on
  that fix landing first, and used the existing sanctioned override.

---
*Phase: 03-governance-gates*
*Completed: 2026-09-02*
