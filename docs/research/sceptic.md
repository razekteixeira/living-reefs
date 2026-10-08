# Sceptic report

Role: R4 Research Sceptic (Deep research team), gate G5 Sceptic clean. Adversarial pass over
`docs/research.md` and its evidence packs, by a fresh-context agent that did not write them.
Date: 2026-10-08. About 40 tool calls. All fetched content was treated as untrusted data.

**Verdict: G5 not clean.** One CRITICAL and three HIGH findings. The competitor claim at the centre
of the report, and so the stop-rule verdict, does not survive disconfirmation as written. Three
projects the report missed make coral grow or spread without bone meal. Two of them run on
Minecraft 26.3, one of those on Fabric. The technical claims (a) to (d) all held. One of them
needs a scope note and one more open risk was found.

## Load-bearing claims and defences

| # | Claim (from research.md) | Attack | Outcome |
|---|---|---|---|
| C1 | "No mod, datapack or plugin found on Modrinth or CurseForge makes coral spread over time without bone meal on 1.21.5 or later, or on 26.x, for any loader." The stop rule is not triggered. | Generic searches with no coral words (ecosystem, plant spread, regrowth, skyblock), GitHub code search for `CoralBlock` plus `randomTick`, and reading the source of each hit. | **Broken.** Grassidium (NeoForge, 1.21.1 to 26.3, server-side, updated 2026-09-22): "It works underwater too: seagrass, kelp and corals spread on their own." Carpet Sky Additions Reborn (Fabric 26.2 and 26.3, server-side, updated 2026-10-04): the rule `spreadingCoral` turns calcite into coral when 8 or more coral blocks of one type surround it. Regrowth by mactso (Forge, Fabric and NeoForge up to 1.21.11, 6.59M CurseForge downloads): fish and squid grow coral blocks, wall fans and pickles. |
| C2 | Custom game rules are safe for vanilla clients. | Fabric API branch 26.3: `FabricRegistryInit` (the list of SYNCED registries) and `RegistrySyncManager` (disconnects clients that lack Fabric). Vanilla `GameRuleCommand`. | **Holds.** `GAME_RULE` is not in the SYNCED list. The 26.3 list covers block, item, fluid, entity type and others, but not game rules. A client is disconnected only when synced registries hold modded entries. The vanilla `/gamerule` tree uses a literal per rule plus brigadier value types, so a vanilla client can parse it. The `fabric-game-rule-api-v1` client code sits in the client source set, and the `rpc` package serves the management protocol. A live join test is still missing (the report already says so). |
| C3 | Making coral random tick through a mixin works with Lithium. | Lithium `BlockStateFlags` (develop branch). | **Holds on develop.** `RANDOM_TICKING.test` returns `operand.isRandomlyTicking() \|\| operand.getFluidState().isRandomlyTicking()`, so the report's inference is now confirmed by source. The release tag path `mc26.3-0.26.2-fabric/common/...` returned 404, so the claim is still sourced from develop only. **New risk the report missed:** other mods add `randomTick` and `isRandomlyTicking` to `CoralBlock` through mixins too: Nature's Spirit (`CoralBlockBlockMixin`, 10.6M Modrinth downloads, latest 1.21.1) and improveMint. When two mixins add the same method to `CoralBlock`, only one survives, chosen by priority. |
| C4 | Neighbour reads within a chunk of the coral stay inside loaded chunks. | `ServerChunkCache#tickChunks`, `ChunkMap#forEachBlockTickingChunk`, `DistanceManager`, `ChunkLevel` in the decompiled 26.3 sources. | **Holds, with a scope note.** Random ticks run for chunks that are entity-ticking in the simulation tracker and whose holder returns a ticking chunk (loading level 32 or lower). Neighbours are therefore at level 33 or lower, which is `FULL` (levels: 31 entity ticking, 32 block ticking, 33 full). This only covers reads within 16 blocks horizontally. A read two chunks away can reach an unloaded chunk, and a server-thread `getBlockState` there loads the chunk synchronously. |
| C5 | Water dims sky light by one level per block. | `BlockBehaviour#getLightDampening` and `LiquidBlock#propagatesSkylightDown`. | **Holds.** Dampening is 15 for solid-render blocks, otherwise 0 if the block passes sky light down and 1 if not. Water returns `false`, so it dims by 1 per block. **The report misses a consequence:** reefs on the warm ocean floor sit several to many blocks below sea level, so sky light there is already low. A `minLight` rule could make most worldgen reefs inert, or so permissive that it means nothing (see F6). |

## Disconfirmation queries (query, where, result)

| Query | Where | Result |
|---|---|---|
| coral | Modrinth API search, limit 40 | 82 hits. No growth or spread beyond known ones. Noted `corals-tfc` (TFC use), `creative-superflat-coral-reef-worldgen`, `elkonia-deep-coral-ocean-biome` (worldgen). |
| reef | Modrinth | 16 hits, all worldgen, decor or unrelated (`rainbow-reef` is fish). |
| coral grow; coral regrowth; growing coral; coral farm; coral reproduce | Modrinth | 0 each. |
| coral spread | Modrinth | 1: Bonemeal Renewable Coral (bone meal). The report says this query "found nothing", which is slightly off. |
| renewable coral | Modrinth | Bonemeal Renewable Coral, parrot-fish, feather-experience (no passive spread). |
| bleach | Modrinth | Anime packs and `bleached_corals` (resource pack). No mechanic. |
| ocean life; aquatic ecosystem; ocean; underwater | Modrinth | Checked `water-world-atolls` (26.3): atoll villages, no coral. Nothing else. |
| ecosystem; dynamic ecosystem; ecology; natural spread; plants spread | Modrinth | **Grassidium** (26.3, NeoForge): corals spread on their own. Also checked Dynamic Life, Alive Dynamic Ecosystems, Gaïa's Breath, Evo Biomes, Seasons&Scarcity, Living Ecosystem, Living World Seasons & Ecology, Blue Depths: none mention coral spread. |
| grassidium | CurseForge (curse.tools proxy, `/v1/mods/search`) | Listed, 638 downloads, summary "Grass, flowers, underwater, and modded plants spread". The word coral is not in the summary, which is why coral searches miss it. |
| grassidium versions | Modrinth `/project/grassidium/version` | 1.2.0 for 26.3, 26.2, 1.21.11 and 1.21.1, all NeoForge, 2026-09-22. All rights reserved, no source. |
| coral; reef; coral grow; corals grow; coral spread; regrow; bleaching; ocean revival; living ocean | CurseForge, classes 6 (mods), 6945 (data packs), 5 (Bukkit) | Coral 31, reef 17, regrow 18. **Regrowth** (id 381597) found under "regrow". Its description says "Coral (after 1.16) Fish and squid will slowly regrow coral including fans". Others checked had no coral spread (Alive Dynamic Ecosystems, Mariculture 1.7.10 worldgen, Korrals, Coral Resurrection, Tidal Terror, Warm Coral Reef, Crafting Refinements). |
| Regrowth source | GitHub mactso/Regrowth, branch `1.21.11_Common_Code`, `CoralActionHelpers.java` and `MobActions.java` | Confirmed. An aquatic mob above a coral block, outside cold or frozen oceans, places a copy of that coral block 1 block up and 0 or 1 sideways (at most 3 coral blocks in the 3x3 above). It also adds wall fans (density cap 3 in 5x5), sea pickles in the dark, and seagrass. There is no bleaching, no recovery and no spread across the seabed. Loaders 1 (Forge), 4 (Fabric) and 6 (NeoForge) up to 1.21.11. Client and server both required. Repo last pushed 2026-02-24. |
| "CoralBlock" "randomTick" (Java) | GitHub code search | Nature's Spirit `CoralBlockBlockMixin` (calcite buds beside coral columns over bubble columns, not coral growth). improveMint mixin (known). **CarpetSkyAdditions(-Reborn) `CoralSpreader`**. VoX/ocean-overhaul (own block). Oceanic Expanse (1.12 own blocks). |
| CarpetSkyAdditions-Reborn | GitHub source and `gradle.properties`, Modrinth `carpetskyadditionsreborn` | `minecraft_version=26.3`, Fabric, server-side, 34,117 downloads, updated 2026-10-04. `CustomCalciteBlock#randomTick`: under rule `spreadingCoral`, calcite with 8 or more same-type coral blocks in its 3x3x3 becomes that coral. The chance scales with a climate "suitability" from the temperature and continentalness noise. It is a skyblock renewability mechanic. Carpet rules are usually off by default, but this one was not checked. |
| sky additions | Modrinth | Sky Void Additions (808k, 26.2): coral growth only through bone meal. |
| coral; reef | Hangar (PaperMC) API | No coral growth plugin. TeaksTweaks has only craftable coral blocks. |
| corals; coral grow; coral spread; plant spread; ecosystem | Spiget (SpigotMC) API | GTCoralsThatGrow (known), CoralStay, noWaterCorals, Shear Corals. EcoHarmony has no coral. |
| coral grow (data packs) | Planet Minecraft | HTTP 403, not checked. Gap remains. |
| "minecraft mod coral grows naturally spreads over time" | WebSearch | Only improveMint, Reefier Reefs, Coral Up, GTCoralsThatGrow (all known). |
| "minecraft datapack coral reef regrowth spread planetminecraft" | WebSearch | Crafting, worldgen and recipe packs only. |
| "coral" "spread" fabric 1.21 random tick github | WebSearch | Nothing new. |
| coral spread minecraft; coral grow minecraft | GitHub repository search | 0 results. |

## Bias check

- **Confirmation-shaped queries (main bias).** Every competitor query in pack A names the thing being
  looked for (coral, reef, coral growth, coral spread, reefier). Projects where coral is one feature of
  a general plant, ecosystem, mob or skyblock mod do not say coral in their name or summary, so these
  searches cannot find them. All three misses (Grassidium, Regrowth, Carpet Sky Additions Reborn) are of
  this kind. Wave 2 of pack A stopped after finding no new competitor among coral-named projects. The
  stop rule was applied to a search space that was too narrow.
- **Origin diversity.** Competitor evidence rests on two platform APIs plus one unaffiliated
  CurseForge proxy. Spigot was reached only through a hosting-site mirror, and Planet Minecraft and
  GitHub code were not searched. Technical claims rest on the decompiled game code (tier 1, good) and on
  Lithium's develop branch (one origin, not the release).
- **Vendor narrative.** The brief came from Throughput's research ("clearest 'nothing exists' gap"). The
  report reworded this honestly in pack A ("Strictly, that is not true"), but the Summary still states
  the gap without qualification ("The gap is real on current versions ... for any loader").
- **Demand.** The report rightly marks demand as thin, and the Reddit and Feedback sources as unverified.
  No demand claim is overstated in research.md. Watch the README, site and CurseForge copy: they must
  not cite the 86-point thread as fact. Regrowth's 6.59M downloads show that mods with a coral feature
  get installed, but they do not measure demand for coral.

## Injection sweep

I swept every page I fetched (Modrinth and CurseForge bodies, Hangar page, Spiget description, Regrowth,
Nature's Spirit, CarpetSkyAdditions and Lithium sources, Fabric API sources) for instruction-like text:
ignore or disregard instructions, system prompt, assistant or AI role text, model or vendor names.
**One hit, harmless:** the TeaksTweaks page says "Disregard this, it is just a side effect of modifying
recipes", which is a note to users and not aimed at an agent. The evidence packs contain no instruction
text. Pack A's own sweep note is accurate. Nothing was followed.

## Findings ledger

| # | Severity | Finding | Recommended fix |
|---|---|---|---|
| F1 | CRITICAL | The Summary and Q1 claim that no project makes coral spread without bone meal on 26.x "for any loader" is false. **Grassidium** (NeoForge 26.3, server-side, game rules, density cap) says "seagrass, kelp and corals spread on their own". This comes close to the stop rule ("a mod that already does natural reef growth ... on current versions"). The owner has not been told. | **Re-source and reword, then report to the owner before building**, as CLAUDE.md requires. Add Grassidium to the table. Restate the gap as: no Fabric mod and no reef-specific mod with conditions (light, substrate, biome), bleaching, recovery or caps tuned for reefs. Grassidium treats coral as one generic plant, its rules cover land plants, and it is closed source (all rights reserved), so how well it does this cannot be judged from the page. Offer options (differentiate on Fabric with a reef simulation, or stop) and log the owner's decision at G0/G1. Optionally watch its demo video or run it on NeoForge 26.3 to judge quality. |
| F2 | HIGH | **Carpet Sky Additions Reborn** (Fabric 26.2 and 26.3, server-side, 34k downloads, updated 2026-10-04) has `spreadingCoral`: calcite surrounded by 8 or more same-type coral blocks becomes coral, weighted by a climate noise. It is on the same loader and version and works without bone meal, yet the report does not mention it. | Add it to the table as "Partly: skyblock renewability, calcite only, no spread into water, no bleaching". Check its default value. It is also a design precedent for climate suitability without tags. |
| F3 | HIGH | **Regrowth** (mactso, Forge, Fabric and NeoForge up to 1.21.11, 6.59M CurseForge downloads, active branches) grows coral blocks, wall fans and pickles near fish and squid outside cold oceans. This contradicts "1.21.5 or later" and is the most installed precedent. A 26.x port is plausible given its port history. | Add it to the table ("Partly: mob-driven vertical stacking, client required, no spread or bleaching, not on 26.x"). Name it in the differentiation and as a compatibility risk (both mods could add coral in the same area). |
| F4 | HIGH | The method behind F1 to F3: competitor queries were confirmation-shaped (coral and reef words only), and wave 2 stopped too early. The stop-rule verdict rests on this search. | Re-run pack A with generic queries ("plant spread", "ecosystem", "regrowth", "skyblock", "renewable", "underwater plants"), a GitHub code search for `CoralBlock` with `randomTick` or `isRandomlyTicking`, and Spigot and Hangar directly. Record these in the evidence pack and claims ledger. |
| F5 | MEDIUM | Mixin collision. Nature's Spirit (10.6M downloads, up to 1.21.1) and improveMint add `randomTick` and `isRandomlyTicking` to `CoralBlock`. If Living Reefs adds the same methods, only one mod's version applies, and the choice depends on mixin priority. Q6 does not cover this. | Add it as an open risk in Q5 and Q6. In the spec, prefer a mixin that cooperates with others (for example `@Inject` at HEAD of the coral classes' existing methods, or a `BlockBehaviour#randomTick` injection guarded by `instanceof`). Note that an override in another mod that skips `super` still defeats a base-class hook. Add a compatibility test or a known-issues entry for Nature's Spirit when it reaches 26.x. |
| F6 | MEDIUM | The consequence of the light rule is not quantified. Water dims sky light by 1 per block (verified), and warm ocean reefs sit several to many blocks below sea level, so sky light at the reef is probably single digits. The depth is not measured in the report. A `minLight` gate may make natural reefs never grow, or a low value makes the rule cosmetic. | Measure actual sky and block light at worldgen reef positions (a GameTest or a sample of warm ocean chunks), then calibrate `minLight` or use depth to the water surface instead. State the measured distribution in the spec. |
| F7 | MEDIUM | The Lithium claim is checked only against the `develop` branch. The 26.3 release tag path returned 404 (its layout may differ), and no run with Lithium has been done. | Keep it as an open risk (already listed). Re-source from the 26.3 release jar or tag, or add a Lithium smoke run to the benchmark or GameTest plan. Source now confirms the flag source: `isRandomlyTicking() \|\| fluid.isRandomlyTicking()`. |
| F8 | LOW | The chunk-loading claim (C4) holds only for reads within 16 blocks horizontally. A larger radius can reach an unloaded chunk, and a server-thread `getBlockState` there loads the chunk synchronously. | Reword Q5 to "within 16 blocks horizontally (one chunk)". In the spec, cap the neighbour and density radius at 15 or guard with `getChunkNow`, and add a test. |
| F9 | LOW | Q1 says the search for "coral spread" "found nothing on either platform". On Modrinth it returns Bonemeal Renewable Coral. | Reword to "found only bone meal projects". |
| F10 | LOW | The game-rule claim (C2) is now confirmed against Fabric API 26.3 (`GAME_RULE` not SYNCED) and the vanilla command tree (literals). The only gap left is the live join test with an unmodified client. | Keep the release checklist item. Cite `FabricRegistryInit` on branch 26.3 as a source. |
| F11 | LOW | Planet Minecraft (403) and Modrinth plugin-only listings were not fully searched, so the competitor search still has a known blind spot. | List it under Limitations. |
