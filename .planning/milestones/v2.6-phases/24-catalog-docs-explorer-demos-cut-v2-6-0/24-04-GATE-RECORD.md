# Phase 24 Plan 04: v2.6.0 Gate Record

| Field | Value |
|---|---|
| GATED_HEAD | `78d89cf49fa3f0b59dbde91d2b69ac680eae5fc1` (`docs(24-04): current release v2.6.0 (D-09)`) |
| Tag object (annotated `v2.6.0`) | `b837d02d1208647b433a258580a4116e5d237b79` |
| Tagged commit (`v2.6.0^{commit}`) | `78d89cf49fa3f0b59dbde91d2b69ac680eae5fc1` (equals GATED_HEAD) |
| Tag message | `v2.6.0: Receipt voice surfaces + ProviderKeyCard lifecycle` |
| Base `origin/main` | `b6c363c8958a8077a540cc40c59eb4f200ac111d` (= `v2.5.1^{commit}`) |
| Date | 2026-10-12, gates run 18:08 to 18:13 MDT (TZ=America/Denver) |
| Executor | gsd-executor, sequential on main checkout, no worktree, no device, no consumer repo |

This record is a `.planning`-only descendant of the tagged commit: the tag points at exactly the tree the gates ran on
(verified by `git diff --name-only v2.6.0 HEAD`, all paths under `.planning/`).

Scratch logs (untracked, never staged) are under `.planning/phases/24-catalog-docs-explorer-demos-cut-v2-6-0/.gate/`.
Every gate below ran fresh on GATED_HEAD with exactly one gradle invocation at a time. Gradle was never run in the background.

## Preconditions

- Main checkout, branch `main`, origin `https://github.com/Ygaray/yahirandroidtaste.git`.
- Gate-1: `uat-run verify --self-uat 24-03-SELF-UAT.md` gave `verdict: verified, ok: true`, run `uatr-20261012T230611Z-4dbe72cf` (one warning: an empty logcat fatal-check capture is negative evidence).
- No `v2.6*` tag locally or on the remote; `.planning/config.json` has `"create_tag": false`.
- Review and security: `24-REVIEW.md` resolved, `24-SECURITY.md` threats_open 0. Review-fix commits (be61b21, 9e397e5, d081250, 7f139c2) touched only docs, tools and src/test.
- Artifact path list clean before the edit. One stray untracked generated file, `tools/__pycache__/verify-api-source-additive.cpython-312.pyc` (Python bytecode written by an earlier run of the additivity tool), was deleted with `rm` so the tree was clean; later runs used `PYTHONDONTWRITEBYTECODE=1`. Nothing tracked was affected.
- Clock at start 18:08 MDT 2026-10-12; push OK is valid through 2026-10-13 MDT.
- Release-line edit (D-09): `CLAUDE.md` coordinate line now `current release: v2.6.0` with descriptor `Receipt voice surfaces + ProviderKeyCard lifecycle`; `README.md` Current release callout (2 strings) and sample coordinate now v2.6.0 (no `v2.5.0` left in README). `classify-hub-change.sh --baseline v2.5.1` printed `LANE 1 (mode=additive, baseline=v2.5.1)`. Commit stages only those two files.

## (a) apiDump, api.txt no-diff, apiCheck

| Command | Exit | Decisive output |
|---|---|---|
| `./gradlew apiDump --offline --console=plain` | 0 | BUILD SUCCESSFUL |
| `git diff --exit-code -- api.txt` | 0 | regenerated api.txt identical to committed |
| `./gradlew apiCheck --offline --console=plain` | 0 | BUILD SUCCESSFUL |

## (b) Full unit suite, non-vacuous

`touch .gate/test-marker`, then `./gradlew cleanTestDebugUnitTest testDebugUnitTest --offline --console=plain`: exit 0, BUILD SUCCESSFUL in 2m 39s.
`:testDebugUnitTest` executed (not FROM-CACHE). `find build/test-results/testDebugUnitTest -name "*.xml" ! -newer .gate/test-marker` listed 0 of 135 XML files (all newer than the marker).

**Totals: tests=1317 skipped=22 failures=0 errors=0, 135 result files.**
The 22 skips come from five classes carrying `@Ignore`d cases (CardBaseTest, VoiceAlbumEditMenuTest, VoiceCardClipListTest, TextListEditMenuTest, TextCardImageIndicatorTest); their on-device proof is discharged in the Gate-1 flows named in those test files.

| Class | tests | failures | errors |
|---|---|---|---|
| V251CallSiteCanaryTest | 12 | 0 | 0 |
| ComponentRegistryDriftGuardTest | 1 | 0 | 0 |
| CatalogDocsDriftGuardTest | 10 | 0 | 0 |
| LibraryInvariantsGuardTest | 5 | 0 | 0 |
| DomainVocabularyDriftGuardTest | 2 | 0 | 0 |
| GeneratedSymbolDriftGuardTest | 2 | 0 | 0 |
| ComponentStatesMatrixTest | 2 | 0 | 0 |
| ComponentPlaygroundIntegrityTest | 5 | 0 | 0 |
| ToneAtomsRegistryRenderTest | 10 | 0 | 0 |
| ReceiptRowRegistryRenderTest | 7 | 0 | 0 |
| ReceiptDemosRegistryRenderTest | 14 | 0 | 0 |
| ProviderKeyLifecycleDemoRegistryRenderTest | 11 | 0 | 0 |
| UndoGroupDemoRegistryRenderTest | 5 | 0 | 0 |

## (c) detekt

- `./gradlew detekt --offline --console=plain` first came back `1 from cache` (exit 0). To avoid citing a cached result it was re-executed with `./gradlew detekt --rerun --offline --console=plain`: `:detekt` executed, exit 0, BUILD SUCCESSFUL, zero findings, zero baseline.
- `git diff --exit-code v2.5.1 -- config/detekt-baseline.xml config/detekt/detekt.yml config/detekt-compose.yml build.gradle.kts settings.gradle.kts gradle.properties gradle jitpack.yml`: exit 0 (byte-identical to v2.5.1).

## (d) tools/test/run-all.sh

Exit 0. Fixture suites all green, last lines: additivity tool `cases passed=34 failed=0`, `test-verify-binary-abi.sh PASS=52 FAIL=0`; the word FAILED never appears in the log.

## (e) Source additivity v2.5.1

`bash tools/verify-api-source-additive.sh v2.5.1`: exit 0. Output:

```
SEAMS: none
ACCEPTED R5: ...component.SheetScaffoldKt method SheetScaffold (shape inserted before the trailing content lambda; positional content breaks (API.md))
ACCEPTED R5: ...feedback.UndoCenterScreenKt method UndoCenterScreen (groups and onUndoGroup inserted before onClear; four positional arguments break (API.md))
ACCEPTED R5: ...model.VoiceOutcomeUiState.NeedsConfirmation ctor NeedsConfirmation (confirmEnabled and titleActions inserted before onConfirm and onCancel (API.md))
ACCEPTED R6: ...model.VoiceOutcomeUiState.NeedsConfirmation method component10 (destructuring shifts after the inserted fields (API.md))
ACCEPTED R6: ...model.VoiceOutcomeUiState.NeedsConfirmation method component11 (destructuring shifts after the inserted fields (API.md))
ACCEPTED R5: ...model.VoiceOutcomeUiState.NeedsConfirmation method copy (data-class copy of the same inserted fields (API.md))
API SOURCE-ADDITIVE OK baseline=v2.5.1 removed_classes=0 removed_members=0 enum_constants_intact=55 required_param_changes=0 added_classes=29 added_members=224 signature_changes=0 accepted_exceptions=6
```

The six ACCEPTED lines are the reviewed exceptions of the post-review tool (rules R1 to R8, each carrying an `(API.md)` reference in its reason).

`git diff --numstat v2.5.1 -- api.txt`: `337 16 api.txt` (337 added, 16 removed).

Removed api.txt lines (single leading minus, count 16; each has a same-member added line, proven by the tool; the tool's own output above shows no removed member and no signature change beyond the six accepted ones):

```
-    method ... DynamicActionButton(String label, ActionButtonRole role, Function0 onClick, optional boolean enabled, optional Modifier modifier);
-    method ... OutcomeSheet(VoiceOutcomeUiState outcome, Function0 onDismissRequest, optional Modifier modifier);
-    method ... ProviderKeyCard(List providers, String? selectedProviderId, Function1 onProviderSelected, String keyValue, Function1 onKeyChange, KeyFieldState keyState, String keyLabel, optional Modifier modifier, optional String emptyProvidersReason, optional String providerLabel);
-    method ... SegmentedOptionSelector(int selectedIndex, List options, Function1 onSelect, optional Modifier modifier, optional boolean enabled, optional String? disabledReason);
-    method ... SheetScaffold(Function0 onDismissRequest, optional Modifier modifier, optional SheetState sheetState, optional Function0? dragHandle, optional Function0 contentWindowInsets, Function1 content);
-    method ... UndoCenterScreen(List entries, Function0 onNavigateBack, Function1 onUndo, Function0 onClear);
-    ctor FailureActionUiModel(String label, Function0 onClick, optional ActionButtonRole role);
-    method FailureActionUiModel copy(optional String label, optional Function0 onClick, optional ActionButtonRole role);
-    method HandledByUiModel copy(optional tier, approach, provider, model, escalationCount, escalationsLabel);
-    method VoiceOutcomeUiState.Failure copy(optional reason, handledBy, action, body, semanticsPrefix);
-    ctor VoiceOutcomeUiState.NeedsConfirmation(... onConfirm, onCancel);
-    method NeedsConfirmation component10();
-    method NeedsConfirmation component11();
-    method NeedsConfirmation copy(optional reason, items, selectionMode, title, severity, reversibilityHint, confirmLabel, cancelLabel, topLevelContent, onConfirm, onCancel);
-    ctor VoiceOutcomeUiState.Success(summary, optional handledBy, editableContent, inFlight, batchResults, undo);
-    method VoiceOutcomeUiState.Success copy(optional summary, handledBy, editableContent, inFlight, batchResults, undo);
```

(Signatures abbreviated here with `...`; the full unabbreviated list is in `.gate/1e-removed.txt`.)

## (f) Binary ABI, INFORMATIONAL (does not gate the cut, D-02)

`bash tools/verify-binary-abi.sh v2.5.1`: first line `SEAMS: none`, **exit 3** (expected; recorded, not blocking).
HEAD AAR sha256 `24ca736aa8315e64f1fc727ebc80d6856014c549b3ee65ee9a3c6aa21b4623a0`; baseline AAR sha256 `2e37fe2f4c83e97072dad94dd38a0dc91c7da1c007fc35cd5bed32d198470ebb` (Gradle cache); `base=3953 head=4545 filtered_ComposableSingletons=13 missing=29`.
The 29 missing descriptors by class: DynamicActionButtonKt 1, OutcomeSheetKt 2, ProviderKeyCardKt 1, SegmentedOptionSelectorKt 1, SheetScaffoldKt 1, UndoCenterScreenKt 2 (+1 inlined lambda synthetic), FailureActionUiModel 4, HandledByUiModel 3, VoiceOutcomeUiState$Failure 3, $NeedsConfirmation 6, $Success 4. They are the JVM-level shadow of the 16 source-level replaced signatures (composable overloads, constructors, `copy`, `copy$default`, `componentN`, default-constructor-marker synthetics). Consumers always recompile (source compatibility is the bar; see `CLAUDE.md`), so this is informational only.

## (g) INV-03 and hygiene greps

| Check | Result |
|---|---|
| `git log v2.5.1..HEAD --oneline -S"DeprecationLevel.HIDDEN" -- src/main` | no output (no hidden-deprecated declaration added or removed) |
| `/usr/bin/grep -rnE "^[^/*]*@(HiltAndroidApp\|AndroidEntryPoint)" src/main --include=*.kt` | exit 1, no output |
| `/usr/bin/grep -rnE "^import (com\.example\.secondbrain\|com\.caltracker)" src` | exit 1, no output |
| `git diff --exit-code v2.5.1 -- <detekt configs, gradle files, jitpack.yml>` | exit 0 |

## Post-battery re-assertion

`git rev-parse HEAD` equals `.gate/GATED_HEAD` (`78d89cf...`) and the artifact path list (`src build.gradle.kts settings.gradle.kts gradle.properties gradle config api.txt jitpack.yml API.md INTEGRATION.md README.md CLAUDE.md tools`) is clean (empty `git status --porcelain --untracked-files=all`).

## Tag and range check (D-04)

State before tagging: local tag absent, remote `v2.6*` absent, remote main `b6c363c8958a8077a540cc40c59eb4f200ac111d`.
Tag created by SHA at GATED_HEAD: `git cat-file -t v2.6.0` prints `tag`; `git rev-parse v2.6.0` is `b837d02d1208647b433a258580a4116e5d237b79`; `git rev-parse v2.6.0^{commit}` is `78d89cf49fa3f0b59dbde91d2b69ac680eae5fc1`; `git tag --list "v2.6*"` prints exactly `v2.6.0`.

**Range `git log --oneline origin/main..v2.6.0`: 189 commits** (`git rev-list --count` also 189). The dispatch estimate of about 187 was measured as 188 unpushed commits on main at plan start plus this plan's release-line commit. Newest: `78d89cf docs(24-04): current release v2.6.0 (D-09)`. Oldest: `3ca6162 docs(ecosystem): SecondBrain + CalTracker on v2.5.1 (repin_status reconcile)`; its parent is `b6c363c8958a8077a540cc40c59eb4f200ac111d` = the v2.5.1 commit = `origin/main` tip. The full listing is in `.gate/range-log.txt`.

| Scan | Tool | Exit | Result |
|---|---|---|---|
| origin/main is an ancestor of v2.6.0 (fast-forward) | `git merge-base --is-ancestor` | 0 | pass |
| single author | `/usr/bin/grep -vx Yahir range-authors.txt` | 1, no output | pass (all 189 commits by Yahir) |
| path allowlist (689 changed paths) | `/usr/bin/grep -vE '^(\.planning/\|src/\|tools/\|api\.txt$\|API\.md$\|INTEGRATION\.md$\|README\.md$\|CLAUDE\.md$\|ECOSYSTEM\.md$)'` | 1, no output | pass |
| credential positive control (synthetic `+ sk-` line) | `/usr/bin/grep -cE` | printed 1 | control matches |
| credential scan over added lines of the 6.9 MB range diff | `/usr/bin/grep -nE '^\+.*(sk-[A-Za-z0-9_-]{16,}\|AIza[0-9A-Za-z_-]{30,}\|ghp_[A-Za-z0-9]{30,}\|xox[baprs]-[A-Za-z0-9-]{10,}\|Authorization: Bearer [A-Za-z0-9._-]{20,})'` | 1, no output | pass |
| extra private-key / AWS / JWT scan (`-----BEGIN ... PRIVATE KEY`, `AKIA...`, `eyJ...`) | `/usr/bin/grep -cE` | count 0 | pass |

The credential regex differs from the plan text on one point: `sk-[A-Za-z0-9_-]{16,}` replaces the narrower `sk-[A-Za-z0-9]{20,}` (director-confirmed correction, wider net, still zero hits).

Reviewed and accepted, not scrubbed (listed as reviewed, not credentials):
- Two superseded Phase 23 UI dumps contain launcher application names (from the device's launcher UI): `.planning/phases/23-providerkeycard-lifecycle-undo-center-groups/23-02-uat-evidence/uatr-20261012T172501Z-65a2a2aa/28-sc2-p08-pk-summary-with-raw-keyvalue-ui.xml` and `.../30-sc2-p08-pk-summary-while-raw-keyvalue-held-ui.xml`.
- Device serials, tailnet host names and phone model names in evidence under `.planning/` are not credentials.

Record-paths check (descendant property) runs after this commit; its result is appended in the SUMMARY: `git diff --name-only v2.6.0 HEAD` must touch only `.planning/` paths.
