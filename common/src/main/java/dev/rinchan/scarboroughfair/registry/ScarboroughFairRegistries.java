package dev.rinchan.scarboroughfair.registry;

import dev.rinchan.scarboroughfair.ScarboroughFair;
import dev.rinchan.scarboroughfair.block.ReturnPortalBlock;
import dev.rinchan.scarboroughfair.feature.CenterVoidHoleFeature;
import dev.rinchan.scarboroughfair.feature.SoilCoatFeature;
import dev.rinchan.scarboroughfair.feature.WaterIslandFeature;
import dev.rinchan.scarboroughfair.feature.WaterPoolFeature;
import dev.rinchan.scarboroughfair.worldgen.AddConfiguredSpawnsModifier;
import dev.rinchan.scarboroughfair.worldgen.AddIslandBiomesToMatchingStructuresModifier;
import dev.rinchan.scarboroughfair.worldgen.ScarboroughFairBiomeSource;
import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.StructureModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ScarboroughFairRegistries {
    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(BuiltInRegistries.FEATURE, ScarboroughFair.MOD_ID);
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, ScarboroughFair.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends BiomeSource>> BIOME_SOURCES = DeferredRegister.create(BuiltInRegistries.BIOME_SOURCE, ScarboroughFair.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends StructureModifier>> STRUCTURE_MODIFIER_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.Keys.STRUCTURE_MODIFIER_SERIALIZERS, ScarboroughFair.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, ScarboroughFair.MOD_ID);

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
    public static final Supplier<MapCodec<? extends BiomeSource>> BIOME_SOURCE = BIOME_SOURCES.register(
        "scarborough_fair",
        () -> ScarboroughFairBiomeSource.CODEC
    );
    public static final Supplier<MapCodec<? extends StructureModifier>> ADD_ISLAND_BIOMES_TO_MATCHING_STRUCTURES = STRUCTURE_MODIFIER_SERIALIZERS.register(
        "add_island_biomes_to_matching_structures",
        () -> AddIslandBiomesToMatchingStructuresModifier.CODEC
    );
    public static final Supplier<MapCodec<? extends BiomeModifier>> ADD_CONFIGURED_SPAWNS = BIOME_MODIFIER_SERIALIZERS.register(
        "add_configured_spawns",
        () -> AddConfiguredSpawnsModifier.CODEC
    );

    private ScarboroughFairRegistries() {
    }

    public static void register(IEventBus modBus) {
        FEATURES.register(modBus);
        BLOCKS.register(modBus);
        BIOME_SOURCES.register(modBus);
        STRUCTURE_MODIFIER_SERIALIZERS.register(modBus);
        BIOME_MODIFIER_SERIALIZERS.register(modBus);
    }
}
