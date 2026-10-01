---
phase: "14"
slug: "cut-v2-4-0"
status: verified
# threats_open = count of OPEN threats at or above workflow.security_block_on severity (the blocking gate)
threats_open: 0
asvs_level: 1
# audited_head = git HEAD sha at audit time — freshness stamp. child-result re-checks it: if
# implementation (outside .planning) changed since this sha, the audit is stale (INC-2026-08-06-04).
audited_head: 2465670ca3d01fbbf0f0d2aa6b3d61f25c3bbfb0
created: "2026-10-01"
---

# Phase 14 — Security

> Per-phase security contract: threat register, accepted risks, and audit trail.

Short-circuit path (per `secure-phase.md` Step 3): `threats_open: 0`,
`register_authored_at_plan_time: true` (PLAN.md's `<threat_model>` block was fully populated at
plan time), `asvs_level == 1` — L1 grep-depth classification against the fresh evidence in
`14-SHIP-GATE-EVIDENCE.md` and `14-01-SUMMARY.md` is sufficient; no auditor subagent spawn
required.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| Local build -> git remote (`origin`/GitHub) | Pushing a tag publishes it publicly and immutably; once pushed it is externally observable and cannot be un-pushed, only superseded by a new patch tag | Git tag object + ref (no secrets) |
| This repo -> JitPack (public CDN/build service) | JitPack pulls from GitHub and builds remotely under its own toolchain; this repo only observes JitPack's build status via public HTTP GET | Public HTTP GET (`.pom`/`.aar`/builds-API JSON) |
| This repo's session -> cross-repo orchestrator session | The only write path to the shared section-11 ledger is via messaging the orchestrator; this repo never edits `CROSS-REPO-SCOPE-CONTRACT.md` directly (A14) | Ledger row content (repo/tag/commit/coordinate/summary — no secrets) |

---

## Threat Register

| Threat ID | Category | Component | Severity | Disposition | Mitigation | Status |
|-----------|----------|-----------|----------|-------------|------------|--------|
| T-14-01 | Tampering / Repudiation | `api.txt` / Metalava `apiCheck` | high | mitigate | v2.3.0-swap-baseline `./gradlew apiCheck` BUILD SUCCESSFUL (14-SHIP-GATE-EVIDENCE.md Step 2b) — public API confirmed additive vs `v2.3.0`, `api.txt` restored clean afterward | closed |
| T-14-02 | Tampering (release-coordinate namespace) | `.planning/config.json` / `git tag` | high | mitigate | `git.create_tag: false` re-verified immediately before tagging (Pre-Tag Guard Check) and again at Task 3 closing; `git tag -l 'v2.4*'` confirmed exactly one tag (`v2.4.0`) at phase close | closed |
| T-14-03 | Repudiation (false completion claim) | JitPack remote build / `14-SHIP-LEDGER-ROW.md` | medium | mitigate | Live `curl` triple against `jitpack.io` with retry-with-backoff (14-SHIP-GATE-EVIDENCE.md Step 4) — `.pom`/`.aar` HTTP 200, builds-API `status:"ok"`, `isTag:true`, `commit` equal to the exact tagged SHA; a local `publishReleasePublicationToMavenLocal` run was explicitly never accepted as a substitute | closed |

*Status: open · closed · open — below {block_on} threshold (non-blocking)*
*Severity: critical > high > medium > low — only open threats at or above workflow.security_block_on (`high`) count toward threats_open*
*Disposition: mitigate (implementation required) · accept (documented risk) · transfer (third-party)*

---

## Accepted Risks Log

| Risk ID | Threat Ref | Rationale | Accepted By | Date |
|---------|------------|-----------|-------------|------|

No accepted risks.

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-10-01 | 3 | 3 | 0 | gsd-milestone-phase-orchestrator (L1 short-circuit, no auditor spawn per asvs_level==1) |

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed
- [x] `status: verified` set in frontmatter

**Approval:** verified 2026-10-01
