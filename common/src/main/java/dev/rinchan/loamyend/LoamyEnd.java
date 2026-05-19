package dev.rinchan.loamyend;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public final class LoamyEnd {
    public static final String MOD_ID = "loamy_end";
    public static final ResourceKey<Level> LEVEL = ResourceKey.create(Registries.DIMENSION, id("loamy_end"));

    private LoamyEnd() {
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
