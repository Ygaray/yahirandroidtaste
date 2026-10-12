---
phase: quick-261005-e2e
plan: 01
subsystem: model (voice outcome data classes) / binary ABI
tags: [binary-compat, abi, data-class, copy-default, default-constructor-marker, INC-2026-10-05-02, F1c]
status: complete
date: 2026-10-05
requirements: [INC-2026-10-05-02/F1c]
variant: K
dependency_graph:
  requires: [261005-dmc (F1 + F1b)]
  provides: [v2.4.1 default-ctor synthetics x4, v2.4.1 copy$default x6, javap ABI gate seed for F3]
  affects: [F3 tools/verify-binary-abi.sh, F4 API.md/PATTERNS docs, Phase 17 D-03 wording]
tech-stack:
  added: []
  patterns: ["hidden synthetic-shaped secondary ctor (..., mask: Int, marker: DefaultConstructorMarker?)", "private companion + @Deprecated(HIDDEN) @JvmStatic @JvmName(\"copy\\$default\")"]
key-files:
  created:
    - src/test/java/io/github/ygaray/yahirandroidtaste/model/DataClassBinaryCompatShimTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/model/HandledByUiModel.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/model/ProposedItemUiModel.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/model/UndoRefusedUiModel.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/model/VoiceOutcomeUiState.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/model/UndoRowUiModel.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/model/FailureActionUiModel.kt
decisions:
  - "Variant K rolled out (orchestrator yahir-gsd-control-plane-3b ruling): keep @JvmOverloads and the visible legacy copy byte-identical; add hidden synthetic-shaped members only."
  - "Behavioural tests also pass deliberately wrong values in masked slots (mixed masks), pinning the bit-to-parameter mapping rather than only the all-defaults path."
metrics:
  duration: ~10 min (resume at Task 1 Step 6; spike time not included)
  completed: 2026-10-05
  tasks: 3
  files: 7
estimate:
  tokens: 55000
actuals:
  tokens: 7834      # chars/4 over git diff cea06a6..0956d79 (522 insertions, 7 files)
  tasks: 3
  commits: 6        # measured: git rev-list --count cea06a6..HEAD
plan_head_before: cea06a6ab025552ba929662a5ded3356402e915f
commits: 6
---

# Quick 261005-e2e Plan 01: F1c v2.4.1 data-class synthetics Summary

**What landed:** variant K, ruled by the orchestrator. Ten v2.4.1 compiler-generated synthetics are restored on six data
classes: four `<init>(…, int, DefaultConstructorMarker)` default-argument ctors and six static `copy$default`.
Each one is a hidden member with the synthetic's own shape, and each is pinned by a reflection test that
invokes it with v2.4-style masks. The javap diff of the full AAR against the v2.4.1 JitPack AAR now reports
**missing=0**, and api.txt is unchanged (0/0).

## Commits (all on `main`, all LANE 1, no override, no `--no-verify`)

| # | sha | message | hook lane |
|---|-----|---------|-----------|
| 1 | `4a0fe6d` | test(261005-e2e): add failing v2.4.1 synthetic-ABI test for HandledByUiModel | 1 |
| 2 | `6963d07` | fix(261005-e2e): restore HandledByUiModel v2.4.1 default-ctor and copy$default synthetics | 1 |
| 3 | `ce20c99` | test(261005-e2e): add failing v2.4.1 synthetic-ABI tests for ProposedItemUiModel, UndoRefusedUiModel, Failure | 1 |
| 4 | `6f93bfd` | fix(261005-e2e): restore v2.4.1 default-ctor and copy$default synthetics for ProposedItemUiModel, UndoRefusedUiModel, Failure | 1 |
| 5 | `3e307bd` | test(261005-e2e): add failing v2.4.1 copy$default tests for UndoRowUiModel, FailureActionUiModel | 1 |
| 6 | `0956d79` | fix(261005-e2e): restore v2.4.1 copy$default for UndoRowUiModel and FailureActionUiModel | 1 |

Every commit's hook printed `LANE 1 (mode=additive, baseline=v2.4.1)`, so no `HUB_LANE_OVERRIDE` was needed.
Nothing was tagged (`git tag --points-at HEAD` is empty) and nothing was pushed.

## Spike answers (Task 1 Steps 0-5)

The spike ran before this resume. Its full measured evidence is in `261005-e2e-SPIKE.md` in this directory:
Step 0 baseline ctor sets, the R1 clash line verbatim, R's apiCheck errors and api.txt diff verbatim, and
the flags tables. In brief:

| Check | R1 | R | K (shipped) |
|---|---|---|---|
| Compiles | no: platform declaration clash on `<init>(String,String,String,String,Integer)V` | yes | yes |
| apiCheck before apiDump | n/a | FAIL (6 `Removed` errors) | PASS |
| api.txt after apiDump | n/a | -6 / +0 (lane 3) | 0 / 0 |
| v2.4.1 primary `<init>(5)` / `copy(5)` | n/a | become ACC_SYNTHETIC (Java-source break) | unchanged, public |
| Existing pins | n/a | VoiceModelLabelDefaultsTest copy-arity pin RED | all green |
| Gate verdict | does not qualify | does not qualify | QUALIFIES |

- **Q1:** see SPIKE §Step 0 and §Q1. K never declares a defaulted v2.4.1-arity ctor, so it cannot clash. It declares the synthetic's own 7-param shape.
- **Q2:** see SPIKE §Q2. Under K the visible legacy copy and every `@JvmOverloads` arity are untouched, and api.txt stays 0/0. That held after the full rollout too (below).
- **Q3:** every source-shape pin compiles without ambiguity. That covers the positional legacy ctor, `copy(<first> = …)` and `copy()` for all six classes; FailureActionUiModel's trailing-lambda shape is pinned in VoiceI18nSourceCompatTest.

## RED evidence (NoSuchMethodException, before each GREEN)

- **Task 1** (`4a0fe6d`): `java.lang.NoSuchMethodException: io.github.ygaray.yahirandroidtaste.model.HandledByUiModel.<init>(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,int,kotlin.jvm.internal.DefaultConstructorMarker)`
- **Task 2** (`ce20c99`, 4 run / 3 failed):
  - `ProposedItemUiModel.<init>(java.lang.String,java.lang.String,java.lang.String,java.lang.String,boolean,kotlin.jvm.functions.Function0,kotlin.jvm.functions.Function2,int,kotlin.jvm.internal.DefaultConstructorMarker)`
  - `UndoRefusedUiModel.<init>(java.lang.String,java.lang.String,int,kotlin.jvm.internal.DefaultConstructorMarker)`
  - `VoiceOutcomeUiState$Failure.<init>(java.lang.String,…HandledByUiModel,…FailureActionUiModel,int,kotlin.jvm.internal.DefaultConstructorMarker)`
- **Task 3** (`3e307bd`, 6 run / 2 failed):
  - `UndoRowUiModel.copy$default(…UndoRowUiModel,java.lang.String,java.lang.String,…UndoRowState,int,java.lang.Object)`
  - `FailureActionUiModel.copy$default(…FailureActionUiModel,java.lang.String,kotlin.jvm.functions.Function0,int,java.lang.Object)`

## Tests (ruling condition 1)

`DataClassBinaryCompatShimTest` has 6 tests, one per class, and every one is green. Each test does two things:

- **Reflection:** it resolves the exact v2.4.1 descriptor through `getDeclaredConstructor(<v2.4.1 params>, int, DefaultConstructorMarker)` or `getMethod("copy$default", cls, <v2.4.1 params>, int, Object)`. It also asserts the member is public, plus static for `copy$default`.
- **Behaviour:** it invokes the member with non-zero masks:
  - **all-defaults mask** (ctor): the result equals the v2.4.1-default construction, and the appended properties take their English default (`"Escalations:"`, `"Remove"`, `"Couldn't undo:"` / `"changed since"`, null body and null semanticsPrefix).
  - **single-slot mask** (copy): the result equals `original.copy(<first> = x)`. The original has every field non-default, including appended props such as `"Escaladas:"`, `"Quitar"`, `"No se pudo:"`, `"Deshecho"` and `Destructive`, and the test asserts each appended prop is preserved (lambda refs by `assertSame`).
  - **all-bits mask** (copy): the result equals the original.
  - **mask 0** (ctor): explicit values bind to their named properties, which pins positional order.
  - **mixed mask** (added beyond the plan): deliberately wrong values (`"IGNORED"`, `99`, `true`, a different lambda) go in the masked slots, and the test asserts they are ignored while the unmasked slots bind, including an explicit `null` or `false`. This pins the bit-to-parameter mapping, not just the all-defaults path.
  - **source-shape pins:** the call shapes compile without ambiguity.

Full suite: **734 tests, 0 failures, 0 errors, 22 skipped**. That is the 729 measured during the spike plus 5 new tests.

## Gates (ruling condition 3)

| Gate | Result |
|---|---|
| `./gradlew testDebugUnitTest detekt apiCheck assembleRelease` | BUILD SUCCESSFUL (rc 0) |
| detekt | 0 findings; `config/detekt-baseline.xml` still empty (`<CurrentIssues/>`) and untouched; no `@Suppress` added |
| `./gradlew apiDump` then `git diff -U0 api.txt` | **+0 / -0** (byte-identical) |
| VoiceModelLabelDefaultsTest, VoiceI18nSourceCompatTest, VoiceBinaryCompatShimTest | byte-identical to `489c410` (`git diff --quiet` rc 0) and green |
| Scope: `git log --name-only --grep='(261005-e2e)'` | only the 6 model files and the new test file; no `.planning/phases/`, API.md or CLAUDE.md |

## Descriptor table: the 10 v2.4.1 baseline descriptors at HEAD (release AAR, `javap -p -v`)

| Class | Member | HEAD | HEAD flags (v2.4.1) |
|---|---|---|---|
| HandledByUiModel | `<init>(String,String,String,String,Integer,I,DefaultConstructorMarker)V` | present | `0x1001` ACC_PUBLIC, ACC_SYNTHETIC (`0x1001`) |
| HandledByUiModel | `copy$default(H,String,String,String,String,Integer,I,Object)H` | present | `0x1019` ACC_PUBLIC, ACC_STATIC, ACC_FINAL, ACC_SYNTHETIC (`0x1009`) |
| ProposedItemUiModel | `<init>(String,String,String,String,Z,Function0,Function2,I,DCM)V` | present | `0x1001` (`0x1001`) |
| ProposedItemUiModel | `copy$default(P,String,String,String,String,Z,Function0,Function2,I,Object)P` | present | `0x1019` (`0x1009`) |
| UndoRefusedUiModel | `<init>(String,String,I,DCM)V` | present | `0x1001` (`0x1001`) |
| UndoRefusedUiModel | `copy$default(U,String,String,I,Object)U` | present | `0x1019` (`0x1009`) |
| VoiceOutcomeUiState$Failure | `<init>(String,HandledByUiModel,FailureActionUiModel,I,DCM)V` | present | `0x1001` (`0x1001`) |
| VoiceOutcomeUiState$Failure | `copy$default(F,String,HandledByUiModel,FailureActionUiModel,I,Object)F` | present | `0x1019` (`0x1009`) |
| UndoRowUiModel | `copy$default(R,String,String,UndoRowState,I,Object)R` | present | `0x1019` (`0x1009`) |
| FailureActionUiModel | `copy$default(A,String,Function0,I,Object)A` | present | `0x1019` (`0x1009`) |

The extra `ACC_FINAL` on the `copy$default` bridges comes from the `@JvmStatic` bridge of a companion member.
It has no effect on `invokestatic` linking. Each class still carries its own compiler-generated full-arity
`copy$default` (flags `0x1009`), which has a different descriptor and does not collide.

## Binary ABI gate (seed for F3 tools/verify-binary-abi.sh)

This is the exact command (Task 3 verify, run from the repo root at `0956d79`'s tree):

```bash
cd /home/yahir/Projects/Reusable/android/yahirandroidtaste && ./gradlew testDebugUnitTest detekt apiCheck assembleRelease && ./gradlew apiDump && D=$(git diff -U0 api.txt) && ! printf '%s\n' "$D" | grep -q '^-[^-]' && git diff --quiet 489c410 -- src/test/java/io/github/ygaray/yahirandroidtaste/model/VoiceModelLabelDefaultsTest.kt src/test/java/io/github/ygaray/yahirandroidtaste/component/VoiceI18nSourceCompatTest.kt src/test/java/io/github/ygaray/yahirandroidtaste/component/VoiceBinaryCompatShimTest.kt && B=$(ls ~/.gradle/caches/modules-2/files-2.1/com.github.Ygaray/yahirandroidtaste/v2.4.1/*/yahirandroidtaste-v2.4.1.aar) && W=$(mktemp -d) && unzip -q -o "$B" classes.jar -d $W/base && unzip -q -o build/outputs/aar/yahirandroidtaste-release.aar classes.jar -d $W/head && for v in base head; do javap -public -s -cp $W/$v/classes.jar $(unzip -Z1 $W/$v/classes.jar | grep '\.class$' | grep -v '_Factory\|_MembersInjector' | sed 's/\.class$//; s#/#.#g') 2>/dev/null | awk '/ (class|interface) [^ ]+.*\{$/ { match($0, /(class|interface) [^ <{]+/); s=substr($0, RSTART, RLENGTH); sub(/^(class|interface) /, "", s); cls=s } /descriptor:/ { n=prev; sub(/\(.*/, "", n); k=split(n, a, " "); print cls "#" a[k] $2 } { prev=$0 }' | sort -u > $W/$v.txt; done && test "$(wc -l < $W/base.txt)" -ge 2000 && test "$(wc -l < $W/head.txt)" -ge 2000 && comm -23 $W/base.txt $W/head.txt > $W/all-missing.txt; grep 'ComposableSingletons\$' $W/all-missing.txt > $W/filtered.txt; grep -v 'ComposableSingletons\$' $W/all-missing.txt > $W/missing.txt; echo "base=$(wc -l < $W/base.txt) head=$(wc -l < $W/head.txt) filtered_ComposableSingletons=$(wc -l < $W/filtered.txt) missing=$(wc -l < $W/missing.txt)"; cat $W/filtered.txt; cat $W/missing.txt; test ! -s $W/missing.txt
```

This is its full output after the gradle part. Baseline AAR:
`~/.gradle/caches/modules-2/files-2.1/com.github.Ygaray/yahirandroidtaste/v2.4.1/e574584823b9cd55c67b50baabfc4875b9203fee/yahirandroidtaste-v2.4.1.aar`.

```
base=2526 head=2584 filtered_ComposableSingletons=12 missing=0
io.github.ygaray.yahirandroidtaste.component.ComposableSingletons$ClarificationBarKt#getLambda$-478393069$yahirandroidtaste()Lkotlin/jvm/functions/Function3;
io.github.ygaray.yahirandroidtaste.component.ComposableSingletons$ClarificationBarKt#INSTANCE;Lio/github/ygaray/yahirandroidtaste/component/ComposableSingletons$ClarificationBarKt;
io.github.ygaray.yahirandroidtaste.component.ComposableSingletons$ClarificationBarKt#io.github.ygaray.yahirandroidtaste.component.ComposableSingletons$ClarificationBarKt()V
io.github.ygaray.yahirandroidtaste.component.ComposableSingletons$ModelSelectCardKt#getLambda$1483823158$yahirandroidtaste()Lkotlin/jvm/functions/Function2;
io.github.ygaray.yahirandroidtaste.component.ComposableSingletons$ModelSelectCardKt#INSTANCE;Lio/github/ygaray/yahirandroidtaste/component/ComposableSingletons$ModelSelectCardKt;
io.github.ygaray.yahirandroidtaste.component.ComposableSingletons$ModelSelectCardKt#io.github.ygaray.yahirandroidtaste.component.ComposableSingletons$ModelSelectCardKt()V
io.github.ygaray.yahirandroidtaste.component.ComposableSingletons$OutcomeSheetKt#getLambda$-1916355578$yahirandroidtaste()Lkotlin/jvm/functions/Function2;
io.github.ygaray.yahirandroidtaste.component.ComposableSingletons$OutcomeSheetKt#INSTANCE;Lio/github/ygaray/yahirandroidtaste/component/ComposableSingletons$OutcomeSheetKt;
io.github.ygaray.yahirandroidtaste.component.ComposableSingletons$OutcomeSheetKt#io.github.ygaray.yahirandroidtaste.component.ComposableSingletons$OutcomeSheetKt()V
io.github.ygaray.yahirandroidtaste.component.ComposableSingletons$ProviderKeyCardKt#getLambda$145518132$yahirandroidtaste()Lkotlin/jvm/functions/Function2;
io.github.ygaray.yahirandroidtaste.component.ComposableSingletons$ProviderKeyCardKt#INSTANCE;Lio/github/ygaray/yahirandroidtaste/component/ComposableSingletons$ProviderKeyCardKt;
io.github.ygaray.yahirandroidtaste.component.ComposableSingletons$ProviderKeyCardKt#io.github.ygaray.yahirandroidtaste.component.ComposableSingletons$ProviderKeyCardKt()V
```

The `missing` list is empty, and the final `test ! -s missing.txt` exits 0.

**Baseline sanity:** base=2526 matches the planning-time v2.4.1 count (2526) exactly, and both sides clear the
2000-line floor. That floor rules out a silently failing javap producing an empty base and a false missing=0.
For trajectory: 10 missing before F1c (planning dry run), 8 after the HandledByUiModel spike, and 0 now.
Head grew by 18 lines since the spike (2584 vs 2566). The full measured breakdown of all 70 head-only
lines (`comm -13`) by class is:
- 54 on the six model classes: the restored synthetics plus the Phase 15/16 appended-property members;
- 12 on the six package-private `<Class>$Companion` classes, 2 each: the public `legacyCopyDefault` and the standard synthetic `Companion(DefaultConstructorMarker)`. `javap -public` lists public members even of a package-private class when the class is named explicitly. Neither is consumer-reachable;
- 4 on component `*Kt` facades (`ApproachLadderCard`, `ClarificationBar`, `ModelSelectCard`, `ProviderKeyCard`): their current full-arity composable signatures, which grew label params after v2.4.1. Their v2.4.1 arities are kept by 261005-dmc's hidden overloads.

**Filter justifications:**
- **`ComposableSingletons$*` (12 lines filtered):** these are compiler-generated holders for the library's own non-capturing composable lambdas. Their members are positional (`getLambda$<hash>$<module>`) and churn on unrelated edits. They are referenced only by bytecode compiled in this module; a consumer's own lambdas live in the consumer's own `ComposableSingletons`.
  - The only path by which a consumer could reference a library `ComposableSingletons` is a public `inline` function whose body uses one. `git grep -E '\binline\s+fun' v2.4.1 -- src/main` returns nothing, so no v2.4.1 inline body exists that could leak one.
  - At HEAD, all four `ComposableSingletons$<File>Kt` classes above are gone entirely. That follows from 261005-dmc's restructuring of those files, and it is not consumer ABI.
- **`*_Factory` / `*_MembersInjector` (excluded before diffing):** Dagger/Hilt generates these per compilation, and the consuming app's own Hilt graph regenerates them against the library's `@Inject` constructors. A consumer never links to the library's copies by hand.

## K recipe (feeds F4 docs and F3)

**When to use it:** a data class appended a property after a shipped release R. The compiler then stops
emitting R's default-argument synthetics, because they always track the current full parameter list. A
consumer compiled against R that omitted defaulted args links to those synthetics by exact descriptor and
gets `NoSuchMethodError`. Keep `@JvmOverloads` on the primary and keep the visible no-default legacy
`copy(<R params>)` exactly as they are. Add these members:

**1. Default-ctor shim.** Only needed if R's primary had at least one defaulted param, which means R's AAR has `<init>(…, I, Lkotlin/jvm/internal/DefaultConstructorMarker;)V`.

```kotlin
import kotlin.jvm.internal.DefaultConstructorMarker

@Deprecated("Binary compatibility with v2.4.x", level = DeprecationLevel.HIDDEN)
constructor(
    <R param 0..n-1 with R's exact Kotlin types, NO defaults>,
    mask: Int,
    marker: DefaultConstructorMarker?
) : this(
    p0 = p0,                                                     // required params: pass through
    pI = if (mask and (1 shl I) != 0) <R default for pI> else pI, // each param that had a default in R
    // appended properties: omit -> they take their current default
)
```

**2. `copy$default` shim.** Always needed when R had a data-class `copy`, because the compiler always emits `copy$default` for it.

```kotlin
private companion object {
    @Deprecated("Binary compatibility with v2.4.x", level = DeprecationLevel.HIDDEN)
    @JvmStatic
    @JvmName("copy\$default")
    fun legacyCopyDefault(
        self: Outer,
        <R param 0..n-1: every REFERENCE type made nullable; primitives stay primitive>,
        mask: Int,
        marker: Any?
    ): Outer = self.copy(
        p0 = if (mask and (1 shl 0) != 0) self.p0 else requireNotNull(p0), // non-null in R
        pI = if (mask and (1 shl I) != 0) self.pI else pI,                 // nullable or primitive in R
        appended = self.appended,                                          // EVERY appended prop, from self
    )
}
```

**How masks map to params:**
- Bit `i` corresponds to value parameter `i` in R's declaration order: the ctor counts from its first param; `copy$default` counts from the first param after the `self` receiver.
- **Ctor:** a set bit means "this param took its default". Substitute R's default; never use the current one, because an R-compiled caller expects R's default.
- **`copy$default`:** a set bit means "keep `self.<prop>`".
- The compiler sets bits only for omitted args. A caller may still pass non-null garbage in a masked slot, and the shim must ignore it, which the mixed-mask tests pin.
- With more than 32 params the compiler splits the mask into several `int`s. That does not apply to any of the six classes.

**Pitfalls (each one was hit or ruled out here):**
- **Do not add a defaulted hidden ctor with R's params (R1).** Its no-mask arity clashes with the `@JvmOverloads` overload (`Platform declaration clash`).
- **Do not make the legacy copy hidden with `= this.x` defaults (R).** It drops api.txt lines (lane 3), turns the shipped R-arity ctor and `copy` into `ACC_SYNTHETIC` (a Java-source break), and turns the existing `!isSynthetic` copy-arity pin RED.
- **Make every reference-typed `copy$default` param nullable**, including the required ones. An R caller passes `null` in each defaulted slot, and a non-null Kotlin param would throw at entry. Use `requireNotNull` only on the non-masked branch. Keep primitives primitive (`amended: Boolean`), or the descriptor changes from `Z` to `Ljava/lang/Boolean;`.
- **Ctor `marker` must be typed `DefaultConstructorMarker?`.** `Any?` gives the wrong descriptor. The `copy$default` marker, by contrast, is `Any?` (`Ljava/lang/Object;`).
- **`@Composable () -> Unit` slots:** declare the shim param with the same Kotlin type, `(@Composable () -> Unit)?`. The Compose plugin lowers it to `Function2`, matching R's descriptor. Do not hand-write `Function2`.
- **The companion must be `private`.** The `@JvmStatic` bridge on the outer class stays `public static synthetic`, while the `Companion` field stays private, so nothing reaches api.txt or Kotlin callers. A data class can have only one companion. If one ever needs a public companion, put the shim in it, still `@Deprecated(HIDDEN)`.
- **Nested classes** (such as `VoiceOutcomeUiState.Failure`): put both shims inside the nested class, not the outer sealed interface.
- **No-default primaries** (`UndoRowUiModel`, `FailureActionUiModel`): R shipped no DCM ctor, so add only the `copy$default` shim. Check R's AAR with `javap -public -s` before adding a ctor shim that never existed.
- **Flags:** the `copy$default` bridge comes out `0x1019`, carrying an extra `ACC_FINAL` compared with R's `0x1009`. That is irrelevant for `invokestatic`. The ctor shim comes out `0x1001`, identical to R.
- **Semantic-drift risk:** the mask is decoded by hand, so a wrong bit or a forgotten `self.<appended>` compiles and silently misbehaves. Every shim needs the four-mask test set: all-defaults, single-slot, all-bits and mixed-with-garbage, run against a fully non-default original.
- **Gate:** run the full-AAR `javap -public -s` diff against the cached release AAR, as in the section above. apiCheck and api.txt cannot see synthetics at all.

## D-03 follow-up for the orchestrator

R was not rolled out. The measured reason is in `261005-e2e-SPIKE.md` §Q2: with `@JvmOverloads` kept it fails to
compile, and with `@JvmOverloads` dropped it causes a 6-line api.txt removal (lane 3), a Java-source break and a
red pin. The proposed replacement wording for Phase 17 D-03's data-class bullet is in SPIKE §Recommendation
(the block beginning **"Data classes (appended properties):"**), and the K recipe above is its long form. I did
not edit Phase 17 files.

Note: `.planning/phases/17-*/17-CONTEXT.md` showed up as modified in the working tree during this run. I did
not make that change (presumably the orchestrator's concurrent D-03 edit). I left it unstaged and untouched.

## Deviations from plan

- **[Additive test strength] Mixed-mask assertions.** Each behavioural test also invokes the shims with deliberately wrong values in masked slots. That strengthens ruling condition 1 by pinning the bit mapping, not just the all-defaults and all-bits extremes. It is test-only and changes no production behaviour.
- **[Format] One test line wrapped.** The `UndoRowUiModel` source pin was wrapped to stay under 120 columns. One existing test line from commit `ce20c99` is 122 columns. Detekt does not flag it (0 findings), so I left it.
- **Spike via worktree.** Per the SPIKE, the R1/R/K measurements ran in a scratch worktree before this resume, not in `$S` patches on `main`. The landed K patch for HandledByUiModel is byte-equivalent to SPIKE Appendix A.

No auth gates, no Rule 4 escalations, no fix-attempt limits hit.

## Not done (by instruction)

- STATE.md and ROADMAP.md are untouched; the orchestrator owns the docs commit. PLAN, SPIKE and this SUMMARY are uncommitted.
- Phase 17 files, API.md and CLAUDE.md are untouched.
- Nothing was tagged or pushed (`main` is ahead of `origin/main`), and no consumer was repinned.
- Pre-existing dirt was not staged: `.planning/graphs/*`, `.gsd/`, `.oc-audit/`, `docs/superpowers/`, `graphify-out/`.

## Self-Check: PASSED

- FOUND: src/test/java/io/github/ygaray/yahirandroidtaste/model/DataClassBinaryCompatShimTest.kt
- FOUND (git log): 4a0fe6d, 6963d07, ce20c99, 6f93bfd, 3e307bd, 0956d79
- `git rev-list --count cea06a6..HEAD` = 6
