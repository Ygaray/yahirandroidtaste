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

### maven: org.osmdroid:osmdroid-android
- **Verdict:** `human-approved` (package-legitimacy CLI does not cover Maven artifacts)
- **Approved:** 2026-09-15
- **Phase:** SecondBrain v4.1 Phase 164 Plan 01 (HUBW-02, D-01)
- **Milestone:** v4.1
- **Note:** Version 6.1.20 from Maven Central, released 2024-08-18, Apache-2.0. POM declares zero
  dependencies (verified by namespace-agnostic XML parse). AAR manifest (`org.osmdroid.library`)
  declares no `uses-permission`, only four optional `uses-feature` entries
  (`android.hardware.location.network`, `android.hardware.location.gps`,
  `android.hardware.telephony`, `android.hardware.wifi`, all `required="false"`) and a
  `supports-screens` element. Upstream repository (`osmdroid/osmdroid`) is **archived** (GitHub
  API: `archived: true`, last push 2024-11-20) with no further releases or security fixes for the
  life of this dependency; that maintenance risk was explicitly accepted by the project owner at
  164-01 Task 1 (2026-09-15, "approved, maintenance risk accepted"; recorded RD-02 in
  164-CONTEXT.md), with a MapLibre GL Native swap tracked as ROADMAP.md backlog item **999.111**
  ("swap before osmdroid breaks"). AAR SHA-512
  `7ccd615f05f33238419aa2bbaef966accda56e4e04c01a555d914a029a9aeb3a564d0e6996faafa497bf93148d6cd837d8c7bebc15e1a32c57b62e674dd1e4fc`
  computed locally and matched against Maven Central's published `.sha512`. PGP signature key id
  `98AD2E19BFF4106D` (signature created 2024-08-18); the key could not be fetched from
  `hkps://keys.openpgp.org` (`gpg --recv-keys` exit 2, "No data"), so the signature is
  **unverified** — Central's SHA-512 remains the principal integrity evidence, as accepted by the
  human at the checkpoint. Added as an `implementation` dependency so no osmdroid type enters
  `api.txt`; all tile traffic is governed by the OSM tile usage policy through the hub's
  `configureOsmdroid` step (D-06, identifying User-Agent, default cache-header honoring, no bulk
  pre-fetch).
