---
phase: 19-cut-v2-5-0
fixed_at: 2026-10-05T00:00:00Z
review_path: /home/yahir/Projects/Reusable/android/yahirandroidtaste/.planning/phases/19-cut-v2-5-0/19-REVIEW.md
iteration: 1
findings_in_scope: 8
fixed: 8
skipped: 0
status: resolved
---

# Phase 19: Code Review Fix Report

**Fixed at:** 2026-10-05
**Source review:** /home/yahir/Projects/Reusable/android/yahirandroidtaste/.planning/phases/19-cut-v2-5-0/19-REVIEW.md
**Iteration:** 1

**Summary:**
- Findings in scope: 8 (5 warning, 3 info; fix_scope=all)
- Fixed: 8
- Skipped: 0 (one sub-item of IN-03 is a documented acceptable-skip, see below)

**Verification environment:** all gates ran in the MAIN checkout (`use_worktrees` is true in config, but the
parent task required committing directly on `main` and staging by name, and the untracked
`.planning/phases/19-cut-v2-5-0/.gate/` AARs only exist there; no worktree, temp branch or recovery sentinel
was created). After each fix `bash tools/test/test-verify-binary-abi.sh` was run in the foreground; after
WR-05, WR-02, WR-01 and IN-01 the gate was also re-run against the REAL AARs (`.gate/jitpack-v2.4.1.aar` vs
`.gate/jitpack-v2.5.0.aar`, `SKIP_BUILD=1`) and still reports `0 missing`. Final `tools/test/run-all.sh`:
classify 12/12, precommit-hook 6/6, verify-additive-diff 5/5, verify-binary-abi 52/52, all ok.
No tag was touched, nothing was pushed, `src/` was not edited. Only `tools/` and `API.md` changed.

## Fixed Issues

### WR-03: The dirty-input check ignores untracked files
**Files modified:** `tools/verify-binary-abi.sh`, `tools/test/test-verify-binary-abi.sh`
**Commit:** 40d8da6
**Applied fix:** `git status --untracked-files=all`, plus a `git ls-files --others --ignored --exclude-standard -- src`
check (ignored files are also compiled by Gradle). Fixture case (g2) proves an untracked `src/` file exits 1.

### WR-04: Baseline "tag" accepted as any commit-ish
**Files modified:** `tools/verify-binary-abi.sh`, `tools/test/test-verify-binary-abi.sh`
**Commit:** d4d0133
**Applied fix:** existence check is now `refs/tags/$TAG^{commit}`, so a branch name or `HEAD` exits 1; the Gradle
cache pick is `ls ... | sort | head -1` (deterministic). Fixtures reject `main` and `HEAD`. The optional strict
semver regex was NOT added: milestone-style bare tags (e.g. `v2.2`) are legitimate tags in this repo.

### WR-05: `normalize` misattributes members of classes with modifier-less headers
**Files modified:** `tools/verify-binary-abi.sh`, `tools/test/test-verify-binary-abi.sh`
**Commit:** 8576da5
**Applied fix:** class-header regex anchored as `^([a-z]+ )*(class|interface) ...`; a `descriptor:` line seen
before any class header now aborts with exit 2. Fixture `p.Hidden` (package-private class); confirmed the new
test fails against the previous script (it returned a false PASS, exit 0).

### WR-02: `javap -public` excludes protected members
**Files modified:** `tools/verify-binary-abi.sh`, `tools/test/test-verify-binary-abi.sh`, `API.md`, `tools/README-api-guard.md`
**Commit:** 6ed57f4
**Applied fix:** `javap -protected -s`; script header, README and the API.md gate paragraph now say "public or
protected". Historical API.md text describing how the v2.5.0 proof was originally run (`javap -public -s`) was left
as written. Fixture: removed protected method exits 3. (The real v2.4.1 AAR does contain one protected member
line, so the baseline listing grew by 1; the v2.4.1 to v2.5.0 diff still shows 0 missing.)

### WR-01: Class-level and supertype changes are never compared
**Files modified:** `tools/verify-binary-abi.sh`, `tools/test/test-verify-binary-abi.sh`, `tools/README-api-guard.md`
**Commit:** 52792e7
**Status:** fixed: requires human verification (logic change to the gate's comparison semantics)
**Applied fix:** the awk now also emits `class#@class|@interface`, `@public`, `@nonfinal`, `@concrete` and one
`@super <erased type>` per extends/implements entry (generics erased) instead of a verbatim header, so adding an
interface or opening a class stays additive. Fixtures: member-less interface removed, supertype dropped, open class
made final all exit 3; the reverse directions exit 0. Real-AAR run: baseline listing is now 3885 lines (head 3943),
0 missing, no javap stderr; README sanity-floor figure updated 2526 to 3885. Known strictness to be aware of:
replacing a direct supertype by an intermediate one is reported as missing (documented in the README); lane 3 is
"STOP", so that errs on the safe side.

### IN-01: `*_Factory*` / `*_MembersInjector*` exclusion is a substring match
**Files modified:** `tools/verify-binary-abi.sh`, `tools/test/test-verify-binary-abi.sh`
**Commit:** a796cdf
**Applied fix:** pattern is now `*_Factory|*_Factory\$*|*_MembersInjector|*_MembersInjector\$*`. Fixture:
dropping `Foo_FactoryHelper` exits 3.

### IN-02: `classify-hub-change.sh` accepts any `--mode` and builds JSON by interpolation
**Files modified:** `tools/classify-hub-change.sh`, `tools/test/test-classify-hub-change.sh`
**Commit:** 6022db6
**Applied fix:** `--mode` validated to `additive|curation`, `--baseline`/`--mode` guard a missing value with a usage
message (exit 1), `--baseline` checked against the same ref allow-list as the ABI gate (also keeps `--json` valid).
Four new fixture checks.

### IN-03: Fixture test gaps and fragile `reset` offsets
**Files modified:** `tools/test/test-verify-binary-abi.sh`, `tools/test/test-precommit-hook.sh`
**Commit:** f42fb1a
**Applied fix:** hook test tags the "additive" commit (`t-additive`) and resets to the tag instead of
`HEAD~1`/`HEAD~2`. ABI test gained: Gradle-cache baseline branch, successful JitPack download branch
(`JITPACK_BASE=file://...`), and a baseline-side-only sanity-floor case. The dirty-tree exit 1 path (WR-03),
marker-interface (WR-01), bare-class (WR-05) and protected-member (WR-02) fixtures were added with their findings.
**Documented acceptable-skip (sub-item):** the default `--proto =https` curl path stays untested offline; it can only
be exercised against the real JitPack over the network (and `JITPACK_BASE` deliberately drops the https-only flags).
That path was already exercised for real during the v2.5.0 cut (`.gate/abi-published.log`).

---

_Fixed: 2026-10-05_
_Fixer: Claude (gsd-code-fixer)_
_Iteration: 1_
