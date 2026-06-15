package dev.rinchan.scarboroughfair.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.rinchan.scarboroughfair.ScarboroughFairConfig;
import dev.rinchan.scarboroughfair.registry.ScarboroughFairRegistries;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.neoforge.common.world.ModifiableStructureInfo;
import net.neoforged.neoforge.common.world.StructureModifier;

public record AddIslandBiomesToMatchingStructuresModifier(
    HolderSet<Biome> sourceBiomes,
    HolderSet<Biome> additionalBiomes
) implements StructureModifier {
    public static final MapCodec<AddIslandBiomesToMatchingStructuresModifier> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            Biome.LIST_CODEC.fieldOf("source_biomes").forGetter(AddIslandBiomesToMatchingStructuresModifier::sourceBiomes),
            Biome.LIST_CODEC.fieldOf("additional_biomes").forGetter(AddIslandBiomesToMatchingStructuresModifier::additionalBiomes)
        ).apply(instance, AddIslandBiomesToMatchingStructuresModifier::new)
    );

    @Override
    public void modify(Holder<Structure> structure, Phase phase, ModifiableStructureInfo.StructureInfo.Builder builder) {
        if (phase != Phase.ADD || !ScarboroughFairConfig.USE_OVERWORLD_STRUCTURE_LIST.get()) {
            return;
        }
        HolderSet<Biome> currentBiomes = builder.getStructureSettings().getBiomes();
        if (currentBiomes.stream().noneMatch(this.sourceBiomes::contains)) {
            return;
        }

        List<Holder<Biome>> mergedBiomes = new ArrayList<>();
        currentBiomes.stream().forEach(mergedBiomes::add);
        this.additionalBiomes.stream().forEach(biome -> {
            if (!mergedBiomes.contains(biome)) {
                mergedBiomes.add(biome);
            }
        });
        builder.getStructureSettings().setBiomes(HolderSet.direct(mergedBiomes));
    }

    @Override
    public MapCodec<? extends StructureModifier> codec() {
        return ScarboroughFairRegistries.ADD_ISLAND_BIOMES_TO_MATCHING_STRUCTURES.get();
    }
}
