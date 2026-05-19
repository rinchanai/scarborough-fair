package dev.rinchan.scarboroughfair.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Fluids;

public class WaterIslandFeature extends Feature<NoneFeatureConfiguration> {
    private static final int CELL_CHUNKS = 12;
    private static final int CELL_SIZE = CELL_CHUNKS * 16;

    public WaterIslandFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        int chunkX = Math.floorDiv(context.origin().getX(), 16);
        int chunkZ = Math.floorDiv(context.origin().getZ(), 16);
        int minX = chunkX * 16;
        int minZ = chunkZ * 16;
        boolean changed = false;

        int cellX = Math.floorDiv(chunkX, CELL_CHUNKS);
        int cellZ = Math.floorDiv(chunkZ, CELL_CHUNKS);
        for (int cx = cellX - 1; cx <= cellX + 1; cx++) {
            for (int cz = cellZ - 1; cz <= cellZ + 1; cz++) {
                WaterIsland island = islandForCell(level.getSeed(), cx, cz);
                if (island == null || !intersectsChunk(island, minX, minZ)) {
                    continue;
                }
                changed |= placeChunkPart(level, island, minX, minZ);
            }
        }
        return changed;
    }

    private static WaterIsland islandForCell(long seed, int cellX, int cellZ) {
        long mixed = seed ^ 0x4F1BBCDCB6A5D63DL ^ ((long) cellX * 341873128712L) ^ ((long) cellZ * 132897987541L);
        RandomSource random = RandomSource.create(mixed);
        if (random.nextInt(18) != 0) {
            return null;
        }
        int centerX = cellX * CELL_SIZE + random.nextInt(CELL_SIZE);
        int centerZ = cellZ * CELL_SIZE + random.nextInt(CELL_SIZE);
        int radius = 24 + random.nextInt(25);
        return new WaterIsland(centerX, centerZ, radius);
    }

    private static boolean intersectsChunk(WaterIsland island, int minX, int minZ) {
        int maxX = minX + 15;
        int maxZ = minZ + 15;
        int nearestX = Math.max(minX, Math.min(island.x, maxX));
        int nearestZ = Math.max(minZ, Math.min(island.z, maxZ));
        int dx = nearestX - island.x;
        int dz = nearestZ - island.z;
        return dx * dx + dz * dz <= island.radius * island.radius;
    }

    private static boolean placeChunkPart(WorldGenLevel level, WaterIsland island, int minX, int minZ) {
        int centerY = WaterPoolFeature.surfaceY(level, island.x, island.z);
        if (centerY < level.getMinBuildHeight() + 4 || centerY > 150 || !WaterPoolFeature.isIslandSurface(level, island.x, centerY, island.z)) {
            return false;
        }

        boolean changed = false;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int radiusSq = island.radius * island.radius;
        RandomSource random = RandomSource.create(((long) island.x << 32) ^ island.z ^ level.getSeed());
        for (int x = minX; x < minX + 16; x++) {
            for (int z = minZ; z < minZ + 16; z++) {
                int dx = x - island.x;
                int dz = z - island.z;
                int distanceSq = dx * dx + dz * dz;
                if (distanceSq > radiusSq) {
                    continue;
                }
                int y = WaterPoolFeature.surfaceY(level, x, z);
                if (Math.abs(y - centerY) > 7 || !WaterPoolFeature.isIslandSurface(level, x, y, z)) {
                    continue;
                }
                pos.set(x, y, z);
                if (!level.ensureCanWrite(pos)) {
                    continue;
                }
                level.setBlock(pos, Blocks.WATER.defaultBlockState(), 2);
                level.scheduleTick(pos, Fluids.WATER, 1);
                changed = true;

                double edge = Math.sqrt(distanceSq) / island.radius;
                if (edge > 0.72 && random.nextFloat() < 0.16F) {
                    pos.set(x, y - 1, z);
                    if (level.ensureCanWrite(pos)) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
        return changed;
    }

    private record WaterIsland(int x, int z, int radius) {
    }
}
