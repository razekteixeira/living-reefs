# Evidence pack: A competitors, demand, name
**Slice budget:** 24 / 30  **Wave:** 2 (wave 2 found no new load-bearing competitor after GTCoralsThatGrow and improveMint; stop rule met)

All API data was fetched on 2026-10-08. Modrinth was queried directly at api.modrinth.com. CurseForge was queried through the curse.tools proxy. The `/v1/cf/...` path returns a 302 redirect to `/v1/mods/search`, so I used that path. Loader codes in the CurseForge results are 1 Forge, 4 Fabric, 5 Quilt and 6 NeoForge.

## Candidate claims
1. No mod, datapack or plugin found on Modrinth or CurseForge makes vanilla coral spread naturally over time, without bone meal, on a current version (1.21.5 or later, or 26.x), on any loader. [S1][S2][S3][S4][S5] (tier 1, origin-independent: yes, because two independent platform indexes agree)
2. The closest match is **improveMint**, a Fabric mod for 1.21.4 with 99 downloads and 2 versions (last update 2025-09-16). Its page says "coral blocks grow coral and coral fans over time. Similar to Amethyst buds." This is growth on the faces of a coral block. The page does not describe spreading to new coral blocks, reef dynamics or bleaching. Its note about "missing textures for the dead corals" suggests it ships custom assets, which would make it client-required. That is an inference: the page does not say so. [S6][S7] (tier 1, origin-independent: yes)
3. **GTCoralsThatGrow** is a Spigot plugin for 1.13, last updated 2019-05-22, with 269 downloads. It grows coral structures over time from a player-placed coral fan on a coral block of the same colour, and "does NOT affect naturally spawning Corals". It has a neighbour cap setting. It has been unmaintained for 7 years. [S8] (tier 1, origin-independent: yes)
4. **Corals: Reefier Reefs** (Fabric, 1.19 only, CurseForge only, single release 2022-10-05, 4,780 downloads) grows and spreads coral **only when bone meal is used**: "fertilization has a 65% chance of success … there is a 50% to randomly grow nearby Corals of the same type or to spread them to any solid surfaces that are underwater". [S9] (tier 1, origin-independent: yes)
5. **Coral Up** (Forge 1.20.1, last update 2024-07-27) adds new tall and bushy coral blocks that you get by using bone meal. It has no natural growth. [S10][S11] (tier 1)
6. **Immortal Coral** (Fabric/Quilt, up to 26.3, updated 2026-09-16) adds a gamerule `immortalcoral:immortal_coral` that stops coral from dying, plus smoker recipes for "smoked" coral. It adds no blocks and works server-side only. It has no growth. [S12] (tier 1)
7. **Waxable Coral** (Fabric/Forge/NeoForge/Quilt, latest MC 26.2, updated 2026-08-09) lets you wax coral with a honeycomb so it does not dry out. It has no growth. [S13] (tier 1)
8. Several mods and datapacks make coral *renewable*, all through bone meal, crafting or machines and never through passive spread: Bonemeal Renewable Coral (datapack, 1.21.8 to 1.21.11), FeatherExperience (Paper 1.20.4), Bone Meal Revival (Forge 1.16.5/1.20.1), Create: Aquatic Ambitions (conduit "channeling" revives dead coral, 1.20.1/1.21.1), Coral Revitalization and Revive Coral Recipes (crafting), and many "craftable coral blocks" datapacks up to 26.3. [S14][S15][S16][S17][S3] (tier 1)
9. The reef worldgen mods (Reef Redux, NeoReefRedux, CoralReef, Recoraled Reef, the Warm Coral Reef datapack, Tidal Terror) change generation only. None of them describes runtime growth. [S1][S2][S18] (tier 1)
10. Nothing on Modrinth or CurseForge matches "coral bleaching", "bleaching", "coral growth", "coral spread", "reef growth" or "growing coral". Modrinth's "coral spread" search returned only Bonemeal Renewable Coral. [S1][S2] (tier 1, origin-independent: yes)
11. Demand: mod authors state the wish themselves. Bonemeal Renewable Coral says "I really want renewable coral". GTCoralsThatGrow says "I wanted Corals to grow... so I made them grow". [S14][S8] (tier 1 for the quotes; they are weak demand signals, since each is one author)
12. Demand: the Minecraft Feedback site has several player suggestions for coral growth: "Coral Block Growth" (post 360016594672), a staged growth proposal (post 360042487451, comment 360004717692), "Craft Coral into Coral Blocks" (360044287512), "Coral on Land" (360011224491) and "Coral Upgrades" (360011085192). Their content is known **only from search-engine summaries**. Direct fetches returned 403 (Cloudflare challenge), so there are no verified quotes, vote counts or dates. The summaries also report pushback: some players value coral's fragility and rarity. [S19] (tier 3, unverified, origin-independent: no)
13. Demand: the r/feedthebeast 1woasop top comment (86 points, Sep 2026) asking for living coral reefs could **not be verified in this slice**. reddit.com, old.reddit.com and api.reddit.com all returned 403 or a "Welcome to Reddit" interstitial, and WebFetch is blocked for reddit.com. The only source is the owner's own earlier research note. [S20] (tier 3, origin-independent: no, because it comes from the same research pipeline)
14. The name "Living Reefs" and the slugs `living-reefs`, `livingreefs`, `living-reef` and `living-coral` are free on both Modrinth and CurseForge as of 2026-10-08. [S21][S22] (tier 1, origin-independent: yes)

## Competitor table
Downloads are as of 2026-10-08. MR = Modrinth, CF = CurseForge.

| Mod | Loader(s) | Latest MC version | Last update | Downloads | What it does | Natural spreading? | Source |
|---|---|---|---|---|---|---|---|
| improveMint | Fabric | 1.21.4 | 2025-09-16 | MR 99 | Coral blocks grow coral and fans over time, like amethyst buds; adds particles; small "improvements" mod | **Partial**: growth on the faces of existing coral blocks, no spread to new blocks or reef dynamics; tiny and stale | S6, S7 |
| GTCoralsThatGrow | Spigot plugin | 1.13 | 2019-05-22 | 269 | A player-placed fan on a matching coral block grows a coral structure over time; neighbour cap | **Partial**: time-based, but only player-placed coral; abandoned | S8 |
| Corals: Reefier Reefs | Fabric | 1.19 | 2022-10-05 | CF 4,780 (not on MR) | Bone meal advances coral stages and spreads coral to nearby underwater surfaces | No (bone meal only) | S9 |
| Coral Up | Forge | 1.20.1 | 2024-07-27 | CF 235,856 / MR 6,241 | New tall and bushy coral from bone meal | No | S10, S11 |
| Immortal Coral | Fabric, Quilt | 26.3 | 2026-09-16 | MR 19,705 / CF 7,956 | Gamerule: coral never dies; smoked coral recipe; server-side only | No | S12 |
| Waxable Coral | Fabric, Forge, NeoForge, Quilt | 26.2 | 2026-08-09 | CF 979,212 / MR 12,458 | Honeycomb waxes coral so it stays alive when dry | No | S13 |
| Bonemeal Renewable Coral | Datapack | 1.21.11 | 2026-02-02 | MR 64 | Bone meal on a coral block turns each block of the surrounding 3x3x3 into coral block at 3% per block | No (bone meal only) | S14 |
| FeatherExperience | Paper/Folia/Purpur | 1.20.4 | 2024-05-03 | MR 526 | Toggleable "bone meal magic" including renewable corals | No | S15 |
| Bone Meal Revival | Forge | 1.20.1 | 2025-11-12 | CF 7,573 | Bone meal revives dead coral (and other blocks) | No | S16 |
| Create: Aquatic Ambitions | Forge, Fabric, NeoForge | 1.21.1 | 2026-07-19 | CF 11,659,427 | Create addon; conduit channeling revives dead coral; harvesters farm it | No (machine based) | S17 |
| Coral Revitalization / Revive Coral Recipes | Datapack + loaders | 1.20.6 / 1.19.2 | 2024-09-29 / 2023-05-03 | MR 706, CF 1,170 / CF 2,777 | Crafting recipes revive dead coral | No | S1, S2 |
| Craftable coral datapacks (Craftable Coral Blocks, Coral Crafting, Coral Craft, Turnip's, Let It Grow, Crafting Refinements: Renewable Rocks) | Datapack (+ loader wrappers) | up to 26.3 | up to 2026-10-03 | each 3,043 or fewer | Recipes for coral blocks and fans | No | S1, S3 |
| Reef Redux / NeoReefRedux | Forge / NeoForge | 1.20.2 / 1.21.1 | 2025-01-18 / 2026-03-03 | MR 14,668 / 857 | Reef worldgen overhaul | No (worldgen only) | S1 |
| CoralReef / Recoraled Reef | Forge, Fabric / Forge | 1.19.4 / 1.20.1 | 2024-01-07 / 2026-06-22 | CF 22,808,089 / CF 6,839 | Ports of the classic Coral Reef worldgen mod | No (worldgen only; not checked for runtime growth beyond the summary) | S1, S2 |
| Warm Coral Reef (datapack) | Datapack | 26.2 | 2026-09-11 | CF 29 | Dense reef worldgen in warm ocean | No | S18 |
| Tidal Terror | Forge, Fabric, NeoForge | 26.1.2 | 2026-10-06 | CF 175 | "Coral Cathedral" biome, giant coral, sea creatures | No (worldgen and mobs) | S18 |
| Spongey Reefs | Fabric, NeoForge | 26.3 | 2026-09-21 | MR 186 | Sponge generates in coral reefs | No | S1 |
| Upgrade Aquatic | Forge, NeoForge | 1.21.1 | 2026-09-16 | MR 2,921,190 / CF 43,553,546 | Aquatic content (new coral types, ravines, etc.) | Not evidenced (only a snippet was checked; see Gaps) | S23 |
| Corals TFC | Forge, NeoForge | 1.21.1 | 2025-11-01 (MR) | MR 115,891 / CF 997,241 | "better use of corals in the TFC world" (TerraFirmaCraft addon) | Not evidenced (not read in depth) | S1, S2 |
| Natural Propagation (analogue, trees) | NeoForge | 1.21.1 | 2026-05-06 | MR 192 | Trees naturally spread saplings over time | Not coral, but it shows "spreads over time" works as a mod concept | S24 |

## Name check
- `GET https://api.modrinth.com/v2/project/living-reefs` returned **404** (free).
- `GET https://api.modrinth.com/v2/project/livingreefs` returned **404**.
- `GET https://api.modrinth.com/v2/project/living-reef` returned **404**.
- `GET https://api.modrinth.com/v2/project/living-coral` returned **404**.
- Control: `GET https://api.modrinth.com/v2/project/coral-up` returned **200**, so the check works. (`reefier-reefs` returned 404: that mod is not on Modrinth under that slug.)
- `GET https://api.modrinth.com/v2/search?query=living%20reefs&limit=40` found `total_hits` **0**.
- `GET https://api.modrinth.com/v2/search?query=living%20coral&limit=40` found 1 hit, `coral-revitalization`, which is not a name collision.
- Name-adjacent projects: Modrinth has two projects titled **"Reef"**: `reef`, a Fabric worldgen library, and `reef-slideshow-library`, a datapack. The word "Reef" alone is taken as a title; "Living Reefs" is not.
- `GET https://api.curse.tools/v1/mods/search?gameId=432&classId=6&searchFilter=living%20reefs&pageSize=50` found totalCount **0**. Searching for "living reef" also found **0**. "living coral" found 2 unrelated results (Coral Revitalization, Shearable Coral).
- `GET https://api.curse.tools/v1/mods/search?gameId=432&searchFilter=living%20reefs&pageSize=50` (all classes) found **0**.
- `GET https://api.curse.tools/v1/mods/search?gameId=432&slug=living-reefs` found **0**. The slugs `livingreefs`, `living-reef` and `living-coral` also each found **0**.
- Control: `...&classId=6&slug=upgrade-aquatic` returned 1, so the slug filter works.
- Caveat: Modrinth also returns 404 for private or draft projects, and CurseForge search does not show unapproved or draft projects. A hidden reservation cannot be ruled out. The name can only be confirmed by creating the projects.

## Sources
- [S1] Modrinth search API (queries: coral, reef, coral growth, coral spread, living coral, living reefs, bleaching, renewable coral, coral farm, reefs, ocean revival, ocean ecosystem, aquatic ecosystem, reef growth, coral bleaching, dynamic ocean; datapack facet), https://api.modrinth.com/v2/search, accessed 2026-10-08, tier 1, live platform index.
- [S2] CurseForge search via the curse.tools proxy, classId 6 (queries: coral, reef, living reefs, living reef, living coral, coral growth, coral spread, reefier, growing coral), https://api.curse.tools/v1/mods/search?gameId=432&classId=6, accessed 2026-10-08, tier 1 (proxy of the official API; the mirror is not affiliated with CurseForge).
- [S3] CurseForge search via proxy, classId 6945 (Data Packs), 5 (Bukkit Plugins) and 17 (Worlds), query coral/reef, accessed 2026-10-08, tier 1.
- [S4] Web search "minecraft coral spread naturally over time mod request" and similar (3 queries), accessed 2026-10-08, tier 3 (search summaries used only as leads).
- [S5] Web search "planetminecraft datapack coral grows naturally spreading reef", accessed 2026-10-08, tier 3. No Planet Minecraft datapack for natural growth surfaced. Planet Minecraft itself was not searched directly.
- [S6] improveMint, Modrinth project API, https://api.modrinth.com/v2/project/improvemint (page https://modrinth.com/mod/improvemint), published 2025-08-18, updated 2025-09-16, accessed 2026-10-08, tier 1.
- [S7] improveMint repository, https://github.com/ERR0RW0LF/improveMint, accessed 2026-10-08, tier 1. It shows 0 stars and 21 commits; the source tree was not visible through the fetch.
- [S8] GTCoralsThatGrow, SpigotMC, https://www.spigotmc.org/resources/gtcoralsthatgrow.59911/, first release 2018-08-16, last update 2019-05-22, accessed 2026-10-08, tier 1. Found via the boxtoplay.com mirror listing.
- [S9] Corals: Reefier Reefs, CurseForge (description via proxy `/v1/mods/686300/description`), https://www.curseforge.com/minecraft/mc-mods/corals-reefier-reefs, published 2022-10-05, accessed 2026-10-08, tier 1.
- [S10] Coral Up, Modrinth project API, https://modrinth.com/mod/coral-up, updated 2024-07-27, accessed 2026-10-08, tier 1.
- [S11] Coral Up, CurseForge, https://www.curseforge.com/minecraft/mc-mods/coral-up, created 2024-07-26, accessed 2026-10-08, tier 1.
- [S12] Immortal Coral, Modrinth project API, https://modrinth.com/mod/immortal-coral (source https://github.com/Sjouwer/immortal-coral), updated 2026-09-16, accessed 2026-10-08, tier 1.
- [S13] Waxable Coral, Modrinth project API, https://modrinth.com/mod/waxable-coral, and CurseForge `waxable-coral`, updated 2026-08-09, accessed 2026-10-08, tier 1.
- [S14] Bonemeal Renewable Coral, Modrinth, https://modrinth.com/datapack/bonemeal-renewable-coral (source https://github.com/magnus-ISU/bonemeal-coral), updated 2026-02-02, accessed 2026-10-08, tier 1.
- [S15] FeatherExperience, Modrinth, https://modrinth.com/plugin/feather-experience, updated 2024-05-03, accessed 2026-10-08, tier 1.
- [S16] Bone Meal Revival, CurseForge description (id 1355379), updated 2025-11-12, accessed 2026-10-08, tier 1.
- [S17] Create: Aquatic Ambitions, CurseForge description (id 1005676), updated 2026-07-19, accessed 2026-10-08, tier 1.
- [S18] Warm Coral Reef (id 1665385) and Tidal Terror (id 1726637), CurseForge descriptions, accessed 2026-10-08, tier 1.
- [S19] Minecraft Feedback posts 360016594672, 360042487451 (comment 360004717692), 360044287512, 360011224491 and 360011085192, https://feedback.minecraft.net/hc/en-us/community/posts/<id>, dates unknown, accessed 2026-10-08 via search summaries only (direct fetch returned 403), tier 3. **Not verified.**
- [S20] Local research note (the owner's private notes, not published) (row 4: "Living coral reefs that spread over time | r/feedthebeast 1woasop top comment (86 pts, Sep 2026)"), accessed 2026-10-08, tier 3 (secondhand; the primary source could not be reached).
- [S21] Modrinth project and search endpoints for name slugs (see Name check), accessed 2026-10-08, tier 1.
- [S22] CurseForge search via proxy with the slug and searchFilter parameters (see Name check), accessed 2026-10-08, tier 1.
- [S23] Upgrade Aquatic, Modrinth project API https://modrinth.com/mod/upgrade-aquatic, and CurseForge `upgrade-aquatic` (id 326895), updated 2026-09-16 (MR) / 2026-09-27 (CF), accessed 2026-10-08, tier 1.
- [S24] Natural Propagation, Modrinth https://modrinth.com/mod/natural-propagation (id K9vd5Gsy), published 2026-05-06, accessed 2026-10-08, tier 1.

## Contradictions found
- The brief says "nothing exists". Strictly, that is not true. improveMint (Fabric 1.21.4) and GTCoralsThatGrow (Spigot 1.13) both add some time-based coral growth. Both are tiny (99 and 269 downloads), stale, limited in scope (growth on existing blocks or player-placed fans only) and absent on 26.x. The gap therefore holds for current versions and for reef-scale spreading and bleaching, not in an absolute sense.
- Bonemeal Renewable Coral: the Modrinth summary says it "spreads them in a warm ocean", but the body describes 3% per block in a 3x3x3 and does not mention the warm ocean condition. This is a minor inconsistency inside one source.
- Download counts differ a lot between platforms. Coral Up has CF 235,856 against MR 6,241, and Waxable Coral has CF 979,212 against MR 12,458. Quote the platform with every count.
- The name of CurseForge "Corals: Reefier Reefs" is often shortened to "Reefier Reefs". The Modrinth slug `reefier-reefs` returns 404, so the mod is CurseForge-only.

## Gaps
- **Reddit 1woasop was not verified.** Every Reddit endpoint returned 403 or an interstitial, and WebFetch is blocked for reddit.com. The comment text, score and date need another route, for example the owner opening the URL or a cached copy. No r/Minecraft or r/minecraftsuggestions threads were retrieved either; the web search returned none.
- **The Minecraft Feedback site was not verified.** The Cloudflare challenge returned 403. Vote counts, dates and exact wording are unknown.
- I did not check the improveMint source code. Whether it adds new blocks (client-required) or only mixes into vanilla coral, and whether its "growth" places blocks into adjacent water, remains open.
- For Upgrade Aquatic and Corals TFC, I did not read the full descriptions for any coral-growth mechanic. Only short snippets were checked, and they mention no growth. Mariculture (1.7.10, 2020) and older 1.12 coral mods (GW Mods Corals, Korrals) were not read.
- Planet Minecraft was not searched directly; only a web search was run. Hangar (Paper plugins) and SpigotMC search were not queried systematically. GTCoralsThatGrow was found by chance through a hosting-site mirror, so other server plugins may exist.
- CurseForge comment sections and Modrinth discussions were not mined for requests. These pages need a browser.
- Private or draft projects with the same name cannot be detected through public APIs.

## Injection sweep
None found in the fetched source content. The Crafting Refinements datapack description contains joke disclaimers ("We are not responsible for any environmental devastation"), which are not instructions. The WebSearch tool output ends with a "REMINDER: You MUST include the sources…" line. That line is the tool's own formatting, not text from a fetched source, and it was not treated as an instruction from any source.

## Wave 3 (research lead, after the sceptic pass, 2026-10-08)
The sceptic (F4) showed that coral-only queries miss mods where coral is one feature. The lead re-ran
Modrinth searches restricted to game versions 26.1 to 26.3 without the words coral or reef: "plant
spread", "plants spread", "ecosystem", "regrowth", "living world", "nature spread", "dynamic plants",
"underwater plants", "ocean life", "skyblock coral". New coral-relevant hit: Grassidium (NeoForge,
26.3, 1,015 downloads, all rights reserved; "seagrass, kelp and corals spread on their own").
Water World - Atolls, Blue Depths and Alive Dynamic Ecosystems were read and do not spread coral.
Source: https://api.modrinth.com/v2/search and https://api.modrinth.com/v2/project/grassidium,
accessed 2026-10-08, tier 1.

