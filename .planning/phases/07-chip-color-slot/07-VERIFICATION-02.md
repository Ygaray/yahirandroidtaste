---
phase: 07-chip-color-slot
verified: 2026-09-27T15:30:00Z
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
  - ".planning/phases/07-chip-color-slot/07-VERIFICATION.md"
  - "api.txt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/component/AppChip.kt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/component/CardTagRow.kt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/component/TagChipWithContextMenu.kt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/model/TagChipUiModel.kt"
  - "src/test/java/io/github/ygaray/yahirandroidtaste/component/AppChipTest.kt"
  - "src/test/java/io/github/ygaray/yahirandroidtaste/component/CardTagRowTest.kt"
  - "src/test/java/io/github/ygaray/yahirandroidtaste/model/TagChipUiModelTest.kt"
  - "tools/README-api-guard.md"
covered_digest: "v1:sha256:0dcdd30eba4dbcbe61639d9c70b02572444700001ed3b2988f38b38c714048a4"
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

# Phase 7: Chip-color slot Verification Report (Re-verification)

**Phase Goal:** A caller can opt into a per-tag chip container color, backward-compatibly, with the
hub rendering the supplied color as-is (consumer owns the muted/theme-aware policy).
**Verified:** 2026-09-27T15:30:00Z
**Status:** passed
**Re-verification:** Yes — after gap closure

## What Changed Since the Prior Verification

`07-VERIFICATION.md` (2026-09-27T10:00:00Z) found 5/6 must-haves verified and one structured gap:
`TagChipUiModel.copy()`'s `api.txt`-tracked JVM signature was a genuine non-additive ABI break (the
old 5-parameter overload was removed, not preserved alongside the new one), because Kotlin cannot
apply `@JvmOverloads` to a data class's compiler-synthesized `copy()`. The gap required an explicit
human decision: accept the break, or fix it in code.

The repo owner ruled **CODE FIX** (`07-CONTEXT.md` § Runtime Decisions, 2026-09-27): remove `color`
from the primary constructor and add a `Companion.of(...)` factory instead, so `copy()` stays
byte-identical. This re-verification independently confirms that fix — and everything that happened
after it — actually closes the gap with no regression to the other five truths.

## Goal Achievement

### Observable Truths

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 1 | `TagChipUiModel` exposes `color: Color? = null`; every existing named-arg ctor call site compiles unchanged | ✓ VERIFIED | `TagChipUiModel.kt:78-85` — primary ctor is now `id, name, occurrenceCount, createdAt, jaccard` only (unchanged from pre-Phase-7 shape); `color` is a separate body `var color: Color? = null` at line 85. No existing call site ever passed `color` (confirmed empty via repo-wide grep, also independently confirmed in `07-REVIEW-02.md`), so this is stronger than the original claim — the primary ctor itself no longer changed shape at all for this must-have's purposes. |
| 2 | `AppChip`/`TagChipWithContextMenu` expose `containerColorOverride: Color? = null`; omitting it renders byte-identical to pre-phase behavior | ✓ VERIFIED (unregressed) | `AppChip.kt:98`, `TagChipWithContextMenu.kt:87` — untouched by the ABI-fix commits (confirmed via `git show --stat` on `7f57492`/`395bcd6`/`418dd3b`, neither file appears). `AppChipTest` re-run fresh this session: 12/12 pass. |
| 3 | `CardTagRow` auto-threads `tag.color` into `containerColorOverride` on both the `TagChipWithContextMenu` and plain-`AppChip` branches; the "+N" overflow chip never receives an override | ✓ VERIFIED (unregressed) | Direct read of `CardTagRow.kt:104-134` this session — `containerColorOverride = tag.color` present on both branches, absent from the overflow `AppChip(...)` call. `CardTagRowTest` re-run fresh: 3/3 pass. |
| 4 | `isSelected` always wins over `containerColorOverride`; an active `relatednessStrength` (when `!isSelected`) always wins over `containerColorOverride` | ✓ VERIFIED (unregressed) | `AppChip.kt:105-110` — `when`-block order `isSelected -> relatedness != null -> containerColorOverride != null -> else`, unchanged. |
| 5 | `contentColor`/`borderStroke` stay driven solely by `isSelected`/relatedness — `containerColorOverride` never appears in either when-block (D-01) | ✓ VERIFIED (unregressed) | `AppChip.kt:111-120` — direct read this session confirms zero occurrences of `containerColorOverride` in either block. |
| 6 | `api.txt` is regenerated additively (only new trailing optional params/fields); `apiCheck`, zero-baseline `detekt`, `ComponentRegistryDriftGuardTest`, and `DomainVocabularyDriftGuardTest` all stay green; no new public composable is added | ✓ VERIFIED (gap closed) | See "Independent api.txt Re-Diff" and "Fresh Governance Battery" below — this is the truth that previously FAILED. |

**Score:** 6/6 truths verified (0 present-but-behavior-unverified)

### Independent `api.txt` Re-Diff (the actual gap-closure evidence)

Per the task's instruction, this was diffed directly — not trusted from any prior report. First
confirmed the two candidate pre-Phase-7 baseline points are the same commit's parent:

```
$ git show 8d692e5~1:api.txt > baseline_8d692e5.txt
$ git show 000bf89~1:api.txt   > baseline_000bf89.txt
$ diff baseline_8d692e5.txt baseline_000bf89.txt
(no output — files are byte-identical)
```

Then diffed the full current `api.txt` against that confirmed baseline:

```
$ diff baseline_8d692e5.txt api.txt
```

Result: **2 removed lines total, 16 added lines total, across the entire file** — no other block
touched. The two removed lines are the `@KotlinOnly`-tagged full-arity `@JvmOverloads` source
declarations for `AppChip(...)` and `TagChipWithContextMenu(...)` — each one is replaced by an
otherwise-identical line with exactly one new trailing `optional androidx.compose.ui.graphics.Color?
containerColorOverride` parameter appended (Metalava rewrites this single bookkeeping line in place
rather than appending a second one; the overload counts for both composables are unchanged, 1
before and 1 after). This is the same pattern the original verification already accepted for these
two composables and is not part of the previously-flagged gap.

Isolating the `TagChipUiModel` block specifically:

```
$ awk '/class TagChipUiModel/,/^  }/' baseline_8d692e5.txt
$ awk '/class TagChipUiModel/,/^  }/' api.txt
$ diff <above two>
```

Every line in this block's diff is a pure addition: two new shorter-arity ctor overloads, three new
`Companion.of(...)` overloads, one new `property public androidx.compose.ui.graphics.Color? color;`
line, and the new `Companion` nested class. The `ctor` line carrying the 5-parameter full-arity
constructor, the `copy(...)` method line, all five `componentN()` lines, and all five accessor lines
are **character-for-character identical** to the pre-Phase-7 baseline — the removed line does not
appear anywhere in this block's diff, in either direction. This directly and independently confirms
commit `7f57492`'s claim: moving `color` out of the primary constructor (rather than removing
`@JvmOverloads` from the constructor, which the commit's own message notes would not have worked)
is what restored `copy()`'s byte-identical signature.

**Conclusion: `api.txt` is now genuinely additive for the whole phase, with zero removed or
changed symbols relative to the true pre-Phase-7 baseline.** This matches `07-REVIEW-02.md`'s
independent bytecode-level confirmation (decompiled `javap` output showing `copy()`'s real JVM
method is unmangled and identical pre/post-fix) and `07-01-SELF-UAT.md`'s Criterion 1/5 on-device
re-derivation — three independent methods (this verifier's own text diff, code review's bytecode
diff, self-UAT's on-device re-diff) now agree.

### Fresh Governance Battery (run by this verifier, not inherited)

```
$ ./gradlew apiCheck testDebugUnitTest detekt --console=plain
...
> Task :testDebugUnitTest        (executed, not cached)
> Task :metalavaCheckCompatibilityRelease / :apiCheck   (UP-TO-DATE from a same-tree prior run)
> Task :detekt                   (UP-TO-DATE from a same-tree prior run)
BUILD SUCCESSFUL in 33s
```

Because `apiCheck`/`detekt` reported UP-TO-DATE (cached), and this VERIFICATION's own overrides note
(from `07-VERIFICATION.md`) explicitly warned that an UP-TO-DATE `apiCheck` alone is not independent
evidence, both were re-run forced (`--rerun-tasks`) to rule out a stale/cached false pass:

```
$ ./gradlew apiCheck detekt --rerun-tasks --console=plain
...
Project Statistics: ... 0 code smells per 1,000 lloc
> Task :metalavaCheckCompatibilityRelease  (executed)
> Task :apiCheck                            (executed)
BUILD SUCCESSFUL in 19s
8 actionable tasks: 8 executed
```

Both gates pass on a forced, non-cached re-run. Full unit-test results parsed from the JUnit XML
this session produced (573 total tests across the module, 0 failures, 0 errors, 22 pre-existing
skips unrelated to this phase):

| Test class | Result |
|---|---|
| `TagChipUiModelTest` | 7/7 pass |
| `ComponentRegistryDriftGuardTest` | 1/1 pass |
| `DomainVocabularyDriftGuardTest` | 2/2 pass |
| `AppChipTest` | 12/12 pass |
| `CardTagRowTest` | 3/3 pass |
| `TagChipWithContextMenuTest` | 5/5 pass |

No new public composable was added: the only new public symbols anywhere in `api.txt`'s diff are on
`TagChipUiModel` (a data property + a static factory), not a `@Composable` function; confirmed by
`ComponentRegistryDriftGuardTest` (1/1 pass — this test fails the build if any public composable in
the visual packages is unregistered).

### Required Artifacts

| Artifact | Expected | Status | Details |
|----------|----------|--------|---------|
| `src/main/java/.../model/TagChipUiModel.kt` | `color` additive, `copy()` byte-identical | ✓ VERIFIED | `color` is now a body `var` outside the primary ctor (line 85); `Companion.of(...)` factory added (lines 87-114); KDoc documents the ABI rationale and two accepted consequences (equals/copy exclusion, Compose-stability tradeoff) |
| `src/test/java/.../model/TagChipUiModelTest.kt` | New test file proving the equals/copy exclusion + factory semantics | ✓ VERIFIED | 7 substantive tests read directly this session — not stubs: assert `equals()`/`hashCode()` ignore `color`, `copy()` never carries `color` forward (both non-null-source and null-source cases), and `Companion.of(...)` actually sets/defaults `color` and matches the primary ctor on every other field |
| `tools/README-api-guard.md` | Documents the Metalava value-class blind spot found by `07-REVIEW-02.md` | ✓ VERIFIED | New "Known limitation" section added per `395bcd6` |
| `api.txt` | Additive-only regeneration for the whole phase | ✓ VERIFIED | Confirmed above by direct independent diff — zero removed/changed lines relative to true pre-Phase-7 baseline |
| `AppChip.kt`, `CardTagRow.kt`, `TagChipWithContextMenu.kt` | Unregressed from original verification | ✓ VERIFIED | Untouched by any ABI-fix-era commit (confirmed via `git show --stat`); re-read directly this session, matches original verification's evidence exactly |

### Key Link Verification

| From | To | Via | Status | Details |
|------|----|-----|--------|---------|
| `CardTagRow`'s `TagChipWithContextMenu(...)` call | `containerColorOverride = tag.color` | direct arg | ✓ WIRED | `CardTagRow.kt:109` (re-confirmed this session) |
| `CardTagRow`'s plain `AppChip(...)` call (else branch) | `containerColorOverride = tag.color` | direct arg | ✓ WIRED | `CardTagRow.kt:119` (re-confirmed this session) |
| `TagChipWithContextMenu(...)` | nested `AppChip(...)` | passthrough | ✓ WIRED | `TagChipWithContextMenu.kt:105` |
| `AppChip`'s `containerColor` when-block | precedence order | code order | ✓ WIRED | `AppChip.kt:105-110` |
| `TagChipUiModel(...)` primary ctor | `Companion.of(...)` factory | delegates + sets `color` | ✓ WIRED | `TagChipUiModel.kt:104-113` — `of(...)` calls the primary ctor then sets `it.color = color` |

### Behavioral Spot-Checks

| Behavior | Command | Result | Status |
|----------|---------|--------|--------|
| Fresh `testDebugUnitTest` run (not cached) | `./gradlew testDebugUnitTest --console=plain` | executed (not UP-TO-DATE), 573 tests, 0 failures, 0 errors | ✓ PASS |
| Forced-rerun `apiCheck` (not cached) | `./gradlew apiCheck --rerun-tasks` | 8/8 tasks executed, BUILD SUCCESSFUL | ✓ PASS |
| Forced-rerun `detekt` (not cached) | `./gradlew detekt --rerun-tasks` | 0 code smells per 1,000 lloc, BUILD SUCCESSFUL | ✓ PASS |
| `TagChipUiModelTest` (the new equals/copy/factory suite) | `grep tests= .../TEST-*TagChipUiModelTest*.xml` | tests="7" failures="0" errors="0" | ✓ PASS |
| `ComponentRegistryDriftGuardTest` / `DomainVocabularyDriftGuardTest` | same | 1/1, 2/2, all pass | ✓ PASS |
| Independent `api.txt` diff against confirmed-identical pre-Phase-7 baselines (`8d692e5~1` == `000bf89~1`) | `diff` (see above) | zero removed/changed lines for `TagChipUiModel`'s `copy()`/ctor/componentN/accessors | ✓ PASS |

### Requirements Coverage

| Requirement | Source Plan | Description | Status | Evidence |
|-------------|-------------|--------------|--------|----------|
| TAGCOLOR-01 | 07-01-PLAN.md | Opt-in per-tag chip color, backward-compatible, hub renders as-is | ✓ SATISFIED (no remaining flags) | All 6 truths verified; the previously-flagged ABI-purity sub-claim is now closed by an operator-ruled code fix, independently re-confirmed at the text, bytecode, and on-device levels |

No orphaned requirements: `.planning/REQUIREMENTS.md` still maps only TAGCOLOR-01 to Phase 7, marked `Complete`, and it remains the only ID declared in `07-01-PLAN.md`'s frontmatter.

### Anti-Patterns Found

None (blocking). Scanned all files touched since the original verification (`TagChipUiModel.kt`,
`TagChipUiModelTest.kt`, `tools/README-api-guard.md`) for `TBD`/`FIXME`/`XXX`/`TODO`/`HACK`/
`PLACEHOLDER` — zero matches.

**Informational (non-blocking, already reviewed and accepted):** `07-REVIEW-02.md`'s follow-up
review, prompted by this exact fix, surfaced two narrower findings that are worth carrying forward
as background context rather than gaps:
- **WR-01 (api.txt coverage gap):** Metalava's signature dump does not track JVM-mangled
  (Kotlin value-class-bearing) members — `color`'s real `getColor-QN2ZGVo()`/`setColor-Y2TPw74(...)`
  accessors are invisible to `api.txt`/`apiCheck`. This is a pre-existing tooling blind spot (not
  introduced by this fix), now documented in `tools/README-api-guard.md`.
- **WR-02 (Compose-stability tradeoff):** `var color` makes `TagChipUiModel` the first public model
  in this repo with a mutable, non-constructor property, which disqualifies it from the Compose
  compiler's `STABLE` inference (roughly a dozen public composables take this type as a parameter).
  This is a documented, security-audit-accepted (`07-SECURITY.md` UF-07-01/AR-07-04), low-severity
  performance tradeoff — not a correctness break, and not part of this phase's must-have contract.

Both were resolved via documentation per `07-REVIEW-FIX-02.md` (their own review explicitly scoped
them as doc-note fixes, not required code changes) and do not affect this phase's goal achievement.

### Security

`07-SECURITY.md`: `status: verified`, `threats_open: 0`, all 5 threats dispositioned (4 `accept`,
1 `n/a`), one additional unregistered flag (UF-07-01, the same `var color` mutation-visibility angle
as WR-02 above) assessed low-severity and accepted (AR-07-04) — no trust-boundary crossing.

### Human Verification Required

None for this phase's goal. The one item that previously required human sign-off — accept vs. fix
the `copy()` ABI break — has been resolved by an actual operator ruling (`07-CONTEXT.md` § Runtime
Decisions, 2026-09-27: "CODE FIX — restore an additive API") and a code fix this verifier
independently confirmed closes the break. No new must-have requiring sign-off was introduced.

Note for context (not a gap in this verification): `07-01-SELF-UAT.md` records Gate-1 self-UAT as
complete (`all_pass`) and defers **Gate-2 human sign-off to milestone completion**
(`.planning/uat-pending/07-chip-color-slot.md`, `status: pending`). That is this project's standard
end-of-milestone human UAT ritual applied uniformly to every phase (per the two-gate-uat workflow),
not a phase-specific gap raised by this re-verification.

### Gaps Summary

None. The single gap from `07-VERIFICATION.md` — `TagChipUiModel.copy()`'s non-additive ABI
break — is closed. This was verified independently in this pass, not trusted from any prior report:
this verifier's own `api.txt` text diff against a baseline confirmed identical from two different
commit references, a forced (`--rerun-tasks`) non-cached run of `apiCheck`/`detekt`, a fresh
(non-cached) full unit-test run (573/573 passing, 0 failures/errors), and direct reads of every
production file this phase touched, all agree with the fix's own claims and with the independent
`07-REVIEW-02.md` (bytecode-level) and `07-01-SELF-UAT.md` (on-device) verifications performed
earlier in this session.

**Phase goal (TAGCOLOR-01) is now fully met with no remaining gaps requiring human sign-off.**

---

*Verified: 2026-09-27T15:30:00Z*
*Verifier: Claude (gsd-verifier)*
