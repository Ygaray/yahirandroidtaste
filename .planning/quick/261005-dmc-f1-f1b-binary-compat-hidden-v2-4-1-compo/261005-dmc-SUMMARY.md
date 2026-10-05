---
phase: quick-261005-dmc
plan: 01
status: complete
subsystem: voice-components / binary-compat
tags: [binary-compat, abi, deprecated-hidden, jvm-descriptor, INC-2026-10-05-02]
requires: []
provides:
  - "HIDDEN v2.4.1-signature overloads for ApproachLadderCard, ClarificationBar, ModelSelectCard, ProviderKeyCard"
  - "FailureActionUiModel explicit (label, onClick) secondary constructor (v2.4.0 trailing-lambda shape)"
affects:
  - "Phase 17 (F2 router shim will add further overloads; no overload-count assertions were added)"
tech-stack:
  added: []
  patterns:
    - "@Deprecated(level = HIDDEN) + @Composable overload with exact old params/defaults, named-arg expression-body delegation"
    - "Reflective exact-descriptor + v2.4 $default-mask render-through Robolectric test"
key-files:
  created:
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/VoiceBinaryCompatShimTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/ClarificationBar.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/ModelSelectCard.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCard.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/model/FailureActionUiModel.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/VoiceI18nSourceCompatTest.kt
decisions:
  - "Shims need no @OptIn (compiler did not demand it)"
  - "F1b rationale comment placed inside the class body above the secondary ctor (not between KDoc and declaration, which would detach the KDoc)"
  - "Data-class synthetic ABI residual (10 lines) recorded, NOT fixed — out of F1/F1b scope"
metrics:
  started: 2026-10-05T15:59:39Z
  completed: 2026-10-05T16:06:04Z
  duration: ~7m
plan_head_before: 559810ca762e98ec99f3d3848685d302fc8ab772
actuals:
  tokens: 5800
  tasks: 3
  commits: 6
---

# Quick 261005-dmc Plan 01: F1 + F1b binary/source compat Summary

Four `DeprecationLevel.HIDDEN` v2.4.1-signature composable overloads (named-arg delegation into the
current overloads) restore the exact v2.4.1 JVM descriptors in the release AAR, and an explicit
`FailureActionUiModel(label, onClick)` secondary constructor replaces `@JvmOverloads` so the v2.4.0
`FailureActionUiModel("l") { }` shape compiles again with role Neutral — api.txt unchanged.

## Commits

| # | Task | Commit | Type | Hook lane | Override |
|---|------|--------|------|-----------|----------|
| 1 | T1 RED  — ApproachLadderCard shim test | `812a63b` | test | LANE 1 | none needed |
| 2 | T1 GREEN — ApproachLadderCard hidden overload | `07f66cf` | fix | LANE 1 | none needed |
| 3 | T2 RED  — ClarificationBar/ModelSelectCard/ProviderKeyCard shim tests | `39fa2db` | test | LANE 1 | none needed |
| 4 | T2 GREEN — three hidden overloads | `4025356` | fix | LANE 1 | none needed |
| 5 | T3 RED  — v2.4.0 trailing-lambda test (non-compiling) | `910296a` | test | LANE 1 | none needed |
| 6 | T3 GREEN — FailureActionUiModel secondary ctor | `2d803be` | fix | LANE 2 (first attempt BLOCKED) | `HUB_LANE_OVERRIDE=2` — matches detected lane |

Lane 2 on commit 6 = classifier rule "api append-only BUT a pre-existing source line changed" (the
`data class FailureActionUiModel @JvmOverloads constructor(` declaration line). No commit was ever
classified lane 3. `--no-verify` was never used. The pure-additive shims (commits 2 and 4) were
lane 1, so no override was required there.

## RED evidence

**Task 1** (`VoiceBinaryCompatShimTest.approachLadderCard_…`):
```
java.lang.NoSuchMethodException: io.github.ygaray.yahirandroidtaste.component.ApproachLadderCardKt.ApproachLadderCard(java.util.List,java.lang.Boolean,kotlin.jvm.functions.Function1,java.lang.String,kotlin.jvm.functions.Function1,androidx.compose.ui.Modifier,androidx.compose.runtime.Composer,int,int)
```

**Task 2** (3 of 4 tests failed; ApproachLadderCard already green):
```
java.lang.NoSuchMethodException: io.github.ygaray.yahirandroidtaste.component.ClarificationBarKt.ClarificationBar(java.lang.String,java.util.List,kotlin.jvm.functions.Function1,kotlin.jvm.functions.Function0,androidx.compose.ui.Modifier,androidx.compose.runtime.Composer,int,int)
java.lang.NoSuchMethodException: io.github.ygaray.yahirandroidtaste.component.ModelSelectCardKt.ModelSelectCard(java.util.List,java.lang.String,kotlin.jvm.functions.Function1,java.lang.String,androidx.compose.ui.Modifier,androidx.compose.runtime.Composer,int,int)
java.lang.NoSuchMethodException: io.github.ygaray.yahirandroidtaste.component.ProviderKeyCardKt.ProviderKeyCard(java.util.List,java.lang.String,kotlin.jvm.functions.Function1,java.lang.String,kotlin.jvm.functions.Function1,io.github.ygaray.yahirandroidtaste.model.KeyFieldState,java.lang.String,androidx.compose.ui.Modifier,java.lang.String,androidx.compose.runtime.Composer,int,int)
```

**Task 3 (F1b)** — test sources failed to compile; the lambda bound to `role` (the hook accepted the
commit, so the RED test is committed as `910296a`):
```
e: …/VoiceI18nSourceCompatTest.kt:168:45 No value passed for parameter 'onClick'.
e: …/VoiceI18nSourceCompatTest.kt:168:50 Argument type mismatch: actual type is '() -> Unit', but 'ActionButtonDefaults.ActionButtonRole' was expected.
e: …/VoiceI18nSourceCompatTest.kt:175:50 No value passed for parameter 'onClick'.
e: …/VoiceI18nSourceCompatTest.kt:175:63 Argument type mismatch: actual type is '() -> Unit', but 'ActionButtonDefaults.ActionButtonRole' was expected.
Execution failed for task ':compileDebugUnitTestKotlin'.
```

## Gate results

| Gate | Result |
|------|--------|
| Task 1 verify (shim + ApproachLadderCardTest + drift guard + assembleRelease + javap grep) | VoiceBinaryCompatShimTest 1/1, ApproachLadderCardTest 21/21, ComponentRegistryDriftGuardTest 1/1; descriptor present |
| Task 2 verify | VoiceBinaryCompatShimTest 4/4, ClarificationBarTest 11/11, ModelSelectCardTest 5/5, ProviderKeyCardTest 8/8, VoiceI18nSourceCompatTest 3/3, drift guard 1/1; HIDDEN in all 4 files |
| Task 3 targeted | VoiceI18nSourceCompatTest 4/4 |
| `./gradlew testDebugUnitTest detekt apiCheck assembleRelease` | BUILD SUCCESSFUL — 74 suites, **728 tests, 0 failures, 0 errors, 22 skipped** |
| detekt | 0 code smells, zero baseline (no baseline regenerated) |
| Metalava apiCheck | pass |
| `apiDump` + `git diff --exit-code api.txt` | **api.txt unchanged** (HIDDEN members omitted by Metalava; ctor list unchanged) |
| javap binary gate (Task 3 verify block) | pass: no composable / FailureActionUiModel ctor missing; residual = only data-class synthetics (10 lines) |
| Tag / push | none (`git tag --points-at HEAD` empty; nothing pushed) |
| Scope | no change to `.planning/phases/*`, `API.md`, `CLAUDE.md`, `api.txt` |

## Descriptor table (javap -public -s; baseline = cached JitPack v2.4.1 AAR, HEAD = build/outputs/aar/yahirandroidtaste-release.aar)

| Member | v2.4.1 baseline descriptor | HEAD |
|--------|---------------------------|------|
| `ApproachLadderCardKt.ApproachLadderCard` | `(Ljava/util/List;Ljava/lang/Boolean;Lkotlin/jvm/functions/Function1;Ljava/lang/String;Lkotlin/jvm/functions/Function1;Landroidx/compose/ui/Modifier;Landroidx/compose/runtime/Composer;II)V` | present (identical, hidden shim) |
| `ClarificationBarKt.ClarificationBar` | `(Ljava/lang/String;Ljava/util/List;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function0;Landroidx/compose/ui/Modifier;Landroidx/compose/runtime/Composer;II)V` | present (identical, hidden shim) |
| `ModelSelectCardKt.ModelSelectCard` | `(Ljava/util/List;Ljava/lang/String;Lkotlin/jvm/functions/Function1;Ljava/lang/String;Landroidx/compose/ui/Modifier;Landroidx/compose/runtime/Composer;II)V` | present (identical, hidden shim) |
| `ProviderKeyCardKt.ProviderKeyCard` | `(Ljava/util/List;Ljava/lang/String;Lkotlin/jvm/functions/Function1;Ljava/lang/String;Lkotlin/jvm/functions/Function1;Lio/github/ygaray/yahirandroidtaste/model/KeyFieldState;Ljava/lang/String;Landroidx/compose/ui/Modifier;Ljava/lang/String;Landroidx/compose/runtime/Composer;II)V` | present (identical, hidden shim) |
| `FailureActionUiModel.<init>` (2-arg) | `(Ljava/lang/String;Lkotlin/jvm/functions/Function0;)V` | present (identical, explicit secondary ctor) |

Current (v2.5) descriptors coexisting at HEAD:

```
ApproachLadderCardKt#ApproachLadderCard(Ljava/util/List;Ljava/lang/Boolean;Lkotlin/jvm/functions/Function1;Ljava/lang/String;Lkotlin/jvm/functions/Function1;Landroidx/compose/ui/Modifier;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Landroidx/compose/runtime/Composer;III)V
ClarificationBarKt#ClarificationBar(Ljava/lang/String;Ljava/util/List;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function0;Landroidx/compose/ui/Modifier;Ljava/lang/String;Landroidx/compose/runtime/Composer;II)V
ModelSelectCardKt#ModelSelectCard(Ljava/util/List;Ljava/lang/String;Lkotlin/jvm/functions/Function1;Ljava/lang/String;Landroidx/compose/ui/Modifier;Ljava/lang/String;Landroidx/compose/runtime/Composer;II)V
ProviderKeyCardKt#ProviderKeyCard(Ljava/util/List;Ljava/lang/String;Lkotlin/jvm/functions/Function1;Ljava/lang/String;Lkotlin/jvm/functions/Function1;Lio/github/ygaray/yahirandroidtaste/model/KeyFieldState;Ljava/lang/String;Landroidx/compose/ui/Modifier;Ljava/lang/String;Ljava/lang/String;Landroidx/compose/runtime/Composer;II)V
FailureActionUiModel#<init>(Ljava/lang/String;Lkotlin/jvm/functions/Function0;Lio/github/ygaray/yahirandroidtaste/component/ActionButtonDefaults$ActionButtonRole;)V
FailureActionUiModel#<init>(Ljava/lang/String;Lkotlin/jvm/functions/Function0;Lio/github/ygaray/yahirandroidtaste/component/ActionButtonDefaults$ActionButtonRole;ILkotlin/jvm/internal/DefaultConstructorMarker;)V
```

(Note: javap prints the hidden shims without generic signatures because the compiler emits them
`ACC_SYNTHETIC`. `javap -v` on `ClarificationBarKt` confirms that the shim's flags are `0x1019 ACC_PUBLIC,
ACC_STATIC, ACC_FINAL, ACC_SYNTHETIC`, while the current overload's are `0x0019`. That is the expected `HIDDEN` shape:
Java source can't see it, but bytecode can still link to it by descriptor.)

## $default masks used by the render-through tests (v2.4-caller path)

| Shim | Mask | Bits = defaulted params |
|------|------|-------------------------|
| ApproachLadderCard | `0b111000` (56) | 3-5: maxTierId, onMaxTierChange, modifier |
| ClarificationBar | `0b10000` (16) | 4: modifier |
| ModelSelectCard | `0b10000` (16) | 4: modifier |
| ProviderKeyCard | `0b110000000` (384) | 7-8: modifier, emptyProvidersReason |

No mask correction was needed; every render passed first time with `null` in the defaulted slots.

## Out-of-scope finding for the orchestrator: data-class synthetic ABI gap

The javap descriptor diff (`comm -23 base head`) of the release AAR against the cached v2.4.1 AAR
leaves exactly these 10 public members missing at HEAD — identical to the planning-time prediction.
They are all compiler-generated data-class synthetics in the `model` package (`copy$default` and the
`DefaultConstructorMarker` default-arg constructors), whose arity changed when Phase 15/16 appended
properties. A consumer compiled against v2.4.x that called `copy(...)` with omitted args or a
constructor with omitted defaulted args links to these and would hit `NoSuchMethodError`. This
contradicts the fix plan's statement that "the data classes need nothing". **Not fixed here** (out
of F1/F1b scope) — for the orchestrator / gsd-technician to decide on a follow-up.

Verbatim `missing.txt`:

```
io.github.ygaray.yahirandroidtaste.model.FailureActionUiModel#copy$default(Lio/github/ygaray/yahirandroidtaste/model/FailureActionUiModel;Ljava/lang/String;Lkotlin/jvm/functions/Function0;ILjava/lang/Object;)Lio/github/ygaray/yahirandroidtaste/model/FailureActionUiModel;
io.github.ygaray.yahirandroidtaste.model.HandledByUiModel#copy$default(Lio/github/ygaray/yahirandroidtaste/model/HandledByUiModel;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;ILjava/lang/Object;)Lio/github/ygaray/yahirandroidtaste/model/HandledByUiModel;
io.github.ygaray.yahirandroidtaste.model.HandledByUiModel#io.github.ygaray.yahirandroidtaste.model.HandledByUiModel(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;ILkotlin/jvm/internal/DefaultConstructorMarker;)V
io.github.ygaray.yahirandroidtaste.model.ProposedItemUiModel#copy$default(Lio/github/ygaray/yahirandroidtaste/model/ProposedItemUiModel;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function2;ILjava/lang/Object;)Lio/github/ygaray/yahirandroidtaste/model/ProposedItemUiModel;
io.github.ygaray.yahirandroidtaste.model.ProposedItemUiModel#io.github.ygaray.yahirandroidtaste.model.ProposedItemUiModel(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function2;ILkotlin/jvm/internal/DefaultConstructorMarker;)V
io.github.ygaray.yahirandroidtaste.model.UndoRefusedUiModel#copy$default(Lio/github/ygaray/yahirandroidtaste/model/UndoRefusedUiModel;Ljava/lang/String;Ljava/lang/String;ILjava/lang/Object;)Lio/github/ygaray/yahirandroidtaste/model/UndoRefusedUiModel;
io.github.ygaray.yahirandroidtaste.model.UndoRefusedUiModel#io.github.ygaray.yahirandroidtaste.model.UndoRefusedUiModel(Ljava/lang/String;Ljava/lang/String;ILkotlin/jvm/internal/DefaultConstructorMarker;)V
io.github.ygaray.yahirandroidtaste.model.UndoRowUiModel#copy$default(Lio/github/ygaray/yahirandroidtaste/model/UndoRowUiModel;Ljava/lang/String;Ljava/lang/String;Lio/github/ygaray/yahirandroidtaste/model/UndoRowState;ILjava/lang/Object;)Lio/github/ygaray/yahirandroidtaste/model/UndoRowUiModel;
io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState$Failure#copy$default(Lio/github/ygaray/yahirandroidtaste/model/VoiceOutcomeUiState$Failure;Ljava/lang/String;Lio/github/ygaray/yahirandroidtaste/model/HandledByUiModel;Lio/github/ygaray/yahirandroidtaste/model/FailureActionUiModel;ILjava/lang/Object;)Lio/github/ygaray/yahirandroidtaste/model/VoiceOutcomeUiState$Failure;
io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState$Failure#io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState$Failure(Ljava/lang/String;Lio/github/ygaray/yahirandroidtaste/model/HandledByUiModel;Lio/github/ygaray/yahirandroidtaste/model/FailureActionUiModel;ILkotlin/jvm/internal/DefaultConstructorMarker;)V
```

Filter note: the diff command drops `ComposableSingletons$*` lines (12 at HEAD vs v2.4.1) — these are
compiler-internal composable-lambda holder classes that consumer bytecode never references, so they
are not ABI. `_Factory` / `_MembersInjector` Hilt-generated classes are also excluded from the scan.

## Deviations from Plan

1. **F1b comment placement (minor, judgment call).** The plan said to "update the class comment".
   The rationale was placed as a `//` block inside the class body directly above the new secondary
   constructor (mirroring the existing legacy-`copy` comment), rather than between the KDoc and the
   `data class` line, where it would detach the consumer-facing KDoc. The KDoc itself is unchanged.
2. **Lane-2 override on commit 6** — expected by the plan's hook rules: the first commit attempt was
   BLOCKED at lane 2 and re-run with `HUB_LANE_OVERRIDE=2`, matching the detected lane.

Otherwise the plan ran as written. No `@OptIn` was needed on any shim, and no `$default` mask had to be corrected.

## TDD Gate Compliance

RED → GREEN observed for every task: test commits `812a63b`, `39fa2db`, `910296a` each precede the
matching fix commits `07f66cf`, `4025356`, `2d803be`. No REFACTOR commits were needed.

## Not done (per constraints)

STATE.md / ROADMAP.md were not updated and docs were not committed (the orchestrator owns the
docs commit). Nothing was tagged or pushed. No Phase 17 files were touched. The pre-existing dirty
`.planning/graphs/*` files and untracked dirs were left unstaged.

## Self-Check: PASSED

- FOUND: src/test/java/io/github/ygaray/yahirandroidtaste/component/VoiceBinaryCompatShimTest.kt
- FOUND commits: 812a63b, 07f66cf, 39fa2db, 4025356, 910296a, 2d803be (measured `git rev-list --count 559810c..HEAD` = 6)
