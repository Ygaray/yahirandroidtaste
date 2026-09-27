---
phase: "06"
slug: "forward-port-reunification"
status: verified
threats_open: 0
asvs_level: 1
audited_head: 1aed673ed0be74ed89aec8ad38375e104e85fef3
created: "2026-09-27"
---

# Phase 06 — Security

> Per-phase security contract: threat register, accepted risks, and audit trail.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| Consumer app -> osmdroid tile server (network) | `PlaceMapPicker`'s live `MapView` (06-03) fetches OSM raster tiles over the network on the consumer's behalf; untrusted network response boundary | Raster tile imagery only — no user data sent beyond standard HTTP/User-Agent |
| Consumer app -> device filesystem (private `cacheDir`) | `PlaceMapOsmdroidConfig.configureOsmdroid` (06-03) writes the tile cache to the consumer's own private `cacheDir` | Cached raster tiles only — no domain/user data |
| Build machine -> Maven Central | `osmdroid-android` (6.1.20) + `androidx-lifecycle-runtime-compose` (2.9.4) artifacts pulled from a public registry at build/publish time (06-01) | Build-time dependency binaries |

---

## Threat Register

| Threat ID | Category | Component | Severity | Disposition | Mitigation | Status |
|-----------|----------|-----------|----------|-------------|------------|--------|
| T-06-SC | Tampering | `osmdroid-android` Maven dependency admission (`gradle/libs.versions.toml`, `build.gradle.kts`) | high | mitigate | Human-approved in `.planning/APPROVED-DEPS.md` (2026-09-26) with Maven Central currency verification (6.1.20 confirmed both `<latest>` and `<release>`). Pre-tag advisory (CVE/GHSA) re-check remains a documented Phase 9 obligation (D-02), not a Phase 6 blocker. | closed |
| T-06-01 | Denial of Service (against the upstream OSM tile server, not this app) | `PlaceMapOsmdroidConfig.configureOsmdroid` / `PlaceMapPicker`'s `MapView` | low | accept | Already mitigated by the restored v1.13.0 design: `configureOsmdroid` sets a proper identifying User-Agent, calls no bulk-download/cache-manager API, and leaves cache-expiry-header honoring at osmdroid's default. Restored verbatim, no new mitigation authored by this phase. | closed |
| T-06-02 | Information Disclosure (storage-permission overreach) | `PlaceMapOsmdroidConfig`'s tile cache path (`File(appContext.cacheDir, "osmdroid")`) | low | accept | Tiles cache to the consumer's own private `cacheDir` only, never external/shared storage — no storage permission required. Consumer must still declare `INTERNET` itself (documented cross-tier responsibility split, D-02 / ECOSYSTEM.md). This hub-side restore introduces no new permission request. | closed |
| T-06-04 | (informational — no applicable STRIDE category) | `PresetChip.kt` | low | accept | Pure Compose chip taking `label`/`onClick`/`supportingLabel`/`enabled`/`isSelected`/`contentDescription` — no I/O, no storage, no network. No new attack surface. | closed |

*Status: open · closed · open — below high threshold (non-blocking)*
*Severity: critical > high > medium > low — only open threats at or above `workflow.security_block_on` (high) count toward `threats_open`*
*Disposition: mitigate (implementation required) · accept (documented risk) · transfer (third-party)*

All four threats were authored at plan time (`register_authored_at_plan_time: true` for all three
plans in this phase) and disposed as `mitigate` (with pre-existing evidence) or `accept` (with
documented low-severity rationale). No SUMMARY.md in this phase raised new `## Threat Flags`
entries during execution, so the plan-time register is unchanged post-implementation. This phase
restores already-reviewed `v1.13.0` code — it introduces no new attack surface beyond the
already-approved osmdroid dependency admission.

---

## Accepted Risks Log

| Risk ID | Threat Ref | Rationale | Accepted By | Date |
|---------|------------|-----------|-------------|------|
| AR-06-01 | T-06-01 | Upstream OSM tile server DoS risk is out of this app's control surface; existing `configureOsmdroid` User-Agent + no-bulk-download design is the standard osmdroid-recommended mitigation. | Plan 06-01/06-03 threat model (documented at plan time) | 2026-09-26 |
| AR-06-02 | T-06-02 | Tile cache confined to private `cacheDir`; no storage permission requested; matches documented cross-tier `INTERNET`-permission responsibility split (D-02 / ECOSYSTEM.md). | Plan 06-01/06-03 threat model (documented at plan time) | 2026-09-26 |
| AR-06-04 | T-06-04 | `PresetChip` is a pure label/callback UI chip with no I/O, storage, or network surface — no applicable STRIDE category. | Plan 06-02 threat model (documented at plan time) | 2026-09-26 |

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-09-27 | 4 | 4 | 0 | execute-phase secure_phase_gate (short-circuit: threats_open=0, register_authored_at_plan_time=true, asvs_level=1 — L1 grep-depth sufficient, auditor spawn skipped per secure-phase.md Step 3) |

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed
- [x] `status: verified` set in frontmatter

**Approval:** verified 2026-09-27
