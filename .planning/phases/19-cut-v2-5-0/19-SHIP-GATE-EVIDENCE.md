# Phase 19 Ship-Gate Evidence: v2.5.0

- **Captured:** 2026-10-05 (UTC)
- **GATED_HEAD:** `7101516e089964b3fd45696bdeb4082d4f8c5580`
- **Tag object SHA (annotated `v2.5.0`):** `07e89727381157fbd49e0bfeb01bedfca5f96fc0`
- **Baseline tag:** `v2.4.1`
- **Baseline AAR sha256:** `df21a126c07065a37cc63d4a9a5bb1cd3cac430a4026e3da37032ed75480689e` (Gradle cache, equal to an independent HTTPS download from JitPack)
- **HEAD AAR sha256 (local release build on GATED_HEAD):** `27b6fd7994735a5cd3182089e74d30771cad2c8f178c93177308af6d3862bda1`
- **Published AAR sha256 (JitPack-built v2.5.0):** `d83212f7a07af099ce9d08201ed995fdf742f41d60ca5f194ad33f120051a323`
- **D-01 provenance:** Yahir granted the A12 tag-cut waiver for this effort's v2.5.0 tag in-session on 2026-10-05; this tag only, not a standing policy change. The waiver removed the human gate, not any verification gate.
- **DEC-3 (after-the-tag record):** this file and `19-SHIP-LEDGER-ROW.md` are committed AFTER the tag. The tag points at GATED_HEAD, the tagged tree does not contain these files, and the evidence commit is a descendant of GATED_HEAD. Every proof below names GATED_HEAD.

Source logs live in the untracked scratch directory `.planning/phases/19-cut-v2-5-0/.gate/` (never staged). Excerpts below are verbatim from them.

## Preconditions

Log: `.gate/preconditions.log`.

```
== (a) branch
main
== (b) local v2.5 tags
(empty)
remote:
(empty)   ls-remote rc=0
== (c)
15:    "create_tag": false,
== (d)
no tracked change outside .planning
== (e)
MemAvailable:    6593256 kB
== (g) orchestrator
4:  "orchestrator": "yahir-gsd-control-plane-3b",
== origin HEAD
def49645b1d28f889674bcedae08a81c338730ec	HEAD   (ancestor of GATED_HEAD)
```

- Plan 19-01 complete (`19-01-SUMMARY.md` present, `tools/verify-binary-abi.sh` mode 100755); Phase 18 `18-VERIFICATION.md` reports `status: passed`.
- (f) Secret scan of the added lines of `git diff v2.4.1 HEAD -U0` (138 commits, 35299 added lines): `.gate/secret-scan.log` has 0 lines. Patterns: `sk-` + 20 alnum, `AKIA` + 16, `ghp_` + 30, PEM private-key headers, quoted key/secret/token/password assignments with 8+ char values.
- (h) Independent JitPack download of v2.4.1 AAR: sha256 `df21a126c07065a37cc63d4a9a5bb1cd3cac430a4026e3da37032ed75480689e`.

PRE PASS: all preconditions met on `main`; no v2.5* tag locally or on origin; create_tag false; no tracked change outside .planning/; 0 secret-pattern hits.

## Step 1 Evidence (fresh battery on GATED_HEAD)

Command (marker `.gate/battery.start` touched first, run once, log `.gate/battery.log`):

```
./gradlew cleanTestDebugUnitTest testDebugUnitTest detekt --rerun apiCheck metalavaCheckCompatibilityRelease --rerun metalavaCheckCompatibilityDebug --rerun publishReleasePublicationToMavenLocal --no-build-cache
```

```
> Task :cleanTestDebugUnitTest
> Task :testDebugUnitTest
> Task :detekt
> Task :metalavaCheckCompatibilityRelease
> Task :publishReleasePublicationToMavenLocal
> Task :apiCheck
> Task :metalavaCheckCompatibilityDebug
BUILD SUCCESSFUL in 6m 35s
69 actionable tasks: 8 executed, 61 up-to-date
rc=0
```

- JUnit freshness: 77 of 77 XML files newer than `.gate/battery.start`.
- Totals (`.gate/junit-totals.txt`): `tests=751 skipped=22 nonzero_fail_err=0`.
- `config/detekt-baseline.xml` is still `<ManuallySuppressedIssues/>` + `<CurrentIssues/>` and `git diff HEAD` on it is empty.
- `git status --short api.txt` empty after the battery.
- `bash tools/test/run-all.sh` (`.gate/run-all.log`): 4 test scripts, 4 `ok` lines, no `FAILED`: test-classify-hub-change (PASS=6), test-precommit-hook (PASS=6), test-verify-additive-diff (PASS=5), test-verify-binary-abi (PASS=26).

STEP1 PASS: BUILD SUCCESSFUL with forced reruns and no build cache; 751 fresh tests, 0 failures/errors; detekt baseline empty and unchanged; run-all.sh 4/4 ok.

## Step 2 Evidence (API additive vs v2.4.1)

(i) Idempotence: `./gradlew apiDump -q` then `cmp api.txt .gate/api.head.txt` printed `APIDUMP-IDENTICAL`; `git status --short api.txt` empty. api.txt sha256 `218bd9c2acb1c01c3a988dfb462449dc3fd43f3ba387c88cb3ae01160d23740e`.

(ii) Swap-baseline (one shell, trap on EXIT INT TERM running `git checkout -- api.txt`; `git show v2.4.1:api.txt > api.txt`; `./gradlew metalavaCheckCompatibilityRelease --rerun`), log `.gate/swap-baseline.log`:

```
> Task :metalavaCheckCompatibilityRelease
BUILD SUCCESSFUL in 1m 51s
7 actionable tasks: 1 executed, 1 from cache, 5 up-to-date
gradle rc=0
restored
```

The Metalava task executed (not UP-TO-DATE). Restore confirmed: `git status --short api.txt` empty and sha256 `218bd9c2acb1c01c3a988dfb462449dc3fd43f3ba387c88cb3ae01160d23740e` equal to `.gate/api.head.sha256`.

STEP2 PASS: apiDump reproduces the committed api.txt; the v2.4.1 swap-baseline Metalava compatibility check is BUILD SUCCESSFUL; api.txt restored byte-identical.

## D-03 Binary ABI Gate (pre-tag)

Command: `env -u BASELINE_AAR -u HEAD_AAR -u SKIP_BUILD -u MIN_LINES -u JITPACK_BASE tools/verify-binary-abi.sh v2.4.1` (log `.gate/abi.log`, exit status `.gate/abi.rc` = 0):

```
SEAMS: none
HEAD=7101516e089964b3fd45696bdeb4082d4f8c5580
HEAD_AAR sha256=27b6fd7994735a5cd3182089e74d30771cad2c8f178c93177308af6d3862bda1 path=/home/yahir/Projects/Reusable/android/yahirandroidtaste/build/outputs/aar/yahirandroidtaste-release.aar
BASELINE_AAR sha256=df21a126c07065a37cc63d4a9a5bb1cd3cac430a4026e3da37032ed75480689e source=gradle-cache
base=2526 head=2584 filtered_ComposableSingletons=12 missing=0
ABI-ADDITIVE PASS: 0 missing public descriptors vs v2.4.1
```

The BASELINE_AAR sha256 equals the sha256 of the independent JitPack v2.4.1 download (`.gate/jitpack-v2.4.1.sha256`).

ABI PASS: no seam, HEAD= equals GATED_HEAD, base=2526 head=2584 missing=0, exit 0, baseline hash matches the independent download.

## Tag and Push

Step 7 re-assertion immediately before tagging: HEAD == GATED_HEAD; no tracked change outside .planning/; nothing staged; api.txt clean; create_tag false; no v2.5 tag locally or on origin (`REASSERT-OK`). No commit was made between recording GATED_HEAD and the tag.

```
git tag -a v2.5.0 7101516e089964b3fd45696bdeb4082d4f8c5580 -m "v2.5.0 - voice UI localization, accessibility, Failure enrichment, router toggle (additive; v2.4.1 binary-compatible)"
git cat-file -t v2.5.0                      -> tag
GIT_TERMINAL_PROMPT=0 git push --dry-run origin refs/tags/v2.5.0
  To https://github.com/Ygaray/yahirandroidtaste.git
   * [new tag]         v2.5.0 -> v2.5.0     (dry rc=0)
GIT_TERMINAL_PROMPT=0 git push origin refs/tags/v2.5.0
   * [new tag]         v2.5.0 -> v2.5.0     (push rc=0)
git ls-remote --tags origin 'v2.5*'
  07e89727381157fbd49e0bfeb01bedfca5f96fc0	refs/tags/v2.5.0
  7101516e089964b3fd45696bdeb4082d4f8c5580	refs/tags/v2.5.0^{}
git tag -l 'v2.5*'                          -> v2.5.0
```

Only `refs/tags/v2.5.0` was pushed (explicit refspec; no main, no --tags, no --follow-tags, no --force).

TAG PASS: annotated tag by SHA on GATED_HEAD; peeled remote SHA equals GATED_HEAD; local and remote v2.5* sets are exactly v2.5.0.

## Step 4 Evidence (JitPack resolution)

Poll log `.gate/jitpack.log` (30 s cadence, 15-minute budget; the first request triggered the lazy build):

```
round 1 20:10:15 pom=404 aar=200 json={ "version" : "v2.5.0", "status" : "ok", ... "commit" : "7101516e089964b3fd45696bdeb4082d4f8c5580" ...
round 2 20:10:46 pom=200 aar=200 json={ "version" : "v2.5.0", "status" : "ok", ... "commit" : "7101516e089964b3fd45696bdeb4082d4f8c5580" ...
PASS round 2
```

Builds API body (`.gate/jitpack-api.json`): `"status" : "ok"`, `"commit" : "7101516e089964b3fd45696bdeb4082d4f8c5580"`, `"isTag" : true`, `"private" : false`. The whitespace-tolerant matches for status ok, isTag true and commit == GATED_HEAD all passed together with pom 200 and aar 200 in round 2. This is JitPack's own remote build; a local publish run was not used as this proof.

JITPACK PASS: .pom 200, .aar 200, status ok, isTag true, commit equals GATED_HEAD (round 2 of 30).

## Post-tag ABI Check on the Published AAR (non-gating)

DEC-4: this run is non-gating (an immutable tag cannot be un-cut); a failure would be escalated as a corrective v2.5.1 candidate. Command: `HEAD_AAR=<.gate/jitpack-v2.5.0.aar> SKIP_BUILD=1 tools/verify-binary-abi.sh v2.4.1` (log `.gate/abi-published.log`, rc `.gate/abi-published.rc` = 0). The SEAMS line lists HEAD_AAR and SKIP_BUILD as expected for this run only; the gating run above had no seam.

```
SEAMS: HEAD_AAR SKIP_BUILD
HEAD=7101516e089964b3fd45696bdeb4082d4f8c5580
HEAD_AAR sha256=d83212f7a07af099ce9d08201ed995fdf742f41d60ca5f194ad33f120051a323 path=.../.gate/jitpack-v2.5.0.aar
BASELINE_AAR sha256=df21a126c07065a37cc63d4a9a5bb1cd3cac430a4026e3da37032ed75480689e source=gradle-cache
base=2526 head=2584 filtered_ComposableSingletons=12 missing=0
ABI-ADDITIVE PASS: 0 missing public descriptors vs v2.4.1
```

PUBLISHED-ABI PASS: the JitPack-built v2.5.0 AAR keeps every v2.4.1 public descriptor (missing=0; same 2584 head lines as the local build, though the AAR bytes differ).

## Cut Mechanism and Coordination (D-02)

- The tag, push and JitPack confirmation were performed in this repo (v2.4.1 / Phase 14 precedent), per DEC-5. `xrepo` is not on PATH in this repo session (`xrepo: command not found`); it is a control-plane release-window lock that cannot run from here (`.gate/cut-mechanism.txt`). This is the CONTEXT discretion fallback.
- Messaging: this executor has no cross-session messaging tool. Nothing was sent; no confirmation is claimed. Soft outcome per DEC-2 (Phase 14 precedent); `19-SHIP-LEDGER-ROW.md` carries the relay.
- Orchestrator name (DEC-6): resolved from the `orchestrator` key of the control-plane `effort.json` at cut time and again at relay time: `yahir-gsd-control-plane-3b`. D-02 and ROADMAP cite `yahir-gsd-control-plane-6e`, the pre-restart session of the same role; both appear in the ledger row.
- The section 11 ledger (`CROSS-REPO-SCOPE-CONTRACT.md`) was not written and no consumer repo was touched (A14).

CUT-MECH INFO: manual in-repo annotated tag and push (DEC-5); orchestrator resolved as yahir-gsd-control-plane-3b; build-window notices not sent (no messaging tool), non-blocking.

## Close-out (SHIP-03)

```
git tag -l 'v2.5*'                           -> v2.5.0
"create_tag": false,                         (.planning/config.json line 15)
git rev-parse 'v2.5.0^{commit}'              -> 7101516e089964b3fd45696bdeb4082d4f8c5580 (== GATED_HEAD)
git rev-parse v2.5.0                         -> 07e89727381157fbd49e0bfeb01bedfca5f96fc0 (tag object)
git merge-base --is-ancestor GATED_HEAD HEAD -> ancestor
git diff --name-only GATED_HEAD HEAD         -> (none at close-out; the evidence commit adds only .planning files)
git ls-remote --tags origin 'v2.5.0^{}'      -> 7101516e089964b3fd45696bdeb4082d4f8c5580
remote v2.5* refs (peeled stripped)          -> refs/tags/v2.5.0
```

CLOSE PASS: exactly one v2.5* tag (v2.5.0) locally and on origin, create_tag false, tag resolves to GATED_HEAD locally and remotely, no path outside .planning/ changed since GATED_HEAD.
