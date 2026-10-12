---
quick_id: 261005-qcp
status: complete
---
# Quick 261005-qcp Summary

- README: personal-library framing; consumers are Yahir's own apps; install pins `v2.5.0`.
- PROJECT.md: "What This Is" and "Core Value" rewritten around agent reuse, with a source-only compatibility bar.
- API.md: § "The binary-compatibility rule" replaced by § "Compatibility rule (personal library; source compatibility only)". The v2.5.0 binary-compat release notes earlier in the file stay as historical record.
- CONVENTIONS.md: § "Public API Evolution (source compatibility)".
- tools/README-api-guard.md and the verify-binary-abi.sh header: the check is optional and informational; exit 3 no longer means STOP.
- No code or behavior change. The script logic, the tests and the existing shims are untouched. CLAUDE.md is not touched.
- Historical planning records (15/17/19-CONTEXT, quick 261005-dmc/e2e/eyu) are left as they were written.
