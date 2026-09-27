---
phase: 07-chip-color-slot
verified: 2026-09-27T18:00:00Z
status: passed
score: 6/6 must-haves verified
covered_files:
  - ".planning/REQUIREMENTS.md"
  - ".planning/phases/07-chip-color-slot/07-01-PLAN.md"
  - ".planning/phases/07-chip-color-slot/07-01-SELF-UAT.md"
  - ".planning/phases/07-chip-color-slot/07-01-SUMMARY.md"
  - ".planning/phases/07-chip-color-slot/07-CONTEXT.md"
  - ".planning/phases/07-chip-color-slot/07-NYQUIST.md"
  - ".planning/phases/07-chip-color-slot/07-PATTERNS.md"
  - ".planning/phases/07-chip-color-slot/07-REVIEW-02.md"
  - ".planning/phases/07-chip-color-slot/07-REVIEW-FIX-02.md"
  - ".planning/phases/07-chip-color-slot/07-REVIEW-FIX.md"
  - ".planning/phases/07-chip-color-slot/07-REVIEW.md"
  - ".planning/phases/07-chip-color-slot/07-SECURITY.md"
  - ".planning/phases/07-chip-color-slot/07-UI-SPEC.md"
  - "api.txt"
  - "config/detekt-compose.yml"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/component/AppChip.kt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/component/CardTagRow.kt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/component/TagChipWithContextMenu.kt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/model/TagChipUiModel.kt"
  - "src/test/java/io/github/ygaray/yahirandroidtaste/component/AppChipTest.kt"
  - "src/test/java/io/github/ygaray/yahirandroidtaste/component/CardTagRowTest.kt"
  - "src/test/java/io/github/ygaray/yahirandroidtaste/component/TagChipWithContextMenuTest.kt"
  - "src/test/java/io/github/ygaray/yahirandroidtaste/model/TagChipUiModelTest.kt"
  - "tools/README-api-guard.md"
covered_digest: "v1:sha256:06881184ccb3a36d80e2b5317ded9158998ae17cca99ae951283512230cd9eba"
behavior_unverified: 0
overrides_applied: 0
re_verification:
  previous_status: gaps_found
  previous_score: 5/6
  gaps_closed:
    - "api.txt is regenerated additively (only new trailing optional params/fields); apiCheck, zero-baseline detekt, ComponentRegistryDriftGuardTest, and DomainVocabularyDriftGuardTest all stay green; no new public composable is added."
  gaps_remaining: []
  regressions: []
---

# Phase 7: Chip-color slot Verification Report (Canonical, re-verified)

**Phase Goal:** A caller can opt into a per-tag chip container color, backward-compatibly, with the
hub rendering the supplied color as-is (consumer owns the muted/theme-aware policy).
**Verified:** 2026-09-27T18:00:00Z
**Status:** passed
**Re-verification:** Yes — after gap closure

## Provenance Note

This file replaces the canonical `07-VERIFICATION.md`, which previously held a stale first-pass
verdict (`status: gaps_found`, 5/6, timestamp `2026-09-27T10:00:00Z`) written before the repo owner's
ABI-break ruling was implemented. A second verification pass ran after the code fix landed and wrote
its passing verdict to a suffixed file, `07-VERIFICATION-02.md` (`status: passed`, 6/6,
`2026-09-27T15:30:00Z`), instead of overwriting the canonical filename — so `phase.complete 7`
(which reads only the canonical filename + its digest) kept refusing on the stale 5/6 gaps_found
verdict even though the implementation had already been fixed, reviewed, and independently confirmed.

This pass independently re-verified the phase goal against the current `main` HEAD (commit `136c907`,
clean working tree for every production file in scope) rather than trusting either prior report's
narrative, and confirms the same conclusion `07-VERIFICATION-02.md` reached. It is now written to the
canonical path with a freshly computed content-digest fingerprint.

## Goal Achievement

### Observable Truths

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 1 | `TagChipUiModel` exposes `color: Color? = null`; every existing named-arg ctor call site compiles unchanged | ✓ VERIFIED | Direct read of `TagChipUiModel.kt:78-85` this session: primary ctor is `id, name, occurrenceCount, createdAt, jaccard` — unchanged in arity/shape from pre-Phase-7; `color` is a separate body `var color: Color? = null` (line 85), never a ctor param. `TagChipUiModelTest` (7 tests, read in full this session — not stubs) re-run fresh: 7/7 pass. |
| 2 | `AppChip`/`TagChipWithContextMenu` expose `containerColorOverride: Color? = null`; omitting it renders byte-identical to pre-phase behavior | ✓ VERIFIED | `AppChip.kt:98`, `TagChipWithContextMenu.kt:87` — trailing nullable param, default `null`. `AppChipTest` re-run fresh (not cached — forced alongside a full `--rerun-tasks` governance battery): 12/12 pass. |
| 3 | `CardTagRow` auto-threads `tag.color` into `containerColorOverride` on both the `TagChipWithContextMenu` and plain-`AppChip` branches; the "+N" overflow chip never receives an override | ✓ VERIFIED | Direct read of `CardTagRow.kt:98-134` this session — `containerColorOverride = tag.color` present on both branches (lines 109, 119); the overflow `AppChip("+$overflow", ...)` call at line 128 has no such argument. `CardTagRowTest` re-run fresh: 3/3 pass. |
| 4 | `isSelected` always wins over `containerColorOverride`; an active `relatednessStrength` (when `!isSelected`) always wins over `containerColorOverride` | ✓ VERIFIED | `AppChip.kt:105-110` — `containerColor` when-block order is exactly `isSelected -> relatedness != null -> containerColorOverride != null -> else`, read directly this session. |
| 5 | `contentColor`/`borderStroke` stay driven solely by `isSelected`/relatedness — `containerColorOverride` never appears in either when-block (D-01) | ✓ VERIFIED | `AppChip.kt:111-120` — direct read this session confirms zero occurrences of `containerColorOverride` in either block. |
| 6 | `api.txt` is regenerated additively (only new trailing optional params/fields); `apiCheck`, zero-baseline `detekt`, `ComponentRegistryDriftGuardTest`, and `DomainVocabularyDriftGuardTest` all stay green; no new public composable is added | ✓ VERIFIED | See "Independent `api.txt` Re-Diff" and "Fresh Governance Battery" below — this is the truth that previously FAILED in the first-pass verification; independently confirmed closed in this pass. |

**Score:** 6/6 truths verified (0 present-but-behavior-unverified)

### Independent `api.txt` Re-Diff (this session's own evidence, not trusted from any prior report)

Confirmed the two candidate pre-Phase-7 baseline references resolve to the same content:

```
$ git show 000bf89~1:api.txt > baseline_pre7.txt
```

Isolating the `TagChipUiModel` class block specifically and diffing baseline vs. current `api.txt`:

```
$ awk '/class TagChipUiModel/,/^  }/' baseline_pre7.txt > pre_tagchip.txt
$ awk '/class TagChipUiModel/,/^  }/' api.txt          > cur_tagchip.txt
$ diff pre_tagchip.txt cur_tagchip.txt
```

Result: every line in the diff is a pure addition — two new shorter-arity ctor overloads, three new
`Companion.of(...)` overloads, one new `property public androidx.compose.ui.graphics.Color? color;`
line, and the new nested `Companion` class. The 5-parameter primary `ctor`, the `copy(...)` method
line, all `componentN()` lines, and all accessor lines from the pre-Phase-7 baseline are **not present
in the diff at all** — i.e., character-for-character unchanged. Zero removed or modified lines.

The `AppChip`/`TagChipWithContextMenu` signature lines each show exactly one new trailing
`optional androidx.compose.ui.graphics.Color? containerColorOverride` parameter appended, with the
original parameter list otherwise untouched — the same additive pattern already accepted for those
two composables in the first-pass verification (never part of the flagged gap).

**Conclusion: `api.txt` is genuinely additive for the whole phase, with zero removed or changed
symbols relative to the true pre-Phase-7 baseline.** This independently reproduces
`07-VERIFICATION-02.md`'s conclusion via a fresh diff run in this session, and agrees with
`07-REVIEW-02.md`'s separate bytecode-level (`javap`) confirmation.

### Fresh Governance Battery (run by this verifier this session, not cached, not inherited)

```
$ ./gradlew apiCheck detekt --rerun-tasks --console=plain
...
0 code smells per 1,000 lloc
> Task :metalavaCheckCompatibilityRelease   (executed)
> Task :apiCheck                             (executed)
BUILD SUCCESSFUL in 19s
8 actionable tasks: 8 executed
```

Both gates pass on a forced, non-cached re-run. Test-class results parsed from the JUnit XML this
session produced:

| Test class | Result |
|---|---|
| `TagChipUiModelTest` | 7/7 pass |
| `AppChipTest` | 12/12 pass |
| `CardTagRowTest` | 3/3 pass |
| `TagChipWithContextMenuTest` | 5/5 pass |
| `ComponentRegistryDriftGuardTest` | 1/1 pass |
| `DomainVocabularyDriftGuardTest` | 2/2 pass |

No new public composable was added: `ComponentRegistry.kt` has no commits touching it in this
phase's range (confirmed via `git log`), and the only new public symbols in `api.txt`'s diff live on
`TagChipUiModel` (a data property + a static factory), not a `@Composable` function — confirmed by
`ComponentRegistryDriftGuardTest` passing (this test fails the build if any public composable in the
visual packages is unregistered).

`TagChipUiModelTest.kt` was read in full this session (not sampled) — its 7 tests are substantive,
directly exercising the exact semantics this phase's fix depends on: `equals()`/`hashCode()` ignore
`color`, `copy()` never carries `color` forward (both non-null-source and null-source cases), and
`Companion.of(...)` actually sets/defaults `color` and matches the primary ctor on every other field.

### Required Artifacts

| Artifact | Expected | Status | Details |
|----------|----------|--------|---------|
| `src/main/java/.../model/TagChipUiModel.kt` | `color` additive, `copy()` byte-identical | ✓ VERIFIED | `color` is a body `var` outside the primary ctor (line 85); `Companion.of(...)` factory added (lines 87-114); KDoc documents the ABI rationale |
| `src/test/java/.../model/TagChipUiModelTest.kt` | Test file proving the equals/copy exclusion + factory semantics | ✓ VERIFIED | 7 substantive tests, read in full this session, all pass |
| `tools/README-api-guard.md` | Documents the Metalava value-class blind spot found by `07-REVIEW-02.md` | ✓ VERIFIED | "Known limitation" section present |
| `api.txt` | Additive-only regeneration for the whole phase | ✓ VERIFIED | Confirmed by this session's own direct diff — zero removed/changed lines relative to the true pre-Phase-7 baseline |
| `AppChip.kt`, `CardTagRow.kt`, `TagChipWithContextMenu.kt` | Threading + precedence intact | ✓ VERIFIED | Re-read directly this session; matches the declared contract exactly |

### Key Link Verification

| From | To | Via | Status | Details |
|------|----|-----|--------|---------|
| `CardTagRow`'s `TagChipWithContextMenu(...)` call | `containerColorOverride = tag.color` | direct arg | ✓ WIRED | `CardTagRow.kt:109` |
| `CardTagRow`'s plain `AppChip(...)` call (else branch) | `containerColorOverride = tag.color` | direct arg | ✓ WIRED | `CardTagRow.kt:119` |
| `TagChipWithContextMenu(...)` | nested `AppChip(...)` | passthrough | ✓ WIRED | `TagChipWithContextMenu.kt:105` |
| `AppChip`'s `containerColor` when-block | precedence order | code order | ✓ WIRED | `AppChip.kt:105-110` |
| `TagChipUiModel(...)` primary ctor | `Companion.of(...)` factory | delegates + sets `color` | ✓ WIRED | `TagChipUiModel.kt:104-113` |

### Behavioral Spot-Checks

| Behavior | Command | Result | Status |
|----------|---------|--------|--------|
| Forced-rerun `apiCheck` (not cached) | `./gradlew apiCheck --rerun-tasks` | executed, BUILD SUCCESSFUL | ✓ PASS |
| Forced-rerun `detekt` (not cached) | `./gradlew detekt --rerun-tasks` | 0 code smells per 1,000 lloc, BUILD SUCCESSFUL | ✓ PASS |
| `TagChipUiModelTest` | JUnit XML | tests="7" failures="0" errors="0" | ✓ PASS |
| `AppChipTest` / `CardTagRowTest` / `TagChipWithContextMenuTest` | JUnit XML | 12/12, 3/3, 5/5 | ✓ PASS |
| `ComponentRegistryDriftGuardTest` / `DomainVocabularyDriftGuardTest` | JUnit XML | 1/1, 2/2 | ✓ PASS |
| Independent `api.txt` diff against `000bf89~1` baseline | `diff` (see above) | zero removed/changed lines for `TagChipUiModel`'s `copy()`/ctor/componentN/accessors | ✓ PASS |

### Requirements Coverage

| Requirement | Source Plan | Description | Status | Evidence |
|-------------|-------------|--------------|--------|----------|
| TAGCOLOR-01 | 07-01-PLAN.md | Opt-in per-tag chip color, backward-compatible, hub renders as-is | ✓ SATISFIED (no remaining flags) | All 6 truths verified independently this session; the previously-flagged ABI-purity sub-claim is closed by an operator-ruled code fix (`7f57492`), confirmed at the text-diff, bytecode, and on-device levels |

No orphaned requirements: `.planning/REQUIREMENTS.md` maps only TAGCOLOR-01 to Phase 7, marked
`Complete`, and it remains the only ID declared in `07-01-PLAN.md`'s frontmatter.

### Anti-Patterns Found

None (blocking). Scanned all files touched by this phase's implementation and ABI-fix commits
(`TagChipUiModel.kt`, `AppChip.kt`, `CardTagRow.kt`, `TagChipWithContextMenu.kt`,
`TagChipUiModelTest.kt`, `AppChipTest.kt`, `CardTagRowTest.kt`) for `TBD`/`FIXME`/`XXX`/`TODO`/`HACK`/
`PLACEHOLDER` — zero matches.

**Informational (non-blocking, already reviewed and accepted):** `07-REVIEW-02.md` surfaced two
narrower findings resolved via documentation (not required code changes), carried forward here for
context only:
- **WR-01 (api.txt coverage gap):** Metalava's signature dump does not track JVM-mangled
  (Kotlin value-class-bearing) members — `color`'s real mangled accessors are invisible to
  `api.txt`/`apiCheck`. Pre-existing tooling blind spot, now documented in `tools/README-api-guard.md`.
- **WR-02 (Compose-stability tradeoff):** `var color` disqualifies `TagChipUiModel` from the Compose
  compiler's `STABLE` inference. Documented, security-audit-accepted (`07-SECURITY.md`
  UF-07-01/AR-07-04), low-severity performance tradeoff — not a correctness break.

### Security

`07-SECURITY.md`: `status: verified`, `threats_open: 0`, all 5 threats dispositioned (4 `accept`,
1 `n/a`), one additional unregistered flag (UF-07-01) assessed low-severity and accepted (AR-07-04) —
no trust-boundary crossing.

### Human Verification Required

None for this phase's goal. The item that previously required human sign-off — accept vs. fix the
`copy()` ABI break — was resolved by an actual operator ruling (`07-CONTEXT.md` § Runtime Decisions,
2026-09-27: "CODE FIX — restore an additive API") and the resulting code fix is independently
confirmed by this pass to close the break, with no regression to the other five truths.

Note for context (not a gap): `07-01-SELF-UAT.md` records Gate-1 self-UAT as complete (`all_pass`)
and defers Gate-2 human sign-off to milestone completion
(`.planning/uat-pending/07-chip-color-slot.md`, `status: pending`) — this project's standard
end-of-milestone human UAT ritual applied uniformly to every phase, not a phase-specific gap.

### Gaps Summary

None. The single gap from the stale first-pass verification — `TagChipUiModel.copy()`'s non-additive
ABI break — is closed. Verified independently in this pass, without trusting either prior report's
narrative: this verifier's own fresh `api.txt` text diff against the true pre-Phase-7 baseline
(`000bf89~1`), a forced (`--rerun-tasks`) non-cached run of `apiCheck`/`detekt`, fresh JUnit XML
results for all six relevant test classes (all green), and direct reads of every production file
this phase touched all agree that the phase goal (TAGCOLOR-01) is fully met with no remaining gaps.

**Phase goal (TAGCOLOR-01) is now fully met with no remaining gaps requiring human sign-off.**

---

*Verified: 2026-09-27T18:00:00Z*
*Verifier: Claude (gsd-verifier)*
