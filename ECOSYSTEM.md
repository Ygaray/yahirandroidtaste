# ECOSYSTEM.md — the yahirandroidtaste hub & its consumers (read this before working across repos)

> **Audience: coding agents (and humans) working in `yahirandroidtaste` — this hub — or in any
> Android app that consumes it.** This is the constitution for how the repos relate, who has
> authority over what, and where new components go. When it conflicts with a single repo's stale
> note, this document wins — flag the drift. The authoritative pin is always each consumer's
> manifest + Gradle resolution; this doc is a best-effort cache.
>
> This hub uses **Mechanism B (Android / Gradle / JitPack)**. The ecosystem-wide repin procedure
> lives in the machine-config layer at `~/.claude/context/workflows/repin.md` (§ Mechanism B) and
> the cross-dependency map at `~/.claude/context/deps/_index.md`.

---

## 1. The shape — hub + spokes

This is **not one project.** It is a shared Android **design-system library** module plus N
independent apps that consume it:

- **The hub — `yahirandroidtaste`** (this repo; JitPack `com.github.Ygaray:yahirandroidtaste`;
  import root `io.github.ygaray.yahirandroidtaste`; public at
  `github.com/Ygaray/yahirandroidtaste`). A curated Compose UI component catalog: seven component
  families (cards, chips, sheets, buttons/FAB, pickers, feedback, empty-state), the theme tokens
  they read, the interaction conventions they enforce (reveal-confirm swipe, standardized snackbar
  feedback, conditional-render-no-dead-space), and a self-launching `ExplorerActivity` gallery.
  **Domain-agnostic UI mechanism** — it names no note, no card *content*, no module; a consumer
  passes its own data and callbacks into each composable.
- **The consumers (spokes)** — separate repos/GSD projects, each pinning the hub at an immutable
  JitPack tag:

  | Consumer | Repo | Dev checkout | Pins hub at | Pin file |
  |----------|------|--------------|-------------|----------|
  | SecondBrain | `github.com/Ygaray/…` (private working tree) | `~/Projects/SecondBrain` | **`v1.4.0`** (repinned, v1.23 Phase 118; LIST-04 `ListCardBottomSheet` read-only preview) — two tags behind latest (`v1.6.0`); repin is SecondBrain Phase 124's job | `gradle/libs.versions.toml` |
  | CalTracker | `github.com/Ygaray/…` | `~/Projects/CalTracker_Android` | **`v1.5.0`** (repinned + Gate-1-confirmed, Phase 48 / REL-01) — the hub's Phase-44 additive-growth tag CalTracker was authorized to consume (hub's own latest tag has since moved to `v1.6.0` via an unrelated concurrent SecondBrain session — not a v1.7 CalTracker task) | `gradle/libs.versions.toml` |

  _(Best-effort cache — keep it current: a new consumer adds a row; a repin updates "Pins hub at".
  The authoritative pin is each consumer's manifest + `./gradlew :app:dependencies` resolution.
  No "Deploy host" column — Android apps are installed on devices, not daemon-deployed.)_

**Current published tag:** **`v1.6.0`** — cut in **SecondBrain v2.0 Phase 123 Plan 05**, an
autonomous minor bump shipping the Tactile design-system foundation (elevation scale, Space
Grotesk display ramp, gradient/tint accent surfaces, Heat relatedness ramp — `DS-01`), on top of
the hub's own already-published **`v1.5.0`** (cut by the hub's internal Phase 44 additive-growth
track — see the version-numbering note below). **SecondBrain pins `v1.4.0`** — two tags behind
latest; the repin plus the four-registry reconcile is **SecondBrain Phase 124's** job, not this
phase's; **CalTracker pins `v1.5.0`** (repinned Phase 48, REL-01) — the hub's own additive-growth
tag it was authorized to consume (the hub's own latest tag has since moved to `v1.6.0` via
SecondBrain's unrelated Phase 123, not a v1.7 CalTracker task; a hub change is inert until a
consumer repins). `v1.0.0` was the first immutable tag, cut human-gated in **Phase 102**
(LIB-06) on hub commit `4584b60` (JitPack BUILD SUCCESSFUL, `.aar`/`.sources`/`.pom` HTTP 200).
SecondBrain repinned onto it in **Phase 103** (REPIN-01/02) and it is **device-verified** on the
SM-S908U (Gate-1 all_pass: cold-start Hilt-across-AAR resolution, live undo + ExplorerActivity,
move-only pixel parity). SecondBrain subsequently **repinned to `v1.1.3`** across **v1.21
Phases 109→113** (REPIN-02): the `v1.1.1`→`v1.1.2`→`v1.1.3` line was cut over the Phase 112→113
remediation cycle (EDIT consolidation, album footer button, Voice rename dirty gate) and the repin
was **Gate-1 device-verified** (SC4 re-verify all_pass, SM-S908U / API 35). SecondBrain then
**repinned to `v1.3.0`** in **v1.22 Phase 115** (REPIN-04/REPIN-05), Gate-1 device-verified on the
SM-S908U (EDIT-04 bottom-sheet Rename routing, ICON-01 icon-picker live search, TAG-03 double-tap
tag removal — all 5 in-scope criteria PASS). *(SB's in-repo `yahirandroidtaste/` module-directory deletion is deferred —
an SB-internal cleanup — but SB consumes this published AAR regardless.)*

CalTracker cut and repinned onto **`v1.2.0`** in **Phase 43** (GIVE-04), human-gated on hub commit
`9aea3d3b27969c99b7e7c5c32baacbf0ba8fee7f` (hub `main` tip, built on the Phase-42 give-leg content at
`449c4b18fc1d1fce4572264388c967a8d48f86b1`; JitPack BUILD SUCCESSFUL, `.aar`/`.pom` HTTP 200 at the
v-prefixed coordinate `com.github.Ygaray:yahirandroidtaste:v1.2.0`). This tag carries CalTracker's
own 4 upstreamed give-leg components — **MetricBar** (new 8th "Progress / Metrics" `ComponentRegistry`
family), **RevealActionRow** (additive swipe-reveal sibling, `SwipeableActionRow` kept
byte-unchanged), **SegmentedOptionSelector**, and **AttentionCue** — plus the Compose BOM
`2026.04.01` alignment. The repin is **Gate-1 self-UAT confirmed** (`43-02-SELF-UAT.md`, `gsd-api35`
emulator): live (non-cached) `--refresh-dependencies` resolution confirm, zero crashes across a full
navigation smoke pass, and the `RevealActionRow`/`SwipeRevealRow` fold spot-checked live on-device.

CalTracker cut and repinned onto **`v1.5.0`** in **Phase 48** (REL-01/REL-02), human-gated on hub
commit `759179b7369d0159613c1fd1a670052a676356bf` (Phase 44's additive-growth tip; JitPack `BUILD
SUCCESSFUL`, `.aar`/`.pom` HTTP 200 at the v-prefixed coordinate
`com.github.Ygaray:yahirandroidtaste:v1.5.0`). Unlike Phase 43's give-leg upstream (CalTracker
components landing IN the hub), this tag carries the hub's own Phase-44 additive
expressive-token/primitive growth — the `LocalExpressive`/`ExpressiveMotion` token set,
`ProgressRing`, `AnimatedStatValue`, `HeroStatCard`, `verify-additive-surface.sh`, and an API.md
parity fix — a hub-first co-evolution this milestone, not an upstreamed CalTracker contribution.
The repin is **Gate-1 self-UAT confirmed** (`48-02-SELF-UAT.md`, physical SM-S908U rig +
`gsd-api35` emulator): `ContrastGuardrailTest` 12/12, Macrobenchmark frame-timing within budget on
the physical rig, real rotate/kill/predictive-back-mid-animation all clean, and reduced-motion
instant end-state clean — no regression versus Phase 47's baseline.

`v1.3.0` was cut in **SecondBrain Phase 114** (`EDIT-04`, `ICON-01`, `TAG-03`), an autonomous
minor bump — the owner's tag-cut checkpoint is waived for this personal-use hub ecosystem
(2026-08-20, `[[personal-app-tag-cut-gate-waived]]`, Option C) — on hub commit
`ea969a3b577eb611d5d1594d0f7d5ccc304e5742` (Plan 04's recorded green tip: full suite, CATALOG-03
drift guard non-vacuously, states-matrix, detekt zero-baseline, build, and a
JitPack-equivalent local publish all green). The tag carries three additive edits, none of them
breaking: **`TextCardBottomSheet`/`ListCardBottomSheet`** gain a trailing nullable defaulted
`onEditRequest: (() -> Unit)? = null` hook routing the three-dot menu's Edit row to the host's
shared tag-inclusive sheet when bound (`EDIT-04`); **`IconPickerGrid`** gains an internal live
name-substring search with an empty-state, public signature unchanged (`ICON-01`); and
**`TagChipEditorContent`**'s applied-tag chips gain double-tap removal through the existing
undo-backed `onRemoveTag` callback (`TAG-03`). Verified resolvable via a real (non-cached-local)
JitPack build (`com.github.Ygaray:yahirandroidtaste:v1.3.0`) — see `114-05-SUMMARY.md` for the
full evidence. **First consumer repin (SecondBrain) landed in SecondBrain v1.22 Phase 115**
(REPIN-04/REPIN-05), Gate-1 device-verified on the SM-S908U — EDIT-04 bottom-sheet Rename routing
to the shared tag-inclusive editor, ICON-01 icon-picker live search, and TAG-03 double-tap tag
removal all confirmed on-device, all 5 in-scope Gate-1 criteria PASS (`115-02-SELF-UAT.md`).

`v1.4.0` was cut in **SecondBrain Phase 118 Plan 01** (`LIST-04`), an autonomous minor bump — the
owner's tag-cut checkpoint is waived for this personal-use hub ecosystem
(2026-08-20, `[[personal-app-tag-cut-gate-waived]]`, Option C) — on hub commit `0af7e4e` (green
`testDebugUnitTest detekt`, 0 code smells). The tag carries two additive, defaulted trailing
params on **`ListCardBottomSheet`**: `readOnlyPreview: Boolean = false` swaps the live tappable
CHECKBOX `Checkbox` for a static, non-interactive check `Icon` (the item-toggle callback is never
wired into that branch), and `previewOverflowCount: Int = 0` renders a "+N more" hint when the
caller has truncated the list — neither param is wired by any existing call site, so every
existing consumer keeps compiling and behaving unchanged. Verified resolvable via a real
(non-cached-local) JitPack build (`com.github.Ygaray:yahirandroidtaste:v1.4.0`) — see
`118-01-SUMMARY.md` for the full evidence. **SecondBrain repinned in the same plan**
(`gradle/libs.versions.toml` → `v1.4.0`, `CardListSection.kt`'s List sheet call site wired to
`readOnlyPreview = true` / a 3-item prefix / the computed overflow count) — device confirmation is
Gate-1's job (`verify_work_agentic_gate`), not this plan's; not yet recorded here.

`v1.5.0` was cut by this hub repo's **own internal GSD project** (Phase 44, "additive growth" —
plan-task tags `44-01`/`44-02`/`44-03` in the commit log), entirely independent of and prior to
SecondBrain's Phase 123, on hub commit `759179b7369d0159613c1fd1a670052a676356bf` (annotated tag,
already pushed to `origin` before this record was written). It carries the `ExpressiveTokens`/
`ExpressiveMotion` expansion, `ProgressRing`, `AnimatedStatValue`, `HeroStatCard`, the
`tools/verify-additive-surface.sh` DS-04 guard, and an `API.md` doc-parity count fix — see the tag
message (`git show v1.5.0 --no-patch`) for its own evidence. This record is added retroactively
by SecondBrain Phase 123 Plan 05, which discovered the tag already existed (see the `v1.6.0`
version-numbering note directly below) — no consumer repin onto `v1.5.0` alone is recorded here.

`v1.6.0` was cut in **SecondBrain v2.0 Phase 123 Plan 05** (`DS-01`), an autonomous minor bump —
the owner's tag-cut checkpoint is waived for this personal-use hub ecosystem (2026-08-20,
`[[personal-app-tag-cut-gate-waived]]`, Option C) — on this doc-update commit itself (its exact SHA
is necessarily self-referential at write time; recorded verbatim in the Task 3 trailing amendment,
in `123-05-SUMMARY.md`, and in `123-TAG-CUT-RECORD.md`) — the code Task 1 verified green was hub
commit `fe20ce863ad03f705a07cef36309443721388835`, and this commit re-runs that same green gate
after only additive documentation/build-config edits (no source changes) (green-gate evidence:
full `testDebugUnitTest`, `detekt` at zero baseline (`config/detekt-baseline.xml` still 5 lines),
`build`, both
`ComponentRegistryDriftGuardTest`/`ComponentStatesMatrixTest` registry gates proven non-vacuous via
an `--info --rerun-tasks` re-run, and both `DS-04`/`DS-05` additive guards passing against `v1.4.0`
and against the phase's own pre-phase HEAD `759179b7369d0159613c1fd1a670052a676356bf`). The tag
carries four wholly additive Tactile primitive families, none modifying any existing public
token/component/signature: `Dimens.Elevation` (a six-level `Level0`..`Level5` dp shadow scale) +
`ElevationLadder`; `TactileType`/`SpaceGroteskFamily` (a four-tier Space Grotesk display-type ramp,
additive sibling to the untouched Material3 `Typography`) + `TactileTypeShowcase`;
`accentGradientStops`/`accentGradient`/`accentTint` (parametrized accent-surface color helpers,
`contrastingForeground` untouched) + `GradientSwatch`; and
`HeatTier`/`HeatVisual`/`heatTier`/`heatVisual`/`hubNodeVisual` (an independent Heat relatedness
ramp beside the untouched Jaccard `RelatednessTier` ramp) + `HeatSwatch` — all four registered as
the ninth "Tactile Foundation" `ComponentRegistry` family (51 registered composables, up from 47).
This is also the module's first-ever `res/` directory, bundling the canonical Space Grotesk
variable font (`src/main/res/font/space_grotesk_variable.ttf`, SHA-256 `acad6de1...9f72`) with its
verbatim OFL 1.1 license (`licenses/SpaceGrotesk-OFL.txt`).

**Version-numbering deviation (`v1.5.0` → `v1.6.0`):** the 123-05-PLAN.md text specified cutting
`v1.5.0` (assuming it was the next available tag after `v1.4.0`, and describing the
`v1.4.0..HEAD` ancestor commits as "unreleased"). Task 3 discovered `v1.5.0` was **already cut and
already pushed to origin** by this hub's own Phase 44 track, at the exact commit the plan called
"pre-phase HEAD" — so those ancestor commits are, in fact, already released. `v1.5.0` is
immutable and cannot be re-cut or moved (git itself refuses `git tag -a v1.5.0` a second time, and
doing so by force would violate this repo's own tag-immutability invariant). Phase 123's release
is cut as `v1.6.0` instead — the next mechanically-correct minor version — with every version
string in this plan's artifacts (`build.gradle.kts`, this record, the tag itself, the JitPack
resolution URLs) substituted accordingly. See `123-05-SUMMARY.md` for the full deviation writeup.

**JitPack resolution evidence (Task 3, confirmed post-tag):** both
`https://jitpack.io/com/github/Ygaray/yahirandroidtaste/v1.6.0/yahirandroidtaste-v1.6.0.pom` and
`.../yahirandroidtaste-v1.6.0.aar` returned HTTP `200` (AAR `Content-Length: 1426287` bytes) on
the first-ever request for this tag (a real, non-cached lazy JitPack build, not a
`publishToMavenLocal` substitute). `https://jitpack.io/api/builds/com.github.Ygaray/yahirandroidtaste/v1.6.0`
reports `"status":"ok"`, `"commit":"3e2ecbf1616b2adefd2de88f29272154505de39c"` (the exact release
SHA) and `"isTag":true`. The build log
(`https://jitpack.io/com/github/Ygaray/yahirandroidtaste/v1.6.0/build.log`) reports
`BUILD SUCCESSFUL in 2m 18s`, `Build tool exit code: 0`, and
`Found artifact: com.github.Ygaray:yahirandroidtaste:1.6.0`. Full evidence captured in
`123-TAG-CUT-RECORD.md` and `123-05-SUMMARY.md`. **SecondBrain has NOT repinned** — it still pins
`v1.4.0`; the repin, the `gradle/libs.versions.toml` bump, and the four-registry reconcile are
**SecondBrain Phase 124's** job.

**The load-bearing invariant (one-way dependency):** consumers import the hub; **no hub file ever
imports a consumer.** Everything app-specific — the data a card renders, the callbacks a sheet
fires, the module a chip filters — is *passed in* at the call site. A hub change is **inert** until
each consumer cuts a repin (new tag → bump the pin → re-resolve → rebuild → reinstall/re-verify).
This is the #1 thing that surprises people (and agents).

---

## 2. How a consumer actually consumes the hub (and why it matters for bug-fixing)

A consumer does **not** run the hub *source*. It runs the **pinned, resolved artifact**: the
`implementation("com.github.Ygaray:yahirandroidtaste:vX.Y.Z")` coordinate names an immutable JitPack
tag, which JitPack lazily builds from that tagged commit on first request. So "the hub" a running
consumer sees is an AAR built from a frozen commit — not your local `../Reusable/yahirandroidtaste`
working tree.

**This is the single most important thing to understand before you edit the hub from a consumer.**
If you fix a bug in `../Reusable/yahirandroidtaste` source and re-run the consumer's build, it
**still uses the old pinned AAR** — not your edit.

Two mechanisms bridge that gap:

- **Dev-time — the `mavenLocal()` overlay (Mechanism B's editable equivalent).** Publish the hub
  locally and let the consumer resolve it first:
  ```bash
  # from this hub repo (root IS the module — no module prefix, D-01):
  ./gradlew publishReleasePublicationToMavenLocal      # publishes local coordinate 1.0.0
  # in the consumer's settings.gradle.kts dependencyResolutionManagement:
  #   repositories { mavenLocal(); google(); mavenCentral(); maven { url = uri("https://jitpack.io") } }
  ```
  Use a **distinct local version string** (the local publish writes `1.0.0`, no `v` prefix) so it
  can't collide with an immutable JitPack tag (`vX.Y.Z`). This is **uncommitted, dev-only**. Revert
  by removing `mavenLocal()` and repinning the JitPack tag.
- **Confirm the repin actually landed.** Editing the version string proves nothing — Gradle/JitPack
  caching can silently resolve stale bytes:
  ```bash
  ./gradlew --refresh-dependencies :app:dependencies | grep yahirandroidtaste   # must print the NEW tag
  ```
- **Ship-time — the repin ritual.** To make a hub change *real* in a consumer, see §7. That step is
  **human-gated** (§3).

---

## 3. Cross-repo jurisdiction — you may fix the hub, but shipping it is gated

**You have standing authority to fix hub bugs from a consumer.** If a UI bug you hit in a consumer
actually lives in this shared library, the correct fix is *upstream in the hub*, not a consumer-side
work-around. Do not paper over a hub bug.

But respect the blast radius: **a hub change ripples to every consumer, present and future.**

- ✅ **Autonomous:** read/edit the hub source, add/adjust its tests, run the hub's own JVM /
  Robolectric / Compose-UI-test suite and detekt, and verify the fix live in a consumer via the
  `mavenLocal()` overlay (§2). Fix it, prove it, keep the suite green and detekt at its zero
  baseline.
- 🛑 **Surface and confirm before doing:** the step that changes *what a shipped app runs* — cutting
  a new immutable JitPack tag, repinning a consumer's coordinate, and reinstalling / device-
  re-verifying. Do everything up to that point, then stop and present the tag-cut + repin for human
  confirmation. This is the standing invariant in `~/.claude/context/workflows/repin.md` (human
  gates the tag cut).

---

## 4. Where new code goes — the tiers

| Tier | What it is | Where it lives |
|------|------------|----------------|
| **Generic component archetypes / theme tokens / interaction conventions** | A reusable visual component or design token any app could render (`AppChip`, `ConfirmationDialog`, `EmptyState`, `YahirAndroidTasteTheme`, the reveal-confirm swipe convention) | **The hub.** |
| **New concrete component built from existing primitives** | A composable that composes the hub's own building blocks and names no domain concept | **The hub** (register it in `ComponentRegistry` — the drift guard requires every public composable be registered XOR allowlisted). |
| **Consumer-specific screens / navigation / domain data** | Which module a screen shows, the Room data a card renders, the app's nav graph and ViewModels | **The consumer, forever.** Never promotes. |

## 5. The litmus — the one test that decides tier

> **"Could a *different* app render this with zero domain assumptions?"**

- **Yes** → it belongs in the **hub**. It must name no domain noun (no `note`, `module`, `voice
  card` *content* semantics) — it takes data + callbacks as parameters and renders.
- **No** → it stays in the **consumer**.

`yahirandroidtaste` is generic by construction — its components take content and callbacks as
parameters, hold no Room dependency, and name zero domain concepts. **The CATALOG drift guard**
(`ComponentRegistry` + its integrity test) enforces that every public composable is either
registered in the seven-family catalog or explicitly allowlisted — so a new hub component cannot be
added silently.

---

## 6. Extraction history — this hub was carved OUT of a consumer (consumer-first origin)

Unlike a hub-first library, `yahirandroidtaste` was **extracted from SecondBrain**: SecondBrain's
in-app `:yahirandroidtaste` design-system package (built up across SecondBrain Phases ~55–95: the
component families, `ComponentRegistry`, the explorer gallery, the reveal-confirm / snackbar /
conditional-render conventions) was lifted into this standalone repo (milestone v1.20, LIB-01/02 —
Phase 100). SecondBrain then repins to consume it as the published JitPack artifact (Phase 103),
keeping only its own screens, navigation, and Room domain in-app.

*(There is no `_promotable/` quarantine convention in these Android repos — the reusable code
originated by extraction, not by consumer-side incubation. This section records the actual history
rather than importing an unused pattern.)*

---

## 7. Versioning & the repin ritual (shipping a hub change)

- The hub uses **semver immutable JitPack tags** (`v1.0.0`, …). **Never `-SNAPSHOT`** (mutable +
  Gradle's ~24 h changing-module cache = stale-bytes false pass). A re-cut is a new version number.
- Removing/renaming a public composable, or changing a component's required parameters, is a
  **breaking change** → major-ish bump, human-gated. (The package rename in Phase 101 is itself such
  a break — every consumer import path changes.)
- Each consumer pins **one tag** and repins on its own cadence; one hub release does not force every
  consumer to move at once.
- The full, ordered ritual is **`~/.claude/context/workflows/repin.md` (§ Mechanism B)** — edit the
  coordinate → `--refresh-dependencies` + resolve-confirm → rebuild → run the suite → reinstall +
  device re-verify → update the four registries. It ends at a human-gated tag cut.

---

## 8. Onboarding a new consumer — the manual path (no scaffolder yet)

There is no `new_consumer` scaffolder for these Android hubs. To wire a new consumer by hand:

1. Add the JitPack repo in `settings.gradle.kts` `dependencyResolutionManagement.repositories`:
   `maven { url = uri("https://jitpack.io") }` (project-level repos are forbidden under
   `FAIL_ON_PROJECT_REPOS`).
2. Pin the immutable tag: `implementation("com.github.Ygaray:yahirandroidtaste:vX.Y.Z")` (or a
   `libs.versions.toml` catalog entry). Never `-SNAPSHOT`.
3. `./gradlew --refresh-dependencies :app:dependencies | grep yahirandroidtaste` — confirm it resolved.
4. Ensure the two consumer prerequisites hold: a Hilt-enabled `Application` (`@HiltAndroidApp`) so
   the library's `@Singleton` bindings aggregate into your `SingletonComponent`, and a Compose BOM
   aligned with the library's (2026.04.01). See this repo's `README.md`, `API.md`, and
   `INTEGRATION.md`.
5. Register the new consumer in §1 above **and** in `~/.claude/context/deps/_index.md` +
   `deps/yahirandroidtaste.md` (the drift rule — four registries must agree, manifest wins).

---

*Canonical hub constitution. Lives in the hub repo root (matching the backup-engine /
YahirReusableBot ECOSYSTEM convention). The §1 consumer table is a best-effort cache; the
authoritative pin is each consumer's manifest + Gradle resolution. Created SecondBrain milestone
v1.20, Phase 100 (LIB-05).*
