# Living Reefs: specification and plan

Agentic SDLC central version 2026-09-16+2, Software delivery team. Inputs: `docs/research.md`.
Gate evidence is logged in `docs/sdlc-log.md`.

## G0 Intake ready

- **Outcome case.** Beneficiary: survival players and server communities who build near warm
  oceans or restore reefs. Observed job: "make a reef grow and stay alive without spamming bone
  meal, and see it react to how I treat it". Current alternative: bone meal, crafting datapacks, or
  mods that only stop coral dying (research Q1). Outcome measure: on a vanilla client, a time-lapse
  of a natural warm ocean reef shows new coral of the same species appearing over time, a reef
  next to magma bleaching, and bleached coral recovering once the heat source is removed.
  Stop or pivot rule: stop if a current mod already does this (raised for Grassidium, a NeoForge
  generic plant-spreading mod; the owner decided on 2026-10-08 to differentiate), or if the
  measured cost per coral random tick is high enough to show up in `/tick query` at default
  settings on a dense reef (pivot to a budgeted scanner).
- **Scope.** A Fabric mod for Minecraft Java 26.3, server-side, no new blocks, items, entities,
  packets or registries other than two game rules. Plus repository, artwork and CurseForge listing
  files.
- **Non-goals.** Forge or NeoForge ports; other Minecraft versions; new blocks or a separate
  "bleached" texture; coral from other mods that does not extend the vanilla coral classes; a
  scanner over loaded chunks; persistent world data.
- **Sensitive tags.** `input-parsing` (config JSON, command arguments), `permissions` (command
  access, permission nodes), `ci-secrets` (the release workflow's CurseForge token). No auth,
  tenancy, personal data, saved world data or network protocol changes. `uiSurface: yes` (chat
  output, project site, banners).
- **Effort level.** L as a whole, split into WorkItems below (an XL single item is not allowed).
  Not fast-lane. Governance budget: spec and log stay short; the mod, its tests and the media are the
  deliverables.
- **Delivery posture.** `completion-first` for the release candidate (the owner asked for a
  publishable result); nothing is published without the owner: no tag, no CurseForge upload.
- **Prior lessons applied** (from the session brief, Throughput): config lenient read and strict
  write, clamp with warnings, never overwrite a broken file, `/livingreefs reload` (applied);
  bundled fabric-permissions-api with `PermissionLevel` overloads and command tree resend after
  reload (applied); measure the operation itself, warmed up, repeated, spread work, respect
  `/tick freeze` (applied); GameTests from a fresh world, global state cleaned up (applied);
  discriminating tests and mutants with named killers (applied); look at real captures in review,
  spectator mode for chat shots (applied); `curl --get` pitfall and Pages branch rule (applied when
  publishing); saved data versioning and quarantine (not applicable: Living Reefs stores no world
  data, decided below).

## G1 Spec/Design ready

### Design

**Driver.** A mixin on `BlockBehaviour` makes the four living coral classes (`CoralBlock`,
`CoralPlantBlock`, `CoralFanBlock`, `CoralWallFanBlock`, and subclasses) report
`isRandomlyTicking`, and calls `ReefTicker.onRandomTick` from `randomTick`. Vanilla then provides
simulation distance scoping, `random_tick_speed` scaling, `/tick freeze` and Lithium
compatibility. Vanilla death (scheduled tick when dry) is untouched.

**On each coral random tick** (cheap gate first):

1. If both game rules are off, return.
2. Draw one random number. If it is above the largest chance any action could have, return. Only
   then read the world.
3. Read the conditions around the coral into a `Conditions` value: in water, water kind (reef,
   neutral, frozen; from biome tags), light (`max(sky, block)` at the coral and its six neighbours, since a coral block is opaque), heat source within 2 blocks,
   active conduit within 16 blocks, sea pickles within 2 blocks, tropical fish within 6 blocks.
4. `ReefRules` (pure) turns conditions into a `Health`:
   - `STRESSED` when a heat source is near or the water is frozen, unless a conduit protects it;
   - `THRIVING` when in reef water (or near an active conduit) with light at or above `minLight`;
   - `DORMANT` otherwise (neutral water, or too dark): nothing happens.
5. `ReefRules.decide` picks one action with the random number: `BLEACH` (stressed), or `GROW` or
   `RECOVER` (thriving). Growth chance is multiplied by the fish, pickle and conduit boosts.
6. `GROW`: `SpreadPlanner` (pure, over a `ReefView` of the neighbourhood) picks a random target
   within `spreadRadius` and validates it: a still water source block; at least `minWaterAbove`
   water blocks above it; fewer than `maxNearbyCoral` coral within 2 blocks of it; supported by a
   `livingreefs:reef_substrate` block below (plant, fan or, from a coral block, a new coral block
   up to `maxReefHeight` high) or beside it (wall fan facing away). The new coral has the source's
   species. Nothing but water is ever replaced.
7. `RECOVER`: a dead coral of the same species within 1 block that is wet (waterlogged plant, fan or
   wall fan, or a dead coral block touching water) becomes living again, keeping its facing.
8. `BLEACH`: the coral turns into its dead twin, keeping `waterlogged` and `facing`.
9. Growth and recovery show vanilla happy-villager particles, bleaching shows white ash, both
   optional.

**Species.** `SpeciesTable` (pure) pairs living, dead and form from block ids by the vanilla
pattern (`<ns>:<type>_coral_block`, `_coral`, `_coral_fan`, `_coral_wall_fan`, dead with a `dead_`
prefix). Built from the block registry when the server starts.

**Data-driven surface.**

| Kind | Id | Default |
|---|---|---|
| Game rule | `livingreefs:coral_growth` (boolean) | true: growth and recovery |
| Game rule | `livingreefs:coral_bleaching` (boolean) | true |
| Biome tag | `livingreefs:reef_waters` | warm ocean (lukewarm removed at G6, see the spread decision below) |
| Biome tag | `livingreefs:frozen_waters` | frozen ocean, deep frozen ocean, frozen river |
| Block tag | `livingreefs:reef_substrate` | sand, red sand, gravel, clay, stone family, all coral blocks |
| Block tag | `livingreefs:reef_heat_sources` | magma block, lava |
| Config | `config/livingreefs.json` | rates, radii, caps, boosts, particles, permission levels |

**Commands.** `/livingreefs` with alias `/reef`: `check [pos]` (use: health of the coral looked at
or at `pos`, which must be within 16 blocks without admin rights, with each condition and the expected time to the next growth), `stats` (use: counts
since start and the handler's own time), `reload` (admin). Permission nodes `livingreefs.use` and
`livingreefs.admin`; without a permissions mod the configured vanilla levels apply (0 and 3).

**Spread decision (G6, owner, 2026-10-09).** The caps bound density, not extent: every new coral is
a source, so a thriving reef keeps creeping across the floor near players. The owner chose to limit
spreading to warm ocean (the one biome where vanilla generates reefs) by narrowing the
`reef_waters` tag, and to say plainly in the docs that reefs creep slowly across warm ocean floor.
Lukewarm water is neutral: coral lives there but does not spread. Servers can widen or narrow the tag.

**No saved data.** Everything the mod needs is in the world's blocks; statistics are kept in memory
since server start. This removes the saved-data risk class entirely.

**Server-side decision.** Server-side only (research Q4). Losses accepted: the dead twin stands in
for a bleached look; the custom rules are absent from the in-game rules screen.

### Acceptance criteria

Each criterion has a discriminating test: it fails if the behaviour is removed or inverted.

| AC | Criterion | Test (class#method) |
|---|---|---|
| AC1 | Living coral of every form random ticks; dead coral does not | `LivingReefsGameTests#livingCoralRandomTicksDeadDoesNot` |
| AC2 | Real random ticks grow a reef: with growth forced and a high tick speed, new coral appears in reef water | `LivingReefsGameTests#randomTicksGrowAReef` |
| AC3 | Growth keeps the species and only fills still water on substrate | `LivingReefsGameTests#growthKeepsSpeciesAndFillsOnlyWater`, `SpreadPlannerTest#*` |
| AC4 | Caps: no growth with too little water above, on non-substrate, or past the density cap | `LivingReefsGameTests#shallowWaterStopsGrowth`, `#glassFloorStopsGrowth`, `SpreadPlannerTest#respectsWaterClearance`, `#respectsDensityCap`, `#rejectsNonSubstrate`, `#coralBlockHeightIsCapped` |
| AC5 | Heat bleaches coral into its dead twin, keeping facing and waterlogging | `LivingReefsGameTests#heatBleachesCoral`, `ReefRulesTest#heatStresses` |
| AC6 | Thriving coral revives wet dead coral of its species; never dry dead coral, another species, or coral next to heat | `LivingReefsGameTests#recoveryRevivesWetDeadCoralOnly`, `#noRecoveryNextToHeat` |
| AC7 | Outside reef water and away from a conduit, coral is dormant; a conduit makes it thrive and protects it | `ReefRulesTest#dormantOutsideReefWaters`, `#conduitMakesWaterReefAndProtects`, `LivingReefsGameTests#conduitProtectsFromHeat` |
| AC8 | Turning off the game rules stops growth and bleaching | `LivingReefsGameTests#randomTicksGrowAReef` (both rules off), `#bleachingRuleSwitchesBleachingOff` (bleaching rule alone) |
| AC9 | Darkness stops growth without bleaching; the light of the water around a coral counts | `ReefRulesTest#darkCoralIsDormant`, `LivingReefsGameTests#darkCoralWaitsForLight` |
| AC10 | Boosts raise the growth chance (fish, pickles, conduit) | `ReefRulesTest#boostsMultiplyGrowthChance` |
| AC11 | Species pairing works for vanilla and same-pattern modded ids, and ignores incomplete families | `SpeciesTableTest#*` |
| AC12 | Config: missing file writes every default, partial file keeps defaults, out-of-range values clamp with warnings, a broken file is left untouched and the previous settings stay | `SettingsTest#*`, `LivingReefsConfigTest#*` |
| AC13 | `/reef check` explains the health of real coral; without admin rights only near the player | `LivingReefsGameTests#checkCommandExplainsHealth`, `#checkFarAwayNeedsAdmin`, `SpreadPlannerTest#openSpotsCountWhatGrowthCouldUse` |
| AC14 | A vanilla client can join (no registry or packet changes besides game rules) | Code review at G6 plus manual join in the release checklist (`CONTRIBUTING.md`) |
| AC15 | The handler costs little: measured median time per coral random tick and per second on a dense reef | `tools/benchmark.sh` output recorded in `docs/sdlc-log.md` |

## G2 Plan ready

**Scope boundary (files the build may touch):** `src/**`, `build.gradle`, `gradle.properties`,
`settings.gradle`, `tools/**`, `branding/**`, `site/**`, `docs/**`, `.github/**`, root
documentation files. Nothing outside this repository except reading the Throughput source repo
(`../../throughput/throughput-source-code`) and the central Agentic SDLC vault.

| WorkItem | Content | Gates |
|---|---|---|
| WI-1 Research | `docs/research.md` with ledgers | Research gates G0 to G8 |
| WI-2 Mod | core (`SpeciesTable`, `ReefRules`, `SpreadPlanner`, `Settings`), glue (mixin, `ReefTicker`, `WorldReefView`, config, rules, tags, commands, stats), JUnit and GameTests, benchmark | G3 Build green, G4 Tests with teeth, G5 Security clean, G6 Independent review |
| WI-3 Media and site | client capture GameTest, icon, Blender logo, GIF, banners, Pages site, README, CurseForge files | G6 includes a visual check of real captures |
| WI-4 Release preparation | CHANGELOG, CONTRIBUTING release steps, CI green, Pages live, dry-run publish | G7 Release prepared (no tag by owner rule), G8 Learning captured |

Build order inside WI-2: core with tests first, then the mixin and ticker with a GameTest for AC1
and AC2, then the remaining GameTests, then commands, then the benchmark.
