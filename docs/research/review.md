# Research review: Living Reefs

Role: R6 Research Reviewer (Deep research team), gate G6 Independent review. Fresh context; did not
write, search or verify the report. Date: 2026-10-08. Inputs read: `docs/research/brief.md`,
`docs/research.md`, `docs/research/claims.md`, `docs/research/sceptic.md`, the `## Sources` sections
of evidence packs A to D, and `docs/spec.md` (G0 section only, for the stop rule). No web access.

**Verdict: approve with changes.** The substance holds: every question is answered, all 14 R3 fixes
and all 11 R4 findings are applied or listed as limitations, and the five spot-checked citations
resolve. The changes are about the record and about calibration. The report claims gates and an owner
decision that no file in the worktree records, four citation keys do not resolve, and the new
competitor claims added after the sceptic pass have no ledger rows. Required changes 1 to 4 block
marking G6 passed. Changes 5 to 9 are small text fixes.

## 1. Question coverage

| Q | Status | Evidence (report section, quoted) |
|---|---|---|
| Q1 Demand, gaps, name | Answered | "Q1 Demand and gaps": "Who asks" (two author quotes, Feedback and Reddit marked unverified), "What exists" table of 13 rows including Grassidium, Carpet Sky Additions Reborn, Regrowth, and "Name": "return nothing on Modrinth (404) or CurseForge (0 results), with working control queries". The stop rule was raised: "The owner decided to differentiate (2026-10-08, after the stop rule was raised)". **The owner decision cannot be verified**: `docs/sdlc-log.md` does not exist, and `docs/spec.md` G0 says the stop rule was "checked, not triggered" (see required change 2). |
| Q2 Vanilla coral in 26.3 | Answered | "Q2 Vanilla coral in 26.3": inventory (20 living and 20 dead ids, four classes, `deadBlock`), tags, "No growth, no random tick" (kelp 14 %, seagrass never spreads), death (`scanForWater`, 60 to 99 ticks), support, bone meal (128 attempts, one in four, wall fan on first side attempt), sea pickles, worldgen (25 % coral, 3.75 % pickle), biomes (temperature 0.5), light, conduit. Small gap: bone meal on seagrass (tall seagrass) is not mentioned. |
| Q3 Design | Answered | "Q3 Design": ecology grounding, vanilla spread precedents ("vines: fewer than 4 other vines in a 9 x 9 x 3 box"), and a 12-row "Recommended rules" table covering biome, light, substrate, spread, caps, bleaching, recovery, fish, sea pickles, conduit, rate and the data-driven surface; species pairing from the id pattern. |
| Q4 Server-side first | Answered | "Q4 Server-side first": "What works with vanilla clients", "Game rules are safe" (code-level reasoning plus Immortal Coral precedent), "What is lost without client code", "Recommendation: server-side only". |
| Q5 Performance | Answered, one thin spot | "Q5 Performance": random tick scope, tick flag fixed at bootstrap, Lithium, "Options compared" (a) to (c), "Cost of (a)", "Measurement plan", "Recommendation: option (a)". Cost caps are thin: the report gives the per-section cost but does not say how per-call work stays bounded at a high `random_tick_speed` (recommended change 10). |
| Q6 Compatibility | Answered | "Q6 Compatibility": other mods' coral (no convention tag, id pattern), "Mods that also tick coral" (Nature's Spirit, improveMint), worldgen mods (BoP, Terralith, Tectonic), servers. |

## 2. Disposition of R3 and R4 findings

### R3 claim ledger, "Findings requiring changes"

| # | Required change | Status in `docs/research.md` |
|---|---|---|
| 1 | Dead coral in utility tags | Applied (Q2 "Tags") |
| 2 | 25 % coral and 3.75 % pickle | Applied (Q2 "Worldgen") |
| 3 | "fewer than 4 other vines" | Applied (Q3) |
| 4 | First side attempt places a wall fan | Applied (Q2 "Bone meal") |
| 5 | "various, up to 1.21.11" | Applied. The craftable datapacks now have their own row "up to 26.3" from A-S3, which R3 did not re-check (low stakes, nothing grows) |
| 6 | improveMint wording | Applied ("its description mentions no spreading to new blocks and no bleaching") |
| 7 | Tidal Terror also adds mobs | Applied |
| 8 | "found nothing" | Applied by removal; the sentence is replaced by the Sk-F4 paragraph |
| 9 | Drop C-S5 | Applied (prose and Bleaching row cite only C-S4) |
| 10 | Fish claim hedged | Applied in Q3 prose; **not in the design table** ("grazers protect recruitment [C-S8]", see change 7) |
| 11 | [C-S25][C-S26] for "since 1.21.11" | Applied (Q4) |
| 12 | Lithium citations | Applied (Summary [D-S17]; Q5 [D-S17][D-S18]) |
| 13 | Chunk-loading citations | Applied (Q5 "Cost of (a)") |
| 14 | Servers citations | Applied ([D-S1][D-S10]) |
| 15 | Claims 29 and 41 open | Recorded as a limitation ("growth rates and the BoP, Terralith and Tectonic claims were not re-fetched at G4"). The ledger said "re-check before spec freeze"; the spec exists and the re-check was not done. Acceptable as a limitation, but Q6 states them flatly (change 8) |

### R4 sceptic findings

| # | Status |
|---|---|
| F1 CRITICAL Grassidium | Content applied (Summary, Q1 table, differentiation paragraph). The required "report to the owner and log the decision" step is asserted but not recorded: `docs/sdlc-log.md` is missing (change 2) |
| F2 Carpet Sky Additions Reborn | Applied to the table. "Check its default value" was neither done nor listed as a limitation (change 9) |
| F3 Regrowth | Applied (table, differentiation, Q6 density cap note) |
| F4 Re-run with generic queries | Applied as [R2] in the annex and the Q1 paragraph. Not recorded in evidence pack A or the claim ledger, which F4 asked for: pack A has no mention of Grassidium, Regrowth or Carpet, and the ledger still holds the 42 pre-sceptic claims (change 3) |
| F5 Mixin collision | Applied in Q6 only, not Q5. The Q6 sentence frames it as "a hook on the shared base class", while Q5 recommends a mixin on the coral classes themselves, which collides directly on `CoralBlock` (change 6) |
| F6 Light calibration | Applied (design table, limitations) |
| F7 Lithium develop only | Limitation recorded |
| F8 16-block scope | Applied (Q5) |
| F9 "coral spread" wording | Applied by removal |
| F10 Cite FabricRegistryInit | Applied via [C-S27]; live join test listed as a limitation |
| F11 Planet Minecraft | Limitation recorded |

Neither applied nor recorded: F2's default-value check (change 9) and F4's ledger and pack update
(change 3). The F1 owner-decision record is claimed but missing (change 2).

## 3. Rubric

| Criterion | Score | Justification |
|---|---|---|
| Comprehensiveness | 4 | All six questions answered from code and platform data; a broad competitor table; small gaps (cost bound at high tick speed, seagrass bone meal, Carpet default). |
| Insight | 4 | Non-obvious findings with build consequences: temperature cannot separate warm water, the tick flag is fixed before tags load, Lithium bypasses a loop hook, species pairing must come from the id pattern. |
| Instruction-following | 3 | The stop rule was raised and citations are dense, but the gate log the report and brief point to does not exist, G6 is listed as passed before this review, four citation keys do not resolve, and the ledger does not cover the new claims. |
| Readability | 4 | Short, plain, well tabled. The first summary bullet ("No Fabric mod ... exists") is contradicted two sentences later by a Fabric 26.3 mod. |
| Calibration | 3 | Limitations are honest and specific, and demand is called thin. But the summary uses absolute wording ("exists", "Nothing on any loader") where the evidence is "not found on the searched platforms". The fish row and Q6 worldgen claims read as settled while the ledger marks them U. The header claims G5 clean, though the sceptic's verdict was "G5 not clean" and no re-check of the fixes is recorded. |

## 4. Citation spot-check (5 keys)

| Report sentence | Key | Resolves to | Supports? |
|---|---|---|---|
| Immortal Coral: "Game rule that stops coral dying; server-side" (Q1 table) | [A-S12] | Pack A S12, Modrinth project API `immortal-coral`, updated 2026-09-16 | Yes (R3 claim 10 confirms the quote) |
| "vines: fewer than 4 other vines in a 9 x 9 x 3 box ... `spread_vines`" (Q3) | [C-S13] | Pack C S13, Minecraft Wiki "Vines" | Yes (R3 claim 33, after fix 3) |
| Lithium replaces the loop in sections with 384 or fewer ticking states (Q5) | [D-S17] | Pack D S17, raw GitHub Lithium develop `ServerLevelMixin.java`, fetched 2026-10-08 | Yes, develop branch only (limitation stated) |
| "Since 1.21.11 game rules are a built-in registry" (Q4) | [C-S25][C-S26] | Pack C S25, Minecraft Wiki "Game rule" (history: 25w44a registry); S26, Fabric blog "Fabric for Minecraft 1.21.11" | Yes, plausibly (25w44a is a 1.21.11 snapshot) |
| Measurement plan: RCON benchmark, warm-up, readings (Q5) | [D-S20][D-S21] | Pack D S20, Throughput `tools/benchmark.sh` and `tools/rcon.py`; S21, `MinecraftServer` tick-time members | Yes |

Keys that do **not** resolve: `[Sk-a]` (Q4), `[Sk-b]` (Q5 Lithium), `[Sk-c]` (Q5 cost), `[Sk-d]` (Q2 light).
`sceptic.md` labels its claim table C1 to C5 and mentions "(a) to (d)" only in prose; the report's key
legend defines only `[Sk-F1]`-style keys. From the content they map to C2, C3, C4 and C5. Also
`[D-S6]` in Q4 (coral class member lists) does not support "uses vanilla particles for feedback, and
adds commands built from vanilla argument types": that is a design intent and needs no citation.

## 5. Required changes

Blocking for G6:

1. **Header, lines 3 to 5 of `docs/research.md`.** "Gates passed: ... G5 Sceptic clean ... G6
   Independent review (`docs/sdlc-log.md`)" overstates the record. G6 had not run, the sceptic's
   verdict was "G5 not clean" with no recorded re-check, and the cited log does not exist. Reword it
   to the true status, for example "G4 Claims with teeth (ledger, 42 claims on the pre-sceptic
   version); G5 Sceptic: not clean, findings F1 to F11 resolved by the lead (see sdlc-log); G6:
   approve with changes (`docs/research/review.md`)". Update it once the changes below are made.
2. **Create `docs/sdlc-log.md`** (the report, brief and spec all point to it) with at least: the
   G0 plan-checkpoint deviation from the brief; the F1 to F11 resolution table; and the **owner's
   stop-rule decision** with date, time and the owner's words or a link to where he gave them. If
   the owner has not actually answered, change Summary bullet 2 ("The owner decided to
   differentiate") to say the decision is pending, and hold the build. Also flag to the spec author
   that `docs/spec.md` G0 says "stop if a current mod already does this (checked, not triggered)";
   that contradicts the report (Grassidium does spread coral, and the stop rule was raised) and
   should read "raised; owner chose to differentiate on <date>".
3. **Ledger coverage of the new claims.** The load-bearing claims added after the sceptic pass
   (Grassidium: NeoForge only, 26.3, closed source, spreads coral; Carpet Sky Additions Reborn:
   Fabric 26.3, `spreadingCoral`; Regrowth: up to 1.21.11, mob-driven; Nature's Spirit and improveMint
   mixins; the [R2] generic-query re-run) have no rows in `docs/research/claims.md`. Add an addendum
   with rows 43 onward, giving sources (Sk-F1 to F5, R2), verdict and origin independence. Note that
   these were checked by the sceptic and the lead, not by R3. Alternatively, add one line under
   Limitations saying they were not R3-verified. Fix the annex text "(42 claims; 14 fixes applied in
   this version)" to match. Add a short wave-3 note to evidence pack A pointing to the sceptic queries
   and R2, as F4 asked.
4. **Unresolvable citation keys.** Replace `[Sk-a]`, `[Sk-b]`, `[Sk-c]` and `[Sk-d]` with `[Sk-C2]`,
   `[Sk-C3]`, `[Sk-C4]` and `[Sk-C5]` (Q4 game rules, Q5 Lithium, Q5 cost, Q2 light). Extend the key
   legend in the intro: "`[Sk-C2]` is load-bearing claim C2 in the sceptic's defence table".

Text and calibration:

5. **Summary bullet 1.** "No Fabric mod and no reef-specific mod exists on current versions" is
   contradicted by the next sentence (Carpet Sky Additions Reborn is a Fabric 26.3 mod that makes
   coral), and "exists" claims more than a search can show. Suggested: "No Fabric mod spreads coral
   through water, and no reef-specific mod was found on Modrinth, CurseForge, Hangar or Spigot for
   current versions." Change "Nothing on any loader bleaches coral or revives it" to "No project
   found on those platforms bleaches coral or revives it (Planet Minecraft not searched)".
6. **Q6 "Mods that also tick coral".** Replace "A mod that overrides it on the coral class would take
   precedence over a hook on the shared base class" with wording that matches the Q5 design: "If two
   mods add `randomTick` or `isRandomlyTicking` to `CoralBlock` through mixins, only one survives,
   chosen by mixin priority; Living Reefs should inject into existing methods, not add overriding
   ones, and an override elsewhere that skips `super` still defeats it [Sk-F5]". Add a one-line
   pointer to it in the Q5 "Options compared" bullet, as F5 asked.
7. **Q3 design table, Fish row.** "grazers protect recruitment [C-S8]" becomes "grazers are reported
   to protect recruitment (abstract seen only via search summary) [C-S8]", to match the prose and R3
   fix 10.
8. **Q6 "Worldgen mods".** The BoP, Terralith and Tectonic statements are ledger verdict U (not
   re-checked). Add "(from each project's source branch, not re-verified at G4; Terralith's branch is
   1.20 and may lag)" or re-check them before the revival rule is built on them.
9. **Limitations.** Add: "The default value of Carpet Sky Additions Reborn's `spreadingCoral` rule
   was not checked (F2)." In Q4, drop `[D-S6]` from the sentence about particles and commands, or cite
   only the parts it supports.

Recommended, not blocking:

10. Q5: add one sentence on the cost bound at a high `random_tick_speed`. The handler does a fixed,
    small number of neighbour reads per call, after the chance roll, so its cost scales linearly with
    the rule; the stress value in the measurement plan checks this.
11. Q1 "Who asks": the sceptic noted that Regrowth's downloads "do not measure demand for coral".
    Add those words after the 6.59 million figure so README and site copy are not tempted to reuse
    it as demand.
12. Q2: one clause on seagrass and bone meal (tall seagrass), since the brief lists seagrass.
