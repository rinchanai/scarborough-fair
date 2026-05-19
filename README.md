# Loamy End

Loamy End adds a new End-shaped dimension with soil-covered floating islands, overworld-style stone and ore strata, oak and birch trees, and water features.

## Behavior

- Terrain uses the vanilla End island density shape.
- Exposed island surfaces are dirt, while island interiors use overworld base stone so normal ore and stone replacement features can run.
- The Loamy End biome is tagged as an overworld/plains/forest biome for data-driven biome modifiers.
- Chorus plant decoration is not used; oak and birch trees generate instead.
- Small water pools can appear on islands.
- Rare larger water islands can cover much of a small island and leave gaps where water may spill downward.
- Players entering the dimension are placed on an outer-island surface instead of the central island.

## Dimension id

- `loamy_end:loamy_end`

## Build

```bash
./gradlew :neoforge:build
```
