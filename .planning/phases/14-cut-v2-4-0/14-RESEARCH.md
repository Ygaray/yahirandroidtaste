# Phase 14: Cut v2.4.0 - Research

**Researched:** 2026-09-30
**Domain:** Git tag / JitPack release cut, cross-repo ledger protocol (procedural, no product code)
**Confidence:** HIGH

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

**tag-procedure**
- **D-01 [tag-procedure]:** `v2.4.0` is "cut" ONLY after §11 steps 1–4 all hold: (1) green FULL suite (`./gradlew testDebugUnitTest` — all four drift guards: registry, tier, domain-vocabulary, generated-symbol — plus zero-baseline `./gradlew detekt`), (2) Metalava `./gradlew apiCheck` additive vs `v2.3.0` (with `api.txt` refreshed + committed), (3) the tagged commit pushed, and (4) JitPack resolves `v2.4.0` from a clean Gradle cache. THEN message the orchestrator the full §11 ledger row (repo, tag, commit, coordinate, contents, evidence). NEVER self-write the §11 ledger (A14). Tag-cut human gate is WAIVED (A12, Yahir-confirmed in-session). — **Reversibility:** one-way — the tag is immutable once pushed; a defect needs a new `v2.4.1` patch tag + a superseded ledger row.

**no-stray-tag**
- **D-02 [no-stray-tag]:** Milestone close cuts NO git tag beyond the `v2.4.0` release coordinate. `git.create_tag` is already set to `false` in `.planning/config.json` (committed `b20d79d`) as the mechanical guard. A bare `v2.2` milestone-marker tag already leaked into the JitPack coordinate namespace at the v2.0 close — do not repeat it (SHIP-02, INC-2026-09-30-01). Git tags in this repo ARE JitPack coordinates.

### Claude's Discretion
"Cut on green verification" (gate waived) does not mean "push and walk away" — JitPack must actually build/resolve the tag before it counts as cut (§11 step 4), and the orchestrator re-checks (A14). Do NOT edit the hardcoded publishing `version` in `build.gradle.kts` — JitPack derives it from the resolved git ref.

### Deferred Ideas (OUT OF SCOPE)
Consumer repins (SecondBrain, CalTracker → `v2.4.0`) are Wave-1 work in the consumers' own channels.
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| SHIP-01 | Cut library `v2.4.0` per §11 steps 1–4 (green full suite incl. CATALOG-03, Metalava additive, tagged commit pushed, JitPack resolves the coordinate from a clean cache), then message the orchestrator the full §11 ledger row (§11, A12/A14) | §11 steps 1-4 concretized below with exact commands verified in this session; JitPack-resolution mechanism recommended from this repo's own v2.2.0/v2.3.0 precedent (ECOSYSTEM.md) |
| SHIP-02 | Milestone close creates **no** git tag beyond `v2.4.0`; guards the stray milestone-marker-tag hazard (INC-2026-09-30-01) | `.planning/config.json` `git.create_tag: false` confirmed live; Risks section documents the guard and what would defeat it |

</phase_requirements>

## Summary

Phase 14 adds no product code. It is a 4-step verification-then-tag procedure (§11 of the
cross-repo contract) followed by a message to the orchestrator — never a self-written ledger
entry (A14). The good news: **steps 1 and 2 are already proven green at current HEAD** by Phase
13's `13-SHIP-GATE-EVIDENCE.md`, captured this same milestone, at commit `5765e4e...` (an ancestor
of current HEAD `5909544`). Phase 14's job is to re-run that evidence fresh at the exact commit it
tags (never trust inherited evidence per this repo's own established practice, see Pitfall 2), then
do what Phase 13 explicitly did NOT do: tag, push, and verify JitPack resolution.

This repo has cut 24 prior tags and has a **documented, proven JitPack-verification pattern** used
for the two most recent cuts (`v2.2.0`, `v2.3.0`, both recorded in `ECOSYSTEM.md`): three direct
HTTPS checks (`.pom`, `.aar`, and the JitPack builds-API JSON) requiring no scratch Gradle project
and no local cache manipulation, because JitPack build state lives server-side, not in the
consumer's Gradle cache. This is the recommended Step 4 mechanism — lower-cost and already proven
scriptable in this exact repo.

The one genuinely new element this phase introduces is the cross-repo orchestrator-messaging
step (A14) — this repo has never done this before (its prior 24 tags predate the multi-repo
effort). No file-based convention for "the orchestrator message" exists yet in this repo; this is
flagged as an open question / assumption for the planner (see Risks and Assumptions Log).

**Primary recommendation:** Re-run Phase 13's exact verification commands fresh at the commit
about to be tagged (do not reuse `13-SHIP-GATE-EVIDENCE.md`'s captured output as proof for a later
commit), refresh+commit `api.txt` only if `apiCheck` step 1 shows local drift (it should not, since
nothing changes source between Phase 13 and Phase 14), tag with an **annotated** tag (matching
`v2.3.0`'s object type), push to `origin`, then verify JitPack with the direct-HTTPS pattern this
repo already proved twice. Produce the ledger row as a clearly delimited block in a new
`14-SHIP-LEDGER-ROW.md` artifact and, if an agent-to-agent messaging tool is available in the
execution harness, additionally send it there — but never commit it to the contract file.

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Full-suite + detekt verification | Build/CI (local Gradle) | — | Gradle test/lint tasks run entirely in the local toolchain; no network dependency |
| API-additive proof (Metalava) | Build/CI (local Gradle) | Database/Storage (committed `api.txt`) | Metalava compares generated signatures against the committed `api.txt` file, which is itself the source of truth artifact |
| Tag creation + push | Database/Storage (git object store) | CDN/Static (GitHub, the public remote) | A git tag is a stored ref; pushing publishes it to the public remote JitPack reads from |
| JitPack build resolution | CDN/Static (JitPack's build service) | — | JitPack is a third-party build-and-CDN service external to this repo; this phase only *observes* its build status, it does not control it |
| Orchestrator ledger messaging | API/Backend (cross-repo control plane) | — | The §11 ledger is owned and written exclusively by the control-plane orchestrator session (A14); this repo's phase is a client that reports in, never a writer |

This phase touches no Browser/Client or Frontend-Server tier — it is pure build/release tooling
plus one cross-session reporting step. There is no risk of tier misassignment here because the
phase has no UI or runtime application surface.

## Standard Stack

### Core
No new libraries are introduced. The phase uses tooling already wired in this repo:

| Tool | Version | Purpose | Why Standard (this repo) |
|------|---------|---------|---------------------------|
| Gradle wrapper | (repo-pinned) | `testDebugUnitTest`, `detekt`, `apiCheck` | Already the repo's sole build driver |
| Metalava via `me.tylerbwong.gradle.metalava` plugin v0.5.0 | 0.5.0 [VERIFIED: tools/README-api-guard.md:30] | Public API signature dump/check (`api.txt`) | Chosen mechanism after a documented spike rejected two alternatives (see `tools/README-api-guard.md`) |
| JitPack | n/a (hosted service) | Resolves `com.github.Ygaray:yahirandroidtaste:<tag>` from GitHub | Existing distribution channel for all 24 prior tags |

### Supporting
None — this phase installs no new packages. Package Legitimacy Audit is **not applicable** (see
below).

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Direct-HTTPS JitPack verification (`.pom`/`.aar`/builds-API `curl`) | A scratch Gradle project declaring `implementation("com.github.Ygaray:yahirandroidtaste:v2.4.0")` and resolving dependencies | The scratch-project approach requires standing up a second Gradle project and manually clearing `~/.gradle/caches/modules-2/files-2.1/com.github.Ygaray` to force a non-cached resolution; the direct-HTTPS approach is 3 `curl` calls, already proven twice in this exact repo (`v2.2.0`, `v2.3.0`), and avoids any local-cache-state ambiguity entirely because JitPack build status lives server-side |

**Installation:** N/A — no packages installed this phase.

## Package Legitimacy Audit

**Not applicable.** This phase installs zero external packages (no `npm install`, `pip install`,
or new Gradle dependency). It only invokes already-configured Gradle tasks and issues `git`/`curl`
commands. No disposition table is required.

## Architecture Patterns

### System Architecture Diagram

```
 [HEAD at phase start, Phase 13 green]
            |
            v
   +--------------------+
   | Step 1: verify      |  ./gradlew testDebugUnitTest   (4 drift guards + full suite)
   | full suite + detekt |  ./gradlew detekt               (zero-baseline)
   +--------------------+
            | BUILD SUCCESSFUL (both)
            v
   +--------------------+
   | Step 2: verify      |  ./gradlew apiCheck                     (local sync check)
   | API additive vs     |  swap api.txt <- git show v2.3.0:api.txt
   | v2.3.0               |  ./gradlew apiCheck                     (TRUE compat verdict)
   +--------------------+  restore api.txt; git status --short api.txt == empty
            | additive confirmed, api.txt committed + clean
            v
   +--------------------+
   | Step 3: tag + push  |  git tag -a v2.4.0 -m "..."
   +--------------------+  git push origin v2.4.0
            | tag live on origin
            v
   +--------------------+
   | Step 4: confirm      |  curl .../yahirandroidtaste-v2.4.0.pom        -> 200
   | JitPack resolves     |  curl .../yahirandroidtaste-v2.4.0.aar        -> 200
   | the coordinate       |  curl .../api/builds/.../v2.4.0               -> status:"ok", isTag:true,
   +--------------------+                                                   commit == git rev-list -n1 v2.4.0
            | all 3 signals pass
            v
   +--------------------+
   | Produce ledger row   |  write 14-SHIP-LEDGER-ROW.md (repo/tag/commit/coordinate/contents/evidence)
   | (never self-commit   |  message orchestrator session if a messaging tool is available (A14)
   | to §11)               |
   +--------------------+
```

### Recommended Project Structure
No new files beyond planning artifacts and one evidence file:
```
.planning/phases/14-cut-v2-4-0/
├── 14-CONTEXT.md          # already exists
├── 14-RESEARCH.md         # this file
├── 14-SHIP-GATE-EVIDENCE.md   # fresh re-verification transcript at the exact tagged commit
└── 14-SHIP-LEDGER-ROW.md      # the §11 row content for the orchestrator, NOT committed to the contract
```

### Pattern 1: Swap-baseline additive proof (Phase 13's proven technique — reuse verbatim)
**What:** `apiCheck` only compares current code against the file *currently on disk* named
`api.txt`. To get the TRUE verdict against a specific historical tag, temporarily swap that tag's
`api.txt` onto disk, run `apiCheck`, then restore.
**When to use:** Any time API-01-style "additive vs tag X" must be proven, not merely "additive vs
whatever is currently committed."
**Example (from `13-SHIP-GATE-EVIDENCE.md`, already run and green this milestone):**
```bash
# Source: .planning/phases/13-catalog-integrity-v2-4-0-ship/13-SHIP-GATE-EVIDENCE.md
cp api.txt /tmp/api13ship.bak
git show v2.3.0:api.txt > api.txt
./gradlew apiCheck          # BUILD SUCCESSFUL in 36s -> additive vs v2.3.0 confirmed
cp /tmp/api13ship.bak api.txt
git status --short api.txt  # must print nothing
```
**Note for Phase 14:** this compound command sequence must be re-run fresh at the exact commit
being tagged — Phase 13's captured output is evidence for Phase 13's HEAD, not automatically valid
proof for whatever commit Phase 14 eventually tags (see Pitfall 2).

### Pattern 2: Direct-HTTPS JitPack resolution check (this repo's own proven precedent, v2.2.0 and v2.3.0)
**What:** Three `curl` calls prove a tag is live and non-cached on JitPack — no scratch project
needed.
**When to use:** §11 step 4, every tag cut in this repo going forward.
**Example:**
```bash
# Source: ECOSYSTEM.md lines 481-491 (v2.3.0 cut record) and
# .planning/milestones/v2.0-phases/09-ship-coordinated-repin/09-VERIFICATION.md (v2.2.0 cut record)
curl -s -o /dev/null -w '%{http_code}\n' \
  https://jitpack.io/com/github/Ygaray/yahirandroidtaste/v2.4.0/yahirandroidtaste-v2.4.0.pom
curl -s -o /dev/null -w '%{http_code}\n' \
  https://jitpack.io/com/github/Ygaray/yahirandroidtaste/v2.4.0/yahirandroidtaste-v2.4.0.aar
curl -s https://jitpack.io/api/builds/com.github.Ygaray/yahirandroidtaste/v2.4.0
# expect JSON containing "status":"ok", "isTag":true, and "commit":"<the tagged SHA>"
# cross-check: git rev-list -n1 v2.4.0   # must equal the "commit" field exactly
```
Both prior cuts (`v2.2.0`: `09-VERIFICATION.md`; `v2.3.0`: `ECOSYSTEM.md:481-491`) report this
exact 3-signal pattern returning 200/200/`status:"ok"` on the **first poll after the tag push**,
each time "well inside the 10-minute bound" — i.e., no multi-minute wait was actually needed
historically, though JitPack's documented behavior is a lazy build-on-first-request, so a retry
loop with backoff is still the correct implementation (see Pitfall 1).

### Anti-Patterns to Avoid
- **Treating `publishReleasePublicationToMavenLocal` success as proof of §11 step 4:** that task
  only proves the *local* build publishes; it says nothing about whether JitPack's own remote
  build of the pushed tag succeeds (different JDK/toolchain environment, different trigger).
  `jitpack.yml`'s `install: ./gradlew publishReleasePublicationToMavenLocal` is what JitPack's
  **remote** builder runs — step 4 must observe that remote outcome, not re-run the command
  locally and call it done.
- **Treating `classify-hub-change.sh`'s exit code as the additive proof:** Phase 13's own evidence
  documents this script producing a LANE 3 (breaking-change) false positive on a textbook additive
  change (`ClearableTextField`'s new trailing optional params). The swap-baseline `apiCheck` run
  (Pattern 1) is the sole authoritative signal; the shell script is corroboration only.
- **Editing `build.gradle.kts`'s hardcoded `version`:** explicitly forbidden by CONTEXT.md's
  Claude's Discretion note — JitPack derives the version from the resolved git ref, not this field.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| API-additive detection | A custom symbol-diff script | The existing `apiCheck`/Metalava pipeline (swap-baseline pattern) | Already built, already proven, already documented with a known false-positive case to avoid re-litigating |
| JitPack build-status polling | A new polling script/tool | Three `curl` one-liners (Pattern 2) | This repo has run this exact 3-call pattern twice already with recorded success; no new tooling is justified for a procedure this small |

**Key insight:** this phase's entire job is procedural composition of existing, already-proven
building blocks (Gradle tasks, `curl`, `git`) — there is nothing in this phase's scope that
benefits from new tooling.

## Common Pitfalls

### Pitfall 1: Declaring JitPack failure too early
**What goes wrong:** JitPack lazily builds a tag only on first resolution request; a build can take
up to several minutes (occasionally longer under load), and a single immediate `curl` returning a
non-200 status is not evidence of failure.
**Why it happens:** Agents (and humans) impatient-poll once and conclude the coordinate doesn't
resolve.
**How to avoid:** Poll the builds API (`/api/builds/...`) in a retry loop with backoff (e.g. every
30s up to 10 minutes) and check its JSON `status` field progression (`"status":"building"` →
`"status":"ok"`, or `"status":"error"` on genuine failure — fail only on `"error"` or on exhausting
the time budget). The prior two cuts in this exact repo both resolved on the *first* poll
(`ECOSYSTEM.md` v2.3.0 record, `09-VERIFICATION.md` v2.2.0 record) — in practice resolution has
been fast historically — but the plan's verify step should not hard-fail on a single immediate
non-200, since nothing in this repo's history guarantees that will hold for v2.4.0.

### Pitfall 2: Trusting Phase 13's captured evidence as proof for Phase 14's tagged commit
**What goes wrong:** `13-SHIP-GATE-EVIDENCE.md` was captured at commit `5765e4e...`, an ancestor of
current HEAD (`5909544`, per `git log`). If any commit lands between Phase 13's evidence capture
and the commit Phase 14 actually tags (e.g. additional doc commits, or anything else), the cached
evidence no longer describes the tagged commit.
**Why it happens:** It is tempting to treat Phase 13's PASS table as already-done work and skip
straight to tagging.
**How to avoid:** Phase 14 must re-run `./gradlew testDebugUnitTest`, `./gradlew detekt`, and the
swap-baseline `apiCheck` sequence fresh, at the exact commit about to be tagged, and record a NEW
evidence file (`14-SHIP-GATE-EVIDENCE.md`) rather than citing Phase 13's. This mirrors Phase 13's
own stated discipline ("every command below was run fresh this session... not inherited from
Phase 10-12 memory").
**Warning signs:** The plan citing `13-SHIP-GATE-EVIDENCE.md`'s PASS lines as its own §11 step 1/2
evidence without re-running the commands.

### Pitfall 3: A stray milestone-marker tag leaking into the JitPack namespace (SHIP-02 / INC-2026-09-30-01)
**What goes wrong:** At the v2.0 close, a bare `v2.2` tag (no patch component) was cut as a
milestone marker and is now a *second*, unintended JitPack coordinate (`git tag -l` confirms both
`v2.2` and `v2.2.0` exist in this repo today).
**Why it happens:** GSD's own milestone-close workflow can create a tag by default; in a repo where
every tag is a live JitPack coordinate, that default is actively harmful.
**How to avoid:** `.planning/config.json` already has `"create_tag": false` under the `git` block
`[VERIFIED: .planning/config.json:15]` (content: `"create_tag": false,`). The plan's final
milestone-close step should verify this value is still `false` immediately before closing, since it
is the sole mechanical guard — do not rely on memory of having set it once.
**Warning signs:** Any `git tag -l` output showing a bare non-semver tag (e.g. `v2.4`, no patch)
after milestone close.

### Pitfall 4: Confusing `apiCheck`'s "local sync" result with the swap-baseline result
**What goes wrong:** Running only `./gradlew apiCheck` (against whatever `api.txt` is currently
committed) and treating a green result as proof the API is additive *versus v2.3.0*. It only proves
the code matches the currently committed file — which could itself already be ahead of or
inconsistent with `v2.3.0` without the comparison ever happening.
**Why it happens:** The task name `apiCheck` sounds like it does the comparison-against-a-release
job by itself; it does not — it always compares against whatever is on disk right now.
**How to avoid:** Always run the Pattern 1 swap-baseline sequence as the authoritative evidence for
"additive vs v2.3.0"; treat a plain `apiCheck` run as only proving local-tree/file consistency.

### Pitfall 5: Signing/annotation mismatch on the new tag
**What goes wrong:** Using a lightweight tag (`git tag v2.4.0`) when the repo's convention and
prior tag are annotated objects.
**Why it happens:** `git tag <name>` without `-a`/`-m` silently creates a lightweight tag with no
warning.
**How to avoid:** `v2.3.0` is confirmed an **annotated** tag object `[VERIFIED: git cat-file -t
v2.3.0]` (output: `tag`, not `commit`) — and `09-VERIFICATION.md`'s v2.2.0 record explicitly notes
"unsigned is expected/acceptable; the repo's ritual requires immutable-annotated, not GPG-signed."
Cut `v2.4.0` with `git tag -a v2.4.0 -m "<message>"` (unsigned is fine, annotated is the
convention), never a bare lightweight tag.

### Pitfall 6: Not knowing what "message the orchestrator" means mechanically (A14)
**What goes wrong:** This repo's prior 24 tags all predate the multi-repo effort / orchestrator
concept; there is no established in-repo artifact or tool call that demonstrates "messaging the
orchestrator" in practice for this specific action.
**Why it happens:** `.planning/cross-repo/HANDOFF.md` describes the orchestrator being "found with
`ListAgents`" and messaged directly — implying a live multi-agent session messaging capability in
the execution harness — but no example transcript or committed artifact in this repo shows the
actual call being made for a ledger row (only for reconvene-readiness pings, per `HANDOFF.md`'s own
language "R1 ready: <path>").
**How to avoid:** See Assumptions Log (A-row) — the plan should (a) always produce the ledger row
as a clearly delimited, orchestrator-ready block in a durable file (`14-SHIP-LEDGER-ROW.md`) so the
content exists regardless of mechanism, and (b) if a session-messaging tool is available in the
execution context, additionally send it there. Never attempt to write directly to the contract
file at `~/Projects/Reusable/android/voice-action-engine/CROSS-REPO-SCOPE-CONTRACT.md` — that
violates A14 regardless of mechanism.

## Code Examples

### §11 Step 1 — full verification (exact commands, all four drift guards + zero-baseline detekt)
```bash
# Source: 13-SHIP-GATE-EVIDENCE.md (this exact command set already proved green this milestone;
# re-run fresh at Phase 14's tagged commit, do not cite Phase 13's captured output)
./gradlew testDebugUnitTest   # unscoped run — only the unscoped run exercises the full-registry
                               # check; a --tests-filtered run does not (confirmed Phase 13 research).
                               # Exercises, in one run: ComponentRegistryDriftGuardTest,
                               # ComponentRegistryTierTest, DomainVocabularyDriftGuardTest,
                               # GeneratedSymbolDriftGuardTest
                               # [VERIFIED: src/test/java/io/github/ygaray/yahirandroidtaste/explorer/
                               #  {ComponentRegistryDriftGuardTest,ComponentRegistryTierTest,
                               #  DomainVocabularyDriftGuardTest,GeneratedSymbolDriftGuardTest}.kt
                               #  — all four files confirmed present via Glob this session]
./gradlew detekt               # zero-baseline: config/detekt-baseline.xml must remain
                               # literally "<SmellBaseline><ManuallySuppressedIssues/>
                               # <CurrentIssues/></SmellBaseline>"
                               # [VERIFIED: config/detekt-baseline.xml — read this session, exact
                               #  current content is <?xml version="1.0" ?><SmellBaseline>
                               #  <ManuallySuppressedIssues/><CurrentIssues/></SmellBaseline>]
```
No separate guard task exists outside `testDebugUnitTest` — Phase 13's plan and evidence both
confirm the single unscoped run covers all four drift guards; there is no fifth guard task to
discover.

### §11 Step 2 — API additive vs v2.3.0, refresh + commit if needed
```bash
# First: confirm no local drift against the currently committed api.txt
./gradlew apiCheck
# If this fails (code has public-API changes not yet dumped), regenerate:
./gradlew apiDump          # [VERIFIED: ./gradlew tasks --all output this session confirms
                             #  "apiDump - Regenerates the committed public-API signature file
                             #  (delegates to Metalava)." exists as a registered task name]
git add api.txt && git commit -m "..."   # only if apiDump produced a diff

# Then: the TRUE swap-baseline verdict against v2.3.0 (Pattern 1, Phase 13-proven)
cp api.txt /tmp/api14ship.bak
git show v2.3.0:api.txt > api.txt
./gradlew apiCheck          # must be BUILD SUCCESSFUL -> additive vs v2.3.0
cp /tmp/api14ship.bak api.txt
git status --short api.txt  # must print nothing (fully restored)
```
At the time of this research (current HEAD `5909544`), `api.txt` is already committed, clean
(`git status --porcelain api.txt` empty), and its diff against `v2.3.0` is purely additive (410
insertions, 1 deletion of a stale concatenation-list line, 0 removed symbol lines per
`13-SHIP-GATE-EVIDENCE.md`'s authoritative swap-baseline run) `[VERIFIED: git diff v2.3.0 --stat --
api.txt, run this session]`. No `apiDump` should be needed unless a commit lands between this
research and Phase 14's tag that changes the public API.

### §11 Step 3 — tag and push
```bash
# v2.3.0 confirmed an annotated tag object this session:
#   git cat-file -t v2.3.0  ->  tag        [VERIFIED: git cat-file -t v2.3.0, run this session]
# origin confirmed the sole/correct remote this session:
#   git remote -v  ->  origin  https://github.com/Ygaray/yahirandroidtaste.git (fetch/push)
#   [VERIFIED: git remote -v, run this session]
git tag -a v2.4.0 -m "v2.4.0 — voice command UI (settings cards, outcome/undo sheet, clarification bar)"
git push origin v2.4.0
```

### §11 Step 4 — confirm JitPack resolution (recommended: direct HTTPS, this repo's proven pattern)
```bash
# Source: ECOSYSTEM.md:481-491 (v2.3.0 cut) and
# .planning/milestones/v2.0-phases/09-ship-coordinated-repin/09-VERIFICATION.md (v2.2.0 cut)
# — the exact pattern used for the two most recent prior tag cuts in this repo.
POM_CODE=$(curl -s -o /dev/null -w '%{http_code}' \
  https://jitpack.io/com/github/Ygaray/yahirandroidtaste/v2.4.0/yahirandroidtaste-v2.4.0.pom)
AAR_CODE=$(curl -s -o /dev/null -w '%{http_code}' \
  https://jitpack.io/com/github/Ygaray/yahirandroidtaste/v2.4.0/yahirandroidtaste-v2.4.0.aar)
BUILD_JSON=$(curl -s https://jitpack.io/api/builds/com.github.Ygaray/yahirandroidtaste/v2.4.0)
TAGGED_SHA=$(git rev-list -n1 v2.4.0)
# Pass criteria: POM_CODE==200, AAR_CODE==200, BUILD_JSON contains "status":"ok" and "isTag":true,
# and BUILD_JSON's "commit" field equals $TAGGED_SHA exactly.
# If BUILD_JSON shows "status":"building" (first poll can be a cold-start lazy trigger), retry
# every 30s up to a 10-minute budget before declaring failure (Pitfall 1).
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|---------------|--------|
| Human-gated tag cut (every prior tag in this repo, SB's own CLAUDE.md doctrine) | Agent cuts the tag autonomously on green §11 verification | A12 (2026-09-29), confirmed in-session for this effort | Phase 14's plan should NOT insert a `checkpoint:human-verify`/approval gate before the tag push — A12 explicitly waives it. The orchestrator re-checks post-hoc instead (A14) |
| Per-repo §11 ledger self-maintained in each repo's own doc | A14: control-plane orchestrator is the sole §11 ledger writer | A14 (2026-09-29) | This repo must never append to `CROSS-REPO-SCOPE-CONTRACT.md`'s §11 table itself — only report the row content |

**Deprecated/outdated:** The bare `v2.2` milestone-marker-tag pattern (no patch component) is
retired; `git.create_tag: false` is now the standing guard against it recurring (D-02).

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | "Message the orchestrator" mechanically means using a live multi-agent session-messaging tool (e.g., something like `ListAgents`/`SendMessage` referenced in `HANDOFF.md`) available in the execution harness, in addition to producing a durable `14-SHIP-LEDGER-ROW.md` file | Pitfall 6, Risks | If no such tool exists in the plan's actual execution context, the planner must fall back to the file-only approach and flag a human/orchestrator-session relay step; if the plan assumes a tool call that doesn't exist, the task will fail or silently no-op |
| A2 | JitPack's lazy-build time budget of "up to 10 minutes" (used for the retry-loop recommendation) is a reasonable upper bound based on this repo's own prior release-log phrasing ("well inside the 10-minute bound") rather than JitPack's own published SLA | Pitfall 1, Code Examples §11 Step 4 | If a build genuinely takes longer under load, a 10-minute retry budget could falsely declare failure; low risk given 2/2 prior cuts resolved on first poll |

**All other claims in this research were verified this session** (file reads, `git` commands,
`./gradlew tasks --all` output) or cited from this repo's own committed evidence/doc files
(`13-SHIP-GATE-EVIDENCE.md`, `ECOSYSTEM.md`, `09-VERIFICATION.md`, `tools/README-api-guard.md`) —
no training-knowledge package names or external-ecosystem claims are made in this research (no
packages are installed this phase).

## Open Questions

1. **Does the execution harness expose an agent-to-agent messaging tool this session can call for A14's "message the orchestrator" step?**
   - What we know: `.planning/cross-repo/HANDOFF.md` describes the orchestrator being found via
     `ListAgents` and messaged directly for reconvene-readiness pings; this repo has never done a
     §11 ledger-row message before (its 24 prior tags predate the multi-repo effort).
   - What's unclear: whether that messaging capability is available/invocable from inside a GSD
     plan task's action, or whether it is purely a convention for the human-driven top-level
     session.
   - Recommendation: the planner should design the final task to (a) always write
     `14-SHIP-LEDGER-ROW.md` with the full row content (durable, mechanism-independent), and (b)
     attempt the live message only if the tool is confirmed available in that task's context,
     treating its absence as a soft "produced for human/orchestrator relay" outcome, not a hard
     failure of SHIP-01.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| Gradle wrapper + JDK | Steps 1-2 (test/detekt/apiCheck) | ✓ (already used successfully in Phase 13 this session) | openjdk17 (`jitpack.yml:2`) | — |
| `git` + push access to `origin` | Step 3 | ✓ `[VERIFIED: git remote -v, run this session — origin -> github.com/Ygaray/yahirandroidtaste.git]` | — | — |
| Network access to `jitpack.io` | Step 4 | Not re-probed this session (no live `curl` run during research — research avoided network calls that would itself trigger a build); treat as available by strong precedent: 24/24 prior tags in this repo resolved, most recently `v2.2.0` and `v2.3.0` both confirmed 200/200/`status:ok` on first poll | — | If `jitpack.io` is unreachable from the execution environment at tag time, the planner should add a retry/escalate-to-human step rather than silently declaring SHIP-01 incomplete |
| Cross-repo agent-messaging tool (A14 step) | "message the orchestrator" | Unknown — not verified this session (see Open Questions #1) | — | File-based `14-SHIP-LEDGER-ROW.md` artifact for human/orchestrator relay |

**Missing dependencies with no fallback:** none identified — every dependency either has strong
precedent of availability or a documented fallback.

**Missing dependencies with fallback:**
- Cross-repo agent-messaging tool → file-based ledger-row artifact for manual/human relay to the
  orchestrator session.

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit (Robolectric/Compose-UI tests) via Gradle `testDebugUnitTest`, Detekt via `detekt`, Metalava via `apiCheck` |
| Config file | `build.gradle.kts` (Metalava block, `tasks.register("apiCheck"/"apiDump")`), `config/detekt-baseline.xml` |
| Quick run command | `./gradlew testDebugUnitTest` (full/unscoped — a `--tests`-filtered run does not exercise the full-registry drift guard, per Phase 13's own research finding) |
| Full suite command | `./gradlew testDebugUnitTest && ./gradlew detekt && ./gradlew apiCheck` |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|---------------------|-------------|
| SHIP-01 (step 1) | Full suite + all 4 drift guards + zero-baseline detekt green at tagged commit | unit/static-analysis | `./gradlew testDebugUnitTest && ./gradlew detekt` | ✅ (all 4 drift-guard test files confirmed present this session) |
| SHIP-01 (step 2) | Public API strictly additive vs `v2.3.0` | static-analysis (Metalava) | `cp api.txt /tmp/b.bak && git show v2.3.0:api.txt > api.txt && ./gradlew apiCheck; cp /tmp/b.bak api.txt` | ✅ (pattern proven, Phase 13) |
| SHIP-01 (step 3) | Tagged commit pushed to `origin` | manual/scripted git check | `git ls-remote --tags origin v2.4.0` (must match local `git rev-parse v2.4.0^{}`) | ✅ (git is present and configured) |
| SHIP-01 (step 4) | JitPack resolves `v2.4.0` from a clean build | smoke (network) | `curl` triple (Pattern 2 above) | ✅ (pattern proven twice in this repo) |
| SHIP-01 (ledger) | Orchestrator receives the full §11 row | manual/scripted (A14) | produce `14-SHIP-LEDGER-ROW.md`; message orchestrator if tool available | ⚠ mechanism unconfirmed (Open Question #1) |
| SHIP-02 | No stray tag beyond `v2.4.0` created at milestone close | config check | `grep '"create_tag"' .planning/config.json` must show `false` immediately before milestone close | ✅ (currently `false`, `[VERIFIED: .planning/config.json:15]`) |

### Sampling Rate
- **Per task commit:** re-run `./gradlew testDebugUnitTest && ./gradlew detekt && ./gradlew apiCheck` after any file change (there should be none beyond a possible `api.txt` refresh).
- **Per wave merge:** N/A — single-wave, single-plan phase expected (no product code, no parallel waves).
- **Phase gate:** the full §11 step 1-4 command sequence, run fresh at the exact commit tagged, is itself the phase gate — there is no separate "full suite" beyond what §11 already requires.

### Wave 0 Gaps
None — existing test infrastructure (all 4 drift guard tests, detekt, Metalava `apiCheck`) fully
covers this phase's requirements. No new test file is needed; this phase authors zero new
production or test code.

## Security Domain

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|-------------------|
| V2 Authentication | no | No auth surface touched |
| V3 Session Management | no | N/A |
| V4 Access Control | no | N/A |
| V5 Input Validation | no | No user input processed this phase |
| V6 Cryptography | no | No crypto surface touched; tag is unsigned-but-annotated by established convention (Pitfall 5), not a security control |

This phase has no application-security surface — it is release tooling only (git tag, push, HTTP
GET checks against a public CDN). The one supply-chain-adjacent concern is covered below.

### Known Threat Patterns for this phase

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|----------------------|
| A breaking (non-additive) API change reaching consumers under a false "additive" claim | Tampering / Repudiation | The swap-baseline `apiCheck` (Pattern 1) is the sole authoritative proof; the plan must not accept the naive lane-classifier script's exit code as sufficient (documented false-positive precedent, Phase 13) |
| A stray git tag polluting the JitPack coordinate namespace (SHIP-02) | Tampering (of the release-coordinate namespace) | `git.create_tag: false` mechanical guard in `.planning/config.json`, re-checked immediately before milestone close (Pitfall 3) |
| Tag declared "cut" before JitPack's remote build actually succeeds | Repudiation (false completion claim) | §11 step 4's live `curl` triple (Pattern 2) — never substitute a local `publishReleasePublicationToMavenLocal` run for this remote-build proof |

## Sources

### Primary (HIGH confidence — read/run directly this session)
- `.planning/phases/14-cut-v2-4-0/14-CONTEXT.md` — locked decisions D-01, D-02, A12/A14 text
- `.planning/ROADMAP.md` § Phase 14, § Phase 13 — success criteria, sequencing
- `.planning/REQUIREMENTS.md` — SHIP-01, SHIP-02 exact text and traceability table
- `~/Projects/Reusable/android/voice-action-engine/CROSS-REPO-SCOPE-CONTRACT.md` §11 + A12 + A14 — full tag protocol, ledger table (empty — no tags cut yet under this protocol)
- `.planning/cross-repo/HANDOFF.md` — orchestrator identity, messaging convention description, current cross-repo state
- `jitpack.yml` — confirmed `install: ./gradlew publishReleasePublicationToMavenLocal` is what JitPack's remote builder runs
- `tools/verify-api-additive.sh`, `tools/README-api-guard.md` — Metalava mechanism choice, `apiDump`/`apiCheck` task names, known value-class ABI-tracking limitation
- `.planning/phases/13-catalog-integrity-v2-4-0-ship/13-01-PLAN.md`, `13-SHIP-GATE-EVIDENCE.md` — the exact proven command sequences this phase reuses, and their fresh-this-session PASS verdicts
- `build.gradle.kts`, `api.txt`, `config/detekt-baseline.xml` — read directly this session to confirm Metalava config, current additive state vs `v2.3.0`, and zero-baseline detekt content
- `git` commands run directly this session: `git tag -l`, `git cat-file -t v2.3.0`, `git remote -v`, `git log`, `git status --porcelain`, `git diff v2.3.0 --stat -- api.txt`, `./gradlew tasks --all`
- `ECOSYSTEM.md` lines 460-496 (v2.3.0 cut record) and `.planning/milestones/v2.0-phases/09-ship-coordinated-repin/09-VERIFICATION.md` (v2.2.0 cut record) — this repo's own proven JitPack-verification pattern

### Secondary (MEDIUM confidence)
None — all findings in this research trace to primary sources read/run directly this session.

### Tertiary (LOW confidence)
None.

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH — no new tools; all tooling already wired and proven in this exact repo this milestone
- Architecture: HIGH — §11 steps are a fixed, contract-defined sequence; JitPack-verification pattern has 2/2 prior successful precedent in this exact repo
- Pitfalls: HIGH — all six pitfalls are drawn from this repo's own documented incidents/evidence (Phase 13 evidence, INC-2026-09-30-01, `tools/README-api-guard.md`), not generic external knowledge
- Orchestrator-messaging mechanism (A14): LOW — genuinely novel for this repo; flagged as Open Question #1 and Assumption A1

**Research date:** 2026-09-30
**Valid until:** This research is tied to a specific commit (`5909544`) and tag target (`v2.4.0`); re-verify steps 1-2 fresh if any commit lands before the plan executes. The JitPack-pattern and §11-protocol findings are stable until the next contract amendment.
