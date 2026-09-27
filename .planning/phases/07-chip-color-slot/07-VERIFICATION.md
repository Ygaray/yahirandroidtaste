---
phase: 07-chip-color-slot
verified: 2026-09-27T10:00:00Z
status: gaps_found
score: 5/6 must-haves verified
covered_files:
  - .planning/REQUIREMENTS.md
  - .planning/phases/07-chip-color-slot/07-01-PLAN.md
  - .planning/phases/07-chip-color-slot/07-01-SUMMARY.md
  - .planning/phases/07-chip-color-slot/07-CONTEXT.md
  - .planning/phases/07-chip-color-slot/07-PATTERNS.md
  - .planning/phases/07-chip-color-slot/07-REVIEW-FIX.md
  - .planning/phases/07-chip-color-slot/07-REVIEW.md
  - .planning/phases/07-chip-color-slot/07-UI-SPEC.md
  - api.txt
  - config/detekt-compose.yml
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/AppChip.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/CardTagRow.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/TagChipWithContextMenu.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/TagChipUiModel.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/AppChipTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/CardTagRowTest.kt
covered_digest: "v1:sha256:fa680e39f164ba594becabf8f257a496277434d1126b58b0aa7348acbf2f3adb"
behavior_unverified: 0
overrides_applied: 0
gaps:
  - truth: "api.txt is regenerated additively (only new trailing optional params/fields); apiCheck, zero-baseline detekt, ComponentRegistryDriftGuardTest, and DomainVocabularyDriftGuardTest all stay green; no new public composable is added."
    status: partial
    reason: >
      Five of the six sub-claims in this must-have hold exactly as stated (verified directly
      against api.txt and a live gradle run — see Required Artifacts / Key Link tables below).
      The sixth — "api.txt is regenerated additively (only new trailing optional
      params/fields)" — is literally false for one member: `TagChipUiModel.copy()`'s
      Metalava-tracked signature changed from `copy(String, String, int, long, Double)` to
      `copy(String, String, int, long, Double, Color?)` with the old 5-parameter overload
      REMOVED from api.txt, not added alongside. This is a genuine binary-ABI break on a
      public data-class member (`@JvmOverloads` cannot be applied to Kotlin's compiler-
      synthesized `copy()`). `./gradlew apiCheck` shows green only because the plan's own
      verify command runs `apiDump` before `apiCheck` in the same invocation, which rewrites
      api.txt to match current source before checking it against itself — apiCheck is
      therefore trivially green here and is not independent evidence that the change is
      additive. This is a real, if narrow, deviation from the literal must-have text, not a
      new anti-pattern: it was already caught by this phase's own code review (07-REVIEW.md
      WR-01), disclosed in 07-01-SUMMARY.md's deviation log, and risk-accepted there on the
      grounds that this repo's JitPack-full-source-recompile consumer model has no
      stale-precompiled-caller scenario for a `NoSuchMethodError` at link time to occur. That
      acceptance was made by the executor/reviewer roles, not confirmed by the repo owner —
      accepting a documented public-API ABI break is exactly the kind of decision this
      project's CLAUDE.md profile says should be surfaced for explicit confirmation rather than
      auto-closed by an agent.
    artifacts:
      - path: "api.txt"
        issue: "Line ~944: TagChipUiModel.copy() shows only the new 6-param signature; the pre-phase 5-param copy() overload (String,String,int,long,Double?) no longer exists anywhere in the file — a removal, not an addition."
    missing:
      - "Either an explicit owner-approved override accepting the copy() ABI break as-is (it is already fully investigated, documented, and low-risk per the JitPack full-recompile model — a one-line sign-off would close this), or a code change (e.g. a @JvmStatic Companion.of(...) factory per REVIEW.md's own suggested alternative) that restores a genuinely additive api.txt."
---

# Phase 7: Chip-color slot Verification Report

**Phase Goal:** A caller can opt into a per-tag chip container color, backward-compatibly, with the
hub rendering the supplied color as-is (consumer owns the muted/theme-aware policy).
**Verified:** 2026-09-27T10:00:00Z
**Status:** gaps_found
**Re-verification:** No — initial verification

## Goal Achievement

### Observable Truths

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 1 | `TagChipUiModel` exposes `color: Color? = null`; every existing named-arg ctor call site compiles unchanged | ✓ VERIFIED | `TagChipUiModel.kt:44-51` — `color: Color? = null` trailing field; `@JvmOverloads` on the primary ctor; `api.txt:934-937` shows the pre-phase 3/4/5-arg ctor overloads preserved unchanged alongside the new 6-arg one; full `testDebugUnitTest` run green (existing call sites `ExplorerFakeData.kt`, `TagPickerSheetContentTest.kt`, etc. compile and pass) |
| 2 | `AppChip`/`TagChipWithContextMenu` expose `containerColorOverride: Color? = null`; omitting it renders byte-identical to pre-phase behavior | ✓ VERIFIED | `AppChip.kt:98`, `TagChipWithContextMenu.kt:87` — trailing nullable param, default `null`; `AppChipTest.kt` "omitting containerColorOverride — this phase's regression floor" test passes (ran live: `AppChipTest` 12/12 pass, 0 failures) |
| 3 | `CardTagRow` auto-threads `tag.color` into `containerColorOverride` on both the `TagChipWithContextMenu` and plain-`AppChip` branches; the "+N" overflow chip never receives an override | ✓ VERIFIED | `CardTagRow.kt:109` (`TagChipWithContextMenu(... containerColorOverride = tag.color)`), `CardTagRow.kt:119` (plain `AppChip(... containerColorOverride = tag.color)`), overflow `AppChip` at `CardTagRow.kt:128-137` has no `containerColorOverride` arg at all; `CardTagRowTest` ran live: 3/3 pass, 0 failures |
| 4 | `isSelected` always wins over `containerColorOverride`; an active `relatednessStrength` (when `!isSelected`) always wins over `containerColorOverride` | ✓ VERIFIED | `AppChip.kt:105-110` — `containerColor` when-block order is `isSelected -> relatedness != null -> containerColorOverride != null -> else`, matching the declared precedence exactly; `AppChipTest.kt` "containerColor precedence order..." test passes live |
| 5 | `contentColor`/`borderStroke` stay driven solely by `isSelected`/relatedness — `containerColorOverride` never appears in either when-block (D-01) | ✓ VERIFIED | `AppChip.kt:111-120` — direct read confirms zero occurrences of `containerColorOverride` in either block; `AppChipTest.kt` "contentColor when-block never references..." and "borderStroke when-block never references..." tests pass live |
| 6 | `api.txt` is regenerated additively (only new trailing optional params/fields); `apiCheck`, zero-baseline `detekt`, both drift guards stay green; no new public composable is added | ✗ FAILED (partial) | `AppChip`/`TagChipWithContextMenu` signatures ARE purely additive (confirmed in `api.txt`); no new public composable exists (confirmed — no `ComponentRegistry.kt` change needed/made); `detekt`, `apiCheck`, `ComponentRegistryDriftGuardTest` (1/1 pass), `DomainVocabularyDriftGuardTest` (2/2 pass) all green live. BUT `TagChipUiModel.copy()`'s api.txt entry is a genuine non-additive ABI break — the old 5-param overload was removed, not preserved (see Gaps below). |

**Score:** 5/6 truths verified (0 present-but-behavior-unverified)

### Required Artifacts

| Artifact | Expected | Status | Details |
|----------|----------|--------|---------|
| `src/main/java/.../model/TagChipUiModel.kt` | `color: Color? = null` additive field | ✓ VERIFIED | Field present, KDoc documents null-means-no-override semantics; `@JvmOverloads` on ctor |
| `src/main/java/.../component/AppChip.kt` | `containerColorOverride: Color? = null` param + precedence arm | ✓ VERIFIED | Param present at correct position; single new `when`-arm slotted correctly |
| `src/main/java/.../component/TagChipWithContextMenu.kt` | Same param, verbatim pass-through | ✓ VERIFIED | Param present; passed straight through to nested `AppChip(...)` unwrapped |
| `src/main/java/.../component/CardTagRow.kt` | Auto-thread `tag.color` on both visible branches, not overflow | ✓ VERIFIED | Both branches confirmed; overflow branch confirmed clean |
| `src/test/java/.../component/CardTagRowTest.kt` | New source-structural test file, 3 tests | ✓ VERIFIED | File exists, 3 tests, all pass live (0 failures) |
| `src/test/java/.../component/AppChipTest.kt` | Extended with precedence/D-01/regression tests | ✓ VERIFIED | 12 tests total (5 new for this phase per SUMMARY), all pass live (0 failures) |
| `api.txt` | Additive-only regeneration | ⚠️ PARTIAL | Additive for `AppChip`/`TagChipWithContextMenu`/ctor; non-additive for `TagChipUiModel.copy()` (see Gaps) |

### Key Link Verification

| From | To | Via | Status | Details |
|------|----|-----|--------|---------|
| `CardTagRow`'s `TagChipWithContextMenu(...)` call | `containerColorOverride = tag.color` | direct arg | ✓ WIRED | `CardTagRow.kt:109` |
| `CardTagRow`'s plain `AppChip(...)` call (else branch) | `containerColorOverride = tag.color` | direct arg | ✓ WIRED | `CardTagRow.kt:119` |
| `TagChipWithContextMenu(...)` | nested `AppChip(...)` | passthrough | ✓ WIRED | `TagChipWithContextMenu.kt:105` — plain value pass, not wrapped |
| `AppChip`'s `containerColor` when-block | precedence order | code order | ✓ WIRED | `AppChip.kt:105-110` matches `isSelected -> relatedness -> override -> else` exactly |

### Behavioral Spot-Checks

| Behavior | Command | Result | Status |
|----------|---------|--------|--------|
| AppChipTest suite (12 tests incl. 5 new for TAGCOLOR-01) | `./gradlew testDebugUnitTest --tests "*AppChipTest*"` | tests=12 failures=0 errors=0 | ✓ PASS |
| CardTagRowTest suite (3 new tests) | `./gradlew testDebugUnitTest --tests "*CardTagRowTest*"` | tests=3 failures=0 errors=0 | ✓ PASS |
| ComponentRegistryDriftGuardTest | `./gradlew testDebugUnitTest --tests "*ComponentRegistryDriftGuardTest*"` | tests=1 failures=0 errors=0 | ✓ PASS |
| DomainVocabularyDriftGuardTest | `./gradlew testDebugUnitTest --tests "*DomainVocabularyDriftGuardTest*"` | tests=2 failures=0 errors=0 | ✓ PASS |
| detekt zero-baseline | `./gradlew detekt` | BUILD SUCCESSFUL (UP-TO-DATE) | ✓ PASS |
| apiCheck | `./gradlew apiCheck` | BUILD SUCCESSFUL (UP-TO-DATE) | ⚠️ PASS but not independent evidence of additivity — see Gaps (apiCheck only compares current source against the already-regenerated api.txt in this working tree; it cannot detect the copy() break because the break has already been baked into the committed baseline via apiDump) |

Note: the project's `./gradlew build` full chain is not exercised here per the orchestrator's note re:
KI-2026-09-02-01 (pre-existing, out-of-scope `metalavaCheckCompatibilityDebug` false-positive on
`UndoHistoryStore_Factory`); the individual gates above (`testDebugUnitTest`, `detekt`, `apiCheck`)
were run directly and independently instead, which sidesteps that known issue entirely.

### Requirements Coverage

| Requirement | Source Plan | Description | Status | Evidence |
|-------------|-------------|--------------|--------|----------|
| TAGCOLOR-01 | 07-01-PLAN.md | Opt-in per-tag chip color, backward-compatible, hub renders as-is | ✓ SATISFIED (with one flagged gap) | Truths 1-5 fully verified; truth 6's core intent (no new composable, drift guards/detekt green) holds, but the "additive api.txt" sub-claim has one documented exception (`copy()` ABI break) — functionally the requirement's user-facing contract is met; the ABI-purity sub-claim is not, pending owner sign-off |

No orphaned requirements: REQUIREMENTS.md maps only TAGCOLOR-01 to Phase 7, and it is the only ID declared in 07-01-PLAN.md's frontmatter.

### Anti-Patterns Found

None. Scanned all phase-touched files (`TagChipUiModel.kt`, `AppChip.kt`, `TagChipWithContextMenu.kt`,
`CardTagRow.kt`, `CardTagRowTest.kt`, `AppChipTest.kt`) for `TBD`/`FIXME`/`XXX`/`TODO`/`HACK`/
`PLACEHOLDER`/empty-implementation patterns — zero matches (one incidental doc-comment use of the
word "placeholder" describing UI behavior, not a stub marker).

### Human Verification Required

None. Every must-have is either fully code-and-test verified or resolved into the single structured
gap below (which is a human sign-off decision, not a "can't verify programmatically" item — the
technical facts are fully established by direct evidence).

### Gaps Summary

Five of six PLAN must-have truths are cleanly verified against the live codebase: the color slot
threads end-to-end exactly as specified (model -> AppChip/TagChipWithContextMenu ->
CardTagRow), the precedence contract (`isSelected > relatedness > override > else`) is implemented
and test-locked, D-01's "no content/border color computation" invariant holds by direct code
inspection, and no new public composable was introduced. All associated tests were re-run live in
this verification pass (not merely trusted from SUMMARY.md) and are 100% green with zero failures.

The one gap is narrow and already well-understood: `TagChipUiModel.copy()`'s Metalava-tracked JVM
signature changed non-additively (the old 5-parameter overload was removed, not preserved
alongside a new one) because Kotlin disallows `@JvmOverloads` on a data class's compiler-synthesized
`copy()`. This was independently caught by this phase's own code review (07-REVIEW.md WR-01),
disclosed in 07-01-SUMMARY.md's deviation log, and risk-accepted there on the reasoning that this
library's actual consumer model (JitPack tag bump -> full source recompile) has no
stale-precompiled-caller scenario for this specific break to bite. That reasoning is sound and this
verifier finds no fault with it technically — but it is a public-API ABI-breaking change accepted by
an agent's own judgment call, not confirmed by the repo owner, and the root CLAUDE.md's governance
tone (explicit human gating on anything that ripples to consumers) suggests this class of decision
warrants an explicit go/no-go rather than an automatic pass.

**This looks intentional and well-reasoned.** To accept this deviation, add to VERIFICATION.md
frontmatter:

```yaml
overrides:
  - must_have: "api.txt is regenerated additively (only new trailing optional params/fields); apiCheck, zero-baseline detekt, ComponentRegistryDriftGuardTest, and DomainVocabularyDriftGuardTest all stay green; no new public composable is added."
    reason: "TagChipUiModel.copy()'s non-additive ABI break (5-param overload removed, not preserved) is a documented, structurally-unfixable Kotlin/Metalava limitation (data-class synthetic copy() cannot carry @JvmOverloads). Risk accepted: this library's JitPack-full-source-recompile consumer model has no stale-precompiled-caller scenario for the resulting NoSuchMethodError failure mode to occur in practice."
    accepted_by: "<owner>"
    accepted_at: "<ISO timestamp>"
```

Alternatively, per 07-REVIEW.md's own suggested remediation, a future follow-up could add a
`@JvmStatic Companion.of(...)` factory with `@JvmOverloads` for ABI-sensitive callers, restoring a
genuinely additive public surface without touching `copy()`.

---

*Verified: 2026-09-27T10:00:00Z*
*Verifier: Claude (gsd-verifier)*
