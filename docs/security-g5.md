# G5 Security clean: security review

Reviewer: fresh-context security reviewer (Agentic SDLC, gate G5 Security clean), 2026-10-09.
Scope: branch `build` at `fc50e11`, worktree `.claude/worktrees/build`. Sensitive tags from
`docs/spec.md`: `input-parsing`, `permissions`, `ci-secrets`. Review only, no code changed. Gradle
was not run (a client capture run was using the project); behaviour claims are checked against the
code and the decompiled 26.3 sources.

## Verdict

**Clean with fixes.** No CRITICAL or HIGH findings. One MEDIUM finding: `/reef check <pos>` lets any
player probe any loaded chunk in the world, so they can find where other players are. Fix it
before release. The LOW findings are hardening and can be fixed now or tracked.

## Findings ledger

| ID | Severity | Where | Finding and scenario | Fix | Verified by |
|---|---|---|---|---|---|
| S1 | MEDIUM | `src/main/java/io/github/razekteixeira/livingreefs/LivingReefsCommands.java:52-53`, `:49` (`canUse`, default level 0 in `core/Settings.java:57`) | **Remote loaded-chunk oracle and remote inspection for unprivileged players.** `check <pos>` is open at the `use` level (0 by default) and takes any absolute coordinates. `BlockPosArgument.getLoadedBlockPos` throws `argument.pos.unloaded` ("That position is not loaded") for unloaded chunks and the mod throws "That block is not coral" for loaded ones. Loaded chunks are where players, chunk loaders and spawn are, so any player can scan the map and find other players' positions and bases. The vanilla command spam throttle allows about 20 probes a second, and a coarse grid (one probe per view-distance square) covers a large map in minutes. When the block is coral, the report also gives remote light level, the tropical fish count within 6 blocks, sea pickles and whether an active conduit is within 16 blocks. That information is otherwise only visible on site. No vanilla level-0 command exposes remote world state. This matters on survival, PvP and anarchy servers. | For sources without `canAdmin`, accept `pos` only within a short distance of the player (for example `blockInteractionRange() + 8`, or the same chunk radius as the player's view). Outside it, return the same generic error ("Look at a coral or give a position near you"), so loaded and unloaded positions answer the same. Alternatively, put the `pos` branch behind `canAdmin` and keep the looked-at form for everyone. Add a GameTest: a level-0 source with a far `pos` gets the generic error. | Read `BlockPosArgument.getLoadedBlockPos` (mcsrc, lines 37-46: `hasChunkAt` then `ERROR_NOT_LOADED`); `ServerChunkCache.hasChunk` uses the ticket level; `ServerGamePacketListenerImpl.detectRateSpam` / `TickThrottler(20, 20*threshold)` sets the probe rate; spec line 95 confirms `check [pos]` is at the use level. |
| S2 | LOW | `.github/workflows/release.yml:7-8,14,28-29` | **Write token reachable by build code in the release job.** The job has `contents: write`, and `actions/checkout` keeps the default `persist-credentials: true`, so `GITHUB_TOKEN` sits in `.git/config` while `./gradlew build` runs. That build runs third-party code: Loom `1.18-SNAPSHOT`, a mutable version (S3), plus Minecraft and the GameTests. A compromised plugin or dependency could push to the repository or change the release. | Add `with: persist-credentials: false` to the checkout step. Optionally split the job: build and test with `contents: read`, then upload the jar as an artifact to a second job with `contents: write` that only runs `gh release`. | Read the workflow. `gh release` gets its token from `GH_TOKEN`, so the persisted credential is not needed. |
| S3 | LOW | `gradle.properties:7` (`loom_version=1.18-SNAPSHOT`) | **Mutable build plugin version.** A SNAPSHOT resolves to whatever Fabric's maven serves at build time, so the same tag can build differently. Fabric's template does the same, and Throughput uses it too. There is no Gradle dependency verification or lockfile. | Pin a released Loom version when 26.3 support has one, or enable `gradle --write-verification-metadata sha256` for plugins. | Read the build files. |
| S4 | LOW | `gradle/wrapper/gradle-wrapper.properties:3` | **No `distributionSha256Sum`.** The Gradle distribution zip is downloaded over HTTPS without a pinned checksum. The wrapper jar matches Throughput's (sha256 `7a9ce74c...262c5d`), and `gradle/actions/setup-gradle@v6` validates wrapper jars in CI by default. | Add `distributionSha256Sum=` with the official checksum for `gradle-9.7.1-bin.zip` from services.gradle.org. | `grep` found no checksum line; compared wrapper jar hashes. |
| S5 | LOW | `.github/workflows/*.yml` | **Actions pinned by major tag, not by commit SHA** (`actions/checkout@v7`, `setup-java@v6`, `gradle/actions/setup-gradle@v6`, `upload-artifact@v7`, `configure-pages@v6`, `upload-pages-artifact@v5`, `deploy-pages@v5`). If a tag is moved, the release job (with its write token and the CurseForge secret) runs that code. All are first-party GitHub or Gradle actions. | Pin to full SHAs with a version comment, and let Dependabot (`github-actions` ecosystem) update them. Risk level, not a blocker. | Read the workflows. |
| S6 | LOW | `.github/workflows/release.yml:3-5,55-58` | **The CurseForge secret has no environment protection.** `CURSEFORGE_TOKEN` will be a repository secret, so any workflow on any pushed ref or tag can read it. The release runs on any `v*` tag pointing at any commit. The secret is not set yet (`gh api .../actions/secrets` is empty), so today every publish is a dry run. | When the token is added, store it in a `release` environment limited to `v*` tags (optionally with a required reviewer), and set `environment: release` on the job. | `gh api repos/razekteixeira/living-reefs/actions/secrets` returned no secrets; the default workflow permission is `read`. |
| S7 | LOW | `SECURITY.md:3-5` | **The advertised reporting channel is off.** SECURITY.md sends reporters to GitHub private vulnerability reporting, but it is disabled for the repository (`{"enabled":false}`), so reporters hit a dead end or file a public issue. | The owner enables it: Settings, Code security, Private vulnerability reporting, or `env -u GITHUB_TOKEN gh api -X PUT repos/razekteixeira/living-reefs/private-vulnerability-reporting`. | `gh api repos/razekteixeira/living-reefs/private-vulnerability-reporting`. |
| S8 | LOW | `src/main/java/io/github/razekteixeira/livingreefs/ReefConditions.java:79-100` | **Assessment cost grows with nearby block entities.** Every assessed coral tick (about 22% of coral random ticks at the defaults, 100% when an admin sets `growthChance` 1) walks every block entity in up to 3x3 chunks to find conduits. A player who packs thousands of block entities (signs, chests) beside a dense hand-placed coral field multiplies the per-tick cost. The work is bounded by vanilla's random-tick rate, needs a deliberate build, and is on the same order as vanilla lag machines. | Optional: keep a per-level set of active conduit positions, for example updated from `ConduitBlockEntity` server ticks, and check distance against that. Or cache the result per chunk per game tick. Add a block-entity-heavy case to `tools/benchmark.sh`. | Code reading; chunk scan limited to `getChunkNow`. |
| S9 | LOW | `src/main/java/io/github/razekteixeira/livingreefs/core/SpreadPlanner.java:23-66` | **Growth does not consult claim or protection mods.** Coral can grow into source-water cells of another player's underwater build in reef water or near their conduit. Coral blocks from coral-block sources can fill water cells, up to `maxReefHeight` 3. A griefer could seed coral next to someone's base. The impact is low: growth is slow (about 28 min per attempt at the defaults), capped by density, limited to water source blocks with 2 water above, and a griefer could place the blocks directly in vanilla anyway. | Document it in the README FAQ (gamerule `livingreefs:coral_growth false` per world). Optionally integrate Patbox's Common Protection API later. | Code reading: placement needs `Blocks.WATER` source and `canSurvive`. Nothing else is replaced (see "Checks that passed"). |
| S10 | LOW | `src/main/java/io/github/razekteixeira/livingreefs/LivingReefs.java:16`, `LivingReefsConfig.java:56-58` | **A broken config at startup falls back to the default permission levels.** If an owner raised `permissionLevels.use` to restrict the commands and the file breaks, the server starts with `use = 0` and the commands are open again. The impact is limited because `use` only covers `check` and `stats`, and `admin` stays at 3. A later `reload` of a broken file correctly keeps the previous settings. | Accept it and document it, or on a startup parse failure use `use = admin = 4` (closed) until a good reload. | Read the code and `brokenFileKeepsPreviousSettingsAndIsNotOverwritten`. |
| S11 | LOW | `docs/research/claims.md:8`, `docs/research/evidence/A-competitors-demand.md:83` | **Absolute local paths in committed files**: `(a local scratch path)` and `(a private notes path)`. They expose the local username and directory layout, nothing secret. | Replace them with neutral wording ("decompiled 26.3 sources", "owner's local research note"). | `git ls-files -z \| xargs -0 grep -In '/Users/'`. |
| S12 | LOW (info) | `tools/benchmark.sh:16-33`, `tools/compat-lithium.sh:14-28` | **Dev RCON password stays on disk after a run** in `run/server.properties` (mode 644, gitignored), and the server log goes to an undeleted `mktemp` file. The password is random per run (`secrets.token_hex(16)`), RCON and the game port bind to `127.0.0.1`, the password goes to `rcon.py` through the environment (not argv), and it is never printed. | Optional: `chmod 600 run/server.properties`; delete or blank `rcon.password` in the EXIT trap. | Read the scripts. `RconThread.create` binds `new ServerSocket(port, 0, InetAddress.getByName(serverIp))` with `server-ip=127.0.0.1`. `git check-ignore` covers `run/`. |
| S13 | LOW (info) | `site/index.html:27-29` | **Third-party fonts from Google Fonts**, so visitor IPs go to Google (a GDPR consideration in the EU). There are no third-party scripts, the inline scripts use `textContent`, and there is no CSP (GitHub Pages cannot set headers). | Optional: self-host the three fonts (Geist and Silkscreen are already in `branding/fonts`, under OFL). | `grep` of the external URLs in the site. |

## Checks that passed (no finding)

- **Config parsing** (`LivingReefsConfig.java`, `core/Settings.java`): the file name is fixed under
  the Fabric config dir, with no user-supplied path. The file is written only when
  `Files.notExists`, so a broken file is never overwritten. Every `IOException | RuntimeException`
  (malformed JSON, `null`, `[]`, wrong types through `getOrThrow`) is caught and keeps the previous
  settings (unit tests `brokenFileKeepsPreviousSettingsAndIsNotOverwritten` and
  `wrongTypeIsReportedNotIgnored`). Gson's lenient `NaN`/`Infinity` are clamped, and NaN goes to the
  minimum (`notANumberIsClampedNotKept`). Huge integers parse through `LazilyParsedNumber` and are
  then clamped. Every numeric field has a range (radius 1..4, density 1..124, chances 0..1, boosts
  1..10, permission levels 0..4). Only the server owner can write the file.
- **Permissions**: `Permissions.check(CommandSourceStack, String, PermissionLevel)` is the
  non-deprecated overload (confirmed with `javap` on the bundled 0.7.0 jar). `PermissionLevel.byId`
  uses `ByIdMap ... OutOfBoundsStrategy.CLAMP`, and the values are already clamped to 0..4. Levels
  are read on every check. `reload` needs `canAdmin` (default 3), and the `/reef` redirect keeps
  that requirement because it goes to the same node. After a reload, command trees are resent to
  every player. With a permissions mod, the `livingreefs.use` and `livingreefs.admin` nodes take
  precedence and an undefined node falls back to the level.
- **Chunk loading**: the growth, recovery, condition, light, wetness and conduit reads use
  `getChunkNow` (main thread, FULL chunks only) or `getBrightness`, `getBiome` and
  `getEntitiesOfClass`, and none of them load chunks. `/reef check` uses
  `getLoadedBlockPos` (ticket level FULL, in world bounds) and then `level.getBlockState`. That is
  the same pattern vanilla commands use, and it cannot generate new chunks. The looked-at form is
  limited to the player's interaction range.
- **Block changes are contained**: growth writes only into a still water source (`Blocks.WATER` and
  `isSource`) that has `minWaterAbove` water cells above, and only after `canSurvive`. Recovery
  only turns a wet dead coral of the same species into its living twin. Bleaching only turns the
  ticked living coral into its dead twin, and only when both ids exist. Only instances of the four
  vanilla coral classes are ticked (`LivingCoral`). No non-coral, non-water block is ever replaced.
- **Bounded cost per tick**: one random number per coral random tick. World reads happen only
  below `ReefRules.gate`, and each growth attempt tests one spot. Vanilla fixes the number of random
  ticks per section, so more coral does not mean more ticks. A density cap of 14 in a 5x5x5 box and
  a reef height cap of 3 stop overgrowth. Command spam from non-operators is throttled by vanilla
  (`detectCommandRateSpam` kicks the player).
- **Information exposed by `/reef stats`**: global counters and handler timings only, nothing about
  positions or players.
- **CI injection**: no `${{ github.* }}` expressions in `run:` blocks. `GITHUB_REF_NAME` is used
  as a quoted shell variable, and the tag must equal `v` + the `version` in `gradle.properties`
  before anything else runs. `build.yml` has `contents: read`, is triggered by `push` and
  `pull_request` (not `pull_request_target`), and gets no secrets. `pages.yml` has the minimal Pages
  permissions. The repository's default workflow permission is `read`.
- **CurseForge token**: it is only in the env of the final `publishCurseforge` step.
  `build.gradle:97` makes it a dry run when the token is blank or missing or
  `curseforge_project_id` is empty (the secret is not set yet and the project id is empty, so today
  every run is a dry run). The token is never echoed.
- **Supply chain**: the bundled jar-in-jar is `me.lucko:fabric-permissions-api:0.7.0` (MIT, needs
  Minecraft >=26.1), credited in `THIRD_PARTY_NOTICES.md`. Plugins come from Fabric maven, Maven
  Central and the Gradle plugin portal over HTTPS. `mod-publish-plugin` 2.2.1 is pinned. JUnit
  comes from the BOM 6.1.3.
- **Secrets and personal data**: the tracked tree and all history contain no tokens, keys or
  passwords. The only e-mail address is `luck@lucko.me` in the third-party license notice, which is
  public upstream. Commit authors are the GitHub noreply address only. The owner's private e-mail
  address appears nowhere in files or history. `CLAUDE.md` is gitignored and was never committed.
- **Dev tooling**: `tools/mutation.py` uses `subprocess.run` with an argument list (no shell) and
  restores the source in `finally`. The shell scripts quote their variables, and `rm -rf` targets
  fixed relative paths after `cd` to the repo root.

## Checks run

| Command | Result |
|---|---|
| `shellcheck tools/*.sh branding/make_media.sh` | exit 0, no warnings |
| `actionlint .github/workflows/*.yml` | exit 0, no findings |
| `git ls-files -z \| xargs -0 grep -IhoE '<email regex>' \| sort \| uniq -c` | 1 hit, `luck@lucko.me` (THIRD_PARTY_NOTICES.md:12) |
| `git log --all --format='%ae %ce' \| sort -u` | only `22983237+razekteixeira@users.noreply.github.com` |
| `git ls-files ... \| grep -E 'ghp_\|github_pat_\|AKIA\|-----BEGIN\|api_key\|token=\|password='` and `git log --all -p \| grep -c privaterelay` | no secrets; only `rcon.password=${RCON_PASSWORD}` placeholders; 0 private-address hits |
| `git ls-files ... \| grep -In '/Users/'` | 2 hits in docs/research (S11) |
| `git log --all --name-only` for CLAUDE.md, .env, keys | none committed |
| `shasum -a 256 gradle/wrapper/gradle-wrapper.jar` vs Throughput | identical (`7a9ce74c...262c5d`) |
| `grep distributionSha256Sum gradle/wrapper/gradle-wrapper.properties` | absent (S4) |
| `javap` on `fabric-permissions-api-0.7.0.jar` `Permissions` | `check(SharedSuggestionProvider, String, PermissionLevel)` exists; the int overloads are not used |
| mcsrc: `BlockPosArgument.getLoadedBlockPos`, `ServerChunkCache.hasChunk/getChunkNow`, `PermissionLevel.BY_ID`, `RconThread.create`, `ServerGamePacketListenerImpl.detectRateSpam` | behaviour as described in S1, S12 and "Checks that passed" |
| `env -u GITHUB_TOKEN gh api repos/razekteixeira/living-reefs/private-vulnerability-reporting` | `{"enabled":false}` (S7) |
| `env -u GITHUB_TOKEN gh api repos/razekteixeira/living-reefs/actions/permissions/workflow` | `default_workflow_permissions: read` |
| `env -u GITHUB_TOKEN gh api repos/razekteixeira/living-reefs/actions/secrets` | no secrets set (CurseForge publish is a dry run) |
| `grep` of site external resources | Google Fonts only; no CDN scripts (S13) |
