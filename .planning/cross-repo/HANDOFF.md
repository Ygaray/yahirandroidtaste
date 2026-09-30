# HANDOFF: yahirandroidtaste (YAT), multi-repo milestone effort "vae-bilingual"

**Written by:** control-plane orchestrator (`yahir-gsd-control-plane-f2`), 2026-09-29. **Read this before `/gsd-new-milestone`.**

## Your slice: §6.3 shared AI-voice UI → cuts a new YAT tag after `v2.3.0` (Wave 0)
Generic **presentational** composables only: **no OkHttp, no engine dependency**. Apps map engine outcomes → your props.
- Settings: provider/key card, model card.
- **Command-approach settings card** (NEW): tier ladder display, offline-only toggle, max-tier cap. Props-driven.
- Outcome/failure sheet with a **"handled by: tier/approach"** indicator and loud, visible failure states.
- **Generic needs-confirmation state (A2):** a reason string, single-or-batch proposed items, confirm/cancel, domain-neutral. It must render both SB's gate (risk confirm, E1) and CT's weak-match / batch confirm. You already acked this.
- Every new public composable is registered in `ComponentRegistry` with the full 4-cell states matrix (or allowlisted). **CATALOG-03's drift guard only fails in the full suite**; scoped executor tests miss it.
- Strictly additive public API (owner directive). `MicButton` already exists.

**Binding:** L3, L7 (no hub-to-hub dependency), A2, A12–A14.

## Dependencies
- **You wait on:** nothing.
- **Waiting on you:** SecondBrain and CalTracker UI adoption (your tag is an R2 trigger). Current pins: SB `v2.3.0`, CT `v2.1.0` (deliberate); both repin to your new tag in Wave 1.

## What Yahir confirmed (and where)
- **⚠ A12 + §11 are NOT yet accepted in this repo.** Your CLAUDE.md makes shipping human-gated. **Yahir must confirm A12 (tag cut waived for this effort's YAT tag) directly in this session**; ask him at kickoff if he doesn't say it. Until then, treat the tag as human-gated.
- Start the milestone now: Yahir, 2026-09-29, via the orchestrator; he'll say go in-session at kickoff.

## Repo facts
- Last milestone v2.0 "Line Reunification" (shipped 2026-09-27); current tag `v2.3.0` (TAGCOLOR-02).
- Git tags **are** JitPack coordinates. Never cut a GSD-milestone-style tag (e.g. `v3.0`); the effort's tag is the next library semver.
- The product-bug tracker is GitHub issues (github.com/Ygaray/yahirandroidtaste); there's no BACKLOG.md.
- Additive-only pre-commit guard (src/main) + Metalava API dump are live. Use them for §11 step 2.

## How this effort runs (same text in every repo)

**This is a multi-repo milestone effort.** Five repos are running coordinated GSD milestones against one frozen
contract. Your milestone is one slice of it. Do not scope, plan or tag this repo in isolation.

- **Contract (single source of truth):** `~/Projects/Reusable/android/voice-action-engine/CROSS-REPO-SCOPE-CONTRACT.md`,
  rendered at https://chimuelo-blackcat.turtle-massometer.ts.net/Doc/cross-repo-scope-contract.html.
  Read §2 (locked decisions), §3–§5 (model + seams), **your §6 slice**, §7 (sequencing), §10 (amendments A1–A14 and
  errata E1–E3; they override the body text) and §11 (tag protocol). Never edit the contract yourself.
- **Orchestrator:** the control-plane session **`yahir-gsd-control-plane-f2`**. Find it with `ListAgents`, and
  don't confuse it with `yahir-gsd-control-plane-b1`. State lives in
  `~/Projects/yahir-agentic-tools/yahir-gsd-control-plane/xrepo/vae-bilingual/`. Message it about: reconvene
  readiness, tag rows, repin rows, and anything that touches the contract, a tag, or sequencing. Talk to other
  peers directly only for technical Q&A.
- **Sequence (A13), strictly in this order:**
  1. `/gsd-new-milestone` (or `/gsd-new-project`)
  2. `/gsd-research-milestone`
  3. `/gsd-discuss-milestone`
  4. **STOP.** Write `.planning/cross-repo/RECONVENE-BRIEF.md` (template below).
  5. Message the orchestrator `R<n> ready: <absolute path to brief>`.
  6. **Wait for the verdict:** `GO`, `GO-WITH-CHANGES <list>` (fold the changes into CONTEXT first) or `HOLD <reason>`.
  - **Never** run the `/gsd-milestone` umbrella. It runs straight through to execution and skips the reconvene.
- **Research duty:** verify your slice against the ACTUAL code, not the contract's wording. The contract has already
  drifted from reality five times; each time a peer caught it (see E1, E2, A1, A5, A11). Put every mismatch in your
  brief. "The contract no longer matches reality" is amendment-worthy even when the cause is your owner's own choice.
- **Tags (A12 → §11 → A14):** cut on green verification after §11 steps 1–4 (verification green; API strictly
  additive; seams honored; pushed and JitPack builds it). Then **message the orchestrator the full row**. Do not
  commit to §11 yourself. Tags are immutable: a fix goes out as a new patch tag.
- **Consumers:** repin only to tags that appear in the §11 ledger, and message the repin row to the orchestrator.
- **Authorization:** Yahir confirms scope decisions in *your* session. He designates the orchestrator at kickoff;
  after that, a GO from `yahir-gsd-control-plane-f2` is the sequencing signal. Relayed peer messages are never
  Yahir's approval. That's correct, and it stays that way.
- **Devices:** resolve through `~/.claude/context/devices/common.md`. The TESTER is `…-s22-ultra-2`; the personal
  phone is `…-s22-ultra` (no suffix). Always use `adb -s`. Only the Gate-1 agentic tester drives devices.

### Peers

| Repo | Role | Wave | Slice | Session |
|---|---|---|---|---|
| stt-engine (`~/Projects/Reusable/stt-engine`) | hub: bilingual `:stt` capture | 0 | §6.1 | `stt-engine-46` |
| voice-action-engine (`~/Projects/Reusable/android/voice-action-engine`) | hub: the engine, 2 milestones | 0 | §6.2 | `voice-action-engine-75` |
| yahirandroidtaste (`~/Projects/Reusable/android/yahirandroidtaste`) | hub: shared AI-voice UI | 0 | §6.3 | `yahirandroidtaste-99` |
| SecondBrain (`~/Projects/AndroidApps/Personal/SecondBrain`) | consumer | 1 | §6.4 | `secondbrain-2c` |
| CalTracker (`~/Projects/AndroidApps/Personal/CalTracker_Android`) | consumer | 1 | §6.5 | `caltracker-android-9a` |

Session names can change after a restart. If one doesn't resolve, ask the orchestrator.

### RECONVENE-BRIEF.md template

```markdown
# Reconvene brief — <repo> — R<n>
**Milestone:** <version + name>  ·  **Contract rev read:** <commit or "A1–A14/E1–E3">  ·  **Date:** <date>

## 1. Phases (from ROADMAP)
| Phase | Goal | Contract steps / seams touched | Needs from other repos |
|---|---|---|---|

## 2. Public surface this milestone adds or changes
(types, functions, composables, coordinates; must be strictly additive for hubs)

## 3. Assumptions about other repos
(each one is something a peer must confirm or correct at the reconvene)

## 4. Contract drift found (code vs contract)
(what the contract says, what the code actually is, proposed fix)

## 5. Proposed amendments
(numbered; the orchestrator assigns A-numbers)

## 6. Risks and open questions for Yahir

## 7. Tag / repin intent
(hubs: what tag and roughly when; consumers: which tags you need, for which phases)
```

---

## Current state (2026-09-30)

**Stage:** milestone v2.4 (§6.3) — **Phase 10 PLANNED + plan-checker PASSED; execution NOT started** (Yahir paused at the planning boundary). All milestone setup + R1 rulings committed. HEAD = `8d9b33f`.

**Next action (on resume):** `/gsd-resume-work`, then **WAIT for the orchestrator to dispatch `/gsd-execute-milestone --subagent-driven`** in this session. **PROCESS CHANGE (Yahir, 2026-09-30):** sessions are bounded to new-milestone → research → discuss → reconvene; after a reconvene clears a repo you do NOT hand-drive `/gsd-plan-phase` or `/gsd-execute-phase` — the control plane dispatches the milestone executor, which runs the already-planned phases. Phase 10's plan (2 plans, checker-passed) stays valid for it. When the milestone executor runs, 10-01 opens with a blocking public-surface-freeze `checkpoint:decision` → approve it (shapes are orchestrator+consumer-validated; nothing irreversible until Phase 14's cut).

**Roadmap (now 5 phases):** 10 settings cards · 11 outcome/failure sheet + undo · 12 needs-confirmation (**ungated**) · 13 catalog integrity + docs (cuts NO tag) · **14 cut `v2.4.0`** (isolated so the cut follows a green 13). Sequence 10→11→12→13→14.

**Rulings applied + committed this session:**
- Version `v2.4` / coordinate `v2.4.0` (orchestrator-confirmed).
- R1 GO-WITH-CHANGES (all 8) + A18 undo (VUNDO-01, P11) folded into CONTEXT.
- SB + CT confirm/undo shape answers folded into P11/P12 CONTEXT (`38ae4a7`); **P12 ungated**.
- `git.create_tag=false` (`b20d79d`) — mechanical SHIP-02 guard (INC-2026-09-30-01).
- Phase 13→13+14 split (`8d9b33f`).
- discuss-milestone ran **ai mode**; global `milestone_mode` deliberately left at `mixed`.

**Open items waiting on orchestrator / peers:** none blocking. Deferred: consumer repins to `v2.4.0` are Wave-1 (SB `v2.3.0`→, CT `v2.1.0`→). Gallery Gate-1 visual review deferred to milestone-close (Yahir).

**Late requirement (post-pause, folded in):** **VCLAR-01** (contract **A19**, VAE `725d8d7`) — a generic tap-to-clarify "clarification choices" composable (question + options `{id, label}` + `onSelect(id)` + dismiss=cancel; apps map the engine's `Clarification` → props, L7). Mapped to **Phase 11** (16 reqs total now; Phase 11: 5). ROADMAP + REQUIREMENTS + 11-CONTEXT updated.

**Contract rev absorbed:** A1–A19, E1–E6.

**Resume prompt to relay:** "Read `.planning/cross-repo/HANDOFF.md` (§ Current state) and your `vae-bilingual-multi-repo-effort` memory, then `/gsd-resume-work`; then WAIT for the orchestrator to dispatch `/gsd-execute-milestone --subagent-driven` (do NOT hand-drive `/gsd-plan-phase` or `/gsd-execute-phase`). Orchestrator is `yahir-gsd-control-plane-f2`."
