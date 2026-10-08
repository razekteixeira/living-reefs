# Evidence pack: B vanilla coral in 26.3
**Slice budget:** 38 / 40 tool calls  **Wave:** 1

Method: no sources jar and no decompiler (cfr, vineflower) on this machine, so code claims come from
`javap -p -c -constants` bytecode of the 26.3 game jar (Mojang names), plus the data files bundled in
the same jar (`data/minecraft/...`). The Minecraft Wiki was used only to cross-check. Code is tier 1
for "what 26.3 does"; the wiki is tier 2.

Jar: `~/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-common-deobf/26.3/minecraft-common-deobf-26.3.jar`
(client: `minecraft-clientonly-deobf-26.3.jar`).

## Candidate claims
1. No vanilla coral block, plant, fan or wall fan randomly ticks or grows. None of `CoralBlock`, `BaseCoralPlantTypeBlock`, `BaseCoralPlantBlock`, `BaseCoralFanBlock`, `BaseCoralWallFanBlock`, `CoralPlantBlock`, `CoralFanBlock` or `CoralWallFanBlock` defines `randomTick` or `isRandomlyTicking`. [C:CoralBlock] [C:CoralPlantBlock] [C:BaseCoral*] [S2] (tier 1, origin-independent yes)
2. Coral death runs only on a scheduled tick. If no water is found, `onPlace`, `updateShape` or `getStateForPlacement` schedules a tick after `60 + random.nextInt(40)` game ticks (60 to 99 ticks, 3.0 to 4.95 s). `tick()` checks again and swaps in the dead block with flag 2. [C:CoralBlock#updateShape, #getStateForPlacement, #tick] [C:BaseCoralPlantTypeBlock#tryScheduleDieTick] [S2][S3] (tier 1, yes)
3. The "has water" test is `scanForWater`. A coral block passes if any of its 6 neighbours has a fluid state in `#minecraft:water` (fluid tag). A plant, fan or wall fan passes if it is itself `waterlogged=true`, or failing that if any of its 6 neighbours has water fluid. It checks nothing else: no light, depth, biome, temperature or neighbouring coral. [C:CoralBlock#scanForWater] [C:BaseCoralPlantTypeBlock#scanForWater] (tier 1, yes)
4. Dead coral is final in vanilla. Dead variants have no tick logic that converts them back (the dead classes are the `BaseCoral*` bases, which have no `tick`). The wiki says "cannot be revived". [C:BaseCoralPlantBlock] [S2][S3] (tier 1, yes)
5. A living plant, fan or wall fan dies to its dead block with `WATERLOGGED=false`, and a wall fan keeps its `FACING`. [C:CoralPlantBlock#tick: setValue(WATERLOGGED, false)] [C:CoralWallFanBlock#tick] (tier 1, yes)
6. Plants and fans survive only on a block whose top face is sturdy (`below.isFaceSturdy(UP)`). Otherwise `updateShape` turns them into AIR. No substrate tag exists for coral, and there is no `supports_coral` tag in 26.3. [C:BaseCoralPlantTypeBlock#canSurvive, #updateShape] [D:tags/block listing] (tier 1, yes)
7. In 26.3 every ocean biome has `temperature` 0.5. The exception is the frozen oceans (0.0, with the `frozen` modifier). Biome temperature therefore cannot tell warm, lukewarm, ocean and cold oceans apart. Use biome identity or tags. [D:worldgen/biome/warm_ocean.json, lukewarm_ocean.json, deep_lukewarm_ocean.json, ocean.json, deep_ocean.json, cold_ocean.json, frozen_ocean.json] (tier 1, yes)
8. Underwater bone meal makes coral only in biomes in `#minecraft:produces_corals_from_bonemeal`, which contains only `minecraft:warm_ocean`. The code checks `BiomeTags.PRODUCES_CORALS_FROM_BONEMEAL`. [C:BoneMealItem#growWaterPlant] [D:tags/worldgen/biome/produces_corals_from_bonemeal.json] [S4] (tier 1, yes)
9. `growWaterPlant` makes 128 attempts. Attempt i takes i/16 random-walk steps of `(rand3-1, (rand3-1)*rand3/2, rand3-1)` and aborts if it reaches a full-collision block. In the coral biome, attempt 0 on a horizontal clicked face places a random `#wall_corals` facing that way. Other attempts have a 1-in-4 chance (`nextInt(4)==0`) of placing a random block from `#underwater_bonemeals` (seagrass, `#corals`, `#wall_corals`), and a wall coral rotates randomly, up to 4 tries, until it can survive. Everything else becomes seagrass. Placement needs a water block with fluid amount 8. Existing seagrass gets `performBonemeal` with probability 1/10. [C:BoneMealItem#growWaterPlant: sipush 128, bipush 16, iconst_4, bipush 10] (tier 1, yes)
10. Sea pickle bone meal works only if the pickle is waterlogged (`isDead` means not waterlogged) and the block below is in `#coral_blocks` (living only). It scans a diamond with taxicab radius 2 at Y−1 and Y. Each position gets a 1/6 chance (`nextInt(6)==0`), and needs water there plus a `#coral_blocks` block below it. Hits place a sea pickle with `pickles = nextInt(4)+1`, and the source is set to 4 pickles. [C:SeaPickleBlock#isValidBonemealTarget, #performBonemeal, #isDead] [S5] (tier 1, yes)
11. Kelp is the only aquatic plant that random-ticks. `GrowingPlantHeadBlock.randomTick` grows it if `age < 25` and `nextDouble() < growPerTickProbability`, and `KelpBlock.GROW_PER_TICK_PROBABILITY = 0.14`. There is no light check (no brightness call in KelpBlock or GrowingPlantHeadBlock). `isRandomlyTicking` is true only while `age < 25`. [C:KelpBlock] [C:GrowingPlantHeadBlock#randomTick, #isRandomlyTicking, MAX_AGE=25] (tier 1, yes)
12. Seagrass and tall seagrass never random-tick or spread. Seagrass grows only through bone meal, into tall seagrass. [C:SeagrassBlock] [C:TallSeagrassBlock] (tier 1, yes)
13. The vanilla aquatic blocks examined (coral family, sea pickle, kelp, seagrass) contain no light-level check. [C:* listed above] (tier 1, yes; based on the absence of brightness calls in the inspected classes)
14. Since 1.21.11 (25w44a) game rules are a built-in registry (`Registries.GAME_RULE`, `BuiltInRegistries.GAME_RULE`) with namespaced ids. The random tick rule is `minecraft:random_tick_speed`, an int with default 3 and min 0, in category UPDATES. [C:GameRules static init: ldc "random_tick_speed", iconst_3, iconst_0] [C:Registries#GAME_RULE] [S6] (tier 1, yes)
15. The game rule registry is static. It is not in the dynamic registry sync set, because `RegistrySynchronization.NETWORKABLE_REGISTRIES` is built from `RegistryDataLoader.SYNCHRONIZED_REGISTRIES`, which holds data-driven registries only. The game-rule packets carry `ResourceKey` ids as strings, not numeric ids. The vanilla client's `InWorldGameRulesScreen.onGameRuleValuesUpdated` looks each key up in its own `BuiltInRegistries.GAME_RULE` and skips null (unknown) keys. A server-only mod's game rule therefore should not crash a vanilla client's game-rules screen, and the `/gamerule` tree uses literal nodes plus the rule's own argument type (bool or int). [C:RegistrySynchronization] [C:ClientboundGameRuleValuesPacket STREAM_CODEC] [C:client InWorldGameRulesScreen#lambda$onGameRuleValuesUpdated$0: ifnull] [C:GameRuleCommand#buildRuleArguments] (tier 1, yes; not tested live, and Fabric API registry sync not checked, see Gaps)
16. Worldgen coral in 26.3 is data-driven. `warm_ocean_vegetation` is a `simple_random_selector` over 15 entries: for each of the 5 types, one `coral_tree`, one `coral_claw` and one "mushroom-like" offset entry. Each refers to `minecraft:coral/<type>_block`, which places the coral block and then `coral/block_decoration`. The `CoralMushroomFeature` class no longer exists; only `CoralTreeFeature` and `CoralClawFeature` remain as Java feature types. [D:worldgen/feature/warm_ocean_vegetation.json, worldgen/feature/coral/*.json] [C:levelgen/feature listing] (tier 1, yes)
17. Coral decoration on top of each worldgen coral block uses weights 20 for a random `#corals` (plant or fan), 3 for a sea pickle and 57 for nothing (25% total chance of something on top). Each of the 4 sides independently has a 0.2 chance of a random `#wall_corals` facing outward, if that spot is water. [D:worldgen/feature/coral/block_decoration.json] (tier 1, yes)
18. Only `warm_ocean` has `warm_ocean_vegetation` and `sea_pickle` features. Lukewarm and deep lukewarm have only seagrass and `kelp_warm`, so there are no vanilla reefs outside warm_ocean. [D:worldgen/biome/*.json features] (tier 1, yes)
19. There is no biome tag for "warm ocean" as such. The coral-relevant biome tags are `produces_corals_from_bonemeal` = [warm_ocean] and `spawns_coral_variant_zombie_nautilus` = [warm_ocean], plus the generic `is_ocean` and `is_deep_ocean`. [D:tags/worldgen/biome/*] (tier 1, yes)
20. The conduit has no block interaction with coral. It needs frame blocks from `#conduit_effect_block` (prismarine, prismarine bricks, sea lantern, dark prismarine). It activates at 16 or more frame blocks, gives `CONDUIT_POWER` to players in range, and kills hostiles at 42 or more frame blocks (`MIN_ACTIVE_SIZE=16`, `MIN_KILL_SIZE=42`, `KILL_RANGE=8`, `EFFECT_DURATION=13`). The effect range is 16 per 7 frame blocks, per the wiki; the bytecode shows `bipush 16`. [C:ConduitBlockEntity] [D:tags/block/conduit_effect_block.json] (tier 1 for constants, yes)
21. Sea pickle light is 6, 9, 12 or 15 for 1 to 4 pickles, and only when waterlogged. [S5] (tier 2, yes; not re-derived from code)
22. In 26.2, sulfur cubes (a new mob) can "absorb coral blocks". This is a new vanilla interaction that could remove reef blocks. [S3] (tier 2, yes; not checked in code)

## Block inventory (ids, alive/dead pairs)
The 5 types are tube, brain, bubble, fire and horn. Each type has 4 forms, each with a dead counterpart, giving 20 living and 20 dead ids (40 total).

| Form | Living id | Dead id | Class (living / dead) |
|---|---|---|---|
| Block | `minecraft:<t>_coral_block` | `minecraft:dead_<t>_coral_block` | `CoralBlock` / plain `Block` |
| Plant | `minecraft:<t>_coral` | `minecraft:dead_<t>_coral` | `CoralPlantBlock` / `BaseCoralPlantBlock` |
| Fan | `minecraft:<t>_coral_fan` | `minecraft:dead_<t>_coral_fan` | `CoralFanBlock` / `BaseCoralFanBlock` |
| Wall fan | `minecraft:<t>_coral_wall_fan` | `minecraft:dead_<t>_coral_wall_fan` | `CoralWallFanBlock` / `BaseCoralWallFanBlock` |

- The living class holds a `deadBlock` field, which is how vanilla maps living to dead. There is no reverse map, so a mod needs its own dead-to-living table.
- Plant, fan and wall fan states have `waterlogged` (default true). The wall fan adds `facing` (horizontal). Coral blocks have no properties.
- Related ids: `minecraft:sea_pickle` (`pickles` 1 to 4, `waterlogged`), `minecraft:kelp` (`age` 0 to 25), `minecraft:kelp_plant`, `minecraft:seagrass`, `minecraft:tall_seagrass` (`half`), `minecraft:conduit`.
- Shapes: plant column 12×15, fan column 12×4 (from `<clinit>` constants).

## Tags (exact contents)
Block tags (`data/minecraft/tags/block/`):
- `coral_blocks`: tube_coral_block, brain_coral_block, bubble_coral_block, fire_coral_block, horn_coral_block. Living only.
- `coral_plants`: tube_coral, brain_coral, bubble_coral, fire_coral, horn_coral. Living only.
- `corals`: `#coral_plants`, tube_coral_fan, brain_coral_fan, bubble_coral_fan, fire_coral_fan, horn_coral_fan.
- `wall_corals`: tube/brain/bubble/fire/horn `_coral_wall_fan`. Living only.
- `underwater_bonemeals`: seagrass, `#corals`, `#wall_corals`.
- `conduit_effect_block`: prismarine, prismarine_bricks, sea_lantern, dark_prismarine.
- `cannot_support_kelp`: magma_block. `cannot_support_seagrass`: magma_block. Both are new in 26.1.
- `washed_away_by_fluids` includes `#corals`, `#wall_corals`, tall_seagrass, kelp_plant, seagrass, kelp, sea_pickle.
- `mineable/pickaxe` includes every living and dead coral block and every dead plant, fan and wall fan. Living plants and fans are not listed.
- `blocks_motion_no_leaves` includes `#coral_blocks`, dead coral blocks, dead plants, fans and wall fans, and dried_kelp_block.
- `replaceable`, `replaceable_by_trees` and `replaceable_by_mushrooms` include seagrass and tall_seagrass.
- **No vanilla tag lists dead coral as a group.** A mod must define its own (e.g. `livingreefs:dead_coral_blocks`, `dead_corals`, `dead_wall_corals`).

Biome tags (`data/minecraft/tags/worldgen/biome/`):
- `produces_corals_from_bonemeal`: warm_ocean.
- `spawns_coral_variant_zombie_nautilus`: warm_ocean.
- `is_ocean`: `#is_deep_ocean`, frozen_ocean, ocean, cold_ocean, lukewarm_ocean, warm_ocean.
- `is_deep_ocean`: deep_frozen_ocean, deep_cold_ocean, deep_ocean, deep_lukewarm_ocean. There is no deep warm ocean.
- `spawns_warm_variant_farm_animals`: includes warm_ocean, lukewarm_ocean and deep_lukewarm_ocean (among land biomes).

## Death mechanics (exact)
- **Trigger points.** All three call `scanForWater` and, if it fails, schedule a block tick of `60 + nextInt(40)` ticks:
  - `CoralBlock`: `getStateForPlacement` and `updateShape`.
  - Plants, fans and wall fans: `onPlace` and `updateShape`, through `BaseCoralPlantTypeBlock.tryScheduleDieTick`.
- **Tick.** `tick()` re-checks `scanForWater`. If it is still false:
  - a coral block calls `level.setBlock(pos, deadBlock.defaultBlockState(), 2)`;
  - a plant or fan sets the dead state with `WATERLOGGED=false`;
  - a wall fan does the same and copies `FACING`.
- **Water test.** The fluid at the neighbour (or, for plants, the block itself if waterlogged) matches `FluidTags.WATER`. Flowing water counts, and so does any waterlogged neighbour.
- **Random tick speed.** `random_tick_speed=0` does not stop death, because death runs on a scheduled tick. The wiki agrees.
- **Survival.** Separately from death, a plant or fan loses support and becomes AIR in `updateShape` when the block below no longer has a sturdy top face.
- **Design implication.** Vanilla has no "bleaching" stage and no recovery. Any bleaching or recovery must be mod logic swapping between the two vanilla ids.
  - Living to dead reuses vanilla.
  - Dead to living needs a mod table, because there is no vanilla path.
  - A dead block next to water stays dead forever in vanilla.
  - A living block placed by a mod next to water stays alive with no further vanilla ticks.

## Bone meal and sea pickle mechanics (exact)
- **`BoneMealItem.growWaterPlant(stack, level, pos, face)`** runs when the clicked block is water (or when used on a sturdy surface with water above; see useOn).
  - Loop: `for i in 0..127`: start at `pos`, state = seagrass. Then, `for j < i/16`, step by `(nextInt(3)-1, (nextInt(3)-1)*nextInt(3)/2, nextInt(3)-1)`, and abort this attempt if the block there `isCollisionShapeFullBlock`.
  - Coral biome (`#produces_corals_from_bonemeal`):
    - When `i==0` and the face is horizontal, use a random block from `#wall_corals` with `FACING=face`.
    - Otherwise, with chance `nextInt(4)==0`, use a random block from `#underwater_bonemeals`.
  - A chosen wall coral gets up to 4 random horizontal facings until it `canSurvive`.
  - Place with `setBlockAndUpdate` only if the target is `Blocks.WATER` with fluid amount 8 and the state can survive.
  - If the target is already seagrass, call its `performBonemeal` with chance 1/10 (`nextInt(10)==0`), which makes tall seagrass.
- **Sea pickle `isValidBonemealTarget`.** Requires `!isDead(state)` (that is, waterlogged) and the block below in `#coral_blocks`. Dead coral blocks do not count.
- **Sea pickle `performBonemeal`.**
  - Scans a diamond with taxicab radius 2 around the pickle (X−2..X+2, Z rows 1, 3, 5, 3, 1 wide) at Y−1 and Y, skipping its own position.
  - Each position gets a 1/6 chance; it needs `Blocks.WATER` there and `#coral_blocks` below.
  - A hit places `sea_pickle[pickles = 1 + nextInt(4)]`.
  - Finally the source pickle is set to `pickles=4` with flag 2.
- **Vanilla coral growth.** Vanilla coral can only be "grown" by player bone meal in warm_ocean water. Nothing spreads by itself.

## Worldgen features (exact)
26.3 renamed the configured-feature folder to `worldgen/feature`, and coral generation is composed from data.
- **`placed_feature/warm_ocean_vegetation`.** Placement: `noise_based_count` (noise_factor 400.0, noise_to_count_ratio 20), then `in_square`, then `heightmap OCEAN_FLOOR_WG`, then `biome`.
- **`feature/warm_ocean_vegetation`.** A `simple_random_selector` with 15 entries.
  - For each of tube, brain, bubble, fire and horn there is one `coral_tree`, one `coral_claw` and one offset-stacked entry (the old "mushroom" shape is now expressed as data placement).
  - Each refers to `minecraft:coral/<t>_block` and is filtered by the predicate "(water OR `#corals`) AND water above".
- **`feature/coral/<t>_block`.** An `overlay` that places `<t>_coral_block`, then `coral/block_decoration`. The decoration is the 20/3/57 weighted top described in claim 17, plus a 0.2 chance per side of a wall coral when that side is water.
- **`CoralTreeFeature` and `CoralClawFeature`.** Java records that take a `Holder<PlacedFeature>`.
  - Tree: trunk height `nextInt(3)+1`, then branches: a shuffled subset (`nextInt(3)+2`) of horizontal directions, each `nextInt(5)+2` long, with a chance of `nextFloat() < 0.25` to step up.
  - Claw: a random horizontal direction, `nextInt(2)+2` arms, segments `nextInt(3)+3`, with the same 0.25 upward chance.
  - These numbers are read from bytecode constants, and the exact loop semantics are not fully reconstructed.
- **`placed_feature/sea_pickle`.** Placement: `rarity_filter` chance 16, `in_square`, `count` 20, a trapezoid offset of ±7 in X and Z, `heightmap OCEAN_FLOOR`, a filter for water, then `biome`. The feature places `sea_pickle` (waterlogged) with pickles uniform 1 to 4.
- **Seagrass.**
  - `seagrass_warm` uses `seagrass_short` (tall 30, short 70) with count 80 and ±7 offsets.
  - `seagrass_deep_warm` uses `seagrass_tall` (tall 80, short 20).
- **Kelp.**
  - `placed_feature/kelp_warm` (in lukewarm and deep lukewarm, not warm_ocean) uses `noise_based_count` (factor 80.0, ratio 80).
  - It requires water at the spot and above, a sturdy top below, and the block below not in `#cannot_support_kelp`.
  - `feature/kelp` is a `block_column` of 0 to 9 kelp_plant plus a kelp tip with age 20 to 23.
- **Biome features.**
  - warm_ocean: `warm_ocean_vegetation`, `seagrass_warm`, `sea_pickle`.
  - lukewarm_ocean: `seagrass_warm`, `kelp_warm`.
  - deep_lukewarm_ocean: `seagrass_deep_warm`, `kelp_warm`.
  - ocean and deep_ocean: seagrass plus `kelp_cold`.
- **Biome data is in the jar.** `data/minecraft/worldgen/biome/*.json` is bundled. The temperature values come from those files, and attributes such as `minecraft:visual/water_fog_color` (warm `#041f33`, lukewarm `#041633`) are visible there.

## Game rules in 26.3 (registry? synced?)
- **Package and type.** `net.minecraft.world.level.gamerules` contains `GameRules`, `GameRule<T>`, `GameRuleType`, `GameRuleCategory`, `GameRuleMap` and `GameRuleTypeVisitor`. Rules are `GameRule<T>` constants registered in `BuiltInRegistries.GAME_RULE` (key `Registries.GAME_RULE`).
- **Registration.** The private helpers are `registerInteger(name, category, default, min[, max][, flags])` and `register(...)`. A mod would register into the same registry, through Fabric API's game rule helper if one exists for 26.3; this is not checked here.
- **Random tick speed.** `RANDOM_TICK_SPEED` is `"random_tick_speed"`, `UPDATES`, default **3**, min **0**.
- **Not part of dynamic registry sync.** `RegistrySynchronization.NETWORKABLE_REGISTRIES` derives from `RegistryDataLoader.SYNCHRONIZED_REGISTRIES`, which lists data-driven registries only. `GAME_RULE` is a static built-in registry. `networkSafeRegistries` concatenates the STATIC layer for some uses, but static registries are never sent as registry data packets.
- **Packets.**
  - `ClientboundGameRuleValuesPacket` carries `Map<ResourceKey<GameRule<?>>, String>`, using `ResourceKey.streamCodec`, which writes an identifier and not a numeric id.
  - `ServerboundSetGameRulePacket$Entry` is the same (key and string).
  - The vanilla client handler forwards values only to an open `InWorldGameRulesScreen`. Its lambda calls `BuiltInRegistries.GAME_RULE.getValue(key)` and skips `null`, so unknown modded rules are ignored.
- **Command.** `/gamerule` builds a literal per rule and an argument from the rule's own `ArgumentType`: brigadier `bool` or `integer`, which vanilla clients understand.
- **Conclusion (code reading, not tested live).** A server-only mod can most likely add game rules without breaking vanilla clients. Two things still need a live check with a vanilla client: Fabric API registry sync and any client-side registry checks. Note too that 26.1 moved `game_rules` "to the data folder", per the wiki.

## Sources
- [C] 26.3 game jar bytecode, `minecraft-common-deobf-26.3.jar` (Mojang names), classes as cited (`net.minecraft.world.level.block.*Coral*`, `SeaPickleBlock`, `KelpBlock`, `GrowingPlantHeadBlock`, `SeagrassBlock`, `TallSeagrassBlock`, `net.minecraft.world.item.BoneMealItem`, `net.minecraft.world.level.levelgen.feature.CoralTreeFeature`, `CoralClawFeature`, `net.minecraft.world.level.block.entity.ConduitBlockEntity`, `net.minecraft.world.level.gamerules.GameRules`, `net.minecraft.core.RegistrySynchronization`, `net.minecraft.network.protocol.game.ClientboundGameRuleValuesPacket`, `ServerboundSetGameRulePacket`, `net.minecraft.server.commands.GameRuleCommand`), and the client jar `net.minecraft.client.multiplayer.ClientPacketListener`, `net.minecraft.client.gui.screens.options.InWorldGameRulesScreen`. Release 2026-09-15, accessed 2026-10-08, tier 1.
- [D] 26.3 bundled data in the same jar: `data/minecraft/tags/block/*.json`, `data/minecraft/tags/worldgen/biome/*.json`, `data/minecraft/worldgen/{biome,feature,placed_feature}/*.json`. Accessed 2026-10-08, tier 1.
- [S2] Minecraft Wiki, "Coral", https://minecraft.wiki/w/Coral, living page, accessed 2026-10-08, tier 2.
- [S3] Minecraft Wiki, "Coral Block", https://minecraft.wiki/w/Coral_Block, living page, accessed 2026-10-08, tier 2.
- [S4] Minecraft Wiki, "Bone Meal", https://minecraft.wiki/w/Bone_Meal, living page, accessed 2026-10-08, tier 2 (only the first 100k chars read).
- [S5] Minecraft Wiki, "Sea Pickle", https://minecraft.wiki/w/Sea_Pickle, living page, accessed 2026-10-08, tier 2.
- [S6] Minecraft Wiki, "Game rule", https://minecraft.wiki/w/Game_rule, living page, accessed 2026-10-08, tier 2.
- [S7] Minecraft Wiki, "Java Edition 26.3", https://minecraft.wiki/w/Java_Edition_26.3, released 2026-09-15, accessed 2026-10-08, tier 2 (first 100k chars only).
- [S8] Minecraft Wiki, "Java Edition 26.1", https://minecraft.wiki/w/Java_Edition_26.1, accessed 2026-10-08, tier 2 (first 100k chars only).

## Contradictions found (wiki vs code)
1. **Pickles per new colony.** The wiki says nearby coral blocks "can gain 1-3 new pickles" [S5]. Code places `nextInt(4)+1`, which is 1 to 4 per new colony. Code wins.
2. **Pickle spread area.** The Bone Meal page says pickles spread "up to 3 blocks away" and recommends a 7×7 area [S4]. Code uses taxicab radius 2 (a 5-wide diamond) at Y and Y−1, and the Sea Pickle page [S5] agrees with code. The S4 "2 blocks deep" water requirement is not in code either: code needs only `waterlogged` plus a coral block below.
3. **Coral bone meal substrate.** The wiki says coral generates from bone meal "on dirt, coarse dirt, sand, red sand, gravel, or clay" [S2]. Code has no substrate list. It needs water (amount 8) at the target, the placed state's `canSurvive` (a sturdy top below), and the biome tag.
4. **Death timing units.** The wiki gives seconds (3 to 4.95 s) and code gives ticks (60 + nextInt(40), so 60 to 99). They are consistent: 99 ticks is 4.95 s.
5. **Missing changelog.** No wiki history entry for 1.21.x to 26.3 mentions coral changes, yet code shows `CoralMushroomFeature` gone and coral worldgen recomposed as data (`coral_tree`/`coral_claw` taking a placed feature, `worldgen/feature` folder). The wiki changelog read was truncated, so the change is probably documented further down rather than truly missing.

## Gaps
- No decompiled source, so complex loops are read from bytecode. The exact shapes of the coral tree and claw (segment lengths) are approximate, and the replacement for the "mushroom" entry was not decoded beyond "offset with a uniform y". The sea pickle diamond was reconstructed from the loop structure (`iconst_5`, `j` += or −= 2) and matches the wiki.
- Fabric API: whether its registry sync covers `GAME_RULE` and would reject vanilla clients when a modded rule exists is not checked. It should be checked against the Fabric API version Throughput uses, with a live vanilla-client join test.
- `BoneMealItem.useOn` was not decoded: which clicked blocks route into `growWaterPlant`, and whether a sturdy face with water above is needed.
- Wall fan `canSurvive`, meaning the sturdiness of the attached face, was assumed by analogy and not decoded.
- The 26.2 sulfur cube coral absorption was not checked in code.
- Conduit effect range formula: only the constants were captured. The wiki's 16 blocks per 7 frame blocks was not re-verified.
- Changelogs for 1.21.2 to 1.21.11 were not read for aquatic changes beyond game rules (25w44a).
- Sea pickle light levels were taken from the wiki only.

## Injection sweep
All 7 wiki pages fetched (Coral, Sea Pickle, Bone Meal, Java Edition 26.3, Game rule, Coral Block, Java Edition 26.1) were checked for instruction-like text aimed at an AI. None was found. The only imperative text was wiki-maintenance notices addressed to human editors (e.g. "Please remove this notice once you have added suitable images"), which are not instructions to this agent. The jar data contains no prose.
