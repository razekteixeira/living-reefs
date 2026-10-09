# Living Reefs: gate log

Agentic SDLC central version 2026-09-16+2. One entry per gate, newest evidence last. Operators:
the orchestrating Claude Code session (Claude Opus 5.5) and fresh-context subagents on the same
model; independence comes from fresh context (I2 Distinct run), recorded per gate.

## WI-1 Research (Deep research team)

| Gate | Status | Evidence |
|---|---|---|
| G0 Intake ready | passed | `docs/research/brief.md`: questions Q1 to Q6, tags, effort M, budgets, stop rule. Deviation: the plan was not shown to the owner for approval before retrieval (unattended goal run); the questions came from the owner's session brief. |
| G1 Research brief ready, G2 Plan ready | passed (collapsed) | `docs/research/brief.md`, four slices with closure criteria. |
| G3 Draft report ready | passed | Evidence packs A to D in `docs/research/evidence/`, single-writer draft `docs/research.md`. |
| G4 Claims with teeth | passed after fixes | `docs/research/claims.md`: 42 claims checked against fetched sources and the 26.3 code; 22 supported, 14 partial, 5 unsupported, 1 contradicted. All 14 required fixes applied; unsupported claims cut, hedged or listed as limitations. Rows 43 to 48 added for post-sceptic claims (checked by the sceptic and the lead). |
| G5 Sceptic clean | passed after resolution | `docs/research/sceptic.md`: 1 CRITICAL, 3 HIGH, 3 MEDIUM, 4 LOW. Resolutions below. |
| HumanDecision (stop rule) | decided | 2026-10-08: the stop rule was raised because Grassidium (NeoForge, 26.3) spreads coral as a generic plant. Asked through the session's question prompt with options "Differentiate (Recommended)" and "Stop". The owner answered **"Differentiate (Recommended)"**. The build continues on Fabric with reef-specific behaviour. |
| G6 Independent review | passed after fixes | `docs/research/review.md`: verdict approve with changes; required changes 1 to 9 applied (gate status wording, this log, ledger rows, citation keys `[Sk-C2]` to `[Sk-C5]`, summary calibration, mixin-collision wording, fish hedge, worldgen not re-verified note, limitations). Non-blocking 10 and 11 applied, 12 left out. Reviewer: fresh-context subagent (I2). |
| G7 Report published | passed | `docs/research.md` in the repository with the provenance annex. Distribution: public. |
| G8 Learning captured | see the closing section | |

### Sceptic findings and resolutions

| Finding | Severity | Resolution |
|---|---|---|
| F1 Grassidium spreads coral on 26.3 | CRITICAL | Added to the competitor table and summary; stop rule raised; owner decided to differentiate. |
| F2 Carpet Sky Additions Reborn `spreadingCoral` | HIGH | Added to the table; its default value listed as a limitation. |
| F3 Regrowth (fish grow coral, up to 1.21.11) | HIGH | Added to the table and to compatibility (density cap counts its coral). |
| F4 Search method used only coral words | HIGH | Generic re-run by the lead (wave 3 in pack A); no further project found. |
| F5 Mixin collision on `CoralBlock` | MEDIUM | Design injects into existing base-class methods; collision documented in Q6 and in the README. |
| F6 Light threshold uncalibrated | MEDIUM | Calibrated during the build on real worldgen reefs (WI-2 below). |
| F7 Lithium read on develop branch | MEDIUM | Closed by a real run with Lithium 0.26.2 (WI-2 below). |
| F8 Chunk-loading bound only within 16 blocks | LOW | Stated in Q5; all world reads go through loaded chunks only. |
| F9 "coral spread" search wording | LOW | Reworded. |
| F10 Vanilla client join untested | LOW | Manual join test in the release checklist. |
| F11 Planet Minecraft not searched | LOW | Listed as a limitation. |

## WI-2 Mod

| Gate | Status | Evidence |
|---|---|---|
| G0 Intake ready, G1 Spec/Design ready, G2 Plan ready | passed | `docs/spec.md` (outcome case, tags, effort L split into WorkItems, AC1 to AC15 with named tests, scope boundary). |
| G3 Build green | passed | `./gradlew test runGameTest`: 41 JUnit tests and 12 server GameTests pass on a headless 26.3 server (2026-10-08). |
| G4 Tests with teeth | passed | `python3 -P tools/mutation.py`: 25 planted mutants (15 in `core` and config, 10 in the Minecraft layer), each killed by its named test, none by a compile error; table below. Discrimination fix found while planting: the bleaching GameTest used the default `facing=north`, so a mutant dropping `facing` would have survived; the test now uses east. A redundant waterlogging branch in `ReefTicker.place` was removed instead of mutated (equivalent mutant). |
| AC15 cost | measured | `tools/benchmark.sh`, fresh flat world, 8,192 coral in a 64 x 64 warm-ocean reef (verified with `/reef check`: "warm reef water"), 40 s warm-up, 15 readings of the handler's own time: `random_tick_speed` 3: min 0.650, median 1.911, max 3.424 ms per second of game time (110 coral random ticks per second); 300: min 26.084, median 43.356, max 49.847 ms per second (11,965 per second). Two earlier runs were invalid and are not used: the first read a paused server (`pause-when-empty-seconds`, 60 s by default since 1.21.2); the second built its water and biome with `fill`/`fillbiome` over the `max_block_modifications` limit (32,768), which failed silently, so the coral sat in plains. The script now disables the pause, raises the limit, fails on any setup error and checks the reef reports reef water. |
| F7 Lithium | tested | `tools/compat-lithium.sh` with Lithium 0.26.2+mc26.3 (Modrinth, SHA-512 checked) on a fresh flat world: 49 coral blocks spaced 8 apart (sparse sections, Lithium's own random tick path) at `random_tick_speed` 300 for 30 s: 2,658 coral random ticks (the expected rate is about 72 per second plus new coral) and 29 coral grown. |
| F6 light calibration | done | Client capture "[reef survey]": 820 to 911 living coral around a natural warm ocean reef (seed `living-reefs`); light min 0, median 9, max 15; 33 to 34 below 1 (about 4 %); depth median 7, max 17. Default `minLight` 1 lets about 96 % of worldgen reef coral grow. |

### G4 mutation table (2026-10-08)

| Mutant | Planted fault | Killed by |
|---|---|---|
| M1 | species table keeps forms without a dead twin | `SpeciesTableTest.formsWithoutBothTwinsAreLeftOut` |
| M2 | parser accepts `dead_dead_` ids | `SpeciesTableTest.parseRejectsMalformedIds` |
| M3 | heat never stresses | `ReefRulesTest.heatStresses` (and 3 more) |
| M4 | conduit never shelters | `ReefRulesTest.conduitMakesWaterReefAndProtects` |
| M5 | light threshold off by one | `ReefRulesTest.darkCoralIsDormant` |
| M6 | recovery band ignores the growth band | `ReefRulesTest.decideUsesTheRollBands` |
| M7 | early-exit gate ignores bleaching | `ReefRulesTest.gateCoversEveryAction` |
| M8 | water clearance one block short | `SpreadPlannerTest.respectsWaterClearance` |
| M9 | density cap off by one | `SpreadPlannerTest.respectsDensityCap` |
| M10 | reef height cap off by one | `SpreadPlannerTest.coralBlockHeightIsCapped` |
| M11 | wall fan support on the wrong side | `SpreadPlannerTest.wallFanFacesAwayFromSupport` |
| M12 | recovery revives other species | `SpreadPlannerTest.recoveryNeverTouchesDryOrOtherSpeciesOrLiving` |
| M13 | recovery revives dry coral | `SpreadPlannerTest.recoveryNeverTouchesDryOrOtherSpeciesOrLiving` |
| M14 | NaN chance kept | `SettingsTest.notANumberIsClampedNotKept` |
| M15 | config loaded without clamping | `LivingReefsConfigTest.outOfRangeValuesAreClampedOnLoad` |
| G1 | wall fans do not random tick | `livingCoralRandomTicksDeadDoesNot` |
| G2 | mixin no longer calls the ticker | `randomTicksGrowAReef` |
| G3 | bleaching drops the block state | `heatBleachesCoral` |
| G4 | substrate tag ignored | `glassFloorStopsGrowth` (and `wallFansGrowOnEverySide`) |
| G5 | heat sources never seen | `heatBleachesCoral` (and 2 more) |
| G6 | conduit never seen | `conduitProtectsFromHeat` |
| G7 | growth game rule ignored | `randomTicksGrowAReef` |
| G8 | dead coral reported as living | `checkCommandExplainsHealth` |
| G9 | wall fan facing never set | `wallFansGrowOnEverySide` |
| G10 | west mapped to east | `wallFansGrowOnEverySide` |

G4 re-run after the G5 and G6 fixes (2026-10-09), `python3 -P tools/mutation.py`: 30 mutants, all
killed by their named test (M11 re-run alone after its target text was narrowed). The harness now
fails when only another test kills a mutant (G6 F15). New mutants:

| Mutant | Planted fault | Killed by |
|---|---|---|
| M16 | open-spot count ignores water clearance and the density cap | `SpreadPlannerTest.openSpotsCountWhatGrowthCouldUse` |
| G11 | bleaching game rule ignored in the ticker (G6 F4) | `bleachingRuleSwitchesBleachingOff` |
| G12 | light read only at the coral itself (G6 F5) | `darkCoralWaitsForLight` |
| G13 | dead coral next to heat revived (G6 F10) | `noRecoveryNextToHeat` |
| G14 | `/reef check <pos>` range limit removed (G5 S1) | `checkFarAwayNeedsAdmin` |

## WI-2 G5 Security clean and G6 Independent review (2026-10-09)

The first session stopped while both reviews were running; they were started again in a fresh
session. Both reviewers were fresh-context subagents (I2 Distinct run) and edited only their reports.

| Gate | Status | Evidence |
|---|---|---|
| G5 Security clean | passed after fixes | `docs/security-g5.md`: clean with fixes, 0 CRITICAL, 0 HIGH, 1 MEDIUM, 12 LOW. Resolutions below. `shellcheck` and `actionlint` clean. |
| G6 Independent review | passed after fixes | `docs/review-g6.md`: approve with changes, 0 CRITICAL, 0 HIGH, 6 MEDIUM, 14 LOW. Resolutions below. Visual check of real captures included. |
| HumanDecision (spread extent, G6 F6) | decided | 2026-10-09, asked through the question prompt with "Warm ocean only (Recommended)", "Reword only" and "Add a distance rule". The owner answered **"Warm ocean only (Recommended)"**: `reef_waters` is warm ocean only and the docs say reefs creep slowly across warm ocean floor. Recorded in `docs/spec.md`. |

| Finding | Severity | Resolution |
|---|---|---|
| S1 `/reef check <pos>` maps loaded chunks anywhere | MEDIUM | Without admin rights the position must be within 16 blocks, checked before the loaded test, so far positions all give the same answer. GameTest `checkFarAwayNeedsAdmin`, mutant G14. |
| S2 checkout persists credentials | LOW | `persist-credentials: false` in build and release. |
| S3 Loom snapshot | LOW | Pinned to 1.18.3; build and tests green. |
| S4 no wrapper checksum | LOW | `distributionSha256Sum` from services.gradle.org added. |
| S5 actions pinned by tag | LOW | Accepted (same as Throughput); Dependabot can be added later. |
| S6 token environment | LOW | Release steps in CONTRIBUTING say to add `CURSEFORGE_TOKEN` to a protected `release` environment. |
| S7 private vulnerability reporting off | LOW | Owner action: turn it on in the repository settings (listed in the handover). |
| S8 conduit scan unbounded | LOW | Accepted with G6 F8: bounded to 9 loaded chunks and only on assessed ticks; noted as a known cost. |
| S9 no claim protection | LOW | Documented in the README with the per-world game rule. |
| S10 broken config at start uses default levels | LOW | Accepted: the defaults are level 0 for check and stats (read-only) and 3 for reload; documented. |
| S11 absolute paths in research docs | LOW | Removed. |
| S12, S13 dev password file, Google Fonts | LOW | Accepted. |
| F1 chat text not from the capture run | MEDIUM | `branding/sync_chat.py` copies the run's `/reef check` output into the site and the CurseForge description; called by `make_media.sh`. |
| F2 check shots taken with sped-up settings | MEDIUM | The capture restores the defaults before both check shots. |
| F3 density line contradicts the outlook | MEDIUM | `/reef check` now reports the open spots a growth attempt could use (`SpreadPlanner.openSpots`, the same test as growth) and says when a coral has no room. |
| F4 spec names missing tests; bleaching rule untested in the glue | MEDIUM | `bleachingRuleSwitchesBleachingOff` in its own fast environment, mutant G11; spec test names fixed. |
| F5 `lightAt` untested | MEDIUM | `darkCoralWaitsForLight` (closed stone pool, then glowstone), mutant G12. |
| F6 "never overgrows" overclaims | MEDIUM | Owner decision above; README, site, FAQ and CurseForge text reworded. |
| F7 benchmark comment | LOW | Corrected: growth placement and block-entity-heavy chunks are not in the number. |
| F8, F9 conduit scan cost, gate dominated by bleach chance | LOW | Accepted as known costs (bounded; about 10 % at defaults). |
| F10 revive and re-bleach loop | LOW | Revival skips targets with heat within 2 blocks; `noRecoveryNextToHeat`, mutant G13. |
| F11 `minWaterAbove` 0 | LOW | Clamped to 1..8. |
| F12 coral block grown into an entity | LOW | Coral blocks need `isUnobstructed`; no dedicated test (review only). |
| F13 dead coral wording, dry coral outlook | LOW | Reworded; dry coral reports "Out of water: it dies like any vanilla coral". |
| F14 config left behind by a failed test | LOW | `checkCommandExplainsHealth` starts from the defaults; fast tests set their own. |
| F15 incidental kills counted | LOW | Fixed in `tools/mutation.py`. |
| F16 night shot looked like day | LOW | Default brightness for the shot; captions say "after dark"; no day-night claim. |
| F17, F18 dead `Facing.opposite`, light Javadoc | LOW | Removed and fixed. |
| F19 CurseForge categories | LOW | Main Server Utility; World Gen only as an additional category with an honest reason; Biomes dropped. |
| F20 RCON client | LOW | Detects a failed login and reads exact lengths. |
