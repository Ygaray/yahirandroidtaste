# Pitfalls Research

**Domain:** Adding shared, prop-driven "voice-command settings + outcome/confirm" UI to a reusable, domain-agnostic Jetpack Compose design-system library (`yahirandroidtaste`), consumed by two apps (SecondBrain, CalTracker) via immutable JitPack tags
**Researched:** 2026-09-29
**Confidence:** HIGH (grounded in this repo's own live guards, the documented v2.0 `copy()`-ABI break in `TagChipUiModel.kt`, and the cross-repo HANDOFF)

> Scope note: these are the pitfalls specific to THIS milestone (v2.4, Phases 10-13). Generic Compose/Kotlin mistakes are omitted. Every pitfall names the warning sign, the concrete prevention, and the phase that must own it. The library's whole value is reusability + a strictly-additive public API; every pitfall below is a way that value silently erodes.

## Critical Pitfalls

### Pitfall 1: Leaking a domain assumption or noun into a "generic" composable

**What goes wrong:**
A composable is authored to render voice-command state, but its name, a parameter name, a hardcoded string, an enum, or a default value quietly encodes ONE consumer's domain. Examples in this milestone's blast radius: naming the outcome sheet `VoiceCommandResultSheet` (head token `Voice` is a consumer-domain noun per this repo's own drift guard); a `handled-by` indicator whose enum hardcodes `Grammar | SingleShot | Plan | Agentic` (that ladder is the *engine's* vocabulary, not every consumer's); a needs-confirmation prop typed as `riskTier: RiskTier` (SecondBrain's concept — CalTracker has no risk tier, it has a weak-match score); a default label like `"Add note?"` or `"Log entry?"`. Once shipped in a tag it is frozen into the reusable surface and both consumers inherit the coupling.

**Why it happens:**
The author is almost always thinking in the first consumer's mental model (this UI is being extracted *from* the SB/CT need), so domain words feel "natural." The library's whole reusability litmus (`library → Android/AndroidX/Compose/Hilt/Coil/nav/reorderable/osmdroid only, never → a consumer`) is about *imports*, but a noun leak needs no import — it hides in a string or a name and passes compilation clean.

**How to avoid:**
- Run the litmus on every new public name and param: *"Would a third, unrelated consumer read this word as naming THEIR business object?"* If yes, it's a leak.
- This repo enforces it mechanically: `DomainVocabularyDriftGuardTest` scans every public top-level `@Composable` outside `explorer/` and fails the build unless its **head token** (leading PascalCase word) is in `PRIMITIVE_NOUN_ALLOWLIST` or the full name is grandfathered in `DOMAIN_VOCABULARY`. `Voice` is NOT a primitive noun — it's currently only present as *grandfathered domain vocabulary* (`VoiceCard`, `VoiceRenameTagsSheet`). A new `Voice…` composable will go RED. **Decide the naming vocabulary before authoring**: prefer already-allowlisted structural head tokens (`Card`, `Sheet`, `Bar`, `Ladder`, `Badge`, `Field`, `Selector`, `State`, `Confirmation`) — e.g. `CommandOutcomeSheet`, `ApproachLadderCard`, `ProviderKeyCard`, `ConfirmationSheet`. Do NOT reflexively add `Voice`/`Command`/`Approach` to the allowlist to silence the guard — that defeats the guard. If a domain-flavored head token is genuinely unavoidable, add it to `DOMAIN_VOCABULARY` with a rationale (a human-acknowledged coupling), never to `PRIMITIVE_NOUN_ALLOWLIST`.
- Keep every user-facing string a **prop with no domain default** (or a neutral structural default). The `handled-by: tier/approach` indicator must take the tier label as a `String`/model from the caller, never an enum the library defines over the engine's ladder.
- Litmus for parameters: props carry *content + callbacks*, never *concepts*. `reason: String`, `items: List<ConfirmItem>`, `onConfirm`, `onCancel` — not `riskTier`, `mutationKind`, `matchScore`.

**Warning signs:**
- A new composable name whose leading word isn't already in `PRIMITIVE_NOUN_ALLOWLIST`.
- Any string literal in the new composables that a user would read as an app-specific action ("note", "meal", "event", "card", "log").
- An enum or sealed type defined in the library that mirrors the engine's tier ladder or a consumer's confirm taxonomy.
- The `DomainVocabularyDriftGuardTest` going RED (this is the guard doing its job — read the failure, don't allowlist past it).

**Phase to address:**
Phases 10, 11, 12 (every phase that authors a composable — this is a per-composable discipline). Naming vocabulary should be settled at the top of **Phase 10** and reused. Phase 13 is the backstop (full-suite drift guard).

---

### Pitfall 2: Designing the needs-confirmation prop shape too narrowly — it fits ONE consumer's confirm but not the other

**What goes wrong:**
The confirm state is modeled around whichever consumer the author had in mind, so it can't render the other without a library change. Two concrete failure shapes:
- Model it as SecondBrain's `MutationGate`/`VoiceConfirmGate` risk confirm → a single item + a `riskTier` → CalTracker's **batch** weak-match confirm (N proposed items, each with its own accept/reject) has nowhere to live, and its "weak match" reason doesn't map to a risk tier.
- Model it as CalTracker's weak-match single/batch → SB's risk confirm has no per-item batch, but DOES need the *reason/severity* prominence that a plain list doesn't express.

Either way, Phase 12's success criterion #4 ("satisfies BOTH … without any library-side change") fails, and — worse — if it's discovered *after* `v2.4.0` is tagged, the fix is a **new tag**, because the prop shape is frozen public surface.

**Why it happens:**
"Single confirm" is the obvious first case; batch feels like an edge case bolted on later. The two consumers' confirm concepts (risk-tier vs weak-match-score) look different enough that a shared shape seems impossible, so the author picks one and plans to "generalize later" — but later is a breaking change.

**How to avoid:**
- Model the **union as the common denominator, expressed structurally, not semantically**. The shape both need is: *a reason to show the user, one-or-more proposed items, and a confirm/cancel (or per-item accept) decision.* Concretely:
  - `reason: String` (SB puts its risk-tier prose here; CT puts its "low-confidence match" prose here — the library never names either).
  - `items: List<ProposedItem>` where a single confirm is just a list of size 1. **Never** model "single" and "batch" as two different props or two composables — batch is the general case; single is `size == 1`. This is Phase 12 success criterion #2 made structural.
  - Per-item optional fields that both can ignore: a `label`/`supportingText` on each item; optionally a per-item `onAccept`/`onReject` for batch, with a top-level `onConfirmAll`/`onCancel`. SB uses the top-level pair; CT can use per-item.
  - Any distinguishing metadata (SB's risk level, CT's match score) collapses to **presentational** fields the library renders opaquely: an optional `emphasis`/`severity` hint or a per-item trailing slot — the library does not interpret it.
- **Validate against both consumers on paper before authoring** (Phase 12 planning): write the SB `MutationGate` call-site and the CT `VoiceResultSheet` Proposed/ProposedBatch call-site as two hypothetical usages of the SAME signature. If either needs a field the other can't supply, or a field one must leave meaningless, iterate the shape until both fit cleanly. Put both worked call-sites in the phase's CONTEXT/plan as the acceptance evidence.
- Favor **slot APIs over data enums** for the parts that differ: a per-item `trailingContent: @Composable () -> Unit` lets each consumer render its own risk badge / score chip without the library knowing what it is. Slots are the reusability escape hatch that keeps the shape additive AND domain-neutral.
- Make optional fields genuinely optional (nullable / defaulted) so a future third consumer with a different confirm concept can also map onto it.

**Warning signs:**
- The confirm signature mentions "risk", "tier", "score", "match", "mutation" — any word from one consumer.
- Separate `SingleConfirm` and `BatchConfirm` composables or a boolean `isBatch` flag instead of `items: List<…>`.
- You can write the SB call-site OR the CT call-site cleanly, but the other requires passing a dummy/sentinel value.
- The reason for a field is "SB needs it" with no answer to "what does CT pass?"

**Phase to address:**
**Phase 12** (owns this outright — it is the phase's entire reason to exist; success criterion #4 is the gate). Prevention *work* happens in Phase 12 planning: two-consumer paper validation before code.

---

### Pitfall 3: Making the public API non-additive (the `copy()`/data-class ABI break, required params, changed signatures)

**What goes wrong:**
A change that looks additive to the author is a binary-incompatible removal at the JVM/`api.txt` level, so `apiCheck`/Metalava fails — or worse, if a guard misses it, a consumer that pins the new tag fails to link. This repo already hit the canonical version: adding a field to a `data class`'s **primary constructor**. Kotlin regenerates `copy()`/`equals()`/`hashCode()`/`componentN()` over the *full* primary-ctor list as a single non-overloadable signature, so the old-arity `copy()` becomes a `RemovedMethod` — an unfixable ABI break that `@JvmOverloads` **cannot** paper over (it only helps the constructor, never the synthetic members). See `TagChipUiModel.kt`: `color` was ultimately declared as a body `var` *outside* the primary constructor precisely to keep `copy()`/`componentN()` byte-identical. Other forms: adding a required (non-defaulted) parameter to an existing public composable; changing an existing parameter's type or order; renaming a public symbol; tightening a nullable to non-null.

**Why it happens:**
Kotlin's source-level ergonomics ("just add a field, everything still compiles here") hide the ABI consequence — the break is invisible until Metalava or a consumer link surfaces it. Adding a param to an *existing* composable feels cheaper than a new overload. Under milestone time pressure it's tempting to extend a shipped type rather than add a new one.

**How to avoid:**
- **Prefer new public symbols over modifying existing ones.** This milestone is nearly all-new surface (new composables, new models) — keep it that way. Do NOT extend `TagChipUiModel`, `MicButton`, or any shipped type to carry voice state; introduce new models/composables.
- If you must define a public **data class** for the confirm/outcome models, freeze its primary constructor early and add any later field as a **body property with a default**, exactly as `TagChipUiModel.color` was reshaped — OR mark the model non-`data` if `copy()`/`componentN()` aren't part of its contract (avoids the whole synthetic-member ABI surface). Given the CT batch case, prefer a plain class or an interface with slots over a `data class` you'll be tempted to grow.
- Every new param on any composable that a consumer will call must be **defaulted** so call-sites and the ABI stay compatible; use `@JvmOverloads` on constructors for Java-arity compatibility (necessary but NOT sufficient — it does nothing for `data class` synthetic members).
- Run the repo's additive tooling locally before proposing the tag: `tools/verify-api-additive.sh` / `verify-additive-surface.sh` / `verify-additive-diff.sh` and `./gradlew apiCheck` against the `v2.3.0` `api.txt`. This is §11 step 2. The additive pre-commit guard on `src/main` is live — do not bypass it.
- Watch the Compose-stability side effect the `TagChipUiModel` KDoc documents: a public model containing any `var` becomes non-`STABLE` and de-optimizes skipping on every composable that takes it. For the voice models, prefer all-`val` immutable shapes constructed whole (via a factory like `TagChipUiModel.of`) so you don't trade ABI-safety for a stability regression.

**Warning signs:**
- Any edit to a symbol already present in `api.txt` (vs. a net-new line).
- A `data class` whose primary constructor you're adding a field to.
- Metalava reporting `RemovedMethod`/`RemovedClass`/`ChangedType`, or `apiCheck` RED.
- A new required (non-defaulted) parameter on an existing public composable.
- A reviewer asking "does this change `copy()`'s signature?" and the answer being unclear.

**Phase to address:**
Phases 10, 11, 12 must each author additively (per-composable discipline; freeze model constructors on first authoring). **Phase 13** is the hard gate — success criterion #2 is Metalava strictly-additive vs `v2.3.0`. But do NOT defer the *thinking* to Phase 13: a break authored in Phase 10 and caught in Phase 13 costs a rework of Phase 10.

---

### Pitfall 4: Forgetting `ComponentRegistry` registration — and trusting scoped tests to catch it (the CATALOG-03 full-suite-only guard)

**What goes wrong:**
A new public composable is authored and tested, but not registered in a `ComponentRegistry` family list (nor allowlisted in `INTENTIONALLY_UNREGISTERED`). The `ComponentRegistryDriftGuardTest` that enforces "every public top-level `@Composable` is registered XOR intentionally-unregistered" **only runs — and only fails — in the FULL test suite**, not in the scoped/per-plan test runs an executor typically runs during a phase. So the phase looks green, the gallery silently lacks the new component, and the drift guard only trips at Phase 13's full-suite run (or, if that's skipped, at JitPack/CI). The HANDOFF calls this out explicitly: *"CATALOG-03's drift guard only fails in the full suite; scoped executor tests miss it."*

**Why it happens:**
Executors run the narrow test set relevant to their plan for speed; the drift guard isn't in that set. Registration also requires the full 4-cell `states` matrix (`Entry.states: List<StateCell>`) plus a `Tier`, which is extra authoring work easy to defer and forget. "Tests pass" creates false confidence.

**How to avoid:**
- Make registration part of the **definition of done for each new composable in the phase that authors it** (Phases 10/11/12), not a Phase-13 cleanup. When you add a public composable, add its `ComponentRegistry.Entry` in the correct family screen the same commit — with `name`, its full 4-cell `states` matrix, `content`, and an explicit `Tier` (PRIMITIVE vs PATTERN — these prop-driven presentational shells are almost all PATTERN, but decide deliberately per the DESIGN-INTENT litmus).
- If a composable is genuinely not gallery-appropriate (e.g. a pure sub-part), it must be allowlisted in `INTENTIONALLY_UNREGISTERED` with a rationale — **never neither, never both** (the registry integrity test enforces the XOR).
- **Run the full suite (`./gradlew testDebugUnitTest`) at least once per phase before declaring it done**, and unconditionally in Phase 13 — do not rely on scoped executor tests. Add "full suite green including drift guards" to each phase's verification.
- Remember there are TWO independent guards over the same composable universe: the **registry drift guard** (registered XOR intentionally-unregistered) and the **domain-vocabulary drift guard** (Pitfall 1). A new composable must satisfy both.

**Warning signs:**
- A phase declared done having run only scoped/plan-level tests.
- A new public `@Composable` with no corresponding `Entry` added in the same change.
- The gallery not showing a component you just built (visible in Gate-1 self-UAT).
- Full-suite run failing with a drift-guard message listing an unregistered composable name.

**Phase to address:**
Phases 10, 11, 12 register-as-you-author (per-composable done criterion). **Phase 13** is the explicit gate — success criterion #1 requires the full suite (including CATALOG-03) green with every new composable registered with its 4-cell states matrix.

---

### Pitfall 5: Introducing a forbidden dependency or coupling (OkHttp / the engine / serialization)

**What goes wrong:**
To make the voice UI "work," someone pulls in a networking or serialization dependency, or references the `voice-action-engine` module — e.g. adding OkHttp/Retrofit to validate an API key, a JSON library to model the outcome, or importing an engine type for the tier/approach enum. Any of these violates the one-way-dependency invariant (INV-01): the library must depend only on `Android SDK, AndroidX/Compose, Hilt, Coil, navigation-compose, reorderable, osmdroid`. It also breaks L7 (no hub-to-hub dependency — the UI hub must not depend on the engine hub). Once such a dependency is in a shipped tag, every consumer inherits it transitively.

**Why it happens:**
The settings cards *look* like they should test a key or talk to a provider; the outcome sheet *looks* like it should parse an engine result. The mental model "voice UI needs the voice engine" is exactly backwards for a presentational hub. The HANDOFF is blunt: *"Generic presentational composables only: no OkHttp, no engine dependency. Apps map engine outcomes → your props."*

**How to avoid:**
- Hold the line: **the library renders whatever the caller passes; it never fetches, validates, parses, or calls.** The provider/API-key card takes the key string + an `onKeyChange` callback and holds nothing (Phase 10 success criterion #1: "holding no key and making no network call in the library"). The consumer app owns the OkHttp call, the engine, the persistence.
- The `handled-by: tier/approach` indicator takes a **plain `String` (or a library-defined domain-neutral presentational model)** the consumer maps from the engine's result — never an engine type. Same for the tier ladder: the consumer passes an ordered `List<String>`/model of approach labels; the library defines no engine enum.
- **Do not add any dependency to `build.gradle.kts`** for this milestone. If you think you need one, you've moved logic that belongs in the consumer into the library.
- Verify at Phase 13: grep the new sources for engine/OkHttp/serialization imports; confirm `build.gradle.kts` gained no `implementation(...)`. Phase 13 success criterion #3 is exactly this.

**Warning signs:**
- A new line in `build.gradle.kts` dependencies for this milestone.
- Any `import` of an `okhttp3`, `retrofit2`, `kotlinx.serialization`, `com.google.gson`, or `voice-action-engine`/engine-package symbol in the new composables.
- A composable that "validates" a key, "calls" a provider, or "parses" a result rather than rendering passed-in state.
- KDoc describing what the component *does* to data rather than what it *renders*.

**Phase to address:**
Phases 10 and 11 are where the temptation is strongest (settings cards want to validate; outcome sheet wants to parse) — hold the invariant at authoring time. **Phase 13** is the verification gate (criterion #3: no OkHttp/engine dependency; every composable takes data + actions as params).

---

### Pitfall 6: Loud-failure-state UX pitfalls — failures that are silent or subtle

**What goes wrong:**
The outcome/failure sheet renders success prominently but treats failure as a muted afterthought: a gray subtitle, a tiny inline text, a transient snackbar that auto-dismisses, or — worst — a `when` branch that renders nothing (empty Box) for an unhandled/failed outcome. The user issues a voice command, it fails, and they either don't notice or can't tell WHAT failed or WHY. This directly violates the milestone's stated requirement (VOUT-03 / Phase 11 success criterion #3: "Failure states render prominently and visibly — loud, not silent or subtle") and the repo owner's documented UX philosophy (failures must be loud and visible in the UI).

**Why it happens:**
Happy-path-first authoring: the success rendering is built and demoed, failure is a stub. Compose makes silent-empty easy — an unhandled `when`/`if` branch simply composes nothing, leaving dead space instead of a loud error. The library's own "conditional-render-no-dead-space" convention can be *misread* as "render nothing on failure" when it actually means "don't reserve empty space for absent content" — a failure is present content and must be shown loudly.

**How to avoid:**
- Treat failure as a **first-class, visually-emphatic state**, not an edge case: error color/container, an icon, a clear headline, and the failure reason string (passed as a prop) — parallel in prominence to the success state. Reuse the hub's existing feedback primitives (e.g. `ConfirmationDialog`, error-styled `Sheet`/`Card`, `Badge`) rather than inventing a quiet one.
- **Exhaustive state handling with a loud fallback:** model the outcome as a sealed/enum-driven state and make the `when` exhaustive; the "unknown/unhandled" branch must render a loud, visible error, never `Unit`/empty. No silent branches.
- Surface the `handled-by: tier/approach` indicator even on failure ("attempted by: X, failed") so the user gets diagnostic context, not just a red X.
- Verify **on-device via Gate-1 self-UAT**, driving the failure state explicitly (not just the happy path) — a failure that's "loud" in code review can still be subtle on a real screen. Track any UI-prominence gap as a first-class item, exactly like a logic bug.

**Warning signs:**
- A `when`/`if` over outcome state with a branch that returns nothing or renders an empty container.
- Failure rendered in the same neutral color/weight as body text; no error color, icon, or headline.
- Failure shown only via a transient/auto-dismissing snackbar with no persistent surface.
- Gate-1 self-UAT screenshots only ever show the success/confirm path.

**Phase to address:**
**Phase 11** owns this (the outcome/failure sheet; success criterion #3 is the loud-failure gate). Phase 12's needs-confirmation state must apply the same prominence discipline to the "cancelled/rejected" outcome. Verify in each phase's Gate-1 self-UAT by exercising the failure/cancel branches.

---

## Technical Debt Patterns

Shortcuts that seem reasonable but create long-term problems.

| Shortcut | Immediate Benefit | Long-term Cost | When Acceptable |
|----------|-------------------|----------------|-----------------|
| Model needs-confirmation around one consumer, "generalize later" | Ships Phase 12 faster | Generalizing is a breaking change once tagged → forces a new library tag + re-repin of both consumers | Never — the dual-consumer shape IS the deliverable |
| Add voice fields to an existing shipped model (`TagChipUiModel`/`MicButton`) instead of a new type | Less new surface to author | Risk of the exact `copy()`/data-class ABI break v2.0 already hit; couples unrelated types | Never — introduce new types |
| Allowlist a `Voice`/`Command` head token into `PRIMITIVE_NOUN_ALLOWLIST` to silence the drift guard | Build goes green immediately | Permanently blinds the guard to that domain word for all future composables | Never — use a structural name, or `DOMAIN_VOCABULARY` with rationale |
| Declare a phase done on scoped executor tests only | Faster per-phase turnaround | CATALOG-03 / drift guards trip late (Phase 13 or JitPack), forcing rework of earlier phases | Only mid-plan; each phase must end on a full-suite green |
| Render failure as a muted subtitle / empty branch | Happy path demos cleanly | Users miss failures; violates VOUT-03 + owner UX rule; caught late in Gate-2 | Never for failure; fine for genuinely-absent optional content |
| Skip on-device Gate-1 of the failure/cancel branches | Saves a device run | "Loud in code" can be "subtle on screen"; prominence regressions ship | Never for this milestone (failure prominence is a success criterion) |

## Integration Gotchas

Common mistakes when connecting to external services / the ecosystem.

| Integration | Common Mistake | Correct Approach |
|-------------|----------------|------------------|
| `voice-action-engine` (peer hub) | Import an engine type for the tier/approach enum or outcome model (breaks L7 no-hub-to-hub) | Library defines its own domain-neutral presentational shape; consumer maps engine result → props |
| Provider / API-key validation | Add OkHttp/Retrofit to test the key in the settings card | Card takes key `String` + `onKeyChange`; holds nothing, calls nothing; consumer validates |
| JitPack tag / consumer repin | Cut the tag, then discover a non-additive break; or repin a consumer to an untagged/`main-SNAPSHOT` ref | §11: verify green + strictly-additive + JitPack builds it BEFORE messaging the row; consumers pin immutable ledger tags only |
| Cross-repo tag protocol (A12/§11) | Cut the `v2.4.0` tag autonomously (repo default is human-gated) | A12 waiver must be confirmed by Yahir in-session; message the orchestrator the full §11 row — do not self-commit to §11 |
| Milestone close (git) | Cut a GSD-milestone-marker tag (e.g. `v2.4`) — tags here are JitPack coordinates | Only `v2.4.0` (the release semver) is a tag; milestone close cuts NO extra tag (Phase 13 criterion #5) |

## Performance Traps

Patterns that work at small scale but fail as usage grows.

| Trap | Symptoms | Prevention | When It Breaks |
|------|----------|------------|----------------|
| Public model with a `var` → non-`STABLE`, de-optimizes skipping on every composable taking it | Recompositions on reference-unchanged params; jank in lists using the type | Prefer all-`val` immutable models constructed whole (factory), as the `TagChipUiModel` KDoc prescribes | Any batch-confirm list with many items re-rendering |
| Batch confirm passes a fresh `List`/lambdas every recomposition | Whole list recomposes on each parent recomposition | Hoist stable state; `rememberUpdatedState` for callbacks (the MicButton hardening precedent); stable item keys | Large `ProposedBatch` from CT |
| Non-lazy rendering of an unbounded proposed-item batch | Slow first render / dropped frames on big batches | Use a lazy list for the batch items inside the sheet | CT batch with many weak matches |

## Security Mistakes

Domain-specific security issues beyond general web security.

| Mistake | Risk | Prevention |
|---------|------|------------|
| API-key card persists or logs the key inside the library | Secret leaks into a reusable AAR / logs; the library must hold no secrets | Card is stateless: key in via prop, changes out via callback; consumer owns storage. No logging of the key value |
| Key rendered in plain text with no masking option | Shoulder-surfing; screenshot leakage | Support a masked/visibility-toggle field (prop-driven); default masked |
| Confirm/cancel callbacks not identity-safe across recomposition | Wrong action fires mid-interaction on a destructive confirm | Route callbacks through `rememberUpdatedState` (established MicButton pattern) so the latest lambda always fires |

## UX Pitfalls

Common user experience mistakes in this domain.

| Pitfall | User Impact | Better Approach |
|---------|-------------|-----------------|
| Silent/subtle failure state (Pitfall 6) | User can't tell a command failed or why | Loud error surface: error color, icon, headline, reason prop; exhaustive `when` with a loud fallback |
| No "handled-by" context on failure | User sees failure but no diagnostic info | Show attempted tier/approach even on failure |
| Single vs batch confirm as two different UIs | Inconsistent feel; CT batch feels bolted-on | One composable, `items` list; single is `size == 1` |
| Destructive confirm without clear reason prominence | User confirms/cancels blind | Reason string rendered prominently above the proposed items |
| Empty-branch "no dead space" misread as "render nothing on failure" | Failure disappears into blank space | Failure is present content — render it loudly; "no dead space" applies to ABSENT optional content only |

## "Looks Done But Isn't" Checklist

Things that appear complete but are missing critical pieces.

- [ ] **New composable:** Often missing its `ComponentRegistry.Entry` — verify it's registered (with 4-cell `states` + `Tier`) XOR in `INTENTIONALLY_UNREGISTERED`, and that the FULL suite (not scoped tests) is green.
- [ ] **New composable name:** Often leaks a domain head token — verify `DomainVocabularyDriftGuardTest` is green and the name uses a `PRIMITIVE_NOUN_ALLOWLIST` head token (or a rationale'd `DOMAIN_VOCABULARY` entry).
- [ ] **Needs-confirmation shape:** Often fits only one consumer — verify BOTH the SB `MutationGate` and CT `VoiceResultSheet` Proposed/ProposedBatch call-sites compile against the SAME signature with no dummy values.
- [ ] **Public model:** Often a `data class` that will break `copy()` when grown — verify additivity vs `v2.3.0` via `apiCheck` + `verify-api-additive.sh`, and that no `var` silently de-`STABLE`s it.
- [ ] **Outcome sheet:** Often only the happy path is demoed — verify the failure and cancel branches render loudly, on-device via Gate-1.
- [ ] **Dependencies:** Often a "small" networking/serialization/engine dep sneaks in — verify `build.gradle.kts` gained nothing and no engine/OkHttp imports exist.
- [ ] **Tag/ship:** Often the milestone-marker tag or an autonomous tag-cut — verify only `v2.4.0` is tagged, A12 waiver was confirmed in-session, and the §11 row went to the orchestrator.

## Recovery Strategies

When pitfalls occur despite prevention, how to recover.

| Pitfall | Recovery Cost | Recovery Steps |
|---------|---------------|----------------|
| Domain noun leaked but NOT yet tagged | LOW | Rename the symbol / neutralize the string; re-run both drift guards; re-register. |
| Domain noun leaked AND already in `v2.4.0` | HIGH | New tag with the neutral name; old name is frozen — deprecate, keep additive, re-repin both consumers. |
| Confirm shape fits only one consumer, pre-tag | MEDIUM | Redesign to `reason + items + slots`; re-validate both call-sites; re-author Phase 12. |
| Confirm shape too narrow, post-tag | HIGH | Add a new additive overload/type; new tag; can't remove the old shape. |
| `copy()`/ABI break caught by Metalava | LOW-MEDIUM | Move the field out of the primary ctor to a body property (the `TagChipUiModel.color` fix), or make the type non-`data`; re-run `apiCheck`. |
| Missing registration caught in full suite | LOW | Add the `Entry` (states + tier) or allowlist it; re-run full suite. |
| Forbidden dependency added | MEDIUM | Remove the dep; move the logic (validate/parse/call) back to the consumer; convert to prop + callback. |
| Silent failure state found in Gate-1/Gate-2 | LOW (pre-tag) | Add a loud error surface + exhaustive `when` fallback; re-run Gate-1 on the failure branch. |

## Pitfall-to-Phase Mapping

How roadmap phases should address these pitfalls.

| Pitfall | Prevention Phase | Verification |
|---------|------------------|--------------|
| 1. Domain noun/assumption leak | 10 (settle vocabulary), 11, 12 author; 13 backstop | `DomainVocabularyDriftGuardTest` green (full suite); litmus on every public name/param/string |
| 2. Confirm shape too narrow for both consumers | 12 | Both SB `MutationGate` and CT `VoiceResultSheet` call-sites compile against one signature (criterion #4); single = `items` size 1 |
| 3. Non-additive API / `copy()`-ABI break | 10, 11, 12 author additively; 13 gate | `./gradlew apiCheck` + `verify-api-additive.sh` strictly-additive vs `v2.3.0` (criterion #2); no `data class` primary-ctor growth |
| 4. Missing registration / full-suite-only CATALOG-03 | 10, 11, 12 register-as-authored; 13 gate | FULL `testDebugUnitTest` green incl. drift guard; every new composable registered w/ 4-cell states + Tier (criterion #1) |
| 5. Forbidden dependency / coupling | 10, 11 hold invariant; 13 gate | No new `build.gradle.kts` dep; no engine/OkHttp imports; props+callbacks only (criterion #3) |
| 6. Silent/subtle failure state | 11 (owns); 12 (cancel/reject) | Gate-1 self-UAT drives the failure + cancel branches; loud error surface; exhaustive `when` (criterion #3) |

## Sources

- `.planning/PROJECT.md`, `.planning/ROADMAP.md`, `.planning/MILESTONES.md` — milestone v2.4 scope, phase success criteria, and the documented v2.0 `copy()`-ABI break — HIGH (project source of truth)
- `.planning/cross-repo/HANDOFF.md` — §6.3 slice, CATALOG-03 full-suite-only warning, no-OkHttp/no-engine directive, A12/§11 tag protocol — HIGH
- `src/main/java/.../model/TagChipUiModel.kt` — the canonical, deeply-documented `copy()`/data-class ABI break and its body-property fix; the `var`→non-`STABLE` consequence — HIGH (live code + KDoc)
- `src/test/java/.../explorer/DomainVocabularyDriftGuardTest.kt` — head-token drift guard, `PRIMITIVE_NOUN_ALLOWLIST`, `DOMAIN_VOCABULARY` (incl. grandfathered `Voice`/`MicButton`) — HIGH (live guard)
- `src/main/java/.../explorer/ComponentRegistry.kt` — `Entry(name, states, tier)`, registered-XOR-intentionally-unregistered integrity contract — HIGH (live code)
- `tools/verify-api-additive.sh`, `verify-additive-surface.sh`, `verify-additive-diff.sh`, `tools/hooks/pre-commit` — live additive-API enforcement tooling — HIGH
- Root `CLAUDE.md` — one-way-dependency invariant, allowed dependency set, additive-only + human-gated ship ritual — HIGH
- `~/.claude/CLAUDE.md` developer profile — loud-failure-in-UI UX rule, UI issues tracked like logic bugs — HIGH

---
*Pitfalls research for: reusable prop-driven voice-command settings/outcome/confirm UI in a design-system library (yahirandroidtaste v2.4)*
*Researched: 2026-09-29*
