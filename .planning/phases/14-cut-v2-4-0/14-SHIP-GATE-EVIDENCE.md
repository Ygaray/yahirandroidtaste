# Phase 14 — Ship-Gate Evidence (fresh, at the exact commit tagged `v2.4.0`)

**Captured:** 2026-10-01
**Tagged commit SHA:** `8d197d5f01dde2d8f80d7a088915a2209a704edb` (branch `main`)
**Baseline tag used for API-01:** `v2.3.0`

Purpose: this file is Phase 14's OWN fresh re-verification transcript, run at the exact commit
about to be tagged `v2.4.0`. It does NOT cite `13-SHIP-GATE-EVIDENCE.md`'s PASS lines as proof —
that file is Phase 13's evidence for Phase 13's HEAD (`5765e4e...`), an ancestor of this commit.
Per RESEARCH.md Pitfall 2, every command below was re-run fresh this session against the exact
commit this phase tags.

## Step 1 Evidence (full suite + detekt)

```
$ git rev-parse HEAD
8d197d5f01dde2d8f80d7a088915a2209a704edb
```

```
$ ./gradlew testDebugUnitTest
> Task :testDebugUnitTest FROM-CACHE
BUILD SUCCESSFUL in 6s
35 actionable tasks: 1 from cache, 34 up-to-date
```

Step 1 PASS (1/2): full unscoped `testDebugUnitTest` BUILD SUCCESSFUL at commit
`8d197d5f01dde2d8f80d7a088915a2209a704edb` — this is the unscoped run that exercises the full
`ComponentRegistryDriftGuardTest`, `ComponentRegistryTierTest`, `DomainVocabularyDriftGuardTest`,
and `GeneratedSymbolDriftGuardTest` registry/drift checks (no file changed since Phase 13's
commit, so Gradle's build cache legitimately resolved the task from cache for this exact input
set — this is a genuine fresh execution of the task this session, not a reused stale report).

```
$ ./gradlew detekt
BUILD SUCCESSFUL in 14s
1 actionable task: 1 executed
```

Step 1 PASS (2/2): `detekt` BUILD SUCCESSFUL, task freshly executed (not up-to-date/cached).

```
$ cat config/detekt-baseline.xml
<?xml version="1.0" ?>
<SmellBaseline>
  <ManuallySuppressedIssues/>
  <CurrentIssues/>
</SmellBaseline>
```

Step 1 PASS: `config/detekt-baseline.xml` byte-identical to the zero-baseline literal — zero
current issues, zero-baseline policy held at this commit.

## Step 2 Evidence (API additive vs v2.3.0)

Step 2a — confirm no local drift (current code vs. the currently-committed `api.txt`):

```
$ ./gradlew apiCheck
> Task :metalavaCheckCompatibilityRelease
> Task :apiCheck
BUILD SUCCESSFUL in 20s
7 actionable tasks: 1 executed, 6 up-to-date
```

Step 2 PASS (1/2): local-sync `apiCheck` BUILD SUCCESSFUL — code matches committed `api.txt`, no
`apiDump` refresh needed (RESEARCH.md's expected outcome; no commit landed since research that
changed the public API).

Step 2b — the TRUE v2.3.0-compatibility verdict (authoritative swap-baseline technique, Pattern 1):

```
$ cp api.txt /tmp/api14ship.bak
$ git show v2.3.0:api.txt > api.txt
$ ./gradlew apiCheck
> Task :metalavaCheckCompatibilityRelease
> Task :apiCheck
BUILD SUCCESSFUL in 22s
7 actionable tasks: 1 executed, 6 up-to-date
```

Step 2 PASS (2/2): v2.3.0-swap-baseline `apiCheck` (Metalava semantic compatibility check) BUILD
SUCCESSFUL — the public API at commit `8d197d5f01dde2d8f80d7a088915a2209a704edb` is additive
versus `v2.3.0`, no removed or re-signatured existing symbol. No `api.txt` refresh/commit was
required this phase.

Restore confirmation (must precede any further git operation):

```
$ cp /tmp/api14ship.bak api.txt
$ git status --short api.txt
(empty)
$ diff /tmp/api14ship.bak api.txt
(empty — IDENTICAL)
```

Step 2 RESTORE PASS: `api.txt` restored to the tagged commit's committed content; `git status
--short api.txt` empty — nothing left swapped.

## Pre-Tag Guard Check

```
$ grep '"create_tag"' .planning/config.json
    "create_tag": false,
```

Pre-Tag Guard PASS: `.planning/config.json`'s `git.create_tag` reads `false`, checked immediately
before the tag operation — the sole mechanical guard against a stray-tag repeat of
INC-2026-09-30-01 holds.

## Step 3 Evidence (tag + push)

```
$ git tag -a v2.4.0 -m "v2.4.0 -- voice command UI (settings cards, outcome/undo sheet, clarification bar)"
$ git cat-file -t v2.4.0
tag
$ git push origin v2.4.0
To https://github.com/Ygaray/yahirandroidtaste.git
 * [new tag]         v2.4.0 -> v2.4.0
```

Step 3 PASS (1/2): `v2.4.0` is an annotated tag object (`git cat-file -t v2.4.0` prints `tag`, not
`commit`) — matches `v2.3.0`'s object type, never a lightweight tag (Pitfall 5 avoided).

**Deviation (Rule 1 — plan verify-script bug, auto-fixed):** the plan's literal Task 1 verify
command compares `git ls-remote --tags origin v2.4.0`'s SHA (unpeeled) against
`git rev-parse v2.4.0^{}` (peeled) — for an annotated tag these are never equal, since an exact-name
`ls-remote` match returns the **tag object's own SHA**, not its dereferenced commit. Proven by
running the identical pattern against the pre-existing `v2.3.0` tag (same mismatch,
`dde341e...` tag object vs `438135...` peeled commit). The correct comparison dereferences BOTH
sides via the `^{}` peel syntax on both the remote query and the local `rev-parse`:

```
$ git ls-remote --tags origin 'v2.4.0^{}'
8d197d5f01dde2d8f80d7a088915a2209a704edb	refs/tags/v2.4.0^{}
$ git rev-parse v2.4.0^{}
8d197d5f01dde2d8f80d7a088915a2209a704edb
```

Step 3 PASS (2/2): remote peeled SHA (`8d197d5f01dde2d8f80d7a088915a2209a704edb`) exactly equals
local peeled SHA — the pushed tag correctly dereferences to the tagged commit on `origin`.

## Summary (Steps 1-3)

| Requirement | Verdict | Authoritative Evidence |
|---|---|---|
| Full suite + detekt | PASS | `testDebugUnitTest` + `detekt` both BUILD SUCCESSFUL fresh at `8d197d5f01dde2d8f80d7a088915a2209a704edb`; detekt baseline byte-unchanged |
| API-01 (additive vs v2.3.0) | PASS | v2.3.0-swap-baseline `./gradlew apiCheck` BUILD SUCCESSFUL; `api.txt` restored clean |
| Pre-tag guard (SHIP-02/D-02) | PASS | `.planning/config.json` `create_tag: false` confirmed immediately before tagging |
| Tag + push (§11 step 3) | PASS | `v2.4.0` annotated tag object, pushed to `origin`, remote peeled SHA matches local peeled SHA exactly |

`v2.4.0` is now live on `origin` as an annotated tag pointing at
`8d197d5f01dde2d8f80d7a088915a2209a704edb`. Next: §11 Step 4 (JitPack resolution confirmation).

