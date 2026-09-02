---
phase: 03
slug: governance-gates
status: verified
# threats_open = count of OPEN threats at or above workflow.security_block_on severity (the blocking gate)
threats_open: 0
asvs_level: 1
# audited_head = git HEAD sha at audit time — freshness stamp. child-result re-checks it: if
# implementation (outside .planning) changed since this sha, the audit is stale (INC-2026-08-06-04).
audited_head: 02dd14d4908e848471454ae2b27f29311447611f
created: 2026-09-01
---

# Phase 03 — Security

> Per-phase security contract: threat register, accepted risks, and audit trail.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| None applicable (03-01) | `tools/verify-additive-diff.sh` and its bash fixture tests read/write only this repo's own tracked files inside throwaway `mktemp -d` directories, cleaned via `trap ... EXIT`. No network input, no untrusted external data, no auth/session/access-control surface, no crypto. | None — local git-hook tooling operating on the repo's own tracked files only. |
| None applicable (03-02) | `DomainVocabularyDriftGuardTest.kt` is a build-time JVM test that reads `.kt` source text from this repo's own `src/main` tree; `docs/DESIGN-INTENT.md` is a documentation edit. No network input, no untrusted external data, no auth/session/access-control surface, no crypto. | None — local build-time source scan + documentation. |

---

## Threat Register

| Threat ID | Category | Component | Severity | Disposition | Mitigation | Status |
|-----------|----------|-----------|----------|-------------|------------|--------|
| T-03-01 | N/A | tools/verify-additive-diff.sh, tools/test/*.sh | low | accept | No untrusted input, no external surface — local git-hook tooling operating on the repo's own tracked files only; ASVS not applicable. Documented in 03-01-PLAN.md's threat model, reconfirmed against RESEARCH.md's "## Security Domain" finding. | closed |
| T-03-02 | N/A | DomainVocabularyDriftGuardTest.kt, docs/DESIGN-INTENT.md | low | accept | Local build-time source scan + documentation; no untrusted input or external surface — ASVS not applicable. Documented in 03-02-PLAN.md's threat model, reconfirmed against RESEARCH.md's "## Security Domain" finding. | closed |

*Status: open · closed · open — below {block_on} threshold (non-blocking)*
*Severity: critical > high > medium > low — only open threats at or above workflow.security_block_on (high) count toward threats_open*
*Disposition: mitigate (implementation required) · accept (documented risk) · transfer (third-party)*

Both plans' `<threat_model>` blocks independently concluded no STRIDE category applies: neither
plan's implementation touches network input, untrusted external data, auth/session/access-control,
or cryptography — both are local git-hook tooling and a build-time source-text scan operating only
on this repo's own tracked files. `register_authored_at_plan_time: true` for both plans (each PLAN.md
carries a parseable `<threat_model>` block); `asvs_level: 1` — per the secure-phase workflow's
short-circuit rule (`threats_open: 0 AND register_authored_at_plan_time: true AND asvs_level == 1`),
this audit skips the deep-verification auditor: L1 grep-depth is sufficient, no deeper verification
required.

---

## Accepted Risks Log

| Risk ID | Threat Ref | Rationale | Accepted By | Date |
|---------|------------|-----------|-------------|------|
| T-03-01 | T-03-01 | No untrusted input, no external surface — local git-hook tooling operating on the repo's own tracked files only. | gsd-executor (03-01 plan authoring) | 2026-09-01 |
| T-03-02 | T-03-02 | Local build-time source scan + documentation; no untrusted input or external surface. | gsd-executor (03-02 plan authoring) | 2026-09-01 |

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-09-01 | 2 | 2 | 0 | gsd-secure-phase (auto mode, short-circuit L1) |

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed
- [x] `status: verified` set in frontmatter

**Approval:** verified 2026-09-01
