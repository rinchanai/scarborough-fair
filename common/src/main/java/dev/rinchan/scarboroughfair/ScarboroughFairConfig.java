package dev.rinchan.scarboroughfair;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ScarboroughFairConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue DEFAULT_SPAWN_IN_DIMENSION;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("spawn");
        DEFAULT_SPAWN_IN_DIMENSION = builder
            .comment("When enabled, new players start on a Scarborough Fair island and their initial personal respawn point is set there. Disabled by default so worlds keep the normal overworld first spawn.")
            .define("defaultSpawnInDimension", false);
        builder.pop();
        SPEC = builder.build();
    }

    private ScarboroughFairConfig() {
    }
}
