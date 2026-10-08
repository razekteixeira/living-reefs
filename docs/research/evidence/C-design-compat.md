# Evidence pack: C design inputs and compatibility
**Slice budget:** ~37 / 35 (slightly over; several calls were refused by the worktree sandbox and repeated)  **Wave:** 1

## Candidate claims
1. Reef-building coral grows within roughly 18 to 36 °C, with best reef growth near 26 to 28 °C; temperature and light are the main limits. [S1] (tier 2: NOAA glossary text mirrored on a university site, origin-independent: no, secondary sources give 20 to 32 °C and 23 to 29 °C [S2])
2. Reef development is mostly limited to depths under about 20 m in clear water because light drives symbiont photosynthesis; individual corals live deeper (to about 48 m in clear tropical water) but sparsely. [S1][S3] (tier 2, origin-independent: yes)
3. Silt and freshwater from rivers suppress reefs; sediment smothers coral surfaces. [S1][S2] (tier 2/3, origin-independent: no)
4. Bleaching is coral expelling its symbiotic algae (zooxanthellae) under stress, leaving tissue clear over a white skeleton; the algae supply most of the coral's food and colour. [S4][S5] (tier 1, origin-independent: yes, two NOAA pages)
5. Heat is the main trigger: about 1 °C above normal for a week or more can stress corals; pH change and excess nutrients also trigger it. [S4][S5] (tier 1)
6. Mild or short bleaching is recoverable (corals regrow symbionts); severe or prolonged bleaching kills colonies; repeated bleaching within a short period sharply reduces recovery. [S4][S5] (tier 1)
7. Typical linear extension is in the order of millimetres to a few centimetres per year (branching Acropora about 5 to 15 mm/yr, massive Porites about 8 to 21 mm/yr in one meta-analysis). [S6][S7] (tier 2, origin-independent: yes; site-dependent, exposure changes rates)
8. Large herbivorous fish keep algae in check; where they were excluded, algae overgrew reefs and blocked coral recruitment. [S8] (tier 1, peer-reviewed via PubMed record; single study)
9. Reef recovery after disturbance takes years and can be reset by new disturbances (cyclones, crown-of-thorns outbreaks). [S9] (tier 2, AIMS testimony in parliamentary tabled document)
10. Restoration practice: coral nurseries (fragments hung on PVC "trees"), outplanting, and microfragmentation, which can increase cover "orders of magnitude faster than natural growth". [S10][S11] (tier 1, origin-independent: yes)
11. Vanilla grass spreads to dirt within x/z ±1, y +1 to −3 of the source, needs light ≥ 9 above the source, makes 4 attempts per random tick, and cannot spread under water or a waterlogged block. [S12] (tier 2)
12. Vanilla vines spread with 25% chance per random tick, but upward and sideways spread stops when 4 or more other vines exist in a 9×9×3 box; a Java game rule `spread_vines` disables spread entirely. [S13] (tier 2)
13. Vanilla kelp uses an `age` 0 to 25 cap set randomly at placement (0 to 24), 14% growth chance per random tick; shears set age 25 to stop growth. [S14] (tier 2)
14. Bamboo grows with 1/3 chance per random tick, needs light ≥ 9 at the top, caps at 12 to 16 blocks via a `stage` property. [S15] (tier 2)
15. Sculk spread is event driven (mob death near a catalyst), budgeted by XP-derived charge (1 XP = 1 charge), restricted to the `sculk_replaceable` tag, and sensors/shriekers are capped at 2 per 9×3×9 box. [S16] (tier 2)
16. At default `random_tick_speed` 3, a given block is random-ticked on average every 68.27 s (median 47.3 s). [S17] (tier 2)
17. Fabric convention tags v2 (Fabric API branch 26.3, version 0.162.0) define biome tags `c:is_ocean`, `c:is_deep_ocean`, `c:is_shallow_ocean`, `c:is_aquatic`, `c:is_aquatic_icy`, `c:is_hot`, `c:is_hot/overworld`, `c:is_temperate/overworld`, `c:is_cold/overworld`, `c:is_wet/overworld` and others. [S18] (tier 1, primary source code)
18. Neither Fabric convention tags v2 (ConventionalBlockTags, branch 26.3) nor NeoForge `Tags.java` (branch 26.3.x) defines any coral or reef tag. Coral interop must go through vanilla `minecraft:corals`, `minecraft:coral_plants`, `minecraft:coral_blocks`, `minecraft:wall_corals`. [S18][S19] (tier 1, origin-independent: yes)
19. NeoForge `Tags.Biomes` defines the same `c:` ocean/hot/aquatic ids as Fabric. [S19] (tier 1)
20. Upgrade Aquatic (NeoForge, latest branch 1.21.1) adds ten coral types and adds them to vanilla `minecraft:corals`, `minecraft:coral_plants`, `minecraft:coral_blocks`, `minecraft:wall_corals`. [S20] (tier 1, primary repo)
21. Biomes O' Plenty (branch 26.3) adds no ocean or reef biomes (its `end_reef` is an End biome), but places dead vanilla coral via its own features and has its own tags `biomesoplenty:dead_coral_blocks` / `dead_coral_plants` listing vanilla dead coral. [S21] (tier 1)
22. Terralith (latest branch in repo is 1.20 / main) only overrides frozen ocean biomes, ships empty `c:ocean` tag extensions, and places non-waterlogged dead coral fans on desert gravel. [S22] (tier 1, but repo may lag the released mod)
23. Tectonic (branch rewrite-squared, pushed 2026-09) adds no ocean biomes but changes ocean depth/offset density functions, so ocean floors may be deeper or shallower than vanilla. [S23] (tier 1)
24. Minecraft Java 1.21.11 (snapshot 25w44a) moved game rules into a registry and renamed them to namespaced snake_case ids. [S24][S25] (tier 2, origin-independent: yes)
25. Fabric API since 1.21.11 registers custom rules with `GameRuleBuilder` and offers `GameRuleEvents` for change notifications. [S26][S18b] (tier 1)
26. Fabric registry sync (branch 26.3) only disconnects vanilla clients for modded entries in registries marked SYNCED; `BuiltInRegistries.GAME_RULE` is not in that list (blocks, items, entity types, etc. are). So a custom game rule should not by itself force Fabric on the client. [S27] (tier 1, inferred from source; not run-tested)
27. Vanilla 26.3 has `ClientboundGameRuleValuesPacket`, which sends game rule values keyed by `ResourceKey<GameRule>` (identifier string) plus a string value, not by numeric id, so unknown modded rule keys decode on a vanilla client; how the vanilla game rules screen handles an unknown key is untested. [S28] (tier 1, bytecode inspection)
28. Server-side spreading precedent: Nether Portal Spread (Serilum, Fabric/Forge/NeoForge, 26.3, client unsupported) tracks spread sources in saved data instead of scanning the world, uses a configurable radius, and lets players stop spread by placing a vanilla-block marker (four coal blocks). [S29] (tier 2, author's own description)

## Reef ecology (short)
Ground the design in these, do not simulate them:
- **Warm, bright, clear, hard bottom.** Coral reefs need warm water (about 18 to 36 °C, best 26 to 28 °C) and light, so they sit in shallow clear water (reefs mostly < 20 m) and fail near river silt and freshwater [S1][S2][S3]. Game mapping: warm ocean biome or `c:is_hot/overworld` plus `c:is_ocean`, water source above, sky or block light threshold, a depth window below sea level, a substrate tag (sand, gravel, coral block, stone), and a "turbidity" penalty for nearby sand/gravel falling or dirt/mud.
- **Slow growth.** Millimetres to centimetres per year [S6][S7]. Game mapping: spread should be much slower than kelp or grass; one new block per reef cluster per in-game day or slower is the right order.
- **Bleaching = stress, not instant death.** Coral expels symbionts under heat stress (about +1 °C for a week), turns white, and recovers if stress ends quickly; long or repeated stress kills it [S4][S5]. Game mapping: a reversible "bleached" stage before vanilla dead coral. Server-side only, vanilla has no bleached block, so options are (a) skip a visual stage and use a hidden counter in saved data, or (b) use the dead variant as "bleached" and allow revival within a window (deviates from vanilla where dead coral is permanent).
- **Grazers protect recruitment.** Herbivorous fish stop algae from blocking new coral [S8]. Game mapping: nearby tropical fish or other fish entities (or sea pickles, which vanilla already associates with coral) can raise spread chance; a "grazer bonus" rather than a requirement.
- **Recovery is slow and reversible.** Years, reset by new disturbances [S9]. Game mapping: dead coral may revert only when adjacent to living coral and conditions hold, at a lower rate than spread.
- **Restoration methods** map onto vanilla tools: coral gardening (player places live coral fragments next to dead coral), microfragmenting (breaking/placing coral to start new colonies), outplanting from nurseries [S10][S11]. Larval reseeding has no clear vanilla analogue (possible: bone meal on water near coral). No tier 1 source on larval methods was found (gap).

## Spreading-block precedents and caps
| Block | Trigger | Rate | Conditions | Overgrowth cap / off switch | Source |
|---|---|---|---|---|---|
| Grass block | random tick | 4 attempts per tick, ±1 x/z, +1..−3 y | light ≥ 9 above source; target not under water or waterlogged | only converts dirt (not coarse or rooted); dies under opaque cover | [S12] |
| Vines | random tick | 25% per tick, one direction | none for light | fewer than 4 vines in 9×9×3 box for up/side spread; game rule `spread_vines` | [S13] |
| Kelp | random tick | 14% per tick | water above | per-plant `age` cap 0..25, random start; shears freeze | [S14] |
| Bamboo | random tick | 1/3 per tick | light ≥ 9 at top | height cap 12..16 via `stage` | [S15] |
| Sculk | event (mob death) | every other tick per charge | `sculk_replaceable` tag | XP budget per death; 2 sensors/shriekers per 9×3×9 | [S16] |
| Nether Portal Spread (mod) | tracked sources | configurable | radius around portal | config radius; vanilla-block blocker; sources kept in saved data | [S29] |

Design lessons:
- Every vanilla spreader has a **local density cap** (vines 9×9×3), an **age/stage cap** (kelp, bamboo), or a **budget** (sculk charge). A coral spreader needs at least one; a local density cap like vines is the most natural fit and also stops worldgen reefs overgrowing.
- Whitelists via **tags** (`sculk_replaceable`, dirt only) bound what can be converted; use a `livingreefs:coral_substrate` block tag and never replace non-air/non-water blocks.
- Vanilla already exposes **game rule off switches** for spreading (`spread_vines`); a `livingreefs:...` boolean rule fits that convention.
- Random tick cadence: at default speed a block is ticked about every 68 s on average [S17]; spread chance must be tuned against that, and random ticks only happen in ticking chunks (exact radius not confirmed by [S17], gap).
- Server-side mods avoid world scans by **tracking sources** in saved data [S29].
- No Fabric "ecosystem/spreading moss" server mod with a documented performance design was found (gap); Dynamic Trees, Ecologics, Serene Seasons were not checked in this wave.

## Worldgen and coral mod compatibility (table)
| Mod | Loader / latest seen | Adds ocean or reef biomes? | Places coral? | Tags | Impact on Living Reefs | Source |
|---|---|---|---|---|---|---|
| Terralith | Fabric/Forge (repo default branch 1.20; `main` exists) | No new oceans; overrides frozen ocean biomes | Yes: non-waterlogged **dead** coral fans on desert gravel | ships `c:ocean`, `c:deep_ocean`, `c:shallow_ocean` biome tag files (empty additions, old v1 tag names) | Must not treat dry dead coral on land as reef or revive it; require water | [S22] |
| Tectonic | Fabric/NeoForge, 1.21.1 and 26.1 listed | No | No | n/a | Changes ocean depth; depth rules must use "blocks below water surface" not absolute Y | [S23][S30] |
| Biomes O' Plenty | Fabric/NeoForge, branch 26.3 | No (only `end_reef` in the End) | Yes: dead coral blocks and patches (vanilla dead coral) | own `biomesoplenty:dead_coral_blocks/plants`; ships `c:is_aquatic`, `c:is_hot/overworld` etc. | Dry dead coral in land biomes; same rule: water required | [S21] |
| Regions Unexplored | not found at `RegionsUnexplored/Regions-Unexplored` | unknown | unknown | unknown | gap | — |
| Lithosphere, OTBWG, WWOO, Explorify | not checked | — | — | — | gap | — |
| Upgrade Aquatic | NeoForge, branch 1.21.1 (no 26.x) | n/a | adds 10 coral types (+ dead variants) | added to vanilla `minecraft:corals`, `coral_plants`, `coral_blocks`, `wall_corals` | Tag-based detection finds them, but live→dead mapping cannot come from tags; needs a data-driven pair map (or `dead_` name convention fallback) | [S20] |
| Fabric convention tags v2 | Fabric API 0.162.0 for 26.3 | — | — | biome: `c:is_ocean`, `c:is_shallow_ocean`, `c:is_deep_ocean`, `c:is_aquatic`, `c:is_hot/overworld`; block: **no coral tag** | Use `c:` biome tags + vanilla `minecraft:is_ocean`, `minecraft:warm_ocean`-style checks; coral via vanilla tags | [S18] |
| NeoForge Tags | branch 26.3.x | — | — | same `c:` biome ids; **no coral tag** | same | [S19] |

## Settings surface and vanilla client compatibility
- **Game rules are a registry since 1.21.11** (25w44a), renamed to namespaced snake_case ids [S24][S25]. Fabric API exposes `GameRuleBuilder` (int, double, boolean, enum, with range, category, command result) and `GameRuleEvents` for change listeners [S26][S18b].
- **Vanilla clients:** Fabric registry sync disconnects a client only when a SYNCED registry has modded entries; the game rule registry is not marked SYNCED in `FabricRegistryInit` on branch 26.3 [S27]. Vanilla's `ClientboundGameRuleValuesPacket` keys values by identifier, not by numeric id [S28]. Inference: a `livingreefs:*` game rule should be safe for vanilla clients, but **this must be proven by a real vanilla-client join test** and by opening the in-game game rules screen (26.1 added a game rules screen reachable from the pause menu [S25]) as an operator.
- **Typical server-mod pattern:** config file for tuning (rates, caps, tags), game rule for on/off and a coarse speed, command for reload/inspection. Serilum mods use config files plus vanilla-block in-world controls [S29]; vanilla uses game rules for spread toggles (`spread_vines`) [S13]. Recommendation for the spec: boolean game rule `livingreefs:reef_growth` (per-world, admin-friendly, mirrors `spread_vines`), detailed JSON config with lenient read and strict write (Throughput lesson), tags for blocks/biomes as data.

## Sources
- [S1] NOAA glossary text on reef growth limits (mirrored), https://bennington.edu/doc/23691, published n.d., accessed 2026-10-08, tier 2, secondary mirror of NOAA text; verify on coral.noaa.gov
- [S2] Conditions for coral growth (course notes), https://tutorchase.com/notes/cie-a-level/geography/8-3-2-conditions-for-coral-growth, n.d., accessed 2026-10-08, tier 3
- [S3] Florida Museum, coral habitat, https://www.floridamuseum.ufl.edu/southflorida/habitats/corals/habitat/, n.d., accessed 2026-10-08, tier 2
- [S4] NOAA Ocean Service podcast "Coral bleaching", https://oceanservice.noaa.gov/podcast/sep15/os11-bleaching.html, 2015-09, accessed 2026-10-08, tier 1
- [S5] NOAA Coral Reef Conservation Program, water temperature and coral infographic, https://coralreef.noaa.gov/gallery/infographic/watertempcoral-diagram.html, n.d., accessed 2026-10-08, tier 1
- [S6] BCO-DMO coral growth meta-analysis dataset, https://bco-dmo.org/dataset/737526/data/view/902160, n.d., accessed 2026-10-08, tier 1/2 (dataset)
- [S7] Acropora humilis growth, Hurghada (EJABF), https://ejabf.journals.ekb.eg/article_2096.html, n.d., accessed 2026-10-08, tier 2
- [S8] PubMed record, GBR herbivorous fish and reef recovery (Current Biology), https://pubmed.ncbi.nlm.nih.gov/27462981/, n.d., accessed 2026-10-08, tier 1 (abstract only seen via search summary)
- [S9] AIMS tabled document, Senate Estimates 2018-19, https://aph.gov.au/-/media/Estimates/economics/bud1819/Tabled_Documents/Industry_Innovation_and_Science/TabledDoc_19.pdf, 2018, accessed 2026-10-08, tier 2
- [S10] NOAA Office of Response and Restoration, "How NOAA uses coral nurseries", https://response.restoration.noaa.gov/node/1237, 2014, accessed 2026-10-08, tier 1
- [S11] Knapp et al. 2022, coral micro-fragmentation assays, https://www.ncbi.nlm.nih.gov/pmc/articles/PMC9302430/, 2022, accessed 2026-10-08, tier 1
- [S12] Minecraft Wiki, Grass Block, https://minecraft.wiki/w/Grass_Block, n.d., accessed 2026-10-08, tier 2
- [S13] Minecraft Wiki, Vines, https://minecraft.wiki/w/Vines, n.d., accessed 2026-10-08, tier 2
- [S14] Minecraft Wiki, Kelp, https://minecraft.wiki/w/Kelp, n.d., accessed 2026-10-08, tier 2
- [S15] Minecraft Wiki, Bamboo, https://minecraft.wiki/w/Bamboo, n.d., accessed 2026-10-08, tier 2
- [S16] Minecraft Wiki, Sculk Catalyst, https://minecraft.wiki/w/Sculk_Catalyst, n.d., accessed 2026-10-08, tier 2
- [S17] Minecraft Wiki, Random tick, https://minecraft.wiki/w/Random_tick, n.d., accessed 2026-10-08, tier 2
- [S18] FabricMC/fabric branch 26.3, `fabric-convention-tags-v2/.../ConventionalBiomeTags.java` and `ConventionalBlockTags.java`, https://github.com/FabricMC/fabric/tree/26.3/fabric-convention-tags-v2, gradle.properties version 0.162.0 / minecraft 26.3, accessed 2026-10-08, tier 1
- [S18b] FabricMC/fabric branch 26.3, `fabric-game-rule-api-v1/.../GameRuleBuilder.java`, https://github.com/FabricMC/fabric/tree/26.3/fabric-game-rule-api-v1, accessed 2026-10-08, tier 1
- [S19] neoforged/NeoForge branch 26.3.x, `src/main/java/net/neoforged/neoforge/common/Tags.java`, https://github.com/neoforged/NeoForge/blob/26.3.x/src/main/java/net/neoforged/neoforge/common/Tags.java, accessed 2026-10-08, tier 1
- [S20] team-abnormals/upgrade-aquatic branch 1.21.1, `src/generated/resources/data/minecraft/tags/block/corals.json` (+ coral_blocks, coral_plants, wall_corals), https://github.com/team-abnormals/upgrade-aquatic, pushed 2026-09-16, accessed 2026-10-08, tier 1
- [S21] Glitchfiend/BiomesOPlenty branch 26.3, biome list, dead coral features and tags, https://github.com/Glitchfiend/BiomesOPlenty/tree/26.3, pushed 2026-10-02, accessed 2026-10-08, tier 1
- [S22] Stardust-Labs-MC/Terralith branch 1.20, `data/terralith/worldgen/configured_feature/desert/gravel/coral_fans.json`, `data/c/tags/worldgen/biome/ocean.json`, https://github.com/Stardust-Labs-MC/Terralith, pushed 2025-02-07, accessed 2026-10-08, tier 1 (may lag released versions)
- [S23] Apollounknowndev/tectonic branch rewrite-squared, ocean density functions, https://github.com/Apollounknowndev/tectonic, pushed 2026-09-21, accessed 2026-10-08, tier 1
- [S24] Nodecraft, Minecraft 1.21.11 game rule changes, https://nodecraft.com/support/games/minecraft/general/minecraft-update-1-21-11-gamerule-changes, n.d., accessed 2026-10-08, tier 3
- [S25] Minecraft Wiki, Game rule (history: 25w44a registry and rename; 26.1 snapshot 3 game rules screen from pause menu), https://minecraft.wiki/w/Game_rule, n.d., accessed 2026-10-08, tier 2
- [S26] Fabric blog, "Fabric for Minecraft 1.21.11", https://fabricmc.net/2025/12/05/12111.html, 2025-12-05, accessed 2026-10-08, tier 1
- [S27] FabricMC/fabric branch 26.3, `fabric-registry-sync-v0/.../RegistrySyncManager.java` and `FabricRegistryInit.java`, https://github.com/FabricMC/fabric/tree/26.3/fabric-registry-sync-v0, accessed 2026-10-08, tier 1
- [S28] Minecraft 26.3 `minecraft-common-deobf-26.3.jar`, `net.minecraft.network.protocol.game.ClientboundGameRuleValuesPacket` (javap), local Loom cache, accessed 2026-10-08, tier 1
- [S29] Modrinth, Nether Portal Spread (Serilum), https://modrinth.com/mod/nether-portal-spread, updated 2026-10-02, accessed 2026-10-08, tier 2
- [S30] Modrinth, Tectonic, https://modrinth.com/mod/tectonic, n.d., accessed 2026-10-08, tier 2

## Contradictions found
- Temperature ranges differ by source: 18 to 36 °C (NOAA glossary via [S1]) vs 20 to 32 °C and optimum 23 to 29 °C [S2]. Use the NOAA band as narrative only; the game uses biome temperature tags, not °C.
- Max depth: "reefs < 20 m" [S1] vs "corals to 48 m" [S3]: not a true contradiction (reef building vs individual colonies).
- Tectonic supported versions: Modrinth lists 1.21.1 and 26.1; CurseForge lists 1.20.1, 1.21.1, 1.21.5 (search summary) [S30].
- Nodecraft says the 1.21.11 rename did not change behaviour, but other sources and the wiki note inverted values (e.g. `disableRaids` → `raids`) [S24][S25].
- Terralith repo default branch is 1.20 while the mod is released for newer versions; the repo may not reflect current released data [S22].

## Gaps
- Not run-tested: a vanilla 26.3 client joining a Fabric server with a custom `livingreefs:*` game rule, and the vanilla game rules screen receiving an unknown key. Needs a real test before the spec relies on game rules.
- Which chunks receive random ticks (simulation distance) was not confirmed from a source in this wave.
- No tier 1 source on larval reseeding; no AIMS long-term monitoring figure for recovery time.
- Regions Unexplored repo not found at the guessed path; Lithosphere, Oh The Biomes We've Gone, WWOO, Explorify, Nature's Spirit not checked for ocean biomes or coral placement.
- Ecosystem/spreading mod precedents (Dynamic Trees, Ecologics, Serene Seasons, moss/grass spreading mods) not checked; only Nether Portal Spread was examined.
- Herbivore claim [S8] seen only via search summary of a PubMed record; the abstract was not fetched directly.
- Vanilla coral random tick behaviour is out of this slice (slice A).

## Injection sweep
No instruction-like text found in fetched pages, GitHub files, Modrinth API responses or search results. Serilum's Modrinth body contained promotional links (Patreon, YouTube) only, not instructions.
