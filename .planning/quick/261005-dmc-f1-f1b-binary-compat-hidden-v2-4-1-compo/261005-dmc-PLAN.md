---
phase: quick-261005-dmc
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/ClarificationBar.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/ModelSelectCard.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCard.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/FailureActionUiModel.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/VoiceBinaryCompatShimTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/VoiceI18nSourceCompatTest.kt
autonomous: true
requirements:
  - INC-2026-10-05-02/F1
  - INC-2026-10-05-02/F1b

estimate:
  tokens: 40000
  raw_tokens: 80000
  tasks: 3
  confidence: high

must_haves:
  truths:
    - 'A consumer binary compiled against v2.4.1 that calls ApproachLadderCard, ClarificationBar, ModelSelectCard or ProviderKeyCard links at HEAD: each exact v2.4.1 JVM descriptor exists as a public static method on its *Kt class in the release AAR (javap diff against the cached v2.4.1 AAR lists none of them as missing)'
    - 'Reflectively invoking each hidden v2.4.1-shaped method with a v2.4-style $default mask renders the component with its English default labels and routes the callbacks to the right slots'
    - 'The hidden overloads are invisible to Kotlin source: every v2.4.0 and v2.5 call shape still compiles and resolves to the current overload without ambiguity'
    - 'FailureActionUiModel("l") { } compiles again and yields role Neutral, and the v2.4.1 JVM constructor (String, Function0) still exists'
    - 'The ComponentRegistry drift guard, detekt (zero baseline) and Metalava apiCheck stay green, and api.txt is either unchanged or changed only additively'
  artifacts:
    - path: src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt
      provides: hidden v2.4.1-signature ApproachLadderCard overload delegating to the current overload
      contains: "DeprecationLevel.HIDDEN"
    - path: src/main/java/io/github/ygaray/yahirandroidtaste/component/ClarificationBar.kt
      provides: hidden v2.4.1-signature ClarificationBar overload delegating to the current overload
      contains: "DeprecationLevel.HIDDEN"
    - path: src/main/java/io/github/ygaray/yahirandroidtaste/component/ModelSelectCard.kt
      provides: hidden v2.4.1-signature ModelSelectCard overload delegating to the current overload
      contains: "DeprecationLevel.HIDDEN"
    - path: src/main/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCard.kt
      provides: hidden v2.4.1-signature ProviderKeyCard overload delegating to the current overload
      contains: "DeprecationLevel.HIDDEN"
    - path: src/test/java/io/github/ygaray/yahirandroidtaste/component/VoiceBinaryCompatShimTest.kt
      provides: per-composable exact-descriptor reflection checks plus render-through-the-shim Robolectric tests
      contains: "Composer::class.java"
    - path: src/main/java/io/github/ygaray/yahirandroidtaste/model/FailureActionUiModel.kt
      provides: explicit two-argument secondary constructor that restores the v2.4.0 trailing-lambda shape
      contains: "constructor(label: String, onClick: () -> Unit)"
    - path: src/test/java/io/github/ygaray/yahirandroidtaste/component/VoiceI18nSourceCompatTest.kt
      provides: v2.4.0 trailing-lambda FailureActionUiModel call-shape test
      contains: 'FailureActionUiModel("l") {'
  key_links:
    - from: each hidden v2.4.1 overload
      to: the current (v2.5) overload of the same composable
      via: named-argument expression-body delegation
      pattern: "level = DeprecationLevel.HIDDEN"
    - from: src/test/java/io/github/ygaray/yahirandroidtaste/component/VoiceBinaryCompatShimTest.kt
      to: the ApproachLadderCardKt / ClarificationBarKt / ModelSelectCardKt / ProviderKeyCardKt file-facade classes
      via: Class.forName plus getMethod with the exact v2.4.1 parameter types followed by Composer, Int, Int
      pattern: "Class.forName"
    - from: build/outputs/aar/yahirandroidtaste-release.aar
      to: the cached v2.4.1 AAR (the JitPack artifact in ~/.gradle/caches)
      via: javap -public -s descriptor diff (comm -23 baseline head)
      pattern: "comm -23"
---

<objective>
Restore v2.4.x binary compatibility for the four Phase-15 re-signatured composables (F1), and restore
v2.4.0 source compatibility for the `FailureActionUiModel` trailing-lambda constructor (F1b), before
Phase 17 executes and before the v2.5.0 tag. Source: the control-plane fix plan
`2026-10-05-yat-binary-compat-fix-plan.md` (INC-2026-10-05-02), sections F1 and F1b only.

Purpose: Phase 15 appended label parameters to `ApproachLadderCard`, `ClarificationBar`,
`ModelSelectCard` and `ProviderKeyCard` in place. That changed each composable's single JVM method
descriptor `(…, Composer, int $changed, int $default)V`, so a consumer compiled against v2.4.x and run
against v2.5 fails with `NoSuchMethodError`. Phase 16's `@JvmOverloads` constructor made the v2.4.0
`FailureActionUiModel("l") { }` shape stop compiling, because the trailing lambda now binds to `role`.

Output: four `DeprecationLevel.HIDDEN` v2.4.1-signature overloads, an explicit two-argument
`FailureActionUiModel` secondary constructor, a new `VoiceBinaryCompatShimTest`, an extended
`VoiceI18nSourceCompatTest`, and a javap descriptor diff against the real v2.4.1 artifact recorded in
SUMMARY.

OUT OF SCOPE (do not touch): F2 (Phase 17 router shim, folded into Phase 17's replan), F3
(`tools/verify-binary-abi.sh`), F4 (API.md / CLAUDE.md / PATTERNS docs), and every file under
`.planning/phases/17-*`. Also out of scope: the data-class synthetic gap described in Task 3. Record
it, do not fix it. Commit on `main`, sequentially, with no worktree. Do NOT push, tag, or repin any
consumer: shipping is human-gated per root `CLAUDE.md`.
</objective>

<execution_context>
@~/.claude/gsd-core/workflows/execute-plan.md
@~/.claude/gsd-core/templates/summary.md
</execution_context>

<context>
@./CLAUDE.md
@.planning/STATE.md
@~/Projects/yahir-agentic-tools/yahir-gsd-control-plane/docs/plans/2026-10-05-yat-binary-compat-fix-plan.md
@src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt
@src/main/java/io/github/ygaray/yahirandroidtaste/component/ClarificationBar.kt
@src/main/java/io/github/ygaray/yahirandroidtaste/component/ModelSelectCard.kt
@src/main/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCard.kt
@src/main/java/io/github/ygaray/yahirandroidtaste/model/FailureActionUiModel.kt
@src/test/java/io/github/ygaray/yahirandroidtaste/component/VoiceI18nSourceCompatTest.kt
@src/test/java/io/github/ygaray/yahirandroidtaste/component/ClarificationBarTest.kt
@src/test/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCardTest.kt

<interfaces>
v2.4.1 Kotlin signatures. These are the exact params and defaults the hidden overloads must declare,
verified with `git show v2.4.1:src/main/java/io/github/ygaray/yahirandroidtaste/component/<X>.kt`:

  ApproachLadderCard(ladder: List<ApproachRungUiModel>, offlineOnly: Boolean? = null,
      onOfflineOnlyChange: ((Boolean) -> Unit)? = null, maxTierId: String? = null,
      onMaxTierChange: ((String) -> Unit)? = null, modifier: Modifier = Modifier)
  ClarificationBar(question: String, options: List<ClarificationOptionUiModel>,
      onSelect: (String) -> Unit, onDismiss: () -> Unit, modifier: Modifier = Modifier)
  ModelSelectCard(models: List<ModelOptionUiModel>, selectedModelId: String?,
      onModelSelected: (String) -> Unit, emptyReason: String, modifier: Modifier = Modifier)
  ProviderKeyCard(providers: List<ProviderOptionUiModel>, selectedProviderId: String?,
      onProviderSelected: (String) -> Unit, keyValue: String, onKeyChange: (String) -> Unit,
      keyState: KeyFieldState, keyLabel: String, modifier: Modifier = Modifier,
      emptyProvidersReason: String = "No providers configured yet")

HEAD signatures are the same params followed by appended English label params:
ApproachLadderCard +unavailableLabel, cappedLabel, needsNetworkLabel, onlineLabel, offlineOnlyLabel;
ClarificationBar +dismissLabel; ModelSelectCard +modelLabel; ProviderKeyCard +providerLabel.

v2.4.1 JVM descriptors. These were read with `javap -public -s` from the real JitPack v2.4.1 AAR,
cached at `~/.gradle/caches/modules-2/files-2.1/com.github.Ygaray/yahirandroidtaste/v2.4.1/*/yahirandroidtaste-v2.4.1.aar`.
All four are missing from the current `build/outputs/aar/yahirandroidtaste-release.aar`, which was
measured at planning time:

  ApproachLadderCardKt.ApproachLadderCard (Ljava/util/List;Ljava/lang/Boolean;Lkotlin/jvm/functions/Function1;Ljava/lang/String;Lkotlin/jvm/functions/Function1;Landroidx/compose/ui/Modifier;Landroidx/compose/runtime/Composer;II)V
  ClarificationBarKt.ClarificationBar (Ljava/lang/String;Ljava/util/List;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function0;Landroidx/compose/ui/Modifier;Landroidx/compose/runtime/Composer;II)V
  ModelSelectCardKt.ModelSelectCard (Ljava/util/List;Ljava/lang/String;Lkotlin/jvm/functions/Function1;Ljava/lang/String;Landroidx/compose/ui/Modifier;Landroidx/compose/runtime/Composer;II)V
  ProviderKeyCardKt.ProviderKeyCard (Ljava/util/List;Ljava/lang/String;Lkotlin/jvm/functions/Function1;Ljava/lang/String;Lkotlin/jvm/functions/Function1;Lio/github/ygaray/yahirandroidtaste/model/KeyFieldState;Ljava/lang/String;Landroidx/compose/ui/Modifier;Ljava/lang/String;Landroidx/compose/runtime/Composer;II)V
  FailureActionUiModel.<init> (Ljava/lang/String;Lkotlin/jvm/functions/Function0;)V   (present at HEAD via the JvmOverloads annotation; must STAY present after F1b)

Compose `$default` mask: bit i is set when value parameter i (0-based, counting every value parameter,
defaulted or not) uses its default, and the caller then passes null for that slot. A v2.4-compiled
caller that omits trailing defaults executes exactly that path, so the render tests MUST use it.

FailureActionUiModel (HEAD): `data class FailureActionUiModel` with the JvmOverloads annotation on its
primary constructor (label, onClick, role = ActionButtonDefaults.ActionButtonRole.Neutral), plus a
hand-written legacy `copy(label, onClick)` that keeps the current role. v2.4.1 was the plain
`data class FailureActionUiModel(val label: String, val onClick: () -> Unit)`.

Test harness convention (ClarificationBarTest, ApproachLadderCardTest):
`@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [35])`,
`@get:Rule val composeTestRule = createComposeRule()`, one `setContent` per test. Label assertions use
`onAllNodesWithText(..., useUnmergedTree = true)` (ModelSelectCardTest, ProviderKeyCardTest). The
ApproachLadderCard offline toggle is found by content description "Offline only, not selected"
(ApproachLadderCardTest line ~88).

The drift guard (explorer/ComponentRegistryDriftGuardTest) scans source text for a column-0
`@Composable` line followed by a public top-level `fun Name(` and collects names into a Set. The
hidden overloads reuse already-registered names, so no registry change is needed. Keep `@Composable`
on its own column-0 line directly above the `fun` line, with `@Deprecated` ABOVE it. This mirrors
the existing `@OptIn` then `@Composable` convention.

Pre-commit hook (.git/hooks/pre-commit to tools/classify-hub-change.sh against the latest `v*` tag,
v2.4.1): Phases 15 and 16 committed with `HUB_LANE_OVERRIDE=2`, matching the hook-detected lane.
Note INC-2026-10-05-02: the hook's raw-line api.txt check is currently inert, so Metalava apiCheck and
the javap diff below are the real gates for this task.
</interfaces>
</context>

<tasks>

<task type="tracer" tdd="true">
  <name>Task 1 (tracer): ApproachLadderCard v2.4.1 hidden overload, end-to-end from source to the release-AAR descriptor</name>
  <files>src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt, src/test/java/io/github/ygaray/yahirandroidtaste/component/VoiceBinaryCompatShimTest.kt</files>
  <behavior>
    - Reflection: Class.forName("io.github.ygaray.yahirandroidtaste.component.ApproachLadderCardKt").getMethod("ApproachLadderCard", List::class.java, Boolean::class.javaObjectType, Function1::class.java, String::class.java, Function1::class.java, Modifier::class.java, Composer::class.java, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType) succeeds and the method is public and static. RED before the shim: NoSuchMethodException.
    - Render through the shim with the v2.4 default-mask path: inside setContent, invoke the method with ladder [Cloud(rank 3, offlineCapable false), Hybrid(rank 2, true), Local(rank 1, true)], offlineOnly=false, onOfflineOnlyChange = a recorder, null for maxTierId, onMaxTierChange and modifier, then currentComposer, $changed 0, $default 0b111000 (=56, bits 3-5). The rung labels render, the English default offline-only affordance (content description "Offline only, not selected") exists, and clicking it records true.
    - Delegate-drift pin: in the SAME composition (a Column), a normal call to the CURRENT full signature with every parameter named (all eleven, Spanish labels such as offlineOnlyLabel "Solo sin conexión", a distinct rung label such as "Nube") renders the custom label. The English "Offline only" affordance exists exactly once, coming from the shim path only.
    - Do NOT assert the total number of ApproachLadderCard overloads: Phase 17 (F2) may add more shims.
  </behavior>
  <action>
    Implements fix-plan F1 for the first composable and proves the whole pattern end-to-end (compiles without ambiguity, drift guard tolerates it, the descriptor ships in the release AAR) before Task 2 repeats it.

    RED: create src/test/java/io/github/ygaray/yahirandroidtaste/component/VoiceBinaryCompatShimTest.kt using the Robolectric harness from the interfaces block. Add a class KDoc stating that it is the binary-level evidence for INC-2026-10-05-02 F1: each hidden v2.4.1 overload's exact JVM descriptor exists and renders through to the current overload. Write the ApproachLadderCard test per the behavior block. Import java.lang.reflect.Modifier under an alias (for example JModifier) to avoid clashing with androidx.compose.ui.Modifier. Import androidx.compose.runtime.Composer and androidx.compose.runtime.currentComposer, and pass currentComposer as the Composer argument of Method.invoke inside the setContent lambda. Reference the facade class only by its string name, because Kotlin source cannot name a *Kt class directly. Run the test and confirm it FAILS with NoSuchMethodException, then commit that failing test as its own commit, with the message test(261005-dmc): add failing binary-compat shim test for ApproachLadderCard.

    GREEN: in ApproachLadderCard.kt, immediately after the closing brace of the current public ApproachLadderCard function and before the next declaration, add the hidden shim. Its annotations go on separate column-0 lines: first Deprecated with the message "Binary compatibility with v2.4.x" and level = DeprecationLevel.HIDDEN, then Composable directly above the fun line. Declare exactly the six v2.4.1 parameters with the same names, types, order and defaults listed in the interfaces block. Use an expression body that calls ApproachLadderCard with all six arguments NAMED (ladder = ladder, …, modifier = modifier). Named binding is what keeps the delegate correct when Phase 17 appends router params. Do not add OptIn, do not change the current overload, and do not reference the shim from API.md. Precede it with a short comment or KDoc of 2-3 lines saying: v2.4.x binary-compatibility shim (INC-2026-10-05-02 F1); restores the pre-v2.5 JVM descriptor for consumers compiled against v2.4.x; hidden from Kotlin and Java source, so do not call it or document it as API.

    Re-run the test (now GREEN) plus ApproachLadderCardTest and ComponentRegistryDriftGuardTest. Then run assembleRelease and confirm with javap that the v2.4.1 descriptor is present in the release AAR (see verify).

    Commit with message fix(261005-dmc): restore v2.4.x binary ABI for ApproachLadderCard via hidden overload. Let the pre-commit hook classify the commit. If it reports lane 2, re-commit with HUB_LANE_OVERRIDE=2, the hook-detected lane, as Phases 15 and 16 did. If it reports lane 3, STOP and report: that means an api line was removed, which this task must never do. Never use --no-verify.
  </action>
  <verify>
    <automated>cd /home/yahir/Projects/Reusable/android/yahirandroidtaste && ./gradlew testDebugUnitTest --tests 'io.github.ygaray.yahirandroidtaste.component.VoiceBinaryCompatShimTest' --tests 'io.github.ygaray.yahirandroidtaste.component.ApproachLadderCardTest' --tests 'io.github.ygaray.yahirandroidtaste.explorer.ComponentRegistryDriftGuardTest' assembleRelease && W=$(mktemp -d) && unzip -q -o build/outputs/aar/yahirandroidtaste-release.aar classes.jar -d $W && javap -public -s -cp $W/classes.jar io.github.ygaray.yahirandroidtaste.component.ApproachLadderCardKt | grep -F '(Ljava/util/List;Ljava/lang/Boolean;Lkotlin/jvm/functions/Function1;Ljava/lang/String;Lkotlin/jvm/functions/Function1;Landroidx/compose/ui/Modifier;Landroidx/compose/runtime/Composer;II)V'</automated>
  </verify>
  <done>VoiceBinaryCompatShimTest's ApproachLadderCard test was committed failing (RED), then passes after the shim. ApproachLadderCardTest and the drift guard stay green. The release AAR's ApproachLadderCardKt carries the exact v2.4.1 descriptor alongside the current one. The shim delegates with named args, and the commit landed through the pre-commit hook without --no-verify.</done>
</task>

<task type="auto" tdd="true">
  <name>Task 2: Hidden v2.4.1 overloads for ClarificationBar, ModelSelectCard and ProviderKeyCard, with render-through tests</name>
  <files>src/main/java/io/github/ygaray/yahirandroidtaste/component/ClarificationBar.kt, src/main/java/io/github/ygaray/yahirandroidtaste/component/ModelSelectCard.kt, src/main/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCard.kt, src/test/java/io/github/ygaray/yahirandroidtaste/component/VoiceBinaryCompatShimTest.kt</files>
  <behavior>
    - ClarificationBar: getMethod with (String, List, Function1, Function0, Modifier, Composer, int, int) on ClarificationBarKt succeeds. Invoke it with question "Which list?", options [Groceries], onSelect and onDismiss recorders, null modifier, and $default 0b10000 (=16, bit 4). The question renders, plus the English default "Dismiss" text. Clicking "Dismiss" sets the dismissed flag and leaves the selected recorder null, which proves onSelect and onDismiss were not swapped. In the same Column, render the current full signature, all named, with question "¿Qué lista?" and dismissLabel "Descartar". "Descartar" exists, and "Dismiss" exists exactly once.
    - ModelSelectCard: getMethod with (List, String, Function1, String, Modifier, Composer, int, int) on ModelSelectCardKt succeeds. Invoke it with models [GPT-4 (id gpt-4), Claude (id claude)], selectedModelId "gpt-4", emptyReason "No models", null modifier, and $default 16. The selected "GPT-4" renders, and the English default "Model" label exists, checked with useUnmergedTree = true. In the same Column, render the current full signature, all named, with selectedModelId "claude" and modelLabel "Modelo". "Modelo" exists, and the exact-text "Model" exists exactly once.
    - ProviderKeyCard: getMethod with (List, String, Function1, String, Function1, KeyFieldState, String, Modifier, String, Composer, int, int) on ProviderKeyCardKt succeeds. Invoke it with providers [OpenAI (openai), Anthropic (anthropic)], selectedProviderId "openai", keyValue "", an onKeyChange recorder, KeyFieldState.Empty, keyLabel "API key", null for modifier and emptyProvidersReason, and $default 0b110000000 (=384, bits 7-8). "OpenAI", "API key" and the English default "Provider" label render, checked with useUnmergedTree = true. In the same Column, render the current full signature, all named, with providerLabel "Proveedor", keyLabel "Clave API" and emptyProvidersReason "Sin proveedores". "Proveedor" exists, and "Provider" exists exactly once.
    - Same rule as Task 1: no overload-count assertions.
  </behavior>
  <action>
    Implements fix-plan F1 for the remaining three composables, repeating the pattern Task 1 proved.

    RED: add the three tests from the behavior block to VoiceBinaryCompatShimTest, reusing Task 1's reflection and render helper shape. A small private helper that resolves the facade method by class name plus parameter types is fine. Run the tests, confirm all three FAIL with NoSuchMethodException, and commit them as test(261005-dmc): add failing binary-compat shim tests for ClarificationBar, ModelSelectCard, ProviderKeyCard.

    GREEN: in each of ClarificationBar.kt, ModelSelectCard.kt and ProviderKeyCard.kt, add a hidden shim immediately after the current public function's closing brace. Use the same annotation layout and comment as Task 1: Deprecated("Binary compatibility with v2.4.x", level = DeprecationLevel.HIDDEN) on its own column-0 line, then Composable directly above fun. Declare exactly the v2.4.1 parameter list from the interfaces block, preserving that selectedModelId and selectedProviderId have NO default and that emptyProvidersReason defaults to "No providers configured yet". Defaults must match v2.4.1 exactly, because the shim's generated $default handling is what v2.4-compiled callers execute. Use an expression body that delegates with every argument NAMED. Do not add OptIn to the shims: they call no experimental API themselves. Add it only if the compiler actually demands it, and if so note it in SUMMARY. Leave each current overload, its OptIn, and its KDoc untouched.

    If any mask-based render throws (for example an NPE on a null modifier), read the shim's bit layout with javap -c on the debug class under build/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes and correct the mask. Never replace the default-mask call with an all-explicit call, because the default-mask path is the one real v2.4 consumers execute.

    Run the verify. Commit with message fix(261005-dmc): hidden v2.4.x overloads for ClarificationBar, ModelSelectCard, ProviderKeyCard, using the same hook and lane rules as Task 1 (override only with a detected lane 2; lane 3 means STOP; never --no-verify).
  </action>
  <verify>
    <automated>cd /home/yahir/Projects/Reusable/android/yahirandroidtaste && ./gradlew testDebugUnitTest --tests 'io.github.ygaray.yahirandroidtaste.component.VoiceBinaryCompatShimTest' --tests 'io.github.ygaray.yahirandroidtaste.component.ClarificationBarTest' --tests 'io.github.ygaray.yahirandroidtaste.component.ModelSelectCardTest' --tests 'io.github.ygaray.yahirandroidtaste.component.ProviderKeyCardTest' --tests 'io.github.ygaray.yahirandroidtaste.component.VoiceI18nSourceCompatTest' --tests 'io.github.ygaray.yahirandroidtaste.explorer.ComponentRegistryDriftGuardTest' && test "$(grep -l 'level = DeprecationLevel.HIDDEN' src/main/java/io/github/ygaray/yahirandroidtaste/component/{ApproachLadderCard,ClarificationBar,ModelSelectCard,ProviderKeyCard}.kt | wc -l)" = 4</automated>
  </verify>
  <done>All four VoiceBinaryCompatShimTest tests pass, after the three new ones were committed RED. The existing per-component tests, VoiceI18nSourceCompatTest (v2.4.0 positional shapes still resolve with no ambiguity) and the drift guard are green. All four component files carry exactly one HIDDEN v2.4.1 shim with named-argument delegation.</done>
</task>

<task type="auto" tdd="true">
  <name>Task 3: FailureActionUiModel trailing-lambda constructor (F1b), plus full gates and the javap descriptor diff against v2.4.1</name>
  <files>src/main/java/io/github/ygaray/yahirandroidtaste/model/FailureActionUiModel.kt, src/test/java/io/github/ygaray/yahirandroidtaste/component/VoiceI18nSourceCompatTest.kt</files>
  <precondition>The cached v2.4.1 AAR exists at ~/.gradle/caches/modules-2/files-2.1/com.github.Ygaray/yahirandroidtaste/v2.4.1/*/yahirandroidtaste-v2.4.1.aar (present at planning time). If it is missing, download https://jitpack.io/com/github/Ygaray/yahirandroidtaste/v2.4.1/yahirandroidtaste-v2.4.1.aar into a mktemp dir and point B at it.</precondition>
  <behavior>
    - v2.4.0 trailing-lambda shape: FailureActionUiModel("l") { clicked = true } compiles. label == "l", role == ActionButtonDefaults.ActionButtonRole.Neutral, and invoking onClick() flips clicked.
    - Named-plus-trailing shape: FailureActionUiModel(label = "l") { } compiles, and role == Neutral.
    - Positional FailureActionUiModel("l", {}) and named FailureActionUiModel(label = "l", onClick = {}) both have role == Neutral. FailureActionUiModel("l", {}, Destructive) keeps Destructive. The existing legacy-copy assertions stay unchanged and green.
    - JVM: FailureActionUiModel::class.java.getConstructor(String::class.java, Function0::class.java) succeeds. That is the v2.4.1 descriptor (Ljava/lang/String;Lkotlin/jvm/functions/Function0;)V.
  </behavior>
  <action>
    Implements fix-plan F1b, then runs the full gate set for F1 and F1b together.

    RED: in VoiceI18nSourceCompatTest add a test named v240TrailingLambdaActionShape_compilesAndDefaultsNeutral covering the behavior block, and extend the class KDoc with one sentence saying it is also the v2.4.0 trailing-lambda evidence for INC-2026-10-05-02 F1b. Run it and confirm the test sources FAIL TO COMPILE: the lambda binds to role. That compile error is the RED evidence, so copy it into SUMMARY. Commit with message test(261005-dmc): add failing v2.4.0 trailing-lambda FailureActionUiModel shape test. If the hook refuses a commit whose test sources do not compile, keep it uncommitted and record the RED output in SUMMARY instead.

    GREEN, per the fix-plan F1b "explicit secondary constructor" option, in FailureActionUiModel.kt:
    (a) Remove the JvmOverloads annotation and the now-redundant constructor keyword from the primary constructor, keeping all three properties. Keep role's default of ActionButtonDefaults.ActionButtonRole.Neutral on the primary, so the three-argument optional ctor line in api.txt stays identical.
    (b) Inside the class body, add an explicit secondary constructor with parameters label: String and onClick: () -> Unit, delegating via this(label, onClick, ActionButtonDefaults.ActionButtonRole.Neutral).
    (c) Leave the hand-written legacy copy exactly as is.
    (d) Update the class comment to explain the choice. The JvmOverloads annotation would generate the same JVM (String, Function0) constructor, a platform-declaration clash. Its overloads are also invisible to Kotlin overload resolution, which is why the trailing lambda used to bind to role. With an explicit two-argument constructor, a trailing lambda can only match it, and two positional or named args prefer it because it uses no defaults. Both keep the role Neutral.

    Run the targeted test until it is GREEN.

    Full gates, from the repo root: ./gradlew testDebugUnitTest detekt apiCheck assembleRelease. Then run ./gradlew apiDump followed by git diff --exit-code api.txt. The expected result is no change: Metalava omits HIDDEN members, and the ctor list stays the same two lines. If api.txt does change, inspect the diff. Added or reordered lines only: commit api.txt in this task's commit and record the diff in SUMMARY. ANY removed ctor or method line: STOP and report, and do not commit a buried removal. Detekt must stay green at zero baseline. Never regenerate a baseline.

    Binary gate: run the javap descriptor diff command from this task's verify block, which compares every public member of the release AAR with the cached v2.4.1 AAR. It must find none of the four composable descriptors and no FailureActionUiModel constructor missing, and every remaining missing line must be a known model-package data-class synthetic (copy$default or a DefaultConstructorMarker constructor). At planning time that residual was exactly 10 lines: copy$default on FailureActionUiModel, HandledByUiModel, ProposedItemUiModel, UndoRefusedUiModel, UndoRowUiModel and VoiceOutcomeUiState$Failure, plus the DefaultConstructorMarker synthetic constructors of HandledByUiModel, ProposedItemUiModel, UndoRefusedUiModel and VoiceOutcomeUiState$Failure. Do NOT fix the residual: it is outside F1/F1b and contradicts the fix plan's statement that "the data classes need nothing". Record it verbatim in SUMMARY under a heading "Out-of-scope finding for the orchestrator: data-class synthetic ABI gap" so the orchestrator and gsd-technician can decide on a follow-up. ComposableSingletons$* lines are compiler-internal composable-lambda holders that consumers never reference, so the command filters them out. Mention that filter in SUMMARY.

    Commit with message fix(261005-dmc): restore FailureActionUiModel v2.4.0 trailing-lambda constructor, using the same hook and lane rules as Task 1.
  </action>
  <verify>
    <automated>cd /home/yahir/Projects/Reusable/android/yahirandroidtaste && ./gradlew testDebugUnitTest detekt apiCheck assembleRelease && ./gradlew apiDump && git diff --exit-code api.txt && B=$(ls ~/.gradle/caches/modules-2/files-2.1/com.github.Ygaray/yahirandroidtaste/v2.4.1/*/yahirandroidtaste-v2.4.1.aar) && W=$(mktemp -d) && unzip -q -o "$B" classes.jar -d $W/base && unzip -q -o build/outputs/aar/yahirandroidtaste-release.aar classes.jar -d $W/head && for v in base head; do javap -public -s -cp $W/$v/classes.jar $(unzip -Z1 $W/$v/classes.jar | grep '\.class$' | grep -v '_Factory\|_MembersInjector' | sed 's/\.class$//; s#/#.#g') 2>/dev/null | awk '/ (class|interface) [^ ]+.*\{$/ { match($0, /(class|interface) [^ <{]+/); s=substr($0, RSTART, RLENGTH); sub(/^(class|interface) /, "", s); cls=s } /descriptor:/ { n=prev; sub(/\(.*/, "", n); k=split(n, a, " "); print cls "#" a[k] $2 } { prev=$0 }' | sort -u > $W/$v.txt; done && comm -23 $W/base.txt $W/head.txt | grep -v 'ComposableSingletons\$' > $W/missing.txt; cat $W/missing.txt; ! grep -E 'component\.(ApproachLadderCard|ClarificationBar|ModelSelectCard|ProviderKeyCard)Kt#|model\.FailureActionUiModel#io\.github' $W/missing.txt && test "$(grep -vcE 'model\.[A-Za-z$]+#(copy\$default\(|io\.github\.[^(]+\(.*Lkotlin/jvm/internal/DefaultConstructorMarker;\)V)' $W/missing.txt)" = 0</automated>
  </verify>
  <done>The trailing-lambda test was observed RED (a compile failure) and is now GREEN. testDebugUnitTest, detekt and apiCheck are green. api.txt is unchanged, or changed additively with the diff recorded. The javap gate passes: none of the four v2.4.1 composable descriptors and no FailureActionUiModel constructor are missing at HEAD, and the residual is only known data-class synthetics, recorded verbatim in SUMMARY as an out-of-scope finding. The commit landed through the hook.</done>
</task>

</tasks>

<threat_model>
## Trust Boundaries

| Boundary | Description |
|----------|-------------|
| library binary → consumer binary | Consumer bytecode compiled against v2.4.x links against v2.5 classes by exact JVM descriptor. A missing descriptor crashes the consumer at runtime (NoSuchMethodError). |
| library source → consumer source | Consumer Kotlin call shapes from v2.4.0 must still compile (the F1b trailing lambda). |
| pre-commit lane classifier → developer override | HUB_LANE_OVERRIDE can wave through a non-additive change. |

## STRIDE Threat Register

| Threat ID | Category | Component | Severity | Disposition | Mitigation Plan |
|-----------|----------|-----------|----------|-------------|-----------------|
| T-dmc-01 | D (Denial of service) | JVM descriptors of ApproachLadderCard, ClarificationBar, ModelSelectCard, ProviderKeyCard | high | mitigate | HIDDEN v2.4.1-signature overloads (Tasks 1-2), gated by the javap diff against the real cached v2.4.1 AAR (Task 3 verify). |
| T-dmc-02 | T (Tampering: semantic drift) | the shims' delegation into the current overloads | medium | mitigate | Named-argument delegation, plus Robolectric tests that invoke each shim through the v2.4 $default-mask path and assert English defaults and callback routing (onSelect vs onDismiss, offline toggle). |
| T-dmc-03 | T (Tampering: source break) | FailureActionUiModel constructor | medium | mitigate | Explicit two-arg secondary constructor, plus a v2.4.0 trailing-lambda test and a getConstructor(String, Function0) assertion. |
| T-dmc-04 | E (guard bypass) | pre-commit hook and lane override | medium | mitigate | Override only with the hook-detected lane 2. Lane 3 means STOP. Never use --no-verify. Metalava apiCheck and the apiDump diff are the source-level gate (the raw-line api.txt guard is inert per INC-2026-10-05-02). |
| T-dmc-05 | D (Denial of service) | data-class synthetic members (copy$default, DefaultConstructorMarker constructors) missing vs v2.4.1 | medium | transfer | Out of F1/F1b scope. Recorded verbatim in SUMMARY and handed to the orchestrator as a follow-up candidate. |
| T-dmc-06 | I/E (invariant) | one-way dependency | low | mitigate | Shims reference only library, Compose and Kotlin types. No consumer import and no new dependency. |
| T-dmc-SC | T (supply chain) | dependencies | low | accept | No packages installed or added. javap and unzip are JDK and system tools, and the baseline AAR is the already-resolved JitPack artifact. |
</threat_model>

<verification>
Run from the repo root /home/yahir/Projects/Reusable/android/yahirandroidtaste:

1. ./gradlew testDebugUnitTest detekt apiCheck assembleRelease. All must be green.
2. ./gradlew apiDump, then git diff --exit-code api.txt. Expected unchanged. Any removed line means STOP.
3. The javap descriptor diff from Task 3's verify block (release AAR vs the cached v2.4.1 AAR). No
   composable or FailureActionUiModel constructor line may be missing, and the residual must be only
   known model data-class synthetics.
4. git log --oneline -8 shows the RED test commits and the fix commits on main, with no tag created
   (git tag --points-at HEAD is empty) and nothing pushed.
5. No file under .planning/phases/17-* changed (git diff --stat HEAD~N -- .planning/phases is empty for
   this task's commits), and API.md and CLAUDE.md are untouched.
</verification>

<success_criteria>
- The four v2.4.1 composable JVM descriptors exist at HEAD in the release AAR (javap proof recorded in
  SUMMARY next to the v2.4.1 baseline lines).
- Every shim is HIDDEN, declares exact v2.4.1 params and defaults, delegates with named args, and is
  exercised by a reflective render-through test using the v2.4 $default path.
- `FailureActionUiModel("l") { }` compiles with role Neutral, and the JVM (String, Function0) constructor persists.
- testDebugUnitTest, detekt (zero baseline) and apiCheck are green. api.txt is unchanged or changed
  additively only.
- The data-class synthetic residual is recorded, unfixed, as an out-of-scope finding for the orchestrator.
</success_criteria>

<output>
Create `.planning/quick/261005-dmc-f1-f1b-binary-compat-hidden-v2-4-1-compo/261005-dmc-SUMMARY.md` when done. It must include:
- A descriptor table with, for each of the 5 members (4 composables plus the FailureActionUiModel 2-arg ctor), the v2.4.1 baseline descriptor and the HEAD descriptor from `javap -public -s` on the release AAR. Also list the current (v2.5) composable descriptors, to show both coexist.
- The RED evidence for each task (the NoSuchMethodException output, and the F1b compile error).
- The pre-commit lane the hook detected for each commit, and whether the HUB_LANE_OVERRIDE matched it.
- The api.txt apiDump result (unchanged, or the additive diff).
- The section "Out-of-scope finding for the orchestrator: data-class synthetic ABI gap" with the verbatim residual lines from missing.txt and a note that the ComposableSingletons$* holders were filtered as compiler-internal.
</output>
