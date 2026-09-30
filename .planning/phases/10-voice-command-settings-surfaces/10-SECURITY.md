---
phase: "10"
slug: "voice-command-settings-surfaces"
status: verified
# threats_open = count of OPEN threats at or above workflow.security_block_on severity (the blocking gate)
threats_open: 0
asvs_level: 1
# audited_head = git HEAD sha at audit time — freshness stamp. child-result re-checks it: if
# implementation (outside .planning) changed since this sha, the audit is stale (INC-2026-08-06-04).
audited_head: bca91cebf6cc6bdb378df9042e25e1d430facde0
created: "2026-09-30"
---

# Phase 10 — Security

> Per-phase security contract: threat register, accepted risks, and audit trail.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| consumer → library (props) | The API key, provider/model catalog, and ladder rungs cross from the consumer into the composables as props; the library must not retain them. | API key string, provider/model id+label lists, approach-ladder rung models |
| library → consumer (callbacks) | Provider/key/model selection and cap/offline-only edits emit back via callbacks; the library performs no side effect, persistence, or network call of its own. | selection ids, edited key string, boolean toggles |

---

## Threat Register

| Threat ID | Category | Component | Severity | Disposition | Mitigation | Status |
|-----------|----------|-----------|----------|-------------|------------|--------|
| T-10-01 | Information Disclosure | `ProviderKeyCard` key field | high | mitigate | `keyValue` is a hoisted `String` prop; no model/data-class stores the key; no `Log.*`/println call anywhere in the diff (verified: `grep -rn "Log\.\|println" component/ProviderKeyCard.kt model/*.kt` → no matches). | closed |
| T-10-02 | Information Disclosure | Masked key rendering | high | mitigate | `PasswordVisualTransformation()` masks by default; `revealed` (local `rememberSaveable` UI-only boolean, never the key itself) toggles to `VisualTransformation.None` only on explicit user tap; reveal affordance is hideable (`revealToggle` null ⇒ no eye at all, D-05). Verified in `ProviderKeyCard.kt:80-94`. | closed |
| T-10-03 | Tampering / Info Disclosure | Secret persistence in a reusable lib | high | mitigate | Library holds nothing — no `SharedPreferences`/`DataStore`/`Keystore`/network call exists anywhere in the new component/model files (verified via grep, 0 matches); persistence is the consumer's job (anti-feature, INV-01). | closed |
| T-10-04 | Tampering | Registry / vocab drift guards | medium | mitigate | `ProviderKeyCard`, `ModelSelectCard`, `ApproachLadderCard` all registered in `voiceCommandFamilyEntries`, concatenated into `ComponentRegistry.kt:103`; `ComponentRegistryDriftGuardTest`/`DomainVocabularyDriftGuardTest` both pass live (confirmed in 10-VERIFICATION.md). | closed |
| T-10-05 | Tampering (reuse-break) | `ApproachLadderCard` | high | mitigate | Card displays + emits only — no `TierPolicy`/engine/network import anywhere in `ApproachLadderCard.kt` (verified via grep of its import block: only AndroidX/Compose + local model/theme imports); `offlineCapable` is an app-derived prop (D-06). One-way-dependency invariant (INV-01) preserved. | closed |
| T-10-06 | Tampering | Registry / vocab drift guards | medium | mitigate | Both new composables registered in `voiceCommandFamilyEntries`; head tokens allowlisted (Plan 01); both guards run in the full suite (CATALOG-03) — confirmed green in 10-VERIFICATION.md. | closed |
| T-10-07 | Information Disclosure | Model/rung labels | low | accept | Labels are caller-supplied display strings; the library adds no persistence, logging, or telemetry. Accepted — below block threshold and no mitigation action needed. | closed |
| T-10-SC | Tampering | npm/pip/cargo installs (Plan 01) | low | accept | No new packages: every API resolves from the already-pinned Compose BOM 2026.04.01. | closed |
| T-10-SC | Tampering | npm/pip/cargo installs (Plan 02) | low | accept | No new packages: every API resolves from the already-pinned Compose BOM 2026.04.01. | closed |

*Status: open · closed · open — below high threshold (non-blocking)*
*Severity: critical > high > medium > low — only open threats at or above `workflow.security_block_on` (high) count toward `threats_open`*
*Disposition: mitigate (implementation required) · accept (documented risk) · transfer (third-party)*

All 9 threats (register authored at plan time in both `10-01-PLAN.md` and `10-02-PLAN.md`
`<threat_model>` blocks) verified CLOSED via direct grep-level (ASVS L1) evidence against the
implemented source — see Mitigation column per threat. `threats_open: 0`. Per the short-circuit
rule (`threats_open: 0 AND register_authored_at_plan_time: true AND asvs_level == 1`), this audit
did not require spawning `gsd-security-auditor` — L1 grep-depth verification is sufficient at ASVS
level 1.

---

## Accepted Risks Log

| Risk ID | Threat Ref | Rationale | Accepted By | Date |
|---------|------------|-----------|-------------|------|
| AR-10-01 | T-10-07 | Caller-supplied display strings (model/rung labels) carry no library-side persistence, logging, or telemetry risk; severity low, below the `high` block threshold. | orchestrator (gates-only re-drive) | 2026-09-30 |
| AR-10-02 | T-10-SC (both plans) | No new npm/pip/cargo packages introduced — all APIs resolve from the already-pinned Compose BOM 2026.04.01; supply-chain audit not applicable. | orchestrator (gates-only re-drive) | 2026-09-30 |

*Accepted risks do not resurface in future audit runs.*

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-09-30 | 9 | 9 | 0 | gates-only re-drive orchestrator (grep-level ASVS L1 verification, short-circuited per Step 3 rule — no auditor subagent spawned) |

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed
- [x] `status: verified` set in frontmatter

**Approval:** verified 2026-09-30
