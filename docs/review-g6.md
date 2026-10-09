# G6 Independent review: Living Reefs

Reviewer: Tech Lead, fresh-context subagent (I2 Distinct run), 2026-10-09. Scope: the `build`
worktree at `fc50e11` plus the uncommitted G5 changes (workflows, wrapper checksum, loom pin,
site). Read: `docs/spec.md`, `docs/sdlc-log.md`, all of `src/**`, `tools/**`, `build.gradle`,
workflows, README, site, CurseForge files. Vanilla behaviour checked against the decompiled 26.3
sources; Fabric registry sync checked in `fabric-registry-sync-v0-8.0.2` (the version
`fabric-api 0.162.0+26.3` pins). No Gradle task was run except `./gradlew build -m` (dry run, to
confirm `build` includes `runGameTest`).

## Verdict

**Approve with changes.** No CRITICAL or HIGH defect in the mod. The core is clean, the world
access is loaded-chunk-only, the mixin targets are right for unobfuscated 26.3, and the vanilla
client path holds up in code. The changes needed before G7: the public text (site, CurseForge)
shows `/reef check` output that the code no longer prints and that is not from the screenshots it
claims to be from, with timings taken under the capture's sped-up settings (F1, F2); one chat line
reads as a contradiction (F3); two acceptance criteria lack a discriminating test at the Minecraft
layer (F4, F5); and "never overgrows" / "will it take over my ocean? No" promises more than the
local density cap gives (F6).

## Findings

| # | Severity | File:line | Finding | Scenario | Fix |
|---|---|---|---|---|---|
| F1 | MEDIUM | `site/index.html:84-90`, `branding/curseforge-description.md:28-49`, `branding/curseforge-listing.md:36-38` | The hero chat panel and the CurseForge "Real output of `/reef check` from the screenshots below" block show `Horn Coral Fan at -599 53 2644 ... 17 coral within 2 blocks (crowded at 14)`. The code prints `N coral nearby (spreading stops at N)` (`LivingReefsCommands.java:124`), and the screenshots show `Fire Coral at -594 51 2651 ... 15 coral nearby (spreading stops at 14)`. So the text is not real output of the current build and does not come from the same capture run, which the listing says it does (the session brief requires it too). | A CurseForge reader compares the code block with the image right under it: different coral, coordinates, counts and wording. | Regenerate both chat blocks from the capture run's log lines (the `<!-- CHAT:... -->` markers suggest a sync step: script it in `make_media.sh` so it cannot drift), or drop the "from the screenshots below" claim. |
| F2 | MEDIUM | `LivingReefsCaptures.java:93,109-125`; `site/index.html:90,232`; `curseforge-description.md:36,48,81` | The two `/reef check` shots are taken while the capture's sped-up config is active (`growthChance 0.4`, `bleachChance 0.6`, restored only at line 146). So the screenshot and text say "Tries to spread about every 76 s" and "Bleaches in about 2 min". With defaults the same coral reads about 13 min (0.04 x 1.5 x 1.5) and about 5 min. The same pages say "about every half hour". | A player installs the mod, runs `/reef check` on a similar coral, sees about 13 min instead of 76 s, and concludes the page or the mod is wrong. | Restore `original` settings and `random_tick_speed 3` before both check shots (the check is read-only), then regenerate. Or caption the shots "with the capture's sped-up settings". |
| F3 | MEDIUM | `LivingReefsCommands.java:123-125` | The density line counts coral around the checked coral, but the cap applies around each candidate target (`SpreadPlanner.grow:36`). The flagship screenshot reads "15 coral nearby (spreading stops at 14)" and, on the next line, "Tries to spread about every 76 s". To a player that is a contradiction. The stressed shot shows "14 ... stops at 14". | A server owner trusts the line, assumes the coral is capped, and files a bug when it spreads. | Report what the planner uses: for example, count open candidate spots (water, clearance, under the cap) within `spreadRadius`, "3 of 40 spots nearby can take new coral". Or reword: "15 coral within 2 blocks (new coral needs fewer than 14 around its own spot)". |
| F4 | MEDIUM | `docs/spec.md:115,119`; `ReefTicker.java:49-50` | The spec's AC table names tests that do not exist: `LivingReefsGameTests#capsStopGrowth` (AC4; the real ones are `shallowWaterStopsGrowth` and `glassFloorStopsGrowth`) and `#gameRulesSwitchBehaviourOff` (AC8; folded into `randomTicksGrowAReef`). The bleaching half of AC8 has no discriminating test in the Minecraft layer: the mutant `boolean bleaching = true;` in `ReefTicker.tick` survives the suite. `randomTicksGrowAReef` has no heat source; the quiet tests call `act()` with explicit flags; `checkCommandExplainsHealth` only reads the rule for the outlook text. `ReefRulesTest#gameRulesSwitchActionsOff` covers `decide`, not the glue. | A refactor that reads the wrong rule (or caches it) leaves `coral_bleaching false` doing nothing, and CI stays green. | Add a FAST-environment GameTest: coral next to magma, bleaching rule off, growth on, idle N ticks at speed 1024, assert still living; turn bleaching on, assert bleached. Add the mutant to `tools/mutation.py`. Fix the test names in the spec. |
| F5 | MEDIUM | `ReefConditions.java:66-76`; `LivingReefsGameTests.java:48-51,170` | AC9 is covered only in `core`. Every server GameTest uses `minLight 0`, so `ReefConditions.lightAt` is never exercised. That method is load-bearing: a coral block is opaque, so its own light is 0, and only the neighbour maximum lets it thrive. The mutant `return brightness(level, pos);` (own position only) makes every coral block dormant at the default `minLight 1`, and it survives the suite. It would show only as a frozen time-lapse in the capture run, which asserts nothing. | A "simplification" of `lightAt` ships, and reefs of coral blocks never grow on real servers. | Add a GameTest with `minLight 1`: a coral block in an open-sky lit pool asserts THRIVING (and grows with a fixed roll); the same pool roofed over asserts DORMANT with `TOO_DARK`. Add the mutant. |
| F6 | MEDIUM | `README.md:69`; `site/index.html:158,357-358`; `curseforge-description.md:20` | "It never overgrows" and the FAQ "Will it take over my ocean? No." The caps are local (14 coral in a 5x5x5 box around the target, height 3, two water above). Nothing bounds the extent. Every new coral is a source, so over loaded play time coral carpets every sand, gravel or stone floor of warm, lukewarm and deep lukewarm ocean near players at roughly the density shown in `timelapse-last.png`. That includes lukewarm oceans, which have no natural coral. | A server near a lukewarm ocean finds, after some weeks, its seabed paths and builds ringed by a coral carpet, despite the FAQ. | Either word it truthfully ("never gets denser than ...; it keeps spreading slowly across reef water near players; turn it off with the game rule"), or add an extent rule for the owner to decide (for example, the target must be within N blocks of a coral block, a sea-floor depth band, or a cap in a larger radius). Record the decision in the spec. |
| F7 | LOW | `tools/benchmark.sh:2-4` | The comment says the reef is "so dense that nothing can grow and every check runs to the end". In fact a coral-block source's growth target is never water (fans above, blocks beside, sand below), so `grow` returns at the first cell read and the 124-cell density scan does not run. Only fan sources with dy=+1 reach it. The measured number is still valid for the condition read, but successful growth (setBlock, neighbour updates, light) and block-entity-heavy chunks (conduit scan) are not measured. | A reader takes 1.9 ms per second as the worst case. | Fix the comment. Optionally add a second workload with open water above a sparse floor, so growth succeeds. |
| F8 | LOW | `ReefConditions.java:79-99` | The conduit scan iterates every block entity in up to 9 chunks on each assessed tick, with no bound by block entity count. | An ocean base with thousands of hoppers or chests within 16 blocks of a reef multiplies the per-assessment cost. | Run the scan lazily, only when its answer matters (stress, neutral water, or the boost on a GROW roll). Or cache the result per chunk for some ticks. |
| F9 | LOW | `ReefRules.java:92-95`, `ReefTicker.java:55-58` | The gate is dominated by `bleachChance` (0.25 vs 0.22 thriving), so 25 % of all coral random ticks do the full read (125 blocks, 14 light lookups, biome, entity query, conduit scan), even in a world with no heat anywhere. | Not a bug; it costs about 10 % more than necessary at default settings. | When `roll >= thriving gate`, only BLEACH is possible: scan heat and the biome first and return before the light, entity and conduit reads unless stressed. |
| F10 | LOW | `SpreadPlanner.java:72-91` | Recovery does not check the target's own stress. A thriving source 3 blocks from magma revives a dead neighbour that is 2 blocks from the magma. That neighbour bleaches again, and the loop repeats forever (particles flicker, stats count both). | Visible at a vent's edge in the capture world. | In `ReefTicker` RECOVER, skip a target with a heat source within `NEAR` (a cheap 5x5x5 scan), or accept and document it. |
| F11 | LOW | `Settings.java:117`, `ReefTicker.java:105` | `minWaterAbove` may be 0. A coral block can then grow into a water hole with air above and solid sides, and end up with no water neighbour. `CoralBlock` has no `onPlace` die tick, so it stays a dry living block until a neighbour update. | Odd living coral blocks at the surface with a non-default config. | Clamp `minWaterAbove` to 1..8, or check `scanForWater` after placing a coral block. |
| F12 | LOW | `ReefTicker.java:105-108` | A coral block grows into a water cell without an entity check, so it can enclose a swimming player or mob (suffocation damage, push-out). | A player hovering by a reef at default speed gets a block placed in their head. | For the BLOCK form, require `level.isUnobstructed(state, target, CollisionContext.empty())`. |
| F13 | LOW | `LivingReefsCommands.java:105-107,170` | The dead-coral line says it recovers "when a thriving coral of the same kind grows right next to it". Recovery needs a thriving coral of that kind next to it (within 1 block, diagonals included); no growth is involved. "Stays as it is" is also printed for a dry living coral, which vanilla is about to kill. | Players plant fragments, wait for "growth", and are confused. | Change it to "...when a thriving coral of the same kind is right next to it", and add a DRY outlook ("Out of water: it will die"). |
| F14 | LOW | `LivingReefsGameTests.java:169-186` | `randomTicksGrowAReef` restores the global config only on the success path. A timeout leaves `growthChance 0.5` active for later batches. | Confusing follow-on failures when the test itself fails. | Restore it at the start of each test that relies on the config, or set it in `@BeforeBatch`-style setup and reset it in the environment teardown. |
| F15 | LOW | `tools/mutation.py:123,136` | A result of "KILLED (by other tests)" counts as a pass in the exit code. The gate rule says a kill counts only when the named test killed it. | A mutant killed incidentally passes G4 without anyone noticing. | Fail the run (or mark it in the table) when the expected killer is not among the failures. |
| F16 | LOW | `site/index.html:180-181`, `curseforge-listing.md:50`, README gallery | The "At night, lit by sea pickles" shot is taken with night vision, so it looks like dim daylight. The rules use raw sky light (`getBrightness(SKY)`), so night never affects growth, while the listing says "Sea pickles light the reef after dark, and light lets deep coral grow". | Implies a day/night mechanic that does not exist. | Clear night vision for that shot, and reword: sea pickles matter for deep or roofed reefs, not for night. |
| F17 | LOW | `core/Facing.java:26-33` | `Facing.opposite()` is unused. | Dead code. | Remove it. |
| F18 | LOW | `core/Conditions.java:8` | The Javadoc says light is "the brighter of sky and block light at the coral"; it is the maximum over the coral and its six neighbours. | Misleading for the next maintainer, see F5. | Fix the Javadoc. |
| F19 | LOW | `branding/curseforge-listing.md:17-18` | The categories "World Gen" and "Biomes": the mod adds no worldgen or biomes. | Moderator pushback, or wrong-audience browsing. | Consider "Server Utility" as main and drop "Biomes"; keep the reasons honest. |
| F20 | LOW | `tools/rcon.py:15-28` | The RCON client ignores an auth failure (response id -1) and assumes `recv(4)` returns 4 bytes. | With a wrong password it prints empty lines instead of an error. | Check the auth response id and loop until all 4 length bytes arrive. |

Checked and found correct (no finding):
- **Only water is replaced.** The target must be `Blocks.WATER` with a source fluid (`WorldReefView.java:36`), read in the same tick that places it.
- **Placement survives.** Placed coral passes `canSurvive` before `setBlock`. Plants and fans default to `waterlogged=true`. Wall fan support at `target - facing` matches `BaseCoralWallFanBlock.canSurvive`. Revived coral keeps `waterlogged` and `facing` through `withPropertiesOf`. `CoralPlantBlock.onPlace` finds water, so no die tick.
- **No chunk loads or generation.**
  - All neighbourhood reads use `getChunkNow`.
  - `getBiome` goes through `getChunk(..., BIOMES, false)`.
  - Light reads use the light engine.
  - The `canSurvive` and `getBlockState` reads in `place` hit cells the planner already read as loaded.
  - The positions it updates stay within one chunk of a ticking chunk, because `spreadRadius` is at most 4 and the density radius is 2.
- **Server only.** `randomTick` takes `ServerLevel`. The client never random-ticks blocks, so the cached flag is harmless on a client.
- **Mixin.**
  - Both targets, `isRandomlyTicking(BlockState)` and `randomTick(BlockState, ServerLevel, BlockPos, RandomSource)`, are declared on `BlockBehaviour` (lines 337, 399).
  - None of the eight coral classes override either method.
  - `initCache` caches the flag per state (line 508), so the class-based choice is right and Lithium sees it.
  - `defaultRequire 1` makes a failed injection crash.
  - The names are Mojang names in production (unobfuscated 26.3), so no refmap is needed. The jar holds no refmap.
- **Vanilla client (AC14, code level).**
  - `BuiltInRegistries.GAME_RULE` is not in Fabric's synced list (`FabricRegistryInit`).
  - `ClientboundGameRuleValuesPacket` carries `ResourceKey`s, and `InWorldGameRulesScreen.onGameRuleValuesUpdated` drops unknown keys.
  - `/gamerule` uses literals.
  - Commands use only vanilla argument types, and particles are vanilla.
  - Custom block and biome tags sync like any datapack tag.
- **Species parser.** It rejects `dead_dead_`, an empty type, a missing namespace and `coral_block`. Suffix order (`_coral_wall_fan` before `_coral_fan` before `_coral`) is right.
- **Config.** The read is lenient, the write strict. NaN is clamped. A broken file is never overwritten and the previous settings stay.
- **Commands.** The `PermissionLevel` overload is used, and reload resends command trees.
- **Global state.** Stats reset on `SERVER_STOPPED`. The species table is static by design (frozen registry).
- **Hot path.** The cheap path allocates nothing (`Result.SKIPPED` is static).
- **CI.** `./gradlew build` runs `runGameTest`.

## AC coverage

| AC | Named test(s) exist? | Discriminating? | Note |
|---|---|---|---|
| AC1 | yes, `livingCoralRandomTicksDeadDoesNot` | yes | Every state of all 40 vanilla coral blocks; kills G1. |
| AC2 | yes, `randomTicksGrowAReef` | yes | Real random ticks at speed 1024; kills G2. Runs with `minLight 0` (see F5). |
| AC3 | yes, `growthKeepsSpeciesAndFillsOnlyWater`, `wallFansGrowOnEverySide`, `SpreadPlannerTest` | yes | Species, floor and walls untouched, facing per side (kills G9, G10). |
| AC4 | partly: `capsStopGrowth` does not exist; real: `shallowWaterStopsGrowth`, `glassFloorStopsGrowth`, plus the four `SpreadPlannerTest` methods | yes for clearance, substrate, density and height (unit) | The density cap has unit tests only. Fix the spec names (F4). |
| AC5 | yes, `heatBleachesCoral`, `ReefRulesTest#heatStresses` | yes | Facing east plus waterlogged checked exactly (kills G3, G5); `noHeatNoBleaching` control. |
| AC6 | yes, `recoveryRevivesWetDeadCoralOnly` | yes | Exactly one revived; dry sealed block and other species untouched. |
| AC7 | yes, `dormantOutsideReefWaters`, `conduitMakesWaterReefAndProtects`, `conduitProtectsFromHeat` | yes | Real conduit frame; kills G6. "Conduit makes neutral water thrive" is unit-tested only. |
| AC8 | **no**: `gameRulesSwitchBehaviourOff` does not exist | growth rule yes (G7 via `randomTicksGrowAReef`); **bleaching rule no** | F4. |
| AC9 | yes, `ReefRulesTest#darkCoralIsDormant` | core only | `lightAt` in the glue is untested (F5). |
| AC10 | yes, `ReefRulesTest#boostsMultiplyGrowthChance` | core only | Fish and pickle counting in `ReefConditions` untested in the world (LOW; covered by the check screenshot only). |
| AC11 | yes, `SpeciesTableTest#*` | yes | Kills M1, M2. |
| AC12 | yes, `SettingsTest#*`, `LivingReefsConfigTest#*` | yes | Missing, partial, clamp, broken-untouched, wrong type. |
| AC13 | yes, `checkCommandExplainsHealth` | yes for stressed and bleached | No thriving or dormant report asserted; F3 wording not covered. |
| AC14 | code review (this document) | n/a | Passes at code level (see "Checked"). The manual vanilla-client join in the release checklist is still to do before tagging. |
| AC15 | `tools/benchmark.sh`, recorded in the log | n/a | Valid for the condition read; not a worst case (F7). |

## Visual check

`build/run/clientGameTest/screenshots/` was empty at review time (only `eula.txt` under
`clientGameTest/`; a capture run may have cleaned it). I reviewed the committed media derived from
the latest capture run in `site/media/`: `check.png`, `check-stressed.png`,
`timelapse-first/last.png`, `bleach-before/after.png`, `recovered.png` and
`gallery-reef/restored/night.png`.

- **Chat:** readable, clear background, correct colours (aqua name, green "thriving", red
  "stressed", red ✘ and green ✔). Chat was cleared before each shot; no spam. The content has
  issues: F1 (text elsewhere does not match), F2 (sped-up timings), F3 ("15 nearby, stops at 14"
  next to "tries to spread").
- **Hand and hotbar:** hidden in all shots (spectator). In `check.png` a tropical fish passes in
  front of the camera at bottom left, partly behind the chat box. Cosmetic; a re-take would be
  cleaner.
- **Water clarity:** good in every shot. The time-lapse and bleach shots are bright and clear; the
  gallery shots show the surface and depth well.
- **Time-lapse:** clearly shows growth. The first frame has five blocks and four pickle groups on
  bare sand; the last frame is a dense multi-species carpet with coral blocks stacked and wall fans
  in place. Growth particles are visible. It also illustrates F6: the carpet density at the cap.
- **Bleaching:** clearly visible. `bleach-after` has a white-grey patch about 7 x 7 with dead coral
  blocks. `recovered` shows colour back with a few grey remnants. The before, after and recovered
  framing is identical, which makes it a strong comparison.
- **Night:** reads as dusk daylight because of night vision; the caption overclaims (F16).

## Notes

- **Order of work:** F1 and F2 are one fix (regenerate the check shots with default settings and
  sync the text from the log). Then F3 (one line of code) and F4 and F5 (two GameTests plus two
  mutants). F6 needs an owner decision or a wording change; please record it in the spec.
- **Severity calibration:** nothing found can corrupt worlds, load chunks, break vanilla clients
  or crash a server. The MEDIUMs are about public claims and test teeth on two glue paths.
- **Working tree:** the uncommitted G5 changes (`persist-credentials: false`, wrapper
  `distributionSha256Sum`, loom pinned to 1.18.3) look right; `site/media/` is untracked and must be
  committed for Pages.
