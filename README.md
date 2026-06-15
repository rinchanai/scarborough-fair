# Scarborough Fair

Scarborough Fair adds an End-shaped floating-island dimension with grass-covered surfaces, overworld-style stone and ore strata, synced day-night sky, larger sun and moon, oak and birch trees, small water pools, and occasional water-covered islands.

## Features

- Terrain uses the vanilla End island density shape.
- Island top surfaces are grass blocks, while sides and undersides use dirt and the interiors use overworld base stone so normal ore and stone replacement features can run.
- The biome is tagged as overworld / plains / forest for data-driven biome modifiers.
- The dimension uses the overworld day-night cycle, with a sun and moon rendered twice as large to make the islands feel closer to the sky.
- Chorus plant decoration is replaced by oak and birch trees.
- Small water pools can appear on islands.
- Rare larger water islands can cover much of an island and leave gaps where water may spill downward.
- Optional config can start new players on a deterministic outer-island surface in Scarborough Fair and set their initial personal respawn point there; this is disabled by default.
- Optional config controls whether Scarborough Fair uses overworld-targeting structures, passive mob spawns, and hostile mob spawns.

## Dimension ID

- `scarborough_fair:scarborough_fair`

## Common config

Generated at `config/scarborough_fair-common.toml`:

- `spawn.defaultSpawnInDimension = false`: keep vanilla overworld first spawn by default. When enabled, new players are sent to a Scarborough Fair outer island with `changeDimension`, and their initial personal respawn point is set there, matching Aether-style start-in-dimension behavior.
- `worldgen.useOverworldStructureList = true`: allow overworld-targeting structures to also target Scarborough Fair island biomes.
- `worldgen.generatePassiveMobs = true`: add passive/ambient island biome spawns.
- `worldgen.generateHostileMobs = false`: keep natural hostile mob spawns disabled by default.

## Build

```bash
./gradlew :neoforge:build
```
