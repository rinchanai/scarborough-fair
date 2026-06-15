package dev.rinchan.scarboroughfair;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ScarboroughFairConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue DEFAULT_SPAWN_IN_DIMENSION;
    public static final ModConfigSpec.BooleanValue USE_OVERWORLD_STRUCTURE_LIST;
    public static final ModConfigSpec.BooleanValue GENERATE_PASSIVE_MOBS;
    public static final ModConfigSpec.BooleanValue GENERATE_HOSTILE_MOBS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("spawn");
        DEFAULT_SPAWN_IN_DIMENSION = builder
            .comment("When enabled, new players start on a Scarborough Fair outer island and their initial personal respawn point is set there, matching Aether-style start-in-dimension behavior. Disabled by default so worlds keep the normal overworld first spawn.")
            .define("defaultSpawnInDimension", false);
        builder.pop();

        builder.push("worldgen");
        USE_OVERWORLD_STRUCTURE_LIST = builder
            .comment("Allow structures that can generate in overworld biomes to also target Scarborough Fair island biomes.")
            .define("useOverworldStructureList", true);
        GENERATE_PASSIVE_MOBS = builder
            .comment("Add passive/ambient Scarborough Fair biome spawns such as farm animals, bats, and glow squids.")
            .define("generatePassiveMobs", true);
        GENERATE_HOSTILE_MOBS = builder
            .comment("Add common hostile overworld-style natural mob spawns in Scarborough Fair. Disabled by default.")
            .define("generateHostileMobs", false);
        builder.pop();

        SPEC = builder.build();
    }

    private ScarboroughFairConfig() {
    }
}
