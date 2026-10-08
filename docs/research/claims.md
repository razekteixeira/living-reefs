# Claim ledger: Living Reefs research

- Report verified: `docs/research.md` (version present in the worktree on 2026-10-08).
- As of: 2026-10-08. Volatile facts (downloads, versions, dates) re-read on that date.
- Verifier: R3 Claim Verification, fresh context, did not write the report. Gate: G4 Claims with teeth.
- Method: factored verification. Only the `## Sources` sections of evidence packs A to D were read to
  resolve citation keys; the packs' claims and reasoning were not read. Code and data claims were
  checked against the decompiled 26.3 sources (`/Users/razekteixeira/.claude/jobs/f8f56cc0/tmp/mcsrc`)
  and the bundled `data/` of `minecraft-common-deobf-26.3.jar`. Web claims were re-fetched: Modrinth API,
  CurseForge via the curse.tools proxy (`/v1/cf/...`), Spiget API (SpigotMC mirror API), raw GitHub
  (FabricMC/fabric 26.3, NeoForge 26.3.x, Upgrade Aquatic 1.21.1, Lithium develop), Minecraft Wiki
  API, NOAA, Florida Museum, Bennington mirror PDF. Strictness: token boundary (scope, version, date,
  number).
- Budget: about 45 tool calls planned, 44 used.
- Not fetchable: Reddit (known 403, not attempted), feedback.minecraft.net (known 403, not attempted),
  PubMed (bot-check page returned), NOAA coral infographic C-S5 (HTTP 404 today).

Verdict legend: S supported, P partial, U unsupported (source does not say it or could not be
checked), C contradicted. Actions: keep, reword, re-source, cut, open-question.

| # | Claim (load-bearing?) | Citations | Fetched? | Verdict | Origin-independent | As-of date | Action |
|---|---|---|---|---|---|---|---|
| 1 | No mod, datapack or plugin on Modrinth or CurseForge makes coral spread over time without bone meal on 1.21.5+ or 26.x, any loader (yes, stop rule) | A-S1, A-S2 | Yes, re-run with 14 Modrinth and 20 CurseForge queries (see spot-check) | S | Yes: two independent platform indexes plus this re-run | 2026-10-08 | keep |
| 2 | improveMint is Fabric 1.21.4 with 99 downloads (yes) | A-S6 | Yes, Modrinth API | S (loaders `fabric`, game_versions `1.21.4`, downloads 99, updated 2025-09-16) | Single source (Modrinth) | 2026-10-08 | keep |
| 3 | GTCoralsThatGrow is Spigot 1.13, abandoned in 2019 (yes) | A-S8 | Yes, Spiget API for resource 59911 | S (tested 1.13, released 2018-08-16, updated 2019-05-22) | Single source | 2026-10-08 | keep |
| 4 | Neither improveMint nor GTCoralsThatGrow spreads a reef or bleaches it (yes) | A-S6, A-S8 | Yes | P: GTCoralsThatGrow supported (player-placed fan grows a structure, ignores natural coral). improveMint's 307-character description only says coral blocks "grow coral and coral fans over time"; absence of spreading/bleaching is inferred from a short text, source tree not seen (A-S7) | Single source each | 2026-10-08 | reword (finding 6) |
| 5 | Mod authors quote "I really want renewable coral" and "I wanted Corals to grow... so I made them grow" (no, colour) | A-S14, A-S8 | Yes | S (exact text in Modrinth body and Spigot tag line) | Two different authors | 2026-10-08 | keep |
| 6 | Feedback site has coral growth suggestions, seen only through search summaries (no) | A-S19 | No (403 known) | U, already flagged "Not verified" in the report | Tier 3 | n/a | keep (flag already present) |
| 7 | r/feedthebeast 1woasop top comment 86 points, September 2026 (no) | A-S20 | No (secondhand local note; Reddit 403) | U, already flagged "secondhand and unverified" | Single secondhand source | n/a | keep (flag already present) |
| 8 | Coral Up: Forge 1.20.1, tall and bushy coral from bone meal (yes) | A-S10, A-S11 | Yes, Modrinth API | S ("Adds bushy and tall coral, obtained by bonemealing", forge, 1.20.1) | Single platform checked | 2026-10-08 | keep |
| 9 | Corals: Reefier Reefs: Fabric 1.19, CurseForge only, 2022, bone meal advances and spreads coral (yes) | A-S9 | Yes, CF proxy mod 686300 and description | S (Fabric 1.19, created and last modified 2022-10-05, "use Bone Meal on any Corals, causing them to grow and spread"); "CurseForge only" consistent with my Modrinth searches (no hit) | Single source | 2026-10-08 | keep |
| 10 | Immortal Coral: Fabric/Quilt 26.3, game rule that stops coral dying, server-side (yes) | A-S12 | Yes, Modrinth API | S (fabric, quilt; 26.3 listed; "Gamerule to make coral never die"; "if it's installed on the server there is no need to have it installed client side"; namespaced game rule from 1.21.11) | Single source | 2026-10-08 | keep |
| 11 | Waxable Coral: Fabric/Forge/NeoForge/Quilt 26.2, honeycomb keeps coral alive out of water (yes) | A-S13 | Yes, Modrinth API | S (four loaders, latest 26.2, updated 2026-08-09, "Wax your coral! Stays alive even when it's dry!") | Single source | 2026-10-08 | keep |
| 12 | improveMint coral blocks sprout coral "similar to Amethyst buds" (yes) | A-S6 | Yes | S (quote exact) | Single source | 2026-10-08 | keep |
| 13 | Bonemeal Renewable Coral, Bone Meal Revival, Create: Aquatic Ambitions, craftable-coral datapacks are "various, up to 26.3" (no) | A-S14, A-S16, A-S17, A-S3 | Yes for the three named projects | P: Bonemeal Renewable Coral latest 1.21.11 (datapack); Bone Meal Revival Forge 1.20.1; Create: Aquatic Ambitions up to 1.21.1. None of the three reaches 26.3; the "26.3" can only come from the unnamed datapacks in A-S3, not re-checked | Single source each | 2026-10-08 | reword (finding 5) |
| 14 | Reef Redux, CoralReef, Warm Coral Reef, Tidal Terror do reef worldgen only (no) | A-S1, A-S18 | Yes (CF descriptions of 1665385 and 1726637, search hits for the others) | P: no growth in any (supported); Tidal Terror also adds mobs (sharks) and seafood, so "worldgen only" is too narrow | Single source each | 2026-10-08 | reword (finding 7) |
| 15 | Searches for "coral growth", "coral spread", "reef growth", "coral bleaching", "bleaching" found nothing on either platform (yes) | A-S1, A-S2 | Re-ran close variants | P: CurseForge "coral grow" returns Corals: Reefier Reefs (bone meal). "Found nothing" is only true as "found no natural-growth project" | Re-run independent | 2026-10-08 | reword (finding 8) |
| 16 | "Living Reefs" and slugs living-reefs, livingreefs, living-reef, living-coral return 404 on Modrinth and 0 on CurseForge, with working controls (yes) | A-S21, A-S22 | Yes, re-run | S (all four: Modrinth 404, CF slug 0; controls immortal-coral 200, waxable-coral 1) | Re-run independent | 2026-10-08 | keep |
| 17 | 20 living and 20 dead coral ids; living classes CoralBlock, CoralPlantBlock, CoralFanBlock, CoralWallFanBlock each with `deadBlock`; dead coral blocks are plain Block; plants and fans waterlogged, wall fans add facing (yes) | B-C | Yes, decompiled source | S (40 registrations; `private final Block deadBlock` in all four; DEAD_*_CORAL_BLOCK registered with properties only; BaseCoralWallFanBlock adds FACING and WATERLOGGED) | Primary code | 26.3 | keep |
| 18 | Tags coral_blocks, coral_plants, corals, wall_corals list living coral only; **no vanilla tag lists dead coral**; no tag maps dead to living (yes) | B-D | Yes, jar data | C for "no vanilla tag lists dead coral": `minecraft:mineable/pickaxe` and `minecraft:blocks_motion_no_leaves` each list all 20 dead coral ids. The four coral tags do list living only (S); no dead-to-living mapping (S) | Primary data | 26.3 | reword (finding 1) |
| 19 | No coral class random ticks; kelp is the only aquatic plant that does (14 % per tick, age cap 25); seagrass never spreads (yes) | B-C, D-S6, D-S7 | Yes | S (Blocks: only KELP among aquatic plants has `randomTicks()`; GROW_PER_TICK_PROBABILITY 0.14, MAX_AGE 25) | Primary code | 26.3 | keep |
| 20 | Death: `scanForWater` fails, scheduled tick 60 to 99 ticks, dead twin, plants/fans not waterlogged, wall fans keep facing; no light/depth/biome checks; random_tick_speed 0 does not stop death (yes) | B-C | Yes | S (`60 + random.nextInt(40)`; `setValue(WATERLOGGED, false)`; FACING copied; scheduled, not random, tick) | Primary code | 26.3 | keep |
| 21 | Plants and fans need a sturdy top face below, else air; no substrate tag (no) | B-C | Partly (tag list checked, `canSurvive` not opened) | P: no substrate tag supported; sturdy-face rule not re-checked within budget | Primary code | 26.3 | keep (low stakes) |
| 22 | Bone meal: only warm_ocean in `#produces_corals_from_bonemeal`, 128 attempts, one in four from `#underwater_bonemeals`, never coral blocks (yes) | B-C, R1 | Yes | P: all numbers supported; omits that the first attempt on a horizontal clicked face places a wall coral facing that face | Primary code and data | 26.3 | reword (finding 4) |
| 23 | Sea pickle bone meal spreads in radius-2 diamond onto living coral blocks; light 6/9/12/15 when waterlogged (no) | B-C, B-S5 | Yes | S (`3 + 3 * PICKLES`, 0 when dead; spread loop requires CORAL_BLOCKS below) | Code plus wiki | 26.3 | keep |
| 24 | Worldgen: only warm_ocean has reefs (tree, claw, stacked shape per type); **25 % chance of coral or a pickle** on top of each coral block; 20 % per side wall fan; data-driven, CoralMushroomFeature gone (yes) | B-D | Yes | P/C: biome, 5 coral_tree + 5 coral_claw + cuboid shape, 0.2 wall fan chance, no CoralMushroomFeature class are all S. The top roll is weighted 20 coral / 3 sea pickle / 57 nothing: coral 25 %, pickle 3.75 %, together 28.75 %. "25 % chance of coral or a pickle" is contradicted | Primary data | 26.3 | reword (finding 2) |
| 25 | Every non-frozen ocean has temperature 0.5 (yes) | B-D | Yes | S (warm, lukewarm, deep lukewarm, ocean, deep ocean, cold, deep cold all 0.5; frozen_ocean 0.0, deep_frozen_ocean 0.5) | Primary data | 26.3 | keep |
| 26 | Water dims sky light by one per block, so about 15 - d at depth d (yes) | R1 | Yes | S (`LiquidBlock#propagatesSkylightDown` false; `getLightDampening` returns 1 for non-solid, non-sky-passing) | Primary code | 26.3 | keep |
| 27 | Conduit activates with 16 or more frame blocks; no coral interaction (no) | B-C | Yes | S (`effectBlocks.size() >= 16`) | Primary code | 26.3 | keep |
| 28 | Reef-building coral needs warm, clear water and light; reefs mostly above about 20 m (no, grounding) | C-S1, C-S3 | Yes | P: C-S1 (NOAA text mirror) says reefs "limited to depths of less than 20 meters"; C-S3 says corals may live to 48 m (150 ft) with abundance falling with depth | Two sources, slightly different numbers | n.d. | keep ("mostly" covers it) |
| 29 | Growth is millimetres to centimetres a year (no) | C-S6, C-S7 | No (budget) | U (not re-checked) | n/a | n/a | open-question |
| 30 | Bleaching is expelling algae under heat stress; short bleaching recoverable, long kills (no) | C-S4, C-S5 | C-S4 yes; C-S5 404 | P: C-S4 NOAA podcast supports both halves ("if the event is mild enough and doesn't last too long, the corals are able to recover"). C-S5 URL now returns 404 | Effectively single source today | 2015-09 | re-source (finding 9) |
| 31 | Herbivorous fish protect new coral from algae (no, design input for fish rule) | C-S8 | No (PubMed bot check) | U: source resolves to a PubMed record whose abstract was only seen through a search summary (per the Sources entry) | Single, tier-limited | n/a | open-question (finding 10) |
| 32 | Restoration grows fragments in nurseries and plants them out (no) | C-S10, C-S11 | C-S10 yes | S (NOAA 2014-12-05: fragments brought into coral nurseries to restore reefs) | Two sources | 2014 | keep |
| 33 | Vines: fewer than 4 vines in a 9 x 9 x 3 box; `spread_vines` game rule (yes, cap design) | C-S13 | Yes, wiki and 26.3 code | P: wiki says "fewer than 4 **other** vine blocks"; code `atMostMatched(4)` over a box from (-4,-1,-4) to (4,1,4) counts the vine itself. `spread_vines` S | Wiki plus code | 26.3 | reword (finding 3) |
| 34 | One random tick per block per about 68 s at speed 3 (yes, rate) | C-S17, D-S1 | Code yes, wiki grep found no "68" | S by arithmetic from code (3 picks per 4096-block section per tick: 1365 ticks, 68.3 s) | Derived | 26.3 | keep |
| 35 | Game rules are a built-in registry since 1.21.11; not synced as registry data; rule packet carries identifiers, only to operators on request; vanilla client skips unknown keys; Fabric registry sync does not list game rules (yes, server-side decision) | B-C, D-S12, D-S13, C-S27, R1 | Yes | P: every 26.3 code part S (packet codec `ResourceKey.streamCodec(Registries.GAME_RULE)`; `sendGameRuleValues` needs COMMANDS_GAMEMASTER; `rule != null` check on client; no GAME_RULE in `RegistrySynchronization` or Fabric `FabricRegistryInit`). "Since 1.21.11" is not in any cited source; it belongs to C-S25/C-S26 (Immortal Coral's page corroborates "From 1.21.11 onwards") | Code plus Fabric source | 26.3 | re-source (finding 11) |
| 36 | Random ticks only in entity-ticking chunks, `random_tick_speed` positions per section with a ticking block, skipped while frozen (yes) | D-S1, D-S3, D-S4, D-S10 | Yes | S (`forEachBlockTickingChunk` iterates `forEachEntityTickingChunk`; `section.isRandomlyTicking()` then tickSpeed picks; `runsNormally()` gate) | Primary code | 26.3 | keep |
| 37 | `isRandomlyTicking` cached in `initCache()` during bootstrap, before tags (yes) | D-S8, R1 | Yes | S (`initCache` sets the field from the owner; called in the Blocks bootstrap loop) | Primary code | 26.3 | keep |
| 38 | Lithium replaces the random tick loop in sections with 384 or fewer ticking states; a block reporting `isRandomlyTicking` is still ticked (yes) | D-S16, D-S17 (summary: D-S16 only) | Yes, Lithium develop | P: content S (`(int)(4096 * 0.09375)` = 384; counts RANDOM_TICKING states; Lithium mc26.3-0.26.2 exists, 2026-09-28). Citation D-S16 is the chunk-loading API list, not Lithium; the summary bullet cites only D-S16 for Lithium compatibility | Single upstream source | 2026-10-05 push | re-source (finding 12) |
| 39 | Neighbour reads within a chunk stay in loaded chunks because chunks next to an entity-ticking chunk are loaded (yes) | D-S1, D-S5, D-S15 | Yes | P: content S (ChunkLevel entity 31, block 32, full 33). D-S15 is Throughput's saved-data code and does not support it | Primary code | 26.3 | re-source (finding 13) |
| 40 | Neither Fabric nor NeoForge convention tags have a coral tag; Upgrade Aquatic adds its coral to vanilla tags (yes) | C-S18, C-S19, C-S20 | Yes | S (no "coral" in ConventionalBlockTags 26.3 or NeoForge Tags 26.3.x; Upgrade Aquatic `data/minecraft/tags/block/corals.json` lists `upgrade_aquatic:*` fans) | Three repos | 2026-10-08 | keep |
| 41 | Biomes O' Plenty and Terralith place dry dead coral on land; Tectonic changes ocean depth; none adds warm reef biomes (yes, revival rule) | C-S21, C-S22, C-S23 | No (budget) | U (not re-checked by this verifier) | n/a | n/a | open-question (re-check before spec freeze) |
| 42 | Random ticks honour `random_tick_speed` and `/tick freeze` (servers section) | D-S10, C-S29 | Code yes | P: content S (see 36). C-S29 is the Nether Portal Spread Modrinth page and does not support it | Primary code | 26.3 | re-source (finding 14) |

Tally (42 claims): S 22, P 14, U 5, C 1 (claim 24 is counted as P but contains one contradicted number; see finding 2).

## Gap spot-check (queries and results)

Run 2026-10-08 with `/tmp/lr-verify/scripts/gap.py` (Modrinth `/v2/search`, limit 12; CurseForge proxy
`/v1/cf/mods/search`, gameId 432, classId 6 mods and 6945 data packs, sorted by popularity).

Modrinth (14 queries): "coral grow" 0, "coral regrow" 0, "living coral" 1 (coral-revitalization: craft
dead coral with dye, 1.20.6), "ocean ecosystem" 1 (unrelated), "coral reef" 9, "coral bleach" 0,
"growing coral" 0, "coral spreading" 0, "reef restoration" 0, "coral propagation" 0, "natural coral" 1
(neoreefredux, worldgen, 1.21.1), "coral regeneration" 0, "coral spread naturally" 0, "reef" 16.
The "coral reef" and "reef" hits are worldgen, decoration, fish, panoramas or unrelated libraries:
coral-reef (b1.7.3 port), creative-superflat-coral-reef-worldgen (26.1.2), rainbow-reef (Forge 1.20.1,
966,605 downloads, fish and aquarium content; body has no grow/spread/bleach text), coralreef (1.19.4),
reef-redux (1.20.2), reefdecor, neoreefredux, spongey-reefs (26.3, sponge worldgen), recoraled-reef,
reef (worldgen library 1.21.11).

CurseForge (10 queries x 2 classes): "coral grow" mods 1 (corals-reefier-reefs, bone meal, 1.19), data
packs 0; "coral regrow" 0/0; "living coral" 2/1 (coral-revitalization, shearable-coral); "ocean
ecosystem" 0/0; "coral reef" 8/0 (coralreef, reefdecor, recoraled-reef, reef-redux, reefier reefs,
tidal-terror, spongey-reefs, warm-coral-reef); "coral bleach" 0/0; "growing coral" 0/0; "coral
regeneration" 0/0; "reef" 17/1 (fish, structures, tree farms; nothing coral-growing); "coral spread" 0/0.

Result: no project on either platform makes coral grow or spread on its own on 1.21.5+ or 26.x. The
competitor gap claim (1) holds. New names not in the report's table (coral-revitalization,
shearable-coral, rainbow-reef, spongey-reefs, neoreefredux, recoraled-reef, reefdecor) are crafting,
decoration, fish or worldgen. They do not change the conclusion. Not searched: Planet Minecraft,
Hangar, SpigotMC search (out of budget).

## Findings requiring changes (numbered, with exact suggested wording fix)

1. Claim 18, contradicted. Replace "list living coral only. No vanilla tag lists dead coral, and no tag
   maps dead to living." with: "list living coral only. Dead coral appears only in utility tags such as
   `minecraft:mineable/pickaxe` and `minecraft:blocks_motion_no_leaves`; no coral-family tag lists dead
   coral, and no tag maps dead to living."
2. Claim 24, contradicted number. Replace "with a 25 % chance of coral or a pickle on top of each coral
   block" with: "with a 25 % chance of coral and a 3.75 % chance of a sea pickle on top of each coral
   block".
3. Claim 33. Replace "(vines: fewer than 4 vines in a 9 x 9 x 3 box)" with: "(vines: fewer than 4 other
   vines in a 9 x 9 x 3 box)".
4. Claim 22. Replace "with a one in four chance; it never places coral blocks." with: "with a one in
   four chance (the first attempt on a side face places a wall fan instead); it never places coral
   blocks."
5. Claim 13. In the table row for Bonemeal Renewable Coral, Bone Meal Revival, Create: Aquatic Ambitions
   and craftable-coral datapacks, replace "various, up to 26.3" with "various, up to 1.21.11 (named
   projects)".
6. Claim 4. Replace "Neither spreads a reef or bleaches it." with: "Neither description mentions
   spreading coral to new blocks or bleaching."
7. Claim 14. In the table row for Reef Redux, CoralReef, Warm Coral Reef, Tidal Terror replace "Reef
   worldgen only" with "Reef worldgen (Tidal Terror also adds mobs); no growth".
8. Claim 15. Replace "found nothing on either platform" with: "found no project with natural growth on
   either platform (CurseForge returns Reefier Reefs, which is bone meal only)".
9. Claim 30. Re-source: C-S5 returns HTTP 404 on 2026-10-08. Keep C-S4 as the citation, and either
   replace C-S5 with an archived copy or drop it: "[C-S4]" instead of "[C-S4][C-S5]" (both in Q3 prose
   and in the Bleaching row of the design table).
10. Claim 31. The fish claim rests on an abstract seen only through a search summary. Change "herbivorous
    fish protect new coral from algae [C-S8]" to "herbivorous fish are reported to protect new coral
    from algae (abstract only) [C-S8]", or fetch the abstract and keep as is.
11. Claim 35. Add the history source: "Since 1.21.11 game rules are a built-in registry [C-S25][C-S26]."
    The remaining citations stay on the following sentences.
12. Claim 38. Summary bullet: replace "Lithium compatibility for free. [D-S1][D-S4][D-S8][D-S16]" with
    "[D-S1][D-S4][D-S8][D-S17]". Q5 bullet: replace "[D-S16][D-S17]" with "[D-S17][D-S18]".
13. Claim 39. Replace "[D-S1][D-S5][D-S15]" with "[D-S1][D-S3][D-S5][D-S16]".
14. Claim 42. Replace "[D-S10][C-S29]" with "[D-S1][D-S10]" (or cite whatever C-S29 was meant to support;
    as listed it is the Nether Portal Spread mod page).
15. Claims 29 and 41 were not re-checked within budget (growth rates; BoP/Terralith/Tectonic). They are
    open questions for the sceptic pass, not changes.

## Injection sweep

Fetched content (Modrinth project bodies and search hits, CurseForge descriptions and search hits, Spiget
description, GitHub raw files, Minecraft Wiki wikitext, NOAA and Florida Museum pages, Bennington PDF,
PubMed bot-check page, NOAA 404 page) was read as data only. No instruction-like text addressed to an AI
agent or reader was found. Mod descriptions contain ordinary promotional text and links (for example
GTCoralsThatGrow links to an external site for images); none were followed. Reddit and the Minecraft
Feedback site were not fetched.

## Claims added after the sceptic pass (rows 43 to 48)

Not verified by R3. Checked by the R4 sceptic (fetched sources in `sceptic.md`) and by the research
lead on 2026-10-08 ([R2] in the report's provenance annex).

| # | Claim (load-bearing?) | Citations | Fetched? | Verdict | Origin-independent | As-of date | Action |
|---|---|---|---|---|---|---|---|
| 43 | Grassidium (NeoForge only, up to 26.3, closed source) makes seagrass, kelp and corals spread on their own, with a density cap; no bleaching or recovery described (yes) | Sk-F1, R2 | yes (Modrinth API by sceptic and lead) | supported | no (one project page) | 2026-10-08 | keep |
| 44 | Carpet Sky Additions Reborn (Fabric 26.3) has a `spreadingCoral` rule turning calcite into coral (yes) | Sk-F2 | yes (sceptic) | supported; default value not checked | no | 2026-10-08 | keep, default listed as limitation |
| 45 | Regrowth (up to 1.21.11) lets fish and squid grow coral blocks, wall fans and pickles; client and server (yes) | Sk-F3 | yes (sceptic read the source) | supported | yes (description and source) | 2026-10-08 | keep |
| 46 | Nature's Spirit and improveMint add `randomTick` to `CoralBlock` (no) | Sk-F5 | yes (sceptic) | supported | no | 2026-10-08 | keep |
| 47 | Generic Modrinth searches restricted to 26.1 to 26.3 found no further project that spreads coral (yes) | R2 | yes (lead) | supported | yes (sceptic ran overlapping generic queries) | 2026-10-08 | keep |
| 48 | The owner chose to differentiate after the stop rule was raised (yes) | `docs/sdlc-log.md` | n/a | supported (recorded answer) | n/a | 2026-10-08 | keep |
