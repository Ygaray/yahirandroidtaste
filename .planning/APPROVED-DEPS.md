# Approved Dependencies

Third-party packages vetted (package-legitimacy) and human-approved for install in this project.
A package NOT listed still escalates to a human (INC-2026-08-24-04). Normally generated/appended by
the discuss stage; entries below the standard shape may be recorded manually at execution with a
**Note:** (mirroring the human-approved-at-execution pattern).

## Entries
### gradle-plugin: me.tylerbwong.gradle.metalava
- **Verdict:** `human-approved` (package-legitimacy CLI does not cover Gradle plugins)
- **Approved:** 2026-08-27
- **Phase:** hub-additive-guards (control-plane Plan A, Task 1)
- **Milestone:** hub-guards
- **Note:** Build-time-only Gradle plugin (v0.5.0) that wraps Google/AOSP-official **Metalava** (the
  API tracker AndroidX itself uses), aliased to `apiDump`/`apiCheck` to generate + check the additive
  guard's public-API signature file (`api.txt`). It **never ships in the published `.aar`** — consumer
  supply chain is unaffected; exposure is limited to the hub's own build machine. Adopted because the
  official ABI tools do not work on this **AGP 9.2.1 / Kotlin 2.3.20 Compose** stack (Kotlin built-in
  `abiValidation` is unavailable under AGP-9 built-in Kotlin; JetBrains binary-compatibility-validator
  silently registers no tasks on `com.android.library`). Verified 2026-08-27: deterministic dump
  (two dumps byte-identical) + negative control (a removed public symbol makes `apiCheck` fail). The
  underlying tool is official; only the Gradle glue is community — revisit to drop it if JetBrains/Google
  ship AGP-9-built-in-Kotlin ABI support.
