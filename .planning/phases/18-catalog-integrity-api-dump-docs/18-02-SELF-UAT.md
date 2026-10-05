---
status: complete
result: all_pass
gate: 1
phase: 18-catalog-integrity-api-dump-docs
source: [18-ROADMAP success criteria SC1-SC5]
device: none (headless; non-device docs + evidence phase, tester rig not touched)
apk: n/a (library; api.txt sha256 218bd9c2acb1c01c3a988dfb462449dc3fd43f3ba387c88cb3ae01160d23740e @ a10c79e)
run: 2026-10-05
---

# Self-UAT Log - Phase 18 (Catalog integrity + API dump + docs), Gate 1

**Target:** headless on the host (Gradle/JUnit/Metalava/detekt/git/grep). No on-device behavior exists to verify:
the phase changed only `API.md` and `INTEGRATION.md` (`git diff --name-only 625c714 HEAD -- src tools api.txt config build.gradle.kts` is empty).
The library surface itself was device-verified in Phases 15-17 (their SELF-UAT logs). No device drive was performed and none is claimed.
**Driver playbook:** `AGENT-DEVICE-TESTING.md` (project root). Its D1-D2 harness mechanic applies only to criteria with on-device behavior; none of SC1-SC5 has any.
**Build identity:** HEAD `a10c79e`; code-identical to the evidence capture HEAD `b9848a9` (only `.planning/`, `API.md`, `INTEGRATION.md` differ). `api.txt` sha256 `218bd9c2...3740e`.
**Run:** 2026-10-05. Prior verifier report `18-VERIFICATION.md` and `18-SHIP-GATE-EVIDENCE.md` were treated as claims and every number below was re-observed fresh on HEAD.
**Unit suite (fresh, uncached):** `./gradlew cleanTestDebugUnitTest testDebugUnitTest detekt apiCheck --rerun-tasks --offline` -> BUILD SUCCESSFUL in 2m 9s, 44 actionable tasks / 44 executed. 77 JUnit XML files, all 77 newer than a pre-run marker; tests=751 skipped=22 failures=0 errors=0.
**Coverage/Nyquist:** n/a (docs/evidence phase; the verifier already scored 5/5 against ROADMAP SC).
**Fixture integrity:** `api.txt` clean in `git status` before and after; sha256 unchanged after every swap; `config/detekt-baseline.xml` is `<CurrentIssues/>` (empty).

## Criteria

### 1. SC1 / CAT-02 - every new/changed public composable registered (or allowlisted); CATALOG drift guard green in the full suite
result: passed
- **Rung:** 1 (unit/integration suite; no higher rung applies - no visual claim)
- **Target:** headless
- **Expected:** drift guard + registry integrity tests run and pass in the full, unscoped suite; added public composable names are all registered or allowlisted.
- **Arranged (seeded):** none
- **Did (drove):** full suite command above, then per-class XML read: `ComponentRegistryDriftGuardTest` tests=1 f=0 e=0; `DomainVocabularyDriftGuardTest` 2/0/0; `GeneratedSymbolDriftGuardTest` 2/0/0; `ComponentRegistryTierTest` 4/0/0 (all non-vacuous, all fresh).
- **Observed (falsification):** `grep -c 'ComponentRegistry.Entry(' explorer/*FamilyScreen.kt` sums to 61 (Voice family = 5). `git diff v2.4.1..HEAD -U0 -- src/main | grep '^+fun [A-Z]'` yields exactly ApproachLadderCard, ClarificationBar, ModelSelectCard, ProviderKeyCard: the hidden same-name v2.4.x shims, whose names are already registered in `VoiceCommandFamilyScreen.kt`, so the name-set guard has nothing new to register. No new public composable name was introduced.
- **Evidence:** fresh `build/test-results/testDebugUnitTest/*.xml` (not committed; reproducible with the command above).

### 2. SC2 / API-02 - api.txt regenerated and strictly additive vs v2.4.x
result: passed
- **Rung:** 3 (headless data/tool checks)
- **Target:** headless
- **Expected:** `api.txt` is current, and the public API is strictly additive vs v2.4.x. The ROADMAP wording says `tools/verify-api-additive.sh` "passes".
- **Arranged (seeded):** baseline swap `git show v2.4.1:api.txt > api.txt` inside one shell with `trap 'git checkout -- api.txt' EXIT INT TERM`; restored with an explicit `git checkout -- api.txt` before any exit code was read.
- **Did (drove):**
  - `./gradlew apiDump` -> rc=0 and `cmp api.txt <pre-run copy>` IDENTICAL (api.txt is current).
  - `./gradlew apiCheck` -> rc=0.
  - Swap-baseline `./gradlew metalavaCheckCompatibilityRelease --rerun` against the v2.4.1 api.txt -> `> Task :metalavaCheckCompatibilityRelease` executed, BUILD SUCCESSFUL, rc=0 (Metalava judges HEAD source additive vs v2.4.1).
  - `API_FILE=api.txt bash tools/verify-api-additive.sh v2.4.1` -> **rc=3, exactly 10 `API-ADDITIVE FAIL (lane 3)` lines**.
- **Observed (falsification of the raw-script exit):** `git diff v2.4.1 HEAD -- api.txt` removes 10 lines and adds 60. A script check that tests each removed line against the added set (the removed line with its closing `)` stripped is a string prefix of an added line) matched 10/10: 4 composable lines (ApproachLadderCard, ClarificationBar, ModelSelectCard, ProviderKeyCard) and 6 `copy(optional ...)` lines (FailureActionUiModel, HandledByUiModel, ProposedItemUiModel, UndoRefusedUiModel, UndoRowUiModel, VoiceOutcomeUiState.Failure). Every one is an in-place "appended defaulted parameter" rewrite, not a removal. Not one of the ten is a real removal or rename. The script's exit 3 is a raw-line false positive on that pattern.
- **Caveat (honest reading):** the literal ROADMAP words "verify-api-additive.sh passes" are NOT met (rc=3). The criterion is passed under the CONTEXT D-01 reading (Metalava `apiCheck` green + `api.txt` current), which mirrors the Phase 15 human ruling. The exit was neither overridden nor hidden, and the script was not edited. See Notes; the owner should confirm this reading at Gate-2.
- **Evidence:** command output above; `api.txt` sha256 unchanged `218bd9c2...3740e` and `git status --short api.txt` empty after the swap.

### 3. SC3 / DOC-02 - API.md and INTEGRATION.md updated for the new label params, the Failure enrichment, and the router toggle
result: passed
- **Rung:** 3 (grep of docs against source)
- **Target:** headless
- **Expected:** each new public param/field (derived from the Phase 15-17 source, not from the SUMMARY) is documented, with correct defaults and semantics.
- **Arranged (seeded):** none
- **Did (drove):** per-identifier counts for 21 identifiers (`providerLabel`, `emptyProvidersReason`, `modelLabel`, `dismissLabel`, the five `ApproachLadderCard` labels, `escalationsLabel`, `removeContentDescription`, `undoneLabel`, `refusedPrefix`, `changedSinceSuffix`, `body`, `semanticsPrefix`, `role`, `router`, `onRouterChange`, `routerOnLabel`, `routerOffLabel`) across API.md / INTEGRATION.md / src/main. Every identifier appears in API.md (>=1) and in src/main; no documented name is absent from source. INTEGRATION.md covers the label params; the router pair and `role`/`emptyProvidersReason` are documented in API.md only (INTEGRATION.md carries a light localization note by design, CONTEXT D-02).
- **Observed (falsification):** defaults spot-checked against source: `routerOnLabel = "Router on"` / `routerOffLabel = "Router off"` (ApproachLadderCard.kt:109-110), `refusedPrefix = "Couldn't undo:"`, `changedSinceSuffix = "changed since"`, `undoneLabel = "Undone"`, `escalationsLabel = "Escalations:"` all match API.md rows 216-218. `require()` pairing is documented (API.md:201) and exists in source (ApproachLadderCard.kt:117,121). Section structure: exactly one `## 10. Voice Command` (API.md:176) and one `### Localizing the voice surface (optional)` (INTEGRATION.md:121). The `ProposedItemUiModel` trailing-lambda caveat (named `trailingContent = { ... }`) is present. The docs diff mentions consumer names only in the compatibility-sweep sentence (SecondBrain, CalTracker), following the v2.3.0 precedent; section 10 itself is consumer-name-free.
- **Evidence:** `git diff 625c714 HEAD -- API.md INTEGRATION.md` (114 insertions, 5 deletions, two files).

### 4. SC4 / INV-02 - one-way-dependency invariant holds (no engine/consumer import added); detekt green at zero baseline
result: passed
- **Rung:** 3
- **Target:** headless
- **Expected:** no import of a consumer/engine package in the `src/main` diff since v2.4.1; detekt green with an empty baseline.
- **Arranged (seeded):** none
- **Did (drove):** `git diff v2.4.1..HEAD -U0 -- src/main | grep '^+import '` -> 10 added imports (non-vacuous): androidx `selectable`, `selectableGroup`, `minimumInteractiveComponentSize`, `Role`, `contentDescription`; the library's own `ActionButtonDefaults`; `kotlin.jvm.internal.DefaultConstructorMarker` x4. Zero foreign or consumer imports. `git diff --exit-code v2.4.1..HEAD -- build.gradle.kts gradle/ settings.gradle.kts jitpack.yml config/` rc=0 (no dependency added, baseline untouched). Added-lines grep for `HiltAndroidApp|AndroidEntryPoint|Log.[dewiv](|println(|Timber` -> empty. `detekt` ran inside the fresh suite (executed, not up-to-date, via `--rerun-tasks`) -> green. `config/detekt-baseline.xml` contains only `<CurrentIssues/>`.
- **Observed (falsification):** the import filter is non-vacuous (10 hits), so an empty foreign-import set is meaningful. Residual accepted per CONTEXT D-03: the check does not see fully-qualified non-import references or transitive coupling, and there is no automated forbidden-import test this milestone; mitigated by the unchanged build and dependency files.
- **Evidence:** command output above.

### 5. SC5 - no git tag is cut in this phase
result: passed
- **Rung:** 3
- **Target:** headless
- **Expected:** no `v2.5*` tag, none at HEAD, nothing moved.
- **Arranged (seeded):** none
- **Did (drove):** `git tag --list 'v2.5*'` -> empty; `git tag --points-at HEAD` -> empty; `git tag` tail is v2.2, v2.2.0, v2.3.0, v2.4.0, v2.4.1 (unchanged). `git ls-remote --tags origin 'v2.5*'` printed nothing. This run made no tag, no push, and edited nothing under `src/`, `tools/`, or `api.txt`.
- **Observed:** boundary diff `625c714..HEAD` outside `.planning/` is `API.md` and `INTEGRATION.md` only.
- **Evidence:** command output above.

## Summary

total: 5
passed: 5
partial: 0
failed: 0
infra: 0

## Notes / anomalies (for the Gate-2 reviewer)

- **Non-device phase.** All five criteria were settled headlessly at rung 1 or 3 on current HEAD. Rung 5 (screenshot) does not apply and no device or emulator was used or leased.
- **SC2 literal wording vs D-01 (needs a human nod, not a blocker).** `tools/verify-api-additive.sh` exits 3 with 10 lines; this run independently confirmed all 10 are appended-defaulted-parameter rewrites and Metalava is green vs v2.4.1. Passed under the D-01/Phase-15-human-ruled reading. The owner should confirm that reading, or, if the roadmap text must be read strictly, amend SC2 wording.
- **Latent guard defect (Phase 19 D-03):** with an absolute `API_FILE` the script silently SKIPs with exit 0; only the relative form (`API_FILE=api.txt`) actually checks. The pre-commit hook's API half uses the absolute form.
- **Known source-level break, documented only:** `ProposedItemUiModel(id, title) { ... }` trailing-lambda no longer binds to `trailingContent` (the appended `removeContentDescription` is now the last parameter). Documented in API.md with the named-arg mitigation per the orchestrator ruling; `15-VERIFICATION.md` line 60 claims otherwise and was not edited.
- **Not re-run:** the v2.4.0 swap-baseline (blob is byte-identical to v2.4.1's per the evidence file) and the javap binary diff (Phase 19 D-03 owns the binary gate on the tagged commit).
- **Prior-log audit:** `18-VERIFICATION.md` / `18-SHIP-GATE-EVIDENCE.md` numbers (751/22/0/0, 61 entries, 10 raw-line FAILs, 10 imports) were all reproduced exactly; no contradiction found.

## Findings routed to gap-closure (if any)

None.

## Verdict

All 5 criteria PASS (headless, non-device) -> Gate-1 complete; human Gate-2 deferred to milestone completion (registered as `.planning/uat-pending/18-catalog-integrity-api-dump-docs.md`). Phase 19 (cut v2.5.0) is unblocked from the Gate-1 side.
