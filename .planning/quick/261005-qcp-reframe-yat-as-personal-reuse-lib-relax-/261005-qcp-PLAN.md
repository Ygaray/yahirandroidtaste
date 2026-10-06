---
quick_id: 261005-qcp
mode: quick
autonomous: true
---
# Quick 261005-qcp: reframe YAT as a personal reuse library; relax the compat rule to source-only

Yahir's ruling, relayed by the orchestrator (yahir-gsd-control-plane-3b), 2026-10-05: YAT is personal. It exists so his agents don't code widgets he reuses from scratch. It has no outside consumers, every consumer recompiles on repin, and the future is a cross-platform (KMP/CMP) rewrite, not a public release. Docs-only; run inline by the orchestrating session.

1. README.md and .planning/PROJECT.md: rewrite the purpose and core value. README's install note now names v2.5.0 (was "tag not cut yet, Phase 102").
2. API.md, CONVENTIONS.md and tools/README-api-guard.md: source compatibility for Yahir's consumers (defaults on new params, trailing lambdas stay last, never append after a trailing lambda). Binary compat is NOT required: no new HIDDEN overloads and no new K synthetics; the existing v2.5.0 shims stay.
3. tools/verify-binary-abi.sh: optional and informational (header comment and docs), not a release gate. Metalava apiCheck stays per-commit.
Excluded: CLAUDE.md (the orchestrator edits it on Yahir's direct approval).
