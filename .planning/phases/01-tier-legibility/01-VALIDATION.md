---
phase: 1
slug: tier-legibility
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
# audit-milestone §5.5 distinguishes NOT-VALIDATED (draft) from PARTIAL (validated + nyquist_compliant: false) (#2117)
status: draft
nyquist_compliant: false
wave_0_complete: false
created: 2026-09-01
---

# Phase 1 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit4 + Robolectric (`RobolectricTestRunner`, `@Config(sdk = [35])`) for Compose-UI-touching tests; plain JUnit4 for pure-logic tests |
| **Config file** | `build.gradle.kts` `testOptions { unitTests { ... } }` block (line 67+) |
| **Quick run command** | `./gradlew testDebugUnitTest --tests "io.github.ygaray.yahirandroidtaste.explorer.*"` |
| **Full suite command** | `./gradlew testDebugUnitTest` |
| **Estimated runtime** | ~60 seconds (quick, package-scoped) / ~3-5 minutes (full suite) |

---

## Sampling Rate

- **After every task commit:** Run `./gradlew compileDebugKotlin` (fast — catches a missing `tier = ...` at any of the 53 `Entry(...)` call sites immediately), then `./gradlew testDebugUnitTest --tests "io.github.ygaray.yahirandroidtaste.explorer.*"` where applicable (Waves 1-2 tasks are source-level/grep-verified per plan design — see note below).
- **After every plan wave:** Run `./gradlew testDebugUnitTest` (full suite) + `./gradlew apiCheck` + `./gradlew detekt`
- **Before `/gsd-verify-work`:** Full suite + `apiCheck` + `detekt` must be green
- **Max feedback latency:** ~60 seconds (compile gate is the fast signal; full suite is the wave-boundary signal)

**Note on Wave 1-2 compile gate:** `Entry.tier` is required with no default, and `Entry` is
instantiated 53 times across 9 files in one Kotlin compilation unit — the module cannot compile
green until ALL 53 sites supply `tier =` simultaneously. Plans `01-01`/`01-03`/`01-04` (Waves 1-2)
are therefore verified by source-level grep confirmation (every `Entry(` call site in the files
they touch carries `tier = ...`), not a full `./gradlew compileDebugKotlin`. Plan `01-05` (Wave 3)
is the first point in the phase where all 53 sites exist together — it runs the first real
`compileDebugKotlin`, `apiDump`/`apiCheck`, and `detekt`. This is a hard compiler constraint of the
required-field design (D-01), not a gap in sampling discipline.

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 01-01-T1 | 01-01 | 1 | LEG-01 | T-01-01 | N/A — compile-time enum field | source-assertion | grep `enum class Tier` + `tier:` in `ComponentRegistry.kt`; `./gradlew compileDebugKotlin` will fail until Wave 3 (expected) | ✅ ComponentRegistry.kt exists | ⬜ pending |
| 01-01-T2 | 01-01 | 1 | LEG-01 | T-01-01 | N/A | source-assertion | grep all `Entry(` call sites in `CardsFamilyScreen.kt`/`ChipsFamilyScreen.kt` carry `tier = ` | ✅ both files exist | ⬜ pending |
| 01-02-T1 | 01-02 | 1 | LEG-02 | T-01-02 | N/A — doc-only | manual (doc content review) | N/A — plan-checker / human review of `docs/DESIGN-INTENT.md` content | ❌ new file, created by this task | ⬜ pending |
| 01-02-T2 | 01-02 | 1 | LEG-02 | T-01-02 | N/A | manual (doc content review) | N/A — verify 3 worked examples (CardBase/ChipBar/HeatSwatch) match RESEARCH.md/CONTEXT.md tiering | ❌ same file as T1 | ⬜ pending |
| 01-03-T1..T3 | 01-03 | 2 | LEG-01 | T-01-01 | N/A | source-assertion | grep all 18 `Entry(` call sites in `SheetsFamilyScreen.kt` carry `tier = ` | ✅ SheetsFamilyScreen.kt exists | ⬜ pending |
| 01-04-T1..T3 | 01-04 | 2 | LEG-01 | T-01-01 | N/A | source-assertion | grep all 19 remaining `Entry(` call sites (ButtonsFab/Pickers/Feedback/EmptyState/Progress/TactileFoundation) carry `tier = ` | ✅ all 6 files exist | ⬜ pending |
| 01-05-T1 | 01-05 | 3 | LEG-01 | T-01-01 | N/A | unit | `./gradlew compileDebugKotlin` — first full-module compile with all 53 sites populated | ✅ | ⬜ pending |
| 01-05-T2 | 01-05 | 3 | LEG-01 / LEG-02 (D-02) | T-01-01 | N/A — badge is non-interactive, text-only, no user input | unit + manual | `ComponentRow`/`ComponentDetailScreen` render badge — `./gradlew testDebugUnitTest --tests "*ComponentRegistrySearchTest*" --tests "*ComponentRegistryDriftGuardTest*"`; visual confirmation via running the gallery (manual, per this module's own established UAT convention — no Compose-UI badge-render test exists) | ✅ | ⬜ pending |
| 01-05-T3 | 01-05 | 3 | LEG-01 | T-01-01 | N/A | unit | `./gradlew testDebugUnitTest --tests "*ComponentRegistryTierTest*"` — new test proving `tier` is queryable and cross-checking DESIGN-INTENT.md's 3 worked examples against real registry values; `./gradlew apiDump && ./gradlew apiCheck`; `./gradlew detekt` | ❌ `ComponentRegistryTierTest.kt` — new file, created by this task | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

Existing infrastructure covers all phase requirements — `ComponentRegistry.entries` IS the fixture
data; `ComponentRegistrySearchTest.kt` and `ComponentRegistryDriftGuardTest.kt` already exist and
already exercise the registry. No new test framework, config, or fixture scaffolding is required
before Wave 1 starts. The one new test file (`ComponentRegistryTierTest.kt`) is authored directly
in `01-05-PLAN.md` (Wave 3), not as a Wave 0 stub, because it asserts on the fully-populated
53-entry registry that only exists once Waves 1-2 land.

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Gallery displays the tier badge on `ComponentRow` (list/search) and `ComponentDetailScreen` (detail header) | LEG-01 (D-02) | No Robolectric Compose-UI test asserts the badge's rendered text node today (Wave 0 gap noted as optional in RESEARCH.md); this module's own precedent (`GalleryDemoInteractionTest` KDoc) treats some visual states as "confirmed visually by running the gallery — NOT by a rendered test" | Launch `ExplorerActivity`, open any family list, confirm each row shows a "Primitive"/"Pattern" badge beside the name; tap into a detail screen, confirm the same badge appears in the `TopAppBar` title row |
| `docs/DESIGN-INTENT.md` content quality (litmus is decidable, 3 worked examples correctly reasoned, no consumer-app naming) | LEG-02 | Doc-content correctness is not code-testable — it is a prose/reasoning quality check | Read `docs/DESIGN-INTENT.md`; confirm it states the primitives contract, the patterns contract, and a litmus anchored to the one-way-dependency/no-domain-assumption invariant; confirm CardBase/ChipBar/HeatSwatch are each assigned a tier with the litmus applied, not asserted by fiat |

---

## Validation Sign-Off

> **Plan-time state is a DRAFT.** Leave frontmatter `status: draft` and `nyquist_compliant: false`.
> These are finalized ONLY post-execution by the Nyquist finalizer (the `verify:post` →
> `validate-phase` hook, invoked by execute-phase `finalize_nyquist_validation` after Gate-1). Never
> set `nyquist_compliant: true` — or otherwise "sign off" compliance — at plan time, and do not let
> the plan-checker do so (INC-2026-07-27-01: a premature plan-time flip is what caused inconsistent
> COMPLIANT/PARTIAL milestone-audit states).

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies (Waves 1-2: source-level grep verification, documented above as the correct sampling method given the required-field compiler constraint; Wave 3: full automated compile/test/apiCheck/detekt)
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify (every task above has at minimum a grep/source-assertion or a compiler/test command)
- [ ] Wave 0 covers all MISSING references (none — existing registry/test infrastructure is sufficient, see Wave 0 Requirements above)
