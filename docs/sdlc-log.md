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
