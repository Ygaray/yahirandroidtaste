# Phase 19: Cut v2.5.0 - Research

**Researched:** 2026-10-05
**Domain:** Release engineering for a JitPack-published Android library: shell tooling (javap AAR-diff ABI gate, pre-commit lane classifier), docs, and the section-11 immutable-tag cut.
**Confidence:** HIGH (every mechanism below was reproduced in this session against the real repo; the only MEDIUM items are orchestrator-coordination behavior and executor messaging capability)

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- **D-01 [tag-gate]:** Yahir **GRANTED the A12 tag-cut waiver for this effort's YAT tag**, directly in-session (2026-10-05). The agent MAY cut `v2.5.0` autonomously once Phase 18 verification is green (full section-11 battery on the exact tagged HEAD, API strictly additive, pushed, JitPack-resolvable) - no further human gate required for this tag. Resolution (human). - **Reversibility:** one-way - a cut immutable public JitPack tag cannot be rescinded; a mistake is remedied only by a corrective patch tag. This waiver applies to v2.5.0 within the vae-bilingual effort, not a standing change to the repo's human-gated shipping policy.
- **D-02 [cut-mechanism]:** Cut via the orchestrator's `xrepo build`, one repo at a time (orchestrator directive). Resolution (human). Relay the full section-11 ledger row to `yahir-gsd-control-plane-6e`; never write the section-11 ledger here. `git.create_tag` stays false (no stray marker tag - SHIP-02 guard).
- **D-03 [binary-abi-gate] (LOCKED - orchestrator yahir-gsd-control-plane-3b ruling, 2026-10-05, F3 of the binary-compat fix plan):** Before the `v2.5.0` tag:
  - **Add the script.** Add `tools/verify-binary-abi.sh <baseline-tag>`, invoked here as `tools/verify-binary-abi.sh v2.4.1`. It builds the release AAR at HEAD and gets the baseline AAR (the tag's JitPack artifact or a cached/worktree build). It runs `javap -public -s` over every class in each `classes.jar` and normalizes the output to sorted `class#member descriptor` lines. It then does an append-only diff.
  - **The gate.** **Zero missing public descriptors** at HEAD versus the baseline. Exclude Dagger `*_Factory` / `*_MembersInjector` and `ComposableSingletons$*`. Keep a baseline line-count sanity floor so an empty javap run can't pass. The proven command and its output are in the quick 261005-dmc / 261005-e2e SUMMARYs (v2.4.1 baseline = 2526 lines; missing=0 at `0956d79`). It **must be green on the exact tagged HEAD before the tag is cut**. Any missing descriptor is lane 3: STOP, never waive.
  - **Hook change.** Delete the raw-line `api.txt` check (`tools/verify-api-additive.sh` invocation / `API_FILE` path, INC-2026-10-05-02, seed S-003) from `tools/hooks/pre-commit`. Metalava `apiCheck` stays as the per-commit source-level gate.
  - **Docs.** Update `tools/README-api-guard.md` and API.md section "The binary-compatibility rule" to point at the script.
  - The control plane closes INC-2026-10-05-02 when F3 runs green on the tag. _(source: orchestrator ruling - not revisitable by the planner)_

### Claude's Discretion
- If `xrepo build` is unavailable/undefined at cut time, fall back to the in-repo manual annotated-tag+push precedent (v2.2.0/v2.4.1) after confirming with the orchestrator.

### Deferred Ideas (OUT OF SCOPE)
Consumer repins (SB/CT -> v2.5.0) are Wave-1, each consumer's own channel - not this phase.
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| SHIP-03 | Immutable tag `v2.5.0` cut via the section-11 protocol (verification green, API additive, seams honored, pushed, JitPack builds it), ledger row relayed to the orchestrator; no stray marker tag (`git.create_tag` false). (The "human-gated / waiver pending" wording in REQUIREMENTS/ROADMAP is superseded by D-01.) | Sections "Section-11 Cut Protocol", "F3 Tooling Work", "Common Pitfalls", "Validation Architecture" |
| D-03 (F3) | `verify-binary-abi.sh` + hook raw-line check deleted + docs updated, green on the exact tagged HEAD before the tag | "F3 Tooling Work" (prototype proven: base=2526, head=2584, missing=0) |
</phase_requirements>

## Summary

Phase 19 is two pieces of work in strict order. **Plan A (F3, D-03)** is ordinary tooling: add `tools/verify-binary-abi.sh`, unwire the raw-line `api.txt` check from the pre-commit lane pipeline, update tests and docs. **Plan B (the cut)** is the irreversible section-11 ritual: fresh battery on a recorded HEAD, ABI gate on that HEAD, annotated tag on that exact SHA, push, JitPack confirmation, and a relayed ledger row. No production source changes are needed; `git diff b9848a9 HEAD -- src api.txt build.gradle.kts gradle config jitpack.yml tools` is empty, and `build.gradle.kts`/`gradle`/`config`/`jitpack.yml`/`settings`/`gradle.properties` are unchanged versus `v2.4.1` [VERIFIED: `git diff --stat v2.4.1 HEAD -- build.gradle.kts gradle settings.gradle.kts gradle.properties jitpack.yml config` returned nothing], so the JitPack toolchain risk is minimal.

Four findings change the plan versus how the CONTEXT/ROADMAP describe it. (1) **The pre-commit hook does not call `verify-api-additive.sh` directly**: it exports `API_FILE` and calls `classify-hub-change.sh`, which calls the script. Deleting only the hook's `export` line makes the classifier fail closed (exit 1) and blocks every commit, so the classifier must change too. (2) **The hook's raw-line check has been a silent no-op**: the hook exports an *absolute* `API_FILE`, so the script's `git cat-file -e "$BASE:$API_FILE"` fails and it degrades to exit 0. Deleting it changes no live behavior; "fixing the path" instead would have started blocking commits on the 10 known false-positive lines. (3) **`xrepo build` is not a cut tool**: it is a release-build *lock/window* (`build start|done` writing `builds.jsonl`) that only runs from inside the control-plane repo. The tag and push are performed in this repo. (4) **The orchestrator is now `yahir-gsd-control-plane-3b`**, not `-6e`; `-6e` is the previous (restarted) session.

**Primary recommendation:** Two plans. Plan 19-01 (autonomous, wave 1): TDD `tools/verify-binary-abi.sh` + new shell test, unwire the raw-line check (classifier + hook + tests), `git rm` the retired script and its test, update `tools/README-api-guard.md` and API.md "The binary-compatibility rule"; commit; no src change. Plan 19-02 (autonomous: true per D-01, depends_on 19-01): record `GATED_HEAD`, run the fresh battery + swap-baseline Metalava + `tools/verify-binary-abi.sh v2.4.1` on it, tag `GATED_HEAD` explicitly as an annotated tag, push the tag only, poll JitPack to `status:ok / isTag:true / commit match`, then write the ledger-row artifact for relay. Make no commits between the battery and the tag.

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Binary-ABI gate (javap diff) | Repo build tooling (`tools/*.sh`, bash + JDK 17) | Gradle `assembleRelease` | Metalava models Kotlin signatures only; the JVM descriptor is only visible in the compiled `classes.jar` |
| Per-commit source/behavior lane signal | git pre-commit hook -> `classify-hub-change.sh` -> `verify-additive-diff.sh` | Metalava `apiCheck` (manual/battery) | Per-commit staged-vs-HEAD diff is cheap; Metalava is too heavy per commit |
| Immutable tag + push | git (this repo) -> GitHub `origin` | JitPack (lazy remote build) | Tags ARE the Maven coordinates; JitPack builds from GitHub, not the local clone |
| Release-build window / host-memory check | Control-plane `xrepo build` (orchestrator-run) | - | Lock primitive only; cannot be invoked from this repo |
| Section-11 ledger row | Control-plane orchestrator (`xrepo ledger-row`, sole writer, A14) | This repo supplies row args + evidence file | This repo never writes the contract file |
| Docs of the compatibility rule | `API.md`, `tools/README-api-guard.md` | - | Consumer-facing rule + tool README |

## Standard Stack

No external packages are installed or added in this phase. The "stack" is the existing toolchain.

### Core
| Tool | Version | Purpose | Why Standard |
|------|---------|---------|--------------|
| JDK 17 (`javap`, `jar`, `javac`) | 17.0.19 [VERIFIED: `javap -version`, `java -version` in this session] | Descriptor extraction for the ABI gate; fixtures for tests | Already required by the project toolchain (jitpack.yml `jdk: openjdk17`) |
| `unzip`, `curl`, `git`, GNU `awk`/`sort`/`comm` | present [VERIFIED: `which unzip`; curl and git used this session] | AAR unpacking, baseline download, tag/push | Existing tool-test conventions already use `comm`/`sort -u` |
| Gradle 9.4.1 wrapper, AGP 9.2.1, Metalava plugin 0.5.0 | per repo [VERIFIED: gradle-wrapper.properties `gradle-9.4.1-bin.zip`; README-api-guard.md] | `assembleRelease`, `apiCheck`, `metalavaCheckCompatibilityDebug`, `testDebugUnitTest`, `detekt` | Existing governance battery |
| bash shell tests in `tools/test/test-*.sh`, auto-run by `tools/test/run-all.sh` | - | Test convention for tooling | `run-all.sh` globs `test-*.sh`, so a new `test-verify-binary-abi.sh` is wired by existing it; a deleted test is unwired by deleting it [VERIFIED: tools/test/run-all.sh `for t in "$DIR"/test-*.sh`] |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| javap descriptor diff | `abidiff`/japicmp/kotlinx BCV | BCV is dead on AGP 9 per README-api-guard.md; extra tools add packages (legitimacy gate). javap is already proven here. D-03 locks javap anyway |
| Baseline from Gradle cache/JitPack URL | Git worktree build of tag `v2.4.1` | Worktree build is slow and rebuilds with today's toolchain (not the published bytes). Keep only as a manual fallback (`BASELINE_AAR=<path>`) |

**Installation:** none. **Version verification:** N/A (no packages). 

## Package Legitimacy Audit

No external packages are installed in this phase; `gsd-tools query package-legitimacy check` was not applicable. All tools used (JDK, git, curl, unzip, Gradle wrapper pinned by the repo) are existing, pre-installed or already pinned.

**Packages removed due to [SLOP] verdict:** none
**Packages flagged as suspicious [SUS]:** none

## Architecture Patterns

### System Architecture Diagram

```
PLAN 19-01 (F3, wave 1)                                   PLAN 19-02 (cut, wave 2, one-way)
------------------------                                  ---------------------------------
tools/verify-binary-abi.sh v2.4.1                         record GATED_HEAD=$(git rev-parse HEAD)
   |  ./gradlew assembleRelease -> build/outputs/aar/       |  (tree clean for artifact inputs; create_tag==false)
   |     yahirandroidtaste-release.aar  (HEAD AAR)          v
   |  baseline AAR: BASELINE_AAR env                      swap-baseline Metalava vs v2.4.1 (trap-restore api.txt)
   |     -> ~/.gradle cache (v2.4.1, sha256 == JitPack)     |
   |     -> curl JitPack .aar  -> else exit 2               v
   v                                                      battery: testDebugUnitTest detekt apiCheck
unzip classes.jar  (both)                                   metalavaCheckCompatibilityDebug publishReleasePublicationToMavenLocal
   v                                                        + tools/test/run-all.sh   (HEAD still == GATED_HEAD)
javap -public -s  (excl *_Factory/*_MembersInjector)        |
   v                                                        v
awk -> sorted "class#member descriptor"                   tools/verify-binary-abi.sh v2.4.1   (exit 0 required)
   v                                                        |   any exit != 0 -> STOP (lane 3: never waive)
floor check (>=2000 lines both) --fail--> exit 2            v
   v                                                      git tag -a v2.5.0 $GATED_HEAD -m ...  (annotated, by SHA)
comm -23 base head; drop ComposableSingletons$              |
   v                                                        v
missing==0 -> exit 0 | else exit 3 (lane 3)               git push origin v2.5.0  (tag only)
                                                            |  ls-remote peeled SHA == GATED_HEAD
classifier/hook: drop api branch                            v
   hook -> classify-hub-change.sh                         poll JitPack: .pom 200, .aar 200,
        -> verify-additive-diff.sh (src/main only)          /api/builds: status ok, isTag true, commit == GATED_HEAD
   lane1 allow | lane2 block unless HUB_LANE_OVERRIDE=2     |
                                                            v
docs: README-api-guard.md, API.md rule section            post-tag commit: 19-SHIP-GATE-EVIDENCE.md + 19-SHIP-LEDGER-ROW.md
                                                          relay row to CURRENT orchestrator (effort.json "orchestrator")
```

### Recommended Project Structure
```
tools/
├── verify-binary-abi.sh            # NEW (mode 100755)
├── classify-hub-change.sh          # api branch removed
├── hooks/pre-commit                # API_FILE export + its comment removed
├── verify-additive-diff.sh         # unchanged (source line guard, src/main)
├── verify-additive-surface.sh      # unchanged
├── verify-api-additive.sh          # git rm (retired)
├── README-api-guard.md             # updated
└── test/
    ├── run-all.sh                  # unchanged (globs)
    ├── test-verify-binary-abi.sh   # NEW
    ├── test-classify-hub-change.sh # rewritten api cases
    ├── test-precommit-hook.sh      # rewritten api cases
    └── test-verify-api-additive.sh # git rm (retired)
```

### Pattern 1: The proven javap AAR-diff (normalization verbatim from 261005-e2e)
**What:** `javap -public -s` over every class in `classes.jar`, awk-normalized to `class#member descriptor`, `sort -u`, `comm -23 base head`.
**Evidence:** The 261005-e2e SUMMARY's gate command printed `base=2526 head=2584 filtered_ComposableSingletons=12 missing=0` at `0956d79` [VERIFIED: 261005-e2e-SUMMARY.md "Binary ABI gate" section, quoted]. Re-run this session at HEAD `9ccc564af209e52e0b909d9c332c9cd5e4c2e3bd` with a prototype script: **`base=2526 head=2584 filtered_ComposableSingletons=12 missing=0`**, rc 0, ~1 s wall [VERIFIED: ran `verify-binary-abi.proto.sh v2.4.1`].
**Prototype (proven in this session; planner should adopt as the starting point, with the hardening notes below):**
```bash
#!/usr/bin/env bash
# verify-binary-abi.sh <baseline-tag>  exit 0 pass | 3 missing public descriptors (lane 3) | 1 usage/resolution | 2 tool/sanity failure
set -euo pipefail
ROOT="$(git rev-parse --show-toplevel)"; cd "$ROOT"
[ "$#" -ge 1 ] || { echo "Usage: $0 <baseline-tag>" >&2; exit 1; }
TAG="$1"; MIN_LINES="${MIN_LINES:-2000}"
HEAD_AAR="${HEAD_AAR:-$ROOT/build/outputs/aar/yahirandroidtaste-release.aar}"
git rev-parse --verify --quiet "$TAG^{commit}" >/dev/null || { echo "ABI FAIL: baseline tag '$TAG' does not resolve" >&2; exit 1; }
for t in javap unzip; do command -v "$t" >/dev/null || { echo "ABI FAIL: $t not found (need JDK 17)" >&2; exit 2; }; done
W="$(mktemp -d)"; trap 'rm -rf "$W"' EXIT
if [ -z "${SKIP_BUILD:-}" ]; then ./gradlew assembleRelease -q >&2; fi
[ -f "$HEAD_AAR" ] || { echo "ABI FAIL: HEAD AAR not found at $HEAD_AAR" >&2; exit 2; }
BASE_AAR="${BASELINE_AAR:-}"
if [ -z "$BASE_AAR" ]; then
  BASE_AAR="$(ls "${GRADLE_USER_HOME:-$HOME/.gradle}"/caches/modules-2/files-2.1/com.github.Ygaray/yahirandroidtaste/"$TAG"/*/yahirandroidtaste-"$TAG".aar 2>/dev/null | head -1 || true)"
fi
if [ -z "$BASE_AAR" ]; then
  BASE_AAR="$W/base.aar"
  curl -fsSL --max-time 120 -o "$BASE_AAR" "https://jitpack.io/com/github/Ygaray/yahirandroidtaste/$TAG/yahirandroidtaste-$TAG.aar" \
    || { echo "ABI FAIL: cannot obtain baseline AAR for $TAG (no cache, JitPack unreachable); set BASELINE_AAR=<path>" >&2; exit 2; }
fi
normalize() {  # $1 aar  $2 out
  local d="$W/$(basename "$2" .txt)"; mkdir -p "$d"
  unzip -q -o "$1" classes.jar -d "$d"
  mapfile -t CLS < <(unzip -Z1 "$d/classes.jar" | grep '\.class$' | grep -v '_Factory\|_MembersInjector' | sed 's/\.class$//; s#/#.#g')
  javap -public -s -cp "$d/classes.jar" "${CLS[@]}" 2>/dev/null | awk '
    / (class|interface) [^ ]+.*\{$/ { match($0, /(class|interface) [^ <{]+/); s=substr($0, RSTART, RLENGTH); sub(/^(class|interface) /, "", s); cls=s }
    /descriptor:/ { n=prev; sub(/\(.*/, "", n); k=split(n, a, " "); print cls "#" a[k] $2 }
    { prev=$0 }' | sort -u > "$2"
}
normalize "$BASE_AAR" "$W/base.txt"; normalize "$HEAD_AAR" "$W/head.txt"
nb=$(wc -l < "$W/base.txt"); nh=$(wc -l < "$W/head.txt")
[ "$nb" -ge "$MIN_LINES" ] && [ "$nh" -ge "$MIN_LINES" ] || { echo "ABI FAIL: sanity floor $MIN_LINES not met (base=$nb head=$nh)" >&2; exit 2; }
comm -23 "$W/base.txt" "$W/head.txt" > "$W/all-missing.txt"
grep -F 'ComposableSingletons$' "$W/all-missing.txt" > "$W/filtered.txt" || true
grep -vF 'ComposableSingletons$' "$W/all-missing.txt" > "$W/missing.txt" || true
echo "base=$nb head=$nh filtered_ComposableSingletons=$(wc -l < "$W/filtered.txt") missing=$(wc -l < "$W/missing.txt")"
if [ -s "$W/missing.txt" ]; then
  while IFS= read -r l; do echo "ABI-ADDITIVE FAIL (lane 3): public descriptor missing vs $TAG: $l" >&2; done < "$W/missing.txt"; exit 3
fi
echo "ABI-ADDITIVE PASS: 0 missing public descriptors vs $TAG"
```
Controls run this session: unresolvable tag -> rc 1; no cache + unreachable network (proxy to dead port) -> rc 2 with a clear message; `MIN_LINES=99999` -> rc 2; **negative control** (baseline = current HEAD AAR, head = v2.4.1 AAR) -> rc 3 listing missing descriptors [VERIFIED: ran all four]. 

**Hardening the planner should add to the prototype (not yet run):**
- Print `HEAD=$(git rev-parse HEAD)`, the HEAD AAR path + sha256, the baseline source (override/cache/JitPack) + sha256, so the evidence file names the verified commit and the exact bytes. The Gradle-cached v2.4.1 AAR and the live JitPack download are byte-identical: both have sha256 `df21a126c07065a37cc63d4a9a5bb1cd3cac430a4026e3da37032ed75480689e` [VERIFIED: `sha256sum` of both files in this session].
- Refuse (exit 1) if the tree is dirty for artifact inputs, so the AAR provably reflects the commit: `git status --porcelain --untracked-files=no -- src build.gradle.kts gradle config api.txt jitpack.yml settings.gradle.kts gradle.properties` must be empty. (The working tree today has dirty `.planning/*` and untracked `.gsd/ .oc-audit/ graphify-out/ docs/superpowers/`; none are artifact inputs, so this check passes today.)
- Send javap stderr to a file and fail loudly on non-empty output/non-zero exit instead of `2>/dev/null` (the floor only catches *empty* output, not a partially-failed run).
- Only pass class names matching `^[A-Za-z0-9_$.]+$` to javap (entry names come from a downloaded archive; a name beginning with `-` would be parsed as an option).
- Keep `BASELINE_AAR`/`HEAD_AAR`/`SKIP_BUILD`/`MIN_LINES` as env test seams, but the cut task must run the script with **none** of them set.

### Pattern 2: Lane pipeline after the raw-line check is removed
`pre-commit` -> `classify-hub-change.sh` -> `verify-additive-diff.sh` only. Lane 1 (src/main append-only) allowed; lane 2 (an existing src/main line rewritten) blocked unless `HUB_LANE_OVERRIDE=2`; lane 3 is no longer produced by the hook path (it is owned by `apiCheck` + `verify-binary-abi.sh`). Prototype run this session: classifier api branch removed (~10 lines), hook `API_FILE` export + comment removed, rewritten tests, `bash tools/test/run-all.sh`: classify PASS=5, precommit PASS=6, additive-diff PASS=5, all `ok` [VERIFIED: ran in scratch copy of `tools/`].

### Anti-Patterns to Avoid
- **Deleting only the hook's `export API_FILE`**: `env -u API_FILE bash tools/classify-hub-change.sh --baseline v2.4.1` prints `classify: cannot classify - verify-api-additive.sh returned unexpected exit 1 (not 0/3)` and rc=1, so the hook would block every commit [VERIFIED: ran].
- **"Fixing" the absolute-path bug instead of deleting**: with a relative `API_FILE=api.txt` the script exits 3 on exactly the 10 known false-positive lines, and the classifier returns `LANE 3` [VERIFIED: ran]. The hook would block all commits.
- **Citing earlier evidence as the cut proof**: Phase 18's `javap` run and 751-test run are not valid for the Phase 19 HEAD (Phase 14 Pitfall 2 precedent); re-run fresh on `GATED_HEAD`.
- **`git add -A` / `git stash` / `git clean` during the cut**: the tree has unrelated dirty `.planning` files and untracked dirs that belong to the milestone run/other tools.
- **Using `xrepo` from this repo**: `xrepo host` here returns `xrepo: error: control-plane-only - run from inside /home/yahir/Projects/yahir-agentic-tools/yahir-gsd-control-plane` [VERIFIED: ran].

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Descriptor extraction | Custom class-file parser | `javap -public -s` + the proven awk | Proven at base=2526; reproduces compiler synthetics (`$default`, `DefaultConstructorMarker`) that Metalava cannot see |
| Baseline AAR | Rebuilding the old tag in a worktree | Gradle cache or JitPack URL (byte-identical, verified) | Published bytes, not today's toolchain's rebuild |
| Ledger row | Editing `CROSS-REPO-SCOPE-CONTRACT.md` | Relay row to orchestrator (`xrepo ledger-row`) | A14: orchestrator is sole writer |
| JitPack confirmation | `publishReleasePublicationToMavenLocal` as proof | 3-signal poll of JitPack (pom 200, aar 200, builds API) | Local publish says nothing about JitPack's remote build (Phase 14 plan anti-pattern) |
| Release window lock | A homegrown lock file | Orchestrator's `xrepo build start/done` | Control-plane state (`builds.jsonl`); not definable here |

**Key insight:** every piece of this phase already has a proven precedent in-repo (javap in 261005-e2e; the cut in Phase 14 and the v2.4.1 cut; the shell-test harness in `tools/test`). The work is assembling them in the right order with the right gates, not inventing mechanisms.

## F3 Tooling Work (exactly what changes, retires, stays)

| File | Action | Detail |
|------|--------|--------|
| `tools/verify-binary-abi.sh` | **Add** (100755) | Pattern 1 + hardening. Usage `tools/verify-binary-abi.sh v2.4.1` |
| `tools/test/test-verify-binary-abi.sh` | **Add** | See Validation Architecture. Fixture AARs from `javac` + `jar cf x.aar classes.jar` (a `.aar` is just a zip; `javac`/`jar` exist [VERIFIED: `which javac`, javap fixture run]) |
| `tools/hooks/pre-commit` | **Edit** | Remove the comment block + line 8 `export API_FILE="${API_FILE:-$ROOT/api.txt}"` [VERIFIED: tools/hooks/pre-commit:8]. Keep BASE detection (line 9 `BASE="$(git describe --tags --abbrev=0 --match 'v*' 2>/dev/null || true)"`) and the `case "$lane"` block (the `2\|3)` arm may stay as a defensive no-op or narrow to `2)`) |
| `tools/classify-hub-change.sh` | **Edit** | Remove line 20 `bash "$DIR/verify-api-additive.sh" "$BASE" >/dev/null 2>&1; api_rc=$?`, the `api_rc` sanity block (lines 23-26) and the `api_rc -eq 3` lane-3 arm (line 32), leaving `if [ "$src_rc" -ne 0 ]; then lane=2; else lane=1; fi`. Keep `--mode`/`--json` and the lane->exit mapping [VERIFIED: tools/classify-hub-change.sh:20,23,24,32] |
| `tools/verify-api-additive.sh` | **git rm** | Retired (orchestrator note in EVENTS: "retired by F3"). Recommended; see Open Question 1 for the keep-it alternative |
| `tools/test/test-verify-api-additive.sh` | **git rm** | Tests the retired script; includes the "KNOWN RESIDUAL RISK" case (e) that asserts the bug |
| `tools/test/test-classify-hub-change.sh` | **Rewrite api cases** | Drop the `verify-api-additive.sh` copy (line 8) and `export API_FILE` (line 12). Replace "api removal => lane 3" with "api-line removal is NOT a signal (exit 0)"; add "source rewrite => lane 2 exit 2" and "lane 2 permitted under curation"; replace "missing API_FILE => fail closed" with a fixture-copy case: copy the classifier into the fixture's `tools/`, delete the fixture's `verify-additive-diff.sh`, run `bash tools/classify-hub-change.sh` -> exit 1 (the test invokes `$SCRIPT` from the real tools dir, so deleting the fixture copy alone has no effect [VERIFIED: first attempt printed `got 0 want 1`]) |
| `tools/test/test-precommit-hook.sh` | **Rewrite api cases** | Drop `verify-api-additive.sh` copy (line 7) and `export API_FILE` (line 12). Remove "lane-3 commit blocked" / "declared lane-3 allowed"; add "api-line removal alone is not blocked"; keep lane-2 block/override and the GOV-03 regression; replace "missing API_FILE => fail closed" (line 47) with `rm tools/verify-additive-diff.sh` -> sub-guard rc 127 -> classifier exit 1 -> hook exit 1 (`classify: cannot classify - verify-additive-diff.sh returned unexpected exit 127 (not 0/1)` [VERIFIED]). Fix the `git reset --hard HEAD~N` counts after removing cases |
| `tools/test/run-all.sh`, `verify-additive-diff.sh`, `verify-additive-surface.sh`, `hooks/install.sh` | Unchanged | - |
| `tools/README-api-guard.md` | **Update** | Lane 3 text + `HUB_LANE_OVERRIDE=3` example (lines ~10-13) no longer apply; "API dump discipline" paragraph says the pre-commit guard sees api.txt (stale); line 26 lists `verify-api-additive`; "Known limitation: mangled (value-class-bearing) members" mitigation is now closed by the javap gate; add a "Binary ABI gate" section: usage, exit codes, baseline resolution order, exclusions, sanity floor, when to run (every release cut; `$BASE` = previous tag) |
| `API.md` "### The binary-compatibility rule" (line 343) | **Update** | Line 350 currently reads "It becomes `tools/verify-binary-abi.sh` at the v2.5.0 cut." Replace with the real command `tools/verify-binary-abi.sh <previous-tag>`, exit codes, and "run on the exact tagged HEAD before tagging". Leave the prior-claim sentences at lines 288-290 / 318-322 truthful ("re-proven at the v2.5.0 cut"): do not assert fresh verification in a pre-gate commit |

Run-time facts: the hook's raw-line check was inert in practice (see Summary finding 2) so there is no behavior regression to guard; Metalava `apiCheck` was already the real per-commit source gate by D-03.

**Not in scope / do not touch:** root `CLAUDE.md` (its "current release `v2.3.0`" line at CLAUDE.md:11-14 is stale vs the actual `v2.4.1`, but editing CLAUDE.md needs Yahir's direct OK - the F4 quick left it pending [VERIFIED: 261005-eyu-SUMMARY.md:11]). `build.gradle.kts` `version = "1.10.0"` (build.gradle.kts:147) is deliberately never edited - JitPack derives the version from the git ref (Phase 14 plan, CONTEXT discretion). No version string in README/INTEGRATION needs a bump [VERIFIED: `git grep` of version patterns]. ECOSYSTEM.md has no section 11 (its sections run 1-8); the section-11 ledger lives in `~/Projects/Reusable/android/voice-action-engine/CROSS-REPO-SCOPE-CONTRACT.md` and the machine-reconciled pin matrix is regenerated by `repin_status.py`, not by this phase.

## Section-11 Cut Protocol (Plan B)

### Steps and gates (A12 -> section 11 -> A14)
1. **Preconditions (read-only):** Phase 18 green (VERIFICATION + SELF-UAT exist); Plan A committed; `git tag -l 'v2.5*'` empty (currently 0 [VERIFIED]); `grep '"create_tag"' .planning/config.json` shows `false` (.planning/config.json:15 `"create_tag": false` [VERIFIED]); `curl .../api/builds/com.github.Ygaray/yahirandroidtaste/v2.5.0` returns `status: none` (it did [VERIFIED]); artifact-input paths clean; `git push --dry-run origin HEAD:refs/tags/<probe>` succeeds (auth OK - it did, and the probe tag does not exist remotely [VERIFIED: `ls-remote` count 0]); host memory (`MemAvailable` was 12.2 GB; xrepo's own thresholds are HOST-LOW-MEM <8 GiB, HOST-CRITICAL <5 GiB [CITED: control-plane xrepo SKILL.md `host` section]); no other repo's Gradle build running.
2. **Record `GATED_HEAD=$(git rev-parse HEAD)`**; no commit may be made until the tag exists.
3. **API-additive vs v2.4.1 (section-11 step 2):** swap-baseline Metalava in ONE shell with a trap restore (Phase 18 pattern): `trap 'git checkout -- api.txt' EXIT INT TERM; git show v2.4.1:api.txt > api.txt; ./gradlew metalavaCheckCompatibilityRelease --rerun`; then restore and assert `git status --short api.txt` empty. Note `v2.4.0:api.txt` and `v2.4.1:api.txt` are the same blob `f5832527695eb837c794cf4bf2cb59bcd3ba3e84` [VERIFIED: `git rev-parse v2.4.1:api.txt`; Phase 18 evidence], so one swap suffices. `apiDump` idempotence: `./gradlew apiDump && git diff --exit-code api.txt`.
4. **Fresh battery (section-11 step 1):** `./gradlew cleanTestDebugUnitTest testDebugUnitTest detekt apiCheck metalavaCheckCompatibilityDebug publishReleasePublicationToMavenLocal --no-build-cache` (Phase 18 ran the first four in 1m54s and measured 751 tests / 22 skipped / 0 failures [CITED: 18-SHIP-GATE-EVIDENCE.md:18-34]; force `detekt --rerun` and `--rerun` on Metalava tasks, which are otherwise `UP-TO-DATE`) + `bash tools/test/run-all.sh` (1.5 s). Confirm JUnit XML files are newer than the run start (non-vacuous), and `config/detekt-baseline.xml` is still `<CurrentIssues/>` and unchanged.
5. **ABI gate (D-03):** `tools/verify-binary-abi.sh v2.4.1` -> must exit 0. Any non-zero (3 = missing descriptor, 2 = tool/sanity/network) = STOP, no tag, escalate; never waive.
6. **Re-assert `git rev-parse HEAD == GATED_HEAD`** and `create_tag == false`.
7. **Tag by SHA, annotated:** `git tag -a v2.5.0 "$GATED_HEAD" -m "v2.5.0 - ..."` (precedent: `v2.4.1`, `v2.4.0` and `v2.2.0` are annotated objects; `git rev-parse v2.4.1` = `1319a768...` (tag object) vs `v2.4.1^{commit}` = `ada4a01c...` [VERIFIED]). Then `git cat-file -t v2.5.0` must print `tag`.
8. **Push the tag only:** `git push origin v2.5.0` (non-interactive auth works: `gh auth status` logged in; dry-run push succeeded [VERIFIED]). Do not push `main` (precedent: main is 128 commits ahead of `origin/main` and pushes are held pending Yahir; `git ls-remote` shows `v2.4.1^{}` = `ada4a01...` on origin while main lags [VERIFIED]). Verify `git ls-remote --tags origin v2.5.0^{}` SHA == `GATED_HEAD`. Note the tag publishes the ~129 commits since v2.4.1 (`git rev-list --count v2.4.1..HEAD` = 129 [VERIFIED]); a secret-pattern scan of `git diff v2.4.1 HEAD` found nothing [VERIFIED: grep for `sk-`, `AKIA`, `ghp_`, key assignments, PEM headers returned no hits].
9. **JitPack confirmation (section-11 step 4):** poll every 30 s up to 10 min (first request triggers a lazy build; do not fail on one non-200): `.../v2.5.0/yahirandroidtaste-v2.5.0.pom` = 200, `.aar` = 200, and `https://jitpack.io/api/builds/com.github.Ygaray/yahirandroidtaste/v2.5.0` has `"status" : "ok"`, `"isTag" : true`, `"commit" : "<GATED_HEAD>"`. (v2.4.1's record looks exactly like that: `status ok`, `commit ada4a01...`, `isTag true` [VERIFIED: live API call]. JSON is pretty-printed with spaces around `:`, so grep with `"status" *: *"ok"`; the Phase 14 verify script's `'"status":"ok"'` pattern would not match this formatting.) Sources/AAR URL pattern verified live for v2.4.1: `https://jitpack.io/com/github/Ygaray/yahirandroidtaste/v2.4.1/yahirandroidtaste-v2.4.1.aar` -> HTTP 200, 1800892 bytes [VERIFIED].
10. **Recommended extra (non-gating, cheap):** run `HEAD_AAR=<downloaded JitPack v2.5.0 aar> SKIP_BUILD=1 tools/verify-binary-abi.sh v2.4.1` to prove the *published* artifact, not just the local build, keeps every v2.4.1 descriptor. A failure here cannot un-cut; it means a corrective `v2.5.1`.
11. **Post-tag docs commit (allowed, HEAD may now move):** `19-SHIP-GATE-EVIDENCE.md` (verbatim transcripts naming `GATED_HEAD`) and `19-SHIP-LEDGER-ROW.md` headed "FOR ORCHESTRATOR RELAY" (Phase 14 precedent). Stage only these named files.
12. **Relay:** ledger row args for the orchestrator's `xrepo ledger-row --repo yahirandroidtaste --tag v2.5.0 --commit <GATED_HEAD> --coord com.github.Ygaray:yahirandroidtaste:v2.5.0 --contents "<...>" --evidence .planning/phases/19-cut-v2-5-0/19-SHIP-GATE-EVIDENCE.md` (that command hard-fails unless the tag is on origin, `ls-remote` agrees with `--commit` (annotated tags peeled), the `.pom` returns HTTP 200, and the row does not already exist [CITED: xrepo SKILL.md `ledger-row` section]). Contents summary: voice-surface localization (VI18N-01..04), a11y (VA11Y-01), Failure enrichment (VFAIL-01..03), router toggle (VAPPR-04), additive; v2.4.1 binary-compat shims; consumers should repin SB (required) / CT (optional).
13. **Close-out checks:** `git tag -l 'v2.5*'` outputs exactly `v2.5.0`; `create_tag` still `false`; `git status` shows no new tracked changes other than the evidence files.

### xrepo build: what it is and what the plan should do
`xrepo build start <repo> "<purpose>"` / `build done <repo>` is "release-build lock: one cut builds at a time (builds.jsonl)" - it refuses while another repo holds the lock, prints the host memory line, and appends to `builds.jsonl`; the SKILL says peers message build-window start/done to the orchestrator (like device windows) [CITED: control-plane `.claude/skills/xrepo/SKILL.md` "host and build start|done"; `xrepo --help` output]. It does no tagging and no pushing. It cannot be run from this repo (control-plane-only error [VERIFIED]). `builds.jsonl` does not exist for the vae-bilingual effort yet [VERIFIED: `ls` of the effort dir]; `ledger-row` merely warns when no build window was recorded.
**Recommendation (autonomy):** keep the cut task `autonomous: true` (D-01 grants no further human gate; the waiver is recorded in 19-CONTEXT.md D-01, `.planning/v2.5-DECISION-MAP.md` Phase 19 [tag-gate], and HANDOFF.md "What Yahir confirmed"). Treat the orchestrator build-window as a **non-blocking coordination step**: if a cross-session messaging tool (ListAgents/SendMessage) is available to the executor, message the CURRENT orchestrator to open/close the window; if not (Phase 14 precedent: no such tool in the executor context, accepted as a soft outcome and Phase 14's VERIFICATION applied the pre-approved ledger-relay fallback override), proceed using the in-repo manual annotated-tag+push path and let the durable `19-SHIP-LEDGER-ROW.md` carry the relay. Replace the placeholder "confirm with orchestrator" in the CONTEXT discretion with this explicit rule in the plan, and flag it in Open Questions so the checker/orchestrator can object.
**Relay target:** `effort.json` `"orchestrator"` is **`yahir-gsd-control-plane-3b`** [VERIFIED: python read of `~/Projects/yahir-agentic-tools/yahir-gsd-control-plane/xrepo/vae-bilingual/effort.json`], and D-03 itself cites `-3b`; D-02/ROADMAP/DECISION-MAP still say `-6e` (the pre-restart session; the ORCHESTRATOR-HANDOFF was "written by ...-6e" before Yahir restarted every session [VERIFIED]). Session names change on restart: the plan should say "the current orchestrator (resolve via ListAgents / effort.json; at research time `-3b`, formerly `-6e`)".

## Common Pitfalls

### Pitfall 1: HEAD moves between the battery and the tag
**What goes wrong:** a bookkeeping commit (state/run-log/docs) lands after the battery; the tag then points at an unverified commit, or the plan tags `HEAD` after it moved.
**Why:** the milestone driver and orchestrator commit `.planning` bookkeeping; `git.create_tag=false` does not stop commits.
**How to avoid:** record `GATED_HEAD`, assert equality before tagging, and tag the explicit SHA (`git tag -a v2.5.0 "$GATED_HEAD"`). No commits in between. All doc edits (including Plan A's) must land before the battery; evidence docs land after the tag.
**Warning signs:** `git rev-parse HEAD != GATED_HEAD`; the battery output names a different SHA than the tag.

### Pitfall 2: Resting a proof on `UP-TO-DATE`
**What goes wrong:** Gradle skips tests/Metalava because inputs are unchanged; the "fresh" evidence is a skip (Phase 18 hit this with `v2.4.0`/`v2.4.1` identical api.txt blobs and `detekt UP-TO-DATE`).
**How to avoid:** `cleanTestDebugUnitTest ... --no-build-cache`, `--rerun` on detekt and Metalava tasks, JUnit XML mtimes newer than run start.

### Pitfall 3: Deleting only part of the raw-line check
See Anti-Patterns: removing only the hook export blocks all commits; keeping the classifier branch but fixing the path blocks all commits on the 10 false-positive lines.

### Pitfall 4: Swap-baseline leaves `api.txt` swapped
**What goes wrong:** a failure mid-swap leaves a tracked file modified; battery then runs against the wrong api.txt or the file gets committed.
**How to avoid:** one shell, `trap ... EXIT INT TERM`, explicit `git checkout -- api.txt`, then assert `git status --short api.txt` empty and `sha256sum api.txt` unchanged (Phase 18 pattern).

### Pitfall 5: JitPack JSON formatting / lazy build
**What goes wrong:** greps written for compact JSON miss `"status" : "ok"`; a single immediate non-200 is read as failure.
**How to avoid:** whitespace-tolerant regex; 30 s x 20 poll; fail only on explicit `"status" : "error"` or timeout.

### Pitfall 6: Staging the wrong files
**What goes wrong:** `git add -A` sweeps `.planning/graphs/*`, `.planning/state.json`, `config.json` (`_milestone_run_active: true`, a run flag) and untracked `.gsd/`, `.oc-audit/`, `graphify-out/`, `docs/superpowers/` into a commit.
**How to avoid:** `git add <named files>` only; never `stash`/`clean`; those files do not affect the AAR or the tag (the tag is a commit, not the working tree). Today's dirty set is all under `.planning/` or untracked dirs [VERIFIED: `git status --short`].

### Pitfall 7: Mutable or marker tags
**What goes wrong:** a second tag (e.g. a `/gsd-complete-milestone` marker `v2.5`) appears; JitPack coordinates are tags. A `v2.2` bare tag already exists as a historical incident [VERIFIED: `git tag` output includes `v2.2`].
**How to avoid:** `git.create_tag` false before and after; assert `git tag -l 'v2.5*'` is exactly `v2.5.0`; never delete/move a published tag (corrective patch = `v2.5.1` + superseding ledger row).

### Pitfall 8: Misreading the ledger recipient / mechanism
Relaying to the retired `-6e`, or waiting for an `xrepo build` that cannot be run from this repo. See cut section above.

## Code Examples

### Swap-baseline Metalava (single shell, trap-restored; Phase 18 pattern)
```bash
set -euo pipefail
sha_before=$(sha256sum api.txt)
restore(){ git checkout -- api.txt; }
trap restore EXIT INT TERM
git show v2.4.1:api.txt > api.txt
./gradlew metalavaCheckCompatibilityRelease --rerun
restore; trap - EXIT INT TERM
test -z "$(git status --short api.txt)" && [ "$sha_before" = "$(sha256sum api.txt)" ]
```
### Tag + push + verify (Phase 14 pattern, hardened to tag by SHA)
```bash
test "$(git rev-parse HEAD)" = "$GATED_HEAD"
grep -q '"create_tag": false' .planning/config.json
git tag -a v2.5.0 "$GATED_HEAD" -m "v2.5.0 - voice UI localization, accessibility, Failure enrichment, router toggle (additive; v2.4.1 binary-compatible)"
test "$(git cat-file -t v2.5.0)" = tag
git push origin v2.5.0
test "$(git ls-remote --tags origin 'v2.5.0^{}' | cut -f1)" = "$GATED_HEAD"
```
### JitPack poll
```bash
for i in $(seq 1 20); do
  POM=$(curl -s -o /dev/null -w '%{http_code}' "https://jitpack.io/com/github/Ygaray/yahirandroidtaste/v2.5.0/yahirandroidtaste-v2.5.0.pom")
  AAR=$(curl -s -o /dev/null -w '%{http_code}' "https://jitpack.io/com/github/Ygaray/yahirandroidtaste/v2.5.0/yahirandroidtaste-v2.5.0.aar")
  J=$(curl -s https://jitpack.io/api/builds/com.github.Ygaray/yahirandroidtaste/v2.5.0)
  if [ "$POM" = 200 ] && [ "$AAR" = 200 ] && printf '%s' "$J" | grep -Eq '"status" *: *"ok"' \
     && printf '%s' "$J" | grep -Eq '"isTag" *: *true' && printf '%s' "$J" | grep -Eq "\"commit\" *: *\"$GATED_HEAD\""; then echo RESOLVED; break; fi
  printf '%s' "$J" | grep -Eq '"status" *: *"error"' && { echo JITPACK-ERROR; break; }
  sleep 30
done
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Raw-line `api.txt` set-diff in the hook (`verify-api-additive.sh`) | Metalava `apiCheck` (source) + javap AAR diff (binary) | D-03, 2026-10-05 | Raw-line check false-positives on appended defaulted params (10 lines) and was silently inert via the absolute `API_FILE` |
| "additive" proven by Metalava alone | Metalava + javap descriptor gate | INC-2026-10-05-02 | Metalava is blind to Compose `$default`/`$changed` and data-class synthetics |

**Deprecated/outdated:** the `README-api-guard.md` "Known limitation: mangled (value-class-bearing) members are not tracked" mitigation text (now covered by the javap gate); ROADMAP/REQUIREMENTS SHIP-03 "human-gated (A12 waiver pending)" wording (superseded by D-01); CONTEXT/ROADMAP orchestrator name `-6e`; root CLAUDE.md "current release v2.3.0" (stale, left for Yahir's OK).

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | The cut executor may lack a cross-session messaging tool (as in Phase 14), so the orchestrator build-window coordination can only be best-effort and the durable ledger-row file is the relay | Cut protocol / xrepo build | If the orchestrator *requires* an acknowledged build window before any cut, the plan needs a blocking checkpoint. D-01 says no human gate; orchestrator coordination is not a human gate, but its strictness is not verified |
| A2 | Deleting (`git rm`) `verify-api-additive.sh` + its test is acceptable (vs leaving them unwired) | F3 tooling | Low; historical planning docs still mention the script by name. Orchestrator EVENTS says "retired by F3" but D-03 literally says only delete the check from the hook |
| A3 | Post-tag evidence/ledger commits (Phase 14 precedent) satisfy "gated on HEAD identity" because the tag is on `GATED_HEAD` | Cut protocol | If the orchestrator wants the tag to be the repo's final HEAD, evidence would need to be committed before the battery (impossible for post-tag evidence) |
| A4 | Pushing only the tag (not `main`) remains the held-push policy | Cut step 8 | If Yahir wants `main` pushed too, that is a separate human decision (HANDOFF: "pushes held pending Yahir") |
| A5 | JitPack will build `v2.5.0` the same way it built `v2.4.1` because build/gradle/jitpack files are unchanged vs v2.4.1 | Summary | JitPack environment drift (not repo content) could still fail; the 10-minute poll + `status:error` handling covers detection; remedy = corrective patch tag, not re-pointing |

## Open Questions

1. **Delete or keep `tools/verify-api-additive.sh` + its test?**
   - Known: D-03 requires removing its *invocation/`API_FILE` path* from the hook; it is only reachable through the classifier. Keeping an unwired script with a documented false-positive and a test asserting a known bug is dead weight.
   - Recommendation: `git rm` both and update README-api-guard.md; if the planner prefers a smaller blast radius, leave them but remove them from the README's guard list and `run-all` expectations. Either satisfies D-03.
2. **Orchestrator build-window acknowledgement.** Recommendation above (non-blocking, durable fallback). Surface it in the plan's cut task as an explicit, logged decision so the plan-checker/orchestrator can overrule.
3. **Evidence-commit timing vs HEAD identity.** Recommend post-tag evidence commit (precedent). State it in the plan so it is a deliberate choice, not an accident.
4. **Whether to re-run the ABI gate against the JitPack-built v2.5.0 AAR** (step 10). Recommended, non-gating.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| JDK 17 (`javap`, `jar`, `javac`) | ABI script, test fixtures | yes | 17.0.19 | - |
| `unzip`, `curl`, `zip` | AAR unpack, JitPack download/poll | yes | - | `jar xf` for unzip if needed |
| Gradle wrapper + Android SDK | `assembleRelease`, battery | yes (assemble was UP-TO-DATE in 1.2 s; `build/outputs/aar/yahirandroidtaste-release.aar` present) | Gradle 9.4.1 | - |
| Gradle-cached v2.4.1 AAR | baseline | yes, `~/.gradle/caches/modules-2/files-2.1/com.github.Ygaray/yahirandroidtaste/v2.4.1/e574584823b9cd55c67b50baabfc4875b9203fee/yahirandroidtaste-v2.4.1.aar` | sha256 identical to JitPack | JitPack URL (HTTP 200, 1800892 bytes), then `BASELINE_AAR` |
| Network to jitpack.io | baseline download, cut step 9 | yes (this session) | - | If blocked in the executor sandbox: baseline via cache works offline; the JitPack poll cannot be substituted (escalate rather than substitute `publishToMavenLocal`) |
| GitHub push auth (`gh` keyring) | tag push | yes (`gh auth status` logged in; `git push --dry-run` OK) | - | Halt and escalate; never embed credentials |
| `xrepo` | build window / ledger row | **No, from this repo** (control-plane-only) | - | Orchestrator runs it; this repo relays args |

**Missing with no fallback:** none for the tooling plan. For the cut: if JitPack is unreachable from the executor, step 9 cannot complete and must escalate.

## Validation Architecture

`workflow.nyquist_validation` is `true` in `.planning/config.json` [VERIFIED: config.json `"nyquist_validation": true`].

### Test Framework
| Property | Value |
|----------|-------|
| Framework | Gradle `testDebugUnitTest` (JUnit4 + Robolectric + Compose UI test, 751 tests / 22 skipped last measured) for the library; bash shell tests under `tools/test/` for tooling |
| Config file | `build.gradle.kts`; detekt `config/detekt/detekt.yml`, `config/detekt-compose.yml`, `config/detekt-baseline.xml` (empty); shell tests have no config |
| Quick run command | `bash tools/test/run-all.sh` (about 1.5 s) |
| Full suite command | `./gradlew cleanTestDebugUnitTest testDebugUnitTest detekt apiCheck metalavaCheckCompatibilityDebug publishReleasePublicationToMavenLocal --no-build-cache && bash tools/test/run-all.sh && tools/verify-binary-abi.sh v2.4.1` |

### Phase Requirements -> Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| D-03 | ABI script exits 0 when head is a superset of baseline | shell fixture | `bash tools/test/test-verify-binary-abi.sh` | Wave 0 (new) |
| D-03 | Exits 3 when a public descriptor is missing at head | shell fixture | same | Wave 0 |
| D-03 | Ignores added members, `ComposableSingletons$*` loss, `*_Factory` / `*_MembersInjector` classes | shell fixture | same | Wave 0 |
| D-03 | Exits 2 when the sanity floor is not met (empty/garbage javap run) and when baseline AAR unobtainable | shell fixture | same | Wave 0 |
| D-03 | Exits 1 on unresolvable baseline tag / no args | shell fixture | same | Wave 0 |
| D-03 | Real gate: v2.4.1 vs HEAD AAR, missing=0, base>=2526 | integration | `tools/verify-binary-abi.sh v2.4.1` | Wave 0 (script) |
| D-03 | Hook no longer references `API_FILE`/`verify-api-additive`; classifier has no api branch | grep + shell | `! grep -rn "API_FILE\|verify-api-additive" tools/hooks tools/classify-hub-change.sh && bash tools/test/test-precommit-hook.sh && bash tools/test/test-classify-hub-change.sh` | tests exist, need rewrite |
| D-03 | Lane 1 allowed, lane 2 blocked/override, GOV-03 regression, fail-closed on classifier error | shell | `bash tools/test/run-all.sh` | exists, rewrite |
| D-03 | Docs point at the script | grep | `grep -n "verify-binary-abi" API.md tools/README-api-guard.md` (and no remaining "becomes ... at the v2.5.0 cut") | edit |
| SHIP-03 | Battery green on `GATED_HEAD` | Gradle | the full suite command above | exists |
| SHIP-03 | API additive vs v2.4.1 (Metalava swap-baseline) | Gradle | swap-baseline snippet | exists (pattern) |
| SHIP-03 | Annotated tag at `GATED_HEAD`, on origin | git | `git cat-file -t v2.5.0` = `tag`; `git rev-parse v2.5.0^{commit}` = `GATED_HEAD`; `git ls-remote --tags origin 'v2.5.0^{}'` = `GATED_HEAD` | cut task |
| SHIP-03 | JitPack resolves (pom 200, aar 200, status ok, isTag true, commit match) | network | poll snippet | cut task |
| SHIP-03 | No stray marker tag; `create_tag` false | git | `test "$(git tag -l 'v2.5*')" = v2.5.0 && grep -q '"create_tag": false' .planning/config.json` | cut task |
| SHIP-03 | Ledger row artifact exists, relay-bannered, row never written to the contract | file | `test -f .planning/phases/19-cut-v2-5-0/19-SHIP-LEDGER-ROW.md` + `git diff --name-only` shows nothing under the voice-action-engine repo | cut task |

### Test cases for `tools/test/test-verify-binary-abi.sh` (follow the existing convention: temp git repo, copy the script into `tools/`, `check()` counter, `PASS=n FAIL=n`, exit non-zero on fail)
Build fixtures offline with `javac` + `jar cf x.aar classes.jar` (a minimal `A.java` produced `javap -public -s` output `public void a(); descriptor: ()V` etc. [VERIFIED in scratch]). Run with `SKIP_BUILD=1 BASELINE_AAR=... HEAD_AAR=... MIN_LINES=1`: (a) identical -> 0; (b) head adds a method -> 0; (c) head removes a method -> 3 and the message names it; (d) head drops a `ComposableSingletons$FooKt` class -> 0; (e) head drops a `Foo_Factory` class -> 0; (f) `MIN_LINES` above the fixture size -> 2; (g) tag does not resolve -> 1; (h) baseline AAR path missing and no network seam -> 2; (i) head changes a descriptor (same name, new param) -> 3.

### Sampling Rate
- **Per task commit:** `bash tools/test/run-all.sh` (plus the one new/edited test file); doc tasks run their grep checks.
- **Per wave merge:** `bash tools/test/run-all.sh` and `SKIP_BUILD=1 tools/verify-binary-abi.sh v2.4.1` (no Gradle needed if the AAR is current).
- **Phase gate:** the full suite command on `GATED_HEAD`, before the tag.

### Wave 0 Gaps
- [ ] `tools/test/test-verify-binary-abi.sh` - covers D-03 script behavior (new)
- [ ] `tools/verify-binary-abi.sh` - the unit under test (new; created in the same TDD task, RED test first)
- [ ] Rewrite api cases in `tools/test/test-classify-hub-change.sh` and `tools/test/test-precommit-hook.sh`
- [ ] Framework install: none (JDK/bash present)

## Security Domain

`security_enforcement` is `true` (absent = enabled) and `security_asvs_level` is 1 [VERIFIED: config.json]. This phase has no auth, session, user input, or crypto surface; the relevant risks are supply-chain and release integrity.

### Applicable ASVS Categories
| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2 Authentication | no (git/GitHub auth only) | `gh` keyring / credential helper, non-interactive (`GIT_TERMINAL_PROMPT=0`); never embed credentials |
| V3 Session Management | no | - |
| V4 Access Control | no | Push rights are the existing `Ygaray` account |
| V5 Input Validation | yes (script args, archive entry names) | Quote all expansions; validate tag via `git rev-parse --verify`; whitelist class-name characters before passing to `javap`; extract only the `classes.jar` member from the AAR |
| V6 Cryptography | partial | No hand-rolled crypto; record `sha256sum` of baseline/head AARs as provenance |
| V14 Config / build integrity | yes | Immutable annotated tag by SHA; never `-SNAPSHOT`; `mktemp -d` + `trap` cleanup |

### Known Threat Patterns
| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| Tag re-pointed / mutated after publish | Tampering | Never move/delete a tag; corrective patch tag only (section-11 step 6) |
| Tagging an unverified commit | Tampering / Repudiation | `GATED_HEAD` identity + tag by SHA + evidence transcript naming the SHA |
| Tampered baseline AAR (cache poisoning / MITM) | Tampering | HTTPS to JitPack; cached AAR sha256 matches the live JitPack download (verified identical); `BASELINE_AAR` override is a test seam, unset for the gate |
| Zip-slip / hostile archive entry names | Tampering | Extract one named member; whitelist class names |
| Secrets published via the tag (129 unpushed commits become public) | Information disclosure | Pre-push secret-pattern scan of `git diff v2.4.1 HEAD` (none found this session); note `.planning/` content is already public via earlier tags |
| Stray marker tag becomes a live coordinate | Tampering | `git.create_tag` false + exact-tag-set assertion |

## Project Constraints (from CLAUDE.md)

- Library is domain-agnostic; imports no host code, holds no secrets, no `@HiltAndroidApp` (not touched by this phase; no `src/` change).
- `ComponentRegistry` drift guard: no new public composables in this phase.
- **Shipping is human-gated in general**: tag/bump/deploy is surfaced for confirmation. D-01 waives this *for v2.5.0 only*; the cut task must record the waiver provenance and must not be read as a standing change. Consumer repins are not part of this phase.
- **Tags are immutable**; never `main-SNAPSHOT`; never a moving ref.
- **Cross-repo convention:** sequential in the hub, commit on `main`, no consumer worktrees, do not modify consumer files; consumer orchestrator owns its own tracking.
- Gradle commands drop the module prefix: `./gradlew testDebugUnitTest`, `./gradlew detekt`, `./gradlew assembleRelease`, `./gradlew publishReleasePublicationToMavenLocal`.
- Detekt zero-baseline: do not regenerate a baseline; the battery asserts `config/detekt-baseline.xml` stays empty.
- Root `CLAUDE.md` edits need Yahir's direct OK (F4 left it pending): do not edit it in this phase.
- Global CLAUDE.md: milestone-close git rules and "never merge a frozen keeper branch into main" are not triggered here; flag any state changes (new tag, push) explicitly in the summary; avoid emojis in output.

## Sources

### Primary (HIGH confidence, read or executed this session)
- `.planning/phases/19-cut-v2-5-0/19-CONTEXT.md`; `.planning/quick/261005-dmc*/261005-dmc-SUMMARY.md`, `261005-e2e*/261005-e2e-SUMMARY.md` (command, base=2526, missing=0, K recipe); `.planning/quick/261005-eyu*/261005-eyu-SUMMARY.md`
- `tools/hooks/pre-commit`, `tools/classify-hub-change.sh`, `tools/verify-api-additive.sh`, `tools/verify-additive-diff.sh`, `tools/test/*.sh`, `tools/README-api-guard.md`; `API.md` lines 262-371; `build.gradle.kts`; `jitpack.yml`; `.planning/config.json`
- `.planning/cross-repo/HANDOFF.md`, `RECONVENE-BRIEF-R-v1.1.md` section 7, `.planning/v2.5-DECISION-MAP.md` Phase 19, `.planning/ROADMAP.md`, `.planning/REQUIREMENTS.md`
- `.planning/milestones/v2.4-phases/14-cut-v2-4-0/` (14-01-PLAN, 14-01-SUMMARY, 14-SHIP-LEDGER-ROW): the cut precedent
- `.planning/phases/18-catalog-integrity-api-dump-docs/` (SHIP-GATE-EVIDENCE, SELF-UAT, VALIDATION)
- `~/Projects/yahir-agentic-tools/yahir-gsd-control-plane/.claude/skills/xrepo/SKILL.md`, `xrepo --help`, `xrepo/vae-bilingual/effort.json` (orchestrator `yahir-gsd-control-plane-3b`), `ORCHESTRATOR-HANDOFF.md`, `EVENTS.md`
- `~/Projects/Reusable/android/voice-action-engine/CROSS-REPO-SCOPE-CONTRACT.md` section 11, A12, A14
- Live commands: `javap -version`, `git ls-remote --tags origin`, `curl` to JitPack (`/api/builds/.../v2.4.1`, `/v2.5.0`, the v2.4.1 `.aar`), `git push --dry-run`, prototype ABI script and prototype tooling edits in the scratchpad

### Secondary (MEDIUM confidence)
- Phase 14 verification treating the no-messaging-tool case as a soft outcome (read via 14-01-SUMMARY)

### Tertiary (LOW confidence)
- None.

## Metadata

**Confidence breakdown:**
- Standard stack / ABI method: HIGH - reproduced base=2526, head=2584, missing=0, plus four negative/edge controls
- Hook/classifier/test changes: HIGH - prototype passed `run-all.sh` in a scratch copy; both failure modes of a partial deletion reproduced
- Cut protocol: HIGH for in-repo mechanics (git, JitPack, battery), MEDIUM for orchestrator coordination (messaging capability and build-window strictness unverified)
- Pitfalls: HIGH

**Research date:** 2026-10-05
**Valid until:** 2026-10-12 (fast-moving: orchestrator session names change on restart; HEAD will move after Plan A)
