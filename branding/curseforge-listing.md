# CurseForge launch sheet

Everything CurseForge asks for, in order. The project page, logo and gallery have no public API,
so they are entered by hand once; files are uploaded by the release workflow afterwards.

## 1. Create the project

[authors.curseforge.com](https://authors.curseforge.com) → Create project → Minecraft → Mods.

| Field | Value |
|---|---|
| Name | `Living Reefs` |
| Slug | `living-reefs` (URL: curseforge.com/minecraft/mc-mods/living-reefs; free on 2026-10-08) |
| Summary | contents of [`summary.txt`](summary.txt) (under 120 characters) |
| Description | switch the editor to **Markdown**, paste [`curseforge-description.md`](curseforge-description.md) |
| Main category | World Gen |
| Additional categories | Server Utility, Utility & QoL, Biomes |
| License | Apache License 2.0 |
| Source URL | https://github.com/razekteixeira/living-reefs |
| Issues URL | https://github.com/razekteixeira/living-reefs/issues |
| Wiki / website | https://razekteixeira.github.io/living-reefs/ |
| Logo | [`icon-512.png`](icon-512.png) (square PNG, at least 400×400; not WebP) |

Why these categories: Living Reefs changes how a natural world feature behaves over time, which is
where players browse for world and nature mods (World Gen). It is server-side only and has
per-world switches, so server owners find it under Server Utility; `/reef check` is a
quality-of-life tool (Utility & QoL). It works on ocean biomes and their coral (Biomes). It adds no
items, mobs or blocks, so not Mobs, Armor or Technology.

## 2. Settings

- **Project distribution / third-party apps: enable it.** Without it, launchers (Prism,
  ATLauncher) and server hosts cannot download the file, which matters for a server-side mod.
- Environment on each file: client and server (it works in singleplayer when installed on the
  client, and on dedicated servers alone).

## 3. Images tab (gallery)

Upload in this order; mark the first one as featured. All come from the same capture run as the
description text (`./gradlew runClientGameTest`, then `branding/make_media.sh` and
`python3 -P branding/make_banners.py`).

| File | Title | Description |
|---|---|---|
| `site/media/banner.png` | Living Reefs | Coral reefs that grow, bleach and recover on their own. |
| `site/media/before-after.png` | From fragments to a reef | Five coral fragments planted on bare sand, and the reef they grew into. |
| `site/media/timelapse.gif` | Time-lapse | A restoration site filling in, sped up with random_tick_speed. |
| `site/media/check.png` | /reef check | Why a coral thrives: warm water, light, fish and sea pickles nearby. |
| `site/media/bleach-recover.png` | Bleaching and recovery | A magma vent bleaches the reef; once it closes, living coral takes the dead coral back. |
| `site/media/check-stressed.png` | Stressed coral | /reef check next to the vent: magma within two blocks. |
| `site/media/gallery-reef.png` | A natural reef | A warm ocean reef as it generated, now alive. |
| `site/media/gallery-restored.png` | A restored reef | The grown restoration site up close. |
| `site/media/gallery-night.png` | Night reef | Sea pickles light the reef after dark, and light lets deep coral grow. |

## 4. Releases

1. Fill in `curseforge_project_id` in `gradle.properties` with the numeric project ID from the
   project page, and add the repository secret: create a token at
   [authors.curseforge.com → API tokens](https://authors.curseforge.com/#/settings/api-tokens), then
   `env -u GITHUB_TOKEN gh secret set CURSEFORGE_TOKEN -R razekteixeira/living-reefs`.
2. Optional: add the downloads badge to the README once the ID exists
   (`https://img.shields.io/curseforge/dt/<id>?logo=curseforge&label=CurseForge`).
3. Join a dev server with an unmodified 26.3 client (see CONTRIBUTING, "Releases").
4. Tag the release: `git tag v1.0.0-beta.1 && git push origin v1.0.0-beta.1`. The workflow builds,
   runs every test, creates the GitHub release and uploads the jar to CurseForge as a beta
   (game version 26.3, Java 25, Fabric, client + server, requires Fabric API). Without the token or
   the project ID, the upload step is skipped and `./gradlew publishCurseforge` stays a dry run.
5. The file shows as "Under review" until a CurseForge moderator approves it.
