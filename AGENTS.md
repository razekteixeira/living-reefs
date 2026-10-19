# Source Code Agent Guide

This repository contains the source code for Living Reefs. The sibling vault
repository owns the project authority map and durable agent-facing synthesis.
The binding project, toolchain, quality and conduct rules are in the
"Living Reefs" sections at the end of this file; they apply to every agent
(Claude Code, Codex, Cursor, others).

The vault is local only (no remote). Cloud sessions and worktrees outside
`personal/living-reefs/` will not find it: everything needed for code work is
in this file, `CONTRIBUTING.md` and `docs/`.

## First Read

1. Read this file.
2. Read `../living-reefs-vault/Welcome.md`.
3. Read `../living-reefs-vault/Vault Map.md`.
4. Read `../living-reefs-vault/00 - Meta/Agent Guide.md`.
5. Read `../living-reefs-vault/00 - Meta/Source Authority.md` and
   `../living-reefs-vault/00 - Meta/Agentic Project Profile.json`.
6. Read only the notes relevant to the task.

## Source Hierarchy

1. User request in the current conversation.
2. Source-code repository rules in this file.
3. The authority map and governed vault notes under `../living-reefs-vault/`.
4. The owner's global agent instructions, when the runtime loads them.

## SDLC method (central)

This project adopts the central Agentic SDLC method. Source of truth for the
method (profiles, phases, G0 Intake ready to G8 Learning captured gates,
operational patterns):
`../../../agentic-suite-agnostic/` (start with `00 - Map/Vault map.md`).
Adapt, do not copy: the central method governs the form; this project governs
the local instantiation (stack, commands, thresholds).

- adopted agentic_sdlc_version: 2026-10-15+6
- Deltas since then: `../../../agentic-suite-agnostic/CHANGELOG.md` (focus on the
  "Downstream impact" section).
- Without the sibling checkout, the method is at
  `https://github.com/brunofgsaraiva/agentic-suite-agnostic`.
- Local adaptation (when it exists): see local instantiation note,
  `../living-reefs-vault/05 - Operations/SDLC Local Instantiation.md` (gates
  mapping, thresholds, repeatability decisions). The gate commands needed for
  code work are repeated in "Quality bar and gates" below.
- Knowledge sync mode: authoritative-vault.
- Knowledge navigation scale: small.
- Adoption contract: `../../../agentic-suite-agnostic/ADOPTION.md`.
- Agent entry rule: for delivery or coding work, use the `agentic-sdlc` gateway
  before acting. If the runtime does not load skills automatically, read
  `../../../agentic-suite-agnostic/skills/agentic-sdlc/SKILL.md` and follow it.

<!-- agentic-project:runtime-profile:start -->
## Agentic Project runtime defaults

- Selected by default when available: `rtk-ai-cli`. Compress noisy development command output before it enters agent context.
- These are project runtime preferences, not Agentic SDLC dependencies or gate
  criteria. The user can opt out of a default for a task.
- Explicit operator modes such as `modo-compacto` remain opt-in only.
- Optional scaffold files for project type `product-tooling`: none, by declaration. Reason: Product tooling is reproduced by its own engineers from the repository's test path, so the reader-facing report adds a second, weaker surface.

<!-- agentic-project:runtime-profile:end -->

## Rules

- When reporting information to me, be extremely concise and sacrifice grammar for the sake of concision
- Use EN-GB for project knowledge unless the user asks otherwise.
- Follow the sibling vault's `00 - Meta/Source Authority.md` for persistent
  knowledge, external facts, approvals and work state.
- Preserve relative links between the source-code and vault repositories.
- Keep raw source material, working notes and derived outputs separate.
- Use `../living-reefs-vault/02 - Sources/raw/` as the unprocessed source inbox;
  move ingested source files to `../living-reefs-vault/02 - Sources/raw/processed/`
  and attachments to `../living-reefs-vault/02 - Sources/raw/assets/`.
- Classify documentation code by role, not extension. This repository owns
  generators, themes and documentation applications that require a build,
  dependencies, tests or deployment. Hand-authored HTML, CSS or JavaScript that
  is itself human-readable documentation may belong in the sibling vault.
- Do not edit generated documentation outputs directly. Record
  `generated_from`, `build_command` and `output_path` in the vault operations
  note.
- If the sibling vault is unavailable, do not invent or silently replace its
  decisions. Record the missing dependency and continue only with bounded code work.
- Repeatability rule: when a task, or a stable part of it, is reasonably
  expected to run more than once, the work is not complete until it leaves the
  smallest suitable deterministic path under version control. Reuse an existing
  path first. Otherwise add a script or task, hook or CI check, scheduled job,
  skill or pipeline, or a loop only when iteration and a verifiable done-rule
  are required.
  Size the mechanism against its payback before building it: state the
  expected number of runs over a named horizon, the cost of one manual run,
  and the cost of building and owning the path. When the build does not pay
  back inside that horizon, keep the task manual and record that decision
  where the next operator will look. Frequency alone does not justify
  automation, and neither does a paid subscription.
  Document the trigger and verification. Keep irreducible judgement as an
  explicit human checkpoint; do not automate an unstable process or oracle.
- Do not revert or overwrite changes from another agent. Inspect dirty state before editing.
- Keep this file as the only canonical instruction source. `CLAUDE.md` is an
  import-only pointer and must not contain separate project rules.
- Do not store credentials, secrets or private customer data in the repository.

## Writing Conventions

- Never use the em dash or en dash in human-facing deliverables (HTML pages, slide decks, DOCX/PDF, published or client-facing content) or in the sources that render into them. Prefer commas, colons, parentheses or full stops; in ranges use "to" (for example, "G0 Intake ready to G8 Learning captured"). Internal artefacts (chat responses, code, commit messages, scratch notes) are exempt.
- Short identifiers (G0 Intake ready, D40 plus readable name, R10 plus readable name) always appear with the readable name beside them (for example, "G4 Tests with teeth"), never just the code.

## Living Reefs

Fabric mod for Minecraft Java 26.3 that makes coral reefs grow, bleach and
recover. Mod id `livingreefs`, package `io.github.razekteixeira.livingreefs`,
Apache-2.0. Public repo github.com/razekteixeira/living-reefs (default branch
`main`), site https://razekteixeira.github.io/living-reefs/ (GitHub Pages from
`site/`, only `main` deploys), published on CurseForge (project 1734790).
Product facts: `README.md`; requirements and acceptance criteria:
`docs/spec.md`; research: `docs/research.md`; gate log and owner decisions:
`docs/sdlc-log.md`; contributor rules, artwork pipeline and release steps:
`CONTRIBUTING.md`.

### Reference implementation

- Throughput (`../../throughput/throughput-source-code/`,
  github.com/razekteixeira/throughput) was built the same way and is the
  reference implementation: reuse its structure, tooling, scripts and quality
  bar. Read it only; never modify it from this project.

### Scope and conduct

- Never push a release tag or upload to CurseForge: both are the owner's
  actions (the owner pushes the tag; the release workflow then uploads).
- Do not create other repositories. Do not change anything outside this
  repository except reading the Throughput source repo, the central Agentic SDLC
  method and this project's local vault.
- Publish only on CurseForge (owner's choice on 2026-10-08, for its larger
  community), through mod-publish-plugin in `release.yml`. Do not add Modrinth
  or another platform unless the owner asks.
- Do not change the commit identity configured in this repository.
- Never publish the owner's personal or work email address. No secrets, tokens,
  absolute local paths or private notes in tracked files: this repository is
  public. Machine and account specifics live only in the local vault.
- The mod is server-side only: no new blocks, items, packets or synced
  registries, so vanilla clients keep working (see "Rules for changes" in
  `CONTRIBUTING.md`, which binds agents too).

### Toolchain

- JDK 25 (CI uses Temurin 25; on macOS with Homebrew,
  `export JAVA_HOME=/opt/homebrew/opt/openjdk@25`). Gradle only through the
  wrapper.
- Minecraft 26.3 is unobfuscated (Mojang names). Inspect APIs with `javap` on
  Loom's cached jars:
  `~/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-common-deobf/26.3/*.jar`
  (client classes: `minecraft-clientonly-deobf`).
- GitHub CLI and Git authentication specifics for the owner's machine are in
  the vault (`01 - Context/Session Brief.md`); follow them, never print tokens.
- Blender: headless `Blender -b -P script.py -- args` (5.2 LTS, Cycles), as in
  `branding/render_logo3d.py`.
- Headless Chrome (`--headless=new --virtual-time-budget=5000 --screenshot=...`)
  has a minimum window width: check the phone layout inside a 390 px iframe
  wrapper. Virtual time can stall CSS animations and lazy images in an iframe;
  re-check with reduced motion and a longer budget before "fixing" a layout.
- Python scripts run with `python3 -P` (not `-I`: Pillow is in the user
  site-packages). Also used: ffmpeg, ImageMagick (`magick`), actionlint,
  shellcheck.
- Client captures: `caffeinate -dimsu ./gradlew runClientGameTest` (display
  sleep stalls the window), always with a time limit and a "process died" exit
  in any wait loop.
- Dev server ports for this project: server 25566, RCON 25576 bound to
  127.0.0.1 with a random password per run (`tools/benchmark.sh`,
  `tools/rcon.py`). Sibling mods may run dev servers at the same time; never
  reuse their ports.

### Quality bar and gates

Commands and evidence for the SDLC gates (full mapping in the vault's SDLC
Local Instantiation note):

- G3 Build green: `./gradlew build` (compile, JUnit tests for `core` and
  config, server GameTests on a headless 26.3 server). Same command as the
  `build` workflow.
- G4 Tests with teeth: every behaviour change has a test that fails without
  it. `python3 -P tools/mutation.py` plants known faults one at a time; each
  must be killed by a named test, never by a compile error, and a harness
  result counts only when the log names the killing test. Add a mutant when
  you add a rule.
- Performance: per-tick work needs a reproducible benchmark. Touching
  `ReefTicker`, `ReefConditions` or `SpreadPlanner` requires
  `tools/benchmark.sh` numbers before and after.
- G5 Security clean: decide on every change (audit or explicit skip reason).
  Release workflow secrets (`CURSEFORGE_TOKEN`) stay scoped to the publish
  step.
- G6 Independent review: a fresh-context reviewer; it includes a visual check
  of real captures when chat output or visuals change.
- G7 Release: follow "Releases (maintainers)" in `CONTRIBUTING.md`, including
  the manual join test with an unmodified client; the owner pushes the tag.
- G8 Learning captured: lessons in `docs/sdlc-log.md`.
- Visuals: every image is produced by a committed script from a real capture
  run (`CONTRIBUTING.md` "Artwork"). Text next to a screenshot comes from the
  same run, by script (`branding/sync_chat.py`). Reset to default settings
  before any shot whose text or look is a claim. Verify the site at 1440 px
  and in a true 390 px iframe; every Pages asset must return 200.
- CurseForge copy: `branding/curseforge-listing.md` and
  `branding/curseforge-description.md`, text and screenshots from the same
  capture run.

### Lessons from Throughput (apply them)

- Saved data: version it, never let a decode throw, quarantine unreadable or
  newer data verbatim, and handle Minecraft's own unreadable-file path (it
  starts empty and overwrites on next save): back the file up and go
  read-only. Prove it with a truncated-file run on a real server plus a
  control run without the guard.
- Config: lenient read codec with defaults, strict write codec
  (`optionalFieldOf` omits default values when encoding), clamp with warnings,
  never overwrite a broken file, `/livingreefs reload`.
- Permissions: bundle `me.lucko:fabric-permissions-api` with `include`, use
  the `PermissionLevel` overloads (the int ones are deprecated), resend
  command trees after reload.
- Performance: measure the operation itself, warmed up, with repeated
  readings; coarse `/tick query` hides per-second spikes. Spread periodic work
  across ticks. Guard against `/tick freeze`.
- GameTests: start from a fresh world each run; tests touching global state
  clean up first.
- Tests and harnesses must discriminate: check what killed a mutant; quote
  counts from real runs.
- Visual check of real captures belongs in review. Hide hand and hotbar in
  chat shots (spectator mode), clear chat first.
- `curl --get` moves `--data-binary` into the URL. The GitHub Pages
  environment must deploy only from `main`.
