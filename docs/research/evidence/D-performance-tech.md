# Evidence pack: D performance and server technique
**Slice budget:** 44 / 40 (4 over: the worktree guard refused several compound shell commands, which then had to be split up)  **Wave:** 1

Method: bytecode disassembly (`javap -p -c`) of the 26.3 game jar `minecraft-common-deobf-26.3.jar` (Mojang names) and
`minecraft-clientonly-deobf-26.3.jar`. No sources jar and no Vineflower were found in the Gradle caches, so every code claim
below comes from bytecode. Fabric API facts come from the module jars that the `fabric-api` 0.162.0+26.3 POM resolves to.
Throughput files were only read. Web: Lithium's repository (source fetched raw from GitHub) and the Modrinth API.

## Candidate claims

1. Random ticks are driven from `ServerChunkCache#tickChunks(ProfilerFiller)`. It reads `GameRules.RANDOM_TICK_SPEED` once per tick, then calls `ChunkMap#forEachBlockTickingChunk(chunk -> level.tickChunk(chunk, randomTickSpeed))`. [S1] (tier 1, primary code)
2. `random_tick_speed` is registered in `GameRules.<clinit>` as `registerInteger("random_tick_speed", category, 3, 0)`: **default 3, minimum 0**. [S2] (tier 1)
3. Despite its name, `ChunkMap#forEachBlockTickingChunk` delegates to `DistanceManager#forEachEntityTickingChunk`. That method iterates `SimulationChunkTracker.chunks` and keeps the chunks where `ChunkLevel.isEntityTicking(level)` holds (`level <= 31`). The lambda then skips chunks whose `ChunkHolder#getTickingChunk()` is null. So random ticks run only in entity-ticking chunks: inside the **simulation distance** of a player, or in force-loaded or ticket-held chunks at level 31 or lower. [S1][S3] (tier 1)
4. `ServerLevel#shouldTickBlocksAt(long)` uses `DistanceManager#inBlockTickingRange`, which is `ChunkLevel.isBlockTicking(level)` (`level <= 32`). That is a wider ring than the random-tick ring (31): it governs scheduled block ticks, not random ticks. [S3][S4] (tier 1)
5. `ServerLevel#tickChunk(LevelChunk, int randomTickSpeed)` works in three steps. First it runs `randomTickSpeed` precipitation attempts, each with a 1 in 48 chance (`random.nextInt(48)==0`, profiler "iceandsnow"). Then, for each `LevelChunkSection` with `isRandomlyTicking()`, it picks `randomTickSpeed` positions with `getBlockRandomPos(minX, sectionMinY, minZ, 15)`. For each position it reads `section.getBlockState(x,y,z)`, calls `BlockState.randomTick(level,pos,random)` if `state.isRandomlyTicking()`, and also random-ticks the `FluidState` if that is randomly ticking. [S4] (tier 1)
6. `LevelChunkSection#isRandomlyTicking()` is `tickingBlockCount > 0 || tickingFluidCount > 0`. Both counters are kept up to date in `setBlockState(..)` and rebuilt by `recalcBlockCounts()`. A section with no randomly ticking state costs one boolean check per tick. [S5] (tier 1)
7. Coral does **not** random tick in 26.3. `CoralBlock`, `CoralPlantBlock`, `CoralFanBlock` and `CoralWallFanBlock` override only `tick` (the scheduled die tick), plus `onPlace`, `scanForWater` and `updateShape`-type helpers. None overrides `randomTick` or `isRandomlyTicking`. The `Blocks.<clinit>` registrations from `DEAD_TUBE_CORAL_BLOCK` through `SEA_PICKLE` contain no `Properties.randomTicks()` call, whereas `KELP` has one. Dead coral *blocks* are plain `Block` instances (`register(BlockItemId, Properties)`). [S6][S7] (tier 1)
8. `BlockState.isRandomlyTicking()` returns a **cached field**. `BlockStateBase#initCache()` sets it from `Block.isRandomlyTicking(BlockState)`, and the default implementation of that returns `Properties.isRandomlyTicking` (set by `randomTicks()`). `initCache()` runs for every registered state during bootstrap (an iteration over `Block.BLOCK_STATE_REGISTRY`). Two things follow. (a) A mixin that overrides `isRandomlyTicking(BlockState)` on the coral classes takes effect, because mixins apply at class load, before `initCache`. (b) The flag **cannot depend on tags**, because tags are bound later, at datapack load. [S8][S7] (tier 1)
9. Fabric API itself has to refresh this cache when it changes tick behaviour after bootstrap: `fabric-content-registries-v0` ships `OxidizableBlocksRegistryImpl$RandomTickCacheRefresher`. [S9] (tier 1, class name only; body not read)
10. Freeze gating: `ServerChunkCache#tickChunks()` returns early in a debug world, and calls `tickChunks(profiler)`, which holds spawning and random ticks, only when `level.tickRateManager().runsNormally()`. Change broadcasting still runs every tick. `TickRateManager#runsNormally()` returns the field `runGameElements`. `isFrozen()` and `isSteppingForward()` (`frozenTicksToRun > 0`) also exist. [S1][S10] (tier 1)
11. Fabric API 0.162.0+26.3 has **no random-tick event**. A class-name scan of all 45 module jars that resolve from the 0.162.0 POM (47 artifacts listed) found only the internal `RandomTickCacheRefresher`. [S9] (tier 1)
12. Fabric API 0.162.0+26.3 still ships `fabric-game-rule-api-v1` 4.0.10. Its builder is `GameRuleBuilder.forInteger(int).range(min,max).category(GameRuleCategory).buildAndRegister(Identifier)`, which returns `net.minecraft.world.level.gamerules.GameRule<T>`. `forBoolean`, `forDouble` and `forEnum` also exist, and change callbacks come from `GameRuleEvents.changeCallback(rule)`. [S11] (tier 1)
13. In 26.3, game rules are a registry: `BuiltInRegistries.GAME_RULE` / `Registries.GAME_RULE`, with classes in `net.minecraft.world.level.gamerules`. `ClientboundGameRuleValuesPacket(Map<ResourceKey<GameRule<?>>, String>)` encodes keys by identifier, not by numeric id. The server sends it only from `ServerGamePacketListenerImpl#sendGameRuleValues()`, on request, to players with `Permissions.COMMANDS_GAMEMASTER`. The vanilla client applies it in `InWorldGameRulesScreen#lambda$onGameRuleValuesUpdated$0`, which calls `BuiltInRegistries.GAME_RULE.getValue(key)` and **skips null**, so an unknown modded rule is silently ignored. [S12][S13] (tier 1)
14. Saved data in 26.3 uses `record SavedDataType<T extends SavedData>(Identifier id, Supplier<T> constructor, Codec<T> codec, DataFixTypes dataFixType)`. Data is stored in `net.minecraft.world.level.storage.SavedDataStorage` (there is no `DimensionDataStorage`). Server-wide data comes from `MinecraftServer#getDataStorage()` and per-level data from `ServerLevel#getDataStorage()` (via `ServerChunkCache#getDataStorage()`). Throughput passes `null` for `DataFixTypes` and has shipped that way. [S14][S15] (tier 1)
15. `ServerChunkCache#getChunkNow(int,int)` returns null off the main thread. On the main thread it checks a 4-entry cache, then `getVisibleChunkIfPresent(pos)` and `ChunkHolder#getChunkIfPresent(ChunkStatus.FULL)`. It never schedules a load. `LevelReader#hasChunkAt(BlockPos)` / `hasChunksAt(...)` and `ServerLevel#isPositionEntityTicking(BlockPos)` also exist. `LevelChunkSection#maybeHas(Predicate<BlockState>)` tests the palette, so a section that cannot contain coral is rejected without scanning 4096 blocks. [S16][S5] (tier 1)
16. Lithium's `mixin.world.chunk_ticking.random_block_ticking` (`ServerLevelMixin`) applies a MixinExtras `@ModifyExpressionValue` to the loop-start constant `0` in `ServerLevel#tickChunk`. When a section has 384 or fewer randomly ticking states (`4096 * 0.09375`), it replaces the vanilla loop with its own: it draws an index in [0,4096) and, if the index is below the count, calls `RandomTickingSectionDataHelper.randomTickNthBlock(...)`. Above 384 it runs the vanilla loop. Consequence: a third-party mixin on the `BlockState.randomTick` call site inside `tickChunk` would **not fire** in sparse sections when Lithium is installed. Coral reef sections are usually sparse. [S17][S18] (tier 2, upstream `develop` branch; Lithium `mc26.3-0.26.2-fabric` exists [S19])
17. Lithium keeps its counts with `BlockStateFlags.RANDOM_TICKING` and `LevelChunkSection$BlockCounterMixin`. The design comment says it does not change the random distribution: the chance is N in 4096, and N comes from the state flag. A coral class that reports `isRandomlyTicking=true` at bootstrap is therefore counted and ticked by Lithium as well. [S17] (tier 2; this is an inference that the flag source is `BlockState.isRandomlyTicking`, as `BlockStateFlags` was not read)
18. Throughput's benchmark starts a dev server with a random RCON password on 127.0.0.1 (flat world) and builds the workload with RCON (forceload, then `fill`). It waits 40 s to warm up, takes 15 readings 1.1 s apart of a **mod-internal measurement of the operation itself** ("last sample" in ms), and prints n/min/median/max. `rcon.py` is a stdlib RCON client; the port comes from `RCON_PORT`, defaulting to 25575. [S20] (tier 1)
19. MSPT probes available in 26.3: `MinecraftServer#getAverageTickTimeNanos()`, `getTickTimesNanos()` (a `long[]` ring), `getCurrentSmoothedTickTime()`, plus `/tick query`. [S21] (tier 1)
20. spark (lucko) is the community-standard profiler. `1.10.187-fabric` supports 26.3 (published 2026-09-19), and the project has about 23.2M Modrinth downloads. [S22] (tier 2)

## Random tick mechanics (exact)

```
MinecraftServer tick -> ServerLevel.tick -> ServerChunkCache.tick(BooleanSupplier, boolean tickChunks)
  ServerChunkCache.tickChunks():
      if level.isDebug(): return
      if level.tickRateManager().runsNormally(): tickChunks(profiler)   // frozen => no random ticks
      broadcast changed chunk holders (always)
  ServerChunkCache.tickChunks(profiler):
      spawnState, spawnMobs = gameRules.get(SPAWN_MOBS)
      randomTickSpeed = gameRules.get(RANDOM_TICK_SPEED)          // default 3, min 0
      ... natural spawning over collectSpawningChunks (shuffled) ...
      chunkMap.forEachBlockTickingChunk(c -> level.tickChunk(c, randomTickSpeed))
          = DistanceManager.forEachEntityTickingChunk: SimulationChunkTracker level <= 31
            and ChunkHolder.getTickingChunk() != null
      if spawnMobs: level.tickCustomSpawners(spawnEnemies)
  ServerLevel.tickChunk(chunk, n):
      repeat n: if random.nextInt(48)==0: tickPrecipitation(random column)
      for section in chunk.getSections():
          if section.isRandomlyTicking():        // tickingBlockCount>0 || tickingFluidCount>0
              repeat n:
                  pos = getBlockRandomPos(minX, sectionMinY, minZ, 15)
                  state = section.getBlockState(local)
                  if state.isRandomlyTicking(): state.randomTick(level, pos, random)
                  fluid = state.getFluidState(); if fluid.isRandomlyTicking(): fluid.randomTick(...)
```

Coral rate if it random ticked: in a section with `C` coral states, the expected number of coral random ticks per game tick is
`3*C/4096`. Each coral block is picked about once every `4096/3 = 1365` ticks, or 68 s at 20 TPS. The expected interval does
not depend on C, but variance is high (geometric).

Cost if coral is made randomly ticking: reef sections that are not already ticking (warm oceans have no kelp, and water does
not random tick) become ticking sections. Each costs `randomTickSpeed` (3) palette reads per tick, the same as any
grass-covered land section. On top of that, the coral handler runs about `3*C/4096` times per section per tick. The handler
must therefore be O(1), with a probability gate first and neighbour reads after.

## Driving options compared

| Option | Cost model | Compat risk | Vanilla-client safe? |
|---|---|---|---|
| (a) Mixin on coral classes (`CoralBlock`, `CoralPlantBlock`, `CoralFanBlock`, `CoralWallFanBlock`; dead variants only if needed) adding `isRandomlyTicking(BlockState)=true` and `randomTick(..)` | Native: only chunks within simulation distance, scaled by `randomTickSpeed`, stops on `/tick freeze`, follows `/tick rate`. 3 reads per reef section per tick plus `3C/4096` handler calls. No bookkeeping or persistence of "where reefs are". | Low. Lithium honours it, because it counts states by the cached flag. Another mod that also overrides coral `randomTick` would conflict (use `@Inject` at HEAD/TAIL, not `@Overwrite`). The decision cannot read tags (it is fixed at bootstrap): other mods' coral needs its own mixin, or the scanner. Dead coral *block* is plain `Block`, so it cannot be targeted by class. | Yes. Random ticks are server-only, and block state ids are unchanged. |
| (b) Budgeted scanner: `ServerTickEvents.END_LEVEL_TICK`, round-robin over loaded chunks, `getChunkNow` + `section.maybeHas(tag predicate)`, N sections per tick | Fully controllable: hard cap of sections or µs per tick, spread across ticks. It must reimplement the ticking-range check (`isPositionEntityTicking` / `shouldTickBlocksAt`), the `runsNormally()` freeze check, and chunk-border safety. Iterating loaded chunks needs a list kept from `ServerChunkEvents.CHUNK_LOAD/UNLOAD`. | None with Lithium. Works with tags, so other mods' coral is covered by data alone. | Yes |
| (c) Mixin into the `ServerLevel#tickChunk` random tick loop (call site of `BlockState.randomTick`) | Same as (a) | **High.** Lithium's `@ModifyExpressionValue` bypasses the vanilla loop in sections with 384 or fewer ticking states (typical reefs), so the hook would silently not fire. Paper-style forks also rewrite this loop. | Yes |
| (d) Hybrid: (a) for vanilla coral, plus (b) as a slow, tag-driven scanner for modded coral and for bleaching/recovery evaluation | Sum of both, with the scanner budget small | Low | Yes |

## Fabric API 0.162.0+26.3 relevant modules and events

The versions below are resolved from the `fabric-api-0.162.0+26.3.pom`.

- `fabric-lifecycle-events-v1` 4.1.9+ffef5f675d: `ServerTickEvents` (`START_SERVER_TICK`, `END_SERVER_TICK`, `START_LEVEL_TICK`, `END_LEVEL_TICK`, with `EndLevelTick#onEndTick(ServerLevel)`); `ServerChunkEvents` (`CHUNK_LOAD` with `Load#onChunkLoad(ServerLevel, LevelChunk, boolean)`, `CHUNK_GENERATE`, `CHUNK_UNLOAD`, `FULL_CHUNK_STATUS_CHANGE`); `ServerLevelEvents` (`LOAD`, `UNLOAD`); `ServerLifecycleEvents`; `ServerBlockEntityEvents`; `ServerEntityEvents`; `CommonLifecycleEvents`. Note the renamed classes: `ServerLevelEvents`, not `ServerWorldEvents`.
- `fabric-game-rule-api-v1` 4.0.10+3434d6d95d (environment `*`, mixins on `GameRule`, `GameRuleCommand`, `GameRulesService` (JSON-RPC), `MinecraftServer`): `GameRuleBuilder` (`forBoolean`, `forInteger`, `forDouble`, `forEnum`; `.range`, `.minValue`, `.category`, `.codec`, `.argumentType`, `.buildAndRegister(Identifier)`), `GameRuleEvents.changeCallback(GameRule<T>)`, `FabricGameRuleTypeVisitor`.
- `fabric-data-attachment-api-v1` 2.2.31: `AttachmentRegistry.createPersistent(Identifier, Codec<A>)`, `create`, `createDefaulted`, `builder()`; `AttachmentType`, `AttachmentTarget` (chunks, entities, block entities, levels), `AttachmentSyncPredicate`, `GlobalAttachments`. This is an alternative to SavedData for per-chunk reef state that is saved inside the chunk itself.
- `fabric-block-api-v1` 3.1.0: `FabricBlock`, `FabricBlockState`, `BlockFunctionalityTags`, `FluidFlowEvents` (no tick hooks).
- `fabric-gametest-api-v1` 4.0.33, `fabric-client-gametest-api-v1` 6.0.8, `fabric-command-api-v2` 3.1.2, `fabric-permission-api-v1` (present), `fabric-events-interaction-v0` 5.3.7 (bone-meal and use hooks for tools).
- No random-tick event exists in any module.

## Tick freeze, saved data, game rules (exact)

**Freeze.** Vanilla gates random ticks with `level.tickRateManager().runsNormally()` (the field `runGameElements`), so it is false
while frozen unless stepping. Option (a) inherits this automatically. A scanner (b) must call
`if (!level.tickRateManager().runsNormally()) return;` itself. Whether Fabric's `END_LEVEL_TICK` still fires while frozen was
not verified; assume it does and guard anyway. `/tick rate` changes how often game ticks run, so per-tick work scales with it
in both options.

**Saved data.** `new SavedDataType<>(Identifier.fromNamespaceAndPath(MOD_ID, "reefs"), ReefSavedData::new, CODEC, null)`; storage
is `server.getDataStorage()` (written to `<world>/data/<ns>/<path>.dat`) or `level.getDataStorage()` for per-dimension data.
Throughput's pattern is the one to reuse:
- `VersionedState`: a `format` int. Its codec always decodes: on a newer format or a parse error it **quarantines** the raw `Dynamic`
  and encodes it back verbatim. `Codec.of(encode, decode)`, partial results are never used, and RuntimeExceptions are caught.
- `get(server)`: if `storage.get(TYPE)` is null but the file exists, Minecraft could not read it (for example after truncation).
  The code then backs it up to `*.unreadable-<timestamp>`, installs a read-only instance, `setDirty(false)`, and refuses to
  encode.
- Alternative: a Fabric `AttachmentRegistry.createPersistent` on chunks keeps reef state inside chunk NBT. Nothing is global,
  and it is unloaded with the chunk. The cost is a weaker equivalent of the unreadable-file guard.

**Game rules.** Server-only custom rules are safe at the protocol level for vanilla clients:
- The rule registry is static (`BuiltInRegistries.GAME_RULE`) and is not part of the networked registry data.
- The only rule packet carries identifier strings. The vanilla client looks each key up and ignores unknown ones (null check
  in bytecode), so a modded rule does not appear in the in-world Game Rules screen, but nothing breaks.
- `/gamerule livingreefs:...` uses brigadier integer and boolean argument types, which vanilla clients already know.
- Remaining risks, unverified: whether Fabric `registry-sync-v0` treats `GAME_RULE` as a synced registry and refuses vanilla
  clients when modded entries exist, and the argument type used for enum rules. Both need a real-server test with a vanilla
  client before the spec commits to game rules. A config file plus `/livingreefs` command is the fallback.

## Benchmark plan inputs

- Reuse Throughput's `tools/benchmark.sh` and `tools/rcon.py` shape. Use port 25576 (`RCON_PORT=25576`) and `server-port=25566`
  for this project, a random password, 127.0.0.1, a flat world (or a fixed seed with a warm ocean), and 40 s of warm-up.
- Measure the operation itself: wrap the coral `randomTick` handler (or the scanner pass) in `System.nanoTime()`, accumulate per
  second, and expose last, max and mean through a command, as Throughput's "last sample" does. Coarse `/tick query` MSPT
  hides per-second spikes (Throughput lesson).
- Workload: force-load a 16x16-chunk ocean area, `fill` coral fields at known densities (sparse: 50 per section, dense: 2000
  per section), and run at `randomTickSpeed` 3 and a stress value of 300. Compare mod off and mod on with
  `MinecraftServer#getAverageTickTimeNanos()` and `getTickTimesNanos()`, plus the handler's own nanoseconds. Take 15 readings
  and report min/median/max.
- Run the stress test with Lithium present and absent; Lithium switches between its own loop and the vanilla loop above 384
  ticking states per section.
- spark 1.10.187-fabric (26.3) is available for flame graphs during development. It is not needed for the reproducible
  script.

## Sources

- [S1] `net.minecraft.server.level.ServerChunkCache#tick(BooleanSupplier,boolean)`, `#tickChunks()`, `#tickChunks(ProfilerFiller)`, `#lambda$tickChunks$0` (javap, 26.3 common jar)
- [S2] `net.minecraft.world.level.gamerules.GameRules.<clinit>`: `ldc "random_tick_speed"; iconst_3; iconst_0; invokestatic registerInteger`
- [S3] `net.minecraft.server.level.ChunkMap#forEachBlockTickingChunk` + `lambda$forEachBlockTickingChunk$0`; `DistanceManager#forEachEntityTickingChunk`, `#inBlockTickingRange`; `ChunkLevel#isEntityTicking` (`<= 31`), `#isBlockTicking` (`<= 32`)
- [S4] `net.minecraft.server.level.ServerLevel#tickChunk(LevelChunk,int)`, `#shouldTickBlocksAt(long)`
- [S5] `net.minecraft.world.level.chunk.LevelChunkSection` (fields `nonEmptyBlockCount`, `tickingBlockCount`, `tickingFluidCount`; `isRandomlyTicking*`, `recalcBlockCounts`, `maybeHas`)
- [S6] `net.minecraft.world.level.block.{CoralBlock, CoralPlantBlock, BaseCoralPlantTypeBlock, CoralFanBlock, BaseCoralFanBlock, CoralWallFanBlock, BaseCoralWallFanBlock, BaseCoralPlantBlock}` member lists
- [S7] `net.minecraft.world.level.block.Blocks.<clinit>` (putstatic `DEAD_TUBE_CORAL_BLOCK` … `SEA_PICKLE`, `Properties.randomTicks()` call sites; the bootstrap loop calling `BlockState.initCache()`)
- [S8] `BlockBehaviour$BlockStateBase#initCache`, `#isRandomlyTicking`; `BlockBehaviour#isRandomlyTicking(BlockState)`, `#randomTick` (empty); `BlockBehaviour$Properties#randomTicks`
- [S9] Fabric API 0.162.0+26.3 module jars (class-name scan of 45 jars); `fabric-content-registries-v0` `OxidizableBlocksRegistryImpl$RandomTickCacheRefresher`
- [S10] `net.minecraft.world.TickRateManager#runsNormally`, `#isFrozen`, `#isSteppingForward`
- [S11] `fabric-game-rule-api-v1-4.0.10+3434d6d95d.jar` (`GameRuleBuilder`, `GameRuleEvents`, `fabric.mod.json`, mixin list)
- [S12] `net.minecraft.network.protocol.game.ClientboundGameRuleValuesPacket` (STREAM_CODEC: `ResourceKey.streamCodec(Registries.GAME_RULE)` → `STRING_UTF8` map); `ServerGamePacketListenerImpl#sendGameRuleValues`, `#handleSetGameRule`
- [S13] client jar: `ClientPacketListener#handleGameRuleValues`; `InWorldGameRulesScreen#lambda$onGameRuleValuesUpdated$0` (null check on `GAME_RULE.getValue`)
- [S14] `net.minecraft.world.level.saveddata.SavedDataType` (record components), `SavedData`; `ServerLevel#getDataStorage`, `MinecraftServer#getDataStorage` → `net.minecraft.world.level.storage.SavedDataStorage`
- [S15] Throughput `src/main/java/io/github/razekteixeira/throughput/ThroughputSavedData.java`, `core/VersionedState.java` (read only)
- [S16] `ServerChunkCache#getChunkNow(int,int)`, `#hasChunk`, `#isPositionTicking(long)`; `LevelReader#hasChunkAt`, `#hasChunksAt`; `Level#isLoaded(BlockPos)`; `ServerLevel#isPositionEntityTicking(BlockPos)`
- [S17] https://raw.githubusercontent.com/CaffeineMC/lithium/develop/common/src/main/java/net/caffeinemc/mods/lithium/mixin/world/chunk_ticking/random_block_ticking/ServerLevelMixin.java (fetched 2026-10-08; repo last pushed 2026-10-05)
- [S18] https://raw.githubusercontent.com/CaffeineMC/lithium/develop/lithium-fabric-mixin-config.md (`mixin.world.chunk_ticking.random_block_ticking`: "Speed up random ticks by evaluating random chances early and using a fast block search."; `mixin.alloc.chunk_random`; `mixin.util.block_tracking`)
- [S19] Modrinth API `project/lithium/version?loaders=["fabric"]&game_versions=["26.3"]` → `mc26.3-0.26.2-fabric` (2026-09-28)
- [S20] Throughput `tools/benchmark.sh`, `tools/rcon.py` (read only)
- [S21] `net.minecraft.server.MinecraftServer` members: `tickTimesNanos`, `getAverageTickTimeNanos()`, `getTickTimesNanos()`, `getCurrentSmoothedTickTime()`, `tickRateManager()`
- [S22] Modrinth API `project/spark` (23,199,412 downloads) and `project/spark/version?...26.3` → `1.10.187-fabric` (2026-09-19); https://modrinth.com/mod/spark

## Contradictions found

- The method name `ChunkMap#forEachBlockTickingChunk` suggests the block-ticking ring (level 32), but its body iterates the
  **entity**-ticking ring (level 31). `ServerLevel#shouldTickBlocksAt` uses the block-ticking ring (32). A scanner that
  copies `shouldTickBlocksAt` would tick one chunk ring further out than vanilla random ticks do. Use
  `isPositionEntityTicking` to match vanilla.
- Prompt wording vs 26.3: "ServerWorldEvents" is now `ServerLevelEvents`, and "persistent state" / `DimensionDataStorage` is
  now `SavedDataStorage`.
- The prompt lists "nonEmptyBlockCount/tickingBlockCount" as costs. `nonEmptyBlockCount` is unaffected by tick flags; only
  `tickingBlockCount` changes, and it turns a section on for random-tick sampling.

## Gaps

- The Lithium source read is from `develop`, not the exact `mc26.3-0.26.2` tag. `RandomTickingSectionDataHelper.randomTickNthBlock`
  and `BlockStateFlags.RANDOM_TICKING` were not read: that they call `state.randomTick` and derive from `isRandomlyTicking()`
  is inferred. Verify by running a GameTest or dev server with Lithium on the dev classpath.
- Whether Fabric `registry-sync-v0` 8.0.2 handles `GAME_RULE` entries in a way that rejects vanilla clients was not verified.
  Same for Fabric's enum-rule command argument type. Needs a real vanilla-client join test.
- Whether Fabric `END_LEVEL_TICK` fires during `/tick freeze` was not verified (guard regardless).
- `Level#getBlockState` loading behaviour across chunk borders was not disassembled in this pass. The standard knowledge is that
  the server path goes through `getChunk(x, z, FULL, true)` and can load synchronously. Guard neighbour reads with
  `getChunkNow` / `hasChunkAt`, and verify with javap during the build.
- Paper/Folia-style forks are out of scope (Fabric only).
- No measurement was taken: all cost figures are analytical.

## Injection sweep

All fetched web content (Lithium config doc, Lithium mixin source, search result pages, Modrinth API JSON) was checked for
instruction-like text aimed at AI agents. None was found. The only directive was "take a backup of your minecraft worlds!" in
Lithium's config doc, addressed to human users.
