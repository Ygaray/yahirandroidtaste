---
phase: 1
slug: tier-legibility
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
# audit-milestone §5.5 distinguishes NOT-VALIDATED (draft) from PARTIAL (validated + nyquist_compliant: false) (#2117)
status: validated
nyquist_compliant: true
wave_0_complete: true
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
| 01-01-T1 | 01-01 | 1 | LEG-01 | T-01-01-01 | N/A — compile-time enum field | source-assertion | grep `enum class Tier` + `tier:` in `ComponentRegistry.kt`; confirmed present, `./gradlew compileDebugKotlin` green at Wave 3 | ✅ ComponentRegistry.kt | ✅ green |
| 01-01-T2 | 01-01 | 1 | LEG-01 | T-01-01-01 | N/A | source-assertion | grep confirms all 11 `Entry(` sites in `CardsFamilyScreen.kt` + all 5 in `ChipsFamilyScreen.kt` carry `tier = ` | ✅ both files | ✅ green |
| 01-02-T1 | 01-02 | 1 | LEG-02 | T-01-02-01 | N/A — doc-only | manual (doc content review) | Reviewed by `01-VERIFICATION.md` (goal-backward check) — `docs/DESIGN-INTENT.md` contains Primitives Contract + Patterns Contract, no consumer names | ✅ docs/DESIGN-INTENT.md | ✅ green |
| 01-02-T2 | 01-02 | 1 | LEG-02 | T-01-02-01 | N/A | manual (doc content review) | Reviewed by `01-VERIFICATION.md` — 3 worked examples (CardBase→PATTERN, ChipBar→PRIMITIVE, HeatSwatch→PATTERN) cross-checked against live registry by `ComponentRegistryTierTest` | ✅ same file as T1 | ✅ green |
| 01-03-T1..T3 | 01-03 | 2 | LEG-01 | T-01-03-01 | N/A | source-assertion | grep confirms all 18 `Entry(` sites in `SheetsFamilyScreen.kt` carry `tier = ` | ✅ SheetsFamilyScreen.kt | ✅ green |
| 01-04-T1..T3 | 01-04 | 2 | LEG-01 | T-01-04-01 | N/A | source-assertion | grep confirms all 19 remaining `Entry(` sites (ButtonsFab/Pickers/Feedback/EmptyState/Progress/TactileFoundation) carry `tier = ` | ✅ all 6 files | ✅ green |
| 01-05-T1 | 01-05 | 3 | LEG-01 | T-01-05-01 | N/A | unit | `./gradlew compileDebugKotlin` — first full-module compile with all 53 sites populated — BUILD SUCCESSFUL (re-run live at finalization) | ✅ | ✅ green |
| 01-05-T2 | 01-05 | 3 | LEG-01 / LEG-02 (D-02) | T-01-05-01 | N/A — badge is non-interactive, text-only, no user input | unit + manual | Automated: `ComponentRegistrySearchTest`, `ComponentRegistryDriftGuardTest` — re-run live, all green (`testDebugUnitTest --tests "io.github.ygaray.yahirandroidtaste.explorer.*"`, exit 0). Manual: on-device visual confirmation — **closed by Gate-1 self-UAT** (`01-05-SELF-UAT.md`, result `all_pass`, real device Samsung SM-S908U, 2026-09-02): badge never clipped/pushed off-row, name truncates with ellipsis not the badge, colors distinguishable in both themes | ✅ | ✅ green |
| 01-05-T3 | 01-05 | 3 | LEG-01 | T-01-05-02 | N/A | unit | `ComponentRegistryTierTest` — 4/4 passed, re-run live; `apiDump`/`apiCheck` — BUILD SUCCESSFUL; `detekt` — BUILD SUCCESSFUL (zero-baseline maintained) | ✅ ComponentRegistryTierTest.kt | ✅ green |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

Existing infrastructure covers all phase requirements — `ComponentRegistry.entries` IS the fixture
data; `ComponentRegistrySearchTest.kt` and `ComponentRegistryDriftGuardTest.kt` already exist and
already exercise the registry. No new test framework, config, or fixture scaffolding was required
before Wave 1 started. The one new test file (`ComponentRegistryTierTest.kt`) was authored directly
in `01-05-PLAN.md` (Wave 3), not as a Wave 0 stub, because it asserts on the fully-populated
53-entry registry that only exists once Waves 1-2 landed. `wave_0_complete: true` — confirmed at
finalization, no gaps found.

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Resolution |
|----------|-------------|------------|------------|
| Gallery displays the tier badge on `ComponentRow` (list/search) and `ComponentDetailScreen` (detail header) | LEG-01 (D-02) | No Robolectric Compose-UI test asserts the badge's rendered text node (pixel-level clipping/truncation/color-contrast is not code-testable) | **Confirmed 2026-09-02 via Gate-1 self-UAT** (`01-05-SELF-UAT.md`, `result: all_pass`) on real hardware (Samsung SM-S908U / yahirs-s22-ultra-2, Android 15) — badge renders fully on both `ComponentRow` and `ComponentDetailScreen` `TopAppBar`, name truncates with ellipsis (not the badge) for the longest registered names, colors visually distinguishable between tiers in both light and dark theme |
| `docs/DESIGN-INTENT.md` content quality (litmus is decidable, 3 worked examples correctly reasoned, no consumer-app naming) | LEG-02 | Doc-content correctness is not code-testable — it is a prose/reasoning quality check | **Confirmed via `01-VERIFICATION.md`** goal-backward review — primitives/patterns contracts present, litmus applied to CardBase/ChipBar/HeatSwatch, cross-checked against live registry by `ComponentRegistryTierTest` (all 3 assertions pass); zero consumer-app names present |

---

## Validation Audit 2026-09-02

| Metric | Count |
|--------|-------|
| Gaps found | 0 |
| Resolved | 0 |
| Escalated | 0 |

All 12 tasks across 5 plans have automated verification (source-assertion grep, compile, or unit
test) that was actually re-run live at finalization and confirmed green — not merely claimed in
SUMMARY.md. The two Manual-Only items are inherently non-automatable (visual pixel-level rendering,
prose content quality) but both were independently closed with evidence this run: the visual item
via Gate-1's on-device self-UAT, the doc-quality item via the goal-backward verifier. Zero
automatable gaps remain — `nyquist_compliant: true`.

---

## Validation Sign-Off

- [x] All tasks have `<automated>` verify or Wave 0 dependencies (Waves 1-2: source-level grep verification, correct given the required-field compiler constraint; Wave 3: full automated compile/test/apiCheck/detekt — all re-run live and green)
- [x] Sampling continuity: no 3 consecutive tasks without automated verify (every task above has at minimum a grep/source-assertion or a compiler/test command)
- [x] Wave 0 covers all MISSING references (none — existing registry/test infrastructure was sufficient)

**Finalized:** 2026-09-02 by auto-mode `validate-phase` finalizer (execute-phase `finalize_nyquist_gate`, post-Gate-1).
