package dev.rinchan.loamyend.registry;

import dev.rinchan.loamyend.LoamyEnd;
import dev.rinchan.loamyend.feature.SoilCoatFeature;
import dev.rinchan.loamyend.feature.WaterIslandFeature;
import dev.rinchan.loamyend.feature.WaterPoolFeature;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LoamyEndRegistries {
    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(BuiltInRegistries.FEATURE, LoamyEnd.MOD_ID);

    public static final Supplier<Feature<NoneFeatureConfiguration>> SOIL_COAT = FEATURES.register(
        "soil_coat",
        () -> new SoilCoatFeature(NoneFeatureConfiguration.CODEC)
    );
    public static final Supplier<Feature<NoneFeatureConfiguration>> WATER_POOL = FEATURES.register(
        "water_pool",
        () -> new WaterPoolFeature(NoneFeatureConfiguration.CODEC)
    );
    public static final Supplier<Feature<NoneFeatureConfiguration>> WATER_ISLAND = FEATURES.register(
        "water_island",
        () -> new WaterIslandFeature(NoneFeatureConfiguration.CODEC)
    );

    private LoamyEndRegistries() {
    }

    public static void register(IEventBus modBus) {
        FEATURES.register(modBus);
    }
}
