---
phase: "07"
slug: "chip-color-slot"
status: verified
threats_open: 0
asvs_level: 1
audited_head: 91e252107be579470aacbe3a58a6576c111f8e26
created: "2026-09-27"
---

# Phase 07 — Security

> Per-phase security contract: threat register, accepted risks, and audit trail.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| Consumer app -> `TagChipUiModel.color` / `AppChip.containerColorOverride` | Consumer supplies an unvalidated `androidx.compose.ui.graphics.Color?` value; the hub renders it as-is (no contrast/muting computation, D-01) | A single `Color` value (an inline class over one `ULong`) — no string/URI/parse surface, no I/O |

No other trust boundary is introduced by this phase: the feature is a same-process Compose
parameter thread (`TagChipUiModel.color` -> `AppChip`/`TagChipWithContextMenu.containerColorOverride`
-> `CardTagRow` auto-thread), with no network, storage, logging, or serialization anywhere in the
touched files.

---

## Threat Register

| Threat ID | Category | Component | Severity | Disposition | Mitigation | Status |
|-----------|----------|-----------|----------|-------------|------------|--------|
| T-07-01 | Tampering | `AppChip.containerColorOverride` / `CardTagRow` auto-thread | low | accept | `Color` is an inline class over a single `ULong` — no parse/injection surface. Established precedent: `CardBase.kt:140,255` already renders an unvalidated caller-supplied `accent: Color?` the same way. New `containerColor` when-arm (`AppChip.kt:108`) sits strictly below `isSelected`/`relatedness` (lines 106-107), matching the documented precedence contract. | closed |
| T-07-02 | Information Disclosure | `TagChipUiModel.color`, `AppChip.kt`, `TagChipWithContextMenu.kt`, `CardTagRow.kt` | low | accept | Grep for `Log.`/`persist`/`Serializable`/`DataStore`/`Room`/`Retrofit`/`File(` across all four touched files returns zero matches — the color value is read once per recomposition and drawn, never logged/stored/transmitted. | closed |
| T-07-03 | Denial of Service | `AppChip`'s new `when`-arm / `CardTagRow`'s `tag.color` thread | low | accept | Same fixed-size inline-class fact as T-07-01; no allocation/recursion introduced by the new arm or the two new thread sites (`CardTagRow.kt:109,119`). | closed |
| T-07-04 | Spoofing/Repudiation/Elevation of Privilege | N/A | n/a | n/a | No identity, session, or privilege construct anywhere in the call chain — same-process Compose parameter passing only. | closed |
| T-07-SC | Tampering (supply chain) | Phase 07 commit set | n/a | n/a | `git show --stat` on all 7 Phase-07 commits (`8d692e5`, `f70acd0`, `93df13d`, `59e08b9`, `000bf89`, `7f57492`, `91e2521`) confirms zero touches to `build.gradle.kts`/`settings.gradle.kts` and no new import beyond the AndroidX/Compose types already in use. | closed |

*Status: open · closed · open — below high threshold (non-blocking)*
*Severity: critical > high > medium > low — only open threats at or above `workflow.security_block_on` (high) count toward `threats_open`*
*Disposition: mitigate (implementation required) · accept (documented risk) · transfer (third-party)*

**threats_open: 0**

---

## Unregistered Flags (surfaced during this audit, not in the original plan-time register)

**WARNING — UF-07-01 (low severity, non-blocking):** The 2026-09-27 ABI-remediation deviation
(commit `7f57492`, closing the WR-01 `copy()` ABI break per the operator's ruling) re-shaped
`TagChipUiModel.color` from the plan's originally-stated immutable primary-constructor `val` into a
**mutable body `var`**, declared outside the primary constructor and deliberately excluded from
`equals()`/`hashCode()`/`copy()` (`TagChipUiModel.kt`, `var color: Color? = null`). This is new
attack-adjacent surface the plan-time STRIDE register (written against the original `val` shape)
never re-assessed:
- Mutating `model.color` mutates every holder of that same object reference — no per-consumer
  isolation, unlike a `copy(color = ...)` (which the type no longer supports carrying, by design).
- Two `TagChipUiModel` instances differing only in `color` remain `equals()` to each other (styling
  metadata deliberately excluded from identity) — a latent correctness/UI-integrity footgun if a
  future consumer ever relies on `equals()` for change detection or list-diffing.
- The mutation does not participate in Compose's snapshot state system (plain `var`, not
  `mutableStateOf`), so a shared cached instance mutated off the composition thread carries the
  same plain-mutable-Kotlin-property data-race risk as any other non-thread-confined `var` — not a
  regression introduced specifically by this phase's design, but newly present on this model.

Assessed severity: **low** — no trust/auth boundary is crossed (same-process UI library, single
user, no secrets/session context), so this does not block ship at `security_block_on: high`. Filed
here as an explicit addendum to T-07-01 rather than left implicit in the KDoc, since the KDoc
documents the ABI *consequence* (color excluded from equals/copy) but not the *security-adjacent*
shared-mutable-reference angle. No action required this phase; worth a one-line callout if a future
phase gives `TagChipUiModel` instances a longer/shared lifetime (e.g. caching across
recompositions) where this mutation-visibility property would matter more.

---

## Accepted Risks Log

| Risk ID | Threat Ref | Rationale | Accepted By | Date |
|---------|------------|-----------|-------------|------|
| AR-07-01 | T-07-01 | `Color` is a fixed-size inline value class with no parse/injection surface; matches the established `CardBase.accent` unvalidated-render-as-is precedent. | Phase 07 security audit (gsd-security-auditor) | 2026-09-27 |
| AR-07-02 | T-07-02 | No logging/persistence/serialization/network touches the color value anywhere in the four changed files (confirmed by grep). | Phase 07 security audit (gsd-security-auditor) | 2026-09-27 |
| AR-07-03 | T-07-03 | Fixed-size value, no new allocation/recursion in the new when-arm or thread sites. | Phase 07 security audit (gsd-security-auditor) | 2026-09-27 |
| AR-07-04 | UF-07-01 | Mutable-body-`var` shape (from the WR-01 ABI fix) has no trust-boundary crossing; shared-reference-mutation and equals-exclusion risks are real but low-severity for this library's current single-process, no-caching usage. Revisit if a future phase gives instances a longer/shared lifetime. | Phase 07 security audit (gsd-security-auditor) | 2026-09-27 |

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-09-27 | 5 | 5 | 0 | gsd-security-auditor, dispatched by the Phase 07 execute-stage orchestrator during tail-gate resumption after the WR-01 ABI-break code fix (commits `7f57492`, `91e2521`). Independently re-verified the fix's additivity claim against the true pre-Phase-7 `api.txt` baseline (`000bf89~1`) rather than trusting the deviation log narrative. |

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed
- [x] `status: verified` set in frontmatter

**Approval:** verified 2026-09-27
