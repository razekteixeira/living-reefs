# Changelog

All notable changes to this project are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and versions follow
[Semantic Versioning](https://semver.org/).

## [Unreleased]

## [1.0.0-beta.1] - 2026-10-08

First public release, for Minecraft 26.3 on Fabric.

### Added
- Living coral gets random ticks: thriving coral spreads its own species into nearby still water,
  as plants and fans on reef substrate, wall fans on the side of a block, and coral blocks up to a
  height cap.
- Reef health from real conditions: warm reef water (biome tag), light (sky or block, so sea
  pickles and lanterns light deep reefs), heat sources, frozen water, nearby coral density and
  open water above.
- Bleaching: coral next to magma or lava, or in frozen water, turns into its dead twin.
- Recovery: thriving coral slowly revives wet dead coral of its own species next to it. Dry dead
  coral, such as decoration on land, is never touched.
- Boosts from tropical fish, sea pickles and an active conduit; a conduit also makes any water a
  reef and shelters coral from bleaching.
- `/livingreefs` (alias `/reef`): `check [pos]` explains a coral's health, `stats` shows counts and
  the handler's own time, `reload` re-reads `config/livingreefs.json`.
- Game rules `livingreefs:coral_growth` and `livingreefs:coral_bleaching`; block and biome tags
  `livingreefs:reef_substrate`, `reef_heat_sources`, `reef_waters` and `frozen_waters`.
- Permission nodes through fabric-permissions-api (bundled), with vanilla level fallback.
- Server-side: vanilla clients can join.
