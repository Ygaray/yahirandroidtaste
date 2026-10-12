---
phase: 19-cut-v2-5-0
reviewed: 2026-10-05T00:00:00Z
depth: standard
files_reviewed: 8
files_reviewed_list:
  - API.md
  - tools/README-api-guard.md
  - tools/classify-hub-change.sh
  - tools/hooks/pre-commit
  - tools/test/test-classify-hub-change.sh
  - tools/test/test-precommit-hook.sh
  - tools/test/test-verify-binary-abi.sh
  - tools/verify-binary-abi.sh
findings:
  critical: 0
  warning: 5
  info: 3
  total: 8
status: resolved
---

# Phase 19: Code Review Report

**Reviewed:** 2026-10-05
**Depth:** standard
**Files Reviewed:** 8 (of the 10 listed; `tools/verify-api-additive.sh` and `tools/test/test-verify-api-additive.sh` were deleted by this phase and no longer exist)

## Summary

The new javap AAR-diff gate (`tools/verify-binary-abi.sh`) is well built. It fails closed on javap errors, applies a sanity floor, validates entry names, prints SEAMS, and defends the download with `--proto =https`. `tools/test/run-all.sh` passes (26/26 for the ABI fixture test, 6/6 for the pre-commit hook test). I also ran the descriptor-extraction awk against the real v2.4.1 AAR (`.gate/jitpack-v2.4.1.aar`): all 411 class headers parse and javap emits no stderr, so the recorded 0-missing result is not a parser artifact.

The gate's guarantee is narrower than its documentation claims, though. Its header says "every public descriptor present in the baseline must still exist at HEAD" and README/API.md call it the binary compatibility gate. Several classes of binary-breaking change are invisible to it (WR-01, WR-02). The dirty-tree precondition can also let an AAR that does not reflect the commit through (WR-03). None of these invalidate the v2.5.0 result: the v2.4.1 to HEAD diff has 0 missing and the Kotlin AAR has no protected members. They are gaps for the next release that reuses this gate. No security vulnerabilities were found: the entry-name allow-list, single-member unzip and https-only curl are sound.

## Warnings

### WR-01: Class-level and supertype changes are never compared; only member lines are

**File:** `tools/verify-binary-abi.sh:110-113`
**Issue:** The awk emits a line only on `descriptor:` lines, so the normalized listing contains members only. The class header (modifiers, `extends`, `implements`) never reaches the listing. Consequences, all binary-breaking and all reported as PASS:
- a public class or interface with zero public members (marker interface, empty annotation, member-less sealed interface) can be deleted at HEAD with no listing difference;
- a class that drops a supertype or interface, or changes `extends`, produces no diff;
- `public` to `final`, or `class` to `abstract`, produces no diff.

Only a class that has at least one public member and is removed shows up.
**Fix:** Also emit one synthetic line per class header, with `cls` normalized to a stable form, e.g. in the class-header rule add `print cls "#<class> " $0`. Alternatively, add `-v` or the class line verbatim after stripping `Compiled from`. Keep the existing member lines. Add a fixture variant (marker interface removed, `implements` dropped) that must exit 3.

### WR-02: `javap -public` excludes protected members, which are binary API for subclassing consumers

**File:** `tools/verify-binary-abi.sh:107`
**Issue:** I verified that `javap -public` omits `protected` members (a `protected void prot(int)` and a `protected abstract void must()` in public classes do not appear in the output). A protected method or constructor of an open or abstract public class is callable and overridable from consumer subclasses, so removing or changing it is binary-breaking, yet the gate would pass. The script, README and API.md all describe the contract as "public descriptors", which is accurate for the code and incomplete for the rule it enforces ("never change a tagged public signature"). The v2.4.1 AAR currently has no protected members (grep count 0), so this is latent.
**Fix:** Use `javap -protected -s` (it includes public and protected), and update the wording to "public and protected". Add a fixture where a protected method is removed and expect exit 3.

### WR-03: The dirty-input check ignores untracked files, so the AAR can differ from the commit

**File:** `tools/verify-binary-abi.sh:56`
**Issue:** `git status --porcelain --untracked-files=no -- src ...` deliberately skips untracked files. Gradle compiles every file under `src/`, tracked or not, so an untracked `src/main/.../Foo.kt` ends up in the HEAD AAR. That defeats the stated guarantee ("the AAR would not reflect the commit") and can produce a false PASS: an untracked file can re-add a class or member that the committed tree removed. Ignored files under `src` have the same effect.
**Fix:** Count untracked files in the artifact paths too:
```bash
DIRTY="$(git status --porcelain --untracked-files=all -- src build.gradle.kts settings.gradle.kts gradle.properties gradle config api.txt jitpack.yml)"
```
Optionally also fail on `git ls-files --others --ignored --exclude-standard -- src` output. Add a fixture or note covering the untracked case.

### WR-04: The baseline "tag" is accepted as any commit-ish, but the AAR is fetched by name

**File:** `tools/verify-binary-abi.sh:42-46, 74-84`
**Issue:** The tag regex allows letters, digits, `.`, `_`, `/`, `-`, and the existence check is `git rev-parse --verify "$TAG^{commit}"`. That accepts branch names (`main`) and `HEAD`. Gradle-cache and JitPack resolution then use the literal string as the version. For `main` that asks JitPack to build a moving branch, which CLAUDE.md forbids (never `main-SNAPSHOT` / moving refs). Nothing ties the fetched AAR to the commit the local tag points at either, and the cache glob `*/` takes whichever hash directory `ls | head -1` returns first. Only hashes are printed, not pinned.
**Fix:** Require a real tag: `git rev-parse --verify --quiet "refs/tags/$TAG^{commit}"`. Optionally also require `^v[0-9]+\.[0-9]+\.[0-9]+$`, and use `ls -d ... | sort` so the cache pick is deterministic.

### WR-05: `normalize` misattributes members of any class whose header does not match the awk class regex

**File:** `tools/verify-binary-abi.sh:111`
**Issue:** The class-header rule is `/ (class|interface) [^ ]+.*\{$/`, which needs a space before `class`. A modifier-less header such as `class p.Hidden {` (package-private, non-final, which javac emits for Java sources and which `-public` still prints with its public members) does not match. `cls` then keeps the previous class's name, or is empty for the first class. I reproduced this: members of `p.Hidden` came out as `p.A#hid()V` or `#hid()V` depending on class order. A change in class ordering or in which classes exist then yields spurious "missing" lines (false exit 3), and a collision with a real descriptor can mask a removal. The real v2.4.1 AAR is Kotlin-only (all 411 headers carry a modifier), so today it is latent.
**Fix:** Anchor to the line start instead: `/^[a-z ]*(class|interface|enum) [^ ]+.*\{$/` (or `/(^| )(class|interface) /`). Fail the build if a `descriptor:` line is seen while `cls == ""`.

## Info

### IN-01: `*_Factory*` / `*_MembersInjector*` exclusion is a substring match on the whole class name

**File:** `tools/verify-binary-abi.sh:100`
**Issue:** `case "$n" in *_Factory*|*_MembersInjector*)` skips any class whose fully qualified name contains the text anywhere, including package segments or hand-written classes such as `Foo_FactoryHelper`. Removal of those public classes would be ignored. The documented intent is only Dagger-generated classes.
**Fix:** Anchor to the simple-name suffix: `*_Factory|*_Factory\$*|*_MembersInjector|*_MembersInjector\$*`.

### IN-02: `classify-hub-change.sh` accepts any `--mode` value and builds JSON by interpolation

**File:** `tools/classify-hub-change.sh:13, 30`
**Issue:** The usage line says `additive|curation` but the value is not validated, so a typo silently behaves as `additive` (the strict direction, so safe). `--mode` or `--baseline` without a following value dies with an `unbound variable` message rather than the usage text. The `--json` output interpolates `$BASE` and `$MODE` unescaped, so a value containing `"` produces invalid JSON.
**Fix:** `case "$MODE" in additive|curation) ;; *) echo "bad --mode" >&2; exit 1;; esac`; guard `$2` with `[ "$#" -ge 2 ]`; restrict `$BASE` to the same ref allow-list used in verify-binary-abi.

### IN-03: Fixture test gaps and fragile `reset` offsets

**File:** `tools/test/test-verify-binary-abi.sh`, `tools/test/test-precommit-hook.sh:32`
**Issue:**
- The ABI fixture test never covers: the dirty-tree exit 1 path, the baseline-side sanity floor on its own, the Gradle-cache and JitPack-success resolution branches, the default `--proto =https` curl path (the test forces `JITPACK_BASE=http://...`), a bare-`class` or marker-interface fixture (WR-01, WR-05), or a protected-member fixture (WR-02).
- In `test-precommit-hook.sh`, the `git reset --hard HEAD~1` / `HEAD~2` offsets assume every earlier `git commit` succeeded. If case (2) is blocked, `HEAD~1` lands on `base` instead of `additive`, and the later cases then test a different state (they could still pass or fail for the wrong reason).
**Fix:** Add the missing cases above. In the hook test, tag the "additive" commit (`git tag t-additive`) and reset to the tag instead of relative offsets.

---

_Reviewed: 2026-10-05_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
