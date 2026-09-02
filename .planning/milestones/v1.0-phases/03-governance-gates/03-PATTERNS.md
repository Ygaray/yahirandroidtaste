# Phase 3: Governance Gates - Pattern Map

**Mapped:** 2026-09-01
**Files analyzed:** 6 (2 core targets + 3 possible/related tooling files + 1 doc)
**Analogs found:** 6 / 6 (all in-repo, self-referential — this phase modifies/extends existing governance files themselves)

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|---|---|---|---|---|
| `tools/hooks/pre-commit` | utility (git hook, classifier orchestration) | request-response (invoked per commit, exit-code driven) | itself (existing file, in-place fix) | exact — modify in place |
| `tools/classify-hub-change.sh` | utility (lane classifier) | request-response | `tools/verify-additive-diff.sh` (sibling diff-basis logic) | role-match — only touch if scope widens to `--staged` mode |
| `tools/verify-additive-diff.sh` | utility (diff-based guard) | batch (git diff → normalize → set-diff) | itself | exact — the D-01 fix's core diff-basis change lives here (or in a new `--staged` invocation site) |
| `tools/verify-api-additive.sh` | utility (diff-based guard) | batch | `tools/verify-additive-diff.sh` (near-identical architecture: baseline-vs-current comm -23 set diff) | exact structural twin — same fix shape applies if Open-Question-1 scope is widened |
| `tools/test/test-precommit-hook.sh` | test (bash fixture/integration) | event-driven (sequential git-commit fixtures + exit-code assertions) | itself (existing fixture harness, append a new case) | exact |
| `src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt` | test (JUnit source-text-scan drift guard) | batch (walk `.kt` tree → extract names → set-diff against allowlist) | `src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistryDriftGuardTest.kt` | exact — same package, same shape, different predicate/allowlist |
| `docs/DESIGN-INTENT.md` | config/documentation (prose contract) | transform (append new sections to existing structure) | itself (existing `## The Litmus` / `## Applying the Litmus` sections) | exact — append, do not restructure |

## Pattern Assignments

### `tools/hooks/pre-commit` (utility, request-response)

**Analog:** itself — full source already read live (35 lines).

**Current baseline-resolution pattern** (lines 3-10):
```bash
ROOT="$(git rev-parse --show-toplevel)"
export API_FILE="${API_FILE:-$ROOT/api.txt}"
BASE="$(git describe --tags --abbrev=0 --match 'v*' 2>/dev/null || true)"
[ -n "$BASE" ] || exit 0   # no release tag yet -> nothing to diff against
```
This is the root cause site — `BASE` resolves to the last tag (`v1.10.0`), then gets handed to
`classify-hub-change.sh --baseline "$BASE"`, which diffs that stale tag against the **working
tree**, not the staged delta. D-01's fix must change what gets compared, not this baseline
resolution itself (the API_FILE env-var wiring stays; only the src-line comparison basis changes).

**Classifier invocation pattern to preserve** (lines 11-15):
```bash
set +e
TMP="$(mktemp)"
"$ROOT/tools/classify-hub-change.sh" --baseline "$BASE" >"$TMP" 2>&1; lane=$?
set -e
cat "$TMP"; rm -f "$TMP"
```
The lane-exit-code contract (0/2/3, mapped to `HUB_LANE_OVERRIDE`) is unchanged by this fix — only
the internal diff basis inside `verify-additive-diff.sh` (invoked transitively via
`classify-hub-change.sh`) needs to switch from tag-vs-working-tree to staged-vs-HEAD.

**Override/block pattern to preserve exactly** (lines 16-29):
```bash
case "$lane" in
  0) exit 0 ;;
  2|3)
    if [ "${HUB_LANE_OVERRIDE:-}" = "$lane" ]; then
      echo "pre-commit: lane $lane change explicitly declared (HUB_LANE_OVERRIDE=$lane) — allowed." >&2
      exit 0
    fi
    echo "pre-commit: BLOCKED — lane $lane (non-additive) change on the fast path." >&2
    echo "  A lane-2/3 change must be coordinated. To land it deliberately: HUB_LANE_OVERRIDE=$lane git commit …" >&2
    exit 1 ;;
  *)
    echo "pre-commit: BLOCKED (fail-closed) — additive-guard classifier returned unexpected exit $lane; cannot verify additivity, refusing commit." >&2
    exit 1 ;;
esac
```
Fail-closed-on-unexpected-exit-code is a load-bearing convention — any new diff-basis code path
inside `verify-additive-diff.sh`/`verify-api-additive.sh` must still only ever return 0/1 (src
guard) or 0/3 (api guard) so this case statement's contract holds.

---

### `tools/verify-additive-diff.sh` (utility, batch diff-guard) — the D-01 fix site

**Analog:** itself.

**Current (buggy) comparison basis** (lines 73-78):
```bash
# Single-ref form (BASELINE, not BASELINE..HEAD): diffs the baseline commit against the current
# WORKING TREE, not just the last commit — so an uncommitted regression is caught too...
git diff -U0 "$BASELINE_COMMIT" -- "${PATHS[@]}" > "$TMP_DIR/full.diff" || true
```
This is the exact line the fix touches. D-01: replace (or add a `--staged`-mode branch that
replaces) this single-ref working-tree diff with a staged-vs-HEAD diff:
```bash
# Fix: diff the INDEX (what THIS commit stages) against HEAD, not a stale tag against the
# working tree. Verified live this session — exit 0, empty, with nothing staged.
git diff --cached -U0 -- "${PATHS[@]}" > "$TMP_DIR/full.diff" || true
```
Note: the default `PATHS` derivation at line 58 (`git ls-tree -z -r --name-only "$BASELINE_COMMIT"
-- src/main`) still needs a baseline ref to enumerate tracked files — keep `BASELINE_COMMIT` as an
arg for path-enumeration purposes only; do not remove the parameter, just change which diff it
feeds (per RESEARCH.md Pattern 1 and Anti-Pattern 3 — do NOT touch the path filter, it is already
correct).

**Reconciliation logic to reuse unchanged** (lines 89-101):
```bash
normalize() {
  sed -E \
    -e 's/^.//' \
    -e 's/[[:space:]]+$//' \
    -e 's/[+,]$//' \
    -e 's/[[:space:]]+$//' \
    "$1" | sort -u
}

normalize "$REMOVED_RAW" > "$REMOVED_NORM"
normalize "$ADDED_RAW" > "$ADDED_NORM"

OFFENDERS="$(comm -23 "$REMOVED_NORM" "$ADDED_NORM" || true)"
```
This normalize + `comm -23` shape is proven and handles the one legitimate false-rewrite case
(trailing `+`/`,` punctuation drift). Do not reinvent — only the `git diff` invocation feeding
`REMOVED_RAW`/`ADDED_RAW` changes.

**Ref-resolution fail-loudly pattern to preserve** (lines 39-44):
```bash
if ! git show "$BASELINE_COMMIT" --stat >/dev/null 2>&1; then
  echo "DS-05 FAIL: baseline ref '$BASELINE_COMMIT' could not be resolved via 'git show'." >&2
  exit 1
fi
```

---

### `tools/verify-api-additive.sh` (structural twin — same architecture, in scope per Open Question 1)

**Analog:** `tools/verify-additive-diff.sh` (near-identical shape, different granularity — whole-file-content set diff vs. line-diff).

**Current (identically-flawed) comparison basis** (lines 22-24):
```bash
# Every line present in the baseline .api must still be present now. A missing line = a removed
# or renamed public symbol. (Ordering-independent: compare as sets, like DS-05 / DS-04.)
missing="$(comm -23 <(git show "$BASE:$API_FILE" | sort -u) <(sort -u "$API_FILE") || true)"
```
`sort -u "$API_FILE"` reads the **current on-disk working-tree file**, not the staged/HEAD
version — same stale-cumulative-baseline architecture as the DS-05 bug, currently dormant only
because `v1.10.0` predates `api.txt`. If the planner elects to widen scope (RESEARCH.md Open
Question 1), the symmetric fix is:
```bash
# Staged-delta equivalent: compare HEAD's committed api.txt against what's staged now.
missing="$(comm -23 <(git show "HEAD:$API_FILE" 2>/dev/null | sort -u) <(git show ":$API_FILE" 2>/dev/null | sort -u) || true)"
```
(`git show ":$API_FILE"` reads the staged/index version of a path — the staged-content
equivalent of `git diff --cached`.) The dormant-skip guard at lines 17-20 (`git cat-file -e
"$BASE:$API_FILE"`) should be preserved as a distinct concern (baseline predates the file at all)
separate from the staged-vs-HEAD fix.

**Fail-loudly ref/file-existence checks to preserve** (lines 9-16):
```bash
[ -r "$API_FILE" ] || { echo "API-ADDITIVE FAIL: current $API_FILE is missing/unreadable" >&2; exit 1; }
git rev-parse --verify --quiet "$BASE^{commit}" >/dev/null 2>&1 || { echo "API-ADDITIVE FAIL: baseline ref '$BASE' does not resolve" >&2; exit 1; }
```

---

### `tools/test/test-precommit-hook.sh` (test, event-driven fixture)

**Analog:** itself — existing fixture harness (49 lines), extend with a new case.

**Existing fixture-setup pattern** (lines 1-13):
```bash
DIR="$(cd "$(dirname "$0")/.." && pwd)"
TMP="$(mktemp -d)"; trap 'rm -rf "$TMP"' EXIT
cd "$TMP"; git init -q -b main; git config user.email t@t; git config user.name t
mkdir -p tools/hooks tools/explorer src/main api
cp "$DIR"/verify-additive-diff.sh "$DIR"/verify-api-additive.sh "$DIR"/verify-additive-surface.sh \
   "$DIR"/classify-hub-change.sh tools/ 2>/dev/null || true
cp "$DIR"/hooks/pre-commit tools/hooks/pre-commit 2>/dev/null || true
printf 'val x = 1\n' > src/main/A.kt; printf 'public fun a(): Unit\n' > api/hub.api
git add -A; git commit -qm base; git tag v1.0.0
export API_FILE="api/hub.api"
ln -sf ../../tools/hooks/pre-commit .git/hooks/pre-commit; chmod +x tools/hooks/pre-commit 2>/dev/null || true
```

**Existing assertion helper to reuse** (line 16):
```bash
check(){ if [ "$1" = "$2" ]; then pass=$((pass+1)); else echo "FAIL: $3 (got $1 want $2)"; fail=$((fail+1)); fi; }
```

**Existing lane-2-then-override case to model the new regression case on** (lines 29-36):
```bash
# lane 2: rewrite an EXISTING source line (api unchanged) -> blocked; override allows
git reset --hard HEAD~1
git checkout -q -- .; git clean -fdq
printf 'val x = 999\n' > src/main/A.kt   # A.kt started as 'val x = 1' at tag v1.0.0 -> line rewrite = lane 2
git add -A
set +e; git commit -qm "behavior change"; check "$?" 1 "lane-2 commit blocked"; set -e
set +e; HUB_LANE_OVERRIDE=2 git commit -qm "declared behavior change"; check "$?" 0 "declared lane-2 allowed"; set -e
```

**New regression case to append** (per RESEARCH.md Code Examples, reproducing the exact 5b01532
sequence): after a lane-2 commit lands WITH override (reuse the block above, or a fresh minimal
variant), commit an unrelated non-`src/main` change (e.g. a file under a `docs/` or top-level path
outside `src/main`, mirroring `.planning/`) with **no override set**, and assert exit `0`:
```bash
# NEW: post-fix regression — a src/main rewrite landed earlier (lane 2, with override) must not
# poison a LATER, unrelated non-src/main commit into also being blocked (the exact 5b01532-then-
# .planning/-commit bug this phase fixes).
printf 'notes\n' > NOTES.md; git add -A
set +e; git commit -qm "docs only, no override"; check "$?" 0 "post-lane-2 unrelated commit unblocked (GOV-03 fix)"; set -e
```
Place this immediately after the existing lane-2 case block (after line 36) so it exercises the
hook state right after a legitimate override-landed src rewrite — the precise poisoned-history
shape RESEARCH.md's Code Examples section calls out.

---

### `src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt` (NEW test)

**Analog:** `src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistryDriftGuardTest.kt` (253 lines, fully read live this session — verbatim reuse target).

**Package + imports pattern** (lines 1-6):
```kotlin
package io.github.ygaray.yahirandroidtaste.explorer

import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.File
```

**Scan-scope field to duplicate exactly** (line 54):
```kotlin
private val excludedPackages = setOf("explorer")
```

**Vacuous-pass guard sequence to duplicate exactly** (lines 60-94) — three `assertTrue` gates
before drawing any coverage conclusion: non-empty `.kt` file scan, non-empty post-exclusion file
scan, non-empty extracted-name set. Copy this three-tier structure verbatim, adjusted only for the
new predicate's terminology (RESEARCH.md's own "Don't Hand-Roll" table calls this out explicitly —
this codebase's guard was "specifically hardened against" the vacuous-pass failure mode).

**Source-root resolution helper to duplicate exactly** (lines 121-148, `resolveModuleSourceRoot`)
— robust CWD-independent walk-up-then-fallback; copy verbatim, it is CWD-agnostic and has no
coupling to `ComponentRegistry`.

**Extraction method to duplicate exactly** (lines 163-198, `extractPublicTopLevelComposableNames`)
and its two helpers (`findDeclarationLineIndex`, lines 214-232; `FUN_DECLARATION_REGEX` +
`MAX_DECLARATION_LOOKAHEAD_LINES`, lines 240-251) — these are `private` on the sibling class and
not importable; RESEARCH.md's "Don't Hand-Roll" table and the phase's own Anti-Patterns section
explicitly direct duplication over refactor-to-shared-utility this phase. Copy the full body
verbatim (handles generic type params and extension-receiver composables — both live patterns in
this codebase).

**New predicate to write (not present in the analog — this is the delta):** after extracting
`scannedComposableNames` the same way the analog does (lines 85-94 shape), instead of comparing
against `ComponentRegistry.entries`/`INTENTIONALLY_UNREGISTERED`, compute each name's **head token**
(leading PascalCase word — RESEARCH.md Open Question 2 / Pitfall 3 recommends `Regex("[A-Z][a-z0-9]*")`
first match) and fail for any name whose head token is not in a seed `PRIMITIVE_NOUN_ALLOWLIST` set
AND whose full name is not in a new `DOMAIN_VOCABULARY: Map<String, String>` allowlist (name →
rationale), mirroring the `INTENTIONALLY_UNREGISTERED` shape:

**Allowlist-map shape to mirror** (from `ComponentRegistry.kt` lines 109-114, the sibling
`INTENTIONALLY_UNREGISTERED` convention this phase's new `DOMAIN_VOCABULARY` map should copy the
shape of, per D-02/D-03 in CONTEXT.md):
```kotlin
val INTENTIONALLY_UNREGISTERED: Map<String, String> = mapOf(
    "WaveformCanvas" to
        "Sub-part rendered inside RecordingBottomSheetContent, and called directly by " +
        "VoiceCard's clip mini-rows (Phase 129 DS-03 D-02, VoiceClipRow) — VoiceCard's " +
        "own overview strip still uses its separate private wrapper (VoiceWaveformCanvas). " +
    ...
```
name → free-text rationale string, string-concatenated across lines for readability. The new
`DOMAIN_VOCABULARY` map in the test file (or a small object it references) should use this same
`Map<String, String>` shape — each entry an acknowledged domain-coupled name with its rationale, so
"fail-until-allowlisted" produces an audit trail (D-02's explicit requirement), not a silent pass.

**Fail-message shape to mirror** (lines 101-109):
```kotlin
if (offendingNames.isNotEmpty()) {
    fail(
        "Found ${offendingNames.size} public top-level @Composable function(s) outside " +
            "the excluded packages ($excludedPackages) that are neither registered in " +
            "ComponentRegistry.entries nor allowlisted in " +
            "ComponentRegistry.INTENTIONALLY_UNREGISTERED: " +
            "${offendingNames.sorted()} — register each as a new explorer entry, or add " +
            "it to INTENTIONALLY_UNREGISTERED with a one-line reason (D-04)."
    )
}
```
Adapt wording to the new guard's own remediation instruction ("add `<Name>` to
`DOMAIN_VOCABULARY` with a rationale") but keep the same `fail()`-with-sorted-offender-list shape.

---

### `docs/DESIGN-INTENT.md` (append, GOV-01)

**Analog:** itself — existing structure (74 lines total), sections in order: `## The Primitives
Contract` (line 13), `## The Patterns Contract` (line 24), `## The Litmus` (line 39), `## Worked
Examples (the three borderline cases)` (line 54), `## Applying the Litmus` (line 68).

**Where to append:** two new sections after the existing `## Applying the Litmus` (currently the
last section, ending at line 74) — per D-04/Runtime Decision, extend this file rather than create
`CONTRIBUTING.md`. Follow the existing section's heading level (`##`) and prose register (short,
declarative, cross-referencing decision IDs like `D-03`, matching the existing file's own citation
style seen in `## The Litmus`/`## Worked Examples`).

**Content shape to add (per CONTEXT.md D-04 + RESEARCH.md Architecture Patterns GOV-01 block):**
1. A section naming the tier-aware contribution litmus: primitives get the strict gate
   (mechanically enforced — cross-reference `DomainVocabularyDriftGuardTest`'s fail-until-
   allowlisted guard), patterns get the loose/opinion-allowed gate (prose-only, no enforcement
   surface — explicitly note no `.github/` PR-template or CI-review checklist exists to wire a
   stricter check into).
2. Keep the enforcement claim scoped accurately: "enforced where feasible" = the GOV-02 guard
   checks the strict-primitives half only.

## Shared Patterns

### Fail-closed / fail-loudly on unexpected state
**Source:** `tools/hooks/pre-commit` lines 26-28 (unexpected classifier exit code → block); `tools/verify-additive-diff.sh` lines 41-44 (unresolvable ref → fail); `ComponentRegistryDriftGuardTest.kt` lines 60-94 (vacuous-pass guard) and lines 183-190/223-231 (`check`/`error` on unparseable declaration).
**Apply to:** All modified/new files in this phase — the codebase's governance layer never silently degrades to a pass on a broken assumption; every new code path (staged-delta diff basis, head-token extraction, allowlist lookup) must fail loudly rather than default-pass.

### Set-diff via `comm -23` on normalized/sorted line lists
**Source:** `tools/verify-additive-diff.sh` lines 89-101; `tools/verify-api-additive.sh` line 24.
**Apply to:** Any staged-delta comparison logic touched in `tools/verify-additive-diff.sh` (and `tools/verify-api-additive.sh` if scope widens) — reuse the existing normalize+`comm -23` shape rather than reimplementing line-set comparison.

### `Map<String, String>` name → rationale allowlist, fail-until-acknowledged
**Source:** `ComponentRegistry.kt` lines 109-114 (`INTENTIONALLY_UNREGISTERED`).
**Apply to:** The new `DOMAIN_VOCABULARY` allowlist in `DomainVocabularyDriftGuardTest.kt` — same shape (name key, string-concatenated rationale value), same audit-trail intent (D-02).

### Bash fixture-test harness (init tmp repo, copy tooling, sequence commits, assert exit codes)
**Source:** `tools/test/test-precommit-hook.sh` lines 1-16.
**Apply to:** The new regression case appended to this same file — reuse `check()`, the existing tmp-repo scaffold, and the existing cleanup/reset (`git reset --hard HEAD~1; git checkout -q -- .; git clean -fdq`) idiom already used between cases (lines 31-32, 39-40).

## No Analog Found

None — every file in this phase's scope is either an existing file being modified in place, or a
new file with a direct, fully-read structural twin already in the codebase (GOV-02's new test vs.
`ComponentRegistryDriftGuardTest`; the `verify-api-additive.sh` widening, if taken, vs.
`verify-additive-diff.sh`'s already-fixed shape).

## Metadata

**Analog search scope:** `tools/`, `tools/test/`, `tools/hooks/`, `src/test/java/io/github/ygaray/yahirandroidtaste/explorer/`, `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistry.kt`, `docs/DESIGN-INTENT.md`.
**Files scanned:** 8 (all read in full this session, cross-checked live against current repo state — no line numbers taken on faith from RESEARCH.md alone).
**Pattern extraction date:** 2026-09-01
