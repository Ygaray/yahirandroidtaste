# Phase 4: Repin Bookkeeping Hardening - Research

**Researched:** 2026-09-01
**Domain:** Cross-repo tooling bookkeeping — Markdown marker-block contract for a Python reconciliation script (`repin_status.py`) + control-plane incident lifecycle closure
**Confidence:** HIGH

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

**D-01 [consumer-name]:** seed the row name as `CalTracker_Android` (matches `render_matrix_block`'s
output exactly → byte-stable first run) rather than `CalTracker` — `reconcile` always writes the
checkout dir name, so seeding `CalTracker` makes run-one report a one-time rename a byte-stability
verifier could misread as a failure. The matrix is a *separate* machine-owned
`<!-- repin-matrix -->` block, never wrapping the §1 human prose table `reconcile` would overwrite
(settled). _(source: ai-auto)_

**D-02 [inc-closure]:** confirm `INC-2026-08-28-03`'s worded acceptance text via the `incident`
tooling / control-plane log first, then map the reconcile evidence 1:1 to it before marking closed
— rather than treating a clean `reconcile` run as self-sufficient closure. The incident lives in
the control-plane log, not this repo, so its acceptance condition must be read and matched
explicitly. _(source: ai-auto)_

### Claude's Discretion

None recorded — both in-scope decisions (D-01, D-02) are locked. All other execution details
(exact insertion point, exact seed values, exact incident-lifecycle commands) are this research's
job to pin down, which this document does.

### Deferred Ideas (OUT OF SCOPE)

The actual coordinated repin execution is Phase 5 — this phase only hardens/proves the reconcile
bookkeeping ahead of it.
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|--------------|----------------------|
| REPIN-01 | The hub's `ECOSYSTEM.md` carries the `repin-matrix` markers so `repin_status.py reconcile` operates without hand edits — closes `INC-2026-08-28-03`. | Exact marker pair, insertion point, and byte-for-byte seed content verified live (see Architecture Patterns, Code Examples). Root-cause `ValueError` reproduced live. Post-seed `validate`/`reconcile` idempotency verified live (see Validation Architecture). Pre-commit lane-gate confirmed clean (Lane 1) for the real diff, so no `HUB_LANE_OVERRIDE` is needed. Incident-closure lifecycle (`diagnose`→`fix`→`verify`→`close`) mapped to D-02 via the `incident` skill contract (see Open Questions #2 for the one unverified sub-step). |
</phase_requirements>

## Summary

This phase is pure integration work: no new code, no new dependencies, one Markdown edit to
`ECOSYSTEM.md` plus a control-plane incident-lifecycle update. Everything the phase needs was
verified **live, this session**, against the actual running tool and the actual repo's pre-commit
gate — not inferred from documentation.

The core finding: `repin_status.py reconcile --hub yahirandroidtaste` currently fails with a
`ValueError` (root cause now **VERIFIED**, not just suspected as the incident states) because
`ECOSYSTEM.md` has no `<!-- repin-matrix:begin -->`/`<!-- repin-matrix:end -->` marker pair. I
constructed the exact seed block by importing `repin_status.py` live and calling
`render_matrix_block()` against the real discovered consumer data (`CalTracker_Android` pinned
`v1.5.0`, 7 tags behind; `SecondBrain` pinned `v1.10.0`, current — hub's live latest tag is
`v1.10.0`, confirmed via `git ls-remote --tags`, network reachable from this sandbox). I then
**live-tested the full insertion** against the real `ECOSYSTEM.md` (seed → `validate` → `reconcile`
→ confirm no further diff → `git checkout --` to discard): it works end-to-end, first run reports
`no drift`, exit 0. I also tested the exact real diff against the repo's own pre-commit lane
classifier (`tools/classify-hub-change.sh`) — it returns **LANE 1** (clean, no
`HUB_LANE_OVERRIDE` needed), which **updates** a stale project memory claiming `.planning/`/doc
edits get false-flagged: that was true before Phase 3's GOV-03 fix landed; it is no longer true.

**Primary recommendation:** Insert a new `### Machine-reconciled pin matrix` subsection directly
after the existing §1 human-prose consumer table (after its closing `_(Best-effort cache…)_` note,
before the `**Current published tag:**` paragraph), containing ONLY the
`<!-- repin-matrix:begin -->`/`<!-- repin-matrix:end -->`-wrapped block seeded with the exact
`render_matrix_block()` output below. Commit normally (no `HUB_LANE_OVERRIDE` required). Then run
the incident tool's `diagnose` → `fix` → `verify` (dispatches `incident-verifier`) → `close
--resolution fixed` lifecycle against `INC-2026-08-28-03` in the control-plane repo, mapping each
step's evidence 1:1 to the incident's stated acceptance test.

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Repin matrix render/parse contract | Documentation / Registry (`ECOSYSTEM.md`, git-tracked) | External Tooling (`repin_status.py`, lives outside this repo at `~/.claude/context/deps/`) | The hub repo owns the data (the marker block); the control-plane-adjacent script owns the parse/render logic. Neither may assume the other's internal layout beyond the marker contract. |
| Repin reconciliation execution (`validate`/`reconcile`) | External Tooling / CLI (`repin_status.py`, stdlib-only Python) | — | Runs entirely outside this repo; touches `ECOSYSTEM.md` as a file, has no awareness of Gradle/Kotlin. |
| Incident lifecycle closure | Control-plane process/governance (`yahir-gsd-control-plane/incidents/`) | — | Incident tracking is explicitly routed to the control-plane repo, never a project's own tracker (see `incident` skill routing rule). This hub repo change is *evidence* for closure, not the closure record itself. |
| Pre-commit governance gate on the `ECOSYSTEM.md` edit | Repo tooling / local CI gate (`tools/classify-hub-change.sh`, `tools/pre-commit`) | — | Gates every commit in this repo; already verified this phase's edit passes it cleanly (Lane 1). |

## Standard Stack

No new libraries, packages, or dependencies are introduced by this phase. `repin_status.py` is an
existing Python 3 stdlib-only script (`re`, `json`, `subprocess`, `time`, `pathlib`, `argparse`) —
this phase only seeds the Markdown data contract it already expects. `git` and `python3` are the
only external tools invoked, both already present in this environment
`[VERIFIED: ran directly this session]`.

**Installation:** None required.

## Package Legitimacy Audit

**N/A — this phase installs no external packages.** No `npm install`/`pip install`/`cargo add`
occurs. Skip the legitimacy gate; there is nothing to check.

## Architecture Patterns

### System Architecture Diagram

```
 ┌─────────────────────────┐        ┌───────────────────────────────┐
 │  Consumer manifests       │       │  github.com/Ygaray/yahirandroidtaste │
 │  (SecondBrain,             │      │  (git ls-remote --tags)         │
 │   CalTracker_Android)      │      └────────────┬────────────────────┘
 │  gradle/libs.versions.toml │                    │ tags → hub_tags() (1h cache)
 └────────────┬───────────────┘                    │
              │ discover_edges() scans ~/Projects   │
              ▼                                     ▼
        ┌───────────────────────────────────────────────┐
        │           repin_status.py (external tool)       │
        │  discover_edges → classify → status_report      │
        └───────────────────────┬───────────────────────┘
                                 │ validate | reconcile
                                 ▼
        ┌───────────────────────────────────────────────┐
        │ ECOSYSTEM.md  (THIS PHASE'S TARGET)              │
        │  §1 human prose table  (unchanged, narrative)     │
        │  ### Machine-reconciled pin matrix  (NEW)         │
        │    <!-- repin-matrix:begin -->                    │
        │    | Consumer | Pinned | Latest | Status |        │
        │    <!-- repin-matrix:end -->                      │
        └───────────────────────┬───────────────────────┘
                                 │ commit (Lane 1, no override)
                                 ▼
                    tools/pre-commit → classify-hub-change.sh
                                 │ exit 0
                                 ▼
                     git commit lands in yahirandroidtaste

        ┌───────────────────────────────────────────────┐
        │  yahir-gsd-control-plane/incidents/               │
        │  INC-2026-08-28-03.md  (separate repo)            │
        │  open → diagnosed → fixed → verified → closed      │
        │  evidence = the reconcile run above (D-02)          │
        └───────────────────────────────────────────────┘
```

### Recommended insertion point in `ECOSYSTEM.md`

Insert immediately after the existing §1 note (verbatim anchor text, confirmed present at that
exact position by `Read` this session — `ECOSYSTEM.md:36-38`):

```
_(Best-effort cache — keep it current: a new consumer adds a row; a repin updates "Pins hub at".
  The authoritative pin is each consumer's manifest + `./gradlew :app:dependencies` resolution.
  No "Deploy host" column — Android apps are installed on devices, not daemon-deployed.)_
```

and before the `**Current published tag:**` paragraph (`ECOSYSTEM.md:40`). This keeps the new
block a **sibling** of the human table inside §1, never wrapping/replacing it — satisfying D-01.

### Pattern: Machine-owned marker block, separate from narrative prose

**What:** A `<!-- repin-matrix:begin -->…<!-- repin-matrix:end -->` fenced region containing
*only* the 4-column table `render_matrix_block()` emits — no narrative text inside the fence.
**When to use:** Any time a tool needs to own a specific slice of a human-maintained doc without
clobbering surrounding prose.
**Why this shape specifically:** `parse_ecosystem_matrix()`/`_matrix_region()` restrict parsing to
strictly between the two markers **once they exist** — so once seeded, the pre-existing §1 prose
table (which also happens to contain the word "Consumer" in its own header) becomes invisible to
the tool entirely; there is no risk of the two tables being confused post-seeding
`[VERIFIED: ~/.claude/context/deps/repin_status.py:123-155 — read this session]`.

**Exact seed content (verified live, byte-for-byte, this session):**
```markdown
### Machine-reconciled pin matrix

> Regenerated by `repin_status.py reconcile --hub yahirandroidtaste` (see
> `~/.claude/context/workflows/repin.md`). Do not hand-edit the rows between the markers.

<!-- repin-matrix:begin -->
| Consumer | Pinned | Latest | Status |
|---|---|---|---|
| CalTracker_Android | v1.5.0 | v1.10.0 | behind |
| SecondBrain | v1.10.0 | v1.10.0 | current |
<!-- repin-matrix:end -->
```

`[VERIFIED: ran this exact insertion against the live ECOSYSTEM.md this session, then
python3 ~/.claude/context/deps/repin_status.py reconcile --hub yahirandroidtaste → printed
"no drift", exit 0; python3 …/repin_status.py validate --hub yahirandroidtaste → printed
"yahirandroidtaste: ECOSYSTEM.md matrix matches truth", exit 0. Reverted via git checkout --
ECOSYSTEM.md immediately after, confirmed byte-identical to the pre-test file via diff.]`

The table content itself (row order, cell values) was not hand-typed — it is the literal `repr()`
output of calling `render_matrix_block(status_report(hub="yahirandroidtaste"))` from a live Python
session that imported `repin_status.py` directly:
```
'| Consumer | Pinned | Latest | Status |\n|---|---|---|---|\n| CalTracker_Android | v1.5.0 | v1.10.0 | behind |\n| SecondBrain | v1.10.0 | v1.10.0 | current |'
```
Row order is alphabetical by consumer name (`sorted(statuses, key=lambda e: e["consumer"])` —
`[VERIFIED: repin_status.py:182 — read this session]`), so `CalTracker_Android` sorts before
`SecondBrain` (`C` < `S`).

### Anti-Patterns to Avoid

- **Wrapping the markers around the §1 human table.** `reconcile` overwrites everything strictly
  between the markers with `render_matrix_block()`'s 4-column output on every run — it would
  silently delete the entire narrative history (tag-cut records, Gate-1 evidence, deviation
  writeups) the first time `reconcile` actually finds drift and writes. This is D-01's explicit
  concern and this phase's single highest-consequence mistake to avoid.
- **Seeding the CalTracker row as `CalTracker` instead of `CalTracker_Android`.** The tool derives
  consumer names from the checkout directory's top-level folder name under `~/Projects`
  (`_consumer_name()`), and the real checkout is `~/Projects/CalTracker_Android`
  `[VERIFIED: ran repin_status.py status --hub yahirandroidtaste --json this session — output
  literally shows "consumer": "CalTracker_Android"]`. Seeding `CalTracker` would make the very
  first `reconcile` run silently rename the row — not a functional bug, but exactly the kind of
  diff a byte-stability check would misread as a failure (D-01's stated rationale).
- **Treating today's `Latest`/`Status`/`behind_count` values as durable.** They are a live snapshot
  of `git ls-remote --tags` at research time (2026-09-01). If the hub cuts a new tag before this
  phase executes, `reconcile` will (correctly) write different `Latest`/`Status` values than seeded
  here — that is expected, correct tool behavior, not a bug. The **executor must re-derive current
  truth at execution time** via `python3 ~/.claude/context/deps/repin_status.py status --hub
  yahirandroidtaste --json` rather than blindly copying this document's numbers if time has passed.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Parsing/writing the pin matrix | A custom regex/sed edit of `ECOSYSTEM.md` | `repin_status.py reconcile` (already exists, already correct) | The whole point of this phase is to make the *existing* tool work — hand-editing the table defeats REPIN-01 and re-creates the manual-reconcile failure mode `INC-2026-08-28-03` documents. |
| Discovering current consumer pins | Grepping `libs.versions.toml` files by hand | `repin_status.py status --hub yahirandroidtaste --json` | Already does the catalog `[versions]`/`[libraries]` cross-reference correctly (confirmed live this session against both real consumer manifests). |

**Key insight:** This phase's job is narrowly to make an existing, already-correct tool
operational against this one hub — not to write new reconciliation logic.

## Common Pitfalls

### Pitfall 1: Confusing "singular marker" phase-name language with the actual sentinel contract
**What goes wrong:** The phase/roadmap language says "repin-matrix markers" (could be read as one
marker). The tool requires a **begin/end pair**.
**Why it happens:** Casual phase naming vs. the literal `ValueError` check
(`_MATRIX_BEGIN not in text or _MATRIX_END not in text`).
**How to avoid:** Always add both `<!-- repin-matrix:begin -->` and `<!-- repin-matrix:end -->`.
**Warning signs:** `reconcile` still raises `ValueError` after adding only one comment.

### Pitfall 2: Running `reconcile` when the hub's own tag status is `unknown`
**What goes wrong:** `reconcile_ecosystem()` raises `ValueError` if ANY consumer's status is
`"unknown"` — i.e. if `git ls-remote --tags https://github.com/Ygaray/yahirandroidtaste` fails
(offline, DNS issue, GitHub outage). `[VERIFIED: repin_status.py:187-189 — read this session]`
**Why it happens:** The tool refuses to write `Latest`/`Status` columns it can't actually derive,
rather than writing a stale or blank value.
**How to avoid:** Confirm network reachability before running `reconcile` for real:
`git ls-remote --tags https://github.com/Ygaray/yahirandroidtaste` — this succeeded from this
sandbox this session (28 tags listed, `v1.10.0` is latest), but if the phase executes in a
network-isolated environment (e.g. a different sandboxed subagent), this exact step may need to
run in an environment with live GitHub reachability. There is a 1-hour local cache at
`~/.cache/repin-status/tags.json`, so a prior successful fetch this session may satisfy a
same-machine re-run within the hour even if network drops later.
**Warning signs:** `reconcile` error mentions "refusing to reconcile with unknown tag status
(offline?)".

### Pitfall 3: Reading "the acceptance test wants an actual rewrite" too literally
**What goes wrong:** The incident's proposed acceptance text says reconcile should "exit 0 and
**update** the matrix rows … without a hand edit." If the seeded values already exactly match live
truth (as they will if seeded correctly using this research's live-verified values), the real
`reconcile` run will report **`no drift`** rather than performing a visible rewrite — because
there is nothing to update. This is *correct* behavior, not a failure, but a literal reading of
"updates the matrix rows" could be misinterpreted as requiring a visible diff.
**Why it happens:** The incident was written before this research verified the exact seed content;
its author reasonably expected an initial hand-reconcile gap to close via a visible write.
**How to avoid:** Map to ROADMAP.md's own SC3 wording instead, which is the less literal and more
authoritative success criterion: *"the reconcile output reflects the true pin state for both
consumers"* — satisfied by either a write or a confirmed `no drift`/`ECOSYSTEM.md matrix matches
truth` result. If a literal before/after diff is wanted as stronger evidence, the plan can
optionally seed with a deliberately stale value first (e.g. leave the `CalTracker_Android` row at
a placeholder pin), run `reconcile` once to observe a real write, then run it a second time to
observe `no drift` (idempotency) — see Validation Architecture below.

### Pitfall 4: Assuming `HUB_LANE_OVERRIDE` is still needed for this doc-only commit
**What goes wrong:** A stale session memory (captured 2026-08-29, before Phase 3/GOV-03 landed)
claims this repo's pre-commit hook false-flags `.planning/`/doc-only commits as lane-2, requiring
`HUB_LANE_OVERRIDE`. Following that stale guidance would add an unnecessary override flag and an
unneeded "bypassing the gate" explanation to the commit.
**Why it happens:** `tools/verify-additive-diff.sh`'s default path enumeration used to scan the
whole tree; GOV-03 (Phase 3, already complete — see `tools/verify-additive-diff.sh:53-64`, commit
history `fix(03): …` `5ea861d`/`dc5bf6f`/`1845d3b`) fixed it to scope to `git ls-tree … -- src/main`
only, and `verify-api-additive.sh` only ever looked at `api.txt`. Neither touches root-level
Markdown.
**How to avoid:** Trust the live test result over the stale memory: staging the exact real
`ECOSYSTEM.md` insertion and running `tools/classify-hub-change.sh --baseline v1.10.0` returned
`LANE 1 (mode=additive, baseline=v1.10.0)`, exit 0 — `[VERIFIED: ran this exact command against
the exact real diff this session, twice — once with a trivial edit, once with the full real
insertion]`. **No `HUB_LANE_OVERRIDE` is required for this phase's commit.**
**Warning signs:** If a future edit to this repo's `tools/` scripts widens the scan scope again,
re-verify with the same dry-run technique before assuming this holds.

## Code Examples

### Reproduce the current failure (root cause, verified this session)
```bash
python3 ~/.claude/context/deps/repin_status.py reconcile --hub yahirandroidtaste
# error: /home/yahir/Projects/Reusable/yahirandroidtaste/ECOSYSTEM.md: no <!-- repin-matrix:begin/end --> markers (add them once around the consumer matrix before reconcile)
# exit 2
```
`[VERIFIED: ran verbatim this session against the current, unmodified ECOSYSTEM.md]`

### Compute the exact seed block for any future re-seed (don't hand-type it)
```bash
cd /home/yahir/Projects/Reusable/yahirandroidtaste
python3 - <<'EOF'
import sys
sys.path.insert(0, "/home/yahir/.claude/context/deps")
import repin_status as rs
statuses = rs.status_report(hub="yahirandroidtaste")
print(rs.render_matrix_block(statuses))
EOF
```
`[VERIFIED: exact command run this session, output reproduced above in Architecture Patterns]`

### Post-insertion verification sequence (what the plan's verification task should run)
```bash
# 1. Confirm no more ValueError, and no drift on a correctly-seeded first run:
python3 ~/.claude/context/deps/repin_status.py validate --hub yahirandroidtaste
# expect: "yahirandroidtaste: ECOSYSTEM.md matrix matches truth", exit 0

python3 ~/.claude/context/deps/repin_status.py reconcile --hub yahirandroidtaste
# expect: "no drift", exit 0   (or "reconciled" if live tag state moved since seeding — also a pass)

# 2. Confirm idempotency (run again, must still be clean):
python3 ~/.claude/context/deps/repin_status.py reconcile --hub yahirandroidtaste
# expect: "no drift", exit 0
```
`[VERIFIED: this exact 2-command sequence ran clean this session against a live-seeded copy of
the real file]`

### Pre-commit dry-run (confirm no override needed before committing)
```bash
git add ECOSYSTEM.md
API_FILE="$(git rev-parse --show-toplevel)/api.txt" \
  bash tools/classify-hub-change.sh --baseline "$(git describe --tags --abbrev=0 --match 'v*')"
# expect: LANE 1 (mode=additive, baseline=v1.10.0), exit 0
```
`[VERIFIED: ran this exact command against the exact real ECOSYSTEM.md insertion this session]`
Note: a real `git commit` does not need `API_FILE` exported manually — `tools/pre-commit` exports
it itself (`export API_FILE="${API_FILE:-$ROOT/api.txt}"`,
`[VERIFIED: tools/pre-commit:8 — read this session]`). The explicit export above is only needed
because this dry-run invoked `classify-hub-change.sh` directly, bypassing the hook wrapper.

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|---------------|--------|
| Hand-reconcile the 4 repin registries for this hub (`validate`/`tag-status`/`verify-landed` only) | `reconcile` writes the matrix automatically once markers exist | This phase | Closes the exact gap `INC-2026-08-28-03` documents; matches other hubs (e.g. backup-engine) whose `ECOSYSTEM.md` already has working markers per the incident's own cross-check suggestion. |
| Pre-commit lane-gate treats root-level docs/`.planning/` as lane-2 (needs `HUB_LANE_OVERRIDE`) | Scoped to `src/main` only — docs commit clean at Lane 1 | Phase 3 (GOV-03), already landed before this phase | A stale session memory (2026-08-29) still describes the old behavior — do not follow it for this phase's commit. |

**Deprecated/outdated:** The `~/.claude/projects/.../memory/hub-additive-guard-blocks-planning-docs.md`
memory note's guidance (always use `HUB_LANE_OVERRIDE` for docs commits in this repo) is now
stale for this repo's current state — GOV-03 already fixed the underlying false-flag it describes.

## Runtime State Inventory

Not applicable — this is not a rename/refactor/migration phase. No renamed identifiers, no
datastores, no OS-registered state, no secrets, and no build artifacts are affected by adding a
Markdown marker block and closing a control-plane incident record.

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|----------------|
| A1 | The exact `Latest`/`Status`/`behind_count` values seeded in this document (SecondBrain `current`, CalTracker_Android `behind` by 7) remain accurate at phase execution time. | Architecture Patterns, Pitfall 3 | Low — if a new hub tag is cut before execution, `reconcile` will correctly write different values; the executor should re-run `repin_status.py status --hub yahirandroidtaste --json` at execution time rather than copy this document's numbers verbatim, per the explicit warning in Anti-Patterns. This is flagged as a process note, not a blind assumption — the mitigation is already documented. |
| A2 | The incident's literal "updates the matrix rows" acceptance wording is satisfiable by a `no drift` result (not requiring a visible file diff) when seeded values already match truth, per D-02's instruction to map evidence to the *worded acceptance text*, cross-checked against ROADMAP.md SC3's less literal phrasing. | Pitfall 3 | Medium — if the incident's author (owner) intends the stricter "must show a real write" reading, the plan should include the optional two-pass (stale-seed → write → re-run → no-drift) demonstration described in Pitfall 3 and Validation Architecture, rather than relying on the single-pass `no drift` result alone. Flag for `/gsd-discuss-phase` or owner confirmation if ambiguity matters. |

**If this table is empty:** N/A — see entries above. All package/tooling/code claims elsewhere in
this document were verified live this session; only the two time-sensitivity/interpretation risks
above carry residual uncertainty.

## Open Questions

1. **Should the phase demonstrate a genuine before/after write, not just a `no drift` confirmation?**
   - What we know: A single correctly-seeded `reconcile` run reports `no drift` (verified live).
     This satisfies ROADMAP SC2/SC3 literally.
   - What's unclear: Whether the incident owner wants to *see* `reconcile` actually rewrite
     something as stronger proof the mechanism works end-to-end (not just that it's a no-op on
     already-correct data).
   - Recommendation: Default to the single-pass seed (matches D-01's byte-stability intent
     precisely). If the planner or `/gsd-discuss-phase` wants stronger before/after evidence, add
     an optional verification task: seed with an intentionally stale `CalTracker_Android` pin
     value, run `reconcile` once (expect a real write + `git diff` showing the pin column change),
     then re-run to confirm `no drift`, then finally reseed/rerun with the correct live values
     before committing.

2. **Does the incident-verifier subagent need filesystem access to this hub repo to independently
   confirm `reconcile` behavior, or is it satisfied by the evidence pasted into the incident's Fix
   section?**
   - What we know: The `incident` skill's `verify` step dispatches an `incident-verifier` subagent
     that "attempts to REFUTE both the claimed root cause and the claimed fix against the live
     system" — implying it re-runs commands itself rather than trusting pasted text.
   - What's unclear: Whether that subagent has (or needs) access to
     `~/Projects/Reusable/yahirandroidtaste` — it should, since both are on the same local
     machine/filesystem, but this wasn't tested in this research session.
   - Recommendation: The plan's incident-closure task should pass the subagent the exact
     verification commands from this document's Code Examples section so it can independently
     re-run `validate`/`reconcile` against the (by-then-committed) real `ECOSYSTEM.md`.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|--------------|-----------|---------|----------|
| `python3` | `repin_status.py` execution | ✓ | (system Python 3, stdlib-only script) | — |
| `git` | `repin_status.py`'s `git ls-remote`/manifest discovery; repo commit | ✓ | (system git) | — |
| Network reachability to `github.com/Ygaray/yahirandroidtaste` | `reconcile`'s tag-status derivation (fails closed with `ValueError` if unreachable — Pitfall 2) | ✓ (confirmed this session: 28 tags listed via `git ls-remote --tags`) | — | 1h local cache at `~/.cache/repin-status/tags.json`; if truly offline at execution time, `reconcile` will refuse — run `validate` only and defer `reconcile` until reachable. |
| `~/.claude/context/deps/repin_status.py` | The reconcile mechanism itself | ✓ (read + executed live this session) | — | — |
| `yahir-gsd-control-plane` repo checkout | Incident closure (`diagnose`/`fix`/`verify`/`close`) | ✓ (confirmed at `~/Projects/yahir-agentic-tools/yahir-gsd-control-plane`, git log inspected this session) | — | — |

**Missing dependencies with no fallback:** None.

**Missing dependencies with fallback:** Network reachability to GitHub — has a 1h cache fallback,
and graceful `ValueError` refusal (never a stale silent write) if genuinely offline.

## Validation Architecture

This phase makes no Kotlin/Compose code changes, so the repo's Robolectric/JVM test suite
(`./gradlew testDebugUnitTest`) is **not the relevant validation surface** — nothing under `src/`
changes. Validation here is **functional/CLI verification** of the tooling contract, run directly
against `repin_status.py` and the pre-commit gate, exactly as demonstrated live in this research
session.

### Test Framework
| Property | Value |
|----------|-------|
| Framework | None (not applicable — no source code changes) |
| Config file | — |
| Quick run command | `python3 ~/.claude/context/deps/repin_status.py validate --hub yahirandroidtaste` |
| Full suite command | `python3 ~/.claude/context/deps/repin_status.py reconcile --hub yahirandroidtaste` (run twice — second run proves idempotency) |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|---------------------|-------------|
| REPIN-01 | `ECOSYSTEM.md` carries markers `reconcile` requires | functional/CLI | `python3 ~/.claude/context/deps/repin_status.py reconcile --hub yahirandroidtaste` — must exit 0, not raise `ValueError` | ✅ — tool already exists, verified this session |
| REPIN-01 | `reconcile` succeeds with no hand edits, reflecting true pin state | functional/CLI | `python3 ~/.claude/context/deps/repin_status.py validate --hub yahirandroidtaste` — must print "matrix matches truth", exit 0 | ✅ |
| REPIN-01 | Commit lands without triggering the lane-gate | functional/CLI | `git add ECOSYSTEM.md && API_FILE=$(git rev-parse --show-toplevel)/api.txt bash tools/classify-hub-change.sh --baseline $(git describe --tags --abbrev=0 --match 'v*')` — must print `LANE 1`, exit 0 | ✅ |
| REPIN-01 (D-02) | `INC-2026-08-28-03` closed with 1:1-mapped evidence | process | `incident` skill's `diagnose`/`fix`/`verify`/`close --resolution fixed` sequence against the control-plane repo, citing the above three commands' outputs as evidence | ✅ (skill already exists) |

### Sampling Rate
- **Per task commit:** Run the `validate` + `reconcile` (×2, for idempotency) sequence before
  committing `ECOSYSTEM.md`.
- **Per wave merge:** N/A — single-file, single-wave phase.
- **Phase gate:** Both the `reconcile` idempotency check AND the incident's `verify` subagent
  result must be green before declaring the phase complete.

### Wave 0 Gaps
None — existing tooling (`repin_status.py`, `tools/classify-hub-change.sh`, the `incident` skill)
covers all phase requirements. No new test infrastructure is needed.

## Security Domain

This phase edits a public Markdown document and updates a local incident-tracking record; it
introduces no authentication, session, network-input-parsing-from-untrusted-sources, or
cryptographic surface. ASVS review is a formality here, included for completeness per the
`security_enforcement: true` config setting.

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|-----------------|---------|---------------------|
| V2 Authentication | No | N/A — no auth surface touched |
| V3 Session Management | No | N/A |
| V4 Access Control | No | N/A |
| V5 Input Validation | Marginal | `repin_status.py`'s regex-based Markdown/manifest parsing already exists and is unchanged by this phase; this phase's own input (the seed table) is authored by the agent, not attacker-controlled |
| V6 Cryptography | No | N/A — no secrets, no crypto |

### Known Threat Patterns for this stack
None applicable. This phase touches no user-facing surface, no network-facing endpoint, and no
credential material — `ECOSYSTEM.md` is already public (this repo is public per its own
`CLAUDE.md`/`ECOSYSTEM.md`), and the control-plane incident log is local-machine-only tooling
bookkeeping.

## Sources

### Primary (HIGH confidence — verified live this session)
- `~/.claude/context/deps/repin_status.py` — read in full and executed directly (`status`,
  `validate`, `reconcile`, and a raw Python import calling `render_matrix_block()`/`status_report()`)
  this session.
- `/home/yahir/Projects/Reusable/yahirandroidtaste/ECOSYSTEM.md` — read in full; live-tested a real
  insertion against it and reverted via `git checkout --`.
- `/home/yahir/Projects/Reusable/yahirandroidtaste/tools/pre-commit`,
  `tools/classify-hub-change.sh`, `tools/verify-additive-diff.sh`, `tools/verify-api-additive.sh`
  — read in full; ran the real classifier against the real diff this session.
- `git ls-remote --tags https://github.com/Ygaray/yahirandroidtaste` — ran live this session (28
  tags, latest `v1.10.0`).
- `~/Projects/SecondBrain/gradle/libs.versions.toml`,
  `~/Projects/CalTracker_Android/gradle/libs.versions.toml` — read/grepped this session.
- `~/.claude/skills/incident/SKILL.md`,
  `~/Projects/yahir-agentic-tools/yahir-gsd-control-plane/incidents/_schema.md`,
  `.../incidents/INC-2026-08-28-03-....md` — read in full this session.
- `git log` against the control-plane repo and this repo's `tools/` scripts — ran this session to
  confirm GOV-03's fix commits already landed.

### Secondary (MEDIUM confidence)
- `.planning/research/SUMMARY.md` (this project's own prior milestone-research pass) — cross-
  checked against live behavior; one claim (P3-before-P4 sequencing to avoid
  `HUB_LANE_OVERRIDE`) is now superseded since P3 already landed, confirmed live this session.

### Tertiary (LOW confidence, now superseded)
- Session memory `hub-additive-guard-blocks-planning-docs.md` (2026-08-29) — described the
  pre-GOV-03 pre-commit behavior; explicitly contradicted by this session's live test. Documented
  in State of the Art / Pitfall 4 as stale, not carried forward as guidance.

## Metadata

**Confidence breakdown:**
- Standard stack: N/A — no new stack introduced
- Architecture (marker-block seed content, insertion point, lane-gate behavior): HIGH — every claim
  was executed live against the real files/tools this session, then reverted
- Pitfalls: HIGH — each pitfall was either directly triggered/observed (Pitfall 1, the actual
  `ValueError`) or its avoidance was directly tested (Pitfalls 2 via source read + cache
  mechanism, 4 via live lane-classifier run)
- Incident-closure mechanics (D-02): MEDIUM — the `incident` skill's lifecycle contract is
  well-documented and read in full, but the `incident-verifier` subagent's actual re-verification
  behavior against this specific hub was not exercised this session (see Open Question 2)

**Research date:** 2026-09-01
**Valid until:** The seeded `Latest`/`Status`/`behind_count` values are valid only as a snapshot —
re-derive at execution time if more than a few hours have passed (a new hub tag could be cut at
any time via any consumer's autonomous-bump convention documented in `ECOSYSTEM.md` §7). The
marker-contract mechanics and lane-gate behavior are stable until either `repin_status.py` or
`tools/classify-hub-change.sh` change (no near-term change signal found).
