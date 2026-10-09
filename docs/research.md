# Living Reefs: research report

Agentic SDLC central version 2026-09-16+2, Deep research team. As of 2026-10-08.
Gate status and evidence: `docs/sdlc-log.md`. Claim ledger `docs/research/claims.md` (G4 Claims with
teeth), sceptic report `docs/research/sceptic.md` (G5 Sceptic clean, raised 1 CRITICAL and 3 HIGH,
resolved in this version), independent review `docs/research/review.md` (G6 Independent review).

Citations point into the evidence packs under `docs/research/evidence/`: `[A-S9]` is source S9 of
pack A (competitors and demand), `[B-C]` is the 26.3 game code read in pack B, and so on. `[Sk-F1]`
is finding F1 of the sceptic report, which lists its own queries and sources; `[Sk-C2]` to `[Sk-C5]`
are the technical claims the sceptic re-checked against code (C2 game rules, C3 Lithium, C4 chunk
loading and cost, C5 light). `[R1]` and `[R2]` are
checks the research lead ran, listed in the provenance annex.

## Summary

- **No Fabric mod spreads coral through water, and no reef-specific mod was found on the searched
  platforms.** One mod makes coral spread on its own on 26.3: Grassidium, a NeoForge-only, closed-source mod with about a thousand
  downloads that spreads seagrass, kelp and coral as generic plants with a density cap, without
  bleaching, recovery or reef conditions. On Fabric 26.3, Carpet Sky Additions Reborn has a skyblock
  rule that turns calcite into coral, and Regrowth (up to 1.21.11) lets fish grow coral blocks.
  No project found on those platforms bleaches coral or revives it. [A-S1][A-S2][Sk-F1][Sk-F2][Sk-F3][R2]
- **The owner decided to differentiate** (2026-10-08, after the stop rule was raised; recorded in
  `docs/sdlc-log.md`): build for Fabric, reef-specific.
- **Vanilla coral never grows or comes back.** Coral only dies, on a scheduled tick when no water
  touches it, and dead coral is final. Everything living and recovering must be new. [B-C]
- **It can be fully server-side.** Living Reefs needs no new blocks or items: it swaps between the
  40 vanilla coral ids. Custom game rules are safe for vanilla clients at the protocol level.
  [B-C][D-S12][D-S13][R1]
- **Drive growth from coral's own random ticks.** A mixin makes the four living coral classes
  random tick, which brings simulation distance, `random_tick_speed`, `/tick freeze` and Lithium
  compatibility for free. [D-S1][D-S4][D-S8][D-S17]
- **Bound spread like vanilla bounds vines.** A local density cap, a substrate tag, a water
  clearance rule and a height cap keep worldgen reefs from smothering the ocean floor.
  [C-S13][C-S16]

## Q1 Demand and gaps

### Who asks

- Two mod authors state the wish directly: "I really want renewable coral" (Bonemeal Renewable
  Coral) and "I wanted Corals to grow... so I made them grow" (GTCoralsThatGrow). [A-S14][A-S8]
- The Minecraft Feedback site has several coral growth suggestions (for example "Coral Block
  Growth"), seen only through search summaries; the same summaries show players who value coral's
  fragility. Not verified: direct fetches returned 403. [A-S19]
- The r/feedthebeast thread 1woasop (top comment, 86 points, September 2026) is cited in the
  owner's earlier research note. Reddit blocked every fetch, so this is secondhand and unverified.
  [A-S20]
- Regrowth's 6.59 million CurseForge downloads show that players install mods where coral growth is
  one feature among many; that figure does not measure demand for coral itself. [Sk-F3]

The demand evidence is thin: the strongest signal is that people keep building bone meal, crafting
and mob-driven workarounds, not a large vote count.

### What exists

| Project | Loader, latest version | What it does | Grows on its own? |
|---|---|---|---|
| Grassidium | NeoForge, 26.3, about 1,000 downloads, closed source | Plants spread by light with a density cap; "seagrass, kelp and corals spread on their own"; game rules | Yes, as a generic plant; no bleaching, recovery or reef conditions [Sk-F1][R2] |
| Carpet Sky Additions Reborn | Fabric 26.3 | Skyblock rule `spreadingCoral`: calcite surrounded by 8 or more coral blocks of one type becomes coral | Partly: calcite only, no spread into water, no bleaching [Sk-F2] |
| Regrowth | Forge, Fabric, NeoForge, up to 1.21.11 | Fish and squid above a coral block grow coral blocks, wall fans and pickles; client and server | Partly: mob-driven stacking, not on 26.x, no bleaching or recovery [Sk-F3] |
| improveMint | Fabric 1.21.4, 99 downloads | Coral blocks sprout coral on their faces, "similar to Amethyst buds" | Partly; its description mentions no spreading to new blocks and no bleaching [A-S6] |
| GTCoralsThatGrow | Spigot 1.13, last update 2019 | A player-placed fan on a coral block grows a structure; ignores natural coral | Partly, abandoned [A-S8] |
| Corals: Reefier Reefs | Fabric 1.19, CurseForge only, 2022 | Bone meal advances and spreads coral | No, bone meal only [A-S9] |
| Coral Up | Forge 1.20.1 | Tall and bushy coral from bone meal | No [A-S10][A-S11] |
| Immortal Coral | Fabric/Quilt 26.3 | Game rule that stops coral dying; server-side | No [A-S12] |
| Waxable Coral | Fabric/Forge/NeoForge/Quilt 26.2 | Honeycomb keeps coral alive out of water | No [A-S13] |
| Bonemeal Renewable Coral, Bone Meal Revival, Create: Aquatic Ambitions | various, up to 1.21.11 | Renewable coral through bone meal or machines | No [A-S14][A-S16][A-S17] |
| Craftable-coral datapacks | datapacks, up to 26.3 | Recipes for coral | No [A-S3] |
| Reef Redux, CoralReef, Warm Coral Reef, Tidal Terror | various | Reef worldgen (Tidal Terror also adds mobs) | No [A-S1][A-S18] |

The first competitor searches all used the words "coral" or "reef" and missed the three projects
at the top of the table, where coral is one feature of a larger mod. [Sk-F4] Searches without those
words (plant spread, ecosystem, regrowth, living world, dynamic plants, underwater plants, ocean
life) found no further project that spreads coral on 26.x. [R2] The sceptic pass also searched
Hangar, SpigotMC and GitHub without new finds; Planet Minecraft returned 403 and was not searched.
[Sk-F11]

### Name

"Living Reefs" and the slugs `living-reefs`, `livingreefs`, `living-reef` and `living-coral`
return nothing on Modrinth (404) or CurseForge (0 results), with working control queries. Private
or draft projects cannot be seen through the public APIs, so the name is only certain once the
listing exists. [A-S21][A-S22]

## Q2 Vanilla coral in 26.3

- **Inventory.** Five types (tube, brain, bubble, fire, horn) times four forms (block, plant, fan,
  wall fan), each with a dead twin: 20 living and 20 dead ids. Living classes are `CoralBlock`,
  `CoralPlantBlock`, `CoralFanBlock` and `CoralWallFanBlock`; each holds a `deadBlock` field, the
  only living-to-dead link. Dead coral blocks are plain `Block` instances. Plants and fans carry
  `waterlogged`; wall fans add `facing`. [B-C]
- **Tags.** `minecraft:coral_blocks`, `coral_plants`, `corals` (plants and fans) and `wall_corals`
  list living coral only. Dead coral appears only in utility tags such as `minecraft:mineable/pickaxe`
  and `minecraft:blocks_motion_no_leaves`; no coral-family tag lists dead coral, and no tag maps dead
  to living. [B-D]
- **No growth, no random tick.** None of the coral classes random ticks; kelp is the only aquatic
  plant that does (14 % per tick, age cap 25). Seagrass never spreads. [B-C][D-S6][D-S7]
- **Death.** When `scanForWater` fails (no water in the six neighbours, and for plants not
  waterlogged), coral schedules a tick 60 to 99 game ticks later and turns into its dead twin;
  plants and fans become dead and not waterlogged, wall fans keep `facing`. Nothing checks light,
  depth, biome or neighbours. `random_tick_speed 0` does not stop death. [B-C]
- **Support.** Plants and fans need a sturdy top face below, otherwise they become air; wall fans
  need a sturdy face behind them. There is no substrate tag. [B-C][R1]
- **Bone meal.** In biomes tagged `#produces_corals_from_bonemeal` (only `warm_ocean`), bone meal
  on water makes 128 random-walk attempts and picks from `#underwater_bonemeals` (seagrass, coral
  and wall coral) with a one in four chance (the first attempt on a side face places a wall fan
  instead); it never places coral blocks. [B-C][R1]
- **Sea pickles.** Bone meal on a waterlogged pickle that sits on a living coral block spreads
  pickles in a radius-2 diamond onto other living coral blocks. Pickles give light 6, 9, 12 or 15
  when waterlogged. [B-C][B-S5]
- **Worldgen.** Only `warm_ocean` generates reefs (`warm_ocean_vegetation`: a tree, a claw and a
  stacked shape per type), with a 25 % chance of coral and a 3.75 % chance of a sea pickle on top of
  each coral block, and a 20 % chance per side of a wall fan. In 26.3 coral worldgen is data-driven
  and `CoralMushroomFeature` is gone. [B-D]
- **Biomes.** Every non-frozen ocean has temperature 0.5, so temperature cannot tell warm from cold
  water; only biome ids or tags can. [B-D]
- **Light under water.** Water does not pass sky light down: it dims light by one level per block,
  so sky light at depth `d` below the surface is about `15 - d`. [R1][Sk-C5]
- **Conduit.** No coral interaction; it activates with 16 or more prismarine-type frame blocks.
  [B-C]

## Q3 Design

Real reefs ground the design without simulating it: reef-building coral needs warm water, light
and clear water, so reefs sit mostly above about 20 m [C-S1][C-S3]; growth is millimetres to
centimetres a year [C-S6][C-S7]; bleaching is coral expelling its algae under heat stress, and short
bleaching is recoverable while long bleaching kills [C-S4]; herbivorous fish are reported to protect
new coral from algae (abstract seen only through a search summary) [C-S8]; restoration grows
fragments in nurseries and plants them out [C-S10][C-S11].

Every vanilla spreading block is bounded by a local density cap (vines: fewer than 4 other vines in
a 9 x 9 x 3 box), an age cap (kelp, bamboo) or a budget (sculk charge), and vines have an off switch
game rule, `spread_vines`. [C-S12][C-S13][C-S14][C-S15][C-S16]

Recommended rules (decided in `docs/spec.md`):

| Topic | Recommendation | Grounding |
|---|---|---|
| Where coral thrives | Biome tag `livingreefs:reef_waters` (warm, lukewarm, deep lukewarm ocean) | temperature is useless [B-D]; reefs need warm water [C-S1] |
| Light | `max(sky, block light) >= minLight`, so deep or roofed coral needs sea pickles or lanterns; the default must be calibrated on real worldgen reefs | water dims light per block [R1]; reefs need light [C-S1]; calibration [Sk-F6] |
| Substrate | Block tag `livingreefs:reef_substrate` (sand, gravel, stone family, clay, coral blocks); new coral only lands on it | vanilla has no coral substrate tag [B-C]; tag whitelists bound sculk [C-S16] |
| Spread | Living coral seeds the same species into a nearby still water block | vanilla bone meal picks random species [B-C]; species continuity reads as a living reef |
| Caps | Density cap around the target, water clearance above it, a reef height cap, water only (never replace blocks) | vines-style density cap [C-S13] |
| Bleaching | Heat sources (`livingreefs:reef_heat_sources`: magma, lava) and frozen waters turn coral into its vanilla dead twin | heat stress causes bleaching [C-S4] |
| Recovery | A thriving coral slowly revives a wet dead coral of its species next to it; dry dead coral is never revived | bleaching is reversible if stress ends [C-S4]; worldgen mods place dry dead coral on land [C-S21][C-S22] |
| Fish | Tropical fish nearby raise the growth chance | grazers reported to protect recruitment (abstract seen only via a search summary) [C-S8] |
| Sea pickles | Pickles nearby raise the growth chance and light deep reefs | pickles already live on coral [B-C] |
| Conduit | An active conduit makes nearby water count as reef water and shelters coral from bleaching | conduit has no vanilla coral link [B-C]; a reward for restoration |
| Rate | Tune against one random tick per block per about 68 s at speed 3 | [C-S17][D-S1] |
| Data-driven surface | Tags for blocks and biomes, a boolean game rule as the off switch, a JSON config for rates and caps | `spread_vines` precedent [C-S13]; Throughput config lessons |

Species pairing cannot come from tags. The mod derives living, dead and form from the vanilla id
pattern (`<type>_coral_block`, `<type>_coral`, `<type>_coral_fan`, `<type>_coral_wall_fan`, with a
`dead_` prefix), which also covers mods that name their coral the same way. [B-C][C-S20]

These rules are what sets Living Reefs apart from Grassidium, Regrowth and Carpet Sky Additions:
reef conditions, species continuity, bleaching and recovery, on Fabric 26.3, server-side.

## Q4 Server-side first

- **What works with vanilla clients:** everything above. Living Reefs only places and swaps
  existing vanilla block states, runs on server random ticks, uses vanilla particles for feedback,
  and adds commands built from vanilla argument types. [B-C]
- **Game rules are safe.** Since 1.21.11 game rules are a built-in registry [C-S25][C-S26]. The
  registry is not synchronised as registry data; the only rule packet carries identifiers, is sent
  only to operators on request, and the vanilla client skips unknown keys. Fabric's registry sync
  does not list the game rule registry as synced. [B-C][D-S12][D-S13][C-S27][R1][Sk-C2] Immortal
  Coral ships a custom game rule and runs server-side on 26.3 [A-S12].
- **What is lost without client code:** no new "bleached" block (the dead twin stands in for it),
  no custom textures or sounds, the custom rules do not appear in the in-game game rules screen
  (they work through `/gamerule`), and no client-side config screen.
- **Recommendation:** server-side only, no new blocks or items. Decided in the spec.

## Q5 Performance

- **Random ticks** run only in entity-ticking chunks (simulation distance or force-loaded), pick
  `random_tick_speed` positions per section that has a randomly ticking block, and are skipped while
  the game is frozen (`TickRateManager.runsNormally()`). [D-S1][D-S3][D-S4][D-S10]
- **The tick flag is fixed at startup.** `BlockState.isRandomlyTicking()` is cached in
  `initCache()` during bootstrap, before tags load, so it can be changed for coral classes by a
  mixin but cannot depend on tags. [D-S8][R1]
- **Lithium** replaces the vanilla random tick loop in sections with 384 or fewer ticking states, so
  a hook on the loop would silently not run; a block that reports `isRandomlyTicking` is still
  ticked. Read on Lithium's development branch; the 26.3 release source was not reachable.
  [D-S17][D-S18][Sk-C3][Sk-F7]
- **Options compared:** (a) mixin on the coral classes, native scoping and costs; (b) a budgeted
  scanner over loaded chunks, works with tags but must reimplement range, freeze and border rules;
  (c) a hook in the tick loop, broken by Lithium. [D-S1][D-S5][D-S16][D-S17]
- **Cost of (a):** reef sections become ticking sections (3 palette reads per tick, like any grass
  section), plus about `3C/4096` handler calls per section per tick for `C` coral. The handler must
  roll its chance first and read neighbours only after. Chunks next to a random-ticking chunk are
  loaded, so reads within 16 blocks horizontally of the coral stay in loaded chunks; Living Reefs
  also reads only chunks that are already loaded. [D-S1][D-S3][D-S5][D-S16][Sk-C4][Sk-F8]
- **Measurement plan:** copy Throughput's RCON benchmark (dev server on 25566, RCON 25576 on
  127.0.0.1, random password, 40 s warm-up, 15 readings, min/median/max) and measure the handler
  itself in nanoseconds, at `random_tick_speed` 3 and a stress value. [D-S20][D-S21]
- **Cost bound:** the handler runs only on coral random ticks, so its call rate grows linearly with
  `random_tick_speed` and the number of loaded coral; most calls end after one random number, and the
  rest read a fixed neighbourhood (at most a few hundred block reads, one entity query and the block
  entities of up to nine loaded chunks). The mixin design is described in `docs/spec.md`.
- **Recommendation:** option (a). A tag-driven scanner for other mods' coral is left out of the
  first release (see limitations).

## Q6 Compatibility

- **Other mods' coral.** Neither Fabric nor NeoForge convention tags have a coral tag; mods such as
  Upgrade Aquatic add their coral to the vanilla tags. Coral that extends the vanilla coral classes
  and follows the vanilla id pattern is handled automatically; anything else is ignored.
  [C-S18][C-S19][C-S20]
- **Mods that also tick coral.** Nature's Spirit and improveMint add `randomTick` to `CoralBlock`.
  If two mods add the same method to the same class, only one survives (chosen by mixin priority),
  and a mod that overrides `randomTick` on the coral class hides a hook on its base class. Living
  Reefs injects into the existing base-class methods instead of adding methods to `CoralBlock`, and
  documents the conflict. Neither mod is on 26.3 today. Regrowth and Grassidium could
  add coral in the same area; the density cap counts their coral too. [Sk-F5][Sk-F3]
- **Worldgen mods.** None of those checked adds warm reef biomes. Biomes O' Plenty and Terralith
  place dry dead coral on land, which Living Reefs must never revive; Tectonic changes ocean depth,
  so rules use light and water above the coral, never an absolute height. The `reef_waters` tag can
  take other mods' warm ocean biomes as optional entries. These three claims come from pack C and were
  not re-verified at G4; Terralith's source branch is 1.20 and may lag its releases. [C-S21][C-S22][C-S23]
- **Servers.** Server-side install only; settings through a config file, two game rules and
  `/livingreefs reload`; permission nodes through the bundled fabric-permissions-api, as Throughput
  does. Random ticks honour `random_tick_speed` and `/tick freeze`. [D-S1][D-S10]

## Limitations and open questions

- Demand evidence is weak and partly unverified (Reddit and the Feedback site could not be
  fetched). The build decision rests on the verified absence of a Fabric or reef-specific mod and
  the owner's decision, not on a measured audience.
- Grassidium is closed source; how well it grows coral was judged from its description only. The
  default value of Carpet Sky Additions Reborn's `spreadingCoral` rule was not checked.
- A live join test with an unmodified 26.3 client was not run; the vanilla client safety of custom
  game rules rests on code reading of the 26.3 client and Fabric's registry sync, plus Immortal
  Coral's precedent. The release checklist includes a manual join test.
- Lithium compatibility rested on reading Lithium's development branch; it was confirmed afterwards
  by a run with Lithium 0.26.2 during the build (`docs/sdlc-log.md`, F7).
- The default light threshold needs calibration against real worldgen reef depths (done during the
  build, recorded in `docs/sdlc-log.md`).
- Coral from other mods that does not extend the vanilla classes is not covered. A tag-driven scanner
  could add it later.
- Ecology numbers are narrative grounding only; growth rates and the BoP, Terralith and Tectonic
  claims were not re-fetched at G4 (pack-level evidence only).
- Not checked: Regions Unexplored, Lithosphere, Oh The Biomes We've Gone, William Wythers'
  Overhauled Overworld, Explorify; the 26.2 sulfur cube interaction with coral blocks; Planet
  Minecraft.

## Provenance annex

- Brief and plan: `docs/research/brief.md`.
- Evidence packs (sources with URL, publication and access dates, tiers, queries, budgets):
  `docs/research/evidence/A-competitors-demand.md` (24 of 30 calls),
  `B-vanilla-coral.md` (38 of 40), `C-design-compat.md` (about 37 of 35),
  `D-performance-tech.md` (44 of 40). Overruns came from shell commands refused by the worktree
  guard and repeated. Pack A has a wave-3 note for the generic re-run [R2].
- Claim ledger: `docs/research/claims.md` (42 claims checked by R3, 14 fixes applied; rows 43 to 48
  cover claims added after the sceptic pass, checked by the sceptic and the lead, not by R3).
  Sceptic report: `docs/research/sceptic.md` (F1 to F11, resolutions in `docs/sdlc-log.md`).
- [R1] Research lead checks on decompiled 26.3 sources (Loom `genSources`, 2026-10-08):
  `ClientboundGameRuleValuesPacket`, `ServerGamePacketListenerImpl#sendGameRuleValues`,
  `ClientPacketListener#handleGameRuleValues`, `InWorldGameRulesScreen#onGameRuleValuesUpdated`
  (unknown keys skipped), `BlockBehaviour$BlockStateBase#initCache`, `Blocks` static initialiser,
  `LiquidBlock#propagatesSkylightDown` (false) with `BlockBehaviour#getLightDampening` (1 for
  non-solid blocks that do not pass sky light), `BoneMealItem#growWaterPlant`, `CoralBlock`,
  `BaseCoralPlantTypeBlock`, `CoralPlantBlock`, `BaseCoralWallFanBlock#canSurvive`.
- [R2] Research lead checks on 2026-10-08: Modrinth API `project/grassidium` (NeoForge only, game
  versions to 26.3, 1,015 downloads, licence all rights reserved, description quoted above) and
  Modrinth searches restricted to 26.1 to 26.3 for "plant spread", "plants spread", "ecosystem",
  "regrowth", "living world", "nature spread", "dynamic plants", "underwater plants", "ocean life"
  and "skyblock coral"; hits were read for coral mentions (Water World - Atolls, Blue Depths, Alive
  Dynamic Ecosystems: none spread coral).
- Operators: orchestrating session and every R2 Search, R3 Claim Verification and R4 Research
  Sceptic subagent ran on Claude Opus 5.5, each subagent in a fresh context. The G6 reviewer is
  recorded in `docs/sdlc-log.md`.
- Distribution: public, in the open source repository. No paid sources.
