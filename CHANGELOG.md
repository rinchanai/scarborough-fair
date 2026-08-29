# Changelog

## 0.2.1

### Fixed

- Removed internal smoke-test and screenshot automation from production code and development run configuration.
- If no validated island surface can be found for a new player, leave the player in the Overworld instead of teleporting to an unchecked fixed coordinate.

## 0.2.0

- Replaced custom biome and feature ownership with vanilla End terrain shape and vanilla Overworld biome/content systems.
- Added the optional, disabled-by-default first-spawn route for genuinely new players.
