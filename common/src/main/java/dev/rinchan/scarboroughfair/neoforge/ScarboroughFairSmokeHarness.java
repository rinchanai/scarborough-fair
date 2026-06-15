package dev.rinchan.scarboroughfair.neoforge;

import dev.rinchan.scarboroughfair.ScarboroughFair;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.status.ChunkStatus;
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

        int grass = 0;
        int dirt = 0;
        int stone = 0;
        int water = 0;
        int outerLogs = 0;
        for (int cx = 64; cx <= 72; cx++) {
            for (int cz = -4; cz <= 4; cz++) {
                var chunk = level.getChunk(cx, cz, ChunkStatus.FULL, true);
                int minX = cx * 16;
                int minZ = cz * 16;
                for (int x = minX; x < minX + 16; x++) {
                    for (int z = minZ; z < minZ + 16; z++) {
                        for (int y = 0; y < 160; y++) {
                            var state = chunk.getBlockState(new net.minecraft.core.BlockPos(x, y, z));
                            if (state.is(Blocks.GRASS_BLOCK)) {
                                grass++;
                            } else if (state.is(Blocks.DIRT)) {
                                dirt++;
                            } else if (state.is(Blocks.STONE)) {
                                stone++;
                            } else if (state.is(Blocks.WATER)) {
                                water++;
                            } else if (state.is(Blocks.OAK_LOG) || state.is(Blocks.BIRCH_LOG)) {
                                outerLogs++;
                            }
                        }
                    }
                }
            }
        }
        var mainBiome = level.getBiome(new net.minecraft.core.BlockPos(0, 80, 0)).unwrapKey().orElseThrow().location();
        var outerBiome = level.getBiome(new net.minecraft.core.BlockPos(1400, 80, 0)).unwrapKey().orElseThrow().location();
        System.out.println("SCARBOROUGH_FAIR_SMOKE grass=" + grass + " dirt=" + dirt + " stone=" + stone + " water=" + water + " outerLogs=" + outerLogs + " mainBiome=" + mainBiome + " outerBiome=" + outerBiome);
        if (grass <= 0 || dirt <= 0 || stone <= 0) {
            throw new IllegalStateException("Scarborough Fair smoke failed: expected grass surface, dirt sides, and stone interior");
        }
        if (!mainBiome.equals(ScarboroughFair.id("main_island")) || !outerBiome.equals(ScarboroughFair.id("outer_islands"))) {
            throw new IllegalStateException("Scarborough Fair smoke failed: expected split main/outer island biomes");
        }
        if (outerLogs <= 0) {
            throw new IllegalStateException("Scarborough Fair smoke failed: expected sparse outer-island ordinary trees");
        }
        server.halt(false);
    }
}
