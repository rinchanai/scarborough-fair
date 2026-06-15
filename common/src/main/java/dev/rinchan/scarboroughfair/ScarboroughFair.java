package dev.rinchan.scarboroughfair;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public final class ScarboroughFair {
    public static final String MOD_ID = "scarborough_fair";
    public static final ResourceKey<Level> LEVEL = ResourceKey.create(Registries.DIMENSION, id("scarborough_fair"));
    public static final ResourceKey<Biome> MAIN_ISLAND_BIOME = ResourceKey.create(Registries.BIOME, id("main_island"));
    public static final ResourceKey<Biome> OUTER_ISLANDS_BIOME = ResourceKey.create(Registries.BIOME, id("outer_islands"));

    private ScarboroughFair() {
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
