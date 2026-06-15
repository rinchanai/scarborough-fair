package dev.rinchan.scarboroughfair.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.rinchan.scarboroughfair.ScarboroughFair;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

public class ScarboroughFairBiomeSource extends BiomeSource {
    public static final MapCodec<ScarboroughFairBiomeSource> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            RegistryOps.retrieveElement(ScarboroughFair.MAIN_ISLAND_BIOME),
            RegistryOps.retrieveElement(ScarboroughFair.OUTER_ISLANDS_BIOME)
        ).apply(instance, instance.stable(ScarboroughFairBiomeSource::new))
    );

    private final Holder<Biome> mainIsland;
    private final Holder<Biome> outerIslands;

    private ScarboroughFairBiomeSource(Holder<Biome> mainIsland, Holder<Biome> outerIslands) {
        this.mainIsland = mainIsland;
        this.outerIslands = outerIslands;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.of(this.mainIsland, this.outerIslands);
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        int blockX = QuartPos.toBlock(x);
        int blockZ = QuartPos.toBlock(z);
        int sectionX = SectionPos.blockToSectionCoord(blockX);
        int sectionZ = SectionPos.blockToSectionCoord(blockZ);
        if ((long) sectionX * (long) sectionX + (long) sectionZ * (long) sectionZ <= 4096L) {
            return this.mainIsland;
        }
        return this.outerIslands;
    }
}
