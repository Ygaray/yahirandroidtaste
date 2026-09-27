# Phase 9: Ship & coordinated repin - Context

**Gathered:** 2026-09-26
**Status:** Ready for planning

<domain>
## Phase Boundary

Once Phases 6–8 are green on `main`, cut library `v2.2.0` (human-gated), surface the coordinated
consumer repins, and reconcile the hub's own ECOSYSTEM.md — completing v1.0's GARD-02 and clearing W-1.
Depends on Phases 6, 7, 8. The hub run edits NO consumer repos; consumer repins execute in their own
channels.

</domain>

<decisions>
## Implementation Decisions

### apicheck-ki
- **D-01 [apicheck-ki]:** Apply KI-2026-09-02-01 fix option 1 — hide the Dagger-generated `@DaggerGenerated`/`UndoHistoryStore_Factory` symbol from the metalava surface and rebaseline `api.txt`, so the false "Removed class" clears on **both** the Debug variant and Release `apiCheck` (human — chose to fix, not route around). _(provisional — refresh at execution; depends on Phase 6)_ — the rebaseline must reflect Phases 6/7's final additive `api.txt`.

### publish-version
- **D-02 [publish-version]:** Leave the `build.gradle.kts` version marker stale (`1.10.0`) — JitPack overrides the version from the resolved git ref, and the marker was never bumped for v2.0.0/v2.1.0 either; an optional bump to `2.2.0` in the tag commit is hygiene-only (collision-free: tags are `vX.Y.Z`, marker is bare `X.Y.Z`) (ai-auto).

### w1-scope
- **D-03 [w1-scope]:** Clear W-1 fully — run `repin_status.py reconcile` post-tag to regenerate the `<!-- repin-matrix -->` machine block, AND hand-correct the stale ECOSYSTEM.md §1 narrative prose (still claims SB pins v1.11.0 / latest v1.10.0) in the same phase, since `reconcile` does not touch narrative (human).

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Milestone artifacts
- `.planning/ROADMAP.md` — Phase 9 goal + success criteria
- `.planning/REQUIREMENTS.md` — SHIP-01, SHIP-02
- `.planning/v2.0-DECISION-MAP.md` § Phase 9 — the resolved gray areas
- `.planning/KNOWN-ISSUES.md` — KI-2026-09-02-01 (the metalava false-positive + fix options)

### Ship ritual + invariants
- `docs/superpowers/specs/2026-09-26-hub-line-reunification-design.md` §6 — versioning, human-gated cut, consumer-impact, cross-repo convention
- `CLAUDE.md` (repo root) + `ECOSYSTEM.md` §7 — human-gated shipping ritual, immutable-tag rule
- `~/.claude/context/workflows/repin.md` — Mechanism B (Android/Gradle/JitPack), reconcile step, `-SNAPSHOT` trap
- `~/.claude/context/deps/repin_status.py` — `tag-status` / `verify-landed` / `reconcile` verbs

</canonical_refs>

<code_context>
## Existing Code Insights

### Established Patterns
- Human-gated tag cut: gates green on `main` → surface `v2.2.0` for owner go-ahead → cut + push immutable annotated tag → verify JitPack resolves the coordinate (lazy first build; verify-resolvable with retry, don't infer) → reconcile.
- `apiCheck` binds to `metalavaCheckCompatibilityRelease` (build.gradle.kts); the KI false-positive fires only on the Debug variant / full `./gradlew build`.

### Integration Points
- `repin_status.py reconcile` derives `latest` from `git ls-remote --tags`, so it must run **after** the tag is at `origin`; it rewrites only the machine matrix block (hence the separate §1 prose hand-fix per D-03).
- Consumer repins run in their own channels (cross-repo-hub convention, `05-CONTEXT.md` D-04): SecondBrain single-hop `v1.13.0→v2.2.0` (+ FilterBar→ChipBar migration + color wiring), CalTracker `v2.1.0→v2.2.0` (+ its own disabled-copy). Hub edits no consumer repos.

</code_context>

<specifics>
## Specific Ideas

Consumers are already aligned and waiting: SecondBrain repins once at its Phase 169; CalTracker whenever — both blocked only on the surfaced, JitPack-verified `v2.2.0` tag.

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope. (The coordinated consumer repins themselves execute downstream in each consumer's own channel, not from this hub phase.)

</deferred>

---

*Phase: 9-ship-coordinated-repin*
*Context gathered: 2026-09-26*

## Runtime Decisions

### 2026-09-27 — apiCheck KI disposition FINALIZED (operator, milestone plan stage)
Provisional decision (area: apicheck-ki, depends-on Phase 6) is now CONFIRMED by the operator after
Phase 6 completed. **Disposition: KI fix option 1** — exclude `@DaggerGenerated` types (e.g.
`UndoHistoryStore_Factory`) from the metalava/api surface, then rebaseline `api.txt` so the false
"Removed class" metalava finding (KI-2026-09-02-01) clears on BOTH Debug and Release variants. Goal:
apiCheck gate genuinely green (no standing suppressed diff); close KI-2026-09-02-01. This is the
zero-baseline-consistent path, not an accepted-override.

### 2026-09-27 — v2.2.0 tag CUT (operator-approved, milestone execute stage)
Operator explicitly approved the tag cut ("you can cut tags... i approve"). Annotated tag `v2.2.0`
created at `5310b9a` and pushed to origin (github.com/Ygaray/yahirandroidtaste). Wave 2 (Plan 09-02)
is now unblocked: verify JitPack resolution of `com.github.Ygaray:yahirandroidtaste:v2.2.0`, run
repin_status.py reconcile, and fix ECOSYSTEM.md's stale narrative (clear W-1). Consumer repins remain
human-gated — surface, do not perform.
