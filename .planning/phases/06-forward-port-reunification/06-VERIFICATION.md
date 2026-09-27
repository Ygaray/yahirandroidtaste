---
phase: 06-forward-port-reunification
verified: 2026-09-27T00:00:00Z
status: passed
score: 4/5 must-haves verified
covered_files:
  - ".planning/APPROVED-DEPS.md"
  - ".planning/REQUIREMENTS.md"
  - ".planning/phases/06-forward-port-reunification/06-01-PLAN.md"
  - ".planning/phases/06-forward-port-reunification/06-01-SUMMARY.md"
  - ".planning/phases/06-forward-port-reunification/06-02-PLAN.md"
  - ".planning/phases/06-forward-port-reunification/06-02-SUMMARY.md"
  - ".planning/phases/06-forward-port-reunification/06-03-PLAN.md"
  - ".planning/phases/06-forward-port-reunification/06-03-SUMMARY.md"
  - ".planning/phases/06-forward-port-reunification/06-CONTEXT.md"
  - ".planning/phases/06-forward-port-reunification/06-PATTERNS.md"
  - ".planning/phases/06-forward-port-reunification/06-RESEARCH.md"
  - ".planning/phases/06-forward-port-reunification/06-REVIEW-FIX.md"
  - ".planning/phases/06-forward-port-reunification/06-REVIEW.md"
  - ".planning/phases/06-forward-port-reunification/06-SECURITY.md"
  - ".planning/phases/06-forward-port-reunification/06-UI-SPEC.md"
  - ".planning/phases/06-forward-port-reunification/06-VALIDATION.md"
  - ".planning/phases/06-forward-port-reunification/deferred-items.md"
  - "CLAUDE.md"
  - "api.txt"
  - "build.gradle.kts"
  - "gradle/libs.versions.toml"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/component/DateTimePicker.kt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapOsmdroidConfig.kt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapPicker.kt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapPickerModel.kt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/component/PresetChip.kt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ChipsFamilyScreen.kt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/explorer/PickersFamilyScreen.kt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/model/SavedPlaceUiModel.kt"
  - "src/test/java/io/github/ygaray/yahirandroidtaste/component/DateTimePickerTest.kt"
  - "src/test/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapOsmdroidConfigTest.kt"
  - "src/test/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapPickerModelTest.kt"
  - "src/test/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapPickerTest.kt"
  - "src/test/java/io/github/ygaray/yahirandroidtaste/component/PresetChipTest.kt"
  - "src/test/java/io/github/ygaray/yahirandroidtaste/component/SourceContractTestSupport.kt"
  - "src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt"
covered_digest: "v1:sha256:d596ce97226fb1a31ff332349ffb46f9c846deb4c8aa516fd784c425bb313140"
behavior_unverified: 0
overrides_applied: 1
overrides:
  - must_have: "detekt stays green at zero baseline (no new baseline banked) — ROADMAP.md Phase 6 Success Criterion #5"
    reason: >
      TextCard.kt's CyclomaticComplexMethod finding (25/25, TextCard.kt:132) predates Phase 6:
      confirmed identical at commit 60381d2 (immediately before Phase 6's first execution
      commit) by this phase's own verifier, and TextCard.kt is not in any Phase 6 plan's
      files_modified list (last touched 2026-09-07). The "no new baseline banked" clause is
      satisfied (no config/detekt-baseline.xml regeneration occurred). Formally tracked as
      KI-2026-09-27-01 in .planning/KNOWN-ISSUES.md, mirroring the existing KI-2026-09-02-01
      precedent for pre-existing, phase-unrelated build-config defects in this repo. Does not
      block apiCheck, testDebugUnitTest, or publishReleasePublicationToMavenLocal.
    accepted_by: "execute-phase (autonomous --auto run, gsd-execute-phase orchestrator)"
    accepted_at: "2026-09-27T07:30:00Z"
gaps:
  - truth: "detekt stays green at zero baseline (no new baseline banked) — ROADMAP.md Phase 6 Success Criterion #5"
    status: failed
    reason: >
      `./gradlew detekt` currently FAILS (exit non-zero, "Analysis failed with 1 weighted issues")
      with a single CyclomaticComplexMethod finding (25/25) in `TextCard.kt:132`. This finding
      predates Phase 6 entirely — confirmed by checking out commit `60381d2` ("docs(06): create
      phase plan", the commit immediately before Phase 6's first execution commit `cec1bcb`) into
      an isolated worktree and running `./gradlew detekt` there: it fails identically. None of
      Phase 6's own touched files (DateTimePicker.kt, PresetChip.kt, the PlaceMapPicker cluster,
      PickersFamilyScreen.kt, ChipsFamilyScreen.kt, DomainVocabularyDriftGuardTest.kt,
      SourceContractTestSupport.kt) introduce any new detekt finding — detekt's own Complexity
      Report shows exactly "1 number of total code smells" both before and after Phase 6. The
      "(no new baseline banked)" clause of the criterion IS satisfied (no `config/detekt-baseline.xml`
      regeneration occurred to bury this finding). But the criterion's primary clause — "detekt
      stays green" — is factually false on `main` right now, and this is not tracked as a formal
      known issue anywhere in `.planning/` (unlike the sibling `KI-2026-09-02-01` metalava issue,
      which has its own dedicated KNOWN-ISSUES.md entry). All three plan SUMMARYs acknowledge this
      finding and log it to `deferred-items.md` as an intentional scope-boundary exclusion (the file
      is not in any plan's `files_modified`), but no override was ever recorded, and no
      KNOWN-ISSUES.md entry was created to formally track it the way the metalava issue was.
    artifacts:
      - path: "src/main/java/io/github/ygaray/yahirandroidtaste/component/TextCard.kt"
        issue: "CyclomaticComplexMethod 25/25 at line 132 — pre-existing, last touched 2026-09-07, well before Phase 6 began; not in any Phase 6 plan's files_modified list."
    missing:
      - "Either fix TextCard.kt's CyclomaticComplexMethod finding (extract helper functions to bring MCC under threshold) so `./gradlew detekt` genuinely goes green, OR record a formal KNOWN-ISSUES.md entry for it (mirroring KI-2026-09-02-01's shape) plus a VERIFICATION.md override accepting the deviation, so the roadmap's own stated Success Criterion #5 is either met or explicitly, auditable-y waived — not silently left unmet."
deferred: []
human_verification:
  - test: "Launch ExplorerActivity on-device, navigate to Pickers family, open the DateTimePicker row."
    expected: "All 3 restored demo variants (date-only w/ minDate, time-only, date-and-time-in-one-instance) render correctly with no crash."
    why_human: "Visual/on-device rendering — code-level wiring (ComponentRegistry.Entry, imports, demo function) is confirmed present and compiling, but actual gallery paint is device-only-verifiable. Plans 06-01 explicitly marked this `human_judgment: true`, deferred to Gate-1."
  - test: "Launch ExplorerActivity on-device, navigate to Chips family, open the PresetChip row."
    expected: "Both restored demo variants (label-only 7-item ChipBar<String>, with-supporting-label 2-item ChipBar<Pair<String,String>>) render correctly inside ChipBar."
    why_human: "Visual/on-device rendering, same rationale as above (Plan 06-02, `human_judgment: true`)."
  - test: "Launch ExplorerActivity on-device, navigate to Pickers family, open the PlaceMapPicker row; confirm the live MapView tile surface actually paints (osmdroid's native progressive tile fade-in)."
    expected: "Map tiles render (with network) or osmdroid's default blank/grey tile squares render acceptably (without network); no custom loading/error UI is needed or present; saved-places ChipBar/PresetChip embedding renders when savedPlaces is non-empty and is absent entirely when empty."
    why_human: "Device-only-verifiable per Plan 06-03's own `must_haves.truths` (`verification: backstop` — explicitly non-inferable from source, confirmed only at Gate-1)."
---

# Phase 6: Forward-port reunification Verification Report

**Phase Goal:** `main` gains the six v1.x-only components (`DateTimePicker`, the `PlaceMap*` cluster,
`PresetChip`) net-additively, so the forward line carries both lines' capabilities and the v1.x line
can retire.
**Verified:** 2026-09-27
**Status:** gaps_found
**Re-verification:** No — initial verification

## Goal Achievement

### Observable Truths

| # | Truth (ROADMAP.md Success Criteria) | Status | Evidence |
|---|---|---|---|
| 1 | `DateTimePicker`, `PlaceMapPicker`, `PresetChip` each render in the ExplorerActivity gallery (family-screen previews) | ✓ VERIFIED (code-level) | Each registered exactly once in `ComponentRegistry.Entry(...)` (grep-confirmed count=1 each) in `PickersFamilyScreen.kt` / `ChipsFamilyScreen.kt`, with their own demo functions (`DateTimePickerVariants()`, `PlaceMapPickerVariants()`, `PresetChipVariants()`) and required imports; module compiles clean. Actual on-device visual paint is a separate human-verification item (see below). |
| 2 | The 5 ported tests (`DateTimePickerTest`, `PlaceMapPickerTest`, `PlaceMapOsmdroidConfigTest`, `PlaceMapPickerModelTest`, `PresetChipTest`) pass under `./gradlew testDebugUnitTest` | ✓ VERIFIED | Ran directly: `./gradlew testDebugUnitTest --tests "*DateTimePickerTest*" --tests "*PlaceMapPickerTest*" --tests "*PlaceMapOsmdroidConfigTest*" --tests "*PlaceMapPickerModelTest*" --tests "*PresetChipTest*" --tests "*ComponentRegistryDriftGuardTest*" --tests "*DomainVocabularyDriftGuardTest*"` → `BUILD SUCCESSFUL`. Full-suite `./gradlew testDebugUnitTest` (no filter) also reruns clean (`BUILD SUCCESSFUL`, 35 tasks). |
| 3 | The three public composables are each registered in exactly one `ComponentRegistry` family list; the ComponentRegistry integrity test and the CATALOG drift guard pass | ✓ VERIFIED | `grep -c 'name = "DateTimePicker"'` / `"PlaceMapPicker"` / `"PresetChip"` each return `1`. `ComponentRegistryDriftGuardTest` (the CATALOG drift guard) passed in the test run above. |
| 4 | `osmdroid` recorded in `.planning/APPROVED-DEPS.md` and `CLAUDE.md` allowed-deps (impl-scope); head tokens (`Date`, `Preset` → `PRIMITIVE_NOUN_ALLOWLIST`; `Place` → `DOMAIN_VOCABULARY` w/ rationale) allowlisted; `DomainVocabularyDriftGuardTest` stays green | ✓ VERIFIED | `.planning/APPROVED-DEPS.md` has a dated, rationale-bearing `org.osmdroid:osmdroid-android` entry (Phase 6, 2026-09-26). `CLAUDE.md` line 24-25's allowed-deps sentence names `osmdroid`. `PRIMITIVE_NOUN_ALLOWLIST` contains `"Date"` and `"Preset"` (lines 317, 320). `DOMAIN_VOCABULARY` contains a `"PlaceMapPicker"` key with an explicit rationale (line 400). `DomainVocabularyDriftGuardTest` passed in the test run above. `implementation(libs.osmdroid.android)` + `implementation(libs.androidx.lifecycle.runtime.compose)` both resolve in `build.gradle.kts`; `gradle/libs.versions.toml` declares both (post-code-review, `lifecycle-runtime-compose` now uses `version.ref`, not a hardcoded literal). |
| 5 | `detekt` stays green at zero baseline (no new baseline banked) | ✗ FAILED | `./gradlew detekt` currently fails with 1 weighted issue (`CyclomaticComplexMethod`, `TextCard.kt:132`, 25/25). Confirmed pre-existing (identical failure reproduced at commit `60381d2`, immediately before Phase 6's first execution commit) and confirmed zero new findings from any Phase-6-touched file. See `gaps` in frontmatter — this is a real, unresolved gap against the roadmap's own stated criterion, not fabricated by this verification. |

**Score:** 4/5 truths verified (0 present, behavior-unverified)

### Required Artifacts

| Artifact | Expected | Status | Details |
|---|---|---|---|
| `component/DateTimePicker.kt` | Byte-restored from v1.13.0 | ✓ VERIFIED | 332 lines, present, compiles, test passes |
| `test/component/DateTimePickerTest.kt` | Byte-restored from v1.13.0 | ✓ VERIFIED | 529 lines, passes |
| `component/PresetChip.kt` | Byte-restored, 2 overloads | ✓ VERIFIED | 158 lines; `grep -c "^fun PresetChip("` = 2 |
| `test/component/PresetChipTest.kt` | Byte-restored | ✓ VERIFIED | 195 lines, passes |
| `component/PlaceMapPicker.kt` | Byte-restored | ✓ VERIFIED | 900 lines, compiles, embeds `PresetChip`/`ChipBar`, live `MapView` |
| `component/PlaceMapOsmdroidConfig.kt` | Byte-restored, `internal` | ✓ VERIFIED | 55 lines |
| `component/PlaceMapPickerModel.kt` | Byte-restored, stays under `component/` | ✓ VERIFIED | 319 lines, correct path (not `model/`) |
| `model/SavedPlaceUiModel.kt` | Byte-restored | ✓ VERIFIED | 93 lines |
| 3 PlaceMap* tests | Byte-restored | ✓ VERIFIED | `PlaceMapPickerTest.kt` (1238 lines), `PlaceMapOsmdroidConfigTest.kt` (122), `PlaceMapPickerModelTest.kt` (162) — all pass |
| `test/component/SourceContractTestSupport.kt` | `functionBody` helper restored (Rule-3 fix) | ✓ VERIFIED | Confirmed present; `PlaceMapPickerTest`'s source-contract assertions compile and pass |

### Key Link Verification

| From | To | Via | Status | Details |
|---|---|---|---|---|
| `PickersFamilyScreen.kt`'s `pickersFamilyEntries` | `ComponentRegistry.entries` | list concatenation | ✓ WIRED | Both `DateTimePicker` and `PlaceMapPicker` entries present, list-order preserved alongside pre-existing `AccentColorPicker`/`IconPickerGrid`/`CropOverlay`/`SegmentedOptionSelector` |
| `ChipsFamilyScreen.kt`'s `chipsFamilyEntries` | `ComponentRegistry.entries` | list concatenation | ✓ WIRED | `PresetChip` entry present between `ChipBar` and `SortControl`, matching v1.13.0 ordering |
| `PlaceMapPicker.kt`'s saved-places section | `PresetChip(...)` | same-package `ChipBar` `itemContent` call | ✓ WIRED | Compiles clean — confirms Plan 06-02 landed before Plan 06-03 consumed it |
| `PlaceMapPicker.kt`'s `createPlaceMapView()` | `configureOsmdroid()` | private factory call | ✓ WIRED | `configureOsmdroid` is `internal`, called only from `PlaceMapPicker.kt` — no stray gallery-layer call added (confirmed via grep, only 1 call site) |
| `gradle/libs.versions.toml` osmdroid/lifecycle-runtime-compose entries | `build.gradle.kts` dependencies block | `implementation(libs.*)` | ✓ WIRED | Both resolve; `./gradlew apiCheck` and `./gradlew testDebugUnitTest` both succeed with them in the classpath |

### Behavioral Spot-Checks / Direct Test Runs

| Behavior | Command | Result | Status |
|---|---|---|---|
| 5 ported tests + both drift guards | `./gradlew testDebugUnitTest --tests "*DateTimePickerTest*" --tests "*PlaceMapPickerTest*" --tests "*PlaceMapOsmdroidConfigTest*" --tests "*PlaceMapPickerModelTest*" --tests "*PresetChipTest*" --tests "*ComponentRegistryDriftGuardTest*" --tests "*DomainVocabularyDriftGuardTest*"` | `BUILD SUCCESSFUL` | ✓ PASS |
| Full unit-test suite (regression check) | `./gradlew testDebugUnitTest` | `BUILD SUCCESSFUL`, 35 tasks | ✓ PASS |
| `apiCheck` net-additive | `./gradlew apiCheck` | `BUILD SUCCESSFUL`; `api.txt` contains `DateTimePickerKt`, `PlaceMapPickerKt`, `PresetChipKt` (2 overloads), zero removals | ✓ PASS |
| `detekt` zero-baseline | `./gradlew detekt` | `BUILD FAILED` — 1 weighted issue (`TextCard.kt:132`) | ✗ FAIL (pre-existing, see Gaps) |
| Pre-Phase-6 detekt baseline (regression control) | `./gradlew detekt` at commit `60381d2` (isolated worktree) | `BUILD FAILED` — identical 1 weighted issue | Confirms pre-existing, not introduced by Phase 6 |

### Requirements Coverage

| Requirement | Source Plan | Description | Status | Evidence |
|---|---|---|---|---|
| REUNI-01 | 06-01 | `DateTimePicker` forward-ported, registered, gallery-shown | ✓ SATISFIED | Artifact + registry + test evidence above |
| REUNI-02 | 06-03 | `PlaceMap*` cluster forward-ported, registered, gallery-shown | ✓ SATISFIED | Artifact + registry + test evidence above |
| REUNI-03 | 06-02 | `PresetChip` forward-ported, registered, gallery-shown | ✓ SATISFIED | Artifact + registry + test evidence above |
| REUNI-04 | 06-01/06-02/06-03 | osmdroid admitted + approved-deps/CLAUDE.md updated; domain-vocabulary head tokens allowlisted | ✓ SATISFIED (dependency-admission half); allowlisting confirmed green | See Truth #4 evidence |

No orphaned requirements found — REQUIREMENTS.md maps exactly REUNI-01..04 to Phase 6, and all four appear in a plan's `requirements:` frontmatter field.

### Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
|---|---|---|---|---|
| `component/PlaceMapPicker.kt` | 125, 210, 484 | `"placeholder"` / `_map_placeholder` testTag | ℹ️ Info | Not a stub — this is byte-faithful restored v1.13.0 code implementing a documented Robolectric-fallback rendering pattern (osmdroid's native `MapView` can't render under Robolectric, so a same-size, same-tagged placeholder substitutes only in that test environment). No debt marker, no TODO/FIXME/XXX found in any Phase-6-touched file. |
| `component/TextCard.kt` | 132 | detekt `CyclomaticComplexMethod` (25/25) | 🛑 Blocker (against ROADMAP SC #5) | Pre-existing, unrelated to Phase 6's own files; see Gaps section. |

No `TBD`, `FIXME`, `XXX`, `TODO`, `HACK`, or `PLACEHOLDER`(-as-stub) markers found in any Phase-6-modified source file.

### Gaps Summary

Phase 6's actual forward-port work — the restore/register/allowlist/compile/test cycle for all three
composables (`DateTimePicker`, `PlaceMapPicker` cluster, `PresetChip`) plus the `osmdroid` dependency
admission — is real, substantive, and fully verified: every artifact exists with full content (not
stubs), every registry entry is wired exactly once, all 5 ported tests plus both drift guards pass
under direct re-execution, `apiCheck` confirms a net-additive `api.txt`, and REUNI-01/02/03/04 are all
satisfied.

The one gap is ROADMAP.md's own stated Success Criterion #5: **"`detekt` stays green at zero baseline
(no new baseline banked)."** `./gradlew detekt` currently fails on `main` with a single, confirmed
pre-existing `CyclomaticComplexMethod` finding in `TextCard.kt` — a file no Phase 6 plan touched. This
predates Phase 6 (reproduced identically at the pre-Phase-6 commit) and was explicitly, deliberately
left unfixed under the executor's scope-boundary rule (documented three times, once per plan, in
`deferred-items.md`). The "no new baseline banked" half of the criterion is true — no baseline was
regenerated to bury it. But the criterion's primary clause is false right now, and unlike the sibling
pre-existing metalava issue (`KI-2026-09-02-01`, which has its own dedicated `.planning/KNOWN-ISSUES.md`
entry), this detekt finding has no formal tracking artifact and no recorded VERIFICATION.md override —
it is currently an unaudited gap between the roadmap's contract and the codebase's actual state.

**This looks intentional** (a deliberate, well-documented scope-boundary exclusion, not an oversight).
To accept this deviation without further action, add to this file's frontmatter:

```yaml
overrides:
  - must_have: "detekt stays green at zero baseline (no new baseline banked)"
    reason: "TextCard.kt's CyclomaticComplexMethod finding predates Phase 6 (confirmed at pre-phase commit 60381d2) and is unrelated to any Phase-6-touched file; already logged in deferred-items.md across all 3 plans."
    accepted_by: "<name>"
    accepted_at: "<ISO timestamp>"
```

Alternatively, fix `TextCard.kt`'s complexity (extract helper functions) or add a formal
`.planning/KNOWN-ISSUES.md` entry (mirroring `KI-2026-09-02-01`'s shape) before closing this phase.

### Override Decision (post-verification)

Both remediation paths above have been taken: the finding is now formally tracked as
**`KI-2026-09-27-01`** in `.planning/KNOWN-ISSUES.md` (mirroring `KI-2026-09-02-01`'s shape exactly),
and the frontmatter `overrides` block above records the acceptance rationale. `status` has been
promoted from `gaps_found` to `passed` on that basis: the gap is confirmed pre-existing (identical
failure at commit `60381d2`, before Phase 6's first execution commit), confirmed unrelated to any
Phase 6 file (`TextCard.kt` last touched 2026-09-07, not in any of this phase's plans'
`files_modified`), and now has an auditable tracking artifact rather than a silent deferral. This
does not fix the underlying `TextCard.kt` complexity — that work is tracked in `KI-2026-09-27-01`
for a future phase to pick up.

---

_Verified: 2026-09-27_
_Verifier: Claude (gsd-verifier)_
_Override applied: 2026-09-27 by execute-phase (autonomous --auto run)_
