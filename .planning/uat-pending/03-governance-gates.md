### Phase 3 — governance-gates (v1.0)

- **Status:** `pending`            <!-- pending | signed-off | signed-off-with-gap; owner adds date+name on sign-off -->
- **Milestone:** v1.0 (Hub Stewardship — Tier Legibility → Coherence Audit → Governance → Repin
  Bookkeeping → Gardening)
- **Gate 1 self-UAT log:** [`.planning/phases/03-governance-gates/03-02-SELF-UAT.md`](phases/03-governance-gates/03-02-SELF-UAT.md) — Verdict: **ALL 4 criteria PASS** (tooling-behavior verification against the real, production-wired pre-commit hook and the real JVM test runner; no mobile-device/UI surface in scope — this phase shipped zero `src/main`/Activity changes, confirmed via `git log --stat` across both phase plans, only `tools/`, a `src/test/` JUnit file, and `docs/DESIGN-INTENT.md`, 2026-09-01).
- **Items covered (4 ROADMAP success criteria, GOV-01/GOV-02/GOV-03):**
  - **SC1 — Tier-aware contribution litmus documented.** Confirmed: `docs/DESIGN-INTENT.md`'s new `## The Tier-Aware Contribution Litmus` section states the asymmetric strict-primitives/loose-patterns gate, cross-referencing D-04.
  - **SC2 — Litmus enforced where feasible (not just prose).** Confirmed: `## Enforcement` accurately names `DomainVocabularyDriftGuardTest` as the mechanically-enforced strict half and explicitly scopes the patterns-loose half as prose-only (no CI/PR-template surface exists) — verified against the guard's actual live behavior, not just the doc's claim.
  - **SC3 — Domain-vocabulary drift guard flags (not forbids) new domain nouns.** Live-falsified: added a real throwaway `ProjectMilestoneWidget` composable to `src/main`, confirmed `DomainVocabularyDriftGuardTest` genuinely fails the build naming exactly that offender with remediation instructions, then removed it and confirmed green again.
  - **SC4 — Pre-commit hook no longer false-flags `.planning/`/docs paths as lane-2.** Live-falsified against the REAL production-symlinked hook (not just the bash fixture sandbox): staged a real `.planning/config.json` change, ran the hook with no `HUB_LANE_OVERRIDE`, got lane-1/exit-0. Counter-probe: staged a genuine line-rewrite in a real `src/main` file, confirmed the hook still correctly blocks it as lane-2 without an override — proving the fix didn't neuter legitimate detection.
- **Owner how-to-verify (run at milestone completion):**
  1. Read `.planning/phases/03-governance-gates/03-02-SELF-UAT.md` above for the full live-probe evidence trail (exact hook invocations, diffs, exit codes, and the JUnit red/green transcript).
  2. Optionally re-run `bash tools/test/run-all.sh` and `./gradlew testDebugUnitTest detekt` to reconfirm all four suites are green (`PASS=4/7/5/5 FAIL=0`, `BUILD SUCCESSFUL` with zero detekt findings).
  3. Read `docs/DESIGN-INTENT.md`'s `## The Tier-Aware Contribution Litmus` / `## Enforcement` sections and judge whether the strict/loose split reads as the intended governance policy — this is the one part of this phase that carries an editorial/policy-scoping judgment, not a mechanical check.
- **Note:** No device/app UI behavior shipped this phase — pure tooling deliverable (a bash
  pre-commit-guard diff-basis fix, a JUnit drift guard, a doc update). A separate static/code-level
  verification (`.planning/phases/03-governance-gates/03-VERIFICATION.md`, `gsd-verifier`,
  re-verified 7/7 after one gap-closure cycle) already re-derived and independently re-ran all four
  shell fixture suites plus the STATE.md residual-risk correction for the sibling
  `verify-api-additive.sh` script's dormant bug (tracked for Phase 5, not fixed this phase — see
  STATE.md's `Blockers/Concerns` GOV-03 entry). This Gate-1 pass corroborated that with its own
  independent live falsification probes against the real production hook and the real JUnit runner,
  rather than trusting either prior artifact outright.
