---
phase: 29-docs-v2-8-0-cut-readiness
plan: 04
subsystem: tooling
tags: [release-gate, pre-push-scan, junit-freshness, fixture-tests, shell]
status: complete
requires:
  - phase: 29-03
    provides: "docs finished; the range now holds every docs change that the scan must cover"
provides:
  - "tools/verify-junit-fresh.sh: freshness, totals and required-class proof for a Gradle JUnit result directory"
  - "tools/verify-publish-range.sh: range facts and a net plus history scan (tailnet address, host suffix, home path, credential) with a positive control per class"
  - "two fixture tests picked up by tools/test/run-all.sh (34 and 71 assertions)"
  - "the one shipped-path tailnet address replaced by a documentation-range address"
affects: [29-05, 29-06]
plan_head_before: ff36f63e5f63693450da058f54ebaf5ae4a805a1
actuals:
  tokens: 8300
  tasks: 3
  commits: 3
key-files:
  created:
    - tools/verify-junit-fresh.sh
    - tools/test/test-verify-junit-fresh.sh
    - tools/verify-publish-range.sh
    - tools/test/test-verify-publish-range.sh
  modified:
    - tools/test/test-verify-tester-restored.sh
key-decisions:
  - "PD-1 committed scripts instead of inline pipes, so every gate command in 29-05 and 29-06 is one plain command"
  - "PD-2 .planning/ hits and history-only shipped hits are reported but do not fail the scan; a net shipped-path hit, a net credential, a path outside the allowlist or a non-fast-forward does"
  - "PD-3 the replacement fixture serial is 192.0.2.1:1496 (RFC 5737 TEST-NET-1)"
  - "PD-4 history is not rewritten; the neutralization is a forward commit"
  - "A credential found only in history also returns RESULT REVIEW (strict reading of 'no credential'); PD-2 only exempts non-credential history hits"
requirements-completed: [SHIP-05]
duration: 20 min
completed: 2026-10-18
---

# Phase 29 Plan 04: Gate tooling for the v2.8.0 cut Summary

Two committed, fixture-tested scripts (`verify-junit-fresh.sh`, `verify-publish-range.sh`) now produce the forced-fresh test proof and the pre-push range scan as single plain commands, and the only shipped-path tailnet address in the unpushed range was swapped for a documentation address. The scan of `origin/main..HEAD` now reads `RESULT OK` with net_credential=0 and net_shipped=0.

SHIP-05 stays open in REQUIREMENTS.md (plans 29-05 and 29-06 still own it); I did not mark it.

## What was built

- **`tools/verify-junit-fresh.sh <dir> --marker <file> [--require <Class>]...`** is read-only. Fresh means every `TEST-*.xml` is strictly newer than the marker. It prints `FILES`, `STALE`, `TOTALS tests= skipped= failures= errors=`, one `CLASS` line per required class, and ends `JUNIT-FRESH OK` or `JUNIT-FRESH FAIL <reasons>` (exit 1); usage errors exit 2 and evaluate nothing. A required class matches by exact simple name (a longer class name does not satisfy it). Fixture: 34 assertions.
- **`tools/verify-publish-range.sh --base --head --out [--diff-file] [--history-file]`** prints `RANGE`, `FASTFORWARD`, `AUTHORS`, `BINARY`, `OUTSIDE`, four `CONTROL` lines (run before scanning; a failed control prints `RESULT INVALID` and exits 2), `NET` and `HISTORY` lines, `SUMMARY` and `RESULT`. Credential hits print class, path and count only; the matched text never reaches stdout or any file under `--out` except `range.diff` (the scanned text). Binary paths go to `<out>/binary-files.txt` (113 files in this range, mostly UAT evidence PNGs) to keep stdout short. Fixture: 71 assertions on temp repos built with git plumbing (no commit hook runs), including a self-scan proving the tool and fixture text match none of their own classes.
- Every pattern and synthetic sample in both tools is assembled from fragments, so no committed file holds a whole match.

## Dry run on the current tree (base `origin/main` = `c2182ec`, confirmed equal to `git ls-remote`, so no fetch was needed)

Before the neutralization (HEAD `bb4f4d6`, 294 commits, fast-forward yes, one author, outside allowlist 0):

`SUMMARY net_credential=0 net_shipped=1 net_planning=48 history_credential=0 history_shipped=1 history_planning=29 outside=0 binary=113 fastforward=yes` then `RESULT REVIEW`

After the neutralization commit (HEAD `e6749e6`, 295 commits):

`SUMMARY net_credential=0 net_shipped=0 net_planning=48 history_credential=0 history_shipped=1 history_planning=29 outside=0 binary=113 fastforward=yes` then `RESULT OK`

### Hit classification

| Path | Class | Count | Area | Verdict |
|------|-------|-------|------|---------|
| tools/test/test-verify-tester-restored.sh (line 134) | tailnet_ip | 1 | shipped | fixture value (the tester's wireless tailnet address 100.118.x.x with port 1496), neutralized in e6749e6 |
| .planning/phases/26..29 plans, summaries, research, self-UAT logs (8 files) | tailnet_ip | 9 | planning | reviewed and accepted, not scrubbed (prose naming the tester's wireless address; the already-pushed tree holds the same class and the v2.6 gate accepted it) |
| .planning/phases/26..29 prose and evidence logs (17 files) | home_path | 34 | planning | reviewed and accepted, not scrubbed |
| .planning/phases/29 plan and research text (4 files) | ts_net | 5 | planning | reviewed and accepted, not scrubbed (documents the scan class itself) |
| credential, all paths | credential | 0 | n/a | none found, net or history |
| the one tools/test line above, in commit 32bd7d5 | tailnet_ip | 1 | shipped (history) | history only, not rewritable (PD-4) |
| the 29 history planning hits | all | 29 | planning (history) | history only, not rewritable |

The new tools and fixtures add zero hits to the range.

## Honest limit for the proposal

Neutralizing the fixture changes the net tree diff, not the already-committed history: the history scan still lists `HISTORY tailnet_ip 32bd7d5 tools/test/test-verify-tester-restored.sh area=shipped` (the original line, in the range) and 29 `.planning/` history hits. History is not rewritten. The director decides whether the history line matters; the address is a tailnet address that is only reachable inside the tailnet.

## Fixture PASS counts

- `test-verify-junit-fresh.sh`: 34 (new)
- `test-verify-publish-range.sh`: 71 (new)
- `test-verify-tester-restored.sh`: 82 before the edit, 82 after (the wireless-serial case still asserts exit 2 and an empty fake-adb log, because the serial allowlist refuses anything that is not letters and digits before any adb call)
- `bash tools/test/run-all.sh`: all nine tests ok, no FAILED line (no Gradle was running)

## Task commits

| Task | Commit | Files |
|------|--------|-------|
| 1 | 8b7d461 | tools/verify-junit-fresh.sh, tools/test/test-verify-junit-fresh.sh |
| 2 | bb4f4d6 | tools/verify-publish-range.sh, tools/test/test-verify-publish-range.sh |
| 3 | e6749e6 | tools/test/test-verify-tester-restored.sh |

## Deviations from Plan

None - plan executed exactly as written. Two small additions inside the tool's contract: binary paths are written to `binary-files.txt` instead of printed (stdout stayed readable at 113 files), and a credential found only in history returns `RESULT REVIEW` (documented in key-decisions).

## Acceptance and verification

- `test -x` holds for both scripts; both fixtures end `FAIL=0` (34 and 71 assertions, above the 14 and 24 floors); the credential case asserts the text is absent from stdout and from every file under `--out` except `range.diff`.
- `git show --stat` of each task commit lists exactly the planned paths.
- `.planning/phases/29-docs-v2-8-0-cut-readiness/.gate/` (`scan-dryrun*`, `scan-after*`) is untracked and was never staged.
- `git tag --list 'v2.8*'` prints nothing; `git diff --stat c1681ff HEAD -- api.txt` prints nothing; the only `src` changes since `55f5288` are the two guard test files from plans 29-01 and 29-02 (no `src/main`).
- No Gradle invocation, no device, no tag, no push, no repin, no rig message.

## Next

Ready for 29-05 (Gate-1 self-UAT; use `verify-junit-fresh.sh` for the forced-fresh gate) and 29-06 (release flip and proposal; use `verify-publish-range.sh` for the range and scan).

## Self-Check: PASSED
