![Living Reefs: coral reefs that grow, bleach and recover on their own](https://razekteixeira.github.io/living-reefs/media/banner.png)

# Living Reefs

**Coral reefs that grow, bleach and recover on their own.**

In vanilla Minecraft a coral reef is a painting: coral only grows from bone meal and only ever dies. Living Reefs makes reefs live. Coral spreads its own kind across the sea floor, turns white next to a magma vent, and comes back once the heat is gone. Restore a dead reef, farm one, or just watch it grow while you build.

Server-side only. Players join with a vanilla client.

![Five coral fragments on bare sand grow into a reef](https://razekteixeira.github.io/living-reefs/media/timelapse.gif)

## Features

- **Coral spreads its own kind** into nearby still water: plants and fans on sand, wall fans on the side of a block, and new coral blocks that build the reef up to three high.
- **Bleaching:** coral next to magma or lava, or in frozen water, turns into its dead, white twin.
- **Recovery:** remove the heat and thriving coral slowly takes back dead coral of its own kind right next to it. Plant living fragments next to a dead reef to bring it back.
- **What a reef likes:** warm ocean water, light (sea pickles light deep reefs), tropical fish and sea pickles nearby (growth x1.5 each), and an active **conduit** (x2), which also makes any water a reef and shelters coral from bleaching.
- **`/reef check`** tells you whether a coral is thriving, dormant or stressed, and why.
- **Never overgrows:** new coral only fills still water on sand, gravel, stone, clay or coral, never replaces a block, keeps two blocks of water above it, and stops where 14 coral are already within two blocks.
- **Safe for your builds:** coral outside warm oceans just stays as it is, dry dead coral (decoration) is never touched, and nothing is saved on the side: every change is a vanilla coral block.
- **Light on the server:** runs on vanilla random ticks, so only near players, at `random_tick_speed`, and never loads chunks. A warm ocean reef of 8,192 coral costs about 0.1 ms per tick at default speed.

## What it looks like

Real output of `/reef check` from the screenshots below:

<!-- CHAT:check -->
```
Horn Coral Fan at -599 53 2644 is thriving
  ✔ warm reef water
  ✔ light 15, needs 1
  + 6 tropical fish nearby (x1.5)
  + 2 sea pickles nearby (x1.5)
  17 coral within 2 blocks (crowded at 14)
  Tries to spread about every 76 s while players are near
```
<!-- /CHAT:check -->

Next to a magma vent:

<!-- CHAT:stressed -->
```
Horn Coral at -597 53 2643 is stressed
  ✘ magma or lava within 2 blocks
  12 coral within 2 blocks (crowded at 14)
  Bleaches in about 2 min if nothing changes
```
<!-- /CHAT:stressed -->

In game it is colour coded: green when thriving, yellow when dormant, red when stressed.

![/reef check on a thriving coral](https://razekteixeira.github.io/living-reefs/media/check.png)

![/reef check next to a magma vent](https://razekteixeira.github.io/living-reefs/media/check-stressed.png)

![From five fragments to a reef](https://razekteixeira.github.io/living-reefs/media/before-after.png)

![Bleached by a magma vent, then recovering](https://razekteixeira.github.io/living-reefs/media/bleach-recover.png)

## Gallery

![A natural warm ocean reef under water](https://razekteixeira.github.io/living-reefs/media/gallery-reef.png)

![The restored reef up close](https://razekteixeira.github.io/living-reefs/media/gallery-restored.png)

![The restored reef at night, lit by sea pickles](https://razekteixeira.github.io/living-reefs/media/gallery-night.png)

Every screenshot is a real capture from the game client, made by the mod's own capture test. The time-lapse is sped up with `random_tick_speed`.

## How it works

Living coral gets random ticks, like grass and crops. Most ticks do nothing; now and then a coral reads its surroundings:

| Health | When | What happens |
|---|---|---|
| Thriving | warm, lukewarm or deep lukewarm ocean, or near an active conduit, with light of at least 1 | tries to spread, or revives dead coral of its kind next to it |
| Dormant | anywhere else, too dark, or sheltered by a conduit | nothing |
| Stressed | magma or lava within 2 blocks, or frozen water | bleaches into its dead twin |

At default speed a thriving coral tries to spread about every half hour, only near players.

## Commands

`/livingreefs` and the short alias `/reef` are the same command.

| Command | Who | What it does |
|---|---|---|
| `/reef check [pos]` | everyone | Health of the coral you look at, and why |
| `/reef stats` | everyone | Growth, recovery and bleaching since start, and the mod's own time |
| `/reef reload` | operators | Re-read `config/livingreefs.json` |

Game rules: `livingreefs:coral_growth` and `livingreefs:coral_bleaching` (both `true`).

## For server owners

- **Permissions:** with a permissions mod such as LuckPerms, grant `livingreefs.use` and `livingreefs.admin`. Without one, operator level 3 is needed to reload.
- **Config:** `config/livingreefs.json` holds every chance, radius, cap and boost. Out-of-range values are clamped with a warning; a broken file is reported and left untouched.
- **Datapack tags:** `livingreefs:reef_waters` and `frozen_waters` (biomes), `reef_substrate` and `reef_heat_sources` (blocks).
- **Compatibility:** tested with Lithium. Modded coral that extends the vanilla coral blocks and is named like vanilla coral works automatically.

## Install

Needs Minecraft **26.3**, Fabric Loader 0.19.5 or newer, **Fabric API** and Java 25.

- **Server:** put the Living Reefs jar and Fabric API in the server's `mods/` folder.
- **Singleplayer:** install Fabric Loader for 26.3, then put both jars in `.minecraft/mods`.

## Links

- Source, issues and changelog: [github.com/razekteixeira/living-reefs](https://github.com/razekteixeira/living-reefs)
- Project page: [razekteixeira.github.io/living-reefs](https://razekteixeira.github.io/living-reefs/)

Open source under the Apache License 2.0.

NOT AN OFFICIAL MINECRAFT PRODUCT. NOT APPROVED BY OR ASSOCIATED WITH MOJANG OR MICROSOFT.
