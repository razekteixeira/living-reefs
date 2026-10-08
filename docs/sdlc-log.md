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
| F7 Lithium read on develop branch | MEDIUM | Stated in Q5 and limitations. |
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
| AC15 cost | measured | `tools/benchmark.sh`, 8,192 coral in a 64 x 64 warm-ocean reef, 40 s warm-up, 15 readings of the handler's own time: `random_tick_speed` 3: min 0.609, median 0.952, max 2.641 ms per second of game time (126 coral random ticks per second); 300: min 20.987, median 31.596, max 37.775 ms per second (11,982 per second). A first run gave identical readings at 300: the empty server had paused (`pause-when-empty-seconds`, default 60 s since 1.21.2); the benchmark now disables the pause and fails if the log shows one. |
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
