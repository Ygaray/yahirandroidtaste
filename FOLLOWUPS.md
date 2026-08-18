# Follow-ups — yahirandroidtaste hub

Tracked items that must be addressed **in this repo** (not the SecondBrain consumer). Sourced from
SecondBrain's v1.20 Phase 100 code review (`100-REVIEW.md`), surfaced again during v1.20 milestone
certification (`/gsd-certify-milestone`, 2026-08-18) — the SB-side certify can't touch hub files, so
they land here.

Provenance: SecondBrain `.planning/phases/100-hub-bootstrap-move/100-REVIEW.md` (0 critical, 1 warning,
2 info). Status below reflects a re-check of the live hub tree on 2026-08-18 (HEAD `ff545a0`, tag `v1.0.0`).

---

## OPEN

### IN-01 — README links "SecondBrain" to a GitHub *profile*, not the repo
- **Files:** `README.md:7`, `README.md:25` (review cited `:7` + `:27`; line drifted after the LIB-03 rename — content unchanged).
- **Issue:** Both link the text `SecondBrain` to `https://github.com/Ygaray` (the user profile) instead of the SecondBrain repository. `ECOSYSTEM.md:22,33` already use the correct repo-qualified form, so this is an internal inconsistency and the links land on the wrong page.
- **Fix:** Point both to the SecondBrain repo (e.g. `https://github.com/Ygaray/SecondBrain`). Note: `ECOSYSTEM.md` describes SecondBrain as a *private working tree* — if there is no public repo URL yet, de-link the text or point it at whatever canonical location exists, rather than leaving it aimed at the bare profile.
- **Risk if skipped:** cosmetic/doc only. No code, build, or publish impact. Safe to batch into the next hub doc pass.

---

## RESOLVED / MOOT (recorded for traceability — no action needed)

### IN-02 — "compileSdk 36" vs. the 36.1 minor level — **RESOLVED**
- The review flagged `README.md` / `INTEGRATION.md` stating `compileSdk 36` while the build targets `release(36) { minorApiLevel = 1 }` (36.1). Re-check: `README.md:88` and `INTEGRATION.md:19` already read `compileSdk 36 (minor API 36.1)`. Docs are aligned. No action.

### WR-01 — JitPack remote build of `compileSdk 36.1` unproven — **MOOT**
- The review's one warning was that a green *local* `publishToMavenLocal` did not de-risk JitPack's remote toolchain provisioning platform 36.1. This is now closed by evidence: tag `v1.0.0` was cut and JitPack built + published it, and SecondBrain repinned + device-verified against the published AAR (`ECOSYSTEM.md:33`). No fallback to plain `compileSdk 36` was needed. No action; keep `36.1` as-is.

---

_Left by SecondBrain v1.20 milestone certification, 2026-08-18. Update or delete entries as addressed._
