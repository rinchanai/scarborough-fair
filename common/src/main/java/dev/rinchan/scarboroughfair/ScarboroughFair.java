package dev.rinchan.scarboroughfair;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public final class ScarboroughFair {
    public static final String MOD_ID = "scarborough_fair";
    public static final ResourceKey<Level> LEVEL = ResourceKey.create(Registries.DIMENSION, id("scarborough_fair"));

    private ScarboroughFair() {
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
