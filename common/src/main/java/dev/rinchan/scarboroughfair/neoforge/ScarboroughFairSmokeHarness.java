package dev.rinchan.scarboroughfair.neoforge;

import dev.rinchan.scarboroughfair.ScarboroughFair;
import dev.rinchan.scarboroughfair.worldgen.EndShapeOverworldContentChunkGenerator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.SurfaceRuleData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

final class ScarboroughFairSmokeHarness {
    private ScarboroughFairSmokeHarness() {
    }

    static void register() {
        NeoForge.EVENT_BUS.addListener(ScarboroughFairSmokeHarness::onServerStarted);
    }

    private static void onServerStarted(ServerStartedEvent event) {
        var server = event.getServer();
        var level = server.getLevel(ScarboroughFair.LEVEL);
        if (level == null) {
            throw new IllegalStateException("Scarborough Fair dimension did not load");
        }
        if (!(level.getChunkSource().getGenerator() instanceof EndShapeOverworldContentChunkGenerator generator)) {
            throw new IllegalStateException("Scarborough Fair did not load its End-shape/Overworld-content generator");
        }
        if (!(generator.getBiomeSource() instanceof MultiNoiseBiomeSource)) {
            throw new IllegalStateException("Scarborough Fair did not use the vanilla multi-noise biome source");
        }
        verifySettingsComposition(generator);

        int stone = 0;
        int grass = 0;
        int dirt = 0;
        int endStone = 0;
        int logs = 0;
        Set<ResourceLocation> biomes = new HashSet<>();
        Set<Holder<Biome>> biomeHolders = new HashSet<>();
        boolean hasGeneratedFeatures = false;
        boolean hasPassiveSpawns = false;

        List<int[]> chunkSamples = new ArrayList<>();
        chunkSamples.add(new int[] {0, 0});
        for (int cx = 64; cx <= 72; cx++) {
            for (int cz = -4; cz <= 4; cz++) {
                chunkSamples.add(new int[] {cx, cz});
            }
        }
        chunkSamples.add(new int[] {-64, 4});
        chunkSamples.add(new int[] {128, 16});
        chunkSamples.add(new int[] {-128, -16});
        chunkSamples.add(new int[] {256, 64});
        chunkSamples.add(new int[] {-256, -64});
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int[] sample : chunkSamples) {
            int cx = sample[0];
            int cz = sample[1];
            var chunk = level.getChunk(cx, cz, ChunkStatus.FULL, true);
            int minX = cx * 16;
            int minZ = cz * 16;
            for (int x = minX; x < minX + 16; x++) {
                for (int z = minZ; z < minZ + 16; z++) {
                    var biomeHolder = level.getBiome(pos.set(x, 80, z));
                    ResourceLocation biomeId = biomeHolder.unwrapKey().orElseThrow().location();
                    if (!biomeId.getNamespace().equals("minecraft")) {
                        throw new IllegalStateException("Scarborough Fair generated a non-vanilla biome: " + biomeId);
                    }
                    biomes.add(biomeId);
                    biomeHolders.add(biomeHolder);
                    var biome = biomeHolder.value();
                    hasGeneratedFeatures |= biome.getGenerationSettings().features().stream().anyMatch(features -> features.size() > 0);
                    hasPassiveSpawns |= !biome.getMobSettings().getMobs(MobCategory.CREATURE).isEmpty();
                    for (int y = 0; y < 128; y++) {
                        var state = chunk.getBlockState(pos.set(x, y, z));
                        if (state.is(Blocks.STONE)) {
                            stone++;
                        } else if (state.is(Blocks.GRASS_BLOCK)) {
                            grass++;
                        } else if (state.is(Blocks.DIRT)) {
                            dirt++;
                        } else if (state.is(Blocks.END_STONE)) {
                            endStone++;
                        } else if (state.is(Blocks.OAK_LOG) || state.is(Blocks.BIRCH_LOG)) {
                            logs++;
                        }
                    }
                }
            }
        }

        var structureRegistry = server.registryAccess().registryOrThrow(Registries.STRUCTURE);
        boolean hasEligibleStructure = structureRegistry.holders().anyMatch(
            structure -> biomeHolders.stream().anyMatch(structure.value().biomes()::contains)
        );

        System.out.println(
            "SCARBOROUGH_FAIR_SMOKE stone=" + stone
                + " grass=" + grass
                + " dirt=" + dirt
                + " endStone=" + endStone
                + " logs=" + logs
                + " biomeCount=" + biomes.size()
                + " eligibleStructure=" + hasEligibleStructure
                + " biomes=" + biomes
        );
        if (stone <= 0 || grass <= 0 || dirt <= 0 || endStone != 0) {
            throw new IllegalStateException("Expected End island density with Overworld stone and grass/dirt surfaces, and no End stone");
        }
        if (biomes.size() < 2 || !hasGeneratedFeatures || !hasPassiveSpawns || !hasEligibleStructure) {
            throw new IllegalStateException("Expected multiple vanilla Overworld biomes with their own features, passive spawns, and structures");
        }
        server.halt(false);
    }

    private static void verifySettingsComposition(EndShapeOverworldContentChunkGenerator generator) {
        if (!generator.shapeSettings().is(NoiseGeneratorSettings.END)
                || !generator.contentSettings().is(NoiseGeneratorSettings.OVERWORLD)) {
            throw new IllegalStateException("Generator settings do not point to vanilla End and Overworld producers");
        }
        var shape = generator.shapeSettings().value();
        var content = generator.contentSettings().value();
        var combined = generator.combinedSettings();
        if (!combined.noiseSettings().equals(shape.noiseSettings())
                || !combined.noiseRouter().finalDensity().equals(shape.noiseRouter().finalDensity())
                || !combined.noiseRouter().initialDensityWithoutJaggedness().equals(shape.noiseRouter().initialDensityWithoutJaggedness())) {
            throw new IllegalStateException("Generator did not preserve vanilla End terrain density");
        }
        if (!combined.defaultBlock().equals(content.defaultBlock())
                || !combined.surfaceRule().equals(SurfaceRuleData.overworldLike(false, false, true))
                || !combined.noiseRouter().temperature().equals(content.noiseRouter().temperature())
                || !combined.noiseRouter().vegetation().equals(content.noiseRouter().vegetation())) {
            throw new IllegalStateException("Generator did not preserve vanilla Overworld content settings");
        }
    }
}
