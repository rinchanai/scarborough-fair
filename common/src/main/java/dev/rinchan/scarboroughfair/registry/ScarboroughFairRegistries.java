package dev.rinchan.scarboroughfair.registry;

import dev.rinchan.scarboroughfair.ScarboroughFair;
import dev.rinchan.scarboroughfair.block.ReturnPortalBlock;
import dev.rinchan.scarboroughfair.feature.CenterVoidHoleFeature;
import dev.rinchan.scarboroughfair.feature.SoilCoatFeature;
import dev.rinchan.scarboroughfair.feature.WaterIslandFeature;
import dev.rinchan.scarboroughfair.feature.WaterPoolFeature;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ScarboroughFairRegistries {
    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(BuiltInRegistries.FEATURE, ScarboroughFair.MOD_ID);
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, ScarboroughFair.MOD_ID);

    public static final Supplier<Block> RETURN_PORTAL = BLOCKS.register(
        "return_portal",
        () -> new ReturnPortalBlock(BlockBehaviour.Properties.of().noCollission().noOcclusion().strength(-1.0F, 3600000.0F))
    );

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
    public static final Supplier<Feature<NoneFeatureConfiguration>> CENTER_VOID_HOLE = FEATURES.register(
        "center_void_hole",
        () -> new CenterVoidHoleFeature(NoneFeatureConfiguration.CODEC)
    );

    private ScarboroughFairRegistries() {
    }

    public static void register(IEventBus modBus) {
        FEATURES.register(modBus);
        BLOCKS.register(modBus);
    }
}
