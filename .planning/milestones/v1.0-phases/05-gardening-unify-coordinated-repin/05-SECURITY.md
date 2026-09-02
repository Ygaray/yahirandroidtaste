---
phase: 05
slug: gardening-unify-coordinated-repin
status: verified
# threats_open = count of OPEN threats at or above workflow.security_block_on (high) severity
threats_open: 0
asvs_level: 1
audited_head: 6d5f21d881c803d78d7310226f83d9c9c30180ae
created: 2026-09-02
---

# Phase 05 — Security

> Per-phase security contract: threat register, accepted risks, and audit trail.

State B (no `*-SECURITY.md` existed): register built from `05-01-PLAN.md`/`05-02-PLAN.md`/
`05-03-PLAN.md` `<threat_model>` blocks (`register_authored_at_plan_time: true`). Preliminary
classification found `threats_open: 0` at `asvs_level: 1` — per `secure-phase.md`'s short-circuit
rule (threats_open:0 AND register_authored_at_plan_time:true AND asvs_level==1), L1 grep-depth
verification is sufficient and no deeper `gsd-security-auditor` dispatch was required. Verification
below cross-checks each mitigation against evidence already independently gathered this run by the
`gsd-code-reviewer` and `gsd-verifier` agents (both re-ran `apiCheck`/`detekt`/full test suite and
grepped source directly), not merely the plan authors' own claims.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| N/A (05-01, 05-02) | Same-package Compose UI refactor (ChipBar/FilterBar fold-in, SheetHeaderMenu extraction) plus ComponentRegistry/api.txt bookkeeping. No new network call, no persisted data, no secret material — the hub holds none (CLAUDE.md invariant). | None |
| Hub `main` → both consumers (05-03, Task 2) | The coordinated repin is the one point in this phase where a change crosses from the hub repo into two independent consumer applications (SecondBrain, CalTracker). | Not yet crossed — gated behind the unresolved `checkpoint:decision` (see T-05-05 below); no consumer file was touched by this run. |

---

## Threat Register

| Threat ID | Category | Component | Severity | Disposition | Mitigation | Status |
|-----------|----------|-----------|----------|-------------|------------|--------|
| T-05-01 | Tampering | `api.txt` rebaseline (`./gradlew apiDump`, WO-1 + WO-2) | medium | mitigate | Line-by-line diff review performed as a required task step both rebaselines (05-01 Task 2, 05-02 Task 2) — confirmed via SUMMARY.md + independently re-verified by `gsd-verifier` this run (grepped `api.txt`/`API.md` directly: zero stale `FilterBar` mentions, `ExpandableConfig` present, `SheetHeaderMenuKt` absent). | closed |
| T-05-02 | Elevation of Privilege | `SheetHeaderMenu`'s public-vs-internal visibility choice | low | mitigate | Declared `internal` per UI-SPEC's locked default; `apiCheck` confirms no public symbol leaked. Independently confirmed this run by both `gsd-code-reviewer` ("no public API leak — confirmed via api.txt grep") and `gsd-verifier`. | closed |
| T-05-03 | Tampering | Pre-commit lane-gate override (`HUB_LANE_OVERRIDE`) | low | accept | Intentional, human-legible coordination gate for a real lane-3 (non-additive) change — both 05-01 and 05-02 landed via the override (actual value `=2`, per the pre-existing T-05-04 classifier bug, not `=3` as originally drafted; documented as a deviation in both SUMMARYs). | closed |
| T-05-04 | (informational — pre-existing, not introduced this phase) | `verify-api-additive.sh` absolute-vs-relative path bug — silently no-ops the lane-3 API-break detector | medium | accept | Tracked in `.planning/STATE.md` Blockers/Concerns since before Phase 3 (commit `534ec10`); out of this phase's scope to fix. `./gradlew apiCheck`, the mechanism this phase's own gates actually rely on, is separate and unaffected — re-confirmed green (`--rerun-tasks`, non-cached) by `gsd-verifier` this run. | closed (accepted, pre-existing) |
| T-05-05 | Tampering / Spoofing | Git tag `v2.0.0` cut + consumer coordinate bump (SecondBrain, CalTracker) | high | mitigate | Never executed autonomously anywhere in this phase — the blocking `checkpoint:decision` in `05-03-PLAN.md` Task 2 was reached and correctly left UNRESOLVED pending the human's explicit go/hold, per CLAUDE.md's human-gated-shipping rule. Confirmed this run: `git tag -l` shows no `v2.0.0`; no SecondBrain/CalTracker path was touched by any commit in this phase. | closed (mitigation = correctly-unresolved checkpoint, not yet exercised) |
| T-05-06 | Repudiation | Post-repin pin-state drift (a consumer silently ends up on the wrong tag) | medium | mitigate | Not yet applicable — the repin itself has not happened (see T-05-05). `repin_status.py reconcile` (Phase 4 tooling) is the required post-repin step named in the checkpoint's own resume-signal; will apply once the human acts. | not yet applicable |
| T-05-SC | Tampering | npm/pip/cargo installs | n/a | accept | Not applicable — this phase added no new third-party dependency. | closed |

*Status: open · closed · open — below `high` threshold (non-blocking) · not yet applicable*
*Severity: critical > high > medium > low — only open threats at or above `workflow.security_block_on` (high) count toward `threats_open`*
*Disposition: mitigate (implementation required) · accept (documented risk) · transfer (third-party)*

---

## Accepted Risks Log

| Risk ID | Threat Ref | Rationale | Accepted By | Date |
|---------|------------|-----------|-------------|------|
| AR-05-01 | T-05-03 | Lane-gate override is the correct, expected mechanism for an intentional non-additive API change — not a bypass. | plan authors (05-01/05-02 CONTEXT) | 2026-09-01 |
| AR-05-02 | T-05-04 | Pre-existing classifier bug, out of this phase's scope; the working `apiCheck` Gradle task is the mechanism this phase's own gates rely on. | tracked in STATE.md since before Phase 3 | 2026-09-01 |
| AR-05-03 | T-05-SC | No new third-party dependency introduced. | plan authors | 2026-09-01 |

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-09-02 | 7 | 6 (1 not-yet-applicable) | 0 | gsd-execute-phase orchestrator (short-circuit L1 path, cross-checked against gsd-code-reviewer + gsd-verifier findings) |

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed
- [x] `status: verified` set in frontmatter

**Approval:** verified 2026-09-02 (hub-side scope only — T-05-05/T-05-06 remain live threats to re-check post-repin, once the human-gated tag cut + consumer repin actually executes)
