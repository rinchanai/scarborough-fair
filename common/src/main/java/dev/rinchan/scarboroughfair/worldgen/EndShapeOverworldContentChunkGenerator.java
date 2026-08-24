package dev.rinchan.scarboroughfair.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.rinchan.scarboroughfair.registry.ScarboroughFairRegistries;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.SurfaceRuleData;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;

/**
 * Uses the vanilla End settings only for floating-island shape while retaining
 * vanilla Overworld climate, blocks, surfaces, ores, biomes, mobs, and structures.
 */
public final class EndShapeOverworldContentChunkGenerator extends NoiseBasedChunkGenerator {
    public static final MapCodec<EndShapeOverworldContentChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource),
            NoiseGeneratorSettings.CODEC.fieldOf("shape_settings").forGetter(EndShapeOverworldContentChunkGenerator::shapeSettings),
            NoiseGeneratorSettings.CODEC.fieldOf("content_settings").forGetter(EndShapeOverworldContentChunkGenerator::contentSettings)
        ).apply(instance, instance.stable(EndShapeOverworldContentChunkGenerator::new))
    );

    private final Holder<NoiseGeneratorSettings> shapeSettings;
    private final Holder<NoiseGeneratorSettings> contentSettings;
    private final NoiseGeneratorSettings combinedSettings;

    public EndShapeOverworldContentChunkGenerator(
        BiomeSource biomeSource,
        Holder<NoiseGeneratorSettings> shapeSettings,
        Holder<NoiseGeneratorSettings> contentSettings
    ) {
        this(biomeSource, shapeSettings, contentSettings, combine(shapeSettings.value(), contentSettings.value()));
    }

    private EndShapeOverworldContentChunkGenerator(
        BiomeSource biomeSource,
        Holder<NoiseGeneratorSettings> shapeSettings,
        Holder<NoiseGeneratorSettings> contentSettings,
        NoiseGeneratorSettings combinedSettings
    ) {
        super(biomeSource, Holder.direct(combinedSettings));
        this.shapeSettings = shapeSettings;
        this.contentSettings = contentSettings;
        this.combinedSettings = combinedSettings;
    }

    public Holder<NoiseGeneratorSettings> shapeSettings() {
        return this.shapeSettings;
    }

    public Holder<NoiseGeneratorSettings> contentSettings() {
        return this.contentSettings;
    }

    public NoiseGeneratorSettings combinedSettings() {
        return this.combinedSettings;
    }

    public static NoiseGeneratorSettings combine(NoiseGeneratorSettings shape, NoiseGeneratorSettings content) {
        NoiseRouter shapeRouter = shape.noiseRouter();
        NoiseRouter contentRouter = content.noiseRouter();
        NoiseRouter combinedRouter = new NoiseRouter(
            shapeRouter.barrierNoise(),
            shapeRouter.fluidLevelFloodednessNoise(),
            shapeRouter.fluidLevelSpreadNoise(),
            shapeRouter.lavaNoise(),
            contentRouter.temperature(),
            contentRouter.vegetation(),
            contentRouter.continents(),
            contentRouter.erosion(),
            contentRouter.depth(),
            contentRouter.ridges(),
            shapeRouter.initialDensityWithoutJaggedness(),
            shapeRouter.finalDensity(),
            contentRouter.veinToggle(),
            contentRouter.veinRidged(),
            contentRouter.veinGap()
        );
        return new NoiseGeneratorSettings(
            shape.noiseSettings(),
            content.defaultBlock(),
            shape.defaultFluid(),
            combinedRouter,
            SurfaceRuleData.overworldLike(false, false, true),
            content.spawnTarget(),
            shape.seaLevel(),
            content.disableMobGeneration(),
            shape.isAquifersEnabled(),
            content.oreVeinsEnabled(),
            shape.useLegacyRandomSource()
        );
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return ScarboroughFairRegistries.END_SHAPE_OVERWORLD_CONTENT.get();
    }
}
