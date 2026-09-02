---
status: complete
result: all_pass
gate: 1
phase: 03-governance-gates
source: [.planning/ROADMAP.md Phase 3 Success Criteria]
device: N/A — no app/device UI surface in scope for this phase (see Classification below); target = local git repo + JVM test runner on the host
apk: N/A — zero Activity/Compose-UI runtime behavior touched; phase deliverables are tools/verify-additive-diff.sh, a JUnit source-scan guard, and docs/DESIGN-INTENT.md
run: 2026-09-01T00:00:00Z
build: 853c3de (main, clean working tree except pre-existing unrelated background diffs — see Notes)
---

# Self-UAT Log — Phase 3 (Governance Gates)

## Classification (read before the criteria below)

Re-derived from `.planning/ROADMAP.md`'s Phase 3 section directly (not from SUMMARY.md or the prior
`03-VERIFICATION.md`), the Goal is: "Future additive-duplicate drift and lane-flagging friction are
caught by tooling, not left to memory." All 4 numbered Success Criteria are claims about **developer
tooling behavior** (a pre-commit git hook's classification decision, a JUnit test's pass/fail
verdict, and a markdown doc's content) — none are claims about the app's runtime UI. Confirmed via
`git log --stat` across both phase plans (`03-01`, `03-02`): the only files touched are
`tools/verify-additive-diff.sh`, `tools/test/test-*.sh`, `src/test/.../DomainVocabularyDriftGuardTest.kt`
(a **test** file, not `src/main`), and `docs/DESIGN-INTENT.md`. Zero `src/main` runtime files
changed; the project's only `Activity` (`ExplorerActivity`) is untouched.

Per the workflow's own contract (`<initialize>`: "If no user-visible [UI] criteria exist (pure
doc/infra phase): report 'nothing to verify on a target' and exit") — there is no mobile
device/emulator surface to build/install/drive for this phase, and I did not fabricate one. This is
correctly an **INFRA/not-applicable case for on-device UI testing**, matching Phase 2's precedent
(`02-02-SELF-UAT.md`).

However, per core principles 1 and 3 ("drive it, don't describe it" / "adversarial, not
confirmatory"), this phase's actual deliverables ARE independently, adversarially drivable — just at
a different "target": the real git pre-commit hook and the real JVM test runner, both on this host,
both fully exercisable headlessly (verification-ladder rungs 0–3). I did not rubber-stamp the prior
`03-VERIFICATION.md` (`gsd-verifier`, 2026-09-02, 7/7) or either plan's SUMMARY — I re-ran the real
hook against the real repo and re-ran the real JUnit guard, including two genuine red→green
falsification probes not present in the prior verification.

## Arrange/Act discipline for this phase

- **Arranged (seeded):** nothing needed seeding — the SUT is stateless tooling (a bash script and a
  JUnit source scan) operating directly on this repo's own tracked files; no fixture/DB/rows to
  prepare.
- **Did (drove) — genuinely exercised, not just read:**
  1. Invoked the REAL, production-symlinked `tools/hooks/pre-commit` (confirmed live:
     `.git/hooks/pre-commit -> ../../tools/hooks/pre-commit`) directly against this repo's actual
     `HEAD`/tag state (`v1.10.0`) — not the bash fixture suite's throwaway `mktemp -d` sandbox repos
     (which I also re-ran for the plans' own written regression coverage).
  2. Staged a real `.planning/`-only file (`.planning/config.json`) and ran the hook with **no**
     `HUB_LANE_OVERRIDE` — falsifying SC4's "planning/doc commits land without needing
     `HUB_LANE_OVERRIDE`" directly, then restored the stage (`git restore --staged`).
  3. Rewrote an existing line (not merely appended one) in a real `src/main` file
     (`AccentColorPicker.kt`'s package declaration), staged it, and ran the hook with no override —
     falsifying the counter-claim that the fix didn't accidentally neuter lane-2 detection — then
     reverted the file byte-for-byte (`git checkout --`).
  4. Added a real throwaway public `@Composable` (`ProjectMilestoneWidget`, head token `Project` —
     not in either allowlist) to `src/main`, ran `./gradlew testDebugUnitTest
     --tests "*DomainVocabularyDriftGuardTest*" --rerun`, confirmed a genuine RED failure naming
     exactly that offender, then deleted the file and re-ran to confirm GREEN — falsifying SC3's
     "flags... surfacing the coupling for human review" claim end-to-end, not just reading the
     allowlist maps.
  5. Ran the full `tools/test/run-all.sh` shell-fixture suite and the full `./gradlew
     testDebugUnitTest detekt` suite fresh, and read `docs/DESIGN-INTENT.md`'s actual section text.
- Repo restored to its exact pre-probe state after every probe; final `git status --short` matches
  the pre-run snapshot (only pre-existing, unrelated background diffs remain — see Notes).

## Criteria

### 1. A tier-aware contribution litmus is documented: primitives get the strict no-domain-vocabulary gate, patterns get the looser opinion-allowed gate.
result: passed
- **Rung:** 3 (headless content read — no UI surface exists to climb to).
- **Target:** `docs/DESIGN-INTENT.md` (the document itself), on this host's filesystem.
- **Expected:** a section stating the asymmetric strict/loose split by tier, distinct from the
  pre-existing `## The Litmus`/`## The Patterns Contract` sections it references.
- **Arranged (seeded):** none.
- **Did (drove):** `grep -n "^## " docs/DESIGN-INTENT.md` then read the new
  `## The Tier-Aware Contribution Litmus` section verbatim.
- **Observed:** section present (7 `##` headings total, up from the pre-Phase-3 5), states
  "Primitives get the **strict no-domain-vocabulary gate**... Patterns get the **looser,
  opinion-allowed gate**", cross-references GOV-01/D-04 and `## The Litmus`/`## The Patterns
  Contract` by name — matches the ROADMAP criterion text closely enough to falsify a vague/absent
  claim.
- **Evidence:** command output this session (`grep -n "^## " docs/DESIGN-INTENT.md` → 7 headings
  incl. line 76 `## The Tier-Aware Contribution Litmus`); full section text captured in this
  session's transcript.

### 2. That litmus is enforced where feasible — wired into review/test tooling, not just prose.
result: passed
- **Rung:** 3 (data/no-error claim: does the named test actually exist and actually gate the build).
- **Target:** `docs/DESIGN-INTENT.md`'s `## Enforcement` section AND the real
  `DomainVocabularyDriftGuardTest` JUnit test it names, cross-checked against each other.
- **Expected:** the doc's enforcement claim must be accurate against what was actually built — named
  test file must exist, must run via `./gradlew testDebugUnitTest`, must actually fail on a real
  offender (not merely compile/exist).
- **Arranged (seeded):** none.
- **Did (drove):** read `## Enforcement`'s full text; independently ran the named test both green
  (day-one corpus) and red (temporary `ProjectMilestoneWidget` probe, criterion 3 below) to confirm
  the doc's claim is literally true, not aspirational.
- **Observed:** doc states "mechanically enforced by `DomainVocabularyDriftGuardTest`... a
  fail-until-allowlisted JUnit test run via `./gradlew testDebugUnitTest`" and explicitly scopes the
  patterns-loose half as NOT enforced ("no `.github/` PR-template, no CI-review checklist... stays
  prose-only") — confirmed accurate: `find .github` was not run again here but the doc's own
  non-overclaim was cross-checked against the red/green probe in criterion 3, which is the only
  enforcement surface that exists.
- **Evidence:** `docs/DESIGN-INTENT.md` `## Enforcement` section text (this session);
  `DomainVocabularyDriftGuardTest` red/green re-run, this session (see criterion 3).

### 3. A domain-vocabulary drift guard flags (does not forbid) when a new public component name introduces a domain noun, surfacing the coupling for human review.
result: passed
- **Rung:** 3 (headless JVM test data/log check — genuinely exercised RED and GREEN states).
- **Target:** `src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt`
  via `./gradlew testDebugUnitTest`, real JVM/Gradle on this host.
- **Expected:** day-one green against the live corpus (zero offenders); a genuinely new domain-flavored
  composable causes the test to FAIL (not warn/log — the ROADMAP says "flags", but the plan's D-02
  decision is fail-until-allowlisted, i.e. the build itself fails, surfacing it for review, never
  silently forbidding/blocking the composable from compiling).
- **Arranged (seeded):** none — the SUT is the live `src/main` corpus itself.
- **Did (drove):** (1) `./gradlew testDebugUnitTest --tests "*DomainVocabularyDriftGuardTest*"` fresh
  against the untouched tree → green. (2) Added
  `src/main/.../component/ZZZGate1ProbeComposable.kt` with `@Composable fun ProjectMilestoneWidget()`
  (head token `Project` — not in `PRIMITIVE_NOUN_ALLOWLIST`, not in `DOMAIN_VOCABULARY`), re-ran with
  `--rerun` → BUILD FAILED. (3) Deleted the probe file, re-ran → BUILD SUCCESSFUL again.
- **Observed:** the failure's exact `AssertionError` message (read from
  `build/test-results/testDebugUnitTest/TEST-...DomainVocabularyDriftGuardTest.xml`):
  `"Found 1 public top-level @Composable function(s)... whose leading name token is neither an
  established UI-primitive noun... nor an acknowledged domain name: [ProjectMilestoneWidget] — add
  each to DOMAIN_VOCABULARY with a one-line rationale, or confirm its head token belongs in
  PRIMITIVE_NOUN_ALLOWLIST (D-02/D-03)."` — this is a build-time FLAG-and-block-the-build-until-
  acknowledged mechanism (fails the test, not a compile error, not silently forbidding the code from
  existing), matching the ROADMAP's "flags (does not forbid)" language precisely: the composable
  itself compiles fine; only the governance test fails until a human adds a `DOMAIN_VOCABULARY`
  entry. Repo restored to green afterward — confirmed no repo residue (`git status --short` shows no
  `ZZZGate1Probe*` entry).
- **Evidence:** this session's live `./gradlew` runs (RED then GREEN);
  `build/test-results/testDebugUnitTest/TEST-io.github.ygaray.yahirandroidtaste.explorer.DomainVocabularyDriftGuardTest.xml`
  message text captured above.

### 4. The additive-guard pre-commit hook no longer false-flags non-AAR paths (`.planning/`, docs) as lane-2 — planning/doc commits land without needing `HUB_LANE_OVERRIDE`.
result: passed
- **Rung:** 3 (headless data/log check — real git hook, real repo, real tag, no sandbox).
- **Target:** the REAL `.git/hooks/pre-commit` (symlinked to `tools/hooks/pre-commit`, confirmed
  live), invoked directly against this repo's actual current `HEAD` and its actual most recent
  release tag `v1.10.0` — not the bash fixture suite's isolated `mktemp -d` sandbox (that suite was
  also re-run, see Notes, but a sandbox-only pass would not by itself prove the PRODUCTION hook
  wiring is correct).
- **Expected:** (a) staging a real `.planning/`-only file and running the hook with no
  `HUB_LANE_OVERRIDE` exits 0/lane-1; (b) staging a real rewrite of an existing `src/main` line
  still exits 1/lane-2 without an override (the fix must not have neutered legitimate lane-2
  detection as a side effect — the falsifying check this criterion most needs, since a guard that
  never blocks anything would trivially satisfy (a) while being useless).
- **Arranged (seeded):** none — used the repo's real existing tag/history as-is.
- **Did (drove):** `git add .planning/config.json && bash tools/hooks/pre-commit` (no override set) →
  then `git restore --staged .planning/config.json`. Separately: rewrote line 1 of
  `AccentColorPicker.kt` (`package ... // gate1-uat-rewrite-probe`), `git add` it, `bash
  tools/hooks/pre-commit` (no override set) → then `git checkout --` to revert byte-for-byte.
- **Observed:** (a) `LANE 1 (mode=additive, baseline=v1.10.0)`, hook exit 0 — no override needed,
  proving SC4 directly against production wiring, not a simulated fixture. (b) `git diff -U0`
  confirmed a genuine 1-line rewrite (not an append); hook output `LANE 2 (mode=additive,
  baseline=v1.10.0)` / `pre-commit: BLOCKED — lane 2 (non-additive) change on the fast path... To
  land it deliberately: HUB_LANE_OVERRIDE=2 git commit …`, exit 1 — the fix did NOT collapse lane-2
  detection into a false always-pass. Both probes cleanly reverted; final `git status --short`
  contains no residue for either file.
- **Evidence:** this session's live command transcript (both hook invocations, both diffs, both
  exit codes); `tools/verify-additive-diff.sh:85` (`git diff --cached -U0 -- "${PATHS[@]}"` — the
  staged-vs-HEAD basis, read directly, confirming the production script matches the plan's stated
  fix, not just the SUMMARY's claim).

## Independent regression check (bash fixture suites + JVM full suite + detekt, run fresh this session)

- `bash tools/test/run-all.sh` → `PASS=4 FAIL=0` / `PASS=7 FAIL=0` / `PASS=5 FAIL=0` / `PASS=5 FAIL=0`
  across the four suites (`test-classify-hub-change.sh`, `test-precommit-hook.sh`,
  `test-verify-additive-diff.sh`, `test-verify-api-additive.sh`) — matches `03-VERIFICATION.md`'s
  prior count exactly; no regression.
- `./gradlew testDebugUnitTest` (full suite, not just the drift guard) → `BUILD SUCCESSFUL`.
- `./gradlew detekt` → `BUILD SUCCESSFUL`, zero findings (zero-baseline policy honored).

## Summary

total: 4
passed: 4
partial: 0
failed: 0
infra: 0

## Notes / anomalies (for the Gate-2 reviewer)

- This phase shipped **zero app/device-UI-behavioral surface** — confirmed by inspecting every
  file touched across both plans (`tools/`, a `src/test/` JUnit file, `docs/DESIGN-INTENT.md`). No
  mobile device/emulator target applies; Gate-1 agentic UI-driving does not apply to this phase, by
  the same precedent Phase 2 (`02-02-SELF-UAT.md`) established. What "driving the real app" means
  here is exercising the real production tooling (the live-symlinked pre-commit hook, the real
  Gradle/JUnit runner) against the real repo state — which I did, including two falsification probes
  (a real line-rewrite still blocking, and a real domain-noun composable still failing the build)
  that neither prior SUMMARY nor `03-VERIFICATION.md` explicitly performed against the LIVE
  production hook (both prior artifacts' live-repo evidence was concentrated on the `verify-api-
  additive.sh` root-cause correction, not a live pre-commit-hook invocation against real `.planning/`
  and `src/main` files).
- Pre-existing, unrelated background diffs present at session start and still present at session end
  (untouched by this run, confirmed by identical `git status --short` before/after): `.gitignore`,
  `.planning/config.json` and other `.planning/graphs/*` files, `.planning/v1.0-DECISION-MAP.md`,
  `.planning/v1.0-MILESTONE-RUN.md`, plus untracked `.gsd/`, `.planning/intel/`,
  `.planning/phases/03-governance-gates/03-VERIFICATION.md`, `graphify-out/` — none are phase-3
  deliverables and none were modified by any probe in this run.
- STATE.md's GOV-03 residual-risk tracking note (the sibling `verify-api-additive.sh` bug, corrected
  per `03-VERIFICATION.md`'s audit) is a plan-level `must_have`, not a ROADMAP success criterion —
  out of this Gate-1 UAT's scope (which verifies the 4 numbered ROADMAP criteria), but I spot-read it
  and it is present and consistent with the corrected root-cause finding in `03-VERIFICATION.md`.

## Findings routed to gap-closure (if any)

None. All 4 criteria genuinely PASS on live, adversarially-probed evidence — including two
falsification attempts (line-rewrite still blocked; domain-noun composable still fails the build)
that had a real chance to expose an overcorrection and did not.

## Verdict

All 4 criteria PASS → Gate-1 complete (tooling-behavior verification against the real, production-
wired pre-commit hook and the real JVM test runner; no mobile-device/UI surface in scope for this
phase). Human Gate-2 deferred to milestone completion (registered in `HUMAN-UAT-PENDING.md`).
