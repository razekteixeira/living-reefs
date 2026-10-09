# G6 Independent review: re-check of the fixes

Reviewer: fresh-context re-verifier (I2 Distinct run), 2026-10-09. Scope: commit `76672d6` on
`build` ("Fix G5 and G6 findings..."), against `docs/review-g6.md`, `docs/security-g5.md` and the
claimed resolutions in `docs/sdlc-log.md` ("WI-2 G5 Security clean and G6 Independent review").
Read: the full diff of `src`, `tools` and `branding/sync_chat.py`, the current `LivingReefsCommands`,
`ReefTicker`, `ReefConditions`, `WorldReefView`, `SpreadPlanner`, `ReefRules` and the GameTests,
plus the README, site, CurseForge files and spec. Vanilla behaviour checked in the decompiled 26.3
sources (`CollisionGetter`, `EntityGetter`, `GameTestBatchFactory`, `GameTestRunner`,
`ServerGamePacketListenerImpl`, `TickThrottler`). I looked at the new captures `check.png`,
`check-stressed.png`, `gallery-night.png`, `recovered.png`, `bleach-after.png`, `bleach-recover.png`
and `gallery-restored.png`. Gradle was not run. The run evidence I used is the session's
`build.log`, `gt.log` and `mutation.log`, which say "All 16 required tests passed" and give the
mutant table.

## Verdict

**The fixes are verified, with notes.** All seven MEDIUM findings (G6 F1 to F6 and G5 S1) are
fixed, and so are the fifteen LOW findings marked fixed. The fixes add no CRITICAL, HIGH or MEDIUM
problem. I found six new LOW items. N1 is a small behaviour regression from the F10 fix: a coral
sheltered by a conduit near heat never revives. N2 is a docs claim about conduits and aquariums.
N3 is about captions on the new captures. The other three are housekeeping.

## Resolution table

| Finding | Status | Evidence |
|---|---|---|
| **S1** MEDIUM `/reef check <pos>` maps loaded chunks | VERIFIED | `LivingReefsCommands.java:46-48` (`CHECK_RANGE = 16`, generic `TOO_FAR`). `:92-99` `nearbyPos` reads the raw position with `getBlockPos`, rejects non-admins farther than 16 blocks from `source.getPosition()` (`:95`), and only then calls `getLoadedBlockPos` (`:98`). So far loaded and far unloaded positions give the same answer. GameTest `LivingReefsGameTests.java:398-425` checks three cases: level-0 near works, level-0 far gives "Stand within", admin far works. Mutant G14 is killed by `check_far_away_needs_admin`. Bypass analysis under Notes. |
| **F1** MEDIUM chat text not from the capture run | VERIFIED | `branding/sync_chat.py` parses `[CHAT]` lines from the client log and rewrites the `CHAT:check` and `CHAT:stressed` blocks. `make_media.sh:57` calls it. The text in `site/index.html:83-90` and `branding/curseforge-description.md:28-49` matches `check.png` and `check-stressed.png` character for character: "Brain Coral Block at -591 51 2649 is thriving / warm reef water / light 10, needs 1 / 5 tropical fish nearby (x1.5) / 1 open spot to spread to / Tries to spread about every 19 min..." and "Brain Coral at -592 51 2650 is stressed / magma or lava within 2 blocks / Bleaches in about 5 min...". |
| **F2** MEDIUM check shots under sped-up settings | VERIFIED | `LivingReefsCaptures.java:108` sets `random_tick_speed 3` and `:111` restores `original` before both check shots. The sped-up config comes back only after the stressed shot (`:141-142`). The numbers match the defaults: 4096 / (3 × 0.04 × 1.5) / 20 = 1138 s, which prints as 19 min, and 4096 / (3 × 0.25) / 20 = 273 s, which prints as 5 min. |
| **F3** MEDIUM density line contradicts outlook | VERIFIED | `LivingReefsCommands.java:143-149` prints open spots from `SpreadPlanner.openSpots` (`SpreadPlanner.java:97-116`), which uses the same `room()` test as `grow`. `:185-186` prints "Waits for room to spread" when there are none. Unit test `openSpotsCountWhatGrowthCouldUse`, mutant M16. |
| **F4** MEDIUM bleaching rule untested in the glue; wrong test names in the spec | VERIFIED | `bleachingRuleSwitchesBleachingOff` (`LivingReefsGameTests.java:327-348`) runs in its own `fast_rules` environment (`src/gametest/resources/.../fast_rules.json`). Mutant G11 (`bleaching = true` at `ReefTicker.java:51`) is killed by that test alone. The spec AC table (`docs/spec.md:117-133`) names only tests that exist; I grepped all ten unit-test names. |
| **F5** MEDIUM `lightAt` untested | VERIFIED | `darkCoralWaitsForLight` (`LivingReefsGameTests.java:351-378`) uses a sealed stone pool and `minLight 1`. It asserts TOO_DARK and 0 growths, then adds glowstone and asserts THRIVING and growth. Mutant G12 (own position only) is killed by that test. |
| **F6** MEDIUM "never overgrows" overclaims | VERIFIED | `reef_waters.json` now holds only `minecraft:warm_ocean`. The owner decision is recorded in `docs/spec.md:99-104` and `docs/sdlc-log.md`. The wording now says reefs creep slowly across warm ocean floor: `README.md:69-74`, `site/index.html:356-357` (FAQ) and `curseforge-description.md:20`. The phrases "never overgrows" and "No." are gone. Conduit caveat: see N2. |
| F10 LOW revive and re-bleach loop | VERIFIED, with a regression note (N1) | `ReefTicker.java:113-115` skips a revival target with heat within 2 blocks, using `ReefConditions.heatNear` (`ReefConditions.java:56-69`, loaded-only). `noRecoveryNextToHeat` (`:381-395`) asserts the source is THRIVING first, so the test discriminates. Over 400 attempts with a 1/27 chance each, the mutant almost surely revives the fan. G13 is killed. |
| F11 LOW `minWaterAbove` 0 | VERIFIED | `Settings.java:117` clamps to 1..8. |
| F12 LOW coral block grown into an entity | VERIFIED (code only, no test, as the log states) | `ReefTicker.java:116-119`. `CollisionGetter.isUnobstructed(state, pos, ctx)` returns true for an empty collision shape (plants, fans, wall fans). For a coral block it checks entities whose `blocksBuilding` is set (`EntityGetter.java:33-45`; every `LivingEntity`, boats, minecarts). Revival is exempt, which is right because the shape replaces the same shape. |
| F13 LOW dead-coral wording, dry outlook | VERIFIED | `LivingReefsCommands.java:124` now reads "...is right next to it, away from heat", which matches `ReefTicker.java:113`. `:183-184` prints "Out of water: it dies like any vanilla coral". |
| F14 LOW config left behind by a failed test | VERIFIED | `checkCommandExplainsHealth` sets `Settings.DEFAULTS` first (`:438`). `randomTicksGrowAReef` (`:176`) and `bleachingRuleSwitchesBleachingOff` (`:335`) set their own config. Every other test passes its settings to `act` directly. |
| F15 LOW incidental kills counted | VERIFIED | `tools/mutation.py:131-135` labels the result "KILLED" only when the named test is among the failures, and `:149-150` fails the run on anything else. All 30 original strings occur exactly once in the current sources (checked with a script). The session's mutation log shows M11 "INVALID" in the full run and "KILLED" in a separate run, as the sdlc-log says. |
| F16 LOW night shot looked like day | VERIFIED | `LivingReefsCaptures.java:161-167` clears night vision and conduit power, sets gamma to 0.5 and the time to 18000. `gallery-night.png` is dark blue with visible pickles. The captions say "after dark", and `curseforge-listing.md:54` describes pickles as light for deep or roofed reefs. |
| F17 LOW dead `Facing.opposite` | VERIFIED | Removed from `core/Facing.java`. |
| F18 LOW light Javadoc | VERIFIED | `core/Conditions.java:8`. Spec drift remains, see N6. |
| F19 LOW CurseForge categories | VERIFIED | `curseforge-listing.md:16-17` and `:24-29`: main category Server Utility; World Gen only as an additional category, with an honest reason; Biomes dropped. |
| F20 LOW RCON client | VERIFIED | `tools/rcon.py`: `exactly()` loops until all bytes arrive and raises on EOF, and `main` exits when the login response id is -1. |
| S2 LOW checkout persists credentials | VERIFIED | `build.yml:14` and `release.yml:16` set `persist-credentials: false`. |
| S3 LOW Loom snapshot | VERIFIED | `gradle.properties:8` pins `loom_version=1.18.3`. |
| S4 LOW wrapper checksum | VERIFIED | `gradle-wrapper.properties:4` holds `acd53f1e...f804d20a`, identical to `https://services.gradle.org/distributions/gradle-9.7.1-bin.zip.sha256`. |
| S11 LOW absolute paths | VERIFIED | `docs/research/claims.md:8` and `evidence/A-competitors-demand.md:83` now use neutral wording. `git ls-files \| xargs grep '/Users/'` finds only the S11 description in `security-g5.md`. The committed `.pyc` (N4) holds only the relative `tools/rcon.py`. |

## New findings

| # | Severity | File:line | Finding | Scenario | Fix |
|---|---|---|---|---|---|
| N1 | LOW | `ReefTicker.java:113-115`; `ReefRules.java:48-54` | The F10 heat check ignores conduit shelter. With an active conduit within 16 blocks, a coral near heat is DORMANT, not STRESSED, so a revived coral would not bleach again. Yet `heatNear` blocks the revival anyway, and dead coral within 2 blocks of the heat stays dead forever. The F10 rationale ("it would only bleach again") does not hold there. | A player builds a conduit by a vent reef to protect and restore it. The coral that died within two blocks of the magma never comes back, while the docs sell the conduit as shelter. | Skip the revival only when the target would be stressed, e.g. `heatNear(level, target) && !ReefConditions.conduitNear(level, target)`, or assess the target with `ReefConditions.read` and `ReefRules.assess`. Add a conduit variant of `noRecoveryNextToHeat`. |
| N2 | LOW | `branding/curseforge-description.md:21`; `site/index.html:158`, `:223`; `README.md:60` | Several lines say coral outside warm oceans "just stays as it is" or that new coral "only ever fills still water in warm oceans", and "coral in your aquarium is safe". But an active conduit within 16 blocks makes any water a reef (`ReefRules.java:60-62`). The same pages state that rule elsewhere, for example in `curseforge-description.md:18`. | Ocean bases with a conduit and a sand-floored aquarium are a common build, and the most likely place where coral sits near a conduit. That coral spreads, despite "safe for your builds". | Add "unless an active conduit is within 16 blocks" to those four lines. |
| N3 | LOW | `site/media/recovered.png`, `gallery-night.png`, `gallery-restored.png`; captions at `site/index.html:111` and `curseforge-listing.md:50` | In this run the vent killed every brain coral in the patch. Recovery needs a living neighbour of the same kind (`SpreadPlanner.java:86-88`), so the centre of the "recovered" shot is still grey. Only the fans of other species and a tube block came back. The "restored reef" gallery and night shots show the same grey patch. "Once it closes, living coral takes the dead coral back" overstates what the picture shows. The mechanics are correct. | A reader compares the before, after and recovered panels and sees the main bleached patch unchanged. | In the capture, plant one living brain coral fragment next to the patch after the vent closes. That also shows the documented "plant living fragments next to a dead reef" mechanic. Or caption it "coral of each kind takes back its own dead". |
| N4 | LOW | `tools/__pycache__/rcon.cpython-314.pyc` (added in `76672d6`); `.gitignore` | A Python bytecode file is committed. It holds no secret and no absolute path. | Repository noise. It changes on every local Python run. | `git rm --cached tools/__pycache__/rcon.cpython-314.pyc` and add `__pycache__/` to `.gitignore`. |
| N5 | LOW | `LivingReefsCommands.java:185-191` | "Waits for room to spread" is checked before the growth game rule and before `random_tick_speed 0`. A thriving coral with no room reports that it is waiting for room even when growth is off. | An owner who turned growth off reads "waits for room" and thinks growth is still on. | Check `CORAL_GROWTH` and `seconds < 0` before the `open == 0` branch. |
| N6 | LOW | `docs/spec.md:59`, `:93-95`; `LivingReefsGameTests.java:338` | The spec still describes light as `max(sky, block)` at the coral. The code takes the maximum over the coral and its six neighbours (`ReefConditions.java:82-88`), which is the point of F5 and F18. The spec's Commands paragraph also does not mention the 16-block limit (AC13 does). The comment in the bleaching test says "100 ticks is about 40 chances": at speed 1024 a block gets 0.25 random ticks per game tick, so it is about 25. The test still discriminates, because the chance of no random tick at all is 0.75^100. | A reader of the spec, or of the test comment, is misled. | Update the three texts. |

## Notes

- **S1 bypasses checked.**
  - A level-0 player cannot use `/execute`, which needs level 2.
  - Relative and local coordinates (`~`, `^`) resolve against the player's own position and are then range-checked.
  - The looked-at form is limited by `blockInteractionRange`.
  - Command blocks and level-2 operators are non-admins at the default admin level 3, so they are range-checked against their own position. A level-2 operator can move the source with `/execute positioned`, but they can already run `/execute if loaded` and `/tp`, so this opens no new oracle.
  - The console (level 4) is an admin.
  - Within 16 blocks the chunk is always loaded, and the out-of-world error reveals nothing.
  - A nearby report still tells whether an active conduit is within 16 blocks of the coral, so up to about 32 blocks from the player. I accept that as on-site information.
- **Command cost.** In 26.3 the command spam throttle adds 20 per command, decays by 1 per tick and kicks non-operators at 200 (`ServerGamePacketListenerImpl.java:288`, `:1714-1718`; `TickThrottler`). That is a burst of about 10 commands, then about one per second. (The G5 report's "about 20 probes a second" was too high.) At the maximum `spreadRadius` of 4, `openSpots` costs at most 243 spots × (1 + 8 + 124) cell reads, about 32k. Every read is loaded-only through `stateIfLoaded` and `getChunkNow`, and only a thriving coral pays it. That is negligible at that rate.
- **`heatNear`** reads 125 cells, loaded chunks only. It runs only on a RECOVER attempt that already found a wet dead coral of the same species and passed `canSurvive`, so it is rare and costs little.
- **`SpreadPlanner.room`** is the exact De Morgan form of the old condition, with the same short-circuit order (water, then clearance, then density). So `grow` behaves exactly as before. `openSpots` covers the same spots as `grow`: dx and dz within ±r, dy within ±1, the origin excluded, the same `room` test and the same floor versus wall-fan split. It does not run `canSurvive` or the entity check, which only matters for coral blocks and for transient entities.
- **GameTest environment split.** `GameTestBatchFactory` groups batches by environment and dimension (lines 27-28). `GameTestRunner.runBatch` runs them one after another, activating each environment and tearing it down (lines 86-119 and 153-155). The run log shows four batches: `fast` (1 test), `fast_rules` (1), `minecraft:default` (1) and `quiet` (13). So the two fast tests never share game rules. `LivingReefsConfig` is still global. Each test that reads it sets it first, and a failed test leaves its config only until the next one sets it.
- **Visual check.**
  - `check.png`: the chat is readable and nothing covers it (the fish near the camera was removed).
  - `check-stressed.png`: clear, with the magma under the reef visible.
  - `gallery-night.png`: now reads as dusk or night, not day.
  - Hand and hotbar are hidden in all shots.
  - The only issue is N3.
- **Docs wording now matches the code** on warm ocean only (`reef_waters.json`, `NEUTRAL_WATER` line at `LivingReefsCommands.java:165`), the 16-block range (README, site, CurseForge, CHANGELOG) and open spots. The exceptions are the conduit caveat (N2) and the spec drift (N6).
