# Research brief: Living Reefs

Agentic SDLC central version 2026-09-16+2. Team: Deep research (R1 to R6).
Owner: César (GitHub `razekteixeira`). Research lead (R1): the orchestrating Claude Code session.

## G0 Intake ready

- **Objective:** decide whether and how to build a Fabric mod for Minecraft Java 26.3 that makes
  coral reefs live (natural spreading, bleaching, recovery), and give the build the facts it needs.
- **Outcome case:** the beneficiary is the survival or server player who builds or restores ocean
  bases; the observed job is "make a reef grow and stay alive without bone meal spam"; the current
  alternative is bone meal plus mods that only stop coral dying; the outcome measure is a reef that
  visibly spreads in a time-lapse on a vanilla client. Stop rule: if a mod already does natural
  reef growth well on current versions, report to the owner with options before building.
- **Acceptance criteria (questions the report must answer):**
  - Q1 Demand and gaps: who asks, what Coral Up, Reefier Reefs, Immortal Coral, Waxable Coral and
    newer mods do, and whether any mod already spreads coral naturally. Name free on CurseForge and
    Modrinth.
  - Q2 Vanilla coral in 26.3: blocks, fans, wall fans, coral blocks, dead variants, ticking, bone
    meal, worldgen features, sea pickles, kelp and seagrass.
  - Q3 Design: growth conditions, spread rules and caps, bleaching and recovery, interplay with
    fish, conduits and sea pickles, data driven surface.
  - Q4 Server-side first: what works with vanilla clients and what is lost.
  - Q5 Performance: random ticks versus a scheduled scanner, chunk loading, cost caps, measurement.
  - Q6 Compatibility: other mods' coral via tags, ocean worldgen mods, servers.
- **Sensitive tags:** none of `regulated-claim`, `personal-data`, `paid-or-licensed-sources`.
  `decision-support` was considered and not applied: the build decision is reversible and carries
  no external commitment (nothing is published before the owner acts).
- **Effort level:** M (six questions, four parallel slices, up to two waves). Not fast-lane.
- **Budgets:** four R2 Search slices (A 30, B 40, C 35, D 40 tool calls), up to two waves,
  saturation rule N=2, deadline: this session.
- **Plan checkpoint:** the owner set the questions in the session brief and launched an unattended
  goal run, so the plan was not shown for interactive approval. Recorded as a deviation in
  `docs/sdlc-log.md`.

## G1 Research brief ready and G2 Plan ready (collapsed)

| Slice | Questions | Sources | Closure criterion |
|---|---|---|---|
| A Competitors, demand, name | Q1 | Modrinth API, CurseForge API (curse.tools proxy), mod pages, Reddit, feedback site | every named mod described from its own page; natural spreading searched with at least three query terms on both platforms; name slug checked on both |
| B Vanilla coral in 26.3 | Q2, part of Q4 | 26.3 game jar (bytecode), Minecraft Wiki | each mechanic stated from code with class and method, cross-checked with the wiki or marked code-only |
| C Design and compatibility | Q3, Q6, part of Q4 | NOAA, AIMS and reviews for ecology; Minecraft Wiki; mod pages; Fabric convention tags | each design input with one tier 1 or 2 source; each worldgen mod from its own page |
| D Performance and technique | Q5, part of Q4 | 26.3 game jar, Fabric API 0.162.0+26.3 jars, Throughput tooling | random tick and scanner costs stated from code; game rule sync answered from code |

Non-goals: reef science beyond what grounds the game design; Forge or NeoForge ports; other
Minecraft versions.

Artefacts: evidence packs in `docs/research/evidence/`, claim ledger `docs/research/claims.md`,
sceptic report `docs/research/sceptic.md`, report `docs/research.md`.
