# Scarborough Fair

Scarborough Fair combines the vanilla End's floating-island terrain shape with the vanilla Overworld's biome and content systems.

## What it does

- Uses the vanilla `minecraft:end` noise settings for island height, density, void gaps, and legacy terrain randomness.
- Uses the vanilla `minecraft:overworld` multi-noise biome preset.
- Reuses the Overworld's base stone, surface rules, climate fields, ore veins, biome features, mob spawns, and structure biome membership.
- Keeps an air-filled void and the End's zero sea level instead of flooding the dimension to Overworld sea level.
- Uses an Overworld-style dimension type with skylight, weather, beds, raids, and the normal day-night cycle.
- Adds no custom biomes, structures, mobs, ores, trees, pools, terrain features, sky renderer, or return portal.

A plains island therefore uses the actual vanilla plains biome's surfaces, vegetation, spawns, and structures, while the island shape still comes from the vanilla End density functions.

## Dimension ID

- `scarborough_fair:scarborough_fair`

Scarborough Fair does not add a portal or item entry route. Packs can provide their own route. The optional first-spawn behavior below is disabled by default.

## Updating from 0.1.x

Version 0.2.0 replaced every old Scarborough Fair biome and world-generation registry entry. Existing 0.1.x dimension chunks are not a supported migration target. Back up the world and regenerate `dimensions/scarborough_fair/scarborough_fair` before loading 0.2.x. Version 0.2.1 does not change the 0.2.0 world-generation schema.

## Common config

Generated at `config/scarborough_fair-common.toml`:

```toml
[spawn]
defaultSpawnInDimension = false
```

When enabled, genuinely new players start on a deterministic safe island surface in Scarborough Fair and receive that position as their initial personal respawn point. Existing players are not moved.

## Build

```bash
./gradlew :neoforge:build
```
